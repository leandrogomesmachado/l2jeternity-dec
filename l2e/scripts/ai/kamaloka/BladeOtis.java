package l2e.scripts.ai.kamaloka;

import l2e.commons.util.NpcUtils;
import l2e.gameserver.ai.npc.Mystic;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class BladeOtis extends Mystic {
   private long _spawnTimer = 0L;
   private int _spawnCounter = 0;
   private static final long _spawnInterval = 60000L;
   private static final int _spawnLimit = 10;

   public BladeOtis(Attackable actor) {
      super(actor);
   }

   @Override
   protected void thinkAttack() {
      Attackable actor = this.getActiveChar();
      if (this._spawnTimer == 0L) {
         this._spawnTimer = System.currentTimeMillis();
      }

      if (actor.getCurrentHpPercents() < 60.0 && this._spawnCounter < 10 && this._spawnTimer + 60000L < System.currentTimeMillis()) {
         NpcUtils.spawnSingle(18563, Location.findPointToStay(actor.getLocation(), 200, actor.getGeoIndex(), true), actor.getReflectionId(), 0L);
         this._spawnTimer = System.currentTimeMillis();
         ++this._spawnCounter;
         actor.broadcastPacket(
            new NpcSay(actor.getObjectId(), 0, actor.getId(), NpcStringId.IF_YOU_THOUGHT_THAT_MY_SUBORDINATES_WOULD_BE_SO_FEW_YOU_ARE_MISTAKEN), 2000
         );
      }

      super.thinkAttack();
   }
}
