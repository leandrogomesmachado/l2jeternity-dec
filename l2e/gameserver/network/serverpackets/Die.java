package l2e.gameserver.network.serverpackets;

import l2e.gameserver.data.parser.AdminParser;
import l2e.gameserver.instancemanager.CHSiegeManager;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.model.AccessLevel;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.SiegeClan;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.model.entity.clanhall.SiegableHall;
import l2e.gameserver.model.entity.events.cleft.AerialCleftEvent;

public class Die extends GameServerPacket {
   private final int _charObjId;
   private boolean _canTeleport;
   private final boolean _sweepable;
   private AccessLevel _access = AdminParser.getInstance().getAccessLevel(0);
   private boolean _haveItem = false;
   private Clan _clan;
   private final Creature _activeChar;
   private boolean _isJailed;

   public Die(Creature cha) {
      this._charObjId = cha.getObjectId();
      this._activeChar = cha;
      this._canTeleport = cha.canRevive() && !cha.isPendingRevive();
      this._sweepable = cha.isAttackable() && cha.isSweepActive();
      if (cha.isPlayer()) {
         Player player = cha.getActingPlayer();
         this._access = player.getAccessLevel();
         this._clan = player.getClan();
         this._haveItem = player.getInventory().getItemByItemId(13300) != null;
         this._isJailed = player.isJailed();
         if ((AerialCleftEvent.getInstance().isStarted() || AerialCleftEvent.getInstance().isRewarding())
            && AerialCleftEvent.getInstance().isPlayerParticipant(player.getObjectId())) {
            this._canTeleport = false;
         }

         if (player.isInFightEvent() || player.getUCState() > 0) {
            this._canTeleport = false;
         }
      }
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._charObjId);
      this.writeD(this._canTeleport ? 1 : 0);
      if (this._canTeleport && this._clan != null && !this._isJailed) {
         boolean isInCastleDefense = false;
         boolean isInFortDefense = false;
         SiegeClan siegeClan = null;
         Castle castle = CastleManager.getInstance().getCastle(this._activeChar);
         Fort fort = FortManager.getInstance().getFort(this._activeChar);
         SiegableHall hall = CHSiegeManager.getInstance().getNearbyClanHall(this._activeChar);
         if (castle != null && castle.getSiege().getIsInProgress()) {
            siegeClan = castle.getSiege().getAttackerClan(this._clan);
            if (siegeClan == null && castle.getSiege().checkIsDefender(this._clan)) {
               isInCastleDefense = true;
            }
         } else if (fort != null && fort.getSiege().getIsInProgress()) {
            siegeClan = fort.getSiege().getAttackerClan(this._clan);
            if (siegeClan == null && fort.getSiege().checkIsDefender(this._clan)) {
               isInFortDefense = true;
            }
         }

         this.writeD(this._clan.getHideoutId() > 0 ? 1 : 0);
         this.writeD(this._clan.getCastleId() <= 0 && !isInCastleDefense ? 0 : 1);
         this.writeD(
            TerritoryWarManager.getInstance().getHQForClan(this._clan) == null
                  && TerritoryWarManager.getInstance().getFlagForClan(this._clan) == null
                  && (siegeClan == null || isInCastleDefense || isInFortDefense || siegeClan.getFlag().isEmpty())
                  && (hall == null || !hall.getSiege().checkIsAttacker(this._clan))
               ? 0
               : 1
         );
         this.writeD(this._sweepable ? 1 : 0);
         this.writeD(!this._access.allowFixedRes() && !this._haveItem ? 0 : 1);
         this.writeD(this._clan.getFortId() <= 0 && !isInFortDefense ? 0 : 1);
      } else {
         this.writeD(0);
         this.writeD(0);
         this.writeD(0);
         this.writeD(this._sweepable ? 1 : 0);
         this.writeD(!this._access.allowFixedRes() && !this._haveItem ? 0 : 1);
         this.writeD(0);
      }
   }
}
