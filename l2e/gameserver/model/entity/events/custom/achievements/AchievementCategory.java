package l2e.gameserver.model.entity.events.custom.achievements;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.player.AchiveTemplate;

public class AchievementCategory {
   private static final int BAR_MAX = 24;
   private final List<AchiveTemplate> _achievements = new ArrayList<>();
   private final int _categoryId;
   private final String _nameEn;
   private final String _nameRu;
   private final String _icon;
   private final String _descEn;
   private final String _descRu;

   public AchievementCategory(int categoryId, String catNameEn, String catNameRu, String categoryIcon, String catDescEn, String catDescRu) {
      this._categoryId = categoryId;
      this._nameEn = catNameEn;
      this._nameRu = catNameRu;
      this._icon = categoryIcon;
      this._descEn = catDescEn;
      this._descRu = catDescRu;
   }

   public String getHtml(Player player) {
      return this.getHtml(player, AchievementManager.getAchievementLevelSum(player, this.getCategoryId()));
   }

   public String getHtml(Player player, int totalPlayerLevel) {
      int greenbar = 0;
      if (totalPlayerLevel > 0) {
         greenbar = 24 * (totalPlayerLevel * 100 / this._achievements.size()) / 100;
         greenbar = Math.min(greenbar, 24);
      }

      String temp = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/achievements/achievementsCat.htm");
      temp = temp.replaceFirst("%bg%", this.getCategoryId() % 2 == 0 ? "090908" : "0f100f");
      temp = temp.replaceFirst("%desc%", player.getLang().equalsIgnoreCase("ru") ? this.getDescRu() : this.getDescEn());
      temp = temp.replaceFirst("%icon%", this.getIcon());
      temp = temp.replaceFirst("%name%", player.getLang().equalsIgnoreCase("ru") ? this.getNameRu() : this.getNameEn());
      temp = temp.replaceFirst("%id%", "" + this.getCategoryId());
      temp = temp.replaceFirst("%caps1%", greenbar > 0 ? "Gauge_DF_Food_Left" : "Gauge_DF_Exp_bg_Left");
      temp = temp.replaceFirst("%caps2%", greenbar >= 24 ? "Gauge_DF_Food_Right" : "Gauge_DF_Exp_bg_Right");
      temp = temp.replaceAll("%bar1%", "" + greenbar);
      return temp.replaceAll("%bar2%", "" + (24 - greenbar));
   }

   public int getCategoryId() {
      return this._categoryId;
   }

   public List<AchiveTemplate> getAchievements() {
      return this._achievements;
   }

   public String getDescEn() {
      return this._descEn;
   }

   public String getDescRu() {
      return this._descRu;
   }

   public String getIcon() {
      return this._icon;
   }

   public String getNameEn() {
      return this._nameEn;
   }

   public String getNameRu() {
      return this._nameRu;
   }
}
