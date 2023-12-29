package l2e.gameserver.listener.events;

import l2e.gameserver.listener.EventListener;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.transform.Transform;

public class TransformEvent implements EventListener {
   private Player _player;
   private Transform _transformation;
   private boolean _transforming;

   public Player getPlayer() {
      return this._player;
   }

   public void setPlayer(Player player) {
      this._player = player;
   }

   public Transform getTransformation() {
      return this._transformation;
   }

   public void setTransformation(Transform transformation) {
      this._transformation = transformation;
   }

   public boolean isTransforming() {
      return this._transforming;
   }

   public void setTransforming(boolean transforming) {
      this._transforming = transforming;
   }
}
