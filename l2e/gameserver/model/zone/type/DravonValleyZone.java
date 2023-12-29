package l2e.gameserver.model.zone.type;

import java.util.HashMap;
import java.util.Map;
import l2e.commons.util.Rnd;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.zone.AbstractZoneSettings;
import l2e.gameserver.model.zone.TaskZoneSettings;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.ZoneType;

public class DravonValleyZone extends ZoneType {
   private static final Map<ClassId, Double> weight = new HashMap<>();
   private int _chance;
   private int _initialDelay;
   private int _reuse;
   private boolean _enabled = true;

   public DravonValleyZone(int id) {
      super(id);
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
      } else {
         super.setParameter(name, value);
      }
   }

   @Override
   protected void onEnter(Creature character) {
      if (character.isPlayer() && this.getSettings().getTask() == null) {
         synchronized(this) {
            if (this.getSettings().getTask() == null) {
               this.getSettings()
                  .setTask(ThreadPoolManager.getInstance().scheduleAtFixedRate(new DravonValleyZone.BuffTask(), (long)this._initialDelay, (long)this._reuse));
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

   protected int getBuffLevel(Creature character) {
      if (character != null && character.getParty() != null) {
         Party party = character.getParty();
         if (party.getMemberCount() < 4) {
            return 0;
         } else {
            for(Player p : party.getMembers()) {
               if (p.getLevel() < 80 || p.getClassId().level() != 3) {
                  return 0;
               }
            }

            double points = 0.0;
            int count = party.getMemberCount();

            for(Player p : party.getMembers()) {
               points += weight.get(p.getClassId());
            }

            return (int)Math.max(0L, Math.min(3L, Math.round(points * this.getCoefficient(count))));
         }
      } else {
         return 0;
      }
   }

   private double getCoefficient(int count) {
      double cf;
      switch(count) {
         case 1:
            cf = 0.7;
            break;
         case 2:
         case 3:
         default:
            cf = 1.0;
            break;
         case 4:
            cf = 0.7;
            break;
         case 5:
            cf = 0.75;
            break;
         case 6:
            cf = 0.8;
            break;
         case 7:
            cf = 0.85;
            break;
         case 8:
            cf = 0.9;
            break;
         case 9:
            cf = 0.95;
      }

      return cf;
   }

   public boolean isEnabled() {
      return this._enabled;
   }

   public int getChance() {
      return this._chance;
   }

   static {
      weight.put(ClassId.duelist, 0.2);
      weight.put(ClassId.dreadnought, 0.7);
      weight.put(ClassId.phoenixKnight, 0.5);
      weight.put(ClassId.hellKnight, 0.5);
      weight.put(ClassId.sagittarius, 0.3);
      weight.put(ClassId.adventurer, 0.4);
      weight.put(ClassId.archmage, 0.3);
      weight.put(ClassId.soultaker, 0.3);
      weight.put(ClassId.arcanaLord, 1.0);
      weight.put(ClassId.cardinal, -0.6);
      weight.put(ClassId.hierophant, 0.0);
      weight.put(ClassId.evaTemplar, 0.8);
      weight.put(ClassId.swordMuse, 0.5);
      weight.put(ClassId.windRider, 0.4);
      weight.put(ClassId.moonlightSentinel, 0.3);
      weight.put(ClassId.mysticMuse, 0.3);
      weight.put(ClassId.elementalMaster, 1.0);
      weight.put(ClassId.evaSaint, -0.6);
      weight.put(ClassId.shillienTemplar, 0.8);
      weight.put(ClassId.spectralDancer, 0.5);
      weight.put(ClassId.ghostHunter, 0.4);
      weight.put(ClassId.ghostSentinel, 0.3);
      weight.put(ClassId.stormScreamer, 0.3);
      weight.put(ClassId.spectralMaster, 1.0);
      weight.put(ClassId.shillienSaint, -0.6);
      weight.put(ClassId.titan, 0.3);
      weight.put(ClassId.dominator, 0.1);
      weight.put(ClassId.grandKhavatari, 0.2);
      weight.put(ClassId.doomcryer, 0.1);
      weight.put(ClassId.fortuneSeeker, 0.9);
      weight.put(ClassId.maestro, 0.7);
      weight.put(ClassId.doombringer, 0.2);
      weight.put(ClassId.trickster, 0.5);
      weight.put(ClassId.judicator, 0.1);
      weight.put(ClassId.maleSoulhound, 0.3);
      weight.put(ClassId.femaleSoulhound, 0.3);
   }

   protected final class BuffTask implements Runnable {
      @Override
      public void run() {
         if (DravonValleyZone.this.isEnabled()) {
            for(Creature player : DravonValleyZone.this.getCharactersInside()) {
               if (player != null && !player.isDead() && DravonValleyZone.this.getBuffLevel(player) > 0 && Rnd.get(100) < DravonValleyZone.this.getChance()) {
                  Skill skill = DravonValleyZone.this.getSkill(6885, DravonValleyZone.this.getBuffLevel(player));
                  if (skill != null && player.getFirstEffect(6885) == null) {
                     skill.getEffects(player, player, false);
                  }
               }
            }
         }
      }
   }
}
