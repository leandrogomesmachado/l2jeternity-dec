package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.olympiad.Participant;

public class ExOlympiadUserInfo extends GameServerPacket {
   private final Player _player;
   private Participant _par = null;
   private int _curHp;
   private int _maxHp;
   private int _curCp;
   private int _maxCp;

   public ExOlympiadUserInfo(Player player) {
      this._player = player;
      if (this._player != null) {
         this._curHp = (int)this._player.getCurrentHp();
         this._maxHp = (int)this._player.getMaxHp();
         this._curCp = (int)this._player.getCurrentCp();
         this._maxCp = (int)this._player.getMaxCp();
      } else {
         this._curHp = 0;
         this._maxHp = 100;
         this._curCp = 0;
         this._maxCp = 100;
      }
   }

   public ExOlympiadUserInfo(Participant par) {
      this._par = par;
      this._player = par.getPlayer();
      if (this._player != null) {
         this._curHp = (int)this._player.getCurrentHp();
         this._maxHp = (int)this._player.getMaxHp();
         this._curCp = (int)this._player.getCurrentCp();
         this._maxCp = (int)this._player.getMaxCp();
      } else {
         this._curHp = 0;
         this._maxHp = 100;
         this._curCp = 0;
         this._maxCp = 100;
      }
   }

   @Override
   protected final void writeImpl() {
      if (this._player != null) {
         this.writeC(this._player.getOlympiadSide());
         this.writeD(this._player.getObjectId());
         this.writeS(this._player.getName());
         this.writeD(this._player.getClassId().getId());
      } else {
         this.writeC(this._par.getSide());
         this.writeD(this._par.getObjectId());
         this.writeS(this._par.getName());
         this.writeD(this._par.getBaseClass());
      }

      this.writeD(this._curHp);
      this.writeD(this._maxHp);
      this.writeD(this._curCp);
      this.writeD(this._maxCp);
   }
}
