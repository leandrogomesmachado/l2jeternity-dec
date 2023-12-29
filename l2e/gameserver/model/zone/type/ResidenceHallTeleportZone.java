package l2e.gameserver.model.zone.type;

import java.util.concurrent.ScheduledFuture;
import l2e.commons.util.Rnd;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.zone.ZoneId;

public class ResidenceHallTeleportZone extends ResidenceTeleportZone {
   private int _id;
   private ScheduledFuture<?> _teleTask;

   public ResidenceHallTeleportZone(int id) {
      super(id);
      this.addZoneId(ZoneId.NO_SUMMON_FRIEND);
   }

   @Override
   public void setParameter(String name, String value) {
      if (name.equals("residenceZoneId")) {
         this._id = Integer.parseInt(value);
      } else {
         super.setParameter(name, value);
      }
   }

   public int getResidenceZoneId() {
      return this._id;
   }

   public synchronized void checkTeleporTask() {
      if (this._teleTask == null || this._teleTask.isDone()) {
         this._teleTask = ThreadPoolManager.getInstance().schedule(new ResidenceHallTeleportZone.TeleportTask(), 30000L);
      }
   }

   protected class TeleportTask implements Runnable {
      @Override
      public void run() {
         int index = 0;
         if (ResidenceHallTeleportZone.this.getSpawns().size() > 1) {
            index = Rnd.get(ResidenceHallTeleportZone.this.getSpawns().size());
         }

         Location loc = ResidenceHallTeleportZone.this.getSpawns().get(index);
         if (loc == null) {
            throw new NullPointerException();
         } else {
            for(Player pc : ResidenceHallTeleportZone.this.getPlayersInside()) {
               if (pc != null) {
                  pc.teleToLocation(loc, false);
               }
            }
         }
      }
   }
}
