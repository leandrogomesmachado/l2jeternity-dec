package l2e.gameserver.handler.communityhandlers.impl;

import java.util.Collection;
import java.util.StringTokenizer;
import java.util.Map.Entry;
import l2e.gameserver.Config;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.data.parser.SkillBalanceParser;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.handler.communityhandlers.ICommunityBoardHandler;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.base.SkillChangeType;
import l2e.gameserver.model.holders.SkillBalanceHolder;
import l2e.gameserver.model.skills.Skill;

public class CommunityBalancerSkill extends AbstractCommunity implements ICommunityBoardHandler {
   public CommunityBalancerSkill() {
      if (Config.DEBUG) {
         _log.info(this.getClass().getSimpleName() + ": Loading all functions.");
      }
   }

   @Override
   public String[] getBypassCommands() {
      return new String[]{
         "skillbalance",
         "_bbs_skillbalance",
         "_bbs_save_skillbalance",
         "_bbs_remove_skillbalance",
         "_bbs_modify_skillbalance",
         "_bbs_add_menu_skillbalance",
         "_bbs_add_skillbalance",
         "_bbs_search_skillbalance",
         "_bbs_search_nav_skillbalance",
         "_bbs_get_skillbalance"
      };
   }

   @Override
   public void onBypassCommand(String command, Player activeChar) {
      if (Config.BALANCER_ALLOW && activeChar.isGM()) {
         if (command.startsWith("_bbs_skillbalance")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            int pageId = st.countTokens() == 2 ? Integer.parseInt(st.nextToken()) : 1;
            boolean isOly = Boolean.parseBoolean(st.nextToken());
            this.showMainHtml(activeChar, pageId, isOly);
         } else if (command.startsWith("_bbs_save_skillbalance")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            int pageId = Integer.parseInt(st.nextToken());
            boolean isOly = Boolean.parseBoolean(st.nextToken());
            SkillBalanceParser.getInstance().store(activeChar);
            this.showMainHtml(activeChar, pageId, isOly);
         } else if (command.startsWith("_bbs_remove_skillbalance")) {
            String[] info = command.substring(25).split(" ");
            String key = info[0];
            int pageId = info.length > 1 ? Integer.parseInt(info[1]) : 1;
            int type = Integer.valueOf(info[2]);
            boolean isOly = Boolean.parseBoolean(info[3]);
            SkillBalanceParser.getInstance().removeSkillBalance(key, SkillChangeType.VALUES[type], isOly);
            this.showMainHtml(activeChar, pageId, isOly);
         } else if (command.startsWith("_bbs_modify_skillbalance")) {
            String[] st = command.split(";");
            int skillId = Integer.valueOf(st[0].substring(25));
            int target = Integer.valueOf(st[1]);
            int changeType = Integer.valueOf(st[2]);
            double value = Double.parseDouble(st[3]);
            int pageId = Integer.parseInt(st[4]);
            boolean isSearch = Boolean.parseBoolean(st[5]);
            boolean isOly = Boolean.parseBoolean(st[6]);
            String key = skillId + ";" + target;
            SkillBalanceHolder cbh = SkillBalanceParser.getInstance().getSkillHolder(key);
            if (isOly) {
               cbh.addOlySkillBalance(SkillChangeType.VALUES[changeType], value);
            } else {
               cbh.addSkillBalance(SkillChangeType.VALUES[changeType], value);
            }

            SkillBalanceParser.getInstance().addSkillBalance(key, cbh, true);
            if (isSearch) {
               this.showSearchHtml(activeChar, pageId, skillId, isOly);
            } else {
               this.showMainHtml(activeChar, pageId, isOly);
            }
         } else if (command.startsWith("_bbs_add_menu_skillbalance")) {
            StringTokenizer st = new StringTokenizer(command.substring(27), " ");
            int pageId = Integer.parseInt(st.nextToken());
            int tRace = Integer.parseInt(st.nextToken());
            boolean isOly = Boolean.parseBoolean(st.nextToken());
            this.showAddHtml(activeChar, pageId, tRace, isOly);
         } else if (command.startsWith("_bbs_add_skillbalance")) {
            String[] st = command.substring(22).split(";");
            StringTokenizer st2 = new StringTokenizer(command.substring(22), ";");
            if (st2.countTokens() != 5 || st[0].isEmpty() || st[1].isEmpty() || st[2].isEmpty() || st[3].isEmpty() || st[4].isEmpty()) {
               activeChar.sendMessage("Incorrect input count.");
               return;
            }

            int skillId = Integer.valueOf(st[0].trim());
            String attackTypeSt = st[1].trim();
            String val = st[2].trim();
            String targetClassName = st[3].trim();
            boolean isoly = Boolean.parseBoolean(st[4].trim());
            double value = Double.parseDouble(val);
            if (SkillsParser.getInstance().getInfo(skillId, SkillsParser.getInstance().getMaxLevel(skillId)) == null) {
               activeChar.sendMessage("Skill with id: " + skillId + " not found!");
               return;
            }

            int targetClassId = targetClassName.equals("All") ? -2 : -1;
            if (!targetClassName.equals("")) {
               for(ClassId cId : ClassId.values()) {
                  if (cId.name().equalsIgnoreCase(targetClassName)) {
                     targetClassId = cId.ordinal();
                  }
               }
            }

            targetClassId = SkillChangeType.valueOf(attackTypeSt).isOnlyVsAll() ? -2 : targetClassId;
            String key = skillId + ";" + targetClassId;
            SkillBalanceHolder cbh = SkillBalanceParser.getInstance().getSkillHolder(key) != null
               ? SkillBalanceParser.getInstance().getSkillHolder(key)
               : new SkillBalanceHolder(skillId, targetClassId);
            if (isoly) {
               cbh.addOlySkillBalance(SkillChangeType.valueOf(attackTypeSt), value);
            } else {
               cbh.addSkillBalance(SkillChangeType.valueOf(attackTypeSt), value);
            }

            SkillBalanceParser.getInstance().addSkillBalance(key, cbh, isoly);
            this.showMainHtml(activeChar, 1, isoly);
         } else if (command.startsWith("_bbs_search_skillbalance")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            if (st.countTokens() == 2) {
               int skillId = Integer.valueOf(st.nextToken());
               boolean isOly = Boolean.parseBoolean(st.nextToken());
               this.showSearchHtml(activeChar, 1, skillId, isOly);
            }
         } else if (command.startsWith("_bbs_search_nav_skillbalance")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            if (st.countTokens() == 3) {
               int skillId = Integer.valueOf(st.nextToken());
               int pageID = Integer.valueOf(st.nextToken());
               boolean isOly = Boolean.parseBoolean(st.nextToken());
               this.showSearchHtml(activeChar, pageID, skillId, isOly);
            }
         } else if (command.startsWith("_bbs_get_skillbalance")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            if (st.hasMoreTokens()) {
               int skillId = Integer.valueOf(st.nextToken());
               Skill skill = SkillsParser.getInstance().getInfo(skillId, SkillsParser.getInstance().getMaxLevel(skillId));
               if (skill != null) {
                  activeChar.addSkill(skill);
                  activeChar.sendMessage("You have learned: " + skill.getNameEn());
               }
            }
         }
      }
   }

