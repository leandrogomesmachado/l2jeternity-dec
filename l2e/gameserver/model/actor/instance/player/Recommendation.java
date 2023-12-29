package l2e.gameserver.model.actor.instance.player;

import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;
import l2e.commons.threading.RunnableImpl;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectType;

public class Recommendation {
   public static final int[][] REC_BONUS = new int[][]{
      {25, 50, 50, 50, 50, 50, 50, 50, 50, 50},
      {16, 33, 50, 50, 50, 50, 50, 50, 50, 50},
      {12, 25, 37, 50, 50, 50, 50, 50, 50, 50},
      {10, 20, 30, 40, 50, 50, 50, 50, 50, 50},
      {8, 16, 25, 33, 41, 50, 50, 50, 50, 50},
      {7, 14, 21, 28, 35, 42, 50, 50, 50, 50},
      {6, 12, 18, 25, 31, 37, 43, 50, 50, 50},
      {5, 11, 16, 22, 27, 33, 38, 44, 50, 50},
      {5, 10, 15, 20, 25, 30, 35, 40, 45, 50},
      {5, 10, 15, 20, 25, 30, 35, 40, 45, 50},
      {5, 10, 15, 20, 25, 30, 35, 40, 45, 50}
   };
   private final Player _owner;
   public ScheduledFuture<?> _recVoteTask;
   private int _recomHave;
   private int _recomLeft;
   private int _recomTimeLeft;
   private long _recomBonusStart;

   public Recommendation(Player player) {
      this._owner = player;
   }

   private Player getPlayer() {
      return this._owner;
   }

