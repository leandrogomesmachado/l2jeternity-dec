package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.ShortCutTemplate;
import l2e.gameserver.model.base.ShortcutType;
import l2e.gameserver.network.serverpackets.ShortCutRegister;

public final class RequestShortCutReg extends GameClientPacket {
   private ShortcutType _type;
   private int _id;
   private int _slot;
   private int _page;
   private int _lvl;
   private int _characterType;

   @Override
   protected void readImpl() {
      int typeId = this.readD();
      this._type = ShortcutType.values()[typeId >= 1 && typeId <= 6 ? typeId : 0];
      int slot = this.readD();
      this._id = this.readD();
      this._lvl = this.readD();
      this._characterType = this.readD();
      this._slot = slot % 12;
      this._page = slot / 12;
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null && this._page <= 12 && this._page >= 0) {
         ShortCutTemplate sc = new ShortCutTemplate(this._slot, this._page, this._type, this._id, this._lvl, this._characterType);
         activeChar.registerShortCut(sc);
         this.sendPacket(new ShortCutRegister(sc));
      }
   }

   @Override
   protected boolean triggersOnActionRequest() {
      return false;
   }
}
