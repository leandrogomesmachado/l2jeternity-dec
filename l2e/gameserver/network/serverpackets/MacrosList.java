package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.Macro;
import l2e.gameserver.model.actor.templates.MacroTemplate;

public class MacrosList extends GameServerPacket {
   private final int _rev;
   private final int _count;
   private final Macro _macro;

   public MacrosList(int rev, int count, Macro macro) {
      this._rev = rev;
      this._count = count;
      this._macro = macro;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._rev);
      this.writeC(0);
      this.writeC(this._count);
      this.writeC(this._macro != null ? 1 : 0);
      if (this._macro != null) {
         this.writeD(this._macro.getId());
         this.writeS(this._macro.getName());
         this.writeS(this._macro.getDescr());
         this.writeS(this._macro.getAcronym());
         this.writeC(this._macro.getIcon());
         this.writeC(this._macro.getCommands().size());
         int i = 1;

         for(MacroTemplate cmd : this._macro.getCommands()) {
            this.writeC(i++);
            this.writeC(cmd.getType().ordinal());
            this.writeD(cmd.getD1());
            this.writeC(cmd.getD2());
            this.writeS(cmd.getCmd());
         }
      }
   }
}
