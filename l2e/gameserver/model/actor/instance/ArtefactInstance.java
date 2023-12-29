package l2e.gameserver.model.actor.instance;

import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.skills.Skill;

public final class ArtefactInstance extends Npc {
   public ArtefactInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.ArtefactInstance);
   }

   @Override
   public void onSpawn() {
      super.onSpawn();
      this.getCastle().registerArtefact(this);
   }

   @Override
   public boolean isAutoAttackable(Creature attacker) {
      return false;
   }

   @Override
   public boolean canBeAttacked() {
      return false;
   }

   @Override
   public void onForcedAttack(Player player) {
      player.sendActionFailed();
   }

   @Override
   public void reduceCurrentHp(double damage, Creature attacker, Skill skill) {
   }

   @Override
   public void reduceCurrentHp(double damage, Creature attacker, boolean awake, boolean isDOT, Skill skill) {
   }
}
