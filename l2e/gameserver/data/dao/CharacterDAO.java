package l2e.gameserver.data.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.data.holder.CharSchemesHolder;
import l2e.gameserver.data.holder.CharSummonHolder;
import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.data.holder.FacebookProfilesHolder;
import l2e.gameserver.data.parser.CharTemplateParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.instancemanager.CursedWeaponsManager;
import l2e.gameserver.model.ClanMember;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.TradeItem;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.appearance.PcAppearance;
import l2e.gameserver.model.actor.templates.player.PcTemplate;
import l2e.gameserver.model.base.SubClass;
import l2e.gameserver.model.entity.Hero;
import l2e.gameserver.model.entity.auction.Auction;
import l2e.gameserver.model.entity.auction.AuctionsManager;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.skills.effects.EffectType;

public class CharacterDAO {
   private static final Logger _log = Logger.getLogger(CharacterDAO.class.getName());
   private static final String INSERT_CHARACTER = "INSERT INTO characters (account_name,charId,char_name,level,maxHp,curHp,maxCp,curCp,maxMp,curMp,face,hairStyle,hairColor,sex,exp,sp,karma,fame,pvpkills,pkkills,clanid,race,classid,deletetime,title,accesslevel,online,isin7sdungeon,clan_privs,wantspeace,base_class,newbie,nobless,power_grade,createDate) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
   private static final String UPDATE_CHARACTER = "UPDATE characters SET level=?,maxHp=?,curHp=?,maxCp=?,curCp=?,maxMp=?,curMp=?,face=?,hairStyle=?,hairColor=?,sex=?,heading=?,x=?,y=?,z=?,exp=?,expBeforeDeath=?,sp=?,karma=?,fame=?,pvpkills=?,pkkills=?,clanid=?,race=?,classid=?,deletetime=?,title=?,accesslevel=?,online=?,isin7sdungeon=?,clan_privs=?,wantspeace=?,base_class=?,onlinetime=?,newbie=?,nobless=?,power_grade=?,subpledge=?,lvl_joined_academy=?,apprentice=?,sponsor=?,clan_join_expiry_time=?,clan_create_expiry_time=?,char_name=?,death_penalty_level=?,bookmarkslot=?,rec_have=?,rec_left=?,rec_bonus_time=?,hunt_points=?,hunt_time=?,vitality_points=?,pccafe_points=?,language=?,hitman_target=?,game_points=?,botRating=?,chatMsg=?,facebook_id=? WHERE charId=?";
   private static final String RESTORE_CHARACTER = "SELECT * FROM characters WHERE charId=?";
   private static final String RESTORE_CHAR_SUBCLASSES = "SELECT class_id,exp,sp,level,class_index FROM character_subclasses WHERE charId=? ORDER BY class_index ASC";
   private static final String ADD_CHAR_SUBCLASS = "INSERT INTO character_subclasses (charId,class_id,exp,sp,level,class_index) VALUES (?,?,?,?,?,?)";
   private static final String UPDATE_CHAR_SUBCLASS = "UPDATE character_subclasses SET exp=?,sp=?,level=?,class_id=? WHERE charId=? AND class_index =?";
   private static CharacterDAO _instance = new CharacterDAO();

