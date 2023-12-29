package l2e.scripts.quests;

import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.effects.Effect;

public class _194_SevenSignContractOfMammon extends Quest {
   private static final String qn = "_194_SevenSignContractOfMammon";
   private static final int ATHEBALDT = 30760;
   private static final int COLIN = 32571;
   private static final int FROG = 32572;
   private static final int TESS = 32573;
   private static final int KUTA = 32574;
   private static final int CLAUDIA = 31001;
   private static final int INTRODUCTION = 13818;
   private static final int FROG_KING_BEAD = 13820;
   private static final int CANDY_POUCH = 13821;
   private static final int NATIVES_GLOVE = 13819;

   public _194_SevenSignContractOfMammon(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30760);
      this.addTalkId(30760);
      this.addTalkId(32571);
      this.addTalkId(32572);
      this.addTalkId(32573);
      this.addTalkId(32574);
      this.addTalkId(31001);
      this.questItemIds = new int[]{13818, 13820, 13821, 13819};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_194_SevenSignContractOfMammon");
      if (st == null) {
         return event;
      } else {
         if (npc.getId() == 30760) {
            if (event.equalsIgnoreCase("30760-02.htm")) {
               st.setState((byte)1);
               st.set("cond", "1");
               st.playSound("ItemSound.quest_accept");
            } else if (event.equalsIgnoreCase("30760-07.htm")) {
               st.set("cond", "3");
               st.giveItems(13818, 1L);
               st.playSound("ItemSound.quest_middle");
            } else if (event.equalsIgnoreCase("10")) {
               st.set("cond", "2");
               st.playSound("ItemSound.quest_middle");
               player.showQuestMovie(10);
               return "";
            }
         } else if (npc.getId() == 32571) {
            if (event.equalsIgnoreCase("32571-04.htm")) {
               st.set("cond", "4");
               st.takeItems(13818, 1L);
               this.transformPlayer(npc, player, 6201);
               st.playSound("ItemSound.quest_middle");
            }

            if (!event.equalsIgnoreCase("32571-06.htm") && !event.equalsIgnoreCase("32571-14.htm") && !event.equalsIgnoreCase("32571-22.htm")) {
               if (event.equalsIgnoreCase("32571-08.htm")) {
                  this.transformPlayer(npc, player, 6201);
               } else if (event.equalsIgnoreCase("32571-10.htm")) {
                  st.set("cond", "6");
                  st.takeItems(13820, 1L);
                  st.playSound("ItemSound.quest_middle");
               } else if (event.equalsIgnoreCase("32571-12.htm")) {
                  st.set("cond", "7");
                  this.transformPlayer(npc, player, 6202);
                  st.playSound("ItemSound.quest_middle");
               } else if (event.equalsIgnoreCase("32571-16.htm")) {
                  this.transformPlayer(npc, player, 6202);
               } else if (event.equalsIgnoreCase("32571-18.htm")) {
                  st.set("cond", "9");
                  st.takeItems(13821, 1L);
                  st.playSound("ItemSound.quest_middle");
               } else if (event.equalsIgnoreCase("32571-20.htm")) {
                  st.set("cond", "10");
                  this.transformPlayer(npc, player, 6203);
                  st.playSound("ItemSound.quest_middle");
               } else if (event.equalsIgnoreCase("32571-24.htm")) {
                  this.transformPlayer(npc, player, 6203);
               } else if (event.equalsIgnoreCase("32571-26.htm")) {
                  st.set("cond", "12");
                  st.takeItems(13819, 1L);
                  st.playSound("ItemSound.quest_middle");
               }
            } else if (player.isTransformed()) {
               player.untransform();
            }
         } else if (npc.getId() == 32572) {
            if (event.equalsIgnoreCase("32572-04.htm")) {
               st.set("cond", "5");
               st.giveItems(13820, 1L);
               st.playSound("ItemSound.quest_middle");
            }
         } else if (npc.getId() == 32573) {
            if (event.equalsIgnoreCase("32573-03.htm")) {
               st.set("cond", "8");
               st.giveItems(13821, 1L);
               st.playSound("ItemSound.quest_middle");
            }
         } else if (npc.getId() == 32574) {
            if (event.equalsIgnoreCase("32574-04.htm")) {
               st.set("cond", "11");
               st.giveItems(13819, 1L);
               st.playSound("ItemSound.quest_middle");
            }
         } else if (npc.getId() == 31001 && event.equalsIgnoreCase("31001-03.htm")) {
            st.addExpAndSp(25000000, 2500000);
            st.unset("cond");
            st.setState((byte)2);
            st.exitQuest(false);
            st.playSound("ItemSound.quest_finish");
         }

         return event;
      }
   }

   private void transformPlayer(Npc npc, Player player, int transId) {
      if (player.isTransformed()) {
         player.untransform();

         try {
            Thread.sleep(2000L);
         } catch (InterruptedException var8) {
            var8.printStackTrace();
         }
      }

      for(Effect effect : player.getAllEffects()) {
         if (effect.getSkill().getId() == 959 || effect.getSkill().getId() == 960 || effect.getSkill().getId() == 961) {
            effect.exit();
         }
      }

      npc.setTarget(player);
      npc.doCast(SkillsParser.getInstance().getInfo(transId, 1));
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_194_SevenSignContractOfMammon");
      QuestState second = player.getQuestState("_193_SevenSignDyingMessage");
      if (st == null) {
         return htmltext;
      } else {
         if (npc.getId() == 30760) {
            switch(st.getState()) {
               case 0:
                  if (second != null && second.getState() == 2 && player.getLevel() >= 79) {
                     htmltext = "30760-01.htm";
                  } else {
                     htmltext = "30760-00.htm";
                     st.exitQuest(true);
                  }
                  break;
               case 1:
                  if (st.getInt("cond") == 1) {
                     htmltext = "30760-03.htm";
                  } else if (st.getInt("cond") == 2) {
                     htmltext = "30760-05.htm";
                  } else if (st.getInt("cond") == 3) {
                     htmltext = "30760-08.htm";
                  }
                  break;
               case 2:
                  htmltext = getAlreadyCompletedMsg(player);
            }
         } else if (npc.getId() == 32571) {
            if (st.getState() == 1) {
               if (st.getInt("cond") == 3) {
                  htmltext = "32571-01.htm";
               } else if (st.getInt("cond") == 4) {
                  if (this.checkPlayer(player, 6201)) {
                     htmltext = "32571-05.htm";
                  } else {
                     htmltext = "32571-07.htm";
                  }
               } else if (st.getInt("cond") == 5) {
                  htmltext = "32571-09.htm";
               } else if (st.getInt("cond") == 6) {
                  htmltext = "32571-11.htm";
               } else if (st.getInt("cond") == 7) {
                  if (this.checkPlayer(player, 6202)) {
                     htmltext = "32571-13.htm";
                  } else {
                     htmltext = "32571-15.htm";
                  }
               } else if (st.getInt("cond") == 8) {
                  htmltext = "32571-17.htm";
               } else if (st.getInt("cond") == 9) {
                  htmltext = "32571-19.htm";
               } else if (st.getInt("cond") == 10) {
                  if (this.checkPlayer(player, 6203)) {
                     htmltext = "32571-21.htm";
                  } else {
                     htmltext = "32571-23.htm";
                  }
               } else if (st.getInt("cond") == 11) {
                  htmltext = "32571-25.htm";
               } else if (st.getInt("cond") == 12) {
                  htmltext = "32571-27.htm";
               }
            }
         } else if (npc.getId() == 32572) {
            if (this.checkPlayer(player, 6201)) {
               if (st.getInt("cond") == 4) {
                  htmltext = "32572-01.htm";
               } else if (st.getInt("cond") == 5) {
                  htmltext = "32572-05.htm";
               }
            } else {
               htmltext = "32572-00.htm";
            }
         } else if (npc.getId() == 32573) {
            if (this.checkPlayer(player, 6202)) {
               if (st.getInt("cond") == 7) {
                  htmltext = "32573-01.htm";
               } else if (st.getInt("cond") == 8) {
                  htmltext = "32573-04.htm";
               }
            } else {
               htmltext = "32573-00.htm";
            }
         } else if (npc.getId() == 32574) {
            if (this.checkPlayer(player, 6203)) {
               if (st.getInt("cond") == 10) {
                  htmltext = "32574-01.htm";
               } else if (st.getInt("cond") == 11) {
                  htmltext = "32574-05.htm";
               }
            } else {
               htmltext = "32574-00.htm";
            }
         } else if (npc.getId() == 31001 && st.getInt("cond") == 12) {
            htmltext = "31001-01.htm";
         }

         return htmltext;
      }
   }

   private boolean checkPlayer(Player player, int transId) {
      return player.getFirstEffect(transId) != null;
   }

   public static void main(String[] args) {
      new _194_SevenSignContractOfMammon(194, "_194_SevenSignContractOfMammon", "");
   }
}
