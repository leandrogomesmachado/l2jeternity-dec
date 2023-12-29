package l2e.scripts.ai.pagan_temple;

import l2e.commons.util.Rnd;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.interfaces.ILocational;

public class AndreasCaptainRoyalGuard extends Fighter {
   private boolean _tele = false;
   public static final Location[] _locs = new Location[]{
      new Location(-16128, -35888, -10726), new Location(-17029, -39617, -10724), new Location(-15729, -42001, -10724)
   };

   public AndreasCaptainRoyalGuard(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtSpawn() {
      Attackable actor = this.getActiveChar();
      if (actor != null) {
         this._tele = Rnd.chance(10);
         super.onEvtSpawn();
      }
   }

   @Override
   protected boolean thinkActive() {
      Attackable actor = this.getActiveChar();
      if (actor != null && this._tele) {
         if (actor.getId() != 22188 && Rnd.chance(5)) {
            for(Player player : World.getInstance().getAroundPlayers(actor, 500, 500)) {
               if (player != null && player.isInParty() && player.getParty().getMemberCount() >= 9) {
                  this._tele = false;
                  player.teleToLocation(Rnd.get((ILocational[])_locs), true);
               }
            }
         }

         return true;
      } else {
         return true;
      }
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable actor = this.getActiveChar();
      if (actor != null) {
         if (actor.getId() != 22188 && attacker != null && actor.getCurrentHpPercents() <= 70.0) {
            actor.setTarget(attacker);
            actor.doCast(SkillsParser.getInstance().getInfo(4612, 9));
            actor.doDie(attacker);
         }

         super.onEvtAttacked(attacker, damage);
      }
   }
}
