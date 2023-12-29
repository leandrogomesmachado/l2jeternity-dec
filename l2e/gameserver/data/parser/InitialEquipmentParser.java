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

public final class InitialEquipmentParser extends DocumentParser {
   private final Map<ClassId, List<PcItemTemplate>> _initialEquipmentList = new HashMap<>();

   protected InitialEquipmentParser() {
      this.load();
   }

   @Override
   public void load() {
      this._initialEquipmentList.clear();
      this.parseDatapackFile("data/stats/chars/initialEquipment.xml");
      this._log.info(this.getClass().getSimpleName() + ": Loaded " + this._initialEquipmentList.size() + " initial equipment data.");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node n = this.getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equalsIgnoreCase(n.getNodeName())) {
            for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
               if ("equipment".equalsIgnoreCase(d.getNodeName())) {
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

                  this._initialEquipmentList.put(classId, equipList);
               }
            }
         }
      }
   }

   public List<PcItemTemplate> getEquipmentList(ClassId cId) {
      return this._initialEquipmentList.get(cId);
   }

   public List<PcItemTemplate> getEquipmentList(int cId) {
      return this._initialEquipmentList.get(ClassId.getClassId(cId));
   }

   public static InitialEquipmentParser getInstance() {
      return InitialEquipmentParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final InitialEquipmentParser _instance = new InitialEquipmentParser();
   }
}
