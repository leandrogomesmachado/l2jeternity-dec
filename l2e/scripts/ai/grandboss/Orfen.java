package l2e.scripts.ai.grandboss;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.instancemanager.EpicBossManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.GrandBossInstance;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.model.stats.StatsSet;
import l2e.gameserver.model.zone.type.BossZone;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.gameserver.network.serverpackets.PlaySound;
import l2e.scripts.ai.AbstractNpcAI;

public class Orfen extends AbstractNpcAI {
   private static final Location[] _pos = new Location[]{
      new Location(43728, 17220, -4342), new Location(55024, 17368, -5412), new Location(53504, 21248, -5486), new Location(53248, 24576, -5262)
   };
   private static final NpcStringId[] _text = new NpcStringId[]{
      NpcStringId.S1_STOP_KIDDING_YOURSELF_ABOUT_YOUR_OWN_POWERLESSNESS,
      NpcStringId.S1_ILL_MAKE_YOU_FEEL_WHAT_TRUE_FEAR_IS,
      NpcStringId.YOURE_REALLY_STUPID_TO_HAVE_CHALLENGED_ME_S1_GET_READY,
      NpcStringId.S1_DO_YOU_THINK_THATS_GOING_TO_WORK
   };
   private GrandBossInstance _boss = null;
   private static List<Attackable> _minions = new CopyOnWriteArrayList<>();
   private static BossZone _zone = (BossZone)ZoneManager.getInstance().getZoneById(12013);
   private long _lastMsg = 0L;

   private Orfen(String name, String descr) {
      super(name, descr);
      int[] mobs = new int[]{29014, 29016, 29018};
      this.registerMobs(mobs);
      StatsSet info = EpicBossManager.getInstance().getStatsSet(29014);
      int status = EpicBossManager.getInstance().getBossStatus(29014);
      if (status == 3) {
         long temp = info.getLong("respawnTime") - System.currentTimeMillis();
         if (temp > 0L) {
            this.startQuestTimer("orfen_unlock", temp, null, null);
         } else {
            int i = getRandom(10);
            Location loc;
            if (i < 4) {
               loc = _pos[1];
            } else if (i < 7) {
               loc = _pos[2];
            } else {
               loc = _pos[3];
            }

            this._boss = (GrandBossInstance)addSpawn(29014, loc, false, 0L);
            EpicBossManager.getInstance().setBossStatus(29014, 0, false);
            this.spawnBoss(this._boss);
         }
      } else {
         int loc_x = info.getInteger("loc_x");
         int loc_y = info.getInteger("loc_y");
         int loc_z = info.getInteger("loc_z");
         int heading = info.getInteger("heading");
         int hp = info.getInteger("currentHP");
         int mp = info.getInteger("currentMP");
         this._boss = (GrandBossInstance)addSpawn(29014, loc_x, loc_y, loc_z, heading, false, 0L);
         this._boss.setCurrentHpMp((double)hp, (double)mp);
         this.spawnBoss(this._boss);
      }
   }

   public void setSpawnPoint(Npc npc, int index) {
      ((Attackable)npc).clearAggroList();
      npc.getAI().setIntention(CtrlIntention.IDLE, null, null);
      Spawner spawn = npc.getSpawn();
      spawn.setLocation(_pos[index]);
      npc.teleToLocation(_pos[index], false);
   }

