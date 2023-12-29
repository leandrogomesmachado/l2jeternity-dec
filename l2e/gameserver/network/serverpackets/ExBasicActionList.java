package l2e.gameserver.network.serverpackets;

public final class ExBasicActionList extends GameServerPacket {
   public static final int[] ACTIONS_ON_TRANSFORM = new int[]{
      1,
      2,
      3,
      4,
      5,
      6,
      7,
      8,
      9,
      11,
      15,
      16,
      17,
      18,
      19,
      21,
      22,
      23,
      32,
      36,
      39,
      40,
      41,
      42,
      43,
      44,
      45,
      46,
      47,
      48,
      50,
      52,
      53,
      54,
      55,
      56,
      57,
      63,
      64,
      65,
      70,
      1000,
      1001,
      1003,
      1004,
      1005,
      1006,
      1007,
      1008,
      1009,
      1010,
      1011,
      1012,
      1013,
      1014,
      1015,
      1016,
      1017,
      1018,
      1019,
      1020,
      1021,
      1022,
      1023,
      1024,
      1025,
      1026,
      1027,
      1028,
      1029,
      1030,
      1031,
      1032,
      1033,
      1034,
      1035,
      1036,
      1037,
      1038,
      1039,
      1040,
      1041,
      1042,
      1043,
      1044,
      1045,
      1046,
      1047,
      1048,
      1049,
      1050,
      1051,
      1052,
      1053,
      1054,
      1055,
      1056,
      1057,
      1058,
      1059,
      1060,
      1061,
      1062,
      1063,
      1064,
      1065,
      1066,
      1067,
      1068,
      1069,
      1070,
      1071,
      1072,
      1073,
      1074,
      1075,
      1076,
      1077,
      1078,
      1079,
      1080,
      1081,
      1082,
      1083,
      1084,
      1089,
      1090,
      1091,
      1092,
      1093,
      1094,
      1095,
      1096,
      1097,
      1098
   };
   public static final int[] DEFAULT_ACTION_LIST = new int[189];
   public static final ExBasicActionList STATIC_PACKET;
   private final int[] _actionIds;

   public ExBasicActionList(int[] actionIds) {
      this._actionIds = actionIds;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._actionIds.length);

      for(int i = 0; i < this._actionIds.length; ++i) {
         this.writeD(this._actionIds[i]);
      }
   }

   static {
      int count1 = 74;
      int count2 = 99;
      int count3 = 16;
      int i = 74;

      while(i-- > 0) {
         DEFAULT_ACTION_LIST[i] = i;
      }

      i = 99;

      while(i-- > 0) {
         DEFAULT_ACTION_LIST[74 + i] = 1000 + i;
      }

      i = 16;

      while(i-- > 0) {
         DEFAULT_ACTION_LIST[173 + i] = 5000 + i;
      }

      STATIC_PACKET = new ExBasicActionList(DEFAULT_ACTION_LIST);
   }
}
