package l2e.scripts.quests;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _254_LegendaryTales extends Quest {
   private static final String qn = "_254_LegendaryTales";
   private static final int GILMORE = 30754;
   private static final int LARGE_DRAGON_SKULL = 17249;
   private static final int[] BOSS = new int[]{
      _254_LegendaryTales.Bosses.EMERALD_HORN.getId(),
      _254_LegendaryTales.Bosses.DUST_RIDER.getId(),
      _254_LegendaryTales.Bosses.BLEEDING_FLY.getId(),
      _254_LegendaryTales.Bosses.BLACK_DAGGER.getId(),
      _254_LegendaryTales.Bosses.SHADOW_SUMMONER.getId(),
      _254_LegendaryTales.Bosses.SPIKE_SLASHER.getId(),
      _254_LegendaryTales.Bosses.MUSCLE_BOMBER.getId()
   };
   private static final int[] REWARDS = new int[]{0, 13457, 13458, 13459, 13460, 13461, 13462, 13463, 13464, 13465, 13466, 13467};

   public _254_LegendaryTales(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30754);
      this.addTalkId(30754);
      this.addKillId(BOSS);
      this.questItemIds = new int[]{17249};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_254_LegendaryTales");
      if (st == null) {
         return event;
      } else {
         if (npc.getId() == 30754) {
            if (event.equalsIgnoreCase("accept")) {
               st.setState((byte)1);
               st.set("cond", "1");
               st.playSound("ItemSound.quest_accept");
               htmltext = "30754-07.htm";
            } else if (event.equalsIgnoreCase("emerald")) {
               htmltext = checkMask(st, _254_LegendaryTales.Bosses.EMERALD_HORN) ? "30754-22.htm" : "30754-16.htm";
            } else if (event.equalsIgnoreCase("dust")) {
               htmltext = checkMask(st, _254_LegendaryTales.Bosses.DUST_RIDER) ? "30754-23.htm" : "30754-17.htm";
            } else if (event.equalsIgnoreCase("bleeding")) {
               htmltext = checkMask(st, _254_LegendaryTales.Bosses.BLEEDING_FLY) ? "30754-24.htm" : "30754-18.htm";
            } else if (event.equalsIgnoreCase("daggerwyrm")) {
               htmltext = checkMask(st, _254_LegendaryTales.Bosses.BLACK_DAGGER) ? "30754-25.htm" : "30754-19.htm";
            } else if (event.equalsIgnoreCase("shadowsummoner")) {
               htmltext = checkMask(st, _254_LegendaryTales.Bosses.SHADOW_SUMMONER) ? "30754-26.htm" : "30754-16.htm";
            } else if (event.equalsIgnoreCase("spikeslasher")) {
               htmltext = checkMask(st, _254_LegendaryTales.Bosses.SPIKE_SLASHER) ? "30754-27.htm" : "30754-17.htm";
            } else if (event.equalsIgnoreCase("muclebomber")) {
               htmltext = checkMask(st, _254_LegendaryTales.Bosses.MUSCLE_BOMBER) ? "30754-28.htm" : "30754-18.htm";
            } else if (Util.isDigit(event)) {
               int reward_id = Integer.parseInt(event);
               if (reward_id > 0) {
                  if (st.getQuestItemsCount(17249) == 7L) {
                     int REWARD = REWARDS[reward_id];
                     st.takeItems(17249, 7L);
                     st.giveItems(REWARD, 1L);
                     htmltext = "30754-13.htm";
                     st.playSound("ItemSound.quest_finish");
                     st.exitQuest(false);
                  } else {
                     htmltext = "30754-12.htm";
                  }
               }
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_254_LegendaryTales");
      if (st == null) {
         return htmltext;
      } else {
         if (npc.getId() == 30754) {
            switch(st.getState()) {
               case 0:
                  if (player.getLevel() < 80) {
                     htmltext = "30754-03.htm";
                  } else {
                     htmltext = "30754-01.htm";
                  }
                  break;
               case 1:
                  if (st.isCond(1)) {
                     htmltext = "30754-09.htm";
                  } else if (st.isCond(2)) {
                     htmltext = "30754-10.htm";
                  }
                  break;
               case 2:
                  htmltext = "30754-02.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_254_LegendaryTales");
      if (st == null) {
         return null;
      } else {
         if (player.isInParty()) {
            for(Player memb : player.getParty().getMembers()) {
               this.rewardPlayer(npc, memb);
            }
         } else {
            this.rewardPlayer(npc, player);
         }

         return super.onKill(npc, player, isSummon);
      }
   }

   private void rewardPlayer(Npc npc, Player player) {
      QuestState st = player.getQuestState("_254_LegendaryTales");
      if (st != null && st.isCond(1)) {
         int raids = st.getInt("raids");
         _254_LegendaryTales.Bosses boss = _254_LegendaryTales.Bosses.valueOf(npc.getId());
         if (!checkMask(st, boss)) {
            st.set("raids", raids | boss.getMask());
            st.giveItems(17249, 1L);
            if (st.getQuestItemsCount(17249) < 7L) {
               st.playSound("Itemsound.quest_itemget");
            } else {
               st.set("cond", "2");
               st.playSound("ItemSound.quest_middle");
            }
         }
      }
   }

   private static boolean checkMask(QuestState st, _254_LegendaryTales.Bosses boss) {
      int pos = boss.getMask();
      return (st.getInt("raids") & pos) == pos;
   }

   public static void main(String[] args) {
      new _254_LegendaryTales(254, "_254_LegendaryTales", "");
   }

   public static enum Bosses {
      EMERALD_HORN(25718),
      DUST_RIDER(25719),
      BLEEDING_FLY(25720),
      BLACK_DAGGER(25721),
      SHADOW_SUMMONER(25722),
      SPIKE_SLASHER(25723),
      MUSCLE_BOMBER(25724);

      private final int _bossId;
      private final int _mask;

      private Bosses(int bossId) {
         this._bossId = bossId;
         this._mask = 1 << this.ordinal();
      }

      public int getId() {
         return this._bossId;
      }

      public int getMask() {
         return this._mask;
      }

      public static _254_LegendaryTales.Bosses valueOf(int npcId) {
         for(_254_LegendaryTales.Bosses val : values()) {
            if (val.getId() == npcId) {
               return val;
            }
         }

         return null;
      }
   }
}
