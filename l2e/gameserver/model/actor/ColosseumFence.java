package l2e.gameserver.model.actor;

import java.awt.Rectangle;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.network.serverpackets.ExColosseumFenceInfo;

public final class ColosseumFence extends GameObject {
   private final int _minZ;
   private final int _maxZ;
   private final ColosseumFence.FenceState _state;
   private final Rectangle _bounds;

   private ColosseumFence(int objectId, int instanceId, int x, int y, int z, int minZ, int maxZ, int width, int height, ColosseumFence.FenceState state) {
      super(objectId);
      this.setReflectionId(instanceId);
      this.setXYZ(x, y, z);
      this._minZ = minZ;
      this._maxZ = maxZ;
      this._state = state;
      this._bounds = new Rectangle(x - width / 2, y - height / 2, width, height);
   }

   public ColosseumFence(int instanceId, int x, int y, int z, int minZ, int maxZ, int width, int height, ColosseumFence.FenceState state) {
      this(IdFactory.getInstance().getNextId(), instanceId, x, y, z, minZ, maxZ, width, height, state);
   }

   @Override
   public void sendInfo(Player activeChar) {
      activeChar.sendPacket(new ExColosseumFenceInfo(this));
   }

   public int getFenceX() {
      return this._bounds.x;
   }

   public int getFenceY() {
      return this._bounds.y;
   }

   public int getFenceMinZ() {
      return this._minZ;
   }

   public int getFenceMaxZ() {
      return this._maxZ;
   }

   public int getFenceWidth() {
      return this._bounds.width;
   }

   public int getFenceHeight() {
      return this._bounds.height;
   }

   public ColosseumFence.FenceState getFenceState() {
      return this._state;
   }

   @Override
   public int getId() {
      return this.getObjectId();
   }

   @Override
   public boolean isAutoAttackable(Creature attacker) {
      return false;
   }

   public boolean isInsideFence(int x, int y, int z) {
      return x >= this._bounds.x
         && y >= this._bounds.y
         && z >= this._minZ
         && z <= this._maxZ
         && x <= this._bounds.x + this._bounds.width
         && y <= this._bounds.y + this._bounds.width;
   }

   public static enum FenceState {
      HIDDEN,
      OPEN,
      CLOSED;
   }
}
