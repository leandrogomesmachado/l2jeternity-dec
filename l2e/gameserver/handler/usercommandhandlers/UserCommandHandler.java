package l2e.gameserver.handler.usercommandhandlers;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.handler.usercommandhandlers.impl.ChannelDelete;
import l2e.gameserver.handler.usercommandhandlers.impl.ChannelInfo;
import l2e.gameserver.handler.usercommandhandlers.impl.ChannelLeave;
import l2e.gameserver.handler.usercommandhandlers.impl.ClanPenalty;
import l2e.gameserver.handler.usercommandhandlers.impl.ClanWarsList;
import l2e.gameserver.handler.usercommandhandlers.impl.DisMount;
import l2e.gameserver.handler.usercommandhandlers.impl.InstanceZone;
import l2e.gameserver.handler.usercommandhandlers.impl.Loc;
import l2e.gameserver.handler.usercommandhandlers.impl.Mount;
import l2e.gameserver.handler.usercommandhandlers.impl.MyBirthday;
import l2e.gameserver.handler.usercommandhandlers.impl.OlympiadStat;
import l2e.gameserver.handler.usercommandhandlers.impl.PartyInfo;
import l2e.gameserver.handler.usercommandhandlers.impl.SiegeStatus;
import l2e.gameserver.handler.usercommandhandlers.impl.Time;
import l2e.gameserver.handler.usercommandhandlers.impl.Unstuck;

public class UserCommandHandler {
   private static Logger _log = Logger.getLogger(UserCommandHandler.class.getName());
   private final Map<Integer, IUserCommandHandler> _handlers = new HashMap<>();

   public static UserCommandHandler getInstance() {
      return UserCommandHandler.SingletonHolder._instance;
   }

   protected UserCommandHandler() {
      this.registerHandler(new ChannelDelete());
      this.registerHandler(new ChannelInfo());
      this.registerHandler(new ChannelLeave());
      this.registerHandler(new ClanPenalty());
      this.registerHandler(new ClanWarsList());
      this.registerHandler(new DisMount());
      this.registerHandler(new InstanceZone());
      this.registerHandler(new Loc());
      this.registerHandler(new Mount());
      this.registerHandler(new MyBirthday());
      this.registerHandler(new OlympiadStat());
      this.registerHandler(new PartyInfo());
      this.registerHandler(new SiegeStatus());
      this.registerHandler(new Time());
      this.registerHandler(new Unstuck());
      _log.info("Loaded " + this._handlers.size() + " UserHandlers.");
   }

   public void registerHandler(IUserCommandHandler handler) {
      int[] ids = handler.getUserCommandList();

      for(int i = 0; i < ids.length; ++i) {
         if (this._handlers.containsKey(ids[i])) {
            _log.fine(
               "UserCommand: dublicate bypass registered! First handler: "
                  + this._handlers.get(ids[i]).getClass().getSimpleName()
                  + " second: "
                  + handler.getClass().getSimpleName()
            );
            this._handlers.remove(ids[i]);
         }

         this._handlers.put(ids[i], handler);
      }
   }

   public synchronized void removeHandler(IUserCommandHandler handler) {
      int[] ids = handler.getUserCommandList();

      for(int id : ids) {
         this._handlers.remove(id);
      }
   }

   public IUserCommandHandler getHandler(Integer userCommand) {
      if (Config.DEBUG) {
         _log.fine("getting handler for user command: " + userCommand);
      }

      return this._handlers.get(userCommand);
   }

   public int size() {
      return this._handlers.size();
   }

   private static class SingletonHolder {
      protected static final UserCommandHandler _instance = new UserCommandHandler();
   }
}
