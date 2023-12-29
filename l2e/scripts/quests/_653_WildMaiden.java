package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.serverpackets.MagicSkillUse;

public class _653_WildMaiden extends Quest {
   private static final String qn = "_653_WildMaiden";
   private static final int[][] spawns = new int[][]{
      {66578, 72351, -3731, 0}, {77189, 73610, -3708, 2555}, {71809, 67377, -3675, 29130}, {69166, 88825, -3447, 43886}
   };
   private int _currentPosition = 0;

   public _653_WildMaiden(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32013);
      this.addTalkId(new int[]{32013, 30181});
      addSpawn(32013, 66578, 72351, -3731, 0, false, 0L);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_653_WildMaiden");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32013-03.htm")) {
            if (st.getQuestItemsCount(736) >= 1L) {
               st.set("cond", "1");
               st.setState((byte)1);
               st.takeItems(736, 1L);
               st.playSound("ItemSound.quest_accept");
               npc.broadcastPacket(new MagicSkillUse(npc, npc, 2013, 1, 3500, 0));
               this.startQuestTimer("apparition_npc", 4000L, npc, player);
            } else {
               htmltext = "32013-03a.htm";
               st.exitQuest(true);
            }
         } else if (event.equalsIgnoreCase("apparition_npc")) {
            int chance = st.getRandom(4);

            while(chance == this._currentPosition) {
               chance = st.getRandom(4);
            }

            this._currentPosition = chance;
            npc.deleteMe();
            addSpawn(32013, spawns[chance][0], spawns[chance][1], spawns[chance][2], spawns[chance][3], false, 0L);
            return null;
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_653_WildMaiden");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 26) {
                  htmltext = "32013-02.htm";
               } else {
                  htmltext = "32013-01.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               switch(npc.getId()) {
                  case 30181:
                     htmltext = "30181-01.htm";
                     st.rewardItems(57, 2883L);
                     st.playSound("ItemSound.quest_finish");
                     st.exitQuest(true);
                     break;
                  case 32013:
                     htmltext = "32013-04a.htm";
               }
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _653_WildMaiden(653, "_653_WildMaiden", "");
   }
}
