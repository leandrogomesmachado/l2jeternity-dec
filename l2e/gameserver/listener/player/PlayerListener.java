package l2e.gameserver.listener.player;

import l2e.gameserver.listener.AbstractListener;
import l2e.gameserver.listener.events.PlayerEvent;
import l2e.gameserver.network.clientpackets.RequestCharacterCreate;
import l2e.gameserver.network.clientpackets.RequestCharacterDelete;
import l2e.gameserver.network.clientpackets.RequestCharacterRestore;
import l2e.gameserver.network.clientpackets.RequestGameStart;

public abstract class PlayerListener extends AbstractListener {
   public PlayerListener() {
      this.register();
   }

   public abstract void onCharCreate(PlayerEvent var1);

   public abstract void onCharDelete(PlayerEvent var1);

   public abstract void onCharRestore(PlayerEvent var1);

   public abstract void onCharSelect(PlayerEvent var1);

   @Override
   public void register() {
      RequestCharacterCreate.addPlayerListener(this);
      RequestCharacterDelete.addPlayerListener(this);
      RequestCharacterRestore.addPlayerListener(this);
      RequestGameStart.addPlayerListener(this);
   }

   @Override
   public void unregister() {
      RequestCharacterCreate.removePlayerListener(this);
      RequestCharacterDelete.removePlayerListener(this);
      RequestCharacterRestore.removePlayerListener(this);
      RequestGameStart.removePlayerListener(this);
   }
}
