package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Player;

public class ExBrExtraUserInfo extends GameServerPacket {
   private final int _charObjId;
   private final int _val;
   private final int _lectureMark;

   public ExBrExtraUserInfo(Player player) {
      this._charObjId = player.getObjectId();
      this._val = player.getAbnormalEffectMask3();
      this._lectureMark = player.getLectureMark();
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._charObjId);
      this.writeD(this._val);
      this.writeC(this._lectureMark);
   }
}
