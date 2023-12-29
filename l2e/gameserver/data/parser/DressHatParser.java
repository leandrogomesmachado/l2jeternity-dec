package l2e.gameserver.data.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.actor.templates.DressHatTemplate;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class DressHatParser extends DocumentParser {
   protected static Logger _log = Logger.getLogger(DressHatParser.class.getName());
   private final List<DressHatTemplate> _hat = new ArrayList<>();

   private DressHatParser() {
      this._hat.clear();
      this.load();
   }

   @Override
   public synchronized void load() {
      this.parseDatapackFile("data/stats/services/dress/dressHat.xml");
      _log.info(this.getClass().getSimpleName() + ": Loaded " + this._hat.size() + " dress hat templates.");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node n = this.getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equalsIgnoreCase(n.getNodeName())) {
            for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
               if ("hat".equalsIgnoreCase(d.getNodeName())) {
                  NamedNodeMap hat = d.getAttributes();
                  int itemId = 0;
                  long itemCount = 0L;
                  int number = Integer.parseInt(hat.getNamedItem("number").getNodeValue());
                  int id = Integer.parseInt(hat.getNamedItem("id").getNodeValue());
                  String name = hat.getNamedItem("name").getNodeValue();
                  int slot = Integer.parseInt(hat.getNamedItem("slot").getNodeValue());

                  for(Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
                     hat = cd.getAttributes();
                     if ("price".equalsIgnoreCase(cd.getNodeName())) {
                        itemId = Integer.parseInt(hat.getNamedItem("id").getNodeValue());
                        itemCount = Long.parseLong(hat.getNamedItem("count").getNodeValue());
                     }
                  }

                  this.addHat(new DressHatTemplate(number, id, name, slot, itemId, itemCount));
               }
            }
         }
      }
   }

   public void addHat(DressHatTemplate shield) {
      this._hat.add(shield);
   }

   public List<DressHatTemplate> getAllHats() {
      return this._hat;
   }

   public DressHatTemplate getHat(int id) {
      for(DressHatTemplate hat : this._hat) {
         if (hat.getId() == id) {
            return hat;
         }
      }

      return null;
   }

   public int getHatId(int id) {
      for(DressHatTemplate hat : this._hat) {
         if (hat.getHatId() == id) {
            return hat.getId();
         }
      }

      return -1;
   }

   public int size() {
      return this._hat.size();
   }

   public static DressHatParser getInstance() {
      return DressHatParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final DressHatParser _instance = new DressHatParser();
   }
}
