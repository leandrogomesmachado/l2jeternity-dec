package l2e.gameserver.model.zone.type;

import l2e.gameserver.instancemanager.ClanHallManager;
import l2e.gameserver.model.TeleportWhereType;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.ClanHall;
import l2e.gameserver.model.entity.clanhall.AuctionableHall;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.ZoneRespawn;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.AgitDecoInfo;

public class ClanHallZone extends ZoneRespawn {
   private int _clanHallId;

   public ClanHallZone(int id) {
      super(id);
      this.addZoneId(ZoneId.CLAN_HALL);
   }

   @Override
   public void setParameter(String name, String value) {
      if (name.equals("clanHallId")) {
         this._clanHallId = Integer.parseInt(value);
         ClanHall hall = ClanHallManager.getInstance().getClanHallById(this._clanHallId);
         if (hall == null) {
            _log.warning("ClanHallZone: Clan hall with id " + this._clanHallId + " does not exist!");
         } else {
            hall.setZone(this);
         }
      } else {
         super.setParameter(name, value);
      }
   }

   @Override
   protected void onEnter(Creature character) {
      if (character.isPlayer()) {
         AuctionableHall clanHall = ClanHallManager.getInstance().getAuctionableHallById(this._clanHallId);
         if (clanHall == null) {
            return;
         }

         AgitDecoInfo deco = new AgitDecoInfo(clanHall);
         character.sendPacket(deco);
      }
   }

   @Override
   protected void onExit(Creature character) {
   }

   @Override
   public void onDieInside(Creature character) {
   }

   @Override
   public void onReviveInside(Creature character) {
   }

   public void banishForeigners(int owningClanId) {
      TeleportWhereType type = TeleportWhereType.CLANHALL_BANISH;

      for(Player temp : this.getPlayersInside()) {
         if (temp.getClanId() != owningClanId || owningClanId == 0) {
            temp.teleToLocation(type, true);
         }
      }
   }

   public int getClanHallId() {
      return this._clanHallId;
   }

   public void updateSiegeStatus() {
      if (this._clanHallId == 35) {
         for(Creature character : this._characterList.values()) {
            try {
               this.onEnter(character);
            } catch (Exception var5) {
            }
         }
      } else {
         this.getZoneId().clear();

         for(Creature character : this._characterList.values()) {
            try {
               if (character.isPlayer()) {
                  character.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
               }
            } catch (Exception var4) {
            }
         }
      }
   }
}
