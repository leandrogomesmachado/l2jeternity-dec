package l2e.gameserver.handler.bypasshandlers.impl;

import java.util.logging.Level;
import l2e.gameserver.handler.bypasshandlers.IBypassHandler;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class Link implements IBypassHandler {
   private static final String[] COMMANDS = new String[]{"Link"};

   @Override
   public boolean useBypass(String command, Player activeChar, Creature target) {
      if (!target.isNpc()) {
         return false;
      } else {
         try {
            String path = command.substring(5).trim();
            if (path.indexOf("..") != -1) {
               return false;
            } else {
               String filename = "data/html/" + path;
               NpcHtmlMessage html = new NpcHtmlMessage(((Npc)target).getObjectId());
               html.setFile(activeChar, activeChar.getLang(), filename);
               html.replace("%objectId%", String.valueOf(((Npc)target).getObjectId()));
               activeChar.sendPacket(html);
               return true;
            }
         } catch (Exception var7) {
            _log.log(Level.WARNING, "Exception in " + this.getClass().getSimpleName(), (Throwable)var7);
            return false;
         }
      }
   }

   @Override
   public String[] getBypassList() {
      return COMMANDS;
   }
}
