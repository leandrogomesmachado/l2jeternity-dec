package l2e.gameserver.handler.bypasshandlers;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.handler.bypasshandlers.impl.AgressionInfo;
import l2e.gameserver.handler.bypasshandlers.impl.Augment;
import l2e.gameserver.handler.bypasshandlers.impl.Buy;
import l2e.gameserver.handler.bypasshandlers.impl.BuyShadowItem;
import l2e.gameserver.handler.bypasshandlers.impl.ChatLink;
import l2e.gameserver.handler.bypasshandlers.impl.ClanWarehouse;
import l2e.gameserver.handler.bypasshandlers.impl.DropInfo;
import l2e.gameserver.handler.bypasshandlers.impl.EffectInfo;
import l2e.gameserver.handler.bypasshandlers.impl.ElcardiaBuff;
import l2e.gameserver.handler.bypasshandlers.impl.Festival;
import l2e.gameserver.handler.bypasshandlers.impl.FortSiege;
import l2e.gameserver.handler.bypasshandlers.impl.Freight;
import l2e.gameserver.handler.bypasshandlers.impl.Hennas;
import l2e.gameserver.handler.bypasshandlers.impl.ItemAuctionLink;
import l2e.gameserver.handler.bypasshandlers.impl.Link;
import l2e.gameserver.handler.bypasshandlers.impl.Loto;
import l2e.gameserver.handler.bypasshandlers.impl.ManorManager;
import l2e.gameserver.handler.bypasshandlers.impl.Multisell;
import l2e.gameserver.handler.bypasshandlers.impl.Observation;
import l2e.gameserver.handler.bypasshandlers.impl.OlympiadManagerLink;
import l2e.gameserver.handler.bypasshandlers.impl.OlympiadObservation;
import l2e.gameserver.handler.bypasshandlers.impl.PlayerHelp;
import l2e.gameserver.handler.bypasshandlers.impl.PrivateWarehouse;
import l2e.gameserver.handler.bypasshandlers.impl.QuestLink;
import l2e.gameserver.handler.bypasshandlers.impl.QuestList;
import l2e.gameserver.handler.bypasshandlers.impl.ReleaseAttribute;
import l2e.gameserver.handler.bypasshandlers.impl.RentPet;
import l2e.gameserver.handler.bypasshandlers.impl.Rift;
import l2e.gameserver.handler.bypasshandlers.impl.SkillList;
import l2e.gameserver.handler.bypasshandlers.impl.SupportBlessing;
import l2e.gameserver.handler.bypasshandlers.impl.SupportMagic;
import l2e.gameserver.handler.bypasshandlers.impl.TerritoryStatus;
import l2e.gameserver.handler.bypasshandlers.impl.VoiceCommand;
import l2e.gameserver.handler.bypasshandlers.impl.Wear;
import l2e.gameserver.handler.voicedcommandhandlers.impl.SellBuff;

public class BypassHandler {
   private static Logger _log = Logger.getLogger(BypassHandler.class.getName());
   private final Map<String, IBypassHandler> _handlers = new HashMap<>();

   public static BypassHandler getInstance() {
      return BypassHandler.SingletonHolder._instance;
   }

   protected BypassHandler() {
      this.registerHandler(new AgressionInfo());
      this.registerHandler(new Augment());
      this.registerHandler(new Buy());
      this.registerHandler(new BuyShadowItem());
      this.registerHandler(new ChatLink());
      this.registerHandler(new ClanWarehouse());
      this.registerHandler(new DropInfo());
      this.registerHandler(new EffectInfo());
      this.registerHandler(new ElcardiaBuff());
      this.registerHandler(new Festival());
      this.registerHandler(new FortSiege());
      this.registerHandler(new Freight());
      this.registerHandler(new Hennas());
      this.registerHandler(new ItemAuctionLink());
      this.registerHandler(new Link());
      this.registerHandler(new Loto());
      this.registerHandler(new ManorManager());
      this.registerHandler(new Multisell());
      this.registerHandler(new Observation());
      this.registerHandler(new OlympiadManagerLink());
      this.registerHandler(new OlympiadObservation());
      this.registerHandler(new PlayerHelp());
      this.registerHandler(new PrivateWarehouse());
      this.registerHandler(new QuestLink());
      this.registerHandler(new QuestList());
      this.registerHandler(new ReleaseAttribute());
      this.registerHandler(new RentPet());
      this.registerHandler(new Rift());
      this.registerHandler(new SkillList());
      this.registerHandler(new SupportBlessing());
      this.registerHandler(new SupportMagic());
      this.registerHandler(new TerritoryStatus());
      this.registerHandler(new VoiceCommand());
      this.registerHandler(new Wear());
      this.registerHandler(new SellBuff());
      _log.info("Loaded " + this._handlers.size() + " BypassHandlers");
   }

   public void registerHandler(IBypassHandler handler) {
      for(String element : handler.getBypassList()) {
         if (this._handlers.containsKey(element)) {
            _log.fine(
               "BypassHandler: dublicate bypass registered! First handler: "
                  + this._handlers.get(element).getClass().getSimpleName()
                  + " second: "
                  + handler.getClass().getSimpleName()
            );
            this._handlers.remove(element);
         }

         this._handlers.put(element.toLowerCase(), handler);
      }
   }

   public synchronized void removeHandler(IBypassHandler handler) {
      for(String element : handler.getBypassList()) {
         this._handlers.remove(element.toLowerCase());
      }
   }

   public IBypassHandler getHandler(String BypassCommand) {
      String command = BypassCommand;
      if (BypassCommand.indexOf(" ") != -1) {
         command = BypassCommand.substring(0, BypassCommand.indexOf(" "));
      }

      if (Config.DEBUG) {
         _log.log(Level.FINE, "getting handler for command: " + command + " -> " + (this._handlers.get(command.hashCode()) != null));
      }

      return this._handlers.get(command.toLowerCase());
   }

   public int size() {
      return this._handlers.size();
   }

   private static class SingletonHolder {
      protected static final BypassHandler _instance = new BypassHandler();
   }
}
