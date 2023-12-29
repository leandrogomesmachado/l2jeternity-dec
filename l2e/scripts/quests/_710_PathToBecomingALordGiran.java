package l2e.scripts.quests;

import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class _710_PathToBecomingALordGiran extends Quest {
   private static final String qn = "_710_PathToBecomingALordGiran";
   private static final int SAUL = 35184;
   private static final int GESTO = 30511;
   private static final int FELTON = 30879;
   private static final int CARGO_BOX = 32243;
   private static final int FREIGHT_CHESTS_SEAL = 13014;
   private static final int GESTOS_BOX = 13013;
   private static final int[] MOBS = new int[]{20832, 20833, 20835, 21602, 21603, 21604, 21605, 21606, 21607, 21608, 21609};
   private static final int GIRAN_CASTLE = 3;

   public _710_PathToBecomingALordGiran(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(35184);
      this.addTalkId(new int[]{35184, 30511, 30879, 32243});
      this.addKillId(MOBS);
      this.questItemIds = new int[]{13014, 13013};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_710_PathToBecomingALordGiran");
      Castle castle = CastleManager.getInstance().getCastleById(3);
      if (castle.getOwner() == null) {
         return "Castle has no lord";
      } else {
         if (event.equals("35184-03.htm")) {
            st.startQuest();
         } else if (event.equals("30511-03.htm")) {
            st.setCond(3);
         } else if (event.equals("30879-02.htm")) {
            st.setCond(4);
         } else if (event.equals("35184-07.htm") && castle.getOwner().getLeader().getPlayerInstance() != null) {
            NpcSay packet = new NpcSay(
               npc.getObjectId(), 23, npc.getId(), NpcStringId.S1_HAS_BECOME_THE_LORD_OF_THE_TOWN_OF_GIRAN_MAY_THERE_BE_GLORY_IN_THE_TERRITORY_OF_GIRAN
            );
            packet.addStringParameter(player.getName());
            npc.broadcastPacket(packet);
            castle.getTerritory().changeOwner(castle.getOwner());
            st.exitQuest(true, true);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player talker) {
      QuestState st = this.getQuestState(talker, true);
      String htmltext = getNoQuestMsg(talker);
      Castle castle = CastleManager.getInstance().getCastleById(3);
      if (castle.getOwner() == null) {
         return "Castle has no lord";
      } else {
         Player castleOwner = castle.getOwner().getLeader().getPlayerInstance();
         switch(npc.getId()) {
            case 30511:
               if (st.isCond(2)) {
                  htmltext = "30511-01.htm";
               } else if (st.isCond(3) || st.isCond(4)) {
                  htmltext = "30511-04.htm";
               } else if (st.isCond(5)) {
                  takeItems(talker, 13014, -1L);
                  st.setCond(7);
                  htmltext = "30511-05.htm";
               } else if (st.isCond(7)) {
                  htmltext = "30511-06.htm";
               } else if (st.isCond(8)) {
                  takeItems(talker, 13013, -1L);
                  st.setCond(9);
                  htmltext = "30511-07.htm";
               } else if (st.isCond(9)) {
                  htmltext = "30511-07.htm";
               }
               break;
            case 30879:
               if (st.isCond(3)) {
                  htmltext = "30879-01.htm";
               } else if (st.isCond(4)) {
                  htmltext = "30879-03.htm";
               }
               break;
            case 32243:
               if (st.isCond(4)) {
                  st.setCond(5);
                  giveItems(talker, 13014, 1L);
                  htmltext = "32243-01.htm";
               } else if (st.isCond(5)) {
                  htmltext = "32243-02.htm";
               }
               break;
            case 35184:
               if (st.isCond(0)) {
                  if (castleOwner == talker) {
                     if (!this.hasFort() && castle.getTerritory().getLordObjectId() != talker.getObjectId()) {
                        htmltext = "35184-01.htm";
                     } else {
                        htmltext = "35184-00.htm";
                        st.exitQuest(true);
                     }
                  } else {
                     htmltext = "35184-00a.htm";
                     st.exitQuest(true);
                  }
               } else if (st.isCond(1)) {
                  st.setCond(2);
                  htmltext = "35184-04.htm";
               } else if (st.isCond(2)) {
                  htmltext = "35184-05.htm";
               } else if (st.isCond(9)) {
                  htmltext = "35184-06.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player killer, boolean isSummon) {
      QuestState st = killer.getQuestState("_710_PathToBecomingALordGiran");
      if (st != null && st.isCond(7)) {
         if (getQuestItemsCount(killer, 13013) < 300L) {
            giveItems(killer, 13013, 1L);
         }

         if (getQuestItemsCount(killer, 13013) >= 300L) {
            st.setCond(8);
         }
      }

      return super.onKill(npc, killer, isSummon);
   }

   private boolean hasFort() {
      for(Fort fortress : FortManager.getInstance().getForts()) {
         if (fortress.getContractedCastleId() == 3) {
            return true;
         }
      }

      return false;
   }

   public static void main(String[] args) {
      new _710_PathToBecomingALordGiran(710, "_710_PathToBecomingALordGiran", "");
   }
}
