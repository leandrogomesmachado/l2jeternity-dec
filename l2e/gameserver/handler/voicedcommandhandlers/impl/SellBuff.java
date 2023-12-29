package l2e.gameserver.handler.voicedcommandhandlers.impl;

import java.util.StringTokenizer;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.handler.bypasshandlers.IBypassHandler;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.entity.mods.SellBuffsManager;
import l2e.gameserver.model.holders.SellBuffHolder;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.strings.server.ServerMessage;

public class SellBuff implements IVoicedCommandHandler, IBypassHandler {
   private static final String[] VOICED_COMMANDS = new String[]{"sellbuff", "sellbuffs"};
   private static final String[] BYPASS_COMMANDS = new String[]{
      "sellbuffadd",
      "sellbuffaddskill",
      "sellbuffedit",
      "sellbuffchangeprice",
      "sellbuffremove",
      "sellbuffbuymenu",
      "sellbuffbuyskill",
      "sellbuffstart",
      "sellbuffstop"
   };

   @Override
   public boolean useBypass(String command, Player activeChar, Creature target) {
      if (!Config.ALLOW_SELLBUFFS_COMMAND) {
         return false;
      } else {
         String cmd = "";
         String params = "";
         StringTokenizer st = new StringTokenizer(command, " ");
         if (st.hasMoreTokens()) {
            cmd = st.nextToken();
         }

         while(st.hasMoreTokens()) {
            params = params + st.nextToken() + (st.hasMoreTokens() ? " " : "");
         }

         return cmd.isEmpty() ? false : this.useBypass(cmd, activeChar, params);
      }
   }

   @Override
   public boolean useVoicedCommand(String command, Player activeChar, String params) {
      if (!Config.ALLOW_SELLBUFFS_COMMAND) {
         return false;
      } else {
         switch(command) {
            case "sellbuff":
            case "sellbuffs":
               SellBuffsManager.sendSellMenu(activeChar);
            default:
               return true;
         }
      }
   }

