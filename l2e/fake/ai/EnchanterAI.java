package l2e.fake.ai;

import java.util.List;
import l2e.commons.util.Rnd;
import l2e.fake.FakePlayer;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.FakeArmorParser;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.actor.templates.items.Weapon;
import l2e.gameserver.model.items.PcItemTemplate;
import l2e.gameserver.model.items.instance.ItemInstance;

public class EnchanterAI extends FakePlayerAI {
   private int _enchantIterations = 0;
   private final int _maxEnchant = Config.ENCHANTERS_MAX_LVL;
   private final int iterationsForAction = Rnd.get(3, 5);

   public EnchanterAI(FakePlayer character) {
      super(character, false);
   }

   @Override
   public void setup() {
      super.setup();
      ItemInstance weapon = this._fakePlayer.getActiveWeaponInstance();
      weapon = this.checkIfWeaponIsExistsEquipped(weapon);
      weapon.setEnchantLevel(0);
      this._fakePlayer.broadcastCharInfo();
   }

   @Override
   public void thinkAndAct() {
      this.handleDeath();
      this.setBusyThinking(true);
      if (this._enchantIterations % this.iterationsForAction == 0) {
         ItemInstance weapon = this._fakePlayer.getActiveWeaponInstance();
         weapon = this.checkIfWeaponIsExistsEquipped(weapon);
         double chance = this.getSuccessChance(weapon);
         int currentEnchantLevel = weapon.getEnchantLevel();
         if (currentEnchantLevel < this._maxEnchant || this.serverHasUnlimitedMax()) {
            if (!(Rnd.nextDouble() < chance) && weapon.getEnchantLevel() >= 4) {
               this.destroyFailedItem(weapon);
            } else {
               weapon.setEnchantLevel(currentEnchantLevel + 1);
               this._fakePlayer.broadcastCharInfo();
            }
         }
      }

      ++this._enchantIterations;
      this.setBusyThinking(false);
   }

   private void destroyFailedItem(ItemInstance weapon) {
      this._fakePlayer.getInventory().destroyItem("Enchant", weapon, this._fakePlayer, null);
      this._fakePlayer.broadcastCharInfo();
      this._fakePlayer.setActiveEnchantItemId(-1);
   }

   private double getSuccessChance(ItemInstance weapon) {
      double chance = 0.0;
      if (((Weapon)weapon.getItem()).isMagicWeapon()) {
         chance = weapon.getEnchantLevel() > 14 ? 60.0 : 70.0;
      } else {
         chance = weapon.getEnchantLevel() > 14 ? 65.0 : 75.0;
      }

      return chance;
   }

   private boolean serverHasUnlimitedMax() {
      return this._maxEnchant == 0;
   }

   private ItemInstance checkIfWeaponIsExistsEquipped(ItemInstance weapon) {
      if (weapon == null) {
         ItemInstance newItem = this.checkRndItems(this._fakePlayer);
         if (newItem != null) {
            weapon = newItem;
         }
      }

      return weapon;
   }

   private ItemInstance checkRndItems(FakePlayer player) {
      ItemInstance weapon = null;
      List<PcItemTemplate> items = null;
      if (player.getLevel() < 20) {
         items = FakeArmorParser.getInstance().getNgGradeList(player.getClassId());
      } else if (player.getLevel() > 19 && player.getLevel() < 40) {
         items = FakeArmorParser.getInstance().getDGradeList(player.getClassId());
      } else if (player.getLevel() > 40 && player.getLevel() < 52) {
         items = FakeArmorParser.getInstance().getCGradeList(player.getClassId());
      } else if (player.getLevel() > 52 && player.getLevel() < 61) {
         items = FakeArmorParser.getInstance().getBGradeList(player.getClassId());
      } else if (player.getLevel() > 60 && player.getLevel() < 76) {
         items = FakeArmorParser.getInstance().getAGradeList(player.getClassId());
      } else if (player.getLevel() > 75 && player.getLevel() < 80) {
         items = FakeArmorParser.getInstance().getSGradeList(player.getClassId());
      } else if (Rnd.get(100) <= 50) {
         items = FakeArmorParser.getInstance().getS80GradeList(player.getClassId());
      } else {
         items = FakeArmorParser.getInstance().getS84GradeList(player.getClassId());
      }

      if (items != null) {
         for(PcItemTemplate ie : items) {
            if (ie != null) {
               Item item = ItemsParser.getInstance().getTemplate(ie.getId());
               if (item != null && item.isWeapon()) {
                  ItemInstance it = player.getInventory().addItem("Items", ie.getId(), ie.getCount(), player, null);
                  player.getInventory().equipItem(it);
                  weapon = it;
               }
            }
         }
      }

      return weapon;
   }

   @Override
   protected int[][] getBuffs() {
      return new int[0][0];
   }
}
