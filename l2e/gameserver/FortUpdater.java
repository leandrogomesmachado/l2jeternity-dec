package l2e.gameserver;

import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.entity.Fort;

public class FortUpdater implements Runnable {
   protected static Logger _log = Logger.getLogger(FortUpdater.class.getName());
   private final Clan _clan;
   private final Fort _fort;
   private int _runCount;
   private final FortUpdater.UpdaterType _updaterType;

   public FortUpdater(Fort fort, Clan clan, int runCount, FortUpdater.UpdaterType ut) {
      this._fort = fort;
      this._clan = clan;
      this._runCount = runCount;
      this._updaterType = ut;
   }

   @Override
   public void run() {
      try {
         switch(this._updaterType) {
            case PERIODIC_UPDATE:
               ++this._runCount;
               if (this._fort.getOwnerClan() != null && this._fort.getOwnerClan() == this._clan) {
                  this._fort.getOwnerClan().increaseBloodOathCount();
                  if (this._fort.getFortState() == 2) {
                     if (this._clan.getWarehouse().getAdena() >= (long)Config.FS_FEE_FOR_CASTLE) {
                        this._clan.getWarehouse().destroyItemByItemId("FS_fee_for_Castle", 57, (long)Config.FS_FEE_FOR_CASTLE, null, null);
                        this._fort.getContractedCastle().addToTreasuryNoTax((long)Config.FS_FEE_FOR_CASTLE);
                        this._fort.raiseSupplyLvL();
                     } else {
                        this._fort.setFortState(1, 0);
                     }
                  }

                  this._fort.saveFortVariables();
                  break;
               }

               return;
            case MAX_OWN_TIME:
               if (this._fort.getOwnerClan() == null || this._fort.getOwnerClan() != this._clan) {
                  return;
               }

               if (this._fort.getOwnedTime() > Config.FS_MAX_OWN_TIME * 3600) {
                  this._fort.removeOwner(true);
                  this._fort.setFortState(0, 0);
               }
         }
      } catch (Exception var2) {
         _log.log(Level.WARNING, "", (Throwable)var2);
      }
   }

   public int getRunCount() {
      return this._runCount;
   }

   public static enum UpdaterType {
      MAX_OWN_TIME,
      PERIODIC_UPDATE;
   }
}
