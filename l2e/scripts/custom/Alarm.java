package l2e.scripts.custom;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;
import l2e.scripts.ai.AbstractNpcAI;

public final class Alarm extends AbstractNpcAI {
   public Alarm(String name, String descr) {
      super(name, descr);
      this.addStartNpc(32367);
      this.addTalkId(32367);
      this.addFirstTalkId(32367);
      this.addSpawnId(new int[]{32367});
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = null;
      Player player0 = npc.getVariables().getObject("player0", Player.class);
      Npc npc0 = npc.getVariables().getObject("npc0", Npc.class);
      switch(event) {
         case "SELF_DESTRUCT_IN_60":
            this.startQuestTimer("SELF_DESTRUCT_IN_30", 30000L, npc, null);
            this.broadcastNpcSay(npc, 22, NpcStringId.THE_ALARM_WILL_SELF_DESTRUCT_IN_60_SECONDS_ENTER_PASSCODE_TO_OVERRIDE);
            break;
         case "SELF_DESTRUCT_IN_30":
            this.startQuestTimer("SELF_DESTRUCT_IN_10", 20000L, npc, null);
            this.broadcastNpcSay(npc, 22, NpcStringId.THE_ALARM_WILL_SELF_DESTRUCT_IN_30_SECONDS_ENTER_PASSCODE_TO_OVERRIDE);
            break;
         case "SELF_DESTRUCT_IN_10":
            this.startQuestTimer("RECORDER_CRUSHED", 10000L, npc, null);
            this.broadcastNpcSay(npc, 22, NpcStringId.THE_ALARM_WILL_SELF_DESTRUCT_IN_10_SECONDS_ENTER_PASSCODE_TO_OVERRIDE);
            break;
         case "RECORDER_CRUSHED":
            if (npc0 != null && npc0.getVariables().getBool("SPAWNED")) {
               npc0.getVariables().set("SPAWNED", false);
               if (player0 != null) {
                  this.broadcastNpcSay(npc, 22, NpcStringId.RECORDER_CRUSHED);
                  if (verifyMemoState(player0, 184, -1)) {
                     setMemoState(player0, 184, 5);
                  } else if (verifyMemoState(player0, 185, -1)) {
                     setMemoState(player0, 185, 5);
                  }
               }
            }

            npc.deleteMe();
            break;
         case "32367-184_04.htm":
         case "32367-184_06.htm":
         case "32367-184_08.htm":
            htmltext = event;
            break;
         case "2":
            if (player0 == player) {
               if (verifyMemoState(player, 184, 3)) {
                  htmltext = "32367-184_02.htm";
               } else if (verifyMemoState(player, 185, 3)) {
                  htmltext = "32367-185_02.htm";
               }
            }
            break;
         case "3":
            if (verifyMemoState(player, 184, 3)) {
               setMemoStateEx(player, 184, 1, 1);
               htmltext = "32367-184_04.htm";
            } else if (verifyMemoState(player, 185, 3)) {
               setMemoStateEx(player, 185, 1, 1);
               htmltext = "32367-185_04.htm";
            }
            break;
         case "4":
            if (verifyMemoState(player, 184, 3)) {
               setMemoStateEx(player, 184, 1, getMemoStateEx(player, 184, 1) + 1);
               htmltext = "32367-184_06.htm";
            } else if (verifyMemoState(player, 185, 3)) {
               setMemoStateEx(player, 185, 1, getMemoStateEx(player, 185, 1) + 1);
               htmltext = "32367-185_06.htm";
            }
            break;
         case "5":
            if (verifyMemoState(player, 184, 3)) {
               setMemoStateEx(player, 184, 1, getMemoStateEx(player, 184, 1) + 1);
               htmltext = "32367-184_08.htm";
            } else if (verifyMemoState(player, 185, 3)) {
               setMemoStateEx(player, 185, 1, getMemoStateEx(player, 185, 1) + 1);
               htmltext = "32367-185_08.htm";
            }
            break;
         case "6":
            if (verifyMemoState(player, 184, 3)) {
               int i0 = getMemoStateEx(player, 184, 1);
               if (i0 >= 3) {
                  if (npc0 != null && npc0.getVariables().getBool("SPAWNED")) {
                     npc0.getVariables().set("SPAWNED", false);
                  }

                  npc.deleteMe();
                  setMemoState(player, 184, 4);
                  htmltext = "32367-184_09.htm";
               } else {
                  setMemoStateEx(player, 184, 1, 0);
                  htmltext = "32367-184_10.htm";
               }
            } else if (verifyMemoState(player, 185, 3)) {
               int i0 = getMemoStateEx(player, 185, 1);
               if (i0 >= 3) {
                  if (npc0 != null && npc0.getVariables().getBool("SPAWNED")) {
                     npc0.getVariables().set("SPAWNED", false);
                  }

                  npc.deleteMe();
                  setMemoState(player, 185, 4);
                  htmltext = "32367-185_09.htm";
               } else {
                  setMemoStateEx(player, 185, 1, 0);
                  htmltext = "32367-185_10.htm";
               }
            }
      }

      return htmltext;
   }

