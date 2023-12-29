package l2e.scripts.quests;

import java.util.Calendar;
import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _453_NotStrongEnoughAlone extends Quest {
   private static final String qn = "_453_NotStrongEnoughAlone";
   private static final int Klemis = 32734;
   private static final int[] Monsters1 = new int[]{22746, 22747, 22748, 22749, 22750, 22751, 22752, 22753};
   private static final int[] Monsters2 = new int[]{22754, 22755, 22756, 22757, 22758, 22759};
   private static final int[] Monsters3 = new int[]{22760, 22761, 22762, 22763, 22764, 22765};
   private static final int ResetHour = 6;
   private static final int ResetMin = 30;

   public _453_NotStrongEnoughAlone(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32734);
      this.addTalkId(32734);

      for(int i : Monsters1) {
         this.addKillId(i);
      }

      for(int i : Monsters2) {
         this.addKillId(i);
      }

      for(int i : Monsters3) {
         this.addKillId(i);
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_453_NotStrongEnoughAlone");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32734-06.html")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("32734-07.html")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32734-08.html")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32734-09.html")) {
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_453_NotStrongEnoughAlone");
      if (st == null) {
         return htmltext;
      } else {
         QuestState prev = player.getQuestState("_10282_ToTheSeedOfAnnihilation");
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 84 && prev != null && prev.getState() == 2) {
                  htmltext = "32734-01.html";
               } else {
                  htmltext = "32734-03.html";
               }
               break;
            case 1:
               if (st.getInt("cond") == 1) {
                  htmltext = "32734-10.html";
               } else if (st.getInt("cond") == 2) {
                  htmltext = "32734-11.html";
               } else if (st.getInt("cond") == 3) {
                  htmltext = "32734-12.html";
               } else if (st.getInt("cond") == 4) {
                  htmltext = "32734-13.html";
               } else if (st.getInt("cond") == 5) {
                  boolean i1 = getRandomBoolean();
                  int i0 = getRandom(100);
                  if (i1) {
                     if (i0 < 9) {
                        st.giveItems(15815, 1L);
                     } else if (i0 < 18) {
                        st.giveItems(15816, 1L);
                     } else if (i0 < 27) {
                        st.giveItems(15817, 1L);
                     } else if (i0 < 36) {
                        st.giveItems(15818, 1L);
                     } else if (i0 < 47) {
                        st.giveItems(15819, 1L);
                     } else if (i0 < 56) {
                        st.giveItems(15820, 1L);
                     } else if (i0 < 65) {
                        st.giveItems(15821, 1L);
                     } else if (i0 < 74) {
                        st.giveItems(15822, 1L);
                     } else if (i0 < 83) {
                        st.giveItems(15823, 1L);
                     } else if (i0 < 92) {
                        st.giveItems(15824, 1L);
                     } else {
                        st.giveItems(15825, 1L);
                     }
                  } else if (i0 < 9) {
                     st.giveItems(15634, 1L);
                  } else if (i0 < 18) {
                     st.giveItems(15635, 1L);
                  } else if (i0 < 27) {
                     st.giveItems(15636, 1L);
                  } else if (i0 < 36) {
                     st.giveItems(15637, 1L);
                  } else if (i0 < 47) {
                     st.giveItems(15638, 1L);
                  } else if (i0 < 56) {
                     st.giveItems(15639, 1L);
                  } else if (i0 < 65) {
                     st.giveItems(15640, 1L);
                  } else if (i0 < 74) {
                     st.giveItems(15641, 1L);
                  } else if (i0 < 83) {
                     st.giveItems(15642, 1L);
                  } else if (i0 < 92) {
                     st.giveItems(15643, 1L);
                  } else {
                     st.giveItems(15644, 1L);
                  }

                  st.exitQuest(false);
                  st.playSound("ItemSound.quest_finish");
                  htmltext = "32734-14.html";
                  Calendar reset = Calendar.getInstance();
                  reset.set(12, 30);
                  if (reset.get(11) >= 6) {
                     reset.add(5, 1);
                  }

                  reset.set(11, 6);
                  st.set("reset", String.valueOf(reset.getTimeInMillis()));
               }
               break;
            case 2:
               if (Long.parseLong(st.get("reset")) > System.currentTimeMillis()) {
                  htmltext = "32734-02.html";
               } else {
                  st.setState((byte)0);
                  if (player.getLevel() >= 84 && prev != null && prev.getState() == 2) {
                     htmltext = "32734-01.html";
                  } else {
                     htmltext = "32734-03.html";
                  }
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      if (player.getParty() != null) {
         for(Player member : player.getParty().getMembers()) {
            this.increaseNpcKill(member, npc);
         }
      } else {
         this.increaseNpcKill(player, npc);
      }

      return null;
   }

   private void increaseNpcKill(Player player, Npc npc) {
      QuestState st = player.getQuestState("_453_NotStrongEnoughAlone");
      if (st != null) {
         if (Util.contains(Monsters1, npc.getId()) && st.getInt("cond") == 2) {
            int val = 0;
            if (npc.getId() == Monsters1[0] || npc.getId() == Monsters1[4]) {
               val = Monsters1[0];
            } else if (npc.getId() == Monsters1[1] || npc.getId() == Monsters1[5]) {
               val = Monsters1[1];
            } else if (npc.getId() == Monsters1[2] || npc.getId() == Monsters1[6]) {
               val = Monsters1[2];
            } else if (npc.getId() == Monsters1[3] || npc.getId() == Monsters1[7]) {
               val = Monsters1[3];
            }

            int i = st.getInt(String.valueOf(val));
            if (i < 15) {
               st.set(String.valueOf(val), String.valueOf(i + 1));
            }

            if (st.getInt(String.valueOf(Monsters1[0])) >= 15
               && st.getInt(String.valueOf(Monsters1[1])) >= 15
               && st.getInt(String.valueOf(Monsters1[2])) >= 15
               && st.getInt(String.valueOf(Monsters1[3])) >= 15) {
               st.set("cond", "5");
               st.playSound("ItemSound.quest_middle");
            } else {
               st.playSound("ItemSound.quest_itemget");
            }
         } else if (Util.contains(Monsters2, npc.getId()) && st.getInt("cond") == 3) {
            int val = 0;
            if (npc.getId() == Monsters2[0] || npc.getId() == Monsters2[3]) {
               val = Monsters2[0];
            } else if (npc.getId() == Monsters2[1] || npc.getId() == Monsters2[4]) {
               val = Monsters2[1];
            } else if (npc.getId() == Monsters2[2] || npc.getId() == Monsters2[5]) {
               val = Monsters2[2];
            }

            int i = st.getInt(String.valueOf(val));
            if (i < 20) {
               st.set(String.valueOf(val), String.valueOf(i + 1));
            }

            if (st.getInt(String.valueOf(Monsters2[0])) >= 20
               && st.getInt(String.valueOf(Monsters2[1])) >= 20
               && st.getInt(String.valueOf(Monsters2[2])) >= 20) {
               st.set("cond", "5");
               st.playSound("ItemSound.quest_middle");
            } else {
               st.playSound("ItemSound.quest_itemget");
            }
         } else if (Util.contains(Monsters3, npc.getId()) && st.getInt("cond") == 4) {
            int val = 0;
            if (npc.getId() == Monsters3[0] || npc.getId() == Monsters3[3]) {
               val = Monsters3[0];
            } else if (npc.getId() == Monsters3[1] || npc.getId() == Monsters3[4]) {
               val = Monsters3[1];
            } else if (npc.getId() == Monsters3[2] || npc.getId() == Monsters3[5]) {
               val = Monsters3[2];
            }

            int i = st.getInt(String.valueOf(val));
            if (i < 20) {
               st.set(String.valueOf(val), String.valueOf(i + 1));
            }

            if (st.getInt(String.valueOf(Monsters3[0])) >= 20
               && st.getInt(String.valueOf(Monsters3[1])) >= 20
               && st.getInt(String.valueOf(Monsters3[2])) >= 20) {
               st.set("cond", "5");
               st.playSound("ItemSound.quest_middle");
            } else {
               st.playSound("ItemSound.quest_itemget");
            }
         }
      }
   }

   public static void main(String[] args) {
      new _453_NotStrongEnoughAlone(453, "_453_NotStrongEnoughAlone", "");
   }
}
