package l2e.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.model.TimeStamp;
import l2e.gameserver.model.actor.Player;

public class SkillCoolTime extends GameServerPacket {
   private final List<TimeStamp> _skillReuseTimeStamps = new ArrayList<>();

   public SkillCoolTime(Player cha) {
      for(TimeStamp ts : cha.getSkillReuseTimeStamps().values()) {
         if (ts.hasNotPassed()) {
            this._skillReuseTimeStamps.add(ts);
         }
      }
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._skillReuseTimeStamps.size());

      for(TimeStamp ts : this._skillReuseTimeStamps) {
         this.writeD(ts.getSkillId());
         this.writeD(ts.getSkillLvl());
         this.writeD((int)ts.getReuse() / 1000);
         this.writeD((int)ts.getRemaining() / 1000);
      }
   }
}
