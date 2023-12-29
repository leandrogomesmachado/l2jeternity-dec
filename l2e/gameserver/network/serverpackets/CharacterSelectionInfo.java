package l2e.gameserver.network.serverpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import l2e.gameserver.Config;
import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.data.parser.ExperienceParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.CharSelectInfoPackage;
import l2e.gameserver.model.Clan;
import l2e.gameserver.network.GameClient;

public class CharacterSelectionInfo extends GameServerPacket {
   private final String _loginName;
   private final int _sessionId;
   private int _activeId;
   private final CharSelectInfoPackage[] _characterPackages;

   public CharacterSelectionInfo(String loginName, int sessionId) {
      this._sessionId = sessionId;
      this._loginName = loginName;
      this._characterPackages = loadCharacterSelectInfo(this._loginName);
      this._activeId = -1;
   }

   public CharacterSelectionInfo(String loginName, int sessionId, int activeId) {
      this._sessionId = sessionId;
      this._loginName = loginName;
      this._characterPackages = loadCharacterSelectInfo(this._loginName);
      this._activeId = activeId;
   }

   public CharSelectInfoPackage[] getCharInfo() {
      return this._characterPackages;
   }

   @Override
   protected final void writeImpl() {
      int size = this._characterPackages.length;
      this.writeD(size);
      this.writeD(Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT);
      this.writeC(0);
      long lastAccess = 0L;
      if (this._activeId == -1) {
         for(int i = 0; i < size; ++i) {
            if (lastAccess < this._characterPackages[i].getLastAccess()) {
               lastAccess = this._characterPackages[i].getLastAccess();
               this._activeId = i;
            }
         }
      }

      for(int i = 0; i < size; ++i) {
         CharSelectInfoPackage charInfoPackage = this._characterPackages[i];
         this.writeS(charInfoPackage.getName());
         this.writeD(charInfoPackage.getObjectId());
         this.writeS(this._loginName);
         this.writeD(this._sessionId);
         this.writeD(charInfoPackage.getClanId());
         this.writeD(0);
         this.writeD(charInfoPackage.getSex());
         this.writeD(charInfoPackage.getRace());
         if (charInfoPackage.getClassId() == charInfoPackage.getBaseClassId()) {
            this.writeD(charInfoPackage.getClassId());
         } else {
            this.writeD(charInfoPackage.getBaseClassId());
         }

         this.writeD(1);
         this.writeD(charInfoPackage.getX());
         this.writeD(charInfoPackage.getY());
         this.writeD(charInfoPackage.getZ());
         this.writeF(charInfoPackage.getCurrentHp());
         this.writeF(charInfoPackage.getCurrentMp());
         this.writeD(charInfoPackage.getSp());
         this.writeQ(charInfoPackage.getExp());
         this.writeF(
            (double)(
               (float)(charInfoPackage.getExp() - ExperienceParser.getInstance().getExpForLevel(charInfoPackage.getLevel()))
                  / (float)(
                     ExperienceParser.getInstance().getExpForLevel(charInfoPackage.getLevel() + 1)
                        - ExperienceParser.getInstance().getExpForLevel(charInfoPackage.getLevel())
                  )
            )
         );
         this.writeD(charInfoPackage.getLevel());
         this.writeD(charInfoPackage.getKarma());
         this.writeD(charInfoPackage.getPkKills());
         this.writeD(charInfoPackage.getPvPKills());
         this.writeD(0);
         this.writeD(0);
         this.writeD(0);
         this.writeD(0);
         this.writeD(0);
         this.writeD(0);
         this.writeD(0);
         this.writeD(charInfoPackage.getPaperdollItemId(2));
         this.writeD(charInfoPackage.getPaperdollItemId(8));
         this.writeD(charInfoPackage.getPaperdollItemId(9));
         this.writeD(charInfoPackage.getPaperdollItemId(4));
         this.writeD(charInfoPackage.getPaperdollItemId(13));
         this.writeD(charInfoPackage.getPaperdollItemId(14));
         this.writeD(charInfoPackage.getPaperdollItemId(1));
         this.writeD(charInfoPackage.getPaperdollItemId(5));
         this.writeD(charInfoPackage.getPaperdollItemId(7));
         this.writeD(charInfoPackage.getPaperdollItemId(10));
         this.writeD(charInfoPackage.getPaperdollItemId(6));
         this.writeD(charInfoPackage.getPaperdollItemId(11));
         this.writeD(charInfoPackage.getPaperdollItemId(12));
         this.writeD(charInfoPackage.getPaperdollItemId(23));
         this.writeD(charInfoPackage.getPaperdollItemId(5));
         this.writeD(charInfoPackage.getPaperdollItemId(2));
         this.writeD(charInfoPackage.getPaperdollItemId(3));
         this.writeD(charInfoPackage.getPaperdollItemId(16));
         this.writeD(charInfoPackage.getPaperdollItemId(15));
         this.writeD(charInfoPackage.getPaperdollItemId(17));
         this.writeD(charInfoPackage.getPaperdollItemId(18));
         this.writeD(charInfoPackage.getPaperdollItemId(19));
         this.writeD(charInfoPackage.getPaperdollItemId(20));
         this.writeD(charInfoPackage.getPaperdollItemId(21));
         this.writeD(charInfoPackage.getPaperdollItemId(22));
         this.writeD(charInfoPackage.getPaperdollItemId(24));
         this.writeD(charInfoPackage.getHairStyle());
         this.writeD(charInfoPackage.getHairColor());
         this.writeD(charInfoPackage.getFace());
         this.writeF((double)charInfoPackage.getMaxHp());
         this.writeF((double)charInfoPackage.getMaxMp());
         long deleteTime = charInfoPackage.getDeleteTimer();
         int deletedays = 0;
         if (deleteTime > 0L) {
            deletedays = (int)((deleteTime - System.currentTimeMillis()) / 1000L);
         }

         this.writeD(deletedays);
         this.writeD(charInfoPackage.getClassId());
         this.writeD(i == this._activeId ? 1 : 0);
         this.writeC(charInfoPackage.getEnchantEffect() > 127 ? 127 : charInfoPackage.getEnchantEffect());
         this.writeH(0);
         this.writeH(0);
         this.writeD(0);
         this.writeD(0);
         this.writeD(0);
         this.writeD(0);
         this.writeD(0);
         this.writeF(0.0);
         this.writeF(0.0);
         this.writeD(charInfoPackage.getVitalityPoints());
      }
   }

