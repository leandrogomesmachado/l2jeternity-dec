package l2e.gameserver.model.actor.listener;

import l2e.gameserver.instancemanager.BotCheckManager;
import l2e.gameserver.listener.talk.OnAnswerListener;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.effects.AbnormalEffect;
import l2e.gameserver.model.strings.server.ServerMessage;

public class BotCheckAnswerListner implements OnAnswerListener {
   private final Player _player;
   private final int _qId;

   public BotCheckAnswerListner(Player player, int qId) {
      this._player = player;
      this._qId = qId;
   }

   @Override
   public void sayYes() {
      if (this._player != null) {
         boolean rightAnswer = BotCheckManager.getInstance().checkAnswer(this._qId, true);
         if (rightAnswer) {
            this._player.increaseBotRating();
            this.sendFeedBack(this._player, true);
         } else {
            this.sendFeedBack(this._player, false);
            this._player.decreaseBotRating();
         }
      }
   }

   @Override
   public void sayNo() {
      if (this._player != null) {
         boolean rightAnswer = BotCheckManager.getInstance().checkAnswer(this._qId, false);
         if (rightAnswer) {
            this._player.increaseBotRating();
            this.sendFeedBack(this._player, true);
         } else {
            this._player.decreaseBotRating();
            this.sendFeedBack(this._player, false);
         }
      }
   }

   private void sendFeedBack(Player player, boolean rightAnswer) {
      if (rightAnswer) {
         player.sendMessage(new ServerMessage("BotCheck.CORRECT_ANSWER", player.getLang()).toString());
      } else {
         player.sendMessage(new ServerMessage("BotCheck.INCORRECT_ANSWER", player.getLang()).toString());
      }

      if (player.isParalyzed()) {
         player.stopAbnormalEffect(AbnormalEffect.HOLD_2);
         player.setIsParalyzed(false);
      }

      player.setIsInvul(false);
      player.stopBotCheckTask();
   }
}
