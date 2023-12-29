package l2e.gameserver.handler.communityhandlers.impl.model;

import java.text.NumberFormat;
import java.util.Locale;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public abstract class NpcUtils {
   private static final NumberFormat pf = NumberFormat.getPercentInstance(Locale.ENGLISH);
   private static final NumberFormat df = NumberFormat.getInstance(Locale.ENGLISH);

   public static void showNpcSkillList(Player player, NpcTemplate npc) {
      NpcHtmlMessage html = new NpcHtmlMessage(5, 1);
      html.setFile(player, player.getLang(), "data/html/npc_skills.htm");
      html.replace("%npc_name%", String.valueOf(player.getNpcName(npc)));
      StringBuilder sb = new StringBuilder(100);

      for(Skill skill : npc.getSkills().values()) {
         sb.append("<table width=260 height=35 cellspacing=0 background=\"L2UI_CT1.Windows.Windows_DF_TooltipBG\">");
         sb.append("<tr><td fixwidth=34 valign=top><img src=\"" + skill.getIcon() + "\" width=\"32\" height=\"32\"></td>");
         sb.append("<td fixwidth=180>");
         sb.append(player.getSkillName(skill));
         sb.append("</td>");
         sb.append("<td fixwidth=65>");
         sb.append(skill.getLevel());
         sb.append(" Lvl</td></tr></table>");
      }

      html.replace("%skills%", sb.toString());
      player.sendPacket(html);
   }

   static {
      pf.setMaximumFractionDigits(4);
      df.setMinimumFractionDigits(2);
   }
}
