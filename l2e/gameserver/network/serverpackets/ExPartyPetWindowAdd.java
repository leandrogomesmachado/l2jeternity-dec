package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Summon;

public final class ExPartyPetWindowAdd extends GameServerPacket {
   private final Summon _summon;

   public ExPartyPetWindowAdd(Summon summon) {
      this._summon = summon;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._summon.getObjectId());
      this.writeD(this._summon.getTemplate().getIdTemplate() + 1000000);
      this.writeD(this._summon.getSummonType());
      this.writeD(this._summon.getOwner().getObjectId());
      this.writeS(this._summon.getName());
      this.writeD((int)this._summon.getCurrentHp());
      this.writeD((int)this._summon.getMaxHp());
      this.writeD((int)this._summon.getCurrentMp());
      this.writeD((int)this._summon.getMaxMp());
      this.writeD(this._summon.getLevel());
   }
}
