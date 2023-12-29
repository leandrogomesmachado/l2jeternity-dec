package l2e.scripts.quests;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.strings.server.ServerStorage;

public class _662_AGameOfCards extends Quest {
   private static final String qn = "_662_AGameOfCards";
   private static final int KLUMP = 30845;
   private static final int[] mobs = new int[]{
      20677,
      21109,
      21112,
      21116,
      21114,
      21004,
      21002,
      21006,
      21008,
      21010,
      18001,
      20672,
      20673,
      20674,
      20955,
      20962,
      20961,
      20959,
      20958,
      20966,
      20965,
      20968,
      20973,
      20972,
      21278,
      21279,
      21280,
      21286,
      21287,
      21288,
      21520,
      21526,
      21530,
      21535,
      21508,
      21510,
      21513,
      21515
   };
   private static final int RED_GEM = 8765;
   private static final int Enchant_Weapon_S = 959;
   private static final int Enchant_Weapon_A = 729;
   private static final int Enchant_Weapon_B = 947;
   private static final int Enchant_Weapon_C = 951;
   private static final int Enchant_Weapon_D = 955;
   private static final int Enchant_Armor_D = 956;
   private static final int ZIGGOS_GEMSTONE = 8868;
   protected static final Map<Integer, _662_AGameOfCards.CardGame> Games = new ConcurrentHashMap<>();

