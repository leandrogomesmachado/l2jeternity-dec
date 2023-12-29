package l2e.scripts.ai.gracia;

import l2e.commons.lang.ArrayUtils;
import l2e.commons.util.NpcUtils;
import l2e.commons.util.Rnd;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.entity.Reflection;
import l2e.gameserver.model.skills.effects.Effect;

public class YehanBrother extends Fighter {
   private long _spawnTimer = 0L;
   private static final int[] _minions = ArrayUtils.createAscendingArray(22509, 22512);

   public YehanBrother(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtSpawn() {
      super.onEvtSpawn();
      this._spawnTimer = System.currentTimeMillis();
   }

   private Npc getBrother() {
      Attackable actor = this.getActiveChar();
      if (actor == null) {
         return null;
      } else {
         int brotherId = 0;
         if (actor.getId() == 25665) {
            brotherId = 25666;
         } else if (actor.getId() == 25666) {
            brotherId = 25665;
         }

         Reflection inst = ReflectionManager.getInstance().getReflection(actor.getReflectionId());
         if (inst != null) {
            for(Npc npc : inst.getNpcs()) {
               if (npc.getId() == brotherId) {
                  return npc;
               }
            }
         }

         return null;
      }
   }

   @Override
   protected void thinkAttack() {
      Attackable actor = this.getActiveChar();
      if (actor != null) {
         Npc brother = this.getBrother();
         if (!brother.isDead() && !actor.isInRange(brother, 300L)) {
            actor.makeTriggerCast(SkillsParser.getInstance().getInfo(6371, 1), actor);
         } else {
            this.removeInvul(actor);
         }

         if (this._spawnTimer + 40000L < System.currentTimeMillis()) {
            this._spawnTimer = System.currentTimeMillis();
            Npc mob = NpcUtils.spawnSingle(_minions[Rnd.get(_minions.length)], Location.findAroundPosition(actor, 100, 300), actor.getReflectionId(), 0L);
            if (actor.getAI().getAttackTarget() != null) {
               mob.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, actor.getAI().getAttackTarget(), Integer.valueOf(1000));
            }
         }

         super.thinkAttack();
      }
   }

   private void removeInvul(Npc npc) {
      for(Effect e : npc.getEffectList().getAllEffects()) {
         if (e.getSkill().getId() == 6371) {
            e.exit();
         }
      }
   }
}
