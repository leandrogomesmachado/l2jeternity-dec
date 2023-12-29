package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Summon;

public class PetStatusShow extends GameServerPacket {
   private final int _summonType;

   public PetStatusShow(Summon summon) {
      this._summonType = summon.getSummonType();
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._summonType);
   }
}
