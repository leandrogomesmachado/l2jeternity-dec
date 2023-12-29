package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _903_TheCallofAntharas extends Quest {
   private static final String qn = "_903_TheCallofAntharas";
   private static final int Theodric = 30755;
   private static final int BehemothDragonLeather = 21992;
   private static final int TaraskDragonsLeatherFragment = 21991;
   private static final int TaraskDragon = 29070;
   private static final int BehemothDragon = 29069;
   private static final int[] MOBS = new int[]{29070, 29069};

   public _903_TheCallofAntharas(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30755);
      this.addTalkId(30755);

      for(int mobs : MOBS) {
         this.addKillId(mobs);
      }

      this.questItemIds = new int[]{21992, 21991};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_903_TheCallofAntharas");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30755-03.htm")) {
            st.setState((byte)1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30755-06.htm")) {
            st.takeItems(21992, -1L);
            st.takeItems(21991, -1L);
            st.giveItems(21897, 1L);
            st.setState((byte)2);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(QuestState.QuestType.DAILY);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_903_TheCallofAntharas");
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         if (npc.getId() == 30755) {
            switch(st.getState()) {
               case 0:
                  if (player.getLevel() >= 83) {
                     if (st.getQuestItemsCount(3865) > 0L) {
                        htmltext = "30755-01.htm";
                     } else {
                        htmltext = "30755-00b.htm";
                     }
                  } else {
                     htmltext = "30755-00.htm";
                     st.exitQuest(true);
                  }
                  break;
               case 1:
                  if (cond == 1) {
                     htmltext = "30755-04.htm";
                  } else if (cond == 2) {
                     htmltext = "30755-05.htm";
                  }
                  break;
               case 2:
                  if (st.isNowAvailable()) {
                     if (player.getLevel() >= 83) {
                        if (st.getQuestItemsCount(3865) > 0L) {
                           htmltext = "30755-01.htm";
                        } else {
                           htmltext = "30755-00b.htm";
                        }
                     } else {
                        htmltext = "30755-00.htm";
                        st.exitQuest(true);
                     }
                  } else {
                     htmltext = "30755-00a.htm";
                  }
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMember(player, 1);
      if (partyMember == null) {
         return super.onKill(npc, player, isSummon);
      } else {
         QuestState st = partyMember.getQuestState("_903_TheCallofAntharas");
         if (st == null) {
            return null;
         } else {
            int cond = st.getInt("cond");
            int npcId = npc.getId();
            if (cond == 1) {
               switch(npcId) {
                  case 29069:
                     if (st.getQuestItemsCount(21992) < 1L) {
                        st.giveItems(21992, 1L);
                        st.playSound("ItemSound.quest_accept");
                     }
                     break;
                  case 29070:
                     if (st.getQuestItemsCount(21991) < 1L) {
                        st.giveItems(21991, 1L);
                        st.playSound("ItemSound.quest_accept");
                     }
               }

               if (st.getQuestItemsCount(21992) > 0L && st.getQuestItemsCount(21991) > 0L) {
                  st.set("cond", "2");
                  st.playSound("ItemSound.quest_middle");
               }
            }

            if (player.getParty() != null) {
               for(Player pmember : player.getParty().getMembers()) {
                  QuestState st2 = pmember.getQuestState("_903_TheCallofAntharas");
                  if (st2 != null && cond == 1 && pmember.getObjectId() != partyMember.getObjectId()) {
                     switch(npc.getId()) {
                        case 29069:
                           if (st.getQuestItemsCount(21992) < 1L) {
                              st.giveItems(21992, 1L);
                              st.playSound("ItemSound.quest_itemget");
                           }
                           break;
                        case 29070:
                           if (st.getQuestItemsCount(21991) < 1L) {
                              st.giveItems(21991, 1L);
                              st.playSound("ItemSound.quest_itemget");
                           }
                     }

                     if (st.getQuestItemsCount(21992) > 0L && st.getQuestItemsCount(21991) > 0L) {
                        st.set("cond", "2");
                        st.playSound("ItemSound.quest_middle");
                     }
                  }
               }
            }

            return super.onKill(npc, player, isSummon);
         }
      }
   }

   public static void main(String[] args) {
      new _903_TheCallofAntharas(903, "_903_TheCallofAntharas", "");
   }
}
