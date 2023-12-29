package l2e.gameserver.data.parser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import l2e.commons.util.GMAudit;
import l2e.gameserver.Config;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.listener.events.ItemCreateEvent;
import l2e.gameserver.listener.player.NewItemListener;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.Armor;
import l2e.gameserver.model.actor.templates.items.EtcItem;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.actor.templates.items.Weapon;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.type.ArmorType;
import l2e.gameserver.model.items.type.WeaponType;
import l2e.gameserver.model.reward.CalculateRewardChances;
import l2e.gameserver.model.skills.engines.DocumentEngine;
import l2e.gameserver.model.skills.engines.items.ItemTemplate;
import org.apache.commons.lang3.StringUtils;

public class ItemsParser {
   private static Logger _log = Logger.getLogger(ItemsParser.class.getName());
   private static Logger _logItems = Logger.getLogger("item");
   private static List<NewItemListener> newItemListeners = new LinkedList<>();
   public static final Map<String, Integer> _materials = new HashMap<>();
   public static final Map<String, Integer> _crystalTypes = new HashMap<>();
   public static final Map<String, Integer> _slots = new HashMap<>();
   public static final Map<String, WeaponType> _weaponTypes = new HashMap<>();
   public static final Map<String, ArmorType> _armorTypes = new HashMap<>();
   private Item[] _allTemplates;
   private Item[] _droppableTemplates;
   private final Map<Integer, EtcItem> _etcItems = new HashMap<>();
   private final Map<Integer, Armor> _armors = new HashMap<>();
   private final Map<Integer, Weapon> _weapons = new HashMap<>();

   public static ItemsParser getInstance() {
      return ItemsParser.SingletonHolder._instance;
   }

   public ItemTemplate newItem() {
      return new ItemTemplate();
   }

   protected ItemsParser() {
      this.load();
   }

   private void load() {
      int highest = 0;
      this._armors.clear();
      this._etcItems.clear();
      this._weapons.clear();

      for(Item item : DocumentEngine.getInstance().loadItems()) {
         if (highest < item.getId()) {
            highest = item.getId();
         }

         if (item instanceof EtcItem) {
            this._etcItems.put(item.getId(), (EtcItem)item);
         } else if (item instanceof Armor) {
            this._armors.put(item.getId(), (Armor)item);
         } else {
            this._weapons.put(item.getId(), (Weapon)item);
         }
      }

      this.buildFastLookupTable(highest);
      if (Config.DEBUG) {
         _log.log(Level.INFO, this.getClass().getSimpleName() + ": Loaded: " + this._etcItems.size() + " etc items");
         _log.log(Level.INFO, this.getClass().getSimpleName() + ": Loaded: " + this._armors.size() + " armor items");
         _log.log(Level.INFO, this.getClass().getSimpleName() + ": Loaded: " + this._weapons.size() + " weapon items");
      }

      _log.log(
         Level.INFO,
         this.getClass().getSimpleName() + ": Loaded: " + (this._etcItems.size() + this._armors.size() + this._weapons.size()) + " items template."
      );
   }

   private void buildFastLookupTable(int size) {
      if (Config.DEBUG) {
         _log.info(this.getClass().getSimpleName() + ": highest item id used:" + size);
      }

      this._allTemplates = new Item[size + 1];

      for(Armor item : this._armors.values()) {
         this._allTemplates[item.getId()] = item;
      }

      for(Weapon item : this._weapons.values()) {
         this._allTemplates[item.getId()] = item;
      }

      for(EtcItem item : this._etcItems.values()) {
         this._allTemplates[item.getId()] = item;
      }
   }

   public Item getTemplate(int id) {
      return id < this._allTemplates.length && id >= 0 ? this._allTemplates[id] : null;
   }

