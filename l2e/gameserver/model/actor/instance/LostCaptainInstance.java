package l2e.gameserver.model.actor.instance;

import l2e.gameserver.Config;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;

public class LostCaptainInstance extends RaidBossInstance {
   public LostCaptainInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
      this.setIsRaid(true);
   }

   @Override
   protected void onDeath(Creature killer) {
      ReflectionWorld r = ReflectionManager.getInstance().getWorld(this.getReflectionId());
      if (r != null && r.getAllowed() != null) {
         for(int objectId : r.getAllowed()) {
            Player player = World.getInstance().getPlayer(objectId);
            if (player != null) {
               if (!Config.ALT_KAMALOKA_ESSENCE_PREMIUM_ONLY || player.hasPremiumBonus()) {
                  switch(r.getTemplateId()) {
                     case 59:
                        player.addItem("Reward by LostCaptainInstance die", 13002, 8L, player, true);
                        break;
                     case 62:
                        player.addItem("Reward by LostCaptainInstance die", 13002, 9L, player, true);
                        break;
                     case 65:
                        player.addItem("Reward by LostCaptainInstance die", 13002, 11L, player, true);
                        break;
                     case 68:
                        player.addItem("Reward by LostCaptainInstance die", 13002, 13L, player, true);
                        break;
                     case 71:
                        player.addItem("Reward by LostCaptainInstance die", 13002, 15L, player, true);
                        break;
                     case 73:
                        player.addItem("Reward by LostCaptainInstance die", 13002, 5L, player, true);
                        break;
                     case 74:
                        player.addItem("Reward by LostCaptainInstance die", 13002, 7L, player, true);
                        break;
                     case 75:
                        player.addItem("Reward by LostCaptainInstance die", 13002, 8L, player, true);
                        break;
                     case 76:
                        player.addItem("Reward by LostCaptainInstance die", 13002, 12L, player, true);
                        break;
                     case 77:
                        player.addItem("Reward by LostCaptainInstance die", 13002, 15L, player, true);
                        break;
                     case 78:
                        player.addItem("Reward by LostCaptainInstance die", 13002, 18L, player, true);
                        break;
                     case 79:
                        player.addItem("Reward by LostCaptainInstance die", 13002, 18L, player, true);
                        break;
                     case 134:
                        player.addItem("Reward by LostCaptainInstance die", 13002, 19L, player, true);
                  }
               }

               player.getCounters().addAchivementInfo("lostCaptainKiller", this.getId(), -1L, false, true, false);
            }
         }
      }

      super.onDeath(killer);
   }
}
