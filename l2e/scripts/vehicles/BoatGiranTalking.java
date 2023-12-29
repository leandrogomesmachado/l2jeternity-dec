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

public class BoatGiranTalking extends Quest implements Runnable {
   private static final Logger _log = Logger.getLogger(BoatGiranTalking.class.getName());
   private static final VehicleTemplate[] GIRAN_TO_TALKING = new VehicleTemplate[]{
      new VehicleTemplate(51914, 189023, -3610, 150, 800),
      new VehicleTemplate(60567, 189789, -3610, 150, 800),
      new VehicleTemplate(63732, 197457, -3610, 200, 800),
      new VehicleTemplate(63732, 219946, -3610, 250, 800),
      new VehicleTemplate(62008, 222240, -3610, 250, 1200),
      new VehicleTemplate(56115, 226791, -3610, 250, 1200),
      new VehicleTemplate(40384, 226432, -3610, 300, 800),
      new VehicleTemplate(37760, 226432, -3610, 300, 800),
      new VehicleTemplate(27153, 226791, -3610, 300, 800),
      new VehicleTemplate(12672, 227535, -3610, 300, 800),
      new VehicleTemplate(-1808, 228280, -3610, 300, 800),
      new VehicleTemplate(-22165, 230542, -3610, 300, 800),
      new VehicleTemplate(-42523, 235205, -3610, 300, 800),
      new VehicleTemplate(-68451, 259560, -3610, 250, 800),
      new VehicleTemplate(-70848, 261696, -3610, 200, 800),
      new VehicleTemplate(-83344, 261610, -3610, 200, 800),
      new VehicleTemplate(-88344, 261660, -3610, 180, 800),
      new VehicleTemplate(-92344, 261660, -3610, 180, 800),
      new VehicleTemplate(-94242, 261659, -3610, 150, 800)
   };
   private static final VehicleTemplate[] TALKING_DOCK = new VehicleTemplate[]{new VehicleTemplate(-96622, 261660, -3610, 150, 800)};
   private static final VehicleTemplate[] TALKING_TO_GIRAN = new VehicleTemplate[]{
      new VehicleTemplate(-113925, 261660, -3610, 150, 800),
      new VehicleTemplate(-126107, 249116, -3610, 180, 800),
      new VehicleTemplate(-126107, 234499, -3610, 180, 800),
      new VehicleTemplate(-126107, 219882, -3610, 180, 800),
      new VehicleTemplate(-109414, 204914, -3610, 180, 800),
      new VehicleTemplate(-92807, 204914, -3610, 180, 800),
      new VehicleTemplate(-80425, 216450, -3610, 250, 800),
      new VehicleTemplate(-68043, 227987, -3610, 250, 800),
      new VehicleTemplate(-63744, 231168, -3610, 250, 800),
      new VehicleTemplate(-60844, 231369, -3610, 250, 1800),
      new VehicleTemplate(-44915, 231369, -3610, 200, 800),
      new VehicleTemplate(-28986, 231369, -3610, 200, 800),
      new VehicleTemplate(8233, 207624, -3610, 200, 800),
      new VehicleTemplate(21470, 201503, -3610, 180, 800),
      new VehicleTemplate(40058, 195383, -3610, 180, 800),
      new VehicleTemplate(43022, 193793, -3610, 150, 800),
      new VehicleTemplate(45986, 192203, -3610, 150, 800),
      new VehicleTemplate(48950, 190613, -3610, 150, 800)
   };
   private static final VehicleTemplate GIRAN_DOCK = TALKING_TO_GIRAN[TALKING_TO_GIRAN.length - 1];
   private final BoatInstance _boat;
   private int _cycle = 0;
   private int _shoutCount = 0;
   private final CreatureSay ARRIVED_AT_GIRAN;
   private final CreatureSay ARRIVED_AT_GIRAN_2;
   private final CreatureSay LEAVE_GIRAN5;
   private final CreatureSay LEAVE_GIRAN1;
   private final CreatureSay LEAVE_GIRAN0;
   private final CreatureSay LEAVING_GIRAN;
   private final CreatureSay ARRIVED_AT_TALKING;
   private final CreatureSay ARRIVED_AT_TALKING_2;
   private final CreatureSay LEAVE_TALKING5;
   private final CreatureSay LEAVE_TALKING1;
   private final CreatureSay LEAVE_TALKING0;
   private final CreatureSay LEAVING_TALKING;
   private final CreatureSay BUSY_TALKING;
   private final CreatureSay ARRIVAL_TALKING15;
   private final CreatureSay ARRIVAL_TALKING10;
   private final CreatureSay ARRIVAL_TALKING5;
   private final CreatureSay ARRIVAL_TALKING1;
   private final CreatureSay ARRIVAL_GIRAN20;
   private final CreatureSay ARRIVAL_GIRAN15;
   private final CreatureSay ARRIVAL_GIRAN10;
   private final CreatureSay ARRIVAL_GIRAN5;
   private final CreatureSay ARRIVAL_GIRAN1;
   private final PlaySound GIRAN_SOUND;
   private final PlaySound TALKING_SOUND;

