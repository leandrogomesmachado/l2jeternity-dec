package l2e.gameserver.data.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import l2e.fake.model.HealingSpell;
import l2e.fake.model.OffensiveSpell;
import l2e.fake.model.SpellUsageCondition;
import l2e.fake.model.SupportSpell;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.targets.TargetType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class FakeSkillsParser extends DocumentParser {
   private final Map<ClassId, List<OffensiveSpell>> _offensiveSkills = new HashMap<>();
   private final Map<ClassId, List<HealingSpell>> _healSkills = new HashMap<>();
   private final Map<ClassId, List<SupportSpell>> _supportSkills = new HashMap<>();
   private final Map<ClassId, Integer> _skillsChance = new HashMap<>();

   protected FakeSkillsParser() {
      this.load();
   }

   @Override
   public void load() {
      this._skillsChance.clear();
      this._offensiveSkills.clear();
      this._healSkills.clear();
      this._supportSkills.clear();
      this.parseDatapackFile("config/mods/fakes/skills.xml");
      this._log
         .info(
            this.getClass().getSimpleName()
               + ": Loaded "
               + (this._offensiveSkills.size() + this._healSkills.size() + this._supportSkills.size())
               + " skills for fake players."
         );
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node n = this.getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equalsIgnoreCase(n.getNodeName())) {
            for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
               if ("fake".equalsIgnoreCase(d.getNodeName())) {
                  this.parseEquipment(d);
               }
            }
         }
      }
   }

   private void parseEquipment(Node d) {
      NamedNodeMap attrs = d.getAttributes();
      ClassId classId = ClassId.getClassId(Integer.parseInt(attrs.getNamedItem("classId").getNodeValue()));
      int skillChance = Integer.parseInt(attrs.getNamedItem("skillsChance").getNodeValue());
      List<OffensiveSpell> offensiveSkills = new ArrayList<>();
      List<HealingSpell> healSkills = new ArrayList<>();
      List<SupportSpell> supportSkills = new ArrayList<>();

      for(Node c = d.getFirstChild(); c != null; c = c.getNextSibling()) {
         if ("offensiveSkill".equalsIgnoreCase(c.getNodeName())) {
            attrs = c.getAttributes();
            int id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
            int priority = Integer.parseInt(attrs.getNamedItem("priority").getNodeValue());
            Skill skill = SkillsParser.getInstance().getInfo(id, SkillsParser.getInstance().getMaxLevel(id));
            if (skill == null) {
               this._log.warning(this.getClass().getSimpleName() + ": Can't find fake offensive skill id: " + id + " skills.xml");
            } else {
               offensiveSkills.add(new OffensiveSpell(id, priority));
            }
         } else if ("healSkill".equalsIgnoreCase(c.getNodeName())) {
            attrs = c.getAttributes();
            int id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
            int value = Integer.parseInt(attrs.getNamedItem("value").getNodeValue());
            int priority = Integer.parseInt(attrs.getNamedItem("priority").getNodeValue());
            Skill skill = SkillsParser.getInstance().getInfo(id, SkillsParser.getInstance().getMaxLevel(id));
            if (skill == null) {
               this._log.warning(this.getClass().getSimpleName() + ": Can't find fake heal skill id: " + id + " in skills.xml");
            } else {
               healSkills.add(new HealingSpell(id, TargetType.ONE, value, priority));
            }
         } else if ("supportSkill".equalsIgnoreCase(c.getNodeName())) {
            attrs = c.getAttributes();
            int id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
            String cond = attrs.getNamedItem("cond").getNodeValue();
            int value = Integer.parseInt(attrs.getNamedItem("value").getNodeValue());
            int priority = Integer.parseInt(attrs.getNamedItem("priority").getNodeValue());
            SpellUsageCondition condition = null;
            switch(cond) {
               case "MOREHPPERCENT":
                  condition = SpellUsageCondition.MOREHPPERCENT;
                  break;
               case "LESSHPPERCENT":
                  condition = SpellUsageCondition.LESSHPPERCENT;
                  break;
               case "MISSINGCP":
                  condition = SpellUsageCondition.MISSINGCP;
                  break;
               default:
                  condition = SpellUsageCondition.NONE;
            }

            Skill skill = SkillsParser.getInstance().getInfo(id, SkillsParser.getInstance().getMaxLevel(id));
            if (skill == null) {
               this._log.warning(this.getClass().getSimpleName() + ": Can't find fake support skill id: " + id + " level: skills.xml");
            } else {
               supportSkills.add(new SupportSpell(id, condition, value, priority));
            }
         }
      }

      this._skillsChance.put(classId, skillChance);
      this._offensiveSkills.put(classId, offensiveSkills);
      this._healSkills.put(classId, healSkills);
      this._supportSkills.put(classId, supportSkills);
   }

   public int getSkillsChance(ClassId cId) {
      return this._skillsChance.get(cId);
   }

   public List<OffensiveSpell> getOffensiveSkills(ClassId cId) {
      return this._offensiveSkills.get(cId);
   }

   public List<HealingSpell> getHealSkills(ClassId cId) {
      return this._healSkills.get(cId);
   }

   public List<SupportSpell> getSupportSkills(ClassId cId) {
      return this._supportSkills.get(cId);
   }

   public static FakeSkillsParser getInstance() {
      return FakeSkillsParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final FakeSkillsParser _instance = new FakeSkillsParser();
   }
}
