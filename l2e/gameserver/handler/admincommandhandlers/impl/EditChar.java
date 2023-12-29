package l2e.gameserver.handler.admincommandhandlers.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import l2e.commons.util.StringUtil;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.holder.CharNameHolder;
import l2e.gameserver.data.parser.ClassListParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.ClanMember;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.actor.instance.PetInstance;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.olympiad.Olympiad;
import l2e.gameserver.network.GameClient;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.GMViewItemList;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.PartySmallWindowAll;
import l2e.gameserver.network.serverpackets.PartySmallWindowDeleteAll;
import l2e.gameserver.network.serverpackets.SetSummonRemainTime;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class EditChar implements IAdminCommandHandler {
   private static Logger _log = Logger.getLogger(EditChar.class.getName());
   private static final String[] ADMIN_COMMANDS = new String[]{
      "admin_edit_character",
      "admin_current_player",
      "admin_nokarma",
      "admin_setkarma",
      "admin_setfame",
      "admin_character_list",
      "admin_offline_list",
      "admin_total_list",
      "admin_fake_list",
      "admin_character_info",
      "admin_show_characters",
      "admin_find_character",
      "admin_find_ip",
      "admin_find_account",
      "admin_find_dualbox",
      "admin_strict_find_dualbox",
      "admin_save_modifications",
      "admin_rec",
      "admin_settitle",
      "admin_changename",
      "admin_setsex",
      "admin_setcolor",
      "admin_settcolor",
      "admin_setclass",
      "admin_setpk",
      "admin_setpvp",
      "admin_fullfood",
      "admin_remove_clan_penalty",
      "admin_summon_info",
      "admin_unsummon",
      "admin_summon_setlvl",
      "admin_show_pet_inv",
      "admin_partyinfo",
      "admin_setnoble",
      "admin_game_points",
      "admin_resetrec"
   };

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      NpcHtmlMessage adminhtm = new NpcHtmlMessage(5);
      if (command.equals("admin_current_player")) {
         this.showCharacterInfo(activeChar, activeChar);
      } else if (command.startsWith("admin_character_info")) {
         String[] data = command.split(" ");
         if (data.length > 1) {
            this.showCharacterInfo(activeChar, World.getInstance().getPlayer(data[1]));
         } else if (activeChar.getTarget() instanceof Player) {
            this.showCharacterInfo(activeChar, activeChar.getTarget().getActingPlayer());
         } else {
            activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
         }
      } else if (command.startsWith("admin_total_list")) {
         this.listCharacters(activeChar, 0, 1);
      } else if (command.startsWith("admin_fake_list")) {
         this.listCharacters(activeChar, 2, 1);
      } else if (command.startsWith("admin_character_list")) {
         this.listCharacters(activeChar, 1, 1);
      } else if (command.startsWith("admin_offline_list")) {
         this.listCharacters(activeChar, 3, 1);
      } else if (command.startsWith("admin_show_characters")) {
         try {
            String val = command.substring(22);
            String[] param = val.split(" ");
            int type = Integer.parseInt(param[0]);
            int page = Integer.parseInt(param[1]);
            this.listCharacters(activeChar, type, page);
         } catch (StringIndexOutOfBoundsException var91) {
            activeChar.sendMessage("Usage: //show_characters <page_number>");
         }
      } else if (command.startsWith("admin_find_character")) {
         try {
            String val = command.substring(21);
            this.findCharacter(activeChar, val);
         } catch (StringIndexOutOfBoundsException var90) {
            activeChar.sendMessage("Usage: //find_character <character_name>");
            this.listCharacters(activeChar, 0, 1);
         }
      } else if (command.startsWith("admin_find_ip")) {
         try {
            String val = command.substring(14);
            this.findCharactersPerIp(activeChar, val);
         } catch (Exception var89) {
            activeChar.sendMessage("Usage: //find_ip <www.xxx.yyy.zzz>");
            this.listCharacters(activeChar, 0, 1);
         }
      } else if (command.startsWith("admin_find_account")) {
         try {
            String val = command.substring(19);
            this.findCharactersPerAccount(activeChar, val);
         } catch (Exception var88) {
            activeChar.sendMessage("Usage: //find_account <player_name>");
            this.listCharacters(activeChar, 0, 1);
         }
      } else if (command.startsWith("admin_edit_character")) {
         String[] data = command.split(" ");
         if (data.length > 1) {
            this.editCharacter(activeChar, data[1]);
         } else if (activeChar.getTarget() instanceof Player) {
            this.editCharacter(activeChar, null);
         } else {
            activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
         }
      } else if (command.equals("admin_nokarma")) {
         this.setTargetKarma(activeChar, 0);
      } else if (command.startsWith("admin_setkarma")) {
         try {
            String val = command.substring(15);
            int karma = Integer.parseInt(val);
            this.setTargetKarma(activeChar, karma);
         } catch (Exception var103) {
            if (Config.DEVELOPER) {
               _log.warning("Set karma error: " + var103);
            }

            activeChar.sendMessage("Usage: //setkarma <new_karma_value>");
         }
      } else if (command.startsWith("admin_setpk")) {
         try {
            String val = command.substring(12);
            int pk = Integer.parseInt(val);
            GameObject target = activeChar.getTarget();
            if (target instanceof Player) {
               Player player = (Player)target;
               player.setPkKills(pk);
               player.broadcastUserInfo(true);
               player.sendMessage("A GM changed your PK count to " + pk);
               activeChar.sendMessage(player.getName() + "'s PK count changed to " + pk);
            } else {
               activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
            }
         } catch (Exception var102) {
            if (Config.DEVELOPER) {
               _log.warning("Set pk error: " + var102);
            }

            activeChar.sendMessage("Usage: //setpk <pk_count>");
         }
      } else if (command.startsWith("admin_setpvp")) {
         try {
            String val = command.substring(13);
            int pvp = Integer.parseInt(val);
            GameObject target = activeChar.getTarget();
            if (target instanceof Player) {
               Player player = (Player)target;
               player.setPvpKills(pvp);
               player.broadcastUserInfo(true);
               player.sendMessage("A GM changed your PVP count to " + pvp);
               activeChar.sendMessage(player.getName() + "'s PVP count changed to " + pvp);
            } else {
               activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
            }
         } catch (Exception var101) {
            if (Config.DEVELOPER) {
               _log.warning("Set pvp error: " + var101);
            }

            activeChar.sendMessage("Usage: //setpvp <pvp_count>");
         }
      } else if (command.startsWith("admin_setfame")) {
         try {
            String val = command.substring(14);
            int fame = Integer.parseInt(val);
            GameObject target = activeChar.getTarget();
            if (target instanceof Player) {
               Player player = (Player)target;
               player.setFame(fame);
               player.broadcastUserInfo(true);
               player.sendMessage("A GM changed your Reputation points to " + fame);
               activeChar.sendMessage(player.getName() + "'s Fame changed to " + fame);
            } else {
               activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
            }
         } catch (Exception var100) {
            if (Config.DEVELOPER) {
               _log.warning("Set Fame error: " + var100);
            }

            activeChar.sendMessage("Usage: //setfame <new_fame_value>");
         }
      } else if (command.startsWith("admin_save_modifications")) {
         try {
            String val = command.substring(24);
            this.adminModifyCharacter(activeChar, val);
         } catch (StringIndexOutOfBoundsException var87) {
            activeChar.sendMessage("Error while modifying character.");
            this.listCharacters(activeChar, 0, 1);
         }
      } else if (command.startsWith("admin_rec")) {
         try {
            String val = command.substring(10);
            int recVal = Integer.parseInt(val);
            GameObject target = activeChar.getTarget();
            if (target instanceof Player) {
               Player player = (Player)target;
               player.getRecommendation().setRecomHave(recVal);
               player.sendVoteSystemInfo();
               player.broadcastUserInfo(true);
               player.sendMessage("A GM changed your Recommend points to " + recVal);
               activeChar.sendMessage(player.getName() + "'s Recommend changed to " + recVal);
            } else {
               activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
            }
         } catch (Exception var86) {
            activeChar.sendMessage("Usage: //rec number");
         }
      } else if (command.startsWith("admin_setclass")) {
         try {
            String val = command.substring(15).trim();
            int classidval = Integer.parseInt(val);
            GameObject target = activeChar.getTarget();
            Player player = null;
            if (!(target instanceof Player)) {
               return false;
            }

            player = (Player)target;
            boolean valid = false;

            for(ClassId classid : ClassId.values()) {
               if (classidval == classid.getId()) {
                  valid = true;
               }
            }

            if (valid && player.getClassId().getId() != classidval) {
               player.setClassId(classidval);
               if (!player.isSubClassActive()) {
                  player.setBaseClass(classidval);
               }

               String newclass = ClassListParser.getInstance().getClass(player.getClassId()).getClassName();
               player.store();
               player.sendMessage("A GM changed your class to " + newclass + ".");
               player.broadcastUserInfo(true);
               activeChar.sendMessage(player.getName() + " is a " + newclass + ".");
            } else {
               activeChar.sendMessage("Usage: //setclass <valid_new_classid>");
            }
         } catch (StringIndexOutOfBoundsException var98) {
            adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/setclass/human_fighter.htm");
            activeChar.sendPacket(adminhtm);
         } catch (NumberFormatException var99) {
            activeChar.sendMessage("Usage: //setclass <valid_new_classid>");
         }
      } else if (command.startsWith("admin_settitle")) {
         try {
            String val = command.substring(15);
            GameObject target = activeChar.getTarget();
            Player player = null;
            if (!(target instanceof Player)) {
               return false;
            }

            player = (Player)target;
            player.setTitle(val);
            player.sendMessage("Your title has been changed by a GM");
            player.broadcastTitleInfo();
         } catch (StringIndexOutOfBoundsException var85) {
            activeChar.sendMessage("You need to specify the new title.");
         }
      } else if (command.startsWith("admin_changename")) {
         try {
            String val = command.substring(17);
            GameObject target = activeChar.getTarget();
            Player player = null;
            if (!(target instanceof Player)) {
               return false;
            }

            player = (Player)target;
            if (CharNameHolder.getInstance().getIdByName(val) > 0) {
               activeChar.sendMessage("Warning, player " + val + " already exists");
               return false;
            }

            player.setName(val);
            player.store();
            activeChar.sendMessage("Changed name to " + val);
            player.sendMessage("Your name has been changed by a GM.");
            player.broadcastUserInfo(true);
            if (player.isInParty()) {
               player.getParty().broadcastToPartyMembers(player, PartySmallWindowDeleteAll.STATIC_PACKET);

               for(Player member : player.getParty().getMembers()) {
                  if (member != player) {
                     member.sendPacket(new PartySmallWindowAll(member, player.getParty()));
                  }
               }
            }

            if (player.getClan() != null) {
               player.getClan().broadcastClanStatus();
            }
         } catch (StringIndexOutOfBoundsException var96) {
            activeChar.sendMessage("Usage: //setname new_name_for_target");
         }
      } else if (command.startsWith("admin_setsex")) {
         GameObject target = activeChar.getTarget();
         Player player = null;
         if (!(target instanceof Player)) {
            return false;
         }

         player = (Player)target;
         player.getAppearance().setSex(!player.getAppearance().getSex());
         player.sendMessage("Your gender has been changed by a GM");
         player.broadcastUserInfo(true);
      } else if (command.startsWith("admin_setcolor")) {
         GameObject target = activeChar.getTarget();
         if (target == null) {
            activeChar.sendMessage("You have to select a player!");
            return false;
         }

         if (!(target instanceof Player)) {
            activeChar.sendMessage("Your target is not a player!");
            return false;
         }

         String[] params = command.split(" ");
         if (params.length < 2) {
            activeChar.sendMessage("Usage: //setcolor <colorHex> <timeInDays>");
            return false;
         }

         Player player = (Player)target;
         int color = Util.decodeColor(params[1]);
         long time = Long.valueOf(params[2]);
         if (params.length == 2) {
            player.getAppearance().setNameColor(color);
            player.setVar("namecolor", Integer.toString(color), System.currentTimeMillis() + time * 24L * 60L * 60L * 1000L);
            player.sendMessage("Your name color has been changed by a GM!");
            player.broadcastUserInfo(true);
            return true;
         }
      } else if (command.startsWith("admin_settcolor")) {
         GameObject target = activeChar.getTarget();
         if (target == null) {
            activeChar.sendMessage("You have to select a player!");
            return false;
         }

         if (!(target instanceof Player)) {
            activeChar.sendMessage("Your target is not a player!");
            return false;
         }

         String[] params = command.split(" ");
         if (params.length < 2) {
            activeChar.sendMessage("Usage: //settcolor <colorHex> <timeInDays>");
            return false;
         }

         Player player = (Player)target;
         int color = Util.decodeColor(params[1]);
         long time = Long.valueOf(params[2]);
         if (params.length == 2) {
            player.getAppearance().setTitleColor(color);
            player.setVar("titlecolor", Integer.toString(color), System.currentTimeMillis() + time * 24L * 60L * 60L * 1000L);
            player.sendMessage("Your title color has been changed by a GM");
            player.broadcastUserInfo(true);
            return true;
         }
      } else if (command.startsWith("admin_fullfood")) {
         GameObject target = activeChar.getTarget();
         if (target instanceof PetInstance) {
            PetInstance targetPet = (PetInstance)target;
            targetPet.setCurrentFed(targetPet.getMaxFed());
            targetPet.sendPacket(new SetSummonRemainTime(targetPet.getMaxFed(), targetPet.getCurrentFed()));
         } else {
            activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
         }
      } else if (command.startsWith("admin_remove_clan_penalty")) {
         try {
            StringTokenizer st = new StringTokenizer(command, " ");
            if (st.countTokens() != 3) {
               activeChar.sendMessage("Usage: //remove_clan_penalty join|create charname");
               return false;
            }

            st.nextToken();
            boolean changeCreateExpiryTime = st.nextToken().equalsIgnoreCase("create");
            String playerName = st.nextToken();
            Player player = null;
            player = World.getInstance().getPlayer(playerName);
            if (player == null) {
               Connection con = DatabaseFactory.getInstance().getConnection();
               PreparedStatement ps = con.prepareStatement(
                  "UPDATE characters SET " + (changeCreateExpiryTime ? "clan_create_expiry_time" : "clan_join_expiry_time") + " WHERE char_name=? LIMIT 1"
               );
               ps.setString(1, playerName);
               ps.execute();
            } else if (changeCreateExpiryTime) {
               player.setClanCreateExpiryTime(0L);
            } else {
               player.setClanJoinExpiryTime(0L);
            }

            activeChar.sendMessage("Clan penalty successfully removed to character: " + playerName);
         } catch (Exception var84) {
            var84.printStackTrace();
         }
      } else if (command.startsWith("admin_find_dualbox")) {
         int multibox = 2;

         try {
            String val = command.substring(19);
            multibox = Integer.parseInt(val);
            if (multibox < 1) {
               activeChar.sendMessage("Usage: //find_dualbox [number > 0]");
               return false;
            }
         } catch (Exception var83) {
         }

         this.findDualbox(activeChar, multibox);
      } else if (command.startsWith("admin_strict_find_dualbox")) {
         int multibox = 2;

         try {
            String val = command.substring(26);
            multibox = Integer.parseInt(val);
            if (multibox < 1) {
               activeChar.sendMessage("Usage: //strict_find_dualbox [number > 0]");
               return false;
            }
         } catch (Exception var82) {
         }

         this.findDualboxStrict(activeChar, multibox);
      } else if (command.startsWith("admin_summon_info")) {
         GameObject target = activeChar.getTarget();
         if (target instanceof Summon) {
            this.gatherSummonInfo((Summon)target, activeChar);
         } else {
            activeChar.sendMessage("Invalid target.");
         }
      } else if (command.startsWith("admin_unsummon")) {
         GameObject target = activeChar.getTarget();
         if (target instanceof Summon) {
            ((Summon)target).unSummon(((Summon)target).getOwner());
         } else {
            activeChar.sendMessage("Usable only with Pets/Summons");
         }
      } else if (command.startsWith("admin_summon_setlvl")) {
         GameObject target = activeChar.getTarget();
         if (target instanceof PetInstance) {
            PetInstance pet = (PetInstance)target;

            try {
               String val = command.substring(20);
               int level = Integer.parseInt(val);
               long oldexp = 0L;
               oldexp = pet.getStat().getExp();
               long newexp = pet.getStat().getExpForLevel(level);
               if (oldexp > newexp) {
                  pet.getStat().removeExp(oldexp - newexp);
               } else if (oldexp < newexp) {
                  pet.getStat().addExp(newexp - oldexp);
               }
            } catch (Exception var81) {
            }
         } else {
            activeChar.sendMessage("Usable only with Pets");
         }
      } else if (command.startsWith("admin_show_pet_inv")) {
         GameObject target;
         try {
            String val = command.substring(19);
            int objId = Integer.parseInt(val);
            target = World.getInstance().getPet(objId);
         } catch (Exception var80) {
            target = activeChar.getTarget();
         }

         if (target instanceof PetInstance) {
            ItemInstance[] items = ((PetInstance)target).getInventory().getItems();
            activeChar.sendPacket(new GMViewItemList((PetInstance)target, items, items.length));
         } else {
            activeChar.sendMessage("Usable only with Pets");
         }
      } else if (command.startsWith("admin_partyinfo")) {
         GameObject target;
         try {
            String val = command.substring(16);
            target = World.getInstance().getPlayer(val);
            if (target == null) {
               target = activeChar.getTarget();
            }
         } catch (Exception var79) {
            target = activeChar.getTarget();
         }

         if (target instanceof Player) {
            if (((Player)target).isInParty()) {
               this.gatherPartyInfo((Player)target, activeChar);
            } else {
               activeChar.sendMessage("Not in party.");
            }
         } else {
            activeChar.sendMessage("Invalid target.");
         }
      } else if (command.equals("admin_setnoble")) {
         Player player = null;
         if (activeChar.getTarget() == null) {
            player = activeChar;
         } else if (activeChar.getTarget() != null && activeChar.getTarget() instanceof Player) {
            player = (Player)activeChar.getTarget();
         }

         if (player != null && !player.isNoble()) {
            Olympiad.addNoble(player);
            player.setNoble(true);
            if (player.getClan() != null) {
               player.setPledgeClass(ClanMember.calculatePledgeClass(player));
            } else {
               player.setPledgeClass(5);
            }

            player.sendUserInfo();
            if (player.getObjectId() != activeChar.getObjectId()) {
               activeChar.sendMessage("You've changed nobless status of: " + player.getName());
            }

            player.sendMessage("GM changed your nobless status!");
         }
      } else if (command.startsWith("admin_game_points")) {
         try {
            String val = command.substring(18);
            int points = Integer.parseInt(val);
            GameObject target = activeChar.getTarget();
            if (target instanceof Player) {
               Player player = (Player)target;
               player.setGamePoints(player.getGamePoints() + (long)points);
               player.sendMessage("GM changed your game points count to " + (player.getGamePoints() + (long)points));
               activeChar.sendMessage(player.getName() + "'s game points count changed to " + (player.getGamePoints() + (long)points));
            } else {
               activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
            }
         } catch (Exception var97) {
            if (Config.DEVELOPER) {
               _log.warning("Set game points error: " + var97);
            }

            activeChar.sendMessage("Usage: //game_points <game_points_count>");
         }
      } else if (command.startsWith("admin_resetrec")) {
         for(Player player : World.getInstance().getAllPlayers()) {
            player.getRecommendation().restartRecom();
         }

         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("UPDATE `characters` SET `rec_bonus_time`=3600");
         ) {
            statement.execute();
         } catch (Exception var95) {
            _log.log(java.util.logging.Level.WARNING, "Could not update chararacters recommendations!", (Throwable)var95);
         }

         _log.info("Recommendation Global Task: completed.");
      }

      return true;
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }

   private void listCharacters(Player activeChar, int type, int page) {
      List<String> realIpList = new ArrayList<>();
      List<Player> totalList = new ArrayList<>();
      List<Player> playerList = new ArrayList<>();
      List<Player> fakeList = new ArrayList<>();
      List<Player> offlineList = new ArrayList<>();

      for(Player onlinePlayer : World.getInstance().getAllPlayers()) {
         if (onlinePlayer != null) {
            if (onlinePlayer.isFakePlayer()) {
               fakeList.add(onlinePlayer);
            } else if (onlinePlayer.getClient().isDetached()) {
               offlineList.add(onlinePlayer);
            } else {
               playerList.add(onlinePlayer);
            }

            totalList.add(onlinePlayer);
            if (!realIpList.contains(onlinePlayer.getIPAddress()) && !onlinePlayer.isFakePlayer() && !onlinePlayer.getClient().isDetached()) {
               realIpList.add(onlinePlayer.getIPAddress());
            }
         }
      }

      int perpage = 6;
      int counter = 0;
      List<Player> curList = new ArrayList<>();
      switch(type) {
         case 0:
            curList.addAll(totalList);
            break;
         case 1:
            curList.addAll(playerList);
            break;
         case 2:
            curList.addAll(fakeList);
            break;
         case 3:
            curList.addAll(offlineList);
      }

      boolean isThereNextPage = curList.size() > 6;
      NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
      adminReply.setFile(activeChar, activeChar.getLang(), "data/html/admin/charlist.htm");
      String list = "";
      if (curList != null && !curList.isEmpty()) {
         for(int i = (page - 1) * 6; i < curList.size(); ++i) {
            Player player = curList.get(i);
            if (player != null) {
               list = list
                  + "<table width=280 height=30><tr><td width=90><a action=\"bypass -h admin_character_info "
                  + player.getName()
                  + "\">"
                  + player.getName()
                  + "</a></td><td width=120>"
                  + Util.className(activeChar, player.getClassId().getId())
                  + "</td><td width=50 align=center>"
                  + player.getLevel()
                  + "</td></tr></table><img src=\"L2UI.squaregray\" width=\"280\" height=\"1\">";
            }

            if (++counter >= 6) {
               break;
            }
         }
      } else {
         list = list + "<table width=280><tr><td align=center>Empty List....</td></tr></table>";
      }

      int count = (int)Math.ceil((double)curList.size() / 6.0);
      adminReply.replace("%total%", String.valueOf(totalList.size()));
      adminReply.replace("%real%", String.valueOf(realIpList.size()));
      adminReply.replace("%players%", String.valueOf(playerList.size()));
      adminReply.replace("%fakes%", String.valueOf(fakeList.size()));
      adminReply.replace("%offline%", String.valueOf(offlineList.size()));
      adminReply.replace("%playerList%", list);
      adminReply.replace("%pages%", Util.getNavigationBlock(count, page, curList.size(), 6, isThereNextPage, "admin_show_characters " + type + " %s"));
      activeChar.sendPacket(adminReply);
      totalList.clear();
      realIpList.clear();
      playerList.clear();
      fakeList.clear();
      offlineList.clear();
   }

   private void showCharacterInfo(Player activeChar, Player player) {
      if (player == null) {
         GameObject target = activeChar.getTarget();
         if (!(target instanceof Player)) {
            return;
         }

         player = (Player)target;
      } else {
         activeChar.setTarget(player);
      }

      this.gatherCharacterInfo(activeChar, player, "charinfo.htm");
   }

   private void gatherCharacterInfo(Player activeChar, Player player, String filename) {
      String ip = "N/A";
      if (player == null) {
         activeChar.sendMessage("Player is null.");
      } else {
         GameClient client = player.getClient();
         if (client == null) {
            activeChar.sendMessage("Client is null.");
         } else if (client.isDetached()) {
            activeChar.sendMessage("Client is detached.");
         } else {
            ip = player.getIPAddress();
         }

         NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
         adminReply.setFile(activeChar, activeChar.getLang(), "data/html/admin/" + filename);
         adminReply.replace("%name%", player.getName());
         adminReply.replace("%level%", String.valueOf(player.getLevel()));
         adminReply.replace(
            "%clan%",
            String.valueOf(
               player.getClan() != null ? "<a action=\"bypass -h admin_clan_info " + player.getObjectId() + "\">" + player.getClan().getName() + "</a>" : ""
            )
         );
         adminReply.replace("%xp%", String.valueOf(player.getExp()));
         adminReply.replace("%sp%", String.valueOf(player.getSp()));
         adminReply.replace("%class%", Util.className(activeChar, player.getClassId().getId()));
         adminReply.replace("%ordinal%", String.valueOf(player.getClassId().ordinal()));
         adminReply.replace("%classid%", String.valueOf(player.getClassId()));
         adminReply.replace("%baseclass%", ClassListParser.getInstance().getClass(player.getBaseClass()).getClientCode());
         adminReply.replace("%x%", String.valueOf(player.getX()));
         adminReply.replace("%y%", String.valueOf(player.getY()));
         adminReply.replace("%z%", String.valueOf(player.getZ()));
         adminReply.replace("%currenthp%", String.valueOf((int)player.getCurrentHp()));
         adminReply.replace("%maxhp%", String.valueOf((int)player.getMaxHp()));
         adminReply.replace("%karma%", String.valueOf(player.getKarma()));
         adminReply.replace("%currentmp%", String.valueOf((int)player.getCurrentMp()));
         adminReply.replace("%maxmp%", String.valueOf((int)player.getMaxMp()));
         adminReply.replace("%pvpflag%", String.valueOf(player.getPvpFlag()));
         adminReply.replace("%currentcp%", String.valueOf((int)player.getCurrentCp()));
         adminReply.replace("%maxcp%", String.valueOf((int)player.getMaxCp()));
         adminReply.replace("%pvpkills%", String.valueOf(player.getPvpKills()));
         adminReply.replace("%pkkills%", String.valueOf(player.getPkKills()));
         adminReply.replace("%currentload%", String.valueOf(player.getCurrentLoad()));
         adminReply.replace("%maxload%", String.valueOf(player.getMaxLoad()));
         adminReply.replace("%percent%", String.valueOf(Util.roundTo((float)player.getCurrentLoad() / (float)player.getMaxLoad() * 100.0F, 2)));
         adminReply.replace("%patk%", String.valueOf((int)player.getPAtk(null)));
         adminReply.replace("%matk%", String.valueOf((int)player.getMAtk(null, null)));
         adminReply.replace("%pdef%", String.valueOf((int)player.getPDef(null)));
         adminReply.replace("%mdef%", String.valueOf((int)player.getMDef(null, null)));
         adminReply.replace("%accuracy%", String.valueOf(player.getAccuracy()));
         adminReply.replace("%evasion%", String.valueOf(player.getEvasionRate(null)));
         adminReply.replace("%critical%", String.valueOf((int)player.getCriticalHit(null, null)));
         adminReply.replace("%runspeed%", String.valueOf((int)player.getRunSpeed()));
         adminReply.replace("%patkspd%", String.valueOf((int)player.getPAtkSpd()));
         adminReply.replace("%matkspd%", String.valueOf((int)player.getMAtkSpd()));
         adminReply.replace("%access%", player.getAccessLevel().getLevel() + " (" + player.getAccessLevel().getName() + ")");
         adminReply.replace("%account%", player.getAccountName());
         adminReply.replace("%ip%", ip);
         adminReply.replace("%ai%", String.valueOf(player.getAI().getIntention().name()));
         adminReply.replace(
            "%inst%",
            player.getReflectionId() > 0
               ? "<a action=\"bypass -h admin_instance_spawns " + String.valueOf(player.getReflectionId()) + "\">" + player.getReflectionId() + "</a>"
               : "" + String.valueOf(player.getReflectionId()) + ""
         );
         adminReply.replace("%noblesse%", player.isNoble() ? "Yes" : "No");
         adminReply.replace("%region%", player.getWorldRegion() != null ? player.getWorldRegion().getName() : "");
         adminReply.replace("%farm%", player.getFarmSystem().isAutofarming() ? "<font color=FF0000>Farm Active</font>" : "");
         activeChar.sendPacket(adminReply);
      }
   }

   private void setTargetKarma(Player activeChar, int newKarma) {
      GameObject target = activeChar.getTarget();
      Player player = null;
      if (target instanceof Player) {
         player = (Player)target;
         if (newKarma >= 0) {
            int oldKarma = player.getKarma();
            player.setKarma(newKarma);
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOUR_KARMA_HAS_BEEN_CHANGED_TO_S1);
            sm.addNumber(newKarma);
            player.sendPacket(sm);
            activeChar.sendMessage("Successfully Changed karma for " + player.getName() + " from (" + oldKarma + ") to (" + newKarma + ").");
            if (Config.DEBUG) {
               _log.fine(
                  "[SET KARMA] [GM]" + activeChar.getName() + " Changed karma for " + player.getName() + " from (" + oldKarma + ") to (" + newKarma + ")."
               );
            }
         } else {
            activeChar.sendMessage("You must enter a value for karma greater than or equal to 0.");
            if (Config.DEBUG) {
               _log.fine(
                  "[SET KARMA] ERROR: [GM]"
                     + activeChar.getName()
                     + " entered an incorrect value for new karma: "
                     + newKarma
                     + " for "
                     + player.getName()
                     + "."
               );
            }
         }
      }
   }

   private void adminModifyCharacter(Player activeChar, String modifications) {
      GameObject target = activeChar.getTarget();
      if (target instanceof Player) {
         Player player = (Player)target;
         StringTokenizer st = new StringTokenizer(modifications);
         if (st.countTokens() != 6) {
            this.editCharacter(activeChar, null);
         } else {
            String hp = st.nextToken();
            String mp = st.nextToken();
            String cp = st.nextToken();
            String pvpflag = st.nextToken();
            String pvpkills = st.nextToken();
            String pkkills = st.nextToken();
            int hpval = Integer.parseInt(hp);
            int mpval = Integer.parseInt(mp);
            int cpval = Integer.parseInt(cp);
            int pvpflagval = Integer.parseInt(pvpflag);
            int pvpkillsval = Integer.parseInt(pvpkills);
            int pkkillsval = Integer.parseInt(pkkills);
            player.sendMessage(
               "Admin has changed your stats.  HP: "
                  + hpval
                  + "  MP: "
                  + mpval
                  + "  CP: "
                  + cpval
                  + "  PvP Flag: "
                  + pvpflagval
                  + " PvP/PK "
                  + pvpkillsval
                  + "/"
                  + pkkillsval
            );
            player.setCurrentHp((double)hpval);
            player.setCurrentMp((double)mpval);
            player.setCurrentCp((double)cpval);
            player.setPvpFlag(pvpflagval);
            player.setPvpKills(pvpkillsval);
            player.setPkKills(pkkillsval);
            player.store();
            StatusUpdate su = new StatusUpdate(player);
            su.addAttribute(9, hpval);
            su.addAttribute(10, player.getMaxHp());
            su.addAttribute(11, mpval);
            su.addAttribute(12, player.getMaxMp());
            su.addAttribute(33, cpval);
            su.addAttribute(34, player.getMaxCp());
            player.sendPacket(su);
            activeChar.sendMessage(
               "Changed stats of " + player.getName() + ".  HP: " + hpval + "  MP: " + mpval + "  CP: " + cpval + "  PvP: " + pvpflagval + " / " + pvpkillsval
            );
            if (Config.DEBUG) {
               _log.fine(
                  "[GM]"
                     + activeChar.getName()
                     + " changed stats of "
                     + player.getName()
                     + ".  HP: "
                     + hpval
                     + " MP: "
                     + mpval
                     + " CP: "
                     + cpval
                     + " PvP: "
                     + pvpflagval
                     + " / "
                     + pvpkillsval
               );
            }

            this.showCharacterInfo(activeChar, null);
            player.broadcastCharInfo();
            player.getAI().setIntention(CtrlIntention.IDLE);
            player.decayMe();
            player.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());
         }
      }
   }

   private void editCharacter(Player activeChar, String targetName) {
      GameObject target = null;
      if (targetName != null) {
         target = World.getInstance().getPlayer(targetName);
      } else {
         target = activeChar.getTarget();
      }

      if (target instanceof Player) {
         Player player = (Player)target;
         this.gatherCharacterInfo(activeChar, player, "charedit.htm");
      }
   }

   private void findCharacter(Player activeChar, String CharacterToFind) {
      int CharactersFound = 0;
      NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
      adminReply.setFile(activeChar, activeChar.getLang(), "data/html/admin/charfind.htm");
      StringBuilder replyMSG = new StringBuilder(1000);

      for(Player player : World.getInstance().getAllPlayers()) {
         String name = player.getName();
         if (name.toLowerCase().contains(CharacterToFind.toLowerCase())) {
            ++CharactersFound;
            StringUtil.append(
               replyMSG,
               "<tr><td width=80><a action=\"bypass -h admin_character_info ",
               name,
               "\">",
               name,
               "</a></td><td width=110>",
               ClassListParser.getInstance().getClass(player.getClassId()).getClientCode(),
               "</td><td width=40>",
               String.valueOf(player.getLevel()),
               "</td></tr>"
            );
         }

         if (CharactersFound > 20) {
            break;
         }
      }

      adminReply.replace("%results%", replyMSG.toString());
      String replyMSG2;
      if (CharactersFound == 0) {
         replyMSG2 = "s. Please try again.";
      } else if (CharactersFound > 20) {
         adminReply.replace("%number%", " more than 20");
         replyMSG2 = "s.<br>Please refine your search to see all of the results.";
      } else if (CharactersFound == 1) {
         replyMSG2 = ".";
      } else {
         replyMSG2 = "s.";
      }

      adminReply.replace("%number%", String.valueOf(CharactersFound));
      adminReply.replace("%end%", replyMSG2);
      activeChar.sendPacket(adminReply);
   }

   private void findCharactersPerIp(Player activeChar, String IpAdress) throws IllegalArgumentException {
      boolean findDisconnected = false;
      if (IpAdress.equals("disconnected")) {
         findDisconnected = true;
      } else if (!IpAdress.matches(
         "^(?:(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2(?:[0-4][0-9]|5[0-5]))\\.){3}(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2(?:[0-4][0-9]|5[0-5]))$"
      )) {
         throw new IllegalArgumentException("Malformed IPv4 number");
      }

      int CharactersFound = 0;
      String ip = "0.0.0.0";
      StringBuilder replyMSG = new StringBuilder(1000);
      NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
      adminReply.setFile(activeChar, activeChar.getLang(), "data/html/admin/ipfind.htm");

      for(Player player : World.getInstance().getAllPlayers()) {
         GameClient client = player.getClient();
         if (client != null) {
            if (client.isDetached()) {
               if (!findDisconnected) {
                  continue;
               }
            } else {
               if (findDisconnected) {
                  continue;
               }

               ip = player.getIPAddress();
               if (!ip.equals(IpAdress)) {
                  continue;
               }
            }

            String name = player.getName();
            ++CharactersFound;
            StringUtil.append(
               replyMSG,
               "<tr><td width=80><a action=\"bypass -h admin_character_info ",
               name,
               "\">",
               name,
               "</a></td><td width=110>",
               ClassListParser.getInstance().getClass(player.getClassId()).getClientCode(),
               "</td><td width=40>",
               String.valueOf(player.getLevel()),
               "</td></tr>"
            );
            if (CharactersFound > 20) {
               break;
            }
         }
      }

      adminReply.replace("%results%", replyMSG.toString());
      String replyMSG2;
      if (CharactersFound == 0) {
         replyMSG2 = "s. Maybe they got d/c? :)";
      } else if (CharactersFound > 20) {
         adminReply.replace("%number%", " more than " + String.valueOf(CharactersFound));
         replyMSG2 = "s.<br>In order to avoid you a client crash I won't <br1>display results beyond the 20th character.";
      } else if (CharactersFound == 1) {
         replyMSG2 = ".";
      } else {
         replyMSG2 = "s.";
      }

      adminReply.replace("%ip%", IpAdress);
      adminReply.replace("%number%", String.valueOf(CharactersFound));
      adminReply.replace("%end%", replyMSG2);
      activeChar.sendPacket(adminReply);
   }

   private void findCharactersPerAccount(Player activeChar, String characterName) throws IllegalArgumentException {
      if (!characterName.matches(Config.CNAME_TEMPLATE)) {
         throw new IllegalArgumentException("Malformed character name");
      } else {
         String account = null;
         Player player = World.getInstance().getPlayer(characterName);
         if (player == null) {
            throw new IllegalArgumentException("Player doesn't exist");
         } else {
            Map<Integer, String> chars = player.getAccountChars();
            account = player.getAccountName();
            StringBuilder replyMSG = new StringBuilder(chars.size() * 20);
            NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
            adminReply.setFile(activeChar, activeChar.getLang(), "data/html/admin/accountinfo.htm");

            for(String charname : chars.values()) {
               StringUtil.append(replyMSG, charname, "<br1>");
            }

            adminReply.replace("%characters%", replyMSG.toString());
            adminReply.replace("%account%", account);
            adminReply.replace("%player%", characterName);
            activeChar.sendPacket(adminReply);
         }
      }
   }

   private void findDualbox(Player activeChar, int multibox) {
      Map<String, List<Player>> ipMap = new HashMap<>();
      String ip = "0.0.0.0";
      final Map<String, Integer> dualboxIPs = new HashMap<>();

      for(Player player : World.getInstance().getAllPlayers()) {
         GameClient client = player.getClient();
         if (client != null && !client.isDetached()) {
            ip = player.getIPAddress();
            if (ipMap.get(ip) == null) {
               ipMap.put(ip, new ArrayList<>());
            }

            ipMap.get(ip).add(player);
            if (ipMap.get(ip).size() >= multibox) {
               Integer count = dualboxIPs.get(ip);
               if (count == null) {
                  dualboxIPs.put(ip, multibox);
               } else {
                  dualboxIPs.put(ip, count + 1);
               }
            }
         }
      }

      List<String> keys = new ArrayList<>(dualboxIPs.keySet());
      Collections.sort(keys, new Comparator<String>() {
         public int compare(String left, String right) {
            return dualboxIPs.get(left).compareTo(dualboxIPs.get(right));
         }
      });
      Collections.reverse(keys);
      StringBuilder results = new StringBuilder();

      for(String dualboxIP : keys) {
         StringUtil.append(results, "<a action=\"bypass -h admin_find_ip " + dualboxIP + "\">" + dualboxIP + " (" + dualboxIPs.get(dualboxIP) + ")</a><br1>");
      }

      NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
      adminReply.setFile(activeChar, activeChar.getLang(), "data/html/admin/dualbox.htm");
      adminReply.replace("%multibox%", String.valueOf(multibox));
      adminReply.replace("%results%", results.toString());
      adminReply.replace("%strict%", "");
      activeChar.sendPacket(adminReply);
   }

   private void findDualboxStrict(Player activeChar, int multibox) {
      Map<String, List<Player>> ipMap = new HashMap<>();
      final Map<String, Integer> dualboxIPs = new HashMap<>();

      for(Player player : World.getInstance().getAllPlayers()) {
         GameClient client = player.getClient();
         if (client != null && !client.isDetached()) {
            String ipAdress = player.getIPAddress();
            if (ipMap.get(ipAdress) == null) {
               ipMap.put(ipAdress, new ArrayList<>());
            }

            ipMap.get(ipAdress).add(player);
            if (ipMap.get(ipAdress).size() >= multibox) {
               Integer count = dualboxIPs.get(ipAdress);
               if (count == null) {
                  dualboxIPs.put(ipAdress, multibox);
               } else {
                  dualboxIPs.put(ipAdress, count + 1);
               }
            }
         }
      }

      List<String> keys = new ArrayList<>(dualboxIPs.keySet());
      Collections.sort(keys, new Comparator<String>() {
         public int compare(String left, String right) {
            return dualboxIPs.get(left).compareTo(dualboxIPs.get(right));
         }
      });
      Collections.reverse(keys);
      StringBuilder results = new StringBuilder();

      for(String dualboxIP : keys) {
         StringUtil.append(results, "<a action=\"bypass -h admin_find_ip " + dualboxIP + "\">" + dualboxIP + " (" + dualboxIPs.get(dualboxIP) + ")</a><br1>");
      }

      NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
      adminReply.setFile(activeChar, activeChar.getLang(), "data/html/admin/dualbox.htm");
      adminReply.replace("%multibox%", String.valueOf(multibox));
      adminReply.replace("%results%", results.toString());
      adminReply.replace("%strict%", "strict_");
      activeChar.sendPacket(adminReply);
   }

   private void gatherSummonInfo(Summon target, Player activeChar) {
      NpcHtmlMessage html = new NpcHtmlMessage(0);
      html.setFile(activeChar, activeChar.getLang(), "data/html/admin/petinfo.htm");
      String name = target.getName();
      html.replace("%name%", name == null ? "N/A" : name);
      html.replace("%level%", Integer.toString(target.getLevel()));
      html.replace("%exp%", Long.toString(target.getStat().getExp()));
      String owner = target.getActingPlayer().getName();
      html.replace("%owner%", " <a action=\"bypass -h admin_character_info " + owner + "\">" + owner + "</a>");
      html.replace("%class%", target.getClass().getSimpleName());
      html.replace("%ai%", target.hasAI() ? String.valueOf(target.getAI().getIntention().name()) : "NULL");
      html.replace("%hp%", (int)target.getStatus().getCurrentHp() + "/" + target.getStat().getMaxHp());
      html.replace("%mp%", (int)target.getStatus().getCurrentMp() + "/" + target.getStat().getMaxMp());
      html.replace("%karma%", Integer.toString(target.getKarma()));
      html.replace("%undead%", target.isUndead() ? "yes" : "no");
      if (target instanceof PetInstance) {
         int objId = target.getActingPlayer().getObjectId();
         html.replace("%inv%", " <a action=\"bypass admin_show_pet_inv " + objId + "\">view</a>");
      } else {
         html.replace("%inv%", "none");
      }

      if (target instanceof PetInstance) {
         html.replace("%food%", ((PetInstance)target).getCurrentFed() + "/" + ((PetInstance)target).getPetLevelData().getPetMaxFeed());
         html.replace("%load%", ((PetInstance)target).getInventory().getTotalWeight() + "/" + ((PetInstance)target).getMaxLoad());
      } else {
         html.replace("%food%", "N/A");
         html.replace("%load%", "N/A");
      }

      activeChar.sendPacket(html);
   }

   private void gatherPartyInfo(Player target, Player activeChar) {
      boolean color = true;
      NpcHtmlMessage html = new NpcHtmlMessage(0);
      html.setFile(activeChar, activeChar.getLang(), "data/html/admin/partyinfo.htm");
      StringBuilder text = new StringBuilder(400);

      for(Player member : target.getParty().getMembers()) {
         if (color) {
            text.append("<tr><td><table width=270 border=0 bgcolor=131210 cellpadding=2><tr><td width=30 align=right>");
         } else {
            text.append("<tr><td><table width=270 border=0 cellpadding=2><tr><td width=30 align=right>");
         }

         text.append(
            member.getLevel() + "</td><td width=130><a action=\"bypass -h admin_character_info " + member.getName() + "\">" + member.getName() + "</a>"
         );
         text.append("</td><td width=110 align=right>" + member.getClassId().toString() + "</td></tr></table></td></tr>");
         color = !color;
      }

      html.replace("%player%", target.getName());
      html.replace("%party%", text.toString());
      activeChar.sendPacket(html);
   }
}
