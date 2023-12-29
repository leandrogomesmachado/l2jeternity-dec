package l2e.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.effects.Effect;

public class ExOlympiadSpelledInfo extends GameServerPacket {
   private final int _playerId;
   private final List<Effect> _effects = new ArrayList<>();

   public ExOlympiadSpelledInfo(Player player) {
      this._playerId = player.getObjectId();
   }

   public void addSkill(Effect info) {
      this._effects.add(info);
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._playerId);
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
