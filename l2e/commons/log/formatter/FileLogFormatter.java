package l2e.commons.log.formatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import l2e.commons.util.StringUtil;
import l2e.gameserver.Config;

public class FileLogFormatter extends Formatter {
   private static final String empty_string = " ";
   private final SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

   @Override
   public String format(LogRecord record) {
      return StringUtil.concat(this.dateFmt.format(new Date(record.getMillis())), " ", record.getMessage(), Config.EOL);
   }
}
