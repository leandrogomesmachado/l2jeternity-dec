package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.serverpackets.SocialAction;

public class _414_PathToOrcRaider extends Quest {
   private static final String qn = "_414_PathToOrcRaider";
   private static final int KARUKIA = 30570;
   private static final int KASMAN = 30501;
   private static final int TAZEER = 31978;
   private static final int[] TALKERS = new int[]{30570, 30501, 31978};
   private static final int GOBLIN_TOMB_RAIDER_LEADER = 20320;
   private static final int KURUKA_RATMAN_LEADER = 27045;
   private static final int UMBAR_ORC = 27054;
   private static final int TIMORA_ORC = 27320;
   private static final int[] KILLS = new int[]{20320, 27045, 27054, 27320};
   private static final int GREEN_BLOOD = 1578;
   private static final int GOBLIN_DWELLING_MAP = 1579;
   private static final int KURUKA_RATMAN_TOOTH = 1580;
   private static final int BETRAYER_UMBAR_REPORT = 1589;
   private static final int HEAD_OF_BETRAYER = 1591;
   private static final int TIMORA_ORC_HEAD = 8544;
   private static final int[] QUESTITEMS = new int[]{1578, 1579, 1580, 1589, 1591, 8544};
   private static final int MARK_OF_RAIDER = 1592;

