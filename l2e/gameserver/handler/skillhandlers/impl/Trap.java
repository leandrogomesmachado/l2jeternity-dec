package l2e.gameserver.handler.skillhandlers.impl;

import l2e.gameserver.handler.skillhandlers.ISkillHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.instance.TrapInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.SkillType;
import l2e.gameserver.network.SystemMessageId;

public class Trap implements ISkillHandler {
   private static final SkillType[] SKILL_IDS = new SkillType[]{SkillType.DETECT_TRAP, SkillType.REMOVE_TRAP};

   @Override
   public void useSkill(Creature activeChar, Skill skill, GameObject[] targets) {
      if (activeChar != null && skill != null) {
         switch(skill.getSkillType()) {
            case DETECT_TRAP:
               for(Creature target : World.getInstance().getAroundCharacters(activeChar, skill.getAffectRange(), 200)) {
                  if (target.isTrap() && !target.isAlikeDead()) {
                     TrapInstance trap = (TrapInstance)target;
                     if ((double)trap.getLevel() <= skill.getPower()) {
                        trap.setDetected(activeChar);
                     }
                  }
               }
               break;
            case REMOVE_TRAP:
               for(Creature target : (Creature[])targets) {
                  if (target.isTrap() && !target.isAlikeDead()) {
                     TrapInstance trap = (TrapInstance)target;
                     if (!trap.canBeSeen(activeChar)) {
                        if (activeChar.isPlayer()) {
                           activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
                        }
                     } else if (!((double)trap.getLevel() > skill.getPower())) {
                        if (trap.getTemplate().getEventQuests(Quest.QuestEventType.ON_TRAP_ACTION) != null) {
                           for(Quest quest : trap.getTemplate().getEventQuests(Quest.QuestEventType.ON_TRAP_ACTION)) {
                              quest.notifyTrapAction(trap, activeChar, Quest.TrapAction.TRAP_DISARMED);
                           }
                        }

                        trap.unSummon();
                        if (activeChar.isPlayer()) {
                           activeChar.sendPacket(SystemMessageId.A_TRAP_DEVICE_HAS_BEEN_STOPPED);
                        }
                     }
                  }
               }
         }
      }
   }

   @Override
   public SkillType[] getSkillIds() {
      return SKILL_IDS;
   }
}
