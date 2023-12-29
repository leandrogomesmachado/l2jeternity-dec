package l2e.gameserver.data.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.actor.templates.DressWeaponTemplate;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class DressWeaponParser extends DocumentParser {
   protected static Logger _log = Logger.getLogger(DressWeaponParser.class.getName());
   private final List<DressWeaponTemplate> _weapons = new ArrayList<>();

   private DressWeaponParser() {
      this._weapons.clear();
      this.load();
   }

   @Override
   public synchronized void load() {
      this.parseDatapackFile("data/stats/services/dress/dressWeapon.xml");
      _log.info(this.getClass().getSimpleName() + ": Loaded " + this._weapons.size() + " dress weapon templates.");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node n = this.getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equalsIgnoreCase(n.getNodeName())) {
            for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
               if ("weapon".equalsIgnoreCase(d.getNodeName())) {
                  NamedNodeMap weapon = d.getAttributes();
                  int itemId = 0;
                  long itemCount = 0L;
                  int id = Integer.parseInt(weapon.getNamedItem("id").getNodeValue());
                  String name = weapon.getNamedItem("name").getNodeValue();
                  String type = weapon.getNamedItem("type").getNodeValue();
                  boolean allowEnchant = weapon.getNamedItem("allowEnchant") != null
                     ? Boolean.parseBoolean(weapon.getNamedItem("allowEnchant").getNodeValue())
                     : true;
                  boolean allowAugment = weapon.getNamedItem("allowAugment") != null
                     ? Boolean.parseBoolean(weapon.getNamedItem("allowAugment").getNodeValue())
                     : true;

                  for(Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
                     weapon = cd.getAttributes();
                     if ("price".equalsIgnoreCase(cd.getNodeName())) {
                        itemId = Integer.parseInt(weapon.getNamedItem("id").getNodeValue());
                        itemCount = Long.parseLong(weapon.getNamedItem("count").getNodeValue());
                     }
                  }

                  this.addWeapon(new DressWeaponTemplate(id, name, type, itemId, itemCount, allowEnchant, allowAugment));
               }
            }
         }
      }
   }

   public void addWeapon(DressWeaponTemplate weapon) {
      this._weapons.add(weapon);
   }

   public List<DressWeaponTemplate> getAllWeapons() {
      return this._weapons;
   }

   public DressWeaponTemplate getWeapon(int id) {
      for(DressWeaponTemplate weapon : this._weapons) {
         if (weapon.getId() == id) {
            return weapon;
         }
      }

      return null;
   }

   public int size() {
      return this._weapons.size();
   }

   public static DressWeaponParser getInstance() {
      return DressWeaponParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final DressWeaponParser _instance = new DressWeaponParser();
   }
}
