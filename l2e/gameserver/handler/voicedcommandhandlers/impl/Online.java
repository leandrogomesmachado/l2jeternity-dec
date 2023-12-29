package l2e.gameserver.handler.voicedcommandhandlers.impl;

import l2e.gameserver.Config;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;

public class Online implements IVoicedCommandHandler {
   private static String[] _voicedCommands = new String[]{"online"};

   @Override
   public boolean useVoicedCommand(String command, Player activeChar, String target) {
      if (command.equalsIgnoreCase("online") && Config.VOICE_ONLINE_ENABLE) {
         int currentOnline = (int)((double)World.getInstance().getAllPlayers().size() * Config.FAKE_ONLINE);
         activeChar.sendMessage("Total online: " + currentOnline + " players.");
      }

      return true;
   }

   @Override
   public String[] getVoicedCommandList() {
      return _voicedCommands;
   }
}
