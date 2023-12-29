package l2e.gameserver.network.serverpackets;

public class PledgeSkillListAdd extends GameServerPacket {
   private final int _id;
   private final int _lvl;

   public PledgeSkillListAdd(int id, int lvl) {
      this._id = id;
      this._lvl = lvl;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._id);
      this.writeD(this._lvl);
   }
}
