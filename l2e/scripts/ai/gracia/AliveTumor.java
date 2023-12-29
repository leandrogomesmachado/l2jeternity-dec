package l2e.scripts.ai.gracia;

import l2e.gameserver.ai.DefaultAI;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import org.apache.commons.lang3.ArrayUtils;

public class AliveTumor extends DefaultAI {
   private long _checkTimer = 0L;
   private int _coffinsCount = 0;
   private static final int[] regenCoffins = new int[]{18706, 18709, 18710};

   public AliveTumor(Attackable actor) {
      super(actor);
      actor.setIsImmobilized(true);
   }

   @Override
   protected boolean thinkActive() {
      Attackable actor = this.getActiveChar();
      if (actor == null) {
         return false;
      } else {
         if (this._checkTimer + 10000L < System.currentTimeMillis()) {
            this._checkTimer = System.currentTimeMillis();
            int i = 0;

            for(Npc n : World.getInstance().getAroundNpc(actor, 400, 300)) {
               if (ArrayUtils.contains(regenCoffins, n.getId()) && !n.isDead()) {
                  ++i;
               }
            }

            if (this._coffinsCount != i) {
               this._coffinsCount = i;
               this._coffinsCount = Math.min(this._coffinsCount, 12);
               if (this._coffinsCount > 0) {
                  actor.makeTriggerCast(SkillsParser.getInstance().getInfo(5940, this._coffinsCount), actor);
               }
            }
         }

         return super.thinkActive();
      }
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
   }

   @Override
   protected void onEvtAggression(Creature target, int aggro) {
   }
}
