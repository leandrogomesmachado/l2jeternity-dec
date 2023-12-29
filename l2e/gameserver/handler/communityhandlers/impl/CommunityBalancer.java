package l2e.gameserver.handler.communityhandlers.impl;

import java.util.Collection;
import java.util.StringTokenizer;
import java.util.Map.Entry;
import l2e.gameserver.Config;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.data.parser.ClassBalanceParser;
import l2e.gameserver.handler.communityhandlers.ICommunityBoardHandler;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.base.AttackType;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.holders.ClassBalanceHolder;

public class CommunityBalancer extends AbstractCommunity implements ICommunityBoardHandler {
   public CommunityBalancer() {
      if (Config.DEBUG) {
         _log.info(this.getClass().getSimpleName() + ": Loading all functions.");
      }
   }

   @Override
   public String[] getBypassCommands() {
      return new String[]{
         "classbalance",
         "_bbs_balancer",
         "_bbs_save_classbalance",
         "_bbs_remove_classbalance",
         "_bbs_modify_classbalance",
         "_bbs_add_menu_classbalance",
         "_bbs_add_classbalance",
         "_bbs_search_classbalance",
         "_bbs_search_nav_classbalance"
      };
   }

   private static void showMainWindow(Player activeChar) {
      String html = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/mods/balancer/index.htm");
      separateAndSend(html, activeChar);
   }

   @Override
   public void onBypassCommand(String command, Player activeChar) {
      if (Config.BALANCER_ALLOW && activeChar.isGM()) {
         if (command.equals("_bbs_balancer")) {
            showMainWindow(activeChar);
         } else if (command.startsWith("_bbs_classbalance")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            int pageId = st.countTokens() == 2 ? Integer.parseInt(st.nextToken()) : 1;
            boolean isOly = Boolean.parseBoolean(st.nextToken());
            this.showMainHtml(activeChar, pageId, isOly);
         } else if (command.startsWith("_bbs_save_classbalance")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            int pageId = Integer.parseInt(st.nextToken());
            boolean isOly = Boolean.parseBoolean(st.nextToken());
            ClassBalanceParser.getInstance().store(activeChar);
            this.showMainHtml(activeChar, pageId, isOly);
         } else if (command.startsWith("_bbs_remove_classbalance")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            String key = st.nextToken();
            int pageId = Integer.parseInt(st.nextToken());
            int type = Integer.valueOf(st.nextToken());
            boolean isOly = Boolean.parseBoolean(st.nextToken());
            ClassBalanceParser.getInstance().removeClassBalance(key, AttackType.VALUES[type], isOly);
            this.showMainHtml(activeChar, pageId, isOly);
         } else if (command.startsWith("_bbs_modify_classbalance")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            String[] array = st.nextToken().split(";");
            int classId = Integer.valueOf(array[0]);
            int targetClassId = Integer.valueOf(array[1]);
            int attackType = Integer.valueOf(array[2]);
            double value = Double.parseDouble(array[3]);
            int pageId = Integer.parseInt(array[4]);
            boolean isSearch = Boolean.parseBoolean(array[5]);
            boolean isOly = Boolean.parseBoolean(array[6]);
            String key = classId + ";" + targetClassId;
            ClassBalanceHolder cbh = ClassBalanceParser.getInstance().getBalanceHolder(key);
            if (isOly) {
               cbh.addOlyBalance(AttackType.VALUES[attackType], value);
            } else {
               cbh.addNormalBalance(AttackType.VALUES[attackType], value);
            }

            ClassBalanceParser.getInstance().addClassBalance(key, cbh, true);
            if (isSearch) {
               this.showSearchHtml(activeChar, pageId, Integer.valueOf(classId), isOly);
            } else {
               this.showMainHtml(activeChar, pageId, isOly);
            }
         } else if (command.startsWith("_bbs_add_menu_classbalance")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            int pageId = Integer.parseInt(st.nextToken());
            int race = Integer.parseInt(st.nextToken());
            int tRace = Integer.parseInt(st.nextToken());
            boolean isOly = Boolean.parseBoolean(st.nextToken());
            this.showAddHtml(activeChar, pageId, race, tRace, isOly);
         } else if (command.startsWith("_bbs_add_classbalance")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            String className = st.nextToken().trim();
            String attackTypeSt = st.nextToken();
            String val = st.nextToken();
            String targetClassName = st.nextToken().trim();
            boolean isoly = Boolean.parseBoolean(st.nextToken());
            int classId = -1;
            if (!className.equals("")) {
               ClassId[] values = ClassId.values();

               for(int key = 0; key < values.length; ++key) {
                  ClassId cId = values[key];
                  if (cId.name().equalsIgnoreCase(className)) {
                     classId = cId.ordinal();
                  }
               }
            }

            int targetClassId = targetClassName.equals("All") ? -2 : -1;
            if (!targetClassName.equals("")) {
               ClassId[] values = ClassId.values();

               for(int key = 0; key < values.length; ++key) {
                  ClassId cId = values[key];
                  if (cId.name().equalsIgnoreCase(targetClassName)) {
                     targetClassId = cId.ordinal();
                  }
               }
            }

            targetClassId = AttackType.valueOf(attackTypeSt).isOnlyVsAll() ? -2 : targetClassId;
            double value = Double.parseDouble(val);
            String key = classId + ";" + targetClassId;
            ClassBalanceHolder cbh = ClassBalanceParser.getInstance().getBalanceHolder(key) != null
               ? ClassBalanceParser.getInstance().getBalanceHolder(key)
               : new ClassBalanceHolder(classId, targetClassId);
            if (isoly) {
               cbh.addOlyBalance(AttackType.valueOf(attackTypeSt), value);
            } else {
               cbh.addNormalBalance(AttackType.valueOf(attackTypeSt), value);
            }

            ClassBalanceParser.getInstance().addClassBalance(key, cbh, false);
            this.showMainHtml(activeChar, 1, isoly);
         } else if (command.startsWith("_bbs_search_classbalance")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            if (st.countTokens() == 2) {
               int classId = Integer.valueOf(st.nextToken());
               boolean isOly = Boolean.parseBoolean(st.nextToken());
               this.showSearchHtml(activeChar, 1, classId, isOly);
            }
         } else if (command.startsWith("_bbs_search_nav_classbalance")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            if (st.countTokens() == 3) {
               int classId = Integer.valueOf(st.nextToken());
               int pageID = Integer.valueOf(st.nextToken());
               boolean isOly = Boolean.parseBoolean(st.nextToken());
               this.showSearchHtml(activeChar, pageID, classId, isOly);
            }
         }
      }
   }

