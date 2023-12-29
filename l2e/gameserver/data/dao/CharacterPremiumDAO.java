package l2e.gameserver.data.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.PremiumAccountsParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.player.PremiumBonus;
import l2e.gameserver.model.service.premium.PremiumGift;
import l2e.gameserver.model.service.premium.PremiumTemplate;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExBrPremiumState;
import l2e.gameserver.network.serverpackets.MagicSkillUse;

public class CharacterPremiumDAO {
   private static final Logger _log = Logger.getLogger(CharacterPremiumDAO.class.getName());
   private static final CharacterPremiumDAO _instance = new CharacterPremiumDAO();
   private static final String SELECT_SQL_QUERY = "SELECT id,status,expireTime FROM character_premium WHERE account=?";
   private static final String INSERT_SQL_QUERY = "INSERT INTO character_premium (account,id,status,expireTime) values(?,?,?,?)";
   private static final String UPDATE_SQL_QUERY = "UPDATE character_premium SET id=?,status=?,expireTime=? WHERE account=?";
   private static final String UPDATE_TIME_SQL_QUERY = "UPDATE character_premium SET expireTime=? WHERE account=?";
   private static final String REPLACE_SQL_QUERY = "UPDATE character_premium SET id=?,status=?,expireTime=? WHERE account=?";
   private static final String SELECT_SQL_QUERY_PERSONAL = "SELECT id,status,expireTime FROM character_premium_personal WHERE charId=?";
   private static final String INSERT_SQL_QUERY_PERSONAL = "INSERT INTO character_premium_personal (charId,id,status,expireTime) values(?,?,?,?)";
   private static final String UPDATE_SQL_QUERY_PERSONAL = "UPDATE character_premium_personal SET id=?,status=?,expireTime=? WHERE charId=?";
   private static final String UPDATE_TIME_SQL_QUERY_PERSONAL = "UPDATE character_premium_personal SET expireTime=? WHERE charId=?";
   private static final String REPLACE_SQL_QUERY_PERSONAL = "UPDATE character_premium_personal SET id=?,status=?,expireTime=? WHERE charId=?";

   public static CharacterPremiumDAO getInstance() {
      return _instance;
   }

   private void insert(Player player) {
      if (Config.USE_PREMIUMSERVICE) {
         try (Connection con = DatabaseFactory.getInstance().getConnection()) {
            PreparedStatement statement = con.prepareStatement("INSERT INTO character_premium (account,id,status,expireTime) values(?,?,?,?)");
            statement.setString(1, player.getAccountName());
            statement.setInt(2, 0);
            statement.setInt(3, 0);
            statement.setLong(4, 0L);
            statement.executeUpdate();
            statement.close();
         } catch (Exception var15) {
            _log.log(Level.WARNING, "Could not insert char data: " + var15);
         }
      }
   }

   private void insertPersonal(Player player) {
      if (Config.USE_PREMIUMSERVICE) {
         try (Connection con = DatabaseFactory.getInstance().getConnection()) {
            PreparedStatement statement = con.prepareStatement("INSERT INTO character_premium_personal (charId,id,status,expireTime) values(?,?,?,?)");
            statement.setInt(1, player.getObjectId());
            statement.setInt(2, 0);
            statement.setInt(3, 0);
            statement.setLong(4, 0L);
            statement.executeUpdate();
            statement.close();
         } catch (Exception var15) {
            _log.log(Level.WARNING, "Could not insert char data: " + var15);
         }
      }
   }

   public void updateOnlineTime(Player player) {
      if (Config.USE_PREMIUMSERVICE) {
         long time = player.getPremiumBonus().getOnlineTime() + (System.currentTimeMillis() - player.getPremiumOnlineTime());

         try (Connection con = DatabaseFactory.getInstance().getConnection()) {
            PreparedStatement statement = con.prepareStatement("UPDATE character_premium SET expireTime=? WHERE account=?");
            statement.setLong(1, time);
            statement.setString(2, player.getAccountName());
            statement.execute();
            statement.close();
         } catch (SQLException var17) {
            _log.log(Level.WARNING, "Could not update data: " + var17);
         }
      }
   }

