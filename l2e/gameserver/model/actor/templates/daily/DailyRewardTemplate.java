package l2e.gameserver.model.actor.templates.daily;

import java.util.Map;

public class DailyRewardTemplate {
   private int _day;
   private Map<Integer, Integer> _rewards;
   private String _displayImage;

   public DailyRewardTemplate(int day, Map<Integer, Integer> rewards) {
      this._day = day;
      this._rewards = rewards;
   }

   public int getDay() {
      return this._day;
   }

   public Map<Integer, Integer> getRewards() {
      return this._rewards;
   }

   public String getDisplayImage() {
      return this._displayImage;
   }

   public void setDisplayImage(String image) {
      this._displayImage = image;
   }
}
