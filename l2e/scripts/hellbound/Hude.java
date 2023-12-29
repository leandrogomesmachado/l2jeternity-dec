package l2e.scripts.hellbound;

import l2e.gameserver.data.parser.MultiSellParser;
import l2e.gameserver.instancemanager.HellboundManager;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class Hude extends Quest {
   private static final int HUDE = 32298;
   private static final int BASIC_CERT = 9850;
   private static final int STANDART_CERT = 9851;
   private static final int PREMIUM_CERT = 9852;
   private static final int MARK_OF_BETRAYAL = 9676;
   private static final int LIFE_FORCE = 9681;
   private static final int CONTAINED_LIFE_FORCE = 9682;
   private static final int MAP = 9994;
   private static final int STINGER = 10012;

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      QuestState qs = player.getQuestState(this.getName());
      if (qs == null) {
         qs = this.newQuestState(player);
      }

      if ("scertif".equalsIgnoreCase(event)) {
         if (HellboundManager.getInstance().getLevel() > 3
            && qs.hasQuestItems(9850)
            && qs.getQuestItemsCount(9676) >= 30L
            && qs.getQuestItemsCount(10012) >= 60L) {
            qs.takeItems(9676, 30L);
            qs.takeItems(10012, 60L);
            qs.takeItems(9850, 1L);
            qs.giveItems(9851, 1L);
            return "32298-04a.htm";
         } else {
            return "32298-04b.htm";
         }
      } else if ("pcertif".equalsIgnoreCase(event)) {
         if (HellboundManager.getInstance().getLevel() > 6
            && qs.hasQuestItems(9851)
            && qs.getQuestItemsCount(9681) >= 56L
            && qs.getQuestItemsCount(9682) >= 14L) {
            qs.takeItems(9681, 56L);
            qs.takeItems(9682, 14L);
            qs.takeItems(9851, 1L);
            qs.giveItems(9852, 1L);
            qs.giveItems(9994, 1L);
            return "32298-06a.htm";
         } else {
            return "32298-06b.htm";
         }
      } else {
         if ("multisell1".equalsIgnoreCase(event)) {
            if (qs.hasQuestItems(9851) || qs.hasQuestItems(9852)) {
               MultiSellParser.getInstance().separateAndSend(322980001, player, npc, false);
            }
         } else if ("multisell2".equalsIgnoreCase(event) && qs.hasQuestItems(9852)) {
            MultiSellParser.getInstance().separateAndSend(322980002, player, npc, false);
         }

         return null;
      }
   }

   @Override
   public final String onFirstTalk(Npc npc, Player player) {
      String htmltext = "";
      QuestState qs = player.getQuestState(this.getName());
      if (qs == null) {
         qs = this.newQuestState(player);
      }

      if (!qs.hasQuestItems(9850) && !qs.hasQuestItems(9851) && !qs.hasQuestItems(9852)) {
         htmltext = "32298-01.htm";
      } else if (qs.hasQuestItems(9850) && !qs.hasQuestItems(9851) && !qs.hasQuestItems(9852)) {
         htmltext = "32298-03.htm";
      } else if (qs.hasQuestItems(9851) && !qs.hasQuestItems(9852)) {
         htmltext = "32298-05.htm";
      } else if (qs.hasQuestItems(9852)) {
         htmltext = "32298-07.htm";
      }

      return htmltext;
   }

   public Hude(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addFirstTalkId(32298);
      this.addStartNpc(32298);
      this.addTalkId(32298);
   }

   public static void main(String[] args) {
      new Hude(-1, "Hude", "hellbound");
   }
}
