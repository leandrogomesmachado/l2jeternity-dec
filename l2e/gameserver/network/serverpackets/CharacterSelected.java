package l2e.gameserver.network.serverpackets;

import l2e.gameserver.GameTimeController;
import l2e.gameserver.model.actor.Player;

public class CharacterSelected extends GameServerPacket {
   private final Player _activeChar;
   private final int _sessionId;

   public CharacterSelected(Player cha, int sessionId) {
      this._activeChar = cha;
      this._sessionId = sessionId;
   }

   @Override
   protected final void writeImpl() {
      this.writeS(this._activeChar.getName());
      this.writeD(this._activeChar.getObjectId());
      this.writeS(this._activeChar.getTitle());
      this.writeD(this._sessionId);
      this.writeD(this._activeChar.getClanId());
      this.writeD(0);
      this.writeD(this._activeChar.getAppearance().getSex() ? 1 : 0);
      this.writeD(this._activeChar.getRace().ordinal());
      this.writeD(this._activeChar.getClassId().getId());
      this.writeD(1);
      this.writeD(this._activeChar.getX());
      this.writeD(this._activeChar.getY());
      this.writeD(this._activeChar.getZ());
      this.writeF(this._activeChar.getCurrentHp());
      this.writeF(this._activeChar.getCurrentMp());
      this.writeD(this._activeChar.getSp());
      this.writeQ(this._activeChar.getExp());
      this.writeD(this._activeChar.getLevel());
      this.writeD(this._activeChar.getKarma());
      this.writeD(this._activeChar.getPkKills());
      this.writeD(this._activeChar.getINT());
      this.writeD(this._activeChar.getSTR());
      this.writeD(this._activeChar.getCON());
      this.writeD(this._activeChar.getMEN());
      this.writeD(this._activeChar.getDEX());
      this.writeD(this._activeChar.getWIT());
      this.writeD(GameTimeController.getInstance().getGameTime() % 1440);
      this.writeD(0);
      this.writeD(this._activeChar.getClassId().getId());
      this.writeD(0);
      this.writeD(0);
      this.writeD(0);
      this.writeD(0);
      this.writeB(new byte[64]);
      this.writeD(0);
   }
}
