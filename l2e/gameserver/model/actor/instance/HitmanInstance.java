package l2e.gameserver.model.actor.instance;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import l2e.gameserver.Config;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.model.strings.server.ServerStorage;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.scripts.events.Hitman;

public class HitmanInstance extends Npc {
   private static Integer maxPerPage = Config.HITMAN_MAX_PER_PAGE;
   private final DecimalFormat f = new DecimalFormat(",##0,000");

   public HitmanInstance(int objectID, NpcTemplate template) {
      super(objectID, template);
   }

   @Override
   public void onBypassFeedback(Player player, String command) {
      StringTokenizer st = new StringTokenizer(command, " ");
      String currentcommand = st.nextToken();

      try {
         if (currentcommand.startsWith("showList")) {
            int p = Integer.parseInt(st.nextToken());
            this.parseWindow(player, this.showListWindow(player, p));
         } else if (currentcommand.startsWith("showInfo")) {
            int playerId = Integer.parseInt(st.nextToken());
            int p = Integer.parseInt(st.nextToken());
            this.parseWindow(player, this.showInfoWindow(player, playerId, p));
         } else if (currentcommand.startsWith("showAddList")) {
            NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
            String filename = "data/html/default/51-1.htm";
            html.setFile(player, player.getLang(), "data/html/default/51-1.htm");
            this.parseWindow(player, html);
         } else if (currentcommand.startsWith("addList")) {
            String name = st.nextToken();
            long amount = Long.parseLong(st.nextToken());
            Integer itemId = Hitman.getCurrencyId(st.nextToken());
            if (amount <= 0L) {
               amount = 1L;
            }

            Hitman.putHitOn(player, name, amount, itemId);
         } else if (currentcommand.startsWith("removeList")) {
            String name = st.nextToken();
            Hitman.cancelAssasination(name, player);
            this.showChatWindow(player, 0);
         } else {
            super.onBypassFeedback(player, command);
         }
      } catch (Exception var9) {
         player.sendMessage(new ServerMessage("Hitman.MAKE_SURE", player.getLang()).toString());
      }
   }

   public void parseWindow(Player player, NpcHtmlMessage html) {
      html.replace("%objectId%", String.valueOf(this.getObjectId()));
      html.replace("%npc_name%", this.getName());
      html.replace("%player_name%", player.getName());
      player.sendPacket(html);
   }

   public NpcHtmlMessage showAddList(Player player, String list) {
      NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
      StringBuilder content = new StringBuilder();
      content.append("<html>");
      content.append("<body>");
      content.append("<center>");
      content.append("<img src=\"L2Font-e.mini_logo-e\" width=\"245\" height=\"80\">");
      content.append("<img src=\"L2UI_CH3.herotower_deco\" width=\"256\" height=\"32\">");
      content.append("<br>" + ServerStorage.getInstance().getString(player.getLang(), "Hitman.ORDER_TARGET") + "<br1>");
      content.append("<table width=\"256\">");
      content.append("<tr>");
      content.append("<td width=\"256\" align=\"center\">" + ServerStorage.getInstance().getString(player.getLang(), "Hitman.NAME") + "<br1>");
      content.append("<edit var=\"name\" width=\"150\" height=\"15\">");
      content.append("</td>");
      content.append("</tr>");
      content.append("<tr>");
      content.append("<td wi dth=\"256\" align=\"center\">" + ServerStorage.getInstance().getString(player.getLang(), "Hitman.CURRENCY") + "<br1>");
      content.append("<combobox width=\"180\" var=\"currency\" list=\"Adena;Coin_of_Luck;Golden_Apiga\">");
      content.append("</td>");
      content.append("</tr>");
      content.append("<tr>");
      content.append("<td width=\"256\" align=\"center\">" + ServerStorage.getInstance().getString(player.getLang(), "Hitman.AMOUNT") + "<br1>");
      content.append("<edit var=\"bounty\" width=\"150\" height=\"15\">");
      content.append("</td>");
      content.append("</tr>");
      content.append("</table>");
      content.append("<br>");
      content.append(
         "<button value="
            + ServerStorage.getInstance().getString(player.getLang(), "Hitman.ADD")
            + " action=\"bypass -h npc_%objectId%_addList $name $bounty $currency\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" width=\"95\" height=\"21\">"
      );
      content.append("<br>" + ServerStorage.getInstance().getString(player.getLang(), "Hitman.IF_DEL_TARGET") + "<br1>");
      content.append("<table width=\"240\">");
      content.append("<tr>");
      content.append("<td width=\"60\">" + ServerStorage.getInstance().getString(player.getLang(), "Hitman.NAME") + ":</td>");
      content.append("<td><edit var=\"remname\" width=\"110\" height=\"15\"></td>");
      content.append(
         "<td><button value="
            + ServerStorage.getInstance().getString(player.getLang(), "Hitman.DELETE")
            + " action=\"bypass -h npc_%objectId%_removeList $remname\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" width=\"60\" height=\"21\"></td>"
      );
      content.append("</tr>");
      content.append("</table>");
      content.append("<br>");
      content.append("<br>");
      content.append(
         "<button value="
            + ServerStorage.getInstance().getString(player.getLang(), "Hitman.BACK")
            + " action=\"bypass -h npc_%objectId%_Chat 0\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" width=\"80\" height=\"21\"><br1>"
      );
      content.append("<img src=\"L2UI_CH3.herotower_deco\" widt h=\"256\" height=\"32\">");
      content.append("<img src=\"l2ui.bbs_lineage2\" height=\"16\" width=\"80\">");
      content.append("</center>");
      content.append("</body>");
      content.append("</html>");
      html.setHtml(player, content.toString());
      return html;
   }

