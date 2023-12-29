package l2e.scripts.quests;

import l2e.commons.util.Rnd;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.SpawnParser;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.CreatureSay;

public final class _457_LostAndFound extends Quest {
   private static Spawner[] _escortCheckers = new Spawner[2];

   private _457_LostAndFound(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(32759);
      this.addFirstTalkId(32759);
      this.addTalkId(32759);
      this.addKillId(new int[]{22789, 22790, 22791, 22793});
      int i = 0;

      for(Spawner spawn : SpawnParser.getInstance().getSpawnData()) {
         if (spawn.getId() == 32764) {
            _escortCheckers[i] = spawn;
            ++i;
         }
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return getNoQuestMsg(player);
      } else {
         String htmltext = null;
         switch(event) {
            case "32759-06.htm":
               npc.setScriptValue(0);
               st.startQuest();
               npc.setTarget(player);
               npc.setWalking();
               npc.getAI().setIntention(CtrlIntention.FOLLOW, player);
               this.startQuestTimer("check", 1000L, npc, player, true);
               this.startQuestTimer("time_limit", 600000L, npc, player);
               this.startQuestTimer("talk_time", 120000L, npc, player);
               this.startQuestTimer("talk_time2", 30000L, npc, player);
               break;
            case "talk_time":
               this.broadcastNpcSay(npc, player, NpcStringId.AH_I_THINK_I_REMEMBER_THIS_PLACE, false);
               break;
            case "talk_time2":
               this.broadcastNpcSay(npc, player, NpcStringId.WHAT_WERE_YOU_DOING_HERE, false);
               this.startQuestTimer("talk_time3", 10000L, npc, player);
               break;
            case "talk_time3":
               this.broadcastNpcSay(npc, player, NpcStringId.I_GUESS_YOURE_THE_SILENT_TYPE_THEN_ARE_YOU_LOOKING_FOR_TREASURE_LIKE_ME, false);
               break;
            case "time_limit":
               this.startQuestTimer("stop", 2000L, npc, player);
               st.exitQuest(QuestState.QuestType.DAILY);
               break;
            case "check":
               double distance = Math.sqrt(npc.getPlanDistanceSq(player.getX(), player.getY()));
               if (distance > 1000.0) {
                  if (distance > 5000.0) {
                     this.startQuestTimer("stop", 2000L, npc, player);
                     st.exitQuest(QuestState.QuestType.DAILY);
                  } else if (npc.isScriptValue(0)) {
                     this.broadcastNpcSay(npc, player, NpcStringId.HEY_DONT_GO_SO_FAST, true);
                     npc.setScriptValue(1);
                  } else if (npc.isScriptValue(1)) {
                     this.broadcastNpcSay(npc, player, NpcStringId.ITS_HARD_TO_FOLLOW, true);
                     npc.setScriptValue(2);
                  } else if (npc.isScriptValue(2)) {
                     this.startQuestTimer("stop", 2000L, npc, player);
                     st.exitQuest(QuestState.QuestType.DAILY);
                  }
               }

               for(Spawner escortSpawn : _escortCheckers) {
                  Npc escort = escortSpawn.getLastSpawn();
                  if (escort != null && npc.isInsideRadius(escort, 1000, false, false)) {
                     this.startQuestTimer("stop", 1000L, npc, player);
                     this.startQuestTimer("bye", 3000L, npc, player);
                     this.cancelQuestTimer("check", npc, player);
                     npc.broadcastPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), NpcStringId.AH_FRESH_AIR), 2000);
                     this.broadcastNpcSay(npc, player, NpcStringId.AH_FRESH_AIR, false);
                     st.calcReward(this.getId());
                     st.exitQuest(QuestState.QuestType.DAILY, true);
                     return htmltext;
                  }
               }
               break;
            case "stop":
               npc.setTarget(null);
               npc.getAI().stopFollow();
               npc.getAI().setIntention(CtrlIntention.IDLE);
               this.cancelQuestTimer("check", npc, player);
               this.cancelQuestTimer("time_limit", npc, player);
               this.cancelQuestTimer("talk_time", npc, player);
               this.cancelQuestTimer("talk_time2", npc, player);
               break;
            case "bye":
               npc.deleteMe();
               break;
            default:
               htmltext = event;
         }

         return htmltext;
      }
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      if (npc.getTarget() != null) {
         return npc.getTarget().equals(player) ? "32759-08.htm" : "32759-01a.htm";
      } else {
         return "32759.htm";
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      switch(st.getState()) {
         case 0:
            htmltext = player.getLevel() >= 82 ? "32759-01.htm" : "32759-03.htm";
            break;
         case 2:
            if (st.isNowAvailable()) {
               st.setState((byte)0);
               htmltext = player.getLevel() >= 82 ? "32759-01.htm" : "32759-03.htm";
            } else {
               htmltext = "32759-02.htm";
            }
      }

      return htmltext;
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = this.getQuestState(player, true);
      if (st.isNowAvailable() && player.getLevel() >= 82 && Rnd.chance(1) && Rnd.chance(10)) {
         addSpawn(32759, npc);
      }

      return super.onKill(npc, player, isSummon);
   }

   public void broadcastNpcSay(Npc npc, Player player, NpcStringId stringId, boolean whisper) {
      ((Creature)(whisper ? player : npc)).sendPacket(new CreatureSay(npc.getObjectId(), whisper ? 2 : 0, npc.getId(), stringId));
   }

   public static void main(String[] args) {
      new _457_LostAndFound(457, _457_LostAndFound.class.getSimpleName(), "");
   }
}
