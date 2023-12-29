package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.BookmarkTemplate;

public class ExGetBookMarkInfo extends GameServerPacket {
   private final Player player;

   public ExGetBookMarkInfo(Player cha) {
      this.player = cha;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(0);
      this.writeD(this.player.getBookmarkslot());
      this.writeD(this.player.getTeleportBookmarks().size());

      for(BookmarkTemplate tpbm : this.player.getTeleportBookmarks()) {
         this.writeD(tpbm.getId());
         this.writeD(tpbm.getX());
         this.writeD(tpbm.getY());
         this.writeD(tpbm.getZ());
         this.writeS(tpbm.getName());
         this.writeD(tpbm.getIcon());
         this.writeS(tpbm.getTag());
      }
   }
}
