package l2e.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.model.actor.templates.player.PcTemplate;

public final class NewCharacterSuccess extends GameServerPacket {
   private final List<PcTemplate> _chars = new ArrayList<>();

   public void addChar(PcTemplate template) {
      this._chars.add(template);
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._chars.size());

      for(PcTemplate chr : this._chars) {
         if (chr != null) {
            this.writeD(chr.getRace().ordinal());
            this.writeD(chr.getClassId().getId());
            this.writeD(70);
            this.writeD(chr.getBaseSTR());
            this.writeD(10);
            this.writeD(70);
            this.writeD(chr.getBaseDEX());
            this.writeD(10);
            this.writeD(70);
            this.writeD(chr.getBaseCON());
            this.writeD(10);
            this.writeD(70);
            this.writeD(chr.getBaseINT());
            this.writeD(10);
            this.writeD(70);
            this.writeD(chr.getBaseWIT());
            this.writeD(10);
            this.writeD(70);
            this.writeD(chr.getBaseMEN());
            this.writeD(10);
         }
      }
   }
}
