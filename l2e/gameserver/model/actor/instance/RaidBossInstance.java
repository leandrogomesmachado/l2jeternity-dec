package l2e.gameserver.model.actor.instance;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import l2e.commons.threading.RunnableImpl;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.parser.ExperienceParser;
import l2e.gameserver.instancemanager.RaidBossSpawnManager;
import l2e.gameserver.model.CommandChannel;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.entity.Hero;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class RaidBossInstance extends MonsterInstance {
   private RaidBossSpawnManager.StatusEnum _raidStatus;
   private boolean _useRaidCurse = true;
   private ScheduledFuture<?> _minionMaintainTask;

   public RaidBossInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.RaidBossInstance);
      this.setIsRaid(true);
   }

   @Override
   protected int getMaintenanceInterval() {
      return 30000;
   }

   @Override
   public int getKilledInterval(MinionInstance minion) {
      int respawnTime = Config.MINIONS_RESPAWN_TIME.containsKey(minion.getId()) ? Config.MINIONS_RESPAWN_TIME.get(minion.getId()) * 1000 : -1;
      return respawnTime < 0 ? (minion.getLeader().isRaid() ? (int)Config.RAID_MINION_RESPAWN_TIMER : 0) : respawnTime;
   }

   @Override
   public void onSpawn() {
      this.setIsNoRndWalk(true);
      super.onSpawn();
   }

   @Override
   protected void onDeath(Creature killer) {
      if (this._minionMaintainTask != null) {
         this._minionMaintainTask.cancel(false);
         this._minionMaintainTask = null;
      }

      if (this.getMinionList().hasAliveMinions()) {
         ThreadPoolManager.getInstance().schedule(new RunnableImpl() {
            @Override
            public void runImpl() {
               if (RaidBossInstance.this.isDead()) {
                  RaidBossInstance.this.getMinionList().unspawnMinions();
               }
            }
         }, (long)this.getMinionUnspawnInterval());
      }

      int points = this.getTemplate().getRewardRp();
      if (points > 0) {
         this.calcRaidPointsReward(points);
      }

      for(Attackable.AggroInfo ai : this.getAggroList().values()) {
         Player player = ai.getAttacker().getActingPlayer();
         if (player != null && player.isCastingNow()) {
            player.abortCast();
         }
      }

      Player player = killer.getActingPlayer();
      if (player != null) {
         this.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.RAID_WAS_SUCCESSFUL));
         if (player.getParty() != null) {
            for(Player member : player.getParty().getMembers()) {
               if (member.isNoble()) {
                  Hero.getInstance().setRBkilled(member.getObjectId(), this.getId());
               }
            }
         } else if (player.isNoble()) {
            Hero.getInstance().setRBkilled(player.getObjectId(), this.getId());
         }

         player.getCounters().addAchivementInfo("raidKiller", this.getId(), -1L, false, true, false);
      }

      RaidBossSpawnManager.getInstance().updateStatus(this, true);
      super.onDeath(killer);
   }

   private void calcRaidPointsReward(int totalPoints) {
      Map<Object, RaidBossInstance.GroupInfo> groupsInfo = new HashMap<>();
      double totalHp = this.getMaxHp();

      for(Attackable.AggroInfo ai : this.getAggroList().values()) {
         Player player = ai.getAttacker().getActingPlayer();
         if (player != null) {
            Object key = player.getParty() != null
               ? (player.getParty().getCommandChannel() != null ? player.getParty().getCommandChannel() : player.getParty())
               : player.getActingPlayer();
            RaidBossInstance.GroupInfo info = groupsInfo.get(key);
            if (info == null) {
               info = new RaidBossInstance.GroupInfo();
               groupsInfo.put(key, info);
            }

            if (key instanceof CommandChannel) {
               for(Player p : (CommandChannel)key) {
                  if (p.isInRangeZ(this, (long)Config.ALT_PARTY_RANGE2)) {
                     info._players.add(p);
                  }
               }
            } else if (key instanceof Party) {
               for(Player p : ((Party)key).getMembers()) {
                  if (p.isInRangeZ(this, (long)Config.ALT_PARTY_RANGE2)) {
                     info._players.add(p);
                  }
               }
            } else {
               info._players.add(player);
            }

            info._reward += (long)ai.getDamage();
         }
      }

      for(RaidBossInstance.GroupInfo groupInfo : groupsInfo.values()) {
         HashSet<Player> players = groupInfo._players;
         int perPlayer = (int)Math.round((double)((long)totalPoints * groupInfo._reward) / (totalHp * (double)players.size()));

         for(Player player : players) {
            if (player != null) {
               int var20 = (int)Math.round(
                  (double)perPlayer * ExperienceParser.getInstance().penaltyModifier((long)this.calculateLevelDiffForDrop(player.getLevel()), 9.0)
               );
               if (var20 != 0) {
                  player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1_RAID_POINTS).addNumber(var20));
                  RaidBossSpawnManager.getInstance().addPoints(player, this.getId(), var20);
               }
            }
         }
      }

      RaidBossSpawnManager.getInstance().updatePointsDb();
      RaidBossSpawnManager.getInstance().calculateRanking();
   }

   @Override
   public void notifyMinionDied(MinionInstance minion) {
      int respawnTime = this.getKilledInterval(minion);
      if (respawnTime > 0) {
         this._minionMaintainTask = ThreadPoolManager.getInstance().schedule(new RaidBossInstance.MaintainKilledMinion(minion), (long)respawnTime);
      }

      super.notifyMinionDied(minion);
   }

   public void setRaidStatus(RaidBossSpawnManager.StatusEnum status) {
      this._raidStatus = status;
   }

   public RaidBossSpawnManager.StatusEnum getRaidStatus() {
      return this._raidStatus;
   }

   @Override
   public double getVitalityPoints(int damage) {
      return -super.getVitalityPoints(damage) / 100.0 + Config.VITALITY_RAID_BONUS;
   }

   @Override
   public boolean useVitalityRate() {
      return false;
   }

   public void setUseRaidCurse(boolean val) {
      this._useRaidCurse = val;
   }

   @Override
   public boolean giveRaidCurse() {
      return this._useRaidCurse;
   }

   private class GroupInfo {
      public HashSet<Player> _players = new HashSet<>();
      public long _reward = 0L;

      public GroupInfo() {
      }
   }

   private class MaintainKilledMinion extends RunnableImpl {
      private final MinionInstance _minion;

      public MaintainKilledMinion(MinionInstance minion) {
         this._minion = minion;
      }

      @Override
      public void runImpl() {
         if (!RaidBossInstance.this.isDead()) {
            this._minion.refreshID();
            RaidBossInstance.this.spawnMinion(this._minion);
         }
      }
   }
}
