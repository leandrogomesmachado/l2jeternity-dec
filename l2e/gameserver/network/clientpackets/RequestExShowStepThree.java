package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.petition.PetitionMainGroup;
import l2e.gameserver.model.petition.PetitionSection;
import l2e.gameserver.network.serverpackets.ExResponseShowContents;

public class RequestExShowStepThree extends GameClientPacket {
   private int _subId;

   @Override
   protected void readImpl() {
      this._subId = this.readC();
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         PetitionMainGroup group = player.getPetitionGroup();
         if (group != null) {
            PetitionSection section = group.getSubGroup(this._subId);
            if (section != null) {
               player.sendPacket(new ExResponseShowContents(section.getDescription(player.getLang())));
            }
         }
      }
   }
}
