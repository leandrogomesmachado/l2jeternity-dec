package l2e.gameserver.ai.npc;

import l2e.gameserver.ai.DefaultAI;
import l2e.gameserver.model.actor.Attackable;

public class Ranger extends DefaultAI {
   public Ranger(Attackable actor) {
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
   public int getRatePHYS() {
      return 10;
   }

   @Override
   public int getRateDOT() {
      return 8;
   }

   @Override
   public int getRateDEBUFF() {
      return 5;
   }

   @Override
   public int getRateDAM() {
      return 5;
   }

   @Override
   public int getRateSTUN() {
      return 8;
   }

   @Override
   public int getRateBUFF() {
      return 5;
   }

   @Override
   public int getRateHEAL() {
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
      return 15;
   }
}
