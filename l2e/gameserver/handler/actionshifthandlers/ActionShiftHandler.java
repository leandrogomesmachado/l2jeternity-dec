package l2e.gameserver.handler.actionshifthandlers;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import l2e.gameserver.handler.actionhandlers.IActionHandler;
import l2e.gameserver.handler.actionshifthandlers.impl.DoorActionShift;
import l2e.gameserver.handler.actionshifthandlers.impl.ItemActionShift;
import l2e.gameserver.handler.actionshifthandlers.impl.NpcActionShift;
import l2e.gameserver.handler.actionshifthandlers.impl.PlayerActionShift;
import l2e.gameserver.handler.actionshifthandlers.impl.StaticObjectActionShift;
import l2e.gameserver.handler.actionshifthandlers.impl.SummonActionShift;
import l2e.gameserver.model.GameObject;

public class ActionShiftHandler {
   private static Logger _log = Logger.getLogger(ActionShiftHandler.class.getName());
   private final Map<GameObject.InstanceType, IActionHandler> _handlers = new HashMap<>();

   public static ActionShiftHandler getInstance() {
      return ActionShiftHandler.SingletonHolder._instance;
   }

   protected ActionShiftHandler() {
      this.registerHandler(new DoorActionShift());
      this.registerHandler(new ItemActionShift());
      this.registerHandler(new NpcActionShift());
      this.registerHandler(new PlayerActionShift());
      this.registerHandler(new StaticObjectActionShift());
      this.registerHandler(new SummonActionShift());
      _log.info("Loaded " + this._handlers.size() + " ActionShiftHandlers");
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
      protected static final ActionShiftHandler _instance = new ActionShiftHandler();
   }
}
