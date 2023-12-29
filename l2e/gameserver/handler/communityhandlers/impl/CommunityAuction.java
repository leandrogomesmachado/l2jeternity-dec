package l2e.gameserver.handler.communityhandlers.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.handler.communityhandlers.ICommunityBoardHandler;
import l2e.gameserver.listener.talk.OnAnswerListener;
import l2e.gameserver.model.Elementals;
import l2e.gameserver.model.TradeItem;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.auction.AccessoryItemType;
import l2e.gameserver.model.entity.auction.ArmorItemType;
import l2e.gameserver.model.entity.auction.Auction;
import l2e.gameserver.model.entity.auction.AuctionItemTypes;
import l2e.gameserver.model.entity.auction.AuctionsManager;
import l2e.gameserver.model.entity.auction.EtcAuctionItemType;
import l2e.gameserver.model.entity.auction.PetItemType;
import l2e.gameserver.model.entity.auction.SuppliesItemType;
import l2e.gameserver.model.entity.auction.WeaponItemType;
import l2e.gameserver.model.items.ItemAuction;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.itemcontainer.PcInventory;
import l2e.gameserver.model.items.type.WeaponType;
import l2e.gameserver.model.skills.funcs.FuncTemplate;
import l2e.gameserver.model.stats.Stats;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.model.strings.server.ServerStorage;
import org.apache.commons.lang3.StringUtils;

public class CommunityAuction extends AbstractCommunity implements ICommunityBoardHandler {
   private static final AuctionItemTypes[][] ALL_AUCTION_ITEM_TYPES = new AuctionItemTypes[][]{
      AccessoryItemType.values(),
      ArmorItemType.values(),
      EtcAuctionItemType.values(),
      PetItemType.values(),
      SuppliesItemType.values(),
      WeaponItemType.values()
   };

   public CommunityAuction() {
      if (Config.DEBUG) {
         _log.info(this.getClass().getSimpleName() + ": Loading all functions.");
      }
   }

   @Override
   public String[] getBypassCommands() {
      return new String[]{"_maillist_0_1_0_", "_bbsAuction", "_bbsNewAuction"};
   }

