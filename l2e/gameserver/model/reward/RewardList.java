package l2e.gameserver.model.reward;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Player;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class RewardList extends ArrayList<RewardGroup> {
   private static final long serialVersionUID = 1L;
   public static final int MAX_CHANCE = 1000000;
   private final RewardType _type;
   private final boolean _autoLoot;

   public RewardList(RewardType rewardType, boolean a) {
      super(5);
      this._type = rewardType;
      this._autoLoot = a;
   }

   public List<RewardItem> roll(Player player) {
      return this.roll(player, 1.0, 1.0, null);
   }

   public List<RewardItem> roll(Player player, double penaltyMod, double rateMod) {
      return this.roll(player, penaltyMod, rateMod, null);
   }

   public List<RewardItem> roll(Player player, double penaltyMod, double rateMod, Attackable npc) {
      List<RewardItem> temp = new ArrayList<>();

      for(RewardGroup g : this) {
         temp.addAll(g.roll(this._type, player, penaltyMod, rateMod, npc));
      }

      return temp;
   }

   public RewardType getType() {
      return this._type;
   }

   public boolean isAutoLoot() {
      return this._autoLoot;
   }

   public static RewardList parseRewardList(Logger _log, Node cat, NamedNodeMap rewardElement, RewardType type, boolean isRaid, String debug) {
      boolean autoLoot = rewardElement.getNamedItem("auto_loot") != null && Boolean.parseBoolean(rewardElement.getNamedItem("auto_loot").getNodeValue());
      RewardList list = new RewardList(type, autoLoot);

      for(Node reward = cat.getFirstChild(); reward != null; reward = reward.getNextSibling()) {
         boolean notGroupType = type == RewardType.SWEEP || type == RewardType.NOT_RATED_NOT_GROUPED;
         if (!"group".equalsIgnoreCase(reward.getNodeName())) {
            if ("reward".equalsIgnoreCase(reward.getNodeName())) {
               if (!notGroupType) {
                  _log.warning("Reward can't be without group(and not grouped): " + debug + "; type: " + type);
               } else {
                  RewardData data = RewardData.parseReward(reward.getAttributes(), type);
                  if (data != null) {
                     RewardGroup g = new RewardGroup(1000000.0);
                     g.addData(data);
                     list.add(g);
                  }
               }
            }
         } else {
            NamedNodeMap attrs = reward.getAttributes();
            double enterChance = attrs.getNamedItem("chance") == null ? 1000000.0 : Double.parseDouble(attrs.getNamedItem("chance").getNodeValue()) * 10000.0;
            RewardGroup group = notGroupType ? null : new RewardGroup(enterChance * Config.RATE_CHANCE_GROUP_DROP_ITEMS);

            for(Node drop = reward.getFirstChild(); drop != null; drop = drop.getNextSibling()) {
               if ("reward".equalsIgnoreCase(drop.getNodeName())) {
                  RewardData data = RewardData.parseReward(drop.getAttributes(), type);
                  if (notGroupType) {
                     _log.warning("Can't load rewardlist from group: " + debug + "; type: " + type);
                  } else if (data != null) {
                     group.addData(data);
                     if (data.getItem().isEquipment() && !group.isNotUseMode() && !isRaid) {
                        group.setIsNotUseMode(true);
                     }
                  }
               }
            }

            list.add(group);
         }
      }

      return list;
   }
}
