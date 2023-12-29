package l2e.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.underground_coliseum.UCArena;
import l2e.gameserver.model.entity.underground_coliseum.UCTeam;

public class ExPVPMatchRecord extends GameServerPacket {
   public static final int START = 0;
   public static final int UPDATE = 1;
   public static final int FINISH = 2;
   private final int _type;
   private final int _winnerTeam;
   private final int _blueKills;
   private final int _redKills;
   private final List<ExPVPMatchRecord.Member> _blueList;
   private final List<ExPVPMatchRecord.Member> _redList;

   public ExPVPMatchRecord(int type, int winnerTeam, UCArena arena) {
      this._type = type;
      this._winnerTeam = winnerTeam;
      UCTeam blueTeam = arena.getTeams()[0];
      this._blueKills = blueTeam.getKillCount();
      UCTeam redTeam = arena.getTeams()[1];
      this._redKills = redTeam.getKillCount();
      this._blueList = new ArrayList<>(9);
      if (blueTeam.getParty() != null) {
         for(Player memberObject : blueTeam.getParty().getMembers()) {
            if (memberObject != null) {
               this._blueList.add(new ExPVPMatchRecord.Member(memberObject.getName(), memberObject.getUCKills(), memberObject.getUCDeaths()));
            }
         }
      }

      this._redList = new ArrayList<>(9);
      if (redTeam.getParty() != null) {
         for(Player memberObject : redTeam.getParty().getMembers()) {
            if (memberObject != null) {
               this._redList.add(new ExPVPMatchRecord.Member(memberObject.getName(), memberObject.getUCKills(), memberObject.getUCDeaths()));
            }
         }
      }
   }

   public ExPVPMatchRecord(int type, int winnerTeam) {
      this._type = type;
      this._winnerTeam = winnerTeam;
      this._blueKills = 0;
      this._redKills = 0;
      this._blueList = new ArrayList<>(9);
      this._redList = new ArrayList<>(9);
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._type);
      this.writeD(this._winnerTeam);
      this.writeD(this._winnerTeam == 0 ? 0 : (this._winnerTeam == 1 ? 2 : 1));
      this.writeD(this._blueKills);
      this.writeD(this._redKills);
      this.writeD(this._blueList.size());

      for(ExPVPMatchRecord.Member member : this._blueList) {
         this.writeS(member._name);
         this.writeD(member._kills);
         this.writeD(member._deaths);
      }

      this.writeD(this._redList.size());

      for(ExPVPMatchRecord.Member member : this._redList) {
         this.writeS(member._name);
         this.writeD(member._kills);
         this.writeD(member._deaths);
      }
   }

   public static class Member {
      public String _name;
      public int _kills;
      public int _deaths;

      public Member(String name, int kills, int deaths) {
         this._name = name;
         this._kills = kills;
         this._deaths = deaths;
      }
   }
}