   private String generateButtonPage(int page, int select) {
      String text = "";
      if (page == 1) {
         return text;
      } else {
         text = text + "<table><tr>";

         for(int i = 1; i <= page; ++i) {
            String v = i == select ? i + "*" : String.valueOf(i);
            text = text
               + "<td><button value=\"P"
               + v
               + "\"action=\"bypass -h npc_%objectId%_showList "
               + i
               + "\" back=\"L2UI_CT1.Button_DF_Down\"fore=\"L2UI_CT1.Button_DF\" width=35 height=21></td>";
            text = text + (i % 8 == 0 ? "</tr><tr>" : "");
         }

         return text + "</tr></table>";
      }
   }

   public NpcHtmlMessage showListWindow(Player player, int p) {
      NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
      StringBuilder content = new StringBuilder("<html><body><center>");
      content.append("<img src=\"L2UI_CH3.herotower_deco\" width=\"256\" height=\"32\">");
      content.append("<br>");
      content.append("<table>");
      content.append(
         "<tr><td align=\"center\"><font color=AAAAAA>" + ServerStorage.getInstance().getString(player.getLang(), "Hitman.AGENCY") + "</font></td></tr>"
      );
      content.append("<tr><td align=\"center\"><img src=\"L2UI.SquareWhite\" width=\"261\" height=\"1\"></td></tr>");
      content.append("<tr><td align=\"center\">");
      List<Hitman.PlayerToAssasinate> list = new ArrayList<>();
      list.addAll(Hitman.getTargetsOnline().values());
      if (list.size() > 0) {
         int countPag = (int)Math.ceil((double)list.size() / (double)maxPerPage.intValue());
         int startRow = maxPerPage * (p - 1);
         int stopRow = startRow + maxPerPage;
         int countReg = 0;
         String pages = this.generateButtonPage(countPag, p);
         content.append(pages);
         content.append("<table bgcolor=\"000000\">");
         content.append("<tr><td width=\"60\" align=\"center\">" + ServerStorage.getInstance().getString(player.getLang(), "Hitman.TARGET") + "</td>");
         content.append(
            "<td width=\"125\" align=\"center\"><font color=\"F2FEBF\">"
               + ServerStorage.getInstance().getString(player.getLang(), "Hitman.REWARD")
               + "</font></td>"
         );
         content.append(
            "<td width=\"115\" align=\"center\"><font color=\"00CC00\">"
               + ServerStorage.getInstance().getString(player.getLang(), "Hitman.CURRENCY")
               + "</font></td></tr>"
         );

         for(Hitman.PlayerToAssasinate pta : list) {
            if (pta == null || countReg >= stopRow) {
               break;
            }

            if (countReg >= startRow && countReg < stopRow) {
               content.append("<tr><td align=\"center\">" + pta.getName() + "</td>");
               content.append("<td align=\"center\">" + (pta.getBounty() > 999L ? this.f.format(pta.getBounty()) : pta.getBounty()) + "</td>");
               content.append("<td align=\"center\">" + pta.getItemName(player) + "</td></tr>");
            }

            ++countReg;
         }

         content.append("<tr><td height=\"3\"> </td><td height=\"3\"> </td><td height=\"3\"> </td></tr>");
         content.append("</table><br1>");
         content.append(pages);
      } else {
         content.append("" + ServerStorage.getInstance().getString(player.getLang(), "Hitman.NO_TARGET") + "");
      }

      content.append("</td></tr>");
      content.append("<tr><td align=\"center\"><img src=\"L2UI.SquareWhite\" width=\"261\" height=\"1\"></td></tr>");
      content.append("</table>");
      content.append(
         "<button value="
            + ServerStorage.getInstance().getString(player.getLang(), "Hitman.BACK")
            + " action=\"bypass -h npc_%objectId%_Chat 0\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" width=\"55\" height=\"21\">"
      );
      content.append("<br><font color=\"cc9900\"><img src=\"L2UI_CH3.herotower_deco\" width=\"256\" height=\"32\"></font><br1>");
      content.append("</center></body></html>");
      html.setHtml(player, content.toString());
      return html;
   }

