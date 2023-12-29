package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Player;

public class ExDuelUpdateUserInfo extends GameServerPacket {
   private final Player _activeChar;

   public ExDuelUpdateUserInfo(Player cha) {
      this._activeChar = cha;
   }

   @Override
   protected void writeImpl() {
      this.writeS(this._activeChar.getName());
      this.writeD(this._activeChar.getObjectId());
      this.writeD(this._activeChar.getClassId().getId());
      this.writeD(this._activeChar.getLevel());
      this.writeD((int)this._activeChar.getCurrentHp());
      this.writeD((int)this._activeChar.getMaxHp());
      this.writeD((int)this._activeChar.getCurrentMp());
      this.writeD((int)this._activeChar.getMaxMp());
      this.writeD((int)this._activeChar.getCurrentCp());
      this.writeD((int)this._activeChar.getMaxCp());
   }
}
