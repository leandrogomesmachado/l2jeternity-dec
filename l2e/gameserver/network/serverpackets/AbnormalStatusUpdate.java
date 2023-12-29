package l2e.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.model.skills.effects.Effect;

public class AbnormalStatusUpdate extends GameServerPacket {
   private final List<Effect> _effects = new ArrayList<>();

   public void addSkill(Effect info) {
      if (!info.getSkill().isHealingPotionSkill()) {
         this._effects.add(info);
      }
   }

   @Override
   protected final void writeImpl() {
      this.writeH(this._effects.size());

      for(Effect info : this._effects) {
         if (info != null && info.isInUse()) {
            this.writeD(info.getSkill().getDisplayId());
            this.writeH(info.getSkill().getDisplayLevel());
            this.writeD(info.getTimeLeft());
         }
      }
   }
}
