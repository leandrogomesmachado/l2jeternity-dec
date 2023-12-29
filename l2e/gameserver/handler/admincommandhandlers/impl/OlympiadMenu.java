package l2e.gameserver.handler.admincommandhandlers.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.holder.CustomHeroHolder;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.ClanMember;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.olympiad.Olympiad;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.SocialAction;

public class OlympiadMenu implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{"admin_olyinfo", "admin_olysave", "admin_olystart", "admin_olyend", "admin_sethero"};

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      if (command.startsWith("admin_olyinfo")) {
         this.showMenu(activeChar);
      } else if (command.startsWith("admin_olysave")) {
         Olympiad.getInstance().saveOlympiadStatus();
         ThreadPoolManager.getInstance().schedule(new OlympiadMenu.RefreshMenu(activeChar), 100L);
         activeChar.sendMessage("olympiad system saved.");
      } else if (command.startsWith("admin_olyend")) {
         try {
            Olympiad.getInstance().manualSelectHeroes();
         } catch (Exception var7) {
            activeChar.sendMessage("Problem while ending olympiad...");
         }

         ThreadPoolManager.getInstance().schedule(new OlympiadMenu.RefreshMenu(activeChar), 100L);
         activeChar.sendMessage("Heroes formed");
      } else if (command.startsWith("admin_olystart")) {
         try {
            Olympiad.getInstance().manualStartNewOlympiad();
         } catch (Exception var6) {
            activeChar.sendMessage("Problem while starting olympiad...");
         }

         ThreadPoolManager.getInstance().schedule(new OlympiadMenu.RefreshMenu(activeChar), 100L);
      } else if (command.startsWith("admin_sethero")) {
         GameObject target = activeChar.getTarget();
         if (target instanceof Player) {
            Player targetPlayer = (Player)target;
            boolean isHero = targetPlayer.isHero();
            if (isHero) {
               targetPlayer.setHero(false, true);
               CustomHeroHolder.updateDatabase(targetPlayer, false);
               targetPlayer.sendMessage("You are not hero now!");
               if (targetPlayer.getClan() != null) {
                  targetPlayer.setPledgeClass(ClanMember.calculatePledgeClass(targetPlayer));
               } else {
                  targetPlayer.setPledgeClass(targetPlayer.isNoble() ? 5 : 1);
               }
            } else {
               targetPlayer.setHero(true, true);
               if (targetPlayer.getClan() != null) {
                  targetPlayer.setPledgeClass(ClanMember.calculatePledgeClass(targetPlayer));
               } else {
                  targetPlayer.setPledgeClass(8);
               }

               targetPlayer.broadcastPacket(new SocialAction(targetPlayer.getObjectId(), 16));
               CustomHeroHolder.updateDatabase(targetPlayer, true);
               targetPlayer.sendMessage("You are hero now!");
            }

            targetPlayer.broadcastUserInfo(true);
         }

         this.showMenu(activeChar);
      }

      return true;
   }

   private void showMenu(Player activeChar) {
      NpcHtmlMessage html = new NpcHtmlMessage(0);
      html.setFile(activeChar, activeChar.getLang(), "data/html/admin/olympiad.htm");
      if (Olympiad.getInstance().isOlympiadEnd()) {
         html.replace("%endDate%", "<font color=\"b02e31\">Olympiad End!</font>");
         html.replace(
            "%validDate%",
            "<font color=\"LEVEL\">" + new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(new Date(Olympiad.getInstance().getValidationEndDate())) + "</font>"
         );
      } else {
         html.replace(
            "%endDate%",
            "<font color=\"LEVEL\">" + new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(new Date(Olympiad.getInstance().getOlympiadEndDate())) + "</font>"
         );
         html.replace("%validDate%", "<font color=\"b02e31\">Olympiad in Progress!</font>");
      }

      activeChar.sendPacket(html);
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }

   protected class RefreshMenu implements Runnable {
      Player _player;

      private RefreshMenu(Player player) {
         this._player = player;
      }

      @Override
      public void run() {
         OlympiadMenu.this.showMenu(this._player);
      }
   }
}
