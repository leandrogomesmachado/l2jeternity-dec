package l2e.scripts.custom;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.QuestState;
import l2e.scripts.ai.AbstractNpcAI;

public final class Jinia extends AbstractNpcAI {
   private Jinia() {
      super(Jinia.class.getSimpleName(), "custom");
      this.addStartNpc(32781);
      this.addFirstTalkId(32781);
      this.addTalkId(32781);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      switch(event) {
         case "32781-10.htm":
         case "32781-11.htm":
            htmltext = event;
            break;
         case "check":
            if (this.hasAtLeastOneQuestItem(player, new int[]{15469, 15470})) {
               htmltext = "32781-03.htm";
            } else {
               QuestState qs = player.getQuestState("_10286_ReunionWithSirra");
               if (qs != null && qs.isCompleted()) {
                  giveItems(player, 15469, 1L);
               } else {
                  giveItems(player, 15470, 1L);
               }

               htmltext = "32781-04.htm";
            }
      }

      return htmltext;
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      QuestState qs = player.getQuestState("_10286_ReunionWithSirra");
      if (qs != null && player.getLevel() >= 82) {
         if (qs.isCompleted()) {
            return "32781-02.htm";
         }

         if (qs.isCond(5) || qs.isCond(6)) {
            return "32781-09.htm";
         }
      }

      return "32781-01.htm";
   }

   public static void main(String[] args) {
      new Jinia();
   }
}
