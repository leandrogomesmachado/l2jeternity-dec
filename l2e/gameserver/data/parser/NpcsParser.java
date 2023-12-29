package l2e.gameserver.data.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.instancemanager.DropManager;
import l2e.gameserver.model.actor.templates.npc.AbsorbInfo;
import l2e.gameserver.model.actor.templates.npc.Faction;
import l2e.gameserver.model.actor.templates.npc.MinionData;
import l2e.gameserver.model.actor.templates.npc.MinionTemplate;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.reward.RewardList;
import l2e.gameserver.model.reward.RewardType;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.stats.StatsSet;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class NpcsParser extends DocumentParser {
   private static final Logger _log = Logger.getLogger(NpcsParser.class.getName());
   private final Map<Integer, NpcTemplate> _npcs = new HashMap<>();

   protected NpcsParser() {
      this.load();
   }

   @Override
   public final void load() {
      this._npcs.clear();
      this.parseDirectory("data/stats/npcs/npcs", false);
      if (Config.CUSTOM_NPC) {
         this.parseDirectory("data/stats/npcs/npcs/custom", false);
      }

      _log.info(this.getClass().getSimpleName() + ": Loaded " + this._npcs.size() + " npc templates.");
   }

   @Override
   protected void reloadDocument() {
      for(Node c = this.getCurrentDocument().getFirstChild(); c != null; c = c.getNextSibling()) {
         if ("list".equalsIgnoreCase(c.getNodeName())) {
            for(Node npc = c.getFirstChild(); npc != null; npc = npc.getNextSibling()) {
               if ("npc".equalsIgnoreCase(npc.getNodeName())) {
                  NamedNodeMap attrs = npc.getAttributes();
                  int npcId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
                  NpcTemplate template = this.getTemplate(npcId);
                  if (template != null) {
                     for(Node cat = npc.getFirstChild(); cat != null; cat = cat.getNextSibling()) {
                        if (!"rewardlist".equalsIgnoreCase(cat.getNodeName())) {
                           if ("skills".equalsIgnoreCase(cat.getNodeName())) {
                              for(Node skillCat = cat.getFirstChild(); skillCat != null; skillCat = skillCat.getNextSibling()) {
                                 if ("skill".equalsIgnoreCase(skillCat.getNodeName())) {
                                    attrs = skillCat.getAttributes();
                                    int skillId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
                                    int level = Integer.parseInt(attrs.getNamedItem("level").getNodeValue());
                                    if (skillId == 4416) {
                                       template.setRace(level);
                                    } else {
                                       Skill data = SkillsParser.getInstance().getInfo(skillId, level);
                                       if (data != null) {
                                          template.addSkill(data);
                                       } else {
                                          _log.warning(
                                             "["
                                                + this.getCurrentFile().getName()
                                                + "] skill not found. NPC ID: "
                                                + npcId
                                                + " Skill ID:"
                                                + skillId
                                                + " Skill Level: "
                                                + level
                                          );
                                       }
                                    }
                                 }
                              }
                           }
                        } else {
                           RewardType type = RewardType.valueOf(cat.getAttributes().getNamedItem("type").getNodeValue());
                           template.putRewardList(
                              type,
                              RewardList.parseRewardList(
                                 _log, cat, cat.getAttributes(), type, template.isEpicRaid() || template.isRaid(), String.valueOf(npcId)
                              )
                           );
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   protected void parseDocument() {
      for(Node c = this.getCurrentDocument().getFirstChild(); c != null; c = c.getNextSibling()) {
         if ("list".equalsIgnoreCase(c.getNodeName())) {
            for(Node npc = c.getFirstChild(); npc != null; npc = npc.getNextSibling()) {
               if ("npc".equalsIgnoreCase(npc.getNodeName())) {
                  NamedNodeMap attrs = npc.getAttributes();
                  int npcId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
                  int templateId = attrs.getNamedItem("templateId") == null ? npcId : Integer.parseInt(attrs.getNamedItem("templateId").getNodeValue());
                  String nameEn = attrs.getNamedItem("nameEn").getNodeValue();
                  String nameRu = attrs.getNamedItem("nameRu").getNodeValue();
                  String titleEn = attrs.getNamedItem("titleEn").getNodeValue();
                  String titleRu = attrs.getNamedItem("titleRu").getNodeValue();
                  StatsSet set = new StatsSet();
                  set.set("npcId", npcId);
                  set.set("displayId", templateId);
                  set.set("nameEn", nameEn);
                  set.set("nameRu", nameRu);
                  set.set("titleEn", titleEn);
                  set.set("titleRu", titleRu);
                  set.set("baseCpReg", 0);
                  set.set("baseCpMax", 0);

                  for(Node cat = npc.getFirstChild(); cat != null; cat = cat.getNextSibling()) {
                     if ("set".equalsIgnoreCase(cat.getNodeName())) {
                        attrs = cat.getAttributes();
                        set.set(attrs.getNamedItem("name").getNodeValue(), attrs.getNamedItem("value").getNodeValue());
                     } else if ("equip".equalsIgnoreCase(cat.getNodeName())) {
                        for(Node equip = cat.getFirstChild(); equip != null; equip = equip.getNextSibling()) {
                           if ("rhand".equalsIgnoreCase(equip.getNodeName())) {
                              set.set(equip.getNodeName(), Integer.parseInt(equip.getAttributes().getNamedItem("itemId").getNodeValue()));
                           }

                           if ("lhand".equalsIgnoreCase(equip.getNodeName())) {
                              set.set(equip.getNodeName(), Integer.parseInt(equip.getAttributes().getNamedItem("itemId").getNodeValue()));
                           }
                        }
                     }
                  }

                  NpcTemplate template = new NpcTemplate(set);

                  for(Node cat = npc.getFirstChild(); cat != null; cat = cat.getNextSibling()) {
                     if ("faction".equalsIgnoreCase(cat.getNodeName())) {
                        String factionId = cat.getAttributes().getNamedItem("name").getNodeValue();
                        Faction faction = new Faction(factionId);
                        int factionRange = Integer.parseInt(cat.getAttributes().getNamedItem("range").getNodeValue());
                        faction.setRange(factionRange);

                        for(Node ignore = cat.getFirstChild(); ignore != null; ignore = ignore.getNextSibling()) {
                           if ("ignore".equalsIgnoreCase(ignore.getNodeName())) {
                              int ignoreId = Integer.parseInt(ignore.getAttributes().getNamedItem("npcId").getNodeValue());
                              faction.addIgnoreNpcId(ignoreId);
                           }
                        }

                        template.setFaction(faction);
                     } else if ("ai_params".equalsIgnoreCase(cat.getNodeName())) {
                        StatsSet ai = new StatsSet();

                        for(Node params = cat.getFirstChild(); params != null; params = params.getNextSibling()) {
                           if ("set".equalsIgnoreCase(params.getNodeName())) {
                              ai.set(params.getAttributes().getNamedItem("name").getNodeValue(), params.getAttributes().getNamedItem("value").getNodeValue());
                           }
                        }

                        template.setParameters(ai);
                     } else if ("attributes".equalsIgnoreCase(cat.getNodeName())) {
                        for(Node attribute = cat.getFirstChild(); attribute != null; attribute = attribute.getNextSibling()) {
                           if ("attack".equalsIgnoreCase(attribute.getNodeName())) {
                              attrs = attribute.getAttributes();
                              String var40 = attrs.getNamedItem("attribute").getNodeValue();
                              switch(var40) {
                                 case "fire":
                                    template.setBaseFire(Integer.parseInt(attrs.getNamedItem("value").getNodeValue()));
                                    break;
                                 case "wind":
                                    template.setBaseWind(Integer.parseInt(attrs.getNamedItem("value").getNodeValue()));
                                    break;
                                 case "water":
                                    template.setBaseWater(Integer.parseInt(attrs.getNamedItem("value").getNodeValue()));
                                    break;
                                 case "earth":
                                    template.setBaseEarth(Integer.parseInt(attrs.getNamedItem("value").getNodeValue()));
                                    break;
                                 case "holy":
                                    template.setBaseHoly(Integer.parseInt(attrs.getNamedItem("value").getNodeValue()));
                                    break;
                                 case "unholy":
                                    template.setBaseDark(Integer.parseInt(attrs.getNamedItem("value").getNodeValue()));
                              }
                           } else if ("defence".equalsIgnoreCase(attribute.getNodeName())) {
                              attrs = attribute.getAttributes();
                              String var41 = attrs.getNamedItem("attribute").getNodeValue();
                              switch(var41) {
                                 case "fire":
                                    template.setBaseFireRes((double)Integer.parseInt(attrs.getNamedItem("value").getNodeValue()));
                                    break;
                                 case "wind":
                                    template.setBaseWindRes((double)Integer.parseInt(attrs.getNamedItem("value").getNodeValue()));
                                    break;
                                 case "water":
                                    template.setBaseWaterRes((double)Integer.parseInt(attrs.getNamedItem("value").getNodeValue()));
                                    break;
                                 case "earth":
                                    template.setBaseEarthRes((double)Integer.parseInt(attrs.getNamedItem("value").getNodeValue()));
                                    break;
                                 case "holy":
                                    template.setBaseHolyRes((double)Integer.parseInt(attrs.getNamedItem("value").getNodeValue()));
                                    break;
                                 case "unholy":
                                    template.setBaseDarkRes((double)Integer.parseInt(attrs.getNamedItem("value").getNodeValue()));
                              }
                           }
                        }
                     } else if ("minions".equalsIgnoreCase(cat.getNodeName())) {
                        for(Node minion = cat.getFirstChild(); minion != null; minion = minion.getNextSibling()) {
                           if (!"random".equalsIgnoreCase(minion.getNodeName())) {
                              if ("minion".equalsIgnoreCase(minion.getNodeName())) {
                                 attrs = minion.getAttributes();
                                 template.addRaidData(
                                    new MinionData(
                                       new MinionTemplate(
                                          Integer.parseInt(attrs.getNamedItem("npcId").getNodeValue()),
                                          Integer.parseInt(attrs.getNamedItem("count").getNodeValue())
                                       )
                                    ),
                                    false
                                 );
                              }
                           } else {
                              for(Node m = minion.getFirstChild(); m != null; m = m.getNextSibling()) {
                                 if ("minion".equalsIgnoreCase(m.getNodeName())) {
                                    List<MinionTemplate> minions = new ArrayList<>();
                                    attrs = m.getAttributes();
                                    String[] minionsList = attrs.getNamedItem("list").getNodeValue().split(";");

                                    for(String minionId : minionsList) {
                                       String[] minionSplit = minionId.split(",");
                                       if (minionSplit.length == 2) {
                                          minions.add(new MinionTemplate(Integer.parseInt(minionSplit[0]), Integer.parseInt(minionSplit[1])));
                                       }
                                    }

                                    if (!minions.isEmpty()) {
                                       template.addRaidData(new MinionData(minions), true);
                                    }
                                 }
                              }
                           }
                        }
                     } else if ("skills".equalsIgnoreCase(cat.getNodeName())) {
                        for(Node skillCat = cat.getFirstChild(); skillCat != null; skillCat = skillCat.getNextSibling()) {
                           if ("skill".equalsIgnoreCase(skillCat.getNodeName())) {
                              attrs = skillCat.getAttributes();
                              int skillId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
                              int level = Integer.parseInt(attrs.getNamedItem("level").getNodeValue());
                              if (skillId == 4416) {
                                 template.setRace(level);
                              } else {
                                 Skill data = SkillsParser.getInstance().getInfo(skillId, level);
                                 if (data != null) {
                                    template.addSkill(data);
                                 } else {
                                    _log.warning(
                                       "["
                                          + this.getCurrentFile().getName()
                                          + "] skill not found. NPC ID: "
                                          + npcId
                                          + " Skill ID:"
                                          + skillId
                                          + " Skill Level: "
                                          + level
                                    );
                                 }
                              }
                           }
                        }
                     } else if ("teach_classes".equalsIgnoreCase(cat.getNodeName())) {
                        List<ClassId> teachInfo = new ArrayList<>();

                        for(Node teach = cat.getFirstChild(); teach != null; teach = teach.getNextSibling()) {
                           if ("class".equalsIgnoreCase(teach.getNodeName())) {
                              int id = Integer.parseInt(teach.getAttributes().getNamedItem("id").getNodeValue());
                              teachInfo.add(ClassId.values()[id]);
                           }
                        }

                        template.addTeachInfo(teachInfo);
                     } else if ("absorblist".equalsIgnoreCase(cat.getNodeName())) {
                        for(Node absorb = cat.getFirstChild(); absorb != null; absorb = absorb.getNextSibling()) {
                           if ("absorb".equalsIgnoreCase(absorb.getNodeName())) {
                              int chance = Integer.parseInt(absorb.getAttributes().getNamedItem("chance").getNodeValue());
                              int cursedChance = absorb.getAttributes().getNamedItem("cursed_chance") == null
                                 ? 0
                                 : Integer.parseInt(absorb.getAttributes().getNamedItem("cursed_chance").getNodeValue());
                              int minLevel = Integer.parseInt(absorb.getAttributes().getNamedItem("min_level").getNodeValue());
                              int maxLevel = Integer.parseInt(absorb.getAttributes().getNamedItem("max_level").getNodeValue());
                              boolean skill = absorb.getAttributes().getNamedItem("skill") != null
                                 && Boolean.parseBoolean(absorb.getAttributes().getNamedItem("skill").getNodeValue());
                              AbsorbInfo.AbsorbType absorbType = AbsorbInfo.AbsorbType.valueOf(absorb.getAttributes().getNamedItem("type").getNodeValue());
                              template.addAbsorbInfo(new AbsorbInfo(skill, absorbType, chance, cursedChance, minLevel, maxLevel));
                           }
                        }
                     } else if ("rewardlist".equalsIgnoreCase(cat.getNodeName())) {
                        RewardType type = RewardType.valueOf(cat.getAttributes().getNamedItem("type").getNodeValue());
                        template.putRewardList(
                           type,
                           RewardList.parseRewardList(_log, cat, cat.getAttributes(), type, template.isEpicRaid() || template.isRaid(), String.valueOf(npcId))
                        );
                     }
                  }

                  this._npcs.put(npcId, template);
               }
            }
         }
      }
   }

   public NpcTemplate getTemplate(int id) {
      return this._npcs.get(id);
   }

   public NpcTemplate getTemplateByName(String name) {
      for(NpcTemplate npcTemplate : this._npcs.values()) {
         if (npcTemplate.getName().equalsIgnoreCase(name)) {
            return npcTemplate;
         }
      }

      return null;
   }

   public Collection<NpcTemplate> getAllNpcs() {
      return this._npcs.values();
   }

   public List<NpcTemplate> getAllOfLevel(int... lvls) {
      List<NpcTemplate> list = new ArrayList<>();

      for(int lvl : lvls) {
         for(NpcTemplate t : this._npcs.values()) {
            if (t.getLevel() == lvl) {
               list.add(t);
            }
         }
      }

      return list;
   }

   public List<NpcTemplate> getAllMonstersOfLevel(int... lvls) {
      List<NpcTemplate> list = new ArrayList<>();

      for(int lvl : lvls) {
         for(NpcTemplate t : this._npcs.values()) {
            if (t.getLevel() == lvl && t.isType("Monster")) {
               list.add(t);
            }
         }
      }

      return list;
   }

   public List<NpcTemplate> getAllNpcStartingWith(String... letters) {
      List<NpcTemplate> list = new ArrayList<>();

      for(String letter : letters) {
         for(NpcTemplate t : this._npcs.values()) {
            if (t.getName().startsWith(letter) && t.isType("Npc")) {
               list.add(t);
            }
         }
      }

      return list;
   }

   public List<NpcTemplate> getAllNpcOfClassType(String... classTypes) {
      List<NpcTemplate> list = new ArrayList<>();

      for(String classType : classTypes) {
         for(NpcTemplate t : this._npcs.values()) {
            if (t.isType(classType)) {
               list.add(t);
            }
         }
      }

      return list;
   }

   public void reloadAllDropAndSkills() {
      for(NpcTemplate template : this.getAllNpcs()) {
         if (template != null) {
            if (template.getRewards() != null) {
               template.getRewards().clear();
            }

            template.getSkills().clear();
         }
      }

      this.parseDirectory("data/stats/npcs/npcs", true);
      if (Config.CUSTOM_NPC) {
         this.parseDirectory("data/stats/npcs/npcs/custom", true);
      }

      _log.info(this.getClass().getSimpleName() + ": Reloaded all npc drop templates.");
      DropManager.getInstance().reload();
   }

   public static NpcsParser getInstance() {
      return NpcsParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final NpcsParser _instance = new NpcsParser();
   }
}
