package l2e.gameserver.model.zone.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import l2e.commons.util.Rnd;
import l2e.commons.util.StringUtil;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.zone.AbstractZoneSettings;
import l2e.gameserver.model.zone.TaskZoneSettings;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.ZoneType;

public class ReflectionZone extends ZoneType {
   private int _chance;
   private int _initialDelay;
   private int _reuse;
   private boolean _enabled = true;
   private Map<Integer, Integer> _skills;
   private final List<Integer> _reflections = new ArrayList<>();

   public ReflectionZone(int id) {
      super(id);
      AbstractZoneSettings settings = ZoneManager.getSettings(this.getName());
      if (settings == null) {
         settings = new TaskZoneSettings();
      }

      this.setSettings(settings);
      this.addZoneId(ZoneId.REFLECTION);
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
                  .setTask(ThreadPoolManager.getInstance().scheduleAtFixedRate(new ReflectionZone.ApplySkill(), (long)this._initialDelay, (long)this._reuse));
            }
         }
      }
   }

   @Override
   protected void onExit(Creature character) {
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

   public void removeRef(int id) {
      if (this._reflections.contains(id)) {
         this._reflections.remove(this._reflections.indexOf(id));
      }
   }

   public void addRef(int id) {
      if (!this._reflections.contains(id)) {
         this._reflections.add(id);
      }
   }

   private final class ApplySkill implements Runnable {
      protected ApplySkill() {
         if (ReflectionZone.this._skills == null) {
            throw new IllegalStateException("No skills defined.");
         }
      }

      @Override
      public void run() {
         if (ReflectionZone.this.isEnabled() && !ReflectionZone.this._reflections.isEmpty()) {
            for(Creature temp : ReflectionZone.this.getCharactersInside()) {
               if (temp != null
                  && !temp.isDead()
                  && ReflectionZone.this._reflections.contains(temp.getReflectionId())
                  && Rnd.chance(ReflectionZone.this.getChance())) {
                  for(Entry<Integer, Integer> e : ReflectionZone.this._skills.entrySet()) {
                     Skill skill = ReflectionZone.this.getSkill(e.getKey(), e.getValue());
                     if (skill != null && skill.checkCondition(temp, temp, false, true) && temp.getFirstEffect(e.getKey()) == null) {
                        skill.getEffects(temp, temp, false);
                     }
                  }
               }
            }
         }
      }
   }
}
