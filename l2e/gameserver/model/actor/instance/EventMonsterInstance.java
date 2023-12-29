package l2e.gameserver.model.actor.instance;

import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;

public class EventMonsterInstance extends MonsterInstance {
   public boolean block_skill_attack = false;
   public boolean drop_on_ground = false;

   public EventMonsterInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.EventMobInstance);
   }

   public void eventSetBlockOffensiveSkills(boolean value) {
      this.block_skill_attack = value;
   }

   public void eventSetDropOnGround(boolean value) {
      this.drop_on_ground = value;
   }

   public boolean eventDropOnGround() {
      return this.drop_on_ground;
   }

   public boolean eventSkillAttackBlocked() {
      return this.block_skill_attack;
   }

   @Override
   public boolean isMonster() {
      return false;
   }
}
