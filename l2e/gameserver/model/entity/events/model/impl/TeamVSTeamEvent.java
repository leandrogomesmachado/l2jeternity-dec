package l2e.gameserver.model.entity.events.model.impl;

import l2e.commons.collections.MultiValueSet;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.events.AbstractFightEvent;
import l2e.gameserver.model.entity.events.model.template.FightEventPlayer;
import l2e.gameserver.model.strings.server.ServerMessage;

public class TeamVSTeamEvent extends AbstractFightEvent {
   public TeamVSTeamEvent(MultiValueSet<String> set) {
      super(set);
   }

   @Override
   public void onKilled(Creature actor, Creature victim) {
      if (actor != null && actor.isPlayer()) {
         FightEventPlayer realActor = this.getFightEventPlayer(actor.getActingPlayer());
         if (victim.isPlayer() && realActor != null) {
            realActor.increaseKills();
            realActor.getTeam().incScore(1);
            this.updatePlayerScore(realActor);
            this.updateScreenScores();
            ServerMessage msg = new ServerMessage("FightEvents.YOU_HAVE_KILL", realActor.getPlayer().getLang());
            msg.add(victim.getName());
            this.sendMessageToPlayer(realActor.getPlayer(), AbstractFightEvent.MESSAGE_TYPES.GM, msg);
            actor.getActingPlayer().sendUserInfo();
         }
      }

      if (victim.isPlayer()) {
         FightEventPlayer realVictim = this.getFightEventPlayer(victim);
         if (realVictim != null) {
            realVictim.increaseDeaths();
            if (actor != null) {
               ServerMessage msg = new ServerMessage("FightEvents.YOU_KILLED", realVictim.getPlayer().getLang());
               msg.add(actor.getName());
               this.sendMessageToPlayer(realVictim.getPlayer(), AbstractFightEvent.MESSAGE_TYPES.GM, msg);
            }

            victim.getActingPlayer().broadcastCharInfo();
         }
      }

      super.onKilled(actor, victim);
   }

   @Override
   public String getVisibleTitle(Player player, Player viewer, String currentTitle, boolean toMe) {
      FightEventPlayer fPlayer = this.getFightEventPlayer(player);
      if (fPlayer == null) {
         return currentTitle;
      } else {
         ServerMessage msg = new ServerMessage("FightEvents.TITLE_INFO", viewer.getLang());
         msg.add(fPlayer.getKills());
         msg.add(fPlayer.getDeaths());
         return msg.toString();
      }
   }
}
