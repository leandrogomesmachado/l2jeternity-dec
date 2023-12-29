package l2e.gameserver.network.clientpackets;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.L2FriendSay;

public final class RequestSendL2FriendSay extends GameClientPacket {
   private static Logger _logChat = Logger.getLogger("chat");
   private String _message;
   private String _reciever;

   @Override
   protected void readImpl() {
      this._message = this.readS();
      this._reciever = this.readS();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         if (this._message != null && !this._message.isEmpty() && this._message.length() <= 300) {
            Player targetPlayer = World.getInstance().getPlayer(this._reciever);
            if (targetPlayer != null && targetPlayer.getFriendList().contains(activeChar.getObjectId())) {
               if (Config.LOG_CHAT) {
                  LogRecord record = new LogRecord(Level.INFO, this._message);
                  record.setLoggerName("chat");
                  record.setParameters(new Object[]{"PRIV_MSG", "[" + activeChar.getName() + " to " + this._reciever + "]"});
                  _logChat.log(record);
               }

               targetPlayer.sendPacket(new L2FriendSay(activeChar.getName(), this._reciever, this._message));
            } else {
               activeChar.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
            }
         }
      }
   }
}
