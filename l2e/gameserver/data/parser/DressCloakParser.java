package l2e.gameserver.data.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.actor.templates.DressCloakTemplate;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class DressCloakParser extends DocumentParser {
   protected static Logger _log = Logger.getLogger(DressCloakParser.class.getName());
   private final List<DressCloakTemplate> _cloak = new ArrayList<>();

   private DressCloakParser() {
      this._cloak.clear();
      this.load();
   }

   @Override
   public synchronized void load() {
      this.parseDatapackFile("data/stats/services/dress/dressCloak.xml");
      _log.info(this.getClass().getSimpleName() + ": Loaded " + this._cloak.size() + " dress cloak templates.");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node n = this.getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equalsIgnoreCase(n.getNodeName())) {
            for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
               if ("cloak".equalsIgnoreCase(d.getNodeName())) {
                  NamedNodeMap cloak = d.getAttributes();
                  int itemId = 0;
                  long itemCount = 0L;
                  int number = Integer.parseInt(cloak.getNamedItem("number").getNodeValue());
                  int id = Integer.parseInt(cloak.getNamedItem("id").getNodeValue());
                  String name = cloak.getNamedItem("name").getNodeValue();

                  for(Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
                     cloak = cd.getAttributes();
                     if ("price".equalsIgnoreCase(cd.getNodeName())) {
                        itemId = Integer.parseInt(cloak.getNamedItem("id").getNodeValue());
                        itemCount = Long.parseLong(cloak.getNamedItem("count").getNodeValue());
                     }
                  }

                  this.addCloak(new DressCloakTemplate(number, id, name, itemId, itemCount));
               }
            }
         }
      }
   }

   public void addCloak(DressCloakTemplate cloak) {
      this._cloak.add(cloak);
   }

   public List<DressCloakTemplate> getAllCloaks() {
      return this._cloak;
   }

   public DressCloakTemplate getCloak(int id) {
      for(DressCloakTemplate cloak : this._cloak) {
         if (cloak.getId() == id) {
            return cloak;
         }
      }

      return null;
   }

   public int getCloakId(int id) {
      for(DressCloakTemplate cloak : this._cloak) {
         if (cloak.getCloakId() == id) {
            return cloak.getId();
         }
      }

      return -1;
   }

   public int size() {
      return this._cloak.size();
   }

   public static DressCloakParser getInstance() {
      return DressCloakParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final DressCloakParser _instance = new DressCloakParser();
   }
}
