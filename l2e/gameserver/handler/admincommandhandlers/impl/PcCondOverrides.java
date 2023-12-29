package l2e.gameserver.handler.admincommandhandlers.impl;

import java.util.StringTokenizer;
import l2e.commons.util.Util;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.model.strings.server.ServerStorage;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class PcCondOverrides implements IAdminCommandHandler {
   private static final String[] COMMANDS = new String[]{"admin_exceptions", "admin_set_exception"};

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      StringTokenizer st = new StringTokenizer(command);
      if (st.hasMoreTokens()) {
         String var4 = st.nextToken();
         switch(var4) {
            case "admin_exceptions":
               NpcHtmlMessage msg = new NpcHtmlMessage(5, 1);
               msg.setFile(activeChar, activeChar.getLang(), "data/html/admin/cond_override.htm");
               StringBuilder sb = new StringBuilder();

               for(PcCondOverride ex : PcCondOverride.values()) {
                  sb.append(
                     "<tr><td fixwidth=\"200\">"
                        + ServerStorage.getInstance().getString(activeChar.getLang(), ex.getDescription())
                        + ":</td><td><a action=\"bypass -h admin_set_exception "
                        + ex.ordinal()
                        + "\">"
                        + (
                           activeChar.canOverrideCond(ex)
                              ? ServerStorage.getInstance().getString(activeChar.getLang(), "PcCondOverride.DISABLE")
                              : ServerStorage.getInstance().getString(activeChar.getLang(), "PcCondOverride.ENABLE")
                        )
                        + "</a></td></tr>"
                  );
               }

               msg.replace("%cond_table%", sb.toString());
               activeChar.sendPacket(msg);
               break;
            case "admin_set_exception":
               if (st.hasMoreTokens()) {
                  String token = st.nextToken();
                  if (Util.isDigit(token)) {
                     PcCondOverride ex = PcCondOverride.getCondOverride(Integer.valueOf(token));
                     if (ex != null) {
                        if (activeChar.canOverrideCond(ex)) {
                           activeChar.removeOverridedCond(ex);
                           ServerMessage msg = new ServerMessage("PcCondOverride.DISABLE_MSG", activeChar.getLang());
                           msg.add(ServerStorage.getInstance().getString(activeChar.getLang(), ex.getDescription()));
                           activeChar.sendMessage(msg.toString());
                        } else {
                           activeChar.addOverrideCond(ex);
                           ServerMessage msg = new ServerMessage("PcCondOverride.ENABLE_MSG", activeChar.getLang());
                           msg.add(ServerStorage.getInstance().getString(activeChar.getLang(), ex.getDescription()));
                           activeChar.sendMessage(msg.toString());
                        }
                     }
                  } else {
                     switch(token) {
                        case "enable_all":
                           PcCondOverride[] var18 = PcCondOverride.values();
                           int var20 = var18.length;
                           int var22 = 0;

                           for(; var22 < var20; ++var22) {
                              PcCondOverride ex = var18[var22];
                              if (!activeChar.canOverrideCond(ex)) {
                                 activeChar.addOverrideCond(ex);
                              }
                           }

                           activeChar.sendMessage(new ServerMessage("PcCondOverride.ENABLE_ALL", activeChar.getLang()).toString());
                           break;
                        case "disable_all":
                           for(PcCondOverride ex : PcCondOverride.values()) {
                              if (activeChar.canOverrideCond(ex)) {
                                 activeChar.removeOverridedCond(ex);
                              }
                           }

                           activeChar.sendMessage(new ServerMessage("PcCondOverride.DISABLE_ALL", activeChar.getLang()).toString());
                     }
                  }

                  this.useAdminCommand(COMMANDS[0], activeChar);
               }
         }
      }

      return true;
   }

   @Override
   public String[] getAdminCommandList() {
      return COMMANDS;
   }
}
