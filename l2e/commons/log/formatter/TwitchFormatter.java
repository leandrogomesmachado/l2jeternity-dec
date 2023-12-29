package l2e.commons.log.formatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import l2e.commons.util.StringUtil;
import l2e.gameserver.Config;

public class TwitchFormatter extends Formatter {
   private final SimpleDateFormat dateFmt = new SimpleDateFormat("dd MMM H:mm:ss");

   @Override
   public String format(LogRecord record) {
      Object[] params = record.getParameters();
      StringBuilder output = StringUtil.startAppend(
         30 + record.getMessage().length() + (params != null ? 10 * params.length : 0), "[", this.dateFmt.format(new Date(record.getMillis())), "] "
      );
      if (params != null) {
         for(Object p : params) {
            StringUtil.append(output, String.valueOf(p), " ");
         }
      }

      StringUtil.append(output, record.getMessage(), Config.EOL);
      return output.toString();
   }
}
