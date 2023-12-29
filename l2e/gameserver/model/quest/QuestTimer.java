package l2e.gameserver.model.quest;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;

public class QuestTimer {
   protected static final Logger _log = Logger.getLogger(QuestTimer.class.getName());
   private boolean _isActive = true;
   private final String _name;
   private final Quest _quest;
   private final Npc _npc;
   private final Player _player;
   private final boolean _isRepeating;
   private ScheduledFuture<?> _schedular;
   private int _instanceId;

   public QuestTimer(Quest quest, String name, long time, Npc npc, Player player, boolean repeating) {
      this._name = name;
      this._quest = quest;
      this._player = player;
      this._npc = npc;
      this._isRepeating = repeating;
      if (npc != null) {
         this._instanceId = npc.getReflectionId();
      } else if (player != null) {
         this._instanceId = player.getReflectionId();
      }

      if (repeating) {
         this._schedular = ThreadPoolManager.getInstance().scheduleAtFixedRate(new QuestTimer.ScheduleTimerTask(), time, time);
      } else {
         this._schedular = ThreadPoolManager.getInstance().schedule(new QuestTimer.ScheduleTimerTask(), time);
      }
   }

   public QuestTimer(Quest quest, String name, long time, Npc npc, Player player) {
      this(quest, name, time, npc, player, false);
   }

   public QuestTimer(QuestState qs, String name, long time) {
      this(qs.getQuest(), name, time, null, qs.getPlayer(), false);
   }

   public void cancel() {
      this._isActive = false;
      if (this._schedular != null) {
         this._schedular.cancel(false);
      }
   }

   public void cancelAndRemove() {
      this.cancel();
      this._quest.removeQuestTimer(this);
   }

   public boolean isMatch(Quest quest, String name, Npc npc, Player player) {
      if (quest == null || name == null) {
         return false;
      } else if (quest == this._quest && name.equalsIgnoreCase(this.getName())) {
         return npc == this._npc && player == this._player;
      } else {
         return false;
      }
   }

   public final boolean getIsActive() {
      return this._isActive;
   }

   public final boolean getIsRepeating() {
      return this._isRepeating;
   }

   public final Quest getQuest() {
      return this._quest;
   }

   public final String getName() {
      return this._name;
   }

   public final Npc getNpc() {
      return this._npc;
   }

   public final Player getPlayer() {
      return this._player;
   }

   public final int getReflectionId() {
      return this._instanceId;
   }

   public final long getRemainDelay() {
      return this._schedular == null ? 0L : this._schedular.getDelay(TimeUnit.MILLISECONDS);
   }

   @Override
   public final String toString() {
      return this._name;
   }

   public class ScheduleTimerTask implements Runnable {
      @Override
      public void run() {
         if (QuestTimer.this.getIsActive()) {
            try {
               if (!QuestTimer.this.getIsRepeating()) {
                  QuestTimer.this.cancelAndRemove();
               }

               QuestTimer.this.getQuest().notifyEvent(QuestTimer.this.getName(), QuestTimer.this.getNpc(), QuestTimer.this.getPlayer());
            } catch (Exception var2) {
               QuestTimer._log.log(Level.SEVERE, "", (Throwable)var2);
            }
         }
      }
   }
}
