package l2e.scripts.quests;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.serverpackets.SocialAction;

public class _215_TrialOfPilgrim extends Quest {
   private static final String qn = "_215_TrialOfPilgrim";
   private static final int SANTIAGO = 30648;
   private static final int PETRON = 30036;
   private static final int PRIMOS = 30117;
   private static final int ANDELLIA = 30362;
   private static final int GAURI_TWINKLEROCK = 30550;
   private static final int TANAPI = 30571;
   private static final int CASIAN = 30612;
   private static final int ANCESTOR_MARTANKUS = 30649;
   private static final int GERALD = 30650;
   private static final int DORF = 30651;
   private static final int URUHA = 30652;
   private static final int[] TALKERS = new int[]{30648, 30036, 30117, 30362, 30550, 30571, 30612, 30649, 30650, 30651, 30652};
   private static final int LAVA_SALAMANDER = 27116;
   private static final int NAHIR = 27117;
   private static final int BLACK_WILLOW = 27118;
   private static final int[] MOBS = new int[]{27116, 27117, 27118};
   private static final int BOOK_OF_SAGE = 2722;
   private static final int VOUCHER_OF_TRIAL = 2723;
   private static final int SPIRIT_OF_FLAME = 2724;
   private static final int ESSENSE_OF_FLAME = 2725;
   private static final int BOOK_OF_GERALD = 2726;
   private static final int GREY_BADGE = 2727;
   private static final int PICTURE_OF_NAHIR = 2728;
   private static final int HAIR_OF_NAHIR = 2729;
   private static final int STATUE_OF_EINHASAD = 2730;
   private static final int BOOK_OF_DARKNESS = 2731;
   private static final int DEBRIS_OF_WILLOW = 2732;
   private static final int TAG_OF_RUMOR = 2733;
   private static final int[] QUESTITEMS = new int[]{2722, 2723, 2724, 2725, 2726, 2727, 2728, 2729, 2730, 2731, 2732, 2733};
   private static final int MARK_OF_PILGRIM = 2721;
   private static final int[] CLASSES = new int[]{15, 29, 42, 50};

   public _215_TrialOfPilgrim(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30648);

      for(int talkId : TALKERS) {
         this.addTalkId(talkId);
      }

      for(int mobId : MOBS) {
         this.addKillId(mobId);
      }

