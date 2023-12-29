package l2e.gameserver.model.actor.templates;

import l2e.gameserver.model.Clan;

public class ClanInfoTemplate {
   private final Clan _clan;
   private final int _total;
   private final int _online;

   public ClanInfoTemplate(Clan clan) {
      this._clan = clan;
      this._total = clan.getMembersCount();
      this._online = clan.getOnlineMembersCount();
   }

   public Clan getClan() {
      return this._clan;
   }

   public int getTotal() {
      return this._total;
   }

   public int getOnline() {
      return this._online;
   }
}
