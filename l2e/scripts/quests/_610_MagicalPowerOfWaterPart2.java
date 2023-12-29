package l2e.scripts.quests;

import l2e.commons.util.Rnd;
import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class _610_MagicalPowerOfWaterPart2 extends Quest {
   private _610_MagicalPowerOfWaterPart2(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31372);
      this.addTalkId(new int[]{31372, 31560});
      this.addKillId(25316);
      this.questItemIds = new int[]{7238, 7239};
      String info = this.loadGlobalQuestVar("_610_MagicalPowerOfWaterPart2_respawn");
      long remain = !info.isEmpty() ? Long.parseLong(info) - System.currentTimeMillis() : 0L;
      if (remain > 0L) {
         this.startQuestTimer("spawn_altar", remain, null, null);
      } else {
         addSpawn(31560, 105452, -36775, -1050, 34000, false, 0L, true);
      }
   }

   @Override
   public void actionForEachPlayer(Player player, Npc npc, boolean isSummon) {
      QuestState st = player.getQuestState(this.getName());
      if (st != null && Util.checkIfInRange(1500, npc, player, false) && npc.getId() == 25316) {
         switch(st.getCond()) {
            case 1:
               st.takeItems(7238, 1L);
               break;
            case 2:
               if (!st.hasQuestItems(7239)) {
                  st.giveItems(7239, 1L);
               }

               st.setCond(3, true);
         }
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = null;
      if (player != null) {
         QuestState st = player.getQuestState(this.getName());
         if (st == null) {
            return null;
         }

         switch(event) {
            case "31372-02.htm":
               st.startQuest();
               htmltext = event;
               break;
            case "give_heart":
               if (st.hasQuestItems(7239)) {
                  st.calcExpAndSp(this.getId());
                  st.calcReward(this.getId(), Rnd.get(1, 6));
                  st.exitQuest(true, true);
                  htmltext = "31372-06.htm";
               } else {
                  htmltext = "31372-07.htm";
               }
               break;
            case "spawn_totem":
               htmltext = st.hasQuestItems(7238) ? this.spawnAshutar(npc, st) : "31560-04.htm";
         }
      } else if (event.equals("despawn_ashutar")) {
         npc.broadcastPacket(new NpcSay(npc, 22, NpcStringId.THE_POWER_OF_CONSTRAINT_IS_GETTING_WEAKER_YOUR_RITUAL_HAS_FAILED), 2000);
         npc.deleteMe();
         addSpawn(31560, 105452, -36775, -1050, 34000, false, 0L, true);
      } else if (event.equals("spawn_altar")) {
         addSpawn(31560, 105452, -36775, -1050, 34000, false, 0L, true);
      }

      return htmltext;
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      if (npc.getId() == 25316) {
         long respawnDelay = 10800000L;
         this.cancelQuestTimer("despawn_ashutar", npc, null);
         this.saveGlobalQuestVar("_610_MagicalPowerOfWaterPart2_respawn", String.valueOf(System.currentTimeMillis() + 10800000L));
         this.startQuestTimer("spawn_altar", 10800000L, null, null);
         this.executeForEachPlayer(killer, npc, isSummon, true, false);
      }

      return super.onKill(npc, killer, isSummon);
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = this.getQuestState(player, true);
      if (st == null) {
         return htmltext;
      } else {
         switch(npc.getId()) {
            case 31372:
               switch(st.getState()) {
                  case 0:
                     return player.getLevel() >= 75 ? (st.hasQuestItems(7238) ? "31372-01.htm" : "31372-00.htm") : "31372-00a.htm";
                  case 1:
                     htmltext = st.isCond(1) ? "31372-03.htm" : (st.hasQuestItems(7239) ? "31372-04.htm" : "31372-05.htm");
                     return htmltext;
                  default:
                     return htmltext;
               }
            case 31560:
               if (st.isStarted()) {
                  switch(st.getCond()) {
                     case 1:
                        htmltext = "31560-01.htm";
                        break;
                     case 2:
                        htmltext = this.spawnAshutar(npc, st);
                        break;
                     case 3:
                        htmltext = "31560-05.htm";
                  }
               }
         }

         return htmltext;
      }
   }

   private String spawnAshutar(Npc npc, QuestState st) {
      if (this.getQuestTimer("spawn_altar", null, null) != null) {
         return "31560-03.htm";
      } else {
         if (st.isCond(1)) {
            st.takeItems(7238, 1L);
            st.setCond(2, true);
         }

         npc.deleteMe();
         Npc ashutar = addSpawn(25316, 104825, -36926, -1136, 0, false, 0L);
         if (ashutar != null) {
            ashutar.broadcastPacket(
               new NpcSay(
                  ashutar,
                  22,
                  NpcStringId.THE_MAGICAL_POWER_OF_WATER_COMES_FROM_THE_POWER_OF_STORM_AND_HAIL_IF_YOU_DARE_TO_CONFRONT_IT_ONLY_DEATH_WILL_AWAIT_YOU
               ),
               2000
            );
            this.startQuestTimer("despawn_ashutar", 1200000L, ashutar, null);
         }

         return "31560-02.htm";
      }
   }

   public static void main(String[] args) {
      new _610_MagicalPowerOfWaterPart2(610, _610_MagicalPowerOfWaterPart2.class.getSimpleName(), "");
   }
}
