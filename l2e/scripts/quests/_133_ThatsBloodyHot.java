package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public final class _133_ThatsBloodyHot extends Quest {
   private static final String qn = "_133_ThatsBloodyHot";
   private static final int KANIS = 32264;
   private static final int GALATE = 32292;
   private static final int CRYSTAL_SAMPLE = 9785;

   public _133_ThatsBloodyHot(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32264);
      this.addTalkId(32264);
      this.addTalkId(32292);
      this.questItemIds = new int[]{9785};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_133_ThatsBloodyHot");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32264-02.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("32264-07.htm")) {
            st.set("cond", "2");
            st.giveItems(9785, 1L);
         } else if (event.equals("32292-04.htm")) {
            st.takeItems(9785, 1L);
            st.giveItems(57, 254247L);
            st.addExpAndSp(331457, 32524);
            st.setState((byte)2);
            st.exitQuest(false);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_133_ThatsBloodyHot");
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         QuestState qs131 = player.getQuestState("_131_BirdInACage");
         if (st.isCompleted()) {
            htmltext = getAlreadyCompletedMsg(player);
         }

         switch(npcId) {
            case 32264:
               switch(cond) {
                  case 0:
                     if (qs131 != null && qs131.isCompleted() && player.getLevel() >= 78) {
                        htmltext = "32264-01.htm";
                     } else {
                        htmltext = "32264-00.htm";
                     }

                     return htmltext;
                  case 1:
                     return "32264-02.htm";
                  case 2:
                     htmltext = "32264-07.htm";
                     return htmltext;
                  default:
                     return htmltext;
               }
            case 32292:
               switch(cond) {
                  case 2:
                     htmltext = "32292-01.htm";
               }
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _133_ThatsBloodyHot(133, "_133_ThatsBloodyHot", "");
   }
}
