package l2e.scripts.vehicles;

import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.instancemanager.AirShipManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.AirShipInstance;
import l2e.gameserver.model.actor.instance.ControllableAirShipInstance;
import l2e.gameserver.model.actor.templates.VehicleTemplate;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.zone.ZoneType;
import l2e.gameserver.model.zone.type.ScriptZone;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.gameserver.network.serverpackets.SystemMessage;

public abstract class AirShipController extends Quest {
   public static final Logger _log = Logger.getLogger(AirShipController.class.getName());
   protected int _dockZone = 0;
   protected int _shipSpawnX = 0;
   protected int _shipSpawnY = 0;
   protected int _shipSpawnZ = 0;
   protected int _shipHeading = 0;
   protected Location _oustLoc = null;
   protected int _locationId = 0;
   protected VehicleTemplate[] _arrivalPath = null;
   protected VehicleTemplate[] _departPath = null;
   protected VehicleTemplate[][] _teleportsTable = (VehicleTemplate[][])null;
   protected int[] _fuelTable = null;
   protected int _movieId = 0;
   protected boolean _isBusy = false;
   protected ControllableAirShipInstance _dockedShip = null;
   private final Runnable _decayTask = new AirShipController.DecayTask();
   private final Runnable _departTask = new AirShipController.DepartTask();
   private Future<?> _departSchedule = null;
   private NpcSay _arrivalMessage = null;
   private static final int DEPART_INTERVAL = 300000;
   private static final int LICENSE = 13559;
   private static final int STARSTONE = 13277;
   private static final int SUMMON_COST = 5;
   private static final SystemMessage SM_ALREADY_EXISTS = SystemMessage.getSystemMessage(SystemMessageId.THE_AIRSHIP_IS_ALREADY_EXISTS);
   private static final SystemMessage SM_ALREADY_SUMMONED = SystemMessage.getSystemMessage(SystemMessageId.ANOTHER_AIRSHIP_ALREADY_SUMMONED);
   private static final SystemMessage SM_NEED_LICENSE = SystemMessage.getSystemMessage(SystemMessageId.THE_AIRSHIP_NEED_LICENSE_TO_SUMMON);
   private static final SystemMessage SM_NEED_CLANLVL5 = SystemMessage.getSystemMessage(SystemMessageId.THE_AIRSHIP_NEED_CLANLVL_5_TO_SUMMON);
   private static final SystemMessage SM_NO_PRIVS = SystemMessage.getSystemMessage(SystemMessageId.THE_AIRSHIP_NO_PRIVILEGES);
   private static final SystemMessage SM_ALREADY_USED = SystemMessage.getSystemMessage(SystemMessageId.THE_AIRSHIP_ALREADY_USED);
   private static final SystemMessage SM_LICENSE_ALREADY_ACQUIRED = SystemMessage.getSystemMessage(SystemMessageId.THE_AIRSHIP_SUMMON_LICENSE_ALREADY_ACQUIRED);
   private static final SystemMessage SM_LICENSE_ENTERED = SystemMessage.getSystemMessage(SystemMessageId.THE_AIRSHIP_SUMMON_LICENSE_ENTERED);
   private static final SystemMessage SM_NEED_MORE = SystemMessage.getSystemMessage(SystemMessageId.THE_AIRSHIP_NEED_MORE_S1);

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      if ("summon".equalsIgnoreCase(event)) {
         if (this._dockedShip != null) {
            if (this._dockedShip.isOwner(player)) {
               player.sendPacket(SM_ALREADY_EXISTS);
            }

            return null;
         } else if (this._isBusy) {
            player.sendPacket(SM_ALREADY_SUMMONED);
            return null;
         } else if ((player.getClanPrivileges() & 1024) != 1024) {
            player.sendPacket(SM_NO_PRIVS);
            return null;
         } else {
            int ownerId = player.getId();
            if (!AirShipManager.getInstance().hasAirShipLicense(ownerId)) {
               player.sendPacket(SM_NEED_LICENSE);
               return null;
            } else if (AirShipManager.getInstance().hasAirShip(ownerId)) {
               player.sendPacket(SM_ALREADY_USED);
               return null;
            } else if (!player.destroyItemByItemId("AirShipSummon", 13277, 5L, npc, true)) {
               SM_NEED_MORE.addItemName(13277);
               player.sendPacket(SM_NEED_MORE);
               return null;
            } else {
               this._isBusy = true;
               AirShipInstance ship = AirShipManager.getInstance()
                  .getNewAirShip(this._shipSpawnX, this._shipSpawnY, this._shipSpawnZ, this._shipHeading, ownerId);
               if (ship != null) {
                  if (this._arrivalPath != null) {
                     ship.executePath(this._arrivalPath);
                  }

                  if (this._arrivalMessage == null) {
                     this._arrivalMessage = new NpcSay(
                        npc.getObjectId(), 23, npc.getId(), NpcStringId.THE_AIRSHIP_HAS_BEEN_SUMMONED_IT_WILL_AUTOMATICALLY_DEPART_IN_5_MINUTES
                     );
                  }

                  npc.broadcastPacket(this._arrivalMessage);
               } else {
                  this._isBusy = false;
               }

               return null;
            }
         }
      } else if ("board".equalsIgnoreCase(event)) {
         if (player.isTransformed()) {
            player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_TRANSFORMED);
            return null;
         } else if (player.isParalyzed()) {
            player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_PETRIFIED);
            return null;
         } else if (player.isDead() || player.isFakeDeath()) {
            player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_DEAD);
            return null;
         } else if (player.isFishing()) {
            player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_FISHING);
            return null;
         } else if (player.isInCombat()) {
            player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_IN_BATTLE);
            return null;
         } else if (player.isInDuel()) {
            player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_IN_A_DUEL);
            return null;
         } else if (player.isSitting()) {
            player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_SITTING);
            return null;
         } else if (player.isCastingNow()) {
            player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_CASTING);
            return null;
         } else if (player.isCursedWeaponEquipped()) {
            player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_A_CURSED_WEAPON_IS_EQUIPPED);
            return null;
         } else if (player.isCombatFlagEquipped()) {
            player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_HOLDING_A_FLAG);
            return null;
         } else if (player.hasSummon() || player.isMounted()) {
            player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_A_PET_OR_A_SERVITOR_IS_SUMMONED);
            return null;
         } else if (player.isFlyingMounted()) {
            player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_NOT_MEET_REQUEIREMENTS);
            return null;
         } else {
            if (this._dockedShip != null) {
               this._dockedShip.addPassenger(player);
            }

            return null;
         }
      } else if ("register".equalsIgnoreCase(event)) {
         if (player.getClan() == null || player.getClan().getLevel() < 5) {
            player.sendPacket(SM_NEED_CLANLVL5);
            return null;
         } else if (!player.isClanLeader()) {
            player.sendPacket(SM_NO_PRIVS);
            return null;
         } else {
            int ownerId = player.getId();
            if (AirShipManager.getInstance().hasAirShipLicense(ownerId)) {
               player.sendPacket(SM_LICENSE_ALREADY_ACQUIRED);
               return null;
            } else if (!player.destroyItemByItemId("AirShipLicense", 13559, 1L, npc, true)) {
               SM_NEED_MORE.addItemName(13277);
               player.sendPacket(SM_NEED_MORE);
               return null;
            } else {
               AirShipManager.getInstance().registerLicense(ownerId);
               player.sendPacket(SM_LICENSE_ENTERED);
               return null;
            }
         }
      } else {
         return event;
      }
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      return npc.getId() + ".htm";
   }

   @Override
   public String onEnterZone(Creature character, ZoneType zone) {
      if (character instanceof ControllableAirShipInstance && this._dockedShip == null) {
         this._dockedShip = (ControllableAirShipInstance)character;
         this._dockedShip.setInDock(this._dockZone);
         this._dockedShip.setOustLoc(this._oustLoc);
         if (!this._dockedShip.isEmpty()) {
            if (this._movieId != 0) {
               for(Player passenger : this._dockedShip.getPassengers()) {
                  if (passenger != null) {
                     passenger.showQuestMovie(this._movieId);
                  }
               }
            }

            ThreadPoolManager.getInstance().schedule(this._decayTask, 1000L);
         } else {
            this._departSchedule = ThreadPoolManager.getInstance().schedule(this._departTask, 300000L);
         }
      }

      return null;
   }

   @Override
   public String onExitZone(Creature character, ZoneType zone) {
      if (character instanceof ControllableAirShipInstance && character.equals(this._dockedShip)) {
         if (this._departSchedule != null) {
            this._departSchedule.cancel(false);
            this._departSchedule = null;
         }

         this._dockedShip.setInDock(0);
         this._dockedShip = null;
         this._isBusy = false;
      }

      return null;
   }

   protected void validityCheck() {
      ScriptZone zone = ZoneManager.getInstance().getZoneById(this._dockZone, ScriptZone.class);
      if (zone == null) {
         _log.log(Level.WARNING, this.getName() + ": Invalid zone " + this._dockZone + ", controller disabled");
         this._isBusy = true;
      } else {
         if (this._arrivalPath != null) {
            if (this._arrivalPath.length == 0) {
               _log.log(Level.WARNING, this.getName() + ": Zero arrival path length.");
               this._arrivalPath = null;
            } else {
               VehicleTemplate p = this._arrivalPath[this._arrivalPath.length - 1];
               if (!zone.isInsideZone(p.getLocation())) {
                  _log.log(
                     Level.WARNING,
                     this.getName() + ": Arrival path finish point (" + p.getX() + "," + p.getY() + "," + p.getZ() + ") not in zone " + this._dockZone
                  );
                  this._arrivalPath = null;
               }
            }
         }

         if (this._arrivalPath == null
            && !ZoneManager.getInstance().getZoneById(this._dockZone, ScriptZone.class).isInsideZone(this._shipSpawnX, this._shipSpawnY, this._shipSpawnZ)) {
            _log.log(Level.WARNING, this.getName() + ": Arrival path is null and spawn point not in zone " + this._dockZone + ", controller disabled");
            this._isBusy = true;
         } else {
            if (this._departPath != null) {
               if (this._departPath.length == 0) {
                  _log.log(Level.WARNING, this.getName() + ": Zero depart path length.");
                  this._departPath = null;
               } else {
                  VehicleTemplate p = this._departPath[this._departPath.length - 1];
                  if (zone.isInsideZone(p.getLocation())) {
                     _log.log(
                        Level.WARNING,
                        this.getName() + ": Departure path finish point (" + p.getX() + "," + p.getY() + "," + p.getZ() + ") in zone " + this._dockZone
                     );
                     this._departPath = null;
                  }
               }
            }

            if (this._teleportsTable != null) {
               if (this._fuelTable == null) {
                  _log.log(Level.WARNING, this.getName() + ": Fuel consumption not defined.");
               } else if (this._teleportsTable.length != this._fuelTable.length) {
                  _log.log(Level.WARNING, this.getName() + ": Fuel consumption not match teleport list.");
               } else {
                  AirShipManager.getInstance().registerAirShipTeleportList(this._dockZone, this._locationId, this._teleportsTable, this._fuelTable);
               }
            }
         }
      }
   }

   public AirShipController(int questId, String name, String descr) {
      super(questId, name, descr);
   }

   public static void main(String[] args) {
   }

   protected final class DecayTask implements Runnable {
      @Override
      public void run() {
         if (AirShipController.this._dockedShip != null) {
            AirShipController.this._dockedShip.deleteMe();
         }
      }
   }

   protected final class DepartTask implements Runnable {
      @Override
      public void run() {
         if (AirShipController.this._dockedShip != null && AirShipController.this._dockedShip.isInDock() && !AirShipController.this._dockedShip.isMoving()) {
            if (AirShipController.this._departPath != null) {
               AirShipController.this._dockedShip.executePath(AirShipController.this._departPath);
            } else {
               AirShipController.this._dockedShip.deleteMe();
            }
         }
      }
   }
}
