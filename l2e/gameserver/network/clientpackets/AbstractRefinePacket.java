package l2e.gameserver.network.clientpackets;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.Armor;
import l2e.gameserver.model.actor.templates.items.Weapon;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;

public abstract class AbstractRefinePacket extends GameClientPacket {
   public static final int GRADE_NONE = 0;
   public static final int GRADE_MID = 1;
   public static final int GRADE_HIGH = 2;
   public static final int GRADE_TOP = 3;
   public static final int GRADE_ACC = 4;
   protected static final int GEMSTONE_D = 2130;
   protected static final int GEMSTONE_C = 2131;
   protected static final int GEMSTONE_B = 2132;
   private static final Map<Integer, AbstractRefinePacket.LifeStone> _lifeStones = new HashMap<>();

   protected static final AbstractRefinePacket.LifeStone getLifeStone(int itemId) {
      return _lifeStones.get(itemId);
   }

   protected static final boolean isValid(Player player, ItemInstance item, ItemInstance refinerItem, ItemInstance gemStones) {
      if (!isValid(player, item, refinerItem)) {
         return false;
      } else if (gemStones.getOwnerId() != player.getObjectId()) {
         return false;
      } else if (gemStones.getItemLocation() != ItemInstance.ItemLocation.INVENTORY) {
         return false;
      } else {
         int grade = item.getItem().getItemGrade();
         AbstractRefinePacket.LifeStone ls = getLifeStone(refinerItem.getId());
         if (getGemStoneId(grade) != gemStones.getId()) {
            return false;
         } else {
            return (long)getGemStoneCount(grade, ls.getGrade()) <= gemStones.getCount();
         }
      }
   }

   protected static final boolean isValid(Player player, ItemInstance item, ItemInstance refinerItem) {
      if (!isValid(player, item)) {
         return false;
      } else if (refinerItem.getOwnerId() != player.getObjectId()) {
         return false;
      } else if (refinerItem.getItemLocation() != ItemInstance.ItemLocation.INVENTORY) {
         return false;
      } else {
         AbstractRefinePacket.LifeStone ls = getLifeStone(refinerItem.getId());
         if (ls == null) {
            return false;
         } else if (item.getItem() instanceof Weapon && ls.getGrade() == 4) {
            return false;
         } else if (item.getItem() instanceof Armor && ls.getGrade() != 4) {
            return false;
         } else {
            return player.getLevel() >= ls.getPlayerLevel();
         }
      }
   }

   protected static final boolean isValid(Player player, ItemInstance item) {
      if (!isValid(player)) {
         return false;
      } else if (item.getOwnerId() != player.getObjectId()) {
         return false;
      } else if (item.isAugmented()) {
         return false;
      } else if (item.isHeroItem()) {
         return false;
      } else if (item.isShadowItem()) {
         return false;
      } else if (item.isCommonItem()) {
         return false;
      } else if (item.isEtcItem()) {
         return false;
      } else if (item.isTimeLimitedItem()) {
         return false;
      } else if (item.isPvp() && !Config.ALT_ALLOW_AUGMENT_PVP_ITEMS) {
         return false;
      } else if (item.getItem().getCrystalType() < 2) {
         return false;
      } else {
         switch(item.getItemLocation()) {
            case INVENTORY:
            case PAPERDOLL:
               if (item.getItem() instanceof Weapon) {
                  switch(((Weapon)item.getItem()).getItemType()) {
                     case NONE:
                     case FISHINGROD:
                        return false;
                  }
               } else {
                  if (!(item.getItem() instanceof Armor)) {
                     return false;
                  }

                  switch(item.getItem().getBodyPart()) {
                     case 6:
                     case 8:
                     case 48:
                        break;
                     default:
                        return false;
                  }
               }

               if (Arrays.binarySearch(Config.AUGMENTATION_BLACKLIST, item.getId()) >= 0) {
                  return false;
               }

               return true;
            default:
               return false;
         }
      }
   }