   public BoatGiranTalking(int questId, String name, String descr) {
      super(questId, name, descr);
      this._boat = BoatManager.getInstance().getNewBoat(2, 48950, 190613, -3610, 60800);
      this._boat.registerEngine(this);
      this._boat.runEngine(180000);
      this.ARRIVED_AT_GIRAN = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_ARRIVED_AT_GIRAN);
      this.ARRIVED_AT_GIRAN_2 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_LEAVE_FOR_TALKING_AFTER_10_MINUTES);
      this.LEAVE_GIRAN5 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_LEAVE_FOR_TALKING_IN_5_MINUTES);
      this.LEAVE_GIRAN1 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_LEAVE_FOR_TALKING_IN_1_MINUTE);
      this.LEAVE_GIRAN0 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_LEAVE_SOON_FOR_TALKING);
      this.LEAVING_GIRAN = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_LEAVING_FOR_TALKING);
      this.ARRIVED_AT_TALKING = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_ARRIVED_AT_TALKING);
      this.ARRIVED_AT_TALKING_2 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_LEAVE_FOR_GIRAN_AFTER_10_MINUTES);
      this.LEAVE_TALKING5 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_LEAVE_FOR_GIRAN_IN_5_MINUTES);
      this.LEAVE_TALKING1 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_LEAVE_FOR_GIRAN_IN_1_MINUTE);
      this.LEAVE_TALKING0 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_LEAVE_SOON_FOR_GIRAN);
      this.LEAVING_TALKING = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_LEAVING_FOR_GIRAN);
      this.BUSY_TALKING = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_GIRAN_TALKING_DELAYED);
      this.ARRIVAL_TALKING15 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_FROM_GIRAN_ARRIVE_AT_TALKING_15_MINUTES);
      this.ARRIVAL_TALKING10 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_FROM_GIRAN_ARRIVE_AT_TALKING_10_MINUTES);
      this.ARRIVAL_TALKING5 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_FROM_GIRAN_ARRIVE_AT_TALKING_5_MINUTES);
      this.ARRIVAL_TALKING1 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_FROM_GIRAN_ARRIVE_AT_TALKING_1_MINUTE);
      this.ARRIVAL_GIRAN20 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_20_MINUTES);
      this.ARRIVAL_GIRAN15 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_15_MINUTES);
      this.ARRIVAL_GIRAN10 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_10_MINUTES);
      this.ARRIVAL_GIRAN5 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_5_MINUTES);
      this.ARRIVAL_GIRAN1 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_1_MINUTE);
      this.GIRAN_SOUND = new PlaySound(
         0, "itemsound.ship_arrival_departure", 1, this._boat.getObjectId(), GIRAN_DOCK.getX(), GIRAN_DOCK.getY(), GIRAN_DOCK.getZ()
      );
      this.TALKING_SOUND = new PlaySound(
         0, "itemsound.ship_arrival_departure", 1, this._boat.getObjectId(), TALKING_DOCK[0].getX(), TALKING_DOCK[0].getY(), TALKING_DOCK[0].getZ()
      );
   }

   @Override
   public void run() {
      try {
         switch(this._cycle) {
            case 0:
               BoatManager.getInstance().broadcastPacket(GIRAN_DOCK, TALKING_DOCK[0], this.LEAVE_GIRAN5);
               ThreadPoolManager.getInstance().schedule(this, 240000L);
               break;
            case 1:
               BoatManager.getInstance().broadcastPacket(GIRAN_DOCK, TALKING_DOCK[0], this.LEAVE_GIRAN1);
               ThreadPoolManager.getInstance().schedule(this, 40000L);
               break;
            case 2:
               BoatManager.getInstance().broadcastPacket(GIRAN_DOCK, TALKING_DOCK[0], this.LEAVE_GIRAN0);
               ThreadPoolManager.getInstance().schedule(this, 20000L);
               break;
            case 3:
               BoatManager.getInstance().broadcastPackets(GIRAN_DOCK, TALKING_DOCK[0], this.LEAVING_GIRAN, this.ARRIVAL_TALKING15);
               this._boat.broadcastPacket(this.GIRAN_SOUND);
               this._boat.payForRide(3946, 1, 46763, 187041, -3451);
               this._boat.executePath(GIRAN_TO_TALKING);
               ThreadPoolManager.getInstance().schedule(this, 250000L);
               break;
            case 4:
               BoatManager.getInstance().broadcastPacket(TALKING_DOCK[0], GIRAN_DOCK, this.ARRIVAL_TALKING10);
               ThreadPoolManager.getInstance().schedule(this, 300000L);
               break;
            case 5:
               BoatManager.getInstance().broadcastPacket(TALKING_DOCK[0], GIRAN_DOCK, this.ARRIVAL_TALKING5);
               ThreadPoolManager.getInstance().schedule(this, 240000L);
               break;
            case 6:
               BoatManager.getInstance().broadcastPacket(TALKING_DOCK[0], GIRAN_DOCK, this.ARRIVAL_TALKING1);
               break;
            case 7:
               if (BoatManager.getInstance().dockBusy(1)) {
                  if (this._shoutCount == 0) {
                     BoatManager.getInstance().broadcastPacket(TALKING_DOCK[0], GIRAN_DOCK, this.BUSY_TALKING);
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
            case 8:
               BoatManager.getInstance().dockShip(1, true);
               BoatManager.getInstance().broadcastPackets(TALKING_DOCK[0], GIRAN_DOCK, this.ARRIVED_AT_TALKING, this.ARRIVED_AT_TALKING_2);
               this._boat.broadcastPacket(this.TALKING_SOUND);
               ThreadPoolManager.getInstance().schedule(this, 300000L);
               break;
            case 9:
               BoatManager.getInstance().broadcastPacket(TALKING_DOCK[0], GIRAN_DOCK, this.LEAVE_TALKING5);
               ThreadPoolManager.getInstance().schedule(this, 240000L);
               break;
            case 10:
               BoatManager.getInstance().broadcastPacket(TALKING_DOCK[0], GIRAN_DOCK, this.LEAVE_TALKING1);
               ThreadPoolManager.getInstance().schedule(this, 40000L);
               break;
            case 11:
               BoatManager.getInstance().broadcastPacket(TALKING_DOCK[0], GIRAN_DOCK, this.LEAVE_TALKING0);
               ThreadPoolManager.getInstance().schedule(this, 20000L);
               break;
            case 12:
               BoatManager.getInstance().dockShip(1, false);
               BoatManager.getInstance().broadcastPackets(TALKING_DOCK[0], GIRAN_DOCK, this.LEAVING_TALKING);
               this._boat.broadcastPacket(this.TALKING_SOUND);
               this._boat.payForRide(3945, 1, -96777, 258970, -3623);
               this._boat.executePath(TALKING_TO_GIRAN);
               ThreadPoolManager.getInstance().schedule(this, 200000L);
               break;
            case 13:
               BoatManager.getInstance().broadcastPacket(GIRAN_DOCK, TALKING_DOCK[0], this.ARRIVAL_GIRAN20);
               ThreadPoolManager.getInstance().schedule(this, 300000L);
               break;
            case 14:
               BoatManager.getInstance().broadcastPacket(GIRAN_DOCK, TALKING_DOCK[0], this.ARRIVAL_GIRAN15);
               ThreadPoolManager.getInstance().schedule(this, 300000L);
               break;
            case 15:
               BoatManager.getInstance().broadcastPacket(GIRAN_DOCK, TALKING_DOCK[0], this.ARRIVAL_GIRAN10);
               ThreadPoolManager.getInstance().schedule(this, 300000L);
               break;
            case 16:
               BoatManager.getInstance().broadcastPacket(GIRAN_DOCK, TALKING_DOCK[0], this.ARRIVAL_GIRAN5);
               ThreadPoolManager.getInstance().schedule(this, 240000L);
               break;
            case 17:
               BoatManager.getInstance().broadcastPacket(GIRAN_DOCK, TALKING_DOCK[0], this.ARRIVAL_GIRAN1);
               break;
            case 18:
               BoatManager.getInstance().broadcastPackets(GIRAN_DOCK, TALKING_DOCK[0], this.ARRIVED_AT_GIRAN, this.ARRIVED_AT_GIRAN_2);
               this._boat.broadcastPacket(this.GIRAN_SOUND);
               ThreadPoolManager.getInstance().schedule(this, 300000L);
         }

         this._shoutCount = 0;
         ++this._cycle;
         if (this._cycle > 18) {
            this._cycle = 0;
         }
      } catch (Exception var2) {
         _log.log(Level.WARNING, var2.getMessage());
      }
   }

   public static void main(String[] args) {
      new BoatGiranTalking(-1, BoatGiranTalking.class.getSimpleName(), "vehicles");
   }
}
