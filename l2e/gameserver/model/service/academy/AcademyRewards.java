package l2e.gameserver.model.service.academy;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.Item;

public class AcademyRewards {
   private static List<AcademyRewards.AcademyReward> _academyRewards = new ArrayList<>();

   public void load() {
      _academyRewards.clear();
      String[] rewardList = Config.SERVICES_ACADEMY_REWARD.split(";");

      for(String reward : rewardList) {
         Item tmp = ItemsParser.getInstance().getTemplate(Integer.parseInt(reward));
         if (tmp != null) {
            _academyRewards.add(
               new AcademyRewards.AcademyReward(tmp.getNameEn().replaceAll(" ", ""), tmp.getNameRu().replaceAll(" ", ""), Integer.parseInt(reward))
            );
         }
      }
   }

   public int getItemId(String itemName, Player player) {
      int id = -1;
      boolean isRuLang = player.getLang().equalsIgnoreCase("ru");

      for(AcademyRewards.AcademyReward item : _academyRewards) {
         String name = isRuLang ? item.getNameRu() : item.getNameEn();
         if (name.equalsIgnoreCase(itemName)) {
            id = item.getItemId();
         }
      }

      return id;
   }

   public String toList(Player player) {
      String list = "";
      boolean isRuLang = player.getLang().equalsIgnoreCase("ru");

      for(AcademyRewards.AcademyReward a : _academyRewards) {
         String name = isRuLang ? a.getNameRu() : a.getNameEn();
         list = list + name + ";";
      }

      return list;
   }

   public static AcademyRewards getInstance() {
      return AcademyRewards.SingletonHolder._instance;
   }

   public class AcademyReward {
      private final String _itemNameEn;
      private final String _itemNameRu;
      private final int _itemId;

      public AcademyReward(String nameEn, String nameRu, int id) {
         this._itemNameEn = nameEn;
         this._itemNameRu = nameRu;
         this._itemId = id;
      }

      public String getNameEn() {
         return this._itemNameEn;
      }

      public String getNameRu() {
         return this._itemNameRu;
      }

      public int getItemId() {
         return this._itemId;
      }
   }

   private static class SingletonHolder {
      protected static final AcademyRewards _instance = new AcademyRewards();
   }
}
