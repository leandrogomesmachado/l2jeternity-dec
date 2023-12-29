package l2e.gameserver.network.serverpackets;

public class ExSubPledgeSkillAdd extends GameServerPacket {
   private final int _type;
   private final int _skillId;
   private final int _skillLevel;

   public ExSubPledgeSkillAdd(int type, int skillId, int skillLevel) {
      this._type = type;
      this._skillId = skillId;
      this._skillLevel = skillLevel;
   }

   @Override
   public void writeImpl() {
      this.writeD(this._type);
      this.writeD(this._skillId);
      this.writeD(this._skillLevel);
   }
}
