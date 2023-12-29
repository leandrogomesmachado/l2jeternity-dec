package l2e.gameserver.handler.voicedcommandhandlers.impl;

import l2e.gameserver.Config;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.strings.server.ServerMessage;

public class HideBuffsAnimation implements IVoicedCommandHandler {
   private static final String[] _voicedCommands = new String[]{"notbuffanim", "notbuffanimation", "buffanim", "buffanimation"};

   @Override
   public boolean useVoicedCommand(String command, Player activeChar, String target) {
      if (!Config.ALLOW_HIDE_BUFFS_ANIMATION_COMMAND) {
         return false;
      } else {
         if (activeChar.getBlockBuffs()) {
            activeChar.setVar("useBlockBuffs@", "0");
            activeChar.sendMessage(new ServerMessage("BuffsAnim.DISABLED", activeChar.getLang()).toString());
         } else {
            activeChar.setVar("useBlockBuffs@", "1");
            activeChar.sendMessage(new ServerMessage("BuffsAnim.ENABLED", activeChar.getLang()).toString());
         }

         return true;
      }
   }

   @Override
   public String[] getVoicedCommandList() {
      return _voicedCommands;
   }
}
