package l2e.fake.ai.walker;

import l2e.commons.util.Rnd;
import l2e.fake.FakePlayer;
import l2e.fake.model.WalkNode;
import l2e.fake.model.WalkerType;

public class GiranWalkerAI extends WalkerAI {
   public GiranWalkerAI(FakePlayer character) {
      super(character);
   }

   @Override
   protected WalkerType getWalkerType() {
      return WalkerType.RANDOM;
   }

   @Override
   protected void setWalkNodes() {
      this._walkNodes.add(new WalkNode(82248, 148600, -3464, Rnd.get(1, 20)));
      this._walkNodes.add(new WalkNode(82072, 147560, -3464, Rnd.get(1, 20)));
      this._walkNodes.add(new WalkNode(82792, 147832, -3464, Rnd.get(1, 20)));
      this._walkNodes.add(new WalkNode(83320, 147976, -3400, Rnd.get(1, 20)));
      this._walkNodes.add(new WalkNode(84584, 148536, -3400, Rnd.get(1, 20)));
      this._walkNodes.add(new WalkNode(83384, 149256, -3400, Rnd.get(1, 20)));
      this._walkNodes.add(new WalkNode(83064, 148392, -3464, Rnd.get(1, 20)));
      this._walkNodes.add(new WalkNode(87016, 148632, -3400, Rnd.get(1, 20)));
      this._walkNodes.add(new WalkNode(85816, 148872, -3400, Rnd.get(1, 20)));
      this._walkNodes.add(new WalkNode(85832, 153208, -3496, Rnd.get(1, 20)));
      this._walkNodes.add(new WalkNode(81384, 150040, -3528, Rnd.get(1, 20)));
      this._walkNodes.add(new WalkNode(79656, 150728, -3512, Rnd.get(1, 20)));
      this._walkNodes.add(new WalkNode(79272, 149544, -3528, Rnd.get(1, 20)));
      this._walkNodes.add(new WalkNode(80744, 146424, -3528, Rnd.get(1, 20)));
   }
}
