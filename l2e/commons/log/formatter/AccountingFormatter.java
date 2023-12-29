package l2e.commons.log.formatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import l2e.commons.util.StringUtil;
import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.GameClient;

public class AccountingFormatter extends Formatter {
   private final SimpleDateFormat dateFmt = new SimpleDateFormat("dd MMM H:mm:ss");

   @Override
   public String format(LogRecord record) {
      Object[] params = record.getParameters();
      StringBuilder output = StringUtil.startAppend(
         30 + record.getMessage().length() + (params == null ? 0 : params.length * 10),
         "[",
         this.dateFmt.format(new Date(record.getMillis())),
         "] ",
         record.getMessage()
      );
      if (params != null) {
         for(Object p : params) {
            if (p != null) {
               StringUtil.append(output, ", ");
               if (p instanceof GameClient) {
                  GameClient client = (GameClient)p;
                  String address = null;

                  try {
                     if (!client.isDetached()) {
                        address = client.getConnection().getSocket().getInetAddress().getHostAddress();
                     }
                  } catch (Exception var11) {
                  }

                  switch(client.getState()) {
                     case ENTERING:
                     case IN_GAME:
                        if (client.getActiveChar() != null) {
                           StringUtil.append(output, client.getActiveChar().getName());
                           StringUtil.append(output, "(", String.valueOf(client.getActiveChar().getObjectId()), ") ");
                        }
                     case AUTHED:
                        if (client.getLogin() != null) {
                           StringUtil.append(output, client.getLogin(), " ");
                        }
                     case CONNECTED:
                        if (address != null) {
                           StringUtil.append(output, address);
                        }
                        break;
                     default:
                        throw new IllegalStateException("Missing state on switch");
                  }
               } else if (p instanceof Player) {
                  Player player = (Player)p;
                  StringUtil.append(output, player.getName());
                  StringUtil.append(output, "(", String.valueOf(player.getObjectId()), ")");
               } else {
                  StringUtil.append(output, p.toString());
               }
            }
         }
      }

      output.append(Config.EOL);
      return output.toString();
   }
}
