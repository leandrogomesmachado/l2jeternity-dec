package l2e.gameserver.listener.player;

import l2e.gameserver.listener.AbstractListener;
import l2e.gameserver.listener.events.ProfessionChangeEvent;
import l2e.gameserver.model.actor.Player;

public abstract class ProfessionChangeListener extends AbstractListener {
   public ProfessionChangeListener(Player activeChar) {
      super(activeChar);
      this.register();
   }

   public abstract void professionChanged(ProfessionChangeEvent var1);

   @Override
   public void register() {
      if (this.getPlayer() == null) {
         Player.addGlobalProfessionChangeListener(this);
      } else {
         this.getPlayer().addProfessionChangeListener(this);
      }
   }

   @Override
   public void unregister() {
      if (this.getPlayer() == null) {
         Player.removeGlobalProfessionChangeListener(this);
      } else {
         this.getPlayer().removeProfessionChangeListener(this);
      }
   }
}
