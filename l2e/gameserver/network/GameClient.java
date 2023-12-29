package l2e.gameserver.network;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import l2e.commons.util.FloodProtectorAction;
import l2e.commons.util.FloodProtectorConfig;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.holder.CharNameHolder;
import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.data.holder.SecPasswordHolder;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.instancemanager.DoubleSessionManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.CharSelectInfoPackage;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.service.academy.AcademyList;
import l2e.gameserver.model.skills.effects.AbnormalEffect;
import l2e.gameserver.model.zone.type.FunPvpZone;
import l2e.gameserver.network.communication.AuthServerCommunication;
import l2e.gameserver.network.communication.SessionKey;
import l2e.gameserver.network.communication.gameserverpackets.PlayerLogout;
import l2e.gameserver.network.serverpackets.CharInfo;
import l2e.gameserver.network.serverpackets.GameServerPacket;
import l2e.gameserver.network.serverpackets.ServerClose;
import org.nio.impl.MMOClient;
import org.nio.impl.MMOConnection;
import org.strixplatform.StrixPlatform;
import org.strixplatform.network.cipher.StrixGameCrypt;
import org.strixplatform.utils.StrixClientData;
import smartguard.integration.SmartClient;

public final class GameClient extends MMOClient<MMOConnection<GameClient>> {
   protected static final Logger _log = Logger.getLogger(GameClient.class.getName());
   protected static final Logger _logAccounting = Logger.getLogger("accounting");
   private GameClient.GameClientState _state;
   private final InetAddress _addr;
   private SessionKey _sessionId;
   private Player _activeChar;
   private final ReentrantLock _activeCharLock = new ReentrantLock();
   private SecPasswordHolder _secondaryAuth;
   private boolean _isAuthedGG;
   private final long _connectionStartTime;
   private CharSelectInfoPackage[] _charSlotMapping = null;
   private final Map<String, FloodProtectorAction> _floodProtectors = new HashMap<>();
   protected final ScheduledFuture<?> _autoSaveInDB;
   protected ScheduledFuture<?> _cleanupTask = null;
   public StrixGameCrypt _gameCrypt = null;
   private StrixClientData _clientData;
   private String _playerName = "";
   private int _playerId = 0;
   private int _revision = 0;
   private int _playerID;
   private String _login;
   private GameClient.LockType _lockType;
   private boolean _isDetached = false;
   private int _failedPackets = 0;
   private String _realIpAddress;
   private String _hwid;

   public GameClient(MMOConnection<GameClient> con) {
      super(con);
      this._state = GameClient.GameClientState.CONNECTED;
      this._connectionStartTime = System.currentTimeMillis();
      this._gameCrypt = new StrixGameCrypt();
      if (Config.CHAR_STORE_INTERVAL > 0) {
         this._autoSaveInDB = ThreadPoolManager.getInstance()
            .scheduleAtFixedRate(new GameClient.AutoSaveTask(), 300000L, (long)Config.CHAR_STORE_INTERVAL * 60000L);
      } else {
         this._autoSaveInDB = null;
      }

      try {
         this._addr = con != null ? con.getSocket().getInetAddress() : InetAddress.getLocalHost();
      } catch (UnknownHostException var4) {
         throw new Error("Unable to determine localhost address.");
      }

      for(FloodProtectorConfig config : Config.FLOOD_PROTECTORS) {
         this._floodProtectors.put(config.FLOOD_PROTECTOR_TYPE, new FloodProtectorAction(this, config));
      }
   }

   public byte[] enableCrypt() {
      byte[] key = BlowFishKeygen.getRandomKey();
      this._gameCrypt.setKey(key);
      return key;
   }

   public GameClient.GameClientState getState() {
      return this._state;
   }

   public void setState(GameClient.GameClientState state) {
      this._state = state;
   }

   public InetAddress getConnectionAddress() {
      return this._addr;
   }

