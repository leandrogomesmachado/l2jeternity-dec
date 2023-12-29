package l2e.gameserver.data.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import l2e.gameserver.Config;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.EnchantSkillGroup;
import l2e.gameserver.model.EnchantSkillLearn;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.stats.StatsSet;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class EnchantSkillGroupsParser extends DocumentParser {
   public static final int NORMAL_ENCHANT_COST_MULTIPLIER = Config.NORMAL_ENCHANT_COST_MULTIPLIER;
   public static final int SAFE_ENCHANT_COST_MULTIPLIER = Config.SAFE_ENCHANT_COST_MULTIPLIER;
   public static final int NORMAL_ENCHANT_BOOK = 6622;
   public static final int SAFE_ENCHANT_BOOK = 9627;
   public static final int CHANGE_ENCHANT_BOOK = 9626;
   public static final int UNTRAIN_ENCHANT_BOOK = 9625;
   private final Map<Integer, EnchantSkillGroup> _enchantSkillGroups = new HashMap<>();
   private final Map<Integer, EnchantSkillLearn> _enchantSkillTrees = new HashMap<>();

   protected EnchantSkillGroupsParser() {
      this.load();
   }

   @Override
   public void load() {
      this._enchantSkillGroups.clear();
      this._enchantSkillTrees.clear();
      this.parseDatapackFile("data/stats/enchanting/enchantSkillGroups.xml");
      int routes = 0;

      for(EnchantSkillGroup group : this._enchantSkillGroups.values()) {
         routes += group.getEnchantGroupDetails().size();
      }

      if (Config.DEBUG) {
         this._log.info("SkillTreesParser: Loaded " + this._enchantSkillGroups.size() + " groups and " + routes + " routes.");
      }
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      int id = 0;

      for(Node n = this.getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equalsIgnoreCase(n.getNodeName())) {
            for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
               if ("group".equalsIgnoreCase(d.getNodeName())) {
                  NamedNodeMap attrs = d.getAttributes();
                  id = parseInt(attrs, "id");
                  EnchantSkillGroup group = this._enchantSkillGroups.get(id);
                  if (group == null) {
                     group = new EnchantSkillGroup(id);
                     this._enchantSkillGroups.put(id, group);
                  }

                  for(Node b = d.getFirstChild(); b != null; b = b.getNextSibling()) {
                     if ("enchant".equalsIgnoreCase(b.getNodeName())) {
                        attrs = b.getAttributes();
                        StatsSet set = new StatsSet();

                        for(int i = 0; i < attrs.getLength(); ++i) {
                           Node att = attrs.item(i);
                           set.set(att.getNodeName(), att.getNodeValue());
                        }

                        group.addEnchantDetail(new EnchantSkillGroup.EnchantSkillsHolder(set));
                     }
                  }
               }
            }
         }
      }
   }

   public int addNewRouteForSkill(int skillId, int maxLvL, int route, int group) {
      EnchantSkillLearn enchantableSkill = this._enchantSkillTrees.get(skillId);
      if (enchantableSkill == null) {
         enchantableSkill = new EnchantSkillLearn(skillId, maxLvL);
         this._enchantSkillTrees.put(skillId, enchantableSkill);
      }

      if (this._enchantSkillGroups.containsKey(group)) {
         enchantableSkill.addNewEnchantRoute(route, group);
         return this._enchantSkillGroups.get(group).getEnchantGroupDetails().size();
      } else {
         this._log
            .log(
               Level.SEVERE,
               this.getClass().getSimpleName()
                  + ": Error while loading generating enchant skill id: "
                  + skillId
                  + "; route: "
                  + route
                  + "; missing group: "
                  + group
            );
         return 0;
      }
   }

   public EnchantSkillLearn getSkillEnchantmentForSkill(Skill skill) {
      EnchantSkillLearn esl = this.getSkillEnchantmentBySkillId(skill.getId());
      return esl != null && skill.getLevel() >= esl.getBaseLevel() ? esl : null;
   }

   public EnchantSkillLearn getSkillEnchantmentBySkillId(int skillId) {
      return this._enchantSkillTrees.get(skillId);
   }

   public EnchantSkillGroup getEnchantSkillGroupById(int id) {
      return this._enchantSkillGroups.get(id);
   }

   public int getEnchantSkillSpCost(Skill skill) {
      EnchantSkillLearn enchantSkillLearn = this._enchantSkillTrees.get(skill.getId());
      if (enchantSkillLearn != null) {
         EnchantSkillGroup.EnchantSkillsHolder esh = enchantSkillLearn.getEnchantSkillsHolder(skill.getLevel());
         if (esh != null) {
            return esh.getSpCost();
         }
      }

      return Integer.MAX_VALUE;
   }

   public int getEnchantSkillAdenaCost(Skill skill) {
      EnchantSkillLearn enchantSkillLearn = this._enchantSkillTrees.get(skill.getId());
      if (enchantSkillLearn != null) {
         EnchantSkillGroup.EnchantSkillsHolder esh = enchantSkillLearn.getEnchantSkillsHolder(skill.getLevel());
         if (esh != null) {
            return esh.getAdenaCost();
         }
      }

      return Integer.MAX_VALUE;
   }

   public byte getEnchantSkillRate(Player player, Skill skill) {
      EnchantSkillLearn enchantSkillLearn = this._enchantSkillTrees.get(skill.getId());
      if (enchantSkillLearn != null) {
         EnchantSkillGroup.EnchantSkillsHolder esh = enchantSkillLearn.getEnchantSkillsHolder(skill.getLevel());
         if (esh != null) {
            return esh.getRate(player);
         }
      }

      return 0;
   }

   public static EnchantSkillGroupsParser getInstance() {
      return EnchantSkillGroupsParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final EnchantSkillGroupsParser _instance = new EnchantSkillGroupsParser();
   }
}
