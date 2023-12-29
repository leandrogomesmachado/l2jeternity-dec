package l2e.gameserver.instancemanager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.reward.RewardGroup;
import l2e.gameserver.model.reward.RewardList;
import l2e.gameserver.model.reward.RewardType;
import l2e.gameserver.model.skills.Skill;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class DropManager {
   private static final Logger _log = Logger.getLogger(DropManager.class.getName());
   private static DropManager _instance;

   public DropManager() {
      this.loadDropSettings();
      this.loadSkillSettings();
   }

   public void reload() {
      this.loadDropSettings();
      this.loadSkillSettings();
   }

   private void loadDropSettings() {
      try {
         File file = new File(Config.DATAPACK_ROOT + "/data/stats/npcs/customDrop.xml");
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setValidating(false);
         factory.setIgnoringComments(true);
         Document doc1 = factory.newDocumentBuilder().parse(file);
         int counter = 0;

         for(Node n1 = doc1.getFirstChild(); n1 != null; n1 = n1.getNextSibling()) {
            if ("list".equalsIgnoreCase(n1.getNodeName())) {
               for(Node cat = n1.getFirstChild(); cat != null; cat = cat.getNextSibling()) {
                  if ("rewardlist".equalsIgnoreCase(cat.getNodeName())) {
                     int id = Integer.parseInt(cat.getAttributes().getNamedItem("id").getNodeValue());
                     RewardType type = RewardType.valueOf(cat.getAttributes().getNamedItem("type").getNodeValue());
                     String lvlDiff = cat.getAttributes().getNamedItem("lvlDiff") != null
                        ? cat.getAttributes().getNamedItem("lvlDiff").getNodeValue()
                        : "1-85";
                     String npcType = cat.getAttributes().getNamedItem("npcType") != null ? cat.getAttributes().getNamedItem("npcType").getNodeValue() : "";
                     String npcId = cat.getAttributes().getNamedItem("npcId") != null ? cat.getAttributes().getNamedItem("npcId").getNodeValue() : "";
                     String forbiddenId = cat.getAttributes().getNamedItem("forbiddenList") != null
                        ? cat.getAttributes().getNamedItem("forbiddenList").getNodeValue()
                        : "";
                     List<Integer> forbiddenList = null;
                     if (!forbiddenId.isEmpty()) {
                        String[] list = forbiddenId.split(";");
                        forbiddenList = new ArrayList<>(list.length);

                        for(String nId : list) {
                           forbiddenList.add(Integer.parseInt(nId));
                        }
                     }

                     Map<RewardType, List<RewardGroup>> groupList = new HashMap<>();

                     for(Node reward = cat.getFirstChild(); reward != null; reward = reward.getNextSibling()) {
                        RewardList rewards = RewardList.parseRewardList(
                           _log,
                           cat,
                           cat.getAttributes(),
                           type,
                           !npcType.isEmpty() && (npcType.equals("RaidBoss") || npcType.equals("GrandBoss")),
                           String.valueOf(id)
                        );
                        if (rewards != null) {
                           groupList.put(type, rewards);
                        }
                     }

                     this.addDropToNpc(groupList, lvlDiff, npcType, npcId, forbiddenList);
                     ++counter;
                  }
               }
            }
         }

         _log.info("DropManager: Loaded " + counter + " custom drop templates.");
      } catch (DOMException | ParserConfigurationException | SAXException | NumberFormatException var19) {
         _log.log(Level.WARNING, "DropManager: customDrop.xml could not be initialized.", (Throwable)var19);
      } catch (IllegalArgumentException | IOException var20) {
         _log.log(Level.WARNING, "DropManager: IOException or IllegalArgumentException.", (Throwable)var20);
      }
   }

   private void loadSkillSettings() {
      try {
         File file = new File(Config.DATAPACK_ROOT + "/data/stats/npcs/customSkills.xml");
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setValidating(false);
         factory.setIgnoringComments(true);
         Document doc1 = factory.newDocumentBuilder().parse(file);
         int counter = 0;

         for(Node n1 = doc1.getFirstChild(); n1 != null; n1 = n1.getNextSibling()) {
            if ("list".equalsIgnoreCase(n1.getNodeName())) {
               for(Node cat = n1.getFirstChild(); cat != null; cat = cat.getNextSibling()) {
                  if ("npclist".equalsIgnoreCase(cat.getNodeName())) {
                     String lvlDiff = cat.getAttributes().getNamedItem("lvlDiff") != null
                        ? cat.getAttributes().getNamedItem("lvlDiff").getNodeValue()
                        : "1-85";
                     String npcType = cat.getAttributes().getNamedItem("npcType") != null ? cat.getAttributes().getNamedItem("npcType").getNodeValue() : "";
                     String npcId = cat.getAttributes().getNamedItem("npcId") != null ? cat.getAttributes().getNamedItem("npcId").getNodeValue() : "";
                     String forbiddenId = cat.getAttributes().getNamedItem("forbiddenList") != null
                        ? cat.getAttributes().getNamedItem("forbiddenList").getNodeValue()
                        : "";
                     List<Integer> forbiddenList = null;
                     if (!forbiddenId.isEmpty()) {
                        String[] list = forbiddenId.split(";");
                        forbiddenList = new ArrayList<>(list.length);

                        for(String nId : list) {
                           forbiddenList.add(Integer.parseInt(nId));
                        }
                     }

                     List<Skill> groupList = new ArrayList<>();

                     for(Node sk = cat.getFirstChild(); sk != null; sk = sk.getNextSibling()) {
                        if ("skill".equalsIgnoreCase(sk.getNodeName())) {
                           int skillId = Integer.parseInt(sk.getAttributes().getNamedItem("id").getNodeValue());
                           int level = Integer.parseInt(sk.getAttributes().getNamedItem("level").getNodeValue());
                           Skill data = SkillsParser.getInstance().getInfo(skillId, level);
                           if (data != null) {
                              groupList.add(data);
                           }
                        }
                     }

                     this.addSkillToNpc(groupList, lvlDiff, npcType, npcId, forbiddenList);
                     ++counter;
                  }
               }
            }
         }

         _log.info("DropManager: Added " + counter + " custom skill templates.");
      } catch (DOMException | ParserConfigurationException | SAXException | NumberFormatException var17) {
         _log.log(Level.WARNING, "DropManager: customSkills.xml could not be initialized.", (Throwable)var17);
      } catch (IllegalArgumentException | IOException var18) {
         _log.log(Level.WARNING, "DropManager: IOException or IllegalArgumentException.", (Throwable)var18);
      }
   }

   private void addDropToNpc(Map<RewardType, List<RewardGroup>> groupList, String lvlDiff, String npcType, String npcId, List<Integer> forbiddenList) {
      if (!groupList.isEmpty()) {
         String[] splitLvl = lvlDiff.split("-");
         int minLvl = Integer.parseInt(splitLvl[0]);
         int maxLvl = Integer.parseInt(splitLvl[1]);
         boolean isDropForAll = !npcType.isEmpty() && npcType.equals("ALL");
         boolean isDropbyId = !npcId.isEmpty();
         boolean isHaveForbidden = forbiddenList != null && !forbiddenList.isEmpty();
         if (isDropForAll) {
            for(NpcTemplate template : NpcsParser.getInstance().getAllNpcs()) {
               if (template != null
                  && !template.isType("Npc")
                  && template.getLevel() >= minLvl
                  && template.getLevel() <= maxLvl
                  && (!isHaveForbidden || !forbiddenList.contains(template.getId()))) {
                  this.addDrop(template, groupList);
               }
            }
         } else if (isDropbyId) {
            String[] npcList = npcId.split(";");

            for(NpcTemplate template : NpcsParser.getInstance().getAllNpcs()) {
               if (template != null && this.isValidId(template, npcList)) {
                  this.addDrop(template, groupList);
               }
            }
         } else {
            if (npcType.isEmpty()) {
               return;
            }

            String[] npcList = npcType.split(";");

            for(NpcTemplate template : NpcsParser.getInstance().getAllNpcs()) {
               if (template != null
                  && this.isValidType(template, npcList)
                  && template.getLevel() >= minLvl
                  && template.getLevel() <= maxLvl
                  && (!isHaveForbidden || !forbiddenList.contains(template.getId()))) {
                  this.addDrop(template, groupList);
               }
            }
         }
      }
   }

   private void addDrop(NpcTemplate template, Map<RewardType, List<RewardGroup>> groupList) {
      for(RewardType type : groupList.keySet()) {
         RewardList dropList = template.getRewardList(type);
         if (dropList != null) {
            for(RewardGroup group : groupList.get(type)) {
               dropList.add(group);
            }
         } else {
            RewardList newList = new RewardList(type, false);

            for(RewardGroup group : groupList.get(type)) {
               newList.add(group);
            }

            template.putRewardList(type, newList);
         }
      }
   }

   private boolean isValidType(NpcTemplate template, String[] npcList) {
      for(String type : npcList) {
         if (template.isType(type)) {
            return true;
         }
      }

      return false;
   }

   private boolean isValidId(NpcTemplate template, String[] npcList) {
      for(String id : npcList) {
         if (Integer.parseInt(id) == template.getId()) {
            return true;
         }
      }

      return false;
   }

   private void addSkillToNpc(List<Skill> groupList, String lvlDiff, String npcType, String npcId, List<Integer> forbiddenList) {
      if (!groupList.isEmpty()) {
         String[] splitLvl = lvlDiff.split("-");
         int minLvl = Integer.parseInt(splitLvl[0]);
         int maxLvl = Integer.parseInt(splitLvl[1]);
         boolean isDropForAll = !npcType.isEmpty() && npcType.equals("ALL");
         boolean isDropbyId = !npcId.isEmpty();
         boolean isHaveForbidden = forbiddenList != null && !forbiddenList.isEmpty();
         if (isDropForAll) {
            for(NpcTemplate template : NpcsParser.getInstance().getAllNpcs()) {
               if (template != null
                  && !template.isType("Npc")
                  && template.getLevel() >= minLvl
                  && template.getLevel() <= maxLvl
                  && (!isHaveForbidden || !forbiddenList.contains(template.getId()))) {
                  this.addSkill(template, groupList);
               }
            }
         } else if (isDropbyId) {
            String[] npcList = npcId.split(";");

            for(NpcTemplate template : NpcsParser.getInstance().getAllNpcs()) {
               if (template != null && this.isValidId(template, npcList)) {
                  this.addSkill(template, groupList);
               }
            }
         } else {
            if (npcType.isEmpty()) {
               return;
            }

            String[] npcList = npcType.split(";");

            for(NpcTemplate template : NpcsParser.getInstance().getAllNpcs()) {
               if (template != null
                  && this.isValidType(template, npcList)
                  && template.getLevel() >= minLvl
                  && template.getLevel() <= maxLvl
                  && (!isHaveForbidden || !forbiddenList.contains(template.getId()))) {
                  this.addSkill(template, groupList);
               }
            }
         }
      }
   }

   private void addSkill(NpcTemplate template, List<Skill> groupList) {
      for(Skill skill : groupList) {
         if (skill != null && !template.getSkills().containsKey(skill.getId())) {
            template.addSkill(skill);
         }
      }
   }

   public static DropManager getInstance() {
      if (_instance == null) {
         _instance = new DropManager();
      }

      return _instance;
   }
}
