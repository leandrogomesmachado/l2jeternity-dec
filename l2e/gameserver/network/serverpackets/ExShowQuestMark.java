package l2e.gameserver.network.serverpackets;

public class ExShowQuestMark extends GameServerPacket {
   private final int _questId;

   public ExShowQuestMark(int questId, int cond) {
      this._questId = questId;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._questId);
   }
}
