package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Creature;

public final class SetupGauge extends GameServerPacket {
   public static final int BLUE = 0;
   public static final int RED = 1;
   public static final int CYAN = 2;
   private final int _dat1;
   private final int _time;
   private final int _time2;
   private final int _charObjId;

   public SetupGauge(Creature character, int dat1, int time) {
      this._charObjId = character.getObjectId();
      this._dat1 = dat1;
      this._time = time;
      this._time2 = time;
   }

   public SetupGauge(Creature character, int color, int currentTime, int maxTime) {
      this._charObjId = character.getObjectId();
      this._dat1 = color;
      this._time = currentTime;
      this._time2 = maxTime;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._charObjId);
      this.writeD(this._dat1);
      this.writeD(this._time);
      this.writeD(this._time2);
   }
}
