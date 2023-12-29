package l2e.scripts.ai.freya;

import l2e.commons.threading.RunnableImpl;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;

public class Glacier extends Fighter {
   public Glacier(Attackable actor) {
      super(actor);
      actor.block();
   }

   @Override
   protected void onEvtSpawn() {
      super.onEvtSpawn();
      this.getActiveChar().setDisplayEffect(1);
      ThreadPoolManager.getInstance().schedule(new Glacier.Freeze(), 800L);
      ThreadPoolManager.getInstance().schedule(new Glacier.Despawn(), 30000L);
   }

   @Override
   protected void onEvtDead(Creature killer) {
      for(Player cha : World.getInstance().getAroundPlayers(this.getActiveChar(), 350, 200)) {
         cha.makeTriggerCast(SkillsParser.getInstance().getInfo(6301, 1), cha);
      }

      super.onEvtDead(killer);
   }

   private class Despawn extends RunnableImpl {
      private Despawn() {
      }

      @Override
      public void runImpl() {
         Glacier.this.getActor().deleteMe();
      }
   }

   private class Freeze extends RunnableImpl {
      private Freeze() {
      }

      @Override
      public void runImpl() {
         Glacier.this.getActiveChar().setDisplayEffect(2);
      }
   }
}
