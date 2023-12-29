package l2e.scripts.ai.selmahum;

import java.util.ArrayList;
import java.util.List;
import l2e.commons.util.Rnd;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Npc;

public class SelMahumChef extends Fighter {
   private Location _targetLoc;
   private long _waitTime = 0L;

   public SelMahumChef(Attackable actor) {
      super(actor);
      actor.setIsRunner(true);
      actor.setCanReturnToSpawnPoint(false);
      actor.setIsGlobalAI(true);
   }

   @Override
   protected void onEvtSpawn() {
      this.getActiveChar().getAI().enableAI();
      super.onEvtSpawn();
   }

   @Override
   protected boolean thinkActive() {
      Attackable actor = this.getActiveChar();
      if (actor.isDead()) {
         return true;
      } else if (System.currentTimeMillis() > this._waitTime) {
         this._waitTime = System.currentTimeMillis() + 2000L;
         actor.setWalking();
         this._targetLoc = this.findFirePlace(actor);
         actor.getAI().setIntention(CtrlIntention.MOVING, this._targetLoc);
         return true;
      } else {
         return false;
      }
   }

   private Location findFirePlace(Attackable actor) {
      new Location(0, 0, 0);
      List<Npc> list = new ArrayList<>();

      for(Npc npc : World.getInstance().getAroundNpc(actor, (int)(3000.0 + actor.getColRadius()), 400)) {
         if (npc.getId() == 18927 && GeoEngine.canSeeTarget(actor, npc, false)) {
            list.add(npc);
         }
      }

      Location loc;
      if (!list.isEmpty()) {
         loc = list.get(Rnd.get(list.size())).getLocation();
      } else {
         loc = Location.findPointToStay(actor, 1000, 1500, true);
      }

      return loc;
   }
}
