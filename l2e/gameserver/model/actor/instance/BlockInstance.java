package l2e.gameserver.model.actor.instance;

import l2e.commons.util.Rnd;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.model.ArenaParticipantsHolder;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.entity.BlockCheckerEngine;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.serverpackets.ExBlockUpSetState;
import l2e.gameserver.network.serverpackets.NpcInfo;

public class BlockInstance extends MonsterInstance {
   private int _colorEffect;

   public BlockInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
   }

   public void changeColor(Player attacker, ArenaParticipantsHolder holder, int team) {
      synchronized(this) {
         BlockCheckerEngine event = holder.getEvent();
         if (this._colorEffect == 83) {
            this._colorEffect = 0;
            this.broadcastPacket(new NpcInfo.Info(this, attacker));
            this.increaseTeamPointsAndSend(attacker, team, event);
         } else {
            this._colorEffect = 83;
            this.broadcastPacket(new NpcInfo.Info(this, attacker));
            this.increaseTeamPointsAndSend(attacker, team, event);
         }

         int random = Rnd.get(100);
         if (random > 69 && random <= 84) {
            this.dropItem(13787, event, attacker);
         } else if (random > 84) {
            this.dropItem(13788, event, attacker);
         }
      }
   }

   public void setRed(boolean isRed) {
      this._colorEffect = isRed ? 83 : 0;
   }

   @Override
   public int getColorEffect() {
      return this._colorEffect;
   }

   @Override
   public boolean isAutoAttackable(Creature attacker) {
      if (!attacker.isPlayer()) {
         return true;
      } else {
         return attacker.getActingPlayer() != null && attacker.getActingPlayer().getBlockCheckerArena() > -1;
      }
   }

   @Override
   protected void onDeath(Creature killer) {
   }

   @Override
   public void onAction(Player player, boolean interact) {
      if (this.canTarget(player)) {
         player.setLastFolkNPC(this);
         if (player.getTarget() != this) {
            player.setTarget(this);
            this.getAI();
         } else if (interact) {
            player.sendActionFailed();
         }
      }
   }

   private void increaseTeamPointsAndSend(Player player, int team, BlockCheckerEngine eng) {
      eng.increasePlayerPoints(player, team);
      int timeLeft = (int)((eng.getStarterTime() - System.currentTimeMillis()) / 1000L);
      boolean isRed = eng.getHolder().getRedPlayers().contains(player);
      ExBlockUpSetState changePoints = new ExBlockUpSetState(timeLeft, eng.getBluePoints(), eng.getRedPoints());
      ExBlockUpSetState secretPoints = new ExBlockUpSetState(
         timeLeft, eng.getBluePoints(), eng.getRedPoints(), isRed, player, eng.getPlayerPoints(player, isRed)
      );
      eng.getHolder().broadCastPacketToTeam(changePoints);
      eng.getHolder().broadCastPacketToTeam(secretPoints);
   }

   private void dropItem(int id, BlockCheckerEngine eng, Player player) {
      ItemInstance drop = ItemsParser.getInstance().createItem("Loot", id, 1L, player, this);
      int x = this.getX() + Rnd.get(50);
      int y = this.getY() + Rnd.get(50);
      int z = this.getZ();
      drop.dropMe(this, x, y, z);
      eng.addNewDrop(drop);
   }
}
