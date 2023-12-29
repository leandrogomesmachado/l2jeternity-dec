package l2e.gameserver.network.serverpackets;

import l2e.gameserver.Config;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.skills.effects.AbnormalEffect;
import l2e.gameserver.network.ServerPacketOpcodes;

public class NpcInfoPoly extends GameServerPacket {
   private final Player _activeChar;
   private final int _objId;
   private int _x;
   private int _y;
   private int _z;
   private final int _heading;
   private final double _mAtkSpd;
   private final double _pAtkSpd;
   private final int _runSpd;
   private final int _walkSpd;
   private final double _moveMultiplier;
   private final float _attackSpeedMultiplier;

   @Override
   protected ServerPacketOpcodes getOpcodes() {
      return ServerPacketOpcodes.NpcInfo;
   }

   public NpcInfoPoly(Player cha) {
      this._activeChar = cha;
      this._objId = cha.getObjectId();
      if (this._activeChar.getVehicle() != null && this._activeChar.getInVehiclePosition() != null) {
         this._x = this._activeChar.getInVehiclePosition().getX();
         this._y = this._activeChar.getInVehiclePosition().getY();
         this._z = this._activeChar.getInVehiclePosition().getZ() + Config.CLIENT_SHIFTZ;
      } else {
         this._x = this._activeChar.getX();
         this._y = this._activeChar.getY();
         this._z = this._activeChar.getZ() + Config.CLIENT_SHIFTZ;
      }

      this._heading = this._activeChar.getHeading();
      this._mAtkSpd = this._activeChar.getMAtkSpd();
      this._pAtkSpd = (double)((int)this._activeChar.getPAtkSpd());
      this._moveMultiplier = this._activeChar.getMovementSpeedMultiplier();
      this._attackSpeedMultiplier = this._activeChar.getAttackSpeedMultiplier();
      this._runSpd = (int)(this._activeChar.getRunSpeed() / this._moveMultiplier);
      this._walkSpd = (int)(this._activeChar.getWalkSpeed() / this._moveMultiplier);
      this._invisible = cha.isInvisible();
   }

   @Override
   protected final void writeImpl() {
      boolean gmSeeInvis = false;
      if (this._invisible) {
         Player activeChar = this.getClient().getActiveChar();
         if (activeChar != null && activeChar.canOverrideCond(PcCondOverride.SEE_ALL_PLAYERS)) {
            gmSeeInvis = true;
         }
      }

      NpcTemplate template = NpcsParser.getInstance().getTemplate(this._activeChar.getPoly().getPolyId());
      if (template != null) {
         this.writeD(this._objId);
         this.writeD(template.getId() + 1000000);
         this.writeD(this._activeChar.getKarma() > 0 ? 1 : 0);
         this.writeD(this._x);
         this.writeD(this._y);
         this.writeD(this._z);
         this.writeD(this._heading);
         this.writeD(0);
         this.writeD((int)this._mAtkSpd);
         this.writeD((int)this._pAtkSpd);
         this.writeD(this._runSpd);
         this.writeD(this._walkSpd);
         this.writeD(this._runSpd);
         this.writeD(this._walkSpd);
         this.writeD(this._runSpd);
         this.writeD(this._walkSpd);
         this.writeD(this._runSpd);
         this.writeD(this._walkSpd);
         this.writeF(this._moveMultiplier);
         this.writeF((double)this._attackSpeedMultiplier);
         this.writeF(template.getfCollisionRadius());
         this.writeF(template.getfCollisionHeight());
         this.writeD(template.getRightHand());
         this.writeD(0);
         this.writeD(template.getLeftHand());
         this.writeC(1);
         this.writeC(this._activeChar.isRunning() ? 1 : 0);
         this.writeC(this._activeChar.isInCombat() ? 1 : 0);
         this.writeC(this._activeChar.isAlikeDead() ? 1 : 0);
         this.writeC(!gmSeeInvis && this._invisible ? 1 : 0);
         this.writeD(-1);
         this.writeS(this._activeChar.getAppearance().getVisibleName());
         this.writeD(-1);
         this.writeS(gmSeeInvis ? "Invisible" : this._activeChar.getAppearance().getVisibleTitle());
         this.writeD(this._activeChar.getAppearance().getTitleColor());
         this.writeD(this._activeChar.getPvpFlag());
         this.writeD(this._activeChar.getKarma());
         this.writeD(gmSeeInvis ? this._activeChar.getAbnormalEffectMask() | AbnormalEffect.STEALTH.getMask() : this._activeChar.getAbnormalEffectMask());
         this.writeD(this._activeChar.getClanId());
         this.writeD(this._activeChar.getClanCrestId());
         this.writeD(this._activeChar.getAllyId());
         this.writeD(this._activeChar.getAllyCrestId());
         this.writeC(this._activeChar.isFlying() ? 2 : 0);
         this.writeC(this._activeChar.getTeam());
         this.writeF(template.getfCollisionRadius());
         this.writeF(template.getfCollisionHeight());
         this.writeD(0);
         this.writeD(this._activeChar.isFlying() ? 2 : 0);
         this.writeD(0);
         this.writeD(0);
         this.writeC(!template.isTargetable() ? 1 : 0);
         this.writeC(!template.isShowName() ? 1 : 0);
         this.writeC(this._activeChar.getAbnormalEffectMask2());
         this.writeD(0);
      }
   }
}
