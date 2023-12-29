package l2e.gameserver.model.actor.instance;

import l2e.commons.util.StringUtil;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.data.parser.ClassMasterParser;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.model.ClanMember;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.ClassMasterTemplate;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.olympiad.Olympiad;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.ShowTutorialMark;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.network.serverpackets.TutorialCloseHtml;
import l2e.gameserver.network.serverpackets.TutorialShowHtml;

public final class ClassMasterInstance extends MerchantInstance {
   public ClassMasterInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.ClassMasterInstance);
   }

   @Override
   public String getHtmlPath(int npcId, int val) {
      String pom = "";
      if (val == 0) {
         pom = "" + npcId;
      } else {
         pom = npcId + "-" + val;
      }

      return "data/html/classmaster/" + pom + ".htm";
   }

   @Override
   public void onBypassFeedback(Player player, String command) {
      if (command.startsWith("1stClass")) {
         showHtmlMenu(player, this.getObjectId(), 1);
      } else if (command.startsWith("2ndClass")) {
         showHtmlMenu(player, this.getObjectId(), 2);
      } else if (command.startsWith("3rdClass")) {
         showHtmlMenu(player, this.getObjectId(), 3);
      } else if (command.startsWith("change_class")) {
         int val = Integer.parseInt(command.substring(13));
         if (checkAndChangeClass(player, val)) {
            NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
            html.setFile(player, player.getLang(), "data/html/classmaster/ok.htm");
            html.replace("%name%", Util.className(player, val));
            player.sendPacket(html);
         }
      } else if (command.startsWith("become_noble")) {
         if (!player.isNoble()) {
            if (player.getInventory().getItemByItemId(Config.SERVICES_GIVENOOBLESS_ITEM[0]) == null) {
               player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            if (player.getInventory().getItemByItemId(Config.SERVICES_GIVENOOBLESS_ITEM[0]).getCount() < (long)Config.SERVICES_GIVENOOBLESS_ITEM[1]) {
               player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            player.destroyItemByItemId("ShopBBS", Config.SERVICES_GIVENOOBLESS_ITEM[0], (long)Config.SERVICES_GIVENOOBLESS_ITEM[1], player, true);
            Olympiad.addNoble(player);
            player.setNoble(true);
            if (player.getClan() != null) {
               player.setPledgeClass(ClanMember.calculatePledgeClass(player));
            } else {
               player.setPledgeClass(5);
            }

            player.sendUserInfo();
            NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
            html.setFile(player, player.getLang(), "data/html/classmaster/nobleok.htm");
            player.sendPacket(html);
         }
      } else if (command.startsWith("learn_skills")) {
         player.giveAvailableSkills(Config.AUTO_LEARN_FS_SKILLS, true);
      } else if (command.startsWith("increase_clan_level")) {
         if (!player.isClanLeader()) {
            NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
            html.setFile(player, player.getLang(), "data/html/classmaster/noclanleader.htm");
            player.sendPacket(html);
         } else if (player.getClan().getLevel() >= 5) {
            NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
            html.setFile(player, player.getLang(), "data/html/classmaster/noclanlevel.htm");
            player.sendPacket(html);
         } else {
            player.getClan().changeLevel(5, true);
         }
      } else {
         super.onBypassFeedback(player, command);
      }
   }

   public static final void onTutorialLink(Player player, String request) {
      if (Config.ALTERNATE_CLASS_MASTER && request != null && request.startsWith("CO")) {
         try {
            int val = Integer.parseInt(request.substring(2));
            checkAndChangeClass(player, val);
         } catch (NumberFormatException var3) {
         }

         player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
      }
   }

   public static final void onTutorialQuestionMark(Player player, int number) {
      if (Config.ALTERNATE_CLASS_MASTER && number == 1001) {
         showTutorialHtml(player);
      }
   }

   public static final void showQuestionMark(Player player) {
      if (Config.ALTERNATE_CLASS_MASTER) {
         ClassId classId = player.getClassId();
         if (getMinLevel(classId.level()) <= player.getLevel()) {
            if (!ClassMasterParser.getInstance().isAllowClassMaster() || ClassMasterParser.getInstance().isAllowedClassChange(classId.level() + 1)) {
               player.sendPacket(new ShowTutorialMark(false, 1001));
            }
         }
      }
   }

   private static final void showHtmlMenu(Player player, int objectId, int level) {
      NpcHtmlMessage html = new NpcHtmlMessage(objectId);
      if (!ClassMasterParser.getInstance().isAllowClassMaster()) {
         html.setFile(player, player.getLang(), "data/html/classmaster/disabled.htm");
         player.sendPacket(html);
      } else {
         ClassMasterTemplate tpl = ClassMasterParser.getInstance().getClassTemplate(level);
         if (tpl == null) {
            html.setFile(player, player.getLang(), "data/html/classmaster/disabled.htm");
            player.sendPacket(html);
         } else {
            if (!tpl.isAllowedChangeClass()) {
               int jobLevel = player.getClassId().level();
               StringBuilder sb = new StringBuilder(100);
               ClassMasterTemplate tpl1 = ClassMasterParser.getInstance().getClassTemplate(1);
               ClassMasterTemplate tpl2 = ClassMasterParser.getInstance().getClassTemplate(2);
               ClassMasterTemplate tpl3 = ClassMasterParser.getInstance().getClassTemplate(3);
               sb.append("<html><body>");
               switch(jobLevel) {
                  case 0:
                     if (tpl1 != null && tpl1.isAllowedChangeClass()) {
                        sb.append("Come back here when you reached level 20 to change your class.<br>");
                     } else if (tpl2 != null && tpl2.isAllowedChangeClass()) {
                        sb.append("Come back after your first occupation change.<br>");
                     } else {
                        if (tpl3 != null && tpl3.isAllowedChangeClass()) {
                           sb.append("Come back after your second occupation change.<br>");
                           break;
                        }

                        sb.append("I can't change your occupation.<br>");
                     }
                     break;
                  case 1:
                     if (tpl2 != null && tpl2.isAllowedChangeClass()) {
                        sb.append("Come back here when you reached level 40 to change your class.<br>");
                     } else {
                        if (tpl3 != null && tpl3.isAllowedChangeClass()) {
                           sb.append("Come back after your second occupation change.<br>");
                           break;
                        }

                        sb.append("I can't change your occupation.<br>");
                     }
                     break;
                  case 2:
                     if (tpl3 != null && tpl3.isAllowedChangeClass()) {
                        sb.append("Come back here when you reached level 76 to change your class.<br>");
                        break;
                     }

                     sb.append("I can't change your occupation.<br>");
                     break;
                  case 3:
                     sb.append("There is no class change available for you anymore.<br>");
               }

               sb.append("</body></html>");
               html.setHtml(player, sb.toString());
            } else {
               ClassId currentClassId = player.getClassId();
               if (currentClassId.level() >= level) {
                  html.setFile(player, player.getLang(), "data/html/classmaster/nomore.htm");
               } else {
                  int minLevel = getMinLevel(currentClassId.level());
                  if (player.getLevel() >= minLevel || Config.ALLOW_ENTIRE_TREE) {
                     StringBuilder menu = new StringBuilder(100);

                     for(ClassId cid : ClassId.values()) {
                        if ((cid != ClassId.inspector || player.getTotalSubClasses() >= 2) && validateClassId(currentClassId, cid) && cid.level() == level) {
                           StringUtil.append(
                              menu,
                              "<tr><td width=280 align=center><button value = \""
                                 + Util.className(player, cid.getId())
                                 + "\" action=\"bypass -h npc_%objectId%_change_class ",
                              String.valueOf(cid.getId()),
                              "\" back=\"l2ui_ct1.button.OlympiadWnd_DF_Back_Down\" width=200 height=30 fore=\"l2ui_ct1.button.OlympiadWnd_DF_Back\"></td></tr>"
                           );
                        }
                     }

                     if (menu.length() > 0) {
                        html.setFile(player, player.getLang(), "data/html/classmaster/template.htm");
                        html.replace("%name%", Util.className(player, currentClassId.getId()));
                        html.replace("%menu%", menu.toString());
                     } else {
                        html.setFile(player, player.getLang(), "data/html/classmaster/comebacklater.htm");
                        html.replace("%level%", String.valueOf(getMinLevel(level - 1)));
                     }
                  } else if (minLevel < Integer.MAX_VALUE) {
                     html.setFile(player, player.getLang(), "data/html/classmaster/comebacklater.htm");
                     html.replace("%level%", String.valueOf(minLevel));
                  } else {
                     html.setFile(player, player.getLang(), "data/html/classmaster/nomore.htm");
                  }
               }
            }

            html.replace("%objectId%", String.valueOf(objectId));
            html.replace("%req_items%", getRequiredItems(player, level));
            player.sendPacket(html);
         }
      }
   }

   private static final void showTutorialHtml(Player player) {
      ClassId currentClassId = player.getClassId();
      if (getMinLevel(currentClassId.level()) <= player.getLevel() || Config.ALLOW_ENTIRE_TREE) {
         String msg = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/classmaster/tutorialtemplate.htm");
         msg = msg.replaceAll("%name%", Util.className(player, currentClassId.getId()));
         StringBuilder menu = new StringBuilder(100);

         for(ClassId cid : ClassId.values()) {
            if ((cid != ClassId.inspector || player.getTotalSubClasses() >= 2) && validateClassId(currentClassId, cid)) {
               StringUtil.append(
                  menu,
                  "<tr><td width=280 align=center><button value = \""
                     + Util.className(player, cid.getId())
                     + "\" action=\"link CO"
                     + cid.getId()
                     + "\" back=\"l2ui_ct1.button.OlympiadWnd_DF_Back_Down\" width=200 height=30 fore=\"l2ui_ct1.button.OlympiadWnd_DF_Back\"></td></tr>"
               );
            }
         }

         msg = msg.replaceAll("%menu%", menu.toString());
         msg = msg.replace("%req_items%", getRequiredItems(player, currentClassId.level() + 1));
         player.sendPacket(new TutorialShowHtml(msg));
      }
   }

   private static final boolean checkAndChangeClass(Player player, int val) {
      ClassId currentClassId = player.getClassId();
      if (getMinLevel(currentClassId.level()) > player.getLevel() && !Config.ALLOW_ENTIRE_TREE) {
         return false;
      } else if (!validateClassId(currentClassId, val)) {
         return false;
      } else {
         int newJobLevel = currentClassId.level() + 1;
         ClassMasterTemplate tpl = ClassMasterParser.getInstance().getClassTemplate(newJobLevel);
         if (tpl == null) {
            return false;
         } else if (!tpl.getRewardItems().isEmpty() && !player.isInventoryUnder90(false)) {
            player.sendPacket(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT);
            return false;
         } else {
            if (!tpl.getRequestItems().isEmpty()) {
               for(int itemId : tpl.getRequestItems().keySet()) {
                  long count = tpl.getRequestItems().get(itemId);
                  if (player.getInventory().getInventoryItemCount(itemId, -1) < count) {
                     player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
                     return false;
                  }
               }

               for(int itemId : tpl.getRequestItems().keySet()) {
                  long count = tpl.getRequestItems().get(itemId);
                  if (!player.destroyItemByItemId("ClassMaster", itemId, count, player, true)) {
                     return false;
                  }
               }
            }

            if (!tpl.getRewardItems().isEmpty()) {
               for(int itemId : tpl.getRewardItems().keySet()) {
                  long count = tpl.getRewardItems().get(itemId);
                  player.addItem("ClassMaster", itemId, count, player, true);
               }
            }

            player.setClassId(val);
            if (player.isSubClassActive()) {
               player.getSubClasses().get(player.getClassIndex()).setClassId(player.getActiveClass());
            } else {
               player.setBaseClass(player.getActiveClass());
            }

            player.broadcastUserInfo(true);
            tpl = ClassMasterParser.getInstance().getClassTemplate(player.getClassId().level() + 1);
            if (tpl != null
               && tpl.isAllowedChangeClass()
               && Config.ALTERNATE_CLASS_MASTER
               && (player.getClassId().level() == 1 && player.getLevel() >= 40 || player.getClassId().level() == 2 && player.getLevel() >= 76)) {
               showQuestionMark(player);
            }

            return true;
         }
      }
   }

   private static final int getMinLevel(int level) {
      switch(level) {
         case 0:
            return 20;
         case 1:
            return 40;
         case 2:
            return 76;
         default:
            return Integer.MAX_VALUE;
      }
   }

   private static final boolean validateClassId(ClassId oldCID, int val) {
      try {
         return validateClassId(oldCID, ClassId.getClassId(val));
      } catch (Exception var3) {
         return false;
      }
   }

   private static final boolean validateClassId(ClassId oldCID, ClassId newCID) {
      if (newCID == null || newCID.getRace() == null) {
         return false;
      } else if (oldCID.equals(newCID.getParent())) {
         return true;
      } else {
         return Config.ALLOW_ENTIRE_TREE && newCID.childOf(oldCID);
      }
   }

   private static String getRequiredItems(Player player, int level) {
      ClassMasterTemplate tpl = ClassMasterParser.getInstance().getClassTemplate(level);
      if (tpl == null) {
         return "";
      } else if (tpl.getRequestItems() != null && !tpl.getRequestItems().isEmpty()) {
         StringBuilder sb = new StringBuilder();

         for(int itemId : tpl.getRequestItems().keySet()) {
            long count = tpl.getRequestItems().get(itemId);
            sb.append(
               "<tr><td width=280 align=center><font color=\"LEVEL\">"
                  + count
                  + "</font> "
                  + player.getItemName(ItemsParser.getInstance().getTemplate(itemId))
                  + "</td></tr>"
            );
         }

         return sb.toString();
      } else {
         return "<tr><td>none</td></tr>";
      }
   }
}
