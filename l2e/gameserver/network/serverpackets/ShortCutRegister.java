package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.templates.ShortCutTemplate;

public final class ShortCutRegister extends GameServerPacket {
   private final ShortCutTemplate _shortcut;

   public ShortCutRegister(ShortCutTemplate shortcut) {
      this._shortcut = shortcut;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._shortcut.getType().ordinal());
      this.writeD(this._shortcut.getSlot() + this._shortcut.getPage() * 12);
      switch(this._shortcut.getType()) {
         case ITEM:
            this.writeD(this._shortcut.getId());
            this.writeD(this._shortcut.getCharacterType());
            this.writeD(this._shortcut.getSharedReuseGroup());
            this.writeD(this._shortcut.getCurrenReuse());
            this.writeD(this._shortcut.getReuse());
            this.writeD(this._shortcut.getAugmentationId());
            break;
         case SKILL:
            this.writeD(this._shortcut.getId());
            this.writeD(this._shortcut.getLevel());
            this.writeC(0);
            this.writeD(this._shortcut.getCharacterType());
            break;
         case ACTION:
         case MACRO:
         case RECIPE:
         case BOOKMARK:
            this.writeD(this._shortcut.getId());
            this.writeD(this._shortcut.getCharacterType());
      }
   }
}
