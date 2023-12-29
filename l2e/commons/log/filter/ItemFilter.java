package l2e.commons.log.filter;

import java.util.logging.Filter;
import java.util.logging.LogRecord;
import l2e.gameserver.model.items.instance.ItemInstance;

public class ItemFilter implements Filter {
   private final String _excludeProcess = "Consume";
   private final String _excludeItemType = "Arrow, Shot, Herb";

   @Override
   public boolean isLoggable(LogRecord record) {
      if (!"item".equals(record.getLoggerName())) {
         return false;
      } else {
         if ("Consume" != null) {
            String[] messageList = record.getMessage().split(":");
            if (messageList.length < 2 || !"Consume".contains(messageList[1])) {
               return true;
            }
         }

         if ("Arrow, Shot, Herb" != null) {
            ItemInstance item = (ItemInstance)record.getParameters()[0];
            if (!"Arrow, Shot, Herb".contains(item.getItemType().toString())) {
               return true;
            }
         }

         return "Consume" == null && "Arrow, Shot, Herb" == null;
      }
   }
}
