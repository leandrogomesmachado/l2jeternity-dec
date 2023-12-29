package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.ClanMember;
import l2e.gameserver.model.actor.Player;

public final class PledgeShowMemberListAdd extends GameServerPacket {
   private final String _name;
   private final int _lvl;
   private final int _classId;
   private final int _isOnline;
   private final int _pledgeType;

   public PledgeShowMemberListAdd(Player player) {
      this._name = player.getName();
      this._lvl = player.getLevel();
      this._classId = player.getClassId().getId();
      this._isOnline = player.isOnline() ? player.getObjectId() : 0;
      this._pledgeType = player.getPledgeType();
   }

   public PledgeShowMemberListAdd(ClanMember cm) {
      this._name = cm.getName();
      this._lvl = cm.getLevel();
      this._classId = cm.getClassId();
      this._isOnline = cm.isOnline() ? cm.getObjectId() : 0;
      this._pledgeType = cm.getPledgeType();
   }

   @Override
   protected final void writeImpl() {
      this.writeS(this._name);
      this.writeD(this._lvl);
      this.writeD(this._classId);
      this.writeD(0);
      this.writeD(1);
      this.writeD(this._isOnline);
      this.writeD(this._pledgeType);
   }
}
