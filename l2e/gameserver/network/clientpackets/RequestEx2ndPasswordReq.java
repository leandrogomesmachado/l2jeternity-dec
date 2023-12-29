package l2e.gameserver.network.clientpackets;

import l2e.gameserver.Config;
import l2e.gameserver.data.holder.SecPasswordHolder;
import l2e.gameserver.network.serverpackets.Ex2ndPasswordAck;

public class RequestEx2ndPasswordReq extends GameClientPacket {
   private int _changePass;
   private String _password;
   private String _newPassword;

   @Override
   protected void readImpl() {
      this._changePass = this.readC();
      this._password = this.readS();
      if (this._changePass == 2) {
         this._newPassword = this.readS();
      }
   }

   @Override
   protected void runImpl() {
      if (Config.SECOND_AUTH_ENABLED) {
         SecPasswordHolder spa = this.getClient().getSecondaryAuth();
         boolean exVal = false;
         if (this._changePass == 0 && !spa.passwordExist()) {
            exVal = spa.savePassword(this._password);
         } else if (this._changePass == 2 && spa.passwordExist()) {
            exVal = spa.changePassword(this._password, this._newPassword);
         }

         if (exVal) {
            this.getClient().sendPacket(new Ex2ndPasswordAck(Ex2ndPasswordAck.SUCCESS));
         }
      }
   }
}
