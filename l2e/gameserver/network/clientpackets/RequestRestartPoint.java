package l2e.gameserver.network.clientpackets;

import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.instancemanager.CHSiegeManager;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.ClanHallManager;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.instancemanager.MapRegionManager;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.SiegeClan;
import l2e.gameserver.model.TeleportWhereType;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.SiegeFlagInstance;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.model.entity.clanhall.SiegableHall;

public final class RequestRestartPoint extends GameClientPacket {
   protected int _requestedPointType;
   protected int _requestedPointItemId = 0;
   protected boolean _continuation;

   @Override
   protected void readImpl() {
      this._requestedPointType = this.readD();
      if (this._buf.hasRemaining()) {
         this._requestedPointItemId = this.readD();
      }
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         if (activeChar.canRevive()) {
            if (activeChar.isFakeDeathNow()) {
               activeChar.stopFakeDeath(true);
            } else if (!activeChar.isDead()) {
               if (Config.DEBUG) {
                  _log.warning("Living player [" + activeChar.getName() + "] called RestartPointPacket! Ban this player!");
               }
            } else {
               Castle castle = CastleManager.getInstance().getCastle(activeChar.getX(), activeChar.getY(), activeChar.getZ());
               if (castle != null
                  && castle.getSiege().getIsInProgress()
                  && activeChar.getClan() != null
                  && castle.getSiege().checkIsAttacker(activeChar.getClan())) {
                  ThreadPoolManager.getInstance().schedule(new RequestRestartPoint.DeathTask(activeChar), (long)castle.getSiege().getAttackerRespawnDelay());
                  if (castle.getSiege().getAttackerRespawnDelay() > 0) {
                     activeChar.sendMessage("You will be re-spawned in " + castle.getSiege().getAttackerRespawnDelay() / 1000 + " seconds");
                  }
               } else {
                  this.portPlayer(activeChar);
               }
            }
         }
      }
   }

   protected final void portPlayer(Player activeChar) {
      Location loc = null;
      Castle castle = null;
      Fort fort = null;
      SiegableHall hall = null;
      boolean isInDefense = false;
      int instanceId = 0;
      if (activeChar.getIsInKrateisCube()) {
         this._requestedPointType = 30;
      }

      if (activeChar.isJailed()) {
         this._requestedPointType = 27;
      } else if (activeChar.isFestivalParticipant()) {
         this._requestedPointType = 5;
      }

      switch(this._requestedPointType) {
         case 1:
            if (activeChar.getClan() == null || activeChar.getClan().getHideoutId() == 0) {
               if (Config.DEBUG) {
                  _log.warning("Player [" + activeChar.getName() + "] called RestartPointPacket - To Clanhall and he doesn't have Clanhall!");
               }

               return;
            }

            loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.CLANHALL);
            if (ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan()) != null
               && ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan()).getFunction(5) != null) {
               activeChar.restoreExp((double)ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan()).getFunction(5).getLvl());
            }
            break;
         case 2:
            castle = CastleManager.getInstance().getCastle(activeChar);
            if (castle != null && castle.getSiege().getIsInProgress()) {
               if (castle.getSiege().checkIsDefender(activeChar.getClan())) {
                  loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.CASTLE);
               } else {
                  if (!castle.getSiege().checkIsAttacker(activeChar.getClan())) {
                     if (Config.DEBUG) {
                        _log.warning("Player [" + activeChar.getName() + "] called RestartPointPacket - To Castle and he doesn't have Castle!");
                     }

                     return;
                  }

                  loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.TOWN);
               }
            } else {
               if (activeChar.getClan() == null || activeChar.getClan().getCastleId() == 0) {
                  return;
               }

               loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.CASTLE);
            }

            if (CastleManager.getInstance().getCastleByOwner(activeChar.getClan()) != null
               && CastleManager.getInstance().getCastleByOwner(activeChar.getClan()).getFunction(4) != null) {
               activeChar.restoreExp((double)CastleManager.getInstance().getCastleByOwner(activeChar.getClan()).getFunction(4).getLvl());
            }
            break;
         case 3:
            if (activeChar.getClan() == null || activeChar.getClan().getFortId() == 0) {
               if (Config.DEBUG) {
                  _log.warning("Player [" + activeChar.getName() + "] called RestartPointPacket - To Fortress and he doesn't have Fortress!");
               }

               return;
            }

            loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.FORTRESS);
            if (FortManager.getInstance().getFortByOwner(activeChar.getClan()) != null
               && FortManager.getInstance().getFortByOwner(activeChar.getClan()).getFunction(4) != null) {
               activeChar.restoreExp((double)FortManager.getInstance().getFortByOwner(activeChar.getClan()).getFunction(4).getLvl());
            }
            break;
         case 4:
            SiegeClan siegeClan = null;
            castle = CastleManager.getInstance().getCastle(activeChar);
            fort = FortManager.getInstance().getFort(activeChar);
            hall = CHSiegeManager.getInstance().getNearbyClanHall(activeChar);
            SiegeFlagInstance flag = TerritoryWarManager.getInstance().getHQForClan(activeChar.getClan());
            if (flag == null) {
               flag = TerritoryWarManager.getInstance().getFlagForClan(activeChar.getClan());
            }

            if (castle != null && castle.getSiege().getIsInProgress()) {
               siegeClan = castle.getSiege().getAttackerClan(activeChar.getClan());
            } else if (fort != null && fort.getSiege().getIsInProgress()) {
               siegeClan = fort.getSiege().getAttackerClan(activeChar.getClan());
            } else if (hall != null && hall.isInSiege()) {
               siegeClan = hall.getSiege().getAttackerClan(activeChar.getClan());
            }

            if ((siegeClan == null || siegeClan.getFlag().isEmpty()) && flag == null) {
               if (hall == null || (loc = hall.getSiege().getInnerSpawnLoc(activeChar)) == null) {
                  if (Config.DEBUG) {
                     _log.warning("Player [" + activeChar.getName() + "] called RestartPointPacket - To Siege HQ and he doesn't have Siege HQ!");
                  }

                  return;
               }
               break;
            }

            loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.SIEGEFLAG);
            break;
         case 5:
            if (activeChar.getInventory().getItemByItemId(10649) != null) {
               activeChar.getInventory().destroyItemByItemId("RequestRestartPoint", 10649, 1L, null, null);
               activeChar.doRevive();
               return;
            }

            if (activeChar.getInventory().getItemByItemId(13300) != null) {
               activeChar.getInventory().destroyItemByItemId("RequestRestartPoint", 13300, 1L, null, null);
               activeChar.doRevive();
               return;
            }

            if (!activeChar.isGM() && !activeChar.isFestivalParticipant()) {
               if (Config.DEBUG) {
                  _log.warning("Player [" + activeChar.getName() + "] called RestartPointPacket - Fixed and he isn't festival participant!");
               }

               return;
            }

            if (activeChar.isGM()) {
               activeChar.doRevive(100.0);
            } else {
               instanceId = activeChar.getReflectionId();
               loc = new Location(activeChar);
            }
         case 6:
            break;
         case 27:
            if (!activeChar.isJailed()) {
               return;
            }

            loc = new Location(-114356, -249645, -2984);
            break;
         default:
            loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.TOWN);
            if (activeChar.isInFightEvent()) {
               activeChar.getFightEvent().requestRespawn(activeChar);
               return;
            }
      }

      if (loc != null) {
         activeChar.setReflectionId(instanceId);
         activeChar.setIsIn7sDungeon(false);
         activeChar.setIsPendingRevive(true);
         activeChar.teleToLocation(loc, true);
      }
   }

   class DeathTask implements Runnable {
      final Player activeChar;

      DeathTask(Player _activeChar) {
         this.activeChar = _activeChar;
      }

      @Override
      public void run() {
         RequestRestartPoint.this.portPlayer(this.activeChar);
      }
   }
}
