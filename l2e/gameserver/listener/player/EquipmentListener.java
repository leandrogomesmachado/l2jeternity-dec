package l2e.gameserver.listener.player;

import l2e.gameserver.listener.AbstractListener;
import l2e.gameserver.listener.events.EquipmentEvent;
import l2e.gameserver.model.actor.Player;

public abstract class EquipmentListener extends AbstractListener {
   public EquipmentListener(Player activeChar) {
      super(activeChar);
      this.register();
   }

   public abstract boolean onEquip(EquipmentEvent var1);

   @Override
   public void register() {
      if (this.getPlayer() == null) {
         Player.addGlobalEquipmentListener(this);
      } else {
         this.getPlayer().addEquipmentListener(this);
      }
   }

   @Override
   public void unregister() {
      if (this.getPlayer() == null) {
         Player.removeGlobalEquipmentListener(this);
      } else {
         this.getPlayer().removeEquipmentListener(this);
      }
   }
}
