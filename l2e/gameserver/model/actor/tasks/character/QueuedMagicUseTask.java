package l2e.gameserver.model.actor.tasks.character;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.Skill;

public final class QueuedMagicUseTask implements Runnable {
   private final Player _currPlayer;
   private final Skill _queuedSkill;
   private final boolean _isCtrlPressed;
   private final boolean _isShiftPressed;

   public QueuedMagicUseTask(Player currPlayer, Skill queuedSkill, boolean isCtrlPressed, boolean isShiftPressed) {
      this._currPlayer = currPlayer;
      this._queuedSkill = queuedSkill;
      this._isCtrlPressed = isCtrlPressed;
      this._isShiftPressed = isShiftPressed;
   }

   @Override
   public void run() {
      if (this._currPlayer != null) {
         this._currPlayer.useMagic(this._queuedSkill, this._isCtrlPressed, this._isShiftPressed, true);
      }
   }
}
