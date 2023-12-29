package l2e.gameserver.handler.skillhandlers.impl;

import java.util.logging.Logger;
import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.ManorParser;
import l2e.gameserver.handler.skillhandlers.ISkillHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.SkillType;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class Sow implements ISkillHandler {
   private static Logger _log = Logger.getLogger(Sow.class.getName());
   private static final SkillType[] SKILL_IDS = new SkillType[]{SkillType.SOW};

   @Override
   public void useSkill(Creature activeChar, Skill skill, GameObject[] targets) {
      if (activeChar.isPlayer()) {
         GameObject[] targetList = skill.getTargetList(activeChar);
         if (targetList != null && targetList.length != 0) {
            if (Config.DEBUG) {
               _log.info("Casting sow");
            }

            for(GameObject tgt : targetList) {
               if (tgt.isMonster()) {
                  MonsterInstance target = (MonsterInstance)tgt;
                  if (!target.isDead() && !target.isSeeded() && target.getSeederId() == activeChar.getObjectId()) {
                     int seedId = target.getSeedType();
                     if (seedId == 0) {
                        activeChar.sendActionFailed();
                     } else {
                        if (!activeChar.destroyItemByItemId("Consume", seedId, 1L, target, false)) {
                           activeChar.sendActionFailed();
                           return;
                        }

                        SystemMessage sm;
                        if (this.calcSuccess(activeChar, target, seedId)) {
                           activeChar.sendPacket(Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET.getPacket());
                           target.setSeeded(activeChar.getActingPlayer());
                           sm = SystemMessage.getSystemMessage(SystemMessageId.THE_SEED_WAS_SUCCESSFULLY_SOWN);
                        } else {
                           sm = SystemMessage.getSystemMessage(SystemMessageId.THE_SEED_WAS_NOT_SOWN);
                        }

                        if (activeChar.getParty() == null) {
                           activeChar.sendPacket(sm);
                        } else {
                           activeChar.getParty().broadCast(sm);
                        }

                        target.getAI().setIntention(CtrlIntention.IDLE);
                     }
                  } else {
                     activeChar.sendActionFailed();
                  }
               }
            }
         }
      }
   }

   private boolean calcSuccess(Creature activeChar, Creature target, int seedId) {
      int basicSuccess = ManorParser.getInstance().isAlternative(seedId) ? 20 : 90;
      int minlevelSeed = ManorParser.getInstance().getSeedMinLevel(seedId);
      int maxlevelSeed = ManorParser.getInstance().getSeedMaxLevel(seedId);
      int levelPlayer = activeChar.getLevel();
      int levelTarget = target.getLevel();
      if (levelTarget < minlevelSeed) {
         basicSuccess -= 5 * (minlevelSeed - levelTarget);
      }

      if (levelTarget > maxlevelSeed) {
         basicSuccess -= 5 * (levelTarget - maxlevelSeed);
      }

      int diff = levelPlayer - levelTarget;
      if (diff < 0) {
         diff = -diff;
      }

      if (diff > 5) {
         basicSuccess -= 5 * (diff - 5);
      }

      if (basicSuccess < 1) {
         basicSuccess = 1;
      }

      return Rnd.nextInt(99) < basicSuccess;
   }

   @Override
   public SkillType[] getSkillIds() {
      return SKILL_IDS;
   }
}
