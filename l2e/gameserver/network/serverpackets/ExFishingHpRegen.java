package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Creature;

public class ExFishingHpRegen extends GameServerPacket {
   private final Creature _activeChar;
   private final int _time;
   private final int _fishHP;
   private final int _hpMode;
   private final int _anim;
   private final int _goodUse;
   private final int _penalty;
   private final int _hpBarColor;

   public ExFishingHpRegen(Creature character, int time, int fishHP, int HPmode, int GoodUse, int anim, int penalty, int hpBarColor) {
      this._activeChar = character;
      this._time = time;
      this._fishHP = fishHP;
      this._hpMode = HPmode;
      this._goodUse = GoodUse;
      this._anim = anim;
      this._penalty = penalty;
      this._hpBarColor = hpBarColor;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._activeChar.getObjectId());
      this.writeD(this._time);
      this.writeD(this._fishHP);
      this.writeC(this._hpMode);
      this.writeC(this._goodUse);
      this.writeC(this._anim);
      this.writeD(this._penalty);
      this.writeC(this._hpBarColor);
   }
}
