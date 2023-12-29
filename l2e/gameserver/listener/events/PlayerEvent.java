package l2e.gameserver.listener.events;

import l2e.gameserver.listener.EventListener;
import l2e.gameserver.network.GameClient;

public class PlayerEvent implements EventListener {
   private int _objectId;
   private String _name;
   private GameClient _client;

   public void setObjectId(int objectId) {
      this._objectId = objectId;
   }

   public int getObjectId() {
      return this._objectId;
   }

   public void setName(String name) {
      this._name = name;
   }

   public String getName() {
      return this._name;
   }

   public void setClient(GameClient client) {
      this._client = client;
   }

   public GameClient getClient() {
      return this._client;
   }
}
