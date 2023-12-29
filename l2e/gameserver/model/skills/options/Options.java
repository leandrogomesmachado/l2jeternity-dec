package l2e.gameserver.model.skills.options;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.funcs.Func;
import l2e.gameserver.model.skills.funcs.FuncTemplate;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.serverpackets.SkillCoolTime;

public class Options {
   private final int _id;
   private static final Func[] _emptyFunctionSet = new Func[0];
   private final List<FuncTemplate> _funcs = new ArrayList<>();
   private SkillHolder _activeSkill = null;
   private SkillHolder _passiveSkill = null;
   private final List<OptionsSkillHolder> _activationSkills = new ArrayList<>();

   public Options(int id) {
      this._id = id;
   }

   public final int getId() {
      return this._id;
   }

   public boolean hasFuncs() {
      return !this._funcs.isEmpty();
   }

   public Func[] getStatFuncs(ItemInstance item, Creature player) {
      if (this._funcs.isEmpty()) {
         return _emptyFunctionSet;
      } else {
         List<Func> funcs = new ArrayList<>(this._funcs.size());
         Env env = new Env();
         env.setCharacter(player);
         env.setTarget(player);
         env.setItem(item);

         for(FuncTemplate t : this._funcs) {
            Func f = t.getFunc(env, this);
            if (f != null) {
               funcs.add(f);
            }

            player.sendDebugMessage("Adding stats: " + t.stat + " val: " + t.lambda.calc(env));
         }

         return funcs.isEmpty() ? _emptyFunctionSet : funcs.toArray(new Func[funcs.size()]);
      }
   }

   public void addFunc(FuncTemplate template) {
      this._funcs.add(template);
   }

   public boolean hasActiveSkill() {
      return this._activeSkill != null;
   }

   public SkillHolder getActiveSkill() {
      return this._activeSkill;
   }

   public void setActiveSkill(SkillHolder holder) {
      this._activeSkill = holder;
   }

   public boolean hasPassiveSkill() {
      return this._passiveSkill != null;
   }

   public SkillHolder getPassiveSkill() {
      return this._passiveSkill;
   }

   public void setPassiveSkill(SkillHolder holder) {
      this._passiveSkill = holder;
   }

   public boolean hasActivationSkills() {
      return !this._activationSkills.isEmpty();
   }

   public boolean hasActivationSkills(OptionsSkillType type) {
      for(OptionsSkillHolder holder : this._activationSkills) {
         if (holder.getSkillType() == type) {
            return true;
         }
      }

      return false;
   }

   public List<OptionsSkillHolder> getActivationsSkills() {
      return this._activationSkills;
   }

   public List<OptionsSkillHolder> getActivationsSkills(OptionsSkillType type) {
      List<OptionsSkillHolder> temp = new ArrayList<>();

      for(OptionsSkillHolder holder : this._activationSkills) {
         if (holder.getSkillType() == type) {
            temp.add(holder);
         }
      }

      return temp;
   }

   public void addActivationSkill(OptionsSkillHolder holder) {
      this._activationSkills.add(holder);
   }

   public void apply(Player player) {
      player.sendDebugMessage("Activating option id: " + this._id);
      if (this.hasFuncs()) {
         player.addStatFuncs(this.getStatFuncs(null, player));
      }

      if (this.hasActiveSkill()) {
         this.addSkill(player, this.getActiveSkill().getSkill());
         player.sendDebugMessage("Adding active skill: " + this.getActiveSkill());
      }

      if (this.hasPassiveSkill()) {
         this.addSkill(player, this.getPassiveSkill().getSkill());
         player.sendDebugMessage("Adding passive skill: " + this.getPassiveSkill());
      }

      if (this.hasActivationSkills()) {
         for(OptionsSkillHolder holder : this._activationSkills) {
            player.addTriggerSkill(holder);
            player.sendDebugMessage("Adding trigger skill: " + holder);
         }
      }

      player.sendSkillList(false);
   }

   public void remove(Player player) {
      player.sendDebugMessage("Deactivating option id: " + this._id);
      if (this.hasFuncs()) {
         player.removeStatsOwner(this);
      }

      if (this.hasActiveSkill()) {
         player.removeSkill(this.getActiveSkill().getSkill(), false, false);
         player.sendDebugMessage("Removing active skill: " + this.getActiveSkill());
      }

      if (this.hasPassiveSkill()) {
         player.removeSkill(this.getPassiveSkill().getSkill(), false, true);
         player.sendDebugMessage("Removing passive skill: " + this.getPassiveSkill());
      }

      if (this.hasActivationSkills()) {
         for(OptionsSkillHolder holder : this._activationSkills) {
            player.removeTriggerSkill(holder);
            player.sendDebugMessage("Removing trigger skill: " + holder);
         }
      }

      player.sendSkillList(false);
   }

   private final void addSkill(Player player, Skill skill) {
      boolean updateTimeStamp = false;
      player.addSkill(skill, false);
      if (skill.isActive()) {
         long remainingTime = player.getSkillRemainingReuseTime(skill.getReuseHashCode());
         if (remainingTime > 0L) {
            player.addTimeStamp(skill, remainingTime);
            player.disableSkill(skill, remainingTime);
         }

         updateTimeStamp = true;
      }

      if (updateTimeStamp) {
         player.sendPacket(new SkillCoolTime(player));
      }
   }

   public static enum AugmentationFilter {
      NONE,
      ACTIVE_SKILL,
      PASSIVE_SKILL,
      CHANCE_SKILL,
      STATS;
   }
}
