package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;

public class _101_SwordOfSolidarity extends Quest {
   public _101_SwordOfSolidarity(int id, String name, String desc) {
      super(id, name, desc);
      this.addStartNpc(30008);
      this.addTalkId(30008);
      this.addTalkId(30283);
      this.addKillId(20361);
      this.addKillId(20362);
      this.questItemIds = new int[]{796, 937, 739, 740, 741, 742};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30008-04.htm")) {
            st.startQuest();
            st.giveItems(796, 1L);
         } else if (event.equalsIgnoreCase("30283-02.htm")) {
            st.takeItems(796, st.getQuestItemsCount(796));
            st.giveItems(937, 1L);
            st.setCond(2, true);
         } else if (event.equalsIgnoreCase("30283-07.htm")) {
            st.takeItems(739, -1L);
            st.calcExpAndSp(this.getId());
            st.calcReward(this.getId());
            st.exitQuest(false, true);
            if (!player.getClassId().isMage()) {
               st.playTutorialVoice("tutorial_voice_027");
               st.giveItems(5790, 3500L);
            } else {
               st.playTutorialVoice("tutorial_voice_026");
               st.giveItems(5789, 7000L);
            }

            showOnScreenMsg(player, NpcStringId.ACQUISITION_OF_RACE_SPECIFIC_WEAPON_COMPLETE_N_GO_FIND_THE_NEWBIE_GUIDE, 2, 5000, new String[0]);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int id = st.getState();
         switch(id) {
            case 0:
               if (npcId == 30008) {
                  if (player.getRace().ordinal() != 0) {
                     htmltext = "30008-00.htm";
                  } else if (player.getLevel() >= 9) {
                     htmltext = "30008-02.htm";
                  } else {
                     htmltext = "30008-08.htm";
                     st.exitQuest(true);
                  }
               }
               break;
            case 1:
               if (npcId == 30008) {
                  if (st.isCond(1) && st.getQuestItemsCount(796) == 1L) {
                     htmltext = "30008-05.htm";
                  } else if (st.getCond() >= 2 && st.getQuestItemsCount(796) == 0L && st.getQuestItemsCount(742) == 0L) {
                     if (st.getQuestItemsCount(741) > 0L && st.getQuestItemsCount(740) > 0L) {
                        htmltext = "30008-12.htm";
                     } else if (st.getQuestItemsCount(741) + st.getQuestItemsCount(740) <= 1L) {
                        htmltext = "30008-11.htm";
                     } else if (st.getQuestItemsCount(739) > 0L) {
                        htmltext = "30008-07.htm";
                     } else if (st.getQuestItemsCount(937) == 1L) {
                        htmltext = "30008-10.htm";
                     }
                  } else if (st.isCond(4) && st.getQuestItemsCount(796) == 0L && st.getQuestItemsCount(742) > 0L) {
                     htmltext = "30008-06.htm";
                     st.takeItems(742, st.getQuestItemsCount(742));
                     st.giveItems(739, 1L);
                     st.setCond(5, true);
                  }
               } else if (npcId == 30283) {
                  if (st.isCond(1) && st.getQuestItemsCount(796) > 0L) {
                     htmltext = "30283-01.htm";
                  } else if (st.getCond() >= 2 && st.getQuestItemsCount(796) == 0L && st.getQuestItemsCount(937) > 0L) {
                     if (st.getQuestItemsCount(741) + st.getQuestItemsCount(740) == 1L) {
                        htmltext = "30283-08.htm";
                     } else if (st.getQuestItemsCount(741) + st.getQuestItemsCount(740) == 0L) {
                        htmltext = "30283-03.htm";
                     } else if (st.getQuestItemsCount(741) > 0L && st.getQuestItemsCount(740) > 0L) {
                        htmltext = "30283-04.htm";
                        st.takeItems(937, st.getQuestItemsCount(937));
                        st.takeItems(741, st.getQuestItemsCount(741));
                        st.takeItems(740, st.getQuestItemsCount(740));
                        st.giveItems(742, 1L);
                        st.setCond(4, true);
                     }
                  } else if (st.getInt("cond") == 4 && st.getQuestItemsCount(742) > 0L) {
                     htmltext = "30283-05.htm";
                  } else if (st.getInt("cond") == 5 && st.getQuestItemsCount(739) > 0L) {
                     htmltext = "30283-06.htm";
                  }
               }
               break;
            case 2:
               htmltext = getAlreadyCompletedMsg(player);
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return null;
      } else {
         if (npc.getId() == 20361 || npc.getId() == 20362) {
            if (st.getQuestItemsCount(937) > 0L) {
               st.calcDoDropItems(this.getId(), 741, npc.getId(), 1);
               st.calcDoDropItems(this.getId(), 740, npc.getId(), 1);
            }

            if (st.isCond(2) && st.getQuestItemsCount(741) > 0L && st.getQuestItemsCount(740) > 0L) {
               st.setCond(3, true);
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _101_SwordOfSolidarity(101, _101_SwordOfSolidarity.class.getSimpleName(), "");
   }
}
