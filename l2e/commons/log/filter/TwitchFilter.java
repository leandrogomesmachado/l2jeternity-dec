package l2e.commons.log.filter;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class TwitchFilter implements Filter {
   @Override
   public boolean isLoggable(LogRecord record) {
      return "twitch".equals(record.getLoggerName());
   }
}
