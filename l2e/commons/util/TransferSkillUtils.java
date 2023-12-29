package l2e.commons.util;

import l2e.gameserver.Config;
import l2e.gameserver.data.parser.ClassListParser;
import l2e.gameserver.data.parser.SkillTreesParser;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.SkillLearn;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.holders.ItemHolder;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.Skill;

public class TransferSkillUtils {
   private static final ItemHolder[] _items = new ItemHolder[]{new ItemHolder(15307, 1L), new ItemHolder(15308, 1L), new ItemHolder(15309, 4L)};

   public static void checkTransferItems(Player player) {
      int index = getTransferClassIndex(player);
      if (index >= 0) {
         boolean oldSupport = false;
         QuestState st = player.getQuestState("SkillTransfer");
         if (st != null && !st.getGlobalQuestVar("SkillTransfer").isEmpty()) {
            oldSupport = true;
         }

         String varName = "SkillTransfer" + String.valueOf(player.getClassId().getId());
         if (!player.getVarB(varName, false)) {
            player.setVar(varName, true);
            if (!oldSupport) {
               player.addItem("PORMANDERS", _items[index].getId(), _items[index].getCount(), null, true);
            }
         }

         if (Config.SKILL_CHECK_ENABLE && (!player.canOverrideCond(PcCondOverride.SKILL_CONDITIONS) || Config.SKILL_CHECK_GM)) {
            long count = _items[index].getCount() - player.getInventory().getInventoryItemCount(_items[index].getId(), -1, false);

            for(Skill sk : player.getAllSkills()) {
               for(SkillLearn s : SkillTreesParser.getInstance().getTransferSkillTree(player.getClassId()).values()) {
                  if (s.getId() == sk.getId() && (sk.getId() != 1043 || index != 2 || !player.isInStance())) {
                     if (--count < 0L) {
                        String className = ClassListParser.getInstance().getClass(player.getClassId()).getClassName();
                        Util.handleIllegalPlayerAction(
                           player,
                           ""
                              + player.getName()
                              + " has too many transfered skills or items, skill:"
                              + s.getName()
                              + " ("
                              + sk.getId()
                              + "/"
                              + sk.getLevel()
                              + "), class:"
                              + className
                        );
                        if (Config.SKILL_CHECK_REMOVE) {
                           player.removeSkill(sk);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static int getTransferClassIndex(Player player) {
      switch(player.getClassId().getId()) {
         case 97:
            return 0;
         case 105:
            return 1;
         case 112:
            return 2;
         default:
            return -1;
      }
   }
}
