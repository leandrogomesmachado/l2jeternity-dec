package l2e.gameserver.handler.admincommandhandlers;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.handler.admincommandhandlers.impl.Admin;
import l2e.gameserver.handler.admincommandhandlers.impl.AerialCleft;
import l2e.gameserver.handler.admincommandhandlers.impl.Announcement;
import l2e.gameserver.handler.admincommandhandlers.impl.Balancer;
import l2e.gameserver.handler.admincommandhandlers.impl.BloodAltars;
import l2e.gameserver.handler.admincommandhandlers.impl.Buffs;
import l2e.gameserver.handler.admincommandhandlers.impl.CHSiege;
import l2e.gameserver.handler.admincommandhandlers.impl.Cache;
import l2e.gameserver.handler.admincommandhandlers.impl.Camera;
import l2e.gameserver.handler.admincommandhandlers.impl.ChangeAccessLevel;
import l2e.gameserver.handler.admincommandhandlers.impl.Clans;
import l2e.gameserver.handler.admincommandhandlers.impl.CreateItem;
import l2e.gameserver.handler.admincommandhandlers.impl.CursedWeapons;
import l2e.gameserver.handler.admincommandhandlers.impl.DailyTasks;
import l2e.gameserver.handler.admincommandhandlers.impl.Debug;
import l2e.gameserver.handler.admincommandhandlers.impl.Delete;
import l2e.gameserver.handler.admincommandhandlers.impl.Disconnect;
import l2e.gameserver.handler.admincommandhandlers.impl.DoorControl;
import l2e.gameserver.handler.admincommandhandlers.impl.EditChar;
import l2e.gameserver.handler.admincommandhandlers.impl.EditNpc;
import l2e.gameserver.handler.admincommandhandlers.impl.Effects;
import l2e.gameserver.handler.admincommandhandlers.impl.Element;
import l2e.gameserver.handler.admincommandhandlers.impl.Enchant;
import l2e.gameserver.handler.admincommandhandlers.impl.Events;
import l2e.gameserver.handler.admincommandhandlers.impl.ExpSp;
import l2e.gameserver.handler.admincommandhandlers.impl.Facebook;
import l2e.gameserver.handler.admincommandhandlers.impl.FakePlayers;
import l2e.gameserver.handler.admincommandhandlers.impl.FightCalculator;
import l2e.gameserver.handler.admincommandhandlers.impl.FortSiege;
import l2e.gameserver.handler.admincommandhandlers.impl.GeoEditor;
import l2e.gameserver.handler.admincommandhandlers.impl.Geodata;
import l2e.gameserver.handler.admincommandhandlers.impl.Gm;
import l2e.gameserver.handler.admincommandhandlers.impl.GmChat;
import l2e.gameserver.handler.admincommandhandlers.impl.GraciaSeeds;
import l2e.gameserver.handler.admincommandhandlers.impl.HWIDBans;
import l2e.gameserver.handler.admincommandhandlers.impl.Heal;
import l2e.gameserver.handler.admincommandhandlers.impl.Hellbound;
import l2e.gameserver.handler.admincommandhandlers.impl.HelpPage;
import l2e.gameserver.handler.admincommandhandlers.impl.InstanceZone;
import l2e.gameserver.handler.admincommandhandlers.impl.Instances;
import l2e.gameserver.handler.admincommandhandlers.impl.Invul;
import l2e.gameserver.handler.admincommandhandlers.impl.Kick;
import l2e.gameserver.handler.admincommandhandlers.impl.Kill;
import l2e.gameserver.handler.admincommandhandlers.impl.KrateisCube;
import l2e.gameserver.handler.admincommandhandlers.impl.Level;
import l2e.gameserver.handler.admincommandhandlers.impl.LogsViewer;
import l2e.gameserver.handler.admincommandhandlers.impl.Mammon;
import l2e.gameserver.handler.admincommandhandlers.impl.Manor;
import l2e.gameserver.handler.admincommandhandlers.impl.Menu;
import l2e.gameserver.handler.admincommandhandlers.impl.Messages;
import l2e.gameserver.handler.admincommandhandlers.impl.MobGroups;
import l2e.gameserver.handler.admincommandhandlers.impl.OlympiadMenu;
import l2e.gameserver.handler.admincommandhandlers.impl.OnlineReward;
import l2e.gameserver.handler.admincommandhandlers.impl.Packets;
import l2e.gameserver.handler.admincommandhandlers.impl.PcCondOverrides;
import l2e.gameserver.handler.admincommandhandlers.impl.Petition;
import l2e.gameserver.handler.admincommandhandlers.impl.Pledge;
import l2e.gameserver.handler.admincommandhandlers.impl.Polymorph;
import l2e.gameserver.handler.admincommandhandlers.impl.Premium;
import l2e.gameserver.handler.admincommandhandlers.impl.Punishment;
import l2e.gameserver.handler.admincommandhandlers.impl.Quests;
import l2e.gameserver.handler.admincommandhandlers.impl.RepairChar;
import l2e.gameserver.handler.admincommandhandlers.impl.Res;
import l2e.gameserver.handler.admincommandhandlers.impl.Ride;
import l2e.gameserver.handler.admincommandhandlers.impl.Shop;
import l2e.gameserver.handler.admincommandhandlers.impl.ShowQuests;
import l2e.gameserver.handler.admincommandhandlers.impl.ShutdownMenu;
import l2e.gameserver.handler.admincommandhandlers.impl.Siege;
import l2e.gameserver.handler.admincommandhandlers.impl.Skills;
import l2e.gameserver.handler.admincommandhandlers.impl.Spawn;
import l2e.gameserver.handler.admincommandhandlers.impl.Streaming;
import l2e.gameserver.handler.admincommandhandlers.impl.Summons;
import l2e.gameserver.handler.admincommandhandlers.impl.Targets;
import l2e.gameserver.handler.admincommandhandlers.impl.Teleports;
import l2e.gameserver.handler.admincommandhandlers.impl.TerritoryWar;
import l2e.gameserver.handler.admincommandhandlers.impl.Test;
import l2e.gameserver.handler.admincommandhandlers.impl.UnblockIp;
import l2e.gameserver.handler.admincommandhandlers.impl.Vitality;
import l2e.gameserver.handler.admincommandhandlers.impl.Zones;

