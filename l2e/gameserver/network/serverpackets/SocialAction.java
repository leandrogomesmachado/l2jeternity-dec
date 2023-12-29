package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Creature;

public class SocialAction extends GameServerPacket {
   public static final int LEVEL_UP = 2122;
   private final int _charObjId;
   private final int _actionId;

   public SocialAction(int objectId, int actionId) {
      this._charObjId = objectId;
      this._actionId = actionId;
   }

   public SocialAction(Creature cha, int actionId) {
      this(cha.getObjectId(), actionId);
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._charObjId);
      this.writeD(this._actionId);
   }
}
