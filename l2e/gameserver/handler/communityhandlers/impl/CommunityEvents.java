package l2e.gameserver.handler.communityhandlers.impl;

import java.util.List;
import java.util.StringTokenizer;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.data.parser.FightEventParser;
import l2e.gameserver.handler.communityhandlers.ICommunityBoardHandler;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.events.AbstractFightEvent;
import l2e.gameserver.model.entity.events.model.FightEventManager;
import l2e.gameserver.model.entity.events.model.FightLastStatsManager;
import l2e.gameserver.model.entity.events.model.template.FightEventLastPlayerStats;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.model.strings.server.ServerStorage;

public class CommunityEvents extends AbstractCommunity implements ICommunityBoardHandler {
   public CommunityEvents() {
      if (Config.DEBUG) {
         _log.info(this.getClass().getSimpleName() + ": Loading all functions.");
      }
   }

   @Override
   public String[] getBypassCommands() {
      return new String[]{"_bbsgetfav", "_bbsaddfav", "_bbsdelfav", "_bbsevent", "_bbseventUnregister", "_bbseventRegister", "_bbsfightList"};
   }

   @Override
   public void onBypassCommand(String command, Player player) {
      if (!Config.ALLOW_FIGHT_EVENTS) {
         player.sendMessage(new ServerMessage("Community.ALL_DISABLE", player.getLang()).toString());
      } else {
         StringTokenizer st = new StringTokenizer(command, "_");
         String cmd = st.nextToken();
         if (!"bbsevent".equals(cmd) && !"bbsgetfav".equals(cmd)) {
            if ("bbsfightList".equals(cmd)) {
               int page = Integer.parseInt(st.nextToken());
               String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/events.htm");
               String template = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/event-template.htm");
               String block = "";
               String list = "";
               int perpage = 1;
               int counter = 0;
               int totalSize = FightEventParser.getInstance().getAcviteEvents().size();
               boolean isThereNextPage = totalSize > 1;

               for(int i = (page - 1) * 1; i < totalSize; ++i) {
                  AbstractFightEvent event = FightEventParser.getInstance().getEvent(FightEventParser.getInstance().getAcviteEvents().get(i));
                  if (event != null) {
                     block = template.replace("%eventIcon%", event.getIcon());
                     block = block.replace("%eventName%", player.getEventName(event.getId()));
                     block = block.replace("%eventDesc%", player.getEventDescr(event.getId()));
                     String register;
                     if (!FightEventManager.getInstance().isRegistrationOpened(event)) {
                        register = "<font color=\"FF0000\">" + ServerStorage.getInstance().getString(player.getLang(), "FightEvents.REG_CLOSE") + "</font>";
                     } else if (FightEventManager.getInstance().isPlayerRegistered(player, event.getId())) {
                        register = "<button value=\""
                           + ServerStorage.getInstance().getString(player.getLang(), "FightEvents.UNREG_COMPLETE")
                           + "\" action=\"bypass _bbseventUnregister_"
                           + event.getId()
                           + "\" back=\"L2UI_CT1.OlympiadWnd_DF_Fight3None_Down\" width=200 height=30 fore=\"L2UI_CT1.OlympiadWnd_DF_Fight3None\">";
                     } else if (player.getFightEventGameRoom() != null) {
                        register = "<font color=\"FF0000\">" + ServerStorage.getInstance().getString(player.getLang(), "FightEvents.AREADY_REG") + "</font>";
                     } else {
                        register = "<button value=\""
                           + ServerStorage.getInstance().getString(player.getLang(), "FightEvents.REG_COMPLETE")
                           + "\" action=\"bypass _bbseventRegister_"
                           + event.getId()
                           + "\" back=\"L2UI_CT1.OlympiadWnd_DF_Fight3None_Down\" width=200 height=30 fore=\"L2UI_CT1.OlympiadWnd_DF_Fight3None\">";
                     }

                     block = block.replace("%eventRegister%", register);
                     block = getEventStats(event.getId(), block, player);
                     list = list + block;
                  }

                  if (++counter >= 1) {
                     break;
                  }
               }

               int count = (int)Math.ceil((double)totalSize / 1.0);
               html = html.replace("{list}", list);
               html = html.replace("{navigation}", Util.getNavigationBlock(count, page, totalSize, 1, isThereNextPage, "_bbsfightList_%s"));
               separateAndSend(html, player);
            } else if ("bbseventUnregister".equals(cmd)) {
               int eventId = Integer.parseInt(st.nextToken());
               FightEventManager.getInstance().unsignFromEvent(player, eventId);
               this.onBypassCommand("_bbsfightList_" + eventId, player);
            } else if ("bbseventRegister".equals(cmd)) {
               int eventId = Integer.parseInt(st.nextToken());
               AbstractFightEvent event = FightEventManager.getInstance().getEventById(eventId);
               if (event != null) {
                  FightEventManager.getInstance().trySignForEvent(player, event, true);
               }

               this.onBypassCommand("_bbsfightList_" + eventId, player);
            }
         } else {
            AbstractFightEvent event = FightEventManager.getInstance().getNextEvent();
            if (event != null) {
               this.onBypassCommand("_bbsfightList_" + this.getAcviteEvent(event.getId()), player);
            } else {
               this.onBypassCommand("_bbsfightList_1", player);
            }
         }
      }
   }

