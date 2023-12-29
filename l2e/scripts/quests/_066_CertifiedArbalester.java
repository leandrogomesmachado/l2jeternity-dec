package l2e.scripts.quests;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public final class _066_CertifiedArbalester extends Quest {
   private static final int[] _floranMobs = new int[]{21102, 21103, 21104, 21105, 21106, 21107, 21108, 20781};
   private static final int[] _egMobs = new int[]{20199, 20200, 20201, 20202, 20203, 20083, 20144};
   private static int _killsAmount;

   public _066_CertifiedArbalester(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32201);
      this.addTalkId(new int[]{32201, 30464, 30458, 30058, 32214, 32220, 30171, 30717, 30720});
      this.addKillId(
         new int[]{21102, 21103, 21104, 21105, 21106, 21107, 21108, 20781, 20199, 20200, 20201, 20202, 20203, 20083, 20144, 20584, 20585, 20554, 20563, 27336}
      );
      _killsAmount = this.getQuestParams(questId).getInteger("timakAmount");
      this.questItemIds = new int[]{9773, 9774, 9775, 9776, 9777, 9778, 9779, 9780, 9781};
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32201-02.htm")) {
            st.startQuest();
            st.set("id", "0");
            if (player.getVarInt("2ND_CLASS_DIAMOND_REWARD", 0) == 0) {
               giveItems(player, 7562, 64L);
               player.setVar("2ND_CLASS_DIAMOND_REWARD", 1);
            }
         } else if (event.equalsIgnoreCase("32201-03.htm")) {
            st.setCond(2, true);
         } else if (event.equalsIgnoreCase("30464-05.htm")) {
            st.setCond(3, true);
         } else if (event.equalsIgnoreCase("30464-08.htm")) {
            st.takeItems(9773, -1L);
         } else if (event.equalsIgnoreCase("30464-09.htm")) {
            st.giveItems(9774, 1L);
            st.setCond(5, true);
         } else if (event.equalsIgnoreCase("30458-03.htm")) {
            st.takeItems(9774, -1L);
         } else if (event.equalsIgnoreCase("30458-07.htm")) {
            st.setCond(6, true);
         } else if (event.equalsIgnoreCase("30058-04.htm")) {
            st.setCond(7, true);
         } else if (event.equalsIgnoreCase("30058-07.htm")) {
            st.setCond(9, true);
            st.giveItems(9776, 1L);
         } else if (event.equalsIgnoreCase("32214-03.htm")) {
            st.setCond(10, true);
            st.takeItems(9776, -1L);
            st.giveItems(9777, 1L);
         } else if (event.equalsIgnoreCase("32220-11.htm")) {
            st.setCond(11, true);
         } else if (event.equalsIgnoreCase("30171-02.htm")) {
            st.takeItems(9779, -1L);
         } else if (event.equalsIgnoreCase("30171-05.htm")) {
            st.setCond(14, true);
         } else if (event.equalsIgnoreCase("30717-02.htm")) {
            st.takeItems(9780, -1L);
         } else if (event.equalsIgnoreCase("30717-07.htm")) {
            st.setCond(17, true);
         } else if (event.equalsIgnoreCase("30720-03.htm")) {
            st.setCond(18, true);
         } else if (event.equalsIgnoreCase("32220-19.htm")) {
            st.setCond(19, true);
         }

         return event;
      }
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int cond = st.getCond();
         if (st.getState() == 2) {
            htmltext = getAlreadyCompletedMsg(player);
         } else if (npcId == 32201) {
            if (player.getClassId().getId() != 126 || player.getLevel() < 39) {
               htmltext = "32201-00.htm";
               st.exitQuest(true);
            } else if (st.getState() == 0) {
               htmltext = "32201-01.htm";
            } else if (cond == 1) {
               htmltext = "32201-03.htm";
            } else if (cond == 2) {
               htmltext = "32201-04.htm";
            }
         } else if (npcId == 30464) {
            if (cond == 2) {
               htmltext = "30464-01.htm";
            } else if (cond == 3) {
               htmltext = "30464-06.htm";
            } else if (cond == 4) {
               htmltext = "30464-07.htm";
            } else if (cond == 5) {
               htmltext = "30464-09.htm";
            }
         } else if (npcId == 30458) {
            if (cond == 5) {
               htmltext = "30458-01.htm";
            } else if (cond == 6) {
               htmltext = "30458-08.htm";
            }
         } else if (npcId == 30058) {
            if (cond == 6) {
               htmltext = "30058-01.htm";
            } else if (cond == 7) {
               htmltext = "30058-05.htm";
            } else if (cond == 8) {
               htmltext = "30058-06.htm";
               st.takeItems(9775, -1L);
            } else if (cond == 9) {
               htmltext = "30058-08.htm";
            }
         } else if (npcId == 32214) {
            if (cond == 9) {
               htmltext = "32214-01.htm";
            } else if (cond == 10) {
               htmltext = "32214-04.htm";
            }
         } else if (npcId == 32220) {
            if (cond == 10) {
               htmltext = "32220-01.htm";
            } else if (cond == 11) {
               htmltext = "32220-11.htm";
            } else if (cond == 18) {
               htmltext = "32220-12.htm";
            } else if (cond == 19) {
               htmltext = "32220-19.htm";
            } else if (cond == 20) {
               htmltext = "32220-20.htm";
               st.takeItems(9781, -1L);
               st.calcExpAndSp(this.getId());
               st.calcReward(this.getId());
               st.exitQuest(false, true);
            }
         } else if (npcId == 30171) {
            if (cond == 13) {
               htmltext = "30171-01.htm";
            } else if (cond == 14) {
               htmltext = "30171-06.htm";
            } else if (cond == 16) {
               htmltext = "30171-07.htm";
            }
         } else if (npcId == 30717) {
            if (cond == 16) {
               htmltext = "30717-01.htm";
            } else if (cond == 17) {
               htmltext = "30717-08.htm";
            }
         } else if (npcId == 30720) {
            if (cond == 17) {
               htmltext = "30720-01.htm";
            } else if (cond == 18) {
               htmltext = "30720-04.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState(this.getName());
      if (st != null) {
         int npcId = npc.getId();
         if (npcId != 20554 || !st.isCond(11) && !st.isCond(12)) {
            if (npcId != 20563 || !st.isCond(14) && !st.isCond(15)) {
               if (npcId == 27336 && st.isCond(19) && st.calcDropItems(this.getId(), 9781, npc.getId(), 1)) {
                  st.setCond(20, true);
               } else if (Util.contains(_floranMobs, npc.getId()) && st.isCond(3) && st.calcDropItems(this.getId(), 9773, npc.getId(), 30)) {
                  st.setCond(4, true);
               } else if (Util.contains(_egMobs, npc.getId()) && st.isCond(7) && st.calcDropItems(this.getId(), 9775, npc.getId(), 30)) {
                  st.setCond(8, true);
               } else if ((npc.getId() == 20584 || npc.getId() == 20585) && st.isCond(19)) {
                  if (st.getInt("id") < _killsAmount) {
                     st.set("id", String.valueOf(st.getInt("id") + 1));
                  } else {
                     st.set("id", "0");
                     st.addSpawn(27336);
                  }
               }
            } else {
               if (st.isCond(14)) {
                  st.setCond(15);
               }

               if (st.calcDropItems(this.getId(), 9780, npc.getId(), 10)) {
                  st.setCond(16, true);
               }
            }
         } else {
            if (st.isCond(11)) {
               st.setCond(12);
            }

            if (st.calcDropItems(this.getId(), 9778, npc.getId(), 10)) {
               st.takeItems(9778, -1L);
               st.giveItems(9779, 1L);
               st.setCond(13, true);
            }
         }
      }

      return super.onKill(npc, player, isSummon);
   }

   public static void main(String[] args) {
      new _066_CertifiedArbalester(66, _066_CertifiedArbalester.class.getSimpleName(), "");
   }
}
