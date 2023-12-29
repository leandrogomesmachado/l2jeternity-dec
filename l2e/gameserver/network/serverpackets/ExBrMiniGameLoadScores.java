package l2e.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import l2e.gameserver.instancemanager.games.MiniGameScoreManager;
import l2e.gameserver.model.actor.Player;

public class ExBrMiniGameLoadScores extends GameServerPacket {
   private int _place;
   private int _score;
   private int _lastScore;
   private final List<MiniGameScoreManager.MiniGameScore> _entries;

   public ExBrMiniGameLoadScores(Player player) {
      int i = 1;
      NavigableSet<MiniGameScoreManager.MiniGameScore> score = MiniGameScoreManager.getInstance().getScores();
      this._entries = new ArrayList<>(score.size() >= 100 ? 100 : score.size());
      MiniGameScoreManager.MiniGameScore last = score.isEmpty() ? null : score.last();
      if (last != null) {
         this._lastScore = last.getScore();
      }

      for(MiniGameScoreManager.MiniGameScore entry : score) {
         if (i > 100) {
            break;
         }

         if (entry.getObjectId() == player.getObjectId()) {
            this._place = i;
            this._score = entry.getScore();
         }

         this._entries.add(entry);
         ++i;
      }
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._place);
      this.writeD(this._score);
      this.writeD(this._entries.size());
      this.writeD(this._lastScore);

      for(int i = 0; i < this._entries.size(); ++i) {
         MiniGameScoreManager.MiniGameScore pair = this._entries.get(i);
         this.writeD(i + 1);
         this.writeS(pair.getName());
         this.writeD(pair.getScore());
      }
   }
}
