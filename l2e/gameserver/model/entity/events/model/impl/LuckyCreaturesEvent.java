package l2e.gameserver.model.entity.events.model.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import l2e.commons.collections.MultiValueSet;
import l2e.commons.threading.RunnableImpl;
import l2e.commons.util.Rnd;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.data.parser.SpawnParser;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.entity.events.AbstractFightEvent;
import l2e.gameserver.model.entity.events.model.template.FightEventPlayer;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.model.zone.ZoneType;

public class LuckyCreaturesEvent extends AbstractFightEvent {
   private final int _monstersCount;
   private final int[] _monsterTemplates;
   private final int _respawnSeconds;
   private final List<Npc> _monsters = new CopyOnWriteArrayList<>();
   private final List<Long> _deathTimes = new CopyOnWriteArrayList<>();

   public LuckyCreaturesEvent(MultiValueSet<String> set) {
      super(set);
      this._monstersCount = set.getInteger("monstersCount", 1);
      this._respawnSeconds = set.getInteger("monstersRespawn", 60);
      this._monsterTemplates = this.parseExcludedSkills(set.getString("monsterTemplates", ""));
   }

   @Override
   public void onKilled(Creature actor, Creature victim) {
      if (victim.isMonster() && actor != null && actor.isPlayer()) {
         FightEventPlayer fActor = this.getFightEventPlayer(actor.getActingPlayer());
         fActor.increaseKills();
         this.updatePlayerScore(fActor);
         actor.getActingPlayer().sendUserInfo();
         this._deathTimes.add(System.currentTimeMillis() + (long)(this._respawnSeconds * 1000));
         this._monsters.remove(victim);
      }

      super.onKilled(actor, victim);
   }

   @Override
   public void startEvent() {
      super.startEvent();
      ThreadPoolManager.getInstance().schedule(new LuckyCreaturesEvent.RespawnThread(), 30000L);
   }

   @Override
   public void startRound() {
      super.startRound();

      for(int i = 0; i < this._monstersCount; ++i) {
         this.spawnMonster();
      }

      _log.info("LuckyCreaturesEvent: Spawning " + this._monstersCount + " monsters.");
   }

   @Override
   public void stopEvent() {
      super.stopEvent();

      for(Npc npc : this._monsters) {
         if (npc != null) {
            npc.onDecay();
         }
      }

      this._monsters.clear();
   }

   private void spawnMonster() {
      if (this.getState() != AbstractFightEvent.EVENT_STATE.OVER && this.getState() != AbstractFightEvent.EVENT_STATE.NOT_ACTIVE) {
         ZoneType zone = this.getActiveZones().values().iterator().next();
         int[] coords = zone.getZone().getRandomPoint();

         try {
            NpcTemplate template = NpcsParser.getInstance().getTemplate(Rnd.get(this._monsterTemplates));
            if (template != null) {
               Spawner spawn = new Spawner(template);
               spawn.setX(coords[0]);
               spawn.setY(coords[1]);
               spawn.setZ(coords[2]);
               spawn.setAmount(1);
               spawn.setRespawnDelay(0);
               spawn.setReflectionId(this.getReflectionId());
               spawn.stopRespawn();
               SpawnParser.getInstance().addNewSpawn(spawn);
               spawn.init();
               Npc monster = spawn.getLastSpawn();
               this._monsters.add(monster);
            }
         } catch (Exception var6) {
         }
      }
   }

   @Override
   protected boolean inScreenShowBeScoreNotKills() {
      return false;
   }

   @Override
   public boolean isFriend(Creature c1, Creature c2) {
      return !c1.isMonster() && !c2.isMonster();
   }

   @Override
   public String getVisibleTitle(Player player, Player viewer, String currentTitle, boolean toMe) {
      FightEventPlayer fPlayer = this.getFightEventPlayer(player);
      if (fPlayer == null) {
         return currentTitle;
      } else {
         ServerMessage msg = new ServerMessage("FightEvents.TITLE_INFO2", viewer.getLang());
         msg.add(fPlayer.getKills());
         return msg.toString();
      }
   }

   private class RespawnThread extends RunnableImpl {
      private RespawnThread() {
      }

      @Override
      public void runImpl() throws Exception {
         if (LuckyCreaturesEvent.this.getState() != AbstractFightEvent.EVENT_STATE.OVER
            && LuckyCreaturesEvent.this.getState() != AbstractFightEvent.EVENT_STATE.NOT_ACTIVE) {
            long current = System.currentTimeMillis();
            List<Long> toRemove = new ArrayList<>();

            for(Long deathTime : LuckyCreaturesEvent.this._deathTimes) {
               if (deathTime < current) {
                  LuckyCreaturesEvent.this.spawnMonster();
                  toRemove.add(deathTime);
               }
            }

            for(Long l : toRemove) {
               LuckyCreaturesEvent.this._deathTimes.remove(l);
            }

            ThreadPoolManager.getInstance().schedule(this, 10000L);
         }
      }
   }
}
