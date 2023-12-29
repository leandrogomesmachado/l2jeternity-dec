package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Player;

public final class PartySmallWindowUpdate extends GameServerPacket {
   private final Player _member;

   public PartySmallWindowUpdate(Player member) {
      this._member = member;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._member.getObjectId());
      this.writeS(this._member.getName());
      this.writeD((int)this._member.getCurrentCp());
      this.writeD((int)this._member.getMaxCp());
      this.writeD((int)this._member.getCurrentHp());
      this.writeD((int)this._member.getMaxHp());
      this.writeD((int)this._member.getCurrentMp());
      this.writeD((int)this._member.getMaxMp());
      this.writeD(this._member.getLevel());
      this.writeD(this._member.getClassId().getId());
   }
}
