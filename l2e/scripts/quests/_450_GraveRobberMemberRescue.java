package l2e.scripts.quests;

import java.util.Calendar;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class _450_GraveRobberMemberRescue extends Quest {
   private static final String qn = "_450_GraveRobberMemberRescue";
   private static final int KANEMIKA = 32650;
   private static final int WARRIOR_NPC = 32651;
   private static final int WARRIOR_MON = 22741;
   private static final int EVIDENCE_OF_MIGRATION = 14876;
   private static final int RESET_HOUR = 6;
   private static final int RESET_MIN = 30;

   public _450_GraveRobberMemberRescue(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(32650);
      this.addTalkId(32650);
      this.addTalkId(32651);
      this.questItemIds = new int[]{14876};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_450_GraveRobberMemberRescue");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32650-05.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_450_GraveRobberMemberRescue");
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         if (npc.getId() == 32650) {
            if (cond == 0) {
               String reset = st.get("reset");
               long remain = 0L;
               if (reset != null && this.isDigit(reset)) {
                  remain = Long.parseLong(reset) - System.currentTimeMillis();
               }

               if (remain <= 0L) {
                  if (player.getLevel() >= 80) {
                     htmltext = "32650-01.htm";
                  } else {
                     htmltext = "32650-00.htm";
                     st.exitQuest(true);
                  }
               } else {
                  htmltext = "32650-09.htm";
               }
            } else if (cond == 1) {
               if (st.getQuestItemsCount(14876) >= 1L) {
                  htmltext = "32650-07.htm";
               } else {
                  htmltext = "32650-06.htm";
               }
            } else if (cond == 2 && st.getQuestItemsCount(14876) == 10L) {
               htmltext = "32650-08.htm";
               st.giveItems(57, 65000L);
               st.takeItems(14876, 10L);
               st.setState((byte)2);
               st.unset("cond");
               st.exitQuest(false);
               st.playSound("ItemSound.quest_finish");
               Calendar reset = Calendar.getInstance();
               reset.set(12, 30);
               if (reset.get(11) >= 6) {
                  reset.add(5, 1);
               }

               reset.set(11, 6);
               st.set("reset", String.valueOf(reset.getTimeInMillis()));
            }
         } else if (cond == 1 && npc.getId() == 32651) {
            if (getRandom(100) < 50) {
               htmltext = "32651-01.htm";
               st.giveItems(14876, 1L);
               st.playSound("ItemSound.quest_itemget");
               npc.getAI().setIntention(CtrlIntention.MOVING, new Location(npc.getX() + 100, npc.getY() + 100, npc.getZ(), 0));
               npc.getSpawn().decreaseCount(npc);
               npc.deleteMe();
               if (st.getQuestItemsCount(14876) == 10L) {
                  st.set("cond", "2");
                  st.playSound("ItemSound.quest_middle");
               }
            } else {
               htmltext = "";
               Npc warrior = st.addSpawn(22741, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 600000);
               warrior.setRunning();
               ((Attackable)warrior).addDamageHate(player, 0, 999);
               warrior.getAI().setIntention(CtrlIntention.ATTACK, player);
               showOnScreenMsg(player, NpcStringId.THE_GRAVE_ROBBER_WARRIOR_HAS_BEEN_FILLED_WITH_DARK_ENERGY_AND_IS_ATTACKING_YOU, 5, 5000, new String[0]);
               if (getRandom(100) < 50) {
                  npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.GRUNT_OH), 2000);
               } else {
                  npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.GRUNT_WHATS_WRONG_WITH_ME), 2000);
               }

               npc.getSpawn().decreaseCount(npc);
               npc.deleteMe();
            }
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _450_GraveRobberMemberRescue(450, "_450_GraveRobberMemberRescue", "");
   }
}
