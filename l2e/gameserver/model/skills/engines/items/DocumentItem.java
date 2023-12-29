package l2e.gameserver.model.skills.engines.items;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.skills.DocumentBase;
import l2e.gameserver.model.skills.conditions.Condition;
import l2e.gameserver.model.stats.StatsSet;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public final class DocumentItem extends DocumentBase {
   private ItemTemplate _currentItem = null;
   private final List<Item> _itemsInFile = new ArrayList<>();

   public DocumentItem(File file) {
      super(file);
   }

   @Override
   protected StatsSet getStatsSet() {
      return this._currentItem.set;
   }

   @Override
   protected String getTableValue(String name) {
      return this._tables.get(name)[this._currentItem.currentLevel];
   }

   @Override
   protected String getTableValue(String name, int idx) {
      return this._tables.get(name)[idx - 1];
   }

   @Override
   protected void parseDocument(Document doc) {
      for(Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equalsIgnoreCase(n.getNodeName())) {
            for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
               if ("item".equalsIgnoreCase(d.getNodeName())) {
                  try {
                     this._currentItem = new ItemTemplate();
                     this.parseItem(d);
                     this._itemsInFile.add(this._currentItem.item);
                     this.resetTable();
                  } catch (Exception var5) {
                     this._log.log(Level.WARNING, "Cannot create item " + this._currentItem.id, (Throwable)var5);
                  }
               }
            }
         }
      }
   }

   protected void parseItem(Node n) throws InvocationTargetException {
      int itemId = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
      String className = n.getAttributes().getNamedItem("type").getNodeValue();
      String itemNameEn = n.getAttributes().getNamedItem("nameEn").getNodeValue();
      String itemNameRu = n.getAttributes().getNamedItem("nameRu").getNodeValue();
      this._currentItem.id = itemId;
      this._currentItem.nameEn = itemNameEn;
      this._currentItem.nameRu = itemNameRu;
      this._currentItem.type = className;
      this._currentItem.set = new StatsSet();
      this._currentItem.set.set("item_id", itemId);
      this._currentItem.set.set("nameEn", itemNameEn);
      this._currentItem.set.set("nameRu", itemNameRu);
      Node first = n.getFirstChild();

      for(Node var11 = first; var11 != null; var11 = var11.getNextSibling()) {
         if ("table".equalsIgnoreCase(var11.getNodeName())) {
            if (this._currentItem.item != null) {
               throw new IllegalStateException("Item created but table node found! Item " + itemId);
            }

            this.parseTable(var11);
         } else if ("set".equalsIgnoreCase(var11.getNodeName())) {
            if (this._currentItem.item != null) {
               throw new IllegalStateException("Item created but set node found! Item " + itemId);
            }

            this.parseBeanSet(var11, this._currentItem.set, Integer.valueOf(1));
         } else if ("for".equalsIgnoreCase(var11.getNodeName())) {
            this.makeItem();
            this.parseTemplate(var11, this._currentItem.item);
         } else if ("cond".equalsIgnoreCase(var11.getNodeName())) {
            this.makeItem();
            Condition condition = this.parseCondition(var11.getFirstChild(), this._currentItem.item);
            Node msg = var11.getAttributes().getNamedItem("msg");
            Node msgId = var11.getAttributes().getNamedItem("msgId");
            if (condition != null && msg != null) {
               condition.setMessage(msg.getNodeValue());
            } else if (condition != null && msgId != null) {
               condition.setMessageId(Integer.decode(this.getValue(msgId.getNodeValue(), null)));
               Node addName = var11.getAttributes().getNamedItem("addName");
               if (addName != null && Integer.decode(this.getValue(msgId.getNodeValue(), null)) > 0) {
                  condition.addName();
               }
            }

            this._currentItem.item.attach(condition);
         }
      }

      this.makeItem();
   }

   private void makeItem() throws InvocationTargetException {
      if (this._currentItem.item == null) {
         try {
            Constructor<?> c = Class.forName("l2e.gameserver.model.actor.templates.items." + this._currentItem.type).getConstructor(StatsSet.class);
            this._currentItem.item = (Item)c.newInstance(this._currentItem.set);
         } catch (Exception var2) {
            throw new InvocationTargetException(var2);
         }
      }
   }

   public List<Item> getItemList() {
      return this._itemsInFile;
   }
}
