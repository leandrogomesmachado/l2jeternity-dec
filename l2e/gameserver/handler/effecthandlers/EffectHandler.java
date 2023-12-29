package l2e.gameserver.handler.effecthandlers;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.handler.effecthandlers.impl.AbortCast;
import l2e.gameserver.handler.effecthandlers.impl.Betray;
import l2e.gameserver.handler.effecthandlers.impl.BigHead;
import l2e.gameserver.handler.effecthandlers.impl.BlockAction;
import l2e.gameserver.handler.effecthandlers.impl.BlockBuffSlot;
import l2e.gameserver.handler.effecthandlers.impl.BlockChat;
import l2e.gameserver.handler.effecthandlers.impl.BlockParty;
import l2e.gameserver.handler.effecthandlers.impl.BlockResurrection;
import l2e.gameserver.handler.effecthandlers.impl.Bluff;
import l2e.gameserver.handler.effecthandlers.impl.Buff;
import l2e.gameserver.handler.effecthandlers.impl.CallParty;
import l2e.gameserver.handler.effecthandlers.impl.CallPc;
import l2e.gameserver.handler.effecthandlers.impl.CallSkills;
import l2e.gameserver.handler.effecthandlers.impl.Cancel;
import l2e.gameserver.handler.effecthandlers.impl.CancelAll;
import l2e.gameserver.handler.effecthandlers.impl.CancelBySlot;
import l2e.gameserver.handler.effecthandlers.impl.CancelProbability;
import l2e.gameserver.handler.effecthandlers.impl.ChameleonRest;
import l2e.gameserver.handler.effecthandlers.impl.ChanceSkillTrigger;
import l2e.gameserver.handler.effecthandlers.impl.ChangeFace;
import l2e.gameserver.handler.effecthandlers.impl.ChangeHairColor;
import l2e.gameserver.handler.effecthandlers.impl.ChangeHairStyle;
import l2e.gameserver.handler.effecthandlers.impl.CharmOfCourage;
import l2e.gameserver.handler.effecthandlers.impl.CharmOfLuck;
import l2e.gameserver.handler.effecthandlers.impl.ClanGate;
import l2e.gameserver.handler.effecthandlers.impl.Confusion;
import l2e.gameserver.handler.effecthandlers.impl.ConsumeBody;
import l2e.gameserver.handler.effecthandlers.impl.CpDamPercent;
import l2e.gameserver.handler.effecthandlers.impl.CpHeal;
import l2e.gameserver.handler.effecthandlers.impl.CpHealOverTime;
import l2e.gameserver.handler.effecthandlers.impl.CpHealPercent;
import l2e.gameserver.handler.effecthandlers.impl.CubicMastery;
import l2e.gameserver.handler.effecthandlers.impl.DamOverTime;
import l2e.gameserver.handler.effecthandlers.impl.DamOverTimePercent;
import l2e.gameserver.handler.effecthandlers.impl.Debuff;
import l2e.gameserver.handler.effecthandlers.impl.Disarm;
import l2e.gameserver.handler.effecthandlers.impl.EnergyDamOverTime;
import l2e.gameserver.handler.effecthandlers.impl.EnlargeAbnormalSlot;
import l2e.gameserver.handler.effecthandlers.impl.EquipmentSet;
import l2e.gameserver.handler.effecthandlers.impl.Escape;
import l2e.gameserver.handler.effecthandlers.impl.FakeDeath;
import l2e.gameserver.handler.effecthandlers.impl.Fear;
import l2e.gameserver.handler.effecthandlers.impl.Flag;
import l2e.gameserver.handler.effecthandlers.impl.FocusEnergy;
import l2e.gameserver.handler.effecthandlers.impl.FocusMaxEnergy;
import l2e.gameserver.handler.effecthandlers.impl.FocusSouls;
import l2e.gameserver.handler.effecthandlers.impl.Fusion;
import l2e.gameserver.handler.effecthandlers.impl.GiveRecommendation;
import l2e.gameserver.handler.effecthandlers.impl.GiveSp;
import l2e.gameserver.handler.effecthandlers.impl.Grow;
import l2e.gameserver.handler.effecthandlers.impl.Harvesting;
import l2e.gameserver.handler.effecthandlers.impl.Heal;
import l2e.gameserver.handler.effecthandlers.impl.HealOverTime;
import l2e.gameserver.handler.effecthandlers.impl.HealPercent;
import l2e.gameserver.handler.effecthandlers.impl.Hide;
import l2e.gameserver.handler.effecthandlers.impl.HolythingPossess;
import l2e.gameserver.handler.effecthandlers.impl.HpByLevel;
import l2e.gameserver.handler.effecthandlers.impl.IgnoreSkills;
import l2e.gameserver.handler.effecthandlers.impl.ImmobileBuff;
import l2e.gameserver.handler.effecthandlers.impl.ImmobilePetBuff;
import l2e.gameserver.handler.effecthandlers.impl.Invincible;
import l2e.gameserver.handler.effecthandlers.impl.Lucky;
import l2e.gameserver.handler.effecthandlers.impl.ManaDamOverTime;
import l2e.gameserver.handler.effecthandlers.impl.ManaHeal;
import l2e.gameserver.handler.effecthandlers.impl.ManaHealByLevel;
import l2e.gameserver.handler.effecthandlers.impl.ManaHealOverTime;
import l2e.gameserver.handler.effecthandlers.impl.ManaHealPercent;
import l2e.gameserver.handler.effecthandlers.impl.MpByLevel;
import l2e.gameserver.handler.effecthandlers.impl.MpConsumePerLevel;
import l2e.gameserver.handler.effecthandlers.impl.Mute;
import l2e.gameserver.handler.effecthandlers.impl.NoblesseBless;
import l2e.gameserver.handler.effecthandlers.impl.OpenCommonRecipeBook;
import l2e.gameserver.handler.effecthandlers.impl.OpenDwarfRecipeBook;
import l2e.gameserver.handler.effecthandlers.impl.OutpostDestroy;
import l2e.gameserver.handler.effecthandlers.impl.Paralyze;
import l2e.gameserver.handler.effecthandlers.impl.PcBangPointUp;
import l2e.gameserver.handler.effecthandlers.impl.Petrification;
import l2e.gameserver.handler.effecthandlers.impl.PhoenixBless;
import l2e.gameserver.handler.effecthandlers.impl.PhysicalAttackMute;
import l2e.gameserver.handler.effecthandlers.impl.PhysicalMute;
import l2e.gameserver.handler.effecthandlers.impl.ProtectionBlessing;
import l2e.gameserver.handler.effecthandlers.impl.RandomizeHate;
import l2e.gameserver.handler.effecthandlers.impl.RebalanceHP;
import l2e.gameserver.handler.effecthandlers.impl.RecoBonus;
import l2e.gameserver.handler.effecthandlers.impl.Recovery;
import l2e.gameserver.handler.effecthandlers.impl.RefuelAirship;
import l2e.gameserver.handler.effecthandlers.impl.Relax;
import l2e.gameserver.handler.effecthandlers.impl.ResetReflectionEntry;
import l2e.gameserver.handler.effecthandlers.impl.Restoration;
import l2e.gameserver.handler.effecthandlers.impl.RestorationRandom;
import l2e.gameserver.handler.effecthandlers.impl.Root;
import l2e.gameserver.handler.effecthandlers.impl.ServitorShare;
import l2e.gameserver.handler.effecthandlers.impl.SetSkill;
import l2e.gameserver.handler.effecthandlers.impl.Signet;
import l2e.gameserver.handler.effecthandlers.impl.SignetAntiSummon;
import l2e.gameserver.handler.effecthandlers.impl.SignetMDam;
import l2e.gameserver.handler.effecthandlers.impl.SignetNoise;
import l2e.gameserver.handler.effecthandlers.impl.SilentMove;
import l2e.gameserver.handler.effecthandlers.impl.SingleTarget;
import l2e.gameserver.handler.effecthandlers.impl.Sleep;
import l2e.gameserver.handler.effecthandlers.impl.SoulEating;
import l2e.gameserver.handler.effecthandlers.impl.Spoil;
import l2e.gameserver.handler.effecthandlers.impl.StealBuffs;
import l2e.gameserver.handler.effecthandlers.impl.Stun;
import l2e.gameserver.handler.effecthandlers.impl.SummonAgathion;
import l2e.gameserver.handler.effecthandlers.impl.SummonCubic;
import l2e.gameserver.handler.effecthandlers.impl.SummonNpc;
import l2e.gameserver.handler.effecthandlers.impl.SummonPet;
import l2e.gameserver.handler.effecthandlers.impl.SummonTrap;
import l2e.gameserver.handler.effecthandlers.impl.Sweeper;
import l2e.gameserver.handler.effecthandlers.impl.TargetCancel;
import l2e.gameserver.handler.effecthandlers.impl.TargetMe;
import l2e.gameserver.handler.effecthandlers.impl.Teleport;
import l2e.gameserver.handler.effecthandlers.impl.TransferDamage;
import l2e.gameserver.handler.effecthandlers.impl.TransferHate;
import l2e.gameserver.handler.effecthandlers.impl.Transformation;
import l2e.gameserver.handler.effecthandlers.impl.TransformationDispel;
import l2e.gameserver.handler.effecthandlers.impl.UnsummonAgathion;
import l2e.gameserver.handler.effecthandlers.impl.VisualSkin;
import l2e.gameserver.handler.effecthandlers.impl.VitalityPointUp;
import l2e.gameserver.model.skills.effects.Effect;

