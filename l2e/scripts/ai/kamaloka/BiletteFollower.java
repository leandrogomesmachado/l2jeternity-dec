package l2e.scripts.ai.kamaloka;

import l2e.commons.util.Rnd;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class BiletteFollower extends Fighter {
   private long _skillTimer = 0L;
   private static final long _skillInterval = 20000L;

   public BiletteFollower(Attackable actor) {
      super(actor);
   }

   @Override
   protected void thinkAttack() {
      Attackable actor = this.getActiveChar();
      if (this._skillTimer == 0L) {
         this._skillTimer = System.currentTimeMillis();
      }

      if (this._skillTimer + 20000L < System.currentTimeMillis()) {
         Npc boss = null;

         for(Npc npc : World.getInstance().getAroundNpc(actor)) {
            if (npc.getId() == 18573) {
               boss = npc;
            }
         }

         if (boss != null) {
            actor.setTarget(boss);
            actor.doCast(SkillsParser.getInstance().getInfo(4065, 6));
         }

         this._skillTimer = System.currentTimeMillis();
      }

      super.thinkAttack();
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      if (attacker != null) {
         if (Rnd.chance(10)) {
            this.getActiveChar()
               .broadcastPacket(
                  new NpcSay(this.getActiveChar().getObjectId(), 0, this.getActiveChar().getId(), NpcStringId.ARG_THE_PAIN_IS_MORE_THAN_I_CAN_STAND)
               );
         } else if (Rnd.chance(3)) {
            this.getActiveChar()
               .broadcastPacket(new NpcSay(this.getActiveChar().getObjectId(), 0, this.getActiveChar().getId(), NpcStringId.AHH_HOW_DID_HE_FIND_MY_WEAKNESS));
         }
      }
   }
}
