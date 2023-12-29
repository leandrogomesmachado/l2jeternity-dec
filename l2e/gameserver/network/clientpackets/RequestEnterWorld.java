package l2e.gameserver.network.clientpackets;

import l2e.commons.util.Base64;
import l2e.commons.util.TimeUtils;
import l2e.commons.util.TransferSkillUtils;
import l2e.gameserver.Announcements;
import l2e.gameserver.AutoRestart;
import l2e.gameserver.Config;
import l2e.gameserver.SevenSigns;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.dao.CharacterPremiumDAO;
import l2e.gameserver.data.dao.DailyTasksDAO;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.data.parser.AdminParser;
import l2e.gameserver.data.parser.PremiumAccountsParser;
import l2e.gameserver.data.parser.SkillTreesParser;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.instancemanager.CHSiegeManager;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.ClanHallManager;
import l2e.gameserver.instancemanager.CoupleManager;
import l2e.gameserver.instancemanager.CursedWeaponsManager;
import l2e.gameserver.instancemanager.DimensionalRiftManager;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.instancemanager.FortSiegeManager;
import l2e.gameserver.instancemanager.GlobalVariablesManager;
import l2e.gameserver.instancemanager.KrateisCubeManager;
import l2e.gameserver.instancemanager.MailManager;
import l2e.gameserver.instancemanager.OnlineRewardManager;
import l2e.gameserver.instancemanager.PetitionManager;
import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.instancemanager.SiegeManager;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.appearance.PcAppearance;
import l2e.gameserver.model.actor.instance.ClassMasterInstance;
import l2e.gameserver.model.actor.tasks.player.TeleportToTownTask;
import l2e.gameserver.model.entity.Couple;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.model.entity.FortSiege;
import l2e.gameserver.model.entity.Siege;
import l2e.gameserver.model.entity.clanhall.AuctionableHall;
import l2e.gameserver.model.entity.clanhall.SiegableHall;
import l2e.gameserver.model.entity.mods.ProtectionIP;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.service.premium.PremiumTemplate;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.model.strings.server.ServerStorage;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.GameClient;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.Die;
import l2e.gameserver.network.serverpackets.EtcStatusUpdate;
import l2e.gameserver.network.serverpackets.ExBasicActionList;
import l2e.gameserver.network.serverpackets.ExBrPremiumState;
import l2e.gameserver.network.serverpackets.ExGetBookMarkInfo;
import l2e.gameserver.network.serverpackets.ExNoticePostArrived;
import l2e.gameserver.network.serverpackets.ExNotifyPremiumItem;
import l2e.gameserver.network.serverpackets.ExPCCafePointInfo;
import l2e.gameserver.network.serverpackets.ExPrivateStorePackageMsg;
import l2e.gameserver.network.serverpackets.ExReceiveShowPostFriend;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage;
import l2e.gameserver.network.serverpackets.ExStorageMaxCount;
import l2e.gameserver.network.serverpackets.HennaInfo;
import l2e.gameserver.network.serverpackets.L2FriendList;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.PledgeShowMemberListAll;
import l2e.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import l2e.gameserver.network.serverpackets.PledgeSkillList;
import l2e.gameserver.network.serverpackets.PledgeStatusChanged;
import l2e.gameserver.network.serverpackets.PrivateStoreBuyMsg;
import l2e.gameserver.network.serverpackets.PrivateStoreSellMsg;
import l2e.gameserver.network.serverpackets.QuestList;
import l2e.gameserver.network.serverpackets.RecipeShopMsg;
import l2e.gameserver.network.serverpackets.ShortCutInit;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.scripts.events.Hitman;

