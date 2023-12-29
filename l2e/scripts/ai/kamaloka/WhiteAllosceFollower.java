package l2e.scripts.ai.kamaloka;

import java.util.ArrayList;
import java.util.List;
import l2e.commons.util.Rnd;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.ai.npc.Mystic;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Player;

public class WhiteAllosceFollower extends Mystic {
   private long _skillTimer = 0L;
   private static final long _skillInterval = 15000L;

   public WhiteAllosceFollower(Attackable actor) {
      super(actor);
      actor.setIsInvul(true);
   }

   @Override
   protected boolean thinkActive() {
      Attackable actor = this.getActiveChar();
      if (this._skillTimer + 15000L < System.currentTimeMillis()) {
         List<Player> aggressionList = new ArrayList<>();

         for(Player p : World.getInstance().getAroundPlayers(actor, 1000, 200)) {
            if (!p.isDead() && !p.isInvisible()) {
               actor.addDamageHate(p, 0, 10);
               aggressionList.add(p.getActingPlayer());
            }
         }

         if (!aggressionList.isEmpty()) {
            Player aggressionTarget = aggressionList.get(Rnd.get(aggressionList.size()));
            if (aggressionTarget != null) {
               actor.setTarget(aggressionTarget);
               actor.doCast(SkillsParser.getInstance().getInfo(5624, 1));
            }
         }

         this.setIntention(CtrlIntention.ACTIVE);
         actor.getAI().setIntention(CtrlIntention.MOVING, Location.findPointToStay(actor, 400, true), Boolean.valueOf(false));
         this._skillTimer = System.currentTimeMillis() + Rnd.get(1L, 5000L);
      }

      return super.thinkActive();
   }

   @Override
   protected void thinkAttack() {
      this.setIntention(CtrlIntention.ACTIVE);
   }
}
