package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.Henna;

public final class HennaUnequipInfo extends GameServerPacket {
   private final Player _activeChar;
   private final Henna _henna;

   public HennaUnequipInfo(Henna henna, Player player) {
      this._henna = henna;
      this._activeChar = player;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._henna.getDyeId());
      this.writeD(this._henna.getDyeItemId());
      this.writeQ((long)this._henna.getCancelCount());
      this.writeQ((long)this._henna.getCancelFee());
      this.writeD(this._henna.isAllowedClass(this._activeChar.getClassId()) ? 1 : 0);
      this.writeQ(this._activeChar.getAdena());
      this.writeD(this._activeChar.getINT());
      this.writeC(this._activeChar.getINT() - this._henna.getStatINT());
      this.writeD(this._activeChar.getSTR());
      this.writeC(this._activeChar.getSTR() - this._henna.getStatSTR());
      this.writeD(this._activeChar.getCON());
      this.writeC(this._activeChar.getCON() - this._henna.getStatCON());
      this.writeD(this._activeChar.getMEN());
      this.writeC(this._activeChar.getMEN() - this._henna.getStatMEN());
      this.writeD(this._activeChar.getDEX());
      this.writeC(this._activeChar.getDEX() - this._henna.getStatDEX());
      this.writeD(this._activeChar.getWIT());
      this.writeC(this._activeChar.getWIT() - this._henna.getStatWIT());
   }
}
