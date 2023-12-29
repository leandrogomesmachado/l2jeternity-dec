package l2e.gameserver.data.parser;

import java.util.HashMap;
import java.util.Map;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.actor.templates.npc.DamageLimit;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class DamageLimitParser extends DocumentParser {
   private final Map<Integer, DamageLimit> _templates = new HashMap<>();

   protected DamageLimitParser() {
      this.load();
   }

   @Override
   public void load() {
      this._templates.clear();
      this.parseDatapackFile("data/stats/npcs/damageLimit.xml");
      this._log.info(this.getClass().getSimpleName() + ": Loaded " + this._templates.size() + " damage limit templates.");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node n = this.getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equals(n.getNodeName())) {
            for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
               if ("npc".equals(d.getNodeName())) {
                  NamedNodeMap attrs = d.getAttributes();
                  int id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
                  int damage = attrs.getNamedItem("damage") != null ? Integer.parseInt(attrs.getNamedItem("damage").getNodeValue()) : -1;
                  int physicDamage = attrs.getNamedItem("physicDamage") != null ? Integer.parseInt(attrs.getNamedItem("physicDamage").getNodeValue()) : -1;
                  int magicDamage = attrs.getNamedItem("magicDamage") != null ? Integer.parseInt(attrs.getNamedItem("magicDamage").getNodeValue()) : -1;
                  this._templates.put(id, new DamageLimit(damage, physicDamage, magicDamage));
               }
            }
         }
      }
   }

   public DamageLimit getDamageLimit(int npcId) {
      return this._templates.containsKey(npcId) ? this._templates.get(npcId) : null;
   }

   public static DamageLimitParser getInstance() {
      return DamageLimitParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final DamageLimitParser _instance = new DamageLimitParser();
   }
}
