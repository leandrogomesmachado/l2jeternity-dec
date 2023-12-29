package l2e.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.effects.Effect;

public class PartySpelled extends GameServerPacket {
   private final List<Effect> _effects = new ArrayList<>();
   private final Creature _activeChar;

   public PartySpelled(Creature cha) {
      this._activeChar = cha;
   }

   public void addSkill(Effect info) {
      this._effects.add(info);
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._activeChar.isServitor() ? 2 : (this._activeChar.isPet() ? 1 : 0));
      this.writeD(this._activeChar.getObjectId());
      this.writeD(this._effects.size());

      for(Effect info : this._effects) {
         if (info != null && info.isInUse()) {
            this.writeD(info.getSkill().getDisplayId());
            this.writeH(info.getSkill().getDisplayLevel());
            this.writeD(info.getTimeLeft());
         }
      }
   }
}
