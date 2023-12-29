package l2e.gameserver.model.actor.instance;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.NpcSay;

public class MainMachineInstance extends NpcInstance {
   private int _powerUnits = 3;

   public MainMachineInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
   }

   @Override
   public void onBypassFeedback(Player player, String command) {
      if (this._powerUnits == 0) {
         this.broadcastPacket(new NpcSay(this.getObjectId(), 23, this.getId(), NpcStringId.FORTRESS_POWER_DISABLED));
         if (this.getFort().getSiege().getIsInProgress()) {
            this.onDecay();
            this.getFort().getSiege().disablePower(true);
            this.getFort().getSiege().checkCommanders();
         }
      }
   }

   public void powerOff(PowerControlUnitInstance powerUnit) {
      int totalSize = this.getFort().getSiege().getPowerUnits().size();
      int machineNumber = 3 - totalSize;
      NpcStringId msg = null;
      switch(machineNumber) {
         case 1:
            msg = NpcStringId.MACHINE_NO_1_POWER_OFF;
            break;
         case 2:
            msg = NpcStringId.MACHINE_NO_2_POWER_OFF;
            break;
         case 3:
            msg = NpcStringId.MACHINE_NO_3_POWER_OFF;
            break;
         default:
            throw new IllegalArgumentException("Wrong spawn at fortress: " + this.getFort().getName());
      }

      --this._powerUnits;
      this.broadcastPacket(new NpcSay(this.getObjectId(), 23, this.getId(), msg));
   }

   @Override
   public void showChatWindow(Player player, int val) {
      NpcHtmlMessage message = new NpcHtmlMessage(this.getObjectId());
      if (this._powerUnits != 0) {
         message.setFile(player, player.getLang(), "data/html/fortress/fortress_mainpower002.htm");
      } else {
         message.setFile(player, player.getLang(), "data/html/fortress/fortress_mainpower001.htm");
      }

      message.replace("%objectId%", String.valueOf(this.getObjectId()));
      player.sendPacket(message);
   }
}