   public void updateOnlineTimePersonal(Player player) {
      if (Config.USE_PREMIUMSERVICE) {
         long time = player.getPremiumBonus().getOnlineTime() + (System.currentTimeMillis() - player.getPremiumOnlineTime());

         try (Connection con = DatabaseFactory.getInstance().getConnection()) {
            PreparedStatement statement = con.prepareStatement("UPDATE character_premium_personal SET expireTime=? WHERE charId=?");
            statement.setLong(1, time);
            statement.setInt(2, player.getObjectId());
            statement.execute();
            statement.close();
         } catch (SQLException var17) {
            _log.log(Level.WARNING, "Could not update data: " + var17);
         }
      }
   }

   public void update(Player player, int id, long time) {
      if (Config.USE_PREMIUMSERVICE) {
         try (Connection con = DatabaseFactory.getInstance().getConnection()) {
            PreparedStatement statement = con.prepareStatement("UPDATE character_premium SET id=?,status=?,expireTime=? WHERE account=?");
            statement.setInt(1, id);
            statement.setInt(2, 1);
            statement.setLong(3, time);
            statement.setString(4, player.getAccountName());
            statement.execute();
            statement.close();
         } catch (SQLException var18) {
            _log.log(Level.WARNING, "Could not update data: " + var18);
         }

         PremiumTemplate template = PremiumAccountsParser.getInstance().getPremiumTemplate(id);
         if (template != null) {
            PremiumBonus bonus = player.getPremiumBonus();
            bonus.setPremiumId(template.getId());
            bonus.setIsPersonal(template.isPersonal());
            bonus.setRateXp(template.getExp());
            bonus.setRateSp(template.getSp());
            bonus.setDropSiege(template.getEpaulette());
            bonus.setDropElementStones(template.getElementStones());
            bonus.setDropSealStones(template.getSealStones());
            bonus.setQuestRewardRate(template.getQuestReward());
            bonus.setQuestDropRate(template.getQuestDrop());
            bonus.setDropAdena(template.getAdena());
            bonus.setDropItems(template.getItems());
            bonus.setDropRaid(template.getDropRaids());
            bonus.setDropEpic(template.getDropEpics());
            bonus.setDropSpoil(template.getSpoil());
            bonus.setWeight(template.getWeight());
            bonus.setCraftChance(template.getCraftChance());
            bonus.setMasterWorkChance(template.getMasterWorkChance());
            bonus.setEnchantChance(template.getEnchantChance());
            bonus.setFishingRate(template.getFishing());
            bonus.setFameBonus(template.getFameBonus());
            bonus.setReflectionReduce(template.getReflectionReduce());
            bonus.setNobleStonesMinCount(template.getMinNobleStonesCount());
            bonus.setNobleStonesMaxCount(template.getMaxNobleStonesCount());
            bonus.setSealStonesMinCount(template.getMinSealStonesCount());
            bonus.setSealStonesMaxCount(template.getMaxSealStonesCount());
            bonus.setLifeStonesMinCount(template.getMinLifeStonesCount());
            bonus.setLifeStonesMaxCount(template.getMaxLifeStonesCount());
            bonus.setEnchantScrollsMinCount(template.getMinEnchantScrollsCount());
            bonus.setEnchantScrollsMaxCount(template.getMaxEnchantScrollsCount());
            bonus.setForgottenScrollsMinCount(template.getMinForgottenScrollsCount());
            bonus.setForgottenScrollsMaxCount(template.getMaxForgottenScrollsCount());
            bonus.setMaterialsMinCount(template.getMinMaterialsCount());
            bonus.setMaterialsMaxCount(template.getMaxMaterialsCount());
            bonus.setRepicesMinCount(template.getMinRepicesCount());
            bonus.setRepicesMaxCount(template.getMaxRepicesCount());
            bonus.setBeltsMinCount(template.getMinBeltsCount());
            bonus.setBeltsMaxCount(template.getMaxBeltsCount());
            bonus.setBraceletsMinCount(template.getMinBraceletsCount());
            bonus.setBraceletsMaxCount(template.getMaxBraceletsCount());
            bonus.setCloaksMinCount(template.getMinCloaksCount());
            bonus.setCloaksMaxCount(template.getMaxCloaksCount());
            bonus.setCodexMinCount(template.getMinCodexCount());
            bonus.setCodexMaxCount(template.getMaxCodexCount());
            bonus.setAttStonesMinCount(template.getMinAttStonesCount());
            bonus.setAttStonesMaxCount(template.getMaxAttStonesCount());
            bonus.setAttCrystalsMinCount(template.getMinAttCrystalsCount());
            bonus.setAttCrystalsMaxCount(template.getMaxAttCrystalsCount());
            bonus.setAttJewelsMinCount(template.getMinAttJewelsCount());
            bonus.setAttJewelsMaxCount(template.getMaxAttJewelsCount());
            bonus.setAttEnergyMinCount(template.getMinAttEnergyCount());
            bonus.setAttEnergyMaxCount(template.getMaxAttEnergyCount());
            bonus.setWeaponsMinCount(template.getMinWeaponsCount());
            bonus.setWeaponsMaxCount(template.getMaxWeaponsCount());
            bonus.setArmorsMinCount(template.getMinArmorsCount());
            bonus.setArmorsMaxCount(template.getMaxArmorsCount());
            bonus.setAccessoryesMinCount(template.getMinAccessoryesCount());
            bonus.setAccessoryesMaxCount(template.getMaxAccessoryesCount());
            bonus.setMaxSpoilItemsFromOneGroup(template.getMaxSpoilItemsFromOneGroup());
            bonus.setMaxDropItemsFromOneGroup(template.getMaxDropItemsFromOneGroup());
            bonus.setMaxRaidDropItemsFromOneGroup(template.getMaxRaidDropItemsFromOneGroup());
            bonus.setOnlineType(template.isOnlineType());
            bonus.setOnlineTime(time);
            bonus.setActivate(true);
            player.startPremiumTask(time);
            player.sendPacket(new ExBrPremiumState(player.getObjectId(), 1));
            player.broadcastPacket(new MagicSkillUse(player, player, 6463, 1, 0, 0));
            player.sendPacket(SystemMessageId.THE_PREMIUM_ITEM_FOR_THIS_ACCOUNT_WAS_PROVIDED);
            if (Config.PC_BANG_ENABLED && Config.PC_BANG_ONLY_FOR_PREMIUM) {
               player.startPcBangPointsTask();
            }

            for(PremiumGift gift : template.getGifts()) {
               if (gift != null) {
                  player.addItem("PremiumGift", gift.getId(), gift.getCount(), player, true);
               }
            }
         }
      }
   }