   public void checkRecom() {
      Player player = this.getPlayer();
      if (player != null) {
         if (this._recVoteTask != null) {
            this._recVoteTask.cancel(false);
         }

         Calendar temp = Calendar.getInstance();
         temp.set(11, 6);
         temp.set(12, 30);
         temp.set(13, 0);
         long count = (long)Math.round((float)((System.currentTimeMillis() - player.getLastAccess()) / 1000L / 86400L));
         if (count == 0L && player.getLastAccess() < temp.getTimeInMillis() && System.currentTimeMillis() > temp.getTimeInMillis()) {
            ++count;
         }

         int time = 0;
         if (count != 0L) {
            this.setRecomLeft(20);
            this.setRecomTimeLeft(3600);
            int have = this.getRecomHave();

            for(int i = 0; (long)i < count; ++i) {
               have -= 20;
            }

            if (have < 0) {
               have = 0;
            }

            this.setRecomHave(have);
            time = 2;
         }

         this.updateVoteInfo();
         if (Config.ALLOW_RECO_BONUS_SYSTEM) {
            this._recVoteTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Recommendation.RecVoteTask(time), 3600000L, 3600000L);
         }
      }
   }

   public void restartRecom() {
      Player player = this.getPlayer();
      if (player != null) {
         try {
            this.setRecomLeft(20);
            this.setRecomTimeLeft(3600);
            this._recomHave -= 20;
            if (this._recomHave < 0) {
               this._recomHave = 0;
            }

            if (this._recVoteTask != null) {
               this._recVoteTask.cancel(false);
            }

            this.updateVoteInfo();
            if (Config.ALLOW_RECO_BONUS_SYSTEM) {
               this._recVoteTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Recommendation.RecVoteTask(2), 3600000L, 3600000L);
            }
         } catch (Exception var3) {
         }
      }
   }

   public void startRecBonus() {
      if (!this.isRecBonusActive() && this.isHourglassBonusActive() <= 0L && Config.ALLOW_RECO_BONUS_SYSTEM) {
         Player player;
         if (this.getRecomTimeLeft() != 0 && ((player = this.getPlayer()) == null || !player.isInZonePeace())) {
            this._recomBonusStart = System.currentTimeMillis();
            this.updateVoteInfo();
         } else {
            this.stopRecBonus();
         }
      }
   }

   public void stopRecBonus() {
      if (this.isRecBonusActive()) {
         this._recomTimeLeft = this.getRecomTimeLeft();
         this._recomBonusStart = 0L;
         this.updateVoteInfo();
      }
   }

   public boolean isRecBonusActive() {
      return this._recomBonusStart != 0L;
   }

   public void updateVoteInfo() {
      Player player = this.getPlayer();
      if (player != null) {
         player.sendUserInfo(true);
         player.sendVoteSystemInfo();
      }
   }

   public void addRecomHave(int value) {
      this.setRecomHave(this.getRecomHave() + value);
      this.updateVoteInfo();
   }

   public void addRecomLeft(int value) {
      this.setRecomLeft(this.getRecomLeft() + value);
      this.updateVoteInfo();
   }

   public void giveRecom(Player target) {
      int targetRecom = target.getRecommendation().getRecomHave();
      if (targetRecom < 255) {
         target.getRecommendation().setRecomHave(targetRecom + 1);
         target.getCounters().addAchivementInfo("recomHave", 0, -1L, false, false, false);
         target.getRecommendation().updateVoteInfo();
      }

      if (this._recomLeft > 0) {
         if (this.getPlayer() != null) {
            this.getPlayer().getCounters().addAchivementInfo("recomLeft", 0, -1L, false, false, false);
         }

         --this._recomLeft;
      }
   }

   public int getRecomHave() {
      return this._recomHave;
   }

   public void setRecomHave(int value) {
      this._recomHave = Math.max(Math.min(value, 255), 0);
   }

   public int getRecomLeft() {
      return this._recomLeft;
   }

   public void setRecomLeft(int value) {
      this._recomLeft = Math.max(Math.min(value, 255), 0);
   }

   public int getRecomTimeLeft() {
      return this.isRecBonusActive()
         ? Math.max(this._recomTimeLeft - (int)(System.currentTimeMillis() - this._recomBonusStart) / 1000, 0)
         : this._recomTimeLeft;
   }

   public void setRecomTimeLeft(int value) {
      this._recomTimeLeft = value;
   }

   public long isHourglassBonusActive() {
      Player player = this.getPlayer();
      if (player != null && Config.ALLOW_RECO_BONUS_SYSTEM) {
         if (player.isOnline()) {
            Effect effect = player.getEffectList().getFirstEffect(EffectType.NEVIT_HOURGLASS);
            if (effect != null) {
               return (long)effect.getTimeLeft();
            }
         }

         return 0L;
      } else {
         return 0L;
      }
   }

   public int getRecomExpBonus() {
      Player player = this.getPlayer();
      if (player != null && Config.ALLOW_RECO_BONUS_SYSTEM) {
         if (this.isHourglassBonusActive() <= 0L) {
            if (this.getRecomTimeLeft() <= 0) {
               return 0;
            }

            if (player.getLevel() < 1) {
               return 0;
            }

            if (this.getRecomHave() < 1) {
               return 0;
            }
         }

         if (this.getRecomHave() >= 100) {
            return 50;
         } else {
            int lvl = player.getLevel() / 10;
            int exp = (Math.min(100, this.getRecomHave()) - 1) / 10;
            return REC_BONUS[lvl][exp];
         }
      } else {
         return 0;
      }
   }

   public double getRecoMultiplier() {
      double multiplier = 0.0;
      double bonus = (double)this.getRecomExpBonus();
      if (bonus > 0.0) {
         multiplier += bonus / 100.0;
      }

      return multiplier;
   }

   public void stopRecomendationTask() {
      Player player = this.getPlayer();
      if (player != null) {
         if (this._recVoteTask != null) {
            this._recVoteTask.cancel(false);
         }
      }
   }

   private class RecVoteTask extends RunnableImpl {
      private int _time;

      public RecVoteTask(int time) {
         this._time = time;
      }

      @Override
      public void runImpl() {
         if (this._time > 0) {
            Recommendation.this.addRecomLeft(10);
            --this._time;
         } else {
            Recommendation.this.addRecomLeft(1);
         }
      }
   }
}
