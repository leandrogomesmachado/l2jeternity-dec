package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Summon;

public class ExPartyPetWindowDelete extends GameServerPacket {
   private final Summon _summon;

   public ExPartyPetWindowDelete(Summon summon) {
      this._summon = summon;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._summon.getObjectId());
      this.writeD(this._summon.getOwner().getObjectId());
      this.writeS(this._summon.getName());
   }
}
