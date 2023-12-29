package l2e.gameserver.model.zone.type;

import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.effects.AbnormalEffect;
import l2e.gameserver.model.zone.ZoneType;

public class AbnormalZone extends ZoneType {
   private AbnormalEffect[] _abnormalEffect = null;

   public AbnormalZone(int id) {
      super(id);
   }

   @Override
   public void setParameter(String name, String value) {
      if (name.equals("abnormalVisualEffect")) {
         String[] specialEffects = value.split(",");
         this._abnormalEffect = new AbnormalEffect[specialEffects.length];

         for(int i = 0; i < specialEffects.length; ++i) {
            this._abnormalEffect[i] = AbnormalEffect.getByName(specialEffects[i]);
         }
      } else {
         super.setParameter(name, value);
      }
   }

   @Override
   protected void onEnter(Creature character) {
      if (this._abnormalEffect != null) {
         for(AbnormalEffect eff : this._abnormalEffect) {
            if (eff != null && eff != AbnormalEffect.NONE) {
               character.startAbnormalEffect(eff);
            }
         }
      }
   }

   @Override
   protected void onExit(Creature character) {
      if (this._abnormalEffect != null) {
         for(AbnormalEffect eff : this._abnormalEffect) {
            if (eff != null && eff != AbnormalEffect.NONE) {
               character.stopAbnormalEffect(eff);
            }
         }
      }
   }

   @Override
   public void onDieInside(Creature character) {
      this.onExit(character);
   }

   @Override
   public void onReviveInside(Creature character) {
      this.onEnter(character);
   }
}
