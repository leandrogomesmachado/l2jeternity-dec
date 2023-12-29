package l2e.scripts.ai.dragonvalley;

import l2e.commons.util.Rnd;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.instance.MonsterInstance;

public class Drakos extends Fighter {
   public Drakos(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable actor = this.getActiveChar();
      if (actor != null && !actor.isDead()) {
         int chance = actor.getTemplate().getParameter("helpersSpawnChance", 0);
         if (attacker != null && actor.getCurrentHp() < actor.getMaxHp() / 2.0 && Rnd.chance(chance) && actor.isScriptValue(0)) {
            actor.setScriptValue(1);
            String[] amount = actor.getTemplate().getParameter("helpersRndAmount", "1;2").split(";");
            int rnd = Rnd.get(Integer.parseInt(amount[0]), Integer.parseInt(amount[1]));

            for(int i = 0; i < rnd; ++i) {
               try {
                  MonsterInstance npc = new MonsterInstance(IdFactory.getInstance().getNextId(), NpcsParser.getInstance().getTemplate(22823));
                  Location loc = ((MonsterInstance)actor).getMinionPosition();
                  npc.setReflectionId(actor.getReflectionId());
                  npc.setHeading(actor.getHeading());
                  npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp());
                  npc.spawnMe(loc.getX(), loc.getY(), loc.getZ());
                  npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Integer.valueOf(Rnd.get(1, 100)));
               } catch (Exception var10) {
                  var10.printStackTrace();
               }
            }
         }

         super.onEvtAttacked(attacker, damage);
      }
   }
}
