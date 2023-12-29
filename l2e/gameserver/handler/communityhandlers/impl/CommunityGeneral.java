package l2e.gameserver.handler.communityhandlers.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Map.Entry;
import l2e.commons.util.TimeUtils;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.GameServer;
import l2e.gameserver.GameTimeController;
import l2e.gameserver.data.dao.CharacterPremiumDAO;
import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.data.parser.AugmentationParser;
import l2e.gameserver.data.parser.BuyListParser;
import l2e.gameserver.data.parser.DonationParser;
import l2e.gameserver.data.parser.ExchangeItemParser;
import l2e.gameserver.data.parser.ExperienceParser;
import l2e.gameserver.data.parser.InitialShortcutParser;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.data.parser.MultiSellParser;
import l2e.gameserver.data.parser.OptionsParser;
import l2e.gameserver.data.parser.PremiumAccountsParser;
import l2e.gameserver.data.parser.SkillTreesParser;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.handler.communityhandlers.ICommunityBoardHandler;
import l2e.gameserver.handler.communityhandlers.impl.model.CertificationUtils;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.handler.voicedcommandhandlers.VoicedCommandHandler;
import l2e.gameserver.instancemanager.PunishmentManager;
import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.Augmentation;
import l2e.gameserver.model.ClanMember;
import l2e.gameserver.model.Elementals;
import l2e.gameserver.model.SkillLearn;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.listener.LevelAnswerListener;
import l2e.gameserver.model.actor.stat.PcStat;
import l2e.gameserver.model.actor.templates.items.Henna;
import l2e.gameserver.model.actor.templates.items.Weapon;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.base.PlayerClass;
import l2e.gameserver.model.base.Race;
import l2e.gameserver.model.base.SubClass;
import l2e.gameserver.model.entity.events.cleft.AerialCleftEvent;
import l2e.gameserver.model.items.buylist.ProductList;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.olympiad.Olympiad;
import l2e.gameserver.model.punishment.PunishmentAffect;
import l2e.gameserver.model.punishment.PunishmentType;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.service.donate.Attribution;
import l2e.gameserver.model.service.donate.DonateItem;
import l2e.gameserver.model.service.donate.Donation;
import l2e.gameserver.model.service.donate.Enchant;
import l2e.gameserver.model.service.donate.FoundList;
import l2e.gameserver.model.service.donate.SimpleList;
import l2e.gameserver.model.service.exchange.Change;
import l2e.gameserver.model.service.exchange.Variant;
import l2e.gameserver.model.service.premium.PremiumPrice;
import l2e.gameserver.model.service.premium.PremiumTemplate;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.options.Options;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.model.strings.server.ServerStorage;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.BuyList;
import l2e.gameserver.network.serverpackets.ExBuySellList;
import l2e.gameserver.network.serverpackets.ExShowVariationCancelWindow;
import l2e.gameserver.network.serverpackets.ExShowVariationMakeWindow;
import l2e.gameserver.network.serverpackets.ExStorageMaxCount;
import l2e.gameserver.network.serverpackets.ExVariationCancelResult;
import l2e.gameserver.network.serverpackets.HennaEquipList;
import l2e.gameserver.network.serverpackets.HennaUnequipList;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.ShortCutInit;
import l2e.gameserver.network.serverpackets.ShowBoard;
import l2e.gameserver.network.serverpackets.SocialAction;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.network.serverpackets.WareHouseDepositList;
import l2e.gameserver.network.serverpackets.WareHouseWithdrawList;

public class CommunityGeneral extends AbstractCommunity implements ICommunityBoardHandler {
   private static final String[] _vars = new String[]{"FOUNDATION", "ENCHANT", "ATTRIBUTION"};
   private static final int[] CLASSITEMS = new int[]{10281, 10282, 10283, 10287, 10284, 10286, 10285};
   private static final int[] TRANSFORMITEMS = new int[]{10289, 10288, 10290, 10293, 10292, 10294, 10291};
   private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");

   public CommunityGeneral() {
      if (Config.DEBUG) {
         _log.info(this.getClass().getSimpleName() + ": Loading all functions.");
      }
   }

   @Override
   public String[] getBypassCommands() {
      return new String[]{
         "_bbshome",
         "_bbsabout",
         "_bbspage",
         "_bbshtm",
         "_bbsmod",
         "_bbsvoice",
         "_bbsmultisell;",
         "_bbsmsell;",
         "_bbsExcMultisell;",
         "_bbsExcMsell;",
         "_bbslistclanskills",
         "_bbslearnclanskills",
         "_bbspremium",
         "_bbspremiumPage",
         "_bbspremiumOnlyPage",
         "_bbspremiumBuy",
         "_bbspremiumList",
         "_bbswarhouse",
         "_bbsAugment",
         "_bbsdraw",
         "_bbsservice",
         "_bbsunban"
      };
   }

   @Override
   public void onBypassCommand(String command, Player activeChar) {
      if (command.equals("_bbshome")) {
         String html = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/community/index.htm");
         html = html.replace("%nick%", String.valueOf(activeChar.getName().toString()));
         html = html.replace("%account_name%", activeChar.getAccountName());
         html = html.replace("%name_server%", Config.SERVER_NAME);
         html = html.replace("%clan_count%", String.valueOf(ClanHolder.getInstance().getClans().length));
         html = html.replace("%player_name%", String.valueOf(activeChar.getName()));
         html = html.replace("%player_class%", Util.className(activeChar, activeChar.getClassId().getId()));
         html = html.replace("%player_level%", String.valueOf(activeChar.getLevel()));
         html = html.replace(
            "%player_clan%",
            String.valueOf(
               activeChar.getClan() != null
                  ? activeChar.getClan().getName()
                  : "<font color=\"FF0000\">" + ServerStorage.getInstance().getString(activeChar.getLang(), "Util.FALSE") + "</font>"
            )
         );
         html = html.replace(
            "%player_noobless%",
            String.valueOf(
               activeChar.isNoble()
                  ? "<font color=\"18FF00\">" + ServerStorage.getInstance().getString(activeChar.getLang(), "Util.TRUE") + "</font>"
                  : "<font color=\"ff6755\">" + ServerStorage.getInstance().getString(activeChar.getLang(), "Util.FALSE") + "</font>"
            )
         );
         html = html.replace("%online_time%", TimeUtils.formatTime(activeChar, (int)activeChar.getTotalOnlineTime(), false));
         html = html.replace("%player_ip%", activeChar.getIPAddress());
         html = html.replace("%mytime%", this.getTimeInServer(activeChar));
         html = html.replace("%player_premium%", this.getStatus(activeChar));
         html = html.replace("%premium_status%", this.getPremiumStatus(activeChar));
         html = html.replace(
            "%rate_xp%", String.valueOf("" + (double)((int)Config.RATE_XP_BY_LVL[activeChar.getLevel()]) * activeChar.getPremiumBonus().getRateXp() + "")
         );
         html = html.replace(
            "%rate_sp%", String.valueOf("" + (double)((int)Config.RATE_SP_BY_LVL[activeChar.getLevel()]) * activeChar.getPremiumBonus().getRateSp() + "")
         );
         html = html.replace("%rate_adena%", String.valueOf("" + (double)((int)Config.RATE_DROP_ADENA) * activeChar.getPremiumBonus().getDropAdena() + ""));
         html = html.replace("%rate_items%", String.valueOf("" + (double)((int)Config.RATE_DROP_ITEMS) * activeChar.getPremiumBonus().getDropItems() + ""));
         html = html.replace("%rate_spoil%", String.valueOf("" + (double)((int)Config.RATE_DROP_SPOIL) * activeChar.getPremiumBonus().getDropSpoil() + ""));
         html = html.replace(
            "%rate_quest%", String.valueOf("" + (double)((int)Config.RATE_QUEST_REWARD) * activeChar.getPremiumBonus().getQuestRewardRate() + "")
         );
         html = html.replace(
            "%rate_siege%", String.valueOf("" + (double)((int)Config.RATE_DROP_SIEGE_GUARD) * activeChar.getPremiumBonus().getDropSiege() + "")
         );
         html = html.replace("%rate_manor%", String.valueOf("" + Config.RATE_DROP_MANOR + ""));
         html = html.replace("%rate_hellbound%", String.valueOf("" + Config.RATE_HB_TRUST_INCREASE + ""));
         html = html.replace("%rate_reputation%", String.valueOf("" + (double)Config.REPUTATION_SCORE_PER_KILL + ""));
         html = html.replace(
            "%rate_fishing%", String.valueOf("" + (double)((int)Config.RATE_DROP_FISHING) * activeChar.getPremiumBonus().getFishingRate() + "")
         );
         html = html.replace(
            "%rate_raidboss%", String.valueOf("" + (double)((int)Config.RATE_DROP_RAIDBOSS) * activeChar.getPremiumBonus().getDropRaids() + "")
         );
         html = html.replace(
            "%rate_epicboss%", String.valueOf("" + (double)((int)Config.RATE_DROP_EPICBOSS) * activeChar.getPremiumBonus().getDropEpics() + "")
         );
         html = html.replace("%server_uptime%", String.valueOf(uptime()));
         html = html.replace("%time%", String.valueOf(time()));
         html = html.replace("%online%", this.online(false));
         html = html.replace("%offtrade%", this.online(true));
         separateAndSend(html, activeChar);
      } else if (command.equals("_bbsabout")) {
         String html = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/community/about.htm");
         html = html.replace("%rate_xp%", String.valueOf(Config.RATE_XP_BY_LVL[activeChar.getLevel()]));
         html = html.replace("%rate_sp%", String.valueOf(Config.RATE_SP_BY_LVL[activeChar.getLevel()]));
         html = html.replace("%rate_adena%", String.valueOf(Config.RATE_DROP_ADENA));
         html = html.replace("%rate_items%", String.valueOf(Config.RATE_DROP_ITEMS));
         html = html.replace("%rate_spoil%", String.valueOf(Config.RATE_DROP_SPOIL));
         html = html.replace("%rate_quest%", String.valueOf(Config.RATE_QUEST_REWARD));
         html = html.replace("%rate_siege%", String.valueOf(Config.RATE_DROP_SIEGE_GUARD));
         html = html.replace("%rate_manor%", String.valueOf(Config.RATE_DROP_MANOR));
         html = html.replace("%rate_hellbound%", String.valueOf(Config.RATE_HB_TRUST_INCREASE));
         html = html.replace("%rate_reputation%", String.valueOf((double)Config.REPUTATION_SCORE_PER_KILL));
         html = html.replace("%rate_fishing%", String.valueOf(Config.RATE_DROP_FISHING));
         html = html.replace("%rate_raidboss%", String.valueOf(Config.RATE_DROP_RAIDBOSS));
         html = html.replace(
            "%bonus_xp%",
            activeChar.getPremiumBonus().getRateXp() > 1.0
               ? "<font color=\"E6D0AE\" name=\"hs11\">(+" + cutOff((activeChar.getPremiumBonus().getRateXp() - 1.0) * 100.0, 0) + "%)</font>"
               : ""
         );
         html = html.replace(
            "%bonus_sp%",
            activeChar.getPremiumBonus().getRateSp() > 1.0
               ? "<font color=\"E6D0AE\" name=\"hs11\">(+" + cutOff((activeChar.getPremiumBonus().getRateSp() - 1.0) * 100.0, 0) + "%)</font>"
               : ""
         );
         html = html.replace(
            "%bonus_adena%",
            activeChar.getPremiumBonus().getDropAdena() > 1.0
               ? "<font color=\"E6D0AE\" name=\"hs11\">(+" + cutOff((activeChar.getPremiumBonus().getDropAdena() - 1.0) * 100.0, 0) + "%)</font>"
               : ""
         );
         html = html.replace(
            "%bonus_items%",
            activeChar.getPremiumBonus().getDropItems() > 1.0
               ? "<font color=\"E6D0AE\" name=\"hs11\">(+" + cutOff((activeChar.getPremiumBonus().getDropItems() - 1.0) * 100.0, 0) + "%)</font>"
               : ""
         );
         html = html.replace(
            "%bonus_spoil%",
            activeChar.getPremiumBonus().getDropSpoil() > 1.0
               ? "<font color=\"E6D0AE\" name=\"hs11\">(+" + cutOff((activeChar.getPremiumBonus().getDropSpoil() - 1.0) * 100.0, 0) + "%)</font>"
               : ""
         );
         html = html.replace(
            "%bonus_quest%",
            activeChar.getPremiumBonus().getQuestRewardRate() > 1.0
               ? "<font color=\"E6D0AE\" name=\"hs11\">(+" + cutOff((activeChar.getPremiumBonus().getQuestRewardRate() - 1.0) * 100.0, 0) + "%)</font>"
               : ""
         );
         html = html.replace(
            "%bonus_siege%",
            activeChar.getPremiumBonus().getDropSiege() > 1.0
               ? "<font color=\"E6D0AE\" name=\"hs11\">(+" + cutOff((activeChar.getPremiumBonus().getDropSiege() - 1.0) * 100.0, 0) + "%)</font>"
               : ""
         );
         html = html.replace(
            "%bonus_fishing%",
            activeChar.getPremiumBonus().getFishingRate() > 1.0
               ? "<font color=\"E6D0AE\" name=\"hs11\">(+" + cutOff((activeChar.getPremiumBonus().getFishingRate() - 1.0) * 100.0, 0) + "%)</font>"
               : ""
         );
         html = html.replace(
            "%bonus_raidboss%",
            activeChar.getPremiumBonus().getDropRaids() > 1.0
               ? "<font color=\"E6D0AE\" name=\"hs11\">(+" + cutOff((activeChar.getPremiumBonus().getDropRaids() - 1.0) * 100.0, 0) + "%)</font>"
               : ""
         );
         html = html.replace(
            "%bonus_epicboss%",
            activeChar.getPremiumBonus().getDropEpics() > 1.0
               ? "<font color=\"E6D0AE\" name=\"hs11\">(+" + cutOff((activeChar.getPremiumBonus().getDropEpics() - 1.0) * 100.0, 0) + "%)</font>"
               : ""
         );
         html = html.replace("%server_uptime%", String.valueOf(uptime()));
         html = html.replace("%time%", String.valueOf(time()));
         html = html.replace("%online%", this.online(false));
         html = html.replace("%offtrade%", this.online(true));
         separateAndSend(html, activeChar);
      } else if (command.startsWith("_bbspage")) {
         StringTokenizer st = new StringTokenizer(command, ":");
         st.nextToken();
         String page = st.nextToken();
         String html = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/community/" + page + ".htm");
         separateAndSend(html, activeChar);
      } else if (command.startsWith("_bbshtm")) {
         StringTokenizer st = new StringTokenizer(command, ":");
         st.nextToken();
         String page = st.nextToken();
         NpcHtmlMessage htm = new NpcHtmlMessage(5);
         htm.setFile(activeChar, activeChar.getLang(), "data/html/community/" + page + ".htm");
         activeChar.sendPacket(htm);
      } else if (command.startsWith("_bbsmod")) {
         StringTokenizer st = new StringTokenizer(command, ":");
         st.nextToken();
         String page = st.nextToken();
         NpcHtmlMessage htm = new NpcHtmlMessage(5);
         htm.setFile(activeChar, activeChar.getLang(), "data/html/mods/" + page + ".htm");
         activeChar.sendPacket(htm);
      } else if (command.startsWith("_bbsvoice")) {
         StringTokenizer st = new StringTokenizer(command, ":");
         st.nextToken();
         String voice_command = st.nextToken();
         IVoicedCommandHandler use_command = VoicedCommandHandler.getInstance().getHandler(voice_command);
         if (use_command != null) {
            use_command.useVoicedCommand(voice_command, activeChar, "");
            return;
         }
      } else if (command.startsWith("_bbspremiumPage")) {
         StringTokenizer st = new StringTokenizer(command, " ");
         st.nextToken();
         String page = null;

         try {
            page = st.nextToken();
         } catch (Exception var36) {
         }

         if (activeChar.hasPremiumBonus()) {
            String html = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/community/donate/premium.htm");
            PremiumTemplate tpl = PremiumAccountsParser.getInstance().getPremiumTemplate(activeChar.getPremiumBonus().getPremiumId());
            if (tpl != null) {
               String line = "";
               if (tpl.isOnlineType()) {
                  html = html.replace("%onlineType%", ServerStorage.getInstance().getString(activeChar.getLang(), "CommunityGeneral.PREMIUM_ONLINE"));
                  long lastTime = (
                        tpl.getTime() * 1000L
                           - (activeChar.getPremiumBonus().getOnlineTime() + (System.currentTimeMillis() - activeChar.getPremiumOnlineTime()))
                     )
                     / 1000L;
                  line = "<font color=\"E6D0AE\">" + TimeUtils.formatTime(activeChar, (int)lastTime, false) + "</font>";
               } else {
                  html = html.replace("%onlineType%", ServerStorage.getInstance().getString(activeChar.getLang(), "CommunityGeneral.PREMIUM_COMMON"));
                  long lastTime = (activeChar.getPremiumBonus().getOnlineTime() - System.currentTimeMillis()) / 1000L;
                  line = "<font color=\"E6D0AE\">" + TimeUtils.formatTime(activeChar, (int)lastTime, false) + "</font>";
               }

               html = html.replace("%timeLeft%", line);
               if (tpl.isPersonal()) {
                  html = html.replace("%isPersonal%", ServerStorage.getInstance().getString(activeChar.getLang(), "CommunityGeneral.RERSONAL"));
               } else {
                  html = html.replace("%isPersonal%", ServerStorage.getInstance().getString(activeChar.getLang(), "CommunityGeneral.ACCOUNT"));
               }
            }

            separateAndSend(html, activeChar);
            return;
         }

         this.checkFullPremiumList(activeChar, page != null ? Integer.parseInt(page) : 1);
      } else if (command.startsWith("_bbspremiumOnlyPage")) {
         StringTokenizer st = new StringTokenizer(command, " ");
         st.nextToken();
         String page = null;

         try {
            page = st.nextToken();
         } catch (Exception var35) {
         }

         this.checkFullPremiumListOnly(activeChar, page != null ? Integer.parseInt(page) : 1);
      } else if (command.startsWith("_bbspremiumList")) {
         StringTokenizer st = new StringTokenizer(command, " ");
         st.nextToken();
         int page = Integer.parseInt(st.nextToken());
         this.checkPremiumList(activeChar, page);
      } else if (command.startsWith("_bbspremiumBuy")) {
         StringTokenizer st = new StringTokenizer(command, " ");
         st.nextToken();
         int premiumId = Integer.parseInt(st.nextToken());
         int page = Integer.parseInt(st.nextToken());
         String type = null;

         try {
            type = st.nextToken();
         } catch (Exception var34) {
         }

         int typeInfo = type != null ? Integer.parseInt(type) : 0;
         if (activeChar.hasPremiumBonus() && !Config.PREMIUMSERVICE_DOUBLE) {
            activeChar.sendMessage(new ServerMessage("ServiceBBS.PREMIUM_MSG", activeChar.getLang()).toString());
            this.checkPremiumList(activeChar, page);
            return;
         }

         this.checkPremium(activeChar, premiumId, page, typeInfo);
      } else if (command.startsWith("_bbspremium")) {
         StringTokenizer st = new StringTokenizer(command, " ");
         st.nextToken();
         int premiumId = Integer.parseInt(st.nextToken());
         int page = Integer.parseInt(st.nextToken());
         String type = null;

         try {
            type = st.nextToken();
         } catch (Exception var33) {
         }

         int typeInfo = type != null ? Integer.parseInt(type) : 0;
         PremiumTemplate tpl = PremiumAccountsParser.getInstance().getPremiumTemplate(premiumId);
         if (tpl != null) {
            NpcHtmlMessage html = new NpcHtmlMessage(5);
            if (typeInfo == 0) {
               html.setFile(activeChar, activeChar.getLang(), "data/html/community/donate/premiumInfo.htm");
            } else {
               html.setFile(activeChar, activeChar.getLang(), "data/html/community/donate/buyPremiumInfo.htm");
            }

            html.replace("%name%", activeChar.getLang().equalsIgnoreCase("en") ? tpl.getNameEn() : tpl.getNameRu());
            html.replace("%icon%", tpl.getIcon());
            html.replace("%time%", TimeUtils.formatTime(activeChar, (int)tpl.getTime()));
            String priceLine = "<font color=99CC66>Cost:</font> ";

            for(PremiumPrice price : tpl.getPriceList()) {
               if (price != null) {
                  priceLine = priceLine + "" + Util.formatPay(activeChar, price.getCount(), price.getId()) + " ";
               }
            }

            html.replace("%price%", priceLine);
            html.replace("%link%", "bypass -h _bbspremiumBuy " + tpl.getId() + " " + page + " " + typeInfo);
            if (typeInfo == 0) {
               html.replace("%back%", "bypass _bbspremiumList " + page);
            } else if (typeInfo == 1) {
               html.replace("%back%", "bypass _bbspremiumPage " + page);
            } else {
               html.replace("%back%", "bypass _bbspremiumOnlyPage " + page);
            }

            html.replace("%xp%", "+" + cutOff((tpl.getExp() - 1.0) * 100.0, 0) + "%");
            html.replace("%sp%", "+" + cutOff((tpl.getSp() - 1.0) * 100.0, 0) + "%");
            html.replace("%adena%", "+" + cutOff((tpl.getAdena() - 1.0) * 100.0, 0) + "%");
            html.replace("%items%", "+" + cutOff((tpl.getItems() - 1.0) * 100.0, 0) + "%");
            html.replace("%raids%", "+" + cutOff((tpl.getDropRaids() - 1.0) * 100.0, 0) + "%");
            html.replace("%epics%", "+" + cutOff((tpl.getDropEpics() - 1.0) * 100.0, 0) + "%");
            html.replace("%elementStones%", "+" + cutOff((tpl.getElementStones() - 1.0) * 100.0, 0) + "%");
            html.replace("%spoil%", "+" + cutOff((tpl.getSpoil() - 1.0) * 100.0, 0) + "%");
            html.replace("%questReward%", "+" + cutOff((tpl.getQuestReward() - 1.0) * 100.0, 0) + "%");
            html.replace("%questDrop%", "+" + cutOff((tpl.getQuestDrop() - 1.0) * 100.0, 0) + "%");
            html.replace("%fishing%", "+" + cutOff((tpl.getFishing() - 1.0) * 100.0, 0) + "%");
            html.replace("%epaulette%", "+" + cutOff((tpl.getEpaulette() - 1.0) * 100.0, 0) + "%");
            html.replace("%weight%", "+" + cutOff((tpl.getWeight() - 1.0) * 100.0, 0) + "%");
            html.replace("%masterwork%", "+" + tpl.getMasterWorkChance() + "%");
            html.replace("%craft%", "+" + tpl.getCraftChance() + "%");
            html.replace("%enchant%", "+" + tpl.getEnchantChance() + "%");
            html.replace("%fame%", "+" + cutOff((tpl.getFameBonus() - 1.0) * 100.0, 0) + "%");
            html.replace("%reflection%", "" + cutOff((tpl.getReflectionReduce() - 1.0) * 100.0, 0) + "%");
            if (tpl.isOnlineType()) {
               html.replace("%onlineType%", "<font color=LEVEL>Premium is only spent when you are online!</font>");
            } else {
               html.replace("%onlineType%", "&nbsp;");
            }

            if (tpl.isPersonal()) {
               html.replace("%isPersonal%", "(<font color=\"b02e31\">Personal Type</font>)");
            } else {
               html.replace("%isPersonal%", "&nbsp;");
            }

            html.replace("%xp_f%", String.valueOf(cutOff(Config.RATE_XP_BY_LVL[activeChar.getLevel()] * tpl.getExp(), 2)));
            html.replace("%sp_f%", String.valueOf(cutOff(Config.RATE_SP_BY_LVL[activeChar.getLevel()] * tpl.getSp(), 2)));
            html.replace("%adena_f%", String.valueOf(cutOff(Config.RATE_DROP_ADENA * tpl.getAdena(), 2)));
            html.replace("%items_f%", String.valueOf(cutOff(Config.RATE_DROP_ITEMS * tpl.getItems(), 2)));
            html.replace("%raids_f%", String.valueOf(cutOff(Config.RATE_DROP_RAIDBOSS * tpl.getDropRaids(), 2)));
            html.replace("%epics_f%", String.valueOf(cutOff(Config.RATE_DROP_EPICBOSS * tpl.getDropEpics(), 2)));
            html.replace("%elementStones_f%", String.valueOf(cutOff(Config.RATE_DROP_ITEMS * tpl.getItems() * tpl.getElementStones(), 2)));
            html.replace("%spoil_f%", String.valueOf(cutOff(Config.RATE_DROP_SPOIL * tpl.getSpoil(), 2)));
            html.replace("%questReward_f%", String.valueOf(cutOff((double)Config.RATE_QUEST_REWARD * tpl.getQuestReward(), 2)));
            html.replace("%questDrop_f%", String.valueOf(cutOff((double)Config.RATE_QUEST_DROP * tpl.getQuestDrop(), 2)));
            html.replace("%fishing_f%", String.valueOf(cutOff(Config.RATE_DROP_FISHING * tpl.getFishing(), 2)));
            html.replace("%epaulette_f%", String.valueOf(cutOff(Config.RATE_DROP_SIEGE_GUARD * tpl.getEpaulette(), 2)));
            html.replace("%weight_f%", String.valueOf(cutOff((double)activeChar.getMaxLoad() * tpl.getWeight(), 2)));
            activeChar.sendPacket(html);
         }
      } else if (command.startsWith("_bbslistclanskills")) {
         if (!activeChar.isClanLeader()) {
            activeChar.sendMessage(new ServerMessage("ServiceBBS.CLAN_LEADER", activeChar.getLang()).toString());
            return;
         }

         StringTokenizer st = new StringTokenizer(command, " ");
         st.nextToken();
         int page = Integer.parseInt(st.nextToken());
         this.checkClanSkills(activeChar, page);
      } else if (command.startsWith("_bbslearnclanskills")) {
         StringTokenizer st = new StringTokenizer(command, " ");
         st.nextToken();
         int skillId = Integer.parseInt(st.nextToken());
         int skillLvl = Integer.parseInt(st.nextToken());
         int page = Integer.parseInt(st.nextToken());
         if (activeChar.getInventory().getItemByItemId(Config.SERVICES_CLANSKILLS_ITEM[0]) == null) {
            activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
            return;
         }

         if (activeChar.getInventory().getItemByItemId(Config.SERVICES_CLANSKILLS_ITEM[0]).getCount() < (long)Config.SERVICES_CLANSKILLS_ITEM[1]) {
            activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
            return;
         }

         activeChar.destroyItemByItemId("ClanSkillsBBS", Config.SERVICES_CLANSKILLS_ITEM[0], (long)Config.SERVICES_CLANSKILLS_ITEM[1], activeChar, true);
         activeChar.getClan().addNewSkill(SkillsParser.getInstance().getInfo(skillId, skillLvl));
         Util.addServiceLog(
            activeChar.getName() + " buy clan skill " + SkillsParser.getInstance().getInfo(skillId, skillLvl).getNameEn() + " " + skillLvl + " level!"
         );
         this.checkClanSkills(activeChar, page);
      } else if (command.startsWith("_bbswarhouse")) {
         if (command.equals("_bbswarhouse:chardeposit")) {
            activeChar.sendActionFailed();
            activeChar.setActiveWarehouse(activeChar.getWarehouse());
            if (activeChar.getWarehouse().getSize() == activeChar.getWareHouseLimit()) {
               activeChar.sendPacket(SystemMessageId.WAREHOUSE_FULL);
               return;
            }

            activeChar.setInventoryBlockingStatus(true);
            activeChar.sendPacket(new WareHouseDepositList(activeChar, 1));
         } else if (command.equals("_bbswarhouse:clandeposit")) {
            if (activeChar.isEnchanting()) {
               return;
            }

            if (activeChar.getClan() == null) {
               activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER);
               return;
            }

            activeChar.sendActionFailed();
            activeChar.setActiveWarehouse(activeChar.getClan().getWarehouse());
            if (activeChar.getClan().getLevel() == 0) {
               activeChar.sendPacket(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE);
               return;
            }

            activeChar.setActiveWarehouse(activeChar.getClan().getWarehouse());
            activeChar.setInventoryBlockingStatus(true);
            activeChar.sendPacket(new WareHouseDepositList(activeChar, 4));
         } else if (command.equals("_bbswarhouse:charwithdraw")) {
            activeChar.sendActionFailed();
            activeChar.setActiveWarehouse(activeChar.getWarehouse());
            if (activeChar.getActiveWarehouse().getSize() == 0) {
               activeChar.sendPacket(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH);
               return;
            }

            activeChar.sendPacket(new WareHouseWithdrawList(activeChar, 1));
         } else if (command.equals("_bbswarhouse:clanwithdraw")) {
            if (activeChar.isEnchanting()) {
               return;
            }

            if (activeChar.getClan() == null) {
               activeChar.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CLAN_WAREHOUSE);
               return;
            }

            if (activeChar.getClan().getLevel() == 0) {
               activeChar.sendPacket(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE);
               return;
            }

            activeChar.sendActionFailed();
            if ((activeChar.getClanPrivileges() & 8) != 8) {
               activeChar.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CLAN_WAREHOUSE);
               return;
            }

