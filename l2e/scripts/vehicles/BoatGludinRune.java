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

public class BoatGludinRune extends Quest implements Runnable {
   private static final Logger _log = Logger.getLogger(BoatGludinRune.class.getName());
   private static final VehicleTemplate[] GLUDIN_TO_RUNE = new VehicleTemplate[]{
      new VehicleTemplate(-95686, 155514, -3610, 150, 800),
      new VehicleTemplate(-98112, 159040, -3610, 150, 800),
      new VehicleTemplate(-104192, 160608, -3610, 200, 1800),
      new VehicleTemplate(-109952, 159616, -3610, 250, 1800),
      new VehicleTemplate(-112768, 154784, -3610, 290, 1800),
      new VehicleTemplate(-114688, 139040, -3610, 290, 1800),
      new VehicleTemplate(-115232, 134368, -3610, 290, 1800),
      new VehicleTemplate(-113888, 121696, -3610, 290, 1800),
      new VehicleTemplate(-107808, 104928, -3610, 290, 1800),
      new VehicleTemplate(-97152, 75520, -3610, 290, 800),
      new VehicleTemplate(-85536, 67264, -3610, 290, 1800),
      new VehicleTemplate(-64640, 55840, -3610, 290, 1800),
      new VehicleTemplate(-60096, 44672, -3610, 290, 1800),
      new VehicleTemplate(-52672, 37440, -3610, 290, 1800),
      new VehicleTemplate(-46144, 33184, -3610, 290, 1800),
      new VehicleTemplate(-36096, 24928, -3610, 290, 1800),
      new VehicleTemplate(-33792, 8448, -3610, 290, 1800),
      new VehicleTemplate(-23776, 3424, -3610, 290, 1000),
      new VehicleTemplate(-12000, -1760, -3610, 290, 1000),
      new VehicleTemplate(672, 480, -3610, 290, 1800),
      new VehicleTemplate(15488, 200, -3610, 290, 1000),
      new VehicleTemplate(24736, 164, -3610, 290, 1000),
      new VehicleTemplate(32192, -1156, -3610, 290, 1000),
      new VehicleTemplate(39200, -8032, -3610, 270, 1000),
      new VehicleTemplate(44320, -25152, -3610, 270, 1000),
      new VehicleTemplate(40576, -31616, -3610, 250, 800),
      new VehicleTemplate(36819, -35315, -3610, 220, 800)
   };
   private static final VehicleTemplate[] RUNE_DOCK = new VehicleTemplate[]{new VehicleTemplate(34381, -37680, -3610, 200, 800)};
   private static final VehicleTemplate[] RUNE_TO_GLUDIN = new VehicleTemplate[]{
      new VehicleTemplate(32750, -39300, -3610, 150, 800),
      new VehicleTemplate(27440, -39328, -3610, 180, 1000),
      new VehicleTemplate(21456, -34272, -3610, 200, 1000),
      new VehicleTemplate(6608, -29520, -3610, 250, 800),
      new VehicleTemplate(4160, -27828, -3610, 270, 800),
      new VehicleTemplate(2432, -25472, -3610, 270, 1000),
      new VehicleTemplate(-8000, -16272, -3610, 220, 1000),
      new VehicleTemplate(-18976, -9760, -3610, 290, 800),
      new VehicleTemplate(-23776, 3408, -3610, 290, 800),
      new VehicleTemplate(-33792, 8432, -3610, 290, 800),
      new VehicleTemplate(-36096, 24912, -3610, 290, 800),
      new VehicleTemplate(-46144, 33184, -3610, 290, 800),
      new VehicleTemplate(-52688, 37440, -3610, 290, 800),
      new VehicleTemplate(-60096, 44672, -3610, 290, 800),
      new VehicleTemplate(-64640, 55840, -3610, 290, 800),
      new VehicleTemplate(-85552, 67248, -3610, 290, 800),
      new VehicleTemplate(-97168, 85264, -3610, 290, 800),
      new VehicleTemplate(-107824, 104912, -3610, 290, 800),
      new VehicleTemplate(-102151, 135704, -3610, 290, 800),
      new VehicleTemplate(-96686, 140595, -3610, 290, 800),
      new VehicleTemplate(-95686, 147717, -3610, 250, 800),
      new VehicleTemplate(-95686, 148218, -3610, 200, 800)
   };
   private static final VehicleTemplate[] GLUDIN_DOCK = new VehicleTemplate[]{new VehicleTemplate(-95686, 150514, -3610, 150, 800)};
   private final BoatInstance _boat;
   private int _cycle = 0;
   private int _shoutCount = 0;
   private final CreatureSay ARRIVED_AT_GLUDIN;
   private final CreatureSay ARRIVED_AT_GLUDIN_2;
   private final CreatureSay LEAVE_GLUDIN5;
   private final CreatureSay LEAVE_GLUDIN1;
   private final CreatureSay LEAVE_GLUDIN0;
   private final CreatureSay LEAVING_GLUDIN;
   private final CreatureSay ARRIVED_AT_RUNE;
   private final CreatureSay ARRIVED_AT_RUNE_2;
   private final CreatureSay LEAVE_RUNE5;
   private final CreatureSay LEAVE_RUNE1;
   private final CreatureSay LEAVE_RUNE0;
   private final CreatureSay LEAVING_RUNE;
   private final CreatureSay BUSY_GLUDIN;
   private final CreatureSay BUSY_RUNE;
   private final CreatureSay ARRIVAL_RUNE15;
   private final CreatureSay ARRIVAL_RUNE10;
   private final CreatureSay ARRIVAL_RUNE5;
   private final CreatureSay ARRIVAL_RUNE1;
   private final CreatureSay ARRIVAL_GLUDIN15;
   private final CreatureSay ARRIVAL_GLUDIN10;
   private final CreatureSay ARRIVAL_GLUDIN5;
   private final CreatureSay ARRIVAL_GLUDIN1;
   private final PlaySound GLUDIN_SOUND;
   private final PlaySound RUNE_SOUND;

