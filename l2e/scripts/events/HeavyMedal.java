package l2e.scripts.events;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import l2e.commons.util.TimeUtils;
import l2e.gameserver.Announcements;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.events.AbstractWorldEvent;
import l2e.gameserver.model.entity.events.EventsDropManager;
import l2e.gameserver.model.entity.events.model.template.WorldEventReward;
import l2e.gameserver.model.entity.events.model.template.WorldEventSpawn;
import l2e.gameserver.model.entity.events.model.template.WorldEventTemplate;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.strings.server.ServerMessage;

public class HeavyMedal extends AbstractWorldEvent {
   private boolean _isActive = false;
   private WorldEventTemplate _template = null;
   private ScheduledFuture<?> _startTask = null;
   private ScheduledFuture<?> _stopTask = null;
   private final List<Npc> _npcList = new ArrayList<>();

   public HeavyMedal(String name, String descr) {
      super(name, descr);
      this.addStartNpc(new int[]{31228, 31229});
      this.addTalkId(new int[]{31228, 31229});
      this.addFirstTalkId(new int[]{31228, 31229});
      this._template = this.parseSettings(this.getName());
      if (this._template != null && !this._isActive) {
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
   public boolean eventStart(long totalTime) {
      if (!this._isActive && totalTime != 0L) {
         if (this._startTask != null) {
            this._startTask.cancel(false);
            this._startTask = null;
         }

         this._isActive = true;
         List<WorldEventSpawn> spawnList = this._template.getSpawnList();
         if (spawnList != null && !spawnList.isEmpty()) {
            for(WorldEventSpawn spawn : spawnList) {
               this._npcList
                  .add(
                     addSpawn(
                        spawn.getNpcId(),
                        spawn.getLocation().getX(),
                        spawn.getLocation().getY(),
                        spawn.getLocation().getZ(),
                        spawn.getLocation().getHeading(),
                        false,
                        0L
                     )
                  );
            }
         }

         if (this._template.getDropList() != null && !this._template.getDropList().isEmpty()) {
            EventsDropManager.getInstance().addRule(this._template.getId(), this._template.getDropList(), true);
         }

         ServerMessage msg = new ServerMessage("EventHeavyMedal.START", true);
         Announcements.getInstance().announceToAll(msg);
         this.updateStatus(this.getName(), totalTime == -1L ? -1L : totalTime + System.currentTimeMillis(), true);
         if (totalTime > 0L) {
            this._stopTask = ThreadPoolManager.getInstance().schedule(new Runnable() {
               @Override
               public void run() {
                  HeavyMedal.this.eventStop();
               }
            }, totalTime);
            _log.info("Event " + this._template.getName() + " will end in: " + TimeUtils.toSimpleFormat(System.currentTimeMillis() + totalTime));
         }

         return true;
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
         EventsDropManager.getInstance().removeRule(this._template.getId());
         ServerMessage msg = new ServerMessage("EventHeavyMedal.STOP", true);
         Announcements.getInstance().announceToAll(msg);
         this.updateStatus(this.getName(), 0L, false);
         this.checkTimerTask(this.calcEventTime(this._template), false);
         return true;
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      int level = this.checkLevel(st);
      if (event.equalsIgnoreCase("game")) {
         boolean haveItems = true;
         List<WorldEventReward> requestItems = this._template.getVariantRequests().get(level);
         if (requestItems != null && !requestItems.isEmpty()) {
            for(WorldEventReward request : requestItems) {
               if (st.getQuestItemsCount(request.getId()) < request.getMinCount()) {
                  haveItems = false;
               }
            }
         }

         htmltext = !haveItems ? "31229-no.htm" : "31229-game.htm";
      } else if (event.equalsIgnoreCase("heads") || event.equalsIgnoreCase("tails")) {
         boolean haveItems = true;
         List<WorldEventReward> requestItems = this._template.getVariantRequests().get(level);
         if (requestItems != null && !requestItems.isEmpty()) {
            for(WorldEventReward request : requestItems) {
               if (st.getQuestItemsCount(request.getId()) < request.getMinCount()) {
                  haveItems = false;
               }
            }
         }

         if (!haveItems) {
            htmltext = "31229-" + event.toLowerCase() + "-10.htm";
         } else {
            for(WorldEventReward request : requestItems) {
               st.takeItems(request.getId(), request.getMinCount());
            }

            if (getRandom(100) > 50) {
               level = 0;
            } else {
               if (level > 0) {
                  List<WorldEventReward> rewards = this._template.getVariantRewards().get(level - 1);
                  if (rewards != null && !rewards.isEmpty()) {
                     for(WorldEventReward reward : rewards) {
                        if (reward != null) {
                           st.takeItems(reward.getId(), -1L);
                        }
                     }
                  }
               }

               calcReward(player, this._template, level);
               st.playSound(Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET);
               ++level;
            }

            htmltext = "31229-" + event.toLowerCase() + "-" + level + ".htm";
         }
      } else if (event.equalsIgnoreCase("talk")) {
         htmltext = npc.getId() + "-lvl-" + level + ".htm";
      }

      return htmltext;
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      if (player.getQuestState(this.getName()) == null) {
         this.newQuestState(player);
      }

      return npc.getId() + ".htm";
   }

   public int checkLevel(QuestState st) {
      int lvl = 0;
      if (st == null) {
         return 0;
      } else {
         if (st.hasQuestItems(6402)) {
            lvl = 4;
         } else if (st.hasQuestItems(6401)) {
            lvl = 3;
         } else if (st.hasQuestItems(6400)) {
            lvl = 2;
         } else if (st.hasQuestItems(6399)) {
            lvl = 1;
         }

         return lvl;
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
            HeavyMedal.this.eventStart(checkZero ? -1L : (long)(HeavyMedal.this._template.getPeriod() * 3600000));
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
      new HeavyMedal(HeavyMedal.class.getSimpleName(), "events");
   }
}
