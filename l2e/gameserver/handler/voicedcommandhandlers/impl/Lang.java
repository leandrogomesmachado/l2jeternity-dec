package l2e.gameserver.handler.voicedcommandhandlers.impl;

import l2e.gameserver.Config;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.strings.server.ServerStorage;

public class Lang implements IVoicedCommandHandler {
   private static final String[] _voicedCommands = new String[]{"lang"};

   @Override
   public boolean useVoicedCommand(String command, Player activeChar, String target) {
      if (command.equalsIgnoreCase("lang") && target != null) {
         if (!Config.MULTILANG_ALLOWED.contains(target)) {
            String answer = "" + ServerStorage.getInstance().getString(activeChar.getLang(), "Lang.WRONG_LANG") + "";

            for(String lang : Config.MULTILANG_ALLOWED) {
               answer = answer + " " + lang;
            }

            activeChar.sendMessage(answer);
            return false;
         }

         activeChar.setLang(target);
         if (target.equalsIgnoreCase("en")) {
            activeChar.sendMessage("" + ServerStorage.getInstance().getString(activeChar.getLang(), "Lang.EN_LANG") + "");
         } else if (target.equalsIgnoreCase("ru")) {
            activeChar.sendMessage("" + ServerStorage.getInstance().getString(activeChar.getLang(), "Lang.RU_LANG") + "");
         }

         activeChar.updateNpcNames();
      } else if (command.startsWith("lang")) {
         String[] params = command.split(" ");
         if (params.length == 2) {
            String lng = params[1];
            if (!Config.MULTILANG_ALLOWED.contains(lng)) {
               String answer = "" + ServerStorage.getInstance().getString(activeChar.getLang(), "Lang.WRONG_LANG") + "";

               for(String lang : Config.MULTILANG_ALLOWED) {
                  answer = answer + " " + lang;
               }

               activeChar.sendMessage(answer);
               return false;
            }

            activeChar.setLang(lng);
            if (lng.equalsIgnoreCase("en")) {
               activeChar.sendMessage("" + ServerStorage.getInstance().getString(activeChar.getLang(), "Lang.EN_LANG") + "");
            } else if (lng.equalsIgnoreCase("ru")) {
               activeChar.sendMessage("" + ServerStorage.getInstance().getString(activeChar.getLang(), "Lang.RU_LANG") + "");
            }

            activeChar.updateNpcNames();
         }
      }

      return true;
   }

   @Override
   public String[] getVoicedCommandList() {
      return _voicedCommands;
   }
}
