package l2e.gameserver.model.actor.listener;

import l2e.gameserver.instancemanager.CoupleManager;
import l2e.gameserver.listener.talk.OnAnswerListener;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;

public class EngageAnswerListener implements OnAnswerListener {
   private final Player _player;

   public EngageAnswerListener(Player player) {
      this._player = player;
   }

   @Override
   public void sayYes() {
      if (this._player != null && this._player.isOnline()) {
         if (!this._player.isEngageRequest() || this._player.getEngageId() == 0) {
            return;
         }

         Player partner = World.getInstance().getPlayer(this._player.getEngageId());
         if (partner != null) {
            CoupleManager.getInstance().createCouple(partner, this._player);
            partner.sendMessage("Request to Engage has been >ACCEPTED<");
         }

         this._player.setEngageRequest(false, 0);
      }
   }

   @Override
   public void sayNo() {
      if (this._player != null && this._player.isOnline()) {
         if (!this._player.isEngageRequest() || this._player.getEngageId() == 0) {
            return;
         }

         Player partner = World.getInstance().getPlayer(this._player.getEngageId());
         if (partner != null) {
            partner.sendMessage("Request to Engage has been >DENIED<!");
         }

         this._player.setEngageRequest(false, 0);
      }
   }
}
