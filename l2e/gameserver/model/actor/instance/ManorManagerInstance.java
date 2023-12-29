package l2e.gameserver.model.actor.instance;

import l2e.gameserver.instancemanager.CastleManorManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;

public class ManorManagerInstance extends MerchantInstance {
   public ManorManagerInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.ManorManagerInstance);
   }

   @Override
   public String getHtmlPath(int npcId, int val) {
      return "data/html/manormanager/manager.htm";
   }

   @Override
   public void showChatWindow(Player player) {
      if (CastleManorManager.getInstance().isDisabled()) {
         this.showChatWindow(player, "data/html/npcdefault.htm");
      } else {
         if (!player.canOverrideCond(PcCondOverride.CASTLE_CONDITIONS)
            && this.getCastle() != null
            && this.getCastle().getId() > 0
            && player.isClanLeader()
            && this.getCastle().getOwnerId() == player.getClanId()) {
            this.showChatWindow(player, "data/html/manormanager/manager-lord.htm");
         } else {
            this.showChatWindow(player, "data/html/manormanager/manager.htm");
         }
      }
   }
}
