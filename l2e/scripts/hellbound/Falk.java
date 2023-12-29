package l2e.scripts.hellbound;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class Falk extends Quest {
   private static final int FALK = 32297;
   private static final int BASIC_CERT = 9850;
   private static final int STANDART_CERT = 9851;
   private static final int PREMIUM_CERT = 9852;
   private static final int DARION_BADGE = 9674;

   @Override
   public final String onFirstTalk(Npc npc, Player player) {
      QuestState qs = player.getQuestState(this.getName());
      if (qs == null) {
         qs = this.newQuestState(player);
      }

      return !qs.hasQuestItems(9850) && !qs.hasQuestItems(9851) && !qs.hasQuestItems(9852) ? "32297-01.htm" : "32297-01a.htm";
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      QuestState qs = player.getQuestState(this.getName());
      if (qs == null) {
         qs = this.newQuestState(player);
      }

      return !qs.hasQuestItems(9850) && !qs.hasQuestItems(9851) && !qs.hasQuestItems(9852) ? "32297-02.htm" : "32297-01a.htm";
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      QuestState qs = player.getQuestState(this.getName());
      if (qs == null) {
         qs = this.newQuestState(player);
      }

      if (event.equalsIgnoreCase("badges") && !qs.hasQuestItems(9850) && !qs.hasQuestItems(9851) && !qs.hasQuestItems(9852)) {
         if (qs.getQuestItemsCount(9674) >= 20L) {
            qs.takeItems(9674, 20L);
            qs.giveItems(9850, 1L);
            return "32297-02a.htm";
         } else {
            return "32297-02b.htm";
         }
      } else {
         return event;
      }
   }

   public Falk(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addFirstTalkId(32297);
      this.addStartNpc(32297);
      this.addTalkId(32297);
   }

   public static void main(String[] args) {
      new Falk(-1, "Falk", "hellbound");
   }
}
