package l2e.commons.util;

import l2e.gameserver.network.NpcStringId;
import org.apache.commons.lang3.StringUtils;

public class HtmlUtil {
   public static final String PREV_BUTTON = "<button value=\"&$1037;\" action=\"bypass %prev_bypass%\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">";
   public static final String NEXT_BUTTON = "<button value=\"&$1038;\" action=\"bypass %next_bypass%\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">";

   public static String getCpGauge(int width, long current, long max, boolean displayAsPercentage) {
      return getGauge(
         width, current, max, displayAsPercentage, "L2UI_CT1.Gauges.Gauge_DF_Large_CP_bg_Center", "L2UI_CT1.Gauges.Gauge_DF_Large_CP_Center", 17L, -13L
      );
   }

   public static String getHpGauge(int width, long current, long max, boolean displayAsPercentage) {
      return getGauge(
         width, current, max, displayAsPercentage, "L2UI_CT1.Gauges.Gauge_DF_Large_HP_bg_Center", "L2UI_CT1.Gauges.Gauge_DF_Large_HP_Center", 21L, -13L
      );
   }

   public static String getHpWarnGauge(int width, long current, long max, boolean displayAsPercentage) {
      return getGauge(
         width,
         current,
         max,
         displayAsPercentage,
         "L2UI_CT1.Gauges.Gauge_DF_Large_HPWarn_bg_Center",
         "L2UI_CT1.Gauges.Gauge_DF_Large_HPWarn_Center",
         17L,
         -13L
      );
   }

   public static String getHpFillGauge(int width, long current, long max, boolean displayAsPercentage) {
      return getGauge(
         width,
         current,
         max,
         displayAsPercentage,
         "L2UI_CT1.Gauges.Gauge_DF_Large_HPFill_bg_Center",
         "L2UI_CT1.Gauges.Gauge_DF_Large_HPFill_Center",
         17L,
         -13L
      );
   }

   public static String getMpGauge(int width, long current, long max, boolean displayAsPercentage) {
      return getGauge(
         width, current, max, displayAsPercentage, "L2UI_CT1.Gauges.Gauge_DF_Large_MP_bg_Center", "L2UI_CT1.Gauges.Gauge_DF_Large_MP_Center", 17L, -13L
      );
   }

   public static String getExpGauge(int width, long current, long max, boolean displayAsPercentage) {
      return getGauge(
         width, current, max, displayAsPercentage, "L2UI_CT1.Gauges.Gauge_DF_Large_EXP_bg_Center", "L2UI_CT1.Gauges.Gauge_DF_Large_EXP_Center", 17L, -13L
      );
   }

   public static String getFoodGauge(int width, long current, long max, boolean displayAsPercentage) {
      return getGauge(
         width, current, max, displayAsPercentage, "L2UI_CT1.Gauges.Gauge_DF_Large_Food_Bg_Center", "L2UI_CT1.Gauges.Gauge_DF_Large_Food_Center", 17L, -13L
      );
   }

   public static String getWeightGauge(int width, long current, long max, boolean displayAsPercentage, long level) {
      return getGauge(
         width,
         current,
         max,
         displayAsPercentage,
         "L2UI_CT1.Gauges.Gauge_DF_Large_Weight_bg_Center" + level,
         "L2UI_CT1.Gauges.Gauge_DF_Large_Weight_Center" + level,
         17L,
         -13L
      );
   }

   public static String getEternityGauge(int width, long current, long max, boolean displayAsPercentage) {
      return getEternityGauge(width, current, max, displayAsPercentage, "l2ui_ct1_cn.titlebasebarmini", "branchsys2.br_navitgaugemid", 12L, -13L);
   }

   private static String getEternityGauge(
      int width, long current, long max, boolean displayAsPercentage, String backgroundImage, String image, long imageHeight, long top
   ) {
      current = Math.min(current, max);
      StringBuilder sb = new StringBuilder();
      sb.append("<table width=");
      sb.append(width);
      sb.append(" cellpadding=0 cellspacing=0 background=" + backgroundImage + " height=" + imageHeight + ">");
      sb.append("<tr>");
      sb.append("<td align=left valign=top>");
      sb.append("<img src=\"");
      sb.append(image);
      sb.append("\" width=");
      sb.append((long)((double)current / (double)max * (double)width));
      sb.append(" height=");
      sb.append(imageHeight);
      sb.append(">");
      sb.append("</td>");
      sb.append("</tr>");
      sb.append("<tr>");
      sb.append("<td align=center>");
      sb.append("<table cellpadding=0 cellspacing=");
      sb.append(top);
      sb.append(">");
      sb.append("<tr>");
      sb.append("<td>");
      if (displayAsPercentage) {
         sb.append("<table cellpadding=0 cellspacing=2>");
         sb.append("<tr><td>");
         sb.append(String.format("%.2f%%", (double)current / (double)max * 100.0));
         sb.append("</td></tr>");
         sb.append("</table>");
      } else {
         int tdWidth = (width - 10) / 2;
         sb.append("<table cellpadding=0 cellspacing=0>");
         sb.append("<tr>");
         sb.append("<td width=");
         sb.append(tdWidth);
         sb.append(" align=right>");
         sb.append(current);
         sb.append("</td>");
         sb.append("<td width=10 align=center>/</td>");
         sb.append("<td width=");
         sb.append(tdWidth);
         sb.append(">");
         sb.append(max);
         sb.append("</td>");
         sb.append("</tr>");
         sb.append("</table>");
      }

      sb.append("</td>");
      sb.append("</tr>");
      sb.append("</table>");
      sb.append("</td>");
      sb.append("</tr>");
      sb.append("</table>");
      return sb.toString();
   }

