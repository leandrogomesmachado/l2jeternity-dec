package l2e.gameserver.data.parser;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.data.DocumentParser;
import org.w3c.dom.Node;

public final class SellBuffsParser extends DocumentParser {
   private static final List<Integer> _sellBuffs = new ArrayList<>();

   protected SellBuffsParser() {
      _sellBuffs.clear();
      this.load();
   }

   @Override
   public synchronized void load() {
      this.parseDatapackFile("data/stats/services/sellBuffs.xml");
      this._log.info(this.getClass().getSimpleName() + ": Loaded " + _sellBuffs.size() + " available skills.");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node node = this.getCurrentDocument().getFirstChild(); node != null; node = node.getNextSibling()) {
         if ("list".equalsIgnoreCase(node.getNodeName())) {
            for(Node cd = node.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
               if ("skill".equalsIgnoreCase(cd.getNodeName())) {
                  int skillId = Integer.parseInt(cd.getAttributes().getNamedItem("id").getNodeValue());
                  if (!_sellBuffs.contains(skillId)) {
                     _sellBuffs.add(skillId);
                  }
               }
            }
         }
      }
   }

   public List<Integer> getSellBuffs() {
      return _sellBuffs;
   }

   public static SellBuffsParser getInstance() {
      return SellBuffsParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final SellBuffsParser _instance = new SellBuffsParser();
   }
}
