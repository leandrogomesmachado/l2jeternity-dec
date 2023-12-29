package l2e.gameserver.handler.communityhandlers.impl.model;

import l2e.commons.util.ArrayUtils;
import l2e.gameserver.model.actor.Player;

public class CertificationUtils {
   private static final int[] WARRIORCLASSES = new int[]{3, 88, 2, 89, 46, 48, 113, 114, 55, 117, 56, 118, 127, 131, 128, 129, 132, 133};
   private static final int[] ROGUECLASSES = new int[]{9, 92, 24, 102, 37, 109, 130, 134, 8, 93, 23, 101, 36, 108};
   private static final int[] KNIGHTCLASSES = new int[]{5, 90, 6, 91, 20, 99, 33, 106};
   private static final int[] SUMMONERCLASSES = new int[]{14, 96, 28, 104, 41, 111};
   private static final int[] WIZARDCLASSES = new int[]{12, 94, 13, 95, 27, 103, 40, 110};
   private static final int[] HEALERCLASSES = new int[]{16, 97, 30, 105, 43, 112};
   private static final int[] ENCHANTERCLASSES = new int[]{17, 98, 21, 100, 34, 107, 51, 115, 52, 116, 135, 136};

   public static int getClassIndex(Player player) {
      if (ArrayUtils.isIntInArray(player.getClassId().getId(), WARRIORCLASSES)) {
         return 0;
      } else if (ArrayUtils.isIntInArray(player.getClassId().getId(), KNIGHTCLASSES)) {
         return 1;
      } else if (ArrayUtils.isIntInArray(player.getClassId().getId(), ROGUECLASSES)) {
         return 2;
      } else if (ArrayUtils.isIntInArray(player.getClassId().getId(), ENCHANTERCLASSES)) {
         return 3;
      } else if (ArrayUtils.isIntInArray(player.getClassId().getId(), WIZARDCLASSES)) {
         return 4;
      } else if (ArrayUtils.isIntInArray(player.getClassId().getId(), SUMMONERCLASSES)) {
         return 5;
      } else {
         return ArrayUtils.isIntInArray(player.getClassId().getId(), HEALERCLASSES) ? 6 : -1;
      }
   }
}
