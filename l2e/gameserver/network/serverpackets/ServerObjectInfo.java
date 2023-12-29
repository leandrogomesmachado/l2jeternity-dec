package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;

public final class ServerObjectInfo extends GameServerPacket {
   private final Npc _activeChar;
   private final int _x;
   private final int _y;
   private final int _z;
   private final int _heading;
   private final int _idTemplate;
   private final boolean _isAttackable;
   private final double _collisionHeight;
   private final double _collisionRadius;
   private final String _name;

   public ServerObjectInfo(Npc activeChar, Creature actor) {
      this._activeChar = activeChar;
      this._idTemplate = this._activeChar.getTemplate().getIdTemplate();
      this._isAttackable = this._activeChar.isAutoAttackable(actor);
      this._collisionHeight = this._activeChar.getColHeight();
      this._collisionRadius = this._activeChar.getColRadius();
      this._x = this._activeChar.getX();
      this._y = this._activeChar.getY();
      this._z = this._activeChar.getZ();
      this._heading = this._activeChar.getHeading();
      this._name = this._activeChar.getTemplate().getName();
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._activeChar.getObjectId());
      this.writeD(this._idTemplate + 1000000);
      this.writeS(this._name);
      this.writeD(this._isAttackable ? 1 : 0);
      this.writeD(this._x);
      this.writeD(this._y);
      this.writeD(this._z);
      this.writeD(this._heading);
      this.writeF(1.0);
      this.writeF(1.0);
      this.writeF(this._collisionRadius);
      this.writeF(this._collisionHeight);
      this.writeD((int)(this._isAttackable ? this._activeChar.getCurrentHp() : 0.0));
      this.writeD(this._isAttackable ? (int)this._activeChar.getMaxHp() : 0);
      this.writeD(1);
      this.writeD(0);
   }
}
