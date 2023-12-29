package l2e.gameserver.model.entity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.ReflectionParser;
import l2e.gameserver.instancemanager.DuelManager;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.DoorInstance;
import l2e.gameserver.model.actor.templates.reflection.ReflectionTemplate;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ActionFail;
import l2e.gameserver.network.serverpackets.ExDuelEnd;
import l2e.gameserver.network.serverpackets.ExDuelReady;
import l2e.gameserver.network.serverpackets.ExDuelStart;
import l2e.gameserver.network.serverpackets.ExDuelUpdateUserInfo;
import l2e.gameserver.network.serverpackets.GameServerPacket;
import l2e.gameserver.network.serverpackets.PlaySound;
import l2e.gameserver.network.serverpackets.SocialAction;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class Duel {
   protected static final Logger _log = Logger.getLogger(Duel.class.getName());
   public static final int DUELSTATE_NODUEL = 0;
   public static final int DUELSTATE_DUELLING = 1;
   public static final int DUELSTATE_DEAD = 2;
   public static final int DUELSTATE_WINNER = 3;
   public static final int DUELSTATE_INTERRUPTED = 4;
   public static final int DUELSTATE_PREPARING = 5;
   private final int _duelId;
   private Player _playerA;
   private Player _playerB;
   private final boolean _partyDuel;
   private final Calendar _duelEndTime;
   private int _surrenderRequest = 0;
   private int _countdown = 4;
   private boolean _finished = false;
   private int _duelInstanceId;
   private List<Duel.PlayerCondition> _playerConditions = new CopyOnWriteArrayList<>();

   public Duel(Player playerA, Player playerB, int partyDuel, int duelId) {
      this._duelId = duelId;
      this._playerA = playerA;
      this._playerB = playerB;
      this._partyDuel = partyDuel == 1;
      this.prepareStatus(this._playerA, this._playerB, this._partyDuel);
      this._duelEndTime = Calendar.getInstance();
      if (this._partyDuel) {
         this._duelEndTime.add(13, 300);
      } else {
         this._duelEndTime.add(13, 120);
      }

      this.setFinished(false);
      if (this._partyDuel) {
         ++this._countdown;
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.IN_A_MOMENT_YOU_WILL_BE_TRANSPORTED_TO_THE_SITE_WHERE_THE_DUEL_WILL_TAKE_PLACE);
         this.broadcastToTeam1(sm);
         this.broadcastToTeam2(sm);
      }

      ThreadPoolManager.getInstance().schedule(new Duel.ScheduleStartDuelTask(this), 3000L);
   }

   private void stopFighting() {
      ActionFail af = ActionFail.STATIC_PACKET;
      if (this._partyDuel) {
         for(Player temp : this._playerA.getParty().getMembers()) {
            temp.abortCast();
            temp.getAI().setIntention(CtrlIntention.ACTIVE);
            temp.setTarget(null);
            temp.sendPacket(af);
         }

         for(Player temp : this._playerB.getParty().getMembers()) {
            temp.abortCast();
            temp.getAI().setIntention(CtrlIntention.ACTIVE);
            temp.setTarget(null);
            temp.sendPacket(af);
         }
      } else {
         this._playerA.abortCast();
         this._playerB.abortCast();
         this._playerA.getAI().setIntention(CtrlIntention.ACTIVE);
         this._playerA.setTarget(null);
         this._playerB.getAI().setIntention(CtrlIntention.ACTIVE);
         this._playerB.setTarget(null);
         this._playerA.sendPacket(af);
         this._playerB.sendPacket(af);
      }
   }

   public boolean isDuelistInPvp(boolean sendMessage) {
      if (this._partyDuel) {
         return false;
      } else if (this._playerA.getPvpFlag() == 0 && this._playerB.getPvpFlag() == 0) {
         return false;
      } else {
         if (sendMessage) {
            String engagedInPvP = "The duel was canceled because a duelist engaged in PvP combat.";
            this._playerA.sendMessage("The duel was canceled because a duelist engaged in PvP combat.");
            this._playerB.sendMessage("The duel was canceled because a duelist engaged in PvP combat.");
         }

         return true;
      }
   }

   public void startDuel() {
      this.savePlayerConditions();
      if (this._playerA != null && this._playerB != null && !this._playerA.isInDuel() && !this._playerB.isInDuel()) {
         if (this._partyDuel) {
            for(Player temp : this._playerA.getParty().getMembers()) {
               temp.cancelActiveTrade();
               temp.setIsInDuel(this._duelId);
               temp.setTeam(1);
               temp.broadcastUserInfo(true);
               this.broadcastToTeam2(new ExDuelUpdateUserInfo(temp));
            }

            for(Player temp : this._playerB.getParty().getMembers()) {
               temp.cancelActiveTrade();
               temp.setIsInDuel(this._duelId);
               temp.setTeam(2);
               temp.broadcastUserInfo(true);
               this.broadcastToTeam1(new ExDuelUpdateUserInfo(temp));
            }

            if (this._partyDuel) {
               for(DoorInstance door : ReflectionManager.getInstance().getReflection(this.getDueldInstanceId()).getDoors()) {
                  if (door != null && door.isOpened()) {
                     door.closeMe();
                  }
               }
            }

            ExDuelReady ready = new ExDuelReady(1);
            ExDuelStart start = new ExDuelStart(1);
            this.broadcastToTeam1(ready);
            this.broadcastToTeam2(ready);
            this.broadcastToTeam1(start);
            this.broadcastToTeam2(start);
         } else {
            this._playerA.setIsInDuel(this._duelId);
            this._playerA.setTeam(1);
            this._playerB.setIsInDuel(this._duelId);
            this._playerB.setTeam(2);
            ExDuelReady ready = new ExDuelReady(0);
            ExDuelStart start = new ExDuelStart(0);
            this.broadcastToTeam1(ready);
            this.broadcastToTeam2(ready);
            this.broadcastToTeam1(start);
            this.broadcastToTeam2(start);
            this.broadcastToTeam1(new ExDuelUpdateUserInfo(this._playerB));
            this.broadcastToTeam2(new ExDuelUpdateUserInfo(this._playerA));
            this._playerA.broadcastUserInfo(true);
            this._playerB.broadcastUserInfo(true);
         }

         PlaySound ps = new PlaySound(1, "B04_S01", 0, 0, 0, 0, 0);
         this.broadcastToTeam1(ps);
         this.broadcastToTeam2(ps);
         ThreadPoolManager.getInstance().schedule(new Duel.ScheduleDuelTask(this), 1000L);
      } else {
         this._playerConditions.clear();
         this._playerConditions = null;
         DuelManager.getInstance().removeDuel(this);
      }
   }

   public void savePlayerConditions() {
      if (this._partyDuel) {
         for(Player temp : this._playerA.getParty().getMembers()) {
            this._playerConditions.add(new Duel.PlayerCondition(temp, this._partyDuel));
         }

         for(Player temp : this._playerB.getParty().getMembers()) {
            this._playerConditions.add(new Duel.PlayerCondition(temp, this._partyDuel));
         }
      } else {
         this._playerConditions.add(new Duel.PlayerCondition(this._playerA, this._partyDuel));
         this._playerConditions.add(new Duel.PlayerCondition(this._playerB, this._partyDuel));
      }
   }

   public void restorePlayerConditions(boolean abnormalDuelEnd) {
      if (this._partyDuel) {
         for(Player temp : this._playerA.getParty().getMembers()) {
            temp.setIsInDuel(0);
            temp.setTeam(0);
            temp.broadcastUserInfo(true);
         }

         for(Player temp : this._playerB.getParty().getMembers()) {
            temp.setIsInDuel(0);
            temp.setTeam(0);
            temp.broadcastUserInfo(true);
         }
      } else {
         this._playerA.setIsInDuel(0);
         this._playerA.setTeam(0);
         this._playerA.broadcastUserInfo(true);
         this._playerB.setIsInDuel(0);
         this._playerB.setTeam(0);
         this._playerB.broadcastUserInfo(true);
      }

      if (!abnormalDuelEnd) {
         for(Duel.PlayerCondition e : this._playerConditions) {
            e.restoreCondition();
         }
      }
   }

   public int getId() {
      return this._duelId;
   }

   public int getRemainingTime() {
      return (int)(this._duelEndTime.getTimeInMillis() - Calendar.getInstance().getTimeInMillis());
   }

   public Player getPlayerA() {
      return this._playerA;
   }

   public Player getPlayerB() {
      return this._playerB;
   }

   public boolean isPartyDuel() {
      return this._partyDuel;
   }

   public int getDueldInstanceId() {
      return this._duelInstanceId;
   }

   public void setFinished(boolean mode) {
      this._finished = mode;
   }

   public boolean getFinished() {
      return this._finished;
   }

   public void teleportPlayers() {
      if (this._partyDuel) {
         ReflectionTemplate template = ReflectionParser.getInstance().getReflectionId(1);
         if (template != null) {
            this._duelInstanceId = ReflectionManager.getInstance().createDynamicReflection(template).getId();
            Reflection ref = ReflectionManager.getInstance().getReflection(this._duelInstanceId);
            if (ref != null) {
               int i = 0;

               for(Player player : this._playerA.getParty().getMembers()) {
                  player.setReflectionId(this._duelInstanceId);
                  Location loc = template.getTeleportCoords().get(i++);
                  player.teleToLocation(loc.getX(), loc.getY(), loc.getZ(), 0, 0, true, true);
               }

               i = 9;

               for(Player player : this._playerB.getParty().getMembers()) {
                  player.setReflectionId(this._duelInstanceId);
                  Location loc = template.getTeleportCoords().get(i++);
                  player.teleToLocation(loc.getX(), loc.getY(), loc.getZ(), 0, 0, true, true);
               }
            }
         }
      }
   }

   public void broadcastToTeam1(GameServerPacket packet) {
      if (this._playerA != null) {
         if (this._partyDuel && this._playerA.getParty() != null) {
            for(Player temp : this._playerA.getParty().getMembers()) {
               temp.sendPacket(packet);
            }
         } else {
            this._playerA.sendPacket(packet);
         }
      }
   }

   public void broadcastToTeam2(GameServerPacket packet) {
      if (this._playerB != null) {
         if (this._partyDuel && this._playerB.getParty() != null) {
            for(Player temp : this._playerB.getParty().getMembers()) {
               temp.sendPacket(packet);
            }
         } else {
            this._playerB.sendPacket(packet);
         }
      }
   }

   public Player getWinner() {
      if (this.getFinished() && this._playerA != null && this._playerB != null) {
         if (this._playerA.getDuelState() == 3) {
            return this._playerA;
         } else {
            return this._playerB.getDuelState() == 3 ? this._playerB : null;
         }
      } else {
         return null;
      }
   }

   public Player getLooser() {
      if (this.getFinished() && this._playerA != null && this._playerB != null) {
         if (this._playerA.getDuelState() == 3) {
            return this._playerB;
         } else {
            return this._playerB.getDuelState() == 3 ? this._playerA : null;
         }
      } else {
         return null;
      }
   }

   public void playKneelAnimation() {
      Player looser = this.getLooser();
      if (looser != null) {
         if (this._partyDuel && looser.getParty() != null) {
            for(Player temp : looser.getParty().getMembers()) {
               temp.broadcastPacket(new SocialAction(temp.getObjectId(), 7));
            }
         } else {
            looser.broadcastPacket(new SocialAction(looser.getObjectId(), 7));
         }
      }
   }

   public int countdown() {
      --this._countdown;
      if (this._countdown > 3) {
         return this._countdown;
      } else {
         SystemMessage sm = null;
         if (this._countdown > 0) {
            sm = SystemMessage.getSystemMessage(SystemMessageId.THE_DUEL_WILL_BEGIN_IN_S1_SECONDS);
            sm.addNumber(this._countdown);
         } else {
            sm = SystemMessage.getSystemMessage(SystemMessageId.LET_THE_DUEL_BEGIN);
         }

         this.broadcastToTeam1(sm);
         this.broadcastToTeam2(sm);
         return this._countdown;
      }
   }

   public void endDuel(Duel.DuelResultEnum result) {
      if (this._playerA != null && this._playerB != null) {
         SystemMessage sm = null;
         switch(result) {
            case Team1Win:
            case Team2Surrender:
               this.restorePlayerConditions(false);
               if (this._partyDuel) {
                  sm = SystemMessage.getSystemMessage(SystemMessageId.C1_PARTY_HAS_WON_THE_DUEL);
               } else {
                  sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_WON_THE_DUEL);
               }

               sm.addString(this._playerA.getName());
               this._playerA.getCounters().addAchivementInfo("duelsWon", 0, -1L, false, this._partyDuel, false);
               this.broadcastToTeam1(sm);
               this.broadcastToTeam2(sm);
               break;
            case Team1Surrender:
            case Team2Win:
               this.restorePlayerConditions(false);
               if (this._partyDuel) {
                  sm = SystemMessage.getSystemMessage(SystemMessageId.C1_PARTY_HAS_WON_THE_DUEL);
               } else {
                  sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_WON_THE_DUEL);
               }

               sm.addString(this._playerB.getName());
               this._playerB.getCounters().addAchivementInfo("duelsWon", 0, -1L, false, this._partyDuel, false);
               this.broadcastToTeam1(sm);
               this.broadcastToTeam2(sm);
               break;
            case Canceled:
               this.stopFighting();
               this.restorePlayerConditions(true);
               sm = SystemMessage.getSystemMessage(SystemMessageId.THE_DUEL_HAS_ENDED_IN_A_TIE);
               this.broadcastToTeam1(sm);
               this.broadcastToTeam2(sm);
               break;
            case Timeout:
               this.stopFighting();
               this.restorePlayerConditions(false);
               sm = SystemMessage.getSystemMessage(SystemMessageId.THE_DUEL_HAS_ENDED_IN_A_TIE);
               this.broadcastToTeam1(sm);
               this.broadcastToTeam2(sm);
         }

         ExDuelEnd duelEnd = null;
         if (this._partyDuel) {
            duelEnd = new ExDuelEnd(1);
         } else {
            duelEnd = new ExDuelEnd(0);
         }

         this.broadcastToTeam1(duelEnd);
         this.broadcastToTeam2(duelEnd);
         this._playerConditions.clear();
         this._playerConditions = null;
         if (this._partyDuel) {
            ReflectionManager.getInstance().destroyReflection(this.getDueldInstanceId());
         }

         DuelManager.getInstance().removeDuel(this);
      } else {
         this._playerConditions.clear();
         this._playerConditions = null;
         DuelManager.getInstance().removeDuel(this);
      }
   }

   public Duel.DuelResultEnum checkEndDuelCondition() {
      if (this._playerA == null || this._playerB == null) {
         return Duel.DuelResultEnum.Canceled;
      } else if (this._surrenderRequest != 0) {
         return this._surrenderRequest == 1 ? Duel.DuelResultEnum.Team1Surrender : Duel.DuelResultEnum.Team2Surrender;
      } else if (this.getRemainingTime() <= 0) {
         return Duel.DuelResultEnum.Timeout;
      } else if (this._playerA.getDuelState() == 3) {
         this.stopFighting();
         return Duel.DuelResultEnum.Team1Win;
      } else if (this._playerB.getDuelState() == 3) {
         this.stopFighting();
         return Duel.DuelResultEnum.Team2Win;
      } else {
         if (!this._partyDuel) {
            if (this._playerA.getDuelState() == 4 || this._playerB.getDuelState() == 4) {
               return Duel.DuelResultEnum.Canceled;
            }

            if (!this._playerA.isInsideRadius(this._playerB, 1600, false, false)) {
               return Duel.DuelResultEnum.Canceled;
            }

            if (this.isDuelistInPvp(true)) {
               return Duel.DuelResultEnum.Canceled;
            }

            if (this._playerA.isInsideZone(ZoneId.PEACE)
               || this._playerB.isInsideZone(ZoneId.PEACE)
               || this._playerA.isInsideZone(ZoneId.SIEGE)
               || this._playerB.isInsideZone(ZoneId.SIEGE)
               || this._playerA.isInsideZone(ZoneId.PVP)
               || this._playerB.isInsideZone(ZoneId.PVP)) {
               return Duel.DuelResultEnum.Canceled;
            }
         }

         return Duel.DuelResultEnum.Continue;
      }
   }

   public void doSurrender(Player player) {
      if (this._surrenderRequest == 0) {
         this.stopFighting();
         if (this._partyDuel) {
            if (this._playerA.getParty().getMembers().contains(player)) {
               this._surrenderRequest = 1;

               for(Player temp : this._playerA.getParty().getMembers()) {
                  temp.setDuelState(2);
               }

               for(Player temp : this._playerB.getParty().getMembers()) {
                  temp.setDuelState(3);
               }
            } else if (this._playerB.getParty().getMembers().contains(player)) {
               this._surrenderRequest = 2;

               for(Player temp : this._playerB.getParty().getMembers()) {
                  temp.setDuelState(2);
               }

               for(Player temp : this._playerA.getParty().getMembers()) {
                  temp.setDuelState(3);
               }
            }
         } else if (player == this._playerA) {
            this._surrenderRequest = 1;
            this._playerA.setDuelState(2);
            this._playerB.setDuelState(3);
         } else if (player == this._playerB) {
            this._surrenderRequest = 2;
            this._playerB.setDuelState(2);
            this._playerA.setDuelState(3);
         }
      }
   }

   public void onPlayerDefeat(Player player) {
      player.setDuelState(2);
      if (this._partyDuel) {
         boolean teamdefeated = true;

         for(Player temp : player.getParty().getMembers()) {
            if (temp.getDuelState() == 1) {
               teamdefeated = false;
               break;
            }
         }

         if (teamdefeated) {
            Player winner = this._playerA;
            if (this._playerA.getParty().getMembers().contains(player)) {
               winner = this._playerB;
            }

            for(Player temp : winner.getParty().getMembers()) {
               temp.setDuelState(3);
            }
         }
      } else {
         if (player != this._playerA && player != this._playerB) {
            _log.warning("Error in onPlayerDefeat(): player is not part of this 1vs1 duel");
         }

         if (this._playerA == player) {
            this._playerB.setDuelState(3);
         } else {
            this._playerA.setDuelState(3);
         }
      }
   }

   public void onRemoveFromParty(Player player) {
      if (this._partyDuel) {
         if (player != this._playerA && player != this._playerB) {
            for(Duel.PlayerCondition e : this._playerConditions) {
               if (e.getPlayer() == player) {
                  e.teleportBack();
                  this._playerConditions.remove(e);
                  break;
               }
            }

            player.setIsInDuel(0);
         } else {
            for(Duel.PlayerCondition e : this._playerConditions) {
               e.teleportBack();
               e.getPlayer().setIsInDuel(0);
            }

            this._playerA = null;
            this._playerB = null;
         }
      }
   }

   private void prepareStatus(Player playerA, Player playerB, boolean isPartyDuel) {
      if (playerA != null && playerB != null) {
         if (isPartyDuel) {
            for(Player temp : playerA.getParty().getMembers()) {
               temp.setDuelState(5);
            }

            for(Player temp : playerB.getParty().getMembers()) {
               temp.setDuelState(5);
            }
         } else {
            playerA.setDuelState(5);
            playerB.setDuelState(5);
         }
      }
   }

   public static enum DuelResultEnum {
      Continue,
      Team1Win,
      Team2Win,
      Team1Surrender,
      Team2Surrender,
      Canceled,
      Timeout;
   }

   public static class PlayerCondition {
      private Player _player;
      private double _hp;
      private double _mp;
      private double _cp;
      private boolean _paDuel;
      private int _x;
      private int _y;
      private int _z;
      private List<Effect> _effects;

      public PlayerCondition(Player player, boolean partyDuel) {
         if (player != null) {
            this._player = player;
            this._hp = this._player.getCurrentHp();
            this._mp = this._player.getCurrentMp();
            this._cp = this._player.getCurrentCp();
            this._paDuel = partyDuel;
            Effect[] effectList = player.getAllEffects();
            this._effects = new ArrayList<>(effectList.length);

            for(Effect effect : effectList) {
               Effect ef = effect.getEffectTemplate().getEffect(new Env(effect.getEffector(), effect.getEffected(), effect.getSkill()));
               ef.setCount(effect.getTickCount());
               ef.setAbnormalTime(effect.getAbnormalTime());
               ef.setFirstTime(effect.getTime());
               this._effects.add(ef);
            }

            if (this._paDuel) {
               this._x = this._player.getX();
               this._y = this._player.getY();
               this._z = this._player.getZ();
            }
         }
      }

      public void restoreCondition() {
         if (this._player != null) {
            this._player.getEffectList().stopAllEffects();
            if (this._effects != null && !this._effects.isEmpty()) {
               for(Effect e : this._effects) {
                  e.scheduleEffect(true);
               }
            }

            this._player.setCurrentHp(this._hp);
            this._player.setCurrentMp(this._mp);
            this._player.setCurrentCp(this._cp);
            if (this._paDuel) {
               this.teleportBack();
            }
         }
      }

      public void teleportBack() {
         if (this._paDuel) {
            this._player.setReflectionId(0);
            this._player.teleToLocation(this._x, this._y, this._z, true);
         }
      }

      public Player getPlayer() {
         return this._player;
      }
   }

   public class ScheduleDuelTask implements Runnable {
      private final Duel _duel;

      public ScheduleDuelTask(Duel duel) {
         this._duel = duel;
      }

      @Override
      public void run() {
         try {
            Duel.DuelResultEnum status = this._duel.checkEndDuelCondition();
            if (status == Duel.DuelResultEnum.Canceled) {
               Duel.this.setFinished(true);
               this._duel.endDuel(status);
            } else if (status != Duel.DuelResultEnum.Continue) {
               Duel.this.setFinished(true);
               Duel.this.playKneelAnimation();
               ThreadPoolManager.getInstance().schedule(new Duel.ScheduleEndDuelTask(this._duel, status), 5000L);
            } else {
               ThreadPoolManager.getInstance().schedule(this, 1000L);
            }
         } catch (Exception var2) {
            Duel._log.log(Level.SEVERE, "", (Throwable)var2);
         }
      }
   }

   public static class ScheduleEndDuelTask implements Runnable {
      private final Duel _duel;
      private final Duel.DuelResultEnum _result;

      public ScheduleEndDuelTask(Duel duel, Duel.DuelResultEnum result) {
         this._duel = duel;
         this._result = result;
      }

      @Override
      public void run() {
         try {
            this._duel.endDuel(this._result);
         } catch (Exception var2) {
            Duel._log.log(Level.SEVERE, "", (Throwable)var2);
         }
      }
   }

   public static class ScheduleStartDuelTask implements Runnable {
      private final Duel _duel;

      public ScheduleStartDuelTask(Duel duel) {
         this._duel = duel;
      }

      @Override
      public void run() {
         try {
            int count = this._duel.countdown();
            if (count == 4) {
               this._duel.teleportPlayers();
               ThreadPoolManager.getInstance().schedule(this, 20000L);
            } else if (count > 0) {
               ThreadPoolManager.getInstance().schedule(this, 1000L);
            } else {
               this._duel.startDuel();
            }
         } catch (Exception var2) {
            Duel._log.log(Level.SEVERE, "", (Throwable)var2);
         }
      }
   }
}
