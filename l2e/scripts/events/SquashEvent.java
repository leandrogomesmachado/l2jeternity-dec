package l2e.scripts.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import l2e.commons.util.TimeUtils;
import l2e.gameserver.Announcements;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.ChronoMonsterInstance;
import l2e.gameserver.model.actor.templates.items.Weapon;
import l2e.gameserver.model.entity.events.AbstractWorldEvent;
import l2e.gameserver.model.entity.events.EventsDropManager;
import l2e.gameserver.model.entity.events.model.template.WorldEventSpawn;
import l2e.gameserver.model.entity.events.model.template.WorldEventTemplate;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.PlaySound;

public class SquashEvent extends AbstractWorldEvent {
   private boolean _isActive = false;
   private static WorldEventTemplate _template = null;
   private ScheduledFuture<?> _startTask = null;
   private ScheduledFuture<?> _stopTask = null;
   private final List<Npc> _npcList = new ArrayList<>();
   private static final int[] CHRONO_LIST = new int[]{4202, 5133, 5817, 7058, 8350};
   private static final String[] SPAWN_TEXT = new String[]{
      "SquashEvent.SPAWN_TEXT1", "SquashEvent.SPAWN_TEXT2", "SquashEvent.SPAWN_TEXT3", "SquashEvent.SPAWN_TEXT4", "SquashEvent.SPAWN_TEXT5"
   };
   private static final String[] GROWUP_TEXT = new String[]{
      "SquashEvent.GROWUP_TEXT1", "SquashEvent.GROWUP_TEXT2", "SquashEvent.GROWUP_TEXT3", "SquashEvent.GROWUP_TEXT4", "SquashEvent.GROWUP_TEXT5"
   };
   private static final String[] KILL_TEXT = new String[]{
      "SquashEvent.KILL_TEXT1", "SquashEvent.KILL_TEXT2", "SquashEvent.KILL_TEXT3", "SquashEvent.KILL_TEXT4", "SquashEvent.KILL_TEXT5"
   };
   private static final String[] NOCHRONO_TEXT = new String[]{
      "SquashEvent.NOCHRONO_TEXT1", "SquashEvent.NOCHRONO_TEXT2", "SquashEvent.NOCHRONO_TEXT3", "SquashEvent.NOCHRONO_TEXT4", "SquashEvent.NOCHRONO_TEXT5"
   };
   private static final String[] CHRONO_TEXT = new String[]{
      "SquashEvent.CHRONO_TEXT1", "SquashEvent.CHRONO_TEXT2", "SquashEvent.CHRONO_TEXT3", "SquashEvent.CHRONO_TEXT4", "SquashEvent.CHRONO_TEXT5"
   };
   private static final String[] NECTAR_TEXT = new String[]{
      "SquashEvent.NECTAR_TEXT1",
      "SquashEvent.NECTAR_TEXT2",
      "SquashEvent.NECTAR_TEXT3",
      "SquashEvent.NECTAR_TEXT4",
      "SquashEvent.NECTAR_TEXT5",
      "SquashEvent.NECTAR_TEXT6"
   };
   Map<ChronoMonsterInstance, SquashEvent.TheInstance> _monsterInstances = new ConcurrentHashMap<>();

   private SquashEvent.TheInstance create(ChronoMonsterInstance mob) {
      SquashEvent.TheInstance mons = new SquashEvent.TheInstance();
      this._monsterInstances.put(mob, mons);
      return mons;
   }

   private SquashEvent.TheInstance get(ChronoMonsterInstance mob) {
      return this._monsterInstances.get(mob);
   }

   private void remove(ChronoMonsterInstance mob) {
      this.cancelQuestTimer("countdown", mob, null);
      this.cancelQuestTimer("despawn", mob, null);
      this._monsterInstances.remove(mob);
   }

