package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.PetItemList;

public class Restoration extends Effect {
   private final int _itemId;
   private final int _itemCount;

   public Restoration(Env env, EffectTemplate template) {
      super(env, template);
      this._itemId = template.getParameters().getInteger("itemId", 0);
      this._itemCount = template.getParameters().getInteger("itemCount", 0);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.NONE;
   }

   @Override
   public boolean onStart() {
      if (this.getEffected() == null || !this.getEffected().isPlayable()) {
         return false;
      } else if (this._itemId > 0 && this._itemCount > 0) {
         if (this.getEffected().isPlayer()) {
            this.getEffected().getActingPlayer().addItem("Skill", this._itemId, (long)this._itemCount, this.getEffector(), true);
         } else if (this.getEffected().isPet()) {
            this.getEffected().getInventory().addItem("Skill", this._itemId, (long)this._itemCount, this.getEffected().getActingPlayer(), this.getEffector());
            this.getEffected().getActingPlayer().sendPacket(new PetItemList(this.getEffected().getInventory().getItems()));
         }

         return true;
      } else {
         this.getEffected().sendPacket(SystemMessageId.NOTHING_INSIDE_THAT);
         _log.warning(Restoration.class.getSimpleName() + " effect with wrong item Id/count: " + this._itemId + "/" + this._itemCount + "!");
         return false;
      }
   }
}
