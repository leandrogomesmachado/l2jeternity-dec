package l2e.gameserver.data.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.actor.templates.DressShieldTemplate;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class DressShieldParser extends DocumentParser {
   protected static Logger _log = Logger.getLogger(DressShieldParser.class.getName());
   private final List<DressShieldTemplate> _shield = new ArrayList<>();

   private DressShieldParser() {
      this._shield.clear();
      this.load();
   }

   @Override
   public synchronized void load() {
      this.parseDatapackFile("data/stats/services/dress/dressShield.xml");
      _log.info(this.getClass().getSimpleName() + ": Loaded " + this._shield.size() + " dress shield templates.");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node n = this.getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equalsIgnoreCase(n.getNodeName())) {
            for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
               if ("shield".equalsIgnoreCase(d.getNodeName())) {
                  NamedNodeMap shield = d.getAttributes();
                  int itemId = 0;
                  long itemCount = 0L;
                  int number = Integer.parseInt(shield.getNamedItem("number").getNodeValue());
                  int id = Integer.parseInt(shield.getNamedItem("id").getNodeValue());
                  String name = shield.getNamedItem("name").getNodeValue();

                  for(Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
                     shield = cd.getAttributes();
                     if ("price".equalsIgnoreCase(cd.getNodeName())) {
                        itemId = Integer.parseInt(shield.getNamedItem("id").getNodeValue());
                        itemCount = Long.parseLong(shield.getNamedItem("count").getNodeValue());
                     }
                  }

                  this.addShield(new DressShieldTemplate(number, id, name, itemId, itemCount));
               }
            }
         }
      }
   }

   public void addShield(DressShieldTemplate shield) {
      this._shield.add(shield);
   }

   public List<DressShieldTemplate> getAllShields() {
      return this._shield;
   }

   public DressShieldTemplate getShield(int id) {
      for(DressShieldTemplate shield : this._shield) {
         if (shield.getId() == id) {
            return shield;
         }
      }

      return null;
   }

   public int getShieldId(int id) {
      for(DressShieldTemplate shield : this._shield) {
         if (shield.getShieldId() == id) {
            return shield.getId();
         }
      }

      return -1;
   }

   public int size() {
      return this._shield.size();
   }

   public static DressShieldParser getInstance() {
      return DressShieldParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final DressShieldParser _instance = new DressShieldParser();
   }
}