   @Override
   public void onBypassCommand(String command, Player player) {
      String html = "";
      if (command.equals("_maillist_0_1_0_")) {
         if (Config.ENABLE_MULTI_AUCTION_SYSTEM) {
            html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/auction/multi_auction_list.htm");
         } else {
            html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/auction/auction_list.htm");
            html = this.fillAuctionListPage(player, html, 1, 57, new int[]{-1, -1}, "All", null, 1, 0, 0, 0);
         }
      } else if (command.startsWith("_bbsAuction")) {
         StringTokenizer st = new StringTokenizer(command, "_");
         st.nextToken();

         try {
            int page = Integer.parseInt(st.nextToken().trim());
            int priceItemId = Integer.parseInt(st.nextToken().trim());
            int[] itemTypes = new int[2];
            int i = 0;

            for(String type : st.nextToken().trim().split(" ")) {
               itemTypes[i] = Integer.parseInt(type);
               ++i;
            }

            String grade = st.nextToken().trim();
            String search = st.nextToken().trim();
            int itemSort = Integer.parseInt(st.nextToken().trim());
            int gradeSort = Integer.parseInt(st.nextToken().trim());
            int quantitySort = Integer.parseInt(st.nextToken().trim());
            int priceSort = Integer.parseInt(st.nextToken().trim());
            if (st.hasMoreTokens()) {
               int action = Integer.parseInt(st.nextToken().trim());
               int auctionId = Integer.parseInt(st.nextToken().trim());
               if (action == 1) {
                  if (!st.hasMoreTokens()) {
                     player.sendMessage(new ServerMessage("CommunityAuction.FILL_ALL", player.getLang()).toString());
                  } else {
                     String quantity = st.nextToken().trim();
                     Auction auction = AuctionsManager.getInstance().getAuction(auctionId);
                     if (auction != null && auction.getItem() != null) {
                        long realPrice;
                        try {
                           realPrice = auction.getPricePerItem() * Long.parseLong(quantity);
                        } catch (NumberFormatException var23) {
                           player.sendMessage(new ServerMessage("CommunityAuction.INVALID", player.getLang()).toString());
                           return;
                        }

                        ItemInstance item = auction.getItem();
                        ServerMessage msg = new ServerMessage("CommunityAuction.WANT_TO_BUY", player.getLang());
                        msg.add(quantity);
                        msg.add(player.getItemName(item.getItem()));
                        msg.add(Util.getNumberWithCommas(realPrice));
                        msg.add(Util.getItemName(player, priceItemId));
                        player.sendConfirmDlg(
                           new CommunityAuction.ButtonClick(player, priceItemId, item, CommunityAuction.Buttons.Buy_Item, new String[]{quantity}),
                           60000,
                           msg.toString()
                        );
                     } else {
                        player.sendMessage(new ServerMessage("CommunityAuction.ALREADY_SOLD", player.getLang()).toString());
                     }
                  }
               }

               html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/auction/buy_item.htm");
               html = this.fillPurchasePage(player, html, page, priceItemId, itemTypes, grade, search, itemSort, gradeSort, quantitySort, priceSort, auctionId);
            } else {
               html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/auction/auction_list.htm");
               html = this.fillAuctionListPage(player, html, page, priceItemId, itemTypes, grade, search, itemSort, gradeSort, quantitySort, priceSort);
            }
         } catch (NumberFormatException var24) {
         }
      } else if (command.startsWith("_bbsNewAuction")) {
         StringTokenizer st = new StringTokenizer(command, "_");
         st.nextToken();
         if (player.isInStoreMode()) {
            player.sendMessage(new ServerMessage("CommunityAuction.CANT_OPEN", player.getLang()).toString());
            return;
         }

         String priceItemId = st.hasMoreTokens() ? st.nextToken().trim() : "57";
         String currentItem = st.hasMoreTokens() ? st.nextToken().trim() : "c0";
         int currentObjectId = Integer.parseInt(currentItem.substring(1));
         currentItem = currentItem.substring(0, 1);
         int line = Integer.parseInt(st.hasMoreTokens() ? st.nextToken().trim() : "0");
         String buttonClicked = st.hasMoreTokens() ? st.nextToken().trim() : null;
         if (buttonClicked != null) {
            if (!buttonClicked.equals("0")) {
               if (buttonClicked.equals("1")) {
                  ItemInstance item = ItemAuction.getInstance().getItemByObjectId(currentObjectId);
                  if (item == null) {
                     player.sendMessage(new ServerMessage("CommunityAuction.ALREADY_SOLD", player.getLang()).toString());

                     for(Auction a : AuctionsManager.getInstance().getMyAuctions(player.getObjectId(), Integer.parseInt(priceItemId))) {
                        if (a.getItem() == null) {
                           _log.warning(
                              "Auction bugged! Item:null itemId:"
                                 + currentObjectId
                                 + " auctionId:"
                                 + a.getAuctionId()
                                 + " Count:"
                                 + a.getCountToSell()
                                 + " Price:"
                                 + a.getPricePerItem()
                                 + " Seller:"
                                 + a.getSellerName()
                                 + "["
                                 + a.getSellerObjectId()
                                 + "] store:"
                                 + a.isPrivateStore()
                           );
                        } else {
                           _log.warning(
                              "Auction bugged! Item:"
                                 + a.getItem().getName()
                                 + " itemId:"
                                 + currentObjectId
                                 + " playerInv:"
                                 + player.getInventory().getItemByObjectId(player.getObjectId())
                                 + " auctionId:"
                                 + a.getAuctionId()
                                 + " Count:"
                                 + a.getCountToSell()
                                 + " Price:"
                                 + a.getPricePerItem()
                                 + " Seller:"
                                 + a.getSellerName()
                                 + "["
                                 + a.getSellerObjectId()
                                 + "] store:"
                                 + a.isPrivateStore()
                           );
                        }

                        AuctionsManager.getInstance().removeStore(player, a.getAuctionId());
                     }
                  } else if (!player.hasDialogAskActive()) {
                     ServerMessage msg = new ServerMessage("CommunityAuction.WANT_TO_CANCEL", player.getLang());
                     msg.add(player.getItemName(item.getItem()));
                     player.sendConfirmDlg(
                        new CommunityAuction.ButtonClick(player, Integer.parseInt(priceItemId), item, CommunityAuction.Buttons.Cancel_Auction, new String[0]),
                        60000,
                        msg.toString()
                     );
                  }
               }
            } else {
               ItemInstance item = player.getInventory().getItemByObjectId(currentObjectId);
               boolean error = false;
               String[] vars = new String[2];

               for(int i = 0; i < 2; ++i) {
                  if (st.hasMoreTokens()) {
                     vars[i] = st.nextToken().trim();
                     if (vars[i].isEmpty()) {
                        error = true;
                     }
                  } else {
                     error = true;
                  }
               }

               if (error) {
                  player.sendMessage(new ServerMessage("CommunityAuction.FILL_FIELDS", player.getLang()).toString());
               } else if (item == null) {
                  player.sendMessage(new ServerMessage("CommunityAuction.DONT_EXIST", player.getLang()).toString());
               } else {
                  ServerMessage msg = new ServerMessage("CommunityAuction.WANT_TO_SELL", player.getLang());
                  msg.add(player.getItemName(item.getItem()));
                  player.sendConfirmDlg(
                     new CommunityAuction.ButtonClick(
                        player, Integer.parseInt(priceItemId), item, CommunityAuction.Buttons.New_Auction, new String[]{vars[0], vars[1]}
                     ),
                     60000,
                     msg.toString()
                  );
               }
            }
         }

         html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/auction/new_auction.htm");
         html = this.fillNewAuctionPage(player, html, Integer.parseInt(priceItemId), currentItem.equals("n"), currentObjectId, line);
      }

      separateAndSend(html, player);
   }

