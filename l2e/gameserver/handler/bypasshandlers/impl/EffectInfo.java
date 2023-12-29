package l2e.gameserver.handler.bypasshandlers.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import l2e.commons.util.Util;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.handler.bypasshandlers.IBypassHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.effects.Effect;

public class EffectInfo implements IBypassHandler {
   private static final String[] COMMANDS = new String[]{"effects"};

   @Override
   public boolean useBypass(String command, Player activeChar, Creature target) {
      try {
         StringTokenizer st = new StringTokenizer(command, " ");
         String actualCommand = st.nextToken();
         String var6 = actualCommand.toLowerCase();
         switch(var6) {
            case "effects":
               try {
                  String pg = null;

                  try {
                     pg = st.nextToken();
                  } catch (Exception var24) {
                  }

                  if (pg != null) {
                     int page = Integer.parseInt(pg);
                     GameObject targetmob = activeChar.getTarget();
                     if (!(targetmob instanceof Attackable)) {
                        activeChar.sendMessage("You cant use this option with this target.");
                        return false;
                     }

                     Attackable npc = (Attackable)targetmob;
                     String html = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/effects_info.htm");
                     String template = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/effects_template.htm");
                     String block = "";
                     String list = "";
                     List<Effect> effList = new ArrayList<>();

                     for(Effect ef : npc.getAllEffects()) {
                        if (ef != null && ef.isIconDisplay()) {
                           effList.add(ef);
                        }
                     }

                     if (effList.isEmpty() || effList.size() == 0) {
                        html = html.replace("{list}", "<tr><td align=center>Empty Effects List!</td></tr>");
                        html = html.replace("{navigation}", "<td>&nbsp;</td>");
                        html = html.replace(
                           "{npc_name}", activeChar.getLang() != null && !activeChar.getLang().equalsIgnoreCase("en") ? npc.getNameRu() : npc.getName()
                        );
                        Util.setHtml(html, activeChar);
                        return false;
                     }

                     int perpage = 6;
                     int counter = 0;
                     int totalSize = effList.size();
                     boolean isThereNextPage = totalSize > 6;

                     for(int i = (page - 1) * 6; i < totalSize; ++i) {
                        Effect data = effList.get(i);
                        if (data != null) {
                           block = template.replace("{name}", activeChar.getSkillName(data.getSkill()));
                           block = block.replace("{icon}", data.getSkill().getIcon());
                           block = block.replace("{time}", getTimeLeft((long)data.getTimeLeft()));
                           list = list + block;
                        }

                        if (++counter >= 6) {
                           break;
                        }
                     }

                     double pages = (double)totalSize / 6.0;
                     int count = (int)Math.ceil(pages);
                     html = html.replace("{list}", list);
                     html = html.replace("{navigation}", Util.getNavigationBlock(count, page, totalSize, 6, isThereNextPage, "effects %s"));
                     html = html.replace(
                        "{npc_name}", activeChar.getLang() != null && !activeChar.getLang().equalsIgnoreCase("en") ? npc.getNameRu() : npc.getName()
                     );
                     Util.setHtml(html, activeChar);
                  }
               } catch (Exception var25) {
                  activeChar.sendMessage("Something went wrong with the effects preview.");
               }
         }
      } catch (Exception var26) {
         activeChar.sendMessage("You cant use this option with this target.");
      }

      return false;
   }

   private static String getTimeLeft(long time) {
      int hours = (int)(time / 60L / 60L);
      int mins = (int)((time - (long)(hours * 60 * 60)) / 60L);
      int secs = (int)(time - (long)(hours * 60 * 60 + mins * 60));
      String Strhours = hours < 10 ? "0" + hours : "" + hours;
      String Strmins = mins < 10 ? "0" + mins : "" + mins;
      String Strsecs = secs < 10 ? "0" + secs : "" + secs;
      if (hours > 0) {
         return "<font color=\"b02e31\">" + Strhours + ":" + Strmins + ":" + Strsecs + "</font>";
      } else {
         return hours <= 0 && mins > 0 ? "<font color=\"b02e31\">" + Strmins + ":" + Strsecs + "</font>" : "<font color=\"b02e31\">" + Strsecs + "</font>";
      }
   }

   @Override
   public String[] getBypassList() {
      return COMMANDS;
   }
}
