package l2e.scripts.ai.hellbound;

import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.ai.npc.Mystic;
import l2e.gameserver.instancemanager.HellboundManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.MinionList;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.instance.MinionInstance;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.gameserver.taskmanager.DecayTaskManager;

public class JuniorMystic extends Mystic {
   public JuniorMystic(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtSpawn() {
      Attackable npc = this.getActiveChar();
      ((MonsterInstance)npc).enableMinions(HellboundManager.getInstance().getLevel() < 5);
      ((MonsterInstance)npc).setOnKillDelay(1000);
      super.onEvtSpawn();
   }

   @Override
   protected void onEvtDead(Creature killer) {
      Attackable npc = this.getActiveChar();
      MinionList ml = npc.getMinionList();
      if (ml != null && ml.hasAliveMinions()) {
         for(MinionInstance slave : ml.getAliveMinions()) {
            if (slave != null && !slave.isDead()) {
               slave.clearAggroList();
               slave.abortAttack();
               slave.abortCast();
               slave.broadcastPacket(new NpcSay(slave.getObjectId(), 22, slave.getId(), NpcStringId.THANK_YOU_FOR_SAVING_ME_FROM_THE_CLUTCHES_OF_EVIL), 2000);
               if (HellboundManager.getInstance().getLevel() >= 1 && HellboundManager.getInstance().getLevel() <= 2) {
                  HellboundManager.getInstance().updateTrust(10, false);
               }

               slave.getAI().setIntention(CtrlIntention.MOVING, new Location(-25451, 252291, -3252, 3500));
               DecayTaskManager.getInstance().add(slave);
            }
         }
      }

      super.onEvtDead(killer);
   }
}
