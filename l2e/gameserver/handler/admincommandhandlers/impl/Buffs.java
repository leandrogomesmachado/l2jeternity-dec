package l2e.gameserver.handler.admincommandhandlers.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import l2e.commons.util.GMAudit;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.data.parser.SkillTreesParser;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.network.SystemMessageId;

public class Buffs implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{
      "admin_getbuffs", "admin_stopbuff", "admin_stopallbuffs", "admin_areacancel", "admin_removereuse", "admin_switch_gm_buffs"
   };

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      if (command.startsWith("admin_getbuffs")) {
         StringTokenizer st = new StringTokenizer(command, " ");
         command = st.nextToken();
         int page = 1;
         if (st.hasMoreTokens()) {
            page = Integer.parseInt(st.nextToken());
         }

         showBuffs(activeChar, page);
         return true;
      } else if (command.startsWith("admin_stopbuff")) {
         try {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            int objectId = Integer.parseInt(st.nextToken());
            int skillId = Integer.parseInt(st.nextToken());
            this.removeBuff(activeChar, objectId, skillId);
            return true;
         } catch (Exception var8) {
            activeChar.sendMessage("Failed removing effect: " + var8.getMessage());
            activeChar.sendMessage("Usage: //stopbuff <objectId> <skillId>");
            return false;
         }
      } else if (command.startsWith("admin_stopallbuffs")) {
         try {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            int objectId = Integer.parseInt(st.nextToken());
            removeAllBuffs(activeChar, objectId);
            return true;
         } catch (Exception var9) {
            activeChar.sendMessage("Failed removing all effects: " + var9.getMessage());
            activeChar.sendMessage("Usage: //stopallbuffs <objectId>");
            return false;
         }
      } else if (command.startsWith("admin_areacancel")) {
         StringTokenizer st = new StringTokenizer(command, " ");
         st.nextToken();
         String val = st.nextToken();

         try {
            int radius = Integer.parseInt(val);

            for(Player knownChar : World.getInstance().getAroundPlayers(activeChar, radius, 200)) {
               knownChar.stopAllEffects();
            }

            activeChar.sendMessage("All effects canceled within raidus " + radius);
            return true;
         } catch (NumberFormatException var12) {
            activeChar.sendMessage("Usage: //areacancel <radius>");
            return false;
         }
      } else if (command.startsWith("admin_removereuse")) {
         StringTokenizer st = new StringTokenizer(command, " ");
         command = st.nextToken();
         Player player = null;
         if (st.hasMoreTokens()) {
            String playername = st.nextToken();

            try {
               player = World.getInstance().getPlayer(playername);
            } catch (Exception var11) {
            }

            if (player == null) {
               activeChar.sendMessage("The player " + playername + " is not online.");
               return false;
            }
         } else {
            if (!activeChar.getTarget().isPlayer()) {
               activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
               return false;
            }

            player = activeChar.getTarget().getActingPlayer();
         }

         try {
            player.getSkillReuseTimeStamps().clear();
            player.resetDisabledSkills();
            player.resetReuse();
            player.sendSkillList(true);
            activeChar.sendMessage("Skill reuse was removed from " + player.getName() + ".");
            return true;
         } catch (NullPointerException var10) {
            return false;
         }
      } else if (command.startsWith("admin_switch_gm_buffs")) {
         if (Config.GM_GIVE_SPECIAL_SKILLS != Config.GM_GIVE_SPECIAL_AURA_SKILLS) {
            boolean toAuraSkills = activeChar.getKnownSkill(7041) != null;
            switchSkills(activeChar, toAuraSkills);
            activeChar.sendSkillList(false);
            activeChar.sendMessage("You have succefully changed to target " + (toAuraSkills ? "aura" : "one") + " special skills.");
            return true;
         } else {
            activeChar.sendMessage("There is nothing to switch.");
            return false;
         }
      } else {
         return true;
      }
   }

   public static void switchSkills(Player gmchar, boolean toAuraSkills) {
      for(Skill skill : toAuraSkills ? SkillTreesParser.getInstance().getGMSkillTree().values() : SkillTreesParser.getInstance().getGMAuraSkillTree().values()) {
         gmchar.removeSkill(skill, false);
      }

      SkillTreesParser.getInstance().addSkills(gmchar, toAuraSkills);
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }

   public static void showBuffs(Player activeChar, int page) {
      Creature target = (Creature)activeChar.getTarget();
      if (target != null) {
         String html = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/admin/effects_info.htm");
         String template = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/admin/effects_template.htm");
         String block = "";
         String list = "";
         List<Effect> effList = new ArrayList<>();

         for(Effect ef : target.getAllEffects()) {
            if (ef != null && ef.isIconDisplay()) {
               effList.add(ef);
            }
         }

         if (!effList.isEmpty() && effList.size() != 0) {
            int perpage = 5;
            int counter = 0;
            int totalSize = effList.size();
            boolean isThereNextPage = totalSize > 5;

            for(int i = (page - 1) * 5; i < totalSize; ++i) {
               Effect data = effList.get(i);
               if (data != null) {
                  block = template.replace("{name}", activeChar.getSkillName(data.getSkill()));
                  block = block.replace("{icon}", data.getSkill().getIcon());
                  block = block.replace("{time}", data.getSkill().isToggle() ? "<font color=\"b02e31\">-1</font>" : getTimeLeft((long)data.getTimeLeft()));
                  block = block.replace("{type}", data.getSkill().isToggle() ? "Toogle" : (data.getSkill().isPassive() ? "Passive" : "Active"));
                  block = block.replace("{bypass}", "bypass -h admin_stopbuff " + Integer.toString(target.getObjectId()) + " " + data.getSkill().getId() + "");
                  list = list + block;
               }

               if (++counter >= 5) {
                  break;
               }
            }

            double pages = (double)totalSize / 5.0;
            int count = (int)Math.ceil(pages);
            html = html.replace("{list}", list);
            html = html.replace("{page}", String.valueOf(page));
            html = html.replace("{objId}", String.valueOf(target.getObjectId()));
            html = html.replace("{navigation}", Util.getNavigationBlock(count, page, totalSize, 5, isThereNextPage, "admin_getbuffs %s"));
            html = html.replace(
               "{npc_name}",
               target instanceof Npc
                  ? (activeChar.getLang() != null && !activeChar.getLang().equalsIgnoreCase("en") ? ((Npc)target).getNameRu() : ((Npc)target).getName())
                  : target.getName()
            );
            Util.setHtml(html, activeChar);
            if (Config.GMAUDIT) {
               GMAudit.auditGMAction(
                  activeChar.getName() + " [" + activeChar.getObjectId() + "]", "getbuffs", "(" + Integer.toString(target.getObjectId()) + ")", ""
               );
            }
         } else {
            html = html.replace("{list}", "<tr><td align=center>Empty Effects List!</td></tr>");
            html = html.replace("{page}", String.valueOf(page));
            html = html.replace("{navigation}", "<td>&nbsp;</td>");
            html = html.replace(
               "{npc_name}",
               target instanceof Npc
                  ? (activeChar.getLang() != null && !activeChar.getLang().equalsIgnoreCase("en") ? ((Npc)target).getNameRu() : ((Npc)target).getName())
                  : target.getName()
            );
            Util.setHtml(html, activeChar);
         }
      }
   }

   private void removeBuff(Player activeChar, int objId, int skillId) {
      Creature target = null;

      try {
         target = (Creature)World.getInstance().findObject(objId);
      } catch (Exception var10) {
      }

      if (target != null && skillId > 0) {
         Effect[] effects = target.getAllEffects();

         for(Effect e : effects) {
            if (e != null && e.getSkill().getId() == skillId) {
               e.exit();
               activeChar.sendMessage(
                  "Removed " + activeChar.getSkillName(e.getSkill()) + " level " + e.getSkill().getLevel() + " from " + target.getName() + " (" + objId + ")"
               );
            }
         }

         showBuffs(activeChar, 1);
         if (Config.GMAUDIT) {
            GMAudit.auditGMAction(
               activeChar.getName() + " [" + activeChar.getObjectId() + "]", "stopbuff", target.getName() + " (" + objId + ")", Integer.toString(skillId)
            );
         }
      }
   }

   private static String getTimeLeft(long time) {
      int hours = (int)(time / 60L / 60L);
      int mins = (int)((time - (long)(hours * 60 * 60)) / 60L);
      int secs = (int)(time - (long)(hours * 60 * 60 + mins * 60));
      String Strhours = hours < 10 ? "0" + hours : "" + hours;
      String Strmins = mins < 10 ? "0" + mins : "" + mins;
      String Strsecs = secs < 10 ? "0" + secs : "" + secs;
      if (hours > 0) {
         return "<font color=\"b02e31\">" + Strhours + ":" + Strmins + ":" + Strsecs + "</font>";
      } else {
         return hours <= 0 && mins > 0 ? "<font color=\"b02e31\">" + Strmins + ":" + Strsecs + "</font>" : "<font color=\"b02e31\">" + Strsecs + "</font>";
      }
   }

   private static void removeAllBuffs(Player activeChar, int objId) {
      Creature target = null;

      try {
         target = (Creature)World.getInstance().findObject(objId);
      } catch (Exception var4) {
      }

      if (target != null) {
         target.stopAllEffects();
         activeChar.sendMessage("Removed all effects from " + target.getName() + " (" + objId + ")");
         showBuffs(activeChar, 1);
         if (Config.GMAUDIT) {
            GMAudit.auditGMAction(activeChar.getName() + " [" + activeChar.getObjectId() + "]", "stopallbuffs", target.getName() + " (" + objId + ")", "");
         }
      }
   }
}
