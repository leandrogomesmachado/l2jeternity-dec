package l2e.gameserver.model.olympiad;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.SkillTreesParser;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.handler.voicedcommandhandlers.VoicedCommandHandler;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.DailyTaskManager;
import l2e.gameserver.instancemanager.DoubleSessionManager;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.actor.templates.daily.DailyTaskTemplate;
import l2e.gameserver.model.actor.templates.player.PlayerTaskTemplate;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.zone.type.OlympiadStadiumZone;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExOlympiadMode;
import l2e.gameserver.network.serverpackets.GameServerPacket;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public abstract class AbstractOlympiadGame {
   protected static final Logger _log = Logger.getLogger(AbstractOlympiadGame.class.getName());
   protected static final Logger _logResults = Logger.getLogger("olympiad");
   protected static final String POINTS = "olympiad_points";
   protected static final String COMP_DONE = "competitions_done";
   protected static final String COMP_WON = "competitions_won";
   protected static final String COMP_LOST = "competitions_lost";
   protected static final String COMP_DRAWN = "competitions_drawn";
   protected static final String COMP_DONE_WEEK = "competitions_done_week";
   protected static final String COMP_DONE_WEEK_CLASSED = "competitions_done_week_classed";
   protected static final String COMP_DONE_WEEK_NON_CLASSED = "competitions_done_week_non_classed";
   protected static final String COMP_DONE_WEEK_TEAM = "competitions_done_week_team";
   protected long _startTime = 0L;
   protected boolean _aborted = false;
   protected final int _stadiumID;
   protected OlympiadStadiumZone _zone;
   public boolean _hasEnded = false;

   protected AbstractOlympiadGame(int id) {
      this._stadiumID = id;
   }

   public final boolean isAborted() {
      return this._aborted;
   }

   public final int getStadiumId() {
      return this._stadiumID;
   }

   protected boolean makeCompetitionStart() {
      this._startTime = System.currentTimeMillis();
      return !this._aborted;
   }

   protected final void addPointsToParticipant(Participant par, int points) {
      par.updateStat("olympiad_points", points);
      SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_GAINED_S2_OLYMPIAD_POINTS);
      sm.addString(par.getName());
      sm.addNumber(points);
      this.broadcastPacket(sm);

      for(Quest quest : QuestManager.getInstance().getAllManagedScripts()) {
         if (quest != null && quest.isOlympiadUse()) {
            quest.notifyOlympiadWin(par.getPlayer(), this.getType());
         }
      }
   }

   protected final void removePointsFromParticipant(Participant par, int points) {
      par.updateStat("olympiad_points", -points);
      SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_LOST_S2_OLYMPIAD_POINTS);
      sm.addString(par.getName());
      sm.addNumber(points);
      this.broadcastPacket(sm);

      for(Quest quest : QuestManager.getInstance().getAllManagedScripts()) {
         if (quest != null && quest.isOlympiadUse()) {
            quest.notifyOlympiadLose(par.getPlayer(), this.getType());
         }
      }
   }

   protected static SystemMessage checkDefaulted(Player player) {
      if (player == null || !player.isOnline()) {
         return SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_ENDS_THE_GAME);
      } else if (player.getClient() == null || player.getClient().isDetached()) {
         return SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_ENDS_THE_GAME);
      } else if (player.isInFightEvent() || player.inObserverMode()) {
         return SystemMessage.getSystemMessage(
            SystemMessageId.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME
         );
      } else if (player.isDead()) {
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_CANNOT_PARTICIPATE_OLYMPIAD_WHILE_DEAD);
         sm.addPcName(player);
         player.sendPacket(sm);
         return SystemMessage.getSystemMessage(
            SystemMessageId.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME
         );
      } else if (player.isSubClassActive()) {
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_CANNOT_PARTICIPATE_IN_OLYMPIAD_WHILE_CHANGED_TO_SUB_CLASS);
         sm.addPcName(player);
         player.sendPacket(sm);
         return SystemMessage.getSystemMessage(
            SystemMessageId.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME
         );
      } else if (player.isCursedWeaponEquipped()) {
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_CANNOT_JOIN_OLYMPIAD_POSSESSING_S2);
         sm.addPcName(player);
         sm.addItemName(player.getCursedWeaponEquippedId());
         player.sendPacket(sm);
         return SystemMessage.getSystemMessage(
            SystemMessageId.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME
         );
      } else if (!player.isInventoryUnder90(true)) {
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_CANNOT_PARTICIPATE_IN_OLYMPIAD_INVENTORY_SLOT_EXCEEDS_80_PERCENT);
         sm.addPcName(player);
         player.sendPacket(sm);
         return SystemMessage.getSystemMessage(
            SystemMessageId.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME
         );
      } else {
         return null;
      }
   }

   protected static final boolean portPlayerToArena(Participant par, Location loc, int id) {
      Player player = par.getPlayer();
      if (player != null && player.isOnline()) {
         try {
            player.setLastCords(player.getX(), player.getY(), player.getZ());
            if (player.isSitting()) {
               player.standUp();
            }

            player.setTarget(null);
            player.setOlympiadGameId(id);
            player.setIsInOlympiadMode(true);
            player.setIsOlympiadStart(false);
            player.setOlympiadSide(par.getSide());
            player.olyBuff = 5;
            player.setReflectionId(OlympiadGameManager.getInstance().getOlympiadTask(id).getZone().getReflectionId());
            player.teleToLocation(loc, false);
            player.sendPacket(new ExOlympiadMode(2));
            return true;
         } catch (Exception var5) {
            _log.log(Level.WARNING, var5.getMessage(), (Throwable)var5);
            return false;
         }
      } else {
         return false;
      }
   }

   protected void setOlympiadGame(Participant par, AbstractOlympiadGame game) {
      if (par.getPlayer() != null) {
         par.getPlayer().setOlympiadGame(game);
      }
   }

   protected static final void removals(Player player, boolean removeParty) {
      try {
         if (player == null) {
            return;
         }

         player.stopAllEffectsExceptThoseThatLastThroughDeath();
         if (player.getClan() != null) {
            player.getClan().removeSkillEffects(player);
            if (player.getClan().getCastleId() > 0) {
               CastleManager.getInstance().getCastleByOwner(player.getClan()).removeResidentialSkills(player);
            }

            if (player.getClan().getFortId() > 0) {
               FortManager.getInstance().getFortByOwner(player.getClan()).removeResidentialSkills(player);
            }
         }

         player.abortAttack();
         player.abortCast();
         player.setInvisible(false);
         if (player.isHero()) {
            for(Skill skill : SkillTreesParser.getInstance().getHeroSkillTree().values()) {
               player.removeSkill(skill, false);
            }
         }

         player.setCurrentCp(player.getMaxCp());
         player.setCurrentHp(player.getMaxHp());
         player.setCurrentMp(player.getMaxMp());
         if (player.hasSummon()) {
            Summon summon = player.getSummon();
            summon.stopAllEffectsExceptThoseThatLastThroughDeath();
            summon.abortAttack();
            summon.abortCast();
            if (!summon.isDead()) {
               summon.setCurrentHp(summon.getMaxHp());
               summon.setCurrentMp(summon.getMaxMp());
            }

            if (summon.isPet()) {
               summon.unSummon(player);
            }
         }

         player.stopCubicsByOthers();
         if (removeParty) {
            Party party = player.getParty();
            if (party != null) {
               party.removePartyMember(player, Party.messageType.Expelled);
            }
         }

         if (player.getAgathionId() > 0) {
            player.setAgathionId(0);
            player.broadcastUserInfo(true);
         }

         player.checkItemRestriction();
         player.disableAutoShotsAll();
         ItemInstance item = player.getActiveWeaponInstance();
         if (item != null) {
            item.unChargeAllShots();
         }

         for(Skill skill : player.getAllSkills()) {
            if (skill.getReuseDelay() <= 900000) {
               player.enableSkill(skill);
            }
         }

         player.sendSkillList(true);
      } catch (Exception var5) {
         _log.log(Level.WARNING, var5.getMessage(), (Throwable)var5);
      }
   }

   protected static final void restorePlayer(Player player) {
      if (player.isDead()) {
         player.setIsDead(false);
      }

      player.setCurrentCp(player.getMaxCp());
      player.setCurrentHp(player.getMaxHp());
      player.setCurrentMp(player.getMaxMp());
      player.getStatus().startHpMpRegeneration();
   }

   protected static final void cleanEffects(Player player) {
      try {
         player.setIsOlympiadStart(false);
         player.setTarget(null);
         player.abortAttack();
         player.abortCast();
         player.getAI().setIntention(CtrlIntention.IDLE);
         player.stopAllEffectsExceptThoseThatLastThroughDeath();
         player.clearSouls();
         player.clearCharges();
         if (player.getAgathionId() > 0) {
            player.setAgathionId(0);
         }

         Summon summon = player.getSummon();
         if (summon != null && !summon.isDead()) {
            summon.setTarget(null);
            summon.abortAttack();
            summon.abortCast();
            summon.stopAllEffectsExceptThoseThatLastThroughDeath();
         }

         player.setCurrentCp(player.getMaxCp());
         player.setCurrentHp(player.getMaxHp());
         player.setCurrentMp(player.getMaxMp());
         player.getStatus().startHpMpRegeneration();
      } catch (Exception var2) {
         _log.log(Level.WARNING, var2.getMessage(), (Throwable)var2);
      }
   }

   protected static final void playerStatusBack(Player player) {
      try {
         if (player.isTransformed()) {
            player.untransform();
         }

         player.setIsInOlympiadMode(false);
         player.setIsOlympiadStart(false);
         player.setOlympiadSide(-1);
         player.setOlympiadGameId(-1);
         player.setOlympiadGame(null);
         player.sendPacket(new ExOlympiadMode(0));
         if (player.getClan() != null) {
            player.getClan().addSkillEffects(player);
            if (player.getClan().getCastleId() > 0) {
               CastleManager.getInstance().getCastleByOwner(player.getClan()).giveResidentialSkills(player);
            }

            if (player.getClan().getFortId() > 0) {
               FortManager.getInstance().getFortByOwner(player.getClan()).giveResidentialSkills(player);
            }
         }

         if (player.isHero() && !player.isTempHero()) {
            for(Skill skill : SkillTreesParser.getInstance().getHeroSkillTree().values()) {
               player.addSkill(skill, false);
            }
         }

         player.sendSkillList(false);
         player.setCurrentCp(player.getMaxCp());
         player.setCurrentHp(player.getMaxHp());
         player.setCurrentMp(player.getMaxMp());
         player.getStatus().startHpMpRegeneration();
         if (player.hasSummon()) {
            Summon summon = player.getSummon();
            if (!summon.isDead()) {
               summon.setCurrentHp(summon.getMaxHp());
               summon.setCurrentMp(summon.getMaxMp());
            }
         }

         if (Config.DOUBLE_SESSIONS_CHECK_MAX_OLYMPIAD_PARTICIPANTS > 0) {
            DoubleSessionManager.getInstance().removePlayer(100, player);
         }
      } catch (Exception var3) {
         _log.log(Level.WARNING, "portPlayersToArena()", (Throwable)var3);
      }
   }

   protected static final void portPlayerBack(Player player) {
      if (player != null) {
         if (player.getLastX() != 0 || player.getLastY() != 0) {
            player.setReflectionId(0);
            player.teleToLocation(player.getLastX(), player.getLastY(), player.getLastZ(), false);
            player.setLastCords(0, 0, 0);
         }
      }
   }

   public static final void rewardParticipant(Player player, int[][] reward) {
      if (player != null && player.isOnline() && reward != null) {
         if (Config.ALLOW_DAILY_TASKS && player.getActiveDailyTasks() != null) {
            for(PlayerTaskTemplate taskTemplate : player.getActiveDailyTasks()) {
               if (taskTemplate.getType().equalsIgnoreCase("Olympiad") && !taskTemplate.isComplete()) {
                  DailyTaskTemplate task = DailyTaskManager.getInstance().getDailyTask(taskTemplate.getId());
                  if (taskTemplate.getCurrentOlyMatchCount() < task.getOlyMatchCount()) {
                     taskTemplate.setCurrentOlyMatchCount(taskTemplate.getCurrentOlyMatchCount() + 1);
                     if (taskTemplate.isComplete()) {
                        IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getHandler("missions");
                        if (vch != null) {
                           player.updateDailyStatus(taskTemplate);
                           vch.useVoicedCommand("missions", player, null);
                        }
                     }
                  }
               }
            }
         }

         try {
            InventoryUpdate iu = new InventoryUpdate();

            for(int[] it : reward) {
               if (it != null && it.length == 2) {
                  ItemInstance item = player.getInventory().addItem("Olympiad", it[0], (long)it[1], player, null);
                  if (item != null) {
                     iu.addModifiedItem(item);
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
                     sm.addItemName(it[0]);
                     sm.addNumber(it[1]);
                     player.sendPacket(sm);
                  }
               }
            }

            player.sendPacket(iu);
         } catch (Exception var9) {
            _log.log(Level.WARNING, var9.getMessage(), (Throwable)var9);
         }
      }
   }

   public void setZone(OlympiadStadiumZone zone) {
      this._zone = zone;
   }

   public void checkWinner() {
      if (this.haveWinner() && !this._hasEnded) {
         this._hasEnded = true;
         this._zone.updateZoneStatusForCharactersInside(true);
         this.cleanEffects();
         this.validateWinner(this._zone);
         this.restorePlayers();
      }
   }

   public abstract CompetitionType getType();

   public abstract String[] getPlayerNames();

   public abstract boolean containsParticipant(int var1);

   public abstract boolean containsHwidParticipant(Player var1);

   public abstract void sendOlympiadInfo(Creature var1);

   public abstract void broadcastOlympiadInfo(OlympiadStadiumZone var1);

   protected abstract void broadcastPacket(GameServerPacket var1);

   protected abstract boolean needBuffers();

   protected abstract boolean checkDefaulted();

   protected abstract void removals();

   protected abstract boolean portPlayersToArena(List<Location> var1);

   protected abstract void confirmDlgInvite();

   protected abstract void cleanEffects();

   protected abstract void restorePlayers();

   protected abstract void portPlayersBack();

   protected abstract void playersStatusBack();

   protected abstract void clearPlayers();

   protected abstract void healPlayers();

   protected abstract void handleDisconnect(Player var1);

   protected abstract void resetDamage();

   protected abstract void addDamage(Player var1, int var2);

   protected abstract boolean checkBattleStatus();

   protected abstract boolean haveWinner();

   protected abstract void validateWinner(OlympiadStadiumZone var1);

   protected abstract int getDivider();

   protected abstract int[][] getReward();

   protected abstract String getWeeklyMatchType();
}
