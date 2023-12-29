package l2e.gameserver.listener.player;

import l2e.gameserver.listener.AbstractListener;
import l2e.gameserver.listener.events.TransformEvent;
import l2e.gameserver.model.actor.Player;

public abstract class TransformListener extends AbstractListener {
   public TransformListener(Player activeChar) {
      super(activeChar);
      this.register();
   }

   public abstract boolean onTransform(TransformEvent var1);

   public abstract boolean onUntransform(TransformEvent var1);

   @Override
   public void register() {
      if (this.getPlayer() != null) {
         this.getPlayer().addTransformListener(this);
      }
   }

   @Override
   public void unregister() {
      if (this.getPlayer() != null) {
         this.getPlayer().removeTransformListener(this);
      }
   }
}
