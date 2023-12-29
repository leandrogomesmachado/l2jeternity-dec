package l2e.gameserver.model.actor.instance;

import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.skills.Skill;

public final class QuestGuardInstance extends GuardInstance {
   private boolean _isPassive = false;

   public QuestGuardInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.QuestGuardInstance);
   }

   @Override
   public void addDamage(Creature attacker, int damage, Skill skill) {
      super.addDamage(attacker, damage, skill);
      if (attacker instanceof Attackable && this.getTemplate().getEventQuests(Quest.QuestEventType.ON_ATTACK) != null) {
         for(Quest quest : this.getTemplate().getEventQuests(Quest.QuestEventType.ON_ATTACK)) {
            quest.notifyAttack(this, null, damage, false, skill);
         }
      }
   }

   @Override
   public void addDamageHate(Creature attacker, int damage, int aggro) {
      if (!this.isPassive() && !attacker.isPlayer()) {
         super.addDamageHate(attacker, damage, aggro);
      }
   }

   @Override
   public boolean isAutoAttackable(Creature attacker) {
      return !attacker.isPlayer();
   }

   @Override
   public void returnHome() {
   }

   public void setPassive(boolean state) {
      this._isPassive = state;
   }

   public boolean isPassive() {
      return this._isPassive;
   }
}
