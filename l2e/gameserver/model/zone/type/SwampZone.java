package l2e.gameserver.model.zone.type;

import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.ZoneType;

public class SwampZone extends ZoneType {
   private double _moveBonus;
   private int _castleId;
   private Castle _castle;

   public SwampZone(int id) {
      super(id);
      this.addZoneId(ZoneId.SWAMP);
      this._moveBonus = 0.5;
      this._castleId = 0;
      this._castle = null;
   }

   @Override
   public void setParameter(String name, String value) {
      if (name.equals("move_bonus")) {
         this._moveBonus = Double.parseDouble(value);
      } else if (name.equals("castleId")) {
         this._castleId = Integer.parseInt(value);
      } else {
         super.setParameter(name, value);
      }
   }

   private Castle getCastle() {
      if (this._castleId > 0 && this._castle == null) {
         this._castle = CastleManager.getInstance().getCastleById(this._castleId);
      }

      return this._castle;
   }

   @Override
   protected void onEnter(Creature character) {
      if (this.getCastle() != null) {
         if (!this.getCastle().getSiege().getIsInProgress() || !this.getCastle().getSiege().isTrapsActive()) {
            return;
         }

         Player player = character.getActingPlayer();
         if (player != null && player.isInSiege() && player.getSiegeState() == 2) {
            return;
         }
      }

      if (character.isPlayer()) {
         character.getActingPlayer().broadcastUserInfo(true);
      }
   }

   @Override
   protected void onExit(Creature character) {
      if (!character.isInsideZone(ZoneId.SWAMP, this) && character.isPlayer()) {
         character.getActingPlayer().broadcastUserInfo(true);
      }
   }

   public double getMoveBonus(Creature character) {
      if (this.getCastle() != null) {
         if (!this.getCastle().getSiege().getIsInProgress() || !this.getCastle().getSiege().isTrapsActive()) {
            return 1.0;
         }

         Player player = character.getActingPlayer();
         if (player != null && player.isInSiege() && player.getSiegeState() == 2) {
            return 1.0;
         }
      }

      return this._moveBonus;
   }
}
