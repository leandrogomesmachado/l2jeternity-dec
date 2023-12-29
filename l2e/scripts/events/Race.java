package l2e.scripts.events;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import l2e.commons.util.Rnd;
import l2e.commons.util.TimeUtils;
import l2e.gameserver.Announcements;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.events.AbstractWorldEvent;
import l2e.gameserver.model.entity.events.model.template.WorldEventLocation;
import l2e.gameserver.model.entity.events.model.template.WorldEventSpawn;
import l2e.gameserver.model.entity.events.model.template.WorldEventTemplate;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.network.serverpackets.CreatureSay;

public class Race extends AbstractWorldEvent {
   private boolean _isActive = false;
   private WorldEventTemplate _template = null;
   private ScheduledFuture<?> _startTask = null;
   private ScheduledFuture<?> _stopTask = null;
   private ScheduledFuture<?> _eventTask = null;
   private final List<Npc> _npcList = new ArrayList<>();
   private Npc _npc;
   private final List<Player> _players = new ArrayList<>();
   private static boolean _isRaceStarted = false;
   private static int _timeRegister;
   private static int _skill;
   private static final int _start_npc = 900103;
   private static final int _stop_npc = 900104;
   private static int[] _randspawn = null;

   public Race(String name, String descr) {
      super(name, descr);
      this.addStartNpc(900103);
      this.addFirstTalkId(900103);
      this.addTalkId(900103);
      this.addStartNpc(900104);
      this.addFirstTalkId(900104);
      this.addTalkId(900104);
      this._template = this.parseSettings(this.getName());
      if (this._template != null && !this._isActive) {
         _timeRegister = this._template.getParams().getInteger("regTime", 5);
         _skill = this._template.getParams().getInteger("transformSkill", 6201);
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
   public boolean eventStart(final long totalTime) {
      if (this._isActive || totalTime == 0L) {
         return false;
      } else if (!Config.CUSTOM_NPC) {
         _log.info(this._template.getName() + ": Event can't be started, because custom npc table is disabled!");
         return false;
      } else {
         if (this._startTask != null) {
            this._startTask.cancel(false);
            this._startTask = null;
         }

         this._npcList.clear();
         this._players.clear();
         this._isActive = true;
         List<WorldEventSpawn> spawnList = this._template.getSpawnList();
         if (spawnList != null && !spawnList.isEmpty()) {
            for(WorldEventSpawn spawn : spawnList) {
               this._npcList
                  .add(
                     this._npc = addSpawn(
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

         ServerMessage msg1 = new ServerMessage("EventRace.START_MSG_1", true);
         Announcements.getInstance().announceToAll(msg1);
         ServerMessage msg2 = new ServerMessage("EventRace.START_MSG_2", true);
         msg2.add(_timeRegister);
         Announcements.getInstance().announceToAll(msg2);
         this._eventTask = ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
               Race.this.startRace(totalTime);
            }
         }, (long)(_timeRegister * 60 * 1000));
         return true;
      }
   }

   protected void startRace(long totalTime) {
      if (this._players.isEmpty()) {
         ServerMessage msg = new ServerMessage("EventRace.ABORTED", true);
         Announcements.getInstance().announceToAll(msg);
         this.eventStop();
      } else {
         _isRaceStarted = true;
         ServerMessage msg = new ServerMessage("EventRace.RACE_START", true);
         Announcements.getInstance().announceToAll(msg);
         WorldEventLocation loc = this._template.getLocations().get(Rnd.get(this._template.getLocations().size()));
         this._npcList
            .add(addSpawn(900104, loc.getLocation().getX(), loc.getLocation().getY(), loc.getLocation().getZ(), loc.getLocation().getHeading(), false, 0L));
         _randspawn = new int[]{loc.getLocation().getX(), loc.getLocation().getY(), loc.getLocation().getZ()};

         for(Player player : this._players) {
            if (player != null && player.isOnline()) {
               if (player.isInsideRadius(this._npc, 500, false, false)) {
                  this.sendMessage(player, "Race started! Go find Finish NPC as fast as you can... He is located near " + loc.getName());
                  this.transformPlayer(player);
                  player.getRadar().addMarker(_randspawn[0], _randspawn[1], _randspawn[2]);
               } else {
                  this.sendMessage(player, "I told you stay near me right? Distance was too high, you are excluded from race");
                  this._players.remove(player);
               }
            }
         }

         this._eventTask = ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
               Race.this.timeUp();
            }
         }, totalTime);
         _log.info("Event " + this._template.getName() + " will end in: " + TimeUtils.toSimpleFormat(System.currentTimeMillis() + totalTime));
      }
   }

