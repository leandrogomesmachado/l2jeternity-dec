package l2e.gameserver.data.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import l2e.commons.util.Rnd;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.templates.player.FakeLocTemplate;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class FakeLocationParser extends DocumentParser {
   private static Logger _log = Logger.getLogger(FakeLocationParser.class.getName());
   private static List<FakeLocTemplate> _locations = new ArrayList<>();
   private int _totalAmount = 0;

   protected FakeLocationParser() {
      this.load();
   }

   @Override
   public void load() {
      _locations.clear();
      this.parseDatapackFile("config/mods/fakes/locations.xml");
      _log.info(this.getClass().getSimpleName() + ": Loaded " + _locations.size() + " fake players locations.");
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
            int distance = Integer.valueOf(node.getNamedItem("distance").getNodeValue());
            Location loc = null;
            int minLvl = 1;
            int maxLvl = 1;
            List<Integer> classes = null;

            for(Node d = list.getFirstChild(); d != null; d = d.getNextSibling()) {
               if ("coords".equalsIgnoreCase(d.getNodeName())) {
                  int x = Integer.valueOf(d.getAttributes().getNamedItem("x").getNodeValue());
                  int y = Integer.valueOf(d.getAttributes().getNamedItem("y").getNodeValue());
                  int z = Integer.valueOf(d.getAttributes().getNamedItem("z").getNodeValue());
                  loc = new Location(x, y, z);
               } else if ("class".equalsIgnoreCase(d.getNodeName())) {
                  classes = this.parseExcludedClasses(d.getAttributes().getNamedItem("id").getNodeValue());
               } else if ("level".equalsIgnoreCase(d.getNodeName())) {
                  minLvl = Integer.valueOf(d.getAttributes().getNamedItem("min").getNodeValue());
                  maxLvl = Integer.valueOf(d.getAttributes().getNamedItem("max").getNodeValue());
               }
            }

            this._totalAmount += amount;
            _locations.add(new FakeLocTemplate(id, amount, loc, classes, minLvl, maxLvl, distance));
         }
      }
   }

   public FakeLocTemplate createRndLoc(Location location) {
      int id = _locations.size() + 1;
      int minLvl = Rnd.get(10, 85);
      int distance = Rnd.get(2000, 5000);
      FakeLocTemplate loc = new FakeLocTemplate(id, 1, location, null, minLvl, minLvl + 5, distance);
      _locations.add(loc);
      return loc;
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

   public FakeLocTemplate getRandomSpawnLoc() {
      List<FakeLocTemplate> locations = new ArrayList<>();

      for(FakeLocTemplate template : _locations) {
         if (template != null && template.getCurrentAmount() < template.getAmount()) {
            locations.add(template);
         }
      }

      return locations.get(Rnd.get(locations.size()));
   }

   public int getTotalAmount() {
      return this._totalAmount;
   }

   public static FakeLocationParser getInstance() {
      return FakeLocationParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final FakeLocationParser _instance = new FakeLocationParser();
   }
}
