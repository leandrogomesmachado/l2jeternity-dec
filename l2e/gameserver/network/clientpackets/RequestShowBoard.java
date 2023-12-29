package l2e.gameserver.network.clientpackets;

import l2e.gameserver.Config;
import l2e.gameserver.handler.communityhandlers.CommunityBoardHandler;
import l2e.gameserver.handler.communityhandlers.ICommunityBoardHandler;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.events.cleft.AerialCleftEvent;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.type.FunPvpZone;
import l2e.gameserver.network.SystemMessageId;

public final class RequestShowBoard extends GameClientPacket {
   protected int _unknown;

   @Override
   protected final void readImpl() {
      this._unknown = this.readD();
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         if (!player.isBlocked()
            && !player.isCursedWeaponEquipped()
            && !player.isInDuel()
            && !player.isFlying()
            && !player.isJailed()
            && !player.isInOlympiadMode()
            && !player.inObserverMode()
            && !player.isAlikeDead()
            && !player.isInSiege()
            && !player.isDead()) {
            if ((AerialCleftEvent.getInstance().isStarted() || AerialCleftEvent.getInstance().isRewarding())
               && AerialCleftEvent.getInstance().isPlayerParticipant(player.getObjectId())) {
               player.sendMessage(new ServerMessage("Community.ALL_DISABLE", player.getLang()).toString());
            } else {
               if (player.isInsideZone(ZoneId.PVP) && !player.isInFightEvent()) {
                  if (!player.isInsideZone(ZoneId.FUN_PVP)) {
                     player.sendMessage(new ServerMessage("Community.ALL_DISABLE", player.getLang()).toString());
                     return;
                  }

                  FunPvpZone zone = ZoneManager.getInstance().getZone(player, FunPvpZone.class);
                  if (zone != null && !zone.canUseCbBuffs() && !zone.canUseCbTeleports()) {
                     player.sendMessage(new ServerMessage("Community.ALL_DISABLE", player.getLang()).toString());
                     return;
                  }
               }

               if (player.isInCombat() && !player.isInFightEvent() && !player.isInsideZone(ZoneId.FUN_PVP)) {
                  player.sendMessage(new ServerMessage("Community.ALL_DISABLE", player.getLang()).toString());
               } else if (player.isCastingNow() && !player.isInFightEvent() && !player.isInsideZone(ZoneId.FUN_PVP)) {
                  player.sendMessage(new ServerMessage("Community.ALL_DISABLE", player.getLang()).toString());
               } else if (player.isAttackingNow() && !player.isInFightEvent() && !player.isInsideZone(ZoneId.FUN_PVP)) {
                  player.sendMessage(new ServerMessage("Community.ALL_DISABLE", player.getLang()).toString());
               } else if (Config.ALLOW_COMMUNITY_PEACE_ZONE && !player.isInsideZone(ZoneId.PEACE) && !player.isInFightEvent()) {
                  player.sendMessage(new ServerMessage("Community.ALL_DISABLE", player.getLang()).toString());
               } else {
                  player.isntAfk();
                  if (Config.ALLOW_COMMUNITY) {
                     ICommunityBoardHandler handler = CommunityBoardHandler.getInstance().getHandler(Config.BBS_HOME_PAGE);
                     if (handler != null) {
                        handler.onBypassCommand(Config.BBS_HOME_PAGE, player);
                     }
                  } else {
                     player.sendPacket(SystemMessageId.CB_OFFLINE);
                  }
               }
            }
         } else {
            player.sendMessage(new ServerMessage("Community.ALL_DISABLE", player.getLang()).toString());
         }
      }
   }

   @Override
   protected boolean triggersOnActionRequest() {
      return false;
   }
}
