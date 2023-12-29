package l2e.gameserver.handler.actionhandlers;

import java.util.logging.Logger;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;

public interface IActionHandler {
   Logger _log = Logger.getLogger(IActionHandler.class.getName());

   boolean action(Player var1, GameObject var2, boolean var3);

   GameObject.InstanceType getInstanceType();
}
