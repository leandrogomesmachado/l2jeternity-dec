package l2e.gameserver.handler.voicedcommandhandlers.impl;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.Config;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.model.strings.server.ServerStorage;
import l2e.gameserver.network.serverpackets.CreatureSay;

public class CheckPremium implements IVoicedCommandHandler {
   private static final String[] _voicedCommands = new String[]{"premium", "premiumlist", "premiuminfo"};

   @Override
   public boolean useVoicedCommand(String command, Player activeChar, String target) {
      if (!Config.USE_PREMIUMSERVICE) {
         return false;
      } else if (!command.equals("premium") && !command.equals("premiumlist")) {
         if (!command.equals("premiuminfo")) {
            return true;
         } else if (activeChar.isInParty()) {
            int i = 0;
            CreatureSay[] packets = new CreatureSay[9];
            packets[i++] = new CreatureSay(
               activeChar.getObjectId(),
               20,
               ServerStorage.getInstance().getString(activeChar.getLang(), "CheckPremium.PARTY"),
               ServerStorage.getInstance().getString(activeChar.getLang(), "CheckPremium.PREMIUM_BONUSES")
            );
            packets[i++] = new CreatureSay(
               activeChar.getObjectId(), 20, "" + (i - 1) + "", "Rate Xp: +" + cutOff((activeChar.getParty().getRateXp() - 1.0) * 100.0, 0) + "%"
            );
            packets[i++] = new CreatureSay(
               activeChar.getObjectId(), 20, "" + (i - 1) + "", "Rate Sp: +" + cutOff((activeChar.getParty().getRateSp() - 1.0) * 100.0, 0) + "%"
            );
            packets[i++] = new CreatureSay(
               activeChar.getObjectId(), 20, "" + (i - 1) + "", "Rate Adena: +" + cutOff((activeChar.getParty().getDropAdena() - 1.0) * 100.0, 0) + "%"
            );
            packets[i++] = new CreatureSay(
               activeChar.getObjectId(), 20, "" + (i - 1) + "", "Rate Drop: +" + cutOff((activeChar.getParty().getDropItems() - 1.0) * 100.0, 0) + "%"
            );
            packets[i++] = new CreatureSay(
               activeChar.getObjectId(), 20, "" + (i - 1) + "", "Rate Spoil: +" + cutOff((activeChar.getParty().getDropSpoil() - 1.0) * 100.0, 0) + "%"
            );
            packets[i++] = new CreatureSay(
               activeChar.getObjectId(), 20, "" + (i - 1) + "", "Rate Raids: +" + cutOff((activeChar.getParty().getDropRaids() - 1.0) * 100.0, 0) + "%"
            );
            packets[i++] = new CreatureSay(
               activeChar.getObjectId(), 20, "" + (i - 1) + "", "Rate Epics: +" + cutOff((activeChar.getParty().getDropEpics() - 1.0) * 100.0, 0) + "%"
            );
            packets[i++] = new CreatureSay(
               activeChar.getObjectId(),
               20,
               ServerStorage.getInstance().getString(activeChar.getLang(), "CheckPremium.PARTY"),
               ServerStorage.getInstance().getString(activeChar.getLang(), "CheckPremium.PREMIUM_BONUSES")
            );
            activeChar.sendPacket(packets);
            return true;
         } else {
            activeChar.sendMessage(new ServerMessage("CheckPremium.NO_PARTY", activeChar.getLang()).toString());
            return true;
         }
      } else if (activeChar.isInParty()) {
         List<Player> premiums = new ArrayList<>();

         for(Player player : activeChar.getParty().getMembers()) {
            if (player != null && player.hasPremiumBonus()) {
               premiums.add(player);
            }
         }

         if (!premiums.isEmpty()) {
            int i = 0;
            CreatureSay[] packets = new CreatureSay[premiums.size() + 2];
            packets[i++] = new CreatureSay(
               activeChar.getObjectId(),
               20,
               ServerStorage.getInstance().getString(activeChar.getLang(), "CheckPremium.PARTY"),
               ServerStorage.getInstance().getString(activeChar.getLang(), "CheckPremium.PREMIUM_LIST")
            );

            for(Player player : premiums) {
               if (player != null) {
                  String rate = player.getPremiumBonus().getRateXp() > 1.0
                     ? "(+" + cutOff((player.getPremiumBonus().getRateXp() - 1.0) * 100.0, 0) + "%)"
                     : "";
                  ServerMessage msg = player.getPremiumBonus().isPersonal()
                     ? new ServerMessage("CheckPremium.PERSONAL", activeChar.getLang())
                     : new ServerMessage("CheckPremium.ACCOUNT", activeChar.getLang());
                  msg.add(player.getName());
                  packets[i++] = new CreatureSay(activeChar.getObjectId(), 20, "" + (i - 1) + "", "" + msg.toString() + " " + rate);
               }
            }

            packets[i++] = new CreatureSay(
               activeChar.getObjectId(),
               20,
               ServerStorage.getInstance().getString(activeChar.getLang(), "CheckPremium.PARTY"),
               ServerStorage.getInstance().getString(activeChar.getLang(), "CheckPremium.PREMIUM_LIST")
            );
            activeChar.sendPacket(packets);
            return true;
         } else {
            activeChar.sendMessage(new ServerMessage("CheckPremium.EMPTY_LIST", activeChar.getLang()).toString());
            return true;
         }
      } else {
         activeChar.sendMessage(new ServerMessage("CheckPremium.NO_PARTY", activeChar.getLang()).toString());
         return true;
      }
   }

   private static double cutOff(double num, int pow) {
      return (double)((int)(num * Math.pow(10.0, (double)pow))) / Math.pow(10.0, (double)pow);
   }

   @Override
   public String[] getVoicedCommandList() {
      return _voicedCommands;
   }
}