public class AdminCommandHandler {
   private static Logger _log = Logger.getLogger(AdminCommandHandler.class.getName());
   private final Map<String, IAdminCommandHandler> _handlers = new HashMap<>();

   public static AdminCommandHandler getInstance() {
      return AdminCommandHandler.SingletonHolder._instance;
   }

   protected AdminCommandHandler() {
      this.registerHandler(new Admin());
      this.registerHandler(new AerialCleft());
      this.registerHandler(new Announcement());
      this.registerHandler(new Balancer());
      this.registerHandler(new BloodAltars());
      this.registerHandler(new Buffs());
      this.registerHandler(new Cache());
      this.registerHandler(new Camera());
      this.registerHandler(new ChangeAccessLevel());
      this.registerHandler(new CHSiege());
      this.registerHandler(new Clans());
      this.registerHandler(new CreateItem());
      this.registerHandler(new CursedWeapons());
      this.registerHandler(new Debug());
      this.registerHandler(new Delete());
      this.registerHandler(new Disconnect());
      this.registerHandler(new DoorControl());
      this.registerHandler(new EditChar());
      this.registerHandler(new EditNpc());
      this.registerHandler(new Effects());
      this.registerHandler(new Element());
      this.registerHandler(new Enchant());
      this.registerHandler(new Events());
      this.registerHandler(new ExpSp());
      if (Config.ALLOW_FAKE_PLAYERS) {
         this.registerHandler(new FakePlayers());
      }

      this.registerHandler(new FightCalculator());
      this.registerHandler(new FortSiege());
      this.registerHandler(new Hellbound());
      this.registerHandler(new Geodata());
      this.registerHandler(new GeoEditor());
      this.registerHandler(new Gm());
      this.registerHandler(new GmChat());
      this.registerHandler(new GraciaSeeds());
      this.registerHandler(new Heal());
      this.registerHandler(new HelpPage());
      this.registerHandler(new HWIDBans());
      this.registerHandler(new Instances());
      this.registerHandler(new InstanceZone());
      this.registerHandler(new Invul());
      this.registerHandler(new Kick());
      this.registerHandler(new Kill());
      this.registerHandler(new KrateisCube());
      this.registerHandler(new Level());
      this.registerHandler(new LogsViewer());
      this.registerHandler(new Mammon());
      this.registerHandler(new Manor());
      this.registerHandler(new Menu());
      this.registerHandler(new Messages());
      this.registerHandler(new MobGroups());
      this.registerHandler(new OlympiadMenu());
      this.registerHandler(new OnlineReward());
      this.registerHandler(new Packets());
      this.registerHandler(new PcCondOverrides());
      this.registerHandler(new Petition());
      this.registerHandler(new Pledge());
      this.registerHandler(new Polymorph());
      this.registerHandler(new Premium());
      this.registerHandler(new Punishment());
      this.registerHandler(new Quests());
      this.registerHandler(new RepairChar());
      this.registerHandler(new Res());
      this.registerHandler(new Ride());
      this.registerHandler(new Shop());
      this.registerHandler(new ShowQuests());
      this.registerHandler(new ShutdownMenu());
      this.registerHandler(new Siege());
      this.registerHandler(new Skills());
      this.registerHandler(new Spawn());
      this.registerHandler(new Summons());
      this.registerHandler(new Targets());
      this.registerHandler(new Teleports());
      this.registerHandler(new TerritoryWar());
      this.registerHandler(new Test());
      this.registerHandler(new UnblockIp());
      this.registerHandler(new Vitality());
      this.registerHandler(new Zones());
      if (Config.ALLOW_DAILY_TASKS) {
         this.registerHandler(new DailyTasks());
      }

      this.registerHandler(new Streaming());
      this.registerHandler(new Facebook());
      _log.info("Loaded " + this._handlers.size() + " AdminCommandHandlers");
   }

   public void registerHandler(IAdminCommandHandler handler) {
      String[] ids = handler.getAdminCommandList();

      for(String id : ids) {
         if (this._handlers.containsKey(id)) {
            _log.fine(
               "VoicedCommand: dublicate bypass registered! First handler: "
                  + this._handlers.get(id).getClass().getSimpleName()
                  + " second: "
                  + handler.getClass().getSimpleName()
            );
            this._handlers.remove(id);
         }

         this._handlers.put(id, handler);
      }
   }

   public IAdminCommandHandler getHandler(String adminCommand) {
      String command = adminCommand;
      if (adminCommand.indexOf(" ") != -1) {
         command = adminCommand.substring(0, adminCommand.indexOf(" "));
      }

      return this._handlers.get(command);
   }

   public int size() {
      return this._handlers.size();
   }

   private static class SingletonHolder {
      protected static final AdminCommandHandler _instance = new AdminCommandHandler();
   }
}
