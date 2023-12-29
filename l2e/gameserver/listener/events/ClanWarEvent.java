package l2e.gameserver.listener.events;

import l2e.gameserver.listener.EventListener;
import l2e.gameserver.listener.ScriptListener;
import l2e.gameserver.model.Clan;

public class ClanWarEvent implements EventListener {
   private Clan _clan1;
   private Clan _clan2;
   private ScriptListener.EventStage _stage;

   public Clan getClan1() {
      return this._clan1;
   }

   public void setClan1(Clan clan1) {
      this._clan1 = clan1;
   }

   public Clan getClan2() {
      return this._clan2;
   }

   public void setClan2(Clan clan2) {
      this._clan2 = clan2;
   }

   public ScriptListener.EventStage getStage() {
      return this._stage;
   }

   public void setStage(ScriptListener.EventStage stage) {
      this._stage = stage;
   }
}
