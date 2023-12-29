package l2e.gameserver.handler.itemhandlers;

import java.util.logging.Logger;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.items.instance.ItemInstance;

public interface IItemHandler {
   Logger _log = Logger.getLogger(IItemHandler.class.getName());

   boolean useItem(Playable var1, ItemInstance var2, boolean var3);
}
