package l2e.gameserver.handler.voicedcommandhandlers;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.handler.voicedcommandhandlers.impl.Achievement;
import l2e.gameserver.handler.voicedcommandhandlers.impl.Acp;
import l2e.gameserver.handler.voicedcommandhandlers.impl.AncientAdenaExchanger;
import l2e.gameserver.handler.voicedcommandhandlers.impl.AutoLoot;
import l2e.gameserver.handler.voicedcommandhandlers.impl.Banking;
import l2e.gameserver.handler.voicedcommandhandlers.impl.BlockBuffs;
import l2e.gameserver.handler.voicedcommandhandlers.impl.ChangePasswords;
import l2e.gameserver.handler.voicedcommandhandlers.impl.ChatAdmin;
import l2e.gameserver.handler.voicedcommandhandlers.impl.CheckPremium;
import l2e.gameserver.handler.voicedcommandhandlers.impl.CombineTalismans;
import l2e.gameserver.handler.voicedcommandhandlers.impl.Debug;
import l2e.gameserver.handler.voicedcommandhandlers.impl.DressMe;
import l2e.gameserver.handler.voicedcommandhandlers.impl.Enchant;
import l2e.gameserver.handler.voicedcommandhandlers.impl.ExpGain;
import l2e.gameserver.handler.voicedcommandhandlers.impl.FacebookPanel;
import l2e.gameserver.handler.voicedcommandhandlers.impl.FindParty;
import l2e.gameserver.handler.voicedcommandhandlers.impl.Hellbound;
import l2e.gameserver.handler.voicedcommandhandlers.impl.HideBuffsAnimation;
import l2e.gameserver.handler.voicedcommandhandlers.impl.HideTraders;
import l2e.gameserver.handler.voicedcommandhandlers.impl.Lang;
import l2e.gameserver.handler.voicedcommandhandlers.impl.Menu;
import l2e.gameserver.handler.voicedcommandhandlers.impl.Offline;
import l2e.gameserver.handler.voicedcommandhandlers.impl.Online;
import l2e.gameserver.handler.voicedcommandhandlers.impl.OpenAtod;
import l2e.gameserver.handler.voicedcommandhandlers.impl.Ping;
import l2e.gameserver.handler.voicedcommandhandlers.impl.PromoCode;
import l2e.gameserver.handler.voicedcommandhandlers.impl.Ranking;
import l2e.gameserver.handler.voicedcommandhandlers.impl.RecoveryItem;
import l2e.gameserver.handler.voicedcommandhandlers.impl.Relog;
import l2e.gameserver.handler.voicedcommandhandlers.impl.Repair;
import l2e.gameserver.handler.voicedcommandhandlers.impl.SchemeBuffs;
import l2e.gameserver.handler.voicedcommandhandlers.impl.Security;
import l2e.gameserver.handler.voicedcommandhandlers.impl.SellBuff;
import l2e.gameserver.handler.voicedcommandhandlers.impl.SevenRaidBosses;
import l2e.gameserver.handler.voicedcommandhandlers.impl.StreamVoice;
import l2e.gameserver.handler.voicedcommandhandlers.impl.TeleToLeader;
import l2e.gameserver.handler.voicedcommandhandlers.impl.VoteReward;
import l2e.gameserver.handler.voicedcommandhandlers.impl.Wedding;
import l2e.gameserver.handler.voicedcommandhandlers.impl.WhoAmI;

public class VoicedCommandHandler {
   private static Logger _log = Logger.getLogger(VoicedCommandHandler.class.getName());
   private final Map<String, IVoicedCommandHandler> _handlers = new HashMap<>();

   public static VoicedCommandHandler getInstance() {
      return VoicedCommandHandler.SingletonHolder._instance;
   }

   protected VoicedCommandHandler() {
      this.registerHandler(new Achievement());
      this.registerHandler(new Acp());
      this.registerHandler(new AncientAdenaExchanger());
      this.registerHandler(new AutoLoot());
      this.registerHandler(new Banking());
      this.registerHandler(new BlockBuffs());
      this.registerHandler(new ChangePasswords());
      this.registerHandler(new ChatAdmin());
      this.registerHandler(new CombineTalismans());
      this.registerHandler(new Menu());
      this.registerHandler(new Online());
      this.registerHandler(new OpenAtod());
      this.registerHandler(new Ping());
      this.registerHandler(new Repair());
      this.registerHandler(new Debug());
      this.registerHandler(new ExpGain());
      this.registerHandler(new Hellbound());
      this.registerHandler(new HideBuffsAnimation());
      this.registerHandler(new HideTraders());
      this.registerHandler(new Lang());
      this.registerHandler(new Ranking());
      this.registerHandler(new SevenRaidBosses());
      this.registerHandler(new TeleToLeader());
      this.registerHandler(new Wedding());
      this.registerHandler(new DressMe());
      DressMe.parseWeapon();
      this.registerHandler(new WhoAmI());
      this.registerHandler(new SellBuff());
      this.registerHandler(new VoteReward());
      this.registerHandler(new FindParty());
      this.registerHandler(new Security());
      this.registerHandler(new Enchant());
      this.registerHandler(new RecoveryItem());
      this.registerHandler(new CheckPremium());
      this.registerHandler(new Offline());
      this.registerHandler(new SchemeBuffs());
      this.registerHandler(new StreamVoice());
      this.registerHandler(new FacebookPanel());
      this.registerHandler(new Relog());
      this.registerHandler(new PromoCode());
      _log.info("Loaded " + this._handlers.size() + " VoicedHandlers");
   }

   public void registerHandler(IVoicedCommandHandler handler) {
      String[] ids = handler.getVoicedCommandList();

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

   public synchronized void removeHandler(IVoicedCommandHandler handler) {
      String[] ids = handler.getVoicedCommandList();

      for(String id : ids) {
         this._handlers.remove(id);
      }
   }

   public IVoicedCommandHandler getHandler(String voicedCommand) {
      String command = voicedCommand;
      if (voicedCommand.indexOf(" ") != -1) {
         command = voicedCommand.substring(0, voicedCommand.indexOf(" "));
      }

      if (Config.DEBUG) {
         _log.fine("getting handler for command: " + command + " -> " + (this._handlers.get(command.hashCode()) != null));
      }

      return this._handlers.get(command);
   }

   public int size() {
      return this._handlers.size();
   }

   private static class SingletonHolder {
      protected static final VoicedCommandHandler _instance = new VoicedCommandHandler();
   }
}
