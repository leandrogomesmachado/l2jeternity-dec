package l2e.scripts.quests;

import l2e.commons.util.Rnd;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _343_UndertheShadowoftheIvoryTower extends Quest {
   private static final String qn = "_343_UndertheShadowoftheIvoryTower";
   public final int CEMA = 30834;
   public final int ICARUS = 30835;
   public final int MARSHA = 30934;
   public final int TRUMPIN = 30935;
   public final int[] MOBS = new int[]{20563, 20564, 20565, 20566};
   public final int ORB = 4364;
   public final int ECTOPLASM = 4365;
   public final int[] AllowClass = new int[]{11, 12, 13, 14, 26, 27, 28, 39, 40, 41};
   public final int CHANCE = 50;

   public _343_UndertheShadowoftheIvoryTower(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30834);
      this.addTalkId(30834);
      this.addTalkId(30835);
      this.addTalkId(30934);
      this.addTalkId(30935);

      for(int i : this.MOBS) {
         this.addKillId(i);
      }

      this.questItemIds = new int[]{4364};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_343_UndertheShadowoftheIvoryTower");
      if (st == null) {
         return null;
      } else {
         String htmltext = event;
         int random1 = getRandom(3);
         int random2 = getRandom(2);
         long orbs = st.getQuestItemsCount(4364);
         if (event.equalsIgnoreCase("30834-03.htm")) {
            st.setState((byte)1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30834-08.htm")) {
            if (orbs > 0L) {
               st.rewardItems(57, orbs * 120L);
               st.takeItems(4364, -1L);
            } else {
               htmltext = "30834-08.htm";
            }
         } else if (event.equalsIgnoreCase("30834-09.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         } else if (!event.equalsIgnoreCase("30934-02.htm") && !event.equalsIgnoreCase("30934-03.htm")) {
            if (event.equalsIgnoreCase("30934-04.htm")) {
               if (st.getInt("playing") > 0) {
                  if (random1 == 0) {
                     htmltext = "30934-05.htm";
                     st.giveItems(4364, 10L);
                  } else if (random1 == 1) {
                     htmltext = "30934-06.htm";
                  } else {
                     htmltext = "30934-04.htm";
                     st.giveItems(4364, 20L);
                  }

                  st.unset("playing");
               } else {
                  htmltext = "Player is cheating";
                  st.takeItems(4364, -1L);
                  st.exitQuest(true);
               }
            } else if (event.equalsIgnoreCase("30934-05.htm")) {
               if (st.getInt("playing") > 0) {
                  if (random1 == 0) {
                     htmltext = "30934-04.htm";
                     st.giveItems(4364, 20L);
                  } else if (random1 == 1) {
                     htmltext = "30934-05.htm";
                     st.giveItems(4364, 10L);
                  } else {
                     htmltext = "30934-06.htm";
                  }

                  st.unset("playing");
               } else {
                  htmltext = "Player is cheating";
                  st.takeItems(4364, -1L);
                  st.exitQuest(true);
               }
            } else if (event.equalsIgnoreCase("30934-06.htm")) {
               if (st.getInt("playing") > 0) {
                  if (random1 == 0) {
                     htmltext = "30934-04.htm";
                     st.giveItems(4364, 20L);
                  } else if (random1 == 1) {
                     htmltext = "30934-06.htm";
                  } else {
                     htmltext = "30934-05.htm";
                     st.giveItems(4364, 10L);
                  }

                  st.unset("playing");
               } else {
                  htmltext = "Player is cheating";
                  st.takeItems(4364, -1L);
                  st.exitQuest(true);
               }
            } else if (event.equalsIgnoreCase("30935-02.htm") || event.equalsIgnoreCase("30935-03.htm")) {
               st.unset("toss");
               if (orbs < 10L) {
                  htmltext = "noorbs.htm";
               }
            } else if (event.equalsIgnoreCase("30935-05.htm")) {
               if (orbs >= 10L) {
                  if (random2 == 0) {
                     int toss = st.getInt("toss");
                     if (toss == 4) {
                        st.unset("toss");
                        st.giveItems(4364, 150L);
                        htmltext = "30935-07.htm";
                     } else {
                        st.set("toss", String.valueOf(toss + 1));
                        htmltext = "30935-04.htm";
                     }
                  } else {
                     st.unset("toss");
                     st.takeItems(4364, 10L);
                  }
               } else {
                  htmltext = "noorbs.htm";
               }
            } else if (event.equalsIgnoreCase("30935-06.htm")) {
               if (orbs >= 10L) {
                  int toss = st.getInt("toss");
                  st.unset("toss");
                  if (toss == 1) {
                     st.giveItems(4364, 10L);
                  } else if (toss == 2) {
                     st.giveItems(4364, 30L);
                  } else if (toss == 3) {
                     st.giveItems(4364, 70L);
                  } else if (toss == 4) {
                     st.giveItems(4364, 150L);
                  }
               } else {
                  htmltext = "noorbs.htm";
               }
            } else if (event.equalsIgnoreCase("30835-02.htm")) {
               if (st.getQuestItemsCount(4365) > 0L) {
                  st.takeItems(4365, 1L);
                  int random = getRandom(1000);
                  if (random <= 119) {
                     st.giveItems(955, 1L);
                  } else if (random <= 169) {
                     st.giveItems(951, 1L);
                  } else if (random <= 329) {
                     st.giveItems(2511, (long)(getRandom(200) + 401));
                  } else if (random <= 559) {
                     st.giveItems(2510, (long)(getRandom(200) + 401));
                  } else if (random <= 561) {
                     st.giveItems(316, 1L);
                  } else if (random <= 578) {
                     st.giveItems(630, 1L);
                  } else if (random <= 579) {
                     st.giveItems(188, 1L);
                  } else if (random <= 581) {
                     st.giveItems(885, 1L);
                  } else if (random <= 582) {
                     st.giveItems(103, 1L);
                  } else if (random <= 584) {
                     st.giveItems(917, 1L);
                  } else {
                     st.giveItems(736, 1L);
                  }
               } else {
                  htmltext = "30835-03.htm";
               }
            }
         } else if (orbs < 10L) {
            htmltext = "noorbs.htm";
         } else if (event.equalsIgnoreCase("30934-03.htm")) {
            if (orbs >= 10L) {
               st.takeItems(4364, 10L);
               st.set("playing", "1");
            } else {
               htmltext = "noorbs.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_343_UndertheShadowoftheIvoryTower");
      if (st == null) {
         return null;
      } else {
         int npcId = npc.getId();
         String htmltext = getNoQuestMsg(player);
         int id = st.getState();
         if (npcId == 30834) {
            if (id != 1) {
               for(int i : this.AllowClass) {
                  if (player.getClassId().getId() == i && player.getLevel() >= 40) {
                     htmltext = "30834-01.htm";
                  }
               }

               if (!htmltext.equals("30834-01.htm")) {
                  htmltext = "30834-07.htm";
                  st.exitQuest(true);
               }
            } else if (st.getQuestItemsCount(4364) > 0L) {
               htmltext = "30834-06.htm";
            } else {
               htmltext = "30834-05.htm";
            }
         } else if (npcId == 30835) {
            htmltext = "30835-01.htm";
         } else if (npcId == 30934) {
            htmltext = "30934-01.htm";
         } else if (npcId == 30935) {
            htmltext = "30935-01.htm";
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isPet) {
      QuestState st = player.getQuestState("_343_UndertheShadowoftheIvoryTower");
      if (st == null) {
         return null;
      } else if (st.getState() != 1) {
         return null;
      } else {
         if (Rnd.chance(50)) {
            st.giveItems(4364, 1L);
            st.playSound("ItemSound.quest_itemget");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _343_UndertheShadowoftheIvoryTower(343, "_343_UndertheShadowoftheIvoryTower", "");
   }
}
