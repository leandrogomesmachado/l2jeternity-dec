package l2e.gameserver.network;

import java.nio.ByteBuffer;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.network.clientpackets.Action;
import l2e.gameserver.network.clientpackets.AnswerJoinPartyRoom;
import l2e.gameserver.network.clientpackets.AnswerTradeRequest;
import l2e.gameserver.network.clientpackets.BypassUserCmd;
import l2e.gameserver.network.clientpackets.CanNotMoveAnymore;
import l2e.gameserver.network.clientpackets.CanNotMoveAnymoreInVehicle;
import l2e.gameserver.network.clientpackets.ChangeMoveType;
import l2e.gameserver.network.clientpackets.ChangeWaitType;
import l2e.gameserver.network.clientpackets.ConfirmDlg;
import l2e.gameserver.network.clientpackets.FinishRotating;
import l2e.gameserver.network.clientpackets.GMSnoopEnd;
import l2e.gameserver.network.clientpackets.MoveBackwardToLocation;
import l2e.gameserver.network.clientpackets.MoveWithDelta;
import l2e.gameserver.network.clientpackets.NetPing;
import l2e.gameserver.network.clientpackets.PetitionVote;
import l2e.gameserver.network.clientpackets.ReplyGameGuardQuery;
import l2e.gameserver.network.clientpackets.ReplyStopAllianceWar;
import l2e.gameserver.network.clientpackets.RequestAcquireSkill;
import l2e.gameserver.network.clientpackets.RequestAcquireSkillInfo;
import l2e.gameserver.network.clientpackets.RequestActionUse;
import l2e.gameserver.network.clientpackets.RequestAddExpandQuestAlarm;
import l2e.gameserver.network.clientpackets.RequestAddTradeItem;
import l2e.gameserver.network.clientpackets.RequestAllAgitInfo;
import l2e.gameserver.network.clientpackets.RequestAllCastleInfo;
import l2e.gameserver.network.clientpackets.RequestAllFortressInfo;
import l2e.gameserver.network.clientpackets.RequestAllyCrest;
import l2e.gameserver.network.clientpackets.RequestAllyInfo;
import l2e.gameserver.network.clientpackets.RequestAnswerCoupleAction;
import l2e.gameserver.network.clientpackets.RequestAnswerJoinAlly;
import l2e.gameserver.network.clientpackets.RequestAnswerJoinParty;
import l2e.gameserver.network.clientpackets.RequestAnswerJoinPledge;
import l2e.gameserver.network.clientpackets.RequestAnswerPartyLootingModify;
import l2e.gameserver.network.clientpackets.RequestAskJoinPartyRoom;
import l2e.gameserver.network.clientpackets.RequestAskMemberShip;
import l2e.gameserver.network.clientpackets.RequestAttack;
import l2e.gameserver.network.clientpackets.RequestAutoSoulShot;
import l2e.gameserver.network.clientpackets.RequestBBSwrite;
import l2e.gameserver.network.clientpackets.RequestBidItemAuction;
import l2e.gameserver.network.clientpackets.RequestBlock;
import l2e.gameserver.network.clientpackets.RequestBookMarkSlotInfo;
import l2e.gameserver.network.clientpackets.RequestBrBuyProduct;
import l2e.gameserver.network.clientpackets.RequestBrGamePoint;
import l2e.gameserver.network.clientpackets.RequestBrLectureMark;
import l2e.gameserver.network.clientpackets.RequestBrMiniGameInsertScore;
import l2e.gameserver.network.clientpackets.RequestBrMiniGameLoadScores;
import l2e.gameserver.network.clientpackets.RequestBrProductInfo;
import l2e.gameserver.network.clientpackets.RequestBrProductList;
import l2e.gameserver.network.clientpackets.RequestBrRecentProductList;
import l2e.gameserver.network.clientpackets.RequestBuyItem;
import l2e.gameserver.network.clientpackets.RequestBuySeed;
import l2e.gameserver.network.clientpackets.RequestBuySellUIClose;
import l2e.gameserver.network.clientpackets.RequestBypassToServer;
import l2e.gameserver.network.clientpackets.RequestCancelSentPost;
import l2e.gameserver.network.clientpackets.RequestCannotMoveAnymoreAirShip;
import l2e.gameserver.network.clientpackets.RequestCastleSiegeAttackerList;
import l2e.gameserver.network.clientpackets.RequestCastleSiegeDefenderList;
import l2e.gameserver.network.clientpackets.RequestCastleSiegeInfo;
import l2e.gameserver.network.clientpackets.RequestChangeBookMarkSlot;
import l2e.gameserver.network.clientpackets.RequestChangeNicknameColor;
import l2e.gameserver.network.clientpackets.RequestChangePetName;
import l2e.gameserver.network.clientpackets.RequestCharacterCreate;
import l2e.gameserver.network.clientpackets.RequestCharacterDelete;
import l2e.gameserver.network.clientpackets.RequestCharacterRestore;
import l2e.gameserver.network.clientpackets.RequestConfirmCancelItem;
import l2e.gameserver.network.clientpackets.RequestConfirmCastleSiegeWaitingList;
import l2e.gameserver.network.clientpackets.RequestConfirmGemStone;
import l2e.gameserver.network.clientpackets.RequestConfirmRefinerItem;
import l2e.gameserver.network.clientpackets.RequestConfirmTargetItem;
import l2e.gameserver.network.clientpackets.RequestCreatePledge;
import l2e.gameserver.network.clientpackets.RequestCrystallizeItem;
import l2e.gameserver.network.clientpackets.RequestCursedWeaponList;
import l2e.gameserver.network.clientpackets.RequestCursedWeaponLocation;
import l2e.gameserver.network.clientpackets.RequestDeleteBookMarkSlot;
import l2e.gameserver.network.clientpackets.RequestDeleteMacro;
import l2e.gameserver.network.clientpackets.RequestDeleteReceivedPost;
import l2e.gameserver.network.clientpackets.RequestDeleteSentPost;
import l2e.gameserver.network.clientpackets.RequestDestroyItem;
import l2e.gameserver.network.clientpackets.RequestDestroyQuest;
import l2e.gameserver.network.clientpackets.RequestDismissAlly;
import l2e.gameserver.network.clientpackets.RequestDismissMpccRoom;
import l2e.gameserver.network.clientpackets.RequestDismissParty;
import l2e.gameserver.network.clientpackets.RequestDismissPartyRoom;
import l2e.gameserver.network.clientpackets.RequestDismissPledge;
import l2e.gameserver.network.clientpackets.RequestDispel;
import l2e.gameserver.network.clientpackets.RequestDominionInfo;
import l2e.gameserver.network.clientpackets.RequestDropItem;
import l2e.gameserver.network.clientpackets.RequestDuelAnswerStart;
import l2e.gameserver.network.clientpackets.RequestDuelStart;
import l2e.gameserver.network.clientpackets.RequestDuelSurrender;
import l2e.gameserver.network.clientpackets.RequestEnchantItem;
import l2e.gameserver.network.clientpackets.RequestEndScenePlayer;
import l2e.gameserver.network.clientpackets.RequestEnterWorld;
import l2e.gameserver.network.clientpackets.RequestEquipItem;
import l2e.gameserver.network.clientpackets.RequestEventMatchObserverEnd;
import l2e.gameserver.network.clientpackets.RequestEx2ndPasswordCheck;
import l2e.gameserver.network.clientpackets.RequestEx2ndPasswordReq;
import l2e.gameserver.network.clientpackets.RequestEx2ndPasswordVerify;
import l2e.gameserver.network.clientpackets.RequestExAcceptJoinMPCC;
import l2e.gameserver.network.clientpackets.RequestExAddPostFriendForPostBox;
import l2e.gameserver.network.clientpackets.RequestExAgitDetailInfo;
import l2e.gameserver.network.clientpackets.RequestExAgitInitialize;
import l2e.gameserver.network.clientpackets.RequestExAgitListForBid;
import l2e.gameserver.network.clientpackets.RequestExAgitListForLot;
import l2e.gameserver.network.clientpackets.RequestExApplyForAgitLotStep1;
import l2e.gameserver.network.clientpackets.RequestExApplyForAgitLotStep2;
import l2e.gameserver.network.clientpackets.RequestExApplyForBidStep1;
import l2e.gameserver.network.clientpackets.RequestExApplyForBidStep2;
import l2e.gameserver.network.clientpackets.RequestExApplyForBidStep3;
import l2e.gameserver.network.clientpackets.RequestExAskJoinMPCC;
import l2e.gameserver.network.clientpackets.RequestExBlockGameEnter;
import l2e.gameserver.network.clientpackets.RequestExBlockGameVote;
import l2e.gameserver.network.clientpackets.RequestExBrEventRankerList;
import l2e.gameserver.network.clientpackets.RequestExCancelEnchantItem;
import l2e.gameserver.network.clientpackets.RequestExChangeName;
import l2e.gameserver.network.clientpackets.RequestExCleftEnter;
import l2e.gameserver.network.clientpackets.RequestExConfirmCancelAgitLot;
import l2e.gameserver.network.clientpackets.RequestExConfirmCancelRegisteringAgit;
import l2e.gameserver.network.clientpackets.RequestExConnectToRaidServer;
import l2e.gameserver.network.clientpackets.RequestExDeletePostFriendForPostBox;
import l2e.gameserver.network.clientpackets.RequestExEnchantItemAttribute;
import l2e.gameserver.network.clientpackets.RequestExEnchantSkill;
import l2e.gameserver.network.clientpackets.RequestExEnchantSkillChange;
import l2e.gameserver.network.clientpackets.RequestExEnchantSkillInfo;
import l2e.gameserver.network.clientpackets.RequestExEnchantSkillInfoDetail;
import l2e.gameserver.network.clientpackets.RequestExEnchantSkillSafe;
import l2e.gameserver.network.clientpackets.RequestExEnchantSkillUntrain;
import l2e.gameserver.network.clientpackets.RequestExFishRanking;
import l2e.gameserver.network.clientpackets.RequestExFriendListForPostBox;
import l2e.gameserver.network.clientpackets.RequestExGetOffAirShip;
import l2e.gameserver.network.clientpackets.RequestExGetOnAirShip;
import l2e.gameserver.network.clientpackets.RequestExMPCCShowPartyMembersInfo;
import l2e.gameserver.network.clientpackets.RequestExMagicSkillUseGround;
import l2e.gameserver.network.clientpackets.RequestExMyAgitState;
import l2e.gameserver.network.clientpackets.RequestExOlympiadMatchListRefresh;
import l2e.gameserver.network.clientpackets.RequestExOrcMove;
import l2e.gameserver.network.clientpackets.RequestExOustFromMPCC;
import l2e.gameserver.network.clientpackets.RequestExPledgeCrestLarge;
import l2e.gameserver.network.clientpackets.RequestExProceedCancelAgitLot;
import l2e.gameserver.network.clientpackets.RequestExProceedCancelRegisteringAgit;
import l2e.gameserver.network.clientpackets.RequestExReBid;
import l2e.gameserver.network.clientpackets.RequestExRegisterAgitForBidStep1;
import l2e.gameserver.network.clientpackets.RequestExRegisterAgitForBidStep3;
import l2e.gameserver.network.clientpackets.RequestExRemoveItemAttribute;
import l2e.gameserver.network.clientpackets.RequestExReturnFromRaidServer;
import l2e.gameserver.network.clientpackets.RequestExRqItemLink;
import l2e.gameserver.network.clientpackets.RequestExSetPledgeCrestLarge;
import l2e.gameserver.network.clientpackets.RequestExSetTutorial;
import l2e.gameserver.network.clientpackets.RequestExShowPostFriendListForPostBox;
import l2e.gameserver.network.clientpackets.RequestExShowStepThree;
import l2e.gameserver.network.clientpackets.RequestExShowStepTwo;
import l2e.gameserver.network.clientpackets.RequestExTryToPutEnchantSupportItem;
import l2e.gameserver.network.clientpackets.RequestExTryToPutEnchantTargetItem;
import l2e.gameserver.network.clientpackets.RequestExitPartyMatchingWaitingRoom;
import l2e.gameserver.network.clientpackets.RequestFortressMapInfo;
import l2e.gameserver.network.clientpackets.RequestFortressSiegeInfo;
import l2e.gameserver.network.clientpackets.RequestFriendAddReply;
import l2e.gameserver.network.clientpackets.RequestFriendDel;
import l2e.gameserver.network.clientpackets.RequestFriendInfoList;
import l2e.gameserver.network.clientpackets.RequestFriendInvite;
import l2e.gameserver.network.clientpackets.RequestGMCommand;
import l2e.gameserver.network.clientpackets.RequestGameStart;
import l2e.gameserver.network.clientpackets.RequestGetBossRecord;
import l2e.gameserver.network.clientpackets.RequestGetItemFromPet;
import l2e.gameserver.network.clientpackets.RequestGetOffVehicle;
import l2e.gameserver.network.clientpackets.RequestGetOnVehicle;
import l2e.gameserver.network.clientpackets.RequestGiveItemToPet;
import l2e.gameserver.network.clientpackets.RequestGiveNickName;
import l2e.gameserver.network.clientpackets.RequestGmList;
import l2e.gameserver.network.clientpackets.RequestGoodsInventoryInfo;
import l2e.gameserver.network.clientpackets.RequestGotoLobby;
import l2e.gameserver.network.clientpackets.RequestHandOverPartyMaster;
import l2e.gameserver.network.clientpackets.RequestHardWareInfo;
import l2e.gameserver.network.clientpackets.RequestHennaEquip;
import l2e.gameserver.network.clientpackets.RequestHennaItemInfo;
import l2e.gameserver.network.clientpackets.RequestHennaItemList;
import l2e.gameserver.network.clientpackets.RequestHennaUnequip;
import l2e.gameserver.network.clientpackets.RequestHennaUnequipInfo;
import l2e.gameserver.network.clientpackets.RequestHennaUnequipList;
import l2e.gameserver.network.clientpackets.RequestInfoItemAuction;
import l2e.gameserver.network.clientpackets.RequestItemList;
import l2e.gameserver.network.clientpackets.RequestJoinAlly;
import l2e.gameserver.network.clientpackets.RequestJoinCastleSiege;
import l2e.gameserver.network.clientpackets.RequestJoinDominionWar;
import l2e.gameserver.network.clientpackets.RequestJoinMpccRoom;
import l2e.gameserver.network.clientpackets.RequestJoinParty;
import l2e.gameserver.network.clientpackets.RequestJoinPartyRoom;
import l2e.gameserver.network.clientpackets.RequestJoinPledge;
import l2e.gameserver.network.clientpackets.RequestJump;
import l2e.gameserver.network.clientpackets.RequestKeyMapping;
import l2e.gameserver.network.clientpackets.RequestKeyPacket;
import l2e.gameserver.network.clientpackets.RequestLinkHtml;
import l2e.gameserver.network.clientpackets.RequestListMpccWaiting;
import l2e.gameserver.network.clientpackets.RequestListPartyMatchingWaitingRoom;
import l2e.gameserver.network.clientpackets.RequestListPartyWaiting;
import l2e.gameserver.network.clientpackets.RequestLogin;
import l2e.gameserver.network.clientpackets.RequestMagicList;
import l2e.gameserver.network.clientpackets.RequestMagicSkillUse;
import l2e.gameserver.network.clientpackets.RequestMakeMacro;
import l2e.gameserver.network.clientpackets.RequestManageMpccRoom;
import l2e.gameserver.network.clientpackets.RequestManagePartyRoom;
import l2e.gameserver.network.clientpackets.RequestManorList;
import l2e.gameserver.network.clientpackets.RequestModifyBookMarkSlot;
import l2e.gameserver.network.clientpackets.RequestMoveToLocationAirShip;
import l2e.gameserver.network.clientpackets.RequestMoveToLocationInAirShip;
import l2e.gameserver.network.clientpackets.RequestMoveToLocationInVehicle;
import l2e.gameserver.network.clientpackets.RequestMpccPartymasterList;
import l2e.gameserver.network.clientpackets.RequestMultiSellChoose;
import l2e.gameserver.network.clientpackets.RequestNewCharacter;
import l2e.gameserver.network.clientpackets.RequestNewVoteSystem;
import l2e.gameserver.network.clientpackets.RequestNotifyStartMiniGame;
import l2e.gameserver.network.clientpackets.RequestObserverEnd;
import l2e.gameserver.network.clientpackets.RequestOlympiadMatchList;
import l2e.gameserver.network.clientpackets.RequestOlympiadObserverEnd;
import l2e.gameserver.network.clientpackets.RequestOpenMinimap;
import l2e.gameserver.network.clientpackets.RequestOustAlly;
import l2e.gameserver.network.clientpackets.RequestOustFromMpccRoom;
import l2e.gameserver.network.clientpackets.RequestOustFromPartyRoom;
import l2e.gameserver.network.clientpackets.RequestOustPartyMember;
import l2e.gameserver.network.clientpackets.RequestOustPledgeMember;
import l2e.gameserver.network.clientpackets.RequestPCCafeCouponUse;
import l2e.gameserver.network.clientpackets.RequestPVPMatchRecord;
import l2e.gameserver.network.clientpackets.RequestPackageSend;
import l2e.gameserver.network.clientpackets.RequestPackageSendableItemList;
import l2e.gameserver.network.clientpackets.RequestPartyLootingModify;
import l2e.gameserver.network.clientpackets.RequestPetGetItem;
import l2e.gameserver.network.clientpackets.RequestPetUseItem;
import l2e.gameserver.network.clientpackets.RequestPetition;
import l2e.gameserver.network.clientpackets.RequestPetitionCancel;
import l2e.gameserver.network.clientpackets.RequestPledgeCrest;
import l2e.gameserver.network.clientpackets.RequestPledgeExtendedInfo;
import l2e.gameserver.network.clientpackets.RequestPledgeInfo;
import l2e.gameserver.network.clientpackets.RequestPledgeMemberInfo;
import l2e.gameserver.network.clientpackets.RequestPledgeMemberList;
import l2e.gameserver.network.clientpackets.RequestPledgeMemberPowerInfo;
import l2e.gameserver.network.clientpackets.RequestPledgePower;
import l2e.gameserver.network.clientpackets.RequestPledgePowerGradeList;
import l2e.gameserver.network.clientpackets.RequestPledgeReorganizeMember;
import l2e.gameserver.network.clientpackets.RequestPledgeSetAcademyMaster;
import l2e.gameserver.network.clientpackets.RequestPledgeSetMemberPowerGrade;
import l2e.gameserver.network.clientpackets.RequestPledgeWarList;
import l2e.gameserver.network.clientpackets.RequestPostItemList;
import l2e.gameserver.network.clientpackets.RequestPreviewItem;
import l2e.gameserver.network.clientpackets.RequestPrivateStoreBuyManageList;
import l2e.gameserver.network.clientpackets.RequestPrivateStoreBuyManageQuit;
import l2e.gameserver.network.clientpackets.RequestPrivateStoreSellManageList;
import l2e.gameserver.network.clientpackets.RequestPrivateStoreSellQuit;
import l2e.gameserver.network.clientpackets.RequestProcureCrop;
import l2e.gameserver.network.clientpackets.RequestProcureCropList;
import l2e.gameserver.network.clientpackets.RequestQuestList;
import l2e.gameserver.network.clientpackets.RequestReceivePost;
import l2e.gameserver.network.clientpackets.RequestReceivedPost;
import l2e.gameserver.network.clientpackets.RequestReceivedPostList;
import l2e.gameserver.network.clientpackets.RequestRecipeBookOpen;
import l2e.gameserver.network.clientpackets.RequestRecipeItemDelete;
import l2e.gameserver.network.clientpackets.RequestRecipeItemMakeInfo;
import l2e.gameserver.network.clientpackets.RequestRecipeItemMakeSelf;
import l2e.gameserver.network.clientpackets.RequestRecipeShopListSet;
import l2e.gameserver.network.clientpackets.RequestRecipeShopMakeDo;
import l2e.gameserver.network.clientpackets.RequestRecipeShopMakeInfo;
import l2e.gameserver.network.clientpackets.RequestRecipeShopManageCancel;
import l2e.gameserver.network.clientpackets.RequestRecipeShopManageList;
import l2e.gameserver.network.clientpackets.RequestRecipeShopManageQuit;
import l2e.gameserver.network.clientpackets.RequestRecipeShopMessageSet;
import l2e.gameserver.network.clientpackets.RequestRecipeShopSellList;
import l2e.gameserver.network.clientpackets.RequestRefine;
import l2e.gameserver.network.clientpackets.RequestRefineCancel;
import l2e.gameserver.network.clientpackets.RequestRefundItem;
import l2e.gameserver.network.clientpackets.RequestRejectPost;
import l2e.gameserver.network.clientpackets.RequestReload;
import l2e.gameserver.network.clientpackets.RequestRemainTime;
import l2e.gameserver.network.clientpackets.RequestReplyStartPledgeWar;
import l2e.gameserver.network.clientpackets.RequestReplyStopPledgeWar;
import l2e.gameserver.network.clientpackets.RequestReplySurrenderPledgeWar;
import l2e.gameserver.network.clientpackets.RequestResetNickname;
import l2e.gameserver.network.clientpackets.RequestRestart;
import l2e.gameserver.network.clientpackets.RequestRestartPoint;
import l2e.gameserver.network.clientpackets.RequestReviveReply;
import l2e.gameserver.network.clientpackets.RequestSEKCustom;
import l2e.gameserver.network.clientpackets.RequestSSQStatus;
import l2e.gameserver.network.clientpackets.RequestSaveBookMarkSlot;
import l2e.gameserver.network.clientpackets.RequestSaveInventoryOrder;
import l2e.gameserver.network.clientpackets.RequestSaveKeyMapping;
import l2e.gameserver.network.clientpackets.RequestSeedPhase;
import l2e.gameserver.network.clientpackets.RequestSellItem;
import l2e.gameserver.network.clientpackets.RequestSendL2FriendSay;
import l2e.gameserver.network.clientpackets.RequestSendMsnChatLog;
import l2e.gameserver.network.clientpackets.RequestSendPost;
import l2e.gameserver.network.clientpackets.RequestSentPost;
import l2e.gameserver.network.clientpackets.RequestSentPostList;
import l2e.gameserver.network.clientpackets.RequestSetAllyCrest;
import l2e.gameserver.network.clientpackets.RequestSetCastleSiegeTime;
import l2e.gameserver.network.clientpackets.RequestSetCrop;
import l2e.gameserver.network.clientpackets.RequestSetPledgeCrest;
import l2e.gameserver.network.clientpackets.RequestSetSeed;
import l2e.gameserver.network.clientpackets.RequestShortCutDel;
import l2e.gameserver.network.clientpackets.RequestShortCutReg;
import l2e.gameserver.network.clientpackets.RequestShortCutUse;
import l2e.gameserver.network.clientpackets.RequestShowBoard;
import l2e.gameserver.network.clientpackets.RequestShowNewUserPetition;
import l2e.gameserver.network.clientpackets.RequestSiegeInfo;
import l2e.gameserver.network.clientpackets.RequestSkillCoolTime;
import l2e.gameserver.network.clientpackets.RequestSkillList;
import l2e.gameserver.network.clientpackets.RequestStartPledgeWar;
import l2e.gameserver.network.clientpackets.RequestStartShowCrataeCubeRank;
import l2e.gameserver.network.clientpackets.RequestStopPledgeWar;
import l2e.gameserver.network.clientpackets.RequestStopShowCrataeCubeRank;
import l2e.gameserver.network.clientpackets.RequestSurrenderPersonally;
import l2e.gameserver.network.clientpackets.RequestSurrenderPledgeWar;
import l2e.gameserver.network.clientpackets.RequestTargetCancel;
import l2e.gameserver.network.clientpackets.RequestTeleport;
import l2e.gameserver.network.clientpackets.RequestTeleportBookMark;
import l2e.gameserver.network.clientpackets.RequestTrade;
import l2e.gameserver.network.clientpackets.RequestTutorialClientEvent;
import l2e.gameserver.network.clientpackets.RequestTutorialLinkHtml;
import l2e.gameserver.network.clientpackets.RequestTutorialPassCmdToServer;
import l2e.gameserver.network.clientpackets.RequestTutorialQuestionMarkPressed;
import l2e.gameserver.network.clientpackets.RequestUnEquipItem;
import l2e.gameserver.network.clientpackets.RequestUseGoodsInventoryItem;
import l2e.gameserver.network.clientpackets.RequestUseItem;
import l2e.gameserver.network.clientpackets.RequestWithDrawPremiumItem;
import l2e.gameserver.network.clientpackets.RequestWithDrawalParty;
import l2e.gameserver.network.clientpackets.RequestWithDrawalPledge;
import l2e.gameserver.network.clientpackets.RequestWithdrawAlly;
import l2e.gameserver.network.clientpackets.RequestWithdrawMpccRoom;
import l2e.gameserver.network.clientpackets.RequestWithdrawPartyRoom;
import l2e.gameserver.network.clientpackets.RequestWriteHeroWords;
import l2e.gameserver.network.clientpackets.Say;
import l2e.gameserver.network.clientpackets.Say2;
import l2e.gameserver.network.clientpackets.SendAppearing;
import l2e.gameserver.network.clientpackets.SendBypassBuildCmd;
import l2e.gameserver.network.clientpackets.SendLogOut;
import l2e.gameserver.network.clientpackets.SendPrivateStoreBuyList;
import l2e.gameserver.network.clientpackets.SendPrivateStoreSellList;
import l2e.gameserver.network.clientpackets.SendProtocolVersion;
import l2e.gameserver.network.clientpackets.SendTimeCheck;
import l2e.gameserver.network.clientpackets.SendWareHouseDepositList;
import l2e.gameserver.network.clientpackets.SendWareHouseWithDrawList;
import l2e.gameserver.network.clientpackets.SetPrivateStoreBuyList;
import l2e.gameserver.network.clientpackets.SetPrivateStoreBuyMsg;
import l2e.gameserver.network.clientpackets.SetPrivateStoreSellList;
import l2e.gameserver.network.clientpackets.SetPrivateStoreSellMsg;
import l2e.gameserver.network.clientpackets.SetPrivateStoreWholeMsg;
import l2e.gameserver.network.clientpackets.SocialAction;
import l2e.gameserver.network.clientpackets.StartRotating;
import l2e.gameserver.network.clientpackets.TradeDone;
import l2e.gameserver.network.clientpackets.UserAck;
import l2e.gameserver.network.clientpackets.ValidatePosition;
import l2e.gameserver.network.clientpackets.VoteSociality;
import org.nio.impl.IClientFactory;
import org.nio.impl.IMMOExecutor;
import org.nio.impl.IPacketHandler;
import org.nio.impl.MMOConnection;
import org.nio.impl.ReceivablePacket;

