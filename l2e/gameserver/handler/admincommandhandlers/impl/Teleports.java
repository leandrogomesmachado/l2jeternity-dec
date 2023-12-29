package l2e.gameserver.handler.admincommandhandlers.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import l2e.commons.util.StringUtil;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.holder.SpawnHolder;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.instancemanager.MapRegionManager;
import l2e.gameserver.instancemanager.RaidBossSpawnManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.GrandBossInstance;
import l2e.gameserver.model.actor.instance.RaidBossInstance;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.entity.events.cleft.AerialCleftEvent;
import l2e.gameserver.model.holders.SummonRequestHolder;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ConfirmDlg;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class Teleports implements IAdminCommandHandler {
   private static final Logger _log = Logger.getLogger(Teleports.class.getName());
   private static final String[] ADMIN_COMMANDS = new String[]{
      "admin_teleport",
      "admin_show_moves",
      "admin_show_moves_other",
      "admin_show_teleport",
      "admin_teleport_to_character",
      "admin_teleportto",
      "admin_move_to",
      "admin_teleport_character",
      "admin_recall",
      "admin_recall_players",
      "admin_walk",
      "teleportto",
      "recall",
      "admin_recall_npc",
      "admin_gonorth",
      "admin_gosouth",
      "admin_goeast",
      "admin_gowest",
      "admin_goup",
      "admin_godown",
      "admin_tele",
      "admin_teleto",
      "admin_instant_move",
      "admin_sendhome",
      "admin_tonpc"
   };

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      NpcHtmlMessage adminhtm = new NpcHtmlMessage(5);
      if (command.equals("admin_teleto")) {
         activeChar.setTeleMode(1);
      }

      if (command.equals("admin_instant_move")) {
         activeChar.sendMessage("Instant move ready. Click where you want to go.");
         activeChar.setTeleMode(1);
      }

      if (command.equals("admin_teleto r")) {
         activeChar.setTeleMode(2);
      }

      if (command.equals("admin_teleto end")) {
         activeChar.setTeleMode(0);
      }

      if (command.equals("admin_show_moves")) {
         adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/teleports.htm");
         activeChar.sendPacket(adminhtm);
      }

      if (command.equals("admin_show_moves_other")) {
         adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/telepots/other.htm");
         activeChar.sendPacket(adminhtm);
      } else if (command.equals("admin_show_teleport")) {
         this.showTeleportCharWindow(activeChar);
      } else if (command.equals("admin_recall_npc")) {
         this.recallNPC(activeChar);
      } else if (command.equals("admin_teleport_to_character")) {
         this.teleportToCharacter(activeChar, activeChar.getTarget());
      } else if (command.startsWith("admin_tonpc")) {
         String val = command.substring(12);
         StringTokenizer st = new StringTokenizer(val);
         int npcId = 0;

         try {
            npcId = Integer.parseInt(st.nextToken());
         } catch (Exception var21) {
         }

         if (npcId == 0) {
            activeChar.sendMessage("USAGE: //tonpc npcId");
            return false;
         }

         Npc npc = null;

         try {
            if ((npc = World.getInstance().getNpcById(npcId)) != null) {
               this.teleportToNpc(activeChar, npc);
               return true;
            }
         } catch (Exception var20) {
         }

         activeChar.sendMessage("Npc with id: " + npcId + " not found!");
      } else if (command.startsWith("admin_walk")) {
         try {
            String val = command.substring(11);
            StringTokenizer st = new StringTokenizer(val);
            int x = Integer.parseInt(st.nextToken());
            int y = Integer.parseInt(st.nextToken());
            int z = Integer.parseInt(st.nextToken());
            activeChar.getAI().setIntention(CtrlIntention.MOVING, new Location(x, y, z, 0));
         } catch (Exception var22) {
            if (Config.DEBUG) {
               _log.info("admin_walk: " + var22);
            }
         }
      } else if (command.startsWith("admin_move_to")) {
         try {
            String val = command.substring(14);
            this.teleportTo(activeChar, val);
         } catch (StringIndexOutOfBoundsException var18) {
            adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/teleports.htm");
            activeChar.sendPacket(adminhtm);
         } catch (NumberFormatException var19) {
            activeChar.sendMessage("Usage: //move_to <x> <y> <z>");
            adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/teleports.htm");
            activeChar.sendPacket(adminhtm);
         }
      } else if (command.startsWith("admin_teleport_character")) {
         try {
            String val = command.substring(25);
            this.teleportCharacter(activeChar, val);
         } catch (StringIndexOutOfBoundsException var17) {
            activeChar.sendMessage("Wrong or no Coordinates given.");
            this.showTeleportCharWindow(activeChar);
         }
      } else if (command.startsWith("admin_teleportto ")) {
         try {
            String targetName = command.substring(17);
            Player player = World.getInstance().getPlayer(targetName);
            this.teleportToCharacter(activeChar, player);
         } catch (StringIndexOutOfBoundsException var16) {
         }
      } else if (command.startsWith("admin_recall ")) {
         try {
            String[] param = command.split(" ");
            if (param.length != 2) {
               activeChar.sendMessage("Usage: //recall <playername>");
               return false;
            }

            String targetName = param[1];
            Player player = World.getInstance().getPlayer(targetName);
            if (player != null) {
               teleportCharacter(player, activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar);
            } else {
               this.changeCharacterPosition(activeChar, targetName);
            }
         } catch (StringIndexOutOfBoundsException var15) {
         }
      } else if (command.startsWith("admin_recall_players")) {
         for(Player player : World.getInstance().getAllPlayers()) {
            if (player != null
               && player.getUCState() <= 0
               && !player.isRegisteredInFightEvent()
               && !player.isInFightEvent()
               && !player.isBlocked()
               && !player.isCursedWeaponEquipped()
               && !player.isInDuel()
               && !player.isFlying()
               && !player.isJailed()
               && !player.isInOlympiadMode()
               && !player.inObserverMode()
               && !player.isAlikeDead()
               && !player.isInSiege()
               && !player.isFestivalParticipant()
               && (
                  !AerialCleftEvent.getInstance().isStarted() && !AerialCleftEvent.getInstance().isRewarding()
                     || !AerialCleftEvent.getInstance().isPlayerParticipant(player.getObjectId())
               )
               && !Util.checkIfInRange(1000, activeChar, player, false)) {
               player.addScript(new SummonRequestHolder(activeChar, null, true));
               ConfirmDlg confirm = new ConfirmDlg(SystemMessageId.C1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT.getId());
               confirm.addCharName(activeChar);
               confirm.addZoneName(activeChar.getX(), activeChar.getY(), activeChar.getZ());
               confirm.addTime(15000);
               confirm.addRequesterId(activeChar.getObjectId());
               player.sendPacket(confirm);
            }
         }
      } else if (command.equals("admin_tele")) {
         this.showTeleportWindow(activeChar);
      } else if (command.startsWith("admin_teleport")) {
         String x = null;
         String y = null;
         String z = null;
         StringTokenizer st = new StringTokenizer(command, " ");
         st.nextToken();

         try {
            x = st.nextToken();
         } catch (Exception var14) {
         }

         try {
            y = st.nextToken();
         } catch (Exception var13) {
         }

         try {
            z = st.nextToken();
         } catch (Exception var12) {
         }

         if (x != null && y != null) {
            int z_loc = z != null ? Integer.parseInt(z) : 32767;
            Location loc = Location.parseLoc(x + " " + y + " " + z_loc);
            if (loc != null) {
               loc.correctGeoZ(activeChar.getGeoIndex());
               activeChar.getAI().setIntention(CtrlIntention.IDLE);
               activeChar.teleToLocation(loc, false);
               activeChar.sendMessage("You have been teleported to " + loc.getX() + " " + loc.getY() + " " + loc.getZ());
            }
         }
      } else if (command.startsWith("admin_go")) {
         int intVal = 150;
         int x = activeChar.getX();
         int y = activeChar.getY();
         int z = activeChar.getZ();

         try {
            String val = command.substring(8);
            StringTokenizer st = new StringTokenizer(val);
            String dir = st.nextToken();
            if (st.hasMoreTokens()) {
               intVal = Integer.parseInt(st.nextToken());
            }

            if (dir.equals("east")) {
               x += intVal;
            } else if (dir.equals("west")) {
               x -= intVal;
            } else if (dir.equals("north")) {
               y -= intVal;
            } else if (dir.equals("south")) {
               y += intVal;
            } else if (dir.equals("up")) {
               z += intVal;
            } else if (dir.equals("down")) {
               z -= intVal;
            }

            activeChar.teleToLocation(x, y, z, false);
            this.showTeleportWindow(activeChar);
         } catch (Exception var11) {
            activeChar.sendMessage("Usage: //go<north|south|east|west|up|down> [offset] (default 150)");
         }
      } else if (command.startsWith("admin_sendhome")) {
         StringTokenizer st = new StringTokenizer(command, " ");
         st.nextToken();
         if (st.countTokens() > 1) {
            activeChar.sendMessage("Usage: //sendhome <playername>");
         } else if (st.countTokens() == 1) {
            String name = st.nextToken();
            Player player = World.getInstance().getPlayer(name);
            if (player == null) {
               activeChar.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
               return false;
            }

            this.teleportHome(player);
         } else {
            GameObject target = activeChar.getTarget();
            if (target instanceof Player) {
               this.teleportHome(target.getActingPlayer());
            } else {
               activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
            }
         }
      }

      return true;
   }

   private void teleportHome(Player player) {
      String regionName;
      switch(player.getRace()) {
         case Elf:
            regionName = "elf_town";
            break;
         case DarkElf:
            regionName = "darkelf_town";
            break;
         case Orc:
            regionName = "orc_town";
            break;
         case Dwarf:
            regionName = "dwarf_town";
            break;
         case Kamael:
            regionName = "kamael_town";
            break;
         case Human:
         default:
            regionName = "talking_island_town";
      }

      player.teleToLocation(MapRegionManager.getInstance().getMapRegionByName(regionName).getSpawnLoc(), true);
      player.setReflectionId(0);
      player.setIsIn7sDungeon(false);
   }

   private void teleportTo(Player activeChar, String Coords) {
      try {
         StringTokenizer st = new StringTokenizer(Coords);
         String x1 = st.nextToken();
         int x = Integer.parseInt(x1);
         String y1 = st.nextToken();
         int y = Integer.parseInt(y1);
         String z1 = st.nextToken();
         int z = Integer.parseInt(z1);
         activeChar.getAI().setIntention(CtrlIntention.IDLE);
         activeChar.teleToLocation(x, y, z, false);
         activeChar.sendMessage("You have been teleported to " + Coords);
      } catch (NoSuchElementException var10) {
         activeChar.sendMessage("Wrong or no Coordinates given.");
      }
   }

   private void showTeleportWindow(Player activeChar) {
      NpcHtmlMessage adminhtm = new NpcHtmlMessage(5);
      adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/move.htm");
      activeChar.sendPacket(adminhtm);
   }

   private void showTeleportCharWindow(Player activeChar) {
      GameObject target = activeChar.getTarget();
      Player player = null;
      if (target instanceof Player) {
         player = (Player)target;
         NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
         String replyMSG = StringUtil.concat(
            "<html><title>Teleport Character</title><body>The character you will teleport is ",
            player.getName(),
            ".<br>Co-ordinate x<edit var=\"char_cord_x\" width=110>Co-ordinate y<edit var=\"char_cord_y\" width=110>Co-ordinate z<edit var=\"char_cord_z\" width=110><button value=\"Teleport\" action=\"bypass -h admin_teleport_character $char_cord_x $char_cord_y $char_cord_z\" width=60 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><button value=\"Teleport near you\" action=\"bypass -h admin_teleport_character ",
            String.valueOf(activeChar.getX()),
            " ",
            String.valueOf(activeChar.getY()),
            " ",
            String.valueOf(activeChar.getZ()),
            "\" width=115 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><center><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></body></html>"
         );
         adminReply.setHtml(activeChar, replyMSG);
         activeChar.sendPacket(adminReply);
      } else {
         activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
      }
   }

   private void teleportCharacter(Player activeChar, String Cords) {
      GameObject target = activeChar.getTarget();
      Player player = null;
      if (target instanceof Player) {
         player = (Player)target;
         if (player.getObjectId() == activeChar.getObjectId()) {
            player.sendPacket(SystemMessageId.CANNOT_USE_ON_YOURSELF);
         } else {
            try {
               StringTokenizer st = new StringTokenizer(Cords);
               String x1 = st.nextToken();
               int x = Integer.parseInt(x1);
               String y1 = st.nextToken();
               int y = Integer.parseInt(y1);
               String z1 = st.nextToken();
               int z = Integer.parseInt(z1);
               teleportCharacter(player, x, y, z, null);
            } catch (NoSuchElementException var12) {
            }
         }
      } else {
         activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
      }
   }

   public static void teleportCharacter(Player player, int x, int y, int z, Player activeChar) {
      if (player != null) {
         if (player.isJailed()) {
            activeChar.sendMessage("Sorry, player " + player.getName() + " is in Jail.");
         } else {
            if (activeChar != null && activeChar.getReflectionId() >= 0) {
               player.setReflectionId(activeChar.getReflectionId());
               activeChar.sendMessage("You have recalled " + player.getName());
            } else {
               player.setReflectionId(0);
            }

            player.sendMessage("Admin is teleporting you.");
            player.getAI().setIntention(CtrlIntention.IDLE);
            player.teleToLocation(x, y, z, true);
         }
      }
   }

   private void teleportToCharacter(Player activeChar, GameObject target) {
      if (target == null) {
         activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
      } else {
         Player player = null;
         if (target instanceof Player) {
            player = (Player)target;
            if (player.getObjectId() == activeChar.getObjectId()) {
               player.sendPacket(SystemMessageId.CANNOT_USE_ON_YOURSELF);
            } else {
               activeChar.setReflectionId(target.getReflectionId());
               int x = player.getX();
               int y = player.getY();
               int z = player.getZ();
               activeChar.getAI().setIntention(CtrlIntention.IDLE);
               activeChar.teleToLocation(x, y, z, true);
               activeChar.sendMessage("You have teleported to " + player.getName() + ".");
            }
         } else {
            activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
         }
      }
   }

   private void teleportToNpc(Player activeChar, Npc npc) {
      if (npc == null) {
         activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
      } else {
         activeChar.setReflectionId(npc.getReflectionId());
         int x = npc.getX();
         int y = npc.getY();
         int z = npc.getZ();
         activeChar.getAI().setIntention(CtrlIntention.IDLE);
         activeChar.teleToLocation(x, y, z, true);
         activeChar.sendMessage("You have teleported to " + npc.getName() + ".");
      }
   }

   private void changeCharacterPosition(Player activeChar, String name) {
      int x = activeChar.getX();
      int y = activeChar.getY();
      int z = activeChar.getZ();

      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("UPDATE characters SET x=?, y=?, z=? WHERE char_name=?");
         statement.setInt(1, x);
         statement.setInt(2, y);
         statement.setInt(3, z);
         statement.setString(4, name);
         statement.execute();
         int count = statement.getUpdateCount();
         statement.close();
         if (count == 0) {
            activeChar.sendMessage("Character not found or position unaltered.");
         } else {
            activeChar.sendMessage("Player's [" + name + "] position is now set to (" + x + "," + y + "," + z + ").");
         }
      } catch (SQLException var20) {
         activeChar.sendMessage("SQLException while changing offline character's position");
      }
   }

   private void recallNPC(Player activeChar) {
      GameObject obj = activeChar.getTarget();
      if (obj instanceof Npc && !((Npc)obj).isMinion() && !(obj instanceof RaidBossInstance) && !(obj instanceof GrandBossInstance)) {
         Npc target = (Npc)obj;
         int monsterTemplate = target.getTemplate().getId();
         NpcTemplate template1 = NpcsParser.getInstance().getTemplate(monsterTemplate);
         if (template1 == null) {
            activeChar.sendMessage("Incorrect monster template.");
            _log.warning("ERROR: NPC " + target.getObjectId() + " has a 'null' template.");
            return;
         }

         Spawner spawn = target.getSpawn();
         if (spawn == null) {
            activeChar.sendMessage("Incorrect monster spawn.");
            _log.warning("ERROR: NPC " + target.getObjectId() + " has a 'null' spawn.");
            return;
         }

         int respawnTime = spawn.getRespawnDelay() / 1000;
         target.deleteMe();
         spawn.stopRespawn();
         SpawnHolder.getInstance().deleteSpawn(spawn, true);

         try {
            spawn = new Spawner(template1);
            if (Config.SAVE_GMSPAWN_ON_CUSTOM) {
               spawn.setCustom(true);
            }

            spawn.setX(activeChar.getX());
            spawn.setY(activeChar.getY());
            spawn.setZ(activeChar.getZ());
            spawn.setAmount(1);
            spawn.setHeading(activeChar.getHeading());
            spawn.setRespawnDelay(respawnTime);
            if (activeChar.getReflectionId() >= 0) {
               spawn.setReflectionId(activeChar.getReflectionId());
            } else {
               spawn.setReflectionId(0);
            }

            SpawnHolder.getInstance().addNewSpawn(spawn, true);
            spawn.init();
            activeChar.sendMessage("Created " + template1.getName() + " on " + target.getObjectId() + ".");
            if (Config.DEBUG) {
               _log.fine("Spawn at X=" + spawn.getX() + " Y=" + spawn.getY() + " Z=" + spawn.getZ());
               _log.warning("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") moved NPC " + target.getObjectId());
            }
         } catch (Exception var10) {
            activeChar.sendMessage("Target is not in game.");
         }
      } else if (obj instanceof RaidBossInstance) {
         RaidBossInstance target = (RaidBossInstance)obj;
         Spawner spawn = target.getSpawn();
         if (spawn == null) {
            activeChar.sendMessage("Incorrect raid spawn.");
            _log.warning("ERROR: NPC Id" + target.getId() + " has a 'null' spawn.");
            return;
         }

         target.deleteMe();
         RaidBossSpawnManager.getInstance().deleteSpawn(spawn, true);

         try {
            NpcTemplate template = NpcsParser.getInstance().getTemplate(target.getId());
            Spawner spawnDat = new Spawner(template);
            if (Config.SAVE_GMSPAWN_ON_CUSTOM) {
               spawn.setCustom(true);
            }

            spawnDat.setX(activeChar.getX());
            spawnDat.setY(activeChar.getY());
            spawnDat.setZ(activeChar.getZ());
            spawnDat.setAmount(1);
            spawnDat.setHeading(activeChar.getHeading());
            spawnDat.setRespawnMinDelay(43200);
            spawnDat.setRespawnMaxDelay(129600);
            RaidBossSpawnManager.getInstance().addNewSpawn(spawnDat, true);
         } catch (Exception var9) {
            activeChar.sendPacket(SystemMessageId.TARGET_CANT_FOUND);
         }
      } else {
         activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
      }
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }
}
