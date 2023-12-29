package l2e.gameserver.data.parser;

import gnu.trove.map.hash.TIntObjectHashMap;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import l2e.gameserver.Config;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.SkillLearn;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.base.AcquireSkillType;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.base.Race;
import l2e.gameserver.model.base.SocialClass;
import l2e.gameserver.model.base.SubClass;
import l2e.gameserver.model.holders.ItemHolder;
import l2e.gameserver.model.holders.PlayerSkillHolder;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.interfaces.ISkillsHolder;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.stats.StatsSet;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class SkillTreesParser extends DocumentParser {
   private static final Map<ClassId, Map<Integer, SkillLearn>> _classSkillTrees = new HashMap<>();
   private static final Map<ClassId, Map<Integer, SkillLearn>> _transferSkillTrees = new HashMap<>();
   private static final Map<Integer, SkillLearn> _collectSkillTree = new HashMap<>();
   private static final Map<Integer, SkillLearn> _fishingSkillTree = new HashMap<>();
   private static final Map<Integer, SkillLearn> _pledgeSkillTree = new HashMap<>();
   private static final Map<Integer, SkillLearn> _subClassSkillTree = new HashMap<>();
   private static final Map<Integer, SkillLearn> _subPledgeSkillTree = new HashMap<>();
   private static final Map<Integer, SkillLearn> _transformSkillTree = new HashMap<>();
   private static final Map<Integer, SkillLearn> _commonSkillTree = new HashMap<>();
   private static final Map<Integer, SkillLearn> _nobleSkillTree = new HashMap<>();
   private static final Map<Integer, SkillLearn> _heroSkillTree = new HashMap<>();
   private static final Map<Integer, SkillLearn> _gameMasterSkillTree = new HashMap<>();
   private static final Map<Integer, SkillLearn> _gameMasterAuraSkillTree = new HashMap<>();
   private static final List<SkillLearn> _mutiProffSkills = new ArrayList<>();
   private TIntObjectHashMap<int[]> _skillsByClassIdHashCodes;
   private TIntObjectHashMap<int[]> _skillsByRaceHashCodes;
   TIntObjectHashMap<ArrayList<Integer>> _restrictedSkills = new TIntObjectHashMap<>();
   private int[] _allSkillsHashCodes;
   private boolean _loading = true;
   private static final Map<ClassId, ClassId> _parentClassMap = new HashMap<>();

   protected SkillTreesParser() {
      this.load();
   }

   @Override
   public void load() {
      this._loading = true;
      _classSkillTrees.clear();
      _collectSkillTree.clear();
      _fishingSkillTree.clear();
      _pledgeSkillTree.clear();
      _subClassSkillTree.clear();
      _subPledgeSkillTree.clear();
      _transferSkillTrees.clear();
      _transformSkillTree.clear();
      _nobleSkillTree.clear();
      _heroSkillTree.clear();
      _gameMasterSkillTree.clear();
      _gameMasterAuraSkillTree.clear();
      _mutiProffSkills.clear();
      this.parseDirectory(new File(Config.DATAPACK_ROOT, "data/stats/skills/skillTrees/"));
      this.generateCheckArrays();
      this._loading = false;
      this.report();
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      String type = null;
      int cId = -1;
      int parentClassId = -1;
      ClassId classId = null;

      for(Node n = this.getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equalsIgnoreCase(n.getNodeName())) {
            for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
               if ("skillTree".equalsIgnoreCase(d.getNodeName())) {
                  Map<Integer, SkillLearn> classSkillTree = new HashMap<>();
                  Map<Integer, SkillLearn> trasferSkillTree = new HashMap<>();
                  type = d.getAttributes().getNamedItem("type").getNodeValue();
                  Node attr = d.getAttributes().getNamedItem("classId");
                  if (attr != null) {
                     cId = Integer.parseInt(attr.getNodeValue());
                     classId = ClassId.values()[cId];
                  } else {
                     cId = -1;
                  }

                  attr = d.getAttributes().getNamedItem("parentClassId");
                  if (attr != null) {
                     parentClassId = Integer.parseInt(attr.getNodeValue());
                     if (cId > -1 && cId != parentClassId && parentClassId > -1 && !_parentClassMap.containsKey(classId)) {
                        _parentClassMap.put(classId, ClassId.values()[parentClassId]);
                     }
                  }

                  for(Node c = d.getFirstChild(); c != null; c = c.getNextSibling()) {
                     if ("skill".equalsIgnoreCase(c.getNodeName())) {
                        StatsSet learnSkillSet = new StatsSet();
                        NamedNodeMap attrs = c.getAttributes();

                        for(int i = 0; i < attrs.getLength(); ++i) {
                           attr = attrs.item(i);
                           learnSkillSet.set(attr.getNodeName(), attr.getNodeValue());
                        }

                        SkillLearn skillLearn = new SkillLearn(learnSkillSet);

                        for(Node b = c.getFirstChild(); b != null; b = b.getNextSibling()) {
                           attrs = b.getAttributes();
                           String var15 = b.getNodeName();
                           switch(var15) {
                              case "item":
                                 skillLearn.addRequiredItem(new ItemHolder(parseInt(attrs, "id"), (long)parseInt(attrs, "count")));
                                 break;
                              case "preRequisiteSkill":
                                 skillLearn.addPreReqSkill(new SkillHolder(parseInt(attrs, "id"), parseInt(attrs, "lvl")));
                                 break;
                              case "race":
                                 skillLearn.addRace(Race.valueOf(b.getTextContent()));
                                 break;
                              case "residenceId":
                                 skillLearn.addResidenceId(Integer.valueOf(b.getTextContent()));
                                 break;
                              case "socialClass":
                                 skillLearn.setSocialClass(Enum.valueOf(SocialClass.class, b.getTextContent()));
                                 break;
                              case "subClassConditions":
                                 skillLearn.addSubclassConditions(parseInt(attrs, "slot"), parseInt(attrs, "lvl"));
                           }
                        }

                        int skillHashCode = SkillsParser.getSkillHashCode(skillLearn.getId(), skillLearn.getLvl());
                        switch(type) {
                           case "classSkillTree":
                              if (cId != -1) {
                                 classSkillTree.put(skillHashCode, skillLearn);
                              } else {
                                 _commonSkillTree.put(skillHashCode, skillLearn);
                              }
                              break;
                           case "transferSkillTree":
                              trasferSkillTree.put(skillHashCode, skillLearn);
                              break;
                           case "collectSkillTree":
                              _collectSkillTree.put(skillHashCode, skillLearn);
                              break;
                           case "fishingSkillTree":
                              _fishingSkillTree.put(skillHashCode, skillLearn);
                              break;
                           case "pledgeSkillTree":
                              _pledgeSkillTree.put(skillHashCode, skillLearn);
                              break;
                           case "subClassSkillTree":
                              _subClassSkillTree.put(skillHashCode, skillLearn);
                              break;
                           case "subPledgeSkillTree":
                              _subPledgeSkillTree.put(skillHashCode, skillLearn);
                              break;
                           case "transformSkillTree":
                              _transformSkillTree.put(skillHashCode, skillLearn);
                              break;
                           case "nobleSkillTree":
                              _nobleSkillTree.put(skillHashCode, skillLearn);
                              break;
                           case "heroSkillTree":
                              _heroSkillTree.put(skillHashCode, skillLearn);
                              break;
                           case "gameMasterSkillTree":
                              _gameMasterSkillTree.put(skillHashCode, skillLearn);
                              break;
                           case "gameMasterAuraSkillTree":
                              _gameMasterAuraSkillTree.put(skillHashCode, skillLearn);
                              break;
                           default:
                              this._log.warning(this.getClass().getSimpleName() + ": Unknown Skill Tree type: " + type + "!");
                        }
                     }
                  }

                  if (type.equals("transferSkillTree")) {
                     _transferSkillTrees.put(classId, trasferSkillTree);
                  } else if (type.equals("classSkillTree") && cId > -1) {
                     if (!_classSkillTrees.containsKey(classId)) {
                        _classSkillTrees.put(classId, classSkillTree);
                     } else {
                        _classSkillTrees.get(classId).putAll(classSkillTree);
                     }
                  }
               }
            }
         }
      }
   }

   public Map<Integer, SkillLearn> getCompleteClassSkillTree(ClassId classId) {
      Map<Integer, SkillLearn> skillTree = new HashMap<>();
      skillTree.putAll(_commonSkillTree);

      while(classId != null && _classSkillTrees.get(classId) != null) {
         skillTree.putAll(_classSkillTrees.get(classId));
         classId = _parentClassMap.get(classId);
      }

      return skillTree;
   }

   public Map<Integer, SkillLearn> getAllClassSkillTree(ClassId classId) {
      Map<Integer, SkillLearn> skillTree = new HashMap<>();
      skillTree.putAll(_commonSkillTree);
      if (_classSkillTrees.get(classId) != null) {
         skillTree.putAll(_classSkillTrees.get(classId));
         if (classId.level() == 1) {
            classId = _parentClassMap.get(classId);
            if (classId != null) {
               skillTree.putAll(_classSkillTrees.get(classId));
            }
         }
      }

      return skillTree;
   }

   public Map<Integer, SkillLearn> getTransferSkillTree(ClassId classId) {
      return classId.level() >= 3 ? this.getTransferSkillTree(classId.getParent()) : _transferSkillTrees.get(classId);
   }

   public Map<Integer, SkillLearn> getCommonSkillTree() {
      return _commonSkillTree;
   }

   public Map<Integer, SkillLearn> getCollectSkillTree() {
      return _collectSkillTree;
   }

   public Map<Integer, SkillLearn> getFishingSkillTree() {
      return _fishingSkillTree;
   }

   public Map<Integer, SkillLearn> getPledgeSkillTree() {
      return _pledgeSkillTree;
   }

   public Map<Integer, SkillLearn> getSubClassSkillTree() {
      return _subClassSkillTree;
   }

   public Map<Integer, SkillLearn> getSubPledgeSkillTree() {
      return _subPledgeSkillTree;
   }

   public Map<Integer, SkillLearn> getTransformSkillTree() {
      return _transformSkillTree;
   }

   public Map<Integer, Skill> getNobleSkillTree() {
      Map<Integer, Skill> tree = new HashMap<>();
      SkillsParser st = SkillsParser.getInstance();

      for(Entry<Integer, SkillLearn> e : _nobleSkillTree.entrySet()) {
         tree.put(e.getKey(), st.getInfo(e.getValue().getId(), e.getValue().getLvl()));
      }

      return tree;
   }

   public Map<Integer, Skill> getHeroSkillTree() {
      Map<Integer, Skill> tree = new HashMap<>();
      SkillsParser st = SkillsParser.getInstance();

      for(Entry<Integer, SkillLearn> e : _heroSkillTree.entrySet()) {
         tree.put(e.getKey(), st.getInfo(e.getValue().getId(), e.getValue().getLvl()));
      }

      return tree;
   }

   public Map<Integer, Skill> getGMSkillTree() {
      Map<Integer, Skill> tree = new HashMap<>();
      SkillsParser st = SkillsParser.getInstance();

      for(Entry<Integer, SkillLearn> e : _gameMasterSkillTree.entrySet()) {
         tree.put(e.getKey(), st.getInfo(e.getValue().getId(), e.getValue().getLvl()));
      }

      return tree;
   }

   public Map<Integer, Skill> getGMAuraSkillTree() {
      Map<Integer, Skill> tree = new HashMap<>();
      SkillsParser st = SkillsParser.getInstance();

      for(Entry<Integer, SkillLearn> e : _gameMasterAuraSkillTree.entrySet()) {
         tree.put(e.getKey(), st.getInfo(e.getValue().getId(), e.getValue().getLvl()));
      }

      return tree;
   }

   public List<SkillLearn> getAvailableSkills(Player player, ClassId classId, boolean includeByFs, boolean includeAutoGet) {
      return this.getAvailableSkills(player, classId, includeByFs, includeAutoGet, player);
   }

   private List<SkillLearn> getAvailableSkills(Player player, ClassId classId, boolean includeByFs, boolean includeAutoGet, ISkillsHolder holder) {
      List<SkillLearn> result = new ArrayList<>();
      Map<Integer, SkillLearn> skills = this.getCompleteClassSkillTree(classId);
      if (skills.isEmpty()) {
         this._log.warning(this.getClass().getSimpleName() + ": Skilltree for class " + classId + " is not defined!");
         return result;
      } else {
         for(SkillLearn skill : skills.values()) {
            if ((includeAutoGet && skill.isAutoGet() || skill.isLearnedByNpc() || includeByFs && skill.isLearnedByFS())
               && player.getLevel() >= skill.getGetLevel()) {
               Skill oldSkill = holder.getKnownSkill(skill.getId());
               if (oldSkill != null) {
                  if (oldSkill.getLevel() == skill.getLvl() - 1) {
                     result.add(skill);
                  }
               } else if (skill.getLvl() == 1) {
                  result.add(skill);
               }
            }
         }

         return result;
      }
   }

   private List<SkillLearn> getAvailablePlayerSkills(Player player, ClassId classId, boolean includeByFs, boolean includeAutoGet, ISkillsHolder holder) {
      List<SkillLearn> result = new ArrayList<>();
      Map<Integer, SkillLearn> skills = this.getCompleteClassSkillTree(classId);
      if (skills.isEmpty()) {
         this._log.warning(this.getClass().getSimpleName() + ": Skilltree for class " + classId + " is not defined!");
         return result;
      } else {
         for(SkillLearn skill : skills.values()) {
            if ((includeAutoGet && skill.isAutoGet() || skill.isLearnedByNpc() || includeByFs && skill.isLearnedByFS())
               && player.getLevel() >= skill.getGetLevel()) {
               Skill oldSkill = holder.getKnownSkill(skill.getId());
               if (oldSkill != null) {
                  if (oldSkill.getLevel() < skill.getLvl()) {
                     result.add(skill);
                  }
               } else {
                  result.add(skill);
               }
            }
         }

         return result;
      }
   }

   public Collection<Skill> getAllAvailableSkills(Player player, ClassId classId, boolean includeByFs, boolean includeAutoGet) {
      int unLearnable = 0;
      PlayerSkillHolder holder = new PlayerSkillHolder(player);

      for(List<SkillLearn> learnable = this.getAvailablePlayerSkills(player, classId, includeByFs, includeAutoGet, holder);
         learnable.size() > unLearnable;
         learnable = this.getAvailablePlayerSkills(player, classId, includeByFs, includeAutoGet, holder)
      ) {
         for(SkillLearn s : learnable) {
            Skill sk = SkillsParser.getInstance().getInfo(s.getId(), s.getLvl());
            if (sk != null && (sk.getId() != 1405 || Config.AUTO_LEARN_DIVINE_INSPIRATION || player.isGM())) {
               holder.addSkill(sk);
            } else {
               ++unLearnable;
            }
         }
      }

      return holder.getSkills().values();
   }

   public List<SkillLearn> getAvailableAutoGetSkills(Player player) {
      List<SkillLearn> result = new ArrayList<>();
      Map<Integer, SkillLearn> skills = this.getCompleteClassSkillTree(player.getClassId());
      if (skills.isEmpty()) {
         this._log.warning(this.getClass().getSimpleName() + ": Skill Tree for this class Id(" + player.getClassId() + ") is not defined!");
         return result;
      } else {
         Race race = player.getRace();

         for(SkillLearn skill : skills.values()) {
            if ((skill.getRaces().isEmpty() || skill.getRaces().contains(race)) && skill.isAutoGet() && player.getLevel() >= skill.getGetLevel()) {
               Skill oldSkill = player.getSkills().get(skill.getId());
               if (oldSkill != null) {
                  if (oldSkill.getLevel() < skill.getLvl()) {
                     result.add(skill);
                  }
               } else {
                  result.add(skill);
               }
            }
         }

         return result;
      }
   }

   public List<SkillLearn> getAvailableFishingSkills(Player player) {
      List<SkillLearn> result = new ArrayList<>();
      Race playerRace = player.getRace();

      for(SkillLearn skill : _fishingSkillTree.values()) {
         if ((skill.getRaces().isEmpty() || skill.getRaces().contains(playerRace)) && skill.isLearnedByNpc() && player.getLevel() >= skill.getGetLevel()) {
            Skill oldSkill = player.getSkills().get(skill.getId());
            if (oldSkill != null) {
               if (oldSkill.getLevel() == skill.getLvl() - 1) {
                  result.add(skill);
               }
            } else if (skill.getLvl() == 1) {
               result.add(skill);
            }
         }
      }

      return result;
   }

   public List<SkillLearn> getAvailableCollectSkills(Player player) {
      List<SkillLearn> result = new ArrayList<>();

      for(SkillLearn skill : _collectSkillTree.values()) {
         Skill oldSkill = player.getSkills().get(skill.getId());
         if (oldSkill != null) {
            if (oldSkill.getLevel() == skill.getLvl() - 1) {
               result.add(skill);
            }
         } else if (skill.getLvl() == 1) {
            result.add(skill);
         }
      }

      return result;
   }

   public List<SkillLearn> getAvailableTransferSkills(Player player) {
      List<SkillLearn> result = new ArrayList<>();
      ClassId classId = player.getClassId();
      if (classId.level() == 3) {
         classId = classId.getParent();
      }

      if (!_transferSkillTrees.containsKey(classId)) {
         return result;
      } else {
         for(SkillLearn skill : _transferSkillTrees.get(classId).values()) {
            if (player.getKnownSkill(skill.getId()) == null) {
               result.add(skill);
            }
         }

         return result;
      }
   }

   public List<SkillLearn> getAvailableTransformSkills(Player player) {
      List<SkillLearn> result = new ArrayList<>();
      Race race = player.getRace();

      for(SkillLearn skill : _transformSkillTree.values()) {
         if (player.getLevel() >= skill.getGetLevel() && (skill.getRaces().isEmpty() || skill.getRaces().contains(race))) {
            Skill oldSkill = player.getSkills().get(skill.getId());
            if (oldSkill != null) {
               if (oldSkill.getLevel() == skill.getLvl() - 1) {
                  result.add(skill);
               }
            } else if (skill.getLvl() == 1) {
               result.add(skill);
            }
         }
      }

      return result;
   }

   public List<SkillLearn> getAvailablePledgeSkills(Clan clan) {
      List<SkillLearn> result = new ArrayList<>();

      for(SkillLearn skill : _pledgeSkillTree.values()) {
         if (!skill.isResidencialSkill() && clan.getLevel() >= skill.getGetLevel()) {
            Skill oldSkill = clan.getSkills().get(skill.getId());
            if (oldSkill != null) {
               if (oldSkill.getLevel() == skill.getLvl() - 1) {
                  result.add(skill);
               }
            } else if (skill.getLvl() == 1) {
               result.add(skill);
            }
         }
      }

      return result;
   }

   public List<SkillLearn> getAvailableSubPledgeSkills(Clan clan) {
      List<SkillLearn> result = new ArrayList<>();

      for(SkillLearn skill : _subPledgeSkillTree.values()) {
         if (clan.getLevel() >= skill.getGetLevel() && clan.isLearnableSubSkill(skill.getId(), skill.getLvl())) {
            result.add(skill);
         }
      }

      return result;
   }

   public List<SkillLearn> getAvailableSubClassSkills(Player player) {
      List<SkillLearn> result = new ArrayList<>();

      for(SkillLearn skill : _subClassSkillTree.values()) {
         if (player.getLevel() >= skill.getGetLevel()) {
            List<SkillLearn.SubClassData> subClassConds = null;

            for(SubClass subClass : player.getSubClasses().values()) {
               subClassConds = skill.getSubClassConditions();
               if (!subClassConds.isEmpty()
                  && subClass.getClassIndex() <= subClassConds.size()
                  && subClass.getClassIndex() == subClassConds.get(subClass.getClassIndex() - 1).getSlot()
                  && subClassConds.get(subClass.getClassIndex() - 1).getLvl() <= subClass.getLevel()) {
                  Skill oldSkill = player.getSkills().get(skill.getId());
                  if (oldSkill != null) {
                     if (oldSkill.getLevel() == skill.getLvl() - 1) {
                        result.add(skill);
                     }
                  } else if (skill.getLvl() == 1) {
                     result.add(skill);
                  }
               }
            }
         }
      }

      return result;
   }

   public boolean isSubClassSkill(int skillId) {
      for(SkillLearn skill : _subClassSkillTree.values()) {
         if (skill != null) {
            return skill.getId() == skillId;
         }
      }

      return false;
   }

   public List<SkillLearn> getAvailableResidentialSkills(int residenceId) {
      List<SkillLearn> result = new ArrayList<>();

      for(SkillLearn skill : _pledgeSkillTree.values()) {
         if (skill.isResidencialSkill() && skill.getResidenceIds().contains(residenceId)) {
            result.add(skill);
         }
      }

      return result;
   }

   public SkillLearn getSkillLearn(AcquireSkillType skillType, int id, int lvl, Player player) {
      SkillLearn sl = null;
      switch(skillType) {
         case CLASS:
            sl = this.getClassSkill(id, lvl, player.getLearningClass());
            break;
         case TRANSFORM:
            sl = this.getTransformSkill(id, lvl);
            break;
         case FISHING:
            sl = this.getFishingSkill(id, lvl);
            break;
         case PLEDGE:
            sl = this.getPledgeSkill(id, lvl);
            break;
         case SUBPLEDGE:
            sl = this.getSubPledgeSkill(id, lvl);
            break;
         case TRANSFER:
            sl = this.getTransferSkill(id, lvl, player.getClassId());
            break;
         case SUBCLASS:
            sl = this.getSubClassSkill(id, lvl);
            break;
         case COLLECT:
            sl = this.getCollectSkill(id, lvl);
      }

      return sl;
   }

   public SkillLearn getTransformSkill(int id, int lvl) {
      return _transformSkillTree.get(SkillsParser.getSkillHashCode(id, lvl));
   }

   public SkillLearn getClassSkill(int id, int lvl, ClassId classId) {
      return this.getCompleteClassSkillTree(classId).get(SkillsParser.getSkillHashCode(id, lvl));
   }

   public SkillLearn getFishingSkill(int id, int lvl) {
      return _fishingSkillTree.get(SkillsParser.getSkillHashCode(id, lvl));
   }

   public SkillLearn getPledgeSkill(int id, int lvl) {
      return _pledgeSkillTree.get(SkillsParser.getSkillHashCode(id, lvl));
   }

   public SkillLearn getSubPledgeSkill(int id, int lvl) {
      return _subPledgeSkillTree.get(SkillsParser.getSkillHashCode(id, lvl));
   }

   public SkillLearn getTransferSkill(int id, int lvl, ClassId classId) {
      if (classId.getParent() != null) {
         ClassId parentId = classId.getParent();
         if (_transferSkillTrees.get(parentId) != null) {
            return _transferSkillTrees.get(parentId).get(SkillsParser.getSkillHashCode(id, lvl));
         }
      }

      return null;
   }

   public SkillLearn getSubClassSkill(int id, int lvl) {
      return _subClassSkillTree.get(SkillsParser.getSkillHashCode(id, lvl));
   }

   public SkillLearn getCommonSkill(int id, int lvl) {
      return _commonSkillTree.get(SkillsParser.getSkillHashCode(id, lvl));
   }

   public SkillLearn getCollectSkill(int id, int lvl) {
      return _collectSkillTree.get(SkillsParser.getSkillHashCode(id, lvl));
   }

   public int getMinLevelForNewSkill(Player player, Map<Integer, SkillLearn> skillTree) {
      int minLevel = 0;
      if (skillTree.isEmpty()) {
         this._log.warning(this.getClass().getSimpleName() + ": SkillTree is not defined for getMinLevelForNewSkill!");
      } else {
         for(SkillLearn s : skillTree.values()) {
            if (s.isLearnedByNpc() && player.getLevel() < s.getGetLevel() && (minLevel == 0 || minLevel > s.getGetLevel())) {
               minLevel = s.getGetLevel();
            }
         }
      }

      return minLevel;
   }

   public boolean isNotCheckSkill(Player player, int id, int lvl) {
      SkillLearn sl = this.getTransformSkill(id, lvl);
      if (sl != null && player.isTransformed()) {
         return true;
      } else if (this.isGMSkill(id, lvl) && player.isGM()) {
         return true;
      } else {
         sl = this.getFishingSkill(id, lvl);
         if (sl != null) {
            return true;
         } else {
            sl = this.getCommonSkill(id, lvl);
            if (sl != null) {
               return true;
            } else {
               sl = this.getPledgeSkill(id, lvl);
               if (sl != null && player.getClan() != null) {
                  return true;
               } else {
                  sl = this.getSubPledgeSkill(id, lvl);
                  if (sl != null && player.getClan() != null) {
                     return true;
                  } else {
                     sl = this.getTransferSkill(id, lvl, player.getClassId());
                     if (sl != null) {
                        return true;
                     } else {
                        sl = this.getSubClassSkill(id, lvl);
                        if (sl != null) {
                           return true;
                        } else {
                           sl = this.getCollectSkill(id, lvl);
                           if (sl != null) {
                              return true;
                           } else {
                              sl = this.getNobleSkill(id, lvl);
                              if (sl != null && player.isNoble()) {
                                 return true;
                              } else {
                                 return this.isHeroSkill(id, lvl) && player.isHero();
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public SkillLearn getNobleSkill(int id, int lvl) {
      return _nobleSkillTree.get(SkillsParser.getSkillHashCode(id, lvl));
   }

   public boolean isHeroSkill(int skillId, int skillLevel) {
      if (_heroSkillTree.containsKey(SkillsParser.getSkillHashCode(skillId, skillLevel))) {
         return true;
      } else {
         for(SkillLearn skill : _heroSkillTree.values()) {
            if (skill.getId() == skillId && skillLevel == -1) {
               return true;
            }
         }

         return false;
      }
   }

   public boolean isGMSkill(int skillId, int skillLevel) {
      Map<Integer, SkillLearn> gmSkills = new HashMap<>();
      gmSkills.putAll(_gameMasterSkillTree);
      gmSkills.putAll(_gameMasterAuraSkillTree);
      if (gmSkills.containsKey(SkillsParser.getSkillHashCode(skillId, skillLevel))) {
         return true;
      } else {
         for(SkillLearn skill : gmSkills.values()) {
            if (skill.getId() == skillId && skillLevel == -1) {
               return true;
            }
         }

         return false;
      }
   }

   public boolean isClanSkill(int skillId, int skillLevel) {
      int hashCode = SkillsParser.getSkillHashCode(skillId, skillId);
      return _pledgeSkillTree.containsKey(hashCode) || _subPledgeSkillTree.containsKey(hashCode);
   }

   public void addSkills(Player gmchar, boolean auraSkills) {
      Collection<SkillLearn> skills = auraSkills ? _gameMasterAuraSkillTree.values() : _gameMasterSkillTree.values();
      SkillsParser st = SkillsParser.getInstance();

      for(SkillLearn sl : skills) {
         gmchar.addSkill(st.getInfo(sl.getId(), sl.getLvl()), false);
      }
   }

   private void generateCheckArrays() {
      Set<ClassId> keySet = _classSkillTrees.keySet();
      this._skillsByClassIdHashCodes = new TIntObjectHashMap<>(keySet.size());

      for(ClassId cls : keySet) {
         int i = 0;
         Map<Integer, SkillLearn> tempMap = this.getCompleteClassSkillTree(cls);
         int[] array = new int[tempMap.size()];

         for(int h : tempMap.keySet()) {
            array[i++] = h;
         }

         tempMap.clear();
         Arrays.sort(array);
         this._skillsByClassIdHashCodes.put(cls.ordinal(), array);
      }

      List<Integer> list = new ArrayList<>();
      this._skillsByRaceHashCodes = new TIntObjectHashMap<>(Race.values().length);

      for(Race r : Race.values()) {
         for(SkillLearn s : _fishingSkillTree.values()) {
            if (s.getRaces().contains(r)) {
               list.add(SkillsParser.getSkillHashCode(s.getId(), s.getLvl()));
            }
         }

         for(SkillLearn s : _transformSkillTree.values()) {
            if (s.getRaces().contains(r)) {
               list.add(SkillsParser.getSkillHashCode(s.getId(), s.getLvl()));
            }
         }

         int i = 0;
         int[] array = new int[list.size()];

         for(int s : list) {
            array[i++] = s;
         }

         Arrays.sort(array);
         this._skillsByRaceHashCodes.put(r.ordinal(), array);
         list.clear();
      }

      for(SkillLearn s : _commonSkillTree.values()) {
         if (s.getRaces().isEmpty()) {
            list.add(SkillsParser.getSkillHashCode(s.getId(), s.getLvl()));
         }
      }

      for(SkillLearn s : _fishingSkillTree.values()) {
         if (s.getRaces().isEmpty()) {
            list.add(SkillsParser.getSkillHashCode(s.getId(), s.getLvl()));
         }
      }

      for(SkillLearn s : _transformSkillTree.values()) {
         if (s.getRaces().isEmpty()) {
            list.add(SkillsParser.getSkillHashCode(s.getId(), s.getLvl()));
         }
      }

      for(SkillLearn s : _collectSkillTree.values()) {
         list.add(SkillsParser.getSkillHashCode(s.getId(), s.getLvl()));
      }

      this._allSkillsHashCodes = new int[list.size()];
      int j = 0;

      for(int hashcode : list) {
         this._allSkillsHashCodes[j++] = hashcode;
      }

      Arrays.sort(this._allSkillsHashCodes);
   }

   public boolean isSkillAllowed(Player player, Skill skill) {
      if (skill.isExcludedFromCheck()) {
         return true;
      } else if (player.isGM() && skill.isGMSkill()) {
         return true;
      } else if (this._loading) {
         return true;
      } else {
         int maxLvl = SkillsParser.getInstance().getMaxLevel(skill.getId());
         int hashCode = SkillsParser.getSkillHashCode(skill.getId(), Math.min(skill.getLevel(), maxLvl));
         if (Arrays.binarySearch(this._skillsByClassIdHashCodes.get(player.getClassId().ordinal()), hashCode) >= 0) {
            return true;
         } else if (Arrays.binarySearch(this._skillsByRaceHashCodes.get(player.getRace().ordinal()), hashCode) >= 0) {
            return true;
         } else if (Arrays.binarySearch(this._allSkillsHashCodes, hashCode) >= 0) {
            return true;
         } else {
            return this.getTransferSkill(skill.getId(), Math.min(skill.getLevel(), maxLvl), player.getClassId()) != null;
         }
      }
   }

   private void report() {
      int classSkillTreeCount = 0;

      for(Map<Integer, SkillLearn> classSkillTree : _classSkillTrees.values()) {
         classSkillTreeCount += classSkillTree.size();
      }

      int trasferSkillTreeCount = 0;

      for(Map<Integer, SkillLearn> trasferSkillTree : _transferSkillTrees.values()) {
         trasferSkillTreeCount += trasferSkillTree.size();
      }

      int dwarvenOnlyFishingSkillCount = 0;

      for(SkillLearn fishSkill : _fishingSkillTree.values()) {
         if (fishSkill.getRaces().contains(Race.Dwarf)) {
            ++dwarvenOnlyFishingSkillCount;
         }
      }

      int resSkillCount = 0;

      for(SkillLearn pledgeSkill : _pledgeSkillTree.values()) {
         if (pledgeSkill.isResidencialSkill()) {
            ++resSkillCount;
         }
      }

      if (Config.DEBUG) {
         String className = this.getClass().getSimpleName();
         this._log.info(className + ": Loaded " + classSkillTreeCount + " Class Skills for " + _classSkillTrees.size() + " Class Skill Trees.");
         this._log.info(className + ": Loaded " + _subClassSkillTree.size() + " Sub-Class Skills.");
         this._log.info(className + ": Loaded " + trasferSkillTreeCount + " Transfer Skills for " + _transferSkillTrees.size() + " Transfer Skill Trees.");
         this._log
            .info(className + ": Loaded " + _fishingSkillTree.size() + " Fishing Skills, " + dwarvenOnlyFishingSkillCount + " Dwarven only Fishing Skills.");
         this._log.info(className + ": Loaded " + _collectSkillTree.size() + " Collect Skills.");
         this._log
            .info(
               className
                  + ": Loaded "
                  + _pledgeSkillTree.size()
                  + " Pledge Skills, "
                  + (_pledgeSkillTree.size() - resSkillCount)
                  + " for Pledge and "
                  + resSkillCount
                  + " Residential."
            );
         this._log.info(className + ": Loaded " + _subPledgeSkillTree.size() + " Sub-Pledge Skills.");
         this._log.info(className + ": Loaded " + _transformSkillTree.size() + " Transform Skills.");
         this._log.info(className + ": Loaded " + _nobleSkillTree.size() + " Noble Skills.");
         this._log.info(className + ": Loaded " + _heroSkillTree.size() + " Hero Skills.");
         this._log.info(className + ": Loaded " + _gameMasterSkillTree.size() + " Game Master Skills.");
         this._log.info(className + ": Loaded " + _gameMasterAuraSkillTree.size() + " Game Master Aura Skills.");
         int commonSkills = _commonSkillTree.size();
         if (commonSkills > 0) {
            this._log.info(className + ": Loaded " + commonSkills + " Common Skills to all classes.");
         }
      }

      this.loadRestrictedSkills();
      this.collectAllClassesSkill();
   }

   private void loadRestrictedSkills() {
      TIntObjectHashMap<ArrayList<Integer>> allowedSkillIds = new TIntObjectHashMap<>();

      for(ClassId classid : ClassId.values()) {
         if (classid.getRace() != null) {
            Map<Integer, SkillLearn> skills = this.getCompleteClassSkillTree(classid);
            ArrayList<Integer> skillIds = new ArrayList<>();

            for(SkillLearn sk : skills.values()) {
               if (!skillIds.contains(sk.getId())) {
                  skillIds.add(sk.getId());
               }
            }

            allowedSkillIds.put(classid.getId(), skillIds);
         }
      }

      for(ClassId classId : ClassId.values()) {
         if (classId.getRace() != null) {
            ArrayList<Integer> skillIds = new ArrayList<>();

            for(ClassId classid : ClassId.values()) {
               if (classid != classId && classid.getRace() != null && !classId.childOf(classid)) {
                  for(Integer skillId : allowedSkillIds.get(classid.getId())) {
                     if (!skillIds.contains(skillId)) {
                        skillIds.add(skillId);
                     }
                  }
               }
            }

            for(Integer skillId : allowedSkillIds.get(classId.getId())) {
               if (skillIds.contains(skillId)) {
                  skillIds.remove(skillId);
               }
            }

            this._restrictedSkills.put(classId.getId(), skillIds);
         }
      }

      if (Config.DEBUG) {
         this._log.info(this.getClass().getSimpleName() + ": Loaded " + this._restrictedSkills.size() + " classes with restricted skill(s).");
      }
   }

   private void collectAllClassesSkill() {
      for(Map<Integer, SkillLearn> skills : _classSkillTrees.values()) {
         if (skills != null) {
            for(SkillLearn skill : skills.values()) {
               if (skill != null) {
                  if (!_mutiProffSkills.isEmpty()) {
                     for(SkillLearn sk : _mutiProffSkills) {
                        if (sk.getId() == skill.getId() && sk.getLvl() == skill.getLvl()) {
                        }
                     }
                  }

                  _mutiProffSkills.add(skill);
               }
            }
         }
      }

      if (Config.DEBUG) {
         this._log.info(this.getClass().getSimpleName() + ": Loaded " + _mutiProffSkills.size() + " multiproff skills.");
      }
   }

   public boolean checkClassesSkill(Player player, int id, int lvl) {
      for(SkillLearn skill : _mutiProffSkills) {
         if (skill != null && skill.getId() == id && skill.getLvl() == lvl) {
            if (lvl == 1) {
               return true;
            }

            Skill oldSkill = player.getKnownSkill(id);
            if (oldSkill != null && oldSkill.getLevel() == skill.getLvl() - 1) {
               return true;
            }

            return false;
         }
      }

      return false;
   }

   public boolean checkValidClassSkills(int id, ClassId classId) {
      for(SkillLearn skill : getInstance().getAllClassSkillTree(classId).values()) {
         if (skill != null && skill.getId() == id) {
            return true;
         }
      }

      return false;
   }

   public ArrayList<Integer> getRestrictedSkills(ClassId classId) {
      return this._restrictedSkills.get(classId.getId());
   }

   public static SkillTreesParser getInstance() {
      return SkillTreesParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final SkillTreesParser _instance = new SkillTreesParser();
   }
}
