package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Creature;

public class ChangeMoveType extends GameServerPacket {
   public static final int WALK = 0;
   public static final int RUN = 1;
   private final int _charObjId;
   private final boolean _running;

   public ChangeMoveType(Creature character) {
      this._charObjId = character.getObjectId();
      this._running = character.isRunning();
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._charObjId);
      this.writeD(this._running ? 1 : 0);
      this.writeD(0);
   }
}
