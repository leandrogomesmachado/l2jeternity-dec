package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;

public final class RequestSkillList extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         player.isntAfk();
         player.sendSkillList(false);
      }
   }

   @Override
   protected boolean triggersOnActionRequest() {
      return false;
   }
}
