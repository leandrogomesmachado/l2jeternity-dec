package l2e.commons.log.formatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import l2e.commons.util.StringUtil;
import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.skills.Skill;

public class DamageFormatter extends Formatter {
   private final SimpleDateFormat dateFmt = new SimpleDateFormat("yy.MM.dd H:mm:ss");

   @Override
   public String format(LogRecord record) {
      Object[] params = record.getParameters();
      StringBuilder output = StringUtil.startAppend(
         30 + record.getMessage().length() + (params == null ? 0 : params.length * 10),
         "[",
         this.dateFmt.format(new Date(record.getMillis())),
         "] '---': ",
         record.getMessage()
      );
      if (params != null) {
         for(Object p : params) {
            if (p != null) {
               if (p instanceof Creature) {
                  if (p instanceof Attackable && ((Attackable)p).isRaid()) {
                     StringUtil.append(output, "RaidBoss ");
                  }

                  StringUtil.append(output, ((Creature)p).getName(), "(", String.valueOf(((Creature)p).getObjectId()), ") ");
                  StringUtil.append(output, String.valueOf(((Creature)p).getLevel()), " lvl");
                  if (p instanceof Summon) {
                     Player owner = ((Summon)p).getOwner();
                     if (owner != null) {
                        StringUtil.append(output, " Owner:", owner.getName(), "(", String.valueOf(owner.getObjectId()), ")");
                     }
                  }
               } else if (p instanceof Skill) {
                  StringUtil.append(output, " with skill ", ((Skill)p).getNameEn(), "(", String.valueOf(((Skill)p).getId()), ")");
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
