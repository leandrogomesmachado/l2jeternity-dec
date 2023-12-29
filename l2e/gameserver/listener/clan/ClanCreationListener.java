package l2e.gameserver.listener.clan;

import l2e.gameserver.listener.AbstractListener;
import l2e.gameserver.listener.events.ClanCreationEvent;
import l2e.gameserver.listener.events.ClanLevelUpEvent;
import l2e.gameserver.model.Clan;

public abstract class ClanCreationListener extends AbstractListener {
   public ClanCreationListener() {
      this.register();
   }

   public abstract void onClanCreate(ClanCreationEvent var1);

   public abstract boolean onClanLevelUp(ClanLevelUpEvent var1);

   @Override
   public void register() {
      Clan.addClanCreationListener(this);
   }

   @Override
   public void unregister() {
      Clan.removeClanCreationListener(this);
   }
}
