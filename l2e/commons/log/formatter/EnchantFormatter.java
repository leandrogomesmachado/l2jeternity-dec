package l2e.commons.log.formatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import l2e.commons.util.StringUtil;
import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.skills.Skill;

public class EnchantFormatter extends Formatter {
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
               if (p instanceof Player) {
                  Player player = (Player)p;
                  StringUtil.append(
                     output, "Character:", player.getName(), " [" + String.valueOf(player.getObjectId()) + "] Account:", player.getAccountName()
                  );
                  if (player.getClient() != null && !player.getClient().isDetached()) {
                     StringUtil.append(output, " IP:", player.getClient().getConnection().getSocket().getInetAddress().getHostAddress());
                  }
               } else if (p instanceof ItemInstance) {
                  ItemInstance item = (ItemInstance)p;
                  if (item.getEnchantLevel() > 0) {
                     StringUtil.append(output, "+", String.valueOf(item.getEnchantLevel()), " ");
                  }

                  StringUtil.append(output, item.getItem().getNameEn(), "(", String.valueOf(item.getCount()), ")");
                  StringUtil.append(output, " [", String.valueOf(item.getObjectId()), "]");
               } else if (p instanceof Skill) {
                  Skill skill = (Skill)p;
                  if (skill.getLevel() > 100) {
                     StringUtil.append(output, "+", String.valueOf(skill.getLevel() % 100), " ");
                  }

                  StringUtil.append(output, skill.getNameEn(), "(", String.valueOf(skill.getId()), " ", String.valueOf(skill.getLevel()), ")");
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
