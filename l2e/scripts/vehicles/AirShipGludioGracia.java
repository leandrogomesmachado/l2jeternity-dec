package l2e.scripts.vehicles;

import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.instancemanager.AirShipManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.AirShipInstance;
import l2e.gameserver.model.actor.templates.VehicleTemplate;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class AirShipGludioGracia extends Quest implements Runnable {
   private static final int[] CONTROLLERS = new int[]{32607, 32609};
   private static final int GLUDIO_DOCK_ID = 10;
   private static final int GRACIA_DOCK_ID = 11;
   private static final Location OUST_GLUDIO = new Location(-149379, 255246, -80);
   private static final Location OUST_GRACIA = new Location(-186563, 243590, 2608);
   private static final VehicleTemplate[] GLUDIO_TO_WARPGATE = new VehicleTemplate[]{
      new VehicleTemplate(-151216, 252544, 231), new VehicleTemplate(-160416, 256144, 222), new VehicleTemplate(-167888, 256720, -509, 0, 41035)
   };
   private static final VehicleTemplate[] WARPGATE_TO_GRACIA = new VehicleTemplate[]{
      new VehicleTemplate(-169776, 254800, 282),
      new VehicleTemplate(-171824, 250048, 425),
      new VehicleTemplate(-172608, 247728, 398),
      new VehicleTemplate(-174544, 246176, 39),
      new VehicleTemplate(-179440, 243648, 1337),
      new VehicleTemplate(-182608, 243952, 2739),
      new VehicleTemplate(-184960, 245120, 2694),
      new VehicleTemplate(-186944, 244560, 2617)
   };
   private static final VehicleTemplate[] GRACIA_TO_WARPGATE = new VehicleTemplate[]{
      new VehicleTemplate(-187808, 244992, 2672),
      new VehicleTemplate(-188528, 245920, 2465),
      new VehicleTemplate(-189936, 245232, 1682),
      new VehicleTemplate(-191200, 242960, 1523),
      new VehicleTemplate(-190416, 239088, 1706),
      new VehicleTemplate(-187488, 237104, 2768),
      new VehicleTemplate(-184688, 238432, 2802),
      new VehicleTemplate(-184528, 241104, 2816),
      new VehicleTemplate(-182144, 243376, 2733),
      new VehicleTemplate(-179440, 243648, 1337),
      new VehicleTemplate(-174544, 246176, 39),
      new VehicleTemplate(-172608, 247728, 398),
      new VehicleTemplate(-171824, 250048, 425),
      new VehicleTemplate(-169776, 254800, 282),
      new VehicleTemplate(-168080, 256624, 343),
      new VehicleTemplate(-157264, 255664, 221, 0, 64781)
   };
   private static final VehicleTemplate[] WARPGATE_TO_GLUDIO = new VehicleTemplate[]{
      new VehicleTemplate(-153424, 255376, 221),
      new VehicleTemplate(-149552, 258160, 221),
      new VehicleTemplate(-146896, 257088, 221),
      new VehicleTemplate(-146672, 254224, 221),
      new VehicleTemplate(-147856, 252704, 206),
      new VehicleTemplate(-149392, 252544, 198)
   };
   private final AirShipInstance _ship;
   private int _cycle = 0;
   private boolean _foundAtcGludio = false;
   private Npc _atcGludio = null;
   private boolean _foundAtcGracia = false;
   private Npc _atcGracia = null;

   public AirShipGludioGracia(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(CONTROLLERS);
      this.addFirstTalkId(CONTROLLERS);
      this.addTalkId(CONTROLLERS);
      this._ship = AirShipManager.getInstance().getNewAirShip(-149378, 252552, 198, 33837);
      this._ship.setOustLoc(OUST_GLUDIO);
      this._ship.setInDock(10);
      this._ship.registerEngine(this);
      this._ship.runEngine(60000);
   }

   private final void broadcastInGludio(NpcStringId npcString) {
      if (!this._foundAtcGludio) {
         this._foundAtcGludio = true;
         this._atcGludio = this.findController();
      }

      if (this._atcGludio != null) {
         this._atcGludio.broadcastPacket(new NpcSay(this._atcGludio.getObjectId(), 23, this._atcGludio.getId(), npcString));
      }
   }

   private final void broadcastInGracia(NpcStringId npcStringId) {
      if (!this._foundAtcGracia) {
         this._foundAtcGracia = true;
         this._atcGracia = this.findController();
      }

      if (this._atcGracia != null) {
         this._atcGracia.broadcastPacket(new NpcSay(this._atcGracia.getObjectId(), 23, this._atcGracia.getId(), npcStringId));
      }
   }

   private final Npc findController() {
      for(Npc obj : World.getInstance().getAroundNpc(this._ship, 600, 400)) {
         for(int id : CONTROLLERS) {
            if (obj.getId() == id) {
               return obj;
            }
         }
      }

      return null;
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
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
      } else if (!player.hasSummon() && !player.isMounted()) {
         if (this._ship.isInDock() && this._ship.isInsideRadius(player, 600, true, false)) {
            this._ship.addPassenger(player);
         }

         return null;
      } else {
         player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_A_PET_OR_A_SERVITOR_IS_SUMMONED);
         return null;
      }
   }

   @Override
   public final String onFirstTalk(Npc npc, Player player) {
      return npc.getId() + ".htm";
   }

   @Override
   public void run() {
      try {
         switch(this._cycle) {
            case 0:
               this.broadcastInGludio(NpcStringId.THE_REGULARLY_SCHEDULED_AIRSHIP_THAT_FLIES_TO_THE_GRACIA_CONTINENT_HAS_DEPARTED);
               this._ship.setInDock(0);
               this._ship.executePath(GLUDIO_TO_WARPGATE);
               break;
            case 1:
               this._ship.setOustLoc(OUST_GRACIA);
               ThreadPoolManager.getInstance().schedule(this, 5000L);
               break;
            case 2:
               this._ship.executePath(WARPGATE_TO_GRACIA);
               break;
            case 3:
               this.broadcastInGracia(NpcStringId.THE_REGULARLY_SCHEDULED_AIRSHIP_HAS_ARRIVED_IT_WILL_DEPART_FOR_THE_ADEN_CONTINENT_IN_1_MINUTE);
               this._ship.setInDock(11);
               this._ship.oustPlayers();
               ThreadPoolManager.getInstance().schedule(this, 60000L);
               break;
            case 4:
               this.broadcastInGracia(NpcStringId.THE_REGULARLY_SCHEDULED_AIRSHIP_THAT_FLIES_TO_THE_ADEN_CONTINENT_HAS_DEPARTED);
               this._ship.setInDock(0);
               this._ship.executePath(GRACIA_TO_WARPGATE);
               break;
            case 5:
               this._ship.setOustLoc(OUST_GLUDIO);
               ThreadPoolManager.getInstance().schedule(this, 5000L);
               break;
            case 6:
               this._ship.executePath(WARPGATE_TO_GLUDIO);
               break;
            case 7:
               this.broadcastInGludio(NpcStringId.THE_REGULARLY_SCHEDULED_AIRSHIP_HAS_ARRIVED_IT_WILL_DEPART_FOR_THE_GRACIA_CONTINENT_IN_1_MINUTE);
               this._ship.setInDock(10);
               this._ship.oustPlayers();
               ThreadPoolManager.getInstance().schedule(this, 60000L);
         }

         ++this._cycle;
         if (this._cycle > 7) {
            this._cycle = 0;
         }
      } catch (Exception var2) {
         var2.printStackTrace();
      }
   }

   @Override
   public boolean unload(boolean removeFromList) {
      if (this._ship != null) {
         this._ship.oustPlayers();
         this._ship.deleteMe();
      }

      return super.unload(removeFromList);
   }

   public static void main(String[] args) {
      new AirShipGludioGracia(-1, AirShipGludioGracia.class.getSimpleName(), "vehicles");
   }
}
