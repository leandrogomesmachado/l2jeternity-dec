package l2e.gameserver.network;

public enum ServerPacketOpcodes {
   Die(0),
   Revive(1),
   AttackOutOfRange(2),
   AttackinCoolTime(3),
   AttackDeadTarget(4),
   SpawnItem(5),
   DeleteObject(8),
   CharacterSelectionInfo(9),
   LoginFail(10),
   CharacterSelected(11),
   NpcInfo(12),
   NewCharacterSuccess(13),
   NewCharacterFail(14),
   CharacterCreateSuccess(15),
   CharacterCreateFail(16),
   ItemList(17),
   SunRise(18),
   SunSet(19),
   TradeStart(20),
   TradeStartOk(21),
   DropItem(22),
   GetItem(23),
   StatusUpdate(24),
   NpcHtmlMessage(25),
   TradeOwnAdd(26),
   TradeOtherAdd(27),
   TradeDone(28),
   CharacterDeleteSuccess(29),
   CharacterDeleteFail(30),
   ActionFail(31),
   ServerClose(32),
   InventoryUpdate(33),
   TeleportToLocation(34),
   TargetSelected(35),
   TargetUnselected(36),
   AutoAttackStart(37),
   AutoAttackStop(38),
   SocialAction(39),
   ChangeMoveType(40),
   ChangeWaitType(41),
   ManagePledgePower(42),
   CreatePledge(43),
   AskJoinPledge(44),
   JoinPledge(45),
   VersionCheck(46),
   MoveToLocation(47),
   NpcSay(48),
   CharInfo(49),
   UserInfo(50),
   Attack(51),
   WithdrawalPledge(52),
   OustPledgeMember(53),
   SetOustPledgeMember(54),
   DismissPledge(55),
   SetDismissPledge(56),
   AskJoinParty(57),
   JoinParty(58),
   WithdrawalParty(59),
   OustPartyMember(60),
   SetOustPartyMember(61),
   DismissParty(62),
   SetDismissParty(63),
   UserAck(64),
   WareHouseDepositList(65),
   WareHouseWithdrawList(66),
   WareHouseDone(67),
   ShortCutRegister(68),
   ShortCutInit(69),
   ShortCutDelete(70),
   StopMove(71),
   MagicSkillUse(72),
   MagicSkillCanceled(73),
   CreatureSay(74),
   EquipUpdate(75),
   DoorInfo(76),
   DoorStatusUpdate(77),
   PartySmallWindowAll(78),
   PartySmallWindowAdd(79),
   PartySmallWindowDeleteAll(80),
   PartySmallWindowDelete(81),
   PartySmallWindowUpdate(82),
   TradePressOwnOk(83),
   MagicSkillLaunched(84),
   FriendAddRequestResult(85),
   FriendAdd(86),
   FriendRemove(87),
   FriendList(88),
   FriendStatus(89),
   PledgeShowMemberListAll(90),
   PledgeShowMemberListUpdate(91),
   PledgeShowMemberListAdd(92),
   PledgeShowMemberListDelete(93),
   MagicList(94),
   SkillList(95),
   VehicleInfo(96),
   FinishRotatings(97),
   SystemMessage(98),
   StartPledgeWar(99),
   ReplyStartPledgeWar(100),
   StopPledgeWar(101),
   ReplyStopPledgeWar(102),
   SurrenderPledgeWar(103),
   ReplySurrenderPledgeWar(104),
   SetPledgeCrest(105),
   PledgeCrest(106),
   SetupGauge(107),
   VehicleDeparture(108),
   VehicleCheckLocation(109),
   GetOnVehicle(110),
   GetOffVehicle(111),
   TradeRequest(112),
   RestartResponse(113),
   MoveToPawn(114),
   SSQInfo(115),
   GameGuardQuery(116),
   L2FriendList(117),
   L2Friend(118),
   L2FriendStatus(119),
   L2FriendSay(120),
   ValidateLocation(121),
   StartRotation(122),
   ShowBoard(123),
   ChooseInventoryItem(124),
   Dummy_7D(125),
   Dummy_8D(255),
   MoveToLocationInVehicle(126),
   StopMoveInVehicle(127),
   ValidateLocationInVehicle(128),
   TradeUpdate(129),
   TradePressOtherOk(130),
   FriendAddRequest(131),
   LogOutOk(132),
   AbnormalStatusUpdate(133),
   QuestList(134),
   EnchantResult(135),
   PledgeShowMemberListDeleteAll(136),
   PledgeInfo(137),
   PledgeExtendedInfo(138),
   SurrenderPersonally(139),
   Ride(140),
   GiveNickNameDone(141),
   PledgeShowInfoUpdate(142),
   ClientAction(143),
   AcquireSkillList(144),
   AcquireSkillInfo(145),
   ServerObjectInfo(146),
   GMHide(147),
   AcquireSkillDone(148),
   GMViewCharacterInfo(149),
   GMViewPledgeInfo(150),
   GMViewSkillInfo(151),
   GMViewMagicInfo(152),
   GmViewQuestInfo(153),
   GMViewItemList(154),
   GMViewWarehouseWithdrawList(155),
   ListPartyWaiting(156),
   PartyRoomInfo(157),
   PlaySound(158),
   StaticObject(159),
   PrivateStoreSellManageList(160),
   PrivateStoreSellList(161),
   PrivateStoreSellMsg(162),
   ShowMiniMap(163),
   ReviveRequest(164),
   AbnormalVisualEffect(165),
   TutorialShowHtml(166),
   ShowTutorialMark(167),
   TutorialEnableClientEvent(168),
   TutorialCloseHtml(169),
   ShowRadar(170),
   WithdrawAlliance(171),
   OustAllianceMemberPledge(172),
   DismissAlliance(173),
   SetAllianceCrest(174),
   AllianceCrest(175),
   ServerCloseSocket(176),
   PetStatusShow(177),
   PetInfo(178),
   PetItemList(179),
   PetInventoryUpdate(180),
   AllianceInfo(181),
   PetStatusUpdate(182),
   PetDelete(183),
   DeleteRadar(184),
   MyTargetSelected(185),
   PartyMemberPosition(186),
   AskJoinAlliance(187),
   JoinAlliance(188),
   PrivateStoreBuyManageList(189),
   PrivateStoreBuyList(190),
   PrivateStoreBuyMsg(191),
   VehicleStarted(192),
   RequestTimeCheck(193),
   StartAllianceWar(194),
   ReplyStartAllianceWar(195),
   StopAllianceWar(196),
   ReplyStopAllianceWar(197),
   SurrenderAllianceWar(198),
   SkillCoolTime(199),
   PackageToList(200),
   CastleSiegeInfo(201),
   CastleSiegeAttackerList(202),
   CastleSiegeDefenderList(203),
   NickNameChanged(204),
   PledgeStatusChanged(205),
   RelationChanged(206),
   EventTrigger(207),
   MultiSellList(208),
   SetSummonRemainTime(209),
   PackageSendableList(210),
   EarthQuake(211),
   FlyToLocation(212),
   BlockList(213),
   SpecialCamera(214),
   NormalCamera(215),
   SkillRemainSec(216),
   NetPing(217),
   Dice(218),
   Snoop(219),
   RecipeBookItemList(220),
   RecipeItemMakeInfo(221),
   RecipeShopManageList(222),
   RecipeShopSellList(223),
   RecipeShopItemInfo(224),
   RecipeShopMsg(225),
   ShowCalc(226),
   MonRaceInfo(227),
   HennaItemInfo(228),
   HennaInfo(229),
   HennaUnequipList(230),
   HennaUnequipInfo(231),
   MacrosList(232),
   BuyListSeed(233),
   ShowTownMap(234),
   ObserverStart(235),
   ObserverEnd(236),
   ChairSit(237),
   HennaEquipList(238),
   SellListProcure(239),
   GMHennaInfo(240),
   RadarControl(241),
   ClientSetTime(242),
   ConfirmDlg(243),
   PartySpelled(244),
   ShopPreviewList(245),
   ShopPreviewInfo(246),
   CameraMode(247),
   ShowXMasSeal(248),
   EtcStatusUpdate(249),
   ShortBuffStatusUpdate(250),
   SSQStatus(251),
   PetitionVote(252),
   AgitDecoInfo(253),
   ExRegenMax(254, 1),
   ExEventMatchUserInfo(254, 2),
   ExColosseumFenceInfo(254, 3),
   ExEventMatchSpelledInfo(254, 4),
   ExEventMatchFirecracker(254, 5),
   ExEventMatchTeamUnlocked(254, 6),
   ExEventMatchGMTest(254, 7),
   ExPartyRoomMember(254, 8),
   ExClosePartyRoom(254, 9),
   ExManagePartyRoomMember(254, 10),
   ExEventMatchLockResult(254, 11),
   ExAutoSoulShot(254, 12),
   ExEventMatchList(254, 173),
   ExEventMatchObserver(254, 14),
   ExEventMatchMessage(254, 15),
   ExEventMatchScore(254, 16),
   ExServerPrimitive(254, 17),
   ExOpenMPCC(254, 18),
   ExCloseMPCC(254, 19),
   ExShowCastleInfo(254, 20),
   ExShowFortressInfo(254, 21),
   ExShowAgitInfo(254, 22),
   ExShowFortressSiegeInfo(254, 23),
   ExPartyPetWindowAdd(254, 24),
   ExPartyPetWindowUpdate(254, 25),
   ExAskJoinMPCC(254, 26),
   ExPledgeEmblem(254, 27),
   ExEventMatchTeamInfo(254, 28),
   ExEventMatchCreate(254, 29),
   ExFishingStart(254, 30),
   ExFishingEnd(254, 31),
   ExShowQuestInfo(254, 32),
   ExShowQuestMark(254, 33),
   ExSendManorList(254, 34),
   ExShowSeedInfo(254, 35),
   ExShowCropInfo(254, 36),
   ExShowManorDefaultInfo(254, 37),
   ExShowSeedSetting(254, 38),
   ExFishingStartCombat(254, 39),
   ExFishingHpRegen(254, 40),
   ExEnchantSkillList(254, 41),
   ExEnchantSkillInfo(254, 42),
   ExShowCropSetting(254, 43),
   ExShowSellCropList(254, 44),
   ExOlympiadMatchEnd(254, 45),
   ExMailArrived(254, 46),
   ExStorageMaxCount(254, 47),
   ExEventMatchManage(254, 48),
   ExMultiPartyCommandChannelInfo(254, 49),
   ExPCCafePointInfo(254, 50),
   ExSetCompassZoneCode(254, 51),
   ExGetBossRecord(254, 52),
   ExAskJoinPartyRoom(254, 53),
   ExListPartyMatchingWaitingRoom(254, 54),
   ExSetMpccRouting(254, 55),
   ExShowAdventurerGuideBook(254, 56),
   ExShowScreenMessage(254, 57),
   PledgeSkillList(254, 58),
   PledgeSkillListAdd(254, 59),
   PledgePowerGradeList(254, 60),
   PledgeReceivePowerInfo(254, 61),
   PledgeReceiveMemberInfo(254, 62),
   PledgeReceiveWarList(254, 63),
   PledgeReceiveSubPledgeCreated(254, 64),
   ExRedSky(254, 65),
   PledgeReceiveUpdatePower(254, 66),
   FlySelfDestination(254, 67),
   ShowPCCafeCouponShowUI(254, 68),
   ExSearchOrc(254, 69),
   ExCursedWeaponList(254, 70),
   ExCursedWeaponLocation(254, 71),
   ExRestartClient(254, 72),
   ExRequestHackShield(254, 73),
   ExUseSharedGroupItem(254, 74),
   ExMPCCShowPartyMemberInfo(254, 75),
   ExDuelAskStart(254, 76),
   ExDuelReady(254, 77),
   ExDuelStart(254, 78),
   ExDuelEnd(254, 79),
   ExDuelUpdateUserInfo(254, 80),
   ExShowVariationMakeWindow(254, 81),
   ExShowVariationCancelWindow(254, 82),
   ExPutItemResultForVariationMake(254, 83),
   ExPutIntensiveResultForVariationMake(254, 84),
   ExPutCommissionResultForVariationMake(254, 85),
   ExVariationResult(254, 86),
   ExPutItemResultForVariationCancel(254, 87),
   ExVariationCancelResult(254, 88),
   ExDuelEnemyRelation(254, 89),
   ExPlayAnimation(254, 90),
   ExMPCCPartyInfoUpdate(254, 91),
   ExPlayScene(254, 92),
   ExSpawnEmitter(254, 93),
   ExEnchantSkillInfoDetail(254, 94),
   ExBasicActionList(254, 95),
   ExAirShipInfo(254, 96),
   ExAttributeEnchantResult(254, 97),
   ExChooseInventoryAttributeItem(254, 98),
   ExGetOnAirShip(254, 99),
   ExGetOffAirShip(254, 100),
   ExMoveToLocationAirShip(254, 101),
   ExStopMoveAirShip(254, 102),
   ExShowTrace(254, 103),
   ExItemAuctionInfo(254, 104),
   ExNeedToChangeName(254, 105),
   ExPartyPetWindowDelete(254, 106),
   ExTutorialList(254, 107),
   ExRpItemLink(254, 108),
   ExMoveToLocationInAirShip(254, 109),
   ExStopMoveInAirShip(254, 110),
   ExValidateLocationInAirShip(254, 111),
   ExUISetting(254, 112),
   ExMoveToTargetInAirShip(254, 113),
   ExAttackInAirShip(254, 114),
   ExMagicSkillUseInAirShip(254, 115),
   ExShowBaseAttributeCancelWindow(254, 116),
   ExBaseAttributeCancelResult(254, 117),
   ExSubPledgeSkillAdd(254, 118),
   ExResponseFreeServer(254, 119),
   ExShowProcureCropDetail(254, 120),
   ExHeroList(254, 121),
   ExOlympiadUserInfo(254, 122),
   ExOlympiadSpelledInfo(254, 123),
   ExOlympiadMode(254, 124),
   ExShowFortressMapInfo(254, 125),
   ExPVPMatchRecord(254, 126),
   ExPVPMatchUserDie(254, 127),
   ExPrivateStorePackageMsg(254, 128),
   ExPutEnchantTargetItemResult(254, 129),
   ExPutEnchantSupportItemResult(254, 130),
   ExRequestChangeNicknameColor(254, 131),
   ExGetBookMarkInfo(254, 132),
   ExNotifyPremiumItem(254, 133),
   ExGetPremiumItemList(254, 134),
   ExPeriodicItemList(254, 135),
   ExJumpToLocation(254, 136),
   ExPVPMatchCCRecord(254, 137),
   ExPVPMatchCCMyRecord(254, 138),
   ExPVPMatchCCRetire(254, 139),
   ExShowTerritory(254, 140),
   ExNpcQuestHtmlMessage(254, 141),
   ExSendUIEvent(254, 142),
   ExNotifyBirthDay(254, 143),
   ExShowDominionRegistry(254, 144),
   ExReplyRegisterDominion(254, 145),
   ExReplyDominionInfo(254, 146),
   ExShowOwnthingPos(254, 147),
   ExCleftList(254, 148),
   ExCleftState(254, 149),
   ExDominionChannelSet(254, 150),
   ExBlockUpSetList(254, 151),
   ExBlockUpSetState(254, 152),
   ExStartScenePlayer(254, 153),
   ExAirShipTeleportList(254, 154),
   ExMpccRoomInfo(254, 155),
   ExListMpccWaiting(254, 156),
   ExDissmissMpccRoom(254, 157),
   ExManageMpccRoomMember(254, 158),
   ExMpccRoomMember(254, 159),
   ExVitalityPointInfo(254, 160),
   ExShowSeedMapInfo(254, 161),
   ExMpccPartymasterList(254, 162),
   ExDominionWarStart(254, 163),
   ExDominionWarEnd(254, 164),
   ExShowLines(254, 165),
   ExPartyMemberRenamed(254, 166),
   ExEnchantSkillResult(254, 167),
   ExRefundList(254, 168),
   ExNoticePostArrived(254, 169),
   ExShowReceivedPostList(254, 170),
   ExReplyReceivedPost(254, 171),
   ExShowSentPostList(254, 172),
   ExReplySentPost(254, 173),
   ExResponseShowStepOne(254, 174),
   ExResponseShowStepTwo(254, 175),
   ExResponseShowContents(254, 176),
   ExShowPetitionHtml(254, 177),
   ExReplyPostItemList(254, 178),
   ExChangePostState(254, 179),
   ExNoticePostSent(254, 180),
   ExInitializeSeed(254, 181),
   ExRaidReserveResult(254, 182),
   ExBuySellList(254, 183),
   ExCloseRaidSocket(254, 184),
   ExPrivateMarketList(254, 185),
   ExRaidCharacterSelected(254, 186),
   ExAskCoupleAction(254, 187),
   ExBrBroadcastEventState(254, 188),
   ExBrLoadEventTopRankers(254, 189),
   ExChangeNpcState(254, 190),
   ExAskModifyPartyLooting(254, 191),
   ExSetPartyLooting(254, 192),
   ExRotation(254, 193),
   ExChangeClientEffectInfo(254, 194),
   ExMembershipInfo(254, 195),
   ExReplyHandOverPartyMaster(254, 196),
   ExQuestNpcLogList(254, 197),
   ExQuestItemList(254, 198),
   ExGMViewQuestItemList(254, 199),
   ExRestartResponse(254, 200),
   ExVoteSystemInfo(254, 201),
   ExShuttleInfo(254, 202),
   ExSuttleGetOn(254, 203),
   ExSuttleGetOff(254, 204),
   ExSuttleMove(254, 205),
   ExMoveToLocationInSuttle(254, 206),
   ExStopMoveInShuttle(254, 207),
   ExValidateLocationInShuttle(254, 208),
   ExAgitAuctionCmd(254, 209),
   ExConfirmAddingPostFriend(254, 210),
   ExReceiveShowPostFriend(254, 211),
   ExReceiveOlympiadList(254, 212),
   ExBrGamePoint(254, 213),
   ExBrProductList(254, 214),
   ExBrProductInfo(254, 215),
   ExBrBuyProduct(254, 216),
   ExBrPremiumState(254, 217),
   ExBrExtraUserInfo(254, 218),
   ExBrBuffEventState(254, 219),
   ExBrRecentProductList(254, 220),
   ExBrMiniGameLoadScores(254, 221),
   ExBrAgathionEnergyInfo(254, 222),
   ExNavitAdventPointInfo(254, 223),
   ExNevitAdventEffect(254, 224),
   ExNevitAdventTimeChange(254, 225),
   ExGoodsInventoryChangedNotify(254, 226),
   ExGoodsInventoryInfo(254, 227),
   ExGoodsInventoryResult(254, 228),
   Ex2ndPasswordCheck(254, 229),
   Ex2ndPasswordVerify(254, 230),
   Ex2ndPasswordAck(254, 231),
   ExSay2Fail(254, 232),
   KeyPacket(254, 245),
   ConfigPacket(254, 246),
   OnScreenMsg(254, 57),
   InterfaceConfigPacket(254, 246),
   InterfaceCustomFontsPacket(254, 233),
   InterfaceKeyPacket(254, 245),
   InterfaceScreenTextInfoPacket(254, 234);

   public static final ServerPacketOpcodes[] VALUES = values();
   private int _id;
   private int _exId;

   private ServerPacketOpcodes(int id, int exId) {
      this._id = id;
      this._exId = exId;
   }

   private ServerPacketOpcodes(int id) {
      this(id, -1);
   }

   public int getId() {
      return this._id;
   }

   public int getExId() {
      return this._exId;
   }
}