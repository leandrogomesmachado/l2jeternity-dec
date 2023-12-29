package l2e.gameserver.model.entity.underground_coliseum;

import java.util.List;
import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.instancemanager.UndergroundColiseumManager;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.actor.instance.UCTowerInstance;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExPCCafePointInfo;
import l2e.gameserver.network.serverpackets.ExPVPMatchUserDie;
import l2e.gameserver.network.serverpackets.GameServerPacket;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class UCTeam {
   public static final byte NOT_DECIDED = 0;
   public static final byte WIN = 1;
   public static final byte FAIL = 2;
   private final int _index;
   private final UCArena _baseArena;
   protected final int _x;
   protected final int _y;
   protected final int _z;
   private final int _npcId;
   private UCTowerInstance _tower = null;
   private Party _party;
   private int _killCount;
   private byte _status;
   private Party _lastParty;
   private int _consecutiveWins;
   private long _registerTime;

   public UCTeam(int index, UCArena baseArena, int x, int y, int z, int npcId) {
      this._index = index;
      this._baseArena = baseArena;
      this._x = x;
      this._y = y;
      this._z = z;
      this._npcId = npcId;
      this.setStatus((byte)0);
   }

   public long getRegisterTime() {
      return this._registerTime;
   }

   public void setLastParty(Party party) {
      this._lastParty = party;
   }

   public void setRegisterTime(long time) {
      this._registerTime = time;
   }

   public void increaseConsecutiveWins() {
      ++this._consecutiveWins;
      if (this._consecutiveWins > 1 && this._party != null && this._party.getLeader() != null) {
         UndergroundColiseumManager.getInstance().updateBestTeam(this._baseArena.getId(), this._party.getLeader().getName(), this._consecutiveWins);
      }
   }

   public int getConsecutiveWins() {
      return this._consecutiveWins;
   }

   public void spawnTower() {
      if (this._tower == null) {
         NpcTemplate template = NpcsParser.getInstance().getTemplate(this._npcId);
         if (template != null) {
            this._tower = new UCTowerInstance(this, IdFactory.getInstance().getNextId(), template);
            this._tower.setIsInvul(false);
            this._tower.setCurrentHpMp(this._tower.getMaxHp(), this._tower.getMaxMp());
            this._tower.spawnMe(this._x, this._y, this._z);
         }
      }
   }

   public void deleteTower() {
      if (this._tower != null) {
         this._tower.deleteMe();
         this._tower = null;
      }
   }

   public void onKill(final Player player, Player killer) {
      if (player != null && killer != null && this.getParty() != null) {
         if (!player.isInSameParty(killer)) {
            UCTeam otherTeam = this.getOtherTeam();
            otherTeam.increaseKillCount();
            player.addDeathCountUC();
            killer.addKillCountUC();
            this._baseArena.broadcastToAll(new ExPVPMatchUserDie(this._baseArena));
            if (player.getUCState() == 1) {
               for(UCPoint point : this._baseArena.getPoints()) {
                  if (point.checkPlayer(player)) {
                     break;
                  }
               }
            }

            if (this._tower == null) {
               boolean flag = true;

               for(Player member : this.getParty().getMembers()) {
                  if (member != null && !member.isDead()) {
                     flag = false;
                  }
               }

               if (flag) {
                  this.setStatus((byte)2);
                  otherTeam.setStatus((byte)1);
                  this._baseArena.runTaskNow();
               }
            } else {
               ThreadPoolManager.getInstance().schedule(new Runnable() {
                  @Override
                  public void run() {
                     if (UCTeam.this._tower != null) {
                        if (player.isDead()) {
                           UCTeam.resPlayer(player);
                           player.teleToLocation(UCTeam.this._x + Rnd.get(2, 50), UCTeam.this._y + Rnd.get(10, 100), UCTeam.this._z, true);
                           if (player.hasSummon()) {
                              Summon summon = player.getSummon();
                              summon.abortAttack();
                              summon.abortCast();
                              if (!summon.isDead()) {
                                 summon.setCurrentHp(summon.getMaxHp());
                                 summon.setCurrentMp(summon.getMaxMp());
                                 summon.teleToLocation(UCTeam.this._x + Rnd.get(2, 50), UCTeam.this._y + Rnd.get(10, 100), UCTeam.this._z, true);
                              }
                           }
                        }
                     }
                  }
               }, (long)(Config.UC_RESS_TIME * 1000));
            }
         }
      }
   }

   public void increaseKillCount() {
      ++this._killCount;
   }

   public static void resPlayer(Player player) {
      if (player != null) {
         player.restoreExp(100.0);
         player.doRevive();
         player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
         player.setCurrentCp(player.getMaxCp());
      }
   }

   public void cleanUp() {
      if (this.getParty() != null) {
         this.getParty().setUCState(null);
         this._party = null;
      }

      this._party = null;
      this._lastParty = null;
      this._consecutiveWins = 0;
      this.setStatus((byte)0);
      this._killCount = 0;
   }

   public byte getStatus() {
      return this._status;
   }

   public UCArena getBaseArena() {
      return this._baseArena;
   }

   public void computeReward() {
      if (this._lastParty == null || this._lastParty != this.getOtherTeam().getParty()) {
         List<UCReward> rewards = this._baseArena.getRewards();
         double modifier = 1.0;
         switch(this._consecutiveWins) {
            case 1:
               modifier = 1.0;
               break;
            case 2:
               modifier = 1.06;
               break;
            case 3:
               modifier = 1.12;
               break;
            case 4:
               modifier = 1.18;
               break;
            case 5:
               modifier = 1.25;
               break;
            case 6:
               modifier = 1.27;
               break;
            case 7:
               modifier = 1.3;
               break;
            case 8:
               modifier = 1.32;
               break;
            case 9:
               modifier = 1.35;
               break;
            case 10:
               modifier = 1.37;
               break;
            default:
               if (this._consecutiveWins > 10) {
                  modifier = 1.4;
               }
         }

         if (rewards == null || rewards.isEmpty()) {
            return;
         }

         for(Player member : this.getParty().getMembers()) {
            if (member != null) {
               for(UCReward reward : rewards) {
                  if (reward.getId() == -100) {
                     long amount = reward.isAllowMidifier() ? (long)((double)reward.getAmount() * modifier) : reward.getAmount();
                     if ((long)member.getPcBangPoints() + amount > (long)Config.MAX_PC_BANG_POINTS) {
                        amount = (long)(Config.MAX_PC_BANG_POINTS - member.getPcBangPoints());
                     }

                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_ACQUIRED_S1_PC_CAFE_POINTS);
                     sm.addNumber((int)amount);
                     member.sendPacket(sm);
                     member.setPcBangPoints((int)((long)member.getPcBangPoints() + amount));
                     member.sendPacket(new ExPCCafePointInfo(member.getPcBangPoints(), (int)amount, true, false, 1));
                  } else if (reward.getId() == -200) {
                     if (member.getClan() != null) {
                        long amount = reward.isAllowMidifier() ? (long)((double)reward.getAmount() * modifier) : reward.getAmount();
                        member.getClan().addReputationScore((int)amount, true);
                     }
                  } else if (reward.getId() == -300) {
                     long amount = reward.isAllowMidifier() ? (long)((double)reward.getAmount() * modifier) : reward.getAmount();
                     member.setFame((int)((long)member.getFame() + amount));
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_REPUTATION_SCORE);
                     sm.addNumber((int)amount);
                     member.sendPacket(sm);
                     member.sendUserInfo();
                  } else if (reward.getId() > 0) {
                     long amount = reward.isAllowMidifier() ? (long)((double)reward.getAmount() * modifier) : reward.getAmount();
                     member.addItem("UC reward", reward.getId(), amount, null, true);
                  }
               }
            }
         }
      }
   }

   public void setStatus(byte status) {
      this._status = status;
      if (this._status == 1) {
         if (this.getIndex() == 0) {
            this._baseArena.broadcastToAll(SystemMessage.getSystemMessage(SystemMessageId.THE_BLUE_TEAM_IS_VICTORIOUS));
         } else {
            this._baseArena.broadcastToAll(SystemMessage.getSystemMessage(SystemMessageId.THE_RED_TEAM_IS_VICTORIOUS));
         }
      }

      switch(this._status) {
         case 0:
         default:
            break;
         case 1:
            this.increaseConsecutiveWins();
            this.computeReward();
            this.deleteTower();
            break;
         case 2:
            this.deleteTower();
      }
   }

   public void broadcastToTeam(GameServerPacket packet) {
      Party party = this._party;
      if (party != null) {
         for(Player member : party.getMembers()) {
            if (member != null) {
               member.sendPacket(packet);
            }
         }
      }
   }

   public UCTeam getOtherTeam() {
      return this._baseArena.getTeams()[this.getOtherTeamIndex()];
   }

   public int getOtherTeamIndex() {
      return this._index == 0 ? 1 : 0;
   }

   public int getKillCount() {
      return this._killCount;
   }

   public void setParty(Party party) {
      Party oldParty = this._party;
      this._party = party;
      if (oldParty != null) {
         oldParty.setUCState(null);
      }

      if (this._party != null) {
         this._party.setUCState(this);
      }
   }

   public Party getParty() {
      return this._party;
   }

   public int getIndex() {
      return this._index;
   }
}
