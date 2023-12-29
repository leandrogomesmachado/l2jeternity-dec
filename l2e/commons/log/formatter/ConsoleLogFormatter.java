package l2e.commons.log.formatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import l2e.commons.util.StringUtil;
import l2e.commons.util.Util;
import l2e.gameserver.Config;

public class ConsoleLogFormatter extends Formatter {
   private final SimpleDateFormat dateFmt = new SimpleDateFormat("HH:mm:ss");

   @Override
   public String format(LogRecord record) {
      StringBuilder output = new StringBuilder(500);
      StringUtil.append(output, "[", this.dateFmt.format(new Date(record.getMillis())), "] ", record.getMessage(), Config.EOL);
      if (record.getThrown() != null) {
         try {
            StringUtil.append(output, Util.getStackTrace(record.getThrown()), Config.EOL);
         } catch (Exception var4) {
         }
      }

      return output.toString();
   }
}
