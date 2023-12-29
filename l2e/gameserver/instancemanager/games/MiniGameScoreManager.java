package l2e.gameserver.instancemanager.games;

import java.util.Comparator;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListSet;
import l2e.gameserver.data.holder.CharMiniGameHolder;
import l2e.gameserver.model.actor.Player;

public class MiniGameScoreManager {
   private final NavigableSet<MiniGameScoreManager.MiniGameScore> _scores = new ConcurrentSkipListSet<>(new Comparator<MiniGameScoreManager.MiniGameScore>() {
      public int compare(MiniGameScoreManager.MiniGameScore o1, MiniGameScoreManager.MiniGameScore o2) {
         return o2.getScore() - o1.getScore();
      }
   });
   private static MiniGameScoreManager _instance = new MiniGameScoreManager();

   public static MiniGameScoreManager getInstance() {
      return _instance;
   }

   private MiniGameScoreManager() {
   }

   public void addScore(Player player, int score) {
      MiniGameScoreManager.MiniGameScore miniGameScore = null;

      for(MiniGameScoreManager.MiniGameScore $miniGameScore : this._scores) {
         if ($miniGameScore.getObjectId() == player.getObjectId()) {
            miniGameScore = $miniGameScore;
         }
      }

      if (miniGameScore == null) {
         this._scores.add(new MiniGameScoreManager.MiniGameScore(player.getObjectId(), player.getName(), score));
      } else {
         if (miniGameScore.getScore() > score) {
            return;
         }

         miniGameScore.setScore(score);
      }

      CharMiniGameHolder.getInstance().replace(player.getObjectId(), score);
   }

   public void addScore(int objectId, int score, String name) {
      this._scores.add(new MiniGameScoreManager.MiniGameScore(objectId, name, score));
   }

   public NavigableSet<MiniGameScoreManager.MiniGameScore> getScores() {
      return this._scores;
   }

   public static class MiniGameScore {
      private final int _objectId;
      private final String _name;
      private int _score;

      public MiniGameScore(int objectId, String name, int score) {
         this._objectId = objectId;
         this._name = name;
         this._score = score;
      }

      public int getObjectId() {
         return this._objectId;
      }

      public String getName() {
         return this._name;
      }

      public int getScore() {
         return this._score;
      }

      public void setScore(int score) {
         this._score = score;
      }

      @Override
      public boolean equals(Object o) {
         return o != null
            && o.getClass() == MiniGameScoreManager.MiniGameScore.class
            && ((MiniGameScoreManager.MiniGameScore)o).getObjectId() == this.getObjectId();
      }
   }
}
