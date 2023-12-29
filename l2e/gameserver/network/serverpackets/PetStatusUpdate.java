package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.actor.instance.PetInstance;
import l2e.gameserver.model.actor.instance.ServitorInstance;

public class PetStatusUpdate extends GameServerPacket {
   private final Summon _summon;
   private final double _maxHp;
   private final double _maxMp;
   private int _maxFed;
   private int _curFed;

   public PetStatusUpdate(Summon summon) {
      this._summon = summon;
      this._maxHp = this._summon.getMaxHp();
      this._maxMp = this._summon.getMaxMp();
      if (this._summon instanceof PetInstance) {
         PetInstance pet = (PetInstance)this._summon;
         this._curFed = pet.getCurrentFed();
         this._maxFed = pet.getMaxFed();
      } else if (this._summon instanceof ServitorInstance) {
         ServitorInstance sum = (ServitorInstance)this._summon;
         this._curFed = sum.getTimeRemaining();
         this._maxFed = sum.getTotalLifeTime();
      }
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._summon.getSummonType());
      this.writeD(this._summon.getObjectId());
      this.writeD(this._summon.getX());
      this.writeD(this._summon.getY());
      this.writeD(this._summon.getZ());
      this.writeS("");
      this.writeD(this._curFed);
      this.writeD(this._maxFed);
      this.writeD((int)this._summon.getCurrentHp());
      this.writeD((int)this._maxHp);
      this.writeD((int)this._summon.getCurrentMp());
      this.writeD((int)this._maxMp);
      this.writeD(this._summon.getLevel());
      this.writeQ(this._summon.getStat().getExp());
      this.writeQ(this._summon.getExpForThisLevel());
      this.writeQ(this._summon.getExpForNextLevel());
   }
}
