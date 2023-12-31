package l2e.gameserver.network.serverpackets;

import l2e.gameserver.data.parser.ExperienceParser;
import l2e.gameserver.model.actor.Player;

public class GMViewCharacterInfo extends GameServerPacket {
   private final Player _activeChar;
   private final int _runSpd;
   private final int _walkSpd;
   private final int _swimRunSpd;
   private final int _swimWalkSpd;
   private final int _flyRunSpd;
   private final int _flyWalkSpd;
   private final double _moveMultiplier;

   public GMViewCharacterInfo(Player cha) {
      this._activeChar = cha;
      this._moveMultiplier = cha.getMovementSpeedMultiplier();
      this._runSpd = (int)Math.round(cha.getRunSpeed() / this._moveMultiplier);
      this._walkSpd = (int)Math.round(cha.getWalkSpeed() / this._moveMultiplier);
      this._swimRunSpd = (int)Math.round(cha.getSwimRunSpeed() / this._moveMultiplier);
      this._swimWalkSpd = (int)Math.round(cha.getSwimWalkSpeed() / this._moveMultiplier);
      this._flyRunSpd = cha.isFlying() ? this._runSpd : 0;
      this._flyWalkSpd = cha.isFlying() ? this._walkSpd : 0;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._activeChar.getX());
      this.writeD(this._activeChar.getY());
      this.writeD(this._activeChar.getZ());
      this.writeD(this._activeChar.getHeading());
      this.writeD(this._activeChar.getObjectId());
      this.writeS(this._activeChar.getName());
      this.writeD(this._activeChar.getRace().ordinal());
      this.writeD(this._activeChar.getAppearance().getSex() ? 1 : 0);
      this.writeD(this._activeChar.getClassId().getId());
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
      this.writeD(this._activeChar.getPkKills());
      this.writeD(this._activeChar.getInventory().getPaperdollObjectId(2));
      this.writeD(this._activeChar.getInventory().getPaperdollObjectId(8));
      this.writeD(this._activeChar.getInventory().getPaperdollObjectId(9));
      this.writeD(this._activeChar.getInventory().getPaperdollObjectId(4));
      this.writeD(this._activeChar.getInventory().getPaperdollObjectId(13));
      this.writeD(this._activeChar.getInventory().getPaperdollObjectId(14));
      this.writeD(this._activeChar.getInventory().getPaperdollObjectId(1));
      this.writeD(this._activeChar.getInventory().getPaperdollObjectId(5));
      this.writeD(this._activeChar.getInventory().getPaperdollObjectId(7));
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
      this.writeD(0);
      this.writeD(this._activeChar.getInventory().getPaperdollItemDisplayId(2));
      this.writeD(this._activeChar.getInventory().getPaperdollItemDisplayId(8));
      this.writeD(this._activeChar.getInventory().getPaperdollItemDisplayId(9));
      this.writeD(this._activeChar.getInventory().getPaperdollItemDisplayId(4));
      this.writeD(this._activeChar.getInventory().getPaperdollItemDisplayId(13));
      this.writeD(this._activeChar.getInventory().getPaperdollItemDisplayId(14));
      this.writeD(this._activeChar.getInventory().getPaperdollItemDisplayId(1));
      this.writeD(this._activeChar.getInventory().getPaperdollItemDisplayId(5));
      this.writeD(this._activeChar.getInventory().getPaperdollItemDisplayId(7));
      this.writeD(this._activeChar.getInventory().getPaperdollItemDisplayId(10));
      this.writeD(this._activeChar.getInventory().getPaperdollItemDisplayId(6));
      this.writeD(this._activeChar.getInventory().getPaperdollItemDisplayId(11));
      this.writeD(this._activeChar.getInventory().getPaperdollItemDisplayId(12));
      this.writeD(this._activeChar.getInventory().getPaperdollItemDisplayId(23));
      this.writeD(this._activeChar.getInventory().getPaperdollItemDisplayId(5));
      this.writeD(this._activeChar.getInventory().getPaperdollItemDisplayId(2));
      this.writeD(this._activeChar.getInventory().getPaperdollItemDisplayId(3));
      this.writeD(this._activeChar.getInventory().getPaperdollItemDisplayId(16));
      this.writeD(this._activeChar.getInventory().getPaperdollItemDisplayId(15));
      this.writeD(this._activeChar.getInventory().getPaperdollItemDisplayId(17));
      this.writeD(this._activeChar.getInventory().getPaperdollItemDisplayId(18));
      this.writeD(this._activeChar.getInventory().getPaperdollItemDisplayId(19));
      this.writeD(this._activeChar.getInventory().getPaperdollItemDisplayId(20));
      this.writeD(this._activeChar.getInventory().getPaperdollItemDisplayId(21));
      this.writeD(this._activeChar.getInventory().getPaperdollItemDisplayId(22));
      this.writeD(0);
      this.writeD(0);
      this.writeD(0);
      this.writeH(0);
      this.writeH(0);
      this.writeH(0);
      this.writeH(0);
      this.writeH(0);
      this.writeH(0);
      this.writeH(0);
      this.writeH(0);
      this.writeH(0);
      this.writeH(0);
      this.writeH(0);
      this.writeH(0);
      this.writeH(0);
      this.writeH(0);
      this.writeD(this._activeChar.getInventory().getPaperdollAugmentationId(5));
      this.writeH(0);
      this.writeH(0);
      this.writeH(0);
      this.writeH(0);
      this.writeH(0);
      this.writeH(0);
      this.writeH(0);
      this.writeH(0);
      this.writeH(0);
      this.writeH(0);
      this.writeH(0);
      this.writeH(0);
      this.writeD(this._activeChar.getInventory().getPaperdollAugmentationId(5));
      this.writeH(0);
      this.writeH(0);
      this.writeH(0);
      this.writeH(0);
      this.writeH(0);
      this.writeH(0);
      this.writeH(0);
      this.writeH(0);
      this.writeH(0);
      this.writeH(0);
      this.writeH(0);
      this.writeH(0);
      this.writeH(0);
      this.writeH(0);
      this.writeH(0);
      this.writeH(0);
      this.writeH(0);
      this.writeH(0);
      this.writeH(0);
      this.writeH(0);
      this.writeH(0);
      this.writeH(0);
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
      this.writeS(this._activeChar.getTitle());
      this.writeD(this._activeChar.getClanId());
      this.writeD(this._activeChar.getClanCrestId());
      this.writeD(this._activeChar.getAllyId());
      this.writeC(this._activeChar.getMountType().ordinal());
      this.writeC(this._activeChar.getPrivateStoreType());
      this.writeC(this._activeChar.hasDwarvenCraft() ? 1 : 0);
      this.writeD(this._activeChar.getPkKills());
      this.writeD(this._activeChar.getPvpKills());
      this.writeH(this._activeChar.getRecommendation().getRecomLeft());
      this.writeH(this._activeChar.getRecommendation().getRecomHave());
      this.writeD(this._activeChar.getClassId().getId());
      this.writeD(0);
      this.writeD((int)this._activeChar.getMaxCp());
      this.writeD((int)this._activeChar.getCurrentCp());
      this.writeC(this._activeChar.isRunning() ? 1 : 0);
      this.writeC(321);
      this.writeD(this._activeChar.getPledgeClass());
      this.writeC(this._activeChar.isNoble() ? 1 : 0);
      this.writeC(this._activeChar.isHero() ? 1 : 0);
      this.writeD(this._activeChar.getAppearance().getNameColor());
      this.writeD(this._activeChar.getAppearance().getTitleColor());
      byte attackAttribute = this._activeChar.getAttackElement();
      this.writeH(attackAttribute);
      this.writeH(this._activeChar.getAttackElementValue(attackAttribute));
      this.writeH(this._activeChar.getDefenseElementValue((byte)0));
      this.writeH(this._activeChar.getDefenseElementValue((byte)1));
      this.writeH(this._activeChar.getDefenseElementValue((byte)2));
      this.writeH(this._activeChar.getDefenseElementValue((byte)3));
      this.writeH(this._activeChar.getDefenseElementValue((byte)4));
      this.writeH(this._activeChar.getDefenseElementValue((byte)5));
      this.writeD(this._activeChar.getFame());
      this.writeD(this._activeChar.getVitalityPoints());
   }
}
