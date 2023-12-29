package l2e.gameserver.data.parser;

import gnu.trove.map.hash.THashMap;
import java.io.File;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import l2e.gameserver.Config;
import l2e.gameserver.model.skills.Skill;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class SchemesParser {
   private static final Logger _log = Logger.getLogger(SchemesParser.class.getName());
   private final THashMap<String, ArrayList<SchemesParser.SkillInfo>> _buffs = new THashMap<>();

   protected SchemesParser() {
      this.load();
   }

   protected void load() {
      this._buffs.clear();

      try {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setValidating(false);
         factory.setIgnoringComments(true);
         File file = new File(Config.DATAPACK_ROOT, "data/stats/services/communityBuffer.xml");
         if (!file.exists()) {
            _log.warning(this.getClass().getSimpleName() + ": Couldn't find data/stats/services/" + file.getName());
            return;
         }

         Document doc = factory.newDocumentBuilder().parse(file);

         for(Node list = doc.getFirstChild(); list != null; list = list.getNextSibling()) {
            if ("list".equalsIgnoreCase(list.getNodeName())) {
               for(Node groups = list.getFirstChild(); groups != null; groups = groups.getNextSibling()) {
                  if ("group".equalsIgnoreCase(groups.getNodeName())) {
                     NamedNodeMap attrs = groups.getAttributes();
                     String groupEn = attrs.getNamedItem("buffEn").getNodeValue();
                     String groupRu = attrs.getNamedItem("buffRu").getNodeValue();

                     for(Node skills = groups.getFirstChild(); skills != null; skills = skills.getNextSibling()) {
                        if ("skill".equalsIgnoreCase(skills.getNodeName())) {
                           attrs = skills.getAttributes();
                           int id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
                           int level = Integer.parseInt(attrs.getNamedItem("level").getNodeValue());
                           int adena = Integer.parseInt(attrs.getNamedItem("adena").getNodeValue());
                           int minLvl = Integer.parseInt(attrs.getNamedItem("minLvl").getNodeValue());
                           SchemesParser.SkillInfo info = new SchemesParser.SkillInfo();
                           info._id = id;
                           info._lvl = level;
                           info._groupEn = groupEn;
                           info._groupRu = groupRu;
                           info._cost = (long)adena;
                           info._minLvl = minLvl;
                           Skill skill = SkillsParser.getInstance().getInfo(info._id, info._lvl);
                           if (skill == null) {
                              _log.warning(
                                 this.getClass().getSimpleName() + ": Can't find skill id: " + info._id + " level: " + info._lvl + " in communityBuffer.xml"
                              );
                           } else {
                              this._buffs.putIfAbsent(info._groupEn, new ArrayList<>());
                              this._buffs.get(info._groupEn).add(info);
                           }
                        }
                     }
                  }
               }
            }
         }
      } catch (Exception var16) {
         _log.warning(this.getClass().getSimpleName() + ": Error while loading buffs: " + var16);
      }

      _log.info(this.getClass().getSimpleName() + ": Loaded " + this._buffs.size() + " buff templates.");
   }

   public Set<String> getBuffGroups() {
      return this._buffs.keySet();
   }

   public ArrayList<SchemesParser.SkillInfo> getBuffInfoByGroup(String group) {
      return this._buffs.get(group);
   }

   public boolean buffsContainsSkill(int skillId, int skillLvl) {
      for(ArrayList<SchemesParser.SkillInfo> infos : this._buffs.values()) {
         for(SchemesParser.SkillInfo info : infos) {
            if (skillId == info._id && skillLvl == info._lvl) {
               return true;
            }
         }
      }

      return false;
   }

   public boolean buffsIdContainsSkill(int skillId) {
      for(ArrayList<SchemesParser.SkillInfo> infos : this._buffs.values()) {
         for(SchemesParser.SkillInfo info : infos) {
            if (skillId == info._id) {
               return true;
            }
         }
      }

      return false;
   }

   public int getSkillFee(int skillId) {
      for(ArrayList<SchemesParser.SkillInfo> infos : this._buffs.values()) {
         for(SchemesParser.SkillInfo info : infos) {
            if (skillId == info._id) {
               return (int)info._cost;
            }
         }
      }

      return 0;
   }

   public int getSkillMinLvl(int skillId) {
      for(ArrayList<SchemesParser.SkillInfo> infos : this._buffs.values()) {
         for(SchemesParser.SkillInfo info : infos) {
            if (skillId == info._id) {
               return info._minLvl;
            }
         }
      }

      return 0;
   }

   public static SchemesParser getInstance() {
      return SchemesParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final SchemesParser _instance = new SchemesParser();
   }

   public class SkillInfo {
      public int _id;
      public int _lvl;
      public String _groupEn;
      public String _groupRu;
      public long _cost;
      public int _minLvl;
   }
}
