package l2e.scripts.quests;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import l2e.gameserver.model.Bingo;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _386_StolenDignity extends Quest {
   private static final String qn = "_386_StolenDignity";
   private static final int Romp = 30843;
   private static final short Stolen_Infernium_Ore = 6363;
   private static final short Required_Stolen_Infernium_Ore = 100;
   private static final Map<Integer, Integer> dropchances = new ConcurrentHashMap<>();
   protected static final Map<Integer, Bingo> bingos = new ConcurrentHashMap<>();
   protected static final int[][] Rewards_Win = new int[][]{
      {5529, 10},
      {5532, 10},
      {5533, 10},
      {5534, 10},
      {5535, 10},
      {5536, 10},
      {5537, 10},
      {5538, 10},
      {5539, 10},
      {5541, 10},
      {5542, 10},
      {5543, 10},
      {5544, 10},
      {5545, 10},
      {5546, 10},
      {5547, 10},
      {5548, 10},
      {8331, 10},
      {8341, 10},
      {8342, 10},
      {8346, 10},
      {8349, 10},
      {8712, 10},
      {8713, 10},
      {8714, 10},
      {8715, 10},
      {8716, 10},
      {8717, 10},
      {8718, 10},
      {8719, 10},
      {8720, 10},
      {8721, 10},
      {8722, 10}
   };
   protected static final int[][] Rewards_Lose = new int[][]{
      {5529, 4},
      {5532, 4},
      {5533, 4},
      {5534, 4},
      {5535, 4},
      {5536, 4},
      {5537, 4},
      {5538, 4},
      {5539, 4},
      {5541, 4},
      {5542, 4},
      {5543, 4},
      {5544, 4},
      {5545, 4},
      {5546, 4},
      {5547, 4},
      {5548, 4},
      {8331, 4},
      {8341, 4},
      {8342, 4},
      {8346, 4},
      {8349, 4},
      {8712, 4},
      {8713, 4},
      {8714, 4},
      {8715, 4},
      {8716, 4},
      {8717, 4},
      {8718, 4},
      {8719, 4},
      {8720, 4},
      {8721, 4},
      {8722, 4}
   };

   public _386_StolenDignity(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(30843);
      this.addTalkId(30843);
      dropchances.put(20670, 140000);
      dropchances.put(20671, 140000);
      dropchances.put(20954, 110000);
      dropchances.put(20956, 130000);
      dropchances.put(20958, 130000);
      dropchances.put(20959, 130000);
      dropchances.put(20960, 110000);
      dropchances.put(20964, 130000);
      dropchances.put(20969, 190000);
      dropchances.put(20967, 180000);
      dropchances.put(20970, 180000);
      dropchances.put(20971, 180000);
      dropchances.put(20974, 280000);
      dropchances.put(20975, 280000);
      dropchances.put(21001, 140000);
      dropchances.put(21003, 180000);
      dropchances.put(21005, 140000);
      dropchances.put(21020, 160000);
      dropchances.put(21021, 150000);
      dropchances.put(21259, 150000);
      dropchances.put(21089, 130000);
      dropchances.put(21108, 190000);
      dropchances.put(21110, 180000);
      dropchances.put(21113, 250000);
      dropchances.put(21114, 230000);
      dropchances.put(21116, 250000);

      for(int kill_id : dropchances.keySet()) {
         this.addKillId(kill_id);
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_386_StolenDignity");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30843-03.htm")) {
            st.setState((byte)1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30843-06.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
         } else {
            if (event.equalsIgnoreCase("game")) {
               if (st.getQuestItemsCount(6363) < 100L) {
                  return "30843-08.htm";
               }

               st.takeItems(6363, 100L);
               int char_obj_id = player.getObjectId();
               if (bingos.containsKey(char_obj_id)) {
                  bingos.remove(char_obj_id);
               }

               Bingo bingo = new _386_StolenDignity.BingoClass(st);
               bingos.put(char_obj_id, bingo);
               return bingo.getDialog("");
            }

            if (event.contains("choice-")) {
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
      QuestState st = player.getQuestState("_386_StolenDignity");
      if (st == null) {
         return htmltext;
      } else if (st.getState() == 0) {
         if (player.getLevel() < 58) {
            st.exitQuest(true);
            return "30843-00.htm";
         } else {
            return "30843-01.htm";
         }
      } else {
         return st.getQuestItemsCount(6363) < 100L ? "30843-04.htm" : "30843-05.htm";
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_386_StolenDignity");
      if (st == null) {
         return null;
      } else {
         Integer _chance = dropchances.get(npc.getId());
         if (_chance != null) {
            st.dropQuestItems(6363, 1, 10000000L, _chance, true);
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _386_StolenDignity(386, "_386_StolenDignity", "");
   }

   public static class BingoClass extends Bingo {
      protected static final String msg_begin = "I've arranged the numbers 1 through 9 on the grid. Don't peek!<br>Let me have the 100 Infernium Ores. Too many players try to run away without paying when it becomes obvious that they're losing...<br>OK, select six numbers between 1 and 9. Choose the %choicenum% number.";
      protected static final String msg_again = "You've already chosen that number. Make your %choicenum% choice again.";
      protected static final String msg_0lines = "Wow! How unlucky can you get? Your choices are highlighted in red below. As you can see, your choices didn't make a single line! Losing this badly is actually quite rare!<br>You look so sad, I feel bad for you... Wait here... <br>.<br>.<br>.<br>Take this... I hope it will bring you better luck in the future.";
      protected static final String msg_3lines = "Excellent! As you can see, you've formed three lines! Congratulations! As promised, I'll give you some unclaimed merchandise from the warehouse. Wait here...<br>.<br>.<br>.<br>Whew, it's dusty! OK, here you go. Do you like it?";
      protected static final String msg_lose = "Oh, too bad. Your choices didn't form three lines. You should try again... Your choices are highlighted in red.";
      private static final String template_choice = "<a action=\"bypass -h Quest _386_StolenDignity choice-%n%\">%n%</a>&nbsp;&nbsp;&nbsp;&nbsp;  ";
      private final QuestState _qs;

      public BingoClass(QuestState qs) {
         super("<a action=\"bypass -h Quest _386_StolenDignity choice-%n%\">%n%</a>&nbsp;&nbsp;&nbsp;&nbsp;  ");
         this._qs = qs;
      }

      @Override
      protected String getFinal() {
         String result = super.getFinal();
         if (this.lines == 3) {
            this.reward(_386_StolenDignity.Rewards_Win);
         } else if (this.lines == 0) {
            this.reward(_386_StolenDignity.Rewards_Lose);
         }

         _386_StolenDignity.bingos.remove(this._qs.getPlayer().getObjectId());
         return result;
      }

      private void reward(int[][] rew) {
         int[] r = rew[Quest.getRandom(rew.length)];
         this._qs.giveItems(r[0], (long)r[1]);
      }
   }
}
