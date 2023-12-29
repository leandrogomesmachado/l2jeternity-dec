package l2e.gameserver.model.skills.engines.skills;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import l2e.gameserver.data.parser.EnchantSkillGroupsParser;
import l2e.gameserver.model.skills.DocumentBase;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.SkillType;
import l2e.gameserver.model.skills.conditions.Condition;
import l2e.gameserver.model.stats.StatsSet;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class DocumentSkill extends DocumentBase {
   private DocumentSkill.SkillTemplate _currentSkill;
   private final List<Skill> _skillsInFile = new ArrayList<>();

   public DocumentSkill(File file) {
      super(file);
   }

   private void setCurrentSkill(DocumentSkill.SkillTemplate skill) {
      this._currentSkill = skill;
   }

   @Override
   protected StatsSet getStatsSet() {
      return this._currentSkill.sets[this._currentSkill.currentLevel];
   }

   public List<Skill> getSkills() {
      return this._skillsInFile;
   }

   @Override
   protected String getTableValue(String name) {
      try {
         return this._tables.get(name)[this._currentSkill.currentLevel];
      } catch (RuntimeException var3) {
         this._log.log(Level.SEVERE, "Error in table: " + name + " of Skill Id " + this._currentSkill.id, (Throwable)var3);
         return "";
      }
   }

   @Override
   protected String getTableValue(String name, int idx) {
      try {
         return this._tables.get(name)[idx - 1];
      } catch (RuntimeException var4) {
         this._log.log(Level.SEVERE, "wrong level count in skill Id " + this._currentSkill.id + " name: " + name + " index : " + idx, (Throwable)var4);
         return "";
      }
   }

   @Override
   protected void parseDocument(Document doc) {
      for(Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equalsIgnoreCase(n.getNodeName())) {
            for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
               if ("skill".equalsIgnoreCase(d.getNodeName())) {
                  this.setCurrentSkill(new DocumentSkill.SkillTemplate());
                  this.parseSkill(d);
                  this._skillsInFile.addAll(this._currentSkill.skills);
                  this.resetTable();
               }
            }
         } else if ("skill".equalsIgnoreCase(n.getNodeName())) {
            this.setCurrentSkill(new DocumentSkill.SkillTemplate());
            this.parseSkill(n);
            this._skillsInFile.addAll(this._currentSkill.skills);
         }
      }
   }

   protected void parseSkill(Node n) {
      NamedNodeMap attrs = n.getAttributes();
      int enchantLevels1 = 0;
      int enchantLevels2 = 0;
      int enchantLevels3 = 0;
      int enchantLevels4 = 0;
      int enchantLevels5 = 0;
      int enchantLevels6 = 0;
      int enchantLevels7 = 0;
      int enchantLevels8 = 0;
      int skillId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
      String skillNameEn = attrs.getNamedItem("nameEn").getNodeValue();
      String skillNameRu = attrs.getNamedItem("nameRu").getNodeValue();
      String levels = attrs.getNamedItem("levels").getNodeValue();
      int lastLvl = Integer.parseInt(levels);
      if (attrs.getNamedItem("enchantGroup1") != null) {
         enchantLevels1 = EnchantSkillGroupsParser.getInstance()
            .addNewRouteForSkill(skillId, lastLvl, 1, Integer.parseInt(attrs.getNamedItem("enchantGroup1").getNodeValue()));
      }

      if (attrs.getNamedItem("enchantGroup2") != null) {
         enchantLevels2 = EnchantSkillGroupsParser.getInstance()
            .addNewRouteForSkill(skillId, lastLvl, 2, Integer.parseInt(attrs.getNamedItem("enchantGroup2").getNodeValue()));
      }

      if (attrs.getNamedItem("enchantGroup3") != null) {
         enchantLevels3 = EnchantSkillGroupsParser.getInstance()
            .addNewRouteForSkill(skillId, lastLvl, 3, Integer.parseInt(attrs.getNamedItem("enchantGroup3").getNodeValue()));
      }

      if (attrs.getNamedItem("enchantGroup4") != null) {
         enchantLevels4 = EnchantSkillGroupsParser.getInstance()
            .addNewRouteForSkill(skillId, lastLvl, 4, Integer.parseInt(attrs.getNamedItem("enchantGroup4").getNodeValue()));
      }

      if (attrs.getNamedItem("enchantGroup5") != null) {
         enchantLevels5 = EnchantSkillGroupsParser.getInstance()
            .addNewRouteForSkill(skillId, lastLvl, 5, Integer.parseInt(attrs.getNamedItem("enchantGroup5").getNodeValue()));
      }

      if (attrs.getNamedItem("enchantGroup6") != null) {
         enchantLevels6 = EnchantSkillGroupsParser.getInstance()
            .addNewRouteForSkill(skillId, lastLvl, 6, Integer.parseInt(attrs.getNamedItem("enchantGroup6").getNodeValue()));
      }

      if (attrs.getNamedItem("enchantGroup7") != null) {
         enchantLevels7 = EnchantSkillGroupsParser.getInstance()
            .addNewRouteForSkill(skillId, lastLvl, 7, Integer.parseInt(attrs.getNamedItem("enchantGroup7").getNodeValue()));
      }

      if (attrs.getNamedItem("enchantGroup8") != null) {
         enchantLevels8 = EnchantSkillGroupsParser.getInstance()
            .addNewRouteForSkill(skillId, lastLvl, 8, Integer.parseInt(attrs.getNamedItem("enchantGroup8").getNodeValue()));
      }

      this._currentSkill.id = skillId;
      this._currentSkill.nameEn = skillNameEn;
      this._currentSkill.nameRu = skillNameRu;
      this._currentSkill.sets = new StatsSet[lastLvl];
      this._currentSkill.enchsets1 = new StatsSet[enchantLevels1];
      this._currentSkill.enchsets2 = new StatsSet[enchantLevels2];
      this._currentSkill.enchsets3 = new StatsSet[enchantLevels3];
      this._currentSkill.enchsets4 = new StatsSet[enchantLevels4];
      this._currentSkill.enchsets5 = new StatsSet[enchantLevels5];
      this._currentSkill.enchsets6 = new StatsSet[enchantLevels6];
      this._currentSkill.enchsets7 = new StatsSet[enchantLevels7];
      this._currentSkill.enchsets8 = new StatsSet[enchantLevels8];

      for(int i = 0; i < lastLvl; ++i) {
         this._currentSkill.sets[i] = new StatsSet();
         this._currentSkill.sets[i].set("skill_id", this._currentSkill.id);
         this._currentSkill.sets[i].set("level", i + 1);
         this._currentSkill.sets[i].set("nameEn", this._currentSkill.nameEn);
         this._currentSkill.sets[i].set("nameRu", this._currentSkill.nameRu);
      }

      if (this._currentSkill.sets.length != lastLvl) {
         throw new RuntimeException("Skill id=" + skillId + " number of levels missmatch, " + lastLvl + " levels expected");
      } else {
         Node first = n.getFirstChild();

         for(Node var22 = first; var22 != null; var22 = var22.getNextSibling()) {
            if ("table".equalsIgnoreCase(var22.getNodeName())) {
               this.parseTable(var22);
            }
         }

         for(int i = 1; i <= lastLvl; ++i) {
            for(Node var23 = first; var23 != null; var23 = var23.getNextSibling()) {
               if ("set".equalsIgnoreCase(var23.getNodeName())) {
                  if ("capsuled_items_skill".equalsIgnoreCase(var23.getAttributes().getNamedItem("name").getNodeValue())) {
                     this.setExtractableSkillData(this._currentSkill.sets[i - 1], this.getTableValue("#extractableItems", i));
                  } else {
                     this.parseBeanSet(var23, this._currentSkill.sets[i - 1], Integer.valueOf(i));
                  }
               }
            }
         }

         for(int i = 0; i < enchantLevels1; ++i) {
            this._currentSkill.enchsets1[i] = new StatsSet();
            this._currentSkill.enchsets1[i].set("skill_id", this._currentSkill.id);
            this._currentSkill.enchsets1[i].set("level", i + 101);
            this._currentSkill.enchsets1[i].set("nameEn", this._currentSkill.nameEn);
            this._currentSkill.enchsets1[i].set("nameRu", this._currentSkill.nameRu);

            for(Node var24 = first; var24 != null; var24 = var24.getNextSibling()) {
               if ("set".equalsIgnoreCase(var24.getNodeName())) {
                  this.parseBeanSet(var24, this._currentSkill.enchsets1[i], Integer.valueOf(this._currentSkill.sets.length));
               }
            }

            for(Node var25 = first; var25 != null; var25 = var25.getNextSibling()) {
               if ("enchant1".equalsIgnoreCase(var25.getNodeName())) {
                  this.parseBeanSet(var25, this._currentSkill.enchsets1[i], Integer.valueOf(i + 1));
               }
            }
         }

         if (this._currentSkill.enchsets1.length != enchantLevels1) {
            throw new RuntimeException("Skill id=" + skillId + " number of levels missmatch, " + enchantLevels1 + " levels expected");
         } else {
            for(int i = 0; i < enchantLevels2; ++i) {
               this._currentSkill.enchsets2[i] = new StatsSet();
               this._currentSkill.enchsets2[i].set("skill_id", this._currentSkill.id);
               this._currentSkill.enchsets2[i].set("level", i + 201);
               this._currentSkill.enchsets2[i].set("nameEn", this._currentSkill.nameEn);
               this._currentSkill.enchsets2[i].set("nameRu", this._currentSkill.nameRu);

               for(Node var26 = first; var26 != null; var26 = var26.getNextSibling()) {
                  if ("set".equalsIgnoreCase(var26.getNodeName())) {
                     this.parseBeanSet(var26, this._currentSkill.enchsets2[i], Integer.valueOf(this._currentSkill.sets.length));
                  }
               }

               for(Node var27 = first; var27 != null; var27 = var27.getNextSibling()) {
                  if ("enchant2".equalsIgnoreCase(var27.getNodeName())) {
                     this.parseBeanSet(var27, this._currentSkill.enchsets2[i], Integer.valueOf(i + 1));
                  }
               }
            }

            if (this._currentSkill.enchsets2.length != enchantLevels2) {
               throw new RuntimeException("Skill id=" + skillId + " number of levels missmatch, " + enchantLevels2 + " levels expected");
            } else {
               for(int i = 0; i < enchantLevels3; ++i) {
                  this._currentSkill.enchsets3[i] = new StatsSet();
                  this._currentSkill.enchsets3[i].set("skill_id", this._currentSkill.id);
                  this._currentSkill.enchsets3[i].set("level", i + 301);
                  this._currentSkill.enchsets3[i].set("nameEn", this._currentSkill.nameEn);
                  this._currentSkill.enchsets3[i].set("nameRu", this._currentSkill.nameRu);

                  for(Node var28 = first; var28 != null; var28 = var28.getNextSibling()) {
                     if ("set".equalsIgnoreCase(var28.getNodeName())) {
                        this.parseBeanSet(var28, this._currentSkill.enchsets3[i], Integer.valueOf(this._currentSkill.sets.length));
                     }
                  }

                  for(Node var29 = first; var29 != null; var29 = var29.getNextSibling()) {
                     if ("enchant3".equalsIgnoreCase(var29.getNodeName())) {
                        this.parseBeanSet(var29, this._currentSkill.enchsets3[i], Integer.valueOf(i + 1));
                     }
                  }
               }

               if (this._currentSkill.enchsets3.length != enchantLevels3) {
                  throw new RuntimeException("Skill id=" + skillId + " number of levels missmatch, " + enchantLevels3 + " levels expected");
               } else {
                  for(int i = 0; i < enchantLevels4; ++i) {
                     this._currentSkill.enchsets4[i] = new StatsSet();
                     this._currentSkill.enchsets4[i].set("skill_id", this._currentSkill.id);
                     this._currentSkill.enchsets4[i].set("level", i + 401);
                     this._currentSkill.enchsets4[i].set("nameEn", this._currentSkill.nameEn);
                     this._currentSkill.enchsets4[i].set("nameRu", this._currentSkill.nameRu);

                     for(Node var30 = first; var30 != null; var30 = var30.getNextSibling()) {
                        if ("set".equalsIgnoreCase(var30.getNodeName())) {
                           this.parseBeanSet(var30, this._currentSkill.enchsets4[i], Integer.valueOf(this._currentSkill.sets.length));
                        }
                     }

                     for(Node var31 = first; var31 != null; var31 = var31.getNextSibling()) {
                        if ("enchant4".equalsIgnoreCase(var31.getNodeName())) {
                           this.parseBeanSet(var31, this._currentSkill.enchsets4[i], Integer.valueOf(i + 1));
                        }
                     }
                  }

                  if (this._currentSkill.enchsets4.length != enchantLevels4) {
                     throw new RuntimeException("Skill id=" + skillId + " number of levels missmatch, " + enchantLevels4 + " levels expected");
                  } else {
                     for(int i = 0; i < enchantLevels5; ++i) {
                        this._currentSkill.enchsets5[i] = new StatsSet();
                        this._currentSkill.enchsets5[i].set("skill_id", this._currentSkill.id);
                        this._currentSkill.enchsets5[i].set("level", i + 501);
                        this._currentSkill.enchsets5[i].set("nameEn", this._currentSkill.nameEn);
                        this._currentSkill.enchsets5[i].set("nameRu", this._currentSkill.nameRu);

                        for(Node var32 = first; var32 != null; var32 = var32.getNextSibling()) {
                           if ("set".equalsIgnoreCase(var32.getNodeName())) {
                              this.parseBeanSet(var32, this._currentSkill.enchsets5[i], Integer.valueOf(this._currentSkill.sets.length));
                           }
                        }

                        for(Node var33 = first; var33 != null; var33 = var33.getNextSibling()) {
                           if ("enchant5".equalsIgnoreCase(var33.getNodeName())) {
                              this.parseBeanSet(var33, this._currentSkill.enchsets5[i], Integer.valueOf(i + 1));
                           }
                        }
                     }

                     if (this._currentSkill.enchsets5.length != enchantLevels5) {
                        throw new RuntimeException("Skill id=" + skillId + " number of levels missmatch, " + enchantLevels5 + " levels expected");
                     } else {
                        for(int i = 0; i < enchantLevels6; ++i) {
                           this._currentSkill.enchsets6[i] = new StatsSet();
                           this._currentSkill.enchsets6[i].set("skill_id", this._currentSkill.id);
                           this._currentSkill.enchsets6[i].set("level", i + 601);
                           this._currentSkill.enchsets6[i].set("nameEn", this._currentSkill.nameEn);
                           this._currentSkill.enchsets6[i].set("nameRu", this._currentSkill.nameRu);

                           for(Node var34 = first; var34 != null; var34 = var34.getNextSibling()) {
                              if ("set".equalsIgnoreCase(var34.getNodeName())) {
                                 this.parseBeanSet(var34, this._currentSkill.enchsets6[i], Integer.valueOf(this._currentSkill.sets.length));
                              }
                           }

                           for(Node var35 = first; var35 != null; var35 = var35.getNextSibling()) {
                              if ("enchant6".equalsIgnoreCase(var35.getNodeName())) {
                                 this.parseBeanSet(var35, this._currentSkill.enchsets6[i], Integer.valueOf(i + 1));
                              }
                           }
                        }

                        if (this._currentSkill.enchsets6.length != enchantLevels6) {
                           throw new RuntimeException("Skill id=" + skillId + " number of levels missmatch, " + enchantLevels6 + " levels expected");
                        } else {
                           for(int i = 0; i < enchantLevels7; ++i) {
                              this._currentSkill.enchsets7[i] = new StatsSet();
                              this._currentSkill.enchsets7[i].set("skill_id", this._currentSkill.id);
                              this._currentSkill.enchsets7[i].set("level", i + 701);
                              this._currentSkill.enchsets7[i].set("nameEn", this._currentSkill.nameEn);
                              this._currentSkill.enchsets7[i].set("nameRu", this._currentSkill.nameRu);

                              for(Node var36 = first; var36 != null; var36 = var36.getNextSibling()) {
                                 if ("set".equalsIgnoreCase(var36.getNodeName())) {
                                    this.parseBeanSet(var36, this._currentSkill.enchsets7[i], Integer.valueOf(this._currentSkill.sets.length));
                                 }
                              }

                              for(Node var37 = first; var37 != null; var37 = var37.getNextSibling()) {
                                 if ("enchant7".equalsIgnoreCase(var37.getNodeName())) {
                                    this.parseBeanSet(var37, this._currentSkill.enchsets7[i], Integer.valueOf(i + 1));
                                 }
                              }
                           }

                           if (this._currentSkill.enchsets7.length != enchantLevels7) {
                              throw new RuntimeException("Skill id=" + skillId + " number of levels missmatch, " + enchantLevels7 + " levels expected");
                           } else {
                              for(int i = 0; i < enchantLevels8; ++i) {
                                 this._currentSkill.enchsets8[i] = new StatsSet();
                                 this._currentSkill.enchsets8[i].set("skill_id", this._currentSkill.id);
                                 this._currentSkill.enchsets8[i].set("level", i + 801);
                                 this._currentSkill.enchsets8[i].set("nameEn", this._currentSkill.nameEn);
                                 this._currentSkill.enchsets8[i].set("nameRu", this._currentSkill.nameRu);

                                 for(Node var38 = first; var38 != null; var38 = var38.getNextSibling()) {
                                    if ("set".equalsIgnoreCase(var38.getNodeName())) {
                                       this.parseBeanSet(var38, this._currentSkill.enchsets8[i], Integer.valueOf(this._currentSkill.sets.length));
                                    }
                                 }

                                 for(Node var39 = first; var39 != null; var39 = var39.getNextSibling()) {
                                    if ("enchant8".equalsIgnoreCase(var39.getNodeName())) {
                                       this.parseBeanSet(var39, this._currentSkill.enchsets8[i], Integer.valueOf(i + 1));
                                    }
                                 }
                              }

                              if (this._currentSkill.enchsets8.length != enchantLevels8) {
                                 throw new RuntimeException("Skill id=" + skillId + " number of levels missmatch, " + enchantLevels8 + " levels expected");
                              } else {
                                 this.makeSkills();

                                 for(int i = 0; i < lastLvl; ++i) {
                                    this._currentSkill.currentLevel = i;

                                    for(Node var40 = first; var40 != null; var40 = var40.getNextSibling()) {
                                       if ("cond".equalsIgnoreCase(var40.getNodeName())) {
                                          Condition condition = this.parseCondition(var40.getFirstChild(), this._currentSkill.currentSkills.get(i));
                                          Node msg = var40.getAttributes().getNamedItem("msg");
                                          Node msgId = var40.getAttributes().getNamedItem("msgId");
                                          if (condition != null && msg != null) {
                                             condition.setMessage(msg.getNodeValue());
                                          } else if (condition != null && msgId != null) {
                                             condition.setMessageId(Integer.decode(this.getValue(msgId.getNodeValue(), null)));
                                             Node addName = var40.getAttributes().getNamedItem("addName");
                                             if (addName != null && Integer.decode(this.getValue(msgId.getNodeValue(), null)) > 0) {
                                                condition.addName();
                                             }
                                          }

                                          this._currentSkill.currentSkills.get(i).attach(condition, false);
                                       } else if ("for".equalsIgnoreCase(var40.getNodeName())) {
                                          this.parseTemplate(var40, this._currentSkill.currentSkills.get(i));
                                       }
                                    }
                                 }

                                 for(int i = lastLvl; i < lastLvl + enchantLevels1; ++i) {
                                    this._currentSkill.currentLevel = i - lastLvl;
                                    boolean foundCond = false;
                                    boolean foundFor = false;

                                    for(Node var41 = first; var41 != null; var41 = var41.getNextSibling()) {
                                       if ("enchant1cond".equalsIgnoreCase(var41.getNodeName())) {
                                          foundCond = true;
                                          Condition condition = this.parseCondition(var41.getFirstChild(), this._currentSkill.currentSkills.get(i));
                                          Node msg = var41.getAttributes().getNamedItem("msg");
                                          if (condition != null && msg != null) {
                                             condition.setMessage(msg.getNodeValue());
                                          }

                                          this._currentSkill.currentSkills.get(i).attach(condition, false);
                                       } else if ("enchant1for".equalsIgnoreCase(var41.getNodeName())) {
                                          foundFor = true;
                                          this.parseTemplate(var41, this._currentSkill.currentSkills.get(i));
                                       }
                                    }

                                    if (!foundCond || !foundFor) {
                                       this._currentSkill.currentLevel = lastLvl - 1;

                                       for(Node var42 = first; var42 != null; var42 = var42.getNextSibling()) {
                                          if (!foundCond && "cond".equalsIgnoreCase(var42.getNodeName())) {
                                             Condition condition = this.parseCondition(var42.getFirstChild(), this._currentSkill.currentSkills.get(i));
                                             Node msg = var42.getAttributes().getNamedItem("msg");
                                             if (condition != null && msg != null) {
                                                condition.setMessage(msg.getNodeValue());
                                             }

                                             this._currentSkill.currentSkills.get(i).attach(condition, false);
                                          } else if (!foundFor && "for".equalsIgnoreCase(var42.getNodeName())) {
                                             this.parseTemplate(var42, this._currentSkill.currentSkills.get(i));
                                          }
                                       }
                                    }
                                 }

                                 for(int i = lastLvl + enchantLevels1; i < lastLvl + enchantLevels1 + enchantLevels2; ++i) {
                                    boolean foundCond = false;
                                    boolean foundFor = false;
                                    this._currentSkill.currentLevel = i - lastLvl - enchantLevels1;

                                    for(Node var43 = first; var43 != null; var43 = var43.getNextSibling()) {
                                       if ("enchant2cond".equalsIgnoreCase(var43.getNodeName())) {
                                          foundCond = true;
                                          Condition condition = this.parseCondition(var43.getFirstChild(), this._currentSkill.currentSkills.get(i));
                                          Node msg = var43.getAttributes().getNamedItem("msg");
                                          if (condition != null && msg != null) {
                                             condition.setMessage(msg.getNodeValue());
                                          }

                                          this._currentSkill.currentSkills.get(i).attach(condition, false);
                                       } else if ("enchant2for".equalsIgnoreCase(var43.getNodeName())) {
                                          foundFor = true;
                                          this.parseTemplate(var43, this._currentSkill.currentSkills.get(i));
                                       }
                                    }

                                    if (!foundCond || !foundFor) {
                                       this._currentSkill.currentLevel = lastLvl - 1;

                                       for(Node var44 = first; var44 != null; var44 = var44.getNextSibling()) {
                                          if (!foundCond && "cond".equalsIgnoreCase(var44.getNodeName())) {
                                             Condition condition = this.parseCondition(var44.getFirstChild(), this._currentSkill.currentSkills.get(i));
                                             Node msg = var44.getAttributes().getNamedItem("msg");
                                             if (condition != null && msg != null) {
                                                condition.setMessage(msg.getNodeValue());
                                             }

                                             this._currentSkill.currentSkills.get(i).attach(condition, false);
                                          } else if (!foundFor && "for".equalsIgnoreCase(var44.getNodeName())) {
                                             this.parseTemplate(var44, this._currentSkill.currentSkills.get(i));
                                          }
                                       }
                                    }
                                 }

                                 for(int i = lastLvl + enchantLevels1 + enchantLevels2; i < lastLvl + enchantLevels1 + enchantLevels2 + enchantLevels3; ++i) {
                                    boolean foundCond = false;
                                    boolean foundFor = false;
                                    this._currentSkill.currentLevel = i - lastLvl - enchantLevels1 - enchantLevels2;

                                    for(Node var45 = first; var45 != null; var45 = var45.getNextSibling()) {
                                       if ("enchant3cond".equalsIgnoreCase(var45.getNodeName())) {
                                          foundCond = true;
                                          Condition condition = this.parseCondition(var45.getFirstChild(), this._currentSkill.currentSkills.get(i));
                                          Node msg = var45.getAttributes().getNamedItem("msg");
                                          if (condition != null && msg != null) {
                                             condition.setMessage(msg.getNodeValue());
                                          }

                                          this._currentSkill.currentSkills.get(i).attach(condition, false);
                                       } else if ("enchant3for".equalsIgnoreCase(var45.getNodeName())) {
                                          foundFor = true;
                                          this.parseTemplate(var45, this._currentSkill.currentSkills.get(i));
                                       }
                                    }

                                    if (!foundCond || !foundFor) {
                                       this._currentSkill.currentLevel = lastLvl - 1;

                                       for(Node var46 = first; var46 != null; var46 = var46.getNextSibling()) {
                                          if (!foundCond && "cond".equalsIgnoreCase(var46.getNodeName())) {
                                             Condition condition = this.parseCondition(var46.getFirstChild(), this._currentSkill.currentSkills.get(i));
                                             Node msg = var46.getAttributes().getNamedItem("msg");
                                             if (condition != null && msg != null) {
                                                condition.setMessage(msg.getNodeValue());
                                             }

                                             this._currentSkill.currentSkills.get(i).attach(condition, false);
                                          } else if (!foundFor && "for".equalsIgnoreCase(var46.getNodeName())) {
                                             this.parseTemplate(var46, this._currentSkill.currentSkills.get(i));
                                          }
                                       }
                                    }
                                 }

                                 for(int i = lastLvl + enchantLevels1 + enchantLevels2 + enchantLevels3;
                                    i < lastLvl + enchantLevels1 + enchantLevels2 + enchantLevels3 + enchantLevels4;
                                    ++i
                                 ) {
                                    boolean foundCond = false;
                                    boolean foundFor = false;
                                    this._currentSkill.currentLevel = i - lastLvl - enchantLevels1 - enchantLevels2 - enchantLevels3;

                                    for(Node var47 = first; var47 != null; var47 = var47.getNextSibling()) {
                                       if ("enchant4cond".equalsIgnoreCase(var47.getNodeName())) {
                                          foundCond = true;
                                          Condition condition = this.parseCondition(var47.getFirstChild(), this._currentSkill.currentSkills.get(i));
                                          Node msg = var47.getAttributes().getNamedItem("msg");
                                          if (condition != null && msg != null) {
                                             condition.setMessage(msg.getNodeValue());
                                          }

                                          this._currentSkill.currentSkills.get(i).attach(condition, false);
                                       } else if ("enchant4for".equalsIgnoreCase(var47.getNodeName())) {
                                          foundFor = true;
                                          this.parseTemplate(var47, this._currentSkill.currentSkills.get(i));
                                       }
                                    }

                                    if (!foundCond || !foundFor) {
                                       this._currentSkill.currentLevel = lastLvl - 1;

                                       for(Node var48 = first; var48 != null; var48 = var48.getNextSibling()) {
                                          if (!foundCond && "cond".equalsIgnoreCase(var48.getNodeName())) {
                                             Condition condition = this.parseCondition(var48.getFirstChild(), this._currentSkill.currentSkills.get(i));
                                             Node msg = var48.getAttributes().getNamedItem("msg");
                                             if (condition != null && msg != null) {
                                                condition.setMessage(msg.getNodeValue());
                                             }

                                             this._currentSkill.currentSkills.get(i).attach(condition, false);
                                          } else if (!foundFor && "for".equalsIgnoreCase(var48.getNodeName())) {
                                             this.parseTemplate(var48, this._currentSkill.currentSkills.get(i));
                                          }
                                       }
                                    }
                                 }

                                 for(int i = lastLvl + enchantLevels1 + enchantLevels2 + enchantLevels3 + enchantLevels4;
                                    i < lastLvl + enchantLevels1 + enchantLevels2 + enchantLevels3 + enchantLevels4 + enchantLevels5;
                                    ++i
                                 ) {
                                    boolean foundCond = false;
                                    boolean foundFor = false;
                                    this._currentSkill.currentLevel = i - lastLvl - enchantLevels1 - enchantLevels2 - enchantLevels3 - enchantLevels4;

                                    for(Node var49 = first; var49 != null; var49 = var49.getNextSibling()) {
                                       if ("enchant5cond".equalsIgnoreCase(var49.getNodeName())) {
                                          foundCond = true;
                                          Condition condition = this.parseCondition(var49.getFirstChild(), this._currentSkill.currentSkills.get(i));
                                          Node msg = var49.getAttributes().getNamedItem("msg");
                                          if (condition != null && msg != null) {
                                             condition.setMessage(msg.getNodeValue());
                                          }

                                          this._currentSkill.currentSkills.get(i).attach(condition, false);
                                       } else if ("enchant5for".equalsIgnoreCase(var49.getNodeName())) {
                                          foundFor = true;
                                          this.parseTemplate(var49, this._currentSkill.currentSkills.get(i));
                                       }
                                    }

                                    if (!foundCond || !foundFor) {
                                       this._currentSkill.currentLevel = lastLvl - 1;

                                       for(Node var50 = first; var50 != null; var50 = var50.getNextSibling()) {
                                          if (!foundCond && "cond".equalsIgnoreCase(var50.getNodeName())) {
                                             Condition condition = this.parseCondition(var50.getFirstChild(), this._currentSkill.currentSkills.get(i));
                                             Node msg = var50.getAttributes().getNamedItem("msg");
                                             if (condition != null && msg != null) {
                                                condition.setMessage(msg.getNodeValue());
                                             }

                                             this._currentSkill.currentSkills.get(i).attach(condition, false);
                                          } else if (!foundFor && "for".equalsIgnoreCase(var50.getNodeName())) {
                                             this.parseTemplate(var50, this._currentSkill.currentSkills.get(i));
                                          }
                                       }
                                    }
                                 }

                                 for(int i = lastLvl + enchantLevels1 + enchantLevels2 + enchantLevels3 + enchantLevels4 + enchantLevels5;
                                    i < lastLvl + enchantLevels1 + enchantLevels2 + enchantLevels3 + enchantLevels4 + enchantLevels5 + enchantLevels6;
                                    ++i
                                 ) {
                                    boolean foundCond = false;
                                    boolean foundFor = false;
                                    this._currentSkill.currentLevel = i
                                       - lastLvl
                                       - enchantLevels1
                                       - enchantLevels2
                                       - enchantLevels3
                                       - enchantLevels4
                                       - enchantLevels5;

                                    for(Node var51 = first; var51 != null; var51 = var51.getNextSibling()) {
                                       if ("enchant6cond".equalsIgnoreCase(var51.getNodeName())) {
                                          foundCond = true;
                                          Condition condition = this.parseCondition(var51.getFirstChild(), this._currentSkill.currentSkills.get(i));
                                          Node msg = var51.getAttributes().getNamedItem("msg");
                                          if (condition != null && msg != null) {
                                             condition.setMessage(msg.getNodeValue());
                                          }

                                          this._currentSkill.currentSkills.get(i).attach(condition, false);
                                       } else if ("enchant6for".equalsIgnoreCase(var51.getNodeName())) {
                                          foundFor = true;
                                          this.parseTemplate(var51, this._currentSkill.currentSkills.get(i));
                                       }
                                    }

                                    if (!foundCond || !foundFor) {
                                       this._currentSkill.currentLevel = lastLvl - 1;

                                       for(Node var52 = first; var52 != null; var52 = var52.getNextSibling()) {
                                          if (!foundCond && "cond".equalsIgnoreCase(var52.getNodeName())) {
                                             Condition condition = this.parseCondition(var52.getFirstChild(), this._currentSkill.currentSkills.get(i));
                                             Node msg = var52.getAttributes().getNamedItem("msg");
                                             if (condition != null && msg != null) {
                                                condition.setMessage(msg.getNodeValue());
                                             }

                                             this._currentSkill.currentSkills.get(i).attach(condition, false);
                                          } else if (!foundFor && "for".equalsIgnoreCase(var52.getNodeName())) {
                                             this.parseTemplate(var52, this._currentSkill.currentSkills.get(i));
                                          }
                                       }
                                    }
                                 }

                                 for(int i = lastLvl + enchantLevels1 + enchantLevels2 + enchantLevels3 + enchantLevels4 + enchantLevels5 + enchantLevels6;
                                    i
                                       < lastLvl
                                          + enchantLevels1
                                          + enchantLevels2
                                          + enchantLevels3
                                          + enchantLevels4
                                          + enchantLevels5
                                          + enchantLevels6
                                          + enchantLevels7;
                                    ++i
                                 ) {
                                    boolean foundCond = false;
                                    boolean foundFor = false;
                                    this._currentSkill.currentLevel = i
                                       - lastLvl
                                       - enchantLevels1
                                       - enchantLevels2
                                       - enchantLevels3
                                       - enchantLevels4
                                       - enchantLevels5
                                       - enchantLevels6;

                                    for(Node var53 = first; var53 != null; var53 = var53.getNextSibling()) {
                                       if ("enchant7cond".equalsIgnoreCase(var53.getNodeName())) {
                                          foundCond = true;
                                          Condition condition = this.parseCondition(var53.getFirstChild(), this._currentSkill.currentSkills.get(i));
                                          Node msg = var53.getAttributes().getNamedItem("msg");
                                          if (condition != null && msg != null) {
                                             condition.setMessage(msg.getNodeValue());
                                          }

                                          this._currentSkill.currentSkills.get(i).attach(condition, false);
                                       } else if ("enchant7for".equalsIgnoreCase(var53.getNodeName())) {
                                          foundFor = true;
                                          this.parseTemplate(var53, this._currentSkill.currentSkills.get(i));
                                       }
                                    }

                                    if (!foundCond || !foundFor) {
                                       this._currentSkill.currentLevel = lastLvl - 1;

                                       for(Node var54 = first; var54 != null; var54 = var54.getNextSibling()) {
                                          if (!foundCond && "cond".equalsIgnoreCase(var54.getNodeName())) {
                                             Condition condition = this.parseCondition(var54.getFirstChild(), this._currentSkill.currentSkills.get(i));
                                             Node msg = var54.getAttributes().getNamedItem("msg");
                                             if (condition != null && msg != null) {
                                                condition.setMessage(msg.getNodeValue());
                                             }

                                             this._currentSkill.currentSkills.get(i).attach(condition, false);
                                          } else if (!foundFor && "for".equalsIgnoreCase(var54.getNodeName())) {
                                             this.parseTemplate(var54, this._currentSkill.currentSkills.get(i));
                                          }
                                       }
                                    }
                                 }

                                 for(int i = lastLvl
                                       + enchantLevels1
                                       + enchantLevels2
                                       + enchantLevels3
                                       + enchantLevels4
                                       + enchantLevels5
                                       + enchantLevels6
                                       + enchantLevels7;
                                    i
                                       < lastLvl
                                          + enchantLevels1
                                          + enchantLevels2
                                          + enchantLevels3
                                          + enchantLevels4
                                          + enchantLevels5
                                          + enchantLevels6
                                          + enchantLevels7
                                          + enchantLevels8;
                                    ++i
                                 ) {
                                    boolean foundCond = false;
                                    boolean foundFor = false;
                                    this._currentSkill.currentLevel = i
                                       - lastLvl
                                       - enchantLevels1
                                       - enchantLevels2
                                       - enchantLevels3
                                       - enchantLevels4
                                       - enchantLevels5
                                       - enchantLevels6
                                       - enchantLevels7;

                                    for(Node var55 = first; var55 != null; var55 = var55.getNextSibling()) {
                                       if ("enchant8cond".equalsIgnoreCase(var55.getNodeName())) {
                                          foundCond = true;
                                          Condition condition = this.parseCondition(var55.getFirstChild(), this._currentSkill.currentSkills.get(i));
                                          Node msg = var55.getAttributes().getNamedItem("msg");
                                          if (condition != null && msg != null) {
                                             condition.setMessage(msg.getNodeValue());
                                          }

                                          this._currentSkill.currentSkills.get(i).attach(condition, false);
                                       } else if ("enchant8for".equalsIgnoreCase(var55.getNodeName())) {
                                          foundFor = true;
                                          this.parseTemplate(var55, this._currentSkill.currentSkills.get(i));
                                       }
                                    }

                                    if (!foundCond || !foundFor) {
                                       this._currentSkill.currentLevel = lastLvl - 1;

                                       for(Node var56 = first; var56 != null; var56 = var56.getNextSibling()) {
                                          if (!foundCond && "cond".equalsIgnoreCase(var56.getNodeName())) {
                                             Condition condition = this.parseCondition(var56.getFirstChild(), this._currentSkill.currentSkills.get(i));
                                             Node msg = var56.getAttributes().getNamedItem("msg");
                                             if (condition != null && msg != null) {
                                                condition.setMessage(msg.getNodeValue());
                                             }

                                             this._currentSkill.currentSkills.get(i).attach(condition, false);
                                          } else if (!foundFor && "for".equalsIgnoreCase(var56.getNodeName())) {
                                             this.parseTemplate(var56, this._currentSkill.currentSkills.get(i));
                                          }
                                       }
                                    }
                                 }

                                 this._currentSkill.skills.addAll(this._currentSkill.currentSkills);
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

   private void makeSkills() {
      int count = 0;
      this._currentSkill.currentSkills = new ArrayList<>(
         this._currentSkill.sets.length
            + this._currentSkill.enchsets1.length
            + this._currentSkill.enchsets2.length
            + this._currentSkill.enchsets3.length
            + this._currentSkill.enchsets4.length
            + this._currentSkill.enchsets5.length
            + this._currentSkill.enchsets6.length
            + this._currentSkill.enchsets7.length
            + this._currentSkill.enchsets8.length
      );

      for(int i = 0; i < this._currentSkill.sets.length; ++i) {
         try {
            this._currentSkill
               .currentSkills
               .add(i, this._currentSkill.sets[i].getEnum("skillType", SkillType.class, SkillType.DUMMY).makeSkill(this._currentSkill.sets[i]));
            ++count;
         } catch (Exception var13) {
            this._log
               .log(
                  Level.SEVERE,
                  "Skill id="
                     + this._currentSkill.sets[i].getEnum("skillType", SkillType.class, SkillType.DUMMY).makeSkill(this._currentSkill.sets[i]).getDisplayId()
                     + "level"
                     + this._currentSkill.sets[i].getEnum("skillType", SkillType.class, SkillType.DUMMY).makeSkill(this._currentSkill.sets[i]).getLevel(),
                  (Throwable)var13
               );
         }
      }

      int _count = count;

      for(int i = 0; i < this._currentSkill.enchsets1.length; ++i) {
         try {
            this._currentSkill
               .currentSkills
               .add(
                  _count + i,
                  this._currentSkill.enchsets1[i].getEnum("skillType", SkillType.class, SkillType.DUMMY).makeSkill(this._currentSkill.enchsets1[i])
               );
            ++count;
         } catch (Exception var12) {
            this._log
               .log(
                  Level.SEVERE,
                  "Skill id="
                     + this._currentSkill.enchsets1[i]
                        .getEnum("skillType", SkillType.class, SkillType.DUMMY)
                        .makeSkill(this._currentSkill.enchsets1[i])
                        .getDisplayId()
                     + " level="
                     + this._currentSkill.enchsets1[i]
                        .getEnum("skillType", SkillType.class, SkillType.DUMMY)
                        .makeSkill(this._currentSkill.enchsets1[i])
                        .getLevel(),
                  (Throwable)var12
               );
         }
      }

      _count = count;

      for(int i = 0; i < this._currentSkill.enchsets2.length; ++i) {
         try {
            this._currentSkill
               .currentSkills
               .add(
                  _count + i,
                  this._currentSkill.enchsets2[i].getEnum("skillType", SkillType.class, SkillType.DUMMY).makeSkill(this._currentSkill.enchsets2[i])
               );
            ++count;
         } catch (Exception var11) {
            this._log
               .log(
                  Level.SEVERE,
                  "Skill id="
                     + this._currentSkill.enchsets2[i]
                        .getEnum("skillType", SkillType.class, SkillType.DUMMY)
                        .makeSkill(this._currentSkill.enchsets2[i])
                        .getDisplayId()
                     + " level="
                     + this._currentSkill.enchsets2[i]
                        .getEnum("skillType", SkillType.class, SkillType.DUMMY)
                        .makeSkill(this._currentSkill.enchsets2[i])
                        .getLevel(),
                  (Throwable)var11
               );
         }
      }

      _count = count;

      for(int i = 0; i < this._currentSkill.enchsets3.length; ++i) {
         try {
            this._currentSkill
               .currentSkills
               .add(
                  _count + i,
                  this._currentSkill.enchsets3[i].getEnum("skillType", SkillType.class, SkillType.DUMMY).makeSkill(this._currentSkill.enchsets3[i])
               );
            ++count;
         } catch (Exception var10) {
            this._log
               .log(
                  Level.SEVERE,
                  "Skill id="
                     + this._currentSkill.enchsets3[i]
                        .getEnum("skillType", SkillType.class, SkillType.DUMMY)
                        .makeSkill(this._currentSkill.enchsets3[i])
                        .getDisplayId()
                     + " level="
                     + this._currentSkill.enchsets3[i]
                        .getEnum("skillType", SkillType.class, SkillType.DUMMY)
                        .makeSkill(this._currentSkill.enchsets3[i])
                        .getLevel(),
                  (Throwable)var10
               );
         }
      }

      _count = count;

      for(int i = 0; i < this._currentSkill.enchsets4.length; ++i) {
         try {
            this._currentSkill
               .currentSkills
               .add(
                  _count + i,
                  this._currentSkill.enchsets4[i].getEnum("skillType", SkillType.class, SkillType.DUMMY).makeSkill(this._currentSkill.enchsets4[i])
               );
            ++count;
         } catch (Exception var9) {
            this._log
               .log(
                  Level.SEVERE,
                  "Skill id="
                     + this._currentSkill.enchsets4[i]
                        .getEnum("skillType", SkillType.class, SkillType.DUMMY)
                        .makeSkill(this._currentSkill.enchsets4[i])
                        .getDisplayId()
                     + " level="
                     + this._currentSkill.enchsets4[i]
                        .getEnum("skillType", SkillType.class, SkillType.DUMMY)
                        .makeSkill(this._currentSkill.enchsets4[i])
                        .getLevel(),
                  (Throwable)var9
               );
         }
      }

      _count = count;

      for(int i = 0; i < this._currentSkill.enchsets5.length; ++i) {
         try {
            this._currentSkill
               .currentSkills
               .add(
                  _count + i,
                  this._currentSkill.enchsets5[i].getEnum("skillType", SkillType.class, SkillType.DUMMY).makeSkill(this._currentSkill.enchsets5[i])
               );
            ++count;
         } catch (Exception var8) {
            this._log
               .log(
                  Level.SEVERE,
                  "Skill id="
                     + this._currentSkill.enchsets5[i]
                        .getEnum("skillType", SkillType.class, SkillType.DUMMY)
                        .makeSkill(this._currentSkill.enchsets5[i])
                        .getDisplayId()
                     + " level="
                     + this._currentSkill.enchsets5[i]
                        .getEnum("skillType", SkillType.class, SkillType.DUMMY)
                        .makeSkill(this._currentSkill.enchsets5[i])
                        .getLevel(),
                  (Throwable)var8
               );
         }
      }

      _count = count;

      for(int i = 0; i < this._currentSkill.enchsets6.length; ++i) {
         try {
            this._currentSkill
               .currentSkills
               .add(
                  _count + i,
                  this._currentSkill.enchsets6[i].getEnum("skillType", SkillType.class, SkillType.DUMMY).makeSkill(this._currentSkill.enchsets6[i])
               );
            ++count;
         } catch (Exception var7) {
            this._log
               .log(
                  Level.SEVERE,
                  "Skill id="
                     + this._currentSkill.enchsets6[i]
                        .getEnum("skillType", SkillType.class, SkillType.DUMMY)
                        .makeSkill(this._currentSkill.enchsets6[i])
                        .getDisplayId()
                     + " level="
                     + this._currentSkill.enchsets6[i]
                        .getEnum("skillType", SkillType.class, SkillType.DUMMY)
                        .makeSkill(this._currentSkill.enchsets6[i])
                        .getLevel(),
                  (Throwable)var7
               );
         }
      }

      _count = count;

      for(int i = 0; i < this._currentSkill.enchsets7.length; ++i) {
         try {
            this._currentSkill
               .currentSkills
               .add(
                  _count + i,
                  this._currentSkill.enchsets7[i].getEnum("skillType", SkillType.class, SkillType.DUMMY).makeSkill(this._currentSkill.enchsets7[i])
               );
            ++count;
         } catch (Exception var6) {
            this._log
               .log(
                  Level.SEVERE,
                  "Skill id="
                     + this._currentSkill.enchsets7[i]
                        .getEnum("skillType", SkillType.class, SkillType.DUMMY)
                        .makeSkill(this._currentSkill.enchsets7[i])
                        .getDisplayId()
                     + " level="
                     + this._currentSkill.enchsets7[i]
                        .getEnum("skillType", SkillType.class, SkillType.DUMMY)
                        .makeSkill(this._currentSkill.enchsets7[i])
                        .getLevel(),
                  (Throwable)var6
               );
         }
      }

      _count = count;

      for(int i = 0; i < this._currentSkill.enchsets8.length; ++i) {
         try {
            this._currentSkill
               .currentSkills
               .add(
                  _count + i,
                  this._currentSkill.enchsets8[i].getEnum("skillType", SkillType.class, SkillType.DUMMY).makeSkill(this._currentSkill.enchsets8[i])
               );
            ++count;
         } catch (Exception var5) {
            this._log
               .log(
                  Level.SEVERE,
                  "Skill id="
                     + this._currentSkill.enchsets8[i]
                        .getEnum("skillType", SkillType.class, SkillType.DUMMY)
                        .makeSkill(this._currentSkill.enchsets8[i])
                        .getDisplayId()
                     + " level="
                     + this._currentSkill.enchsets8[i]
                        .getEnum("skillType", SkillType.class, SkillType.DUMMY)
                        .makeSkill(this._currentSkill.enchsets8[i])
                        .getLevel(),
                  (Throwable)var5
               );
         }
      }
   }

   public static class SkillTemplate {
      public int id;
      public String nameEn;
      public String nameRu;
      public StatsSet[] sets;
      public StatsSet[] enchsets1;
      public StatsSet[] enchsets2;
      public StatsSet[] enchsets3;
      public StatsSet[] enchsets4;
      public StatsSet[] enchsets5;
      public StatsSet[] enchsets6;
      public StatsSet[] enchsets7;
      public StatsSet[] enchsets8;
      public int currentLevel;
      public List<Skill> skills = new ArrayList<>();
      public List<Skill> currentSkills = new ArrayList<>();
   }
}
