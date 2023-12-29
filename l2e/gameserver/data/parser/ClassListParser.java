package l2e.gameserver.data.parser;

import java.util.HashMap;
import java.util.Map;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.base.ClassInfo;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class ClassListParser extends DocumentParser {
   private static final Map<ClassId, ClassInfo> _classData = new HashMap<>();

   protected ClassListParser() {
      this.load();
   }

   @Override
   public void load() {
      _classData.clear();
      this.parseDatapackFile("data/stats/chars/classList.xml");
      this._log.info(this.getClass().getSimpleName() + ": Loaded " + _classData.size() + " Class data.");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node n = this.getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equals(n.getNodeName())) {
            for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
               NamedNodeMap attrs = d.getAttributes();
               if ("class".equals(d.getNodeName())) {
                  Node attr = attrs.getNamedItem("classId");
                  ClassId classId = ClassId.getClassId(parseInt(attr));
                  attr = attrs.getNamedItem("name");
                  String className = attr.getNodeValue();
                  attr = attrs.getNamedItem("serverName");
                  String classServName = attr.getNodeValue();
                  attr = attrs.getNamedItem("parentClassId");
                  ClassId parentClassId = attr != null ? ClassId.getClassId(parseInt(attr)) : null;
                  _classData.put(classId, new ClassInfo(classId, className, classServName, parentClassId));
               }
            }
         }
      }
   }

   public Map<ClassId, ClassInfo> getClassList() {
      return _classData;
   }

   public ClassInfo getClass(ClassId classId) {
      return _classData.get(classId);
   }

   public ClassInfo getClass(int classId) {
      ClassId id = ClassId.getClassId(classId);
      return id != null ? _classData.get(id) : null;
   }

   public ClassInfo getClass(String classServName) {
      for(ClassInfo classInfo : _classData.values()) {
         if (classInfo.getClassServName().equals(classServName)) {
            return classInfo;
         }
      }

      return null;
   }

   public static ClassListParser getInstance() {
      return ClassListParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final ClassListParser _instance = new ClassListParser();
   }
}
