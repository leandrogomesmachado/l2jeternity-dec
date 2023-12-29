package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.serverpackets.SocialAction;

public class _405_PathToCleric extends Quest {
   private static final String qn = "_405_PathToCleric";
   private static final int ZIGAUNT = 30022;
   private static final int GALLINT = 30017;
   private static final int VIVYAN = 30030;
   private static final int SIMPLON = 30253;
   private static final int PRAGA = 30333;
   private static final int LIONEL = 30408;
   private static final int[] TALKERS = new int[]{30022, 30017, 30030, 30253, 30333, 30408};
   private static final int RUIN_ZOMBIE = 20026;
   private static final int RUIN_ZOMBIE_LEADER = 20029;
   private static final int[] MOBS = new int[]{20026, 20029};
   private static final int LETTER_OF_ORDER1 = 1191;
   private static final int LETTER_OF_ORDER2 = 1192;
   private static final int BOOK_OF_LEMONIELL = 1193;
   private static final int BOOK_OF_VIVI = 1194;
   private static final int BOOK_OF_SIMLON = 1195;
   private static final int BOOK_OF_PRAGA = 1196;
   private static final int CERTIFICATE_OF_GALLINT = 1197;
   private static final int PENDANT_OF_MOTHER = 1198;
   private static final int NECKLACE_OF_MOTHER = 1199;
   private static final int LEMONIELLS_COVENANT = 1200;
   private static final int[] QUESTITEMS = new int[]{1191, 1192, 1193, 1194, 1195, 1196, 1197, 1198, 1199, 1200};
   private static final int MARK_OF_FAITH = 1201;

