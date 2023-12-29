package l2e.gameserver.model;

import java.util.Iterator;
import l2e.commons.collections.EmptyIterator;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.GameServerPacket;

public interface PlayerGroup extends Iterable<Player> {
   PlayerGroup EMPTY = new PlayerGroup() {
      @Override
      public void broadCast(GameServerPacket... packet) {
      }

      @Override
      public int getMemberCount() {
         return 0;
      }

      @Override
      public Player getGroupLeader() {
         return null;
      }

      @Override
      public Iterator<Player> iterator() {
         return EmptyIterator.getInstance();
      }
   };

   void broadCast(GameServerPacket... var1);

   int getMemberCount();

   Player getGroupLeader();
}
