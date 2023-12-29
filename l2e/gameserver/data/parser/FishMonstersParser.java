package l2e.gameserver.data.parser;

import java.util.HashMap;
import java.util.Map;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.fishing.FishingMonster;
import l2e.gameserver.model.stats.StatsSet;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class FishMonstersParser extends DocumentParser {
   private static final Map<Integer, FishingMonster> _fishingMonstersData = new HashMap<>();

   protected FishMonstersParser() {
      this.load();
   }

   @Override
   public void load() {
      _fishingMonstersData.clear();
      this.parseDatapackFile("data/stats/items/fishing/fishingMonsters.xml");
      this._log.info(this.getClass().getSimpleName() + ": Loaded " + _fishingMonstersData.size() + " fishing monsters.");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node n = this.getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equalsIgnoreCase(n.getNodeName())) {
            for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
               if ("fishingMonster".equalsIgnoreCase(d.getNodeName())) {
                  NamedNodeMap attrs = d.getAttributes();
                  StatsSet set = new StatsSet();

                  for(int i = 0; i < attrs.getLength(); ++i) {
                     Node att = attrs.item(i);
                     set.set(att.getNodeName(), att.getNodeValue());
                  }

                  FishingMonster fishingMonster = new FishingMonster(set);
                  _fishingMonstersData.put(fishingMonster.getFishingMonsterId(), fishingMonster);
               }
            }
         }
      }
   }

   public FishingMonster getFishingMonster(int lvl) {
      for(FishingMonster fishingMonster : _fishingMonstersData.values()) {
         if (lvl >= fishingMonster.getUserMinLevel() && lvl <= fishingMonster.getUserMaxLevel()) {
            return fishingMonster;
         }
      }

      return null;
   }

   public FishingMonster getFishingMonsterById(int id) {
      return _fishingMonstersData.containsKey(id) ? _fishingMonstersData.get(id) : null;
   }

   public static FishMonstersParser getInstance() {
      return FishMonstersParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final FishMonstersParser _instance = new FishMonstersParser();
   }
}
