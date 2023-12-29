package l2e.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.Henna;

public final class GMHennaInfo extends GameServerPacket {
   private final Player _activeChar;
   private final List<Henna> _hennas = new ArrayList<>();

   public GMHennaInfo(Player player) {
      this._activeChar = player;

      for(Henna henna : this._activeChar.getHennaList()) {
         if (henna != null) {
            this._hennas.add(henna);
         }
      }
   }

   @Override
   protected void writeImpl() {
      this.writeC(this._activeChar.getHennaStatINT());
      this.writeC(this._activeChar.getHennaStatSTR());
      this.writeC(this._activeChar.getHennaStatCON());
      this.writeC(this._activeChar.getHennaStatMEN());
      this.writeC(this._activeChar.getHennaStatDEX());
      this.writeC(this._activeChar.getHennaStatWIT());
      this.writeD(3);
      this.writeD(this._hennas.size());

      for(Henna henna : this._hennas) {
         this.writeD(henna.getDyeId());
         this.writeD(1);
      }
   }
}
