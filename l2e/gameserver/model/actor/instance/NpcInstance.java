package l2e.gameserver.model.actor.instance;

import java.util.List;
import java.util.Map;
import l2e.commons.util.StringUtil;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.SkillTreesParser;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.SkillLearn;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.status.FolkStatus;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.base.AcquireSkillType;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.AcquireSkillDone;
import l2e.gameserver.network.serverpackets.AcquireSkillList;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class NpcInstance extends Npc {
   public NpcInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.NpcInstance);
      this.setIsInvul(false);
   }

   public FolkStatus getStatus() {
      return (FolkStatus)super.getStatus();
   }

   @Override
   public void initCharStatus() {
      this.setStatus(new FolkStatus(this));
   }

   public List<ClassId> getClassesToTeach() {
      return this.getTemplate().getTeachInfo();
   }

   public static void showSkillList(Player player, Npc npc, ClassId classId) {
      if (Config.DEBUG) {
         _log.fine("SkillList activated on: " + npc.getObjectId());
      }

      if (player.getWeightPenalty() < 3 && player.isInventoryUnder90(true)) {
         int npcId = npc.getTemplate().getId();
         if (npcId == 32611) {
            List<SkillLearn> skills = SkillTreesParser.getInstance().getAvailableCollectSkills(player);
            AcquireSkillList asl = new AcquireSkillList(AcquireSkillType.COLLECT);
            int counts = 0;

            for(SkillLearn s : skills) {
               Skill sk = SkillsParser.getInstance().getInfo(s.getId(), s.getLvl());
               if (sk != null) {
                  ++counts;
                  asl.addSkill(s.getId(), s.getGetLevel(), s.getLvl(), s.getLvl(), 0, 1);
               }
            }

            if (counts == 0) {
               int minLevel = SkillTreesParser.getInstance().getMinLevelForNewSkill(player, SkillTreesParser.getInstance().getCollectSkillTree());
               if (minLevel > 0) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN_S1);
                  sm.addNumber(minLevel);
                  player.sendPacket(sm);
               } else {
                  player.sendPacket(SystemMessageId.NO_MORE_SKILLS_TO_LEARN);
               }

               player.sendPacket(AcquireSkillDone.STATIC);
            } else {
               player.sendPacket(asl);
            }
         } else if (!npc.getTemplate().canTeach(classId)) {
            npc.showNoTeachHtml(player);
         } else if (((NpcInstance)npc).getClassesToTeach().isEmpty()) {
            NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
            String sb = StringUtil.concat(
               "<html><body>I cannot teach you. My class list is empty.<br> Ask admin to fix it. Need add my npcid and classes to skill_learn.sql.<br>NpcId:",
               String.valueOf(npcId),
               ", Your classId:",
               String.valueOf(player.getClassId().getId()),
               "<br></body></html>"
            );
            html.setHtml(player, sb);
            player.sendPacket(html);
         } else {
            List<SkillLearn> skills = SkillTreesParser.getInstance().getAvailableSkills(player, classId, false, false);
            AcquireSkillList asl = new AcquireSkillList(AcquireSkillType.CLASS);
            int count = 0;
            player.setLearningClass(classId);

            for(SkillLearn s : skills) {
               if (SkillsParser.getInstance().getInfo(s.getId(), s.getLvl()) != null) {
                  asl.addSkill(s.getId(), s.getGetLevel(), s.getLvl(), s.getLvl(), s.getCalculatedLevelUpSp(player.getClassId(), classId), 0);
                  ++count;
               }
            }

            if (count == 0) {
               Map<Integer, SkillLearn> skillTree = SkillTreesParser.getInstance().getCompleteClassSkillTree(classId);
               int minLevel = SkillTreesParser.getInstance().getMinLevelForNewSkill(player, skillTree);
               if (minLevel > 0) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN_S1);
                  sm.addNumber(minLevel);
                  player.sendPacket(sm);
               } else if (player.getClassId().level() == 1) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.NO_SKILLS_TO_LEARN_RETURN_AFTER_S1_CLASS_CHANGE);
                  sm.addNumber(2);
                  player.sendPacket(sm);
               } else {
                  player.sendPacket(SystemMessageId.NO_MORE_SKILLS_TO_LEARN);
               }
            } else {
               player.sendPacket(asl);
            }
         }
      } else {
         player.sendPacket(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT);
      }
   }

   @Override
   public boolean isInvul() {
      return true;
   }
}
