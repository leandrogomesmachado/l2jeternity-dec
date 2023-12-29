package l2e.gameserver.model.actor.instance.player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import l2e.commons.annotations.NotNull;
import l2e.commons.util.Rnd;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.holder.SummonSkillsHolder;
import l2e.gameserver.data.parser.PetsParser;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.instancemanager.AutoFarmManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.actor.instance.TreasureChestInstance;
import l2e.gameserver.model.actor.tasks.player.AutoArcherFarmTask;
import l2e.gameserver.model.actor.tasks.player.AutoFarmEndTask;
import l2e.gameserver.model.actor.tasks.player.AutoHealFarmTask;
import l2e.gameserver.model.actor.tasks.player.AutoMagicFarmTask;
import l2e.gameserver.model.actor.tasks.player.AutoPhysicalFarmTask;
import l2e.gameserver.model.actor.tasks.player.AutoSummonFarmTask;
import l2e.gameserver.model.actor.templates.ShortCutTemplate;
import l2e.gameserver.model.base.ShortcutType;
import l2e.gameserver.model.service.autofarm.FarmSettings;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.SkillType;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.skills.targets.TargetType;

public class AutoFarmOptions {
   private int _farmType;
   private int _shortcutsIndex;
   private int _radius;
   private int _attackSkillChance;
   private boolean _rndAttackSkills = false;
   private int _chanceSkillChance;
   private boolean _rndChanceSkills = false;
   private int _selfSkillChance;
   private boolean _rndSelfSkills = false;
   private int _lifeSkillChance;
   private boolean _rndLifeSkills = false;
   private int _attackSummonSkillChance;
   private boolean _rndSummonAttackSkills = false;
   private int _selfSummonSkillChance;
   private boolean _rndSummonSelfSkills = false;
   private int _lifeSummonSkillChance;
   private boolean _rndSummonLifeSkills = false;
   private int _attackSummonSkillPercent;
   private int _selfSummonSkillPercent;
   private int _lifeSummonSkillPercent;
   private int _attackSkillPercent;
   private int _chanceSkillPercent;
   private int _selfSkillPercent;
   private int _lifeSkillPercent;
   private boolean _leaderAssist = false;
   private boolean _keepLocation = false;
   private boolean _exDelaySkill = false;
   private boolean _exSummonDelaySkill = false;
   private boolean _runTargetCloseUp = false;
   private boolean _isAssistMonsterAttack = false;
   private boolean _isTargetRestoreMp = false;
   private boolean _useSummonSkills = false;
   private long _autoFarmEnd;
   private long _farmOnlineTime = 0L;
   private long _farmLastOnlineTime = 0L;
   private boolean _activeFarmOnlineTime = false;
   private final List<Integer> _attackSlots = Arrays.asList(0, 1, 2, 3);
   private final List<Integer> _chanceSlots = Arrays.asList(4, 5);
   private final List<Integer> _selfSlots = Arrays.asList(6, 7, 8, 9);
   private final List<Integer> _lowLifeSlots = Arrays.asList(10, 11);
   private final List<Integer> _attackSkills = new ArrayList<>();
   private final List<Integer> _chanceSkills = new ArrayList<>();
   private final List<Integer> _selfSkills = new ArrayList<>();
   private final List<Integer> _lowLifeSkills = new ArrayList<>();
   private final List<Integer> _summonAttackSkills = new ArrayList<>();
   private final List<Integer> _summonSelfSkills = new ArrayList<>();
   private final List<Integer> _summonHealSkills = new ArrayList<>();
   private Location keepLocation = null;
   private final Player _player;
   private ScheduledFuture<?> _farmTask;
   private ScheduledFuture<?> _farmEndTask;

   public AutoFarmOptions(Player player) {
      this._player = player;
   }

   public void setFarmTypeValue(int value) {
      if (value < 0) {
         value = 0;
      } else if (value > 4) {
         value = 4;
      }

      this._farmType = value;
      if (this.isAutofarming()) {
         this.stopFarmTask(true);
      }
   }

   private int getFarmType() {
      return this._farmType;
   }

   public void setRadiusValue(int value) {
      this._radius = value;
   }

   public void setShortcutPageValue(int value) {
      if (value < 1) {
         value = 1;
      } else if (value > 10) {
         value = 10;
      }

      int correct = value - 1;
      this._shortcutsIndex = correct;
   }

   public int getAttackPercent() {
      return this._attackSkillPercent;
   }

   public int getAttackChance() {
      return this._attackSkillChance;
   }

   public int getChancePercent() {
      return this._chanceSkillPercent;
   }

   public int getChanceChance() {
      return this._chanceSkillChance;
   }

   public int getSelfPercent() {
      return this._selfSkillPercent;
   }

   public int getSelfChance() {
      return this._selfSkillChance;
   }

   public int getLifePercent() {
      return this._lifeSkillPercent;
   }

   public int getLifeChance() {
      return this._lifeSkillChance;
   }

   public void setAttackSkillValue(boolean isPercent, int value) {
      if (isPercent) {
         this._attackSkillPercent = value;
      } else {
         this._attackSkillChance = value;
      }
   }

   public void setChanceSkillValue(boolean isPercent, int value) {
      if (isPercent) {
         this._chanceSkillPercent = value;
      } else {
         this._chanceSkillChance = value;
      }
   }

   public void setSelfSkillValue(boolean isPercent, int value) {
      if (isPercent) {
         this._selfSkillPercent = value;
      } else {
         this._selfSkillChance = value;
      }
   }

   public void setLifeSkillValue(boolean isPercent, int value) {
      if (isPercent) {
         this._lifeSkillPercent = value;
      } else {
         this._lifeSkillChance = value;
      }
   }

