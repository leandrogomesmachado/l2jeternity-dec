package l2e.gameserver.model.actor.instance;

import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.network.serverpackets.GameServerPacket;
import l2e.gameserver.network.serverpackets.NpcInfo;

public final class EventChestInstance extends EventMonsterInstance {
   private boolean _isTriggered = false;

   public EventChestInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
      this.setIsNoRndWalk(true);
      this.disableCoreAI(true);
      this.setInvisible(true);
      this.eventSetDropOnGround(true);
      this.eventSetBlockOffensiveSkills(true);
   }

   public boolean canSee(Creature cha) {
      return !this.isInvisible();
   }

   public void trigger(Creature cha) {
      this._isTriggered = true;
      this.setInvisible(false);
      if (!this._isTriggered) {
         this.broadcastPacket(new NpcInfo.Info(this, cha));
      }
   }

   @Override
   public void sendInfo(Player activeChar) {
      if (this._isTriggered || this.canSee(activeChar)) {
         activeChar.sendPacket(new NpcInfo.Info(this, activeChar));
      }
   }

   @Override
   public void broadcastPacket(GameServerPacket mov) {
      mov.setInvisible(this.isInvisible());

      for(Player player : World.getInstance().getAroundPlayers(this)) {
         if (player != null && (this._isTriggered || this.canSee(player))) {
            player.sendPacket(mov);
         }
      }
   }

   @Override
   public void broadcastPacket(GameServerPacket mov, int radiusInKnownlist) {
      mov.setInvisible(this.isInvisible());

      for(Player player : World.getInstance().getAroundPlayers(this)) {
         if (player != null && this.isInsideRadius(player, radiusInKnownlist, false, false) && (this._isTriggered || this.canSee(player))) {
            player.sendPacket(mov);
         }
      }
   }

   @Override
   public boolean isAutoAttackable(Creature attacker) {
      return !this.canSee(attacker);
   }

   @Override
   public void reduceHate(Creature target, int amount) {
   }

   @Override
   public boolean canBeAttacked() {
      return false;
   }
}
