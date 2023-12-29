package l2e.gameserver.handler.admincommandhandlers.impl;

import java.util.StringTokenizer;
import l2e.gameserver.Config;
import l2e.gameserver.data.dao.CharacterPremiumDAO;
import l2e.gameserver.data.parser.PremiumAccountsParser;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.ClanMember;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.player.PremiumBonus;
import l2e.gameserver.model.service.autofarm.FarmSettings;
import l2e.gameserver.model.service.premium.PremiumGift;
import l2e.gameserver.model.service.premium.PremiumTemplate;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExBrPremiumState;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class Premium implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{
      "admin_premium_menu", "admin_premium_give", "admin_clean_premium", "admin_premium_clan_give", "admin_addPremium", "admin_addClanPremium"
   };

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      if (command.equals("admin_premium_menu")) {
         this.showMenu(activeChar);
         return true;
      } else if (command.startsWith("admin_addPremium")) {
         try {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            int premiumId = Integer.parseInt(st.nextToken());
            GameObject targetChar = activeChar.getTarget();
            if (targetChar == null || !(targetChar instanceof Player)) {
               activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
               this.showMenu(activeChar);
               return false;
            }

            Player targetPlayer = (Player)targetChar;
            if (this.givePremium(targetPlayer, premiumId)) {
               activeChar.sendMessage("Preumium account is successfully added for: " + targetPlayer.getName());
            }
         } catch (Exception var17) {
            activeChar.sendMessage("Failed to give premium account...");
         }

         this.showMenu(activeChar);
         return true;
      } else {
         if (command.startsWith("admin_clean_premium")) {
            try {
               StringTokenizer st = new StringTokenizer(command, " ");
               st.nextToken();
               String charName = null;

               try {
                  charName = st.nextToken();
               } catch (Exception var16) {
               }

               if (charName != null) {
                  Player player = World.getInstance().getPlayer(charName);
                  if (player != null) {
                     if (!player.hasPremiumBonus()) {
                        activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
                        this.showMenu(activeChar);
                        return false;
                     }

                     player.stopPremiumTask();
                     this.clearPremium(player);
                     activeChar.sendMessage("Preumium account removed at : " + player.getName());
                     this.showMenu(activeChar);
                     return true;
                  }

                  activeChar.sendMessage("Failed to clean premium account...");
                  this.showMenu(activeChar);
                  return false;
               }

               GameObject targetChar = activeChar.getTarget();
               if (targetChar == null) {
                  activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
                  this.showMenu(activeChar);
                  return false;
               }

               if (targetChar.isPlayer() && targetChar.getActingPlayer().hasPremiumBonus()) {
                  targetChar.getActingPlayer().stopPremiumTask();
                  this.clearPremium(targetChar.getActingPlayer());
                  activeChar.sendMessage("Preumium account removed at : " + targetChar.getName());
                  this.showMenu(activeChar);
                  return true;
               }

               activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
               this.showMenu(activeChar);
               return false;
            } catch (Exception var19) {
               activeChar.sendMessage("Failed to clean premium account...");
            }
         } else {
            if (command.startsWith("admin_addclanPremium")) {
               try {
                  StringTokenizer st = new StringTokenizer(command, " ");
                  st.nextToken();
                  String premiumId = null;

                  try {
                     premiumId = st.nextToken();
                  } catch (Exception var11) {
                  }

                  GameObject targetChar = activeChar.getTarget();
                  if (targetChar != null && targetChar instanceof Player) {
                     if (premiumId == null) {
                        activeChar.sendMessage("Failed to give premium account...");
                        this.showMenu(activeChar);
                        return false;
                     }

                     Player targetPlayer = (Player)targetChar;
                     if (targetPlayer.getClan() == null) {
                        if (this.givePremium(targetPlayer, Integer.parseInt(premiumId))) {
                           activeChar.sendMessage("Preumium account is successfully added for: " + targetPlayer.getName());
                        }

                        this.showMenu(activeChar);
                        return true;
                     }

                     for(ClanMember member : targetPlayer.getClan().getMembers()) {
                        if (member != null && member.isOnline() && this.givePremium(member.getPlayerInstance(), Integer.parseInt(premiumId))) {
                           activeChar.sendMessage("Preumium account is successfully added for: " + member.getName());
                        }
                     }

                     this.showMenu(activeChar);
                     return true;
                  }

                  activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
                  this.showMenu(activeChar);
                  return false;
               } catch (Exception var12) {
                  activeChar.sendMessage("Failed to give premium account...");
                  this.showMenu(activeChar);
                  return true;
               }
            }

            if (command.startsWith("admin_premium_give")) {
               try {
                  StringTokenizer st = new StringTokenizer(command, " ");
                  st.nextToken();
                  String charName = null;
                  String premiumId = null;

                  try {
                     charName = st.nextToken();
                  } catch (Exception var14) {
                  }

                  try {
                     premiumId = st.nextToken();
                  } catch (Exception var13) {
                  }

                  if (charName == null) {
                     activeChar.sendMessage("Failed to give premium account...");
                     this.showMenu(activeChar);
                     return false;
                  }

                  Player player = World.getInstance().getPlayer(charName);
                  if (player != null && premiumId != null && this.givePremium(player, Integer.parseInt(premiumId))) {
                     activeChar.sendMessage("Preumium account is successfully added for: " + player.getName());
                  }

                  this.showMenu(activeChar);
                  return true;
               } catch (Exception var15) {
                  activeChar.sendMessage("Failed to give premium account...");
                  this.showMenu(activeChar);
                  return true;
               }
            }

            if (command.startsWith("admin_premium_clan_give")) {
               try {
                  StringTokenizer st = new StringTokenizer(command, " ");
                  st.nextToken();
                  String charName = st.nextToken();
                  int premiumId = Integer.parseInt(st.nextToken());
                  Player player = World.getInstance().getPlayer(charName);
                  if (player != null) {
                     if (player.getClan() == null) {
                        if (this.givePremium(player, premiumId)) {
                           activeChar.sendMessage("Preumium account is successfully added for: " + player.getName());
                        }
                     } else {
                        for(ClanMember member : player.getClan().getMembers()) {
                           if (member != null && member.isOnline() && this.givePremium(member.getPlayerInstance(), premiumId)) {
                              activeChar.sendMessage("Preumium account is successfully added for: " + member.getName());
                           }
                        }
                     }

                     this.showMenu(activeChar);
                     return true;
                  }
               } catch (Exception var18) {
                  activeChar.sendMessage("Failed to give premium account...");
               }

               this.showMenu(activeChar);
               return true;
            }
         }

         return true;
      }
   }

   private void showMenu(Player activeChar) {
      NpcHtmlMessage adminhtm = new NpcHtmlMessage(5);
      adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/premium_menu.htm");
      activeChar.sendPacket(adminhtm);
   }

   private boolean givePremium(Player player, int premiumId) {
      if (player != null) {
         PremiumTemplate template = PremiumAccountsParser.getInstance().getPremiumTemplate(premiumId);
         if (template != null) {
            long time = !template.isOnlineType() ? System.currentTimeMillis() + template.getTime() * 1000L : 0L;
            if (template.isPersonal()) {
               CharacterPremiumDAO.getInstance().updatePersonal(player, template.getId(), time);
            } else {
               CharacterPremiumDAO.getInstance().update(player, template.getId(), time);
            }

            if (player.isInParty()) {
               player.getParty().recalculatePartyData();
            }

            return true;
         }
      }

      return false;
   }

   private void clearPremium(Player player) {
      PremiumBonus bonus = player.getPremiumBonus();
      int premiumId = bonus.getPremiumId();
      bonus.setPremiumId(0);
      bonus.setRateXp(1.0);
      bonus.setRateSp(1.0);
      bonus.setDropSiege(1.0);
      bonus.setDropElementStones(1.0);
      bonus.setDropSealStones(1.0);
      bonus.setQuestRewardRate(1.0);
      bonus.setQuestDropRate(1.0);
      bonus.setDropAdena(1.0);
      bonus.setDropItems(1.0);
      bonus.setDropRaid(1.0);
      bonus.setDropEpic(1.0);
      bonus.setDropSpoil(1.0);
      bonus.setWeight(1.0);
      bonus.setCraftChance(0);
      bonus.setMasterWorkChance(0);
      bonus.setEnchantChance(0);
      bonus.setFishingRate(1.0);
      bonus.setFameBonus(1.0);
      bonus.setReflectionReduce(1.0);
      bonus.setNobleStonesMinCount(Config.RATE_NOBLE_STONES_COUNT_MIN);
      bonus.setNobleStonesMaxCount(Config.RATE_NOBLE_STONES_COUNT_MAX);
      bonus.setSealStonesMinCount(Config.RATE_SEAL_STONES_COUNT_MIN);
      bonus.setSealStonesMaxCount(Config.RATE_SEAL_STONES_COUNT_MAX);
      bonus.setLifeStonesMinCount(Config.RATE_LIFE_STONES_COUNT_MIN);
      bonus.setLifeStonesMaxCount(Config.RATE_LIFE_STONES_COUNT_MAX);
      bonus.setEnchantScrollsMinCount(Config.RATE_ENCHANT_SCROLLS_COUNT_MIN);
      bonus.setEnchantScrollsMaxCount(Config.RATE_ENCHANT_SCROLLS_COUNT_MAX);
      bonus.setForgottenScrollsMinCount(Config.RATE_FORGOTTEN_SCROLLS_COUNT_MIN);
      bonus.setForgottenScrollsMaxCount(Config.RATE_FORGOTTEN_SCROLLS_COUNT_MAX);
      bonus.setMaterialsMinCount(Config.RATE_KEY_MATHETIRALS_COUNT_MIN);
      bonus.setMaterialsMaxCount(Config.RATE_KEY_MATHETIRALS_COUNT_MAX);
      bonus.setRepicesMinCount(Config.RATE_RECEPIES_COUNT_MIN);
      bonus.setRepicesMaxCount(Config.RATE_RECEPIES_COUNT_MAX);
      bonus.setBeltsMinCount(Config.RATE_BELTS_COUNT_MIN);
      bonus.setBeltsMaxCount(Config.RATE_BELTS_COUNT_MAX);
      bonus.setBraceletsMinCount(Config.RATE_BRACELETS_COUNT_MIN);
      bonus.setBraceletsMaxCount(Config.RATE_BRACELETS_COUNT_MAX);
      bonus.setCloaksMinCount(Config.RATE_CLOAKS_COUNT_MIN);
      bonus.setCloaksMaxCount(Config.RATE_CLOAKS_COUNT_MAX);
      bonus.setCodexMinCount(Config.RATE_CODEX_BOOKS_COUNT_MIN);
      bonus.setCodexMaxCount(Config.RATE_CODEX_BOOKS_COUNT_MAX);
      bonus.setAttStonesMinCount(Config.RATE_ATTRIBUTE_STONES_COUNT_MIN);
      bonus.setAttStonesMaxCount(Config.RATE_ATTRIBUTE_STONES_COUNT_MAX);
      bonus.setAttCrystalsMinCount(Config.RATE_ATTRIBUTE_CRYSTALS_COUNT_MIN);
      bonus.setAttCrystalsMaxCount(Config.RATE_ATTRIBUTE_CRYSTALS_COUNT_MAX);
      bonus.setAttJewelsMinCount(Config.RATE_ATTRIBUTE_JEWELS_COUNT_MIN);
      bonus.setAttJewelsMaxCount(Config.RATE_ATTRIBUTE_JEWELS_COUNT_MAX);
      bonus.setAttEnergyMinCount(Config.RATE_ATTRIBUTE_ENERGY_COUNT_MIN);
      bonus.setAttEnergyMaxCount(Config.RATE_ATTRIBUTE_ENERGY_COUNT_MAX);
      bonus.setWeaponsMinCount(Config.RATE_WEAPONS_COUNT_MIN);
      bonus.setWeaponsMaxCount(Config.RATE_WEAPONS_COUNT_MAX);
      bonus.setArmorsMinCount(Config.RATE_ARMOR_COUNT_MIN);
      bonus.setArmorsMaxCount(Config.RATE_ARMOR_COUNT_MAX);
      bonus.setAccessoryesMinCount(Config.RATE_ACCESSORY_COUNT_MIN);
      bonus.setAccessoryesMaxCount(Config.RATE_ACCESSORY_COUNT_MAX);
      bonus.setMaxSpoilItemsFromOneGroup(Config.MAX_SPOIL_ITEMS_FROM_ONE_GROUP);
      bonus.setMaxDropItemsFromOneGroup(Config.MAX_DROP_ITEMS_FROM_ONE_GROUP);
      bonus.setMaxRaidDropItemsFromOneGroup(Config.MAX_DROP_ITEMS_FROM_ONE_GROUP_RAIDS);
      bonus.setOnlineType(false);
      bonus.setActivate(false);
      if (bonus.isPersonal()) {
         CharacterPremiumDAO.getInstance().disablePersonal(player);
         bonus.setIsPersonal(false);
      } else {
         CharacterPremiumDAO.getInstance().disable(player);
      }

      player.sendPacket(new ExBrPremiumState(player.getObjectId(), 0));
      player.sendPacket(SystemMessageId.THE_PREMIUM_ACCOUNT_HAS_BEEN_TERMINATED);
      if (Config.PC_BANG_ENABLED && Config.PC_BANG_ONLY_FOR_PREMIUM) {
         player.stopPcBangPointsTask();
      }

      PremiumTemplate template = PremiumAccountsParser.getInstance().getPremiumTemplate(premiumId);
      if (template != null) {
         for(PremiumGift gift : template.getGifts()) {
            if (gift != null && gift.isRemovable()) {
               if (player.getInventory().getItemByItemId(gift.getId()) != null) {
                  player.getInventory().destroyItemByItemId(gift.getId(), gift.getCount(), "Remove Premium");
               } else if (player.getWarehouse().getItemByItemId(gift.getId()) != null) {
                  player.getWarehouse().destroyItemByItemId(gift.getId(), gift.getCount(), "Remove Premium");
               }
            }
         }
      }

      if (player.isInParty()) {
         player.getParty().recalculatePartyData();
      }

      if (FarmSettings.ALLOW_AUTO_FARM && FarmSettings.PREMIUM_FARM_FREE && !player.getFarmSystem().isActiveFarmTask()) {
         player.getFarmSystem().stopFarmTask(false);
      }
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }
}
