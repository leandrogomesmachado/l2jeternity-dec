package l2e.gameserver.model.actor.listener;

import l2e.gameserver.handler.communityhandlers.impl.CommunityBuffer;
import l2e.gameserver.listener.talk.OnAnswerListener;
import l2e.gameserver.model.actor.Player;

public class AskQuestionAnswerListener implements OnAnswerListener {
   private final Player _player;

   public AskQuestionAnswerListener(Player player) {
      this._player = player;
   }

   @Override
   public void sayYes() {
      if (this._player != null) {
         CommunityBuffer.getInstance().deleteScheme(this._player.getQuickVarI("schemeToDel"), this._player);
         this._player.deleteQuickVar("schemeToDel");
         CommunityBuffer.getInstance().showCommunity(this._player, CommunityBuffer.main(this._player));
      }
   }

   @Override
   public void sayNo() {
      if (this._player != null) {
         CommunityBuffer.getInstance().showCommunity(this._player, CommunityBuffer.main(this._player));
      }
   }
}
