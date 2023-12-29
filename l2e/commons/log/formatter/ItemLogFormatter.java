package l2e.commons.log.formatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import l2e.commons.util.StringUtil;
import l2e.gameserver.Config;
import l2e.gameserver.model.items.instance.ItemInstance;

public class ItemLogFormatter extends Formatter {
   private final SimpleDateFormat dateFmt = new SimpleDateFormat("dd MMM H:mm:ss");

   @Override
   public String format(LogRecord record) {
      Object[] params = record.getParameters();
      StringBuilder output = StringUtil.startAppend(
         30 + record.getMessage().length() + params.length * 50, "[", this.dateFmt.format(new Date(record.getMillis())), "] ", record.getMessage()
      );

      for(Object p : record.getParameters()) {
         if (p != null) {
            output.append(", ");
            if (p instanceof ItemInstance) {
               ItemInstance item = (ItemInstance)p;
               StringUtil.append(output, "item ", String.valueOf(item.getObjectId()), ":");
               if (item.getEnchantLevel() > 0) {
                  StringUtil.append(output, "+", String.valueOf(item.getEnchantLevel()), " ");
               }

               StringUtil.append(output, item.getItem().getNameEn(), "(", String.valueOf(item.getCount()), ")");
            } else {
               output.append(p.toString());
            }
         }
      }

      output.append(Config.EOL);
      return output.toString();
   }
}
