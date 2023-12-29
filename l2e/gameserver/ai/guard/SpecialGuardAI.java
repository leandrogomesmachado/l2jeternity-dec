package l2e.gameserver.ai.guard;

import java.util.ArrayList;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.instance.DefenderInstance;

public final class SpecialGuardAI extends GuardAI {
   private final ArrayList<Integer> _allied = new ArrayList<>();

   public SpecialGuardAI(DefenderInstance character) {
      super(character);
   }

   public ArrayList<Integer> getAlly() {
      return this._allied;
   }

   @Override
   protected boolean checkAggression(Creature target) {
      return this._allied.contains(target.getObjectId()) ? false : super.checkAggression(target);
   }
}
