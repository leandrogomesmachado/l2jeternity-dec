package l2e.gameserver.model.zone.type;

import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.ZoneType;

public class HqZone extends ZoneType {
   private int _clanHallId = 0;
   private int _fortId = 0;
   private int _castleId = 0;
   private int _territoryId = 0;

   public HqZone(int id) {
      super(id);
      this.addZoneId(ZoneId.HQ);
   }

   @Override
   public void setParameter(String name, String value) {
      if ("castleId".equals(name)) {
         this._castleId = Integer.parseInt(value);
      } else if ("fortId".equals(name)) {
         this._fortId = Integer.parseInt(value);
      } else if ("clanHallId".equals(name)) {
         this._clanHallId = Integer.parseInt(value);
      } else if ("territoryId".equals(name)) {
         this._territoryId = Integer.parseInt(value);
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

   public int getCastleId() {
      return this._castleId;
   }

   public int getFortId() {
      return this._fortId;
   }

   public int getClanHallId() {
      return this._clanHallId;
   }

   public int getTerritoryId() {
      return this._territoryId;
   }
}
