package l2e.gameserver.data.parser;

import java.util.HashMap;
import java.util.Map;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.items.enchant.EnchantItem;
import l2e.gameserver.model.items.enchant.EnchantScroll;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.stats.StatsSet;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class EnchantItemParser extends DocumentParser {
   public static final Map<Integer, EnchantScroll> _scrolls = new HashMap<>();
   public static final Map<Integer, EnchantItem> _supports = new HashMap<>();

   public EnchantItemParser() {
      this.load();
   }

   @Override
   public synchronized void load() {
      _scrolls.clear();
      _supports.clear();
      this.parseDatapackFile("data/stats/enchanting/enchantItemData.xml");
      this._log.info(this.getClass().getSimpleName() + ": Loaded " + _scrolls.size() + " enchant scrolls and " + _supports.size() + " support items.");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node n = this.getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equalsIgnoreCase(n.getNodeName())) {
            for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
               if ("enchant".equalsIgnoreCase(d.getNodeName())) {
                  NamedNodeMap attrs = d.getAttributes();
                  StatsSet set = new StatsSet();

                  for(int i = 0; i < attrs.getLength(); ++i) {
                     Node att = attrs.item(i);
                     set.set(att.getNodeName(), att.getNodeValue());
                  }

                  EnchantScroll item = new EnchantScroll(set);

                  for(Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
                     if ("item".equalsIgnoreCase(cd.getNodeName())) {
                        item.addItem(parseInteger(cd.getAttributes(), "id"));
                     }
                  }

                  _scrolls.put(item.getId(), item);
               } else if ("support".equalsIgnoreCase(d.getNodeName())) {
                  NamedNodeMap attrs = d.getAttributes();
                  StatsSet set = new StatsSet();

                  for(int i = 0; i < attrs.getLength(); ++i) {
                     Node att = attrs.item(i);
                     set.set(att.getNodeName(), att.getNodeValue());
                  }

                  EnchantItem item = new EnchantItem(set);

                  for(Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
                     if ("item".equalsIgnoreCase(cd.getNodeName())) {
                        item.addItem(parseInteger(cd.getAttributes(), "id"));
                     }
                  }

                  _supports.put(item.getId(), item);
               }
            }
         }
      }
   }

   public final EnchantScroll getEnchantScroll(ItemInstance scroll) {
      return _scrolls.get(scroll.getId());
   }

   public final EnchantItem getSupportItem(ItemInstance item) {
      return _supports.get(item.getId());
   }

   public static final EnchantItemParser getInstance() {
      return EnchantItemParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final EnchantItemParser _instance = new EnchantItemParser();
   }
}
