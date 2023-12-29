package l2e.gameserver.data.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.model.Augmentation;
import l2e.gameserver.model.actor.templates.items.Weapon;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.skills.options.Options;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class AugmentationParser {
   private static final Logger _log = Logger.getLogger(AugmentationParser.class.getName());
   private static final int STAT_BLOCKSIZE = 3640;
   private static final int STAT_SUBBLOCKSIZE = 91;
   public static final int MIN_SKILL_ID = 14560;
   private static final int BLUE_START = 14561;
   private static final int SKILLS_BLOCKSIZE = 178;
   private static final int BASESTAT_STR = 16341;
   private static final int BASESTAT_MEN = 16344;
   private static final int ACC_START = 16669;
   private static final int ACC_BLOCKS_NUM = 10;
   private static final int ACC_STAT_SUBBLOCKSIZE = 21;
   private static final int ACC_RING_START = 16669;
   private static final int ACC_RING_SKILLS = 18;
   private static final int ACC_RING_BLOCKSIZE = 102;
   private static final int ACC_RING_END = 17688;
   private static final int ACC_EAR_START = 17689;
   private static final int ACC_EAR_SKILLS = 18;
   private static final int ACC_EAR_BLOCKSIZE = 102;
   private static final int ACC_EAR_END = 18708;
   private static final int ACC_NECK_START = 18709;
   private static final int ACC_NECK_SKILLS = 24;
   private static final int ACC_NECK_BLOCKSIZE = 108;
   private final List<List<Integer>> _blueSkills = new ArrayList<>(10);
   private final List<List<Integer>> _purpleSkills = new ArrayList<>(10);
   private final List<List<Integer>> _redSkills = new ArrayList<>(10);
   private final List<List<Integer>> _yellowSkills = new ArrayList<>(10);
   private final List<AugmentationParser.AugmentationChance> _augmentationChances = new ArrayList<>();
   private final List<AugmentationParser.augmentationChanceAcc> _augmentationChancesAcc = new ArrayList<>();
   private final Map<Integer, SkillHolder> _allSkills = new HashMap<>();

   protected AugmentationParser() {
      for(int i = 0; i < 10; ++i) {
         this._blueSkills.add(new ArrayList<>());
         this._purpleSkills.add(new ArrayList<>());
         this._redSkills.add(new ArrayList<>());
         this._yellowSkills.add(new ArrayList<>());
      }

      this.load();
      if (!Config.RETAIL_LIKE_AUGMENTATION) {
         for(int i = 0; i < 10; ++i) {
            _log.info(
               this.getClass().getSimpleName()
                  + ": Loaded: "
                  + this._blueSkills.get(i).size()
                  + " blue, "
                  + this._purpleSkills.get(i).size()
                  + " purple and "
                  + this._redSkills.get(i).size()
                  + " red skills for lifeStoneLevel "
                  + i
            );
         }
      } else {
         _log.log(Level.INFO, this.getClass().getSimpleName() + ": Loaded: " + this._augmentationChances.size() + " augmentations.");
         _log.log(Level.INFO, this.getClass().getSimpleName() + ": Loaded: " + this._augmentationChancesAcc.size() + " accessory augmentations.");
      }
   }

   private final void load() {
      DocumentBuilderFactory factory2 = DocumentBuilderFactory.newInstance();
      factory2.setValidating(false);
      factory2.setIgnoringComments(true);
      if (!Config.RETAIL_LIKE_AUGMENTATION) {
         try {
            int badAugmantData = 0;
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setIgnoringComments(true);
            File file = new File(Config.DATAPACK_ROOT + "/data/stats/skills/augmentation/augmentation_skillmap.xml");
            if (!file.exists()) {
               _log.log(Level.WARNING, this.getClass().getSimpleName() + ": ERROR The augmentation skillmap file is missing.");
               return;
            }

            Document doc = factory.newDocumentBuilder().parse(file);

            for(Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
               if ("list".equalsIgnoreCase(n.getNodeName())) {
                  for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
                     if ("augmentation".equalsIgnoreCase(d.getNodeName())) {
                        NamedNodeMap attrs = d.getAttributes();
                        int skillId = 0;
                        int augmentationId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
                        int skillLvL = 0;
                        String type = "blue";

                        for(Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
                           if ("skillId".equalsIgnoreCase(cd.getNodeName())) {
                              attrs = cd.getAttributes();
                              skillId = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
                           } else if ("skillLevel".equalsIgnoreCase(cd.getNodeName())) {
                              attrs = cd.getAttributes();
                              skillLvL = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
                           } else if ("type".equalsIgnoreCase(cd.getNodeName())) {
                              attrs = cd.getAttributes();
                              type = attrs.getNamedItem("val").getNodeValue();
                           }
                        }

                        if (skillId == 0) {
                           ++badAugmantData;
                        } else if (skillLvL == 0) {
                           ++badAugmantData;
                        } else {
                           int k = (augmentationId - 14561) / 178;
                           if (type.equalsIgnoreCase("blue")) {
                              this._blueSkills.get(k).add(augmentationId);
                           } else if (type.equalsIgnoreCase("purple")) {
                              this._purpleSkills.get(k).add(augmentationId);
                           } else {
                              this._redSkills.get(k).add(augmentationId);
                           }

                           this._allSkills.put(augmentationId, new SkillHolder(skillId, skillLvL));
                        }
                     }
                  }
               }
            }

            if (badAugmantData != 0) {
               _log.info(this.getClass().getSimpleName() + ": " + badAugmantData + " bad skill(s) were skipped.");
            }
         } catch (Exception var20) {
            _log.log(Level.WARNING, this.getClass().getSimpleName() + ": ERROR parsing augmentation_skillmap.xml.", (Throwable)var20);
            return;
         }
      } else {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setValidating(false);
         factory.setIgnoringComments(true);
         File aFile = new File(Config.DATAPACK_ROOT + "/data/stats/skills/augmentation/retailchances.xml");
         if (!aFile.exists()) {
            _log.log(Level.WARNING, this.getClass().getSimpleName() + ": ERROR The retailchances.xml data file is missing.");
            return;
         }

         Document aDoc = null;

         try {
            aDoc = factory.newDocumentBuilder().parse(aFile);
         } catch (Exception var19) {
            var19.printStackTrace();
            return;
         }

         String aWeaponType = null;
         int aStoneId = 0;
         int aVariationId = 0;
         int aCategoryChance = 0;
         int aAugmentId = 0;
         float aAugmentChance = 0.0F;

         for(Node l = aDoc.getFirstChild(); l != null; l = l.getNextSibling()) {
            if (l.getNodeName().equals("list")) {
               NamedNodeMap aNodeAttributes = null;

               for(Node n = l.getFirstChild(); n != null; n = n.getNextSibling()) {
                  if (n.getNodeName().equals("weapon")) {
                     aNodeAttributes = n.getAttributes();
                     aWeaponType = aNodeAttributes.getNamedItem("type").getNodeValue();

                     for(Node c = n.getFirstChild(); c != null; c = c.getNextSibling()) {
                        if (c.getNodeName().equals("stone")) {
                           aNodeAttributes = c.getAttributes();
                           aStoneId = Integer.parseInt(aNodeAttributes.getNamedItem("id").getNodeValue());

                           for(Node v = c.getFirstChild(); v != null; v = v.getNextSibling()) {
                              if (v.getNodeName().equals("variation")) {
                                 aNodeAttributes = v.getAttributes();
                                 aVariationId = Integer.parseInt(aNodeAttributes.getNamedItem("id").getNodeValue());

                                 for(Node j = v.getFirstChild(); j != null; j = j.getNextSibling()) {
                                    if (j.getNodeName().equals("category")) {
                                       aNodeAttributes = j.getAttributes();
                                       aCategoryChance = Integer.parseInt(aNodeAttributes.getNamedItem("probability").getNodeValue());

                                       for(Node e = j.getFirstChild(); e != null; e = e.getNextSibling()) {
                                          if (e.getNodeName().equals("augment")) {
                                             aNodeAttributes = e.getAttributes();
                                             aAugmentId = Integer.parseInt(aNodeAttributes.getNamedItem("id").getNodeValue());
                                             aAugmentChance = Float.parseFloat(aNodeAttributes.getNamedItem("chance").getNodeValue());
                                             this._augmentationChances
                                                .add(
                                                   new AugmentationParser.AugmentationChance(
                                                      aWeaponType, aStoneId, aVariationId, aCategoryChance, aAugmentId, aAugmentChance
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
                  }
               }
            }
         }
      }

      if (Config.RETAIL_LIKE_AUGMENTATION_ACCESSORY) {
         DocumentBuilderFactory factory3 = DocumentBuilderFactory.newInstance();
         factory3.setValidating(false);
         factory3.setIgnoringComments(true);
         File aFile3 = new File(Config.DATAPACK_ROOT + "/data/stats/skills/augmentation/retailchances_accessory.xml");
         if (!aFile3.exists()) {
            _log.log(Level.WARNING, this.getClass().getSimpleName() + ": ERROR The retailchances_accessory.xml data file is missing.");
            return;
         }

         Document aDoc = null;

         try {
            aDoc = factory3.newDocumentBuilder().parse(aFile3);
         } catch (Exception var18) {
            var18.printStackTrace();
            return;
         }

         String aWeaponType = null;
         int aStoneId = 0;
         int aVariationId = 0;
         int aCategoryChance = 0;
         int aAugmentId = 0;
         float aAugmentChance = 0.0F;

         for(Node l = aDoc.getFirstChild(); l != null; l = l.getNextSibling()) {
            if (l.getNodeName().equals("list")) {
               NamedNodeMap aNodeAttributes = null;

               for(Node n = l.getFirstChild(); n != null; n = n.getNextSibling()) {
                  if (n.getNodeName().equals("weapon")) {
                     aNodeAttributes = n.getAttributes();
                     aWeaponType = aNodeAttributes.getNamedItem("type").getNodeValue();

                     for(Node c = n.getFirstChild(); c != null; c = c.getNextSibling()) {
                        if (c.getNodeName().equals("stone")) {
                           aNodeAttributes = c.getAttributes();
                           aStoneId = Integer.parseInt(aNodeAttributes.getNamedItem("id").getNodeValue());

                           for(Node v = c.getFirstChild(); v != null; v = v.getNextSibling()) {
                              if (v.getNodeName().equals("variation")) {
                                 aNodeAttributes = v.getAttributes();
                                 aVariationId = Integer.parseInt(aNodeAttributes.getNamedItem("id").getNodeValue());

                                 for(Node j = v.getFirstChild(); j != null; j = j.getNextSibling()) {
                                    if (j.getNodeName().equals("category")) {
                                       aNodeAttributes = j.getAttributes();
                                       aCategoryChance = Integer.parseInt(aNodeAttributes.getNamedItem("probability").getNodeValue());

                                       for(Node e = j.getFirstChild(); e != null; e = e.getNextSibling()) {
                                          if (e.getNodeName().equals("augment")) {
                                             aNodeAttributes = e.getAttributes();
                                             aAugmentId = Integer.parseInt(aNodeAttributes.getNamedItem("id").getNodeValue());
                                             aAugmentChance = Float.parseFloat(aNodeAttributes.getNamedItem("chance").getNodeValue());
                                             this._augmentationChancesAcc
                                                .add(
                                                   new AugmentationParser.augmentationChanceAcc(
                                                      aWeaponType, aStoneId, aVariationId, aCategoryChance, aAugmentId, aAugmentChance
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
                  }
               }
            }
         }
      }
   }

   public Augmentation generateRandomAugmentation(int lifeStoneLevel, int lifeStoneGrade, int bodyPart, int lifeStoneId, ItemInstance targetItem) {
      switch(bodyPart) {
         case 6:
         case 8:
         case 48:
            return this.generateRandomAccessoryAugmentation(lifeStoneLevel, bodyPart, lifeStoneId);
         default:
            return this.generateRandomWeaponAugmentation(lifeStoneLevel, lifeStoneGrade, lifeStoneId, targetItem);
      }
   }

   private Augmentation generateRandomWeaponAugmentation(int lifeStoneLevel, int lifeStoneGrade, int lifeStoneId, ItemInstance item) {
      int stat12 = 0;
      int stat34 = 0;
      if (Config.RETAIL_LIKE_AUGMENTATION) {
         if (((Weapon)item.getItem()).isMagicWeapon()) {
            List<AugmentationParser.AugmentationChance> _selectedChances12 = new ArrayList<>();
            List<AugmentationParser.AugmentationChance> _selectedChances34 = new ArrayList<>();

            for(AugmentationParser.AugmentationChance ac : this._augmentationChances) {
               if (ac.getWeaponType().equals("mage") && ac.getStoneId() == lifeStoneId) {
                  if (ac.getVariationId() == 1) {
                     _selectedChances12.add(ac);
                  } else {
                     _selectedChances34.add(ac);
                  }
               }
            }

            int r = Rnd.get(10000);
            float s = 10000.0F;

            for(AugmentationParser.AugmentationChance ac : _selectedChances12) {
               if (s > (float)r) {
                  s -= ac.getAugmentChance() * 100.0F;
                  stat12 = ac.getAugmentId();
               }
            }

            int[] gradeChance = null;
            switch(lifeStoneGrade) {
               case 0:
                  gradeChance = Config.RETAIL_LIKE_AUGMENTATION_NG_CHANCE;
                  break;
               case 1:
                  gradeChance = Config.RETAIL_LIKE_AUGMENTATION_MID_CHANCE;
                  break;
               case 2:
                  gradeChance = Config.RETAIL_LIKE_AUGMENTATION_HIGH_CHANCE;
                  break;
               case 3:
                  gradeChance = Config.RETAIL_LIKE_AUGMENTATION_TOP_CHANCE;
                  break;
               default:
                  gradeChance = Config.RETAIL_LIKE_AUGMENTATION_NG_CHANCE;
            }

            int c = Rnd.get(100);
            byte var44;
            if (c < gradeChance[0]) {
               var44 = 55;
            } else if (c < gradeChance[0] + gradeChance[1]) {
               var44 = 35;
            } else if (c < gradeChance[0] + gradeChance[1] + gradeChance[2]) {
               var44 = 7;
            } else {
               var44 = 3;
            }

            List<AugmentationParser.AugmentationChance> _selectedChances34final = new ArrayList<>();

            for(AugmentationParser.AugmentationChance ac : _selectedChances34) {
               if (ac.getCategoryChance() == var44) {
                  _selectedChances34final.add(ac);
               }
            }

            r = Rnd.get(10000);
            s = 10000.0F;

            for(AugmentationParser.AugmentationChance ac : _selectedChances34final) {
               if (s > (float)r) {
                  s -= ac.getAugmentChance() * 100.0F;
                  stat34 = ac.getAugmentId();
               }
            }
         } else {
            List<AugmentationParser.AugmentationChance> _selectedChances12 = new ArrayList<>();
            List<AugmentationParser.AugmentationChance> _selectedChances34 = new ArrayList<>();

            for(AugmentationParser.AugmentationChance ac : this._augmentationChances) {
               if (ac.getWeaponType().equals("warrior") && ac.getStoneId() == lifeStoneId) {
                  if (ac.getVariationId() == 1) {
                     _selectedChances12.add(ac);
                  } else {
                     _selectedChances34.add(ac);
                  }
               }
            }

            int r = Rnd.get(10000);
            float s = 10000.0F;

            for(AugmentationParser.AugmentationChance ac : _selectedChances12) {
               if (s > (float)r) {
                  s -= ac.getAugmentChance() * 100.0F;
                  stat12 = ac.getAugmentId();
               }
            }

            int[] gradeChance = null;
            switch(lifeStoneGrade) {
               case 0:
                  gradeChance = Config.RETAIL_LIKE_AUGMENTATION_NG_CHANCE;
                  break;
               case 1:
                  gradeChance = Config.RETAIL_LIKE_AUGMENTATION_MID_CHANCE;
                  break;
               case 2:
                  gradeChance = Config.RETAIL_LIKE_AUGMENTATION_HIGH_CHANCE;
                  break;
               case 3:
                  gradeChance = Config.RETAIL_LIKE_AUGMENTATION_TOP_CHANCE;
                  break;
               default:
                  gradeChance = Config.RETAIL_LIKE_AUGMENTATION_NG_CHANCE;
            }

            int c = Rnd.get(100);
            byte var47;
            if (c < gradeChance[0]) {
               var47 = 55;
            } else if (c < gradeChance[0] + gradeChance[1]) {
               var47 = 35;
            } else if (c < gradeChance[0] + gradeChance[1] + gradeChance[2]) {
               var47 = 7;
            } else {
               var47 = 3;
            }

            List<AugmentationParser.AugmentationChance> _selectedChances34final = new ArrayList<>();

            for(AugmentationParser.AugmentationChance ac : _selectedChances34) {
               if (ac.getCategoryChance() == var47) {
                  _selectedChances34final.add(ac);
               }
            }

            r = Rnd.get(10000);
            s = 10000.0F;

            for(AugmentationParser.AugmentationChance ac : _selectedChances34final) {
               if (s > (float)r) {
                  s -= ac.getAugmentChance() * 100.0F;
                  stat34 = ac.getAugmentId();
               }
            }
         }

         return new Augmentation((stat34 << 16) + stat12);
      } else {
         boolean generateSkill = false;
         boolean generateGlow = false;
         lifeStoneLevel = Math.min(lifeStoneLevel, 9);
         switch(lifeStoneGrade) {
            case 0:
               if (Rnd.get(1, 100) <= Config.AUGMENTATION_NG_SKILL_CHANCE) {
                  generateSkill = true;
               }

               if (Rnd.get(1, 100) <= Config.AUGMENTATION_NG_GLOW_CHANCE) {
                  generateGlow = true;
               }
               break;
            case 1:
               if (Rnd.get(1, 100) <= Config.AUGMENTATION_MID_SKILL_CHANCE) {
                  generateSkill = true;
               }

               if (Rnd.get(1, 100) <= Config.AUGMENTATION_MID_GLOW_CHANCE) {
                  generateGlow = true;
               }
               break;
            case 2:
               if (Rnd.get(1, 100) <= Config.AUGMENTATION_HIGH_SKILL_CHANCE) {
                  generateSkill = true;
               }

               if (Rnd.get(1, 100) <= Config.AUGMENTATION_HIGH_GLOW_CHANCE) {
                  generateGlow = true;
               }
               break;
            case 3:
               if (Rnd.get(1, 100) <= Config.AUGMENTATION_TOP_SKILL_CHANCE) {
                  generateSkill = true;
               }

               if (Rnd.get(1, 100) <= Config.AUGMENTATION_TOP_GLOW_CHANCE) {
                  generateGlow = true;
               }
               break;
            case 4:
               if (Rnd.get(1, 100) <= Config.AUGMENTATION_ACC_SKILL_CHANCE) {
                  generateSkill = true;
               }
         }

         if (!generateSkill && Rnd.get(1, 100) <= Config.AUGMENTATION_BASESTAT_CHANCE) {
            stat34 = Rnd.get(16341, 16344);
         }

         int resultColor = Rnd.get(0, 100);
         byte var22;
         if (stat34 == 0 && !generateSkill) {
            if (resultColor <= 15 * lifeStoneGrade + 40) {
               var22 = 1;
            } else {
               var22 = 0;
            }
         } else if (resultColor > 10 * lifeStoneGrade + 5 && stat34 == 0) {
            if (resultColor <= 10 * lifeStoneGrade + 10) {
               var22 = 1;
            } else {
               var22 = 2;
            }
         } else {
            var22 = 3;
         }

         if (generateSkill) {
            switch(var22) {
               case 1:
                  stat34 = this._blueSkills.get(lifeStoneLevel).get(Rnd.get(0, this._blueSkills.get(lifeStoneLevel).size() - 1));
                  break;
               case 2:
                  stat34 = this._purpleSkills.get(lifeStoneLevel).get(Rnd.get(0, this._purpleSkills.get(lifeStoneLevel).size() - 1));
                  break;
               case 3:
                  stat34 = this._redSkills.get(lifeStoneLevel).get(Rnd.get(0, this._redSkills.get(lifeStoneLevel).size() - 1));
            }
         }

         int offset;
         if (stat34 == 0) {
            int temp = Rnd.get(2, 3);
            int colorOffset = var22 * 910 + temp * 3640 + 1;
            offset = lifeStoneLevel * 91 + colorOffset;
            stat34 = Rnd.get(offset, offset + 91 - 1);
            if (generateGlow && lifeStoneGrade >= 2) {
               offset = lifeStoneLevel * 91 + (temp - 2) * 3640 + lifeStoneGrade * 910 + 1;
            } else {
               offset = lifeStoneLevel * 91 + (temp - 2) * 3640 + Rnd.get(0, 1) * 910 + 1;
            }
         } else if (!generateGlow) {
            offset = lifeStoneLevel * 91 + Rnd.get(0, 1) * 3640 + 1;
         } else {
            offset = lifeStoneLevel * 91 + Rnd.get(0, 1) * 3640 + (lifeStoneGrade + var22) / 2 * 910 + 1;
         }

         stat12 = Rnd.get(offset, offset + 91 - 1);
         if (Config.DEBUG) {
            _log.info(
               this.getClass().getSimpleName()
                  + ": Augmentation success: stat12="
                  + stat12
                  + "; stat34="
                  + stat34
                  + "; resultColor="
                  + var22
                  + "; level="
                  + lifeStoneLevel
                  + "; grade="
                  + lifeStoneGrade
            );
         }

         return new Augmentation((stat34 << 16) + stat12);
      }
   }

   private Augmentation generateRandomAccessoryAugmentation(int lifeStoneLevel, int bodyPart, int lifeStoneId) {
      int stat12 = 0;
      int stat34 = 0;
      if (Config.RETAIL_LIKE_AUGMENTATION_ACCESSORY) {
         List<AugmentationParser.augmentationChanceAcc> _selectedChances12 = new ArrayList<>();
         List<AugmentationParser.augmentationChanceAcc> _selectedChances34 = new ArrayList<>();

         for(AugmentationParser.augmentationChanceAcc ac : this._augmentationChancesAcc) {
            if (ac.getWeaponType().equals("warrior") && ac.getStoneId() == lifeStoneId) {
               if (ac.getVariationId() == 1) {
                  _selectedChances12.add(ac);
               } else {
                  _selectedChances34.add(ac);
               }
            }
         }

         int r = Rnd.get(10000);
         float s = 10000.0F;

         for(AugmentationParser.augmentationChanceAcc ac : _selectedChances12) {
            if (s > (float)r) {
               s -= ac.getAugmentChance() * 100.0F;
               stat12 = ac.getAugmentId();
            }
         }

         int c = Rnd.get(100);
         byte var29;
         if (c < 55) {
            var29 = 55;
         } else if (c < 90) {
            var29 = 35;
         } else if (c < 99) {
            var29 = 9;
         } else {
            var29 = 1;
         }

         List<AugmentationParser.augmentationChanceAcc> _selectedChances34final = new ArrayList<>();

         for(AugmentationParser.augmentationChanceAcc ac : _selectedChances34) {
            if (ac.getCategoryChance() == var29) {
               _selectedChances34final.add(ac);
            }
         }

         r = Rnd.get(10000);
         s = 10000.0F;

         for(AugmentationParser.augmentationChanceAcc ac : _selectedChances34final) {
            if (s > (float)r) {
               s -= ac.getAugmentChance() * 100.0F;
               stat34 = ac.getAugmentId();
            }
         }

         return new Augmentation((stat34 << 16) + stat12);
      } else {
         lifeStoneLevel = Math.min(lifeStoneLevel, 9);
         int base = 0;
         int skillsLength = 0;
         byte var20;
         switch(bodyPart) {
            case 6:
               base = 17689 + 102 * lifeStoneLevel;
               var20 = 18;
               break;
            case 8:
               base = 18709 + 108 * lifeStoneLevel;
               var20 = 24;
               break;
            case 48:
               base = 16669 + 102 * lifeStoneLevel;
               var20 = 18;
               break;
            default:
               return null;
         }

         int resultColor = Rnd.get(0, 3);
         stat12 = Rnd.get(21);
         Options op = null;
         if (Rnd.get(1, 100) <= Config.AUGMENTATION_ACC_SKILL_CHANCE) {
            stat34 = base + Rnd.get(var20);
            op = OptionsParser.getInstance().getOptions(stat34);
         }

         if (op == null || !op.hasActiveSkill() && !op.hasPassiveSkill() && !op.hasActivationSkills()) {
            stat34 = (stat12 + 1 + Rnd.get(20)) % 21;
            stat34 = base + var20 + 21 * resultColor + stat34;
         }

         stat12 = base + var20 + 21 * resultColor + stat12;
         if (Config.DEBUG) {
            _log.info(
               this.getClass().getSimpleName() + ": Accessory augmentation success: stat12=" + stat12 + "; stat34=" + stat34 + "; level=" + lifeStoneLevel
            );
         }

         return new Augmentation((stat34 << 16) + stat12);
      }
   }

   public int generateRandomSecondaryAugmentation() {
      int offset = 819 + Rnd.get(0, 1) * 3640 + 1820 + 1;
      return Rnd.get(offset, offset + 91 - 1);
   }

   public static final AugmentationParser getInstance() {
      return AugmentationParser.SingletonHolder._instance;
   }

   public class AugmentationChance {
      private final String _WeaponType;
      private final int _StoneId;
      private final int _VariationId;
      private final int _CategoryChance;
      private final int _AugmentId;
      private final float _AugmentChance;

      public AugmentationChance(String WeaponType, int StoneId, int VariationId, int CategoryChance, int AugmentId, float AugmentChance) {
         this._WeaponType = WeaponType;
         this._StoneId = StoneId;
         this._VariationId = VariationId;
         this._CategoryChance = CategoryChance;
         this._AugmentId = AugmentId;
         this._AugmentChance = AugmentChance;
      }

      public String getWeaponType() {
         return this._WeaponType;
      }

      public int getStoneId() {
         return this._StoneId;
      }

      public int getVariationId() {
         return this._VariationId;
      }

      public int getCategoryChance() {
         return this._CategoryChance;
      }

      public int getAugmentId() {
         return this._AugmentId;
      }

      public float getAugmentChance() {
         return this._AugmentChance;
      }
   }

   private static class SingletonHolder {
      protected static final AugmentationParser _instance = new AugmentationParser();
   }

   public class augmentationChanceAcc {
      private final String _WeaponType;
      private final int _StoneId;
      private final int _VariationId;
      private final int _CategoryChance;
      private final int _AugmentId;
      private final float _AugmentChance;

      public augmentationChanceAcc(String WeaponType, int StoneId, int VariationId, int CategoryChance, int AugmentId, float AugmentChance) {
         this._WeaponType = WeaponType;
         this._StoneId = StoneId;
         this._VariationId = VariationId;
         this._CategoryChance = CategoryChance;
         this._AugmentId = AugmentId;
         this._AugmentChance = AugmentChance;
      }

      public String getWeaponType() {
         return this._WeaponType;
      }

      public int getStoneId() {
         return this._StoneId;
      }

      public int getVariationId() {
         return this._VariationId;
      }

      public int getCategoryChance() {
         return this._CategoryChance;
      }

      public int getAugmentId() {
         return this._AugmentId;
      }

      public float getAugmentChance() {
         return this._AugmentChance;
      }
   }
}
