package l2e.gameserver.network.clientpackets;

import java.util.LinkedList;
import java.util.List;
import l2e.gameserver.listener.events.PlayerEvent;
import l2e.gameserver.listener.player.PlayerListener;
import l2e.gameserver.network.serverpackets.CharacterSelectionInfo;

public final class RequestCharacterRestore extends GameClientPacket {
   private static final List<PlayerListener> _listeners = new LinkedList<>();
   private int _charSlot;

   @Override
   protected void readImpl() {
      this._charSlot = this.readD();
   }

   @Override
   protected void runImpl() {
      this.getClient().markRestoredChar(this._charSlot);
      CharacterSelectionInfo cl = new CharacterSelectionInfo(this.getClient().getLogin(), this.getClient().getSessionId().playOkID1, 0);
      this.sendPacket(cl);
      this.getClient().setCharSelection(cl.getCharInfo());
      PlayerEvent event = new PlayerEvent();
      event.setClient(this.getClient());
      event.setObjectId(this.getClient().getCharSelection(this._charSlot).getObjectId());
      event.setName(this.getClient().getCharSelection(this._charSlot).getName());
      this.firePlayerListener(event);
   }

   private void firePlayerListener(PlayerEvent event) {
      for(PlayerListener listener : _listeners) {
         listener.onCharRestore(event);
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
