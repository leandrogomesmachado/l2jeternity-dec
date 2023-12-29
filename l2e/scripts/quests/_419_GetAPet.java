package l2e.scripts.quests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _419_GetAPet extends Quest {
   private static final String qn = "_419_GetAPet";
   private static final int REQUIRED_SPIDER_LEGS = 50;
   private static final int ANIMAL_LOVERS_LIST1 = 3417;
   private static final int ANIMAL_SLAYER_LIST1 = 3418;
   private static final int ANIMAL_SLAYER_LIST2 = 3419;
   private static final int ANIMAL_SLAYER_LIST3 = 3420;
   private static final int ANIMAL_SLAYER_LIST4 = 3421;
   private static final int ANIMAL_SLAYER_LIST5 = 3422;
   private static final int SPIDER_LEG1 = 3423;
   private static final int SPIDER_LEG2 = 3424;
   private static final int SPIDER_LEG3 = 3425;
   private static final int SPIDER_LEG4 = 3426;
   private static final int SPIDER_LEG5 = 3427;
   private static final int ANIMAL_SLAYER_LIST6 = 10164;
   private static final int SPIDER_LEG6 = 10165;
   private static final int[] QUESTITEMS = new int[]{3417, 3418, 3419, 3420, 3421, 3422, 3423, 3424, 3425, 3426, 3427, 10164, 10165};
   private static final int SPIDER_LEG_DROP = 100;
   private static final int SPIDER_H1 = 20103;
   private static final int SPIDER_H2 = 20106;
   private static final int SPIDER_H3 = 20108;
   private static final int SPIDER_LE1 = 20460;
   private static final int SPIDER_LE2 = 20308;
   private static final int SPIDER_LE3 = 20466;
   private static final int SPIDER_DE1 = 20025;
   private static final int SPIDER_DE2 = 20105;
   private static final int SPIDER_DE3 = 20034;
   private static final int SPIDER_O1 = 20474;
   private static final int SPIDER_O2 = 20476;
   private static final int SPIDER_O3 = 20478;
   private static final int SPIDER_D1 = 20403;
   private static final int SPIDER_D2 = 20508;
   private static final int SPIDER_K1 = 22244;
   private static final int[] TOKILL = new int[]{20103, 20106, 20108, 20460, 20308, 20466, 20025, 20105, 20034, 20474, 20476, 20478, 20403, 20508, 22244};
   private static final int PET_MANAGER_MARTIN = 30731;
   private static final int GK_BELLA = 30256;
   private static final int MC_ELLIE = 30091;
   private static final int GD_METTY = 30072;
   private static final int WOLF_COLLAR = 2375;

   public _419_GetAPet(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30731);

      for(int mobId : TOKILL) {
         this.addKillId(mobId);
      }

      this.addTalkId(30731);
      this.addTalkId(30256);
      this.addTalkId(30091);
      this.addTalkId(30072);
      this.questItemIds = QUESTITEMS;
   }

   private long getCountOfProofs(QuestState st) {
      int race = st.getPlayer().getRace().ordinal();
      long proofs = 0L;
      switch(race) {
         case 0:
            proofs = st.getQuestItemsCount(3423);
            break;
         case 1:
            proofs = st.getQuestItemsCount(3424);
            break;
         case 2:
            proofs = st.getQuestItemsCount(3425);
            break;
         case 3:
            proofs = st.getQuestItemsCount(3426);
            break;
         case 4:
            proofs = st.getQuestItemsCount(3427);
            break;
         case 5:
            proofs = st.getQuestItemsCount(10165);
      }

      return proofs;
   }

   private String checkQuestions(QuestState st) {
      String htmltext = null;
      int question = 1;
      String quiz = st.get("quiz");
      int answers = st.getInt("answers");
      if (answers < 10) {
         List<String> temp = Arrays.asList(quiz.split("\\s"));
         List<String> questions = new ArrayList<>(temp);
         int index = st.getRandom(questions.size());
         question = Integer.parseInt(questions.get(index));
         questions.remove(index);
         String questionsF = "";
         Iterator<String> i = questions.iterator();

         while(i.hasNext()) {
            questionsF = questionsF + (i.hasNext() ? (String)i.next() + " " : (String)i.next());
         }

         st.set("quiz", questionsF);
         htmltext = "419_q" + String.valueOf(question) + ".htm";
      } else if (answers == 10) {
         st.giveItems(2375, 1L);
         st.takeItems(3417, -1L);
         st.exitQuest(true);
         st.playSound("ItemSound.quest_finish");
         htmltext = "Completed.htm";
      }

      return htmltext;
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_419_GetAPet");
      if (st == null) {
         return null;
      } else {
         String htmltext = null;
         int state = st.getState();
         int progress = st.getInt("progress");
         if (state == 0) {
            st.set("cond", "0");
            if (event.equalsIgnoreCase("details")) {
               htmltext = "419_confirm.htm";
            } else if (event.equalsIgnoreCase("agree")) {
               st.setState((byte)1);
               st.set("step", "STARTED");
               st.set("cond", "1");
               int race = player.getRace().ordinal();
               if (race == 0) {
                  st.giveItems(3418, 1L);
                  htmltext = "419_slay_0.htm";
               } else if (race == 1) {
                  st.giveItems(3419, 1L);
                  htmltext = "419_slay_1.htm";
               } else if (race == 2) {
                  st.giveItems(3420, 1L);
                  htmltext = "419_slay_2.htm";
               } else if (race == 3) {
                  st.giveItems(3421, 1L);
                  htmltext = "419_slay_3.htm";
               } else if (race == 4) {
                  st.giveItems(3422, 1L);
                  htmltext = "419_slay_4.htm";
               } else if (race == 5) {
                  st.giveItems(10164, 1L);
                  htmltext = "419_slay_5.htm";
               } else {
                  htmltext = "Error: unknown race...";
                  st.exitQuest(true);
                  st.playSound("ItemSound.quest_accept");
               }
            } else if (event.equalsIgnoreCase("disagree")) {
               st.exitQuest(true);
               htmltext = "419_cancelled.htm";
            }
         } else if (state == 1 && progress == 7) {
            if (event.equalsIgnoreCase("tryme")) {
               st.set("quiz", "1 2 3 4 5 6 7 8 9 10 11 12 13 14 15");
               st.set("answers", "0");
               htmltext = this.checkQuestions(st);
            } else if (event.equalsIgnoreCase("wrong")) {
               st.set("step", "SLAYED");
               st.set("progress", "0");
               st.unset("quiz");
               st.unset("answers");
               htmltext = "419_failed.htm";
            } else if (event.equalsIgnoreCase("right")) {
               st.set("answers", String.valueOf(st.getInt("answers") + 1));
               htmltext = this.checkQuestions(st);
            }
         } else if (state == 1 && st.get("step").equalsIgnoreCase("SLAYED")) {
            if (event.equalsIgnoreCase("talk")) {
               st.set("progress", "0");
               int race = player.getRace().ordinal();
               if (race == 0) {
                  st.takeItems(3423, 50L);
                  st.takeItems(3418, 1L);
               } else if (race == 1) {
                  st.takeItems(3424, 50L);
                  st.takeItems(3419, 1L);
               } else if (race == 2) {
                  st.takeItems(3425, 50L);
                  st.takeItems(3420, 1L);
               } else if (race == 3) {
                  st.takeItems(3426, 50L);
                  st.takeItems(3421, 1L);
               } else if (race == 4) {
                  st.takeItems(3427, 50L);
                  st.takeItems(3422, 1L);
               } else if (race == 5) {
                  st.takeItems(10165, 50L);
                  st.takeItems(10164, 1L);
               }

               st.giveItems(3417, 1L);
               htmltext = "419_talk.htm";
            } else if (event.equalsIgnoreCase("talk1")) {
               htmltext = "419_bella_2.htm";
            } else if (event.equalsIgnoreCase("talk2")) {
               st.set("progress", String.valueOf(st.getInt("progress") | 1));
               htmltext = "419_bella_3.htm";
            } else if (event.equalsIgnoreCase("talk3")) {
               st.set("progress", String.valueOf(st.getInt("progress") | 2));
               htmltext = "419_ellie_2.htm";
            } else if (event.equalsIgnoreCase("talk4")) {
               st.set("progress", String.valueOf(st.getInt("progress") | 4));
               htmltext = "419_metty_2.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player talker) {
      String htmltext = Quest.getNoQuestMsg(talker);
      QuestState st = talker.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int state = st.getState();
         String step = st.get("step");
         if (npcId != 30731 && state == 1 && !step.equalsIgnoreCase("SLAYED")) {
            return htmltext;
         } else {
            if (state == 2) {
               st.setState((byte)0);
            }

            if (npcId == 30731) {
               if (state == 0) {
                  if (talker.getLevel() < 15) {
                     st.exitQuest(true);
                     htmltext = "419_low_level.htm";
                  } else {
                     htmltext = "Start.htm";
                  }
               }

               if (state == 1 && step.equalsIgnoreCase("STARTED")) {
                  long proofs = this.getCountOfProofs(st);
                  if (proofs == 0L) {
                     htmltext = "419_no_slay.htm";
                  } else if (proofs < 50L) {
                     htmltext = "419_pending_slay.htm";
                  } else {
                     st.set("step", "SLAYED");
                     htmltext = "Slayed.htm";
                  }
               }

               if (state == 1 && step.equalsIgnoreCase("SLAYED")) {
                  int progress = st.getInt("progress");
                  htmltext = progress == 7 ? "Talked.htm" : "419_pending_talk.htm";
               }
            } else if (state == 1 && step.equalsIgnoreCase("SLAYED")) {
               if (npcId == 30256) {
                  htmltext = "419_bella_1.htm";
               } else if (npcId == 30091) {
                  htmltext = "419_ellie_1.htm";
               } else if (npcId == 30072) {
                  htmltext = "419_metty_1.htm";
               }
            }

            return htmltext;
         }
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      QuestState st = killer.getQuestState(this.getName());
      if (st == null) {
         return super.onKill(npc, killer, isSummon);
      } else {
         int state = st.getState();
         if (state != 1) {
            return super.onKill(npc, killer, isSummon);
         } else {
            int npcId = npc.getId();
            long collected = this.getCountOfProofs(st);
            if (collected < 50L) {
               List<Integer> npcs = new ArrayList<>();
               int item = 0;
               int race = killer.getRace().ordinal();
               switch(race) {
                  case 0:
                     npcs.add(20103);
                     npcs.add(20106);
                     npcs.add(20108);
                     item = 3423;
                     break;
                  case 1:
                     npcs.add(20460);
                     npcs.add(20308);
                     npcs.add(20466);
                     item = 3424;
                     break;
                  case 2:
                     npcs.add(20025);
                     npcs.add(20105);
                     npcs.add(20034);
                     item = 3425;
                     break;
                  case 3:
                     npcs.add(20474);
                     npcs.add(20476);
                     npcs.add(20478);
                     item = 3426;
                     break;
                  case 4:
                     npcs.add(20403);
                     npcs.add(20508);
                     item = 3427;
                     break;
                  case 5:
                     npcs.add(22244);
                     item = 10165;
               }

               if (npcs.contains(npcId)) {
                  st.dropQuestItems(item, 1, 1, 50L, false, 100.0F, true);
               }
            }

            return super.onKill(npc, killer, isSummon);
         }
      }
   }

   public static void main(String[] args) {
      new _419_GetAPet(419, "_419_GetAPet", "");
   }
}
