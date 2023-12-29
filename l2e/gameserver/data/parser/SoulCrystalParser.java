package l2e.gameserver.data.parser;

import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.logging.Logger;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.actor.templates.SoulCrystalTemplate;
import org.w3c.dom.Node;

public final class SoulCrystalParser extends DocumentParser {
   private static final Logger _log = Logger.getLogger(NpcsParser.class.getName());
   private final TIntObjectHashMap<SoulCrystalTemplate> _crystals = new TIntObjectHashMap<>();

   private SoulCrystalParser() {
      this.load();
   }

   @Override
   public final void load() {
      this._crystals.clear();
      this.parseDatapackFile("data/stats/items/soul_crystals.xml");
      _log.info(this.getClass().getSimpleName() + ": Loaded " + this._crystals.size() + " soul crystal templates.");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node c = this.getCurrentDocument().getFirstChild(); c != null; c = c.getNextSibling()) {
         if ("list".equalsIgnoreCase(c.getNodeName())) {
            for(Node d = c.getFirstChild(); d != null; d = d.getNextSibling()) {
               if ("crystal".equalsIgnoreCase(d.getNodeName())) {
                  int itemId = Integer.parseInt(d.getAttributes().getNamedItem("itemId").getNodeValue());
                  int level = Integer.parseInt(d.getAttributes().getNamedItem("level").getNodeValue());
                  int nextItemId = Integer.parseInt(d.getAttributes().getNamedItem("next_itemId").getNodeValue());
                  int cursedNextItemId = d.getAttributes().getNamedItem("cursed_next_itemId") == null
                     ? 0
                     : Integer.parseInt(d.getAttributes().getNamedItem("cursed_next_itemId").getNodeValue());
                  this.addCrystal(new SoulCrystalTemplate(itemId, level, nextItemId, cursedNextItemId));
               }
            }
         }
      }
   }

   public void addCrystal(SoulCrystalTemplate crystal) {
      this._crystals.put(crystal.getId(), crystal);
   }

   public SoulCrystalTemplate getCrystal(int item) {
      return this._crystals.get(item);
   }

   public SoulCrystalTemplate[] getCrystals() {
      return this._crystals.values(new SoulCrystalTemplate[this._crystals.size()]);
   }

   public static SoulCrystalParser getInstance() {
      return SoulCrystalParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final SoulCrystalParser _instance = new SoulCrystalParser();
   }
}
