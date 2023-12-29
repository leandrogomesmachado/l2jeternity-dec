package l2e.scripts.quests;

import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public final class _998_FallenAngelSelect extends Quest {
   private static final String qn = "_998_FallenAngelSelect";
   private static int NATOOLS = 30894;

   public _998_FallenAngelSelect(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(NATOOLS);
      this.addTalkId(NATOOLS);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_998_FallenAngelSelect");
      if (st == null) {
         return event;
      } else if (event.equalsIgnoreCase("dawn")) {
         Quest q1 = QuestManager.getInstance().getQuest("_142_FallenAngelRequestOfDawn");
         QuestState qs1 = player.getQuestState("_142_FallenAngelRequestOfDawn");
         if (q1 != null) {
            qs1 = q1.newQuestState(player);
            qs1.setState((byte)1);
            q1.notifyEvent("30894-01.htm", npc, player);
            st.setState((byte)2);
         }

         return null;
      } else if (event.equalsIgnoreCase("dusk")) {
         Quest q2 = QuestManager.getInstance().getQuest("_143_FallenAngelRequestOfDusk");
         QuestState qs2 = player.getQuestState("_143_FallenAngelRequestOfDusk");
         if (q2 != null) {
            qs2 = q2.newQuestState(player);
            qs2.setState((byte)1);
            q2.notifyEvent("30894-01.htm", npc, player);
            st.setState((byte)2);
         }

         return null;
      } else {
         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_998_FallenAngelSelect");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 1:
               htmltext = "30894-01.htm";
            default:
               return htmltext;
         }
      }
   }

   public static void main(String[] args) {
      new _998_FallenAngelSelect(998, "_998_FallenAngelSelect", "");
   }
}
