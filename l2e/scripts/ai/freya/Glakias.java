package l2e.scripts.ai.freya;

import l2e.commons.util.Rnd;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class Glakias extends Fighter {
   public Glakias(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable actor = this.getActiveChar();
      if (attacker != null && actor.getCurrentHp() < actor.getMaxHp() * 0.2 && actor.isScriptValue(0)) {
         NpcStringId stringId = null;
         switch(Rnd.get(4)) {
            case 0:
               stringId = NpcStringId.ARCHER_GIVE_YOUR_BREATH_FOR_THE_INTRUDER;
               break;
            case 1:
               stringId = NpcStringId.MY_KNIGHTS_SHOW_YOUR_LOYALTY;
               break;
            case 2:
               stringId = NpcStringId.I_CAN_TAKE_IT_NO_LONGER;
               break;
            case 3:
               stringId = NpcStringId.ARCHER_HEED_MY_CALL;
         }

         actor.broadcastPacket(new NpcSay(actor.getObjectId(), 1, actor.getId(), stringId), 2000);
         actor.setScriptValue(1);
      }

      super.onEvtAttacked(attacker, damage);
   }

   @Override
   protected boolean thinkActive() {
      ReflectionWorld instance = ReflectionManager.getInstance().getWorld(this.getActiveChar().getReflectionId());
      if (instance != null && instance.getAllowed() != null) {
         for(int objectId : instance.getAllowed()) {
            Player activeChar = World.getInstance().getPlayer(objectId);
            if (activeChar != null) {
               this.getActiveChar().getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar, Integer.valueOf(300));
            }
         }
      }

      return true;
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
