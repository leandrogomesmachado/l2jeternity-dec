package l2e.gameserver.handler.admincommandhandlers.impl;

import l2e.fake.FakePlayer;
import l2e.fake.FakePlayerManager;
import l2e.fake.ai.EnchanterAI;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.FakeLocationParser;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class FakePlayers implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{
      "admin_takecontrol", "admin_releasecontrol", "admin_fakes", "admin_spawnrandom", "admin_deletefake", "admin_spawnenchanter", "admin_autofakes"
   };

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }

   private void fakeMenu(Player activeChar) {
      NpcHtmlMessage html = new NpcHtmlMessage(0);
      html.setFile(activeChar, activeChar.getLang(), "data/html/admin/fakeplayers/index.htm");
      html.replace("%fakecount%", (long)FakePlayerManager.getInstance().getFakePlayers().size());
      activeChar.sendPacket(html);
   }

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      if (command.startsWith("admin_fakes")) {
         this.fakeMenu(activeChar);
      } else {
         if (command.startsWith("admin_deletefake")) {
            if (activeChar.getTarget() != null && activeChar.getTarget() instanceof FakePlayer) {
               FakePlayer fakePlayer = (FakePlayer)activeChar.getTarget();
               if (FakePlayerManager.getInstance().removeFakePlayers(fakePlayer)) {
                  fakePlayer.despawnPlayer();
               }
            }

            return true;
         }

         if (command.startsWith("admin_autofakes")) {
            FakePlayerManager.getInstance().autoSpawnPlayer(20);
            if (command.contains(" ")) {
               String arg = command.split(" ")[1];
               if (arg.equalsIgnoreCase("htm")) {
                  this.fakeMenu(activeChar);
               }
            }
         } else if (command.startsWith("admin_spawnenchanter")) {
            if (FakePlayerManager.getInstance().getFakePlayers().size() >= Config.FAKE_PLAYERS_AMOUNT) {
               activeChar.sendMessage("You reach limit fake players!");
               this.fakeMenu(activeChar);
               return false;
            }

            FakePlayer fakePlayer = FakePlayerManager.getInstance()
               .spawnRndPlayer(FakeLocationParser.getInstance().createRndLoc(Location.findPointToStay(activeChar, activeChar.getLocation(), 0, 60, true)));
            if (fakePlayer != null) {
               fakePlayer.setFakeAi(new EnchanterAI(fakePlayer));
               FakePlayerManager.getInstance().addFakePlayers(fakePlayer);
            }

            this.fakeMenu(activeChar);
         } else {
            if (command.startsWith("admin_spawnrandom")) {
               if (FakePlayerManager.getInstance().getFakePlayers().size() >= Config.FAKE_PLAYERS_AMOUNT) {
                  activeChar.sendMessage("You reach limit fake players!");
                  this.fakeMenu(activeChar);
                  return false;
               }

               FakePlayer fakePlayer = FakePlayerManager.getInstance()
                  .spawnRndPlayer(FakeLocationParser.getInstance().createRndLoc(Location.findPointToStay(activeChar, activeChar.getLocation(), 0, 60, true)));
               if (fakePlayer != null) {
                  fakePlayer.assignDefaultAI();
                  FakePlayerManager.getInstance().addFakePlayers(fakePlayer);
                  if (command.contains(" ")) {
                     String arg = command.split(" ")[1];
                     if (arg.equalsIgnoreCase("htm")) {
                        this.fakeMenu(activeChar);
                     }
                  }
               }

               return true;
            }

            if (command.startsWith("admin_takecontrol")) {
               if (activeChar.getTarget() != null && activeChar.getTarget() instanceof FakePlayer) {
                  FakePlayer fakePlayer = (FakePlayer)activeChar.getTarget();
                  fakePlayer.setUnderControl(true);
                  activeChar.setPlayerUnderControl(fakePlayer);
                  activeChar.sendMessage("You are now controlling: " + fakePlayer.getName());
                  return true;
               }

               activeChar.sendMessage("You can only take control of a Fake Player");
            } else if (command.startsWith("admin_releasecontrol")) {
               if (activeChar.isControllingFakePlayer()) {
                  FakePlayer fakePlayer = activeChar.getPlayerUnderControl();
                  activeChar.sendMessage("You are no longer controlling: " + fakePlayer.getName());
                  fakePlayer.setUnderControl(false);
                  activeChar.setPlayerUnderControl(null);
                  return true;
               }

               activeChar.sendMessage("You are not controlling a Fake Player");
            }
         }
      }

      return true;
   }
}
