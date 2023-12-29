package l2e.gameserver.handler.bypasshandlers.impl;

import java.util.StringTokenizer;
import java.util.logging.Level;
import l2e.gameserver.handler.bypasshandlers.IBypassHandler;
import l2e.gameserver.instancemanager.SiegeManager;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.events.cleft.AerialCleftEvent;
import l2e.gameserver.network.SystemMessageId;

public class Observation implements IBypassHandler {
   private static final String[] COMMANDS = new String[]{"observesiege", "observeoracle", "observe"};

   @Override
   public boolean useBypass(String command, Player activeChar, Creature target) {
      if (!target.isNpc()) {
         return false;
      } else {
         try {
            if (command.toLowerCase().startsWith(COMMANDS[0])) {
               String val = command.substring(13);
               StringTokenizer st = new StringTokenizer(val);
               st.nextToken();
               if (SiegeManager.getInstance().getSiege(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()))
                  != null) {
                  doObserve(activeChar, (Npc)target, val);
               } else {
                  activeChar.sendPacket(SystemMessageId.ONLY_VIEW_SIEGE);
               }

               return true;
            } else if (command.toLowerCase().startsWith(COMMANDS[1])) {
               String val = command.substring(13);
               StringTokenizer st = new StringTokenizer(val);
               st.nextToken();
               doObserve(activeChar, (Npc)target, val);
               return true;
            } else if (command.toLowerCase().startsWith(COMMANDS[2])) {
               doObserve(activeChar, (Npc)target, command.substring(8));
               return true;
            } else {
               return false;
            }
         } catch (Exception var6) {
            _log.log(Level.WARNING, "Exception in " + this.getClass().getSimpleName(), (Throwable)var6);
            return false;
         }
      }
   }

   private static final void doObserve(Player player, Npc npc, String val) {
      if (!player.isInParty() && !player.isPartyBanned() && !player.isCursedWeaponEquipped() && !player.isCursedWeaponEquipped() && !player.isInFightEvent()) {
         if ((AerialCleftEvent.getInstance().isStarted() || AerialCleftEvent.getInstance().isRewarding())
            && AerialCleftEvent.getInstance().isPlayerParticipant(player.getObjectId())) {
            player.sendMessage("You can not observe games while registered for event.");
         } else {
            StringTokenizer st = new StringTokenizer(val);
            long cost = Long.parseLong(st.nextToken());
            int x = Integer.parseInt(st.nextToken());
            int y = Integer.parseInt(st.nextToken());
            int z = Integer.parseInt(st.nextToken());
            if (player.reduceAdena("Broadcast", cost, npc, true)) {
               player.enterObserverMode(x, y, z);
               player.sendItemList(false);
            }

            player.sendActionFailed();
         }
      } else {
         player.sendMessage("You can not observe games while registered for event.");
      }
   }

   @Override
   public String[] getBypassList() {
      return COMMANDS;
   }
}
