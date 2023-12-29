package l2e.gameserver.listener.talk;

import l2e.gameserver.listener.AbstractListener;
import l2e.gameserver.listener.events.RequestBypassToServerEvent;
import l2e.gameserver.network.clientpackets.RequestBypassToServer;

public abstract class RequestBypassToServerListener extends AbstractListener {
   public RequestBypassToServerListener() {
      this.register();
   }

   public abstract void onRequestBypassToServer(RequestBypassToServerEvent var1);

   @Override
   public void register() {
      RequestBypassToServer.addBypassListener(this);
   }

   @Override
   public void unregister() {
      RequestBypassToServer.removeBypassListener(this);
   }
}
