package l2e.gameserver.taskmanager;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.CubicInstance;
import l2e.gameserver.network.serverpackets.AutoAttackStop;

public class AttackStanceTaskManager {
   protected static final Logger _log = Logger.getLogger(AttackStanceTaskManager.class.getName());
   protected static final Map<Creature, Long> _attackStanceTasks = new ConcurrentHashMap<>();

   protected AttackStanceTaskManager() {
      ThreadPoolManager.getInstance().scheduleAtFixedRate(new AttackStanceTaskManager.FightModeScheduler(), 0L, 1000L);
   }

   public void addAttackStanceTask(Creature actor) {
      if (actor != null) {
         if (actor.isPlayable()) {
            Player player = actor.getActingPlayer();

            for(CubicInstance cubic : player.getCubics().values()) {
               if (cubic.getId() != 3) {
                  cubic.doAction();
               }
            }
         }

         _attackStanceTasks.put(actor, System.currentTimeMillis());
      }
   }

   public void removeAttackStanceTask(Creature actor) {
      if (actor != null) {
         if (actor.isSummon()) {
            actor = actor.getActingPlayer();
         }

         _attackStanceTasks.remove(actor);
      }
   }

   public boolean hasAttackStanceTask(Creature actor) {
      if (actor != null) {
         if (actor.isSummon()) {
            actor = actor.getActingPlayer();
         }

         return _attackStanceTasks.containsKey(actor);
      } else {
         return false;
      }
   }

   public static AttackStanceTaskManager getInstance() {
      return AttackStanceTaskManager.SingletonHolder._instance;
   }

   protected class FightModeScheduler implements Runnable {
      @Override
      public void run() {
         long current = System.currentTimeMillis();

         try {
            Iterator<Entry<Creature, Long>> iter = AttackStanceTaskManager._attackStanceTasks.entrySet().iterator();

            while(iter.hasNext()) {
               Entry<Creature, Long> e = iter.next();
               if (current - e.getValue() > 15000L) {
                  Creature actor = e.getKey();
                  if (actor != null) {
                     actor.broadcastPacket(new AutoAttackStop(actor.getObjectId()));
                     actor.getAI().setAutoAttacking(false);
                     if (actor.isPlayer() && actor.hasSummon()) {
                        actor.getSummon().broadcastPacket(new AutoAttackStop(actor.getSummon().getObjectId()));
                     }
                  }

                  iter.remove();
               }
            }
         } catch (Exception var6) {
            AttackStanceTaskManager._log.log(Level.WARNING, "Error in FightModeScheduler: " + var6.getMessage(), (Throwable)var6);
         }
      }
   }

   private static class SingletonHolder {
      protected static final AttackStanceTaskManager _instance = new AttackStanceTaskManager();
   }
}
