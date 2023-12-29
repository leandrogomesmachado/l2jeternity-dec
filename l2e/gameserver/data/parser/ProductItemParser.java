package l2e.gameserver.data.parser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import l2e.gameserver.Config;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.ProductItem;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.ProductItemTemplate;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.network.serverpackets.ExBrBuyProduct;
import l2e.gameserver.network.serverpackets.ExBrGamePoint;
import l2e.gameserver.network.serverpackets.ExBrRecentProductList;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class ProductItemParser extends DocumentParser {
   private final Map<Integer, ProductItem> _itemsList = new TreeMap<>();
   private final ConcurrentHashMap<Integer, List<ProductItem>> _recentList = new ConcurrentHashMap<>();

   protected ProductItemParser() {
      this.load();
   }

   @Override
   public final void load() {
      this._itemsList.clear();
      this._recentList.clear();
      this.parseDatapackFile("data/stats/services/item-mall.xml");
      this._log.info(this.getClass().getSimpleName() + ": Loaded " + this._itemsList.size() + " items for item mall.");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node c = this.getCurrentDocument().getFirstChild(); c != null; c = c.getNextSibling()) {
         if ("list".equalsIgnoreCase(c.getNodeName())) {
            for(Node n = c.getFirstChild(); n != null; n = n.getNextSibling()) {
               if ("product".equalsIgnoreCase(n.getNodeName())) {
                  NamedNodeMap list = n.getAttributes();
                  int productId = Integer.parseInt(list.getNamedItem("id").getNodeValue());
                  int category = list.getNamedItem("category") != null ? Integer.parseInt(list.getNamedItem("category").getNodeValue()) : 5;
                  int price = list.getNamedItem("price") != null ? Integer.parseInt(list.getNamedItem("price").getNodeValue()) : 0;
                  Boolean isEvent = list.getNamedItem("isEvent") != null && Boolean.parseBoolean(list.getNamedItem("isEvent").getNodeValue());
                  Boolean isBest = list.getNamedItem("isBest") != null && Boolean.parseBoolean(list.getNamedItem("isBest").getNodeValue());
                  Boolean isNew = list.getNamedItem("isNew") != null && Boolean.parseBoolean(list.getNamedItem("isNew").getNodeValue());
                  int tabId = getProductTabId(isEvent, isBest, isNew);
                  long startTimeSale = list.getNamedItem("sale_start_date") != null
                     ? getMillisecondsFromString(list.getNamedItem("sale_start_date").getNodeValue())
                     : 0L;
                  long endTimeSale = list.getNamedItem("sale_end_date") != null
                     ? getMillisecondsFromString(list.getNamedItem("sale_end_date").getNodeValue())
                     : 0L;
                  int daysOfWeek = list.getNamedItem("daysOfWeek") != null ? Integer.parseInt(list.getNamedItem("daysOfWeek").getNodeValue()) : 127;
                  int stock = list.getNamedItem("stock") != null ? Integer.parseInt(list.getNamedItem("stock").getNodeValue()) : 0;
                  int maxStock = list.getNamedItem("maxStock") != null ? Integer.parseInt(list.getNamedItem("maxStock").getNodeValue()) : -1;
                  ArrayList<ProductItemTemplate> components = new ArrayList<>();
                  ProductItem pr = new ProductItem(productId, category, price, tabId, startTimeSale, endTimeSale, daysOfWeek, stock, maxStock);

                  for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
                     if ("component".equalsIgnoreCase(d.getNodeName())) {
                        NamedNodeMap component = d.getAttributes();
                        int itemId = Integer.parseInt(component.getNamedItem("itemId").getNodeValue());
                        int count = Integer.parseInt(component.getNamedItem("count").getNodeValue());
                        ProductItemTemplate product = new ProductItemTemplate(itemId, count);
                        components.add(product);
                     }
                  }

                  pr.setComponents(components);
                  this._itemsList.put(productId, pr);
               }
            }
         }
      }
   }

   public void requestBuyItem(Player player, int productId, int count) {
      if (count <= 99 && count > 0) {
         ProductItem product = getInstance().getProduct(productId);
         if (product == null) {
            player.sendPacket(new ExBrBuyProduct(-2));
         } else if (System.currentTimeMillis() >= product.getStartTimeSale() && System.currentTimeMillis() <= product.getEndTimeSale()) {
            long totalPoints = (long)(product.getPoints() * count);
            if (totalPoints <= 0L) {
               player.sendPacket(new ExBrBuyProduct(-2));
            } else {
               long gamePointSize = Config.GAME_POINT_ITEM_ID == -1
                  ? player.getGamePoints()
                  : player.getInventory().getInventoryItemCount(Config.GAME_POINT_ITEM_ID, -1);
               if (totalPoints > gamePointSize) {
                  player.sendPacket(new ExBrBuyProduct(-1));
               } else {
                  int totalWeight = 0;

                  for(ProductItemTemplate com : product.getComponents()) {
                     totalWeight += com.getWeight();
                  }

                  totalWeight *= count;
                  int totalCount = 0;

                  for(ProductItemTemplate com : product.getComponents()) {
                     Item item = ItemsParser.getInstance().getTemplate(com.getId());
                     if (item == null) {
                        player.sendPacket(new ExBrBuyProduct(-2));
                        return;
                     }

                     totalCount += item.isStackable() ? 1 : com.getCount() * count;
                  }

                  if (player.getInventory().validateCapacity((long)totalCount) && player.getInventory().validateWeight((long)totalWeight)) {
                     if (Config.GAME_POINT_ITEM_ID == -1) {
                        player.setGamePoints(player.getGamePoints() - totalPoints);
                     } else {
                        player.getInventory().destroyItemByItemId("Buy Product" + productId, Config.GAME_POINT_ITEM_ID, totalPoints, player, null);
                     }

                     for(ProductItemTemplate comp : product.getComponents()) {
                        player.getInventory().addItem("Buy Product" + productId, comp.getId(), (long)(comp.getCount() * count), player, null);
                     }

                     if (this._recentList.get(player.getObjectId()) == null) {
                        List<ProductItem> charList = new ArrayList<>();
                        charList.add(product);
                        this._recentList.put(player.getObjectId(), charList);
                     } else {
                        this._recentList.get(player.getObjectId()).add(product);
                     }

                     StatusUpdate su = new StatusUpdate(player);
                     su.addAttribute(14, player.getCurrentLoad());
                     player.sendPacket(su);
                     player.sendPacket(new ExBrGamePoint(player));
                     player.sendPacket(new ExBrBuyProduct(1));
                  } else {
                     player.sendPacket(new ExBrBuyProduct(-4));
                  }
               }
            }
         } else {
            player.sendPacket(new ExBrBuyProduct(-7));
         }
      }
   }

   private static int getProductTabId(boolean isEvent, boolean isBest, boolean isNew) {
      if (isEvent && isBest) {
         return 3;
      } else if (isEvent) {
         return 1;
      } else {
         return isBest ? 2 : 4;
      }
   }

   private static long getMillisecondsFromString(String datetime) {
      DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");

      try {
         Date time = df.parse(datetime);
         Calendar calendar = Calendar.getInstance();
         calendar.setTime(time);
         return calendar.getTimeInMillis();
      } catch (Exception var4) {
         var4.printStackTrace();
         return 0L;
      }
   }

   public Collection<ProductItem> getAllItems() {
      return this._itemsList.values();
   }

   public ProductItem getProduct(int id) {
      return this._itemsList.get(id);
   }

   public void recentProductList(Player player) {
      player.sendPacket(new ExBrRecentProductList(player.getObjectId()));
   }

   public List<ProductItem> getRecentListByOID(int objId) {
      return (List<ProductItem>)(this._recentList.get(objId) == null ? new ArrayList<>() : this._recentList.get(objId));
   }

   public static ProductItemParser getInstance() {
      return ProductItemParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final ProductItemParser _instance = new ProductItemParser();
   }
}
