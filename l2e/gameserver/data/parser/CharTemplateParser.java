package l2e.gameserver.data.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.templates.player.PcTemplate;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.stats.StatsSet;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class CharTemplateParser extends DocumentParser {
   private static final Logger _log = Logger.getLogger(CharTemplateParser.class.getName());
   private static final Map<ClassId, PcTemplate> _charTemplates = new HashMap<>();

   protected CharTemplateParser() {
      this.load();
   }

   @Override
   public void load() {
      this.parseDirectory("data/stats/chars/classes/", false);
      _log.info(this.getClass().getSimpleName() + ": Loaded " + _charTemplates.size() + " character templates.");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      int classId = 0;

      for(Node n = this.getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equalsIgnoreCase(n.getNodeName())) {
            for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
               if ("classId".equalsIgnoreCase(d.getNodeName())) {
                  classId = Integer.parseInt(d.getTextContent());
               } else if ("staticData".equalsIgnoreCase(d.getNodeName())) {
                  StatsSet set = new StatsSet();
                  set.set("classId", classId);
                  List<Location> creationPoints = new ArrayList<>();

                  for(Node nd = d.getFirstChild(); nd != null; nd = nd.getNextSibling()) {
                     if (!nd.getNodeName().equals("#text")) {
                        if (nd.getChildNodes().getLength() > 1) {
                           for(Node cnd = nd.getFirstChild(); cnd != null; cnd = cnd.getNextSibling()) {
                              if (nd.getNodeName().equalsIgnoreCase("collisionMale")) {
                                 if (cnd.getNodeName().equalsIgnoreCase("radius")) {
                                    set.set("collision_radius", cnd.getTextContent());
                                 } else if (cnd.getNodeName().equalsIgnoreCase("height")) {
                                    set.set("collision_height", cnd.getTextContent());
                                 }
                              }

                              if ("node".equalsIgnoreCase(cnd.getNodeName())) {
                                 NamedNodeMap attrs = cnd.getAttributes();
                                 creationPoints.add(new Location(parseInt(attrs, "x"), parseInt(attrs, "y"), parseInt(attrs, "z")));
                              } else if ("walk".equalsIgnoreCase(cnd.getNodeName())) {
                                 set.set("baseWalkSpd", cnd.getTextContent());
                              } else if ("run".equalsIgnoreCase(cnd.getNodeName())) {
                                 set.set("baseRunSpd", cnd.getTextContent());
                              } else if ("slowSwim".equals(cnd.getNodeName())) {
                                 set.set("baseSwimWalkSpd", cnd.getTextContent());
                              } else if ("fastSwim".equals(cnd.getNodeName())) {
                                 set.set("baseSwimRunSpd", cnd.getTextContent());
                              } else if (!cnd.getNodeName().equals("#text")) {
                                 set.set(nd.getNodeName() + cnd.getNodeName(), cnd.getTextContent());
                              }
                           }
                        } else {
                           set.set(nd.getNodeName(), nd.getTextContent());
                        }
                     }
                  }

                  set.set(
                     "basePDef",
                     set.getInteger("basePDefchest", 0)
                        + set.getInteger("basePDeflegs", 0)
                        + set.getInteger("basePDefhead", 0)
                        + set.getInteger("basePDeffeet", 0)
                        + set.getInteger("basePDefgloves", 0)
                        + set.getInteger("basePDefunderwear", 0)
                        + set.getInteger("basePDefcloak", 0)
                  );
                  set.set(
                     "baseMDef",
                     set.getInteger("baseMDefrear", 0)
                        + set.getInteger("baseMDeflear", 0)
                        + set.getInteger("baseMDefrfinger", 0)
                        + set.getInteger("baseMDefrfinger", 0)
                        + set.getInteger("baseMDefneck", 0)
                  );
                  PcTemplate ct = new PcTemplate(set, creationPoints);
                  _charTemplates.put(ClassId.getClassId(classId), ct);
               } else if ("lvlUpgainData".equalsIgnoreCase(d.getNodeName())) {
                  for(Node lvlNode = d.getFirstChild(); lvlNode != null; lvlNode = lvlNode.getNextSibling()) {
                     if ("level".equalsIgnoreCase(lvlNode.getNodeName())) {
                        NamedNodeMap attrs = lvlNode.getAttributes();
                        int level = parseInt(attrs, "val");

                        for(Node valNode = lvlNode.getFirstChild(); valNode != null; valNode = valNode.getNextSibling()) {
                           String nodeName = valNode.getNodeName();
                           if ((nodeName.startsWith("hp") || nodeName.startsWith("mp") || nodeName.startsWith("cp"))
                              && _charTemplates.containsKey(ClassId.getClassId(classId))) {
                              _charTemplates.get(ClassId.getClassId(classId)).setUpgainValue(nodeName, level, Double.parseDouble(valNode.getTextContent()));
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public PcTemplate getTemplate(ClassId classId) {
      return _charTemplates.get(classId);
   }

   public PcTemplate getTemplate(int classId) {
      return _charTemplates.get(ClassId.getClassId(classId));
   }

   public static final CharTemplateParser getInstance() {
      return CharTemplateParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final CharTemplateParser _instance = new CharTemplateParser();
   }
}
