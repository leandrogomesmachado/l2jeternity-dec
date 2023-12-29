package l2e.gameserver.model.skills.funcs.formulas;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.funcs.Func;
import l2e.gameserver.model.stats.BaseStats;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Stats;

public class FuncMDefMod extends Func {
   private static final FuncMDefMod _fmm_instance = new FuncMDefMod();

   public static Func getInstance() {
      return _fmm_instance;
   }

   private FuncMDefMod() {
      super(Stats.MAGIC_DEFENCE, 32, null);
   }

   @Override
   public void calc(Env env) {
      if (env.getCharacter().isPlayer()) {
         Player p = env.getPlayer();
         if (!p.getInventory().isPaperdollSlotEmpty(14)) {
            env.subValue((double)p.getTemplate().getBaseDefBySlot(p.isTransformed() ? p.getTransformation().getBaseDefBySlot(p, 14) : 14));
         }

         if (!p.getInventory().isPaperdollSlotEmpty(13)) {
            env.subValue((double)p.getTemplate().getBaseDefBySlot(p.isTransformed() ? p.getTransformation().getBaseDefBySlot(p, 13) : 13));
         }

         if (!p.getInventory().isPaperdollSlotEmpty(9)) {
            env.subValue((double)p.getTemplate().getBaseDefBySlot(p.isTransformed() ? p.getTransformation().getBaseDefBySlot(p, 9) : 9));
         }

         if (!p.getInventory().isPaperdollSlotEmpty(8)) {
            env.subValue((double)p.getTemplate().getBaseDefBySlot(p.isTransformed() ? p.getTransformation().getBaseDefBySlot(p, 8) : 8));
         }

         if (!p.getInventory().isPaperdollSlotEmpty(4)) {
            env.subValue((double)p.getTemplate().getBaseDefBySlot(p.isTransformed() ? p.getTransformation().getBaseDefBySlot(p, 4) : 4));
         }

         env.mulValue(BaseStats.MEN.calcBonus(env.getPlayer()) * env.getPlayer().getLevelMod());
      } else if (env.getCharacter().isPet()) {
         if (env.getCharacter().getInventory().getPaperdollObjectId(4) != 0) {
            env.subValue(13.0);
            env.mulValue(BaseStats.MEN.calcBonus(env.getCharacter()) * env.getCharacter().getLevelMod());
         } else {
            env.mulValue(BaseStats.MEN.calcBonus(env.getCharacter()) * env.getCharacter().getLevelMod());
         }
      } else {
         env.mulValue(BaseStats.MEN.calcBonus(env.getCharacter()) * env.getCharacter().getLevelMod());
      }
   }
}