public final class EffectHandler {
   private static final Logger _log = Logger.getLogger(EffectHandler.class.getName());
   private final Map<Integer, Class<? extends Effect>> _handlers = new HashMap<>();
   private static final Class<?> _loadInstances = EffectHandler.class;
   private static final Class<?>[] _effects = new Class[]{
      AbortCast.class,
      Betray.class,
      BigHead.class,
      BlockAction.class,
      BlockBuffSlot.class,
      BlockChat.class,
      BlockParty.class,
      BlockResurrection.class,
      Bluff.class,
      Buff.class,
      CallParty.class,
      CallPc.class,
      CallSkills.class,
      Cancel.class,
      CancelBySlot.class,
      CancelProbability.class,
      CancelAll.class,
      ChameleonRest.class,
      ChanceSkillTrigger.class,
      ChangeFace.class,
      ChangeHairColor.class,
      ChangeHairStyle.class,
      CharmOfCourage.class,
      CharmOfLuck.class,
      ClanGate.class,
      Confusion.class,
      ConsumeBody.class,
      CpHeal.class,
      CpHealOverTime.class,
      CpHealPercent.class,
      CpDamPercent.class,
      CubicMastery.class,
      DamOverTime.class,
      DamOverTimePercent.class,
      Debuff.class,
      Disarm.class,
      EnergyDamOverTime.class,
      EnlargeAbnormalSlot.class,
      EquipmentSet.class,
      Escape.class,
      FakeDeath.class,
      Fear.class,
      Flag.class,
      FocusEnergy.class,
      FocusMaxEnergy.class,
      FocusSouls.class,
      Fusion.class,
      GiveRecommendation.class,
      GiveSp.class,
      Grow.class,
      Harvesting.class,
      HealOverTime.class,
      HealPercent.class,
      Heal.class,
      Hide.class,
      HolythingPossess.class,
      HpByLevel.class,
      IgnoreSkills.class,
      ImmobileBuff.class,
      ImmobilePetBuff.class,
      Invincible.class,
      Lucky.class,
      ManaDamOverTime.class,
      ManaHeal.class,
      ManaHealByLevel.class,
      ManaHealOverTime.class,
      ManaHealPercent.class,
      MpByLevel.class,
      MpConsumePerLevel.class,
      Mute.class,
      NoblesseBless.class,
      OpenCommonRecipeBook.class,
      OpenDwarfRecipeBook.class,
      OutpostDestroy.class,
      Paralyze.class,
      Petrification.class,
      PhoenixBless.class,
      PhysicalAttackMute.class,
      PhysicalMute.class,
      PcBangPointUp.class,
      ProtectionBlessing.class,
      RebalanceHP.class,
      RecoBonus.class,
      ResetReflectionEntry.class,
      RandomizeHate.class,
      Recovery.class,
      RefuelAirship.class,
      Relax.class,
      Restoration.class,
      RestorationRandom.class,
      Root.class,
      ServitorShare.class,
      SetSkill.class,
      Signet.class,
      SignetAntiSummon.class,
      SignetMDam.class,
      SignetNoise.class,
      SilentMove.class,
      SingleTarget.class,
      Sleep.class,
      SoulEating.class,
      Spoil.class,
      StealBuffs.class,
      Stun.class,
      SummonAgathion.class,
      SummonCubic.class,
      SummonNpc.class,
      SummonPet.class,
      SummonTrap.class,
      Sweeper.class,
      TargetCancel.class,
      TargetMe.class,
      Teleport.class,
      TransferDamage.class,
      TransferHate.class,
      Transformation.class,
      TransformationDispel.class,
      UnsummonAgathion.class,
      VitalityPointUp.class,
      VisualSkin.class
   };