   public void updatePersonal(Player player, int id, long time) {
      if (Config.USE_PREMIUMSERVICE) {
         try (Connection con = DatabaseFactory.getInstance().getConnection()) {
            PreparedStatement statement = con.prepareStatement("UPDATE character_premium_personal SET id=?,status=?,expireTime=? WHERE charId=?");
            statement.setInt(1, id);
            statement.setInt(2, 1);
            statement.setLong(3, time);
            statement.setInt(4, player.getObjectId());
            statement.execute();
            statement.close();
         } catch (SQLException var18) {
            _log.log(Level.WARNING, "Could not update data: " + var18);
         }

         PremiumTemplate template = PremiumAccountsParser.getInstance().getPremiumTemplate(id);
         if (template != null) {
            PremiumBonus bonus = player.getPremiumBonus();
            bonus.setPremiumId(template.getId());
            bonus.setIsPersonal(template.isPersonal());
            bonus.setRateXp(template.getExp());
            bonus.setRateSp(template.getSp());
            bonus.setDropSiege(template.getEpaulette());
            bonus.setDropElementStones(template.getElementStones());
            bonus.setDropSealStones(template.getSealStones());
            bonus.setQuestRewardRate(template.getQuestReward());
            bonus.setQuestDropRate(template.getQuestDrop());
            bonus.setDropAdena(template.getAdena());
            bonus.setDropItems(template.getItems());
            bonus.setDropRaid(template.getDropRaids());
            bonus.setDropEpic(template.getDropEpics());
            bonus.setDropSpoil(template.getSpoil());
            bonus.setWeight(template.getWeight());
            bonus.setCraftChance(template.getCraftChance());
            bonus.setMasterWorkChance(template.getMasterWorkChance());
            bonus.setEnchantChance(template.getEnchantChance());
            bonus.setFishingRate(template.getFishing());
            bonus.setFameBonus(template.getFameBonus());
            bonus.setReflectionReduce(template.getReflectionReduce());
            bonus.setNobleStonesMinCount(template.getMinNobleStonesCount());
            bonus.setNobleStonesMaxCount(template.getMaxNobleStonesCount());
            bonus.setSealStonesMinCount(template.getMinSealStonesCount());
            bonus.setSealStonesMaxCount(template.getMaxSealStonesCount());
            bonus.setLifeStonesMinCount(template.getMinLifeStonesCount());
            bonus.setLifeStonesMaxCount(template.getMaxLifeStonesCount());
            bonus.setEnchantScrollsMinCount(template.getMinEnchantScrollsCount());
            bonus.setEnchantScrollsMaxCount(template.getMaxEnchantScrollsCount());
            bonus.setForgottenScrollsMinCount(template.getMinForgottenScrollsCount());
            bonus.setForgottenScrollsMaxCount(template.getMaxForgottenScrollsCount());
            bonus.setMaterialsMinCount(template.getMinMaterialsCount());
            bonus.setMaterialsMaxCount(template.getMaxMaterialsCount());
            bonus.setRepicesMinCount(template.getMinRepicesCount());
            bonus.setRepicesMaxCount(template.getMaxRepicesCount());
            bonus.setBeltsMinCount(template.getMinBeltsCount());
            bonus.setBeltsMaxCount(template.getMaxBeltsCount());
            bonus.setBraceletsMinCount(template.getMinBraceletsCount());
            bonus.setBraceletsMaxCount(template.getMaxBraceletsCount());
            bonus.setCloaksMinCount(template.getMinCloaksCount());
            bonus.setCloaksMaxCount(template.getMaxCloaksCount());
            bonus.setCodexMinCount(template.getMinCodexCount());
            bonus.setCodexMaxCount(template.getMaxCodexCount());
            bonus.setAttStonesMinCount(template.getMinAttStonesCount());
            bonus.setAttStonesMaxCount(template.getMaxAttStonesCount());
            bonus.setAttCrystalsMinCount(template.getMinAttCrystalsCount());
            bonus.setAttCrystalsMaxCount(template.getMaxAttCrystalsCount());
            bonus.setAttJewelsMinCount(template.getMinAttJewelsCount());
            bonus.setAttJewelsMaxCount(template.getMaxAttJewelsCount());
            bonus.setAttEnergyMinCount(template.getMinAttEnergyCount());
            bonus.setAttEnergyMaxCount(template.getMaxAttEnergyCount());
            bonus.setWeaponsMinCount(template.getMinWeaponsCount());
            bonus.setWeaponsMaxCount(template.getMaxWeaponsCount());
            bonus.setArmorsMinCount(template.getMinArmorsCount());
            bonus.setArmorsMaxCount(template.getMaxArmorsCount());
            bonus.setAccessoryesMinCount(template.getMinAccessoryesCount());
            bonus.setAccessoryesMaxCount(template.getMaxAccessoryesCount());
            bonus.setMaxSpoilItemsFromOneGroup(template.getMaxSpoilItemsFromOneGroup());
            bonus.setMaxDropItemsFromOneGroup(template.getMaxDropItemsFromOneGroup());
            bonus.setMaxRaidDropItemsFromOneGroup(template.getMaxRaidDropItemsFromOneGroup());
            bonus.setOnlineType(template.isOnlineType());
            bonus.setOnlineTime(time);
            bonus.setActivate(true);
            player.startPremiumTask(time);
            player.sendPacket(new ExBrPremiumState(player.getObjectId(), 1));
            player.broadcastPacket(new MagicSkillUse(player, player, 6463, 1, 0, 0));
            player.sendPacket(SystemMessageId.THE_PREMIUM_ITEM_FOR_THIS_ACCOUNT_WAS_PROVIDED);
            if (Config.PC_BANG_ENABLED && Config.PC_BANG_ONLY_FOR_PREMIUM) {
               player.startPcBangPointsTask();
            }

            for(PremiumGift gift : template.getGifts()) {
               if (gift != null) {
                  player.addItem("PremiumGift", gift.getId(), gift.getCount(), player, true);
               }
            }
         }
      }
   }

