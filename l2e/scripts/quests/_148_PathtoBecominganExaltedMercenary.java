package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _148_PathtoBecominganExaltedMercenary extends Quest {
   private static final String qn = "_148_PathtoBecominganExaltedMercenary";
   private static final int[] _merc = new int[]{36481, 36482, 36483, 36484, 36485, 36486, 36487, 36488, 36489};
   private static final int _cert_elite = 13767;
   private static final int _cert_top_elite = 13768;

   public _148_PathtoBecominganExaltedMercenary(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(_merc);
      this.addTalkId(_merc);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_148_PathtoBecominganExaltedMercenary");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("exalted-00b.htm")) {
            st.giveItems(13767, 1L);
         } else if (event.equalsIgnoreCase("exalted-03.htm")) {
            st.setState((byte)1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_148_PathtoBecominganExaltedMercenary");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               QuestState _prev = player.getQuestState("_147_PathtoBecominganEliteMercenary");
               if (player.getClan() != null && player.getClan().getCastleId() > 0) {
                  htmltext = "castle.htm";
               } else if (st.hasQuestItems(13767)) {
                  htmltext = "exalted-01.htm";
               } else if (_prev != null && _prev.isCompleted()) {
                  htmltext = "exalted-00a.htm";
               } else {
                  htmltext = "exalted-00.htm";
               }
               break;
            case 1:
               if (st.getInt("cond") < 4) {
                  htmltext = "exalted-04.htm";
               } else if (st.getInt("cond") == 4) {
                  st.takeItems(13767, -1L);
                  st.giveItems(13768, 1L);
                  st.exitQuest(false);
                  htmltext = "exalted-05.htm";
               }
               break;
            case 2:
               htmltext = getAlreadyCompletedMsg(player);
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _148_PathtoBecominganExaltedMercenary(148, "_148_PathtoBecominganExaltedMercenary", "");
   }
}
