package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.serverpackets.SocialAction;

public class _406_PathToElvenKnight extends Quest {
   private static final String qn = "_406_PathToElvenKnight";
   private static final int SORIUS = 30327;
   private static final int KLUTO = 30317;
   private static final int[] TALKERS = new int[]{30327, 30317};
   private static final int TRACKER_SKELETON = 20035;
   private static final int TRACKER_SKELETON_LEADER = 20042;
   private static final int SKELETON_SCOUT = 20045;
   private static final int SKELETON_BOWMAN = 20051;
   private static final int RUIN_SPARTOI = 20054;
   private static final int RAGING_SPARTOI = 20060;
   private static final int OL_MAHUM_NOVICE = 20782;
   private static final int[] MOBS = new int[]{20035, 20042, 20045, 20051, 20054, 20060, 20782};
   private static final int SORIUS_LETTER1 = 1202;
   private static final int KLUTO_BOX = 1203;
   private static final int TOPAZ_PIECE = 1205;
   private static final int EMERALD_PIECE = 1206;
   private static final int KLUTO_MEMO = 1276;
   private static final int[] QUESTITEMS = new int[]{1202, 1203, 1205, 1206, 1276};
   private static final int ELVEN_KNIGHT_BROOCH = 1204;

   public _406_PathToElvenKnight(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30327);

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
      QuestState st = player.getQuestState("_406_PathToElvenKnight");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30327-05.htm")) {
            if (player.getClassId().getId() != 18) {
               if (player.getClassId().getId() == 19) {
                  htmltext = "30327-02a.htm";
               } else {
                  st.exitQuest(true);
                  htmltext = "30327-02.htm";
               }
            } else if (player.getLevel() < 18) {
               st.exitQuest(true);
               htmltext = "30327-03.htm";
            } else if (st.getQuestItemsCount(1204) > 0L) {
               htmltext = "30327-04.htm";
            }
         } else if (event.equalsIgnoreCase("30327-06.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30317-02.htm")) {
            if (st.getInt("cond") == 3) {
               st.takeItems(1202, -1L);
               if (st.getQuestItemsCount(1276) == 0L) {
                  st.giveItems(1276, 1L);
                  st.set("cond", "4");
               } else {
                  htmltext = getNoQuestMsg(player);
               }
            } else {
               htmltext = getNoQuestMsg(player);
            }
         }

         return htmltext;
      }
   }

   @Override
   public final String onTalk(Npc npc, Player talker) {
      String htmltext = getNoQuestMsg(talker);
      QuestState st = talker.getQuestState("_406_PathToElvenKnight");
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int id = st.getState();
         int cond = st.getInt("cond");
         if (npcId != 30327 && id != 1) {
            return htmltext;
         } else {
            if (npcId == 30327) {
               if (cond == 0) {
                  htmltext = "30327-01.htm";
               } else if (cond == 1) {
                  htmltext = st.getQuestItemsCount(1205) == 0L ? "30327-07.htm" : "30327-08.htm";
               } else if (cond == 2) {
                  if (st.getQuestItemsCount(1202) == 0L) {
                     st.giveItems(1202, 1L);
                  }

                  st.set("cond", "3");
                  htmltext = "30327-09.htm";
               } else if (cond == 3 || cond == 4 || cond == 5) {
                  htmltext = "30327-11.htm";
               } else if (cond == 6) {
                  st.takeItems(1203, -1L);
                  String isFinished = st.getGlobalQuestVar("1ClassQuestFinished");
                  if (isFinished.equalsIgnoreCase("")) {
                     st.addExpAndSp(228064, 3520);
                  }

                  if (st.getQuestItemsCount(1204) == 0L) {
                     st.giveItems(1204, 1L);
                  }

                  st.giveItems(57, 163800L);
                  st.saveGlobalQuestVar("1ClassQuestFinished", "1");
                  st.set("cond", "0");
                  talker.sendPacket(new SocialAction(talker.getObjectId(), 3));
                  st.exitQuest(false);
                  st.playSound("ItemSound.quest_finish");
                  htmltext = "30327-10.htm";
               }
            } else if (npcId == 30317) {
               if (cond == 3) {
                  htmltext = "30317-01.htm";
               } else if (cond == 4) {
                  htmltext = st.getQuestItemsCount(1206) == 0L ? "30317-03.htm" : "30317-04.htm";
               } else if (cond == 5) {
                  st.takeItems(1206, -1L);
                  st.takeItems(1205, -1L);
                  if (st.getQuestItemsCount(1203) == 0L) {
                     st.giveItems(1203, 1L);
                  }

                  st.takeItems(1276, -1L);
                  st.set("cond", "6");
                  htmltext = "30317-05.htm";
               } else if (cond == 6) {
                  htmltext = "30317-06.htm";
               }
            }

            return htmltext;
         }
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      QuestState st = killer.getQuestState("_406_PathToElvenKnight");
      if (st == null) {
         return null;
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         if (npcId != 20782) {
            if (cond == 1 && st.getQuestItemsCount(1205) < 20L && st.getRandom(100) < 70) {
               st.giveItems(1205, 1L);
               if (st.getQuestItemsCount(1205) == 20L) {
                  st.playSound("ItemSound.quest_middle");
                  st.set("cond", "2");
               } else {
                  st.playSound("ItemSound.quest_itemget");
               }
            }
         } else if (cond == 4 && st.getQuestItemsCount(1206) < 20L && st.getRandom(100) < 50) {
            st.giveItems(1206, 1L);
            if (st.getQuestItemsCount(1206) == 20L) {
               st.playSound("ItemSound.quest_middle");
               st.set("cond", "5");
            } else {
               st.playSound("ItemSound.quest_itemget");
            }
         }

         return super.onKill(npc, killer, isSummon);
      }
   }

   public static void main(String[] args) {
      new _406_PathToElvenKnight(406, "_406_PathToElvenKnight", "");
   }
}
