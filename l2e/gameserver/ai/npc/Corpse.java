package l2e.gameserver.ai.npc;

import l2e.gameserver.ai.DefaultAI;
import l2e.gameserver.model.actor.Attackable;

public class Corpse extends DefaultAI {
   public Corpse(Attackable actor) {
      super(actor);
   }

   @Override
   protected boolean thinkActive() {
      return super.thinkActive();
   }

   @Override
   public int getRatePHYS() {
      return 0;
   }

   @Override
   public int getRateDOT() {
      return 0;
   }

   @Override
   public int getRateDEBUFF() {
      return 0;
   }

   @Override
   public int getRateDAM() {
      return 0;
   }

   @Override
   public int getRateSTUN() {
      return 0;
   }

   @Override
   public int getRateBUFF() {
      return 0;
   }

   @Override
   public int getRateHEAL() {
      return 0;
   }

   @Override
   protected int getRateSuicide() {
      return 0;
   }

   @Override
   protected int getRateRes() {
      return 0;
   }

   @Override
   protected int getRateDodge() {
      return 0;
   }
}
