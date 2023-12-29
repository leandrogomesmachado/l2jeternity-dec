package l2e.gameserver.model.skills.funcs.formulas;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.funcs.Func;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Stats;

public class FuncPDefMod extends Func {
   private static final FuncPDefMod _fmm_instance = new FuncPDefMod();

   public static Func getInstance() {
      return _fmm_instance;
   }

   private FuncPDefMod() {
      super(Stats.POWER_DEFENCE, 32, null);
   }

   @Override
   public void calc(Env env) {
      if (env.getCharacter().isPlayer()) {
         Player p = env.getPlayer();
         if (!p.getInventory().isPaperdollSlotEmpty(6)) {
            env.subValue(p.isTransformed() ? (double)p.getTransformation().getBaseDefBySlot(p, 6) : (double)p.getTemplate().getBaseDefBySlot(6));
         }

         if (!p.getInventory().isPaperdollSlotEmpty(11)
            || !p.getInventory().isPaperdollSlotEmpty(6) && p.getInventory().getPaperdollItem(6).getItem().getBodyPart() == 32768) {
            env.subValue((double)p.getTemplate().getBaseDefBySlot(p.isTransformed() ? p.getTransformation().getBaseDefBySlot(p, 11) : 11));
         }

         if (!p.getInventory().isPaperdollSlotEmpty(1)) {
            env.subValue((double)p.getTemplate().getBaseDefBySlot(p.isTransformed() ? p.getTransformation().getBaseDefBySlot(p, 1) : 1));
         }

         if (!p.getInventory().isPaperdollSlotEmpty(12)) {
            env.subValue((double)p.getTemplate().getBaseDefBySlot(p.isTransformed() ? p.getTransformation().getBaseDefBySlot(p, 12) : 12));
         }

         if (!p.getInventory().isPaperdollSlotEmpty(10)) {
            env.subValue((double)p.getTemplate().getBaseDefBySlot(p.isTransformed() ? p.getTransformation().getBaseDefBySlot(p, 10) : 10));
         }

         if (!p.getInventory().isPaperdollSlotEmpty(0)) {
            env.subValue((double)p.getTemplate().getBaseDefBySlot(p.isTransformed() ? p.getTransformation().getBaseDefBySlot(p, 0) : 0));
         }

         if (!p.getInventory().isPaperdollSlotEmpty(23)) {
            env.subValue((double)p.getTemplate().getBaseDefBySlot(p.isTransformed() ? p.getTransformation().getBaseDefBySlot(p, 23) : 23));
         }

         env.mulValue(p.getLevelMod());
      } else {
         env.mulValue(env.getCharacter().getLevelMod());
      }
   }
}
