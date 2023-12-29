package l2e.gameserver.listener.events;

import l2e.gameserver.listener.EventListener;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.player.PcTemplate;

public class ProfessionChangeEvent implements EventListener {
   private Player _player;
   private boolean _isSubClass;
   private PcTemplate _template;

   public Player getPlayer() {
      return this._player;
   }

   public void setPlayer(Player player) {
      this._player = player;
   }

   public boolean isSubClass() {
      return this._isSubClass;
   }

   public void setSubClass(boolean isSubClass) {
      this._isSubClass = isSubClass;
   }

   public PcTemplate getTemplate() {
      return this._template;
   }

   public void setTemplate(PcTemplate template) {
      this._template = template;
   }
}
