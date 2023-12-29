package l2e.scripts.hellbound;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilderFactory;
import l2e.gameserver.Announcements;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.DoorParser;
import l2e.gameserver.instancemanager.HellboundManager;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.DoorInstance;
import l2e.gameserver.model.quest.Quest;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class Engine extends Quest implements Runnable {
   private static final String pointsInfoFile = "data/stats/npcs/hellboundTrustPoints.xml";
   private static final int UPDATE_INTERVAL = 10000;
   private static final int[][] DOOR_LIST = new int[][]{{19250001, 5}, {19250002, 5}, {20250001, 9}, {20250002, 7}};
   private static final int[] MAX_TRUST = new int[]{0, 300000, 600000, 1000000, 1010000, 1400000, 1490000, 2000000, 2000001, 2500000, 4000000, 0};
   private static final String ANNOUNCE = "Hellbound now has reached level: %lvl%";
   private int _cachedLevel = -1;
   private static Map<Integer, Engine.PointsInfoHolder> pointsInfo = new HashMap<>();

   private final void onLevelChange(int newLevel) {
      try {
         HellboundManager.getInstance().setMaxTrust(MAX_TRUST[newLevel]);
         HellboundManager.getInstance().setMinTrust(MAX_TRUST[newLevel - 1]);
      } catch (ArrayIndexOutOfBoundsException var8) {
         HellboundManager.getInstance().setMaxTrust(0);
         HellboundManager.getInstance().setMinTrust(0);
      }

      HellboundManager.getInstance().updateTrust(0, false);
      HellboundManager.getInstance().doSpawn();

      for(int[] doorData : DOOR_LIST) {
         try {
            DoorInstance door = DoorParser.getInstance().getDoor(doorData[0]);
            if (door.getOpen()) {
               if (newLevel < doorData[1]) {
                  door.closeMe();
               }
            } else if (newLevel >= doorData[1]) {
               door.openMe();
            }
         } catch (Exception var7) {
            _log.log(Level.WARNING, "Hellbound doors problem!", (Throwable)var7);
         }
      }

      if (this._cachedLevel > 0) {
         Announcements.getInstance().announceToAll("Hellbound now has reached level: %lvl%".replace("%lvl%", String.valueOf(newLevel)));
         if (Config.DEBUG) {
            _log.info("HellboundEngine: New Level: " + newLevel);
         }
      }

      this._cachedLevel = newLevel;
   }

   private void loadPointsInfoData() {
      File file = new File(Config.DATAPACK_ROOT, "data/stats/npcs/hellboundTrustPoints.xml");
      if (!file.exists()) {
         _log.warning("Cannot locate points info file: data/stats/npcs/hellboundTrustPoints.xml");
      } else {
         Document doc = null;

         try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setIgnoringComments(true);
            doc = factory.newDocumentBuilder().parse(file);
         } catch (Exception var12) {
            _log.log(Level.WARNING, "Could not parse data/stats/npcs/hellboundTrustPoints.xml file: " + var12.getMessage(), (Throwable)var12);
            return;
         }

         for(Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
            if ("list".equalsIgnoreCase(n.getNodeName())) {
               for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
                  if ("npc".equalsIgnoreCase(d.getNodeName())) {
                     NamedNodeMap attrs = d.getAttributes();
                     Node att = attrs.getNamedItem("id");
                     if (att == null) {
                        _log.severe("[Hellbound Trust Points Info] Missing NPC ID, skipping record");
                     } else {
                        int npcId = Integer.parseInt(att.getNodeValue());
                        att = attrs.getNamedItem("points");
                        if (att == null) {
                           _log.severe("[Hellbound Trust Points Info] Missing reward point info for NPC ID " + npcId + ", skipping record");
                        } else {
                           int points = Integer.parseInt(att.getNodeValue());
                           att = attrs.getNamedItem("minHellboundLvl");
                           if (att == null) {
                              _log.severe("[Hellbound Trust Points Info] Missing minHellboundLvl info for NPC ID " + npcId + ", skipping record");
                           } else {
                              int minHbLvl = Integer.parseInt(att.getNodeValue());
                              att = attrs.getNamedItem("maxHellboundLvl");
                              if (att == null) {
                                 _log.severe("[Hellbound Trust Points Info] Missing maxHellboundLvl info for NPC ID " + npcId + ", skipping record");
                              } else {
                                 int maxHbLvl = Integer.parseInt(att.getNodeValue());
                                 att = attrs.getNamedItem("lowestTrustLimit");
                                 int lowestTrustLimit = 0;
                                 if (att != null) {
                                    lowestTrustLimit = Integer.parseInt(att.getNodeValue());
                                 }

                                 pointsInfo.put(npcId, new Engine.PointsInfoHolder(points, minHbLvl, maxHbLvl, lowestTrustLimit));
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         if (Config.DEBUG) {
            _log.info("HellboundEngine: Loaded: " + pointsInfo.size() + " trust point reward data");
         }
      }
   }

   @Override
   public void run() {
      int level = HellboundManager.getInstance().getLevel();
      if (level > 0 && level == this._cachedLevel) {
         if (HellboundManager.getInstance().getTrust() == HellboundManager.getInstance().getMaxTrust() && level != 4) {
            HellboundManager.getInstance().setLevel(++level);
            this.onLevelChange(level);
         }
      } else {
         this.onLevelChange(level);
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      int npcId = npc.getId();
      if (pointsInfo.containsKey(npcId)) {
         Engine.PointsInfoHolder npcInfo = pointsInfo.get(npcId);
         if (HellboundManager.getInstance().getLevel() >= npcInfo.minHbLvl
            && HellboundManager.getInstance().getLevel() <= npcInfo.maxHbLvl
            && (npcInfo.lowestTrustLimit == 0 || HellboundManager.getInstance().getTrust() > npcInfo.lowestTrustLimit)) {
            HellboundManager.getInstance().updateTrust(npcInfo.pointsAmount, true);
         }

         if (npc.getId() == 18465 && HellboundManager.getInstance().getLevel() == 4) {
            HellboundManager.getInstance().setLevel(5);
         }
      }

      return super.onKill(npc, killer, isSummon);
   }

   public Engine(int questId, String name, String descr) {
      super(questId, name, descr);
      HellboundManager.getInstance().registerEngine(this, 10000);
      this.loadPointsInfoData();

      for(int npcId : pointsInfo.keySet()) {
         this.addKillId(npcId);
      }

      if (Config.DEBUG) {
         _log.info("HellboundEngine: Mode: levels 0-3");
         _log.info("HellboundEngine: Level: " + HellboundManager.getInstance().getLevel());
         _log.info("HellboundEngine: Trust: " + HellboundManager.getInstance().getTrust());
      }

      if (HellboundManager.getInstance().isLocked()) {
         if (Config.DEBUG) {
            _log.info("HellboundEngine: State: locked");
         }
      } else if (Config.DEBUG) {
         _log.info("HellboundEngine: State: unlocked");
      }
   }

   public static void main(String[] args) {
      new Engine(-1, Engine.class.getSimpleName(), "hellbound");
   }

   private class PointsInfoHolder {
      protected int pointsAmount;
      protected int minHbLvl;
      protected int maxHbLvl;
      protected int lowestTrustLimit;

      protected PointsInfoHolder(int points, int min, int max, int trust) {
         this.pointsAmount = points;
         this.minHbLvl = min;
         this.maxHbLvl = max;
         this.lowestTrustLimit = trust;
      }
   }
}
