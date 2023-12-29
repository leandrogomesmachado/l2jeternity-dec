package l2e.gameserver.data.holder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.dbutils.DbUtils;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.instancemanager.AuctionManager;
import l2e.gameserver.instancemanager.CHSiegeManager;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.instancemanager.FortSiegeManager;
import l2e.gameserver.instancemanager.SiegeManager;
import l2e.gameserver.listener.ScriptListener;
import l2e.gameserver.listener.clan.ClanWarListener;
import l2e.gameserver.listener.events.ClanWarEvent;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.ClanMember;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.Auction;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.model.entity.FortSiege;
import l2e.gameserver.model.entity.Siege;
import l2e.gameserver.model.entity.clanhall.SiegableHall;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.CreatePledge;
import l2e.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import l2e.gameserver.network.serverpackets.PledgeShowMemberListAll;
import l2e.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class ClanHolder {
   private static final Logger _log = Logger.getLogger(ClanHolder.class.getName());
   private static List<ClanWarListener> clanWarListeners = new CopyOnWriteArrayList<>();
   private final Map<Integer, Clan> _clans = new HashMap<>();

   public Clan[] getClans() {
      return this._clans.values().toArray(new Clan[this._clans.size()]);
   }

   protected ClanHolder() {
      int clanCount = 0;

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         Statement s = con.createStatement();
         ResultSet rs = s.executeQuery("SELECT clan_id FROM clan_data");
      ) {
         for(; rs.next(); ++clanCount) {
            int clanId = rs.getInt("clan_id");
            this._clans.put(clanId, new Clan(clanId));
            Clan clan = this.getClan(clanId);
            if (clan.getDissolvingExpiryTime() != 0L) {
               this.scheduleRemoveClan(clan.getId());
            }
         }
      } catch (Exception var61) {
         _log.log(Level.SEVERE, this.getClass().getSimpleName() + ": Error restoring ClanHolder.", (Throwable)var61);
      }

      _log.info(this.getClass().getSimpleName() + ": Restored " + clanCount + " clans from the database.");
      this.allianceCheck();
      this.restorewars();
   }

   public Clan getClan(int clanId) {
      return this._clans.get(clanId);
   }

   public int getClanId(int playerId) {
      int res = 0;
      Connection con = null;
      PreparedStatement statement = null;
      ResultSet rset = null;

      try {
         con = DatabaseFactory.getInstance().getConnection();
         statement = con.prepareStatement("SELECT clanid FROM `characters` WHERE charId=" + playerId + " LIMIT 1;");
         rset = statement.executeQuery();
         if (rset.next()) {
            res = rset.getInt(1);
         }
      } catch (SQLException var10) {
         _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Error check player clanId", (Throwable)var10);
      } finally {
         DbUtils.closeQuietly(con, statement, rset);
      }

      return res;
   }

   public Clan getClanByName(String clanName) {
      for(Clan clan : this.getClans()) {
         if (clan.getName().equalsIgnoreCase(clanName)) {
            return clan;
         }
      }

      return null;
   }

   public Clan createClan(Player player, String clanName) {
      if (null == player) {
         return null;
      } else {
         if (Config.DEBUG) {
            _log.info(this.getClass().getSimpleName() + ": " + player.getObjectId() + "(" + player.getName() + ") requested a clan creation.");
         }

         if (10 > player.getLevel()) {
            player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_CLAN);
            return null;
         } else if (0 != player.getClanId()) {
            player.broadcastPacket(CreatePledge.STATIC);
            return null;
         } else if (System.currentTimeMillis() < player.getClanCreateExpiryTime()) {
            player.sendPacket(SystemMessageId.YOU_MUST_WAIT_XX_DAYS_BEFORE_CREATING_A_NEW_CLAN);
            return null;
         } else if (!Util.isAlphaNumeric(clanName) || 2 > clanName.length()) {
            player.sendPacket(SystemMessageId.CLAN_NAME_INCORRECT);
            return null;
         } else if (16 < clanName.length()) {
            player.sendPacket(SystemMessageId.CLAN_NAME_TOO_LONG);
            return null;
         } else if (null != this.getClanByName(clanName)) {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_EXISTS);
            sm.addString(clanName);
            player.sendPacket(sm);
            SystemMessage var6 = null;
            return null;
         } else {
            Clan clan = new Clan(IdFactory.getInstance().getNextId(), clanName);
            ClanMember leader = new ClanMember(clan, player);
            clan.setLeader(leader);
            leader.setPlayerInstance(player);
            clan.store();
            player.setClan(clan);
            player.setPledgeClass(ClanMember.calculatePledgeClass(player));
            player.setClanPrivileges(16777214);
            if (Config.ALT_CLAN_DEFAULT_LEVEL > 1) {
               clan.changeLevel(Config.ALT_CLAN_DEFAULT_LEVEL, false);
            }

            this._clans.put(clan.getId(), clan);
            player.sendPacket(new PledgeShowInfoUpdate(clan));
            player.sendPacket(new PledgeShowMemberListAll(clan, player));
            player.sendUserInfo();
            player.sendPacket(new PledgeShowMemberListUpdate(player));
            player.sendPacket(SystemMessageId.CLAN_CREATED);
            return clan;
         }
      }
   }

   public synchronized void destroyClan(int clanId) {
      Clan clan = this.getClan(clanId);
      if (clan != null) {
         clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_HAS_DISPERSED));
         int castleId = clan.getCastleId();
         if (castleId == 0) {
            for(Siege siege : SiegeManager.getInstance().getSieges()) {
               siege.removeSiegeClan(clan);
            }
         }

         int fortId = clan.getFortId();
         if (fortId == 0) {
            for(FortSiege siege : FortSiegeManager.getInstance().getSieges()) {
               siege.removeSiegeClan(clan);
            }
         }

         int hallId = clan.getHideoutId();
         if (hallId == 0) {
            for(SiegableHall hall : CHSiegeManager.getInstance().getConquerableHalls().values()) {
               hall.removeAttacker(clan);
            }
         }

         Auction auction = AuctionManager.getInstance().getAuction(clan.getAuctionBiddedAt());
         if (auction != null) {
            auction.cancelBid(clan.getId());
         }

         ClanMember leaderMember = clan.getLeader();
         if (leaderMember == null) {
            clan.getWarehouse().destroyAllItems("ClanRemove", null, null);
         } else {
            clan.getWarehouse().destroyAllItems("ClanRemove", clan.getLeader().getPlayerInstance(), null);
         }

         for(ClanMember member : clan.getMembers()) {
            clan.removeClanMember(member.getObjectId(), 0L);
         }

         this._clans.remove(clanId);
         IdFactory.getInstance().releaseId(clanId);

         try (Connection con = DatabaseFactory.getInstance().getConnection()) {
            PreparedStatement statement = con.prepareStatement("DELETE FROM clan_data WHERE clan_id=?");
            statement.setInt(1, clanId);
            statement.execute();
            statement.close();
            statement = con.prepareStatement("DELETE FROM clan_privs WHERE clan_id=?");
            statement.setInt(1, clanId);
            statement.execute();
            statement.close();
            statement = con.prepareStatement("DELETE FROM clan_skills WHERE clan_id=?");
            statement.setInt(1, clanId);
            statement.execute();
            statement.close();
            statement = con.prepareStatement("DELETE FROM clan_subpledges WHERE clan_id=?");
            statement.setInt(1, clanId);
            statement.execute();
            statement.close();
            statement = con.prepareStatement("DELETE FROM clan_wars WHERE clan1=? OR clan2=?");
            statement.setInt(1, clanId);
            statement.setInt(2, clanId);
            statement.execute();
            statement.close();
            statement = con.prepareStatement("DELETE FROM clan_notices WHERE clan_id=?");
            statement.setInt(1, clanId);
            statement.execute();
            statement.close();
            if (castleId != 0) {
               Castle castle = CastleManager.getInstance().getCastleById(castleId);
               if (castle != null) {
                  castle.getTerritory().changeOwner(null);
               }

               statement = con.prepareStatement("UPDATE castle SET taxPercent = 0 WHERE id = ?");
               statement.setInt(1, castleId);
               statement.execute();
               statement.close();
            }

            if (fortId != 0) {
               Fort fort = FortManager.getInstance().getFortById(fortId);
               if (fort != null) {
                  Clan owner = fort.getOwnerClan();
                  if (clan == owner) {
                     fort.removeOwner(true);
                  }
               }
            }

            if (hallId != 0) {
               SiegableHall hall = CHSiegeManager.getInstance().getSiegableHall(hallId);
               if (hall != null && hall.getOwnerId() == clanId) {
                  hall.free();
               }
            }
         } catch (Exception var23) {
            _log.log(Level.SEVERE, this.getClass().getSimpleName() + ": Error removing clan from DB.", (Throwable)var23);
         }
      }
   }

   public void scheduleRemoveClan(final int clanId) {
      ThreadPoolManager.getInstance().schedule(new Runnable() {
         @Override
         public void run() {
            if (ClanHolder.this.getClan(clanId) != null) {
               if (ClanHolder.this.getClan(clanId).getDissolvingExpiryTime() != 0L) {
                  ClanHolder.this.destroyClan(clanId);
               }
            }
         }
      }, Math.max(this.getClan(clanId).getDissolvingExpiryTime() - System.currentTimeMillis(), 300000L));
   }

   public boolean isAllyExists(String allyName) {
      for(Clan clan : this.getClans()) {
         if (clan.getAllyName() != null && clan.getAllyName().equalsIgnoreCase(allyName)) {
            return true;
         }
      }

      return false;
   }

   public void storeclanswars(int clanId1, int clanId2) {
      Clan clan1 = getInstance().getClan(clanId1);
      Clan clan2 = getInstance().getClan(clanId2);
      if (this.fireClanWarStartListeners(clan1, clan2)) {
         clan1.setEnemyClan(clan2);
         clan2.setAttackerClan(clan1);
         clan1.broadcastClanStatus();
         clan2.broadcastClanStatus();

         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement ps = con.prepareStatement("REPLACE INTO clan_wars (clan1, clan2, wantspeace1, wantspeace2) VALUES(?,?,?,?)");
         ) {
            ps.setInt(1, clanId1);
            ps.setInt(2, clanId2);
            ps.setInt(3, 0);
            ps.setInt(4, 0);
            ps.execute();
         } catch (Exception var37) {
            _log.log(Level.SEVERE, this.getClass().getSimpleName() + ": Error storing clan wars data.", (Throwable)var37);
         }

         SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.CLAN_WAR_DECLARED_AGAINST_S1_IF_KILLED_LOSE_LOW_EXP);
         msg.addString(clan2.getName());
         clan1.broadcastToOnlineMembers(msg);
         msg = SystemMessage.getSystemMessage(SystemMessageId.CLAN_S1_DECLARED_WAR);
         msg.addString(clan1.getName());
         clan2.broadcastToOnlineMembers(msg);
      }
   }

   public void deleteclanswars(int clanId1, int clanId2) {
      Clan clan1 = getInstance().getClan(clanId1);
      Clan clan2 = getInstance().getClan(clanId2);
      if (this.fireClanWarEndListeners(clan1, clan2)) {
         clan1.deleteEnemyClan(clan2);
         clan2.deleteAttackerClan(clan1);
         clan1.broadcastClanStatus();
         clan2.broadcastClanStatus();

         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement ps = con.prepareStatement("DELETE FROM clan_wars WHERE clan1=? AND clan2=?");
         ) {
            ps.setInt(1, clanId1);
            ps.setInt(2, clanId2);
            ps.execute();
         } catch (Exception var37) {
            _log.log(Level.SEVERE, this.getClass().getSimpleName() + ": Error removing clan wars data.", (Throwable)var37);
         }

         SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.WAR_AGAINST_S1_HAS_STOPPED);
         msg.addString(clan2.getName());
         clan1.broadcastToOnlineMembers(msg);
         msg = SystemMessage.getSystemMessage(SystemMessageId.CLAN_S1_HAS_DECIDED_TO_STOP);
         msg.addString(clan1.getName());
         clan2.broadcastToOnlineMembers(msg);
      }
   }

   public void checkSurrender(Clan clan1, Clan clan2) {
      int count = 0;

      for(ClanMember player : clan1.getMembers()) {
         if (player != null && player.getPlayerInstance().getWantsPeace() == 1) {
            ++count;
         }
      }

      if (count == clan1.getMembers().length - 1) {
         if (!this.fireClanWarEndListeners(clan1, clan2)) {
            return;
         }

         clan1.deleteEnemyClan(clan2);
         clan2.deleteEnemyClan(clan1);
         this.deleteclanswars(clan1.getId(), clan2.getId());
      }
   }

   private void restorewars() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         Statement statement = con.createStatement();
         ResultSet rset = statement.executeQuery("SELECT clan1, clan2 FROM clan_wars");
      ) {
         while(rset.next()) {
            Clan clan1 = this.getClan(rset.getInt("clan1"));
            Clan clan2 = this.getClan(rset.getInt("clan2"));
            if (clan1 != null && clan2 != null) {
               clan1.setEnemyClan(rset.getInt("clan2"));
               clan2.setAttackerClan(rset.getInt("clan1"));
            } else {
               _log.log(Level.WARNING, this.getClass().getSimpleName() + ": restorewars one of clans is null clan1:" + clan1 + " clan2:" + clan2);
            }
         }
      } catch (Exception var61) {
         _log.log(Level.SEVERE, this.getClass().getSimpleName() + ": Error restoring clan wars data.", (Throwable)var61);
      }
   }

   private void allianceCheck() {
      for(Clan clan : this._clans.values()) {
         int allyId = clan.getAllyId();
         if (allyId != 0 && clan.getId() != allyId && !this._clans.containsKey(allyId)) {
            clan.setAllyId(0);
            clan.setAllyName(null);
            clan.changeAllyCrest(0, true);
            clan.updateClanInDB();
            _log.info(this.getClass().getSimpleName() + ": Removed alliance from clan: " + clan);
         }
      }
   }

   public List<Clan> getClanAllies(int allianceId) {
      List<Clan> clanAllies = new ArrayList<>();
      if (allianceId != 0) {
         for(Clan clan : this._clans.values()) {
            if (clan != null && clan.getAllyId() == allianceId) {
               clanAllies.add(clan);
            }
         }
      }

      return clanAllies;
   }

   public void storeClanScore() {
      for(Clan clan : this._clans.values()) {
         clan.updateClanScoreInDB();
      }
   }

   private boolean fireClanWarStartListeners(Clan clan1, Clan clan2) {
      if (!clanWarListeners.isEmpty() && clan1 != null && clan2 != null) {
         ClanWarEvent event = new ClanWarEvent();
         event.setClan1(clan1);
         event.setClan2(clan2);
         event.setStage(ScriptListener.EventStage.START);

         for(ClanWarListener listener : clanWarListeners) {
            if (!listener.onWarStart(event)) {
               return false;
            }
         }
      }

      return true;
   }

   private boolean fireClanWarEndListeners(Clan clan1, Clan clan2) {
      if (!clanWarListeners.isEmpty() && clan1 != null && clan2 != null) {
         ClanWarEvent event = new ClanWarEvent();
         event.setClan1(clan1);
         event.setClan2(clan2);
         event.setStage(ScriptListener.EventStage.END);

         for(ClanWarListener listener : clanWarListeners) {
            if (!listener.onWarEnd(event)) {
               return false;
            }
         }
      }

      return true;
   }

   public static void addClanWarListener(ClanWarListener listener) {
      if (!clanWarListeners.contains(listener)) {
         clanWarListeners.add(listener);
      }
   }

   public static void removeClanWarListener(ClanWarListener listener) {
      clanWarListeners.remove(listener);
   }

   public static ClanHolder getInstance() {
      return ClanHolder.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final ClanHolder _instance = new ClanHolder();
   }
}
