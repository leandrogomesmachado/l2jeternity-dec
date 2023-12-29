package l2e.gameserver.data.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.CategoryType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class CategoryParser extends DocumentParser {
   private static final Logger _log = Logger.getLogger(CategoryParser.class.getName());
   private final Map<CategoryType, Set<Integer>> _categories = new HashMap<>();

   protected CategoryParser() {
      this.load();
   }

   @Override
   public void load() {
      this.parseDatapackFile("data/stats/chars/categoryList.xml");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node node = this.getCurrentDocument().getFirstChild(); node != null; node = node.getNextSibling()) {
         if ("list".equalsIgnoreCase(node.getNodeName())) {
            for(Node list_node = node.getFirstChild(); list_node != null; list_node = list_node.getNextSibling()) {
               if ("category".equalsIgnoreCase(list_node.getNodeName())) {
                  NamedNodeMap attrs = list_node.getAttributes();
                  CategoryType categoryType = CategoryType.findByName(attrs.getNamedItem("name").getNodeValue());
                  if (categoryType == null) {
                     _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Can't find category by name :" + attrs.getNamedItem("name").getNodeValue());
                  } else {
                     Set<Integer> ids = new HashSet<>();

                     for(Node category_node = list_node.getFirstChild(); category_node != null; category_node = category_node.getNextSibling()) {
                        if ("id".equalsIgnoreCase(category_node.getNodeName())) {
                           ids.add(Integer.parseInt(category_node.getTextContent()));
                        }
                     }

                     this._categories.put(categoryType, ids);
                  }
               }
            }
         }
      }
   }

   public boolean isInCategory(CategoryType type, int id) {
      Set<Integer> category = this.getCategoryByType(type);
      if (category == null) {
         _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Can't find category type :" + type);
         return false;
      } else {
         return category.contains(id);
      }
   }

   public Set<Integer> getCategoryByType(CategoryType type) {
      return this._categories.get(type);
   }

   public static CategoryParser getInstance() {
      return CategoryParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final CategoryParser _instance = new CategoryParser();
   }
}
