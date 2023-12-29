package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.Party;
import l2e.gameserver.model.actor.Player;

public final class PartySmallWindowAdd extends GameServerPacket {
   private final Player _member;
   private final int _leaderId;
   private final int _distribution;

   public PartySmallWindowAdd(Player member, Party party) {
      this._member = member;
      this._leaderId = party.getLeaderObjectId();
      this._distribution = party.getLootDistribution();
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._leaderId);
      this.writeD(this._distribution);
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
      this.writeD(0);
      this.writeD(0);
   }
}
