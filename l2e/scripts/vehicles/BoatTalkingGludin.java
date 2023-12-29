package l2e.scripts.vehicles;

import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.instancemanager.BoatManager;
import l2e.gameserver.model.actor.instance.BoatInstance;
import l2e.gameserver.model.actor.templates.VehicleTemplate;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.PlaySound;

public class BoatTalkingGludin extends Quest implements Runnable {
   private static final Logger _log = Logger.getLogger(BoatTalkingGludin.class.getName());
   private static final VehicleTemplate[] TALKING_TO_GLUDIN = new VehicleTemplate[]{
      new VehicleTemplate(-121385, 261660, -3610, 180, 800),
      new VehicleTemplate(-127694, 253312, -3610, 200, 800),
      new VehicleTemplate(-129274, 237060, -3610, 250, 800),
      new VehicleTemplate(-114688, 139040, -3610, 200, 800),
      new VehicleTemplate(-109663, 135704, -3610, 180, 800),
      new VehicleTemplate(-102151, 135704, -3610, 180, 800),
      new VehicleTemplate(-96686, 140595, -3610, 180, 800),
      new VehicleTemplate(-95686, 147718, -3610, 180, 800),
      new VehicleTemplate(-95686, 148718, -3610, 180, 800),
      new VehicleTemplate(-95686, 149718, -3610, 150, 800)
   };
   private static final VehicleTemplate[] GLUDIN_DOCK = new VehicleTemplate[]{new VehicleTemplate(-95686, 150514, -3610, 150, 800)};
   private static final VehicleTemplate[] GLUDIN_TO_TALKING = new VehicleTemplate[]{
      new VehicleTemplate(-95686, 155514, -3610, 180, 800),
      new VehicleTemplate(-95686, 185514, -3610, 250, 800),
      new VehicleTemplate(-60136, 238816, -3610, 200, 800),
      new VehicleTemplate(-60520, 259609, -3610, 180, 1800),
      new VehicleTemplate(-65344, 261460, -3610, 180, 1800),
      new VehicleTemplate(-83344, 261560, -3610, 180, 1800),
      new VehicleTemplate(-88344, 261660, -3610, 180, 1800),
      new VehicleTemplate(-92344, 261660, -3610, 150, 1800),
      new VehicleTemplate(-94242, 261659, -3610, 150, 1800)
   };
   private static final VehicleTemplate[] TALKING_DOCK = new VehicleTemplate[]{new VehicleTemplate(-96622, 261660, -3610, 150, 1800)};
   private final BoatInstance _boat;
   private int _cycle = 0;
   private int _shoutCount = 0;
   private final CreatureSay ARRIVED_AT_TALKING;
   private final CreatureSay ARRIVED_AT_TALKING_2;
   private final CreatureSay LEAVE_TALKING5;
   private final CreatureSay LEAVE_TALKING1;
   private final CreatureSay LEAVE_TALKING1_2;
   private final CreatureSay LEAVE_TALKING0;
   private final CreatureSay LEAVING_TALKING;
   private final CreatureSay ARRIVED_AT_GLUDIN;
   private final CreatureSay ARRIVED_AT_GLUDIN_2;
   private final CreatureSay LEAVE_GLUDIN5;
   private final CreatureSay LEAVE_GLUDIN1;
   private final CreatureSay LEAVE_GLUDIN0;
   private final CreatureSay LEAVING_GLUDIN;
   private final CreatureSay BUSY_TALKING;
   private final CreatureSay BUSY_GLUDIN;
   private final CreatureSay ARRIVAL_GLUDIN10;
   private final CreatureSay ARRIVAL_GLUDIN5;
   private final CreatureSay ARRIVAL_GLUDIN1;
   private final CreatureSay ARRIVAL_TALKING10;
   private final CreatureSay ARRIVAL_TALKING5;
   private final CreatureSay ARRIVAL_TALKING1;
   private final PlaySound TALKING_SOUND;
   private final PlaySound GLUDIN_SOUND;

