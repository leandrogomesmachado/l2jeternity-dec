package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.Party;
import l2e.gameserver.model.actor.Player;

public final class PartySmallWindowAll extends GameServerPacket {
   private final Party _party;
   private final Player _exclude;
   private final int _dist;
   private final int _LeaderOID;

   public PartySmallWindowAll(Player exclude, Party party) {
      this._exclude = exclude;
      this._party = party;
      this._LeaderOID = this._party.getLeaderObjectId();
      this._dist = this._party.getLootDistribution();
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._LeaderOID);
      this.writeD(this._dist);
      this.writeD(this._party.getMemberCount() - 1);

      for(Player member : this._party.getMembers()) {
         if (member != null && member != this._exclude) {
            this.writeD(member.getObjectId());
            this.writeS(member.getName());
            this.writeD((int)member.getCurrentCp());
            this.writeD((int)member.getMaxCp());
            this.writeD((int)member.getCurrentHp());
            this.writeD((int)member.getMaxHp());
            this.writeD((int)member.getCurrentMp());
            this.writeD((int)member.getMaxMp());
            this.writeD(member.getLevel());
            this.writeD(member.getClassId().getId());
            this.writeD(0);
            this.writeD(member.getRace().ordinal());
            this.writeD(0);
            this.writeD(0);
            if (member.hasSummon()) {
               this.writeD(member.getSummon().getObjectId());
               this.writeD(member.getSummon().getId() + 1000000);
               this.writeD(member.getSummon().getSummonType());
               this.writeS(member.getSummon().getName());
               this.writeD((int)member.getSummon().getCurrentHp());
               this.writeD((int)member.getSummon().getMaxHp());
               this.writeD((int)member.getSummon().getCurrentMp());
               this.writeD((int)member.getSummon().getMaxMp());
               this.writeD(member.getSummon().getLevel());
            } else {
               this.writeD(0);
            }
         }
      }
   }
}
