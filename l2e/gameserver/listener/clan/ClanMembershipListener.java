package l2e.gameserver.listener.clan;

import l2e.gameserver.listener.AbstractListener;
import l2e.gameserver.listener.events.ClanJoinEvent;
import l2e.gameserver.listener.events.ClanLeaderChangeEvent;
import l2e.gameserver.listener.events.ClanLeaveEvent;
import l2e.gameserver.model.Clan;

public abstract class ClanMembershipListener extends AbstractListener {
   public ClanMembershipListener() {
      this.register();
   }

   public abstract boolean onJoin(ClanJoinEvent var1);

   public abstract boolean onLeave(ClanLeaveEvent var1);

   public abstract boolean onLeaderChange(ClanLeaderChangeEvent var1);

   @Override
   public void register() {
      Clan.addClanMembershipListener(this);
   }

   @Override
   public void unregister() {
      Clan.removeClanMembershipListener(this);
   }
}