   public void showMainHtml(Player activeChar, int pageId, boolean isolyinfo) {
      String html = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/mods/balancer/skillbalance/index.htm");
      String info = getSkillBalanceInfo(activeChar, SkillBalanceParser.getInstance().getAllBalances().values(), pageId, false, isolyinfo);
      int count = SkillBalanceParser.getInstance().getSize(isolyinfo);
      int limitInPage = 6;
      html = html.replace("<?title?>", isolyinfo ? "Olympiad" : "");
      html = html.replace("<?isoly?>", String.valueOf(isolyinfo));
      html = html.replace("%pageID%", String.valueOf(pageId));
      int totalpages = 1;

      for(int tmpcount = count; tmpcount - 6 > 0; tmpcount -= 6) {
         ++totalpages;
      }

      html = html.replace("%totalPages%", String.valueOf(totalpages));
      html = html.replace("%info%", info);
      html = html.replace("%previousPage%", String.valueOf(pageId - 1 != 0 ? pageId - 1 : 1));
      html = html.replace("%nextPage%", String.valueOf(pageId * 6 >= count ? pageId : pageId + 1));
      separateAndSend(html, activeChar);
   }

   public void showSearchHtml(Player activeChar, int pageId, int skillId, boolean isolysearch) {
      String html = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/mods/balancer/skillbalance/search.htm");
      String info = getSkillBalanceInfo(activeChar, SkillBalanceParser.getInstance().getSkillBalances(skillId), pageId, true, isolysearch);
      int count = SkillBalanceParser.getInstance().getSkillBalanceSize(skillId, isolysearch);
      int limitInPage = 6;
      html = html.replace("%pageID%", String.valueOf(pageId));
      int totalpages = 1;

      for(int tmpcount = count; tmpcount - 6 > 0; tmpcount -= 6) {
         ++totalpages;
      }

      html = html.replace("<?title?>", isolysearch ? "Olympiad" : "");
      html = html.replace("<?isoly?>", String.valueOf(isolysearch));
      html = html.replace("%totalPages%", String.valueOf(totalpages));
      html = html.replace("%info%", info);
      html = html.replace("%skillId%", String.valueOf(skillId));
      html = html.replace("%previousPage%", String.valueOf(pageId - 1 != 0 ? pageId - 1 : 1));
      html = html.replace("%nextPage%", String.valueOf(pageId * 6 >= count ? pageId : pageId + 1));
      separateAndSend(html, activeChar);
   }