   public _405_PathToCleric(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30022);

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
      QuestState st = player.getQuestState("_405_PathToCleric");
      if (st == null) {
         return event;
      } else {
         int level = player.getLevel();
         int classId = player.getClassId().getId();
         if (event.equalsIgnoreCase("1")) {
            st.set("id", "0");
            if (level >= 18 && classId == 10 && st.getQuestItemsCount(1201) == 0L) {
               st.set("cond", "1");
               st.setState((byte)1);
               st.playSound("ItemSound.quest_accept");
               st.giveItems(1191, 1L);
               htmltext = "30022-05.htm";
            } else if (classId != 10) {
               htmltext = classId == 15 ? "30022-02a.htm" : "30022-02.htm";
            } else if (level < 18 && classId == 10) {
               htmltext = "30022-03.htm";
            } else if (level >= 18 && classId == 10 && st.getQuestItemsCount(1201) == 1L) {
               htmltext = "30022-04.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public final String onTalk(Npc npc, Player talker) {
      String htmltext = getNoQuestMsg(talker);
      QuestState st = talker.getQuestState("_405_PathToCleric");
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int id = st.getState();
         if (npcId != 30022 && id != 1) {
            return htmltext;
         } else {
            int cond = st.getInt("cond");
            if (npcId == 30022 && cond == 0) {
               htmltext = st.getQuestItemsCount(1201) == 0L ? "30022-01.htm" : "30022-04.htm";
            } else if (npcId == 30022 && cond > 0 && st.getQuestItemsCount(1192) == 1L && st.getQuestItemsCount(1200) == 0L) {
               htmltext = "30022-07.htm";
            } else if (npcId == 30022 && cond > 0 && st.getQuestItemsCount(1192) == 1L && st.getQuestItemsCount(1200) == 1L) {
               st.takeItems(1192, 1L);
               st.takeItems(1200, 1L);
               String isFinished = st.getGlobalQuestVar("1ClassQuestFinished");
               if (isFinished.equalsIgnoreCase("")) {
                  st.addExpAndSp(295862, 2910);
               }

               st.giveItems(1201, 1L);
               st.giveItems(57, 163800L);
               st.set("cond", "0");
               st.saveGlobalQuestVar("1ClassQuestFinished", "1");
               st.exitQuest(false);
               talker.sendPacket(new SocialAction(talker.getObjectId(), 3));
               st.playSound("ItemSound.quest_finish");
               htmltext = "30022-09.htm";
            } else if (npcId == 30022 && cond > 0 && st.getQuestItemsCount(1191) == 1L) {
               if (st.getQuestItemsCount(1194) == 1L && st.getQuestItemsCount(1195) > 0L && st.getQuestItemsCount(1196) == 1L) {
                  st.takeItems(1196, 1L);
                  st.takeItems(1194, 1L);
                  st.takeItems(1195, 3L);
                  st.takeItems(1191, 1L);
                  st.giveItems(1192, 1L);
                  st.set("cond", "3");
                  htmltext = "30022-08.htm";
               } else {
                  htmltext = "30022-06.htm";
               }
            } else if (npcId == 30253 && cond > 0 && st.getQuestItemsCount(1191) == 1L) {
               if (st.getQuestItemsCount(1195) == 0L) {
                  st.giveItems(1195, 3L);
                  htmltext = "30253-01.htm";
               } else if (st.getQuestItemsCount(1195) > 0L) {
                  htmltext = "30253-02.htm";
               }
            } else if (npcId == 30030 && cond > 0 && st.getQuestItemsCount(1191) == 1L) {
               if (st.getQuestItemsCount(1194) == 0L) {
                  st.giveItems(1194, 1L);
                  htmltext = "30030-01.htm";
               } else if (st.getQuestItemsCount(1194) == 1L) {
                  htmltext = "30030-02.htm";
               }
            } else if (npcId == 30333 && cond > 0 && st.getQuestItemsCount(1191) == 1L) {
               if (st.getQuestItemsCount(1196) == 0L && st.getQuestItemsCount(1199) == 0L) {
                  st.giveItems(1199, 1L);
                  htmltext = "30333-01.htm";
               } else if (st.getQuestItemsCount(1196) == 0L && st.getQuestItemsCount(1199) == 1L && st.getQuestItemsCount(1198) == 0L) {
                  htmltext = "30333-02.htm";
               } else if (st.getQuestItemsCount(1196) == 0L && st.getQuestItemsCount(1199) == 1L && st.getQuestItemsCount(1198) == 1L) {
                  st.takeItems(1199, 1L);
                  st.takeItems(1198, 1L);
                  st.giveItems(1196, 1L);
                  st.set("cond", "2");
                  htmltext = "30333-03.htm";
               } else if (st.getQuestItemsCount(1196) > 0L) {
                  htmltext = "30333-04.htm";
               }
            } else if (npcId == 30408 && cond > 0) {
               if (st.getQuestItemsCount(1192) == 0L) {
                  htmltext = "30408-02.htm";
               } else if (st.getQuestItemsCount(1192) == 1L
                  && st.getQuestItemsCount(1193) == 0L
                  && st.getQuestItemsCount(1200) == 0L
                  && st.getQuestItemsCount(1197) == 0L) {
                  st.giveItems(1193, 1L);
                  st.set("cond", "4");
                  htmltext = "30408-01.htm";
               } else if (st.getQuestItemsCount(1192) == 1L
                  && st.getQuestItemsCount(1193) == 1L
                  && st.getQuestItemsCount(1200) == 0L
                  && st.getQuestItemsCount(1197) == 0L) {
                  htmltext = "30408-03.htm";
               } else if (st.getQuestItemsCount(1192) == 1L
                  && st.getQuestItemsCount(1193) == 0L
                  && st.getQuestItemsCount(1200) == 0L
                  && st.getQuestItemsCount(1197) == 1L) {
                  st.takeItems(1197, 1L);
                  st.giveItems(1200, 1L);
                  st.set("cond", "6");
                  htmltext = "30408-04.htm";
               } else if (st.getQuestItemsCount(1192) == 1L
                  && st.getQuestItemsCount(1193) == 0L
                  && st.getQuestItemsCount(1200) == 1L
                  && st.getQuestItemsCount(1197) == 0L) {
                  htmltext = "30408-05.htm";
               }
            } else if (npcId == 30017 && cond > 0 && st.getQuestItemsCount(1192) == 1L && st.getQuestItemsCount(1200) == 0L) {
               if (st.getQuestItemsCount(1193) == 1L && st.getQuestItemsCount(1197) == 0L) {
                  st.takeItems(1193, 1L);
                  st.giveItems(1197, 1L);
                  st.set("cond", "5");
                  htmltext = "30017-01.htm";
               } else if (st.getQuestItemsCount(1193) == 0L && st.getQuestItemsCount(1197) == 1L) {
                  htmltext = "30017-02.htm";
               }
            }

            return htmltext;
         }
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      QuestState st = killer.getQuestState("_405_PathToCleric");
      if (st == null) {
         return null;
      } else {
         int npcId = npc.getId();
         if (npcId == 20026) {
            st.set("id", "0");
            if (st.getInt("cond") > 0 && st.getQuestItemsCount(1198) == 0L) {
               st.giveItems(1198, 1L);
               st.playSound("ItemSound.quest_middle");
            }
         } else if (npcId == 20029) {
            st.set("id", "0");
            if (st.getInt("cond") > 0 && st.getQuestItemsCount(1198) == 0L) {
               st.giveItems(1198, 1L);
               st.playSound("ItemSound.quest_middle");
            }
         }

         return super.onKill(npc, killer, isSummon);
      }
   }

   public static void main(String[] args) {
      new _405_PathToCleric(405, "_405_PathToCleric", "");
   }
}
