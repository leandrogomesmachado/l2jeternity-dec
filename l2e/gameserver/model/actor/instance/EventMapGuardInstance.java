package l2e.gameserver.model.actor.instance;

import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;

public final class EventMapGuardInstance extends GuardInstance {
   private static Logger _log = Logger.getLogger(GuardInstance.class.getName());

   public EventMapGuardInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
   }

   @Override
   public boolean isAutoAttackable(Creature attacker) {
      return false;
   }

   @Override
   public void onAction(Player player) {
      if (this.canTarget(player)) {
         if (this.getObjectId() != player.getTargetId()) {
            if (Config.DEBUG) {
               _log.fine(player.getObjectId() + ": Targetted guard " + this.getObjectId());
            }

            player.setTarget(this);
         } else if (this.containsTarget(player)) {
            if (Config.DEBUG) {
               _log.fine(player.getObjectId() + ": Attacked guard " + this.getObjectId());
            }

            player.getAI().setIntention(CtrlIntention.ATTACK, this);
         } else if (!this.canInteract(player)) {
            player.getAI().setIntention(CtrlIntention.INTERACT, this);
         } else {
            player.sendMessage("Did you know that you are on the event right now?");
            player.sendActionFailed();
         }

         player.sendActionFailed();
      }
   }
}
