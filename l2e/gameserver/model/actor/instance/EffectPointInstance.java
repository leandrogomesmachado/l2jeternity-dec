package l2e.gameserver.model.actor.instance;

import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;

public class EffectPointInstance extends Npc {
   private final Player _owner;

   public EffectPointInstance(int objectId, NpcTemplate template, Creature owner) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.EffectPointInstance);
      this.setIsInvul(false);
      this._owner = owner == null ? null : owner.getActingPlayer();
      if (owner != null) {
         this.setReflectionId(owner.getReflectionId());
      }
   }

   @Override
   public Player getActingPlayer() {
      return this._owner;
   }

   @Override
   public void onAction(Player player, boolean interact) {
      player.sendActionFailed();
   }

   @Override
   public void onActionShift(Player player) {
      if (player != null) {
         player.sendActionFailed();
      }
   }
}
