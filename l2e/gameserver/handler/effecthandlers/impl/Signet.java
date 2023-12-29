package l2e.gameserver.handler.effecthandlers.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.EffectPointInstance;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.skills.l2skills.SkillSignet;
import l2e.gameserver.model.skills.l2skills.SkillSignetCasttime;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.MagicSkillUse;

public class Signet extends Effect {
   private Skill _skill;
   private boolean _srcInArena;
   protected ScheduledFuture<?> _timerTask = null;
   private EffectPointInstance _actor;

   public Signet(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.SIGNET_EFFECT;
   }

   @Override
   public boolean onStart() {
      if (this.getSkill() instanceof SkillSignet || this.getSkill() instanceof SkillSignetCasttime) {
         this._skill = SkillsParser.getInstance().getInfo(this.getSkill().getEffectId(), this.getSkill().getLevel());
      }

      this._actor = (EffectPointInstance)this.getEffected();
      this._srcInArena = this.getEffector().isInsideZone(ZoneId.PVP) && !this.getEffector().isInsideZone(ZoneId.SIEGE);
      if (this._timerTask != null) {
         this._timerTask.cancel(false);
      }

      this._timerTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Signet.TimerTask(), 1000L, 2000L);
      return true;
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
         if (Signet.this._skill == null) {
            Signet._log.warning("Signet Effect null for skill: " + Signet.this.getSkill().getId());
         } else {
            int mpConsume = Signet.this._skill.getMpConsume();
            if ((double)mpConsume > Signet.this.getEffector().getCurrentMp()) {
               Signet.this.getEffector().sendPacket(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
            } else {
               Player activeChar = Signet.this.getEffector().getActingPlayer();
               activeChar.reduceCurrentMp((double)mpConsume);
               List<Creature> targets = new ArrayList<>();

               for(Creature cha : World.getInstance().getAroundCharacters(Signet.this._actor, Signet.this.getSkill().getAffectRange(), 300)) {
                  if (cha != null
                     && !cha.isAlikeDead()
                     && (cha != activeChar || !Signet.this._skill.isOffensive())
                     && (Signet.this._skill.isOffensive() || cha.isPlayable())
                     && (
                        !cha.isPlayable()
                           || (
                                 !Signet.this._skill.isOffensive()
                                    || Skill.checkForAreaOffensiveSkills(Signet.this.getEffector(), cha, Signet.this.getSkill(), Signet.this._srcInArena)
                              )
                              && (Signet.this._skill.isOffensive() || cha.getActingPlayer().isFriend(activeChar))
                              && (
                                 !cha.isPlayer() || !activeChar.isPlayer() || !Signet.this._skill.isOffensive() || !activeChar.isFriend(cha.getActingPlayer())
                              )
                     )) {
                     Signet.this._actor
                        .broadcastPacket(new MagicSkillUse(Signet.this.getEffected(), cha, Signet.this._skill.getId(), Signet.this._skill.getLevel(), 0, 0));
                     targets.add(cha);
                  }
               }

               if (!targets.isEmpty()) {
                  Signet.this.getEffector().callSkill(Signet.this._skill, targets.toArray(new Creature[targets.size()]));
               }
            }
         }
      }
   }
}