   private int getAcviteEvent(int eventId) {
      int id = 0;

      for(int i = 0; i < FightEventParser.getInstance().getAcviteEvents().size(); ++i) {
         AbstractFightEvent event = FightEventParser.getInstance().getEvent(FightEventParser.getInstance().getAcviteEvents().get(i));
         if (event != null && event.getId() == eventId) {
            id = i;
         }
      }

      return id + 1;
   }

   private static String getEventStats(int eventId, String html, Player player) {
      List<FightEventLastPlayerStats> stats = FightLastStatsManager.getInstance().getStats(eventId, true);

      for(int i = 0; i < 10; ++i) {
         if (i + 1 <= stats.size()) {
            FightEventLastPlayerStats stat = stats.get(i);
            if (stat.isMyStat(player)) {
               html = html.replace("<?name_" + i + "?>", "<fonr color=\"CC3333\">" + stat.getPlayerName() + "</font>");
               html = html.replace("<?count_" + i + "?>", "<fonr color=\"CC3333\">" + Util.formatAdena((long)stat.getScore()) + "</font>");
               html = html.replace("<?class_" + i + "?>", "<fonr color=\"CC3333\">" + Util.className(player, stat.getClassId()) + "</font>");
               html = html.replace("<?clan_" + i + "?>", "<fonr color=\"CC3333\">" + stat.getClanName() + "</font>");
               html = html.replace("<?ally_" + i + "?>", "<fonr color=\"CC3333\">" + stat.getAllyName() + "</font>");
            } else {
               html = html.replace("<?name_" + i + "?>", stat.getPlayerName());
               html = html.replace("<?count_" + i + "?>", Util.formatAdena((long)stat.getScore()));
               html = html.replace("<?class_" + i + "?>", Util.className(player, stat.getClassId()));
               html = html.replace("<?clan_" + i + "?>", stat.getClanName());
               html = html.replace("<?ally_" + i + "?>", stat.getAllyName());
            }
         } else {
            html = html.replace("<?name_" + i + "?>", "...");
            html = html.replace("<?count_" + i + "?>", "...");
            html = html.replace("<?class_" + i + "?>", "...");
            html = html.replace("<?clan_" + i + "?>", "...");
            html = html.replace("<?ally_" + i + "?>", "...");
         }
      }

      FightEventLastPlayerStats my = FightLastStatsManager.getInstance().getMyStat(eventId, player);
      if (my != null) {
         html = html.replace("<?name_me?>", "<fonr color=\"CC3333\">" + my.getPlayerName() + "</font>");
         html = html.replace("<?count_me?>", "<fonr color=\"CC3333\">" + Util.formatAdena((long)my.getScore()) + "</font>");
         html = html.replace("<?class_me?>", "<fonr color=\"CC3333\">" + Util.className(player, my.getClassId()) + "</font>");
         html = html.replace("<?clan_me?>", "<fonr color=\"CC3333\">" + my.getClanName() + "</font>");
         html = html.replace("<?ally_me?>", "<fonr color=\"CC3333\">" + my.getAllyName() + "</font>");
      } else {
         html = html.replace("<?name_me?>", "...");
         html = html.replace("<?count_me?>", "...");
         html = html.replace("<?class_me?>", "...");
         html = html.replace("<?clan_me?>", "...");
         html = html.replace("<?ally_me?>", "...");
      }

      return html;
   }

   @Override
   public void onWriteCommand(String command, String ar1, String ar2, String ar3, String ar4, String ar5, Player activeChar) {
   }

   public static CommunityEvents getInstance() {
      return CommunityEvents.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final CommunityEvents _instance = new CommunityEvents();
   }
}
