package l2e.gameserver.model.actor.instance;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import l2e.commons.util.StringUtil;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.data.parser.ClassListParser;
import l2e.gameserver.data.parser.InitialShortcutParser;
import l2e.gameserver.data.parser.SkillTreesParser;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.instancemanager.FortSiegeManager;
import l2e.gameserver.instancemanager.SiegeManager;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.ClanMember;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.SkillLearn;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.base.AcquireSkillType;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.base.ClassType;
import l2e.gameserver.model.base.PlayerClass;
import l2e.gameserver.model.base.Race;
import l2e.gameserver.model.base.SubClass;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.AcquireSkillDone;
import l2e.gameserver.network.serverpackets.AcquireSkillList;
import l2e.gameserver.network.serverpackets.MagicSkillLaunched;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.ShortCutInit;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class VillageMasterInstance extends NpcInstance {
   private static Logger _log = Logger.getLogger(VillageMasterInstance.class.getName());

   public VillageMasterInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.VillageMasterInstance);
   }

   @Override
   public String getHtmlPath(int npcId, int val) {
      String pom = "";
      if (val == 0) {
         pom = "" + npcId;
      } else {
         pom = npcId + "-" + val;
      }

      return "data/html/villagemaster/" + pom + ".htm";
   }

   @Override
   public void onBypassFeedback(Player player, String command) {
      String[] commandStr = command.split(" ");
      String actualCommand = commandStr[0];
      String cmdParams = "";
      String cmdParams2 = "";
      if (commandStr.length >= 2) {
         cmdParams = commandStr[1];
      }

      if (commandStr.length >= 3) {
         cmdParams2 = commandStr[2];
      }

      if (actualCommand.equalsIgnoreCase("create_clan")) {
         if (cmdParams.isEmpty()) {
            return;
         }

         if (!isValidName(cmdParams)) {
            player.sendPacket(SystemMessageId.CLAN_NAME_INCORRECT);
            return;
         }

         ClanHolder.getInstance().createClan(player, cmdParams);
      } else if (actualCommand.equalsIgnoreCase("create_academy")) {
         if (cmdParams.isEmpty()) {
            return;
         }

         createSubPledge(player, cmdParams, null, -1, 5);
      } else if (actualCommand.equalsIgnoreCase("rename_pledge")) {
         if (cmdParams.isEmpty() || cmdParams2.isEmpty()) {
            return;
         }

         renameSubPledge(player, Integer.parseInt(cmdParams), cmdParams2);
      } else if (actualCommand.equalsIgnoreCase("create_royal")) {
         if (cmdParams.isEmpty()) {
            return;
         }

         createSubPledge(player, cmdParams, cmdParams2, 100, 6);
      } else if (actualCommand.equalsIgnoreCase("create_knight")) {
         if (cmdParams.isEmpty()) {
            return;
         }

         createSubPledge(player, cmdParams, cmdParams2, 1001, 7);
      } else if (actualCommand.equalsIgnoreCase("assign_subpl_leader")) {
         if (cmdParams.isEmpty()) {
            return;
         }

         assignSubPledgeLeader(player, cmdParams, cmdParams2);
      } else if (actualCommand.equalsIgnoreCase("create_ally")) {
         if (cmdParams.isEmpty()) {
            return;
         }

         if (player.getClan() == null) {
            player.sendPacket(SystemMessageId.ONLY_CLAN_LEADER_CREATE_ALLIANCE);
         } else {
            player.getClan().createAlly(player, cmdParams);
         }
      } else if (actualCommand.equalsIgnoreCase("dissolve_ally")) {
         player.getClan().dissolveAlly(player);
      } else if (actualCommand.equalsIgnoreCase("dissolve_clan")) {
         dissolveClan(player, player.getClanId());
      } else if (actualCommand.equalsIgnoreCase("change_clan_leader")) {
         if (cmdParams.isEmpty()) {
            return;
         }

         if (!player.isClanLeader()) {
            player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
            return;
         }

         if (player.getName().equalsIgnoreCase(cmdParams)) {
            return;
         }

         Clan clan = player.getClan();
         ClanMember member = clan.getClanMember(cmdParams);
         if (member == null) {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DOES_NOT_EXIST);
            sm.addString(cmdParams);
            player.sendPacket(sm);
            return;
         }

         if (!member.isOnline()) {
            player.sendPacket(SystemMessageId.INVITED_USER_NOT_ONLINE);
            return;
         }

         if (member.getPlayerInstance().isAcademyMember()) {
            player.sendPacket(SystemMessageId.RIGHT_CANT_TRANSFERRED_TO_ACADEMY_MEMBER);
            return;
         }

         if (Config.ALT_CLAN_LEADER_INSTANT_ACTIVATION) {
            clan.setNewLeader(member);
         } else {
            NpcHtmlMessage msg = new NpcHtmlMessage(this.getObjectId());
            if (clan.getNewLeaderId() == 0) {
               clan.setNewLeaderId(member.getObjectId(), true);
               msg.setFile(player, "data/scripts/village_master/Clan/" + player.getLang() + "/9000-07-success.htm");
            } else {
               msg.setFile(player, "data/scripts/village_master/Clan/" + player.getLang() + "/9000-07-in-progress.htm");
            }

            player.sendPacket(msg);
         }
      } else if (actualCommand.equalsIgnoreCase("cancel_clan_leader_change")) {
         if (!player.isClanLeader()) {
            player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
            return;
         }

         Clan clan = player.getClan();
         NpcHtmlMessage msg = new NpcHtmlMessage(this.getObjectId());
         if (clan.getNewLeaderId() != 0) {
            clan.setNewLeaderId(0, true);
            msg.setFile(player, "data/scripts/village_master/Clan/" + player.getLang() + "/9000-07-canceled.htm");
         } else {
            msg.setHtml(player, "<html><body>You don't have clan leader delegation applications submitted yet!</body></html>");
         }

         player.sendPacket(msg);
      } else if (actualCommand.equalsIgnoreCase("recover_clan")) {
         recoverClan(player, player.getClanId());
      } else if (actualCommand.equalsIgnoreCase("increase_clan_level")) {
         if (player.getClan().levelUpClan(player)) {
            player.broadcastPacket(new MagicSkillUse(player, 5103, 1, 0, 0));
            player.broadcastPacket(new MagicSkillLaunched(player, 5103, 1));
         }
      } else if (actualCommand.equalsIgnoreCase("learn_clan_skills")) {
         showPledgeSkillList(player);
      } else if (command.startsWith("Subclass")) {
         if (player.isActionsDisabled() || player.isAllSkillsDisabled()) {
            player.sendPacket(SystemMessageId.SUBCLASS_NO_CHANGE_OR_CREATE_WHILE_SKILL_IN_USE);
            return;
         }

         NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
         if (player.getTransformation() != null) {
            html.setFile(player, player.getLang(), "data/html/villagemaster/SubClass_NoTransformed.htm");
            player.sendPacket(html);
            return;
         }

         if (player.hasSummon()) {
            html.setFile(player, player.getLang(), "data/html/villagemaster/SubClass_NoSummon.htm");
            player.sendPacket(html);
            return;
         }

         if (!player.isInventoryUnder90(true)) {
            player.sendPacket(SystemMessageId.NOT_SUBCLASS_WHILE_INVENTORY_FULL);
            return;
         }

         if (player.getWeightPenalty() >= 2) {
            player.sendPacket(SystemMessageId.NOT_SUBCLASS_WHILE_OVERWEIGHT);
            return;
         }

         if (!player.isInsideZone(ZoneId.PEACE) || player.getDuelState() != 0) {
            player.sendMessage(new ServerMessage("CommunityGeneral.CANT_SUB", player.getLang()).toString());
            return;
         }

         int cmdChoice = 0;
         int paramOne = 0;
         int paramTwo = 0;

         try {
            cmdChoice = Integer.parseInt(command.substring(9, 10).trim());
            int endIndex = command.indexOf(32, 11);
            if (endIndex == -1) {
               endIndex = command.length();
            }

            if (command.length() > 11) {
               paramOne = Integer.parseInt(command.substring(11, endIndex).trim());
               if (command.length() > endIndex) {
                  paramTwo = Integer.parseInt(command.substring(endIndex).trim());
               }
            }
         } catch (Exception var17) {
            _log.warning(VillageMasterInstance.class.getName() + ": Wrong numeric values for command " + command);
         }

         Set<PlayerClass> subsAvailable = null;
         switch(cmdChoice) {
            case 0:
               html.setFile(player, player.getLang(), this.getSubClassMenu(player.getRace()));
               break;
            case 1:
               if (player.getTotalSubClasses() >= Config.MAX_SUBCLASS) {
                  html.setFile(player, player.getLang(), this.getSubClassFail());
                  break;
               } else {
                  subsAvailable = this.getAvailableSubClasses(player);
                  if (subsAvailable != null && !subsAvailable.isEmpty()) {
                     html.setFile(player, player.getLang(), "data/html/villagemaster/SubClass_Add.htm");
                     StringBuilder content1 = StringUtil.startAppend(200);

                     for(PlayerClass subClass : subsAvailable) {
                        StringUtil.append(
                           content1,
                           "<a action=\"bypass -h npc_%objectId%_Subclass 4 ",
                           String.valueOf(subClass.ordinal()),
                           "\" msg=\"1268;",
                           ClassListParser.getInstance().getClass(subClass.ordinal()).getClassName(),
                           "\">",
                           ClassListParser.getInstance().getClass(subClass.ordinal()).getClientCode(),
                           "</a><br>"
                        );
                     }

                     html.replace("%list%", content1.toString());
                     break;
                  }

                  if (player.getRace() == Race.Elf || player.getRace() == Race.DarkElf) {
                     html.setFile(player, player.getLang(), "data/html/villagemaster/SubClass_Fail_Elves.htm");
                     player.sendPacket(html);
                  } else if (player.getRace() == Race.Kamael) {
                     html.setFile(player, player.getLang(), "data/html/villagemaster/SubClass_Fail_Kamael.htm");
                     player.sendPacket(html);
                  } else {
                     player.sendMessage(new ServerMessage("CommunityGeneral.NO_SUB", player.getLang()).toString());
                  }

                  return;
               }
            case 2:
               if (player.getSubClasses().isEmpty()) {
                  html.setFile(player, player.getLang(), "data/html/villagemaster/SubClass_ChangeNo.htm");
               } else {
                  StringBuilder content2 = StringUtil.startAppend(200);
                  if (this.checkVillageMaster(player.getBaseClass())) {
                     StringUtil.append(
                        content2,
                        "<a action=\"bypass -h npc_%objectId%_Subclass 5 0\">",
                        ClassListParser.getInstance().getClass(player.getBaseClass()).getClientCode(),
                        "</a><br>"
                     );
                  }

                  Iterator<SubClass> subList = iterSubClasses(player);

                  while(subList.hasNext()) {
                     SubClass subClass = subList.next();
                     if (this.checkVillageMaster(subClass.getClassDefinition())) {
                        StringUtil.append(
                           content2,
                           "<a action=\"bypass -h npc_%objectId%_Subclass 5 ",
                           String.valueOf(subClass.getClassIndex()),
                           "\">",
                           ClassListParser.getInstance().getClass(subClass.getClassId()).getClientCode(),
                           "</a><br>"
                        );
                     }
                  }

                  if (content2.length() > 0) {
                     html.setFile(player, player.getLang(), "data/html/villagemaster/SubClass_Change.htm");
                     html.replace("%list%", content2.toString());
                  } else {
                     html.setFile(player, player.getLang(), "data/html/villagemaster/SubClass_ChangeNotFound.htm");
                  }
               }
               break;
            case 3:
               if (player.getSubClasses() != null && !player.getSubClasses().isEmpty()) {
                  if (player.getTotalSubClasses() > 3) {
                     html.setFile(player, player.getLang(), "data/html/villagemaster/SubClass_ModifyCustom.htm");
                     StringBuilder content3 = StringUtil.startAppend(200);
                     int classIndex = 1;
                     Iterator<SubClass> subList = iterSubClasses(player);

                     while(subList.hasNext()) {
                        SubClass subClass = subList.next();
                        StringUtil.append(
                           content3,
                           "Sub-class ",
                           String.valueOf(classIndex++),
                           "<br>",
                           "<a action=\"bypass -h npc_%objectId%_Subclass 6 ",
                           String.valueOf(subClass.getClassIndex()),
                           "\">",
                           ClassListParser.getInstance().getClass(subClass.getClassId()).getClientCode(),
                           "</a><br>"
                        );
                     }

                     html.replace("%list%", content3.toString());
                  } else {
                     html.setFile(player, player.getLang(), "data/html/villagemaster/SubClass_Modify.htm");
                     if (player.getSubClasses().containsKey(1)) {
                        html.replace("%sub1%", ClassListParser.getInstance().getClass(player.getSubClasses().get(1).getClassId()).getClientCode());
                     } else {
                        html.replace("<a action=\"bypass -h npc_%objectId%_Subclass 6 1\">%sub1%</a><br>", "");
                     }

                     if (player.getSubClasses().containsKey(2)) {
                        html.replace("%sub2%", ClassListParser.getInstance().getClass(player.getSubClasses().get(2).getClassId()).getClientCode());
                     } else {
                        html.replace("<a action=\"bypass -h npc_%objectId%_Subclass 6 2\">%sub2%</a><br>", "");
                     }

                     if (player.getSubClasses().containsKey(3)) {
                        html.replace("%sub3%", ClassListParser.getInstance().getClass(player.getSubClasses().get(3).getClassId()).getClientCode());
                     } else {
                        html.replace("<a action=\"bypass -h npc_%objectId%_Subclass 6 3\">%sub3%</a><br>", "");
                     }
                  }
               } else {
                  html.setFile(player, player.getLang(), "data/html/villagemaster/SubClass_ModifyEmpty.htm");
               }
               break;
            case 4:
               if (!player.checkFloodProtection("SUBCLASS", "subclass_change")) {
                  _log.warning(VillageMasterInstance.class.getName() + ": Player " + player.getName() + " has performed a subclass change too fast");
                  return;
               }

               boolean added = this.addNewSubclass(player, paramOne);
               if (added) {
                  html.setFile(player, player.getLang(), "data/html/villagemaster/SubClass_AddOk.htm");
               } else {
                  html.setFile(player, player.getLang(), this.getSubClassFail());
               }
               break;
            case 5:
               if (!player.checkFloodProtection("SUBCLASS", "subclass_change")) {
                  _log.warning(VillageMasterInstance.class.getName() + ": Player " + player.getName() + " has performed a subclass change too fast");
                  return;
               }

               if (Config.SUBCLASS_STORE_SKILL && player.getBuffCount() < 1) {
                  player.sendMessage("Take one Buff to your self first.");
                  return;
               }

               if (player.isInCombat()) {
                  player.sendMessage(new ServerMessage("CommunityGeneral.SUB_IN_COMBAT", player.getLang()).toString());
                  return;
               }

               if (player.getClassIndex() != paramOne) {
                  if (paramOne == 0) {
                     if (!this.checkVillageMaster(player.getBaseClass())) {
                        return;
                     }
                  } else {
                     try {
                        if (!this.checkVillageMaster(player.getSubClasses().get(paramOne).getClassDefinition())) {
                           return;
                        }
                     } catch (NullPointerException var16) {
                        return;
                     }
                  }

                  player.setActiveClass(paramOne);
                  player.sendPacket(SystemMessageId.SUBCLASS_TRANSFER_COMPLETED);
                  return;
               }

               html.setFile(player, player.getLang(), "data/html/villagemaster/SubClass_Current.htm");
               break;
            case 6:
               if (paramOne < 1 || paramOne > Config.MAX_SUBCLASS) {
                  return;
               }

               if (player.isInCombat()) {
                  player.sendMessage(new ServerMessage("CommunityGeneral.SUB_IN_COMBAT", player.getLang()).toString());
                  return;
               }

               subsAvailable = this.getAvailableSubClasses(player);
               if (subsAvailable == null || subsAvailable.isEmpty()) {
                  player.sendMessage(new ServerMessage("CommunityGeneral.NO_SUB", player.getLang()).toString());
                  return;
               }

               StringBuilder content6 = StringUtil.startAppend(200);

               for(PlayerClass subClass : subsAvailable) {
                  StringUtil.append(
                     content6,
                     "<a action=\"bypass -h npc_%objectId%_Subclass 7 ",
                     String.valueOf(paramOne),
                     " ",
                     String.valueOf(subClass.ordinal()),
                     "\" msg=\"1445;",
                     "\">",
                     ClassListParser.getInstance().getClass(subClass.ordinal()).getClientCode(),
                     "</a><br>"
                  );
               }

               switch(paramOne) {
                  case 1:
                     html.setFile(player, player.getLang(), "data/html/villagemaster/SubClass_ModifyChoice1.htm");
                     break;
                  case 2:
                     html.setFile(player, player.getLang(), "data/html/villagemaster/SubClass_ModifyChoice2.htm");
                     break;
                  case 3:
                     html.setFile(player, player.getLang(), "data/html/villagemaster/SubClass_ModifyChoice3.htm");
                     break;
                  default:
                     html.setFile(player, player.getLang(), "data/html/villagemaster/SubClass_ModifyChoice.htm");
               }

               html.replace("%list%", content6.toString());
               break;
            case 7:
               if (!player.checkFloodProtection("SUBCLASS", "subclass_change")) {
                  _log.warning(VillageMasterInstance.class.getName() + ": Player " + player.getName() + " has performed a subclass change too fast");
                  return;
               }

               if (player.isInCombat()) {
                  player.sendMessage(new ServerMessage("CommunityGeneral.SUB_IN_COMBAT", player.getLang()).toString());
                  return;
               }

               if (!this.isValidNewSubClass(player, paramTwo)) {
                  return;
               }

               if (!player.modifySubClass(paramOne, paramTwo)) {
                  player.setActiveClass(0);
                  player.sendMessage(new ServerMessage("CommunityGeneral.CANT_ADD_SUB", player.getLang()).toString());
                  return;
               }

               player.abortCast();
               player.stopAllEffectsExceptThoseThatLastThroughDeath();
               player.stopAllEffectsNotStayOnSubclassChange();
               player.stopCubics();
               player.setActiveClass(paramOne);
               InitialShortcutParser.getInstance().registerAllShortcuts(player);
               player.sendPacket(new ShortCutInit(player));
               html.setFile(player, player.getLang(), "data/html/villagemaster/SubClass_ModifyOk.htm");
               html.replace("%name%", ClassListParser.getInstance().getClass(paramTwo).getClientCode());
               player.sendPacket(SystemMessageId.ADD_NEW_SUBCLASS);
         }

         html.replace("%objectId%", String.valueOf(this.getObjectId()));
         player.sendPacket(html);
      } else {
         super.onBypassFeedback(player, command);
      }
   }

   public boolean addNewSubclass(Player player, int classId) {
      boolean allowAddition = true;
      if (player.getTotalSubClasses() >= Config.MAX_SUBCLASS) {
         allowAddition = false;
      }

      if (player.getLevel() < 75) {
         allowAddition = false;
      }

      if (allowAddition && !player.getSubClasses().isEmpty()) {
         Iterator<SubClass> subList = iterSubClasses(player);

         while(subList.hasNext()) {
            SubClass subClass = subList.next();
            if (subClass.getLevel() < 75) {
               allowAddition = false;
               break;
            }
         }
      }

      if (allowAddition && !Config.ALT_GAME_SUBCLASS_WITHOUT_QUESTS) {
         allowAddition = this.checkQuests(player);
      }

      if (allowAddition && this.isValidNewSubClass(player, classId)) {
         if (!player.addSubClass(classId, player.getTotalSubClasses() + 1)) {
            return false;
         } else {
            player.setActiveClass(player.getTotalSubClasses());
            InitialShortcutParser.getInstance().registerAllShortcuts(player);
            player.sendPacket(new ShortCutInit(player));
            player.sendPacket(SystemMessageId.ADD_NEW_SUBCLASS);
            return true;
         }
      } else {
         return false;
      }
   }

   protected String getSubClassMenu(Race pRace) {
      return !Config.ALT_GAME_SUBCLASS_EVERYWHERE && !Config.ALT_GAME_SUBCLASS_ALL_CLASSES && pRace == Race.Kamael
         ? "data/html/villagemaster/SubClass_NoOther.htm"
         : "data/html/villagemaster/SubClass.htm";
   }

   protected String getSubClassFail() {
      return "data/html/villagemaster/SubClass_Fail.htm";
   }

   protected boolean checkQuests(Player player) {
      if (player.isNoble()) {
         return true;
      } else {
         QuestState qs = player.getQuestState("_234_FatesWhisper");
         if (qs != null && qs.isCompleted()) {
            qs = player.getQuestState("_235_MimirsElixir");
            return qs != null && qs.isCompleted();
         } else {
            return false;
         }
      }
   }

   public final Set<PlayerClass> getAvailableSubClasses(Player player) {
      int currentBaseId = player.getBaseClass();
      ClassId baseCID = ClassId.getClassId(currentBaseId);
      int baseClassId;
      if (baseCID.level() > 2) {
         baseClassId = baseCID.getParent().ordinal();
      } else {
         baseClassId = currentBaseId;
      }

      Set<PlayerClass> availSubs = PlayerClass.values()[baseClassId].getAvailableSubclasses(player);
      if (availSubs != null && !availSubs.isEmpty()) {
         Iterator<PlayerClass> availSub = availSubs.iterator();

         while(availSub.hasNext()) {
            PlayerClass pclass = availSub.next();
            if (!this.checkVillageMaster(pclass)) {
               availSub.remove();
            } else {
               int availClassId = pclass.ordinal();
               ClassId cid = ClassId.getClassId(availClassId);
               Iterator<SubClass> subList = iterSubClasses(player);

               while(subList.hasNext()) {
                  SubClass prevSubClass = subList.next();
                  ClassId subClassId = ClassId.getClassId(prevSubClass.getClassId());
                  if (subClassId.equalsOrChildOf(cid)) {
                     availSub.remove();
                     break;
                  }
               }
            }
         }
      }

      return availSubs;
   }

   private final boolean isValidNewSubClass(Player player, int classId) {
      if (!this.checkVillageMaster(classId)) {
         return false;
      } else {
         ClassId cid = ClassId.values()[classId];
         Iterator<SubClass> subList = iterSubClasses(player);

         while(subList.hasNext()) {
            SubClass sub = subList.next();
            ClassId subClassId = ClassId.values()[sub.getClassId()];
            if (subClassId.equalsOrChildOf(cid)) {
               return false;
            }
         }

         int currentBaseId = player.getBaseClass();
         ClassId baseCID = ClassId.getClassId(currentBaseId);
         int baseClassId;
         if (baseCID.level() > 2) {
            baseClassId = baseCID.getParent().ordinal();
         } else {
            baseClassId = currentBaseId;
         }

         Set<PlayerClass> availSubs = PlayerClass.values()[baseClassId].getAvailableSubclasses(player);
         if (availSubs != null && !availSubs.isEmpty()) {
            boolean found = false;

            for(PlayerClass pclass : availSubs) {
               if (pclass.ordinal() == classId) {
                  found = true;
                  break;
               }
            }

            return found;
         } else {
            return false;
         }
      }
   }

   public final Race getVillageMasterRace() {
      String npcClass = this.getTemplate().getClientClass().toLowerCase();
      if (npcClass.contains("human")) {
         return Race.Human;
      } else if (npcClass.contains("darkelf")) {
         return Race.DarkElf;
      } else if (npcClass.contains("elf")) {
         return Race.Elf;
      } else if (npcClass.contains("orc")) {
         return Race.Orc;
      } else {
         return npcClass.contains("dwarf") ? Race.Dwarf : Race.Kamael;
      }
   }

   public final ClassType getVillageMasterTeachType() {
      String npcClass = this.getTemplate().getClientClass().toLowerCase();
      if (npcClass.contains("sanctuary") || npcClass.contains("clergyman")) {
         return ClassType.Priest;
      } else {
         return !npcClass.contains("mageguild") && !npcClass.contains("patriarch") ? ClassType.Fighter : ClassType.Mystic;
      }
   }

   protected boolean checkVillageMasterRace(PlayerClass pclass) {
      return true;
   }

   protected boolean checkVillageMasterTeachType(PlayerClass pclass) {
      return true;
   }

   public final boolean checkVillageMaster(int classId) {
      return this.checkVillageMaster(PlayerClass.values()[classId]);
   }

   public final boolean checkVillageMaster(PlayerClass pclass) {
      if (Config.ALT_GAME_SUBCLASS_EVERYWHERE) {
         return true;
      } else {
         return this.checkVillageMasterRace(pclass) && this.checkVillageMasterTeachType(pclass);
      }
   }

   private static final Iterator<SubClass> iterSubClasses(Player player) {
      return player.getSubClasses().values().iterator();
   }

   private static final void dissolveClan(Player player, int clanId) {
      if (!player.isClanLeader()) {
         player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
      } else {
         Clan clan = player.getClan();
         if (clan.getAllyId() != 0) {
            player.sendPacket(SystemMessageId.CANNOT_DISPERSE_THE_CLANS_IN_ALLY);
         } else if (clan.isAtWar()) {
            player.sendPacket(SystemMessageId.CANNOT_DISSOLVE_WHILE_IN_WAR);
         } else if (clan.getCastleId() == 0 && clan.getHideoutId() == 0 && clan.getFortId() == 0) {
            for(Castle castle : CastleManager.getInstance().getCastles()) {
               if (SiegeManager.getInstance().checkIsRegistered(clan, castle.getId())) {
                  player.sendPacket(SystemMessageId.CANNOT_DISSOLVE_WHILE_IN_SIEGE);
                  return;
               }
            }

            for(Fort fort : FortManager.getInstance().getForts()) {
               if (FortSiegeManager.getInstance().checkIsRegistered(clan, fort.getId())) {
                  player.sendPacket(SystemMessageId.CANNOT_DISSOLVE_WHILE_IN_SIEGE);
                  return;
               }
            }

            if (player.isInsideZone(ZoneId.SIEGE)) {
               player.sendPacket(SystemMessageId.CANNOT_DISSOLVE_WHILE_IN_SIEGE);
            } else if (clan.getDissolvingExpiryTime() > System.currentTimeMillis()) {
               player.sendPacket(SystemMessageId.DISSOLUTION_IN_PROGRESS);
            } else {
               clan.setDissolvingExpiryTime(System.currentTimeMillis() + (long)Config.ALT_CLAN_DISSOLVE_DAYS * 3600000L);
               clan.updateClanInDB();
               ClanHolder.getInstance().scheduleRemoveClan(clan.getId());
               player.deathPenalty(null, false, false, false);
            }
         } else {
            player.sendPacket(SystemMessageId.CANNOT_DISSOLVE_WHILE_OWNING_CLAN_HALL_OR_CASTLE);
         }
      }
   }

   private static final void recoverClan(Player player, int clanId) {
      if (!player.isClanLeader()) {
         player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
      } else {
         Clan clan = player.getClan();
         clan.setDissolvingExpiryTime(0L);
         clan.updateClanInDB();
      }
   }

   private static final void createSubPledge(Player player, String clanName, String leaderName, int pledgeType, int minClanLvl) {
      if (!player.isClanLeader()) {
         player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
      } else {
         Clan clan = player.getClan();
         if (clan.getLevel() < minClanLvl) {
            if (pledgeType == -1) {
               player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_CLAN_ACADEMY);
            } else {
               player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_MILITARY_UNIT);
            }
         } else if (Util.isAlphaNumeric(clanName) && isValidName(clanName) && 2 <= clanName.length()) {
            if (clanName.length() > 16) {
               player.sendPacket(SystemMessageId.CLAN_NAME_TOO_LONG);
            } else {
               for(Clan tempClan : ClanHolder.getInstance().getClans()) {
                  if (tempClan.getSubPledge(clanName) != null) {
                     if (pledgeType == -1) {
                        SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_EXISTS);
                        sm.addString(clanName);
                        player.sendPacket(sm);
                        Object var15 = null;
                     } else {
                        player.sendPacket(SystemMessageId.ANOTHER_MILITARY_UNIT_IS_ALREADY_USING_THAT_NAME);
                     }

                     return;
                  }
               }

               if (pledgeType == -1 || clan.getClanMember(leaderName) != null && clan.getClanMember(leaderName).getPledgeType() == 0) {
                  int leaderId = pledgeType != -1 ? clan.getClanMember(leaderName).getObjectId() : 0;
                  if (clan.createSubPledge(player, pledgeType, leaderId, clanName) != null) {
                     SystemMessage sm;
                     if (pledgeType == -1) {
                        sm = SystemMessage.getSystemMessage(SystemMessageId.THE_S1S_CLAN_ACADEMY_HAS_BEEN_CREATED);
                        sm.addString(player.getClan().getName());
                     } else if (pledgeType >= 1001) {
                        sm = SystemMessage.getSystemMessage(SystemMessageId.THE_KNIGHTS_OF_S1_HAVE_BEEN_CREATED);
                        sm.addString(player.getClan().getName());
                     } else if (pledgeType >= 100) {
                        sm = SystemMessage.getSystemMessage(SystemMessageId.THE_ROYAL_GUARD_OF_S1_HAVE_BEEN_CREATED);
                        sm.addString(player.getClan().getName());
                     } else {
                        sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_CREATED);
                     }

                     player.sendPacket(sm);
                     if (pledgeType != -1) {
                        ClanMember leaderSubPledge = clan.getClanMember(leaderName);
                        Player leaderPlayer = leaderSubPledge.getPlayerInstance();
                        if (leaderPlayer != null) {
                           leaderPlayer.setPledgeClass(ClanMember.calculatePledgeClass(leaderPlayer));
                           leaderPlayer.sendUserInfo();
                        }
                     }
                  }
               } else {
                  if (pledgeType >= 1001) {
                     player.sendPacket(SystemMessageId.CAPTAIN_OF_ORDER_OF_KNIGHTS_CANNOT_BE_APPOINTED);
                  } else if (pledgeType >= 100) {
                     player.sendPacket(SystemMessageId.CAPTAIN_OF_ROYAL_GUARD_CANNOT_BE_APPOINTED);
                  }
               }
            }
         } else {
            player.sendPacket(SystemMessageId.CLAN_NAME_INCORRECT);
         }
      }
   }

   private static final void renameSubPledge(Player player, int pledgeType, String pledgeName) {
      if (!player.isClanLeader()) {
         player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
      } else {
         Clan clan = player.getClan();
         Clan.SubPledge subPledge = player.getClan().getSubPledge(pledgeType);
         if (subPledge != null) {
            if (Util.isAlphaNumeric(pledgeName) && isValidName(pledgeName) && 2 <= pledgeName.length()) {
               if (pledgeName.length() > 16) {
                  player.sendPacket(SystemMessageId.CLAN_NAME_TOO_LONG);
               } else {
                  subPledge.setName(pledgeName);
                  clan.updateSubPledgeInDB(subPledge.getId());
                  clan.broadcastClanStatus();
                  player.sendMessage("Pledge name changed.");
               }
            } else {
               player.sendPacket(SystemMessageId.CLAN_NAME_INCORRECT);
            }
         }
      }
   }

   private static final void assignSubPledgeLeader(Player player, String clanName, String leaderName) {
      if (!player.isClanLeader()) {
         player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
      } else if (leaderName.length() > 16) {
         player.sendPacket(SystemMessageId.NAMING_CHARNAME_UP_TO_16CHARS);
      } else if (player.getName().equals(leaderName)) {
         player.sendPacket(SystemMessageId.CAPTAIN_OF_ROYAL_GUARD_CANNOT_BE_APPOINTED);
      } else {
         Clan clan = player.getClan();
         Clan.SubPledge subPledge = player.getClan().getSubPledge(clanName);
         if (null == subPledge || subPledge.getId() == -1) {
            player.sendPacket(SystemMessageId.CLAN_NAME_INCORRECT);
         } else if (clan.getClanMember(leaderName) != null && clan.getClanMember(leaderName).getPledgeType() == 0) {
            subPledge.setLeaderId(clan.getClanMember(leaderName).getObjectId());
            clan.updateSubPledgeInDB(subPledge.getId());
            ClanMember leaderSubPledge = clan.getClanMember(leaderName);
            Player leaderPlayer = leaderSubPledge.getPlayerInstance();
            if (leaderPlayer != null) {
               leaderPlayer.setPledgeClass(ClanMember.calculatePledgeClass(leaderPlayer));
               leaderPlayer.sendUserInfo();
            }

            clan.broadcastClanStatus();
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_BEEN_SELECTED_AS_CAPTAIN_OF_S2);
            sm.addString(leaderName);
            sm.addString(clanName);
            clan.broadcastToOnlineMembers(sm);
            SystemMessage var8 = null;
         } else {
            if (subPledge.getId() >= 1001) {
               player.sendPacket(SystemMessageId.CAPTAIN_OF_ORDER_OF_KNIGHTS_CANNOT_BE_APPOINTED);
            } else if (subPledge.getId() >= 100) {
               player.sendPacket(SystemMessageId.CAPTAIN_OF_ROYAL_GUARD_CANNOT_BE_APPOINTED);
            }
         }
      }
   }

   public static final void showPledgeSkillList(Player player) {
      if (!player.isClanLeader()) {
         NpcHtmlMessage html = new NpcHtmlMessage(1);
         html.setFile(player, player.getLang(), "data/html/villagemaster/NotClanLeader.htm");
         player.sendPacket(html);
         player.sendActionFailed();
      } else {
         List<SkillLearn> skills = SkillTreesParser.getInstance().getAvailablePledgeSkills(player.getClan());
         AcquireSkillList asl = new AcquireSkillList(AcquireSkillType.PLEDGE);
         int counts = 0;

         for(SkillLearn s : skills) {
            asl.addSkill(s.getId(), s.getGetLevel(), s.getLvl(), s.getLvl(), s.getLevelUpSp(), s.getSocialClass().ordinal());
            ++counts;
         }

         if (counts == 0) {
            if (player.getClan().getLevel() < 8) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN_S1);
               if (player.getClan().getLevel() < 5) {
                  sm.addNumber(5);
               } else {
                  sm.addNumber(player.getClan().getLevel() + 1);
               }

               player.sendPacket(sm);
            } else {
               NpcHtmlMessage html = new NpcHtmlMessage(1);
               html.setFile(player, player.getLang(), "data/html/villagemaster/NoMoreSkills.htm");
               player.sendPacket(html);
            }

            player.sendPacket(AcquireSkillDone.STATIC);
         } else {
            player.sendPacket(asl);
         }

         player.sendActionFailed();
      }
   }

   private static boolean isValidName(String name) {
      Pattern pattern;
      try {
         pattern = Pattern.compile(Config.CLAN_NAME_TEMPLATE);
      } catch (PatternSyntaxException var3) {
         _log.warning("ERROR: Wrong pattern for clan name!");
         pattern = Pattern.compile(".*");
      }

      return pattern.matcher(name).matches();
   }
}
