package l2e.gameserver.handler.effecthandlers.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;
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

public class SignetAntiSummon extends Effect {
   private Skill _skill;
   private boolean _srcInArena;
   protected ScheduledFuture<?> timerTask;

   public SignetAntiSummon(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.SIGNET_GROUND;
   }

   @Override
   public void onExit() {
      if (this.timerTask != null) {
         this.timerTask.cancel(false);
      }

      if (this.getEffected() != null) {
         this.getEffected().deleteMe();
      }
   }

   @Override
   public boolean onStart() {
      if (this.getSkill() instanceof SkillSignet) {
         this._skill = SkillsParser.getInstance().getInfo(this.getSkill().getEffectId(), this.getSkill().getLevel());
      } else if (this.getSkill() instanceof SkillSignetCasttime) {
         this._skill = SkillsParser.getInstance().getInfo(this.getSkill().getEffectId(), this.getSkill().getLevel());
      }

      this._srcInArena = this.getEffector().isInsideZone(ZoneId.PVP) && !this.getEffector().isInsideZone(ZoneId.SIEGE);
      this.timerTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new SignetAntiSummon.TimerTask(), 2000L, 9000L);
      return true;
   }

   protected class TimerTask implements Runnable {
      @Override
      public void run() {
         if (SignetAntiSummon.this._skill != null) {
            int mpConsume = SignetAntiSummon.this._skill.getMpConsume();
            if ((double)mpConsume > SignetAntiSummon.this.getEffector().getCurrentMp()) {
               SignetAntiSummon.this.getEffector().sendPacket(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
            } else {
               SignetAntiSummon.this.getEffector().reduceCurrentMp((double)mpConsume);
               List<Creature> targets = new ArrayList<>();
               Player caster = SignetAntiSummon.this.getEffector().getActingPlayer();

               for(Creature cha : World.getInstance()
                  .getAroundCharacters(SignetAntiSummon.this.getEffected(), SignetAntiSummon.this.getSkill().getAffectRange(), 300)) {
                  if (cha != null && cha.isPlayable() && caster.canAttackCharacter(cha)) {
                     Player owner = null;
                     if (cha.isSummon()) {
                        owner = ((Summon)cha).getOwner();
                     } else {
                        owner = cha.getActingPlayer();
                     }

                     if (owner != null && owner.hasSummon()) {
                        owner.getSummon().unSummon(owner);
                        owner.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, SignetAntiSummon.this.getEffector(), Integer.valueOf(0));
                     }

                     if (!SignetAntiSummon.this._skill.isOffensive()
                        || Skill.checkForAreaOffensiveSkills(
                           SignetAntiSummon.this.getEffector(), cha, SignetAntiSummon.this._skill, SignetAntiSummon.this._srcInArena
                        )) {
                        SignetAntiSummon.this.getEffected()
                           .broadcastPacket(
                              new MagicSkillUse(
                                 SignetAntiSummon.this.getEffected(), cha, SignetAntiSummon.this._skill.getId(), SignetAntiSummon.this._skill.getLevel(), 0, 0
                              )
                           );
                        targets.add(cha);
                     }
                  }
               }

               if (!targets.isEmpty()) {
                  SignetAntiSummon.this.getEffector().callSkill(SignetAntiSummon.this._skill, targets.toArray(new Creature[targets.size()]));
               }
            }
         }
      }
   }
}
