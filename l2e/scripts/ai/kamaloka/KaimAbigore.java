package l2e.scripts.ai.kamaloka;

import l2e.commons.util.NpcUtils;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.instance.MonsterInstance;

public class KaimAbigore extends Fighter {
   private long _spawnTimer = 0L;
   private static final long _spawnInterval = 60000L;

   public KaimAbigore(Attackable actor) {
      super(actor);
   }

   @Override
   protected void thinkAttack() {
      Attackable actor = this.getActiveChar();
      if (this._spawnTimer == 0L) {
         this._spawnTimer = System.currentTimeMillis();
      }

      if (this._spawnTimer + 60000L < System.currentTimeMillis()) {
         MonsterInstance follower = NpcUtils.spawnSingle(
            18567, Location.findPointToStay(actor.getLocation(), 600, actor.getGeoIndex(), true), actor.getReflectionId(), 0L
         );
         follower.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, this.getAttackTarget(), Integer.valueOf(1000));
         this._spawnTimer = System.currentTimeMillis();
      }

      super.thinkAttack();
   }
}
