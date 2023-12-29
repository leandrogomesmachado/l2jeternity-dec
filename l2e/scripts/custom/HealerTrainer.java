package l2e.scripts.custom;

import java.util.List;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.SkillTreesParser;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.SkillLearn;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.base.AcquireSkillType;
import l2e.gameserver.model.holders.ItemHolder;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.AcquireSkillList;
import l2e.scripts.ai.AbstractNpcAI;

public class HealerTrainer extends AbstractNpcAI {
   private static final int[] HEALER_TRAINERS = new int[]{
      30022,
      30030,
      30032,
      30036,
      30067,
      30068,
      30116,
      30117,
      30118,
      30119,
      30144,
      30145,
      30188,
      30194,
      30293,
      30330,
      30375,
      30377,
      30464,
      30473,
      30476,
      30680,
      30701,
      30720,
      30721,
      30858,
      30859,
      30860,
      30861,
      30864,
      30906,
      30908,
      30912,
      31280,
      31281,
      31287,
      31329,
      31330,
      31335,
      31969,
      31970,
      31976,
      32155,
      32162
   };
   private static final int MIN_LEVEL = 76;
   private static final int MIN_CLASS_LEVEL = 3;

   public HealerTrainer() {
      super(HealerTrainer.class.getSimpleName(), "custom");
      this.addStartNpc(HEALER_TRAINERS);
      this.addTalkId(HEALER_TRAINERS);
      this.addFirstTalkId(HEALER_TRAINERS);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = null;
      switch(event) {
         case "30864.htm":
         case "30864-1.htm":
            htmltext = event;
            break;
         case "SkillTransfer":
            htmltext = "main.htm";
            break;
         case "SkillTransferLearn":
            if (!npc.getTemplate().canTeach(player.getClassId())) {
               htmltext = npc.getId() + "-noteach.htm";
            } else if (player.getLevel() >= 76 && player.getClassId().level() >= 3) {
               displayTransferSkillList(player);
            } else {
               htmltext = "learn-lowlevel.htm";
            }
            break;
         case "SkillTransferCleanse":
            if (!npc.getTemplate().canTeach(player.getClassId())) {
               htmltext = "cleanse-no.htm";
            } else if (player.getLevel() < 76 || player.getClassId().level() < 3) {
               htmltext = "cleanse-no.htm";
            } else if (player.getAdena() < (long)Config.FEE_DELETE_TRANSFER_SKILLS) {
               player.sendPacket(SystemMessageId.CANNOT_RESET_SKILL_LINK_BECAUSE_NOT_ENOUGH_ADENA);
            } else {
               boolean hasSkills = false;
               if (!hasTransferSkillItems(player)) {
                  for(SkillLearn s : SkillTreesParser.getInstance().getTransferSkillTree(player.getClassId()).values()) {
                     Skill sk = player.getKnownSkill(s.getId());
                     if (sk != null) {
                        player.removeSkill(sk);

                        for(ItemHolder item : s.getRequiredItems()) {
                           player.addItem("Cleanse", item.getId(), item.getCount(), npc, true);
                        }

                        hasSkills = true;
                     }
                  }

                  if (hasSkills) {
                     player.reduceAdena("Cleanse", (long)Config.FEE_DELETE_TRANSFER_SKILLS, npc, true);
                  }
               } else {
                  htmltext = "cleanse-no_skills.htm";
               }
            }
      }

      return htmltext;
   }

   private static void displayTransferSkillList(Player player) {
      List<SkillLearn> skills = SkillTreesParser.getInstance().getAvailableTransferSkills(player);
      AcquireSkillList asl = new AcquireSkillList(AcquireSkillType.TRANSFER);
      int count = 0;

      for(SkillLearn s : skills) {
         if (SkillsParser.getInstance().getInfo(s.getId(), s.getLvl()) != null) {
            ++count;
            asl.addSkill(s.getId(), s.getGetLevel(), s.getLvl(), s.getLvl(), s.getLevelUpSp(), 0);
         }
      }

      if (count > 0) {
         player.sendPacket(asl);
      } else {
         player.sendPacket(SystemMessageId.NO_MORE_SKILLS_TO_LEARN);
      }
   }

   private static boolean hasTransferSkillItems(Player player) {
      int itemId;
      switch(player.getClassId()) {
         case cardinal:
            itemId = 15307;
            break;
         case evaSaint:
            itemId = 15308;
            break;
         case shillienSaint:
            itemId = 15309;
            break;
         default:
            itemId = -1;
      }

      return player.getInventory().getInventoryItemCount(itemId, -1) > 0L;
   }

   public static void main(String[] args) {
      new HealerTrainer();
   }
}
