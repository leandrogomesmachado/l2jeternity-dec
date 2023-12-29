package l2e.gameserver.model.actor.templates.promocode.impl;

import l2e.gameserver.model.actor.Player;

public abstract class AbstractCodeReward {
   public abstract void giveReward(Player var1);

   public abstract String getIcon();
}
