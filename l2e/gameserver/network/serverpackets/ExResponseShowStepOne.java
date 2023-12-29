package l2e.gameserver.network.serverpackets;

import java.util.Collection;
import l2e.gameserver.data.parser.PetitionGroupParser;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.petition.PetitionMainGroup;

public class ExResponseShowStepOne extends GameServerPacket {
   private final String _lang;

   public ExResponseShowStepOne(Player player) {
      this._lang = player.getLang();
   }

   @Override
   protected void writeImpl() {
      Collection<PetitionMainGroup> petitionGroups = PetitionGroupParser.getInstance().getPetitionGroups();
      this.writeD(petitionGroups.size());

      for(PetitionMainGroup group : petitionGroups) {
         this.writeC(group.getId());
         this.writeS(group.getName(this._lang));
      }
   }
}
