package l2e.gameserver.model.actor.templates;

public final class CropProcureTemplate {
   final int _cropId;
   long _buyResidual;
   final int _rewardType;
   final long _buy;
   final long _price;

   public CropProcureTemplate(int id) {
      this._cropId = id;
      this._buyResidual = 0L;
      this._rewardType = 0;
      this._buy = 0L;
      this._price = 0L;
   }

   public CropProcureTemplate(int id, long amount, int type, long buy, long price) {
      this._cropId = id;
      this._buyResidual = amount;
      this._rewardType = type;
      this._buy = buy;
      this._price = price;
   }

   public int getReward() {
      return this._rewardType;
   }

   public int getId() {
      return this._cropId;
   }

   public long getAmount() {
      return this._buyResidual;
   }

   public long getStartAmount() {
      return this._buy;
   }

   public long getPrice() {
      return this._price;
   }

   public void setAmount(long amount) {
      this._buyResidual = amount;
   }
}