   public NpcHtmlMessage showInfoWindow(Player player, int objectId, int p) {
      NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
      Hitman.PlayerToAssasinate pta = Hitman.getTargets().get(objectId);
      Player target = World.getInstance().getPlayer(pta.getName());
      StringBuilder content = new StringBuilder("<html><body><center>");
      content.append("<img src=\"L2UI_CH3.herotower_deco\" width=\"256\" height=\"32\">");
      content.append("<table>");
      content.append(
         "<tr><td align=\"center\"><font color=\"AAAAAA\">"
            + ServerStorage.getInstance().getString(player.getLang(), "Hitman.TARGET")
            + ": "
            + pta.getName()
            + "</font></td></tr>"
      );
      content.append("<tr><td align=\"center\"><img src=\"L2UI.SquareWhite\" width=\"261\" height=\"1\"></td></tr>");
      content.append("<tr><td align=\"center\">");
      if (target != null) {
         content.append("<table bgcolor=\"000000\"><tr><td>");
         content.append("" + ServerStorage.getInstance().getString(player.getLang(), "Hitman.INFO") + ".<br>");
         content.append("<br><br>");
         content.append(
            "" + ServerStorage.getInstance().getString(player.getLang(), "Hitman.TARGET") + ": <font color=\"D74B18\">" + pta.getName() + "</font><br1>"
         );
         content.append(
            ""
               + ServerStorage.getInstance().getString(player.getLang(), "Hitman.REWARD")
               + " <font color=\"D74B18\">"
               + (pta.getBounty() > 999L ? this.f.format(pta.getBounty()) : pta.getBounty())
               + " "
               + pta.getItemName(player)
               + "</font><br1>"
         );
         content.append("</td></tr></table>");
      } else {
         content.append("Player went offline.");
      }

      content.append("</td></tr>");
      content.append("<tr><td align=\"center\"><img src=\"L2UI.SquareWhite\" width=\"261\" height=\"1\"></td></tr>");
      content.append("</table>");
      content.append(
         "<button value="
            + ServerStorage.getInstance().getString(player.getLang(), "Hitman.BACK")
            + " action=\"bypass -h npc_%objectId%_showList "
            + p
            + "\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" width=\"100\" height=\"21\">"
      );
      content.append("<br><font color=\"cc9900\"><img src=\"L2UI_CH3.herotower_deco\" width=\"256\" height=\"32\"></font><br1>");
      content.append("</center></body></html>");
      html.setHtml(player, content.toString());
      return html;
   }
}
