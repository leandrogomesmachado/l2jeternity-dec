package l2e.gameserver.model.zone.type;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import l2e.commons.util.Rnd;
import l2e.commons.util.StringUtil;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.zone.AbstractZoneSettings;
import l2e.gameserver.model.zone.TaskZoneSettings;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.ZoneType;
import l2e.gameserver.network.serverpackets.EtcStatusUpdate;

public class EffectZone extends ZoneType {
   private int _chance = 100;
   private int _initialDelay = 0;
   private int _reuse = 30000;
   private boolean _enabled = true;
   protected boolean _bypassConditions;
   private boolean _isShowDangerIcon;
   protected Map<Integer, Integer> _skills;

   public EffectZone(int id) {
      super(id);
      this.setTargetType(GameObject.InstanceType.Playable);
      this._bypassConditions = false;
      this._isShowDangerIcon = true;
      AbstractZoneSettings settings = ZoneManager.getSettings(this.getName());
      if (settings == null) {
         settings = new TaskZoneSettings();
      }

      this.setSettings(settings);
      this.addZoneId(ZoneId.ALTERED);
   }

   public TaskZoneSettings getSettings() {
      return (TaskZoneSettings)super.getSettings();
   }

   @Override
   public void setParameter(String name, String value) {
      if (name.equals("chance")) {
         this._chance = Integer.parseInt(value);
      } else if (name.equals("initialDelay")) {
         this._initialDelay = Integer.parseInt(value);
      } else if (name.equals("default_enabled")) {
         this._enabled = Boolean.parseBoolean(value);
      } else if (name.equals("reuse")) {
         this._reuse = Integer.parseInt(value);
      } else if (name.equals("bypassSkillConditions")) {
         this._bypassConditions = Boolean.parseBoolean(value);
      } else if (name.equals("maxDynamicSkillCount")) {
         this._skills = new ConcurrentHashMap<>(Integer.parseInt(value));
      } else if (name.equals("skillIdLvl")) {
         String[] propertySplit = value.split(";");
         this._skills = new ConcurrentHashMap<>(propertySplit.length);

         for(String skill : propertySplit) {
            String[] skillSplit = skill.split("-");
            if (skillSplit.length != 2) {
               _log.warning(StringUtil.concat(this.getClass().getSimpleName() + ": invalid config property -> skillsIdLvl \"", skill, "\""));
            } else {
               try {
                  this._skills.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
               } catch (NumberFormatException var10) {
                  if (!skill.isEmpty()) {
                     _log.warning(
                        StringUtil.concat(this.getClass().getSimpleName() + ": invalid config property -> skillsIdLvl \"", skillSplit[0], "\"", skillSplit[1])
                     );
                  }
               }
            }
         }
      } else if (name.equals("showDangerIcon")) {
         this._isShowDangerIcon = Boolean.parseBoolean(value);
         if (this._isShowDangerIcon) {
            this.addZoneId(ZoneId.DANGER_AREA);
         }
      } else {
         super.setParameter(name, value);
      }
   }

   @Override
   protected void onEnter(Creature character) {
      if (this._skills != null && this.getSettings().getTask() == null) {
         synchronized(this) {
            if (this.getSettings().getTask() == null) {
               this.getSettings()
                  .setTask(ThreadPoolManager.getInstance().scheduleAtFixedRate(new EffectZone.ApplySkill(), (long)this._initialDelay, (long)this._reuse));
            }
         }
      }

      if (character.isPlayer() && this._isShowDangerIcon) {
         character.sendPacket(new EtcStatusUpdate(character.getActingPlayer()));
      }
   }

   @Override
   protected void onExit(Creature character) {
      if (character.isPlayer() && this._isShowDangerIcon && !character.isInsideZone(ZoneId.DANGER_AREA, this)) {
         character.sendPacket(new EtcStatusUpdate(character.getActingPlayer()));
      }

      if (this._characterList.isEmpty() && this.getSettings().getTask() != null) {
         this.getSettings().clear();
      }
   }

   protected Skill getSkill(int skillId, int skillLvl) {
      return SkillsParser.getInstance().getInfo(skillId, skillLvl);
   }

   public boolean isEnabled() {
      return this._enabled;
   }

   public int getChance() {
      return this._chance;
   }

   public void addSkill(int skillId, int skillLvL) {
      if (skillLvL < 1) {
         this.removeSkill(skillId);
      } else {
         if (this._skills == null) {
            synchronized(this) {
               if (this._skills == null) {
                  this._skills = new ConcurrentHashMap<>(3);
               }
            }
         }

         this._skills.put(skillId, skillLvL);
      }
   }

   public void removeSkill(int skillId) {
      if (this._skills != null) {
         this._skills.remove(skillId);
      }
   }

   public void clearSkills() {
      if (this._skills != null) {
         this._skills.clear();
      }
   }

   public void setZoneEnabled(boolean val) {
      this._enabled = val;
   }

   public int getSkillLevel(int skillId) {
      return this._skills != null && this._skills.containsKey(skillId) ? this._skills.get(skillId) : 0;
   }

   private final class ApplySkill implements Runnable {
      protected ApplySkill() {
         if (EffectZone.this._skills == null) {
            throw new IllegalStateException("No skills defined.");
         }
      }

      @Override
      public void run() {
         if (EffectZone.this.isEnabled()) {
            for(Creature temp : EffectZone.this.getCharactersInside()) {
               if (temp != null && !temp.isDead() && Rnd.get(100) < EffectZone.this.getChance()) {
                  for(Entry<Integer, Integer> e : EffectZone.this._skills.entrySet()) {
                     Skill skill = EffectZone.this.getSkill(e.getKey(), e.getValue());
                     if (skill != null
                        && (EffectZone.this._bypassConditions || skill.checkCondition(temp, temp, false, true))
                        && temp.getFirstEffect(e.getKey()) == null) {
                        skill.getEffects(temp, temp, false);
                     }
                  }
               }
            }
         }
      }
   }
}