   private String fillAuctionListPage(
      Player player,
      String html,
      int page,
      int priceItemId,
      int[] itemTypes,
      String itemGrade,
      String search,
      int itemSort,
      int gradeSort,
      int quantitySort,
      int priceSort
   ) {
      int heightToBeUsed = 220;

      for(int i = 1; i <= 6; ++i) {
         if (itemTypes[0] != i) {
            html = html.replace(
               "%plusMinusBtn" + i + "%",
               "<button value=\"\" action=\"bypass _bbsAuction_ 1 _ %priceItemId% _ "
                  + i
                  + " -1 _ %grade% _ %search% _ %itemSort% _ %gradeSort% _ %quantitySort% _ %priceSort%\" width=15 height=15 back=\"L2UI_CH3.QuestWndPlusBtn\" fore=\"L2UI_CH3.QuestWndPlusBtn\">"
            );
            html = html.replace("%itemListHeight" + i + "%", "0");
            html = html.replace("%itemList" + i + "%", "");
         } else {
            AuctionItemTypes[] types = this.getGroupsInType(itemTypes[0]);
            html = html.replace(
               "%plusMinusBtn" + i + "%",
               "<button value=\"\" action=\"bypass _bbsAuction_ 1 _ %priceItemId% _ -1 -1 _ %grade% _ %search% _ %itemSort% _ %gradeSort% _ %quantitySort% _ %priceSort%\" width=15 height=15 back=\"L2UI_CH3.QuestWndMinusBtn\" fore=\"L2UI_CH3.QuestWndMinusBtn\">"
            );
            html = html.replace("%itemListHeight" + i + "%", String.valueOf(types.length * 5));
            heightToBeUsed -= types.length * 15;
            StringBuilder builder = new StringBuilder();
            builder.append("<table>");
            int count = 0;

            for(AuctionItemTypes itemType : types) {
               builder.append("<tr><td><table width=150 bgcolor=").append(count % 2 == 1 ? "22211d" : "1b1a15").append(">");
               builder.append("<tr><td width=150 height=17><font color=93886c>");
               builder.append("<button value=\"")
                  .append(ServerStorage.getInstance().getString(player.getLang(), "CommunityAuction." + itemType.toString() + ""))
                  .append("\" action=\"bypass _bbsAuction_ 1 _ %priceItemId% _ ")
                  .append(itemTypes[0])
                  .append(" ")
                  .append(count)
                  .append(
                     " _ %grade% _ %search% _ %itemSort% _ %gradeSort% _ %quantitySort% _ %priceSort%\" width=150 height=17 back=\"L2UI_CT1.emptyBtn\" fore=\"L2UI_CT1.emptyBtn\">"
                  );
               builder.append("</font></td></tr></table></td></tr>");
               ++count;
            }

            builder.append("</table>");
            html = html.replace("%itemList" + i + "%", builder.toString());
         }
      }

      html = html.replace("%lastItemHeight%", String.valueOf(heightToBeUsed - 40));
      StringBuilder builder = new StringBuilder();
      Collection<Auction> allAuctions = AuctionsManager.getInstance().getAllAuctionsPerItemId(priceItemId);
      List<Auction> auctions = this.getRightAuctions(allAuctions, itemTypes, itemGrade, search);
      auctions = this.sortAuctions(player, auctions, itemSort, gradeSort, quantitySort, priceSort);
      int maxPage = (int)Math.ceil((double)auctions.size() / 10.0);

      for(int i = 10 * (page - 1); i < Math.min(auctions.size(), 10 * page); ++i) {
         Auction auction;
         try {
            auction = auctions.get(i);
         } catch (RuntimeException var21) {
            break;
         }

         ItemInstance item = auction.getItem();
         builder.append("<table border=0 cellspacing=1 cellpadding=0 width=558 height=30 bgcolor=").append(i % 2 == 1 ? "1a1914" : "23221d").append(">");
         builder.append("<tr><td fixwidth=280 height=25><table border=0 width=280 height=30><tr>");
         builder.append(
               "<td width=32 background="
                  + item.getItem().getIcon()
                  + "><button value=\"\" action=\"bypass _bbsAuction_ %page% _ %priceItemId% _ %type% _ %grade% _ %search% _ %itemSort% _ %gradeSort% _ %quantitySort% _ %priceSort% _ 0 _ "
            )
            .append(auction.getAuctionId())
            .append("\" width=32 height=32 back=\"L2UI_CT1.ItemWindow_DF_Frame_Down\" fore=\"L2UI_CT1.ItemWindow_DF_Frame\"></td>");
         builder.append(this.getItemName(player, item, 248, 25, auction.isPrivateStore()));
         builder.append("</tr></table></td><td width=50 height=30><center>");
         if (item.getItem().getCrystalType() != 0) {
            builder.append("<img src=")
               .append(this.getGradeIcon(item.getItem().getItemsGrade(item.getItem().getCrystalType())))
               .append(" width=15 height=15>");
         } else {
            builder.append("None");
         }

         builder.append("</center></td><td width=75 height=30>");
         builder.append("<center>").append(auction.getCountToSell()).append("</center>");
         builder.append("</td><td width=150 height=30 valign=top align=right>");
         builder.append(Util.getNumberWithCommas(auction.getPricePerItem()) + "<br1>");
         builder.append(
            "<font color=A18C70 name=CREDITTEXTSMALL>("
               + ServerStorage.getInstance().getString(player.getLang(), "CommunityAuction.TOTAL")
               + ": "
               + Util.getNumberWithCommas(auction.getCountToSell() * auction.getPricePerItem())
               + ")</font>"
         );
         builder.append("</td></tr></table>");
      }

      html = html.replace("%auctionItems%", builder.toString());
      html = html.replace("%type%", itemTypes[0] + " " + itemTypes[1]);
      html = html.replace("%grade%", itemGrade);
      html = html.replace("%search%", search == null ? "" : search);
      html = html.replace("%totalItems%", String.valueOf(auctions.size()));
      html = html.replace("%itemSort%", String.valueOf(itemSort));
      html = html.replace("%gradeSort%", String.valueOf(gradeSort));
      html = html.replace("%quantitySort%", String.valueOf(quantitySort));
      html = html.replace("%priceSort%", String.valueOf(priceSort));
      html = html.replace("%changeItemSort%", "" + (itemSort <= 0 ? 1 : -1));
      html = html.replace("%changeGradeSort%", "" + (gradeSort <= 0 ? 1 : -1));
      html = html.replace("%changeQuantitySort%", "" + (quantitySort <= 0 ? 1 : -1));
      html = html.replace("%changePriceSort%", "" + (priceSort <= 0 ? 1 : -1));
      html = html.replace("%page%", "" + page);
      html = html.replace("%priceItemId%", String.valueOf(priceItemId));
      html = html.replace("%prevPage%", String.valueOf(Math.max(1, page - 1)));
      html = html.replace("%nextPage%", String.valueOf(Math.min(maxPage, page + 1)));
      html = html.replace("%lastPage%", String.valueOf(maxPage));
      html = html.replace("%priceItemName%", Util.getItemName(player, priceItemId));
      return html.replace(
         "%priceItemCount%",
         Util.getNumberWithCommas(
            player.getInventory().getItemByItemId(priceItemId) == null ? 0L : player.getInventory().getItemByItemId(priceItemId).getCount()
         )
      );
   }

