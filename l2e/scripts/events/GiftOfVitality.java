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
import l2e.gameserver.model.entity.events.model.template.WorldEventSpawn;
import l2e.gameserver.model.entity.events.model.template.WorldEventTemplate;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.service.BotFunctions;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class GiftOfVitality extends AbstractWorldEvent {
   private boolean _isActive = false;
   private WorldEventTemplate _template = null;
   private ScheduledFuture<?> _startTask = null;
   private ScheduledFuture<?> _stopTask = null;
   private final List<Npc> _npcList = new ArrayList<>();
   private static SkillHolder[] FIGHTER_SKILLS = new SkillHolder[]{
      new SkillHolder(5627, 1),
      new SkillHolder(5628, 1),
      new SkillHolder(5637, 1),
      new SkillHolder(5629, 1),
      new SkillHolder(5630, 1),
      new SkillHolder(5631, 1),
      new SkillHolder(5632, 1)
   };
   private static SkillHolder[] MAGE_SKILLS = new SkillHolder[]{
      new SkillHolder(5627, 1),
      new SkillHolder(5628, 1),
      new SkillHolder(5637, 1),
      new SkillHolder(5633, 1),
      new SkillHolder(5634, 1),
      new SkillHolder(5635, 1),
      new SkillHolder(5636, 1)
   };
   private static SkillHolder[] SERVITOR_SKILLS = new SkillHolder[]{
      new SkillHolder(5627, 1),
      new SkillHolder(5628, 1),
      new SkillHolder(5637, 1),
      new SkillHolder(5629, 1),
      new SkillHolder(5633, 1),
      new SkillHolder(5630, 1),
      new SkillHolder(5634, 1),
      new SkillHolder(5631, 1),
      new SkillHolder(5635, 1),
      new SkillHolder(5632, 1),
      new SkillHolder(5636, 1)
   };
   private static int _reuseHours;

   public GiftOfVitality(String name, String descr) {
      super(name, descr);
      this.addStartNpc(4306);
      this.addFirstTalkId(4306);
      this.addTalkId(4306);
      this._template = this.parseSettings(this.getName());
      if (this._template != null && !this._isActive) {
         _reuseHours = this._template.getParams().getInteger("reuseHours", 5);
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

         ServerMessage msg = new ServerMessage("EventGiftOfVitality.START", true);
         Announcements.getInstance().announceToAll(msg);
         this.updateStatus(this.getName(), totalTime == -1L ? -1L : totalTime + System.currentTimeMillis(), true);
         if (totalTime > 0L) {
            this._stopTask = ThreadPoolManager.getInstance().schedule(new Runnable() {
               @Override
               public void run() {
                  GiftOfVitality.this.eventStop();
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
         ServerMessage msg = new ServerMessage("EventGiftOfVitality.STOP", true);
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
      if (!this._isActive) {
         return null;
      } else {
         switch(event) {
            case "vitality":
               long reuse = st.get("reuse") != null ? Long.parseLong(st.get("reuse")) : 0L;
               if (reuse > System.currentTimeMillis()) {
                  long remainingTime = (reuse - System.currentTimeMillis()) / 1000L;
                  int hours = (int)(remainingTime / 3600L);
                  int minutes = (int)(remainingTime % 3600L / 60L);
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.AVAILABLE_AFTER_S1_S2_HOURS_S3_MINUTES);
                  sm.addSkillName(23179);
                  sm.addNumber(hours);
                  sm.addNumber(minutes);
                  player.sendPacket(sm);
                  htmltext = "4306-notime.htm";
               } else {
                  player.doCast(new SkillHolder(23179, 1).getSkill());
                  player.doCast(new SkillHolder(23180, 1).getSkill());
                  st.setState((byte)1);
                  st.set("reuse", String.valueOf(System.currentTimeMillis() + (long)(_reuseHours * 3600000)));
                  BotFunctions.getInstance().getAutoVitality(player);
                  htmltext = "4306-okvitality.htm";
               }
               break;
            case "memories_player":
               if (player.getLevel() <= 75) {
                  htmltext = "4306-nolevel.htm";
               } else {
                  SkillHolder[] skills = player.isMageClass() ? MAGE_SKILLS : FIGHTER_SKILLS;
                  npc.setTarget(player);

                  for(SkillHolder sk : skills) {
                     npc.doCast(sk.getSkill());
                  }

                  htmltext = "4306-okbuff.htm";
               }
               break;
            case "memories_summon":
               if (player.getLevel() <= 75) {
                  htmltext = "4306-nolevel.htm";
               } else if (!player.hasServitor()) {
                  htmltext = "4306-nosummon.htm";
               } else {
                  npc.setTarget(player.getSummon());

                  for(SkillHolder sk : SERVITOR_SKILLS) {
                     npc.doCast(sk.getSkill());
                  }

                  htmltext = "4306-okbuff.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      if (player.getQuestState(this.getName()) == null) {
         this.newQuestState(player);
      }

      return "4306.htm";
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
            GiftOfVitality.this.eventStart(checkZero ? -1L : (long)(GiftOfVitality.this._template.getPeriod() * 3600000));
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
      new GiftOfVitality(GiftOfVitality.class.getSimpleName(), "events");
   }
}