   protected static final boolean isValid(Player player) {
      if (player.getPrivateStoreType() != 0) {
         player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP_IS_IN_OPERATION);
         return false;
      } else if (player.getActiveTradeList() != null) {
         player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_TRADING);
         return false;
      } else if (player.isDead()) {
         player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_DEAD);
         return false;
      } else if (player.isParalyzed()) {
         player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_PARALYZED);
         return false;
      } else if (player.isFishing()) {
         player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_FISHING);
         return false;
      } else if (player.isSitting()) {
         player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_SITTING_DOWN);
         return false;
      } else if (player.isCursedWeaponEquipped() || player.isActionsDisabled()) {
         return false;
      } else {
         return !player.isEnchanting() && !player.isProcessingTransaction();
      }
   }

   protected static final int getGemStoneId(int itemGrade) {
      switch(itemGrade) {
         case 2:
         case 3:
            return 2130;
         case 4:
         case 5:
            return 2131;
         case 6:
         case 7:
            return 2132;
         default:
            return 0;
      }
   }

   protected static final int getGemStoneCount(int itemGrade, int lifeStoneGrade) {
      switch(lifeStoneGrade) {
         case 4:
            switch(itemGrade) {
               case 2:
                  return 200;
               case 3:
                  return 300;
               case 4:
                  return 200;
               case 5:
                  return 250;
               case 6:
                  return 360;
               case 7:
                  return 480;
               default:
                  return 0;
            }
         default:
            switch(itemGrade) {
               case 2:
                  return 20;
               case 3:
                  return 30;
               case 4:
                  return 20;
               case 5:
                  return 25;
               case 6:
               case 7:
                  return 36;
               default:
                  return 0;
            }
      }
   }

   static {
      _lifeStones.put(8723, new AbstractRefinePacket.LifeStone(0, 0));
      _lifeStones.put(8724, new AbstractRefinePacket.LifeStone(0, 1));
      _lifeStones.put(8725, new AbstractRefinePacket.LifeStone(0, 2));
      _lifeStones.put(8726, new AbstractRefinePacket.LifeStone(0, 3));
      _lifeStones.put(8727, new AbstractRefinePacket.LifeStone(0, 4));
      _lifeStones.put(8728, new AbstractRefinePacket.LifeStone(0, 5));
      _lifeStones.put(8729, new AbstractRefinePacket.LifeStone(0, 6));
      _lifeStones.put(8730, new AbstractRefinePacket.LifeStone(0, 7));
      _lifeStones.put(8731, new AbstractRefinePacket.LifeStone(0, 8));
      _lifeStones.put(8732, new AbstractRefinePacket.LifeStone(0, 9));
      _lifeStones.put(8733, new AbstractRefinePacket.LifeStone(1, 0));
      _lifeStones.put(8734, new AbstractRefinePacket.LifeStone(1, 1));
      _lifeStones.put(8735, new AbstractRefinePacket.LifeStone(1, 2));
      _lifeStones.put(8736, new AbstractRefinePacket.LifeStone(1, 3));
      _lifeStones.put(8737, new AbstractRefinePacket.LifeStone(1, 4));
      _lifeStones.put(8738, new AbstractRefinePacket.LifeStone(1, 5));
      _lifeStones.put(8739, new AbstractRefinePacket.LifeStone(1, 6));
      _lifeStones.put(8740, new AbstractRefinePacket.LifeStone(1, 7));
      _lifeStones.put(8741, new AbstractRefinePacket.LifeStone(1, 8));
      _lifeStones.put(8742, new AbstractRefinePacket.LifeStone(1, 9));
      _lifeStones.put(8743, new AbstractRefinePacket.LifeStone(2, 0));
      _lifeStones.put(8744, new AbstractRefinePacket.LifeStone(2, 1));
      _lifeStones.put(8745, new AbstractRefinePacket.LifeStone(2, 2));
      _lifeStones.put(8746, new AbstractRefinePacket.LifeStone(2, 3));
      _lifeStones.put(8747, new AbstractRefinePacket.LifeStone(2, 4));
      _lifeStones.put(8748, new AbstractRefinePacket.LifeStone(2, 5));
      _lifeStones.put(8749, new AbstractRefinePacket.LifeStone(2, 6));
      _lifeStones.put(8750, new AbstractRefinePacket.LifeStone(2, 7));
      _lifeStones.put(8751, new AbstractRefinePacket.LifeStone(2, 8));
      _lifeStones.put(8752, new AbstractRefinePacket.LifeStone(2, 9));
      _lifeStones.put(8753, new AbstractRefinePacket.LifeStone(3, 0));
      _lifeStones.put(8754, new AbstractRefinePacket.LifeStone(3, 1));
      _lifeStones.put(8755, new AbstractRefinePacket.LifeStone(3, 2));
      _lifeStones.put(8756, new AbstractRefinePacket.LifeStone(3, 3));
      _lifeStones.put(8757, new AbstractRefinePacket.LifeStone(3, 4));
      _lifeStones.put(8758, new AbstractRefinePacket.LifeStone(3, 5));
      _lifeStones.put(8759, new AbstractRefinePacket.LifeStone(3, 6));
      _lifeStones.put(8760, new AbstractRefinePacket.LifeStone(3, 7));
      _lifeStones.put(8761, new AbstractRefinePacket.LifeStone(3, 8));
      _lifeStones.put(8762, new AbstractRefinePacket.LifeStone(3, 9));
      _lifeStones.put(9573, new AbstractRefinePacket.LifeStone(0, 10));
      _lifeStones.put(9574, new AbstractRefinePacket.LifeStone(1, 10));
      _lifeStones.put(9575, new AbstractRefinePacket.LifeStone(2, 10));
      _lifeStones.put(9576, new AbstractRefinePacket.LifeStone(3, 10));
      _lifeStones.put(10483, new AbstractRefinePacket.LifeStone(0, 11));
      _lifeStones.put(10484, new AbstractRefinePacket.LifeStone(1, 11));
      _lifeStones.put(10485, new AbstractRefinePacket.LifeStone(2, 11));
      _lifeStones.put(10486, new AbstractRefinePacket.LifeStone(3, 11));
      _lifeStones.put(12754, new AbstractRefinePacket.LifeStone(4, 0));
      _lifeStones.put(12755, new AbstractRefinePacket.LifeStone(4, 1));
      _lifeStones.put(12756, new AbstractRefinePacket.LifeStone(4, 2));
      _lifeStones.put(12757, new AbstractRefinePacket.LifeStone(4, 3));
      _lifeStones.put(12758, new AbstractRefinePacket.LifeStone(4, 4));
      _lifeStones.put(12759, new AbstractRefinePacket.LifeStone(4, 5));
      _lifeStones.put(12760, new AbstractRefinePacket.LifeStone(4, 6));
      _lifeStones.put(12761, new AbstractRefinePacket.LifeStone(4, 7));
      _lifeStones.put(12762, new AbstractRefinePacket.LifeStone(4, 8));
      _lifeStones.put(12763, new AbstractRefinePacket.LifeStone(4, 9));
      _lifeStones.put(12821, new AbstractRefinePacket.LifeStone(4, 10));
      _lifeStones.put(12822, new AbstractRefinePacket.LifeStone(4, 11));
      _lifeStones.put(12840, new AbstractRefinePacket.LifeStone(4, 0));
      _lifeStones.put(12841, new AbstractRefinePacket.LifeStone(4, 1));
      _lifeStones.put(12842, new AbstractRefinePacket.LifeStone(4, 2));
      _lifeStones.put(12843, new AbstractRefinePacket.LifeStone(4, 3));
      _lifeStones.put(12844, new AbstractRefinePacket.LifeStone(4, 4));
      _lifeStones.put(12845, new AbstractRefinePacket.LifeStone(4, 5));
      _lifeStones.put(12846, new AbstractRefinePacket.LifeStone(4, 6));
      _lifeStones.put(12847, new AbstractRefinePacket.LifeStone(4, 7));
      _lifeStones.put(12848, new AbstractRefinePacket.LifeStone(4, 8));
      _lifeStones.put(12849, new AbstractRefinePacket.LifeStone(4, 9));
      _lifeStones.put(12850, new AbstractRefinePacket.LifeStone(4, 10));
      _lifeStones.put(12851, new AbstractRefinePacket.LifeStone(4, 11));
      _lifeStones.put(14008, new AbstractRefinePacket.LifeStone(4, 12));
      _lifeStones.put(14166, new AbstractRefinePacket.LifeStone(0, 12));
      _lifeStones.put(14167, new AbstractRefinePacket.LifeStone(1, 12));
      _lifeStones.put(14168, new AbstractRefinePacket.LifeStone(2, 12));
      _lifeStones.put(14169, new AbstractRefinePacket.LifeStone(3, 12));
      _lifeStones.put(16160, new AbstractRefinePacket.LifeStone(0, 13));
      _lifeStones.put(16161, new AbstractRefinePacket.LifeStone(1, 13));
      _lifeStones.put(16162, new AbstractRefinePacket.LifeStone(2, 13));
      _lifeStones.put(16163, new AbstractRefinePacket.LifeStone(3, 13));
      _lifeStones.put(16177, new AbstractRefinePacket.LifeStone(4, 13));
      _lifeStones.put(16164, new AbstractRefinePacket.LifeStone(0, 13));
      _lifeStones.put(16165, new AbstractRefinePacket.LifeStone(1, 13));
      _lifeStones.put(16166, new AbstractRefinePacket.LifeStone(2, 13));
      _lifeStones.put(16167, new AbstractRefinePacket.LifeStone(3, 13));
      _lifeStones.put(16178, new AbstractRefinePacket.LifeStone(4, 13));
      _lifeStones.put(83200, new AbstractRefinePacket.LifeStone(2, 13));
      _lifeStones.put(31109, new AbstractRefinePacket.LifeStone(2, 13));
      _lifeStones.put(31110, new AbstractRefinePacket.LifeStone(2, 13));
   }

   protected static final class LifeStone {
      private static final int[] LEVELS = new int[]{46, 49, 52, 55, 58, 61, 64, 67, 70, 76, 80, 82, 84, 85};
      private final int _grade;
      private final int _level;

      public LifeStone(int grade, int level) {
         this._grade = grade;
         this._level = level;
      }

      public final int getLevel() {
         return this._level;
      }

      public final int getGrade() {
         return this._grade;
      }

      public final int getPlayerLevel() {
         return LEVELS[this._level];
      }
   }
}
