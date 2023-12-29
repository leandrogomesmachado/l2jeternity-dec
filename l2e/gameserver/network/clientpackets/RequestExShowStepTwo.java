package l2e.gameserver.network.clientpackets;

import l2e.gameserver.data.parser.PetitionGroupParser;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.petition.PetitionMainGroup;
import l2e.gameserver.network.serverpackets.ExResponseShowStepTwo;

public class RequestExShowStepTwo extends GameClientPacket {
   private int _petitionGroupId;

   @Override
   protected void readImpl() {
      this._petitionGroupId = this.readC();
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         PetitionMainGroup group = PetitionGroupParser.getInstance().getPetitionGroup(this._petitionGroupId);
         if (group != null) {
            player.setPetitionGroup(group);
            player.sendPacket(new ExResponseShowStepTwo(player, group));
         }
      }
   }
}
