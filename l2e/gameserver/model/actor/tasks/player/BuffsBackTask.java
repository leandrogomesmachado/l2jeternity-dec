package l2e.gameserver.model.actor.tasks.player;

import java.util.List;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.effects.Effect;

public class BuffsBackTask implements Runnable {
   private final Player _player;
   private final List<Effect> _effects;

   public BuffsBackTask(Player player, List<Effect> effects) {
      this._player = player;
      this._effects = effects;
   }

   @Override
   public void run() {
      if (this._player != null && !this._player.isInOlympiadMode() && this._effects != null && !this._effects.isEmpty()) {
         for(Effect e : this._effects) {
            e.scheduleEffect(true);
         }
      }
   }
}
