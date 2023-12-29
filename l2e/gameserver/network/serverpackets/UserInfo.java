package l2e.gameserver.network.serverpackets;

import l2e.gameserver.Config;
import l2e.gameserver.data.parser.ExperienceParser;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.instancemanager.CursedWeaponsManager;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.CubicInstance;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.matching.MatchingRoom;
import l2e.gameserver.model.skills.effects.AbnormalEffect;

public final class UserInfo extends GameServerPacket {
   private final Player _activeChar;
   private int _relation;
   private int _airShipHelm;
   private final boolean partyRoom;
   private final int _runSpd;
   private final int _walkSpd;
   private final int _swimRunSpd;
   private final int _swimWalkSpd;
   private final int _flyRunSpd;
   private final int _flyWalkSpd;
   private final double _moveMultiplier;
   private final int[] _visualSlots;

   public UserInfo(Player character) {
      this._activeChar = character;
      this._visualSlots = character.getUserVisualSlots();
      this._moveMultiplier = character.getMovementSpeedMultiplier();
      this._runSpd = (int)Math.round(character.getRunSpeed() / this._moveMultiplier);
      this._walkSpd = (int)Math.round(character.getWalkSpeed() / this._moveMultiplier);
      this._swimRunSpd = (int)Math.round(character.getSwimRunSpeed() / this._moveMultiplier);
      this._swimWalkSpd = (int)Math.round(character.getSwimWalkSpeed() / this._moveMultiplier);
      this._flyRunSpd = character.isFlying() ? this._runSpd : 0;
      this._flyWalkSpd = character.isFlying() ? this._walkSpd : 0;
      this.partyRoom = character.getMatchingRoom() != null
         && character.getMatchingRoom().getType() == MatchingRoom.PARTY_MATCHING
         && character.getMatchingRoom().getLeader() == character;
      int _territoryId = TerritoryWarManager.getInstance().getRegisteredTerritoryId(character);
      this._relation = this._activeChar.isClanLeader() ? 64 : 0;
      if (this._activeChar.getSiegeState() == 1) {
         if (_territoryId == 0) {
            this._relation |= 384;
         } else {
            this._relation |= 4096;
         }
      }

      if (this._activeChar.getSiegeState() == 2) {
         this._relation |= 128;
      }

      if (this._activeChar.isInAirShip() && this._activeChar.getAirShip().isCaptain(this._activeChar)) {
         this._airShipHelm = this._activeChar.getAirShip().getHelmItemId();
      } else {
         this._airShipHelm = 0;
      }
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._activeChar.getX());
      this.writeD(this._activeChar.getY());
      this.writeD(this._activeChar.getZ() + Config.CLIENT_SHIFTZ);
      this.writeD(this._activeChar.getVehicle() != null ? this._activeChar.getVehicle().getObjectId() : 0);
      this.writeD(this._activeChar.getObjectId());
      this.writeS(this._activeChar.getName());
      this.writeD(this._activeChar.getRace().ordinal());
      this.writeD(this._activeChar.getAppearance().getSex() ? 1 : 0);
      this.writeD(this._activeChar.getBaseClass());
      this.writeD(this._activeChar.getLevel());
      this.writeQ(this._activeChar.getExp());
      this.writeF(
         (double)(
            (float)(this._activeChar.getExp() - ExperienceParser.getInstance().getExpForLevel(this._activeChar.getLevel()))
               / (float)(
                  ExperienceParser.getInstance().getExpForLevel(this._activeChar.getLevel() + 1)
                     - ExperienceParser.getInstance().getExpForLevel(this._activeChar.getLevel())
               )
         )
      );
      this.writeD(this._activeChar.getSTR());
      this.writeD(this._activeChar.getDEX());
      this.writeD(this._activeChar.getCON());
      this.writeD(this._activeChar.getINT());
      this.writeD(this._activeChar.getWIT());
      this.writeD(this._activeChar.getMEN());
      this.writeD((int)this._activeChar.getMaxHp());
      this.writeD((int)this._activeChar.getCurrentHp());
      this.writeD((int)this._activeChar.getMaxMp());
      this.writeD((int)this._activeChar.getCurrentMp());
      this.writeD(this._activeChar.getSp());
      this.writeD(this._activeChar.getCurrentLoad());
      this.writeD(this._activeChar.getMaxLoad());
      this.writeD(this._activeChar.getActiveWeaponItem() != null ? 40 : 20);
      this.writeD(this._activeChar.getInventory().getPaperdollObjectId(0));
      this.writeD(this._activeChar.getInventory().getPaperdollObjectId(8));
      this.writeD(this._activeChar.getInventory().getPaperdollObjectId(9));
      this.writeD(this._activeChar.getInventory().getPaperdollObjectId(4));
      this.writeD(this._activeChar.getInventory().getPaperdollObjectId(13));
      this.writeD(this._activeChar.getInventory().getPaperdollObjectId(14));
      this.writeD(this._activeChar.getInventory().getPaperdollObjectId(1));
      this.writeD(this._airShipHelm == 0 ? this._activeChar.getInventory().getPaperdollObjectId(5) : this._airShipHelm);
      this.writeD(this._airShipHelm == 0 ? this._activeChar.getInventory().getPaperdollObjectId(7) : 0);
      this.writeD(this._activeChar.getInventory().getPaperdollObjectId(10));
      this.writeD(this._activeChar.getInventory().getPaperdollObjectId(6));
      this.writeD(this._activeChar.getInventory().getPaperdollObjectId(11));
      this.writeD(this._activeChar.getInventory().getPaperdollObjectId(12));
      this.writeD(this._activeChar.getInventory().getPaperdollObjectId(23));
      this.writeD(this._activeChar.getInventory().getPaperdollObjectId(5));
      this.writeD(this._activeChar.getInventory().getPaperdollObjectId(2));
      this.writeD(this._activeChar.getInventory().getPaperdollObjectId(3));
      this.writeD(this._activeChar.getInventory().getPaperdollObjectId(16));
      this.writeD(this._activeChar.getInventory().getPaperdollObjectId(15));
      this.writeD(this._activeChar.getInventory().getPaperdollObjectId(17));
      this.writeD(this._activeChar.getInventory().getPaperdollObjectId(18));
      this.writeD(this._activeChar.getInventory().getPaperdollObjectId(19));
      this.writeD(this._activeChar.getInventory().getPaperdollObjectId(20));
      this.writeD(this._activeChar.getInventory().getPaperdollObjectId(21));
      this.writeD(this._activeChar.getInventory().getPaperdollObjectId(22));
      this.writeD(this._activeChar.getInventory().getPaperdollObjectId(24));
      this.writeD(this._activeChar.getInventory().getPaperdollItemDisplayId(0));
      this.writeD(this._activeChar.getInventory().getPaperdollItemDisplayId(8));
      this.writeD(this._activeChar.getInventory().getPaperdollItemDisplayId(9));
      this.writeD(this._activeChar.getInventory().getPaperdollItemDisplayId(4));
      this.writeD(this._activeChar.getInventory().getPaperdollItemDisplayId(13));
      this.writeD(this._activeChar.getInventory().getPaperdollItemDisplayId(14));
      this.writeD(this._activeChar.getInventory().getPaperdollItemDisplayId(1));
      this.writeD(this._airShipHelm == 0 ? this._visualSlots[0] : this._airShipHelm);
      this.writeD(this._airShipHelm == 0 ? this._visualSlots[1] : this._airShipHelm);
      this.writeD(this._visualSlots[2]);
      this.writeD(this._visualSlots[3]);
      this.writeD(this._visualSlots[4]);
      this.writeD(this._visualSlots[5]);
      this.writeD(this._visualSlots[6]);
      this.writeD(this._visualSlots[0]);
      this.writeD(this._visualSlots[7]);
      this.writeD(this._visualSlots[8]);
      this.writeD(this._activeChar.getInventory().getPaperdollItemDisplayId(16));
      this.writeD(this._activeChar.getInventory().getPaperdollItemDisplayId(15));
      this.writeD(this._activeChar.getInventory().getPaperdollItemDisplayId(17));
      this.writeD(this._activeChar.getInventory().getPaperdollItemDisplayId(18));
      this.writeD(this._activeChar.getInventory().getPaperdollItemDisplayId(19));
      this.writeD(this._activeChar.getInventory().getPaperdollItemDisplayId(20));
      this.writeD(this._activeChar.getInventory().getPaperdollItemDisplayId(21));
      this.writeD(this._activeChar.getInventory().getPaperdollItemDisplayId(22));
      this.writeD(this._activeChar.getInventory().getPaperdollItemDisplayId(24));
      this.writeD(this._activeChar.getInventory().getPaperdollAugmentationId(0));
      this.writeD(this._activeChar.getInventory().getPaperdollAugmentationId(8));
      this.writeD(this._activeChar.getInventory().getPaperdollAugmentationId(9));
      this.writeD(this._activeChar.getInventory().getPaperdollAugmentationId(4));
      this.writeD(this._activeChar.getInventory().getPaperdollAugmentationId(13));
      this.writeD(this._activeChar.getInventory().getPaperdollAugmentationId(14));
      this.writeD(this._activeChar.getInventory().getPaperdollAugmentationId(1));
      this.writeD(this._airShipHelm == 0 ? this._visualSlots[10] : this._airShipHelm);
      this.writeD(this._airShipHelm == 0 ? this._activeChar.getInventory().getPaperdollAugmentationId(7) : 0);
      this.writeD(this._activeChar.getInventory().getPaperdollAugmentationId(10));
      this.writeD(this._activeChar.getInventory().getPaperdollAugmentationId(6));
      this.writeD(this._activeChar.getInventory().getPaperdollAugmentationId(11));
      this.writeD(this._activeChar.getInventory().getPaperdollAugmentationId(12));
      this.writeD(this._activeChar.getInventory().getPaperdollAugmentationId(23));
      this.writeD(this._activeChar.getInventory().getPaperdollAugmentationId(5));
      this.writeD(this._activeChar.getInventory().getPaperdollAugmentationId(2));
      this.writeD(this._activeChar.getInventory().getPaperdollAugmentationId(3));
      this.writeD(this._activeChar.getInventory().getPaperdollAugmentationId(16));
      this.writeD(this._activeChar.getInventory().getPaperdollAugmentationId(15));
      this.writeD(this._activeChar.getInventory().getPaperdollAugmentationId(17));
      this.writeD(this._activeChar.getInventory().getPaperdollAugmentationId(18));
      this.writeD(this._activeChar.getInventory().getPaperdollAugmentationId(19));
      this.writeD(this._activeChar.getInventory().getPaperdollAugmentationId(20));
      this.writeD(this._activeChar.getInventory().getPaperdollAugmentationId(21));
      this.writeD(this._activeChar.getInventory().getPaperdollAugmentationId(22));
      this.writeD(this._activeChar.getInventory().getPaperdollAugmentationId(24));
      this.writeD(this._activeChar.getInventory().getMaxTalismanCount());
      this.writeD(this._activeChar.getInventory().getCloakStatus());
      this.writeD((int)this._activeChar.getPAtk(null));
      this.writeD((int)this._activeChar.getPAtkSpd());
      this.writeD((int)this._activeChar.getPDef(null));
      this.writeD(this._activeChar.getEvasionRate(null));
      this.writeD(this._activeChar.getAccuracy());
      this.writeD((int)this._activeChar.getCriticalHit(null, null));
      this.writeD((int)this._activeChar.getMAtk(null, null));
      this.writeD((int)this._activeChar.getMAtkSpd());
      this.writeD((int)this._activeChar.getPAtkSpd());
      this.writeD((int)this._activeChar.getMDef(null, null));
      this.writeD(this._activeChar.getPvpFlag());
      this.writeD(this._activeChar.getKarma());
      this.writeD(this._runSpd);
      this.writeD(this._walkSpd);
      this.writeD(this._swimRunSpd);
      this.writeD(this._swimWalkSpd);
      this.writeD(this._flyRunSpd);
      this.writeD(this._flyWalkSpd);
      this.writeD(this._flyRunSpd);
      this.writeD(this._flyWalkSpd);
      this.writeF(this._moveMultiplier);
      this.writeF((double)this._activeChar.getAttackSpeedMultiplier());
      this.writeF(this._activeChar.getColRadius());
      this.writeF(this._activeChar.getColHeight());
      this.writeD(this._activeChar.getAppearance().getHairStyle());
      this.writeD(this._activeChar.getAppearance().getHairColor());
      this.writeD(this._activeChar.getAppearance().getFace());
      this.writeD(this._activeChar.isGM() ? 1 : 0);
      String title = this._activeChar.isInFightEvent()
         ? this._activeChar.getFightEvent().getVisibleTitle(this._activeChar, this._activeChar, this._activeChar.getTitle(), true)
         : this._activeChar.getTitle();
      if (this._activeChar.isInvisible()) {
         title = "Invisible";
      }