public final class GamePacketHandler implements IPacketHandler<GameClient>, IClientFactory<GameClient>, IMMOExecutor<GameClient> {
   private static final Logger _log = Logger.getLogger(GamePacketHandler.class.getName());

   public ReceivablePacket<GameClient> handlePacket(ByteBuffer buf, GameClient client) {
      ReceivablePacket<GameClient> msg = null;
      int opcode = buf.get() & 255;
      GameClient.GameClientState state = client.getState();
      switch(state) {
         case CONNECTED:
            switch(opcode) {
               case 14:
                  return new SendProtocolVersion();
               case 43:
                  return new RequestLogin();
               case 203:
                  return new ReplyGameGuardQuery();
               default:
                  this.printDebug(opcode, buf, state, client);
                  return msg;
            }
         case AUTHED:
            switch(opcode) {
               case 0:
                  return new SendLogOut();
               case 12:
                  return new RequestCharacterCreate();
               case 13:
                  return new RequestCharacterDelete();
               case 18:
                  return new RequestGameStart();
               case 19:
                  return new RequestNewCharacter();
               case 123:
                  return new RequestCharacterRestore();
               case 203:
                  return new ReplyGameGuardQuery();
               case 208:
                  int id2 = -1;
                  if (buf.remaining() >= 2) {
                     id2 = buf.getShort() & '\uffff';
                     switch(id2) {
                        case 33:
                           return new RequestKeyMapping();
                        case 54:
                           return new RequestGotoLobby();
                        case 59:
                           return new RequestExChangeName();
                        case 147:
                           return new RequestEx2ndPasswordCheck();
                        case 148:
                           return new RequestEx2ndPasswordVerify();
                        case 149:
                           return new RequestEx2ndPasswordReq();
                        default:
                           this.printDebugDoubleOpcode(opcode, id2, buf, state, client);
                     }
                  } else if (Config.PACKET_HANDLER_DEBUG) {
                     _log.warning("Client: " + client.toString() + " sent a 0xd0 without the second opcode.");
                     return msg;
                  }

                  return msg;
               default:
                  this.printDebug(opcode, buf, state, client);
                  return msg;
            }
         case ENTERING:
            switch(opcode) {
               case 17:
                  return new RequestEnterWorld();
               case 208:
                  int id2 = -1;
                  if (buf.remaining() >= 2) {
                     id2 = buf.getShort() & '\uffff';
                     switch(id2) {
                        case 1:
                           return new RequestManorList();
                        case 33:
                           return new RequestKeyMapping();
                        case 131:
                           int id99 = 0;
                           if (buf.remaining() >= 4) {
                              id99 = buf.getInt();
                           }

                           switch(id99) {
                              case 16:
                                 if (Config.ALLOW_CUSTOM_INTERFACE) {
                                    msg = new RequestKeyPacket();
                                 }

                                 return msg;
                              default:
                                 return msg;
                           }
                        default:
                           this.printDebugDoubleOpcode(opcode, id2, buf, state, client);
                     }
                  } else if (Config.PACKET_HANDLER_DEBUG) {
                     _log.warning("Client: " + client.toString() + " sent a 0xd0 without the second opcode.");
                     return msg;
                  }

                  return msg;
               default:
                  this.printDebug(opcode, buf, state, client);
                  return msg;
            }
         case IN_GAME:
            switch(opcode) {
               case 0:
                  msg = new SendLogOut();
                  break;
               case 1:
                  msg = new RequestAttack();
                  break;
               case 2:
               case 10:
               case 12:
               case 13:
               case 14:
               case 17:
               case 18:
               case 19:
               case 24:
               case 29:
               case 30:
               case 32:
               case 33:
               case 42:
               case 43:
               case 45:
               case 50:
               case 74:
               case 75:
               case 76:
               case 78:
               case 81:
               case 93:
               case 97:
               case 100:
               case 104:
               case 106:
               case 123:
               case 130:
               case 158:
               case 161:
               case 162:
               case 163:
               case 164:
               case 165:
               case 202:
               default:
                  this.printDebug(opcode, buf, state, client);
                  break;
               case 3:
                  msg = new RequestStartPledgeWar();
                  break;
               case 4:
                  msg = new RequestReplyStartPledgeWar();
                  break;
               case 5:
                  msg = new RequestStopPledgeWar();
                  break;
               case 6:
                  msg = new RequestReplyStopPledgeWar();
                  break;
               case 7:
                  msg = new RequestSurrenderPledgeWar();
                  break;
               case 8:
                  msg = new RequestReplySurrenderPledgeWar();
                  break;
               case 9:
                  msg = new RequestSetPledgeCrest();
                  break;
               case 11:
                  msg = new RequestGiveNickName();
                  break;
               case 15:
                  msg = new MoveBackwardToLocation();
                  break;
               case 16:
                  msg = new Say();
                  break;
               case 20:
                  msg = new RequestItemList();
                  break;
               case 21:
                  msg = new RequestEquipItem();
                  break;
               case 22:
                  msg = new RequestUnEquipItem();
                  break;
               case 23:
                  msg = new RequestDropItem();
                  break;
               case 25:
                  msg = new RequestUseItem();
                  break;
               case 26:
                  msg = new RequestTrade();
                  break;
               case 27:
                  msg = new RequestAddTradeItem();
                  break;
               case 28:
                  msg = new TradeDone();
                  break;
               case 31:
                  msg = new Action();
                  break;
               case 34:
                  msg = new RequestLinkHtml();
                  break;
               case 35:
                  msg = new RequestBypassToServer();
                  break;
               case 36:
                  msg = new RequestBBSwrite();
                  break;
               case 37:
                  msg = new RequestCreatePledge();
                  break;
               case 38:
                  msg = new RequestJoinPledge();
                  break;
               case 39:
                  msg = new RequestAnswerJoinPledge();
                  break;
               case 40:
                  msg = new RequestWithDrawalPledge();
                  break;
               case 41:
                  msg = new RequestOustPledgeMember();
                  break;
               case 44:
                  msg = new RequestGetItemFromPet();
                  break;
               case 46:
                  msg = new RequestAllyInfo();
                  break;
               case 47:
                  msg = new RequestCrystallizeItem();
                  break;
               case 48:
                  msg = new RequestPrivateStoreSellManageList();
                  break;
               case 49:
                  msg = new SetPrivateStoreSellList();
                  break;
               case 51:
                  msg = new RequestTeleport();
                  break;
               case 52:
                  msg = new SocialAction();
                  break;
               case 53:
                  msg = new ChangeMoveType();
                  break;
               case 54:
                  msg = new ChangeWaitType();
                  break;
               case 55:
                  msg = new RequestSellItem();
                  break;
               case 56:
                  msg = new UserAck();
                  break;
               case 57:
                  msg = new RequestMagicSkillUse();
                  break;
               case 58:
                  msg = new SendAppearing();
                  break;
               case 59:
                  if (Config.ALLOW_WAREHOUSE) {
                     msg = new SendWareHouseDepositList();
                  }
                  break;
               case 60:
                  msg = new SendWareHouseWithDrawList();
                  break;
               case 61:
                  msg = new RequestShortCutReg();
                  break;
               case 62:
                  msg = new RequestShortCutUse();
                  break;
               case 63:
                  msg = new RequestShortCutDel();
                  break;
               case 64:
                  msg = new RequestBuyItem();
                  break;
               case 65:
                  msg = new RequestDismissPledge();
                  break;
               case 66:
                  msg = new RequestJoinParty();
                  break;
               case 67:
                  msg = new RequestAnswerJoinParty();
                  break;
               case 68:
                  msg = new RequestWithDrawalParty();
                  break;
               case 69:
                  msg = new RequestOustPartyMember();
                  break;
               case 70:
                  msg = new RequestDismissParty();
                  break;
               case 71:
                  msg = new CanNotMoveAnymore();
                  break;
               case 72:
                  msg = new RequestTargetCancel();
                  break;
               case 73:
                  msg = new Say2();
                  break;
               case 77:
                  msg = new RequestPledgeMemberList();
                  break;
               case 79:
                  msg = new RequestMagicList();
                  break;
               case 80:
                  msg = new RequestSkillList();
                  break;
               case 82:
                  msg = new MoveWithDelta();
                  break;
               case 83:
                  msg = new RequestGetOnVehicle();
                  break;
               case 84:
                  msg = new RequestGetOffVehicle();
                  break;
               case 85:
                  msg = new AnswerTradeRequest();
                  break;
               case 86:
                  msg = new RequestActionUse();
                  break;
               case 87:
                  msg = new RequestRestart();
                  break;
               case 88:
                  msg = new RequestSiegeInfo();
                  break;
               case 89:
                  msg = new ValidatePosition();
                  break;
               case 90:
                  msg = new RequestSEKCustom();
                  break;
               case 91:
                  msg = new StartRotating();
                  break;
               case 92:
                  msg = new FinishRotating();
                  break;
               case 94:
                  msg = new RequestShowBoard();
                  break;
               case 95:
                  msg = new RequestEnchantItem();
                  break;
               case 96:
                  msg = new RequestDestroyItem();
                  break;
               case 98:
                  msg = new RequestQuestList();
                  break;
               case 99:
                  msg = new RequestDestroyQuest();
                  break;
               case 101:
                  msg = new RequestPledgeInfo();
                  break;
               case 102:
                  msg = new RequestPledgeExtendedInfo();
                  break;
               case 103:
                  msg = new RequestPledgeCrest();
                  break;
               case 105:
                  msg = new RequestSurrenderPersonally();
                  break;
               case 107:
                  msg = new RequestSendL2FriendSay();
                  break;
               case 108:
                  msg = new RequestOpenMinimap();
                  break;
               case 109:
                  msg = new RequestSendMsnChatLog();
                  break;
               case 110:
                  msg = new RequestReload();
                  break;
               case 111:
                  msg = new RequestHennaEquip();
                  break;
               case 112:
                  msg = new RequestHennaUnequipList();
                  break;
               case 113:
                  msg = new RequestHennaUnequipInfo();
                  break;
               case 114:
                  msg = new RequestHennaUnequip();
                  break;
               case 115:
                  msg = new RequestAcquireSkillInfo();
                  break;
               case 116:
                  msg = new SendBypassBuildCmd();
                  break;
               case 117:
                  msg = new RequestMoveToLocationInVehicle();
                  break;
               case 118:
                  msg = new CanNotMoveAnymoreInVehicle();
                  break;
               case 119:
                  msg = new RequestFriendInvite();
                  break;
               case 120:
                  msg = new RequestFriendAddReply();
                  break;
               case 121:
                  msg = new RequestFriendInfoList();
                  break;
               case 122:
                  msg = new RequestFriendDel();
                  break;
               case 124:
                  msg = new RequestAcquireSkill();
                  break;
               case 125:
                  msg = new RequestRestartPoint();
                  break;
               case 126:
                  msg = new RequestGMCommand();
                  break;
               case 127:
                  msg = new RequestListPartyWaiting();
                  break;
               case 128:
                  msg = new RequestManagePartyRoom();
                  break;
               case 129:
                  msg = new RequestJoinPartyRoom();
                  break;
               case 131:
                  msg = new SendPrivateStoreBuyList();
                  break;
               case 132:
                  msg = new RequestReviveReply();
                  break;
               case 133:
                  msg = new RequestTutorialLinkHtml();
                  break;
               case 134:
                  msg = new RequestTutorialPassCmdToServer();
                  break;
               case 135:
                  msg = new RequestTutorialQuestionMarkPressed();
                  break;
               case 136:
                  msg = new RequestTutorialClientEvent();
                  break;
               case 137:
                  msg = new RequestPetition();
                  break;
               case 138:
                  msg = new RequestPetitionCancel();
                  break;
               case 139:
                  msg = new RequestGmList();
                  break;
               case 140:
                  msg = new RequestJoinAlly();
                  break;
               case 141:
                  msg = new RequestAnswerJoinAlly();
                  break;
               case 142:
                  msg = new RequestWithdrawAlly();
                  break;
               case 143:
                  msg = new RequestOustAlly();
                  break;
               case 144:
                  msg = new RequestDismissAlly();
                  break;
               case 145:
                  msg = new RequestSetAllyCrest();
                  break;
               case 146:
                  msg = new RequestAllyCrest();
                  break;
               case 147:
                  msg = new RequestChangePetName();
                  break;
               case 148:
                  msg = new RequestPetUseItem();
                  break;
               case 149:
                  msg = new RequestGiveItemToPet();
                  break;
               case 150:
                  msg = new RequestPrivateStoreSellQuit();
                  break;
               case 151:
                  msg = new SetPrivateStoreSellMsg();
                  break;
               case 152:
                  msg = new RequestPetGetItem();
                  break;
               case 153:
                  msg = new RequestPrivateStoreBuyManageList();
                  break;
               case 154:
                  msg = new SetPrivateStoreBuyList();
                  break;
               case 155:
                  msg = new ReplyStopAllianceWar();
                  break;
               case 156:
                  msg = new RequestPrivateStoreBuyManageQuit();
                  break;
               case 157:
                  msg = new SetPrivateStoreBuyMsg();
                  break;
               case 159:
                  msg = new SendPrivateStoreSellList();
                  break;
               case 160:
                  msg = new SendTimeCheck();
                  break;
               case 166:
                  msg = new RequestSkillCoolTime();
                  break;
               case 167:
                  msg = new RequestPackageSendableItemList();
                  break;
               case 168:
                  msg = new RequestPackageSend();
                  break;
               case 169:
                  msg = new RequestBlock();
                  break;
               case 170:
                  msg = new RequestCastleSiegeInfo();
                  break;
               case 171:
                  msg = new RequestCastleSiegeAttackerList();
                  break;
               case 172:
                  msg = new RequestCastleSiegeDefenderList();
                  break;
               case 173:
                  msg = new RequestJoinCastleSiege();
                  break;
               case 174:
                  msg = new RequestConfirmCastleSiegeWaitingList();
                  break;
               case 175:
                  msg = new RequestSetCastleSiegeTime();
                  break;
               case 176:
                  msg = new RequestMultiSellChoose();
                  break;
               case 177:
                  msg = new NetPing();
                  break;
               case 178:
                  msg = new RequestRemainTime();
                  break;
               case 179:
                  msg = new BypassUserCmd();
                  break;
               case 180:
                  msg = new GMSnoopEnd();
                  break;
               case 181:
                  msg = new RequestRecipeBookOpen();
                  break;
               case 182:
                  msg = new RequestRecipeItemDelete();
                  break;
               case 183:
                  msg = new RequestRecipeItemMakeInfo();
                  break;
               case 184:
                  msg = new RequestRecipeItemMakeSelf();
                  break;
               case 185:
                  msg = new RequestRecipeShopManageList();
                  break;
               case 186:
                  msg = new RequestRecipeShopMessageSet();
                  break;
               case 187:
                  msg = new RequestRecipeShopListSet();
                  break;
               case 188:
                  msg = new RequestRecipeShopManageQuit();
                  break;
               case 189:
                  msg = new RequestRecipeShopManageCancel();
                  break;
               case 190:
                  msg = new RequestRecipeShopMakeInfo();
                  break;
               case 191:
                  msg = new RequestRecipeShopMakeDo();
                  break;
               case 192:
                  msg = new RequestRecipeShopSellList();
                  break;
               case 193:
                  msg = new RequestObserverEnd();
                  break;
               case 194:
                  msg = new VoteSociality();
                  break;
               case 195:
                  msg = new RequestHennaItemList();
                  break;
               case 196:
                  msg = new RequestHennaItemInfo();
                  break;
               case 197:
                  msg = new RequestBuySeed();
                  break;
               case 198:
                  msg = new ConfirmDlg();
                  break;
               case 199:
                  msg = new RequestPreviewItem();
                  break;
               case 200:
                  msg = new RequestSSQStatus();
                  break;
               case 201:
                  msg = new PetitionVote();
                  break;
               case 203:
                  msg = new ReplyGameGuardQuery();
                  break;
               case 204:
                  msg = new RequestPledgePower();
                  break;
               case 205:
                  msg = new RequestMakeMacro();
                  break;
               case 206:
                  msg = new RequestDeleteMacro();
                  break;
               case 207:
                  msg = new RequestProcureCrop();
                  break;
               case 208:
                  int id2 = -1;
                  if (buf.remaining() >= 2) {
                     id2 = buf.getShort() & '\uffff';
                     switch(id2) {
                        case 1:
                           msg = new RequestManorList();
                           break;
                        case 2:
                           msg = new RequestProcureCropList();
                           break;
                        case 3:
                           msg = new RequestSetSeed();
                           break;
                        case 4:
                           msg = new RequestSetCrop();
                           break;
                        case 5:
                           msg = new RequestWriteHeroWords();
                           break;
                        case 6:
                           msg = new RequestExAskJoinMPCC();
                           break;
                        case 7:
                           msg = new RequestExAcceptJoinMPCC();
                           break;
                        case 8:
                           msg = new RequestExOustFromMPCC();
                           break;
                        case 9:
                           msg = new RequestOustFromPartyRoom();
                           break;
                        case 10:
                           msg = new RequestDismissPartyRoom();
                           break;
                        case 11:
                           msg = new RequestWithdrawPartyRoom();
                           break;
                        case 12:
                           msg = new RequestHandOverPartyMaster();
                           break;
                        case 13:
                           msg = new RequestAutoSoulShot();
                           break;
                        case 14:
                           msg = new RequestExEnchantSkillInfo();
                           break;
                        case 15:
                           msg = new RequestExEnchantSkill();
                           break;
                        case 16:
                           msg = new RequestExPledgeCrestLarge();
                           break;
                        case 17:
                           msg = new RequestExSetPledgeCrestLarge();
                           break;
                        case 18:
                           msg = new RequestPledgeSetAcademyMaster();
                           break;
                        case 19:
                           msg = new RequestPledgePowerGradeList();
                           break;
                        case 20:
                           msg = new RequestPledgeMemberPowerInfo();
                           break;
                        case 21:
                           msg = new RequestPledgeSetMemberPowerGrade();
                           break;
                        case 22:
                           msg = new RequestPledgeMemberInfo();
                           break;
                        case 23:
                           msg = new RequestPledgeWarList();
                           break;
                        case 24:
                           msg = new RequestExFishRanking();
                           break;
                        case 25:
                           msg = new RequestPCCafeCouponUse();
                           break;
                        case 26:
                           msg = new RequestExOrcMove();
                           break;
                        case 27:
                           msg = new RequestDuelStart();
                           break;
                        case 28:
                           msg = new RequestDuelAnswerStart();
                           break;
                        case 29:
                           msg = new RequestExSetTutorial();
                           break;
                        case 30:
                           msg = new RequestExRqItemLink();
                           break;
                        case 31:
                           msg = new RequestCannotMoveAnymoreAirShip();
                           break;
                        case 32:
                           msg = new RequestMoveToLocationInAirShip();
                           break;
                        case 33:
                           msg = new RequestKeyMapping();
                           break;
                        case 34:
                           msg = new RequestSaveKeyMapping();
                           break;
                        case 35:
                           msg = new RequestExRemoveItemAttribute();
                           break;
                        case 36:
                           msg = new RequestSaveInventoryOrder();
                           break;
                        case 37:
                           msg = new RequestExitPartyMatchingWaitingRoom();
                           break;
                        case 38:
                           msg = new RequestConfirmTargetItem();
                           break;
                        case 39:
                           msg = new RequestConfirmRefinerItem();
                           break;
                        case 40:
                           msg = new RequestConfirmGemStone();
                           break;
                        case 41:
                           msg = new RequestOlympiadObserverEnd();
                           break;
                        case 42:
                           msg = new RequestCursedWeaponList();
                           break;
                        case 43:
                           msg = new RequestCursedWeaponLocation();
                           break;
                        case 44:
                           msg = new RequestPledgeReorganizeMember();
                           break;
                        case 45:
                           msg = new RequestExMPCCShowPartyMembersInfo();
                           break;
                        case 46:
                           msg = new RequestOlympiadMatchList();
                           break;
                        case 47:
                           msg = new RequestAskJoinPartyRoom();
                           break;
                        case 48:
                           msg = new AnswerJoinPartyRoom();
                           break;
                        case 49:
                           msg = new RequestListPartyMatchingWaitingRoom();
                           break;
                        case 50:
                           msg = new RequestExEnchantSkillSafe();
                           break;
                        case 51:
                           msg = new RequestExEnchantSkillUntrain();
                           break;
                        case 52:
                           msg = new RequestExEnchantSkillChange();
                           break;
                        case 53:
                           msg = new RequestExEnchantItemAttribute();
                           break;
                        case 54:
                           msg = new RequestExGetOnAirShip();
                           break;
                        case 55:
                           msg = new RequestExGetOffAirShip();
                           break;
                        case 56:
                           msg = new RequestMoveToLocationAirShip();
                           break;
                        case 57:
                           msg = new RequestBidItemAuction();
                           break;
                        case 58:
                           msg = new RequestInfoItemAuction();
                           break;
                        case 59:
                           msg = new RequestExChangeName();
                           break;
                        case 60:
                           msg = new RequestAllCastleInfo();
                           break;
                        case 61:
                           msg = new RequestAllFortressInfo();
                           break;
                        case 62:
                           msg = new RequestAllAgitInfo();
                           break;
                        case 63:
                           msg = new RequestFortressSiegeInfo();
                           break;
                        case 64:
                           msg = new RequestGetBossRecord();
                           break;
                        case 65:
                           msg = new RequestRefine();
                           break;
                        case 66:
                           msg = new RequestConfirmCancelItem();
                           break;
                        case 67:
                           msg = new RequestRefineCancel();
                           break;
                        case 68:
                           msg = new RequestExMagicSkillUseGround();
                           break;
                        case 69:
                           msg = new RequestDuelSurrender();
                           break;
                        case 70:
                           msg = new RequestExEnchantSkillInfoDetail();
                           break;
                        case 71:
                        case 127:
                        case 128:
                        case 129:
                        case 130:
                        case 147:
                        case 148:
                        case 149:
                        default:
                           this.printDebugDoubleOpcode(opcode, id2, buf, state, client);
                           break;
                        case 72:
                           msg = new RequestFortressMapInfo();
                           break;
                        case 73:
                           msg = new RequestPVPMatchRecord();
                           break;
                        case 74:
                           msg = new SetPrivateStoreWholeMsg();
                           break;
                        case 75:
                           msg = new RequestDispel();
                           break;
                        case 76:
                           msg = new RequestExTryToPutEnchantTargetItem();
                           break;
                        case 77:
                           msg = new RequestExTryToPutEnchantSupportItem();
                           break;
                        case 78:
                           msg = new RequestExCancelEnchantItem();
                           break;
                        case 79:
                           msg = new RequestChangeNicknameColor();
                           break;
                        case 80:
                           msg = new RequestResetNickname();
                           break;
                        case 81:
                           int id3 = 0;
                           if (buf.remaining() >= 4) {
                              id3 = buf.getInt();
                              switch(id3) {
                                 case 0:
                                    return new RequestBookMarkSlotInfo();
                                 case 1:
                                    return new RequestSaveBookMarkSlot();
                                 case 2:
                                    return new RequestModifyBookMarkSlot();
                                 case 3:
                                    return new RequestDeleteBookMarkSlot();
                                 case 4:
                                    return new RequestTeleportBookMark();
                                 case 5:
                                    return new RequestChangeBookMarkSlot();
                                 default:
                                    this.printDebugDoubleOpcode(opcode, id3, buf, state, client);
                              }
                           } else if (Config.PACKET_HANDLER_DEBUG) {
                              _log.warning("Client: " + client.toString() + " sent a 0xd0:0x51 without the third opcode.");
                           }
                           break;
                        case 82:
                           msg = new RequestWithDrawPremiumItem();
                           break;
                        case 83:
                           msg = new RequestJump();
                           break;
                        case 84:
                           msg = new RequestStartShowCrataeCubeRank();
                           break;
                        case 85:
                           msg = new RequestStopShowCrataeCubeRank();
                           break;
                        case 86:
                           msg = new RequestNotifyStartMiniGame();
                           break;
                        case 87:
                           msg = new RequestJoinDominionWar();
                           break;
                        case 88:
                           msg = new RequestDominionInfo();
                           break;
                        case 89:
                           msg = new RequestExCleftEnter();
                           break;
                        case 90:
                           int id4 = 0;
                           if (buf.remaining() >= 4) {
                              id4 = buf.getInt();
                              switch(id4) {
                                 case 0:
                                    msg = new RequestExBlockGameEnter();
                              }
                           } else if (Config.PACKET_HANDLER_DEBUG) {
                              _log.warning("Client: " + client.toString() + " sent a 0xd0:0x5A without the third opcode.");
                           }
                           break;
                        case 91:
                           msg = new RequestEndScenePlayer();
                           break;
                        case 92:
                           msg = new RequestExBlockGameVote();
                           break;
                        case 93:
                           msg = new RequestListMpccWaiting();
                           break;
                        case 94:
                           msg = new RequestManageMpccRoom();
                           break;
                        case 95:
                           msg = new RequestJoinMpccRoom();
                           break;
                        case 96:
                           msg = new RequestOustFromMpccRoom();
                           break;
                        case 97:
                           msg = new RequestDismissMpccRoom();
                           break;
                        case 98:
                           msg = new RequestWithdrawMpccRoom();
                           break;
                        case 99:
                           msg = new RequestSeedPhase();
                           break;
                        case 100:
                           msg = new RequestMpccPartymasterList();
                           break;
                        case 101:
                           msg = new RequestPostItemList();
                           break;
                        case 102:
                           msg = new RequestSendPost();
                           break;
                        case 103:
                           msg = new RequestReceivedPostList();
                           break;
                        case 104:
                           msg = new RequestDeleteReceivedPost();
                           break;
                        case 105:
                           msg = new RequestReceivedPost();
                           break;
                        case 106:
                           msg = new RequestReceivePost();
                           break;
                        case 107:
                           msg = new RequestRejectPost();
                           break;
                        case 108:
                           msg = new RequestSentPostList();
                           break;
                        case 109:
                           msg = new RequestDeleteSentPost();
                           break;
                        case 110:
                           msg = new RequestSentPost();
                           break;
                        case 111:
                           msg = new RequestCancelSentPost();
                           break;
                        case 112:
                           msg = new RequestShowNewUserPetition();
                           break;
                        case 113:
                           msg = new RequestExShowStepTwo();
                           break;
                        case 114:
                           msg = new RequestExShowStepThree();
                           break;
                        case 115:
                           msg = new RequestExConnectToRaidServer();
                           break;
                        case 116:
                           msg = new RequestExReturnFromRaidServer();
                           break;
                        case 117:
                           msg = new RequestRefundItem();
                           break;
                        case 118:
                           msg = new RequestBuySellUIClose();
                           break;
                        case 119:
                           msg = new RequestEventMatchObserverEnd();
                           break;
                        case 120:
                           msg = new RequestPartyLootingModify();
                           break;
                        case 121:
                           msg = new RequestAnswerPartyLootingModify();
                           break;
                        case 122:
                           msg = new RequestAnswerCoupleAction();
                           break;
                        case 123:
                           msg = new RequestExBrEventRankerList();
                           break;
                        case 124:
                           msg = new RequestAskMemberShip();
                           break;
                        case 125:
                           msg = new RequestAddExpandQuestAlarm();
                           break;
                        case 126:
                           msg = new RequestNewVoteSystem();
                           break;
                        case 131:
                           int id5 = 0;
                           if (buf.remaining() >= 4) {
                              id5 = buf.getInt();
                              switch(id5) {
                                 case 1:
                                    return new RequestExAgitInitialize();
                                 case 2:
                                    return new RequestExAgitDetailInfo();
                                 case 3:
                                    return new RequestExMyAgitState();
                                 case 4:
                                    return new RequestExRegisterAgitForBidStep1();
                                 case 5:
                                    msg = new RequestExRegisterAgitForBidStep3();
                                    return msg;
                                 case 6:
                                 case 11:
                                 case 12:
                                 default:
                                    return msg;
                                 case 7:
                                    return new RequestExConfirmCancelRegisteringAgit();
                                 case 8:
                                    return new RequestExProceedCancelRegisteringAgit();
                                 case 9:
                                    return new RequestExConfirmCancelAgitLot();
                                 case 10:
                                    return new RequestExProceedCancelAgitLot();
                                 case 13:
                                    return new RequestExApplyForBidStep1();
                                 case 14:
                                    return new RequestExApplyForBidStep2();
                                 case 15:
                                    return new RequestExApplyForBidStep3();
                                 case 16:
                                    if (Config.ALLOW_CUSTOM_INTERFACE) {
                                       msg = new RequestKeyPacket();
                                    } else {
                                       msg = new RequestExReBid();
                                    }

                                    return msg;
                                 case 17:
                                    return new RequestExAgitListForLot();
                                 case 18:
                                    return new RequestExApplyForAgitLotStep1();
                                 case 19:
                                    return new RequestExApplyForAgitLotStep2();
                                 case 20:
                                    msg = new RequestExAgitListForBid();
                              }
                           } else if (Config.PACKET_HANDLER_DEBUG) {
                              _log.warning("Client: " + client.toString() + " sent a 0xd0:0x83 without the third opcode.");
                           }
                           break;
                        case 132:
                           msg = new RequestExAddPostFriendForPostBox();
                           break;
                        case 133:
                           msg = new RequestExDeletePostFriendForPostBox();
                           break;
                        case 134:
                           msg = new RequestExShowPostFriendListForPostBox();
                           break;
                        case 135:
                           msg = new RequestExFriendListForPostBox();
                           break;
                        case 136:
                           msg = new RequestExOlympiadMatchListRefresh();
                           break;
                        case 137:
                           msg = new RequestBrGamePoint();
                           break;
                        case 138:
                           msg = new RequestBrProductList();
                           break;
                        case 139:
                           msg = new RequestBrProductInfo();
                           break;
                        case 140:
                           msg = new RequestBrBuyProduct();
                           break;
                        case 141:
                           msg = new RequestBrRecentProductList();
                           break;
                        case 142:
                           msg = new RequestBrMiniGameLoadScores();
                           break;
                        case 143:
                           msg = new RequestBrMiniGameInsertScore();
                           break;
                        case 144:
                           msg = new RequestBrLectureMark();
                           break;
                        case 145:
                           msg = new RequestGoodsInventoryInfo();
                           break;
                        case 146:
                           msg = new RequestUseGoodsInventoryItem();
                           break;
                        case 150:
                           msg = new RequestHardWareInfo();
                     }
                  } else if (Config.PACKET_HANDLER_DEBUG) {
                     _log.warning("Client: " + client.toString() + " sent a 0xd0 without the second opcode.");
                  }
            }
      }

      return msg;
   }

   private void printDebug(int opcode, ByteBuffer buf, GameClient.GameClientState state, GameClient client) {
      client.onUnknownPacket();
      if (Config.PACKET_HANDLER_DEBUG) {
         _log.warning("Unknown client packet! State: " + state.name() + ", packet ID: " + Integer.toHexString(opcode).toUpperCase());
      }
   }

   private void printDebugDoubleOpcode(int opcode, int id2, ByteBuffer buf, GameClient.GameClientState state, GameClient client) {
      client.onUnknownPacket();
      if (Config.PACKET_HANDLER_DEBUG) {
         _log.warning(
            "Unknown client packet! State: "
               + state.name()
               + ", packet ID: "
               + Integer.toHexString(opcode).toUpperCase()
               + ":"
               + Integer.toHexString(id2).toUpperCase()
         );
      }
   }

   public GameClient create(MMOConnection<GameClient> con) {
      return new GameClient(con);
   }

   @Override
   public void execute(Runnable r) {
      ThreadPoolManager.getInstance().execute(r);
   }
}
