package l2e.gameserver.listener.events;

import l2e.gameserver.listener.EventListener;
import l2e.gameserver.listener.ScriptListener;
import l2e.gameserver.model.entity.Siege;

public class SiegeEvent implements EventListener {
   private Siege _siege;
   private ScriptListener.EventStage _stage;

   public Siege getSiege() {
      return this._siege;
   }

   public void setSiege(Siege siege) {
      this._siege = siege;
   }

   public ScriptListener.EventStage getStage() {
      return this._stage;
   }

   public void setStage(ScriptListener.EventStage stage) {
      this._stage = stage;
   }
}
