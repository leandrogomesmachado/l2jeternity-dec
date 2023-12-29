package l2e.scripts.ai.kamaloka;

import l2e.commons.util.NpcUtils;
import l2e.gameserver.ai.npc.Mystic;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Attackable;

public class WhiteAllosce extends Mystic {
   private long _spawnTimer = 0L;
   private int _spawnCounter = 0;
   private static final long _spawnInterval = 60000L;
   private static final int _spawnLimit = 10;

   public WhiteAllosce(Attackable actor) {
      super(actor);
   }

   @Override
   protected void thinkAttack() {
      Attackable actor = this.getActiveChar();
      if (this._spawnTimer == 0L) {
         this._spawnTimer = System.currentTimeMillis();
      }

      if (this._spawnCounter < 10 && this._spawnTimer + 60000L < System.currentTimeMillis()) {
         NpcUtils.spawnSingle(18578, Location.findPointToStay(actor.getLocation(), 200, actor.getGeoIndex(), true), actor.getReflectionId(), 0L);
         this._spawnTimer = System.currentTimeMillis();
         ++this._spawnCounter;
      }

      super.thinkAttack();
   }
}
