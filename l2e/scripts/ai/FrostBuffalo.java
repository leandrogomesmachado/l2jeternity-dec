package l2e.scripts.ai;

import l2e.commons.util.Rnd;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.spawn.Spawner;

public class FrostBuffalo extends Fighter {
   public FrostBuffalo(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtSeeSpell(Skill skill, Creature caster) {
      Attackable actor = this.getActiveChar();
      if (!skill.isMagic()) {
         if (actor.isScriptValue(0)) {
            actor.setScriptValue(1);

            for(int i = 0; i < 4; ++i) {
               try {
                  Spawner sp = new Spawner(NpcsParser.getInstance().getTemplate(22093));
                  sp.setLocation(Location.findPointToStay(actor, 100, 120, false));
                  Npc npc = sp.doSpawn(true);
                  if (caster.isPet() || caster.isSummon()) {
                     npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, caster, Integer.valueOf(Rnd.get(2, 100)));
                  }

                  npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, caster.getActingPlayer(), Integer.valueOf(Rnd.get(1, 100)));
               } catch (Exception var7) {
                  var7.printStackTrace();
               }
            }
         }
      }
   }
}
