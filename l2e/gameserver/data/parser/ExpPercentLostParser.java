package l2e.gameserver.data.parser;

import java.util.Arrays;
import l2e.gameserver.data.DocumentParser;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class ExpPercentLostParser extends DocumentParser {
   private final int _maxlevel = ExperienceParser.getInstance().getMaxLevel();
   private final double[] _expPercentLost = new double[this._maxlevel + 1];

   protected ExpPercentLostParser() {
      Arrays.fill(this._expPercentLost, 1.0);
      this.load();
   }

   @Override
   public void load() {
      this.parseDatapackFile("data/stats/chars/expPercentLost.xml");
      this._log.info(this.getClass().getSimpleName() + ": Loaded " + this._expPercentLost.length + " levels.");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   public void parseDocument() {
      for(Node n = this.getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equalsIgnoreCase(n.getNodeName())) {
            for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
               if ("set".equalsIgnoreCase(d.getNodeName())) {
                  NamedNodeMap attrs = d.getAttributes();
                  this._expPercentLost[parseInteger(attrs, "level")] = parseDouble(attrs, "val");
               }
            }
         }
      }
   }

   public double getExpPercent(int level) {
      return level > this._maxlevel ? this._expPercentLost[this._maxlevel] : this._expPercentLost[level];
   }

   public static ExpPercentLostParser getInstance() {
      return ExpPercentLostParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final ExpPercentLostParser _instance = new ExpPercentLostParser();
   }
}
