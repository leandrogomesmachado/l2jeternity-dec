package l2e.gameserver.handler.communityhandlers.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.data.parser.FoundationParser;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.handler.communityhandlers.ICommunityBoardHandler;
import l2e.gameserver.handler.communityhandlers.impl.model.ForgeElement;
import l2e.gameserver.model.Elementals;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.Armor;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.type.WeaponType;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.model.strings.server.ServerStorage;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class CommunityForge extends AbstractCommunity implements ICommunityBoardHandler {
   public CommunityForge() {
      if (Config.DEBUG) {
         _log.info(this.getClass().getSimpleName() + ": Loading all functions.");
      }
   }

   @Override
   public String[] getBypassCommands() {
      return new String[]{"_bbsforge"};
   }

   @Override
   public void onBypassCommand(String command, Player player) {
      String content = "";
      if (command.equals("_bbsforge")) {
         content = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/forge/index.htm");
      } else if (command.equals("_bbsforge:enchant:list")) {
         content = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/forge/itemlist.htm");
         ItemInstance head = player.getInventory().getPaperdollItem(1);
         ItemInstance chest = player.getInventory().getPaperdollItem(6);
         ItemInstance legs = player.getInventory().getPaperdollItem(11);
         ItemInstance gloves = player.getInventory().getPaperdollItem(10);
         ItemInstance feet = player.getInventory().getPaperdollItem(12);
         ItemInstance lhand = player.getInventory().getPaperdollItem(7);
         ItemInstance rhand = player.getInventory().getPaperdollItem(5);
         ItemInstance lfinger = player.getInventory().getPaperdollItem(14);
         ItemInstance rfinger = player.getInventory().getPaperdollItem(13);
         ItemInstance neck = player.getInventory().getPaperdollItem(4);
         ItemInstance lear = player.getInventory().getPaperdollItem(9);
         ItemInstance rear = player.getInventory().getPaperdollItem(8);
         Map<Integer, String[]> data = new HashMap<>();
         data.put(1, ForgeElement.generateEnchant(head, Config.BBS_FORGE_ENCHANT_MAX[1], 1, player));
         data.put(6, ForgeElement.generateEnchant(chest, Config.BBS_FORGE_ENCHANT_MAX[1], 6, player));
         data.put(11, ForgeElement.generateEnchant(legs, Config.BBS_FORGE_ENCHANT_MAX[1], 11, player));
         data.put(10, ForgeElement.generateEnchant(gloves, Config.BBS_FORGE_ENCHANT_MAX[1], 10, player));
         data.put(12, ForgeElement.generateEnchant(feet, Config.BBS_FORGE_ENCHANT_MAX[1], 12, player));
         data.put(14, ForgeElement.generateEnchant(lfinger, Config.BBS_FORGE_ENCHANT_MAX[2], 14, player));
         data.put(13, ForgeElement.generateEnchant(rfinger, Config.BBS_FORGE_ENCHANT_MAX[2], 13, player));
         data.put(4, ForgeElement.generateEnchant(neck, Config.BBS_FORGE_ENCHANT_MAX[2], 4, player));
         data.put(9, ForgeElement.generateEnchant(lear, Config.BBS_FORGE_ENCHANT_MAX[2], 9, player));
         data.put(8, ForgeElement.generateEnchant(rear, Config.BBS_FORGE_ENCHANT_MAX[2], 8, player));
         data.put(5, ForgeElement.generateEnchant(rhand, Config.BBS_FORGE_ENCHANT_MAX[0], 5, player));
         if (rhand != null
            && (
               rhand.getItem().getItemType() == WeaponType.BIGBLUNT
                  || rhand.getItem().getItemType() == WeaponType.BOW
                  || rhand.getItem().getItemType() == WeaponType.DUALDAGGER
                  || rhand.getItem().getItemType() == WeaponType.ANCIENTSWORD
                  || rhand.getItem().getItemType() == WeaponType.CROSSBOW
                  || rhand.getItem().getItemType() == WeaponType.BIGBLUNT
                  || rhand.getItem().getItemType() == WeaponType.BIGSWORD
                  || rhand.getItem().getItemType() == WeaponType.DUALFIST
                  || rhand.getItem().getItemType() == WeaponType.DUAL
                  || rhand.getItem().getItemType() == WeaponType.POLE
                  || rhand.getItem().getItemType() == WeaponType.FIST
            )) {
            data.put(
               7,
               new String[]{
                  rhand.getItem().getIcon(),
                  player.getItemName(rhand.getItem()) + " " + (rhand.getEnchantLevel() > 0 ? "+" + rhand.getEnchantLevel() : ""),
                  "" + ServerStorage.getInstance().getString(player.getLang(), "ServiceBBS.EMPTY") + "",
                  "L2UI_CT1.ItemWindow_DF_SlotBox_Disable"
               }
            );
         } else {
            data.put(7, ForgeElement.generateEnchant(lhand, Config.BBS_FORGE_ENCHANT_MAX[0], 7, player));
         }

         content = content.replace("<?content?>", ForgeElement.page(player));

         for(Entry<Integer, String[]> info : data.entrySet()) {
            int slot = info.getKey();
            String[] array = (String[])info.getValue();
            content = content.replace("<?" + slot + "_icon?>", array[0]);
            content = content.replace("<?" + slot + "_name?>", array[1]);
            content = content.replace("<?" + slot + "_button?>", array[2]);
            content = content.replace("<?" + slot + "_pic?>", array[3]);
         }

         data.clear();
      } else if (command.startsWith("_bbsforge:enchant:item:")) {
         String[] array = command.split(":");
         int item = Integer.parseInt(array[3]);
         String name = player.getItemName(ItemsParser.getInstance().getTemplate(Config.BBS_FORGE_ENCHANT_ITEM));
         if (name.isEmpty()) {
            name = "" + ServerStorage.getInstance().getString(player.getLang(), "ServiceBBS.NO_NAME") + "";
         }

         if (item < 1 || item > 14) {
            return;
         }

         ItemInstance _item = player.getInventory().getPaperdollItem(item);
         if (_item == null) {
            player.sendMessage(new ServerMessage("ServiceBBS.YOU_REMOVE_ITEM", player.getLang()).toString());
            this.onBypassCommand("_bbsforge:enchant:list", player);
            return;
         }

         if (_item.isHeroItem()) {
            player.sendMessage(new ServerMessage("ServiceBBS.CANT_ENCH_HERO_WEAPON", player.getLang()).toString());
            this.onBypassCommand("_bbsforge:enchant:list", player);
            return;
         }

         if (_item.getItem().isArrow()) {
            player.sendMessage(new ServerMessage("ServiceBBS.CANT_ENCH_ARROW", player.getLang()).toString());
            this.onBypassCommand("_bbsforge:enchant:list", player);
            return;
         }

         content = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/forge/enchant.htm");
         String template = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/forge/enchant_template.htm");
         template = template.replace("{icon}", _item.getItem().getIcon());
         String _name = player.getItemName(_item.getItem());
         _name = _name.replace(" {PvP}", "");
         if (_name.length() > 30) {
            _name = _name.substring(0, 29) + "...";
         }

         template = template.replace("{name}", _name);
         template = template.replace("{enchant}", _item.getEnchantLevel() <= 0 ? "" : "+" + _item.getEnchantLevel());
         template = template.replace("{msg}", "" + ServerStorage.getInstance().getString(player.getLang(), "ServiceBBS.SELECT_ENCH_LEVEL") + "");
         String button_tm = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/forge/enchant_button_template.htm");
         String button = null;
         String block = "";
         int[] level = _item.isArmor()
            ? Config.BBS_FORGE_ARMOR_ENCHANT_LVL
            : (_item.isWeapon() ? Config.BBS_FORGE_WEAPON_ENCHANT_LVL : Config.BBS_FORGE_JEWELS_ENCHANT_LVL);
         int index = 0;

         for(int i = 0; i < level.length; ++i) {
            if (_item.getEnchantLevel() < level[i]) {
               block = button_tm.replace("{link}", "bypass _bbsforge:enchant:" + i * item + ":" + item);
               block = block.replace(
                  "{value}",
                  "+"
                     + level[i]
                     + " ("
                     + (
                        _item.isArmor()
                           ? Config.BBS_FORGE_ENCHANT_PRICE_ARMOR[i]
                           : (_item.isWeapon() ? Config.BBS_FORGE_ENCHANT_PRICE_WEAPON[i] : Config.BBS_FORGE_ENCHANT_PRICE_JEWELS[i])
                     )
                     + " "
                     + name
                     + ")"
               );
               if (++index % 2 == 0) {
                  if (index > 0) {
                     block = block + "</tr>";
                  }

                  block = block + "<tr>";
               }

               button = button + block;
            }
         }

         template = template.replace("{button}", button == null ? "" : button);
         content = content.replace("<?content?>", template);
      } else if (command.equals("_bbsforge:foundation:list")) {
         content = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/forge/foundationlist.htm");
         ItemInstance head = player.getInventory().getPaperdollItem(1);
         ItemInstance chest = player.getInventory().getPaperdollItem(6);
         ItemInstance legs = player.getInventory().getPaperdollItem(11);
         ItemInstance gloves = player.getInventory().getPaperdollItem(10);
         ItemInstance feet = player.getInventory().getPaperdollItem(12);
         ItemInstance lhand = player.getInventory().getPaperdollItem(7);
         ItemInstance rhand = player.getInventory().getPaperdollItem(5);
         ItemInstance lfinger = player.getInventory().getPaperdollItem(14);
         ItemInstance rfinger = player.getInventory().getPaperdollItem(13);
         ItemInstance neck = player.getInventory().getPaperdollItem(4);
         ItemInstance lear = player.getInventory().getPaperdollItem(9);
         ItemInstance rear = player.getInventory().getPaperdollItem(8);
         Map<Integer, String[]> data = new HashMap<>();
         data.put(1, ForgeElement.generateFoundation(head, 1, player));
         data.put(6, ForgeElement.generateFoundation(chest, 6, player));
         data.put(11, ForgeElement.generateFoundation(legs, 11, player));
         data.put(10, ForgeElement.generateFoundation(gloves, 10, player));
         data.put(12, ForgeElement.generateFoundation(feet, 12, player));
         data.put(14, ForgeElement.generateFoundation(lfinger, 14, player));
         data.put(13, ForgeElement.generateFoundation(rfinger, 13, player));
         data.put(4, ForgeElement.generateFoundation(neck, 4, player));
         data.put(9, ForgeElement.generateFoundation(lear, 9, player));
         data.put(8, ForgeElement.generateFoundation(rear, 8, player));
         data.put(5, ForgeElement.generateFoundation(rhand, 5, player));
         if (rhand != null
            && (
               rhand.getItem().getItemType() == WeaponType.BIGBLUNT
                  || rhand.getItem().getItemType() == WeaponType.BOW
                  || rhand.getItem().getItemType() == WeaponType.DUALDAGGER
                  || rhand.getItem().getItemType() == WeaponType.ANCIENTSWORD
                  || rhand.getItem().getItemType() == WeaponType.CROSSBOW
                  || rhand.getItem().getItemType() == WeaponType.BIGBLUNT
                  || rhand.getItem().getItemType() == WeaponType.BIGSWORD
                  || rhand.getItem().getItemType() == WeaponType.DUALFIST
                  || rhand.getItem().getItemType() == WeaponType.DUAL
                  || rhand.getItem().getItemType() == WeaponType.POLE
                  || rhand.getItem().getItemType() == WeaponType.FIST
            )) {
            data.put(
               7,
               new String[]{
                  rhand.getItem().getIcon(),
                  player.getItemName(rhand.getItem()) + " " + (rhand.getEnchantLevel() > 0 ? "+" + rhand.getEnchantLevel() : ""),
                  "<font color=\"FF0000\">...</font>",
                  "L2UI_CT1.ItemWindow_DF_SlotBox_Disable"
               }
            );
         } else {
            data.put(7, ForgeElement.generateFoundation(lhand, 7, player));
         }

         content = content.replace("<?content?>", ForgeElement.page(player));

         for(Entry<Integer, String[]> info : data.entrySet()) {
            int slot = info.getKey();
            String[] array = (String[])info.getValue();
            content = content.replace("<?" + slot + "_icon?>", array[0]);
            content = content.replace("<?" + slot + "_name?>", array[1]);
            content = content.replace("<?" + slot + "_button?>", array[2]);
            content = content.replace("<?" + slot + "_pic?>", array[3]);
         }
      } else {
         if (command.startsWith("_bbsforge:foundation:item:")) {
            String[] array = command.split(":");
            int item = Integer.parseInt(array[3]);
            if (item >= 1 && item <= 14) {
               ItemInstance _item = player.getInventory().getPaperdollItem(item);
               if (_item == null) {
                  player.sendMessage(new ServerMessage("ServiceBBS.YOU_REMOVE_ITEM", player.getLang()).toString());
                  this.onBypassCommand("_bbsforge:foundation:list", player);
                  return;
               }

               if (_item.isHeroItem()) {
                  player.sendMessage(new ServerMessage("ServiceBBS.CANT_ENCH_HERO_WEAPON", player.getLang()).toString());
                  this.onBypassCommand("_bbsforge:foundation:list", player);
                  return;
               }

               int found = FoundationParser.getInstance().getFoundation(_item.getId());
               if (found == -1) {
                  player.sendMessage(new ServerMessage("ServiceBBS.YOU_REMOVE_ITEM", player.getLang()).toString());
                  this.onBypassCommand("_bbsforge:foundation:list", player);
                  return;
               }

               int price;
               if (_item.getItem().isAccessory()) {
                  price = Config.BBS_FORGE_FOUNDATION_PRICE_JEWEL[_item.getItem().getCrystalType()];
               } else if (_item.isWeapon()) {
                  price = Config.BBS_FORGE_FOUNDATION_PRICE_WEAPON[_item.getItem().getCrystalType()];
               } else {
                  price = Config.BBS_FORGE_FOUNDATION_PRICE_ARMOR[_item.getItem().getCrystalType()];
               }

               if (player.getInventory().getItemByItemId(Config.BBS_FORGE_FOUNDATION_ITEM) == null) {
                  player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                  return;
               }

               if (player.getInventory().getItemByItemId(Config.BBS_FORGE_FOUNDATION_ITEM).getCount() < (long)price) {
                  player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                  return;
               }

               if (player.getInventory().destroyItemByObjectId(_item.getObjectId(), _item.getCount(), player, Boolean.valueOf(true)) != null) {
                  player.destroyItemByItemId("ForgeBBS", Config.BBS_FORGE_FOUNDATION_ITEM, (long)price, player, true);
                  ItemInstance _found = player.getInventory().addItem("ForgeBBS", found, 1L, player, true);
                  _found.setEnchantLevel(_item.getEnchantLevel());
                  _found.setAugmentation(_item.getAugmentation());
                  if (_item.getElementals() != null) {
                     for(Elementals elm : _item.getElementals()) {
                        if (elm.getElement() != -1 && elm.getValue() != -1) {
                           _found.setElementAttr(elm.getElement(), elm.getValue());
                        }
                     }
                  }

                  player.getInventory().equipItem(_found);
                  Util.addServiceLog(player.getName() + " buy foundation item: " + _found.getName() + " +" + _item.getEnchantLevel());
                  InventoryUpdate iu = new InventoryUpdate();
                  iu.addItem(_found);
                  iu.addModifiedItem(_found);
                  player.sendPacket(iu);
                  player.sendMessage(
                     ""
                        + ServerStorage.getInstance().getString(player.getLang(), "ServiceBBS.EXCHANGE_ITEM")
                        + " "
                        + player.getItemName(_item.getItem())
                        + " "
                        + ServerStorage.getInstance().getString(player.getLang(), "ServiceBBS.TO_FOUNDATION")
                        + " "
                        + player.getItemName(_found.getItem())
                  );
               } else {
                  player.sendMessage(new ServerMessage("ServiceBBS.FOUNDATION_FAIL", player.getLang()).toString());
               }

               this.onBypassCommand("_bbsforge:foundation:list", player);
               return;
            }

            return;
         }

         if (command.startsWith("_bbsforge:enchant:")) {
            String[] array = command.split(":");
            int val = Integer.parseInt(array[2]);
            int item = Integer.parseInt(array[3]);
            int conversion = val / item;
            ItemInstance _item = player.getInventory().getPaperdollItem(item);
            if (_item == null) {
               player.sendMessage(new ServerMessage("ServiceBBS.YOU_REMOVE_ITEM", player.getLang()).toString());
               this.onBypassCommand("_bbsforge:enchant:list", player);
               return;
            }

            if (_item.isHeroItem()) {
               player.sendMessage(new ServerMessage("ServiceBBS.CANT_ENCH_HERO_WEAPON", player.getLang()).toString());
               this.onBypassCommand("_bbsforge:enchant:list", player);
               return;
            }

            int[] level = _item.isArmor()
               ? Config.BBS_FORGE_ARMOR_ENCHANT_LVL
               : (_item.isWeapon() ? Config.BBS_FORGE_WEAPON_ENCHANT_LVL : Config.BBS_FORGE_JEWELS_ENCHANT_LVL);
            int Value = level[conversion];
            int max = _item.isArmor()
               ? Config.BBS_FORGE_ENCHANT_MAX[1]
               : (_item.isWeapon() ? Config.BBS_FORGE_ENCHANT_MAX[0] : Config.BBS_FORGE_ENCHANT_MAX[2]);
            if (Value > max) {
               return;
            }

            if (_item.getItem().isArrow()) {
               player.sendMessage(new ServerMessage("ServiceBBS.CANT_ENCH_ARROW", player.getLang()).toString());
               this.onBypassCommand("_bbsforge:enchant:list", player);
               return;
            }

            int price = _item.isArmor()
               ? Config.BBS_FORGE_ENCHANT_PRICE_ARMOR[conversion]
               : (_item.isWeapon() ? Config.BBS_FORGE_ENCHANT_PRICE_WEAPON[conversion] : Config.BBS_FORGE_ENCHANT_PRICE_JEWELS[conversion]);
            if (player.getInventory().getItemByItemId(Config.BBS_FORGE_ENCHANT_ITEM) == null) {
               player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            if (player.getInventory().getItemByItemId(Config.BBS_FORGE_ENCHANT_ITEM).getCount() < (long)price) {
               player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            player.destroyItemByItemId("ForgeBBS", Config.BBS_FORGE_ENCHANT_ITEM, (long)price, player, true);
            _item.setEnchantLevel(Value);
            _item.updateDatabase();
            if (_item.getItem() instanceof Armor && _item.getEnchantLevel() == 4) {
               Skill enchant4Skill = ((Armor)_item.getItem()).getEnchant4Skill();
               if (enchant4Skill != null) {
                  player.addSkill(enchant4Skill, false);
                  player.sendSkillList(false);
               }
            }

            Util.addServiceLog(player.getName() + " buy enchant service for item: " + _item.getName() + " +" + _item.getEnchantLevel());
            InventoryUpdate iu = new InventoryUpdate();
            iu.addModifiedItem(_item);
            player.sendPacket(iu);
            player.broadcastCharInfo();
            ServerMessage msg = new ServerMessage("ServiceBBS.ITEM_ENCHANT", player.getLang());
            msg.add(player.getItemName(_item.getItem()));
            msg.add(Value);
            player.sendMessage(msg.toString());
            this.onBypassCommand("_bbsforge:enchant:list", player);
            return;
         }

         if (command.equals("_bbsforge:attribute:list")) {
            content = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/forge/attributelist.htm");
            ItemInstance head = player.getInventory().getPaperdollItem(1);
            ItemInstance chest = player.getInventory().getPaperdollItem(6);
            ItemInstance legs = player.getInventory().getPaperdollItem(11);
            ItemInstance gloves = player.getInventory().getPaperdollItem(10);
            ItemInstance feet = player.getInventory().getPaperdollItem(12);
            ItemInstance lhand = player.getInventory().getPaperdollItem(7);
            ItemInstance rhand = player.getInventory().getPaperdollItem(5);
            ItemInstance lfinger = player.getInventory().getPaperdollItem(14);
            ItemInstance rfinger = player.getInventory().getPaperdollItem(13);
            ItemInstance neck = player.getInventory().getPaperdollItem(4);
            ItemInstance lear = player.getInventory().getPaperdollItem(9);
            ItemInstance rear = player.getInventory().getPaperdollItem(8);
            Map<Integer, String[]> data = new HashMap<>();
            data.put(1, ForgeElement.generateAttribution(head, 1, player, true));
            data.put(6, ForgeElement.generateAttribution(chest, 6, player, true));
            data.put(11, ForgeElement.generateAttribution(legs, 11, player, true));
            data.put(10, ForgeElement.generateAttribution(gloves, 10, player, true));
            data.put(12, ForgeElement.generateAttribution(feet, 12, player, true));
            data.put(14, ForgeElement.generateAttribution(lfinger, 14, player, true));
            data.put(13, ForgeElement.generateAttribution(rfinger, 13, player, true));
            data.put(4, ForgeElement.generateAttribution(neck, 4, player, true));
            data.put(9, ForgeElement.generateAttribution(lear, 9, player, true));
            data.put(8, ForgeElement.generateAttribution(rear, 8, player, true));
            data.put(5, ForgeElement.generateAttribution(rhand, 5, player, true));
            if (rhand != null
               && (
                  rhand.getItem().getItemType() == WeaponType.BIGBLUNT
                     || rhand.getItem().getItemType() == WeaponType.BOW
                     || rhand.getItem().getItemType() == WeaponType.DUALDAGGER
                     || rhand.getItem().getItemType() == WeaponType.ANCIENTSWORD
                     || rhand.getItem().getItemType() == WeaponType.CROSSBOW
                     || rhand.getItem().getItemType() == WeaponType.BIGBLUNT
                     || rhand.getItem().getItemType() == WeaponType.BIGSWORD
                     || rhand.getItem().getItemType() == WeaponType.DUALFIST
                     || rhand.getItem().getItemType() == WeaponType.DUAL
                     || rhand.getItem().getItemType() == WeaponType.POLE
                     || rhand.getItem().getItemType() == WeaponType.FIST
               )) {
               data.put(
                  7,
                  new String[]{
                     rhand.getItem().getIcon(),
                     player.getItemName(rhand.getItem()) + " " + (rhand.getEnchantLevel() > 0 ? "+" + rhand.getEnchantLevel() : ""),
                     "<font color=\"FF0000\">...</font>",
                     "L2UI_CT1.ItemWindow_DF_SlotBox_Disable"
                  }
               );
            } else {
               data.put(7, ForgeElement.generateAttribution(lhand, 7, player, true));
            }

            content = content.replace("<?content?>", ForgeElement.page(player));

            for(Entry<Integer, String[]> info : data.entrySet()) {
               int slot = info.getKey();
               String[] array = (String[])info.getValue();
               content = content.replace("<?" + slot + "_icon?>", array[0]);
               content = content.replace("<?" + slot + "_name?>", array[1]);
               content = content.replace("<?" + slot + "_button?>", array[2]);
               content = content.replace("<?" + slot + "_pic?>", array[3]);
            }
         } else if (command.startsWith("_bbsforge:attribute:item:")) {
            String[] array = command.split(":");
            int item = Integer.parseInt(array[3]);
            if (item < 1 || item > 14) {
               return;
            }

            ItemInstance _item = player.getInventory().getPaperdollItem(item);
            if (_item == null) {
               player.sendMessage(new ServerMessage("ServiceBBS.YOU_REMOVE_ITEM", player.getLang()).toString());
               this.onBypassCommand("_bbsforge:attribute:list", player);
               return;
            }

            if (!ForgeElement.itemCheckGrade(true, _item)) {
               player.sendMessage(new ServerMessage("ServiceBBS.CANT_GRADE_ENCHANT", player.getLang()).toString());
               this.onBypassCommand("_bbsforge:attribute:list", player);
               return;
            }

            if (_item.isHeroItem()) {
               player.sendMessage(new ServerMessage("ServiceBBS.CANT_ENCH_HERO_WEAPON", player.getLang()).toString());
               this.onBypassCommand("_bbsforge:attribute:list", player);
               return;
            }

            if (_item.getItem().isAccessory()) {
               player.sendMessage(new ServerMessage("ServiceBBS.CATN_ENCH_JEVERLY", player.getLang()).toString());
               this.onBypassCommand("_bbsforge:attribute:list", player);
               return;
            }

            if (_item.getItem().isShield()) {
               player.sendMessage(new ServerMessage("ServiceBBS.CATN_ENCH_SHIELD", player.getLang()).toString());
               this.onBypassCommand("_bbsforge:attribute:list", player);
               return;
            }

            content = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/forge/attribute.htm");
            String slotclose = "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">";
            String buttonFire = "<button action=\"bypass _bbsforge:attribute:element:0:"
               + item
               + "\" width=34 height=34 back=\"L2UI_CT1.ItemWindow_DF_Frame_Down\" fore=\"L2UI_CT1.ItemWindow_DF_Frame\"/>";
            String buttonWater = "<button action=\"bypass _bbsforge:attribute:element:1:"
               + item
               + "\" width=34 height=34 back=\"L2UI_CT1.ItemWindow_DF_Frame_Down\" fore=\"L2UI_CT1.ItemWindow_DF_Frame\"/>";
            String buttonWind = "<button action=\"bypass _bbsforge:attribute:element:2:"
               + item
               + "\" width=34 height=34 back=\"L2UI_CT1.ItemWindow_DF_Frame_Down\" fore=\"L2UI_CT1.ItemWindow_DF_Frame\"/>";
            String buttonEarth = "<button action=\"bypass _bbsforge:attribute:element:3:"
               + item
               + "\" width=34 height=34 back=\"L2UI_CT1.ItemWindow_DF_Frame_Down\" fore=\"L2UI_CT1.ItemWindow_DF_Frame\"/>";
            String buttonHoly = "<button action=\"bypass _bbsforge:attribute:element:4:"
               + item
               + "\" width=34 height=34 back=\"L2UI_CT1.ItemWindow_DF_Frame_Down\" fore=\"L2UI_CT1.ItemWindow_DF_Frame\"/>";
            String buttonUnholy = "<button action=\"bypass _bbsforge:attribute:element:5:"
               + item
               + "\" width=34 height=34 back=\"L2UI_CT1.ItemWindow_DF_Frame_Down\" fore=\"L2UI_CT1.ItemWindow_DF_Frame\"/>";
            if (_item.isWeapon()) {
               if (_item.getElementals() != null && _item.getElementals()[0].getElement() == _item.getElementals()[0].getFire()) {
                  buttonWater = "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">";
                  buttonWind = "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">";
                  buttonEarth = "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">";
                  buttonHoly = "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">";
                  buttonUnholy = "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">";
               }

               if (_item.getElementals() != null && _item.getElementals()[0].getElement() == _item.getElementals()[0].getWater()) {
                  buttonFire = "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">";
                  buttonWind = "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">";
                  buttonEarth = "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">";
                  buttonHoly = "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">";
                  buttonUnholy = "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">";
               }

               if (_item.getElementals() != null && _item.getElementals()[0].getElement() == _item.getElementals()[0].getWind()) {
                  buttonWater = "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">";
                  buttonFire = "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">";
                  buttonEarth = "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">";
                  buttonHoly = "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">";
                  buttonUnholy = "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">";
               }

               if (_item.getElementals() != null && _item.getElementals()[0].getElement() == _item.getElementals()[0].getEarth()) {
                  buttonWater = "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">";
                  buttonWind = "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">";
                  buttonFire = "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">";
                  buttonHoly = "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">";
                  buttonUnholy = "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">";
               }

               if (_item.getElementals() != null && _item.getElementals()[0].getElement() == _item.getElementals()[0].getHoly()) {
                  buttonWater = "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">";
                  buttonWind = "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">";
                  buttonEarth = "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">";
                  buttonFire = "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">";
                  buttonUnholy = "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">";
               }

               if (_item.getElementals() != null && _item.getElementals()[0].getElement() == _item.getElementals()[0].getUnholy()) {
                  buttonWater = "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">";
                  buttonWind = "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">";
                  buttonEarth = "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">";
                  buttonHoly = "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">";
                  buttonFire = "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">";
               }
            }

            if (_item.isArmor() && _item.getElementals() != null) {
               for(Elementals elm : _item.getElementals()) {
                  if (elm.getElement() == elm.getFire()) {
                     if (elm.getValue() >= Config.BBS_FORGE_ARMOR_ATTRIBUTE_MAX) {
                        buttonFire = "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">";
                     }

                     buttonWater = "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">";
                  }

                  if (elm.getElement() == elm.getWater()) {
                     if (elm.getValue() >= Config.BBS_FORGE_ARMOR_ATTRIBUTE_MAX) {
                        buttonWater = "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">";
                     }

                     buttonFire = "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">";
                  }

                  if (elm.getElement() == elm.getWind()) {
                     if (elm.getValue() >= Config.BBS_FORGE_ARMOR_ATTRIBUTE_MAX) {
                        buttonWind = "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">";
                     }

                     buttonEarth = "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">";
                  }

                  if (elm.getElement() == elm.getEarth()) {
                     if (elm.getValue() >= Config.BBS_FORGE_ARMOR_ATTRIBUTE_MAX) {
                        buttonEarth = "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">";
                     }

                     buttonWind = "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">";
                  }

                  if (elm.getElement() == elm.getHoly()) {
                     if (elm.getValue() >= Config.BBS_FORGE_ARMOR_ATTRIBUTE_MAX) {
                        buttonHoly = "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">";
                     }

                     buttonUnholy = "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">";
                  }

                  if (elm.getElement() == elm.getUnholy()) {
                     if (elm.getValue() >= Config.BBS_FORGE_ARMOR_ATTRIBUTE_MAX) {
                        buttonUnholy = "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">";
                     }

                     buttonHoly = "<img src=\"L2UI_CT1.ItemWindow_DF_SlotBox_Disable\" width=\"32\" height=\"32\">";
                  }
               }
            }

            String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/forge/attribute_choice_template.htm");
            html = html.replace("{icon}", _item.getItem().getIcon());
            String _name = player.getItemName(_item.getItem());
            _name = _name.replace(" {PvP}", "");
            if (_name.length() > 30) {
               _name = _name.substring(0, 29) + "...";
            }

            html = html.replace("{name}", _name);
            html = html.replace("{enchant}", _item.getEnchantLevel() <= 0 ? "" : " +" + _item.getEnchantLevel());
            html = html.replace("{msg}", "" + ServerStorage.getInstance().getString(player.getLang(), "ServiceBBS.SELECT_ATTR") + "");
            html = html.replace("{fire}", buttonFire);
            html = html.replace("{water}", buttonWater);
            html = html.replace("{earth}", buttonEarth);
            html = html.replace("{wind}", buttonWind);
            html = html.replace("{holy}", buttonHoly);
            html = html.replace("{unholy}", buttonUnholy);
            content = content.replace("<?content?>", html);
         } else if (command.startsWith("_bbsforge:attribute:element:")) {
            String[] array = command.split(":");
            int element = Integer.parseInt(array[3]);
            String elementName = "";
            if (element == 0) {
               elementName = "" + ServerStorage.getInstance().getString(player.getLang(), "ServiceBBS.ATTR_FIRE") + "";
            } else if (element == 1) {
               elementName = "" + ServerStorage.getInstance().getString(player.getLang(), "ServiceBBS.ATTR_WATER") + "";
            } else if (element == 2) {
               elementName = "" + ServerStorage.getInstance().getString(player.getLang(), "ServiceBBS.ATTR_WIND") + "";
            } else if (element == 3) {
               elementName = "" + ServerStorage.getInstance().getString(player.getLang(), "ServiceBBS.ATTR_EARTH") + "";
            } else if (element == 4) {
               elementName = "" + ServerStorage.getInstance().getString(player.getLang(), "ServiceBBS.ATTR_HOLY") + "";
            } else if (element == 5) {
               elementName = "" + ServerStorage.getInstance().getString(player.getLang(), "ServiceBBS.ATTR_DARK") + "";
            }

            int item = Integer.parseInt(array[4]);
            String name = player.getItemName(ItemsParser.getInstance().getTemplate(Config.BBS_FORGE_ENCHANT_ITEM));
            if (name.isEmpty()) {
               name = "" + ServerStorage.getInstance().getString(player.getLang(), "ServiceBBS.NO_NAME") + "";
            }

            ItemInstance _item = player.getInventory().getPaperdollItem(item);
            if (_item == null) {
               player.sendMessage(new ServerMessage("ServiceBBS.YOU_REMOVE_ITEM", player.getLang()).toString());
               this.onBypassCommand("_bbsforge:attribute:list", player);
               return;
            }

            if (!ForgeElement.itemCheckGrade(true, _item)) {
               player.sendMessage(new ServerMessage("ServiceBBS.CANT_GRADE_ENCHANT", player.getLang()).toString());
               this.onBypassCommand("_bbsforge:attribute:list", player);
               return;
            }

            if (_item.isHeroItem()) {
               player.sendMessage(new ServerMessage("ServiceBBS.CANT_ENCH_HERO_WEAPON", player.getLang()).toString());
               this.onBypassCommand("_bbsforge:attribute:list", player);
               return;
            }

            content = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/forge/attribute.htm");
            String template = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/forge/enchant_template.htm");
            template = template.replace("{icon}", _item.getItem().getIcon());
            String _name = player.getItemName(_item.getItem());
            _name = _name.replace(" {PvP}", "");
            if (_name.length() > 30) {
               _name = _name.substring(0, 29) + "...";
            }

            template = template.replace("{name}", _name);
            template = template.replace("{enchant}", _item.getEnchantLevel() <= 0 ? "" : "+" + _item.getEnchantLevel());
            template = template.replace("{msg}", "" + ServerStorage.getInstance().getString(player.getLang(), "ServiceBBS.SELECTED") + " " + elementName + "");
            String button_tm = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/forge/enchant_button_template.htm");
            StringBuilder button = new StringBuilder();
            String block = null;
            int[] level = _item.isWeapon() ? Config.BBS_FORGE_ATRIBUTE_LVL_WEAPON : Config.BBS_FORGE_ATRIBUTE_LVL_ARMOR;
            int index = 0;

            for(int i = 0; i < level.length; ++i) {
               Elementals elementals = _item.getElementals() == null ? null : _item.getElementals()[0];
               if (elementals == null
                  || elementals.getElement() == -2
                  || elementals.getElement() != Elementals.getElementById(element)
                  || elementals.getValue() < (_item.isWeapon() ? Config.BBS_FORGE_ATRIBUTE_LVL_WEAPON[i] : Config.BBS_FORGE_ATRIBUTE_LVL_ARMOR[i])) {
                  block = button_tm.replace("{link}", String.valueOf("bypass _bbsforge:attribute:" + i * item + ":" + item + ":" + element));
                  block = block.replace(
                     "{value}",
                     "+"
                        + (_item.isWeapon() ? Config.BBS_FORGE_ATRIBUTE_LVL_WEAPON[i] : Config.BBS_FORGE_ATRIBUTE_LVL_ARMOR[i])
                        + " ("
                        + (_item.isWeapon() ? Config.BBS_FORGE_ATRIBUTE_PRICE_WEAPON[i] : Config.BBS_FORGE_ATRIBUTE_PRICE_ARMOR[i])
                        + " "
                        + name
                        + ")"
                  );
                  if (++index % 2 == 0) {
                     if (index > 0) {
                        block = block + "</tr>";
                     }

                     block = block + "<tr>";
                  }

                  button.append(block);
               }
            }

            template = template.replace("{button}", button.toString());
            content = content.replace("<?content?>", template);
         } else if (command.startsWith("_bbsforge:attribute:")) {
            String[] array = command.split(":");
            int val = Integer.parseInt(array[2]);
            int item = Integer.parseInt(array[3]);
            int att = Integer.parseInt(array[4]);
            ItemInstance _item = player.getInventory().getPaperdollItem(item);
            if (_item == null) {
               player.sendMessage(new ServerMessage("ServiceBBS.YOU_REMOVE_ITEM", player.getLang()).toString());
               this.onBypassCommand("_bbsforge:attribute:list", player);
               return;
            }

            if (!ForgeElement.itemCheckGrade(true, _item)) {
               player.sendMessage(new ServerMessage("ServiceBBS.CANT_GRADE_ENCHANT", player.getLang()).toString());
               this.onBypassCommand("_bbsforge:attribute:list", player);
               return;
            }

            if (_item.isHeroItem()) {
               player.sendMessage(new ServerMessage("ServiceBBS.CANT_ENCH_HERO_WEAPON", player.getLang()).toString());
               this.onBypassCommand("_bbsforge:attribute:list", player);
               return;
            }

            if (_item.isArmor() && !ForgeElement.canEnchantArmorAttribute(att, _item)) {
               player.sendMessage(new ServerMessage("ServiceBBS.CANT_INSERT_ATTR", player.getLang()).toString());
               this.onBypassCommand("_bbsforge:attribute:list", player);
               return;
            }

            int conversion = val / item;
            int Value = _item.isWeapon() ? Config.BBS_FORGE_ATRIBUTE_LVL_WEAPON[conversion] : Config.BBS_FORGE_ATRIBUTE_LVL_ARMOR[conversion];
            if (Value > (_item.isWeapon() ? Config.BBS_FORGE_WEAPON_ATTRIBUTE_MAX : Config.BBS_FORGE_ARMOR_ATTRIBUTE_MAX)) {
               return;
            }

            int price = _item.isWeapon() ? Config.BBS_FORGE_ATRIBUTE_PRICE_WEAPON[conversion] : Config.BBS_FORGE_ATRIBUTE_PRICE_ARMOR[conversion];
            if (player.getInventory().getItemByItemId(Config.BBS_FORGE_ENCHANT_ITEM) == null) {
               player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            if (player.getInventory().getItemByItemId(Config.BBS_FORGE_ENCHANT_ITEM).getCount() < (long)price) {
               player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               return;
            }

            player.destroyItemByItemId("ForgeBBS", Config.BBS_FORGE_ENCHANT_ITEM, (long)price, player, true);
            player.getInventory().unEquipItem(_item);
            _item.setElementAttr(Elementals.getElementById(att), Value);
            player.getInventory().equipItem(_item);
            Util.addServiceLog(player.getName() + " buy attribute service for item: " + _item.getName());
            InventoryUpdate iu = new InventoryUpdate();
            iu.addModifiedItem(_item);
            player.sendPacket(iu);
            player.broadcastCharInfo();
            String elementName = "";
            if (att == 0) {
               elementName = "" + ServerStorage.getInstance().getString(player.getLang(), "ServiceBBS.ATTR_FIRE") + "";
            } else if (att == 1) {
               elementName = "" + ServerStorage.getInstance().getString(player.getLang(), "ServiceBBS.ATTR_WATER") + "";
            } else if (att == 2) {
               elementName = "" + ServerStorage.getInstance().getString(player.getLang(), "ServiceBBS.ATTR_WIND") + "";
            } else if (att == 3) {
               elementName = "" + ServerStorage.getInstance().getString(player.getLang(), "ServiceBBS.ATTR_EARTH") + "";
            } else if (att == 4) {
               elementName = "" + ServerStorage.getInstance().getString(player.getLang(), "ServiceBBS.ATTR_HOLY") + "";
            } else if (att == 5) {
               elementName = "" + ServerStorage.getInstance().getString(player.getLang(), "ServiceBBS.ATTR_DARK") + "";
            }

            ServerMessage msg = new ServerMessage("ServiceBBS.ATTR_ADDED", player.getLang());
            msg.add(player.getItemName(_item.getItem()));
            msg.add(elementName);
            msg.add(Value);
            player.sendMessage(msg.toString());
            this.onBypassCommand("_bbsforge:attribute:list", player);
            return;
         }
      }

      separateAndSend(content, player);
   }

   @Override
   public void onWriteCommand(String command, String ar1, String ar2, String ar3, String ar4, String ar5, Player activeChar) {
   }

   public static CommunityForge getInstance() {
      return CommunityForge.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final CommunityForge _instance = new CommunityForge();
   }
}
