package l2e.scripts.events;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import l2e.commons.util.TimeUtils;
import l2e.commons.util.Util;
import l2e.gameserver.Announcements;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.events.AbstractWorldEvent;
import l2e.gameserver.model.entity.events.model.template.WorldEventReward;
import l2e.gameserver.model.entity.events.model.template.WorldEventSpawn;
import l2e.gameserver.model.entity.events.model.template.WorldEventTemplate;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class FreyaCelebration extends AbstractWorldEvent {
   private boolean _isActive = false;
   private WorldEventTemplate _template = null;
   private ScheduledFuture<?> _startTask = null;
   private ScheduledFuture<?> _stopTask = null;
   private final List<Npc> _npcList = new ArrayList<>();
   private static final int[] _skills = new int[]{9150, 9151, 9152, 9153, 9154, 9155, 9156};
   private static final NpcStringId[] FREYA_TEXT = new NpcStringId[]{
      NpcStringId.EVEN_THOUGH_YOU_BRING_SOMETHING_CALLED_A_GIFT_AMONG_YOUR_HUMANS_IT_WOULD_JUST_BE_PROBLEMATIC_FOR_ME,
      NpcStringId.I_JUST_DONT_KNOW_WHAT_EXPRESSION_I_SHOULD_HAVE_IT_APPEARED_ON_ME_ARE_HUMANS_EMOTIONS_LIKE_THIS_FEELING,
      NpcStringId.THE_FEELING_OF_THANKS_IS_JUST_TOO_MUCH_DISTANT_MEMORY_FOR_ME,
      NpcStringId.BUT_I_KIND_OF_MISS_IT_LIKE_I_HAD_FELT_THIS_FEELING_BEFORE,
      NpcStringId.I_AM_ICE_QUEEN_FREYA_THIS_FEELING_AND_EMOTION_ARE_NOTHING_BUT_A_PART_OF_MELISSAA_MEMORIES
   };

   public FreyaCelebration(String name, String descr) {
      super(name, descr);
      this.addStartNpc(13296);
      this.addFirstTalkId(13296);
      this.addTalkId(13296);
      this.addSkillSeeId(new int[]{13296});
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

         ServerMessage msg = new ServerMessage("EventFreyaCelebration.START", true);
         Announcements.getInstance().announceToAll(msg);
         this.updateStatus(this.getName(), totalTime == -1L ? -1L : totalTime + System.currentTimeMillis(), true);
         if (totalTime > 0L) {
            this._stopTask = ThreadPoolManager.getInstance().schedule(new Runnable() {
               @Override
               public void run() {
                  FreyaCelebration.this.eventStop();
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
         ServerMessage msg = new ServerMessage("EventFreyaCelebration.STOP", true);
         Announcements.getInstance().announceToAll(msg);
         this.updateStatus(this.getName(), 0L, false);
         this.checkTimerTask(this.calcEventTime(this._template), false);
         return true;
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return null;
      } else {
         if (event.equalsIgnoreCase("give_potion")) {
            long _curr_time = System.currentTimeMillis();
            String value = this.loadGlobalQuestVar(player.getAccountName());
            long _reuse_time = value == "" ? 0L : Long.parseLong(value);
            if (_curr_time > _reuse_time) {
               if (isTakeRequestItems(player, this._template, 1)) {
                  st.setState((byte)1);
                  calcReward(player, this._template, 1);
                  this.saveGlobalQuestVar(player.getAccountName(), Long.toString(System.currentTimeMillis() + 72000000L));
               } else {
                  player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_REQUIRED_ITEMS));
               }
            } else {
               List<WorldEventReward> rewards = this._template.getVariantRewards().get(1);
               if (rewards != null && !rewards.isEmpty()) {
                  for(WorldEventReward reward : rewards) {
                     if (reward != null) {
                        long remainingTime = (_reuse_time - System.currentTimeMillis()) / 1000L;
                        int hours = (int)(remainingTime / 3600L);
                        int minutes = (int)(remainingTime % 3600L / 60L);
                        SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.AVAILABLE_AFTER_S1_S2_HOURS_S3_MINUTES);
                        sm.addItemName(reward.getId());
                        sm.addNumber(hours);
                        sm.addNumber(minutes);
                        player.sendPacket(sm);
                     }
                  }
               }
            }
         }

         return null;
      }
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      return "13296.htm";
   }

   @Override
   public String onSkillSee(Npc npc, Player caster, Skill skill, GameObject[] targets, boolean isSummon) {
      if (caster != null && npc != null) {
         if (npc.getId() == 13296 && Util.contains(targets, npc) && Util.contains(_skills, skill.getId())) {
            if (getRandom(100) < 5) {
               CreatureSay cs = new CreatureSay(
                  npc.getObjectId(),
                  22,
                  npc.getName(),
                  NpcStringId.DEAR_S1_THINK_OF_THIS_AS_MY_APPRECIATION_FOR_THE_GIFT_TAKE_THIS_WITH_YOU_THERES_NOTHING_STRANGE_ABOUT_IT_ITS_JUST_A_BIT_OF_MY_CAPRICIOUSNESS
               );
               cs.addStringParameter(caster.getName());
               npc.broadcastPacket(cs);
               calcReward(caster, this._template, 2);
            } else if (getRandom(10) < 2) {
               npc.broadcastPacket(new CreatureSay(npc.getObjectId(), 22, npc.getName(), FREYA_TEXT[getRandom(FREYA_TEXT.length - 1)]));
            }
         }

         return super.onSkillSee(npc, caster, skill, targets, isSummon);
      } else {
         return null;
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
            FreyaCelebration.this.eventStart(checkZero ? -1L : (long)(FreyaCelebration.this._template.getPeriod() * 3600000));
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
      new FreyaCelebration(FreyaCelebration.class.getSimpleName(), "events");
   }
}
