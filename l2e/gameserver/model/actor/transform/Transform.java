package l2e.gameserver.model.actor.transform;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.data.parser.SkillTreesParser;
import l2e.gameserver.model.SkillLearn;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.holders.AdditionalItemHolder;
import l2e.gameserver.model.holders.AdditionalSkillHolder;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.interfaces.IIdentifiable;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.stats.Stats;
import l2e.gameserver.model.stats.StatsSet;
import l2e.gameserver.network.serverpackets.ExBasicActionList;

public final class Transform implements IIdentifiable {
   private final int _id;
   private final int _displayId;
   private final TransformType _type;
   private final boolean _canSwim;
   private final int _spawnHeight;
   private final boolean _canAttack;
   private final String _name;
   private final String _title;
   private TransformTemplate _maleTemplate;
   private TransformTemplate _femaleTemplate;

   public Transform(StatsSet set) {
      this._id = set.getInteger("id");
      this._displayId = set.getInteger("displayId", this._id);
      this._type = set.getEnum("type", TransformType.class, TransformType.COMBAT);
      this._canSwim = set.getInteger("can_swim", 0) == 1;
      this._canAttack = set.getInteger("normal_attackable", 1) == 1;
      this._spawnHeight = set.getInteger("spawn_height", 0);
      this._name = set.getString("setName", null);
      this._title = set.getString("setTitle", null);
   }

   @Override
   public int getId() {
      return this._id;
   }

   public int getDisplayId() {
      return this._displayId;
   }

   public TransformType getType() {
      return this._type;
   }

   public boolean canSwim() {
      return this._canSwim;
   }

   public boolean canAttack() {
      return this._canAttack;
   }

   public int getSpawnHeight() {
      return this._spawnHeight;
   }

   public String getName() {
      return this._name;
   }

   public String getTitle() {
      return this._title;
   }

   public TransformTemplate getTemplate(Player player) {
      return player != null ? (player.getAppearance().getSex() ? this._femaleTemplate : this._maleTemplate) : null;
   }

   public void setTemplate(boolean male, TransformTemplate template) {
      if (male) {
         this._maleTemplate = template;
      } else {
         this._femaleTemplate = template;
      }
   }

   public boolean isStance() {
      return this._type == TransformType.MODE_CHANGE;
   }

   public boolean isCombat() {
      return this._type == TransformType.COMBAT;
   }

   public boolean isNonCombat() {
      return this._type == TransformType.NON_COMBAT;
   }

   public boolean isFlying() {
      return this._type == TransformType.FLYING;
   }

   public boolean isCursed() {
      return this._type == TransformType.CURSED;
   }

   public boolean isRiding() {
      return this._type == TransformType.RIDING_MODE;
   }

   public boolean isPureStats() {
      return this._type == TransformType.PURE_STAT;
   }

   public double getCollisionHeight(Player player) {
      TransformTemplate template = this.getTemplate(player);
      return template != null
         ? template.getCollisionHeight()
         : (player.getAppearance().getSex() ? player.getBaseTemplate().getFCollisionHeightFemale() : player.getBaseTemplate().getfCollisionHeight());
   }

   public double getCollisionRadius(Player player) {
      TransformTemplate template = this.getTemplate(player);
      return template != null
         ? template.getCollisionRadius()
         : (player.getAppearance().getSex() ? player.getBaseTemplate().getFCollisionRadiusFemale() : player.getBaseTemplate().getfCollisionRadius());
   }

   public int getBaseAttackRange(Player player) {
      TransformTemplate template = this.getTemplate(player);
      return template != null ? template.getBaseAttackRange() : player.getTemplate().getBaseAttackRange();
   }

