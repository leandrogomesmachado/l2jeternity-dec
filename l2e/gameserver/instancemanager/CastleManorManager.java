package l2e.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.data.parser.ManorParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.CropProcureTemplate;
import l2e.gameserver.model.actor.templates.SeedTemplate;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.items.itemcontainer.ClanWarehouse;
import l2e.gameserver.model.items.itemcontainer.ItemContainer;
import l2e.gameserver.network.SystemMessageId;

public final class CastleManorManager {
   protected static final Logger _log = Logger.getLogger(CastleManorManager.class.getName());
   public static final int PERIOD_CURRENT = 0;
   public static final int PERIOD_NEXT = 1;
   private static final String CASTLE_MANOR_LOAD_PROCURE = "SELECT * FROM castle_manor_procure WHERE castle_id=?";
   private static final String CASTLE_MANOR_LOAD_PRODUCTION = "SELECT * FROM castle_manor_production WHERE castle_id=?";
   private static final int NEXT_PERIOD_APPROVE = Config.ALT_MANOR_APPROVE_TIME;
   private static final int NEXT_PERIOD_APPROVE_MIN = Config.ALT_MANOR_APPROVE_MIN;
   private static final int MANOR_REFRESH = Config.ALT_MANOR_REFRESH_TIME;
   private static final int MANOR_REFRESH_MIN = Config.ALT_MANOR_REFRESH_MIN;
   protected static final long MAINTENANCE_PERIOD = (long)Config.ALT_MANOR_MAINTENANCE_PERIOD;
   private Calendar _manorRefresh;
   private Calendar _periodApprove;
   private boolean _underMaintenance;
   private boolean _disabled;
   protected ScheduledFuture<?> _scheduledManorRefresh;
   protected ScheduledFuture<?> _scheduledMaintenanceEnd;
   protected ScheduledFuture<?> _scheduledNextPeriodapprove;

   public static final CastleManorManager getInstance() {
      return CastleManorManager.SingletonHolder._instance;
   }

   protected CastleManorManager() {
      this.load();
      this.init();
      this._underMaintenance = false;
      this._disabled = !Config.ALLOW_MANOR;
      boolean isApproved;
      if (this._periodApprove.getTimeInMillis() > this._manorRefresh.getTimeInMillis()) {
         isApproved = this._manorRefresh.getTimeInMillis() > Calendar.getInstance().getTimeInMillis();
      } else {
         isApproved = this._periodApprove.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()
            && this._manorRefresh.getTimeInMillis() > Calendar.getInstance().getTimeInMillis();
      }

      for(Castle c : CastleManager.getInstance().getCastles()) {
         c.setNextPeriodApproved(isApproved);
      }
   }

