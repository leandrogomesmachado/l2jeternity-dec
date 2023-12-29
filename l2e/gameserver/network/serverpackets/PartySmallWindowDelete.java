package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Player;

public final class PartySmallWindowDelete extends GameServerPacket {
   private final Player _member;

   public PartySmallWindowDelete(Player member) {
      this._member = member;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._member.getObjectId());
      this.writeS(this._member.getName());
   }
}
