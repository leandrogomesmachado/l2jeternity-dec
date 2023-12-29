package l2e.gameserver.model.actor.events;

import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Summon;

public class SummonEvents extends PlayableEvents {
   public SummonEvents(Playable activeChar) {
      super(activeChar);
   }

   public Summon getActingPlayer() {
      return (Summon)super.getActingPlayer();
   }
}
