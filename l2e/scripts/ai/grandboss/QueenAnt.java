package l2e.scripts.ai.grandboss;

import l2e.gameserver.Config;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.instancemanager.EpicBossManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.GrandBossInstance;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.stats.StatsSet;
import l2e.gameserver.model.zone.type.BossZone;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.gameserver.network.serverpackets.PlaySound;
import l2e.scripts.ai.AbstractNpcAI;

public class QueenAnt extends AbstractNpcAI {
   private static BossZone _zone = (BossZone)ZoneManager.getInstance().getZoneById(12012);
   private static MonsterInstance _larva = null;
   private GrandBossInstance _boss = null;

   private QueenAnt(String name, String descr) {
      super(name, descr);
      this.addSpawnId(new int[]{29002, 29004});
      this.addKillId(29001);
      this.addAggroRangeEnterId(new int[]{29001, 29002, 29003, 29004, 29005});
      StatsSet info = EpicBossManager.getInstance().getStatsSet(29001);
      if (this.getStatus() == 3) {
         long temp = info.getLong("respawnTime") - System.currentTimeMillis();
         if (temp > 0L) {
            this.startQuestTimer("queen_unlock", temp, null, null);
         } else {
            this._boss = (GrandBossInstance)addSpawn(29001, -21610, 181594, -5720, 0, false, 0L);
            EpicBossManager.getInstance().setBossStatus(29001, 0, false);
            this.spawnBoss(this._boss);
         }
      } else {
         int loc_x = info.getInteger("loc_x");
         int loc_y = info.getInteger("loc_y");
         int loc_z = info.getInteger("loc_z");
         int heading = info.getInteger("heading");
         int hp = info.getInteger("currentHP");
         int mp = info.getInteger("currentMP");
         if (!_zone.isInsideZone(loc_x, loc_y, loc_z)) {
            loc_x = -21610;
            loc_y = 181594;
            loc_z = -5720;
         }

         this._boss = (GrandBossInstance)addSpawn(29001, loc_x, loc_y, loc_z, heading, false, 0L);
         this._boss.setCurrentHpMp((double)hp, (double)mp);
         this.spawnBoss(this._boss);
      }
   }

   private void spawnBoss(GrandBossInstance npc) {
      EpicBossManager.getInstance().addBoss(npc);
      this.startQuestTimer("action", 10000L, npc, null, true);
      npc.broadcastPacket(new PlaySound(1, "BS01_A", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
      _larva = (MonsterInstance)addSpawn(29002, -21600, 179482, -5846, getRandom(360), false, 0L);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      if (event.equalsIgnoreCase("action") && npc != null) {
         if (getRandom(3) == 0) {
            if (getRandom(2) == 0) {
               npc.broadcastSocialAction(3);
            } else {
               npc.broadcastSocialAction(4);
            }
         }
      } else if (event.equalsIgnoreCase("queen_unlock")) {
         this._boss = (GrandBossInstance)addSpawn(29001, -21610, 181594, -5720, 0, false, 0L);
         EpicBossManager.getInstance().setBossStatus(29001, 0, true);
         this.spawnBoss(this._boss);
      }

      return super.onAdvEvent(event, npc, player);
   }

   @Override
   public String onSpawn(Npc npc) {
      MonsterInstance mob = (MonsterInstance)npc;
      switch(npc.getId()) {
         case 29002:
            mob.setIsMortal(false);
            mob.setIsRaidMinion(true);
            break;
         case 29004:
            mob.setIsRaidMinion(true);
      }

      return super.onSpawn(npc);
   }

   @Override
   public String onAggroRangeEnter(Npc npc, Player player, boolean isSummon) {
      if (npc != null && (!player.isGM() || !player.isInvisible())) {
         boolean isMage;
         Playable character;
         if (isSummon) {
            isMage = false;
            character = player.getSummon();
         } else {
            isMage = player.isMageClass();
            character = player;
         }

         if (character == null) {
            return null;
         } else if (!Config.RAID_DISABLE_CURSE && character.getLevel() - npc.getLevel() > 8) {
            Skill curse = null;
            if (isMage) {
               if (!character.isMuted() && getRandom(4) == 0) {
                  curse = SkillsParser.FrequentSkill.RAID_CURSE.getSkill();
               }
            } else if (!character.isParalyzed() && getRandom(4) == 0) {
               curse = SkillsParser.FrequentSkill.RAID_CURSE2.getSkill();
            }

            if (curse != null) {
               npc.broadcastPacket(new MagicSkillUse(npc, character, curse.getId(), curse.getLevel(), 300, 0));
               curse.getEffects(npc, character, false);
            }

            ((Attackable)npc).stopHating(character);
            return null;
         } else {
            return super.onAggroRangeEnter(npc, player, isSummon);
         }
      } else {
         return null;
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      int npcId = npc.getId();
      if (npcId == 29001) {
         npc.broadcastPacket(new PlaySound(1, "BS02_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
         long respawnTime = EpicBossManager.getInstance().setRespawnTime(29001, Config.QUEEN_ANT_RESPAWN_PATTERN);
         this.startQuestTimer("queen_unlock", respawnTime - System.currentTimeMillis(), null, null);
         this.cancelQuestTimer("action", npc, null);
         if (_larva != null) {
            _larva.deleteMe();
            _larva = null;
         }

         this._boss = null;
      }

      return super.onKill(npc, killer, isSummon);
   }

   @Override
   public boolean unload(boolean removeFromList) {
      if (_larva != null) {
         _larva.deleteMe();
         _larva = null;
      }

      if (this._boss != null) {
         this._boss.deleteMe();
         this._boss = null;
      }

      this.cancelQuestTimers("queen_unlock");
      return super.unload(removeFromList);
   }

   private int getStatus() {
      return EpicBossManager.getInstance().getBossStatus(29001);
   }

   public static MonsterInstance getLarva() {
      return _larva;
   }

   public static void main(String[] args) {
      new QueenAnt(QueenAnt.class.getSimpleName(), "ai");
   }
}