   public void showMainHtml(Player activeChar, int pageId, boolean isolyinfo) {
      String html = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/mods/balancer/classbalance/index.htm");
      String info = getBalanceInfo(activeChar, ClassBalanceParser.getInstance().getAllBalances().values(), pageId, false, isolyinfo);
      int count = ClassBalanceParser.getInstance().getSize(isolyinfo);
      int limitInPage = 7;
      html = html.replace("<?title?>", isolyinfo ? "Olympiad" : "");
      html = html.replace("<?isoly?>", String.valueOf(isolyinfo));
      html = html.replace("%pageID%", String.valueOf(pageId));
      int totalpages = 1;

      for(int tmpcount = count; tmpcount - 7 > 0; tmpcount -= 7) {
         ++totalpages;
      }

      html = html.replace("%totalPages%", String.valueOf(totalpages));
      html = html.replace("%info%", info);
      html = html.replace("%previousPage%", String.valueOf(pageId - 1 != 0 ? pageId - 1 : 1));
      html = html.replace("%nextPage%", String.valueOf(pageId * 7 >= count ? pageId : pageId + 1));
      separateAndSend(html, activeChar);
   }

   public void showAddHtml(Player activeChar, int pageId, int race, int tRace, boolean isOly) {
      String html = HtmCache.getInstance()
         .getHtm(activeChar, activeChar.getLang(), "data/html/mods/balancer/classbalance/" + (isOly ? "olyadd.htm" : "add.htm"));
      String classes = "";
      if (race < 6) {
         ClassId[] array = ClassId.values();

         for(int cId = 0; cId < array.length; ++cId) {
            ClassId classId = array[cId];
            if (classId.getRace() != null) {
               if (isOly) {
                  if (classId.level() == 3 && classId.getRace().ordinal() == race) {
                     classes = classes + classId.name() + ";";
                  }
               } else if (classId.level() >= 2 && classId.getRace().ordinal() == race) {
                  classes = classes + classId.name() + ";";
               }
            }
         }
      } else {
         classes = race == 6 ? "Monsters" : "All";
      }

      String tClasses = "";
      if (tRace < 6) {
         ClassId[] array2 = ClassId.values();

         for(int cId = 0; cId < array2.length; ++cId) {
            ClassId classId = array2[cId];
            if (classId.getRace() != null) {
               if (isOly) {
                  if (classId.level() == 3 && classId.getRace().ordinal() == tRace) {
                     tClasses = tClasses + classId.name() + ";";
                  }
               } else if (classId.level() >= 2 && classId.getRace().ordinal() == tRace) {
                  tClasses = tClasses + classId.name() + ";";
               }
            }
         }
      } else {
         tClasses = tRace == 6 ? "Monsters" : "All";
      }

      html = html.replace("<?pageId?>", String.valueOf(pageId));
      html = html.replace("<?tRace?>", String.valueOf(tRace));
      html = html.replace("<?race0Checked?>", race == 0 ? "_checked" : "");
      html = html.replace("<?race1Checked?>", race == 1 ? "_checked" : "");
      html = html.replace("<?race2Checked?>", race == 2 ? "_checked" : "");
      html = html.replace("<?race3Checked?>", race == 3 ? "_checked" : "");
      html = html.replace("<?race4Checked?>", race == 4 ? "_checked" : "");
      html = html.replace("<?race5Checked?>", race == 5 ? "_checked" : "");
      html = html.replace("<?race7Checked?>", race == 7 ? "_checked" : "");
      html = html.replace("<?classes?>", classes);
      html = html.replace("<?tClasses?>", tClasses);
      html = html.replace("<?race?>", String.valueOf(race));
      html = html.replace("<?trace0Checked?>", tRace == 0 ? "_checked" : "");
      html = html.replace("<?trace1Checked?>", tRace == 1 ? "_checked" : "");
      html = html.replace("<?trace2Checked?>", tRace == 2 ? "_checked" : "");
      html = html.replace("<?trace3Checked?>", tRace == 3 ? "_checked" : "");
      html = html.replace("<?trace4Checked?>", tRace == 4 ? "_checked" : "");
      html = html.replace("<?trace5Checked?>", tRace == 5 ? "_checked" : "");
      html = html.replace("<?trace6Checked?>", tRace == 6 ? "_checked" : "");
      html = html.replace("<?trace7Checked?>", tRace == 7 ? "_checked" : "");
      html = html.replace("<?isoly?>", String.valueOf(isOly));
      separateAndSend(html, activeChar);
   }

