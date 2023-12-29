package l2e.scripts.ai.kamaloka;

import l2e.commons.util.NpcUtils;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class OlAriosh extends Fighter {
   private long _spawnTimer = 0L;
   private static final long _spawnInterval = 60000L;
   MonsterInstance follower = null;

   public OlAriosh(Attackable actor) {
      super(actor);
   }

   @Override
   protected void thinkAttack() {
      Attackable actor = this.getActiveChar();
      if ((this.follower == null || this.follower.isDead()) && this._spawnTimer + 60000L < System.currentTimeMillis()) {
         this.follower = NpcUtils.spawnSingle(
            18556, Location.findPointToStay(actor.getLocation(), 200, actor.getGeoIndex(), true), actor.getReflectionId(), 0L
         );
         this.follower.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, this.getAttackTarget(), Integer.valueOf(1000));
         this._spawnTimer = System.currentTimeMillis();
         actor.broadcastPacket(new NpcSay(actor.getObjectId(), 0, actor.getId(), NpcStringId.WHAT_ARE_YOU_DOING_HURRY_UP_AND_HELP_ME), 2000);
      }

      super.thinkAttack();
   }
}
