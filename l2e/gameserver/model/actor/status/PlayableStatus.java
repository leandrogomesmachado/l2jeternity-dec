package l2e.gameserver.model.actor.status;

import l2e.gameserver.model.actor.Playable;

public class PlayableStatus extends CharStatus {
   public PlayableStatus(Playable activeChar) {
      super(activeChar);
   }

   public Playable getActiveChar() {
      return (Playable)super.getActiveChar();
   }
}
