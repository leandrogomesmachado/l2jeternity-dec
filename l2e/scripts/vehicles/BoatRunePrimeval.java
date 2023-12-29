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

public class BoatRunePrimeval extends Quest implements Runnable {
   private static final Logger _log = Logger.getLogger(BoatRunePrimeval.class.getName());
   private static final VehicleTemplate[] RUNE_TO_PRIMEVAL = new VehicleTemplate[]{
      new VehicleTemplate(32750, -39300, -3610, 180, 800),
      new VehicleTemplate(27440, -39328, -3610, 250, 1000),
      new VehicleTemplate(19616, -39360, -3610, 270, 1000),
      new VehicleTemplate(3840, -38528, -3610, 270, 1000),
      new VehicleTemplate(1664, -37120, -3610, 270, 1000),
      new VehicleTemplate(896, -34560, -3610, 180, 1800),
      new VehicleTemplate(832, -31104, -3610, 180, 180),
      new VehicleTemplate(2240, -29132, -3610, 150, 1800),
      new VehicleTemplate(4160, -27828, -3610, 150, 1800),
      new VehicleTemplate(5888, -27279, -3610, 150, 1800),
      new VehicleTemplate(7000, -27279, -3610, 150, 1800),
      new VehicleTemplate(10342, -27279, -3610, 150, 1800)
   };
   private static final VehicleTemplate[] PRIMEVAL_TO_RUNE = new VehicleTemplate[]{
      new VehicleTemplate(15528, -27279, -3610, 180, 800),
      new VehicleTemplate(22304, -29664, -3610, 290, 800),
      new VehicleTemplate(33824, -26880, -3610, 290, 800),
      new VehicleTemplate(38848, -21792, -3610, 240, 1200),
      new VehicleTemplate(43424, -22080, -3610, 180, 1800),
      new VehicleTemplate(44320, -25152, -3610, 180, 1800),
      new VehicleTemplate(40576, -31616, -3610, 250, 800),
      new VehicleTemplate(36819, -35315, -3610, 220, 800)
   };
   private static final VehicleTemplate[] RUNE_DOCK = new VehicleTemplate[]{new VehicleTemplate(34381, -37680, -3610, 220, 800)};
   private static final VehicleTemplate PRIMEVAL_DOCK = RUNE_TO_PRIMEVAL[RUNE_TO_PRIMEVAL.length - 1];
   private final BoatInstance _boat;
   private int _cycle = 0;
   private int _shoutCount = 0;
   private final CreatureSay ARRIVED_AT_RUNE;
   private final CreatureSay ARRIVED_AT_RUNE_2;
   private final CreatureSay LEAVING_RUNE;
   private final CreatureSay ARRIVED_AT_PRIMEVAL;
   private final CreatureSay ARRIVED_AT_PRIMEVAL_2;
   private final CreatureSay LEAVING_PRIMEVAL;
   private final CreatureSay BUSY_RUNE;
   private final PlaySound RUNE_SOUND;
   private final PlaySound PRIMEVAL_SOUND;

   public BoatRunePrimeval(int questId, String name, String descr) {
      super(questId, name, descr);
      this._boat = BoatManager.getInstance().getNewBoat(5, 34381, -37680, -3610, 40785);
      this._boat.registerEngine(this);
      this._boat.runEngine(180000);
      BoatManager.getInstance().dockShip(3, true);
      this.ARRIVED_AT_RUNE = new CreatureSay(0, 11, 801, SystemMessageId.ARRIVED_AT_RUNE);
      this.ARRIVED_AT_RUNE_2 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_LEAVING_FOR_PRIMEVAL_3_MINUTES);
      this.LEAVING_RUNE = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_LEAVING_RUNE_FOR_PRIMEVAL_NOW);
      this.ARRIVED_AT_PRIMEVAL = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_ARRIVED_AT_PRIMEVAL);
      this.ARRIVED_AT_PRIMEVAL_2 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_LEAVING_FOR_RUNE_3_MINUTES);
      this.LEAVING_PRIMEVAL = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_LEAVING_PRIMEVAL_FOR_RUNE_NOW);
      this.BUSY_RUNE = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_FROM_PRIMEVAL_TO_RUNE_DELAYED);
      this.RUNE_SOUND = new PlaySound(
         0, "itemsound.ship_arrival_departure", 1, this._boat.getObjectId(), RUNE_DOCK[0].getX(), RUNE_DOCK[0].getY(), RUNE_DOCK[0].getZ()
      );
      this.PRIMEVAL_SOUND = new PlaySound(
         0, "itemsound.ship_arrival_departure", 1, this._boat.getObjectId(), PRIMEVAL_DOCK.getX(), PRIMEVAL_DOCK.getY(), PRIMEVAL_DOCK.getZ()
      );
   }

   @Override
   public void run() {
      try {
         switch(this._cycle) {
            case 0:
               BoatManager.getInstance().dockShip(3, false);
               BoatManager.getInstance().broadcastPackets(RUNE_DOCK[0], PRIMEVAL_DOCK, this.LEAVING_RUNE, this.RUNE_SOUND);
               this._boat.payForRide(8925, 1, 34513, -38009, -3640);
               this._boat.executePath(RUNE_TO_PRIMEVAL);
               break;
            case 1:
               BoatManager.getInstance()
                  .broadcastPackets(PRIMEVAL_DOCK, RUNE_DOCK[0], this.ARRIVED_AT_PRIMEVAL, this.ARRIVED_AT_PRIMEVAL_2, this.PRIMEVAL_SOUND);
               ThreadPoolManager.getInstance().schedule(this, 180000L);
               break;
            case 2:
               BoatManager.getInstance().broadcastPackets(PRIMEVAL_DOCK, RUNE_DOCK[0], this.LEAVING_PRIMEVAL, this.PRIMEVAL_SOUND);
               this._boat.payForRide(8924, 1, 10447, -24982, -3664);
               this._boat.executePath(PRIMEVAL_TO_RUNE);
               break;
            case 3:
               if (BoatManager.getInstance().dockBusy(3)) {
                  if (this._shoutCount == 0) {
                     BoatManager.getInstance().broadcastPacket(RUNE_DOCK[0], PRIMEVAL_DOCK, this.BUSY_RUNE);
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
            case 4:
               BoatManager.getInstance().dockShip(3, true);
               BoatManager.getInstance().broadcastPackets(RUNE_DOCK[0], PRIMEVAL_DOCK, this.ARRIVED_AT_RUNE, this.ARRIVED_AT_RUNE_2, this.RUNE_SOUND);
               ThreadPoolManager.getInstance().schedule(this, 180000L);
         }

         this._shoutCount = 0;
         ++this._cycle;
         if (this._cycle > 4) {
            this._cycle = 0;
         }
      } catch (Exception var2) {
         _log.log(Level.WARNING, var2.getMessage());
      }
   }

   public static void main(String[] args) {
      new BoatRunePrimeval(-1, BoatRunePrimeval.class.getSimpleName(), "vehicles");
   }
}