   @Override
   public String onFirstTalk(Npc npc, Player talker) {
      String htmltext = getNoQuestMsg(talker);
      if (verifyMemoState(talker, 184, 3) || verifyMemoState(talker, 185, 3)) {
         Player player = npc.getVariables().getObject("player0", Player.class);
         if (player == talker) {
            htmltext = "32367-01.htm";
         } else {
            htmltext = "32367-02.htm";
         }
      }

      return htmltext;
   }

   @Override
   public String onSpawn(Npc npc) {
      this.startQuestTimer("SELF_DESTRUCT_IN_60", 60000L, npc, null);
      this.broadcastNpcSay(npc, 22, NpcStringId.INTRUDER_ALERT_THE_ALARM_WILL_SELF_DESTRUCT_IN_2_MINUTES);
      Player player = npc.getVariables().getObject("player0", Player.class);
      if (player != null) {
         playSound(player, Quest.QuestSound.ITEMSOUND_SIREN);
      }

      return super.onSpawn(npc);
   }

   private static final boolean verifyMemoState(Player player, int questId, int memoState) {
      QuestState qs = null;
      switch(questId) {
         case 184:
            qs = player.getQuestState("_184_NikolasCooperationContract");
            break;
         case 185:
            qs = player.getQuestState("_185_NikolasCooperationConsideration");
      }

      return qs != null && (memoState < 0 || qs.isMemoState(memoState));
   }

   private static final void setMemoState(Player player, int questId, int memoState) {
      QuestState qs = null;
      switch(questId) {
         case 184:
            qs = player.getQuestState("_184_NikolasCooperationContract");
            break;
         case 185:
            qs = player.getQuestState("_185_NikolasCooperationConsideration");
      }

      if (qs != null) {
         qs.setMemoState(memoState);
      }
   }

   private static final int getMemoStateEx(Player player, int questId, int slot) {
      QuestState qs = null;
      switch(questId) {
         case 184:
            qs = player.getQuestState("_184_NikolasCooperationContract");
            break;
         case 185:
            qs = player.getQuestState("_185_NikolasCooperationConsideration");
      }

      return qs != null ? qs.getMemoStateEx(slot) : -1;
   }

   private static final void setMemoStateEx(Player player, int questId, int slot, int memoStateEx) {
      QuestState qs = null;
      switch(questId) {
         case 184:
            qs = player.getQuestState("_184_NikolasCooperationContract");
            break;
         case 185:
            qs = player.getQuestState("_185_NikolasCooperationConsideration");
      }

      if (qs != null) {
         qs.setMemoStateEx(slot, memoStateEx);
      }
   }

   public static void main(String[] args) {
      new Alarm(Alarm.class.getSimpleName(), "custom");
   }
}
