package l2e.gameserver.network.serverpackets;

import java.util.List;
import l2e.commons.util.StringUtil;
import l2e.gameserver.model.actor.Player;

public class ShowBoard extends GameServerPacket {
   private final StringBuilder _htmlCode;
   private final boolean _isHide;

   public ShowBoard() {
      this._htmlCode = null;
      this._isHide = true;
   }

   public ShowBoard(String htmlCode, String id, Player player) {
      this._htmlCode = StringUtil.startAppend(500, id, "\b", htmlCode);
      this._isHide = false;
   }

   public ShowBoard(List<String> arg) {
      this._htmlCode = StringUtil.startAppend(500, "1002\b");

      for(String str : arg) {
         StringUtil.append(this._htmlCode, str, " \b");
      }

      this._isHide = false;
   }

   @Override
   protected final void writeImpl() {
      if (this._isHide) {
         this.writeC(0);
      } else {
         this.writeC(1);
         this.writeS("bypass _bbshome");
         this.writeS("bypass _bbsgetfav");
         this.writeS("bypass _bbsloc");
         this.writeS("bypass _bbsclan");
         this.writeS("bypass _bbsmemo");
         this.writeS("bypass _maillist_0_1_0_");
         this.writeS("bypass _bbsfriends");
         this.writeS("bypass _bbsaddfav");
         this.writeS(this._htmlCode.toString());
      }
   }
}