            activeChar.setActiveWarehouse(activeChar.getClan().getWarehouse());
            if (activeChar.getActiveWarehouse().getSize() == 0) {
               activeChar.sendPacket(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH);
               return;
            }

            activeChar.sendPacket(new WareHouseWithdrawList(activeChar, 4));
         }
      } else if (command.startsWith("_bbsAugment")) {
         if (command.equals("_bbsAugment;add")) {
            activeChar.sendPacket(SystemMessageId.SELECT_THE_ITEM_TO_BE_AUGMENTED);
            activeChar.sendPacket(new ExShowVariationMakeWindow());
            activeChar.cancelActiveTrade();
         } else if (command.equals("_bbsAugment;remove")) {
            activeChar.sendPacket(SystemMessageId.SELECT_THE_ITEM_FROM_WHICH_YOU_WISH_TO_REMOVE_AUGMENTATION);
            activeChar.sendPacket(new ExShowVariationCancelWindow());
            activeChar.cancelActiveTrade();
         }
      } else if (command.startsWith("_bbsdraw")) {
         if (command.equals("_bbsdraw:add")) {
            activeChar.sendPacket(new HennaEquipList(activeChar));
         } else if (command.equals("_bbsdraw:remove")) {
            for(Henna henna : activeChar.getHennaList()) {
               if (henna != null) {
                  activeChar.sendPacket(new HennaUnequipList(activeChar));
                  break;
               }
            }
         }
      } else if (command.startsWith("_bbsunban")) {
         String key = null;
         StringTokenizer st = new StringTokenizer(command, " ");
         st.nextToken();
         String affect = st.nextToken();

         try {
            key = st.nextToken();
         } catch (Exception var32) {
         }

         if (key != null) {
            if (activeChar.getInventory().getItemByItemId(Config.SERVICES_UNBAN_ITEM[0]) == null) {
               activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            if (activeChar.getInventory().getItemByItemId(Config.SERVICES_UNBAN_ITEM[0]).getCount() < (long)Config.SERVICES_UNBAN_ITEM[1]) {
               activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            if (this.unbanChar(activeChar, affect, key)) {
               activeChar.destroyItemByItemId("UnbanBBS", Config.SERVICES_UNBAN_ITEM[0], (long)Config.SERVICES_UNBAN_ITEM[1], activeChar, true);
               Util.addServiceLog(activeChar.getName() + " buy unban service!");
            } else {
               ServerMessage msg = new ServerMessage("ServiceBBS.UNBAN_NOTFIND", activeChar.getLang());
               msg.add(affect);
               msg.add(key);
               activeChar.sendMessage(msg.toString());
            }
         } else {
            activeChar.sendMessage(new ServerMessage("ServiceBBS.UNBAN_EMPTY", activeChar.getLang()).toString());
         }
      } else {
         if (command.startsWith("_bbsmultisell;")) {
            StringTokenizer st = new StringTokenizer(command, ";");
            st.nextToken();
            this.onBypassCommand("_bbspage:" + st.nextToken(), activeChar);
            int listId = Integer.parseInt(st.nextToken());
            if (Config.AVALIABLE_COMMUNITY_MULTISELLS.contains(listId)) {
               MultiSellParser.getInstance().separateAndSend(listId, activeChar, null, false);
               return;
            }

            Util.handleIllegalPlayerAction(activeChar, "" + activeChar.getName() + " try to cheat with Community MultiSell!");
            return;
         }

         if (command.startsWith("_bbsmsell;")) {
            StringTokenizer st = new StringTokenizer(command, ";");
            st.nextToken();
            this.onBypassCommand(st.nextToken(), activeChar);
            int listId = Integer.parseInt(st.nextToken());
            if (Config.AVALIABLE_COMMUNITY_MULTISELLS.contains(listId)) {
               MultiSellParser.getInstance().separateAndSend(listId, activeChar, null, false);
               return;
            }

            Util.handleIllegalPlayerAction(activeChar, "" + activeChar.getName() + " try to cheat with Community MultiSell!");
            return;
         }

         if (command.startsWith("_bbsExcMultisell;")) {
            StringTokenizer st = new StringTokenizer(command, ";");
            st.nextToken();
            this.onBypassCommand("_bbspage:" + st.nextToken(), activeChar);
            int listId = Integer.parseInt(st.nextToken());
            if (Config.AVALIABLE_COMMUNITY_MULTISELLS.contains(listId)) {
               MultiSellParser.getInstance().separateAndSend(listId, activeChar, null, true);
               return;
            }

            Util.handleIllegalPlayerAction(activeChar, "" + activeChar.getName() + " try to cheat with Community MultiSell!");
            return;
         }

         if (command.startsWith("_bbsExcMsell;")) {
            StringTokenizer st = new StringTokenizer(command, ";");
            st.nextToken();
            this.onBypassCommand(st.nextToken(), activeChar);
            int listId = Integer.parseInt(st.nextToken());
            if (Config.AVALIABLE_COMMUNITY_MULTISELLS.contains(listId)) {
               MultiSellParser.getInstance().separateAndSend(listId, activeChar, null, true);
               return;
            }

            Util.handleIllegalPlayerAction(activeChar, "" + activeChar.getName() + " try to cheat with Community MultiSell!");
            return;
         }

         if (!command.startsWith("_bbsservice")) {
            ShowBoard sb = new ShowBoard(
               "<html><body><br><br><center>the command: " + command + " is not implemented yet</center><br><br></body></html>", "101", activeChar
            );
            activeChar.sendPacket(sb);
            activeChar.sendPacket(new ShowBoard(null, "102", activeChar));
            activeChar.sendPacket(new ShowBoard(null, "103", activeChar));
         } else if (command.startsWith("_bbsservice:reloadRef")) {
            String id = null;
            String itemId = null;
            String itemAmount = null;
            String forPremium = null;
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();

            try {
               id = st.nextToken();
            } catch (Exception var31) {
            }

            try {
               itemId = st.nextToken();
            } catch (Exception var30) {
            }

            try {
               itemAmount = st.nextToken();
            } catch (Exception var29) {
            }

            try {
               forPremium = st.nextToken();
            } catch (Exception var28) {
            }

            if (id != null && itemId != null && itemAmount != null && forPremium != null) {
               boolean isForPremium = Integer.parseInt(forPremium) == 1;
               if (isForPremium && !activeChar.hasPremiumBonus()) {
                  activeChar.sendMessage(new ServerMessage("ServiceBBS.ONLY_FOR_PREMIUM", activeChar.getLang()).toString());
                  return;
               }

               if (activeChar.getUCState() > 0
                  || activeChar.isInFightEvent()
                  || (AerialCleftEvent.getInstance().isStarted() || AerialCleftEvent.getInstance().isRewarding())
                     && AerialCleftEvent.getInstance().isPlayerParticipant(activeChar.getObjectId())
                  || activeChar.getReflectionId() != 0) {
                  activeChar.sendMessage(new ServerMessage("Community.ALL_DISABLE", activeChar.getLang()).toString());
                  return;
               }

               if (System.currentTimeMillis() > ReflectionManager.getInstance().getReflectionTime(activeChar.getObjectId(), Integer.parseInt(id))) {
                  activeChar.sendMessage(new ServerMessage("ServiceBBS.REF_AVAILIABLE", activeChar.getLang()).toString());
                  return;
               }

               if (Integer.parseInt(itemId) > 0) {
                  if (activeChar.getInventory().getItemByItemId(Integer.parseInt(itemId)) == null) {
                     activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                     return;
                  }

                  if (activeChar.getInventory().getItemByItemId(Integer.parseInt(itemId)).getCount() < Long.parseLong(itemAmount)) {
                     activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                     return;
                  }

                  activeChar.destroyItemByItemId("reloadReflection", Integer.parseInt(itemId), Long.parseLong(itemAmount), activeChar, true);
                  Util.addServiceLog(activeChar.getName() + " buy refresh reflectionId " + Integer.parseInt(id));
               }

               ReflectionManager.getInstance().deleteReflectionTime(activeChar.getObjectId(), Integer.parseInt(id));
            }
         } else if (command.equals("_bbsservice:sell")) {
            ProductList list = BuyListParser.getInstance().getBuyList(1);
            if (list != null) {
               activeChar.sendPacket(new BuyList(list, activeChar.getAdena(), 0.0));
               activeChar.sendPacket(new ExBuySellList(activeChar, false));
            }
         } else if (command.equals("_bbsservice:expandInventory")) {
            if (activeChar.getInventory().getItemByItemId(Config.SERVICES_EXPAND_INVENTORY[0]) == null) {
               activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            if (activeChar.getInventory().getItemByItemId(Config.SERVICES_EXPAND_INVENTORY[0]).getCount() < (long)Config.SERVICES_EXPAND_INVENTORY[1]) {
               activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            int nextSlots = activeChar.getVarInt("expandInventory", 0) + Config.EXPAND_INVENTORY_STEP;
            if (nextSlots > Config.SERVICES_EXPAND_INVENTORY_LIMIT) {
               activeChar.sendMessage(new ServerMessage("ServiceBBS.LIMIT_EXCEEDED", activeChar.getLang()).toString());
               return;
            }

            activeChar.destroyItemByItemId("ExpandInventory", Config.SERVICES_EXPAND_INVENTORY[0], (long)Config.SERVICES_EXPAND_INVENTORY[1], activeChar, true);
            Util.addServiceLog(activeChar.getName() + " buy expand Inventory!");
            activeChar.setVar("expandInventory", nextSlots);
            activeChar.sendPacket(new ExStorageMaxCount(activeChar));
            ServerMessage msg = new ServerMessage("ServiceBBS.EXPAND_INV_INCREASE", activeChar.getLang());
            msg.add(Config.EXPAND_INVENTORY_STEP);
            activeChar.sendMessage(msg.toString());
         } else if (command.equals("_bbsservice:expandWareHouse")) {
            if (activeChar.getInventory().getItemByItemId(Config.SERVICES_EXPAND_WAREHOUSE[0]) == null) {
               activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            if (activeChar.getInventory().getItemByItemId(Config.SERVICES_EXPAND_WAREHOUSE[0]).getCount() < (long)Config.SERVICES_EXPAND_WAREHOUSE[1]) {
               activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            int nextSlots = activeChar.getVarInt("expandWareHouse", 0) + Config.EXPAND_WAREHOUSE_STEP;
            if (nextSlots > Config.SERVICES_EXPAND_WAREHOUSE_LIMIT) {
               activeChar.sendMessage(new ServerMessage("ServiceBBS.LIMIT_EXCEEDED", activeChar.getLang()).toString());
               return;
            }

            activeChar.destroyItemByItemId("ExpandWareHouse", Config.SERVICES_EXPAND_WAREHOUSE[0], (long)Config.SERVICES_EXPAND_WAREHOUSE[1], activeChar, true);
            Util.addServiceLog(activeChar.getName() + " buy expand WareHouse!");
            activeChar.setVar("expandWareHouse", nextSlots);
            activeChar.sendPacket(new ExStorageMaxCount(activeChar));
            ServerMessage msg = new ServerMessage("ServiceBBS.EXPAND_WH_INCREASE", activeChar.getLang());
            msg.add(Config.EXPAND_WAREHOUSE_STEP);
            activeChar.sendMessage(msg.toString());
         } else if (command.equals("_bbsservice:expandSellStore")) {
            if (activeChar.getInventory().getItemByItemId(Config.SERVICES_EXPAND_SELLSTORE[0]) == null) {
               activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            if (activeChar.getInventory().getItemByItemId(Config.SERVICES_EXPAND_SELLSTORE[0]).getCount() < (long)Config.SERVICES_EXPAND_SELLSTORE[1]) {
               activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            int nextSlots = activeChar.getVarInt("expandSellStore", 0) + Config.EXPAND_SELLSTORE_STEP;
            if (nextSlots > Config.SERVICES_EXPAND_SELLSTORE_LIMIT) {
               activeChar.sendMessage(new ServerMessage("ServiceBBS.LIMIT_EXCEEDED", activeChar.getLang()).toString());
               return;
            }

            activeChar.destroyItemByItemId("ExpandSellStore", Config.SERVICES_EXPAND_SELLSTORE[0], (long)Config.SERVICES_EXPAND_SELLSTORE[1], activeChar, true);
            Util.addServiceLog(activeChar.getName() + " buy expand Sell Store!");
            activeChar.setVar("expandSellStore", nextSlots);
            activeChar.sendPacket(new ExStorageMaxCount(activeChar));
            ServerMessage msg = new ServerMessage("ServiceBBS.EXPAND_SELLSTORE_INCREASE", activeChar.getLang());
            msg.add(Config.EXPAND_SELLSTORE_STEP);
            activeChar.sendMessage(msg.toString());
         } else if (command.equals("_bbsservice:expandBuyStore")) {
            if (activeChar.getInventory().getItemByItemId(Config.SERVICES_EXPAND_BUYSTORE[0]) == null) {
               activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            if (activeChar.getInventory().getItemByItemId(Config.SERVICES_EXPAND_BUYSTORE[0]).getCount() < (long)Config.SERVICES_EXPAND_BUYSTORE[1]) {
               activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            int nextSlots = activeChar.getVarInt("expandBuyStore", 0) + Config.EXPAND_BUYSTORE_STEP;
            if (nextSlots > Config.SERVICES_EXPAND_BUYSTORE_LIMIT) {
               activeChar.sendMessage(new ServerMessage("ServiceBBS.LIMIT_EXCEEDED", activeChar.getLang()).toString());
               return;
            }

            activeChar.destroyItemByItemId("ExpandBuyStore", Config.SERVICES_EXPAND_BUYSTORE[0], (long)Config.SERVICES_EXPAND_BUYSTORE[1], activeChar, true);
            Util.addServiceLog(activeChar.getName() + " buy expand Buy Store!");
            activeChar.setVar("expandBuyStore", nextSlots);
            activeChar.sendPacket(new ExStorageMaxCount(activeChar));
            ServerMessage msg = new ServerMessage("ServiceBBS.EXPAND_BUYSTORE_INCREASE", activeChar.getLang());
            msg.add(Config.EXPAND_BUYSTORE_STEP);
            activeChar.sendMessage(msg.toString());
         } else if (command.equals("_bbsservice:expandDwarfRecipe")) {
            if (activeChar.getInventory().getItemByItemId(Config.SERVICES_EXPAND_DWARFRECIPE[0]) == null) {
               activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            if (activeChar.getInventory().getItemByItemId(Config.SERVICES_EXPAND_DWARFRECIPE[0]).getCount() < (long)Config.SERVICES_EXPAND_DWARFRECIPE[1]) {
               activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            if (!activeChar.hasDwarvenCraft()) {
               return;
            }

            int nextSlots = activeChar.getVarInt("expandDwarfRecipe", 0) + Config.EXPAND_DWARFRECIPE_STEP;
            if (nextSlots > Config.SERVICES_EXPAND_DWARFRECIPE_LIMIT) {
               activeChar.sendMessage(new ServerMessage("ServiceBBS.LIMIT_EXCEEDED", activeChar.getLang()).toString());
               return;
            }

            activeChar.destroyItemByItemId(
               "ExpandDwarfRecipe", Config.SERVICES_EXPAND_DWARFRECIPE[0], (long)Config.SERVICES_EXPAND_DWARFRECIPE[1], activeChar, true
            );
            Util.addServiceLog(activeChar.getName() + " buy expand Dwarf Recipe!");
            activeChar.setVar("expandDwarfRecipe", nextSlots);
            activeChar.sendPacket(new ExStorageMaxCount(activeChar));
            ServerMessage msg = new ServerMessage("ServiceBBS.EXPAND_DWRECIPE_INCREASE", activeChar.getLang());
            msg.add(Config.EXPAND_DWARFRECIPE_STEP);
            activeChar.sendMessage(msg.toString());
         } else if (command.equals("_bbsservice:expandCommonRecipe")) {
            if (activeChar.getInventory().getItemByItemId(Config.SERVICES_EXPAND_COMMONRECIPE[0]) == null) {
               activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            if (activeChar.getInventory().getItemByItemId(Config.SERVICES_EXPAND_COMMONRECIPE[0]).getCount() < (long)Config.SERVICES_EXPAND_COMMONRECIPE[1]) {
               activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            int nextSlots = activeChar.getVarInt("expandCommonRecipe", 0) + Config.EXPAND_COMMONRECIPE_STEP;
            if (nextSlots > Config.SERVICES_EXPAND_COMMONRECIPE_LIMIT) {
               activeChar.sendMessage(new ServerMessage("ServiceBBS.LIMIT_EXCEEDED", activeChar.getLang()).toString());
               return;
            }

            activeChar.destroyItemByItemId(
               "ExpandCommonRecipe", Config.SERVICES_EXPAND_COMMONRECIPE[0], (long)Config.SERVICES_EXPAND_COMMONRECIPE[1], activeChar, true
            );
            Util.addServiceLog(activeChar.getName() + " buy expand Common Recipe!");
            activeChar.setVar("expandCommonRecipe", nextSlots);
            activeChar.sendPacket(new ExStorageMaxCount(activeChar));
            ServerMessage msg = new ServerMessage("ServiceBBS.EXPAND_COMRECIPE_INCREASE", activeChar.getLang());
            msg.add(Config.EXPAND_COMMONRECIPE_STEP);
            activeChar.sendMessage(msg.toString());
         } else if (command.equals("_bbsservice:noobles")) {
            if (activeChar.getInventory().getItemByItemId(Config.SERVICES_GIVENOOBLESS_ITEM[0]) == null) {
               activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            if (activeChar.getInventory().getItemByItemId(Config.SERVICES_GIVENOOBLESS_ITEM[0]).getCount() < (long)Config.SERVICES_GIVENOOBLESS_ITEM[1]) {
               activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            if (activeChar.getClassId().level() < 3) {
               activeChar.sendMessage(new ServerMessage("ServiceBBS.NOOBLES_MSG", activeChar.getLang()).toString());
               return;
            }

            if (activeChar.isNoble()) {
               activeChar.sendMessage(new ServerMessage("ServiceBBS.NOOBLES_MSG_1", activeChar.getLang()).toString());
               return;
            }

            activeChar.destroyItemByItemId("NooblesBBS", Config.SERVICES_GIVENOOBLESS_ITEM[0], (long)Config.SERVICES_GIVENOOBLESS_ITEM[1], activeChar, true);
            Util.addServiceLog(activeChar.getName() + " buy Noobless!");
            Olympiad.addNoble(activeChar);
            activeChar.setNoble(true);
            if (activeChar.getClan() != null) {
               activeChar.setPledgeClass(ClanMember.calculatePledgeClass(activeChar));
            } else {
               activeChar.setPledgeClass(5);
            }

            activeChar.sendUserInfo();
         } else if (command.equals("_bbsservice:gender")) {
            if (activeChar.getRace().ordinal() == 5) {
               activeChar.sendMessage(new ServerMessage("ServiceBBS.GENDER_MSG", activeChar.getLang()).toString());
               return;
            }

            if (activeChar.getInventory().getItemByItemId(Config.SERVICES_CHANGEGENDER_ITEM[0]) == null) {
               activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            if (activeChar.getInventory().getItemByItemId(Config.SERVICES_CHANGEGENDER_ITEM[0]).getCount() < (long)Config.SERVICES_CHANGEGENDER_ITEM[1]) {
               activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            activeChar.destroyItemByItemId(
               "ChangeGenderBBS", Config.SERVICES_CHANGEGENDER_ITEM[0], (long)Config.SERVICES_CHANGEGENDER_ITEM[1], activeChar, true
            );
            Util.addServiceLog(activeChar.getName() + " buy change gender!");
            activeChar.getAppearance().setSex(!activeChar.getAppearance().getSex());
            activeChar.broadcastUserInfo(true);
            activeChar.decayMe();
            activeChar.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());
         } else if (command.equals("_bbsservice:level")) {
            if (!Config.SERVICES_LEVELUP_ENABLE && !Config.SERVICES_DELEVEL_ENABLE) {
               activeChar.sendMessage(new ServerMessage("ServiceBBS.DISABLE", activeChar.getLang()).toString());
               return;
            }

            String html = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/community/levelcalc/index.htm");
            if (!Config.SERVICES_LEVELUP_ENABLE) {
               String up = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/community/levelcalc/up_off.htm");
               up = up.replace(
                  "{cost}", "<font color=\"CC3333\">" + ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.DISABLE") + "</font>"
               );
               html = html.replace("%up%", up);
            } else {
               String up = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/community/levelcalc/up.htm");
               up = up.replace("{cost}", Util.formatPay(activeChar, (long)Config.SERVICES_LEVELUP_ITEM[1], Config.SERVICES_LEVELUP_ITEM[0]));
               html = html.replace("%up%", up);
            }

            if (!Config.SERVICES_DELEVEL_ENABLE) {
               String lower = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/community/levelcalc/lower_off.htm");
               lower = lower.replace(
                  "{cost}", "<font color=\"CC3333\">" + ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.DISABLE") + "</font>"
               );
               html = html.replace("%lower%", lower);
            } else {
               String lower = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/community/levelcalc/lower.htm");
               lower = lower.replace("{cost}", Util.formatPay(activeChar, (long)Config.SERVICES_DELEVEL_ITEM[1], Config.SERVICES_DELEVEL_ITEM[0]));
               html = html.replace("%lower%", lower);
            }

            Util.setHtml(html, activeChar);
         } else if (command.startsWith("_bbsservice:levelcalc")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            String lvl = null;

            try {
               lvl = st.nextToken();
            } catch (Exception var27) {
            }

            if (lvl != null) {
               int level = Util.isNumber(lvl) ? Integer.parseInt(lvl) : activeChar.getLevel();
               if (level == activeChar.getLevel()) {
                  activeChar.sendMessage(new ServerMessage("ServiceBBS.LVL_EQUALS", activeChar.getLang()).toString());
                  return;
               }

               boolean delevel = level < activeChar.getLevel();
               if (delevel && !Config.SERVICES_DELEVEL_ENABLE) {
                  activeChar.sendMessage(new ServerMessage("ServiceBBS.CANT_DELEVEL", activeChar.getLang()).toString());
                  return;
               }

               if (!delevel && !Config.SERVICES_LEVELUP_ENABLE) {
                  activeChar.sendMessage(new ServerMessage("ServiceBBS.CANT_LVLUP", activeChar.getLang()).toString());
                  return;
               }

               int item = level < activeChar.getLevel() ? Config.SERVICES_DELEVEL_ITEM[0] : Config.SERVICES_LEVELUP_ITEM[0];
               long count = level < activeChar.getLevel()
                  ? (long)((activeChar.getLevel() - level) * Config.SERVICES_DELEVEL_ITEM[1])
                  : (long)((level - activeChar.getLevel()) * Config.SERVICES_LEVELUP_ITEM[1]);
               ServerMessage msg = new ServerMessage("ServiceBBS.WANT_CHANGE_LVL", activeChar.getLang());
               msg.add(activeChar.getLevel());
               msg.add(level);
               msg.add(Util.formatPay(activeChar, count, item));
               activeChar.sendConfirmDlg(new LevelAnswerListener(activeChar, level), 60000, msg.toString());
            }
         } else if (command.equals("_bbsservice:setHero")) {
            if (activeChar.isHero()) {
               return;
            }

            if (activeChar.getInventory().getItemByItemId(Config.SERVICES_GIVEHERO_ITEM[0]) == null) {
               activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            if (activeChar.getInventory().getItemByItemId(Config.SERVICES_GIVEHERO_ITEM[0]).getCount() < (long)Config.SERVICES_GIVEHERO_ITEM[1]) {
               activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            if (activeChar.isHero()) {
               activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 16));
            } else {
               activeChar.destroyItemByItemId("SetHeroBBS", Config.SERVICES_GIVEHERO_ITEM[0], (long)Config.SERVICES_GIVEHERO_ITEM[1], activeChar, true);
               Util.addServiceLog(activeChar.getName() + " buy hero status!");
               long endTime = System.currentTimeMillis() + (long)Config.SERVICES_GIVEHERO_TIME * 60000L;
               activeChar.setVar("tempHero", String.valueOf(endTime), endTime);
               activeChar.setHero(true, false);
               activeChar.startTempHeroTask(endTime);
               if (activeChar.getClan() != null) {
                  activeChar.setPledgeClass(ClanMember.calculatePledgeClass(activeChar));
               } else {
                  activeChar.setPledgeClass(8);
               }

               activeChar.broadcastUserInfo(true);
            }
         } else if (command.equals("_bbsservice:recoveryPK")) {
            if (activeChar.getPkKills() <= 0) {
               activeChar.sendMessage(new ServerMessage("ServiceBBS.PK_MSG", activeChar.getLang()).toString());
               return;
            }

            if (activeChar.getInventory().getItemByItemId(Config.SERVICES_RECOVERYPK_ITEM[0]) == null) {
               activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            if (activeChar.getInventory().getItemByItemId(Config.SERVICES_RECOVERYPK_ITEM[0]).getCount() < (long)Config.SERVICES_RECOVERYPK_ITEM[1]) {
               activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            activeChar.destroyItemByItemId("RecPkBBS", Config.SERVICES_RECOVERYPK_ITEM[0], (long)Config.SERVICES_RECOVERYPK_ITEM[1], activeChar, true);
            Util.addServiceLog(activeChar.getName() + " buy recovery pk service!");
            activeChar.setPkKills(0);
            activeChar.broadcastUserInfo(true);
         } else if (command.equals("_bbsservice:recoveryKarma")) {
            if (activeChar.getKarma() <= 0) {
               activeChar.sendMessage(new ServerMessage("ServiceBBS.KARMA_MSG", activeChar.getLang()).toString());
               return;
            }

            if (activeChar.getInventory().getItemByItemId(Config.SERVICES_RECOVERYKARMA_ITEM[0]) == null) {
               activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            if (activeChar.getInventory().getItemByItemId(Config.SERVICES_RECOVERYKARMA_ITEM[0]).getCount() < (long)Config.SERVICES_RECOVERYKARMA_ITEM[1]) {
               activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            activeChar.destroyItemByItemId("RecKarmaBBS", Config.SERVICES_RECOVERYKARMA_ITEM[0], (long)Config.SERVICES_RECOVERYKARMA_ITEM[1], activeChar, true);
            Util.addServiceLog(activeChar.getName() + " buy recovery karma service!");
            activeChar.setKarma(0);
            activeChar.broadcastUserInfo(true);
         } else if (command.equals("_bbsservice:recoveryVitality")) {
            if (activeChar.getInventory().getItemByItemId(Config.SERVICES_RECOVERYVITALITY_ITEM[0]) == null) {
               activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            if (activeChar.getInventory().getItemByItemId(Config.SERVICES_RECOVERYVITALITY_ITEM[0]).getCount()
               < (long)Config.SERVICES_RECOVERYVITALITY_ITEM[1]) {
               activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            activeChar.destroyItemByItemId(
               "RecVitalityBBS", Config.SERVICES_RECOVERYVITALITY_ITEM[0], (long)Config.SERVICES_RECOVERYVITALITY_ITEM[1], activeChar, true
            );
            activeChar.setVitalityPoints(Math.min(Config.STARTING_VITALITY_POINTS, PcStat.MAX_VITALITY_POINTS), true);
         } else if (command.equals("_bbsservice:addSP")) {
            if (activeChar.getInventory().getItemByItemId(Config.SERVICES_GIVESP_ITEM[0]) == null) {
               activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            if (activeChar.getInventory().getItemByItemId(Config.SERVICES_GIVESP_ITEM[0]).getCount() < (long)Config.SERVICES_GIVESP_ITEM[1]) {
               activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            activeChar.destroyItemByItemId("AddSpBBS", Config.SERVICES_GIVESP_ITEM[0], (long)Config.SERVICES_GIVESP_ITEM[1], activeChar, true);
            Util.addServiceLog(activeChar.getName() + " buy add SP service!");
            activeChar.setSp(activeChar.getSp() + 10000000);
            activeChar.sendMessage(new ServerMessage("ServiceBBS.ADDSP_MSG", activeChar.getLang()).toString());
            activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 16));
            activeChar.broadcastCharInfo();
         } else if (command.equals("_bbsservice:clanlvlup")) {
            ServerMessage msg = null;
            if (activeChar.getClan() != null) {
               if (activeChar.getClan().getLevel() >= 11) {
                  new ServerMessage("ServiceBBS.MAXLVL", activeChar.getLang());
                  return;
               }

               if (activeChar.getInventory().getItemByItemId(Config.SERVICES_CLANLVL_ITEM[0]) == null) {
                  activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                  return;
               }

               if (activeChar.getInventory().getItemByItemId(Config.SERVICES_CLANLVL_ITEM[0]).getCount() < (long)Config.SERVICES_CLANLVL_ITEM[1]) {
                  activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                  return;
               }

               activeChar.destroyItemByItemId("ClanlvlUpBBS", Config.SERVICES_CLANLVL_ITEM[0], (long)Config.SERVICES_CLANLVL_ITEM[1], activeChar, true);
               Util.addServiceLog(activeChar.getName() + " buy lvl up for clan service!");
               activeChar.getClan().changeLevel(activeChar.getClan().getLevel() + 1, true);
               msg = new ServerMessage("ServiceBBS.CLAN_LVLUP", activeChar.getLang());
               msg.add(activeChar.getClan().getLevel());
            } else {
               msg = new ServerMessage("ServiceBBS.NEED_CREATE", activeChar.getLang());
            }

            activeChar.sendMessage(msg.toString());
         } else if (command.equals("_bbsservice:clanCreatePenalty")) {
            if (activeChar.getInventory().getItemByItemId(Config.SERVICES_CLAN_CREATE_PENALTY_ITEM[0]) == null) {
               activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            if (activeChar.getInventory().getItemByItemId(Config.SERVICES_CLAN_CREATE_PENALTY_ITEM[0]).getCount()
               < (long)Config.SERVICES_CLAN_CREATE_PENALTY_ITEM[1]) {
               activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            if (activeChar.getClanCreateExpiryTime() <= 0L) {
               activeChar.sendMessage(new ServerMessage("CommunityGeneral.HAVE_NO_CREATE_PENALTY", activeChar.getLang()).toString());
               return;
            }

            activeChar.destroyItemByItemId(
               "CreatePenaltyBBS", Config.SERVICES_CLAN_CREATE_PENALTY_ITEM[0], (long)Config.SERVICES_CLAN_CREATE_PENALTY_ITEM[1], activeChar, true
            );
            Util.addServiceLog(activeChar.getName() + " buy refresh clan create penalty service!");
            activeChar.setClanCreateExpiryTime(0L);
            activeChar.sendMessage(new ServerMessage("CommunityGeneral.CREATE_PENALTY_REMOVED", activeChar.getLang()).toString());
         } else if (command.equals("_bbsservice:clanJoinPenalty")) {
            if (activeChar.getInventory().getItemByItemId(Config.SERVICES_CLAN_JOIN_PENALTY_ITEM[0]) == null) {
               activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            if (activeChar.getInventory().getItemByItemId(Config.SERVICES_CLAN_JOIN_PENALTY_ITEM[0]).getCount()
               < (long)Config.SERVICES_CLAN_JOIN_PENALTY_ITEM[1]) {
               activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            if (activeChar.getClan() == null && activeChar.getClanJoinExpiryTime() <= 0L) {
               activeChar.sendMessage(new ServerMessage("CommunityGeneral.HAVE_NO_JOIN_PENALTY", activeChar.getLang()).toString());
               return;
            }

            if (activeChar.getClan() != null && activeChar.getClan().getCharPenaltyExpiryTime() <= 0L) {
               activeChar.sendMessage(new ServerMessage("CommunityGeneral.HAVE_NO_JOIN_PENALTY", activeChar.getLang()).toString());
               return;
            }

            activeChar.destroyItemByItemId(
               "JoinPenaltyBBS", Config.SERVICES_CLAN_JOIN_PENALTY_ITEM[0], (long)Config.SERVICES_CLAN_JOIN_PENALTY_ITEM[1], activeChar, true
            );
            Util.addServiceLog(activeChar.getName() + " buy refresh clan join penalty service!");
            if (activeChar.getClan() == null) {
               activeChar.setClanJoinExpiryTime(0L);
            } else {
               activeChar.getClan().setCharPenaltyExpiryTime(0L);
            }

            activeChar.sendMessage(new ServerMessage("CommunityGeneral.CREATE_JOIN_REMOVE", activeChar.getLang()).toString());
         } else if (command.equals("_bbsservice:giverec")) {
            if (activeChar.getInventory().getItemByItemId(Config.SERVICES_GIVEREC_ITEM[0]) == null) {
               activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            if (activeChar.getInventory().getItemByItemId(Config.SERVICES_GIVEREC_ITEM[0]).getCount() < (long)Config.SERVICES_GIVEREC_ITEM[1]) {
               activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            if (activeChar.getRecommendation().getRecomHave() == 255) {
               activeChar.sendMessage(new ServerMessage("ServiceBBS.MAX_REC", activeChar.getLang()).toString());
               return;
            }

            activeChar.destroyItemByItemId("GiveRecBBS", Config.SERVICES_GIVEREC_ITEM[0], (long)Config.SERVICES_GIVEREC_ITEM[1], activeChar, true);
            Util.addServiceLog(activeChar.getName() + " buy recommendations service!");
            int recCanGive = 255 - activeChar.getRecommendation().getRecomHave();
            activeChar.getRecommendation().setRecomHave(activeChar.getRecommendation().getRecomHave() + recCanGive);
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_OBTAINED_S1_RECOMMENDATIONS);
            sm.addNumber(recCanGive);
            activeChar.sendPacket(sm);
            activeChar.sendUserInfo();
            activeChar.sendVoteSystemInfo();
         } else if (command.equals("_bbsservice:givereputation")) {
            if (activeChar.getClan() != null) {
               if (activeChar.getInventory().getItemByItemId(Config.SERVICES_GIVEREP_ITEM[0]) == null) {
                  activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                  return;
               }

               if (activeChar.getInventory().getItemByItemId(Config.SERVICES_GIVEREP_ITEM[0]).getCount() < (long)Config.SERVICES_GIVEREP_ITEM[1]) {
                  activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                  return;
               }

               activeChar.destroyItemByItemId("GiveRepBBS", Config.SERVICES_GIVEREP_ITEM[0], (long)Config.SERVICES_GIVEREP_ITEM[1], activeChar, true);
               Util.addServiceLog(activeChar.getName() + " buy reputation service!");
               activeChar.getClan().addReputationScore(Config.SERVICES_REP_COUNT, true);
               ServerMessage msg = new ServerMessage("ServiceBBS.ADD_REP", activeChar.getLang());
               msg.add(String.valueOf(Config.SERVICES_REP_COUNT));
               activeChar.sendMessage(msg.toString());
            } else {
               activeChar.sendMessage(new ServerMessage("ServiceBBS.NEED_CREATE", activeChar.getLang()).toString());
            }
         } else if (command.equals("_bbsservice:givefame")) {
            if (activeChar.getInventory().getItemByItemId(Config.SERVICES_GIVEFAME_ITEM[0]) == null) {
               activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            if (activeChar.getInventory().getItemByItemId(Config.SERVICES_GIVEFAME_ITEM[0]).getCount() < (long)Config.SERVICES_GIVEFAME_ITEM[1]) {
               activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            activeChar.destroyItemByItemId("GiveFameBBS", Config.SERVICES_GIVEFAME_ITEM[0], (long)Config.SERVICES_GIVEFAME_ITEM[1], activeChar, true);
            Util.addServiceLog(activeChar.getName() + " buy fame service!");
            activeChar.setFame(activeChar.getFame() + Config.SERVICES_FAME_COUNT);
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_REPUTATION_SCORE);
            sm.addNumber(Config.SERVICES_FAME_COUNT);
            activeChar.sendPacket(sm);
            activeChar.sendUserInfo();
         } else if (command.equals("_bbsservice:augmentation")) {
            this.showMainMenu(activeChar, 0, Options.AugmentationFilter.NONE);
         } else if (command.startsWith("_bbsservice:augmentationSection")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            int select = Integer.parseInt(st.nextToken());
            Options.AugmentationFilter _filter = Options.AugmentationFilter.NONE;

            try {
               switch(select) {
                  case 1:
                     _filter = Options.AugmentationFilter.NONE;
                     break;
                  case 2:
                     _filter = Options.AugmentationFilter.ACTIVE_SKILL;
                     break;
                  case 3:
                     _filter = Options.AugmentationFilter.PASSIVE_SKILL;
                     break;
                  case 4:
                     _filter = Options.AugmentationFilter.CHANCE_SKILL;
                     break;
                  case 5:
                     _filter = Options.AugmentationFilter.STATS;
               }
            } catch (Exception var26) {
               var26.printStackTrace();
            }

            this.showMainMenu(activeChar, 1, _filter);
         } else if (command.startsWith("_bbsservice:augmentationPage")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            int filter = Integer.parseInt(st.nextToken());
            int page = Integer.parseInt(st.nextToken());
            Options.AugmentationFilter _filter = Options.AugmentationFilter.NONE;

            try {
               switch(filter) {
                  case 1:
                     _filter = Options.AugmentationFilter.NONE;
                     break;
                  case 2:
                     _filter = Options.AugmentationFilter.ACTIVE_SKILL;
                     break;
                  case 3:
                     _filter = Options.AugmentationFilter.PASSIVE_SKILL;
                     break;
                  case 4:
                     _filter = Options.AugmentationFilter.CHANCE_SKILL;
                     break;
                  case 5:
                     _filter = Options.AugmentationFilter.STATS;
               }
            } catch (Exception var25) {
               var25.printStackTrace();
            }

            this.showMainMenu(activeChar, page, _filter);
         } else if (command.startsWith("_bbsservice:augmentationPut")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            int select = Integer.parseInt(st.nextToken());
            int selectpage = Integer.parseInt(st.nextToken());
            int page = Integer.parseInt(st.nextToken());
            Options.AugmentationFilter _filter = Options.AugmentationFilter.NONE;

            try {
               if (activeChar.isInStoreMode() || activeChar.isProcessingRequest() || activeChar.getActiveRequester() != null) {
                  activeChar.sendMessage(new ServerMessage("ServiceBBS.AUGMENT_STOREMOD", activeChar.getLang()).toString());
                  return;
               }

               ItemInstance targetItem = activeChar.getInventory().getPaperdollItem(5);
               if (targetItem == null) {
                  activeChar.sendMessage(new ServerMessage("ServiceBBS.AUGMENT_NOWEAPON", activeChar.getLang()).toString());
                  return;
               }

               if (!this.checkItemType(targetItem)) {
                  return;
               }

               if (activeChar.getInventory().getItemByItemId(Config.SERVICES_AUGMENTATION_ITEM[0]) == null) {
                  activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                  return;
               }

               if (activeChar.getInventory().getItemByItemId(Config.SERVICES_AUGMENTATION_ITEM[0]).getCount() < (long)Config.SERVICES_AUGMENTATION_ITEM[1]) {
                  activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                  return;
               }

               activeChar.destroyItemByItemId("AugmentBBS", Config.SERVICES_AUGMENTATION_ITEM[0], (long)Config.SERVICES_AUGMENTATION_ITEM[1], activeChar, true);
               Util.addServiceLog(activeChar.getName() + " buy augmentation for item service!");
               this.unAugment(activeChar, targetItem);
               activeChar.getInventory().unEquipItemInSlot(5);
               int secAugId = AugmentationParser.getInstance().generateRandomSecondaryAugmentation();
               targetItem.setAugmentation(new Augmentation((select << 16) + secAugId));
               activeChar.getInventory().equipItem(targetItem);
               InventoryUpdate iu = new InventoryUpdate();
               iu.addModifiedItem(targetItem);
               activeChar.sendPacket(iu);
               activeChar.broadcastCharInfo();
               activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_ITEM_WAS_SUCCESSFULLY_AUGMENTED));
               switch(page) {
                  case 1:
                     _filter = Options.AugmentationFilter.NONE;
                     break;
                  case 2:
                     _filter = Options.AugmentationFilter.ACTIVE_SKILL;
                     break;
                  case 3:
                     _filter = Options.AugmentationFilter.PASSIVE_SKILL;
                     break;
                  case 4:
                     _filter = Options.AugmentationFilter.CHANCE_SKILL;
                     break;
                  case 5:
                     _filter = Options.AugmentationFilter.STATS;
               }
            } catch (Exception var37) {
               var37.printStackTrace();
            }

            this.showMainMenu(activeChar, selectpage, _filter);
         } else if (command.startsWith("_bbsservice:exchangerPage")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            String upgrade = st.nextToken();
            String pg = st.nextToken();
            boolean isUpgrade = upgrade.equalsIgnoreCase("1");
            this.removeVars(activeChar, true);
            this.cleanAtt(activeChar, -1);
            NpcHtmlMessage html = new NpcHtmlMessage(5);
            html.setFile(activeChar, activeChar.getLang(), "data/html/community/exchanger/page.htm");
            String template = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/community/exchanger/template.htm");
            String block = "";
            String list = "";
            List<Change> _list = new ArrayList<>();

            for(ItemInstance item : activeChar.getInventory().getPaperdollItems()) {
               if (item != null) {
                  Change change = ExchangeItemParser.getInstance().getChanges(item.getId(), isUpgrade);
                  if (change != null) {
                     _list.add(change);
                  }
               }
            }

            if (_list.isEmpty()) {
               NpcHtmlMessage html2 = new NpcHtmlMessage(5);
               html2.setHtml(
                  activeChar,
                  "<html><title>"
                     + (
                        isUpgrade
                           ? "" + ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.UPGRADE_ITEMS") + ""
                           : "" + ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.EXCHANGE_ITEMS") + ""
                     )
                     + "</title><body><center><br><br><font name=hs12>"
                     + ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.WEAR_LIST")
                     + "</font></center></body></html>"
               );
               activeChar.sendPacket(html2);
               return;
            }

            int perpage = 6;
            int page = pg.length() > 0 ? Integer.parseInt(pg) : 1;
            int counter = 0;
            boolean isThereNextPage = _list.size() > 6;

            for(int i = (page - 1) * 6; i < _list.size(); ++i) {
               Change pack = _list.get(i);
               block = template.replace("{bypass}", "bypass _bbsservice:exchangerList " + pack.getId() + " " + (isUpgrade ? 1 : 0) + " " + 1);
               block = block.replace("{name}", Util.getItemName(activeChar, pack.getId()));
               block = block.replace("{icon}", pack.getIcon());
               ServerMessage msg = new ServerMessage("ServiceBBS.EXCHANGE_COST", activeChar.getLang());
               msg.toString(Util.formatPay(activeChar, pack.getCostCount(), pack.getCostId()));
               block = block.replace("{cost}", msg.toString());
               list = list + block;
               if (++counter >= 6) {
                  break;
               }
            }

            double pages = (double)_list.size() / 6.0;
            int count = (int)Math.ceil(pages);
            html.replace("%list%", list);
            html.replace(
               "%navigation%",
               Util.getNavigationBlock(count, page, _list.size(), 6, isThereNextPage, "_bbsservice:exchangerPage " + (isUpgrade ? 1 : 0) + " %s")
            );
            activeChar.sendPacket(html);
         } else if (command.startsWith("_bbsservice:exchangerList")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            String packId = st.nextToken();
            String upgrade = st.nextToken();
            String pg = st.nextToken();
            if (activeChar == null) {
               return;
            }

            this.cleanAtt(activeChar, -1);
            this.removeVars(activeChar, true);
            if (packId.isEmpty() || !Util.isNumber(packId)) {
               return;
            }

            int id = Integer.parseInt(packId);
            boolean isUpgrade = upgrade.equalsIgnoreCase("1");
            NpcHtmlMessage html = new NpcHtmlMessage(5);
            html.setFile(activeChar, activeChar.getLang(), "data/html/community/exchanger/list.htm");
            String template = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/community/exchanger/template.htm");
            String block = "";
            String list = "";
            Change change = ExchangeItemParser.getInstance().getChanges(id, isUpgrade);
            if (change == null) {
               return;
            }

            activeChar.addQuickVar("exchange", id);
            List<Variant> _list = change.getList();
            int perpage = 6;
            int page = pg.length() > 0 ? Integer.parseInt(pg) : 1;
            int counter = 0;
            boolean isThereNextPage = _list.size() > 6;

            for(int i = (page - 1) * 6; i < _list.size(); ++i) {
               Variant pack = _list.get(i);
               block = template.replace("{bypass}", "bypass _bbsservice:exchangerOpen " + pack.getNumber() + " " + (isUpgrade ? 1 : 0));
               block = block.replace("{name}", Util.getItemName(activeChar, pack.getId()));
               block = block.replace("{icon}", pack.getIcon());
               ServerMessage msg = new ServerMessage("ServiceBBS.EXCHANGE_COST", activeChar.getLang());
               msg.toString(Util.formatPay(activeChar, change.getCostCount(), change.getCostId()));
               block = block.replace("{cost}", msg.toString());
               list = list + block;
               if (++counter >= 6) {
                  break;
               }
            }

            double pages = (double)_list.size() / 6.0;
            int count = (int)Math.ceil(pages);
            html.replace("%list%", list);
            html.replace(
               "%navigation%",
               Util.getNavigationBlock(count, page, _list.size(), 6, isThereNextPage, "_bbsservice:exchangerList " + id + " " + (isUpgrade ? 1 : 0) + " %s")
            );
            html.replace("%choice%", Util.getItemName(activeChar, change.getId()));
            activeChar.sendPacket(html);
         } else if (command.startsWith("_bbsservice:exchangerOpen")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            String packId = st.nextToken();
            String upgrade = st.nextToken();
            if (activeChar == null) {
               return;
            }

            int id = activeChar.getQuickVarI("exchange", -1);
            if (id == -1 || packId.isEmpty() || !Util.isNumber(packId)) {
               return;
            }

            int new_id = Integer.parseInt(packId);
            boolean isUpgrade = upgrade.equalsIgnoreCase("1");
            ItemInstance item = null;
            Change change = null;

            for(ItemInstance inv : activeChar.getInventory().getPaperdollItems()) {
               if (inv != null) {
                  change = ExchangeItemParser.getInstance().getChanges(inv.getId(), isUpgrade);
                  if (change != null && change.getId() == id) {
                     item = inv;
                     break;
                  }
               }
            }

            if (item == null) {
               return;
            }

            Variant variant = change.getVariant(new_id);
            if (variant == null) {
               return;
            }

            this.removeVars(activeChar, false);
            activeChar.addQuickVar("exchange_obj", item.getObjectId());
            activeChar.addQuickVar("exchange_new", variant.getId());
            activeChar.addQuickVar("exchange_attribute", change.attChange());
            if (change.attChange()) {
               activeChar.addQuickVar("exchange_number", variant.getNumber());
            }

            NpcHtmlMessage html = new NpcHtmlMessage(5);
            html.setFile(activeChar, activeChar.getLang(), "data/html/community/exchanger/general.htm");
            html.replace("%my_name%", activeChar.getItemName(item.getItem()));
            html.replace("%my_ench%", "+" + item.getEnchantLevel());
            html.replace("%my_icon%", item.getItem().getIcon());
            Elementals att = item.getElementals() == null ? null : item.getElementals()[0];
            if (change.attChange() && att != null) {
               String att_info = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/community/exchanger/att_change.htm");
               if (activeChar.getQuickVarI("ex_att", -1) == -1) {
                  if (att.getElement() == 0) {
                     att_info = att_info.replace("%Fire%", String.valueOf(att.getValue()));
                  } else {
                     att_info = att_info.replace("%Fire%", String.valueOf(0));
                  }

                  if (att.getElement() == 1) {
                     att_info = att_info.replace("%Water%", String.valueOf(att.getValue()));
                  } else {
                     att_info = att_info.replace("%Water%", String.valueOf(0));
                  }

                  if (att.getElement() == 2) {
                     att_info = att_info.replace("%Wind%", String.valueOf(att.getValue()));
                  } else {
                     att_info = att_info.replace("%Wind%", String.valueOf(0));
                  }

                  if (att.getElement() == 3) {
                     att_info = att_info.replace("%Earth%", String.valueOf(att.getValue()));
                  } else {
                     att_info = att_info.replace("%Earth%", String.valueOf(0));
                  }

                  if (att.getElement() == 4) {
                     att_info = att_info.replace("%Holy%", String.valueOf(att.getValue()));
                  } else {
                     att_info = att_info.replace("%Holy%", String.valueOf(0));
                  }

                  if (att.getElement() == 5) {
                     att_info = att_info.replace("%Unholy%", String.valueOf(att.getValue()));
                  } else {
                     att_info = att_info.replace("%Unholy%", String.valueOf(0));
                  }
               } else {
                  att_info = att_info.replace("%Fire%", String.valueOf(activeChar.getQuickVarI("ex_att_0", 0)));
                  att_info = att_info.replace("%Water%", String.valueOf(activeChar.getQuickVarI("ex_att_1", 0)));
                  att_info = att_info.replace("%Wind%", String.valueOf(activeChar.getQuickVarI("ex_att_2", 0)));
                  att_info = att_info.replace("%Earth%", String.valueOf(activeChar.getQuickVarI("ex_att_3", 0)));
                  att_info = att_info.replace("%Holy%", String.valueOf(activeChar.getQuickVarI("ex_att_4", 0)));
                  att_info = att_info.replace("%Unholy%", String.valueOf(activeChar.getQuickVarI("ex_att_5", 0)));
               }

               html.replace("%att_info%", att_info);
            } else {
               String att_info = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/community/exchanger/att_info.htm");
               att_info = att_info.replace("%Fire%", String.valueOf(0));
               att_info = att_info.replace("%Water%", String.valueOf(0));
               att_info = att_info.replace("%Wind%", String.valueOf(0));
               att_info = att_info.replace("%Earth%", String.valueOf(0));
               att_info = att_info.replace("%Holy%", String.valueOf(0));
               att_info = att_info.replace("%Unholy%", String.valueOf(0));
               html.replace("%att_info%", att_info);
            }

            html.replace("%cost%", Util.formatPay(activeChar, change.getCostCount(), change.getCostId()));
            html.replace("%new_name%", Util.getItemName(activeChar, variant.getId()));
            html.replace("%new_icon%", variant.getIcon());
            html.replace("%new_id%", String.valueOf(id));
            html.replace("%is_upgrade%", (long)(isUpgrade ? 1 : 0));
            activeChar.sendPacket(html);
         } else if (command.startsWith("_bbsservice:exchange")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            String upgrade = st.nextToken();
            int exchangeId = activeChar.getQuickVarI("exchange", -1);
            if (exchangeId == -1) {
               return;
            }

            int obj_my = activeChar.getQuickVarI("exchange_obj", -1);
            if (obj_my == -1) {
               return;
            }

            int id_new = activeChar.getQuickVarI("exchange_new", -1);
            if (id_new == -1) {
               return;
            }

            boolean isUpgrade = upgrade.equalsIgnoreCase("1");
            boolean att_change = activeChar.getQuickVarB("exchange_attribute", false);
            Change change = ExchangeItemParser.getInstance().getChanges(exchangeId, isUpgrade);
            if (change == null) {
               return;
            }

            ItemInstance item_my = activeChar.getInventory().getItemByObjectId(obj_my);
            if (item_my == null) {
               return;
            }

            if (activeChar.getInventory().getItemByItemId(change.getCostId()) == null) {
               activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            if (activeChar.getInventory().getItemByItemId(change.getCostId()).getCount() < change.getCostCount()) {
               activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            int EnchantLevel = item_my.getEnchantLevel();
            Augmentation Augmentation = item_my.getAugmentation();
            Elementals elementals = item_my.getElementals() == null ? null : item_my.getElementals()[0];
            byte element = -1;
            int power = -1;
            int new_att = activeChar.getQuickVarI("ex_att", -1);
            if (att_change && new_att != -1) {
               element = Elementals.getElementById(new_att);
               power = item_my.getElementals()[0].getValue();
            } else if (elementals != null) {
               element = elementals.getElement();
               power = elementals.getValue();
            }

            if (activeChar.getInventory().destroyItemByObjectId(item_my.getObjectId(), item_my.getCount(), activeChar, Boolean.valueOf(true)) != null) {
               ItemInstance itemInstance = activeChar.getInventory().addItem("ExchangersBBS", id_new, 1L, activeChar, true);
               itemInstance.setEnchantLevel(EnchantLevel);
               itemInstance.setAugmentation(Augmentation);
               if (element != -1 && power != -1) {
                  itemInstance.setElementAttr(element, power);
               }

               activeChar.getInventory().equipItem(itemInstance);
               InventoryUpdate iu = new InventoryUpdate();
               iu.addModifiedItem(itemInstance);
               activeChar.sendPacket(iu);
               activeChar.destroyItemByItemId("ExchangersBBS", change.getCostId(), change.getCostCount(), activeChar, true);
               Util.addServiceLog(activeChar.getName() + " buy exchange item service!");
               ServerMessage msg = new ServerMessage("ServiceBBS.YOU_EXCHANGE", activeChar.getLang());
               msg.add(activeChar.getItemName(item_my.getItem()));
               msg.add(activeChar.getItemName(itemInstance.getItem()));
               activeChar.sendMessage(msg.toString());
            }

            this.removeVars(activeChar, true);
            this.cleanAtt(activeChar, -1);
         } else if (command.startsWith("_bbsservice:changeAtt")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            String attId = st.nextToken();
            String upgrade = st.nextToken();
            if (attId.isEmpty() || !Util.isNumber(attId)) {
               return;
            }

            int obj_my = activeChar.getQuickVarI("exchange_obj", -1);
            if (obj_my == -1) {
               return;
            }

            ItemInstance item = activeChar.getInventory().getItemByObjectId(obj_my);
            if (item == null) {
               return;
            }

            int id_new = activeChar.getQuickVarI("exchange_number", -1);
            if (id_new == -1) {
               return;
            }

            int att_id = Integer.parseInt(attId);
            boolean isUpgrade = upgrade.equalsIgnoreCase("1");
            byte att = Elementals.getElementById(att_id);
            if (att != -1) {
               activeChar.addQuickVar("ex_att_" + att_id, item.getElementals()[0].getValue());
               activeChar.addQuickVar("ex_att", att_id);
               this.cleanAtt(activeChar, att_id);
            }

            this.onBypassCommand("_bbsservice:exchangerOpen " + String.valueOf(id_new) + " " + (isUpgrade ? "1" : "0"), activeChar);
         } else if (command.startsWith("_bbsservice:donateList")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            String groupId = st.nextToken();
            String pageId = st.nextToken();
            if (!groupId.isEmpty() && Util.isNumber(groupId) && (pageId.isEmpty() || Util.isNumber(pageId))) {
               int id = Integer.parseInt(groupId);
               this.removeVars(activeChar);
               NpcHtmlMessage html = new NpcHtmlMessage(5);
               html.setFile(activeChar, activeChar.getLang(), "data/html/community/donate/index.htm");
               String template = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/community/donate/template.htm");
               String block = "";
               String list = "";
               List<Donation> _donate = DonationParser.getInstance().getGroup(id);
               int perpage = 6;
               int page = pageId.length() > 0 ? Integer.parseInt(pageId) : 1;
               int counter = 0;
               boolean isThereNextPage = _donate.size() > 6;

               for(int i = (page - 1) * 6; i < _donate.size(); ++i) {
                  Donation pack = _donate.get(i);
                  block = template.replace("{bypass}", "bypass _bbsservice:donateOpen " + pack.getId());
                  block = block.replace(
                     "{name}", activeChar.getLang() != null && !activeChar.getLang().equalsIgnoreCase("en") ? pack.getNameRu() : pack.getNameEn()
                  );
                  block = block.replace("{icon}", pack.getIcon());
                  SimpleList simple = pack.getSimple();
                  block = block.replace(
                     "{cost}",
                     ""
                        + ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.COST")
                        + ""
                        + Util.formatPay(activeChar, simple.getCount(), simple.getId())
                  );
                  list = list + block;
                  if (++counter >= 6) {
                     break;
                  }
               }

               double pages = (double)_donate.size() / 6.0;
               int count = (int)Math.ceil(pages);
               html.replace("%list%", list);
               html.replace("%navigation%", Util.getNavigationBlock(count, page, _donate.size(), 6, isThereNextPage, "_bbsservice:donateList " + id + " %s"));
               activeChar.sendPacket(html);
            }
         } else if (command.startsWith("_bbsservice:donateOpen")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            String groupId = st.nextToken();
            if (!Util.isNumber(groupId)) {
               return;
            }

            int id = Integer.parseInt(groupId);
            Donation donate = DonationParser.getInstance().getDonate(id);
            NpcHtmlMessage html = new NpcHtmlMessage(0);
            html.setFile(activeChar, activeChar.getLang(), "data/html/community/donate/open.htm");
            String content = "";
            Map<Integer, Long> price = new HashMap<>();
            html.replace("%name%", activeChar.getLang() != null && !activeChar.getLang().equalsIgnoreCase("en") ? donate.getNameRu() : donate.getNameEn());
            html.replace("%icon%", donate.getIcon());
            html.replace("%id%", String.valueOf(donate.getId()));
            html.replace("%group%", String.valueOf(donate.getGroup()));
            SimpleList simple = donate.getSimple();
            html.replace(
               "%cost%",
               ""
                  + ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.COST")
                  + ""
                  + Util.formatPay(activeChar, simple.getCount(), simple.getId())
            );
            price.put(simple.getId(), simple.getCount());
            if (donate.haveFound()) {
               boolean enchant = this.isVar(activeChar, _vars[0]);
               FoundList found = donate.getFound();
               String block = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/community/donate/foundation.htm");
               block = block.replace("{bypass}", "bypass _bbsservice:donateVar " + _vars[0] + " " + (enchant ? 0 : 1) + " " + donate.getId());
               block = block.replace(
                  "{status}",
                  enchant
                     ? "" + ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.BUY_IT") + ""
                     : "" + ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.NOT_BUY") + ""
               );
               block = block.replace(
                  "{cost}",
                  ""
                     + ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.COST")
                     + ""
                     + Util.formatPay(activeChar, found.getCount(), found.getId())
               );
               block = block.replace(
                  "{action}",
                  enchant
                     ? "" + ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.CANCEL") + ""
                     : "" + ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.BUY") + ""
               );
               if (enchant) {
                  this.updatePrice(price, found.getId(), found.getCount());
               }

               content = content + block;
            }

            Enchant enchant = donate.getEnchant();
            if (enchant != null) {
               boolean is = this.isVar(activeChar, _vars[1]);
               String block = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/community/donate/enchant.htm");
               block = block.replace("{bypass}", "bypass _bbsservice:donateVar " + _vars[1] + " " + (is ? 0 : 1) + " " + donate.getId());
               block = block.replace(
                  "{status}",
                  is
                     ? "" + ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.BUY_IT") + ""
                     : "" + ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.NOT_BUY") + ""
               );
               block = block.replace("{ench}", "+" + enchant.getEnchant());
               block = block.replace(
                  "{cost}",
                  ""
                     + ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.COST")
                     + ""
                     + Util.formatPay(activeChar, enchant.getCount(), enchant.getId())
               );
               block = block.replace(
                  "{action}",
                  is
                     ? "" + ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.CANCEL") + ""
                     : "" + ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.BUY") + ""
               );
               if (is) {
                  this.updatePrice(price, enchant.getId(), enchant.getCount());
               }

               content = content + block;
            }

            Attribution att = donate.getAttribution();
            if (att != null && att.getSize() >= 1) {
               boolean is = this.isVar(activeChar, _vars[2]);
               if (is && this.checkAttVars(activeChar, att.getSize())) {
                  is = false;
                  activeChar.unsetVar(_vars[2]);
                  this.onBypassCommand("_bbsservice:donateVar " + _vars[2] + " 0 0", activeChar);
               }

               String block = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/community/donate/attribute.htm");
               block = block.replace(
                  "{bypass}",
                  is ? "bypass _bbsservice:donateVar " + _vars[2] + " " + 0 + " " + donate.getId() : "bypass _bbsservice:donateAttr " + donate.getId()
               );
               block = block.replace(
                  "{status}",
                  is
                     ? "" + ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.BUY_IT") + ""
                     : "" + ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.NOT_BUY") + ""
               );
               block = block.replace(
                  "{cost}",
                  ""
                     + ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.COST")
                     + ""
                     + Util.formatPay(activeChar, att.getCount(), att.getId())
               );
               block = block.replace(
                  "{action}",
                  is
                     ? "" + ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.CANCEL") + ""
                     : "" + ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.BUY") + ""
               );
               if (is) {
                  this.updatePrice(price, att.getId(), att.getCount());
               }

               content = content + block;
            }

            String total = "";

            for(Entry<Integer, Long> map : price.entrySet()) {
               total = total + Util.formatPay(activeChar, map.getValue(), map.getKey()) + "<br1>";
            }

            html.replace("%content%", content);
            html.replace("%total%", total);
            activeChar.sendPacket(html);
         } else if (command.startsWith("_bbsservice:donateVar")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            String arg0 = st.nextToken();
            String arg1 = st.nextToken();
            String arg2 = st.nextToken();
            if (!Util.isNumber(arg1) || !Util.isNumber(arg2)) {
               return;
            }

            int action = Integer.parseInt(arg1);
            activeChar.addQuickVar(arg0, action);
            if (action == 0) {
               activeChar.deleteQuickVar(arg0);
               if (arg0.equals(_vars[2])) {
                  for(int i = 1; i <= 3; ++i) {
                     activeChar.deleteQuickVar("att_" + i);
                  }
               }
            }

            this.onBypassCommand("_bbsservice:donateOpen " + arg2, activeChar);
         } else if (command.startsWith("_bbsservice:donateAttr")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            String attrId = st.nextToken();
            if (!Util.isNumber(attrId)) {
               return;
            }

            int id = Integer.parseInt(attrId);
            Donation donate = DonationParser.getInstance().getDonate(id);
            if (donate == null) {
               return;
            }

            Attribution atribute = donate.getAttribution();
            if (atribute == null) {
               return;
            }

            if (atribute.getSize() < 1) {
               this.onBypassCommand("_bbsservice:donateOpen " + attrId, activeChar);
               return;
            }

            NpcHtmlMessage html = new NpcHtmlMessage(0);
            html.setFile(activeChar, activeChar.getLang(), "data/html/community/donate/attribute_choice.htm");
            html.replace("%name%", activeChar.getLang() != null && !activeChar.getLang().equalsIgnoreCase("en") ? donate.getNameRu() : donate.getNameEn());
            html.replace("%icon%", donate.getIcon());
            html.replace("%bypass%", "bypass _bbsservice:donateOpen " + donate.getId());
            html.replace("%value%", String.valueOf(atribute.getValue()));
            html.replace("%size%", String.valueOf(atribute.getSize()));
            html.replace("%id%", String.valueOf(donate.getId()));
            int att_1 = activeChar.getQuickVarI("att_1", -1);
            int att_2 = activeChar.getQuickVarI("att_2", -1);
            int att_3 = activeChar.getQuickVarI("att_3", -1);
            html.replace(
               "%att_1%",
               atribute.getSize() >= 1
                  ? (att_1 == -1 ? "..." : this.elementName(activeChar, att_1))
                  : "<font color=FF0000>" + ServerStorage.getInstance().getString(activeChar.getLang(), "CommunityGeneral.SLOT_BLOCK") + "</font>"
            );
            html.replace(
               "%att_2%",
               atribute.getSize() >= 2
                  ? (att_2 == -1 ? "..." : this.elementName(activeChar, att_2))
                  : "<font color=FF0000>" + ServerStorage.getInstance().getString(activeChar.getLang(), "CommunityGeneral.SLOT_BLOCK") + "</font>"
            );
            html.replace(
               "%att_3%",
               atribute.getSize() == 3
                  ? (att_3 == -1 ? "..." : this.elementName(activeChar, att_3))
                  : "<font color=FF0000>" + ServerStorage.getInstance().getString(activeChar.getLang(), "CommunityGeneral.SLOT_BLOCK") + "</font>"
            );
            this.build(activeChar, html, donate, att_1, att_2, att_3);
            activeChar.sendPacket(html);
         } else if (command.startsWith("_bbsservice:donatePut")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            String arg0 = st.nextToken();
            String arg1 = st.nextToken();
            if (!Util.isNumber(arg0) || !Util.isNumber(arg1)) {
               return;
            }

            int att = Integer.parseInt(arg1);
            if (activeChar.getQuickVarI("att_1", -1) == -1) {
               activeChar.addQuickVar("att_1", att);
            } else if (activeChar.getQuickVarI("att_2", -1) == -1) {
               activeChar.addQuickVar("att_2", att);
            } else if (activeChar.getQuickVarI("att_3", -1) == -1) {
               activeChar.addQuickVar("att_3", att);
            }

            activeChar.addQuickVar(_vars[2], 1);
            this.onBypassCommand("_bbsservice:donateAttr " + arg0, activeChar);
         } else if (command.startsWith("_bbsservice:donateClearAtt")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            String attId = st.nextToken();
            if (!Util.isNumber(attId)) {
               return;
            }

            for(int i = 1; i <= 3; ++i) {
               activeChar.deleteQuickVar("att_" + i);
            }

            this.onBypassCommand("_bbsservice:donateAttr " + attId, activeChar);
         } else if (command.startsWith("_bbsservice:donateBuy")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            String donateId = st.nextToken();
            if (!Util.isNumber(donateId)) {
               return;
            }

            int id = Integer.parseInt(donateId);
            Donation donate = DonationParser.getInstance().getDonate(id);
            if (donate == null) {
               return;
            }

            Map<Integer, Long> price = new HashMap<>();
            SimpleList simple = donate.getSimple();
            price.put(simple.getId(), simple.getCount());
            FoundList foundation = donate.getFound();
            boolean found_list = donate.haveFound() && foundation != null && activeChar.getQuickVarI(_vars[0], -1) != -1;
            if (found_list) {
               this.updatePrice(price, foundation.getId(), foundation.getCount());
            }

            Enchant enchant = donate.getEnchant();
            boolean enchanted = enchant != null && activeChar.getQuickVarI(_vars[1], -1) != -1;
            if (enchanted) {
               this.updatePrice(price, enchant.getId(), enchant.getCount());
            }

            Attribution att = donate.getAttribution();
            boolean attribution = att != null && activeChar.getQuickVarI(_vars[2], -1) != -1;
            if (attribution) {
               this.updatePrice(price, att.getId(), att.getCount());
            }

            for(Entry<Integer, Long> map : price.entrySet()) {
               int _id = map.getKey();
               long _count = map.getValue();
               if (activeChar.getInventory().getItemByItemId(_id) == null) {
                  activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                  return;
               }

               if (activeChar.getInventory().getItemByItemId(_id).getCount() < _count) {
                  activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                  return;
               }

               activeChar.destroyItemByItemId("DonateBBS", _id, _count, activeChar, true);
            }

            for(DonateItem _donate : found_list ? foundation.getList() : simple.getList()) {
               ItemInstance item = activeChar.getInventory().addItem("DonateBBS", _donate.getId(), _donate.getCount(), activeChar, true);
               int enchant_level = 0;
               if (enchanted) {
                  enchant_level = enchant.getEnchant();
               } else if (_donate.getEnchant() > 0) {
                  enchant_level = _donate.getEnchant();
               }

               if (enchant_level > 0) {
                  item.setEnchantLevel(enchant_level);
               }

               if ((item.isArmor() || item.isWeapon()) && attribution) {
                  for(int i = 1; i <= att.getSize(); ++i) {
                     int element_id = activeChar.getQuickVarI("att_" + i, -1);
                     if (element_id != -1) {
                        byte element = Elementals.getElementById(element_id);
                        if (item.isArmor()) {
                           element = Elementals.getReverseElement(element);
                        }

                        item.setElementAttr(element, att.getValue());
                     }
                  }
               }

               Util.addServiceLog(activeChar.getName() + " buy donate item: " + item.getName() + " +" + item.getEnchantLevel());
               InventoryUpdate iu = new InventoryUpdate();
               iu.addModifiedItem(item);
               activeChar.sendPacket(iu);
            }

            this.removeVars(activeChar);
            ServerMessage msg = new ServerMessage("ServiceBBS.YOU_BUY_ITEM", activeChar.getLang());
            msg.add(activeChar.getLang() != null && !activeChar.getLang().equalsIgnoreCase("en") ? donate.getNameRu() : donate.getNameEn());
            activeChar.sendMessage(msg.toString());
         } else if (command.equals("_bbsservice:cloak")) {
            int i = activeChar.getInventory().getPaperdollItemId(23);
            if (!this.isValidCloak(i)) {
               activeChar.sendMessage(new ServerMessage("ServiceBBS.CLOAK_EQUIPED", activeChar.getLang()).toString());
               return;
            }

            NpcHtmlMessage html = new NpcHtmlMessage(0);
            html.setFile(activeChar, activeChar.getLang(), "data/html/community/donate/cloaks.htm");
            html.replace("%price%", Util.formatPay(activeChar, (long)Config.SERVICES_SOUL_CLOAK_TRANSFER_ITEM[1], Config.SERVICES_SOUL_CLOAK_TRANSFER_ITEM[0]));
            activeChar.sendPacket(html);
         } else if (command.startsWith("_bbsservice:cloakSend")) {
            String str = null;
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();

            try {
               str = st.nextToken();
            } catch (Exception var24) {
            }

            if (str != null) {
               ItemInstance currentItem = activeChar.getInventory().getPaperdollItem(23);
               if (!this.isValidCloak(currentItem.getId())) {
                  activeChar.sendMessage(new ServerMessage("ServiceBBS.CLOAK_EQUIPED", activeChar.getLang()).toString());
                  return;
               }

               Player reciver = World.getInstance().getPlayer(str);
               if (reciver == null) {
                  ServerMessage msg = new ServerMessage("ServiceBBS.PLAYER_OFF", activeChar.getLang());
                  msg.add(str);
                  activeChar.sendMessage(msg.toString());
                  return;
               }

               if (activeChar.getInventory().getItemByItemId(Config.SERVICES_SOUL_CLOAK_TRANSFER_ITEM[0]) == null) {
                  activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                  return;
               }

               if (activeChar.getInventory().getItemByItemId(Config.SERVICES_SOUL_CLOAK_TRANSFER_ITEM[0]).getCount()
                  < (long)Config.SERVICES_SOUL_CLOAK_TRANSFER_ITEM[1]) {
                  activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                  return;
               }

               int i = currentItem.getId();
               if (activeChar.getInventory().destroyItemByObjectId(currentItem.getObjectId(), currentItem.getCount(), activeChar, Boolean.valueOf(true))
                  != null) {
                  activeChar.destroyItemByItemId(
                     "TransferBBS", Config.SERVICES_SOUL_CLOAK_TRANSFER_ITEM[0], (long)Config.SERVICES_SOUL_CLOAK_TRANSFER_ITEM[1], activeChar, true
                  );
                  Util.addServiceLog(activeChar.getName() + " buy transfer cloak service!");
                  ItemInstance newItem = reciver.getInventory().addItem("ExchangersBBS", i, 1L, reciver, true);
                  newItem.setEnchantLevel(currentItem.getEnchantLevel());
                  InventoryUpdate iu = new InventoryUpdate();
                  iu.addModifiedItem(newItem);
                  reciver.sendPacket(iu);
                  ServerMessage msg = new ServerMessage("ServiceBBS.TRANSFER_CLOAK", activeChar.getLang());
                  msg.add(activeChar.getItemName(newItem.getItem()));
                  msg.add(reciver.getName());
                  activeChar.sendMessage(msg.toString());
                  ServerMessage msg1 = new ServerMessage("ServiceBBS.SENDER_CLOAK", reciver.getLang());
                  msg1.add(activeChar.getName());
                  msg1.add(activeChar.getItemName(newItem.getItem()));
                  reciver.sendMessage(msg1.toString());
               }
            }
         } else if (command.equals("_bbsservice:olfShirt")) {
            NpcHtmlMessage htm = new NpcHtmlMessage(0);
            htm.setFile(activeChar, activeChar.getLang(), "data/html/community/donate/olfstore.htm");
            activeChar.sendPacket(htm);
         } else if (command.startsWith("_bbsservice:olfShirtBuy")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            String olfId = st.nextToken();
            if (!Util.isNumber(olfId)) {
               return;
            }

            int enchant = Integer.parseInt(olfId);
            int price;
            switch(enchant) {
               case 0:
                  price = Config.SERVICES_OLF_STORE_0_PRICE;
                  break;
               case 1:
               case 2:
               case 3:
               case 4:
               case 5:
               default:
                  return;
               case 6:
                  price = Config.SERVICES_OLF_STORE_6_PRICE;
                  break;
               case 7:
                  price = Config.SERVICES_OLF_STORE_7_PRICE;
                  break;
               case 8:
                  price = Config.SERVICES_OLF_STORE_8_PRICE;
                  break;
               case 9:
                  price = Config.SERVICES_OLF_STORE_9_PRICE;
                  break;
               case 10:
                  price = Config.SERVICES_OLF_STORE_10_PRICE;
            }

            if (activeChar.getInventory().getItemByItemId(Config.SERVICES_OLF_STORE_ITEM) == null) {
               activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            if (activeChar.getInventory().getItemByItemId(Config.SERVICES_OLF_STORE_ITEM).getCount() < (long)price) {
               activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            activeChar.destroyItemByItemId("DonateOLFBBS", Config.SERVICES_OLF_STORE_ITEM, (long)price, activeChar, true);
            ItemInstance item = activeChar.getInventory().addItem("DonateOLFBBS", 21580, 1L, activeChar, true);
            item.setEnchantLevel(enchant);
            Util.addServiceLog(activeChar.getName() + " buy OLF +" + enchant);
            InventoryUpdate iu = new InventoryUpdate();
            iu.addModifiedItem(item);
            activeChar.sendPacket(iu);
            activeChar.getInventory().unEquipItemInBodySlot(item.getItem().getBodyPart());
            activeChar.getInventory().equipItem(item);
            ServerMessage msg = new ServerMessage("ServiceBBS.YOU_BUY_ITEM", activeChar.getLang());
            msg.add(activeChar.getItemName(item.getItem()));
            activeChar.sendMessage(msg.toString());
         } else if (command.equals("_bbsservice:olfTransfer")) {
            int i = activeChar.getInventory().getPaperdollItemId(0);
            int j = 21580;
            if (i != 21580) {
               activeChar.sendMessage(new ServerMessage("ServiceBBS.OLF_EQUIPED", activeChar.getLang()).toString());
               return;
            }

            NpcHtmlMessage htm = new NpcHtmlMessage(0);
            htm.setFile(activeChar, activeChar.getLang(), "data/html/community/donate/olftransfer.htm");
            htm.replace("%price%", Util.formatPay(activeChar, (long)Config.SERVICES_OLF_TRANSFER_ITEM[1], Config.SERVICES_OLF_TRANSFER_ITEM[0]));
            activeChar.sendPacket(htm);
         } else if (command.startsWith("_bbsservice:olfShirtTransfer")) {
            String str = null;
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();

            try {
               str = st.nextToken();
            } catch (Exception var23) {
            }

            if (str != null) {
               int itemId = 21580;
               ItemInstance currentItem = activeChar.getInventory().getPaperdollItem(0);
               if (currentItem == null || currentItem.getId() != 21580) {
                  activeChar.sendMessage(new ServerMessage("ServiceBBS.OLF_EQUIPED", activeChar.getLang()).toString());
                  return;
               }

               Player reciver = World.getInstance().getPlayer(str);
               if (reciver == null) {
                  ServerMessage msg = new ServerMessage("ServiceBBS.PLAYER_OFF", activeChar.getLang());
                  msg.add(str);
                  activeChar.sendMessage(msg.toString());
                  return;
               }

               if (activeChar.getInventory().getItemByItemId(Config.SERVICES_OLF_TRANSFER_ITEM[0]) == null) {
                  activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                  return;
               }

               if (activeChar.getInventory().getItemByItemId(Config.SERVICES_OLF_TRANSFER_ITEM[0]).getCount() < (long)Config.SERVICES_OLF_TRANSFER_ITEM[1]) {
                  activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                  return;
               }

               if (activeChar.getInventory().destroyItemByObjectId(currentItem.getObjectId(), currentItem.getCount(), activeChar, Boolean.valueOf(true))
                  != null) {
                  activeChar.destroyItemByItemId(
                     "TransferBBS", Config.SERVICES_OLF_TRANSFER_ITEM[0], (long)Config.SERVICES_OLF_TRANSFER_ITEM[1], activeChar, true
                  );
                  Util.addServiceLog(activeChar.getName() + " buy OLF transfer service!");
                  ItemInstance newItem = reciver.getInventory().addItem("ExchangersBBS", 21580, 1L, reciver, true);
                  newItem.setEnchantLevel(currentItem.getEnchantLevel());
                  InventoryUpdate iu = new InventoryUpdate();
                  iu.addModifiedItem(newItem);
                  reciver.sendPacket(iu);
                  ServerMessage msg = new ServerMessage("ServiceBBS.TRANSFER_CLOAK", activeChar.getLang());
                  msg.add(activeChar.getItemName(newItem.getItem()));
                  msg.add(reciver.getName());
                  activeChar.sendMessage(msg.toString());
                  ServerMessage msg1 = new ServerMessage("ServiceBBS.SENDER_CLOAK", reciver.getLang());
                  msg1.add(activeChar.getName());
                  msg1.add(activeChar.getItemName(newItem.getItem()));
                  reciver.sendMessage(msg1.toString());
               }
            }
         } else if (command.startsWith("_bbsservice:newSubPage")) {
            String race = null;
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();

            try {
               race = st.nextToken();
            } catch (Exception var22) {
            }

            addNewSubPage(activeChar, race);
         } else if (command.startsWith("_bbsservice:addNewSub")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            int subclassId = Integer.parseInt(st.nextToken());
            addNewSub(activeChar, subclassId);
         } else if (command.equals("_bbsservice:changeSubPage")) {
            changeSubPage(activeChar);
         } else if (command.startsWith("_bbsservice:changeSubTo")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            int subclassId = Integer.parseInt(st.nextToken());
            changeSub(activeChar, subclassId);
         } else if (command.equals("_bbsservice:cancelSubPage")) {
            cancelSubPage(activeChar);
         } else if (command.startsWith("_bbsservice:selectCancelSub")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            int subclassId = Integer.parseInt(st.nextToken());
            activeChar.addQuickVar("SubToRemove", subclassId);
            sendFileToPlayer(activeChar, "data/html/community/subclass/subclassChanger_add.htm");
         } else if (command.equals("_bbsservice:chooseCertificate")) {
            chooseCertificatePage(activeChar);
         } else if (command.startsWith("_bbsservice:giveCertificate")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            String certifications = st.nextToken();
            if (!activeChar.isSubClassActive()) {
               activeChar.sendMessage(new ServerMessage("CommunityGeneral.SUB_NOT_ACTIVE", activeChar.getLang()).toString());
               return;
            }

            if (certifications.equals("CommunityCert65")) {
               this.CommunityCert65(activeChar);
            } else if (certifications.equals("CommunityCert70")) {
               this.CommunityCert70(activeChar);
            } else if (certifications.equals("CommunityCert75Class")) {
               this.CommunityCert75Class(activeChar);
            } else if (certifications.equals("CommunityCert75Master")) {
               this.CommunityCert75Master(activeChar);
            } else if (certifications.equals("CommunityCert80")) {
               this.CommunityCert80(activeChar);
            }
         }
      }
   }

   protected void getCertified(Player player, int itemId, String var) {
      QuestState st = player.getQuestState("SubClassSkills");
      String qvar = st.getGlobalQuestVar(var);
      if (qvar.equals("") || qvar.equals("0")) {
         ItemInstance item = player.getInventory().addItem("Quest", itemId, 1L, player, player.getTarget());
         st.saveGlobalQuestVar(var, "" + item.getObjectId());
         player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1).addItemName(item));
      }
   }

   protected void CommunityCert65(Player player) {
      if (this.checkCertificationCondition(player, 65, "EmergentAbility65-")) {
         this.getCertified(player, 10280, "EmergentAbility65-" + player.getClassIndex());
         this.onBypassCommand("_bbsservice:chooseCertificate", player);
      }
   }

   protected void CommunityCert70(Player player) {
      if (this.checkCertificationCondition(player, 70, "EmergentAbility70-")) {
         this.getCertified(player, 10280, "EmergentAbility70-" + player.getClassIndex());
         this.onBypassCommand("_bbsservice:chooseCertificate", player);
      }
   }

   protected void CommunityCert75Class(Player player) {
      if (this.checkCertificationCondition(player, 75, "ClassAbility75-")) {
         this.getCertified(player, CLASSITEMS[CertificationUtils.getClassIndex(player)], "ClassAbility75-" + player.getClassIndex());
         this.onBypassCommand("_bbsservice:chooseCertificate", player);
      }
   }

   protected void CommunityCert75Master(Player player) {
      if (this.checkCertificationCondition(player, 75, "ClassAbility75-")) {
         this.getCertified(player, 10612, "ClassAbility75-" + player.getClassIndex());
         this.onBypassCommand("_bbsservice:chooseCertificate", player);
      }
   }

   protected void CommunityCert80(Player player) {
      if (this.checkCertificationCondition(player, 80, "ClassAbility80-")) {
         this.getCertified(player, TRANSFORMITEMS[CertificationUtils.getClassIndex(player)], "ClassAbility80-" + player.getClassIndex());
         this.onBypassCommand("_bbsservice:chooseCertificate", player);
      }
   }

   private boolean checkCertificationCondition(Player player, int requiredLevel, String index) {
      boolean failed = false;
      if (player.getLevel() < requiredLevel) {
         player.sendMessage(new ServerMessage("CommunityGeneral.YOU_LEVEL_LOW", player.getLang()).toString());
         failed = true;
      }

      QuestState st = player.getQuestState("SubClassSkills");
      if (st == null) {
         Quest subClassSkilllsQuest = QuestManager.getInstance().getQuest("SubClassSkills");
         if (subClassSkilllsQuest == null) {
            _log.warning("Null SubClassSkills quest, for Certification level: " + requiredLevel + " for player " + player.getName() + "!");
            return false;
         }

         st = subClassSkilllsQuest.newQuestState(player);
      }

      String CertificationIndex = st.getGlobalQuestVar(index + player.getClassIndex());
      if (Util.isDigit(CertificationIndex)) {
         player.sendMessage(new ServerMessage("CommunityGeneral.HAVE_CETRIFICATION", player.getLang()).toString());
         failed = true;
      }

      if (failed) {
         sendFileToPlayer(player, "data/html/community/subclass/subclassChanger.htm");
         return false;
      } else {
         return true;
      }
   }

   private static void chooseCertificatePage(Player player) {
      if (canChangeClass(player)) {
         if (player.getBaseClass() == player.getClassId().getId()) {
            sendFileToPlayer(player, "data/html/community/subclass/subclassChanger_back.htm");
         } else {
            String[][] certifications = new String[][]{
               {ServerStorage.getInstance().getString(player.getLang(), "CommunityGeneral.CERT65_EMERGENT"), "CommunityCert65"},
               {ServerStorage.getInstance().getString(player.getLang(), "CommunityGeneral.CERT70_EMERGENT"), "CommunityCert70"},
               {ServerStorage.getInstance().getString(player.getLang(), "CommunityGeneral.CERT75_CLASS"), "CommunityCert75Class"},
               {ServerStorage.getInstance().getString(player.getLang(), "CommunityGeneral.CERT75_MASTER"), "CommunityCert75Master"},
               {ServerStorage.getInstance().getString(player.getLang(), "CommunityGeneral.CERT80_DIVINE"), "CommunityCert80"}
            };
            String[] replacements = new String[22];

            for(int i = 0; i < 11; ++i) {
               replacements[i * 2] = "%sub" + i + '%';
               if (certifications.length <= i) {
                  replacements[i * 2 + 1] = "<br>";
               } else {
                  String[] button = certifications[i];
                  replacements[i * 2 + 1] = "<button value=\""
                     + button[0]
                     + "\" action=\"bypass _bbsservice:giveCertificate "
                     + button[1]
                     + "\" width=300 height=30 back=\"L2UI_CT1.ListCTRL_DF_Title_Down\" fore=\"L2UI_CT1.ListCTRL_DF_Title\">";
               }
            }

            sendFileToPlayer(player, "data/html/community/subclass/subclassChanger_select_cert.htm", replacements);
         }
      }
   }

   private static void cancelSubPage(Player player) {
      List<SubClass> subToChoose = new ArrayList<>();

      for(SubClass sub : player.getSubClasses().values()) {
         if (sub.getClassId() != player.getBaseClass()) {
            subToChoose.add(sub);
         }
      }

      String[] replacements = new String[22];

      for(int i = 0; i < 11; ++i) {
         replacements[i * 2] = "%sub" + i + '%';
         if (subToChoose.size() <= i) {
            replacements[i * 2 + 1] = "<br>";
         } else {
            SubClass playerClass = subToChoose.get(i);
            replacements[i * 2 + 1] = "<button value=\""
               + Util.className(player, playerClass.getClassId())
               + "\" action=\"bypass _bbsservice:selectCancelSub "
               + playerClass.getClassIndex()
               + "\" width=200 height=30 back=\"L2UI_CT1.OlympiadWnd_DF_Fight1None_Down\" fore=\"L2UI_ct1.OlympiadWnd_DF_Fight1None\">";
         }
      }

      sendFileToPlayer(player, "data/html/community/subclass/subclassChanger_select_remove.htm", replacements);
   }

   private static void changeSub(Player player, int subId) {
      if (canChangeClass(player)) {
         player.setActiveClass(subId);
         player.sendPacket(SystemMessageId.SUBCLASS_TRANSFER_COMPLETED);
         player.sendPacket(new ShowBoard());
      }
   }

   private static void changeSubPage(Player player) {
      Collection<SubClass> allSubs = player.getSubClasses().values();
      List<Integer> classId = new ArrayList<>();
      List<Integer> classIndex = new ArrayList<>();
      if (player.getActiveClass() != player.getBaseClass()) {
         classId.add(player.getBaseClass());
         classIndex.add(0);
      }

      for(SubClass sub : allSubs) {
         if (sub.getClassId() != player.getActiveClass()) {
            classId.add(sub.getClassId());
            classIndex.add(sub.getClassIndex());
         }
      }

      String[] replacements = new String[22];

      for(int i = 0; i < 11; ++i) {
         replacements[i * 2] = "%sub" + i + '%';
         if (classId.size() <= i) {
            replacements[i * 2 + 1] = "<br>";
         } else {
            int playerClassId = classId.get(i);
            int playerClassIndex = classIndex.get(i);
            replacements[i * 2 + 1] = "<button value=\""
               + Util.className(player, playerClassId)
               + "\" action=\"bypass _bbsservice:changeSubTo "
               + playerClassIndex
               + "\" width=200 height=30 back=\"L2UI_CT1.OlympiadWnd_DF_Fight1None_Down\" fore=\"L2UI_ct1.OlympiadWnd_DF_Fight1None\">";
         }
      }

      sendFileToPlayer(player, "data/html/community/subclass/subclassChanger_select_change.htm", replacements);
   }

   private static void addNewSub(Player player, int subclassId) {
      if (canChangeClass(player)) {
         int subToRemove = player.getQuickVarI("SubToRemove");
         boolean added;
         if (subToRemove > 0) {
            added = player.modifySubClass(subToRemove, subclassId);
            if (!added) {
               player.setActiveClass(0);
               player.sendMessage(new ServerMessage("CommunityGeneral.CANT_ADD_SUB", player.getLang()).toString());
               return;
            }

            player.abortCast();
            player.stopAllEffectsExceptThoseThatLastThroughDeath();
            player.stopAllEffectsNotStayOnSubclassChange();
            player.stopCubics();
            player.setActiveClass(subToRemove);
            InitialShortcutParser.getInstance().registerAllShortcuts(player);
            player.sendPacket(new ShortCutInit(player));
            player.deleteQuickVar("SubToRemove");
         } else {
            added = addNewSubclass(player, subclassId);
         }

         if (added) {
            player.sendPacket(SystemMessageId.ADD_NEW_SUBCLASS);
         } else {
            player.sendMessage(new ServerMessage("CommunityGeneral.CANT_ADD_SUB1", player.getLang()).toString());
         }

         player.sendPacket(new ShowBoard());
      }
   }

   private static boolean addNewSubclass(Player player, int classId) {
      if (player.getTotalSubClasses() >= Config.MAX_SUBCLASS) {
         return false;
      } else if (player.getLevel() < 75) {
         return false;
      } else {
         if (!player.getSubClasses().isEmpty()) {
            Iterator<SubClass> subList = iterSubClasses(player);

            while(subList.hasNext()) {
               SubClass subClass = subList.next();
               if (subClass.getLevel() < 75) {
                  return false;
               }
            }
         }

         if (!Config.ALT_GAME_SUBCLASS_WITHOUT_QUESTS) {
            checkQuests(player);
         }

         if (isValidNewSubClass(player, classId)) {
            if (!player.addSubClass(classId, player.getTotalSubClasses() + 1)) {
               return false;
            }

            player.setActiveClass(player.getTotalSubClasses());
            InitialShortcutParser.getInstance().registerAllShortcuts(player);
            player.sendPacket(new ShortCutInit(player));
         }

         return true;
      }
   }

   protected static boolean checkQuests(Player player) {
      if (player.isNoble()) {
         return true;
      } else {
         QuestState qs = player.getQuestState("_234_FatesWhisper");
         if (qs != null && qs.isCompleted()) {
            qs = player.getQuestState("_235_MimirsElixir");
            return qs != null && qs.isCompleted();
         } else {
            return false;
         }
      }
   }

   private static boolean isValidNewSubClass(Player player, int classId) {
      ClassId cid = ClassId.values()[classId];
      Iterator<SubClass> subList = iterSubClasses(player);

      while(subList.hasNext()) {
         SubClass sub = subList.next();
         ClassId subClassId = ClassId.values()[sub.getClassId()];
         if (subClassId.equalsOrChildOf(cid)) {
            return false;
         }
      }

      int currentBaseId = player.getBaseClass();
      ClassId baseCID = ClassId.getClassId(currentBaseId);
      int baseClassId;
      if (baseCID.level() > 2) {
         baseClassId = baseCID.getParent().ordinal();
      } else {
         baseClassId = currentBaseId;
      }

      Set<PlayerClass> availSubs = PlayerClass.values()[baseClassId].getAvailableSubclasses(player);
      if (availSubs != null && !availSubs.isEmpty()) {
         boolean found = false;

         for(PlayerClass pclass : availSubs) {
            if (pclass.ordinal() == classId) {
               found = true;
               break;
            }
         }

         return found;
      } else {
         return false;
      }
   }

   private static boolean canChangeClass(Player player) {
      if (player.hasSummon()) {
         player.sendPacket(SystemMessageId.CANT_SUBCLASS_WITH_SUMMONED_SERVITOR);
         return false;
      } else if (player.isCastingNow() || player.isAllSkillsDisabled() || player.getTransformation() != null) {
         player.sendPacket(SystemMessageId.SUBCLASS_NO_CHANGE_OR_CREATE_WHILE_SKILL_IN_USE);
         return false;
      } else if (player.isInsideZone(ZoneId.PEACE) && player.getDuelState() == 0) {
         return true;
      } else {
         player.sendMessage(new ServerMessage("CommunityGeneral.CANT_SUB", player.getLang()).toString());
         return false;
      }
   }

   private static void addNewSubPage(Player player, String raceName) {
      Race race = Race.valueOf(raceName);
      Set<PlayerClass> allSubs = getAvailableSubClasses(player);
      if (allSubs != null) {
         allSubs = getSubsByRace(allSubs, race);
         PlayerClass[] arraySubs = new PlayerClass[allSubs.size()];
         arraySubs = allSubs.toArray(arraySubs);
         String[] replacements = new String[22];

         for(int i = 0; i < 11; ++i) {
            replacements[i * 2] = "%sub" + i + '%';
            if (arraySubs.length <= i) {
               replacements[i * 2 + 1] = "<br>";
            } else {
               PlayerClass playerClass = arraySubs[i];
               replacements[i * 2 + 1] = "<button value=\""
                  + Util.className(player, playerClass.name())
                  + "\" action=\"bypass _bbsservice:addNewSub "
                  + playerClass.ordinal()
                  + "\" width=200 height=30 back=\"L2UI_CT1.OlympiadWnd_DF_Fight1None_Down\" fore=\"L2UI_ct1.OlympiadWnd_DF_Fight1None\">";
            }
         }

         sendFileToPlayer(player, "data/html/community/subclass/subclassChanger_select_add.htm", replacements);
      }
   }

   private static final Set<PlayerClass> getAvailableSubClasses(Player player) {
      int currentBaseId = player.getBaseClass();
      ClassId baseCID = ClassId.getClassId(currentBaseId);
      int baseClassId;
      if (baseCID.level() > 2) {
         baseClassId = baseCID.getParent().ordinal();
      } else {
         baseClassId = currentBaseId;
      }

      Set<PlayerClass> availSubs = PlayerClass.values()[baseClassId].getAvailableSubclasses(player);
      if (availSubs != null && !availSubs.isEmpty()) {
         Iterator<PlayerClass> availSub = availSubs.iterator();

         while(availSub.hasNext()) {
            PlayerClass pclass = availSub.next();
            int availClassId = pclass.ordinal();
            ClassId cid = ClassId.getClassId(availClassId);
            Iterator<SubClass> subList = iterSubClasses(player);

            while(subList.hasNext()) {
               SubClass prevSubClass = subList.next();
               ClassId subClassId = ClassId.getClassId(prevSubClass.getClassId());
               if (subClassId.equalsOrChildOf(cid)) {
                  availSub.remove();
                  break;
               }
            }
         }
      }

      return availSubs;
   }

   private static final Iterator<SubClass> iterSubClasses(Player player) {
      return player.getSubClasses().values().iterator();
   }

   private static Set<PlayerClass> getSubsByRace(Set<PlayerClass> allSubs, Race race) {
      for(PlayerClass sub : allSubs) {
         if (sub != null && !sub.isOfRace(race)) {
            allSubs.remove(sub);
         }
      }

      return allSubs;
   }

   private boolean isValidCloak(int paramInt) {
      int[] arrayOfInt1 = new int[]{21719, 21720, 21721};

      for(int k : arrayOfInt1) {
         if (paramInt == k) {
            return true;
         }
      }

      return false;
   }

   private NpcHtmlMessage build(Player player, NpcHtmlMessage html, Donation donate, int att_1, int att_2, int att_3) {
      String slotclose = "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">";
      int id = donate.getId();
      int size = donate.getAttribution().getSize();
      boolean block = false;
      if (size != 1 || att_1 == -1 && att_2 == -1 && att_3 == -1) {
         if (size != 2 || att_1 == -1 && att_2 == -1 || att_1 == -1 && att_3 == -1 || att_2 == -1 && att_3 == -1) {
            if (size == 3 && att_1 != -1 && att_2 != -1 && att_3 != -1) {
               block = true;
            }
         } else {
            block = true;
         }
      } else {
         block = true;
      }

      boolean one = this.block(player, 0, 1) || block;
      String fire = one ? "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">" : this.button(0, id);
      String water = one ? "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">" : this.button(1, id);
      boolean two = this.block(player, 2, 3) || block;
      String wind = two ? "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">" : this.button(2, id);
      String earth = two ? "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">" : this.button(3, id);
      boolean three = this.block(player, 4, 5) || block;
      String holy = three ? "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">" : this.button(4, id);
      String unholy = three ? "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">" : this.button(5, id);
      html.replace("%fire%", fire);
      html.replace("%water%", water);
      html.replace("%wind%", wind);
      html.replace("%earth%", earth);
      html.replace("%holy%", holy);
      html.replace("%unholy%", unholy);
      return html;
   }

   private String button(int att, int id) {
      return "<button action=\"bypass _bbsservice:donatePut "
         + id
         + " "
         + att
         + "\" width=34 height=34 back=\"L2UI_CT1.ItemWindow_DF_Frame_Down\" fore=\"L2UI_CT1.ItemWindow_DF_Frame\"/>";
   }

   private boolean block(Player player, int id, int id2) {
      for(int i = 1; i <= 3; ++i) {
         int var = player.getQuickVarI("att_" + i, -1);
         if (var == id || var == id2) {
            return true;
         }
      }

      return false;
   }

   private String elementName(Player player, int id) {
      String name = "";
      switch(id) {
         case 0:
            name = "" + ServerStorage.getInstance().getString(player.getLang(), "ServiceBBS.ATTR_FIRE") + "";
            break;
         case 1:
            name = "" + ServerStorage.getInstance().getString(player.getLang(), "ServiceBBS.ATTR_WATER") + "";
            break;
         case 2:
            name = "" + ServerStorage.getInstance().getString(player.getLang(), "ServiceBBS.ATTR_WIND") + "";
            break;
         case 3:
            name = "" + ServerStorage.getInstance().getString(player.getLang(), "ServiceBBS.ATTR_EARTH") + "";
            break;
         case 4:
            name = "" + ServerStorage.getInstance().getString(player.getLang(), "ServiceBBS.ATTR_HOLY") + "";
            break;
         case 5:
            name = "" + ServerStorage.getInstance().getString(player.getLang(), "ServiceBBS.ATTR_DARK") + "";
            break;
         default:
            name = "NONE";
      }

      return name;
   }

   private boolean isVar(Player player, String var) {
      return player.getQuickVarI(var, 0) != 0;
   }

   private boolean checkAttVars(Player player, int size) {
      int count = 0;

      for(int i = 1; i <= 3; ++i) {
         int var = player.getQuickVarI("att_" + i, -1);
         if (var != -1) {
            ++count;
         }
      }

      return count != size;
   }

   private void updatePrice(Map<Integer, Long> price, int id, long count) {
      if (price.containsKey(id)) {
         price.put(id, count + price.get(id));
      } else {
         price.put(id, count);
      }
   }

   private void removeVars(Player player) {
      for(String var : _vars) {
         player.deleteQuickVar(var);
      }

      for(int i = 1; i <= 3; ++i) {
         player.deleteQuickVar("att_" + i);
      }
   }

   private void cleanAtt(Player player, int exclude) {
      if (player != null) {
         for(Elementals.Elemental att : Elementals.Elemental.VALUES) {
            if (att.getId() != exclude) {
               player.deleteQuickVar("ex_att_" + att.getId());
            }
         }

         if (exclude == -1) {
            player.deleteQuickVar("ex_att");
         }
      }
   }

   private void removeVars(Player player, boolean exchange) {
      if (player != null) {
         if (exchange) {
            player.deleteQuickVar("exchange");
         }

         player.deleteQuickVar("exchange_obj");
         player.deleteQuickVar("exchange_new");
         player.deleteQuickVar("exchange_attribute");
      }
   }

   protected void showMainMenu(Player player, int _page, Options.AugmentationFilter _filter) {
      if (_page < 1) {
         NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
         adminReply.setFile(player, player.getLang(), "data/html/community/augmentations/index.htm");
         player.sendPacket(adminReply);
      } else {
         Map<Integer, Options> _augments = new HashMap<>();
         Collection<Options> augmentations = Config.SERVICES_AUGMENTATION_FORMATE
            ? OptionsParser.getInstance().getUniqueAvailableOptions(_filter)
            : OptionsParser.getInstance().getUniqueOptions(_filter);
         int counts = 0;
         if (augmentations.isEmpty()) {
            this.showMainMenu(player, 0, Options.AugmentationFilter.NONE);
            player.sendMessage(new ServerMessage("ServiceBBS.AUGMENT_EMPTY", player.getLang()).toString());
         } else {
            NpcHtmlMessage adminReply = new NpcHtmlMessage(0);
            adminReply.setFile(player, player.getLang(), "data/html/community/augmentations/list.htm");
            String template = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/augmentations/template.htm");

            for(Options augm : augmentations) {
               if (augm != null) {
                  _augments.put(++counts, augm);
               }
            }

            String block = "";
            String list = "";
            int perpage = 6;
            boolean isThereNextPage = _augments.size() > 6;
            int count = 0;
            boolean lastColor = true;

            for(int i = (_page - 1) * 6; i < _augments.size(); ++i) {
               Options augm = _augments.get(i + 1);
               if (augm != null) {
                  Skill skill = null;
                  if (augm.hasActiveSkill()) {
                     skill = augm.getActiveSkill().getSkill();
                  } else if (augm.hasPassiveSkill()) {
                     skill = augm.getPassiveSkill().getSkill();
                  } else if (augm.hasActivationSkills()) {
                     skill = augm.getActivationsSkills().get(0).getSkill();
                  }

                  block = template.replace("{bypass}", "bypass _bbsservice:augmentationPut " + augm.getId() + " " + _page + " " + (_filter.ordinal() + 1));
                  String name = "";
                  if (skill != null) {
                     name = player.getSkillName(skill).length() > 30 ? player.getSkillName(skill).substring(0, 30) : player.getSkillName(skill);
                  } else {
                     switch(augm.getId()) {
                        case 16341:
                           name = "+1 " + ServerStorage.getInstance().getString(player.getLang(), "ServiceBBS.STR") + "";
                           break;
                        case 16342:
                           name = "+1 " + ServerStorage.getInstance().getString(player.getLang(), "ServiceBBS.CON") + "";
                           break;
                        case 16343:
                           name = "+1 " + ServerStorage.getInstance().getString(player.getLang(), "ServiceBBS.INT") + "";
                           break;
                        case 16344:
                           name = "+1 " + ServerStorage.getInstance().getString(player.getLang(), "ServiceBBS.MEN") + "";
                           break;
                        default:
                           name = "(Id:" + augm.getId() + ")";
                     }
                  }

                  block = block.replace("{name}", name);
                  block = block.replace("{icon}", skill != null ? skill.getIcon() : "icon.skill5041");
                  block = block.replace("{color}", lastColor ? "222222" : "333333");
                  block = block.replace(
                     "{price}",
                     Util.formatAdena((long)Config.SERVICES_AUGMENTATION_ITEM[1])
                        + " "
                        + player.getItemName(ItemsParser.getInstance().getTemplate(Config.SERVICES_AUGMENTATION_ITEM[0]))
                  );
                  list = list + block;
                  lastColor = !lastColor;
                  if (++count >= 6) {
                     break;
                  }
               }
            }

            double pages = (double)_augments.size() / 6.0;
            int countss = (int)Math.ceil(pages);
            adminReply.replace(
               "%pages%",
               Util.getNavigationBlock(countss, _page, _augments.size(), 6, isThereNextPage, "_bbsservice:augmentationPage " + (_filter.ordinal() + 1) + " %s")
            );
            adminReply.replace("%augs%", list);
            player.sendPacket(adminReply);
         }
      }
   }

   private void unAugment(Player player, ItemInstance item) {
      if (item.isAugmented()) {
         boolean equipped = item.isEquipped();
         if (equipped) {
            player.getInventory().unEquipItemInSlot(5);
         }

         item.removeAugmentation();
         if (equipped) {
            player.getInventory().equipItem(item);
         }

         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.AUGMENTATION_HAS_BEEN_SUCCESSFULLY_REMOVED_FROM_YOUR_S1);
         sm.addItemName(item.getId());
         player.sendPacket(sm);
         player.sendPacket(new ExVariationCancelResult(1));
         InventoryUpdate iu = new InventoryUpdate();
         iu.addModifiedItem(item);
         player.sendPacket(iu);
      }
   }

   protected boolean checkItemType(ItemInstance item) {
      if (!item.isHeroItem() && !item.isShadowItem() && !item.isCommonItem()) {
         switch(item.getId()) {
            case 13752:
            case 13753:
            case 13754:
            case 13755:
               return false;
            default:
               if (item.isPvp() && !Config.ALT_ALLOW_AUGMENT_PVP_ITEMS) {
                  return false;
               } else if (item.getItem().getCrystalType() < 2) {
                  return false;
               } else {
                  switch(((Weapon)item.getItem()).getItemType()) {
                     case NONE:
                     case FISHINGROD:
                        return false;
                     default:
                        return true;
                  }
               }
         }
      } else {
         return false;
      }
   }

   protected void checkClanSkills(Player player, int page) {
      Map<Integer, SkillLearn> _skills = new HashMap<>();
      List<SkillLearn> skills = SkillTreesParser.getInstance().getAvailablePledgeSkills(player.getClan());
      int counts = 0;

      for(SkillLearn skill : skills) {
         if (skill != null) {
            _skills.put(++counts, skill);
         }
      }

      int perpage = 6;
      boolean isThereNextPage = _skills.size() > 6;
      NpcHtmlMessage html = new NpcHtmlMessage(5);
      html.setFile(player, player.getLang(), "data/html/community/donate/clanskills.htm");
      String template = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/donate/clanskills-template.htm");
      String block = "";
      String list = "";
      int countss = 0;

      for(int i = (page - 1) * 6; i < _skills.size(); ++i) {
         SkillLearn skill = _skills.get(i + 1);
         if (skill != null) {
            String skillId = Integer.toString(skill.getId());
            String icon;
            if (skillId.length() == 3) {
               icon = 0 + skillId;
            } else {
               icon = skillId;
            }

            block = template.replace("%bypassBuy%", "bypass _bbslearnclanskills " + skill.getId() + " " + skill.getLvl() + " " + page);
            block = block.replace("%name%", player.getSkillName(SkillsParser.getInstance().getInfo(skill.getId(), skill.getLvl())));
            block = block.replace("%price%", Util.formatPay(player, (long)Config.SERVICES_CLANSKILLS_ITEM[1], Config.SERVICES_CLANSKILLS_ITEM[0]));
            block = block.replace("%icon%", icon);
            list = list + block;
            if (++countss >= 6) {
               break;
            }
         }
      }

      double pages = (double)_skills.size() / 6.0;
      int count = (int)Math.ceil(pages);
      if (counts == 0) {
         if (player.getClan().getLevel() < 8) {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN_S1);
            if (player.getClan().getLevel() < 5) {
               sm.addNumber(5);
            } else {
               sm.addNumber(player.getClan().getLevel() + 1);
            }

            player.sendPacket(sm);
         } else {
            player.sendMessage(new ServerMessage("ServiceBBS.ALL_CLAN_SKILLS", player.getLang()).toString());
         }
      } else {
         html.replace("%list%", list);
         html.replace("%navigation%", Util.getNavigationBlock(count, page, _skills.size(), 6, isThereNextPage, "_bbslistclanskills %s"));
         player.sendPacket(html);
      }
   }

   protected boolean unbanChar(Player activeChar, String affect, String key) {
      boolean sucsess = false;
      PunishmentAffect af = PunishmentAffect.getByName(affect);
      switch(af) {
         case CHARACTER:
            sucsess = this.unbanCharById(activeChar, key);
            break;
         case ACCOUNT:
            sucsess = this.unbanCharByAcc(activeChar, key);
            break;
         case IP:
            sucsess = this.unbanCharByIP(activeChar, key);
      }

      return sucsess;
   }

   protected boolean unbanCharByIP(Player activeChar, String ip) {
      boolean bool = false;
      String ipAddress = null;

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement ps = con.prepareStatement("SELECT `lastIP` FROM `accounts` WHERE `lastIP` = ?");
      ) {
         ps.setString(1, ip);

         try (ResultSet rs = ps.executeQuery()) {
            while(rs.next()) {
               ipAddress = rs.getString("lastIP");
            }
         }
      } catch (Exception var63) {
         var63.printStackTrace();
      }

      if (ipAddress != null) {
         bool = this.finishUnban(activeChar, ipAddress, PunishmentAffect.IP);
      }

      return bool;
   }

   protected boolean unbanCharByAcc(Player activeChar, String key) {
      boolean bool = false;
      String accName = null;

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement ps = con.prepareStatement("SELECT account_name FROM `characters` WHERE `account_name` = ?");
      ) {
         ps.setString(1, key);
         ResultSet rs = ps.executeQuery();

         while(rs.next()) {
            accName = rs.getString("account_name");
         }

         rs.close();
         ps.close();
      } catch (Exception var37) {
         var37.printStackTrace();
      }

      if (accName != null) {
         bool = this.finishUnban(activeChar, accName, PunishmentAffect.ACCOUNT);
      }

      return bool;
   }

   protected boolean unbanCharById(Player activeChar, String key) {
      boolean bool = false;

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement ps = con.prepareStatement("SELECT charId FROM characters WHERE char_name = ?");
      ) {
         ps.setString(1, key);

         ResultSet rset;
         String charId;
         for(rset = ps.executeQuery(); rset.next(); bool = this.finishUnban(activeChar, charId, PunishmentAffect.CHARACTER)) {
            charId = rset.getString("charId");
         }

         rset.close();
         ps.close();
      } catch (Exception var37) {
         var37.printStackTrace();
      }

      return bool;
   }

   protected boolean finishUnban(Player activeChar, String dbKey, PunishmentAffect param) {
      boolean sucsess = false;

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         Statement st = con.createStatement();
         ResultSet rset = st.executeQuery("SELECT * FROM punishments");
      ) {
         while(rset.next()) {
            String key = rset.getString("key");
            PunishmentAffect affect = PunishmentAffect.getByName(rset.getString("affect"));
            PunishmentType type = PunishmentType.getByName(rset.getString("type"));
            String reason = rset.getString("reason");
            if (type != null && affect != null && affect == param && key.equals(dbKey)) {
               rset.getInt("id");
               if (PunishmentManager.getInstance().clearPunishment(key, type, affect)) {
                  ServerMessage msg = null;
                  switch(affect) {
                     case CHARACTER:
                        msg = new ServerMessage("ServiceBBS.FIND_CHAR", activeChar.getLang());
                        msg.add(key);
                        msg.add(reason);
                        sucsess = true;
                        break;
                     case ACCOUNT:
                        msg = new ServerMessage("ServiceBBS.FIND_ACC", activeChar.getLang());
                        msg.add(key);
                        msg.add(reason);
                        sucsess = true;
                        break;
                     case IP:
                        msg = new ServerMessage("ServiceBBS.FIND_IP", activeChar.getLang());
                        msg.add(key);
                        msg.add(reason);
                        sucsess = true;
                  }

                  activeChar.sendMessage(msg.toString());
               }
            }
         }
      } catch (Exception var67) {
         var67.printStackTrace();
      }

      return sucsess;
   }

   protected boolean deleteSupport(Player activeChar, String banChar, PunishmentAffect affect, int id) {
      boolean sucsess = false;

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement st = con.prepareStatement("DELETE FROM punishments WHERE id=?");
      ) {
         st.setInt(1, id);
         st.execute();
         ServerMessage msg = null;
         switch(affect) {
            case CHARACTER:
               new ServerMessage("ServiceBBS.UNBAN_CHAR", activeChar.getLang());
            case ACCOUNT:
               new ServerMessage("ServiceBBS.UNBAN_ACC", activeChar.getLang());
            case IP:
               msg = new ServerMessage("ServiceBBS.UNBAN_IP", activeChar.getLang());
            default:
               msg.add(banChar);
               activeChar.sendMessage(msg.toString());
               sucsess = true;
         }
      } catch (SQLException var38) {
         var38.printStackTrace();
      }

      return sucsess;
   }

   protected void checkPremiumList(Player activeChar, int page) {
      NpcHtmlMessage html = new NpcHtmlMessage(5);
      html.setFile(activeChar, activeChar.getLang(), "data/html/community/donate/premiumList.htm");
      String template = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/community/donate/premium-template.htm");
      String block = "";
      String list = "";
      int perpage = 6;
      int counter = 0;
      boolean isThereNextPage = Config.SERVICES_PREMIUM_VALID_ID.length > 6;

      for(int i = (page - 1) * 6; i < Config.SERVICES_PREMIUM_VALID_ID.length; ++i) {
         PremiumTemplate tpl = PremiumAccountsParser.getInstance().getPremiumTemplate(Config.SERVICES_PREMIUM_VALID_ID[i]);
         if (tpl != null) {
            block = template.replace("%name%", activeChar.getLang().equalsIgnoreCase("en") ? tpl.getNameEn() : tpl.getNameRu());
            block = block.replace("%icon%", tpl.getIcon());
            block = block.replace("%time%", TimeUtils.formatTime(activeChar, (int)tpl.getTime()));
            String priceLine = "<font color=99CC66>Cost:</font> ";

            for(PremiumPrice price : tpl.getPriceList()) {
               if (price != null) {
                  priceLine = priceLine + "" + Util.formatPay(activeChar, price.getCount(), price.getId()) + " ";
               }
            }

            block = block.replace("%price%", priceLine);
            block = block.replace("%link%", "bypass -h _bbspremium " + tpl.getId() + " " + page + " " + 0);
            list = list + block;
            if (++counter >= 6) {
               break;
            }
         }
      }

      double pages = (double)Config.SERVICES_PREMIUM_VALID_ID.length / 6.0;
      int count = (int)Math.ceil(pages);
      html.replace("%list%", list);
      html.replace("%navigation%", Util.getNavigationBlock(count, page, Config.SERVICES_PREMIUM_VALID_ID.length, 6, isThereNextPage, "_bbspremiumList %s"));
      activeChar.sendPacket(html);
   }

   protected void checkFullPremiumList(Player activeChar, int page) {
      String html = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/community/donate/buyPremium.htm");
      String template = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/community/donate/buyPremium-template.htm");
      String block = "";
      String list = "";
      int perpage = 10;
      int counter = 0;
      boolean isThereNextPage = Config.SERVICES_PREMIUM_VALID_ID.length > 10;
      int countt = 0;

      for(int i = (page - 1) * 10; i < Config.SERVICES_PREMIUM_VALID_ID.length; ++i) {
         PremiumTemplate tpl = PremiumAccountsParser.getInstance().getPremiumTemplate(Config.SERVICES_PREMIUM_VALID_ID[i]);
         if (tpl != null) {
            block = template.replace("%name%", activeChar.getLang().equalsIgnoreCase("en") ? tpl.getNameEn() : tpl.getNameRu());
            block = block.replace("%icon%", tpl.getIcon());
            block = block.replace("%time%", TimeUtils.formatTime(activeChar, (int)tpl.getTime()));
            String priceLine = "<font color=99CC66>Cost:</font> ";

            for(PremiumPrice price : tpl.getPriceList()) {
               if (price != null) {
                  priceLine = priceLine + "" + Util.formatPay(activeChar, price.getCount(), price.getId()) + " ";
               }
            }

            block = block.replace("%price%", priceLine);
            block = block.replace("%link%", "bypass -h _bbspremium " + tpl.getId() + " " + page + " " + 1);
            if (++countt == 2) {
               block = block + "</tr><tr><td><br></td></tr><tr>";
               countt = 0;
            }

            list = list + block;
            if (++counter >= 10) {
               break;
            }
         }
      }

      double pages = (double)Config.SERVICES_PREMIUM_VALID_ID.length / 10.0;
      int count = (int)Math.ceil(pages);
      html = html.replace("%list%", list);
      html = html.replace(
         "%navigation%", Util.getNavigationBlock(count, page, Config.SERVICES_PREMIUM_VALID_ID.length, 10, isThereNextPage, "_bbspremiumPage %s")
      );
      separateAndSend(html, activeChar);
   }

   protected void checkFullPremiumListOnly(Player activeChar, int page) {
      String html = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/community/donate/buyPremium.htm");
      String template = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/community/donate/buyPremium-template.htm");
      String block = "";
      String list = "";
      int perpage = 10;
      int counter = 0;
      boolean isThereNextPage = Config.SERVICES_PREMIUM_VALID_ID.length > 10;
      int countt = 0;

      for(int i = (page - 1) * 10; i < Config.SERVICES_PREMIUM_VALID_ID.length; ++i) {
         PremiumTemplate tpl = PremiumAccountsParser.getInstance().getPremiumTemplate(Config.SERVICES_PREMIUM_VALID_ID[i]);
         if (tpl != null) {
            block = template.replace("%name%", activeChar.getLang().equalsIgnoreCase("en") ? tpl.getNameEn() : tpl.getNameRu());
            block = block.replace("%icon%", tpl.getIcon());
            block = block.replace("%time%", TimeUtils.formatTime(activeChar, (int)tpl.getTime()));
            String priceLine = "<font color=99CC66>Cost:</font> ";

            for(PremiumPrice price : tpl.getPriceList()) {
               if (price != null) {
                  priceLine = priceLine + "" + Util.formatPay(activeChar, price.getCount(), price.getId()) + " ";
               }
            }

            block = block.replace("%price%", priceLine);
            block = block.replace("%link%", "bypass -h _bbspremium " + tpl.getId() + " " + page + " " + 2);
            if (++countt == 2) {
               block = block + "</tr><tr><td><br></td></tr><tr>";
               countt = 0;
            }

            list = list + block;
            if (++counter >= 10) {
               break;
            }
         }
      }

      double pages = (double)Config.SERVICES_PREMIUM_VALID_ID.length / 10.0;
      int count = (int)Math.ceil(pages);
      html = html.replace("%list%", list);
      html = html.replace(
         "%navigation%", Util.getNavigationBlock(count, page, Config.SERVICES_PREMIUM_VALID_ID.length, 10, isThereNextPage, "_bbspremiumOnlyPage %s")
      );
      separateAndSend(html, activeChar);
   }

   protected void checkPremium(Player activeChar, int id, int page, int typeInfo) {
      PremiumTemplate template = PremiumAccountsParser.getInstance().getPremiumTemplate(id);
      if (template != null) {
         for(PremiumPrice price : template.getPriceList()) {
            if (price != null) {
               if (activeChar.getInventory().getItemByItemId(price.getId()) == null) {
                  activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                  if (typeInfo == 0) {
                     this.checkPremiumList(activeChar, page);
                  } else if (typeInfo == 1) {
                     this.onBypassCommand("_bbspremiumPage " + page + "", activeChar);
                  } else {
                     this.onBypassCommand("_bbspremiumOnlyPage " + page + "", activeChar);
                  }

                  return;
               }

               if (activeChar.getInventory().getItemByItemId(price.getId()).getCount() < price.getCount()) {
                  activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                  if (typeInfo == 0) {
                     this.checkPremiumList(activeChar, page);
                  } else if (typeInfo == 1) {
                     this.onBypassCommand("_bbspremiumPage " + page + "", activeChar);
                  } else {
                     this.onBypassCommand("_bbspremiumOnlyPage " + page + "", activeChar);
                  }

                  return;
               }
            }
         }

         for(PremiumPrice price : template.getPriceList()) {
            if (price != null) {
               activeChar.destroyItemByItemId("PremiumBBS", price.getId(), price.getCount(), activeChar, true);
            }
         }

         Util.addServiceLog(activeChar.getName() + " buy premium account template id: " + template.getId());
         long time = !template.isOnlineType() ? System.currentTimeMillis() + template.getTime() * 1000L : 0L;
         if (template.isPersonal()) {
            CharacterPremiumDAO.getInstance().updatePersonal(activeChar, id, time);
         } else {
            CharacterPremiumDAO.getInstance().update(activeChar, id, time);
         }

         if (activeChar.isInParty()) {
            activeChar.getParty().recalculatePartyData();
         }
      }

      if (typeInfo == 0) {
         this.checkPremiumList(activeChar, page);
      } else if (typeInfo == 1) {
         this.onBypassCommand("_bbspremiumPage " + page + "", activeChar);
      } else {
         this.onBypassCommand("_bbspremiumOnlyPage " + page + "", activeChar);
      }
   }

   private static String uptime() {
      SimpleDateFormat dataDateFormat = new SimpleDateFormat("hh:mm dd.MM.yyyy");
      return dataDateFormat.format(GameServer.server_started);
   }

   private String online(boolean off) {
      int i = 0;
      int j = 0;

      for(Player player : World.getInstance().getAllPlayers()) {
         ++i;
         if (player.isInOfflineMode() || player.getPrivateStoreType() != 0) {
            ++j;
         }
      }

      return Util.formatAdena((long)(!off ? (double)i * Config.FAKE_ONLINE : (double)j));
   }

   public static String time() {
      return TIME_FORMAT.format(new Date(System.currentTimeMillis()));
   }

   private String getTimeInServer(Player player) {
      int h = GameTimeController.getInstance().getGameHour();
      int m = GameTimeController.getInstance().getGameMinute();
      String strH;
      if (h < 10) {
         strH = "0" + h;
      } else {
         strH = "" + h;
      }

      String strM;
      if (m < 10) {
         strM = "0" + m;
      } else {
         strM = "" + m;
      }

      return strH + ":" + strM;
   }

   private String getStatus(Player player) {
      return player.hasPremiumBonus()
         ? "<font color=\"LEVEL\">" + ServerStorage.getInstance().getString(player.getLang(), "AccountBBSManager.PREMIUM") + "</font>"
         : "" + ServerStorage.getInstance().getString(player.getLang(), "AccountBBSManager.SIMPLE") + "";
   }

   private String getPremiumStatus(Player player) {
      String line = "";
      if (player.hasPremiumBonus()) {
         if (player.getPremiumBonus().isOnlineType()) {
            PremiumTemplate template = PremiumAccountsParser.getInstance().getPremiumTemplate(player.getPremiumBonus().getPremiumId());
            if (template != null) {
               long lastTime = (
                     template.getTime() * 1000L - (player.getPremiumBonus().getOnlineTime() + (System.currentTimeMillis() - player.getPremiumOnlineTime()))
                  )
                  / 1000L;
               line = "<font color=\"E6D0AE\">" + TimeUtils.formatTime(player, (int)lastTime, false) + "</font>";
            }
         } else {
            long lastTime = (player.getPremiumBonus().getOnlineTime() - System.currentTimeMillis()) / 1000L;
            line = "<font color=\"E6D0AE\">" + TimeUtils.formatTime(player, (int)lastTime, false) + "</font>";
         }
      } else {
         line = "<a action=\"bypass -h _bbspremiumPage\"><font color=\"ff6755\">"
            + ServerStorage.getInstance().getString(player.getLang(), "AccountBBSManager.SIMPLEE")
            + "</a></font>";
      }

      return line;
   }

   private static void sendFileToPlayer(Player player, String path, String... replacements) {
      String html = HtmCache.getInstance().getHtm(player, player.getLang(), path);

      for(int i = 0; i < replacements.length; i += 2) {
         String toReplace = replacements[i + 1];
         if (toReplace == null) {
            toReplace = "<br>";
         }

         html = html.replace(replacements[i], toReplace);
      }

      separateAndSend(html, player);
   }

   private static double cutOff(double num, int pow) {
      return (double)((int)(num * Math.pow(10.0, (double)pow))) / Math.pow(10.0, (double)pow);
   }

   public static final boolean correct(int level, boolean base) {
      return level <= (base ? ExperienceParser.getInstance().getMaxLevel() : Config.MAX_SUBCLASS_LEVEL);
   }

   @Override
   public void onWriteCommand(String command, String ar1, String ar2, String ar3, String ar4, String ar5, Player activeChar) {
   }

   public static CommunityGeneral getInstance() {
      return CommunityGeneral.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final CommunityGeneral _instance = new CommunityGeneral();
   }
}
