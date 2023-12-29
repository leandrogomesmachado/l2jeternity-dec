package l2e.gameserver.network.clientpackets;

import l2e.gameserver.Config;
import l2e.gameserver.handler.communityhandlers.CommunityBoardHandler;
import l2e.gameserver.handler.communityhandlers.ICommunityBoardHandler;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;

public final class RequestBBSwrite extends GameClientPacket {
   private String _url;
   private String _arg1;
   private String _arg2;
   private String _arg3;
   private String _arg4;
   private String _arg5;

   @Override
   protected final void readImpl() {
      this._url = this.readS();
      this._arg1 = this.readS();
      this._arg2 = this.readS();
      this._arg3 = this.readS();
      this._arg4 = this.readS();
      this._arg5 = this.readS();
   }

   @Override
   protected final void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         ICommunityBoardHandler handler = CommunityBoardHandler.getInstance().getHandler(this._url);
         if (handler != null) {
            if (!Config.ALLOW_COMMUNITY) {
               activeChar.sendPacket(SystemMessageId.CB_OFFLINE);
            } else {
               handler.onWriteCommand(this._url, this._arg1, this._arg2, this._arg3, this._arg4, this._arg5, activeChar);
            }
         }
      }
   }
}
