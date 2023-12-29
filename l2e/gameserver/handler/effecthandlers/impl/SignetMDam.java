package l2e.gameserver.handler.effecthandlers.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.ShotType;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.EffectPointInstance;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.skills.l2skills.SkillSignetCasttime;
import l2e.gameserver.model.skills.targets.TargetType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Formulas;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.MagicSkillLaunched;

public class SignetMDam extends Effect {
   private EffectPointInstance _actor;
   private boolean _srcInArena;
   private ScheduledFuture<?> _timerTask = null;

   public SignetMDam(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.SIGNET_GROUND;
   }

   @Override
   public boolean onStart() {
      if (this.getSkill() instanceof SkillSignetCasttime) {
         NpcTemplate template = NpcsParser.getInstance().getTemplate(this.getSkill().getNpcId());
         this._actor = new EffectPointInstance(IdFactory.getInstance().getNextId(), template, this.getEffector());
         this._actor.setCurrentHp(this._actor.getMaxHp());
         this._actor.setCurrentMp(this._actor.getMaxMp());
         int x = this.getEffector().getX();
         int y = this.getEffector().getY();
         int z = this.getEffector().getZ();
         if (this.getEffector().isPlayer() && this.getSkill().getTargetType() == TargetType.GROUND) {
            Location wordPosition = this.getEffector().getActingPlayer().getCurrentSkillWorldPosition();
            if (wordPosition != null) {
               x = wordPosition.getX();
               y = wordPosition.getY();
               z = wordPosition.getZ();
            }
         }

         this._actor.setIsInvul(true);
         this._actor.spawnMe(x, y, z);
         this._srcInArena = this.getEffector().isInsideZone(ZoneId.PVP) && !this.getEffector().isInsideZone(ZoneId.SIEGE);
         this._timerTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new SignetMDam.TimerTask(), 1000L, 2000L);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public void onExit() {
      if (this._timerTask != null) {
         this._timerTask.cancel(false);
      }

      this._timerTask = null;
      if (this._actor != null) {
         this._actor.deleteMe();
      }
   }

   private class TimerTask implements Runnable {
      private TimerTask() {
      }

      @Override
      public void run() {
         if (SignetMDam.this._actor != null) {
            int mpConsume = SignetMDam.this.getSkill().getMpConsume();
            if ((double)mpConsume > SignetMDam.this.getEffector().getCurrentMp()) {
               SignetMDam.this.getEffector().sendPacket(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
            } else {
               Player activeChar = SignetMDam.this.getEffector().getActingPlayer();
               activeChar.reduceCurrentMp((double)mpConsume);
               activeChar.rechargeShots(SignetMDam.this.getSkill().useSoulShot(), SignetMDam.this.getSkill().useSpiritShot());
               boolean sps = SignetMDam.this.getSkill().useSpiritShot() && SignetMDam.this.getEffector().isChargedShot(ShotType.SPIRITSHOTS);
               boolean bss = SignetMDam.this.getSkill().useSpiritShot() && SignetMDam.this.getEffector().isChargedShot(ShotType.BLESSED_SPIRITSHOTS);
               List<Creature> targets = new ArrayList<>();

               for(Creature cha : World.getInstance().getAroundCharacters(SignetMDam.this._actor, SignetMDam.this.getSkill().getAffectRange(), 300)) {
                  if (cha != null
                     && cha != activeChar
                     && (cha.isAttackable() || cha.isPlayable())
                     && !cha.isAlikeDead()
                     && (
                        !SignetMDam.this.getSkill().isOffensive()
                           || Skill.checkForAreaOffensiveSkills(SignetMDam.this.getEffector(), cha, SignetMDam.this.getSkill(), SignetMDam.this._srcInArena)
                     )) {
                     if (cha.isPlayable()) {
                        if ((SignetMDam.this.getSkill().isOffensive() || cha.getActingPlayer().isFriend(activeChar))
                           && (
                              !cha.isPlayer()
                                 || !activeChar.isPlayer()
                                 || !SignetMDam.this.getSkill().isOffensive()
                                 || !activeChar.isFriend(cha.getActingPlayer())
                           )
                           && activeChar.canAttackCharacter(cha)) {
                           targets.add(cha);
                           activeChar.updatePvPStatus(cha);
                        }
                     } else {
                        targets.add(cha);
                     }
                  }
               }

               if (!targets.isEmpty()) {
                  activeChar.broadcastPacket(
                     new MagicSkillLaunched(
                        activeChar, SignetMDam.this.getSkill().getId(), SignetMDam.this.getSkill().getLevel(), targets.toArray(new Creature[targets.size()])
                     )
                  );

                  for(Creature target : targets) {
                     boolean mcrit = Formulas.calcMCrit(activeChar.getMCriticalHit(target, SignetMDam.this.getSkill()));
                     byte shld = Formulas.calcShldUse(activeChar, target, SignetMDam.this.getSkill());
                     int mdam = (int)Formulas.calcMagicDam(activeChar, target, SignetMDam.this.getSkill(), shld, sps, bss, mcrit);
                     if (target.isSummon()) {
                        target.broadcastStatusUpdate();
                     }

                     if (mdam > 0) {
                        if (!target.isRaid() && Formulas.calcAtkBreak(target, mcrit)) {
                           target.breakAttack();
                           target.breakCast();
                        }

                        activeChar.sendDamageMessage(target, mdam, SignetMDam.this.getSkill(), mcrit, false, false);
                        target.reduceCurrentHp((double)mdam, activeChar, SignetMDam.this.getSkill());
                        target.notifyDamageReceived((double)mdam, activeChar, SignetMDam.this.getSkill(), mcrit, false);
                     }

                     target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar, Integer.valueOf(mdam));
                  }
               }

               activeChar.setChargedShot(bss ? ShotType.BLESSED_SPIRITSHOTS : ShotType.SPIRITSHOTS, false);
            }
         }
      }
   }
}