   public ItemInstance createItem(String process, int itemId, long count, Player actor, Object reference) {
      if (!this.fireNewItemListeners(process, itemId, count, actor, reference)) {
         return null;
      } else {
         ItemInstance item = new ItemInstance(IdFactory.getInstance().getNextId(), itemId);
         if (Config.DEBUG) {
            _log.fine(this.getClass().getSimpleName() + ": Item created  oid:" + item.getObjectId() + " itemid:" + itemId);
         }

         World.getInstance().addObject(item);
         if (item.isStackable() && count > 1L) {
            item.setCount(count);
         }

         if (Config.LOG_ITEMS
            && !process.equals("Reset")
            && (!Config.LOG_ITEMS_SMALL_LOG || Config.LOG_ITEMS_SMALL_LOG && (item.isEquipable() || item.getId() == 57))) {
            LogRecord record = new LogRecord(Level.INFO, "CREATE:" + process);
            record.setLoggerName("item");
            record.setParameters(new Object[]{item, actor, reference});
            _logItems.log(record);
         }

         if (actor != null && actor.isGM()) {
            String referenceName = "no-reference";
            if (reference instanceof GameObject) {
               referenceName = ((GameObject)reference).getName() != null ? ((GameObject)reference).getName() : "no-name";
            } else if (reference instanceof String) {
               referenceName = (String)reference;
            }

            String targetName = actor.getTarget() != null ? actor.getTarget().getName() : "no-target";
            if (Config.GMAUDIT) {
               GMAudit.auditGMAction(
                  actor.getName() + " [" + actor.getObjectId() + "]",
                  process + "(id: " + itemId + " count: " + count + " name: " + item.getItem().getNameEn() + " objId: " + item.getObjectId() + ")",
                  targetName,
                  "GameObject referencing this action is: " + referenceName
               );
            }
         }

         return item;
      }
   }

   public ItemInstance createItem(int itemId) {
      ItemInstance item = new ItemInstance(IdFactory.getInstance().getNextId(), itemId);
      item.setItemLocation(ItemInstance.ItemLocation.VOID);
      item.setCount(1L);
      return item;
   }

   public ItemInstance createItem(String process, int itemId, int count, Player actor) {
      return this.createItem(process, itemId, (long)count, actor, null);
   }

   public ItemInstance createDummyItem(int itemId) {
      Item item = this.getTemplate(itemId);
      return item == null ? null : new ItemInstance(0, item);
   }

   public void destroyItem(String process, ItemInstance item, Player actor, Object reference) {
      synchronized(item) {
         long old = item.getCount();
         item.setCount(0L);
         item.setOwnerId(0);
         item.setItemLocation(ItemInstance.ItemLocation.VOID);
         item.setLastChange(3);
         World.getInstance().removeObject(item);
         IdFactory.getInstance().releaseId(item.getObjectId());
         if (Config.LOG_ITEMS && (!Config.LOG_ITEMS_SMALL_LOG || Config.LOG_ITEMS_SMALL_LOG && (item.isEquipable() || item.getId() == 57))) {
            LogRecord record = new LogRecord(Level.INFO, "DELETE:" + process);
            record.setLoggerName("item");
            record.setParameters(new Object[]{item, "PrevCount(" + old + ")", actor, reference});
            _logItems.log(record);
         }

         if (actor != null && actor.isGM()) {
            String referenceName = "no-reference";
            if (reference instanceof GameObject) {
               referenceName = ((GameObject)reference).getName() != null ? ((GameObject)reference).getName() : "no-name";
            } else if (reference instanceof String) {
               referenceName = (String)reference;
            }

            String targetName = actor.getTarget() != null ? actor.getTarget().getName() : "no-target";
            if (Config.GMAUDIT) {
               GMAudit.auditGMAction(
                  actor.getName() + " [" + actor.getObjectId() + "]",
                  process + "(id: " + item.getId() + " count: " + item.getCount() + " itemObjId: " + item.getObjectId() + ")",
                  targetName,
                  "GameObject referencing this action is: " + referenceName
               );
            }
         }

         if (item.getItem().isPetItem()) {
            try (
               Connection con = DatabaseFactory.getInstance().getConnection();
               PreparedStatement statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?");
            ) {
               statement.setInt(1, item.getObjectId());
               statement.execute();
            } catch (Exception var43) {
               _log.log(Level.WARNING, "could not delete pet objectid:", (Throwable)var43);
            }
         }
      }
   }

   public void reload() {
      this.load();
      EnchantItemHPBonusParser.getInstance().load();
   }

   public Set<Integer> getAllArmorsId() {
      return this._armors.keySet();
   }

   public Set<Integer> getAllWeaponsId() {
      return this._weapons.keySet();
   }

   public int getArraySize() {
      return this._allTemplates.length;
   }

   public List<Item> getItemsByNameContainingString(CharSequence name, boolean onlyDroppable) {
      Item[] toChooseFrom = onlyDroppable ? this.getDroppableTemplates() : this._allTemplates;
      List<Item> templates = new ArrayList<>();

      for(Item template : toChooseFrom) {
         if (template != null && (StringUtils.containsIgnoreCase(template.getNameEn(), name) || StringUtils.containsIgnoreCase(template.getNameRu(), name))) {
            templates.add(template);
         }
      }

      return templates;
   }

