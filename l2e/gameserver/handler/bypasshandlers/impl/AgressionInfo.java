package l2e.gameserver.handler.bypasshandlers.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;
import l2e.commons.util.Util;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.handler.bypasshandlers.IBypassHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;

public class AgressionInfo implements IBypassHandler {
   private static final String[] COMMANDS = new String[]{"aggro"};

   @Override
   public boolean useBypass(String command, Player activeChar, Creature target) {
      try {
         StringTokenizer st = new StringTokenizer(command, " ");
         String actualCommand = st.nextToken();
         String var6 = actualCommand.toLowerCase();
         switch(var6) {
            case "aggro":
               try {
                  String pg = null;

                  try {
                     pg = st.nextToken();
                  } catch (Exception var25) {
                  }

                  if (pg != null) {
                     int page = Integer.parseInt(pg);
                     GameObject targetmob = activeChar.getTarget();
                     if (!(targetmob instanceof Attackable)) {
                        activeChar.sendMessage("You cant use this option with this target.");
                        return false;
                     }

                     Attackable npc = (Attackable)targetmob;
                     String html = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/aggro_info.htm");
                     String template = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/aggro_template.htm");
                     String block = "";
                     String list = "";
                     if (npc.getAggroList() == null || npc.getAggroList().isEmpty()) {
                        html = html.replace("{list}", "<tr><td align=center>Empty Aggro Info List!</td></tr>");
                        html = html.replace("{navigation}", "<td>&nbsp;</td>");
                        html = html.replace(
                           "{npc_name}", activeChar.getLang() != null && !activeChar.getLang().equalsIgnoreCase("en") ? npc.getNameRu() : npc.getName()
                        );
                        Util.setHtml(html, activeChar);
                        return false;
                     }

                     List<Attackable.AggroInfo> aggroList = new ArrayList<>();

                     for(Attackable.AggroInfo info : npc.getAggroList().values()) {
                        if (info != null) {
                           aggroList.add(info);
                        }
                     }

                     Comparator<Attackable.AggroInfo> statsComparator = new AgressionInfo.SortAggroInfo();
                     Collections.sort(aggroList, statsComparator);
                     int perpage = 8;
                     int counter = 0;
                     int totalSize = aggroList.size();
                     boolean isThereNextPage = totalSize > 8;

                     for(int i = (page - 1) * 8; i < totalSize; ++i) {
                        Attackable.AggroInfo data = aggroList.get(i);
                        if (data != null) {
                           block = template.replace(
                              "{name}",
                              !data.getAttacker().isSummon() && !data.getAttacker().isPet()
                                 ? data.getAttacker().getName()
                                 : ((Summon)data.getAttacker()).getSummonName(activeChar, (Summon)data.getAttacker())
                           );
                           block = block.replace("{damage}", String.valueOf(data.getDamage()));
                           block = block.replace("{hate}", String.valueOf(data.getHate()));
                           list = list + block;
                        }

                        if (++counter >= 8) {
                           break;
                        }
                     }

                     double pages = (double)totalSize / 8.0;
                     int count = (int)Math.ceil(pages);
                     html = html.replace("{list}", list);
                     html = html.replace("{navigation}", Util.getNavigationBlock(count, page, totalSize, 8, isThereNextPage, "aggro %s"));
                     html = html.replace(
                        "{npc_name}", activeChar.getLang() != null && !activeChar.getLang().equalsIgnoreCase("en") ? npc.getNameRu() : npc.getName()
                     );
                     Util.setHtml(html, activeChar);
                  }
               } catch (Exception var26) {
                  activeChar.sendMessage("Something went wrong with the aggro preview.");
               }
         }
      } catch (Exception var27) {
         activeChar.sendMessage("You cant use this option with this target.");
      }

      return false;
   }

   @Override
   public String[] getBypassList() {
      return COMMANDS;
   }

   private static class SortAggroInfo implements Comparator<Attackable.AggroInfo>, Serializable {
      private static final long serialVersionUID = 7691414259610932752L;

      private SortAggroInfo() {
      }

      public int compare(Attackable.AggroInfo o1, Attackable.AggroInfo o2) {
         return Integer.compare(o2.getHate(), o1.getHate());
      }
   }
}
