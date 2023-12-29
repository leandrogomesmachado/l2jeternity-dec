package l2e.gameserver.data.parser;

import java.util.HashMap;
import java.util.Map;
import l2e.gameserver.data.DocumentParser;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class FoundationParser extends DocumentParser {
   private final Map<Integer, Integer> _foundation = new HashMap<>();

   protected FoundationParser() {
      this.load();
   }

   @Override
   public void load() {
      this._foundation.clear();
      this.parseDatapackFile("data/stats/services/foundation.xml");
      this._log.info(this.getClass().getSimpleName() + ": Loaded " + this._foundation.size() + " foundation templates.");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node n = this.getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equals(n.getNodeName())) {
            for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
               if ("foundation".equals(d.getNodeName())) {
                  NamedNodeMap attrs = d.getAttributes();
                  int simple = Integer.parseInt(attrs.getNamedItem("simple").getNodeValue());
                  int found = Integer.parseInt(attrs.getNamedItem("found").getNodeValue());
                  this.addFoundation(simple, found);
               }
            }
         }
      }
   }

   public void addFoundation(int simple, int found) {
      this._foundation.put(simple, found);
   }

   public int getFoundation(int id) {
      return this._foundation.containsKey(id) ? this._foundation.get(id) : -1;
   }

   public static FoundationParser getInstance() {
      return FoundationParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final FoundationParser _instance = new FoundationParser();
   }
}
