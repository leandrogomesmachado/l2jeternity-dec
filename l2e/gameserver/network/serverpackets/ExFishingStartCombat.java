package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Creature;

public class ExFishingStartCombat extends GameServerPacket {
   private final Creature _activeChar;
   private final int _time;
   private final int _hp;
   private final int _lureType;
   private final int _deceptiveMode;
   private final int _mode;

   public ExFishingStartCombat(Creature character, int time, int hp, int mode, int lureType, int deceptiveMode) {
      this._activeChar = character;
      this._time = time;
      this._hp = hp;
      this._mode = mode;
      this._lureType = lureType;
      this._deceptiveMode = deceptiveMode;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._activeChar.getObjectId());
      this.writeD(this._time);
      this.writeD(this._hp);
      this.writeC(this._mode);
      this.writeC(this._lureType);
      this.writeC(this._deceptiveMode);
   }
}
