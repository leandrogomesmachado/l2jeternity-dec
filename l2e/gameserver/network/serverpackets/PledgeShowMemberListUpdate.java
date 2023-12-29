package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.ClanMember;
import l2e.gameserver.model.actor.Player;

public final class PledgeShowMemberListUpdate extends GameServerPacket {
   private final int _pledgeType;
   private int _hasSponsor;
   private final String _name;
   private final int _level;
   private final int _classId;
   private final int _objectId;
   private final int _isOnline;
   private final int _sex;

   public PledgeShowMemberListUpdate(Player player) {
      this._pledgeType = player.getPledgeType();
      if (this._pledgeType == -1) {
         this._hasSponsor = player.getSponsor() != 0 ? 1 : 0;
      } else {
         this._hasSponsor = 0;
      }

      this._name = player.getName();
      this._level = player.getLevel();
      this._classId = player.getClassId().getId();
      this._sex = player.getAppearance().getSex() ? 1 : 0;
      this._objectId = player.getObjectId();
      this._isOnline = player.isOnline() ? 1 : 0;
   }

   public PledgeShowMemberListUpdate(ClanMember member) {
      this._name = member.getName();
      this._level = member.getLevel();
      this._classId = member.getClassId();
      this._objectId = member.getObjectId();
      this._isOnline = member.isOnline() ? 1 : 0;
      this._pledgeType = member.getPledgeType();
      this._sex = member.getSex() ? 1 : 0;
      if (this._pledgeType == -1) {
         this._hasSponsor = member.getSponsor() != 0 ? 1 : 0;
      } else {
         this._hasSponsor = 0;
      }
   }

   @Override
   protected final void writeImpl() {
      this.writeS(this._name);
      this.writeD(this._level);
      this.writeD(this._classId);
      this.writeD(this._sex);
      this.writeD(this._objectId);
      this.writeD(this._isOnline);
      this.writeD(this._pledgeType);
      this.writeD(this._hasSponsor);
   }
}
