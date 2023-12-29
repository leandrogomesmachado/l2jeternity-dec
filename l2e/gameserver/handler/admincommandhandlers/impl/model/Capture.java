package l2e.gameserver.handler.admincommandhandlers.impl.model;

import l2e.gameserver.model.actor.Player;

public class Capture implements Runnable {
   private final Player _player;
   private final String _param;

   public Capture(Player player, String param) {
      this._player = player;
      this._param = param;
   }

   @Override
   public void run() {
      ViewerUtils.startLogViewer(this._player, this._param);
   }
}
