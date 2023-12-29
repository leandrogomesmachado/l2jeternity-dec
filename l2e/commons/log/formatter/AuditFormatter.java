package l2e.commons.log.formatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import l2e.commons.util.StringUtil;
import l2e.gameserver.Config;

public class AuditFormatter extends Formatter {
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
               StringUtil.append(output, ", ", p.toString());
            }
         }
      }

      output.append(Config.EOL);
      return output.toString();
   }
}
