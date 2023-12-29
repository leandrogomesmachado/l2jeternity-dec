package l2e.gameserver.network.serverpackets;

public class ExStartScenePlayer extends GameServerPacket {
   private final int _movieId;
   public static final int LINDVIOR = 1;
   public static final int EKIMUS_OPENING = 2;
   public static final int EKIMUS_SUCCESS = 3;
   public static final int EKIMUS_FAIL = 4;
   public static final int TIAT_OPENING = 5;
   public static final int TIAT_SUCCESS = 6;
   public static final int TIAT_FAIL = 7;
   public static final int SSQ_SUSPECIOUS_DEATHS = 8;
   public static final int SSQ_DYING_MASSAGE = 9;
   public static final int SSQ_CONTRACT_OF_MAMMON = 10;
   public static final int SSQ_RITUAL_OF_PRIEST = 11;
   public static final int SSQ_SEALING_EMPEROR_1ST = 12;
   public static final int SSQ_SEALING_EMPEROR_2ND = 13;
   public static final int SSQ_EMBRYO = 14;
   public static final int LAND_KSERTH_A = 1000;
   public static final int LAND_KSERTH_B = 1001;
   public static final int LAND_UNDEAD_A = 1002;
   public static final int LAND_DISTRUCTION_A = 1004;

   public ExStartScenePlayer(int id) {
      this._movieId = id;
   }

   @Override
   public void writeImpl() {
      this.writeD(this._movieId);
   }
}
