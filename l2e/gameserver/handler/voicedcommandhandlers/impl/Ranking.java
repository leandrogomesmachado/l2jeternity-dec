package l2e.gameserver.handler.voicedcommandhandlers.impl;

import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.model.CommandChannel;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.player.ranking.PartyTemplate;

public class Ranking implements IVoicedCommandHandler {
   private static final String[] VOICED_COMMANDS = new String[]{"rank", "rk", "partyRank"};

   @Override
   public boolean useVoicedCommand(String command, Player activeChar, String params) {
      if (!activeChar.isInParty() || !Config.ALLOW_PARTY_RANK_COMMAND) {
         return false;
      } else if (activeChar.getParty().getCommandChannel() == null && Config.ALLOW_PARTY_RANK_ONLY_FOR_CC) {
         return false;
      } else {
         if (!command.equals("rank") && !command.equals("rk")) {
            if (command.startsWith("partyRank")) {
               String[] param = command.split(" ");
               if (param.length == 2 && param[1] != null) {
                  int objectId = Integer.parseInt(param[1]);
                  Player player = World.getInstance().getPlayer(objectId);
                  if (player != null) {
                     this.showPartyInfo(activeChar, player);
                     return true;
                  }

                  this.useVoicedCommand("rank", activeChar, params);
                  return true;
               }

               this.useVoicedCommand("rank", activeChar, params);
               return true;
            }
         } else if (activeChar.getParty().getCommandChannel() != null) {
            this.showCCInfo(activeChar);
         } else {
            this.showPartyInfo(activeChar, activeChar);
         }

         return true;
      }
   }

   public void showCCInfo(Player activeChar) {
      CommandChannel channel = activeChar.getParty().getCommandChannel();
      if (channel != null) {
         String html = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/mods/ranking/channel_info.htm");
         String template = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/mods/ranking/channel_template.htm");
         String block = "";
         String list = "";
         int totalKills = 0;
         int totalDeaths = 0;
         int count = 0;

         for(Party pt : channel.getPartys()) {
            if (pt != null) {
               int kills = 0;
               int deaths = 0;

               for(Player pl : pt.getMembers()) {
                  PartyTemplate tpl = pt.getMemberRank(pl);
                  if (tpl != null) {
                     kills += tpl.getKills();
                     deaths = (int)((long)deaths + tpl.getDeaths());
                  }
               }

               totalKills += kills;
               totalDeaths += deaths;
               block = template.replace("{color}", count % 2 == 1 ? "22211d" : "1b1a15");
               block = block.replace("{name}", pt.getLeader().getName());
               block = block.replace("{party}", String.valueOf(pt.getMemberCount()));
               block = block.replace("{kills}", String.valueOf(kills));
               block = block.replace("{deaths}", String.valueOf(deaths));
               block = block.replace("{bypass}", "bypass -h voiced_partyRank " + pt.getLeader().getObjectId() + "");
               ++count;
               list = list + block;
            }
         }

         html = html.replace("{list}", list);
         html = html.replace("{totalKills}", String.valueOf(totalKills));
         html = html.replace("{totalDeaths}", String.valueOf(totalDeaths));
         html = html.replace("{leader}", channel.getLeader().getName());
         html = html.replace("{partyes}", String.valueOf(channel.getPartys().size()));
         html = html.replace("{members}", String.valueOf(channel.getMembers().size()));
         Util.setHtml(html, activeChar);
      }
   }

   public void showPartyInfo(Player player, Player activeChar) {
      Party party = activeChar.getParty();
      if (party == null) {
         this.useVoicedCommand("rank", player, null);
      } else {
         String html = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/mods/ranking/party_info.htm");
         String template = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/mods/ranking/party_template.htm");
         String block = "";
         String list = "";
         int totalKills = 0;
         int totalDeaths = 0;
         int count = 0;

         for(Player pl : party.getMembers()) {
            if (pl != null) {
               PartyTemplate tpl = party.getMemberRank(pl);
               if (tpl != null) {
                  totalKills += tpl.getKills();
                  totalDeaths = (int)((long)totalDeaths + tpl.getDeaths());
                  block = template.replace("{color}", count % 2 == 1 ? "22211d" : "1b1a15");
                  block = block.replace("{name}", pl.getName());
                  block = block.replace("{kills}", String.valueOf(tpl.getKills()));
                  block = block.replace("{deaths}", String.valueOf(tpl.getDeaths()));
                  ++count;
                  list = list + block;
               }
            }
         }

         html = html.replace("{list}", list);
         html = html.replace("{totalKills}", String.valueOf(totalKills));
         html = html.replace("{totalDeaths}", String.valueOf(totalDeaths));
         html = html.replace("{leader}", party.getLeader().getName());
         html = html.replace(
            "{bypass}",
            party.getCommandChannel() != null
               ? "<button value=\"Back\" action=\"bypass -h voiced_rank\" width=\"75\" height=\"26\" back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">"
               : "&nbsp;"
         );
         html = html.replace("{refresh}", "bypass -h voiced_partyRank " + party.getLeader().getObjectId() + "");
         html = html.replace("{members}", String.valueOf(party.getMemberCount()));
         Util.setHtml(html, player);
      }
   }

   @Override
   public String[] getVoicedCommandList() {
      return VOICED_COMMANDS;
   }
}
