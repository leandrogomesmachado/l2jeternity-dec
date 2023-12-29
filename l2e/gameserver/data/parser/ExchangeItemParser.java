package l2e.gameserver.data.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.service.exchange.Change;
import l2e.gameserver.model.service.exchange.Variant;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class ExchangeItemParser extends DocumentParser {
   private static Logger _log = Logger.getLogger(ExchangeItemParser.class.getName());
   private final Map<Integer, Change> _changes = new HashMap<>();
   private final Map<Integer, Change> _upgrades = new HashMap<>();

   public static ExchangeItemParser getInstance() {
      return ExchangeItemParser.SingletonHolder._instance;
   }

   protected ExchangeItemParser() {
      this._changes.clear();
      this._upgrades.clear();
      this.load();
   }

   @Override
   public synchronized void load() {
      this.parseDatapackFile("data/stats/services/exchange.xml");
      _log.info(
         this.getClass().getSimpleName() + ": Loaded " + this._changes.size() + " changes groups and " + this._upgrades.size() + " update changes groups."
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
               if ("change".equalsIgnoreCase(d.getNodeName())) {
                  NamedNodeMap attrs = d.getAttributes();
                  int changeId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
                  String changeName = attrs.getNamedItem("name").getNodeValue();
                  String changeIcon = attrs.getNamedItem("icon").getNodeValue();
                  int cost_id = Integer.parseInt(attrs.getNamedItem("cost_id").getNodeValue());
                  long cost_count = Long.parseLong(attrs.getNamedItem("cost_count").getNodeValue());
                  boolean attribute_change = Boolean.parseBoolean(attrs.getNamedItem("attribute_change").getNodeValue());
                  boolean is_upgrade = Boolean.parseBoolean(attrs.getNamedItem("is_upgrade").getNodeValue());
                  this.addChanges(
                     new Change(changeId, changeName, changeIcon, cost_id, cost_count, attribute_change, is_upgrade, this.parseVariants(d, attrs))
                  );
               }
            }
         }
      }
   }

   private List<Variant> parseVariants(Node d, NamedNodeMap attrs) {
      List<Variant> list = new ArrayList<>();

      for(Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
         if ("variant".equalsIgnoreCase(cd.getNodeName())) {
            attrs = cd.getAttributes();
            int number = Integer.parseInt(attrs.getNamedItem("number").getNodeValue());
            int id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
            String name = attrs.getNamedItem("name").getNodeValue();
            String icon = attrs.getNamedItem("icon").getNodeValue();
            list.add(new Variant(number, id, name, icon));
         }
      }

      return list;
   }

   public void addChanges(Change armorset) {
      if (armorset.isUpgrade()) {
         this._upgrades.put(armorset.getId(), armorset);
      } else {
         this._changes.put(armorset.getId(), armorset);
      }
   }

   public Change getChanges(int id, boolean isUpgrade) {
      return isUpgrade ? this._upgrades.get(id) : this._changes.get(id);
   }

   public int size() {
      return this._changes.size() + this._upgrades.size();
   }

   private static class SingletonHolder {
      protected static final ExchangeItemParser _instance = new ExchangeItemParser();
   }
}
