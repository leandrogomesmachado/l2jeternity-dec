package l2e.gameserver.listener.clan;

import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.listener.AbstractListener;
import l2e.gameserver.listener.events.ClanWarEvent;

public abstract class ClanWarListener extends AbstractListener {
   public ClanWarListener() {
      this.register();
   }

   public abstract boolean onWarStart(ClanWarEvent var1);

   public abstract boolean onWarEnd(ClanWarEvent var1);

   @Override
   public void register() {
      ClanHolder.addClanWarListener(this);
   }

   @Override
   public void unregister() {
      ClanHolder.removeClanWarListener(this);
   }
}