   public SquashEvent(String name, String descr) {
      super(name, descr);
      this.addAttackId(new int[]{12774, 12775, 12776, 12777, 12778, 12779, 13016, 13017});
      this.addKillId(new int[]{12774, 12775, 12776, 12777, 12778, 12779, 13016, 13017});
      this.addSpawnId(new int[]{12774, 12775, 12776, 12777, 12778, 12779, 13016, 13017});
      this.addSkillSeeId(new int[]{12774, 12775, 12776, 12777, 12778, 12779, 13016, 13017});
      this.addStartNpc(31255);
      this.addFirstTalkId(31255);
      this.addTalkId(31255);
      _template = this.parseSettings(this.getName());
      if (_template != null && !this._isActive) {
         long expireTime = this.restoreStatus(this.getName());
         if (expireTime <= System.currentTimeMillis() && expireTime != -1L) {
            this.checkTimerTask(this.calcEventTime(_template), true);
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
      return _template;
   }

   @Override
   public boolean eventStart(long totalTime) {
      if (!this._isActive && totalTime != 0L) {
         if (this._startTask != null) {
            this._startTask.cancel(false);
            this._startTask = null;
         }

         this._isActive = true;
         List<WorldEventSpawn> spawnList = _template.getSpawnList();
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

         if (_template.getDropList() != null && !_template.getDropList().isEmpty()) {
            EventsDropManager.getInstance().addRule(_template.getId(), _template.getDropList(), true);
         }

         ServerMessage msg = new ServerMessage("EventSquashes.START", true);
         Announcements.getInstance().announceToAll(msg);
         this.updateStatus(this.getName(), totalTime == -1L ? -1L : totalTime + System.currentTimeMillis(), true);
         if (totalTime > 0L) {
            this._stopTask = ThreadPoolManager.getInstance().schedule(new Runnable() {
               @Override
               public void run() {
                  SquashEvent.this.eventStop();
               }
            }, totalTime);
            _log.info("Event " + _template.getName() + " will end in: " + TimeUtils.toSimpleFormat(System.currentTimeMillis() + totalTime));
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
         EventsDropManager.getInstance().removeRule(_template.getId());
         ServerMessage msg = new ServerMessage("EventSquashes.STOP", true);
         Announcements.getInstance().announceToAll(msg);
         this.updateStatus(this.getName(), 0L, false);
         this.checkTimerTask(this.calcEventTime(_template), false);
         return true;
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      if (event == "countdown") {
         ChronoMonsterInstance mob = (ChronoMonsterInstance)npc;
         SquashEvent.TheInstance self = this.get(mob);
         int timeLeft = (int)((self.despawnTime - System.currentTimeMillis()) / 1000L);
         if (timeLeft == 30) {
            this.autoChat(player, mob, "OTHER_TEXT1");
         } else if (timeLeft == 20) {
            this.autoChat(player, mob, "OTHER_TEXT2");
         } else if (timeLeft == 10) {
            this.autoChat(player, mob, "OTHER_TEXT3");
         } else if (timeLeft == 0) {
            if (mob.getLevelUp() == 0) {
               this.autoChat(player, mob, "OTHER_TEXT4");
            } else {
               this.autoChat(player, mob, "OTHER_TEXT5");
            }
         } else if (timeLeft % 60 == 0 && mob.getLevelUp() == 0) {
            this.autoChat(player, mob, "OTHER_TEXT6" + timeLeft / 60 + "OTHER_TEXT7");
         }
      } else if (event == "despawn") {
         this.remove((ChronoMonsterInstance)npc);
         npc.deleteMe();
      } else {
         if (event != "sound") {
            return super.onAdvEvent(event, npc, player);
         }

         ChronoMonsterInstance mob = (ChronoMonsterInstance)npc;
         mob.broadcastPacket(new PlaySound(0, "ItemSound3.sys_sow_success", 0, 0, 0, 0, 0));
      }

      return null;
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      if (player.getQuestState(this.getName()) == null) {
         this.newQuestState(player);
      }

      switch(npc.getId()) {
         case 31255:
            return "31255.htm";
         default:
            throw new RuntimeException();
      }
   }

   @Override
   public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon) {
      ChronoMonsterInstance mob = (ChronoMonsterInstance)npc;
      Weapon weapon;
      boolean isChronoAttack = !isSummon && (weapon = attacker.getActiveWeaponItem()) != null && contains(CHRONO_LIST, weapon.getId());
      switch(mob.getId()) {
         case 12774:
         case 12775:
         case 12776:
         case 13016:
            if (isChronoAttack) {
               this.chronoText(attacker, mob);
            } else {
               this.noChronoText(attacker, mob);
            }
            break;
         case 12777:
         case 12778:
         case 12779:
         case 13017:
            if (isChronoAttack) {
               mob.setIsInvul(false);
               if (damage == 0) {
                  mob.getStatus().reduceHp(5.0, attacker);
               } else if (damage > 12) {
                  mob.getStatus().setCurrentHp(mob.getStatus().getCurrentHp() + (double)damage - 12.0);
               }

               this.chronoText(attacker, mob);
            } else {
               mob.setIsInvul(true);
               mob.setCurrentHp(mob.getMaxHp());
               this.noChronoText(attacker, mob);
            }
            break;
         default:
            throw new RuntimeException();
      }

      mob.getStatus().stopHpMpRegeneration();
      return super.onAttack(npc, attacker, damage, isSummon);
   }

   @Override
   public String onSkillSee(Npc npc, Player caster, Skill skill, GameObject[] targets, boolean isSummon) {
      if (skill.getId() == 2005 && targets[0] == npc) {
         ChronoMonsterInstance mob = (ChronoMonsterInstance)npc;
         switch(mob.getId()) {
            case 12774:
            case 12777:
               if (mob.getScriptValue() < 5) {
                  mob.setScriptValue(mob.getScriptValue() + 1);
                  this.nectarText(caster, mob);
                  if (getRandom(100) < 50) {
                     npc.doCast(SkillsParser.getInstance().getInfo(4514, 1));
                  } else {
                     npc.doCast(SkillsParser.getInstance().getInfo(4513, 1));
                     mob.setLevelUp(mob.getLevelUp() + 1);
                  }

                  if (mob.getScriptValue() >= 5) {
                     this.randomSpawn(mob);
                  }
               }
         }
      }

      return null;
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      ChronoMonsterInstance mob = (ChronoMonsterInstance)npc;
      this.remove(mob);
      this.autoChat(killer, mob, KILL_TEXT[getRandom(KILL_TEXT.length)]);
      dropItem(npc, killer);
      return super.onKill(npc, killer, isSummon);
   }

   @Override
   public String onSpawn(Npc npc) {
      assert npc instanceof ChronoMonsterInstance;

      ChronoMonsterInstance mob = (ChronoMonsterInstance)npc;
      mob.setOnKillDelay(1500);
      SquashEvent.TheInstance self = this.create(mob);
      Player player = null;

      for(Player target : World.getInstance().getAroundPlayers(npc, 100, 200)) {
         if (player == null) {
            player = target;
         }
      }

      switch(mob.getId()) {
         case 12774:
         case 12777:
            this.startQuestTimer("countdown", 10000L, mob, null, true);
            this.startQuestTimer("despawn", 180000L, mob, null);
            self.despawnTime = System.currentTimeMillis() + 180000L;
            this.autoChat(player, mob, SPAWN_TEXT[getRandom(SPAWN_TEXT.length)]);
            break;
         case 12775:
         case 12776:
         case 12778:
         case 12779:
         case 13016:
         case 13017:
            this.startQuestTimer("countdown", 10000L, mob, null, true);
            this.startQuestTimer("despawn", 90000L, mob, null);
            this.startQuestTimer("sound", 100L, mob, null);
            self.despawnTime = System.currentTimeMillis() + 90000L;
            this.autoChat(player, mob, GROWUP_TEXT[getRandom(GROWUP_TEXT.length)]);
            break;
         default:
            throw new RuntimeException();
      }

      return null;
   }

   private static final void dropItem(Npc mob, Player player) {
      switch(mob.getId()) {
         case 12775:
            calcRandomGroupReward(mob, player, _template, 1);
            break;
         case 12776:
            calcRandomGroupReward(mob, player, _template, 2);
            break;
         case 12778:
            calcRandomGroupReward(mob, player, _template, 4);
            break;
         case 12779:
            calcRandomGroupReward(mob, player, _template, 5);
            break;
         case 13016:
            calcRandomGroupReward(mob, player, _template, 3);
            break;
         case 13017:
            calcRandomGroupReward(mob, player, _template, 6);
      }
   }

   private void randomSpawn(ChronoMonsterInstance mob) {
      int npcId = 0;
      switch(mob.getLevelUp()) {
         case 0:
         case 1:
         case 2:
         case 3:
            npcId = mob.getId() == 12774 ? 12776 : (mob.getId() == 12777 ? 12779 : 0);
            break;
         case 4:
            npcId = mob.getId() == 12774 ? 12775 : (mob.getId() == 12777 ? 12778 : 0);
            break;
         case 5:
            npcId = mob.getId() == 12774 ? 13016 : (mob.getId() == 12777 ? 13017 : 0);
      }

      if (npcId > 0) {
         this.spawnNext(npcId, mob);
      }
   }

   private void autoChat(Player player, ChronoMonsterInstance mob, String text) {
      ServerMessage msg = null;
      if (player != null) {
         msg = new ServerMessage(text, player.getLang());
      } else {
         msg = new ServerMessage(text, true);
      }

      mob.broadcastPacket(new CreatureSay(mob.getObjectId(), 0, mob.getName(), msg.toString()), 2000);
   }

   private void chronoText(Player player, ChronoMonsterInstance mob) {
      if (getRandom(100) < 20) {
         this.autoChat(player, mob, CHRONO_TEXT[getRandom(CHRONO_TEXT.length)]);
      }
   }

   private void noChronoText(Player player, ChronoMonsterInstance mob) {
      if (getRandom(100) < 20) {
         this.autoChat(player, mob, NOCHRONO_TEXT[getRandom(NOCHRONO_TEXT.length)]);
      }
   }

   private void nectarText(Player player, ChronoMonsterInstance mob) {
      this.autoChat(player, mob, NECTAR_TEXT[getRandom(NECTAR_TEXT.length)]);
   }

   private void spawnNext(int npcId, ChronoMonsterInstance oldMob) {
      this.remove(oldMob);
      ChronoMonsterInstance newMob = (ChronoMonsterInstance)addSpawn(npcId, oldMob.getX(), oldMob.getY(), oldMob.getZ(), oldMob.getHeading(), false, 0L);
      newMob.setOwner(oldMob.getOwner());
      newMob.setTitle(oldMob.getTitle());
      oldMob.deleteMe();
   }

   public static <T> boolean contains(T[] array, T obj) {
      for(T element : array) {
         if (element == obj) {
            return true;
         }
      }

      return false;
   }

   public static boolean contains(int[] array, int obj) {
      for(int element : array) {
         if (element == obj) {
            return true;
         }
      }

      return false;
   }

   @Override
   public String onEvent(String event, QuestState qs) {
      return event;
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
            SquashEvent.this.eventStart(checkZero ? -1L : (long)(SquashEvent._template.getPeriod() * 3600000));
         }
      }, time - System.currentTimeMillis());
      _log.info("Event " + _template.getName() + " will start in: " + TimeUtils.toSimpleFormat(time));
   }

   @Override
   public boolean isReloaded() {
      if (this.isEventActive()) {
         return false;
      } else {
         _template = this.parseSettings(this.getName());
         if (_template == null) {
            return false;
         } else {
            long expireTime = this.restoreStatus(this.getName());
            if (expireTime <= System.currentTimeMillis() && expireTime != -1L) {
               this.checkTimerTask(this.calcEventTime(_template), true);
            } else {
               this.eventStart(expireTime == -1L ? -1L : expireTime - System.currentTimeMillis());
            }

            return true;
         }
      }
   }

   public static void main(String[] args) {
      new SquashEvent(SquashEvent.class.getSimpleName(), "events");
   }

   class TheInstance {
      long despawnTime;
   }
}