   public boolean useBypass(String command, Player activeChar, String params) {
      switch(command) {
         case "sellbuffstart":
            if (activeChar.isSellingBuffs() || params == null || params.isEmpty()) {
               return false;
            }

            if (activeChar.getSellingBuffs().isEmpty()) {
               activeChar.sendMessage(new ServerMessage("SellBuff.EMPTY_LIST", activeChar.getLang()).toString());
               return false;
            }

            if (!activeChar.canOpenPrivateStore(true)) {
               return false;
            }

            String title = "BUFF SELL: ";
            StringTokenizer st = new StringTokenizer(params, " ");

            while(st.hasMoreTokens()) {
               title = title + st.nextToken() + " ";
            }

            if (title.length() > 40) {
               activeChar.sendMessage(new ServerMessage("SellBuff.WRONG_TITLE", activeChar.getLang()).toString());
               return false;
            }

            SellBuffsManager.startSellBuffs(activeChar, title);
            break;
         case "sellbuffstop":
            if (activeChar.isSellingBuffs()) {
               SellBuffsManager.stopSellBuffs(activeChar);
            }
            break;
         case "sellbuffadd":
            if (!activeChar.isSellingBuffs()) {
               int page = 1;
               if (params != null && !params.isEmpty() && Util.isDigit(params)) {
                  page = Integer.parseInt(params);
               }

               SellBuffsManager.sendBuffChoiceMenu(activeChar, page);
            }
            break;
         case "sellbuffedit":
            if (!activeChar.isSellingBuffs()) {
               StringTokenizer st = new StringTokenizer(params, " ");
               int page = 1;
               if (st.hasMoreTokens()) {
                  page = Integer.parseInt(st.nextToken());
               }

               SellBuffsManager.sendBuffEditMenu(activeChar, page);
            }
            break;
         case "sellbuffchangeprice":
            if (!activeChar.isSellingBuffs() && params != null && !params.isEmpty()) {
               StringTokenizer st = new StringTokenizer(params, " ");
               int skillId = -1;
               int price = -1;
               String currency = null;
               int page = 1;
               if (st.hasMoreTokens()) {
                  skillId = Integer.parseInt(st.nextToken());
               }

               if (st.hasMoreTokens()) {
                  try {
                     price = Integer.parseInt(st.nextToken());
                  } catch (NumberFormatException var21) {
                     ServerMessage msg = new ServerMessage("SellBuff.BIG_PRICE", activeChar.getLang());
                     msg.add(Config.SELLBUFF_MAX_PRICE);
                     activeChar.sendMessage(msg.toString());
                     SellBuffsManager.sendBuffEditMenu(activeChar, page);
                  }
               }

               if (st.hasMoreTokens()) {
                  try {
                     currency = st.nextToken();
                  } catch (Exception var20) {
                     SellBuffsManager.sendBuffEditMenu(activeChar, page);
                  }
               }

               if (st.hasMoreTokens()) {
                  page = Integer.parseInt(st.nextToken());
               }

               if (skillId == -1 || price == -1) {
                  return false;
               }

               Skill skillToChange = activeChar.getKnownSkill(skillId);
               if (skillToChange == null || currency == null) {
                  return false;
               }

               for(SellBuffHolder holder : activeChar.getSellingBuffs()) {
                  if (holder != null && holder.getId() == skillToChange.getId()) {
                     int itemId = 0;

                     for(String itemName : Config.SELLBUFF_CURRECY_LIST.keySet()) {
                        if (itemName.equals(currency)) {
                           itemId = Config.SELLBUFF_CURRECY_LIST.get(itemName);
                           break;
                        }
                     }

                     if (itemId == 0) {
                        activeChar.sendMessage(new ServerMessage("SellBuff.WRONG_CURRENCY", activeChar.getLang()).toString());
                        SellBuffsManager.sendBuffEditMenu(activeChar, page);
                        return false;
                     }

                     ServerMessage msg = new ServerMessage("SellBuff.CHANGE_PRICE", activeChar.getLang());
                     msg.add(activeChar.getSkillName(activeChar.getKnownSkill(holder.getId())));
                     msg.add(price);
                     activeChar.sendMessage(msg.toString());
                     holder.setItemId(itemId);
                     holder.setPrice((long)price);
                     SellBuffsManager.sendBuffEditMenu(activeChar, page);
                  }
               }
            }
            break;
         case "sellbuffremove":
            if (!activeChar.isSellingBuffs() && params != null && !params.isEmpty()) {
               StringTokenizer st = new StringTokenizer(params, " ");
               int page = 1;
               int skillId = -1;
               if (st.hasMoreTokens()) {
                  try {
                     skillId = Integer.parseInt(st.nextToken());
                  } catch (NumberFormatException var19) {
                     activeChar.sendMessage(new ServerMessage("SellBuff.WRONG_SKILL", activeChar.getLang()).toString());
                     SellBuffsManager.sendBuffEditMenu(activeChar, page);
                  }
               }

               if (st.hasMoreTokens()) {
                  page = Integer.parseInt(st.nextToken());
               }

               if (skillId == -1) {
                  return false;
               }

               Skill skillToRemove = activeChar.getKnownSkill(skillId);
               if (skillToRemove == null) {
                  return false;
               }

               SellBuffHolder correctSkill = null;

               for(SellBuffHolder holder : activeChar.getSellingBuffs()) {
                  if (holder != null && holder.getId() == skillToRemove.getId()) {
                     correctSkill = holder;
                  }
               }

               if (correctSkill != null) {
                  activeChar.getSellingBuffs().remove(correctSkill);
                  ServerMessage msg = new ServerMessage("SellBuff.SKILL_REMOVED", activeChar.getLang());
                  msg.add(activeChar.getSkillName(activeChar.getKnownSkill(correctSkill.getId())));
                  activeChar.sendMessage(msg.toString());
                  SellBuffsManager.sendBuffEditMenu(activeChar, page);
               }
            }
            break;
         case "sellbuffaddskill":
            if (!activeChar.isSellingBuffs() && params != null && !params.isEmpty()) {
               StringTokenizer st = new StringTokenizer(params, " ");
               int skillId = -1;
               long price = -1L;
               String currency = null;
               int page = 1;
               if (st.hasMoreTokens()) {
                  skillId = Integer.parseInt(st.nextToken());
               }

               if (st.hasMoreTokens()) {
                  try {
                     price = (long)Integer.parseInt(st.nextToken());
                  } catch (NumberFormatException var18) {
                     ServerMessage msg = new ServerMessage("SellBuff.BIG_PRICE", activeChar.getLang());
                     msg.add(Config.SELLBUFF_MAX_PRICE);
                     activeChar.sendMessage(msg.toString());
                     SellBuffsManager.sendBuffChoiceMenu(activeChar, page);
                  }
               }

               if (st.hasMoreTokens()) {
                  try {
                     currency = st.nextToken();
                  } catch (Exception var17) {
                     SellBuffsManager.sendBuffChoiceMenu(activeChar, page);
                  }
               }

               if (st.hasMoreTokens()) {
                  page = Integer.parseInt(st.nextToken());
               }

               if (skillId == -1 || price == -1L) {
                  return false;
               }

               Skill skillToAdd = activeChar.getKnownSkill(skillId);
               if (skillToAdd == null || currency == null) {
                  return false;
               }

               if (price < (long)Config.SELLBUFF_MIN_PRICE) {
                  ServerMessage msg = new ServerMessage("SellBuff.SMALL_PRICE", activeChar.getLang());
                  msg.add(Config.SELLBUFF_MIN_PRICE);
                  activeChar.sendMessage(msg.toString());
                  return false;
               }

               if (price > (long)Config.SELLBUFF_MAX_PRICE) {
                  ServerMessage msg = new ServerMessage("SellBuff.BIG_PRICE", activeChar.getLang());
                  msg.add(Config.SELLBUFF_MAX_PRICE);
                  activeChar.sendMessage(msg.toString());
                  return false;
               }

               if (activeChar.getSellingBuffs().size() >= Config.SELLBUFF_MAX_BUFFS) {
                  ServerMessage msg = new ServerMessage("SellBuff.MAX_BUFFS", activeChar.getLang());
                  msg.add(Config.SELLBUFF_MAX_BUFFS);
                  activeChar.sendMessage(msg.toString());
                  return false;
               }

               if (!SellBuffsManager.isInSellList(activeChar, skillToAdd)) {
                  int itemId = 0;

                  for(String itemName : Config.SELLBUFF_CURRECY_LIST.keySet()) {
                     if (itemName.equals(currency)) {
                        itemId = Config.SELLBUFF_CURRECY_LIST.get(itemName);
                        break;
                     }
                  }

                  if (itemId == 0) {
                     activeChar.sendMessage(new ServerMessage("SellBuff.WRONG_CURRENCY", activeChar.getLang()).toString());
                     SellBuffsManager.sendBuffChoiceMenu(activeChar, page);
                     return false;
                  }

                  activeChar.getSellingBuffs().add(new SellBuffHolder(skillToAdd.getId(), skillToAdd.getLevel(), itemId, price));
                  ServerMessage msg = new ServerMessage("SellBuff.SKILL_ADDED", activeChar.getLang());
                  msg.add(activeChar.getSkillName(skillToAdd));
                  activeChar.sendMessage(msg.toString());
                  SellBuffsManager.sendBuffChoiceMenu(activeChar, page);
               }
            }
            break;
         case "sellbuffbuymenu":
            if (params != null && !params.isEmpty()) {
               StringTokenizer st = new StringTokenizer(params, " ");
               int objId = -1;
               int page = 1;
               if (st.hasMoreTokens()) {
                  objId = Integer.parseInt(st.nextToken());
               }

               if (st.hasMoreTokens()) {
                  page = Integer.parseInt(st.nextToken());
               }

               Player seller = World.getInstance().getPlayer(objId);
               if (seller != null) {
                  if (!seller.isSellingBuffs() || !activeChar.isInsideRadius(seller, 150, true, true)) {
                     return false;
                  }

                  SellBuffsManager.sendBuffMenu(activeChar, seller, page);
               }
            }
            break;
         case "sellbuffbuyskill":
            if (params != null && !params.isEmpty()) {
               StringTokenizer st = new StringTokenizer(params, " ");
               int objId = -1;
               int skillId = -1;
               int page = 1;
               if (st.hasMoreTokens()) {
                  objId = Integer.parseInt(st.nextToken());
               }

               if (st.hasMoreTokens()) {
                  skillId = Integer.parseInt(st.nextToken());
               }

               if (st.hasMoreTokens()) {
                  page = Integer.parseInt(st.nextToken());
               }

               if (skillId == -1 || objId == -1) {
                  return false;
               }

               Player seller = World.getInstance().getPlayer(objId);
               if (seller == null) {
                  return false;
               }

               Skill skillToBuy = seller.getKnownSkill(skillId);
               if (!seller.isSellingBuffs() || !Util.checkIfInRange(150, activeChar, seller, true) || skillToBuy == null) {
                  return false;
               }

               if (Config.SELLBUFF_USED_MP && seller.getCurrentMp() < (double)skillToBuy.getMpConsume()) {
                  ServerMessage msg = new ServerMessage("SellBuff.HAVENO_MP", activeChar.getLang());
                  msg.add(seller.getName());
                  msg.add(activeChar.getSkillName(skillToBuy));
                  activeChar.sendMessage(msg.toString());
                  SellBuffsManager.sendBuffMenu(activeChar, seller, page);
                  return false;
               }

               for(SellBuffHolder holder : seller.getSellingBuffs()) {
                  if (holder != null && holder.getId() == skillToBuy.getId()) {
                     Item item = ItemsParser.getInstance().getTemplate(holder.getItemId());
                     if (activeChar.getInventory().getItemByItemId(holder.getItemId()) == null
                        || activeChar.getInventory().getItemByItemId(holder.getItemId()).getCount() < holder.getPrice()) {
                        ServerMessage msg = new ServerMessage("SellBuff.NO_ITEM", activeChar.getLang());
                        msg.add(activeChar.getItemName(item));
                        activeChar.sendMessage(msg.toString());
                        return false;
                     }

                     activeChar.destroyItemByItemId("SellBuff", holder.getItemId(), holder.getPrice(), activeChar, true);
                     seller.getInventory().addItem("SellBuff", holder.getItemId(), holder.getPrice(), seller, true);
                     Skill s = SkillsParser.getInstance().getInfo(holder.getId(), holder.getLvl());
                     s.getEffects(activeChar, activeChar, false);
                     if (activeChar.hasSummon() && Config.ALLOW_SELLBUFFS_PETS) {
                        s.getEffects(activeChar.getSummon(), activeChar.getSummon(), false);
                     }

                     if (Config.SELLBUFF_USED_MP) {
                        seller.reduceCurrentMp((double)skillToBuy.getMpConsume());
                     }
                  }
               }

               SellBuffsManager.sendBuffMenu(activeChar, seller, page);
            }
      }

      return true;
   }

   @Override
   public String[] getVoicedCommandList() {
      return VOICED_COMMANDS;
   }

   @Override
   public String[] getBypassList() {
      return BYPASS_COMMANDS;
   }
}