   public void onTransform(Player player) {
      TransformTemplate template = this.getTemplate(player);
      if (template != null) {
         if (this.isFlying()) {
            player.setIsFlying(true);
         }

         if (this.getName() != null) {
            player.getAppearance().setVisibleName(this.getName());
         }

         if (this.getTitle() != null) {
            player.getAppearance().setVisibleTitle(this.getTitle());
         }

         for(SkillHolder holder : template.getSkills()) {
            if (player.getSkillLevel(holder.getId()) < holder.getLvl()) {
               player.addSkill(holder.getSkill(), false);
            }

            player.addTransformSkill(holder.getId());
         }

         for(AdditionalSkillHolder holder : template.getAdditionalSkills()) {
            if (player.getLevel() >= holder.getMinLevel()) {
               if (player.getSkillLevel(holder.getId()) < holder.getLvl()) {
                  player.addSkill(holder.getSkill(), false);
               }

               player.addTransformSkill(holder.getId());
            }
         }

         for(SkillLearn skill : SkillTreesParser.getInstance().getCollectSkillTree().values()) {
            if (player.getKnownSkill(skill.getId()) != null) {
               player.addTransformSkill(skill.getId());
            }
         }

         if (!template.getAdditionalItems().isEmpty()) {
            List<Integer> allowed = new ArrayList<>();
            List<Integer> notAllowed = new ArrayList<>();

            for(AdditionalItemHolder holder : template.getAdditionalItems()) {
               if (holder.isAllowedToUse()) {
                  allowed.add(holder.getId());
               } else {
                  notAllowed.add(holder.getId());
               }
            }

            if (!allowed.isEmpty()) {
               int[] items = new int[allowed.size()];

               for(int i = 0; i < items.length; ++i) {
                  items[i] = allowed.get(i);
               }

               player.getInventory().setInventoryBlock(items, 1);
            }

            if (!notAllowed.isEmpty()) {
               int[] items = new int[notAllowed.size()];

               for(int i = 0; i < items.length; ++i) {
                  items[i] = notAllowed.get(i);
               }

               player.getInventory().setInventoryBlock(items, 2);
            }
         }

         if (template.hasBasicActionList()) {
            player.sendPacket(template.getBasicActionList());
         }
      }
   }

   public void onUntransform(Player player) {
      TransformTemplate template = this.getTemplate(player);
      if (template != null) {
         if (this.isFlying()) {
            player.setIsFlying(false);
         }

         if (this.getName() != null) {
            player.getAppearance().setVisibleName(null);
         }

         if (this.getTitle() != null) {
            player.getAppearance().setVisibleTitle(null);
         }

         if (!template.getSkills().isEmpty()) {
            for(SkillHolder holder : template.getSkills()) {
               Skill skill = holder.getSkill();
               if (!SkillTreesParser.getInstance().isSkillAllowed(player, skill)) {
                  player.removeSkill(skill, false, skill.isPassive());
               }
            }
         }

         if (!template.getAdditionalSkills().isEmpty()) {
            for(AdditionalSkillHolder holder : template.getAdditionalSkills()) {
               Skill skill = holder.getSkill();
               if (player.getLevel() >= holder.getMinLevel() && !SkillTreesParser.getInstance().isSkillAllowed(player, skill)) {
                  player.removeSkill(skill, false, skill.isPassive());
               }
            }
         }

         player.removeAllTransformSkills();
         if (!template.getAdditionalItems().isEmpty()) {
            player.getInventory().unblock();
         }

         player.sendPacket(ExBasicActionList.STATIC_PACKET);
      }
   }

   public void onLevelUp(Player player) {
      TransformTemplate template = this.getTemplate(player);
      if (template != null && !template.getAdditionalSkills().isEmpty()) {
         for(AdditionalSkillHolder holder : template.getAdditionalSkills()) {
            if (player.getLevel() >= holder.getMinLevel() && player.getSkillLevel(holder.getId()) < holder.getLvl()) {
               player.addSkill(holder.getSkill(), false);
               player.addTransformSkill(holder.getId());
            }
         }
      }
   }

   public double getStat(Player player, Stats stats) {
      double val = 0.0;
      TransformTemplate template = this.getTemplate(player);
      if (template != null) {
         val = template.getStats(stats);
         TransformLevelData data = template.getData(player.getLevel());
         if (data != null) {
            val = data.getStats(stats);
         }
      }

      return val;
   }

   public int getBaseDefBySlot(Player player, int slot) {
      TransformTemplate template = this.getTemplate(player);
      return template != null ? template.getDefense(slot) : player.getTemplate().getBaseDefBySlot(slot);
   }

   public double getLevelMod(Player player) {
      double val = -1.0;
      TransformTemplate template = this.getTemplate(player);
      if (template != null) {
         TransformLevelData data = template.getData(player.getLevel());
         if (data != null) {
            val = data.getLevelMod();
         }
      }

      return val;
   }
}
