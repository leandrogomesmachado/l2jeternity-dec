package l2e.scripts.hellbound;

import l2e.gameserver.GameTimeController;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.quest.Quest;

public class Shadai extends Quest {
   private static final int SHADAI = 32347;
   private static final int[] DAY_COORDS = new int[]{16882, 238952, 9776};
   private static final int[] NIGHT_COORDS = new int[]{9064, 253037, -1928};

   @Override
   public final String onSpawn(Npc npc) {
      if (!npc.isTeleporting()) {
         ThreadPoolManager.getInstance().scheduleAtFixedRate(new Shadai.ValidatePosition(npc), 60000L, 60000L);
      }

      return super.onSpawn(npc);
   }

   protected static void validatePosition(Npc npc) {
      int[] coords = DAY_COORDS;
      boolean mustRevalidate = false;
      if (npc.getX() != NIGHT_COORDS[0] && GameTimeController.getInstance().isNight()) {
         coords = NIGHT_COORDS;
         mustRevalidate = true;
      } else if (npc.getX() != DAY_COORDS[0] && !GameTimeController.getInstance().isNight()) {
         mustRevalidate = true;
      }

      if (mustRevalidate) {
         npc.getSpawn().setX(coords[0]);
         npc.getSpawn().setY(coords[1]);
         npc.getSpawn().setZ(coords[2]);
         npc.teleToLocation(coords[0], coords[1], coords[2], true);
      }
   }

   public Shadai(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addSpawnId(new int[]{32347});
   }

   public static void main(String[] args) {
      new Shadai(-1, "Shadai", "hellbound");
   }

   private static class ValidatePosition implements Runnable {
      private final Npc _npc;

      public ValidatePosition(Npc npc) {
         this._npc = npc;
      }

      @Override
      public void run() {
         Shadai.validatePosition(this._npc);
      }
   }
}
