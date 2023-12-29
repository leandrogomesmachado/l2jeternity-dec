package l2e.gameserver.listener.events;

import l2e.gameserver.listener.EventListener;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.events.cleft.AerialCleftTeam;

public class AerialCleftKillEvent implements EventListener {
   private Player _killer;
   private Player _victim;
   private AerialCleftTeam _killerTeam;

   public Player getKiller() {
      return this._killer;
   }

   public void setKiller(Player killer) {
      this._killer = killer;
   }

   public Player getVictim() {
      return this._victim;
   }

   public void setVictim(Player victim) {
      this._victim = victim;
   }

   public AerialCleftTeam getKillerTeam() {
      return this._killerTeam;
   }

   public void setKillerTeam(AerialCleftTeam killerTeam) {
      this._killerTeam = killerTeam;
   }
}
