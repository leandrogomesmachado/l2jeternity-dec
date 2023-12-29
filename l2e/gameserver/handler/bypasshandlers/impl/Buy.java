package l2e.gameserver.handler.bypasshandlers.impl;

import java.util.StringTokenizer;
import java.util.logging.Level;
import l2e.gameserver.handler.bypasshandlers.IBypassHandler;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.MerchantInstance;

public class Buy implements IBypassHandler {
   private static final String[] COMMANDS = new String[]{"Buy"};

   @Override
   public boolean useBypass(String command, Player activeChar, Creature target) {
      if (!(target instanceof MerchantInstance)) {
         return false;
      } else {
         try {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            if (st.countTokens() < 1) {
               return false;
            } else {
               ((MerchantInstance)target).showBuyWindow(activeChar, Integer.parseInt(st.nextToken()));
               return true;
            }
         } catch (Exception var5) {
            _log.log(Level.WARNING, "Exception in " + this.getClass().getSimpleName(), (Throwable)var5);
            return false;
         }
      }
   }

   @Override
   public String[] getBypassList() {
      return COMMANDS;
   }
}
