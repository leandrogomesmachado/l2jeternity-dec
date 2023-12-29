package l2e.gameserver.handler.admincommandhandlers.impl;

import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import l2e.commons.util.Broadcast;
import l2e.commons.util.StringUtil;
import l2e.gameserver.Config;
import l2e.gameserver.SevenSigns;
import l2e.gameserver.data.holder.SpawnHolder;
import l2e.gameserver.data.parser.AdminParser;
import l2e.gameserver.data.parser.ColosseumFenceParser;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.data.parser.SpawnParser;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.instancemanager.DayNightSpawnManager;
import l2e.gameserver.instancemanager.MapRegionManager;
import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.instancemanager.RaidBossSpawnManager;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.AutoSpawnHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.ColosseumFence;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.entity.Reflection;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class Spawn implements IAdminCommandHandler {
   private static final Logger _log = Logger.getLogger(Spawn.class.getName());
   private static final String[] ADMIN_COMMANDS = new String[]{
      "admin_show_spawns",
      "admin_spawn",
      "admin_spawn_fence",
      "admin_delete_fence",
      "admin_list_fence",
      "admin_spawn_monster",
      "admin_spawn_index",
      "admin_unspawnall",
      "admin_respawnall",
      "admin_spawn_reload",
      "admin_npc_index",
      "admin_spawn_once",
      "admin_show_npcs",
      "admin_spawnnight",
      "admin_spawnday",
      "admin_instance_spawns",
      "admin_list_spawns",
      "admin_list_positions",
      "admin_spawn_debug_menu",
      "admin_spawn_debug_print",
      "admin_spawn_debug_print_menu",
      "admin_dumpspawn"
   };

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      NpcHtmlMessage adminhtm = new NpcHtmlMessage(5);
      if (command.equals("admin_show_spawns")) {
         adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/spawns.htm");
         activeChar.sendPacket(adminhtm);
      } else if (command.equalsIgnoreCase("admin_spawn_debug_menu")) {
         adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/spawns_debug.htm");
         activeChar.sendPacket(adminhtm);
      } else if (command.startsWith("admin_spawn_debug_print")) {
         StringTokenizer st = new StringTokenizer(command, " ");
         GameObject target = activeChar.getTarget();
         if (target instanceof Npc) {
            try {
               st.nextToken();
               int type = Integer.parseInt(st.nextToken());
               this.printSpawn((Npc)target, type);
               if (command.contains("_menu")) {
                  adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/spawns_debug.htm");
                  activeChar.sendPacket(adminhtm);
               }
            } catch (Exception var22) {
            }
         } else {
            activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
         }
      } else if (command.startsWith("admin_spawn_index")) {
         StringTokenizer st = new StringTokenizer(command, " ");

         try {
            st.nextToken();
            int level = Integer.parseInt(st.nextToken());
            int from = 0;

            try {
               from = Integer.parseInt(st.nextToken());
            } catch (NoSuchElementException var20) {
            }

            this.showMonsters(activeChar, level, from);
         } catch (Exception var21) {
            adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/spawns.htm");
            activeChar.sendPacket(adminhtm);
         }
      } else if (command.equals("admin_show_npcs")) {
         adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/npcs.htm");
         activeChar.sendPacket(adminhtm);
      } else if (command.startsWith("admin_npc_index")) {
         StringTokenizer st = new StringTokenizer(command, " ");

         try {
            st.nextToken();
            String letter = st.nextToken();
            int from = 0;

            try {
               from = Integer.parseInt(st.nextToken());
            } catch (NoSuchElementException var18) {
            }

            this.showNpcs(activeChar, letter, from);
         } catch (Exception var19) {
            adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/npcs.htm");
            activeChar.sendPacket(adminhtm);
         }
      } else if (command.startsWith("admin_instance_spawns")) {
         StringTokenizer st = new StringTokenizer(command, " ");

         try {
            st.nextToken();
            int instance = Integer.parseInt(st.nextToken());
            if (instance >= 300000) {
               StringBuilder html = StringUtil.startAppend(
                  1500,
                  "<html><table width=\"100%\"><tr><td width=45><button value=\"Main\" action=\"bypass -h admin_admin\" width=45 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td><td width=180><center>",
                  "<font color=\"LEVEL\">Spawns for " + String.valueOf(instance) + "</font>",
                  "</td><td width=45><button value=\"Back\" action=\"bypass -h admin_current_player\" width=45 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table><br>",
                  "<table width=\"100%\"><tr><td width=200>NpcName</td><td width=70>Action</td></tr>"
               );
               int counter = 0;
               int skiped = 0;
               Reflection inst = ReflectionManager.getInstance().getReflection(instance);
               if (inst != null) {
                  for(Npc npc : inst.getNpcs()) {
                     if (!npc.isDead()) {
                        if (counter < 50) {
                           StringUtil.append(
                              html,
                              "<tr><td>" + npc.getName() + "</td><td>",
                              "<a action=\"bypass -h admin_move_to " + npc.getX() + " " + npc.getY() + " " + npc.getZ() + "\">Go</a>",
                              "</td></tr>"
                           );
                           ++counter;
                        } else {
                           ++skiped;
                        }
                     }
                  }

                  StringUtil.append(html, "<tr><td>Skipped:</td><td>" + String.valueOf(skiped) + "</td></tr></table></body></html>");
                  NpcHtmlMessage ms = new NpcHtmlMessage(1);
                  ms.setHtml(activeChar, html.toString());
                  activeChar.sendPacket(ms);
               } else {
                  activeChar.sendMessage("Cannot find instance " + instance);
               }
            } else {
               activeChar.sendMessage("Invalid instance number.");
            }
         } catch (Exception var23) {
            activeChar.sendMessage("Usage //instance_spawns <instance_number>");
         }
      } else if (command.startsWith("admin_unspawnall")) {
         Broadcast.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.NPC_SERVER_NOT_OPERATING));
         RaidBossSpawnManager.getInstance().cleanUp();
         DayNightSpawnManager.getInstance().cleanUp();
         World.getInstance().deleteVisibleNpcSpawns();
         AdminParser.getInstance().broadcastMessageToGMs("NPC Unspawn completed!");
      } else if (command.startsWith("admin_spawnday")) {
         DayNightSpawnManager.getInstance().spawnDayCreatures();
      } else if (command.startsWith("admin_spawnnight")) {
         DayNightSpawnManager.getInstance().spawnNightCreatures();
      } else if (command.startsWith("admin_respawnall") || command.startsWith("admin_spawn_reload")) {
         RaidBossSpawnManager.getInstance().cleanUp();
         DayNightSpawnManager.getInstance().cleanUp();
         World.getInstance().deleteVisibleNpcSpawns();
         NpcsParser.getInstance();
         RaidBossSpawnManager.getInstance();
         SpawnHolder.getInstance().reloadAll();
         SpawnParser.getInstance().reloadAll();
         AutoSpawnHandler.getInstance().reload();
         SevenSigns.getInstance().spawnSevenSignsNPC();
         QuestManager.getInstance().reloadAllQuests();
         AdminParser.getInstance().broadcastMessageToGMs("NPC Respawn completed!");
      } else if (command.startsWith("admin_spawn_fence")) {
         StringTokenizer st = new StringTokenizer(command, " ");

         try {
            st.nextToken();
            int type = Integer.parseInt(st.nextToken());
            int width = Integer.parseInt(st.nextToken());
            int height = Integer.parseInt(st.nextToken());
            ColosseumFenceParser.getInstance()
               .addDynamic(
                  activeChar, activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar.getZ() + 50, activeChar.getZ() - 50, width, height, type
               );
            listFences(activeChar);
         } catch (Exception var17) {
            activeChar.sendMessage("Usage: //spawn_fence <type> <width> <height>");
         }
      } else if (command.startsWith("admin_delete_fence")) {
         StringTokenizer st = new StringTokenizer(command, " ");
         st.nextToken();

         try {
            GameObject object = World.getInstance().findObject(Integer.parseInt(st.nextToken()));
            if (object instanceof ColosseumFence) {
               ColosseumFenceParser.getInstance().removeFence((ColosseumFence)object, Integer.parseInt(st.nextToken()));
               if (st.hasMoreTokens()) {
                  listFences(activeChar);
               }
            } else {
               activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
            }
         } catch (Exception var16) {
            activeChar.sendMessage("Usage: //deletefence <objectId> <regionId>");
         }
      } else if (command.startsWith("admin_list_fence")) {
         listFences(activeChar);
      } else if (command.startsWith("admin_spawn_monster") || command.startsWith("admin_spawn")) {
         StringTokenizer st = new StringTokenizer(command, " ");

         try {
            String cmd = st.nextToken();
            String id = st.nextToken();
            int respawnTime = 60;
            int mobCount = 1;
            if (st.hasMoreTokens()) {
               mobCount = Integer.parseInt(st.nextToken());
            }

            if (st.hasMoreTokens()) {
               respawnTime = Integer.parseInt(st.nextToken());
            }

            if (cmd.equalsIgnoreCase("admin_spawn_once")) {
               this.spawnMonster(activeChar, id, respawnTime, mobCount, false, true);
            } else {
               this.spawnMonster(activeChar, id, respawnTime, mobCount, true, true);
            }
         } catch (Exception var15) {
            adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/spawns.htm");
            activeChar.sendPacket(adminhtm);
         }
      } else if (command.startsWith("admin_list_spawns") || command.startsWith("admin_list_positions")) {
         int npcId = 0;
         int teleportIndex = -1;

         try {
            String[] params = command.split(" ");
            Pattern pattern = Pattern.compile("[0-9]*");
            Matcher regexp = pattern.matcher(params[1]);
            if (regexp.matches()) {
               npcId = Integer.parseInt(params[1]);
            } else {
               params[1] = params[1].replace('_', ' ');
               npcId = NpcsParser.getInstance().getTemplateByName(params[1]).getId();
            }

            if (params.length > 2) {
               teleportIndex = Integer.parseInt(params[2]);
            }
         } catch (Exception var14) {
            activeChar.sendMessage("Command format is //list_spawns <npcId|npc_name> [tele_index]");
         }

         if (command.startsWith("admin_list_positions")) {
            SpawnHolder.getInstance().findNPCInstances(activeChar, npcId, teleportIndex, true);
         } else {
            SpawnHolder.getInstance().findNPCInstances(activeChar, npcId, teleportIndex, false);
         }
      } else if (command.startsWith("admin_dumpspawn")) {
         StringTokenizer st = new StringTokenizer(command, " ");

         try {
            st.nextToken();
            String id = st.nextToken();
            int respawnTime = 60;
            int mobCount = 1;
            if (st.hasMoreTokens()) {
               mobCount = Integer.parseInt(st.nextToken());
            }

            if (st.hasMoreTokens()) {
               respawnTime = Integer.parseInt(st.nextToken());
            }

            this.spawnMonster(activeChar, id, respawnTime, mobCount, true, false);

            try {
               new File("./data/stats/npcs/spawns/dumps").mkdir();
               File f = new File("./data/stats/npcs/spawns/dumps/spawndump.txt");
               if (!f.exists()) {
                  f.createNewFile();
               }

               FileWriter writer = new FileWriter(f, true);
               writer.write(
                  "<spawn count=\""
                     + mobCount
                     + "\" respawn=\""
                     + respawnTime
                     + "\" respawn_random=\"0\" period_of_day=\"none\">\n\t<point x=\""
                     + activeChar.getX()
                     + "\" y=\""
                     + activeChar.getY()
                     + "\" z=\""
                     + activeChar.getZ()
                     + "\" h=\""
                     + activeChar.getHeading()
                     + "\" />\n\t<npc id=\""
                     + Integer.parseInt(id)
                     + "\" /><!--"
                     + NpcsParser.getInstance().getTemplate(Integer.parseInt(id)).getName()
                     + "-->\n</spawn>\n"
               );
               writer.close();
            } catch (Exception var12) {
            }
         } catch (Exception var13) {
            adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/spawns.htm");
            activeChar.sendPacket(adminhtm);
         }
      }

      return true;
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }

   private void printSpawn(Npc target, int type) {
      int i = target.getId();
      int x = target.getSpawn().getX();
      int y = target.getSpawn().getY();
      int z = target.getSpawn().getZ();
      int h = target.getSpawn().getHeading();
      switch(type) {
         case 0:
         default:
            _log.info("('',1," + i + "," + x + "," + y + "," + z + ",0,0," + h + ",60,0,0),");
            break;
         case 1:
            _log.info("<spawn npcId=\"" + i + "\" x=\"" + x + "\" y=\"" + y + "\" z=\"" + z + "\" heading=\"" + h + "\" respawn=\"0\" />");
            break;
         case 2:
            _log.info("{ " + i + ", " + x + ", " + y + ", " + z + ", " + h + " },");
      }
   }

   private void spawnMonster(Player activeChar, String monsterId, int respawnTime, int mobCount, boolean permanent, boolean saveToDb) {
      GameObject target = activeChar.getTarget();
      if (target == null) {
         target = activeChar;
      }

      NpcTemplate template1;
      if (monsterId.matches("[0-9]*")) {
         int monsterTemplate = Integer.parseInt(monsterId);
         template1 = NpcsParser.getInstance().getTemplate(monsterTemplate);
      } else {
         monsterId = monsterId.replace('_', ' ');
         template1 = NpcsParser.getInstance().getTemplateByName(monsterId);
      }

      try {
         Spawner spawn = new Spawner(template1);
         if (Config.SAVE_GMSPAWN_ON_CUSTOM) {
            spawn.setCustom(true);
         }

         spawn.setX(target.getX());
         spawn.setY(target.getY());
         spawn.setZ(target.getZ());
         spawn.setAmount(mobCount);
         spawn.setHeading(activeChar.getHeading());
         spawn.setRespawnDelay(respawnTime);
         if (activeChar.getReflectionId() > 0) {
            spawn.setReflectionId(activeChar.getReflectionId());
            permanent = false;
         } else {
            spawn.setReflectionId(0);
         }

         if (RaidBossSpawnManager.getInstance().isDefined(spawn.getId())) {
            activeChar.sendMessage("You cannot spawn another instance of " + template1.getName() + ".");
         } else {
            if (saveToDb) {
               SpawnHolder.getInstance().addNewSpawn(spawn, permanent);
            }

            spawn.init();
            if (!permanent) {
               spawn.stopRespawn();
            }

            activeChar.sendMessage("Created " + template1.getName() + " on " + target.getObjectId());
         }
      } catch (Exception var10) {
         activeChar.sendPacket(SystemMessageId.TARGET_CANT_FOUND);
      }
   }

   private void showMonsters(Player activeChar, int level, int from) {
      List<NpcTemplate> mobs = NpcsParser.getInstance().getAllMonstersOfLevel(level);
      int mobsCount = mobs.size();
      StringBuilder tb = StringUtil.startAppend(
         500 + mobsCount * 80,
         "<html><title>Spawn Monster:</title><body><p> Level : ",
         Integer.toString(level),
         "<br>Total Npc's : ",
         Integer.toString(mobsCount),
         "<br>"
      );
      int i = from;

      for(int j = 0; i < mobsCount && j < 50; ++j) {
         StringUtil.append(tb, "<a action=\"bypass -h admin_spawn_monster ", Integer.toString(mobs.get(i).getId()), "\">", mobs.get(i).getName(), "</a><br1>");
         ++i;
      }

      if (i == mobsCount) {
         tb.append(
            "<br><center><button value=\"Back\" action=\"bypass -h admin_show_spawns\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></body></html>"
         );
      } else {
         StringUtil.append(
            tb,
            "<br><center><button value=\"Next\" action=\"bypass -h admin_spawn_index ",
            Integer.toString(level),
            " ",
            Integer.toString(i),
            "\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><button value=\"Back\" action=\"bypass -h admin_show_spawns\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></body></html>"
         );
      }

      activeChar.sendPacket(new NpcHtmlMessage(activeChar, 5, tb.toString()));
   }

   private void showNpcs(Player activeChar, String starting, int from) {
      List<NpcTemplate> mobs = NpcsParser.getInstance().getAllNpcStartingWith(starting);
      int mobsCount = mobs.size();
      StringBuilder tb = StringUtil.startAppend(
         500 + mobsCount * 80,
         "<html><title>Spawn Monster:</title><body><p> There are ",
         Integer.toString(mobsCount),
         " Npcs whose name starts with ",
         starting,
         ":<br>"
      );
      int i = from;

      for(int j = 0; i < mobsCount && j < 50; ++j) {
         StringUtil.append(tb, "<a action=\"bypass -h admin_spawn_monster ", Integer.toString(mobs.get(i).getId()), "\">", mobs.get(i).getName(), "</a><br1>");
         ++i;
      }

      if (i == mobsCount) {
         tb.append(
            "<br><center><button value=\"Back\" action=\"bypass -h admin_show_npcs\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></body></html>"
         );
      } else {
         StringUtil.append(
            tb,
            "<br><center><button value=\"Next\" action=\"bypass -h admin_npc_index ",
            starting,
            " ",
            Integer.toString(i),
            "\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><button value=\"Back\" action=\"bypass -h admin_show_npcs\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></body></html>"
         );
      }

      activeChar.sendPacket(new NpcHtmlMessage(activeChar, 5, tb.toString()));
   }

   private static void listFences(Player activeChar) {
      int region = MapRegionManager.getInstance().getMapRegionLocId(activeChar.getX(), activeChar.getY());
      List<ColosseumFence> fences = ColosseumFenceParser.getInstance().getFences().get(region);
      StringBuilder sb = new StringBuilder();
      sb.append("<html><body>Total Fences: " + fences.size() + "<br><br>");

      for(ColosseumFence fence : fences) {
         sb.append(
            "<a action=\"bypass -h admin_delete_fence "
               + fence.getObjectId()
               + " "
               + region
               + " 1\">Fence: "
               + fence.getObjectId()
               + " ["
               + fence.getX()
               + " "
               + fence.getY()
               + " "
               + fence.getZ()
               + "]</a><br>"
         );
      }

      sb.append("</body></html>");
      NpcHtmlMessage html = new NpcHtmlMessage(0);
      html.setHtml(activeChar, sb.toString());
      activeChar.sendPacket(html);
   }
}
