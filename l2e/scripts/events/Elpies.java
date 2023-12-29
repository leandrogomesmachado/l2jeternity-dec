package l2e.scripts.events;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import l2e.commons.util.Rnd;
import l2e.commons.util.TimeUtils;
import l2e.gameserver.Announcements;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.EventMonsterInstance;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.entity.events.AbstractWorldEvent;
import l2e.gameserver.model.entity.events.model.template.WorldEventDrop;
import l2e.gameserver.model.entity.events.model.template.WorldEventTemplate;
import l2e.gameserver.model.entity.events.model.template.WorldEventTerritory;
import l2e.gameserver.model.spawn.SpawnTerritory;
import l2e.gameserver.model.strings.server.ServerMessage;

public class Elpies extends AbstractWorldEvent {
   private boolean _isActive = false;
   private WorldEventTemplate _template = null;
   private ScheduledFuture<?> _startTask = null;
   private ScheduledFuture<?> _stopTask = null;
   private final List<Npc> _npcList = new ArrayList<>();
   private static int _elpy = 900100;
   private static int _option_howmuch;
   private static int _elpies_count = 0;
   private static boolean _canUseMagic;

   public Elpies(String name, String descr) {
      super(name, descr);
      this.addSpawnId(new int[]{_elpy});
      this.addKillId(_elpy);
      this._template = this.parseSettings(this.getName());
      if (this._template != null && !this._isActive) {
         _canUseMagic = this._template.getParams().getBool("canUseMagic", false);
         _option_howmuch = this._template.getParams().getInteger("totalAmount", 100);
         long expireTime = this.restoreStatus(this.getName());
         if (expireTime <= System.currentTimeMillis() && expireTime != -1L) {
            this.checkTimerTask(this.calcEventTime(this._template), true);
         } else {
            this.eventStart(expireTime == -1L ? -1L : expireTime - System.currentTimeMillis());
         }
      }
   }

   @Override
   public boolean isEventActive() {
      return this._isActive;
   }

   @Override
   public WorldEventTemplate getEventTemplate() {
      return this._template;
   }

   @Override
   public String onSpawn(Npc npc) {
      ((EventMonsterInstance)npc).eventSetDropOnGround(true);
      if (!_canUseMagic) {
         ((EventMonsterInstance)npc).eventSetBlockOffensiveSkills(true);
      }

      return super.onSpawn(npc);
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      if (this._isActive) {
         if (this._template.getDropList() != null && !this._template.getDropList().isEmpty()) {
            for(WorldEventDrop drop : this._template.getDropList()) {
               if (Rnd.chance(drop.getChance())) {
                  long amount = drop.getMinCount() != drop.getMaxCount() ? Rnd.get(drop.getMinCount(), drop.getMaxCount()) : drop.getMinCount();
                  ((MonsterInstance)npc).dropItem(killer, drop.getId(), (long)((int)amount));
               }
            }
         }

         --_elpies_count;
         if (_elpies_count <= 0) {
            ServerMessage msg = new ServerMessage("EventElpies.NO_MORE", true);
            Announcements.getInstance().announceToAll(msg);
            this.eventStop();
         }
      }

      return super.onKill(npc, killer, isSummon);
   }

   @Override
   public boolean eventStart(long totalTime) {
      if (!this._isActive && totalTime != 0L) {
         if (this._startTask != null) {
            this._startTask.cancel(false);
            this._startTask = null;
         }

         WorldEventTerritory t = this._template.getTerritories().get(Rnd.get(this._template.getTerritories().size()));
         if (t == null) {
            _log.info(this._template.getName() + ": Event can't be started, because territoty cant selected!");
         }

         SpawnTerritory territory = t.getTerritory();
         if (territory == null) {
            _log.info(this._template.getName() + ": Event can't be started, because territoty cant selected!");
            return false;
         } else {
            this._npcList.clear();
            this._isActive = true;
            _elpies_count = 0;

            for(int i = 0; i < _option_howmuch; ++i) {
               Location loc = territory.getRandomLoc(0, false);
               this._npcList.add(addSpawn(_elpy, loc.getX(), loc.getY(), loc.getZ(), 0, true, totalTime));
               ++_elpies_count;
            }

            ServerMessage msg1 = new ServerMessage("EventElpies.START_MSG_1", true);
            msg1.add(t.getName());
            Announcements.getInstance().announceToAll(msg1);
            ServerMessage msg2 = new ServerMessage("EventElpies.START_MSG_2", true);
            Announcements.getInstance().announceToAll(msg2);
            ServerMessage msg3 = new ServerMessage("EventElpies.START_MSG_3", true);
            msg3.add(totalTime / 60000L);
            Announcements.getInstance().announceToAll(msg3);
            this.updateStatus(this.getName(), totalTime == -1L ? -1L : totalTime + System.currentTimeMillis(), true);
            if (totalTime > 0L) {
               this._stopTask = ThreadPoolManager.getInstance().schedule(new Runnable() {
                  @Override
                  public void run() {
                     Elpies.this.eventStop();
                  }
               }, totalTime);
               _log.info("Event " + this._template.getName() + " will end in: " + TimeUtils.toSimpleFormat(System.currentTimeMillis() + totalTime));
            }

            return true;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean eventStop() {
      if (!this._isActive) {
         return false;
      } else {
         if (this._stopTask != null) {
            this._stopTask.cancel(false);
            this._stopTask = null;
         }

         this._isActive = false;
         if (!this._npcList.isEmpty()) {
            for(Npc _npc : this._npcList) {
               if (_npc != null) {
                  _npc.deleteMe();
               }
            }
         }

         this._npcList.clear();
         ServerMessage msg = new ServerMessage("EventElpies.STOP", true);
         Announcements.getInstance().announceToAll(msg);
         this.updateStatus(this.getName(), 0L, false);
         this.checkTimerTask(this.calcEventTime(this._template), false);
         return true;
      }
   }

   @Override
   public void startTimerTask(long time, final boolean checkZero) {
      if (this._startTask != null) {
         this._startTask.cancel(false);
         this._startTask = null;
      }

      this._startTask = ThreadPoolManager.getInstance().schedule(new Runnable() {
         @Override
         public void run() {
            Elpies.this.eventStart(checkZero ? -1L : (long)(Elpies.this._template.getPeriod() * 60000));
         }
      }, time - System.currentTimeMillis());
      _log.info("Event " + this._template.getName() + " will start in: " + TimeUtils.toSimpleFormat(time));
   }

   @Override
   public boolean isReloaded() {
      if (this.isEventActive()) {
         return false;
      } else {
         this._template = this.parseSettings(this.getName());
         if (this._template == null) {
            return false;
         } else {
            long expireTime = this.restoreStatus(this.getName());
            if (expireTime <= System.currentTimeMillis() && expireTime != -1L) {
               this.checkTimerTask(this.calcEventTime(this._template), true);
            } else {
               this.eventStart(expireTime == -1L ? -1L : expireTime - System.currentTimeMillis());
            }

            return true;
         }
      }
   }

   public static void main(String[] args) {
      new Elpies(Elpies.class.getSimpleName(), "events");
   }
}
