package l2e.gameserver.data.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import l2e.commons.util.Util;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.skills.options.EnchantOptions;
import org.w3c.dom.Node;

public class EnchantItemOptionsParser extends DocumentParser {
   private final Map<Integer, Map<Integer, EnchantOptions>> _data = new HashMap<>();

   protected EnchantItemOptionsParser() {
      this.load();
   }

   @Override
   public synchronized void load() {
      this.parseDatapackFile("data/stats/enchanting/enchantItemOptions.xml");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      Node att = null;
      int counter = 0;
      EnchantOptions op = null;

      for(Node n = this.getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equalsIgnoreCase(n.getNodeName())) {
            for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
               if ("item".equalsIgnoreCase(d.getNodeName())) {
                  int itemId = parseInt(d.getAttributes(), "id");
                  if (!this._data.containsKey(itemId)) {
                     this._data.put(itemId, new HashMap<>());
                  }

                  for(Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
                     if ("options".equalsIgnoreCase(cd.getNodeName())) {
                        op = new EnchantOptions(parseInt(cd.getAttributes(), "level"));
                        this._data.get(itemId).put(op.getLevel(), op);

                        for(byte i = 0; i < 3; ++i) {
                           att = cd.getAttributes().getNamedItem("option" + (i + 1));
                           if (att != null && Util.isDigit(att.getNodeValue())) {
                              op.setOption(i, parseInt(att));
                           }
                        }

                        ++counter;
                     }
                  }
               }
            }
         }
      }

      this._log.log(Level.INFO, this.getClass().getSimpleName() + ": Loaded: " + this._data.size() + " items and " + counter + " options.");
   }

   public EnchantOptions getOptions(int itemId, int enchantLevel) {
      return this._data.containsKey(itemId) && this._data.get(itemId).containsKey(enchantLevel) ? this._data.get(itemId).get(enchantLevel) : null;
   }

   public EnchantOptions getOptions(ItemInstance item) {
      return item != null ? this.getOptions(item.getId(), item.getEnchantLevel()) : null;
   }

   public static final EnchantItemOptionsParser getInstance() {
      return EnchantItemOptionsParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final EnchantItemOptionsParser _instance = new EnchantItemOptionsParser();
   }
}