   public void disable(Player player) {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("UPDATE character_premium SET id=?,status=?,expireTime=? WHERE account=?");
         statement.setInt(1, 0);
         statement.setLong(2, 0L);
         statement.setLong(3, 0L);
         statement.setString(4, player.getAccountName());
         statement.execute();
         statement.close();
      } catch (SQLException var15) {
         _log.log(Level.WARNING, "Could not disable data: " + var15);
      }
   }

   public void disablePersonal(Player player) {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("UPDATE character_premium_personal SET id=?,status=?,expireTime=? WHERE charId=?");
         statement.setInt(1, 0);
         statement.setLong(2, 0L);
         statement.setLong(3, 0L);
         statement.setInt(4, player.getObjectId());
         statement.execute();
         statement.close();
      } catch (SQLException var15) {
         _log.log(Level.WARNING, "Could not disable data: " + var15);
      }
   }

   public void restore(Player player) {
      if (Config.USE_PREMIUMSERVICE) {
         boolean sucess = false;
         boolean active = false;

         try (Connection con = DatabaseFactory.getInstance().getConnection()) {
            PreparedStatement statement = con.prepareStatement("SELECT id,status,expireTime FROM character_premium_personal WHERE charId=?");
            statement.setInt(1, player.getObjectId());
            ResultSet rset = statement.executeQuery();

            while(rset.next()) {
               sucess = true;
               if (rset.getInt("status") == 1) {
                  PremiumTemplate template = PremiumAccountsParser.getInstance().getPremiumTemplate(rset.getInt("id"));
                  if (template != null) {
                     boolean validTime;
                     if (template.isOnlineType()) {
                        validTime = rset.getLong("expireTime") < template.getTime() * 1000L;
                     } else {
                        validTime = rset.getLong("expireTime") > System.currentTimeMillis();
                     }

                     if (validTime) {
                        active = true;
                        PremiumBonus bonus = player.getPremiumBonus();
                        bonus.setPremiumId(template.getId());
                        bonus.setIsPersonal(template.isPersonal());
                        bonus.setRateXp(template.getExp());
                        bonus.setRateSp(template.getSp());
                        bonus.setDropSiege(template.getEpaulette());
                        bonus.setDropElementStones(template.getElementStones());
                        bonus.setDropSealStones(template.getSealStones());
                        bonus.setQuestRewardRate(template.getQuestReward());
                        bonus.setQuestDropRate(template.getQuestDrop());
                        bonus.setDropAdena(template.getAdena());
                        bonus.setDropItems(template.getItems());
                        bonus.setDropRaid(template.getDropRaids());
                        bonus.setDropEpic(template.getDropEpics());
                        bonus.setDropSpoil(template.getSpoil());
                        bonus.setWeight(template.getWeight());
                        bonus.setCraftChance(template.getCraftChance());
                        bonus.setMasterWorkChance(template.getMasterWorkChance());
                        bonus.setEnchantChance(template.getEnchantChance());
                        bonus.setFishingRate(template.getFishing());
                        bonus.setFameBonus(template.getFameBonus());
                        bonus.setReflectionReduce(template.getReflectionReduce());
                        bonus.setNobleStonesMinCount(template.getMinNobleStonesCount());
                        bonus.setNobleStonesMaxCount(template.getMaxNobleStonesCount());
                        bonus.setSealStonesMinCount(template.getMinSealStonesCount());
                        bonus.setSealStonesMaxCount(template.getMaxSealStonesCount());
                        bonus.setLifeStonesMinCount(template.getMinLifeStonesCount());
                        bonus.setLifeStonesMaxCount(template.getMaxLifeStonesCount());
                        bonus.setEnchantScrollsMinCount(template.getMinEnchantScrollsCount());
                        bonus.setEnchantScrollsMaxCount(template.getMaxEnchantScrollsCount());
                        bonus.setForgottenScrollsMinCount(template.getMinForgottenScrollsCount());
                        bonus.setForgottenScrollsMaxCount(template.getMaxForgottenScrollsCount());
                        bonus.setMaterialsMinCount(template.getMinMaterialsCount());
                        bonus.setMaterialsMaxCount(template.getMaxMaterialsCount());
                        bonus.setRepicesMinCount(template.getMinRepicesCount());
                        bonus.setRepicesMaxCount(template.getMaxRepicesCount());
                        bonus.setBeltsMinCount(template.getMinBeltsCount());
                        bonus.setBeltsMaxCount(template.getMaxBeltsCount());
                        bonus.setBraceletsMinCount(template.getMinBraceletsCount());
                        bonus.setBraceletsMaxCount(template.getMaxBraceletsCount());
                        bonus.setCloaksMinCount(template.getMinCloaksCount());
                        bonus.setCloaksMaxCount(template.getMaxCloaksCount());
                        bonus.setCodexMinCount(template.getMinCodexCount());
                        bonus.setCodexMaxCount(template.getMaxCodexCount());
                        bonus.setAttStonesMinCount(template.getMinAttStonesCount());
                        bonus.setAttStonesMaxCount(template.getMaxAttStonesCount());
                        bonus.setAttCrystalsMinCount(template.getMinAttCrystalsCount());
                        bonus.setAttCrystalsMaxCount(template.getMaxAttCrystalsCount());
                        bonus.setAttJewelsMinCount(template.getMinAttJewelsCount());
                        bonus.setAttJewelsMaxCount(template.getMaxAttJewelsCount());
                        bonus.setAttEnergyMinCount(template.getMinAttEnergyCount());
                        bonus.setAttEnergyMaxCount(template.getMaxAttEnergyCount());
                        bonus.setWeaponsMinCount(template.getMinWeaponsCount());
                        bonus.setWeaponsMaxCount(template.getMaxWeaponsCount());
                        bonus.setArmorsMinCount(template.getMinArmorsCount());
                        bonus.setArmorsMaxCount(template.getMaxArmorsCount());
                        bonus.setAccessoryesMinCount(template.getMinAccessoryesCount());
                        bonus.setAccessoryesMaxCount(template.getMaxAccessoryesCount());
                        bonus.setMaxSpoilItemsFromOneGroup(template.getMaxSpoilItemsFromOneGroup());
                        bonus.setMaxDropItemsFromOneGroup(template.getMaxDropItemsFromOneGroup());
                        bonus.setMaxRaidDropItemsFromOneGroup(template.getMaxRaidDropItemsFromOneGroup());
                        bonus.setOnlineType(template.isOnlineType());
                        bonus.setOnlineTime(rset.getLong("expireTime"));
                        bonus.setActivate(true);
                        player.startPremiumTask(rset.getLong("expireTime"));
                     } else {
                        this.disablePersonal(player);
                        boolean removed = false;

                        for(PremiumGift gift : template.getGifts()) {
                           if (gift != null && gift.isRemovable()) {
                              if (player.getInventory().getItemByItemId(gift.getId()) != null) {
                                 if (player.getInventory().destroyItemByItemId(gift.getId(), gift.getCount(), "Remove Premium")) {
                                    removed = true;
                                 }
                              } else if (player.getWarehouse().getItemByItemId(gift.getId()) != null
                                 && player.getWarehouse().destroyItemByItemId(gift.getId(), gift.getCount(), "Remove Premium")) {
                                 removed = true;
                              }
                           }
                        }

                        if (removed) {
                           if (Config.PC_BANG_ENABLED && Config.PC_BANG_ONLY_FOR_PREMIUM) {
                              player.stopPcBangPointsTask();
                           }

                           player.sendPacket(SystemMessageId.THE_PREMIUM_ACCOUNT_HAS_BEEN_TERMINATED);
                        }
                     }
                  }
               }
            }

            statement.close();
         } catch (Exception var23) {
            _log.log(Level.WARNING, "Could not restore premium data for:" + player.getAccountName() + " " + var23);
         }

         if (!sucess) {
            this.insertPersonal(player);
         }

         if (!active) {
            this.restoreForAcc(player);
         }
      }
   }

   public void restoreForAcc(Player player) {
      if (Config.USE_PREMIUMSERVICE) {
         boolean sucess = false;

         try (Connection con = DatabaseFactory.getInstance().getConnection()) {
            PreparedStatement statement = con.prepareStatement("SELECT id,status,expireTime FROM character_premium WHERE account=?");
            statement.setString(1, player.getAccountName());
            ResultSet rset = statement.executeQuery();

            while(rset.next()) {
               sucess = true;
               if (rset.getInt("status") == 1) {
                  PremiumTemplate template = PremiumAccountsParser.getInstance().getPremiumTemplate(rset.getInt("id"));
                  if (template != null) {
                     boolean validTime;
                     if (template.isOnlineType()) {
                        validTime = rset.getLong("expireTime") < template.getTime() * 1000L;
                     } else {
                        validTime = rset.getLong("expireTime") > System.currentTimeMillis();
                     }

                     if (validTime) {
                        PremiumBonus bonus = player.getPremiumBonus();
                        bonus.setPremiumId(template.getId());
                        bonus.setIsPersonal(template.isPersonal());
                        bonus.setRateXp(template.getExp());
                        bonus.setRateSp(template.getSp());
                        bonus.setDropSiege(template.getEpaulette());
                        bonus.setDropElementStones(template.getElementStones());
                        bonus.setDropSealStones(template.getSealStones());
                        bonus.setQuestRewardRate(template.getQuestReward());
                        bonus.setQuestDropRate(template.getQuestDrop());
                        bonus.setDropAdena(template.getAdena());
                        bonus.setDropItems(template.getItems());
                        bonus.setDropRaid(template.getDropRaids());
                        bonus.setDropEpic(template.getDropEpics());
                        bonus.setDropSpoil(template.getSpoil());
                        bonus.setWeight(template.getWeight());
                        bonus.setCraftChance(template.getCraftChance());
                        bonus.setMasterWorkChance(template.getMasterWorkChance());
                        bonus.setEnchantChance(template.getEnchantChance());
                        bonus.setFishingRate(template.getFishing());
                        bonus.setFameBonus(template.getFameBonus());
                        bonus.setReflectionReduce(template.getReflectionReduce());
                        bonus.setNobleStonesMinCount(template.getMinNobleStonesCount());
                        bonus.setNobleStonesMaxCount(template.getMaxNobleStonesCount());
                        bonus.setSealStonesMinCount(template.getMinSealStonesCount());
                        bonus.setSealStonesMaxCount(template.getMaxSealStonesCount());
                        bonus.setLifeStonesMinCount(template.getMinLifeStonesCount());
                        bonus.setLifeStonesMaxCount(template.getMaxLifeStonesCount());
                        bonus.setEnchantScrollsMinCount(template.getMinEnchantScrollsCount());
                        bonus.setEnchantScrollsMaxCount(template.getMaxEnchantScrollsCount());
                        bonus.setForgottenScrollsMinCount(template.getMinForgottenScrollsCount());
                        bonus.setForgottenScrollsMaxCount(template.getMaxForgottenScrollsCount());
                        bonus.setMaterialsMinCount(template.getMinMaterialsCount());
                        bonus.setMaterialsMaxCount(template.getMaxMaterialsCount());
                        bonus.setRepicesMinCount(template.getMinRepicesCount());
                        bonus.setRepicesMaxCount(template.getMaxRepicesCount());
                        bonus.setBeltsMinCount(template.getMinBeltsCount());
                        bonus.setBeltsMaxCount(template.getMaxBeltsCount());
                        bonus.setBraceletsMinCount(template.getMinBraceletsCount());
                        bonus.setBraceletsMaxCount(template.getMaxBraceletsCount());
                        bonus.setCloaksMinCount(template.getMinCloaksCount());
                        bonus.setCloaksMaxCount(template.getMaxCloaksCount());
                        bonus.setCodexMinCount(template.getMinCodexCount());
                        bonus.setCodexMaxCount(template.getMaxCodexCount());
                        bonus.setAttStonesMinCount(template.getMinAttStonesCount());
                        bonus.setAttStonesMaxCount(template.getMaxAttStonesCount());
                        bonus.setAttCrystalsMinCount(template.getMinAttCrystalsCount());
                        bonus.setAttCrystalsMaxCount(template.getMaxAttCrystalsCount());
                        bonus.setAttJewelsMinCount(template.getMinAttJewelsCount());
                        bonus.setAttJewelsMaxCount(template.getMaxAttJewelsCount());
                        bonus.setAttEnergyMinCount(template.getMinAttEnergyCount());
                        bonus.setAttEnergyMaxCount(template.getMaxAttEnergyCount());
                        bonus.setWeaponsMinCount(template.getMinWeaponsCount());
                        bonus.setWeaponsMaxCount(template.getMaxWeaponsCount());
                        bonus.setArmorsMinCount(template.getMinArmorsCount());
                        bonus.setArmorsMaxCount(template.getMaxArmorsCount());
                        bonus.setAccessoryesMinCount(template.getMinAccessoryesCount());
                        bonus.setAccessoryesMaxCount(template.getMaxAccessoryesCount());
                        bonus.setMaxSpoilItemsFromOneGroup(template.getMaxSpoilItemsFromOneGroup());
                        bonus.setMaxDropItemsFromOneGroup(template.getMaxDropItemsFromOneGroup());
                        bonus.setMaxRaidDropItemsFromOneGroup(template.getMaxRaidDropItemsFromOneGroup());
                        bonus.setOnlineType(template.isOnlineType());
                        bonus.setOnlineTime(rset.getLong("expireTime"));
                        bonus.setActivate(true);
                        player.startPremiumTask(rset.getLong("expireTime"));
                     } else {
                        this.disable(player);
                        boolean removed = false;

                        for(PremiumGift gift : template.getGifts()) {
                           if (gift != null && gift.isRemovable()) {
                              if (player.getInventory().getItemByItemId(gift.getId()) != null) {
                                 if (player.getInventory().destroyItemByItemId(gift.getId(), gift.getCount(), "Remove Premium")) {
                                    removed = true;
                                 }
                              } else if (player.getWarehouse().getItemByItemId(gift.getId()) != null
                                 && player.getWarehouse().destroyItemByItemId(gift.getId(), gift.getCount(), "Remove Premium")) {
                                 removed = true;
                              }
                           }
                        }

                        if (removed) {
                           player.sendPacket(SystemMessageId.THE_PREMIUM_ACCOUNT_HAS_BEEN_TERMINATED);
                        }
                     }
                  }
               }
            }

            statement.close();
         } catch (Exception var22) {
            _log.log(Level.WARNING, "Could not restore premium data for:" + player.getAccountName() + " " + var22);
         }

         if (!sucess) {
            this.insert(player);
         }
      }
   }
}
