package l2e.scripts.quests;

import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class _712_PathToBecomingALordOren extends Quest {
   private static final String qn = "_712_PathToBecomingALordOren";
   private static final int Brasseur = 35226;
   private static final int Croop = 30676;
   private static final int Marty = 30169;
   private static final int Valleria = 30176;
   private static final int NebuliteOrb = 13851;
   private static final int[] OelMahims = new int[]{20575, 20576};
   private static final int OrenCastle = 4;

   public _712_PathToBecomingALordOren(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(new int[]{35226, 30169});
      this.addTalkId(35226);
      this.addTalkId(30676);
      this.addTalkId(30169);
      this.addTalkId(30176);
      this.questItemIds = new int[]{13851};
      this.addKillId(OelMahims);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_712_PathToBecomingALordOren");
      Castle castle = CastleManager.getInstance().getCastleById(4);
      if (castle.getOwner() == null) {
         return "Castle has no lord";
      } else {
         Player castleOwner = castle.getOwner().getLeader().getPlayerInstance();
         if (event.equals("brasseur_q712_03.htm")) {
            st.startQuest();
         } else if (event.equals("croop_q712_03.htm")) {
            st.setCond(3);
         } else if (event.equals("marty_q712_02.htm")) {
            if (this.isLordAvailable(3, st)) {
               castleOwner.getQuestState("_712_PathToBecomingALordOren").setCond(4);
               st.setState((byte)1);
            }
         } else if (event.equals("valleria_q712_02.htm")) {
            if (this.isLordAvailable(4, st)) {
               castleOwner.getQuestState("_712_PathToBecomingALordOren").setCond(5);
               st.exitQuest(true);
            }
         } else if (event.equals("croop_q712_05.htm")) {
            st.setCond(6);
         } else if (event.equals("croop_q712_07.htm")) {
            takeItems(player, 13851, -1L);
            st.setCond(8);
         } else if (event.equals("brasseur_q712_06.htm") && castleOwner != null) {
            NpcSay packet = new NpcSay(
               npc.getObjectId(), 23, npc.getId(), NpcStringId.S1_HAS_BECOME_THE_LORD_OF_THE_TOWN_OF_OREN_MAY_THERE_BE_GLORY_IN_THE_TERRITORY_OF_OREN
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
   public String onTalk(Npc npc, Player player) {
      QuestState st = this.getQuestState(player, true);
      String htmltext = getNoQuestMsg(player);
      Castle castle = CastleManager.getInstance().getCastleById(4);
      if (castle.getOwner() == null) {
         return "Castle has no lord";
      } else {
         Player castleOwner = castle.getOwner().getLeader().getPlayerInstance();
         switch(npc.getId()) {
            case 30169:
               if (st.isCond(0)) {
                  if (this.isLordAvailable(3, st)) {
                     htmltext = "marty_q712_01.htm";
                  } else {
                     htmltext = "marty_q712_00.htm";
                  }
               }
               break;
            case 30176:
               if (st.getState() == 1 && this.isLordAvailable(4, st)) {
                  htmltext = "valleria_q712_01.htm";
               }
               break;
            case 30676:
               if (st.isCond(2)) {
                  htmltext = "croop_q712_01.htm";
               } else if (st.isCond(3) || st.isCond(4)) {
                  htmltext = "croop_q712_03.htm";
               } else if (st.isCond(5)) {
                  htmltext = "croop_q712_04.htm";
               } else if (st.isCond(6)) {
                  htmltext = "croop_q712_05.htm";
               } else if (st.isCond(7)) {
                  htmltext = "croop_q712_06.htm";
               } else if (st.isCond(8)) {
                  htmltext = "croop_q712_08.htm";
               }
               break;
            case 35226:
               if (st.isCond(0)) {
                  if (castleOwner == player) {
                     if (!this.hasFort() && castle.getTerritory().getLordObjectId() != player.getObjectId()) {
                        htmltext = "brasseur_q712_01.htm";
                     } else {
                        htmltext = "brasseur_q712_00.htm";
                        st.exitQuest(true);
                     }
                  } else {
                     htmltext = "brasseur_q712_00a.htm";
                     st.exitQuest(true);
                  }
               } else if (st.isCond(1)) {
                  st.setCond(2);
                  htmltext = "brasseur_q712_04.htm";
               } else if (st.isCond(2)) {
                  htmltext = "brasseur_q712_04.htm";
               } else if (st.isCond(8)) {
                  htmltext = "brasseur_q712_05.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player killer, boolean isPet) {
      QuestState st = killer.getQuestState("_712_PathToBecomingALordOren");
      if (st != null && st.isCond(6)) {
         if (getQuestItemsCount(killer, 13851) < 300L) {
            giveItems(killer, 13851, 1L);
         }

         if (getQuestItemsCount(killer, 13851) >= 300L) {
            st.setCond(7);
         }
      }

      return null;
   }

   private boolean isLordAvailable(int cond, QuestState st) {
      Castle castle = CastleManager.getInstance().getCastleById(4);
      Clan owner = castle.getOwner();
      Player castleOwner = castle.getOwner().getLeader().getPlayerInstance();
      return owner != null
         && castleOwner != null
         && castleOwner != st.getPlayer()
         && owner == st.getPlayer().getClan()
         && castleOwner.getQuestState("_712_PathToBecomingALordOren") != null
         && castleOwner.getQuestState("_712_PathToBecomingALordOren").isCond(cond);
   }

   private boolean hasFort() {
      for(Fort fortress : FortManager.getInstance().getForts()) {
         if (fortress.getContractedCastleId() == 4) {
            return true;
         }
      }

      return false;
   }

   public static void main(String[] args) {
      new _712_PathToBecomingALordOren(712, "_712_PathToBecomingALordOren", "");
   }
}
