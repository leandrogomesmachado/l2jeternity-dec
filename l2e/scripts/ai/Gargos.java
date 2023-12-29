package l2e.scripts.ai;

import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class Gargos extends Fighter {
   private long _lastFire;

   public Gargos(Attackable actor) {
      super(actor);
   }

   @Override
   protected boolean thinkActive() {
      return super.thinkActive() || this.thinkFire();
   }

   protected boolean thinkFire() {
      if (System.currentTimeMillis() - this._lastFire > 60000L) {
         Attackable actor = this.getActiveChar();
         actor.broadcastPacket(
            new NpcSay(actor.getObjectId(), 0, actor.getId(), NpcStringId.STEP_FORWARD_YOU_WORTHLESS_CREATURES_WHO_CHALLENGE_MY_AUTHORITY), 2000
         );
         actor.setTarget(actor);
         actor.doCast(SkillsParser.getInstance().getInfo(5705, 1));
         this._lastFire = System.currentTimeMillis();
         return true;
      } else {
         return false;
      }
   }
}
