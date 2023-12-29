package l2e.scripts.ai.kamaloka;

import l2e.commons.util.NpcUtils;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class VenomousStorace extends Fighter {
   private long _spawnTimer = 0L;
   private static final long _spawnInterval = 50000L;

   public VenomousStorace(Attackable actor) {
      super(actor);
   }

   @Override
   protected void thinkAttack() {
      Attackable actor = this.getActiveChar();
      if (this._spawnTimer == 0L) {
         this._spawnTimer = System.currentTimeMillis();
      }

      if (this._spawnTimer + 50000L < System.currentTimeMillis()) {
         MonsterInstance follower = NpcUtils.spawnSingle(
            18572, Location.findPointToStay(actor.getLocation(), 200, actor.getGeoIndex(), true), actor.getReflectionId(), 0L
         );
         actor.broadcastPacket(new NpcSay(actor.getObjectId(), 0, actor.getId(), NpcStringId.COME_OUT_MY_SUBORDINATE_I_SUMMON_YOU_TO_DRIVE_THEM_OUT), 2000);
         follower.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, this.getAttackTarget(), Integer.valueOf(1000));
         this._spawnTimer = System.currentTimeMillis();
      }

      super.thinkAttack();
   }
}
