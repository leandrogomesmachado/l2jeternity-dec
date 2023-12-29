package l2e.gameserver.model.entity.events.custom;

import java.util.concurrent.Future;
import java.util.logging.Logger;
import l2e.commons.util.Rnd;
import l2e.gameserver.Announcements;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.data.parser.SpawnParser;
import l2e.gameserver.instancemanager.MapRegionManager;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.network.serverpackets.EarthQuake;
import l2e.gameserver.network.serverpackets.ExRedSky;
import l2e.gameserver.network.serverpackets.MagicSkillUse;

public class Leprechaun {
   protected static final Logger _log = Logger.getLogger(Leprechaun.class.getName());
   protected final Object _lock = new Object();
   protected int _x;
   protected int _y;
   protected int _z;
   protected int _mobId;
   protected int _timer = 0;
   protected int _timerAnnounce = 0;
   protected Npc _eventNpc = null;
   protected String nearestTown = "";
   protected String mobName = "";
   protected Future<?> _eventStart;
   protected Future<?> _eventTask;

   public Leprechaun() {
      new Leprechaun.LeprechaunQuest(-1, "Leprechaun", "events");
      if (Config.ENABLED_LEPRECHAUN) {
         this._eventStart = ThreadPoolManager.getInstance().schedule(new Leprechaun.EventStart(), (long)(Config.LEPRECHAUN_FIRST_SPAWN_DELAY * 60000));
      }
   }

   public void startEvent() {
      if (this._eventStart != null) {
         this._eventStart.cancel(false);
         this._eventStart = null;
      }

      this._eventStart = ThreadPoolManager.getInstance().schedule(new Leprechaun.EventStart(), 100L);
   }

   public void endEvent() {
      if (this._eventNpc != null) {
         if (this._eventTask != null) {
            this._eventTask.cancel(false);
            this._eventTask = null;
         }

         if (this._eventStart != null) {
            this._eventStart.cancel(false);
            this._eventStart = null;
         }

         this._eventNpc.deleteMe();
         this._eventNpc = null;
         this._timer = 0;
         this._timerAnnounce = 0;
         ServerMessage msg = new ServerMessage("Leprechaun.DISAPPEARED", true);
         Announcements.getInstance().announceToAll(msg);
         _log.info("Leprechaun: Event Leprechaun Ended!");
         this._eventStart = ThreadPoolManager.getInstance().schedule(new Leprechaun.EventStart(), (long)(Config.LEPRECHAUN_RESPAWN_INTERVAL * 60000));
      }
   }

   public boolean isActive() {
      return this._eventNpc != null;
   }

   public static Leprechaun getInstance() {
      return Leprechaun.SingletonHolder._instance;
   }

   public class EventStart implements Runnable {
      @Override
      public void run() {
         Leprechaun.this._eventTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(Leprechaun.this.new LeprechaunSpawn(), 100L, 60000L);
      }
   }

   public class LeprechaunQuest extends Quest {
      public LeprechaunQuest(int id, String name, String descr) {
         super(id, name, descr);
         this.addStartNpc(Config.LEPRECHAUN_ID);
         this.addFirstTalkId(Config.LEPRECHAUN_ID);
         this.addKillId(Config.LEPRECHAUN_ID);
      }

      @Override
      public String onFirstTalk(Npc npc, Player player) {
         QuestState qst = player.getQuestState(this.getName());
         if (qst == null) {
            qst = this.newQuestState(player);
         }

         if (npc.getId() == Leprechaun.this._eventNpc.getId()) {
            player.broadcastPacket(new EarthQuake(player.getX(), player.getY(), player.getZ(), 30, 3));
            player.broadcastPacket(new ExRedSky(5));
            player.broadcastPacket(new MagicSkillUse(Leprechaun.this._eventNpc, Leprechaun.this._eventNpc, 1469, 1, 768, 0));
            Leprechaun.this.endEvent();
            int[] chance = Config.LEPRECHAUN_REWARD_CHANCE;

            for(int i = 0; i < chance.length; ++i) {
               if (Rnd.chance(Config.LEPRECHAUN_REWARD_CHANCE[i])) {
                  qst.giveItems(Config.LEPRECHAUN_REWARD_ID[i], (long)Config.LEPRECHAUN_REWARD_COUNT[i]);
               }
            }

            if (Config.SHOW_NICK) {
               ServerMessage msg = new ServerMessage("Leprechaun.WAS_FOUND", true);
               msg.add(player.getName());
               Announcements.getInstance().announceToAll(msg);
            } else {
               ServerMessage msg = new ServerMessage("Leprechaun.NOT_FOUND", true);
               Announcements.getInstance().announceToAll(msg);
            }
         }

         return null;
      }