public class RequestEnterWorld extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar == null) {
         _log.warning("EnterWorld failed! activeChar returned 'null'.");
         this.getClient().closeNow();
      } else if (World.getInstance().findObject(activeChar.getObjectId()) != null) {
         _log.warning("User already exists in Object ID map! User " + activeChar.getName() + " is a character clone.");
         this.getClient().closeNow();
      } else {
         this.getClient().setState(GameClient.GameClientState.IN_GAME);
         if (Config.RESTORE_PLAYER_INSTANCE) {
            activeChar.setReflectionId(ReflectionManager.getInstance().getPlayerReflection(activeChar.getObjectId()));
         } else {
            int instanceId = ReflectionManager.getInstance().getPlayerReflection(activeChar.getObjectId());
            if (instanceId > 0) {
               ReflectionManager.getInstance().getReflection(instanceId).removePlayer(activeChar.getObjectId());
            }
         }

         if (!activeChar.isGM()) {
            int pvpAmmount = activeChar.getPvpKills();
            PcAppearance charAppearance = activeChar.getAppearance();
            boolean defaultColor = charAppearance.getNameColor() == 16777215;
            boolean defaultTitle = charAppearance.getTitleColor() == 16777079;
            if (Config.PVP_COLOR_SYSTEM) {
               if (pvpAmmount >= Config.PVP_AMMOUNT1 && pvpAmmount < Config.PVP_AMMOUNT2) {
                  if (defaultColor) {
                     charAppearance.setNameColor(Config.COLOR_FOR_AMMOUNT1);
                  }

                  if (defaultTitle) {
                     charAppearance.setTitleColor(Config.TITLE_COLOR_FOR_AMMOUNT1);
                  }
               } else if (pvpAmmount >= Config.PVP_AMMOUNT2 && pvpAmmount < Config.PVP_AMMOUNT3) {
                  if (defaultColor) {
                     charAppearance.setNameColor(Config.COLOR_FOR_AMMOUNT2);
                  }

                  if (defaultTitle) {
                     charAppearance.setTitleColor(Config.TITLE_COLOR_FOR_AMMOUNT2);
                  }
               } else if (pvpAmmount >= Config.PVP_AMMOUNT3 && pvpAmmount < Config.PVP_AMMOUNT4) {
                  if (defaultColor) {
                     charAppearance.setNameColor(Config.COLOR_FOR_AMMOUNT3);
                  }

                  if (defaultTitle) {
                     charAppearance.setTitleColor(Config.TITLE_COLOR_FOR_AMMOUNT3);
                  }
               } else if (pvpAmmount >= Config.PVP_AMMOUNT4 && pvpAmmount < Config.PVP_AMMOUNT5) {
                  if (defaultColor) {
                     charAppearance.setNameColor(Config.COLOR_FOR_AMMOUNT4);
                  }

                  if (defaultTitle) {
                     charAppearance.setTitleColor(Config.TITLE_COLOR_FOR_AMMOUNT4);
                  }
               } else if (pvpAmmount >= Config.PVP_AMMOUNT5) {
                  if (defaultColor) {
                     charAppearance.setNameColor(Config.COLOR_FOR_AMMOUNT5);
                  }

                  if (defaultTitle) {
                     charAppearance.setTitleColor(Config.TITLE_COLOR_FOR_AMMOUNT5);
                  }
               }
            }
         }

         if (activeChar.isGM()) {
            if (Config.ENABLE_SAFE_ADMIN_PROTECTION) {
               if (Config.SAFE_ADMIN_NAMES.contains(activeChar.getName())) {
                  activeChar.getAdminProtection().setIsSafeAdmin(true);
                  if (Config.SAFE_ADMIN_SHOW_ADMIN_ENTER) {
                     _log.info("Safe Admin: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") has been logged in.");
                  }
               } else {
                  activeChar.getAdminProtection().punishUnSafeAdmin();
                  _log.warning("WARNING: Unsafe Admin: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") as been logged in.");
                  _log.warning("If you have enabled some punishment, He will be punished.");
               }
            }

            if (Config.GM_STARTUP_INVULNERABLE && AdminParser.getInstance().hasAccess("admin_invul", activeChar.getAccessLevel())) {
               activeChar.setIsInvul(true);
            }

            if (Config.GM_STARTUP_INVISIBLE && AdminParser.getInstance().hasAccess("admin_invisible", activeChar.getAccessLevel())) {
               activeChar.setInvisible(true);
            }

            if (Config.GM_STARTUP_SILENCE && AdminParser.getInstance().hasAccess("admin_silence", activeChar.getAccessLevel())) {
               activeChar.setSilenceMode(true);
            }

            if (Config.GM_STARTUP_DIET_MODE && AdminParser.getInstance().hasAccess("admin_diet", activeChar.getAccessLevel())) {
               activeChar.setDietMode(true);
               activeChar.refreshOverloaded();
            }

            if (Config.GM_STARTUP_AUTO_LIST && AdminParser.getInstance().hasAccess("admin_gmliston", activeChar.getAccessLevel())) {
               AdminParser.getInstance().addGm(activeChar, false);
            } else {
               AdminParser.getInstance().addGm(activeChar, true);
            }

            if (Config.GM_GIVE_SPECIAL_SKILLS) {
               SkillTreesParser.getInstance().addSkills(activeChar, false);
            }

            if (Config.GM_GIVE_SPECIAL_AURA_SKILLS) {
               SkillTreesParser.getInstance().addSkills(activeChar, true);
            }

            activeChar.doSimultaneousCast(SkillsParser.getInstance().getInfo(7029, 2));
         }

         long plX = (long)activeChar.getX();
         long plY = (long)activeChar.getY();
         long plZ = (long)activeChar.getZ();
         if (KrateisCubeManager.getInstance().isRegistered(activeChar)) {
            activeChar.setIsInKrateisCube(true);
         } else if (plZ < -8000L && plZ > -8500L && plX > -91326L && plX < -74008L && plY > -91329L && plY < -74231L) {
            activeChar.teleToLocation(-70381, -70937, -1428, true);
         }

         if (activeChar.getCurrentHp() < 0.5) {
            activeChar.setIsDead(true);
         }

         boolean showClanNotice = false;
         Clan clan = activeChar.getClan();
         if (clan != null) {
            activeChar.sendPacket(new PledgeSkillList(clan));
            this.notifyClanMembers(activeChar);
            this.notifySponsorOrApprentice(activeChar);
            AuctionableHall clanHall = ClanHallManager.getInstance().getClanHallByOwner(clan);
            if (clanHall != null && !clanHall.getPaid() && clan.getWarehouse().getAdena() < (long)clanHall.getLease() && !clanHall.getPaid()) {
               activeChar.sendPacket(SystemMessageId.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW);
            }

            for(Siege siege : SiegeManager.getInstance().getSieges()) {
               if (siege.getIsInProgress()) {
                  if (siege.checkIsAttacker(clan)) {
                     activeChar.setSiegeState((byte)1);
                     activeChar.setSiegeSide(siege.getCastle().getId());
                  } else if (siege.checkIsDefender(clan)) {
                     activeChar.setSiegeState((byte)2);
                     activeChar.setSiegeSide(siege.getCastle().getId());
                  }
               }
            }

            for(FortSiege siege : FortSiegeManager.getInstance().getSieges()) {
               if (siege.getIsInProgress()) {
                  if (siege.checkIsAttacker(clan)) {
                     activeChar.setSiegeState((byte)1);
                     activeChar.setSiegeSide(siege.getFort().getId());
                  } else if (siege.checkIsDefender(clan)) {
                     activeChar.setSiegeState((byte)2);
                     activeChar.setSiegeSide(siege.getFort().getId());
                  }
               }
            }

            for(SiegableHall hall : CHSiegeManager.getInstance().getConquerableHalls().values()) {
               if (hall.isInSiege() && hall.isRegistered(clan)) {
                  activeChar.setSiegeState((byte)1);
                  activeChar.setSiegeSide(hall.getId());
                  activeChar.setIsInHideoutSiege(true);
               }
            }

            this.sendPacket(new PledgeShowMemberListAll(clan, activeChar));
            this.sendPacket(new PledgeStatusChanged(clan));
            if (clan.getCastleId() > 0) {
               CastleManager.getInstance().getCastleByOwner(clan).giveResidentialSkills(activeChar);
            }

            if (clan.getFortId() > 0) {
               FortManager.getInstance().getFortByOwner(clan).giveResidentialSkills(activeChar);
            }

            showClanNotice = clan.isNoticeEnabled();
         }

         if (TerritoryWarManager.getInstance().getRegisteredTerritoryId(activeChar) > 0) {
            if (TerritoryWarManager.getInstance().isTWInProgress()) {
               activeChar.setSiegeState((byte)1);
            }

            activeChar.setSiegeSide(TerritoryWarManager.getInstance().getRegisteredTerritoryId(activeChar));
         }

         if (SevenSigns.getInstance().isSealValidationPeriod() && SevenSigns.getInstance().getSealOwner(3) != 0) {
            int cabal = SevenSigns.getInstance().getPlayerCabal(activeChar.getObjectId());
            if (cabal != 0) {
               if (cabal == SevenSigns.getInstance().getSealOwner(3)) {
                  activeChar.addSkill(SkillsParser.FrequentSkill.THE_VICTOR_OF_WAR.getSkill());
               } else {
                  activeChar.addSkill(SkillsParser.FrequentSkill.THE_VANQUISHED_OF_WAR.getSkill());
               }
            }
         } else {
            activeChar.removeSkill(SkillsParser.FrequentSkill.THE_VICTOR_OF_WAR.getSkill());
            activeChar.removeSkill(SkillsParser.FrequentSkill.THE_VANQUISHED_OF_WAR.getSkill());
         }

         if (Config.ENABLE_VITALITY && Config.RECOVER_VITALITY_ON_RECONNECT) {
            float points = Config.RATE_RECOVERY_ON_RECONNECT * (float)(System.currentTimeMillis() - activeChar.getLastAccess()) / 60000.0F;
            if (points > 0.0F) {
               activeChar.updateVitalityPoints((double)points, false, true);
            }
         }

         activeChar._entering = false;
         activeChar.broadcastUserInfo(true);
         activeChar.getMacros().sendUpdate();
         activeChar.sendItemList(false);
         activeChar.sendPacket(new ExBrPremiumState(activeChar.getObjectId(), activeChar.hasPremiumBonus() ? 1 : 0));
         activeChar.checkPlayer();
         activeChar.queryGameGuard();
         this.sendPacket(new ExGetBookMarkInfo(activeChar));
         this.sendPacket(new ShortCutInit(activeChar));
         activeChar.sendPacket(ExBasicActionList.STATIC_PACKET);
         activeChar.sendPacket(new HennaInfo(activeChar));
         Quest.playerEnter(activeChar);
         if (!Config.DISABLE_TUTORIAL) {
            this.loadTutorial(activeChar);
         }

         for(Quest quest : QuestManager.getInstance().getAllManagedScripts()) {
            if (quest != null && quest.getOnEnterWorld()) {
               quest.notifyEnterWorld(activeChar);
            }
         }

         activeChar.sendPacket(new QuestList(activeChar));
         TransferSkillUtils.checkTransferItems(activeChar);
         if (Config.PLAYER_SPAWN_PROTECTION > 0) {
            activeChar.setProtection(true);
         }

         activeChar.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());
         activeChar.getInventory().applyItemSkills();
         if (Config.ALLOW_WEDDING) {
            this.engage(activeChar);
            this.notifyPartner(activeChar, activeChar.getPartnerId());
         }

         if (activeChar.isCursedWeaponEquipped()) {
            CursedWeaponsManager.getInstance().getCursedWeapon(activeChar.getCursedWeaponEquippedId()).cursedOnLogin();
         }

         activeChar.updateEffectIcons();
         if (Config.PC_BANG_ENABLED) {
            boolean blockPacket = Config.PC_BANG_ONLY_FOR_PREMIUM && !activeChar.hasPremiumBonus();
            if (!blockPacket) {
               activeChar.sendPacket(
                  activeChar.getPcBangPoints() > 0 ? new ExPCCafePointInfo(activeChar.getPcBangPoints(), 0, false, false, 1) : new ExPCCafePointInfo()
               );
            }
         }

         activeChar.startTimers();
         activeChar.sendPacket(new EtcStatusUpdate(activeChar));
         activeChar.sendPacket(new ExStorageMaxCount(activeChar));
         this.sendPacket(new L2FriendList(activeChar));
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.FRIEND_S1_HAS_LOGGED_IN);
         sm.addString(activeChar.getName());

         for(int id : activeChar.getFriendList()) {
            GameObject obj = World.getInstance().findObject(id);
            if (obj != null) {
               obj.sendPacket(sm);
            }
         }

         activeChar.sendPacket(SystemMessageId.WELCOME_TO_LINEAGE);
         if (Config.NEW_CHAR_IS_HERO) {
            activeChar.setHero(true, true);
         }

         if (Config.DISPLAY_SERVER_VERSION) {
            activeChar.sendMessage(this.getText("U2VydmVyIFJldmlzaW9uOg==") + "  " + 2210);
            activeChar.sendMessage(this.getText("Q29weXJpZ2h0IMKpIDIwMTAgLSAyMDE5"));
         }

         SevenSigns.getInstance().sendCurrentPeriodMsg(activeChar);
         Announcements.getInstance().showAnnouncements(activeChar);
         if (AutoRestart.getInstance().getRestartNextTime() > 0L) {
            ServerMessage msg = new ServerMessage("AutoRestart.NEXT_TIME", activeChar.getLang());
            msg.add(TimeUtils.formatTime(activeChar, (int)AutoRestart.getInstance().getRestartNextTime(), false));
            CreatureSay msg3 = new CreatureSay(0, 20, ServerStorage.getInstance().getString(activeChar.getLang(), "AutoRestart.TITLE"), msg.toString());
            activeChar.sendPacket(msg3);
         }

         if (showClanNotice) {
            NpcHtmlMessage notice = new NpcHtmlMessage(1);
            notice.setFile(activeChar, activeChar.getLang(), "data/html/clanNotice.htm");
            notice.replace("%clan_name%", activeChar.getClan().getName());
            notice.replace("%notice_text%", activeChar.getClan().getNotice());
            this.sendPacket(notice);
         } else if (Config.SERVER_NEWS) {
            String serverNews = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/servnews.htm");
            if (serverNews != null) {
               this.sendPacket(new NpcHtmlMessage(activeChar, 1, serverNews));
            }
         }

         if (Config.PETITIONING_ALLOWED) {
            PetitionManager.getInstance().checkPetitionMessages(activeChar);
         }

         if (Config.ONLINE_PLAYERS_AT_STARTUP) {
            activeChar.sendMessage(
               "" + ServerStorage.getInstance().getString(activeChar.getLang(), "OnlinePlayers.ONLINE") + " " + World.getInstance().getAllPlayers().size()
            );
         }

         if (activeChar.isAlikeDead()) {
            this.sendPacket(new Die(activeChar));
         }

         if (activeChar.isSitting()) {
            activeChar.sendPacket(new l2e.gameserver.network.serverpackets.ChangeWaitType(activeChar, 0));
         }

         activeChar.sendSkillList(true);
         if (activeChar.getPrivateStoreType() != 0) {
            if (activeChar.getPrivateStoreType() == 3) {
               activeChar.sendPacket(new PrivateStoreBuyMsg(activeChar));
            } else if (activeChar.getPrivateStoreType() == 1) {
               activeChar.sendPacket(new PrivateStoreSellMsg(activeChar));
            } else if (activeChar.getPrivateStoreType() == 8) {
               activeChar.sendPacket(new ExPrivateStorePackageMsg(activeChar));
            } else if (activeChar.getPrivateStoreType() == 5) {
               activeChar.sendPacket(new RecipeShopMsg(activeChar));
            }

            if (activeChar.getVar("offlineBuff") != null) {
               activeChar.setIsSellingBuffs(true);
               activeChar.unsetVar("offlineBuff");
            }
         }

         activeChar.unsetVar("offline");
         this.sendPacket(new ExReceiveShowPostFriend(activeChar));
         activeChar.getNevitSystem().onEnterWorld();
         activeChar.onPlayerEnter();

         for(ItemInstance i : activeChar.getInventory().getItems()) {
            if (i.isTimeLimitedItem()) {
               i.scheduleLifeTimeTask();
            }

            if (i.isShadowItem() && i.isEquipped()) {
               i.decreaseMana(false);
            }

            if (i.isEnergyItem() && i.isEquipped()) {
               i.decreaseEnergy(false);
            }
         }

         for(ItemInstance i : activeChar.getWarehouse().getItems()) {
            if (i.isTimeLimitedItem()) {
               i.scheduleLifeTimeTask();
            }
         }

         if (DimensionalRiftManager.getInstance().checkIfInRiftZone(activeChar.getX(), activeChar.getY(), activeChar.getZ(), false)) {
            DimensionalRiftManager.getInstance().teleportToWaitingRoom(activeChar);
         }

         if (activeChar.getClanJoinExpiryTime() > System.currentTimeMillis()) {
            activeChar.sendPacket(SystemMessageId.CLAN_MEMBERSHIP_TERMINATED);
         }

         if (activeChar.getInventory().getItemByItemId(9819) != null) {
            Fort fort = FortManager.getInstance().getFort(activeChar);
            if (fort != null) {
               FortSiegeManager.getInstance().dropCombatFlag(activeChar, fort.getId());
            } else {
               int slot = activeChar.getInventory().getSlotFromItem(activeChar.getInventory().getItemByItemId(9819));
               activeChar.getInventory().unEquipItemInBodySlot(slot);
               activeChar.destroyItem("CombatFlag", activeChar.getInventory().getItemByItemId(9819), null, true);
            }
         }

         if (!activeChar.canOverrideCond(PcCondOverride.ZONE_CONDITIONS)
            && activeChar.isInsideZone(ZoneId.SIEGE)
            && (!activeChar.isInSiege() || activeChar.getSiegeState() < 2)) {
            ThreadPoolManager.getInstance().schedule(new TeleportToTownTask(activeChar), 2000L);
         }

         if (Config.ALLOW_MAIL && MailManager.getInstance().hasUnreadPost(activeChar)) {
            this.sendPacket(ExNoticePostArrived.valueOf(false));
         }

         if (Config.PROTECTION_IP_ENABLED) {
            ProtectionIP.onEnterWorld(activeChar);
         }

         if (Config.WELCOME_MESSAGE_ENABLED) {
            activeChar.sendPacket(new ExShowScreenMessage(Config.WELCOME_MESSAGE_TEXT, Config.WELCOME_MESSAGE_TIME));
         }

         ClassMasterInstance.showQuestionMark(activeChar);
         int birthday = activeChar.checkBirthDay();
         if (birthday == 0) {
            activeChar.sendPacket(SystemMessageId.YOUR_BIRTHDAY_GIFT_HAS_ARRIVED);
         } else if (birthday != -1) {
            sm = SystemMessage.getSystemMessage(SystemMessageId.THERE_ARE_S1_DAYS_UNTIL_YOUR_CHARACTERS_BIRTHDAY);
            sm.addString(Integer.toString(birthday));
            activeChar.sendPacket(sm);
         }

         if (!activeChar.getPremiumItemList().isEmpty()) {
            activeChar.sendPacket(ExNotifyPremiumItem.STATIC_PACKET);
         }

         if (Hitman.getActive()) {
            Hitman.getEnterWorld(activeChar);
         }

         if (Config.AUTO_GIVE_PREMIUM && !activeChar.hasPremiumBonus()) {
            String var = GlobalVariablesManager.getInstance().getStoredVariable("Premium-" + activeChar.getAccountName());
            if (var == null) {
               PremiumTemplate tpl = PremiumAccountsParser.getInstance().getPremiumTemplate(Config.GIVE_PREMIUM_ID);
               if (tpl != null) {
                  long time = !tpl.isOnlineType() ? System.currentTimeMillis() + tpl.getTime() * 1000L : 0L;
                  if (tpl.isPersonal()) {
                     CharacterPremiumDAO.getInstance().updatePersonal(activeChar, tpl.getId(), time);
                  } else {
                     CharacterPremiumDAO.getInstance().update(activeChar, tpl.getId(), time);
                  }
               }
            }
         }

         if (activeChar.getUseAutoHpPotions()) {
            activeChar.startHpPotionTask();
         }

         if (activeChar.getUseAutoMpPotions()) {
            activeChar.startMpPotionTask();
         }

         if (activeChar.getUseAutoCpPotions()) {
            activeChar.startCpPotionTask();
         }

         if (activeChar.getUseAutoSoulPotions()) {
            activeChar.startSoulPotionTask();
         }

         if (Config.ALLOW_SECURITY_COMMAND) {
            this.checkSecurity(activeChar);
         }

         if (Config.ALLOW_DAILY_REWARD) {
            activeChar.restoreDailyRewards();
         }

         if (Config.ALLOW_DAILY_TASKS) {
            DailyTasksDAO.getInstance().restoreTasksCount(activeChar);
            DailyTasksDAO.getInstance().restoreDailyTasks(activeChar);
         }

         if (Config.ALLOW_REVENGE_SYSTEM) {
            activeChar.getRevengeMark();
         }

         activeChar.sendVoteSystemInfo();
         activeChar.sendActionFailed();
         activeChar.getFarmSystem().restoreVariables();
         OnlineRewardManager.getInstance().checkOnlineReward(activeChar);
      }
   }

   private void checkSecurity(Player activeChar) {
      boolean disconnect = false;
      if (Config.ALLOW_IP_LOCK
         && activeChar.getVar("lockIp") != null
         && !activeChar.getVar("lockIp").equalsIgnoreCase("0")
         && !activeChar.getVar("lockIp").equalsIgnoreCase(activeChar.getIPAddress())) {
         activeChar.sendMessage(new ServerMessage("Security.LOCK_BY_IP", activeChar.getLang()).toString());
         activeChar.sendPacket(
            new ExShowScreenMessage(ServerStorage.getInstance().getString(activeChar.getLang(), "Security.LOCK_BY_IP"), 30000, (byte)2, true)
         );
         disconnect = true;
      }

      if (Config.ALLOW_HWID_LOCK
         && activeChar.getVar("lockHwid") != null
         && !activeChar.getVar("lockHwid").equalsIgnoreCase("0")
         && !activeChar.getVar("lockHwid").equalsIgnoreCase(activeChar.getHWID())) {
         activeChar.sendMessage(new ServerMessage("Security.LOCK_BY_HWID", activeChar.getLang()).toString());
         activeChar.sendPacket(
            new ExShowScreenMessage(ServerStorage.getInstance().getString(activeChar.getLang(), "Security.LOCK_BY_HWID"), 30000, (byte)2, true)
         );
         disconnect = true;
      }

      if (disconnect) {
         ThreadPoolManager.getInstance().schedule(() -> {
            if (activeChar.getClient() != null) {
               activeChar.getClient().closeNow();
            } else {
               activeChar.deleteMe();
            }
         }, (long)(Config.PUNISH_LOCK_DELAY * 1000));
      }
   }

   private void engage(Player cha) {
      int chaId = cha.getObjectId();

      for(Couple cl : CoupleManager.getInstance().getCouples()) {
         if (cl.getPlayer1Id() == chaId || cl.getPlayer2Id() == chaId) {
            if (cl.getMaried()) {
               cha.setMarried(true);
            }

            cha.setCoupleId(cl.getId());
            if (cl.getPlayer1Id() == chaId) {
               cha.setPartnerId(cl.getPlayer2Id());
            } else {
               cha.setPartnerId(cl.getPlayer1Id());
            }
         }
      }
   }

   private void notifyPartner(Player cha, int partnerId) {
      int objId = cha.getPartnerId();
      if (objId != 0) {
         Player partner = World.getInstance().getPlayer(objId);
         if (partner != null) {
            partner.sendMessage("Your Partner has logged in.");
         }
      }
   }

   private void notifyClanMembers(Player activeChar) {
      Clan clan = activeChar.getClan();
      if (clan != null && clan.getClanMember(activeChar.getObjectId()) != null) {
         clan.getClanMember(activeChar.getObjectId()).setPlayerInstance(activeChar);
         SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_LOGGED_IN);
         msg.addString(activeChar.getName());
         clan.broadcastToOtherOnlineMembers(msg, activeChar);
         clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(activeChar), activeChar);
      }
   }

   private void notifySponsorOrApprentice(Player activeChar) {
      if (activeChar.getSponsor() != 0) {
         Player sponsor = World.getInstance().getPlayer(activeChar.getSponsor());
         if (sponsor != null) {
            SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.YOUR_APPRENTICE_S1_HAS_LOGGED_IN);
            msg.addString(activeChar.getName());
            sponsor.sendPacket(msg);
         }
      } else if (activeChar.getApprentice() != 0) {
         Player apprentice = World.getInstance().getPlayer(activeChar.getApprentice());
         if (apprentice != null) {
            SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.YOUR_SPONSOR_C1_HAS_LOGGED_IN);
            msg.addString(activeChar.getName());
            apprentice.sendPacket(msg);
         }
      }
   }

   private String getText(String string) {
      return new String(Base64.decode(string));
   }

   private void loadTutorial(Player player) {
      QuestState qs = player.getQuestState("_255_Tutorial");
      if (qs != null) {
         qs.getQuest().notifyEvent("UC", null, player);
      }
   }

   @Override
   protected boolean triggersOnActionRequest() {
      return false;
   }
}
