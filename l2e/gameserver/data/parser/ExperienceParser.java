package l2e.gameserver.data.parser;

import java.util.HashMap;
import java.util.Map;
import l2e.gameserver.Config;
import l2e.gameserver.data.DocumentParser;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class ExperienceParser extends DocumentParser {
   private final Map<Integer, Long> _expTable = new HashMap<>();
   private byte MAX_LEVEL;
   private byte MAX_PET_LEVEL;

   protected ExperienceParser() {
      this.load();
   }

   @Override
   public void load() {
      this._expTable.clear();
      this.parseDatapackFile("data/stats/chars/experience.xml");
      this._log.info(this.getClass().getSimpleName() + ": Loaded " + this._expTable.size() + " levels.");
      if (Config.DEBUG) {
         this._log.info(this.getClass().getSimpleName() + ": Max Player Level is: " + (this.MAX_LEVEL - 1));
         this._log.info(this.getClass().getSimpleName() + ": Max Pet Level is: " + (this.MAX_PET_LEVEL - 1));
      }
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      Node table = this.getCurrentDocument().getFirstChild();
      NamedNodeMap tableAttr = table.getAttributes();
      this.MAX_LEVEL = (byte)(Byte.parseByte(tableAttr.getNamedItem("maxLevel").getNodeValue()) + 1);
      this.MAX_PET_LEVEL = (byte)(Byte.parseByte(tableAttr.getNamedItem("maxPetLevel").getNodeValue()) + 1);

      for(Node n = table.getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("experience".equals(n.getNodeName())) {
            NamedNodeMap attrs = n.getAttributes();
            this._expTable.put(parseInteger(attrs, "level"), parseLong(attrs, "tolevel"));
         }
      }
   }

   public long getExpForLevel(int level) {
      return this._expTable.get(level);
   }

   public byte getMaxLevel() {
      return this.MAX_LEVEL;
   }

   public byte getMaxPetLevel() {
      return this.MAX_PET_LEVEL;
   }

   public double penaltyModifier(long count, double percents) {
      return Math.max(1.0 - (double)count * percents / 100.0, 0.0);
   }

   public static ExperienceParser getInstance() {
      return ExperienceParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final ExperienceParser _instance = new ExperienceParser();
   }
}
