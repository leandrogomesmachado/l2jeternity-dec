package l2e.gameserver.network.serverpackets;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import l2e.gameserver.instancemanager.KrateisCubeManager;

public class ExPVPMatchCCRecord extends GameServerPacket {
   private final Map<String, Integer> _scores;
   private final int _state;

   public ExPVPMatchCCRecord(Map<String, Integer> scores) {
      this._scores = scores;
      this._state = 0;
   }

   public ExPVPMatchCCRecord(int state, KrateisCubeManager.CCPlayer[] players) {
      String[] SCBnames = KrateisCubeManager.scoreboardnames;
      Integer[][] SCBkills = KrateisCubeManager.scoreboardkills;
      this._scores = new HashMap<>();

      for(int i = 0; i <= 23; ++i) {
         if (SCBkills[i][1] < SCBkills[i + 1][1]) {
            String playername = SCBnames[i];
            SCBnames[i] = SCBnames[i + 1];
            SCBnames[i + 1] = playername;
            Integer kills = SCBkills[i][1];
            SCBkills[i][1] = SCBkills[i + 1][1];
            SCBkills[i + 1][1] = kills;
            kills = SCBkills[i][0];
            SCBkills[i][0] = SCBkills[i + 1][0];
            SCBkills[i + 1][0] = kills;
            i = 0;
         }
      }

      for(int i = 0; i <= 24; ++i) {
         if (SCBkills[i][0] > 0) {
            this._scores.put(SCBnames[i], SCBkills[i][1]);
         }
      }

      this._state = state;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._state);
      this.writeD(this._scores.size());

      for(Entry<String, Integer> p : this._scores.entrySet()) {
         this.writeS(p.getKey());
         this.writeD(p.getValue());
      }
   }
}
