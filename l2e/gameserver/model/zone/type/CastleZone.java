package l2e.gameserver.model.zone.type;

import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.model.TeleportWhereType;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.ZoneRespawn;

public class CastleZone extends ZoneRespawn {
   private int _castleId;
   private Castle _castle = null;

   public CastleZone(int id) {
      super(id);
   }

   @Override
   public void setParameter(String name, String value) {
      if (name.equals("castleId")) {
         this._castleId = Integer.parseInt(value);
         this._castle = CastleManager.getInstance().getCastleById(this._castleId);
         if (this._castle != null) {
            this.addZoneId(ZoneId.CASTLE);
         }
      } else {
         super.setParameter(name, value);
      }
   }

   @Override
   protected void onEnter(Creature character) {
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
      TeleportWhereType type = TeleportWhereType.TOWN;

      for(Player temp : this.getPlayersInside()) {
         if (temp.getClanId() != owningClanId || owningClanId == 0) {
            temp.teleToLocation(type, true);
         }
      }
   }

   public int getCastleId() {
      return this._castleId;
   }
}
