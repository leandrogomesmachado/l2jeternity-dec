package l2e.scripts.ai.freya;

import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;

public class Breath extends Fighter {
   public Breath(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtSpawn() {
      super.onEvtSpawn();
      ReflectionWorld instance = ReflectionManager.getInstance().getWorld(this.getActiveChar().getReflectionId());
      if (instance != null && instance.getAllowed() != null) {
         for(int objectId : instance.getAllowed()) {
            Player activeChar = World.getInstance().getPlayer(objectId);
            if (activeChar != null) {
               this.notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, Integer.valueOf(300));
            }
         }
      }
   }
}
