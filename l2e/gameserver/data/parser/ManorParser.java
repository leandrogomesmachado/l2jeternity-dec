package l2e.gameserver.data.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.model.Seed;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.stats.StatsSet;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class ManorParser extends DocumentParser {
   private static Logger _log = Logger.getLogger(ManorParser.class.getName());
   private static Map<Integer, Seed> _seeds = new HashMap<>();

   protected ManorParser() {
      this.load();
   }

   @Override
   public void load() {
      _seeds.clear();
      this.parseDatapackFile("data/stats/items/seeds.xml");
      _log.log(Level.INFO, this.getClass().getSimpleName() + ": Loaded: " + _seeds.size() + " seeds");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node n = this.getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equalsIgnoreCase(n.getNodeName())) {
            for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
               if ("castle".equalsIgnoreCase(d.getNodeName())) {
                  int castleId = parseInt(d.getAttributes(), "id");

                  for(Node c = d.getFirstChild(); c != null; c = c.getNextSibling()) {
                     if ("crop".equalsIgnoreCase(c.getNodeName())) {
                        StatsSet set = new StatsSet();
                        set.set("castleId", castleId);
                        NamedNodeMap attrs = c.getAttributes();

                        for(int i = 0; i < attrs.getLength(); ++i) {
                           Node att = attrs.item(i);
                           set.set(att.getNodeName(), att.getNodeValue());
                        }

                        Seed seed = new Seed(set);
                        _seeds.put(seed.getSeedId(), seed);
                     }
                  }
               }
            }
         }
      }
   }

   public List<Integer> getAllCrops() {
      List<Integer> crops = new ArrayList<>();

      for(Seed seed : _seeds.values()) {
         if (!crops.contains(seed.getCropId()) && seed.getCropId() != 0 && !crops.contains(seed.getCropId())) {
            crops.add(seed.getCropId());
         }
      }

      return crops;
   }

   public int getSeedBasicPrice(int seedId) {
      Item seedItem = ItemsParser.getInstance().getTemplate(seedId);
      return seedItem != null ? seedItem.getReferencePrice() : 0;
   }

   public int getSeedBasicPriceByCrop(int cropId) {
      for(Seed seed : _seeds.values()) {
         if (seed.getCropId() == cropId) {
            return this.getSeedBasicPrice(seed.getSeedId());
         }
      }

      return 0;
   }

   public int getCropBasicPrice(int cropId) {
      Item cropItem = ItemsParser.getInstance().getTemplate(cropId);
      return cropItem != null ? cropItem.getReferencePrice() : 0;
   }

   public int getMatureCrop(int cropId) {
      for(Seed seed : _seeds.values()) {
         if (seed.getCropId() == cropId) {
            return seed.getMatureId();
         }
      }

      return 0;
   }

   public long getSeedBuyPrice(int seedId) {
      long buyPrice = (long)this.getSeedBasicPrice(seedId);
      return buyPrice > 0L ? buyPrice : 1L;
   }

   public int getSeedMinLevel(int seedId) {
      Seed seed = _seeds.get(seedId);
      return seed != null ? seed.getLevel() - 5 : -1;
   }

   public int getSeedMaxLevel(int seedId) {
      Seed seed = _seeds.get(seedId);
      return seed != null ? seed.getLevel() + 5 : -1;
   }

   public int getSeedLevelByCrop(int cropId) {
      for(Seed seed : _seeds.values()) {
         if (seed.getCropId() == cropId) {
            return seed.getLevel();
         }
      }

      return 0;
   }

   public int getSeedLevel(int seedId) {
      Seed seed = _seeds.get(seedId);
      return seed != null ? seed.getLevel() : -1;
   }

   public boolean isAlternative(int seedId) {
      Seed seed = _seeds.get(seedId);
      return seed != null ? seed.isAlternative() : false;
   }

   public int getCropType(int seedId) {
      Seed seed = _seeds.get(seedId);
      return seed != null ? seed.getCropId() : -1;
   }

   public int getRewardItem(int cropId, int type) {
      for(Seed seed : _seeds.values()) {
         if (seed.getCropId() == cropId) {
            return seed.getReward(type);
         }
      }

      return -1;
   }

   public int getRewardItemBySeed(int seedId, int type) {
      Seed seed = _seeds.get(seedId);
      return seed != null ? seed.getReward(type) : 0;
   }

   public List<Integer> getCropsForCastle(int castleId) {
      List<Integer> crops = new ArrayList<>();

      for(Seed seed : _seeds.values()) {
         if (seed.getCastleId() == castleId && !crops.contains(seed.getCropId())) {
            crops.add(seed.getCropId());
         }
      }

      return crops;
   }

   public List<Integer> getSeedsForCastle(int castleId) {
      List<Integer> seedsID = new ArrayList<>();

      for(Seed seed : _seeds.values()) {
         if (seed.getCastleId() == castleId && !seedsID.contains(seed.getSeedId())) {
            seedsID.add(seed.getSeedId());
         }
      }

      return seedsID;
   }

   public int getCastleIdForSeed(int seedId) {
      Seed seed = _seeds.get(seedId);
      return seed != null ? seed.getCastleId() : 0;
   }

   public int getSeedSaleLimit(int seedId) {
      Seed seed = _seeds.get(seedId);
      if (seed != null) {
         TerritoryWarManager.Territory territory = CastleManager.getInstance().getCastleById(seed.getCastleId()).getTerritory();
         return territory != null && territory.getLordObjectId() != 0 ? (int)((double)seed.getSeedLimit() * 1.1) : seed.getSeedLimit();
      } else {
         return 0;
      }
   }

   public int getCropPuchaseLimit(int cropId) {
      for(Seed seed : _seeds.values()) {
         if (seed.getCropId() == cropId) {
            TerritoryWarManager.Territory territory = CastleManager.getInstance().getCastleById(seed.getCastleId()).getTerritory();
            if (territory != null && territory.getLordObjectId() != 0) {
               return (int)((double)seed.getCropLimit() * 1.1);
            }

            return seed.getCropLimit();
         }
      }

      return 0;
   }

   public static ManorParser getInstance() {
      return ManorParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final ManorParser _instance = new ManorParser();
   }
}
