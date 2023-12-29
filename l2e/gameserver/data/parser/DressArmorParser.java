package l2e.gameserver.data.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.actor.templates.DressArmorTemplate;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.items.type.ArmorType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class DressArmorParser extends DocumentParser {
   protected static Logger _log = Logger.getLogger(DressArmorParser.class.getName());
   private final List<DressArmorTemplate> _dress = new ArrayList<>();

   protected DressArmorParser() {
      this._dress.clear();
      this.load();
   }

   @Override
   public synchronized void load() {
      this.parseDatapackFile("data/stats/services/dress/dressArmor.xml");
      _log.info(this.getClass().getSimpleName() + ": Loaded " + this._dress.size() + " dress armor templates.");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node n = this.getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equalsIgnoreCase(n.getNodeName())) {
            for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
               if ("dress".equalsIgnoreCase(d.getNodeName())) {
                  NamedNodeMap dress = d.getAttributes();
                  int chest = 0;
                  int legs = 0;
                  int gloves = 0;
                  int feet = 0;
                  int shield = 0;
                  int cloak = 0;
                  int hat = 0;
                  int slot = 0;
                  int itemId = 0;
                  long itemCount = 0L;
                  int id = Integer.parseInt(dress.getNamedItem("id").getNodeValue());
                  String name = dress.getNamedItem("name").getNodeValue();
                  boolean checkEquip = dress.getNamedItem("checkEquip") != null ? Boolean.parseBoolean(dress.getNamedItem("checkEquip").getNodeValue()) : true;

                  for(Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
                     dress = cd.getAttributes();
                     if ("set".equalsIgnoreCase(cd.getNodeName())) {
                        chest = Integer.parseInt(dress.getNamedItem("chest").getNodeValue());
                        legs = Integer.parseInt(dress.getNamedItem("legs").getNodeValue());
                        gloves = Integer.parseInt(dress.getNamedItem("gloves").getNodeValue());
                        feet = Integer.parseInt(dress.getNamedItem("feet").getNodeValue());
                        shield = dress.getNamedItem("shield") != null ? Integer.parseInt(dress.getNamedItem("shield").getNodeValue()) : -1;
                        cloak = dress.getNamedItem("cloak") != null ? Integer.parseInt(dress.getNamedItem("cloak").getNodeValue()) : -1;
                        hat = dress.getNamedItem("hat") != null ? Integer.parseInt(dress.getNamedItem("hat").getNodeValue()) : -1;
                        slot = dress.getNamedItem("slot") != null ? Integer.parseInt(dress.getNamedItem("slot").getNodeValue()) : -1;
                     }

                     if ("price".equalsIgnoreCase(cd.getNodeName())) {
                        itemId = Integer.parseInt(dress.getNamedItem("id").getNodeValue());
                        itemCount = Long.parseLong(dress.getNamedItem("count").getNodeValue());
                     }
                  }

                  this.addDress(
                     new DressArmorTemplate(id, name, checkEquip, isForKamael(chest), chest, legs, gloves, feet, shield, cloak, hat, slot, itemId, itemCount)
                  );
               }
            }
         }
      }
   }

   public void addDress(DressArmorTemplate armorset) {
      this._dress.add(armorset);
   }

   public List<DressArmorTemplate> getAllDress() {
      return this._dress;
   }

   public DressArmorTemplate getArmor(int id) {
      for(DressArmorTemplate dress : this._dress) {
         if (dress.getId() == id) {
            return dress;
         }
      }

      return null;
   }

   public DressArmorTemplate getArmorByPartId(int partId) {
      for(DressArmorTemplate dress : this._dress) {
         if (dress.getChest() == partId || dress.getLegs() == partId || dress.getGloves() == partId || dress.getFeet() == partId) {
            return dress;
         }
      }

      return null;
   }

   public int size() {
      return this._dress.size();
   }

   private static boolean isForKamael(int itemId) {
      Item item = ItemsParser.getInstance().getTemplate(itemId);
      if (item != null) {
         if (item.getItemType() != ArmorType.HEAVY && item.getItemType() != ArmorType.MAGIC) {
            return true;
         } else {
            return item.getBodyPart() == 131072;
         }
      } else {
         return false;
      }
   }

   public static DressArmorParser getInstance() {
      return DressArmorParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final DressArmorParser _instance = new DressArmorParser();
   }
}
