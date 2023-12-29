package l2e.commons.log.formatter;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import l2e.gameserver.Config;

public class GMAuditFormatter extends Formatter {
   @Override
   public String format(LogRecord record) {
      return record.getMessage() + Config.EOL;
   }
}
