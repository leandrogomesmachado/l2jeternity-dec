package l2e.gameserver.ai.npc;

import l2e.gameserver.ai.DefaultAI;
import l2e.gameserver.model.actor.Attackable;

public class Priest extends DefaultAI {
   public Priest(Attackable actor) {
      super(actor);
   }

   @Override
   protected boolean thinkActive() {
      return super.thinkActive() || this.defaultThinkBuff(10, 5);
   }

   @Override
   protected boolean createNewTask() {
      return this.defaultFightTask();
   }

   @Override
   public int getRatePHYS() {
      return 10;
   }

   @Override
   public int getRateDOT() {
      return 15;
   }

   @Override
   public int getRateDEBUFF() {
      return 15;
   }

   @Override
   public int getRateDAM() {
      return 30;
   }

   @Override
   public int getRateSTUN() {
      return 3;
   }

   @Override
   public int getRateBUFF() {
      return 10;
   }

   @Override
   public int getRateHEAL() {
      return 40;
   }

   @Override
   protected int getRateSuicide() {
      return 3;
   }

   @Override
   protected int getRateRes() {
      return 50;
   }

   @Override
   protected int getRateDodge() {
      return 0;
   }
}
