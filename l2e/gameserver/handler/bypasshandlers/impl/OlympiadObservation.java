package l2e.gameserver.handler.bypasshandlers.impl;

import java.util.logging.Level;
import l2e.gameserver.handler.bypasshandlers.IBypassHandler;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.OlympiadManagerInstance;
import l2e.gameserver.model.entity.events.cleft.AerialCleftEvent;
import l2e.gameserver.model.olympiad.Olympiad;
import l2e.gameserver.model.olympiad.OlympiadGameManager;
import l2e.gameserver.model.olympiad.OlympiadGameTask;
import l2e.gameserver.model.olympiad.OlympiadManager;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExReceiveOlympiadList;

public class OlympiadObservation implements IBypassHandler {
   private static final String[] COMMANDS = new String[]{"watchmatch", "arenachange"};

   @Override
   public final boolean useBypass(String command, Player activeChar, Creature target) {
      try {
         Npc olymanager = activeChar.getLastFolkNPC();
         if (command.startsWith(COMMANDS[0])) {
            if (!Olympiad.getInstance().inCompPeriod()) {
               activeChar.sendPacket(SystemMessageId.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
               return false;
            }

            activeChar.sendPacket(new ExReceiveOlympiadList.OlympiadList());
         } else {
            if (olymanager == null || !(olymanager instanceof OlympiadManagerInstance)) {
               return false;
            }

            if (!activeChar.inObserverMode() && !activeChar.isInsideRadius(olymanager, 300, false, false)) {
               return false;
            }

            if (OlympiadManager.getInstance().isRegisteredInComp(activeChar)) {
               activeChar.sendPacket(SystemMessageId.WHILE_YOU_ARE_ON_THE_WAITING_LIST_YOU_ARE_NOT_ALLOWED_TO_WATCH_THE_GAME);
               return false;
            }

            if (!Olympiad.getInstance().inCompPeriod()) {
               activeChar.sendPacket(SystemMessageId.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
               return false;
            }

            if (activeChar.getUCState() > 0 || activeChar.isInFightEvent() || AerialCleftEvent.getInstance().isPlayerParticipant(activeChar.getObjectId())) {
               activeChar.sendMessage("You can not observe games while registered for Event");
               return false;
            }

            int arenaId = Integer.parseInt(command.substring(12).trim());
            OlympiadGameTask nextArena = OlympiadGameManager.getInstance().getOlympiadTask(arenaId);
            if (nextArena != null) {
               activeChar.enterOlympiadObserverMode(nextArena.getZone().getSpectatorSpawns().get(0), arenaId);
               activeChar.setReflectionId(OlympiadGameManager.getInstance().getOlympiadTask(arenaId).getZone().getReflectionId());
            }
         }

         return true;
      } catch (Exception var7) {
         _log.log(Level.WARNING, "Exception in " + this.getClass().getSimpleName(), (Throwable)var7);
         return false;
      }
   }

   @Override
   public final String[] getBypassList() {
      return COMMANDS;
   }
}
