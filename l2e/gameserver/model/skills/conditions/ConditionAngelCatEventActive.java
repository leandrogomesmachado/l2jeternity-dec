package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.events.AbstractWorldEvent;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.SystemMessageId;

public class ConditionAngelCatEventActive extends Condition {
   private final boolean _val;

   public ConditionAngelCatEventActive(boolean val) {
      this._val = val;
   }

   @Override
   public boolean testImpl(Env env) {
      boolean canUse = true;
      Player player = env.getPlayer();
      if (player == null) {
         canUse = false;
      }

      AbstractWorldEvent event = (AbstractWorldEvent)QuestManager.getInstance().getQuest("AngelCat");
      if (event != null && !event.isEventActive()) {
         player.sendPacket(SystemMessageId.NOTHING_HAPPENED);
         canUse = false;
      }

      return this._val == canUse;
   }
}
