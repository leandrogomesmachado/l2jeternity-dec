package l2e.gameserver.data.parser;

import java.util.HashMap;
import java.util.Map;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.actor.templates.ClassMasterTemplate;
import org.w3c.dom.Node;

public final class ClassMasterParser extends DocumentParser {
   private final Map<Integer, ClassMasterTemplate> _templates = new HashMap<>();
   private boolean _allowClassMaster;
   private boolean _allowCommunityClassMaster;

   protected ClassMasterParser() {
      this.load();
   }

   public void reload() {
      this.load();
   }

   @Override
   public void load() {
      this._templates.clear();
      this.parseDatapackFile("data/stats/services/classMaster.xml");
      this._log.info(this.getClass().getSimpleName() + ": Loaded " + this._templates.size() + " class master templates.");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node n = this.getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equals(n.getNodeName())) {
            this._allowClassMaster = Boolean.parseBoolean(n.getAttributes().getNamedItem("allowClassMaster").getNodeValue());
            this._allowCommunityClassMaster = Boolean.parseBoolean(n.getAttributes().getNamedItem("allowCommunityClassMaster").getNodeValue());

            for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
               if ("class".equals(d.getNodeName())) {
                  int level = Integer.parseInt(d.getAttributes().getNamedItem("level").getNodeValue());
                  boolean allowedChange = d.getAttributes().getNamedItem("allowedChange") != null
                     ? Boolean.parseBoolean(d.getAttributes().getNamedItem("allowedChange").getNodeValue())
                     : false;
                  Map<Integer, Long> requestItems = new HashMap<>();
                  Map<Integer, Long> rewardItems = new HashMap<>();

                  for(Node c = d.getFirstChild(); c != null; c = c.getNextSibling()) {
                     if ("requestItems".equals(c.getNodeName())) {
                        for(Node i = c.getFirstChild(); i != null; i = i.getNextSibling()) {
                           if ("item".equals(i.getNodeName())) {
                              int id = Integer.parseInt(i.getAttributes().getNamedItem("id").getNodeValue());
                              long count = Long.parseLong(i.getAttributes().getNamedItem("count").getNodeValue());
                              requestItems.put(id, count);
                           }
                        }
                     } else if ("rewardItems".equals(c.getNodeName())) {
                        for(Node i = c.getFirstChild(); i != null; i = i.getNextSibling()) {
                           if ("item".equals(i.getNodeName())) {
                              int id = Integer.parseInt(i.getAttributes().getNamedItem("id").getNodeValue());
                              long count = Long.parseLong(i.getAttributes().getNamedItem("count").getNodeValue());
                              rewardItems.put(id, count);
                           }
                        }
                     }
                  }

                  this._templates.put(level, new ClassMasterTemplate(requestItems, rewardItems, allowedChange));
               }
            }
         }
      }
   }

   public ClassMasterTemplate getClassTemplate(int level) {
      return this._templates.containsKey(level) ? this._templates.get(level) : null;
   }

   public boolean isAllowedClassChange(int level) {
      if (!this._templates.containsKey(level)) {
         return false;
      } else {
         ClassMasterTemplate template = this._templates.get(level);
         return template != null && template.isAllowedChangeClass();
      }
   }

   public boolean isAllowClassMaster() {
      return this._allowClassMaster;
   }

   public boolean isAllowCommunityClassMaster() {
      return this._allowCommunityClassMaster;
   }

   public static ClassMasterParser getInstance() {
      return ClassMasterParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final ClassMasterParser _instance = new ClassMasterParser();
   }
}
