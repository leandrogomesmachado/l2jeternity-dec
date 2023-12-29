package l2e.gameserver.model.skills.engines;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import l2e.commons.util.file.filter.XMLFilter;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.engines.items.DocumentItem;
import l2e.gameserver.model.skills.engines.skills.DocumentSkill;

public class DocumentEngine {
   private static final Logger _log = Logger.getLogger(DocumentEngine.class.getName());
   private final List<File> _itemFiles = new ArrayList<>();
   private final List<File> _skillFiles = new ArrayList<>();

   public static DocumentEngine getInstance() {
      return DocumentEngine.SingletonHolder._instance;
   }

   protected DocumentEngine() {
      this.hashFiles("data/stats/items/items", this._itemFiles);
      if (Config.CUSTOM_ITEMS) {
         this.hashFiles("data/stats/items/items/custom", this._itemFiles);
      }

      this.hashFiles("data/stats/skills/skills", this._skillFiles);
      if (Config.CUSTOM_SKILLS) {
         this.hashFiles("data/stats/skills/skills/custom", this._skillFiles);
      }
   }

   private void hashFiles(String dirname, List<File> hash) {
      File dir = new File(Config.DATAPACK_ROOT, dirname);
      if (!dir.exists()) {
         _log.warning("Dir " + dir.getAbsolutePath() + " not exists");
      } else {
         File[] files = dir.listFiles(new XMLFilter());

         for(File f : files) {
            hash.add(f);
         }
      }
   }

   public List<Skill> loadSkills(File file) {
      if (file == null) {
         _log.warning("Skill file not found.");
         return null;
      } else {
         DocumentSkill doc = new DocumentSkill(file);
         doc.parse();
         return doc.getSkills();
      }
   }

   public void loadAllSkills(Map<Integer, Skill> allSkills) {
      int count = 0;

      for(File file : this._skillFiles) {
         List<Skill> s = this.loadSkills(file);
         if (s != null) {
            for(Skill skill : s) {
               allSkills.put(SkillsParser.getSkillHashCode(skill), skill);
               ++count;
            }
         }
      }

      _log.info("SkillsParser: Loaded " + count + " skill templates.");
   }

   public List<Item> loadItems() {
      List<Item> list = new ArrayList<>();

      for(File f : this._itemFiles) {
         DocumentItem document = new DocumentItem(f);
         document.parse();
         list.addAll(document.getItemList());
      }

      return list;
   }

   private static class SingletonHolder {
      protected static final DocumentEngine _instance = new DocumentEngine();
   }
}
