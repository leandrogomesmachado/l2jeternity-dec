package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.entity.ClanHall;
import l2e.gameserver.model.entity.clanhall.AuctionableHall;

public class AgitDecoInfo extends GameServerPacket {
   private final AuctionableHall _clanHall;
   private ClanHall.ClanHallFunction _function;

   public AgitDecoInfo(AuctionableHall ClanHall) {
      this._clanHall = ClanHall;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._clanHall.getId());
      this._function = this._clanHall.getFunction(3);
      if (this._function != null && this._function.getLvl() != 0) {
         if ((this._clanHall.getGrade() != 0 || this._function.getLvl() >= 220)
            && (this._clanHall.getGrade() != 1 || this._function.getLvl() >= 160)
            && (this._clanHall.getGrade() != 2 || this._function.getLvl() >= 260)
            && (this._clanHall.getGrade() != 3 || this._function.getLvl() >= 300)) {
            this.writeC(2);
         } else {
            this.writeC(1);
         }
      } else {
         this.writeC(0);
      }

      this._function = this._clanHall.getFunction(4);
      if (this._function != null && this._function.getLvl() != 0) {
         if ((this._clanHall.getGrade() != 0 && this._clanHall.getGrade() != 1 || this._function.getLvl() >= 25)
            && (this._clanHall.getGrade() != 2 || this._function.getLvl() >= 30)
            && (this._clanHall.getGrade() != 3 || this._function.getLvl() >= 40)) {
            this.writeC(2);
            this.writeC(2);
         } else {
            this.writeC(1);
            this.writeC(1);
         }
      } else {
         this.writeC(0);
         this.writeC(0);
      }

      this._function = this._clanHall.getFunction(5);
      if (this._function != null && this._function.getLvl() != 0) {
         if ((this._clanHall.getGrade() != 0 || this._function.getLvl() >= 25)
            && (this._clanHall.getGrade() != 1 || this._function.getLvl() >= 30)
            && (this._clanHall.getGrade() != 2 || this._function.getLvl() >= 40)
            && (this._clanHall.getGrade() != 3 || this._function.getLvl() >= 50)) {
            this.writeC(2);
         } else {
            this.writeC(1);
         }
      } else {
         this.writeC(0);
      }

      this._function = this._clanHall.getFunction(1);
      if (this._function != null && this._function.getLvl() != 0) {
         if (this._function.getLvl() < 2) {
            this.writeC(1);
         } else {
            this.writeC(2);
         }
      } else {
         this.writeC(0);
      }

      this.writeC(0);
      this._function = this._clanHall.getFunction(8);
      if (this._function != null && this._function.getLvl() != 0) {
         if (this._function.getLvl() <= 1) {
            this.writeC(1);
         } else {
            this.writeC(2);
         }
      } else {
         this.writeC(0);
      }

      this._function = this._clanHall.getFunction(2);
      if (this._function != null && this._function.getLvl() != 0) {
         if ((this._clanHall.getGrade() != 0 || this._function.getLvl() >= 2) && this._function.getLvl() >= 3) {
            this.writeC(2);
         } else {
            this.writeC(1);
         }
      } else {
         this.writeC(0);
      }

      this._function = this._clanHall.getFunction(6);
      if (this._function != null && this._function.getLvl() != 0) {
         if ((this._clanHall.getGrade() != 0 || this._function.getLvl() >= 2)
            && (this._clanHall.getGrade() != 1 || this._function.getLvl() >= 4)
            && (this._clanHall.getGrade() != 2 || this._function.getLvl() >= 5)
            && (this._clanHall.getGrade() != 3 || this._function.getLvl() >= 8)) {
            this.writeC(2);
            this.writeC(2);
         } else {
            this.writeC(1);
            this.writeC(1);
         }
      } else {
         this.writeC(0);
         this.writeC(0);
      }

      this._function = this._clanHall.getFunction(7);
      if (this._function != null && this._function.getLvl() != 0) {
         if (this._function.getLvl() <= 1) {
            this.writeC(1);
         } else {
            this.writeC(2);
         }
      } else {
         this.writeC(0);
      }

      this._function = this._clanHall.getFunction(2);
      if (this._function != null && this._function.getLvl() != 0) {
         if ((this._clanHall.getGrade() != 0 || this._function.getLvl() >= 2) && this._function.getLvl() >= 3) {
            this.writeC(2);
         } else {
            this.writeC(1);
         }
      } else {
         this.writeC(0);
      }

      this.writeD(0);
      this.writeD(0);
   }
}