   private String fillPurchasePage(
      Player player,
      String html,
      int page,
      int priceItemId,
      int[] itemTypes,
      String itemGrade,
      String search,
      int itemSort,
      int gradeSort,
      int quantitySort,
      int priceSort,
      int auctionId
   ) {
      Auction auction = AuctionsManager.getInstance().getAuction(auctionId);
      if (auction != null && auction.getItem() != null) {
         ItemInstance choosenItem = auction.getItem();
         StringBuilder builder = new StringBuilder();
         if (choosenItem.getEnchantLevel() > 0) {
            builder.append("<center><font color=b3a683>+").append(choosenItem.getEnchantLevel()).append(" </font>");
         }

         builder.append("<center>" + player.getItemName(choosenItem.getItem()));
         builder.append("<br><center><img src=")
            .append(this.getGradeIcon(choosenItem.getItem().getItemsGrade(choosenItem.getItem().getCrystalType())))
            .append(" width=15 height=15>");
         builder.append(
            "<br><br><br><font color=827d78><br><br>"
               + ServerStorage.getInstance().getString(player.getLang(), "CommunityAuction.SELLER")
               + ":</font> <font color=94775b>"
               + auction.getSellerName()
               + "</font>"
         );
         if (choosenItem.isEquipable()) {
            int pAtk = this.getFunc(choosenItem, Stats.POWER_ATTACK);
            if (pAtk > 0) {
               builder.append(
                     "<br><br><font color=827d78>"
                        + ServerStorage.getInstance().getString(player.getLang(), "CommunityAuction.P_ATK")
                        + ":</font> <font color=94775b>"
                  )
                  .append(pAtk)
                  .append(" </font>");
            }

            int mAtk = this.getFunc(choosenItem, Stats.MAGIC_ATTACK);
            if (mAtk > 0) {
               builder.append(
                     "<br><font color=827d78>"
                        + ServerStorage.getInstance().getString(player.getLang(), "CommunityAuction.M_ATK")
                        + ":</font> <font color=94775b>"
                  )
                  .append(mAtk)
                  .append(" </font>");
            }

            int pDef = this.getFunc(choosenItem, Stats.POWER_DEFENCE);
            if (pDef > 0) {
               builder.append(
                     "<br><font color=827d78>"
                        + ServerStorage.getInstance().getString(player.getLang(), "CommunityAuction.P_DEF")
                        + ":</font> <font color=94775b>"
                  )
                  .append(pDef)
                  .append(" </font>");
            }

            int mDef = this.getFunc(choosenItem, Stats.MAGIC_DEFENCE);
            if (mDef > 0) {
               builder.append(
                     "<br><font color=827d78>"
                        + ServerStorage.getInstance().getString(player.getLang(), "CommunityAuction.M_DEF")
                        + ":</font> <font color=94775b>"
                  )
                  .append(mDef)
                  .append(" </font>");
            }

            if (choosenItem.isWeapon() && choosenItem.getElementals() != null) {
               builder.append("<br><br><br><font color=827d78>")
                  .append(this.getElementName(choosenItem.getAttackElementType()))
                  .append(" " + ServerStorage.getInstance().getString(player.getLang(), "CommunityAuction.ATK") + " ")
                  .append(choosenItem.getAttackElementPower());
               builder.append("</font><br><img src=L2UI_CT1.Gauge_DF_Attribute_")
                  .append(this.getElementName(choosenItem.getAttackElementType()))
                  .append(" width=100 height=10>");
            }

            if (choosenItem.isArmor() && choosenItem.getElementals() != null) {
               for(Elementals elm : choosenItem.getElementals()) {
                  if (elm.getValue() > 0) {
                     builder.append("<br><font color=827d78>")
                        .append(this.getElementName(Elementals.getReverseElement(elm.getElement())))
                        .append(" (")
                        .append(this.getElementName(elm.getElement()))
                        .append(" " + ServerStorage.getInstance().getString(player.getLang(), "CommunityAuction.DEF") + " ")
                        .append(elm.getValue())
                        .append(") </font><img src=L2UI_CT1.Gauge_DF_Attribute_")
                        .append(this.getElementName(elm.getElement()))
                        .append(" width=100 height=10>");
                  }
               }
            }
         }

         builder.append("</center>");
         html = html.replace("%page%", String.valueOf(page));
         html = html.replace("%priceItemId%", String.valueOf(priceItemId));
         html = html.replace("%type%", itemTypes[0] + " " + itemTypes[1]);
         html = html.replace("%grade%", itemGrade);
         html = html.replace("%search%", search == null ? "" : search);
         html = html.replace("%itemSort%", String.valueOf(itemSort));
         html = html.replace("%gradeSort%", String.valueOf(gradeSort));
         html = html.replace("%quantitySort%", String.valueOf(quantitySort));
         html = html.replace("%priceSort%", String.valueOf(priceSort));
         html = html.replace("%auctionId%", String.valueOf(auctionId));
         html = html.replace("%icon%", "<img src=icon." + choosenItem.getItem().getIcon() + " width=32 height=32>");
         html = html.replace(
            "%fullName%",
            "<table width=240 height=50><tr>"
               + this.getItemName(player, choosenItem, 240, 50, auction.isPrivateStore(), auction.getCountToSell() > 1L ? " x" + auction.getCountToSell() : "")
               + "</tr></table>"
         );
         html = html.replace(
            "%quantity%",
            auction.getCountToSell() > 1L
               ? "<edit var=\"quantity\" type=number value=\"\" width=140 height=12>"
               : "<center><font color=94775b>1</font></center>"
         );
         if (auction.getCountToSell() <= 1L) {
            html = html.replace("$quantity", "1");
         }

         html = html.replace("%pricePerItem%", "<font color=94775b>" + Util.getNumberWithCommas(auction.getPricePerItem()) + "</font>");
         html = html.replace(
            "%totalPrice%", "<font color=94775b>" + Util.getNumberWithCommas(auction.getCountToSell() * auction.getPricePerItem()) + "</font>"
         );
         html = html.replace("%priceItemName%", Util.getItemName(player, priceItemId));
         html = html.replace(
            "%priceItemCount%",
            Util.getNumberWithCommas(
               player.getInventory().getItemByItemId(priceItemId) == null ? 0L : player.getInventory().getItemByItemId(priceItemId).getCount()
            )
         );
         return html.replace("%fullAuctionDescription%", builder.toString());
      } else {
         return "";
      }
   }

