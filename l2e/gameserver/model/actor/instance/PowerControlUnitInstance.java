package l2e.gameserver.model.actor.instance;

import java.util.StringTokenizer;
import l2e.commons.util.Rnd;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import org.apache.commons.lang3.ArrayUtils;

public class PowerControlUnitInstance extends NpcInstance {
   public static final int LIMIT = 3;
   public static final int COND_NO_ENTERED = 0;
   public static final int COND_ENTERED = 1;
   public static final int COND_ALL_OK = 2;
   public static final int COND_FAIL = 3;
   public static final int COND_TIMEOUT = 4;
   private final int[] _generated = new int[3];
   private int _index;
   private int _tryCount;
   private long _invalidatePeriod;

   public PowerControlUnitInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
   }

   @Override
   public void onBypassFeedback(Player player, String command) {
      StringTokenizer token = new StringTokenizer(command);
      token.nextToken();
      int val = Integer.parseInt(token.nextToken());
      if (player.getClassId() != ClassId.warsmith && player.getClassId() != ClassId.maestro) {
         if (this._generated[this._index] == val) {
            ++this._index;
         } else {
            ++this._tryCount;
         }
      } else if (this._tryCount == 0) {
         ++this._tryCount;
      } else {
         ++this._index;
      }

      this.showChatWindow(player, 0);
   }

   @Override
   public void onSpawn() {
      super.onSpawn();
      this.generate();
   }

   @Override
   public void showChatWindow(Player player, int val) {
      NpcHtmlMessage message = new NpcHtmlMessage(this.getObjectId());
      if (this._invalidatePeriod > 0L && this._invalidatePeriod < System.currentTimeMillis()) {
         this.generate();
      }

      int cond = this.getCond();
      switch(cond) {
         case 0:
            message.setFile(player, player.getLang(), "data/html/fortress/fortress_inner_controller001.htm");
            break;
         case 1:
            message.setFile(player, player.getLang(), "data/html/fortress/fortress_inner_controller004.htm");
            message.replaceNpcString(
               "%password%",
               this._index == 0
                  ? NpcStringId.PASSWORD_HAS_NOT_BEEN_ENTERED
                  : (this._index == 1 ? NpcStringId.FIRST_PASSWORD_HAS_BEEN_ENTERED : NpcStringId.SECOND_PASSWORD_HAS_BEEN_ENTERED)
            );
            message.replaceNpcString("%try_count%", NpcStringId.ATTEMPT_S1_3_IS_IN_PROGRESS_THIS_IS_THE_THIRD_ATTEMPT_ON_S1, this._tryCount);
            break;
         case 2:
            message.setFile(player, player.getLang(), "data/html/fortress/fortress_inner_controller002.htm");
            if (this.getFort().getSiege().getIsInProgress()) {
               this.getFort().getSiege().killedPowerUnit(this);
               Spawner spawn = this.getFort().getSiege().getMainMachine().get(0);
               MainMachineInstance machineInstance = (MainMachineInstance)spawn.getLastSpawn();
               if (machineInstance != null) {
                  machineInstance.powerOff(this);
               }

               this.onDecay();
            }
            break;
         case 3:
            message.setFile(player, player.getLang(), "data/html/fortress/fortress_inner_controller003.htm");
            this._invalidatePeriod = System.currentTimeMillis() + 30000L;
            break;
         case 4:
            message.setFile(player, player.getLang(), "data/html/fortress/fortress_inner_controller003.htm");
      }

      message.replace("%objectId%", String.valueOf(this.getObjectId()));
      player.sendPacket(message);
   }

   private void generate() {
      this._invalidatePeriod = 0L;
      this._tryCount = 0;
      this._index = 0;

      for(int i = 0; i < this._generated.length; ++i) {
         this._generated[i] = -1;
      }

      int j = 0;

      while(j != 3) {
         int val = Rnd.get(0, 9);
         if (!ArrayUtils.contains(this._generated, val)) {
            this._generated[j++] = val;
         }
      }
   }

   private int getCond() {
      if (this._invalidatePeriod > System.currentTimeMillis()) {
         return 4;
      } else if (this._tryCount >= 3) {
         return 3;
      } else if (this._index == 0 && this._tryCount == 0) {
         return 0;
      } else {
         return this._index == 3 ? 2 : 1;
      }
   }

   public int[] getGenerated() {
      return this._generated;
   }
}