   public BoatTalkingGludin(int questId, String name, String descr) {
      super(questId, name, descr);
      this._boat = BoatManager.getInstance().getNewBoat(1, -96622, 261660, -3610, 32768);
      this._boat.registerEngine(this);
      this._boat.runEngine(180000);
      BoatManager.getInstance().dockShip(1, true);
      this._cycle = 0;
      this.ARRIVED_AT_TALKING = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_ARRIVED_AT_TALKING);
      this.ARRIVED_AT_TALKING_2 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_LEAVE_FOR_GLUDIN_AFTER_10_MINUTES);
      this.LEAVE_TALKING5 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_LEAVE_FOR_GLUDIN_IN_5_MINUTES);
      this.LEAVE_TALKING1 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_LEAVE_FOR_GLUDIN_IN_1_MINUTE);
      this.LEAVE_TALKING1_2 = new CreatureSay(0, 11, 801, SystemMessageId.MAKE_HASTE_GET_ON_BOAT);
      this.LEAVE_TALKING0 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_LEAVE_SOON_FOR_GLUDIN);
      this.LEAVING_TALKING = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_LEAVING_FOR_GLUDIN);
      this.ARRIVED_AT_GLUDIN = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_ARRIVED_AT_GLUDIN);
      this.ARRIVED_AT_GLUDIN_2 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_LEAVE_FOR_TALKING_AFTER_10_MINUTES);
      this.LEAVE_GLUDIN5 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_LEAVE_FOR_TALKING_IN_5_MINUTES);
      this.LEAVE_GLUDIN1 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_LEAVE_FOR_TALKING_IN_1_MINUTE);
      this.LEAVE_GLUDIN0 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_LEAVE_SOON_FOR_TALKING);
      this.LEAVING_GLUDIN = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_LEAVING_FOR_TALKING);
      this.BUSY_TALKING = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_GLUDIN_TALKING_DELAYED);
      this.BUSY_GLUDIN = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_TALKING_GLUDIN_DELAYED);
      this.ARRIVAL_GLUDIN10 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_FROM_TALKING_ARRIVE_AT_GLUDIN_10_MINUTES);
      this.ARRIVAL_GLUDIN5 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_FROM_TALKING_ARRIVE_AT_GLUDIN_5_MINUTES);
      this.ARRIVAL_GLUDIN1 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_FROM_TALKING_ARRIVE_AT_GLUDIN_1_MINUTE);
      this.ARRIVAL_TALKING10 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_FROM_GLUDIN_ARRIVE_AT_TALKING_10_MINUTES);
      this.ARRIVAL_TALKING5 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_FROM_GLUDIN_ARRIVE_AT_TALKING_5_MINUTES);
      this.ARRIVAL_TALKING1 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_FROM_GLUDIN_ARRIVE_AT_TALKING_1_MINUTE);
      this.TALKING_SOUND = new PlaySound(
         0, "itemsound.ship_arrival_departure", 1, this._boat.getObjectId(), TALKING_DOCK[0].getX(), TALKING_DOCK[0].getY(), TALKING_DOCK[0].getZ()
      );
      this.GLUDIN_SOUND = new PlaySound(
         0, "itemsound.ship_arrival_departure", 1, this._boat.getObjectId(), GLUDIN_DOCK[0].getX(), GLUDIN_DOCK[0].getY(), GLUDIN_DOCK[0].getZ()
      );
   }

   @Override
   public void run() {
      try {
         switch(this._cycle) {
            case 0:
               BoatManager.getInstance().broadcastPacket(TALKING_DOCK[0], GLUDIN_DOCK[0], this.LEAVE_TALKING5);
               ThreadPoolManager.getInstance().schedule(this, 240000L);
               break;
            case 1:
               BoatManager.getInstance().broadcastPackets(TALKING_DOCK[0], GLUDIN_DOCK[0], this.LEAVE_TALKING1, this.LEAVE_TALKING1_2);
               ThreadPoolManager.getInstance().schedule(this, 40000L);
               break;
            case 2:
               BoatManager.getInstance().broadcastPacket(TALKING_DOCK[0], GLUDIN_DOCK[0], this.LEAVE_TALKING0);
               ThreadPoolManager.getInstance().schedule(this, 20000L);
               break;
            case 3:
               BoatManager.getInstance().dockShip(1, false);
               BoatManager.getInstance().broadcastPackets(TALKING_DOCK[0], GLUDIN_DOCK[0], this.LEAVING_TALKING);
               this._boat.broadcastPacket(this.TALKING_SOUND);
               this._boat.payForRide(1074, 1, -96777, 258970, -3623);
               this._boat.executePath(TALKING_TO_GLUDIN);
               ThreadPoolManager.getInstance().schedule(this, 300000L);
               break;
            case 4:
               BoatManager.getInstance().broadcastPacket(GLUDIN_DOCK[0], TALKING_DOCK[0], this.ARRIVAL_GLUDIN10);
               ThreadPoolManager.getInstance().schedule(this, 300000L);
               break;
            case 5:
               BoatManager.getInstance().broadcastPacket(GLUDIN_DOCK[0], TALKING_DOCK[0], this.ARRIVAL_GLUDIN5);
               ThreadPoolManager.getInstance().schedule(this, 240000L);
               break;
            case 6:
               BoatManager.getInstance().broadcastPacket(GLUDIN_DOCK[0], TALKING_DOCK[0], this.ARRIVAL_GLUDIN1);
               break;
            case 7:
               if (BoatManager.getInstance().dockBusy(2)) {
                  if (this._shoutCount == 0) {
                     BoatManager.getInstance().broadcastPacket(GLUDIN_DOCK[0], TALKING_DOCK[0], this.BUSY_GLUDIN);
                  }

                  ++this._shoutCount;
                  if (this._shoutCount > 35) {
                     this._shoutCount = 0;
                  }

                  ThreadPoolManager.getInstance().schedule(this, 5000L);
                  return;
               }

               this._boat.executePath(GLUDIN_DOCK);
               break;
            case 8:
               BoatManager.getInstance().dockShip(2, true);
               BoatManager.getInstance().broadcastPackets(GLUDIN_DOCK[0], TALKING_DOCK[0], this.ARRIVED_AT_GLUDIN, this.ARRIVED_AT_GLUDIN_2);
               this._boat.broadcastPacket(this.GLUDIN_SOUND);
               ThreadPoolManager.getInstance().schedule(this, 300000L);
               break;
            case 9:
               BoatManager.getInstance().broadcastPacket(TALKING_DOCK[0], GLUDIN_DOCK[0], this.LEAVE_GLUDIN5);
               ThreadPoolManager.getInstance().schedule(this, 240000L);
               break;
            case 10:
               BoatManager.getInstance().broadcastPackets(TALKING_DOCK[0], GLUDIN_DOCK[0], this.LEAVE_GLUDIN1, this.LEAVE_TALKING1_2);
               ThreadPoolManager.getInstance().schedule(this, 40000L);
               break;
            case 11:
               BoatManager.getInstance().broadcastPacket(TALKING_DOCK[0], GLUDIN_DOCK[0], this.LEAVE_GLUDIN0);
               ThreadPoolManager.getInstance().schedule(this, 20000L);
               break;
            case 12:
               BoatManager.getInstance().dockShip(2, false);
               BoatManager.getInstance().broadcastPackets(TALKING_DOCK[0], GLUDIN_DOCK[0], this.LEAVING_GLUDIN);
               this._boat.broadcastPacket(this.GLUDIN_SOUND);
               this._boat.payForRide(1075, 1, -90015, 150422, -3610);
               this._boat.executePath(GLUDIN_TO_TALKING);
               ThreadPoolManager.getInstance().schedule(this, 150000L);
               break;
            case 13:
               BoatManager.getInstance().broadcastPacket(TALKING_DOCK[0], GLUDIN_DOCK[0], this.ARRIVAL_TALKING10);
               ThreadPoolManager.getInstance().schedule(this, 300000L);
               break;
            case 14:
               BoatManager.getInstance().broadcastPacket(TALKING_DOCK[0], GLUDIN_DOCK[0], this.ARRIVAL_TALKING5);
               ThreadPoolManager.getInstance().schedule(this, 240000L);
               break;
            case 15:
               BoatManager.getInstance().broadcastPacket(TALKING_DOCK[0], GLUDIN_DOCK[0], this.ARRIVAL_TALKING1);
               break;
            case 16:
               if (BoatManager.getInstance().dockBusy(1)) {
                  if (this._shoutCount == 0) {
                     BoatManager.getInstance().broadcastPacket(TALKING_DOCK[0], GLUDIN_DOCK[0], this.BUSY_TALKING);
                  }

                  ++this._shoutCount;
                  if (this._shoutCount > 35) {
                     this._shoutCount = 0;
                  }

                  ThreadPoolManager.getInstance().schedule(this, 5000L);
                  return;
               }

               this._boat.executePath(TALKING_DOCK);
               break;
            case 17:
               BoatManager.getInstance().dockShip(1, true);
               BoatManager.getInstance().broadcastPackets(TALKING_DOCK[0], GLUDIN_DOCK[0], this.ARRIVED_AT_TALKING, this.ARRIVED_AT_TALKING_2);
               this._boat.broadcastPacket(this.TALKING_SOUND);
               ThreadPoolManager.getInstance().schedule(this, 300000L);
         }

         this._shoutCount = 0;
         ++this._cycle;
         if (this._cycle > 17) {
            this._cycle = 0;
         }
      } catch (Exception var2) {
         _log.log(Level.WARNING, var2.getMessage());
      }
   }

   public static void main(String[] args) {
      new BoatTalkingGludin(-1, BoatTalkingGludin.class.getSimpleName(), "vehicles");
   }
}
