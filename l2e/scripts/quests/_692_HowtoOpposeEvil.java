package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import org.apache.commons.lang3.ArrayUtils;

public final class _692_HowtoOpposeEvil extends Quest {
   private static final String qn = "_692_HowtoOpposeEvil";
   private static final int DILIOS = 32549;
   private static final int KUTRAN = 32550;
   private static final int LEKON = 32557;
   private static final int FREED_SOUL_FRAGMENT = 13863;
   private static final int DRAGONKIN_CHARM_FRAGMENT = 13865;
   private static final int RESTLESS_SOUL = 13866;
   private static final int TIAT_CHARM = 13867;
   private static final int CONCENTRATED_SPIRIT_ENERGY = 15535;
   private static final int SPIRIT_STONE_DUST = 15536;
   private static final int FREED_SOUL = 13796;
   private static final int DRAGONKIN_CHARM = 13841;
   private static final int SPIRIT_STONE_FRAGMENT = 15486;
   private static final int[] SOD = new int[]{22552, 22541, 22550, 22551, 22596, 22544, 22540, 22547, 22542, 22543, 22539, 22546, 22548, 22536, 22538, 22537};
   private static final int[] SOI = new int[]{
      22509, 22510, 22511, 22512, 22513, 22514, 22515, 22520, 22522, 22527, 22531, 22535, 22516, 22517, 22518, 22519, 22521, 22524, 22528, 22532, 22530, 22535
   };
   private static final int[] SOA = new int[]{
      22746, 22747, 22748, 22749, 22750, 22751, 22752, 22753, 22754, 22755, 22756, 22757, 22758, 22759, 22760, 22761, 22762, 22763, 22764, 22765
   };

   public _692_HowtoOpposeEvil(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32549);
      this.addTalkId(32549);
      this.addTalkId(32550);
      this.addTalkId(32557);

      for(int i : SOD) {
         this.addKillId(i);
      }

      for(int i : SOI) {
         this.addKillId(i);
      }

      for(int i : SOA) {
         this.addKillId(i);
      }

      this.questItemIds = new int[]{13863, 13865, 13866, 13867, 15535, 15536};
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_692_HowtoOpposeEvil");
      if (st == null) {
         return event;
      } else {
         int cond = st.getInt("cond");
         if (event.equalsIgnoreCase("take_test") && cond == 0) {
            QuestState _quest = player.getQuestState("_10273_GoodDayToFly");
            if (_quest != null && _quest.getState() == 2) {
               st.set("cond", "2");
               st.setState((byte)1);
               st.playSound("ItemSound.quest_accept");
               htmltext = "dilios_q692_4.htm";
            } else {
               st.set("cond", "1");
               st.setState((byte)1);
               st.playSound("ItemSound.quest_accept");
               htmltext = "dilios_q692_3.htm";
            }
         } else if (event.equalsIgnoreCase("lekon_q692_2.htm") && cond == 1) {
            st.unset("cond");
            st.exitQuest(true);
            st.playSound("ItemSound.quest_finish");
         } else if (event.equalsIgnoreCase("kutran_q692_2.htm") && cond == 2) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("exchange_sod") && cond == 3) {
            if (st.getQuestItemsCount(13865) < 5L) {
               htmltext = "kutran_q692_7.htm";
            } else {
               int _charmstogive = Math.round((float)(st.getQuestItemsCount(13865) / 5L));
               st.takeItems(13865, (long)(5 * _charmstogive));
               st.giveItems(13841, (long)_charmstogive);
               htmltext = "kutran_q692_4.htm";
            }
         } else if (event.equalsIgnoreCase("exchange_soi") && cond == 3) {
            if (st.getQuestItemsCount(13863) < 5L) {
               htmltext = "kutran_q692_7.htm";
            } else {
               int _soulstogive = Math.round((float)(st.getQuestItemsCount(13863) / 5L));
               st.takeItems(13863, (long)(5 * _soulstogive));
               st.giveItems(13796, (long)_soulstogive);
               htmltext = "kutran_q692_5.htm";
            }
         } else if (event.equalsIgnoreCase("exchange_soa") && cond == 3) {
            if (st.getQuestItemsCount(15536) < 5L) {
               htmltext = "kutran_q692_7.htm";
            } else {
               int _soulstogive = Math.round((float)(st.getQuestItemsCount(15536) / 5L));
               st.takeItems(15536, (long)(5 * _soulstogive));
               st.giveItems(15486, (long)_soulstogive);
               htmltext = "kutran_q692_5.htm";
            }
         } else if (event.equalsIgnoreCase("exchange_breath") && cond == 3) {
            if (st.getQuestItemsCount(13867) == 0L) {
               htmltext = "kutran_q692_7.htm";
            } else {
               st.giveItems(57, st.getQuestItemsCount(13867) * 2500L);
               st.takeItems(13867, -1L);
               htmltext = "kutran_q692_5.htm";
            }
         } else if (event.equalsIgnoreCase("exchange_portion") && cond == 3) {
            if (st.getQuestItemsCount(13866) == 0L) {
               htmltext = "kutran_q692_7.htm";
            } else {
               st.giveItems(57, st.getQuestItemsCount(13866) * 2500L);
               st.takeItems(13866, -1L);
               htmltext = "kutran_q692_5.htm";
            }
         } else if (event.equalsIgnoreCase("exchange_energy") && cond == 3) {
            if (st.getQuestItemsCount(15535) == 0L) {
               htmltext = "kutran_q692_7.htm";
            } else {
               st.giveItems(57, st.getQuestItemsCount(15535) * 25000L);
               st.takeItems(15535, -1L);
               htmltext = "kutran_q692_5.htm";
            }
         }

         if (event.equalsIgnoreCase("32549-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("32550-04.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
         }

         return htmltext;
      }
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_692_HowtoOpposeEvil");
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         int npcId = npc.getId();
         if (npcId == 32549) {
            if (cond == 0) {
               if (player.getLevel() >= 75) {
                  htmltext = "dilios_q692_1.htm";
               } else {
                  htmltext = "dilios_q692_0.htm";
               }
            }
         } else if (npcId == 32550) {
            if (cond == 2) {
               htmltext = "kutran_q692_1.htm";
            } else if (cond == 3) {
               htmltext = "kutran_q692_3.htm";
            }
         } else if (npcId == 32557 && cond == 1) {
            htmltext = "lekon_q692_1.htm";
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_692_HowtoOpposeEvil");
      if (st == null) {
         return null;
      } else {
         Player partyMember = this.getRandomPartyMember(player, 3);
         if (partyMember == null) {
            return null;
         } else {
            int npcId = npc.getId();
            if (ArrayUtils.contains(SOD, npcId)) {
               st.rollAndGive(13865, 1, 10.0);
            } else if (ArrayUtils.contains(SOI, npcId)) {
               st.rollAndGive(13863, 1, 10.0);
            } else if (ArrayUtils.contains(SOA, npcId)) {
               st.rollAndGive(15536, 1, 15.0);
            }

            return null;
         }
      }
   }

   public static void main(String[] args) {
      new _692_HowtoOpposeEvil(692, "_692_HowtoOpposeEvil", "");
   }
}
