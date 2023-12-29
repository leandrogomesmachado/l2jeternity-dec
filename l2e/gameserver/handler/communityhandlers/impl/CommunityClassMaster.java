package l2e.gameserver.handler.communityhandlers.impl;

import java.util.StringTokenizer;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.ClassMasterParser;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.handler.communityhandlers.ICommunityBoardHandler;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.model.Augmentation;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.ClassMasterTemplate;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.items.PcItemTemplate;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.strings.server.ServerStorage;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class CommunityClassMaster extends AbstractCommunity implements ICommunityBoardHandler {
   public CommunityClassMaster() {
      if (Config.DEBUG) {
         _log.info(this.getClass().getSimpleName() + ": Loading all functions.");
      }
   }

   @Override
   public String[] getBypassCommands() {
      return new String[]{"_bbsclass"};
   }

   @Override
   public void onBypassCommand(String command, Player activeChar) {
      if (ClassMasterParser.getInstance().isAllowCommunityClassMaster()) {
         ClassId classId = activeChar.getClassId();
         int jobLevel = classId.level();
         int level = activeChar.getLevel();
         ClassMasterTemplate tpl1 = ClassMasterParser.getInstance().getClassTemplate(1);
         ClassMasterTemplate tpl2 = ClassMasterParser.getInstance().getClassTemplate(2);
         ClassMasterTemplate tpl3 = ClassMasterParser.getInstance().getClassTemplate(3);
         StringBuilder html = new StringBuilder("");
         html.append("<br>");
         html.append("<center>");
         if (level >= 20 && jobLevel == 0 && tpl1 != null && tpl1.isAllowedChangeClass()
            || level >= 40 && jobLevel == 1 && tpl2 != null && tpl2.isAllowedChangeClass()
            || level >= 76 && jobLevel == 2 && tpl3 != null && tpl3.isAllowedChangeClass()) {
            ClassMasterTemplate tpl = ClassMasterParser.getInstance().getClassTemplate(jobLevel + 1);
            if (tpl.getRequestItems() != null && !tpl.getRequestItems().isEmpty()) {
               html.append("" + ServerStorage.getInstance().getString(activeChar.getLang(), "ClassBBS.MUST_PAY") + " <br>");

               for(int itemId : tpl.getRequestItems().keySet()) {
                  long count = tpl.getRequestItems().get(itemId);
                  html.append(
                     "<center><font color=\"LEVEL\">"
                        + count
                        + "</font> "
                        + activeChar.getItemName(ItemsParser.getInstance().getTemplate(itemId))
                        + "</center><br>"
                  );
               }
            }

            for(ClassId cid : ClassId.values()) {
               if (cid != ClassId.inspector && cid.childOf(classId) && cid.level() == classId.level() + 1) {
                  html.append("<br><center><button value=\"")
                     .append(Util.className(activeChar, cid.getId()))
                     .append("\" action=\"bypass _bbsclass;change_class;")
                     .append(cid.getId())
                     .append(";")
                     .append(jobLevel + 1)
                     .append("\" width=200 height=31 back=\"L2UI_CT1.OlympiadWnd_DF_Apply_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Apply\"></center>");
               }
            }

            html.append("</center>");
         } else {
            switch(jobLevel) {
               case 0:
                  html.append(
                     ""
                        + ServerStorage.getInstance().getString(activeChar.getLang(), "ClassBBS.WELCOME")
                        + " "
                        + activeChar.getName()
                        + "! "
                        + ServerStorage.getInstance().getString(activeChar.getLang(), "ClassBBS.CURRENT_PROF")
                        + " <font color=F2C202>"
                        + Util.className(activeChar, activeChar.getClassId().getId())
                        + "</font>.<br>"
                  );
                  if (tpl1 != null && tpl1.isAllowedChangeClass()) {
                     html.append(
                        ""
                           + ServerStorage.getInstance().getString(activeChar.getLang(), "ClassBBS.FOR_CHANGE_PROF")
                           + " <font color=F2C202>"
                           + ServerStorage.getInstance().getString(activeChar.getLang(), "ClassBBS.20_LVL")
                           + "</font><br>"
                     );
                  } else if (tpl2 != null && tpl2.isAllowedChangeClass()) {
                     html.append(
                        ""
                           + ServerStorage.getInstance().getString(activeChar.getLang(), "ClassBBS.FOR_CHANGE_PROF")
                           + " <font color=F2C202>"
                           + ServerStorage.getInstance().getString(activeChar.getLang(), "ClassBBS.40_LVL_1")
                           + "</font><br>"
                     );
                  } else if (tpl3 != null && tpl3.isAllowedChangeClass()) {
                     html.append(
                        ""
                           + ServerStorage.getInstance().getString(activeChar.getLang(), "ClassBBS.FOR_CHANGE_PROF")
                           + " <font color=F2C202>"
                           + ServerStorage.getInstance().getString(activeChar.getLang(), "ClassBBS.76_LVL_2")
                           + "</font><br>"
                     );
                  } else {
                     html.append("" + ServerStorage.getInstance().getString(activeChar.getLang(), "ClassBBS.NO_CHANGE_PROF") + "<br>");
                  }

                  html.append(
                     ""
                        + ServerStorage.getInstance().getString(activeChar.getLang(), "ClassBBS.SUBCLASS_CHANGE")
                        + " <font color=F2C202>"
                        + ServerStorage.getInstance().getString(activeChar.getLang(), "ClassBBS.76_LVL")
                        + ".</font><br>"
                  );
                  html.append(
                     ""
                        + ServerStorage.getInstance().getString(activeChar.getLang(), "ClassBBS.NOOBLESS_CHANGE")
                        + " <font color=F2C202>"
                        + ServerStorage.getInstance().getString(activeChar.getLang(), "ClassBBS.76_LVL")
                        + ".</font><br>"
                  );
                  break;
               case 1:
                  html.append(
                     ""
                        + ServerStorage.getInstance().getString(activeChar.getLang(), "ClassBBS.WELCOME")
                        + " "
                        + activeChar.getName()
                        + "! "
                        + ServerStorage.getInstance().getString(activeChar.getLang(), "ClassBBS.CURRENT_PROF")
                        + " <font color=F2C202>"
                        + Util.className(activeChar, activeChar.getClassId().getId())
                        + "</font>.<br>"
                  );
                  if (tpl2 != null && tpl2.isAllowedChangeClass()) {
                     html.append(
                        ""
                           + ServerStorage.getInstance().getString(activeChar.getLang(), "ClassBBS.FOR_CHANGE_PROF")
                           + " <font color=F2C202>"
                           + ServerStorage.getInstance().getString(activeChar.getLang(), "ClassBBS.40_LVL")
                           + "</font><br>"
                     );
                  } else if (tpl3 != null && tpl3.isAllowedChangeClass()) {
                     html.append(
                        ""
                           + ServerStorage.getInstance().getString(activeChar.getLang(), "ClassBBS.FOR_CHANGE_PROF")
                           + " <font color=F2C202>"
                           + ServerStorage.getInstance().getString(activeChar.getLang(), "ClassBBS.76_LVL_2")
                           + "</font><br>"
                     );
                  } else {
                     html.append("" + ServerStorage.getInstance().getString(activeChar.getLang(), "ClassBBS.NO_CHANGE_PROF") + "<br>");
                  }

                  html.append(
                     ""
                        + ServerStorage.getInstance().getString(activeChar.getLang(), "ClassBBS.SUBCLASS_CHANGE")
                        + " <font color=F2C202>"
                        + ServerStorage.getInstance().getString(activeChar.getLang(), "ClassBBS.76_LVL")
                        + ".</font><br>"
                  );
                  html.append(
                     ""
                        + ServerStorage.getInstance().getString(activeChar.getLang(), "ClassBBS.NOOBLESS_CHANGE")
                        + " <font color=F2C202>"
                        + ServerStorage.getInstance().getString(activeChar.getLang(), "ClassBBS.76_LVL")
                        + ".</font><br>"
                  );
                  break;
               case 2:
                  html.append(
                     ""
                        + ServerStorage.getInstance().getString(activeChar.getLang(), "ClassBBS.WELCOME")
                        + " "
                        + activeChar.getName()
                        + "! "
                        + ServerStorage.getInstance().getString(activeChar.getLang(), "ClassBBS.CURRENT_PROF")
                        + " <font color=F2C202>"
                        + Util.className(activeChar, activeChar.getClassId().getId())
                        + "</font>.<br>"
                  );
                  if (tpl3 != null && tpl3.isAllowedChangeClass()) {
                     html.append(
                        ""
                           + ServerStorage.getInstance().getString(activeChar.getLang(), "ClassBBS.FOR_CHANGE_PROF")
                           + " <font color=F2C202>"
                           + ServerStorage.getInstance().getString(activeChar.getLang(), "ClassBBS.76_LVL")
                           + ".</font><br>"
                     );
                     html.append(
                        ""
                           + ServerStorage.getInstance().getString(activeChar.getLang(), "ClassBBS.SUBCLASS_CHANGE")
                           + " <font color=F2C202>"
                           + ServerStorage.getInstance().getString(activeChar.getLang(), "ClassBBS.76_LVL")
                           + ".</font><br>"
                     );
                     html.append(
                        ""
                           + ServerStorage.getInstance().getString(activeChar.getLang(), "ClassBBS.NOOBLESS_CHANGE")
                           + " <font color=F2C202>"
                           + ServerStorage.getInstance().getString(activeChar.getLang(), "ClassBBS.76_LVL")
                           + ".</font><br>"
                     );
                  } else {
                     html.append("" + ServerStorage.getInstance().getString(activeChar.getLang(), "ClassBBS.NO_CHANGE_PROF") + "<br>");
                  }
                  break;
               case 3:
                  html.append(
                     ""
                        + ServerStorage.getInstance().getString(activeChar.getLang(), "ClassBBS.WELCOME")
                        + " "
                        + activeChar.getName()
                        + "! "
                        + ServerStorage.getInstance().getString(activeChar.getLang(), "ClassBBS.CURRENT_PROF")
                        + " <font color=F2C202>"
                        + Util.className(activeChar, activeChar.getClassId().getId())
                        + "</font>.<br>"
                  );
                  html.append("" + ServerStorage.getInstance().getString(activeChar.getLang(), "ClassBBS.NO_CHANGE_PROF") + "<br>");
                  if (level >= 76) {
                     html.append(
                        ""
                           + ServerStorage.getInstance().getString(activeChar.getLang(), "ClassBBS.YOU_REACH")
                           + " <font color=F2C202>"
                           + ServerStorage.getInstance().getString(activeChar.getLang(), "ClassBBS.76_LVL")
                           + "</font>! "
                           + ServerStorage.getInstance().getString(activeChar.getLang(), "ClassBBS.ACTIVE_SUBCLASS")
                           + "<br>"
                     );
                  }
            }
         }

         NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
         adminReply.setFile(activeChar, activeChar.getLang(), "data/html/community/classmaster.htm");
         adminReply.replace("%classmaster%", html.toString());
         separateAndSend(adminReply.getHtm(), activeChar);
         if (command.startsWith("_bbsclass;change_class;")) {
            StringTokenizer st = new StringTokenizer(command, ";");
            st.nextToken();
            st.nextToken();
            short val = Short.parseShort(st.nextToken());
            int id = Integer.parseInt(st.nextToken());
            ClassMasterTemplate tpl = ClassMasterParser.getInstance().getClassTemplate(id);
            if (tpl == null) {
               this.onBypassCommand("_bbsclass;", activeChar);
               return;
            }

            if (!validateClassId(activeChar.getClassId(), val)) {
               this.onBypassCommand("_bbsclass;", activeChar);
               return;
            }

            if (!tpl.getRequestItems().isEmpty()) {
               for(int itemId : tpl.getRequestItems().keySet()) {
                  long count = tpl.getRequestItems().get(itemId);
                  if (activeChar.getInventory().getInventoryItemCount(itemId, -1) < count) {
                     activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
                     this.onBypassCommand("_bbsclass;", activeChar);
                     return;
                  }
               }

               for(int itemId : tpl.getRequestItems().keySet()) {
                  long count = tpl.getRequestItems().get(itemId);
                  if (!activeChar.destroyItemByItemId("ClassMaster", itemId, count, activeChar, true)) {
                     this.onBypassCommand("_bbsclass;", activeChar);
                     return;
                  }
               }
            }

            this.changeClass(activeChar, val, tpl);
            this.onBypassCommand("_bbsclass;", activeChar);
         }
      }
   }

   private void changeClass(Player activeChar, short val, ClassMasterTemplate tpl) {
      if (activeChar.getClassId().level() != ClassId.values()[val].level()) {
         if (activeChar.getClassId().level() == 3) {
            activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THIRD_CLASS_TRANSFER));
         } else {
            activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLASS_TRANSFER));
         }

         activeChar.setClassId(val);
         if (activeChar.isSubClassActive()) {
            activeChar.getSubClasses().get(activeChar.getClassIndex()).setClassId(activeChar.getActiveClass());
         } else {
            if (!tpl.getRewardItems().isEmpty()) {
               for(int itemId : tpl.getRewardItems().keySet()) {
                  long count = tpl.getRewardItems().get(itemId);
                  activeChar.addItem("ClassMaster", itemId, count, activeChar, true);
               }
            }

            activeChar.setBaseClass(activeChar.getActiveClass());
            if (activeChar.getTemplate().hasInitialEquipment()) {
               for(PcItemTemplate ie : activeChar.getTemplate().getInitialEquipment()) {
                  ItemInstance item = new ItemInstance(IdFactory.getInstance().getNextId(), ie.getId());
                  if (ie.getEnchant() != 0) {
                     item.setEnchantLevel(ie.getEnchant());
                  }

                  if (ie.getAugmentId() != -1) {
                     item.setAugmentation(new Augmentation(ie.getAugmentId()));
                  }

                  if (ie.getElementals() != null && !ie.getElementals().isEmpty()) {
                     String[] elements = ie.getElementals().split(";");

                     for(String el : elements) {
                        String[] element = el.split(":");
                        if (element != null) {
                           item.setElementAttr(Byte.parseByte(element[0]), Integer.parseInt(element[1]));
                        }
                     }
                  }

                  item.setCount(ie.getCount());
                  if (ie.getDurability() > 0) {
                     item.setMana(ie.getDurability());
                  }

                  activeChar.getInventory().addItem("Code Item", item, activeChar, null);
                  if (item.isEquipable() && ie.isEquipped()) {
                     activeChar.getInventory().equipItem(item);
                  }
               }
            }
         }

         activeChar.broadcastUserInfo(true);
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

   @Override
   public void onWriteCommand(String command, String ar1, String ar2, String ar3, String ar4, String ar5, Player activeChar) {
   }

   public static CommunityClassMaster getInstance() {
      return CommunityClassMaster.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final CommunityClassMaster _instance = new CommunityClassMaster();
   }
}