   public void restoreVariables() {
      this.setAttackSkillValue(false, this._player.getVarInt("attackChanceSkills", FarmSettings.ATTACK_SKILL_CHANCE));
      this.setAttackSkillValue(true, this._player.getVarInt("attackSkillsPercent", FarmSettings.ATTACK_SKILL_PERCENT));
      this.setChanceSkillValue(false, this._player.getVarInt("chanceChanceSkills", FarmSettings.CHANCE_SKILL_CHANCE));
      this.setChanceSkillValue(true, this._player.getVarInt("chanceSkillsPercent", FarmSettings.CHANCE_SKILL_PERCENT));
      this.setSelfSkillValue(false, this._player.getVarInt("selfChanceSkills", FarmSettings.SELF_SKILL_CHANCE));
      this.setSelfSkillValue(true, this._player.getVarInt("selfSkillsPercent", FarmSettings.SELF_SKILL_PERCENT));
      this.setLifeSkillValue(false, this._player.getVarInt("healChanceSkills", FarmSettings.HEAL_SKILL_CHANCE));
      this.setLifeSkillValue(true, this._player.getVarInt("healSkillsPercent", FarmSettings.HEAL_SKILL_PERCENT));
      this.setSummonAttackSkillValue(false, this._player.getVarInt("attackSummonChanceSkills", FarmSettings.SUMMON_ATTACK_SKILL_CHANCE));
      this.setSummonAttackSkillValue(true, this._player.getVarInt("attackSummonSkillsPercent", FarmSettings.SUMMON_ATTACK_SKILL_PERCENT));
      this.setSummonSelfSkillValue(false, this._player.getVarInt("selfSummonChanceSkills", FarmSettings.SUMMON_SELF_SKILL_CHANCE));
      this.setSummonSelfSkillValue(true, this._player.getVarInt("selfSummonSkillsPercent", FarmSettings.SUMMON_SELF_SKILL_PERCENT));
      this.setSummonLifeSkillValue(false, this._player.getVarInt("healSummonChanceSkills", FarmSettings.SUMMON_HEAL_SKILL_CHANCE));
      this.setSummonLifeSkillValue(true, this._player.getVarInt("healSummonSkillsPercent", FarmSettings.SUMMON_HEAL_SKILL_PERCENT));
      this.setShortcutPageValue(this._player.getVarInt("shortcutPage", FarmSettings.SHORTCUT_PAGE));
      this.setRadiusValue(this._player.getVarInt("farmDistance", FarmSettings.SEARCH_DISTANCE));
      this.setFarmTypeValue(this._player.getVarInt("farmType", FarmSettings.FARM_TYPE));
      this.setRndAttackSkills(this._player.getVarB("farmRndAttackSkills", false), true);
      this.setRndChanceSkills(this._player.getVarB("farmRndChanceSkills", false), true);
      this.setRndSelfSkills(this._player.getVarB("farmRndSelfSkills", false), true);
      this.setRndLifeSkills(this._player.getVarB("farmRndLifeSkills", false), true);
      this.setRndSummonAttackSkills(this._player.getVarB("farmRndSummonAttackSkills", false), true);
      this.setRndSummonSelfSkills(this._player.getVarB("farmRndSummonSelfSkills", false), true);
      this.setRndSummonLifeSkills(this._player.getVarB("farmRndSummonLifeSkills", false), true);
      this.setLeaderAssist(this._player.getVarB("farmLeaderAssist", false), true);
      this.setKeepLocation(this._player.getVarB("farmKeepLocation", false), true);
      this.setExDelaySkill(this._player.getVarB("farmExDelaySkill", false), true);
      this.setExSummonDelaySkill(this._player.getVarB("farmExSummonDelaySkill", false), true);
      this.setRunTargetCloseUp(this._player.getVarB("farmRunTargetCloseUp", false), true);
      this.setUseSummonSkills(this._player.getVarB("farmUseSummonSkills", false), true);
      this.setAssistMonsterAttack(this._player.getVarB("farmAssistMonsterAttack", false), true);
      this.setTargetRestoreMp(this._player.getVarB("farmTargetRestoreMp", false), true);
      this.restoreSkills();
   }

   private void restoreSkills() {
      String attackSkills = this._player.getVar("farmAttackSkills", null);
      if (attackSkills != null && !attackSkills.isEmpty()) {
         this.getAttackSpells().clear();
         String[] skills = attackSkills.split(";");

         for(String sk : skills) {
            if (sk != null) {
               Skill skill = this._player.getKnownSkill(Integer.parseInt(sk));
               if (skill != null) {
                  this.getAttackSpells().add(skill.getId());
               }
            }
         }
      }

      String chanceSkills = this._player.getVar("farmChanceSkills", null);
      if (chanceSkills != null && !chanceSkills.isEmpty()) {
         this.getChanceSpells().clear();
         String[] skills = chanceSkills.split(";");

         for(String sk : skills) {
            if (sk != null) {
               Skill skill = this._player.getKnownSkill(Integer.parseInt(sk));
               if (skill != null) {
                  this.getChanceSpells().add(skill.getId());
               }
            }
         }
      }

      String selfSkills = this._player.getVar("farmSelfSkills", null);
      if (selfSkills != null && !selfSkills.isEmpty()) {
         this.getSelfSpells().clear();
         String[] skills = selfSkills.split(";");

         for(String sk : skills) {
            if (sk != null) {
               Skill skill = this._player.getKnownSkill(Integer.parseInt(sk));
               if (skill != null) {
                  this.getSelfSpells().add(skill.getId());
               }
            }
         }
      }

      String healSkills = this._player.getVar("farmHealSkills", null);
      if (healSkills != null && !healSkills.isEmpty()) {
         this.getLowLifeSpells().clear();
         String[] skills = healSkills.split(";");

         for(String sk : skills) {
            if (sk != null) {
               Skill skill = this._player.getKnownSkill(Integer.parseInt(sk));
               if (skill != null) {
                  this.getLowLifeSpells().add(skill.getId());
               }
            }
         }
      }

      String attackSummonSkills = this._player.getVar("farmAttackSummonSkills", null);
      if (attackSummonSkills != null && !attackSummonSkills.isEmpty()) {
         this.getSummonAttackSpells().clear();
         String[] skills = attackSummonSkills.split(";");

         for(String sk : skills) {
            if (sk != null) {
               this.getSummonAttackSpells().add(Integer.parseInt(sk));
            }
         }
      }

      String selfSummonSkills = this._player.getVar("farmSelfSummonSkills", null);
      if (selfSummonSkills != null && !selfSummonSkills.isEmpty()) {
         this.getSummonSelfSpells().clear();
         String[] skills = selfSummonSkills.split(";");

         for(String sk : skills) {
            if (sk != null) {
               this.getSummonSelfSpells().add(Integer.parseInt(sk));
            }
         }
      }

      String healSummonSkills = this._player.getVar("farmHealSummonSkills", null);
      if (healSummonSkills != null && !healSummonSkills.isEmpty()) {
         this.getSummonHealSpells().clear();
         String[] skills = healSummonSkills.split(";");

         for(String sk : skills) {
            if (sk != null) {
               this.getSummonHealSpells().add(Integer.parseInt(sk));
            }
         }
      }
   }

   public void saveSkills(String type) {
      List<Integer> skillList = null;
      switch(type) {
         case "farmAttackSkills":
            skillList = this.getAttackSpells();
            break;
         case "farmChanceSkills":
            skillList = this.getChanceSpells();
            break;
         case "farmSelfSkills":
            skillList = this.getSelfSpells();
            break;
         case "farmHealSkills":
            skillList = this.getLowLifeSpells();
            break;
         case "farmAttackSummonSkills":
            skillList = this.getSummonAttackSpells();
            break;
         case "farmSelfSummonSkills":
            skillList = this.getSummonSelfSpells();
            break;
         case "farmHealSummonSkills":
            skillList = this.getSummonHealSpells();
      }

      if (skillList != null && !skillList.isEmpty()) {
         String line = "";

         for(int id : skillList) {
            line = line + id + ";";
         }

         this._player.setVar(type, line);
      }
   }

   public int getShortcutsIndex() {
      return this._shortcutsIndex;
   }

   public int getFarmRadius() {
      return this._radius;
   }

   @NotNull
   private List<Integer> getSpellsInSlots(List<Integer> slots) {
      return Arrays.stream(this._player.getAllShortCuts())
         .filter(shortcut -> shortcut.getPage() == this.getShortcutsIndex() && shortcut.getType() == ShortcutType.SKILL && slots.contains(shortcut.getSlot()))
         .map(ShortCutTemplate::getId)
         .collect(Collectors.toList());
   }

