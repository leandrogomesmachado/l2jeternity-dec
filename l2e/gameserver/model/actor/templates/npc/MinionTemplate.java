package l2e.gameserver.model.actor.templates.npc;

public class MinionTemplate {
   private final int _minionId;
   private final int _minionAmount;

   public MinionTemplate(int minionId, int minionAmount) {
      this._minionId = minionId;
      this._minionAmount = minionAmount;
   }

   public int getMinionId() {
      return this._minionId;
   }

   public int getAmount() {
      return this._minionAmount;
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (o == null) {
         return false;
      } else if (o.getClass() != this.getClass()) {
         return false;
      } else {
         return ((MinionTemplate)o).getMinionId() == this.getMinionId();
      }
   }
}
