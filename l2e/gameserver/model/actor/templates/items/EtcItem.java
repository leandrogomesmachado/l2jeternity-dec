package l2e.gameserver.model.actor.templates.items;

import java.util.ArrayList;
import java.util.List;
import l2e.commons.util.StringUtil;
import l2e.gameserver.model.actor.templates.ExtractableProductTemplate;
import l2e.gameserver.model.items.type.EtcItemType;
import l2e.gameserver.model.stats.StatsSet;

public final class EtcItem extends Item {
   private String _handler;
   private EtcItemType _type;
   private final boolean _isBlessed;
   private final List<ExtractableProductTemplate> _extractableItems;
   private String _skinType;
   private int _skinId;

   public EtcItem(StatsSet set) {
      super(set);
      this._type = EtcItemType.valueOf(set.getString("etcitem_type", "none").toUpperCase());
      switch(this.getDefaultAction()) {
         case soulshot:
         case summon_soulshot:
         case summon_spiritshot:
         case spiritshot:
            this._type = EtcItemType.SHOT;
         default:
            if (this.is_ex_immediate_effect()) {
               this._type = EtcItemType.HERB;
            }

            this._type1 = 4;
            this._type2 = 5;
            if (this.isQuestItem()) {
               this._type2 = 3;
            } else if (this.getId() == 57 || this.getId() == 5575) {
               this._type2 = 4;
            }

            this._handler = set.getString("handler", null);
            this._isBlessed = set.getBool("blessed", false);
            String capsuled_items = set.getString("capsuled_items", null);
            if (capsuled_items != null) {
               String[] split = capsuled_items.split(";");
               this._extractableItems = new ArrayList<>(split.length);

               for(String part : split) {
                  if (!part.trim().isEmpty()) {
                     String[] data = part.split(",");
                     if (data.length != 4) {
                        _log.info(StringUtil.concat("> Couldnt parse ", part, " in capsuled_items! item ", this.toString()));
                     } else {
                        int itemId = Integer.parseInt(data[0]);
                        int min = Integer.parseInt(data[1]);
                        int max = Integer.parseInt(data[2]);
                        double chance = Double.parseDouble(data[3]);
                        if (max < min) {
                           _log.info(StringUtil.concat("> Max amount < Min amount in ", part, ", item ", this.toString()));
                        } else {
                           ExtractableProductTemplate product = new ExtractableProductTemplate(itemId, min, max, chance);
                           this._extractableItems.add(product);
                        }
                     }
                  }
               }

               ((ArrayList)this._extractableItems).trimToSize();
               if (this._handler == null) {
                  _log.warning("Item " + this + " define capsuled_items but missing handler.");
                  this._handler = "ExtractableItems";
               }
            } else {
               this._extractableItems = null;
            }

            String visual_skin = set.getString("visual_skin", null);
            if (visual_skin != null) {
               String[] data = visual_skin.split(",");
               if (data.length != 2) {
                  _log.info(StringUtil.concat("> Couldnt parse ", visual_skin, " in visual_skin! item ", this.toString()));
                  return;
               }

               this._skinType = data[0];
               this._skinId = Integer.parseInt(data[1]);
            }
      }
   }

   public EtcItemType getItemType() {
      return this._type;
   }

   @Override
   public final boolean isConsumable() {
      return this.getItemType() == EtcItemType.SHOT || this.getItemType() == EtcItemType.POTION;
   }

   @Override
   public int getItemMask() {
      return this.getItemType().mask();
   }

   public String getHandlerName() {
      return this._handler;
   }

   public final boolean isBlessed() {
      return this._isBlessed;
   }

   public List<ExtractableProductTemplate> getExtractableItems() {
      return this._extractableItems;
   }

   @Override
   public boolean isExtractableItem() {
      return this._extractableItems != null;
   }

   public String getSkinType() {
      return this._skinType;
   }

   public int getSkinId() {
      return this._skinId;
   }
}
