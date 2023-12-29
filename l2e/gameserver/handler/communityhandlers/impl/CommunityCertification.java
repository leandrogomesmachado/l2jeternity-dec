package l2e.gameserver.handler.communityhandlers.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.data.parser.SkillTreesParser;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.handler.communityhandlers.ICommunityBoardHandler;
import l2e.gameserver.model.SkillLearn;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.holders.ItemHolder;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class CommunityCertification extends AbstractCommunity implements ICommunityBoardHandler {
   private final Map<Integer, List<Integer>> _skills65List = new HashMap<>();
   private final Map<Integer, List<Integer>> _skills75List = new HashMap<>();
   private final Map<Integer, List<Integer>> _skills80List = new HashMap<>();
   private static int _attempt65 = 0;
   private static int _attempt75 = 0;
   private static int _attempt80 = 0;
   private static CommunityCertification _instance = new CommunityCertification();

   public CommunityCertification() {
      this.load();
      if (Config.DEBUG) {
         _log.info(this.getClass().getSimpleName() + ": Loading all functions.");
      }
   }

   protected void load() {
      for(ClassId classId : ClassId.values()) {
         if (classId.getRace() != null && classId.level() >= 3) {
            List<Integer> skillList65 = new ArrayList<>();
            List<Integer> skillList75 = new ArrayList<>();
            List<Integer> skillList80 = new ArrayList<>();

            for(SkillLearn sl : SkillTreesParser.getInstance().getSubClassSkillTree().values()) {
               if (sl != null) {
                  if (sl.getGetLevel() == 65) {
                     if (!skillList65.contains(sl.getId())) {
                        skillList65.add(sl.getId());
                     }
                  } else if (sl.getGetLevel() == 75) {
                     if (!skillList75.contains(sl.getId())) {
                        skillList75.add(sl.getId());
                     }
                  } else if (sl.getGetLevel() == 80 && !skillList80.contains(sl.getId())) {
                     skillList80.add(sl.getId());
                  }
               }
            }

            this._skills65List.put(classId.getId(), skillList65);
            this._skills75List.put(classId.getId(), skillList75);
            this._skills80List.put(classId.getId(), skillList80);
         }
      }

      if (Config.CERT_BLOCK_SKILL_LIST != null && !Config.CERT_BLOCK_SKILL_LIST.isEmpty()) {
         String[] allInfo = Config.CERT_BLOCK_SKILL_LIST.split(";");

         for(String allClasses : allInfo) {
            String[] classes = allClasses.split(":");
            int classId = Integer.parseInt(classes[0]);
            String[] skillList = classes[1].split(",");
            List<Integer> removeSkills = new ArrayList<>();

            for(String skillId : skillList) {
               removeSkills.add(Integer.parseInt(skillId));
            }

            if (this._skills65List.containsKey(classId)) {
               for(int sk : removeSkills) {
                  this._skills65List.get(classId).remove(Integer.valueOf(sk));
               }
            }

            if (this._skills75List.containsKey(classId)) {
               for(int sk : removeSkills) {
                  this._skills75List.get(classId).remove(Integer.valueOf(sk));
               }
            }

            if (this._skills80List.containsKey(classId)) {
               for(int sk : removeSkills) {
                  this._skills80List.get(classId).remove(Integer.valueOf(sk));
               }
            }

            removeSkills.clear();
         }
      }
   }

   @Override
   public String[] getBypassCommands() {
      return new String[]{
         "_bbscertification",
         "_bbscertup",
         "_bbscertdown",
         "_bbscert75learn",
         "_bbscert75skill",
         "_bbscert75reset",
         "_bbscert80learn",
         "_bbscert80skill",
         "_bbscert80reset"
      };
   }

   @Override
   public void onBypassCommand(String command, Player player) {
      if (checkUseCondition(player)) {
         StringTokenizer st = new StringTokenizer(command, "_");
         String cmd = st.nextToken();
         if ("bbscertification".equals(cmd)) {
            String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/certification/certification.htm");
            html = this.getCertificationInfo(html, player);
            separateAndSend(html, player);
         } else if ("bbscertup".equals(cmd)) {
            int skillId = Integer.parseInt(st.nextToken());
            int skillLevel = Integer.parseInt(st.nextToken());
            if (_attempt65 == 0) {
               this.onBypassCommand("_bbscertification", player);
               return;
            }

            if (skillId != 0) {
               if (Config.ALLOW_CERT_DONATE_MODE) {
                  if (Config.EMERGET_SKILLS_LEARN[0] > 0) {
                     if (player.getInventory().getItemByItemId(Config.EMERGET_SKILLS_LEARN[0]) == null) {
                        player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                        return;
                     }

                     if (player.getInventory().getItemByItemId(Config.EMERGET_SKILLS_LEARN[0]).getCount() < (long)Config.EMERGET_SKILLS_LEARN[1]) {
                        player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                        return;
                     }

                     player.destroyItemByItemId("EmergetSkill", Config.EMERGET_SKILLS_LEARN[0], (long)Config.EMERGET_SKILLS_LEARN[1], player, true);
                     Util.addServiceLog(player.getName() + " buy certification skillId: " + skillId + " level: " + skillLevel);
                  }

                  Skill sk = SkillsParser.getInstance().getInfo(skillId, skillLevel);
                  if (sk != null) {
                     player.addSkill(sk, true);
                     player.sendSkillList(false);
                     player.sendUserInfo();
                  }
               } else {
                  SkillLearn skill = this.selectSkill(skillId, skillLevel);
                  if (skill != null) {
                     for(ItemHolder item : skill.getRequiredItems()) {
                        if (player.getInventory().getItemByItemId(item.getId()) == null) {
                           player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                           return;
                        }

                        if (player.getInventory().getItemByItemId(item.getId()).getCount() < item.getCount()) {
                           player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                           return;
                        }
                     }

                     for(ItemHolder itemIdCount : skill.getRequiredItems()) {
                        if (!player.destroyItemByItemId("CertLearn", itemIdCount.getId(), itemIdCount.getCount(), null, true)) {
                           Util.handleIllegalPlayerAction(
                              player,
                              ""
                                 + player.getName()
                                 + ", level "
                                 + player.getLevel()
                                 + " lose required item Id: "
                                 + itemIdCount.getId()
                                 + " to learn skill while learning skill Id: "
                                 + skill.getId()
                                 + " level "
                                 + skill.getLvl()
                                 + "!"
                           );
                        }
                     }

                     Skill sk = SkillsParser.getInstance().getInfo(skill.getId(), skill.getLvl());
                     if (sk != null) {
                        player.addSkill(sk, true);
                        player.sendSkillList(false);
                        player.sendUserInfo();
                     }
                  }
               }
            }

            this.onBypassCommand("_bbscertification", player);
         } else if ("bbscertdown".equals(cmd)) {
            int skillId = Integer.parseInt(st.nextToken());
            int skillLevel = Integer.parseInt(st.nextToken());
            if (skillLevel < 0) {
               this.onBypassCommand("_bbscertification", player);
               return;
            }

            boolean foundSkill = false;
            if (skillLevel == 0) {
               Skill skill = player.getKnownSkill(skillId);
               if (skill != null) {
                  foundSkill = true;
                  player.removeSkill(skill, true);
               }
            } else if (skillId != 0) {
               Skill sk = SkillsParser.getInstance().getInfo(skillId, skillLevel);
               if (sk != null) {
                  foundSkill = true;
                  player.addSkill(sk, true);
               }
            }

            if (!Config.ALLOW_CERT_DONATE_MODE && foundSkill) {
               SkillLearn skill = this.selectSkill(skillId, skillLevel > 0 ? skillLevel : 1);
               if (skill != null) {
                  for(ItemHolder item : skill.getRequiredItems()) {
                     if (item != null) {
                        player.addItem("Return Book", item.getId(), item.getCount(), null, true);
                     }
                  }
               }
            }

            player.sendSkillList(false);
            player.sendUserInfo();
            this.onBypassCommand("_bbscertification", player);
         } else if ("bbscert75learn".equals(cmd)) {
            int attempt = Integer.parseInt(st.nextToken());
            int page = Integer.parseInt(st.nextToken());
            if (_attempt75 == 0) {
               return;
            }

            this.check75Skills(player, page, attempt);
         } else if ("bbscert75skill".equals(cmd)) {
            if (_attempt75 == 0) {
               this.onBypassCommand("_bbscertification", player);
               return;
            }

            int skillId = Integer.parseInt(st.nextToken());
            int skillLevel = Integer.parseInt(st.nextToken());
            int page = Integer.parseInt(st.nextToken());
            if (!this.checkValidSkill(player, this._skills75List.get(player.getClassId().getId()), skillId, skillLevel)) {
               this.onBypassCommand("_bbscertification", player);
               return;
            }

            if (skillId != 0) {
               if (Config.ALLOW_CERT_DONATE_MODE) {
                  if (Config.MASTER_SKILLS_LEARN[0] > 0) {
                     if (player.getInventory().getItemByItemId(Config.MASTER_SKILLS_LEARN[0]) == null) {
                        player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                        return;
                     }

                     if (player.getInventory().getItemByItemId(Config.MASTER_SKILLS_LEARN[0]).getCount() < (long)Config.MASTER_SKILLS_LEARN[1]) {
                        player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                        return;
                     }

                     player.destroyItemByItemId("MasterSkill", Config.MASTER_SKILLS_LEARN[0], (long)Config.MASTER_SKILLS_LEARN[1], player, true);
                     Util.addServiceLog(player.getName() + " buy certification skillId: " + skillId + " level: " + skillLevel);
                  }

                  Skill sk = SkillsParser.getInstance().getInfo(skillId, skillLevel);
                  if (sk != null) {
                     --_attempt75;
                     player.addSkill(sk, true);
                     player.sendSkillList(false);
                     player.sendUserInfo();
                  }
               } else {
                  SkillLearn skill = this.selectSkill(skillId, skillLevel);
                  if (skill != null) {
                     for(ItemHolder item : skill.getRequiredItems()) {
                        if (player.getInventory().getItemByItemId(item.getId()) == null) {
                           player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                           return;
                        }

                        if (player.getInventory().getItemByItemId(item.getId()).getCount() < item.getCount()) {
                           player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                           return;
                        }
                     }

                     for(ItemHolder itemIdCount : skill.getRequiredItems()) {
                        if (!player.destroyItemByItemId("CertLearn", itemIdCount.getId(), itemIdCount.getCount(), null, true)) {
                           Util.handleIllegalPlayerAction(
                              player,
                              ""
                                 + player.getName()
                                 + ", level "
                                 + player.getLevel()
                                 + " lose required item Id: "
                                 + itemIdCount.getId()
                                 + " to learn skill while learning skill Id: "
                                 + skill.getId()
                                 + " level "
                                 + skill.getLvl()
                                 + "!"
                           );
                        }
                     }

                     Skill sk = SkillsParser.getInstance().getInfo(skill.getId(), skill.getGetLevel());
                     if (sk != null) {
                        --_attempt75;
                        player.addSkill(sk, true);
                        player.sendSkillList(false);
                        player.sendUserInfo();
                     }
                  }
               }
            }

            if (_attempt75 > 0) {
               this.check75Skills(player, page, _attempt75);
            }

            this.onBypassCommand("_bbscertification", player);
         } else if ("bbscert75reset".equals(cmd)) {
            List<Integer> skills75List = this._skills75List.get(player.getClassId().getId());
            if (skills75List == null) {
               return;
            }

            if (Config.ALLOW_CERT_DONATE_MODE) {
               if (Config.CLEAN_SKILLS_LEARN[0] > 0) {
                  if (player.getInventory().getItemByItemId(Config.CLEAN_SKILLS_LEARN[0]) == null) {
                     player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                     return;
                  }

                  if (player.getInventory().getItemByItemId(Config.CLEAN_SKILLS_LEARN[0]).getCount() < (long)Config.CLEAN_SKILLS_LEARN[1]) {
                     player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                     return;
                  }

                  player.destroyItemByItemId("CleanSkills", Config.CLEAN_SKILLS_LEARN[0], (long)Config.CLEAN_SKILLS_LEARN[1], player, true);
                  Util.addServiceLog(player.getName() + " buy reset certification skills!");
               }

               for(int skillId : skills75List) {
                  Skill skill = player.getKnownSkill(skillId);
                  if (skill != null) {
                     player.removeSkill(skill, true);
                  }
               }
            } else {
               for(int skillId : skills75List) {
                  Skill skill = player.getKnownSkill(skillId);
                  if (skill != null) {
                     player.removeSkill(skill, true);
                     SkillLearn sl = this.selectSkill(skillId, 1);
                     if (sl != null) {
                        for(ItemHolder item : sl.getRequiredItems()) {
                           if (item != null) {
                              player.addItem("Return Book", item.getId(), item.getCount(), null, true);
                           }
                        }
                     }
                  }
               }
            }

            player.sendSkillList(false);
            player.sendUserInfo();
            this.onBypassCommand("_bbscertification", player);
         } else if ("bbscert80learn".equals(cmd)) {
            int attempt = Integer.parseInt(st.nextToken());
            int page = Integer.parseInt(st.nextToken());
            if (_attempt80 == 0) {
               return;
            }

            this.check80Skills(player, page, attempt);
         } else if ("bbscert80skill".equals(cmd)) {
            if (_attempt80 == 0) {
               this.onBypassCommand("_bbscertification", player);
               return;
            }

            int skillId = Integer.parseInt(st.nextToken());
            int skillLevel = Integer.parseInt(st.nextToken());
            int page = Integer.parseInt(st.nextToken());
            if (!this.checkValidSkill(player, this._skills80List.get(player.getClassId().getId()), skillId, skillLevel)) {
               this.onBypassCommand("_bbscertification", player);
               return;
            }

            if (skillId != 0) {
               if (Config.ALLOW_CERT_DONATE_MODE) {
                  if (Config.TRANSFORM_SKILLS_LEARN[0] > 0) {
                     if (player.getInventory().getItemByItemId(Config.TRANSFORM_SKILLS_LEARN[0]) == null) {
                        player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                        return;
                     }

                     if (player.getInventory().getItemByItemId(Config.TRANSFORM_SKILLS_LEARN[0]).getCount() < (long)Config.TRANSFORM_SKILLS_LEARN[1]) {
                        player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                        return;
                     }

                     player.destroyItemByItemId("CleanSkills", Config.TRANSFORM_SKILLS_LEARN[0], (long)Config.TRANSFORM_SKILLS_LEARN[1], player, true);
                     Util.addServiceLog(player.getName() + " buy certification trasform skillId: " + skillId + " level: " + skillLevel);
                  }

                  Skill sk = SkillsParser.getInstance().getInfo(skillId, skillLevel);
                  if (sk != null) {
                     --_attempt80;
                     player.addSkill(sk, true);
                     player.sendSkillList(false);
                     player.sendUserInfo();
                  }
               } else {
                  SkillLearn skill = this.selectSkill(skillId, skillLevel);
                  if (skill != null) {
                     for(ItemHolder item : skill.getRequiredItems()) {
                        if (player.getInventory().getItemByItemId(item.getId()) == null) {
                           player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                           return;
                        }

                        if (player.getInventory().getItemByItemId(item.getId()).getCount() < item.getCount()) {
                           player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                           return;
                        }
                     }

                     for(ItemHolder itemIdCount : skill.getRequiredItems()) {
                        if (!player.destroyItemByItemId("CertLearn", itemIdCount.getId(), itemIdCount.getCount(), null, true)) {
                           Util.handleIllegalPlayerAction(
                              player,
                              ""
                                 + player.getName()
                                 + ", level "
                                 + player.getLevel()
                                 + " lose required item Id: "
                                 + itemIdCount.getId()
                                 + " to learn skill while learning skill Id: "
                                 + skill.getId()
                                 + " level "
                                 + skill.getLvl()
                                 + "!"
                           );
                        }
                     }

                     Skill sk = SkillsParser.getInstance().getInfo(skill.getId(), skill.getGetLevel());
                     if (sk != null) {
                        --_attempt80;
                        player.addSkill(sk, true);
                        player.sendSkillList(false);
                        player.sendUserInfo();
                     }
                  }
               }
            }

            if (_attempt80 > 0) {
               this.check80Skills(player, page, _attempt80);
            }

            this.onBypassCommand("_bbscertification", player);
         } else if ("bbscert80reset".equals(cmd)) {
            List<Integer> skills80List = this._skills80List.get(player.getClassId().getId());
            if (skills80List == null) {
               return;
            }

            if (Config.ALLOW_CERT_DONATE_MODE) {
               if (Config.CLEAN_SKILLS_LEARN[0] > 0) {
                  if (player.getInventory().getItemByItemId(Config.CLEAN_SKILLS_LEARN[0]) == null) {
                     player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                     return;
                  }

                  if (player.getInventory().getItemByItemId(Config.CLEAN_SKILLS_LEARN[0]).getCount() < (long)Config.CLEAN_SKILLS_LEARN[1]) {
                     player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                     return;
                  }

                  player.destroyItemByItemId("CleanSkills", Config.CLEAN_SKILLS_LEARN[0], (long)Config.CLEAN_SKILLS_LEARN[1], player, true);
                  Util.addServiceLog(player.getName() + " buy reset certification skillIs!");
               }

               for(int skillId : skills80List) {
                  Skill skill = player.getKnownSkill(skillId);
                  if (skill != null) {
                     player.removeSkill(skill, true);
                  }
               }
            } else {
               for(int skillId : skills80List) {
                  Skill skill = player.getKnownSkill(skillId);
                  if (skill != null) {
                     player.removeSkill(skill, true);
                     SkillLearn sl = this.selectSkill(skillId, 3);
                     if (sl != null) {
                        for(ItemHolder item : sl.getRequiredItems()) {
                           if (item != null) {
                              player.addItem("Return Book", item.getId(), item.getCount(), null, true);
                           }
                        }
                     }
                  }
               }
            }

            player.sendSkillList(false);
            player.sendUserInfo();
            this.onBypassCommand("_bbscertification", player);
         }
      }
   }

   private String getCertificationInfo(String html, Player player) {
      _attempt65 = 6;
      _attempt75 = 3;
      _attempt80 = 3;
      List<Integer> skills65List = this._skills65List.get(player.getClassId().getId());

      for(int i = 0; i < skills65List.size(); ++i) {
         Skill skill = SkillsParser.getInstance().getInfo(skills65List.get(i), 1);
         if (skill != null) {
            html = html.replace("%65skillName-" + i + "%", this.checkSkillName(skill.getNameEn()));
            html = html.replace("%65skillIcon-" + i + "%", skill.getIcon());
            Skill plSkill = player.getKnownSkill(skill.getId());
            int nextLvl;
            if (plSkill != null) {
               nextLvl = plSkill.getLevel();
               _attempt65 -= plSkill.getLevel();
               html = html.replace("%65skillLvl-" + i + "%", String.valueOf(plSkill.getLevel()));
            } else {
               nextLvl = 0;
               html = html.replace("%65skillLvl-" + i + "%", String.valueOf(0));
            }

            html = html.replace(
               "%65skillUp-" + i + "%",
               "<button action=\"bypass _bbscertup_"
                  + skill.getId()
                  + "_"
                  + (nextLvl + 1)
                  + "\" value=\"+\" width=22 height=16 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">"
            );
            html = html.replace(
               "%65skillDown-" + i + "%",
               "<button action=\"bypass _bbscertdown_"
                  + skill.getId()
                  + "_"
                  + (nextLvl - 1)
                  + "\" value=\"-\" width=22 height=16 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">"
            );
            html = html.replace("%65skillInfo-" + i + "%", this.getSkillInfo(plSkill != null ? plSkill.getLevel() : 0));
         }
      }

      List<Integer> skills75List = this._skills75List.get(player.getClassId().getId());
      int amount75 = 0;

      for(int i = 0; i < skills75List.size(); ++i) {
         Skill plSkill = player.getKnownSkill(skills75List.get(i));
         if (plSkill != null && amount75 < 3) {
            ++amount75;
            --_attempt75;
            html = html.replace("%75skillIcon-" + amount75 + "%", plSkill.getIcon());
            html = html.replace("%name75skill-" + amount75 + "%", this.checkSkillName(plSkill.getNameEn()));
         }
      }

      if (amount75 < 3) {
         switch(amount75) {
            case 0:
               html = html.replace("%75skillIcon-1%", "icon.skill00000");
               html = html.replace("%75skillIcon-2%", "icon.skill00000");
               html = html.replace("%75skillIcon-3%", "icon.skill00000");
               html = html.replace("%name75skill-1%", "none");
               html = html.replace("%name75skill-2%", "none");
               html = html.replace("%name75skill-3%", "none");
               break;
            case 1:
               html = html.replace("%75skillIcon-2%", "icon.skill00000");
               html = html.replace("%75skillIcon-3%", "icon.skill00000");
               html = html.replace("%name75skill-2%", "none");
               html = html.replace("%name75skill-3%", "none");
               break;
            case 2:
               html = html.replace("%75skillIcon-3%", "icon.skill00000");
               html = html.replace("%name75skill-3%", "none");
         }
      }

      html = html.replace("%75attempt%", String.valueOf(_attempt75));
      List<Integer> skills80List = this._skills80List.get(player.getClassId().getId());
      int amount80 = 0;

      for(int i = 0; i < skills80List.size(); ++i) {
         Skill plSkill = player.getKnownSkill(skills80List.get(i));
         if (plSkill != null && amount80 < 3) {
            ++amount80;
            --_attempt80;
            html = html.replace("%80skillIcon-" + amount80 + "%", plSkill.getIcon());
            html = html.replace("%name80skill-" + amount80 + "%", this.checkSkillName(plSkill.getNameEn()));
         }
      }

      if (amount80 < 3) {
         switch(amount80) {
            case 0:
               html = html.replace("%80skillIcon-1%", "icon.skill00000");
               html = html.replace("%80skillIcon-2%", "icon.skill00000");
               html = html.replace("%80skillIcon-3%", "icon.skill00000");
               html = html.replace("%name80skill-1%", "none");
               html = html.replace("%name80skill-2%", "none");
               html = html.replace("%name80skill-3%", "none");
               break;
            case 1:
               html = html.replace("%80skillIcon-2%", "icon.skill00000");
               html = html.replace("%80skillIcon-3%", "icon.skill00000");
               html = html.replace("%name80skill-2%", "none");
               html = html.replace("%name80skill-3%", "none");
               break;
            case 2:
               html = html.replace("%80skillIcon-3%", "icon.skill00000");
               html = html.replace("%name80skill-3%", "none");
         }
      }

      return html.replace("%80attempt%", String.valueOf(_attempt80));
   }

   private String checkSkillName(String name) {
      String skillName = name.replace("Emergent Ability - ", "");
      skillName = skillName.replace("Master Ability - ", "");
      skillName = skillName.replace("Warrior Ability - ", "");
      skillName = skillName.replace("Rogue Ability - ", "");
      skillName = skillName.replace("Knight Ability - ", "");
      skillName = skillName.replace("Wizard Ability - ", "");
      skillName = skillName.replace("Healer Ability - ", "");
      skillName = skillName.replace("Summoner Ability - ", "");
      skillName = skillName.replace("Enchanter Ability - ", "");
      return skillName.replace("Transform Divine ", "");
   }

   private String getSkillInfo(int level) {
      switch(level) {
         case 1:
            return "+10";
         case 2:
            return "+20";
         case 3:
            return "+30";
         case 4:
            return "+40";
         case 5:
            return "+50";
         case 6:
            return "+60";
         default:
            return "0";
      }
   }

   private void check75Skills(Player player, int page, int attempt) {
      List<Integer> _skills = new ArrayList<>();
      List<Integer> skillLearnList = this._skills75List.get(player.getClassId().getId());
      int counts = 0;

      for(int skillId : skillLearnList) {
         Skill skill = player.getKnownSkill(skillId);
         if (skill == null) {
            ++counts;
            _skills.add(skillId);
         }
      }

      int perpage = 6;
      boolean isThereNextPage = _skills.size() > 6;
      NpcHtmlMessage html = new NpcHtmlMessage(5);
      html.setFile(player, player.getLang(), "data/html/community/certification/skills.htm");
      String template = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/certification/template.htm");
      String block = "";
      String list = "";
      int countss = 0;

      for(int i = (page - 1) * 6; i < _skills.size(); ++i) {
         int skillsId = _skills.get(i);
         if (skillsId != 0) {
            block = template.replace("%bypass%", "bypass _bbscert75skill_" + skillsId + "_1_" + page);
            block = block.replace("%name%", SkillsParser.getInstance().getInfo(skillsId, 1).getNameEn());
            block = block.replace("%icon%", SkillsParser.getInstance().getInfo(skillsId, 1).getIcon());
            list = list + block;
            if (++countss >= 6) {
               break;
            }
         }
      }

      double pages = (double)_skills.size() / 6.0;
      int count = (int)Math.ceil(pages);
      if (counts != 0 && attempt != 0) {
         html.replace("%list%", list);
         html.replace("%navigation%", Util.getNavigationBlock(count, page, _skills.size(), 6, isThereNextPage, "_bbscert75learn_" + attempt + "_%s"));
         player.sendPacket(html);
      }
   }

   private void check80Skills(Player player, int page, int attempt) {
      List<Integer> _skills = new ArrayList<>();
      List<Integer> skillLearnList = this._skills80List.get(player.getClassId().getId());
      int counts = 0;

      for(int skillId : skillLearnList) {
         Skill skill = player.getKnownSkill(skillId);
         if (skill == null || skill.getLevel() < 3) {
            ++counts;
            _skills.add(skillId);
         }
      }

      int perpage = 6;
      boolean isThereNextPage = _skills.size() > 6;
      NpcHtmlMessage html = new NpcHtmlMessage(5);
      html.setFile(player, player.getLang(), "data/html/community/certification/skills.htm");
      String template = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/certification/template.htm");
      String block = "";
      String list = "";
      int countss = 0;

      for(int i = (page - 1) * 6; i < _skills.size(); ++i) {
         int skillsId = _skills.get(i);
         if (skillsId != 0) {
            int level = 1;
            Skill plSkill = player.getKnownSkill(skillsId);
            if (plSkill != null) {
               level = plSkill.getLevel() + 1;
            }

            block = template.replace("%bypass%", "bypass _bbscert80skill_" + skillsId + "_" + level + "_" + page);
            block = block.replace("%name%", SkillsParser.getInstance().getInfo(skillsId, 1).getNameEn());
            block = block.replace("%icon%", SkillsParser.getInstance().getInfo(skillsId, 1).getIcon());
            list = list + block;
            if (++countss >= 6) {
               break;
            }
         }
      }

      double pages = (double)_skills.size() / 6.0;
      int count = (int)Math.ceil(pages);
      if (counts != 0 && attempt != 0) {
         html.replace("%list%", list);
         html.replace("%navigation%", Util.getNavigationBlock(count, page, _skills.size(), 6, isThereNextPage, "_bbscert80learn_" + attempt + "_%s"));
         player.sendPacket(html);
      }
   }

   private static boolean checkUseCondition(Player player) {
      if (player == null) {
         return false;
      } else if (player.getClassId().level() < 3) {
         player.sendMessage("Your level of profession is too low!");
         return false;
      } else if (player.getLevel() < Config.CERT_MIN_LEVEL) {
         player.sendMessage("Your level is lower! Need to be " + Config.CERT_MIN_LEVEL + " level.");
         return false;
      } else {
         return true;
      }
   }

   private SkillLearn selectSkill(int skillId, int skillLvl) {
      for(SkillLearn sl : SkillTreesParser.getInstance().getSubClassSkillTree().values()) {
         if (sl != null && sl.getId() == skillId && sl.getLvl() == skillLvl) {
            return sl;
         }
      }

      return null;
   }

   private boolean checkValidSkill(Player player, List<Integer> skillList, int id, int lvl) {
      if (player != null && skillList != null) {
         for(int skillId : skillList) {
            if (skillId == id) {
               if (lvl == 1) {
                  return true;
               }

               Skill oldSkill = player.getKnownSkill(id);
               if (oldSkill != null && lvl == oldSkill.getLevel() + 1) {
                  return true;
               }

               return false;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   @Override
   public void onWriteCommand(String command, String s, String s1, String s2, String s3, String s4, Player Player) {
   }

   public static CommunityCertification getInstance() {
      if (_instance == null) {
         _instance = new CommunityCertification();
      }

      return _instance;
   }
}
