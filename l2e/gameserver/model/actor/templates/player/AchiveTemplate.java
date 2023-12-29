package l2e.gameserver.model.actor.templates.player;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.events.custom.achievements.AchievementManager;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.reward.RewardItemResult;
import l2e.gameserver.model.strings.server.ServerStorage;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.MagicSkillUse;

public class AchiveTemplate {
   private final int _id;
   private final int _level;
   private final String _nameEn;
   private final String _nameRu;
   private final int _categoryId;
   private final String _icon;
   private final String _descEn;
   private final String _descRu;
   private final long _pointsToComplete;
   private final String _achievementType;
   private final int _fame;
   private final int _select;
   private final List<RewardItemResult> _rewards;

   public AchiveTemplate(
      int id,
      int level,
      String nameEn,
      String nameRu,
      int categoryId,
      String icon,
      String descEn,
      String descRu,
      long pointsToComplete,
      String achievementType,
      int fame,
      int select
   ) {
      this._id = id;
      this._level = level;
      this._nameEn = nameEn;
      this._nameRu = nameRu;
      this._categoryId = categoryId;
      this._icon = icon;
      this._descEn = descEn;
      this._descRu = descRu;
      this._pointsToComplete = pointsToComplete;
      this._achievementType = achievementType;
      this._fame = fame;
      this._select = select;
      this._rewards = new LinkedList<>();
   }

   public boolean isDone(long playerPoints) {
      return playerPoints >= this._pointsToComplete;
   }

   public String getNotDoneHtml(Player pl, int playerPoints) {
      String oneAchievement = HtmCache.getInstance().getHtm(pl, pl.getLang(), "data/html/mods/achievements/oneAchievement.htm");
      int greenbar = (int)(24L * ((long)(playerPoints * 100) / this._pointsToComplete) / 100L);
      greenbar = Math.max(greenbar, 0);
      if (greenbar > 24) {
         return "";
      } else {
         oneAchievement = oneAchievement.replaceFirst("%fame%", "" + this._fame);
         oneAchievement = oneAchievement.replaceAll("%bar1%", "" + greenbar);
         oneAchievement = oneAchievement.replaceAll("%bar2%", "" + (24 - greenbar));
         oneAchievement = oneAchievement.replaceFirst("%cap1%", greenbar > 0 ? "Gauge_DF_Food_Left" : "Gauge_DF_Exp_bg_Left");
         oneAchievement = oneAchievement.replaceFirst("%cap2%", "Gauge_DF_Exp_bg_Right");
         oneAchievement = oneAchievement.replaceFirst(
            "%desc%",
            pl.getLang().equalsIgnoreCase("ru")
               ? this._descRu.replaceAll("%need%", "" + Math.max(0L, this._pointsToComplete - (long)playerPoints))
               : this._descEn.replaceAll("%need%", "" + Math.max(0L, this._pointsToComplete - (long)playerPoints))
         );
         oneAchievement = oneAchievement.replaceFirst("%bg%", this._id % 2 == 0 ? "090908" : "0f100f");
         oneAchievement = oneAchievement.replaceFirst("%icon%", this._icon);
         return oneAchievement.replaceFirst(
            "%name%",
            (pl.getLang().equalsIgnoreCase("ru") ? this._nameRu : this._nameEn)
               + (this._level > 1 ? " " + ServerStorage.getInstance().getString(pl.getLang(), "Achievement.LEVEL") + " " + this._level : "")
         );
      }
   }

   public String getDoneHtml(Player player) {
      String oneAchievement = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/achievements/oneAchievement.htm");
      oneAchievement = oneAchievement.replaceFirst("%fame%", "" + this._fame);
      oneAchievement = oneAchievement.replaceAll("%bar1%", "24");
      oneAchievement = oneAchievement.replaceAll("%bar2%", "0");
      oneAchievement = oneAchievement.replaceFirst("%cap1%", "Gauge_DF_Food_Left");
      oneAchievement = oneAchievement.replaceFirst("%cap2%", "Gauge_DF_Food_Right");
      oneAchievement = oneAchievement.replaceFirst("%desc%", ServerStorage.getInstance().getString(player.getLang(), "Achievement.DONE"));
      oneAchievement = oneAchievement.replaceFirst("%bg%", this._id % 2 == 0 ? "090908" : "0f100f");
      oneAchievement = oneAchievement.replaceFirst("%icon%", this._icon);
      return oneAchievement.replaceFirst(
         "%name%",
         (player.getLang().equalsIgnoreCase("ru") ? this._nameRu : this._nameEn)
            + (this._level > 1 ? " " + ServerStorage.getInstance().getString(player.getLang(), "Achievement.LEVEL") + " " + this._level : "")
      );
   }

   public void reward(Player player) {
      synchronized(player.getAchievements()) {
         player.sendPacket(
            new CreatureSay(
               player.getObjectId(),
               20,
               player.getLang().equalsIgnoreCase("ru") ? this.getNameRu() : this.getNameEn(),
               ServerStorage.getInstance().getString(player.getLang(), "Achievement.COMPLETED")
            )
         );
         player.getAchievements().put(this.getId(), this.getLevel());
         player.setFame(player.getFame() + this.getFame());
         if (AchievementManager.getInstance().getMaxLevel(this.getId()) > this.getLevel()) {
            player.getCounters().refreshAchievementInfo(this.getId());
         }

         for(ItemInstance item : this.getRewards().stream().map(r -> r.createItem()).collect(Collectors.toList())) {
            player.addItem("Achievement", item, player, true);
         }

         player.sendUserInfo();
         player.broadcastPacket(new MagicSkillUse(player, player, 2528, 1, 0, 500));
      }
   }

   public List<RewardItemResult> getRewards() {
      return this._rewards;
   }

   public String getNameEn() {
      return this._nameEn;
   }

   public String getNameRu() {
      return this._nameRu;
   }

   public String getDescEn() {
      return this._descEn;
   }

   public String getDescRu() {
      return this._descRu;
   }

   public int getId() {
      return this._id;
   }

   public int getLevel() {
      return this._level;
   }

   public void addReward(int itemId, long itemCount) {
      this._rewards.add(new RewardItemResult(itemId, itemCount));
   }

   public String getType() {
      return this._achievementType;
   }

   public long getPointsToComplete() {
      return this._pointsToComplete;
   }

   public int getCategoryId() {
      return this._categoryId;
   }

   public String getIcon() {
      return this._icon;
   }

   public int getFame() {
      return this._fame;
   }

   public boolean isSelected() {
      return this._select > 0;
   }

   public int getSelect() {
      return this._select;
   }
}
