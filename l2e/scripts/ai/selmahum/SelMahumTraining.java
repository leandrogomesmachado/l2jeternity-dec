package l2e.scripts.ai.selmahum;

import l2e.commons.util.Rnd;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.network.serverpackets.SocialAction;
import org.apache.commons.lang3.ArrayUtils;

public class SelMahumTraining extends Fighter {
   private static final int[] _recruits = new int[]{22780, 22782, 22783, 22784, 22785};
   private long _waitTime = 0L;

   public SelMahumTraining(Attackable actor) {
      super(actor);
   }

   @Override
   public boolean thinkActive() {
      Attackable actor = this.getActiveChar();
      if (System.currentTimeMillis() > this._waitTime) {
         this._waitTime = System.currentTimeMillis() + (long)Rnd.get(10, 30) * 1000L;
         actor.broadcastPacket(new SocialAction(actor.getObjectId(), 7));
         switch(Rnd.get(1, 3)) {
            case 1:
               for(Npc npc : World.getInstance().getAroundNpc(actor, (int)(700.0 + actor.getColRadius()), 200)) {
                  if (npc.isMonster() && ArrayUtils.contains(_recruits, npc.getId())) {
                     npc.broadcastPacket(new SocialAction(npc.getObjectId(), 7));
                  }
               }
               break;
            case 2:
               for(Npc npc : World.getInstance().getAroundNpc(actor, (int)(700.0 + actor.getColRadius()), 200)) {
                  if (npc.isMonster() && ArrayUtils.contains(_recruits, npc.getId())) {
                     npc.broadcastPacket(new SocialAction(npc.getObjectId(), 4));
                  }
               }
               break;
            case 3:
               for(Npc npc : World.getInstance().getAroundNpc(actor, (int)(700.0 + actor.getColRadius()), 200)) {
                  if (npc.isMonster() && ArrayUtils.contains(_recruits, npc.getId())) {
                     npc.broadcastPacket(new SocialAction(npc.getObjectId(), 5));
                  }
               }
         }
      }

      return false;
   }
}
