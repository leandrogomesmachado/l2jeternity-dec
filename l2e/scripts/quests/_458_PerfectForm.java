package l2e.scripts.quests;

import java.util.Calendar;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class _458_PerfectForm extends Quest {
   private static final String qn = "_458_PerfectForm";
   private static final int _kelia = 32768;
   private static final int[] _mobs1 = new int[]{18878, 18879};
   private static final int[] _mobs2 = new int[]{18885, 18886};
   private static final int[] _mobs3 = new int[]{18892, 18893};
   private static final int[] _mobs4 = new int[]{18899, 18900};
   private static final int RESET_HOUR = 6;
   private static final int RESET_MIN = 30;
   public int mobs1Count = 0;
   public int mobs2Count = 0;
   public int mobs3Count = 0;
   public int mobs4Count = 0;
   public int mobsoverhitCount = 0;
   private static final int SPICE1 = 15482;
   private static final int SPICE2 = 15483;
   private static final int[][] _rewards1 = new int[][]{
      {10397, 2}, {10398, 2}, {10399, 2}, {10400, 2}, {10401, 2}, {10402, 2}, {10403, 2}, {10404, 2}, {10405, 2}
   };
   private static final int[][] _rewards2 = new int[][]{
      {10397, 5}, {10398, 5}, {10399, 5}, {10400, 5}, {10401, 5}, {10402, 5}, {10403, 5}, {10404, 5}, {10405, 5}
   };
   private static final int[][] _rewards3 = new int[][]{
      {10373, 1}, {10374, 1}, {10375, 1}, {10376, 1}, {10377, 1}, {10378, 1}, {10379, 1}, {10380, 1}, {10381, 1}
   };

   public _458_PerfectForm(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32768);
      this.addTalkId(32768);

      for(int i : _mobs1) {
         this.addKillId(i);
      }

      for(int i : _mobs2) {
         this.addKillId(i);
      }

      for(int i : _mobs3) {
         this.addKillId(i);
      }

      for(int i : _mobs4) {
         this.addKillId(i);
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_458_PerfectForm");
      if (st == null) {
         return event;
      } else {
         if (npc.getId() == 32768) {
            if (event.equalsIgnoreCase("32768-12.htm")) {
               st.setState((byte)1);
               st.set("cond", "1");
               st.playSound("ItemSound.quest_accept");
            } else if (event.equalsIgnoreCase("32768-16.htm")) {
               NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
               if (this.mobsoverhitCount >= 0 && this.mobsoverhitCount <= 6) {
                  html.setFile(player, player.getLang(), "data/scripts/quests/_458_PerfectForm/" + player.getLang() + "/32768-16c.htm");
                  html.replace("%overhits%", "" + this.mobsoverhitCount);
               } else if (this.mobsoverhitCount >= 7 && this.mobsoverhitCount <= 19) {
                  html.setFile(player, player.getLang(), "data/scripts/quests/_458_PerfectForm/" + player.getLang() + "/32768-16b.htm");
                  html.replace("%overhits%", "" + this.mobsoverhitCount);
               } else if (this.mobsoverhitCount >= 20) {
                  html.setFile(player, player.getLang(), "data/scripts/quests/_458_PerfectForm/" + player.getLang() + "/32768-16a.htm");
                  html.replace("%overhits%", "" + this.mobsoverhitCount);
               }
            } else if (event.equalsIgnoreCase("32768-17.htm")) {
               if (this.mobsoverhitCount >= 0 && this.mobsoverhitCount <= 6) {
                  st.unset("cond");
                  st.giveItems(_rewards1[getRandom(_rewards1.length)][0], (long)(_rewards1[getRandom(_rewards1.length)][1] * (int)Config.RATE_QUEST_REWARD));
                  st.giveItems(15482, (long)((int)Config.RATE_QUEST_REWARD));
                  st.giveItems(15483, (long)((int)Config.RATE_QUEST_REWARD));
                  st.playSound("ItemSound.quest_finish");
                  st.exitQuest(false);
                  this.mobs1Count = 0;
                  this.mobs2Count = 0;
                  this.mobs3Count = 0;
                  this.mobs4Count = 0;
                  this.mobsoverhitCount = 0;
                  Calendar reDo = Calendar.getInstance();
                  reDo.set(12, 30);
                  if (reDo.get(11) >= 6) {
                     reDo.add(5, 1);
                  }

                  reDo.set(11, 6);
                  st.set("reDoTime", String.valueOf(reDo.getTimeInMillis()));
               } else if (this.mobsoverhitCount >= 7 && this.mobsoverhitCount <= 19) {
                  st.unset("cond");
                  st.giveItems(_rewards2[getRandom(_rewards2.length)][0], (long)(_rewards2[getRandom(_rewards2.length)][1] * (int)Config.RATE_QUEST_REWARD));
                  st.giveItems(15482, (long)((int)Config.RATE_QUEST_REWARD));
                  st.giveItems(15483, (long)((int)Config.RATE_QUEST_REWARD));
                  st.playSound("ItemSound.quest_finish");
                  st.exitQuest(false);
                  this.mobs1Count = 0;
                  this.mobs2Count = 0;
                  this.mobs3Count = 0;
                  this.mobs4Count = 0;
                  this.mobsoverhitCount = 0;
                  Calendar reDo = Calendar.getInstance();
                  reDo.set(12, 30);
                  if (reDo.get(11) >= 6) {
                     reDo.add(5, 1);
                  }

                  reDo.set(11, 6);
                  st.set("reDoTime", String.valueOf(reDo.getTimeInMillis()));
               } else if (this.mobsoverhitCount >= 20) {
                  st.unset("cond");
                  st.giveItems(_rewards3[getRandom(_rewards3.length)][0], (long)(_rewards3[getRandom(_rewards3.length)][1] * (int)Config.RATE_QUEST_REWARD));
                  st.giveItems(15482, (long)((int)Config.RATE_QUEST_REWARD));
                  st.giveItems(15483, (long)((int)Config.RATE_QUEST_REWARD));
                  st.playSound("ItemSound.quest_finish");
                  st.exitQuest(false);
                  this.mobs1Count = 0;
                  this.mobs2Count = 0;
                  this.mobs3Count = 0;
                  this.mobs4Count = 0;
                  this.mobsoverhitCount = 0;
                  Calendar reDo = Calendar.getInstance();
                  reDo.set(12, 30);
                  if (reDo.get(11) >= 6) {
                     reDo.add(5, 1);
                  }

                  reDo.set(11, 6);
                  st.set("reDoTime", String.valueOf(reDo.getTimeInMillis()));
               }
            }
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_458_PerfectForm");
      if (st == null) {
         return htmltext;
      } else {
         if (npc.getId() == 32768) {
            switch(st.getState()) {
               case 0:
                  if (player.getLevel() >= 82) {
                     htmltext = "32768-01.htm";
                  } else {
                     htmltext = "32768-03.htm";
                  }
                  break;
               case 1:
                  if (st.getInt("cond") == 1) {
                     if (this.mobs1Count == 0 && this.mobs2Count == 0 && this.mobs3Count == 0 && this.mobs4Count == 0) {
                        htmltext = "32768-13.htm";
                     } else {
                        htmltext = "32768-14.htm";
                     }
                  } else if (st.getInt("cond") == 2) {
                     htmltext = "32768-15.htm";
                  }
                  break;
               case 2:
                  Long reDoTime = Long.parseLong(st.get("reDoTime"));
                  if (reDoTime > System.currentTimeMillis()) {
                     htmltext = "32768-02.htm";
                  } else {
                     st.setState((byte)0);
                     if (player.getLevel() >= 82) {
                        htmltext = "32768-01.htm";
                     } else {
                        htmltext = "32768-03.htm";
                     }
                  }
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_458_PerfectForm");
      if (st == null) {
         return null;
      } else {
         int npcId = npc.getId();
         if (st.getInt("cond") == 1) {
            if (this.mobs1Count >= 10 && this.mobs2Count >= 10 && this.mobs3Count >= 10 && this.mobs4Count >= 10) {
               st.set("cond", "2");
            } else if (Util.contains(_mobs1, npcId)) {
               ++this.mobs1Count;
               if (((Attackable)npc).isOverhit()) {
                  ++this.mobsoverhitCount;
               }
            } else if (Util.contains(_mobs2, npcId)) {
               ++this.mobs2Count;
               if (((Attackable)npc).isOverhit()) {
                  ++this.mobsoverhitCount;
               }
            } else if (Util.contains(_mobs3, npcId)) {
               ++this.mobs3Count;
               if (((Attackable)npc).isOverhit()) {
                  ++this.mobsoverhitCount;
               }
            } else if (Util.contains(_mobs4, npcId)) {
               ++this.mobs4Count;
               if (((Attackable)npc).isOverhit()) {
                  ++this.mobsoverhitCount;
               }
            }
         }

         return super.onKill(npc, player, isSummon);
      }
   }

   public static void main(String[] args) {
      new _458_PerfectForm(458, "_458_PerfectForm", "");
   }
}
