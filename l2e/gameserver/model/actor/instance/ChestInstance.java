package l2e.gameserver.model.actor.instance;

import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.skills.Skill;

public class ChestInstance extends MonsterInstance {
   public ChestInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.ChestInstance);
   }

   public void tryOpen(Player opener, Skill skill) {
      this.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, opener, Integer.valueOf(100));
   }

   @Override
   public boolean hasRandomAnimation() {
      return false;
   }

   @Override
   public boolean isMovementDisabled() {
      return true;
   }
}