   private static CharSelectInfoPackage[] loadCharacterSelectInfo(String loginName) {
      List<CharSelectInfoPackage> characterList = new ArrayList<>();

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT * FROM characters WHERE account_name=?");
      ) {
         statement.setString(1, loginName);

         try (ResultSet charList = statement.executeQuery()) {
            while(charList.next()) {
               CharSelectInfoPackage charInfopackage = restoreChar(charList);
               if (charInfopackage != null) {
                  characterList.add(charInfopackage);
               }
            }
         }

         return characterList.toArray(new CharSelectInfoPackage[characterList.size()]);
      } catch (Exception var61) {
         _log.log(Level.WARNING, "Could not restore char info: " + var61.getMessage(), (Throwable)var61);
         return new CharSelectInfoPackage[0];
      }
   }

   private static void loadCharacterSubclassInfo(CharSelectInfoPackage charInfopackage, int ObjectId, int activeClassId) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT exp, sp, level FROM character_subclasses WHERE charId=? && class_id=? ORDER BY charId");
      ) {
         statement.setInt(1, ObjectId);
         statement.setInt(2, activeClassId);

         try (ResultSet charList = statement.executeQuery()) {
            if (charList.next()) {
               charInfopackage.setExp(charList.getLong("exp"));
               charInfopackage.setSp(charList.getInt("sp"));
               charInfopackage.setLevel(charList.getInt("level"));
            }
         }
      } catch (Exception var61) {
         _log.log(Level.WARNING, "Could not restore char subclass info: " + var61.getMessage(), (Throwable)var61);
      }
   }

   private static CharSelectInfoPackage restoreChar(ResultSet chardata) throws Exception {
      int objectId = chardata.getInt("charId");
      String name = chardata.getString("char_name");
      long deletetime = chardata.getLong("deletetime");
      if (deletetime > 0L && System.currentTimeMillis() > deletetime) {
         Clan clan = ClanHolder.getInstance().getClan(chardata.getInt("clanid"));
         if (clan != null) {
            clan.removeClanMember(objectId, 0L);
         }

         GameClient.deleteCharByObjId(objectId);
         return null;
      } else {
         CharSelectInfoPackage charInfopackage = new CharSelectInfoPackage(objectId, name);
         charInfopackage.setAccessLevel(chardata.getInt("accesslevel"));
         charInfopackage.setLevel(chardata.getInt("level"));
         charInfopackage.setMaxHp(chardata.getInt("maxhp"));
         charInfopackage.setCurrentHp(chardata.getDouble("curhp"));
         charInfopackage.setMaxMp(chardata.getInt("maxmp"));
         charInfopackage.setCurrentMp(chardata.getDouble("curmp"));
         charInfopackage.setKarma(chardata.getInt("karma"));
         charInfopackage.setPkKills(chardata.getInt("pkkills"));
         charInfopackage.setPvPKills(chardata.getInt("pvpkills"));
         charInfopackage.setFace(chardata.getInt("face"));
         charInfopackage.setHairStyle(chardata.getInt("hairstyle"));
         charInfopackage.setHairColor(chardata.getInt("haircolor"));
         charInfopackage.setSex(chardata.getInt("sex"));
         charInfopackage.setExp(chardata.getLong("exp"));
         charInfopackage.setSp(chardata.getInt("sp"));
         charInfopackage.setVitalityPoints(chardata.getInt("vitality_points"));
         charInfopackage.setClanId(chardata.getInt("clanid"));
         charInfopackage.setRace(chardata.getInt("race"));
         int baseClassId = chardata.getInt("base_class");
         int activeClassId = chardata.getInt("classid");
         charInfopackage.setX(chardata.getInt("x"));
         charInfopackage.setY(chardata.getInt("y"));
         charInfopackage.setZ(chardata.getInt("z"));
         if (baseClassId != activeClassId) {
            loadCharacterSubclassInfo(charInfopackage, objectId, activeClassId);
         }

         charInfopackage.setClassId(activeClassId);
         int weaponObjId = charInfopackage.getPaperdollObjectId(5);
         if (weaponObjId < 1) {
            weaponObjId = charInfopackage.getPaperdollObjectId(5);
         }

         if (weaponObjId > 0) {
            try (
               Connection con = DatabaseFactory.getInstance().getConnection();
               PreparedStatement statement = con.prepareStatement("SELECT augAttributes FROM item_attributes WHERE itemId=?");
            ) {
               statement.setInt(1, weaponObjId);

               try (ResultSet result = statement.executeQuery()) {
                  if (result.next()) {
                     int augment = result.getInt("augAttributes");
                     charInfopackage.setAugmentationId(augment == -1 ? 0 : augment);
                  }
               }
            } catch (Exception var67) {
               _log.log(Level.WARNING, "Could not restore augmentation info: " + var67.getMessage(), (Throwable)var67);
            }
         }

         if (baseClassId == 0 && activeClassId > 0) {
            charInfopackage.setBaseClassId(activeClassId);
         } else {
            charInfopackage.setBaseClassId(baseClassId);
         }

         charInfopackage.setDeleteTimer(deletetime);
         charInfopackage.setLastAccess(chardata.getLong("lastAccess"));
         return charInfopackage;
      }
   }
}