   public _414_PathToOrcRaider(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30570);

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
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return super.onAdvEvent(event, npc, player);
      } else {
         if (event.equalsIgnoreCase("30570-05.htm")) {
            st.set("id", "1");
            st.set("cond", "1");
            st.setState((byte)1);
            st.giveItems(1579, 1L);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30570-07a.htm")) {
            st.takeItems(1580, -1L);
            st.takeItems(1579, -1L);
            st.takeItems(1578, -1L);
            st.giveItems(1589, 1L);
            st.set("id", "3");
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30570-07b.htm")) {
            st.takeItems(1580, -1L);
            st.takeItems(1579, -1L);
            st.takeItems(1578, -1L);
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("31978-03.htm")) {
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player talker) {
      String htmltext = Quest.getNoQuestMsg(talker);
      QuestState st = talker.getQuestState("_414_PathToOrcRaider");
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int id = st.getState();
         if (npcId != 30570 && id != 1) {
            return htmltext;
         } else {
            int playerClassID = talker.getClassId().getId();
            int playerLvl = talker.getLevel();
            int cond = st.getInt("cond");
            if (npcId == 30570 && cond == 0) {
               if (playerLvl >= 18 && playerClassID == 44 && st.getQuestItemsCount(1592) == 0L && st.getQuestItemsCount(1579) == 0L) {
                  htmltext = "30570-01.htm";
               } else if (playerClassID != 44) {
                  htmltext = playerClassID == 45 ? "30570-02a.htm" : "30570-03.htm";
               } else if (playerLvl < 18 && playerClassID == 44) {
                  htmltext = "30570-02.htm";
               } else if (playerLvl >= 18 && playerClassID == 44 && st.getQuestItemsCount(1592) == 1L) {
                  htmltext = "30570-04.htm";
               } else {
                  htmltext = "30570-02.htm";
               }
            } else if (npcId == 30570 && cond > 0 && st.getQuestItemsCount(1579) == 1L && st.getQuestItemsCount(1580) < 10L) {
               htmltext = "30570-06.htm";
            } else if (npcId == 30570
               && cond > 0
               && st.getQuestItemsCount(1579) == 1L
               && st.getQuestItemsCount(1580) >= 10L
               && st.getQuestItemsCount(1589) == 0L) {
               htmltext = "30570-07.htm";
            } else if (npcId == 30570 && cond > 5) {
               htmltext = "30570-07b.htm";
            } else if (npcId == 30570 && cond > 0 && st.getQuestItemsCount(1589) > 0L && st.getQuestItemsCount(1591) < 2L) {
               htmltext = "30570-08.htm";
            } else if (npcId == 30570 && cond > 0 && st.getQuestItemsCount(1589) > 0L && st.getQuestItemsCount(1591) == 2L) {
               htmltext = "30570-09.htm";
            } else if (npcId == 30501 && cond > 0 && st.getQuestItemsCount(1589) > 0L && st.getQuestItemsCount(1591) == 0L) {
               htmltext = "30501-01.htm";
            } else if (npcId == 30501 && cond > 0 && st.getQuestItemsCount(1591) > 0L && st.getQuestItemsCount(1591) < 2L) {
               htmltext = "30501-02.htm";
            } else if (npcId == 30501 && cond > 0 && st.getQuestItemsCount(1591) == 2L) {
               htmltext = "30501-03.htm";
               st.takeItems(1591, -1L);
               st.takeItems(1589, -1L);
               String isFinished = st.getGlobalQuestVar("1ClassQuestFinished");
               if (isFinished.equalsIgnoreCase("")) {
                  st.addExpAndSp(295862, 2600);
               }

               st.giveItems(1592, 1L);
               st.giveItems(57, 163800L);
               st.saveGlobalQuestVar("1ClassQuestFinished", "1");
               st.unset("cond");
               talker.sendPacket(new SocialAction(talker.getObjectId(), 3));
               st.exitQuest(false);
               st.playSound("ItemSound.quest_finish");
            } else if (npcId == 31978) {
               if (cond == 5) {
                  htmltext = "31978-01.htm";
               } else if (cond == 6) {
                  htmltext = "31978-04.htm";
               } else if (cond == 7) {
                  htmltext = "31978-05.htm";
                  st.unset("cond");
                  st.takeItems(8544, -1L);
                  st.addExpAndSp(160267, 1300);
                  st.giveItems(1592, 1L);
                  talker.sendPacket(new SocialAction(talker.getObjectId(), 3));
                  st.exitQuest(false);
                  st.playSound("ItemSound.quest_finish");
               }
            }

            return htmltext;
         }
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      QuestState st = killer.getQuestState("_414_PathToOrcRaider");
      if (st == null) {
         return super.onKill(npc, killer, isSummon);
      } else if (st.getState() != 1) {
         return super.onKill(npc, killer, isSummon);
      } else {
         int cond = st.getInt("cond");
         int npcId = npc.getId();
         int x = killer.getX();
         int y = killer.getY();
         int z = killer.getZ();
         if (npcId == 20320) {
            if (cond > 0 && st.getQuestItemsCount(1579) == 1L && st.getQuestItemsCount(1580) < 10L && st.getQuestItemsCount(1578) < 40L) {
               if (st.getQuestItemsCount(1578) > 1L) {
                  if ((long)st.getRandom(100) < st.getQuestItemsCount(1578) * 10L) {
                     st.takeItems(1578, -1L);
                     st.addSpawn(27045, x, y, z);
                  } else {
                     st.giveItems(1578, 1L);
                     st.playSound("ItemSound.quest_itemget");
                  }
               } else {
                  st.giveItems(1578, 1L);
                  st.playSound("ItemSound.quest_itemget");
               }
            }
         } else if (npcId == 27045) {
            if (cond > 0 && st.getQuestItemsCount(1579) == 1L && st.getQuestItemsCount(1580) < 10L) {
               st.takeItems(1578, -1L);
               if (st.getQuestItemsCount(1580) == 9L) {
                  st.giveItems(1580, 1L);
                  st.set("id", "2");
                  st.set("cond", "2");
                  st.playSound("ItemSound.quest_middle");
               } else {
                  st.giveItems(1580, 1L);
                  st.playSound("ItemSound.quest_itemget");
               }
            }
         } else if (npcId == 27054) {
            if (cond > 0 && st.getQuestItemsCount(1589) > 0L && st.getQuestItemsCount(1591) < 2L) {
               st.giveItems(1591, 1L);
               if (st.getQuestItemsCount(1591) > 1L) {
                  st.set("id", "4");
                  st.set("cond", "4");
                  st.playSound("ItemSound.quest_middle");
               } else {
                  st.playSound("ItemSound.quest_itemget");
               }
            }
         } else if (npcId == 27320 && cond == 6) {
            st.set("cond", "7");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(8544, 1L);
         }

         return super.onKill(npc, killer, isSummon);
      }
   }

   public static void main(String[] args) {
      new _414_PathToOrcRaider(414, "_414_PathToOrcRaider", "");
   }
}
