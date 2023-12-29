package l2e.gameserver.model.zone.type;

import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.ZoneType;

public class LevelLimitZone extends ZoneType {
   private int _minLvl = 1;
   private int _maxLvl = 85;
   private Location _exitLoc = null;
   private boolean _enabled = false;

   public LevelLimitZone(int id) {
      super(id);
      this.addZoneId(ZoneId.LEVEL_LIMIT);
   }

   @Override
   public void setParameter(String name, String value) {
      if (name.equals("avaliableLvls")) {
         String[] propertySplit = value.split(",");
         if (propertySplit.length != 0) {
            this._minLvl = Integer.parseInt(propertySplit[0]);
            this._maxLvl = Integer.parseInt(propertySplit[1]);
         }
      } else if (name.equals("exitLocation")) {
         String[] propertySplit = value.split(",");
         if (propertySplit.length == 3) {
            this._exitLoc = new Location(Integer.parseInt(propertySplit[0]), Integer.parseInt(propertySplit[1]), Integer.parseInt(propertySplit[2]));
         }
      } else if (name.equals("default_enabled")) {
         this._enabled = Boolean.parseBoolean(value);
      } else {
         super.setParameter(name, value);
      }
   }

   @Override
   protected void onEnter(Creature character) {
      if (this._enabled && (character.getLevel() < this._minLvl || character.getLevel() > this._maxLvl)) {
         if (character.isSummon()) {
            ((Summon)character).unSummon(character.getActingPlayer());
         } else if (character.isPlayer() && !character.getActingPlayer().isGM() && this._exitLoc != null) {
            character.getActingPlayer().teleToLocation(this._exitLoc.getX(), this._exitLoc.getY(), this._exitLoc.getZ(), true);
         }
      }
   }

   @Override
   protected void onExit(Creature character) {
   }
}