   public void refreshChanceSkills() {
      this._chanceSkills.clear();
      List<Integer> newSkills = this.getSpellsInSlots(this._chanceSlots);
      if (!newSkills.isEmpty()) {
         for(int skillId : newSkills) {
            Skill skill = this._player.getKnownSkill(skillId);
            if (skill != null
               && (
                  skill.getSkillType() == SkillType.DOT
                     || skill.getSkillType() == SkillType.MDOT
                     || skill.getSkillType() == SkillType.POISON
                     || skill.getSkillType() == SkillType.BLEED
                     || skill.getSkillType() == SkillType.DEBUFF
                     || skill.getSkillType() == SkillType.SLEEP
                     || skill.getSkillType() == SkillType.ROOT
                     || skill.getSkillType() == SkillType.PARALYZE
                     || skill.getSkillType() == SkillType.MUTE
                     || skill.isSpoilSkill()
                     || skill.isSweepSkill()
                     || skill.getId() == 1263
               )) {
               this._chanceSkills.add(skillId);
            }
         }

         this.saveSkills("farmChanceSkills");
         newSkills.clear();
      }
   }

   public List<Integer> getChanceSpells() {
      return this._chanceSkills;
   }

   public void refreshAttackSkills() {
      this._attackSkills.clear();
      List<Integer> newSkills = this.getSpellsInSlots(this._attackSlots);
      if (!newSkills.isEmpty()) {
         for(int skillId : newSkills) {
            Skill skill = this._player.getKnownSkill(skillId);
            if (skill != null
               && !skill.isSpoilSkill()
               && !skill.isSweepSkill()
               && skill.getId() != 1263
               && (
                  skill.getSkillType() == SkillType.AGGDAMAGE
                     || skill.getSkillType() == SkillType.BLOW
                     || skill.getSkillType() == SkillType.PDAM
                     || skill.getSkillType() == SkillType.MANADAM
                     || skill.getSkillType() == SkillType.MDAM
                     || skill.getSkillType() == SkillType.DRAIN
                     || skill.getSkillType() == SkillType.CHARGEDAM
                     || skill.getSkillType() == SkillType.FATAL
                     || skill.getSkillType() == SkillType.DEATHLINK
                     || skill.getSkillType() == SkillType.CPDAMPERCENT
                     || skill.getSkillType() == SkillType.STUN
                     || skill.hasEffectType(EffectType.STUN)
               )) {
               this._attackSkills.add(skillId);
            }
         }

         this.saveSkills("farmAttackSkills");
         newSkills.clear();
      }
   }

   public List<Integer> getAttackSpells() {
      return this._attackSkills;
   }

   public void refreshSelfSkills() {
      this._selfSkills.clear();
      List<Integer> newSkills = this.getSpellsInSlots(this._selfSlots);
      if (!newSkills.isEmpty()) {
         for(int skillId : newSkills) {
            Skill skill = this._player.getKnownSkill(skillId);
            if (skill != null
               && (
                  skill.isToggle()
                     || skill.isDance()
                     || skill.hasEffectType(EffectType.BUFF)
                     || skill.getSkillType() == SkillType.BUFF
                     || skill.hasEffectType(EffectType.SUMMON_CUBIC)
               )) {
               this._selfSkills.add(skillId);
            }
         }

         this.saveSkills("farmSelfSkills");
         newSkills.clear();
      }
   }

   public List<Integer> getSelfSpells() {
      return this._selfSkills;
   }

   public void refreshLowLifeSkills() {
      this._lowLifeSkills.clear();
      List<Integer> newSkills = this.getSpellsInSlots(this._lowLifeSlots);
      if (!newSkills.isEmpty()) {
         for(int skillId : newSkills) {
            Skill skill = this._player.getKnownSkill(skillId);
            if (skill != null
               && (
                  skill.getSkillType() == SkillType.DRAIN
                     || skill.hasEffectType(EffectType.HEAL)
                     || skill.hasEffectType(EffectType.HEAL_OVER_TIME)
                     || skill.hasEffectType(EffectType.HEAL_PERCENT)
                     || skill.hasEffectType(EffectType.MANAHEAL)
                     || skill.hasEffectType(EffectType.MANA_HEAL_OVER_TIME)
                     || skill.hasEffectType(EffectType.MANAHEAL_BY_LEVEL)
                     || skill.hasEffectType(EffectType.MANAHEAL_PERCENT)
               )) {
               this._lowLifeSkills.add(skillId);
            }
         }

         this.saveSkills("farmHealSkills");
         newSkills.clear();
      }
   }

   public List<Integer> getLowLifeSpells() {
      return this._lowLifeSkills;
   }

   public void checkAllSlots() {
      this.refreshChanceSkills();
      this.refreshAttackSkills();
      this.refreshSelfSkills();
      this.refreshLowLifeSkills();
   }

   public void startFarmTask() {
      if (!this.isAutofarming() && FarmSettings.ALLOW_AUTO_FARM && (!FarmSettings.AUTO_FARM_FOR_PREMIUM || this._player.hasPremiumBonus())) {
         int lastHwids = AutoFarmManager.getInstance()
            .getActiveFarms(FarmSettings.ALLOW_CHECK_HWID_LIMIT ? this._player.getHWID() : this._player.getIPAddress());
         if (lastHwids <= 0 && !AutoFarmManager.getInstance().isNonCheckPlayer(this._player.getObjectId())) {
            this._player.sendMessage("Exceeded limit on use of service!");
         } else {
            try {
               if (this._farmTask != null) {
                  this._farmTask.cancel(false);
                  this._farmTask = null;
               }
            } catch (Exception var8) {
            }

            AutoFarmManager.getInstance()
               .addActiveFarm(FarmSettings.ALLOW_CHECK_HWID_LIMIT ? this._player.getHWID() : this._player.getIPAddress(), this._player.getObjectId());
            if (this.isKeepLocation()) {
               this.setKeepLocation(this._player.getLocation());
            }

            int taskInterval = FarmSettings.FARM_INTERVAL_TASK;
            switch(this.getFarmType()) {
               case 0:
                  if (taskInterval <= 0) {
                     taskInterval = this._player.getPAtkSpd() > 1000.0 ? 500 : 1000;
                  }

                  this._farmTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new AutoPhysicalFarmTask(this._player), 1000L, (long)taskInterval);
                  break;
               case 1:
                  if (taskInterval <= 0) {
                     taskInterval = this._player.getPAtkSpd() > 1000.0 ? 500 : 1000;
                  }

                  this._farmTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new AutoArcherFarmTask(this._player), 1000L, (long)taskInterval);
                  break;
               case 2:
                  if (taskInterval <= 0) {
                     taskInterval = this._player.getMAtkSpd() > 1000.0 ? 500 : 1000;
                  }

                  this._farmTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new AutoMagicFarmTask(this._player), 1000L, (long)taskInterval);
                  break;
               case 3:
                  if (taskInterval <= 0) {
                     if (this._player.getPAtkSpd() > this._player.getMAtkSpd()) {
                        taskInterval = this._player.getPAtkSpd() > 1000.0 ? 500 : 1000;
                     } else {
                        taskInterval = this._player.getMAtkSpd() > 1000.0 ? 500 : 1000;
                     }
                  }

                  this._farmTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new AutoHealFarmTask(this._player), 1000L, (long)taskInterval);
                  break;
               case 4:
                  if (!this._player.hasSummon()) {
                     this._player.sendMessage("You have no summon! Autofarming deactivate!");
                     AutoFarmManager.getInstance()
                        .removeActiveFarm(
                           FarmSettings.ALLOW_CHECK_HWID_LIMIT ? this._player.getHWID() : this._player.getIPAddress(), this._player.getObjectId()
                        );
                     return;
                  }

