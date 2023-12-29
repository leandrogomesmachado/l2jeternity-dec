package l2e.gameserver.handler.admincommandhandlers.impl;

import l2e.commons.util.Broadcast;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.MobGroup;
import l2e.gameserver.model.MobGroupData;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.SetupGauge;

public class MobGroups implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{
      "admin_mobmenu",
      "admin_mobgroup_list",
      "admin_mobgroup_create",
      "admin_mobgroup_remove",
      "admin_mobgroup_delete",
      "admin_mobgroup_spawn",
      "admin_mobgroup_unspawn",
      "admin_mobgroup_kill",
      "admin_mobgroup_idle",
      "admin_mobgroup_attack",
      "admin_mobgroup_rnd",
      "admin_mobgroup_return",
      "admin_mobgroup_follow",
      "admin_mobgroup_casting",
      "admin_mobgroup_nomove",
      "admin_mobgroup_attackgrp",
      "admin_mobgroup_invul"
   };

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      if (command.equals("admin_mobmenu")) {
         this.showMainPage(activeChar, command);
         return true;
      } else {
         if (command.equals("admin_mobgroup_list")) {
            this.showGroupList(activeChar);
         } else if (command.startsWith("admin_mobgroup_create")) {
            this.createGroup(command, activeChar);
         } else if (command.startsWith("admin_mobgroup_delete") || command.startsWith("admin_mobgroup_remove")) {
            this.removeGroup(command, activeChar);
         } else if (command.startsWith("admin_mobgroup_spawn")) {
            this.spawnGroup(command, activeChar);
         } else if (command.startsWith("admin_mobgroup_unspawn")) {
            this.unspawnGroup(command, activeChar);
         } else if (command.startsWith("admin_mobgroup_kill")) {
            this.killGroup(command, activeChar);
         } else if (command.startsWith("admin_mobgroup_attackgrp")) {
            this.attackGrp(command, activeChar);
         } else if (command.startsWith("admin_mobgroup_attack")) {
            if (activeChar.getTarget() instanceof Creature) {
               Creature target = (Creature)activeChar.getTarget();
               this.attack(command, activeChar, target);
            }
         } else if (command.startsWith("admin_mobgroup_rnd")) {
            this.setNormal(command, activeChar);
         } else if (command.startsWith("admin_mobgroup_idle")) {
            this.idle(command, activeChar);
         } else if (command.startsWith("admin_mobgroup_return")) {
            this.returnToChar(command, activeChar);
         } else if (command.startsWith("admin_mobgroup_follow")) {
            this.follow(command, activeChar, activeChar);
         } else if (command.startsWith("admin_mobgroup_casting")) {
            this.setCasting(command, activeChar);
         } else if (command.startsWith("admin_mobgroup_nomove")) {
            this.noMove(command, activeChar);
         } else if (command.startsWith("admin_mobgroup_invul")) {
            this.invul(command, activeChar);
         } else if (command.startsWith("admin_mobgroup_teleport")) {
            this.teleportGroup(command, activeChar);
         }

         this.showMainPage(activeChar, command);
         return true;
      }
   }

   private void showMainPage(Player activeChar, String command) {
      NpcHtmlMessage adminhtm = new NpcHtmlMessage(5);
      adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/mobgroup.htm");
      activeChar.sendPacket(adminhtm);
   }

   private void returnToChar(String command, Player activeChar) {
      int groupId;
      try {
         groupId = Integer.parseInt(command.split(" ")[1]);
      } catch (Exception var5) {
         activeChar.sendMessage("Incorrect command arguments.");
         return;
      }

      MobGroup group = MobGroupData.getInstance().getGroup(groupId);
      if (group == null) {
         activeChar.sendMessage("Invalid group specified.");
      } else {
         group.returnGroup(activeChar);
      }
   }

   private void idle(String command, Player activeChar) {
      int groupId;
      try {
         groupId = Integer.parseInt(command.split(" ")[1]);
      } catch (Exception var5) {
         activeChar.sendMessage("Incorrect command arguments.");
         return;
      }

      MobGroup group = MobGroupData.getInstance().getGroup(groupId);
      if (group == null) {
         activeChar.sendMessage("Invalid group specified.");
      } else {
         group.setIdleMode();
      }
   }

   private void setNormal(String command, Player activeChar) {
      int groupId;
      try {
         groupId = Integer.parseInt(command.split(" ")[1]);
      } catch (Exception var5) {
         activeChar.sendMessage("Incorrect command arguments.");
         return;
      }

      MobGroup group = MobGroupData.getInstance().getGroup(groupId);
      if (group == null) {
         activeChar.sendMessage("Invalid group specified.");
      } else {
         group.setAttackRandom();
      }
   }

   private void attack(String command, Player activeChar, Creature target) {
      int groupId;
      try {
         groupId = Integer.parseInt(command.split(" ")[1]);
      } catch (Exception var6) {
         activeChar.sendMessage("Incorrect command arguments.");
         return;
      }

      MobGroup group = MobGroupData.getInstance().getGroup(groupId);
      if (group == null) {
         activeChar.sendMessage("Invalid group specified.");
      } else {
         group.setAttackTarget(target);
      }
   }

   private void follow(String command, Player activeChar, Creature target) {
      int groupId;
      try {
         groupId = Integer.parseInt(command.split(" ")[1]);
      } catch (Exception var6) {
         activeChar.sendMessage("Incorrect command arguments.");
         return;
      }

      MobGroup group = MobGroupData.getInstance().getGroup(groupId);
      if (group == null) {
         activeChar.sendMessage("Invalid group specified.");
      } else {
         group.setFollowMode(target);
      }
   }

   private void createGroup(String command, Player activeChar) {
      int groupId;
      int templateId;
      int mobCount;
      try {
         String[] cmdParams = command.split(" ");
         groupId = Integer.parseInt(cmdParams[1]);
         templateId = Integer.parseInt(cmdParams[2]);
         mobCount = Integer.parseInt(cmdParams[3]);
      } catch (Exception var8) {
         activeChar.sendMessage("Usage: //mobgroup_create <group> <npcid> <count>");
         return;
      }

      if (MobGroupData.getInstance().getGroup(groupId) != null) {
         activeChar.sendMessage("Mob group " + groupId + " already exists.");
      } else {
         NpcTemplate template = NpcsParser.getInstance().getTemplate(templateId);
         if (template == null) {
            activeChar.sendMessage("Invalid NPC ID specified.");
         } else {
            MobGroup group = new MobGroup(groupId, template, mobCount);
            MobGroupData.getInstance().addGroup(groupId, group);
            activeChar.sendMessage("Mob group " + groupId + " created.");
         }
      }
   }

   private void removeGroup(String command, Player activeChar) {
      int groupId;
      try {
         groupId = Integer.parseInt(command.split(" ")[1]);
      } catch (Exception var5) {
         activeChar.sendMessage("Usage: //mobgroup_remove <groupId>");
         return;
      }

      MobGroup group = MobGroupData.getInstance().getGroup(groupId);
      if (group == null) {
         activeChar.sendMessage("Invalid group specified.");
      } else {
         this.doAnimation(activeChar);
         group.unspawnGroup();
         if (MobGroupData.getInstance().removeGroup(groupId)) {
            activeChar.sendMessage("Mob group " + groupId + " unspawned and removed.");
         }
      }
   }

   private void spawnGroup(String command, Player activeChar) {
      boolean topos = false;
      int posx = 0;
      int posy = 0;
      int posz = 0;

      int groupId;
      try {
         String[] cmdParams = command.split(" ");
         groupId = Integer.parseInt(cmdParams[1]);

         try {
            posx = Integer.parseInt(cmdParams[2]);
            posy = Integer.parseInt(cmdParams[3]);
            posz = Integer.parseInt(cmdParams[4]);
            topos = true;
         } catch (Exception var10) {
         }
      } catch (Exception var11) {
         activeChar.sendMessage("Usage: //mobgroup_spawn <group> [ x y z ]");
         return;
      }

      MobGroup group = MobGroupData.getInstance().getGroup(groupId);
      if (group == null) {
         activeChar.sendMessage("Invalid group specified.");
      } else {
         this.doAnimation(activeChar);
         if (topos) {
            group.spawnGroup(posx, posy, posz);
         } else {
            group.spawnGroup(activeChar);
         }

         activeChar.sendMessage("Mob group " + groupId + " spawned.");
      }
   }

   private void unspawnGroup(String command, Player activeChar) {
      int groupId;
      try {
         groupId = Integer.parseInt(command.split(" ")[1]);
      } catch (Exception var5) {
         activeChar.sendMessage("Usage: //mobgroup_unspawn <groupId>");
         return;
      }

      MobGroup group = MobGroupData.getInstance().getGroup(groupId);
      if (group == null) {
         activeChar.sendMessage("Invalid group specified.");
      } else {
         this.doAnimation(activeChar);
         group.unspawnGroup();
         activeChar.sendMessage("Mob group " + groupId + " unspawned.");
      }
   }

   private void killGroup(String command, Player activeChar) {
      int groupId;
      try {
         groupId = Integer.parseInt(command.split(" ")[1]);
      } catch (Exception var5) {
         activeChar.sendMessage("Usage: //mobgroup_kill <groupId>");
         return;
      }

      MobGroup group = MobGroupData.getInstance().getGroup(groupId);
      if (group == null) {
         activeChar.sendMessage("Invalid group specified.");
      } else {
         this.doAnimation(activeChar);
         group.killGroup(activeChar);
      }
   }

   private void setCasting(String command, Player activeChar) {
      int groupId;
      try {
         groupId = Integer.parseInt(command.split(" ")[1]);
      } catch (Exception var5) {
         activeChar.sendMessage("Usage: //mobgroup_casting <groupId>");
         return;
      }

      MobGroup group = MobGroupData.getInstance().getGroup(groupId);
      if (group == null) {
         activeChar.sendMessage("Invalid group specified.");
      } else {
         group.setCastMode();
      }
   }

   private void noMove(String command, Player activeChar) {
      int groupId;
      String enabled;
      try {
         groupId = Integer.parseInt(command.split(" ")[1]);
         enabled = command.split(" ")[2];
      } catch (Exception var6) {
         activeChar.sendMessage("Usage: //mobgroup_nomove <groupId> <on|off>");
         return;
      }

      MobGroup group = MobGroupData.getInstance().getGroup(groupId);
      if (group == null) {
         activeChar.sendMessage("Invalid group specified.");
      } else {
         if (enabled.equalsIgnoreCase("on") || enabled.equalsIgnoreCase("true")) {
            group.setNoMoveMode(true);
         } else if (!enabled.equalsIgnoreCase("off") && !enabled.equalsIgnoreCase("false")) {
            activeChar.sendMessage("Incorrect command arguments.");
         } else {
            group.setNoMoveMode(false);
         }
      }
   }

   private void doAnimation(Player activeChar) {
      Broadcast.toSelfAndKnownPlayersInRadius(activeChar, new MagicSkillUse(activeChar, 1008, 1, 4000, 0), 1500);
      activeChar.sendPacket(new SetupGauge(activeChar, 0, 4000));
   }

   private void attackGrp(String command, Player activeChar) {
      int groupId;
      int othGroupId;
      try {
         groupId = Integer.parseInt(command.split(" ")[1]);
         othGroupId = Integer.parseInt(command.split(" ")[2]);
      } catch (Exception var7) {
         activeChar.sendMessage("Usage: //mobgroup_attackgrp <groupId> <TargetGroupId>");
         return;
      }

      MobGroup group = MobGroupData.getInstance().getGroup(groupId);
      if (group == null) {
         activeChar.sendMessage("Invalid group specified.");
      } else {
         MobGroup othGroup = MobGroupData.getInstance().getGroup(othGroupId);
         if (othGroup == null) {
            activeChar.sendMessage("Incorrect target group.");
         } else {
            group.setAttackGroup(othGroup);
         }
      }
   }

   private void invul(String command, Player activeChar) {
      int groupId;
      String enabled;
      try {
         groupId = Integer.parseInt(command.split(" ")[1]);
         enabled = command.split(" ")[2];
      } catch (Exception var6) {
         activeChar.sendMessage("Usage: //mobgroup_invul <groupId> <on|off>");
         return;
      }

      MobGroup group = MobGroupData.getInstance().getGroup(groupId);
      if (group == null) {
         activeChar.sendMessage("Invalid group specified.");
      } else {
         if (enabled.equalsIgnoreCase("on") || enabled.equalsIgnoreCase("true")) {
            group.setInvul(true);
         } else if (!enabled.equalsIgnoreCase("off") && !enabled.equalsIgnoreCase("false")) {
            activeChar.sendMessage("Incorrect command arguments.");
         } else {
            group.setInvul(false);
         }
      }
   }

   private void teleportGroup(String command, Player activeChar) {
      String targetPlayerStr = null;
      Player targetPlayer = null;

      int groupId;
      try {
         groupId = Integer.parseInt(command.split(" ")[1]);
         targetPlayerStr = command.split(" ")[2];
         if (targetPlayerStr != null) {
            targetPlayer = World.getInstance().getPlayer(targetPlayerStr);
         }

         if (targetPlayer == null) {
            ;
         }
      } catch (Exception var7) {
         activeChar.sendMessage("Usage: //mobgroup_teleport <groupId> [playerName]");
         return;
      }

      MobGroup group = MobGroupData.getInstance().getGroup(groupId);
      if (group == null) {
         activeChar.sendMessage("Invalid group specified.");
      } else {
         group.teleportGroup(activeChar);
      }
   }

   private void showGroupList(Player activeChar) {
      MobGroup[] mobGroupList = MobGroupData.getInstance().getGroups();
      activeChar.sendMessage("======= <Mob Groups> =======");

      for(MobGroup mobGroup : mobGroupList) {
         activeChar.sendMessage(
            mobGroup.getGroupId()
               + ": "
               + mobGroup.getActiveMobCount()
               + " alive out of "
               + mobGroup.getMaxMobCount()
               + " of NPC ID "
               + mobGroup.getTemplate().getId()
               + " ("
               + mobGroup.getStatus()
               + ")"
         );
      }

      activeChar.sendPacket(SystemMessageId.FRIEND_LIST_FOOTER);
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }
}
