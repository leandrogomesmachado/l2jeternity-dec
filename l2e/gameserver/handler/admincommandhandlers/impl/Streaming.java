package l2e.gameserver.handler.admincommandhandlers.impl;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;
import l2e.gameserver.Config;
import l2e.gameserver.data.dao.StreamingDAO;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.mods.streaming.StreamManager;
import l2e.gameserver.model.entity.mods.streaming.StreamTemplate;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.model.strings.server.ServerStorage;
import l2e.gameserver.network.serverpackets.CreatureSay;

public class Streaming implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{"admin_punish_stream", "admin_active_streams"};

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      if (!Config.ALLOW_STREAM_SYSTEM) {
         return false;
      } else {
         if (command.equalsIgnoreCase("admin_active_streams")) {
            List<StreamTemplate> activeStreams = StreamManager.getInstance().getAllActiveStreamsCopy();
            long currentDate = System.currentTimeMillis();
            boolean rewardWhilePlayerOffline = Config.ALLOW_INCREASE_REWARD;
            long maxSecondsPerDay = (long)Config.MAX_SEC_TO_REWARD_STREAMERS;

            for(StreamTemplate activeStream : activeStreams) {
               if (StreamManager.getInstance().isStreamActive(activeStream)
                  && !activeStream.isNowPunished(currentDate)
                  && activeStream.getTotalRewardedSecondsToday() < maxSecondsPerDay) {
                  Player activeStreamPlayer = activeStream.getStreamingPlayer();
                  if (activeStreamPlayer != null && !activeStreamPlayer.isOnline() && StreamManager.isPlayerActive(activeStreamPlayer, currentDate)
                     || rewardWhilePlayerOffline) {
                     if (activeStreamPlayer == null) {
                        activeChar.sendMessage("Active Stream: " + activeStream.getChannelName());
                     } else {
                        activeChar.sendMessage("Active Stream: " + activeStream.getChannelName() + " Player: " + activeStreamPlayer.toString());
                     }
                  }
               }
            }
         } else if (command.startsWith("admin_punish_stream")) {
            StringTokenizer st = new StringTokenizer(command, " ");

            try {
               st.nextToken();
               String channelName = st.nextToken();
               int hours = 0;

               try {
                  hours = Integer.parseInt(st.nextToken());
               } catch (NoSuchElementException var12) {
               }

               StreamTemplate stream = StreamManager.getInstance().getStreamByChannelName(channelName);
               if (stream == null) {
                  activeChar.sendMessage("Stream with such name was not found!");
                  return false;
               }

               punishStream(stream, (long)hours);
            } catch (Exception var13) {
            }
         }

         return true;
      }
   }

   private static void punishStream(StreamTemplate stream, long hours) {
      stream.setPunishedUntilDate(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(hours));
      StreamingDAO.updateStream(stream);
      Player activeStreamer = stream.getStreamingPlayer();
      if (activeStreamer != null) {
         ServerMessage msg = new ServerMessage("Twitch.PunishedForHours", activeStreamer.getLang());
         msg.add(hours);
         activeStreamer.sendPacket(new CreatureSay(0, 15, ServerStorage.getInstance().getString(activeStreamer.getLang(), "Twitch.Stream"), msg.toString()));
      }
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }
}
