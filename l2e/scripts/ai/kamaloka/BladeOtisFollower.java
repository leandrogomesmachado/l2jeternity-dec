package l2e.scripts.ai.kamaloka;

import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.ai.npc.Mystic;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class BladeOtisFollower extends Mystic {
   private long _skillTimer = 0L;
   private static final long _skillInterval = 20000L;

   public BladeOtisFollower(Attackable actor) {
      super(actor);
   }

   @Override
   protected boolean thinkActive() {
      Attackable actor = this.getActiveChar();
      if (this._skillTimer == 0L) {
         this._skillTimer = System.currentTimeMillis();
      }

      if (this._skillTimer + 20000L < System.currentTimeMillis()) {
         Npc boss = null;

         for(Npc npc : World.getInstance().getAroundNpc(actor)) {
            if (npc.getId() == 18562) {
               boss = npc;
            }
         }

         if (boss != null) {
            actor.setTarget(boss);
            actor.doCast(SkillsParser.getInstance().getInfo(4209, 6));
            this._skillTimer = System.currentTimeMillis();
            this.setIntention(CtrlIntention.ACTIVE);
            actor.getAI().setIntention(CtrlIntention.MOVING, Location.findPointToStay(actor, 300, true), Boolean.valueOf(false));
            actor.broadcastPacket(new NpcSay(actor.getObjectId(), 0, actor.getId(), NpcStringId.THERES_NOT_MUCH_I_CAN_DO_BUT_I_WANT_TO_HELP_YOU), 2000);
         }
      }

      return super.thinkActive();
   }

   @Override
   protected void thinkAttack() {
      this.setIntention(CtrlIntention.ACTIVE);
   }
}
