package l2e.gameserver;

import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.items.itemcontainer.ItemContainer;

public class CastleUpdater implements Runnable {
   protected static final Logger _log = Logger.getLogger(CastleUpdater.class.getName());
   private final Clan _clan;
   private int _runCount = 0;

   public CastleUpdater(Clan clan, int runCount) {
      this._clan = clan;
      this._runCount = runCount;
   }

   @Override
   public void run() {
      try {
         ItemContainer warehouse = this._clan.getWarehouse();
         if (warehouse != null && this._clan.getCastleId() > 0) {
            Castle castle = CastleManager.getInstance().getCastleById(this._clan.getCastleId());
            if (!Config.ALT_MANOR_SAVE_ALL_ACTIONS && this._runCount % Config.ALT_MANOR_SAVE_PERIOD_RATE == 0) {
               castle.saveSeedData();
               castle.saveCropData();
               if (Config.DEBUG) {
                  _log.info("Manor System: all data for " + castle.getName() + " saved");
               }
            }

            CastleUpdater cu = new CastleUpdater(this._clan, ++this._runCount);
            ThreadPoolManager.getInstance().schedule(cu, 3600000L);
         }
      } catch (Exception var4) {
         _log.log(Level.WARNING, "", (Throwable)var4);
      }
   }
}
