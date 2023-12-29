package l2e.scripts.quests;

import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _308_ReedFieldMaintenance extends Quest {
   private static final String qn = "_308_ReedFieldMaintenance";
   private static final int Katensa = 32646;
   private static final int[] Mucrokians = new int[]{22650, 22651, 22652, 22653};
   private static final int ContaminatedMucrokian = 22654;
   private static final int ChangedMucrokian = 22655;
   private static final int MucrokianHide = 14871;
   private static final int AwakenedMucrokianHide = 14872;

   public _308_ReedFieldMaintenance(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(32646);
      this.addTalkId(32646);
      this.addKillId(22655);
      this.addKillId(22654);

      for(int i : Mucrokians) {
         this.addKillId(i);
      }

      this.questItemIds = new int[]{14871, 14872};
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_308_ReedFieldMaintenance");
      if (st == null) {
         return event;
      } else {
         if (event.equals("32646-3.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equals("32646-5.htm")) {
            if (st.getQuestItemsCount(14866) > 0L) {
               htmltext = "32646-5b.htm";
            }
         } else if (event.equals("32646-8.htm")) {
            if (st.getQuestItemsCount(14872) > 0L) {
               long Hides = st.getQuestItemsCount(14872);
               int Reward = (int)(Hides * 2L);
               st.giveItems(14871, (long)Reward);
               st.takeItems(14872, -1L);
            }
         } else if (event.equals("32646-8a.htm")) {
            if (st.getQuestItemsCount(14872) > 0L) {
               long Hides = st.getQuestItemsCount(14872);
               int Reward = (int)(Hides * 2L);
               st.giveItems(14871, (long)Reward);
               st.takeItems(14872, -1L);
            }
         } else if (event.equals("32646-10.htm")) {
            st.exitQuest(true);
            st.playSound("ItemSound.quest_finish");
         } else if (event.equals("32646-11.htm")) {
            if (st.getQuestItemsCount(14871) >= 346L) {
               st.takeItems(14871, 346L);
               st.giveItems(9985, 1L);
               st.playSound("ItemSound.quest_middle");
               htmltext = "32646-11.htm";
            } else {
               htmltext = "32646-8no.htm";
            }
         } else if (event.equals("32646-12.htm")) {
            if (st.getQuestItemsCount(14871) >= 462L) {
               st.takeItems(14871, 462L);
               st.giveItems(9986, 1L);
               st.playSound("ItemSound.quest_middle");
               htmltext = "32646-11.htm";
            } else {
               htmltext = "32646-8no.htm";
            }
         } else if (event.equals("32646-13.htm")) {
            if (st.getQuestItemsCount(14871) >= 232L) {
               st.takeItems(14871, 232L);
               st.giveItems(9987, 1L);
               st.playSound("ItemSound.quest_middle");
               htmltext = "32646-11.htm";
            } else {
               htmltext = "32646-8no.htm";
            }
         } else if (event.equals("32646-14.htm")) {
            if (st.getQuestItemsCount(14871) >= 372L) {
               st.takeItems(14871, 372L);
               st.giveItems(10115, 1L);
               st.playSound("ItemSound.quest_middle");
               htmltext = "32646-11.htm";
            } else {
               htmltext = "32646-8no.htm";
            }
         } else if (event.equals("32646-15.htm")) {
            if (st.getQuestItemsCount(14871) >= 288L) {
               st.takeItems(14871, 288L);
               st.giveItems(9985, 1L);
               st.playSound("ItemSound.quest_middle");
               htmltext = "32646-11.htm";
            } else {
               htmltext = "32646-8no.htm";
            }
         } else if (event.equals("32646-16.htm")) {
            if (st.getQuestItemsCount(14871) >= 384L) {
               st.takeItems(14871, 384L);
               st.giveItems(9986, 1L);
               st.playSound("ItemSound.quest_middle");
               htmltext = "32646-11.htm";
            } else {
               htmltext = "32646-8no.htm";
            }
         } else if (event.equals("32646-17.htm")) {
            if (st.getQuestItemsCount(14871) >= 192L) {
               st.takeItems(14871, 192L);
               st.giveItems(9987, 1L);
               st.playSound("ItemSound.quest_middle");
               htmltext = "32646-11.htm";
            } else {
               htmltext = "32646-8no.htm";
            }
         } else if (event.equals("32646-18.htm")) {
            if (st.getQuestItemsCount(14871) >= 310L) {
               st.takeItems(14871, 310L);
               st.giveItems(10115, 1L);
               st.playSound("ItemSound.quest_middle");
               htmltext = "32646-11.htm";
            } else {
               htmltext = "32646-8no.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_308_ReedFieldMaintenance");
      if (st == null) {
         return htmltext;
      } else {
         QuestState st1 = player.getQuestState("_309_ForAGoodCause");
         QuestState st2 = player.getQuestState("_238_SuccesFailureOfBusiness");
         int cond = st.getInt("cond");
         if (npc.getId() == 32646) {
            if (cond == 0) {
               if (st1 != null && st1.getInt("cond") >= 1) {
                  return "32646-0b.htm";
               }

               if ((st1 == null || st1.getInt("cond") == 0) && player.getLevel() >= 82) {
                  htmltext = "32646-0.htm";
               } else {
                  htmltext = "32646-0a.htm";
                  st.exitQuest(true);
               }
            } else if (cond == 1 && st.getQuestItemsCount(14871) == 0L) {
               htmltext = "32646-3a.htm";
            } else if (cond == 1 && st.getQuestItemsCount(14871) > 0L) {
               if (st2 != null) {
                  if (st2.getState() == 2) {
                     return "32646-4a.htm";
                  }

                  htmltext = "32646-4.htm";
               } else {
                  htmltext = "32646-4.htm";
               }
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMemberState(player, (byte)1);
      if (partyMember == null) {
         return null;
      } else {
         QuestState st = partyMember.getQuestState("_308_ReedFieldMaintenance");
         if (st != null && st.getInt("cond") == 1) {
            if (isIntInArray(npc.getId(), Mucrokians)) {
               int count = 1;

               int chance;
               for(chance = (int)(5.0F * Config.RATE_QUEST_DROP); chance > 1000; ++count) {
                  chance -= 1000;
                  if (chance < 5) {
                     chance = 5;
                  }
               }

               if (getRandom(1000) <= chance) {
                  st.giveItems(14871, (long)count);
                  st.playSound("ItemSound.quest_itemget");
               }
            } else if (npc.getId() == 22654 || npc.getId() == 22655) {
               int count = 1;

               int chance;
               for(chance = (int)(5.0F * Config.RATE_QUEST_DROP); chance > 1000; ++count) {
                  chance -= 1000;
                  if (chance < 5) {
                     chance = 5;
                  }
               }

               if (getRandom(1000) <= chance) {
                  st.giveItems(14872, (long)count);
                  st.playSound("ItemSound.quest_itemget");
               }
            }

            return null;
         } else {
            return null;
         }
      }
   }

   public static void main(String[] args) {
      new _308_ReedFieldMaintenance(308, "_308_ReedFieldMaintenance", "");
   }
}
