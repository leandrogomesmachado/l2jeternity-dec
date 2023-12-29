package l2e.gameserver.network.clientpackets;

import l2e.gameserver.data.holder.CharNameHolder;
import l2e.gameserver.model.BlockedList;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;

public final class RequestBlock extends GameClientPacket {
   private static final int BLOCK = 0;
   private static final int UNBLOCK = 1;
   private static final int BLOCKLIST = 2;
   private static final int ALLBLOCK = 3;
   private static final int ALLUNBLOCK = 4;
   private String _name;
   private Integer _type;

   @Override
   protected void readImpl() {
      this._type = this.readD();
      if (this._type == 0 || this._type == 1) {
         this._name = this.readS();
      }
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      int targetId = CharNameHolder.getInstance().getIdByName(this._name);
      int targetAL = CharNameHolder.getInstance().getAccessLevelById(targetId);
      if (activeChar != null) {
         switch(this._type) {
            case 0:
            case 1:
               if (targetId <= 0) {
                  activeChar.sendPacket(SystemMessageId.FAILED_TO_REGISTER_TO_IGNORE_LIST);
                  return;
               }

               if (targetAL > 0) {
                  activeChar.sendPacket(SystemMessageId.YOU_MAY_NOT_IMPOSE_A_BLOCK_ON_GM);
                  return;
               }

               if (activeChar.getObjectId() == targetId) {
                  return;
               }

               if (this._type == 0) {
                  BlockedList.addToBlockList(activeChar, targetId);
               } else {
                  BlockedList.removeFromBlockList(activeChar, targetId);
               }
               break;
            case 2:
               BlockedList.sendListToOwner(activeChar);
               break;
            case 3:
               activeChar.sendPacket(SystemMessageId.MESSAGE_REFUSAL_MODE);
               BlockedList.setBlockAll(activeChar, true);
               break;
            case 4:
               activeChar.sendPacket(SystemMessageId.MESSAGE_ACCEPTANCE_MODE);
               BlockedList.setBlockAll(activeChar, false);
               break;
            default:
               _log.info("Unknown 0xA9 block type: " + this._type);
         }
      }
   }
}
