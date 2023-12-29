package l2e.gameserver.model;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;
import l2e.commons.threading.RunnableImpl;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.holder.SpawnHolder;
import l2e.gameserver.data.parser.SpawnParser;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.spawn.Spawner;

public final class WorldRegion {
   private static Logger _log = Logger.getLogger(WorldRegion.class.getName());
   private final Map<Integer, Playable> _allPlayable = new ConcurrentHashMap<>();
   private final Map<Integer, GameObject> _visibleObjects = new ConcurrentHashMap<>();
   private final int _tileX;
   private final int _tileY;
   private final int _tileZ;
   private boolean _active;
   private ScheduledFuture<?> _switchTask = null;

   public WorldRegion(int pTileX, int pTileY, int pTileZ) {
      this._tileX = pTileX;
      this._tileY = pTileY;
      this._tileZ = pTileZ;
   }

   public void switchActive(boolean value) {
      if (!value) {
         try {
            if (this._switchTask != null) {
               this._switchTask.cancel(false);
               this._switchTask = null;
            }

            this._switchTask = ThreadPoolManager.getInstance().schedule(new WorldRegion.SwitchTask(value), 60000L);
         } catch (Exception var3) {
         }
      } else {
         this.setActive(value);
      }
   }

   private void setActive(boolean value) {
      if (this._active != value) {
         this._active = value;
         if (!value) {
            for(GameObject o : this._visibleObjects.values()) {
               if (o instanceof Attackable) {
                  Attackable mob = (Attackable)o;
                  if (!mob.isDead() && !mob.isGlobalAI() && (mob.getLeader() == null || mob.getLeader().isVisible())) {
                     mob.setTarget(null);
                     mob.stopMove(null);
                     mob.stopAllEffects();
                     mob.clearAggroList();
                     mob.getAttackByList().clear();
                     if (mob.hasAI()) {
                        mob.getAI().setIntention(CtrlIntention.IDLE);
                        mob.getAI().stopAITask();
                     }
                  }
               }
            }
         } else {
            for(GameObject o : this._visibleObjects.values()) {
               if (o instanceof Attackable) {
                  ((Attackable)o).getStatus().startHpMpRegeneration();
               } else if (o instanceof Npc) {
                  ((Npc)o).startRandomAnimationTimer();
               }
            }
         }
      }
   }

   public boolean isActive() {
      return this._active;
   }

   public boolean isEmptyNeighborhood() {
      for(WorldRegion neighbor : this.getNeighbors()) {
         if (neighbor.getVisiblePlayable().size() != 0) {
            return false;
         }
      }

      return true;
   }

   public void addVisibleObject(GameObject object) {
      if (object != null) {
         this._visibleObjects.put(object.getObjectId(), object);
         if (object.isPlayable()) {
            this._allPlayable.put(object.getObjectId(), (Playable)object);
         }
      }
   }

   public void removeVisibleObject(GameObject object) {
      if (object != null) {
         this._visibleObjects.remove(object.getObjectId());
         if (object.isPlayable()) {
            this._allPlayable.remove(object.getObjectId());
         }
      }
   }

   public List<WorldRegion> getNeighbors() {
      return World.getInstance().getNeighbors(this._tileX, this._tileY, this._tileZ, 1, 1);
   }

   public List<WorldRegion> getNeighbors(int deep, int deepV) {
      return World.getInstance().getNeighbors(this._tileX, this._tileY, this._tileZ, deep, deepV);
   }

   public Map<Integer, Playable> getVisiblePlayable() {
      return this._allPlayable;
   }

   public Map<Integer, GameObject> getVisibleObjects() {
      return this._visibleObjects;
   }

   public String getName() {
      return "(" + this._tileX + ", " + this._tileY + ", " + this._tileZ + ")";
   }

   public void deleteVisibleNpcSpawns() {
      _log.fine("Deleting all visible NPC's in Region: " + this.getName());

      for(GameObject obj : this.getVisibleObjects().values()) {
         if (obj instanceof Npc) {
            Npc target = (Npc)obj;
            target.deleteMe();
            Spawner spawn = target.getSpawn();
            if (spawn != null) {
               spawn.stopRespawn();
               SpawnParser.getInstance().deleteSpawn(spawn);
               SpawnHolder.getInstance().deleteSpawn(spawn, false);
            }
         }
      }

      _log.info("All visible NPC's deleted in Region: " + this.getName());
   }

   private class SwitchTask extends RunnableImpl {
      boolean _value;

      public SwitchTask(boolean value) {
         this._value = value;
      }

      @Override
      public void runImpl() {
         if (WorldRegion.this.isEmptyNeighborhood()) {
            WorldRegion.this.setActive(this._value);
         }
      }
   }
}
