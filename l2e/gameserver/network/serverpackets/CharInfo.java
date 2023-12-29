package l2e.gameserver.network.serverpackets;

import l2e.gameserver.Config;
import l2e.gameserver.instancemanager.CursedWeaponsManager;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.actor.Decoy;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.CubicInstance;
import l2e.gameserver.model.items.itemcontainer.Inventory;
import l2e.gameserver.model.matching.MatchingRoom;
import l2e.gameserver.model.skills.effects.AbnormalEffect;

public class CharInfo extends GameServerPacket {
   private final Player _activeChar;
   private final Player _player;
   private final Inventory _inv;
   private int _objId;
   private int _x;
   private int _y;
   private int _z;
   private int _heading;
   private final double _mAtkSpd;
   private final double _pAtkSpd;
   private final int _runSpd;
   private final int _walkSpd;
   private final double _moveMultiplier;
   private int _vehicleId;
   private int _airShipHelm;
   private final boolean _isPartyRoomLeader;
   private final int[] _visualSlots;

   public CharInfo(Player cha, Player viewer) {
      this._activeChar = cha;
      this._visualSlots = cha.getCharVisualSlots(viewer);
      this._player = viewer;
      this._objId = cha.getObjectId();
      this._inv = cha.getInventory();
      if (this._activeChar.getVehicle() != null && this._activeChar.getInVehiclePosition() != null) {
         this._x = this._activeChar.getInVehiclePosition().getX();
         this._y = this._activeChar.getInVehiclePosition().getY();
         this._z = this._activeChar.getInVehiclePosition().getZ() + Config.CLIENT_SHIFTZ;
         this._vehicleId = this._activeChar.getVehicle().getObjectId();
         if (this._activeChar.isInAirShip() && this._activeChar.getAirShip().isCaptain(this._activeChar)) {
            this._airShipHelm = this._activeChar.getAirShip().getHelmItemId();
         } else {
            this._airShipHelm = 0;
         }
      } else {
         this._x = this._activeChar.getX();
         this._y = this._activeChar.getY();
         this._z = this._activeChar.getZ() + Config.CLIENT_SHIFTZ;
         this._vehicleId = 0;
         this._airShipHelm = 0;
      }

      this._heading = this._activeChar.getHeading();
      this._mAtkSpd = this._activeChar.getMAtkSpd();
      this._pAtkSpd = (double)((int)this._activeChar.getPAtkSpd());
      this._moveMultiplier = this._activeChar.getMovementSpeedMultiplier();
      this._runSpd = (int)(this._activeChar.getRunSpeed() / this._moveMultiplier);
      this._walkSpd = (int)(this._activeChar.getWalkSpeed() / this._moveMultiplier);
      this._invisible = cha.isInvisible();
      this._isPartyRoomLeader = cha.getMatchingRoom() != null
         && cha.getMatchingRoom().getType() == MatchingRoom.PARTY_MATCHING
         && cha.getMatchingRoom().getLeader() == cha;
   }

