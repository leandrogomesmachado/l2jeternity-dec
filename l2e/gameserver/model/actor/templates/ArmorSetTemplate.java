package l2e.gameserver.model.actor.templates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.itemcontainer.Inventory;

public final class ArmorSetTemplate {
   private int _chestId;
   private final List<Integer> _legs = new ArrayList<>();
   private final List<Integer> _head = new ArrayList<>();
   private final List<Integer> _gloves = new ArrayList<>();
   private final List<Integer> _feet = new ArrayList<>();
   private final List<Integer> _shield = new ArrayList<>();
   private final List<SkillHolder> _skills = new ArrayList<>();
   private final List<SkillHolder> _shieldSkills = new ArrayList<>();
   private final List<SkillHolder> _enchant6Skill = new ArrayList<>();
   private final Map<Integer, SkillHolder> _enchantByLevel = new HashMap<>();
   private int _con;
   private int _dex;
   private int _str;
   private int _men;
   private int _wit;
   private int _int;

   public void addChest(int id) {
      this._chestId = id;
   }

   public void addLegs(int id) {
      this._legs.add(id);
   }

   public void addHead(int id) {
      this._head.add(id);
   }

   public void addGloves(int id) {
      this._gloves.add(id);
   }

   public void addFeet(int id) {
      this._feet.add(id);
   }

   public void addShield(int id) {
      this._shield.add(id);
   }

   public void addSkill(SkillHolder holder) {
      this._skills.add(holder);
   }

   public void addShieldSkill(SkillHolder holder) {
      this._shieldSkills.add(holder);
   }

   public void addEnchant6Skill(SkillHolder holder) {
      this._enchant6Skill.add(holder);
   }

   public void addCon(int val) {
      this._con = val;
   }

   public void addDex(int val) {
      this._dex = val;
   }

   public void addStr(int val) {
      this._str = val;
   }

   public void addMen(int val) {
      this._men = val;
   }

   public void addWit(int val) {
      this._wit = val;
   }

   public void addInt(int val) {
      this._int = val;
   }

   public boolean containAll(Player player) {
      Inventory inv = player.getInventory();
      ItemInstance legsItem = inv.getPaperdollItem(11);
      ItemInstance headItem = inv.getPaperdollItem(1);
      ItemInstance glovesItem = inv.getPaperdollItem(10);
      ItemInstance feetItem = inv.getPaperdollItem(12);
      int legs = 0;
      int head = 0;
      int gloves = 0;
      int feet = 0;
      if (legsItem != null) {
         legs = legsItem.getId();
      }

      if (headItem != null) {
         head = headItem.getId();
      }

      if (glovesItem != null) {
         gloves = glovesItem.getId();
      }

      if (feetItem != null) {
         feet = feetItem.getId();
      }

      return this.containAll(this._chestId, legs, head, gloves, feet);
   }

   public boolean containAll(int chest, int legs, int head, int gloves, int feet) {
      if (this._chestId != 0 && this._chestId != chest) {
         return false;
      } else if (!this._legs.isEmpty() && !this._legs.contains(legs)) {
         return false;
      } else if (!this._head.isEmpty() && !this._head.contains(head)) {
         return false;
      } else if (!this._gloves.isEmpty() && !this._gloves.contains(gloves)) {
         return false;
      } else {
         return this._feet.isEmpty() || this._feet.contains(feet);
      }
   }

   public boolean containItem(int slot, int itemId) {
      switch(slot) {
         case 1:
            return this._head.contains(itemId);
         case 2:
         case 3:
         case 4:
         case 5:
         case 7:
         case 8:
         case 9:
         default:
            return false;
         case 6:
            return this._chestId == itemId;
         case 10:
            return this._gloves.contains(itemId);
         case 11:
            return this._legs.contains(itemId);
         case 12:
            return this._feet.contains(itemId);
      }
   }

   public int getChestId() {
      return this._chestId;
   }

   public List<SkillHolder> getSkills() {
      return this._skills;
   }

   public boolean containShield(Player player) {
      Inventory inv = player.getInventory();
      ItemInstance shieldItem = inv.getPaperdollItem(7);
      return shieldItem != null && this._shield.contains(shieldItem.getId());
   }

   public boolean containShield(int shield_id) {
      return this._shield.isEmpty() ? false : this._shield.contains(shield_id);
   }

   public List<SkillHolder> getShieldSkillId() {
      return this._shieldSkills;
   }

   public List<SkillHolder> getEnchant6skillId() {
      return this._enchant6Skill;
   }

   public boolean isEnchanted6(Player player) {
      if (!this.containAll(player)) {
         return false;
      } else {
         Inventory inv = player.getInventory();
         ItemInstance chestItem = inv.getPaperdollItem(6);
         ItemInstance legsItem = inv.getPaperdollItem(11);
         ItemInstance headItem = inv.getPaperdollItem(1);
         ItemInstance glovesItem = inv.getPaperdollItem(10);
         ItemInstance feetItem = inv.getPaperdollItem(12);
         if (chestItem != null && chestItem.getEnchantLevel() >= 6) {
            if (this._legs.isEmpty() || legsItem != null && legsItem.getEnchantLevel() >= 6) {
               if (this._gloves.isEmpty() || glovesItem != null && glovesItem.getEnchantLevel() >= 6) {
                  if (this._head.isEmpty() || headItem != null && headItem.getEnchantLevel() >= 6) {
                     return this._feet.isEmpty() || feetItem != null && feetItem.getEnchantLevel() >= 6;
                  } else {
                     return false;
                  }
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   public int getCON() {
      return this._con;
   }

   public int getDEX() {
      return this._dex;
   }

   public int getSTR() {
      return this._str;
   }

   public int getMEN() {
      return this._men;
   }

   public int getWIT() {
      return this._wit;
   }

   public int getINT() {
      return this._int;
   }

   public void addEnchantByLevel(int enchLvl, SkillHolder holder) {
      this._enchantByLevel.put(enchLvl, holder);
   }

   public Map<Integer, SkillHolder> getEnchantByLevel() {
      return this._enchantByLevel;
   }

   public boolean isEnchantedByLevel(Player player, int enchLvl) {
      if (!this.containAll(player)) {
         return false;
      } else {
         Inventory inv = player.getInventory();
         ItemInstance chestItem = inv.getPaperdollItem(6);
         ItemInstance legsItem = inv.getPaperdollItem(11);
         ItemInstance headItem = inv.getPaperdollItem(1);
         ItemInstance glovesItem = inv.getPaperdollItem(10);
         ItemInstance feetItem = inv.getPaperdollItem(12);
         if (chestItem != null && chestItem.getEnchantLevel() >= enchLvl) {
            if (this._legs.isEmpty() || legsItem != null && legsItem.getEnchantLevel() >= enchLvl) {
               if (this._gloves.isEmpty() || glovesItem != null && glovesItem.getEnchantLevel() >= enchLvl) {
                  if (this._head.isEmpty() || headItem != null && headItem.getEnchantLevel() >= enchLvl) {
                     return this._feet.isEmpty() || feetItem != null && feetItem.getEnchantLevel() >= enchLvl;
                  } else {
                     return false;
                  }
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }
}
