package l2e.gameserver.data.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import l2e.commons.util.Util;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.holders.RangeChanceHolder;
import l2e.gameserver.model.items.enchant.EnchantItemGroup;
import l2e.gameserver.model.items.enchant.EnchantRateItem;
import l2e.gameserver.model.items.enchant.EnchantScrollGroup;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class EnchantItemGroupsParser extends DocumentParser {
   private final Map<String, EnchantItemGroup> _itemGroups = new HashMap<>();
   private final Map<Integer, EnchantScrollGroup> _scrollGroups = new HashMap<>();

   protected EnchantItemGroupsParser() {
      this.load();
   }

   @Override
   public synchronized void load() {
      this._itemGroups.clear();
      this._scrollGroups.clear();
      this.parseDatapackFile("data/stats/enchanting/enchantItemGroups.xml");
      this._log
         .log(
            Level.INFO,
            this.getClass().getSimpleName() + ": Loaded: " + this._itemGroups.size() + " items and " + this._scrollGroups.size() + " scrolls group template."
         );
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node n = this.getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equalsIgnoreCase(n.getNodeName())) {
            for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
               if ("enchantRateGroup".equalsIgnoreCase(d.getNodeName())) {
                  String name = parseString(d.getAttributes(), "name");
                  EnchantItemGroup group = new EnchantItemGroup(name);

                  for(Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
                     if ("current".equalsIgnoreCase(cd.getNodeName())) {
                        String range = parseString(cd.getAttributes(), "enchant");
                        double chance = parseDouble(cd.getAttributes(), "chance");
                        int min = -1;
                        int max = 0;
                        if (range.contains("-")) {
                           String[] split = range.split("-");
                           if (split.length == 2 && Util.isDigit(split[0]) && Util.isDigit(split[1])) {
                              min = Integer.parseInt(split[0]);
                              max = Integer.parseInt(split[1]);
                           }
                        } else if (Util.isDigit(range)) {
                           min = Integer.parseInt(range);
                           max = min;
                        }

                        if (min >= 0 && max >= 0) {
                           group.addChance(new RangeChanceHolder(min, max, chance));
                        }
                     }
                  }

                  this._itemGroups.put(name, group);
               } else if ("enchantScrollGroup".equals(d.getNodeName())) {
                  int id = parseInt(d.getAttributes(), "id");
                  EnchantScrollGroup group = new EnchantScrollGroup(id);

                  for(Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
                     if ("enchantRate".equalsIgnoreCase(cd.getNodeName())) {
                        EnchantRateItem rateGroup = new EnchantRateItem(parseString(cd.getAttributes(), "group"));

                        for(Node z = cd.getFirstChild(); z != null; z = z.getNextSibling()) {
                           if ("item".equals(z.getNodeName())) {
                              NamedNodeMap attrs = z.getAttributes();
                              if (attrs.getNamedItem("slot") != null) {
                                 rateGroup.addSlot(ItemsParser._slots.get(parseString(attrs, "slot")));
                              }

                              if (attrs.getNamedItem("magicWeapon") != null) {
                                 rateGroup.setMagicWeapon(parseBoolean(attrs, "magicWeapon"));
                              }

                              if (attrs.getNamedItem("id") != null) {
                                 rateGroup.setItemId(parseInt(attrs, "id"));
                              }
                           }
                        }

                        group.addRateGroup(rateGroup);
                     }
                  }

                  this._scrollGroups.put(id, group);
               }
            }
         }
      }
   }

   public EnchantItemGroup getItemGroup(Item item, int scrollGroup) {
      EnchantScrollGroup group = this._scrollGroups.get(scrollGroup);
      EnchantRateItem rateGroup = group.getRateGroup(item);
      return rateGroup != null ? this._itemGroups.get(rateGroup.getName()) : null;
   }

   public EnchantItemGroup getItemGroup(String name) {
      return this._itemGroups.get(name);
   }

   public EnchantScrollGroup getScrollGroup(int id) {
      return this._scrollGroups.get(id);
   }

   public static EnchantItemGroupsParser getInstance() {
      return EnchantItemGroupsParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final EnchantItemGroupsParser _instance = new EnchantItemGroupsParser();
   }
}
