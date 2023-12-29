package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _602_ShadowOfLight extends Quest {
   private static final int EYE_OF_ARGOS = 31683;
   private static final int EYE_OF_DARKNESS = 7189;
   private static final int[] MOBS = new int[]{21299, 21304};
   private static final int[][] REWARD = new int[][]{
      {6699, 40000, 120000, 20000}, {6698, 60000, 110000, 15000}, {6700, 40000, 150000, 10000}, {0, 100000, 140000, 11250}
   };

   public _602_ShadowOfLight(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31683);
      this.addTalkId(31683);
      this.addKillId(MOBS);
      this.registerQuestItems(new int[]{7189});
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return null;
      } else {
         String htmltext = event;
         switch(event) {
            case "31683-02.htm":
               st.startQuest();
               break;
            case "31683-05.htm":
               if (st.getQuestItemsCount(7189) < 100L) {
                  return "31683-06.htm";
               }

               int i = getRandom(4);
               if (i < 3) {
                  st.giveItems(REWARD[i][0], 3L);
               }

               st.rewardItems(57, (long)REWARD[i][1]);
               st.addExpAndSp(REWARD[i][2], REWARD[i][3]);
               st.exitQuest(true, true);
               break;
            default:
               htmltext = null;
         }

         return htmltext;
      }
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
               htmltext = player.getLevel() >= 68 ? "31683-01.htm" : "31683-00.htm";
               break;
            case 1:
               htmltext = st.isCond(1) ? "31683-03.htm" : "31683-04.htm";
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return super.onKill(npc, player, isSummon);
      } else {
         int chance = npc.getId() == MOBS[0] ? 560 : 800;
         if (st.isCond(1) && getRandom(1000) < chance) {
            st.giveItems(7189, 1L);
            if (st.getQuestItemsCount(7189) == 100L) {
               st.setCond(2, true);
            } else {
               st.playSound("ItemSound.quest_itemget");
            }
         }

         return super.onKill(npc, player, isSummon);
      }
   }

   public static void main(String[] args) {
      new _602_ShadowOfLight(602, _602_ShadowOfLight.class.getSimpleName(), "");
   }
}
