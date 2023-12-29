package l2e.gameserver.model.actor.instance;

import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.network.serverpackets.CastleSiegeInfo;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class CastleManageInstance extends NpcInstance {
   public CastleManageInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
   }

   @Override
   public void onBypassFeedback(Player player, String command) {
      if (player != null) {
         if (this.canTarget(player)) {
            if (command.startsWith("siege_")) {
               int castleId = 0;
               if (command.startsWith("siege_gludio")) {
                  castleId = 1;
               } else if (command.startsWith("siege_dion")) {
                  castleId = 2;
               } else if (command.startsWith("siege_giran")) {
                  castleId = 3;
               } else if (command.startsWith("siege_oren")) {
                  castleId = 4;
               } else if (command.startsWith("siege_aden")) {
                  castleId = 5;
               } else if (command.startsWith("siege_innadril")) {
                  castleId = 6;
               } else if (command.startsWith("siege_goddard")) {
                  castleId = 7;
               } else if (command.startsWith("siege_rune")) {
                  castleId = 8;
               } else if (command.startsWith("siege_schuttgart")) {
                  castleId = 9;
               }

               Castle castle = CastleManager.getInstance().getCastleById(castleId);
               if (castle != null && castleId != 0) {
                  player.sendPacket(new CastleSiegeInfo(castle));
               }
            }
         }
      }
   }

   @Override
   public void showChatWindow(Player player) {
      player.sendActionFailed();
      NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
      html.setFile(player, player.getLang(), "data/html/mods/CastleManager.htm");
      html.replace("%objectId%", String.valueOf(this.getObjectId()));
      player.sendPacket(html);
   }
}
