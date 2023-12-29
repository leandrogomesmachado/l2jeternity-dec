package l2e.gameserver.data.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.actor.templates.items.Henna;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.stats.StatsSet;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class HennaParser extends DocumentParser {
   private static final Map<Integer, Henna> _hennaList = new HashMap<>();

   protected HennaParser() {
      this.load();
   }

   @Override
   public void load() {
      _hennaList.clear();
      this.parseDatapackFile("data/stats/chars/hennaList.xml");
      this._log.info(this.getClass().getSimpleName() + ": Loaded " + _hennaList.size() + " henna data.");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node n = this.getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equals(n.getNodeName())) {
            for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
               if ("henna".equals(d.getNodeName())) {
                  this.parseHenna(d);
               }
            }
         }
      }
   }

   private void parseHenna(Node d) {
      StatsSet set = new StatsSet();
      List<ClassId> wearClassIds = new ArrayList<>();
      List<Skill> skillList = new ArrayList<>();
      NamedNodeMap attrs = d.getAttributes();

      for(int i = 0; i < attrs.getLength(); ++i) {
         Node attr = attrs.item(i);
         set.set(attr.getNodeName(), attr.getNodeValue());
      }

      for(Node c = d.getFirstChild(); c != null; c = c.getNextSibling()) {
         String name = c.getNodeName();
         attrs = c.getAttributes();
         switch(name) {
            case "stats":
               for(int i = 0; i < attrs.getLength(); ++i) {
                  Node attrx = attrs.item(i);
                  set.set(attrx.getNodeName(), attrx.getNodeValue());
               }
               break;
            case "wear": {
               Node attr = attrs.getNamedItem("count");
               set.set("wear_count", attr.getNodeValue());
               attr = attrs.getNamedItem("fee");
               set.set("wear_fee", attr.getNodeValue());
               break;
            }
            case "cancel": {
               Node attr = attrs.getNamedItem("count");
               set.set("cancel_count", attr.getNodeValue());
               attr = attrs.getNamedItem("fee");
               set.set("cancel_fee", attr.getNodeValue());
               break;
            }
            case "classId":
               wearClassIds.add(ClassId.getClassId(Integer.parseInt(c.getTextContent())));
               break;
            case "skill":
               int id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
               int lvl = Integer.parseInt(attrs.getNamedItem("level").getNodeValue());
               Skill data = SkillsParser.getInstance().getInfo(id, lvl);
               if (data != null) {
                  skillList.add(data);
               }
         }
      }

      Henna henna = new Henna(set);
      henna.setWearClassIds(wearClassIds);
      henna.setSkills(skillList);
      _hennaList.put(henna.getDyeId(), henna);
   }

   public Henna getHenna(int id) {
      return _hennaList.get(id);
   }

   public boolean isHenna(int itemId) {
      for(Henna henna : _hennaList.values()) {
         if (henna.getDyeId() == itemId) {
            return true;
         }
      }

      return false;
   }

   public List<Henna> getHennaList(ClassId classId) {
      List<Henna> list = new ArrayList<>();

      for(Henna henna : _hennaList.values()) {
         if (henna.isAllowedClass(classId)) {
            list.add(henna);
         }
      }

      return list;
   }

   public static HennaParser getInstance() {
      return HennaParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final HennaParser _instance = new HennaParser();
   }
}
