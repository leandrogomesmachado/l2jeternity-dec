package l2e.scripts.ai.primeval_isle;

import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.network.serverpackets.SocialAction;

public class SprigantPoison extends Fighter {
   private long _waitTime;
   private static final int TICK_IN_MILISECONDS = 15000;

   public SprigantPoison(Attackable actor) {
      super(actor);
   }

   @Override
   protected boolean thinkActive() {
      Attackable actor = this.getActiveChar();
      if (System.currentTimeMillis() > this._waitTime) {
         actor.setTarget(actor);
         actor.doCast(SkillsParser.getInstance().getInfo(5086, 1));
         this._waitTime = System.currentTimeMillis() + 15000L;
      }

      actor.broadcastPacket(new SocialAction(actor.getObjectId(), 1));
      super.thinkActive();
      return true;
   }
}
