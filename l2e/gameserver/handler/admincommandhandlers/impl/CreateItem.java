package l2e.gameserver.handler.admincommandhandlers.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class CreateItem implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{
      "admin_itemcreate", "admin_create_item", "admin_create_coin", "admin_give_item_target", "admin_give_item_to_all", "admin_give_item_all_with_check"
   };

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      NpcHtmlMessage adminhtm = new NpcHtmlMessage(5);
      if (command.equals("admin_itemcreate")) {
         adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/itemcreation.htm");
         activeChar.sendPacket(adminhtm);
      } else if (command.startsWith("admin_create_item")) {
         try {
            String val = command.substring(17);
            StringTokenizer st = new StringTokenizer(val);
            if (st.countTokens() == 2) {
               String id = st.nextToken();
               int idval = Integer.parseInt(id);
               String num = st.nextToken();
               long numval = Long.parseLong(num);
               this.createItem(activeChar, activeChar, idval, numval);
            } else if (st.countTokens() == 1) {
               String id = st.nextToken();
               int idval = Integer.parseInt(id);
               this.createItem(activeChar, activeChar, idval, 1L);
            }
         } catch (StringIndexOutOfBoundsException var20) {
            activeChar.sendMessage("Usage: //create_item <itemId> [amount]");
         } catch (NumberFormatException var21) {
            activeChar.sendMessage("Specify a valid number.");
         }

         adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/itemcreation.htm");
         activeChar.sendPacket(adminhtm);
      } else if (command.startsWith("admin_create_coin")) {
         try {
            String val = command.substring(17);
            StringTokenizer st = new StringTokenizer(val);
            if (st.countTokens() == 2) {
               String name = st.nextToken();
               int idval = this.getCoinId(name);
               if (idval > 0) {
                  String num = st.nextToken();
                  long numval = Long.parseLong(num);
                  this.createItem(activeChar, activeChar, idval, numval);
               }
            } else if (st.countTokens() == 1) {
               String name = st.nextToken();
               int idval = this.getCoinId(name);
               this.createItem(activeChar, activeChar, idval, 1L);
            }
         } catch (StringIndexOutOfBoundsException var18) {
            activeChar.sendMessage("Usage: //create_coin <name> [amount]");
         } catch (NumberFormatException var19) {
            activeChar.sendMessage("Specify a valid number.");
         }

         adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/itemcreation.htm");
         activeChar.sendPacket(adminhtm);
      } else if (command.startsWith("admin_give_item_target")) {
         try {
            if (!(activeChar.getTarget() instanceof Player)) {
               activeChar.sendMessage("Invalid target.");
               return false;
            }

            Player target = (Player)activeChar.getTarget();
            String val = command.substring(22);
            StringTokenizer st = new StringTokenizer(val);
            if (st.countTokens() == 2) {
               String id = st.nextToken();
               int idval = Integer.parseInt(id);
               String num = st.nextToken();
               long numval = Long.parseLong(num);
               this.createItem(activeChar, target, idval, numval);
            } else if (st.countTokens() == 1) {
               String id = st.nextToken();
               int idval = Integer.parseInt(id);
               this.createItem(activeChar, target, idval, 1L);
            }
         } catch (StringIndexOutOfBoundsException var16) {
            activeChar.sendMessage("Usage: //give_item_target <itemId> [amount]");
         } catch (NumberFormatException var17) {
            activeChar.sendMessage("Specify a valid number.");
         }

         adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/itemcreation.htm");
         activeChar.sendPacket(adminhtm);
      } else if (command.startsWith("admin_give_item_to_all")) {
         String val = command.substring(22);
         StringTokenizer st = new StringTokenizer(val);
         int idval = 0;
         long numval = 0L;
         if (st.countTokens() == 2) {
            String id = st.nextToken();
            idval = Integer.parseInt(id);
            String num = st.nextToken();
            numval = Long.parseLong(num);
         } else if (st.countTokens() == 1) {
            String id = st.nextToken();
            idval = Integer.parseInt(id);
            numval = 1L;
         }

         int counter = 0;
         Item template = ItemsParser.getInstance().getTemplate(idval);
         if (template == null) {
            activeChar.sendMessage("This item doesn't exist.");
            return false;
         }

         if (numval > 10L && !template.isStackable()) {
            activeChar.sendMessage("This item does not stack - Creation aborted.");
            return false;
         }

         for(Player onlinePlayer : World.getInstance().getAllPlayers()) {
            if (activeChar != onlinePlayer && onlinePlayer.isOnline() && onlinePlayer.getClient() != null && !onlinePlayer.getClient().isDetached()) {
               onlinePlayer.getInventory().addItem("Admin", idval, numval, onlinePlayer, activeChar);
               onlinePlayer.sendMessage("Admin spawned " + numval + " " + onlinePlayer.getItemName(template) + " in your inventory.");
               ++counter;
            }
         }

         activeChar.sendMessage(counter + " players rewarded with " + activeChar.getItemName(template));
      } else if (command.startsWith("admin_give_item_all_with_check")) {
         String val = command.substring(30);
         StringTokenizer st = new StringTokenizer(val);
         int idval = 0;
         long numval = 0L;
         if (st.countTokens() == 2) {
            String id = st.nextToken();
            idval = Integer.parseInt(id);
            String num = st.nextToken();
            numval = Long.parseLong(num);
         } else if (st.countTokens() == 1) {
            String id = st.nextToken();
            idval = Integer.parseInt(id);
            numval = 1L;
         }

         int counter = 0;
         Item template = ItemsParser.getInstance().getTemplate(idval);
         if (template == null) {
            activeChar.sendMessage("This item doesn't exist.");
            return false;
         }

         if (numval > 10L && !template.isStackable()) {
            activeChar.sendMessage("This item does not stack - Creation aborted.");
            return false;
         }

         List<String> hwids = new ArrayList<>();
         boolean isIpCheck = Config.PROTECTION.equalsIgnoreCase("NONE");

         for(Player onlinePlayer : World.getInstance().getAllPlayers()) {
            if (activeChar != onlinePlayer && onlinePlayer.isOnline() && onlinePlayer.getClient() != null && !onlinePlayer.getClient().isDetached()) {
               String plHwid = isIpCheck ? onlinePlayer.getIPAddress() : onlinePlayer.getHWID();
               if (!hwids.contains(plHwid)) {
                  onlinePlayer.addItem("Admin", idval, numval, onlinePlayer, true);
                  hwids.add(plHwid);
                  ++counter;
               }
            }
         }

         activeChar.sendMessage(counter + " players rewarded with " + activeChar.getItemName(template));
         hwids.clear();
      }

      return true;
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }

   private void createItem(Player activeChar, Player target, int id, long num) {
      Item template = ItemsParser.getInstance().getTemplate(id);
      if (template == null) {
         activeChar.sendMessage("This item doesn't exist.");
      } else if (num > 10L && !template.isStackable()) {
         activeChar.sendMessage("This item does not stack - Creation aborted.");
      } else {
         target.addItem("Admin", id, num, activeChar, true);
         if (activeChar != target) {
            target.sendMessage("Admin spawned " + num + " " + target.getItemName(template) + " in your inventory.");
         }

         activeChar.sendMessage("You have spawned " + num + " " + activeChar.getItemName(template) + "(" + id + ") in " + target.getName() + " inventory.");
      }
   }

   private int getCoinId(String name) {
      int id;
      if (name.equalsIgnoreCase("adena")) {
         id = 57;
      } else if (name.equalsIgnoreCase("ancientadena")) {
         id = 5575;
      } else if (name.equalsIgnoreCase("festivaladena")) {
         id = 6673;
      } else if (name.equalsIgnoreCase("blueeva")) {
         id = 4355;
      } else if (name.equalsIgnoreCase("goldeinhasad")) {
         id = 4356;
      } else if (name.equalsIgnoreCase("silvershilen")) {
         id = 4357;
      } else if (name.equalsIgnoreCase("bloodypaagrio")) {
         id = 4358;
      } else if (name.equalsIgnoreCase("fantasyislecoin")) {
         id = 13067;
      } else {
         id = 0;
      }

      return id;
   }
}
