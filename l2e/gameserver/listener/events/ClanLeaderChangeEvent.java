package l2e.gameserver.listener.events;

import l2e.gameserver.listener.EventListener;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.actor.Player;

public class ClanLeaderChangeEvent implements EventListener {
   private Clan _clan;
   private Player _newLeader;
   private Player _oldLeader;

   public Clan getClan() {
      return this._clan;
   }

   public void setClan(Clan clan) {
      this._clan = clan;
   }

   public Player getNewLeader() {
      return this._newLeader;
   }

   public void setNewLeader(Player newLeader) {
      this._newLeader = newLeader;
   }

   public Player getOldLeader() {
      return this._oldLeader;
   }

   public void setOldLeader(Player oldLeader) {
      this._oldLeader = oldLeader;
   }
}
