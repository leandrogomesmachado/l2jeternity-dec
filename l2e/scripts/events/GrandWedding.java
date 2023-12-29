package l2e.scripts.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import l2e.commons.util.Rnd;
import l2e.commons.util.TimeUtils;
import l2e.gameserver.Announcements;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.instancemanager.CoupleManager;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.Couple;
import l2e.gameserver.model.entity.events.AbstractWorldEvent;
import l2e.gameserver.model.entity.events.model.template.WorldEventSpawn;
import l2e.gameserver.model.entity.events.model.template.WorldEventTemplate;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.PlaySound;
import l2e.gameserver.network.serverpackets.SocialAction;
import l2e.gameserver.network.serverpackets.SpecialCamera;

public class GrandWedding extends AbstractWorldEvent {
   private boolean _isActive = false;
   private WorldEventTemplate _template = null;
   private ScheduledFuture<?> _startTask = null;
   private ScheduledFuture<?> _stopTask = null;
   private final List<Npc> _npcList = new ArrayList<>();
   private static int _instance = 0;
   private static int PIXY_ID = 102500;
   private static int MANAGER = 102510;
   private static int ANAKIM = 102509;
   private static int GIFT = 102502;
   private static int[] Gourd = new int[]{102504, 102513};
   private static int[] entertainmentId = new int[]{102501, 102511, 102512};
   private static int[] specialGuests = new int[]{102517, 102518, 102519, 102520, 102521, 102522};
   private static int[] NPCS = new int[]{GIFT, ANAKIM, MANAGER};
   private static int[] numberGuards = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
   private static boolean HUSBAND_OK = false;
   private static boolean WIFE_OK = false;
   private static int _weddingLocked = 0;
   private static int _weddingStep = 0;
   private static int _husbandCoupleId = 0;
   private static int _wifeCoupleId = 0;
   private static Player _husband;
   private static Player _wife;
   private static Npc _giftBox;
   private static Npc _anakim;
   private static Collection<Player> _players = null;
   private static List<Npc> guards = new ArrayList<>();
   private static List<Player> _weddingList = new ArrayList<>();
   private static List<Npc> _guests = new ArrayList<>();
   private static List<Npc> _pixies = new ArrayList<>();
   private static List<Npc> _entertainment = new ArrayList<>();
   private static List<Npc> _entertainment2 = new ArrayList<>();
   private static List<Npc> _gourds = new ArrayList<>();
   private static Npc _pet1;
   private static Npc _pet2;

