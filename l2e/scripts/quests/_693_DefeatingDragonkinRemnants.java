package l2e.scripts.quests;

import l2e.gameserver.model.Party;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _693_DefeatingDragonkinRemnants extends Quest {
   public _693_DefeatingDragonkinRemnants(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32527);
      this.addTalkId(32527);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32527-05.htm")) {
            st.startQuest();
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = this.getQuestState(player, true);
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= getMinLvl(this.getId()) && player.getLevel() <= getMaxLvl(this.getId())) {
                  htmltext = "32527-01.htm";
               } else {
                  htmltext = "32527-00.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               switch(st.getCond()) {
                  case 1:
                     Party party = player.getParty();
                     if (party == null) {
                        htmltext = "32527-07.htm";
                     } else if (!party.getLeader().equals(player)) {
                        htmltext = "32527-08.htm";
                     } else {
                        htmltext = "32527-06.htm";
                     }
                     break;
                  case 2:
                     if (st.getInt("timeDiff") > 0) {
                        if (this.giveReward(st, st.getInt("timeDiff"))) {
                           htmltext = "32527-13.htm";
                        } else {
                           htmltext = "32527-14.htm";
                        }

                        st.unset("timeDiff");
                        st.unset("reflectionId");
                        st.exitQuest(true, true);
                     }
               }
         }

         return htmltext;
      }
   }

   private boolean giveReward(QuestState st, int finishDiff) {
      if (finishDiff == 0) {
         return false;
      } else {
         if (finishDiff < 5) {
            switch(st.getInt("reflectionId")) {
               case 123:
                  st.calcReward(this.getId(), 1);
                  break;
               case 124:
                  st.calcReward(this.getId(), 5);
                  break;
               case 125:
                  st.calcReward(this.getId(), 9);
                  break;
               case 126:
                  st.calcReward(this.getId(), 13);
            }
         } else if (finishDiff < 10) {
            switch(st.getInt("reflectionId")) {
               case 123:
                  st.calcReward(this.getId(), 2);
                  break;
               case 124:
                  st.calcReward(this.getId(), 6);
                  break;
               case 125:
                  st.calcReward(this.getId(), 10);
                  break;
               case 126:
                  st.calcReward(this.getId(), 14);
            }
         } else if (finishDiff < 15) {
            switch(st.getInt("reflectionId")) {
               case 123:
                  st.calcReward(this.getId(), 3);
                  break;
               case 124:
                  st.calcReward(this.getId(), 7);
                  break;
               case 125:
                  st.calcReward(this.getId(), 11);
                  break;
               case 126:
                  st.calcReward(this.getId(), 15);
            }
         } else if (finishDiff < 20) {
            switch(st.getInt("reflectionId")) {
               case 123:
                  st.calcReward(this.getId(), 4);
                  break;
               case 124:
                  st.calcReward(this.getId(), 8);
                  break;
               case 125:
                  st.calcReward(this.getId(), 12);
                  break;
               case 126:
                  st.calcReward(this.getId(), 16);
            }
         }

         return true;
      }
   }

   public static void main(String[] args) {
      new _693_DefeatingDragonkinRemnants(693, _693_DefeatingDragonkinRemnants.class.getSimpleName(), "");
   }
}
