package l2e.gameserver.network.serverpackets;

public class ShortBuffStatusUpdate extends GameServerPacket {
   public static final ShortBuffStatusUpdate RESET_SHORT_BUFF = new ShortBuffStatusUpdate(0, 0, 0);
   private final int _skillId;
   private final int _skillLvl;
   private final int _duration;

   public ShortBuffStatusUpdate(int skillId, int skillLvl, int duration) {
      this._skillId = skillId;
      this._skillLvl = skillLvl;
      this._duration = duration;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._skillId);
      this.writeD(this._skillLvl);
      this.writeD(this._duration);
   }
}