   public long getConnectionStartTime() {
      return this._connectionStartTime;
   }

   @Override
   public boolean decrypt(ByteBuffer buf, int size) {
      this._gameCrypt.decrypt(buf.array(), buf.position(), size);
      return true;
   }

   @Override
   public boolean encrypt(ByteBuffer buf, int size) {
      this._gameCrypt.encrypt(buf.array(), buf.position(), size);
      ((Buffer)buf).position(buf.position() + size);
      return true;
   }

   public Player getActiveChar() {
      return this._activeChar;
   }

   public void setActiveChar(Player pActiveChar) {
      this._activeChar = pActiveChar;
   }

   public ReentrantLock getActiveCharLock() {
      return this._activeCharLock;
   }

   public void setGameGuardOk(boolean val) {
      this._isAuthedGG = val;
   }

   public boolean isAuthedGG() {
      return this._isAuthedGG;
   }

   public void setSessionId(SessionKey sk) {
      this._sessionId = sk;
   }

   public SessionKey getSessionId() {
      return this._sessionId;
   }

   public void sendPacket(GameServerPacket gsp) {
      if (!this._isDetached && gsp != null) {
         if (!gsp.isInvisible() || this.getActiveChar() == null || this.getActiveChar().canOverrideCond(PcCondOverride.SEE_ALL_PLAYERS)) {
            if (this.isConnected()) {
               this.getConnection().sendPacket(gsp);
            }
         }
      }
   }

   public boolean isDetached() {
      return this._isDetached;
   }

   public void setDetached(boolean b) {
      this._isDetached = b;
   }

