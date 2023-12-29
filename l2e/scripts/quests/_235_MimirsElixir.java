package l2e.scripts.quests;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _235_MimirsElixir extends Quest {
   private static final String qn = "_235_MimirsElixir";
   private static final int JOAN = 30718;
   private static final int LADD = 30721;
   private static final int MIXING_URN = 31149;
   private static final int STAR_OF_DESTINY = 5011;
   private static final int PURE_SILVER = 6320;
   private static final int TRUE_GOLD = 6321;
   private static final int SAGES_STONE = 6322;
   private static final int BLOOD_FIRE = 6318;
   private static final int MIMIRS_ELIXIR = 6319;
   private static final int MAGISTER_MIXING_STONE = 5905;
   private static final int SCROLL_ENCHANT_WEAPON_A = 729;
   Map<Integer, int[]> droplist = new ConcurrentHashMap<>();

   public _235_MimirsElixir(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30721);
      this.addTalkId(30721);
      this.addTalkId(30718);
      this.addTalkId(31149);
      this.droplist.put(20965, new int[]{3, 6322});
      this.droplist.put(21090, new int[]{6, 6318});
      this.addKillId(new int[]{20965, 21090});
      this.questItemIds = new int[]{6320, 6321, 6322, 6318, 5905, 6319};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_235_MimirsElixir");
      if (st == null) {
         return event;
      } else {
         switch(npc.getId()) {
            case 30718:
               if (event.equalsIgnoreCase("30718-03.htm")) {
                  st.set("cond", "3");
                  st.playSound("ItemSound.quest_middle");
               }
               break;
            case 30721:
               if (event.equalsIgnoreCase("30721-06.htm")) {
                  st.set("cond", "1");
                  st.setState((byte)1);
                  st.playSound("ItemSound.quest_accept");
               } else if (event.equalsIgnoreCase("30721-12.htm") && st.getQuestItemsCount(6321) >= 1L) {
                  st.set("cond", "6");
                  st.playSound("ItemSound.quest_middle");
                  st.giveItems(5905, 1L);
               } else if (event.equalsIgnoreCase("30721-16.htm") && st.getQuestItemsCount(6319) >= 1L) {
                  st.giveItems(729, 1L);
                  st.takeItems(5011, -1L);
                  st.playSound("ItemSound.quest_finish");
                  st.exitQuest(false);
               }
               break;
            case 31149:
               if (event.equalsIgnoreCase("31149-02.htm")) {
                  if (st.getQuestItemsCount(5905) == 0L) {
                     htmltext = "31149-havent.htm";
                  }
               } else if (event.equalsIgnoreCase("31149-03.htm")) {
                  if (st.getQuestItemsCount(5905) == 0L || st.getQuestItemsCount(6320) == 0L) {
                     htmltext = "31149-havent.htm";
                  }
               } else if (event.equalsIgnoreCase("31149-05.htm")) {
                  if (st.getQuestItemsCount(5905) == 0L || st.getQuestItemsCount(6320) == 0L || st.getQuestItemsCount(6321) == 0L) {
                     htmltext = "31149-havent.htm";
                  }
               } else if (event.equalsIgnoreCase("31149-07.htm")) {
                  if (st.getQuestItemsCount(5905) == 0L
                     || st.getQuestItemsCount(6320) == 0L
                     || st.getQuestItemsCount(6321) == 0L
                     || st.getQuestItemsCount(6318) == 0L) {
                     htmltext = "31149-havent.htm";
                  }
               } else if (event.equalsIgnoreCase("31149-success.htm")) {
                  if (st.getQuestItemsCount(5905) != 0L
                     && st.getQuestItemsCount(6320) != 0L
                     && st.getQuestItemsCount(6321) != 0L
                     && st.getQuestItemsCount(6318) != 0L) {
                     st.set("cond", "8");
                     st.playSound("ItemSound.quest_middle");
                     st.takeItems(6320, -1L);
                     st.takeItems(6321, -1L);
                     st.takeItems(6318, -1L);
                     st.giveItems(6319, 1L);
                  } else {
                     htmltext = "31149-havent.htm";
                  }
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_235_MimirsElixir");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() < 75) {
                  htmltext = "30721-01b.htm";
                  st.exitQuest(true);
               } else if (st.getQuestItemsCount(5011) == 0L) {
                  htmltext = "30721-01a.htm";
                  st.exitQuest(true);
               } else {
                  htmltext = "30721-01.htm";
               }
               break;
            case 1:
               int cond = st.getInt("cond");
               switch(npc.getId()) {
                  case 30718:
                     if (cond == 2) {
                        return "30718-01.htm";
                     } else if (cond == 3) {
                        return "30718-04.htm";
                     } else {
                        if (cond == 4 && st.getQuestItemsCount(6322) >= 1L) {
                           htmltext = "30718-05.htm";
                           st.takeItems(6322, -1L);
                           st.giveItems(6321, 1L);
                           st.set("cond", "5");
                           st.playSound("ItemSound.quest_middle");
                        } else if (cond >= 5) {
                           return "30718-06.htm";
                        }

                        return htmltext;
                     }
                  case 30721:
                     if (cond == 1) {
                        if (st.getQuestItemsCount(6320) >= 1L) {
                           st.set("cond", "2");
                           htmltext = "30721-08.htm";
                           st.playSound("ItemSound.quest_middle");
                        } else {
                           htmltext = "30721-07.htm";
                        }

                        return htmltext;
                     } else if (cond > 1 && cond < 5) {
                        return "30721-10.htm";
                     } else if (cond == 5 && st.getQuestItemsCount(6321) >= 1L) {
                        return "30721-11.htm";
                     } else {
                        if (cond == 6 || cond == 7) {
                           htmltext = "30721-13.htm";
                        } else if (cond == 8 && st.getQuestItemsCount(6319) >= 1L) {
                           htmltext = "30721-14.htm";
                           return htmltext;
                        }

                        return htmltext;
                     }
                  case 31149:
                     return "31149-01.htm";
                  default:
                     return htmltext;
               }
            case 2:
               htmltext = getAlreadyCompletedMsg(player);
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_235_MimirsElixir");
      if (st == null) {
         return null;
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         if (st.isStarted() && this.droplist.containsKey(npcId)) {
            int neededCond = this.droplist.get(npcId)[0];
            int item = this.droplist.get(npcId)[1];
            if (st.getRandom(100) < 20 && cond == neededCond && st.getQuestItemsCount(item) == 0L) {
               st.giveItems(item, 1L);
               st.playSound("ItemSound.quest_itemget");
               st.set("cond", String.valueOf(cond + 1));
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _235_MimirsElixir(235, "_235_MimirsElixir", "");
   }
}
