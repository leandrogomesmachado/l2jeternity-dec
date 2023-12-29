package l2e.gameserver.network.clientpackets;

import Interface.impl.ConfigPacket;
import Interface.impl.KeyPacket;
import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.Interface.InterfaceConfigPacket;
import l2e.gameserver.network.serverpackets.Interface.InterfaceCustomFontsPacket;
import l2e.gameserver.network.serverpackets.Interface.InterfaceKeyPacket;
import l2e.gameserver.network.serverpackets.Interface.InterfaceScreenTextInfoPacket;

public class RequestKeyPacket extends GameClientPacket {
   byte[] data = null;
   int data_size;

   @Override
   public void readImpl() {
      if (this._buf.remaining() > 2) {
         this.data_size = this.readH();
         if (this._buf.remaining() >= this.data_size) {
            this.data = new byte[this.data_size];
            this.readB(this.data);
         }
      }
   }

   @Override
   public void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         if (Config.CUSTOM_INTERFACE_VERSION_OLD) {
            activeChar.sendPacket(new KeyPacket().sendKey(this.data, this.data_size));
            activeChar.sendPacket(new ConfigPacket());
         } else {
            activeChar.sendPacket(new InterfaceKeyPacket().sendKey(this.data, this.data_size));
            activeChar.sendPacket(new InterfaceConfigPacket());
            activeChar.sendPacket(new InterfaceCustomFontsPacket().sendFontInfos());
            activeChar.sendPacket(new InterfaceScreenTextInfoPacket().sendTextInfos());
         }
      }
   }
}
