package l2e.gameserver.data.parser;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.instance.StaticObjectInstance;
import l2e.gameserver.model.actor.templates.character.CharTemplate;
import l2e.gameserver.model.stats.StatsSet;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class StaticObjectsParser extends DocumentParser {
   private static final Map<Integer, StaticObjectInstance> _StaticObjectsParser = new HashMap<>();

   protected StaticObjectsParser() {
      this.load();
   }

   @Override
   public void load() {
      _StaticObjectsParser.clear();
      this.parseDatapackFile("data/stats/regions/staticObjects.xml");
      this._log.info(this.getClass().getSimpleName() + ": Loaded " + _StaticObjectsParser.size() + " staticObject templates.");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node n = this.getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equalsIgnoreCase(n.getNodeName())) {
            for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
               if ("object".equalsIgnoreCase(d.getNodeName())) {
                  NamedNodeMap attrs = d.getAttributes();
                  StatsSet set = new StatsSet();

                  for(int i = 0; i < attrs.getLength(); ++i) {
                     Node att = attrs.item(i);
                     set.set(att.getNodeName(), att.getNodeValue());
                  }

                  this.addObject(set);
               }
            }
         }
      }
   }

   private void addObject(StatsSet set) {
      StaticObjectInstance obj = new StaticObjectInstance(IdFactory.getInstance().getNextId(), new CharTemplate(new StatsSet()), set.getInteger("id"));
      obj.setType(set.getInteger("type", 0));
      obj.setName(set.getString("name"));
      obj.setMap(set.getString("texture", "none"), set.getInteger("map_x", 0), set.getInteger("map_y", 0));
      Location loc = new Location(set.getInteger("x"), set.getInteger("y"), set.getInteger("z"));
      loc.correctGeoZ(0);
      obj.spawnMe(loc.getX(), loc.getY(), loc.getZ());
      _StaticObjectsParser.put(obj.getObjectId(), obj);
   }

   public Collection<StaticObjectInstance> getStaticObjects() {
      return _StaticObjectsParser.values();
   }

   public static StaticObjectsParser getInstance() {
      return StaticObjectsParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final StaticObjectsParser _instance = new StaticObjectsParser();
   }
}
