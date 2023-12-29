package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.serverpackets.SocialAction;

public class _411_PathToAssassin extends Quest {
   private static final String qn = "_411_PathToAssassin";
   private static final int TRISKEL = 30416;
   private static final int LEIKAN = 30382;
   private static final int ARKENIA = 30419;
   private static final int[] TALKERS = new int[]{30416, 30382, 30419};
   private static final int MARSH_ZOMBIE = 20369;
   private static final int MISERY_SKELETON = 27036;
   private static final int[] KILLS = new int[]{20369, 27036};
   private static final int SHILENS_CALL = 1245;
   private static final int ARKENIAS_LETTER = 1246;
   private static final int LEIKANS_NOTE = 1247;
   private static final int ONYX_BEASTS_MOLAR = 1248;
   private static final int SHILENS_TEARS = 1250;
   private static final int ARKENIA_RECOMMEND = 1251;
   private static final int[] QUESTITEMS = new int[]{1245, 1246, 1247, 1248, 1250, 1251};
   private static final int IRON_HEART = 1252;

   public _411_PathToAssassin(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30416);

      for(int talkId : TALKERS) {
         this.addTalkId(talkId);
      }

      for(int killId : KILLS) {
         this.addKillId(killId);
      }

      this.questItemIds = QUESTITEMS;
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return super.onAdvEvent(event, npc, player);
      } else {
         int level = player.getLevel();
         int classId = player.getClassId().getId();
         if (event.equalsIgnoreCase("1")) {
            if (level >= 18 && classId == 31 && st.getQuestItemsCount(1252) == 0L) {
               st.set("cond", "1");
               st.setState((byte)1);
               st.playSound("ItemSound.quest_accept");
               st.giveItems(1245, 1L);
               htmltext = "30416-05.htm";
            } else if (classId != 31) {
               if (classId == 35) {
                  htmltext = "30416-02a.htm";
               } else {
                  st.exitQuest(true);
                  htmltext = "30416-02.htm";
               }
            } else if (level < 18 && classId == 31) {
               st.exitQuest(true);
               htmltext = "30416-03.htm";
            } else if (level >= 18 && classId == 31 && st.getQuestItemsCount(1252) == 1L) {
               htmltext = "30416-04.htm";
            }
         } else if (event.equalsIgnoreCase("30419_1")) {
            st.giveItems(1246, 1L);
            st.takeItems(1245, 1L);
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            htmltext = "30419-05.htm";
         } else if (event.equalsIgnoreCase("30382_1")) {
            st.giveItems(1247, 1L);
            st.takeItems(1246, 1L);
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
            htmltext = "30382-03.htm";
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player talker) {
      String htmltext = Quest.getNoQuestMsg(talker);
      QuestState st = talker.getQuestState("_411_PathToAssassin");
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int id = st.getState();
         if (npcId != 30416 && id != 1) {
            return htmltext;
         } else {
            if (id == 0) {
               st.set("cond", "0");
               st.set("onlyone", "0");
            }

            if (npcId == 30416 && st.getInt("cond") == 0) {
               htmltext = st.getQuestItemsCount(1252) == 0L ? "30416-01.htm" : "30416-04.htm";
            } else if (npcId == 30416 && st.getInt("cond") >= 1) {
               if (st.getQuestItemsCount(1246) == 0L
                  && st.getQuestItemsCount(1247) == 0L
                  && st.getQuestItemsCount(1250) == 0L
                  && st.getQuestItemsCount(1251) == 1L
                  && st.getQuestItemsCount(1252) == 0L) {
                  st.takeItems(1251, 1L);
                  String isFinished = st.getGlobalQuestVar("1ClassQuestFinished");
                  if (isFinished.equalsIgnoreCase("")) {
                     st.addExpAndSp(295862, 6510);
                  }

                  st.giveItems(1252, 1L);
                  st.giveItems(57, 163800L);
                  st.saveGlobalQuestVar("1ClassQuestFinished", "1");
                  st.set("cond", "0");
                  talker.sendPacket(new SocialAction(talker.getObjectId(), 3));
                  st.exitQuest(false);
                  st.playSound("ItemSound.quest_finish");
                  htmltext = "30416-06.htm";
               } else if (st.getQuestItemsCount(1246) == 1L
                  && st.getQuestItemsCount(1247) == 0L
                  && st.getQuestItemsCount(1250) == 0L
                  && st.getQuestItemsCount(1251) == 0L
                  && st.getQuestItemsCount(1252) == 0L
                  && st.getQuestItemsCount(1245) == 0L) {
                  htmltext = "30416-07.htm";
               } else if (st.getQuestItemsCount(1246) == 0L
                  && st.getQuestItemsCount(1247) == 1L
                  && st.getQuestItemsCount(1250) == 0L
                  && st.getQuestItemsCount(1251) == 0L
                  && st.getQuestItemsCount(1252) == 0L
                  && st.getQuestItemsCount(1245) == 0L) {
                  htmltext = "30416-08.htm";
               } else if (st.getQuestItemsCount(1246) == 0L
                  && st.getQuestItemsCount(1247) == 0L
                  && st.getQuestItemsCount(1250) == 0L
                  && st.getQuestItemsCount(1251) == 0L
                  && st.getQuestItemsCount(1252) == 0L
                  && st.getQuestItemsCount(1245) == 0L) {
                  htmltext = "30416-09.htm";
               } else if (st.getQuestItemsCount(1246) == 0L
                  && st.getQuestItemsCount(1247) == 0L
                  && st.getQuestItemsCount(1250) == 1L
                  && st.getQuestItemsCount(1251) == 0L
                  && st.getQuestItemsCount(1252) == 0L
                  && st.getQuestItemsCount(1245) == 0L) {
                  htmltext = "30416-10.htm";
               } else if (st.getQuestItemsCount(1246) == 0L
                  && st.getQuestItemsCount(1247) == 0L
                  && st.getQuestItemsCount(1250) == 0L
                  && st.getQuestItemsCount(1251) == 0L
                  && st.getQuestItemsCount(1252) == 0L
                  && st.getQuestItemsCount(1245) == 1L) {
                  htmltext = "30416-11.htm";
               }
            } else if (npcId == 30419 && st.getInt("cond") >= 1) {
               if (st.getQuestItemsCount(1246) == 0L
                  && st.getQuestItemsCount(1247) == 0L
                  && st.getQuestItemsCount(1250) == 0L
                  && st.getQuestItemsCount(1251) == 0L
                  && st.getQuestItemsCount(1252) == 0L
                  && st.getQuestItemsCount(1245) == 1L) {
                  htmltext = "30419-01.htm";
               } else if (st.getQuestItemsCount(1246) == 1L
                  && st.getQuestItemsCount(1247) == 0L
                  && st.getQuestItemsCount(1250) == 0L
                  && st.getQuestItemsCount(1251) == 0L
                  && st.getQuestItemsCount(1252) == 0L
                  && st.getQuestItemsCount(1245) == 0L) {
                  htmltext = "30419-07.htm";
               } else if (st.getQuestItemsCount(1246) == 0L
                  && st.getQuestItemsCount(1247) == 0L
                  && st.getQuestItemsCount(1250) == 1L
                  && st.getQuestItemsCount(1251) == 0L
                  && st.getQuestItemsCount(1252) == 0L
                  && st.getQuestItemsCount(1245) == 0L) {
                  st.giveItems(1251, 1L);
                  st.takeItems(1250, 1L);
                  st.set("cond", "7");
                  st.playSound("ItemSound.quest_middle");
                  htmltext = "30419-08.htm";
               } else if (st.getQuestItemsCount(1246) == 0L
                  && st.getQuestItemsCount(1247) == 0L
                  && st.getQuestItemsCount(1250) == 0L
                  && st.getQuestItemsCount(1251) == 1L
                  && st.getQuestItemsCount(1252) == 0L
                  && st.getQuestItemsCount(1245) == 0L) {
                  htmltext = "30419-09.htm";
               } else if (st.getQuestItemsCount(1246) == 0L
                  && st.getQuestItemsCount(1247) == 1L
                  && st.getQuestItemsCount(1250) == 0L
                  && st.getQuestItemsCount(1251) == 0L
                  && st.getQuestItemsCount(1252) == 0L
                  && st.getQuestItemsCount(1245) == 0L) {
                  htmltext = "30419-10.htm";
               } else if (st.getQuestItemsCount(1246) == 0L
                  && st.getQuestItemsCount(1247) == 0L
                  && st.getQuestItemsCount(1250) == 0L
                  && st.getQuestItemsCount(1251) == 0L
                  && st.getQuestItemsCount(1252) == 0L
                  && st.getQuestItemsCount(1245) == 0L) {
                  htmltext = "30419-11.htm";
               }
            } else if (npcId == 30382 && st.getInt("cond") >= 1) {
               if (st.getQuestItemsCount(1246) == 1L
                  && st.getQuestItemsCount(1247) == 0L
                  && st.getQuestItemsCount(1250) == 0L
                  && st.getQuestItemsCount(1251) == 0L
                  && st.getQuestItemsCount(1252) == 0L
                  && st.getQuestItemsCount(1245) == 0L
                  && st.getQuestItemsCount(1248) == 0L) {
                  htmltext = "30382-01.htm";
               } else if (st.getQuestItemsCount(1246) == 0L
                  && st.getQuestItemsCount(1247) == 1L
                  && st.getQuestItemsCount(1250) == 0L
                  && st.getQuestItemsCount(1251) == 0L
                  && st.getQuestItemsCount(1252) == 0L
                  && st.getQuestItemsCount(1245) == 0L
                  && st.getQuestItemsCount(1248) == 0L) {
                  htmltext = "30382-05.htm";
               } else if (st.getQuestItemsCount(1246) == 0L
                  && st.getQuestItemsCount(1247) == 1L
                  && st.getQuestItemsCount(1250) == 0L
                  && st.getQuestItemsCount(1251) == 0L
                  && st.getQuestItemsCount(1252) == 0L
                  && st.getQuestItemsCount(1245) == 0L
                  && st.getQuestItemsCount(1248) < 10L) {
                  htmltext = "30382-06.htm";
               } else if (st.getQuestItemsCount(1246) == 0L
                  && st.getQuestItemsCount(1247) == 1L
                  && st.getQuestItemsCount(1250) == 0L
                  && st.getQuestItemsCount(1251) == 0L
                  && st.getQuestItemsCount(1252) == 0L
                  && st.getQuestItemsCount(1245) == 0L
                  && st.getQuestItemsCount(1248) >= 10L) {
                  st.set("cond", "5");
                  st.playSound("ItemSound.quest_middle");
                  st.takeItems(1248, 10L);
                  st.takeItems(1247, 1L);
                  htmltext = "30382-07.htm";
               } else if (st.getQuestItemsCount(1250) == 1L) {
                  htmltext = "30382-08.htm";
               } else if (st.getInt("cond") >= 1
                  && st.getQuestItemsCount(1246) == 0L
                  && st.getQuestItemsCount(1247) == 0L
                  && st.getQuestItemsCount(1250) == 0L
                  && st.getQuestItemsCount(1251) == 0L
                  && st.getQuestItemsCount(1252) == 0L
                  && st.getQuestItemsCount(1245) == 0L
                  && st.getQuestItemsCount(1248) == 0L) {
                  htmltext = "30382-09.htm";
               }
            }

            return htmltext;
         }
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      QuestState st = killer.getQuestState("_411_PathToAssassin");
      if (st == null) {
         return super.onKill(npc, killer, isSummon);
      } else if (st.getState() != 1) {
         return super.onKill(npc, killer, isSummon);
      } else {
         int npcId = npc.getId();
         if (npcId == 27036) {
            if (st.getInt("cond") >= 1 && st.getQuestItemsCount(1250) == 0L) {
               st.giveItems(1250, 1L);
               st.playSound("ItemSound.quest_middle");
               st.set("cond", "6");
            }
         } else if (npcId == 20369 && st.getInt("cond") >= 1 && st.getQuestItemsCount(1247) == 1L && st.getQuestItemsCount(1248) < 10L) {
            st.giveItems(1248, 1L);
            if (st.getQuestItemsCount(1248) == 10L) {
               st.playSound("ItemSound.quest_middle");
               st.set("cond", "4");
            } else {
               st.playSound("ItemSound.quest_itemget");
            }
         }

         return super.onKill(npc, killer, isSummon);
      }
   }

   public static void main(String[] args) {
      new _411_PathToAssassin(411, "_411_PathToAssassin", "");
   }
}