   public Item[] getDroppableTemplates() {
      if (this._droppableTemplates == null) {
         List<Item> templates = CalculateRewardChances.getDroppableItems();
         this._droppableTemplates = templates.toArray(new Item[templates.size()]);
      }

      return this._droppableTemplates;
   }

   private boolean fireNewItemListeners(String process, int itemId, long count, Player actor, Object reference) {
      if (!newItemListeners.isEmpty() && actor != null) {
         ItemCreateEvent event = new ItemCreateEvent();
         event.setId(itemId);
         event.setPlayer(actor);
         event.setCount(count);
         event.setProcess(process);
         event.setReference(reference);

         for(NewItemListener listener : newItemListeners) {
            if (listener.containsItemId(itemId) && !listener.onCreate(event)) {
               return false;
            }
         }
      }

      return true;
   }

   public static void addNewItemListener(NewItemListener listener) {
      if (!newItemListeners.contains(listener)) {
         newItemListeners.add(listener);
      }
   }

   public static void removeNewItemListener(NewItemListener listener) {
      newItemListeners.remove(listener);
   }

   static {
      _materials.put("adamantaite", 15);
      _materials.put("blood_steel", 2);
      _materials.put("bone", 12);
      _materials.put("bronze", 3);
      _materials.put("cloth", 10);
      _materials.put("chrysolite", 16);
      _materials.put("cobweb", 21);
      _materials.put("cotton", 1);
      _materials.put("crystal", 17);
      _materials.put("damascus", 14);
      _materials.put("dyestuff", 20);
      _materials.put("fine_steel", 1);
      _materials.put("fish", 23);
      _materials.put("gold", 5);
      _materials.put("horn", 13);
      _materials.put("leather", 11);
      _materials.put("liquid", 18);
      _materials.put("mithril", 6);
      _materials.put("oriharukon", 7);
      _materials.put("paper", 8);
      _materials.put("rune_xp", 24);
      _materials.put("rune_sp", 25);
      _materials.put("rune_remove_penalty", 32);
      _materials.put("scale_of_dragon", 19);
      _materials.put("seed", 22);
      _materials.put("silver", 4);
      _materials.put("steel", 0);
      _materials.put("wood", 9);
      _crystalTypes.put("s84", 7);
      _crystalTypes.put("s80", 6);
      _crystalTypes.put("s", 5);
      _crystalTypes.put("a", 4);
      _crystalTypes.put("b", 3);
      _crystalTypes.put("c", 2);
      _crystalTypes.put("d", 1);
      _crystalTypes.put("none", 0);

      for(WeaponType type : WeaponType.values()) {
         _weaponTypes.put(type.toString(), type);
      }

      for(ArmorType type : ArmorType.values()) {
         _armorTypes.put(type.toString(), type);
      }

      _slots.put("shirt", 1);
      _slots.put("lbracelet", 2097152);
      _slots.put("rbracelet", 1048576);
      _slots.put("talisman", 4194304);
      _slots.put("chest", 1024);
      _slots.put("fullarmor", 32768);
      _slots.put("head", 64);
      _slots.put("hair", 65536);
      _slots.put("hairall", 524288);
      _slots.put("underwear", 1);
      _slots.put("back", 8192);
      _slots.put("neck", 8);
      _slots.put("legs", 2048);
      _slots.put("feet", 4096);
      _slots.put("gloves", 512);
      _slots.put("chest,legs", 3072);
      _slots.put("belt", 268435456);
      _slots.put("rhand", 128);
      _slots.put("lhand", 256);
      _slots.put("lrhand", 16384);
      _slots.put("rear;lear", 6);
      _slots.put("rfinger;lfinger", 48);
      _slots.put("wolf", -100);
      _slots.put("greatwolf", -104);
      _slots.put("hatchling", -101);
      _slots.put("strider", -102);
      _slots.put("babypet", -103);
      _slots.put("none", 0);
      _slots.put("onepiece", 32768);
      _slots.put("hair2", 262144);
      _slots.put("dhair", 524288);
      _slots.put("alldress", 131072);
      _slots.put("deco1", 4194304);
      _slots.put("waist", 268435456);
   }

   protected static class ResetOwner implements Runnable {
      ItemInstance _item;

      public ResetOwner(ItemInstance item) {
         this._item = item;
      }

      @Override
      public void run() {
         this._item.setOwnerId(0);
         this._item.setItemLootShedule(null);
      }
   }

   private static class SingletonHolder {
      protected static final ItemsParser _instance = new ItemsParser();
   }
}
