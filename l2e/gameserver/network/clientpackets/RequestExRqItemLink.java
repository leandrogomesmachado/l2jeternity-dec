package l2e.gameserver.network.clientpackets;

import l2e.gameserver.Config;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.World;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.GameClient;
import l2e.gameserver.network.serverpackets.ExRpItemLink;

public class RequestExRqItemLink extends GameClientPacket {
   private int _objectId;

   @Override
   protected void readImpl() {
      this._objectId = this.readD();
   }

   @Override
   protected void runImpl() {
      GameClient client = this.getClient();
      if (client != null) {
         GameObject object = World.getInstance().findObject(this._objectId);
         if (object instanceof ItemInstance) {
            ItemInstance item = (ItemInstance)object;
            if (item.isPublished()) {
               client.sendPacket(new ExRpItemLink(item));
            } else if (Config.DEBUG) {
               _log.info(this.getClient() + " requested item link for item which wasnt published! ID:" + this._objectId);
            }
         } else {
            client.getActiveChar().getListeners().onQuestionMarkClicked(this._objectId);
         }
      }
   }
}
