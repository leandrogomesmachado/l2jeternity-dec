package l2e.scripts.ai.grandboss;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import l2e.gameserver.Config;
import l2e.gameserver.instancemanager.EpicBossManager;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.GrandBossInstance;
import l2e.gameserver.model.stats.StatsSet;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.gameserver.network.serverpackets.PlaySound;
import l2e.scripts.ai.AbstractNpcAI;

public class Core extends AbstractNpcAI {
   private final List<Attackable> _minions = new CopyOnWriteArrayList<>();
   private GrandBossInstance _boss = null;

   private Core(String name, String descr) {
      super(name, descr);
      this.registerMobs(new int[]{29006, 29007, 29008, 29011});
      StatsSet info = EpicBossManager.getInstance().getStatsSet(29006);
      int status = EpicBossManager.getInstance().getBossStatus(29006);
      if (status == 3) {
         long temp = info.getLong("respawnTime") - System.currentTimeMillis();
         if (temp > 0L) {
            this.startQuestTimer("core_unlock", temp, null, null);
         } else {
            this._boss = (GrandBossInstance)addSpawn(29006, 17726, 108915, -6480, 0, false, 0L);
            EpicBossManager.getInstance().setBossStatus(29006, 0, false);
            this.spawnBoss(this._boss);
         }
      } else {
         int loc_x = info.getInteger("loc_x");
         int loc_y = info.getInteger("loc_y");
         int loc_z = info.getInteger("loc_z");
         int heading = info.getInteger("heading");
         int hp = info.getInteger("currentHP");
         int mp = info.getInteger("currentMP");
         this._boss = (GrandBossInstance)addSpawn(29006, loc_x, loc_y, loc_z, heading, false, 0L);
         this._boss.setCurrentHpMp((double)hp, (double)mp);
         this.spawnBoss(this._boss);
      }
   }

   public void spawnBoss(GrandBossInstance npc) {
      EpicBossManager.getInstance().addBoss(npc);
      npc.broadcastPacket(new PlaySound(1, "BS01_A", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));

      for(int i = 0; i < 5; ++i) {
         int x = 16800 + i * 360;
         Attackable mob = (Attackable)addSpawn(29007, x, 110000, npc.getZ(), 280 + getRandom(40), false, 0L);
         mob.setIsRaidMinion(true);
         this._minions.add(mob);
         mob = (Attackable)addSpawn(29007, x, 109000, npc.getZ(), 280 + getRandom(40), false, 0L);
         mob.setIsRaidMinion(true);
         this._minions.add(mob);
         int x2 = 16800 + i * 600;
         mob = (Attackable)addSpawn(29008, x2, 109300, npc.getZ(), 280 + getRandom(40), false, 0L);
         mob.setIsRaidMinion(true);
         this._minions.add(mob);
      }

      for(int i = 0; i < 4; ++i) {
         int x = 16800 + i * 450;
         Attackable mob = (Attackable)addSpawn(29011, x, 110300, npc.getZ(), 280 + getRandom(40), false, 0L);
         mob.setIsRaidMinion(true);
         this._minions.add(mob);
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      if (event.equalsIgnoreCase("core_unlock")) {
         this._boss = (GrandBossInstance)addSpawn(29006, 17726, 108915, -6480, 0, false, 0L);
         EpicBossManager.getInstance().setBossStatus(29006, 0, true);
         this.spawnBoss(this._boss);
      } else if (event.equalsIgnoreCase("spawn_minion")) {
         Attackable mob = (Attackable)addSpawn(npc.getId(), npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0L);
         mob.setIsRaidMinion(true);
         this._minions.add(mob);
      } else if (event.equalsIgnoreCase("despawn_minions")) {
         for(int i = 0; i < this._minions.size(); ++i) {
            Attackable mob = this._minions.get(i);
            if (mob != null) {
               mob.decayMe();
            }
         }

         this._minions.clear();
      }

      return super.onAdvEvent(event, npc, player);
   }

   @Override
   public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon) {
      if (npc.getId() == 29006) {
         if (npc.isScriptValue(1)) {
            if (getRandom(100) == 0) {
               npc.broadcastPacket(new NpcSay(npc.getObjectId(), 22, npc.getId(), NpcStringId.REMOVING_INTRUDERS), 2000);
            }
         } else {
            npc.setScriptValue(1);
            npc.broadcastPacket(new NpcSay(npc.getObjectId(), 22, npc.getId(), NpcStringId.A_NON_PERMITTED_TARGET_HAS_BEEN_DISCOVERED), 2000);
            npc.broadcastPacket(new NpcSay(npc.getObjectId(), 22, npc.getId(), NpcStringId.INTRUDER_REMOVAL_SYSTEM_INITIATED), 2000);
         }
      }

      return super.onAttack(npc, attacker, damage, isSummon);
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      int npcId = npc.getId();
      if (npcId == 29006) {
         int objId = npc.getObjectId();
         npc.broadcastPacket(new PlaySound(1, "BS02_D", 1, objId, npc.getX(), npc.getY(), npc.getZ()));
         npc.broadcastPacket(new NpcSay(objId, 22, npcId, NpcStringId.A_FATAL_ERROR_HAS_OCCURRED), 2000);
         npc.broadcastPacket(new NpcSay(objId, 22, npcId, NpcStringId.SYSTEM_IS_BEING_SHUT_DOWN), 2000);
         npc.broadcastPacket(new NpcSay(objId, 22, npcId, NpcStringId.DOT_DOT_DOT_DOT_DOT_DOT), 2000);
         long respawnTime = EpicBossManager.getInstance().setRespawnTime(29006, Config.CORE_RESPAWN_PATTERN);
         this.startQuestTimer("core_unlock", respawnTime - System.currentTimeMillis(), null, null);
         this.startQuestTimer("despawn_minions", 20000L, null, null);
         this.cancelQuestTimers("spawn_minion");
         this._boss = null;
      } else if (EpicBossManager.getInstance().getBossStatus(29006) == 0 && this._minions != null && this._minions.contains(npc)) {
         this._minions.remove(npc);
         this.startQuestTimer("spawn_minion", 60000L, npc, null);
      }

      return super.onKill(npc, killer, isSummon);
   }

   @Override
   public boolean unload(boolean removeFromList) {
      if (this._boss != null) {
         this._boss.deleteMe();
         this._boss = null;
      }

      if (this._minions != null) {
         for(int i = 0; i < this._minions.size(); ++i) {
            Attackable mob = this._minions.get(i);
            if (mob != null) {
               mob.decayMe();
            }
         }
      }

      this._minions.clear();
      this.cancelQuestTimers("core_unlock");
      return super.unload(removeFromList);
   }

   public static void main(String[] args) {
      new Core(Core.class.getSimpleName(), "ai");
   }
}
