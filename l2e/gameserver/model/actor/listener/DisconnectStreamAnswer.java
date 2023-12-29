package l2e.gameserver.model.actor.listener;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.data.dao.StreamingDAO;
import l2e.gameserver.listener.talk.OnAnswerListener;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.mods.streaming.StreamManager;
import l2e.gameserver.model.entity.mods.streaming.StreamTemplate;
import l2e.gameserver.model.strings.server.ServerMessage;

public class DisconnectStreamAnswer implements OnAnswerListener {
   private static Logger _logTwitch = Logger.getLogger("twitch");
   private final Player _player;

   public DisconnectStreamAnswer(Player player) {
      this._player = player;
   }

   @Override
   public void sayYes() {
      if (this._player != null) {
         StreamTemplate stream = StreamManager.getInstance().getMyStream(this._player);
         if (stream == null) {
            this._player.sendMessage(new ServerMessage("Twitch.CantDisconnect.NoStream", this._player.getLang()).toString());
            if (Config.ALLOW_STREAM_LOGS) {
               LogRecord record = new LogRecord(Level.INFO, "" + this._player + " tried to Disconnect from NON Existing Stream Channel!");
               record.setLoggerName("twitch");
               _logTwitch.log(record);
            }
         } else {
            stream.setAttachedPlayerId(-1, "");
            if (Config.ALLOW_STREAM_SAVE_DB) {
               StreamingDAO.updateStream(stream);
            }

            this._player.sendMessage(new ServerMessage("Twitch.DisconnectSuccess", this._player.getLang()).toString());
            if (Config.ALLOW_STREAM_LOGS) {
               LogRecord record = new LogRecord(Level.INFO, "" + this._player + " disconnected from " + stream + "!");
               record.setLoggerName("twitch");
               _logTwitch.log(record);
            }
         }
      }
   }

   @Override
   public void sayNo() {
   }
}
