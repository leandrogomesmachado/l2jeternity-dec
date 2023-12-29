package l2e.gameserver.data.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.transform.Transform;
import l2e.gameserver.model.actor.transform.TransformLevelData;
import l2e.gameserver.model.actor.transform.TransformTemplate;
import l2e.gameserver.model.holders.AdditionalItemHolder;
import l2e.gameserver.model.holders.AdditionalSkillHolder;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.stats.StatsSet;
import l2e.gameserver.network.serverpackets.ExBasicActionList;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class TransformParser extends DocumentParser {
   private final Map<Integer, Transform> _TransformParser = new HashMap<>();

   protected TransformParser() {
      this.load();
   }

   @Override
   public synchronized void load() {
      this._TransformParser.clear();
      this.parseDirectory("data/stats/transformations", false);
      this._log.log(Level.INFO, this.getClass().getSimpleName() + ": Loaded: " + this._TransformParser.size() + " transform templates.");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node n = this.getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equalsIgnoreCase(n.getNodeName())) {
            for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
               if ("transform".equalsIgnoreCase(d.getNodeName())) {
                  NamedNodeMap attrs = d.getAttributes();
                  StatsSet set = new StatsSet();

                  for(int i = 0; i < attrs.getLength(); ++i) {
                     Node att = attrs.item(i);
                     set.set(att.getNodeName(), att.getNodeValue());
                  }

                  Transform transform = new Transform(set);

                  for(Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
                     boolean isMale = "Male".equalsIgnoreCase(cd.getNodeName());
                     if ("Male".equalsIgnoreCase(cd.getNodeName()) || "Female".equalsIgnoreCase(cd.getNodeName())) {
                        TransformTemplate templateData = null;

                        for(Node z = cd.getFirstChild(); z != null; z = z.getNextSibling()) {
                           String var11 = z.getNodeName();
                           switch(var11) {
                              case "common":
                                 Node s = z.getFirstChild();

                                 while(s != null) {
                                    String var33 = s.getNodeName();
                                    switch(var33) {
                                       case "base":
                                       case "stats":
                                       case "defense":
                                       case "magicDefense":
                                       case "collision":
                                       case "moving":
                                          attrs = s.getAttributes();

                                          for(int i = 0; i < attrs.getLength(); ++i) {
                                             Node att = attrs.item(i);
                                             set.set(att.getNodeName(), att.getNodeValue());
                                          }
                                       default:
                                          s = s.getNextSibling();
                                    }
                                 }

                                 templateData = new TransformTemplate(set);
                                 transform.setTemplate(isMale, templateData);
                                 break;
                              case "skills":
                                 if (templateData == null) {
                                    templateData = new TransformTemplate(set);
                                    transform.setTemplate(isMale, templateData);
                                 }

                                 for(Node s = z.getFirstChild(); s != null; s = s.getNextSibling()) {
                                    if ("skill".equals(s.getNodeName())) {
                                       attrs = s.getAttributes();
                                       int skillId = parseInt(attrs, "id");
                                       int skillLevel = parseInt(attrs, "level");
                                       templateData.addSkill(new SkillHolder(skillId, skillLevel));
                                    }
                                 }
                                 break;
                              case "actions":
                                 if (templateData == null) {
                                    templateData = new TransformTemplate(set);
                                    transform.setTemplate(isMale, templateData);
                                 }

                                 set.set("actions", z.getTextContent());
                                 int[] actions = set.getIntegerArray("actions", " ");
                                 templateData.setBasicActionList(new ExBasicActionList(actions));
                                 break;
                              case "additionalSkills":
                                 if (templateData == null) {
                                    templateData = new TransformTemplate(set);
                                    transform.setTemplate(isMale, templateData);
                                 }

                                 for(Node s = z.getFirstChild(); s != null; s = s.getNextSibling()) {
                                    if ("skill".equals(s.getNodeName())) {
                                       attrs = s.getAttributes();
                                       int skillId = parseInt(attrs, "id");
                                       int skillLevel = parseInt(attrs, "level");
                                       int minLevel = parseInt(attrs, "minLevel");
                                       templateData.addAdditionalSkill(new AdditionalSkillHolder(skillId, skillLevel, minLevel));
                                    }
                                 }
                                 break;
                              case "items":
                                 if (templateData == null) {
                                    templateData = new TransformTemplate(set);
                                    transform.setTemplate(isMale, templateData);
                                 }

                                 for(Node s = z.getFirstChild(); s != null; s = s.getNextSibling()) {
                                    if ("item".equals(s.getNodeName())) {
                                       attrs = s.getAttributes();
                                       int itemId = parseInt(attrs, "id");
                                       boolean allowed = parseBoolean(attrs, "allowed");
                                       templateData.addAdditionalItem(new AdditionalItemHolder(itemId, allowed));
                                    }
                                 }
                                 break;
                              case "levels":
                                 if (templateData == null) {
                                    templateData = new TransformTemplate(set);
                                    transform.setTemplate(isMale, templateData);
                                 }

                                 StatsSet levelsSet = new StatsSet();

                                 for(Node s = z.getFirstChild(); s != null; s = s.getNextSibling()) {
                                    if ("level".equals(s.getNodeName())) {
                                       attrs = s.getAttributes();

                                       for(int i = 0; i < attrs.getLength(); ++i) {
                                          Node att = attrs.item(i);
                                          levelsSet.set(att.getNodeName(), att.getNodeValue());
                                       }
                                    }
                                 }

                                 templateData.addLevelData(new TransformLevelData(levelsSet));
                           }
                        }
                     }
                  }

                  this._TransformParser.put(transform.getId(), transform);
               }
            }
         }
      }
   }

   public Transform getTransform(int id) {
      return this._TransformParser.get(id);
   }

   public boolean transformPlayer(int id, Player player) {
      Transform transform = this.getTransform(id);
      if (transform != null) {
         player.transform(transform);
      }

      return transform != null;
   }

   public static TransformParser getInstance() {
      return TransformParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final TransformParser _instance = new TransformParser();
   }
}
