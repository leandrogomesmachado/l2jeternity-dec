package l2e.gameserver.model.entity.events.custom.achievements;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.player.AchiveTemplate;
import l2e.gameserver.model.strings.server.ServerStorage;
import l2e.gameserver.network.serverpackets.TutorialCloseHtml;
import l2e.gameserver.network.serverpackets.TutorialShowHtml;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class AchievementManager {
   private static Logger _log = Logger.getLogger(AchievementManager.class.getName());
   private final Map<Integer, Integer> _achievementMaxLevels = new ConcurrentHashMap<>();
   private final List<AchievementCategory> _achievementCategories = new LinkedList<>();
   private final Map<Integer, AchiveTemplate> _achKillById = new ConcurrentHashMap<>();
   private final Map<Integer, AchiveTemplate> _achRefById = new ConcurrentHashMap<>();
   private final Map<Integer, AchiveTemplate> _achQuestById = new ConcurrentHashMap<>();
   private final Map<Integer, AchiveTemplate> _achEnchantWeaponByLvl = new ConcurrentHashMap<>();
   private final Map<Integer, AchiveTemplate> _achEnchantArmorByLvl = new ConcurrentHashMap<>();
   private final Map<Integer, AchiveTemplate> _achEnchantJewerlyByLvl = new ConcurrentHashMap<>();
   private static AchievementManager _instance;
   private boolean _isActive;

   public AchievementManager() {
      this.load();
   }

   public void onBypass(Player player, String bypass, String[] cm) {
      if (bypass.startsWith("_bbs_achievements_cat")) {
         this.generatePage(player, Integer.parseInt(cm[1]), Integer.parseInt(cm[2]));
      } else if (bypass.equals("_bbs_achievements_close")) {
         player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
      } else if (bypass.startsWith("_bbs_achievements")) {
         this.checkAchievementRewards(player);
         this.generatePage(player);
      }
   }

   public void generatePage(Player player) {
      if (player != null) {
         String achievements = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/achievements/achievements.htm");
         String ac = "";

         for(AchievementCategory cat : this._achievementCategories) {
            ac = ac + cat.getHtml(player);
         }

         achievements = achievements.replace("%categories%", ac);
         player.sendPacket(new TutorialShowHtml(achievements));
      }
   }

   public void generatePage(Player player, int category, int page) {
      if (player != null) {
         String FULL_PAGE = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/achievements/inAchievements.htm");
         String achievementsNotDone = "";
         String achievementsDone = "";
         long playerPoints = 0L;
         int all = 0;
         int clansvisual = 0;
         boolean pagereached = false;
         int totalpages = (int)Math.ceil((double)player.getAchievements(category).size() / 3.0);
         FULL_PAGE = FULL_PAGE.replaceAll(
            "%back%",
            page == 1
               ? "&nbsp;"
               : "<button value=\"\" action=\"bypass _bbs_achievements_cat "
                  + category
                  + " "
                  + (page - 1)
                  + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateRight\">"
         );
         FULL_PAGE = FULL_PAGE.replaceAll(
            "%more%",
            totalpages <= page
               ? "&nbsp;"
               : "<button value=\"\" action=\"bypass _bbs_achievements_cat "
                  + category
                  + " "
                  + (page + 1)
                  + "\" width=40 height=20 back=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\" fore=\"L2UI_CT1.Inventory_DF_Btn_RotateLeft\">"
         );
         AchievementCategory cat = this._achievementCategories.stream().filter(ctg -> ctg.getCategoryId() == category).findAny().orElse(null);
         if (cat == null) {
            _log.warning("AchievementManager: getCatById - cat - is null, return. for " + player.getName());
         } else {
            for(Entry<Integer, Integer> entry : player.getAchievements(category).entrySet()) {
               int aId = entry.getKey();
               int nextLevel = entry.getValue() + 1 >= this.getMaxLevel(aId) ? this.getMaxLevel(aId) : entry.getValue() + 1;
               AchiveTemplate a = this.getAchievement(aId, Math.max(1, nextLevel));
               if (a == null) {
                  _log.warning("AchievementManager: GetAchievement - a - is null, return. for " + player.getName());
                  return;
               }

               playerPoints = player.getCounters().getAchievementInfo(a.getId());
               ++all;
               if ((page != 1 || clansvisual <= 3) && all <= page * 3 && all > (page - 1) * 3) {
                  ++clansvisual;
                  boolean done;
                  String html;
                  if (!a.isDone(playerPoints)) {
                     done = false;
                     String notDoneAchievement = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/achievements/oneAchievement.htm");
                     long needpoints = a.getPointsToComplete();
                     long diff = Math.max(0L, needpoints - playerPoints);
                     long greenbar = 24L * (playerPoints * 100L / needpoints) / 100L;
                     if (greenbar < 0L) {
                        greenbar = 0L;
                     }

                     if (greenbar > 24L) {
                        greenbar = 24L;
                     }

                     notDoneAchievement = notDoneAchievement.replaceFirst("%fame%", "" + a.getFame());
                     notDoneAchievement = notDoneAchievement.replaceAll("%bar1%", "" + greenbar);
                     notDoneAchievement = notDoneAchievement.replaceAll("%bar2%", "" + (24L - greenbar));
                     notDoneAchievement = notDoneAchievement.replaceFirst("%cap1%", greenbar > 0L ? "Gauge_DF_Food_Left" : "Gauge_DF_Exp_bg_Left");
                     notDoneAchievement = notDoneAchievement.replaceFirst("%cap2%", "Gauge_DF_Exp_bg_Right");
                     notDoneAchievement = notDoneAchievement.replaceFirst(
                        "%desc%",
                        player.getLang().equalsIgnoreCase("ru")
                           ? a.getDescRu().replaceAll("%need%", "" + (diff > 0L ? diff : "..."))
                           : a.getDescEn().replaceAll("%need%", "" + (diff > 0L ? diff : "..."))
                     );
                     notDoneAchievement = notDoneAchievement.replaceFirst("%bg%", a.getId() % 2 == 0 ? "090908" : "0f100f");
                     notDoneAchievement = notDoneAchievement.replaceFirst("%icon%", a.getIcon());
                     notDoneAchievement = notDoneAchievement.replaceFirst(
                        "%name%",
                        (player.getLang().equalsIgnoreCase("ru") ? a.getNameRu() : a.getNameEn())
                           + (a.getLevel() > 1 ? " " + ServerStorage.getInstance().getString(player.getLang(), "Achievement.LEVEL") + " " + a.getLevel() : "")
                     );
                     html = notDoneAchievement;
                  } else {
                     done = true;
                     String doneAchievement = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/achievements/oneAchievement.htm");
                     doneAchievement = doneAchievement.replaceFirst("%fame%", "" + a.getFame());
                     doneAchievement = doneAchievement.replaceAll("%bar1%", "24");
                     doneAchievement = doneAchievement.replaceAll("%bar2%", "0");
                     doneAchievement = doneAchievement.replaceFirst("%cap1%", "Gauge_DF_Food_Left");
                     doneAchievement = doneAchievement.replaceFirst("%cap2%", "Gauge_DF_Food_Right");
                     doneAchievement = doneAchievement.replaceFirst("%desc%", ServerStorage.getInstance().getString(player.getLang(), "Achievement.DONE"));
                     doneAchievement = doneAchievement.replaceFirst("%bg%", a.getId() % 2 == 0 ? "090908" : "0f100f");
                     doneAchievement = doneAchievement.replaceFirst("%icon%", a.getIcon());
                     doneAchievement = doneAchievement.replaceFirst(
                        "%name%",
                        (player.getLang().equalsIgnoreCase("ru") ? a.getNameRu() : a.getNameEn())
                           + (a.getLevel() > 1 ? " " + ServerStorage.getInstance().getString(player.getLang(), "Achievement.LEVEL") + " " + a.getLevel() : "")
                     );
                     html = doneAchievement;
                  }

                  if (clansvisual < 3) {
                     for(int d = clansvisual + 1; d != 3; ++d) {
                        html = html.replaceAll("%icon" + d + "%", "L2UI_CT1.Inventory_DF_CloakSlot_Disable");
                        html = html.replaceAll("%bar1" + d + "%", "0");
                        html = html.replaceAll("%bar2" + d + "%", "0");
                        html = html.replaceAll("%cap1" + d + "%", "&nbsp;");
                        html = html.replaceAll("%cap2" + d + "%", "&nbsp");
                        html = html.replaceAll("%desc" + d + "%", "&nbsp");
                        html = html.replaceAll("%bg" + d + "%", "0f100f");
                        html = html.replaceAll("%name" + d + "%", "&nbsp");
                     }
                  }

                  if (!done) {
                     achievementsNotDone = achievementsNotDone + html;
                  } else {
                     achievementsDone = achievementsDone + html;
                  }
               }
            }

            int greenbar = 0;
            if (getAchievementLevelSum(player, category) > 0) {
               greenbar = 248 * (getAchievementLevelSum(player, category) * 100 / cat.getAchievements().size()) / 100;
               greenbar = Math.min(greenbar, 248);
            }

            String var40 = FULL_PAGE.replaceAll("%bar1up%", "" + greenbar);
            var40 = var40.replaceAll("%bar2up%", "" + (248 - greenbar));
            var40 = var40.replaceFirst("%caps1%", greenbar > 0 ? "Gauge_DF_Large_Food_Left" : "Gauge_DF_Large_Exp_bg_Left");
            var40 = var40.replaceFirst("%caps2%", greenbar >= 248 ? "Gauge_DF_Large_Food_Right" : "Gauge_DF_Large_Exp_bg_Right");
            var40 = var40.replaceFirst("%achievementsNotDone%", achievementsNotDone);
            var40 = var40.replaceFirst("%achievementsDone%", achievementsDone);
            var40 = var40.replaceFirst("%catname%", player.getLang().equalsIgnoreCase("ru") ? cat.getNameRu() : cat.getNameEn());
            var40 = var40.replaceFirst("%catDesc%", player.getLang().equalsIgnoreCase("ru") ? cat.getDescRu() : cat.getDescEn());
            var40 = var40.replaceFirst("%catIcon%", cat.getIcon());
            player.sendPacket(new TutorialShowHtml(var40));
         }
      }
   }

   public void checkAchievementRewards(Player player) {
      synchronized(player.getAchievements()) {
         for(Entry<Integer, Integer> arco : player.getAchievements().entrySet()) {
            int achievementId = arco.getKey();
            int achievementLevel = arco.getValue();
            if (this.getMaxLevel(achievementId) > achievementLevel) {
               while(true) {
                  AchiveTemplate nextLevelAchievement = this.getAchievement(achievementId, ++achievementLevel);
                  if (nextLevelAchievement != null && nextLevelAchievement.isDone(player.getCounters().getAchievementInfo(nextLevelAchievement.getId()))) {
                     nextLevelAchievement.reward(player);
                  }

                  if (nextLevelAchievement == null) {
                     break;
                  }
               }
            }
         }
      }
   }

   public int getPointsForThisLevel(int totalPoints, int achievementId, int achievementLevel) {
      if (totalPoints == 0) {
         return 0;
      } else {
         int result = 0;

         for(int i = achievementLevel; i > 0; --i) {
            AchiveTemplate a = this.getAchievement(achievementId, i);
            if (a != null) {
               result = (int)((long)result + a.getPointsToComplete());
            }
         }

         return totalPoints - result;
      }
   }

   public AchiveTemplate getAchievement(int achievementId, int achievementLevel) {
      for(AchievementCategory cat : this._achievementCategories) {
         for(AchiveTemplate ach : cat.getAchievements()) {
            if (ach.getId() == achievementId && ach.getLevel() == achievementLevel) {
               return ach;
            }
         }
      }

      return null;
   }

   public AchiveTemplate getAchievement(int achievementId) {
      for(AchievementCategory cat : this._achievementCategories) {
         for(AchiveTemplate ach : cat.getAchievements()) {
            if (ach.getId() == achievementId) {
               return ach;
            }
         }
      }

      return null;
   }

   public AchiveTemplate getAchievementType(String type) {
      for(AchievementCategory cat : this._achievementCategories) {
         for(AchiveTemplate ach : cat.getAchievements()) {
            if (ach.getType().equals(type)) {
               return ach;
            }
         }
      }

      return null;
   }

   public AchiveTemplate getAchievementKillById(int npcId) {
      return this._achKillById.get(npcId);
   }

   public AchiveTemplate getAchievementRefById(int id) {
      return this._achRefById.get(id);
   }

   public AchiveTemplate getAchievementQuestById(int id) {
      return this._achQuestById.get(id);
   }

   public AchiveTemplate getAchievementWeaponEnchantByLvl(int lvl) {
      return this._achEnchantWeaponByLvl.get(lvl);
   }

   public AchiveTemplate getAchievementArmorEnchantByLvl(int lvl) {
      return this._achEnchantArmorByLvl.get(lvl);
   }

   public AchiveTemplate getAchievementJewerlyEnchantByLvl(int lvl) {
      return this._achEnchantJewerlyByLvl.get(lvl);
   }

   public Collection<Integer> getAchievementIds() {
      return this._achievementMaxLevels.keySet();
   }

   public int getMaxLevel(int id) {
      return this._achievementMaxLevels.getOrDefault(id, 0);
   }

   public static int getAchievementLevelSum(Player player, int categoryId) {
      return player.getAchievements(categoryId).values().stream().mapToInt(level -> level).sum();
   }

   public void load() {
      this._achievementMaxLevels.clear();
      this._achievementCategories.clear();
      this._achKillById.clear();
      this._achRefById.clear();
      this._achQuestById.clear();
      this._achEnchantWeaponByLvl.clear();
      this._achEnchantArmorByLvl.clear();
      this._achEnchantJewerlyByLvl.clear();

      try {
         File file = new File("data/stats/services/achievements.xml");
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setValidating(false);
         factory.setIgnoringComments(true);
         Document doc = factory.newDocumentBuilder().parse(file);

         for(Node g = doc.getFirstChild(); g != null; g = g.getNextSibling()) {
            for(Node z = g.getFirstChild(); z != null; z = z.getNextSibling()) {
               if (z.getNodeName().equals("categories")) {
                  for(Node i = z.getFirstChild(); i != null; i = i.getNextSibling()) {
                     if ("cat".equalsIgnoreCase(i.getNodeName())) {
                        int categoryId = Integer.valueOf(i.getAttributes().getNamedItem("id").getNodeValue());
                        String categoryNameEn = String.valueOf(i.getAttributes().getNamedItem("nameEn").getNodeValue());
                        String categoryNameRu = i.getAttributes().getNamedItem("nameRu") != null
                           ? String.valueOf(i.getAttributes().getNamedItem("nameRu").getNodeValue())
                           : categoryNameEn;
                        String categoryIcon = String.valueOf(i.getAttributes().getNamedItem("icon").getNodeValue());
                        String categoryDescEn = String.valueOf(i.getAttributes().getNamedItem("descEn").getNodeValue());
                        String categoryDescRu = i.getAttributes().getNamedItem("descRu") != null
                           ? String.valueOf(i.getAttributes().getNamedItem("descRu").getNodeValue())
                           : categoryDescEn;
                        this._achievementCategories
                           .add(new AchievementCategory(categoryId, categoryNameEn, categoryNameRu, categoryIcon, categoryDescEn, categoryDescRu));
                     }
                  }
               } else if (z.getNodeName().equals("achievement")) {
                  int achievementId = Integer.valueOf(z.getAttributes().getNamedItem("id").getNodeValue());
                  int achievementCategory = Integer.valueOf(z.getAttributes().getNamedItem("cat").getNodeValue());
                  String descEn = String.valueOf(z.getAttributes().getNamedItem("descEn").getNodeValue());
                  String descRu = z.getAttributes().getNamedItem("descRu") != null
                     ? String.valueOf(z.getAttributes().getNamedItem("descRu").getNodeValue())
                     : descEn;
                  String fieldType = String.valueOf(z.getAttributes().getNamedItem("type").getNodeValue());
                  int select = z.getAttributes().getNamedItem("select") != null ? Integer.valueOf(z.getAttributes().getNamedItem("select").getNodeValue()) : 0;
                  int achievementMaxLevel = 0;

                  for(Node i = z.getFirstChild(); i != null; i = i.getNextSibling()) {
                     if ("level".equalsIgnoreCase(i.getNodeName())) {
                        int level = Integer.valueOf(i.getAttributes().getNamedItem("id").getNodeValue());
                        long pointsToComplete = Long.parseLong(i.getAttributes().getNamedItem("need").getNodeValue());
                        int fame = Integer.valueOf(i.getAttributes().getNamedItem("fame").getNodeValue());
                        String nameEn = String.valueOf(i.getAttributes().getNamedItem("nameEn").getNodeValue());
                        String nameRu = i.getAttributes().getNamedItem("nameRu") != null
                           ? String.valueOf(i.getAttributes().getNamedItem("nameRu").getNodeValue())
                           : nameEn;
                        String icon = String.valueOf(i.getAttributes().getNamedItem("icon").getNodeValue());
                        AchiveTemplate achievement = new AchiveTemplate(
                           achievementId, level, nameEn, nameRu, achievementCategory, icon, descEn, descRu, pointsToComplete, fieldType, fame, select
                        );
                        if (achievementMaxLevel < level) {
                           achievementMaxLevel = level;
                        }

                        for(Node o = i.getFirstChild(); o != null; o = o.getNextSibling()) {
                           if ("reward".equalsIgnoreCase(o.getNodeName())) {
                              int Itemid = Integer.valueOf(o.getAttributes().getNamedItem("id").getNodeValue());
                              long Itemcount = Long.parseLong(o.getAttributes().getNamedItem("count").getNodeValue());
                              achievement.addReward(Itemid, Itemcount);
                           }
                        }

                        AchievementCategory lastCategory = this._achievementCategories
                           .stream()
                           .filter(ctg -> ctg.getCategoryId() == achievementCategory)
                           .findAny()
                           .orElse(null);
                        if (lastCategory != null) {
                           lastCategory.getAchievements().add(achievement);
                        }

                        if (fieldType.equals("killbyId") && !this._achKillById.containsKey(select)) {
                           this._achKillById.put(select, achievement);
                        } else if (fieldType.equals("reflectionById") && !this._achRefById.containsKey(select)) {
                           this._achRefById.put(select, achievement);
                        } else if (fieldType.equals("questById") && !this._achQuestById.containsKey(select)) {
                           this._achQuestById.put(select, achievement);
                        } else if (fieldType.equals("enchantWeaponByLvl") && !this._achEnchantWeaponByLvl.containsKey(achievementId)) {
                           this._achEnchantWeaponByLvl.put(select, achievement);
                        } else if (fieldType.equals("enchantArmorByLvl") && !this._achEnchantArmorByLvl.containsKey(achievementId)) {
                           this._achEnchantArmorByLvl.put(select, achievement);
                        } else if (fieldType.equals("enchantJewerlyByLvl") && !this._achEnchantJewerlyByLvl.containsKey(achievementId)) {
                           this._achEnchantJewerlyByLvl.put(select, achievement);
                        }
                     }
                  }

                  this._achievementMaxLevels.put(achievementId, achievementMaxLevel);
               }
            }
         }
      } catch (Exception var26) {
      }

      _log.info(
         "AchievementManager: Loaded "
            + this._achievementCategories.size()
            + " achievement categories and "
            + this._achievementMaxLevels.size()
            + " achievements."
      );
   }

   public boolean isActive() {
      return this._isActive;
   }

   public void setIsActive(boolean isActive) {
      this._isActive = isActive;
   }

   public static AchievementManager getInstance() {
      if (_instance == null) {
         _instance = new AchievementManager();
      }

      return _instance;
   }
}