   public void storeSubClasses(Player player) {
      if (player.getTotalSubClasses() > 0) {
         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement(
               "UPDATE character_subclasses SET exp=?,sp=?,level=?,class_id=? WHERE charId=? AND class_index =?"
            );
         ) {
            for(SubClass subClass : player.getSubClasses().values()) {
               statement.setLong(1, subClass.getExp());
               statement.setInt(2, subClass.getSp());
               statement.setInt(3, subClass.getLevel());
               statement.setInt(4, subClass.getClassId());
               statement.setInt(5, player.getObjectId());
               statement.setInt(6, subClass.getClassIndex());
               statement.execute();
               statement.clearParameters();
            }
         } catch (Exception var35) {
            _log.log(Level.WARNING, "Could not store sub class data for " + player.getName() + ": " + var35.getMessage(), (Throwable)var35);
         }
      }
   }

   public boolean addSubClass(Player player, int classId, int classIndex) {
      SubClass newClass = new SubClass();
      newClass.setClassId(classId);
      newClass.setClassIndex(classIndex);

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("INSERT INTO character_subclasses (charId,class_id,exp,sp,level,class_index) VALUES (?,?,?,?,?,?)");
      ) {
         statement.setInt(1, player.getObjectId());
         statement.setInt(2, newClass.getClassId());
         statement.setLong(3, newClass.getExp());
         statement.setInt(4, newClass.getSp());
         statement.setInt(5, newClass.getLevel());
         statement.setInt(6, newClass.getClassIndex());
         statement.execute();
      } catch (Exception var37) {
         _log.log(Level.WARNING, "WARNING: Could not add character sub class for " + player.getName() + ": " + var37.getMessage(), (Throwable)var37);
         return false;
      }

      player.getSubClasses().put(newClass.getClassIndex(), newClass);
      return true;
   }

   public boolean isPlayerCreated(Player player) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(
            "INSERT INTO characters (account_name,charId,char_name,level,maxHp,curHp,maxCp,curCp,maxMp,curMp,face,hairStyle,hairColor,sex,exp,sp,karma,fame,pvpkills,pkkills,clanid,race,classid,deletetime,title,accesslevel,online,isin7sdungeon,clan_privs,wantspeace,base_class,newbie,nobless,power_grade,createDate) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
         );
      ) {
         statement.setString(1, player.getAccountNamePlayer());
         statement.setInt(2, player.getObjectId());
         statement.setString(3, player.getName());
         statement.setInt(4, player.getLevel());
         statement.setDouble(5, player.getMaxHp());
         statement.setDouble(6, player.getCurrentHp());
         statement.setDouble(7, player.getMaxCp());
         statement.setDouble(8, player.getCurrentCp());
         statement.setDouble(9, player.getMaxMp());
         statement.setDouble(10, player.getCurrentMp());
         statement.setInt(11, player.getAppearance().getFace());
         statement.setInt(12, player.getAppearance().getHairStyle());
         statement.setInt(13, player.getAppearance().getHairColor());
         statement.setInt(14, player.getAppearance().getSex() ? 1 : 0);
         statement.setLong(15, player.getExp());
         statement.setInt(16, player.getSp());
         statement.setInt(17, player.getKarma());
         statement.setInt(18, player.getFame());
         statement.setInt(19, player.getPvpKills());
         statement.setInt(20, player.getPkKills());
         statement.setInt(21, player.getClanId());
         statement.setInt(22, player.getRace().ordinal());
         statement.setInt(23, player.getClassId().getId());
         statement.setLong(24, player.getDeleteTimer());
         statement.setString(25, player.getTitle());
         statement.setInt(26, player.getAccessLevel().getLevel());
         statement.setInt(27, player.isOnlineInt());
         statement.setInt(28, player.isIn7sDungeon() ? 1 : 0);
         statement.setInt(29, player.getClanPrivileges());
         statement.setInt(30, player.getWantsPeace());
         statement.setInt(31, player.getBaseClass());
         statement.setInt(32, player.getNewbie());
         statement.setInt(33, player.isNoble() ? 1 : 0);
         statement.setLong(34, 0L);
         statement.setDate(35, new Date(player.getCreateDate().getTimeInMillis()));
         statement.executeUpdate();
         return true;
      } catch (Exception var34) {
         _log.log(Level.SEVERE, "Could not insert char data: " + var34.getMessage(), (Throwable)var34);
         return false;
      }
   }

   public void storePlayer(Player player) {
      long exp = player.getStat().getBaseExp();
      int level = player.getStat().getBaseLevel();
      int sp = player.getStat().getBaseSp();

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(
            "UPDATE characters SET level=?,maxHp=?,curHp=?,maxCp=?,curCp=?,maxMp=?,curMp=?,face=?,hairStyle=?,hairColor=?,sex=?,heading=?,x=?,y=?,z=?,exp=?,expBeforeDeath=?,sp=?,karma=?,fame=?,pvpkills=?,pkkills=?,clanid=?,race=?,classid=?,deletetime=?,title=?,accesslevel=?,online=?,isin7sdungeon=?,clan_privs=?,wantspeace=?,base_class=?,onlinetime=?,newbie=?,nobless=?,power_grade=?,subpledge=?,lvl_joined_academy=?,apprentice=?,sponsor=?,clan_join_expiry_time=?,clan_create_expiry_time=?,char_name=?,death_penalty_level=?,bookmarkslot=?,rec_have=?,rec_left=?,rec_bonus_time=?,hunt_points=?,hunt_time=?,vitality_points=?,pccafe_points=?,language=?,hitman_target=?,game_points=?,botRating=?,chatMsg=?,facebook_id=? WHERE charId=?"
         );
      ) {
         statement.setInt(1, level);
         statement.setDouble(2, player.getMaxHp());
         statement.setDouble(3, player.getCurrentHp());
         statement.setDouble(4, player.getMaxCp());
         statement.setDouble(5, player.getCurrentCp());
         statement.setDouble(6, player.getMaxMp());
         statement.setDouble(7, player.getCurrentMp());
         statement.setInt(8, player.getAppearance().getFace());
         statement.setInt(9, player.getAppearance().getHairStyle());
         statement.setInt(10, player.getAppearance().getHairColor());
         statement.setInt(11, player.getAppearance().getSex() ? 1 : 0);
         statement.setInt(12, player.getHeading());
         statement.setInt(13, player.inObserverMode() ? player.getLastX() : player.getX());
         statement.setInt(14, player.inObserverMode() ? player.getLastY() : player.getY());
         statement.setInt(15, player.inObserverMode() ? player.getLastZ() : player.getZ());
         statement.setLong(16, exp);
         statement.setLong(17, player.getExpBeforeDeath());
         statement.setInt(18, sp);
         statement.setInt(19, player.getKarma());
         statement.setInt(20, player.getFame());
         statement.setInt(21, player.getPvpKills());
         statement.setInt(22, player.getPkKills());
         statement.setInt(23, player.getClanId());
         statement.setInt(24, player.getRace().ordinal());
         statement.setInt(25, player.getClassId().getId());
         statement.setLong(26, player.getDeleteTimer());
         statement.setString(27, player.getTitle());
         statement.setInt(28, player.getAccessLevel().getLevel());
         statement.setInt(29, player.isOnlineInt());
         statement.setInt(30, player.isIn7sDungeon() ? 1 : 0);
         statement.setInt(31, player.getClanPrivileges());
         statement.setInt(32, player.getWantsPeace());
         statement.setInt(33, player.getBaseClass());
         long totalOnlineTime = player.getOnlineTime();
         if (player.getOnlineBeginTime() > 0L) {
            totalOnlineTime += (System.currentTimeMillis() - player.getOnlineBeginTime()) / 1000L;
         }

         statement.setLong(34, totalOnlineTime);
         statement.setInt(35, player.getNewbie());
         statement.setInt(36, player.isNoble() ? 1 : 0);
         statement.setInt(37, player.getPowerGrade());
         statement.setInt(38, player.getPledgeType());
         statement.setInt(39, player.getLvlJoinedAcademy());
         statement.setLong(40, (long)player.getApprentice());
         statement.setLong(41, (long)player.getSponsor());
         statement.setLong(42, player.getClanJoinExpiryTime());
         statement.setLong(43, player.getClanCreateExpiryTime());
         statement.setString(44, player.getName());
         statement.setLong(45, (long)player.getDeathPenaltyBuffLevel());
         statement.setInt(46, player.getBookMarkSlot());
         statement.setInt(47, player.getRecommendation().getRecomHave());
         statement.setInt(48, player.getRecommendation().getRecomLeft());
         statement.setInt(49, player.getRecommendation().getRecomTimeLeft());
         statement.setInt(50, player.getNevitSystem().getPoints());
         statement.setInt(51, player.getNevitSystem().getTime());
         statement.setInt(52, player.getVitalityPoints());
         statement.setInt(53, player.getPcBangPoints());
         statement.setString(54, player.getLang());
         statement.setString(55, player.saveHitmanTargets());
         statement.setLong(56, player.getGamePoints());
         statement.setInt(57, player.getBotRating());
         statement.setInt(58, player.getChatMsg());
         statement.setString(59, player.hasFacebookProfile() ? player.getFacebookProfile().getId() : "");
         statement.setInt(60, player.getObjectId());
         statement.execute();
         player.saveAchivements();
      } catch (Exception var39) {
         _log.log(Level.WARNING, "Could not store char base data: " + this + " - " + var39.getMessage(), (Throwable)var39);
      }
   }

   public static Player restore(int objectId) {
      Player player = null;
      double currentCp = 0.0;
      double currentHp = 0.0;
      double currentMp = 0.0;

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT * FROM characters WHERE charId=?");
      ) {
         statement.setInt(1, objectId);

         try (ResultSet rset = statement.executeQuery()) {
            if (rset.next()) {
               int activeClassId = rset.getInt("classid");
               boolean female = rset.getInt("sex") != 0;
               PcTemplate template = CharTemplateParser.getInstance().getTemplate(activeClassId);
               PcAppearance app = new PcAppearance(rset.getByte("face"), rset.getByte("hairColor"), rset.getByte("hairStyle"), female);
               player = new Player(objectId, template, rset.getString("account_name"), app);
               player.restoreVariables();
               player.setName(rset.getString("char_name"));
               player.setLastAccess(rset.getLong("lastAccess"));
               player.getStat().setExp(rset.getLong("exp"));
               player.setExpBeforeDeath(rset.getLong("expBeforeDeath"));
               player.getStat().setLevel(rset.getByte("level"));
               player.getStat().setSp(rset.getInt("sp"));
               player.setWantsPeace(rset.getInt("wantspeace"));
               player.setHeading(rset.getInt("heading"));
               player.setKarma(rset.getInt("karma"));
               player.setFame(rset.getInt("fame"));
               player.setPvpKills(rset.getInt("pvpkills"));
               player.setPkKills(rset.getInt("pkkills"));
               player.setOnlineTime(rset.getLong("onlinetime"));
               player.setNewbie(rset.getInt("newbie"));
               player.setNoble(rset.getInt("nobless") == 1);
               player.setClanJoinExpiryTime(rset.getLong("clan_join_expiry_time"));
               if (player.getClanJoinExpiryTime() < System.currentTimeMillis()) {
                  player.setClanJoinExpiryTime(0L);
               }

               player.setClanCreateExpiryTime(rset.getLong("clan_create_expiry_time"));
               if (player.getClanCreateExpiryTime() < System.currentTimeMillis()) {
                  player.setClanCreateExpiryTime(0L);
               }

               int clanId = rset.getInt("clanid");
               player.setPowerGrade(rset.getInt("power_grade"));
               player.setPledgeType(rset.getInt("subpledge"));
               if (clanId > 0) {
                  player.setClan(ClanHolder.getInstance().getClan(clanId));
               }

               if (player.getClan() != null) {
                  if (player.getClan().getLeaderId() != player.getObjectId()) {
                     if (player.getPowerGrade() == 0) {
                        player.setPowerGrade(5);
                     }

                     player.setClanPrivileges(player.getClan().getRankPrivs(player.getPowerGrade()));
                  } else {
                     player.setClanPrivileges(16777214);
                     player.setPowerGrade(1);
                  }

                  player.setPledgeClass(ClanMember.calculatePledgeClass(player));
               } else {
                  if (player.isNoble()) {
                     player.setPledgeClass(5);
                  }

                  if (player.isHero()) {
                     player.setPledgeClass(8);
                  }

                  player.setClanPrivileges(0);
               }

               player.setDeleteTimer(rset.getLong("deletetime"));
               player.setTitle(rset.getString("title"));
               player.setAccessLevel(rset.getInt("accesslevel"));
               if (player.getVar("namecolor") != null) {
                  player.getAppearance().setNameColor(Integer.parseInt(player.getVar("namecolor")));
               }

               if (player.getVar("titlecolor") != null) {
                  player.getAppearance().setTitleColor(Integer.parseInt(player.getVar("titlecolor")));
               } else {
                  player.getAppearance().setTitleColor(16777079);
               }

               player.setFistsWeaponItem(player.findFistsWeaponItem(activeClassId));
               player.setUptime(System.currentTimeMillis());
               currentHp = rset.getDouble("curHp");
               currentCp = rset.getDouble("curCp");
               currentMp = rset.getDouble("curMp");
               player.setClassIndex(0);

               try {
                  player.setBaseClass(rset.getInt("base_class"));
               } catch (Exception var142) {
                  player.setBaseClass(activeClassId);
               }

               if (restoreSubClassData(player) && activeClassId != player.getBaseClass()) {
                  for(SubClass subClass : player.getSubClasses().values()) {
                     if (subClass.getClassId() == activeClassId) {
                        player.setClassIndex(subClass.getClassIndex());
                     }
                  }
               }

               if (player.getClassIndex() == 0 && activeClassId != player.getBaseClass()) {
                  player.setClassId(player.getBaseClass());
                  _log.warning("Player " + player.getName() + " reverted to base class. Possibly has tried a relogin exploit while subclassing.");
               } else {
                  player.setActiveClassId(activeClassId);
               }

               player.setApprentice(rset.getInt("apprentice"));
               player.setSponsor(rset.getInt("sponsor"));
               player.setLvlJoinedAcademy(rset.getInt("lvl_joined_academy"));
               player.setIsIn7sDungeon(rset.getInt("isin7sdungeon") == 1);
               CursedWeaponsManager.getInstance().checkPlayer(player);
               player.setDeathPenaltyBuffLevel(rset.getInt("death_penalty_level"));
               player.getRecommendation().setRecomHave(rset.getInt("rec_have"));
               player.getRecommendation().setRecomLeft(rset.getInt("rec_left"));
               player.getRecommendation().setRecomTimeLeft(rset.getInt("rec_bonus_time"));
               player.getNevitSystem().setPoints(rset.getInt("hunt_points"), rset.getInt("hunt_time"));
               player.setVitalityPoints(rset.getInt("vitality_points"), true);
               player.setPcBangPoints(rset.getInt("pccafe_points"));
               player.setX(rset.getInt("x"));
               player.setY(rset.getInt("y"));
               player.setZ(rset.getInt("z"));
               player.setBookMarkSlot(rset.getInt("BookmarkSlot"));
               player.getCreateDate().setTime(rset.getDate("createDate"));
               player.setGamePoints(rset.getLong("game_points"));
               player.setBotRating(rset.getInt("botRating"));
               player.setLang(rset.getString("language"));
               player.loadHitmanTargets(rset.getString("hitman_target"));
               player.setChatMsg(rset.getInt("chatMsg"));
               player.setFacebookProfile(FacebookProfilesHolder.getInstance().getProfileById(rset.getString("facebook_id")));
               CharSchemesHolder.getInstance().loadSchemes(player, con);

               try (PreparedStatement stmt = con.prepareStatement("SELECT charId, char_name FROM characters WHERE account_name=? AND charId<>?")) {
                  stmt.setString(1, player.getAccountNamePlayer());
                  stmt.setInt(2, objectId);

                  try (ResultSet chars = stmt.executeQuery()) {
                     while(chars.next()) {
                        player.getAccountChars().put(chars.getInt("charId"), chars.getString("char_name"));
                     }
                  }
               }
            }
         }

         if (player == null) {
            return null;
         }

         if (Hero.getInstance().isHero(objectId)) {
            player.setHero(true, true);
         }

         player.getInventory().restore();
         player.getFreight().restore();
         if (!Config.WAREHOUSE_CACHE) {
            player.getWarehouse();
         }

         CharacterItemReuseDAO.getInstance().restore(player);
         player.restoreCharData();
         player.rewardSkills();
         player.setCurrentCp(currentCp);
         player.setCurrentHp(currentHp);
         player.setCurrentMp(currentMp);
         if (currentHp < 0.5) {
            player.setIsDead(true);
            player.stopHpMpRegeneration();
         }

         player.setPet(World.getInstance().getPet(player.getObjectId()));
         if (player.hasSummon()) {
            player.getSummon().setOwner(player);
         }

         player.refreshOverloaded();
         player.refreshExpertisePenalty();
         player.restoreFriendList();
         if (Config.STORE_UI_SETTINGS) {
            player.restoreUISettings();
         }

         if (player.isGM() && player.getVar("cond_override") != null) {
            player.setOverrideCond(player.getVarLong("cond_override", PcCondOverride.getAllExceptionsMask()));
         }

         if (Config.ALLOW_REVENGE_SYSTEM && player.getVar("revengeList") != null) {
            String var = player.getVar("revengeList", "");
            if (var != null && !var.isEmpty()) {
               player.loadRevergeList(var);
            }
         }

         player.restoreTradeList();
         if (player.getVar("storemode") != null) {
            player.setPrivateStoreType(Integer.parseInt(player.getVar("storemode")));
            player.setIsInStoreNow(true);
            player.sitDown();
            if (player.getVar("offlineBuff") != null) {
               if (player.getVar("sellstorename") != null) {
                  player.getSellList().setTitle(player.getVar("sellstorename"));
               }

               CharacterSellBuffsDAO.getInstance().restoreSellBuffList(player);
            } else if (Config.AUCTION_PRIVATE_STORE_AUTO_ADDED && player.getPrivateStoreType() == 1) {
               if (player.getVar("offline") != null) {
                  AuctionsManager.getInstance().removePlayerStores(player);
               }

               for(TradeItem item : player.getSellList().getItems()) {
                  ItemInstance itemToSell = player.getInventory().getItemByItemId(item.getItem().getId());
                  Auction a = AuctionsManager.getInstance().addNewStore(player, itemToSell, 57, item.getPrice(), item.getCount());
                  item.setAuctionId(a.getAuctionId());
               }
            }
         }

         if (Config.RESTORE_SERVITOR_ON_RECONNECT && !player.hasSummon() && CharSummonHolder.getInstance().getServitors().containsKey(player.getObjectId())) {
            CharSummonHolder.getInstance().restoreServitor(player);
         }

         if (Config.RESTORE_PET_ON_RECONNECT && !player.hasSummon() && CharSummonHolder.getInstance().getPets().containsKey(player.getObjectId())) {
            CharSummonHolder.getInstance().restorePet(player);
         }

         if (Config.USE_PREMIUMSERVICE) {
            CharacterPremiumDAO.getInstance().restore(player);
         }

         if (Config.ALLOW_VIP_SYSTEM) {
            player.setVipLevel(Integer.parseInt(player.getVar("vipLevel", "0")));
            player.setVipPoints(Long.parseLong(player.getVar("vipPoints", "0")));
         }

         player.getFarmSystem().checkFarmTask();
         player.loadAchivements();
         player.getRecommendation().checkRecom();
         player.checkChatMessages();
         player.isntAfk();
         CharacterVisualDAO.getInstance().restore(player);
         CharacterCBTeleportDAO.getInstance().restore(player);
         if (player.getVar("tempHero") != null) {
            player.setHero(true, false);
            player.startTempHeroTask(Long.parseLong(player.getVar("tempHero")));
         }

         if (player.getVarInt("visualBuff", 0) > 0 && player.getFirstEffect(EffectType.VISUAL_SKIN) == null) {
            player.setVar("visualBuff", 0);
         }
      } catch (Exception var153) {
         _log.log(Level.SEVERE, "Failed loading character.", (Throwable)var153);
      }

      return player;
   }

   private static boolean restoreSubClassData(Player player) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(
            "SELECT class_id,exp,sp,level,class_index FROM character_subclasses WHERE charId=? ORDER BY class_index ASC"
         );
      ) {
         statement.setInt(1, player.getObjectId());

         try (ResultSet rset = statement.executeQuery()) {
            while(rset.next()) {
               SubClass subClass = new SubClass();
               subClass.setClassId(rset.getInt("class_id"));
               subClass.setLevel(rset.getByte("level"));
               subClass.setExp(rset.getLong("exp"));
               subClass.setSp(rset.getInt("sp"));
               subClass.setClassIndex(rset.getInt("class_index"));
               player.getSubClasses().put(subClass.getClassIndex(), subClass);
            }
         }
      } catch (Exception var59) {
         _log.log(Level.WARNING, "Could not restore classes for " + player.getName() + ": " + var59.getMessage(), (Throwable)var59);
      }

      return true;
   }

   public boolean modifySubClass(Player player, int classIndex, int newClassId) {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("DELETE FROM character_hennas WHERE charId=? AND class_index=?");
         statement.setInt(1, player.getObjectId());
         statement.setInt(2, classIndex);
         statement.execute();
         statement.close();
         statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE charId=? AND class_index=?");
         statement.setInt(1, player.getObjectId());
         statement.setInt(2, classIndex);
         statement.execute();
         statement.close();
         statement = con.prepareStatement("DELETE FROM character_skills_save WHERE charId=? AND class_index=?");
         statement.setInt(1, player.getObjectId());
         statement.setInt(2, classIndex);
         statement.execute();
         statement.close();
         statement = con.prepareStatement("DELETE FROM character_skills WHERE charId=? AND class_index=?");
         statement.setInt(1, player.getObjectId());
         statement.setInt(2, classIndex);
         statement.execute();
         statement.close();
         statement = con.prepareStatement("DELETE FROM character_subclasses WHERE charId=? AND class_index=?");
         statement.setInt(1, player.getObjectId());
         statement.setInt(2, classIndex);
         statement.execute();
         statement.close();
         return true;
      } catch (Exception var17) {
         _log.log(
            Level.WARNING,
            "Could not modify sub class for " + player.getName() + " to class index " + classIndex + ": " + var17.getMessage(),
            (Throwable)var17
         );
         player.getSubClasses().remove(classIndex);
         return false;
      }
   }

   public void setFacebookId(int playerObjectId, String facebookId) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("UPDATE characters SET facebook_id = ? WHERE obj_Id = ?");
      ) {
         statement.setString(1, facebookId);
         statement.setInt(2, playerObjectId);
         statement.executeUpdate();
      } catch (SQLException var35) {
         _log.log(Level.SEVERE, "Error while setFacebookId(" + playerObjectId + ", " + facebookId + ")", (Throwable)var35);
      }
   }

   public static CharacterDAO getInstance() {
      return _instance;
   }
}
