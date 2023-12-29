package l2e.gameserver.network.serverpackets;

public class ExChangePostState extends GameServerPacket {
   private final boolean _receivedBoard;
   private final int[] _changedMsgIds;
   private final int _changeId;

   public ExChangePostState(boolean receivedBoard, int[] changedMsgIds, int changeId) {
      this._receivedBoard = receivedBoard;
      this._changedMsgIds = changedMsgIds;
      this._changeId = changeId;
   }

   public ExChangePostState(boolean receivedBoard, int changedMsgId, int changeId) {
      this._receivedBoard = receivedBoard;
      this._changedMsgIds = new int[]{changedMsgId};
      this._changeId = changeId;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._receivedBoard ? 1 : 0);
      this.writeD(this._changedMsgIds.length);

      for(int postId : this._changedMsgIds) {
         this.writeD(postId);
         this.writeD(this._changeId);
      }
   }
}
