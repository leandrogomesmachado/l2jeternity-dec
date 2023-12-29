package l2e.gameserver.network.serverpackets;

import l2e.gameserver.data.holder.CharNameHolder;
import l2e.gameserver.model.World;

public class L2Friend extends GameServerPacket {
   private final boolean _action;
   private final boolean _online;
   private final int _objid;
   private final String _name;

   public L2Friend(boolean action, int objId) {
      this._action = action;
      this._objid = objId;
      this._name = CharNameHolder.getInstance().getNameById(objId);
      this._online = World.getInstance().getPlayer(objId) != null;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._action ? 1 : 3);
      this.writeD(this._objid);
      this.writeS(this._name);
      this.writeD(this._online ? 1 : 0);
      this.writeD(this._online ? this._objid : 0);
   }
}
