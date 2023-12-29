package l2e.gameserver.model.service.autoenchant;

import l2e.gameserver.Config;
import l2e.gameserver.model.items.instance.ItemInstance;

public class EnchantParams {
   public ItemInstance targetItem;
   public ItemInstance upgradeItem;
   public boolean isUseCommonScrollWhenSafe = true;
   public int upgradeItemLimit = Config.enchantServiceDefaultLimit;
   public int maxEnchant = Config.enchantServiceDefaultEnchant;
   public int maxEnchantAtt = Config.enchantServiceDefaultAttribute;
   public boolean isChangingUpgradeItemLimit = false;
   public boolean isChangingMaxEnchant = false;
   public long lastEnchant;
   public long lastAbuse;
}