   public void showAddHtml(Player activeChar, int pageId, int tRace, boolean isoly) {
      String html = HtmCache.getInstance()
         .getHtm(activeChar, activeChar.getLang(), "data/html/mods/balancer/skillbalance/" + (isoly ? "olyadd.htm" : "add.htm"));
      String tClasses = "";
      if (tRace < 6) {
         for(ClassId classId : ClassId.values()) {
            if (classId.getRace() != null && classId.level() == 3 && classId.getRace().ordinal() == tRace) {
               tClasses = tClasses + classId.name() + ";";
            }
         }
      } else {
         tClasses = tRace == 6 ? "Monsters" : "All";
      }

      html = html.replace("<?pageId?>", String.valueOf(pageId));
      html = html.replace("<?isoly?>", String.valueOf(isoly));
      html = html.replace("<?tClasses?>", tClasses);
      html = html.replace("<?trace0Checked?>", tRace == 0 ? "_checked" : "");
      html = html.replace("<?trace1Checked?>", tRace == 1 ? "_checked" : "");
      html = html.replace("<?trace2Checked?>", tRace == 2 ? "_checked" : "");
      html = html.replace("<?trace3Checked?>", tRace == 3 ? "_checked" : "");
      html = html.replace("<?trace4Checked?>", tRace == 4 ? "_checked" : "");
      html = html.replace("<?trace5Checked?>", tRace == 5 ? "_checked" : "");
      html = html.replace("<?trace6Checked?>", tRace == 6 ? "_checked" : "");
      html = html.replace("<?trace7Checked?>", tRace == 7 ? "_checked" : "");
      separateAndSend(html, activeChar);
   }

   private static String getSkillBalanceInfo(Player activeChar, Collection<SkillBalanceHolder> collection, int pageId, boolean search, boolean isOly) {
      if (collection == null) {
         return "";
      } else {
         String info = "";
         int count = 1;
         int limitInPage = 6;

         for(SkillBalanceHolder balance : collection) {
            int targetClassId = balance.getTarget();
            if (!ClassId.getClassById(targetClassId).name().equals("") || targetClassId <= -1) {
               for(Entry<SkillChangeType, Double> dt : isOly ? balance.getOlyBalance().entrySet() : balance.getNormalBalance().entrySet()) {
                  if (count > 6 * (pageId - 1) && count <= 6 * pageId) {
                     double val = dt.getValue();
                     double percents = (double)(Math.round(val * 100.0) - 100L);
                     double addedValue = (double)Math.round((val + 0.1) * 10.0) / 10.0;
                     double removedValue = (double)Math.round((val - 0.1) * 10.0) / 10.0;
                     String content = HtmCache.getInstance()
                        .getHtm(activeChar, activeChar.getLang(), "data/html/mods/balancer/skillbalance/info-template.htm");
                     content = content.replace("<?pos?>", String.valueOf(count));
                     content = content.replace("<?key?>", balance.getSkillId() + ";" + balance.getTarget());
                     content = content.replace("<?skillId?>", String.valueOf(balance.getSkillId()));
                     content = content.replace(
                        "<?skillName?>",
                        SkillsParser.getInstance().getInfo(balance.getSkillId(), SkillsParser.getInstance().getMaxLevel(balance.getSkillId())).getNameEn()
                     );
                     content = content.replace("<?type?>", dt.getKey().name());
                     content = content.replace("<?editedType?>", String.valueOf(dt.getKey().getId()));
                     content = content.replace("<?removedValue?>", String.valueOf(removedValue));
                     content = content.replace("<?search?>", String.valueOf(search));
                     content = content.replace("<?isoly?>", String.valueOf(isOly));
                     content = content.replace("<?addedValue?>", String.valueOf(addedValue));
                     content = content.replace("<?pageId?>", String.valueOf(pageId));
                     content = content.replace("<?value?>", String.valueOf(val));
                     content = content.replace(
                        "<?targetClassName?>", targetClassId <= -1 ? "All" : (targetClassId == -1 ? "Monster" : ClassId.getClassById(targetClassId).name())
                     );
                     content = content.replace("<?percents?>", percents > 0.0 ? "+" : "");
                     content = content.replace("<?percentValue?>", String.valueOf(percents).substring(0, String.valueOf(percents).indexOf(".")));
                     content = content.replace("<?targetId?>", String.valueOf(targetClassId));
                     content = content.replace("<?skillIcon?>", balance.getSkillIcon());
                     info = info + content;
                  }

                  ++count;
               }
            }
         }

         return info;
      }
   }

   @Override
   public void onWriteCommand(String command, String s, String s1, String s2, String s3, String s4, Player Player) {
   }

   public static CommunityBalancerSkill getInstance() {
      return CommunityBalancerSkill.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final CommunityBalancerSkill _instance = new CommunityBalancerSkill();
   }
}
