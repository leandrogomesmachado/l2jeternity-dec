package l2e.gameserver.model.actor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import l2e.commons.annotations.Nullable;
import l2e.commons.threading.RunnableImpl;
import l2e.commons.util.Broadcast;
import l2e.commons.util.Rnd;
import l2e.commons.util.TransferSkillUtils;
import l2e.commons.util.Util;
import l2e.fake.FakePlayer;
import l2e.gameserver.Announcements;
import l2e.gameserver.Config;
import l2e.gameserver.GameTimeController;
import l2e.gameserver.ItemsAutoDestroy;
import l2e.gameserver.RecipeController;
import l2e.gameserver.SevenSigns;
import l2e.gameserver.SevenSignsFestival;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.character.CharacterAI;
import l2e.gameserver.ai.character.PlayerAI;
import l2e.gameserver.ai.character.SummonAI;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.dao.AchievementsDAO;
import l2e.gameserver.data.dao.CharacterBookMarkDAO;
import l2e.gameserver.data.dao.CharacterDAO;
import l2e.gameserver.data.dao.CharacterHennaDAO;
import l2e.gameserver.data.dao.CharacterItemReuseDAO;
import l2e.gameserver.data.dao.CharacterPremiumDAO;
import l2e.gameserver.data.dao.CharacterSkillSaveDAO;
import l2e.gameserver.data.dao.CharacterSkillsDAO;
import l2e.gameserver.data.dao.CharacterVariablesDAO;
import l2e.gameserver.data.dao.CharacterVisualDAO;
import l2e.gameserver.data.dao.DailyTasksDAO;
import l2e.gameserver.data.holder.CharNameHolder;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.data.htm.WarehouseCache;
import l2e.gameserver.data.parser.AdminParser;
import l2e.gameserver.data.parser.CategoryParser;
import l2e.gameserver.data.parser.CharTemplateParser;
import l2e.gameserver.data.parser.DamageLimitParser;
import l2e.gameserver.data.parser.DressArmorParser;
import l2e.gameserver.data.parser.DressCloakParser;
import l2e.gameserver.data.parser.DressHatParser;
import l2e.gameserver.data.parser.DressShieldParser;
import l2e.gameserver.data.parser.DressWeaponParser;
import l2e.gameserver.data.parser.EnchantSkillGroupsParser;
import l2e.gameserver.data.parser.ExpPercentLostParser;
import l2e.gameserver.data.parser.ExperienceParser;
import l2e.gameserver.data.parser.FightEventParser;
import l2e.gameserver.data.parser.FishParser;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.data.parser.PetsParser;
import l2e.gameserver.data.parser.PremiumAccountsParser;
import l2e.gameserver.data.parser.RecipeParser;
import l2e.gameserver.data.parser.SkillTreesParser;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.handler.itemhandlers.IItemHandler;
import l2e.gameserver.handler.itemhandlers.ItemHandler;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.handler.voicedcommandhandlers.VoicedCommandHandler;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.instancemanager.BotCheckManager;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.CursedWeaponsManager;
import l2e.gameserver.instancemanager.DailyRewardManager;
import l2e.gameserver.instancemanager.DailyTaskManager;
import l2e.gameserver.instancemanager.DimensionalRiftManager;
import l2e.gameserver.instancemanager.DoubleSessionManager;
import l2e.gameserver.instancemanager.DuelManager;
import l2e.gameserver.instancemanager.EpicBossManager;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.instancemanager.FortSiegeManager;
import l2e.gameserver.instancemanager.HandysBlockCheckerManager;
import l2e.gameserver.instancemanager.ItemsOnGroundManager;
import l2e.gameserver.instancemanager.MatchingRoomManager;
import l2e.gameserver.instancemanager.OnlineRewardManager;
import l2e.gameserver.instancemanager.PunishmentManager;
import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.instancemanager.RevengeManager;
import l2e.gameserver.instancemanager.RewardManager;
import l2e.gameserver.instancemanager.SiegeManager;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.instancemanager.VipManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.listener.events.EquipmentEvent;
import l2e.gameserver.listener.events.HennaEvent;
import l2e.gameserver.listener.events.TransformEvent;
import l2e.gameserver.listener.player.EquipmentListener;
import l2e.gameserver.listener.player.EventListener;
import l2e.gameserver.listener.player.HennaListener;
import l2e.gameserver.listener.player.ProfessionChangeListener;
import l2e.gameserver.listener.player.TransformListener;
import l2e.gameserver.listener.talk.OnAnswerListener;
import l2e.gameserver.model.AccessLevel;
import l2e.gameserver.model.ArenaParticipantsHolder;
import l2e.gameserver.model.BlockedList;
import l2e.gameserver.model.CategoryType;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.ClanMember;
import l2e.gameserver.model.ContactList;
import l2e.gameserver.model.EnchantSkillLearn;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.Macro;
import l2e.gameserver.model.MacroList;
import l2e.gameserver.model.MountType;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.PetData;
import l2e.gameserver.model.PlayerGroup;
import l2e.gameserver.model.Radar;
import l2e.gameserver.model.RecipeList;
import l2e.gameserver.model.Request;
import l2e.gameserver.model.ShortCuts;
import l2e.gameserver.model.ShotType;
import l2e.gameserver.model.SkillLearn;
import l2e.gameserver.model.TeleportWhereType;
import l2e.gameserver.model.TerritoryWard;
import l2e.gameserver.model.TimeStamp;
import l2e.gameserver.model.TradeItem;
import l2e.gameserver.model.TradeList;
import l2e.gameserver.model.UIKeysSettings;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.appearance.PcAppearance;
import l2e.gameserver.model.actor.events.PlayerEvents;
import l2e.gameserver.model.actor.instance.AirShipInstance;
import l2e.gameserver.model.actor.instance.BoatInstance;
import l2e.gameserver.model.actor.instance.ControlTowerInstance;
import l2e.gameserver.model.actor.instance.CubicInstance;
import l2e.gameserver.model.actor.instance.DefenderInstance;
import l2e.gameserver.model.actor.instance.DoorInstance;
import l2e.gameserver.model.actor.instance.EventChestInstance;
import l2e.gameserver.model.actor.instance.EventMonsterInstance;
import l2e.gameserver.model.actor.instance.FortBallistaInstance;
import l2e.gameserver.model.actor.instance.FortCommanderInstance;
import l2e.gameserver.model.actor.instance.FriendlyMobInstance;
import l2e.gameserver.model.actor.instance.GuardInstance;
import l2e.gameserver.model.actor.instance.PetInstance;
import l2e.gameserver.model.actor.instance.StaticObjectInstance;
import l2e.gameserver.model.actor.instance.TamedBeastInstance;
import l2e.gameserver.model.actor.instance.TrapInstance;
import l2e.gameserver.model.actor.instance.player.AchievementCounters;
import l2e.gameserver.model.actor.instance.player.AutoFarmOptions;
import l2e.gameserver.model.actor.instance.player.CharacterVariable;
import l2e.gameserver.model.actor.instance.player.NevitSystem;
import l2e.gameserver.model.actor.instance.player.PremiumBonus;
import l2e.gameserver.model.actor.instance.player.Recommendation;
import l2e.gameserver.model.actor.listener.BotCheckAnswerListner;
import l2e.gameserver.model.actor.listener.PlayerListenerList;
import l2e.gameserver.model.actor.protection.AdminProtection;
import l2e.gameserver.model.actor.stat.PcStat;
import l2e.gameserver.model.actor.status.PcStatus;
import l2e.gameserver.model.actor.tasks.player.CheckBotTask;
import l2e.gameserver.model.actor.tasks.player.CpPotionTask;
import l2e.gameserver.model.actor.tasks.player.DismountTask;
import l2e.gameserver.model.actor.tasks.player.FallingTask;
import l2e.gameserver.model.actor.tasks.player.FameTask;
import l2e.gameserver.model.actor.tasks.player.GameGuardCheckTask;
import l2e.gameserver.model.actor.tasks.player.HpPotionTask;
import l2e.gameserver.model.actor.tasks.player.InventoryEnableTask;
import l2e.gameserver.model.actor.tasks.player.LookingForFishTask;
import l2e.gameserver.model.actor.tasks.player.MpPotionTask;
import l2e.gameserver.model.actor.tasks.player.OnlineRewardTask;
import l2e.gameserver.model.actor.tasks.player.PcPointsTask;
import l2e.gameserver.model.actor.tasks.player.PetFeedTask;
import l2e.gameserver.model.actor.tasks.player.PremuimAccountTask;
import l2e.gameserver.model.actor.tasks.player.PunishmentTask;
import l2e.gameserver.model.actor.tasks.player.PvPFlagTask;
import l2e.gameserver.model.actor.tasks.player.RemoveWearItemsTask;
import l2e.gameserver.model.actor.tasks.player.RentPetTask;
import l2e.gameserver.model.actor.tasks.player.ResetChargesTask;
import l2e.gameserver.model.actor.tasks.player.ResetSoulsTask;
import l2e.gameserver.model.actor.tasks.player.SitDownTask;
import l2e.gameserver.model.actor.tasks.player.SoulPotionTask;
import l2e.gameserver.model.actor.tasks.player.StandUpTask;
import l2e.gameserver.model.actor.tasks.player.TeleportTask;
import l2e.gameserver.model.actor.tasks.player.TeleportWatchdogTask;
import l2e.gameserver.model.actor.tasks.player.TempHeroTask;
import l2e.gameserver.model.actor.tasks.player.VitalityTask;
import l2e.gameserver.model.actor.tasks.player.WarnUserTakeBreakTask;
import l2e.gameserver.model.actor.tasks.player.WaterTask;
import l2e.gameserver.model.actor.templates.BookmarkTemplate;
import l2e.gameserver.model.actor.templates.DressArmorTemplate;
import l2e.gameserver.model.actor.templates.DressCloakTemplate;
import l2e.gameserver.model.actor.templates.DressHatTemplate;
import l2e.gameserver.model.actor.templates.DressShieldTemplate;
import l2e.gameserver.model.actor.templates.DressWeaponTemplate;
import l2e.gameserver.model.actor.templates.ManufactureItemTemplate;
import l2e.gameserver.model.actor.templates.PetLevelTemplate;
import l2e.gameserver.model.actor.templates.PremiumItemTemplate;
import l2e.gameserver.model.actor.templates.ShortCutTemplate;
import l2e.gameserver.model.actor.templates.daily.DailyTaskTemplate;
import l2e.gameserver.model.actor.templates.items.Armor;
import l2e.gameserver.model.actor.templates.items.EtcItem;
import l2e.gameserver.model.actor.templates.items.Henna;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.actor.templates.items.Weapon;
import l2e.gameserver.model.actor.templates.npc.DamageLimit;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.actor.templates.player.AchiveTemplate;
import l2e.gameserver.model.actor.templates.player.FakeLocTemplate;
import l2e.gameserver.model.actor.templates.player.FakePassiveLocTemplate;
import l2e.gameserver.model.actor.templates.player.PcTeleportTemplate;
import l2e.gameserver.model.actor.templates.player.PcTemplate;
import l2e.gameserver.model.actor.templates.player.PlayerTaskTemplate;
import l2e.gameserver.model.actor.templates.player.ranking.PartyTemplate;
import l2e.gameserver.model.actor.templates.player.vip.VipTemplate;
import l2e.gameserver.model.actor.transform.Transform;
import l2e.gameserver.model.actor.transform.TransformTemplate;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.base.ClassLevel;
import l2e.gameserver.model.base.PlayerClass;
import l2e.gameserver.model.base.Race;
import l2e.gameserver.model.base.ShortcutType;
import l2e.gameserver.model.base.SubClass;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.entity.Duel;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.model.entity.Reflection;
import l2e.gameserver.model.entity.Siege;
import l2e.gameserver.model.entity.auction.AuctionsManager;
import l2e.gameserver.model.entity.events.AbstractFightEvent;
import l2e.gameserver.model.entity.events.cleft.AerialCleftEvent;
import l2e.gameserver.model.entity.events.custom.achievements.AchievementManager;
import l2e.gameserver.model.entity.events.model.FightEventManager;
import l2e.gameserver.model.entity.events.model.impl.MonsterAttackEvent;
import l2e.gameserver.model.entity.events.model.template.FightEventGameRoom;
import l2e.gameserver.model.entity.mods.SellBuffsManager;
import l2e.gameserver.model.entity.mods.facebook.template.FacebookProfile;
import l2e.gameserver.model.entity.underground_coliseum.UCTeam;
import l2e.gameserver.model.fishing.Fish;
import l2e.gameserver.model.fishing.Fishing;
import l2e.gameserver.model.holders.ItemHolder;
import l2e.gameserver.model.holders.SellBuffHolder;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.holders.SkillUseHolder;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.itemcontainer.Inventory;
import l2e.gameserver.model.items.itemcontainer.ItemContainer;
import l2e.gameserver.model.items.itemcontainer.PcFreight;
import l2e.gameserver.model.items.itemcontainer.PcInventory;
import l2e.gameserver.model.items.itemcontainer.PcRefund;
import l2e.gameserver.model.items.itemcontainer.PcWarehouse;
import l2e.gameserver.model.items.itemcontainer.PetInventory;
import l2e.gameserver.model.items.multisell.PreparedListContainer;
import l2e.gameserver.model.items.type.ActionType;
import l2e.gameserver.model.items.type.ArmorType;
import l2e.gameserver.model.items.type.EtcItemType;
import l2e.gameserver.model.items.type.WeaponType;
import l2e.gameserver.model.matching.MatchingRoom;
import l2e.gameserver.model.olympiad.AbstractOlympiadGame;
import l2e.gameserver.model.olympiad.OlympiadGameManager;
import l2e.gameserver.model.olympiad.OlympiadGameTask;
import l2e.gameserver.model.olympiad.OlympiadManager;
import l2e.gameserver.model.petition.PetitionMainGroup;
import l2e.gameserver.model.punishment.PunishmentTemplate;
import l2e.gameserver.model.punishment.PunishmentType;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.model.service.academy.AcademyList;
import l2e.gameserver.model.service.autoenchant.EnchantParams;
import l2e.gameserver.model.service.buffer.PlayerScheme;
import l2e.gameserver.model.service.premium.PremiumTemplate;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.SkillType;
import l2e.gameserver.model.skills.effects.AbnormalEffect;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectFlag;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.skills.l2skills.SkillSiegeFlag;
import l2e.gameserver.model.skills.targets.TargetType;
import l2e.gameserver.model.stats.BaseStats;
import l2e.gameserver.model.stats.Formulas;
import l2e.gameserver.model.stats.Stats;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.model.strings.server.ServerStorage;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.ZoneType;
import l2e.gameserver.model.zone.type.BossZone;
import l2e.gameserver.model.zone.type.FunPvpZone;
import l2e.gameserver.model.zone.type.JailZone;
import l2e.gameserver.model.zone.type.SiegeZone;
import l2e.gameserver.model.zone.type.WaterZone;
import l2e.gameserver.network.GameClient;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.communication.AuthServerCommunication;
import l2e.gameserver.network.serverpackets.AutoAttackStart;
import l2e.gameserver.network.serverpackets.CameraMode;
import l2e.gameserver.network.serverpackets.ChairSit;
import l2e.gameserver.network.serverpackets.ChangeWaitType;
import l2e.gameserver.network.serverpackets.CharInfo;
import l2e.gameserver.network.serverpackets.ConfirmDlg;
import l2e.gameserver.network.serverpackets.DeleteObject;
import l2e.gameserver.network.serverpackets.EtcStatusUpdate;
import l2e.gameserver.network.serverpackets.ExAutoSoulShot;
import l2e.gameserver.network.serverpackets.ExBrAgathionEnergyInfo;
import l2e.gameserver.network.serverpackets.ExBrExtraUserInfo;
import l2e.gameserver.network.serverpackets.ExDominionWarStart;
import l2e.gameserver.network.serverpackets.ExDuelUpdateUserInfo;
import l2e.gameserver.network.serverpackets.ExFishingEnd;
import l2e.gameserver.network.serverpackets.ExFishingStart;
import l2e.gameserver.network.serverpackets.ExGetBookMarkInfo;
import l2e.gameserver.network.serverpackets.ExGetOnAirShip;
import l2e.gameserver.network.serverpackets.ExOlympiadMode;
import l2e.gameserver.network.serverpackets.ExPrivateStorePackageMsg;
import l2e.gameserver.network.serverpackets.ExQuestItemList;
import l2e.gameserver.network.serverpackets.ExSetCompassZoneCode;
import l2e.gameserver.network.serverpackets.ExStartScenePlayer;
import l2e.gameserver.network.serverpackets.ExStorageMaxCount;
import l2e.gameserver.network.serverpackets.ExVoteSystemInfo;
import l2e.gameserver.network.serverpackets.FlyToLocation;
import l2e.gameserver.network.serverpackets.GameGuardQuery;
import l2e.gameserver.network.serverpackets.GameServerPacket;
import l2e.gameserver.network.serverpackets.GetOnVehicle;
import l2e.gameserver.network.serverpackets.HennaInfo;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.ItemList;
import l2e.gameserver.network.serverpackets.L2FriendStatus;
import l2e.gameserver.network.serverpackets.LogOutOk;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.gameserver.network.serverpackets.MoveToLocation;
import l2e.gameserver.network.serverpackets.MyTargetSelected;
import l2e.gameserver.network.serverpackets.NickNameChanged;
import l2e.gameserver.network.serverpackets.NormalCamera;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.NpcInfo;
import l2e.gameserver.network.serverpackets.NpcInfoPoly;
import l2e.gameserver.network.serverpackets.ObserverEnd;
import l2e.gameserver.network.serverpackets.ObserverStart;
import l2e.gameserver.network.serverpackets.PartySmallWindowUpdate;
import l2e.gameserver.network.serverpackets.PetInventoryUpdate;
import l2e.gameserver.network.serverpackets.PlaySound;
import l2e.gameserver.network.serverpackets.PledgeShowMemberListDelete;
import l2e.gameserver.network.serverpackets.PledgeShowMemberListDeleteAll;
import l2e.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import l2e.gameserver.network.serverpackets.PrivateStoreBuyList;
import l2e.gameserver.network.serverpackets.PrivateStoreBuyManageList;
import l2e.gameserver.network.serverpackets.PrivateStoreBuyMsg;
import l2e.gameserver.network.serverpackets.PrivateStoreSellList;
import l2e.gameserver.network.serverpackets.PrivateStoreSellManageList;
import l2e.gameserver.network.serverpackets.PrivateStoreSellMsg;
import l2e.gameserver.network.serverpackets.RecipeShopMsg;
import l2e.gameserver.network.serverpackets.RecipeShopSellList;
import l2e.gameserver.network.serverpackets.RelationChanged;
import l2e.gameserver.network.serverpackets.Ride;
import l2e.gameserver.network.serverpackets.ServerClose;
import l2e.gameserver.network.serverpackets.SetupGauge;
import l2e.gameserver.network.serverpackets.ShortCutInit;
import l2e.gameserver.network.serverpackets.ShowTutorialMark;
import l2e.gameserver.network.serverpackets.SkillCoolTime;
import l2e.gameserver.network.serverpackets.SkillList;
import l2e.gameserver.network.serverpackets.Snoop;
import l2e.gameserver.network.serverpackets.SocialAction;
import l2e.gameserver.network.serverpackets.SpawnItem;
import l2e.gameserver.network.serverpackets.SpecialCamera;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.StopMove;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.network.serverpackets.TargetSelected;
import l2e.gameserver.network.serverpackets.TargetUnselected;
import l2e.gameserver.network.serverpackets.TradeDone;
import l2e.gameserver.network.serverpackets.TradePressOtherOk;
import l2e.gameserver.network.serverpackets.TradePressOwnOk;
import l2e.gameserver.network.serverpackets.TradeStart;
import l2e.gameserver.network.serverpackets.UserInfo;
import l2e.gameserver.network.serverpackets.ValidateLocation;
import l2e.gameserver.taskmanager.AttackStanceTaskManager;
import l2e.scripts.events.Hitman;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class Player extends Playable implements PlayerGroup {
   public static final int ID_NONE = -1;
   public static final int REQUEST_TIMEOUT = 15;
   public static final int STORE_PRIVATE_NONE = 0;
   public static final int STORE_PRIVATE_SELL = 1;
   public static final int STORE_PRIVATE_BUY = 3;
   public static final int STORE_PRIVATE_MANUFACTURE = 5;
   public static final int STORE_PRIVATE_PACKAGE_SELL = 8;
   private static final List<HennaListener> HENNA_LISTENERS = new LinkedList<>();
   private static final List<EquipmentListener> GLOBAL_EQUIPMENT_LISTENERS = new LinkedList<>();
   private static final List<ProfessionChangeListener> GLOBAL_PROFESSION_CHANGE_LISTENERS = new LinkedList<>();
   private final List<EquipmentListener> _equipmentListeners = new LinkedList<>();
   private final List<TransformListener> _transformListeners = new LinkedList<>();
   private final List<ProfessionChangeListener> _professionChangeListeners = new LinkedList<>();
   private final List<EventListener> _eventListeners = new LinkedList<>();
   private int _pledgeItemId = 0;
   private long _pledgePrice = 0L;
   private boolean _isInAcademyList = false;
   private int _botRating;
   private GameClient _client;
   private final String _accountName;
   private long _deleteTimer;
   private volatile boolean _isOnline = false;
   private long _onlineTime;
   private long _onlineBeginTime;
   private long _lastAccess;
   private long _uptime;
   private int _ping = -1;
   private int _baseClass;
   private int _activeClass;
   private int _classIndex = 0;
   private int _controlItemId;
   private PetData _data;
   private PetLevelTemplate _leveldata;
   private int _curFeed;
   private boolean _petItems = false;
   private final PcAppearance _appearance;
   private long _expBeforeDeath;
   private int _karma;
   private int _pvpKills;
   private int _pkKills;
   private byte _pvpFlag;
   private int _fame;
   private int _pcBangPoints = 0;
   private long _gamePoints;
   private byte _siegeState = 0;
   private int _siegeSide = 0;
   private int _curWeightPenalty = 0;
   private int _lastCompassZone;
   private boolean _isIn7sDungeon = false;
   private boolean _isInKrateisCube = false;
   private boolean _canFeed;
   private boolean _isInSiege;
   private boolean _isInHideoutSiege = false;
   private PcTemplate _antifeedTemplate = null;
   private boolean _antifeedSex;
   private int _bookmarkslot = 0;
   private Vehicle _vehicle = null;
   private Location _inVehiclePosition;
   private MountType _mountType = MountType.NONE;
   private int _mountNpcId;
   private int _mountLevel;
   private int _mountObjectID = 0;
   private int _telemode = 0;
   private boolean _inCrystallize;
   private boolean _inCraftMode;
   private boolean _inInStoreNow = false;
   private boolean _offline = false;
   private Transform _transformation;
   private boolean _waitTypeSitting;
   private StaticObjectInstance _sittingObject;
   private int _lastX;
   private int _lastY;
   private int _lastZ;
   private boolean _observerMode = false;
   private final Recommendation _recommendation = new Recommendation(this);
   private final NevitSystem _nevitSystem = new NevitSystem(this);
   private final AutoFarmOptions _autoFarmSystem = new AutoFarmOptions(this);
   private final PcInventory _inventory = new PcInventory(this);
   private final PcFreight _freight = new PcFreight(this);
   private PcWarehouse _warehouse;
   private PcRefund _refund;
   private int _privatestore;
   private TradeList _activeTradeList;
   private ItemContainer _activeWarehouse;
   private String _storeName = "";
   private TradeList _sellList;
   private TradeList _buyList;
   private PreparedListContainer _currentMultiSell = null;
   private int _newbie;
   private boolean _noble = false;
   private boolean _hero = false;
   private boolean _timeHero = false;
   private Npc _lastFolkNpc = null;
   private int _questNpcObject = 0;
   private int _hennaSTR;
   private int _hennaINT;
   private int _hennaDEX;
   private int _hennaMEN;
   private int _hennaWIT;
   private int _hennaCON;
   private Summon _summon = null;
   private Decoy _decoy = null;
   private TrapInstance _trap = null;
   private int _agathionId = 0;
   private boolean _minimapAllowed = false;
   private final Radar _radar;
   private int _clanId;
   private Clan _clan;
   private int _apprentice = 0;
   private int _sponsor = 0;
   private long _clanJoinExpiryTime;
   private long _clanCreateExpiryTime;
   private int _powerGrade = 0;
   private int _clanPrivileges = 0;
   private int _pledgeClass = 0;
   private int _pledgeType = 0;
   private int _lvlJoinedAcademy = 0;
   private int _wantsPeace = 0;
   private long _lastMovePacket = 0L;
   private long _lastAttackPacket = 0L;
   private long _lastRequestMagicPacket = 0L;
   private int _deathPenaltyBuffLevel = 0;
   private final AtomicInteger _charges = new AtomicInteger();
   private boolean _inOlympiadMode = false;
   private boolean _OlympiadStart = false;
   private AbstractOlympiadGame _olympiadGame;
   private int _olympiadGameId = -1;
   private int _olympiadSide = -1;
   public int olyBuff = 0;
   private boolean _isInDuel = false;
   private int _duelState = 0;
   private int _duelId = 0;
   private int _souls = 0;
   private long _premiumOnlineTime = 0L;
   private FacebookProfile _facebookProfile = null;
   private Map<Stats, Double> _servitorShare;
   private Location _fallingLoc = null;
   private Location _saveLoc = null;
   private Location _bookmarkLocation = null;
   private final Map<Integer, PlayerTaskTemplate> _activeTasks = new ConcurrentHashMap<>();
   private int _lastDailyTasks = 0;
   private int _lastWeeklyTasks = 0;
   private int _lastMonthTasks = 0;
   private Future<?> _mountFeedTask;
   private ScheduledFuture<?> _chargeTask = null;
   private ScheduledFuture<?> _soulTask = null;
   private ScheduledFuture<?> _taskforfish;
   private ScheduledFuture<?> _dismountTask;
   private ScheduledFuture<?> _fameTask;
   private ScheduledFuture<?> _vitalityTask;
   private ScheduledFuture<?> _pcCafePointsTask;
   private ScheduledFuture<?> _premiumTask;
   private ScheduledFuture<?> _tempHeroTask;
   private ScheduledFuture<?> _botCheckTask;
   private ScheduledFuture<?> _onlineRewardTask;
   private volatile ScheduledFuture<?> _teleportWatchdog;
   private ScheduledFuture<?> _broadcastCharInfoTask;
   private ScheduledFuture<?> _broadcastStatusUpdateTask;
   private ScheduledFuture<?> _effectsUpdateTask;
   private ScheduledFuture<?> _updateAndBroadcastStatusTask;
   private Future<?> _userInfoTask;
   public ScheduledFuture<?> _captureTask;
   ScheduledFuture<?> _previewDoneTask;
   private ScheduledFuture<?> _punishmentTask;
   private Pair<Integer, OnAnswerListener> _askDialog = null;
   private Map<Integer, SubClass> _subClasses;
   private List<TamedBeastInstance> _tamedBeast = null;
   private volatile Map<Integer, ManufactureItemTemplate> _manufactureItems;
   private final Map<Integer, BookmarkTemplate> _tpbookmarks = new ConcurrentHashMap<>();
   private final Map<Integer, RecipeList> _dwarvenRecipeBook = new ConcurrentHashMap<>();
   private final Map<Integer, RecipeList> _commonRecipeBook = new ConcurrentHashMap<>();
   private final Map<Integer, PremiumItemTemplate> _premiumItems = new ConcurrentHashMap<>();
   private final Set<Player> _snoopListener = ConcurrentHashMap.newKeySet(1);
   private final Set<Player> _snoopedPlayer = ConcurrentHashMap.newKeySet(1);
   private final Map<String, QuestState> _quests = new ConcurrentHashMap<>();
   private final Map<String, CharacterVariable> _variables = new ConcurrentHashMap<>();
   private final Map<Integer, Future<?>> _autoPotTasks = new HashMap<>();
   private final List<String> _bannedActions = new ArrayList<>();
   private final ShortCuts _shortCuts = new ShortCuts(this);
   private final MacroList _macros = new MacroList(this);
   private Henna[] _henna = new Henna[3];
   private Calendar _createDate = Calendar.getInstance();
   private FakePlayer _fakePlayerUnderControl = null;
   private boolean _fakePlayer = false;
   private FakeLocTemplate _fakeLocation = null;
   private FakePassiveLocTemplate _fakePassiveLocation = null;
   private final ContactList _contactList = new ContactList(this);
   private final Map<Integer, PcTeleportTemplate> _communtyTeleports = new ConcurrentHashMap<>();
   private final Map<Integer, Integer> _achievementLevels = new ConcurrentHashMap<>();
   private final AchievementCounters _achievementCounters = new AchievementCounters(this);
   private SystemMessageId _noDuelReason = SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL;
   final int[] _charInfoSlots = new int[11];
   final int[] _userInfoSlots = new int[11];
   private Location _currentSkillWorldPosition;
   private AccessLevel _accessLevel;
   private boolean _messageRefusal = false;
   private boolean _silenceMode = false;
   private List<Integer> _silenceModeExcluded;
   private boolean _dietMode = false;
   private boolean _exchangeRefusal = false;
   private Party _party;
   private Player _activeRequester;
   private long _requestExpireTime = 0L;
   private final Request _request = new Request(this);
   private ItemInstance _arrowItem;
   private ItemInstance _boltItem;
   private long _protectEndTime = 0L;
   private long _respawnProtectTime = 0L;
   private int _lectureMark;
   public int expertiseIndex = 0;
   public static final int[] EXPERTISE_LEVELS = new int[]{0, 20, 40, 52, 61, 76, 80, 84, Integer.MAX_VALUE};
   private boolean _matchingRoomWindowOpened = false;
   private MatchingRoom _matchingRoom;
   private PetitionMainGroup _petitionGroup;
   private final List<Integer> _loadedImages = new ArrayList<>();
   private long _teleportProtectEndTime = 0L;
   private long _recentFakeDeathEndTime = 0L;
   private boolean _isFakeDeath;
   private Weapon _fistsWeaponItem;
   private final Map<Integer, String> _chars = new LinkedHashMap<>();
   private int _expertiseArmorPenalty = 0;
   private int _expertiseWeaponPenalty = 0;
   private boolean _isEnchanting = false;
   private int _activeEnchantItemId = -1;
   private int _activeEnchantSupportItemId = -1;
   private int _activeEnchantAttrItemId = -1;
   protected boolean _inventoryDisable = false;
   private final Map<Integer, CubicInstance> _cubics = new ConcurrentSkipListMap<>();
   protected Set<Integer> _activeSoulShots = ConcurrentHashMap.newKeySet(1);
   private final ReentrantLock _subclassLock = new ReentrantLock();
   public final ReentrantLock soulShotLock = new ReentrantLock();
   public final ReentrantReadWriteLock _useItemLock = new ReentrantReadWriteLock();
   private int UCKills = 0;
   private int UCDeaths = 0;
   public static final int UC_STATE_NONE = 0;
   public static final int UC_STATE_POINT = 1;
   public static final int UC_STATE_ARENA = 2;
   private int UCState = 0;
   private byte _handysBlockCheckerEventArena = -1;
   private final int[] _loto = new int[5];
   private final int[] _race = new int[2];
   private final BlockedList _blockList = new BlockedList(this);
   private Fishing _fishCombat;
   private boolean _fishing = false;
   private int _fishx = 0;
   private int _fishy = 0;
   private int _fishz = 0;
   private Set<Integer> _transformAllowedSkills;
   private ScheduledFuture<?> _taskRentPet;
   private ScheduledFuture<?> _taskWater;
   private SkillUseHolder _currentSkill;
   private SkillUseHolder _currentPetSkill;
   private SkillUseHolder _queuedSkill;
   private int _cursedWeaponEquippedId = 0;
   private boolean _combatFlagEquippedId = false;
   private int _reviveRequested = 0;
   private double _revivePower = 0.0;
   private boolean _revivePet = false;
   private double _cpUpdateIncCheck = 0.0;
   private double _cpUpdateDecCheck = 0.0;
   private double _cpUpdateInterval = 0.0;
   private double _mpUpdateIncCheck = 0.0;
   private double _mpUpdateDecCheck = 0.0;
   private double _mpUpdateInterval = 0.0;
   private int _clientX;
   private int _clientY;
   private int _clientZ;
   private int _clientHeading;
   private static final int FALLING_VALIDATION_DELAY = 5000;
   private volatile long _fallingTimestamp = 0L;
   private int _multiSocialTarget = 0;
   private int _multiSociaAction = 0;
   private int _movieId = 0;
   private String _adminConfirmCmd = null;
   private volatile long _lastItemAuctionInfoRequest = 0L;
   private Future<?> _PvPRegTask;
   private long _pvpFlagLasts;
   private long _notMoveUntil = 0L;
   private Map<Integer, Skill> _customSkills = null;
   private int _kamaID = 0;
   private AdminProtection _AdminProtection = null;
   private boolean _canRevive = true;
   private final Map<String, Object> quickVars = new ConcurrentHashMap<>();
   private boolean _isSellingBuffs = false;
   private List<SellBuffHolder> _sellingBuffs = null;
   protected int _cleftKills = 0;
   protected int _cleftDeaths = 0;
   protected int _cleftKillTowers = 0;
   protected boolean _cleftCat = false;
   private long _lastNotAfkTime = 0L;
   private FightEventGameRoom _fightEventGameRoom = null;
   private ScheduledFuture<?> _hpPotionTask;
   private ScheduledFuture<?> _mpPotionTask;
   private ScheduledFuture<?> _cpPotionTask;
   private ScheduledFuture<?> _soulPotionTask;
   private int _chatMsg = 0;
   private final List<PlayerScheme> _buffSchemes = new CopyOnWriteArrayList<>();
   private final PremiumBonus _bonus = new PremiumBonus();
   private final EnchantParams _enchantParams = new EnchantParams();
   private final List<Integer> _weaponSkins = new ArrayList<>();
   private final List<Integer> _armorSkins = new ArrayList<>();
   private final List<Integer> _shieldSkins = new ArrayList<>();
   private final List<Integer> _cloakSkins = new ArrayList<>();
   private final List<Integer> _hairSkins = new ArrayList<>();
   private int _activeWeaponSkin = 0;
   private int _activeArmorSkin = 0;
   private int _activeShieldSkin = 0;
   private int _activeCloakSkin = 0;
   private int _activeHairSkin = 0;
   private int _activeMaskSkin = 0;
   private int _vipLevel = 0;
   private long _vipPoints = 0L;
   public boolean _entering = true;
   private final List<Integer> _revengeList = new ArrayList<>();
   private boolean _isRevengeActive = false;
   private UIKeysSettings _uiKeySettings;
   private boolean _married = false;
   private int _partnerId = 0;
   private int _coupleId = 0;
   private boolean _engagerequest = false;
   private int _engageid = 0;
   private boolean _marryrequest = false;
   private boolean _marryaccepted = false;
   private String _lastPetitionGmName = null;
   private volatile List<QuestState> _notifyQuestOfDeathList;
   private ClassId _learningClass = this.getClassId();
   private ScheduledFuture<?> _taskWarnUserTakeBreak;
   private Fish _fish;
   private ItemInstance _lure = null;
   private final Map<Integer, TimeStamp> _reuseTimeStampsItems = new ConcurrentHashMap<>();
   private final Map<Integer, TimeStamp> _reuseTimeStampsSkills = new ConcurrentHashMap<>();
   private final List<Integer> _friendList = new CopyOnWriteArrayList<>();
   private int _hoursInGame = 0;

   public boolean isSpawnProtected() {
      return this._protectEndTime > (long)GameTimeController.getInstance().getGameTicks();
   }

   public boolean isRespawnProtected() {
      return this._respawnProtectTime > System.currentTimeMillis();
   }

   public void setRespawnProtect() {
      this._respawnProtectTime = System.currentTimeMillis() + 5000L;
   }

   public boolean isTeleportProtected() {
      return this._teleportProtectEndTime > (long)GameTimeController.getInstance().getGameTicks();
   }

   @Override
   public void doAttack(Creature target) {
      if (target != null && target.isPlayer() && this.isPKProtected(target.getActingPlayer())) {
         this.sendMessage("You can't attack this player!");
         this.sendActionFailed();
      } else {
         super.doAttack(target);
         this.setRecentFakeDeath(false);
      }
   }

   @Override
   public void doCast(Skill skill) {
      super.doCast(skill);
      this.setRecentFakeDeath(false);
   }

   public void setPvpFlagLasts(long time) {
      this._pvpFlagLasts = time;
   }

   public long getPvpFlagLasts() {
      return this._pvpFlagLasts;
   }

   public void startPvPFlag() {
      if (ZoneManager.getInstance().getOlympiadStadium(this) == null) {
         this.updatePvPFlag(1);
         if (this.getFarmSystem().isAutofarming()) {
            this.getFarmSystem().stopFarmTask(false);
         }

         if (this._PvPRegTask == null) {
            this._PvPRegTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new PvPFlagTask(this), 1000L, 1000L);
         }
      }
   }

   public void stopPvpRegTask() {
      try {
         if (this._PvPRegTask != null) {
            this._PvPRegTask.cancel(true);
            this._PvPRegTask = null;
         }
      } catch (Exception var2) {
      }
   }

   public void stopPvPFlag() {
      this.stopPvpRegTask();
      this.updatePvPFlag(0);
      this._PvPRegTask = null;
   }

   public static Player create(PcTemplate template, String accountName, String name, PcAppearance app) {
      Player player = new Player(IdFactory.getInstance().getNextId(), template, accountName, app);
      player.setName(name);
      player.setCreateDate(Calendar.getInstance());
      player.setBaseClass(player.getClassId());
      player.setNewbie(1);
      player.getRecommendation().setRecomTimeLeft(3600);
      player.getRecommendation().setRecomLeft(20);
      player.getNevitSystem().restartSystem();
      player.setChatMsg(Config.CHAT_MSG_SIMPLE);
      return CharacterDAO.getInstance().isPlayerCreated(player) ? player : null;
   }

   public String getAccountName() {
      return this.getClient() == null ? this.getAccountNamePlayer() : this.getClient().getLogin();
   }

   public String getAccountNamePlayer() {
      return this._accountName;
   }

   public Map<Integer, String> getAccountChars() {
      return this._chars;
   }

   public int getRelation(Player target) {
      int result = 0;
      if (this.getClan() != null) {
         result |= 64;
         if (this.getClan() == target.getClan()) {
            result |= 256;
         }

         if (this.getAllyId() != 0) {
            result |= 65536;
         }
      }

      if (this.isClanLeader()) {
         result |= 128;
      }

      if (this.getParty() != null && this.getParty() == target.getParty()) {
         result |= 32;

         for(int i = 0; i < this.getParty().getMembers().size(); ++i) {
            if (this.getParty().getMembers().get(i) == this) {
               switch(i) {
                  case 0:
                     result |= 16;
                     break;
                  case 1:
                     result |= 8;
                     break;
                  case 2:
                     result |= 7;
                     break;
                  case 3:
                     result |= 6;
                     break;
                  case 4:
                     result |= 5;
                     break;
                  case 5:
                     result |= 4;
                     break;
                  case 6:
                     result |= 3;
                     break;
                  case 7:
                     result |= 2;
                     break;
                  case 8:
                     result |= 1;
               }
            }
         }
      }

      if (this.getSiegeState() != 0) {
         if (TerritoryWarManager.getInstance().getRegisteredTerritoryId(this) != 0) {
            result |= 524288;
         } else {
            result |= 512;
            if (this.getSiegeState() != target.getSiegeState()) {
               result |= 4096;
            } else {
               result |= 2048;
            }

            if (this.getSiegeState() == 1) {
               result |= 1024;
            }
         }
      }

      if (this.getClan() != null
         && target.getClan() != null
         && target.getPledgeType() != -1
         && this.getPledgeType() != -1
         && target.getClan().isAtWarWith(this.getClan().getId())) {
         result |= 32768;
         if (this.getClan().isAtWarWith(target.getClan().getId())) {
            result |= 16384;
         }
      }

      if (this.getBlockCheckerArena() != -1) {
         result |= 512;
         ArenaParticipantsHolder holder = HandysBlockCheckerManager.getInstance().getHolder(this.getBlockCheckerArena());
         if (holder.getPlayerTeam(this) == 0) {
            result |= 4096;
         } else {
            result |= 2048;
         }

         result |= 1024;
      }

      if (target.isInsideZone(ZoneId.FUN_PVP) && !this.isFriend(target)) {
         FunPvpZone zone = ZoneManager.getInstance().getZone(target, FunPvpZone.class);
         if (zone != null && zone.isPvpZone()) {
            result |= 32768;
            result |= 16384;
         }
      }

      for(AbstractFightEvent e : this.getFightEvents()) {
         result = e.getRelation(this, target, result);
      }

      return result;
   }

   public static Player load(int objectId) {
      return CharacterDAO.restore(objectId);
   }

   private void initPcStatusUpdateValues() {
      this._cpUpdateInterval = this.getMaxCp() / 352.0;
      this._cpUpdateIncCheck = this.getMaxCp();
      this._cpUpdateDecCheck = this.getMaxCp() - this._cpUpdateInterval;
      this._mpUpdateInterval = this.getMaxMp() / 352.0;
      this._mpUpdateIncCheck = this.getMaxMp();
      this._mpUpdateDecCheck = this.getMaxMp() - this._mpUpdateInterval;
   }

   public Player(int objectId, PcTemplate template, String accountName, PcAppearance app) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.Player);
      super.initCharStatusUpdateValues();
      this.initPcStatusUpdateValues();
      this._accountName = accountName;
      app.setOwner(this);
      this._appearance = app;
      this.getAI();
      this._radar = new Radar(this);
      this.startVitalityTask();
   }

   public final PcStat getStat() {
      return (PcStat)super.getStat();
   }

   @Override
   public void initCharStat() {
      this.setStat(new PcStat(this));
   }

   public final PcStatus getStatus() {
      return (PcStatus)super.getStatus();
   }

   @Override
   public void initCharStatus() {
      this.setStatus(new PcStatus(this));
   }

   @Override
   public void initCharEvents() {
      this.setCharEvents(new PlayerEvents(this));
   }

   public PlayerEvents getEvents() {
      return (PlayerEvents)super.getEvents();
   }

   public final PcAppearance getAppearance() {
      return this._appearance;
   }

   public final PcTemplate getBaseTemplate() {
      return CharTemplateParser.getInstance().getTemplate(this._baseClass);
   }

   public final PcTemplate getTemplate() {
      return (PcTemplate)super.getTemplate();
   }

   public void setTemplate(ClassId newclass) {
      super.setTemplate(CharTemplateParser.getInstance().getTemplate(newclass));
   }

   @Override
   public CharacterAI initAI() {
      return new PlayerAI(this);
   }

   @Override
   public final int getLevel() {
      return this.getStat().getLevel();
   }

   @Override
   public double getLevelMod() {
      if (this.isTransformed()) {
         double levelMod = this.getTransformation().getLevelMod(this);
         if (levelMod > -1.0) {
            return levelMod;
         }
      }

      return (89.0 + (double)this.getLevel()) / 100.0;
   }

   public int getNewbie() {
      return this._newbie;
   }

   public void setNewbie(int newbieRewards) {
      this._newbie = newbieRewards;
   }

   public void setBaseClass(int baseClass) {
      this._baseClass = baseClass;
   }

   public void setBaseClass(ClassId classId) {
      this._baseClass = classId.ordinal();
   }

   public boolean isInStoreMode() {
      return this.getPrivateStoreType() > 0;
   }

   public void setIsInStoreNow(boolean b) {
      this._inInStoreNow = b;
   }

   public boolean isInStoreNow() {
      return this._inInStoreNow;
   }

   public boolean isInCraftMode() {
      return this._inCraftMode;
   }

   public void isInCraftMode(boolean b) {
      this._inCraftMode = b;
   }

   public void logout() {
      this.logout(true);
   }

   public void logout(boolean closeClient) {
      try {
         this.closeNetConnection(closeClient);
      } catch (Exception var3) {
         _log.log(Level.WARNING, "Exception on logout(): " + var3.getMessage(), (Throwable)var3);
      }
   }

   public RecipeList[] getCommonRecipeBook() {
      return this._commonRecipeBook.values().toArray(new RecipeList[this._commonRecipeBook.values().size()]);
   }

   public RecipeList[] getDwarvenRecipeBook() {
      return this._dwarvenRecipeBook.values().toArray(new RecipeList[this._dwarvenRecipeBook.values().size()]);
   }

   public void registerCommonRecipeList(RecipeList recipe, boolean saveToDb) {
      this._commonRecipeBook.put(recipe.getId(), recipe);
      if (saveToDb) {
         this.insertNewRecipeParser(recipe.getId(), false);
      }
   }

   public void registerDwarvenRecipeList(RecipeList recipe, boolean saveToDb) {
      this._dwarvenRecipeBook.put(recipe.getId(), recipe);
      if (saveToDb) {
         this.insertNewRecipeParser(recipe.getId(), true);
      }
   }

   public boolean hasRecipeList(int recipeId) {
      return this._dwarvenRecipeBook.containsKey(recipeId) || this._commonRecipeBook.containsKey(recipeId);
   }

   public void unregisterRecipeList(int recipeId) {
      if (this._dwarvenRecipeBook.remove(recipeId) != null) {
         this.deleteRecipeParser(recipeId, true);
      } else if (this._commonRecipeBook.remove(recipeId) != null) {
         this.deleteRecipeParser(recipeId, false);
      } else {
         _log.warning("Attempted to remove unknown RecipeList: " + recipeId);
      }

      for(ShortCutTemplate sc : this.getAllShortCuts()) {
         if (sc != null && sc.getId() == recipeId && sc.getType() == ShortcutType.RECIPE) {
            this.deleteShortCut(sc.getSlot(), sc.getPage());
         }
      }
   }

   private void insertNewRecipeParser(int recipeId, boolean isDwarf) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("INSERT INTO character_recipebook (charId, id, classIndex, type) values(?,?,?,?)");
      ) {
         statement.setInt(1, this.getObjectId());
         statement.setInt(2, recipeId);
         statement.setInt(3, isDwarf ? this._classIndex : 0);
         statement.setInt(4, isDwarf ? 1 : 0);
         statement.execute();
      } catch (SQLException var35) {
         if (_log.isLoggable(Level.SEVERE)) {
            _log.log(Level.SEVERE, "SQL exception while inserting recipe: " + recipeId + " from character " + this.getObjectId(), (Throwable)var35);
         }
      }
   }

   private void deleteRecipeParser(int recipeId, boolean isDwarf) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("DELETE FROM character_recipebook WHERE charId=? AND id=? AND classIndex=?");
      ) {
         statement.setInt(1, this.getObjectId());
         statement.setInt(2, recipeId);
         statement.setInt(3, isDwarf ? this._classIndex : 0);
         statement.execute();
      } catch (SQLException var35) {
         if (_log.isLoggable(Level.SEVERE)) {
            _log.log(Level.SEVERE, "SQL exception while deleting recipe: " + recipeId + " from character " + this.getObjectId(), (Throwable)var35);
         }
      }
   }

   public int getLastQuestNpcObject() {
      return this._questNpcObject;
   }

   public void setLastQuestNpcObject(int npcId) {
      this._questNpcObject = npcId;
   }

   public QuestState getQuestState(String quest) {
      return this._quests.get(quest);
   }

   public void setQuestState(QuestState qs) {
      this._quests.put(qs.getQuestName(), qs);
   }

   public boolean hasQuestState(String quest) {
      return this._quests.containsKey(quest);
   }

   public void delQuestState(String quest) {
      this._quests.remove(quest);
   }

   private QuestState[] addToQuestStateArray(QuestState[] questStateArray, QuestState state) {
      int len = questStateArray.length;
      QuestState[] tmp = new QuestState[len + 1];
      System.arraycopy(questStateArray, 0, tmp, 0, len);
      tmp[len] = state;
      return tmp;
   }

   public Quest[] getAllActiveQuests() {
      List<Quest> quests = new ArrayList<>();

      for(QuestState qs : this._quests.values()) {
         if (qs != null && qs.getQuest() != null && (qs.isStarted() || Config.DEVELOPER)) {
            int questId = qs.getQuest().getId();
            if (questId <= 19999 && questId >= 1) {
               quests.add(qs.getQuest());
            }
         }
      }

      return quests.toArray(new Quest[quests.size()]);
   }

   public QuestState[] getQuestsForAttacks(Npc npc) {
      QuestState[] states = null;

      for(Quest quest : npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_ATTACK)) {
         if (this.getQuestState(quest.getName()) != null) {
            if (states == null) {
               states = new QuestState[]{this.getQuestState(quest.getName())};
            } else {
               states = this.addToQuestStateArray(states, this.getQuestState(quest.getName()));
            }
         }
      }

      return states;
   }

   public QuestState[] getQuestsForKills(Npc npc) {
      QuestState[] states = null;

      for(Quest quest : npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_KILL)) {
         if (this.getQuestState(quest.getName()) != null) {
            if (states == null) {
               states = new QuestState[]{this.getQuestState(quest.getName())};
            } else {
               states = this.addToQuestStateArray(states, this.getQuestState(quest.getName()));
            }
         }
      }

      return states;
   }

   public QuestState[] getQuestsForTalk(int npcId) {
      QuestState[] states = null;
      List<Quest> quests = NpcsParser.getInstance().getTemplate(npcId).getEventQuests(Quest.QuestEventType.ON_TALK);
      if (quests != null) {
         for(Quest quest : quests) {
            if (quest != null && this.getQuestState(quest.getName()) != null) {
               if (states == null) {
                  states = new QuestState[]{this.getQuestState(quest.getName())};
               } else {
                  states = this.addToQuestStateArray(states, this.getQuestState(quest.getName()));
               }
            }
         }
      }

      return states;
   }

   public QuestState processQuestEvent(String quest, String event) {
      QuestState retval = null;
      if (event == null) {
         event = "";
      }

      QuestState qs = this.getQuestState(quest);
      if (qs == null && event.isEmpty()) {
         return retval;
      } else {
         if (qs == null) {
            Quest q = QuestManager.getInstance().getQuest(quest);
            if (q == null) {
               return retval;
            }

            qs = q.newQuestState(this);
         }

         if (qs != null && this.getLastQuestNpcObject() > 0) {
            GameObject object = World.getInstance().findObject(this.getLastQuestNpcObject());
            if (object instanceof Npc && this.isInsideRadius(object, 150, false, false)) {
               Npc npc = (Npc)object;
               QuestState[] states = this.getQuestsForTalk(npc.getId());
               if (states != null) {
                  for(QuestState state : states) {
                     if (state.getQuest().getName().equals(qs.getQuest().getName())) {
                        if (qs.getQuest().notifyEvent(event, npc, this)) {
                           this.showQuestWindow(quest, State.getStateName(qs.getState()));
                        }

                        retval = qs;
                        break;
                     }
                  }
               }
            }
         }

         return retval;
      }
   }

   private void showQuestWindow(String questId, String stateId) {
      String path = "data/scripts/quests/" + questId + "/" + stateId + ".htm";
      String content = HtmCache.getInstance().getHtm(this, this.getLang(), path);
      if (content != null) {
         NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
         npcReply.setHtml(this, content);
         this.sendPacket(npcReply);
      }

      this.sendActionFailed();
   }

   public void addNotifyQuestOfDeath(QuestState qs) {
      if (qs != null) {
         if (!this.getNotifyQuestOfDeath().contains(qs)) {
            this.getNotifyQuestOfDeath().add(qs);
         }
      }
   }

   public void removeNotifyQuestOfDeath(QuestState qs) {
      if (qs != null && this._notifyQuestOfDeathList != null) {
         this._notifyQuestOfDeathList.remove(qs);
      }
   }

   public final List<QuestState> getNotifyQuestOfDeath() {
      if (this._notifyQuestOfDeathList == null) {
         synchronized(this) {
            if (this._notifyQuestOfDeathList == null) {
               this._notifyQuestOfDeathList = new CopyOnWriteArrayList<>();
            }
         }
      }

      return this._notifyQuestOfDeathList;
   }

   public final boolean isNotifyQuestOfDeathEmpty() {
      return this._notifyQuestOfDeathList == null || this._notifyQuestOfDeathList.isEmpty();
   }

   public ShortCutTemplate[] getAllShortCuts() {
      return this._shortCuts.getAllShortCuts();
   }

   public ShortCutTemplate getShortCut(int slot, int page) {
      return this._shortCuts.getShortCut(slot, page);
   }

   private PcTemplate createRandomAntifeedTemplate() {
      Race race = null;

      while(race == null) {
         race = Race.values()[Rnd.get(Race.values().length)];
         if (race == this.getRace() || race == Race.Kamael) {
            race = null;
         }
      }

      for(ClassId c : ClassId.values()) {
         PlayerClass p = PlayerClass.values()[c.getId()];
         if (p.isOfRace(race) && p.isOfLevel(ClassLevel.Fourth)) {
            this._antifeedTemplate = CharTemplateParser.getInstance().getTemplate(c);
            break;
         }
      }

      if (this.getRace() == Race.Kamael) {
         this._antifeedSex = this.getAppearance().getSex();
      }

      this._antifeedSex = Rnd.get(2) == 0;
      return this._antifeedTemplate;
   }

   public void startAntifeedProtection(boolean start, boolean broadcast) {
      if (!start) {
         this.getAppearance().setVisibleName(this.getName());
         this._antifeedTemplate = null;
      } else {
         this.getAppearance().setVisibleName("Unknown");
         this.createRandomAntifeedTemplate();
      }
   }

   public PcTemplate getAntifeedTemplate() {
      return this._antifeedTemplate;
   }

   public boolean getAntifeedSex() {
      return this._antifeedSex;
   }

   public void registerShortCut(ShortCutTemplate shortcut) {
      this._shortCuts.registerShortCut(shortcut);
   }

   public void updateShortCuts(int skillId, int skillLevel) {
      this._shortCuts.updateShortCuts(skillId, skillLevel);
   }

   public void updateShortCuts(int objId, ShortcutType type) {
      this._shortCuts.updateShortCuts(objId, type);
   }

   public void registerShortCut(ShortCutTemplate shortcut, boolean storeToDb) {
      this._shortCuts.registerShortCut(shortcut, storeToDb);
   }

   public void deleteShortCut(int slot, int page) {
      this._shortCuts.deleteShortCut(slot, page);
   }

   public void deleteShortCut(int slot, int page, boolean fromDb) {
      this._shortCuts.deleteShortCut(slot, page, fromDb);
   }

   public void removeAllShortcuts() {
      this._shortCuts.tempRemoveAll();
   }

   public void registerMacro(Macro macro) {
      this._macros.registerMacro(macro);
   }

   public void deleteMacro(int id) {
      this._macros.deleteMacro(id);
   }

   public MacroList getMacros() {
      return this._macros;
   }

   public void setSiegeState(byte siegeState) {
      this._siegeState = siegeState;
   }

   public byte getSiegeState() {
      return this._siegeState;
   }

   public void setSiegeSide(int val) {
      this._siegeSide = val;
   }

   public boolean isRegisteredOnThisSiegeField(int val) {
      return this._siegeSide == val || this._siegeSide >= 81 && this._siegeSide <= 89;
   }

   public int getSiegeSide() {
      return this._siegeSide;
   }

   public void setPvpFlag(int pvpFlag) {
      this._pvpFlag = (byte)pvpFlag;
   }

   @Override
   public byte getPvpFlag() {
      return this._pvpFlag;
   }

   @Override
   public void updatePvPFlag(int value) {
      if (this.getPvpFlag() != value) {
         this.setPvpFlag(value);
         this.sendUserInfo(true);
         this.broadcastRelationChanged();
      }
   }

   @Override
   public void revalidateZone(boolean force) {
      super.revalidateZone(force);
      if (Config.ALLOW_WATER) {
         this.checkWaterState();
      }

      if (this.isInsideZone(ZoneId.ALTERED)) {
         if (this._lastCompassZone != 8) {
            this._lastCompassZone = 8;
            this.sendPacket(new ExSetCompassZoneCode(8));
         }
      } else if (this.isInsideZone(ZoneId.SIEGE)) {
         if (this._lastCompassZone != 11) {
            this._lastCompassZone = 11;
            this.sendPacket(new ExSetCompassZoneCode(11));
         }
      } else if (this.isInsideZone(ZoneId.PVP)) {
         if (this._lastCompassZone != 14) {
            this._lastCompassZone = 14;
            this.sendPacket(new ExSetCompassZoneCode(14));
         }
      } else if (this.isIn7sDungeon()) {
         if (this._lastCompassZone != 13) {
            this._lastCompassZone = 13;
            this.sendPacket(new ExSetCompassZoneCode(13));
         }
      } else if (this.isInZonePeace()) {
         if (this._lastCompassZone != 12) {
            this._lastCompassZone = 12;
            this.sendPacket(new ExSetCompassZoneCode(12));
         }
      } else {
         if (this._lastCompassZone == 15) {
            return;
         }

         if (this._lastCompassZone == 11) {
            this.updatePvPStatus();
         }

         this._lastCompassZone = 15;
         this.sendPacket(new ExSetCompassZoneCode(15));
      }
   }

   public boolean hasDwarvenCraft() {
      return this.getSkillLevel(172) >= 1;
   }

   public int getDwarvenCraft() {
      return this.getSkillLevel(172);
   }

   public boolean hasCommonCraft() {
      return this.getSkillLevel(1320) >= 1;
   }

   public int getCommonCraft() {
      return this.getSkillLevel(1320);
   }

   public int getPkKills() {
      return this._pkKills;
   }

   public void setPkKills(int pkKills) {
      if (this.getEvents().onPKChange(this._pkKills, pkKills)) {
         this._pkKills = pkKills;
      }
   }

   public long getDeleteTimer() {
      return this._deleteTimer;
   }

   public void setDeleteTimer(long deleteTimer) {
      this._deleteTimer = deleteTimer;
   }

   public void setExpBeforeDeath(long exp) {
      this._expBeforeDeath = exp;
   }

   public long getExpBeforeDeath() {
      return this._expBeforeDeath;
   }

   @Override
   public int getKarma() {
      return this._karma;
   }

   public void setKarma(int karma) {
      if (this.getEvents().onKarmaChange(this._karma, karma)) {
         if (karma < 0) {
            karma = 0;
         }

         if (this._karma == 0 && karma > 0) {
            for(GameObject object : World.getInstance().getAroundNpc(this)) {
               if (object instanceof GuardInstance && ((GuardInstance)object).getAI().getIntention() == CtrlIntention.IDLE) {
                  ((GuardInstance)object).getAI().setIntention(CtrlIntention.ACTIVE, null);
               }
            }
         } else if (this._karma > 0 && karma == 0) {
            this.setKarmaFlag(0);
         }

         this._karma = karma;
         this.broadcastKarma();
      }
   }

   public int getExpertiseArmorPenalty() {
      return this._expertiseArmorPenalty;
   }

   public int getExpertiseWeaponPenalty() {
      return this._expertiseWeaponPenalty;
   }

   public int getWeightPenalty() {
      return this._dietMode ? 0 : this._curWeightPenalty;
   }

   public void refreshOverloaded() {
      int maxLoad = this.getMaxLoad();
      if (maxLoad > 0) {
         long weightproc = (long)((this.getCurrentLoad() - this.getBonusWeightPenalty()) * 1000 / this.getMaxLoad());
         int newWeightPenalty;
         if (weightproc < 500L || this._dietMode) {
            newWeightPenalty = 0;
         } else if (weightproc < 666L) {
            newWeightPenalty = 1;
         } else if (weightproc < 800L) {
            newWeightPenalty = 2;
         } else if (weightproc < 1000L) {
            newWeightPenalty = 3;
         } else {
            newWeightPenalty = 4;
         }

         if (this._curWeightPenalty != newWeightPenalty) {
            this._curWeightPenalty = newWeightPenalty;
            if (newWeightPenalty > 0 && !this._dietMode) {
               this.addSkill(SkillsParser.getInstance().getInfo(4270, newWeightPenalty));
               this.setIsOverloaded(this.getCurrentLoad() > maxLoad);
            } else {
               this.removeSkill(this.getKnownSkill(4270), false, true);
               this.setIsOverloaded(false);
            }

            this.sendPacket(new EtcStatusUpdate(this));
            this.broadcastUserInfo(true);
         }
      }
   }

   public void refreshExpertisePenalty() {
      if (Config.EXPERTISE_PENALTY) {
         int level = (int)this.calcStat(Stats.GRADE_EXPERTISE_LEVEL, (double)this.getLevel(), null, null);
         int i = 0;
         i = 0;

         while(i < EXPERTISE_LEVELS.length && level >= EXPERTISE_LEVELS[i + 1]) {
            ++i;
         }

         if (this.expertiseIndex != i) {
            this.expertiseIndex = i;
            if (this.expertiseIndex > 0) {
               this.addSkill(SkillsParser.getInstance().getInfo(239, this.expertiseIndex), false);
            }
         }

         int armorPenalty = 0;
         int weaponPenalty = 0;

         for(ItemInstance item : this.getInventory().getItems()) {
            if (item != null && item.isEquipped() && item.getItemType() != EtcItemType.ARROW && item.getItemType() != EtcItemType.BOLT) {
               int crystaltype = item.getItem().getCrystalType();
               if (item.getItem().getType2() == 0) {
                  if (crystaltype > weaponPenalty) {
                     weaponPenalty = crystaltype;
                  }
               } else if ((item.getItem().getType2() == 1 || item.getItem().getType2() == 2) && crystaltype > armorPenalty) {
                  armorPenalty = crystaltype;
               }
            }
         }

         boolean changed = false;
         armorPenalty -= this.expertiseIndex;
         armorPenalty = Math.min(Math.max(armorPenalty, 0), 4);
         if (this.getExpertiseArmorPenalty() != armorPenalty || this.getSkillLevel(6213) != armorPenalty) {
            this._expertiseArmorPenalty = armorPenalty;
            if (this._expertiseArmorPenalty > 0) {
               this.addSkill(SkillsParser.getInstance().getInfo(6213, this._expertiseArmorPenalty));
            } else {
               this.removeSkill(this.getKnownSkill(6213), false, true);
            }

            changed = true;
         }

         weaponPenalty -= this.expertiseIndex;
         weaponPenalty = Math.min(Math.max(weaponPenalty, 0), 4);
         if (this.getExpertiseWeaponPenalty() != weaponPenalty || this.getSkillLevel(6209) != weaponPenalty) {
            this._expertiseWeaponPenalty = weaponPenalty;
            if (this._expertiseWeaponPenalty > 0) {
               this.addSkill(SkillsParser.getInstance().getInfo(6209, this._expertiseWeaponPenalty));
            } else {
               this.removeSkill(this.getKnownSkill(6209), false, true);
            }

            changed = true;
         }

         if (changed) {
            this.sendPacket(new EtcStatusUpdate(this));
         }
      }
   }

   public void useEquippableItem(int objectId, boolean abortAttack) {
      ItemInstance item = this.getInventory().getItemByObjectId(objectId);
      if (item != null) {
         ItemInstance[] items = null;
         boolean isEquiped = item.isEquipped();
         int oldInvLimit = this.getInventoryLimit();
         SystemMessage sm = null;
         if (this.fireEquipmentListeners(isEquiped, item)) {
            if (isEquiped) {
               if (item.getEnchantLevel() > 0) {
                  sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
                  sm.addNumber(item.getEnchantLevel());
                  sm.addItemName(item);
               } else {
                  sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED);
                  sm.addItemName(item);
               }

               this.sendPacket(sm);
               int slot = this.getInventory().getSlotFromItem(item);
               if (slot == 4194304) {
                  items = this.getInventory().unEquipItemInSlotAndRecord(item.getLocationSlot());
               } else {
                  items = this.getInventory().unEquipItemInBodySlotAndRecord(slot);
               }
            } else {
               items = this.getInventory().equipItemAndRecord(item);
               if (item.isEquipped()) {
                  if (item.getEnchantLevel() > 0) {
                     sm = SystemMessage.getSystemMessage(SystemMessageId.S1_S2_EQUIPPED);
                     sm.addNumber(item.getEnchantLevel());
                     sm.addItemName(item);
                  } else {
                     sm = SystemMessage.getSystemMessage(SystemMessageId.S1_EQUIPPED);
                     sm.addItemName(item);
                  }

                  this.sendPacket(sm);
                  item.decreaseMana(false);
                  item.decreaseEnergy(false);
                  if ((item.getItem().getBodyPart() & 16512) != 0) {
                     this.rechargeShots(true, true);
                  }
               } else {
                  this.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
               }
            }

            this.refreshExpertisePenalty();
            InventoryUpdate iu = new InventoryUpdate();
            iu.addItems(Arrays.asList(items));
            this.sendPacket(iu);
            this.sendItemList(false);
            this.broadcastUserInfo(true);
            if (abortAttack) {
               this.abortAttack();
            }

            if (this.getInventoryLimit() != oldInvLimit) {
               this.sendPacket(new ExStorageMaxCount(this));
            }
         }
      }
   }

   public int getPvpKills() {
      return this._pvpKills;
   }

   public void setPvpKills(int pvpKills) {
      if (this.getEvents().onPvPChange(this._pvpKills, pvpKills)) {
         this._pvpKills = pvpKills;
      }
   }

   public int getFame() {
      return this._fame;
   }

   public void setFame(int fame) {
      if (this.getEvents().onFameChange(this._fame, fame)) {
         int nextFame = fame > Config.MAX_PERSONAL_FAME_POINTS ? Config.MAX_PERSONAL_FAME_POINTS : fame;
         boolean addAch = nextFame > this._fame;
         this._fame = nextFame;
         if (addAch) {
            this.getCounters().addAchivementInfo("fameAcquired", 0, (long)this._fame, true, false, false);
         }
      }
   }

   public ClassId getClassId() {
      return this.getTemplate().getClassId();
   }

   public void setClassId(int Id) {
      if (this._subclassLock.tryLock()) {
         try {
            if (this.getLvlJoinedAcademy() != 0 && this._clan != null && PlayerClass.values()[Id].getLevel() == ClassLevel.Third) {
               if (this.getLvlJoinedAcademy() <= 16) {
                  this._clan.addReputationScore(Config.JOIN_ACADEMY_MAX_REP_SCORE, true);
               } else if (this.getLvlJoinedAcademy() >= 39) {
                  this._clan.addReputationScore(Config.JOIN_ACADEMY_MIN_REP_SCORE, true);
               } else {
                  this._clan.addReputationScore(Config.JOIN_ACADEMY_MAX_REP_SCORE - (this.getLvlJoinedAcademy() - 16) * 20, true);
               }

               this.setLvlJoinedAcademy(0);
               SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_EXPELLED);
               msg.addPcName(this);
               this._clan.broadcastToOnlineMembers(msg);
               this._clan.broadcastToOnlineMembers(new PledgeShowMemberListDelete(this.getName()));
               this._clan.removeClanMember(this.getObjectId(), 0L);
               this.sendPacket(PledgeShowMemberListDeleteAll.STATIC_PACKET);
               this.sendPacket(SystemMessageId.ACADEMY_MEMBERSHIP_TERMINATED);
               this.getInventory().addItem("Gift", 8181, 1L, this, null);
               AcademyList.removeAcademyFromDB(this._clan, this.getObjectId(), true, false);
            }

            if (this.isSubClassActive()) {
               this.getSubClasses().get(this._classIndex).setClassId(Id);
            }

            this.setTarget(this);
            this.broadcastPacket(new MagicSkillUse(this, 5103, 1, 1000, 0));
            this.setClassTemplate(Id);
            if (this.getClassId().level() == 3) {
               this.sendPacket(SystemMessageId.THIRD_CLASS_TRANSFER);
            } else {
               this.sendPacket(SystemMessageId.CLASS_TRANSFER);
            }

            if (this.isInParty()) {
               this.getParty().broadCast(new PartySmallWindowUpdate(this));
            }

            if (this.getClan() != null) {
               this.getClan().broadcastToOnlineMembers(new PledgeShowMemberListUpdate(this));
            }

            if (this._matchingRoom != null) {
               this._matchingRoom.broadcastPlayerUpdate(this);
            }

            this.rewardSkills();
            if (!this.canOverrideCond(PcCondOverride.SKILL_CONDITIONS) && Config.DECREASE_SKILL_LEVEL) {
               this.checkPlayerSkills();
            }
         } finally {
            this._subclassLock.unlock();
         }
      }
   }

   public ClassId getLearningClass() {
      return this._learningClass;
   }

   public void setLearningClass(ClassId learningClass) {
      this._learningClass = learningClass;
   }

   public long getExp() {
      return this.getStat().getExp();
   }

   public void setActiveEnchantAttrItemId(int objectId) {
      this._activeEnchantAttrItemId = objectId;
   }

   public int getActiveEnchantAttrItemId() {
      return this._activeEnchantAttrItemId;
   }

   public void setActiveEnchantItemId(int objectId) {
      if (objectId == -1) {
         this.setActiveEnchantSupportItemId(-1);
         this.setIsEnchanting(false);
      }

      this._activeEnchantItemId = objectId;
   }

   public int getActiveEnchantItemId() {
      return this._activeEnchantItemId;
   }

   public void setActiveEnchantSupportItemId(int objectId) {
      this._activeEnchantSupportItemId = objectId;
   }

   public int getActiveEnchantSupportItemId() {
      return this._activeEnchantSupportItemId;
   }

   public void setIsEnchanting(boolean val) {
      this._isEnchanting = val;
   }

   public boolean isEnchanting() {
      return this._isEnchanting;
   }

   public void setFistsWeaponItem(Weapon weaponItem) {
      this._fistsWeaponItem = weaponItem;
   }

   public Weapon getFistsWeaponItem() {
      return this._fistsWeaponItem;
   }

   public Weapon findFistsWeaponItem(int classId) {
      Weapon weaponItem = null;
      if (classId >= 0 && classId <= 9) {
         Item temp = ItemsParser.getInstance().getTemplate(246);
         weaponItem = (Weapon)temp;
      } else if (classId >= 10 && classId <= 17) {
         Item temp = ItemsParser.getInstance().getTemplate(251);
         weaponItem = (Weapon)temp;
      } else if (classId >= 18 && classId <= 24) {
         Item temp = ItemsParser.getInstance().getTemplate(244);
         weaponItem = (Weapon)temp;
      } else if (classId >= 25 && classId <= 30) {
         Item temp = ItemsParser.getInstance().getTemplate(249);
         weaponItem = (Weapon)temp;
      } else if (classId >= 31 && classId <= 37) {
         Item temp = ItemsParser.getInstance().getTemplate(245);
         weaponItem = (Weapon)temp;
      } else if (classId >= 38 && classId <= 43) {
         Item temp = ItemsParser.getInstance().getTemplate(250);
         weaponItem = (Weapon)temp;
      } else if (classId >= 44 && classId <= 48) {
         Item temp = ItemsParser.getInstance().getTemplate(248);
         weaponItem = (Weapon)temp;
      } else if (classId >= 49 && classId <= 52) {
         Item temp = ItemsParser.getInstance().getTemplate(252);
         weaponItem = (Weapon)temp;
      } else if (classId >= 53 && classId <= 57) {
         Item temp = ItemsParser.getInstance().getTemplate(247);
         weaponItem = (Weapon)temp;
      }

      return weaponItem;
   }

   public void rewardSkills() {
      if ((!Config.AUTO_LEARN_SKILLS || this.getLevel() > Config.AUTO_LEARN_SKILLS_MAX_LEVEL) && !this.isFakePlayer()) {
         this.giveAvailableAutoGetSkills();
      } else {
         this.giveAvailableSkills(Config.AUTO_LEARN_FS_SKILLS, true);
      }

      if (Config.UNSTUCK_SKILL && this.getSkillLevel(1050) < 0) {
         this.addSkill(SkillsParser.getInstance().getInfo(2099, 1), false);
      }

      if (!this.canOverrideCond(PcCondOverride.SKILL_CONDITIONS) && Config.DECREASE_SKILL_LEVEL) {
         this.checkPlayerSkills();
      }

      this.checkItemRestriction();
      this.sendSkillList(false);
   }

   public void regiveTemporarySkills() {
      if (this.isNoble()) {
         this.setNoble(true);
      }

      if (this.isHero()) {
         this.setHero(true, true);
      }

      if (this.getClan() != null) {
         Clan clan = this.getClan();
         clan.addSkillEffects(this);
         if (clan.getLevel() >= SiegeManager.getInstance().getSiegeClanMinLevel() && this.isClanLeader()) {
            SiegeManager.getInstance().addSiegeSkills(this);
         }

         if (this.getClan().getCastleId() > 0) {
            CastleManager.getInstance().getCastleByOwner(this.getClan()).giveResidentialSkills(this);
         }

         if (this.getClan().getFortId() > 0) {
            FortManager.getInstance().getFortByOwner(this.getClan()).giveResidentialSkills(this);
         }
      }

      this.getInventory().reloadEquippedItems();
      this.restoreDeathPenaltyBuffLevel();
   }

   public int giveAvailableSkills(boolean includedByFs, boolean includeAutoGet) {
      int skillCounter = 0;
      Collection<Skill> skills = SkillTreesParser.getInstance().getAllAvailableSkills(this, this.getClassId(), includedByFs, includeAutoGet);
      List<Skill> skillsForStore = new ArrayList<>();

      for(Skill sk : skills) {
         if (Config.DECREASE_SKILL_LEVEL) {
            if (this.getKnownSkill(sk.getId()) == sk) {
               continue;
            }
         } else {
            Skill skill = this.getKnownSkill(sk.getId());
            if (skill != null && skill.getLevel() > sk.getLevel()) {
               continue;
            }
         }

         if (this.getSkillLevel(sk.getId()) == -1) {
            ++skillCounter;
         }

         if (sk.isToggle()) {
            Effect toggleEffect = this.getFirstEffect(sk.getId());
            if (toggleEffect != null) {
               toggleEffect.exit();
               sk.getEffects(this, this, true);
            }
         }

         this.addSkill(sk, false);
         skillsForStore.add(sk);
      }

      CharacterSkillsDAO.getInstance().storeSkills(this, skillsForStore, -1);
      if (Config.AUTO_LEARN_SKILLS && skillCounter > 0) {
         this.sendMessage("You have learned " + skillCounter + " new skills");
      }

      return skillCounter;
   }

   public void giveAvailableAutoGetSkills() {
      List<SkillLearn> autoGetSkills = SkillTreesParser.getInstance().getAvailableAutoGetSkills(this);
      SkillsParser st = SkillsParser.getInstance();

      for(SkillLearn s : autoGetSkills) {
         Skill skill = st.getInfo(s.getId(), s.getLvl());
         if (skill != null) {
            this.addSkill(skill, true);
         } else {
            _log.warning("Skipping null auto-get skill for player: " + this.toString());
         }
      }
   }

   public void setExp(long exp) {
      if (exp < 0L) {
         exp = 0L;
      }

      this.getStat().setExp(exp);
   }

   public Race getRace() {
      return !this.isSubClassActive() ? this.getTemplate().getRace() : CharTemplateParser.getInstance().getTemplate(this._baseClass).getRace();
   }

   public Radar getRadar() {
      return this._radar;
   }

   public boolean isMinimapAllowed() {
      return this._minimapAllowed;
   }

   public void setMinimapAllowed(boolean b) {
      this._minimapAllowed = b;
   }

   public int getSp() {
      return this.getStat().getSp();
   }

   public void setSp(int sp) {
      if (sp < 0) {
         sp = 0;
      }

      super.getStat().setSp(sp);
   }

   public boolean isCastleLord(int castleId) {
      Clan clan = this.getClan();
      if (clan != null && clan.getLeader().getPlayerInstance() == this) {
         Castle castle = CastleManager.getInstance().getCastleByOwner(clan);
         if (castle != null && castle == CastleManager.getInstance().getCastleById(castleId)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public int getClanId() {
      return this._clanId;
   }

   public int getClanCrestId() {
      return this._clan != null ? this._clan.getCrestId() : 0;
   }

   public int getClanCrestLargeId() {
      return this._clan != null ? this._clan.getCrestLargeId() : 0;
   }

   public long getClanJoinExpiryTime() {
      return this._clanJoinExpiryTime;
   }

   public void setClanJoinExpiryTime(long time) {
      this._clanJoinExpiryTime = time;
   }

   public long getClanCreateExpiryTime() {
      return this._clanCreateExpiryTime;
   }

   public void setClanCreateExpiryTime(long time) {
      this._clanCreateExpiryTime = time;
   }

   public void setOnlineTime(long time) {
      this._onlineTime = time;
      this._onlineBeginTime = System.currentTimeMillis();
   }

   public PcInventory getInventory() {
      return this._inventory;
   }

   public void removeItemFromShortCut(int objectId) {
      this._shortCuts.deleteShortCutByObjectId(objectId);
   }

   public boolean isSitting() {
      return this._waitTypeSitting;
   }

   public void setIsSitting(boolean state) {
      this._waitTypeSitting = state;
   }

   public void sitDown() {
      this.sitDown(true);
   }

   public void sitDownNow() {
      this.breakAttack();
      this.setIsSitting(true);
      this.getAI().setIntention(CtrlIntention.REST);
      this.broadcastPacket(new ChangeWaitType(this, 0));
      ThreadPoolManager.getInstance().schedule(new SitDownTask(this), 2500L);
      this.setIsParalyzed(true);
      this.setIsInvul(true);
   }

   public void sitDown(boolean checkCast) {
      if (checkCast && this.isCastingNow()) {
         this.sendMessage("Cannot sit while casting");
      } else {
         if (!this._waitTypeSitting && !this.isAttackingDisabled() && !this.isOutOfControl() && !this.isImmobilized()) {
            this.breakAttack();
            this.setIsSitting(true);
            this.getAI().setIntention(CtrlIntention.REST);
            this.broadcastPacket(new ChangeWaitType(this, 0));
            ThreadPoolManager.getInstance().schedule(new SitDownTask(this), 2500L);
            this.setIsParalyzed(true);
         }
      }
   }

   public void standUp() {
      if (!this.isInFightEvent() || this.getFightEvent().canStandUp(this)) {
         this._sittingObject = null;
         if (this._waitTypeSitting && !this.isInStoreMode() && !this.isAlikeDead()) {
            if (this._effects.isAffected(EffectFlag.RELAXING)) {
               this.stopEffects(EffectType.RELAXING);
            }

            this.broadcastPacket(new ChangeWaitType(this, 1));
            ThreadPoolManager.getInstance().schedule(new StandUpTask(this), 2500L);
         }
      }
   }

   public PcWarehouse getWarehouse() {
      if (this._warehouse == null) {
         this._warehouse = new PcWarehouse(this);
         this._warehouse.restore();
      }

      if (Config.WAREHOUSE_CACHE) {
         WarehouseCache.getInstance().addCacheTask(this);
      }

      return this._warehouse;
   }

   public void clearWarehouse() {
      if (this._warehouse != null) {
         this._warehouse.deleteMe();
      }

      this._warehouse = null;
   }

   public PcFreight getFreight() {
      return this._freight;
   }

   public boolean hasRefund() {
      return this._refund != null && this._refund.getSize() > 0 && Config.ALLOW_REFUND;
   }

   public PcRefund getRefund() {
      if (this._refund == null) {
         this._refund = new PcRefund(this);
      }

      return this._refund;
   }

   public void clearRefund() {
      if (this._refund != null) {
         this._refund.deleteMe();
      }

      this._refund = null;
   }

   public long getAdena() {
      return this._inventory.getAdena();
   }

   public long getAncientAdena() {
      return this._inventory.getAncientAdena();
   }

   public void addAdena(String process, long count, GameObject reference, boolean sendMessage) {
      if (sendMessage) {
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1_ADENA);
         sm.addItemNumber(count);
         this.sendPacket(sm);
      }

      if (count > 0L) {
         this._inventory.addAdena(process, count, this, reference);
         this.getCounters().addAchivementInfo("adenaAcquired", 0, -1L, false, false, false);
         if (!Config.FORCE_INVENTORY_UPDATE) {
            InventoryUpdate iu = new InventoryUpdate();
            iu.addItem(this._inventory.getAdenaInstance());
            this.sendPacket(iu);
         } else {
            this.sendItemList(false);
         }
      }
   }

   public boolean reduceAdena(String process, long count, GameObject reference, boolean sendMessage) {
      if (count > this.getAdena()) {
         if (sendMessage) {
            this.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
         }

         return false;
      } else {
         if (count > 0L) {
            ItemInstance adenaItem = this._inventory.getAdenaInstance();
            if (!this._inventory.reduceAdena(process, count, this, reference)) {
               return false;
            }

            this.getCounters().addAchivementInfo("adenaReduced", 0, -1L, false, false, false);
            if (!Config.FORCE_INVENTORY_UPDATE) {
               InventoryUpdate iu = new InventoryUpdate();
               iu.addItem(adenaItem);
               this.sendPacket(iu);
            } else {
               this.sendItemList(false);
            }

            if (sendMessage) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED_ADENA);
               sm.addItemNumber(count);
               this.sendPacket(sm);
            }
         }

         return true;
      }
   }

   public void addAncientAdena(String process, long count, GameObject reference, boolean sendMessage) {
      if (sendMessage) {
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
         sm.addItemName(5575);
         sm.addItemNumber(count);
         this.sendPacket(sm);
      }

      if (count > 0L) {
         this._inventory.addAncientAdena(process, count, this, reference);
         if (!Config.FORCE_INVENTORY_UPDATE) {
            InventoryUpdate iu = new InventoryUpdate();
            iu.addItem(this._inventory.getAncientAdenaInstance());
            this.sendPacket(iu);
         } else {
            this.sendItemList(false);
         }
      }
   }

   public boolean reduceAncientAdena(String process, long count, GameObject reference, boolean sendMessage) {
      if (count > this.getAncientAdena()) {
         if (sendMessage) {
            this.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
         }

         return false;
      } else {
         if (count > 0L) {
            ItemInstance ancientAdenaItem = this._inventory.getAncientAdenaInstance();
            if (!this._inventory.reduceAncientAdena(process, count, this, reference)) {
               return false;
            }

            if (!Config.FORCE_INVENTORY_UPDATE) {
               InventoryUpdate iu = new InventoryUpdate();
               iu.addItem(ancientAdenaItem);
               this.sendPacket(iu);
            } else {
               this.sendItemList(false);
            }

            if (sendMessage) {
               if (count > 1L) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
                  sm.addItemName(5575);
                  sm.addItemNumber(count);
                  this.sendPacket(sm);
               } else {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
                  sm.addItemName(5575);
                  this.sendPacket(sm);
               }
            }
         }

         return true;
      }
   }

   public void addItem(String process, ItemInstance item, GameObject reference, boolean sendMessage) {
      if (item.getCount() > 0L) {
         if (sendMessage) {
            if (item.getCount() > 1L) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2);
               sm.addItemName(item);
               sm.addItemNumber(item.getCount());
               this.sendPacket(sm);
            } else if (item.getEnchantLevel() > 0) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_A_S1_S2);
               sm.addNumber(item.getEnchantLevel());
               sm.addItemName(item);
               this.sendPacket(sm);
            } else {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1);
               sm.addItemName(item);
               this.sendPacket(sm);
            }
         }

         ItemInstance newitem = this._inventory.addItem(process, item, this, reference);
         if (!Config.FORCE_INVENTORY_UPDATE) {
            InventoryUpdate playerIU = new InventoryUpdate();
            playerIU.addItem(newitem);
            this.sendPacket(playerIU);
         } else {
            this.sendItemList(false);
         }

         StatusUpdate su = new StatusUpdate(this);
         su.addAttribute(14, this.getCurrentLoad());
         this.sendPacket(su);
         if (!this.canOverrideCond(PcCondOverride.ITEM_CONDITIONS) && !this._inventory.validateCapacity(item)) {
            this.dropItem("InvDrop", newitem, null, true, true);
         } else if (CursedWeaponsManager.getInstance().isCursed(newitem.getId())) {
            CursedWeaponsManager.getInstance().activate(this, newitem);
         } else if (FortSiegeManager.getInstance().isCombat(item.getId()) && !this.isInFightEvent()) {
            if (FortSiegeManager.getInstance().activateCombatFlag(this, item)) {
               Fort fort = FortManager.getInstance().getFort(this);
               fort.getSiege().announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.C1_ACQUIRED_THE_FLAG), this.getName());
            }
         } else if (item.getId() >= 13560 && item.getId() <= 13568) {
            TerritoryWard ward = TerritoryWarManager.getInstance().getTerritoryWard(item.getId() - 13479);
            if (ward != null) {
               ward.activate(this, item);
            }
         }

         if (newitem.isEtcItem()) {
            this.checkToEquipArrows(newitem);
         }
      }
   }

   public ItemInstance addItem(String process, int itemId, long count, GameObject reference, boolean sendMessage) {
      if (count > 0L) {
         ItemInstance item = null;
         if (ItemsParser.getInstance().getTemplate(itemId) == null) {
            _log.log(Level.SEVERE, "Item doesn't exist so cannot be added. Item ID: " + itemId);
            return null;
         }

         item = ItemsParser.getInstance().createDummyItem(itemId);
         if (sendMessage) {
            if (count > 1L) {
               if (!process.equalsIgnoreCase("Sweeper") && !process.equalsIgnoreCase("Quest")) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2);
                  sm.addItemName(itemId);
                  sm.addItemNumber(count);
                  this.sendPacket(sm);
               } else {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
                  sm.addItemName(itemId);
                  sm.addItemNumber(count);
                  this.sendPacket(sm);
               }
            } else if (!process.equalsIgnoreCase("Sweeper") && !process.equalsIgnoreCase("Quest")) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1);
               sm.addItemName(itemId);
               this.sendPacket(sm);
            } else {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
               sm.addItemName(itemId);
               this.sendPacket(sm);
            }
         }

         if (!item.getItem().isHerb()) {
            ItemInstance createdItem = this._inventory.addItem(process, itemId, count, this, reference);
            if (!this.canOverrideCond(PcCondOverride.ITEM_CONDITIONS) && !this._inventory.validateCapacity(item)) {
               this.dropItem("InvDrop", createdItem, null, true);
            } else if (CursedWeaponsManager.getInstance().isCursed(createdItem.getId())) {
               CursedWeaponsManager.getInstance().activate(this, createdItem);
            } else if (FortSiegeManager.getInstance().isCombat(createdItem.getId()) && !this.isInFightEvent()) {
               if (FortSiegeManager.getInstance().activateCombatFlag(this, item)) {
                  Fort fort = FortManager.getInstance().getFort(this);
                  fort.getSiege().announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.C1_ACQUIRED_THE_FLAG), this.getName());
               }
            } else if (createdItem.getId() >= 13560 && createdItem.getId() <= 13568) {
               TerritoryWard ward = TerritoryWarManager.getInstance().getTerritoryWard(createdItem.getId() - 13479);
               if (ward != null) {
                  ward.activate(this, createdItem);
               }
            }

            if (createdItem.isEtcItem()) {
               this.checkToEquipArrows(createdItem);
            }

            return createdItem;
         }

         IItemHandler handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
         if (handler == null) {
            _log.warning("No item handler registered for Herb ID " + item.getId() + "!");
         } else {
            handler.useItem(this, new ItemInstance(itemId), false);
            if (this.getSummon() != null && this.getSummon().isServitor() && !this.getSummon().isDead()) {
               handler.useItem(this.getSummon(), new ItemInstance(itemId), false);
            }
         }
      }

      return null;
   }

   public void addItem(String process, ItemHolder item, GameObject reference, boolean sendMessage) {
      this.addItem(process, item.getId(), item.getCount(), reference, sendMessage);
   }

   public boolean destroyItem(String process, ItemInstance item, GameObject reference, boolean sendMessage) {
      return this.destroyItem(process, item, item.getCount(), reference, sendMessage);
   }

   public boolean destroyItem(String process, ItemInstance item, long count, GameObject reference, boolean sendMessage) {
      item = this._inventory.destroyItem(process, item, count, this, reference);
      if (item == null) {
         if (sendMessage) {
            this.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
         }

         return false;
      } else {
         if (!Config.FORCE_INVENTORY_UPDATE) {
            InventoryUpdate playerIU = new InventoryUpdate();
            playerIU.addItem(item);
            this.sendPacket(playerIU);
         } else {
            this.sendItemList(false);
         }

         StatusUpdate su = new StatusUpdate(this);
         su.addAttribute(14, this.getCurrentLoad());
         this.sendPacket(su);
         if (sendMessage) {
            if (count > 1L) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
               sm.addItemName(item);
               sm.addItemNumber(count);
               this.sendPacket(sm);
            } else {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
               sm.addItemName(item);
               this.sendPacket(sm);
            }
         }

         return true;
      }
   }

   @Override
   public boolean destroyItem(String process, int objectId, long count, GameObject reference, boolean sendMessage) {
      ItemInstance item = this._inventory.getItemByObjectId(objectId);
      if (item == null) {
         if (sendMessage) {
            this.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
         }

         return false;
      } else {
         return this.destroyItem(process, item, count, reference, sendMessage);
      }
   }

   public boolean destroyItemWithoutTrace(String process, int objectId, long count, GameObject reference, boolean sendMessage) {
      ItemInstance item = this._inventory.getItemByObjectId(objectId);
      if (item != null && item.getCount() >= count) {
         return this.destroyItem(null, item, count, reference, sendMessage);
      } else {
         if (sendMessage) {
            this.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
         }

         return false;
      }
   }

   public boolean destroyItemWithoutEquip(String process, int itemId, long count, GameObject reference, boolean sendMessage) {
      ItemInstance item = this._inventory.getItemByItemId(itemId);
      if (item == null) {
         this.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
         return false;
      } else {
         if (item.isStackable()) {
            if (!this.destroyItemByItemId("Craft", itemId, count, reference, sendMessage)) {
               this.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
               return false;
            }
         } else {
            ItemInstance[] inventoryContents = this._inventory.getAllItemsByItemId(itemId, false);

            for(int i = 0; (long)i < count; ++i) {
               if (!this.destroyItem(process, inventoryContents[i].getObjectId(), 1L, reference, sendMessage)) {
                  this.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
                  return false;
               }
            }
         }

         return true;
      }
   }

   @Override
   public boolean destroyItemByItemId(String process, int itemId, long count, GameObject reference, boolean sendMessage) {
      if (itemId == 57) {
         return this.reduceAdena(process, count, reference, sendMessage);
      } else {
         ItemInstance item = this._inventory.getItemByItemId(itemId);
         if (item != null && item.getCount() >= count && this._inventory.destroyItemByItemId(process, itemId, count, this, reference) != null) {
            if (!Config.FORCE_INVENTORY_UPDATE) {
               InventoryUpdate playerIU = new InventoryUpdate();
               playerIU.addItem(item);
               this.sendPacket(playerIU);
            } else {
               this.sendItemList(false);
            }

            StatusUpdate su = new StatusUpdate(this);
            su.addAttribute(14, this.getCurrentLoad());
            this.sendPacket(su);
            if (sendMessage) {
               if (count > 1L) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
                  sm.addItemName(itemId);
                  sm.addItemNumber(count);
                  this.sendPacket(sm);
               } else {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
                  sm.addItemName(itemId);
                  this.sendPacket(sm);
               }
            }

            return true;
         } else {
            if (sendMessage) {
               this.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
            }

            return false;
         }
      }
   }

   public ItemInstance transferItem(String process, int objectId, long count, Inventory target, GameObject reference) {
      ItemInstance oldItem = this.checkItemManipulation(objectId, count, "transfer");
      if (oldItem == null) {
         return null;
      } else {
         ItemInstance newItem = this.getInventory().transferItem(process, objectId, count, target, this, reference);
         if (newItem == null) {
            return null;
         } else {
            if (!Config.FORCE_INVENTORY_UPDATE) {
               InventoryUpdate playerIU = new InventoryUpdate();
               if (oldItem.getCount() > 0L && oldItem != newItem) {
                  playerIU.addModifiedItem(oldItem);
               } else {
                  playerIU.addRemovedItem(oldItem);
               }

               this.sendPacket(playerIU);
            } else {
               this.sendItemList(false);
            }

            StatusUpdate playerSU = new StatusUpdate(this);
            playerSU.addAttribute(14, this.getCurrentLoad());
            this.sendPacket(playerSU);
            if (target instanceof PcInventory) {
               Player targetPlayer = ((PcInventory)target).getOwner();
               if (!Config.FORCE_INVENTORY_UPDATE) {
                  InventoryUpdate playerIU = new InventoryUpdate();
                  if (newItem.getCount() > count) {
                     playerIU.addModifiedItem(newItem);
                  } else {
                     playerIU.addNewItem(newItem);
                  }

                  targetPlayer.sendPacket(playerIU);
               } else {
                  targetPlayer.sendItemList(false);
               }

               playerSU = new StatusUpdate(targetPlayer);
               playerSU.addAttribute(14, targetPlayer.getCurrentLoad());
               targetPlayer.sendPacket(playerSU);
            } else if (target instanceof PetInventory) {
               PetInventoryUpdate petIU = new PetInventoryUpdate();
               if (newItem.getCount() > count) {
                  petIU.addModifiedItem(newItem);
               } else {
                  petIU.addNewItem(newItem);
               }

               ((PetInventory)target).getOwner().sendPacket(petIU);
            }

            return newItem;
         }
      }
   }

   public boolean exchangeItemsById(String process, GameObject reference, int coinId, long cost, int rewardId, long count, boolean sendMessage) {
      PcInventory inv = this.getInventory();
      if (!inv.validateCapacityByItemId(rewardId, count)) {
         if (sendMessage) {
            this.sendPacket(SystemMessageId.SLOTS_FULL);
         }

         return false;
      } else if (!inv.validateWeightByItemId(rewardId, count)) {
         if (sendMessage) {
            this.sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
         }

         return false;
      } else if (this.destroyItemByItemId(process, coinId, cost, reference, sendMessage)) {
         this.addItem(process, rewardId, count, reference, sendMessage);
         return true;
      } else {
         return false;
      }
   }

   public boolean dropItem(String process, ItemInstance item, GameObject reference, boolean sendMessage, boolean protectItem) {
      item = this._inventory.dropItem(process, item, this, reference);
      if (item == null) {
         if (sendMessage) {
            this.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
         }

         return false;
      } else {
         item.dropMe(this, this.getX() + Rnd.get(50) - 25, this.getY() + Rnd.get(50) - 25, this.getZ());
         if (Config.AUTODESTROY_ITEM_AFTER > 0
            && Config.DESTROY_DROPPED_PLAYER_ITEM
            && !Config.LIST_PROTECTED_ITEMS.contains(item.getId())
            && (item.isEquipable() && Config.DESTROY_EQUIPABLE_PLAYER_ITEM || !item.isEquipable())) {
            ItemsAutoDestroy.getInstance().addItem(item);
         }

         if (Config.DESTROY_DROPPED_PLAYER_ITEM) {
            if (item.isEquipable() && (!item.isEquipable() || !Config.DESTROY_EQUIPABLE_PLAYER_ITEM)) {
               item.setProtected(true);
            } else {
               item.setProtected(false);
            }
         } else {
            item.setProtected(true);
         }

         if (protectItem) {
            item.getDropProtection().protect(this, false);
         }

         if (!Config.FORCE_INVENTORY_UPDATE) {
            InventoryUpdate playerIU = new InventoryUpdate();
            playerIU.addItem(item);
            this.sendPacket(playerIU);
         } else {
            this.sendItemList(false);
         }

         StatusUpdate su = new StatusUpdate(this);
         su.addAttribute(14, this.getCurrentLoad());
         this.sendPacket(su);
         if (sendMessage) {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_DROPPED_S1);
            sm.addItemName(item);
            this.sendPacket(sm);
         }

         return true;
      }
   }

   public boolean dropItem(String process, ItemInstance item, GameObject reference, boolean sendMessage) {
      return this.dropItem(process, item, reference, sendMessage, false);
   }

   public ItemInstance dropItem(String process, int objectId, long count, int x, int y, int z, GameObject reference, boolean sendMessage, boolean protectItem) {
      ItemInstance invitem = this._inventory.getItemByObjectId(objectId);
      ItemInstance item = this._inventory.dropItem(process, objectId, count, this, reference);
      if (item == null) {
         if (sendMessage) {
            this.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
         }

         return null;
      } else {
         item.dropMe(this, x, y, z);
         if (Config.AUTODESTROY_ITEM_AFTER > 0
            && Config.DESTROY_DROPPED_PLAYER_ITEM
            && !Config.LIST_PROTECTED_ITEMS.contains(item.getId())
            && (item.isEquipable() && Config.DESTROY_EQUIPABLE_PLAYER_ITEM || !item.isEquipable())) {
            ItemsAutoDestroy.getInstance().addItem(item);
         }

         if (Config.DESTROY_DROPPED_PLAYER_ITEM) {
            if (item.isEquipable() && (!item.isEquipable() || !Config.DESTROY_EQUIPABLE_PLAYER_ITEM)) {
               item.setProtected(true);
            } else {
               item.setProtected(false);
            }
         } else {
            item.setProtected(true);
         }

         if (protectItem) {
            item.getDropProtection().protect(this, false);
         }

         if (!Config.FORCE_INVENTORY_UPDATE) {
            InventoryUpdate playerIU = new InventoryUpdate();
            playerIU.addItem(invitem);
            this.sendPacket(playerIU);
         } else {
            this.sendItemList(false);
         }

         StatusUpdate su = new StatusUpdate(this);
         su.addAttribute(14, this.getCurrentLoad());
         this.sendPacket(su);
         if (sendMessage) {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_DROPPED_S1);
            sm.addItemName(item);
            this.sendPacket(sm);
         }

         return item;
      }
   }

   public ItemInstance checkItemManipulation(int objectId, long count, String action) {
      if (World.getInstance().findObject(objectId) == null) {
         _log.finest(this.getObjectId() + ": player tried to " + action + " item not available in World");
         return null;
      } else {
         ItemInstance item = this.getInventory().getItemByObjectId(objectId);
         if (item == null || item.getOwnerId() != this.getObjectId()) {
            _log.finest(this.getObjectId() + ": player tried to " + action + " item he is not owner of");
            return null;
         } else if (count >= 0L && (count <= 1L || item.isStackable())) {
            if (count > item.getCount()) {
               _log.finest(this.getObjectId() + ": player tried to " + action + " more items than he owns");
               return null;
            } else if ((!this.hasSummon() || this.getSummon().getControlObjectId() != objectId) && this.getMountObjectID() != objectId) {
               if (this.getActiveEnchantItemId() == objectId) {
                  if (Config.DEBUG) {
                     _log.finest(this.getObjectId() + ":player tried to " + action + " an enchant scroll he was using");
                  }

                  return null;
               } else {
                  return !item.isAugmented() || !this.isCastingNow() && !this.isCastingSimultaneouslyNow() ? item : null;
               }
            } else {
               if (Config.DEBUG) {
                  _log.finest(this.getObjectId() + ": player tried to " + action + " item controling pet");
               }

               return null;
            }
         } else {
            _log.finest(this.getObjectId() + ": player tried to " + action + " item with invalid count: " + count);
            return null;
         }
      }
   }

   public void setProtection(boolean protect) {
      if (Config.DEVELOPER && (protect || this._protectEndTime > 0L)) {
         _log.warning(
            this.getName()
               + ": Protection "
               + (protect ? "ON " + (GameTimeController.getInstance().getGameTicks() + Config.PLAYER_SPAWN_PROTECTION * 10) : "OFF")
               + " (currently "
               + GameTimeController.getInstance().getGameTicks()
               + ")"
         );
      }

      this._protectEndTime = protect ? (long)(GameTimeController.getInstance().getGameTicks() + Config.PLAYER_SPAWN_PROTECTION * 10) : 0L;
   }

   public void setTeleportProtection(boolean protect) {
      if (Config.DEVELOPER && (protect || this._teleportProtectEndTime > 0L)) {
         _log.warning(
            this.getName()
               + ": Tele Protection "
               + (protect ? "ON " + (GameTimeController.getInstance().getGameTicks() + Config.PLAYER_TELEPORT_PROTECTION * 10) : "OFF")
               + " (currently "
               + GameTimeController.getInstance().getGameTicks()
               + ")"
         );
      }

      this._teleportProtectEndTime = protect ? (long)(GameTimeController.getInstance().getGameTicks() + Config.PLAYER_TELEPORT_PROTECTION * 10) : 0L;
   }

   public void setRecentFakeDeath(boolean protect) {
      this._recentFakeDeathEndTime = protect ? (long)(GameTimeController.getInstance().getGameTicks() + Config.PLAYER_FAKEDEATH_UP_PROTECTION * 10) : 0L;
   }

   public boolean isRecentFakeDeath() {
      return this._recentFakeDeathEndTime > (long)GameTimeController.getInstance().getGameTicks();
   }

   public final boolean isFakeDeath() {
      return this._isFakeDeath;
   }

   public final void setIsFakeDeath(boolean value) {
      this._isFakeDeath = value;
   }

   @Override
   public final boolean isAlikeDead() {
      return super.isAlikeDead() || this.isFakeDeath();
   }

   public GameClient getClient() {
      return this._client;
   }

   public void setClient(GameClient client) {
      if (client == null) {
         this.getFarmSystem().stopFarmTask(false);
         if (Config.ALLOW_DAILY_TASKS) {
            this.saveDailyTasks();
         }
      }

      this._client = client;
   }

   public String getIPAddress() {
      return this._client != null ? (this._client.isDetached() ? "Disconnected" : this._client.getIPAddress()) : "N/A";
   }

   public String getHWID() {
      return this._client != null ? this._client.getHWID() : "N/A";
   }

   public void kick() {
      if (this._client != null) {
         this._client.close(LogOutOk.STATIC_PACKET);
         this.setClient(null);
      }

      this.deleteMe();
   }

   public boolean checkFloodProtection(String type, String command) {
      return this._client == null ? false : this._client.checkFloodProtection(type, command);
   }

   public boolean isConnected() {
      return this._client != null && this._client.isConnected();
   }

   private void closeNetConnection(boolean closeClient) {
      GameClient client = this._client;
      if (client != null) {
         if (this.canOfflineMode(client.getActiveChar(), false)) {
            AuthServerCommunication.getInstance().removeAuthedClient(client.getLogin());
         }

         if (client.isDetached()) {
            client.cleanMe(true);
         } else if (client.getConnection() != null && !client.getConnection().isClosed()) {
            if (closeClient) {
               client.close(LogOutOk.STATIC_PACKET);
            } else {
               client.close(ServerClose.STATIC_PACKET);
            }
         }
      }
   }

   public boolean canOfflineMode(Player player, boolean sendMsg) {
      boolean canSetShop = false;
      if (!player.isInFightEvent()
         && !player.isInOlympiadMode()
         && !player.isFestivalParticipant()
         && !player.isBlockedFromExit()
         && !player.isJailed()
         && player.getVehicle() == null) {
         if (player.getLevel() < Config.OFFLINE_TRADE_MIN_LVL) {
            if (sendMsg) {
               player.sendMessage(new ServerMessage("CommunityGeneral.YOU_LEVEL_LOW", player.getLang()).toString());
            }

            return false;
         } else {
            if (!Config.OFFLINE_TRADE_ENABLE
               || (!player.isSellingBuffs() || player.getPrivateStoreType() != 8)
                  && player.getPrivateStoreType() != 8
                  && player.getPrivateStoreType() != 1
                  && player.getPrivateStoreType() != 3) {
               if (Config.OFFLINE_CRAFT_ENABLE && (player.isInCraftMode() || player.getPrivateStoreType() == 5)) {
                  canSetShop = true;
               }
            } else {
               canSetShop = true;
            }

            if (Config.OFFLINE_MODE_IN_PEACE_ZONE && !player.isInsideZone(ZoneId.PEACE)) {
               canSetShop = false;
            }

            return canSetShop;
         }
      } else {
         return false;
      }
   }

   public Location getCurrentSkillWorldPosition() {
      return this._currentSkillWorldPosition;
   }

   public void setCurrentSkillWorldPosition(Location worldPosition) {
      this._currentSkillWorldPosition = worldPosition;
   }

   @Override
   public void enableSkill(Skill skill) {
      super.enableSkill(skill);
      this._reuseTimeStampsSkills.remove(skill.getReuseHashCode());
   }

   @Override
   public boolean checkDoCastConditions(Skill skill, boolean msg) {
      if (!super.checkDoCastConditions(skill, msg)) {
         return false;
      } else if (skill.getSkillType() != SkillType.SUMMON || !this.hasSummon() && !this.isMounted() && !this.inObserverMode()) {
         if (!this.isInOlympiadMode() || !skill.isHeroSkill() && skill.getSkillType() != SkillType.RESURRECT) {
            if (this.getCharges() < skill.getChargeConsume() || this.isInAirShip() && !skill.hasEffectType(EffectType.REFUEL_AIRSHIP)) {
               if (msg) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
                  sm.addSkillName(skill);
                  this.sendPacket(sm);
               }

               return false;
            } else {
               return true;
            }
         } else {
            if (msg) {
               this.sendPacket(SystemMessageId.THIS_SKILL_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
            }

            return false;
         }
      } else {
         if (msg) {
            this.sendPacket(SystemMessageId.SUMMON_ONLY_ONE);
         }

         return false;
      }
   }

   private boolean needCpUpdate() {
      double currentCp = this.getCurrentCp();
      if (currentCp <= 1.0 || this.getMaxCp() < 352.0) {
         return true;
      } else if (!(currentCp <= this._cpUpdateDecCheck) && !(currentCp >= this._cpUpdateIncCheck)) {
         return false;
      } else {
         if (currentCp == this.getMaxCp()) {
            this._cpUpdateIncCheck = currentCp + 1.0;
            this._cpUpdateDecCheck = currentCp - this._cpUpdateInterval;
         } else {
            double doubleMulti = currentCp / this._cpUpdateInterval;
            int intMulti = (int)doubleMulti;
            this._cpUpdateDecCheck = this._cpUpdateInterval * (double)(doubleMulti < (double)intMulti ? intMulti-- : intMulti);
            this._cpUpdateIncCheck = this._cpUpdateDecCheck + this._cpUpdateInterval;
         }

         return true;
      }
   }

   private boolean needMpUpdate() {
      double currentMp = this.getCurrentMp();
      if (currentMp <= 1.0 || this.getMaxMp() < 352.0) {
         return true;
      } else if (!(currentMp <= this._mpUpdateDecCheck) && !(currentMp >= this._mpUpdateIncCheck)) {
         return false;
      } else {
         if (currentMp == this.getMaxMp()) {
            this._mpUpdateIncCheck = currentMp + 1.0;
            this._mpUpdateDecCheck = currentMp - this._mpUpdateInterval;
         } else {
            double doubleMulti = currentMp / this._mpUpdateInterval;
            int intMulti = (int)doubleMulti;
            this._mpUpdateDecCheck = this._mpUpdateInterval * (double)(doubleMulti < (double)intMulti ? intMulti-- : intMulti);
            this._mpUpdateIncCheck = this._mpUpdateDecCheck + this._mpUpdateInterval;
         }

         return true;
      }
   }

   @Override
   public void broadcastStatusUpdate() {
      try {
         if (this._broadcastStatusUpdateTask != null) {
            return;
         }

         this._broadcastStatusUpdateTask = ThreadPoolManager.getInstance()
            .schedule(new Player.BroadcastStatusUpdateTask(), Config.BROADCAST_STATUS_UPDATE_INTERVAL);
      } catch (Exception var2) {
      }
   }

   public void broadcastStatusUpdateImpl() {
      boolean needCpUpdate = this.needCpUpdate();
      boolean needHpUpdate = this.needHpUpdate();
      boolean needMpUpdate = this.needMpUpdate();
      if (needCpUpdate || needHpUpdate || needMpUpdate) {
         StatusUpdate su = new StatusUpdate(this);
         su.addAttribute(10, this.getMaxHp());
         su.addAttribute(9, (int)this.getCurrentHp());
         su.addAttribute(12, this.getMaxMp());
         su.addAttribute(11, (int)this.getCurrentMp());
         su.addAttribute(34, this.getMaxCp());
         su.addAttribute(33, (int)this.getCurrentCp());
         this.sendPacket(su);
         if (this.isInParty() && (needCpUpdate || needHpUpdate || needMpUpdate)) {
            this.getParty().broadcastToPartyMembers(this, new PartySmallWindowUpdate(this));
         }

         if (this.isInOlympiadMode() && this.isOlympiadStart() && (needCpUpdate || needHpUpdate)) {
            OlympiadGameTask game = OlympiadGameManager.getInstance().getOlympiadTask(this.getOlympiadGameId());
            if (game != null && game.isBattleStarted()) {
               game.getZone().broadcastStatusUpdate(this);
            }
         }

         if (this.isInDuel() && (needCpUpdate || needHpUpdate)) {
            DuelManager.getInstance().broadcastToOppositTeam(this, new ExDuelUpdateUserInfo(this));
         }
      }
   }

   public final void broadcastTitleInfo() {
      this.sendUserInfo(true);
      this.broadcastPacket(new NickNameChanged(this));
   }

   @Override
   public int getAllyId() {
      return this._clan == null ? 0 : this._clan.getAllyId();
   }

   public int getAllyCrestId() {
      if (this.getClanId() == 0) {
         return 0;
      } else {
         return this.getClan().getAllyId() == 0 ? 0 : this.getClan().getAllyCrestId();
      }
   }

   public void queryGameGuard() {
      if (this.getClient() != null) {
         this.getClient().setGameGuardOk(false);
         this.sendPacket(new GameGuardQuery());
      }

      if (Config.GAMEGUARD_ENFORCE) {
         ThreadPoolManager.getInstance().schedule(new GameGuardCheckTask(this), 30000L);
      }
   }

   @Override
   public void sendPacket(GameServerPacket packet) {
      if (this._client != null) {
         this._client.sendPacket(packet);
      }
   }

   @Override
   public void sendPacket(GameServerPacket... packets) {
      if (this._client != null) {
         for(GameServerPacket p : packets) {
            this._client.sendPacket(p);
         }
      }
   }

   @Override
   public void sendPacket(List<? extends GameServerPacket> packets) {
      if (this._client != null) {
         for(GameServerPacket p : packets) {
            this._client.sendPacket(p);
         }
      }
   }

   @Override
   public void sendPacket(GameServerPacket packet, SystemMessageId id) {
      if (this._client != null) {
         this._client.sendPacket(packet);
         if (id != null) {
            this.sendPacket(SystemMessage.getSystemMessage(id));
         }
      }
   }

   @Override
   public void sendPacket(SystemMessageId id) {
      this.sendPacket(SystemMessage.getSystemMessage(id));
   }

   public void doInteract(Creature target) {
      if (target != null && !this.isActionsDisabled()) {
         if (target instanceof Player) {
            Player temp = (Player)target;
            this.sendActionFailed();
            if (temp.getPrivateStoreType() != 1 && temp.getPrivateStoreType() != 8) {
               if (temp.getPrivateStoreType() == 3) {
                  this.sendPacket(new PrivateStoreBuyList(this, temp));
               } else if (temp.getPrivateStoreType() == 5) {
                  this.sendPacket(new RecipeShopSellList(this, temp));
               }
            } else if (temp.isSellingBuffs()) {
               SellBuffsManager.sendBuffMenu(this, temp, 1);
            } else {
               this.sendPacket(new PrivateStoreSellList(this, temp));
            }
         } else {
            target.onAction(this);
         }
      } else {
         this.sendActionFailed();
      }
   }

   public void doAutoLoot(Attackable target, int itemId, long itemCount) {
      if (!Config.DISABLE_ITEM_DROP_LIST.contains(itemId)) {
         if (this.isInParty() && !ItemsParser.getInstance().getTemplate(itemId).isHerb()) {
            this.getParty().distributeItem(this, itemId, itemCount, false, target);
         } else {
            if (itemId == 57) {
               this.addAdena("Loot", itemCount, target, true);
            } else {
               this.addItem("Loot", itemId, itemCount, target, true);
            }
         }
      }
   }

   public void doAutoLoot(Attackable target, ItemHolder item) {
      this.doAutoLoot(target, item.getId(), item.getCount());
   }

   public void doPickupItem(GameObject object) {
      if (!this.isAlikeDead() && !this.isFakeDeathNow()) {
         this.getAI().setIntention(CtrlIntention.IDLE);
         if (!(object instanceof ItemInstance)) {
            _log.warning(this + " trying to pickup wrong target." + this.getTarget());
         } else {
            ItemInstance target = (ItemInstance)object;
            this.sendActionFailed();
            StopMove sm = new StopMove(this);
            this.sendPacket(sm);
            SystemMessage smsg = null;
            synchronized(target) {
               if (!target.isVisible()) {
                  this.sendActionFailed();
                  return;
               }

               if (!target.getDropProtection().tryPickUp(this)) {
                  this.sendActionFailed();
                  smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1);
                  smsg.addItemName(target);
                  this.sendPacket(smsg);
                  return;
               }

               if ((this.isInParty() && this.getParty().getLootDistribution() == 0 || !this.isInParty()) && !this._inventory.validateCapacity(target)) {
                  this.sendActionFailed();
                  this.sendPacket(SystemMessageId.SLOTS_FULL);
                  return;
               }

               if (this.isInvul() && !this.canOverrideCond(PcCondOverride.ITEM_CONDITIONS)) {
                  this.sendActionFailed();
                  smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1);
                  smsg.addItemName(target);
                  this.sendPacket(smsg);
                  return;
               }

               if (target.getOwnerId() != 0 && target.getOwnerId() != this.getObjectId() && !this.isInLooterParty(target.getOwnerId())) {
                  if (target.getId() == 57) {
                     smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1_ADENA);
                     smsg.addItemNumber(target.getCount());
                  } else if (target.getCount() > 1L) {
                     smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S2_S1_S);
                     smsg.addItemName(target);
                     smsg.addItemNumber(target.getCount());
                  } else {
                     smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1);
                     smsg.addItemName(target);
                  }

                  this.sendActionFailed();
                  this.sendPacket(smsg);
                  return;
               }

               if (FortSiegeManager.getInstance().isCombat(target.getId()) && !this.isInFightEvent() && !FortSiegeManager.getInstance().checkIfCanPickup(this)
                  )
                {
                  return;
               }

               if (target.getItemLootShedule() != null && (target.getOwnerId() == this.getObjectId() || this.isInLooterParty(target.getOwnerId()))) {
                  target.resetOwnerTimer();
               }

               target.pickupMe(this);
               if (Config.SAVE_DROPPED_ITEM) {
                  ItemsOnGroundManager.getInstance().removeObject(target);
               }
            }

            if (target.getItem().isHerb()) {
               IItemHandler handler = ItemHandler.getInstance().getHandler(target.getEtcItem());
               if (handler == null) {
                  _log.warning("No item handler registered for item ID: " + target.getId() + ".");
               } else {
                  handler.useItem(this, target, false);
               }

               ItemsParser.getInstance().destroyItem("Consume", target, this, null);
            } else if (CursedWeaponsManager.getInstance().isCursed(target.getId())) {
               this.addItem("Pickup", target, null, true);
            } else if (FortSiegeManager.getInstance().isCombat(target.getId()) && !this.isInFightEvent()) {
               this.addItem("Pickup", target, null, true);
            } else {
               if (target.getItemType() instanceof ArmorType || target.getItemType() instanceof WeaponType) {
                  if (target.getEnchantLevel() > 0) {
                     smsg = SystemMessage.getSystemMessage(SystemMessageId.ANNOUNCEMENT_C1_PICKED_UP_S2_S3);
                     smsg.addPcName(this);
                     smsg.addNumber(target.getEnchantLevel());
                     smsg.addItemName(target.getId());
                     this.broadcastPacket(smsg, 1400);
                  } else {
                     smsg = SystemMessage.getSystemMessage(SystemMessageId.ANNOUNCEMENT_C1_PICKED_UP_S2);
                     smsg.addPcName(this);
                     smsg.addItemName(target.getId());
                     this.broadcastPacket(smsg, 1400);
                  }
               }

               if (this.isInParty()) {
                  this.getParty().distributeItem(this, target);
               } else if (target.getId() == 57 && this.getInventory().getAdenaInstance() != null) {
                  this.addAdena("Pickup", target.getCount(), null, true);
                  ItemsParser.getInstance().destroyItem("Pickup", target, this, null);
               } else {
                  this.addItem("Pickup", target, null, true);
                  if (target.isEtcItem()) {
                     this.checkToEquipArrows(target);
                  }
               }
            }
         }
      }
   }

   public void checkToEquipArrows(ItemInstance newItem) {
      if (newItem != null) {
         ItemInstance weapon = this.getInventory().getPaperdollItem(5);
         if (weapon != null) {
            EtcItem etcItem = newItem.getEtcItem();
            if (etcItem != null) {
               EtcItemType itemType = etcItem.getItemType();
               if (weapon.getItemType() == WeaponType.BOW && itemType == EtcItemType.ARROW) {
                  this.checkAndEquipArrows();
               } else if (weapon.getItemType() == WeaponType.CROSSBOW && itemType == EtcItemType.BOLT) {
                  this.checkAndEquipBolts();
               }
            }
         }
      }
   }

   private boolean isValidPrivateStoreZone(boolean isTrade) {
      if (Config.TRADE_ONLY_IN_PEACE_ZONE && !this.isInZonePeace()) {
         this.sendPacket(SystemMessageId.NO_PRIVATE_STORE_HERE);
         return false;
      } else {
         List<ZoneType> zones = ZoneManager.getInstance().getZones(this);
         if (zones != null && !zones.isEmpty()) {
            for(ZoneType zone : zones) {
               if (zone != null && !zone.getAllowStore()) {
                  if (isTrade) {
                     this.sendPacket(SystemMessageId.NO_PRIVATE_STORE_HERE);
                  } else {
                     this.sendPacket(SystemMessageId.NO_PRIVATE_WORKSHOP_HERE);
                  }

                  this.sendActionFailed();
                  return false;
               }
            }
         }

         return true;
      }
   }

   public boolean canOpenPrivateStore(boolean isTrade) {
      return !this.isSellingBuffs()
         && !this.isAlikeDead()
         && !this.isInOlympiadMode()
         && !this.isMounted()
         && this.isValidPrivateStoreZone(isTrade)
         && !this.isActionsDisabled()
         && !this.isInCombat()
         && !this.isInDuel()
         && !this.isProcessingRequest()
         && !this.isProcessingTransaction()
         && !this.isInFightEvent();
   }

   public void tryOpenPrivateBuyStore() {
      if (this.canOpenPrivateStore(true)) {
         if (this.getPrivateStoreType() == 3 || this.getPrivateStoreType() == 4) {
            this.setPrivateStoreType(0);
         }

         if (this.getPrivateStoreType() == 0) {
            if (this.isSitting()) {
               this.standUp();
            }

            this.setPrivateStoreType(4);
            this.sendPacket(new PrivateStoreBuyManageList(this));
         }
      }
   }

   public void tryOpenPrivateSellStore(boolean isPackageSale) {
      if (this.canOpenPrivateStore(true)) {
         if (this.getPrivateStoreType() == 1 || this.getPrivateStoreType() == 2 || this.getPrivateStoreType() == 8) {
            this.setPrivateStoreType(0);
         }

         if (this.getPrivateStoreType() == 0) {
            if (this.isSitting()) {
               this.standUp();
            }

            this.setPrivateStoreType(2);
            this.sendPacket(new PrivateStoreSellManageList(this, isPackageSale));
         }
      }
   }

   public final PreparedListContainer getMultiSell() {
      return this._currentMultiSell;
   }

   public final void setMultiSell(PreparedListContainer list) {
      this._currentMultiSell = list;
   }

   @Override
   public boolean isTransformed() {
      return this._transformation != null && !this._transformation.isStance();
   }

   public boolean isInStance() {
      return this._transformation != null && this._transformation.isStance();
   }

   public void transform(Transform transformation) {
      if (this._transformation != null) {
         SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.YOU_ALREADY_POLYMORPHED_AND_CANNOT_POLYMORPH_AGAIN);
         this.sendPacket(msg);
      } else if (this.fireTransformListeners(transformation, true)) {
         this.setQueuedSkill(null, false, false);
         if (this.isMounted()) {
            this.dismount();
         }

         this._transformation = transformation;
         TransformTemplate template = transformation.getTemplate(this);
         if (template != null) {
            for(Effect effect : this.getAllEffects()) {
               if (effect != null && (effect.getSkill().isToggle() || effect.getSkill().getId() == 1557)) {
                  boolean found = false;
                  if (effect.getSkill().getId() != 1557) {
                     for(SkillHolder holder : template.getSkills()) {
                        if (holder.getId() == effect.getSkill().getId()) {
                           found = true;
                        }
                     }
                  }

                  if (!found) {
                     effect.exit();
                  }
               }
            }
         }

         transformation.onTransform(this);
         this.sendSkillList(true);
         this.broadcastUserInfo(true);
      }
   }

   @Override
   public void untransform() {
      if (this._transformation != null) {
         if (!this.fireTransformListeners(this._transformation, false)) {
            return;
         }

         this.setQueuedSkill(null, false, false);
         this._transformation.onUntransform(this);
         this._transformation = null;
         Skill skill = null;

         for(Effect effect : this.getAllEffects()) {
            if (effect != null && effect.getSkill().hasEffectType(EffectType.TRANSFORMATION)) {
               skill = effect.getSkill();
            }
         }

         if (skill != null) {
            this.stopSkillEffects(skill.getId());
         }

         this.sendSkillList(true);
         this.broadcastUserInfo(true);
      }
   }

   @Override
   public Transform getTransformation() {
      return this._transformation;
   }

   public int getTransformationId() {
      return this.isTransformed() ? this.getTransformation().getId() : 0;
   }

   public void setFastTarget(GameObject newTarget) {
      super.setTarget(newTarget);
   }

   @Override
   public void setTarget(GameObject newTarget) {
      if (newTarget != null) {
         boolean isInParty = newTarget.isPlayer() && this.isInParty() && this.getParty().containsPlayer(newTarget.getActingPlayer());
         if (!isInParty && Math.abs(newTarget.getZ() - this.getZ()) > 1000) {
            newTarget = null;
         }

         if (newTarget != null && !isInParty && !newTarget.isVisible()) {
            newTarget = null;
         }

         if (!this.isGM() && newTarget instanceof Vehicle) {
            newTarget = null;
         }
      }

      GameObject oldTarget = this.getTarget();
      if (oldTarget != null) {
         if (oldTarget.equals(newTarget)) {
            if (newTarget != null && newTarget.getObjectId() != this.getObjectId()) {
               this.sendPacket(new ValidateLocation(newTarget));
            }

            return;
         }

         oldTarget.removeStatusListener(this);
      }

      if (newTarget instanceof Creature) {
         Creature target = (Creature)newTarget;
         if (newTarget.getObjectId() != this.getObjectId()) {
            this.sendPacket(new ValidateLocation(target));
         }

         this.sendPacket(new MyTargetSelected(this, target));
         target.addStatusListener(this);
         StatusUpdate su = new StatusUpdate(target);
         su.addAttribute(10, target.getMaxHp());
         su.addAttribute(9, (int)target.getCurrentHp());
         this.sendPacket(su);
         Broadcast.toKnownPlayers(this, new TargetSelected(this.getObjectId(), newTarget.getObjectId(), this.getX(), this.getY(), this.getZ()));
      }

      if (newTarget == null && this.getTarget() != null) {
         this.broadcastPacket(new TargetUnselected(this));
      }

      super.setTarget(newTarget);
   }

   @Override
   public ItemInstance getActiveWeaponInstance() {
      return this.getInventory().getPaperdollItem(5);
   }

   @Override
   public Weapon getActiveWeaponItem() {
      ItemInstance weapon = this.getActiveWeaponInstance();
      return weapon == null ? this.getFistsWeaponItem() : (Weapon)weapon.getItem();
   }

   public ItemInstance getChestArmorInstance() {
      return this.getInventory().getPaperdollItem(6);
   }

   public ItemInstance getLegsArmorInstance() {
      return this.getInventory().getPaperdollItem(11);
   }

   public Armor getActiveChestArmorItem() {
      ItemInstance armor = this.getChestArmorInstance();
      return armor == null ? null : (Armor)armor.getItem();
   }

   public Armor getActiveLegsArmorItem() {
      ItemInstance legs = this.getLegsArmorInstance();
      return legs == null ? null : (Armor)legs.getItem();
   }

   public boolean isWearingHeavyArmor() {
      ItemInstance legs = this.getLegsArmorInstance();
      ItemInstance armor = this.getChestArmorInstance();
      if (armor != null && legs != null && (ArmorType)legs.getItemType() == ArmorType.HEAVY && (ArmorType)armor.getItemType() == ArmorType.HEAVY) {
         return true;
      } else {
         return armor != null && this.getInventory().getPaperdollItem(6).getItem().getBodyPart() == 32768 && (ArmorType)armor.getItemType() == ArmorType.HEAVY;
      }
   }

   public boolean isWearingLightArmor() {
      ItemInstance legs = this.getLegsArmorInstance();
      ItemInstance armor = this.getChestArmorInstance();
      if (armor != null && legs != null && (ArmorType)legs.getItemType() == ArmorType.LIGHT && (ArmorType)armor.getItemType() == ArmorType.LIGHT) {
         return true;
      } else {
         return armor != null && this.getInventory().getPaperdollItem(6).getItem().getBodyPart() == 32768 && (ArmorType)armor.getItemType() == ArmorType.LIGHT;
      }
   }

   public boolean isWearingMagicArmor() {
      ItemInstance legs = this.getLegsArmorInstance();
      ItemInstance armor = this.getChestArmorInstance();
      if (armor != null && legs != null && (ArmorType)legs.getItemType() == ArmorType.MAGIC && (ArmorType)armor.getItemType() == ArmorType.MAGIC) {
         return true;
      } else {
         return armor != null && this.getInventory().getPaperdollItem(6).getItem().getBodyPart() == 32768 && (ArmorType)armor.getItemType() == ArmorType.MAGIC;
      }
   }

   public boolean isMarried() {
      return this._married;
   }

   public void setMarried(boolean state) {
      this._married = state;
   }

   public boolean isEngageRequest() {
      return this._engagerequest;
   }

   public void setEngageRequest(boolean state, int playerid) {
      this._engagerequest = state;
      this._engageid = playerid;
   }

   public void setMarryRequest(boolean state) {
      this._marryrequest = state;
   }

   public boolean isMarryRequest() {
      return this._marryrequest;
   }

   public void setMarryAccepted(boolean state) {
      this._marryaccepted = state;
   }

   public boolean isMarryAccepted() {
      return this._marryaccepted;
   }

   public int getEngageId() {
      return this._engageid;
   }

   public int getPartnerId() {
      return this._partnerId;
   }

   public void setPartnerId(int partnerid) {
      this._partnerId = partnerid;
   }

   public int getCoupleId() {
      return this._coupleId;
   }

   public void setCoupleId(int coupleId) {
      this._coupleId = coupleId;
   }

   public void scriptAswer(int answer) {
      Pair<Integer, OnAnswerListener> entry = this.getAskListener(true);
      if (entry != null) {
         OnAnswerListener listener = entry.getValue();
         if (answer == 1) {
            listener.sayYes();
         } else {
            listener.sayNo();
         }
      }
   }

   @Override
   public ItemInstance getSecondaryWeaponInstance() {
      return this.getInventory().getPaperdollItem(7);
   }

   @Override
   public Item getSecondaryWeaponItem() {
      ItemInstance item = this.getInventory().getPaperdollItem(7);
      return item != null ? item.getItem() : null;
   }

   @Override
   protected void onDeath(Creature killer) {
      this.getFarmSystem().stopFarmTask(false);
      if (this.isMounted()) {
         this.stopFeed();
      }

      synchronized(this) {
         if (this.isFakeDeathNow()) {
            this.stopFakeDeath(true);
         }
      }

      if (this.isInFightEvent()) {
         if (killer != null && killer.isPlayable() && killer.isInFightEvent()) {
            Player player = killer.isSummon() ? killer.getSummon().getOwner() : killer.getActingPlayer();
            if (player != null) {
               player.getFightEvent().onKilled(player, this);
            }
         } else if (this.isPlayer()) {
            this.getFightEvent().onKilled(killer, this);
         }

         if (this.getFightEvent() instanceof MonsterAttackEvent) {
            ((MonsterAttackEvent)this.getFightEvent()).checkAlivePlayer();
         }
      }

      AerialCleftEvent.getInstance().onKill(killer, this);
      if (killer != null) {
         Player pk = killer.getActingPlayer();
         if (pk != null) {
            pk.getEvents().onPvPKill(this);
            RevengeManager.getInstance().checkKiller(this, pk);
            FunPvpZone zone = ZoneManager.getInstance().getZone(pk, FunPvpZone.class);
            if (zone != null && !this.isInSameParty(pk) && DoubleSessionManager.getInstance().check(pk, this)) {
               zone.givereward(pk, this);
            }

            if (this.getParty() != null && this.getParty().getUCState() instanceof UCTeam) {
               ((UCTeam)this.getParty().getUCState()).onKill(this, pk);
            }

            if (Config.ANNOUNCE_PK_PVP && !pk.isGM()) {
               String msg = "";
               if (this.getPvpFlag() == 0) {
                  msg = Config.ANNOUNCE_PK_MSG.replace("$killer", pk.getName()).replace("$target", this.getName());
                  if (Config.ANNOUNCE_PK_PVP_NORMAL_MESSAGE) {
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1);
                     sm.addString(msg);
                     Announcements.getInstance().announceToAll(sm);
                  } else {
                     Announcements.getInstance().announceToAll(msg);
                  }
               } else if (this.getPvpFlag() != 0) {
                  msg = Config.ANNOUNCE_PVP_MSG.replace("$killer", pk.getName()).replace("$target", this.getName());
                  if (Config.ANNOUNCE_PK_PVP_NORMAL_MESSAGE) {
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1);
                     sm.addString(msg);
                     Announcements.getInstance().announceToAll(sm);
                  } else {
                     Announcements.getInstance().announceToAll(msg);
                  }
               }
            }

            if (Hitman.getActive()) {
               Hitman.onDeath(pk, this);
            }

            if (this.isInParty() && Config.ALLOW_PARTY_RANK_COMMAND) {
               PartyTemplate tpl = this.getParty().getMemberRank(this);
               if (tpl != null) {
                  tpl.addDeaths();
               }
            }
         }

         this.broadcastStatusUpdate();
         this.setExpBeforeDeath(0L);
         if (this.isCursedWeaponEquipped()) {
            CursedWeaponsManager.getInstance().drop(this._cursedWeaponEquippedId, killer);
         } else if (this.isCombatFlagEquipped() && !this.isInFightEvent()) {
            if (TerritoryWarManager.getInstance().isTWInProgress()) {
               TerritoryWarManager.getInstance().dropCombatFlag(this, true, false);
            } else {
               Fort fort = FortManager.getInstance().getFort(this);
               if (fort != null) {
                  FortSiegeManager.getInstance().dropCombatFlag(this, fort.getId());
               } else {
                  int slot = this.getInventory().getSlotFromItem(this.getInventory().getItemByItemId(9819));
                  this.getInventory().unEquipItemInBodySlot(slot);
                  this.destroyItem("CombatFlag", this.getInventory().getItemByItemId(9819), null, true);
               }
            }
         } else if (pk == null || !pk.isCursedWeaponEquipped()) {
            this.onDieDropItem(killer);
            if ((!this.isInsideZone(ZoneId.PVP) || this.isInsideZone(ZoneId.SIEGE))
               && pk != null
               && pk.getClan() != null
               && this.getClan() != null
               && !this.isAcademyMember()
               && !pk.isAcademyMember()
               && (this._clan.isAtWarWith(pk.getClanId()) && pk.getClan().isAtWarWith(this._clan.getId()) || this.isInSiege() && pk.isInSiege())
               && DoubleSessionManager.getInstance().check(killer, this)) {
               if (this.getClan().getReputationScore() > 0) {
                  pk.getClan().addReputationScore(Config.REPUTATION_SCORE_PER_KILL, false);
               }

               if (pk.getClan().getReputationScore() > 0) {
                  this._clan.takeReputationScore(Config.REPUTATION_SCORE_PER_KILL, false);
               }

               RewardManager.getInstance().checkClanWarReward(pk, this);
               pk.getCounters().addAchivementInfo("clanWarKills", 0, -1L, false, false, false);
            }

            if (Config.ALT_GAME_DELEVEL) {
               if (!this.isLucky()) {
                  boolean siegeNpc = killer instanceof DefenderInstance || killer instanceof FortCommanderInstance;
                  boolean atWar = pk != null && this.getClan() != null && this.getClan().isAtWarWith(pk.getClanId());
                  this.deathPenalty(killer, atWar, pk != null, siegeNpc);
               }
            } else if (!this.isInsideZone(ZoneId.PVP) || this.isInSiege() || pk == null) {
               this.onDieUpdateKarma();
            }
         }
      }

      if (!this._cubics.isEmpty()) {
         for(CubicInstance cubic : this._cubics.values()) {
            cubic.stopAction();
            cubic.cancelDisappear();
         }

         this._cubics.clear();
      }

      if (this._fusionSkill != null) {
         this.abortCast();
      }

      for(Creature character : World.getInstance().getAroundCharacters(this)) {
         if (character.getFusionSkill() != null && character.getFusionSkill().getTarget() == this) {
            character.abortCast();
         }
      }

      if (this.isInParty() && this.getParty().isInDimensionalRift()) {
         this.getParty().getDimensionalRift().getDeadMemberList().add(this);
      }

      if (this.getAgathionId() != 0) {
         this.setAgathionId(0);
      }

      this.calculateDeathPenaltyBuffLevel(killer);
      this.stopRentPet();
      this.stopWaterTask();
      this.getRecommendation().stopRecBonus();
      if (this.isPhoenixBlessed() || this.isAffected(EffectFlag.CHARM_OF_COURAGE) && this.isInsideZone(ZoneId.SIEGE)) {
         this.reviveRequest(this, null, false);
      }

      super.onDeath(killer);
   }

   private void onDieDropItem(Creature killer) {
      if (killer != null) {
         Player pk = killer.getActingPlayer();
         if (this.getKarma() > 0 || pk == null || pk.getClan() == null || this.getClan() == null || !pk.getClan().isAtWarWith(this.getClanId())) {
            if ((!this.isInsideZone(ZoneId.PVP) || pk == null) && (!this.isGM() || Config.KARMA_DROP_GM)) {
               boolean isKarmaDrop = false;
               boolean isKillerNpc = killer instanceof Npc;
               int pkLimit = Config.KARMA_PK_LIMIT;
               int dropEquip = 0;
               int dropEquipWeapon = 0;
               int dropItem = 0;
               int dropLimit = 0;
               int dropPercent = 0;
               if (this.getKarma() > 0 && this.getPkKills() >= pkLimit) {
                  isKarmaDrop = true;
                  dropPercent = Config.KARMA_RATE_DROP;
                  dropEquip = Config.KARMA_RATE_DROP_EQUIP;
                  dropEquipWeapon = Config.KARMA_RATE_DROP_EQUIP_WEAPON;
                  dropItem = Config.KARMA_RATE_DROP_ITEM;
                  dropLimit = Config.KARMA_DROP_LIMIT;
               } else if (isKillerNpc && this.getLevel() > 4 && !this.isFestivalParticipant()) {
                  dropPercent = Config.PLAYER_RATE_DROP;
                  dropEquip = Config.PLAYER_RATE_DROP_EQUIP;
                  dropEquipWeapon = Config.PLAYER_RATE_DROP_EQUIP_WEAPON;
                  dropItem = Config.PLAYER_RATE_DROP_ITEM;
                  dropLimit = Config.PLAYER_DROP_LIMIT;
               }

               if (dropPercent > 0 && Rnd.get(100) < dropPercent) {
                  int dropCount = 0;
                  int itemDropPercent = 0;

                  for(ItemInstance itemDrop : this.getInventory().getItems()) {
                     if (!itemDrop.isShadowItem()
                        && !itemDrop.isTimeLimitedItem()
                        && itemDrop.isDropable()
                        && itemDrop.getId() != 57
                        && itemDrop.getItem().getType2() != 3
                        && (!this.hasSummon() || this.getSummon().getControlObjectId() != itemDrop.getObjectId())
                        && Arrays.binarySearch(Config.KARMA_LIST_NONDROPPABLE_ITEMS, itemDrop.getId()) < 0
                        && Arrays.binarySearch(Config.KARMA_LIST_NONDROPPABLE_PET_ITEMS, itemDrop.getId()) < 0) {
                        if (itemDrop.isEquipped()) {
                           itemDropPercent = itemDrop.getItem().getType2() == 0 ? dropEquipWeapon : dropEquip;
                           this.getInventory().unEquipItemInSlot(itemDrop.getLocationSlot());
                        } else {
                           itemDropPercent = dropItem;
                        }

                        if (Rnd.get(100) < itemDropPercent) {
                           this.dropItem("DieDrop", itemDrop, killer, true);
                           if (isKarmaDrop) {
                              _log.warning(this.getName() + " has karma and dropped id = " + itemDrop.getId() + ", count = " + itemDrop.getCount());
                           } else {
                              _log.warning(this.getName() + " dropped id = " + itemDrop.getId() + ", count = " + itemDrop.getCount());
                           }

                           if (++dropCount >= dropLimit) {
                              break;
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void onDieUpdateKarma() {
      if (this.getKarma() > 0) {
         double karmaLost = (double)Config.KARMA_LOST_BASE;
         karmaLost *= (double)this.getLevel();
         karmaLost *= (double)this.getLevel() / 100.0;
         karmaLost = (double)Math.round(karmaLost);
         if (karmaLost <= 0.0) {
            karmaLost = 1.0;
         }

         this.setKarma(this.getKarma() - (int)karmaLost);
      }
   }

   public void onKillUpdatePvPKarma(Creature target) {
      if (target != null) {
         if (target instanceof Playable) {
            Player targetPlayer = target.getActingPlayer();
            if (targetPlayer != null) {
               if (targetPlayer != this) {
                  if (this.isInFightEvent() && targetPlayer.isInFightEvent() && this.getFightEvent() != null && this.getFightEvent().givePvpPoints()) {
                     this.increasePvpKills(target);
                  } else if (this.isCursedWeaponEquipped() && target.isPlayer()) {
                     CursedWeaponsManager.getInstance().increaseKills(this._cursedWeaponEquippedId);
                  } else if (!this.isInDuel() || !targetPlayer.isInDuel()) {
                     if (this.isInsideZone(ZoneId.FUN_PVP) && targetPlayer.isInsideZone(ZoneId.FUN_PVP)) {
                        FunPvpZone zone = ZoneManager.getInstance().getZone(this, FunPvpZone.class);
                        if (zone != null) {
                           if (zone.allowPvpKills()) {
                              this.increasePvpKills(target);
                           }

                           if (!zone.isPvpZone() && targetPlayer.getPvpFlag() == 0) {
                              this.increasePkKillsAndKarma(target);
                           }

                           return;
                        }
                     }

                     if (!this.isInsideZone(ZoneId.PVP) && !targetPlayer.isInsideZone(ZoneId.PVP)) {
                        if ((!this.checkIfPvP(target) || targetPlayer.getPvpFlag() == 0)
                           && (!this.isInsideZone(ZoneId.PVP) || !targetPlayer.isInsideZone(ZoneId.PVP))) {
                           if (targetPlayer.getClan() != null
                              && this.getClan() != null
                              && this.getClan().isAtWarWith(targetPlayer.getClanId())
                              && targetPlayer.getClan().isAtWarWith(this.getClanId())
                              && targetPlayer.getPledgeType() != -1
                              && this.getPledgeType() != -1) {
                              this.increasePvpKills(target);
                              return;
                           }

                           if (targetPlayer.getKarma() > 0) {
                              if (Config.KARMA_AWARD_PK_KILL) {
                                 this.increasePvpKills(target);
                              }
                           } else if (targetPlayer.getPvpFlag() == 0) {
                              if (Hitman.getActive() && Config.HITMAN_TAKE_KARMA && Hitman.exists(targetPlayer.getObjectId())) {
                                 return;
                              }

                              this.increasePkKillsAndKarma(target);
                              this.checkItemRestriction();
                           }
                        } else {
                           this.increasePvpKills(target);
                        }
                     } else {
                        if (this.getSiegeState() > 0 && targetPlayer.getSiegeState() > 0 && this.getSiegeState() != targetPlayer.getSiegeState()) {
                           Clan killerClan = this.getClan();
                           Clan targetClan = targetPlayer.getClan();
                           if (killerClan != null && targetClan != null) {
                              if (DoubleSessionManager.getInstance().check(this, targetPlayer)) {
                                 SiegeZone zone = ZoneManager.getInstance().getZone(this, SiegeZone.class);
                                 if (zone != null) {
                                    if (zone.getFortId() > 0) {
                                       RewardManager.getInstance().checkFortPvpReward(this, targetPlayer);
                                       this.getCounters().addAchivementInfo("fortSiegePvpKills", 0, -1L, false, false, false);
                                    } else if (zone.getCastleId() > 0) {
                                       RewardManager.getInstance().checkCastlePvpReward(this, targetPlayer);
                                       this.getCounters().addAchivementInfo("siegePvpKills", 0, -1L, false, false, false);
                                    }
                                 }
                              }

                              killerClan.addSiegeKill();
                              targetClan.addSiegeDeath();
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public void increasePvpKills(Creature target) {
      this.increasePvpKills(target, false);
   }

   public void increasePvpKills(Creature target, boolean event) {
      if (target instanceof Player && (event || DoubleSessionManager.getInstance().check(this, target))) {
         if (!this.isInFightEvent() || !target.isInFightEvent()) {
            this.setPvpKills(this.getPvpKills() + 1);
            this.getCounters().addAchivementInfo("pvpKills", 0, -1L, false, false, false);
            if (this.isInParty() && Config.ALLOW_PARTY_RANK_COMMAND) {
               PartyTemplate tpl = this.getParty().getMemberRank(this);
               if (tpl != null) {
                  tpl.addKills();
               }
            }

            if (Config.ALLOW_DAILY_TASKS && this.getActiveDailyTasks() != null) {
               for(PlayerTaskTemplate taskTemplate : this.getActiveDailyTasks()) {
                  if (taskTemplate.getType().equalsIgnoreCase("Pvp") && !taskTemplate.isComplete()) {
                     DailyTaskTemplate task = DailyTaskManager.getInstance().getDailyTask(taskTemplate.getId());
                     if (taskTemplate.getCurrentPvpCount() < task.getPvpCount() && DailyTaskManager.getInstance().checkHWID(this, target.getActingPlayer())) {
                        taskTemplate.setCurrentPvpCount(taskTemplate.getCurrentPvpCount() + 1);
                        if (taskTemplate.isComplete()) {
                           IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getHandler("missions");
                           if (vch != null) {
                              this.updateDailyStatus(taskTemplate);
                              vch.useVoicedCommand("missions", this, null);
                           }
                        }
                     }
                  }
               }
            }

            RewardManager.getInstance().checkPvpReward(this, target.getActingPlayer());
         } else if (this.getFightEvent() != null && this.getFightEvent().givePvpPoints()) {
            this.setPvpKills(this.getPvpKills() + 1);
         }
      }

      this.sendUserInfo(true);
   }

   public void increasePkKillsAndKarma(Creature target) {
      if (!this.isInFightEvent()) {
         int baseKarma = Config.KARMA_MIN_KARMA;
         int karmaLimit = Config.KARMA_MAX_KARMA;
         int pkLVL = this.getLevel();
         int pkPKCount = this.getPkKills();
         int targLVL = target.getLevel();
         int lvlDiffMulti = 0;
         int pkCountMulti = 0;
         if (pkPKCount > 0) {
            pkCountMulti = pkPKCount / 2;
         } else {
            pkCountMulti = 1;
         }

         if (pkCountMulti < 1) {
            pkCountMulti = 1;
         }

         if (pkLVL > targLVL) {
            lvlDiffMulti = pkLVL / targLVL;
         } else {
            lvlDiffMulti = 1;
         }

         if (lvlDiffMulti < 1) {
            lvlDiffMulti = 1;
         }

         int newKarma = baseKarma * pkCountMulti;
         newKarma *= lvlDiffMulti;
         if (newKarma < baseKarma) {
            newKarma = baseKarma;
         }

         if (newKarma > karmaLimit) {
            newKarma = karmaLimit;
         }

         if (this.getKarma() > Integer.MAX_VALUE - newKarma) {
            newKarma = Integer.MAX_VALUE - this.getKarma();
         }

         if (!this.isInFightEvent()) {
            this.setKarma(this.getKarma() + newKarma);
         }

         if (target instanceof Player && DoubleSessionManager.getInstance().check(this, target)) {
            int newPks = 0;
            newPks = this.getPkKills() + 1;
            this.setPkKills(newPks);
            this.getCounters().addAchivementInfo("pkKills", 0, -1L, false, false, false);
            if (this.isInParty() && Config.ALLOW_PARTY_RANK_COMMAND) {
               PartyTemplate tpl = this.getParty().getMemberRank(this);
               if (tpl != null) {
                  tpl.addKills();
               }
            }

            if (Config.ALLOW_DAILY_TASKS && this.getActiveDailyTasks() != null) {
               for(PlayerTaskTemplate taskTemplate : this.getActiveDailyTasks()) {
                  if (taskTemplate.getType().equalsIgnoreCase("Pk") && !taskTemplate.isComplete()) {
                     DailyTaskTemplate task = DailyTaskManager.getInstance().getDailyTask(taskTemplate.getId());
                     if (taskTemplate.getCurrentPkCount() < task.getPkCount() && DailyTaskManager.getInstance().checkHWID(this, target.getActingPlayer())) {
                        taskTemplate.setCurrentPkCount(taskTemplate.getCurrentPkCount() + 1);
                        if (taskTemplate.isComplete()) {
                           IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getHandler("missions");
                           if (vch != null) {
                              this.updateDailyStatus(taskTemplate);
                              vch.useVoicedCommand("missions", this, null);
                           }
                        }
                     }
                  }
               }
            }
         }

         this.sendUserInfo(true);
      }
   }

   public int calculateKarmaLost(long exp) {
      long expGained = Math.abs(exp);
      expGained /= (long)Config.KARMA_XP_DIVIDER;
      int karmaLost = 0;
      if (expGained > 2147483647L) {
         karmaLost = Integer.MAX_VALUE;
      } else {
         karmaLost = (int)expGained;
      }

      if (karmaLost < Config.KARMA_LOST_BASE) {
         karmaLost = Config.KARMA_LOST_BASE;
      }

      if (karmaLost > this.getKarma()) {
         karmaLost = this.getKarma();
      }

      return karmaLost;
   }

   public void updatePvPStatus() {
      if (!this.isInFightEvent()) {
         if (!this.isInsideZone(ZoneId.PVP) && ZoneManager.getInstance().getOlympiadStadium(this) == null) {
            this.setPvpFlagLasts(System.currentTimeMillis() + (long)Config.PVP_NORMAL_TIME);
            if (this.getPvpFlag() == 0) {
               this.startPvPFlag();
            }
         }
      }
   }

   public void updatePvPStatus(Creature target) {
      Player player_target = target.getActingPlayer();
      if (!this.isInFightEvent()) {
         if (player_target != null) {
            if (!this.isInDuel() || player_target.getDuelId() != this.getDuelId()) {
               if ((!this.isInsideZone(ZoneId.PVP) || !player_target.isInsideZone(ZoneId.PVP)) && player_target.getKarma() == 0) {
                  if (this.checkIfPvP(player_target)) {
                     this.setPvpFlagLasts(System.currentTimeMillis() + (long)Config.PVP_PVP_TIME);
                  } else {
                     this.setPvpFlagLasts(System.currentTimeMillis() + (long)Config.PVP_NORMAL_TIME);
                  }

                  if (this.getPvpFlag() == 0) {
                     this.startPvPFlag();
                  }
               }
            }
         }
      }
   }

   public boolean isLucky() {
      return this.getLevel() <= 9 && this.getFirstPassiveEffect(EffectType.LUCKY) != null;
   }

   public void restoreExp(double restorePercent) {
      if (this.getExpBeforeDeath() > 0L) {
         this.getStat().addExp(Math.round((double)(this.getExpBeforeDeath() - this.getExp()) * restorePercent / 100.0));
         this.setExpBeforeDeath(0L);
      }
   }

   public void deathPenalty(Creature killer, boolean atwar, boolean isPlayer, boolean isSiege) {
      if (!this.getNevitSystem().isBlessingActive() && !this.isInFightEvent()) {
         int lvl = this.getLevel();
         double percentLost = ExpPercentLostParser.getInstance().getExpPercent(this.getLevel());
         if (killer != null) {
            if (killer.isRaid()) {
               percentLost *= this.calcStat(Stats.REDUCE_EXP_LOST_BY_RAID, 1.0, null, null);
            } else if (killer.isMonster()) {
               percentLost *= this.calcStat(Stats.REDUCE_EXP_LOST_BY_MOB, 1.0, null, null);
            } else if (killer.isPlayable()) {
               percentLost *= this.calcStat(Stats.REDUCE_EXP_LOST_BY_PVP, 1.0, null, null);
            }
         }

         if (this.getKarma() > 0) {
            percentLost *= (double)Config.RATE_KARMA_EXP_LOST;
         }

         if (this.isFestivalParticipant() || atwar) {
            percentLost /= 4.0;
         }

         long lostExp = 0L;
         if (lvl < ExperienceParser.getInstance().getMaxLevel()) {
            lostExp = Math.round((double)(this.getStat().getExpForLevel(lvl + 1) - this.getStat().getExpForLevel(lvl)) * percentLost / 100.0);
         } else {
            lostExp = Math.round(
               (double)(
                     this.getStat().getExpForLevel(ExperienceParser.getInstance().getMaxLevel())
                        - this.getStat().getExpForLevel(ExperienceParser.getInstance().getMaxLevel() - 1)
                  )
                  * percentLost
                  / 100.0
            );
         }

         this.setExpBeforeDeath(this.getExp());
         if (this.isInsideZone(ZoneId.PVP)) {
            if (this.isInsideZone(ZoneId.SIEGE)) {
               if (this.isInSiege() && (isPlayer || isSiege)) {
                  lostExp = 0L;
               }
            } else if (isPlayer) {
               lostExp = 0L;
            }
         }

         this.getStat().removeExp(lostExp);
      }
   }

   public void startTimers() {
      this.startPcBangPointsTask();
   }

   public void stopAllTimers() {
      this.stopHpMpRegeneration();
      this.stopWarnUserTakeBreak();
      this.stopWaterTask();
      this.stopFeed();
      this.clearPetData();
      this.storePetFood(this._mountNpcId);
      this.stopRentPet();
      this.stopPvpRegTask();
      this.stopSoulTask();
      this.stopChargeTask();
      this.stopFameTask();
      this.stopVitalityTask();
      this.getRecommendation().stopRecomendationTask();
      this.stopPcBangPointsTask();
      this.stopPremiumTask();
      this.stopTempHeroTask();
      this.stopOnlineRewardTask();
      this.stopPunishmentTask();
      this.getNevitSystem().stopTasksOnLogout();
   }

   @Override
   public Summon getSummon() {
      return this._summon;
   }

   public Decoy getDecoy() {
      return this._decoy;
   }

   public TrapInstance getTrap() {
      return this._trap;
   }

   public void setPet(Summon summon) {
      this._summon = summon;
   }

   public void setDecoy(Decoy decoy) {
      this._decoy = decoy;
   }

   public void setTrap(TrapInstance trap) {
      this._trap = trap;
   }

   public List<TamedBeastInstance> getTrainedBeasts() {
      return this._tamedBeast;
   }

   public void addTrainedBeast(TamedBeastInstance tamedBeast) {
      if (this._tamedBeast == null) {
         this._tamedBeast = new CopyOnWriteArrayList<>();
      }

      this._tamedBeast.add(tamedBeast);
   }

   public Request getRequest() {
      return this._request;
   }

   public void setActiveRequester(Player requester) {
      this._activeRequester = requester;
   }

   public Player getActiveRequester() {
      Player requester = this._activeRequester;
      if (requester != null && requester.isRequestExpired() && this._activeTradeList == null) {
         this._activeRequester = null;
      }

      return this._activeRequester;
   }

   public boolean isProcessingRequest() {
      return this.getActiveRequester() != null || this._requestExpireTime > (long)GameTimeController.getInstance().getGameTicks();
   }

   public boolean isProcessingTransaction() {
      return this.getActiveRequester() != null
         || this._activeTradeList != null
         || this._requestExpireTime > (long)GameTimeController.getInstance().getGameTicks();
   }

   public void onTransactionRequest(Player partner) {
      this._requestExpireTime = (long)(GameTimeController.getInstance().getGameTicks() + 150);
      partner.setActiveRequester(this);
   }

   public boolean isRequestExpired() {
      return this._requestExpireTime <= (long)GameTimeController.getInstance().getGameTicks();
   }

   public void onTransactionResponse() {
      this._requestExpireTime = 0L;
   }

   public void setActiveWarehouse(ItemContainer warehouse) {
      this._activeWarehouse = warehouse;
   }

   public ItemContainer getActiveWarehouse() {
      return this._activeWarehouse;
   }

   public void setActiveTradeList(TradeList tradeList) {
      this._activeTradeList = tradeList;
   }

   public TradeList getActiveTradeList() {
      return this._activeTradeList;
   }

   public void onTradeStart(Player partner) {
      this._activeTradeList = new TradeList(this);
      this._activeTradeList.setPartner(partner);
      SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.BEGIN_TRADE_WITH_C1);
      msg.addPcName(partner);
      this.sendPacket(msg);
      this.sendPacket(new TradeStart(this));
   }

   public void onTradeConfirm(Player partner) {
      SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_CONFIRMED_TRADE);
      msg.addPcName(partner);
      this.sendPacket(msg);
      partner.sendPacket(TradePressOwnOk.STATIC_PACKET);
      this.sendPacket(TradePressOtherOk.STATIC_PACKET);
   }

   public void onTradeCancel(Player partner) {
      if (this._activeTradeList != null) {
         this._activeTradeList.lock();
         this._activeTradeList = null;
         this.sendPacket(new TradeDone(0));
         SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_CANCELED_TRADE);
         msg.addPcName(partner);
         this.sendPacket(msg);
      }
   }

   public void onTradeFinish(boolean successfull) {
      this._activeTradeList = null;
      this.sendPacket(new TradeDone(1));
      if (successfull) {
         this.sendPacket(SystemMessageId.TRADE_SUCCESSFUL);
      }
   }

   public void startTrade(Player partner) {
      this.onTradeStart(partner);
      partner.onTradeStart(this);
   }

   public void cancelActiveTrade() {
      if (this._activeTradeList != null) {
         Player partner = this._activeTradeList.getPartner();
         if (partner != null) {
            partner.onTradeCancel(this);
         }

         this.onTradeCancel(this);
      }
   }

   public boolean hasManufactureShop() {
      return this._manufactureItems != null && !this._manufactureItems.isEmpty();
   }

   public Map<Integer, ManufactureItemTemplate> getManufactureItems() {
      if (this._manufactureItems == null) {
         synchronized(this) {
            if (this._manufactureItems == null) {
               this._manufactureItems = Collections.synchronizedMap(new LinkedHashMap<>());
            }
         }
      }

      return this._manufactureItems;
   }

   public String getStoreName() {
      return this._storeName;
   }

   public void setStoreName(String name) {
      this._storeName = name == null ? "" : name;
   }

   public TradeList getSellList() {
      if (this._sellList == null) {
         this._sellList = new TradeList(this);
      }

      return this._sellList;
   }

   public TradeList getBuyList() {
      if (this._buyList == null) {
         this._buyList = new TradeList(this);
      }

      return this._buyList;
   }

   public void setPrivateStoreType(int type) {
      this._privatestore = type;
      if (this._privatestore == 0) {
         for(TradeItem item : this.getSellList().getItems()) {
            AuctionsManager.getInstance().removeStore(this, item.getAuctionId());
         }

         this.unsetVar("storemode");
         this.setIsInStoreNow(false);
         if (this.getClient() != null) {
            this.resetHidePlayer();
         }

         if (Config.OFFLINE_DISCONNECT_FINISHED && (this.getClient() == null || this.getClient().isDetached())) {
            this.setOfflineMode(false);
            this.deleteMe();
         }
      } else {
         this.setVar("storemode", String.valueOf(type), -1L);
      }
   }

   public int getPrivateStoreType() {
      return this._privatestore;
   }

   public void setClan(Clan clan) {
      this._clan = clan;
      if (clan == null) {
         this._clanId = 0;
         this._clanPrivileges = 0;
         this._pledgeType = 0;
         this._powerGrade = 0;
         this._lvlJoinedAcademy = 0;
         this._apprentice = 0;
         this._sponsor = 0;
         this._activeWarehouse = null;
      } else if (!clan.isMember(this.getObjectId())) {
         this.setClan(null);
      } else {
         this.broadcastUserInfo(true);
         this._clanId = clan.getId();
      }
   }

   public Clan getClan() {
      return this._clan;
   }

   public boolean isClanLeader() {
      if (this.getClan() == null) {
         return false;
      } else {
         return this.getObjectId() == this.getClan().getLeaderId();
      }
   }

   @Override
   protected void reduceArrowCount(boolean bolts) {
      ItemInstance arrows = this.getInventory().getPaperdollItem(7);
      if (arrows == null) {
         this.getInventory().unEquipItemInSlot(7);
         if (bolts) {
            this._boltItem = null;
         } else {
            this._arrowItem = null;
         }

         this.sendItemList(false);
      } else if (arrows.getCount() > 1L) {
         synchronized(arrows) {
            arrows.changeCountWithoutTrace(-1, this, null);
            arrows.setLastChange(2);
            if (GameTimeController.getInstance().getGameTicks() % 10 == 0) {
               arrows.updateDatabase();
            }

            this._inventory.refreshWeight();
         }

         if (!Config.FORCE_INVENTORY_UPDATE) {
            InventoryUpdate iu = new InventoryUpdate();
            iu.addModifiedItem(arrows);
            this.sendPacket(iu);
         } else {
            this.sendItemList(false);
         }
      } else {
         this._inventory.destroyItem("Consume", arrows, this, null);
         this.getInventory().unEquipItemInSlot(7);
         if (bolts) {
            this._boltItem = null;
         } else {
            this._arrowItem = null;
         }

         this.sendItemList(false);
      }
   }

   @Override
   protected boolean checkAndEquipArrows() {
      if (this.getInventory().getPaperdollItem(7) == null) {
         this._arrowItem = this.getInventory().findArrowForBow(this.getActiveWeaponItem());
         if (this._arrowItem != null) {
            this.getInventory().setPaperdollItem(7, this._arrowItem);
            this.sendItemList(false);
         }
      } else {
         this._arrowItem = this.getInventory().getPaperdollItem(7);
      }

      return this._arrowItem != null;
   }

   @Override
   protected boolean checkAndEquipBolts() {
      if (this.getInventory().getPaperdollItem(7) == null) {
         this._boltItem = this.getInventory().findBoltForCrossBow(this.getActiveWeaponItem());
         if (this._boltItem != null) {
            this.getInventory().setPaperdollItem(7, this._boltItem);
            this.sendItemList(false);
         }
      } else {
         this._boltItem = this.getInventory().getPaperdollItem(7);
      }

      return this._boltItem != null;
   }

   public boolean disarmWeapons() {
      ItemInstance wpn = this.getInventory().getPaperdollItem(5);
      if (wpn == null) {
         return true;
      } else if (this.isCursedWeaponEquipped()) {
         return false;
      } else if (this.isCombatFlagEquipped()) {
         return false;
      } else if (wpn.getWeaponItem().isForceEquip()) {
         return false;
      } else {
         ItemInstance[] unequiped = this.getInventory().unEquipItemInBodySlotAndRecord(wpn.getItem().getBodyPart());
         InventoryUpdate iu = new InventoryUpdate();

         for(ItemInstance itm : unequiped) {
            iu.addModifiedItem(itm);
         }

         this.sendPacket(iu);
         this.abortAttack();
         this.broadcastUserInfo(true);
         if (unequiped.length > 0) {
            SystemMessage sm;
            if (unequiped[0].getEnchantLevel() > 0) {
               sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
               sm.addNumber(unequiped[0].getEnchantLevel());
               sm.addItemName(unequiped[0]);
            } else {
               sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED);
               sm.addItemName(unequiped[0]);
            }

            this.sendPacket(sm);
         }

         return true;
      }
   }

   public boolean disarmShield() {
      ItemInstance sld = this.getInventory().getPaperdollItem(7);
      if (sld != null) {
         ItemInstance[] unequiped = this.getInventory().unEquipItemInBodySlotAndRecord(sld.getItem().getBodyPart());
         InventoryUpdate iu = new InventoryUpdate();

         for(ItemInstance itm : unequiped) {
            iu.addModifiedItem(itm);
         }

         this.sendPacket(iu);
         this.abortAttack();
         this.broadcastUserInfo(true);
         if (unequiped.length > 0) {
            SystemMessage sm = null;
            if (unequiped[0].getEnchantLevel() > 0) {
               sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
               sm.addNumber(unequiped[0].getEnchantLevel());
               sm.addItemName(unequiped[0]);
            } else {
               sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED);
               sm.addItemName(unequiped[0]);
            }

            this.sendPacket(sm);
         }
      }

      return true;
   }

   public boolean mount(Summon pet) {
      if (!this.disarmWeapons() || !this.disarmShield() || this.isTransformed()) {
         return false;
      } else if (!GeoEngine.canSeeTarget(this, pet, false)) {
         this.sendPacket(SystemMessageId.CANT_SEE_TARGET);
         return false;
      } else {
         this.stopAllToggles();
         this.setMount(pet.getId(), pet.getLevel());
         this.setMountObjectID(pet.getControlObjectId());
         this.clearPetData();
         this.startFeed(pet.getId());
         this.broadcastPacket(new Ride(this));
         this.broadcastUserInfo(true);
         pet.unSummon(this);
         return true;
      }
   }

   public boolean mount(int npcId, int controlItemObjId, boolean useFood) {
      if (this.disarmWeapons() && this.disarmShield() && !this.isTransformed()) {
         this.stopAllToggles();
         this.setMount(npcId, this.getLevel());
         this.clearPetData();
         this.setMountObjectID(controlItemObjId);
         this.broadcastPacket(new Ride(this));
         this.broadcastUserInfo(true);
         if (useFood) {
            this.startFeed(npcId);
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean mountPlayer(Summon pet) {
      if (pet != null && pet.isMountable() && !this.isMounted() && !this.isBetrayed()) {
         if (this.isDead()) {
            this.sendActionFailed();
            this.sendPacket(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_DEAD);
            return false;
         }

         if (pet.isDead()) {
            this.sendActionFailed();
            this.sendPacket(SystemMessageId.DEAD_STRIDER_CANT_BE_RIDDEN);
            return false;
         }

         if (pet.isInCombat() || pet.isRooted()) {
            this.sendActionFailed();
            this.sendPacket(SystemMessageId.STRIDER_IN_BATLLE_CANT_BE_RIDDEN);
            return false;
         }

         if (this.isInCombat()) {
            this.sendActionFailed();
            this.sendPacket(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE);
            return false;
         }

         if (this.isSitting()) {
            this.sendActionFailed();
            this.sendPacket(SystemMessageId.STRIDER_CAN_BE_RIDDEN_ONLY_WHILE_STANDING);
            return false;
         }

         if (this.isFishing()) {
            this.sendActionFailed();
            this.sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_2);
            return false;
         }

         if (this.isTransformed() || this.isCursedWeaponEquipped()) {
            this.sendActionFailed();
            return false;
         }

         if (this.getInventory().getItemByItemId(9819) != null) {
            this.sendActionFailed();
            this.sendMessage("You cannot mount a steed while holding a flag.");
            return false;
         }

         if (pet.isHungry()) {
            this.sendActionFailed();
            this.sendPacket(SystemMessageId.HUNGRY_STRIDER_NOT_MOUNT);
            return false;
         }

         if (!Util.checkIfInRange(200, this, pet, true)) {
            this.sendActionFailed();
            this.sendPacket(SystemMessageId.TOO_FAR_AWAY_FROM_FENRIR_TO_MOUNT);
            return false;
         }

         if (!pet.isDead() && !this.isMounted()) {
            this.mount(pet);
         }
      } else if (this.isRentedPet()) {
         this.stopRentPet();
      } else if (this.isMounted()) {
         if (this.getMountType() == MountType.WYVERN && this.isInsideZone(ZoneId.NO_LANDING)) {
            this.sendActionFailed();
            this.sendPacket(SystemMessageId.NO_DISMOUNT_HERE);
            return false;
         }

         if (this.isHungry()) {
            this.sendActionFailed();
            this.sendPacket(SystemMessageId.HUNGRY_STRIDER_NOT_MOUNT);
            return false;
         }

         this.dismount();
      }

      return true;
   }

   public boolean dismount() {
      boolean wasFlying = this.isFlying();
      this.sendPacket(new SetupGauge(this, 3, 0, 0));
      int petId = this._mountNpcId;
      this.setMount(0, 0);
      this.stopFeed();
      this.clearPetData();
      if (wasFlying) {
         this.removeSkill(SkillsParser.FrequentSkill.WYVERN_BREATH.getSkill());
      }

      this.broadcastPacket(new Ride(this));
      this.setMountObjectID(0);
      this.storePetFood(petId);
      this.broadcastUserInfo(true);
      return true;
   }

   public void setUptime(long time) {
      this._uptime = time;
   }

   public long getUptime() {
      return System.currentTimeMillis() - this._uptime;
   }

   @Override
   public boolean isInvul() {
      return super.isInvul() || this._teleportProtectEndTime > (long)GameTimeController.getInstance().getGameTicks();
   }

   @Override
   public boolean isBlocked() {
      return super.isBlocked() || this.inObserverMode() || this.isTeleporting();
   }

   @Override
   public boolean isInParty() {
      return this._party != null;
   }

   public void setParty(Party party) {
      this._party = party;
   }

   public void joinParty(Party party) {
      if (party != null) {
         this._party = party;
         party.addPartyMember(this);
      }
   }

   public void leaveParty() {
      if (this.isInParty()) {
         this._party.removePartyMember(this, Party.messageType.Disconnected);
         this._party = null;
      }
   }

   @Override
   public Party getParty() {
      return this._party;
   }

   @Override
   public boolean isGM() {
      return this.getAccessLevel().isGm();
   }

   public void setAccessLevel(int level) {
      this._accessLevel = AdminParser.getInstance().getAccessLevel(level);
      this.getAppearance().setNameColor(this._accessLevel.getNameColor());
      this.getAppearance().setTitleColor(this._accessLevel.getTitleColor());
      this.broadcastUserInfo(true);
      CharNameHolder.getInstance().addName(this);
      if (!AdminParser.getInstance().hasAccessLevel(level)) {
         _log.warning("Tryed to set unregistered access level " + level + " for " + this.toString() + ". Setting access level without privileges!");
      } else if (!Config.ENABLE_SAFE_ADMIN_PROTECTION && level > 0) {
         _log.warning(this._accessLevel.getName() + " access level set for character " + this.getName() + "! Just a warning to be careful ;)");
      }
   }

   @Override
   public AccessLevel getAccessLevel() {
      if (Config.EVERYBODY_HAS_ADMIN_RIGHTS) {
         return AdminParser.getInstance().getMasterAccessLevel();
      } else {
         if (this._accessLevel == null) {
            this.setAccessLevel(0);
         }

         return this._accessLevel;
      }
   }

   public void updateAndBroadcastStatus(int broadcastType) {
      if (!this._entering) {
         if (this._updateAndBroadcastStatusTask == null || this._updateAndBroadcastStatusTask.isDone()) {
            this._updateAndBroadcastStatusTask = ThreadPoolManager.getInstance()
               .schedule(new Player.UpdateAndBroadcastStatusTask(broadcastType), Config.USER_STATS_UPDATE_INTERVAL);
         }
      }
   }

   public void broadcastStatusImpl(int broadcastType) {
      this.refreshOverloaded();
      this.refreshExpertisePenalty();
      if (broadcastType == 1) {
         this.sendUserInfo(true);
      }

      if (broadcastType == 2) {
         this.broadcastUserInfo(true);
      }
   }

   public void setKarmaFlag(int flag) {
      this.sendUserInfo(true);
      this.broadcastRelationChanged();
   }

   public void broadcastKarma() {
      StatusUpdate su = new StatusUpdate(this);
      su.addAttribute(27, this.getKarma());
      this.sendPacket(su);
      this.broadcastRelationChanged();
   }

   public void setOnlineStatus(boolean isOnline, boolean updateInDb) {
      if (this._isOnline != isOnline) {
         this._isOnline = isOnline;
      }

      if (Hitman.getActive() && Hitman.exists(this.getObjectId())) {
         Hitman.getTarget(this.getObjectId()).setOnline(isOnline);
      }

      if (updateInDb) {
         this.updateOnlineStatus();
      }
   }

   public void setIsIn7sDungeon(boolean isIn7sDungeon) {
      this._isIn7sDungeon = isIn7sDungeon;
   }

   public void updateOnlineStatus() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("UPDATE characters SET online=?, lastAccess=? WHERE charId=?");
      ) {
         statement.setInt(1, this.isOnlineInt());
         statement.setLong(2, System.currentTimeMillis());
         statement.setInt(3, this.getObjectId());
         statement.execute();
      } catch (Exception var33) {
         _log.log(Level.SEVERE, "Failed updating character online status.", (Throwable)var33);
      }
   }

   public void restoreCharData() {
      CharacterSkillsDAO.getInstance().restoreSkills(this);
      this._macros.restoreMe();
      this._shortCuts.restoreMe();
      this.restoreHenna();
      CharacterBookMarkDAO.getInstance().restore(this);
      this.restoreRecipeBook(true);
      if (Config.STORE_RECIPE_SHOPLIST) {
         this.restoreRecipeShopList();
      }

      this.loadPremiumItemList();
      this.restorePetInventoryItems();
   }

   private void restoreRecipeBook(boolean loadCommon) {
      String sql = loadCommon
         ? "SELECT id, type, classIndex FROM character_recipebook WHERE charId=?"
         : "SELECT id FROM character_recipebook WHERE charId=? AND classIndex=? AND type = 1";

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(sql);
      ) {
         statement.setInt(1, this.getObjectId());
         if (!loadCommon) {
            statement.setInt(2, this._classIndex);
         }

         try (ResultSet rset = statement.executeQuery()) {
            this._dwarvenRecipeBook.clear();
            RecipeParser rd = RecipeParser.getInstance();

            while(rset.next()) {
               RecipeList recipe = rd.getRecipeList(rset.getInt("id"));
               if (loadCommon) {
                  if (rset.getInt(2) == 1) {
                     if (rset.getInt(3) == this._classIndex) {
                        this.registerDwarvenRecipeList(recipe, false);
                     }
                  } else {
                     this.registerCommonRecipeList(recipe, false);
                  }
               } else {
                  this.registerDwarvenRecipeList(recipe, false);
               }
            }
         }
      } catch (Exception var62) {
         _log.log(Level.SEVERE, "Could not restore recipe book data:" + var62.getMessage(), (Throwable)var62);
      }
   }

   public Map<Integer, PremiumItemTemplate> getPremiumItemList() {
      return this._premiumItems;
   }

   private void loadPremiumItemList() {
      String sql = "SELECT itemNum, itemId, itemCount, itemSender FROM character_premium_items WHERE charId=?";

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT itemNum, itemId, itemCount, itemSender FROM character_premium_items WHERE charId=?");
      ) {
         statement.setInt(1, this.getObjectId());

         try (ResultSet rset = statement.executeQuery()) {
            while(rset.next()) {
               int itemNum = rset.getInt("itemNum");
               int itemId = rset.getInt("itemId");
               long itemCount = rset.getLong("itemCount");
               String itemSender = rset.getString("itemSender");
               this._premiumItems.put(itemNum, new PremiumItemTemplate(itemId, itemCount, itemSender));
            }
         }
      } catch (Exception var64) {
         _log.log(Level.SEVERE, "Could not restore premium items: " + var64.getMessage(), (Throwable)var64);
      }
   }

   public void updatePremiumItem(int itemNum, long newcount) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("UPDATE character_premium_items SET itemCount=? WHERE charId=? AND itemNum=? ");
      ) {
         statement.setLong(1, newcount);
         statement.setInt(2, this.getObjectId());
         statement.setInt(3, itemNum);
         statement.execute();
      } catch (Exception var36) {
         _log.log(Level.SEVERE, "Could not update premium items: " + var36.getMessage(), (Throwable)var36);
      }
   }

   public void deletePremiumItem(int itemNum) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("DELETE FROM character_premium_items WHERE charId=? AND itemNum=? ");
      ) {
         statement.setInt(1, this.getObjectId());
         statement.setInt(2, itemNum);
         statement.execute();
      } catch (Exception var34) {
         _log.log(Level.SEVERE, "Could not delete premium item: " + var34);
      }
   }

   public synchronized void store(boolean storeActiveEffects) {
      CharacterDAO.getInstance().storePlayer(this);
      CharacterDAO.getInstance().storeSubClasses(this);
      this.storeEffect(storeActiveEffects);
      CharacterItemReuseDAO.getInstance().store(this);
      if (Config.STORE_RECIPE_SHOPLIST) {
         this.storeRecipeShopList();
      }

      if (Config.STORE_UI_SETTINGS) {
         this.storeUISettings();
      }

      SevenSigns.getInstance().saveSevenSignsData(this.getObjectId());
   }

   @Override
   public void store() {
      this.store(true);
   }

   @Override
   public void storeEffect(boolean storeEffects) {
      if (Config.STORE_SKILL_COOLTIME) {
         CharacterSkillSaveDAO.getInstance().store(this, Config.SUBCLASS_STORE_SKILL, storeEffects);
      }
   }

   public boolean isOnline() {
      return this._isOnline;
   }

   public int isOnlineInt() {
      if (this._isOnline && this.getClient() != null) {
         return this.getClient().isDetached() ? 2 : 1;
      } else {
         return 0;
      }
   }

   public boolean isIn7sDungeon() {
      return this._isIn7sDungeon;
   }

   @Override
   public Skill addSkill(Skill newSkill) {
      this.addCustomSkill(newSkill);
      return super.addSkill(newSkill);
   }

   public Skill addSkill(Skill newSkill, boolean store) {
      Skill oldSkill = this.addSkill(newSkill);
      if (store) {
         CharacterSkillsDAO.getInstance().store(this, newSkill, oldSkill, -1);
      }

      return oldSkill;
   }

   @Override
   public Skill removeSkill(Skill skill, boolean store) {
      this.removeCustomSkill(skill);
      return store ? this.removeSkill(skill) : super.removeSkill(skill, true);
   }

   public Skill removeSkill(Skill skill, boolean store, boolean cancelEffect) {
      this.removeCustomSkill(skill);
      return store ? this.removeSkill(skill) : super.removeSkill(skill, cancelEffect);
   }

   public Skill removeSkill(Skill skill) {
      this.removeCustomSkill(skill);
      Skill oldSkill = super.removeSkill(skill, true);
      if (oldSkill != null) {
         CharacterSkillsDAO.getInstance().remove(this, oldSkill.getId());
      }

      if (this.getTransformationId() <= 0 && !this.isCursedWeaponEquipped()) {
         ShortCutTemplate[] allShortCuts = this.getAllShortCuts();

         for(ShortCutTemplate sc : allShortCuts) {
            if (sc != null
               && skill != null
               && sc.getId() == skill.getId()
               && sc.getType() == ShortcutType.SKILL
               && (skill.getId() < 3080 || skill.getId() > 3259)) {
               this.deleteShortCut(sc.getSlot(), sc.getPage());
            }
         }

         return oldSkill;
      } else {
         return oldSkill;
      }
   }

   @Override
   public void restoreEffects() {
      CharacterSkillSaveDAO.getInstance().restore(this);
   }

   private void restoreHenna() {
      CharacterHennaDAO.getInstance().restore(this);
      this.recalcHennaStats();
   }

   public int getHennaEmptySlots() {
      int totalSlots = 0;
      if (this.getClassId().level() == 1) {
         totalSlots = 2;
      } else {
         totalSlots = 3;
      }

      for(int i = 0; i < 3; ++i) {
         if (this._henna[i] != null) {
            --totalSlots;
         }
      }

      return totalSlots <= 0 ? 0 : totalSlots;
   }

   public boolean removeHenna(int slot) {
      if (!this.fireHennaListeners(this.getHenna(slot + 1), false)) {
         return false;
      } else if (slot >= 1 && slot <= 3) {
         Henna henna = this._henna[--slot];
         if (henna == null) {
            return false;
         } else {
            this._henna[slot] = null;
            CharacterHennaDAO.getInstance().delete(this, slot + 1);
            if (!henna.getSkillList().isEmpty()) {
               for(Skill sk : henna.getSkillList()) {
                  if (sk != null && this.getKnownSkill(sk.getId()) != null) {
                     this.removeSkill(sk, false, true);
                  }
               }

               this.sendSkillList(false);
            }

            this.recalcHennaStats();
            this.sendPacket(new HennaInfo(this));
            this.sendUserInfo(true);
            this.getInventory().addItem("Henna", henna.getDyeItemId(), (long)henna.getCancelCount(), this, null);
            this.reduceAdena("Henna", (long)henna.getCancelFee(), this, false);
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
            sm.addItemName(henna.getDyeItemId());
            sm.addItemNumber((long)henna.getCancelCount());
            this.sendPacket(sm);
            this.sendPacket(SystemMessageId.SYMBOL_DELETED);
            return true;
         }
      } else {
         return false;
      }
   }

   public boolean addHenna(Henna henna) {
      if (!this.fireHennaListeners(henna, true)) {
         return false;
      } else {
         for(int i = 0; i < 3; ++i) {
            if (this._henna[i] == null) {
               this._henna[i] = henna;
               this.recalcHennaStats();
               CharacterHennaDAO.getInstance().add(this, henna.getDyeId(), i + 1);
               this.sendPacket(new HennaInfo(this));
               this.sendUserInfo(true);
               return true;
            }
         }

         return false;
      }
   }

   private void recalcHennaStats() {
      this._hennaINT = 0;
      this._hennaSTR = 0;
      this._hennaCON = 0;
      this._hennaMEN = 0;
      this._hennaWIT = 0;
      this._hennaDEX = 0;

      for(Henna h : this._henna) {
         if (h != null) {
            if (!h.getSkillList().isEmpty()) {
               for(Skill sk : h.getSkillList()) {
                  if (sk != null && this.getKnownSkill(sk.getId()) == null) {
                     this.addSkill(sk, false);
                  }
               }
            }

            this._hennaINT += this._hennaINT + h.getStatINT() > 5 ? 5 - this._hennaINT : h.getStatINT();
            this._hennaSTR += this._hennaSTR + h.getStatSTR() > 5 ? 5 - this._hennaSTR : h.getStatSTR();
            this._hennaMEN += this._hennaMEN + h.getStatMEN() > 5 ? 5 - this._hennaMEN : h.getStatMEN();
            this._hennaCON += this._hennaCON + h.getStatCON() > 5 ? 5 - this._hennaCON : h.getStatCON();
            this._hennaWIT += this._hennaWIT + h.getStatWIT() > 5 ? 5 - this._hennaWIT : h.getStatWIT();
            this._hennaDEX += this._hennaDEX + h.getStatDEX() > 5 ? 5 - this._hennaDEX : h.getStatDEX();
         }
      }
   }

   public Henna getHenna(int slot) {
      return slot >= 1 && slot <= 3 ? this._henna[slot - 1] : null;
   }

   public void setHenna(Henna[] henna) {
      this._henna = henna;
   }

   public boolean hasHennas() {
      for(Henna henna : this._henna) {
         if (henna != null) {
            return true;
         }
      }

      return false;
   }

   public Henna[] getHennaList() {
      return this._henna;
   }

   public int getHennaStatINT() {
      return this._hennaINT;
   }

   public int getHennaStatSTR() {
      return this._hennaSTR;
   }

   public int getHennaStatCON() {
      return this._hennaCON;
   }

   public int getHennaStatMEN() {
      return this._hennaMEN;
   }

   public int getHennaStatWIT() {
      return this._hennaWIT;
   }

   public int getHennaStatDEX() {
      return this._hennaDEX;
   }

   @Override
   public boolean isAutoAttackable(Creature attacker) {
      if (attacker == null) {
         return false;
      } else if (attacker == this || attacker == this.getSummon()) {
         return false;
      } else if (attacker instanceof Player && this.isCombatFlagEquipped() && ((Player)attacker).getSiegeSide() != 0) {
         return true;
      } else if (attacker instanceof FriendlyMobInstance) {
         return false;
      } else if (attacker.isMonster()) {
         return true;
      } else if (attacker.isPlayer() && this.getDuelState() == 1 && this.getDuelId() == ((Player)attacker).getDuelId()) {
         return true;
      } else if (this.isInParty() && this.getParty().getMembers().contains(attacker)) {
         return false;
      } else if (attacker.isPlayer() && attacker.getActingPlayer().isInOlympiadMode()) {
         return this.isInOlympiadMode() && this.isOlympiadStart() && ((Player)attacker).getOlympiadGameId() == this.getOlympiadGameId();
      } else if (this.isOnEvent()) {
         return true;
      } else if (this.isInFightEvent()) {
         return true;
      } else {
         if (attacker.isPlayable()) {
            if (this.isInsideZone(ZoneId.PEACE)) {
               return false;
            }

            Player attackerPlayer = attacker.getActingPlayer();
            if (this.getClan() != null) {
               Siege siege = SiegeManager.getInstance().getSiege(this.getX(), this.getY(), this.getZ());
               if (siege != null) {
                  if (siege.checkIsDefender(attackerPlayer.getClan())
                     && siege.checkIsDefender(this.getClan())
                     && !SiegeManager.getInstance().canAttackSameSiegeSide()) {
                     return false;
                  }

                  if (siege.checkIsAttacker(attackerPlayer.getClan())
                     && siege.checkIsAttacker(this.getClan())
                     && !SiegeManager.getInstance().canAttackSameSiegeSide()) {
                     return false;
                  }
               }

               if (this.getClan() != null
                  && attackerPlayer.getClan() != null
                  && this.getClan().isAtWarWith(attackerPlayer.getClanId())
                  && attackerPlayer.getClan().isAtWarWith(this.getClanId())
                  && this.getWantsPeace() == 0
                  && attackerPlayer.getWantsPeace() == 0
                  && !this.isAcademyMember()) {
                  return true;
               }
            }

            if (this.isInsideZone(ZoneId.PVP)
               && attackerPlayer.isInsideZone(ZoneId.PVP)
               && (!this.isInsideZone(ZoneId.SIEGE) || !attackerPlayer.isInsideZone(ZoneId.SIEGE))) {
               if (this.isInsideZone(ZoneId.FUN_PVP)) {
                  FunPvpZone zone = ZoneManager.getInstance().getZone(this, FunPvpZone.class);
                  if (zone != null && (!zone.isPvpZone() || zone.isPvpZone() && this.isFriend(attackerPlayer))) {
                     return false;
                  }
               }

               return true;
            }

            if (this.getClan() != null && this.getClan().isMember(attacker.getObjectId())) {
               return false;
            }

            if (attacker.isPlayer() && this.getAllyId() != 0 && this.getAllyId() == attackerPlayer.getAllyId()) {
               return false;
            }

            if (this.isInsideZone(ZoneId.PVP)
               && attackerPlayer.isInsideZone(ZoneId.PVP)
               && this.isInsideZone(ZoneId.SIEGE)
               && attackerPlayer.isInsideZone(ZoneId.SIEGE)) {
               return true;
            }
         } else if (attacker instanceof DefenderInstance && this.getClan() != null) {
            Siege siege = SiegeManager.getInstance().getSiege(this);
            return siege != null && siege.checkIsAttacker(this.getClan());
         }

         return this.getKarma() > 0 || this.getPvpFlag() > 0;
      }
   }

   @Override
   public boolean useMagic(Skill skill, boolean forceUse, boolean dontMove, boolean msg) {
      if (skill.isPassive()) {
         this.sendActionFailed();
         return false;
      } else if (skill.isToggle()) {
         if (this.getFirstEffect(skill.getId()) != null) {
            this.stopSkillEffects(skill.getId());
            return false;
         } else {
            this.doSimultaneousCast(skill);
            return false;
         }
      } else {
         GameObject target = null;
         switch(skill.getTargetType()) {
            case AURA:
            case FRONT_AURA:
            case BEHIND_AURA:
            case GROUND:
            case SELF:
            case AURA_CORPSE_MOB:
            case AURA_UNDEAD_ENEMY:
            case COMMAND_CHANNEL:
               target = this;
               break;
            default:
               target = skill.getFirstOfTargetList(this);
         }

         if (target != null && target != this && target.isPlayer() && skill.isOffensive() && this.isPKProtected(target.getActingPlayer())) {
            this.setIsCastingNow(false);
            this.sendMessage("You cannot attack this player!");
            this.sendActionFailed();
            return false;
         } else if (this.isCastingNow()) {
            SkillUseHolder currentSkill = this.getCurrentSkill();
            if (currentSkill != null && skill.getId() == currentSkill.getId()) {
               this.sendActionFailed();
               return false;
            } else if (!this.isSkillDisabled(skill) && !this.isSkillBlocked(skill)) {
               this.setQueuedSkill(skill, forceUse, dontMove);
               this.sendActionFailed();
               return false;
            } else {
               this.sendActionFailed();
               return false;
            }
         } else {
            this.setIsCastingNow(true);
            this.setCurrentSkill(skill, forceUse, dontMove);
            if (this.getQueuedSkill() != null) {
               this.setQueuedSkill(null, false, false);
            }

            if (!this.checkUseMagicConditions(skill, forceUse, dontMove, msg)) {
               this.setIsCastingNow(false);
               GameObject attackTarget = this.getTarget();
               if (skill.getFlyRadius() != 0 && attackTarget != null && attackTarget != this && attackTarget.isAutoAttackable(this)) {
                  this.getAI().setIntention(CtrlIntention.ATTACK, attackTarget);
               }

               return false;
            } else if (target == null) {
               this.setIsCastingNow(false);
               this.sendActionFailed();
               return false;
            } else {
               this.getAI().setIntention(CtrlIntention.CAST, skill, target);
               return true;
            }
         }
      }
   }

   private boolean checkUseMagicConditions(Skill skill, boolean forceUse, boolean dontMove, boolean msg) {
      SkillType sklType = skill.getSkillType();
      if (this.isOutOfControl() || this.isParalyzed() || this.isStunned() || this.isSleeping() || this.isSkillBlocked(skill)) {
         this.sendActionFailed();
         return false;
      } else if (this.isDead()) {
         this.sendActionFailed();
         return false;
      } else if (this.isFishing() && sklType != SkillType.PUMPING && sklType != SkillType.REELING && sklType != SkillType.FISHING) {
         if (msg) {
            this.sendPacket(SystemMessageId.ONLY_FISHING_SKILLS_NOW);
         }

         return false;
      } else if (this.inObserverMode()) {
         if (msg) {
            this.sendPacket(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE);
         }

         this.abortCast();
         this.sendActionFailed();
         return false;
      } else if (this.isSitting()) {
         if (msg) {
            this.sendPacket(SystemMessageId.CANT_MOVE_SITTING);
         }

         this.sendActionFailed();
         return false;
      } else {
         if (skill.isToggle()) {
            Effect effect = this.getFirstEffect(skill.getId());
            if (effect != null) {
               effect.exit();
               this.sendActionFailed();
               return false;
            }
         }

         if (this.isFakeDeathNow()) {
            this.sendActionFailed();
            return false;
         } else {
            GameObject target = null;
            TargetType sklTargetType = skill.getTargetType();
            Location worldPosition = this.getCurrentSkillWorldPosition();
            if (sklTargetType == TargetType.GROUND && worldPosition == null) {
               Util.handleIllegalPlayerAction(this, "" + this.getName() + " try use skill: " + skill.getNameEn() + " with null worldPosition!");
               this.sendActionFailed();
               return false;
            } else {
               switch(sklTargetType) {
                  case AURA:
                  case FRONT_AURA:
                  case BEHIND_AURA:
                  case GROUND:
                  case SELF:
                  case AURA_CORPSE_MOB:
                  case AURA_UNDEAD_ENEMY:
                  case COMMAND_CHANNEL:
                  case PARTY:
                  case CLAN:
                  case PARTY_CLAN:
                  case PARTY_NOTME:
                  case CORPSE_CLAN:
                  case AREA_SUMMON:
                     target = this;
                     break;
                  case PET:
                  case SERVITOR:
                  case SUMMON:
                     target = this.getSummon();
                     break;
                  default:
                     target = this.getTarget();
               }

               if (target == null) {
                  this.sendActionFailed();
                  return false;
               } else if (target instanceof FortBallistaInstance && skill.getFlyType() != FlyToLocation.FlyType.NONE) {
                  if (msg) {
                     this.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
                  }

                  return false;
               } else {
                  if (target.isDoor()) {
                     if (skill.getFlyType() != FlyToLocation.FlyType.NONE) {
                        if (msg) {
                           this.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
                        }

                        return false;
                     }

                     int activeSiegeId = ((DoorInstance)target).getFort() != null
                        ? ((DoorInstance)target).getFort().getId()
                        : (((DoorInstance)target).getCastle() != null ? ((DoorInstance)target).getCastle().getId() : 0);
                     if (TerritoryWarManager.getInstance().isTWInProgress()) {
                        if (TerritoryWarManager.getInstance().isAllyField(this, activeSiegeId)) {
                           if (msg) {
                              this.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
                           }

                           return false;
                        }
                     } else if (((DoorInstance)target).getCastle() != null && ((DoorInstance)target).getCastle().getId() > 0) {
                        if (!((DoorInstance)target).getCastle().getSiege().getIsInProgress()) {
                           if (msg) {
                              this.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
                           }

                           return false;
                        }
                     } else if (((DoorInstance)target).getFort() != null
                        && ((DoorInstance)target).getFort().getId() > 0
                        && !((DoorInstance)target).getIsShowHp()
                        && !((DoorInstance)target).getFort().getSiege().getIsInProgress()) {
                        if (msg) {
                           this.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
                        }

                        return false;
                     }
                  }

                  if (this.isInDuel() && target instanceof Playable) {
                     Player cha = target.getActingPlayer();
                     if (cha.getDuelId() != this.getDuelId()) {
                        if (msg) {
                           this.sendMessage("You cannot do this while duelling.");
                        }

                        this.sendActionFailed();
                        return false;
                     }
                  }

                  if (this.isSkillDisabled(skill)) {
                     SystemMessage sm = null;
                     if (this._reuseTimeStampsSkills.containsKey(skill.getReuseHashCode())) {
                        int remainingTime = (int)(this._reuseTimeStampsSkills.get(skill.getReuseHashCode()).getRemaining() / 1000L);
                        int hours = remainingTime / 3600;
                        int minutes = remainingTime % 3600 / 60;
                        int seconds = remainingTime % 60;
                        if (hours > 0) {
                           sm = SystemMessage.getSystemMessage(SystemMessageId.S2_HOURS_S3_MINUTES_S4_SECONDS_REMAINING_FOR_REUSE_S1);
                           sm.addSkillName(skill);
                           sm.addNumber(hours);
                           sm.addNumber(minutes);
                        } else if (minutes > 0) {
                           sm = SystemMessage.getSystemMessage(SystemMessageId.S2_MINUTES_S3_SECONDS_REMAINING_FOR_REUSE_S1);
                           sm.addSkillName(skill);
                           sm.addNumber(minutes);
                        } else {
                           sm = SystemMessage.getSystemMessage(SystemMessageId.S2_SECONDS_REMAINING_FOR_REUSE_S1);
                           sm.addSkillName(skill);
                        }

                        sm.addNumber(seconds);
                     } else {
                        sm = SystemMessage.getSystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE);
                        sm.addSkillName(skill);
                     }

                     if (msg) {
                        this.sendPacket(sm);
                     }

                     return false;
                  } else if (!skill.checkCondition(this, target, false, msg)) {
                     this.sendActionFailed();
                     return false;
                  } else {
                     if (skill.isOffensive()) {
                        if (this.isInsidePeaceZone(this, target) && !this.getAccessLevel().allowPeaceAttack()) {
                           if (msg) {
                              this.sendPacket(SystemMessageId.TARGET_IN_PEACEZONE);
                           }

                           this.sendActionFailed();
                           return false;
                        }

                        if (this.isInOlympiadMode() && !this.isOlympiadStart()) {
                           this.sendActionFailed();
                           return false;
                        }

                        if (target.getActingPlayer() != null
                           && this.getSiegeState() > 0
                           && this.isInsideZone(ZoneId.SIEGE)
                           && target.getActingPlayer().getSiegeState() == this.getSiegeState()
                           && target.getActingPlayer() != this
                           && target.getActingPlayer().getSiegeSide() == this.getSiegeSide()) {
                           if (!SiegeManager.getInstance().canAttackSameSiegeSide()) {
                              if (msg) {
                                 if (TerritoryWarManager.getInstance().isTWInProgress()) {
                                    this.sendPacket(SystemMessageId.YOU_CANNOT_ATTACK_A_MEMBER_OF_THE_SAME_TERRITORY);
                                 } else {
                                    this.sendPacket(SystemMessageId.FORCED_ATTACK_IS_IMPOSSIBLE_AGAINST_SIEGE_SIDE_TEMPORARY_ALLIED_MEMBERS);
                                 }
                              }

                              this.sendActionFailed();
                              return false;
                           }

                           Clan clan1 = target.getActingPlayer().getClan();
                           Clan clan2 = this.getClan();
                           if (clan1 != null
                              && clan2 != null
                              && (clan1.getAllyId() != 0 && clan2.getAllyId() != 0 && clan1.getAllyId() == clan2.getAllyId() || clan1.getId() == clan2.getId())
                              )
                            {
                              if (msg) {
                                 if (TerritoryWarManager.getInstance().isTWInProgress()) {
                                    this.sendPacket(SystemMessageId.YOU_CANNOT_ATTACK_A_MEMBER_OF_THE_SAME_TERRITORY);
                                 } else {
                                    this.sendPacket(SystemMessageId.FORCED_ATTACK_IS_IMPOSSIBLE_AGAINST_SIEGE_SIDE_TEMPORARY_ALLIED_MEMBERS);
                                 }
                              }

                              this.sendActionFailed();
                              return false;
                           }
                        }

                        switch(skill.getSkillType()) {
                           default:
                              if (!target.isDoor() && !target.canBeAttacked() && !this.getAccessLevel().allowPeaceAttack()) {
                                 this.sendActionFailed();
                                 return false;
                              }
                           case UNLOCK:
                           case UNLOCK_SPECIAL:
                           case DELUXE_KEY_UNLOCK:
                              if (target instanceof EventMonsterInstance && ((EventMonsterInstance)target).eventSkillAttackBlocked()) {
                                 this.sendActionFailed();
                                 return false;
                              }

                              if (!target.isAutoAttackable(this) && !forceUse) {
                                 switch(sklTargetType) {
                                    case AURA:
                                    case FRONT_AURA:
                                    case BEHIND_AURA:
                                    case GROUND:
                                    case SELF:
                                    case AURA_CORPSE_MOB:
                                    case AURA_UNDEAD_ENEMY:
                                    case PARTY:
                                    case CLAN:
                                    case AREA_SUMMON:
                                    case UNLOCKABLE:
                                       break;
                                    case COMMAND_CHANNEL:
                                    case PARTY_CLAN:
                                    case PARTY_NOTME:
                                    case CORPSE_CLAN:
                                    case PET:
                                    case SERVITOR:
                                    case SUMMON:
                                    default:
                                       this.sendActionFailed();
                                       return false;
                                 }
                              }

                              if (dontMove) {
                                 if (sklTargetType == TargetType.GROUND) {
                                    if (!this.isInsideRadius(
                                       worldPosition.getX(),
                                       worldPosition.getY(),
                                       worldPosition.getZ(),
                                       (int)((double)skill.getCastRange() + this.getColRadius()),
                                       false,
                                       false
                                    )) {
                                       if (msg) {
                                          this.sendPacket(SystemMessageId.TARGET_TOO_FAR);
                                       }

                                       this.sendActionFailed();
                                       return false;
                                    }
                                 } else if (skill.getCastRange() > 0
                                    && !this.isInsideRadius(target, (int)((double)skill.getCastRange() + this.getColRadius()), false, false)) {
                                    if (msg) {
                                       this.sendPacket(SystemMessageId.TARGET_TOO_FAR);
                                    }

                                    this.sendActionFailed();
                                    return false;
                                 }
                              }
                        }
                     }

                     if (skill.hasEffectType(EffectType.TELEPORT_TO_TARGET)) {
                        if (this.isMovementDisabled()) {
                           if (msg) {
                              SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
                              sm.addSkillName(skill.getId());
                              this.sendPacket(sm);
                           }

                           this.sendActionFailed();
                           return false;
                        }

                        if (this.isInsideZone(ZoneId.PEACE) && !this.isInFightEvent()) {
                           if (msg) {
                              this.sendPacket(SystemMessageId.TARGET_IN_PEACEZONE);
                           }

                           this.sendActionFailed();
                           return false;
                        }
                     }

                     if (!skill.isOffensive() && target.isMonster() && !forceUse && !skill.isNeutral()) {
                        switch(sklTargetType) {
                           case AURA:
                           case FRONT_AURA:
                           case BEHIND_AURA:
                           case GROUND:
                           case SELF:
                           case AURA_CORPSE_MOB:
                           case AURA_UNDEAD_ENEMY:
                           case PARTY:
                           case CLAN:
                           case PARTY_CLAN:
                           case PARTY_NOTME:
                           case CORPSE_CLAN:
                           case PET:
                           case SERVITOR:
                           case SUMMON:
                           case CORPSE_MOB:
                           case AREA_CORPSE_MOB:
                              break;
                           case COMMAND_CHANNEL:
                           case AREA_SUMMON:
                           case UNLOCKABLE:
                           default:
                              switch(sklType) {
                                 case UNLOCK:
                                 case DELUXE_KEY_UNLOCK:
                                    break;
                                 default:
                                    this.sendActionFailed();
                                    return false;
                              }
                        }
                     }

                     if (!skill.isOffensive() && !forceUse && (target.isPlayer() || target.isSummon()) && !this.isFriend(target.getActingPlayer())) {
                        this.sendActionFailed();
                        return false;
                     } else {
                        switch(sklTargetType) {
                           case AURA:
                           case FRONT_AURA:
                           case BEHIND_AURA:
                           case GROUND:
                           case SELF:
                           case AURA_UNDEAD_ENEMY:
                           case PARTY:
                           case CLAN:
                           case PARTY_CLAN:
                           case PARTY_NOTME:
                           case CORPSE_CLAN:
                           case AREA_SUMMON:
                              break;
                           case AURA_CORPSE_MOB:
                           case COMMAND_CHANNEL:
                           default:
                              if (!this.checkPvpSkill(target, skill) && !this.getAccessLevel().allowPeaceAttack()) {
                                 if (msg) {
                                    this.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
                                 }

                                 this.sendActionFailed();
                                 return false;
                              }
                        }

                        if ((
                              sklTargetType != TargetType.HOLY
                                 || this.checkIfOkToCastSealOfRule(CastleManager.getInstance().getCastle(this), false, skill, target)
                           )
                           && (
                              sklTargetType != TargetType.FLAGPOLE
                                 || this.checkIfOkToCastFlagDisplay(FortManager.getInstance().getFort(this), false, skill, target)
                           )
                           && (sklType != SkillType.SIEGEFLAG || SkillSiegeFlag.checkIfOkToPlaceFlag(this, false, skill.getId() == 844))) {
                           if (skill.getCastRange() > 0 && !skill.isDisableGeoCheck()) {
                              if (sklTargetType == TargetType.GROUND) {
                                 if (!GeoEngine.canSeeTarget(this, worldPosition)) {
                                    if (msg) {
                                       this.sendPacket(SystemMessageId.CANT_SEE_TARGET);
                                    }

                                    this.sendActionFailed();
                                    return false;
                                 }
                              } else if (!GeoEngine.canSeeTarget(this, target, false)) {
                                 if (msg) {
                                    this.sendPacket(SystemMessageId.CANT_SEE_TARGET);
                                 }

                                 this.sendActionFailed();
                                 return false;
                              }
                           }

                           return true;
                        } else {
                           this.sendActionFailed();
                           this.abortCast();
                           return false;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public boolean checkIfOkToCastSealOfRule(Castle castle, boolean isCheckOnly, Skill skill, GameObject target) {
      SystemMessage sm;
      if (castle != null && castle.getId() > 0) {
         if (!castle.getArtefacts().contains(target)) {
            sm = SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET);
         } else if (!castle.getSiege().getIsInProgress()) {
            sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
            sm.addSkillName(skill);
         } else if (!Util.checkIfInRange(200, this, target, true)) {
            sm = SystemMessage.getSystemMessage(SystemMessageId.DIST_TOO_FAR_CASTING_STOPPED);
         } else {
            if (castle.getSiege().getAttackerClan(this.getClan()) != null) {
               if (!isCheckOnly) {
                  sm = SystemMessage.getSystemMessage(SystemMessageId.OPPONENT_STARTED_ENGRAVING);
                  castle.getSiege().announceToPlayer(sm, false);
               }

               return true;
            }

            sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
            sm.addSkillName(skill);
         }
      } else {
         sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
         sm.addSkillName(skill);
      }

      this.sendPacket(sm);
      return false;
   }

   public boolean checkIfOkToCastFlagDisplay(Fort fort, boolean isCheckOnly, Skill skill, GameObject target) {
      SystemMessage sm;
      if (fort != null && fort.getId() > 0) {
         if (fort.getFlagPole() != target) {
            sm = SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET);
         } else if (!fort.getSiege().getIsInProgress()) {
            sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
            sm.addSkillName(skill);
         } else if (!Util.checkIfInRange(200, this, target, true)) {
            sm = SystemMessage.getSystemMessage(SystemMessageId.DIST_TOO_FAR_CASTING_STOPPED);
         } else {
            if (fort.getSiege().getAttackerClan(this.getClan()) != null) {
               if (!isCheckOnly) {
                  fort.getSiege().announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.S1_TRYING_RAISE_FLAG), this.getClan().getName());
               }

               return true;
            }

            sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
            sm.addSkillName(skill);
         }
      } else {
         sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
         sm.addSkillName(skill);
      }

      this.sendPacket(sm);
      return false;
   }

   public boolean isInLooterParty(int LooterId) {
      Player looter = World.getInstance().getPlayer(LooterId);
      if (this.isInParty() && this.getParty().isInCommandChannel() && looter != null) {
         return this.getParty().getCommandChannel().getMembers().contains(looter);
      } else {
         return this.isInParty() && looter != null ? this.getParty().getMembers().contains(looter) : false;
      }
   }

   public boolean checkPvpSkill(GameObject target, Skill skill) {
      return this.checkPvpSkill(target, skill, false);
   }

   public boolean checkPvpSkill(GameObject target, Skill skill, boolean srcIsSummon) {
      Player targetPlayer = target != null ? target.getActingPlayer() : null;
      if (skill.isDebuff()) {
         if (this == targetPlayer) {
            return false;
         }

         if (targetPlayer != null) {
            if (targetPlayer.isInsideZone(ZoneId.PEACE)) {
               return false;
            }

            if (this.isFriend(targetPlayer)) {
               return false;
            }

            if (this.isPKProtected(targetPlayer)) {
               return false;
            }
         }
      }

      if (!(target instanceof EventChestInstance)
         && targetPlayer != null
         && target != this
         && (!this.isInDuel() || targetPlayer.getDuelId() != this.getDuelId())
         && !this.isInsideZone(ZoneId.PVP)
         && !targetPlayer.isInsideZone(ZoneId.PVP)) {
         SkillUseHolder skilldat = this.getCurrentSkill();
         SkillUseHolder skilldatpet = this.getCurrentPetSkill();
         boolean isNotMyTarget = skill.getTargetType() == TargetType.AURA || skill.getTargetType() != TargetType.ONE && this.getTarget() != targetPlayer;
         if ((skilldat == null || skilldat.isCtrlPressed() || !skill.isOffensive() || srcIsSummon)
            && (skilldatpet == null || skilldatpet.isCtrlPressed() || !skill.isOffensive() || !srcIsSummon)) {
            if (skilldat != null && skilldat.isCtrlPressed() && skill.isOffensive() && !srcIsSummon && isNotMyTarget
               || skilldatpet != null && skilldatpet.isCtrlPressed() && skill.isOffensive() && srcIsSummon && isNotMyTarget) {
               if (this.getClan() != null
                  && targetPlayer.getClan() != null
                  && this.getClan().isAtWarWith(targetPlayer.getClan().getId())
                  && targetPlayer.getClan().isAtWarWith(this.getClan().getId())) {
                  return true;
               }

               if (targetPlayer.getPvpFlag() == 0 && targetPlayer.getKarma() == 0) {
                  return false;
               }

               if (this.isPKProtected(targetPlayer)) {
                  return false;
               }
            } else if (skilldat == null && skill.isOffensive() && !srcIsSummon) {
               if (this.getClan() != null
                  && targetPlayer.getClan() != null
                  && this.getClan().isAtWarWith(targetPlayer.getClan().getId())
                  && targetPlayer.getClan().isAtWarWith(this.getClan().getId())) {
                  return true;
               }

               if (targetPlayer.getPvpFlag() == 0 && targetPlayer.getKarma() == 0) {
                  return false;
               }

               if (this.isPKProtected(targetPlayer)) {
                  return false;
               }
            }
         } else {
            if (this.getClan() != null
               && targetPlayer.getClan() != null
               && this.getClan().isAtWarWith(targetPlayer.getClan().getId())
               && targetPlayer.getClan().isAtWarWith(this.getClan().getId())) {
               return true;
            }

            if (targetPlayer.getPvpFlag() == 0 && targetPlayer.getKarma() == 0) {
               return false;
            }

            if (this.isPKProtected(targetPlayer)) {
               return false;
            }
         }
      } else if (targetPlayer != null
         && target != this
         && (!this.isInDuel() || targetPlayer.getDuelId() != this.getDuelId())
         && this.isInsideZone(ZoneId.FUN_PVP)
         && targetPlayer.isInsideZone(ZoneId.FUN_PVP)) {
         SkillUseHolder skilldat = this.getCurrentSkill();
         SkillUseHolder skilldatpet = this.getCurrentPetSkill();
         boolean isNotMyTarget = skill.getTargetType() == TargetType.AURA || skill.getTargetType() != TargetType.ONE && this.getTarget() != targetPlayer;
         if ((skilldat == null || skilldat.isCtrlPressed() || !skill.isOffensive() || srcIsSummon)
            && (skilldatpet == null || skilldatpet.isCtrlPressed() || !skill.isOffensive() || !srcIsSummon)) {
            if ((
                  skilldat != null && skilldat.isCtrlPressed() && skill.isOffensive() && !srcIsSummon && isNotMyTarget
                     || skilldatpet != null && skilldatpet.isCtrlPressed() && skill.isOffensive() && srcIsSummon && isNotMyTarget
               )
               && this.isFriend(targetPlayer)
               && skill.getTargetType() == TargetType.AURA) {
               return false;
            }
         } else if (this.isFriend(targetPlayer)) {
            return false;
         }
      }

      return true;
   }

   public boolean isMageClass() {
      return this.getClassId().isMage();
   }

   public boolean isMounted() {
      return this._mountType != MountType.NONE;
   }

   public boolean checkLandingState() {
      if (this.isInsideZone(ZoneId.NO_LANDING)) {
         return true;
      } else {
         return this.isInsideZone(ZoneId.SIEGE)
            && (
               this.getClan() == null
                  || CastleManager.getInstance().getCastle(this) != CastleManager.getInstance().getCastleByOwner(this.getClan())
                  || this != this.getClan().getLeader().getPlayerInstance()
            );
      }
   }

   public void setMount(int npcId, int npcLevel) {
      MountType type = MountType.findByNpcId(npcId);
      switch(type) {
         case NONE:
            this.setIsFlying(false);
            break;
         case STRIDER:
            if (this.isNoble()) {
               this.addSkill(SkillsParser.FrequentSkill.STRIDER_SIEGE_ASSAULT.getSkill(), false);
            }
            break;
         case WYVERN:
            this.setIsFlying(true);
      }

      this._mountType = type;
      this._mountNpcId = npcId;
      this._mountLevel = npcLevel;
   }

   public MountType getMountType() {
      return this._mountType;
   }

   @Override
   public final void stopAllEffects() {
      super.stopAllEffects();
      this.updateAndBroadcastStatus(2);
   }

   @Override
   public final void stopAllEffectsExceptThoseThatLastThroughDeath() {
      super.stopAllEffectsExceptThoseThatLastThroughDeath();
      this.updateAndBroadcastStatus(2);
   }

   public final void stopAllEffectsNotStayOnSubclassChange() {
      for(Effect effect : this._effects.getAllEffects()) {
         if (effect != null && !effect.getSkill().isStayOnSubclassChange()) {
            effect.exit(true, true);
         }
      }

      this.updateAndBroadcastStatus(2);
   }

   public final void stopAllToggles() {
      this._effects.stopAllToggles();
   }

   public final void stopCubics() {
      if (!this._cubics.isEmpty()) {
         for(CubicInstance cubic : this._cubics.values()) {
            cubic.stopAction();
            cubic.cancelDisappear();
         }

         this._cubics.clear();
         this.broadcastUserInfo(true);
      }
   }

   public final void stopCubicsByOthers() {
      if (!this._cubics.isEmpty()) {
         boolean broadcast = false;

         for(CubicInstance cubic : this._cubics.values()) {
            if (cubic.givenByOther()) {
               cubic.stopAction();
               cubic.cancelDisappear();
               this._cubics.remove(cubic.getId());
               broadcast = true;
            }
         }

         if (broadcast) {
            this.broadcastUserInfo(true);
         }
      }
   }

   @Override
   public void updateAbnormalEffect() {
      try {
         if (this._effectsUpdateTask != null) {
            return;
         }

         this._effectsUpdateTask = ThreadPoolManager.getInstance().schedule(new Player.UpdateAbnormalEffectTask(), Config.USER_ABNORMAL_EFFECTS_INTERVAL);
      } catch (Exception var2) {
      }
   }

   public void setInventoryBlockingStatus(boolean val) {
      this._inventoryDisable = val;
      if (val) {
         ThreadPoolManager.getInstance().schedule(new InventoryEnableTask(this), 1500L);
      }
   }

   public boolean isInventoryDisabled() {
      return this._inventoryDisable;
   }

   public Map<Integer, CubicInstance> getCubics() {
      return this._cubics;
   }

   public void addCubic(int id, int level, double cubicPower, int cubicDelay, int cubicSkillChance, int cubicMaxCount, int cubicDuration, boolean givenByOther) {
      this._cubics.put(id, new CubicInstance(this, id, level, (int)cubicPower, cubicDelay, cubicSkillChance, cubicMaxCount, cubicDuration, givenByOther));
   }

   public CubicInstance getCubicById(int id) {
      for(CubicInstance c : this._cubics.values()) {
         if (c.getId() == id) {
            return c;
         }
      }

      return null;
   }

   public int getEnchantEffect() {
      ItemInstance wpn = this.getActiveWeaponInstance();
      return wpn == null ? 0 : Math.min(127, wpn.getEnchantLevel());
   }

   public void setLastFolkNPC(Npc folkNpc) {
      this._lastFolkNpc = folkNpc;
   }

   public Npc getLastFolkNPC() {
      return this._lastFolkNpc;
   }

   public boolean isFestivalParticipant() {
      return SevenSignsFestival.getInstance().isParticipant(this);
   }

   public void addAutoSoulShot(int itemId) {
      this._activeSoulShots.add(itemId);
   }

   public boolean removeAutoSoulShot(int itemId) {
      return this._activeSoulShots.remove(itemId);
   }

   public Set<Integer> getAutoSoulShot() {
      return this._activeSoulShots;
   }

   @Override
   public void rechargeShots(boolean physical, boolean magic) {
      if (this._activeSoulShots != null && !this._activeSoulShots.isEmpty()) {
         for(int itemId : this._activeSoulShots) {
            ItemInstance item = this.getInventory().getItemByItemId(itemId);
            if (item != null) {
               if (magic) {
                  if (item.getItem().getDefaultAction() == ActionType.fishingshot) {
                     IItemHandler handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
                     if (handler != null) {
                        handler.useItem(this, item, false);
                     }
                  } else if (item.getItem().getDefaultAction() == ActionType.spiritshot) {
                     IItemHandler handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
                     if (handler != null) {
                        handler.useItem(this, item, false);
                     }
                  }
               }

               if (physical && item.getItem().getDefaultAction() == ActionType.soulshot) {
                  IItemHandler handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
                  if (handler != null) {
                     handler.useItem(this, item, false);
                  }
               }
            } else {
               this.removeAutoSoulShot(itemId);
            }
         }
      }
   }

   public boolean haveAutoShot(int itemId) {
      return this._activeSoulShots.contains(itemId);
   }

   public void disableAutoShotsAll() {
      for(int itemId : this._activeSoulShots) {
         this.sendPacket(new ExAutoSoulShot(itemId, 0));
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED);
         sm.addItemName(itemId);
         this.sendPacket(sm);
      }

      this._activeSoulShots.clear();
   }

   public int getClanPrivileges() {
      return this._clanPrivileges;
   }

   public void setClanPrivileges(int n) {
      this._clanPrivileges = n;
   }

   public void setPledgeClass(int classId) {
      this._pledgeClass = classId;
      this.checkItemRestriction();
   }

   public int getPledgeClass() {
      return this._pledgeClass;
   }

   public void setPledgeType(int typeId) {
      this._pledgeType = typeId;
   }

   public int getPledgeType() {
      return this._pledgeType;
   }

   public int getApprentice() {
      return this._apprentice;
   }

   public void setApprentice(int apprentice_id) {
      this._apprentice = apprentice_id;
   }

   public int getSponsor() {
      return this._sponsor;
   }

   public void setSponsor(int sponsor_id) {
      this._sponsor = sponsor_id;
   }

   public int getBookMarkSlot() {
      return this._bookmarkslot;
   }

   public void setBookMarkSlot(int slot) {
      this._bookmarkslot = slot;
      this.sendPacket(new ExGetBookMarkInfo(this));
   }

   @Override
   public void sendMessage(String message) {
      this.sendPacket(SystemMessage.sendString(message));
   }

   public void enterObserverMode(int x, int y, int z) {
      this._lastX = this.getX();
      this._lastY = this.getY();
      this._lastZ = this.getZ();
      this.stopEffects(EffectType.HIDE);
      this._observerMode = true;
      this.setTarget(null);
      this.setIsParalyzed(true);
      this.startParalyze();
      this.setIsInvul(true);
      this.setInvisible(true);
      this.sendPacket(new ObserverStart(x, y, z));
      this.teleToLocation(x, y, z, false);
      this.broadcastCharInfo();
   }

   public void setLastCords(int x, int y, int z) {
      this._lastX = this.getX();
      this._lastY = this.getY();
      this._lastZ = this.getZ();
   }

   public void enterOlympiadObserverMode(Location loc, int id) {
      if (this.hasSummon()) {
         this.getSummon().unSummon(this);
      }

      this.stopEffects(EffectType.HIDE);
      if (!this._cubics.isEmpty()) {
         for(CubicInstance cubic : this._cubics.values()) {
            cubic.stopAction();
            cubic.cancelDisappear();
         }

         this._cubics.clear();
      }

      if (this.getParty() != null) {
         this.getParty().removePartyMember(this, Party.messageType.Expelled);
      }

      this._olympiadGameId = id;
      if (this.isSitting()) {
         this.standUp();
      }

      if (!this._observerMode) {
         this._lastX = this.getX();
         this._lastY = this.getY();
         this._lastZ = this.getZ();
      }

      this._observerMode = true;
      this.setTarget(null);
      this.setIsInvul(true);
      this.setInvisible(true);
      this.teleToLocation(loc, false);
      this.sendPacket(new ExOlympiadMode(3));
      this.broadcastCharInfo();
   }

   public void leaveObserverMode() {
      this.setTarget(null);
      this.teleToLocation(this._lastX, this._lastY, this._lastZ, false);
      this.setLastCords(0, 0, 0);
      this.sendPacket(new ObserverEnd(this));
      this.setIsParalyzed(false);
      if (!this.isGM()) {
         this.setInvisible(false);
         this.setIsInvul(false);
      }

      if (this.hasAI()) {
         this.getAI().setIntention(CtrlIntention.IDLE);
      }

      this.setFalling();
      this._observerMode = false;
      this.broadcastCharInfo();
   }

   public void leaveOlympiadObserverMode() {
      if (this._olympiadGameId != -1) {
         this._olympiadGameId = -1;
         this._observerMode = false;
         this.setTarget(null);
         this.sendPacket(new ExOlympiadMode(0));
         this.setReflectionId(0);
         this.teleToLocation(this._lastX, this._lastY, this._lastZ, true);
         if (!this.isGM()) {
            this.setInvisible(false);
            this.setIsInvul(false);
         }

         if (this.hasAI()) {
            this.getAI().setIntention(CtrlIntention.IDLE);
         }

         this.setLastCords(0, 0, 0);
         this.broadcastUserInfo(true);
      }
   }

   public void setOlympiadSide(int i) {
      this._olympiadSide = i;
   }

   public int getOlympiadSide() {
      return this._olympiadSide;
   }

   public void setOlympiadGameId(int id) {
      this._olympiadGameId = id;
   }

   public int getOlympiadGameId() {
      return this._olympiadGameId;
   }

   public void setOlympiadGame(AbstractOlympiadGame game) {
      this._olympiadGame = game;
   }

   public AbstractOlympiadGame getOlympiadGame() {
      return this._olympiadGame;
   }

   public int getLastX() {
      return this._lastX;
   }

   public int getLastY() {
      return this._lastY;
   }

   public int getLastZ() {
      return this._lastZ;
   }

   public boolean inObserverMode() {
      return this._observerMode;
   }

   public int getTeleMode() {
      return this._telemode;
   }

   public void setTeleMode(int mode) {
      this._telemode = mode;
   }

   public void setLoto(int i, int val) {
      this._loto[i] = val;
   }

   public int getLoto(int i) {
      return this._loto[i];
   }

   public void setRace(int i, int val) {
      this._race[i] = val;
   }

   public int getRace(int i) {
      return this._race[i];
   }

   public boolean getMessageRefusal() {
      return this._messageRefusal;
   }

   public void setMessageRefusal(boolean mode) {
      this._messageRefusal = mode;
      this.sendPacket(new EtcStatusUpdate(this));
   }

   public void setDietMode(boolean mode) {
      this._dietMode = mode;
   }

   public boolean getDietMode() {
      return this._dietMode;
   }

   public void setExchangeRefusal(boolean mode) {
      this._exchangeRefusal = mode;
   }

   public boolean getExchangeRefusal() {
      return this._exchangeRefusal;
   }

   public BlockedList getBlockList() {
      return this._blockList;
   }

   public void setHero(boolean hero, boolean giveSkills) {
      if (hero && this._baseClass == this._activeClass) {
         if (giveSkills) {
            for(Skill skill : SkillTreesParser.getInstance().getHeroSkillTree().values()) {
               this.addSkill(skill, false);
            }
         }
      } else if (giveSkills) {
         for(Skill skill : SkillTreesParser.getInstance().getHeroSkillTree().values()) {
            this.removeSkill(skill, false, true);
         }
      }

      this._hero = hero;
      if (hero && !giveSkills) {
         this._timeHero = true;
      } else {
         this._timeHero = false;
      }

      this.sendSkillList(false);
   }

   public void setIsInOlympiadMode(boolean b) {
      this._inOlympiadMode = b;
   }

   public void setIsOlympiadStart(boolean b) {
      this._OlympiadStart = b;
   }

   public boolean isOlympiadStart() {
      return this._OlympiadStart;
   }

   public boolean isHero() {
      return this._hero;
   }

   public boolean isTimeHero() {
      return this._timeHero;
   }

   public boolean isInOlympiadMode() {
      return this._inOlympiadMode;
   }

   public boolean isInDuel() {
      return this._isInDuel;
   }

   public int getDuelId() {
      return this._duelId;
   }

   public void setDuelState(int mode) {
      this._duelState = mode;
   }

   public int getDuelState() {
      return this._duelState;
   }

   public void setIsInDuel(int duelId) {
      if (duelId > 0) {
         this._isInDuel = true;
         this._duelState = 1;
         this._duelId = duelId;
      } else {
         if (this._duelState == 2) {
            this.enableAllSkills();
            this.getStatus().startHpMpRegeneration();
         }

         this._isInDuel = false;
         this._duelState = 0;
         this._duelId = 0;
      }
   }

   public SystemMessage getNoDuelReason() {
      SystemMessage sm = SystemMessage.getSystemMessage(this._noDuelReason);
      sm.addPcName(this);
      this._noDuelReason = SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL;
      return sm;
   }

   public boolean canDuel() {
      if (!this.isInCombat() && !this.isJailed()) {
         if (this.isDead() || this.isAlikeDead() || this.getCurrentHp() < this.getMaxHp() / 2.0 || this.getCurrentMp() < this.getMaxMp() / 2.0) {
            this._noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_HP_OR_MP_IS_BELOW_50_PERCENT;
            return false;
         } else if (this.isInDuel()) {
            this._noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_ALREADY_ENGAGED_IN_A_DUEL;
            return false;
         } else if (this.isInOlympiadMode() || OlympiadManager.getInstance().isRegistered(this)) {
            this._noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_PARTICIPATING_IN_THE_OLYMPIAD;
            return false;
         } else if (this.isCursedWeaponEquipped()) {
            this._noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_IN_A_CHAOTIC_STATE;
            return false;
         } else if (this.getPrivateStoreType() != 0) {
            this._noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_ENGAGED_IN_A_PRIVATE_STORE_OR_MANUFACTURE;
            return false;
         } else if (this.isMounted() || this.isInBoat()) {
            this._noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_RIDING_A_BOAT_STEED_OR_STRIDER;
            return false;
         } else if (this.isFishing()) {
            this._noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_FISHING;
            return false;
         } else if (!this.isInsideZone(ZoneId.PVP) && !this.isInsideZone(ZoneId.PEACE) && !this.isInsideZone(ZoneId.SIEGE)) {
            return true;
         } else {
            this._noDuelReason = SystemMessageId.C1_CANNOT_MAKE_A_CHALLANGE_TO_A_DUEL_BECAUSE_C1_IS_CURRENTLY_IN_A_DUEL_PROHIBITED_AREA;
            return false;
         }
      } else {
         this._noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_ENGAGED_IN_BATTLE;
         return false;
      }
   }

   public boolean isNoble() {
      return this._noble;
   }

   public void setNoble(boolean val) {
      if (Config.ENABLE_NOBLESS_COLOR) {
         this.getAppearance().setNameColor(Config.NOBLESS_COLOR_NAME);
      }

      if (Config.ENABLE_NOBLESS_TITLE_COLOR) {
         this.getAppearance().setTitleColor(Config.NOBLESS_COLOR_TITLE_NAME);
      }

      Collection<Skill> nobleSkillTree = SkillTreesParser.getInstance().getNobleSkillTree().values();
      if (val) {
         this.broadcastPacket(new MagicSkillUse(this, this, 6673, 1, 1000, 0));

         for(Skill skill : nobleSkillTree) {
            this.addSkill(skill, false);
         }
      } else {
         for(Skill skill : nobleSkillTree) {
            this.removeSkill(skill, false, true);
         }
      }

      this._noble = val;
      this.sendSkillList(false);
   }

   public void setLvlJoinedAcademy(int lvl) {
      this._lvlJoinedAcademy = lvl;
   }

   public int getLvlJoinedAcademy() {
      return this._lvlJoinedAcademy;
   }

   public boolean isAcademyMember() {
      return this._lvlJoinedAcademy > 0;
   }

   @Override
   public void setTeam(int team) {
      super.setTeam(team);
      this.broadcastUserInfo(true);
      if (this.hasSummon()) {
         this.getSummon().broadcastStatusUpdate();
      }
   }

   public void setWantsPeace(int wantsPeace) {
      this._wantsPeace = wantsPeace;
   }

   public int getWantsPeace() {
      return this._wantsPeace;
   }

   public boolean isFishing() {
      return this._fishing;
   }

   public void setFishing(boolean fishing) {
      this._fishing = fishing;
   }

   public void sendSkillList(boolean coolTime) {
      this.sendSkillList(this, coolTime);
   }

   public void sendSkillList(Player player, boolean coolTime) {
      boolean isDisabled = false;
      SkillList sl = new SkillList();
      if (player != null) {
         for(Skill s : player.getAllSkills()) {
            if (s != null && (this._transformation == null || this.hasTransformSkill(s.getId()) || s.allowOnTransform())) {
               if (player.getClan() != null) {
                  if (s.isClanSkill() && player.getClan().getReputationScore() < 0) {
                     boolean var10 = true;
                  } else {
                     boolean var10000 = false;
                  }
               }

               isDisabled = !s.isClanSkill() && player.isSkillBlocked(s);
               boolean isEnchantable = SkillsParser.getInstance().isEnchantable(s.getId());
               if (isEnchantable) {
                  EnchantSkillLearn esl = EnchantSkillGroupsParser.getInstance().getSkillEnchantmentBySkillId(s.getId());
                  if (esl != null) {
                     if (s.getLevel() < esl.getBaseLevel()) {
                        isEnchantable = false;
                     }
                  } else {
                     isEnchantable = false;
                  }
               }

               sl.addSkill(s.getDisplayId(), s.getDisplayLevel(), s.isPassive(), isDisabled, isEnchantable);
            }
         }
      }

      this.sendPacket(sl);
      if (coolTime) {
         this.sendPacket(new SkillCoolTime(this));
      }
   }

   public boolean addSubClass(int classId, int classIndex) {
      if (!this._subclassLock.tryLock()) {
         return false;
      } else {
         boolean subTemplate;
         try {
            if (this.getTotalSubClasses() == Config.MAX_SUBCLASS || classIndex == 0) {
               return false;
            }

            if (!this.getSubClasses().containsKey(classIndex)) {
               if (CharacterDAO.getInstance().addSubClass(this, classId, classIndex)) {
                  ClassId subTemplate = ClassId.getClassId(classId);
                  Map<Integer, SkillLearn> skillTree = SkillTreesParser.getInstance().getCompleteClassSkillTree(subTemplate);
                  Map<Integer, Skill> prevSkillList = new HashMap<>();

                  for(SkillLearn skillInfo : skillTree.values()) {
                     if (skillInfo.getGetLevel() <= 40) {
                        Skill prevSkill = prevSkillList.get(skillInfo.getId());
                        Skill newSkill = SkillsParser.getInstance().getInfo(skillInfo.getId(), skillInfo.getLvl());
                        if (prevSkill == null || prevSkill.getLevel() <= newSkill.getLevel()) {
                           prevSkillList.put(newSkill.getId(), newSkill);
                           CharacterSkillsDAO.getInstance().store(this, newSkill, prevSkill, classIndex);
                        }
                     }
                  }
               }

               return true;
            }

            subTemplate = false;
         } finally {
            this._subclassLock.unlock();
         }

         return subTemplate;
      }
   }

   public boolean modifySubClass(int classIndex, int newClassId) {
      if (!this._subclassLock.tryLock()) {
         return false;
      } else {
         try {
            if (CharacterDAO.getInstance().modifySubClass(this, classIndex, newClassId)) {
               this.getSubClasses().remove(classIndex);
            }
         } finally {
            this._subclassLock.unlock();
         }

         return this.addSubClass(newClassId, classIndex);
      }
   }

   public boolean isSubClassActive() {
      return this._classIndex > 0;
   }

   public Map<Integer, SubClass> getSubClasses() {
      if (this._subClasses == null) {
         this._subClasses = new ConcurrentSkipListMap<>();
      }

      return this._subClasses;
   }

   public int getTotalSubClasses() {
      return this.getSubClasses().size();
   }

   public int getBaseClass() {
      return this._baseClass;
   }

   public void setActiveClassId(int activeClass) {
      this._activeClass = activeClass;
   }

   public int getActiveClass() {
      return this._activeClass;
   }

   public void setClassIndex(int classIndex) {
      this._classIndex = classIndex;
   }

   public int getClassIndex() {
      return this._classIndex;
   }

   private void setClassTemplate(int classId) {
      this._activeClass = classId;
      PcTemplate pcTemplate = CharTemplateParser.getInstance().getTemplate(classId);
      if (pcTemplate == null) {
         _log.severe("Missing template for classId: " + classId);
         throw new Error();
      } else {
         this.setTemplate(pcTemplate);
         TransferSkillUtils.checkTransferItems(this);
      }
   }

   public boolean setActiveClass(int classIndex) {
      if (!this._subclassLock.tryLock()) {
         return false;
      } else {
         try {
            if (this._transformation != null) {
               return false;
            } else {
               for(ItemInstance item : this.getInventory().getAugmentedItems()) {
                  if (item != null && item.isEquipped()) {
                     item.getAugmentation().removeBonus(this);
                  }
               }

               this.abortCast();

               for(Creature character : World.getInstance().getAroundCharacters(this)) {
                  if (character.getFusionSkill() != null && character.getFusionSkill().getTarget() == this) {
                     character.abortCast();
                  }
               }

               if (this._sellingBuffs != null) {
                  this._sellingBuffs.clear();
               }

               this.store(Config.SUBCLASS_STORE_SKILL_COOLTIME);
               this._reuseTimeStampsSkills.clear();
               this._charges.set(0);
               this.stopChargeTask();
               if (this.hasServitor()) {
                  this.getSummon().unSummon(this);
               }

               if (classIndex == 0) {
                  this.setClassTemplate(this.getBaseClass());
               } else {
                  try {
                     this.setClassTemplate(this.getSubClasses().get(classIndex).getClassId());
                  } catch (Exception var9) {
                     _log.log(
                        Level.WARNING,
                        "Could not switch " + this.getName() + "'s sub class to class index " + classIndex + ": " + var9.getMessage(),
                        (Throwable)var9
                     );
                     return false;
                  }
               }

               this._classIndex = classIndex;
               this.setLearningClass(this.getClassId());
               if (this.isInParty()) {
                  this.getParty().recalculatePartyLevel();
               }

               for(Skill oldSkill : this.getAllSkills()) {
                  this.removeSkill(oldSkill, false, true);
               }

               this.stopAllEffectsExceptThoseThatLastThroughDeath();
               this.stopAllEffectsNotStayOnSubclassChange();
               this.stopCubics();
               this.restoreRecipeBook(false);
               this.restoreDeathPenaltyBuffLevel();
               CharacterSkillsDAO.getInstance().restoreSkills(this);
               this.rewardSkills();
               this.regiveTemporarySkills();
               this.resetDisabledSkills();
               this.restoreEffects();
               this.updateEffectIcons();
               this.sendPacket(new EtcStatusUpdate(this));
               QuestState st = this.getQuestState("_422_RepentYourSins");
               if (st != null) {
                  st.exitQuest(true);
               }

               for(int i = 0; i < 3; ++i) {
                  this._henna[i] = null;
               }

               this.restoreHenna();
               this.sendPacket(new HennaInfo(this));
               if (this.getCurrentHp() > this.getMaxHp()) {
                  this.setCurrentHp(this.getMaxHp());
               }

               if (this.getCurrentMp() > this.getMaxMp()) {
                  this.setCurrentMp(this.getMaxMp());
               }

               if (this.getCurrentCp() > this.getMaxCp()) {
                  this.setCurrentCp(this.getMaxCp());
               }

               this.refreshOverloaded();
               this.refreshExpertisePenalty();
               this.setExpBeforeDeath(0L);
               this._shortCuts.restoreMe();
               this.sendPacket(new ShortCutInit(this));
               this.broadcastPacket(new SocialAction(this.getObjectId(), 2122));
               this.sendPacket(new SkillCoolTime(this));
               this.sendPacket(new ExStorageMaxCount(this));
               this.broadcastUserInfo(true);
               if (this.hasPet()) {
                  PetInstance pet = (PetInstance)this.getSummon();
                  if (pet != null && pet.getPetData().isSynchLevel() && pet.getLevel() != this.getLevel()) {
                     pet.getStat().setLevel(this.getStat().getLevel());
                     pet.getStat().getExpForLevel(this.getStat().getLevel());
                     pet.setCurrentHp(pet.getMaxHp());
                     pet.setCurrentMp(pet.getMaxMp());
                     pet.broadcastPacket(new SocialAction(this.getObjectId(), 2122));
                     pet.updateAndBroadcastStatus(1);
                  }
               }

               return true;
            }
         } finally {
            this._subclassLock.unlock();
         }
      }
   }

   public boolean isLocked() {
      return this._subclassLock.isLocked();
   }

   public void stopWarnUserTakeBreak() {
      try {
         if (this._taskWarnUserTakeBreak != null) {
            this._taskWarnUserTakeBreak.cancel(true);
            this._taskWarnUserTakeBreak = null;
         }
      } catch (Exception var2) {
      }
   }

   public void startWarnUserTakeBreak() {
      if (this._taskWarnUserTakeBreak == null) {
         this._taskWarnUserTakeBreak = ThreadPoolManager.getInstance().scheduleAtFixedRate(new WarnUserTakeBreakTask(this), 7200000L, 7200000L);
      }
   }

   public void stopRentPet() {
      if (this._taskRentPet != null) {
         if (this.checkLandingState() && this.getMountType() == MountType.WYVERN) {
            this.teleToLocation(TeleportWhereType.TOWN, true);
         }

         if (this.dismount()) {
            this._taskRentPet.cancel(true);
            this._taskRentPet = null;
         }
      }
   }

   public void startRentPet(int seconds) {
      if (this._taskRentPet == null) {
         this._taskRentPet = ThreadPoolManager.getInstance().scheduleAtFixedRate(new RentPetTask(this), (long)seconds * 1000L, (long)seconds * 1000L);
      }
   }

   public boolean isRentedPet() {
      return this._taskRentPet != null;
   }

   public void stopWaterTask() {
      try {
         if (this._taskWater != null) {
            this._taskWater.cancel(false);
            this._taskWater = null;
            this.sendPacket(new SetupGauge(this, 2, 0));
         }
      } catch (Exception var2) {
      }
   }

   public void startWaterTask() {
      if (!this.isDead() && this._taskWater == null) {
         int timeinwater = (int)this.calcStat(Stats.BREATH, 60000.0, this, null);
         this.sendPacket(new SetupGauge(this, 2, timeinwater));
         this._taskWater = ThreadPoolManager.getInstance().scheduleAtFixedRate(new WaterTask(this), (long)timeinwater, 1000L);
      }
   }

   public boolean isInWater() {
      return this._taskWater != null;
   }

   public void checkWaterState() {
      if (this.isInWater(this)) {
         WaterZone waterZone = ZoneManager.getInstance().getZone(this, WaterZone.class);
         if (waterZone != null && waterZone.canUseWaterTask()) {
            this.startWaterTask();
         }
      } else {
         this.stopWaterTask();
      }
   }

   public void onPlayerEnter() {
      this.startWarnUserTakeBreak();
      if (!SevenSigns.getInstance().isSealValidationPeriod() && !SevenSigns.getInstance().isCompResultsPeriod()) {
         if (!this.isGM() && this.isIn7sDungeon() && SevenSigns.getInstance().getPlayerCabal(this.getObjectId()) == 0) {
            this.teleToLocation(TeleportWhereType.TOWN, true);
            this.setIsIn7sDungeon(false);
            this.sendMessage("You have been teleported to the nearest town because you have not signed for any cabal.");
         }
      } else if (!this.isGM()
         && this.isIn7sDungeon()
         && SevenSigns.getInstance().getPlayerCabal(this.getObjectId()) != SevenSigns.getInstance().getCabalHighestScore()) {
         this.teleToLocation(TeleportWhereType.TOWN, true);
         this.setIsIn7sDungeon(false);
         this.sendMessage("You have been teleported to the nearest town due to the beginning of the Seal Validation period.");
      }

      if (this.isGM()) {
         if (this.isInvul()) {
            this.sendMessage("Entering world in Invulnerable mode.");
         }

         if (this.isInvisible()) {
            this.sendMessage("Entering world in Invisible mode.");
         }

         if (this.isSilenceMode()) {
            this.sendMessage("Entering world in Silence mode.");
         }
      }

      if (Config.STORE_SKILL_COOLTIME) {
         this.restoreEffects();
      }

      this.revalidateZone(true);
      this.notifyFriends();
      if (!this.canOverrideCond(PcCondOverride.SKILL_CONDITIONS) && Config.DECREASE_SKILL_LEVEL) {
         this.checkPlayerSkills();
      }

      this.getEvents().onPlayerLogin();
      if (this.isJailed() && !this.isInsideZone(ZoneId.JAIL)) {
         this.startJail();
      } else if (!this.isJailed() && this.isInsideZone(ZoneId.JAIL) && !this.isGM()) {
         this.stopJail();
      }

      try {
         List<ZoneType> zones = ZoneManager.getInstance().getZones(this);
         if (zones != null && !zones.isEmpty()) {
            for(ZoneType zone : zones) {
               if (zone != null) {
                  zone.onPlayerLoginInside(this);
               }
            }
         }
      } catch (Exception var4) {
         _log.log(Level.SEVERE, "", (Throwable)var4);
      }
   }

   public void setLastAccess(long access) {
      this._lastAccess = access;
   }

   public long getLastAccess() {
      return this._lastAccess;
   }

   @Override
   public void doRevive() {
      super.doRevive();
      this.stopEffects(EffectType.CHARMOFCOURAGE);
      this.updateEffectIcons();
      this.sendPacket(new EtcStatusUpdate(this));
      if (this.isMounted()) {
         this.startFeed(this._mountNpcId);
      }

      if (this.isInParty()
         && this.getParty().isInDimensionalRift()
         && !DimensionalRiftManager.getInstance().checkIfInPeaceZone(this.getX(), this.getY(), this.getZ())) {
         this.getParty().getDimensionalRift().memberRessurected(this);
      }

      if (this.getReflectionId() > 0) {
         Reflection instance = ReflectionManager.getInstance().getReflection(this.getReflectionId());
         if (instance != null) {
            instance.cancelEjectDeadPlayer(this);
         }
      }
   }

   @Override
   public void setName(String value) {
      super.setName(value);
      if (Config.CACHE_CHAR_NAMES) {
         CharNameHolder.getInstance().addName(this);
      }
   }

   @Override
   public void doRevive(double revivePower) {
      this.restoreExp(revivePower);
      this.doRevive();
   }

   public void reviveRequest(Player reviver, Skill skill, boolean Pet) {
      if (!this.isResurrectionBlocked()) {
         if (this._reviveRequested == 1) {
            if (this._revivePet == Pet) {
               reviver.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED);
            } else if (Pet) {
               reviver.sendPacket(SystemMessageId.CANNOT_RES_PET2);
            } else {
               reviver.sendPacket(SystemMessageId.MASTER_CANNOT_RES);
            }
         } else {
            if (Pet && this.hasSummon() && this.getSummon().isDead() || !Pet && this.isDead()) {
               this._reviveRequested = 1;
               int restoreExp = 0;
               if (this.isInFightEvent()) {
                  this._revivePower = 100.0;
               } else if (this.isPhoenixBlessed()) {
                  this._revivePower = 100.0;
               } else if (this.isAffected(EffectFlag.CHARM_OF_COURAGE)) {
                  this._revivePower = 0.0;
               } else {
                  this._revivePower = Formulas.calculateSkillResurrectRestorePercent(skill != null ? skill.getPower() : 0.0, reviver);
               }

               restoreExp = (int)Math.round((double)(this.getExpBeforeDeath() - this.getExp()) * this._revivePower / 100.0);
               this._revivePet = Pet;
               if (this.isAffected(EffectFlag.CHARM_OF_COURAGE)) {
                  ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.RESURRECT_USING_CHARM_OF_COURAGE.getId());
                  dlg.addTime(60000);
                  this.sendPacket(dlg);
                  return;
               }

               ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.RESSURECTION_REQUEST_BY_C1_FOR_S2_XP.getId());
               dlg.addPcName(reviver);
               dlg.addString(String.valueOf(Math.abs(restoreExp)));
               this.sendPacket(dlg);
            }
         }
      }
   }

   public void reviveAnswer(int answer) {
      if (this._reviveRequested == 1 && (this.isDead() || this._revivePet) && (!this._revivePet || !this.hasSummon() || this.getSummon().isDead())) {
         if (answer == 0 && this.isPhoenixBlessed()) {
            this.stopEffects(EffectType.PHOENIX_BLESSING);
            this.stopAllEffectsExceptThoseThatLastThroughDeath();
         }

         if (answer == 1) {
            if (!this._revivePet) {
               if (this._revivePower != 0.0) {
                  this.doRevive(this._revivePower);
               } else {
                  this.doRevive();
               }
            } else if (this.hasSummon()) {
               if (this._revivePower != 0.0) {
                  this.getSummon().doRevive(this._revivePower);
               } else {
                  this.getSummon().doRevive();
               }
            }
         }

         this._reviveRequested = 0;
         this._revivePower = 0.0;
      } else {
         this._reviveRequested = 0;
      }
   }

   public boolean isReviveRequested() {
      return this._reviveRequested == 1;
   }

   public boolean isRevivingPet() {
      return this._revivePet;
   }

   public void removeReviving() {
      this._reviveRequested = 0;
      this._revivePower = 0.0;
   }

   public void onActionRequest() {
      if (this.isSpawnProtected() && !this.isRegisteredInFightEvent()) {
         this.sendPacket(SystemMessageId.YOU_ARE_NO_LONGER_PROTECTED_FROM_AGGRESSIVE_MONSTERS);
      }

      if (this.isTeleportProtected()) {
         this.sendMessage("Teleport spawn protection ended.");
      }

      this.isntAfk();
      this.setProtection(false);
      this.setTeleportProtection(false);
   }

   public int getExpertiseLevel() {
      int level = this.getSkillLevel(239);
      if (level < 0) {
         level = 0;
      }

      return level;
   }

   @Override
   public void teleToLocation(int x, int y, int z, int heading, boolean allowRandomOffset) {
      if (this.isInVehicle() && !this.getVehicle().isTeleporting()) {
         this.setVehicle(null);
      }

      if (this.isFlyingMounted() && z < -1005) {
         z = -1005;
      }

      super.teleToLocation(x, y, z, heading, allowRandomOffset);
   }

   @Override
   public final void onTeleported() {
      super.onTeleported();
      if (this.isInAirShip()) {
         this.getAirShip().sendInfo(this);
      }

      this.revalidateZone(true);
      this.checkItemRestriction();
      if (Config.PLAYER_TELEPORT_PROTECTION > 0 && !this.isInOlympiadMode()) {
         this.setTeleportProtection(true);
      }

      if (this.getTrainedBeasts() != null) {
         for(TamedBeastInstance tamedBeast : this.getTrainedBeasts()) {
            tamedBeast.deleteMe();
         }

         this.getTrainedBeasts().clear();
      }

      if (this.hasSummon()) {
         this.getSummon().setFollowStatus(false);
         this.getSummon().teleToLocation(this.getX(), this.getY(), this.getZ(), false);
         ((SummonAI)this.getSummon().getAI()).setStartFollowController(true);
         this.getSummon().setFollowStatus(true);
         this.getSummon().updateAndBroadcastStatus(0);
      }
   }

   @Override
   public void setIsTeleporting(boolean teleport) {
      this.setIsTeleporting(teleport, true);
   }

   public void setIsTeleporting(boolean teleport, boolean useWatchDog) {
      super.setIsTeleporting(teleport);
      if (useWatchDog) {
         if (teleport) {
            if (this._teleportWatchdog == null && Config.TELEPORT_WATCHDOG_TIMEOUT > 0) {
               synchronized(this) {
                  if (this._teleportWatchdog == null) {
                     this._teleportWatchdog = ThreadPoolManager.getInstance()
                        .schedule(new TeleportWatchdogTask(this), (long)(Config.TELEPORT_WATCHDOG_TIMEOUT * 1000));
                  }
               }
            }
         } else if (this._teleportWatchdog != null) {
            this._teleportWatchdog.cancel(false);
            this._teleportWatchdog = null;
         }
      }
   }

   @Override
   public void addExpAndSp(long addToExp, int addToSp) {
      if (this.getExpOn()) {
         if (addToExp > 0L) {
            this.getCounters().addAchivementInfo("expAcquired", 0, addToExp, false, false, false);
         }

         if (addToSp > 0) {
            this.getCounters().addAchivementInfo("spAcquired", 0, (long)addToSp, false, false, false);
         }

         this.getStat().addExpAndSp(addToExp, addToSp, false);
      } else {
         this.getStat().addExpAndSp(0L, addToSp, false);
      }
   }

   public void addExpAndSp(long addToExp, int addToSp, boolean useVitality) {
      if (this.getExpOn()) {
         if (addToExp > 0L) {
            this.getCounters().addAchivementInfo("expAcquired", 0, addToExp, false, false, false);
         }

         if (addToSp > 0) {
            this.getCounters().addAchivementInfo("spAcquired", 0, (long)addToSp, false, false, false);
         }

         this.getStat().addExpAndSp(addToExp, addToSp, useVitality);
      } else {
         this.getStat().addExpAndSp(0L, addToSp, useVitality);
      }
   }

   public void removeExpAndSp(long removeExp, int removeSp) {
      this.getStat().removeExpAndSp(removeExp, removeSp, true);
   }

   public void removeExpAndSp(long removeExp, int removeSp, boolean sendMessage) {
      this.getStat().removeExpAndSp(removeExp, removeSp, sendMessage);
   }

   @Override
   public void reduceCurrentHp(double value, Creature attacker, boolean awake, boolean isDOT, Skill skill) {
      if (attacker != null && attacker.isPlayer() && attacker.getActingPlayer().isInFightEvent()) {
         attacker.getActingPlayer().getFightEvent().onDamage(attacker, this, value);
      }

      if (skill != null) {
         this.getStatus().reduceHp(value, attacker, awake, isDOT, skill.isToggle(), skill.getDmgDirectlyToHP());
      } else {
         this.getStatus().reduceHp(value, attacker, awake, isDOT, false, false);
      }

      if (this.getTrainedBeasts() != null) {
         for(TamedBeastInstance tamedBeast : this.getTrainedBeasts()) {
            tamedBeast.onOwnerGotAttacked(attacker);
         }
      }
   }

   public void broadcastSnoop(int type, String name, String _text) {
      if (!this._snoopListener.isEmpty()) {
         Snoop sn = new Snoop(this.getObjectId(), this.getName(), type, name, _text);

         for(Player pci : this._snoopListener) {
            if (pci != null) {
               pci.sendPacket(sn);
            }
         }
      }
   }

   public void addSnooper(Player pci) {
      if (!this._snoopListener.contains(pci)) {
         this._snoopListener.add(pci);
      }
   }

   public void removeSnooper(Player pci) {
      this._snoopListener.remove(pci);
   }

   public void addSnooped(Player pci) {
      if (!this._snoopedPlayer.contains(pci)) {
         this._snoopedPlayer.add(pci);
      }
   }

   public void removeSnooped(Player pci) {
      this._snoopedPlayer.remove(pci);
   }

   public boolean validateItemManipulation(int objectId, String action) {
      ItemInstance item = this.getInventory().getItemByObjectId(objectId);
      if (item != null && item.getOwnerId() == this.getObjectId()) {
         if ((!this.hasSummon() || this.getSummon().getControlObjectId() != objectId) && this.getMountObjectID() != objectId) {
            if (this.getActiveEnchantItemId() == objectId) {
               if (Config.DEBUG) {
                  _log.finest(this.getObjectId() + ":player tried to " + action + " an enchant scroll he was using");
               }

               return false;
            } else {
               return !CursedWeaponsManager.getInstance().isCursed(item.getId());
            }
         } else {
            if (Config.DEBUG) {
               _log.finest(this.getObjectId() + ": player tried to " + action + " item controling pet");
            }

            return false;
         }
      } else {
         _log.finest(this.getObjectId() + ": player tried to " + action + " item he is not owner of");
         return false;
      }
   }

   public boolean isInBoat() {
      return this._vehicle != null && this._vehicle.isBoat();
   }

   public BoatInstance getBoat() {
      return (BoatInstance)this._vehicle;
   }

   public boolean isInAirShip() {
      return this._vehicle != null && this._vehicle.isAirShip();
   }

   public AirShipInstance getAirShip() {
      return (AirShipInstance)this._vehicle;
   }

   public Vehicle getVehicle() {
      return this._vehicle;
   }

   public void setVehicle(Vehicle v) {
      if (v == null && this._vehicle != null) {
         this._vehicle.removePassenger(this);
      }

      this._vehicle = v;
   }

   @Override
   public boolean isInVehicle() {
      return this._vehicle != null;
   }

   public void setInCrystallize(boolean inCrystallize) {
      this._inCrystallize = inCrystallize;
   }

   public boolean isInCrystallize() {
      return this._inCrystallize;
   }

   public Location getInVehiclePosition() {
      return this._inVehiclePosition;
   }

   public void setInVehiclePosition(Location loc) {
      this._inVehiclePosition = loc;
   }

   public void setIsInKrateisCube(boolean choice) {
      this._isInKrateisCube = choice;
   }

   public boolean getIsInKrateisCube() {
      return this._isInKrateisCube;
   }

   @Override
   public void deleteMe() {
      if (!this.isInOfflineMode() && this.getPrivateStoreType() == 1 && this.isSitting()) {
         for(TradeItem item : this.getSellList().getItems()) {
            AuctionsManager.getInstance().removeStore(this, item.getAuctionId());
         }
      }

      this.cleanup();
      this.store();
      super.deleteMe();
   }

   private synchronized void cleanup() {
      this.getEvents().onPlayerLogout();
      this.getFarmSystem().stopFarmTask(false);
      this.cleanAutoPots();
      OnlineRewardManager.getInstance().activePlayerDisconnect(this);
      if (this.isInSearchOfAcademy()) {
         this.setSearchforAcademy(false);
         AcademyList.deleteFromAcdemyList(this);
      }

      if (Config.ALLOW_VIP_SYSTEM) {
         this.setVar("vipLevel", String.valueOf(this.getVipLevel()));
         this.setVar("vipPoints", String.valueOf(this.getVipPoints()));
      }

      if (Config.ALLOW_REVENGE_SYSTEM) {
         this.setVar("revengeList", this.saveRevergeList());
      }

      this._bannedActions.clear();
      if (Config.AUTO_POINTS_SYSTEM) {
         try {
            if (this._hpPotionTask != null) {
               this._hpPotionTask.cancel(false);
            }

            this._hpPotionTask = null;
            if (this._mpPotionTask != null) {
               this._mpPotionTask.cancel(false);
            }

            this._mpPotionTask = null;
            if (this._cpPotionTask != null) {
               this._cpPotionTask.cancel(false);
            }

            this._cpPotionTask = null;
            if (this._soulPotionTask != null) {
               this._soulPotionTask.cancel(false);
            }

            this._soulPotionTask = null;
         } catch (Exception var32) {
         }
      }

      try {
         List<ZoneType> zones = ZoneManager.getInstance().getZones(this);
         if (zones != null && !zones.isEmpty()) {
            for(ZoneType zone : zones) {
               if (zone != null) {
                  zone.onPlayerLogoutInside(this);
               }
            }
         }
      } catch (Exception var36) {
         _log.log(Level.SEVERE, "deleteMe()", (Throwable)var36);
      }

      try {
         this.setOnlineStatus(false, true);
      } catch (Exception var31) {
         _log.log(Level.SEVERE, "deleteMe()", (Throwable)var31);
      }

      try {
         if (Config.ENABLE_BLOCK_CHECKER_EVENT && this.getBlockCheckerArena() != -1) {
            HandysBlockCheckerManager.getInstance().onDisconnect(this);
         }
      } catch (Exception var30) {
         _log.log(Level.SEVERE, "deleteMe()", (Throwable)var30);
      }

      try {
         this._isOnline = false;
         this.abortAttack();
         this.abortCast();
         this.stopMove(null);
         this.setDebug(null);
      } catch (Exception var29) {
         _log.log(Level.SEVERE, "deleteMe()", (Throwable)var29);
      }

      try {
         if (this.getInventory().getItemByItemId(9819) != null && !this.isInFightEvent()) {
            Fort fort = FortManager.getInstance().getFort(this);
            if (fort != null) {
               FortSiegeManager.getInstance().dropCombatFlag(this, fort.getId());
            } else {
               int slot = this.getInventory().getSlotFromItem(this.getInventory().getItemByItemId(9819));
               this.getInventory().unEquipItemInBodySlot(slot);
               this.destroyItem("CombatFlag", this.getInventory().getItemByItemId(9819), null, true);
            }
         } else if (this.isCombatFlagEquipped() && !this.isInFightEvent()) {
            TerritoryWarManager.getInstance().dropCombatFlag(this, false, false);
         }
      } catch (Exception var35) {
         _log.log(Level.SEVERE, "deleteMe()", (Throwable)var35);
      }

      try {
         if (this.isFlying()) {
            this.removeSkill(SkillsParser.getInstance().getInfo(4289, 1));
         }
      } catch (Exception var28) {
         _log.log(Level.SEVERE, "deleteMe()", (Throwable)var28);
      }

      if (this.hasPremiumBonus() && this.getPremiumBonus().isOnlineType()) {
         try {
            if (this.getPremiumBonus().isPersonal()) {
               CharacterPremiumDAO.getInstance().updateOnlineTimePersonal(this);
            } else {
               CharacterPremiumDAO.getInstance().updateOnlineTime(this);
            }
         } catch (Exception var27) {
            _log.log(Level.SEVERE, "deleteMe()", (Throwable)var27);
         }
      }

      try {
         this.stopAllTimers();
      } catch (Exception var26) {
         _log.log(Level.SEVERE, "deleteMe()", (Throwable)var26);
      }

      try {
         this.setIsTeleporting(false);
      } catch (Exception var25) {
         _log.log(Level.SEVERE, "deleteMe()", (Throwable)var25);
      }

      try {
         RecipeController.getInstance().requestMakeItemAbort(this);
      } catch (Exception var24) {
         _log.log(Level.SEVERE, "deleteMe()", (Throwable)var24);
      }

      try {
         this.setTarget(null);
      } catch (Exception var23) {
         _log.log(Level.SEVERE, "deleteMe()", (Throwable)var23);
      }

      try {
         if (this._fusionSkill != null) {
            this.abortCast();
         }

         for(Creature character : World.getInstance().getAroundCharacters(this)) {
            if (character.getFusionSkill() != null && character.getFusionSkill().getTarget() == this) {
               character.abortCast();
            }
         }
      } catch (Exception var34) {
         _log.log(Level.SEVERE, "deleteMe()", (Throwable)var34);
      }

      try {
         for(Effect effect : this.getAllEffects()) {
            if (effect.getSkill().isToggle()) {
               effect.exit();
            } else {
               switch(effect.getEffectType()) {
                  case SIGNET_GROUND:
                  case SIGNET_EFFECT:
                     effect.exit();
               }
            }
         }
      } catch (Exception var33) {
         _log.log(Level.SEVERE, "deleteMe()", (Throwable)var33);
      }

      try {
         this.decayMe();
      } catch (Exception var22) {
         _log.log(Level.SEVERE, "deleteMe()", (Throwable)var22);
      }

      if (this.isInParty()) {
         try {
            this.leaveParty();
         } catch (Exception var21) {
            _log.log(Level.SEVERE, "deleteMe()", (Throwable)var21);
         }
      }

      if (OlympiadManager.getInstance().isRegistered(this) || this.getOlympiadGameId() != -1) {
         OlympiadManager.getInstance().removeDisconnectedCompetitor(this);
      }

      if (this.hasSummon()) {
         try {
            this.getSummon().setRestoreSummon(true);
            this.getSummon().unSummon(this);
            if (this.hasSummon()) {
               this.getSummon().broadcastNpcInfo(0);
            }
         } catch (Exception var20) {
            _log.log(Level.SEVERE, "deleteMe()", (Throwable)var20);
         }
      }

      if (this.getClan() != null) {
         try {
            ClanMember clanMember = this.getClan().getClanMember(this.getObjectId());
            if (clanMember != null) {
               clanMember.setPlayerInstance(null);
            }
         } catch (Exception var19) {
            _log.log(Level.SEVERE, "deleteMe()", (Throwable)var19);
         }
      }

      if (this.getActiveRequester() != null) {
         this.setActiveRequester(null);
         this.cancelActiveTrade();
      }

      if (this.isGM()) {
         try {
            AdminParser.getInstance().deleteGm(this);
         } catch (Exception var18) {
            _log.log(Level.SEVERE, "deleteMe()", (Throwable)var18);
         }
      }

      try {
         if (this.inObserverMode()) {
            this.setXYZInvisible(this._lastX, this._lastY, this._lastZ);
         }

         if (this.isInVehicle()) {
            this.getVehicle().oustPlayer(this);
         }
      } catch (Exception var17) {
         _log.log(Level.SEVERE, "deleteMe()", (Throwable)var17);
      }

      try {
         int instanceId = this.getReflectionId();
         if (instanceId != 0 && !Config.RESTORE_PLAYER_INSTANCE) {
            Reflection inst = ReflectionManager.getInstance().getReflection(instanceId);
            if (inst != null) {
               inst.removePlayer(this.getObjectId());
               Effect effect = this.getFirstEffect(8239);
               if (effect != null) {
                  effect.exit();
               }

               Location loc = inst.getReturnLoc();
               if (loc != null) {
                  int x = loc.getX() + Rnd.get(-30, 30);
                  int y = loc.getY() + Rnd.get(-30, 30);
                  this.setXYZInvisible(x, y, loc.getZ());
                  if (this.hasSummon()) {
                     this.getSummon().setReflectionId(0);
                     this.getSummon().teleToLocation(loc, true);
                  }
               }
            }
         }
      } catch (Exception var16) {
         _log.log(Level.SEVERE, "deleteMe()", (Throwable)var16);
      }

      try {
         if (this.getFightEventGameRoom() != null) {
            FightEventManager.getInstance().unsignFromAllEvents(this);
         }
      } catch (Exception var15) {
         _log.log(Level.SEVERE, "deleteMe()", (Throwable)var15);
      }

      try {
         if (this.isInFightEvent()) {
            this.getFightEvent().loggedOut(this);
         }
      } catch (Exception var14) {
         _log.log(Level.SEVERE, "deleteMe()", (Throwable)var14);
      }

      try {
         AerialCleftEvent.getInstance().onLogout(this);
      } catch (Exception var13) {
         _log.log(Level.SEVERE, "deleteMe()", (Throwable)var13);
      }

      try {
         this.getInventory().deleteMe();
      } catch (Exception var12) {
         _log.log(Level.SEVERE, "deleteMe()", (Throwable)var12);
      }

      try {
         this.clearWarehouse();
      } catch (Exception var11) {
         _log.log(Level.SEVERE, "deleteMe()", (Throwable)var11);
      }

      if (Config.WAREHOUSE_CACHE) {
         WarehouseCache.getInstance().remCacheTask(this);
      }

      try {
         this.getFreight().deleteMe();
      } catch (Exception var10) {
         _log.log(Level.SEVERE, "deleteMe()", (Throwable)var10);
      }

      try {
         this.clearRefund();
      } catch (Exception var9) {
         _log.log(Level.SEVERE, "deleteMe()", (Throwable)var9);
      }

      if (this.isCursedWeaponEquipped()) {
         try {
            CursedWeaponsManager.getInstance().getCursedWeapon(this._cursedWeaponEquippedId).setPlayer(null);
         } catch (Exception var8) {
            _log.log(Level.SEVERE, "deleteMe()", (Throwable)var8);
         }
      }

      if (this.getClanId() > 0) {
         this.getClan().broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(this), this);
      }

      MatchingRoom room = this.getMatchingRoom();
      if (room != null) {
         if (room.getLeader() == this) {
            room.disband();
         } else {
            room.removeMember(this, false);
         }
      }

      this.setMatchingRoom(null);
      MatchingRoomManager.getInstance().removeFromWaitingList(this);

      for(Player player : this._snoopedPlayer) {
         player.removeSnooper(this);
      }

      for(Player player : this._snoopListener) {
         player.removeSnooped(this);
      }

      World.getInstance().removeFromAllPlayers(this);

      try {
         this.notifyFriends();
         this.getBlockList().playerLogout();
      } catch (Exception var7) {
         _log.log(Level.WARNING, "Exception on deleteMe() notifyFriends: " + var7.getMessage(), (Throwable)var7);
      }
   }

   public void startFishing(int _x, int _y, int _z, boolean isHotSpring) {
      this.stopMove(null);
      this.setIsImmobilized(true);
      this._fishing = true;
      this._fishx = _x;
      this._fishy = _y;
      this._fishz = _z;
      int lvl = this.GetRandomFishLvl();
      int group = this.GetRandomGroup();
      int type = this.GetRandomFishType(group);
      List<Fish> fishs = FishParser.getInstance().getFish(lvl, type, group);
      if (fishs != null && !fishs.isEmpty()) {
         int check = Rnd.get(fishs.size());
         this._fish = fishs.get(check).clone();
         if (isHotSpring && this._lure.getId() == 8548 && this.getSkillLevel(1315) > 19 && Rnd.nextBoolean()) {
            this._fish = new Fish(271, 8547, "Old Box", 10, 20, 100, 618, 1185, 0, 10, 40, 20, 30, 3, 618, 0, 1);
         }

         fishs.clear();
         List<Fish> var10 = null;
         this.sendPacket(SystemMessageId.CAST_LINE_AND_START_FISHING);
         if (!GameTimeController.getInstance().isNight() && this._lure.isNightLure()) {
            this._fish.setFishGroup(-1);
         }

         this.broadcastPacket(new ExFishingStart(this, this._fish.getFishGroup(), _x, _y, _z, this._lure.isNightLure()));
         this.sendPacket(new PlaySound(1, "SF_P_01", 0, 0, 0, 0, 0));
         this.startLookingForFishTask();
      } else {
         this.sendMessage("Error - Fishes are not definied");
         this.endFishing(false);
      }
   }

   public void stopLookingForFishTask() {
      try {
         if (this._taskforfish != null) {
            this._taskforfish.cancel(false);
            this._taskforfish = null;
         }
      } catch (Exception var2) {
      }
   }

   public void startLookingForFishTask() {
      if (!this.isDead() && this._taskforfish == null) {
         int checkDelay = 0;
         boolean isNoob = false;
         boolean isUpperGrade = false;
         if (this._lure != null) {
            int lureid = this._lure.getId();
            isNoob = this._fish.getFishGrade() == 0;
            isUpperGrade = this._fish.getFishGrade() == 2;
            if (lureid != 6519 && lureid != 6522 && lureid != 6525 && lureid != 8505 && lureid != 8508 && lureid != 8511) {
               if (lureid != 6520
                  && lureid != 6523
                  && lureid != 6526
                  && (lureid < 8505 || lureid > 8513)
                  && (lureid < 7610 || lureid > 7613)
                  && (lureid < 7807 || lureid > 7809)
                  && (lureid < 8484 || lureid > 8486)) {
                  if (lureid == 6521 || lureid == 6524 || lureid == 6527 || lureid == 8507 || lureid == 8510 || lureid == 8513 || lureid == 8548) {
                     checkDelay = Math.round((float)((double)this._fish.getGutsCheckTime() * 0.66));
                  }
               } else {
                  checkDelay = Math.round((float)((double)this._fish.getGutsCheckTime() * 1.0));
               }
            } else {
               checkDelay = Math.round((float)((double)this._fish.getGutsCheckTime() * 1.33));
            }
         }

         this._taskforfish = ThreadPoolManager.getInstance()
            .scheduleAtFixedRate(
               new LookingForFishTask(this, this._fish.getStartCombatTime(), this._fish.getFishGuts(), this._fish.getFishGroup(), isNoob, isUpperGrade),
               10000L,
               (long)checkDelay
            );
      }
   }

   private int GetRandomGroup() {
      switch(this._lure.getId()) {
         case 7807:
         case 7808:
         case 7809:
         case 8486:
            return 0;
         case 8485:
         case 8506:
         case 8509:
         case 8512:
            return 2;
         default:
            return 1;
      }
   }

   private int GetRandomFishType(int group) {
      int check = Rnd.get(100);
      int type = 1;
      switch(group) {
         case 0:
            switch(this._lure.getId()) {
               case 7807:
                  if (check <= 54) {
                     return 5;
                  } else {
                     byte typex;
                     if (check <= 77) {
                        typex = 4;
                     } else {
                        typex = 6;
                     }

                     return typex;
                  }
               case 7808:
                  if (check <= 54) {
                     return 4;
                  } else {
                     byte typex;
                     if (check <= 77) {
                        typex = 6;
                     } else {
                        typex = 5;
                     }

                     return typex;
                  }
               case 7809:
                  if (check <= 54) {
                     return 6;
                  } else {
                     byte typex;
                     if (check <= 77) {
                        typex = 5;
                     } else {
                        typex = 4;
                     }

                     return typex;
                  }
               case 8486:
                  if (check <= 33) {
                     return 4;
                  } else {
                     byte typex;
                     if (check <= 66) {
                        typex = 5;
                     } else {
                        typex = 6;
                     }

                     return typex;
                  }
               default:
                  return type;
            }
         case 1:
            switch(this._lure.getId()) {
               case 6519:
               case 6520:
               case 6521:
               case 8505:
               case 8507:
                  if (check <= 54) {
                     return 1;
                  } else if (check <= 74) {
                     return 0;
                  } else {
                     byte typex;
                     if (check <= 94) {
                        typex = 2;
                     } else {
                        typex = 3;
                     }

                     return typex;
                  }
               case 6522:
               case 6523:
               case 6524:
               case 8508:
               case 8510:
                  if (check <= 54) {
                     return 0;
                  } else if (check <= 74) {
                     return 1;
                  } else {
                     byte typex;
                     if (check <= 94) {
                        typex = 2;
                     } else {
                        typex = 3;
                     }

                     return typex;
                  }
               case 6525:
               case 6526:
               case 6527:
               case 8511:
               case 8513:
                  if (check <= 55) {
                     return 2;
                  } else if (check <= 74) {
                     return 1;
                  } else {
                     byte typex;
                     if (check <= 94) {
                        typex = 0;
                     } else {
                        typex = 3;
                     }

                     return typex;
                  }
               case 7610:
               case 7611:
               case 7612:
               case 7613:
                  return 3;
               case 8484:
                  if (check <= 33) {
                     return 0;
                  } else {
                     byte typex;
                     if (check <= 66) {
                        typex = 1;
                     } else {
                        typex = 2;
                     }

                     return typex;
                  }
               default:
                  return type;
            }
         case 2:
            switch(this._lure.getId()) {
               case 8485:
                  if (check <= 33) {
                     type = 7;
                  } else if (check <= 66) {
                     type = 8;
                  } else {
                     type = 9;
                  }
                  break;
               case 8506:
                  if (check <= 54) {
                     type = 8;
                  } else if (check <= 77) {
                     type = 7;
                  } else {
                     type = 9;
                  }
                  break;
               case 8509:
                  if (check <= 54) {
                     type = 7;
                  } else if (check <= 77) {
                     type = 9;
                  } else {
                     type = 8;
                  }
                  break;
               case 8512:
                  if (check <= 54) {
                     type = 9;
                  } else if (check <= 77) {
                     type = 8;
                  } else {
                     type = 7;
                  }
            }
      }

      return type;
   }

   private int GetRandomFishLvl() {
      int skilllvl = this.getSkillLevel(1315);
      Effect e = this.getFirstEffect(2274);
      if (e != null) {
         skilllvl = (int)e.getSkill().getPower();
      }

      if (skilllvl <= 0) {
         return 1;
      } else {
         int check = Rnd.get(100);
         int randomlvl;
         if (check <= 50) {
            randomlvl = skilllvl;
         } else if (check <= 85) {
            randomlvl = skilllvl - 1;
            if (randomlvl <= 0) {
               randomlvl = 1;
            }
         } else {
            randomlvl = skilllvl + 1;
            if (randomlvl > 27) {
               randomlvl = 27;
            }
         }

         return randomlvl;
      }
   }

   public void startFishCombat(boolean isNoob, boolean isUpperGrade) {
      this._fishCombat = new Fishing(this, this._fish, isNoob, isUpperGrade);
   }

   public void endFishing(boolean win) {
      this._fishing = false;
      this._fishx = 0;
      this._fishy = 0;
      this._fishz = 0;
      if (this._fishCombat == null) {
         this.sendPacket(SystemMessageId.BAIT_LOST_FISH_GOT_AWAY);
      }

      this._fishCombat = null;
      this._lure = null;
      this.broadcastPacket(new ExFishingEnd(win, this));
      this.sendPacket(SystemMessageId.REEL_LINE_AND_STOP_FISHING);
      this.setIsImmobilized(false);
      this.stopLookingForFishTask();
   }

   public Fishing getFishCombat() {
      return this._fishCombat;
   }

   public int getFishx() {
      return this._fishx;
   }

   public int getFishy() {
      return this._fishy;
   }

   public int getFishz() {
      return this._fishz;
   }

   public void setLure(ItemInstance lure) {
      this._lure = lure;
   }

   public ItemInstance getLure() {
      return this._lure;
   }

   public int getInventoryLimit() {
      int ivlim;
      if (this.isGM()) {
         ivlim = Config.INVENTORY_MAXIMUM_GM;
      } else if (this.getRace() == Race.Dwarf) {
         ivlim = Config.INVENTORY_MAXIMUM_DWARF;
      } else {
         ivlim = Config.INVENTORY_MAXIMUM_NO_DWARF;
      }

      ivlim += (int)this.getStat().calcStat(Stats.INV_LIM, 0.0, null, null);
      ivlim += this.getVarInt("expandInventory", 0);
      return Math.min(ivlim, Config.EXPAND_INVENTORY_LIMIT);
   }

   public int getWareHouseLimit() {
      int whlim;
      if (this.getRace() == Race.Dwarf) {
         whlim = Config.WAREHOUSE_SLOTS_DWARF;
      } else {
         whlim = Config.WAREHOUSE_SLOTS_NO_DWARF;
      }

      whlim += (int)this.getStat().calcStat(Stats.WH_LIM, 0.0, null, null);
      whlim += this.getVarInt("expandWareHouse", 0);
      return Math.min(whlim, Config.EXPAND_WAREHOUSE_LIMIT);
   }

   public int getPrivateSellStoreLimit() {
      int pslim;
      if (this.getRace() == Race.Dwarf) {
         pslim = Config.MAX_PVTSTORESELL_SLOTS_DWARF;
      } else {
         pslim = Config.MAX_PVTSTORESELL_SLOTS_OTHER;
      }

      pslim += (int)this.getStat().calcStat(Stats.P_SELL_LIM, 0.0, null, null);
      pslim += this.getVarInt("expandSellStore", 0);
      return Math.min(pslim, Config.EXPAND_SELLSTORE_LIMIT);
   }

   public int getPrivateBuyStoreLimit() {
      int pblim;
      if (this.getRace() == Race.Dwarf) {
         pblim = Config.MAX_PVTSTOREBUY_SLOTS_DWARF;
      } else {
         pblim = Config.MAX_PVTSTOREBUY_SLOTS_OTHER;
      }

      pblim += (int)this.getStat().calcStat(Stats.P_BUY_LIM, 0.0, null, null);
      pblim += this.getVarInt("expandBuyStore", 0);
      return Math.min(pblim, Config.EXPAND_BUYSTORE_LIMIT);
   }

   public int getDwarfRecipeLimit() {
      int recdlim = Config.DWARF_RECIPE_SLOTS;
      recdlim += (int)this.getStat().calcStat(Stats.REC_D_LIM, 0.0, null, null);
      recdlim += this.getVarInt("expandDwarfRecipe", 0);
      return Math.min(recdlim, Config.EXPAND_DWARFRECIPE_LIMIT);
   }

   public int getCommonRecipeLimit() {
      int recclim = Config.COMMON_RECIPE_SLOTS;
      recclim += (int)this.getStat().calcStat(Stats.REC_C_LIM, 0.0, null, null);
      recclim += this.getVarInt("expandCommonRecipe", 0);
      return Math.min(recclim, Config.EXPAND_COMMONRECIPE_LIMIT);
   }

   public int getMountNpcId() {
      return this._mountNpcId;
   }

   public int getMountLevel() {
      return this._mountLevel;
   }

   public void setMountObjectID(int newID) {
      this._mountObjectID = newID;
   }

   public int getMountObjectID() {
      return this._mountObjectID;
   }

   public SkillUseHolder getCurrentSkill() {
      return this._currentSkill;
   }

   public void setCurrentSkill(Skill currentSkill, boolean ctrlPressed, boolean shiftPressed) {
      if (currentSkill == null) {
         this._currentSkill = null;
      } else {
         this._currentSkill = new SkillUseHolder(currentSkill, ctrlPressed, shiftPressed);
      }
   }

   public SkillUseHolder getCurrentPetSkill() {
      return this._currentPetSkill;
   }

   public void setCurrentPetSkill(Skill currentSkill, boolean ctrlPressed, boolean shiftPressed) {
      if (currentSkill == null) {
         this._currentPetSkill = null;
      } else {
         this._currentPetSkill = new SkillUseHolder(currentSkill, ctrlPressed, shiftPressed);
      }
   }

   public SkillUseHolder getQueuedSkill() {
      return this._queuedSkill;
   }

   public void setQueuedSkill(Skill queuedSkill, boolean ctrlPressed, boolean shiftPressed) {
      if (queuedSkill == null) {
         this._queuedSkill = null;
      } else {
         this._queuedSkill = new SkillUseHolder(queuedSkill, ctrlPressed, shiftPressed);
      }
   }

   public boolean isJailed() {
      return PunishmentManager.getInstance().checkPunishment(this.getClient(), PunishmentType.JAIL);
   }

   public boolean isChatBanned() {
      return PunishmentManager.getInstance().checkPunishment(this.getClient(), PunishmentType.CHAT_BAN);
   }

   public void startFameTask(long delay, int fameFixRate) {
      if (this.getLevel() >= 40 && this.getClassId().level() >= 2) {
         if (this._fameTask == null) {
            this._fameTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new FameTask(this, fameFixRate), delay, delay);
         }
      }
   }

   public void stopFameTask() {
      try {
         if (this._fameTask != null) {
            this._fameTask.cancel(false);
            this._fameTask = null;
         }
      } catch (Exception var2) {
      }
   }

   public void startVitalityTask() {
      if (Config.ENABLE_VITALITY && this._vitalityTask == null) {
         this._vitalityTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new VitalityTask(this), 1000L, 60000L);
      }
   }

   public void stopVitalityTask() {
      try {
         if (this._vitalityTask != null) {
            this._vitalityTask.cancel(false);
            this._vitalityTask = null;
         }
      } catch (Exception var2) {
      }
   }

   public int getPowerGrade() {
      return this._powerGrade;
   }

   public void setPowerGrade(int power) {
      this._powerGrade = power;
   }

   public boolean isCursedWeaponEquipped() {
      return this._cursedWeaponEquippedId != 0;
   }

   public void setCursedWeaponEquippedId(int value) {
      this._cursedWeaponEquippedId = value;
   }

   public int getCursedWeaponEquippedId() {
      return this._cursedWeaponEquippedId;
   }

   public boolean isCombatFlagEquipped() {
      return this._combatFlagEquippedId;
   }

   public void setCombatFlagEquipped(boolean value) {
      this._combatFlagEquippedId = value;
   }

   public int getChargedSouls() {
      return this._souls;
   }

   public void increaseSouls(int count) {
      this._souls += count;
      SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOUR_SOUL_HAS_INCREASED_BY_S1_SO_IT_IS_NOW_AT_S2);
      sm.addNumber(count);
      sm.addNumber(this._souls);
      this.sendPacket(sm);
      this.restartSoulTask();
      this.sendPacket(new EtcStatusUpdate(this));
   }

   public boolean decreaseSouls(int count, Skill skill) {
      this._souls -= count;
      if (this.getChargedSouls() < 0) {
         this._souls = 0;
      }

      if (this.getChargedSouls() == 0) {
         this.stopSoulTask();
      } else {
         this.restartSoulTask();
      }

      this.sendPacket(new EtcStatusUpdate(this));
      return true;
   }

   public void clearSouls() {
      this._souls = 0;
      this.stopSoulTask();
      this.sendPacket(new EtcStatusUpdate(this));
   }

   private void restartSoulTask() {
      try {
         if (this._soulTask != null) {
            this._soulTask.cancel(false);
            this._soulTask = null;
         }

         this._soulTask = ThreadPoolManager.getInstance().schedule(new ResetSoulsTask(this), 600000L);
      } catch (Exception var2) {
      }
   }

   public void stopSoulTask() {
      try {
         if (this._soulTask != null) {
            this._soulTask.cancel(false);
            this._soulTask = null;
         }
      } catch (Exception var2) {
      }
   }

   public int getDeathPenaltyBuffLevel() {
      return this._deathPenaltyBuffLevel;
   }

   public void setDeathPenaltyBuffLevel(int level) {
      this._deathPenaltyBuffLevel = level;
   }

   public void calculateDeathPenaltyBuffLevel(Creature killer) {
      if (killer != null) {
         if (!this.isInFightEvent()
            && (
               !AerialCleftEvent.getInstance().isStarted() && !AerialCleftEvent.getInstance().isRewarding()
                  || !AerialCleftEvent.getInstance().isPlayerParticipant(this.getObjectId())
            )) {
            if ((!this.isCharmOfLuckAffected() || !killer.isRaid())
               && !this.isBlockedFromDeathPenalty()
               && !this.getNevitSystem().isBlessingActive()
               && !this.isPhoenixBlessed()
               && !this.isLucky()
               && !this.isInsideZone(ZoneId.PVP)
               && !this.isInsideZone(ZoneId.SIEGE)
               && !this.canOverrideCond(PcCondOverride.DEATH_PENALTY)) {
               double percent = 1.0;
               if (killer.isRaid()) {
                  percent *= this.calcStat(Stats.REDUCE_DEATH_PENALTY_BY_RAID, 1.0, null, null);
               } else if (killer.isMonster()) {
                  percent *= this.calcStat(Stats.REDUCE_DEATH_PENALTY_BY_MOB, 1.0, null, null);
               } else if (killer.isPlayable()) {
                  percent *= this.calcStat(Stats.REDUCE_DEATH_PENALTY_BY_PVP, 1.0, null, null);
               }

               if ((double)Rnd.get(1, 100) <= (double)Config.DEATH_PENALTY_CHANCE * percent && (!killer.isPlayable() || this.getKarma() > 0)) {
                  this.increaseDeathPenaltyBuffLevel();
               }
            }
         }
      }
   }

   public void increaseDeathPenaltyBuffLevel() {
      if (this.getDeathPenaltyBuffLevel() < 15 && !this.isInFightEvent()) {
         if (this.getDeathPenaltyBuffLevel() != 0) {
            Skill skill = SkillsParser.getInstance().getInfo(5076, this.getDeathPenaltyBuffLevel());
            if (skill != null) {
               this.removeSkill(skill, true);
            }
         }

         ++this._deathPenaltyBuffLevel;
         this.addSkill(SkillsParser.getInstance().getInfo(5076, this.getDeathPenaltyBuffLevel()), false);
         this.sendPacket(new EtcStatusUpdate(this));
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DEATH_PENALTY_LEVEL_S1_ADDED);
         sm.addNumber(this.getDeathPenaltyBuffLevel());
         this.sendPacket(sm);
      }
   }

   public void reduceDeathPenaltyBuffLevel() {
      if (this.getDeathPenaltyBuffLevel() > 0) {
         Skill skill = SkillsParser.getInstance().getInfo(5076, this.getDeathPenaltyBuffLevel());
         if (skill != null) {
            this.removeSkill(skill, true);
         }

         --this._deathPenaltyBuffLevel;
         if (this.getDeathPenaltyBuffLevel() > 0) {
            this.addSkill(SkillsParser.getInstance().getInfo(5076, this.getDeathPenaltyBuffLevel()), false);
            this.sendPacket(new EtcStatusUpdate(this));
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DEATH_PENALTY_LEVEL_S1_ADDED);
            sm.addNumber(this.getDeathPenaltyBuffLevel());
            this.sendPacket(sm);
         } else {
            this.sendPacket(new EtcStatusUpdate(this));
            this.sendPacket(SystemMessageId.DEATH_PENALTY_LIFTED);
         }
      }
   }

   public void restoreDeathPenaltyBuffLevel() {
      if (this.getDeathPenaltyBuffLevel() > 0) {
         this.addSkill(SkillsParser.getInstance().getInfo(5076, this.getDeathPenaltyBuffLevel()), false);
      }
   }

   @Override
   public void addTimeStampItem(ItemInstance item, long reuse, boolean byCron) {
      this._reuseTimeStampsItems.put(item.getObjectId(), new TimeStamp(item, reuse, byCron));
   }

   public void addTimeStampItem(ItemInstance item, long reuse, long systime) {
      this._reuseTimeStampsItems.put(item.getObjectId(), new TimeStamp(item, reuse, systime));
   }

   public Map<Integer, TimeStamp> getItemRemainingReuseTime() {
      return this._reuseTimeStampsItems;
   }

   @Override
   public long getItemRemainingReuseTime(int itemObjId) {
      return !this._reuseTimeStampsItems.containsKey(itemObjId) ? -1L : this._reuseTimeStampsItems.get(itemObjId).getRemaining();
   }

   public long getReuseDelayOnGroup(int group) {
      if (group > 0) {
         for(TimeStamp ts : this._reuseTimeStampsItems.values()) {
            if (ts.getSharedReuseGroup() == group && ts.hasNotPassed()) {
               return ts.getRemaining();
            }
         }
      }

      return 0L;
   }

   public TimeStamp getSharedItemReuse(int itemObjId) {
      return this._reuseTimeStampsItems.get(itemObjId);
   }

   public Map<Integer, TimeStamp> getSkillReuseTimeStamps() {
      return this._reuseTimeStampsSkills;
   }

   @Override
   public long getSkillRemainingReuseTime(int skillReuseHashId) {
      return !this._reuseTimeStampsSkills.containsKey(skillReuseHashId) ? -1L : this._reuseTimeStampsSkills.get(skillReuseHashId).getRemaining();
   }

   public boolean hasSkillReuse(int skillReuseHashId) {
      return !this._reuseTimeStampsSkills.containsKey(skillReuseHashId) ? false : this._reuseTimeStampsSkills.get(skillReuseHashId).hasNotPassed();
   }

   public TimeStamp getSkillReuseTimeStamp(int skillReuseHashId) {
      return this._reuseTimeStampsSkills.get(skillReuseHashId);
   }

   @Override
   public void addTimeStamp(Skill skill, long reuse) {
      this._reuseTimeStampsSkills.put(skill.getReuseHashCode(), new TimeStamp(skill, reuse));
   }

   @Override
   public void addTimeStamp(Skill skill, long reuse, long systime) {
      this._reuseTimeStampsSkills.put(skill.getReuseHashCode(), new TimeStamp(skill, reuse, systime));
   }

   @Override
   public Player getActingPlayer() {
      return this;
   }

   @Override
   public final void sendDamageMessage(Creature target, int damage, Skill skill, boolean mcrit, boolean pcrit, boolean miss) {
      if (miss) {
         if (target.isPlayer()) {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_EVADED_C2_ATTACK);
            sm.addPcName(target.getActingPlayer());
            sm.addCharName(this);
            target.sendPacket(sm);
         }

         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ATTACK_WENT_ASTRAY);
         sm.addPcName(this);
         this.sendPacket(sm);
      } else {
         if (pcrit) {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAD_CRITICAL_HIT);
            sm.addPcName(this);
            this.sendPacket(sm);
         }

         if (mcrit) {
            this.sendPacket(SystemMessageId.CRITICAL_HIT_MAGIC);
         }

         if (this.isInOlympiadMode()
            && target.isPlayer()
            && target.getActingPlayer().isInOlympiadMode()
            && target.getActingPlayer().getOlympiadGameId() == this.getOlympiadGameId()) {
            OlympiadGameManager.getInstance().notifyCompetitorDamage(target.getActingPlayer(), damage);
         }

         if (Config.ALLOW_DAMAGE_LIMIT && target.isNpc()) {
            DamageLimit limit = DamageLimitParser.getInstance().getDamageLimit(target.getId());
            if (limit != null) {
               int damageLimit = skill != null ? (skill.isMagic() ? limit.getMagicDamage() : limit.getPhysicDamage()) : limit.getDamage();
               if (damageLimit > 0 && damage > damageLimit) {
                  damage = damageLimit;
               }
            }
         }

         SystemMessage sm;
         if (target.isInvul() && !target.isNpc()) {
            sm = SystemMessage.getSystemMessage(SystemMessageId.ATTACK_WAS_BLOCKED);
         } else if (!target.isDoor() && !(target instanceof ControlTowerInstance)) {
            sm = SystemMessage.getSystemMessage(SystemMessageId.C1_DONE_S3_DAMAGE_TO_C2);
            sm.addPcName(this);
            sm.addCharName(target);
            sm.addNumber(damage);
         } else {
            sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_DID_S1_DMG);
            sm.addNumber(damage);
         }

         this.sendPacket(sm);
      }
   }

   public void setAgathionId(int npcId) {
      this._agathionId = npcId;
   }

   public int getAgathionId() {
      return this._agathionId;
   }

   public int getVitalityPoints() {
      return this.getStat().getVitalityPoints();
   }

   public int getVitalityLevel() {
      return this.getStat().getVitalityLevel();
   }

   public void setVitalityPoints(int points, boolean quiet) {
      this.getStat().setVitalityPoints(points, quiet);
   }

   public void updateVitalityPoints(double vitalityPoints, boolean useRates, boolean quiet) {
      this.getStat().updateVitalityPoints(vitalityPoints, useRates, quiet);
   }

   public void checkItemRestriction() {
      for(int i = 0; i < 25; ++i) {
         ItemInstance equippedItem = this.getInventory().getPaperdollItem(i);
         if (equippedItem != null
            && (
               !equippedItem.getItem().checkCondition(this, this, false)
                  || this.isInOlympiadMode() && equippedItem.isOlyRestrictedItem()
                  || this.isInFightEvent() && equippedItem.isEventRestrictedItem()
            )) {
            this.getInventory().unEquipItemInSlot(i);
            InventoryUpdate iu = new InventoryUpdate();
            iu.addModifiedItem(equippedItem);
            this.sendPacket(iu);
            SystemMessage sm = null;
            if (equippedItem.getItem().getBodyPart() == 8192) {
               this.sendPacket(SystemMessageId.CLOAK_REMOVED_BECAUSE_ARMOR_SET_REMOVED);
               return;
            }

            if (equippedItem.getEnchantLevel() > 0) {
               sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
               sm.addNumber(equippedItem.getEnchantLevel());
               sm.addItemName(equippedItem);
            } else {
               sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED);
               sm.addItemName(equippedItem);
            }

            this.sendPacket(sm);
         }
      }
   }

   public void addTransformSkill(int id) {
      if (this._transformAllowedSkills == null) {
         synchronized(this) {
            if (this._transformAllowedSkills == null) {
               this._transformAllowedSkills = new HashSet<>();
            }
         }
      }

      this._transformAllowedSkills.add(id);
   }

   public boolean hasTransformSkill(int id) {
      return this._transformAllowedSkills != null && this._transformAllowedSkills.contains(id);
   }

   public synchronized void removeAllTransformSkills() {
      this._transformAllowedSkills = null;
   }

   protected void startFeed(int npcId) {
      this._canFeed = npcId > 0;
      if (this.isMounted()) {
         if (this.hasSummon()) {
            this.setCurrentFeed(((PetInstance)this.getSummon()).getCurrentFed());
            this._controlItemId = this.getSummon().getControlObjectId();
            this.sendPacket(new SetupGauge(this, 3, this.getCurrentFeed() * 10000 / this.getFeedConsume(), this.getMaxFeed() * 10000 / this.getFeedConsume()));
            if (!this.isDead()) {
               this._mountFeedTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new PetFeedTask(this), 10000L, 10000L);
            }
         } else if (this._canFeed) {
            this.setCurrentFeed(this.getMaxFeed());
            SetupGauge sg = new SetupGauge(this, 3, this.getCurrentFeed() * 10000 / this.getFeedConsume(), this.getMaxFeed() * 10000 / this.getFeedConsume());
            this.sendPacket(sg);
            if (!this.isDead()) {
               this._mountFeedTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new PetFeedTask(this), 10000L, 10000L);
            }
         }
      }
   }

   public void stopFeed() {
      try {
         if (this._mountFeedTask != null) {
            this._mountFeedTask.cancel(false);
            this._mountFeedTask = null;
         }
      } catch (Exception var2) {
      }
   }

   private final void clearPetData() {
      this._data = null;
   }

   public final PetData getPetData(int npcId) {
      if (this._data == null) {
         this._data = PetsParser.getInstance().getPetData(npcId);
      }

      return this._data;
   }

   private final PetLevelTemplate getPetLevelData(int npcId) {
      if (this._leveldata == null) {
         this._leveldata = PetsParser.getInstance().getPetData(npcId).getPetLevelData(this.getMountLevel());
      }

      return this._leveldata;
   }

   public int getCurrentFeed() {
      return this._curFeed;
   }

   public int getFeedConsume() {
      return this.isAttackingNow() ? this.getPetLevelData(this._mountNpcId).getPetFeedBattle() : this.getPetLevelData(this._mountNpcId).getPetFeedNormal();
   }

   public void setCurrentFeed(int num) {
      boolean lastHungryState = this.isHungry();
      this._curFeed = num > this.getMaxFeed() ? this.getMaxFeed() : num;
      SetupGauge sg = new SetupGauge(this, 3, this.getCurrentFeed() * 10000 / this.getFeedConsume(), this.getMaxFeed() * 10000 / this.getFeedConsume());
      this.sendPacket(sg);
      if (lastHungryState != this.isHungry()) {
         this.broadcastUserInfo(true);
      }
   }

   private int getMaxFeed() {
      return this.getPetLevelData(this._mountNpcId).getPetMaxFeed();
   }

   public boolean isHungry() {
      return this._canFeed
         ? (float)this.getCurrentFeed()
            < (float)this.getPetData(this.getMountNpcId()).getHungryLimit() / 100.0F * (float)this.getPetLevelData(this.getMountNpcId()).getPetMaxFeed()
         : false;
   }

   public void enteredNoLanding(int delay) {
      this._dismountTask = ThreadPoolManager.getInstance().schedule(new DismountTask(this), (long)(delay * 1000));
   }

   public void exitedNoLanding() {
      try {
         if (this._dismountTask != null) {
            this._dismountTask.cancel(true);
            this._dismountTask = null;
         }
      } catch (Exception var2) {
      }
   }

   public void storePetFood(int petId) {
      if (this._controlItemId != 0 && petId != 0) {
         String req = "UPDATE pets SET fed=? WHERE item_obj_id = ?";

         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement(req);
         ) {
            statement.setInt(1, this.getCurrentFeed());
            statement.setInt(2, this._controlItemId);
            statement.executeUpdate();
            this._controlItemId = 0;
         } catch (Exception var35) {
            _log.log(Level.SEVERE, "Failed to store Pet [NpcId: " + petId + "] data", (Throwable)var35);
         }
      }
   }

   public void setIsInSiege(boolean b) {
      this._isInSiege = b;
   }

   public boolean isInSiege() {
      return this._isInSiege;
   }

   public void setIsInHideoutSiege(boolean isInHideoutSiege) {
      this._isInHideoutSiege = isInHideoutSiege;
   }

   public boolean isInHideoutSiege() {
      return this._isInHideoutSiege;
   }

   public boolean isFlyingMounted() {
      return this.isTransformed() && this.getTransformation().isFlying();
   }

   public int getCharges() {
      return this._charges.get();
   }

   public void increaseCharges(int count, int max) {
      if (this._charges.get() >= max) {
         this.sendPacket(SystemMessageId.FORCE_MAXLEVEL_REACHED);
      } else {
         this.restartChargeTask();
         if (this._charges.addAndGet(count) >= max) {
            this._charges.set(max);
            this.sendPacket(SystemMessageId.FORCE_MAXLEVEL_REACHED);
         } else {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.FORCE_INCREASED_TO_S1);
            sm.addNumber(this._charges.get());
            this.sendPacket(sm);
         }

         this.sendPacket(new EtcStatusUpdate(this));
      }
   }

   public boolean decreaseCharges(int count) {
      if (this._charges.get() < count) {
         return false;
      } else {
         if (this._charges.addAndGet(-count) == 0) {
            this.stopChargeTask();
         } else {
            this.restartChargeTask();
         }

         this.sendPacket(new EtcStatusUpdate(this));
         return true;
      }
   }

   public void clearCharges() {
      this._charges.set(0);
      this.sendPacket(new EtcStatusUpdate(this));
   }

   private void restartChargeTask() {
      try {
         if (this._chargeTask != null) {
            this._chargeTask.cancel(false);
            this._chargeTask = null;
         }

         this._chargeTask = ThreadPoolManager.getInstance().schedule(new ResetChargesTask(this), 600000L);
      } catch (Exception var2) {
      }
   }

   public void stopChargeTask() {
      try {
         if (this._chargeTask != null) {
            this._chargeTask.cancel(false);
            this._chargeTask = null;
         }
      } catch (Exception var2) {
      }
   }

   public void teleportBookmarkModify(int id, int icon, String tag, String name) {
      BookmarkTemplate bookmark = this._tpbookmarks.get(id);
      if (bookmark != null) {
         bookmark.setIcon(icon);
         bookmark.setTag(tag);
         bookmark.setName(name);
         CharacterBookMarkDAO.getInstance().update(this, id, icon, tag, name);
      }

      this.sendPacket(new ExGetBookMarkInfo(this));
   }

   public void teleportBookmarkDelete(int id) {
      if (this._tpbookmarks.remove(id) != null) {
         CharacterBookMarkDAO.getInstance().delete(this, id);
         this.sendPacket(new ExGetBookMarkInfo(this));
      }
   }

   public void teleportBookmarkGo(int id) {
      if (this.teleportBookmarkCondition(0)) {
         Skill sk = SkillsParser.getInstance().getInfo(2588, 1);
         if (sk != null && this.checkDoCastConditions(sk, false) && !this.isCastingNow()) {
            int itemId = 0;
            short var6;
            if (this.getInventory().getInventoryItemCount(13016, 0) > 0L) {
               var6 = 13016;
            } else if (this.getInventory().getInventoryItemCount(13302, 0) > 0L) {
               var6 = 13302;
            } else {
               if (this.getInventory().getInventoryItemCount(20025, 0) <= 0L) {
                  this.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_BECAUSE_YOU_DO_NOT_HAVE_A_TELEPORT_ITEM);
                  return;
               }

               var6 = 20025;
            }

            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
            sm.addItemName(var6);
            this.sendPacket(sm);
            BookmarkTemplate bookmark = this._tpbookmarks.get(id);
            if (bookmark != null) {
               this.destroyItem("Consume", this.getInventory().getItemByItemId(var6).getObjectId(), 1L, null, false);
               this._bookmarkLocation = bookmark;
               this.useMagic(sk, false, false, false);
            }

            this.sendPacket(new ExGetBookMarkInfo(this));
         } else {
            if (!this.isCastingNow()) {
               this._bookmarkLocation = null;
            }
         }
      }
   }

   public boolean teleportBookmarkCondition(int type) {
      if (this.isInCombat()) {
         this.sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_DURING_A_BATTLE);
         return false;
      } else if (this.isInSiege() || this.getSiegeState() != 0) {
         this.sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_PARTICIPATING);
         return false;
      } else if (this.isInDuel()) {
         this.sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_DURING_A_DUEL);
         return false;
      } else if (this.isFlying()) {
         this.sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_FLYING);
         return false;
      } else if (this.isInOlympiadMode()) {
         this.sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_PARTICIPATING_IN_AN_OLYMPIAD_MATCH);
         return false;
      } else if (this.isParalyzed()) {
         this.sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_YOU_ARE_PARALYZED);
         return false;
      } else if (this.isDead()) {
         this.sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_YOU_ARE_DEAD);
         return false;
      } else if (type != 1 || !this.isIn7sDungeon() && (!this.isInParty() || !this.getParty().isInDimensionalRift())) {
         if (this.isInWater()) {
            this.sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_UNDERWATER);
            return false;
         } else if (type != 1
            || !this.isInsideZone(ZoneId.SIEGE)
               && !this.isInsideZone(ZoneId.CLAN_HALL)
               && !this.isInsideZone(ZoneId.JAIL)
               && !this.isInsideZone(ZoneId.CASTLE)
               && !this.isInsideZone(ZoneId.NO_SUMMON_FRIEND)
               && !this.isInsideZone(ZoneId.FORT)) {
            if (!this.isInsideZone(ZoneId.NO_BOOKMARK) && !this.isInBoat() && !this.isInAirShip()) {
               return true;
            } else {
               if (type == 0) {
                  this.sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_IN_THIS_AREA);
               } else if (type == 1) {
                  this.sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_TO_REACH_THIS_AREA);
               }

               return false;
            }
         } else {
            this.sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_TO_REACH_THIS_AREA);
            return false;
         }
      } else {
         this.sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_TO_REACH_THIS_AREA);
         return false;
      }
   }

   public void teleportBookmarkAdd(int x, int y, int z, int icon, String tag, String name) {
      if (this.teleportBookmarkCondition(1)) {
         if (this._tpbookmarks.size() >= this._bookmarkslot) {
            this.sendPacket(SystemMessageId.YOU_HAVE_NO_SPACE_TO_SAVE_THE_TELEPORT_LOCATION);
         } else if (this.getInventory().getInventoryItemCount(20033, 0) == 0L) {
            this.sendPacket(SystemMessageId.YOU_CANNOT_BOOKMARK_THIS_LOCATION_BECAUSE_YOU_DO_NOT_HAVE_A_MY_TELEPORT_FLAG);
         } else {
            int id = 1;

            while(id <= this._bookmarkslot && this._tpbookmarks.containsKey(id)) {
               ++id;
            }

            this._tpbookmarks.put(id, new BookmarkTemplate(id, x, y, z, icon, tag, name));
            this.destroyItem("Consume", this.getInventory().getItemByItemId(20033).getObjectId(), 1L, null, false);
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
            sm.addItemName(20033);
            this.sendPacket(sm);
            CharacterBookMarkDAO.getInstance().add(this, id, x, y, z, icon, tag, name);
            this.sendPacket(new ExGetBookMarkInfo(this));
         }
      }
   }

   @Override
   public void sendInfo(Player activeChar) {
      if (!activeChar.getNotShowTraders() || !this.isInStoreNow()) {
         if (this.isInBoat()) {
            this.setXYZ(this.getBoat().getLocation());
            activeChar.sendPacket(new GetOnVehicle(this.getObjectId(), this.getBoat().getObjectId(), this.getInVehiclePosition()));
         } else if (this.isInAirShip()) {
            this.setXYZ(this.getAirShip().getLocation());
            activeChar.sendPacket(new ExGetOnAirShip(this, this.getAirShip()));
         } else if (this.isMoving()) {
            activeChar.sendPacket(new MoveToLocation(this));
         }

         if (this.isSitting() && this.getSittingObject() != null) {
            activeChar.sendPacket(new ChairSit(this, this._sittingObject.getId()));
         }

         activeChar.sendPacket(new CharInfo(this, activeChar), new ExBrExtraUserInfo(this));
         activeChar.sendPacket(RelationChanged.update(activeChar, this, activeChar));
         switch(this.getPrivateStoreType()) {
            case 1:
               activeChar.sendPacket(new PrivateStoreSellMsg(this));
            case 2:
            case 4:
            case 6:
            case 7:
            default:
               break;
            case 3:
               activeChar.sendPacket(new PrivateStoreBuyMsg(this));
               break;
            case 5:
               activeChar.sendPacket(new RecipeShopMsg(this));
               break;
            case 8:
               activeChar.sendPacket(new ExPrivateStorePackageMsg(this));
         }

         if (!activeChar.isInZonePeace()) {
            if (this.isCastingNow()) {
               Creature castingTarget = this.getCastingTarget();
               Skill castingSkill = this.getCastingSkill();
               long animationEndTime = this.getAnimationEndTime();
               if (castingSkill != null && castingTarget != null && this.getAnimationEndTime() > 0L) {
                  activeChar.sendPacket(
                     new MagicSkillUse(
                        this, castingTarget, castingSkill.getId(), castingSkill.getLevel(), (int)(animationEndTime - System.currentTimeMillis()), 0
                     )
                  );
               }
            }

            if (this.isInCombat()) {
               activeChar.sendPacket(new AutoAttackStart(this.getObjectId()));
            }

            GameServerPacket dominion = !TerritoryWarManager.getInstance().isTWInProgress()
                  || !TerritoryWarManager.getInstance().checkIsRegistered(-1, this.getObjectId())
                     && !TerritoryWarManager.getInstance().checkIsRegistered(-1, this.getClan())
               ? null
               : new ExDominionWarStart(this);
            if (dominion != null) {
               activeChar.sendPacket(dominion);
            }
         }
      }
   }

   public void showQuestMovie(int id) {
      if (this._movieId <= 0) {
         this.abortAttack();
         this.abortCast();
         this.stopMove(null);
         this._movieId = id;
         this.sendPacket(new ExStartScenePlayer(id));
      }
   }

   public boolean isAllowedToEnchantSkills() {
      if (this.isLocked()) {
         return false;
      } else if (!this.isTransformed() && !this.isInStance()) {
         if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(this)) {
            return false;
         } else if (this.isCastingNow() || this.isCastingSimultaneouslyNow()) {
            return false;
         } else {
            return !this.isInBoat() && !this.isInAirShip();
         }
      } else {
         return false;
      }
   }

   public void setCreateDate(Calendar createDate) {
      this._createDate = createDate;
   }

   public Calendar getCreateDate() {
      return this._createDate;
   }

   public int checkBirthDay() {
      Calendar now = Calendar.getInstance();
      if (this._createDate.get(5) == 29 && this._createDate.get(2) == 1) {
         this._createDate.add(11, -24);
      }

      if (now.get(2) == this._createDate.get(2) && now.get(5) == this._createDate.get(5) && now.get(1) != this._createDate.get(1)) {
         return 0;
      } else {
         for(int i = 1; i < 6; ++i) {
            now.add(11, 24);
            if (now.get(2) == this._createDate.get(2) && now.get(5) == this._createDate.get(5) && now.get(1) != this._createDate.get(1)) {
               return i;
            }
         }

         return -1;
      }
   }

   public List<Integer> getFriendList() {
      return this._friendList;
   }

   public void restoreFriendList() {
      this._friendList.clear();

      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         String sqlQuery = "SELECT friendId FROM character_friends WHERE charId=? AND relation=0";
         PreparedStatement statement = con.prepareStatement("SELECT friendId FROM character_friends WHERE charId=? AND relation=0");
         statement.setInt(1, this.getObjectId());
         ResultSet rset = statement.executeQuery();

         while(rset.next()) {
            int friendId = rset.getInt("friendId");
            if (friendId != this.getObjectId()) {
               this._friendList.add(friendId);
            }
         }

         rset.close();
         statement.close();
      } catch (Exception var17) {
         _log.log(Level.WARNING, "Error found in " + this.getName() + "'s FriendList: " + var17.getMessage(), (Throwable)var17);
      }
   }

   protected void notifyFriends() {
      L2FriendStatus pkt = new L2FriendStatus(this.getObjectId());

      for(int id : this._friendList) {
         Player friend = World.getInstance().getPlayer(id);
         if (friend != null) {
            friend.sendPacket(pkt);
         }
      }
   }

   public boolean isSilenceMode() {
      return this._silenceMode;
   }

   public boolean isSilenceMode(int playerObjId) {
      if (Config.SILENCE_MODE_EXCLUDE && this._silenceMode && this._silenceModeExcluded != null) {
         return !this._silenceModeExcluded.contains(playerObjId);
      } else {
         return this._silenceMode;
      }
   }

   public void setSilenceMode(boolean mode) {
      this._silenceMode = mode;
      if (this._silenceModeExcluded != null) {
         this._silenceModeExcluded.clear();
      }

      this.sendPacket(new EtcStatusUpdate(this));
   }

   public void addSilenceModeExcluded(int playerObjId) {
      if (this._silenceModeExcluded == null) {
         this._silenceModeExcluded = new ArrayList<>(1);
      }

      this._silenceModeExcluded.add(playerObjId);
   }

   private void storeRecipeShopList() {
      if (this.hasManufactureShop()) {
         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement st = con.prepareStatement("DELETE FROM character_recipeshoplist WHERE charId=?");
         ) {
            st.setInt(1, this.getObjectId());
            st.execute();

            try (PreparedStatement stx = con.prepareStatement(
                  "INSERT INTO character_recipeshoplist (`charId`, `recipeId`, `price`, `index`) VALUES (?, ?, ?, ?)"
               )) {
               int i = 1;

               for(ManufactureItemTemplate item : this._manufactureItems.values()) {
                  stx.setInt(1, this.getObjectId());
                  stx.setInt(2, item.getRecipeId());
                  stx.setLong(3, item.getCost());
                  stx.setInt(4, i++);
                  stx.addBatch();
               }

               stx.executeBatch();
            }
         } catch (Exception var57) {
            _log.log(Level.SEVERE, "Could not store recipe shop for playerId " + this.getObjectId() + ": ", (Throwable)var57);
         }
      }
   }

   private void restoreRecipeShopList() {
      if (this._manufactureItems != null) {
         this._manufactureItems.clear();
      }

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT * FROM character_recipeshoplist WHERE charId=? ORDER BY `index`");
      ) {
         statement.setInt(1, this.getObjectId());

         try (ResultSet rset = statement.executeQuery()) {
            while(rset.next()) {
               this.getManufactureItems().put(rset.getInt("recipeId"), new ManufactureItemTemplate(rset.getInt("recipeId"), rset.getLong("price")));
            }
         }
      } catch (Exception var59) {
         _log.log(Level.SEVERE, "Could not restore recipe shop list data for playerId: " + this.getObjectId(), (Throwable)var59);
      }
   }

   public final int getClientX() {
      return this._clientX;
   }

   public final int getClientY() {
      return this._clientY;
   }

   public final int getClientZ() {
      return this._clientZ;
   }

   public final int getClientHeading() {
      return this._clientHeading;
   }

   public final void setClientX(int val) {
      this._clientX = val;
   }

   public final void setClientY(int val) {
      this._clientY = val;
   }

   public final void setClientZ(int val) {
      this._clientZ = val;
   }

   public final void setClientHeading(int val) {
      this._clientHeading = val;
   }

   public final boolean isFalling(int z) {
      if (this.isDead() || this.isFlying() || this.isFlyingMounted() || this.isInWater(this) || this.isInVehicle()) {
         return false;
      } else if (this.isFalling()) {
         return true;
      } else {
         double deltaZ = (double)Math.abs(this.getZ() - z);
         if (deltaZ <= (double)this.getBaseTemplate().getSafeFallHeight()) {
            return false;
         } else if (!GeoEngine.hasGeo(this.getX(), this.getY(), this.getGeoIndex())) {
            return false;
         } else {
            if (Config.ENABLE_FALLING_DAMAGE) {
               int damage = (int)this.calcStat(Stats.FALL, deltaZ * this.getMaxHp() / 2100.0, null, null);
               if (damage > 0) {
                  ThreadPoolManager.getInstance().schedule(new FallingTask(this, damage), 5000L);
               }
            }

            this.setFalling();
            return false;
         }
      }
   }

   public final boolean isFalling() {
      return System.currentTimeMillis() < this._fallingTimestamp;
   }

   public final void setFalling() {
      this._fallingTimestamp = System.currentTimeMillis() + 5000L;
   }

   public int getMovieId() {
      return this._movieId;
   }

   public void setMovieId(int id) {
      this._movieId = id;
   }

   public void updateLastItemAuctionRequest() {
      this._lastItemAuctionInfoRequest = System.currentTimeMillis();
   }

   public boolean isItemAuctionPolling() {
      return System.currentTimeMillis() - this._lastItemAuctionInfoRequest < 2000L;
   }

   @Override
   public boolean isMovementDisabled() {
      return super.isMovementDisabled() || this._movieId > 0;
   }

   public void restoreUISettings() {
      this._uiKeySettings = new UIKeysSettings(this.getObjectId());
   }

   private void storeUISettings() {
      if (this._uiKeySettings != null) {
         if (!this._uiKeySettings.isSaved()) {
            this._uiKeySettings.saveInDB();
         }
      }
   }

   public UIKeysSettings getUISettings() {
      return this._uiKeySettings;
   }

   public String getLang() {
      return Config.MULTILANG_ENABLE && this.getVar("lang@") != null ? this.getVar("lang@") : Config.MULTILANG_DEFAULT;
   }

   public void setLang(String lang) {
      this.setVar("lang@", lang);
   }

   public boolean getUseAutoLoot() {
      return Config.AUTO_LOOT ? true : this.getVarB("useAutoLoot@");
   }

   public boolean getUseAutoLootHerbs() {
      return Config.AUTO_LOOT_HERBS ? true : this.getVarB("useAutoLootHerbs@");
   }

   public boolean getExpOn() {
      return !this.getVarB("blockedEXP@");
   }

   public boolean getBlockBuffs() {
      return this.getVarB("useBlockBuffs@");
   }

   public boolean getNotShowTraders() {
      return this.getVarB("useHideTraders@");
   }

   public boolean getBlockPartyRecall() {
      return this.getVarB("useBlockPartyRecall@");
   }

   public boolean getNotShowBuffsAnimation() {
      return this.getVarB("useHideBuffs@");
   }

   public boolean getTradeRefusal() {
      return this.getVarB("useBlockTrade@");
   }

   public boolean getPartyInviteRefusal() {
      return this.getVarB("useBlockParty@");
   }

   public boolean getFriendInviteRefusal() {
      return this.getVarB("useBlockFriend@");
   }

   public void setVar(String name, Object value) {
      this.setVar(name, String.valueOf(value), -1L);
   }

   public void setVar(String name, String value, long expirationTime) {
      CharacterVariable var = new CharacterVariable(name, String.valueOf(value), expirationTime);
      if (CharacterVariablesDAO.getInstance().insert(this.getObjectId(), var)) {
         this._variables.put(name, var);
      }
   }

   public void unsetVar(String name) {
      if (name != null && !name.isEmpty()) {
         if (this._variables.containsKey(name) && CharacterVariablesDAO.getInstance().delete(this.getObjectId(), name)) {
            this._variables.remove(name);
         }
      }
   }

   public CharacterVariable getVariable(String name) {
      CharacterVariable var = this._variables.get(name);
      return var != null ? var : null;
   }

   public String getVar(String name) {
      return this.getVar(name, null);
   }

   public String getVar(String name, String defaultValue) {
      CharacterVariable var = this._variables.get(name);
      return var != null && !var.isExpired() ? var.getValue() : defaultValue;
   }

   public boolean getVarB(String name, boolean defaultVal) {
      String var = this.getVar(name);
      if (var == null) {
         return defaultVal;
      } else {
         return !var.equals("0") && !var.equalsIgnoreCase("false");
      }
   }

   public boolean getVarB(String name) {
      return this.getVarB(name, false);
   }

   public Collection<CharacterVariable> getVars() {
      return this._variables.values();
   }

   public int getVarInt(String name) {
      return this.getVarInt(name, 0);
   }

   public int getVarInt(String name, int defaultVal) {
      int result = defaultVal;
      String var = this.getVar(name);
      if (var != null) {
         result = Integer.parseInt(var);
      }

      return result;
   }

   public long getVarLong(String name, long defaultVal) {
      long result = defaultVal;
      String var = this.getVar(name);
      if (var != null) {
         result = Long.parseLong(var);
      }

      return result;
   }

   public void restoreVariables() {
      for(CharacterVariable var : CharacterVariablesDAO.getInstance().restore(this.getObjectId())) {
         this._variables.put(var.getName(), var);
      }

      if (this.getVar("lang@") == null) {
         this.setVar("lang@", Config.MULTILANG_DEFAULT);
      }
   }

   public void removeFromBossZone() {
      try {
         for(BossZone _zone : EpicBossManager.getInstance().getZones().values()) {
            _zone.removePlayer(this);
         }
      } catch (Exception var3) {
         _log.log(Level.WARNING, "Exception on removeFromBossZone(): " + var3.getMessage(), (Throwable)var3);
      }
   }

   public void checkPlayerSkills() {
      for(Entry<Integer, Skill> e : this.getSkills().entrySet()) {
         if (e.getValue().getLevel() <= 99 || Config.DECREASE_ENCHANT_SKILLS) {
            int checkLvl = e.getValue().getLevel() > 99 ? SkillsParser.getInstance().getMaxLevel(e.getKey()) : e.getValue().getLevel();
            SkillLearn learn = SkillTreesParser.getInstance().getClassSkill(e.getKey(), checkLvl, this.getClassId());
            if (learn != null) {
               int lvlDiff = e.getKey() == 239 ? 0 : 9;
               if (this.getLevel() < (e.getValue().getLevel() > 99 ? e.getValue().getMagicLevel() - lvlDiff : learn.getGetLevel() - lvlDiff)) {
                  this.deacreaseSkillLevel(e.getValue(), lvlDiff, e.getValue().getLevel() > 99);
               }
            }
         }
      }
   }

   private void deacreaseSkillLevel(Skill skill, int lvlDiff, boolean isEnchant) {
      int nextLevel = -1;
      if (isEnchant) {
         for(int i = skill.getLevel();
            i != 0
               && i >= 100
               && (i <= 130 || i >= 200)
               && (i <= 230 || i >= 300)
               && (i <= 330 || i >= 400)
               && (i <= 430 || i >= 500)
               && (i <= 530 || i >= 600)
               && (i <= 630 || i >= 700);
            --i
         ) {
            Skill newSkill = SkillsParser.getInstance().getInfo(skill.getId(), i);
            if (newSkill != null && newSkill.getMagicLevel() <= skill.getMagicLevel() - lvlDiff) {
               nextLevel = newSkill.getLevel();
               break;
            }
         }
      }

      if (nextLevel == -1) {
         Map<Integer, SkillLearn> skillTree = SkillTreesParser.getInstance().getCompleteClassSkillTree(this.getClassId());

         for(SkillLearn sl : skillTree.values()) {
            if (sl.getId() == skill.getId() && nextLevel < sl.getLvl() && this.getLevel() >= sl.getGetLevel() - lvlDiff) {
               nextLevel = sl.getLvl();
            }
         }
      }

      if (nextLevel == -1) {
         _log.fine("Removing skill " + skill + " from player " + this.toString());
         this.removeSkill(skill, true);
      } else {
         _log.fine("Decreasing skill " + skill + " to " + nextLevel + " for player " + this.toString());
         this.addSkill(SkillsParser.getInstance().getInfo(skill.getId(), nextLevel), true);
      }
   }

   public boolean canMakeSocialAction() {
      return this.getPrivateStoreType() == 0
         && this.getActiveRequester() == null
         && !this.isAlikeDead()
         && !this.isAllSkillsDisabled()
         && !this.isInDuel()
         && !this.isCastingNow()
         && !this.isCastingSimultaneouslyNow()
         && this.getAI().getIntention() == CtrlIntention.IDLE
         && !AttackStanceTaskManager.getInstance().hasAttackStanceTask(this)
         && !this.isInOlympiadMode();
   }

   public void setMultiSocialAction(int id, int targetId) {
      this._multiSociaAction = id;
      this._multiSocialTarget = targetId;
   }

   public int getMultiSociaAction() {
      return this._multiSociaAction;
   }

   public int getMultiSocialTarget() {
      return this._multiSocialTarget;
   }

   public Collection<BookmarkTemplate> getTeleportBookmarks() {
      return this._tpbookmarks.values();
   }

   public void addTeleportBookmarks(int id, BookmarkTemplate template) {
      this._tpbookmarks.put(id, template);
   }

   public int getBookmarkslot() {
      return this._bookmarkslot;
   }

   public int getQuestInventoryLimit() {
      return Config.INVENTORY_MAXIMUM_QUEST_ITEMS;
   }

   public boolean canAttackCharacter(Creature cha) {
      if (cha instanceof Attackable) {
         return true;
      } else {
         if (cha instanceof Playable) {
            if (cha.isInsideZone(ZoneId.PVP) && !cha.isInsideZone(ZoneId.SIEGE)) {
               if (cha.isInsideZone(ZoneId.FUN_PVP) && cha.isPlayer()) {
                  FunPvpZone zone = ZoneManager.getInstance().getZone(cha, FunPvpZone.class);
                  if (zone != null && zone.isPvpZone() && this.isFriend(cha.getActingPlayer())) {
                     return false;
                  }
               }

               return true;
            }

            Player target;
            if (cha instanceof Summon) {
               target = ((Summon)cha).getOwner();
            } else {
               target = (Player)cha;
            }

            if (this.isInDuel() && target.isInDuel() && target.getDuelId() == this.getDuelId()) {
               return true;
            }

            if (this.isInParty() && target.isInParty()) {
               if (this.getParty() == target.getParty()) {
                  return false;
               }

               if ((this.getParty().getCommandChannel() != null || target.getParty().getCommandChannel() != null)
                  && this.getParty().getCommandChannel() == target.getParty().getCommandChannel()) {
                  return false;
               }
            } else if (this.getClan() != null && target.getClan() != null) {
               if (this.getClanId() == target.getClanId()) {
                  return false;
               }

               if ((this.getAllyId() > 0 || target.getAllyId() > 0) && this.getAllyId() == target.getAllyId()) {
                  return false;
               }

               if (this.getClan().isAtWarWith(target.getClan().getId()) && target.getClan().isAtWarWith(this.getClan().getId())) {
                  return true;
               }
            } else if ((this.getClan() == null || target.getClan() == null) && target.getPvpFlag() == 0 && target.getKarma() == 0) {
               return false;
            }
         }

         return true;
      }
   }

   public boolean isInventoryUnder90(boolean includeQuestInv) {
      return (double)this.getInventory().getSize(includeQuestInv) <= (double)this.getInventoryLimit() * 0.9;
   }

   public boolean havePetInvItems() {
      return this._petItems;
   }

   public void setPetInvItems(boolean haveit) {
      this._petItems = haveit;
   }

   private void restorePetInventoryItems() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT object_id FROM `items` WHERE `owner_id`=? AND (`loc`='PET' OR `loc`='PET_EQUIP') LIMIT 1;");
      ) {
         statement.setInt(1, this.getObjectId());

         try (ResultSet rset = statement.executeQuery()) {
            if (rset.next() && rset.getInt("object_id") > 0) {
               this.setPetInvItems(true);
            } else {
               this.setPetInvItems(false);
            }
         }
      } catch (Exception var59) {
         _log.log(Level.SEVERE, "Could not check Items in Pet Inventory for playerId: " + this.getObjectId(), (Throwable)var59);
      }
   }

   public String getAdminConfirmCmd() {
      return this._adminConfirmCmd;
   }

   public void setAdminConfirmCmd(String adminConfirmCmd) {
      this._adminConfirmCmd = adminConfirmCmd;
   }

   public void setBlockCheckerArena(byte arena) {
      this._handysBlockCheckerEventArena = arena;
   }

   public int getBlockCheckerArena() {
      return this._handysBlockCheckerEventArena;
   }

   public void setLastPetitionGmName(String gmName) {
      this._lastPetitionGmName = gmName;
   }

   public String getLastPetitionGmName() {
      return this._lastPetitionGmName;
   }

   public ContactList getContactList() {
      return this._contactList;
   }

   public long getNotMoveUntil() {
      return this._notMoveUntil;
   }

   public void updateNotMoveUntil() {
      this._notMoveUntil = System.currentTimeMillis() + (long)Config.PLAYER_MOVEMENT_BLOCK_TIME;
   }

   @Override
   public boolean isPlayer() {
      return true;
   }

   @Override
   public boolean isChargedShot(ShotType type) {
      ItemInstance weapon = this.getActiveWeaponInstance();
      return weapon != null && weapon.isChargedShot(type);
   }

   @Override
   public void setChargedShot(ShotType type, boolean charged) {
      ItemInstance weapon = this.getActiveWeaponInstance();
      if (weapon != null) {
         weapon.setChargedShot(type, charged);
      }
   }

   public final Skill getCustomSkill(int skillId) {
      return this._customSkills != null ? this._customSkills.get(skillId) : null;
   }

   private final void addCustomSkill(Skill skill) {
      if (skill != null && skill.getDisplayId() != skill.getId()) {
         if (this._customSkills == null) {
            this._customSkills = new ConcurrentHashMap<>();
         }

         this._customSkills.put(skill.getDisplayId(), skill);
      }
   }

   private final void removeCustomSkill(Skill skill) {
      if (skill != null && this._customSkills != null && skill.getDisplayId() != skill.getId()) {
         this._customSkills.remove(skill.getDisplayId());
      }
   }

   @Override
   public boolean canRevive() {
      return this._canRevive;
   }

   @Override
   public void setCanRevive(boolean val) {
      this._canRevive = val;
   }

   private boolean fireEquipmentListeners(boolean isEquiped, ItemInstance item) {
      if (item != null) {
         EquipmentEvent event = new EquipmentEvent();
         event.setEquipped(!isEquiped);
         event.setItem(item);

         for(EquipmentListener listener : this._equipmentListeners) {
            if (!listener.onEquip(event)) {
               return false;
            }
         }

         for(EquipmentListener listener : GLOBAL_EQUIPMENT_LISTENERS) {
            if (!listener.onEquip(event)) {
               return false;
            }
         }
      }

      return true;
   }

   private boolean fireTransformListeners(Transform transformation, boolean isTransforming) {
      if (transformation != null && !this._transformListeners.isEmpty()) {
         TransformEvent event = new TransformEvent();
         event.setTransformation(transformation);
         event.setTransforming(isTransforming);

         for(TransformListener listener : this._transformListeners) {
            if (!listener.onTransform(event)) {
               return false;
            }
         }
      }

      return true;
   }

   private boolean fireHennaListeners(Henna henna, boolean isAdding) {
      if (henna != null && !HENNA_LISTENERS.isEmpty()) {
         HennaEvent event = new HennaEvent();
         event.setAdd(isAdding);
         event.setHenna(henna);
         event.setPlayer(this);

         for(HennaListener listener : HENNA_LISTENERS) {
            if (!listener.onRemoveHenna(event)) {
               return false;
            }
         }
      }

      return true;
   }

   @Override
   public boolean isOnEvent() {
      for(EventListener listener : this._eventListeners) {
         if (listener.isOnEvent()) {
            return true;
         }
      }

      return super.isOnEvent();
   }

   public boolean isBlockedFromExit() {
      for(EventListener listener : this._eventListeners) {
         if (listener.isOnEvent() && listener.isBlockingExit()) {
            return true;
         }
      }

      return false;
   }

   public boolean isBlockedFromDeathPenalty() {
      for(EventListener listener : this._eventListeners) {
         if (listener.isOnEvent() && listener.isBlockingDeathPenalty()) {
            return true;
         }
      }

      return false;
   }

   @Override
   public void addOverrideCond(PcCondOverride... excs) {
      super.addOverrideCond(excs);
      this.setVar("cond_override", Long.toString(this._exceptions));
   }

   @Override
   public void removeOverridedCond(PcCondOverride... excs) {
      super.removeOverridedCond(excs);
      this.setVar("cond_override", Long.toString(this._exceptions));
   }

   public static void addHennaListener(HennaListener listener) {
      if (!HENNA_LISTENERS.contains(listener)) {
         HENNA_LISTENERS.add(listener);
      }
   }

   public static void removeHennaListener(HennaListener listener) {
      HENNA_LISTENERS.remove(listener);
   }

   public void addEquipmentListener(EquipmentListener listener) {
      if (!this._equipmentListeners.contains(listener)) {
         this._equipmentListeners.add(listener);
      }
   }

   public void removeEquipmentListener(EquipmentListener listener) {
      this._equipmentListeners.remove(listener);
   }

   public static void addGlobalEquipmentListener(EquipmentListener listener) {
      if (!GLOBAL_EQUIPMENT_LISTENERS.contains(listener)) {
         GLOBAL_EQUIPMENT_LISTENERS.add(listener);
      }
   }

   public static void removeGlobalEquipmentListener(EquipmentListener listener) {
      GLOBAL_EQUIPMENT_LISTENERS.remove(listener);
   }

   public void addTransformListener(TransformListener listener) {
      if (!this._transformListeners.contains(listener)) {
         this._transformListeners.add(listener);
      }
   }

   public void removeTransformListener(TransformListener listener) {
      this._transformListeners.remove(listener);
   }

   public void addProfessionChangeListener(ProfessionChangeListener listener) {
      if (!this._professionChangeListeners.contains(listener)) {
         this._professionChangeListeners.add(listener);
      }
   }

   public void removeProfessionChangeListener(ProfessionChangeListener listener) {
      this._professionChangeListeners.remove(listener);
   }

   public static void addGlobalProfessionChangeListener(ProfessionChangeListener listener) {
      if (!GLOBAL_PROFESSION_CHANGE_LISTENERS.contains(listener)) {
         GLOBAL_PROFESSION_CHANGE_LISTENERS.add(listener);
      }
   }

   public static void removeGlobalProfessionChangeListener(ProfessionChangeListener listener) {
      GLOBAL_PROFESSION_CHANGE_LISTENERS.remove(listener);
   }

   public void addEventListener(EventListener listener) {
      this._eventListeners.add(listener);
   }

   public void removeEventListener(EventListener listener) {
      this._eventListeners.remove(listener);
   }

   public void removeEventListener(Class<? extends EventListener> clazz) {
      Iterator<EventListener> it = this._eventListeners.iterator();

      while(it.hasNext()) {
         EventListener event = it.next();
         if (event.getClass() == clazz) {
            it.remove();
         }
      }
   }

   public Collection<EventListener> getEventListeners() {
      return this._eventListeners;
   }

   public void enterMovieMode() {
      this.setTarget(null);
      this.stopMove(null);
      this.setIsInvul(true);
      this.setIsImmobilized(true);
      this.sendPacket(CameraMode.FIRST_PERSON);
   }

   public void leaveMovieMode() {
      if (!this.isGM()) {
         this.setIsInvul(false);
      }

      this.setIsImmobilized(false);
      this.sendPacket(CameraMode.THIRD_PERSON);
      this.sendPacket(NormalCamera.STATIC_PACKET);
   }

   public void specialCamera(GameObject target, int dist, int yaw, int pitch, int time, int duration, int turn, int rise, int widescreen, int unk) {
      this.sendPacket(new SpecialCamera(target.getObjectId(), dist, yaw, pitch, time, duration, turn, rise, widescreen, unk));
   }

   public void specialCamera(GameObject target, int dist, int yaw, int pitch, int time, int duration) {
      this.sendPacket(new SpecialCamera((Creature)target, dist, yaw, pitch, time, duration));
   }

   public int getPcBangPoints() {
      return this._pcBangPoints;
   }

   public void setPcBangPoints(int i) {
      int nextPoints = i < Config.MAX_PC_BANG_POINTS ? i : Config.MAX_PC_BANG_POINTS;
      boolean canAch = nextPoints > this._pcBangPoints;
      this._pcBangPoints = nextPoints;
      if (canAch) {
         this.getCounters().addAchivementInfo("pcPointAcquired", 0, (long)this._pcBangPoints, true, false, false);
      }
   }

   public long getLastMovePacket() {
      return this._lastMovePacket;
   }

   public void setLastMovePacket() {
      this._lastMovePacket = System.currentTimeMillis();
   }

   public int getUCKills() {
      return this.UCKills;
   }

   public void addKillCountUC() {
      ++this.UCKills;
   }

   public int getUCDeaths() {
      return this.UCDeaths;
   }

   public void addDeathCountUC() {
      ++this.UCDeaths;
   }

   public void cleanUCStats() {
      this.UCDeaths = 0;
      this.UCKills = 0;
   }

   public void setUCState(int state) {
      this.UCState = state;
   }

   public int getUCState() {
      return this.UCState;
   }

   public long getGamePoints() {
      return this._gamePoints;
   }

   public void setGamePoints(long gamePoints) {
      this._gamePoints = gamePoints >= Long.MAX_VALUE ? Long.MAX_VALUE : gamePoints;
   }

   public boolean isPKProtected(Player target) {
      if (Config.DISABLE_ATTACK_IF_LVL_DIFFERENCE_OVER != 0) {
         if (target.isInSameClan(this)
            || target.isInSameParty(this)
            || target.isInSameChannel(this)
            || target.isInOlympiadMode()
            || target.isInsideZone(ZoneId.SIEGE)
            || target.isInsideZone(ZoneId.PVP)
            || this.getClan() != null
               && target.getClan() != null
               && target.getClan().isAtWarWith(this.getClanId())
               && this.getClan().isAtWarWith(target.getClan().getId())) {
            return false;
         }

         if ((target.getPvpFlag() == 0 || target.getKarma() == 0) && target.getLevel() + Config.DISABLE_ATTACK_IF_LVL_DIFFERENCE_OVER < this.getLevel()
            || Config.DISABLE_ATTACK_IF_LVL_DIFFERENCE_OVER > 85) {
            return true;
         }
      }

      return false;
   }

   public int getRevision() {
      return this._client == null ? 0 : this._client.getRevision();
   }

   public int getKamalokaId() {
      return this._kamaID;
   }

   public void setKamalokaId(int instanceId) {
      this._kamaID = instanceId;
   }

   public void checkPlayer() {
      if (Config.SECURITY_SKILL_CHECK && !this.isTransformed() && !this.isInStance()) {
         boolean haveWrongSkills = false;

         for(Skill skill : this.getAllSkills()) {
            boolean wrongSkill = false;
            if (!SkillTreesParser.getInstance().isNotCheckSkill(this, skill.getId(), skill.getLevel())) {
               for(int skillId : SkillTreesParser.getInstance().getRestrictedSkills(this.getClassId())) {
                  if (skill.getId() == skillId) {
                     wrongSkill = true;
                     break;
                  }
               }

               if (wrongSkill) {
                  haveWrongSkills = true;
                  if (!Config.SECURITY_SKILL_CHECK_CLEAR) {
                     break;
                  }

                  this.removeSkill(skill);
               }
            }
         }

         if (haveWrongSkills) {
            if (Config.SECURITY_SKILL_CHECK_CLEAR) {
               this.sendPacket(new SkillList());
            }

            Util.handleIllegalPlayerAction(
               this, "Possible cheater with wrong skills! Name: " + this.getName() + " (" + this.getObjectId() + "), account: " + this.getAccountName()
            );
         }
      }
   }

   public AdminProtection getAdminProtection() {
      if (this._AdminProtection == null) {
         this._AdminProtection = new AdminProtection(this);
      }

      return this._AdminProtection;
   }

   public long getOnlineTime() {
      return this._onlineTime;
   }

   public long getTotalOnlineTime() {
      return this._onlineTime + (System.currentTimeMillis() - this.getOnlineBeginTime()) / 1000L;
   }

   public long getOnlineBeginTime() {
      return this._onlineBeginTime;
   }

   @Override
   public boolean isInCategory(CategoryType type) {
      return CategoryParser.getInstance().isInCategory(type, this.getClassId().getId());
   }

   @Override
   public int getId() {
      return 0;
   }

   public void startPcBangPointsTask() {
      if (Config.PC_BANG_ENABLED && Config.PC_BANG_INTERVAL > 0) {
         if (!Config.PC_BANG_ONLY_FOR_PREMIUM || this.hasPremiumBonus()) {
            if (this._pcCafePointsTask == null) {
               this._pcCafePointsTask = ThreadPoolManager.getInstance()
                  .scheduleAtFixedRate(new PcPointsTask(this), (long)(Config.PC_BANG_INTERVAL * 1000), (long)(Config.PC_BANG_INTERVAL * 1000));
            }
         }
      }
   }

   public void stopPcBangPointsTask() {
      try {
         if (this._pcCafePointsTask != null) {
            this._pcCafePointsTask.cancel(false);
         }

         this._pcCafePointsTask = null;
      } catch (Exception var2) {
      }
   }

   public boolean isPartyBanned() {
      return PunishmentManager.getInstance().checkPunishment(this.getClient(), PunishmentType.PARTY_BAN);
   }

   @Override
   public void teleToClosestTown() {
      this.teleToLocation(TeleportWhereType.TOWN, true);
   }

   public int getHoursInGame() {
      ++this._hoursInGame;
      return this._hoursInGame;
   }

   public int getHoursInGames() {
      return this._hoursInGame;
   }

   public void setOfflineMode(boolean val) {
      if (!val) {
         this.unsetVar("offline");
         this.unsetVar("offlineBuff");
      }

      this._offline = val;
   }

   public boolean isInOfflineMode() {
      return this._offline;
   }

   public void addQuickVar(String name, Object value) {
      if (this.quickVars.containsKey(name)) {
         this.quickVars.remove(name);
      }

      this.quickVars.put(name, value);
   }

   public String getQuickVarS(String name, String... defaultValue) {
      if (!this.quickVars.containsKey(name)) {
         return defaultValue.length > 0 ? defaultValue[0] : null;
      } else {
         return (String)this.quickVars.get(name);
      }
   }

   public boolean getQuickVarB(String name, boolean... defaultValue) {
      if (!this.quickVars.containsKey(name)) {
         return defaultValue.length > 0 ? defaultValue[0] : false;
      } else {
         return this.quickVars.get(name);
      }
   }

   public int getQuickVarI(String name, int... defaultValue) {
      if (!this.quickVars.containsKey(name)) {
         return defaultValue.length > 0 ? defaultValue[0] : -1;
      } else {
         return this.quickVars.get(name);
      }
   }

   public long getQuickVarL(String name, long... defaultValue) {
      if (!this.quickVars.containsKey(name)) {
         return defaultValue.length > 0 ? defaultValue[0] : -1L;
      } else {
         return this.quickVars.get(name);
      }
   }

   public Object getQuickVarO(String name, Object... defaultValue) {
      if (!this.quickVars.containsKey(name)) {
         return defaultValue.length > 0 ? defaultValue[0] : null;
      } else {
         return this.quickVars.get(name);
      }
   }

   public boolean containsQuickVar(String name) {
      return this.quickVars.containsKey(name);
   }

   public void deleteQuickVar(String name) {
      this.quickVars.remove(name);
   }

   public boolean isAutoPot(int id) {
      return this._autoPotTasks.keySet().contains(id);
   }

   public void setAutoPot(int id, Future<?> task, boolean add) {
      if (add) {
         this._autoPotTasks.put(id, task);
      } else {
         try {
            if (this._autoPotTasks != null) {
               this._autoPotTasks.get(id).cancel(true);
               this._autoPotTasks.remove(id);
            }
         } catch (Exception var5) {
         }
      }
   }

   private void cleanAutoPots() {
      if (!this._autoPotTasks.isEmpty()) {
         try {
            for(Future<?> task : this._autoPotTasks.values()) {
               if (task != null) {
                  task.cancel(true);
                  Object var4 = null;
               }
            }

            this._autoPotTasks.clear();
         } catch (Exception var3) {
         }
      }
   }

   public void sendConfirmDlg(OnAnswerListener listener, int time, String msg) {
      ConfirmDlg packet = new ConfirmDlg(SystemMessageId.S1.getId());
      packet.addString(msg);
      packet.addTime(time);
      this.ask(packet, listener);
   }

   public void ask(ConfirmDlg dlg, OnAnswerListener listener) {
      if (this._askDialog == null) {
         int rnd = Rnd.nextInt();
         this._askDialog = new ImmutablePair<>(rnd, listener);
         dlg.addRequesterId(rnd);
         this.sendPacket(dlg);
      }
   }

   public Pair<Integer, OnAnswerListener> getAskListener(boolean clear) {
      if (!clear) {
         return this._askDialog;
      } else {
         Pair<Integer, OnAnswerListener> ask = this._askDialog;
         this._askDialog = null;
         return ask;
      }
   }

   public boolean hasDialogAskActive() {
      return this._askDialog != null;
   }

   public boolean isSellingBuffs() {
      return this._isSellingBuffs;
   }

   public void setIsSellingBuffs(boolean val) {
      this._isSellingBuffs = val;
   }

   public List<SellBuffHolder> getSellingBuffs() {
      if (this._sellingBuffs == null) {
         this._sellingBuffs = new ArrayList<>();
      }

      return this._sellingBuffs;
   }

   public void setCleftKill(int killPoint) {
      this._cleftKills += killPoint;
   }

   public int getCleftKills() {
      return this._cleftKills;
   }

   public void setCleftDeath(int deathPoint) {
      this._cleftDeaths += deathPoint;
   }

   public int getCleftDeaths() {
      return this._cleftDeaths;
   }

   public void setCleftKillTower(int killPoint) {
      this._cleftKillTowers += killPoint;
   }

   public int getCleftKillTowers() {
      return this._cleftKillTowers;
   }

   public void setCleftCat(boolean cat) {
      this._cleftCat = cat;
   }

   public boolean isCleftCat() {
      return this._cleftCat;
   }

   public void cleanCleftStats() {
      this._cleftKills = 0;
      this._cleftDeaths = 0;
      this._cleftKillTowers = 0;
      this._cleftCat = false;
   }

   public MatchingRoom getMatchingRoom() {
      return this._matchingRoom;
   }

   public void setMatchingRoom(MatchingRoom matchingRoom) {
      this._matchingRoom = matchingRoom;
      if (matchingRoom == null) {
         this._matchingRoomWindowOpened = false;
      }
   }

   public boolean isMatchingRoomWindowOpened() {
      return this._matchingRoomWindowOpened;
   }

   public void setMatchingRoomWindowOpened(boolean b) {
      this._matchingRoomWindowOpened = b;
   }

   @Override
   public Iterator<Player> iterator() {
      return Collections.singleton(this).iterator();
   }

   @Override
   public void broadCast(GameServerPacket... packet) {
      this.sendPacket(packet);
   }

   @Override
   public int getMemberCount() {
      return 1;
   }

   @Override
   public Player getGroupLeader() {
      return this;
   }

   public int getPing() {
      return this._ping;
   }

   public void setPing(int ping) {
      this._ping = ping;
   }

   public int getLectureMark() {
      return this._lectureMark;
   }

   public void setLectureMark(int lectureMark) {
      this._lectureMark = lectureMark;
   }

   public PetitionMainGroup getPetitionGroup() {
      return this._petitionGroup;
   }

   public void setPetitionGroup(PetitionMainGroup petitionGroup) {
      this._petitionGroup = petitionGroup;
   }

   public void hidePrivateStores() {
      ArrayList<GameServerPacket> pls = new ArrayList<>();

      for(Player player : World.getInstance().getAroundPlayers(this)) {
         if (player.isInStoreNow()) {
            pls.add(new DeleteObject(player));
         }
      }

      this.sendPacket(pls);
      pls.clear();
   }

   public void restorePrivateStores() {
      for(Player player : World.getInstance().getAroundPlayers(this)) {
         if (player.isInStoreNow()) {
            player.broadcastInfo();
         }
      }
   }

   public void resetHidePlayer() {
      for(Player player : World.getInstance().getAroundPlayers(this)) {
         if (player.getNotShowTraders()) {
            this.sendInfo(player);
         }
      }
   }

   public void addLoadedImage(int id) {
      this._loadedImages.add(id);
   }

   public boolean wasImageLoaded(int id) {
      return this._loadedImages.contains(id);
   }

   public int getLoadedImagesSize() {
      return this._loadedImages.size();
   }

   public String getSkillName(Skill skill) {
      return this.getLang() != null && !this.getLang().equalsIgnoreCase("en") ? skill.getNameRu() : skill.getNameEn();
   }

   public String getItemName(Item item) {
      return this.getLang() != null && !this.getLang().equalsIgnoreCase("en") ? item.getNameRu() : item.getNameEn();
   }

   public String getSummonName(Summon summon) {
      return summon.getSummonName(this, summon);
   }

   public String getNpcName(NpcTemplate npc) {
      return this.getLang() != null && !this.getLang().equalsIgnoreCase("en") ? npc.getNameRu() : npc.getName();
   }

   public String getNpcTitle(NpcTemplate npc) {
      return this.getLang() != null && !this.getLang().equalsIgnoreCase("en") ? npc.getTitleRu() : npc.getTitle();
   }

   public String getEventName(int eventId) {
      AbstractFightEvent event = FightEventParser.getInstance().getEvent(eventId);
      if (event == null) {
         return "";
      } else {
         return this.getLang() != null && !this.getLang().equalsIgnoreCase("en") ? event.getNameRu() : event.getNameEn();
      }
   }

   public String getEventDescr(int eventId) {
      AbstractFightEvent event = FightEventParser.getInstance().getEvent(eventId);
      if (event == null) {
         return "";
      } else {
         return this.getLang() != null && !this.getLang().equalsIgnoreCase("en") ? event.getDescriptionRu() : event.getDescriptionEn();
      }
   }

   public void updateNpcNames() {
      for(Npc npc : World.getInstance().getAroundNpc(this)) {
         npc.broadcastPacket(new NpcInfo.Info(npc, this));
      }
   }

   public NevitSystem getNevitSystem() {
      return this._nevitSystem;
   }

   public void sendVoteSystemInfo() {
      if (Config.ALLOW_RECO_BONUS_SYSTEM) {
         this.sendPacket(new ExVoteSystemInfo(this));
      }
   }

   public void isntAfk() {
      this._lastNotAfkTime = System.currentTimeMillis();
   }

   public long getLastNotAfkTime() {
      return this._lastNotAfkTime;
   }

   public FightEventGameRoom getFightEventGameRoom() {
      return this._fightEventGameRoom;
   }

   public void setFightEventGameRoom(FightEventGameRoom room) {
      this._fightEventGameRoom = room;
   }

   public void resetReuse() {
      this._reuseTimeStampsSkills.clear();
      this._reuseTimeStampsItems.clear();
   }

   public void requestCheckBot() {
      BotCheckManager.BotCheckQuestion question = BotCheckManager.getInstance().generateRandomQuestion();
      int qId = question.getId();
      String qDescr = question.getDescr(this.getLang());
      this.sendConfirmDlg(new BotCheckAnswerListner(this, qId), Config.ASK_ANSWER_DELAY * 60000, qDescr);
      this.startAbnormalEffect(AbnormalEffect.HOLD_2);
      this.getAI().setIntention(CtrlIntention.IDLE, this);
      this.setIsParalyzed(true);
      this._botCheckTask = ThreadPoolManager.getInstance().schedule(new CheckBotTask(this), (long)(Config.ASK_ANSWER_DELAY * 60000));
   }

   public void stopBotCheckTask() {
      try {
         if (this._botCheckTask != null) {
            this._botCheckTask.cancel(true);
            this._botCheckTask = null;
         }
      } catch (Exception var2) {
      }
   }

   public void increaseBotRating() {
      int bot_points = this.getBotRating();
      if (bot_points + 1 < Config.MAX_BOT_POINTS) {
         this.setBotRating(bot_points + 1);
      }
   }

   public void decreaseBotRating() {
      int bot_points = this.getBotRating();
      if (bot_points - 1 <= Config.MINIMAL_BOT_RATING_TO_BAN) {
         Util.handleIllegalPlayerAction(this, "" + ServerStorage.getInstance().getString(this.getLang(), "BotCheck.PUNISH_MSG"));
         if (Config.ANNOUNCE_AUTO_BOT_BAN) {
            Announcements.getInstance().announceToAll("Player " + this.getName() + " jailed for botting!");
         }
      } else {
         this.setBotRating(bot_points - 1);
         if (Config.ON_WRONG_QUESTION_KICK) {
            this.logout(false);
         }
      }
   }

   public void setBotRating(int rating) {
      this._botRating = rating;
   }

   public int getBotRating() {
      return this._botRating;
   }

   public void startHpPotionTask() {
      if (Config.AUTO_POINTS_SYSTEM) {
         int potionId = this.getVarInt("autoHpItemId", 0);
         ItemInstance hpPotion = this.getInventory().getItemByItemId(potionId);
         if (hpPotion == null) {
            this.setVar("useAutoHpPotions@", 0);
            if (potionId > 0) {
               ServerMessage msg = new ServerMessage("Menu.STRING_AUTO_POINTS_REQUIRED", this.getLang());
               msg.add(this.getItemName(ItemsParser.getInstance().getTemplate(potionId)));
               this.sendMessage(msg.toString());
            }
         } else {
            if (this._hpPotionTask == null) {
               long reuseDelay = (long)hpPotion.getReuseDelay();
               if (reuseDelay >= 10000L) {
                  reuseDelay += 3000L;
               } else if (reuseDelay < 1000L) {
                  reuseDelay = 1000L;
               }

               this._hpPotionTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new HpPotionTask(this), 1000L, reuseDelay);
               this.sendMessage(new ServerMessage("Menu.STRING_AUTO_HP_START", this.getLang()).toString());
            }
         }
      }
   }

   public void stopHpPotionTask() {
      if (this._hpPotionTask != null) {
         this._hpPotionTask.cancel(false);
      }

      this._hpPotionTask = null;
      this.setVar("useAutoHpPotions@", 0);
      this.sendMessage(new ServerMessage("Menu.STRING_AUTO_HP_STOP", this.getLang()).toString());
   }

   public boolean getUseAutoHpPotions() {
      return this.getVarB("useAutoHpPotions@");
   }

   public void startMpPotionTask() {
      if (Config.AUTO_POINTS_SYSTEM) {
         int potionId = this.getVarInt("autoMpItemId", 0);
         ItemInstance mpPotion = this.getInventory().getItemByItemId(potionId);
         if (mpPotion == null) {
            this.setVar("useAutoMpPotions@", 0);
            if (potionId > 0) {
               ServerMessage msg = new ServerMessage("Menu.STRING_AUTO_POINTS_REQUIRED", this.getLang());
               msg.add(this.getItemName(ItemsParser.getInstance().getTemplate(potionId)));
               this.sendMessage(msg.toString());
            }
         } else {
            if (this._mpPotionTask == null) {
               long reuseDelay = (long)mpPotion.getReuseDelay();
               if (reuseDelay >= 10000L) {
                  reuseDelay += 3000L;
               } else if (reuseDelay < 1000L) {
                  reuseDelay = 1000L;
               }

               this._mpPotionTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new MpPotionTask(this), 1000L, reuseDelay);
               this.sendMessage(new ServerMessage("Menu.STRING_AUTO_MP_START", this.getLang()).toString());
            }
         }
      }
   }

   public void stopMpPotionTask() {
      if (this._mpPotionTask != null) {
         this._mpPotionTask.cancel(false);
      }

      this._mpPotionTask = null;
      this.setVar("useAutoMpPotions@", 0);
      this.sendMessage(new ServerMessage("Menu.STRING_AUTO_MP_STOP", this.getLang()).toString());
   }

   public boolean getUseAutoMpPotions() {
      return this.getVarB("useAutoMpPotions@");
   }

   public void startCpPotionTask() {
      if (Config.AUTO_POINTS_SYSTEM) {
         int potionId = this.getVarInt("autoCpItemId", 0);
         ItemInstance cpPotion = this.getInventory().getItemByItemId(potionId);
         if (cpPotion == null) {
            this.setVar("useAutoCpPotions@", 0);
            if (potionId > 0) {
               ServerMessage msg = new ServerMessage("Menu.STRING_AUTO_POINTS_REQUIRED", this.getLang());
               msg.add(this.getItemName(ItemsParser.getInstance().getTemplate(potionId)));
               this.sendMessage(msg.toString());
            }
         } else {
            if (this._cpPotionTask == null) {
               long reuseDelay = (long)cpPotion.getReuseDelay();
               if (reuseDelay >= 10000L) {
                  reuseDelay += 3000L;
               } else if (reuseDelay < 1000L) {
                  reuseDelay = 1000L;
               }

               this._cpPotionTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new CpPotionTask(this), 1000L, reuseDelay);
               this.sendMessage(new ServerMessage("Menu.STRING_AUTO_CP_START", this.getLang()).toString());
            }
         }
      }
   }

   public void stopCpPotionTask() {
      if (this._cpPotionTask != null) {
         this._cpPotionTask.cancel(false);
      }

      this._cpPotionTask = null;
      this.setVar("useAutoCpPotions@", 0);
      this.sendMessage(new ServerMessage("Menu.STRING_AUTO_CP_STOP", this.getLang()).toString());
   }

   public boolean getUseAutoCpPotions() {
      return this.getVarB("useAutoCpPotions@");
   }

   public void startSoulPotionTask() {
      if (Config.AUTO_POINTS_SYSTEM) {
         int potionId = this.getVarInt("autoSoulItemId", 0);
         ItemInstance soulPotion = this.getInventory().getItemByItemId(potionId);
         if (soulPotion == null) {
            this.setVar("useAutoSoulPotions@", 0);
            if (potionId > 0) {
               ServerMessage msg = new ServerMessage("Menu.STRING_AUTO_POINTS_REQUIRED", this.getLang());
               msg.add(this.getItemName(ItemsParser.getInstance().getTemplate(potionId)));
               this.sendMessage(msg.toString());
            }
         } else {
            if (this._soulPotionTask == null) {
               long reuseDelay = (long)soulPotion.getReuseDelay();
               if (reuseDelay >= 10000L) {
                  reuseDelay += 3000L;
               } else if (reuseDelay < 1000L) {
                  reuseDelay = 1000L;
               }

               this._soulPotionTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new SoulPotionTask(this), 1000L, reuseDelay);
               this.sendMessage(new ServerMessage("Menu.STRING_AUTO_CP_START", this.getLang()).toString());
            }
         }
      }
   }

   public void stopSoulPotionTask() {
      if (this._soulPotionTask != null) {
         this._soulPotionTask.cancel(false);
      }

      this._soulPotionTask = null;
      this.setVar("useAutoSoulPotions@", 0);
      this.sendMessage(new ServerMessage("Menu.STRING_AUTO_CP_STOP", this.getLang()).toString());
   }

   public boolean getUseAutoSoulPotions() {
      return this.getVarB("useAutoSoulPotions@");
   }

   public int getChatMsg() {
      return this._chatMsg;
   }

   public void setChatMsg(int value) {
      this._chatMsg = value;
      if (value > 0 && value <= Config.CHAT_MSG_ANNOUNCE) {
         ServerMessage msg = new ServerMessage("CustomChat.LAST_MSG", this.getLang());
         msg.add(value);
         this.sendMessage(msg.toString());
      } else if (value == 0) {
         this.sendMessage(new ServerMessage("CustomChat.LIMIT", this.getLang()).toString());
      }
   }

   public int getCustomChatStatus() {
      return (int)this.getStat().calcStat(Stats.CHAT_STATUS, 0.0, null, null);
   }

   public void checkChatMessages() {
      if (Config.ALLOW_CUSTOM_CHAT) {
         Calendar temp = Calendar.getInstance();
         temp.set(11, 6);
         temp.set(12, 30);
         temp.set(13, 0);
         temp.set(14, 0);
         long count = (long)Math.round((float)((System.currentTimeMillis() - this._lastAccess) / 1000L / 86400L));
         if (count == 0L && this._lastAccess < temp.getTimeInMillis() && System.currentTimeMillis() > temp.getTimeInMillis()) {
            ++count;
         }

         if (count > 0L) {
            this.restartChatMessages();
         }
      }
   }

   public void restartChatMessages() {
      this.setChatMsg(this.hasPremiumBonus() ? Config.CHAT_MSG_PREMIUM : Config.CHAT_MSG_SIMPLE);
   }

   @Override
   public int getMaxLoad() {
      return (int)this.calcStat(
         Stats.WEIGHT_LIMIT, Math.floor(BaseStats.CON.calcBonus(this) * 69000.0 * Config.ALT_WEIGHT_LIMIT * this.getPremiumBonus().getWeight()), this, null
      );
   }

   @Override
   public int getBonusWeightPenalty() {
      return (int)this.calcStat(Stats.WEIGHT_PENALTY, 1.0, this, null);
   }

   @Override
   public int getCurrentLoad() {
      return this.getInventory().getTotalWeight();
   }

   public boolean isControllingFakePlayer() {
      return this._fakePlayerUnderControl != null;
   }

   public FakePlayer getPlayerUnderControl() {
      return this._fakePlayerUnderControl;
   }

   public void setPlayerUnderControl(FakePlayer fakePlayer) {
      this._fakePlayerUnderControl = fakePlayer;
   }

   public void setFakePlayer(boolean isFakePlayer) {
      this._fakePlayer = isFakePlayer;
   }

   @Override
   public boolean isFakePlayer() {
      return this._fakePlayer;
   }

   public void setFakeLocation(FakeLocTemplate loc) {
      this._fakeLocation = loc;
   }

   public void setFakeTerritoryLocation(FakePassiveLocTemplate loc) {
      this._fakePassiveLocation = loc;
   }

   public FakeLocTemplate getFakeLocation() {
      return this._fakeLocation;
   }

   public FakePassiveLocTemplate getFakeTerritory() {
      return this._fakePassiveLocation;
   }

   public PlayerGroup getPlayerGroup() {
      if (this.getParty() != null) {
         return (PlayerGroup)(this.getParty().getCommandChannel() != null ? this.getParty().getCommandChannel() : this.getParty());
      } else {
         return this;
      }
   }

   public void restoreDailyRewards() {
      String checkHwid = DailyRewardManager.getInstance().isHwidCheck() ? this.getHWID() : this.getIPAddress();
      boolean found = false;

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT * FROM daily_rewards WHERE " + DailyRewardManager.getInstance().getColumnCheck() + "=?");
      ) {
         statement.setString(1, checkHwid);

         try (ResultSet rset = statement.executeQuery()) {
            while(rset.next()) {
               found = true;
            }
         }
      } catch (Exception var61) {
         _log.log(Level.WARNING, "Failed restore daily rewards.", (Throwable)var61);
      }

      if (!found) {
         DailyRewardManager.getInstance().addNewDailyPlayer(this);
      }
   }

   public Collection<PlayerTaskTemplate> getActiveDailyTasks() {
      return this._activeTasks.values();
   }

   public void addActiveDailyTasks(int id, PlayerTaskTemplate template) {
      this._activeTasks.put(id, template);
   }

   public void removeActiveDailyTasks(int id) {
      this._activeTasks.remove(id);
   }

   public int getActiveTasks(String type) {
      int amount = 0;

      for(PlayerTaskTemplate template : this._activeTasks.values()) {
         if (template != null && template.getSort().equalsIgnoreCase(type)) {
            ++amount;
         }
      }

      return amount;
   }

   public PlayerTaskTemplate getDailyTaskTemplate(int id) {
      return this._activeTasks.get(id);
   }

   public int getLastDailyTasks() {
      return this._lastDailyTasks;
   }

   public void setLastDailyTasks(int amount) {
      this._lastDailyTasks = amount;
   }

   public int getLastWeeklyTasks() {
      return this._lastWeeklyTasks;
   }

   public void setLastWeeklyTasks(int amount) {
      this._lastWeeklyTasks = amount;
   }

   public int getLastMonthTasks() {
      return this._lastMonthTasks;
   }

   public void setLastMonthTasks(int amount) {
      this._lastMonthTasks = amount;
   }

   public void updateDailyCount(int dailyCount, int weeklyCount, int monthCount) {
      DailyTasksDAO.getInstance().updateDailyTasksCount(this, dailyCount, weeklyCount, monthCount);
   }

   public void addDailyTask(PlayerTaskTemplate template) {
      DailyTasksDAO.getInstance().addNewDailyTask(this, template);
   }

   public void updateDailyStatus(PlayerTaskTemplate template) {
      DailyTasksDAO.getInstance().updateTaskStatus(this, template);
   }

   public void updateDailyRewardStatus(PlayerTaskTemplate template) {
      DailyTasksDAO.getInstance().updateTaskRewardStatus(this, template);
   }

   public void removeDailyTask(int taskId) {
      DailyTasksDAO.getInstance().removeTask(this, taskId);
   }

   public void saveDailyTasks() {
      if (this.getLastDailyTasks() > 0 || this.getLastWeeklyTasks() > 0 || this.getLastMonthTasks() > 0) {
         this.updateDailyCount(this._lastDailyTasks, this._lastWeeklyTasks, this._lastMonthTasks);
      }

      if (this.getActiveDailyTasks() != null) {
         for(PlayerTaskTemplate taskTemplate : this.getActiveDailyTasks()) {
            if (!taskTemplate.isComplete()) {
               int params = 0;
               String var4 = taskTemplate.getType();
               switch(var4) {
                  case "Farm":
                     params = taskTemplate.getCurrentNpcCount();
                     break;
                  case "Pvp":
                     params = taskTemplate.getCurrentPvpCount();
                     break;
                  case "Pk":
                     params = taskTemplate.getCurrentPkCount();
                     break;
                  case "Olympiad":
                     params = taskTemplate.getCurrentOlyMatchCount();
               }

               DailyTasksDAO.getInstance().updateTaskParams(this, taskTemplate.getId(), params);
            }
         }
      }
   }

   public void cleanDailyTasks() {
      for(PlayerTaskTemplate template : this._activeTasks.values()) {
         if (template != null && template.getSort().equalsIgnoreCase("daily")) {
            this._activeTasks.remove(template.getId());
         }
      }

      this._lastDailyTasks = 0;
      DailyTasksDAO.getInstance().restoreDailyTasksCount(this);
   }

   public void cleanWeeklyTasks() {
      for(PlayerTaskTemplate template : this._activeTasks.values()) {
         if (template != null && template.getSort().equalsIgnoreCase("weekly")) {
            this._activeTasks.remove(template.getId());
         }
      }

      this._lastWeeklyTasks = 0;
      DailyTasksDAO.getInstance().restoreWeeklyTasksCount(this);
   }

   public void cleanMonthTasks() {
      for(PlayerTaskTemplate template : this._activeTasks.values()) {
         if (template != null && template.getSort().equalsIgnoreCase("month")) {
            this._activeTasks.remove(template.getId());
         }
      }

      this._lastMonthTasks = 0;
      DailyTasksDAO.getInstance().restoreMonthTasksCount(this);
   }

   public boolean canJoinParty(Player inviter) {
      Request request = this.getRequest();
      if (request != null && request.isProcessingRequest()) {
         return false;
      } else if (BlockedList.isBlocked(this, inviter) || this.getMessageRefusal()) {
         return false;
      } else if (!this.isInParty()
         && !this.isPartyBanned()
         && this.isVisibleFor(inviter)
         && !this.isCursedWeaponEquipped()
         && !inviter.isCursedWeaponEquipped()) {
         if ((AerialCleftEvent.getInstance().isStarted() || AerialCleftEvent.getInstance().isRewarding())
            && AerialCleftEvent.getInstance().isPlayerParticipant(this.getObjectId())) {
            return false;
         } else if (inviter.getReflectionId() != this.getReflectionId() && inviter.getReflectionId() != 0 && this.getReflectionId() != 0) {
            return false;
         } else if (inviter.isInOlympiadMode() || this.isInOlympiadMode()) {
            return false;
         } else if (this.getTeam() != 0) {
            return false;
         } else {
            return !this.isInFightEvent() || this.getFightEvent().canJoinParty(inviter, this);
         }
      } else {
         return false;
      }
   }

   public PlayerListenerList getListeners() {
      if (this._listeners == null) {
         synchronized(this) {
            if (this._listeners == null) {
               this._listeners = new PlayerListenerList(this);
            }
         }
      }

      return (PlayerListenerList)this._listeners;
   }

   public void restoreTradeList() {
      String var = this.getVar("selllist");
      if (var != null) {
         String[] items = var.split(":");

         for(String item : items) {
            if (!item.equals("")) {
               String[] values = item.split(";");
               if (values.length >= 3) {
                  int oId = Integer.parseInt(values[0]);
                  long count = Long.parseLong(values[1]);
                  long price = Long.parseLong(values[2]);
                  ItemInstance itemToSell = this.getInventory().getItemByObjectId(oId);
                  if (count >= 1L && itemToSell != null) {
                     if (count > itemToSell.getCount()) {
                        count = itemToSell.getCount();
                     }

                     this.getSellList().addItem(itemToSell.getObjectId(), count, price);
                  }
               }
            }
         }

         var = this.getVar("sellstorename");
         if (var != null) {
            this.getSellList().setTitle(var);
         }

         if (this.getVar("storemode") != null) {
            this.getSellList().setPackaged(Integer.parseInt(this.getVar("storemode")) == 8);
         }
      }

      var = this.getVar("buylist");
      if (var != null) {
         String[] items = var.split(":");

         for(String item : items) {
            if (!item.equals("")) {
               String[] values = item.split(";");
               if (values.length >= 3) {
                  ItemInstance itemToSell = this.getInventory().getItemByItemId(Integer.parseInt(values[0]));
                  if (itemToSell != null) {
                     int[] elemDefAttr = new int[]{0, 0, 0, 0, 0, 0};

                     for(byte i = 0; i < 6; ++i) {
                        elemDefAttr[i] = itemToSell.getElementDefAttr(i);
                     }

                     this.getBuyList()
                        .addItemByItemId(
                           Integer.parseInt(values[0]),
                           itemToSell.getEnchantLevel(),
                           Long.parseLong(values[1]),
                           Long.parseLong(values[2]),
                           itemToSell.getAttackElementType(),
                           itemToSell.getAttackElementPower(),
                           elemDefAttr
                        );
                  }
               }
            }
         }

         var = this.getVar("buystorename");
         if (var != null) {
            this.getBuyList().setTitle(var);
         }
      }

      var = this.getVar("createlist");
      if (var != null) {
         String[] items = var.split(":");

         for(String item : items) {
            if (!item.equals("")) {
               String[] values = item.split(";");
               if (values.length >= 2) {
                  int recId = Integer.parseInt(values[0]);
                  long price = Long.parseLong(values[1]);
                  this.getManufactureItems().put(recId, new ManufactureItemTemplate(recId, price));
               }
            }
         }

         var = this.getVar("manufacturename");
         if (var != null) {
            this.setStoreName(var);
         }
      }
   }

   public void saveTradeList() {
      StringBuilder tradeListBuilder = new StringBuilder();
      if (this._sellList == null) {
         this.unsetVar("selllist");
      } else {
         for(TradeItem i : this.getSellList().getItems()) {
            tradeListBuilder.append(i.getObjectId()).append(";").append(i.getCount()).append(";").append(i.getPrice()).append(":");
         }

         this.setVar("selllist", tradeListBuilder.toString(), -1L);
         tradeListBuilder.delete(0, tradeListBuilder.length());
         if (this.getSellList().getTitle() != null) {
            this.setVar("sellstorename", this.getSellList().getTitle(), -1L);
         }
      }

      if (this._buyList == null) {
         this.unsetVar("buylist");
      } else {
         for(TradeItem i : this.getBuyList().getItems()) {
            tradeListBuilder.append(i.getItem().getId()).append(";").append(i.getCount()).append(";").append(i.getPrice()).append(":");
         }

         this.setVar("buylist", tradeListBuilder.toString(), -1L);
         tradeListBuilder.delete(0, tradeListBuilder.length());
         if (this.getBuyList().getTitle() != null) {
            this.setVar("buystorename", this.getBuyList().getTitle(), -1L);
         }
      }

      if (this._manufactureItems != null && !this._manufactureItems.isEmpty()) {
         for(ManufactureItemTemplate i : this.getManufactureItems().values()) {
            tradeListBuilder.append(i.getRecipeId()).append(";").append(i.getCost()).append(":");
         }

         this.setVar("createlist", tradeListBuilder.toString(), -1L);
         if (this.getStoreName() != null) {
            this.setVar("manufacturename", this.getStoreName(), -1L);
         }
      } else {
         this.unsetVar("createlist");
      }
   }

   @Override
   public PremiumBonus getPremiumBonus() {
      return this._bonus;
   }

   @Override
   public boolean hasPremiumBonus() {
      return this._bonus.isActive();
   }

   public void startTempHeroTask(long expirTime) {
      if (this._tempHeroTask == null) {
         long taskTime = expirTime - System.currentTimeMillis();
         this._tempHeroTask = ThreadPoolManager.getInstance().schedule(new TempHeroTask(this), taskTime);
      }
   }

   public boolean isTempHero() {
      return this._tempHeroTask != null;
   }

   private void stopTempHeroTask() {
      try {
         if (this._tempHeroTask != null) {
            this._tempHeroTask.cancel(false);
         }

         this._tempHeroTask = null;
      } catch (Exception var2) {
      }
   }

   public void startPremiumTask(long expirTime) {
      if (Config.USE_PREMIUMSERVICE) {
         if (this._premiumTask == null) {
            PremiumBonus bonus = this.getPremiumBonus();
            if (bonus.isActive()) {
               PremiumTemplate template = PremiumAccountsParser.getInstance().getPremiumTemplate(bonus.getPremiumId());
               if (template != null) {
                  long taskTime = 0L;
                  if (template.isOnlineType()) {
                     taskTime = template.getTime() * 1000L - expirTime;
                     this.setPremiumOnlineTime();
                  } else {
                     taskTime = expirTime - System.currentTimeMillis();
                  }

                  this._premiumTask = ThreadPoolManager.getInstance().schedule(new PremuimAccountTask(this), taskTime);
               }
            }
         }
      }
   }

   public void stopPremiumTask() {
      try {
         if (this._premiumTask != null) {
            this._premiumTask.cancel(false);
         }

         this._premiumTask = null;
      } catch (Exception var2) {
      }
   }

   private void setPremiumOnlineTime() {
      this._premiumOnlineTime = System.currentTimeMillis();
   }

   public long getPremiumOnlineTime() {
      return this._premiumOnlineTime;
   }

   public EnchantParams getEnchantParams() {
      return this._enchantParams;
   }

   public void addVisual(String type, int skinId) {
      switch(type) {
         case "Weapon":
            this._weaponSkins.add(skinId);
            break;
         case "Armor":
            this._armorSkins.add(skinId);
            break;
         case "Shield":
            this._shieldSkins.add(skinId);
            break;
         case "Cloak":
            this._cloakSkins.add(skinId);
            break;
         case "Hair":
            this._hairSkins.add(skinId);
      }

      CharacterVisualDAO.getInstance().add(this, type, skinId);
   }

   private void updateVisual(String type, int active, int skinId) {
      CharacterVisualDAO.getInstance().update(this, type, active, skinId);
   }

   public List<Integer> getWeaponSkins() {
      return this._weaponSkins;
   }

   public void addWeaponSkin(int id) {
      this._weaponSkins.add(id);
   }

   public List<Integer> getArmorSkins() {
      return this._armorSkins;
   }

   public void addArmorSkin(int id) {
      this._armorSkins.add(id);
   }

   public List<Integer> getShieldSkins() {
      return this._shieldSkins;
   }

   public void addShieldSkin(int id) {
      this._shieldSkins.add(id);
   }

   public List<Integer> getCloakSkins() {
      return this._cloakSkins;
   }

   public List<Integer> getHairSkins() {
      return this._hairSkins;
   }

   public void addCloakSkin(int id) {
      this._cloakSkins.add(id);
   }

   public void addHairSkin(int id) {
      this._hairSkins.add(id);
   }

   public int getActiveWeaponSkin() {
      return this._activeWeaponSkin;
   }

   public int getActiveArmorSkin() {
      return this._activeArmorSkin;
   }

   public int getActiveShieldSkin() {
      return this._activeShieldSkin;
   }

   public int getActiveCloakSkin() {
      return this._activeCloakSkin;
   }

   public int getActiveHairSkin() {
      return this._activeHairSkin;
   }

   public int getActiveMaskSkin() {
      return this._activeMaskSkin;
   }

   public void setActiveWeaponSkin(int skinId, boolean separate) {
      if (this._activeWeaponSkin != 0) {
         this.updateVisual("Weapon", 0, this._activeWeaponSkin);
      }

      this._activeWeaponSkin = skinId;
      if (separate) {
         this.updateVisual("Weapon", 1, this._activeWeaponSkin);
      }
   }

   public void setActiveArmorSkin(int skinId, boolean separate) {
      if (this._activeArmorSkin != 0) {
         DressArmorTemplate visual = DressArmorParser.getInstance().getArmor(this._activeArmorSkin);
         if (visual != null) {
            if (visual.getShieldId() > 0) {
               this.unVisualShieldSkin(false);
            }

            if (visual.getCloakId() > 0) {
               this.unVisualCloakSkin(false);
            }

            if (visual.getHatId() > 0) {
               if (visual.getSlot() == 3) {
                  this.unVisualFaceSkin(false);
               } else {
                  this.unVisualHairSkin(false);
               }
            }
         }

         this.updateVisual("Armor", 0, this._activeArmorSkin);
      }

      this._activeArmorSkin = skinId;
      this.updateVisual("Armor", 1, this._activeArmorSkin);
   }

   public void setActiveShieldSkin(int skinId, boolean separate) {
      if (this._activeShieldSkin != 0) {
         this.updateVisual("Shield", 0, this._activeShieldSkin);
      }

      this._activeShieldSkin = skinId;
      if (separate) {
         this.updateVisual("Shield", 1, this._activeShieldSkin);
      }
   }

   public void setActiveCloakSkin(int skinId, boolean separate) {
      if (this._activeCloakSkin != 0) {
         this.updateVisual("Cloak", 0, this._activeCloakSkin);
      }

      this._activeCloakSkin = skinId;
      if (separate) {
         this.updateVisual("Cloak", 1, this._activeCloakSkin);
      }
   }

   public void setActiveHairSkin(int skinId, boolean separate) {
      if (this._activeHairSkin != 0) {
         this.updateVisual("Hair", 0, this._activeHairSkin);
      }

      this._activeHairSkin = skinId;
      if (separate) {
         this.updateVisual("Hair", 1, this._activeHairSkin);
      }
   }

   public void setActiveMaskSkin(int skinId, boolean separate) {
      if (this._activeMaskSkin != 0) {
         this.updateVisual("Hair", 0, this._activeMaskSkin);
      }

      this._activeMaskSkin = skinId;
      if (separate) {
         this.updateVisual("Hair", 1, this._activeMaskSkin);
      }
   }

   public boolean hasHairForVisualEquipped() {
      DressHatTemplate hair = DressHatParser.getInstance().getHat(this._activeHairSkin);
      Item template = ItemsParser.getInstance().getTemplate(hair.getHatId());
      if (template == null) {
         return false;
      } else {
         DressArmorTemplate set = DressArmorParser.getInstance().getArmor(this._activeArmorSkin);
         if (set != null && set.getHatId() > 0 && !set.isCheckEquip()) {
            return true;
         } else {
            int paperdoll = Inventory.getPaperdollIndex(template.getBodyPart());
            if (paperdoll < 0) {
               return false;
            } else {
               ItemInstance item = this.getInventory().getPaperdollItem(2);
               if (item != null) {
                  Item itemTemp = ItemsParser.getInstance().getTemplate(item.getId());
                  if (itemTemp.getBodyPart() != template.getBodyPart()) {
                     return false;
                  }
               }

               ItemInstance slot = this.getInventory().getPaperdollItem(paperdoll);
               if (slot == null) {
                  return false;
               } else {
                  return template.getBodyPart() != 524288 || this.getActiveMaskSkin() == 0;
               }
            }
         }
      }
   }

   public boolean hasFaceForVisualEquipped() {
      DressHatTemplate hair = DressHatParser.getInstance().getHat(this._activeMaskSkin);
      Item template = ItemsParser.getInstance().getTemplate(hair.getHatId());
      if (template == null) {
         return false;
      } else {
         DressArmorTemplate set = DressArmorParser.getInstance().getArmor(this._activeArmorSkin);
         if (set != null && set.getHatId() > 0 && !set.isCheckEquip()) {
            return true;
         } else {
            int paperdoll = Inventory.getPaperdollIndex(template.getBodyPart());
            if (paperdoll < 0) {
               return false;
            } else {
               ItemInstance item = this.getInventory().getPaperdollItem(3);
               if (item != null) {
                  Item itemTemp = ItemsParser.getInstance().getTemplate(item.getId());
                  if (itemTemp.getBodyPart() != template.getBodyPart()) {
                     return false;
                  }
               }

               ItemInstance slot = this.getInventory().getPaperdollItem(paperdoll);
               return slot != null;
            }
         }
      }
   }

   public boolean hasArmorForVisualEquipped() {
      ItemInstance chestItem = this.getInventory().getPaperdollItem(6);
      ItemInstance legsItem = this.getInventory().getPaperdollItem(11);
      ItemInstance glovesItem = this.getInventory().getPaperdollItem(10);
      ItemInstance feetItem = this.getInventory().getPaperdollItem(12);
      if (chestItem != null && glovesItem != null && feetItem != null) {
         return legsItem != null || chestItem.getItem().getBodyPart() == 32768;
      } else {
         return false;
      }
   }

   public boolean hasWeaponForVisualEquipped() {
      ItemInstance weapon = this.getInventory().getPaperdollItem(5);
      if (weapon == null) {
         return false;
      } else {
         DressWeaponTemplate weapon_data = DressWeaponParser.getInstance().getWeapon(this._activeWeaponSkin);
         return weapon.getItemType().toString().equals(weapon_data.getType());
      }
   }

   public boolean hasShieldForVisualEquipped() {
      DressArmorTemplate set = DressArmorParser.getInstance().getArmor(this._activeArmorSkin);
      if (set != null && set.getShieldId() > 0 && !set.isCheckEquip()) {
         return true;
      } else {
         ItemInstance shield = this.getInventory().getPaperdollItem(7);
         return shield != null;
      }
   }

   public boolean hasCloakForVisualEquipped() {
      DressArmorTemplate set = DressArmorParser.getInstance().getArmor(this._activeArmorSkin);
      if (set != null && set.getCloakId() > 0 && !set.isCheckEquip()) {
         return true;
      } else {
         ItemInstance cloak = this.getInventory().getPaperdollItem(23);
         return cloak != null;
      }
   }

   public void unVisualArmorSkin(boolean separate) {
      if (this._activeArmorSkin != 0) {
         if (separate) {
            this.updateVisual("Armor", 0, this._activeArmorSkin);
         }

         this._activeArmorSkin = 0;
      }
   }

   public void unVisualWeaponSkin(boolean separate) {
      if (this._activeWeaponSkin != 0) {
         if (separate) {
            this.updateVisual("Weapon", 0, this._activeWeaponSkin);
         }

         this._activeWeaponSkin = 0;
      }
   }

   public void unVisualShieldSkin(boolean separate) {
      if (this._activeShieldSkin != 0) {
         if (separate) {
            this.updateVisual("Shield", 0, this._activeShieldSkin);
         }

         this._activeShieldSkin = 0;
      }
   }

   public void unVisualCloakSkin(boolean separate) {
      if (this._activeCloakSkin != 0) {
         if (separate) {
            this.updateVisual("Cloak", 0, this._activeCloakSkin);
         }

         this._activeCloakSkin = 0;
      }
   }

   public void unVisualHairSkin(boolean separate) {
      if (this._activeHairSkin != 0) {
         if (separate) {
            this.updateVisual("Hair", 0, this._activeHairSkin);
         }

         this._activeHairSkin = 0;
      }
   }

   public void unVisualFaceSkin(boolean separate) {
      if (this._activeMaskSkin != 0) {
         if (separate) {
            this.updateVisual("Hair", 0, this._activeMaskSkin);
         }

         this._activeMaskSkin = 0;
      }
   }

   public List<PlayerScheme> getBuffSchemes() {
      return this._buffSchemes;
   }

   public PlayerScheme getBuffSchemeById(int id) {
      for(PlayerScheme scheme : this._buffSchemes) {
         if (scheme.getSchemeId() == id) {
            return scheme;
         }
      }

      return null;
   }

   public PlayerScheme getBuffSchemeByName(String name) {
      for(PlayerScheme scheme : this._buffSchemes) {
         if (scheme.getName().equals(name)) {
            return scheme;
         }
      }

      return null;
   }

   public void setFacebookProfile(FacebookProfile facebookProfile) {
      this._facebookProfile = facebookProfile;
   }

   public boolean hasFacebookProfile() {
      return this._facebookProfile != null;
   }

   @Nullable
   public FacebookProfile getFacebookProfile() {
      return this._facebookProfile;
   }

   public boolean isInSameParty(Player target) {
      return this.getParty() != null && target != null && target.getParty() != null && this.getParty() == target.getParty();
   }

   public boolean isInSameChannel(Player target) {
      return this.getParty() != null
         && target != null
         && target.getParty() != null
         && this.getParty().getCommandChannel() != null
         && target.getParty().getCommandChannel() != null
         && this.getParty().getCommandChannel() == target.getParty().getCommandChannel();
   }

   public boolean isInSameClan(Player target) {
      return this.getClan() != null && target != null && target.getClan() != null && this.getClanId() == target.getClanId();
   }

   public final boolean isInSameAlly(Player target) {
      return this.getAllyId() != 0 && target != null && target.getAllyId() != 0 && this.getAllyId() == target.getAllyId();
   }

   public boolean isInTwoSidedWar(Player target) {
      Clan aClan = this.getClan();
      Clan tClan = target.getClan();
      return aClan != null && tClan != null && aClan.isAtWarWith(tClan.getId()) && tClan.isAtWarWith(aClan.getId());
   }

   @Override
   public void broadcastCharInfo() {
      this.broadcastUserInfo(false);
   }

   public void broadcastUserInfo(boolean force) {
      try {
         this.sendUserInfo(force);
         if (Config.BROADCAST_CHAR_INFO_INTERVAL == 0L) {
            force = true;
         }

         if (force) {
            if (this._broadcastCharInfoTask != null && !this._broadcastCharInfoTask.isDone()) {
               this._broadcastCharInfoTask.cancel(false);
               this._broadcastCharInfoTask = null;
            }

            this.broadcastCharInfoImpl();
            return;
         }

         if (this._broadcastCharInfoTask != null) {
            return;
         }

         this._broadcastCharInfoTask = ThreadPoolManager.getInstance().schedule(new Player.BroadcastCharInfoTask(), Config.BROADCAST_CHAR_INFO_INTERVAL);
      } catch (Exception var3) {
      }
   }

   @Override
   public void broadcastCharInfoImpl() {
      GameServerPacket exCi = new ExBrExtraUserInfo(this);
      GameServerPacket dominion = !TerritoryWarManager.getInstance().isTWInProgress()
            || !TerritoryWarManager.getInstance().checkIsRegistered(-1, this.getObjectId())
               && !TerritoryWarManager.getInstance().checkIsRegistered(-1, this.getClan())
         ? null
         : new ExDominionWarStart(this);

      for(Player player : World.getInstance().getAroundPlayers(this)) {
         if (player.getNotShowTraders() && this.isInStoreNow()) {
            player.sendPacket(new DeleteObject(this));
         } else {
            player.sendPacket((GameServerPacket)(this.getPoly().isMorphed() ? new NpcInfoPoly(this) : new CharInfo(this, player)), exCi);
            player.sendPacket(RelationChanged.update(player, this, player));
            if (dominion != null) {
               player.sendPacket(dominion);
            }
         }
      }
   }

   public void broadcastCharInfoAround() {
      try {
         if (this._broadcastCharInfoTask != null && !this._broadcastCharInfoTask.isDone()) {
            this._broadcastCharInfoTask.cancel(false);
            this._broadcastCharInfoTask = null;
         }

         this._broadcastCharInfoTask = ThreadPoolManager.getInstance().schedule(new Player.BroadcastCharInfoAround(), Config.BROADCAST_CHAR_INFO_INTERVAL);
      } catch (Exception var2) {
      }
   }

   private void broadcastCharAroundImpl() {
      for(Player player : World.getInstance().getAroundPlayers(this)) {
         if (player != null && player.isOnline()) {
            this.sendPacket(new CharInfo(player, this));
         }
      }
   }

   private void sendUserInfoImpl() {
      this.sendPacket(new UserInfo(this), new ExBrExtraUserInfo(this));
      if (TerritoryWarManager.getInstance().isTWInProgress()
         && (
            TerritoryWarManager.getInstance().checkIsRegistered(-1, this.getObjectId())
               || TerritoryWarManager.getInstance().checkIsRegistered(-1, this.getClan())
         )) {
         this.sendPacket(new ExDominionWarStart(this));
      }
   }

   public void sendUserInfo() {
      this.sendUserInfo(false);
   }

   public void sendUserInfo(boolean force) {
      if (!this.isFakePlayer() && !this._entering) {
         try {
            if (Config.USER_INFO_INTERVAL == 0L || force) {
               if (this._userInfoTask != null && !this._userInfoTask.isDone()) {
                  this._userInfoTask.cancel(false);
                  this._userInfoTask = null;
               }

               this.sendUserInfoImpl();
               return;
            }

            if (this._userInfoTask != null) {
               return;
            }

            this._userInfoTask = ThreadPoolManager.getInstance().schedule(new Player.UserInfoTask(), Config.USER_INFO_INTERVAL);
         } catch (Exception var3) {
         }
      }
   }

   public void broadcastRelationChanged() {
      for(Player player : World.getInstance().getAroundPlayers(this)) {
         player.sendPacket(RelationChanged.update(player, this, player));
         if (this.hasSummon()) {
            player.sendPacket(RelationChanged.update(player, this.getSummon(), player));
         }
      }
   }

   public boolean isFriend(Player target) {
      if (target == this) {
         return true;
      } else if (this.getDuelState() == 1 && this.getDuelId() == target.getActingPlayer().getDuelId()) {
         Duel duel = DuelManager.getInstance().getDuel(this.getDuelId());
         if (duel.isPartyDuel()) {
            Party partyA = this.getParty();
            Party partyB = target.getParty();
            if (partyA != null && partyA.getMembers().contains(target)) {
               return true;
            } else {
               return partyB != null && partyB.getMembers().contains(this);
            }
         } else {
            return false;
         }
      } else if (!this.isInSameParty(target) && !this.isInSameChannel(target)) {
         for(AbstractFightEvent e : this.getFightEvents()) {
            if (e != null) {
               return !e.canAttack(target, this);
            }
         }

         if (!target.isInsideZone(ZoneId.FUN_PVP)
            && this.isInsideZone(ZoneId.PVP)
            && target.isInsideZone(ZoneId.PVP)
            && !this.isInsideZone(ZoneId.SIEGE)
            && !target.isInsideZone(ZoneId.SIEGE)) {
            return false;
         } else if (this.isInOlympiadMode() && target.isInOlympiadMode() && this.getOlympiadGameId() == target.getOlympiadGameId()) {
            return false;
         } else if (this.isInTwoSidedWar(target)) {
            return false;
         } else if (!this.isInSameClan(target) && !this.isInSameAlly(target)) {
            boolean isInsideSiegeZone = this.isInsideZone(ZoneId.SIEGE);
            if (isInsideSiegeZone && this.isInSiege() && this.getSiegeState() != 0 && target.getSiegeState() != 0) {
               Siege siege = SiegeManager.getInstance().getSiege(this.getX(), this.getY(), this.getZ());
               if (siege != null) {
                  if (siege.checkIsDefender(this.getClan()) && siege.checkIsDefender(target.getClan())) {
                     return true;
                  }

                  if (siege.checkIsAttacker(this.getClan()) && siege.checkIsAttacker(target.getClan())) {
                     return true;
                  }

                  return false;
               }
            }

            if (this.isInsideZone(ZoneId.PVP) && target.isInsideZone(ZoneId.PVP) && this.isInsideZone(ZoneId.SIEGE) && target.isInsideZone(ZoneId.SIEGE)) {
               return false;
            } else if ((target.getPvpFlag() > 0 || target.getKarma() > 0)
               && !this.isInSameParty(target)
               && !this.isInSameChannel(target)
               && !this.isInSameClan(target)
               && !this.isInSameAlly(target)) {
               return false;
            } else {
               return !target.isInsideZone(ZoneId.FUN_PVP)
                  || this.isInSameParty(target)
                  || this.isInSameChannel(target)
                  || this.isInSameClan(target)
                  || this.isInSameAlly(target);
            }
         } else {
            return true;
         }
      } else {
         return true;
      }
   }

   public int getVipLevel() {
      return this._vipLevel;
   }

   public void setVipLevel(int level) {
      this._vipLevel = level;
      this.getPremiumBonus().setVipTemplate(this._vipLevel);
   }

   public long getVipPoints() {
      return this._vipPoints;
   }

   public void refreshVipPoints() {
      this._vipPoints = 0L;
   }

   public void setVipPoints(long point) {
      if (point > VipManager.getInstance().getMaxPoints()) {
         this._vipPoints = VipManager.getInstance().getMaxPoints();
      } else {
         this._vipPoints = point;
      }

      if (this.getPremiumBonus().getVipTemplate().getId() < VipManager.getInstance().getMaxLevel()) {
         VipTemplate tmp = VipManager.getInstance().getVipLevel(this._vipLevel + 1);
         if (tmp != null && this._vipPoints >= tmp.getPoints()) {
            this.setVipLevel(tmp.getId());
            if (this._vipPoints > tmp.getPoints()) {
               this._vipPoints = tmp.getPoints() - this._vipPoints;
            } else {
               this._vipPoints = 0L;
            }
         }
      }
   }

   @Override
   public double getColRadius() {
      if (this.isMounted() && this.getMountNpcId() > 0) {
         return NpcsParser.getInstance().getTemplate(this.getMountNpcId()).getfCollisionRadius();
      } else if (this.isTransformed()) {
         return this.getTransformation().getCollisionRadius(this);
      } else {
         return this.getAppearance().getSex() ? this.getBaseTemplate().getFCollisionRadiusFemale() : this.getBaseTemplate().getfCollisionRadius();
      }
   }

   @Override
   public double getColHeight() {
      if (this.isMounted() && this.getMountNpcId() > 0) {
         return NpcsParser.getInstance().getTemplate(this.getMountNpcId()).getfCollisionHeight();
      } else if (this.isTransformed()) {
         return this.getTransformation().getCollisionHeight(this);
      } else {
         return this.getAppearance().getSex() ? this.getBaseTemplate().getFCollisionHeightFemale() : this.getBaseTemplate().getfCollisionHeight();
      }
   }

   public void setServitorShare(Map<Stats, Double> map) {
      this._servitorShare = map;
   }

   public final double getServitorShareBonus(Stats stat) {
      return this._servitorShare == null ? 1.0 : this._servitorShare.get(stat);
   }

   public List<Integer> getRevengeList() {
      return this._revengeList;
   }

   public void addRevengeId(int charId) {
      if (this._revengeList.size() < 10) {
         if (!this._revengeList.contains(charId)) {
            this._revengeList.add(charId);
         }

         this.getRevengeMark();
      }
   }

   public void removeRevengeId(int objectId) {
      String line = "";
      int amount = 0;

      for(int charId : this._revengeList) {
         if (charId != objectId) {
            ++amount;
            line = line + "" + charId + "";
            if (amount < this._revengeList.size() - 1) {
               line = line + ";";
            }
         }
      }

      this._revengeList.clear();
      if (!line.isEmpty()) {
         String[] targets = line.split(";");

         for(String charId : targets) {
            this._revengeList.add(Integer.parseInt(charId));
         }
      }

      this.getRevengeMark();
   }

   public void clenUpRevengeList() {
      this._revengeList.clear();
   }

   public void getRevengeMark() {
      if (this._revengeList != null && !this._revengeList.isEmpty()) {
         this.sendPacket(new ShowTutorialMark(false, 1002));
      }
   }

   public boolean isRevengeActive() {
      return this._isRevengeActive;
   }

   public void setRevengeActive(boolean active) {
      this._isRevengeActive = active;
   }

   public String saveRevergeList() {
      if (this._revengeList != null && !this._revengeList.isEmpty()) {
         String line = "";
         int amount = 0;

         for(int charId : this._revengeList) {
            ++amount;
            line = line + "" + charId + "";
            if (amount < this._revengeList.size()) {
               line = line + ";";
            }
         }

         return line;
      } else {
         return "";
      }
   }

   public void loadRevergeList(String line) {
      if (line != null && !line.isEmpty()) {
         String[] targets = line.split(";");

         for(String charId : targets) {
            this._revengeList.add(Integer.parseInt(charId));
         }
      }
   }

   @Override
   public void addInfoObject(GameObject object) {
      this.sendInfoFrom(object);
   }

   @Override
   public void removeInfoObject(GameObject object) {
      super.removeInfoObject(object);
      if (object instanceof AirShipInstance) {
         if (((AirShipInstance)object).getCaptainId() != 0 && ((AirShipInstance)object).getCaptainId() != this.getObjectId()) {
            this.sendPacket(new DeleteObject(((AirShipInstance)object).getCaptainId()));
         }

         if (((AirShipInstance)object).getHelmObjectId() != 0) {
            this.sendPacket(new DeleteObject(((AirShipInstance)object).getHelmObjectId()));
         }
      }

      this.sendPacket(new DeleteObject(object));
   }

   public final void refreshInfos() {
      for(GameObject object : World.getInstance().getAroundObjects(this)) {
         if (!object.isPlayer() || !object.getActingPlayer().inObserverMode()) {
            this.sendInfoFrom(object);
         }
      }
   }

   private final void sendInfoFrom(GameObject object) {
      if (object.getPoly().isMorphed() && object.getPoly().getPolyType().equals("item")) {
         this.sendPacket(new SpawnItem(object));
      } else {
         object.sendInfo(this);
         if (object instanceof Creature) {
            Creature obj = (Creature)object;
            if (obj.hasAI()) {
               obj.getAI().describeStateToPlayer(this);
            }
         }
      }
   }

   public void setSittingObject(StaticObjectInstance object) {
      this._sittingObject = object;
   }

   public StaticObjectInstance getSittingObject() {
      return this._sittingObject;
   }

   public Recommendation getRecommendation() {
      return this._recommendation;
   }

   public void addBannedAction(String info) {
      this._bannedActions.add(info);
   }

   public List<String> getBannedActions() {
      return this._bannedActions;
   }

   public boolean canUsePreviewTask() {
      return this._previewDoneTask == null || this._previewDoneTask.isDone();
   }

   public void setRemovePreviewTask() {
      this._previewDoneTask = ThreadPoolManager.getInstance().schedule(new RemoveWearItemsTask(this), 6000L);
   }

   public Location getFallingLoc() {
      return this._fallingLoc;
   }

   public void setFallingLoc(Location loc) {
      if (!this.isFalling()) {
         this._fallingLoc = loc;
      }
   }

   public void addCBTeleport(int id, PcTeleportTemplate data) {
      this._communtyTeleports.put(id, data);
   }

   public Collection<PcTeleportTemplate> getCBTeleports() {
      return this._communtyTeleports.values();
   }

   public void removeCBTeleport(int id) {
      this._communtyTeleports.remove(id);
   }

   public PcTeleportTemplate getCBTeleport(int id) {
      return this._communtyTeleports.get(id);
   }

   public Location getSaveLoc() {
      return this._saveLoc;
   }

   public void setSaveLoc(Location loc) {
      this._saveLoc = loc;
   }

   public Map<Integer, Integer> getAchievements(int category) {
      Map<Integer, Integer> result = new HashMap<>();

      for(Entry<Integer, Integer> entry : this._achievementLevels.entrySet()) {
         int achievementId = entry.getKey();
         int achievementLevel = entry.getValue();
         AchiveTemplate ach = AchievementManager.getInstance().getAchievement(achievementId, Math.max(1, achievementLevel));
         if (ach != null && ach.getCategoryId() == category) {
            result.put(achievementId, achievementLevel);
         }
      }

      return result;
   }

   public Map<Integer, Integer> getAchievements() {
      return this._achievementLevels;
   }

   public void loadAchivements() {
      String achievements = this.getVar("achievements");
      if (achievements != null && !achievements.isEmpty()) {
         String[] levels = achievements.split(";");

         for(String ach : levels) {
            String[] lvl = ach.split(",");
            if (AchievementManager.getInstance().getMaxLevel(Integer.parseInt(lvl[0])) > 0) {
               this._achievementLevels.put(Integer.parseInt(lvl[0]), Integer.parseInt(lvl[1]));
            }
         }
      }

      for(int achievementId : AchievementManager.getInstance().getAchievementIds()) {
         if (!this._achievementLevels.containsKey(achievementId)) {
            this._achievementLevels.put(achievementId, 0);
         }
      }

      AchievementsDAO.getInstance().restoreAchievements(this);
   }

   public void saveAchivements() {
      String str = "";

      for(Entry<Integer, Integer> a : this._achievementLevels.entrySet()) {
         str = str + a.getKey() + "," + a.getValue() + ";";
      }

      this.setVar("achievements", str);
      AchievementsDAO.getInstance().saveAchievements(this);
   }

   public AchievementCounters getCounters() {
      return this._achievementCounters;
   }

   @Override
   public AutoFarmOptions getFarmSystem() {
      return this._autoFarmSystem;
   }

   public Location getBookmarkLocation() {
      return this._bookmarkLocation;
   }

   public void setBookmarkLocation(Location loc) {
      this._bookmarkLocation = loc;
   }

   public int getPledgeItemId() {
      return this._pledgeItemId;
   }

   public void setPledgeItemId(int itemId) {
      this._pledgeItemId = itemId;
   }

   public void setPledgePrice(long price) {
      this._pledgePrice = price;
   }

   public long getPledgePrice() {
      return this._pledgePrice;
   }

   public void setSearchforAcademy(boolean search) {
      this._isInAcademyList = search;
   }

   public boolean isInSearchOfAcademy() {
      return this._isInAcademyList;
   }

   public void sendItemList(boolean show) {
      ItemInstance[] items = this.getInventory().getItems();
      int allSize = items.length;
      int questItemsSize = 0;
      int agathionItemsSize = 0;

      for(ItemInstance item : items) {
         if (item != null) {
            if (item.isQuestItem()) {
               ++questItemsSize;
            }

            if (item.isEnergyItem()) {
               ++agathionItemsSize;
            }
         }
      }

      this.sendPacket(new ItemList(this.getInventory(), allSize - questItemsSize, items, show));
      this.sendPacket(new ExQuestItemList(this.getInventory(), questItemsSize, items));
      if (agathionItemsSize > 0) {
         this.sendPacket(new ExBrAgathionEnergyInfo(agathionItemsSize, items));
      }
   }

   public int[] getUserVisualSlots() {
      int visualId = this.getVarInt("visualBuff", 0);
      boolean isCostumeBuff = visualId > 0 && !this.isTransformed();
      boolean allowBlock = this.isInOlympiadMode() && Config.BLOCK_VISUAL_OLY;
      if (this.getActiveWeaponSkin() != 0 && this.hasWeaponForVisualEquipped() && !allowBlock) {
         DressWeaponTemplate weapon = DressWeaponParser.getInstance().getWeapon(this.getActiveWeaponSkin());
         this._userInfoSlots[0] = weapon != null ? weapon.getId() : this.getInventory().getPaperdollItemDisplayId(5);
         this._userInfoSlots[9] = weapon != null && weapon.isAllowEnchant() ? this.getEnchantEffect() : 0;
         this._userInfoSlots[10] = weapon != null && weapon.isAllowAugment() ? this.getInventory().getPaperdollAugmentationId(5) : 0;
      } else if (Config.ALLOW_VISUAL_ARMOR_COMMAND && !allowBlock) {
         ItemInstance item = this.getInventory().getPaperdollItem(5);
         if (item != null && item.getVisualItemId() > 0) {
            DressWeaponTemplate weapon = DressWeaponParser.getInstance().getWeapon(item.getVisualItemId());
            this._userInfoSlots[0] = weapon != null ? weapon.getId() : 0;
            this._userInfoSlots[9] = weapon != null && weapon.isAllowEnchant() ? this.getEnchantEffect() : 0;
            this._userInfoSlots[10] = weapon != null && weapon.isAllowAugment() ? this.getInventory().getPaperdollAugmentationId(5) : 0;
         } else {
            this._userInfoSlots[0] = this.getInventory().getPaperdollItemDisplayId(5);
            this._userInfoSlots[9] = this.getEnchantEffect();
            this._userInfoSlots[10] = this.getInventory().getPaperdollAugmentationId(5);
         }
      } else {
         this._userInfoSlots[0] = this.getInventory().getPaperdollItemDisplayId(5);
         this._userInfoSlots[9] = this.getEnchantEffect();
         this._userInfoSlots[10] = this.getInventory().getPaperdollAugmentationId(5);
      }

      if (this.getActiveShieldSkin() != 0 && this.hasShieldForVisualEquipped() && !allowBlock) {
         DressShieldTemplate shield = DressShieldParser.getInstance().getShield(this.getActiveShieldSkin());
         this._userInfoSlots[1] = shield != null ? shield.getShieldId() : this.getInventory().getPaperdollItemDisplayId(7);
      } else {
         this._userInfoSlots[1] = Config.ALLOW_VISUAL_ARMOR_COMMAND && !allowBlock
            ? this.getInventory().getPaperdollVisualItemId(7)
            : this.getInventory().getPaperdollItemDisplayId(7);
      }

      if (isCostumeBuff && !allowBlock) {
         DressArmorTemplate tpl = DressArmorParser.getInstance().getArmor(visualId);
         this._userInfoSlots[2] = tpl != null ? tpl.getGloves() : this.getInventory().getPaperdollItemDisplayId(10);
         this._userInfoSlots[3] = tpl != null ? tpl.getChest() : this.getInventory().getPaperdollItemDisplayId(6);
         this._userInfoSlots[4] = tpl != null ? tpl.getLegs() : this.getInventory().getPaperdollItemDisplayId(11);
         this._userInfoSlots[5] = tpl != null ? tpl.getFeet() : this.getInventory().getPaperdollItemDisplayId(12);
      } else if (this.getActiveArmorSkin() != 0 && this.hasArmorForVisualEquipped() && !allowBlock) {
         DressArmorTemplate visual = DressArmorParser.getInstance().getArmor(this.getActiveArmorSkin());
         this._userInfoSlots[2] = visual != null ? visual.getGloves() : this.getInventory().getPaperdollItemDisplayId(10);
         this._userInfoSlots[3] = visual != null ? visual.getChest() : this.getInventory().getPaperdollItemDisplayId(6);
         this._userInfoSlots[4] = visual != null ? visual.getLegs() : this.getInventory().getPaperdollItemDisplayId(11);
         this._userInfoSlots[5] = visual != null ? visual.getFeet() : this.getInventory().getPaperdollItemDisplayId(12);
      } else {
         this._userInfoSlots[2] = Config.ALLOW_VISUAL_ARMOR_COMMAND && !allowBlock
            ? this.getInventory().getPaperdollVisualItemId(10)
            : this.getInventory().getPaperdollItemDisplayId(10);
         this._userInfoSlots[3] = Config.ALLOW_VISUAL_ARMOR_COMMAND && !allowBlock
            ? this.getInventory().getPaperdollVisualItemId(6)
            : this.getInventory().getPaperdollItemDisplayId(6);
         this._userInfoSlots[4] = Config.ALLOW_VISUAL_ARMOR_COMMAND && !allowBlock
            ? this.getInventory().getPaperdollVisualItemId(11)
            : this.getInventory().getPaperdollItemDisplayId(11);
         this._userInfoSlots[5] = Config.ALLOW_VISUAL_ARMOR_COMMAND && !allowBlock
            ? this.getInventory().getPaperdollVisualItemId(12)
            : this.getInventory().getPaperdollItemDisplayId(12);
      }

      if (isCostumeBuff && !allowBlock) {
         DressArmorTemplate tpl = DressArmorParser.getInstance().getArmor(visualId);
         this._userInfoSlots[6] = tpl != null ? tpl.getCloakId() : this.getInventory().getPaperdollItemDisplayId(23);
      }

      if (this.getActiveCloakSkin() != 0 && this.hasCloakForVisualEquipped() && !allowBlock) {
         DressCloakTemplate cloak = DressCloakParser.getInstance().getCloak(this.getActiveCloakSkin());
         this._userInfoSlots[6] = cloak != null ? cloak.getCloakId() : this.getInventory().getPaperdollItemDisplayId(23);
      } else {
         this._userInfoSlots[6] = Config.ALLOW_VISUAL_ARMOR_COMMAND && !allowBlock
            ? this.getInventory().getPaperdollVisualItemId(23)
            : this.getInventory().getPaperdollItemDisplayId(23);
      }

      if (this.getActiveHairSkin() != 0 && this.hasHairForVisualEquipped() && !allowBlock) {
         DressHatTemplate hair = DressHatParser.getInstance().getHat(this.getActiveHairSkin());
         this._userInfoSlots[7] = hair != null ? (hair.getSlot() == 2 ? hair.getHatId() : 0) : this.getInventory().getPaperdollItemDisplayId(2);
      } else {
         this._userInfoSlots[7] = Config.ALLOW_VISUAL_ARMOR_COMMAND && !allowBlock
            ? this.getInventory().getPaperdollVisualItemId(2)
            : this.getInventory().getPaperdollItemDisplayId(2);
      }

      if (this.getActiveMaskSkin() != 0 && this.hasFaceForVisualEquipped() && !allowBlock) {
         DressHatTemplate hair = DressHatParser.getInstance().getHat(this.getActiveMaskSkin());
         this._userInfoSlots[8] = hair != null ? (hair.getSlot() == 3 ? hair.getHatId() : 0) : this.getInventory().getPaperdollItemDisplayId(3);
      } else {
         this._userInfoSlots[8] = Config.ALLOW_VISUAL_ARMOR_COMMAND && !allowBlock
            ? this.getInventory().getPaperdollVisualItemId(3)
            : this.getInventory().getPaperdollItemDisplayId(3);
      }

      return this._userInfoSlots;
   }

   public int[] getCharVisualSlots(Player viewer) {
      int visualId = this.getVarInt("visualBuff", 0);
      boolean isCostumeBuff = visualId > 0 && !this.isTransformed();
      boolean allowBlock = this.isInOlympiadMode() && Config.BLOCK_VISUAL_OLY || viewer != null && viewer.getVarInt("visualBlock", 0) > 0;
      if (this.getActiveWeaponSkin() != 0 && this.hasWeaponForVisualEquipped() && !allowBlock) {
         DressWeaponTemplate weapon = DressWeaponParser.getInstance().getWeapon(this.getActiveWeaponSkin());
         this._charInfoSlots[0] = weapon != null ? weapon.getId() : this.getInventory().getPaperdollItemDisplayId(5);
         this._charInfoSlots[9] = weapon != null && weapon.isAllowEnchant() ? this.getEnchantEffect() : 0;
         this._charInfoSlots[10] = weapon != null && weapon.isAllowAugment() ? this.getInventory().getPaperdollAugmentationId(5) : 0;
      } else if (Config.ALLOW_VISUAL_ARMOR_COMMAND && !allowBlock) {
         ItemInstance item = this.getInventory().getPaperdollItem(5);
         if (item != null && item.getVisualItemId() > 0) {
            DressWeaponTemplate weapon = DressWeaponParser.getInstance().getWeapon(item.getVisualItemId());
            this._charInfoSlots[0] = weapon != null ? weapon.getId() : 0;
            this._charInfoSlots[9] = weapon != null && weapon.isAllowEnchant() ? this.getEnchantEffect() : 0;
            this._charInfoSlots[10] = weapon != null && weapon.isAllowAugment() ? this.getInventory().getPaperdollAugmentationId(5) : 0;
         } else {
            this._charInfoSlots[0] = this.getInventory().getPaperdollItemDisplayId(5);
            this._charInfoSlots[9] = this.getEnchantEffect();
            this._charInfoSlots[10] = this.getInventory().getPaperdollAugmentationId(5);
         }
      } else {
         this._charInfoSlots[0] = this.getInventory().getPaperdollItemDisplayId(5);
         this._charInfoSlots[9] = this.getEnchantEffect();
         this._charInfoSlots[10] = this.getInventory().getPaperdollAugmentationId(5);
      }

      if (this.getActiveShieldSkin() != 0 && this.hasShieldForVisualEquipped() && !allowBlock) {
         DressShieldTemplate shield = DressShieldParser.getInstance().getShield(this.getActiveShieldSkin());
         this._charInfoSlots[1] = shield != null ? shield.getShieldId() : this.getInventory().getPaperdollItemDisplayId(7);
      } else {
         this._charInfoSlots[1] = Config.ALLOW_VISUAL_ARMOR_COMMAND && !allowBlock
            ? this.getInventory().getPaperdollVisualItemId(7)
            : this.getInventory().getPaperdollItemDisplayId(7);
      }

      if (isCostumeBuff && !allowBlock) {
         DressArmorTemplate tpl = DressArmorParser.getInstance().getArmor(visualId);
         this._charInfoSlots[2] = tpl != null ? tpl.getGloves() : this.getInventory().getPaperdollItemDisplayId(10);
         this._charInfoSlots[3] = tpl != null ? tpl.getChest() : this.getInventory().getPaperdollItemDisplayId(6);
         this._charInfoSlots[4] = tpl != null ? tpl.getLegs() : this.getInventory().getPaperdollItemDisplayId(11);
         this._charInfoSlots[5] = tpl != null ? tpl.getFeet() : this.getInventory().getPaperdollItemDisplayId(12);
      } else if (this.getActiveArmorSkin() != 0 && this.hasArmorForVisualEquipped() && !allowBlock) {
         DressArmorTemplate visual = DressArmorParser.getInstance().getArmor(this.getActiveArmorSkin());
         this._charInfoSlots[2] = visual != null ? visual.getGloves() : this.getInventory().getPaperdollItemDisplayId(10);
         this._charInfoSlots[3] = visual != null ? visual.getChest() : this.getInventory().getPaperdollItemDisplayId(6);
         this._charInfoSlots[4] = visual != null ? visual.getLegs() : this.getInventory().getPaperdollItemDisplayId(11);
         this._charInfoSlots[5] = visual != null ? visual.getFeet() : this.getInventory().getPaperdollItemDisplayId(12);
      } else {
         this._charInfoSlots[2] = Config.ALLOW_VISUAL_ARMOR_COMMAND && !allowBlock
            ? this.getInventory().getPaperdollVisualItemId(10)
            : this.getInventory().getPaperdollItemDisplayId(10);
         this._charInfoSlots[3] = Config.ALLOW_VISUAL_ARMOR_COMMAND && !allowBlock
            ? this.getInventory().getPaperdollVisualItemId(6)
            : this.getInventory().getPaperdollItemDisplayId(6);
         this._charInfoSlots[4] = Config.ALLOW_VISUAL_ARMOR_COMMAND && !allowBlock
            ? this.getInventory().getPaperdollVisualItemId(11)
            : this.getInventory().getPaperdollItemDisplayId(11);
         this._charInfoSlots[5] = Config.ALLOW_VISUAL_ARMOR_COMMAND && !allowBlock
            ? this.getInventory().getPaperdollVisualItemId(12)
            : this.getInventory().getPaperdollItemDisplayId(12);
      }

      if (isCostumeBuff && !allowBlock) {
         DressArmorTemplate tpl = DressArmorParser.getInstance().getArmor(visualId);
         this._charInfoSlots[6] = tpl != null ? tpl.getCloakId() : this.getInventory().getPaperdollItemDisplayId(23);
      }

      if (this.getActiveCloakSkin() != 0 && this.hasCloakForVisualEquipped() && !allowBlock) {
         DressCloakTemplate cloak = DressCloakParser.getInstance().getCloak(this.getActiveCloakSkin());
         this._charInfoSlots[6] = cloak != null ? cloak.getCloakId() : this.getInventory().getPaperdollItemDisplayId(23);
      } else {
         this._charInfoSlots[6] = Config.ALLOW_VISUAL_ARMOR_COMMAND && !allowBlock
            ? this.getInventory().getPaperdollVisualItemId(23)
            : this.getInventory().getPaperdollItemDisplayId(23);
      }

      if (this.getActiveHairSkin() != 0 && this.hasHairForVisualEquipped() && !allowBlock) {
         DressHatTemplate hair = DressHatParser.getInstance().getHat(this.getActiveHairSkin());
         this._charInfoSlots[7] = hair != null ? (hair.getSlot() == 2 ? hair.getHatId() : 0) : this.getInventory().getPaperdollItemDisplayId(2);
      } else {
         this._charInfoSlots[7] = Config.ALLOW_VISUAL_ARMOR_COMMAND && !allowBlock
            ? this.getInventory().getPaperdollVisualItemId(2)
            : this.getInventory().getPaperdollItemDisplayId(2);
      }

      if (this.getActiveMaskSkin() != 0 && this.hasFaceForVisualEquipped() && !allowBlock) {
         DressHatTemplate hair = DressHatParser.getInstance().getHat(this.getActiveMaskSkin());
         this._charInfoSlots[8] = hair != null ? (hair.getSlot() == 3 ? hair.getHatId() : 0) : this.getInventory().getPaperdollItemDisplayId(3);
      } else {
         this._charInfoSlots[8] = Config.ALLOW_VISUAL_ARMOR_COMMAND && !allowBlock
            ? this.getInventory().getPaperdollVisualItemId(3)
            : this.getInventory().getPaperdollItemDisplayId(3);
      }

      return this._charInfoSlots;
   }

   public void startOnlineRewardTask(long time) {
      this.stopOnlineRewardTask();
      this._onlineRewardTask = ThreadPoolManager.getInstance().schedule(new OnlineRewardTask(this), time);
   }

   public void stopOnlineRewardTask() {
      try {
         if (this._onlineRewardTask != null) {
            this._onlineRewardTask.cancel(true);
            this._onlineRewardTask = null;
         }
      } catch (Exception var2) {
      }
   }

   public void startPunishmentTask(PunishmentTemplate template) {
      this.stopPunishmentTask();
      long lastTime = 0L;
      if (template.getExpirationTime() > 0L) {
         lastTime = template.getExpirationTime() - System.currentTimeMillis();
      }

      this._punishmentTask = ThreadPoolManager.getInstance().schedule(new PunishmentTask(this, template), lastTime);
   }

   public void stopPunishmentTask() {
      try {
         if (this._punishmentTask != null) {
            this._punishmentTask.cancel(true);
            this._punishmentTask = null;
         }
      } catch (Exception var2) {
      }
   }

   public void startJail() {
      this.setReflectionId(0);
      this.setGeoIndex(0);
      this.setIsIn7sDungeon(false);
      if (this.getFightEventGameRoom() != null) {
         FightEventManager.getInstance().unsignFromAllEvents(this);
      }

      if (this.getFightEvent() != null && this.getFightEvent().leaveEvent(this, false)) {
         this.sendMessage("You have left the event!");
      }

      if (OlympiadManager.getInstance().isRegisteredInComp(this)) {
         OlympiadManager.getInstance().removeDisconnectedCompetitor(this);
      }

      if (this.isSellingBuffs()) {
         this.unsetVar("offlineBuff");
      }

      if (this.isInOfflineMode()) {
         this.unsetVar("offline");
         this.unsetVar("storemode");
      }

      ThreadPoolManager.getInstance().schedule(new TeleportTask(this, JailZone.getLocationIn()), 2000L);
   }

   public void stopJail() {
      ThreadPoolManager.getInstance().schedule(new TeleportTask(this, JailZone.getLocationOut()), 2000L);
   }

   public long getLastAttackPacket() {
      return this._lastAttackPacket;
   }

   public void setLastAttackPacket() {
      this._lastAttackPacket = System.currentTimeMillis();
   }

   public long getLastRequestMagicPacket() {
      return this._lastRequestMagicPacket;
   }

   public void setLastRequestMagicPacket() {
      this._lastRequestMagicPacket = System.currentTimeMillis();
   }

   public class BroadcastCharInfoAround extends RunnableImpl {
      @Override
      public void runImpl() throws Exception {
         Player.this.broadcastCharAroundImpl();
         Player.this._broadcastCharInfoTask = null;
      }
   }

   public class BroadcastCharInfoTask extends RunnableImpl {
      @Override
      public void runImpl() throws Exception {
         Player.this.broadcastCharInfoImpl();
         Player.this._broadcastCharInfoTask = null;
      }
   }

   private class BroadcastStatusUpdateTask extends RunnableImpl {
      private BroadcastStatusUpdateTask() {
      }

      @Override
      public void runImpl() throws Exception {
         Player.this.broadcastStatusUpdateImpl();
         Player.this._broadcastStatusUpdateTask = null;
      }
   }

   protected static class SummonRequest {
      private Player _target = null;
      private Skill _skill = null;

      public void setTarget(Player destination, Skill skill) {
         this._target = destination;
         this._skill = skill;
      }

      public Player getTarget() {
         return this._target;
      }

      public Skill getSkill() {
         return this._skill;
      }
   }

   private class UpdateAbnormalEffectTask extends RunnableImpl {
      private UpdateAbnormalEffectTask() {
      }

      @Override
      public void runImpl() throws Exception {
         Player.this.broadcastUserInfo(false);
         Player.this._effectsUpdateTask = null;
      }
   }

   private class UpdateAndBroadcastStatusTask extends RunnableImpl {
      private final int _broadcastType;

      public UpdateAndBroadcastStatusTask(int broadcastType) {
         this._broadcastType = broadcastType;
      }

      @Override
      public void runImpl() throws Exception {
         Player.this.broadcastStatusImpl(this._broadcastType);
         Player.this._updateAndBroadcastStatusTask = null;
      }
   }

   private class UserInfoTask extends RunnableImpl {
      private UserInfoTask() {
      }

      @Override
      public void runImpl() throws Exception {
         Player.this.sendUserInfoImpl();
         Player.this._userInfoTask = null;
      }
   }
}
