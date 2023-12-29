package l2e.gameserver.data.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.actor.templates.quest.QuestDropItem;
import l2e.gameserver.model.actor.templates.quest.QuestExperience;
import l2e.gameserver.model.actor.templates.quest.QuestRewardItem;
import l2e.gameserver.model.actor.templates.quest.QuestTemplate;
import l2e.gameserver.model.stats.StatsSet;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class QuestsParser extends DocumentParser {
   private static Logger _log = Logger.getLogger(QuestsParser.class.getName());
   private final Map<Integer, QuestTemplate> _quests = new HashMap<>();

   protected QuestsParser() {
      this.load();
   }

   @Override
   public void load() {
      this._quests.clear();
      this.parseDirectory("data/stats/quests", false);
      _log.log(Level.INFO, this.getClass().getSimpleName() + ": Loaded " + this._quests.size() + " quest templates.");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node list = this.getCurrentDocument().getFirstChild().getFirstChild(); list != null; list = list.getNextSibling()) {
         if (list.getNodeName().equalsIgnoreCase("quest")) {
            NamedNodeMap node = list.getAttributes();
            int id = Integer.valueOf(node.getNamedItem("id").getNodeValue());
            String nameEn = node.getNamedItem("nameEn").getNodeValue();
            String nameRu = node.getNamedItem("nameRu").getNodeValue();
            long expReward = 0L;
            long spReward = 0L;
            double expRate = 0.0;
            double spRate = 0.0;
            boolean rateable = false;
            boolean expRateable = false;
            boolean spRateable = false;
            QuestExperience experience = null;
            List<QuestRewardItem> rewards = null;
            Map<Integer, List<QuestRewardItem>> groupRewards = new HashMap<>();
            List<QuestRewardItem> rewardList = null;
            Map<Integer, List<QuestDropItem>> dropList = null;
            List<QuestDropItem> itemList = null;
            int minLvl = 0;
            int maxLvl = 0;
            StatsSet params = new StatsSet();

            for(Node quest = list.getFirstChild(); quest != null; quest = quest.getNextSibling()) {
               if (quest.getNodeName().equalsIgnoreCase("level")) {
                  node = quest.getAttributes();
                  minLvl = node.getNamedItem("min") != null ? Integer.parseInt(node.getNamedItem("min").getNodeValue()) : 1;
                  maxLvl = node.getNamedItem("max") != null ? Integer.parseInt(node.getNamedItem("max").getNodeValue()) : 85;
               } else if (quest.getNodeName().equalsIgnoreCase("expirience")) {
                  for(Node exp = quest.getFirstChild(); exp != null; exp = exp.getNextSibling()) {
                     if (exp.getNodeName().equalsIgnoreCase("rewardExp")) {
                        node = exp.getAttributes();
                        expRate = node.getNamedItem("rate") != null ? Double.valueOf(node.getNamedItem("rate").getNodeValue()) : 1.0;
                        expReward = Long.valueOf(node.getNamedItem("val").getNodeValue());
                        expRateable = node.getNamedItem("rateable") != null ? Boolean.parseBoolean(node.getNamedItem("rateable").getNodeValue()) : false;
                     } else if (exp.getNodeName().equalsIgnoreCase("rewardSp")) {
                        node = exp.getAttributes();
                        spRate = node.getNamedItem("rate") != null ? Double.valueOf(node.getNamedItem("rate").getNodeValue()) : 1.0;
                        spReward = Long.valueOf(node.getNamedItem("val").getNodeValue());
                        spRateable = node.getNamedItem("rateable") != null ? Boolean.parseBoolean(node.getNamedItem("rateable").getNodeValue()) : false;
                     }
                  }
               } else if (quest.getNodeName().equalsIgnoreCase("droplist")) {
                  dropList = new HashMap<>();

                  for(Node drop = quest.getFirstChild(); drop != null; drop = drop.getNextSibling()) {
                     if (drop.getNodeName().equalsIgnoreCase("npc")) {
                        List<QuestDropItem> var47 = new ArrayList();
                        int npcId = Integer.valueOf(drop.getAttributes().getNamedItem("id").getNodeValue());

                        for(Node group = drop.getFirstChild(); group != null; group = group.getNextSibling()) {
                           if (group.getNodeName().equalsIgnoreCase("item")) {
                              node = group.getAttributes();
                              int itemId = Integer.valueOf(node.getNamedItem("id").getNodeValue());
                              double rate = node.getNamedItem("rate") != null ? Double.valueOf(node.getNamedItem("rate").getNodeValue()) : 1.0;
                              long min = (long)Integer.valueOf(node.getNamedItem("min").getNodeValue()).intValue();
                              long max = node.getNamedItem("max") != null ? (long)Integer.valueOf(node.getNamedItem("max").getNodeValue()).intValue() : 0L;
                              double chance = node.getNamedItem("chance") != null ? Double.valueOf(node.getNamedItem("chance").getNodeValue()) : 1.0;
                              boolean isRateable = node.getNamedItem("rateable") != null
                                 ? Boolean.parseBoolean(node.getNamedItem("rateable").getNodeValue())
                                 : false;
                              var47.add(new QuestDropItem(itemId, rate, min, max, chance, isRateable));
                           }
                        }

                        dropList.put(npcId, var47);
                     }
                  }
               } else if (quest.getNodeName().equalsIgnoreCase("add_parameters")) {
                  for(Node sp = quest.getFirstChild(); sp != null; sp = sp.getNextSibling()) {
                     if ("set".equalsIgnoreCase(sp.getNodeName())) {
                        params.set(sp.getAttributes().getNamedItem("name").getNodeValue(), sp.getAttributes().getNamedItem("value").getNodeValue());
                     }
                  }
               } else if (quest.getNodeName().equalsIgnoreCase("rewardlist")) {
                  rewards = new ArrayList<>();

                  for(Node reward = quest.getFirstChild(); reward != null; reward = reward.getNextSibling()) {
                     if (reward.getNodeName().equalsIgnoreCase("item")) {
                        node = reward.getAttributes();
                        int itemId = Integer.valueOf(node.getNamedItem("id").getNodeValue());
                        double rate = node.getNamedItem("rate") != null ? Double.valueOf(node.getNamedItem("rate").getNodeValue()) : 1.0;
                        long min = (long)Integer.valueOf(node.getNamedItem("min").getNodeValue()).intValue();
                        long max = node.getNamedItem("max") != null ? (long)Integer.valueOf(node.getNamedItem("max").getNodeValue()).intValue() : 0L;
                        boolean isRateable = node.getNamedItem("rateable") != null
                           ? Boolean.parseBoolean(node.getNamedItem("rateable").getNodeValue())
                           : false;
                        rewards.add(new QuestRewardItem(itemId, rate, min, max, isRateable));
                     } else if (reward.getNodeName().equalsIgnoreCase("variant")) {
                        List<QuestRewardItem> var46 = new ArrayList();
                        int varId = Integer.valueOf(reward.getAttributes().getNamedItem("id").getNodeValue());

                        for(Node group = reward.getFirstChild(); group != null; group = group.getNextSibling()) {
                           if (group.getNodeName().equalsIgnoreCase("item")) {
                              node = group.getAttributes();
                              int itemId = Integer.valueOf(node.getNamedItem("id").getNodeValue());
                              double rate = node.getNamedItem("rate") != null ? Double.valueOf(node.getNamedItem("rate").getNodeValue()) : 1.0;
                              long min = (long)Integer.valueOf(node.getNamedItem("min").getNodeValue()).intValue();
                              long max = node.getNamedItem("max") != null ? (long)Integer.valueOf(node.getNamedItem("max").getNodeValue()).intValue() : 0L;
                              boolean isRateable = node.getNamedItem("rateable") != null
                                 ? Boolean.parseBoolean(node.getNamedItem("rateable").getNodeValue())
                                 : false;
                              var46.add(new QuestRewardItem(itemId, rate, min, max, isRateable));
                           }
                        }

                        groupRewards.put(varId, var46);
                     }
                  }
               }
            }

            if (expReward != 0L || spReward != 0L) {
               experience = new QuestExperience(expReward, spReward, expRate, spRate, expRateable, spRateable);
            }

            QuestTemplate template = new QuestTemplate(id, nameEn, nameRu, minLvl, maxLvl, dropList, experience, rewards, groupRewards, false, params);
            this._quests.put(id, template);
         }
      }
   }

   public QuestTemplate getTemplate(int id) {
      return this._quests.get(id);
   }

   public static QuestsParser getInstance() {
      return QuestsParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final QuestsParser _instance = new QuestsParser();
   }
}
