package l2e.gameserver.network.serverpackets;

public class PlaySound extends GameServerPacket {
   private final int _unknown1;
   private final String _soundFile;
   private final int _unknown3;
   private final int _unknown4;
   private final int _unknown5;
   private final int _unknown6;
   private final int _unknown7;
   private final int _unknown8;

   public PlaySound(String soundFile) {
      this._unknown1 = 0;
      this._soundFile = soundFile;
      this._unknown3 = 0;
      this._unknown4 = 0;
      this._unknown5 = 0;
      this._unknown6 = 0;
      this._unknown7 = 0;
      this._unknown8 = 0;
   }

   public PlaySound(int unknown1, String soundFile, int unknown3, int unknown4, int unknown5, int unknown6, int unknown7) {
      this._unknown1 = unknown1;
      this._soundFile = soundFile;
      this._unknown3 = unknown3;
      this._unknown4 = unknown4;
      this._unknown5 = unknown5;
      this._unknown6 = unknown6;
      this._unknown7 = unknown7;
      this._unknown8 = 0;
   }

   public PlaySound(int unknown, String soundFile) {
      this._unknown1 = unknown;
      this._soundFile = soundFile;
      this._unknown3 = 0;
      this._unknown4 = 0;
      this._unknown5 = 0;
      this._unknown6 = 0;
      this._unknown7 = 0;
      this._unknown8 = 0;
   }

   public String getSoundName() {
      return this._soundFile;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._unknown1);
      this.writeS(this._soundFile);
      this.writeD(this._unknown3);
      this.writeD(this._unknown4);
      this.writeD(this._unknown5);
      this.writeD(this._unknown6);
      this.writeD(this._unknown7);
      this.writeD(this._unknown8);
   }
}
