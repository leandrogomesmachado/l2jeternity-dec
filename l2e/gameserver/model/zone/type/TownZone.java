package l2e.gameserver.model.zone.type;

import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.ZoneType;

public class TownZone extends ZoneType {
   private int _townId;
   private int _taxById = 0;

   public TownZone(int id) {
      super(id);
      this.addZoneId(ZoneId.TOWN);
   }

   @Override
   public void setParameter(String name, String value) {
      if (name.equals("townId")) {
         this._townId = Integer.parseInt(value);
      } else if (name.equals("taxById")) {
         this._taxById = Integer.parseInt(value);
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

   public void updateForCharactersInside() {
      for(Creature character : this._characterList.values()) {
         if (character != null) {
            this.onEnter(character);
         }
      }
   }

   public int getTownId() {
      return this._townId;
   }

   public final int getTaxById() {
      return this._taxById;
   }
}
