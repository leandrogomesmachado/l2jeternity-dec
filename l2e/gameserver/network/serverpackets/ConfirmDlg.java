package l2e.gameserver.network.serverpackets;

import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.components.AbstractMessage;

public class ConfirmDlg extends AbstractMessage<ConfirmDlg> {
   private int _time;
   private int _requesterId;

   public ConfirmDlg(SystemMessageId smId) {
      super(smId);
   }

   public ConfirmDlg(int id) {
      this(SystemMessageId.getSystemMessageId(id));
   }

   public ConfirmDlg(String text) {
      this(SystemMessageId.S1);
      this.addString(text);
   }

   public ConfirmDlg addTime(int time) {
      this._time = time;
      return this;
   }

   public ConfirmDlg addRequesterId(int id) {
      this._requesterId = id;
      return this;
   }

   @Override
   protected final void writeImpl() {
      this.writeInfo();
      this.writeD(this._time);
      this.writeD(this._requesterId);
   }
}
