package l2e.gameserver.handler.bypasshandlers.impl;

import java.text.DateFormat;
import l2e.gameserver.Config;
import l2e.gameserver.handler.bypasshandlers.IBypassHandler;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.instancemanager.games.Lottery;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.strings.server.ServerStorage;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class Loto implements IBypassHandler {
   private static final String[] COMMANDS = new String[]{"Loto"};

   @Override
   public boolean useBypass(String command, Player activeChar, Creature target) {
      if (target != null && target.isNpc()) {
         int val = 0;

         try {
            val = Integer.parseInt(command.substring(5));
         } catch (IndexOutOfBoundsException var6) {
         } catch (NumberFormatException var7) {
         }

         if (val == 0) {
            for(int i = 0; i < 5; ++i) {
               activeChar.setLoto(i, 0);
            }
         }

         showLotoWindow(activeChar, (Npc)target, val);
         return false;
      } else {
         return false;
      }
   }

   public static final void showLotoWindow(Player player, Npc npc, int val) {
      int npcId = npc.getTemplate().getId();
      NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
      if (val == 0) {
         String filename = npc.getHtmlPath(npcId, 1);
         html.setFile(player, player.getLang(), filename);
      } else if (val >= 1 && val <= 21) {
         if (!Lottery.getInstance().isStarted()) {
            player.sendPacket(SystemMessageId.NO_LOTTERY_TICKETS_CURRENT_SOLD);
            return;
         }

         if (!Lottery.getInstance().isSellableTickets()) {
            player.sendPacket(SystemMessageId.NO_LOTTERY_TICKETS_AVAILABLE);
            return;
         }

         String filename = npc.getHtmlPath(npcId, 5);
         html.setFile(player, player.getLang(), filename);
         int count = 0;
         int found = 0;

         for(int i = 0; i < 5; ++i) {
            if (player.getLoto(i) == val) {
               player.setLoto(i, 0);
               found = 1;
            } else if (player.getLoto(i) > 0) {
               ++count;
            }
         }

         if (count < 5 && found == 0 && val <= 20) {
            for(int i = 0; i < 5; ++i) {
               if (player.getLoto(i) == 0) {
                  player.setLoto(i, val);
                  break;
               }
            }
         }

         count = 0;

         for(int i = 0; i < 5; ++i) {
            if (player.getLoto(i) > 0) {
               ++count;
               String button = String.valueOf(player.getLoto(i));
               if (player.getLoto(i) < 10) {
                  button = "0" + button;
               }

               String search = "fore=\"L2UI.lottoNum" + button + "\" back=\"L2UI.lottoNum" + button + "a_check\"";
               String replace = "fore=\"L2UI.lottoNum" + button + "a_check\" back=\"L2UI.lottoNum" + button + "\"";
               html.replace(search, replace);
            }
         }

         if (count == 5) {
            String search = "0\">" + ServerStorage.getInstance().getString(player.getLang(), "Loto.RETURN") + "";
            String replace = "22\">" + ServerStorage.getInstance().getString(player.getLang(), "Loto.YOUR_LUCKY") + "";
            html.replace(search, replace);
         }
      } else if (val == 22) {
         if (!Lottery.getInstance().isStarted()) {
            player.sendPacket(SystemMessageId.NO_LOTTERY_TICKETS_CURRENT_SOLD);
            return;
         }

         if (!Lottery.getInstance().isSellableTickets()) {
            player.sendPacket(SystemMessageId.NO_LOTTERY_TICKETS_AVAILABLE);
            return;
         }

         long price = Config.ALT_LOTTERY_TICKET_PRICE;
         int lotonumber = Lottery.getInstance().getId();
         int enchant = 0;
         int type2 = 0;

         for(int i = 0; i < 5; ++i) {
            if (player.getLoto(i) == 0) {
               return;
            }

            if (player.getLoto(i) < 17) {
               enchant = (int)((double)enchant + Math.pow(2.0, (double)(player.getLoto(i) - 1)));
            } else {
               type2 = (int)((double)type2 + Math.pow(2.0, (double)(player.getLoto(i) - 17)));
            }
         }

         if (player.getAdena() < price) {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
            player.sendPacket(sm);
            return;
         }

         if (!player.reduceAdena("Loto", price, npc, true)) {
            return;
         }

         Lottery.getInstance().increasePrize(price);
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
         sm.addItemName(4442);
         player.sendPacket(sm);
         ItemInstance item = new ItemInstance(IdFactory.getInstance().getNextId(), 4442);
         item.setCount(1L);
         item.setCustomType1(lotonumber);
         item.setEnchantLevel(enchant);
         item.setCustomType2(type2);
         player.getInventory().addItem("Loto", item, player, npc);
         InventoryUpdate iu = new InventoryUpdate();
         iu.addItem(item);
         ItemInstance adenaupdate = player.getInventory().getItemByItemId(57);
         iu.addModifiedItem(adenaupdate);
         player.sendPacket(iu);
         String filename = npc.getHtmlPath(npcId, 6);
         html.setFile(player, player.getLang(), filename);
      } else if (val == 23) {
         String filename = npc.getHtmlPath(npcId, 3);
         html.setFile(player, player.getLang(), filename);
      } else if (val == 24) {
         String filename = npc.getHtmlPath(npcId, 4);
         html.setFile(player, player.getLang(), filename);
         int lotonumber = Lottery.getInstance().getId();
         String message = "";

         for(ItemInstance item : player.getInventory().getItems()) {
            if (item != null && item.getId() == 4442 && item.getCustomType1() < lotonumber) {
               message = message
                  + "<a action=\"bypass -h npc_%objectId%_Loto "
                  + item.getObjectId()
                  + "\">"
                  + item.getCustomType1()
                  + " "
                  + ServerStorage.getInstance().getString(player.getLang(), "Loto.EVENT_NUMBER")
                  + " ";
               int[] numbers = Lottery.getInstance().decodeNumbers(item.getEnchantLevel(), item.getCustomType2());

               for(int i = 0; i < 5; ++i) {
                  message = message + numbers[i] + " ";
               }

               long[] check = Lottery.getInstance().checkTicket(item);
               if (check[0] > 0L) {
                  switch((int)check[0]) {
                     case 1:
                        message = message + "- " + ServerStorage.getInstance().getString(player.getLang(), "Loto.1_PRIZE") + "";
                        break;
                     case 2:
                        message = message + "- " + ServerStorage.getInstance().getString(player.getLang(), "Loto.2_PRIZE") + "";
                        break;
                     case 3:
                        message = message + "- " + ServerStorage.getInstance().getString(player.getLang(), "Loto.3_PRIZE") + "";
                        break;
                     case 4:
                        message = message + "- " + ServerStorage.getInstance().getString(player.getLang(), "Loto.4_PRIZE") + "";
                  }

                  message = message + " " + check[1] + "a.";
               }

               message = message + "</a><br>";
            }
         }

         if (message.isEmpty()) {
            message = message + "" + ServerStorage.getInstance().getString(player.getLang(), "Loto.NO_WIN") + "<br>";
         }

         html.replace("%result%", message);
      } else if (val == 25) {
         String filename = npc.getHtmlPath(npcId, 2);
         html.setFile(player, player.getLang(), filename);
      } else if (val > 25) {
         int lotonumber = Lottery.getInstance().getId();
         ItemInstance item = player.getInventory().getItemByObjectId(val);
         if (item != null && item.getId() == 4442 && item.getCustomType1() < lotonumber) {
            long[] check = Lottery.getInstance().checkTicket(item);
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
            sm.addItemName(4442);
            player.sendPacket(sm);
            long adena = check[1];
            if (adena > 0L) {
               player.addAdena("Loto", adena, npc, true);
            }

            player.destroyItem("Loto", item, npc, false);
            return;
         }

         return;
      }

      html.replace("%objectId%", String.valueOf(npc.getObjectId()));
      html.replace("%race%", "" + Lottery.getInstance().getId());
      html.replace("%adena%", "" + Lottery.getInstance().getPrize());
      html.replace("%ticket_price%", "" + Config.ALT_LOTTERY_TICKET_PRICE);
      html.replace("%prize5%", "" + Config.ALT_LOTTERY_5_NUMBER_RATE * 100.0F);
      html.replace("%prize4%", "" + Config.ALT_LOTTERY_4_NUMBER_RATE * 100.0F);
      html.replace("%prize3%", "" + Config.ALT_LOTTERY_3_NUMBER_RATE * 100.0F);
      html.replace("%prize2%", "" + Config.ALT_LOTTERY_2_AND_1_NUMBER_PRIZE);
      html.replace("%enddate%", "" + DateFormat.getDateInstance().format(Lottery.getInstance().getEndDate()));
      player.sendPacket(html);
      player.sendActionFailed();
   }

   @Override
   public String[] getBypassList() {
      return COMMANDS;
   }
}
