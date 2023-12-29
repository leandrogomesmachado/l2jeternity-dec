package l2e.gameserver.handler.bypasshandlers.impl;

import java.util.StringTokenizer;
import l2e.gameserver.Config;
import l2e.gameserver.handler.bypasshandlers.IBypassHandler;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.MerchantInstance;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.SetupGauge;

public class RentPet implements IBypassHandler {
   private static final String[] COMMANDS = new String[]{"RentPet"};

   @Override
   public boolean useBypass(String command, Player activeChar, Creature target) {
      if (!(target instanceof MerchantInstance)) {
         return false;
      } else if (!Config.ALLOW_RENTPET) {
         return false;
      } else if (!Config.LIST_PET_RENT_NPC.contains(((Npc)target).getTemplate().getId())) {
         return false;
      } else {
         try {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            if (st.countTokens() < 1) {
               NpcHtmlMessage msg = new NpcHtmlMessage(((Npc)target).getObjectId());
               msg.setHtml(
                  activeChar,
                  "<html><body>Pet Manager:<br>You can rent a wyvern or strider for adena.<br>My prices:<br1><table border=0><tr><td>Ride</td></tr><tr><td>Wyvern</td><td>Strider</td></tr><tr><td><a action=\"bypass -h npc_%objectId%_RentPet 1\">30 sec/1800 adena</a></td><td><a action=\"bypass -h npc_%objectId%_RentPet 11\">30 sec/900 adena</a></td></tr><tr><td><a action=\"bypass -h npc_%objectId%_RentPet 2\">1 min/7200 adena</a></td><td><a action=\"bypass -h npc_%objectId%_RentPet 12\">1 min/3600 adena</a></td></tr><tr><td><a action=\"bypass -h npc_%objectId%_RentPet 3\">10 min/720000 adena</a></td><td><a action=\"bypass -h npc_%objectId%_RentPet 13\">10 min/360000 adena</a></td></tr><tr><td><a action=\"bypass -h npc_%objectId%_RentPet 4\">30 min/6480000 adena</a></td><td><a action=\"bypass -h npc_%objectId%_RentPet 14\">30 min/3240000 adena</a></td></tr></table></body></html>"
               );
               msg.replace("%objectId%", String.valueOf(((Npc)target).getObjectId()));
               activeChar.sendPacket(msg);
            } else {
               tryRentPet(activeChar, Integer.parseInt(st.nextToken()));
            }

            return true;
         } catch (Exception var6) {
            _log.info("Exception in " + this.getClass().getSimpleName());
            return false;
         }
      }
   }

   public static final void tryRentPet(Player player, int val) {
      if (player != null && !player.hasSummon() && !player.isMounted() && !player.isRentedPet() && !player.isTransformed() && !player.isCursedWeaponEquipped()
         )
       {
         if (player.disarmWeapons()) {
            double price = 1.0;
            int[] cost = new int[]{1800, 7200, 720000, 6480000};
            int[] ridetime = new int[]{30, 60, 600, 1800};
            int petId;
            if (val > 10) {
               petId = 12526;
               val -= 10;
               price /= 2.0;
            } else {
               petId = 12621;
            }

            if (val >= 1 && val <= 4) {
               price *= (double)cost[val - 1];
               int time = ridetime[val - 1];
               if (player.reduceAdena("Rent", (long)price, player.getLastFolkNPC(), true)) {
                  player.mount(petId, 0, false);
                  SetupGauge sg = new SetupGauge(player, 3, time * 1000);
                  player.sendPacket(sg);
                  player.startRentPet(time);
               }
            }
         }
      }
   }

   @Override
   public String[] getBypassList() {
      return COMMANDS;
   }
}
