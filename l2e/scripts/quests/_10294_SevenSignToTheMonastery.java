package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.serverpackets.SocialAction;

public final class _10294_SevenSignToTheMonastery extends Quest {
   public _10294_SevenSignToTheMonastery(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32784);
      this.addTalkId(new int[]{32784, 32792, 32787, 32803, 32804, 32805, 32806, 32807, 32821, 32825, 32829, 32833});
      this.addFirstTalkId(new int[]{32822, 32823, 32824, 32826, 32827, 32828, 32830, 32831, 32832, 32834, 32835, 32836});
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         int npcId = npc.getId();
         if (npcId == 32784) {
            if (event.equalsIgnoreCase("32784-03.htm")) {
               st.startQuest();
            }
         } else if (npcId == 32792) {
            if (event.equalsIgnoreCase("32792-03.htm")) {
               st.setCond(2, true);
            } else if (event.equalsIgnoreCase("32792-08.htm")) {
               if (player.isSubClassActive()) {
                  htmltext = "32792-10.htm";
               } else {
                  st.unset("book_32821");
                  st.unset("book_32825");
                  st.unset("book_32829");
                  st.unset("book_32833");
                  st.unset("first");
                  st.unset("second");
                  st.unset("third");
                  st.unset("fourth");
                  st.unset("movie");
                  player.broadcastPacket(new SocialAction(player.getObjectId(), 3));
                  st.calcExpAndSp(this.getId());
                  st.exitQuest(false, true);
                  htmltext = "32792-08.htm";
               }
            }
         } else if (npcId == 32821) {
            if (event.equalsIgnoreCase("32821-02.htm")) {
               st.playSound("ItemSound.quest_middle");
               st.set("book_" + npc.getId(), 1);
               st.set("first", "1");
               if (this.isAllBooksFinded(st)) {
                  npc.setDisplayEffect(1);
                  player.showQuestMovie(25);
                  st.set("movie", "1");
                  return "";
               }
            }
         } else if (npcId == 32825) {
            if (event.equalsIgnoreCase("32825-02.htm")) {
               st.playSound("ItemSound.quest_middle");
               st.set("book_" + npc.getId(), 1);
               st.set("second", "1");
               if (this.isAllBooksFinded(st)) {
                  npc.setDisplayEffect(1);
                  player.showQuestMovie(25);
                  st.set("movie", "1");
                  return "";
               }
            }
         } else if (npcId == 32829) {
            if (event.equalsIgnoreCase("32829-02.htm")) {
               st.playSound("ItemSound.quest_middle");
               st.set("book_" + npc.getId(), 1);
               st.set("third", "1");
               if (this.isAllBooksFinded(st)) {
                  npc.setDisplayEffect(1);
                  player.showQuestMovie(25);
                  st.set("movie", "1");
                  return "";
               }
            }
         } else if (npcId == 32833 && event.equalsIgnoreCase("32833-02.htm")) {
            st.playSound("ItemSound.quest_middle");
            st.set("book_" + npc.getId(), 1);
            st.set("fourth", "1");
            if (this.isAllBooksFinded(st)) {
               npc.setDisplayEffect(1);
               player.showQuestMovie(25);
               st.set("movie", "1");
               return "";
            }
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
         int first = st.getInt("first");
         int second = st.getInt("second");
         int third = st.getInt("third");
         int fourth = st.getInt("fourth");
         int movie = st.getInt("movie");
         if (st.getState() == 0) {
            if (npcId == 32784) {
               QuestState qs = player.getQuestState("_10293_SevenSignsForbiddenBook");
               if (cond == 0) {
                  if (player.getLevel() >= 81 && qs != null && qs.isCompleted()) {
                     htmltext = "32784-01.htm";
                  } else {
                     htmltext = "32784-00.htm";
                     st.exitQuest(true);
                  }
               }
            }
         } else if (st.getState() == 1) {
            if (npcId == 32784) {
               if (cond == 1) {
                  htmltext = "32784-04.htm";
               }
            } else if (npcId == 32804 && cond == 2) {
               if (st.getInt("book_32821") > 0) {
                  htmltext = "32804-05.htm";
               } else {
                  htmltext = "32804-01.htm";
               }
            } else if (npcId == 32805 && cond == 2) {
               if (st.getInt("book_32825") > 0) {
                  htmltext = "32805-05.htm";
               } else {
                  htmltext = "32805-01.htm";
               }
            } else if (npcId == 32806 && cond == 2) {
               if (st.getInt("book_32829") > 0) {
                  htmltext = "32806-05.htm";
               } else {
                  htmltext = "32806-01.htm";
               }
            } else if (npcId == 32807 && cond == 2) {
               if (st.getInt("book_32833") > 0) {
                  htmltext = "32807-05.htm";
               } else {
                  htmltext = "32807-01.htm";
               }
            } else if (npcId == 32787) {
               if (cond == 1) {
                  htmltext = "32787-01.htm";
               } else if (cond == 2) {
                  htmltext = "32787-02.htm";
               } else if (cond == 3) {
                  htmltext = "32787-03.htm";
               }
            } else if (npcId == 32792) {
               if (cond == 1) {
                  htmltext = "32792-01.htm";
               } else if (cond == 2) {
                  htmltext = "32792-06.htm";
               } else if (cond == 3) {
                  htmltext = "32792-07.htm";
               }
            } else if (npcId == 32803) {
               if (cond == 2) {
                  if (this.isAllBooksFinded(st)) {
                     htmltext = "32803-04.htm";
                     st.setCond(3, true);
                  } else {
                     htmltext = "32803-01.htm";
                  }
               } else if (cond == 3) {
                  htmltext = "32803-05.htm";
               }
            } else if (npcId == 32804) {
               htmltext = "32804-01.htm";
            } else if (npcId == 32805) {
               htmltext = "32805-01.htm";
            } else if (npcId == 32806) {
               htmltext = "32806-01.htm";
            } else if (npcId == 32807) {
               htmltext = "32807-01.htm";
            } else if (npcId == 32821) {
               if (movie != 1 && first != 1) {
                  htmltext = "32821-01.htm";
               } else {
                  htmltext = "empty_desk.htm";
               }
            } else if (npcId == 32825) {
               if (movie != 1 && second != 1) {
                  htmltext = "32825-01.htm";
               } else {
                  htmltext = "empty_desk.htm";
               }
            } else if (npcId == 32829) {
               if (movie != 1 && third != 1) {
                  htmltext = "32829-01.htm";
               } else {
                  htmltext = "empty_desk.htm";
               }
            } else if (npcId == 32833) {
               if (movie != 1 && fourth != 1) {
                  htmltext = "32833-01.htm";
               } else {
                  htmltext = "empty_desk.htm";
               }
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      int npcId = npc.getId();
      return npcId != 32822
            && npcId != 32823
            && npcId != 32824
            && npcId != 32826
            && npcId != 32827
            && npcId != 32828
            && npcId != 32830
            && npcId != 32831
            && npcId != 32832
            && npcId != 32834
            && npcId != 32835
            && npcId != 32836
         ? htmltext
         : "empty_desk.htm";
   }

   private boolean isAllBooksFinded(QuestState st) {
      return st.getInt("book_32821") + st.getInt("book_32825") + st.getInt("book_32829") + st.getInt("book_32833") >= 4;
   }

   public static void main(String[] args) {
      new _10294_SevenSignToTheMonastery(10294, _10294_SevenSignToTheMonastery.class.getSimpleName(), "");
   }
}
