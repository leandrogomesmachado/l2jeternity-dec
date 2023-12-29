package l2e.gameserver.model;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.data.parser.OptionsParser;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.options.Options;

public final class Augmentation {
   private int _effectsId = 0;
   private Augmentation.AugmentationStatBoni _boni = null;

   public Augmentation(int effects) {
      this._effectsId = effects;
      this._boni = new Augmentation.AugmentationStatBoni(this._effectsId);
   }

   public int getAttributes() {
      return this._effectsId;
   }

   public int getAugmentationId() {
      return this._effectsId;
   }

   public void applyBonus(Player player) {
      this._boni.applyBonus(player);
   }

   public void removeBonus(Player player) {
      this._boni.removeBonus(player);
   }

   public static class AugmentationStatBoni {
      private static final Logger _log = Logger.getLogger(Augmentation.AugmentationStatBoni.class.getName());
      private final List<Options> _options = new ArrayList<>();
      private boolean _active = false;

      public AugmentationStatBoni(int augmentationId) {
         int[] stats = new int[]{65535 & augmentationId, augmentationId >> 16};

         for(int stat : stats) {
            Options op = OptionsParser.getInstance().getOptions(stat);
            if (op != null) {
               this._options.add(op);
            } else {
               _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Couldn't find option: " + stat);
            }
         }
      }

      public void applyBonus(Player player) {
         if (!this._active) {
            for(Options op : this._options) {
               op.apply(player);
            }

            this._active = true;
         }
      }

      public void removeBonus(Player player) {
         if (this._active) {
            for(Options op : this._options) {
               op.remove(player);
            }

            this._active = false;
         }
      }
   }
}