   public void spawnBoss(GrandBossInstance npc) {
      EpicBossManager.getInstance().addBoss(npc);
      npc.broadcastPacket(new PlaySound(1, "BS01_A", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
      this.startQuestTimer("check_orfen_pos", 10000L, npc, null, true);
      int x = npc.getX();
      int y = npc.getY();
      Attackable mob = (Attackable)addSpawn(29016, x + 100, y + 100, npc.getZ(), 0, false, 0L);
      mob.setIsRaidMinion(true);
      _minions.add(mob);
      mob = (Attackable)addSpawn(29016, x + 100, y - 100, npc.getZ(), 0, false, 0L);
      mob.setIsRaidMinion(true);
      _minions.add(mob);
      mob = (Attackable)addSpawn(29016, x - 100, y + 100, npc.getZ(), 0, false, 0L);
      mob.setIsRaidMinion(true);
      _minions.add(mob);
      mob = (Attackable)addSpawn(29016, x - 100, y - 100, npc.getZ(), 0, false, 0L);
      mob.setIsRaidMinion(true);
      _minions.add(mob);
      this.startQuestTimer("check_minion_loc", 10000L, npc, null, true);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      if (event.equalsIgnoreCase("orfen_unlock")) {
         int i = getRandom(10);
         Location loc;
         if (i < 4) {
            loc = _pos[1];
         } else if (i < 7) {
            loc = _pos[2];
         } else {
            loc = _pos[3];
         }

         this._boss = (GrandBossInstance)addSpawn(29014, loc, false, 0L);
         EpicBossManager.getInstance().setBossStatus(29014, 0, true);
         this.spawnBoss(this._boss);
      } else if (event.equalsIgnoreCase("check_orfen_pos")) {
         if ((!npc.isScriptValue(1) || !(npc.getCurrentHp() > npc.getMaxHp() * 0.95)) && (_zone.isInsideZone(npc) || !npc.isScriptValue(0))) {
            if (npc.isScriptValue(1) && !_zone.isInsideZone(npc)) {
               this.setSpawnPoint(npc, 0);
            }
         } else {
            this.setSpawnPoint(npc, getRandom(3) + 1);
            npc.setScriptValue(0);
         }
      } else if (event.equalsIgnoreCase("check_minion_loc")) {
         for(int i = 0; i < _minions.size(); ++i) {
            Attackable mob = _minions.get(i);
            if (mob != null && !npc.isInsideRadius(mob, 3000, false, false)) {
               mob.teleToLocation(npc.getX(), npc.getY(), npc.getZ(), true);
               ((Attackable)npc).clearAggroList();
               npc.getAI().setIntention(CtrlIntention.IDLE, null, null);
            }
         }
      } else if (event.equalsIgnoreCase("despawn_minions")) {
         for(int i = 0; i < _minions.size(); ++i) {
            Attackable mob = _minions.get(i);
            if (mob != null) {
               mob.decayMe();
            }
         }

         _minions.clear();
      } else if (event.equalsIgnoreCase("spawn_minion")) {
         Attackable mob = (Attackable)addSpawn(29016, npc.getX(), npc.getY(), npc.getZ(), 0, false, 0L);
         mob.setIsRaidMinion(true);
         _minions.add(mob);
      }

      return super.onAdvEvent(event, npc, player);
   }

   @Override
   public String onSkillSee(Npc npc, Player caster, Skill skill, GameObject[] targets, boolean isSummon) {
      if (npc.getId() == 29014) {
         Creature originalCaster = (Creature)(isSummon ? caster.getSummon() : caster);
         if (skill.getAggroPoints() > 0 && Rnd.chance(5) && npc.isInsideRadius(originalCaster, 1000, false, false)) {
            NpcSay packet = new NpcSay(npc.getObjectId(), 22, npc.getId(), _text[getRandom(4)]);
            packet.addStringParameter(caster.getName().toString());
            npc.broadcastPacket(packet, 2000);
            originalCaster.teleToLocation(npc.getX(), npc.getY(), npc.getZ(), true);
            npc.setTarget(originalCaster);
            npc.doCast(SkillsParser.getInstance().getInfo(4064, 1));
         }
      }

      return super.onSkillSee(npc, caster, skill, targets, isSummon);
   }

   @Override
   public String onFactionCall(Npc npc, Npc caller, Player attacker, boolean isSummon) {
      if (caller != null && npc != null && !npc.isCastingNow()) {
         int npcId = npc.getId();
         int callerId = caller.getId();
         if (npcId == 29016 && getRandom(20) == 0) {
            npc.setTarget(attacker);
            npc.doCast(SkillsParser.getInstance().getInfo(4067, 4));
         } else if (npcId == 29018) {
            int chance = 1;
            if (callerId == 29014) {
               chance = 9;
            }

            if (callerId != 29018 && caller.getCurrentHp() < caller.getMaxHp() / 2.0 && getRandom(10) < chance) {
               npc.getAI().setIntention(CtrlIntention.IDLE, null, null);
               npc.setTarget(caller);
               npc.doCast(SkillsParser.getInstance().getInfo(4516, 1));
            }
         }

         return super.onFactionCall(npc, caller, attacker, isSummon);
      } else {
         return super.onFactionCall(npc, caller, attacker, isSummon);
      }
   }

   @Override
   public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon) {
      int npcId = npc.getId();
      if (npcId == 29014) {
         if (npc.isScriptValue(0) && npc.getCurrentHp() - (double)damage < npc.getMaxHp() / 2.0) {
            npc.setScriptValue(1);
            this.setSpawnPoint(npc, 0);
         } else if (npc.isInsideRadius(attacker, 1000, false, false)
            && !npc.isInsideRadius(attacker, 300, false, false)
            && Rnd.chance(1)
            && this._lastMsg + 120000L < System.currentTimeMillis()) {
            this._lastMsg = System.currentTimeMillis();
            NpcSay packet = new NpcSay(npc.getObjectId(), 22, npcId, _text[getRandom(3)]);
            packet.addStringParameter(attacker.getName().toString());
            npc.broadcastPacket(packet, 2000);
            attacker.teleToLocation(npc.getX(), npc.getY(), npc.getZ(), true);
            npc.setTarget(attacker);
            npc.doCast(SkillsParser.getInstance().getInfo(4064, 1));
         }
      } else if (npcId == 29018 && !npc.isCastingNow() && npc.getCurrentHp() - (double)damage < npc.getMaxHp() / 2.0) {
         npc.setTarget(attacker);
         npc.doCast(SkillsParser.getInstance().getInfo(4516, 1));
      }

      return super.onAttack(npc, attacker, damage, isSummon);
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      if (npc.getId() == 29014) {
         this._lastMsg = 0L;
         npc.broadcastPacket(new PlaySound(1, "BS02_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
         long respawnTime = EpicBossManager.getInstance().setRespawnTime(29014, Config.ORFEN_RESPAWN_PATTERN);
         this.startQuestTimer("orfen_unlock", respawnTime - System.currentTimeMillis(), null, null);
         this.cancelQuestTimer("check_minion_loc", npc, null);
         this.cancelQuestTimer("check_orfen_pos", npc, null);
         this.startQuestTimer("despawn_minions", 20000L, null, null);
         this.cancelQuestTimers("spawn_minion");
         this._boss = null;
      } else if (EpicBossManager.getInstance().getBossStatus(29014) == 0 && npc.getId() == 29016) {
         _minions.remove(npc);
         this.startQuestTimer("spawn_minion", 360000L, npc, null);
      }

      return super.onKill(npc, killer, isSummon);
   }

   @Override
   public boolean unload(boolean removeFromList) {
      if (this._boss != null) {
         this._boss.deleteMe();
         this._boss = null;
      }

      if (_minions != null) {
         for(int i = 0; i < _minions.size(); ++i) {
            Attackable mob = _minions.get(i);
            if (mob != null) {
               mob.decayMe();
            }
         }
      }

      _minions.clear();
      this.cancelQuestTimers("orfen_unlock");
      return super.unload(removeFromList);
   }

   public static void main(String[] args) {
      new Orfen(Orfen.class.getSimpleName(), "ai");
   }
}
