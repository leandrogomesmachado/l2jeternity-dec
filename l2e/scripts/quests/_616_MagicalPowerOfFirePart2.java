package l2e.scripts.quests;

import l2e.commons.util.Rnd;
import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class _616_MagicalPowerOfFirePart2 extends Quest {
   public _616_MagicalPowerOfFirePart2(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31379);
      this.addTalkId(new int[]{31379, 31558});
      this.addKillId(25306);
      this.questItemIds = new int[]{7243, 7244};
      String info = this.loadGlobalQuestVar("_616_MagicalPowerOfFirePart2_respawn");
      long remain = !info.isEmpty() ? Long.parseLong(info) - System.currentTimeMillis() : 0L;
      if (remain > 0L) {
         this.startQuestTimer("spawn_altar", remain, null, null);
      } else {
         addSpawn(31558, 142368, -82512, -6487, 58000, false, 0L, true);
      }
   }

   @Override
   public void actionForEachPlayer(Player player, Npc npc, boolean isSummon) {
      QuestState st = player.getQuestState(this.getName());
      if (st != null && Util.checkIfInRange(1500, npc, player, false) && npc.getId() == 25306) {
         switch(st.getCond()) {
            case 1:
               st.takeItems(7243, 1L);
               break;
            case 2:
               if (!st.hasQuestItems(7244)) {
                  st.giveItems(7244, 1L);
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
            case "31379-02.htm":
               st.startQuest();
               htmltext = event;
               break;
            case "give_heart":
               if (st.hasQuestItems(7244)) {
                  st.calcExpAndSp(this.getId());
                  st.calcReward(this.getId(), Rnd.get(1, 6));
                  st.exitQuest(true, true);
                  htmltext = "31379-06.htm";
               } else {
                  htmltext = "31379-07.htm";
               }
               break;
            case "spawn_totem":
               htmltext = st.hasQuestItems(7243) ? this.spawnNastron(npc, st) : "31558-04.htm";
         }
      } else if (event.equals("despawn_nastron")) {
         npc.broadcastPacket(new NpcSay(npc, 22, NpcStringId.THE_POWER_OF_CONSTRAINT_IS_GETTING_WEAKER_YOUR_RITUAL_HAS_FAILED), 2000);
         npc.deleteMe();
         addSpawn(31558, 142368, -82512, -6487, 58000, false, 0L, true);
      } else if (event.equals("spawn_altar")) {
         addSpawn(31558, 142368, -82512, -6487, 58000, false, 0L, true);
      }

      return htmltext;
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = this.getQuestState(player, true);
      if (st == null) {
         return htmltext;
      } else {
         switch(npc.getId()) {
            case 31379:
               switch(st.getState()) {
                  case 0:
                     return player.getLevel() >= 75 ? (st.hasQuestItems(7243) ? "31379-01.htm" : "31379-00.htm") : "31379-00a.htm";
                  case 1:
                     htmltext = st.isCond(1) ? "31379-03.htm" : (st.hasQuestItems(7244) ? "31379-04.htm" : "31379-05.htm");
                     return htmltext;
                  default:
                     return htmltext;
               }
            case 31558:
               if (st.isStarted()) {
                  switch(st.getCond()) {
                     case 1:
                        htmltext = "31558-01.htm";
                        break;
                     case 2:
                        htmltext = this.spawnNastron(npc, st);
                        break;
                     case 3:
                        htmltext = "31558-05.htm";
                  }
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      if (npc.getId() == 25306) {
         long respawnDelay = 10800000L;
         this.cancelQuestTimer("despawn_nastron", npc, null);
         this.saveGlobalQuestVar("_616_MagicalPowerOfFirePart2_respawn", String.valueOf(System.currentTimeMillis() + 10800000L));
         this.startQuestTimer("spawn_altar", 10800000L, null, null);
         this.executeForEachPlayer(killer, npc, isSummon, true, false);
      }

      return super.onKill(npc, killer, isSummon);
   }

   private String spawnNastron(Npc npc, QuestState st) {
      if (this.getQuestTimer("spawn_altar", null, null) != null) {
         return "31558-03.htm";
      } else {
         if (st.isCond(1)) {
            st.takeItems(7243, 1L);
            st.setCond(2, true);
         }

         npc.deleteMe();
         Npc nastron = addSpawn(25306, 142528, -82528, -6496, 0, false, 0L);
         if (nastron != null) {
            nastron.broadcastPacket(
               new NpcSay(
                  nastron, 22, NpcStringId.THE_MAGICAL_POWER_OF_FIRE_IS_ALSO_THE_POWER_OF_FLAMES_AND_LAVA_IF_YOU_DARE_TO_CONFRONT_IT_ONLY_DEATH_WILL_AWAIT_YOU
               ),
               2000
            );
            this.startQuestTimer("despawn_nastron", 1200000L, nastron, null);
         }

         return "31558-02.htm";
      }
   }

   public static void main(String[] args) {
      new _616_MagicalPowerOfFirePart2(616, _616_MagicalPowerOfFirePart2.class.getSimpleName(), "");
   }
}
