package l2e.gameserver.model.actor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.instancemanager.DoubleSessionManager;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.events.PlayableEvents;
import l2e.gameserver.model.actor.stat.PlayableStat;
import l2e.gameserver.model.actor.status.PlayableStatus;
import l2e.gameserver.model.actor.templates.character.CharTemplate;
import l2e.gameserver.model.entity.Reflection;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.effects.EffectFlag;
import l2e.gameserver.model.skills.effects.EffectType;

public abstract class Playable extends Creature {
   private Creature _lockedTarget = null;
   private Player transferDmgTo = null;
   private final List<Integer> _hitmanTargets = new CopyOnWriteArrayList<>();

   public Playable(int objectId, CharTemplate template) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.Playable);
      this.setIsInvul(false);
   }

   public PlayableStat getStat() {
      return (PlayableStat)super.getStat();
   }

   @Override
   public void initCharStat() {
      this.setStat(new PlayableStat(this));
   }

   public PlayableStatus getStatus() {
      return (PlayableStatus)super.getStatus();
   }

   @Override
   public void initCharStatus() {
      this.setStatus(new PlayableStatus(this));
   }

   @Override
   protected void onDeath(Creature killer) {
      this.setTarget(null);
      this.stopMove(null);
      boolean fightEventKeepBuffs = this.isPlayer() && this.isInFightEvent() && !this.getFightEvent().loseBuffsOnDeath(this.getActingPlayer());
      if (this.isPhoenixBlessed()) {
         if (this.isCharmOfLuckAffected()) {
            this.stopEffects(EffectType.CHARM_OF_LUCK);
         }

         if (this.isNoblesseBlessed()) {
            this.stopEffects(EffectType.NOBLESSE_BLESSING);
         }
      } else if (!fightEventKeepBuffs) {
         if (this.isNoblesseBlessed()) {
            this.stopEffects(EffectType.NOBLESSE_BLESSING);
            if (this.isCharmOfLuckAffected()) {
               this.stopEffects(EffectType.CHARM_OF_LUCK);
            }
         } else {
            this.stopAllEffectsExceptThoseThatLastThroughDeath();
         }
      }

      this.broadcastStatusUpdate();
      this.onDeathInZones(this);
      Player actingPlayer = this.getActingPlayer();
      if (actingPlayer != null && !actingPlayer.isNotifyQuestOfDeathEmpty()) {
         for(QuestState qs : actingPlayer.getNotifyQuestOfDeath()) {
            qs.getQuest().notifyDeath((Creature)(killer == null ? this : killer), this, qs);
         }
      }

      if (this.getReflectionId() > 0) {
         Reflection instance = ReflectionManager.getInstance().getReflection(this.getReflectionId());
         if (instance != null) {
            instance.notifyDeath(killer, this);
         }
      }

      if (killer != null && killer.isPlayer() && !killer.isFakePlayer()) {
         Player player = killer.getActingPlayer();
         if (player != null) {
            player.onKillUpdatePvPKarma(this);
            if (this.isPlayer()) {
               DoubleSessionManager.getInstance().setLastDeathTime(this.getObjectId());
            }
         }
      }

      this.getAI().notifyEvent(CtrlEvent.EVT_DEAD);
   }

   public boolean checkIfPvP(Creature target) {
      if (target == null) {
         return false;
      } else if (target == this) {
         return false;
      } else if (!target.isPlayable()) {
         return false;
      } else {
         Player player = this.getActingPlayer();
         if (player == null) {
            return false;
         } else if (player.getKarma() != 0) {
            return false;
         } else {
            Player targetPlayer = target.getActingPlayer();
            if (targetPlayer == null) {
               return false;
            } else if (targetPlayer == this) {
               return false;
            } else if (targetPlayer.getKarma() != 0) {
               return false;
            } else {
               return targetPlayer.getPvpFlag() != 0;
            }
         }
      }
   }

   public final boolean isNoblesseBlessed() {
      return this.isAffected(EffectFlag.NOBLESS_BLESSING);
   }

   public final boolean isPhoenixBlessed() {
      return this.isAffected(EffectFlag.PHOENIX_BLESSING);
   }

   public boolean isSilentMoving() {
      return this.isAffected(EffectFlag.SILENT_MOVE);
   }

   public final boolean isProtectionBlessingAffected() {
      return this.isAffected(EffectFlag.PROTECTION_BLESSING);
   }

   public final boolean isCharmOfLuckAffected() {
      return this.isAffected(EffectFlag.CHARM_OF_LUCK);
   }

   @Override
   public void updateEffectIcons(boolean partyOnly) {
      this._effects.updateEffectIcons(partyOnly, true);
   }

   public boolean isLockedTarget() {
      return this._lockedTarget != null;
   }

   public Creature getLockedTarget() {
      return this._lockedTarget;
   }

   public void setLockedTarget(Creature cha) {
      this._lockedTarget = cha;
   }

   public void setTransferDamageTo(Player val) {
      this.transferDmgTo = val;
   }

   public Player getTransferingDamageTo() {
      return this.transferDmgTo;
   }

   @Override
   public void initCharEvents() {
      this.setCharEvents(new PlayableEvents(this));
   }

   public PlayableEvents getEvents() {
      return (PlayableEvents)super.getEvents();
   }

   public abstract int getKarma();

   public abstract byte getPvpFlag();

   public abstract boolean useMagic(Skill var1, boolean var2, boolean var3, boolean var4);

   public abstract void store();

   public abstract void storeEffect(boolean var1);

   public abstract void restoreEffects();

   @Override
   public boolean isPlayable() {
      return true;
   }

   public void addHitmanTarget(int hitmanTarget) {
      this._hitmanTargets.add(hitmanTarget);
   }

   public void removeHitmanTarget(int hitmanTarget) {
      String line = "";
      int amount = 0;

      for(int charId : this._hitmanTargets) {
         if (charId != hitmanTarget) {
            ++amount;
            line = line + "" + charId + "";
            if (amount < this._hitmanTargets.size() - 1) {
               line = line + ";";
            }
         }
      }

      this._hitmanTargets.clear();
      if (!line.isEmpty()) {
         String[] targets = line.split(";");

         for(String charId : targets) {
            this._hitmanTargets.add(Integer.parseInt(charId));
         }
      }
   }

   public List<Integer> getHitmanTargets() {
      return this._hitmanTargets;
   }

   public String saveHitmanTargets() {
      if (this._hitmanTargets != null && !this._hitmanTargets.isEmpty()) {
         String line = "";
         int amount = 0;

         for(int charId : this._hitmanTargets) {
            ++amount;
            line = line + "" + charId + "";
            if (amount < this._hitmanTargets.size()) {
               line = line + ";";
            }
         }

         return line;
      } else {
         return null;
      }
   }

   public void loadHitmanTargets(String line) {
      if (line != null && !line.isEmpty()) {
         String[] targets = line.split(";");

         for(String charId : targets) {
            this._hitmanTargets.add(Integer.parseInt(charId));
         }
      }
   }

   @Override
   public boolean canBeAttacked() {
      return true;
   }
}
