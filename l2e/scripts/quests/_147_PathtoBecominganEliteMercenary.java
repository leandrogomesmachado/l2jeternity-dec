package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _147_PathtoBecominganEliteMercenary extends Quest {
   private static final String qn = "_147_PathtoBecominganEliteMercenary";
   private static final int[] _merc = new int[]{36481, 36482, 36483, 36484, 36485, 36486, 36487, 36488, 36489};
   private static final int _cert_ordinary = 13766;
   private static final int _cert_elite = 13767;

   public _147_PathtoBecominganEliteMercenary(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(_merc);
      this.addTalkId(_merc);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_147_PathtoBecominganEliteMercenary");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("elite-02.htm")) {
            if (st.hasQuestItems(13766)) {
               return "elite-02a.htm";
            }

            st.giveItems(13766, 1L);
         } else if (event.equalsIgnoreCase("elite-04.htm")) {
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
      QuestState st = player.getQuestState("_147_PathtoBecominganEliteMercenary");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getClan() != null && player.getClan().getCastleId() > 0) {
                  htmltext = "castle.htm";
               } else {
                  htmltext = "elite-01.htm";
               }
               break;
            case 1:
               int cond = st.getInt("cond");
               if (cond < 4) {
                  htmltext = "elite-05.htm";
               } else if (cond == 4) {
                  st.takeItems(13766, -1L);
                  st.giveItems(13767, 1L);
                  st.exitQuest(false);
                  htmltext = "elite-06.htm";
               }
               break;
            case 2:
               htmltext = getAlreadyCompletedMsg(player);
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _147_PathtoBecominganEliteMercenary(147, "_147_PathtoBecominganEliteMercenary", "");
   }
}
