package l2e.gameserver.model.actor.templates;

import l2e.gameserver.network.NpcStringId;

public class NpcWalkerTemplate {
   private final int _routeId;
   private final String _chatString;
   private final NpcStringId _npcString;
   private final int _moveX;
   private final int _moveY;
   private final int _moveZ;
   private final int _delay;
   private final boolean _running;

   public NpcWalkerTemplate(int routeId, NpcStringId npcString, String chatText, int moveX, int moveY, int moveZ, int delay, boolean running) {
      this._routeId = routeId;
      this._chatString = chatText;
      this._npcString = npcString;
      this._moveX = moveX;
      this._moveY = moveY;
      this._moveZ = moveZ;
      this._delay = delay;
      this._running = running;
   }

   public int getRouteId() {
      return this._routeId;
   }

   public String getChatText() {
      if (this._npcString != null) {
         throw new IllegalStateException("npcString is defined for walker route!");
      } else {
         return this._chatString;
      }
   }

   public int getMoveX() {
      return this._moveX;
   }

   public int getMoveY() {
      return this._moveY;
   }

   public int getMoveZ() {
      return this._moveZ;
   }

   public int getDelay() {
      return this._delay;
   }

   public boolean getRunning() {
      return this._running;
   }

   public NpcStringId getNpcString() {
      return this._npcString;
   }
}