      if (this._activeChar.getPoly().isMorphed()) {
         NpcTemplate polyObj = NpcsParser.getInstance().getTemplate(this._activeChar.getPoly().getPolyId());
         if (polyObj != null) {
            title = title + " - " + polyObj.getName();
         }
      }

      this.writeS(title);
      this.writeD(this._activeChar.getClanId());
      this.writeD(this._activeChar.getClanCrestId());
      this.writeD(this._activeChar.getAllyId());
      this.writeD(this._activeChar.getAllyCrestId());
      this.writeD(this._relation);
      this.writeC(this._activeChar.getMountType().ordinal());
      this.writeC(this._activeChar.getPrivateStoreType());
      this.writeC(this._activeChar.hasDwarvenCraft() ? 1 : 0);
      this.writeD(this._activeChar.getPkKills());
      this.writeD(this._activeChar.getPvpKills());
      this.writeH(this._activeChar.getCubics().size());

      for(CubicInstance c : this._activeChar.getCubics().values()) {
         this.writeH(c.getId());
      }

      this.writeC(this.partyRoom ? 1 : 0);
      this.writeD(
         this._activeChar.isInvisible()
            ? this._activeChar.getAbnormalEffectMask() | AbnormalEffect.STEALTH.getMask()
            : this._activeChar.getAbnormalEffectMask()
      );
      this.writeC(this._activeChar.isInWater(this._activeChar) ? 1 : (this._activeChar.isFlyingMounted() ? 2 : 0));
      this.writeD(this._activeChar.getClanPrivileges());
      this.writeH(this._activeChar.getRecommendation().getRecomLeft());
      this.writeH(this._activeChar.getRecommendation().getRecomHave());
      this.writeD(this._activeChar.getMountNpcId() > 0 ? this._activeChar.getMountNpcId() + 1000000 : 0);
      this.writeH(this._activeChar.getInventoryLimit());
      this.writeD(this._activeChar.getClassId().getId());
      this.writeD(0);
      this.writeD((int)this._activeChar.getMaxCp());
      this.writeD((int)this._activeChar.getCurrentCp());
      this.writeC(!this._activeChar.isMounted() && this._airShipHelm == 0 ? this._visualSlots[9] : 0);
      this.writeC(this._activeChar.getTeam());
      this.writeD(this._activeChar.getClanCrestLargeId());
      this.writeC(this._activeChar.isNoble() ? 1 : 0);
      this.writeC(
         !this._activeChar.isHero() && (!this._activeChar.isGM() || !Config.GM_HERO_AURA) && this._activeChar.getInventory().getHeroStatus() < 1 ? 0 : 1
      );
      this.writeC(this._activeChar.isFishing() ? 1 : 0);
      this.writeD(this._activeChar.getFishx());
      this.writeD(this._activeChar.getFishy());
      this.writeD(this._activeChar.getFishz());
      this.writeD(
         this._activeChar.isInFightEvent()
            ? this._activeChar.getFightEvent().getVisibleNameColor(this._activeChar, this._activeChar.getAppearance().getNameColor(), true)
            : this._activeChar.getAppearance().getNameColor()
      );
      this.writeC(this._activeChar.isRunning() ? 1 : 0);
      this.writeD(this._activeChar.getPledgeClass());
      this.writeD(this._activeChar.getPledgeType());
      this.writeD(this._activeChar.getAppearance().getTitleColor());
      this.writeD(this._activeChar.isCursedWeaponEquipped() ? CursedWeaponsManager.getInstance().getLevel(this._activeChar.getCursedWeaponEquippedId()) : 0);
      this.writeD(this._activeChar.getTransformationId());
      byte attackAttribute = this._activeChar.getAttackElement();
      this.writeH(attackAttribute);
      this.writeH(this._activeChar.getAttackElementValue(attackAttribute));
      this.writeH(this._activeChar.getDefenseElementValue((byte)0));
      this.writeH(this._activeChar.getDefenseElementValue((byte)1));
      this.writeH(this._activeChar.getDefenseElementValue((byte)2));
      this.writeH(this._activeChar.getDefenseElementValue((byte)3));
      this.writeH(this._activeChar.getDefenseElementValue((byte)4));
      this.writeH(this._activeChar.getDefenseElementValue((byte)5));
      this.writeD(this._activeChar.getAgathionId());
      this.writeD(this._activeChar.getFame());
      this.writeD(this._activeChar.isMinimapAllowed() ? 1 : 0);
      this.writeD(this._activeChar.getVitalityPoints());
      this.writeD(this._activeChar.getAbnormalEffectMask2());
   }
}
