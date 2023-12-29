package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.DoorInstance;
import l2e.gameserver.model.actor.instance.StaticObjectInstance;

public class StaticObject extends GameServerPacket {
   private final int _staticObjectId;
   private final int _objectId;
   private final int _type;
   private final int _isTargetable;
   private final int _meshIndex;
   private final int _isClosed;
   private final int _isEnemy;
   private final double _maxHp;
   private final int _currentHp;
   private final int _showHp;
   private final int _damageGrade;

   public StaticObject(StaticObjectInstance staticObject) {
      this._staticObjectId = staticObject.getId();
      this._objectId = staticObject.getObjectId();
      this._type = 0;
      this._isTargetable = 1;
      this._meshIndex = staticObject.getMeshIndex();
      this._isClosed = 0;
      this._isEnemy = 0;
      this._maxHp = 0.0;
      this._currentHp = 0;
      this._showHp = 0;
      this._damageGrade = 0;
   }

   public StaticObject(DoorInstance door, boolean targetable) {
      this._staticObjectId = door.getDoorId();
      this._objectId = door.getObjectId();
      this._type = 1;
      this._isTargetable = !door.isTargetable() && !targetable ? 0 : 1;
      this._meshIndex = door.getMeshIndex();
      this._isClosed = door.isOpen() ? 1 : 0;
      this._isEnemy = door.isEnemy() ? 1 : 0;
      this._maxHp = door.getMaxHp();
      this._currentHp = (int)door.getCurrentHp();
      this._showHp = door.getIsShowHp() ? 1 : 0;
      this._damageGrade = door.getDamage();
   }

   public StaticObject(DoorInstance door, Player player) {
      this._staticObjectId = door.getDoorId();
      this._objectId = door.getObjectId();
      this._type = 1;
      this._isTargetable = door.isTargetable() ? 1 : (player.isGM() ? 1 : 0);
      this._meshIndex = 1;
      this._isClosed = door.isOpen() ? 0 : 1;
      this._isEnemy = door.isEnemy() ? 1 : 0;
      this._currentHp = (int)door.getCurrentHp();
      this._maxHp = door.getMaxHp();
      this._showHp = door.getIsShowHp() ? 1 : 0;
      this._damageGrade = door.getDamage();
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._staticObjectId);
      this.writeD(this._objectId);
      this.writeD(this._type);
      this.writeD(this._isTargetable);
      this.writeD(this._meshIndex);
      this.writeD(this._isClosed);
      this.writeD(this._isEnemy);
      this.writeD(this._currentHp);
      this.writeD((int)this._maxHp);
      this.writeD(this._showHp);
      this.writeD(this._damageGrade);
   }
}
