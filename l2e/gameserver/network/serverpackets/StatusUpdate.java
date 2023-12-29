package l2e.gameserver.network.serverpackets;

import java.util.ArrayList;
import l2e.gameserver.model.GameObject;

public final class StatusUpdate extends GameServerPacket {
   public static final int LEVEL = 1;
   public static final int EXP = 2;
   public static final int STR = 3;
   public static final int DEX = 4;
   public static final int CON = 5;
   public static final int INT = 6;
   public static final int WIT = 7;
   public static final int MEN = 8;
   public static final int CUR_HP = 9;
   public static final int MAX_HP = 10;
   public static final int CUR_MP = 11;
   public static final int MAX_MP = 12;
   public static final int SP = 13;
   public static final int CUR_LOAD = 14;
   public static final int MAX_LOAD = 15;
   public static final int P_ATK = 17;
   public static final int ATK_SPD = 18;
   public static final int P_DEF = 19;
   public static final int EVASION = 20;
   public static final int ACCURACY = 21;
   public static final int CRITICAL = 22;
   public static final int M_ATK = 23;
   public static final int CAST_SPD = 24;
   public static final int M_DEF = 25;
   public static final int PVP_FLAG = 26;
   public static final int KARMA = 27;
   public static final int CUR_CP = 33;
   public static final int MAX_CP = 34;
   private final int _objectId;
   private final ArrayList<StatusUpdate.Attribute> _attributes = new ArrayList<>();

   public StatusUpdate(int objectId) {
      this._objectId = objectId;
   }

   public StatusUpdate(GameObject object) {
      this._objectId = object.getObjectId();
   }

   public void addAttribute(int id, int level) {
      this._attributes.add(new StatusUpdate.Attribute(id, (double)level));
   }

   public void addAttribute(int id, double level) {
      this._attributes.add(new StatusUpdate.Attribute(id, level));
   }

   public boolean hasAttributes() {
      return !this._attributes.isEmpty();
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._objectId);
      this.writeD(this._attributes.size());

      for(StatusUpdate.Attribute temp : this._attributes) {
         this.writeD(temp._id);
         this.writeD((int)temp._value);
      }
   }

   static class Attribute {
      public int _id;
      public double _value;

      Attribute(int id, double value) {
         this._id = id;
         this._value = value;
      }
   }
}
