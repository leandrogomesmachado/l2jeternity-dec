package l2e.gameserver.model.actor.instance;

import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.entity.events.cleft.AerialCleftEvent;
import l2e.gameserver.model.olympiad.OlympiadManager;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.network.SystemMessageId;

public final class CleftGatesInstance extends NpcInstance {
   public CleftGatesInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
   }

   @Override
   public void showChatWindow(Player player) {
      boolean checkReg = false;

      for(Skill s : player.getAllSkills()) {
         if (s != null && (s.getId() == 840 || s.getId() == 841 || s.getId() == 842)) {
            checkReg = true;
         }
      }

      if (player.getLevel() >= 75 && checkReg) {
         switch(this.getId()) {
            case 32518:
            case 32519:
               if (AerialCleftEvent.getInstance().isPlayerParticipant(player.getObjectId())) {
                  this.showChatWindow(player, "data/html/aerialCleft/32518-02.htm");
               } else {
                  this.showChatWindow(player, "data/html/aerialCleft/32518-00.htm");
               }
         }
      } else {
         this.showChatWindow(player, "data/html/aerialCleft/32518-03.htm");
      }
   }

   @Override
   public void onBypassFeedback(Player player, String command) {
      if (player != null && player.getLastFolkNPC() != null && player.getLastFolkNPC().getObjectId() == this.getObjectId()) {
         if (command.equalsIgnoreCase("Info")) {
            this.showChatWindow(player, "data/html/aerialCleft/32518-01.htm");
         } else if (command.startsWith("Register")) {
            if (OlympiadManager.getInstance().isRegistered(player)
               || player.isInOlympiadMode()
               || player.isInFightEvent()
               || player.isRegisteredInFightEvent()
               || player.isOnEvent()
               || player.getTeam() != 0) {
               player.sendPacket(
                  SystemMessageId.YOU_CANNOT_BE_SIMULTANEOUSLY_REGISTERED_FOR_PVP_MATCHES_SUCH_AS_THE_OLYMPIAD_UNDERGROUND_COLISEUM_AERIAL_CLEFT_KRATEIS_CUBE_AND_HANDYS_BLOCK_CHECKERS
               );
               return;
            }

            if (player.getParty() != null && player.getParty().getUCState() != null || player.getUCState() > 0) {
               player.sendPacket(
                  SystemMessageId.YOU_CANNOT_BE_SIMULTANEOUSLY_REGISTERED_FOR_PVP_MATCHES_SUCH_AS_THE_OLYMPIAD_UNDERGROUND_COLISEUM_AERIAL_CLEFT_KRATEIS_CUBE_AND_HANDYS_BLOCK_CHECKERS
               );
               return;
            }

            if (player.isCursedWeaponEquipped()) {
               player.sendPacket(SystemMessageId.CANNOT_REGISTER_PROCESSING_CURSED_WEAPON);
               return;
            }

            if (AerialCleftEvent.getInstance().checkRegistration()) {
               if (AerialCleftEvent.getInstance().isPlayerParticipant(player.getObjectId())) {
                  this.showChatWindow(player, "data/html/aerialCleft/32518-02.htm");
                  return;
               }

               if (AerialCleftEvent.getInstance().isValidRegistration()) {
                  if (AerialCleftEvent.getInstance().getTotalEventPlayers() < Config.CLEFT_MAX_PLAYERS) {
                     AerialCleftEvent.getInstance().registerPlayer(player);
                  } else {
                     this.showChatWindow(player, "data/html/aerialCleft/32518-02.htm");
                  }
               } else {
                  this.showChatWindow(player, "data/html/aerialCleft/32518-05.htm");
               }
            } else {
               this.showChatWindow(player, "data/html/aerialCleft/32518-04.htm");
            }
         } else {
            super.onBypassFeedback(player, command);
         }
      }
   }
}