      @Override
      public String onKill(Npc npc, Player player, boolean isSummon) {
         QuestState qst = player.getQuestState(this.getName());
         if (qst == null) {
            qst = this.newQuestState(player);
         }

         if (npc.getId() == Leprechaun.this._eventNpc.getId()) {
            player.broadcastPacket(new EarthQuake(player.getX(), player.getY(), player.getZ(), 30, 3));
            player.broadcastPacket(new ExRedSky(5));
            Leprechaun.this.endEvent();
            int[] chance = Config.LEPRECHAUN_REWARD_CHANCE;

            for(int i = 0; i < chance.length; ++i) {
               if (Rnd.chance(Config.LEPRECHAUN_REWARD_CHANCE[i])) {
                  qst.giveItems(Config.LEPRECHAUN_REWARD_ID[i], (long)Config.LEPRECHAUN_REWARD_COUNT[i]);
               }
            }

            if (Config.SHOW_NICK) {
               ServerMessage msg = new ServerMessage("Leprechaun.WAS_FOUND", true);
               msg.add(player.getName());
               Announcements.getInstance().announceToAll(msg);
            } else {
               ServerMessage msg = new ServerMessage("Leprechaun.NOT_FOUND", true);
               Announcements.getInstance().announceToAll(msg);
            }
         }

         return super.onKill(npc, player, isSummon);
      }
   }

   public class LeprechaunSpawn implements Runnable {
      @Override
      public void run() {
         if (Leprechaun.this._eventNpc != null && Leprechaun.this._timer == Config.LEPRECHAUN_SPAWN_TIME) {
            Leprechaun.this.endEvent();
         }

         if (Leprechaun.this._timerAnnounce == Config.LEPRECHAUN_ANNOUNCE_INTERVAL) {
            if (Leprechaun.this._eventNpc != null) {
               ServerMessage msg = new ServerMessage("Leprechaun.NEAR", true);
               msg.add(Leprechaun.this.mobName);
               msg.add(Leprechaun.this.nearestTown);
               msg.add(Config.LEPRECHAUN_SPAWN_TIME - Leprechaun.this._timer);
               Announcements.getInstance().announceToAll(msg);
            }

            Leprechaun.this._timerAnnounce = 0;
         }

         if (Leprechaun.this._timer == 0) {
            boolean repeat = true;

            while(repeat) {
               this.selectRandomNpc();
               if (Leprechaun.this.mobName != "" && !Leprechaun.this.mobName.equals("Treasure Chest")) {
                  repeat = false;
               }
            }

            this.spawnLep();
         }

         ++Leprechaun.this._timer;
         ++Leprechaun.this._timerAnnounce;
      }

      private void selectRandomNpc() {
         synchronized(Leprechaun.this._lock) {
            int number = Rnd.get(SpawnParser.getInstance().getSpawnData().size());
            int count = 0;

            for(Spawner spawn : SpawnParser.getInstance().getSpawnData()) {
               if (spawn != null) {
                  if (++count == number) {
                     Leprechaun.this._mobId = spawn.getId();
                     Leprechaun.this._x = spawn.getX() + 80;
                     Leprechaun.this._y = spawn.getY() + 10;
                     Leprechaun.this._z = spawn.getZ();
                     Leprechaun.this.mobName = spawn.getTemplate().getName();
                     break;
                  }
               }
            }
         }
      }

      private void spawnLep() {
         if (Leprechaun.this._eventNpc != null) {
            Leprechaun.this._eventNpc.deleteMe();
            Leprechaun.this._eventNpc = null;
         }

         NpcTemplate template = NpcsParser.getInstance().getTemplate(Config.LEPRECHAUN_ID);
         if (template != null) {
            try {
               Spawner spawnDat = new Spawner(template);
               spawnDat.setAmount(1);
               spawnDat.setX(Leprechaun.this._x);
               spawnDat.setY(Leprechaun.this._y);
               spawnDat.setZ(Leprechaun.this._z);
               spawnDat.setHeading(0);
               spawnDat.stopRespawn();
               Leprechaun.this._eventNpc = spawnDat.spawnOne(false);
               if (Config.SHOW_REGION) {
                  Leprechaun.this.nearestTown = " (" + MapRegionManager.getInstance().getClosestTownName(Leprechaun.this._eventNpc) + ")";
               }

               ServerMessage msg = new ServerMessage("Leprechaun.NEAR", true);
               msg.add(Leprechaun.this.mobName);
               msg.add(Leprechaun.this.nearestTown);
               msg.add(Config.LEPRECHAUN_SPAWN_TIME - Leprechaun.this._timer);
               Announcements.getInstance().announceToAll(msg);
               System.out
                  .println(
                     "Leprechaun spawned in " + Leprechaun.this.mobName + ": " + Leprechaun.this._x + "," + Leprechaun.this._y + "," + Leprechaun.this._z
                  );
               Leprechaun._log.info("Leprechaun: Event Leprechaun is Starting!");
            } catch (Exception var4) {
            }
         } else {
            Leprechaun._log.warning("Leprechaun: Data missing in NPC table for ID: " + Config.LEPRECHAUN_ID + ".");
         }
      }
   }

   private static class SingletonHolder {
      protected static final Leprechaun _instance = new Leprechaun();
   }
}
