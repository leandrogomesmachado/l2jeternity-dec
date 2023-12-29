package l2e.gameserver.model;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.stats.StatsSet;

public final class EnchantSkillGroup {
   private final int _id;
   private final List<EnchantSkillGroup.EnchantSkillsHolder> _enchantDetails = new ArrayList<>();

   public EnchantSkillGroup(int id) {
      this._id = id;
   }

   public void addEnchantDetail(EnchantSkillGroup.EnchantSkillsHolder detail) {
      this._enchantDetails.add(detail);
   }

   public int getId() {
      return this._id;
   }

   public List<EnchantSkillGroup.EnchantSkillsHolder> getEnchantGroupDetails() {
      return this._enchantDetails;
   }

   public static class EnchantSkillsHolder {
      private final int _level;
      private final int _adenaCost;
      private final int _expCost;
      private final int _spCost;
      private final byte[] _rate;

      public EnchantSkillsHolder(StatsSet set) {
         this._level = set.getInteger("level");
         this._adenaCost = set.getInteger("adena");
         this._expCost = set.getInteger("exp");
         this._spCost = set.getInteger("sp");
         this._rate = new byte[24];

         for(int i = 0; i < 24; ++i) {
            this._rate[i] = set.getByte("chance" + (76 + i), (byte)0);
         }
      }

      public int getLevel() {
         return this._level;
      }

      public int getSpCost() {
         return this._spCost;
      }

      public int getExpCost() {
         return this._expCost;
      }

      public int getAdenaCost() {
         return this._adenaCost;
      }

      public byte getRate(Player ply) {
         return ply.getLevel() < 76 ? 0 : this._rate[ply.getLevel() - 76];
      }
   }
}
