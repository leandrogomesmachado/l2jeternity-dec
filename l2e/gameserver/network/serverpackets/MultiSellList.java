package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.items.multisell.Entry;
import l2e.gameserver.model.items.multisell.Ingredient;
import l2e.gameserver.model.items.multisell.ListContainer;

public final class MultiSellList extends GameServerPacket {
   private int _size;
   private int _index;
   private final ListContainer _list;
   private final boolean _finished;

   public MultiSellList(ListContainer list, int index) {
      this._list = list;
      this._index = index;
      this._size = list.getEntries().size() - index;
      if (this._size > 40) {
         this._finished = false;
         this._size = 40;
      } else {
         this._finished = true;
      }
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._list.getListId());
      this.writeD(1 + this._index / 40);
      this.writeD(this._finished ? 1 : 0);
      this.writeD(40);
      this.writeD(this._size);

      while(this._size-- > 0) {
         Entry ent = this._list.getEntries().get(this._index++);
         this.writeD(ent.getEntryId());
         this.writeC(ent.isStackable() ? 1 : 0);
         this.writeH(0);
         this.writeD(0);
         this.writeD(0);
         this.writeH(-1);
         this.writeH(0);
         this.writeH(0);
         this.writeH(0);
         this.writeH(0);
         this.writeH(0);
         this.writeH(0);
         this.writeH(0);
         this.writeH(ent.getProducts().size());
         this.writeH(ent.getIngredients().size());

         for(Ingredient ing : ent.getProducts()) {
            this.writeD(ing.getId());
            this.writeD(ing.getTemplate() != null ? ing.getTemplate().getBodyPart() : 0);
            this.writeH(ing.getTemplate() != null ? ing.getTemplate().getType2() : '\uffff');
            this.writeQ(ing.getCount());
            this.writeH(ing.getItemInfo() != null ? ing.getItemInfo().getEnchantLevel() : ing.getEnchantLevel());
            this.writeD(ing.getItemInfo() != null ? ing.getItemInfo().getAugmentId() : ing.getAugmentationId());
            this.writeD(ing.getItemInfo() != null ? ing.getItemInfo().getTime() / 1000 : ing.getTime() / 1000);
            this.writeH(ing.getItemInfo() != null ? ing.getItemInfo().getElementId() : ing.getAttackElementType());
            this.writeH(ing.getItemInfo() != null ? ing.getItemInfo().getElementPower() : ing.getAttackElementPower());

            for(byte i = 0; i < 6; ++i) {
               this.writeH(ing.getItemInfo() != null ? ing.getItemInfo().getElementals()[i] : ing.getElementDefAttr(i));
            }
         }

         for(Ingredient ing : ent.getIngredients()) {
            this.writeD(ing.getId());
            this.writeH(ing.getTemplate() != null ? ing.getTemplate().getType2() : '\uffff');
            this.writeQ(ing.getCount());
            this.writeH(ing.getItemInfo() != null ? ing.getItemInfo().getEnchantLevel() : ing.getEnchantLevel());
            this.writeD(ing.getItemInfo() != null ? ing.getItemInfo().getAugmentId() : ing.getAugmentationId());
            this.writeD(ing.getItemInfo() != null ? ing.getItemInfo().getTime() / 1000 : ing.getTime() / 1000);
            this.writeH(ing.getItemInfo() != null ? ing.getItemInfo().getElementId() : ing.getAttackElementType());
            this.writeH(ing.getItemInfo() != null ? ing.getItemInfo().getElementPower() : ing.getAttackElementPower());

            for(byte i = 0; i < 6; ++i) {
               this.writeH(ing.getItemInfo() != null ? ing.getItemInfo().getElementals()[i] : ing.getElementDefAttr(i));
            }
         }
      }
   }
}
