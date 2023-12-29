package l2e.gameserver.model.holders;

public class AdditionalItemHolder extends ItemHolder {
   private final boolean _allowed;

   public AdditionalItemHolder(int id, boolean allowed) {
      super(id, 0L);
      this._allowed = allowed;
   }

   public boolean isAllowedToUse() {
      return this._allowed;
   }
}
