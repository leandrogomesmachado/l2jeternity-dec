package l2e.gameserver.handler.admincommandhandlers.impl;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import l2e.commons.util.Util;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.data.parser.ExperienceParser;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.instancemanager.WalkingManager;
import l2e.gameserver.model.Elementals;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.actor.templates.npc.champion.ChampionTemplate;
import l2e.gameserver.model.reward.CalculateRewardChances;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class EditNpc implements IAdminCommandHandler {
   private static Logger _log = Logger.getLogger(EditNpc.class.getName());
   private static final NumberFormat pf = NumberFormat.getPercentInstance(Locale.ENGLISH);
   private static final String[] ADMIN_COMMANDS = new String[]{
      "admin_show_skill_list_npc", "admin_show_drop_list", "admin_show_spoil_list", "admin_log_npc_spawn", "admin_show_npc_info", "admin_show_npc_stats"
   };

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      StringTokenizer st = new StringTokenizer(command, " ");
      String actualCommand = st.nextToken();
      String var5 = actualCommand.toLowerCase();
      switch(var5) {
         case "admin_show_skill_list_npc":
            if (st.countTokens() < 1) {
               activeChar.sendMessage("Usage: //show_skill_list_npc <npc_id> <page>");
               return false;
            }

            try {
               int npcId = Integer.parseInt(st.nextToken());
               int page = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 0;
               NpcTemplate npc = NpcsParser.getInstance().getTemplate(npcId);
               if (npc != null) {
                  this.showNpcSkillList(activeChar, npc, page);
               } else {
                  activeChar.sendMessage("NPC does not exist or not loaded. npc_id:" + npcId);
               }
            } catch (NumberFormatException var22) {
               activeChar.sendMessage("npc_id must be a number.");
            }
            break;
         case "admin_show_npc_info":
            if (activeChar.getTarget() == null) {
               activeChar.sendMessage("Usage: //show_npc_info <npc_id> and target!");
               return false;
            }

            try {
               GameObject target = activeChar.getTarget();
               if (target instanceof Npc) {
                  this.showNpcInfoList(activeChar, (Npc)target);
               } else {
                  activeChar.sendMessage("NPC does not exist or not loaded.");
               }
            } catch (NumberFormatException var21) {
               activeChar.sendMessage("npc_id must be a number.");
            }
            break;
         case "admin_show_npc_stats":
            if (activeChar.getTarget() == null) {
               activeChar.sendMessage("Usage: //show_npc_stats <npc_id> and target!");
               return false;
            }

            try {
               GameObject target = activeChar.getTarget();
               if (target instanceof Npc) {
                  this.showNpcStatList(activeChar, (Npc)target);
               } else {
                  activeChar.sendMessage("NPC does not exist or not loaded.");
               }
            } catch (NumberFormatException var20) {
               activeChar.sendMessage("npc_id must be a number.");
            }
            break;
         case "admin_show_drop_list":
            if (st.countTokens() < 1) {
               activeChar.sendMessage("Usage: //show_drop_list <npc_id>!");
               return false;
            }

            try {
               NpcTemplate tpl = null;
               String npcId = null;
               String page = null;
               ChampionTemplate championTemplate = null;
               GameObject target = activeChar.getTarget();
               Npc npc = null;

               try {
                  npcId = st.nextToken();
               } catch (Exception var18) {
               }

               try {
                  page = st.nextToken();
               } catch (Exception var17) {
               }

               if (npcId != null) {
                  tpl = NpcsParser.getInstance().getTemplate(Integer.parseInt(npcId));
               }

               if (tpl != null) {
                  if (target != null && !target.isPlayable() && !target.isPlayer()) {
                     npc = (Npc)target;
                     championTemplate = npc.getChampionTemplate();
                  }

                  this.showNpcDropList(activeChar, npc, tpl, page != null ? Integer.parseInt(page) : 1, championTemplate);
               } else {
                  activeChar.sendMessage("NPC does not exist or not loaded. npc_id:" + npcId);
               }
            } catch (NumberFormatException var19) {
               activeChar.sendMessage("npc_id must be a number.");
            }
            break;
         case "admin_show_spoil_list":
            if (st.countTokens() < 1) {
               activeChar.sendMessage("Usage: //show_drop_list <npc_id>!");
               return false;
            }

            try {
               NpcTemplate tpl = null;
               String npcId = null;
               String page = null;
               ChampionTemplate championTemplate = null;
               GameObject target = activeChar.getTarget();
               Npc npc = null;

               try {
                  npcId = st.nextToken();
               } catch (Exception var15) {
               }

               try {
                  page = st.nextToken();
               } catch (Exception var14) {
               }

               if (npcId != null) {
                  tpl = NpcsParser.getInstance().getTemplate(Integer.parseInt(npcId));
               }

               if (tpl != null) {
                  if (target != null && !target.isPlayable() && !target.isPlayer()) {
                     npc = (Npc)target;
                     championTemplate = npc.getChampionTemplate();
                  }

                  this.showNpcSpoilList(activeChar, npc, tpl, page != null ? Integer.parseInt(page) : 1, championTemplate);
               } else {
                  activeChar.sendMessage("NPC does not exist or not loaded. npc_id:" + npcId);
               }
            } catch (NumberFormatException var16) {
               activeChar.sendMessage("npc_id must be a number.");
            }
            break;
         case "admin_log_npc_spawn":
            GameObject target = activeChar.getTarget();
            if (target instanceof Npc) {
               Npc npc = (Npc)target;
               _log.info("('', 1, " + npc.getId() + ", " + npc.getX() + ", " + npc.getY() + ", " + npc.getZ() + ", 0, 0, " + npc.getHeading() + ", 60, 0, 0),");
            }
      }

      return true;
   }

   private void showNpcSkillList(Player activeChar, NpcTemplate npc, int page) {
      int PAGE_SIZE = 20;
      NpcHtmlMessage html = new NpcHtmlMessage(0);
      html.setFile(activeChar, activeChar.getLang(), "data/html/admin/editnpc-skills.htm");
      html.replace("%npcId%", String.valueOf(npc.getId()));
      html.replace("%title_npc_name%", String.valueOf(npc.getName()));
      html.replace("%page%", String.valueOf(page + 1));
      Map<Integer, Skill> skills = npc.getSkills();
      int pages = skills.size() / 20;
      if (20 * pages < skills.size()) {
         ++pages;
      }

      if (pages > 1) {
         StringBuilder sb = new StringBuilder();
         sb.append("<table width=280 cellspacing=0><tr>");

         for(int i = 0; i < pages; ++i) {
            sb.append(
               "<td align=center><button action=\"bypass admin_show_skill_list_npc "
                  + npc.getId()
                  + " "
                  + i
                  + "\" value=\""
                  + (i + 1)
                  + "\" width=30 height=22 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>"
            );
         }

         sb.append("</tr></table>");
         html.replace("%pages%", sb.toString());
      } else {
         html.replace("%pages%", "");
      }

      if (page >= pages) {
         page = pages - 1;
      }

      int start = 0;
      if (page > 0) {
         start = 20 * page;
      }

      int i = 0;
      StringBuilder sb = new StringBuilder(Math.min(20, skills.size()) * 550);

      for(Skill skill : skills.values()) {
         if (i < start) {
            ++i;
         } else {
            sb.append("<table width=280 height=32 cellspacing=1 background=\"L2UI_CT1.Windows.Windows_DF_TooltipBG\">");
            sb.append("<tr><td fixwidth=32 background=\"" + skill.getIcon() + "\"></td>");
            sb.append("<td fixwidth=140>");
            sb.append(activeChar.getSkillName(skill));
            sb.append("</td>");
            sb.append("<td fixwidth=45 align=center>");
            sb.append(skill.getId());
            sb.append("</td>");
            sb.append("<td fixwidth=35 align=center>");
            sb.append(skill.getLevel());
            sb.append("</td></tr></table>");
            if (++i >= 20 + start) {
               break;
            }
         }
      }

      html.replace("%skills%", sb.toString());
      activeChar.sendPacket(html);
   }

   protected void showNpcDropList(Player player, Npc npc, NpcTemplate tpl, int page, ChampionTemplate championTemplate) {
      String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/admin/editnpc-rewardlist_info.htm");
      String template = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/admin/editnpc-rewardlist_template.htm");
      String block = "";
      String list = "";
      double penaltyMod = npc != null ? ExperienceParser.getInstance().penaltyModifier((long)npc.calculateLevelDiffForDrop(player.getLevel()), 9.0) : 1.0;
      List<CalculateRewardChances.DropInfoTemplate> allItems = CalculateRewardChances.getAmountAndChance(player, tpl, penaltyMod, true, championTemplate);
      if (allItems.isEmpty()) {
         NpcHtmlMessage htm = new NpcHtmlMessage(0);
         htm.setFile(player, player.getLang(), "data/html/admin/editnpc-rewardlist_empty.htm");
         htm.replace("%npc_name%", player.getNpcName(tpl));
         player.sendPacket(htm);
      } else {
         Comparator<CalculateRewardChances.DropInfoTemplate> statsComparator = new EditNpc.SortDropInfo();
         Collections.sort(allItems, statsComparator);
         int perpage = 7;
         int counter = 0;
         int totalSize = allItems.size();
         boolean isThereNextPage = totalSize > 7;

         for(int i = (page - 1) * 7; i < totalSize; ++i) {
            CalculateRewardChances.DropInfoTemplate data = allItems.get(i);
            if (data != null) {
               String icon = data._item.getItem().getIcon();
               if (icon == null || icon.equals("")) {
                  icon = "icon.etc_question_mark_i00";
               }

               block = template.replace("{name}", player.getItemName(data._item.getItem()));
               block = block.replace("{icon}", icon);
               block = block.replace(
                  "{count}", data._maxCount > data._minCount ? "" + data._minCount + " - " + data._maxCount + "" : String.valueOf(data._minCount)
               );
               block = block.replace("{chance}", pf.format(data._chance));
               list = list + block;
            }

            if (++counter >= 7) {
               break;
            }
         }

         double pages = (double)totalSize / 7.0;
         int count = (int)Math.ceil(pages);
         html = html.replace("{list}", list);
         html = html.replace(
            "{navigation}", Util.getNavigationBlock(count, page, totalSize, 7, isThereNextPage, "admin_show_drop_list " + tpl.getId() + " %s")
         );
         html = html.replace("%npc_name%", player.getNpcName(tpl));
         allItems.clear();
         Util.setHtml(html, player);
      }
   }

   protected void showNpcSpoilList(Player player, Npc npc, NpcTemplate tpl, int page, ChampionTemplate championTemplate) {
      double penaltyMod = npc != null ? ExperienceParser.getInstance().penaltyModifier((long)npc.calculateLevelDiffForDrop(player.getLevel()), 9.0) : 1.0;
      List<CalculateRewardChances.DropInfoTemplate> allItems = CalculateRewardChances.getAmountAndChance(player, tpl, penaltyMod, false, championTemplate);
      if (allItems.isEmpty()) {
         NpcHtmlMessage html = new NpcHtmlMessage(0);
         html.setFile(player, player.getLang(), "data/html/admin/editnpc-spoillist_empty.htm");
         html.replace("%npc_name%", player.getNpcName(tpl));
         player.sendPacket(html);
      } else {
         Comparator<CalculateRewardChances.DropInfoTemplate> statsComparator = new EditNpc.SortDropInfo();
         Collections.sort(allItems, statsComparator);
         String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/admin/editnpc-rewardlist_info.htm");
         String template = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/admin/editnpc-rewardlist_template.htm");
         String block = "";
         String list = "";
         int perpage = 7;
         int counter = 0;
         int totalSize = allItems.size();
         boolean isThereNextPage = totalSize > 7;

         for(int i = (page - 1) * 7; i < totalSize; ++i) {
            CalculateRewardChances.DropInfoTemplate data = allItems.get(i);
            if (data != null) {
               String icon = data._item.getItem().getIcon();
               if (icon == null || icon.equals("")) {
                  icon = "icon.etc_question_mark_i00";
               }

               block = template.replace("{name}", player.getItemName(data._item.getItem()));
               block = block.replace("{icon}", icon);
               block = block.replace(
                  "{count}", data._maxCount > data._minCount ? "" + data._minCount + " - " + data._maxCount + "" : String.valueOf(data._minCount)
               );
               block = block.replace("{chance}", pf.format(data._chance));
               list = list + block;
            }

            if (++counter >= 7) {
               break;
            }
         }

         double pages = (double)totalSize / 7.0;
         int count = (int)Math.ceil(pages);
         html = html.replace("{list}", list);
         html = html.replace(
            "{navigation}", Util.getNavigationBlock(count, page, totalSize, 7, isThereNextPage, "admin_show_spoil_list " + tpl.getId() + " %s")
         );
         html = html.replace("%npc_name%", player.getNpcName(tpl));
         allItems.clear();
         Util.setHtml(html, player);
      }
   }

   protected void showNpcInfoList(Player player, Npc npc) {
      NpcHtmlMessage html = new NpcHtmlMessage(0);
      html.setFile(player, player.getLang(), "data/html/admin/npcinfo.htm");
      html.replace("%class%", npc.getClass().getSimpleName());
      html.replace("%id%", String.valueOf(npc.getTemplate().getId()));
      html.replace("%lvl%", String.valueOf(npc.getTemplate().getLevel()));
      html.replace("%name%", String.valueOf(npc.getTemplate().getName()));
      html.replace("%tmplid%", String.valueOf(npc.getTemplate().getId()));
      html.replace("%aggro%", String.valueOf(npc instanceof Attackable ? ((Attackable)npc).getAggroRange() : 0));
      html.replace("%hp%", String.valueOf((int)npc.getCurrentHp()));
      html.replace("%hpmax%", String.valueOf((int)npc.getMaxHp()));
      html.replace("%mp%", String.valueOf((int)npc.getCurrentMp()));
      html.replace("%mpmax%", String.valueOf((int)npc.getMaxMp()));
      html.replace("%refId%", String.valueOf(npc.getReflectionId()));
      html.replace("%loc%", String.valueOf(npc.getX() + " " + npc.getY() + " " + npc.getZ()));
      html.replace("%heading%", String.valueOf(npc.getHeading()));
      html.replace("%collision_radius%", String.valueOf(npc.getTemplate().getfCollisionRadius()));
      html.replace("%collision_height%", String.valueOf(npc.getTemplate().getfCollisionHeight()));
      html.replace("%dist%", String.valueOf((int)Math.sqrt(player.getDistanceSq(npc))));
      html.replace("%region%", npc.getWorldRegion() != null ? npc.getWorldRegion().getName() : "<font color=FF0000>null</font>");
      if (npc.getSpawnedLoc() != null) {
         html.replace("%spawn%", npc.getSpawnedLoc().getX() + " " + npc.getSpawnedLoc().getY() + " " + npc.getSpawnedLoc().getZ());
         html.replace("%loc2d%", String.valueOf((int)Math.sqrt(npc.getPlanDistanceSq(npc.getSpawnedLoc().getX(), npc.getSpawnedLoc().getY()))));
         html.replace(
            "%loc3d%", String.valueOf((int)Math.sqrt(npc.getDistanceSq(npc.getSpawnedLoc().getX(), npc.getSpawnedLoc().getY(), npc.getSpawnedLoc().getZ())))
         );
      } else {
         html.replace("%spawn%", "<font color=FF0000>null</font>");
         html.replace("%loc2d%", "<font color=FF0000>--</font>");
         html.replace("%loc3d%", "<font color=FF0000>--</font>");
      }

      if (npc.getSpawn() != null) {
         if (npc.getSpawn().getRespawnMinDelay() == 0) {
            html.replace("%resp%", "None");
         } else if (npc.getSpawn().hasRespawnRandom()) {
            html.replace("%resp%", npc.getSpawn().getRespawnMinDelay() / 1000 + "-" + npc.getSpawn().getRespawnMaxDelay() / 1000 + " sec");
         } else {
            html.replace("%resp%", npc.getSpawn().getRespawnMinDelay() / 1000 + " sec");
         }

         html.replace(
            "%territory%",
            npc.getSpawn().getTerritoryName() != null && !npc.getSpawn().getTerritoryName().isEmpty()
               ? npc.getSpawn().getTerritoryName()
               : "<font color=FF0000>--</font>"
         );
      } else {
         html.replace("%resp%", "<font color=FF0000>--</font>");
         html.replace("%territory%", "<font color=FF0000>--</font>");
      }

      if (npc.hasAI()) {
         html.replace(
            "%ai_intention%",
            "<tr><td><table width=270 border=0 bgcolor=131210><tr><td width=100><font color=FFAA00>Intention:</font></td><td align=right width=170>"
               + npc.getAI().getIntention().name()
               + "</td></tr></table></td></tr>"
         );
         html.replace(
            "%ai_type%",
            "<tr><td><table width=270 border=0><tr><td width=100><font color=FFAA00>AIType</font></td><td align=right width=170>"
               + npc.getAiType()
               + "</td></tr></table></td></tr>"
         );
         html.replace(
            "%ai_clan%",
            "<tr><td><table width=270 border=0 bgcolor=131210><tr><td width=100><font color=FFAA00>Clan & Range:</font></td><td align=right width=170>"
               + npc.getFaction().getName()
               + " "
               + npc.getFaction().getRange()
               + "</td></tr></table></td></tr>"
         );
      } else {
         html.replace("%ai_intention%", "");
         html.replace("%ai_type%", "");
         html.replace("%ai_clan%", "");
      }

      String routeName = WalkingManager.getInstance().getRouteName(npc);
      if (!routeName.isEmpty()) {
         html.replace(
            "%route%",
            "<tr><td><table width=270 border=0><tr><td width=100><font color=LEVEL>Route:</font></td><td align=right width=170>"
               + routeName
               + "</td></tr></table></td></tr>"
         );
      } else {
         html.replace("%route%", "");
      }

      player.sendPacket(html);
   }

   protected void showNpcStatList(Player player, Npc npc) {
      NpcHtmlMessage html = new NpcHtmlMessage(0);
      html.setFile(player, player.getLang(), "data/html/admin/npcstats.htm");
      html.replace("%id%", String.valueOf(npc.getTemplate().getId()));
      html.replace("%lvl%", String.valueOf(npc.getTemplate().getLevel()));
      html.replace("%name%", String.valueOf(npc.getTemplate().getName()));
      html.replace("%tmplid%", String.valueOf(npc.getTemplate().getId()));
      html.replace("%patk%", String.valueOf((int)npc.getPAtk(null)));
      html.replace("%matk%", String.valueOf((int)npc.getMAtk(null, null)));
      html.replace("%pdef%", String.valueOf((int)npc.getPDef(null)));
      html.replace("%mdef%", String.valueOf((int)npc.getMDef(null, null)));
      html.replace("%accu%", String.valueOf(npc.getAccuracy()));
      html.replace("%evas%", String.valueOf(npc.getEvasionRate(null)));
      html.replace("%crit%", String.valueOf((int)npc.getCriticalHit(null, null)));
      html.replace("%rspd%", String.valueOf((int)npc.getRunSpeed()));
      html.replace("%aspd%", String.valueOf((int)npc.getPAtkSpd()));
      html.replace("%cspd%", String.valueOf((int)npc.getMAtkSpd()));
      html.replace("%atkType%", String.valueOf(npc.getTemplate().getBaseAttackType()));
      html.replace("%atkRng%", String.valueOf(npc.getTemplate().getBaseAttackRange()));
      html.replace("%str%", String.valueOf(npc.getSTR()));
      html.replace("%dex%", String.valueOf(npc.getDEX()));
      html.replace("%con%", String.valueOf(npc.getCON()));
      html.replace("%int%", String.valueOf(npc.getINT()));
      html.replace("%wit%", String.valueOf(npc.getWIT()));
      html.replace("%men%", String.valueOf(npc.getMEN()));
      byte attackAttribute = npc.getAttackElement();
      html.replace("%ele_atk%", Elementals.getElementName(attackAttribute));
      html.replace("%ele_atk_value%", String.valueOf(npc.getAttackElementValue(attackAttribute)));
      html.replace("%ele_dfire%", String.valueOf(npc.getDefenseElementValue((byte)0)));
      html.replace("%ele_dwater%", String.valueOf(npc.getDefenseElementValue((byte)1)));
      html.replace("%ele_dwind%", String.valueOf(npc.getDefenseElementValue((byte)2)));
      html.replace("%ele_dearth%", String.valueOf(npc.getDefenseElementValue((byte)3)));
      html.replace("%ele_dholy%", String.valueOf(npc.getDefenseElementValue((byte)4)));
      html.replace("%ele_ddark%", String.valueOf(npc.getDefenseElementValue((byte)5)));
      player.sendPacket(html);
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }

   static {
      pf.setMaximumFractionDigits(4);
   }

   private static class SortDropInfo implements Comparator<CalculateRewardChances.DropInfoTemplate>, Serializable {
      private static final long serialVersionUID = 7691414259610932752L;

      private SortDropInfo() {
      }

      public int compare(CalculateRewardChances.DropInfoTemplate o1, CalculateRewardChances.DropInfoTemplate o2) {
         return Double.compare(o2._chance, o1._chance);
      }
   }
}
