package l2e.gameserver.network.serverpackets;

import java.util.Collection;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.petition.PetitionMainGroup;
import l2e.gameserver.model.petition.PetitionSection;

public class ExResponseShowStepTwo extends GameServerPacket {
   private final String _lang;
   private final PetitionMainGroup _petitionMainGroup;

   public ExResponseShowStepTwo(Player player, PetitionMainGroup gr) {
      this._lang = player.getLang();
      this._petitionMainGroup = gr;
   }

   @Override
   protected void writeImpl() {
      Collection<PetitionSection> sections = this._petitionMainGroup.getSubGroups();
      this.writeD(sections.size());
      this.writeS(this._petitionMainGroup.getDescription(this._lang));

      for(PetitionSection g : sections) {
         this.writeC(g.getId());
         this.writeS(g.getName(this._lang));
      }
   }
}
