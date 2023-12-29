package l2e.gameserver.model.entity.auction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.HennaParser;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.model.Elementals;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.ItemAuction;
import l2e.gameserver.model.items.ItemRequest;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.itemcontainer.PcInventory;
import l2e.gameserver.model.items.type.ArmorType;
import l2e.gameserver.model.items.type.EtcItemType;
import l2e.gameserver.model.items.type.WeaponType;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.serverpackets.InventoryUpdate;

public class AuctionsManager {
   private static AuctionsManager _instance;
   private static final Logger _log = Logger.getLogger(AuctionsManager.class.getName());
   private final Map<Integer, Auction> _auctions = new ConcurrentHashMap<>();
   private final List<Integer> _deadAuctions = new ArrayList<>();
   private final Map<Integer, Long> _lastMadeAuction = new ConcurrentHashMap<>();
   private int _lastId = -1;
   private static final int[] PET_FOOD_OR_SHOT = new int[]{
      6316, 2515, 4038, 5168, 5169, 7582, 9668, 10425, 6645, 20332, 20329, 20326, 10515, 6647, 6646, 20334, 20333, 20331, 20330, 20329, 20327, 10517, 10516
   };

   private AuctionsManager() {
      this.loadAuctions();
   }

   public Auction getAuction(int auctionId) {
      return this._auctions.get(auctionId);
   }

   public Auction getAuction(ItemInstance item) {
      for(Auction auction : this.getAllAuctions()) {
         if (auction.getItem().equals(item)) {
            return auction;
         }
      }

      return null;
   }

   public Collection<Auction> getAllAuctions() {
      return this._auctions.values();
   }

   public Collection<Auction> getAllAuctionsPerItemId(int itemId) {
      Collection<Auction> coll = new ArrayList<>();

      for(Auction auction : this.getAllAuctions()) {
         if (auction != null && auction.getPriceItemId() == itemId) {
            coll.add(auction);
         }
      }

      return coll;
   }

   public Collection<Auction> getMyAuctions(Player player, int priceItemId) {
      return this.getMyAuctions(player.getObjectId(), priceItemId);
   }

   public Collection<Auction> getMyAuctions(int playerObjectId, int priceItemId) {
      Collection<Auction> coll = new ArrayList<>();

      for(Auction auction : this.getAllAuctions()) {
         if (auction != null && auction.getSellerObjectId() == playerObjectId && auction.getPriceItemId() == priceItemId) {
            coll.add(auction);
         }
      }

      return coll;
   }

