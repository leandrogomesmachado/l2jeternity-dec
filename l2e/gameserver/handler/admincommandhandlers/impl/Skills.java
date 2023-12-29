package l2e.gameserver.handler.admincommandhandlers.impl;

import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import l2e.commons.util.StringUtil;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.ClassListParser;
import l2e.gameserver.data.parser.SkillTreesParser;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.SkillLearn;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.PledgeSkillList;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class Skills implements IAdminCommandHandler {
   private static Logger _log = Logger.getLogger(Skills.class.getName());
   private static final String[] ADMIN_COMMANDS = new String[]{
      "admin_show_skills",
      "admin_remove_skills",
      "admin_skill_list",
      "admin_skill_index",
      "admin_add_skill",
      "admin_remove_skill",
      "admin_get_skills",
      "admin_reset_skills",
      "admin_give_all_skills",
      "admin_give_all_skills_fs",
      "admin_give_all_clan_skills",
      "admin_remove_all_skills",
      "admin_add_clan_skill",
      "admin_setskill",
      "admin_refresh_skills"
   };
   private static Skill[] adminSkills;

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      NpcHtmlMessage adminhtm = new NpcHtmlMessage(5);
      if (command.equals("admin_show_skills")) {
         this.showMainPage(activeChar);
      } else if (command.startsWith("admin_remove_skills")) {
         try {
            String val = command.substring(20);
            this.removeSkillsPage(activeChar, Integer.parseInt(val));
         } catch (StringIndexOutOfBoundsException var12) {
         }
      } else if (command.startsWith("admin_skill_list")) {
         adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/skills.htm");
         activeChar.sendPacket(adminhtm);
      } else if (command.startsWith("admin_skill_index")) {
         try {
            String val = command.substring(18);
            adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/skills/" + val + ".htm");
            activeChar.sendPacket(adminhtm);
         } catch (StringIndexOutOfBoundsException var11) {
         }
      } else if (command.startsWith("admin_add_skill")) {
         try {
            String val = command.substring(15);
            this.adminAddSkill(activeChar, val);
         } catch (Exception var10) {
            activeChar.sendMessage("Usage: //add_skill <skill_id> <level>");
         }
      } else if (command.startsWith("admin_remove_skill")) {
         try {
            String id = command.substring(19);
            int idval = Integer.parseInt(id);
            this.adminRemoveSkill(activeChar, idval);
         } catch (Exception var9) {
            activeChar.sendMessage("Usage: //remove_skill <skill_id>");
         }
      } else if (command.equals("admin_get_skills")) {
         this.adminGetSkills(activeChar);
      } else if (command.equals("admin_reset_skills")) {
         this.adminResetSkills(activeChar);
      } else if (command.equals("admin_give_all_skills")) {
         this.adminGiveAllSkills(activeChar, false);
      } else if (command.equals("admin_give_all_skills_fs")) {
         this.adminGiveAllSkills(activeChar, true);
      } else if (command.equals("admin_give_all_clan_skills")) {
         this.adminGiveAllClanSkills(activeChar);
      } else if (command.equals("admin_remove_all_skills")) {
         GameObject target = activeChar.getTarget();
         if (target == null || !target.isPlayer()) {
            activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
            return false;
         }

         Player player = target.getActingPlayer();

         for(Skill skill : player.getAllSkills()) {
            player.removeSkill(skill);
         }

         activeChar.sendMessage("You have removed all skills from " + player.getName() + ".");
         player.sendMessage("Admin removed all skills from you.");
         player.sendSkillList(false);
         player.broadcastUserInfo(true);
      } else if (command.startsWith("admin_add_clan_skill")) {
         try {
            String[] val = command.split(" ");
            this.adminAddClanSkill(activeChar, Integer.parseInt(val[1]), Integer.parseInt(val[2]));
         } catch (Exception var8) {
            activeChar.sendMessage("Usage: //add_clan_skill <skill_id> <level>");
         }
      } else if (command.startsWith("admin_setskill")) {
         String[] split = command.split(" ");
         int id = Integer.parseInt(split[1]);
         int lvl = Integer.parseInt(split[2]);
         Skill skill = SkillsParser.getInstance().getInfo(id, lvl);
         activeChar.addSkill(skill);
         activeChar.sendSkillList(false);
         activeChar.sendMessage("You added yourself skill " + activeChar.getSkillName(skill) + "(" + id + ") level " + lvl);
      } else if (command.startsWith("admin_refresh_skills")) {
         GameObject target = activeChar.getTarget();
         if (target == null || !target.isPlayer()) {
            activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
            return false;
         }

         Player player = target.getActingPlayer();
         if (player != null) {
            player.resetDisabledSkills();
            player.resetReuse();
            player.sendSkillList(true);
            player.sendMessage("Your skills reuse refreshed!");
         }
      }

      return true;
   }

   private void adminGiveAllSkills(Player activeChar, boolean includedByFs) {
      GameObject target = activeChar.getTarget();
      if (target != null && target.isPlayer()) {
         Player player = target.getActingPlayer();
         activeChar.sendMessage("You gave " + player.giveAvailableSkills(includedByFs, true) + " skills to " + player.getName());
         player.sendSkillList(false);
      } else {
         activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
      }
   }

   private void adminGiveAllClanSkills(Player activeChar) {
      GameObject target = activeChar.getTarget();
      if (target != null && target.isPlayer()) {
         Player player = target.getActingPlayer();
         Clan clan = player.getClan();
         if (clan == null) {
            activeChar.sendPacket(SystemMessageId.TARGET_MUST_BE_IN_CLAN);
         } else {
            if (!player.isClanLeader()) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.TARGET_MUST_BE_IN_CLAN);
               sm.addPcName(player);
               activeChar.sendPacket(sm);
            }

            List<SkillLearn> skills = SkillTreesParser.getInstance().getAvailablePledgeSkills(clan);
            SkillsParser st = SkillsParser.getInstance();

            for(SkillLearn s : skills) {
               clan.addNewSkill(st.getInfo(s.getId(), s.getLvl()));
            }

            clan.broadcastToOnlineMembers(new PledgeSkillList(clan));

            for(Player member : clan.getOnlineMembers(0)) {
               member.sendSkillList(false);
            }

            activeChar.sendMessage("You gave " + skills.size() + " skills to " + player.getName() + "'s clan " + clan.getName() + ".");
            player.sendMessage("Your clan received " + skills.size() + " skills.");
         }
      } else {
         activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
      }
   }

   private void removeSkillsPage(Player activeChar, int page) {
      GameObject target = activeChar.getTarget();
      if (target != null && target.isPlayer()) {
         Player player = target.getActingPlayer();
         Skill[] skills = player.getAllSkills().toArray(new Skill[player.getAllSkills().size()]);
         int maxSkillsPerPage = 10;
         int maxPages = skills.length / 10;
         if (skills.length > 10 * maxPages) {
            ++maxPages;
         }

         if (page > maxPages) {
            page = maxPages;
         }

         int skillsStart = 10 * page;
         int skillsEnd = skills.length;
         if (skillsEnd - skillsStart > 10) {
            skillsEnd = skillsStart + 10;
         }

         NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
         StringBuilder replyMSG = StringUtil.startAppend(
            500 + maxPages * 50 + (skillsEnd - skillsStart + 1) * 50,
            "<html><body><table width=260><tr><td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td><td width=180><center>Character Selection Menu</center></td><td width=40><button value=\"Back\" action=\"bypass -h admin_show_skills\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table><br><br><center>Editing <font color=\"LEVEL\">",
            player.getName(),
            "</font></center><br><table width=270><tr><td>Lv: ",
            String.valueOf(player.getLevel()),
            " ",
            ClassListParser.getInstance().getClass(player.getClassId()).getClientCode(),
            "</td></tr></table><br><table width=270><tr><td>Note: Dont forget that modifying players skills can</td></tr><tr><td>ruin the game...</td></tr></table><br><center>Click on the skill you wish to remove:</center><br><center><table width=270><tr>"
         );

         for(int x = 0; x < maxPages; ++x) {
            int pagenr = x + 1;
            StringUtil.append(replyMSG, "<td><a action=\"bypass -h admin_remove_skills ", String.valueOf(x), "\">Page ", String.valueOf(pagenr), "</a></td>");
         }

         replyMSG.append("</tr></table></center><br><table width=270><tr><td width=80>Name:</td><td width=60>Level:</td><td width=40>Id:</td></tr>");

         for(int i = skillsStart; i < skillsEnd; ++i) {
            StringUtil.append(
               replyMSG,
               "<tr><td width=80><a action=\"bypass -h admin_remove_skill ",
               String.valueOf(skills[i].getId()),
               "\">",
               activeChar.getSkillName(skills[i]),
               "</a></td><td width=60>",
               String.valueOf(skills[i].getLevel()),
               "</td><td width=40>",
               String.valueOf(skills[i].getId()),
               "</td></tr>"
            );
         }

         replyMSG.append(
            "</table><br><center><table>Remove skill by ID :<tr><td>Id: </td><td><edit var=\"id_to_remove\" width=110></td></tr></table></center><center><button value=\"Remove skill\" action=\"bypass -h admin_remove_skill $id_to_remove\" width=110 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center><br><center><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></body></html>"
         );
         adminReply.setHtml(activeChar, replyMSG.toString());
         activeChar.sendPacket(adminReply);
      } else {
         activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
      }
   }

   private void showMainPage(Player activeChar) {
      GameObject target = activeChar.getTarget();
      if (target != null && target.isPlayer()) {
         Player player = target.getActingPlayer();
         NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
         adminReply.setFile(activeChar, activeChar.getLang(), "data/html/admin/charskills.htm");
         adminReply.replace("%name%", player.getName());
         adminReply.replace("%level%", String.valueOf(player.getLevel()));
         adminReply.replace("%class%", ClassListParser.getInstance().getClass(player.getClassId()).getClientCode());
         activeChar.sendPacket(adminReply);
      } else {
         activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
      }
   }

   private void adminGetSkills(Player activeChar) {
      GameObject target = activeChar.getTarget();
      if (target != null && target.isPlayer()) {
         Player player = target.getActingPlayer();
         if (player.getName().equals(activeChar.getName())) {
            player.sendPacket(SystemMessageId.CANNOT_USE_ON_YOURSELF);
         } else {
            Skill[] skills = player.getAllSkills().toArray(new Skill[player.getAllSkills().size()]);
            adminSkills = activeChar.getAllSkills().toArray(new Skill[activeChar.getAllSkills().size()]);

            for(Skill skill : adminSkills) {
               activeChar.removeSkill(skill);
            }

            for(Skill skill : skills) {
               activeChar.addSkill(skill, true);
            }

            activeChar.sendMessage("You now have all the skills of " + player.getName() + ".");
            activeChar.sendSkillList(false);
         }

         this.showMainPage(activeChar);
      } else {
         activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
      }
   }

   private void adminResetSkills(Player activeChar) {
      GameObject target = activeChar.getTarget();
      if (target != null && target.isPlayer()) {
         Player player = target.getActingPlayer();
         if (adminSkills == null) {
            activeChar.sendMessage("You must get the skills of someone in order to do this.");
         } else {
            Skill[] skills = player.getAllSkills().toArray(new Skill[player.getAllSkills().size()]);

            for(Skill skill : skills) {
               player.removeSkill(skill);
            }

            for(Skill skill : activeChar.getAllSkills()) {
               player.addSkill(skill, true);
            }

            for(Skill skill : skills) {
               activeChar.removeSkill(skill);
            }

            for(Skill skill : adminSkills) {
               activeChar.addSkill(skill, true);
            }

            player.sendMessage("[GM]" + activeChar.getName() + " updated your skills.");
            activeChar.sendMessage("You now have all your skills back.");
            adminSkills = null;
            activeChar.sendSkillList(false);
            player.sendSkillList(false);
         }

         this.showMainPage(activeChar);
      } else {
         activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
      }
   }

   private void adminAddSkill(Player activeChar, String val) {
      GameObject target = activeChar.getTarget();
      if (target != null && target.isPlayer()) {
         Player player = target.getActingPlayer();
         StringTokenizer st = new StringTokenizer(val);
         if (st.countTokens() != 2) {
            this.showMainPage(activeChar);
         } else {
            Skill skill = null;

            try {
               String id = st.nextToken();
               String level = st.nextToken();
               int idval = Integer.parseInt(id);
               int levelval = Integer.parseInt(level);
               skill = SkillsParser.getInstance().getInfo(idval, levelval);
            } catch (Exception var11) {
               _log.log(java.util.logging.Level.WARNING, "", (Throwable)var11);
            }

            if (skill != null) {
               String name = activeChar.getSkillName(skill);
               player.sendMessage("Admin gave you the skill " + name + ".");
               player.addSkill(skill, true);
               player.sendSkillList(false);
               activeChar.sendMessage("You gave the skill " + name + " to " + player.getName() + ".");
               if (Config.DEBUG) {
                  _log.fine("[GM]" + activeChar.getName() + " gave skill " + name + " to " + player.getName() + ".");
               }

               activeChar.sendSkillList(false);
            } else {
               activeChar.sendMessage("Error: there is no such skill.");
            }

            this.showMainPage(activeChar);
         }
      } else {
         activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
         this.showMainPage(activeChar);
      }
   }

   private void adminRemoveSkill(Player activeChar, int idval) {
      GameObject target = activeChar.getTarget();
      if (target != null && target.isPlayer()) {
         Player player = target.getActingPlayer();
         Skill skill = SkillsParser.getInstance().getInfo(idval, player.getSkillLevel(idval));
         if (skill != null) {
            String skillname = activeChar.getSkillName(skill);
            player.sendMessage("Admin removed the skill " + skillname + " from your skills list.");
            player.removeSkill(skill);
            activeChar.sendMessage("You removed the skill " + skillname + " from " + player.getName() + ".");
            if (Config.DEBUG) {
               _log.fine("[GM]" + activeChar.getName() + " removed skill " + skill.getNameEn() + " from " + player.getName() + ".");
            }

            activeChar.sendSkillList(false);
         } else {
            activeChar.sendMessage("Error: there is no such skill.");
         }

         this.removeSkillsPage(activeChar, 0);
      } else {
         activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
      }
   }

   private void adminAddClanSkill(Player activeChar, int id, int level) {
      GameObject target = activeChar.getTarget();
      if (target != null && target.isPlayer()) {
         Player player = target.getActingPlayer();
         if (!player.isClanLeader()) {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_IS_NOT_A_CLAN_LEADER);
            sm.addString(player.getName());
            activeChar.sendPacket(sm);
            this.showMainPage(activeChar);
         } else if (id >= 370 && id <= 391 && level >= 1 && level <= 3) {
            Skill skill = SkillsParser.getInstance().getInfo(id, level);
            if (skill == null) {
               activeChar.sendMessage("Error: there is no such skill.");
            } else {
               String skillname = activeChar.getSkillName(skill);
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_SKILL_S1_ADDED);
               sm.addSkillName(skill);
               player.sendPacket(sm);
               Clan clan = player.getClan();
               clan.broadcastToOnlineMembers(sm);
               clan.addNewSkill(skill);
               activeChar.sendMessage("You gave the Clan Skill: " + skillname + " to the clan " + clan.getName() + ".");
               clan.broadcastToOnlineMembers(new PledgeSkillList(clan));

               for(Player member : clan.getOnlineMembers(0)) {
                  member.sendSkillList(false);
               }

               this.showMainPage(activeChar);
            }
         } else {
            activeChar.sendMessage("Usage: //add_clan_skill <skill_id> <level>");
            this.showMainPage(activeChar);
         }
      } else {
         activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
         this.showMainPage(activeChar);
      }
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }
}
