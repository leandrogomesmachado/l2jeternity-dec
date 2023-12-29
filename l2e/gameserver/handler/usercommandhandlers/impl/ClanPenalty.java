package l2e.gameserver.handler.usercommandhandlers.impl;

import java.text.SimpleDateFormat;
import l2e.commons.util.StringUtil;
import l2e.gameserver.handler.usercommandhandlers.IUserCommandHandler;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class ClanPenalty implements IUserCommandHandler {
   private static final int[] COMMAND_IDS = new int[]{100};

   @Override
   public boolean useUserCommand(int id, Player activeChar) {
      if (id != COMMAND_IDS[0]) {
         return false;
      } else {
         boolean penalty = false;
         SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
         StringBuilder htmlContent = StringUtil.startAppend(
            500,
            "<html><body><center><table width=270 border=0 bgcolor=111111><tr><td width=170>Penalty</td><td width=100 align=center>Expiration Date</td></tr></table><table width=270 border=0><tr>"
         );
         if (activeChar.getClanJoinExpiryTime() > System.currentTimeMillis()) {
            StringUtil.append(
               htmlContent,
               "<td width=170>Unable to join a clan.</td><td width=100 align=center>",
               format.format(Long.valueOf(activeChar.getClanJoinExpiryTime())),
               "</td>"
            );
            penalty = true;
         }

         if (activeChar.getClanCreateExpiryTime() > System.currentTimeMillis()) {
            StringUtil.append(
               htmlContent,
               "<td width=170>Unable to create a clan.</td><td width=100 align=center>",
               format.format(Long.valueOf(activeChar.getClanCreateExpiryTime())),
               "</td>"
            );
            penalty = true;
         }

         if (activeChar.getClan() != null && activeChar.getClan().getCharPenaltyExpiryTime() > System.currentTimeMillis()) {
            StringUtil.append(
               htmlContent,
               "<td width=170>Unable to invite a clan member.</td><td width=100 align=center>",
               format.format(Long.valueOf(activeChar.getClan().getCharPenaltyExpiryTime())),
               "</td>"
            );
            penalty = true;
         }

         if (!penalty) {
            htmlContent.append("<td width=170>No penalty is imposed.</td><td width=100 align=center></td>");
         }

         htmlContent.append("</tr></table><img src=\"L2UI.SquareWhite\" width=270 height=1></center></body></html>");
         NpcHtmlMessage penaltyHtml = new NpcHtmlMessage(0);
         penaltyHtml.setHtml(activeChar, htmlContent.toString());
         activeChar.sendPacket(penaltyHtml);
         return true;
      }
   }

   @Override
   public int[] getUserCommandList() {
      return COMMAND_IDS;
   }
}
