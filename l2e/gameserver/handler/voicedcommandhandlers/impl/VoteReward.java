package l2e.gameserver.handler.voicedcommandhandlers.impl;

import java.util.Map;
import l2e.gameserver.data.parser.VoteRewardParser;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.mods.votereward.VoteRewardSite;
import l2e.gameserver.model.strings.server.ServerMessage;

public class VoteReward implements IVoicedCommandHandler {
   private static final String[] _voicedCommands = new String[]{"vote"};

   @Override
   public boolean useVoicedCommand(String command, Player activeChar, String target) {
      if (command.equalsIgnoreCase("vote")) {
         Map<String, VoteRewardSite> sites = VoteRewardParser.getInstance().getVoteRewardSites();
         if (sites == null || sites.isEmpty()) {
            return false;
         }

         if (activeChar.getLevel() < VoteRewardParser.getInstance().getMinLevel()) {
            activeChar.sendMessage(new ServerMessage("VoteReward.LOW_LEVEL", activeChar.getLang()).toString());
            return false;
         }

         boolean received = false;

         for(VoteRewardSite site : sites.values()) {
            if (site.isEnabled() && site.tryGiveRewards(activeChar)) {
               received = true;
            }
         }

         if (!received) {
            activeChar.sendMessage(new ServerMessage("VoteReward.NOT_HAVE_VOTES", activeChar.getLang()).toString());
         }
      }

      return true;
   }

   @Override
   public String[] getVoicedCommandList() {
      return _voicedCommands;
   }
}
