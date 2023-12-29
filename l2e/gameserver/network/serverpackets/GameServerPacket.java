package l2e.gameserver.network.serverpackets;

import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.GameClient;
import l2e.gameserver.network.ServerPacketOpcodes;
import org.nio.impl.SendablePacket;

public abstract class GameServerPacket extends SendablePacket<GameClient> {
   protected static final Logger _log = Logger.getLogger(GameServerPacket.class.getName());
   protected boolean _invisible = false;

   public boolean isInvisible() {
      return this._invisible;
   }

   public void setInvisible(boolean b) {
      this._invisible = b;
   }

   protected void writeD(boolean b) {
      this.writeD(b ? 1 : 0);
   }

   protected void writeItemInfo(ItemInstance item) {
      this.writeItemInfo(item, item.getCount());
   }

   protected void writeItemInfo(ItemInstance item, long count) {
      this.writeD(item.getObjectId());
      this.writeD(item.getDisplayId());
      this.writeD(item.getLocationSlot());
      this.writeQ(count);
      this.writeH(item.getItem().getType2());
      this.writeH(item.getCustomType1());
      this.writeH(item.isEquipped() ? 1 : 0);
      this.writeD(item.getItem().getBodyPart());
      this.writeH(item.getEnchantLevel());
      this.writeH(item.getCustomType2());
      this.writeD(item.isAugmented() ? item.getAugmentation().getAugmentationId() : 0);
      this.writeD(item.getMana());
      this.writeD(item.isTimeLimitedItem() ? (int)(item.getRemainingTime() / 1000L) : -9999);
      this.writeH(item.getAttackElementType());
      this.writeH(item.getAttackElementPower());

      for(byte i = 0; i < 6; ++i) {
         this.writeH(item.getElementDefAttr(i));
      }

      for(int op : item.getEnchantOptions()) {
         this.writeH(op);
      }
   }

   @Override
   protected boolean write() {
      try {
         if (this.writeOpcodes()) {
            this.writeImpl();
            if (Config.PACKET_HANDLER_DEBUG) {
               _log.info(this.getClass().getSimpleName());
            }

            return true;
         }
      } catch (RuntimeException var2) {
         if (Config.PACKET_HANDLER_DEBUG) {
            _log.log(
               Level.SEVERE,
               "Client: " + this.getClient().toString() + " - Failed writing: " + this.getClass().getSimpleName() + " - Revision: " + 2210 + "",
               (Throwable)var2
            );
         }
      }

      return false;
   }

   protected boolean writeOpcodes() {
      ServerPacketOpcodes opcodes = this.getOpcodes();
      if (opcodes == null) {
         return false;
      } else {
         int opcode = opcodes.getId();
         if (opcode >= 0) {
            this.writeC(opcode);
            int exOpcode = opcodes.getExId();
            if (exOpcode >= 0) {
               this.writeH(exOpcode);
            }

            return true;
         } else {
            return false;
         }
      }
   }

   protected ServerPacketOpcodes getOpcodes() {
      try {
         return ServerPacketOpcodes.valueOf(this.getClass().getSimpleName());
      } catch (Exception var2) {
         _log.warning("Cannot find serverpacket opcode: " + this.getClass().getSimpleName() + "!");
         return null;
      }
   }

   protected abstract void writeImpl();
}
