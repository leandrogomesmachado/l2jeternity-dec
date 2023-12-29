package l2e.gameserver.data.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import l2e.commons.geometry.Polygon;
import l2e.commons.util.Rnd;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.actor.templates.player.FakePassiveLocTemplate;
import l2e.gameserver.model.spawn.SpawnTerritory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class FakePassiveLocationParser extends DocumentParser {
   private static Logger _log = Logger.getLogger(FakePassiveLocationParser.class.getName());
   private static List<FakePassiveLocTemplate> _locations = new ArrayList<>();
   private int _totalAmount = 0;

   protected FakePassiveLocationParser() {
      this.load();
   }

   @Override
   public void load() {
      _locations.clear();
      this.parseDatapackFile("config/mods/fakes/passive_locations.xml");
      _log.info(this.getClass().getSimpleName() + ": Loaded " + _locations.size() + " fake players passive locations.");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node list = this.getCurrentDocument().getFirstChild().getFirstChild(); list != null; list = list.getNextSibling()) {
         if (list.getNodeName().equalsIgnoreCase("location")) {
            NamedNodeMap node = list.getAttributes();
            int id = Integer.valueOf(node.getNamedItem("id").getNodeValue());
            int amount = Integer.valueOf(node.getNamedItem("amount").getNodeValue());
            SpawnTerritory territory = null;
            int minLvl = 1;
            int maxLvl = 1;
            long minDelay = 1L;
            long maxDelay = 1L;
            long minRespawn = 1L;
            long maxRespawn = 1L;
            List<Integer> classes = null;

            for(Node d = list.getFirstChild(); d != null; d = d.getNextSibling()) {
               if (d.getNodeName().equalsIgnoreCase("territory")) {
                  String name = d.getAttributes().getNamedItem("name").getNodeValue();
                  territory = new SpawnTerritory();
                  territory.add(this.parsePolygon0(name, d, d.getAttributes()));
               } else if ("class".equalsIgnoreCase(d.getNodeName())) {
                  classes = this.parseExcludedClasses(d.getAttributes().getNamedItem("id").getNodeValue());
               } else if ("level".equalsIgnoreCase(d.getNodeName())) {
                  minLvl = Integer.valueOf(d.getAttributes().getNamedItem("min").getNodeValue());
                  maxLvl = Integer.valueOf(d.getAttributes().getNamedItem("max").getNodeValue());
               } else if ("delay".equalsIgnoreCase(d.getNodeName())) {
                  minDelay = (long)Integer.valueOf(d.getAttributes().getNamedItem("min").getNodeValue()).intValue();
                  maxDelay = (long)Integer.valueOf(d.getAttributes().getNamedItem("max").getNodeValue()).intValue();
               } else if ("respawn".equalsIgnoreCase(d.getNodeName())) {
                  minRespawn = (long)Integer.valueOf(d.getAttributes().getNamedItem("min").getNodeValue()).intValue();
                  maxRespawn = (long)Integer.valueOf(d.getAttributes().getNamedItem("max").getNodeValue()).intValue();
               }
            }

            this._totalAmount += amount;
            _locations.add(new FakePassiveLocTemplate(id, amount, territory, classes, minLvl, maxLvl, minDelay, maxDelay, minRespawn, maxRespawn));
         }
      }
   }

   private Polygon parsePolygon0(String name, Node n, NamedNodeMap attrs) {
      Polygon temp = new Polygon();

      for(Node cd = n.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
         if ("add".equalsIgnoreCase(cd.getNodeName())) {
            attrs = cd.getAttributes();
            int x = Integer.parseInt(attrs.getNamedItem("x").getNodeValue());
            int y = Integer.parseInt(attrs.getNamedItem("y").getNodeValue());
            int zmin = Integer.parseInt(attrs.getNamedItem("zmin").getNodeValue());
            int zmax = Integer.parseInt(attrs.getNamedItem("zmax").getNodeValue());
            temp.add(x, y).setZmin(zmin).setZmax(zmax);
         }
      }

      if (!temp.validate()) {
         _log.warning("Invalid polygon: " + name + "{" + temp + "}. File: " + this.getClass().getSimpleName());
      }

      return temp;
   }

   private List<Integer> parseExcludedClasses(String classes) {
      if (classes.equals("")) {
         return null;
      } else {
         String[] classType = classes.split(";");
         List<Integer> selected = new ArrayList<>(classType.length);

         for(String classId : classType) {
            selected.add(Integer.parseInt(classId.trim()));
         }

         return selected;
      }
   }

   public FakePassiveLocTemplate getRandomSpawnLoc() {
      List<FakePassiveLocTemplate> locations = new ArrayList<>();

      for(FakePassiveLocTemplate template : _locations) {
         if (template != null && template.getCurrentAmount() < template.getAmount()) {
            locations.add(template);
         }
      }

      return locations.get(Rnd.get(locations.size()));
   }

   public int getTotalAmount() {
      return this._totalAmount;
   }

   public static FakePassiveLocationParser getInstance() {
      return FakePassiveLocationParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final FakePassiveLocationParser _instance = new FakePassiveLocationParser();
   }
}
