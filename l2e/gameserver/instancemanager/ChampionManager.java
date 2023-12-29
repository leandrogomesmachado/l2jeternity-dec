package l2e.gameserver.instancemanager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import l2e.gameserver.Config;
import l2e.gameserver.model.actor.templates.npc.champion.ChampionRewardItem;
import l2e.gameserver.model.actor.templates.npc.champion.ChampionTemplate;
import l2e.gameserver.model.skills.effects.AbnormalEffect;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class ChampionManager {
   protected static final Logger _log = Logger.getLogger(ChampionManager.class.getName());
   private static final ChampionManager _instance = new ChampionManager();
   public boolean ENABLE_EXT_CHAMPION_MODE = false;
   public int EXT_CHAMPION_MODE_MAX_ROLL_VALUE = 0;
   private final List<ChampionTemplate> _championTemplates = new ArrayList<>();

   public static final ChampionManager getInstance() {
      return _instance;
   }

   public ChampionManager() {
      this._championTemplates.clear();
      this.championParser();
   }

   private void championParser() {
      try {
         File file = new File(Config.DATAPACK_ROOT + "/config/mods/championTemplate.xml");
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setValidating(false);
         factory.setIgnoringComments(true);
         Document doc = factory.newDocumentBuilder().parse(file);
         Node first = doc.getFirstChild();
         if (first != null && "list".equalsIgnoreCase(first.getNodeName())) {
            NamedNodeMap attrs = first.getAttributes();
            Node att = attrs.getNamedItem("enabled");

            try {
               this.ENABLE_EXT_CHAMPION_MODE = Boolean.parseBoolean(att.getNodeValue());
               att = attrs.getNamedItem("maxRollValue");
               this.EXT_CHAMPION_MODE_MAX_ROLL_VALUE = Integer.parseInt(att.getNodeValue());
            } catch (Exception var19) {
               _log.warning("ChampionManager: Failed to load initial list params, mode skipped.");
               var19.printStackTrace();
            }

            if (!this.ENABLE_EXT_CHAMPION_MODE) {
               return;
            }

            for(Node n = first.getFirstChild(); n != null; n = n.getNextSibling()) {
               if ("champion".equalsIgnoreCase(n.getNodeName())) {
                  ChampionTemplate ct = new ChampionTemplate();
                  attrs = n.getAttributes();
                  if ((att = attrs.getNamedItem("minChance")) == null) {
                     _log.warning("ChampionManager: Missing minChance, skipping");
                  } else {
                     ct.minChance = Integer.parseInt(att.getNodeValue());
                     if ((att = attrs.getNamedItem("maxChance")) == null) {
                        _log.warning("ChampionManager: Missing maxChance, skipping");
                     } else {
                        ct.maxChance = Integer.parseInt(att.getNodeValue());
                        if ((att = attrs.getNamedItem("minLevel")) == null) {
                           _log.warning("ChampionManager: Missing minLevel, skipping");
                        } else {
                           ct.minLevel = Integer.parseInt(att.getNodeValue());
                           if ((att = attrs.getNamedItem("maxLevel")) == null) {
                              _log.warning("ChampionManager: Missing maxLevel, skipping");
                           } else {
                              ct.maxLevel = Integer.parseInt(att.getNodeValue());

                              for(Node cd = n.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
                                 try {
                                    String e = cd.getNodeName();
                                    switch(e) {
                                       case "setByte":
                                          ct.getClass()
                                             .getField(cd.getAttributes().item(0).getNodeName())
                                             .setByte(ct, Byte.parseByte(cd.getAttributes().item(0).getNodeValue()));
                                          break;
                                       case "setBoolean":
                                          ct.getClass()
                                             .getField(cd.getAttributes().item(0).getNodeName())
                                             .setBoolean(ct, Boolean.parseBoolean(cd.getAttributes().item(0).getNodeValue()));
                                          break;
                                       case "setDouble":
                                          ct.getClass()
                                             .getField(cd.getAttributes().item(0).getNodeName())
                                             .setDouble(ct, Double.parseDouble(cd.getAttributes().item(0).getNodeValue()));
                                          break;
                                       case "setFloat":
                                          ct.getClass()
                                             .getField(cd.getAttributes().item(0).getNodeName())
                                             .setFloat(ct, Float.parseFloat(cd.getAttributes().item(0).getNodeValue()));
                                          break;
                                       case "setInt":
                                          ct.getClass()
                                             .getField(cd.getAttributes().item(0).getNodeName())
                                             .setInt(ct, Integer.parseInt(cd.getAttributes().item(0).getNodeValue()));
                                          break;
                                       case "setLong":
                                          ct.getClass()
                                             .getField(cd.getAttributes().item(0).getNodeName())
                                             .setLong(ct, Long.parseLong(cd.getAttributes().item(0).getNodeValue()));
                                          break;
                                       case "setString":
                                          ct.getClass().getField(cd.getAttributes().item(0).getNodeName()).set(ct, cd.getAttributes().item(0).getNodeValue());
                                          break;
                                       case "item":
                                          int itemId = Integer.parseInt(cd.getAttributes().getNamedItem("itemId").getNodeValue());
                                          int minCount = Integer.parseInt(cd.getAttributes().getNamedItem("minCount").getNodeValue());
                                          int maxCount = Integer.parseInt(cd.getAttributes().getNamedItem("maxCount").getNodeValue());
                                          int dropChance = Integer.parseInt(cd.getAttributes().getNamedItem("dropChance").getNodeValue());
                                          ct.rewards.add(new ChampionRewardItem(itemId, minCount, maxCount, dropChance));
                                          break;
                                       case "abnormalEffect":
                                          AbnormalEffect ae = AbnormalEffect.getByName(cd.getAttributes().getNamedItem("name").getNodeValue());
                                          if (ae != null) {
                                             ct.abnormalEffect.add(ae);
                                          }
                                    }
                                 } catch (NoSuchFieldException var17) {
                                    _log.warning(
                                       "ChampionManager: The variable ["
                                          + att.getNodeName()
                                          + "] which is set in the XML config was not found in the java file."
                                    );
                                    var17.printStackTrace();
                                 } catch (Exception var18) {
                                    _log.warning("ChampionManager: A problem occured while setting a value to the variable [" + att.getNodeName() + "]");
                                    var18.printStackTrace();
                                 }
                              }

                              this._championTemplates.add(ct);
                           }
                        }
                     }
                  }
               }
            }
         }
      } catch (Exception var20) {
         this.ENABLE_EXT_CHAMPION_MODE = false;
         _log.log(Level.WARNING, "ChampionManager: Failed to parse xml: " + var20.getMessage(), (Throwable)var20);
      }

      _log.info("ChampionManager: Loaded " + this._championTemplates.size() + " champion templates.");
   }

   public List<ChampionTemplate> getChampionTemplates() {
      return this._championTemplates;
   }
}
