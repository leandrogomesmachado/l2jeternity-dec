package l2e.gameserver.network.clientpackets;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import l2e.gameserver.Config;
import l2e.gameserver.listener.events.PlayerEvent;
import l2e.gameserver.listener.player.PlayerListener;
import l2e.gameserver.network.serverpackets.CharacterDeleteFail;
import l2e.gameserver.network.serverpackets.CharacterDeleteSuccess;
import l2e.gameserver.network.serverpackets.CharacterSelectionInfo;

public final class RequestCharacterDelete extends GameClientPacket {
   private static final List<PlayerListener> _listeners = new LinkedList<>();
   private int _charSlot;

   @Override
   protected void readImpl() {
      this._charSlot = this.readD();
   }

   @Override
   protected void runImpl() {
      if (Config.DEBUG) {
         _log.fine("deleting slot:" + this._charSlot);
      }

      try {
         byte answer = this.getClient().markToDeleteChar(this._charSlot);
         switch(answer) {
            case -1:
            default:
               break;
            case 0:
               this.sendPacket(new CharacterDeleteSuccess());
               PlayerEvent event = new PlayerEvent();
               event.setClient(this.getClient());
               event.setObjectId(this.getClient().getCharSelection(this._charSlot).getObjectId());
               event.setName(this.getClient().getCharSelection(this._charSlot).getName());
               this.firePlayerListener(event);
               break;
            case 1:
               this.sendPacket(new CharacterDeleteFail(2));
               break;
            case 2:
               this.sendPacket(new CharacterDeleteFail(3));
         }
      } catch (Exception var3) {
         _log.log(Level.SEVERE, "Error:", (Throwable)var3);
      }

      CharacterSelectionInfo cl = new CharacterSelectionInfo(this.getClient().getLogin(), this.getClient().getSessionId().playOkID1, 0);
      this.sendPacket(cl);
      this.getClient().setCharSelection(cl.getCharInfo());
   }

   private void firePlayerListener(PlayerEvent event) {
      for(PlayerListener listener : _listeners) {
         listener.onCharDelete(event);
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