   public void showSearchHtml(Player activeChar, int pageId, int sclassId, boolean isolysearch) {
      String html = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/mods/balancer/classbalance/search.htm");
      String info = getBalanceInfo(activeChar, ClassBalanceParser.getInstance().getClassBalances(sclassId), pageId, true, isolysearch);
      int count = ClassBalanceParser.getInstance().getClassBalanceSize(sclassId, isolysearch);
      int limitInPage = 7;
      html = html.replace("%pageID%", String.valueOf(pageId));
      int totalpages = 1;

      for(int tmpcount = count; tmpcount - 7 > 0; tmpcount -= 7) {
         ++totalpages;
      }

      html = html.replace("<?title?>", isolysearch ? "Olympiad" : "");
      html = html.replace("<?isoly?>", String.valueOf(isolysearch));
      html = html.replace("%totalPages%", String.valueOf(totalpages));
      html = html.replace("%info%", info);
      html = html.replace("%classID%", String.valueOf(sclassId));
      html = html.replace("%previousPage%", String.valueOf(pageId - 1 != 0 ? pageId - 1 : 1));
      html = html.replace("%nextPage%", String.valueOf(pageId * 7 >= count ? pageId : pageId + 1));
      separateAndSend(html, activeChar);
   }

   private static String getBalanceInfo(Player activeChar, Collection<ClassBalanceHolder> collection, int pageId, boolean search, boolean isOly) {
      if (collection == null) {
         return "";
      } else {
         String info = "";
         int count = 1;
         int limitInPage = 7;

         for(ClassBalanceHolder balance : collection) {
            int classId = balance.getActiveClass();
            int targetClassId = balance.getTargetClass();
            String id = classId + ";" + targetClassId;
            if (!ClassId.getClassById(classId).name().equals("") && !ClassId.getClassById(targetClassId).name().equals("")
               || !ClassId.getClassById(classId).name().equals("")
               || targetClassId == -1) {
               for(Entry<AttackType, Double> dt : isOly ? balance.getOlyBalance().entrySet() : balance.getNormalBalance().entrySet()) {
                  if (count > 7 * (pageId - 1) && count <= 7 * pageId) {
                     double val = dt.getValue();
                     double percents = (double)(Math.round(val * 100.0) - 100L);
                     double addedValue = (double)Math.round((val + 0.1) * 10.0) / 10.0;
                     double removedValue = (double)Math.round((val - 0.1) * 10.0) / 10.0;
                     String attackTypeSt = dt.getKey().name();
                     String content = HtmCache.getInstance()
                        .getHtm(activeChar, activeChar.getLang(), "data/html/mods/balancer/classbalance/info-template.htm");
                     content = content.replace("<?pos?>", String.valueOf(count));
                     content = content.replace("<?classId?>", String.valueOf(classId));
                     content = content.replace("<?className?>", classId <= -1 ? "(All)" : (classId == -1 ? "Monster" : ClassId.getClassById(classId).name()));
                     content = content.replace("<?type?>", attackTypeSt);
                     content = content.replace("<?key?>", id);
                     content = content.replace("<?targetClassId?>", String.valueOf(targetClassId));
                     content = content.replace("<?editedType?>", String.valueOf(dt.getKey().getId()));
                     content = content.replace("<?removedValue?>", String.valueOf(removedValue));
                     content = content.replace("<?search?>", String.valueOf(search));
                     content = content.replace("<?isoly?>", String.valueOf(isOly));
                     content = content.replace("<?addedValue?>", String.valueOf(addedValue));
                     content = content.replace("<?pageId?>", String.valueOf(pageId));
                     content = content.replace(
                        "<?targetClassName?>", targetClassId <= -1 ? "(All)" : (targetClassId == -1 ? "Monster" : ClassId.getClassById(targetClassId).name())
                     );
                     content = content.replace("<?value?>", String.valueOf(val));
                     content = content.replace("<?percents?>", percents > 0.0 ? "+" : "");
                     content = content.replace("<?percentValue?>", String.valueOf(percents).substring(0, String.valueOf(percents).indexOf(".")));
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

   public static CommunityBalancer getInstance() {
      return CommunityBalancer.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final CommunityBalancer _instance = new CommunityBalancer();
   }
}