                  if (taskInterval <= 0) {
                     if (this._player.getPAtkSpd() > this._player.getMAtkSpd()) {
                        taskInterval = this._player.getPAtkSpd() > 1000.0 ? 700 : 1000;
                     } else {
                        taskInterval = this._player.getMAtkSpd() > 1000.0 ? 700 : 1000;
                     }
                  }

                  this._farmTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new AutoSummonFarmTask(this._player), 1000L, (long)taskInterval);
            }

            boolean notCheckTime = FarmSettings.PREMIUM_FARM_FREE && this._player.hasPremiumBonus() || FarmSettings.AUTO_FARM_FREE;
            if (FarmSettings.FARM_ONLINE_TYPE && !notCheckTime) {
               long taskTime = this._player.getVarLong("activeFarmOnlineTask", 0L) - this.getLastFarmOnlineTime();

               try {
                  if (this._farmEndTask != null) {
                     this._farmEndTask.cancel(false);
                     this._farmEndTask = null;
                  }
               } catch (Exception var7) {
               }

               this._farmEndTask = ThreadPoolManager.getInstance().schedule(new AutoFarmEndTask(this._player), taskTime);
               this.setFarmOnlineTime();
            }

            this._player.sendMessage("Autofarming activated");
         }
      }
   }

   public void stopFarmTask(boolean isSwitch) {
      if (this.isAutofarming() && FarmSettings.ALLOW_AUTO_FARM) {
         try {
            if (this._farmTask != null) {
               this._farmTask.cancel(false);
               this._farmTask = null;
            }
         } catch (Exception var6) {
         }

         boolean notCheckTime = FarmSettings.PREMIUM_FARM_FREE && this._player.hasPremiumBonus() || FarmSettings.AUTO_FARM_FREE;
         if (FarmSettings.FARM_ONLINE_TYPE && !notCheckTime) {
            try {
               if (this._farmEndTask != null) {
                  this._farmEndTask.cancel(false);
                  this._farmEndTask = null;
               }
            } catch (Exception var5) {
            }

            long time = this.getLastFarmOnlineTime() + (System.currentTimeMillis() - this.getFarmOnlineTime());
            this._player.setVar("activeFarmOnlineTime", time);
            this._farmLastOnlineTime = time;
            this._farmOnlineTime = 0L;
         }

         AutoFarmManager.getInstance()
            .removeActiveFarm(FarmSettings.ALLOW_CHECK_HWID_LIMIT ? this._player.getHWID() : this._player.getIPAddress(), this._player.getObjectId());
         this._player.sendMessage("Autofarming deactivated");
         if (isSwitch) {
            this.startFarmTask();
         }
      }
   }

   public boolean isAutofarming() {
      return this._farmTask != null;
   }

   public void checkFarmTask() {
      if (FarmSettings.FARM_ONLINE_TYPE) {
         long timeEnd = this._player.getVarLong("activeFarmOnlineTask", 0L);
         if (this._player.getVarLong("activeFarmOnlineTime", 0L) <= timeEnd && timeEnd != 0L) {
            this._activeFarmOnlineTime = true;
            this._farmLastOnlineTime = this._player.getVarLong("activeFarmOnlineTime", 0L);
         } else {
            this._activeFarmOnlineTime = false;
         }
      } else {
         long timeEnd = this._player.getVarLong("activeFarmTask", 0L);
         if (timeEnd > System.currentTimeMillis()) {
            if (this._farmEndTask == null) {
               this._farmEndTask = ThreadPoolManager.getInstance().schedule(new AutoFarmEndTask(this._player), timeEnd - System.currentTimeMillis());
            }

            this._autoFarmEnd = timeEnd;
         } else {
            this._autoFarmEnd = 0L;
         }
      }
   }

   public void setAutoFarmEndTask(long value) {
      if (value == 0L && this._farmEndTask != null) {
         this._farmEndTask.cancel(false);
         this._farmEndTask = null;
      }

      this._autoFarmEnd = value;
   }

   public long getAutoFarmEnd() {
      return this._autoFarmEnd;
   }

   public boolean isActiveAutofarm() {
      return this._farmEndTask != null
         || FarmSettings.AUTO_FARM_FREE
         || FarmSettings.PREMIUM_FARM_FREE && this._player.hasPremiumBonus()
         || FarmSettings.FARM_ONLINE_TYPE && this.isActiveFarmOnlineTime();
   }

   public boolean isActiveFarmTask() {
      return this._farmEndTask != null;
   }

   public boolean isRndAttackSkills() {
      return this._rndAttackSkills;
   }

   public boolean isRndChanceSkills() {
      return this._rndChanceSkills;
   }

   public boolean isRndSelfSkills() {
      return this._rndSelfSkills;
   }

   public boolean isRndLifeSkills() {
      return this._rndLifeSkills;
   }

   public void setRndAttackSkills(boolean rnd, boolean store) {
      this._rndAttackSkills = rnd;
      if (!store) {
         this._player.setVar("farmRndAttackSkills", this._rndAttackSkills ? 1 : 0);
      }
   }

   public void setRndChanceSkills(boolean rnd, boolean store) {
      this._rndChanceSkills = rnd;
      if (!store) {
         this._player.setVar("farmRndChanceSkills", this._rndChanceSkills ? 1 : 0);
      }
   }

   public void setRndSelfSkills(boolean rnd, boolean store) {
      this._rndSelfSkills = rnd;
      if (!store) {
         this._player.setVar("farmRndSelfSkills", this._rndSelfSkills ? 1 : 0);
      }
   }

   public void setRndLifeSkills(boolean rnd, boolean store) {
      this._rndLifeSkills = rnd;
      if (!store) {
         this._player.setVar("farmRndLifeSkills", this._rndLifeSkills ? 1 : 0);
      }
   }

   public boolean isLeaderAssist() {
      return this._leaderAssist;
   }

   public boolean isKeepLocation() {
      return this._keepLocation;
   }

   public boolean isExtraDelaySkill() {
      return this._exDelaySkill;
   }

   public boolean isExtraSummonDelaySkill() {
      return this._exSummonDelaySkill;
   }

   public boolean isRunTargetCloseUp() {
      return this._runTargetCloseUp;
   }

   public boolean isUseSummonSkills() {
      return this._useSummonSkills;
   }

   public void setLeaderAssist(boolean rnd, boolean store) {
      if (this._player.getParty() != null && this._player.getParty().getLeader() == this._player) {
         this._leaderAssist = false;
      } else {
         this._leaderAssist = rnd;
      }

      if (!store) {
         this._player.setVar("farmLeaderAssist", this._leaderAssist ? 1 : 0);
      }
   }

   public void setKeepLocation(boolean rnd, boolean store) {
      this._keepLocation = rnd;
      if (!store) {
         this._player.setVar("farmKeepLocation", this._keepLocation ? 1 : 0);
         if (this._keepLocation) {
            this.setKeepLocation(this._player.getLocation());
         }
      }
   }

   public void setExDelaySkill(boolean rnd, boolean store) {
      this._exDelaySkill = rnd;
      if (!store) {
         this._player.setVar("farmExDelaySkill", this._exDelaySkill ? 1 : 0);
      }
   }

   public void setExSummonDelaySkill(boolean rnd, boolean store) {
      this._exSummonDelaySkill = rnd;
      if (!store) {
         this._player.setVar("farmExSummonDelaySkill", this._exSummonDelaySkill ? 1 : 0);
      }
   }

   public void setRunTargetCloseUp(boolean rnd, boolean store) {
      this._runTargetCloseUp = rnd;
      if (!store) {
         this._player.setVar("farmRunTargetCloseUp", this._runTargetCloseUp ? 1 : 0);
      }
   }

   public void setUseSummonSkills(boolean rnd, boolean store) {
      this._useSummonSkills = rnd;
      if (!store) {
         this._player.setVar("farmUseSummonSkills", this._useSummonSkills ? 1 : 0);
      }
   }

   public boolean isAssistMonsterAttack() {
      return this._isAssistMonsterAttack;
   }

   public boolean isTargetRestoreMp() {
      return this._isTargetRestoreMp;
   }

   public void setAssistMonsterAttack(boolean rnd, boolean store) {
      this._isAssistMonsterAttack = rnd;
      if (!store) {
         this._player.setVar("farmAssistMonsterAttack", this._isAssistMonsterAttack ? 1 : 0);
      }
   }

   public void setTargetRestoreMp(boolean rnd, boolean store) {
      this._isTargetRestoreMp = rnd;
      if (!store) {
         this._player.setVar("farmTargetRestoreMp", this._isTargetRestoreMp ? 1 : 0);
      }
   }

   public List<Integer> getSummonAttackSpells() {
      return this._summonAttackSkills;
   }

   public List<Integer> getSummonSelfSpells() {
      return this._summonSelfSkills;
   }

   public List<Integer> getSummonHealSpells() {
      return this._summonHealSkills;
   }

   public int getSummonAttackPercent() {
      return this._attackSummonSkillPercent;
   }

   public int getSummonAttackChance() {
      return this._attackSummonSkillChance;
   }

   public int getSummonSelfPercent() {
      return this._selfSummonSkillPercent;
   }

   public int getSummonSelfChance() {
      return this._selfSummonSkillChance;
   }

   public int getSummonLifePercent() {
      return this._lifeSummonSkillPercent;
   }

   public int getSummonLifeChance() {
      return this._lifeSummonSkillChance;
   }

   public void setSummonAttackSkillValue(boolean isPercent, int value) {
      if (isPercent) {
         this._attackSummonSkillPercent = value;
      } else {
         this._attackSummonSkillChance = value;
      }
   }

   public void setSummonSelfSkillValue(boolean isPercent, int value) {
      if (isPercent) {
         this._selfSummonSkillPercent = value;
      } else {
         this._selfSummonSkillChance = value;
      }
   }

   public void setSummonLifeSkillValue(boolean isPercent, int value) {
      if (isPercent) {
         this._lifeSummonSkillPercent = value;
      } else {
         this._lifeSummonSkillChance = value;
      }
   }

   public boolean isRndSummonAttackSkills() {
      return this._rndSummonAttackSkills;
   }

   public boolean isRndSummonSelfSkills() {
      return this._rndSummonSelfSkills;
   }

   public boolean isRndSummonLifeSkills() {
      return this._rndSummonLifeSkills;
   }

   public void setRndSummonAttackSkills(boolean rnd, boolean store) {
      this._rndSummonAttackSkills = rnd;
      if (!store) {
         this._player.setVar("farmRndSummonAttackSkills", this._rndSummonAttackSkills ? 1 : 0);
      }
   }

   public void setRndSummonSelfSkills(boolean rnd, boolean store) {
      this._rndSummonSelfSkills = rnd;
      if (!store) {
         this._player.setVar("farmRndSummonSelfSkills", this._rndSummonSelfSkills ? 1 : 0);
      }
   }

   public void setRndSummonLifeSkills(boolean rnd, boolean store) {
      this._rndSummonLifeSkills = rnd;
      if (!store) {
         this._player.setVar("farmRndSummonLifeSkills", this._rndSummonLifeSkills ? 1 : 0);
      }
   }

   public final List<Attackable> getAroundNpc(Player player, Function<Npc, Boolean> condition) {
      List<Attackable> result = new ArrayList<>();

      for(Npc npc : World.getInstance().getAroundNpc(player, this.getFarmRadius(), 600)) {
         if (npc.isMonster()
            && !npc.isDead()
            && npc.isVisible()
            && !(npc instanceof TreasureChestInstance)
            && !npc.isRaid()
            && !npc.isRaidMinion()
            && condition.apply(npc)
            && npc instanceof Attackable
            && npc.hasAI()) {
            if (npc.getAI().getTargetList().isEmpty() || npc.getAI().getTargetList().contains(player)) {
               result.add((Attackable)npc);
            } else if (player.getParty() != null) {
               for(Player pl : player.getParty().getMembers()) {
                  if (pl != null && npc.getAI().getTargetList().contains(pl)) {
                     result.add((Attackable)npc);
                  }
               }
            }
         }
      }

      return result;
   }

   public Skill nextAttackSkill(Attackable target, long extraDelay) {
      if (this.getAttackSpells().isEmpty() || !Rnd.chance(this.getAttackChance())) {
         return null;
      } else if (this.isExtraDelaySkill() && extraDelay > System.currentTimeMillis()) {
         return null;
      } else {
         double mpPercent = this._player.getCurrentMpPercents();
         if (mpPercent < (double)this.getAttackPercent()) {
            return null;
         } else if (this.isRndAttackSkills()) {
            return this.nextRndAttackSkill(target);
         } else {
            for(int skillId : this.getAttackSpells()) {
               Skill skill = this._player.getKnownSkill(skillId);
               if (skill != null
                  && this._player.checkDoCastConditions(skill, false)
                  && (!skill.isOffensive() || skill.getTargetType() != TargetType.ONE || target != null)) {
                  this._player.setTarget(target);
                  return skill;
               }
            }

            return null;
         }
      }
   }

   private Skill nextRndAttackSkill(Attackable target) {
      List<Skill> skillList = new ArrayList<>();
      Skill rndSkill = null;

      for(int skillId : this.getAttackSpells()) {
         Skill skill = this._player.getKnownSkill(skillId);
         if (skill != null
            && this._player.checkDoCastConditions(skill, false)
            && (!skill.isOffensive() || skill.getTargetType() != TargetType.ONE || target != null)) {
            skillList.add(skill);
         }
      }

      if (!skillList.isEmpty()) {
         rndSkill = skillList.get(Rnd.get(skillList.size()));
         this._player.setTarget(target);
      }

      skillList.clear();
      return rndSkill;
   }

   public Skill nextChanceSkill(Attackable target, long extraDelay) {
      if (this.getChanceSpells().isEmpty() || !Rnd.chance(this.getChanceChance())) {
         return null;
      } else if (this.isExtraDelaySkill() && extraDelay > System.currentTimeMillis()) {
         return null;
      } else {
         double mpPercent = this._player.getCurrentMpPercents();
         if (target != null && !(mpPercent < (double)this.getChancePercent())) {
            if (this.isRndChanceSkills()) {
               return this.nextRndChanceSkill(target);
            } else {
               for(int skillId : this.getChanceSpells()) {
                  Skill skill = this._player.getKnownSkill(skillId);
                  if (skill != null
                     && this._player.checkDoCastConditions(skill, false)
                     && (!skill.isSpoilSkill() || !target.isSpoil())
                     && (!skill.isSweepSkill() || target.isDead())
                     && target.getFirstEffect(skillId) == null) {
                     return skill;
                  }
               }

               return null;
            }
         } else {
            return null;
         }
      }
   }

   private Skill nextRndChanceSkill(Attackable target) {
      List<Skill> skillList = new ArrayList<>();
      Skill rndSkill = null;

      for(int skillId : this.getChanceSpells()) {
         Skill skill = this._player.getKnownSkill(skillId);
         if (skill != null
            && this._player.checkDoCastConditions(skill, false)
            && (!skill.isSpoilSkill() || !target.isSpoil())
            && (!skill.isSweepSkill() || target.isDead())
            && target.getFirstEffect(skillId) == null) {
            skillList.add(skill);
         }
      }

      if (!skillList.isEmpty()) {
         rndSkill = skillList.get(Rnd.get(skillList.size()));
      }

      skillList.clear();
      return rndSkill;
   }

   public Skill nextSelfSkill(Creature ownerTarget) {
      if (!this.getSelfSpells().isEmpty() && Rnd.chance(this.getSelfChance())) {
         double mpPercent = this._player.getCurrentMpPercents();
         if (mpPercent < (double)this.getSelfPercent()) {
            return null;
         } else if (this.isRndSelfSkills()) {
            return this.nextRndSelfSkill(ownerTarget);
         } else {
            for(int skillId : this.getSelfSpells()) {
               Skill skill = this._player.getKnownSkill(skillId);
               if (skill != null && this._player.checkDoCastConditions(skill, false)) {
                  if (skill.isToggle() && this._player.getFirstEffect(skillId) == null) {
                     return skill;
                  }

                  if (ownerTarget != null && ownerTarget.getFirstEffect(skillId) == null && skill.getTargetType() != TargetType.SELF) {
                     this._player.setTarget(ownerTarget);
                     return skill;
                  }

                  if (skill.hasEffectType(EffectType.SUMMON_CUBIC)) {
                     int cubicId = skill.getEffectTemplates()[0].getCubicId();
                     Effect cubicMastery = this._player.getFirstPassiveEffect(EffectType.CUBIC_MASTERY);
                     int cubicCount = (int)(cubicMastery != null ? cubicMastery.calc() - 1.0 : 0.0);
                     if (!this._player.getCubics().containsKey(cubicId) && this._player.getCubics().size() <= cubicCount) {
                        return skill;
                     }
                  } else if (this._player.getFirstEffect(skillId) == null && skill.getTargetType() != TargetType.SERVITOR) {
                     this._player.setTarget(this._player);
                     return skill;
                  }
               }
            }

            return null;
         }
      } else {
         return null;
      }
   }

   private Skill nextRndSelfSkill(Creature ownerTarget) {
      List<Skill> skillList = new ArrayList<>();
      List<Skill> skillOwnerList = new ArrayList<>();
      Skill rndSkill = null;

      for(int skillId : this.getSelfSpells()) {
         Skill skill = this._player.getKnownSkill(skillId);
         if (skill != null && this._player.checkDoCastConditions(skill, false)) {
            if (skill.isToggle() && this._player.getFirstEffect(skillId) == null) {
               skillList.add(skill);
            } else if (skill.hasEffectType(EffectType.SUMMON_CUBIC)) {
               int cubicId = skill.getEffectTemplates()[0].getCubicId();
               Effect cubicMastery = this._player.getFirstPassiveEffect(EffectType.CUBIC_MASTERY);
               int cubicCount = (int)(cubicMastery != null ? cubicMastery.calc() - 1.0 : 0.0);
               if (!this._player.getCubics().containsKey(cubicId) && this._player.getCubics().size() <= cubicCount) {
                  return skill;
               }
            } else {
               if (this._player.getFirstEffect(skillId) == null && skill.getTargetType() != TargetType.SERVITOR) {
                  skillList.add(skill);
               }

               if (ownerTarget != null && ownerTarget.getFirstEffect(skillId) == null && skill.getTargetType() != TargetType.SELF) {
                  skillOwnerList.add(skill);
               }
            }
         }
      }

      boolean isForSelf = true;
      if (!skillOwnerList.isEmpty()) {
         rndSkill = skillOwnerList.get(Rnd.get(skillOwnerList.size()));
         isForSelf = false;
      } else if (!skillList.isEmpty()) {
         rndSkill = skillList.get(Rnd.get(skillList.size()));
      }

      skillList.clear();
      skillOwnerList.clear();
      if (rndSkill == null) {
         return null;
      } else {
         if (ownerTarget != null && !isForSelf) {
            this._player.setTarget(ownerTarget);
         } else {
            this._player.setTarget(this._player);
         }

         return rndSkill;
      }
   }

   public Skill nextHealSkill(Attackable target, Creature ownerTarget) {
      if (!this.getLowLifeSpells().isEmpty() && Rnd.chance(this.getLifeChance())) {
         double hpPercent = this._player.getCurrentHpPercents();
         double ownerHpPercent = ownerTarget != null ? ownerTarget.getCurrentHpPercents() : 100.0;
         double ownerMpPercent = ownerTarget != null ? ownerTarget.getCurrentMpPercents() : 100.0;
         boolean ownerHeal = ownerHpPercent < (double)this.getLifePercent();
         boolean selfHeal = hpPercent < (double)this.getLifePercent();
         boolean ownerMp = ownerMpPercent < (double)this.getLifePercent();
         if (!ownerHeal && !selfHeal && !ownerMp) {
            return null;
         } else if (this.isRndLifeSkills()) {
            return this.nextRndHealSkill(target, ownerTarget);
         } else {
            for(int skillId : this.getLowLifeSpells()) {
               Skill skill = this._player.getKnownSkill(skillId);
               if (skill != null && this._player.checkDoCastConditions(skill, false) && (!skill.isOffensive() || target != null)) {
                  if (!isHeal(skill)) {
                     if (isManaHeal(skill)
                        && this.isTargetRestoreMp()
                        && ownerTarget != null
                        && ownerMp
                        && !ownerTarget.isDead()
                        && skill.getTargetType() != TargetType.SELF) {
                        this._player.setTarget(ownerTarget);
                        return skill;
                     }

                     return skill;
                  }

                  if (ownerHeal || selfHeal) {
                     if (ownerHeal && ownerTarget != null && !ownerTarget.isDead() && skill.getTargetType() != TargetType.SELF) {
                        if (skill.getTargetType() != TargetType.SERVITOR || ownerTarget.isSummon()) {
                           this._player.setTarget(ownerTarget);
                           return skill;
                        }
                     } else {
                        if (!selfHeal) {
                           return null;
                        }

                        if (skill.getTargetType() != TargetType.SERVITOR) {
                           this._player.setTarget(this._player);
                           return skill;
                        }
                     }
                  }
               }
            }

            return null;
         }
      } else {
         return null;
      }
   }

   private Skill nextRndHealSkill(Attackable target, Creature ownerTarget) {
      List<Skill> skillList = new ArrayList<>();
      Skill rndSkill = null;
      double hpPercent = this._player.getCurrentHpPercents();
      double ownerHpPercent = ownerTarget != null ? ownerTarget.getCurrentHpPercents() : 100.0;
      double ownerMpPercent = ownerTarget != null ? ownerTarget.getCurrentMpPercents() : 100.0;
      boolean ownerHeal = ownerHpPercent < (double)this.getLifePercent();
      boolean selfHeal = hpPercent < (double)this.getLifePercent();
      boolean ownerMp = ownerMpPercent < (double)this.getLifePercent();
      if (!ownerHeal && !selfHeal && !ownerMp) {
         return null;
      } else {
         for(int skillId : this.getLowLifeSpells()) {
            Skill skill = this._player.getKnownSkill(skillId);
            if (skill != null && this._player.checkDoCastConditions(skill, false) && (!skill.isOffensive() || target != null)) {
               if (!ownerHeal && !selfHeal) {
                  if (ownerMp
                     && isManaHeal(skill)
                     && this.isTargetRestoreMp()
                     && ownerTarget != null
                     && !ownerTarget.isDead()
                     && skill.getTargetType() != TargetType.SELF) {
                     skillList.add(skill);
                  }
               } else if (isHeal(skill)) {
                  if (ownerHeal) {
                     if (ownerTarget != null
                        && !ownerTarget.isDead()
                        && skill.getTargetType() != TargetType.SELF
                        && (skill.getTargetType() != TargetType.SERVITOR || ownerTarget.isSummon())) {
                        skillList.add(skill);
                     }
                  } else if (selfHeal && skill.getTargetType() != TargetType.SERVITOR) {
                     skillList.add(skill);
                  }
               }
            }
         }

         if (!skillList.isEmpty()) {
            rndSkill = skillList.get(Rnd.get(skillList.size()));
         }

         skillList.clear();
         if (rndSkill == null) {
            return null;
         } else {
            if (!ownerHeal && !ownerMp) {
               this._player.setTarget(this._player);
            } else {
               this._player.setTarget(ownerTarget);
            }

            return rndSkill;
         }
      }
   }

   public void tryUseMagic(Skill skill, boolean forceOnSelf) {
      if (forceOnSelf) {
         GameObject oldTarget = this._player.getTarget();
         this._player.setTarget(this._player);
         this._player.useMagic(skill, false, false, false);
         this._player.setTarget(oldTarget);
      } else {
         this._player.useMagic(skill, false, false, false);
      }
   }

   public void setKeepLocation(Location loc) {
      this.keepLocation = loc;
   }

   public Location getKeepLocation() {
      return this.keepLocation;
   }

   public boolean isNeedToReturn() {
      if (this.isKeepLocation() && this.getKeepLocation() != null) {
         if (Math.sqrt(this._player.getDistanceSq(this.keepLocation.getX(), this.keepLocation.getY(), this.keepLocation.getZ())) > (double)this._radius) {
            this._player.getAI().setIntention(CtrlIntention.MOVING, this.getKeepLocation());
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static boolean isManaHeal(Skill skill) {
      return skill.hasEffectType(EffectType.MANAHEAL)
         || skill.hasEffectType(EffectType.MANA_HEAL_OVER_TIME)
         || skill.hasEffectType(EffectType.MANAHEAL_BY_LEVEL)
         || skill.hasEffectType(EffectType.MANAHEAL_PERCENT);
   }

   private static boolean isHeal(Skill skill) {
      return skill.hasEffectType(EffectType.HEAL) || skill.hasEffectType(EffectType.HEAL_OVER_TIME) || skill.hasEffectType(EffectType.HEAL_PERCENT);
   }

   public Skill nextSummonAttackSkill(Attackable target, Summon summon, long extraDelay) {
      if (this.getSummonAttackSpells().isEmpty() || !Rnd.chance(this.getSummonAttackChance())) {
         return null;
      } else if (this.isExtraSummonDelaySkill() && extraDelay > System.currentTimeMillis()) {
         return null;
      } else {
         double mpPercent = summon.getCurrentMpPercents();
         if (mpPercent < (double)this.getSummonAttackPercent()) {
            return null;
         } else if (this.isRndSummonAttackSkills()) {
            return this.nextSummonRndAttackSkill(target, summon);
         } else {
            for(int skillId : this.getSummonAttackSpells()) {
               int lvl = 0;
               if (summon.isPet()) {
                  lvl = PetsParser.getInstance().getPetData(summon.getId()).getAvailableLevel(skillId, summon.getLevel());
               } else {
                  lvl = SummonSkillsHolder.getInstance().getAvailableLevel(this._player, summon, skillId);
               }

               if (lvl > 0) {
                  Skill skill = SkillsParser.getInstance().getInfo(skillId, lvl);
                  if (summon.checkDoCastConditions(skill, false) && (!skill.isOffensive() || skill.getTargetType() != TargetType.ONE || target != null)) {
                     return skill;
                  }
               }
            }

            return null;
         }
      }
   }

   private Skill nextSummonRndAttackSkill(Attackable target, Summon summon) {
      List<Skill> skillList = new ArrayList<>();
      Skill rndSkill = null;

      for(int skillId : this.getSummonAttackSpells()) {
         int lvl = 0;
         if (summon.isPet()) {
            lvl = PetsParser.getInstance().getPetData(summon.getId()).getAvailableLevel(skillId, summon.getLevel());
         } else {
            lvl = SummonSkillsHolder.getInstance().getAvailableLevel(this._player, summon, skillId);
         }

         if (lvl > 0) {
            Skill skill = SkillsParser.getInstance().getInfo(skillId, lvl);
            if (summon.checkDoCastConditions(skill, false) && (!skill.isOffensive() || skill.getTargetType() != TargetType.ONE || target != null)) {
               skillList.add(skill);
            }
         }
      }

      if (!skillList.isEmpty()) {
         rndSkill = skillList.get(Rnd.get(skillList.size()));
      }

      skillList.clear();
      return rndSkill;
   }

   public Skill nextSummonSelfSkill(Summon summon, Creature ownerTarget) {
      if (!this.getSummonSelfSpells().isEmpty() && Rnd.chance(this.getSummonSelfChance())) {
         double mpPercent = summon.getCurrentMpPercents();
         if (mpPercent < (double)this.getSummonSelfPercent()) {
            return null;
         } else if (this.isRndSummonSelfSkills()) {
            return this.nextSummonRndSelfSkill(summon, ownerTarget);
         } else {
            for(int skillId : this.getSummonSelfSpells()) {
               int lvl = 0;
               if (summon.isPet()) {
                  lvl = PetsParser.getInstance().getPetData(summon.getId()).getAvailableLevel(skillId, summon.getLevel());
               } else {
                  lvl = SummonSkillsHolder.getInstance().getAvailableLevel(this._player, summon, skillId);
               }

               if (lvl > 0) {
                  Skill skill = SkillsParser.getInstance().getInfo(skillId, lvl);
                  if (summon.checkDoCastConditions(skill, false)) {
                     if (skill.isToggle() && summon.getFirstEffect(skillId) == null) {
                        return skill;
                     }

                     if (ownerTarget != null
                        && ownerTarget.getFirstEffect(skillId) == null
                        && skill.getTargetType() != TargetType.SELF
                        && skill.getTargetType() != TargetType.SERVITOR) {
                        summon.setTarget(ownerTarget);
                        return skill;
                     }

                     if (summon.getFirstEffect(skillId) == null) {
                        return skill;
                     }
                  }
               }
            }

            return null;
         }
      } else {
         return null;
      }
   }

   private Skill nextSummonRndSelfSkill(Summon summon, Creature ownerTarget) {
      List<Skill> skillList = new ArrayList<>();
      List<Skill> skillOwnerList = new ArrayList<>();
      Skill rndSkill = null;

      for(int skillId : this.getSelfSpells()) {
         int lvl = 0;
         if (summon.isPet()) {
            lvl = PetsParser.getInstance().getPetData(summon.getId()).getAvailableLevel(skillId, summon.getLevel());
         } else {
            lvl = SummonSkillsHolder.getInstance().getAvailableLevel(this._player, summon, skillId);
         }

         if (lvl > 0) {
            Skill skill = SkillsParser.getInstance().getInfo(skillId, lvl);
            if (summon.checkDoCastConditions(skill, false)) {
               if (skill.isToggle() && summon.getFirstEffect(skillId) == null) {
                  skillList.add(skill);
               } else {
                  if (ownerTarget != null
                     && ownerTarget.getFirstEffect(skillId) == null
                     && skill.getTargetType() != TargetType.SELF
                     && skill.getTargetType() != TargetType.SERVITOR) {
                     skillOwnerList.add(skill);
                  }

                  if (summon.getFirstEffect(skillId) == null) {
                     skillList.add(skill);
                  }
               }
            }
         }
      }

      boolean isForSelf = true;
      if (!skillOwnerList.isEmpty()) {
         rndSkill = skillOwnerList.get(Rnd.get(skillOwnerList.size()));
         isForSelf = false;
      } else if (!skillList.isEmpty()) {
         rndSkill = skillList.get(Rnd.get(skillList.size()));
      }

      skillList.clear();
      skillOwnerList.clear();
      if (rndSkill == null) {
         return null;
      } else {
         if (ownerTarget != null && !isForSelf) {
            summon.setTarget(ownerTarget);
         } else {
            summon.setTarget(summon);
         }

         return rndSkill;
      }
   }

   public Skill nextSummonHealSkill(Attackable target, Summon summon, Creature ownerTarget) {
      if (!this.getSummonHealSpells().isEmpty() && Rnd.chance(this.getSummonLifeChance())) {
         double hpPercent = summon.getCurrentHpPercents();
         double ownerHpPercent = ownerTarget != null ? ownerTarget.getCurrentHpPercents() : 100.0;
         double ownerMpPercent = ownerTarget != null ? ownerTarget.getCurrentMpPercents() : 100.0;
         boolean ownerHeal = ownerHpPercent < (double)this.getSummonLifePercent();
         boolean selfHeal = hpPercent < (double)this.getSummonLifePercent();
         boolean ownerMp = ownerMpPercent < (double)this.getSummonLifePercent();
         if (!ownerHeal && !selfHeal && !ownerMp) {
            return null;
         } else if (this.isRndLifeSkills()) {
            return this.nextSummonRndHealSkill(target, summon, ownerTarget);
         } else {
            for(int skillId : this.getSummonHealSpells()) {
               int lvl = 0;
               if (summon.isPet()) {
                  lvl = PetsParser.getInstance().getPetData(summon.getId()).getAvailableLevel(skillId, summon.getLevel());
               } else {
                  lvl = SummonSkillsHolder.getInstance().getAvailableLevel(this._player, summon, skillId);
               }

               if (lvl > 0) {
                  Skill skill = SkillsParser.getInstance().getInfo(skillId, lvl);
                  if (summon.checkDoCastConditions(skill, false) && (!skill.isOffensive() || target != null)) {
                     if (!isHeal(skill)) {
                        if (isManaHeal(skill) && ownerTarget != null && ownerMp && !ownerTarget.isDead() && skill.getTargetType() != TargetType.SELF) {
                           summon.setTarget(ownerTarget);
                           return skill;
                        }

                        return skill;
                     }

                     if (ownerHeal || selfHeal) {
                        if (ownerHeal && ownerTarget != null && !ownerTarget.isDead() && skill.getTargetType() != TargetType.SELF) {
                           if (skill.getTargetType() == TargetType.SERVITOR && !ownerTarget.isSummon()) {
                              continue;
                           }

                           summon.setTarget(ownerTarget);
                           return skill;
                        }

                        if (selfHeal) {
                           summon.setTarget(summon);
                           return skill;
                        }

                        return null;
                     }
                  }
               }
            }

            return null;
         }
      } else {
         return null;
      }
   }

   private Skill nextSummonRndHealSkill(Attackable target, Summon summon, Creature ownerTarget) {
      List<Skill> skillList = new ArrayList<>();
      Skill rndSkill = null;
      double hpPercent = summon.getCurrentHpPercents();
      double ownerHpPercent = ownerTarget != null ? ownerTarget.getCurrentHpPercents() : 100.0;
      double ownerMpPercent = ownerTarget != null ? ownerTarget.getCurrentMpPercents() : 100.0;
      boolean ownerHeal = ownerHpPercent < (double)this.getSummonLifePercent();
      boolean selfHeal = hpPercent < (double)this.getSummonLifePercent();
      boolean ownerMp = ownerMpPercent < (double)this.getSummonLifePercent();
      if (!ownerHeal && !selfHeal && !ownerMp) {
         return null;
      } else {
         for(int skillId : this.getSummonHealSpells()) {
            int lvl = 0;
            if (summon.isPet()) {
               lvl = PetsParser.getInstance().getPetData(summon.getId()).getAvailableLevel(skillId, summon.getLevel());
            } else {
               lvl = SummonSkillsHolder.getInstance().getAvailableLevel(this._player, summon, skillId);
            }

            if (lvl > 0) {
               Skill skill = SkillsParser.getInstance().getInfo(skillId, lvl);
               if (summon.checkDoCastConditions(skill, false) && (!skill.isOffensive() || target != null)) {
                  if (!ownerHeal && !selfHeal) {
                     if (ownerMp && isManaHeal(skill) && ownerTarget != null && !ownerTarget.isDead() && skill.getTargetType() != TargetType.SELF) {
                        skillList.add(skill);
                     }
                  } else if (isHeal(skill)) {
                     if (ownerHeal) {
                        if (ownerTarget != null
                           && !ownerTarget.isDead()
                           && skill.getTargetType() != TargetType.SELF
                           && (skill.getTargetType() != TargetType.SERVITOR || ownerTarget.isSummon())) {
                           skillList.add(skill);
                        }
                     } else if (selfHeal) {
                        skillList.add(skill);
                     }
                  }
               }
            }
         }

         if (!skillList.isEmpty()) {
            rndSkill = skillList.get(Rnd.get(skillList.size()));
         }

         skillList.clear();
         if (rndSkill == null) {
            return null;
         } else {
            if (!ownerHeal && !ownerMp) {
               summon.setTarget(summon);
            } else {
               summon.setTarget(ownerTarget);
            }

            return rndSkill;
         }
      }
   }

   public Attackable getLeaderTarget(Player leader) {
      GameObject target = leader.getTarget();
      return target != null
            && target != leader
            && target instanceof Attackable
            && ((Attackable)target).hasAI()
            && ((Attackable)target).getAI().getTargetList().contains(leader)
         ? (Attackable)target
         : null;
   }

   public long getLastFarmOnlineTime() {
      return this._farmLastOnlineTime;
   }

   public boolean isActiveFarmOnlineTime() {
      return this._activeFarmOnlineTime;
   }

   public void setFarmOnlineTime() {
      this._farmOnlineTime = System.currentTimeMillis();
   }

   public void refreshFarmOnlineTime() {
      this._farmOnlineTime = 0L;
   }

   public long getFarmOnlineTime() {
      return this._farmOnlineTime;
   }

   public static enum SpellType {
      ATTACK,
      CHANCE,
      SELF,
      LOWLIFE;
   }
}
