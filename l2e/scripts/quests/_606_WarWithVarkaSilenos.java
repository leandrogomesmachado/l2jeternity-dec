package l2e.scripts.quests;

import java.util.HashMap;
import java.util.Map;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _606_WarWithVarkaSilenos extends Quest {
   private static final String qn = "_606_WarWithVarkaSilenos";
   private static final int KADUN = 31370;
   private static final Map<Integer, Integer> MOBS = new HashMap<>();
   private static final int HORN = 7186;
   private static final int MANE = 7233;
   private static final int MIN_LEVEL = 74;
   private static final int MANE_COUNT = 100;

   private _606_WarWithVarkaSilenos(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31370);
      this.addTalkId(31370);
      this.addKillId(MOBS.keySet());
      this.registerQuestItems(new int[]{7233});
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return null;
      } else {
         String htmltext = event;
         switch(event) {
            case "31370-03.htm":
               st.startQuest();
            case "31370-06.html":
               break;
            case "31370-07.html":
               if (st.getQuestItemsCount(7233) < 100L) {
                  return "31370-08.html";
               }

               st.takeItems(7233, 100L);
               st.giveItems(7186, 20L);
               break;
            case "31370-09.html":
               st.exitQuest(true, true);
               break;
            default:
               htmltext = null;
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      Player member = this.getRandomPartyMember(killer, 1);
      if (member != null && getRandom(1000) < MOBS.get(npc.getId())) {
         QuestState st = member.getQuestState(this.getName());
         st.giveItems(7233, 1L);
         st.playSound(Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET);
      }

      return super.onKill(npc, killer, isSummon);
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               htmltext = player.getLevel() >= 74 ? "31370-01.htm" : "31370-02.htm";
               break;
            case 1:
               htmltext = st.hasQuestItems(7233) ? "31370-04.html" : "31370-05.html";
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _606_WarWithVarkaSilenos(606, "_606_WarWithVarkaSilenos", "");
   }

   static {
      MOBS.put(21350, 500);
      MOBS.put(21353, 510);
      MOBS.put(21354, 522);
      MOBS.put(21355, 519);
      MOBS.put(21357, 529);
      MOBS.put(21358, 529);
      MOBS.put(21360, 539);
      MOBS.put(21362, 539);
      MOBS.put(21364, 558);
      MOBS.put(21365, 568);
      MOBS.put(21366, 568);
      MOBS.put(21368, 568);
      MOBS.put(21369, 664);
      MOBS.put(21371, 713);
      MOBS.put(21373, 738);
   }
}
