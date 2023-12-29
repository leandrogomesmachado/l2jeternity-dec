package l2e.gameserver;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import l2e.commons.lang.StatsUtils;
import l2e.commons.listener.Listener;
import l2e.commons.listener.ListenerList;
import l2e.commons.net.IPSettings;
import l2e.fake.FakePlayerManager;
import l2e.gameserver.data.holder.CharMiniGameHolder;
import l2e.gameserver.data.holder.CharNameHolder;
import l2e.gameserver.data.holder.CharSummonHolder;
import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.data.holder.CrestHolder;
import l2e.gameserver.data.holder.FacebookProfilesHolder;
import l2e.gameserver.data.holder.NpcBufferHolder;
import l2e.gameserver.data.holder.OfficialPostsHolder;
import l2e.gameserver.data.holder.SpawnHolder;
import l2e.gameserver.data.holder.SummonSkillsHolder;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.data.htm.ImagesCache;
import l2e.gameserver.data.parser.AdminParser;
import l2e.gameserver.data.parser.ArmorSetsParser;
import l2e.gameserver.data.parser.AugmentationParser;
import l2e.gameserver.data.parser.BotReportParser;
import l2e.gameserver.data.parser.BuyListParser;
import l2e.gameserver.data.parser.CategoryParser;
import l2e.gameserver.data.parser.CharTemplateParser;
import l2e.gameserver.data.parser.ClassBalanceParser;
import l2e.gameserver.data.parser.ClassListParser;
import l2e.gameserver.data.parser.ClassMasterParser;
import l2e.gameserver.data.parser.ColosseumFenceParser;
import l2e.gameserver.data.parser.CommunityTeleportsParser;
import l2e.gameserver.data.parser.DamageLimitParser;
import l2e.gameserver.data.parser.DonationParser;
import l2e.gameserver.data.parser.DoorParser;
import l2e.gameserver.data.parser.DressArmorParser;
import l2e.gameserver.data.parser.DressCloakParser;
import l2e.gameserver.data.parser.DressHatParser;
import l2e.gameserver.data.parser.DressShieldParser;
import l2e.gameserver.data.parser.DressWeaponParser;
import l2e.gameserver.data.parser.EnchantItemGroupsParser;
import l2e.gameserver.data.parser.EnchantItemHPBonusParser;
import l2e.gameserver.data.parser.EnchantItemOptionsParser;
import l2e.gameserver.data.parser.EnchantItemParser;
import l2e.gameserver.data.parser.EnchantSkillGroupsParser;
import l2e.gameserver.data.parser.ExchangeItemParser;
import l2e.gameserver.data.parser.ExpPercentLostParser;
import l2e.gameserver.data.parser.ExperienceParser;
import l2e.gameserver.data.parser.FacebookCommentsParser;
import l2e.gameserver.data.parser.FightEventMapParser;
import l2e.gameserver.data.parser.FightEventParser;
import l2e.gameserver.data.parser.FishMonstersParser;
import l2e.gameserver.data.parser.FishParser;
import l2e.gameserver.data.parser.FoundationParser;
import l2e.gameserver.data.parser.HennaParser;
import l2e.gameserver.data.parser.HitConditionBonusParser;
import l2e.gameserver.data.parser.InitialEquipmentParser;
import l2e.gameserver.data.parser.InitialShortcutParser;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.data.parser.ManorParser;
import l2e.gameserver.data.parser.MerchantPriceParser;
import l2e.gameserver.data.parser.MultiSellParser;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.data.parser.OptionsParser;
import l2e.gameserver.data.parser.PetitionGroupParser;
import l2e.gameserver.data.parser.PetsParser;
import l2e.gameserver.data.parser.PremiumAccountsParser;
import l2e.gameserver.data.parser.ProductItemParser;
import l2e.gameserver.data.parser.PromoCodeParser;
import l2e.gameserver.data.parser.QuestsParser;
import l2e.gameserver.data.parser.RecipeParser;
import l2e.gameserver.data.parser.ReflectionParser;
import l2e.gameserver.data.parser.SchemesParser;
import l2e.gameserver.data.parser.SellBuffsParser;
import l2e.gameserver.data.parser.SkillBalanceParser;
import l2e.gameserver.data.parser.SkillTreesParser;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.data.parser.SoulCrystalParser;
import l2e.gameserver.data.parser.SpawnParser;
import l2e.gameserver.data.parser.StaticObjectsParser;
import l2e.gameserver.data.parser.TeleLocationParser;
import l2e.gameserver.data.parser.TransformParser;
import l2e.gameserver.data.parser.UIParser;
import l2e.gameserver.data.parser.VoteRewardParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.geodata.editor.GeoEditorListener;
import l2e.gameserver.handler.actionhandlers.ActionHandler;
import l2e.gameserver.handler.actionshifthandlers.ActionShiftHandler;
import l2e.gameserver.handler.admincommandhandlers.AdminCommandHandler;
import l2e.gameserver.handler.bypasshandlers.BypassHandler;
import l2e.gameserver.handler.chathandlers.ChatHandler;
import l2e.gameserver.handler.communityhandlers.CommunityBoardHandler;
import l2e.gameserver.handler.effecthandlers.EffectHandler;
import l2e.gameserver.handler.itemhandlers.ItemHandler;
import l2e.gameserver.handler.skillhandlers.SkillHandler;
import l2e.gameserver.handler.targethandlers.TargetHandler;
import l2e.gameserver.handler.usercommandhandlers.UserCommandHandler;
import l2e.gameserver.handler.voicedcommandhandlers.VoicedCommandHandler;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.instancemanager.AirShipManager;
import l2e.gameserver.instancemanager.AuctionManager;
import l2e.gameserver.instancemanager.AutoFarmManager;
import l2e.gameserver.instancemanager.BloodAltarManager;
import l2e.gameserver.instancemanager.BoatManager;
import l2e.gameserver.instancemanager.BotCheckManager;
import l2e.gameserver.instancemanager.CHSiegeManager;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.CastleManorManager;
import l2e.gameserver.instancemanager.ChampionManager;
import l2e.gameserver.instancemanager.ClanHallManager;
import l2e.gameserver.instancemanager.CoupleManager;
import l2e.gameserver.instancemanager.CursedWeaponsManager;
import l2e.gameserver.instancemanager.DailyRewardManager;
import l2e.gameserver.instancemanager.DailyTaskManager;
import l2e.gameserver.instancemanager.DayNightSpawnManager;
import l2e.gameserver.instancemanager.DimensionalRiftManager;
import l2e.gameserver.instancemanager.DoubleSessionManager;
import l2e.gameserver.instancemanager.DropManager;
import l2e.gameserver.instancemanager.EpicBossManager;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.instancemanager.FortSiegeManager;
import l2e.gameserver.instancemanager.FourSepulchersManager;
import l2e.gameserver.instancemanager.GlobalVariablesManager;
import l2e.gameserver.instancemanager.HellboundManager;
import l2e.gameserver.instancemanager.ItemAuctionManager;
import l2e.gameserver.instancemanager.ItemRecoveryManager;
import l2e.gameserver.instancemanager.ItemsOnGroundManager;
import l2e.gameserver.instancemanager.KrateisCubeManager;
import l2e.gameserver.instancemanager.LakfiManager;
import l2e.gameserver.instancemanager.MailManager;
import l2e.gameserver.instancemanager.MapRegionManager;
import l2e.gameserver.instancemanager.MercTicketManager;
import l2e.gameserver.instancemanager.OnlineRewardManager;
import l2e.gameserver.instancemanager.PetitionManager;
import l2e.gameserver.instancemanager.PunishmentManager;
import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.instancemanager.RaidBossSpawnManager;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.instancemanager.RewardManager;
import l2e.gameserver.instancemanager.ServerVariables;
import l2e.gameserver.instancemanager.SiegeManager;
import l2e.gameserver.instancemanager.SoDManager;
import l2e.gameserver.instancemanager.SoIManager;
import l2e.gameserver.instancemanager.SpecialBypassManager;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.instancemanager.UndergroundColiseumManager;
import l2e.gameserver.instancemanager.VipManager;
import l2e.gameserver.instancemanager.WalkingManager;
import l2e.gameserver.instancemanager.WeeklyTraderManager;
import l2e.gameserver.instancemanager.WorldEventManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.instancemanager.games.FishingChampionship;
import l2e.gameserver.instancemanager.games.MonsterRaceManager;
import l2e.gameserver.listener.ScriptListenerLoader;
import l2e.gameserver.listener.game.OnShutdownListener;
import l2e.gameserver.listener.game.OnStartListener;
import l2e.gameserver.model.AutoSpawnHandler;
import l2e.gameserver.model.World;
import l2e.gameserver.model.entity.Hero;
import l2e.gameserver.model.entity.events.EventsDropManager;
import l2e.gameserver.model.entity.events.cleft.AerialCleftEvent;
import l2e.gameserver.model.entity.events.custom.Leprechaun;
import l2e.gameserver.model.entity.events.custom.achievements.AchievementManager;
import l2e.gameserver.model.entity.events.model.FightEventManager;
import l2e.gameserver.model.entity.events.model.FightEventNpcManager;
import l2e.gameserver.model.entity.events.model.FightLastStatsManager;
import l2e.gameserver.model.entity.mods.facebook.ActionsExtractingManager;
import l2e.gameserver.model.entity.mods.facebook.FacebookAutoAnnouncement;
import l2e.gameserver.model.olympiad.Olympiad;
import l2e.gameserver.model.service.autofarm.FarmSettings;
import l2e.gameserver.model.strings.server.ServerStorage;
import l2e.gameserver.network.GameClient;
import l2e.gameserver.network.GamePacketHandler;
import l2e.gameserver.network.communication.AuthServerCommunication;
import l2e.gameserver.taskmanager.AutoAnnounceTaskManager;
import l2e.gameserver.taskmanager.RestoreOfflineTraders;
import l2e.gameserver.taskmanager.StreamTaskManager;
import l2e.gameserver.taskmanager.TaskManager;
import l2e.gameserver.taskmanager.TwitchTaskManager;
import org.HostInfo;
import org.nio.impl.SelectorStats;
import org.nio.impl.SelectorThread;