   public CharInfo(Decoy decoy) {
      this(decoy.getActingPlayer(), decoy.getActingPlayer());
      this._vehicleId = 0;
      this._airShipHelm = 0;
      this._objId = decoy.getObjectId();
      this._x = decoy.getX();
      this._y = decoy.getY();
      this._z = decoy.getZ();
      this._heading = decoy.getHeading();
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

      this.writeD(this._x);
      this.writeD(this._y);
      this.writeD(this._z);
      this.writeD(this._vehicleId);
      this.writeD(this._objId);
      if (Config.ENABLE_OLY_FEED && this._activeChar.isInOlympiadMode()) {
         this.writeS("Player");
         this.writeD(Config.OLY_ANTI_FEED_RACE);
         this.writeD(Config.OLY_ANTI_FEED_GENDER);
      } else {
         this.writeS(this._activeChar.getAppearance().getVisibleName());
         this.writeD(this._activeChar.getRace().ordinal());
         this.writeD(this._activeChar.getAppearance().getSex() ? 1 : 0);
      }

      if (this._activeChar.getClassIndex() == 0) {
         this.writeD(this._activeChar.getClassId().getId());
      } else {
         this.writeD(this._activeChar.getBaseClass());
      }

      this.writeD(this._inv.getPaperdollItemDisplayId(0));
      this.writeD(this._inv.getPaperdollItemDisplayId(1));
      if (Config.ENABLE_OLY_FEED && this._activeChar.isInOlympiadMode()) {
         this.writeD(Config.OLY_ANTI_FEED_WEAPON_RIGHT);
      } else {
         this.writeD(this._airShipHelm == 0 ? this._visualSlots[0] : this._airShipHelm);
      }

      if (Config.ENABLE_OLY_FEED && this._activeChar.isInOlympiadMode()) {
         this.writeD(Config.OLY_ANTI_FEED_WEAPON_LEFT);
      } else {
         this.writeD(this._airShipHelm == 0 ? this._visualSlots[1] : this._airShipHelm);
      }

      if (Config.ENABLE_OLY_FEED && this._activeChar.isInOlympiadMode()) {
         this.writeD(Config.OLY_ANTI_FEED_GLOVES);
         this.writeD(Config.OLY_ANTI_FEED_CHEST);
         this.writeD(Config.OLY_ANTI_FEED_LEGS);
         this.writeD(Config.OLY_ANTI_FEED_FEET);
      } else {
         this.writeD(this._visualSlots[2]);
         this.writeD(this._visualSlots[3]);
         this.writeD(this._visualSlots[4]);
         this.writeD(this._visualSlots[5]);
      }

      if (Config.ENABLE_OLY_FEED && this._activeChar.isInOlympiadMode()) {
         this.writeD(Config.OLY_ANTI_FEED_CLOAK);
      } else {
         this.writeD(this._visualSlots[6]);
      }

      if (Config.ENABLE_OLY_FEED && this._activeChar.isInOlympiadMode()) {
         this.writeD(Config.OLY_ANTI_FEED_RIGH_HAND_ARMOR);
      } else {
         this.writeD(this._visualSlots[0]);
      }

      if (Config.ENABLE_OLY_FEED && this._activeChar.isInOlympiadMode()) {
         this.writeD(Config.OLY_ANTI_FEED_HAIR_MISC_1);
      } else {
         this.writeD(this._visualSlots[7]);
      }

      if (Config.ENABLE_OLY_FEED && this._activeChar.isInOlympiadMode()) {
         this.writeD(Config.OLY_ANTI_FEED_HAIR_MISC_2);
      } else {
         this.writeD(this._visualSlots[8]);
      }

      this.writeD(this._inv.getPaperdollItemDisplayId(16));
      this.writeD(this._inv.getPaperdollItemDisplayId(15));
      this.writeD(this._inv.getPaperdollItemDisplayId(17));
      this.writeD(this._inv.getPaperdollItemDisplayId(18));
      this.writeD(this._inv.getPaperdollItemDisplayId(19));
      this.writeD(this._inv.getPaperdollItemDisplayId(20));
      this.writeD(this._inv.getPaperdollItemDisplayId(21));
      this.writeD(this._inv.getPaperdollItemDisplayId(22));
      this.writeD(this._inv.getPaperdollItemDisplayId(24));
      this.writeD(this._inv.getPaperdollAugmentationId(0));
      this.writeD(this._inv.getPaperdollAugmentationId(1));
      this.writeD(this._airShipHelm == 0 ? this._visualSlots[10] : this._airShipHelm);
      this.writeD(this._airShipHelm == 0 ? this._inv.getPaperdollAugmentationId(7) : 0);
      this.writeD(this._inv.getPaperdollAugmentationId(10));
      this.writeD(this._inv.getPaperdollAugmentationId(6));
      this.writeD(this._inv.getPaperdollAugmentationId(11));
      this.writeD(this._inv.getPaperdollAugmentationId(12));
      this.writeD(this._inv.getPaperdollAugmentationId(23));
      this.writeD(this._inv.getPaperdollAugmentationId(5));
      this.writeD(this._inv.getPaperdollAugmentationId(2));
      this.writeD(this._inv.getPaperdollAugmentationId(3));
      this.writeD(this._inv.getPaperdollAugmentationId(16));
      this.writeD(this._inv.getPaperdollAugmentationId(15));
      this.writeD(this._inv.getPaperdollAugmentationId(17));
      this.writeD(this._inv.getPaperdollAugmentationId(18));
      this.writeD(this._inv.getPaperdollAugmentationId(19));
      this.writeD(this._inv.getPaperdollAugmentationId(20));
      this.writeD(this._inv.getPaperdollAugmentationId(21));
      this.writeD(this._inv.getPaperdollAugmentationId(22));
      this.writeD(this._inv.getPaperdollAugmentationId(24));
      this.writeD(0);
      this.writeD(1);
      this.writeD(this._activeChar.getPvpFlag());
      this.writeD(this._activeChar.getKarma());
      this.writeD((int)this._mAtkSpd);
      this.writeD((int)this._pAtkSpd);
      this.writeD(0);
      this.writeD(this._runSpd);
      this.writeD(this._walkSpd);
      this.writeD(this._runSpd);
      this.writeD(this._walkSpd);
      this.writeD(this._runSpd);
      this.writeD(this._walkSpd);
      this.writeD(this._runSpd);
      this.writeD(this._walkSpd);
      this.writeF(this._activeChar.getMovementSpeedMultiplier());
      this.writeF((double)this._activeChar.getAttackSpeedMultiplier());
      if (Config.ENABLE_OLY_FEED && this._activeChar.isInOlympiadMode()) {
         this.writeF((double)Config.OLY_ANTI_FEED_CLASS_RADIUS);
         this.writeF((double)Config.OLY_ANTI_FEED_CLASS_HEIGHT);
      } else {
         this.writeF(this._activeChar.getColRadius());
         this.writeF(this._activeChar.getColHeight());
      }

      this.writeD(this._activeChar.getAppearance().getHairStyle());
      this.writeD(this._activeChar.getAppearance().getHairColor());
      this.writeD(this._activeChar.getAppearance().getFace());
      this.writeS(
         gmSeeInvis
            ? "Invisible"
            : (
               this._activeChar.isInFightEvent()
                  ? this._activeChar
                     .getFightEvent()
                     .getVisibleTitle(
                        this._activeChar, this._player != null ? this._player : this._activeChar, this._activeChar.getAppearance().getVisibleTitle(), false
                     )
                  : this._activeChar.getAppearance().getVisibleTitle()
            )
      );
      if (!this._activeChar.isCursedWeaponEquipped()) {
         this.writeD(this._activeChar.getClanId());
         this.writeD(this._activeChar.getClanCrestId());
         this.writeD(this._activeChar.getAllyId());
         this.writeD(this._activeChar.getAllyCrestId());
      } else {
         this.writeD(0);
         this.writeD(0);
         this.writeD(0);
         this.writeD(0);
      }

      this.writeC(this._activeChar.isSitting() ? 0 : 1);
      this.writeC(this._activeChar.isRunning() ? 1 : 0);
      this.writeC(this._activeChar.isInCombat() ? 1 : 0);
      this.writeC(!this._activeChar.isInOlympiadMode() && this._activeChar.isAlikeDead() ? 1 : 0);
      this.writeC(!gmSeeInvis && this._invisible ? 1 : 0);
      this.writeC(this._activeChar.getMountType().ordinal());
      this.writeC(this._activeChar.getPrivateStoreType());
      this.writeH(this._activeChar.getCubics().size());

      for(CubicInstance c : this._activeChar.getCubics().values()) {
         this.writeH(c.getId());
      }

      this.writeC(this._isPartyRoomLeader ? 1 : 0);
      this.writeD(gmSeeInvis ? this._activeChar.getAbnormalEffectMask() | AbnormalEffect.STEALTH.getMask() : this._activeChar.getAbnormalEffectMask());
      this.writeC(this._activeChar.isInWater(this._activeChar) ? 1 : (this._activeChar.isFlyingMounted() ? 2 : 0));
      if (Config.ENABLE_OLY_FEED && this._activeChar.isInOlympiadMode()) {
         this.writeH(Config.OLY_ANTI_FEED_PLAYER_HAVE_RECS);
      } else {
         this.writeH(this._activeChar.getRecommendation().getRecomHave());
      }

      this.writeD(this._activeChar.getMountNpcId() + 1000000);
      this.writeD(this._activeChar.getClassId().getId());
      this.writeD(0);
      this.writeC(!this._activeChar.isMounted() && this._airShipHelm == 0 ? this._visualSlots[9] : 0);
      this.writeC(this._activeChar.getTeam());
      this.writeD(this._activeChar.getClanCrestLargeId());
      this.writeC(this._activeChar.isNoble() ? 1 : 0);
      this.writeC(!this._activeChar.isHero() && (!this._activeChar.isGM() || !Config.GM_HERO_AURA) && this._inv.getHeroStatus() < 1 ? 0 : 1);
      this.writeC(this._activeChar.isFishing() ? 1 : 0);
      this.writeD(this._activeChar.getFishx());
      this.writeD(this._activeChar.getFishy());
      this.writeD(this._activeChar.getFishz());
      this.writeD(
         this._activeChar.isInFightEvent()
            ? this._activeChar.getFightEvent().getVisibleNameColor(this._activeChar, this._activeChar.getAppearance().getNameColor(), false)
            : this._activeChar.getAppearance().getNameColor()
      );
      this.writeD(this._heading);
      this.writeD(this._activeChar.getPledgeClass());
      this.writeD(this._activeChar.getPledgeType());
      this.writeD(this._activeChar.getAppearance().getTitleColor());
      this.writeD(this._activeChar.isCursedWeaponEquipped() ? CursedWeaponsManager.getInstance().getLevel(this._activeChar.getCursedWeaponEquippedId()) : 0);
      this.writeD(this._activeChar.getClanId() > 0 ? this._activeChar.getClan().getReputationScore() : 0);
      this.writeD(this._activeChar.getTransformationId());
      this.writeD(this._activeChar.getAgathionId());
      this.writeD(1);
      this.writeD(this._activeChar.getAbnormalEffectMask2());
   }
}
