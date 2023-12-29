package l2e.gameserver.handler.voicedcommandhandlers.impl;

import l2e.gameserver.Config;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class Security implements IVoicedCommandHandler {
   private final String[] _commandList = new String[]{"lock", "unlock", "lockIp", "lockHwid", "unlockIp", "unlockHwid"};

   @Override
   public boolean useVoicedCommand(String command, Player activeChar, String target) {
      if (!Config.ALLOW_SECURITY_COMMAND) {
         return false;
      } else if (command.equalsIgnoreCase("lock")) {
         NpcHtmlMessage html = new NpcHtmlMessage(activeChar.getObjectId());
         html.setFile(activeChar, activeChar.getLang(), "data/html/mods/lock/lock.htm");
         html.replace("%curIP%", activeChar.getIPAddress());
         activeChar.sendPacket(html);
         return true;
      } else if (command.equalsIgnoreCase("lockIp")) {
         if (!Config.ALLOW_IP_LOCK) {
            activeChar.sendMessage(new ServerMessage("Security.DISABLED", activeChar.getLang()).toString());
            this.useVoicedCommand("lock", activeChar, target);
            return true;
         } else {
            activeChar.setVar("lockIp", activeChar.getIPAddress());
            NpcHtmlMessage html = new NpcHtmlMessage(activeChar.getObjectId());
            html.setFile(activeChar, activeChar.getLang(), "data/html/mods/lock/lock_ip.htm");
            html.replace("%curIP%", activeChar.getIPAddress());
            activeChar.sendPacket(html);
            return true;
         }
      } else if (command.equalsIgnoreCase("lockHwid")) {
         if (!Config.ALLOW_HWID_LOCK) {
            activeChar.sendMessage(new ServerMessage("Security.DISABLED", activeChar.getLang()).toString());
            this.useVoicedCommand("lock", activeChar, target);
            return true;
         } else {
            activeChar.setVar("lockHwid", activeChar.getHWID());
            NpcHtmlMessage html = new NpcHtmlMessage(activeChar.getObjectId());
            html.setFile(activeChar, activeChar.getLang(), "data/html/mods/lock/lock_hwid.htm");
            activeChar.sendPacket(html);
            return true;
         }
      } else if (command.equalsIgnoreCase("unlockIp")) {
         activeChar.setVar("lockIp", 0);
         NpcHtmlMessage html = new NpcHtmlMessage(activeChar.getObjectId());
         html.setFile(activeChar, activeChar.getLang(), "data/html/mods/lock/unlock_ip.htm");
         html.replace("%curIP", activeChar.getIPAddress());
         activeChar.sendPacket(html);
         return true;
      } else if (command.equalsIgnoreCase("unlockHwid")) {
         activeChar.setVar("lockHwid", 0);
         NpcHtmlMessage html = new NpcHtmlMessage(activeChar.getObjectId());
         html.setFile(activeChar, activeChar.getLang(), "data/html/mods/lock/unlock_hwid.htm");
         activeChar.sendPacket(html);
         return true;
      } else {
         return true;
      }
   }

   @Override
   public String[] getVoicedCommandList() {
      return this._commandList;
   }
}