      this.questItemIds = QUESTITEMS;
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_215_TrialOfPilgrim");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("1")) {
            htmltext = "30648-04.htm";
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
            st.giveItems(2723, 1L);
         } else if (event.equalsIgnoreCase("30648_1")) {
            htmltext = "30648-05.htm";
         } else if (event.equalsIgnoreCase("30648_2")) {
            htmltext = "30648-06.htm";
         } else if (event.equalsIgnoreCase("30648_3")) {
            htmltext = "30648-07.htm";
         } else if (event.equalsIgnoreCase("30648_4")) {
            htmltext = "30648-08.htm";
         } else if (event.equalsIgnoreCase("30648_5")) {
            htmltext = "30648-05.htm";
         } else if (event.equalsIgnoreCase("30649_1")) {
            htmltext = "30649-04.htm";
            st.giveItems(2724, 1L);
            st.takeItems(2725, 1L);
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30650_1")) {
            if (st.getQuestItemsCount(57) >= 100000L) {
               htmltext = "30650-02.htm";
               st.giveItems(2726, 1L);
               st.takeItems(57, 100000L);
               st.set("cond", "8");
               st.playSound("ItemSound.quest_middle");
            } else {
               htmltext = "30650-03.htm";
            }
         } else if (event.equalsIgnoreCase("30650_2")) {
            htmltext = "30650-03.htm";
         } else if (event.equalsIgnoreCase("30362_1")) {
            htmltext = "30362-05.htm";
            st.takeItems(2731, 1L);
            st.set("cond", "16");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30362_2")) {
            htmltext = "30362-04.htm";
            st.set("cond", "16");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30652_1")) {
            htmltext = "30652-02.htm";
            st.giveItems(2731, 1L);
            st.takeItems(2732, 1L);
            st.set("cond", "15");
            st.playSound("ItemSound.quest_middle");
         }

         return htmltext;
      }
   }

   @Override
   public final String onTalk(Npc npc, Player talker) {
      String htmltext = getNoQuestMsg(talker);
      QuestState st = talker.getQuestState("_215_TrialOfPilgrim");
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int id = st.getState();
         if (npcId != 30648 && id != 1) {
            return htmltext;
         } else {
            int cond = st.getInt("cond");
            if (npcId == 30648 && cond == 0 && id == 0) {
               if (Util.contains(CLASSES, talker.getClassId().getId())) {
                  if (talker.getLevel() >= 35) {
                     htmltext = "30648-03.htm";
                  } else {
                     htmltext = "30648-01.htm";
                     st.exitQuest(true);
                  }
               } else {
                  htmltext = "30648-02.htm";
                  st.exitQuest(true);
               }
            } else if (npcId == 30648 && cond == 0 && id == 2) {
               htmltext = Quest.getAlreadyCompletedMsg(talker);
            } else if (npcId == 30648 && cond == 1 && st.getQuestItemsCount(2723) > 0L) {
               htmltext = "30648-09.htm";
            } else if (npcId == 30648 && cond == 17 && st.getQuestItemsCount(2722) > 0L) {
               htmltext = "30648-10.htm";
               st.unset("cond");
               st.takeItems(2722, 1L);
               st.addExpAndSp(629125, 40803);
               st.giveItems(57, 114649L);
               if (talker.getVarInt("2ND_CLASS_DIAMOND_REWARD", 0) == 0) {
                  st.giveItems(7562, 49L);
                  talker.setVar("2ND_CLASS_DIAMOND_REWARD", 1);
               }

               st.giveItems(2721, 1L);
               talker.sendPacket(new SocialAction(talker.getObjectId(), 3));
               st.exitQuest(false);
               st.playSound("ItemSound.quest_finish");
            } else if (npcId == 30571 && cond == 1 && st.getQuestItemsCount(2723) > 0L) {
               htmltext = "30571-01.htm";
               st.takeItems(2723, 1L);
               st.set("cond", "2");
               st.playSound("ItemSound.quest_middle");
            } else if (npcId == 30571 && cond == 2) {
               htmltext = "30571-02.htm";
            } else if (npcId == 30571 && (cond == 5 || cond == 6) && st.getQuestItemsCount(2724) > 0L) {
               htmltext = "30571-03.htm";
               st.set("cond", "6");
               st.playSound("ItemSound.quest_middle");
            } else if (npcId == 30649 && cond == 2) {
               htmltext = "30649-01.htm";
               st.set("cond", "3");
               st.playSound("ItemSound.quest_middle");
            } else if (npcId == 30649 && cond == 3) {
               htmltext = "30649-02.htm";
            } else if (npcId == 30649 && cond == 4 && st.getQuestItemsCount(2725) > 0L) {
               htmltext = "30649-03.htm";
            } else if (npcId == 30550 && cond == 6 && st.getQuestItemsCount(2724) > 0L) {
               htmltext = "30550-01.htm";
               st.giveItems(2733, 1L);
               st.set("cond", "7");
               st.playSound("ItemSound.quest_middle");
            } else if (npcId == 30550 && cond == 7) {
               htmltext = "30550-02.htm";
            } else if (npcId == 30650 && cond == 7 && st.getQuestItemsCount(2733) > 0L) {
               htmltext = st.showHtmlFile("30650-01.htm").replace("RequiredAdena", String.valueOf(100000));
            } else if (npcId == 30650 && cond >= 9 && st.getQuestItemsCount(2727) > 0L && st.getQuestItemsCount(2726) > 0L) {
               htmltext = "30650-04.htm";
               st.giveItems(57, 100000L);
               st.takeItems(2726, 1L);
            } else if (npcId == 30651 && cond == 7 && st.getQuestItemsCount(2733) > 0L) {
               htmltext = "30651-01.htm";
               st.giveItems(2727, 1L);
               st.takeItems(2733, 1L);
               st.set("cond", "9");
               st.playSound("ItemSound.quest_middle");
            } else if (npcId == 30651 && cond == 8 && st.getQuestItemsCount(2733) > 0L) {
               htmltext = "30651-02.htm";
               st.giveItems(2727, 1L);
               st.takeItems(2733, 1L);
               st.set("cond", "9");
               st.playSound("ItemSound.quest_middle");
            } else if (npcId == 30651 && cond == 9) {
               htmltext = "30651-03.htm";
            } else if (npcId == 30117 && cond == 8) {
               htmltext = "30117-01.htm";
               st.set("cond", "9");
               st.playSound("ItemSound.quest_middle");
            } else if (npcId == 30117 && cond == 9) {
               htmltext = "30117-02.htm";
            } else if (npcId == 30036 && cond == 9) {
               htmltext = "30036-01.htm";
               st.giveItems(2728, 1L);
               st.set("cond", "10");
               st.playSound("ItemSound.quest_middle");
            } else if (npcId == 30036 && cond == 10) {
               htmltext = "30036-02.htm";
            } else if (npcId == 30036 && cond == 11) {
               htmltext = "30036-03.htm";
               st.giveItems(2730, 1L);
               st.takeItems(2728, 1L);
               st.takeItems(2729, 1L);
               st.set("cond", "12");
               st.playSound("ItemSound.quest_middle");
            } else if (npcId == 30036 && cond == 12 && st.getQuestItemsCount(2730) > 0L) {
               htmltext = "30036-04.htm";
            } else if (npcId == 30362 && cond == 12) {
               htmltext = "30362-01.htm";
               st.set("cond", "13");
               st.playSound("ItemSound.quest_middle");
            } else if (npcId == 30362 && cond == 13) {
               htmltext = "30362-02.htm";
            } else if (npcId == 30362 && cond == 15 && st.getQuestItemsCount(2731) > 0L) {
               htmltext = "30362-03.htm";
            } else if (npcId == 30362 && cond == 16) {
               htmltext = "30362-06.htm";
            } else if (npcId == 30362 && cond == 15 && st.getQuestItemsCount(2731) == 0L) {
               htmltext = "30362-07.htm";
            } else if (npcId == 30652 && cond == 14 && st.getQuestItemsCount(2732) > 0L) {
               htmltext = "30652-01.htm";
            } else if (npcId == 30652 && cond == 15 && st.getQuestItemsCount(2731) > 0L) {
               htmltext = "30652-03.htm";
            } else if (npcId == 30612 && cond == 16) {
               htmltext = "30612-01.htm";
               st.giveItems(2722, 1L);
               if (st.getQuestItemsCount(2731) > 0L) {
                  st.takeItems(2731, 1L);
               }

               st.set("cond", "17");
               st.playSound("ItemSound.quest_middle");
               st.takeItems(2727, 1L);
               st.takeItems(2724, 1L);
               st.takeItems(2730, 1L);
            } else if (npcId == 30612 && cond == 17) {
               htmltext = "30612-02.htm";
            }

            return htmltext;
         }
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      QuestState st = killer.getQuestState("_215_TrialOfPilgrim");
      if (st == null) {
         return null;
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         if (npcId == 27116) {
            if (cond == 3 && st.getQuestItemsCount(2725) == 0L && st.getRandom(5) == 0) {
               st.giveItems(2725, 1L);
               st.set("cond", "4");
               st.playSound("ItemSound.quest_middle");
            }
         } else if (npcId == 27117) {
            if (cond == 10 && st.getQuestItemsCount(2729) == 0L) {
               st.giveItems(2729, 1L);
               st.set("cond", "11");
               st.playSound("ItemSound.quest_middle");
            }
         } else if (npcId == 27118 && cond == 13 && st.getQuestItemsCount(2732) == 0L && st.getRandom(5) == 0) {
            st.giveItems(2732, 1L);
            st.set("cond", "14");
            st.playSound("ItemSound.quest_middle");
         }

         return super.onKill(npc, killer, isSummon);
      }
   }

   public static void main(String[] args) {
      new _215_TrialOfPilgrim(215, "_215_TrialOfPilgrim", "");
   }
}
