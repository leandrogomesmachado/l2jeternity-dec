package l2e.scripts.ai.gracia;

import java.util.concurrent.ScheduledFuture;
import l2e.commons.threading.RunnableImpl;
import l2e.commons.util.Rnd;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.parser.SpawnParser;
import l2e.gameserver.instancemanager.ServerVariables;
import l2e.gameserver.instancemanager.SoDManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.QuestGuardInstance;
import l2e.gameserver.network.NpcStringId;
import l2e.scripts.ai.AbstractNpcAI;

public class SoDDefenceStage extends AbstractNpcAI {
   private int controllerKills = 0;
   private int portalKills = 0;
   private static int defenceStage = 0;
   private long timeLimit = 0L;
   private ScheduledFuture<?> _respawnTime = null;
   private ScheduledFuture<?> _timeLimit = null;

   private SoDDefenceStage(String name, String descr) {
      super(name, descr);
      this.addKillId(new int[]{18702, 18775});
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      if (!event.equalsIgnoreCase("StartDefence")) {
         if (event.equalsIgnoreCase("EndDefence")) {
            this.controllerKills = 0;
            defenceStage = 0;
            if (this._respawnTime != null) {
               this._respawnTime.cancel(false);
               this._respawnTime = null;
            }

            if (this._timeLimit != null) {
               this._timeLimit.cancel(false);
               this._timeLimit = null;
            }

            SpawnParser.getInstance().despawnGroup("sod_defence_controller");
            SpawnParser.getInstance().despawnGroup("sod_defence_portal_1stage");
            SpawnParser.getInstance().despawnGroup("sod_defence_portal_2stage");
            SpawnParser.getInstance().despawnGroup("sod_defence_tiat");
            SoDManager.closeSeed();
            return null;
         } else {
            return super.onAdvEvent(event, npc, player);
         }
      } else {
         if (SoDManager.isOpened()) {
            this.timeLimit = ServerVariables.getLong("SoD_defence", 0L) * 1000L;
            int lastHours = (int)((this.timeLimit - System.currentTimeMillis()) / 3600000L);
            if (lastHours >= 10 && lastHours < 11) {
               defenceStage = 2;
            } else if (lastHours < 10) {
               defenceStage = 3;
            } else {
               defenceStage = 1;
            }

            SpawnParser.getInstance().spawnGroup("sod_defence_controller");
            this.printScreenMsg(NpcStringId.TIATS_FOLLOWERS_ARE_COMING_TO_RETAKE_THE_SEED_OF_DESTRUCTION_GET_READY_TO_STOP_THE_ENEMIES);
            this.checkStageStatus();
            if (this._timeLimit != null) {
               this._timeLimit.cancel(false);
               this._timeLimit = null;
            }

            this._timeLimit = ThreadPoolManager.getInstance().schedule(new SoDDefenceStage.ChangeToHardStatus(), this.timeLimit - System.currentTimeMillis());
         }

         return null;
      }
   }

   @Override
   public final String onSpawn(Npc npc) {
      switch(npc.getId()) {
         case 18775:
            ((QuestGuardInstance)npc).setPassive(false);
         default:
            return super.onSpawn(npc);
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      if (npc.getId() == 18702) {
         ++this.portalKills;
         if (this.portalKills >= 3) {
            switch(defenceStage) {
               case 1:
                  ++defenceStage;
                  if (Rnd.chance(40)) {
                     ++defenceStage;
                  }
                  break;
               case 2:
                  ++defenceStage;
               case 3:
               case 4:
            }

            if (this._respawnTime != null) {
               this._respawnTime.cancel(false);
               this._respawnTime = null;
            }

            this._respawnTime = ThreadPoolManager.getInstance().schedule(new SoDDefenceStage.CheckPortalStatus(), this.calcRespawnTime());
         }
      } else if (npc.getId() == 18775) {
         ++this.controllerKills;
         if (this.controllerKills >= 2) {
            this.notifyEvent("EndDefence", null, null);
         }
      }

      return super.onKill(npc, player, isSummon);
   }

   @Override
   public String onKillByMob(Npc npc, Npc killer) {
      if (npc.getId() == 18775) {
         ++this.controllerKills;
         if (this.controllerKills >= 2) {
            this.notifyEvent("EndDefence", null, null);
         }
      }

      return super.onKillByMob(npc, killer);
   }

   private long calcRespawnTime() {
      switch(defenceStage) {
         case 1:
            return (long)(Rnd.get(20, 40) * 60000);
         case 2:
            return (long)(Rnd.get(30, 60) * 60000);
         case 3:
            return (long)(Rnd.get(30, 180) * 60000);
         case 4:
            return (long)(Rnd.get(2, 10) * 60000);
         default:
            return 0L;
      }
   }

   private void checkStageStatus() {
      switch(defenceStage) {
         case 1:
            SpawnParser.getInstance().spawnGroup("sod_defence_portal_1stage");
            SpawnParser.getInstance().spawnGroup("sod_defence_tiat");
            break;
         case 2:
            SpawnParser.getInstance().spawnGroup("sod_defence_portal_1stage");
            break;
         case 3:
         case 4:
            SpawnParser.getInstance().despawnGroup("sod_defence_portal_2stage");
            SpawnParser.getInstance().spawnGroup("sod_defence_portal_2stage");
      }
   }

   private void printScreenMsg(NpcStringId stringId) {
      for(Player player : ZoneManager.getInstance().getZoneById(60009).getPlayersInside()) {
         if (player != null) {
            showOnScreenMsg(player, stringId, 2, 5000, new String[0]);
         }
      }
   }

   public static int getDefenceStage() {
      return defenceStage;
   }

   public static void main(String[] args) {
      new SoDDefenceStage(SoDDefenceStage.class.getSimpleName(), "ai");
   }

   private class ChangeToHardStatus extends RunnableImpl {
      private ChangeToHardStatus() {
      }

      @Override
      public void runImpl() {
         SoDDefenceStage.defenceStage = 4;
         if (SoDDefenceStage.this._respawnTime != null) {
            SoDDefenceStage.this._respawnTime.cancel(false);
            SoDDefenceStage.this._respawnTime = null;
         }

         SoDDefenceStage.this._respawnTime = ThreadPoolManager.getInstance()
            .schedule(SoDDefenceStage.this.new CheckPortalStatus(), SoDDefenceStage.this.calcRespawnTime());
      }
   }

   private class CheckPortalStatus extends RunnableImpl {
      private CheckPortalStatus() {
      }

      @Override
      public void runImpl() {
         SoDDefenceStage.this.portalKills = 0;
         SoDDefenceStage.this.printScreenMsg(NpcStringId.TIATS_FOLLOWERS_ARE_COMING_TO_RETAKE_THE_SEED_OF_DESTRUCTION_GET_READY_TO_STOP_THE_ENEMIES);
         SoDDefenceStage.this.checkStageStatus();
      }
   }
}
