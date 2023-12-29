package l2e.gameserver.data.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.templates.community.CBTeleportTemplate;
import l2e.gameserver.model.holders.ItemHolder;
import org.w3c.dom.Node;

public class CommunityTeleportsParser extends DocumentParser {
   private static Logger _log = Logger.getLogger(CommunityTeleportsParser.class.getName());
   private final Map<Integer, CBTeleportTemplate> _templates = new HashMap<>();

   protected CommunityTeleportsParser() {
      this.load();
   }

   @Override
   public void load() {
      this._templates.clear();
      this.parseDatapackFile("data/stats/services/communityTeleports.xml");
      _log.log(Level.INFO, this.getClass().getSimpleName() + ": Loaded " + this._templates.size() + " community teleport templates.");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node list = this.getCurrentDocument().getFirstChild().getFirstChild(); list != null; list = list.getNextSibling()) {
         if (list.getNodeName().equalsIgnoreCase("point")) {
            int id = Integer.parseInt(list.getAttributes().getNamedItem("id").getNodeValue());
            String name = list.getAttributes().getNamedItem("name").getNodeValue();
            int minLvl = Integer.parseInt(list.getAttributes().getNamedItem("minLevel").getNodeValue());
            int maxLvl = Integer.parseInt(list.getAttributes().getNamedItem("maxLevel").getNodeValue());
            boolean canPk = Boolean.parseBoolean(list.getAttributes().getNamedItem("pk").getNodeValue());
            boolean forPremium = Boolean.parseBoolean(list.getAttributes().getNamedItem("forPremium").getNodeValue());
            ItemHolder price = null;
            Location loc = null;

            for(Node cd = list.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
               if ("cost".equalsIgnoreCase(cd.getNodeName())) {
                  int itemId = Integer.parseInt(cd.getAttributes().getNamedItem("itemId").getNodeValue());
                  long count = Long.parseLong(cd.getAttributes().getNamedItem("count").getNodeValue());
                  price = new ItemHolder(itemId, count);
               } else if ("coordinates".equalsIgnoreCase(cd.getNodeName())) {
                  int x = Integer.parseInt(cd.getAttributes().getNamedItem("x").getNodeValue());
                  int y = Integer.parseInt(cd.getAttributes().getNamedItem("y").getNodeValue());
                  int z = Integer.parseInt(cd.getAttributes().getNamedItem("z").getNodeValue());
                  loc = new Location(x, y, z);
               }
            }

            this._templates.put(id, new CBTeleportTemplate(id, name, minLvl, maxLvl, canPk, forPremium, loc, price));
         }
      }
   }

   public CBTeleportTemplate getTemplate(int id) {
      return this._templates.get(id);
   }

   public static CommunityTeleportsParser getInstance() {
      return CommunityTeleportsParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final CommunityTeleportsParser _instance = new CommunityTeleportsParser();
   }
}
