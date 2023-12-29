package l2e.gameserver.model.actor.instance;

import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.instancemanager.CHSiegeManager;
import l2e.gameserver.instancemanager.FortSiegeManager;
import l2e.gameserver.instancemanager.SiegeManager;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.SiegeClan;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.status.SiegeFlagStatus;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.entity.Siegable;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class SiegeFlagInstance extends Npc {
   private Clan _clan;
   private Player _player;
   private Siegable _siege;
   private final boolean _isAdvanced;
   private boolean _canTalk;

   public SiegeFlagInstance(Player player, int objectId, NpcTemplate template, boolean advanced, boolean outPost) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.SiegeFlagInstance);
      if (TerritoryWarManager.getInstance().isTWInProgress()) {
         this._clan = player.getClan();
         this._player = player;
         this._canTalk = false;
         if (this._clan == null) {
            this.deleteMe();
         }

         if (outPost) {
            this._isAdvanced = false;
            this.setIsInvul(true);
         } else {
            this._isAdvanced = advanced;
            this.setIsInvul(false);
         }

         this.getStatus();
      } else {
         this._clan = player.getClan();
         this._player = player;
         this._canTalk = true;
         this._siege = SiegeManager.getInstance().getSiege(this._player.getX(), this._player.getY(), this._player.getZ());
         if (this._siege == null) {
            this._siege = FortSiegeManager.getInstance().getSiege(this._player.getX(), this._player.getY(), this._player.getZ());
         }

         if (this._siege == null) {
            this._siege = CHSiegeManager.getInstance().getSiege(player);
         }

         if (this._clan != null && this._siege != null) {
            SiegeClan sc = this._siege.getAttackerClan(this._clan);
            if (sc == null) {
               throw new NullPointerException(this.getClass().getSimpleName() + ": Cannot find siege clan.");
            } else {
               sc.addFlag(this);
               this._isAdvanced = advanced;
               this.getStatus();
               this.setIsInvul(false);
            }
         } else {
            throw new NullPointerException(this.getClass().getSimpleName() + ": Initialization failed.");
         }
      }
   }

   @Deprecated
   public SiegeFlagInstance(Player player, int objectId, NpcTemplate template) {
      super(objectId, template);
      this._isAdvanced = false;
   }

   @Override
   public boolean isAutoAttackable(Creature attacker) {
      return !this.isInvul();
   }

   @Override
   public boolean canBeAttacked() {
      return !this.isInvul() && !this.isHealBlocked();
   }

   @Override
   protected void onDeath(Creature killer) {
      if (this._siege != null && this._clan != null) {
         SiegeClan sc = this._siege.getAttackerClan(this._clan);
         if (sc != null) {
            sc.removeFlag(this);
         }
      } else if (this._clan != null) {
         TerritoryWarManager.getInstance().removeClanFlag(this._clan);
      }

      super.onDeath(killer);
   }

   @Override
   public void onForcedAttack(Player player) {
      this.onAction(player);
   }

   @Override
   public void onAction(Player player, boolean interact) {
      if (player != null && this.canTarget(player)) {
         if (this != player.getTarget()) {
            player.setTarget(this);
         } else if (interact) {
            if (this.isAutoAttackable(player) && Math.abs(player.getZ() - this.getZ()) < 100) {
               player.getAI().setIntention(CtrlIntention.ATTACK, this);
            } else {
               player.sendActionFailed();
            }
         }
      }
   }

   public boolean isAdvancedHeadquarter() {
      return this._isAdvanced;
   }

   public SiegeFlagStatus getStatus() {
      return (SiegeFlagStatus)super.getStatus();
   }

   @Override
   public void initCharStatus() {
      this.setStatus(new SiegeFlagStatus(this));
   }

   @Override
   public void reduceCurrentHp(double damage, Creature attacker, Skill skill) {
      super.reduceCurrentHp(damage, attacker, skill);
      if (this.canTalk()
         && (
            this.getCastle() != null && this.getCastle().getSiege().getIsInProgress()
               || this.getFort() != null && this.getFort().getSiege().getIsInProgress()
               || this.getConquerableHall() != null && this.getConquerableHall().isInSiege()
         )
         && this._clan != null) {
         this._clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.BASE_UNDER_ATTACK));
         this.setCanTalk(false);
         ThreadPoolManager.getInstance().schedule(new SiegeFlagInstance.ScheduleTalkTask(), 20000L);
      }
   }

   void setCanTalk(boolean val) {
      this._canTalk = val;
   }

   private boolean canTalk() {
      return this._canTalk;
   }

   @Override
   public boolean isHealBlocked() {
      return true;
   }

   private class ScheduleTalkTask implements Runnable {
      public ScheduleTalkTask() {
      }

      @Override
      public void run() {
         SiegeFlagInstance.this.setCanTalk(true);
      }
   }
}