   private String fillNewAuctionPage(Player player, String html, int priceItemId, boolean newItem, int currentItem, int line) {
      List<ItemInstance> itemsToAuction = this.getItemsToAuction(player, priceItemId);
      int maxLine = (int)Math.ceil((double)itemsToAuction.size() / 6.0);
      StringBuilder builder = new StringBuilder();
      int index = 0;

      for(int i = 6 * line; i < 6 * (line + 3); ++i) {
         ItemInstance item = i >= 0 && itemsToAuction.size() > i ? itemsToAuction.get(i) : null;
         if (index % 6 == 0) {
            builder.append("<tr>");
         }

         builder.append("<td width=32 align=center valign=top background=\"L2UI_CT1.ItemWindow_DF_SlotBox\">");
         if (item != null) {
            builder.append("<table border=0 cellspacing=0 cellpadding=0 width=32 height=32 background=" + item.getItem().getIcon() + ">");
         } else {
            builder.append("<table border=0 cellspacing=0 cellpadding=0 width=32 height=32>");
         }

         builder.append("<tr>");
         builder.append("<td width=32 height=32 align=center valign=top>");
         if (item != null) {
            builder.append("<button value=\"\" action=\"bypass _bbsNewAuction_ " + priceItemId + " _ n")
               .append(item.getObjectId())
               .append(" _ ")
               .append(line)
               .append("\" width=32 height=32 back=L2UI_CT1.ItemWindow_DF_Frame_Down fore=L2UI_CT1.ItemWindow_DF_Frame />");
         } else {
            builder.append("<br>");
         }

         builder.append("</td>");
         builder.append("</tr>");
         builder.append("</table>");
         builder.append("</td>");
         if (index % 6 == 5) {
            builder.append("</tr>");
         }

         ++index;
      }

      if (index % 6 != 5) {
         builder.append("</tr>");
      }

      html = html.replace("%auctionableItems%", builder.toString());
      builder = new StringBuilder();
      Collection<Auction> myAuctions = AuctionsManager.getInstance().getMyAuctions(player, priceItemId);
      Auction[] auctions = myAuctions.toArray(new Auction[myAuctions.size()]);
      boolean pakage = player.getPrivateStoreType() == 8;
      if (pakage || player.getPrivateStoreType() == 1) {
         for(TradeItem ti : player.getSellList().getItems()) {
            for(Auction auction : auctions) {
               if (auction.getItem() != null && auction.getItem().getObjectId() == ti.getObjectId()) {
                  myAuctions.remove(auction);
               }
            }
         }
      }

      auctions = myAuctions.toArray(new Auction[myAuctions.size()]);

      int i;
      for(i = 0; i < 10 && auctions.length > i; ++i) {
         Auction auction = auctions[i];
         ItemInstance item = auction.getItem();
         builder.append("<table border=0 cellspacing=0 cellpadding=0 width=470 bgcolor=").append(i % 2 == 1 ? "1a1914" : "23221d").append(">");
         builder.append("<tr><td fixwidth=240><table border=0 width=240><tr><td width=32 height=32 background=" + item.getItem().getIcon() + ">");
         if (!player.hasDialogAskActive()) {
            builder.append("<button value=\"\" action=\"bypass _bbsNewAuction_ " + priceItemId + " _ c")
               .append(item.getObjectId())
               .append(" _ ")
               .append(line)
               .append(" _ 1\" width=32 height=32 back=\"L2UI_CT1.ItemWindow_DF_Frame_Down\" fore=\"L2UI_CT1.ItemWindow_DF_Frame\">");
         } else {
            builder.append("<button width=32 height=32 back=\"L2UI_CT1.ItemWindow_DF_Frame_Down\" fore=\"L2UI_CT1.ItemWindow_DF_Frame\">");
         }

         builder.append("</td>");
         builder.append(this.getItemName(player, item, 228, 25, auction.isPrivateStore()));
         builder.append("</tr></table></td><td width=55><center>");
         if (item.getItem().getCrystalType() != 0) {
            builder.append("<img src=")
               .append(this.getGradeIcon(item.getItem().getItemsGrade(item.getItem().getCrystalType())))
               .append(" width=15 height=15>");
         } else {
            builder.append("None");
         }

         builder.append("</center></td><td width=75>");
         builder.append("<center>").append(auction.getCountToSell()).append("</center>");
         builder.append("</td><td width=100><center>");
         builder.append(Util.getNumberWithCommas(auction.getPricePerItem()));
         builder.append("</center></td></tr></table>");
      }

      if (i < 10) {
         builder.append("<table border=0 cellspacing=0 cellpadding=0 width=470 height=")
            .append((10 - i) * 35)
            .append("><tr><td width=260><br></td><td width=55></td><td width=55></td><td width=100></td></tr></table>");
      }

      html = html.replace("%priceItemId%", String.valueOf(priceItemId));
      html = html.replace("%priceItemName%", Util.getItemName(player, priceItemId));
      html = html.replace(
         "%priceItemCount%",
         Util.getNumberWithCommas(
            player.getInventory().getItemByItemId(priceItemId) == null ? 0L : player.getInventory().getItemByItemId(priceItemId).getCount()
         )
      );
      html = html.replace("%auctionItems%", builder.toString());
      html = html.replace("%auctioned%", "" + auctions.length);
      html = html.replace("%totalPrice%", Util.getNumberWithCommas(0L));
      html = html.replace("%saleFee%", Util.getNumberWithCommas(Config.AUCTION_FEE));
      html = html.replace("%currentItem%", (newItem ? "n" : "c") + currentItem);
      html = html.replace("%prevLine%", String.valueOf(Math.max(0, line - 1)));
      html = html.replace("%curLine%", String.valueOf(line));
      html = html.replace("%nextLine%", String.valueOf(Math.min(maxLine - 3, line + 1)));
      html = html.replace("%lastLine%", String.valueOf(Math.max(1, maxLine - 3)));
      ItemInstance choosenItem = currentItem > 0 ? player.getInventory().getItemByObjectId(currentItem) : null;
      html = html.replace("%choosenImage%", choosenItem != null ? "<img src=icon." + choosenItem.getItem().getIcon() + " width=32 height=32>" : "");
      html = html.replace(
         "%choosenItem%",
         choosenItem != null ? this.getItemName(player, choosenItem, 180, 45, false, choosenItem.getCount() > 1L ? " x" + choosenItem.getCount() : "") : ""
      );
      html = html.replace(
         "%quantity%",
         choosenItem != null && choosenItem.getCount() <= 1L ? "<center>1</center>" : "<edit var=\"quantity\" type=number value=\"\" width=140 height=12>"
      );
      return html.replace(
         "%NewAuctionButton%",
         choosenItem != null
            ? "<center><button value=\""
               + ServerStorage.getInstance().getString(player.getLang(), "CommunityAuction.NEW_AUCTION")
               + "\" action=\"bypass _bbsNewAuction_ "
               + priceItemId
               + " _ "
               + (newItem ? "n" : "c")
               + currentItem
               + " _ "
               + line
               + " _ 0 _ "
               + (choosenItem != null && choosenItem.getCount() <= 1L ? "1" : "$quantity")
               + " _ $sale_price\" width=120 height=30 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center>"
            : ""
      );
   }

