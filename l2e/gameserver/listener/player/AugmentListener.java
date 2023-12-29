package l2e.gameserver.listener.player;

import l2e.gameserver.listener.AbstractListener;
import l2e.gameserver.listener.events.AugmentEvent;
import l2e.gameserver.model.items.instance.ItemInstance;

public abstract class AugmentListener extends AbstractListener {
   public AugmentListener() {
      this.register();
   }

   public abstract boolean onAugment(AugmentEvent var1);

   public abstract boolean onRemoveAugment(AugmentEvent var1);

   @Override
   public void register() {
      ItemInstance.addAugmentListener(this);
   }

   @Override
   public void unregister() {
      ItemInstance.removeAugmentListener(this);
   }
}