   public _662_AGameOfCards(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30845);
      this.addTalkId(30845);
      this.addKillId(mobs);
      this.questItemIds = new int[]{8765};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_662_AGameOfCards");
      if (st == null) {
         return event;
      } else {
         int _state = st.getState();
         if (event.equalsIgnoreCase("30845-02.htm") && _state == 0) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30845-07.htm") && _state == 1) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         } else if (event.equalsIgnoreCase("30845-03.htm") && _state == 1 && st.getQuestItemsCount(8765) >= 50L) {
            htmltext = "30845-04.htm";
         } else if (event.equalsIgnoreCase("30845-10.htm") && _state == 1) {
            if (st.getQuestItemsCount(8765) < 50L) {
               htmltext = "30845-10a.htm";
            }

            st.takeItems(8765, 50L);
            int player_id = player.getObjectId();
            if (Games.containsKey(player_id)) {
               Games.remove(player_id);
            }

            Games.put(player_id, new _662_AGameOfCards.CardGame(player_id));
         } else {
            if (event.equalsIgnoreCase("play") && _state == 1) {
               int player_id = player.getObjectId();
               if (!Games.containsKey(player_id)) {
                  return null;
               }

               return Games.get(player_id).playField(player);
            }

            if (event.startsWith("card") && _state == 1) {
               int player_id = player.getObjectId();
               if (!Games.containsKey(player_id)) {
                  return null;
               }

               try {
                  int cardn = Integer.valueOf(event.replaceAll("card", ""));
                  return Games.get(player_id).next(cardn, st, player);
               } catch (Exception var9) {
                  return null;
               }
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_662_AGameOfCards");
      if (st == null) {
         return htmltext;
      } else {
         int _state = st.getState();
         if (_state == 0) {
            if (player.getLevel() < 61) {
               st.exitQuest(true);
               htmltext = "30845-00.htm";
            } else {
               htmltext = "30845-01.htm";
            }
         } else if (_state == 1) {
            return st.getQuestItemsCount(8765) < 50L ? "30845-03.htm" : "30845-04.htm";
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_662_AGameOfCards");
      if (st == null) {
         return null;
      } else {
         if (st.isStarted() && st.getRandom(100) < 45) {
            st.giveItems(8765, (long)(1.0F * Config.RATE_QUEST_DROP));
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _662_AGameOfCards(662, "_662_AGameOfCards", "");
   }

   private static class CardGame {
      private final String[] cards = new String[5];
      private final int player_id;
      private static final String[] card_chars = new String[]{"A", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
      private static final String html_header = "<html><body>";
      private static final String html_footer = "</body></html>";
      private static final String table_header = "<table border=\"1\" cellpadding=\"3\"><tr>";
      private static final String table_footer = "</tr></table><br><br>";
      private static final String td_begin = "<center><td width=\"50\" align=\"center\"><br><br><br> ";
      private static final String td_end = " <br><br><br><br></td></center>";

      public CardGame(int _player_id) {
         this.player_id = _player_id;

         for(int i = 0; i < this.cards.length; ++i) {
            this.cards[i] = "<a action=\"bypass -h Quest _662_AGameOfCards card" + i + "\">?</a>";
         }
      }

      public String next(int cardn, QuestState st, Player player) {
         if (cardn < this.cards.length && this.cards[cardn].startsWith("<a")) {
            this.cards[cardn] = card_chars[Quest.getRandom(card_chars.length)];

            for(String card : this.cards) {
               if (card.startsWith("<a")) {
                  return this.playField(player);
               }
            }

            return this.finish(st, player);
         } else {
            return null;
         }
      }

      private String finish(QuestState st, Player player) {
         String result = "<html><body><table border=\"1\" cellpadding=\"3\"><tr>";
         Map<String, Integer> matches = new HashMap<>();

         for(String card : this.cards) {
            int count = matches.containsKey(card) ? matches.remove(card) : 0;
            matches.put(card, ++count);
         }

         for(String card : this.cards) {
            if (matches.get(card) < 2) {
               matches.remove(card);
            }
         }

         String[] smatches = matches.keySet().toArray(new String[matches.size()]);
         Integer[] cmatches = matches.values().toArray(new Integer[matches.size()]);
         String txt = "" + ServerStorage.getInstance().getString(player.getLang(), "662quest.NO_PAIRS") + "";
         if (cmatches.length == 1) {
            if (cmatches[0] == 5) {
               txt = "" + ServerStorage.getInstance().getString(player.getLang(), "662quest.5_PAIRS") + "";
               st.giveItems(8868, 43L);
               st.giveItems(959, 3L);
               st.giveItems(729, 1L);
            } else if (cmatches[0] == 4) {
               txt = "" + ServerStorage.getInstance().getString(player.getLang(), "662quest.4_PAIRS") + "";
               st.giveItems(959, 2L);
               st.giveItems(951, 2L);
            } else if (cmatches[0] == 3) {
               txt = "" + ServerStorage.getInstance().getString(player.getLang(), "662quest.3_PAIRS") + "";
               st.giveItems(951, 2L);
            } else if (cmatches[0] == 2) {
               txt = "" + ServerStorage.getInstance().getString(player.getLang(), "662quest.1_PAIRS") + "";
               st.giveItems(956, 2L);
            }
         } else if (cmatches.length == 2) {
            if (cmatches[0] != 3 && cmatches[1] != 3) {
               txt = "" + ServerStorage.getInstance().getString(player.getLang(), "662quest.2_PAIRS") + "";
               st.giveItems(951, 1L);
            } else {
               txt = "" + ServerStorage.getInstance().getString(player.getLang(), "662quest.FULL_HOUSE") + "";
               st.giveItems(729, 1L);
               st.giveItems(947, 2L);
               st.giveItems(955, 1L);
            }
         }

         for(String card : this.cards) {
            if (smatches.length > 0 && smatches[0].equalsIgnoreCase(card)) {
               result = result
                  + "<center><td width=\"50\" align=\"center\"><br><br><br> <font color=\"55FD44\">"
                  + card
                  + "</font>"
                  + " <br><br><br><br></td></center>";
            } else if (smatches.length == 2 && smatches[1].equalsIgnoreCase(card)) {
               result = result
                  + "<center><td width=\"50\" align=\"center\"><br><br><br> <font color=\"FE6666\">"
                  + card
                  + "</font>"
                  + " <br><br><br><br></td></center>";
            } else {
               result = result + "<center><td width=\"50\" align=\"center\"><br><br><br> " + card + " <br><br><br><br></td></center>";
            }
         }

         result = result + "</tr></table><br><br>" + txt;
         if (st.getQuestItemsCount(8765) >= 50L) {
            result = result
               + "<br><br><a action=\"bypass -h Quest _662_AGameOfCards 30845-10.htm\">"
               + ServerStorage.getInstance().getString(player.getLang(), "662quest.PLAY_AGAIN")
               + "</a>";
         }

         result = result + "</body></html>";
         _662_AGameOfCards.Games.remove(this.player_id);
         return result;
      }

      public String playField(Player player) {
         String result = "<html><body><table border=\"1\" cellpadding=\"3\"><tr>";

         for(String card : this.cards) {
            result = result + "<center><td width=\"50\" align=\"center\"><br><br><br> " + card + " <br><br><br><br></td></center>";
         }

         return result + "</tr></table><br><br>" + ServerStorage.getInstance().getString(player.getLang(), "662quest.NEXT_CARD") + "" + "</body></html>";
      }
   }
}
