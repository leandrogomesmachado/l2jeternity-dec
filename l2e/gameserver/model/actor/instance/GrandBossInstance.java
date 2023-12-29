package l2e.gameserver.model.actor.instance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import l2e.commons.threading.RunnableImpl;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.parser.ExperienceParser;
import l2e.gameserver.instancemanager.RaidBossSpawnManager;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.CommandChannel;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.entity.Hero;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.strings.server.ServerStorage;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class GrandBossInstance extends MonsterInstance {
   private boolean _useRaidCurse = true;
   private final Map<String, Integer> _damageInfo = new HashMap<>();
   private long _infoUpdateTime = 0L;
   private long _infoTotalTime = 0L;
   private ScheduledFuture<?> _minionMaintainTask;

   public GrandBossInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.GrandBossInstance);
      this.setIsRaid(true);
      this.setIsEpicRaid(true);
   }

   @Override
   protected int getMaintenanceInterval() {
      return 10000;
   }

   @Override
   public void onSpawn() {
      this.setIsNoRndWalk(true);
      super.onSpawn();
   }

   @Override
   public void reduceCurrentHp(double damage, Creature attacker, boolean awake, boolean isDOT, Skill skill) {
      if (Config.ALLOW_DAMAGE_INFO && attacker != null && damage > 0.0) {
         Player player = attacker.getActingPlayer();
         if (player != null && player.getClan() != null) {
            if (this._infoTotalTime != 0L && this._infoTotalTime + (long)(Config.DAMAGE_INFO_LIMIT_TIME * 3600000) < System.currentTimeMillis()) {
               this._infoTotalTime = 0L;
               this._infoUpdateTime = 0L;
               this._damageInfo.clear();
            }

            this.checkInfoDamage(player.getClan(), damage);
         }
      }

      super.reduceCurrentHp(damage, attacker, awake, isDOT, skill);
   }

   @Override
   public int getKilledInterval(MinionInstance minion) {
      int respawnTime = Config.MINIONS_RESPAWN_TIME.containsKey(minion.getId()) ? Config.MINIONS_RESPAWN_TIME.get(minion.getId()) * 1000 : -1;
      return respawnTime < 0 ? (minion.getLeader().isRaid() ? (int)Config.RAID_MINION_RESPAWN_TIMER : 0) : respawnTime;
   }

   @Override
   public void notifyMinionDied(MinionInstance minion) {
      int respawnTime = this.getKilledInterval(minion);
      if (respawnTime > 0) {
         this._minionMaintainTask = ThreadPoolManager.getInstance().schedule(new GrandBossInstance.MaintainKilledMinion(minion), (long)respawnTime);
      }

      super.notifyMinionDied(minion);
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
               if (GrandBossInstance.this.isDead()) {
                  GrandBossInstance.this.getMinionList().unspawnMinions();
               }
            }
         }, (long)this.getMinionUnspawnInterval());
      }

      if (Config.ALLOW_DAMAGE_INFO) {
         this._infoTotalTime = 0L;
         this._infoUpdateTime = 0L;
         this._damageInfo.clear();
      }

      for(Attackable.AggroInfo ai : this.getAggroList().values()) {
         if (ai.getAttacker() instanceof QuestGuardInstance) {
            this.getAggroList().remove(ai.getAttacker());
         }
      }

      if (killer == null) {
         super.onDeath(killer);
      } else {
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

            player.getCounters().addAchivementInfo("epicKiller", this.getId(), -1L, false, true, false);
         }

         super.onDeath(killer);
      }
   }

   private void calcRaidPointsReward(int totalPoints) {
      Map<Object, GrandBossInstance.GroupInfo> groupsInfo = new HashMap<>();
      double totalHp = this.getMaxHp();

      for(Attackable.AggroInfo ai : this.getAggroList().values()) {
         Player player = ai.getAttacker().getActingPlayer();
         if (player != null) {
            Object key = player.getParty() != null
               ? (player.getParty().getCommandChannel() != null ? player.getParty().getCommandChannel() : player.getParty())
               : player.getActingPlayer();
            GrandBossInstance.GroupInfo info = groupsInfo.get(key);
            if (info == null) {
               info = new GrandBossInstance.GroupInfo();
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

      for(GrandBossInstance.GroupInfo groupInfo : groupsInfo.values()) {
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

   private void checkInfoDamage(Clan clan, double damage) {
      if (this._infoUpdateTime == 0L) {
         this._infoUpdateTime = System.currentTimeMillis();
         this._infoTotalTime = System.currentTimeMillis();
      }

      if (this._damageInfo.containsKey(clan.getName())) {
         double totalDamage = (double)this._damageInfo.get(clan.getName()).intValue();
         this._damageInfo.put(clan.getName(), (int)(totalDamage + damage));
      } else {
         this._damageInfo.put(clan.getName(), (int)damage);
      }

      if (this._infoUpdateTime + (long)Config.DAMAGE_INFO_UPDATE * 1000L < System.currentTimeMillis()) {
         this._infoUpdateTime = System.currentTimeMillis();
         if (this._damageInfo != null) {
            List<GrandBossInstance.DamageInfo> damageList = new ArrayList<>();
            StringBuilder builderEn = new StringBuilder();
            StringBuilder builderRu = new StringBuilder();

            for(String clanName : this._damageInfo.keySet()) {
               damageList.add(new GrandBossInstance.DamageInfo(clanName, this._damageInfo.get(clanName)));
            }

            Comparator<GrandBossInstance.DamageInfo> statsComparator = new GrandBossInstance.SortDamageInfo();
            Collections.sort(damageList, statsComparator);

            for(GrandBossInstance.DamageInfo info : damageList) {
               if (info != null) {
                  builderEn.append("" + ServerStorage.getInstance().getString("en", "EpicDamageInfo.CLAN") + "")
                     .append(' ')
                     .append(info.getClanName())
                     .append(": ")
                     .append(getDamageFormat(info.getDamage(), "en"))
                     .append('\n');
                  builderRu.append("" + ServerStorage.getInstance().getString("ru", "EpicDamageInfo.CLAN") + "")
                     .append(' ')
                     .append(info.getClanName())
                     .append(": ")
                     .append(getDamageFormat(info.getDamage(), "ru"))
                     .append('\n');
               }
            }

            ExShowScreenMessage msgEn = new ExShowScreenMessage(builderEn.toString(), Config.DAMAGE_INFO_UPDATE * 1000, (byte)1, false);
            ExShowScreenMessage msgRu = new ExShowScreenMessage(builderRu.toString(), Config.DAMAGE_INFO_UPDATE * 1000, (byte)1, false);

            for(Player player : World.getInstance().getAroundPlayers(this, 2000, 200)) {
               player.sendPacket(player.getLang().equalsIgnoreCase("ru") ? msgRu : msgEn);
            }
         }
      }
   }

   private static String getDamageFormat(int damage, String lang) {
      String scount = Integer.toString(damage);
      if (damage < 1000) {
         return scount;
      } else if (damage > 999 && damage < 1000000) {
         return scount.substring(0, scount.length() - 3) + "" + ServerStorage.getInstance().getString(lang, "EpicDamageInfo.K");
      } else if (damage > 999999 && damage < 1000000000) {
         return scount.substring(0, scount.length() - 6) + "" + ServerStorage.getInstance().getString(lang, "EpicDamageInfo.KK");
      } else {
         return damage > 999999999 ? scount.substring(0, scount.length() - 9) + "" + ServerStorage.getInstance().getString(lang, "EpicDamageInfo.KKK") : "0";
      }
   }

   private static class DamageInfo {
      private final String _clan;
      private final int _damage;

      public DamageInfo(String clan, int damage) {
         this._clan = clan;
         this._damage = damage;
      }

      public final String getClanName() {
         return this._clan;
      }

      public final int getDamage() {
         return this._damage;
      }
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
         if (!GrandBossInstance.this.isDead()) {
            this._minion.refreshID();
            GrandBossInstance.this.spawnMinion(this._minion);
         }
      }
   }

   private static class SortDamageInfo implements Comparator<GrandBossInstance.DamageInfo>, Serializable {
      private static final long serialVersionUID = 7691414259610932752L;

      private SortDamageInfo() {
      }

      public int compare(GrandBossInstance.DamageInfo o1, GrandBossInstance.DamageInfo o2) {
         return Integer.compare(o2.getDamage(), o1.getDamage());
      }
   }
}
