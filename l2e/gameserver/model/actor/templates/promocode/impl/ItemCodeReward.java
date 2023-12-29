package l2e.gameserver.model.actor.templates.promocode.impl;

import l2e.commons.util.Util;
import l2e.gameserver.model.Augmentation;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import org.w3c.dom.NamedNodeMap;

public class ItemCodeReward extends AbstractCodeReward {
   private final int _itemId;
   private final long _itemCount;
   private final int _enchant;
   private final int _augmentId;
   private final String _elementals;
   private final int _durability;

   public ItemCodeReward(NamedNodeMap attr) {
      this._itemId = Integer.parseInt(attr.getNamedItem("id").getNodeValue());
      this._itemCount = attr.getNamedItem("count") != null ? Long.parseLong(attr.getNamedItem("count").getNodeValue()) : 1L;
      this._enchant = attr.getNamedItem("enchant") != null ? Integer.parseInt(attr.getNamedItem("enchant").getNodeValue()) : 0;
      this._augmentId = attr.getNamedItem("augmentId") != null ? Integer.parseInt(attr.getNamedItem("augmentId").getNodeValue()) : -1;
      this._elementals = attr.getNamedItem("elementals") != null ? attr.getNamedItem("elementals").getNodeValue() : null;
      this._durability = attr.getNamedItem("durability") != null ? Integer.parseInt(attr.getNamedItem("durability").getNodeValue()) : 0;
   }

   @Override
   public void giveReward(Player player) {
      ItemInstance item = player.getInventory().addItem("Code", this._itemId, this._itemCount, player, null);
      if (item != null && !item.isStackable()) {
         if (this._enchant != 0) {
            item.setEnchantLevel(this._enchant);
         }

         if (this._augmentId != -1) {
            item.setAugmentation(new Augmentation(this._augmentId));
         }

         if (this._elementals != null && !this._elementals.isEmpty()) {
            String[] elements = this._elementals.split(";");

            for(String el : elements) {
               String[] element = el.split(":");
               if (element != null) {
                  item.setElementAttr(Byte.parseByte(element[0]), Integer.parseInt(element[1]));
               }
            }
         }

         item.setCount(this._itemCount);
         if (this._durability > 0) {
            item.setTime((long)this._durability);
         }

         item.updateDatabase();
      }
   }

   @Override
   public String getIcon() {
      return Util.getItemIcon(this._itemId);
   }

   public int getItemId() {
      return this._itemId;
   }

   public long getCount() {
      return this._itemCount;
   }

   public int getEnchant() {
      return this._enchant;
   }

   public int getAugmentId() {
      return this._augmentId;
   }

   public String getElementals() {
      return this._elementals;
   }

   public int getDurability() {
      return this._durability;
   }
}