   @Override
   public boolean eventStop() {
      if (!this._isActive) {
         return false;
      } else {
         if (this._eventTask != null) {
            this._eventTask.cancel(false);
            this._eventTask = null;
         }

         if (this._stopTask != null) {
            this._stopTask.cancel(false);
            this._stopTask = null;
         }

         this._isActive = false;
         _isRaceStarted = false;
         if (!this._players.isEmpty()) {
            for(Player player : this._players) {
               if (player != null && player.isOnline()) {
                  player.untransform();
                  player.teleToLocation(this._npc.getX(), this._npc.getY(), this._npc.getZ(), true);
               }
            }
         }

         if (!this._npcList.isEmpty()) {
            for(Npc _npc : this._npcList) {
               if (_npc != null) {
                  _npc.deleteMe();
               }
            }
         }

         this._npcList.clear();
         this._players.clear();
         this._npc = null;
         ServerMessage msg = new ServerMessage("EventRace.STOP", true);
         Announcements.getInstance().announceToAll(msg);
         return true;
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return null;
      } else if (event.equalsIgnoreCase("transform")) {
         this.transformPlayer(player);
         return null;
      } else if (event.equalsIgnoreCase("untransform")) {
         player.untransform();
         return null;
      } else if (event.equalsIgnoreCase("showfinish")) {
         player.getRadar().addMarker(_randspawn[0], _randspawn[1], _randspawn[2]);
         return null;
      } else if (event.equalsIgnoreCase("signup")) {
         if (this._players.contains(player)) {
            return "900103-onlist.htm";
         } else {
            this._players.add(player);
            return "900103-signup.htm";
         }
      } else if (event.equalsIgnoreCase("quit")) {
         player.untransform();
         if (this._players.contains(player)) {
            this._players.remove(player);
         }

         return "900103-quit.htm";
      } else if (event.equalsIgnoreCase("finish")) {
         if (player.getFirstEffect(_skill) != null) {
            this.winRace(player);
            return "900104-winner.htm";
         } else {
            return "900104-notrans.htm";
         }
      } else {
         return event;
      }
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      if (npc.getId() == 900103) {
         return _isRaceStarted ? "900103-started-" + this.isRacing(player) + ".htm" : "900103-" + this.isRacing(player) + ".htm";
      } else {
         return npc.getId() == 900104 && _isRaceStarted ? "900104-" + this.isRacing(player) + ".htm" : npc.getId() + ".htm";
      }
   }

   private int isRacing(Player player) {
      if (this._players.isEmpty()) {
         return 0;
      } else {
         return this._players.contains(player) ? 1 : 0;
      }
   }

   private void transformPlayer(Player player) {
      if (player.isTransformed() || player.isInStance()) {
         player.untransform();
      }

      if (player.isSitting()) {
         player.standUp();
      }

      for(Effect e : player.getAllEffects()) {
         if (e.getAbnormalType().equalsIgnoreCase("SPEED_UP")) {
            e.exit();
         }

         if (e.getSkill() != null && (e.getSkill().getId() == 268 || e.getSkill().getId() == 298)) {
            e.exit();
         }
      }

      SkillsParser.getInstance().getInfo(_skill, 1).getEffects(player, player, false);
   }

   private void sendMessage(Player player, String text) {
      player.sendPacket(new CreatureSay(this._npc.getObjectId(), 20, this._npc.getName(), text));
   }

   protected void timeUp() {
      ServerMessage msg = new ServerMessage("EventRace.TIME_UP", true);
      Announcements.getInstance().announceToAll(msg);
      this.eventStop();
   }

   private void winRace(Player player) {
      if (this._isActive) {
         calcReward(player, this._template, 1);
         ServerMessage msg = new ServerMessage("EventRace.WINNER", true);
         msg.add(player.getName());
         Announcements.getInstance().announceToAll(msg);
         this.eventStop();
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
            Race.this.eventStart(checkZero ? -1L : (long)(Race.this._template.getPeriod() * 60000));
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
      new Race(Race.class.getSimpleName(), "events");
   }
}