   private void loadAuctions() {
      ItemAuction.getInstance();

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT * FROM auctions");
         ResultSet rset = statement.executeQuery();
      ) {
         while(rset.next()) {
            int id = rset.getInt("auction_id");
            int sellerObjectId = rset.getInt("seller_object_id");
            String sellerName = rset.getString("seller_name");
            int itemObjectId = rset.getInt("item_object_id");
            int priceItemId = rset.getInt("price_itemId");
            long pricePerItem = rset.getLong("price_per_item");
            ItemInstance item = ItemAuction.getInstance().getItemByObjectId(itemObjectId);
            if (id > this._lastId) {
               this._lastId = id;
            }

            if (item != null) {
               Auction auction = new Auction(id, sellerObjectId, sellerName, item, priceItemId, pricePerItem, item.getCount(), this.getItemGroup(item), false);
               this._auctions.put(id, auction);
            } else {
               this._deadAuctions.add(id);
            }
         }
      } catch (Exception var67) {
         _log.log(Level.WARNING, "Error while loading Auctions", (Throwable)var67);
      }
   }

   public void addAuctionToDatabase(Auction auction) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("INSERT INTO auctions VALUES(?,?,?,?,?,?)");
      ) {
         statement.setInt(1, auction.getAuctionId());
         statement.setInt(2, auction.getSellerObjectId());
         statement.setString(3, auction.getSellerName());
         statement.setInt(4, auction.getItem().getObjectId());
         statement.setInt(5, auction.getPriceItemId());
         statement.setLong(6, auction.getPricePerItem());
         statement.execute();
      } catch (Exception var34) {
         _log.log(Level.WARNING, "Error while adding auction to database:", (Throwable)var34);
      }
   }

   public void addItemIdToSeller(int sellerObjectId, int itemId, long count) {
      int objId = -1;

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(
            "SELECT object_id FROM items WHERE item_id=" + itemId + " AND owner_id=" + sellerObjectId + " AND loc='INVENTORY'"
         );
         ResultSet rset = statement.executeQuery();
      ) {
         if (rset.next()) {
            objId = rset.getInt("object_id");
         }
      } catch (Exception var139) {
         _log.log(Level.WARNING, "Error while selecting itemId: " + itemId + "", (Throwable)var139);
      }

      if (objId == -1) {
         ItemInstance item = ItemsParser.getInstance().createItem("Auction", itemId, count, null, null);
         item.setCount(count);
         item.setOwnerId(sellerObjectId);
         item.setItemLocation(ItemInstance.ItemLocation.INVENTORY);
         item.updateDatabase();
      } else {
         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("UPDATE items SET count=count+" + count + " WHERE object_id=" + objId);
         ) {
            statement.execute();
         } catch (Exception var133) {
            _log.log(Level.WARNING, "Error while selecting itemId: " + itemId + "", (Throwable)var133);
         }
      }
   }

   private void deleteAuctionFromDatabase(Auction auction) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("DELETE FROM auctions WHERE auction_id = ?");
      ) {
         statement.setInt(1, auction.getAuctionId());
         statement.execute();
      } catch (Exception var34) {
         _log.log(Level.WARNING, "Error while deleting auction from database:", (Throwable)var34);
      }
   }

   public void deleteAuction(Player seller, ItemInstance item, int priceItemId) {
      Auction auction = null;

      for(Auction anyAuction : this.getMyAuctions(seller, priceItemId)) {
         if (anyAuction.getItem().equals(item)) {
            auction = anyAuction;
            break;
         }
      }

      this.deleteAuction(seller, auction);
   }

   public void deleteAuction(Player seller, Auction auction) {
      if (auction == null) {
         this.sendMessage(seller, "This auction doesnt exist anymore!");
      } else {
         ItemInstance item = auction.getItem();
         long count = item.getCount();
         if (!Config.ALLOW_AUCTION_OUTSIDE_TOWN && !seller.isInsideZone(ZoneId.PEACE)) {
            this.sendMessage(seller, "You cannot delete auction outside town!");
         }

         this._auctions.remove(auction.getAuctionId());
         PcInventory inventory = seller.getInventory();
         ItemAuction storage = ItemAuction.getInstance();
         ItemInstance createdItem = inventory.addItem("Remove Auction", item.getId(), count, seller, true);
         createdItem.setItemLocation(ItemInstance.ItemLocation.INVENTORY);
         createdItem.setEnchantLevel(item.getEnchantLevel());
         createdItem.setCustomType1(item.getCustomType1());
         createdItem.setCustomType2(item.getCustomType2());
         createdItem.setAugmentation(item.getAugmentation());
         createdItem.setVisualItemId(item.getVisualItemId());
         if (item.getElementals() != null) {
            for(Elementals elm : item.getElementals()) {
               if (elm.getElement() != -1 && elm.getValue() != -1) {
                  createdItem.setElementAttr(elm.getElement(), elm.getValue());
               }
            }
         }

         createdItem.updateDatabase(true);
         InventoryUpdate iu = new InventoryUpdate();
         iu.addModifiedItem(createdItem);
         seller.sendPacket(iu);
         storage.removeItemFromDb(item.getObjectId());
         storage.removeItem(item);
         this.deleteAuctionFromDatabase(auction);
         this.sendMessage(seller, "Auction has been removed!");
      }
   }

   public Auction addNewStore(Player seller, ItemInstance item, int saleItemId, long salePrice, long count) {
      int id = this.getNewId();
      AuctionItemTypes type = this.getItemGroup(item);
      return this.addAuction(seller, id, item, saleItemId, salePrice, count, type, true);
   }

   public void removeStore(Player seller, int auctionId) {
      if (Config.AUCTION_PRIVATE_STORE_AUTO_ADDED) {
         if (auctionId > 0) {
            Auction a = this.getAuction(auctionId);
            if (a != null && a.isPrivateStore() && a.getSellerObjectId() == seller.getObjectId()) {
               this._auctions.remove(auctionId);
            }
         }
      }
   }

   public synchronized void removePlayerStores(Player player) {
      if (Config.AUCTION_PRIVATE_STORE_AUTO_ADDED) {
         int playerObjId = player.getObjectId();
         List<Integer> keysToRemove = new ArrayList<>();

         for(Entry<Integer, Auction> auction : this._auctions.entrySet()) {
            if (auction.getValue().getSellerObjectId() == playerObjId && auction.getValue().isPrivateStore()) {
               keysToRemove.add(auction.getKey());
            }
         }

         for(Integer key : keysToRemove) {
            this._auctions.remove(key);
         }
      }
   }

   public void setNewCount(int auctionId, long newCount) {
      if (auctionId > 0) {
         this._auctions.get(auctionId).setCount(newCount);
      }
   }

   public void buyItem(Player buyer, ItemInstance item, long quantity) {
      Auction auction = this.getAuction(item);
      if (auction == null) {
         this.sendMessage(buyer, "This auction doesnt exist anymore!");
      } else if (buyer.isBlocked()) {
         this.sendMessage(buyer, "You cannot buy items while being blocked!");
      } else if (auction.getSellerObjectId() == buyer.getObjectId()) {
         this.sendMessage(buyer, "You cannot win your own auction!");
      } else if (quantity <= 0L) {
         this.sendMessage(buyer, "You need to buy at least one item!");
      } else if (item.getCount() < quantity) {
         this.sendMessage(buyer, "You are trying to buy too many items!");
      } else if (buyer.getInventory().getItemByItemId(auction.getPriceItemId()) == null
         || auction.getPricePerItem() * quantity > buyer.getInventory().getItemByItemId(auction.getPriceItemId()).getCount()) {
         this.sendMessage(buyer, "You don't have enough " + Util.getItemName(buyer, auction.getPriceItemId()) + "!");
      } else if (!Config.ALLOW_AUCTION_OUTSIDE_TOWN && !buyer.isInsideZone(ZoneId.PEACE)) {
         this.sendMessage(buyer, "You can't use buy that item outside town!");
      } else if (auction.isPrivateStore()) {
         Player seller = World.getInstance().getPlayer(auction.getSellerObjectId());
         if (seller == null) {
            this.sendMessage(buyer, "This auction doesnt exist anymore !");
         } else {
            Set<ItemRequest> _items = new HashSet<>();
            _items.add(new ItemRequest(item.getObjectId(), quantity, auction.getPricePerItem()));
            seller.getSellList().privateStoreBuy(buyer, _items);
            if (seller.getSellList().getItemCount() == 0) {
               seller.setPrivateStoreType(0);
               seller.broadcastUserInfo(true);
            }
         }
      } else {
         buyer.destroyItemByItemId("Auction Online Bought", auction.getPriceItemId(), auction.getPricePerItem() * quantity, null, true);
         boolean wholeItemBought = false;
         PcInventory inventory = buyer.getInventory();
         ItemAuction storage = ItemAuction.getInstance();
         if (item.getCount() == quantity) {
            ItemInstance createdItem = inventory.addItem("Auction Part Bought", item.getId(), quantity, buyer, true);
            createdItem.setItemLocation(ItemInstance.ItemLocation.INVENTORY);
            createdItem.setEnchantLevel(item.getEnchantLevel());
            createdItem.setCustomType1(item.getCustomType1());
            createdItem.setCustomType2(item.getCustomType2());
            createdItem.setAugmentation(item.getAugmentation());
            createdItem.setVisualItemId(item.getVisualItemId());
            if (item.getElementals() != null) {
               for(Elementals elm : item.getElementals()) {
                  if (elm.getElement() != -1 && elm.getValue() != -1) {
                     createdItem.setElementAttr(elm.getElement(), elm.getValue());
                  }
               }
            }

            createdItem.updateDatabase(true);
            InventoryUpdate iu = new InventoryUpdate();
            iu.addModifiedItem(createdItem);
            buyer.sendPacket(iu);
            storage.removeItemFromDb(item.getObjectId());
            storage.removeItem(item);
            this.deleteAuctionFromDatabase(auction);
            this._auctions.remove(auction.getAuctionId());
            wholeItemBought = true;
         } else {
            ItemInstance newItem = this.copyItem(item, quantity);
            ItemInstance createdItem = inventory.addItem("Auction Part Bought", newItem.getId(), newItem.getCount(), buyer, true);
            InventoryUpdate iu = new InventoryUpdate();
            iu.addModifiedItem(createdItem);
            buyer.sendPacket(iu);
            storage.changeCount(item, auction.getCountToSell() - quantity);
            auction.setCount(auction.getCountToSell() - quantity);
         }

         Player seller = World.getInstance().getPlayer(auction.getSellerObjectId());
         if (seller != null) {
            if (wholeItemBought) {
               seller.sendMessage(item.getName() + " has been bought by " + buyer.getName() + "!");
            } else {
               seller.sendMessage(quantity + " " + item.getName() + (quantity > 1L ? "s" : "") + " has been bought by " + buyer.getName() + "!");
            }

            seller.addItem("Auction Online Sold", auction.getPriceItemId(), auction.getPricePerItem() * quantity, null, true);
         } else {
            this.addItemIdToSeller(auction.getSellerObjectId(), auction.getPriceItemId(), auction.getPricePerItem() * quantity);
         }

         buyer.sendMessage("You have bought " + item.getName());
      }
   }

   public void checkAndAddNewAuction(Player seller, ItemInstance item, long quantity, int saleItemId, long salePrice) {
      if (this.checkIfItsOk(seller, item, quantity, saleItemId, salePrice)) {
         int id = this.getNewId();
         if (id < 0) {
            this.sendMessage(seller, "There are currently too many auctions!");
         } else {
            AuctionItemTypes type = this.getItemGroup(item);
            PcInventory inventory = seller.getInventory();
            ItemAuction storage = ItemAuction.getInstance();
            Auction auction = null;
            if (item.getCount() > quantity) {
               ItemInstance newItem = this.copyItem(item, quantity);
               seller.destroyItem("Create Auction", item, quantity, null, true);
               storage.addItem("Create Auction", newItem, null, null);
               auction = this.addAuction(seller, id, newItem, saleItemId, salePrice, quantity, type, false);
               InventoryUpdate iu = new InventoryUpdate();
               iu.addModifiedItem(item);
               seller.sendPacket(iu);
            } else {
               inventory.removeItem(item);
               item.setCount(quantity);
               storage.addFullItem(item);
               auction = this.addAuction(seller, id, item, saleItemId, salePrice, quantity, type, false);
               InventoryUpdate iu = new InventoryUpdate();
               iu.addRemovedItem(item);
               seller.sendPacket(iu);
            }

            if (Config.ALLOW_ADDING_AUCTION_DELAY) {
               this._lastMadeAuction.put(seller.getObjectId(), System.currentTimeMillis() + (long)(Config.SECONDS_BETWEEN_ADDING_AUCTIONS * 1000));
            }

            seller.getInventory().reduceAdena("Create Auctino Fee", Config.AUCTION_FEE, null, true);
            this.addAuctionToDatabase(auction);
            this.sendMessage(seller, "Auction has been created!");
         }
      }
   }

   private Auction addAuction(
      Player seller, int auctionId, ItemInstance item, int saleItemId, long salePrice, long sellCount, AuctionItemTypes itemType, boolean privateStore
   ) {
      Auction newAuction = new Auction(auctionId, seller.getObjectId(), seller.getName(), item, saleItemId, salePrice, sellCount, itemType, privateStore);
      this._auctions.put(auctionId, newAuction);
      return newAuction;
   }

   public void sendMessage(Player player, String message) {
      player.sendMessage(message);
   }

   private ItemInstance copyItem(ItemInstance oldItem, long quantity) {
      ItemInstance item = new ItemInstance(IdFactory.getInstance().getNextId(), oldItem.getId());
      item.setOwnerId(oldItem.getOwnerId());
      item.setCount(quantity);
      item.setEnchantLevel(oldItem.getEnchantLevel());
      item.setItemLocation(ItemInstance.ItemLocation.AUCTION);
      item.setCustomType1(oldItem.getCustomType1());
      item.setCustomType2(oldItem.getCustomType2());
      item.setAugmentation(oldItem.getAugmentation());
      item.setVisualItemId(oldItem.getVisualItemId());
      if (oldItem.getElementals() != null) {
         for(Elementals elm : oldItem.getElementals()) {
            if (elm.getElement() != -1 && elm.getValue() != -1) {
               item.setElementAttr(elm.getElement(), elm.getValue());
            }
         }
      }

      return item;
   }

   private synchronized int getNewId() {
      return ++this._lastId;
   }

   private boolean checkIfItsOk(Player seller, ItemInstance item, long quantity, int priceItemId, long salePrice) {
      if (seller == null) {
         return false;
      } else if (item == null) {
         this.sendMessage(seller, "Item you are trying to sell, doesn't exist!");
         return false;
      } else if (item.getOwnerId() != seller.getObjectId() || seller.getInventory().getItemByObjectId(item.getObjectId()) == null) {
         this.sendMessage(seller, "Item you are trying to sell, doesn't exist!");
         return false;
      } else if (item.isEquipped()) {
         this.sendMessage(seller, "You need to unequip that item first!");
         return false;
      } else if (item.isAugmented()) {
         this.sendMessage(seller, "You cannot sell Augmented weapons!");
         return false;
      } else if (item.isQuestItem()) {
         this.sendMessage(seller, "You can't sell quest items!");
         return false;
      } else if (!item.isTradeable()) {
         this.sendMessage(seller, "You cannot sell this item!");
         return false;
      } else if (seller.getSummon() != null && item.getItemType() == EtcItemType.PET_COLLAR) {
         this.sendMessage(seller, "Please unsummon your pet before trying to sell this item.");
         return false;
      } else if (seller.getSummon() != null && item.isSummon() && item.getItem().isPetItem()) {
         this.sendMessage(seller, "Please unsummon your pet before trying to sell this item.");
         return false;
      } else if (quantity < 1L) {
         this.sendMessage(seller, "Quantity is too low!");
         return false;
      } else if (item.getCount() < quantity) {
         this.sendMessage(seller, "You don't have enough items to sell!");
         return false;
      } else if (seller.getAdena() < Config.AUCTION_FEE) {
         this.sendMessage(seller, "You don't have enough adena, to pay the fee!");
         return false;
      } else if (salePrice <= 0L) {
         this.sendMessage(seller, "Sale price is too low!");
         return false;
      } else if (salePrice > 999999999999L) {
         this.sendMessage(seller, "Price is too high!");
         return false;
      } else if (seller.isBlocked()) {
         this.sendMessage(seller, "Cannot create auctions while being Blocked!");
         return false;
      } else if (this.getMyAuctions(seller, priceItemId).size() >= 10) {
         this.sendMessage(seller, "You can have just 10 auctions at the time!");
         return false;
      } else if (!Config.ALLOW_AUCTION_OUTSIDE_TOWN && !seller.isInsideZone(ZoneId.PEACE)) {
         this.sendMessage(seller, "You cannot add new Auction outside town!");
         return false;
      } else if (seller.isInStoreMode()) {
         this.sendMessage(seller, "Close your store before creating new Auction!");
         return false;
      } else if (Config.ALLOW_ADDING_AUCTION_DELAY
         && this._lastMadeAuction.containsKey(seller.getObjectId())
         && this._lastMadeAuction.get(seller.getObjectId()) > System.currentTimeMillis()) {
         this.sendMessage(seller, "You cannot do it so often!");
         return false;
      } else {
         return true;
      }
   }

   private AuctionItemTypes getItemGroup(ItemInstance item) {
      if (item.isEquipable()) {
         if (item.getItem().getBodyPart() == 6) {
            return AccessoryItemType.EARRING;
         }

         if (item.getItem().getBodyPart() == 48) {
            return AccessoryItemType.RING;
         }

         if (item.getItem().getBodyPart() == 8) {
            return AccessoryItemType.NECKLACE;
         }

         if (item.getItem().getBodyPart() == 2097152 || item.getItem().getBodyPart() == 1048576) {
            return AccessoryItemType.BRACELET;
         }

         if (item.getItem().getBodyPart() == 65536 || item.getItem().getBodyPart() == 524288 || item.getItem().getBodyPart() == 262144) {
            return AccessoryItemType.ACCESSORY;
         }
      }

      if (item.isArmor()) {
         if (item.getItem().getBodyPart() == 64) {
            return ArmorItemType.HELMET;
         }

         if (item.getItem().getBodyPart() == 1024) {
            return ArmorItemType.CHEST;
         }

         if (item.getItem().getBodyPart() == 2048) {
            return ArmorItemType.LEGS;
         }

         if (item.getItem().getBodyPart() == 512) {
            return ArmorItemType.GLOVES;
         }

         if (item.getItem().getBodyPart() == 4096) {
            return ArmorItemType.SHOES;
         }

         if (item.getItem().isCloak()) {
            return ArmorItemType.CLOAK;
         }

         if (item.getItem().isUnderwear()) {
            return ArmorItemType.SHIRT;
         }

         if (item.getItem().isBelt()) {
            return ArmorItemType.BELT;
         }
      }

      if (item.getItem().isEnchantScroll()) {
         return EtcAuctionItemType.ENCHANT;
      } else if (item.getItem().isLifeStone()) {
         return EtcAuctionItemType.LIFE_STONE;
      } else if (item.getItem().isAttributeCrystal() || item.getItem().isAttributeStone()) {
         return EtcAuctionItemType.ATTRIBUTE;
      } else if (item.getItem().isCodexBook()) {
         return EtcAuctionItemType.CODEX;
      } else if (item.getItem().isForgottenScroll()) {
         return EtcAuctionItemType.FORGOTTEN_SCROLL;
      } else if (item.getItem().isSoulCrystal()) {
         return EtcAuctionItemType.SA_CRYSTAL;
      } else if (item.isPet()) {
         return PetItemType.PET;
      } else if (item.getItemType() == EtcItemType.PET_COLLAR) {
         return PetItemType.PET;
      } else if (item.getItem().isPetItem()) {
         return PetItemType.GEAR;
      } else if (this.isBabyFoodOrShot(item.getId())) {
         return PetItemType.OTHER;
      } else if (item.getItemType() == EtcItemType.POTION) {
         return SuppliesItemType.ELIXIR;
      } else if (HennaParser.getInstance().isHenna(item.getId())) {
         return SuppliesItemType.DYE;
      } else if (item.getItemType() == EtcItemType.SCROLL) {
         return SuppliesItemType.SCROLL;
      } else if (item.getItem().isKeyMatherial()) {
         return SuppliesItemType.KEY_MATERIAL;
      } else if (item.getItem().isRecipe()) {
         return SuppliesItemType.RECIPE;
      } else if (item.getItemType() == EtcItemType.MATERIAL) {
         return SuppliesItemType.MATERIAL;
      } else if (item.getItemType() instanceof EtcItemType) {
         return SuppliesItemType.MISCELLANEOUS;
      } else if (item.isWeapon()) {
         if (item.getItemType() == WeaponType.SWORD) {
            return WeaponItemType.SWORD;
         } else if (item.getItemType() == WeaponType.ANCIENTSWORD) {
            return WeaponItemType.ANCIENT_SWORD;
         } else if (item.getItemType() == WeaponType.BIGSWORD) {
            return WeaponItemType.BIG_SWORD;
         } else if (item.getItemType() == WeaponType.BLUNT) {
            return WeaponItemType.BLUNT;
         } else if (item.getItemType() == WeaponType.BIGBLUNT) {
            return WeaponItemType.BIG_BLUNT;
         } else if (item.getItemType() == WeaponType.DAGGER) {
            return WeaponItemType.DAGGER;
         } else if (item.getItemType() == WeaponType.DUALDAGGER) {
            return WeaponItemType.DUAL_DAGGER;
         } else if (item.getItemType() == WeaponType.BOW) {
            return WeaponItemType.BOW;
         } else if (item.getItemType() == WeaponType.CROSSBOW) {
            return WeaponItemType.CROSSBOW;
         } else if (item.getItemType() == WeaponType.POLE) {
            return WeaponItemType.POLE;
         } else if (item.getItemType() == WeaponType.DUALFIST) {
            return WeaponItemType.FISTS;
         } else {
            return item.getItemType() == WeaponType.RAPIER ? WeaponItemType.RAPIER : WeaponItemType.OTHER_W;
         }
      } else if (item.getItem().getBodyPart() != 256) {
         return SuppliesItemType.MISCELLANEOUS;
      } else {
         return item.getItemType() == ArmorType.SIGIL ? ArmorItemType.SIGIL : ArmorItemType.SHIELD;
      }
   }

   private boolean isBabyFoodOrShot(int id) {
      for(int i : PET_FOOD_OR_SHOT) {
         if (i == id) {
            return true;
         }
      }

      return false;
   }

   public static AuctionsManager getInstance() {
      if (_instance == null) {
         _instance = new AuctionsManager();
      }

      return _instance;
   }
}
