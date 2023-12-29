package l2e.gameserver.model.items.instance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import l2e.commons.util.GMAudit;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.data.parser.EnchantItemOptionsParser;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.data.parser.OptionsParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.instancemanager.ItemsOnGroundManager;
import l2e.gameserver.instancemanager.MercTicketManager;
import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.listener.events.AugmentEvent;
import l2e.gameserver.listener.events.ItemDropEvent;
import l2e.gameserver.listener.events.ItemPickupEvent;
import l2e.gameserver.listener.player.AugmentListener;
import l2e.gameserver.listener.player.DropListener;
import l2e.gameserver.model.Augmentation;
import l2e.gameserver.model.DropProtection;
import l2e.gameserver.model.Elementals;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.ShotType;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.Armor;
import l2e.gameserver.model.actor.templates.items.EtcItem;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.actor.templates.items.Weapon;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.items.itemcontainer.PcInventory;
import l2e.gameserver.model.items.type.EtcItemType;
import l2e.gameserver.model.items.type.ItemType;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.funcs.Func;
import l2e.gameserver.model.skills.options.EnchantOptions;
import l2e.gameserver.model.skills.options.Options;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.DropItem;
import l2e.gameserver.network.serverpackets.GetItem;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.SpawnItem;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class ItemInstance extends GameObject {
   protected static final Logger _log = Logger.getLogger(ItemInstance.class.getName());
   private static final Logger _logItems = Logger.getLogger("item");
   private static List<AugmentListener> augmentListeners = new LinkedList<>();
   private static List<DropListener> dropListeners = new LinkedList<>();
   private int _ownerId;
   private int _dropperObjectId = 0;
   private long _count;
   private long _initCount;
   private long _time;
   private boolean _decrease = false;
   private int _itemId;
   private int _visualItemId = 0;
   private Item _item;
   private ItemInstance.ItemLocation _loc;
   private int _locData;
   private int _enchantLevel;
   private boolean _wear;
   private Augmentation _augmentation = null;
   private int _agathionEnergy = -1;
   private boolean _consumingEnergy = false;
   private static final int ENERGY_CONSUMPTION_RATE = 60000;
   private int _mana = -1;
   private Future<?> _consumingMana = null;
   private static final int MANA_CONSUMPTION_RATE = 60000;
   private int _type1;
   private int _type2;
   private long _dropTime;
   private boolean _published = false;
   private boolean _protected;
   public static final int UNCHANGED = 0;
   public static final int ADDED = 1;
   public static final int REMOVED = 3;
   public static final int MODIFIED = 2;
   public static final int[] DEFAULT_ENCHANT_OPTIONS = new int[]{0, 0, 0};
   private int _lastChange = 2;
   private boolean _existsInDb;
   private boolean _storedInDb;
   private final ReentrantLock _dbLock = new ReentrantLock();
   private Elementals[] _elementals = null;
   private ScheduledFuture<?> itemLootShedule = null;
   public ScheduledFuture<?> _lifeTimeTask;
   private final DropProtection _dropProtection = new DropProtection();
   private int _shotsMask = 0;
   private final List<Options> _enchantOptions = new ArrayList<>();

   public ItemInstance(int objectId, int itemId) {
      super(objectId);
      this.setInstanceType(GameObject.InstanceType.ItemInstance);
      this._itemId = itemId;
      this._item = ItemsParser.getInstance().getTemplate(itemId);
      if (this._itemId != 0 && this._item != null) {
         super.setName(this._item.getNameEn());
         super.setNameRu(this._item.getNameRu());
         this.setCount(1L);
         this._loc = ItemInstance.ItemLocation.VOID;
         this._type1 = 0;
         this._type2 = 0;
         this._dropTime = 0L;
         this._mana = this._item.getDuration();
         this._agathionEnergy = this._item.getAgathionMaxEnergy();
         this._time = this._item.getTime() == -1 ? -1L : System.currentTimeMillis() + (long)this._item.getTime() * 60L * 1000L;
         this.scheduleLifeTimeTask();
      } else {
         throw new IllegalArgumentException();
      }
   }

   public ItemInstance(int objectId, Item item) {
      super(objectId);
      this.setInstanceType(GameObject.InstanceType.ItemInstance);
      this._itemId = item.getId();
      this._item = item;
      if (this._itemId == 0) {
         throw new IllegalArgumentException();
      } else {
         super.setName(this._item.getNameEn());
         super.setNameRu(this._item.getNameRu());
         this.setCount(1L);
         this._loc = ItemInstance.ItemLocation.VOID;
         this._mana = this._item.getDuration();
         this._agathionEnergy = this._item.getAgathionMaxEnergy();
         this._time = this._item.getTime() == -1 ? -1L : System.currentTimeMillis() + (long)this._item.getTime() * 60L * 1000L;
         this.scheduleLifeTimeTask();
      }
   }

   public ItemInstance(int itemId) {
      this(IdFactory.getInstance().getNextId(), itemId);
   }

   public final void pickupMe(Creature player) {
      if (this.firePickupListeners(player.getActingPlayer())) {
         player.broadcastPacket(new GetItem(this, player.getObjectId()));
         int itemId = this.getId();
         if (MercTicketManager.getInstance().getTicketCastleId(itemId) > 0) {
            MercTicketManager.getInstance().removeTicket(this);
            ItemsOnGroundManager.getInstance().removeObject(this);
         }

         if (this.getReflectionId() > 0 && ReflectionManager.getInstance().getReflection(this.getReflectionId()) != null) {
            ReflectionManager.getInstance().getReflection(this.getReflectionId()).removeItem(this);
         }

         if (!Config.DISABLE_TUTORIAL && (itemId == 57 || itemId == 6353)) {
            Player actor = player.getActingPlayer();
            if (actor != null) {
               QuestState qs = actor.getQuestState("_255_Tutorial");
               if (qs != null && qs.getQuest() != null) {
                  qs.getQuest().notifyEvent("CE" + itemId, null, actor);
               }
            }
         }

         this.setIsVisible(false);
      }
   }

   public void setOwnerId(String process, int owner_id, Player creator, Object reference) {
      this.setOwnerId(owner_id);
      if (Config.LOG_ITEMS && (!Config.LOG_ITEMS_SMALL_LOG || Config.LOG_ITEMS_SMALL_LOG && (this.getItem().isEquipable() || this.getItem().getId() == 57))) {
         LogRecord record = new LogRecord(Level.INFO, "SETOWNER:" + process);
         record.setLoggerName("item");
         record.setParameters(new Object[]{this, creator, reference});
         _logItems.log(record);
      }

      if (creator != null && creator.isGM()) {
         String referenceName = "no-reference";
         if (reference instanceof GameObject) {
            referenceName = ((GameObject)reference).getName() != null ? ((GameObject)reference).getName() : "no-name";
         } else if (reference instanceof String) {
            referenceName = (String)reference;
         }

         String targetName = creator.getTarget() != null ? creator.getTarget().getName() : "no-target";
         if (Config.GMAUDIT) {
            GMAudit.auditGMAction(
               creator.getName() + " [" + creator.getObjectId() + "]",
               process + "(id: " + this.getId() + " name: " + this.getName() + ")",
               targetName,
               "GameObject referencing this action is: " + referenceName
            );
         }
      }
   }

   public void setOwnerId(int owner_id) {
      if (owner_id != this._ownerId) {
         this.removeSkillsFromOwner();
         this._ownerId = owner_id;
         this._storedInDb = false;
         this.giveSkillsToOwner();
      }
   }

   public int getOwnerId() {
      return this._ownerId;
   }

   public void setItemLocation(ItemInstance.ItemLocation loc) {
      this.setItemLocation(loc, 0);
   }

   public void setItemLocation(ItemInstance.ItemLocation loc, int loc_data) {
      if (loc != this._loc || loc_data != this._locData) {
         this.removeSkillsFromOwner();
         this._loc = loc;
         this._locData = loc_data;
         this._storedInDb = false;
         this.giveSkillsToOwner();
      }
   }

   public ItemInstance.ItemLocation getItemLocation() {
      return this._loc;
   }

   public void setCount(long count) {
      if (this.getCount() != count) {
         this._count = count >= -1L ? count : 0L;
         this._storedInDb = false;
      }
   }

   public long getCount() {
      return this._count;
   }

   public void changeCount(String process, long count, Player creator, Object reference) {
      if (count != 0L) {
         long old = this.getCount();
         long max = this.getId() == 57 ? PcInventory.MAX_ADENA : 2147483647L;
         if (count > 0L && this.getCount() > max - count) {
            this.setCount(max);
         } else {
            this.setCount(this.getCount() + count);
         }

         if (this.getCount() < 0L) {
            this.setCount(0L);
         }

         this._storedInDb = false;
         if (Config.LOG_ITEMS
            && process != null
            && (!Config.LOG_ITEMS_SMALL_LOG || Config.LOG_ITEMS_SMALL_LOG && (this._item.isEquipable() || this._item.getId() == 57))) {
            LogRecord record = new LogRecord(Level.INFO, "CHANGE:" + process);
            record.setLoggerName("item");
            record.setParameters(new Object[]{this, "PrevCount(" + old + ")", creator, reference});
            _logItems.log(record);
         }

         if (creator != null && creator.isGM()) {
            String referenceName = "no-reference";
            if (reference instanceof GameObject) {
               referenceName = ((GameObject)reference).getName() != null ? ((GameObject)reference).getName() : "no-name";
            } else if (reference instanceof String) {
               referenceName = (String)reference;
            }

            String targetName = creator.getTarget() != null ? creator.getTarget().getName() : "no-target";
            if (Config.GMAUDIT) {
               GMAudit.auditGMAction(
                  creator.getName() + " [" + creator.getObjectId() + "]",
                  process + "(id: " + this.getId() + " objId: " + this.getObjectId() + " name: " + this.getName() + " count: " + count + ")",
                  targetName,
                  "GameObject referencing this action is: " + referenceName
               );
            }
         }
      }
   }

   public void changeCountWithoutTrace(int count, Player creator, Object reference) {
      this.changeCount(null, (long)count, creator, reference);
   }

   public int isEnchantable() {
      return this.getItemLocation() != ItemInstance.ItemLocation.INVENTORY && this.getItemLocation() != ItemInstance.ItemLocation.PAPERDOLL
         ? 0
         : this.getItem().isEnchantable();
   }

   public boolean isEquipable() {
      return this._item.getBodyPart() != 0
         && this._item.getItemType() != EtcItemType.ARROW
         && this._item.getItemType() != EtcItemType.BOLT
         && this._item.getItemType() != EtcItemType.LURE;
   }

   public boolean isEquipped() {
      return this._loc == ItemInstance.ItemLocation.PAPERDOLL || this._loc == ItemInstance.ItemLocation.PET_EQUIP;
   }

   public int getLocationSlot() {
      assert this._loc == ItemInstance.ItemLocation.PAPERDOLL
         || this._loc == ItemInstance.ItemLocation.PET_EQUIP
         || this._loc == ItemInstance.ItemLocation.INVENTORY
         || this._loc == ItemInstance.ItemLocation.MAIL
         || this._loc == ItemInstance.ItemLocation.FREIGHT;

      return this._locData;
   }

   public Item getItem() {
      return this._item;
   }

   public int getCustomType1() {
      return this._type1;
   }

   public int getCustomType2() {
      return this._type2;
   }

   public void setCustomType1(int newtype) {
      this._type1 = newtype;
   }

   public void setCustomType2(int newtype) {
      this._type2 = newtype;
   }

   public void setDropTime(long time) {
      this._dropTime = time;
   }

   public long getDropTime() {
      return this._dropTime;
   }

   public ItemType getItemType() {
      return this._item.getItemType();
   }

   public boolean isWear() {
      return this._wear;
   }

   public void setItemId(int id) {
      this._itemId = id;
      this._item = ItemsParser.getInstance().getTemplate(id);
   }

   @Override
   public int getId() {
      return this._itemId;
   }

   public int getDisplayId() {
      return this.getItem().getDisplayId();
   }

   public boolean isEtcItem() {
      return this._item instanceof EtcItem;
   }

   public boolean isWeapon() {
      return this._item instanceof Weapon;
   }

   public boolean isArmor() {
      return this._item instanceof Armor;
   }

   public boolean isJewel() {
      return this._item.isJewel();
   }

   public EtcItem getEtcItem() {
      return this._item instanceof EtcItem ? (EtcItem)this._item : null;
   }

   public Weapon getWeaponItem() {
      return this._item instanceof Weapon ? (Weapon)this._item : null;
   }

   public Armor getArmorItem() {
      return this._item instanceof Armor ? (Armor)this._item : null;
   }

   public final int getCrystalCount() {
      return this._item.getCrystalCount(this._enchantLevel);
   }

   public int getReferencePrice() {
      return this._item.getReferencePrice();
   }

   public int getReuseDelay() {
      return this._item.getReuseDelay();
   }

   public boolean isReuseByCron() {
      return this._item.isReuseByCron();
   }

   public int getSharedReuseGroup() {
      return this._item.getSharedReuseGroup();
   }

   public int getLastChange() {
      return this._lastChange;
   }

   public void setLastChange(int lastChange) {
      this._lastChange = lastChange;
   }

   public boolean isStackable() {
      return this._item.isStackable();
   }

   public boolean isDropable() {
      return this.isAugmented() ? Config.ALLOW_DROP_AUGMENT_ITEMS : (this.isTimeLimitedItem() ? false : this._item.isDropable());
   }

   public boolean isDestroyable() {
      return this._item.isDestroyable();
   }

   public boolean isTradeable() {
      return this.isAugmented() ? Config.ALLOW_TRADE_AUGMENT_ITEMS : (this.isTimeLimitedItem() ? false : this._item.isTradeable());
   }

   public boolean isSellable() {
      return this.isAugmented() ? Config.ALLOW_SELL_AUGMENT_ITEMS : (this.isTimeLimitedItem() ? false : this._item.isSellable());
   }

   public boolean isDepositable(boolean isPrivateWareHouse) {
      if (!this.isEquipped() && this._item.isDepositable()) {
         return isPrivateWareHouse || this.isTradeable() && !this.isShadowItem();
      } else {
         return false;
      }
   }

   public boolean isConsumable() {
      return this._item.isConsumable();
   }

   public boolean isPotion() {
      return this._item.isPotion();
   }

   public boolean isElixir() {
      return this._item.isElixir();
   }

   public boolean isHeroItem() {
      return this._item.isHeroItem();
   }

   public boolean isCommonItem() {
      return this._item.isCommon();
   }

   public boolean isPvp() {
      return this._item.isPvpItem();
   }

   public boolean isOlyRestrictedItem() {
      return this.getItem().isOlyRestrictedItem();
   }

   public boolean isEventRestrictedItem() {
      return this.getItem().isEventRestrictedItem();
   }

   public boolean isAvailable(Player player, boolean allowAdena, boolean allowNonTradeable) {
      return !this.isEquipped()
         && this.getItem().getType2() != 3
         && (this.getItem().getType2() != 4 || this.getItem().getType1() != 1)
         && (!player.hasSummon() || this.getObjectId() != player.getSummon().getControlObjectId())
         && player.getActiveEnchantItemId() != this.getObjectId()
         && player.getActiveEnchantSupportItemId() != this.getObjectId()
         && player.getActiveEnchantAttrItemId() != this.getObjectId()
         && (allowAdena || this.getId() != 57)
         && (player.getCurrentSkill() == null || player.getCurrentSkill().getSkill().getItemConsumeId() != this.getId())
         && (!player.isCastingSimultaneouslyNow() || player.getCastingSkill() == null || player.getCastingSkill().getItemConsumeId() != this.getId())
         && (allowNonTradeable || this.isTradeable() && (this.getItem().getItemType() != EtcItemType.PET_COLLAR || !player.havePetInvItems()));
   }

   public int getEnchantLevel() {
      return this._enchantLevel;
   }

   public void setEnchantLevel(int enchantLevel) {
      if (this._enchantLevel != enchantLevel) {
         this.clearEnchantStats();
         this._enchantLevel = enchantLevel;
         this.applyEnchantStats();
         this._storedInDb = false;
      }
   }

   public boolean isAugmented() {
      return this._augmentation != null;
   }

   public Augmentation getAugmentation() {
      return this._augmentation;
   }

   public boolean setAugmentation(Augmentation augmentation) {
      if (this._augmentation != null) {
         _log.info("Warning: Augment set for (" + this.getObjectId() + ") " + this.getName() + " owner: " + this.getOwnerId());
         return false;
      } else if (!this.fireAugmentListeners(true, augmentation)) {
         return false;
      } else {
         this._augmentation = augmentation;

         try (Connection con = DatabaseFactory.getInstance().getConnection()) {
            this.updateItemAttributes(con);
         } catch (SQLException var15) {
            if (Config.DEBUG) {
               _log.log(Level.SEVERE, "Could not update atributes for item: " + this + " from DB:", (Throwable)var15);
            }
         }

         return true;
      }
   }

   public void removeAugmentation() {
      if (this._augmentation != null) {
         if (this.fireAugmentListeners(true, this._augmentation)) {
            this._augmentation = null;

            try (
               Connection con = DatabaseFactory.getInstance().getConnection();
               PreparedStatement statement = con.prepareStatement("DELETE FROM item_attributes WHERE itemId = ?");
            ) {
               statement.setInt(1, this.getObjectId());
               statement.executeUpdate();
            } catch (Exception var33) {
               if (Config.DEBUG) {
                  _log.log(Level.SEVERE, "Could not remove augmentation for item: " + this + " from DB:", (Throwable)var33);
               }
            }
         }
      }
   }

   public void restoreAttributes() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement ps1 = con.prepareStatement("SELECT augAttributes FROM item_attributes WHERE itemId=?");
         PreparedStatement ps2 = con.prepareStatement("SELECT elemType,elemValue FROM item_elementals WHERE itemId=?");
      ) {
         ps1.setInt(1, this.getObjectId());

         try (ResultSet rs = ps1.executeQuery()) {
            if (rs.next()) {
               int aug_attributes = rs.getInt(1);
               if (aug_attributes != -1) {
                  this._augmentation = new Augmentation(rs.getInt("augAttributes"));
               }
            }
         }

         ps2.setInt(1, this.getObjectId());

         try (ResultSet rs = ps2.executeQuery()) {
            while(rs.next()) {
               byte elem_type = rs.getByte(1);
               int elem_value = rs.getInt(2);
               if (elem_type != -1 && elem_value != -1) {
                  this.applyAttribute(elem_type, elem_value);
               }
            }
         }
      } catch (Exception var130) {
         if (Config.DEBUG) {
            _log.log(
               Level.SEVERE, "Could not restore augmentation and elemental data for item " + this + " from DB: " + var130.getMessage(), (Throwable)var130
            );
         }
      }
   }

   private void updateItemAttributes(Connection con) {
      try (PreparedStatement ps = con.prepareStatement("REPLACE INTO item_attributes VALUES(?,?)")) {
         ps.setInt(1, this.getObjectId());
         ps.setInt(2, this._augmentation != null ? this._augmentation.getAttributes() : -1);
         ps.executeUpdate();
      } catch (SQLException var15) {
         if (Config.DEBUG) {
            _log.log(Level.SEVERE, "Could not update atributes for item: " + this + " from DB:", (Throwable)var15);
         }
      }
   }

   private void updateItemElements(Connection con) {
      try (PreparedStatement ps = con.prepareStatement("DELETE FROM item_elementals WHERE itemId = ?")) {
         ps.setInt(1, this.getObjectId());
         ps.executeUpdate();
      } catch (Exception var36) {
      }

      if (this._elementals != null) {
         try (PreparedStatement ps = con.prepareStatement("INSERT INTO item_elementals VALUES(?,?,?)")) {
            for(Elementals elm : this._elementals) {
               ps.setInt(1, this.getObjectId());
               ps.setByte(2, elm.getElement());
               ps.setInt(3, elm.getValue());
               ps.execute();
               ps.clearParameters();
            }
         } catch (Exception var34) {
         }
      }
   }

   public Elementals[] getElementals() {
      return this._elementals;
   }

   public Elementals getElemental(byte attribute) {
      if (this._elementals == null) {
         return null;
      } else {
         for(Elementals elm : this._elementals) {
            if (elm.getElement() == attribute) {
               return elm;
            }
         }

         return null;
      }
   }

   public int getAttributeElementValue(byte attribute) {
      Elementals element = this.getElemental(attribute);
      return element == null ? 0 : element.getValue();
   }

   public byte getAttackElementType() {
      if (!this.isWeapon()) {
         return -2;
      } else if (this.getItem().getElementals() != null) {
         return this.getItem().getElementals()[0].getElement();
      } else {
         return this._elementals != null ? this._elementals[0].getElement() : -2;
      }
   }

   public int getAttackElementPower() {
      if (!this.isWeapon()) {
         return 0;
      } else if (this.getItem().getElementals() != null) {
         return this.getItem().getElementals()[0].getValue();
      } else {
         return this._elementals != null ? this._elementals[0].getValue() : 0;
      }
   }

   public int getElementDefAttr(byte element) {
      if (!this.isArmor()) {
         return 0;
      } else {
         if (this.getItem().getElementals() != null) {
            Elementals elm = this.getItem().getElemental(element);
            if (elm != null) {
               return elm.getValue();
            }
         } else if (this._elementals != null) {
            Elementals elm = this.getElemental(element);
            if (elm != null) {
               return elm.getValue();
            }
         }

         return 0;
      }
   }

   private void applyAttribute(byte element, int value) {
      if (this._elementals == null) {
         this._elementals = new Elementals[1];
         this._elementals[0] = new Elementals(element, value);
      } else {
         Elementals elm = this.getElemental(element);
         if (elm != null) {
            elm.setValue(value);
         } else {
            elm = new Elementals(element, value);
            Elementals[] array = new Elementals[this._elementals.length + 1];
            System.arraycopy(this._elementals, 0, array, 0, this._elementals.length);
            array[this._elementals.length] = elm;
            this._elementals = array;
         }
      }
   }

   public void setElementAttr(byte element, int value) {
      this.applyAttribute(element, value);

      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         this.updateItemElements(con);
      } catch (SQLException var16) {
      }
   }

   public void clearElementAttr(byte element) {
      if (this.getElemental(element) != null || element == -1) {
         Elementals[] array = null;
         if (element != -1 && this._elementals != null && this._elementals.length > 1) {
            array = new Elementals[this._elementals.length - 1];
            int i = 0;

            for(Elementals elm : this._elementals) {
               if (elm.getElement() != element) {
                  array[i++] = elm;
               }
            }
         }

         this._elementals = array;
         String query = element != -1 ? "DELETE FROM item_elementals WHERE itemId = ? AND elemType = ?" : "DELETE FROM item_elementals WHERE itemId = ?";

         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement(query);
         ) {
            if (element != -1) {
               statement.setInt(2, element);
            }

            statement.setInt(1, this.getObjectId());
            statement.executeUpdate();
         } catch (Exception var36) {
            if (Config.DEBUG) {
               _log.log(Level.SEVERE, "Could not remove elemental enchant for item: " + this + " from DB:", (Throwable)var36);
            }
         }
      }
   }

   public boolean isShadowItem() {
      return this._mana >= 0;
   }

   public int getMana() {
      return this._mana;
   }

   public void decreaseMana(boolean resetConsumingMana) {
      this.decreaseMana(resetConsumingMana, 1);
   }

   public void decreaseMana(boolean resetConsumingMana, int count) {
      if (this.isShadowItem()) {
         if (this._mana - count >= 0) {
            this._mana -= count;
         } else {
            this._mana = 0;
         }

         if (this._storedInDb) {
            this._storedInDb = false;
         }

         if (resetConsumingMana) {
            this.stopManaConsumeTask();
         }

         Player player = this.getActingPlayer();
         if (player != null) {
            switch(this._mana) {
               case 1: {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_1);
                  sm.addItemName(this._item);
                  player.sendPacket(sm);
                  break;
               }
               case 5: {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_5);
                  sm.addItemName(this._item);
                  player.sendPacket(sm);
                  break;
               }
               case 10: {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_10);
                  sm.addItemName(this._item);
                  player.sendPacket(sm);
               }
            }

            if (this._mana == 0) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_0);
               sm.addItemName(this._item);
               player.sendPacket(sm);
               if (this.isEquipped()) {
                  ItemInstance[] unequiped = player.getInventory().unEquipItemInSlotAndRecord(this.getLocationSlot());
                  InventoryUpdate iu = new InventoryUpdate();

                  for(ItemInstance item : unequiped) {
                     item.unChargeAllShots();
                     iu.addModifiedItem(item);
                  }

                  player.sendPacket(iu);
                  player.broadcastUserInfo(true);
               }

               if (this.getItemLocation() != ItemInstance.ItemLocation.WAREHOUSE) {
                  player.getInventory().destroyItem("ItemInstance", this, player, null);
                  InventoryUpdate iu = new InventoryUpdate();
                  iu.addRemovedItem(this);
                  player.sendPacket(iu);
                  StatusUpdate su = new StatusUpdate(player);
                  su.addAttribute(14, player.getCurrentLoad());
                  player.sendPacket(su);
               } else {
                  player.getWarehouse().destroyItem("ItemInstance", this, player, null);
               }

               World.getInstance().removeObject(this);
            } else {
               if (this._consumingMana == null && this.isEquipped()) {
                  this.scheduleConsumeManaTask();
               }

               if (this.getItemLocation() != ItemInstance.ItemLocation.WAREHOUSE) {
                  InventoryUpdate iu = new InventoryUpdate();
                  iu.addModifiedItem(this);
                  player.sendPacket(iu);
               }
            }
         }
      }
   }

   public void scheduleConsumeManaTask() {
      if (this._consumingMana == null) {
         ThreadPoolManager.getInstance().schedule(new ItemInstance.ScheduleConsumeManaTask(this), 60000L);
      }
   }

   public void stopManaConsumeTask() {
      if (this._consumingMana != null) {
         this._consumingMana.cancel(true);
         this._consumingMana = null;
      }
   }

   @Override
   public boolean isAutoAttackable(Creature attacker) {
      return false;
   }

   public Func[] getStatFuncs(Creature player) {
      return this.getItem().getStatFuncs(this, player);
   }

   public void updateDatabase() {
      this.updateDatabase(false);
   }

   public void updateDatabase(boolean force) {
      this._dbLock.lock();

      try {
         if (this._existsInDb) {
            if (this._ownerId != 0
               && this._loc != ItemInstance.ItemLocation.VOID
               && this._loc != ItemInstance.ItemLocation.REFUND
               && (this.getCount() != 0L || this._loc == ItemInstance.ItemLocation.LEASE)) {
               if (!Config.LAZY_ITEMS_UPDATE || force) {
                  this.updateInDb();
               }
            } else {
               this.removeFromDb();
            }

            return;
         }

         if (this._ownerId != 0
            && this._loc != ItemInstance.ItemLocation.VOID
            && this._loc != ItemInstance.ItemLocation.REFUND
            && (this.getCount() != 0L || this._loc == ItemInstance.ItemLocation.LEASE)) {
            this.insertIntoDb();
            return;
         }
      } finally {
         this._dbLock.unlock();
      }
   }

   public static ItemInstance restoreFromDb(int ownerId, ResultSet rs) {
      ItemInstance inst = null;

      int objectId;
      int item_id;
      int loc_data;
      int enchant_level;
      int custom_type1;
      int custom_type2;
      int manaLeft;
      int visual_itemId;
      int agathionEnergy;
      long time;
      long count;
      ItemInstance.ItemLocation loc;
      try {
         objectId = rs.getInt(1);
         item_id = rs.getInt("item_id");
         count = rs.getLong("count");
         loc = ItemInstance.ItemLocation.valueOf(rs.getString("loc"));
         loc_data = rs.getInt("loc_data");
         enchant_level = rs.getInt("enchant_level");
         custom_type1 = rs.getInt("custom_type1");
         custom_type2 = rs.getInt("custom_type2");
         manaLeft = rs.getInt("mana_left");
         time = rs.getLong("time");
         visual_itemId = rs.getInt("visual_itemId");
         agathionEnergy = rs.getInt("agathion_energy");
      } catch (Exception var18) {
         _log.log(Level.SEVERE, "Could not restore an item owned by " + ownerId + " from DB:", (Throwable)var18);
         return null;
      }

      Item item = ItemsParser.getInstance().getTemplate(item_id);
      if (item == null) {
         if (Config.DEBUG) {
            _log.severe("Item item_id=" + item_id + " not known, object_id=" + objectId);
         }

         return null;
      } else {
         inst = new ItemInstance(objectId, item);
         inst._ownerId = ownerId;
         inst.setCount(count);
         inst._enchantLevel = enchant_level;
         inst._type1 = custom_type1;
         inst._type2 = custom_type2;
         inst._loc = loc;
         inst._locData = loc_data;
         inst._existsInDb = true;
         inst._storedInDb = true;
         inst._mana = manaLeft;
         inst._time = time;
         inst._visualItemId = visual_itemId;
         inst._agathionEnergy = agathionEnergy;
         if (inst.isEquipable()) {
            inst.restoreAttributes();
         }

         return inst;
      }
   }

   public final void dropMe(Creature dropper, int x, int y, int z) {
      if (this.fireDropListeners(dropper, new Location(x, y, z))) {
         ThreadPoolManager.getInstance().execute(new ItemInstance.ItemDropTask(this, dropper, x, y, z));
      }
   }

   public final void dropMe(Creature dropper, Location loc) {
      if (this.fireDropListeners(dropper, loc)) {
         ThreadPoolManager.getInstance().execute(new ItemInstance.ItemDropTask(this, dropper, loc.getX(), loc.getY(), loc.getZ()));
      }
   }

   private void updateInDb() {
      assert this._existsInDb;

      if (!this._wear) {
         if (!this._storedInDb) {
            try (
               Connection con = DatabaseFactory.getInstance().getConnection();
               PreparedStatement statement = con.prepareStatement(
                  "UPDATE items SET owner_id=?,item_id=?,count=?,loc=?,loc_data=?,enchant_level=?,custom_type1=?,custom_type2=?,mana_left=?,time=?,visual_itemId=?,agathion_energy=? WHERE object_id = ?"
               );
            ) {
               statement.setInt(1, this._ownerId);
               statement.setInt(2, this.getId());
               statement.setLong(3, this.getCount());
               statement.setString(4, this._loc.name());
               statement.setInt(5, this._locData);
               statement.setInt(6, this.getEnchantLevel());
               statement.setInt(7, this.getCustomType1());
               statement.setInt(8, this.getCustomType2());
               statement.setInt(9, this.getMana());
               statement.setLong(10, this.getTime());
               statement.setInt(11, this.getVisualItemId());
               statement.setInt(12, this.getAgathionEnergy());
               statement.setInt(13, this.getObjectId());
               statement.executeUpdate();
               this._existsInDb = true;
               this._storedInDb = true;
            } catch (Exception var33) {
               if (Config.DEBUG) {
                  _log.log(Level.SEVERE, "Could not update item " + this + " in DB: Reason: " + var33.getMessage(), (Throwable)var33);
               }
            }
         }
      }
   }

   private void insertIntoDb() {
      assert !this._existsInDb && this.getObjectId() != 0;

      if (!this._wear) {
         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement(
               "INSERT INTO items (owner_id,item_id,count,loc,loc_data,enchant_level,object_id,custom_type1,custom_type2,mana_left,time,visual_itemId,agathion_energy) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)"
            );
         ) {
            statement.setInt(1, this._ownerId);
            statement.setInt(2, this._itemId);
            statement.setLong(3, this.getCount());
            statement.setString(4, this._loc.name());
            statement.setInt(5, this._locData);
            statement.setInt(6, this.getEnchantLevel());
            statement.setInt(7, this.getObjectId());
            statement.setInt(8, this._type1);
            statement.setInt(9, this._type2);
            statement.setInt(10, this.getMana());
            statement.setLong(11, this.getTime());
            statement.setInt(12, this.getVisualItemId());
            statement.setInt(13, this.getAgathionEnergy());
            statement.executeUpdate();
            this._existsInDb = true;
            this._storedInDb = true;
            if (this._augmentation != null) {
               this.updateItemAttributes(con);
            }

            if (this._elementals != null) {
               this.updateItemElements(con);
            }
         } catch (Exception var33) {
            if (Config.DEBUG) {
               _log.log(Level.SEVERE, "Could not insert item " + this + " into DB: Reason: " + var33.getMessage(), (Throwable)var33);
            }
         }
      }
   }

   private void removeFromDb() {
      assert this._existsInDb;

      if (!this._wear) {
         try (Connection con = DatabaseFactory.getInstance().getConnection()) {
            PreparedStatement statement = con.prepareStatement("DELETE FROM items WHERE object_id = ?");
            statement.setInt(1, this.getObjectId());
            statement.executeUpdate();
            this._existsInDb = false;
            this._storedInDb = false;
            statement.close();
            statement = con.prepareStatement("DELETE FROM item_attributes WHERE itemId = ?");
            statement.setInt(1, this.getObjectId());
            statement.executeUpdate();
            statement.close();
            statement = con.prepareStatement("DELETE FROM item_elementals WHERE itemId = ?");
            statement.setInt(1, this.getObjectId());
            statement.executeUpdate();
            statement.close();
         } catch (Exception var14) {
            _log.log(Level.SEVERE, "Could not delete item " + this + " in DB: " + var14.getMessage(), (Throwable)var14);
         }
      }
   }

   @Override
   public String toString() {
      return this._item + "[" + this.getObjectId() + "]";
   }

   public void resetOwnerTimer() {
      if (this.itemLootShedule != null) {
         this.itemLootShedule.cancel(true);
      }

      this.itemLootShedule = null;
   }

   public void setItemLootShedule(ScheduledFuture<?> sf) {
      this.itemLootShedule = sf;
   }

   public ScheduledFuture<?> getItemLootShedule() {
      return this.itemLootShedule;
   }

   public void setProtected(boolean isProtected) {
      this._protected = isProtected;
   }

   public boolean isProtected() {
      return this._protected;
   }

   public boolean isNightLure() {
      return this._itemId >= 8505 && this._itemId <= 8513 || this._itemId == 8485;
   }

   public void setCountDecrease(boolean decrease) {
      this._decrease = decrease;
   }

   public boolean getCountDecrease() {
      return this._decrease;
   }

   public void setInitCount(int InitCount) {
      this._initCount = (long)InitCount;
   }

   public long getInitCount() {
      return this._initCount;
   }

   public void restoreInitCount() {
      if (this._decrease) {
         this.setCount(this._initCount);
      }
   }

   public boolean isTimeLimitedItem() {
      return this._time > 0L;
   }

   public long getTime() {
      return this._time;
   }

   public void setTime(long time) {
      this._time = System.currentTimeMillis() + time * 60L * 1000L;
      this.scheduleLifeTimeTask();
   }

   public long getRemainingTime() {
      return this._time - System.currentTimeMillis();
   }

   public void endOfLife() {
      Player player = this.getActingPlayer();
      if (player != null) {
         if (this.isEquipped()) {
            ItemInstance[] unequiped = player.getInventory().unEquipItemInSlotAndRecord(this.getLocationSlot());
            InventoryUpdate iu = new InventoryUpdate();

            for(ItemInstance item : unequiped) {
               item.unChargeAllShots();
               iu.addModifiedItem(item);
            }

            player.sendPacket(iu);
            player.broadcastUserInfo(true);
         }

         if (this.getItemLocation() != ItemInstance.ItemLocation.WAREHOUSE) {
            player.getInventory().destroyItem("ItemInstance", this, player, null);
            InventoryUpdate iu = new InventoryUpdate();
            iu.addRemovedItem(this);
            player.sendPacket(iu);
            StatusUpdate su = new StatusUpdate(player);
            su.addAttribute(14, player.getCurrentLoad());
            player.sendPacket(su);
         } else {
            player.getWarehouse().destroyItem("ItemInstance", this, player, null);
         }

         player.sendPacket(SystemMessageId.TIME_LIMITED_ITEM_DELETED);
         World.getInstance().removeObject(this);
      }
   }

   public void scheduleLifeTimeTask() {
      if (this.isTimeLimitedItem()) {
         if (this.getRemainingTime() <= 0L) {
            this.endOfLife();
         } else {
            if (this._lifeTimeTask != null) {
               this._lifeTimeTask.cancel(false);
            }

            this._lifeTimeTask = ThreadPoolManager.getInstance().schedule(new ItemInstance.ScheduleLifeTimeTask(this), this.getRemainingTime());
         }
      }
   }

   public void updateElementAttrBonus(Player player) {
      if (this._elementals != null) {
         for(Elementals elm : this._elementals) {
            elm.updateBonus(player, this.isArmor());
         }
      }
   }

   public void removeElementAttrBonus(Player player) {
      if (this._elementals != null) {
         for(Elementals elm : this._elementals) {
            elm.removeBonus(player);
         }
      }
   }

   public void setDropperObjectId(int id) {
      this._dropperObjectId = id;
   }

   @Override
   public void sendInfo(Player activeChar) {
      if (this._dropperObjectId != 0) {
         activeChar.sendPacket(new DropItem(this, this._dropperObjectId));
      } else {
         activeChar.sendPacket(new SpawnItem(this));
      }
   }

   public final DropProtection getDropProtection() {
      return this._dropProtection;
   }

   public boolean isPublished() {
      return this._published;
   }

   public void publish() {
      this._published = true;
   }

   @Override
   public void decayMe() {
      if (Config.SAVE_DROPPED_ITEM) {
         ItemsOnGroundManager.getInstance().removeObject(this);
      }

      super.decayMe();
   }

   public boolean isQuestItem() {
      return this.getItem().isQuestItem();
   }

   public boolean isElementable() {
      return this.getItemLocation() != ItemInstance.ItemLocation.INVENTORY && this.getItemLocation() != ItemInstance.ItemLocation.PAPERDOLL
         ? false
         : this.getItem().isElementable();
   }

   public boolean isFreightable() {
      return this.getItem().isFreightable();
   }

   public int useSkillDisTime() {
      return this.getItem().useSkillDisTime();
   }

   public int getOlyEnchantLevel() {
      Player player = this.getActingPlayer();
      int enchant = this.getEnchantLevel();
      if (player == null) {
         return enchant;
      } else {
         if (player.isInOlympiadMode() && Config.ALT_OLY_ENCHANT_LIMIT >= 0 && enchant > Config.ALT_OLY_ENCHANT_LIMIT) {
            enchant = Config.ALT_OLY_ENCHANT_LIMIT;
         }

         return enchant;
      }
   }

   public int getDefaultEnchantLevel() {
      return this._item.getDefaultEnchantLevel();
   }

   public boolean hasPassiveSkills() {
      return this.getItemType() == EtcItemType.RUNE
         && this.getItemLocation() == ItemInstance.ItemLocation.INVENTORY
         && this.getOwnerId() > 0
         && this.getItem().hasSkills();
   }

   public void giveSkillsToOwner() {
      if (this.hasPassiveSkills()) {
         Player player = this.getActingPlayer();
         if (player != null) {
            for(SkillHolder sh : this.getItem().getSkills()) {
               if (sh.getSkill().isPassive()) {
                  Skill skill = player.getKnownSkill(sh.getSkill().getId());
                  if (skill != null) {
                     if (skill.getLevel() < sh.getSkill().getLevel()) {
                        player.addSkill(sh.getSkill(), false);
                     }
                  } else {
                     player.addSkill(sh.getSkill(), false);
                  }
               }
            }

            player.sendSkillList(false);
         }
      }
   }

   public void removeSkillsFromOwner() {
      if (this.hasPassiveSkills()) {
         Player player = this.getActingPlayer();
         if (player != null) {
            for(SkillHolder sh : this.getItem().getSkills()) {
               if (sh.getSkill().isPassive()) {
                  player.removeSkill(sh.getSkill(), false, true);
               }
            }

            player.getInventory().checkRuneSkills();
            player.sendSkillList(false);
         }
      }
   }

   @Override
   public boolean isItem() {
      return true;
   }

   @Override
   public Player getActingPlayer() {
      return World.getInstance().getPlayer(this.getOwnerId());
   }

   public int getEquipReuseDelay() {
      return this._item.getEquipReuseDelay();
   }

   public void onBypassFeedback(Player activeChar, String command) {
      if (command.startsWith("Quest")) {
         String questName = command.substring(6);
         String content = null;
         String event = null;
         int idx = questName.indexOf(32);
         if (idx > 0) {
            event = questName.substring(idx).trim();
            questName = questName.substring(0, idx);
         }

         Quest q = QuestManager.getInstance().getQuest(questName);
         QuestState qs = activeChar.getQuestState(questName);
         if (q != null) {
            if (q.getId() >= 1 && q.getId() < 20000 && (activeChar.getWeightPenalty() >= 3 || !activeChar.isInventoryUnder90(true))) {
               activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT));
               return;
            }

            if (qs == null) {
               if (q.getId() >= 1 && q.getId() < 20000 && activeChar.getAllActiveQuests().length > 40) {
                  activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TOO_MANY_QUESTS));
                  return;
               }

               qs = q.newQuestState(activeChar);
            }
         } else {
            content = Quest.getNoQuestMsg(activeChar);
         }

         if (qs != null) {
            if (event != null && !qs.getQuest().notifyItemEvent(this, activeChar, event)) {
               return;
            }

            if (!qs.getQuest().notifyItemTalk(this, activeChar)) {
               return;
            }

            questName = qs.getQuest().getName();
            String stateId = State.getStateName(qs.getState());
            String path = "data/scripts/quests/" + questName + "/" + stateId + ".htm";
            content = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), path);
         }

         if (content != null) {
            this.showChatWindow(activeChar, content);
         }

         activeChar.sendActionFailed();
      }
   }

   public void showChatWindow(Player activeChar, String content) {
      NpcHtmlMessage html = new NpcHtmlMessage(0, this.getId());
      html.setHtml(activeChar, content);
      html.replace("%itemId%", String.valueOf(this.getObjectId()));
      activeChar.sendPacket(html);
   }

   @Override
   public boolean isChargedShot(ShotType type) {
      return (this._shotsMask & type.getMask()) == type.getMask();
   }

   @Override
   public void setChargedShot(ShotType type, boolean charged) {
      if (charged) {
         this._shotsMask |= type.getMask();
      } else {
         this._shotsMask &= ~type.getMask();
      }
   }

   public void unChargeAllShots() {
      this._shotsMask = 0;
   }

   public int[] getEnchantOptions() {
      EnchantOptions op = EnchantItemOptionsParser.getInstance().getOptions(this);
      return op != null ? op.getOptions() : DEFAULT_ENCHANT_OPTIONS;
   }

   public void clearEnchantStats() {
      Player player = this.getActingPlayer();
      if (player == null) {
         this._enchantOptions.clear();
      } else {
         for(Options op : this._enchantOptions) {
            op.remove(player);
         }

         this._enchantOptions.clear();
      }
   }

   public void applyEnchantStats() {
      Player player = this.getActingPlayer();
      if (this.isEquipped() && player != null && this.getEnchantOptions() != DEFAULT_ENCHANT_OPTIONS) {
         for(int id : this.getEnchantOptions()) {
            Options options = OptionsParser.getInstance().getOptions(id);
            if (options != null) {
               options.apply(player);
               this._enchantOptions.add(options);
            } else if (id != 0) {
               _log.log(Level.INFO, "applyEnchantStats: Couldn't find option: " + id);
            }
         }
      }
   }

   private boolean firePickupListeners(Player actor) {
      if (!dropListeners.isEmpty() && actor != null) {
         ItemPickupEvent event = new ItemPickupEvent();
         event.setItem(this);
         event.setPicker(actor);
         event.setLocation(new Location(this.getX(), this.getY(), this.getZ()));

         for(DropListener listener : dropListeners) {
            if (!listener.onPickup(event)) {
               return false;
            }
         }
      }

      return true;
   }

   private boolean fireAugmentListeners(boolean isAugment, Augmentation augmentation) {
      if (!augmentListeners.isEmpty() && augmentation != null) {
         AugmentEvent event = new AugmentEvent();
         event.setAugmentation(augmentation);
         event.setIsAugment(isAugment);
         event.setItem(this);

         for(AugmentListener listener : augmentListeners) {
            if (isAugment) {
               if (!listener.onAugment(event)) {
                  return false;
               }
            } else if (!listener.onRemoveAugment(event)) {
               return false;
            }
         }
      }

      return true;
   }

   private boolean fireDropListeners(Creature dropper, Location loc) {
      if (!dropListeners.isEmpty() && dropper != null) {
         ItemDropEvent event = new ItemDropEvent();
         event.setDropper(dropper);
         event.setItem(this);
         event.setLocation(loc);

         for(DropListener listener : dropListeners) {
            if (!listener.onDrop(event)) {
               return false;
            }
         }
      }

      return true;
   }

   public static void addAugmentListener(AugmentListener listener) {
      if (!augmentListeners.contains(listener)) {
         augmentListeners.add(listener);
      }
   }

   public static void removeAugmentListener(AugmentListener listener) {
      augmentListeners.remove(listener);
   }

   public static void addDropListener(DropListener listener) {
      if (!dropListeners.contains(listener)) {
         dropListeners.add(listener);
      }
   }

   public static void removeDropListener(DropListener listener) {
      dropListeners.remove(listener);
   }

   public void deleteMe() {
      if (this._lifeTimeTask != null && !this._lifeTimeTask.isDone()) {
         this._lifeTimeTask.cancel(false);
         this._lifeTimeTask = null;
      }
   }

   public void setMana(int value) {
      this._mana = value;
   }

   public int getVisualItemId() {
      return this._visualItemId;
   }

   public void setVisualItemId(int visualItemId) {
      this._visualItemId = visualItemId;
   }

   public boolean isEnergyItem() {
      return this._agathionEnergy >= 0;
   }

   public int getAgathionEnergy() {
      return this._agathionEnergy;
   }

   public void setAgathionEnergy(int agathionEnergy) {
      this._agathionEnergy = agathionEnergy;
   }

   public void decreaseEnergy(boolean resetConsumingEnergy) {
      this.decreaseEnergy(resetConsumingEnergy, 1);
   }

   public void decreaseEnergy(boolean resetConsumingEnergy, int count) {
      if (this.isEnergyItem()) {
         if (this._agathionEnergy - count >= 0) {
            this._agathionEnergy -= count;
         } else {
            this._agathionEnergy = 0;
         }

         if (this._storedInDb) {
            this._storedInDb = false;
         }

         if (resetConsumingEnergy) {
            this._consumingEnergy = false;
         }

         Player player = this.getActingPlayer();
         if (player != null) {
            if (this._agathionEnergy == 0) {
               if (this.isEquipped() && player.getAgathionId() > 0) {
                  player.setAgathionId(0);
                  player.broadcastUserInfo(true);
               }
            } else {
               if (!this._consumingEnergy && this.isEquipped()) {
                  this.scheduleConsumeEnergyTask();
               }

               if (this.getItemLocation() != ItemInstance.ItemLocation.WAREHOUSE) {
                  InventoryUpdate iu = new InventoryUpdate();
                  iu.addModifiedItem(this);
                  player.sendPacket(iu);
               }
            }
         }
      }
   }

   public void scheduleConsumeEnergyTask() {
      if (!this._consumingEnergy) {
         this._consumingEnergy = true;
         ThreadPoolManager.getInstance().schedule(new ItemInstance.ScheduleConsumeEnergyTask(this), 60000L);
      }
   }

   public class ItemDropTask implements Runnable {
      private int _x;
      private int _y;
      private int _z;
      private final Creature _dropper;
      private final ItemInstance _itm;

      public ItemDropTask(ItemInstance item, Creature dropper, int x, int y, int z) {
         this._x = x;
         this._y = y;
         this._z = Config.GEODATA ? GeoEngine.getHeight(new Location(x, y, z), ItemInstance.this.getGeoIndex()) : z;
         this._dropper = dropper;
         this._itm = item;
      }

      @Override
      public final void run() {
         assert this._itm.getWorldRegion() == null;

         if (Config.GEODATA
            && this._dropper != null
            && !GeoEngine.canMoveToCoord(
               this._dropper.getX(), this._dropper.getY(), this._dropper.getZ(), this._x, this._y, this._z, this._dropper.getGeoIndex()
            )) {
            this._x = this._dropper.getX();
            this._y = this._dropper.getY();
            this._z = this._dropper.getZ();
         }

         ItemInstance.this.setReflectionId(this._dropper != null ? this._dropper.getReflectionId() : 0);
         this._itm.setDropTime(System.currentTimeMillis());
         this._itm.setDropperObjectId(this._dropper != null ? this._dropper.getObjectId() : 0);
         this._itm.spawnMe(this._x, this._y, this._z);
         if (this._itm.getReflectionId() > 0 && ReflectionManager.getInstance().getReflection(ItemInstance.this.getReflectionId()) != null) {
            ReflectionManager.getInstance().getReflection(ItemInstance.this.getReflectionId()).addItem(this._itm);
         }

         if (Config.SAVE_DROPPED_ITEM) {
            ItemsOnGroundManager.getInstance().save(this._itm);
         }

         this._itm.setDropperObjectId(0);
      }
   }

   public static enum ItemLocation {
      VOID,
      INVENTORY,
      PAPERDOLL,
      WAREHOUSE,
      CLANWH,
      PET,
      PET_EQUIP,
      LEASE,
      REFUND,
      MAIL,
      FREIGHT,
      AUCTION;
   }

   public static class ScheduleConsumeEnergyTask implements Runnable {
      private final ItemInstance _energyItem;

      public ScheduleConsumeEnergyTask(ItemInstance item) {
         this._energyItem = item;
      }

      @Override
      public void run() {
         try {
            if (this._energyItem != null) {
               this._energyItem.decreaseEnergy(true);
            }
         } catch (Exception var2) {
            ItemInstance._log.log(Level.SEVERE, "", (Throwable)var2);
         }
      }
   }

   public static class ScheduleConsumeManaTask implements Runnable {
      private final ItemInstance _shadowItem;

      public ScheduleConsumeManaTask(ItemInstance item) {
         this._shadowItem = item;
      }

      @Override
      public void run() {
         try {
            if (this._shadowItem != null) {
               if (!this._shadowItem.isEquipped()) {
                  this._shadowItem.stopManaConsumeTask();
                  return;
               }

               this._shadowItem.decreaseMana(true);
            }
         } catch (Exception var2) {
            ItemInstance._log.log(Level.SEVERE, "", (Throwable)var2);
         }
      }
   }

   public static class ScheduleLifeTimeTask implements Runnable {
      private final ItemInstance _limitedItem;

      public ScheduleLifeTimeTask(ItemInstance item) {
         this._limitedItem = item;
      }

      @Override
      public void run() {
         try {
            if (this._limitedItem != null) {
               this._limitedItem.endOfLife();
            }
         } catch (Exception var2) {
            ItemInstance._log.log(Level.SEVERE, "", (Throwable)var2);
         }
      }
   }
}
