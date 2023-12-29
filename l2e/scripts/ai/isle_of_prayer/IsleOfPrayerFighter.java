package l2e.scripts.ai.isle_of_prayer;

import l2e.commons.util.Rnd;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.MonsterInstance;

public class IsleOfPrayerFighter extends Fighter {
   private static final int[] PENALTY_MOBS = new int[]{18364, 18365, 18366};

   public IsleOfPrayerFighter(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable actor = this.getActiveChar();
      if (attacker != null && actor.isScriptValue(0) && attacker.isPlayer()) {
         Party party = attacker.getActingPlayer().getParty();
         if (party != null && party.getMemberCount() > 2) {
            actor.setScriptValue(1);

            for(int i = 0; i < 2; ++i) {
               MonsterInstance npc = new MonsterInstance(
                  IdFactory.getInstance().getNextId(), NpcsParser.getInstance().getTemplate(PENALTY_MOBS[Rnd.get(PENALTY_MOBS.length)])
               );
               Location loc = ((MonsterInstance)actor).getMinionPosition();
               npc.setReflectionId(actor.getReflectionId());
               npc.setHeading(actor.getHeading());
               npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp());
               npc.spawnMe(loc.getX(), loc.getY(), loc.getZ());
               npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Integer.valueOf(Rnd.get(1, 100)));
            }
         }
      }

      super.onEvtAttacked(attacker, damage);
   }

   @Override
   protected void onEvtDead(Creature killer) {
      if (killer != null) {
         Player player = killer.getActingPlayer();
         if (player != null) {
            Attackable actor = this.getActiveChar();
            switch(actor.getId()) {
               case 22259:
                  if (Rnd.chance(26)) {
                     actor.dropItem(player, 9593, 1L);
                  }
                  break;
               case 22263:
                  if (Rnd.chance(14)) {
                     actor.dropItem(player, 9594, 1L);
                  }
            }
         }
      }

      super.onEvtDead(killer);
   }
}