   public byte markToDeleteChar(int charslot) {
      int objid = this.getObjectIdForSlot(charslot);
      if (objid < 0) {
         return -1;
      } else {
         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT clanId FROM characters WHERE charId=?");
         ) {
            statement.setInt(1, objid);
            byte answer = 0;

            try (ResultSet rs = statement.executeQuery()) {
               int clanId = rs.next() ? rs.getInt(1) : 0;
               if (clanId != 0) {
                  Clan clan = ClanHolder.getInstance().getClan(clanId);
                  if (clan == null) {
                     answer = 0;
                  } else if (clan.getLeaderId() == objid) {
                     answer = 2;
                  } else {
                     answer = 1;
                  }
               }

               if (answer == 0) {
                  if (Config.DELETE_DAYS == 0) {
                     deleteCharByObjId(objid);
                  } else {
                     try (PreparedStatement ps2 = con.prepareStatement("UPDATE characters SET deletetime=? WHERE charId=?")) {
                        ps2.setLong(1, System.currentTimeMillis() + (long)Config.DELETE_DAYS * 86400000L);
                        ps2.setInt(2, objid);
                        ps2.execute();
                     }
                  }

                  LogRecord record = new LogRecord(Level.WARNING, "Delete");
                  record.setParameters(new Object[]{objid, this});
                  _logAccounting.log(record);
               }
            }

            return answer;
         } catch (Exception var96) {
            _log.log(Level.SEVERE, "Error updating delete time of character.", (Throwable)var96);
            return -1;
         }
      }
   }

   public void saveCharToDisk() {
      try {
         if (this.getActiveChar() != null) {
            this.getActiveChar().store();
            if (Config.UPDATE_ITEMS_ON_CHAR_STORE) {
               this.getActiveChar().getInventory().updateDatabase();
               this.getActiveChar().getWarehouse().updateDatabase();
            }
         }
      } catch (Exception var2) {
         _log.log(Level.SEVERE, "Error saving character..", (Throwable)var2);
      }
   }

   public void markRestoredChar(int charslot) {
      int objid = this.getObjectIdForSlot(charslot);
      if (objid >= 0) {
         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("UPDATE characters SET deletetime=0 WHERE charId=?");
         ) {
            statement.setInt(1, objid);
            statement.execute();
         } catch (Exception var35) {
            _log.log(Level.SEVERE, "Error restoring character.", (Throwable)var35);
         }

         LogRecord record = new LogRecord(Level.WARNING, "Restore");
         record.setParameters(new Object[]{objid, this});
         _logAccounting.log(record);
      }
   }

   public static void deleteCharByObjId(int objid) {
      if (objid >= 0) {
         CharNameHolder.getInstance().removeName(objid);

         try (Connection con = DatabaseFactory.getInstance().getConnection()) {
            PreparedStatement statement = con.prepareStatement("DELETE FROM character_contacts WHERE charId=? OR contactId=?");
            statement.setInt(1, objid);
            statement.setInt(2, objid);
            statement.execute();
            statement.close();
            statement = con.prepareStatement("DELETE FROM character_friends WHERE charId=? OR friendId=?");
            statement.setInt(1, objid);
            statement.setInt(2, objid);
            statement.execute();
            statement.close();
            statement = con.prepareStatement("DELETE FROM character_hennas WHERE charId=?");
            statement.setInt(1, objid);
            statement.execute();
            statement.close();
            statement = con.prepareStatement("DELETE FROM character_macroses WHERE charId=?");
            statement.setInt(1, objid);
            statement.execute();
            statement.close();
            statement = con.prepareStatement("DELETE FROM character_quests WHERE charId=?");
            statement.setInt(1, objid);
            statement.execute();
            statement.close();
            statement = con.prepareStatement("DELETE FROM character_quest_global_data WHERE charId=?");
            statement.setInt(1, objid);
            statement.executeUpdate();
            statement.close();
            statement = con.prepareStatement("DELETE FROM character_recipebook WHERE charId=?");
            statement.setInt(1, objid);
            statement.execute();
            statement.close();
            statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE charId=?");
            statement.setInt(1, objid);
            statement.execute();
            statement.close();
            statement = con.prepareStatement("DELETE FROM character_skills WHERE charId=?");
            statement.setInt(1, objid);
            statement.execute();
            statement.close();
            statement = con.prepareStatement("DELETE FROM character_skills_save WHERE charId=?");
            statement.setInt(1, objid);
            statement.execute();
            statement.close();
            statement = con.prepareStatement("DELETE FROM character_subclasses WHERE charId=?");
            statement.setInt(1, objid);
            statement.execute();
            statement.close();
            statement = con.prepareStatement("DELETE FROM heroes WHERE charId=?");
            statement.setInt(1, objid);
            statement.execute();
            statement.close();
            statement = con.prepareStatement("DELETE FROM olympiad_nobles WHERE charId=?");
            statement.setInt(1, objid);
            statement.execute();
            statement.close();
            statement = con.prepareStatement("DELETE FROM seven_signs WHERE charId=?");
            statement.setInt(1, objid);
            statement.execute();
            statement.close();
            statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id IN (SELECT object_id FROM items WHERE items.owner_id=?)");
            statement.setInt(1, objid);
            statement.execute();
            statement.close();
            statement = con.prepareStatement("DELETE FROM item_attributes WHERE itemId IN (SELECT object_id FROM items WHERE items.owner_id=?)");
            statement.setInt(1, objid);
            statement.execute();
            statement.close();
            statement = con.prepareStatement("DELETE FROM items WHERE owner_id=?");
            statement.setInt(1, objid);
            statement.execute();
            statement.close();
            statement = con.prepareStatement("DELETE FROM merchant_lease WHERE player_id=?");
            statement.setInt(1, objid);
            statement.execute();
            statement.close();
            statement = con.prepareStatement("DELETE FROM raidboss_points WHERE charId=?");
            statement.setInt(1, objid);
            statement.execute();
            statement.close();
            statement = con.prepareStatement("DELETE FROM character_instance_time WHERE charId=?");
            statement.setInt(1, objid);
            statement.execute();
            statement.close();
            statement = con.prepareStatement("DELETE FROM character_variables WHERE obj_id=?");
            statement.setInt(1, objid);
            statement.execute();
            statement.close();
            statement = con.prepareStatement("DELETE FROM characters WHERE charId=?");
            statement.setInt(1, objid);
            statement.execute();
            statement.close();
            if (Config.ALLOW_WEDDING) {
               statement = con.prepareStatement("DELETE FROM mods_wedding WHERE player1Id = ? OR player2Id = ?");
               statement.setInt(1, objid);
               statement.setInt(2, objid);
               statement.execute();
               statement.close();
            }
         } catch (Exception var14) {
            _log.log(Level.SEVERE, "Error deleting character.", (Throwable)var14);
         }
      }
   }

   public Player loadCharFromDisk(int charslot) {
      int objId = this.getObjectIdForSlot(charslot);
      if (objId < 0) {
         return null;
      } else {
         Player character = World.getInstance().getPlayer(objId);
         if (character != null) {
            if (character.isInOfflineMode()) {
               character.deleteMe();
               return null;
            } else {
               _log.severe("Attempt of double login: " + character.getName() + "(" + objId + ") " + this.getLogin());
               if (character.getClient() != null) {
                  character.getClient().closeNow();
               } else {
                  character.deleteMe();
               }

               return null;
            }
         } else {
            character = Player.load(objId);
            if (character != null) {
               character.setRunning();
               character.standUp();
               character.refreshOverloaded();
               character.refreshExpertisePenalty();
               character.setOnlineStatus(true, false);
            } else {
               _log.severe("could not restore in slot: " + charslot);
            }

            return character;
         }
      }
   }

   public void setCharSelection(CharSelectInfoPackage[] chars) {
      this._charSlotMapping = chars;
   }

   public CharSelectInfoPackage getCharSelection(int charslot) {
      return this._charSlotMapping != null && charslot >= 0 && charslot < this._charSlotMapping.length ? this._charSlotMapping[charslot] : null;
   }

   public int getSlotForObjectId(int objectId) {
      for(int slotIdx = 0; slotIdx < this._charSlotMapping.length; ++slotIdx) {
         CharSelectInfoPackage p = this._charSlotMapping[slotIdx];
         if (p != null && p.getObjectId() == objectId) {
            return slotIdx;
         }
      }

      return -1;
   }

   public SecPasswordHolder getSecondaryAuth() {
      return this._secondaryAuth;
   }

   public void close(GameServerPacket gsp) {
      if (this.getConnection() != null) {
         this.getConnection().close(gsp);
      }
   }

   private int getObjectIdForSlot(int charslot) {
      CharSelectInfoPackage info = this.getCharSelection(charslot);
      if (info == null) {
         _log.warning(this.toString() + " tried to delete Character in slot " + charslot + " but no characters exits at that slot.");
         return -1;
      } else {
         return info.getObjectId();
      }
   }

   @Override
   protected void onForcedDisconnection() {
      LogRecord record = new LogRecord(Level.WARNING, "Disconnected abnormally");
      record.setParameters(new Object[]{this});
      _logAccounting.log(record);
   }

   @Override
   protected void onDisconnection() {
      if (Config.DISCONNECT_SYSTEM_ENABLED) {
         try {
            if (this.getActiveChar() != null && !this.getActiveChar().canOfflineMode(this.getActiveChar(), false)) {
               this.getActiveChar().getAppearance().setVisibleTitle(Config.DISCONNECT_TITLE);
               this.getActiveChar().getAppearance().setDisplayName(true);
               String color = Config.DISCONNECT_TITLECOLOR.charAt(4)
                  + String.valueOf(Config.DISCONNECT_TITLECOLOR.charAt(5))
                  + Config.DISCONNECT_TITLECOLOR.charAt(2)
                  + Config.DISCONNECT_TITLECOLOR.charAt(3)
                  + Config.DISCONNECT_TITLECOLOR.charAt(0)
                  + Config.DISCONNECT_TITLECOLOR.charAt(1);
               this.getActiveChar().getAppearance().setTitleColor(Integer.decode("0x" + color));
               this.getActiveChar().broadcastPacket(new CharInfo(this.getActiveChar(), null));
            }
         } catch (Exception var10) {
            _log.warning("onDisconnection " + var10.getMessage());
         } finally {
            long delay = this.getActiveChar() != null
               ? (long)this.getActiveChar().getVarInt("logoutTime", Config.DISCONNECT_TIMEOUT)
               : (long)Config.DISCONNECT_TIMEOUT;
            ThreadPoolManager.getInstance().schedule(new GameClient.DisconnectTask(), delay * 1000L);
         }
      } else {
         try {
            ThreadPoolManager.getInstance().execute(new GameClient.DisconnectTask());
         } catch (RejectedExecutionException var9) {
            _log.warning("onDisconnection " + var9.getMessage());
         }
      }
   }

   public void closeNow() {
      this._isDetached = true;
      this.close(ServerClose.STATIC_PACKET);
      synchronized(this) {
         if (this._cleanupTask != null) {
            this.cancelCleanup();
         }

         this._cleanupTask = ThreadPoolManager.getInstance().schedule(new GameClient.CleanupTask(), 0L);
      }
   }

   @Override
   public String toString() {
      try {
         InetAddress address = this.getConnection().getSocket().getInetAddress();
         switch(this.getState()) {
            case CONNECTED:
               return "[IP: " + (address == null ? "disconnected" : address.getHostAddress()) + "]";
            case AUTHED:
               return "[Account: " + this.getLogin() + " - IP: " + (address == null ? "disconnected" : address.getHostAddress()) + "]";
            case ENTERING:
            case IN_GAME:
               return "[Character: "
                  + (this.getActiveChar() == null ? "disconnected" : this.getActiveChar().getName() + "[" + this.getActiveChar().getObjectId() + "]")
                  + " - Account: "
                  + this.getLogin()
                  + " - IP: "
                  + (address == null ? "disconnected" : address.getHostAddress())
                  + "]";
            default:
               throw new IllegalStateException("Missing state on switch");
         }
      } catch (NullPointerException var2) {
         return "[Character read failed due to disconnect]";
      }
   }

   public void cleanMe(boolean fast) {
      try {
         synchronized(this) {
            if (this._cleanupTask == null) {
               this._cleanupTask = ThreadPoolManager.getInstance().schedule(new GameClient.CleanupTask(), fast ? 5L : 15000L);
            }
         }
      } catch (Exception var5) {
         _log.log(Level.WARNING, "Error during cleanup.", (Throwable)var5);
      }
   }

   public boolean handleCheat(String punishment) {
      if (this._activeChar != null) {
         Util.handleIllegalPlayerAction(this._activeChar, this.toString() + ": " + punishment);
         return true;
      } else {
         Logger _logAudit = Logger.getLogger("audit");
         _logAudit.log(Level.INFO, "AUDIT: Client " + this.toString() + " kicked for reason: " + punishment);
         this.closeNow();
         return false;
      }
   }

   public void onBufferUnderflow() {
      if (this._failedPackets++ >= 10 && this._state == GameClient.GameClientState.CONNECTED) {
         if (Config.PACKET_HANDLER_DEBUG) {
            _log.severe("Client " + this.toString() + " - Disconnected, too many buffer underflows in non-authed state.");
         }

         this.closeNow();
      }
   }

   public void onUnknownPacket() {
      if (this._state == GameClient.GameClientState.CONNECTED) {
         if (Config.PACKET_HANDLER_DEBUG) {
            _log.severe("Client " + this.toString() + " - Disconnected, too many unknown packets in non-authed state.");
         }

         this.closeNow();
      }
   }

   private boolean cancelCleanup() {
      Future<?> task = this._cleanupTask;
      if (task != null) {
         this._cleanupTask = null;
         return task.cancel(true);
      } else {
         return false;
      }
   }

   public int getRevision() {
      return this._revision;
   }

   public void setRevision(int revision) {
      this._revision = revision;
   }

   public int getPlayerId() {
      return this._playerId;
   }

   public void setPlayerId(int plId) {
      this._playerId = plId;
   }

   public final String getPlayerName() {
      return this._playerName;
   }

   public void setPlayerName(String name) {
      this._playerName = name;
   }

   public GameClient.LockType getLockType() {
      return this._lockType;
   }

   public void setLockType(GameClient.LockType lockType) {
      this._lockType = lockType;
   }

   public String getLogin() {
      return this._login;
   }

   public void setLogin(String login) {
      this._login = login;
      if (Config.SECOND_AUTH_ENABLED) {
         this._secondaryAuth = new SecPasswordHolder(this);
      }
   }

   public int getPlayerID() {
      return this._playerID;
   }

   public void setPlayerID(int playerID) {
      this._playerID = playerID;
   }

   public void setStrixClientData(StrixClientData clientData) {
      this._clientData = clientData;
   }

   public StrixClientData getStrixClientData() {
      return this._clientData;
   }

   public void updateHWID() {
      String var1 = Config.PROTECTION;
      switch(var1) {
         case "STRIX":
            if (StrixPlatform.getInstance().isPlatformEnabled() && this.getStrixClientData() != null) {
               this._hwid = this.getStrixClientData().getClientHWID();
            }
            break;
         case "SMART":
            this._hwid = SmartClient.getHwid(this);
         case "ANTICHEAT":
            break;
         case "NONE":
            this._hwid = "N/A";
            break;
         default:
            this._hwid = "N/A";
      }
   }

   public void setHWID(String hwid) {
      this._hwid = hwid;
   }

   public String getHWID() {
      return this._hwid;
   }

   public String getIPAddress() {
      return this.getRealIpAddress() != null && !this.getRealIpAddress().isEmpty()
         ? this.getRealIpAddress()
         : (this.getConnectionAddress() != null ? this.getConnectionAddress().getHostAddress() : "N/A");
   }

   public String getRealIpAddress() {
      return this._realIpAddress;
   }

   public void setRealIpAddress(String realIpAddress) {
      this._realIpAddress = realIpAddress;
   }

   public boolean checkFloodProtection(String type, String command) {
      FloodProtectorAction floodProtector = this._floodProtectors.get(type.toUpperCase());
      return floodProtector == null || floodProtector.tryPerformAction(command);
   }

   protected class AutoSaveTask implements Runnable {
      @Override
      public void run() {
         try {
            Player player = GameClient.this.getActiveChar();
            if (player != null && player.isOnline()) {
               GameClient.this.saveCharToDisk();
               if (player.hasSummon()) {
                  player.getSummon().store();
               }
            }
         } catch (Exception var2) {
            GameClient._log.log(Level.SEVERE, "Error on AutoSaveTask.", (Throwable)var2);
         }
      }
   }

   protected class CleanupTask implements Runnable {
      @Override
      public void run() {
         try {
            if (GameClient.this._autoSaveInDB != null) {
               GameClient.this._autoSaveInDB.cancel(true);
            }

            if (GameClient.this.getActiveChar() != null) {
               FunPvpZone zone = ZoneManager.getInstance().getZone(GameClient.this.getActiveChar(), FunPvpZone.class);
               if (zone != null) {
                  zone.onPlayerLogoutInside(GameClient.this.getActiveChar());
               }

               if (GameClient.this.getActiveChar().isLocked()) {
                  GameClient._log
                     .log(Level.WARNING, "Player " + GameClient.this.getActiveChar().getName() + " still performing subclass actions during disconnect.");
               }

               GameClient.this.getActiveChar().setClient(null);
               if (GameClient.this.getActiveChar().isOnline()) {
                  DoubleSessionManager.getInstance().onDisconnect(GameClient.this.getActiveChar());
                  GameClient.this.getActiveChar().deleteMe();
               }
            }

            GameClient.this.setActiveChar(null);
         } catch (Exception var5) {
            GameClient._log.log(Level.WARNING, "Error while cleanup client.", (Throwable)var5);
         } finally {
            if (GameClient.this.getSessionId() != null) {
               if (GameClient.this.isAuthed()) {
                  AuthServerCommunication.getInstance().removeAuthedClient(GameClient.this.getLogin());
                  AuthServerCommunication.getInstance().sendPacket(new PlayerLogout(GameClient.this.getLogin()));
               } else {
                  AuthServerCommunication.getInstance().removeWaitingClient(GameClient.this.getLogin());
               }
            }
         }
      }
   }

   protected class DisconnectTask implements Runnable {
      @Override
      public void run() {
         boolean fast = true;

         try {
            if (GameClient.this.getActiveChar() != null && !GameClient.this.isDetached()) {
               GameClient.this.setDetached(true);
               if (GameClient.this.getActiveChar().canOfflineMode(GameClient.this.getActiveChar(), false)) {
                  GameClient.this.getActiveChar().leaveParty();
                  if (GameClient.this.getActiveChar().hasSummon()) {
                     GameClient.this.getActiveChar().getSummon().setRestoreSummon(true);
                     GameClient.this.getActiveChar().getSummon().unSummon(GameClient.this.getActiveChar());
                     if (GameClient.this.getActiveChar().getSummon() != null) {
                        GameClient.this.getActiveChar().getSummon().broadcastNpcInfo(0);
                     }
                  }

                  if (Config.OFFLINE_SET_NAME_COLOR) {
                     GameClient.this.getActiveChar().getAppearance().setNameColor(Config.OFFLINE_NAME_COLOR);
                     GameClient.this.getActiveChar().broadcastCharInfo();
                  }

                  GameClient.this.getActiveChar().stopOnlineRewardTask();
                  GameClient.this.getActiveChar().setOfflineMode(true);
                  if (Config.OFFLINE_SET_VISUAL_EFFECT) {
                     GameClient.this.getActiveChar().startAbnormalEffect(AbnormalEffect.SLEEP);
                  }

                  if (GameClient.this.getActiveChar().isSellingBuffs()) {
                     GameClient.this.getActiveChar().setVar("offlineBuff", String.valueOf(System.currentTimeMillis() / 1000L), -1L);
                  } else {
                     GameClient.this.getActiveChar().setVar("offline", String.valueOf(System.currentTimeMillis() / 1000L), -1L);
                  }

                  if (GameClient.this.getActiveChar().isInSearchOfAcademy()) {
                     GameClient.this.getActiveChar().setSearchforAcademy(false);
                     AcademyList.deleteFromAcdemyList(GameClient.this.getActiveChar());
                  }

                  if (!Config.DOUBLE_SESSIONS_CONSIDER_OFFLINE_TRADERS) {
                     DoubleSessionManager.getInstance().onDisconnect(GameClient.this.getActiveChar());
                  }

                  LogRecord record = new LogRecord(Level.INFO, "Entering offline mode");
                  record.setParameters(new Object[]{GameClient.this});
                  GameClient._logAccounting.log(record);
                  return;
               }

               fast = !GameClient.this.getActiveChar().isInCombat() && !GameClient.this.getActiveChar().isLocked();
            }

            GameClient.this.cleanMe(fast);
         } catch (Exception var3) {
            GameClient._log.log(Level.WARNING, "Error while disconnecting client.", (Throwable)var3);
         }
      }
   }

   public static enum GameClientState {
      CONNECTED,
      AUTHED,
      ENTERING,
      IN_GAME;
   }

   public static enum LockType {
      PLAYER_LOCK,
      ACCOUNT_LOCK,
      NONE;
   }
}