   public BoatGludinRune(int questId, String name, String descr) {
      super(questId, name, descr);
      this._boat = BoatManager.getInstance().getNewBoat(3, -95686, 150514, -3610, 16723);
      this._boat.registerEngine(this);
      this._boat.runEngine(180000);
      BoatManager.getInstance().dockShip(2, true);
      this.ARRIVED_AT_GLUDIN = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_ARRIVED_AT_GLUDIN);
      this.ARRIVED_AT_GLUDIN_2 = new CreatureSay(0, 11, 801, SystemMessageId.DEPARTURE_FOR_RUNE_10_MINUTES);
      this.LEAVE_GLUDIN5 = new CreatureSay(0, 11, 801, SystemMessageId.DEPARTURE_FOR_RUNE_5_MINUTES);
      this.LEAVE_GLUDIN1 = new CreatureSay(0, 11, 801, SystemMessageId.DEPARTURE_FOR_RUNE_1_MINUTE);
      this.LEAVE_GLUDIN0 = new CreatureSay(0, 11, 801, SystemMessageId.DEPARTURE_FOR_GLUDIN_SHORTLY2);
      this.LEAVING_GLUDIN = new CreatureSay(0, 11, 801, SystemMessageId.DEPARTURE_FOR_GLUDIN_NOW);
      this.ARRIVED_AT_RUNE = new CreatureSay(0, 11, 801, SystemMessageId.ARRIVED_AT_RUNE);
      this.ARRIVED_AT_RUNE_2 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_LEAVE_FOR_GLUDIN_AFTER_10_MINUTES);
      this.LEAVE_RUNE5 = new CreatureSay(0, 11, 801, SystemMessageId.DEPARTURE_FOR_GLUDIN_5_MINUTES);
      this.LEAVE_RUNE1 = new CreatureSay(0, 11, 801, SystemMessageId.DEPARTURE_FOR_GLUDIN_1_MINUTE);
      this.LEAVE_RUNE0 = new CreatureSay(0, 11, 801, SystemMessageId.DEPARTURE_FOR_GLUDIN_SHORTLY);
      this.LEAVING_RUNE = new CreatureSay(0, 11, 801, SystemMessageId.DEPARTURE_FOR_GLUDIN_NOW);
      this.BUSY_GLUDIN = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_RUNE_GLUDIN_DELAYED);
      this.BUSY_RUNE = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_GLUDIN_RUNE_DELAYED);
      this.ARRIVAL_RUNE15 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_FROM_GLUDIN_AT_RUNE_15_MINUTES);
      this.ARRIVAL_RUNE10 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_FROM_GLUDIN_AT_RUNE_10_MINUTES);
      this.ARRIVAL_RUNE5 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_FROM_GLUDIN_AT_RUNE_5_MINUTES);
      this.ARRIVAL_RUNE1 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_FROM_GLUDIN_AT_RUNE_1_MINUTE);
      this.ARRIVAL_GLUDIN15 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_FROM_RUNE_AT_GLUDIN_15_MINUTES);
      this.ARRIVAL_GLUDIN10 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_FROM_RUNE_AT_GLUDIN_10_MINUTES);
      this.ARRIVAL_GLUDIN5 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_FROM_RUNE_AT_GLUDIN_5_MINUTES);
      this.ARRIVAL_GLUDIN1 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_FROM_RUNE_AT_GLUDIN_1_MINUTE);
      this.GLUDIN_SOUND = new PlaySound(
         0, "itemsound.ship_arrival_departure", 1, this._boat.getObjectId(), GLUDIN_DOCK[0].getX(), GLUDIN_DOCK[0].getY(), GLUDIN_DOCK[0].getZ()
      );
      this.RUNE_SOUND = new PlaySound(
         0, "itemsound.ship_arrival_departure", 1, this._boat.getObjectId(), RUNE_DOCK[0].getX(), RUNE_DOCK[0].getY(), RUNE_DOCK[0].getZ()
      );
   }

   @Override
   public void run() {
      try {
         switch(this._cycle) {
            case 0:
               BoatManager.getInstance().broadcastPacket(GLUDIN_DOCK[0], RUNE_DOCK[0], this.LEAVE_GLUDIN5);
               ThreadPoolManager.getInstance().schedule(this, 240000L);
               break;
            case 1:
               BoatManager.getInstance().broadcastPacket(GLUDIN_DOCK[0], RUNE_DOCK[0], this.LEAVE_GLUDIN1);
               ThreadPoolManager.getInstance().schedule(this, 40000L);
               break;
            case 2:
               BoatManager.getInstance().broadcastPacket(GLUDIN_DOCK[0], RUNE_DOCK[0], this.LEAVE_GLUDIN0);
               ThreadPoolManager.getInstance().schedule(this, 20000L);
               break;
            case 3:
               BoatManager.getInstance().dockShip(2, false);
               BoatManager.getInstance().broadcastPackets(GLUDIN_DOCK[0], RUNE_DOCK[0], this.LEAVING_GLUDIN);
               this._boat.broadcastPacket(this.GLUDIN_SOUND);
               this._boat.payForRide(7905, 1, -90015, 150422, -3610);
               this._boat.executePath(GLUDIN_TO_RUNE);
               ThreadPoolManager.getInstance().schedule(this, 250000L);
               break;
            case 4:
               BoatManager.getInstance().broadcastPacket(RUNE_DOCK[0], GLUDIN_DOCK[0], this.ARRIVAL_RUNE15);
               ThreadPoolManager.getInstance().schedule(this, 300000L);
               break;
            case 5:
               BoatManager.getInstance().broadcastPacket(RUNE_DOCK[0], GLUDIN_DOCK[0], this.ARRIVAL_RUNE10);
               ThreadPoolManager.getInstance().schedule(this, 300000L);
               break;
            case 6:
               BoatManager.getInstance().broadcastPacket(RUNE_DOCK[0], GLUDIN_DOCK[0], this.ARRIVAL_RUNE5);
               ThreadPoolManager.getInstance().schedule(this, 240000L);
               break;
            case 7:
               BoatManager.getInstance().broadcastPacket(RUNE_DOCK[0], GLUDIN_DOCK[0], this.ARRIVAL_RUNE1);
               break;
            case 8:
               if (BoatManager.getInstance().dockBusy(3)) {
                  if (this._shoutCount == 0) {
                     BoatManager.getInstance().broadcastPacket(RUNE_DOCK[0], GLUDIN_DOCK[0], this.BUSY_RUNE);
                  }

                  ++this._shoutCount;
                  if (this._shoutCount > 35) {
                     this._shoutCount = 0;
                  }

                  ThreadPoolManager.getInstance().schedule(this, 5000L);
                  return;
               }

               this._boat.executePath(RUNE_DOCK);
               break;
            case 9:
               BoatManager.getInstance().dockShip(3, true);
               BoatManager.getInstance().broadcastPackets(RUNE_DOCK[0], GLUDIN_DOCK[0], this.ARRIVED_AT_RUNE, this.ARRIVED_AT_RUNE_2);
               this._boat.broadcastPacket(this.RUNE_SOUND);
               ThreadPoolManager.getInstance().schedule(this, 300000L);
               break;
            case 10:
               BoatManager.getInstance().broadcastPacket(RUNE_DOCK[0], GLUDIN_DOCK[0], this.LEAVE_RUNE5);
               ThreadPoolManager.getInstance().schedule(this, 240000L);
               break;
            case 11:
               BoatManager.getInstance().broadcastPacket(RUNE_DOCK[0], GLUDIN_DOCK[0], this.LEAVE_RUNE1);
               ThreadPoolManager.getInstance().schedule(this, 40000L);
               break;
            case 12:
               BoatManager.getInstance().broadcastPacket(RUNE_DOCK[0], GLUDIN_DOCK[0], this.LEAVE_RUNE0);
               ThreadPoolManager.getInstance().schedule(this, 20000L);
               break;
            case 13:
               BoatManager.getInstance().dockShip(3, false);
               BoatManager.getInstance().broadcastPackets(RUNE_DOCK[0], GLUDIN_DOCK[0], this.LEAVING_RUNE);
               this._boat.broadcastPacket(this.RUNE_SOUND);
               this._boat.payForRide(7904, 1, 34513, -38009, -3640);
               this._boat.executePath(RUNE_TO_GLUDIN);
               ThreadPoolManager.getInstance().schedule(this, 60000L);
               break;
            case 14:
               BoatManager.getInstance().broadcastPacket(GLUDIN_DOCK[0], RUNE_DOCK[0], this.ARRIVAL_GLUDIN15);
               ThreadPoolManager.getInstance().schedule(this, 300000L);
               break;
            case 15:
               BoatManager.getInstance().broadcastPacket(GLUDIN_DOCK[0], RUNE_DOCK[0], this.ARRIVAL_GLUDIN10);
               ThreadPoolManager.getInstance().schedule(this, 300000L);
               break;
            case 16:
               BoatManager.getInstance().broadcastPacket(GLUDIN_DOCK[0], RUNE_DOCK[0], this.ARRIVAL_GLUDIN5);
               ThreadPoolManager.getInstance().schedule(this, 240000L);
               break;
            case 17:
               BoatManager.getInstance().broadcastPacket(GLUDIN_DOCK[0], RUNE_DOCK[0], this.ARRIVAL_GLUDIN1);
               break;
            case 18:
               if (BoatManager.getInstance().dockBusy(2)) {
                  if (this._shoutCount == 0) {
                     BoatManager.getInstance().broadcastPacket(GLUDIN_DOCK[0], RUNE_DOCK[0], this.BUSY_GLUDIN);
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
            case 19:
               BoatManager.getInstance().dockShip(2, true);
               BoatManager.getInstance().broadcastPackets(GLUDIN_DOCK[0], RUNE_DOCK[0], this.ARRIVED_AT_GLUDIN, this.ARRIVED_AT_GLUDIN_2);
               this._boat.broadcastPacket(this.GLUDIN_SOUND);
               ThreadPoolManager.getInstance().schedule(this, 300000L);
         }

         this._shoutCount = 0;
         ++this._cycle;
         if (this._cycle > 19) {
            this._cycle = 0;
         }
      } catch (Exception var2) {
         _log.log(Level.WARNING, var2.getMessage());
      }
   }

   public static void main(String[] args) {
      new BoatGludinRune(-1, BoatGludinRune.class.getSimpleName(), "vehicles");
   }
}
