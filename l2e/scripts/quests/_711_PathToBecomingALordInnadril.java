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

public class _711_PathToBecomingALordInnadril extends Quest {
   private static final String qn = "_711_PathToBecomingALordInnadril";
   private static final int Neurath = 35316;
   private static final int IasonHeine = 30969;
   private static final int InnadrilCastle = 6;
   private static final int[] mobs = new int[]{20789, 20790, 20791, 20792, 20793, 20804, 20805, 20806, 20807, 20808};

   public _711_PathToBecomingALordInnadril(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(35316);
      this.addTalkId(35316);
      this.addTalkId(30969);
      this.addKillId(mobs);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_711_PathToBecomingALordInnadril");
      Castle castle = CastleManager.getInstance().getCastleById(6);
      Player castleOwner = castle.getOwner().getLeader().getPlayerInstance();
      if (event.equals("neurath_q711_03.htm")) {
         st.startQuest();
      } else if (event.equals("neurath_q711_05.htm")) {
         st.setCond(2);
      } else if (event.equals("neurath_q711_08.htm")) {
         if (this.isLordAvailable(2, st)) {
            castleOwner.getQuestState("_711_PathToBecomingALordInnadril").set("confidant", String.valueOf(player.getObjectId()));
            castleOwner.getQuestState("_711_PathToBecomingALordInnadril").setCond(3);
            st.setState((byte)1);
         } else {
            htmltext = "neurath_q711_07a.htm";
         }
      } else if (event.equals("heine_q711_03.htm")) {
         if (this.isLordAvailable(3, st)) {
            castleOwner.getQuestState("_711_PathToBecomingALordInnadril").setCond(4);
         } else {
            htmltext = "heine_q711_00a.htm";
         }
      } else if (event.equals("neurath_q711_12.htm") && castleOwner != null) {
         NpcSay packet = new NpcSay(
            npc.getObjectId(), 23, npc.getId(), NpcStringId.S1_HAS_BECOME_THE_LORD_OF_THE_TOWN_OF_INNADRIL_MAY_THERE_BE_GLORY_IN_THE_TERRITORY_OF_INNADRIL
         );
         packet.addStringParameter(player.getName());
         npc.broadcastPacket(packet);
         castle.getTerritory().changeOwner(castle.getOwner());
         st.exitQuest(true, true);
      }

      return htmltext;
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = this.getQuestState(player, true);
      String htmltext = getNoQuestMsg(player);
      Castle castle = CastleManager.getInstance().getCastleById(6);
      if (castle.getOwner() == null) {
         return "Castle has no lord";
      } else {
         Player castleOwner = castle.getOwner().getLeader().getPlayerInstance();
         switch(npc.getId()) {
            case 30969:
               if (st.getState() == 1 && st.isCond(0)) {
                  if (this.isLordAvailable(3, st)) {
                     if (castleOwner.getQuestState("_711_PathToBecomingALordInnadril").getInt("confidant") == player.getObjectId()) {
                        htmltext = "heine_q711_01.htm";
                     } else {
                        htmltext = "heine_q711_00.htm";
                     }
                  } else if (this.isLordAvailable(4, st)) {
                     if (castleOwner.getQuestState("_711_PathToBecomingALordInnadril").getInt("confidant") == player.getObjectId()) {
                        htmltext = "heine_q711_03.htm";
                     } else {
                        htmltext = "heine_q711_00.htm";
                     }
                  } else {
                     htmltext = "heine_q711_00a.htm";
                  }
               }
               break;
            case 35316:
               if (st.isCond(0)) {
                  if (castleOwner == player) {
                     if (!this.hasFort() && castle.getTerritory().getLordObjectId() != player.getObjectId()) {
                        htmltext = "neurath_q711_01.htm";
                     } else {
                        htmltext = "neurath_q711_00.htm";
                        st.exitQuest(true);
                     }
                  } else if (this.isLordAvailable(2, st)) {
                     if (castleOwner.calculateDistance(npc, false, false) <= 200.0) {
                        htmltext = "neurath_q711_07.htm";
                     } else {
                        htmltext = "neurath_q711_07a.htm";
                     }
                  } else if (st.getState() == 1) {
                     htmltext = "neurath_q711_00b.htm";
                  } else {
                     htmltext = "neurath_q711_00a.htm";
                     st.exitQuest(true);
                  }
               } else if (st.isCond(1)) {
                  htmltext = "neurath_q711_04.htm";
               } else if (st.isCond(2)) {
                  htmltext = "neurath_q711_06.htm";
               } else if (st.isCond(3)) {
                  htmltext = "neurath_q711_09.htm";
               } else if (st.isCond(4)) {
                  st.setCond(5);
                  htmltext = "neurath_q711_10.htm";
               } else if (st.isCond(5)) {
                  htmltext = "neurath_q711_10.htm";
               } else if (st.isCond(6)) {
                  htmltext = "neurath_q711_11.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isPet) {
      QuestState st = player.getQuestState("_711_PathToBecomingALordInnadril");
      if (st != null && st.isCond(5)) {
         if (st.getInt("mobs") < 99) {
            st.set("mobs", String.valueOf(st.getInt("mobs") + 1));
         } else {
            st.setCond(6);
         }
      }

      return null;
   }

   private boolean isLordAvailable(int cond, QuestState qs) {
      Castle castle = CastleManager.getInstance().getCastleById(6);
      Clan owner = castle.getOwner();
      Player castleOwner = castle.getOwner().getLeader().getPlayerInstance();
      return owner != null
         && castleOwner != null
         && castleOwner != qs.getPlayer()
         && owner == qs.getPlayer().getClan()
         && castleOwner.getQuestState("_711_PathToBecomingALordInnadril") != null
         && castleOwner.getQuestState("_711_PathToBecomingALordInnadril").isCond(cond);
   }

   private boolean hasFort() {
      for(Fort fortress : FortManager.getInstance().getForts()) {
         if (fortress.getContractedCastleId() == 6) {
            return true;
         }
      }

      return false;
   }

   public static void main(String[] args) {
      new _711_PathToBecomingALordInnadril(711, "_711_PathToBecomingALordInnadril", "");
   }
}