public class GameServer {
   private static final Logger _log = Logger.getLogger(GameServer.class.getName());
   public static final int AUTH_SERVER_PROTOCOL = 4;
   private final List<SelectorThread<GameClient>> _selectorThreads = new ArrayList<>();
   private final SelectorStats _selectorStats = new SelectorStats();
   private final GameServer.GameServerListenerList _listeners;
   private final IdFactory _idFactory;
   public static GameServer _instance;
   public static final Calendar dateTimeServerStarted = Calendar.getInstance();
   public static Date server_started;

   public long getUsedMemoryMB() {
      return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576L;
   }

   public List<SelectorThread<GameClient>> getSelectorThreads() {
      return this._selectorThreads;
   }

   public SelectorStats getSelectorStats() {
      return this._selectorStats;
   }

   public GameServer() throws Exception {
      _instance = this;
      this._listeners = new GameServer.GameServerListenerList();
      _log.finest("used mem:" + this.getUsedMemoryMB() + "MB");
      this._idFactory = IdFactory.getInstance();
      if (!this._idFactory.isInitialized()) {
         _log.severe("Could not read object IDs from DB. Please Check Your Data.");
         throw new Exception("Could not initialize the ID factory");
      } else {
         ThreadPoolManager.getInstance();
         new File(Config.DATAPACK_ROOT, "data/crests").mkdirs();
         new File("log/game").mkdirs();
         new File("log/twitch").mkdirs();
         HostInfo[] var1 = IPSettings.getInstance().getGameServerHosts();
         if (var1.length == 0) {
            throw new Exception("Server hosts list is empty!");
         } else {
            TIntHashSet var2 = new TIntHashSet();

            for(HostInfo var6 : var1) {
               if (var6.getAddress() != null) {
                  while(!checkFreePort(var6.getAddress(), var6.getPort())) {
                     _log.warning("Port '" + var6.getPort() + "' on host '" + var6.getAddress() + "' is allready binded. Please free it and restart server.");

                     try {
                        Thread.sleep(1000L);
                     } catch (InterruptedException var8) {
                     }
                  }

                  var2.add(var6.getPort());
               }
            }

            int[] var9 = var2.toArray();
            if (var9.length == 0) {
               throw new Exception("Server ports list is empty!");
            } else {
               printSection("Engines");
               ScriptListenerLoader.getInstance();
               ServerStorage.getInstance();
               printSection("World");
               GameTimeController.init();
               ReflectionManager.getInstance();
               World.getInstance();
               MapRegionManager.getInstance();
               Announcements.getInstance();
               EventsDropManager.getInstance();
               GlobalVariablesManager.getInstance();
               CategoryParser.getInstance();
               printSection("Skills");
               EffectHandler.getInstance().executeScript();
               EnchantSkillGroupsParser.getInstance();
               SkillTreesParser.getInstance();
               SkillsParser.getInstance();
               SummonSkillsHolder.getInstance();
               SchemesParser.getInstance();
               if (Config.ALLOW_SELLBUFFS_COMMAND) {
                  SellBuffsParser.getInstance();
               }

               printSection("Items");
               ItemsParser.getInstance();
               ProductItemParser.getInstance();
               DonationParser.getInstance();
               ExchangeItemParser.getInstance();
               FoundationParser.getInstance();
               if (Config.ALLOW_VISUAL_ARMOR_COMMAND) {
                  DressArmorParser.getInstance();
                  DressCloakParser.getInstance();
                  DressShieldParser.getInstance();
                  DressHatParser.getInstance();
                  DressWeaponParser.getInstance();
               }

               SoulCrystalParser.getInstance();
               EnchantItemGroupsParser.getInstance();
               EnchantItemParser.getInstance();
               EnchantItemOptionsParser.getInstance();
               OptionsParser.getInstance();
               EnchantItemHPBonusParser.getInstance();
               MerchantPriceParser.getInstance().loadInstances();
               BuyListParser.getInstance();
               MultiSellParser.getInstance();
               RecipeParser.getInstance();
               ArmorSetsParser.getInstance();
               FishMonstersParser.getInstance();
               FishParser.getInstance();
               FishingChampionship.getInstance();
               HennaParser.getInstance();
               CursedWeaponsManager.getInstance();
               printSection("Characters");
               ClassListParser.getInstance();
               ClassMasterParser.getInstance();
               InitialEquipmentParser.getInstance();
               InitialShortcutParser.getInstance();
               ExperienceParser.getInstance();
               ExpPercentLostParser.getInstance();
               HitConditionBonusParser.getInstance();
               CharTemplateParser.getInstance();
               CharNameHolder.getInstance();
               PremiumAccountsParser.getInstance();
               DailyTaskManager.getInstance();
               AdminParser.getInstance();
               PetsParser.getInstance();
               CharSummonHolder.getInstance().init();
               if (Config.NEW_PETITIONING_SYSTEM) {
                  PetitionGroupParser.getInstance();
               }

               PetitionManager.getInstance();
               if (Config.ENABLE_ANTI_BOT_SYSTEM) {
                  BotCheckManager.getInstance();
               }

               UIParser.getInstance();
               PromoCodeParser.getInstance();
               printSection("Clans");
               ClanHolder.getInstance();
               CHSiegeManager.getInstance();
               ClanHallManager.getInstance();
               AuctionManager.getInstance();
               printSection("Geodata");
               GeoEngine.load();
               if (Config.ACCEPT_GEOEDITOR_CONN) {
                  GeoEditorListener.getInstance();
               }

               printSection("NPCs");
               NpcsParser.getInstance();
               DropManager.getInstance();
               WalkingManager.getInstance();
               StaticObjectsParser.getInstance();
               ZoneManager.getInstance();
               DoorParser.getInstance();
               ColosseumFenceParser.getInstance();
               ItemAuctionManager.getInstance();
               CastleManager.getInstance().loadInstances();
               FortManager.getInstance().loadInstances();
               NpcBufferHolder.getInstance();
               ChampionManager.getInstance();
               BloodAltarManager.getInstance();
               RaidBossSpawnManager.getInstance();
               SpawnHolder.getInstance();
               SpawnParser.getInstance();
               DamageLimitParser.getInstance();
               HellboundManager.getInstance();
               ReflectionParser.getInstance();
               ZoneManager.getInstance().createZoneReflections();
               DayNightSpawnManager.getInstance().trim().notifyChangeMode();
               EpicBossManager.getInstance().initZones();
               FourSepulchersManager.getInstance().init();
               DimensionalRiftManager.getInstance();
               BotReportParser.getInstance();
               TeleLocationParser.getInstance();
               CommunityTeleportsParser.getInstance();
               AugmentationParser.getInstance();
               TransformParser.getInstance();
               WorldEventManager.getInstance();
               printSection("Seven Signs");
               SevenSigns.getInstance();
               SevenSigns.getInstance().spawnSevenSignsNPC();
               SevenSignsFestival.getInstance();
               printSection("Siege");
               SiegeManager.getInstance().getSieges();
               FortSiegeManager.getInstance();
               TerritoryWarManager.getInstance();
               CastleManorManager.getInstance();
               MercTicketManager.getInstance();
               ManorParser.getInstance();
               printSection("Olympiad");
               Olympiad.getInstance();
               Hero.getInstance();
               printSection("Cache");
               HtmCache.getInstance();
               CrestHolder.getInstance();
               ImagesCache.getInstance();
               printSection("Handlers");
               if (!Config.ALT_DEV_NO_HANDLERS) {
                  AutoSpawnHandler.getInstance();
                  ActionHandler.getInstance();
                  ActionShiftHandler.getInstance();
                  AdminCommandHandler.getInstance();
                  BypassHandler.getInstance();
                  ChatHandler.getInstance();
                  CommunityBoardHandler.getInstance();
                  ItemHandler.getInstance();
                  SkillHandler.getInstance();
                  TargetHandler.getInstance();
                  UserCommandHandler.getInstance();
                  VoicedCommandHandler.getInstance();
               }

               if (Config.BALANCER_ALLOW) {
                  printSection("Balancer");
                  ClassBalanceParser.getInstance();
                  SkillBalanceParser.getInstance();
               }

               printSection("Gracia");
               SoDManager.getInstance();
               SoIManager.getInstance();
               AerialCleftEvent.getInstance();
               printSection("Vehicles");
               BoatManager.getInstance();
               AirShipManager.getInstance();
               printSection("Game Processes");
               UndergroundColiseumManager.getInstance();
               KrateisCubeManager.getInstance().init();
               MonsterRaceManager.getInstance();
               CharMiniGameHolder.getInstance().select();
               printSection("Scripts");
               QuestManager.getInstance();
               QuestsParser.getInstance();
               CastleManager.getInstance().activateInstances();
               FortManager.getInstance().activateInstances();
               MerchantPriceParser.getInstance().updateReferences();
               ScriptListenerLoader.getInstance().executeScriptList();
               if (Config.LAKFI_ENABLED) {
                  LakfiManager.getInstance();
               }

               if (Config.SAVE_DROPPED_ITEM) {
                  ItemsOnGroundManager.getInstance();
               }

               if (Config.AUTODESTROY_ITEM_AFTER > 0 || Config.HERB_AUTO_DESTROY_TIME > 0) {
                  ItemsAutoDestroy.getInstance();
               }

               DoubleSessionManager.getInstance().registerEvent(0);
               Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());
               _log.fine("IdFactory: Free ObjectID's remaining: " + IdFactory.getInstance().size());
               printSection("Protection System");
               PunishmentManager.getInstance();
               printSection("Eternity Mods");
               if (Config.ALLOW_VIP_SYSTEM) {
                  VipManager.getInstance();
               }

               RewardManager.getInstance();
               AchievementManager.getInstance();
               DailyRewardManager.getInstance();
               OnlineRewardManager.getInstance();
               if (Config.ALLOW_STREAM_SYSTEM && Config.TWITCH_CHECK_DELAY > 0) {
                  TwitchTaskManager.getInstance();
               }

               if (Config.ALLOW_FACEBOOK_SYSTEM) {
                  FacebookProfilesHolder.getInstance();
                  FacebookCommentsParser.getInstance();
                  OfficialPostsHolder.getInstance();
                  ActionsExtractingManager.getInstance().load();
                  FacebookAutoAnnouncement.load();
               }

               if (Config.ALLOW_STREAM_AFK_SYSTEM && Config.STREAM_AFK_DELAY > 0) {
                  StreamTaskManager.getInstance();
               }

               if (Config.ALLOW_WEDDING) {
                  CoupleManager.getInstance();
               }

               WeeklyTraderManager.getInstance();
               printSection("Events");
               if (Config.ALLOW_FIGHT_EVENTS) {
                  FightEventMapParser.getInstance();
                  FightEventParser.getInstance();
                  FightLastStatsManager.getInstance().restoreStats();
                  FightEventManager.getInstance();
                  FightEventNpcManager.getInstance();
               } else {
                  _log.info("FightEventManager: All fight events disabled.");
               }

               if (Config.ENABLED_LEPRECHAUN) {
                  Leprechaun.getInstance();
               }

               printSection("Other");
               SpecialBypassManager.getInstance();
               VoteRewardParser.getInstance();
               if (Config.ALLOW_FAKE_PLAYERS) {
                  _log.info("FakePlayerManager: Loading fake players system...");
                  FakePlayerManager.getInstance();
               }

               TaskManager.getInstance();
               if (Config.ALLOW_MAIL) {
                  MailManager.getInstance();
               }

               ServerVariables.getVars();
               AutoRestart.getInstance();
               if (Config.ALLOW_RECOVERY_ITEMS) {
                  ItemRecoveryManager.getInstance();
               }

               if (Config.ONLINE_PLAYERS_ANNOUNCE_INTERVAL > 0) {
                  OnlinePlayers.getInstance();
               }

               if (FarmSettings.ALLOW_AUTO_FARM) {
                  AutoFarmManager.getInstance();
               }

               if ((Config.OFFLINE_TRADE_ENABLE || Config.OFFLINE_CRAFT_ENABLE) && Config.RESTORE_OFFLINERS) {
                  ThreadPoolManager.getInstance().schedule(new RestoreOfflineTraders(), 10000L);
               }

               Toolkit.getDefaultToolkit().beep();
               AutoAnnounceTaskManager.getInstance();
               _log.info("-------------------------------------------------------------------------------");
               StatsUtils.getMemUsage(_log);
               EternityWorld.getTeamInfo(_log);
               _log.info("-------------------------------------------------------------------------------");
               this.registerSelectorThreads(var2);
               this.getListeners().onStart();
               AuthServerCommunication.getInstance().start();
               server_started = new Date();
            }
         }
      }
   }

   public static void main(String[] var0) throws Exception {
      String var1 = "log";
      String var2 = "./config/log.ini";
      File var3 = new File(Config.DATAPACK_ROOT, "log");
      var3.mkdir();

      try (FileInputStream var4 = new FileInputStream(new File("./config/log.ini"))) {
         LogManager.getLogManager().readConfiguration(var4);
      }

      Config.load();
      printSection("Database");
      DatabaseFactory.getInstance();
      new GameServer();
   }

   private static boolean checkFreePort(String var0, int var1) {
      ServerSocket var2 = null;

      boolean var4;
      try {
         if (var0.equalsIgnoreCase("*")) {
            var2 = new ServerSocket(var1);
         } else {
            var2 = new ServerSocket(var1, 50, InetAddress.getByName(var0));
         }

         return true;
      } catch (Exception var14) {
         var4 = false;
      } finally {
         try {
            var2.close();
         } catch (Exception var13) {
         }
      }

      return var4;
   }

   private void registerSelectorThreads(TIntSet var1) {
      GamePacketHandler var2 = new GamePacketHandler();

      for(int var6 : var1.toArray()) {
         this.registerSelectorThread(var2, null, var6);
      }
   }

   private void registerSelectorThread(GamePacketHandler var1, String var2, int var3) {
      try {
         SelectorThread var4 = new SelectorThread<>(Config.SELECTOR_CONFIG, this._selectorStats, var1, var1, var1, null);
         var4.openServerSocket(var2 == null ? null : InetAddress.getByName(var2), var3);
         var4.start();
         this._selectorThreads.add(var4);
      } catch (Exception var5) {
      }
   }

   public static void printSection(String var0) {
      var0 = "=[ " + var0 + " ]";

      while(var0.length() < 78) {
         var0 = "-" + var0;
      }

      _log.info(var0);
   }

   public GameServer.GameServerListenerList getListeners() {
      return this._listeners;
   }

   public static GameServer getInstance() {
      return _instance;
   }

   public int getOnlineLimit() {
      return Config.MAXIMUM_ONLINE_USERS;
   }

   public class GameServerListenerList extends ListenerList<GameServer> {
      public void onStart() {
         for(Listener var2 : this.getListeners()) {
            if (OnStartListener.class.isInstance(var2)) {
               ((OnStartListener)var2).onStart();
            }
         }
      }

      public void onShutdown() {
         for(Listener var2 : this.getListeners()) {
            if (OnShutdownListener.class.isInstance(var2)) {
               ((OnShutdownListener)var2).onShutdown();
            }
         }
      }
   }
}
