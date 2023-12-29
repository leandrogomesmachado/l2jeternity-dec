package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.ShortCutTemplate;

public final class ShortCutInit extends GameServerPacket {
   private ShortCutTemplate[] _shortCuts;

   public ShortCutInit(Player activeChar) {
      if (activeChar != null) {
         this._shortCuts = activeChar.getAllShortCuts();
      }
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._shortCuts.length);

      for(ShortCutTemplate sc : this._shortCuts) {
         this.writeD(sc.getType().ordinal());
         this.writeD(sc.getSlot() + sc.getPage() * 12);
         switch(sc.getType()) {
            case ITEM:
               this.writeD(sc.getId());
               this.writeD(1);
               this.writeD(sc.getSharedReuseGroup());
               this.writeD(sc.getCurrenReuse());
               this.writeD(sc.getReuse());
               this.writeH(sc.getAugmentationId());
               this.writeH(0);
               break;
            case SKILL:
               this.writeD(sc.getId());
               this.writeD(sc.getLevel());
               this.writeC(0);
               this.writeD(1);
               break;
            case ACTION:
            case MACRO:
            case RECIPE:
            case BOOKMARK:
               this.writeD(sc.getId());
               this.writeD(1);
         }
      }
   }
}
