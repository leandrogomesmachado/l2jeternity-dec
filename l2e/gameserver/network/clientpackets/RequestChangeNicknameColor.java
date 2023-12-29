package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;

public class RequestChangeNicknameColor extends GameClientPacket {
   private static final int[] COLORS = new int[]{9671679, 8145404, 9959676, 16423662, 16735635, 64672, 10528257, 7903407, 4743829, 10066329};
   private int _colorNum;
   private int _itemObjectId;
   private String _title;

   @Override
   protected void readImpl() {
      this._colorNum = this.readD();
      this._title = this.readS();
      this._itemObjectId = this.readD();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         if (this._colorNum >= 0 && this._colorNum < COLORS.length) {
            ItemInstance item = activeChar.getInventory().getItemByObjectId(this._itemObjectId);
            if (item != null
               && item.getEtcItem() != null
               && item.getEtcItem().getHandlerName() != null
               && item.getEtcItem().getHandlerName().equalsIgnoreCase("NicknameColor")) {
               if (activeChar.destroyItem("Consume", item, 1L, null, true)) {
                  activeChar.setTitle(this._title);
                  activeChar.getAppearance().setTitleColor(COLORS[this._colorNum]);
                  activeChar.setVar("titlecolor", Integer.toString(COLORS[this._colorNum]), -1L);
                  activeChar.broadcastUserInfo(true);
               }
            }
         }
      }
   }
}
