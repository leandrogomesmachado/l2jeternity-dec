package l2e.scripts.events;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import l2e.commons.util.Rnd;
import l2e.commons.util.TimeUtils;
import l2e.commons.util.Util;
import l2e.gameserver.Announcements;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.EventChestInstance;
import l2e.gameserver.model.actor.instance.EventMonsterInstance;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.entity.events.AbstractWorldEvent;
import l2e.gameserver.model.entity.events.model.template.WorldEventDrop;
import l2e.gameserver.model.entity.events.model.template.WorldEventTemplate;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.spawn.SpawnTerritory;
import l2e.gameserver.model.strings.server.ServerMessage;

public class Rabbits extends AbstractWorldEvent {
   private boolean _isActive = false;
   private WorldEventTemplate _template = null;
   private ScheduledFuture<?> _startTask = null;
   private ScheduledFuture<?> _stopTask = null;
   private final List<Npc> _npcList = new ArrayList<>();
   private static int _chest_count = 0;
   private static int _option_howmuch;
   private static boolean _canUseMagic;
   private static final int _npc_snow = 900101;
   private static final int _npc_chest = 900102;
   private static final int _skill_tornado = 630;
   private static final int _skill_magic_eye = 629;

   public Rabbits(String name, String descr) {
      super(name, descr);
      this.addStartNpc(900101);
      this.addFirstTalkId(900101);
      this.addTalkId(900101);
      this.addFirstTalkId(900102);
      this.addSkillSeeId(new int[]{900102});
      this.addSpawnId(new int[]{900102});
      this.addAttackId(900102);
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

      npc.setIsImmobilized(true);
      npc.disableCoreAI(true);
      return super.onSpawn(npc);
   }

   @Override
   public boolean eventStart(long totalTime) {
      if (!this._isActive && totalTime != 0L) {
         if (this._startTask != null) {
            this._startTask.cancel(false);
            this._startTask = null;
         }

         if (this._template.getTerritories() == null) {
            _log.info(this._template.getName() + ": Event can't be started, because territoty cant selected!");
         }

         int rndSelect = Rnd.get(this._template.getTerritories().size());
         this._npcList.clear();
         this._isActive = true;
         Location npcLoc = this._template.getLocations().get(rndSelect).getLocation();
         SpawnTerritory territory = this._template.getTerritories().get(rndSelect).getTerritory();
         this._npcList.add(addSpawn(900101, npcLoc.getX(), npcLoc.getY(), npcLoc.getZ(), npcLoc.getHeading(), false, 0L));

         for(int i = 0; i < _option_howmuch; ++i) {
            Location loc = territory.getRandomLoc(0, false);
            this._npcList.add(addSpawn(900102, loc.getX(), loc.getY(), loc.getZ(), 0, true, totalTime));
            ++_chest_count;
         }

         ServerMessage msg1 = new ServerMessage("EventRabbits.START_MSG_1", true);
         Announcements.getInstance().announceToAll(msg1);
         ServerMessage msg2 = new ServerMessage("EventRabbits.START_MSG_2", true);
         Announcements.getInstance().announceToAll(msg2);
         ServerMessage msg3 = new ServerMessage("EventRabbits.START_MSG_3", true);
         msg3.add(totalTime / 60000L);
         Announcements.getInstance().announceToAll(msg3);
         this.updateStatus(this.getName(), totalTime == -1L ? -1L : totalTime + System.currentTimeMillis(), true);
         if (totalTime > 0L) {
            this._stopTask = ThreadPoolManager.getInstance().schedule(new Runnable() {
               @Override
               public void run() {
                  Rabbits.this.eventStop();
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
         ServerMessage msg = new ServerMessage("EventRabbits.STOP", true);
         Announcements.getInstance().announceToAll(msg);
         this.updateStatus(this.getName(), 0L, false);
         this.checkTimerTask(this.calcEventTime(this._template), false);
         return true;
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      if (!event.equalsIgnoreCase("transform")) {
         return event;
      } else {
         if (player.isTransformed() || player.isInStance()) {
            player.untransform();
         }

         SkillsParser.getInstance().getInfo(2428, 1).getEffects(npc, player, false);
         return null;
      }
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      return npc.getId() + ".htm";
   }

   @Override
   public String onSkillSee(Npc npc, Player caster, Skill skill, GameObject[] targets, boolean isSummon) {
      if (Util.contains(targets, npc)) {
         if (skill.getId() == 630) {
            if (this._template.getDropList() != null && !this._template.getDropList().isEmpty()) {
               for(WorldEventDrop drop : this._template.getDropList()) {
                  if (Rnd.chance(drop.getChance())) {
                     long amount = drop.getMinCount() != drop.getMaxCount() ? Rnd.get(drop.getMinCount(), drop.getMaxCount()) : drop.getMinCount();
                     ((MonsterInstance)npc).dropItem(caster, drop.getId(), (long)((int)amount));
                  }
               }
            }

            npc.deleteMe();
            --_chest_count;
            if (_chest_count <= 0) {
               ServerMessage msg = new ServerMessage("EventRabbits.NO_MORE", true);
               Announcements.getInstance().announceToAll(msg);
               this.eventStop();
            }
         } else if (skill.getId() == 629 && npc instanceof EventChestInstance) {
            ((EventChestInstance)npc).trigger(caster);
         }
      }

      return super.onSkillSee(npc, caster, skill, targets, isSummon);
   }

   @Override
   public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon, Skill skill) {
      if (this._isActive && npc.getId() == 900102) {
         SkillsParser.getInstance().getInfo(4515, 1).getEffects(npc, attacker, false);
      }

      return super.onAttack(npc, attacker, damage, isSummon);
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
            Rabbits.this.eventStart(checkZero ? -1L : (long)(Rabbits.this._template.getPeriod() * 60000));
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
      new Rabbits(Rabbits.class.getSimpleName(), "events");
   }
}
