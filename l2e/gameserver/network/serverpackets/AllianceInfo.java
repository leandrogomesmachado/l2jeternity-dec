package l2e.gameserver.network.serverpackets;

import java.util.Collection;
import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.actor.templates.ClanInfoTemplate;

public class AllianceInfo extends GameServerPacket {
   private final String _name;
   private final int _total;
   private final int _online;
   private final String _leaderC;
   private final String _leaderP;
   private final ClanInfoTemplate[] _allies;

   public AllianceInfo(int allianceId) {
      Clan leader = ClanHolder.getInstance().getClan(allianceId);
      this._name = leader.getAllyName();
      this._leaderC = leader.getName();
      this._leaderP = leader.getLeaderName();
      Collection<Clan> allies = ClanHolder.getInstance().getClanAllies(allianceId);
      this._allies = new ClanInfoTemplate[allies.size()];
      int idx = 0;
      int total = 0;
      int online = 0;

      for(Clan clan : allies) {
         ClanInfoTemplate ci = new ClanInfoTemplate(clan);
         this._allies[idx++] = ci;
         total += ci.getTotal();
         online += ci.getOnline();
      }

      this._total = total;
      this._online = online;
   }

   @Override
   protected void writeImpl() {
      this.writeS(this._name);
      this.writeD(this._total);
      this.writeD(this._online);
      this.writeS(this._leaderC);
      this.writeS(this._leaderP);
      this.writeD(this._allies.length);

      for(ClanInfoTemplate aci : this._allies) {
         this.writeS(aci.getClan().getName());
         this.writeD(0);
         this.writeD(aci.getClan().getLevel());
         this.writeS(aci.getClan().getLeaderName());
         this.writeD(aci.getTotal());
         this.writeD(aci.getOnline());
      }
   }

   public String getName() {
      return this._name;
   }

   public int getTotal() {
      return this._total;
   }

   public int getOnline() {
      return this._online;
   }

   public String getLeaderC() {
      return this._leaderC;
   }

   public String getLeaderP() {
      return this._leaderP;
   }

   public ClanInfoTemplate[] getAllies() {
      return this._allies;
   }
}
