package l2e.scripts.quests;

import l2e.gameserver.Config;
import l2e.gameserver.model.CommandChannel;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _10270_BirthOfTheSeed extends Quest {
   public _10270_BirthOfTheSeed(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32563);
      this.addTalkId(32563);
      this.addTalkId(32567);
      this.addTalkId(32566);
      this.addTalkId(32559);
      this.addKillId(25666);
      this.addKillId(25665);
      this.addKillId(25634);
      this.questItemIds = new int[]{13868, 13869, 13870};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32563-05.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("32559-03.htm")) {
            st.setCond(2, true);
         } else if (event.equalsIgnoreCase("32559-09.htm")) {
            st.setCond(4, true);
         } else if (event.equalsIgnoreCase("32559-13.htm")) {
            st.calcExpAndSp(this.getId());
            st.calcReward(this.getId());
            st.exitQuest(false, true);
         } else if (event.equalsIgnoreCase("32566-05.htm")) {
            if (st.getQuestItemsCount(57) < 10000L) {
               htmltext = "32566-04a.htm";
            } else {
               st.takeItems(57, 10000L);
               st.set("pay", "1");
            }
         } else if (event.equalsIgnoreCase("32567-05.htm")) {
            st.setCond(5, true);
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
         int npcId = npc.getId();
         int cond = st.getCond();
         switch(npcId) {
            case 32559:
               if (cond == 1) {
                  htmltext = "32559-01.htm";
               } else if (cond == 2) {
                  if (st.getQuestItemsCount(13868) < 1L && st.getQuestItemsCount(13869) < 1L && st.getQuestItemsCount(13870) < 1L) {
                     htmltext = "32559-04.htm";
                  } else if (st.getQuestItemsCount(13868) + st.getQuestItemsCount(13869) + st.getQuestItemsCount(13870) < 3L) {
                     htmltext = "32559-05.htm";
                  } else if (st.getQuestItemsCount(13868) == 1L && st.getQuestItemsCount(13869) == 1L && st.getQuestItemsCount(13870) == 1L) {
                     htmltext = "32559-06.htm";
                     st.takeItems(13868, 1L);
                     st.takeItems(13869, 1L);
                     st.takeItems(13870, 1L);
                     st.setCond(3, true);
                  }
               } else if (cond == 3 || cond == 4) {
                  htmltext = "32559-07.htm";
               } else if (cond == 5) {
                  htmltext = "32559-12.htm";
               }

               if (st.getState() == 2) {
                  htmltext = "32559-02.htm";
               }
            case 32560:
            case 32561:
            case 32562:
            case 32564:
            case 32565:
            default:
               break;
            case 32563:
               switch(st.getState()) {
                  case 0:
                     if (player.getLevel() < 75) {
                        htmltext = "32563-02.htm";
                     } else {
                        htmltext = "32563-01.htm";
                     }

                     return htmltext;
                  case 1:
                     if (cond == 1) {
                        htmltext = "32563-06.htm";
                     }

                     return htmltext;
                  case 2:
                     htmltext = "32563-03.htm";
                     return htmltext;
                  default:
                     return htmltext;
               }
            case 32566:
               if (cond < 4) {
                  htmltext = "32566-02.htm";
               } else if (cond == 4) {
                  if (st.getInt("pay") == 1) {
                     htmltext = "32566-10.htm";
                  } else {
                     htmltext = "32566-04.htm";
                  }
               } else if (cond > 4) {
                  htmltext = "32566-12.htm";
               }
               break;
            case 32567:
               if (cond == 4) {
                  htmltext = "32567-01.htm";
               } else if (cond == 5) {
                  htmltext = "32567-07.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      if (npc.getId() == 25666) {
         if (player.getParty() != null) {
            Party party = player.getParty();
            if (party.getCommandChannel() != null) {
               CommandChannel cc = party.getCommandChannel();

               for(Player partyMember : cc.getMembers()) {
                  if (partyMember != null && partyMember.isInRangeZ(npc, (long)Config.ALT_PARTY_RANGE2)) {
                     QuestState st = partyMember.getQuestState(this.getName());
                     if (st != null && st.isCond(2)) {
                        st.calcDoDropItems(this.getId(), 13869, npc.getId(), 1);
                     }
                  }
               }
            } else {
               for(Player partyMember : party.getMembers()) {
                  if (partyMember != null && partyMember.isInRangeZ(npc, (long)Config.ALT_PARTY_RANGE2)) {
                     QuestState st = partyMember.getQuestState(this.getName());
                     if (st != null && st.isCond(2)) {
                        st.calcDoDropItems(this.getId(), 13869, npc.getId(), 1);
                     }
                  }
               }
            }
         } else {
            QuestState st = player.getQuestState(this.getName());
            if (st != null && st.isCond(2)) {
               st.calcDoDropItems(this.getId(), 13869, npc.getId(), 1);
            }
         }
      } else if (npc.getId() == 25665) {
         if (player.getParty() != null) {
            Party party = player.getParty();
            if (party.getCommandChannel() != null) {
               CommandChannel cc = party.getCommandChannel();

               for(Player partyMember : cc.getMembers()) {
                  if (partyMember != null && partyMember.isInRangeZ(npc, (long)Config.ALT_PARTY_RANGE2)) {
                     QuestState st = partyMember.getQuestState(this.getName());
                     if (st != null && st.isCond(2)) {
                        st.calcDoDropItems(this.getId(), 13868, npc.getId(), 1);
                     }
                  }
               }
            } else {
               for(Player partyMember : party.getMembers()) {
                  if (partyMember != null && partyMember.isInRangeZ(npc, (long)Config.ALT_PARTY_RANGE2)) {
                     QuestState st = partyMember.getQuestState(this.getName());
                     if (st != null && st.isCond(2)) {
                        st.calcDoDropItems(this.getId(), 13868, npc.getId(), 1);
                     }
                  }
               }
            }
         } else {
            QuestState st = player.getQuestState(this.getName());
            if (st != null && st.isCond(2)) {
               st.calcDoDropItems(this.getId(), 13868, npc.getId(), 1);
            }
         }
      } else if (npc.getId() == 25634) {
         if (player.getParty() != null) {
            Party party = player.getParty();
            if (party.getCommandChannel() != null) {
               CommandChannel cc = party.getCommandChannel();

               for(Player partyMember : cc.getMembers()) {
                  if (partyMember != null && partyMember.isInRangeZ(npc, (long)Config.ALT_PARTY_RANGE2)) {
                     QuestState st = partyMember.getQuestState(this.getName());
                     if (st != null && st.isCond(2)) {
                        st.calcDoDropItems(this.getId(), 13870, npc.getId(), 1);
                     }
                  }
               }
            } else {
               for(Player partyMember : party.getMembers()) {
                  if (partyMember != null && partyMember.isInRangeZ(npc, (long)Config.ALT_PARTY_RANGE2)) {
                     QuestState st = partyMember.getQuestState(this.getName());
                     if (st != null && st.isCond(2)) {
                        st.calcDoDropItems(this.getId(), 13870, npc.getId(), 1);
                     }
                  }
               }
            }
         } else {
            QuestState st = player.getQuestState(this.getName());
            if (st != null && st.isCond(2)) {
               st.calcDoDropItems(this.getId(), 13870, npc.getId(), 1);
            }
         }
      }

      return super.onKill(npc, player, isSummon);
   }

   public static void main(String[] args) {
      new _10270_BirthOfTheSeed(10270, _10270_BirthOfTheSeed.class.getSimpleName(), "");
   }
}
