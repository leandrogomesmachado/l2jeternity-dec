package l2e.gameserver.ai.npc;

import l2e.gameserver.ai.DefaultAI;
import l2e.gameserver.model.actor.Attackable;

public class Fighter extends DefaultAI {
   public Fighter(Attackable actor) {
      super(actor);
   }

   @Override
   protected boolean thinkActive() {
      return super.thinkActive() || this.defaultThinkBuff(2);
   }

   @Override
   protected boolean createNewTask() {
      return this.defaultFightTask();
   }

   @Override
   protected int getRatePHYS() {
      return 10;
   }

   @Override
   protected int getRateDOT() {
      return 8;
   }

   @Override
   protected int getRateDEBUFF() {
      return 5;
   }

   @Override
   protected int getRateDAM() {
      return 5;
   }

   @Override
   protected int getRateSTUN() {
      return 8;
   }

   @Override
   protected int getRateBUFF() {
      return 5;
   }

   @Override
   protected int getRateHEAL() {
      return 5;
   }

   @Override
   protected int getRateSuicide() {
      return 3;
   }

   @Override
   protected int getRateRes() {
      return 2;
   }

   @Override
   protected int getRateDodge() {
      return 0;
   }
}
