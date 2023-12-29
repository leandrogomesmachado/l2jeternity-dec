package l2e.fake.ai;

import l2e.fake.FakePlayer;

public class FallbackAI extends FakePlayerAI {
   public FallbackAI(FakePlayer character) {
      super(character, true);
   }

   @Override
   public void thinkAndAct() {
   }

   @Override
   protected int[][] getBuffs() {
      return new int[0][0];
   }
}
