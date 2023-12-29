package l2e.gameserver.data.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.actor.templates.TeleportTemplate;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class TeleLocationParser extends DocumentParser {
   private static Logger _log = Logger.getLogger(TeleLocationParser.class.getName());
   private final Map<Integer, TeleportTemplate> _teleports = new HashMap<>();

   protected TeleLocationParser() {
      this.load();
   }

   @Override
   public void load() {
      this._teleports.clear();
      this.parseDatapackFile("data/stats/npcs/teleports.xml");
      _log.log(Level.INFO, this.getClass().getSimpleName() + ": Loaded " + this._teleports.size() + " teleport templates.");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node list = this.getCurrentDocument().getFirstChild().getFirstChild(); list != null; list = list.getNextSibling()) {
         if (list.getNodeName().equalsIgnoreCase("teleport")) {
            NamedNodeMap node = list.getAttributes();
            TeleportTemplate teleport = new TeleportTemplate();
            teleport.setTeleId(Integer.valueOf(node.getNamedItem("id").getNodeValue()));
            teleport.setLocX(Integer.valueOf(node.getNamedItem("locX").getNodeValue()));
            teleport.setLocY(Integer.valueOf(node.getNamedItem("locY").getNodeValue()));
            teleport.setLocZ(Integer.valueOf(node.getNamedItem("locZ").getNodeValue()));
            teleport.setPrice(Integer.valueOf(node.getNamedItem("price").getNodeValue()));
            teleport.setIsForNoble(Integer.valueOf(node.getNamedItem("noobless").getNodeValue()) == 1);
            teleport.setItemId(Integer.valueOf(node.getNamedItem("itemId").getNodeValue()));
            this._teleports.put(teleport.getTeleId(), teleport);
         }
      }
   }

   public TeleportTemplate getTemplate(int id) {
      return this._teleports.get(id);
   }

   public static TeleLocationParser getInstance() {
      return TeleLocationParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final TeleLocationParser _instance = new TeleLocationParser();
   }
}
