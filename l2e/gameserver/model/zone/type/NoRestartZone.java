package l2e.gameserver.model.zone.type;

import l2e.gameserver.GameServer;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.tasks.player.TeleportToTownTask;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.ZoneType;

public class NoRestartZone extends ZoneType {
   private int _restartAllowedTime = 0;
   private int _restartTime = 0;
   private boolean _enabled = true;

   public NoRestartZone(int id) {
      super(id);
      this.addZoneId(ZoneId.NO_RESTART);
   }

   @Override
   public void setParameter(String name, String value) {
      if (name.equalsIgnoreCase("default_enabled")) {
         this._enabled = Boolean.parseBoolean(value);
      } else if (name.equalsIgnoreCase("restartAllowedTime")) {
         this._restartAllowedTime = Integer.parseInt(value) * 1000;
      } else if (name.equalsIgnoreCase("restartTime")) {
         this._restartTime = Integer.parseInt(value) * 1000;
      } else if (!name.equalsIgnoreCase("instanceId")) {
         super.setParameter(name, value);
      }
   }

   @Override
   protected void onEnter(Creature character) {
      if (this._enabled) {
         ;
      }
   }

   @Override
   protected void onExit(Creature character) {
      if (this._enabled) {
         ;
      }
   }

   @Override
   public void onPlayerLoginInside(Player player) {
      if (this._enabled && !player.isInFightEvent()) {
         if (System.currentTimeMillis() - player.getLastAccess() > (long)this.getRestartTime()
            && System.currentTimeMillis() - GameServer.dateTimeServerStarted.getTimeInMillis() > (long)this.getRestartAllowedTime()) {
            ThreadPoolManager.getInstance().schedule(new TeleportToTownTask(player), 2000L);
         }
      }
   }

   public int getRestartAllowedTime() {
      return this._restartAllowedTime;
   }

   public void setRestartAllowedTime(int time) {
      this._restartAllowedTime = time;
   }

   public int getRestartTime() {
      return this._restartTime;
   }

   public void setRestartTime(int time) {
      this._restartTime = time;
   }
}
