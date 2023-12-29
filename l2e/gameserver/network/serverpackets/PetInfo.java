package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.actor.instance.PetInstance;
import l2e.gameserver.model.actor.instance.ServitorInstance;

public class PetInfo extends GameServerPacket {
   private final Summon _summon;
   private final int _x;
   private final int _y;
   private final int _z;
   private final int _heading;
   private final boolean _isSummoned;
   private final int _val;
   private final double _mAtkSpd;
   private final double _pAtkSpd;
   private final int _runSpd;
   private final int _walkSpd;
   private final int _swimRunSpd;
   private final int _swimWalkSpd;
   private final int _flyRunSpd;
   private final int _flyWalkSpd;
   private final double _moveMultiplier;
   private final double _maxHp;
   private final double _maxMp;
   private int _maxFed;
   private int _curFed;

   public PetInfo(Summon summon, int val) {
      this._summon = summon;
      this._isSummoned = this._summon.isShowSummonAnimation();
      this._x = this._summon.getX();
      this._y = this._summon.getY();
      this._z = this._summon.getZ();
      this._heading = this._summon.getHeading();
      this._mAtkSpd = this._summon.getMAtkSpd();
      this._pAtkSpd = (double)((int)this._summon.getPAtkSpd());
      this._moveMultiplier = summon.getMovementSpeedMultiplier();
      this._runSpd = (int)Math.round(summon.getRunSpeed() / this._moveMultiplier);
      this._walkSpd = (int)Math.round(summon.getWalkSpeed() / this._moveMultiplier);
      this._swimRunSpd = (int)Math.round(summon.getSwimRunSpeed() / this._moveMultiplier);
      this._swimWalkSpd = (int)Math.round(summon.getSwimWalkSpeed() / this._moveMultiplier);
      this._flyRunSpd = summon.isFlying() ? this._runSpd : 0;
      this._flyWalkSpd = summon.isFlying() ? this._walkSpd : 0;
      this._maxHp = this._summon.getMaxHp();
      this._maxMp = this._summon.getMaxMp();
      this._val = val;
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
      this.writeD(this._summon.getTemplate().getIdTemplate() + 1000000);
      this.writeD(0);
      this.writeD(this._x);
      this.writeD(this._y);
      this.writeD(this._z);
      this.writeD(this._heading);
      this.writeD(0);
      this.writeD((int)this._mAtkSpd);
      this.writeD((int)this._pAtkSpd);
      this.writeD(this._runSpd);
      this.writeD(this._walkSpd);
      this.writeD(this._swimRunSpd);
      this.writeD(this._swimWalkSpd);
      this.writeD(this._flyRunSpd);
      this.writeD(this._flyWalkSpd);
      this.writeD(this._flyRunSpd);
      this.writeD(this._flyWalkSpd);
      this.writeF(this._moveMultiplier);
      this.writeF((double)this._summon.getAttackSpeedMultiplier());
      this.writeF(this._summon.getTemplate().getfCollisionRadius());
      this.writeF(this._summon.getTemplate().getfCollisionHeight());
      this.writeD(this._summon.getWeapon());
      this.writeD(this._summon.getArmor());
      this.writeD(0);
      this.writeC(this._summon.getOwner() != null ? 1 : 0);
      this.writeC(this._summon.isRunning() ? 1 : 0);
      this.writeC(this._summon.isInCombat() ? 1 : 0);
      this.writeC(this._summon.isAlikeDead() ? 1 : 0);
      this.writeC(this._isSummoned ? 2 : this._val);
      this.writeD(-1);
      this.writeS(this._summon.getName());
      this.writeD(-1);
      this.writeS(
         this._summon.getOwner().isInFightEvent()
            ? this._summon.getOwner().getFightEvent().getVisibleTitle(this._summon.getOwner(), this._summon.getOwner(), this._summon.getTitle(), false)
            : this._summon.getTitle()
      );
      this.writeD(1);
      this.writeD(this._summon.getPvpFlag());
      this.writeD(this._summon.getKarma());
      this.writeD(this._curFed);
      this.writeD(this._maxFed);
      this.writeD((int)this._summon.getCurrentHp());
      this.writeD((int)this._maxHp);
      this.writeD((int)this._summon.getCurrentMp());
      this.writeD((int)this._maxMp);
      this.writeD(this._summon.getStat().getSp());
      this.writeD(this._summon.getLevel());
      this.writeQ(this._summon.getStat().getExp());
      if (this._summon.getExpForThisLevel() > this._summon.getStat().getExp()) {
         this.writeQ(this._summon.getStat().getExp());
      } else {
         this.writeQ(this._summon.getExpForThisLevel());
      }

      this.writeQ(this._summon.getExpForNextLevel());
      this.writeD(this._summon instanceof PetInstance ? this._summon.getInventory().getTotalWeight() : 0);
      this.writeD(this._summon.getMaxLoad());
      this.writeD((int)this._summon.getPAtk(null));
      this.writeD((int)this._summon.getPDef(null));
      this.writeD((int)this._summon.getMAtk(null, null));
      this.writeD((int)this._summon.getMDef(null, null));
      this.writeD(this._summon.getAccuracy());
      this.writeD(this._summon.getEvasionRate(null));
      this.writeD((int)this._summon.getCriticalHit(null, null));
      this.writeD((int)this._summon.getMoveSpeed());
      this.writeD((int)this._summon.getPAtkSpd());
      this.writeD((int)this._summon.getMAtkSpd());
      this.writeD(this._summon.getAbnormalEffectMask());
      this.writeH(this._summon.isMountable() ? 1 : 0);
      this.writeC(this._summon.isInWater(this._summon) ? 1 : (this._summon.isFlying() ? 2 : 0));
      this.writeH(0);
      this.writeC(this._summon.getTeam());
      this.writeD(this._summon.getSoulShotsPerHit());
      this.writeD(this._summon.getSpiritShotsPerHit());
      int form = 0;
      int npcId = this._summon.getId();
      if (npcId == 16041 || npcId == 16042) {
         if (this._summon.getLevel() > 69) {
            form = 3;
         } else if (this._summon.getLevel() > 64) {
            form = 2;
         } else if (this._summon.getLevel() > 59) {
            form = 1;
         }
      } else if (npcId == 16025 || npcId == 16037) {
         if (this._summon.getLevel() > 69) {
            form = 3;
         } else if (this._summon.getLevel() > 64) {
            form = 2;
         } else if (this._summon.getLevel() > 59) {
            form = 1;
         }
      }

      this.writeD(form);
      this.writeD(this._summon.getAbnormalEffectMask2());
   }
}
