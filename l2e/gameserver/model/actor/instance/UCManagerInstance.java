package l2e.gameserver.model.actor.instance;

import java.util.Calendar;
import java.util.StringTokenizer;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.TeleLocationParser;
import l2e.gameserver.instancemanager.UndergroundColiseumManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.TeleportTemplate;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.entity.underground_coliseum.UCArena;
import l2e.gameserver.model.entity.underground_coliseum.UCBestTeam;
import l2e.gameserver.model.entity.underground_coliseum.UCWaiting;
import l2e.gameserver.model.service.BotFunctions;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class UCManagerInstance extends Npc {
   public UCManagerInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.UCManagerInstance);
   }

   @Override
   public String getHtmlPath(int npcId, int val) {
      String pom = "";
      if (val == 0) {
         pom = "" + npcId;
      } else {
         pom = npcId + "-" + val;
      }

      return "data/html/underground_coliseum/" + pom + ".htm";
   }

   @Override
   public void onBypassFeedback(Player player, String command) {
      NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
      StringTokenizer token = new StringTokenizer(command, " ");
      String actualCommand = token.nextToken();
      if (actualCommand.equalsIgnoreCase("register")) {
         try {
            if (!player.isInParty()) {
               html.setFile(player, player.getLang(), "data/html/underground_coliseum/noTeam.htm");
               player.sendPacket(html);
               return;
            }

            if (!UndergroundColiseumManager.getInstance().isStarted()) {
               html.setFile(player, player.getLang(), "data/html/underground_coliseum/notStarted.htm");
               player.sendPacket(html);
               return;
            }

            if (!player.getParty().isLeader(player)) {
               html.setFile(player, player.getLang(), "data/html/underground_coliseum/notPartyLeader.htm");
               player.sendPacket(html);
               return;
            }

            if (player.getParty().getUCState() instanceof UCWaiting) {
               html.setFile(player, player.getLang(), "data/html/underground_coliseum/alreadyRegistered.htm");
               player.sendPacket(html);
               return;
            }

            int val = Integer.parseInt(token.nextToken());
            UCArena arena = UndergroundColiseumManager.getInstance().getArena(val);
            if (arena == null) {
               player.sendMessage("This arena is temporarly unavailable.");
               return;
            }

            if ((arena.getTeams()[0].getParty() != null || arena.getTeams()[1].getParty() != null)
               && (arena.getTeams()[0].getParty() == player.getParty() || arena.getTeams()[1].getParty() == player.getParty())) {
               html.setFile(player, player.getLang(), "data/html/underground_coliseum/alreadyRegistered.htm");
               player.sendPacket(html);
               return;
            }

            int realCount = 0;

            for(Player member : player.getParty().getMembers()) {
               if (member != null) {
                  if (member.getClassId().level() < 2) {
                     NpcHtmlMessage packet = new NpcHtmlMessage(this.getObjectId());
                     packet.setFile(member, member.getLang(), "data/html/underground_coliseum/wrongLevel.htm");
                     packet.replace("%name%", member.getName());
                     player.sendPacket(packet);
                     return;
                  }

                  if (member.getLevel() < arena.getMinLevel() || member.getLevel() > arena.getMaxLevel()) {
                     NpcHtmlMessage packet = new NpcHtmlMessage(this.getObjectId());
                     packet.setFile(member, member.getLang(), "data/html/underground_coliseum/wrongLevel.htm");
                     packet.replace("%name%", member.getName());
                     player.sendPacket(packet);
                     return;
                  }

                  ++realCount;
               }
            }

            if (realCount < Config.UC_PARTY_LIMIT) {
               NpcHtmlMessage packet = new NpcHtmlMessage(this.getObjectId());
               packet.setFile(player, player.getLang(), "data/html/underground_coliseum/notEnoughMembers.htm");
               player.sendPacket(packet);
               return;
            }

            if (arena.getWaitingList().size() >= 5) {
               NpcHtmlMessage packet = new NpcHtmlMessage(this.getObjectId());
               packet.setFile(player, player.getLang(), "data/html/underground_coliseum/arenaFull.htm");
               player.sendPacket(packet);
               return;
            }

            UCWaiting waiting = new UCWaiting(player.getParty(), arena);
            arena.getWaitingList().add(waiting);
            waiting.setParty(true);
            waiting.hasRegisterdNow();
            html.setFile(player, player.getLang(), "data/html/underground_coliseum/registered.htm");
            player.sendPacket(html);
            if (arena.getWaitingList().size() >= 2 && !arena.isBattleNow()) {
               arena.runTaskNow();
            }
         } catch (Exception var16) {
            var16.printStackTrace();
         }
      } else if (actualCommand.equalsIgnoreCase("cancel")) {
         if (player.getParty() == null || player.getParty() != null && !player.getParty().isLeader(player)) {
            return;
         }

         if (player.getParty().getUCState() instanceof UCWaiting) {
            NpcHtmlMessage packet = new NpcHtmlMessage(this.getObjectId());
            UCWaiting waiting = (UCWaiting)player.getParty().getUCState();
            waiting.setParty(false);
            waiting.clean();
            waiting.getBaseArena().getWaitingList().remove(waiting);
            packet.setFile(player, player.getLang(), "data/html/underground_coliseum/registrantionCanceled.htm");
            player.sendPacket(packet);
            return;
         }
      } else if (actualCommand.equalsIgnoreCase("bestTeam")) {
         int val = Integer.parseInt(token.nextToken());
         UCArena arena = UndergroundColiseumManager.getInstance().getArena(val);
         UCBestTeam bestTeam = UndergroundColiseumManager.getInstance().getBestTeam(arena.getId());
         if (bestTeam != null) {
            NpcHtmlMessage packet = new NpcHtmlMessage(this.getObjectId());
            packet.setFile(player, player.getLang(), "data/html/underground_coliseum/bestTeam.htm");
            packet.replace("%name%", bestTeam.getLeaderName());
            packet.replace("%best%", String.valueOf(bestTeam.getWins()));
            player.sendPacket(packet);
         } else {
            NpcHtmlMessage packet = new NpcHtmlMessage(this.getObjectId());
            packet.setFile(player, player.getLang(), "data/html/underground_coliseum/view-most-wins.htm");
            player.sendPacket(packet);
         }
      } else if (actualCommand.equalsIgnoreCase("listTeams")) {
         int val = Integer.parseInt(token.nextToken());
         UCArena arena = UndergroundColiseumManager.getInstance().getArena(val);
         if (arena == null) {
            player.sendMessage("This arena is temporarly unavailable.");
            return;
         }

         NpcHtmlMessage packet = new NpcHtmlMessage(this.getObjectId());
         packet.setFile(player, player.getLang(), "data/html/underground_coliseum/view-participating-teams.htm");
         String list = "";
         int i = 0;
         int currentReg = arena.getWaitingList().size();

         for(int var32 = 1; var32 <= 5; ++var32) {
            if (var32 > currentReg) {
               list = list + var32 + ". (Participating Team: Team)<br>";
            } else {
               Party party = arena.getWaitingList().get(var32 - 1).getParty();
               if (party == null) {
                  list = list + var32 + ". (Participating Team: Team)<br>";
               } else {
                  String teamList = "";

                  for(Player m : party.getMembers()) {
                     if (m != null) {
                        teamList = teamList + m.getName() + ";";
                     }
                  }

                  list = list + var32 + ". (Participating Team: <font color=00ffff>" + teamList + "</font>)<br>";
               }
            }
         }

         packet.replace("%list%", list);
         player.sendPacket(packet);
      } else {
         if (actualCommand.equalsIgnoreCase("goto")) {
            if (token.countTokens() <= 0) {
               return;
            }

            int whereTo = Integer.parseInt(token.nextToken());
            this.doTeleport(player, whereTo);
            return;
         }

         super.onBypassFeedback(player, command);
      }
   }

   private void doTeleport(Player player, int val) {
      TeleportTemplate list = TeleLocationParser.getInstance().getTemplate(val);
      if (list != null) {
         if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_GK && player.getKarma() > 0) {
            player.sendMessage("Go away, you're not welcome here.");
            return;
         }

         if (player.isCombatFlagEquipped()) {
            player.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_WHILE_IN_POSSESSION_OF_A_WARD);
            return;
         }

         if (player.isAlikeDead()) {
            return;
         }

         Calendar cal = Calendar.getInstance();
         int price = list.getPrice();
         if (player.getLevel() < 41) {
            price = 0;
         } else if (!list.getIsForNoble() && cal.get(11) >= 20 && cal.get(11) <= 23 && (cal.get(7) == 1 || cal.get(7) == 7)) {
            price /= 2;
         }

         if (Config.ALT_GAME_FREE_TELEPORT || player.destroyItemByItemId("Teleport", list.getId(), (long)price, this, true)) {
            if (BotFunctions.getInstance().isAutoTpGotoEnable(player)) {
               BotFunctions.getInstance().getAutoGotoTeleport(player, player.getLocation(), new Location(list.getLocX(), list.getLocY(), list.getLocZ()));
               return;
            }

            player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ(), true);
         }
      } else {
         _log.warning("No teleport destination with id:" + val);
      }

      player.sendActionFailed();
   }
}
