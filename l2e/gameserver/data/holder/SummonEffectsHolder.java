package l2e.gameserver.data.holder;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.actor.instance.PetInstance;
import l2e.gameserver.model.actor.instance.ServitorInstance;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.stats.Env;

public class SummonEffectsHolder {
   private final Map<Integer, Map<Integer, Map<Integer, List<SummonEffectsHolder.SummonEffect>>>> _servitorEffects = new HashMap<>();
   private final Map<Integer, List<SummonEffectsHolder.SummonEffect>> _petEffects = new ConcurrentHashMap<>();

   private Map<Integer, List<SummonEffectsHolder.SummonEffect>> getServitorEffects(Player owner) {
      Map<Integer, Map<Integer, List<SummonEffectsHolder.SummonEffect>>> servitorMap = this._servitorEffects.get(owner.getObjectId());
      return servitorMap == null ? null : servitorMap.get(owner.getClassIndex());
   }

   private List<SummonEffectsHolder.SummonEffect> getServitorEffects(Player owner, int referenceSkill) {
      return this.containsOwner(owner) ? this.getServitorEffects(owner).get(referenceSkill) : null;
   }

   private boolean containsOwner(Player owner) {
      return this._servitorEffects.getOrDefault(owner.getObjectId(), Collections.emptyMap()).containsKey(owner.getClassIndex());
   }

   private void removeEffects(List<SummonEffectsHolder.SummonEffect> effects, int skillId) {
      if (effects != null && !effects.isEmpty()) {
         for(SummonEffectsHolder.SummonEffect effect : effects) {
            Skill skill = effect.getSkill();
            if (skill != null && skill.getId() == skillId) {
               effects.remove(effect);
            }
         }
      }
   }

   private void applyEffects(Summon summon, List<SummonEffectsHolder.SummonEffect> summonEffects) {
      if (summonEffects != null) {
         for(SummonEffectsHolder.SummonEffect se : summonEffects) {
            if (se != null && se.getSkill().hasEffects()) {
               Env env = new Env();
               env.setCharacter(summon);
               env.setTarget(summon);
               env.setSkill(se.getSkill());

               for(EffectTemplate et : se.getSkill().getEffectTemplates()) {
                  Effect ef = et.getEffect(env);
                  if (ef != null) {
                     switch(ef.getEffectType()) {
                        case CANCEL:
                        case CANCEL_ALL:
                        case CANCEL_BY_SLOT:
                           break;
                        default:
                           ef.setCount(se.getEffectCount());
                           ef.setAbnormalTime(se.getEffectTotalTime());
                           ef.setFirstTime(se.getEffectCurTime());
                           ef.scheduleEffect(true);
                     }
                  }
               }
            }
         }
      }
   }

   public boolean containsSkill(Player owner, int referenceSkill) {
      return this.containsOwner(owner) && this.getServitorEffects(owner).containsKey(referenceSkill);
   }

   public void clearServitorEffects(Player owner, int referenceSkill) {
      if (this.containsOwner(owner)) {
         this.getServitorEffects(owner).getOrDefault(referenceSkill, Collections.emptyList()).clear();
      }
   }

   public void addServitorEffect(Player owner, int referenceSkill, Skill skill, int effectCount, int effectTime, int effectTotalTime) {
      this._servitorEffects.putIfAbsent(owner.getObjectId(), new HashMap<>());
      this._servitorEffects.get(owner.getObjectId()).putIfAbsent(owner.getClassIndex(), new HashMap<>());
      this.getServitorEffects(owner).putIfAbsent(referenceSkill, new CopyOnWriteArrayList<>());
      this.getServitorEffects(owner).get(referenceSkill).add(new SummonEffectsHolder.SummonEffect(skill, effectCount, effectTime, effectTotalTime));
   }

   public void removeServitorEffects(Player owner, int referenceSkill, int skillId) {
      this.removeEffects(this.getServitorEffects(owner, referenceSkill), skillId);
   }

   public void applyServitorEffects(ServitorInstance l2ServitorInstance, Player owner, int referenceSkill) {
      this.applyEffects(l2ServitorInstance, this.getServitorEffects(owner, referenceSkill));
   }

   public void addPetEffect(int controlObjectId, Skill skill, int effectCount, int effectTime, int effectTotalTime) {
      this._petEffects
         .computeIfAbsent(controlObjectId, k -> new CopyOnWriteArrayList())
         .add(new SummonEffectsHolder.SummonEffect(skill, effectCount, effectTime, effectTotalTime));
   }

   public boolean containsPetId(int controlObjectId) {
      return this._petEffects.containsKey(controlObjectId);
   }

   public void applyPetEffects(PetInstance l2PetInstance, int controlObjectId) {
      this.applyEffects(l2PetInstance, this._petEffects.get(controlObjectId));
   }

   public void clearPetEffects(int controlObjectId) {
      this._petEffects.getOrDefault(controlObjectId, Collections.emptyList()).clear();
   }

   public void removePetEffects(int controlObjectId, int skillId) {
      this.removeEffects(this._petEffects.get(controlObjectId), skillId);
   }

   public static SummonEffectsHolder getInstance() {
      return SummonEffectsHolder.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final SummonEffectsHolder _instance = new SummonEffectsHolder();
   }

   private class SummonEffect {
      Skill _skill;
      int _effectCount;
      int _effectCurTime;
      int _effectTotalTime;

      public SummonEffect(Skill skill, int effectCount, int effectCurTime, int effectTotalTime) {
         this._skill = skill;
         this._effectCount = effectCount;
         this._effectCurTime = effectCurTime;
         this._effectTotalTime = effectTotalTime;
      }

      public Skill getSkill() {
         return this._skill;
      }

      public int getEffectCount() {
         return this._effectCount;
      }

      public int getEffectCurTime() {
         return this._effectCurTime;
      }

      public int getEffectTotalTime() {
         return this._effectTotalTime;
      }
   }
}
