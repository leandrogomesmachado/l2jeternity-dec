package l2e.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;

public final class RelationChanged extends GameServerPacket {
   public static final int RELATION_PARTY1 = 1;
   public static final int RELATION_PARTY2 = 2;
   public static final int RELATION_PARTY3 = 4;
   public static final int RELATION_PARTY4 = 8;
   public static final int RELATION_PARTYLEADER = 16;
   public static final int RELATION_HAS_PARTY = 32;
   public static final int RELATION_CLAN_MEMBER = 64;
   public static final int RELATION_LEADER = 128;
   public static final int RELATION_CLAN_MATE = 256;
   public static final int RELATION_INSIEGE = 512;
   public static final int RELATION_ATTACKER = 1024;
   public static final int RELATION_ALLY = 2048;
   public static final int RELATION_ENEMY = 4096;
   public static final int RELATION_MUTUAL_WAR = 16384;
   public static final int RELATION_1SIDED_WAR = 32768;
   public static final int RELATION_ALLY_MEMBER = 65536;
   public static final int RELATION_TERRITORY_WAR = 524288;
   protected final List<RelationChanged.RelationChangedData> _data;

   protected RelationChanged(int s) {
      this._data = new ArrayList<>(s);
   }

   protected void add(RelationChanged.RelationChangedData data) {
      this._data.add(data);
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._data.size());

      for(RelationChanged.RelationChangedData d : this._data) {
         this.writeD(d.charObjId);
         this.writeD(d.relation);
         this.writeD(d.isAutoAttackable ? 1 : 0);
         this.writeD(d.karma);
         this.writeD(d.pvpFlag);
      }
   }

   public static GameServerPacket update(Player sendTo, Playable targetPlayable, Player activeChar) {
      if (sendTo != null && targetPlayable != null && activeChar != null) {
         Player targetPlayer = targetPlayable.getActingPlayer();
         int relation = targetPlayer == null ? 0 : targetPlayer.getRelation(activeChar);
         RelationChanged pkt = new RelationChanged(1);
         pkt.add(new RelationChanged.RelationChangedData(targetPlayable, targetPlayable.isAutoAttackable(activeChar), relation));
         return pkt;
      } else {
         return null;
      }
   }

   static class RelationChangedData {
      public final int charObjId;
      public final boolean isAutoAttackable;
      public final int relation;
      public final int karma;
      public final int pvpFlag;

      public RelationChangedData(Playable cha, boolean _isAutoAttackable, int _relation) {
         this.isAutoAttackable = _isAutoAttackable;
         this.relation = _relation;
         this.charObjId = cha.getObjectId();
         this.karma = cha.getKarma();
         this.pvpFlag = cha.getPvpFlag();
      }
   }
}