   private List<Auction> getRightAuctions(Collection<Auction> allAuctions, int[] splitedTypes, String itemGrade, String search) {
      List<Auction> auctions = new ArrayList<>();
      Iterator var6 = allAuctions.iterator();

      while(true) {
         Auction auction;
         boolean found;
         do {
            if (!var6.hasNext()) {
               return auctions;
            }

            auction = (Auction)var6.next();
            if (splitedTypes == null || splitedTypes[0] < 0) {
               break;
            }

            found = false;
            AuctionItemTypes realItemType = auction.getItemType();
            AuctionItemTypes[] lookedTypes = this.getGroupsInType(splitedTypes[0]);
            if (splitedTypes[1] >= 0) {
               AuctionItemTypes lookedType = lookedTypes[splitedTypes[1]];
               lookedTypes = new AuctionItemTypes[]{lookedType};
            }

            for(AuctionItemTypes itemType : lookedTypes) {
               if (realItemType == itemType) {
                  found = true;
                  break;
               }
            }
         } while(!found);

         if ((itemGrade.equals("All") || auction.getItem().getItem().getItemsGrade(auction.getItem().getItem().getCrystalType()).equalsIgnoreCase(itemGrade))
            && (
               search == null
                  || StringUtils.containsIgnoreCase(auction.getItem().getNameRu(), search)
                  || StringUtils.containsIgnoreCase(auction.getItem().getName(), search)
            )) {
            auctions.add(auction);
         }
      }
   }

   protected String getElementName(int elementId) {
      String name = null;

      for(Elementals.Elemental att : Elementals.Elemental.VALUES) {
         if (att.getId() == elementId) {
            name = att.name();
            if (name == "UNHOLY") {
               name = "Dark";
            }

            if (name == "HOLY") {
               name = "Divine";
            }

            name = name.substring(0, 1) + name.substring(1).toLowerCase();
         }
      }

      return name;
   }

   private List<ItemInstance> getItemsToAuction(Player player, int priceItemId) {
      PcInventory inventory = player.getInventory();
      List<ItemInstance> items = new ArrayList<>();
      if (player.isInStoreMode()) {
         return items;
      } else {
         for(ItemInstance item : inventory.getItems()) {
            if (!item.getItem().isAdena()
               && item.isTradeable()
               && item.getItemLocation() != ItemInstance.ItemLocation.AUCTION
               && !item.isQuestItem()
               && !item.isAugmented()) {
               if (item.isStackable()) {
                  for(Auction playerAuction : AuctionsManager.getInstance().getMyAuctions(player, priceItemId)) {
                     if (playerAuction.getItem().getId() == item.getId()) {
                     }
                  }
               }

               if (!item.isEquipped()) {
                  items.add(item);
               }
            }
         }

         return items;
      }
   }

   private int getFunc(ItemInstance item, Stats stat) {
      for(FuncTemplate func : item.getItem().getAttachedFuncs()) {
         if (func.stat == stat) {
            return this.calc(item, (int)func.lambda.calc(null), stat);
         }
      }

      return 0;
   }

