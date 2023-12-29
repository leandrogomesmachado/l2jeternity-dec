package l2e.gameserver.data.parser;

import java.util.HashMap;
import java.util.Map;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.actor.templates.ArmorSetTemplate;
import l2e.gameserver.model.holders.SkillHolder;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class ArmorSetsParser extends DocumentParser {
   private static final Map<Integer, ArmorSetTemplate> _armorSets = new HashMap<>();

   protected ArmorSetsParser() {
      this.load();
   }

   @Override
   public void load() {
      _armorSets.clear();
      this.parseDirectory("data/stats/items/armorsets", false);
      this._log.info(this.getClass().getSimpleName() + ": Loaded " + _armorSets.size() + " armor sets.");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node n = this.getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equalsIgnoreCase(n.getNodeName())) {
            for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
               if ("set".equalsIgnoreCase(d.getNodeName())) {
                  ArmorSetTemplate set = new ArmorSetTemplate();

                  for(Node a = d.getFirstChild(); a != null; a = a.getNextSibling()) {
                     NamedNodeMap attrs = a.getAttributes();
                     String var6 = a.getNodeName();
                     switch(var6) {
                        case "chest":
                           set.addChest(parseInt(attrs, "id"));
                           break;
                        case "feet":
                           set.addFeet(parseInt(attrs, "id"));
                           break;
                        case "gloves":
                           set.addGloves(parseInt(attrs, "id"));
                           break;
                        case "head":
                           set.addHead(parseInt(attrs, "id"));
                           break;
                        case "legs":
                           set.addLegs(parseInt(attrs, "id"));
                           break;
                        case "shield":
                           set.addShield(parseInt(attrs, "id"));
                           break;
                        case "skill": {
                           int skillId = parseInt(attrs, "id");
                           int skillLevel = parseInt(attrs, "level");
                           set.addSkill(new SkillHolder(skillId, skillLevel));
                           break;
                        }
                        case "shield_skill": {
                           int skillId = parseInt(attrs, "id");
                           int skillLevel = parseInt(attrs, "level");
                           set.addShieldSkill(new SkillHolder(skillId, skillLevel));
                           break;
                        }
                        case "enchant6skill": {
                           int skillId = parseInt(attrs, "id");
                           int skillLevel = parseInt(attrs, "level");
                           set.addEnchant6Skill(new SkillHolder(skillId, skillLevel));
                           break;
                        }
                        case "enchantBy": {
                           int enchLvl = parseInteger(attrs, "level");
                           int skillId = parseInteger(attrs, "skillId");
                           int skillLevel = parseInteger(attrs, "skillLvl");
                           set.addEnchantByLevel(enchLvl, new SkillHolder(skillId, skillLevel));
                           break;
                        }
                        case "con":
                           set.addCon(parseInt(attrs, "val"));
                           break;
                        case "dex":
                           set.addDex(parseInt(attrs, "val"));
                           break;
                        case "str":
                           set.addStr(parseInt(attrs, "val"));
                           break;
                        case "men":
                           set.addMen(parseInt(attrs, "val"));
                           break;
                        case "wit":
                           set.addWit(parseInt(attrs, "val"));
                           break;
                        case "int":
                           set.addInt(parseInt(attrs, "val"));
                     }
                  }

                  _armorSets.put(set.getChestId(), set);
               }
            }
         }
      }
   }

   public boolean isArmorSet(int chestId) {
      return _armorSets.containsKey(chestId);
   }

   public ArmorSetTemplate getSet(int chestId) {
      return _armorSets.get(chestId);
   }

   public static ArmorSetsParser getInstance() {
      return ArmorSetsParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final ArmorSetsParser _instance = new ArmorSetsParser();
   }
}
