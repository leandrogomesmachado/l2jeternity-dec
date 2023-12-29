package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.effects.EffectFlag;
import l2e.gameserver.model.zone.ZoneId;

public class EtcStatusUpdate extends GameServerPacket {
   private final Player _activeChar;

   public EtcStatusUpdate(Player activeChar) {
      this._activeChar = activeChar;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._activeChar.getCharges());
      this.writeD(this._activeChar.getWeightPenalty());
      this.writeD(!this._activeChar.getMessageRefusal() && !this._activeChar.isChatBanned() && !this._activeChar.isSilenceMode() ? 0 : 1);
      this.writeD(this._activeChar.isInsideZone(ZoneId.DANGER_AREA) ? 1 : 0);
      this.writeD(this._activeChar.getExpertiseWeaponPenalty());
      this.writeD(this._activeChar.getExpertiseArmorPenalty());
      this.writeD(this._activeChar.isAffected(EffectFlag.CHARM_OF_COURAGE) ? 1 : 0);
      this.writeD(this._activeChar.getDeathPenaltyBuffLevel());
      this.writeD(this._activeChar.getChargedSouls());
   }
}
