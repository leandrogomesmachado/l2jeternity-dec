package l2e.gameserver.data.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.actor.templates.ActionKeyTemplate;
import org.w3c.dom.Node;

public class UIParser extends DocumentParser {
   private static final Logger _log = Logger.getLogger(UIParser.class.getName());
   private final Map<Integer, List<ActionKeyTemplate>> _storedKeys = new HashMap<>();
   private final Map<Integer, List<Integer>> _storedCategories = new HashMap<>();

   protected UIParser() {
      this.load();
   }

   @Override
   public void load() {
      this._storedKeys.clear();
      this._storedCategories.clear();
      this.parseDatapackFile("data/stats/chars/playerUi.xml");
      _log.info(this.getClass().getSimpleName() + ": Loaded " + this._storedKeys.size() + " keys " + this._storedCategories.size() + " categories.");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node n = this.getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equalsIgnoreCase(n.getNodeName())) {
            for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
               if ("category".equalsIgnoreCase(d.getNodeName())) {
                  this.parseCategory(d);
               }
            }
         }
      }
   }

   private void parseCategory(Node n) {
      int cat = parseInt(n.getAttributes(), "id");

      for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
         if ("commands".equalsIgnoreCase(d.getNodeName())) {
            this.parseCommands(cat, d);
         } else if ("keys".equalsIgnoreCase(d.getNodeName())) {
            this.parseKeys(cat, d);
         }
      }
   }

   private void parseCommands(int cat, Node d) {
      for(Node c = d.getFirstChild(); c != null; c = c.getNextSibling()) {
         if ("cmd".equalsIgnoreCase(c.getNodeName())) {
            addCategory(this._storedCategories, cat, Integer.parseInt(c.getTextContent()));
         }
      }
   }

   private void parseKeys(int cat, Node d) {
      for(Node c = d.getFirstChild(); c != null; c = c.getNextSibling()) {
         if ("key".equalsIgnoreCase(c.getNodeName())) {
            ActionKeyTemplate akey = new ActionKeyTemplate(cat);

            for(int i = 0; i < c.getAttributes().getLength(); ++i) {
               Node att = c.getAttributes().item(i);
               int val = Integer.parseInt(att.getNodeValue());
               String var8 = att.getNodeName();
               switch(var8) {
                  case "cmd":
                     akey.setCommandId(val);
                     break;
                  case "key":
                     akey.setKeyId(val);
                     break;
                  case "toggleKey1":
                     akey.setToogleKey1(val);
                     break;
                  case "toggleKey2":
                     akey.setToogleKey2(val);
                     break;
                  case "showType":
                     akey.setShowStatus(val);
               }
            }

            addKey(this._storedKeys, cat, akey);
         }
      }
   }

   public static void addCategory(Map<Integer, List<Integer>> map, int cat, int cmd) {
      if (!map.containsKey(cat)) {
         map.put(cat, new ArrayList<>());
      }

      map.get(cat).add(cmd);
   }

   public static void addKey(Map<Integer, List<ActionKeyTemplate>> map, int cat, ActionKeyTemplate akey) {
      if (!map.containsKey(cat)) {
         map.put(cat, new ArrayList<>());
      }

      map.get(cat).add(akey);
   }

   public Map<Integer, List<Integer>> getCategories() {
      return this._storedCategories;
   }

   public Map<Integer, List<ActionKeyTemplate>> getKeys() {
      return this._storedKeys;
   }

   public static UIParser getInstance() {
      return UIParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final UIParser _instance = new UIParser();
   }
}
