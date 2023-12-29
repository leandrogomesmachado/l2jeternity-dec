package l2e.gameserver.model.actor.instance;

import java.util.concurrent.Future;
import java.util.logging.Level;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.stat.ControllableAirShipStat;
import l2e.gameserver.model.actor.templates.character.CharTemplate;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.DeleteObject;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class ControllableAirShipInstance extends AirShipInstance {
   private static final int HELM = 13556;
   private static final int LOW_FUEL = 40;
   private int _fuel = 0;
   private int _maxFuel = 0;
   private final int _ownerId;
   private int _helmId;
   private Player _captain = null;
   private Future<?> _consumeFuelTask;
   private Future<?> _checkTask;

   public ControllableAirShipInstance(int objectId, CharTemplate template, int ownerId) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.ControllableAirShipInstance);
      this._ownerId = ownerId;
      this._helmId = IdFactory.getInstance().getNextId();
   }

   public ControllableAirShipStat getStat() {
      return (ControllableAirShipStat)super.getStat();
   }

   @Override
   public void initCharStat() {
      this.setStat(new ControllableAirShipStat(this));
   }

   @Override
   public boolean canBeControlled() {
      return super.canBeControlled() && !this.isInDock();
   }

   @Override
   public boolean isOwner(Player player) {
      if (this._ownerId == 0) {
         return false;
      } else {
         return player.getClanId() == this._ownerId || player.getObjectId() == this._ownerId;
      }
   }

   @Override
   public int getOwnerId() {
      return this._ownerId;
   }

   @Override
   public boolean isCaptain(Player player) {
      return this._captain != null && player == this._captain;
   }

   @Override
   public int getCaptainId() {
      return this._captain != null ? this._captain.getObjectId() : 0;
   }

   @Override
   public int getHelmObjectId() {
      return this._helmId;
   }

   @Override
   public int getHelmItemId() {
      return 13556;
   }

   @Override
   public boolean setCaptain(Player player) {
      if (player == null) {
         this._captain = null;
      } else {
         if (this._captain != null || player.getAirShip() != this) {
            return false;
         }

         int x = player.getInVehiclePosition().getX() - 366;
         int y = player.getInVehiclePosition().getY();
         int z = player.getInVehiclePosition().getZ() - 107;
         if (x * x + y * y + z * z > 2500) {
            player.sendPacket(SystemMessageId.CANT_CONTROL_TOO_FAR);
            return false;
         }

         if (player.isInCombat()) {
            player.sendPacket(SystemMessageId.YOU_CANNOT_CONTROL_THE_HELM_WHILE_IN_A_BATTLE);
            return false;
         }

         if (player.isSitting()) {
            player.sendPacket(SystemMessageId.YOU_CANNOT_CONTROL_THE_HELM_WHILE_IN_A_SITTING_POSITION);
            return false;
         }

         if (player.isParalyzed()) {
            player.sendPacket(SystemMessageId.YOU_CANNOT_CONTROL_THE_HELM_WHILE_YOU_ARE_PETRIFIED);
            return false;
         }

         if (player.isCursedWeaponEquipped()) {
            player.sendPacket(SystemMessageId.YOU_CANNOT_CONTROL_THE_HELM_WHILE_A_CURSED_WEAPON_IS_EQUIPPED);
            return false;
         }

         if (player.isFishing()) {
            player.sendPacket(SystemMessageId.YOU_CANNOT_CONTROL_THE_HELM_WHILE_FISHING);
            return false;
         }

         if (player.isDead() || player.isFakeDeathNow()) {
            player.sendPacket(SystemMessageId.YOU_CANNOT_CONTROL_THE_HELM_WHEN_YOU_ARE_DEAD);
            return false;
         }

         if (player.isCastingNow()) {
            player.sendPacket(SystemMessageId.YOU_CANNOT_CONTROL_THE_HELM_WHILE_USING_A_SKILL);
            return false;
         }

         if (player.isTransformed()) {
            player.sendPacket(SystemMessageId.YOU_CANNOT_CONTROL_THE_HELM_WHILE_TRANSFORMED);
            return false;
         }

         if (player.isCombatFlagEquipped()) {
            player.sendPacket(SystemMessageId.YOU_CANNOT_CONTROL_THE_HELM_WHILE_HOLDING_A_FLAG);
            return false;
         }

         if (player.isInDuel()) {
            player.sendPacket(SystemMessageId.YOU_CANNOT_CONTROL_THE_HELM_WHILE_IN_A_DUEL);
            return false;
         }

         this._captain = player;
         player.broadcastUserInfo(true);
      }

      this.updateAbnormalEffect();
      return true;
   }

   @Override
   public int getFuel() {
      return this._fuel;
   }

   @Override
   public void setFuel(int f) {
      int old = this._fuel;
      if (f < 0) {
         this._fuel = 0;
      } else if (f > this._maxFuel) {
         this._fuel = this._maxFuel;
      } else {
         this._fuel = f;
      }

      if (this._fuel == 0 && old > 0) {
         this.broadcastToPassengers(SystemMessage.getSystemMessage(SystemMessageId.THE_AIRSHIP_FUEL_RUN_OUT));
      } else if (this._fuel < 40) {
         this.broadcastToPassengers(SystemMessage.getSystemMessage(SystemMessageId.THE_AIRSHIP_FUEL_SOON_RUN_OUT));
      }
   }

   @Override
   public int getMaxFuel() {
      return this._maxFuel;
   }

   @Override
   public void setMaxFuel(int mf) {
      this._maxFuel = mf;
   }

   @Override
   public void oustPlayer(Player player) {
      if (player == this._captain) {
         this.setCaptain(null);
      }

      super.oustPlayer(player);
   }

   @Override
   public void onSpawn() {
      super.onSpawn();
      this._checkTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new ControllableAirShipInstance.CheckTask(), 60000L, 10000L);
      this._consumeFuelTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new ControllableAirShipInstance.ConsumeFuelTask(), 60000L, 60000L);
   }

   @Override
   public void deleteMe() {
      super.deleteMe();
      if (this._checkTask != null) {
         this._checkTask.cancel(false);
         this._checkTask = null;
      }

      if (this._consumeFuelTask != null) {
         this._consumeFuelTask.cancel(false);
         this._consumeFuelTask = null;
      }

      try {
         this.broadcastPacket(new DeleteObject(this._helmId));
      } catch (Exception var2) {
         _log.log(Level.SEVERE, "Failed decayMe():" + var2.getMessage());
      }
   }

   @Override
   public void refreshID() {
      super.refreshID();
      IdFactory.getInstance().releaseId(this._helmId);
      this._helmId = IdFactory.getInstance().getNextId();
   }

   @Override
   public void sendInfo(Player activeChar) {
      super.sendInfo(activeChar);
      if (this._captain != null) {
         this._captain.sendInfo(activeChar);
      }
   }

   protected final class CheckTask implements Runnable {
      @Override
      public void run() {
         if (ControllableAirShipInstance.this.isVisible() && ControllableAirShipInstance.this.isEmpty() && !ControllableAirShipInstance.this.isInDock()) {
            ThreadPoolManager.getInstance().execute(ControllableAirShipInstance.this.new DecayTask());
         }
      }
   }

   protected final class ConsumeFuelTask implements Runnable {
      @Override
      public void run() {
         int fuel = ControllableAirShipInstance.this.getFuel();
         if (fuel > 0) {
            fuel -= 10;
            if (fuel < 0) {
               fuel = 0;
            }

            ControllableAirShipInstance.this.setFuel(fuel);
            ControllableAirShipInstance.this.updateAbnormalEffect();
         }
      }
   }

   protected final class DecayTask implements Runnable {
      @Override
      public void run() {
         ControllableAirShipInstance.this.deleteMe();
      }
   }
}
