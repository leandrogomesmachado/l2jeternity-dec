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
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class MasterOfEnchanting extends AbstractWorldEvent {
   private boolean _isActive = false;
   private WorldEventTemplate _template = null;
   private ScheduledFuture<?> _startTask = null;
   private ScheduledFuture<?> _stopTask = null;
   private final List<Npc> _npcList = new ArrayList<>();

   public MasterOfEnchanting(String name, String descr) {
      super(name, descr);
      this.addStartNpc(32599);
      this.addFirstTalkId(32599);
      this.addTalkId(32599);
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

         ServerMessage msg = new ServerMessage("EventMasterOfEnchanting.START", true);
         Announcements.getInstance().announceToAll(msg);
         this.updateStatus(this.getName(), totalTime == -1L ? -1L : totalTime + System.currentTimeMillis(), true);
         if (totalTime > 0L) {
            this._stopTask = ThreadPoolManager.getInstance().schedule(new Runnable() {
               @Override
               public void run() {
                  MasterOfEnchanting.this.eventStop();
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
         ServerMessage msg = new ServerMessage("EventMasterOfEnchanting.STOP", true);
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
      if (event.equalsIgnoreCase("buy_staff")) {
         List<WorldEventReward> rewards = this._template.getVariantRewards().get(13539);
         if (rewards != null && !rewards.isEmpty()) {
            for(WorldEventReward reward : rewards) {
               if (reward != null && st.hasQuestItems(reward.getId())) {
                  return "32599-staffcant.htm";
               }
            }
         }

         if (isTakeRequestItems(player, this._template, 13539)) {
            calcReward(player, this._template, 13539);
            htmltext = "32599-staffbuyed.htm";
         } else {
            htmltext = "32599-staffcant.htm";
         }
      } else if (event.equalsIgnoreCase("buy_scroll_24")) {
         long _curr_time = System.currentTimeMillis();
         String value = this.loadGlobalQuestVar(player.getAccountName());
         long _reuse_time = value == "" ? 0L : Long.parseLong(value);
         if (_curr_time > _reuse_time) {
            if (isTakeRequestItems(player, this._template, 13540)) {
               calcReward(player, this._template, 13540);
               this.saveGlobalQuestVar(player.getAccountName(), Long.toString(System.currentTimeMillis() + 21600000L));
               htmltext = "32599-scroll24.htm";
            } else {
               htmltext = "32599-s24-no.htm";
            }
         } else {
            long _remaining_time = (_reuse_time - _curr_time) / 1000L;
            int hours = (int)_remaining_time / 3600;
            int minutes = (int)_remaining_time % 3600 / 60;
            if (hours > 0) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.ITEM_PURCHASABLE_IN_S1_HOURS_S2_MINUTES);
               sm.addNumber(hours);
               sm.addNumber(minutes);
               player.sendPacket(sm);
               htmltext = "32599-scroll24.htm";
            } else if (minutes > 0) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.ITEM_PURCHASABLE_IN_S1_MINUTES);
               sm.addNumber(minutes);
               player.sendPacket(sm);
               htmltext = "32599-scroll24.htm";
            } else if (isTakeRequestItems(player, this._template, 13540)) {
               calcReward(player, this._template, 13540);
               this.saveGlobalQuestVar(player.getAccountName(), Long.toString(System.currentTimeMillis() + 21600000L));
               htmltext = "32599-scroll24.htm";
            } else {
               htmltext = "32599-s24-no.htm";
            }
         }
      } else if (event.equalsIgnoreCase("buy_scroll_1")) {
         if (isTakeRequestItems(player, this._template, 13541)) {
            calcReward(player, this._template, 13541);
            htmltext = "32599-scroll-ok.htm";
         } else {
            htmltext = "32599-s1-no.htm";
         }
      } else if (event.equalsIgnoreCase("buy_scroll_10")) {
         if (isTakeRequestItems(player, this._template, 13542)) {
            calcReward(player, this._template, 13542);
            htmltext = "32599-scroll-ok.htm";
         } else {
            htmltext = "32599-s10-no.htm";
         }
      } else if (event.equalsIgnoreCase("receive_reward")) {
         if (st.getItemEquipped(5) == 13539 && st.getEnchantLevel(13539) > 3) {
            calcReward(player, this._template, st.getEnchantLevel(13539));
            st.takeItems(13539, 1L);
            htmltext = "32599-rewardok.htm";
         } else {
            htmltext = "32599-rewardnostaff.htm";
         }
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

   @Override
   public void startTimerTask(long time, final boolean checkZero) {
      if (this._startTask != null) {
         this._startTask.cancel(false);
         this._startTask = null;
      }

      this._startTask = ThreadPoolManager.getInstance().schedule(new Runnable() {
         @Override
         public void run() {
            MasterOfEnchanting.this.eventStart(checkZero ? -1L : (long)(MasterOfEnchanting.this._template.getPeriod() * 3600000));
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
      new MasterOfEnchanting(MasterOfEnchanting.class.getSimpleName(), "events");
   }
}