   protected int calc(ItemInstance item, int baseStat, Stats stat) {
      int value = baseStat;
      int enchant = item.getEnchantLevel();
      if (enchant <= 0) {
         return baseStat;
      } else {
         int overenchant = 0;
         if (enchant > 3) {
            overenchant = enchant - 3;
            enchant = 3;
         }

         if (stat == Stats.MAGIC_DEFENCE || stat == Stats.POWER_DEFENCE) {
            return baseStat + enchant + 3 * overenchant;
         } else if (stat == Stats.MAGIC_ATTACK) {
            switch(item.getItem().getItemGradeSPlus()) {
               case 0:
               case 1:
                  value = baseStat + 2 * enchant + 4 * overenchant;
                  break;
               case 2:
               case 3:
               case 4:
                  value = baseStat + 3 * enchant + 6 * overenchant;
                  break;
               case 5:
                  value = baseStat + 4 * enchant + 8 * overenchant;
            }

            return value;
         } else {
            if (item.isWeapon()) {
               WeaponType type = (WeaponType)item.getItemType();
               switch(item.getItem().getItemGradeSPlus()) {
                  case 0:
                  case 1:
                     switch(type) {
                        case BOW:
                        case CROSSBOW:
                           value = baseStat + 4 * enchant + 8 * overenchant;
                           return value;
                        default:
                           value = baseStat + 2 * enchant + 4 * overenchant;
                           return value;
                     }
                  case 2:
                  case 3:
                     switch(type) {
                        case BOW:
                        case CROSSBOW:
                           value = baseStat + 6 * enchant + 12 * overenchant;
                           return value;
                        case BIGSWORD:
                        case BIGBLUNT:
                        case DUAL:
                        case DUALFIST:
                        case ANCIENTSWORD:
                        case DUALDAGGER:
                           value = baseStat + 4 * enchant + 8 * overenchant;
                           return value;
                        default:
                           value = baseStat + 3 * enchant + 6 * overenchant;
                           return value;
                     }
                  case 4:
                     switch(type) {
                        case BOW:
                        case CROSSBOW:
                           value = baseStat + 8 * enchant + 16 * overenchant;
                           return value;
                        case BIGSWORD:
                        case BIGBLUNT:
                        case DUAL:
                        case DUALFIST:
                        case ANCIENTSWORD:
                        case DUALDAGGER:
                           value = baseStat + 5 * enchant + 10 * overenchant;
                           return value;
                        default:
                           value = baseStat + 4 * enchant + 8 * overenchant;
                           return value;
                     }
                  case 5:
                     switch(type) {
                        case BOW:
                        case CROSSBOW:
                           value = baseStat + 10 * enchant + 20 * overenchant;
                           break;
                        case BIGSWORD:
                        case BIGBLUNT:
                        case DUAL:
                        case DUALFIST:
                        case ANCIENTSWORD:
                        case DUALDAGGER:
                           value = baseStat + 6 * enchant + 12 * overenchant;
                           break;
                        default:
                           value = baseStat + 5 * enchant + 10 * overenchant;
                     }
               }
            }

            return value;
         }
      }
   }

   private String getItemName(Player player, ItemInstance item, int windowWidth, int windowHeight, boolean isPrivStore, String... addToItemName) {
      StringBuilder builder = new StringBuilder();
      if (item.getEnchantLevel() > 0) {
         builder.append("<font color=b3a683>+").append(item.getEnchantLevel()).append(" </font>");
      }

      String[] parts = player.getItemName(item.getItem()).split(" - ");
      String itemName = player.getItemName(item.getItem());
      itemName = itemName.replace("<", "&lt;").replace(">", "&gt;");
      if (parts.length <= 1 && (!item.isArmor() && !item.getItem().isAccessory() || !item.getName().endsWith("of Chaos"))) {
         builder.append(itemName);
      } else {
         builder.append("<font color=d4ce25>").append(itemName).append("</font>");
      }

      if (item.isWeapon() && item.getElementals() != null) {
         builder.append(" <font color=")
            .append(this.getElementColor(item.getAttackElementType()))
            .append(">")
            .append(this.getElementName(item.getAttackElementType()))
            .append("+")
            .append(item.getAttackElementPower())
            .append("</font>");
      }

      if (item.isArmor() && item.getElementals() != null) {
         for(Elementals elm : item.getElementals()) {
            if (elm.getValue() > 0) {
               builder.append(" <font color=")
                  .append(this.getElementColor(Elementals.getReverseElement(elm.getElement())))
                  .append(">")
                  .append(this.getElementName(Elementals.getReverseElement(elm.getElement())))
                  .append("</font>");
            }
         }
      }

      if (isPrivStore) {
         builder.append(" <font color=DE9DE8>(" + ServerStorage.getInstance().getString(player.getLang(), "CommunityAuction.PRIVATE_STORE") + ")</font>");
      }

      return "<td align=left width=228 height=25>" + builder.toString() + (addToItemName.length > 0 ? addToItemName[0] : "") + "</td>";
   }

   protected String getElementColor(int attId) {
      switch(attId) {
         case 0:
            return "b36464";
         case 1:
            return "528596";
         case 2:
            return "768f91";
         case 3:
            return "94775b";
         case 4:
            return "8c8787";
         case 5:
            return "4c558f";
         default:
            return "768f91";
      }
   }

   protected String getGradeIcon(String grade) {
      return grade != "NONE" ? "L2UI_CT1.Icon_DF_ItemGrade_" + grade.replace("S8", "8") : "";
   }

   private AuctionItemTypes[] getGroupsInType(int type) {
      return type > 0 && type < 7 ? ALL_AUCTION_ITEM_TYPES[type - 1] : null;
   }

   private List<Auction> sortAuctions(Player player, List<Auction> auctionsToSort, int itemSort, int gradeSort, int quantitySort, int priceSort) {
      if (itemSort != 0) {
         Collections.sort(auctionsToSort, new CommunityAuction.ItemNameComparator(player, itemSort == 1));
      } else if (gradeSort != 0) {
         Collections.sort(auctionsToSort, new CommunityAuction.GradeComparator(gradeSort == 1));
      } else if (quantitySort != 0) {
         Collections.sort(auctionsToSort, new CommunityAuction.QuantityComparator(quantitySort == 1));
      } else if (priceSort != 0) {
         Collections.sort(auctionsToSort, new CommunityAuction.PriceComparator(priceSort == 1));
      }

      return auctionsToSort;
   }

