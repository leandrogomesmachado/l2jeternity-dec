package l2e.scripts.quests;

import l2e.gameserver.instancemanager.HellboundManager;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _131_BirdInACage extends Quest {
   private static final String qn = "_131_BirdInACage";
   private static final int KANIS = 32264;
   private static final int PARME = 32271;
   private static final int GIFTBOX = 32342;
   private static final int[][] GIFTBOXITEMS = new int[][]{{9692, 100, 2}, {9693, 50, 1}};
   private static final int KANIS_ECHO_CRY = 9783;
   private static final int PARMES_LETTER = 9784;
   private static final int KISSOFEVA = 1073;

   public _131_BirdInACage(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(32264);
      this.addTalkId(32264);
      this.addTalkId(32271);
      this.addKillId(32342);
      this.addSpawnId(new int[]{32342});
      this.questItemIds = new int[]{9783, 9784};
   }

   @Override
   public String onSpawn(Npc npc) {
      npc.setIsNoRndWalk(true);
      return super.onSpawn(npc);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_131_BirdInACage");
      if (st == null) {
         return event;
      } else {
         int cond = st.getInt("cond");
         if (event.equalsIgnoreCase("32264-02.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("32264-08.htm") && cond == 1) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(9783, 1L);
         } else if (event.equalsIgnoreCase("32271-03.htm") && cond == 2) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(9784, 1L);
            player.setReflectionId(0);
            player.teleToLocation(143472 + getRandom(-100, 100), 191040 + getRandom(-100, 100), -3696, true);
         } else if (event.equalsIgnoreCase("32264-12.htm") && cond == 3) {
            st.playSound("ItemSound.quest_middle");
            st.takeItems(9784, -1L);
         } else if (event.equalsIgnoreCase("32264-13.htm") && cond == 3) {
            HellboundManager.getInstance().unlock();
            st.playSound("ItemSound.quest_finish");
            st.takeItems(9783, -1L);
            st.addExpAndSp(1304752, 25019);
            st.exitQuest(false);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_131_BirdInACage");
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         if (st.isCompleted()) {
            htmltext = getAlreadyCompletedMsg(player);
         } else if (npcId == 32264) {
            if (cond == 0) {
               if (player.getLevel() >= 78) {
                  htmltext = "32264-01.htm";
               } else {
                  htmltext = "32264-00.htm";
                  st.exitQuest(true);
               }
            } else if (cond == 1) {
               htmltext = "32264-03.htm";
            } else if (cond == 2) {
               htmltext = "32264-08a.htm";
            } else if (cond == 3) {
               if (st.getQuestItemsCount(9784) > 0L) {
                  htmltext = "32264-11.htm";
               } else {
                  htmltext = "32264-12.htm";
               }
            }
         } else if (npcId == 32271 && cond == 2) {
            htmltext = "32271-01.htm";
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      QuestState st = killer.getQuestState("_131_BirdInACage");
      if (st == null) {
         return null;
      } else {
         if (npc.getId() == 32342 && killer.getFirstEffect(1073) != null) {
            for(int[] GIFTBOXITEM : GIFTBOXITEMS) {
               if (getRandom(100) < GIFTBOXITEM[1]) {
                  st.giveItems(GIFTBOXITEM[0], 1L);
               }
            }
         }

         return super.onKill(npc, killer, isSummon);
      }
   }

   public static void main(String[] args) {
      new _131_BirdInACage(131, "_131_BirdInACage", "");
   }
}
