package l2e.gameserver.data.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.skills.funcs.FuncTemplate;
import l2e.gameserver.model.skills.funcs.LambdaConst;
import l2e.gameserver.model.stats.Stats;
import org.w3c.dom.Node;

public class EnchantItemHPBonusParser extends DocumentParser {
   private final Map<Integer, List<Integer>> _armorHPBonuses = new HashMap<>();
   private static final float fullArmorModifier = 1.5F;

   protected EnchantItemHPBonusParser() {
      this.load();
   }

   @Override
   public void load() {
      this._armorHPBonuses.clear();
      this.parseDatapackFile("data/stats/enchanting/enchantHPBonus.xml");
      this._log.info(this.getClass().getSimpleName() + ": Loaded " + this._armorHPBonuses.size() + " enchant hp bonuses!");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node n = this.getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equals(n.getNodeName())) {
            for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
               if ("enchantHP".equals(d.getNodeName())) {
                  List<Integer> bonuses = new ArrayList<>();

                  for(Node e = d.getFirstChild(); e != null; e = e.getNextSibling()) {
                     if ("bonus".equals(e.getNodeName())) {
                        bonuses.add(Integer.valueOf(e.getTextContent()));
                     }
                  }

                  this._armorHPBonuses.put(parseInteger(d.getAttributes(), "grade"), bonuses);
               }
            }
         }
      }

      if (!this._armorHPBonuses.isEmpty()) {
         ItemsParser it = ItemsParser.getInstance();

         for(Integer itemId : it.getAllArmorsId()) {
            Item item = it.getTemplate(itemId);
            if (item != null && item.getCrystalType() != 0) {
               switch(item.getBodyPart()) {
                  case 1:
                  case 64:
                  case 256:
                  case 512:
                  case 1024:
                  case 2048:
                  case 4096:
                  case 8192:
                  case 32768:
                  case 268435456:
                     item.attach(new FuncTemplate(null, null, "EnchantHp", Stats.MAX_HP, 96, new LambdaConst(0.0)));
               }
            }
         }

         for(Integer itemId : it.getAllWeaponsId()) {
            Item item = it.getTemplate(itemId);
            if (item != null && item.getCrystalType() != 0) {
               switch(item.getBodyPart()) {
                  case 256:
                     item.attach(new FuncTemplate(null, null, "EnchantHp", Stats.MAX_HP, 96, new LambdaConst(0.0)));
               }
            }
         }
      }
   }

   public final int getHPBonus(ItemInstance item) {
      List<Integer> values = this._armorHPBonuses.get(item.getItem().getItemGradeSPlus());
      if (values != null && !values.isEmpty() && item.getOlyEnchantLevel() > 0) {
         int bonus = values.get(Math.min(item.getOlyEnchantLevel(), values.size()) - 1);
         return item.getItem().getBodyPart() == 32768 ? (int)((float)bonus * 1.5F) : bonus;
      } else {
         return 0;
      }
   }

   public static final EnchantItemHPBonusParser getInstance() {
      return EnchantItemHPBonusParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final EnchantItemHPBonusParser _instance = new EnchantItemHPBonusParser();
   }
}
