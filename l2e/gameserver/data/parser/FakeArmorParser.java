package l2e.gameserver.data.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.items.PcItemTemplate;
import l2e.gameserver.model.stats.StatsSet;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class FakeArmorParser extends DocumentParser {
   protected final Map<ClassId, List<PcItemTemplate>> _phantomNgradeItemsList = new HashMap<>();
   protected final Map<ClassId, List<PcItemTemplate>> _phantomDgradeItemsList = new HashMap<>();
   protected final Map<ClassId, List<PcItemTemplate>> _phantomCgradeItemsList = new HashMap<>();
   protected final Map<ClassId, List<PcItemTemplate>> _phantomBgradeItemsList = new HashMap<>();
   protected final Map<ClassId, List<PcItemTemplate>> _phantomAgradeItemsList = new HashMap<>();
   protected final Map<ClassId, List<PcItemTemplate>> _phantomSgradeItemsList = new HashMap<>();
   protected final Map<ClassId, List<PcItemTemplate>> _phantomS80gradeItemsList = new HashMap<>();
   protected final Map<ClassId, List<PcItemTemplate>> _phantomS84gradeItemsList = new HashMap<>();

   protected FakeArmorParser() {
      this.load();
   }

   @Override
   public void load() {
      this._phantomNgradeItemsList.clear();
      this._phantomDgradeItemsList.clear();
      this._phantomCgradeItemsList.clear();
      this._phantomBgradeItemsList.clear();
      this._phantomAgradeItemsList.clear();
      this._phantomSgradeItemsList.clear();
      this._phantomS80gradeItemsList.clear();
      this._phantomS84gradeItemsList.clear();
      this.parseDatapackFile("config/mods/fakes/armorSets.xml");
      this._log
         .info(
            this.getClass().getSimpleName()
               + ": Loaded "
               + (
                  this._phantomNgradeItemsList.size()
                     + this._phantomDgradeItemsList.size()
                     + this._phantomCgradeItemsList.size()
                     + this._phantomBgradeItemsList.size()
                     + this._phantomAgradeItemsList.size()
                     + this._phantomSgradeItemsList.size()
                     + this._phantomS80gradeItemsList.size()
                     + this._phantomS84gradeItemsList.size()
               )
               + " fake player sets."
         );
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node n = this.getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equalsIgnoreCase(n.getNodeName())) {
            for(Node list = n.getFirstChild(); list != null; list = list.getNextSibling()) {
               if ("NgradeItems".equalsIgnoreCase(list.getNodeName())) {
                  this.parseNgGrade(list);
               } else if ("DgradeItems".equalsIgnoreCase(list.getNodeName())) {
                  this.parseDGrade(list);
               } else if ("CgradeItems".equalsIgnoreCase(list.getNodeName())) {
                  this.parseCGrade(list);
               } else if ("BgradeItems".equalsIgnoreCase(list.getNodeName())) {
                  this.parseBGrade(list);
               } else if ("AgradeItems".equalsIgnoreCase(list.getNodeName())) {
                  this.parseAGrade(list);
               } else if ("SgradeItems".equalsIgnoreCase(list.getNodeName())) {
                  this.parseSGrade(list);
               } else if ("S80gradeItems".equalsIgnoreCase(list.getNodeName())) {
                  this.parseS80Grade(list);
               } else if ("S84gradeItems".equalsIgnoreCase(list.getNodeName())) {
                  this.parseS84Grade(list);
               }
            }
         }
      }
   }

   private void parseNgGrade(Node d) {
      NamedNodeMap attrs = d.getAttributes();
      ClassId classId = ClassId.getClassId(Integer.parseInt(attrs.getNamedItem("classId").getNodeValue()));
      List<PcItemTemplate> equipList = new ArrayList<>();

      for(Node c = d.getFirstChild(); c != null; c = c.getNextSibling()) {
         if ("item".equalsIgnoreCase(c.getNodeName())) {
            StatsSet set = new StatsSet();
            attrs = c.getAttributes();

            for(int i = 0; i < attrs.getLength(); ++i) {
               Node attr = attrs.item(i);
               set.set(attr.getNodeName(), attr.getNodeValue());
            }

            equipList.add(new PcItemTemplate(set));
         }
      }

      this._phantomNgradeItemsList.put(classId, equipList);
   }

   private void parseDGrade(Node d) {
      NamedNodeMap attrs = d.getAttributes();
      ClassId classId = ClassId.getClassId(Integer.parseInt(attrs.getNamedItem("classId").getNodeValue()));
      List<PcItemTemplate> equipList = new ArrayList<>();

      for(Node c = d.getFirstChild(); c != null; c = c.getNextSibling()) {
         if ("item".equalsIgnoreCase(c.getNodeName())) {
            StatsSet set = new StatsSet();
            attrs = c.getAttributes();

            for(int i = 0; i < attrs.getLength(); ++i) {
               Node attr = attrs.item(i);
               set.set(attr.getNodeName(), attr.getNodeValue());
            }

            equipList.add(new PcItemTemplate(set));
         }
      }

      this._phantomDgradeItemsList.put(classId, equipList);
   }

   private void parseCGrade(Node d) {
      NamedNodeMap attrs = d.getAttributes();
      ClassId classId = ClassId.getClassId(Integer.parseInt(attrs.getNamedItem("classId").getNodeValue()));
      List<PcItemTemplate> equipList = new ArrayList<>();

      for(Node c = d.getFirstChild(); c != null; c = c.getNextSibling()) {
         if ("item".equalsIgnoreCase(c.getNodeName())) {
            StatsSet set = new StatsSet();
            attrs = c.getAttributes();

            for(int i = 0; i < attrs.getLength(); ++i) {
               Node attr = attrs.item(i);
               set.set(attr.getNodeName(), attr.getNodeValue());
            }

            equipList.add(new PcItemTemplate(set));
         }
      }

      this._phantomCgradeItemsList.put(classId, equipList);
   }

   private void parseBGrade(Node d) {
      NamedNodeMap attrs = d.getAttributes();
      ClassId classId = ClassId.getClassId(Integer.parseInt(attrs.getNamedItem("classId").getNodeValue()));
      List<PcItemTemplate> equipList = new ArrayList<>();

      for(Node c = d.getFirstChild(); c != null; c = c.getNextSibling()) {
         if ("item".equalsIgnoreCase(c.getNodeName())) {
            StatsSet set = new StatsSet();
            attrs = c.getAttributes();

            for(int i = 0; i < attrs.getLength(); ++i) {
               Node attr = attrs.item(i);
               set.set(attr.getNodeName(), attr.getNodeValue());
            }

            equipList.add(new PcItemTemplate(set));
         }
      }

      this._phantomBgradeItemsList.put(classId, equipList);
   }

   private void parseAGrade(Node d) {
      NamedNodeMap attrs = d.getAttributes();
      ClassId classId = ClassId.getClassId(Integer.parseInt(attrs.getNamedItem("classId").getNodeValue()));
      List<PcItemTemplate> equipList = new ArrayList<>();

      for(Node c = d.getFirstChild(); c != null; c = c.getNextSibling()) {
         if ("item".equalsIgnoreCase(c.getNodeName())) {
            StatsSet set = new StatsSet();
            attrs = c.getAttributes();

            for(int i = 0; i < attrs.getLength(); ++i) {
               Node attr = attrs.item(i);
               set.set(attr.getNodeName(), attr.getNodeValue());
            }

            equipList.add(new PcItemTemplate(set));
         }
      }

      this._phantomAgradeItemsList.put(classId, equipList);
   }

   private void parseSGrade(Node d) {
      NamedNodeMap attrs = d.getAttributes();
      ClassId classId = ClassId.getClassId(Integer.parseInt(attrs.getNamedItem("classId").getNodeValue()));
      List<PcItemTemplate> equipList = new ArrayList<>();

      for(Node c = d.getFirstChild(); c != null; c = c.getNextSibling()) {
         if ("item".equalsIgnoreCase(c.getNodeName())) {
            StatsSet set = new StatsSet();
            attrs = c.getAttributes();

            for(int i = 0; i < attrs.getLength(); ++i) {
               Node attr = attrs.item(i);
               set.set(attr.getNodeName(), attr.getNodeValue());
            }

            equipList.add(new PcItemTemplate(set));
         }
      }

      this._phantomSgradeItemsList.put(classId, equipList);
   }

   private void parseS80Grade(Node d) {
      NamedNodeMap attrs = d.getAttributes();
      ClassId classId = ClassId.getClassId(Integer.parseInt(attrs.getNamedItem("classId").getNodeValue()));
      List<PcItemTemplate> equipList = new ArrayList<>();

      for(Node c = d.getFirstChild(); c != null; c = c.getNextSibling()) {
         if ("item".equalsIgnoreCase(c.getNodeName())) {
            StatsSet set = new StatsSet();
            attrs = c.getAttributes();

            for(int i = 0; i < attrs.getLength(); ++i) {
               Node attr = attrs.item(i);
               set.set(attr.getNodeName(), attr.getNodeValue());
            }

            equipList.add(new PcItemTemplate(set));
         }
      }

      this._phantomS80gradeItemsList.put(classId, equipList);
   }

   private void parseS84Grade(Node d) {
      NamedNodeMap attrs = d.getAttributes();
      ClassId classId = ClassId.getClassId(Integer.parseInt(attrs.getNamedItem("classId").getNodeValue()));
      List<PcItemTemplate> equipList = new ArrayList<>();

      for(Node c = d.getFirstChild(); c != null; c = c.getNextSibling()) {
         if ("item".equalsIgnoreCase(c.getNodeName())) {
            StatsSet set = new StatsSet();
            attrs = c.getAttributes();

            for(int i = 0; i < attrs.getLength(); ++i) {
               Node attr = attrs.item(i);
               set.set(attr.getNodeName(), attr.getNodeValue());
            }

            equipList.add(new PcItemTemplate(set));
         }
      }

      this._phantomS84gradeItemsList.put(classId, equipList);
   }

   public List<PcItemTemplate> getNgGradeList(ClassId cId) {
      return this._phantomNgradeItemsList.get(cId);
   }

   public List<PcItemTemplate> getDGradeList(ClassId cId) {
      return this._phantomDgradeItemsList.get(cId);
   }

   public List<PcItemTemplate> getCGradeList(ClassId cId) {
      return this._phantomCgradeItemsList.get(cId);
   }

   public List<PcItemTemplate> getBGradeList(ClassId cId) {
      return this._phantomBgradeItemsList.get(cId);
   }

   public List<PcItemTemplate> getAGradeList(ClassId cId) {
      return this._phantomAgradeItemsList.get(cId);
   }

   public List<PcItemTemplate> getSGradeList(ClassId cId) {
      return this._phantomSgradeItemsList.get(cId);
   }

   public List<PcItemTemplate> getS80GradeList(ClassId cId) {
      return this._phantomS80gradeItemsList.get(cId);
   }

   public List<PcItemTemplate> getS84GradeList(ClassId cId) {
      return this._phantomS84gradeItemsList.get(cId);
   }

   public static FakeArmorParser getInstance() {
      return FakeArmorParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final FakeArmorParser _instance = new FakeArmorParser();
   }
}
