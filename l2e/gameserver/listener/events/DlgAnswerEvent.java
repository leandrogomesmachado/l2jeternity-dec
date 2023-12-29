package l2e.gameserver.listener.events;

import l2e.gameserver.listener.EventListener;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;

public class DlgAnswerEvent implements EventListener {
   private Player _activeChar;
   private int _messageId;
   private int _answer;
   private int _requesterId;

   public void setActiveChar(Player activeChar) {
      this._activeChar = activeChar;
   }

   public Player getActiveChar() {
      return this._activeChar;
   }

   public void setMessageId(int messageId) {
      this._messageId = messageId;
   }

   public int getMessageId() {
      return this._messageId;
   }

   public SystemMessageId getSystemMessageId() {
      return SystemMessageId.getSystemMessageId(this._messageId);
   }

   public void setAnswer(int answer) {
      this._answer = answer;
   }

   public int getAnswer() {
      return this._answer;
   }

   public void setRequesterId(int req) {
      this._requesterId = req;
   }

   public int getRequesterId() {
      return this._requesterId;
   }
}