   private void load() {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statementProduction = con.prepareStatement("SELECT * FROM castle_manor_production WHERE castle_id=?");
         PreparedStatement statementProcure = con.prepareStatement("SELECT * FROM castle_manor_procure WHERE castle_id=?");

         for(Castle castle : CastleManager.getInstance().getCastles()) {
            List<SeedTemplate> production = new ArrayList<>();
            List<SeedTemplate> productionNext = new ArrayList<>();
            List<CropProcureTemplate> procure = new ArrayList<>();
            List<CropProcureTemplate> procureNext = new ArrayList<>();
            statementProduction.setInt(1, castle.getId());
            ResultSet rs = statementProduction.executeQuery();
            statementProduction.clearParameters();

            while(rs.next()) {
               int seedId = rs.getInt("seed_id");
               int canProduce = rs.getInt("can_produce");
               int startProduce = rs.getInt("start_produce");
               int price = rs.getInt("seed_price");
               int period = rs.getInt("period");
               if (period == 0) {
                  production.add(new SeedTemplate(seedId, (long)canProduce, (long)price, (long)startProduce));
               } else {
                  productionNext.add(new SeedTemplate(seedId, (long)canProduce, (long)price, (long)startProduce));
               }
            }

            rs.close();
            castle.setSeedProduction(production, 0);
            castle.setSeedProduction(productionNext, 1);
            statementProcure.setInt(1, castle.getId());
            rs = statementProcure.executeQuery();
            statementProcure.clearParameters();

            while(rs.next()) {
               int cropId = rs.getInt("crop_id");
               int canBuy = rs.getInt("can_buy");
               int startBuy = rs.getInt("start_buy");
               int rewardType = rs.getInt("reward_type");
               int price = rs.getInt("price");
               int period = rs.getInt("period");
               if (period == 0) {
                  procure.add(new CropProcureTemplate(cropId, (long)canBuy, rewardType, (long)startBuy, (long)price));
               } else {
                  procureNext.add(new CropProcureTemplate(cropId, (long)canBuy, rewardType, (long)startBuy, (long)price));
               }
            }

            rs.close();
            castle.setCropProcure(procure, 0);
            castle.setCropProcure(procureNext, 1);
            if (!procure.isEmpty() || !procureNext.isEmpty() || !production.isEmpty() || !productionNext.isEmpty()) {
               _log.info(this.getClass().getSimpleName() + ": " + castle.getName() + ": Data loaded");
            }
         }

         statementProduction.close();
         statementProcure.close();
      } catch (Exception var28) {
         _log.info(this.getClass().getSimpleName() + ": Error restoring manor data: " + var28.getMessage());
      }
   }

   private void init() {
      this._manorRefresh = Calendar.getInstance();
      this._manorRefresh.set(11, MANOR_REFRESH);
      this._manorRefresh.set(12, MANOR_REFRESH_MIN);
      this._periodApprove = Calendar.getInstance();
      this._periodApprove.set(11, NEXT_PERIOD_APPROVE);
      this._periodApprove.set(12, NEXT_PERIOD_APPROVE_MIN);
      this.updateManorRefresh();
      this.updatePeriodApprove();
   }

   public void updateManorRefresh() {
      this._scheduledManorRefresh = ThreadPoolManager.getInstance().schedule(new Runnable() {
         @Override
         public void run() {
            if (!CastleManorManager.this.isDisabled()) {
               CastleManorManager.this.setUnderMaintenance(true);
               CastleManorManager._log.info(this.getClass().getSimpleName() + ": Under maintenance mode started");
               CastleManorManager.this._scheduledMaintenanceEnd = ThreadPoolManager.getInstance().schedule(new Runnable() {
                  @Override
                  public void run() {
                     CastleManorManager._log.info(this.getClass().getSimpleName() + ": Next period started");
                     CastleManorManager.this.setNextPeriod();

                     try {
                        CastleManorManager.this.save();
                     } catch (Exception var2) {
                        CastleManorManager._log.log(Level.WARNING, "Manor System: Failed to save manor data: " + var2.getMessage(), (Throwable)var2);
                     }

                     CastleManorManager.this.setUnderMaintenance(false);
                  }
               }, CastleManorManager.MAINTENANCE_PERIOD);
            }

            CastleManorManager.this.updateManorRefresh();
         }
      }, this.getMillisToManorRefresh());
   }

   public void updatePeriodApprove() {
      this._scheduledNextPeriodapprove = ThreadPoolManager.getInstance().schedule(new Runnable() {
         @Override
         public void run() {
            if (!CastleManorManager.this.isDisabled()) {
               CastleManorManager.this.approveNextPeriod();
               CastleManorManager._log.info(this.getClass().getSimpleName() + ": Next period approved");
            }

            CastleManorManager.this.updatePeriodApprove();
         }
      }, this.getMillisToNextPeriodApprove());
   }

   public long getMillisToManorRefresh() {
      if (this._manorRefresh.getTimeInMillis() - Calendar.getInstance().getTimeInMillis() < 120000L) {
         this.setNewManorRefresh();
      }

      _log.info(this.getClass().getSimpleName() + ": Manor refresh at " + this._manorRefresh.getTime());
      return this._manorRefresh.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
   }

   public void setNewManorRefresh() {
      this._manorRefresh = Calendar.getInstance();
      this._manorRefresh.set(11, MANOR_REFRESH);
      this._manorRefresh.set(12, MANOR_REFRESH_MIN);
      this._manorRefresh.set(13, 0);
      this._manorRefresh.add(11, 24);
   }

   public long getMillisToNextPeriodApprove() {
      if (this._periodApprove.getTimeInMillis() - Calendar.getInstance().getTimeInMillis() < 120000L) {
         this.setNewPeriodApprove();
      }

      _log.info(this.getClass().getSimpleName() + ": Period approve at " + this._periodApprove.getTime());
      return this._periodApprove.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
   }

   public void setNewPeriodApprove() {
      this._periodApprove = Calendar.getInstance();
      this._periodApprove.set(11, NEXT_PERIOD_APPROVE);
      this._periodApprove.set(12, NEXT_PERIOD_APPROVE_MIN);
      this._periodApprove.set(13, 0);
      this._periodApprove.add(11, 24);
   }

   public void setNextPeriod() {
      for(Castle c : CastleManager.getInstance().getCastles()) {
         if (c.getOwnerId() > 0) {
            Clan clan = ClanHolder.getInstance().getClan(c.getOwnerId());
            if (clan != null) {
               ItemContainer cwh = clan.getWarehouse();
               if (!(cwh instanceof ClanWarehouse)) {
                  _log.info(this.getClass().getSimpleName() + ": Can't get clan warehouse for clan " + ClanHolder.getInstance().getClan(c.getOwnerId()));
               } else {
                  for(CropProcureTemplate crop : c.getCropProcure(0)) {
                     if (crop.getStartAmount() != 0L) {
                        if (crop.getStartAmount() - crop.getAmount() > 0L) {
                           long count = crop.getStartAmount() - crop.getAmount();
                           count = count * 90L / 100L;
                           if (count < 1L && Rnd.nextInt(99) < 90) {
                              count = 1L;
                           }

                           if (count > 0L) {
                              cwh.addItem("Manor", ManorParser.getInstance().getMatureCrop(crop.getId()), count, null, null);
                           }
                        }

                        if (crop.getAmount() > 0L) {
                           c.addToTreasuryNoTax(crop.getAmount() * crop.getPrice());
                        }
                     }
                  }

                  c.setSeedProduction(c.getSeedProduction(1), 0);
                  c.setCropProcure(c.getCropProcure(1), 0);
                  if (c.getTreasury() < c.getManorCost(0)) {
                     c.setSeedProduction(this.getNewSeedsList(c.getId()), 1);
                     c.setCropProcure(this.getNewCropsList(c.getId()), 1);
                  } else {
                     List<SeedTemplate> production = new ArrayList<>();

                     for(SeedTemplate s : c.getSeedProduction(0)) {
                        s.setCanProduce(s.getStartProduce());
                        production.add(s);
                     }

                     c.setSeedProduction(production, 1);
                     List<CropProcureTemplate> procure = new ArrayList<>();

                     for(CropProcureTemplate cr : c.getCropProcure(0)) {
                        cr.setAmount(cr.getStartAmount());
                        procure.add(cr);
                     }

                     c.setCropProcure(procure, 1);
                  }

                  if (Config.ALT_MANOR_SAVE_ALL_ACTIONS) {
                     c.saveCropData();
                     c.saveSeedData();
                  }

                  Player clanLeader = null;
                  clanLeader = World.getInstance().getPlayer(clan.getLeader().getName());
                  if (clanLeader != null) {
                     clanLeader.sendPacket(SystemMessageId.THE_MANOR_INFORMATION_HAS_BEEN_UPDATED);
                  }

                  c.setNextPeriodApproved(false);
               }
            }
         }
      }
   }

   public void approveNextPeriod() {
      for(Castle c : CastleManager.getInstance().getCastles()) {
         boolean notFunc = false;
         if (c.getOwnerId() <= 0) {
            c.setCropProcure(new ArrayList<>(), 1);
            c.setSeedProduction(new ArrayList<>(), 1);
         } else if (c.getTreasury() < c.getManorCost(1)) {
            notFunc = true;
            _log.info(
               this.getClass().getSimpleName()
                  + ": Manor for castle "
                  + c.getName()
                  + " disabled, not enough adena in treasury: "
                  + c.getTreasury()
                  + ", "
                  + c.getManorCost(1)
                  + " required."
            );
            c.setSeedProduction(this.getNewSeedsList(c.getId()), 1);
            c.setCropProcure(this.getNewCropsList(c.getId()), 1);
         } else {
            ItemContainer cwh = ClanHolder.getInstance().getClan(c.getOwnerId()).getWarehouse();
            if (!(cwh instanceof ClanWarehouse)) {
               _log.info(this.getClass().getSimpleName() + ": Can't get clan warehouse for clan " + ClanHolder.getInstance().getClan(c.getOwnerId()));
               continue;
            }

            int slots = 0;

            for(CropProcureTemplate crop : c.getCropProcure(1)) {
               if (crop.getStartAmount() > 0L && cwh.getItemByItemId(ManorParser.getInstance().getMatureCrop(crop.getId())) == null) {
                  ++slots;
               }
            }

            if (!cwh.validateCapacity((long)slots)) {
               notFunc = true;
               _log.info(
                  this.getClass().getSimpleName()
                     + ": Manor for castle "
                     + c.getName()
                     + " disabled, not enough free slots in clan warehouse: "
                     + (Config.WAREHOUSE_SLOTS_CLAN - cwh.getSize())
                     + ", but "
                     + slots
                     + " required."
               );
               c.setSeedProduction(this.getNewSeedsList(c.getId()), 1);
               c.setCropProcure(this.getNewCropsList(c.getId()), 1);
            }
         }

         c.setNextPeriodApproved(true);
         c.addToTreasuryNoTax(-1L * c.getManorCost(1));
         if (notFunc) {
            Clan clan = ClanHolder.getInstance().getClan(c.getOwnerId());
            Player clanLeader = null;
            if (clan != null) {
               clanLeader = World.getInstance().getPlayer(clan.getLeaderId());
            }

            if (clanLeader != null) {
               clanLeader.sendPacket(SystemMessageId.THE_AMOUNT_IS_NOT_SUFFICIENT_AND_SO_THE_MANOR_IS_NOT_IN_OPERATION);
            }
         }
      }
   }

   private List<SeedTemplate> getNewSeedsList(int castleId) {
      List<SeedTemplate> seeds = new ArrayList<>();

      for(int sd : ManorParser.getInstance().getSeedsForCastle(castleId)) {
         seeds.add(new SeedTemplate(sd));
      }

      return seeds;
   }

   private List<CropProcureTemplate> getNewCropsList(int castleId) {
      List<CropProcureTemplate> crops = new ArrayList<>();

      for(int cr : ManorParser.getInstance().getCropsForCastle(castleId)) {
         crops.add(new CropProcureTemplate(cr));
      }

      return crops;
   }

   public boolean isUnderMaintenance() {
      return this._underMaintenance;
   }

   public void setUnderMaintenance(boolean mode) {
      this._underMaintenance = mode;
   }

   public boolean isDisabled() {
      return this._disabled;
   }

   public void setDisabled(boolean mode) {
      this._disabled = mode;
   }

   public SeedTemplate getNewSeedProduction(int id, long amount, long price, long sales) {
      return new SeedTemplate(id, amount, price, sales);
   }

   public CropProcureTemplate getNewCropProcure(int id, long amount, int type, long price, long buy) {
      return new CropProcureTemplate(id, amount, type, buy, price);
   }

   public void save() {
      for(Castle c : CastleManager.getInstance().getCastles()) {
         c.saveSeedData();
         c.saveCropData();
      }
   }

   private static class SingletonHolder {
      protected static final CastleManorManager _instance = new CastleManorManager();
   }
}
