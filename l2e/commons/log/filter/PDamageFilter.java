package l2e.commons.log.filter;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class PDamageFilter implements Filter {
   @Override
   public boolean isLoggable(LogRecord record) {
      return record.getLoggerName().equalsIgnoreCase("pdam");
   }
}
