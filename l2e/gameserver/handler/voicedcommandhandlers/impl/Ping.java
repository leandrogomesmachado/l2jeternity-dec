package l2e.gameserver.handler.voicedcommandhandlers.impl;

import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.NetPing;

public class Ping implements IVoicedCommandHandler {
   private static String[] _voicedCommands = new String[]{"ping"};

   @Override
   public boolean useVoicedCommand(String command, Player activeChar, String target) {
      if (command.equalsIgnoreCase("ping")) {
         activeChar.sendMessage("Processing request...");
         activeChar.sendPacket(new NetPing(activeChar));
         ThreadPoolManager.getInstance().schedule(new Ping.AnswerTask(activeChar), 3000L);
      }

      return true;
   }

   @Override
   public String[] getVoicedCommandList() {
      return _voicedCommands;
   }

   protected static final class AnswerTask implements Runnable {
      private final Player _player;

      public AnswerTask(Player player) {
         this._player = player;
      }

      @Override
      public void run() {
         int ping = this._player.getPing();
         if (ping != -1) {
            this._player.sendMessage("Current ping: " + ping + " ms.");
         } else {
            this._player.sendMessage("The data from the client was not received.");
         }
      }
   }
}
