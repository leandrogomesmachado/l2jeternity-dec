package l2e.gameserver.handler.actionhandlers;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import l2e.gameserver.handler.actionhandlers.impl.ArtefactAction;
import l2e.gameserver.handler.actionhandlers.impl.DecoyAction;
import l2e.gameserver.handler.actionhandlers.impl.DoorAction;
import l2e.gameserver.handler.actionhandlers.impl.ItemAction;
import l2e.gameserver.handler.actionhandlers.impl.NpcAction;
import l2e.gameserver.handler.actionhandlers.impl.PetAction;
import l2e.gameserver.handler.actionhandlers.impl.PlayerAction;
import l2e.gameserver.handler.actionhandlers.impl.StaticObjectAction;
import l2e.gameserver.handler.actionhandlers.impl.SummonAction;
import l2e.gameserver.handler.actionhandlers.impl.TrapAction;
import l2e.gameserver.model.GameObject;

public class ActionHandler {
   private static Logger _log = Logger.getLogger(ActionHandler.class.getName());
   private final Map<GameObject.InstanceType, IActionHandler> _handlers = new HashMap<>();

   public static ActionHandler getInstance() {
      return ActionHandler.SingletonHolder._instance;
   }

   protected ActionHandler() {
      this.registerHandler(new ArtefactAction());
      this.registerHandler(new DecoyAction());
      this.registerHandler(new DoorAction());
      this.registerHandler(new ItemAction());
      this.registerHandler(new NpcAction());
      this.registerHandler(new PlayerAction());
      this.registerHandler(new PetAction());
      this.registerHandler(new StaticObjectAction());
      this.registerHandler(new SummonAction());
      this.registerHandler(new TrapAction());
      _log.info("Loaded " + this._handlers.size() + " ActionHandlers");
   }

   public void registerHandler(IActionHandler handler) {
      this._handlers.put(handler.getInstanceType(), handler);
   }

   public synchronized void removeHandler(IActionHandler handler) {
      this._handlers.remove(handler.getInstanceType());
   }

   public IActionHandler getHandler(GameObject.InstanceType iType) {
      IActionHandler result = null;

      for(GameObject.InstanceType t = iType; t != null; t = t.getParent()) {
         result = this._handlers.get(t);
         if (result != null) {
            break;
         }
      }

      return result;
   }

   public int size() {
      return this._handlers.size();
   }

   private static class SingletonHolder {
      protected static final ActionHandler _instance = new ActionHandler();
   }
}
