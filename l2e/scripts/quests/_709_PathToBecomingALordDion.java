package l2e.scripts.quests;

import l2e.commons.util.Rnd;
import l2e.commons.util.Util;
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

public class _709_PathToBecomingALordDion extends Quest {
   private static final String qn = "_709_PathToBecomingALordDion";
   private static final int CROSBY = 35142;
   private static final int ROUKE = 31418;
   private static final int SOPHIA = 30735;
   private static final int MANDRADORA_ROOT = 13849;
   private static final int BLOODY_AXE_BLACK_EPAULETTE = 13850;
   private static final int BLOODY_AXE_SUBORDINATE = 27392;
   private static final int[] OL_MAHUMS = new int[]{20208, 20209, 20210, 20211, 27392};
   private static final int[] MANRAGORAS = new int[]{20154, 20155, 20156};
   private static final int CASTLE_ID = 2;

   public _709_PathToBecomingALordDion(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(35142);
      this.addTalkId(35142);
      this.addTalkId(30735);
      this.addTalkId(31418);
      this.addKillId(OL_MAHUMS);
      this.addKillId(MANRAGORAS);
      this.registerQuestItems(new int[]{13849, 13850});
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_709_PathToBecomingALordDion");
      Castle castle = CastleManager.getInstance().getCastleById(2);
      if (castle.getOwner() == null) {
         return "Castle has no lord";
      } else {
         Player castleOwner = castle.getOwner().getLeader().getPlayerInstance();
         if (event.equals("35142-04.htm")) {
            st.startQuest();
         } else if (event.equals("35142-12.htm")) {
            if (this.isLordAvailable(2, st)) {
               castleOwner.getQuestState("_709_PathToBecomingALordDion").set("confidant", String.valueOf(player.getObjectId()));
               castleOwner.getQuestState("_709_PathToBecomingALordDion").setCond(3);
               st.setState((byte)1);
            } else {
               htmltext = "35142-09b.htm";
            }
         } else if (event.equals("35142-11.htm")) {
            if (this.isLordAvailable(3, st)) {
               castleOwner.getQuestState("_709_PathToBecomingALordDion").setCond(4);
            } else {
               htmltext = "35142-09b.htm";
            }
         } else if (event.equals("30735-02.htm")) {
            st.set("cond", "6");
         } else if (event.equals("30735-05.htm")) {
            takeItems(player, 13850, 1L);
            st.set("cond", "8");
         } else if (event.equals("31418-09.htm")) {
            if (this.isLordAvailable(8, st)) {
               takeItems(player, 13849, -1L);
               castleOwner.getQuestState("_709_PathToBecomingALordDion").setCond(9);
            }
         } else if (event.equals("35142-23.htm")) {
            if (castle.getSiege().getIsInProgress()) {
               return "35142-22a.htm";
            }

            for(Fort fort : FortManager.getInstance().getForts()) {
               if (!fort.isBorderFortress() && fort.getSiege().getIsInProgress()) {
                  return "35142-22a.htm";
               }

               if (!fort.isBorderFortress() && fort.getContractedCastleId() != 2) {
                  return "35142-22b.htm";
               }
            }

            NpcSay packet = new NpcSay(npc.getObjectId(), 23, npc.getId(), NpcStringId.S1_HAS_BECOME_LORD_OF_THE_TOWN_OF_DION_LONG_MAY_HE_REIGN);
            packet.addStringParameter(player.getName());
            npc.broadcastPacket(packet);
            castle.getTerritory().changeOwner(castle.getOwner());
            st.exitQuest(true, true);
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = this.getQuestState(player, true);
      String htmltext = getNoQuestMsg(player);
      Castle castle = CastleManager.getInstance().getCastleById(2);
      if (castle.getOwner() == null) {
         return "Castle has no lord";
      } else {
         Player castleOwner = castle.getOwner().getLeader().getPlayerInstance();
         switch(npc.getId()) {
            case 30735:
               if (st.isCond(5)) {
                  htmltext = "30735-01.htm";
               } else if (st.isCond(6)) {
                  htmltext = "30735-03.htm";
               } else if (st.isCond(7)) {
                  htmltext = "30735-04.htm";
               } else if (st.isCond(8)) {
                  htmltext = "30735-07.htm";
               }
               break;
            case 31418:
               if (st.getState() == 1 && st.isCond(0) && this.isLordAvailable(3, st)) {
                  if (castleOwner.getQuestState("_709_PathToBecomingALordDion").getInt("confidant") == player.getObjectId()) {
                     htmltext = "31418-03.htm";
                  }
               } else if (st.getState() == 1 && st.isCond(0) && this.isLordAvailable(8, st)) {
                  if (getQuestItemsCount(player, 13849) >= 100L) {
                     htmltext = "31418-08.htm";
                  } else {
                     htmltext = "31418-07.htm";
                  }
               } else if (st.getState() == 1 && st.isCond(0) && this.isLordAvailable(9, st)) {
                  htmltext = "31418-12.htm";
               }
               break;
            case 35142:
               if (st.isCond(0)) {
                  if (castleOwner == player) {
                     if (!this.hasFort() && castle.getTerritory().getLordObjectId() != player.getObjectId()) {
                        htmltext = "35142-01.htm";
                     } else {
                        htmltext = "35142-03.htm";
                        st.exitQuest(true);
                     }
                  } else if (this.isLordAvailable(2, st)) {
                     if (castleOwner.calculateDistance(npc, false, false) <= 200.0) {
                        htmltext = "35142-11.htm";
                     } else {
                        htmltext = "35142-09b.htm";
                     }
                  } else {
                     htmltext = "35142-09a.htm";
                     st.exitQuest(true);
                  }
               } else if (st.isCond(1)) {
                  st.set("cond", "2");
                  htmltext = "35142-08.htm";
               } else if (st.isCond(2) || st.isCond(3)) {
                  htmltext = "35142-14.htm";
               } else if (st.isCond(4)) {
                  st.set("cond", "5");
                  htmltext = "35142-16.htm";
               } else if (st.isCond(5)) {
                  htmltext = "35142-16.htm";
               } else if (st.getCond() > 5 && st.getCond() < 9) {
                  htmltext = "35142-15.htm";
               } else if (st.isCond(9)) {
                  htmltext = "35142-22.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player killer, boolean isSummon) {
      QuestState st = killer.getQuestState("_709_PathToBecomingALordDion");
      if (st != null && st.isCond(6) && Util.contains(OL_MAHUMS, npc.getId())) {
         if (npc.getId() != 27392 && Rnd.get(9) == 0) {
            addSpawn(27392, npc.getLocation(), true, 300000L);
         } else if (npc.getId() == 27392) {
            giveItems(killer, 13850, 1L);
            st.setCond(7);
         }
      }

      if (st != null
         && st.getState() == 1
         && st.isCond(0)
         && this.isLordAvailable(8, st)
         && Util.contains(MANRAGORAS, npc.getId())
         && getQuestItemsCount(killer, 13849) < 100L) {
         giveItems(killer, 13849, 1L);
      }

      return super.onKill(npc, killer, isSummon);
   }

   private boolean isLordAvailable(int cond, QuestState st) {
      Castle castle = CastleManager.getInstance().getCastleById(2);
      Clan owner = castle.getOwner();
      Player castleOwner = castle.getOwner().getLeader().getPlayerInstance();
      return owner != null
         && castleOwner != null
         && castleOwner != st.getPlayer()
         && owner == st.getPlayer().getClan()
         && castleOwner.getQuestState("_709_PathToBecomingALordDion") != null
         && castleOwner.getQuestState("_709_PathToBecomingALordDion").isCond(cond);
   }

   private boolean hasFort() {
      for(Fort fortress : FortManager.getInstance().getForts()) {
         if (fortress.getContractedCastleId() == 2) {
            return true;
         }
      }

      return false;
   }

   public static void main(String[] args) {
      new _709_PathToBecomingALordDion(709, "_709_PathToBecomingALordDion", "");
   }
}
