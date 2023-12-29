package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Npc;

public class MonRaceInfo extends GameServerPacket {
   private final int _unknown1;
   private final int _unknown2;
   private final Npc[] _monsters;
   private final int[][] _speeds;

   public MonRaceInfo(int unknown1, int unknown2, Npc[] monsters, int[][] speeds) {
      this._unknown1 = unknown1;
      this._unknown2 = unknown2;
      this._monsters = monsters;
      this._speeds = speeds;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._unknown1);
      this.writeD(this._unknown2);
      this.writeD(8);

      for(int i = 0; i < 8; ++i) {
         this.writeD(this._monsters[i].getObjectId());
         this.writeD(this._monsters[i].getTemplate().getId() + 1000000);
         this.writeD(14107);
         this.writeD(181875 + 58 * (7 - i));
         this.writeD(-3566);
         this.writeD(12080);
         this.writeD(181875 + 58 * (7 - i));
         this.writeD(-3566);
         this.writeF(this._monsters[i].getTemplate().getfCollisionHeight());
         this.writeF(this._monsters[i].getTemplate().getfCollisionRadius());
         this.writeD(120);

         for(int j = 0; j < 20; ++j) {
            if (this._unknown1 == 0) {
               this.writeC(this._speeds[i][j]);
            } else {
               this.writeC(0);
            }
         }

         this.writeD(0);
         this.writeD(0);
      }
   }
}
