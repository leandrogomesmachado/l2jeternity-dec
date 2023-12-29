package l2e.gameserver.network.clientpackets;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.data.holder.CharNameHolder;
import l2e.gameserver.instancemanager.DoubleSessionManager;
import l2e.gameserver.instancemanager.PunishmentManager;
import l2e.gameserver.listener.events.PlayerEvent;
import l2e.gameserver.listener.player.PlayerListener;
import l2e.gameserver.model.CharSelectInfoPackage;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.punishment.PunishmentAffect;
import l2e.gameserver.model.punishment.PunishmentType;
import l2e.gameserver.network.GameClient;
import l2e.gameserver.network.serverpackets.CharacterSelected;
import l2e.gameserver.network.serverpackets.SSQInfo;
import l2e.gameserver.network.serverpackets.ServerClose;

public class RequestGameStart extends GameClientPacket {
   protected static final Logger _logAccounting = Logger.getLogger("accounting");
   private static final List<PlayerListener> _listeners = new LinkedList<>();
   private int _charSlot;
   protected int _unk1;
   protected int _unk2;
   protected int _unk3;
   protected int _unk4;

   @Override
   protected void readImpl() {
      this._charSlot = this.readD();
      this._unk1 = this.readH();
      this._unk2 = this.readD();
      this._unk3 = this.readD();
      this._unk4 = this.readD();
   }

   @Override
   protected void runImpl() {
      GameClient client = this.getClient();
      if (Config.SECOND_AUTH_ENABLED && !client.getSecondaryAuth().isAuthed()) {
         client.getSecondaryAuth().openDialog();
      } else {
         if (client.getActiveCharLock().tryLock()) {
            label150: {
               try {
                  if (client.getActiveChar() != null) {
                     break label150;
                  }

                  CharSelectInfoPackage info = client.getCharSelection(this._charSlot);
                  if (info == null) {
                     return;
                  }

                  if (info.getAccessLevel() < 0) {
                     client.close(ServerClose.STATIC_PACKET);
                     return;
                  }

                  client.updateHWID();
                  if (PunishmentManager.getInstance().checkPunishment(client, PunishmentType.BAN)) {
                     client.close(ServerClose.STATIC_PACKET);
                     return;
                  }

                  if (Config.DEBUG) {
                     _log.fine("selected slot:" + this._charSlot);
                  }

                  PlayerEvent event = new PlayerEvent();
                  event.setClient(client);
                  event.setObjectId(client.getCharSelection(this._charSlot).getObjectId());
                  event.setName(client.getCharSelection(this._charSlot).getName());
                  this.firePlayerListener(event);
                  Player cha = client.loadCharFromDisk(this._charSlot);
                  if (cha == null) {
                     return;
                  }

                  if (Config.DOUBLE_SESSIONS_CHECK_MAX_PLAYERS <= 0
                     || DoubleSessionManager.getInstance().tryAddPlayer(0, cha, Config.DOUBLE_SESSIONS_CHECK_MAX_PLAYERS)) {
                     World.getInstance().addToAllPlayers(cha);
                     CharNameHolder.getInstance().addName(cha);
                     cha.setClient(client);
                     client.setActiveChar(cha);
                     cha.setOnlineStatus(true, true);
                     this.sendPacket(new SSQInfo());
                     if (PunishmentManager.getInstance().checkPunishment(client, PunishmentType.BAN, PunishmentAffect.CHARACTER)) {
                        client.close(ServerClose.STATIC_PACKET);
                        return;
                     }

                     client.setState(GameClient.GameClientState.ENTERING);
                     CharacterSelected cs = new CharacterSelected(cha, client.getSessionId().playOkID1);
                     this.sendPacket(cs);
                     break label150;
                  }

                  client.close(ServerClose.STATIC_PACKET);
               } finally {
                  client.getActiveCharLock().unlock();
               }

               return;
            }

            LogRecord record = new LogRecord(Level.INFO, "Logged in");
            record.setParameters(new Object[]{client});
            _logAccounting.log(record);
         }
      }
   }

   private void firePlayerListener(PlayerEvent event) {
      for(PlayerListener listener : _listeners) {
         listener.onCharSelect(event);
      }
   }

   public static void addPlayerListener(PlayerListener listener) {
      if (!_listeners.contains(listener)) {
         _listeners.add(listener);
      }
   }

   public static void removePlayerListener(PlayerListener listener) {
      _listeners.remove(listener);
   }
}
