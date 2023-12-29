package l2e.gameserver.handler.voicedcommandhandlers.impl;

import java.text.SimpleDateFormat;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import l2e.commons.util.Functions;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.dao.StreamingDAO;
import l2e.gameserver.data.holder.CharNameHolder;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.listener.DisconnectStreamAnswer;
import l2e.gameserver.model.entity.mods.streaming.StreamManager;
import l2e.gameserver.model.entity.mods.streaming.StreamTemplate;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.model.strings.server.ServerStorage;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class StreamVoice extends Functions implements IVoicedCommandHandler {
   private static Logger _logTwitch = Logger.getLogger("twitch");
   private static final SimpleDateFormat END_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm");
   private static final String[] VOICED_COMMANDS = new String[]{"stream"};

   private boolean useStreamBypass(Player player, String bypass, Object... params) {
      return this.useVoicedCommand("stream", player, bypass + (params.length > 0 ? " " : "") + Util.joinArrayWithCharacter(params, " "));
   }

   @Override
   public boolean useVoicedCommand(String command, Player activeChar, String args) {
      if (Config.ALLOW_STREAM_PANEL && Config.ALLOW_STREAM_SYSTEM) {
         try {
            if (args == null || args.isEmpty()) {
               args = "main";
            }

            StringTokenizer st = new StringTokenizer(args);
            String var5 = st.nextToken();
            switch(var5) {
               case "main":
                  if (StreamManager.getInstance().getMyStream(activeChar) != null) {
                     return this.useStreamBypass(activeChar, "mainStreamer");
                  } else {
                     if (Config.ALLOW_STREAM_NEED_APPROVAL && StreamManager.getInstance().isAwaitingForApproval(activeChar)) {
                        return this.useStreamBypass(activeChar, "mainAwaitingApproval");
                     }

                     return this.useStreamBypass(activeChar, "mainNotStreamer");
                  }
               case "mainStreamer":
                  checkReward(activeChar);
                  this.showMainStreamerPage(activeChar);
                  return true;
               case "mainNotStreamer":
                  this.showMainNotStreamerPage(activeChar);
                  return true;
               case "mainAwaitingApproval":
                  this.showMainAwaitingApprovalPage(activeChar);
                  return true;
               case "tryConnectToStream":
                  String streamName = st.nextToken();
                  this.tryConnectToStream(activeChar, streamName);
                  return true;
               case "errorConnectToStream": {
                  String errorMsg = Util.getAllTokens(st);
                  this.showErrorConnectToStreamPage(activeChar, errorMsg);
                  return true;
               }
               case "transferStream":
                  this.showTransferStreamPage(activeChar);
                  return true;
               case "finalizeTransferStream":
                  String newCharName = st.nextToken();
                  this.tryTransferChannel(activeChar, newCharName);
                  return true;
               case "errorTransferStream": {
                  String errorMsg = Util.getAllTokens(st);
                  this.showErrorTransferStreamPage(activeChar, errorMsg);
                  return true;
               }
               case "disconnectFromStream":
                  askDisconnectFromStream(activeChar);
                  return true;
               default:
                  return true;
            }
         } catch (Exception var8) {
            return false;
         }
      } else {
         return false;
      }
   }

   private void showMainStreamerPage(Player player) {
      StreamTemplate myStream = StreamManager.getInstance().getMyStream(player);
      NpcHtmlMessage html = new NpcHtmlMessage(0);
      html.setFile(player, player.getLang(), "data/html/mods/stream/mainStreamer.htm");
      html.replace("%streamTitle%", myStream.getStreamTitle());
      html.replace("%streamChannelName%", myStream.getChannelName());
      html.replace("%streamGameName%", myStream.getStreamGameName());
      html.replace("%streamViewersCount%", (long)myStream.getViewersCount());
      StringBuilder rewards = new StringBuilder();
      if (!myStream.isTitleCorrect()) {
         rewards.append("<font color=d50000>Stream is currently NOT being rewarded! Reason: Wrong Title.</font>");
      } else if (!myStream.isStreamGameNameCorrect()) {
         rewards.append("<font color=d50000>Stream is currently NOT being rewarded! Reason: Wrong Game Name.</font>");
      } else if (myStream.getViewersCount() < StreamManager.getInstance().getMinRequiredViewers()) {
         rewards.append(
            "<font color=d50000>Stream is currently NOT being rewarded! Reason: Not enough Viewers. Min Viewers: ${StreamsHolder.minRequiredViewers}</font>"
         );
      } else if (!StreamManager.isPlayerActive(player)) {
         rewards.append("<font color=d50000>AFK Streamers are not getting rewarded!</font>");
      } else if (myStream.isNowPunished()) {
         rewards.append(
            "<font color=d50000>You have been punished! Contact Game Administrators for more info.<br1>End Date: "
               + END_DATE_FORMAT.format(Long.valueOf(myStream.getPunishedUntilDate()))
               + "</font>"
         );
      } else if (myStream.getTotalRewardedSecondsToday() >= (long)Config.MAX_SEC_TO_REWARD_STREAMERS) {
         rewards.append("<font color=d50000>You have reached MAX Streaming Time for today!</font>");
      } else {
         rewards.append(
            "In "
               + Math.ceil((double)(((long)Config.TIME_TO_REWARD_STREAMERS - myStream.getNotRewardedSeconds()) / 60L))
               + " minutes You will receive:</center>"
         );
         rewards.append("<br1>");
         rewards.append("<font color=d9b330>");
         rewards.append("<table width=260>");
         rewards.append("<tr>");
         rewards.append("<td width=130 align=center><font color=d4cdbe>Since 0 Viewers:</font</td>");
         rewards.append("<td width=130 align=center><font color=d4cdbe>Since 3 Viewers:</font></td>");
         rewards.append("</tr>");
         rewards.append("<tr>");
         rewards.append("<td align=center>2 Festival Adena!</td>");
         rewards.append("<td align=center>6 Festival Adena!</td>");
         rewards.append("</tr>");
         rewards.append("<tr>");
         rewards.append("<td align=center><br></td>");
         rewards.append("<td align=center></td>");
         rewards.append("</tr>");
         rewards.append("</table>");
         rewards.append("</font>");
      }

      html.replace("%serverName%", Config.SERVER_NAME);
      html.replace("%streamRewards%", rewards.toString());
      player.sendPacket(html);
   }

   private void showMainNotStreamerPage(Player player) {
      NpcHtmlMessage html = new NpcHtmlMessage(0);
      html.setFile(player, player.getLang(), "data/html/mods/stream/mainNotStreamer.htm");
      StringBuilder rewardTime = new StringBuilder();
      long secondsForReward = (long)Config.TIME_TO_REWARD_STREAMERS;
      int hours = (int)Math.floor((double)(secondsForReward / 3600L));
      if (hours > 1) {
         rewardTime.append(hours + " ");
         if (secondsForReward - (long)(hours * 3600) == 0L) {
            rewardTime.append("Hours");
         } else {
            rewardTime.append(" and " + Math.floor((double)(secondsForReward - (long)(hours * 3600 / 60))) + " Minutes");
         }
      } else if (hours == 1) {
         rewardTime.append("Hour");
      } else if (secondsForReward / 60L > 1L) {
         rewardTime.append(Math.floor((double)(secondsForReward / 60L)) + " Minutes");
      } else if (secondsForReward / 60L == 1L) {
         rewardTime.append("Minute ");
      } else {
         rewardTime.append(secondsForReward + " seconds");
      }

      html.replace("%serverName%", Config.SERVER_NAME);
      html.replace("%rewardTime%", rewardTime.toString());
      player.sendPacket(html);
   }

   private void showMainAwaitingApprovalPage(Player player) {
      NpcHtmlMessage html = new NpcHtmlMessage(0);
      html.setFile(player, player.getLang(), "data/html/mods/stream/mainAwaitingApproval.htm");
      html.replace("%serverName%", Config.SERVER_NAME);
      player.sendPacket(html);
   }

   private void showErrorConnectToStreamPage(Player player, String errorMsg) {
      NpcHtmlMessage html = new NpcHtmlMessage(0);
      html.setFile(player, player.getLang(), "data/html/mods/stream/errorConnectToStream.htm");
      html.replace("%serverName%", Config.SERVER_NAME);
      html.replace("%errorMsg%", errorMsg);
      player.sendPacket(html);
   }

   private void showTransferStreamPage(Player player) {
      NpcHtmlMessage html = new NpcHtmlMessage(0);
      html.setFile(player, player.getLang(), "data/html/mods/stream/transferStream.htm");
      html.replace("%serverName%", Config.SERVER_NAME);
      player.sendPacket(html);
   }

   private void showErrorTransferStreamPage(Player player, String errorMsg) {
      NpcHtmlMessage html = new NpcHtmlMessage(0);
      html.setFile(player, player.getLang(), "data/html/mods/stream/errorTransferStream.htm");
      html.replace("%serverName%", Config.SERVER_NAME);
      html.replace("%errorMsg%", errorMsg);
      player.sendPacket(html);
   }

   private void tryConnectToStream(Player player, String streamName) {
      StreamTemplate stream = StreamManager.getInstance().getStreamByChannelName(streamName);
      if (stream == null) {
         String errorMsg;
         if (Config.ALLOW_STREAM_GAME_NAME) {
            ServerMessage msg = new ServerMessage("Twitch.CantConnect.NotExistOfflineOrWrongGame", player.getLang());
            msg.add(streamName);
            msg.add(Config.TWITCH_CORRECT_STREAM_GAME);
            errorMsg = msg.toString();
         } else {
            ServerMessage msg = new ServerMessage("Twitch.CantConnect.NotExistOrOffline", player.getLang());
            msg.add(streamName);
            errorMsg = msg.toString();
         }

         this.useStreamBypass(player, "errorConnectToStream", errorMsg);
         if (Config.ALLOW_STREAM_LOGS) {
            LogRecord record = new LogRecord(Level.INFO, "" + player + " tried to connect to Stream: " + streamName + ". Such Stream doesn't exist!");
            record.setLoggerName("twitch");
            _logTwitch.log(record);
         }
      } else if (stream.getAttachedPlayerId() > 0) {
         String errorMsg;
         if (stream.getAttachedPlayerServer().equalsIgnoreCase(Config.SERVER_NAME)) {
            errorMsg = ServerStorage.getInstance().getString(player.getLang(), "Twitch.CantConnect.AttachedSameServer");
         } else {
            ServerMessage msg = new ServerMessage("Twitch.CantConnect.AttachedOtherServer", player.getLang());
            msg.add(Config.SERVER_NAME);
            errorMsg = msg.toString();
         }

         this.useStreamBypass(player, "errorConnectToStream", errorMsg);
         if (Config.ALLOW_STREAM_LOGS) {
            LogRecord record = new LogRecord(
               Level.INFO,
               ""
                  + player
                  + " tried to connect to Stream: "
                  + streamName
                  + ". It is attached already to "
                  + stream.getAttachedPlayerId()
                  + " on SubServer: "
                  + stream.getAttachedPlayerServer()
                  + "!"
            );
            record.setLoggerName("twitch");
            _logTwitch.log(record);
         }
      } else {
         if (Config.ALLOW_STREAM_NEED_APPROVAL) {
            stream.addIdToApprove(player.getObjectId());
            if (Config.ALLOW_STREAM_SAVE_DB) {
               StreamingDAO.updateStream(stream);
            }

            player.sendMessage(ServerStorage.getInstance().getString(player.getLang(), "Twitch.ConnectSuccessWaitApproval"));
            if (Config.ALLOW_STREAM_LOGS) {
               LogRecord record = new LogRecord(Level.INFO, "" + player + " attached to Stream: " + stream.getChannelName() + ". Waiting for Approval!");
               record.setLoggerName("twitch");
               _logTwitch.log(record);
            }
         } else {
            stream.setAttachedPlayerId(player.getObjectId(), Config.SERVER_NAME);
            if (Config.ALLOW_STREAM_SAVE_DB) {
               StreamingDAO.updateStream(stream);
            }

            player.sendMessage(ServerStorage.getInstance().getString(player.getLang(), "Twitch.ConnectSuccess"));
            if (Config.ALLOW_STREAM_LOGS) {
               LogRecord record = new LogRecord(Level.INFO, "Correctly " + player + " attached to Stream: " + stream.getChannelName() + ".");
               record.setLoggerName("twitch");
               _logTwitch.log(record);
            }
         }

         this.useStreamBypass(player, "mainStreamer");
      }
   }

   private void tryTransferChannel(Player player, String newCharName) {
      StreamTemplate stream = StreamManager.getInstance().getMyStream(player);
      if (stream == null) {
         this.useStreamBypass(player, "errorTransferStream", ServerStorage.getInstance().getString(player.getLang(), "Twitch.CantTransfer.NoStream"));
         if (Config.ALLOW_STREAM_LOGS) {
            LogRecord record = new LogRecord(Level.INFO, "" + player + " WITHOUT stream tried to transfer it to other character(" + newCharName + ")!");
            record.setLoggerName("twitch");
            _logTwitch.log(record);
         }
      } else if (newCharName.isEmpty()) {
         this.useStreamBypass(player, "errorTransferStream", ServerStorage.getInstance().getString(player.getLang(), "Twitch.CantTransfer.CharNameNotFilled"));
         if (Config.ALLOW_STREAM_LOGS) {
            LogRecord record = new LogRecord(Level.INFO, "" + player + " tried to transfer " + stream + " to Character with Empty Name!");
            record.setLoggerName("twitch");
            _logTwitch.log(record);
         }
      } else {
         Player onlineNewChar = World.getInstance().getPlayer(newCharName);
         if (onlineNewChar == null) {
            int newCharId = CharNameHolder.getInstance().getIdByName(newCharName);
            if (newCharId <= 0) {
               this.useStreamBypass(
                  player, "errorTransferStream", ServerStorage.getInstance().getString(player.getLang(), "Twitch.CantTransfer.NewCharNotExists")
               );
               if (Config.ALLOW_STREAM_LOGS) {
                  LogRecord record = new LogRecord(
                     Level.INFO, "" + player + " tried to transfer " + stream + " to Non Existing Char with Name: " + newCharName + "!"
                  );
                  record.setLoggerName("twitch");
                  _logTwitch.log(record);
               }
            } else {
               stream.setAttachedPlayerId(newCharId, Config.SERVER_NAME);
               if (Config.ALLOW_STREAM_SAVE_DB) {
                  StreamingDAO.updateStream(stream);
               }

               String exactName = CharNameHolder.getInstance().getNameById(newCharId);
               ServerMessage msg = new ServerMessage("Twitch.TransferredToOffline", player.getLang());
               msg.add(exactName);
               player.sendMessage(msg.toString());
               if (Config.ALLOW_STREAM_LOGS) {
                  LogRecord record = new LogRecord(Level.INFO, "" + player + " transferred " + stream + " to New Offline Character: " + exactName);
                  record.setLoggerName("twitch");
                  _logTwitch.log(record);
               }
            }
         } else {
            stream.setAttachedPlayerId(onlineNewChar.getObjectId(), Config.SERVER_NAME);
            if (Config.ALLOW_STREAM_SAVE_DB) {
               StreamingDAO.updateStream(stream);
            }

            ServerMessage msg = new ServerMessage("Twitch.TransferredToYou", onlineNewChar.getLang());
            msg.add(stream.getChannelName());
            onlineNewChar.sendMessage(msg.toString());
            ServerMessage msgg = new ServerMessage("Twitch.TransferredToOnline", player.getLang());
            msgg.add(onlineNewChar.getName());
            player.sendMessage(msgg.toString());
            if (Config.ALLOW_STREAM_LOGS) {
               LogRecord record = new LogRecord(Level.INFO, "" + player + " transferred " + stream + " to New Online Character: " + onlineNewChar.getName());
               record.setLoggerName("twitch");
               _logTwitch.log(record);
            }
         }
      }
   }

   private static void askDisconnectFromStream(Player player) {
      player.sendConfirmDlg(new DisconnectStreamAnswer(player), 60000, new ServerMessage("Twitch.AskDisconnect", player.getLang()).toString());
   }

   private static void checkReward(Player player) {
      if (Config.ALLOW_REWARD_STREAMERS) {
         if (Config.TIME_TO_REWARD_STREAMERS < 60) {
            _log.warning("Config StreamingSecondsToReward has too low value!!!");
         } else {
            StreamTemplate stream = StreamManager.getInstance().getMyStream(player);
            if (stream != null) {
               boolean rewarded = false;

               while(stream.getNotRewardedSeconds() >= (long)Config.TIME_TO_REWARD_STREAMERS) {
                  int highestViewersReached = -1;
                  String[] propertySplit = Config.STREAMING_REWARDS.split(";");

                  for(String rewards : propertySplit) {
                     String[] reward = rewards.split(",");
                     if (reward.length == 3) {
                        int minViewers = Integer.parseInt(reward[0]);
                        if (stream.getViewersCount() >= minViewers && minViewers > highestViewersReached) {
                           highestViewersReached = minViewers;
                        }
                     }
                  }

                  if (highestViewersReached >= 0) {
                     for(String rewards : propertySplit) {
                        String[] reward = rewards.split(",");
                        if (reward.length == 3 && highestViewersReached == Integer.parseInt(reward[0])) {
                           player.addItem("Streaming Reward", Integer.parseInt(reward[1]), Long.parseLong(reward[2]), null, true);
                        }
                     }
                  }

                  stream.setNotRewardedSeconds(stream.getNotRewardedSeconds() - (long)Config.TIME_TO_REWARD_STREAMERS);
                  rewarded = true;
                  if (Config.ALLOW_STREAM_LOGS) {
                     LogRecord record = new LogRecord(
                        Level.INFO,
                        "Correctly "
                           + player
                           + " received reward for Streaming Channel "
                           + stream.getChannelName()
                           + " with "
                           + stream.getViewersCount()
                           + " viewers."
                     );
                     record.setLoggerName("twitch");
                     _logTwitch.log(record);
                  }
               }

               if (rewarded) {
                  player.sendMessage(new ServerMessage("Twitch.Rewarded", player.getLang()).toString());
                  StreamingDAO.updateStream(stream);
               }
            }
         }
      }
   }

   @Override
   public String[] getVoicedCommandList() {
      return VOICED_COMMANDS;
   }
}
