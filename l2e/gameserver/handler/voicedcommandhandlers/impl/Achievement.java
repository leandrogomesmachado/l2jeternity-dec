package l2e.gameserver.handler.voicedcommandhandlers.impl;

import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.events.custom.achievements.AchievementManager;

public class Achievement implements IVoicedCommandHandler {
   private static final String[] _voicedCommands = new String[]{"ach", "acv"};

   @Override
   public boolean useVoicedCommand(String command, Player player, String target) {
      if (!AchievementManager.getInstance().isActive()) {
         return false;
      } else {
         AchievementManager.getInstance().onBypass(player, "_bbs_achievements", null);
         return true;
      }
   }

   @Override
   public String[] getVoicedCommandList() {
      return _voicedCommands;
   }
}
