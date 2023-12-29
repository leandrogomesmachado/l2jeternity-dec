package l2e.gameserver.model.actor.tasks.player;

import l2e.gameserver.instancemanager.PunishmentManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.punishment.PunishmentTemplate;

public class PunishmentTask implements Runnable {
   private final Player _player;
   PunishmentTemplate _template;

   public PunishmentTask(Player player, PunishmentTemplate template) {
      this._player = player;
      this._template = template;
   }

   @Override
   public void run() {
      if (this._player != null) {
         PunishmentManager.getInstance().stopPunishment(this._player.getClient(), this._template.getType(), this._template.getAffect());
      }
   }
}
