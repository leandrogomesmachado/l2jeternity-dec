package l2e.gameserver.data.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.fishing.Fish;
import l2e.gameserver.model.stats.StatsSet;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class FishParser extends DocumentParser {
   private static final Map<Integer, Fish> _fishNormal = new HashMap<>();
   private static final Map<Integer, Fish> _fishEasy = new HashMap<>();
   private static final Map<Integer, Fish> _fishHard = new HashMap<>();

   protected FishParser() {
      this.load();
   }

   @Override
   public void load() {
      _fishEasy.clear();
      _fishNormal.clear();
      _fishHard.clear();
      this.parseDatapackFile("data/stats/items/fishing/fishes.xml");
      this._log.info(this.getClass().getSimpleName() + ": Loaded " + (_fishEasy.size() + _fishNormal.size() + _fishHard.size()) + " Fishes.");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node n = this.getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equalsIgnoreCase(n.getNodeName())) {
            for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
               if ("fish".equalsIgnoreCase(d.getNodeName())) {
                  NamedNodeMap attrs = d.getAttributes();
                  StatsSet set = new StatsSet();

                  for(int i = 0; i < attrs.getLength(); ++i) {
                     Node att = attrs.item(i);
                     set.set(att.getNodeName(), att.getNodeValue());
                  }

                  Fish fish = new Fish(set);
                  switch(fish.getFishGrade()) {
                     case 0:
                        _fishEasy.put(fish.getFishId(), fish);
                        break;
                     case 1:
                        _fishNormal.put(fish.getFishId(), fish);
                        break;
                     case 2:
                        _fishHard.put(fish.getFishId(), fish);
                  }
               }
            }
         }
      }
   }

   public List<Fish> getFish(int level, int group, int grade) {
      ArrayList<Fish> result = new ArrayList<>();
      Map<Integer, Fish> fish = null;
      switch(grade) {
         case 0:
            fish = _fishEasy;
            break;
         case 1:
            fish = _fishNormal;
            break;
         case 2:
            fish = _fishHard;
            break;
         default:
            this._log.warning(this.getClass().getSimpleName() + ": Unmanaged fish grade!");
            return result;
      }

      for(Fish f : fish.values()) {
         if (f.getFishLevel() == level && f.getFishGroup() == group) {
            result.add(f);
         }
      }

      if (result.isEmpty()) {
         this._log.warning(this.getClass().getSimpleName() + ": Cannot find any fish for level: " + level + " group: " + group + " and grade: " + grade + "!");
      }

      return result;
   }

   public static FishParser getInstance() {
      return FishParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final FishParser _instance = new FishParser();
   }
}
