package l2e.scripts.quests;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.SocialAction;

public class _175_TheWayOfTheWarrior extends Quest {
   private static final String qn = "_175_TheWayOfTheWarrior";
   private static final int Kekropus = 32138;
   private static final int Perwan = 32133;
   private static final int WolfTail = 9807;
   private static final int MuertosClaw = 9808;
   private static final int WarriorsSword = 9720;
   private static final int MountainWerewolf = 22235;
   private static final int[] MUERTOS = new int[]{22236, 22239, 22240, 22242, 22243, 22245, 22246};
   private static final int NEWBIE_REWARD = 16;
   private static final int SOULSHOT_FOR_BEGINNERS = 5789;

   public _175_TheWayOfTheWarrior(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32138);
      this.addTalkId(32138);
      this.addTalkId(32133);
      this.addKillId(22235);

      for(int i : MUERTOS) {
         this.addKillId(i);
      }

      this.questItemIds = new int[]{9807, 9808};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_175_TheWayOfTheWarrior");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32138-04.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("32133-06.htm")) {
            st.set("cond", "6");
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("32138-09.htm")) {
            st.set("cond", "7");
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("32138-12.htm")) {
            int newbie = player.getNewbie();
            if ((newbie | 16) != newbie) {
               player.setNewbie(newbie | 16);
               showOnScreenMsg(player, NpcStringId.ACQUISITION_OF_RACE_SPECIFIC_WEAPON_COMPLETE_N_GO_FIND_THE_NEWBIE_GUIDE, 2, 5000, new String[0]);
               st.giveItems(1060, 100L);

               for(int item = 4412; item <= 4417; ++item) {
                  st.giveItems(item, 10L);
               }

               st.playTutorialVoice("tutorial_voice_026");
               st.giveItems(5789, 7000L);
            }

            st.takeItems(9808, -1L);
            st.giveItems(9720, 1L);
            st.giveItems(57, 8799L);
            st.addExpAndSp(20739, 1777);
            player.sendPacket(new SocialAction(player.getObjectId(), 3));
            player.sendPacket(new SocialAction(player.getObjectId(), 15));
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_175_TheWayOfTheWarrior");
      String htmltext = getNoQuestMsg(player);
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         int id = st.getState();
         if (id == 2) {
            htmltext = getAlreadyCompletedMsg(player);
         } else if (id == 0 && npcId == 32138) {
            if (player.getLevel() >= 10 && player.getRace().ordinal() == 5) {
               htmltext = "32138-01.htm";
            } else {
               htmltext = "32138-02.htm";
               st.exitQuest(true);
            }
         } else if (id == 1) {
            if (npcId == 32138) {
               if (cond == 1) {
                  htmltext = "32138-05.htm";
               } else if (cond == 4) {
                  st.set("cond", "5");
                  st.playSound("ItemSound.quest_middle");
                  htmltext = "32138-06.htm";
               } else if (cond == 5) {
                  htmltext = "32138-07.htm";
               } else if (cond == 6) {
                  htmltext = "32138-08.htm";
               } else if (cond == 7) {
                  htmltext = "32138-10.htm";
               } else if (cond == 8) {
                  htmltext = "32138-11.htm";
               }
            } else if (npcId == 32133) {
               if (cond == 1) {
                  st.set("cond", "2");
                  st.playSound("ItemSound.quest_middle");
                  htmltext = "32133-01.htm";
               } else if (cond == 2) {
                  htmltext = "32133-02.htm";
               } else if (cond == 3) {
                  st.takeItems(9807, -1L);
                  st.set("cond", "4");
                  st.playSound("ItemSound.quest_middle");
                  htmltext = "32133-03.htm";
               } else if (cond == 4) {
                  htmltext = "32133-04.htm";
               } else if (cond == 5) {
                  htmltext = "32133-05.htm";
               } else if (cond == 6) {
                  htmltext = "32133-07.htm";
               }
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_175_TheWayOfTheWarrior");
      if (st == null) {
         return null;
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         int chance = getRandom(100);
         long tails = st.getQuestItemsCount(9807);
         long claws = st.getQuestItemsCount(9808);
         if (npcId == 22235 && chance < 50 && cond == 2 && tails < 5L) {
            st.giveItems(9807, 1L);
            st.playSound("ItemSound.quest_itemget");
            if (st.getQuestItemsCount(9807) == 5L) {
               st.set("cond", "3");
               st.playSound("ItemSound.quest_middle");
            }
         } else if (Util.contains(MUERTOS, npc.getId()) && claws < 10L && cond == 7) {
            st.giveItems(9808, 1L);
            st.playSound("ItemSound.quest_itemget");
            if (st.getQuestItemsCount(9808) == 10L) {
               st.set("cond", "8");
               st.playSound("ItemSound.quest_middle");
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _175_TheWayOfTheWarrior(175, "_175_TheWayOfTheWarrior", "");
   }
}