   public GrandWedding(String name, String descr) {
      super(name, descr);
      this.addStartNpc(NPCS[2]);

      for(int i : NPCS) {
         this.addFirstTalkId(i);
         this.addTalkId(i);
      }

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

         ServerMessage msg = new ServerMessage("GrandWedding.START", true);
         Announcements.getInstance().announceToAll(msg);
         this.updateStatus(this.getName(), totalTime == -1L ? -1L : totalTime + System.currentTimeMillis(), true);
         if (totalTime > 0L) {
            this._stopTask = ThreadPoolManager.getInstance().schedule(new Runnable() {
               @Override
               public void run() {
                  GrandWedding.this.eventStop();
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
         ServerMessage msg = new ServerMessage("GrandWedding.STOP", true);
         Announcements.getInstance().announceToAll(msg);
         this.updateStatus(this.getName(), 0L, false);
         this.checkTimerTask(this.calcEventTime(this._template), false);
         return true;
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      if (event.equals("EngageRequest")) {
         boolean sex = player.getAppearance().getSex();
         boolean married = player.isMarried();
         if (married) {
            return "102510-09.htm";
         }

         if (!sex) {
            if (HUSBAND_OK) {
               return "102510-08.htm";
            }

            if (_husbandCoupleId == 0 && _wifeCoupleId == 0) {
               if (player.getQuestState(this.getName()).getQuestItemsCount(57) < (long)Config.WEDDING_PRICE) {
                  return "102510-07.htm";
               }

               _weddingList.clear();
               player.getQuestState(this.getName()).takeItems(57, (long)Config.WEDDING_PRICE);
               _husband = player;
               HUSBAND_OK = true;
               _husbandCoupleId = player.getCoupleId();
               this.startQuestTimer("WeddingAnswer", 120000L, null, null);

               for(Npc i : this._npcList) {
                  _players = World.getInstance().getAroundPlayers(i);
                  if (_players.contains(_husband)) {
                     i.broadcastPacket(
                        new CreatureSay(i.getId(), 0, "Wedding Manager", new ServerMessage("GrandWedding.MSG_01", _husband.getLang()).toString())
                     );
                  }
               }
            }

            if (_wifeCoupleId == player.getCoupleId()) {
               _husband = player;
               HUSBAND_OK = true;
               _husbandCoupleId = player.getCoupleId();
               _weddingLocked = 1;
               this.cancelQuestTimer("WeddingAnswer", null, null);
               this.startQuestTimer("WeddingAnnounce", 10000L, null, null);

               for(Npc i : this._npcList) {
                  _players = World.getInstance().getAroundPlayers(i);
                  if (_players.contains(_husband)) {
                     i.broadcastPacket(
                        new CreatureSay(i.getId(), 0, "Wedding Manager", new ServerMessage("GrandWedding.MSG_02", _husband.getLang()).toString())
                     );
                  }
               }
            }

            if (_husband != player) {
               return "102510-08.htm";
            }
         } else if (sex) {
            if (WIFE_OK) {
               return "102510-08.htm";
            }

            if (_husbandCoupleId == 0 && _wifeCoupleId == 0) {
               if (player.getQuestState(this.getName()).getQuestItemsCount(57) < (long)Config.WEDDING_PRICE) {
                  return "102510-07.htm";
               }

               _weddingList.clear();
               player.getQuestState(this.getName()).takeItems(57, (long)Config.WEDDING_PRICE);
               _wife = player;
               WIFE_OK = true;
               _wifeCoupleId = player.getCoupleId();
               this.cancelQuestTimer("WeddingAnswer", null, null);
               this.startQuestTimer("WeddingAnswer", 120000L, null, null);

               for(Npc i : this._npcList) {
                  _players = World.getInstance().getAroundPlayers(i);
                  if (_players.contains(_wife)) {
                     i.broadcastPacket(new CreatureSay(i.getId(), 0, "Wedding Manager", new ServerMessage("GrandWedding.MSG_03", _wife.getLang()).toString()));
                  }
               }
            }

            if (_husbandCoupleId == player.getCoupleId()) {
               _wife = player;
               WIFE_OK = true;
               _wifeCoupleId = player.getCoupleId();
               _weddingLocked = 1;
               this.cancelQuestTimer("WeddingAnswer", null, null);
               this.startQuestTimer("WeddingAnnounce", 10000L, null, null);

               for(Npc i : this._npcList) {
                  _players = World.getInstance().getAroundPlayers(i);
                  if (_players.contains(_wife)) {
                     i.broadcastPacket(new CreatureSay(i.getId(), 0, "Wedding Manager", new ServerMessage("GrandWedding.MSG_02", _wife.getLang()).toString()));
                  }
               }
            }

            if (_wife != player) {
               return "102510-08.htm";
            }
         }
      }

      if (event.equals("WeddingAnswer")) {
         HUSBAND_OK = false;
         WIFE_OK = false;
         _husband = null;
         _wife = null;
         _husbandCoupleId = 0;
         _wifeCoupleId = 0;
         _weddingLocked = 0;
         _weddingStep = 0;

         for(Npc i : this._npcList) {
            i.broadcastPacket(new CreatureSay(i.getId(), 0, "Wedding Manager", new ServerMessage("GrandWedding.MSG_04", _husband.getLang()).toString()));
         }
      }

      if (event.equals("WeddingAnnounce")) {
         ServerMessage msg = new ServerMessage("GrandWedding.MSG_05", true);
         msg.add(_husband.getName());
         msg.add(_wife.getName());
         Announcements.getInstance().announceToAll(msg.toString());
         this.startQuestTimer("WeddingTeleportAnnounce", 285000L, null, null);
         this.startQuestTimer("WeddingTeleport", 300000L, null, null);
      }

      if (event.equals("WeddingList")) {
         if (!_weddingList.contains(player)) {
            if (player != _wife && player != _husband) {
               _weddingList.add(player);
               return "102510-10.htm";
            } else {
               return "102510-11.htm";
            }
         } else {
            return "102510-12.htm";
         }
      } else {
         if (event.equals("WeddingTeleportAnnounce")) {
            Announcements.getInstance().announceToAll(new ServerMessage("GrandWedding.MSG_06", true).toString());
         }

         if (event.equals("WeddingTeleport")) {
            int xx = 0;
            int yy = 0;
            _instance = ReflectionManager.getInstance().createReflection();
            ReflectionManager.getInstance().getReflection(_instance).setPvPInstance(false);
            ServerMessage msg = new ServerMessage("GrandWedding.MSG_07", _husband.getLang());
            _weddingLocked = 2;
            _husband.setReflectionId(this.getReflectionId());
            _husband.teleToLocation(-51659, -54137, -2820, true);
            _husband.sendMessage(msg.toString());
            _husband.setIsParalyzed(true);
            _wife.setReflectionId(this.getReflectionId());
            _wife.teleToLocation(-51659, -54194, -2819, true);
            _wife.sendMessage(msg.toString());
            _wife.setIsParalyzed(true);
            if (_weddingList.size() > 0) {
               for(Player i : _weddingList) {
                  xx = -51848 + (Rnd.get(100) - 50);
                  yy = -54165 + (Rnd.get(100) - 50);
                  i.setReflectionId(this.getReflectionId());
                  i.teleToLocation(xx, yy, -2826, true);
                  i.setIsParalyzed(true);
               }
            }

            this.startQuestTimer("WeddingGuardsSpawn", 60000L, null, null);
         }

         if (event.equals("WeddingGuardsSpawn")) {
            Npc guard = null;
            int val = 1;
            guards.clear();
            int y1 = -54091;
            int y2 = -54242;
            int x1 = -51480;
            int x2 = -51480;

            for(int i = 0; i < numberGuards.length; ++i) {
               x1 += val;
               x2 += val;
               guard = addSpawn(102503, x1, y1, -2808, 15308, false, 0L, false, this.getReflectionId());
               guards.add(guard);
               guard = addSpawn(102503, x2, y2, -2808, 48643, false, 0L, false, this.getReflectionId());
               guards.add(guard);
               val = 80;
            }

            this.startQuestTimer("guardsPart2", 6000L, null, null);
         }

         if (event.equals("guardsPart2")) {
            int zz = guards.get(0).getZ();

            for(int i = 0; i < guards.size(); i += 2) {
               int xx1 = guards.get(i).getX();
               int yy1 = guards.get(i).getY() - 30;
               int xx2 = guards.get(i + 1).getX();
               int yy2 = guards.get(i + 1).getY() + 30;
               guards.get(i).getAI().setIntention(CtrlIntention.MOVING, new Location(xx1, yy1, zz, 0));
               guards.get(i + 1).getAI().setIntention(CtrlIntention.MOVING, new Location(xx2, yy2, zz, 0));
            }

            this.startQuestTimer("guardsPart3", 2500L, null, null);
         }

         if (event.equals("guardsPart3")) {
            for(Npc i : guards) {
               i.broadcastPacket(new SocialAction(i.getObjectId(), 2));
            }

            this.startQuestTimer("AnakimSpawn", 2000L, null, null);
         }

         if (event.equals("AnakimSpawn")) {
            _anakim = addSpawn(ANAKIM, -52241, -54176, -2827, 0, false, 0L, false, this.getReflectionId());
            this.startQuestTimer("AnakimSpeak", 100L, null, null);
         }

         if (event.equals("AnakimSpeak")) {
            _anakim.broadcastPacket(new CreatureSay(_anakim.getId(), 0, "Anakim", new ServerMessage("GrandWedding.MSG_08", _husband.getLang()).toString()));
            _anakim.broadcastPacket(new SpecialCamera(_anakim.getObjectId(), 200, 0, 150, 0, 5000));
            this.startQuestTimer("AnakimAnim", 1000L, null, null);
            this.startQuestTimer("AnakimPets", 8000L, null, null);
         }

         if (event.equals("AnakimAnim")) {
            _anakim.broadcastPacket(new SocialAction(_anakim.getObjectId(), 2));
         }

         if (event.equals("AnakimPets")) {
            _anakim.broadcastPacket(new CreatureSay(_anakim.getId(), 0, "Anakim", new ServerMessage("GrandWedding.MSG_09", _husband.getLang()).toString()));
            _anakim.broadcastPacket(new CreatureSay(_anakim.getId(), 0, "Anakim", new ServerMessage("GrandWedding.MSG_10", _husband.getLang()).toString()));
            _pet1 = addSpawn(102514, -52241, -54146, -2827, 0, false, 0L, false, this.getReflectionId());
            _pet2 = addSpawn(102514, -52241, -54206, -2827, 0, false, 0L, false, this.getReflectionId());
            this.startQuestTimer("AnakimWalk", 3000L, null, null);
         }

         if (event.equals("AnakimWalk")) {
            _anakim.getAI().setIntention(CtrlIntention.MOVING, new Location(-49877, -54168, -2688, 0));
            this.startQuestTimer("petsWalk", 1500L, null, null);
         }

         if (event.equals("petsWalk")) {
            _pet1.getAI().setIntention(CtrlIntention.MOVING, new Location(-49896, -54116, -2688, 0));
            _pet2.getAI().setIntention(CtrlIntention.MOVING, new Location(-49896, -54220, -2688, 0));
            this.startQuestTimer("AnakimHeading", 27000L, null, null);
            _anakim.broadcastPacket(new SpecialCamera(_anakim.getObjectId(), 400, 180, 150, 0, 31500));
         }

         if (event.equals("AnakimHeading")) {
            _anakim.getAI().setIntention(CtrlIntention.MOVING, new Location(-49984, -54168, -2688, 0));
            _pet2.getAI().setIntention(CtrlIntention.MOVING, new Location(-49976, -54241, -2688, 0));
            this.startQuestTimer("petsHeading", 100L, null, null);
            this.startQuestTimer("witnessSpawn", 500L, null, null);
            this.startQuestTimer("AnakimSpeak2", 3000L, null, null);
         }

         if (event.equals("petsHeading")) {
            _pet1.getAI().setIntention(CtrlIntention.MOVING, new Location(-49976, -54104, -2688, 0));
         }

         if (event.equals("witnessSpawn")) {
            Npc witness = addSpawn(102508, -50034, -54068, -2688, 48643, false, 0L, false, this.getReflectionId());
            guards.add(witness);
            witness = addSpawn(102507, -50034, -54268, -2688, 15308, false, 0L, false, this.getReflectionId());
            guards.add(witness);
         }

         if (event.equals("AnakimSpeak2")) {
            _anakim.broadcastPacket(new CreatureSay(_anakim.getId(), 0, "Anakim", new ServerMessage("GrandWedding.MSG_11", _husband.getLang()).toString()));
            this.startQuestTimer("AnakimSpeak3", 2000L, null, null);
         }

         if (event.equals("AnakimSpeak3")) {
            _anakim.broadcastPacket(new CreatureSay(_anakim.getId(), 0, "Anakim", new ServerMessage("GrandWedding.MSG_12", _husband.getLang()).toString()));
            this.startQuestTimer("PixiesSpawn", 1000L, null, null);
            this.startQuestTimer("PixiesCamera", 10500L, null, null);
         }

         if (event.equals("PixiesSpawn")) {
            for(int i = 0; i < 45; ++i) {
               int xx = -51910 + (Rnd.get(120) - 60);
               int yy = -54985 + (Rnd.get(120) - 60);
               Npc pixy = addSpawn(PIXY_ID, xx, yy, -2824, 0, false, 0L, false, this.getReflectionId());
               pixy.setRunning();
               _pixies.add(pixy);
            }

            this.startQuestTimer("pixiesMove1", 9000L, null, null);
         }

         if (event.equals("PixiesCamera")) {
            _pixies.get(0).broadcastPacket(new SpecialCamera(_pixies.get(0).getObjectId(), 400, 180, 150, 0, 14000));
         }

         if (event.equals("pixiesMove1")) {
            for(Npc i : _pixies) {
               int xx = -51433 + (Rnd.get(250) - 125);
               int yy = -54725 + (Rnd.get(250) - 125);
               i.getAI().setIntention(CtrlIntention.MOVING, new Location(xx, yy, -2827, 0));
            }

            this.startQuestTimer("pixiesMove2", 3000L, null, null);
         }

         if (event.equals("pixiesMove2")) {
            for(Npc i : _pixies) {
               int xx = -51848 + (Rnd.get(60) - 30);
               int yy = -54165 + (Rnd.get(60) - 30);
               i.getAI().setIntention(CtrlIntention.MOVING, new Location(xx, yy, -2826, 0));
            }

            this.startQuestTimer("pixiesMove3", 2500L, null, null);
         }

         if (event.equals("pixiesMove3")) {
            for(Npc i : _pixies) {
               int xx = -51228 + (Rnd.get(1200) - 600);
               int yy = -54178 + (Rnd.get(1200) - 600);
               i.getAI().setIntention(CtrlIntention.MOVING, new Location(xx, yy, -2809, 0));
            }

            this.startQuestTimer("AnakimSpeak4", 5000L, null, null);
         }

         if (event.equals("AnakimSpeak4")) {
            _anakim.broadcastPacket(new CreatureSay(_anakim.getId(), 0, "Anakim", new ServerMessage("GrandWedding.MSG_13", _husband.getLang()).toString()));
            _anakim.broadcastPacket(new CreatureSay(_anakim.getId(), 0, "Anakim", new ServerMessage("GrandWedding.MSG_14", _husband.getLang()).toString()));
            this.startQuestTimer("entertainmentSpawn", 1000L, null, null);
         }

         if (event.equals("entertainmentSpawn")) {
            for(int i = 0; i < 24; ++i) {
               int rr = Rnd.get(3);
               int xx = -53714 + (Rnd.get(150) - 75);
               int yy = -54142 + (Rnd.get(150) - 75);
               Npc show = addSpawn(entertainmentId[rr], xx, yy, -2674, 0, false, 0L, false, this.getReflectionId());
               show.setRunning();
               _entertainment.add(show);
            }

            this.startQuestTimer("entertainmentMove", 4000L, null, null);
         }

         if (event.equals("showCamera")) {
            _entertainment.get(0).broadcastPacket(new SpecialCamera(_entertainment.get(0).getObjectId(), 400, 180, 150, 0, 20000));
         }

         if (event.equals("entertainmentMove")) {
            for(Npc i : _entertainment) {
               int xx = -52083 + (Rnd.get(100) - 50);
               int yy = -54117 + (Rnd.get(100) - 50);
               i.getAI().setIntention(CtrlIntention.MOVING, new Location(xx, yy, -2826, 0));
            }

            this.startQuestTimer("entertainmentMove2", 10500L, null, null);
         }

         if (event.equals("entertainmentMove2")) {
            for(Npc i : _entertainment) {
               int xx = -51770 + (Rnd.get(220) - 110);
               int yy = -54863 + (Rnd.get(220) - 110);
               i.getAI().setIntention(CtrlIntention.MOVING, new Location(xx, yy, -2825, 0));
            }

            for(Npc show : _entertainment) {
               show.setWalking();
            }

            this.startQuestTimer("showCamera", 100L, null, null);
            this.startQuestTimer("entertainmentMove3", 10500L, null, null);
         }

         if (event.equals("entertainmentMove3")) {
            for(Npc i : _entertainment) {
               int xx = -51150 + (Rnd.get(200) - 100);
               int yy = -54511 + (Rnd.get(200) - 100);
               i.getAI().setIntention(CtrlIntention.MOVING, new Location(xx, yy, -2825, 0));
            }

            this.startQuestTimer("AnakimSpeak5", 10000L, null, null);
         }

         if (event.equals("AnakimSpeak5")) {
            _anakim.broadcastPacket(new CreatureSay(_anakim.getId(), 0, "Anakim", new ServerMessage("GrandWedding.MSG_15", _husband.getLang()).toString()));
            _anakim.broadcastPacket(new CreatureSay(_anakim.getId(), 0, "Anakim", new ServerMessage("GrandWedding.MSG_16", _husband.getLang()).toString()));
            this.startQuestTimer("SpecialGuestsSpawn", 4000L, null, null);
         }

         if (event.equals("SpecialGuestsSpawn")) {
            for(int i = 0; i < specialGuests.length; ++i) {
               int x1 = -51311 + (Rnd.get(500) - 250);
               int y1 = -53695 + (Rnd.get(500) - 250);
               Npc guard = addSpawn(specialGuests[i], x1, y1, -2808, 58609, false, 0L, false, this.getReflectionId());
               _guests.add(guard);
            }

            this.startQuestTimer("GuestCamera", 100L, null, null);
            this.startQuestTimer("AnakimSpeak6", 8500L, null, null);
            this.startQuestTimer("CoupleMarch", 10000L, null, null);
         }

         if (event.equals("GuestCamera")) {
            _guests.get(0).broadcastPacket(new SpecialCamera(_guests.get(0).getObjectId(), 1000, 180, 150, 0, 6000));
         }

         if (event.equals("AnakimSpeak6")) {
            ServerMessage msg = new ServerMessage("GrandWedding.MSG_17", _husband.getLang());
            msg.add(_wife.getName());
            msg.add(_husband.getName());
            _anakim.broadcastPacket(new CreatureSay(_anakim.getId(), 0, "Anakim", msg.toString()));
            this.startQuestTimer("AnakimSpeak7", 24000L, null, null);
         }

         if (event.equals("CoupleMarch")) {
            for(Player i : _weddingList) {
               i.sendPacket(new PlaySound(1, "ns23_f", 0, 0, i.getX(), i.getY(), i.getZ()));
            }

            _wife.sendPacket(new PlaySound(1, "ns23_f", 0, 0, _wife.getX(), _wife.getY(), _wife.getZ()));
            _husband.sendPacket(new PlaySound(1, "ns23_f", 0, 0, _husband.getX(), _husband.getY(), _husband.getZ()));
            _husband.setIsParalyzed(false);
            _wife.setIsParalyzed(false);
            _husband.setWalking();
            _wife.setWalking();
            _husband.broadcastPacket(new SpecialCamera(_husband.getObjectId(), 700, 180, 140, 0, 20000));
            _wife.getAI().setIntention(CtrlIntention.MOVING, new Location(-50042, -54178, -2688, 0));
            _husband.getAI().setIntention(CtrlIntention.MOVING, new Location(-50042, -54147, -2688, 0));

            for(Player i : _weddingList) {
               i.setIsParalyzed(false);
            }
         }

         if (event.equals("AnakimSpeak7")) {
            ServerMessage msg = new ServerMessage("GrandWedding.MSG_18", _husband.getLang());
            msg.add(_husband.getName());
            msg.add(_wife.getName());
            _anakim.broadcastPacket(new CreatureSay(_anakim.getId(), 0, "Anakim", msg.toString()));
            _weddingStep = 1;
         }

         if (event.equals("AnakimSpeak8")) {
            ServerMessage msg = new ServerMessage("GrandWedding.MSG_19", _husband.getLang());
            msg.add(_wife.getName());
            msg.add(_husband.getName());
            _anakim.broadcastPacket(new CreatureSay(_anakim.getId(), 0, "Anakim", msg.toString()));
            _weddingStep = 2;
         }

         if (event.equals("AnakimSpeak9")) {
            _anakim.broadcastPacket(new CreatureSay(_anakim.getId(), 0, "Anakim", new ServerMessage("GrandWedding.MSG_20", _husband.getLang()).toString()));
            _weddingStep = 0;
            _husband.setPartnerId(_wife.getObjectId());
            _husband.setMarryAccepted(true);
            _husband.setMarried(true);
            _wife.setPartnerId(_husband.getObjectId());
            _wife.setMarryAccepted(true);
            _wife.setMarried(true);
            _husband.setRunning();
            _wife.setRunning();
            Couple couple = CoupleManager.getInstance().getCouple(_husbandCoupleId);
            couple.marry();
            if (_husband != null) {
               _husband.getCounters().addAchivementInfo("getMarried", 0, -1L, false, false, false);
            }

            if (_wife != null) {
               _wife.getCounters().addAchivementInfo("getMarried", 0, -1L, false, false, false);
            }

            _anakim.doCast(SkillsParser.getInstance().getInfo(2025, 1));

            for(Npc i : guards) {
               int rr = Rnd.get(1);
               if (rr == 0) {
                  i.doCast(SkillsParser.getInstance().getInfo(2024, 1));
               }

               if (rr == 1) {
                  i.doCast(SkillsParser.getInstance().getInfo(2023, 1));
               }
            }

            this.startQuestTimer("WeddingFinale", 3000L, null, null);
         }

         if (event.equals("WeddingFinale")) {
            for(Npc i : _pixies) {
               int xx = -51228 + (Rnd.get(1200) - 600);
               int yy = -54178 + (Rnd.get(1200) - 600);
               i.getAI().setIntention(CtrlIntention.MOVING, new Location(xx, yy, -2809, 0));
            }

            for(int i = 0; i < 25; ++i) {
               int rr = Rnd.get(2);
               int xx = -51228 + (Rnd.get(1200) - 600);
               int yy = -54178 + (Rnd.get(1200) - 600);
               Npc gourd = addSpawn(Gourd[rr], xx, yy, -2824, 0, false, 0L, false, this.getReflectionId());
               _gourds.add(gourd);
            }

            this.startQuestTimer("WeddingFinale2", 4000L, null, null);
         }

         if (event.equals("WeddingFinale2")) {
            for(Npc i : _guests) {
               i.deleteMe();
            }

            for(Npc i : guards) {
               i.deleteMe();
            }

            for(Npc i : _entertainment) {
               int xx = -51862 + (Rnd.get(50) - 25);
               int yy = -54451 + (Rnd.get(50) - 25);
               i.setRunning();
               i.getAI().setIntention(CtrlIntention.MOVING, new Location(xx, yy, -2825, 0));
            }

            for(int n = 0; n < 25; ++n) {
               _gourds.get(n).reduceCurrentHp(999999.0, _gourds.get(n), null);
            }

            this.startQuestTimer("WeddingFinale3", 6000L, null, null);
         }

         if (event.equals("WeddingFinale3")) {
            for(int i = 0; i < 12; ++i) {
               _entertainment2.add(_entertainment.get(i));
               _entertainment.remove(i);
            }

            for(Npc i : _entertainment) {
               int xx = -51867;
               int yy = -54209;
               i.getAI().setIntention(CtrlIntention.MOVING, new Location(xx, yy, -2825, 0));
            }

            for(Npc i : _entertainment2) {
               int xx = -51867;
               int yy = -54120;
               i.getAI().setIntention(CtrlIntention.MOVING, new Location(xx, yy, -2825, 0));
            }

            this.startQuestTimer("WeddingFinale4", 6000L, null, null);
         }

         if (event.equals("WeddingFinale4")) {
            int val = 1;
            int x1 = -51450;
            int x2 = -51510;

            for(int i = 0; i < _entertainment.size(); ++i) {
               x1 += val;
               x2 += val;
               int yy = -54209;
               _entertainment.get(i).getAI().setIntention(CtrlIntention.MOVING, new Location(x1, yy, -2808, 0));
               _entertainment2.get(i).getAI().setIntention(CtrlIntention.MOVING, new Location(x2, yy, -2808, 0));
               val = 80;
            }

            this.startQuestTimer("WeddingFinale5", 6000L, null, null);
         }

         if (event.equals("WeddingFinale5")) {
            _entertainment2.get(0)
               .broadcastPacket(
                  new CreatureSay(_entertainment2.get(0).getId(), 0, "Ceremony Staff", new ServerMessage("GrandWedding.MSG_21", _husband.getLang()).toString())
               );
            this.startQuestTimer("WeddingFinale6", 4000L, null, null);
         }

         if (event.equals("WeddingFinale6")) {
            _giftBox = addSpawn(GIFT, _husband.getX() + 20, _husband.getY() + 20, _husband.getZ(), 0, false, 0L, false, this.getReflectionId());
            this.startQuestTimer("weddingDespawn", 5000L, null, null);
         }

         if (event.equals("weddingDespawn")) {
            HUSBAND_OK = false;
            WIFE_OK = false;
            _husbandCoupleId = 0;
            _wifeCoupleId = 0;
            _weddingLocked = 0;
            _weddingStep = 0;
            _anakim.deleteMe();
            _pet1.deleteMe();
            _pet2.deleteMe();

            for(Npc s : _entertainment2) {
               s.deleteMe();
            }

            for(Npc s : _entertainment) {
               s.deleteMe();
            }

            for(Npc n : _pixies) {
               n.deleteMe();
            }

            for(Npc i : guards) {
               i.deleteMe();
            }
         }

         if (event.equals("destroyInstance")) {
            if (this.getReflectionId() > 0) {
               _husband.setReflectionId(0);
               _wife.setReflectionId(0);

               for(Player i : _weddingList) {
                  i.setReflectionId(0);
               }

               ReflectionManager.getInstance().destroyReflection(this.getReflectionId());
            }

            _husband = null;
            _wife = null;
            _weddingList.clear();
         }

         return "";
      }
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      return npc.getId() + ".htm";
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = "Error";
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         if (npcId == GIFT) {
            if (player != _husband && player != _wife) {
               htmltext = "102502-02.htm";
            } else {
               htmltext = "102502-01.htm";
               calcReward(_husband, this._template, 1);
               calcReward(_wife, this._template, 1);
               _giftBox.deleteMe();
               this.startQuestTimer("destroyInstance", 5000L, null, null);
            }
         } else if (npcId == MANAGER) {
            int level = player.getLevel();
            switch(_weddingLocked) {
               case 0:
                  if (level >= 60) {
                     if (player.getCoupleId() == 0) {
                        htmltext = "102510-03.htm";
                     } else if (this.wearsFormalWear(player)) {
                        htmltext = "102510-01.htm";
                     } else {
                        htmltext = "102510-04.htm";
                     }
                  } else {
                     htmltext = "102510-05.htm";
                  }
                  break;
               case 1:
                  htmltext = "102510-02.htm";
                  break;
               case 2:
                  player.teleToLocation(-51848, -54165, -2826, true);
                  htmltext = "102510-06.htm";
            }
         } else if (npcId == ANAKIM) {
            if (player != _husband && !player.getName().equals(_husband.getName())) {
               if (player != _wife && !player.getName().equals(_wife.getName())) {
                  htmltext = "102509-04.htm";
               } else if (_weddingStep == 2) {
                  htmltext = "102509-02.htm";
               } else {
                  htmltext = "102509-03.htm";
               }
            } else if (_weddingStep == 1) {
               htmltext = "102509-01.htm";
            } else {
               htmltext = "102509-03.htm";
            }
         }

         return htmltext;
      }
   }

   protected boolean wearsFormalWear(Player player) {
      if (Config.WEDDING_FORMALWEAR) {
         ItemInstance fw1 = player.getChestArmorInstance();
         if (fw1 == null || fw1.getId() != 6408) {
            return false;
         }
      }

      return true;
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
            GrandWedding.this.eventStart(checkZero ? -1L : (long)(GrandWedding.this._template.getPeriod() * 3600000));
         }
      }, time - System.currentTimeMillis());
      _log.info("Event " + this._template.getName() + " will start in: " + TimeUtils.toSimpleFormat(time));
   }

   protected int getReflectionId() {
      return _instance;
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
      new GrandWedding(GrandWedding.class.getSimpleName(), "events");
   }
}
