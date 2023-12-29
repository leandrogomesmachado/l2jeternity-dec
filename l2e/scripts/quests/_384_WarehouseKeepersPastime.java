package l2e.scripts.quests;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import l2e.gameserver.model.Bingo;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _384_WarehouseKeepersPastime extends Quest {
   private static final String qn = "_384_WarehouseKeepersPastime";
   private static final int Cliff = 30182;
   private static final int Baxt = 30685;
   private static final short Warehouse_Keepers_Medal = 5964;
   private static final Map<Integer, Integer> Medal_Chances = new ConcurrentHashMap<>();
   protected static final Map<Integer, Bingo> bingos = new ConcurrentHashMap<>();
   protected static final int[][] Rewards_Win = new int[][]{
      {16, 1888, 1}, {32, 1887, 1}, {50, 1894, 1}, {80, 952, 1}, {89, 1890, 1}, {98, 1893, 1}, {100, 951, 1}
   };
   protected static final int[][] Rewards_Win_Big = new int[][]{{50, 883, 1}, {80, 951, 1}, {98, 852, 1}, {100, 401, 1}};
   protected static final int[][] Rewards_Lose = new int[][]{{50, 4041, 1}, {80, 952, 1}, {98, 1892, 1}, {100, 917, 1}};
   protected static final int[][] Rewards_Lose_Big = new int[][]{{50, 951, 1}, {80, 500, 1}, {98, 2437, 2}, {100, 135, 1}};

   public _384_WarehouseKeepersPastime(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(30182);
      this.addTalkId(30182);
      this.addTalkId(30685);
      Medal_Chances.put(20948, 18);
      Medal_Chances.put(20945, 12);
      Medal_Chances.put(20946, 15);
      Medal_Chances.put(20947, 16);
      Medal_Chances.put(20635, 15);
      Medal_Chances.put(20773, 61);
      Medal_Chances.put(20774, 60);
      Medal_Chances.put(20760, 24);
      Medal_Chances.put(20758, 24);
      Medal_Chances.put(20759, 23);
      Medal_Chances.put(20242, 22);
      Medal_Chances.put(20281, 22);
      Medal_Chances.put(20556, 14);
      Medal_Chances.put(20668, 21);
      Medal_Chances.put(20241, 22);
      Medal_Chances.put(20286, 22);
      Medal_Chances.put(20950, 20);
      Medal_Chances.put(20949, 19);
      Medal_Chances.put(20942, 9);
      Medal_Chances.put(20943, 12);
      Medal_Chances.put(20944, 11);
      Medal_Chances.put(20559, 14);
      Medal_Chances.put(20243, 21);
      Medal_Chances.put(20282, 21);
      Medal_Chances.put(20677, 34);
      Medal_Chances.put(20605, 15);

      for(int mob : Medal_Chances.keySet()) {
         this.addKillId(mob);
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_384_WarehouseKeepersPastime");
      if (st == null) {
         return event;
      } else {
         int _state = st.getState();
         long medals = st.getQuestItemsCount(5964);
         if (event.equalsIgnoreCase("30182-05.htm") && _state == 0) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if ((event.equalsIgnoreCase("30182-08.htm") || event.equalsIgnoreCase("30685-08.htm")) && _state == 1) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
         } else {
            if (event.contains("-game") && _state == 1) {
               boolean big_game = event.contains("-big");
               int need_medals = big_game ? 100 : 10;
               if (medals < (long)need_medals) {
                  return event.replaceFirst("-big", "").replaceFirst("game", "09.htm");
               }

               st.takeItems(5964, (long)need_medals);
               int char_obj_id = player.getObjectId();
               if (bingos.containsKey(char_obj_id)) {
                  bingos.remove(char_obj_id);
               }

               Bingo bingo = new _384_WarehouseKeepersPastime.BingoClass(big_game, st);
               bingos.put(char_obj_id, bingo);
               return bingo.getDialog("");
            }

            if (event.contains("choice-") && _state == 1) {
               int char_obj_id = player.getObjectId();
               if (!bingos.containsKey(char_obj_id)) {
                  return null;
               }

               Bingo bingo = bingos.get(char_obj_id);
               return bingo.Select(event.replaceFirst("choice-", ""));
            }
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_384_WarehouseKeepersPastime");
      if (st == null) {
         return htmltext;
      } else {
         int _state = st.getState();
         int npcId = npc.getId();
         if (_state == 0) {
            if (npcId != 30182) {
               return htmltext;
            } else if (player.getLevel() < 40) {
               st.exitQuest(true);
               return "30182-04.htm";
            } else {
               st.set("cond", "0");
               return "30182-01.htm";
            }
         } else if (_state != 1) {
            return htmltext;
         } else {
            long medals = st.getQuestItemsCount(5964);
            if (medals >= 100L) {
               return npcId + "-06.htm";
            } else {
               return medals >= 10L ? npcId + "-06a.htm" : npcId + "-06b.htm";
            }
         }
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_384_WarehouseKeepersPastime");
      if (st == null) {
         return null;
      } else if (st.getState() != 1) {
         return null;
      } else {
         Integer chance = Medal_Chances.get(npc.getId());
         if (chance != null) {
            st.dropQuestItems(5964, 1, 10L, chance * 10000, true);
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _384_WarehouseKeepersPastime(384, "_384_WarehouseKeepersPastime", "");
   }

   public static class BingoClass extends Bingo {
      protected static final String msg_begin = "I've arranged 9 numbers on the panel. Don't peek! Ha ha ha!<br>Now give me your 10 medals. Some players run away when they realize that they don't stand a good chance of winning. Therefore, I prefer to hold the medals before the game starts. If you quit during game play, you'll forfeit your bet. Is that satisfactory?<br>Now, select your %choicenum% number.";
      protected static final String msg_0lines = "You are spectacularly unlucky! The red-colored numbers on the panel below are the ones you chose. As you can see, they didn't create even a single line. Did you know that it is harder not to create a single line than creating all 3 lines?<br>Usually, I don't give a reward when you don't create a single line, but since I'm feeling sorry for you, I'll be generous this time. Wait here.<br>.<br>.<br>.<br><br><br>Here, take this. I hope it will bring you better luck in the future.";
      protected static final String msg_3lines = "You've created 3 lines! The red colored numbers on the bingo panel below are the numbers you chose. Congratulations! As I promised, I'll give you an unclaimed item from my warehouse. Wait here.<br>.<br>.<br>.<br><br><br>Puff puff... it's very dusty. Here it is. Do you like it?";
      private static final String template_choice = "<a action=\"bypass -h Quest _384_WarehouseKeepersPastime choice-%n%\">%n%</a>&nbsp;&nbsp;&nbsp;&nbsp;  ";
      private final boolean _BigGame;
      private final QuestState _qs;

      public BingoClass(boolean BigGame, QuestState qs) {
         super("<a action=\"bypass -h Quest _384_WarehouseKeepersPastime choice-%n%\">%n%</a>&nbsp;&nbsp;&nbsp;&nbsp;  ");
         this._BigGame = BigGame;
         this._qs = qs;
      }

      @Override
      protected String getFinal() {
         String result = super.getFinal();
         if (this.lines == 3) {
            this.reward(this._BigGame ? _384_WarehouseKeepersPastime.Rewards_Win_Big : _384_WarehouseKeepersPastime.Rewards_Win);
         } else if (this.lines == 0) {
            this.reward(this._BigGame ? _384_WarehouseKeepersPastime.Rewards_Lose_Big : _384_WarehouseKeepersPastime.Rewards_Lose);
         }

         _384_WarehouseKeepersPastime.bingos.remove(this._qs.getPlayer().getObjectId());
         return result;
      }

      private void reward(int[][] rew) {
         int r = Quest.getRandom(100);

         for(int[] l : rew) {
            if (r < l[0]) {
               this._qs.giveItems(l[1], (long)l[2]);
               return;
            }
         }
      }
   }
}
