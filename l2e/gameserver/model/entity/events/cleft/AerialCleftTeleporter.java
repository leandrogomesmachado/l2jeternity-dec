package l2e.gameserver.model.entity.events.cleft;

import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;

public class AerialCleftTeleporter implements Runnable {
   private Player _player = null;
   private Location[] _coordinates = new Location[3];
   private boolean _exitEvent = false;

   public AerialCleftTeleporter(Player player, Location[] coordinates, boolean fastSchedule, boolean exitEvent) {
      this._player = player;
      this._coordinates = coordinates;
      this._exitEvent = exitEvent;
      long delay = (long)((AerialCleftEvent.getInstance().isStarted() ? Config.CLEFT_RESPAWN_DELAY : Config.CLEFT_LEAVE_DELAY) * 1000);
      ThreadPoolManager.getInstance().schedule(this, fastSchedule ? 0L : delay);
   }

   @Override
   public void run() {
      if (this._player != null) {
         Summon summon = this._player.getSummon();
         if (summon != null) {
            summon.unSummon(this._player);
         }

         if (this._player.getTeam() == 0 || this._player.isInDuel() && this._player.getDuelState() != 4) {
            this._player.stopAllEffectsExceptThoseThatLastThroughDeath();
         }

         if (this._player.isInDuel()) {
            this._player.setDuelState(4);
         }

         this._player.setReflectionId(0);
         this._player.doRevive();
         Location rndLoc = this._coordinates[Rnd.get(3)];
         this._player.teleToLocation(rndLoc.getX() + Rnd.get(101) - 50, rndLoc.getY() + Rnd.get(101) - 50, rndLoc.getZ(), false);
         if ((AerialCleftEvent.getInstance().isStarted() || AerialCleftEvent.getInstance().isRewarding()) && !this._exitEvent) {
            this._player.setTeam(AerialCleftEvent.getInstance().getParticipantTeamId(this._player.getObjectId()) + 1);
         } else {
            this._player.setTeam(0);
            this._player.cleanCleftStats();
            this._player.cleanBlockSkills();
            this._player.sendSkillList(false);
         }

         this._player.setCurrentCp(this._player.getMaxCp());
         this._player.setCurrentHp(this._player.getMaxHp());
         this._player.setCurrentMp(this._player.getMaxMp());
         this._player.broadcastStatusUpdate();
         this._player.broadcastUserInfo(true);
      }
   }
}