   private static String getGauge(
      int width, long current, long max, boolean displayAsPercentage, String backgroundImage, String image, long imageHeight, long top
   ) {
      current = Math.min(current, max);
      StringBuilder sb = new StringBuilder();
      sb.append("<table width=");
      sb.append(width);
      sb.append(" cellpadding=0 cellspacing=0>");
      sb.append("<tr>");
      sb.append("<td background=\"");
      sb.append(backgroundImage);
      sb.append("\">");
      sb.append("<img src=\"");
      sb.append(image);
      sb.append("\" width=");
      sb.append((long)((double)current / (double)max * (double)width));
      sb.append(" height=");
      sb.append(imageHeight);
      sb.append(">");
      sb.append("</td>");
      sb.append("</tr>");
      sb.append("<tr>");
      sb.append("<td align=center>");
      sb.append("<table cellpadding=0 cellspacing=");
      sb.append(top);
      sb.append(">");
      sb.append("<tr>");
      sb.append("<td>");
      if (displayAsPercentage) {
         sb.append("<table cellpadding=0 cellspacing=2>");
         sb.append("<tr><td>");
         sb.append(String.format("%.2f%%", (double)current / (double)max * 100.0));
         sb.append("</td></tr>");
         sb.append("</table>");
      } else {
         int tdWidth = (width - 10) / 2;
         sb.append("<table cellpadding=0 cellspacing=0>");
         sb.append("<tr>");
         sb.append("<td width=");
         sb.append(tdWidth);
         sb.append(" align=right>");
         sb.append(current);
         sb.append("</td>");
         sb.append("<td width=10 align=center>/</td>");
         sb.append("<td width=");
         sb.append(tdWidth);
         sb.append(">");
         sb.append(max);
         sb.append("</td>");
         sb.append("</tr>");
         sb.append("</table>");
      }

      sb.append("</td>");
      sb.append("</tr>");
      sb.append("</table>");
      sb.append("</td>");
      sb.append("</tr>");
      sb.append("</table>");
      return sb.toString();
   }

   public static String htmlResidenceName(int id) {
      return "&%" + id + ";";
   }

   public static String htmlNpcName(int npcId) {
      return "&@" + npcId + ";";
   }

   public static String htmlSysString(int id) {
      return "&$" + id + ";";
   }

   public static String htmlItemName(int itemId) {
      return "&#" + itemId + ";";
   }

   public static String htmlClassName(int classId) {
      return "<ClassId>" + classId + "</ClassId>";
   }

   public static String htmlNpcString(NpcStringId id, Object... params) {
      return htmlNpcString(id.getId(), params);
   }

   public static String htmlNpcString(int id, Object... params) {
      String replace = "<fstring";
      if (params.length > 0) {
         for(int i = 0; i < params.length; ++i) {
            replace = replace + " p" + (i + 1) + "=\"" + params[i] + "\"";
         }
      }

      return replace + ">" + id + "</fstring>";
   }

   public static String htmlButton(String value, String action, int width) {
      return htmlButton(value, action, width, 22);
   }

   public static String htmlButton(String value, String action, int width, int height) {
      return String.format(
         "<button value=\"%s\" action=\"%s\" back=\"L2UI_CT1.Button_DF_Small_Down\" width=%d height=%d fore=\"L2UI_CT1.Button_DF_Small\">",
         value,
         action,
         width,
         height
      );
   }

   public static String switchButtons(String html) {
      html = StringUtils.replaceAll(
         html,
         "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(npc(\\?|_[0-9]+_)Quest)\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)",
         "<Button ALIGN=LEFT ICON=\"QUEST\" $1>$6</Button>"
      );
      html = StringUtils.replaceAll(
         html,
         "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(npc(\\?|_[0-9]+_)goto)[^\"]+\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)",
         "<Button ALIGN=LEFT ICON=\"TELEPORT\" $1>$6</Button>"
      );
      html = StringUtils.replaceAll(
         html,
         "(?i:<a\\s+(action=\"bypass\\s+-h\\s+(npc(\\?|_[0-9]+_)Chat)\\s+0\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)",
         "<Button ALIGN=LEFT ICON=\"RETURN\" $1>$6</Button>"
      );
      html = StringUtils.replaceAll(
         html,
         "(?i:<a\\s+(action=\"bypass\\s+-h\\s+([^\"]+)\"(\\s+msg=\"([^\"]+)\")?)\\s*>(Back|Return|Назад|Вернуться|В\\s+начало)\\.?</a\\s*>(<br1?>)?)",
         "<Button ALIGN=LEFT ICON=\"RETURN\" $1>$5</Button>"
      );
      html = StringUtils.replaceAll(
         html,
         "(?i:<a\\s+(action=\"bypass\\s+-h\\s+([^\"]+)\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)",
         "<Button ALIGN=LEFT ICON=\"NORMAL\" $1>$5</Button>"
      );
      return StringUtils.replaceAll(
         html,
         "(?i:<a\\s+(action=\"bypass\\s+([^\"]+)\"(\\s+msg=\"([^\"]+)\")?)\\s*>((?:(?!</a).)+)</a\\s*>(<br1?>)?)",
         "<Button ALIGN=LEFT ICON=\"NORMAL\" $1>$5</Button>"
      );
   }
}
