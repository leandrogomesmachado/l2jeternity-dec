package l2e.gameserver.model.actor.instance;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.entity.events.custom.achievements.AchievementManager;

public class AchievementsInstance extends NpcInstance {
   public AchievementsInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
   }

   @Override
   public void showChatWindow(Player player, int val) {
      if (AchievementManager.getInstance().isActive()) {
         AchievementManager.getInstance().onBypass(player, "_bbs_achievements", null);
      }
   }
}
