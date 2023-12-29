package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.SystemMessageId;

public class EquipmentSet extends Effect {
   private final int _level;
   private static final int[][] _classes = new int[][]{{4, 19, 32}, {11, 15, 26, 29, 39, 42}, {1, 54, 56}, {7, 22, 35}, {125, 126}, {45, 47}, {50}};
   private static final int[][] _equips = new int[][]{
      {15194, 15201, 16968},
      {15195, 15202, 16969},
      {15196, 15203, 16970},
      {15197, 15204, 16971},
      {15198, 15205, 16972},
      {15199, 15206, 16973},
      {15200, 15207, 16974}
   };

   public EquipmentSet(Env env, EffectTemplate template) {
      super(env, template);
      this._level = template.getParameters().getInteger("level", 1);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.NONE;
   }

   @Override
   public boolean onStart() {
      if (this.getEffected() == null || !this.getEffected().isPlayer()) {
         return false;
      } else if (this._level <= 0) {
         this.getEffected().sendPacket(SystemMessageId.NOTHING_INSIDE_THAT);
         return false;
      } else {
         Player player = this.getEffected().getActingPlayer();
         if (player != null) {
            ClassId plClass = ClassId.values()[player.isSubClassActive() ? player.getActiveClass() : player.getClassId().getId()];

            for(int i = 0; i < _classes.length; ++i) {
               for(int classId : _classes[i]) {
                  if (plClass.level() > 1) {
                     ClassId checkClass = ClassId.getClassId(classId);
                     if (plClass.childOf(checkClass)) {
                        int itemId = _equips[i][this._level - 1];
                        player.addItem("EquipmentSet", itemId, 1L, null, true);
                        return true;
                     }
                  } else if (plClass.getId() == classId) {
                     int itemId = _equips[i][this._level - 1];
                     player.addItem("EquipmentSet", itemId, 1L, null, true);
                     return true;
                  }
               }
            }
         }

         return true;
      }
   }
}