   protected EffectHandler() {
   }

   public void registerHandler(String name, Class<? extends Effect> func) {
      this._handlers.put(name.hashCode(), func);
   }

   public final Class<? extends Effect> getHandler(String name) {
      return this._handlers.get(name.hashCode());
   }

   public int size() {
      return this._handlers.size();
   }

   public void executeScript() {
      Object loadInstance = null;
      Method method = null;

      try {
         method = _loadInstances.getMethod("getInstance");
         loadInstance = method.invoke(_loadInstances);
      } catch (Exception var10) {
         _log.log(Level.WARNING, "Failed invoking getInstance method for handler: " + _loadInstances.getSimpleName(), (Throwable)var10);
         return;
      }

      method = null;

      for(Class<?> c : _effects) {
         try {
            if (c != null) {
               if (method == null) {
                  method = loadInstance.getClass().getMethod("registerHandler", String.class, Class.class);
               }

               method.invoke(loadInstance, c.getSimpleName(), c);
            }
         } catch (Exception var9) {
            _log.log(Level.WARNING, "Failed loading effect handler: " + c.getSimpleName(), (Throwable)var9);
         }
      }

      try {
         method = loadInstance.getClass().getMethod("size");
         Object returnVal = method.invoke(loadInstance);
         if (Config.DEBUG) {
            _log.log(Level.INFO, "SkillTreesParser: Loaded " + returnVal + " effect templates.");
         }
      } catch (Exception var8) {
         _log.log(Level.WARNING, "Failed invoking size method for handler: " + loadInstance.getClass().getSimpleName(), (Throwable)var8);
      }
   }

   public static EffectHandler getInstance() {
      return EffectHandler.SingletonHolder._instance;
   }

   private static final class SingletonHolder {
      protected static final EffectHandler _instance = new EffectHandler();
   }
}
