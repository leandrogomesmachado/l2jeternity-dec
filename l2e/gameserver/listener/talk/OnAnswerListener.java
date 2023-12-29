package l2e.gameserver.listener.talk;

import l2e.gameserver.listener.player.AbstractPlayerListener;

public interface OnAnswerListener extends AbstractPlayerListener {
   void sayYes();

   void sayNo();
}