   @Override
   public void onWriteCommand(String command, String ar1, String ar2, String ar3, String ar4, String ar5, Player activeChar) {
   }

   public static CommunityAuction getInstance() {
      return CommunityAuction.SingletonHolder._instance;
   }

   private class ButtonClick implements OnAnswerListener {
      private final Player _player;
      private final int _priceItemId;
      private final ItemInstance _item;
      private final CommunityAuction.Buttons _button;
      private final String[] _args;

      private ButtonClick(Player player, int priceItemId, ItemInstance item, CommunityAuction.Buttons button, String... args) {
         this._player = player;
         this._priceItemId = priceItemId;
         this._item = item;
         this._button = button;
         this._args = args;
      }

      @Override
      public void sayYes() {
         switch(this._button) {
            case New_Auction:
               String sQuantity = this._args[0].replace(",", "").replace(".", "");
               String sPricePerItem = this._args[1].replace(",", "").replace(".", "");

               long quantity;
               long pricePerItem;
               try {
                  quantity = Long.parseLong(sQuantity);
                  pricePerItem = Long.parseLong(sPricePerItem);
               } catch (NumberFormatException var8) {
                  CommunityAuction.this.onBypassCommand("_bbsNewAuction_ " + this._priceItemId + " _ c0 _ 0", this._player);
                  return;
               }

               AuctionsManager.getInstance().checkAndAddNewAuction(this._player, this._item, quantity, this._priceItemId, pricePerItem);
               CommunityAuction.this.onBypassCommand("_bbsNewAuction_ " + this._priceItemId + " _ c0 _ 0", this._player);
               break;
            case Cancel_Auction:
               AuctionsManager.getInstance().deleteAuction(this._player, this._item, this._priceItemId);
               CommunityAuction.this.onBypassCommand("_bbsNewAuction_ " + this._priceItemId + " _ c0 _ 0", this._player);
               break;
            case Buy_Item:
               AuctionsManager.getInstance().buyItem(this._player, this._item, Long.parseLong(this._args[0]));
               CommunityAuction.this.onBypassCommand("_bbsAuction_ 1 _ " + this._priceItemId + " _ -1 _ All _  _ 1 _ 0 _ 0 _ 0", this._player);
         }
      }

      @Override
      public void sayNo() {
         switch(this._button) {
            case New_Auction:
            case Cancel_Auction:
               CommunityAuction.this.onBypassCommand("_bbsNewAuction_ " + this._priceItemId + " _ c0 _ 0", this._player);
               break;
            case Buy_Item:
               CommunityAuction.this.onBypassCommand("_bbsAuction_ 1 _ " + this._priceItemId + " _ -1 _ All _  _ 1 _ 0 _ 0 _ 0", this._player);
         }
      }
   }

   private static enum Buttons {
      New_Auction,
      Cancel_Auction,
      Buy_Item;
   }

   private static class GradeComparator implements Comparator<Auction>, Serializable {
      private static final long serialVersionUID = 4096813325789557518L;
      private final boolean _rightOrder;

      private GradeComparator(boolean rightOrder) {
         this._rightOrder = rightOrder;
      }

      public int compare(Auction o1, Auction o2) {
         int grade1 = o1.getItem().getItem().getCrystalType();
         int grade2 = o2.getItem().getItem().getCrystalType();
         return this._rightOrder ? Integer.compare(grade1, grade2) : Integer.compare(grade2, grade1);
      }
   }

   private static class ItemNameComparator implements Comparator<Auction>, Serializable {
      private static final long serialVersionUID = 7850753246573158288L;
      private final boolean _rightOrder;
      private final Player _player;

      private ItemNameComparator(Player player, boolean rightOrder) {
         this._player = player;
         this._rightOrder = rightOrder;
      }

      public int compare(Auction o1, Auction o2) {
         if (this._rightOrder) {
            return this._player.getLang() != null && !this._player.getLang().equalsIgnoreCase("en")
               ? o1.getItem().getNameRu().compareTo(o2.getItem().getNameRu())
               : o1.getItem().getName().compareTo(o2.getItem().getName());
         } else {
            return this._player.getLang() != null && !this._player.getLang().equalsIgnoreCase("en")
               ? o2.getItem().getNameRu().compareTo(o1.getItem().getNameRu())
               : o2.getItem().getName().compareTo(o1.getItem().getName());
         }
      }
   }

   private static class PriceComparator implements Comparator<Auction>, Serializable {
      private static final long serialVersionUID = 7065225580068613464L;
      private final boolean _rightOrder;

      private PriceComparator(boolean rightOrder) {
         this._rightOrder = rightOrder;
      }

      public int compare(Auction o1, Auction o2) {
         return this._rightOrder ? Long.compare(o1.getPricePerItem(), o2.getPricePerItem()) : Long.compare(o2.getPricePerItem(), o1.getPricePerItem());
      }
   }

   private static class QuantityComparator implements Comparator<Auction>, Serializable {
      private static final long serialVersionUID = 1572294088027593791L;
      private final boolean _rightOrder;

      private QuantityComparator(boolean rightOrder) {
         this._rightOrder = rightOrder;
      }

      public int compare(Auction o1, Auction o2) {
         return this._rightOrder ? Long.compare(o1.getCountToSell(), o2.getCountToSell()) : Long.compare(o2.getCountToSell(), o1.getCountToSell());
      }
   }

   private static class SingletonHolder {
      protected static final CommunityAuction _instance = new CommunityAuction();
   }
}
