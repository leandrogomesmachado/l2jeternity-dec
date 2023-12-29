package l2e.scripts.custom;

import java.util.Calendar;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.QuestState;
import l2e.scripts.ai.AbstractNpcAI;

public class BlackMarketeerOfMammon extends AbstractNpcAI {
   private static final int BLACK_MARKETEER = 31092;
   private static final int MIN_LEVEL = 60;

   private BlackMarketeerOfMammon(String name, String descr) {
      super(name, descr);
      this.addStartNpc(31092);
      this.addTalkId(31092);
   }

   @Override
   public String onTalk(Npc npc, Player talker) {
      return this.exchangeAvailable() ? "31092-01.htm" : "31092-02.htm";
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState qs = player.getQuestState(this.getName());
      if ("exchange".equals(event)) {
         if (this.exchangeAvailable()) {
            if (player.getLevel() >= 60) {
               if (!qs.isNowAvailable()) {
                  htmltext = "31092-03.htm";
               } else if (player.getAdena() >= 2000000L) {
                  qs.setState((byte)1);
                  takeItems(player, 57, 2000000L);
                  giveItems(player, 5575, 500000L);
                  htmltext = "31092-04.htm";
                  qs.exitQuest(QuestState.QuestType.DAILY, false);
               } else {
                  htmltext = "31092-05.htm";
               }
            } else {
               htmltext = "31092-06.htm";
            }
         } else {
            htmltext = "31092-02.htm";
         }
      }

      return htmltext;
   }

   private boolean exchangeAvailable() {
      Calendar currentTime = Calendar.getInstance();
      Calendar minTime = Calendar.getInstance();
      minTime.set(11, 20);
      minTime.set(12, 0);
      minTime.set(13, 0);
      Calendar maxtTime = Calendar.getInstance();
      maxtTime.set(11, 23);
      maxtTime.set(12, 59);
      maxtTime.set(13, 59);
      return currentTime.compareTo(minTime) >= 0 && currentTime.compareTo(maxtTime) <= 0;
   }

   public static void main(String[] args) {
      new BlackMarketeerOfMammon(BlackMarketeerOfMammon.class.getSimpleName(), "custom");
   }
}
