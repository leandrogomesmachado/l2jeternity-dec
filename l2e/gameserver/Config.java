package l2e.gameserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import l2e.commons.net.IPSettings;
import l2e.commons.util.Files;
import l2e.commons.util.FloodProtectorConfig;
import l2e.commons.util.GameSettings;
import l2e.commons.util.Util;
import l2e.gameserver.data.parser.ExperienceParser;
import l2e.gameserver.model.service.BotFunctions;
import l2e.gameserver.model.service.academy.AcademyRewards;
import l2e.gameserver.model.service.autofarm.FarmSettings;
import org.apache.commons.lang3.StringUtils;
import org.nio.impl.SelectorConfig;

public final class Config {
   private static final Logger _log = Logger.getLogger(Config.class.getName());
   public static final String EOL = System.getProperty("line.separator");
   public static final String HITMAN_CONFIG = "./config/events/hitman_event.ini";
   public static final String UNDERGROUND_CONFIG_FILE = "./config/events/undergroundColiseum.ini";
   public static final String LEPRECHAUN_FILE = "./config/events/leprechaun_event.ini";
   public static final String AERIAL_CLEFT_FILE = "./config/events/aerialCleft.ini";
   public static final String FIGHT_EVENTS_FILE = "./config/events/fightEvents.ini";
   public static final String CHARACTER_CONFIG_FILE = "./config/main/character.ini";
   public static final String FEATURE_CONFIG_FILE = "./config/main/feature.ini";
   public static final String FORTSIEGE_CONFIGURATION_FILE = "./config/main/fortsiege.ini";
   public static final String GENERAL_CONFIG_FILE = "./config/main/general.ini";
   public static final String ID_CONFIG_FILE = "./config/main/idfactory.ini";
   public static final String NPC_CONFIG_FILE = "./config/main/npc.ini";
   public static final String PVP_CONFIG_FILE = "./config/main/pvp.ini";
   public static final String RATES_CONFIG_FILE = "./config/main/rates.ini";
   public static final String SIEGE_CONFIGURATION_FILE = "./config/main/siege.ini";
   public static final String TW_CONFIGURATION_FILE = "./config/main/territorywar.ini";
   public static final String FLOOD_PROTECTOR_FILE = "./config/main/floodprotector.ini";
   public static final String MMO_CONFIG_FILE = "./config/main/mmo.ini";
   public static final String OLYMPIAD_CONFIG_FILE = "./config/main/olympiad.ini";
   public static final String GRANDBOSS_CONFIG_FILE = "./config/main/grandboss.ini";
   public static final String GRACIASEEDS_CONFIG_FILE = "./config/main/graciaSeeds.ini";
   public static final String CHAT_FILTER_FILE = "./config/main/chatfilter.txt";
   public static final String BROADCAST_CHAT_FILTER_FILE = "./config/main/broadcastfilter.txt";
   public static final String SECURITY_CONFIG_FILE = "./config/main/security.ini";
   public static final String CH_SIEGE_FILE = "./config/main/clanhallSiege.ini";
   public static final String LANGUAGE_FILE = "./config/main/language.ini";
   public static final String VOICE_CONFIG_FILE = "./config/main/voicecommands.ini";
   public static final String CUSTOM_FILE = "./config/main/custom.ini";
   public static final String PREMIUM_CONFIG_FILE = "./config/main/premiumAccount.ini";
   public static final String COMMUNITY_BOARD_CONFIG_FILE = "./config/main/communityBoard.ini";
   public static final String ENCHANT_CONFIG_FILE = "./config/main/enchant.ini";
   public static final String ITEM_MALL_CONFIG_FILE = "./config/main/itemMall.ini";
   public static final String GEO_CONFIG_FILE = "./config/main/geodata.ini";
   public static final String CHAT_CONFIG_FILE = "./config/main/chat.ini";
   public static final String PERSONAL_FILE = "./config/main/personal.ini";
   public static final String PCBANG_CONFIG_FILE = "./config/mods/pcPoints.ini";
   public static final String WEDDING_CONFIG_FILE = "./config/mods/wedding.ini";
   public static final String OFFLINE_TRADE_CONFIG_FILE = "./config/mods/offline_trade.ini";
   public static final String DOUBLE_SESSIONS_CONFIG_FILE = "./config/mods/doubleSessions.ini";
   public static final String OLY_ANTI_FEED_FILE = "./config/mods/olympiadAntiFeed.ini";
   public static final String ANTIBOT_CONFIG = "./config/mods/antiBot.ini";
   public static final String CUSTOM_RATES_CONFIG = "./config/mods/customRates.ini";
   public static final String STREAMERS_CONFIG = "./config/mods/streamers.ini";
   public static final String FACEBOOK_CONFIG = "./config/mods/facebookRewards.ini";
   public static final String FAKES_CONFIG = "./config/mods/fakes/fakePlayers.ini";
   public static final String BOT_FILE = "./config/mods/botFunctions.ini";
   public static final String REVENGE_FILE = "./config/mods/revenge.ini";
   private static final String WEEKLY_TRADER_FILE = "./config/mods/weeklyTrader.ini";
   public static final String AUTO_FARM_FILE = "./config/mods/autoFarm.ini";
   public static final String CONFIGURATION_FILE = "./config/network/server.ini";
   public static final String IP_CONFIG_FILE = "./config/ipconfig.xml";
   private static final HashMap<String, String> _personalConfigs = new HashMap<>();
   public static SelectorConfig SELECTOR_CONFIG = new SelectorConfig();
   public static boolean ALLOW_ZONES_LIMITS;
   public static long ALT_REUSE_CORRECTION;
   public static long USER_INFO_INTERVAL;
   public static long BROADCAST_CHAR_INFO_INTERVAL;
   public static long BROADCAST_STATUS_UPDATE_INTERVAL;
   public static long USER_STATS_UPDATE_INTERVAL;
   public static long USER_ABNORMAL_EFFECTS_INTERVAL;
   public static long MOVE_PACKET_DELAY;
   public static long ATTACK_PACKET_DELAY;
   public static long REQUEST_MAGIC_PACKET_DELAY;
   public static boolean ALLOW_UI_OPEN;
   public static boolean BALANCER_ALLOW;
   public static boolean CHECK_ATTACK_STATUS_TO_MOVE;
   public static int MIN_HIT_TIME;
   public static boolean ALT_GAME_DELEVEL;
   public static boolean DECREASE_SKILL_LEVEL;
   public static boolean DECREASE_ENCHANT_SKILLS;
   public static double ALT_WEIGHT_LIMIT;
   public static int RUN_SPD_BOOST;
   public static int DEATH_PENALTY_CHANCE;
   public static double RESPAWN_RESTORE_CP;
   public static double RESPAWN_RESTORE_HP;
   public static double RESPAWN_RESTORE_MP;
   public static boolean ALT_GAME_TIREDNESS;
   public static boolean ENABLE_MODIFY_SKILL_DURATION;
   public static Map<Integer, Integer> SKILL_DURATION_LIST_SIMPLE;
   public static Map<Integer, Integer> SKILL_DURATION_LIST_PREMIUM;
   public static boolean ENABLE_MODIFY_SKILL_REUSE;
   public static Map<Integer, Integer> SKILL_REUSE_LIST;
   public static boolean ALLOW_SKILL_END_CAST;
   public static boolean AUTO_LEARN_SKILLS;
   public static int AUTO_LEARN_SKILLS_MAX_LEVEL;
   public static boolean AUTO_LEARN_FS_SKILLS;
   public static boolean AUTO_LOOT_HERBS;
   public static byte BUFFS_MAX_AMOUNT;
   public static byte BUFFS_MAX_AMOUNT_PREMIUM;
   public static byte TRIGGERED_BUFFS_MAX_AMOUNT;
   public static byte DANCES_MAX_AMOUNT;
   public static boolean DANCE_CANCEL_BUFF;
   public static boolean DANCE_CONSUME_ADDITIONAL_MP;
   public static boolean ALT_STORE_DANCES;
   public static boolean AUTO_LEARN_DIVINE_INSPIRATION;
   public static boolean ALT_GAME_CANCEL_CAST;
   public static boolean EFFECT_CANCELING;
   public static boolean ALT_GAME_MAGICFAILURES;
   public static int PLAYER_FAKEDEATH_UP_PROTECTION;
   public static boolean STORE_SKILL_COOLTIME;
   public static boolean DEBUFF_REOVERLAY;
   public static boolean ALLOW_DEBUFF_INFO;
   public static boolean SUBCLASS_STORE_SKILL_COOLTIME;
   public static boolean SUBCLASS_STORE_SKILL;
   public static boolean SUMMON_STORE_SKILL_COOLTIME;
   public static boolean ALT_GAME_SHIELD_BLOCKS;
   public static int ALT_PERFECT_SHLD_BLOCK;
   public static double ALT_SHLD_BLOCK_MODIFIER;
   public static boolean ALLOW_ENTIRE_TREE;
   public static boolean ALTERNATE_CLASS_MASTER;
   public static boolean LIFE_CRYSTAL_NEEDED;
   public static boolean ES_SP_BOOK_NEEDED;
   public static boolean DIVINE_SP_BOOK_NEEDED;
   public static boolean ALT_GAME_SKILL_LEARN;
   public static boolean ALT_GAME_SUBCLASS_WITHOUT_QUESTS;
   public static boolean ALT_GAME_SUBCLASS_EVERYWHERE;
   public static boolean ALT_GAME_SUBCLASS_ALL_CLASSES;
   public static boolean ALLOW_TRANSFORM_WITHOUT_QUEST;
   public static int FEE_DELETE_TRANSFER_SKILLS;
   public static int FEE_DELETE_SUBCLASS_SKILLS;
   public static boolean RESTORE_SERVITOR_ON_RECONNECT;
   public static boolean RESTORE_PET_ON_RECONNECT;
   public static double MAX_BONUS_EXP;
   public static double MAX_BONUS_SP;
   public static int MAX_RUN_SPEED;
   public static int MAX_PCRIT_RATE;
   public static int MAX_MCRIT_RATE;
   public static int MAX_PATK_SPEED;
   public static int MAX_MATK_SPEED;
   public static int MAX_EVASION;
   public static double SKILLS_CHANCE_MOD;
   public static double SKILLS_CHANCE_POW;
   public static double STUN_CHANCE_MOD;
   public static double STUN_CHANCE_CRIT_MOD;
   public static double SKILL_BREAK_MOD;
   public static double SKILL_BREAK_CRIT_MOD;
   public static int MIN_ABNORMAL_STATE_SUCCESS_RATE;
   public static int MAX_ABNORMAL_STATE_SUCCESS_RATE;
   public static byte MAX_SUBCLASS;
   public static byte BASE_SUBCLASS_LEVEL;
   public static byte MAX_SUBCLASS_LEVEL;
   public static int MAX_PVTSTORESELL_SLOTS_DWARF;
   public static int MAX_PVTSTORESELL_SLOTS_OTHER;
   public static int MAX_PVTSTOREBUY_SLOTS_DWARF;
   public static int MAX_PVTSTOREBUY_SLOTS_OTHER;
   public static int INVENTORY_MAXIMUM_NO_DWARF;
   public static int INVENTORY_MAXIMUM_DWARF;
   public static int INVENTORY_MAXIMUM_GM;
   public static int INVENTORY_MAXIMUM_QUEST_ITEMS;
   public static int WAREHOUSE_SLOTS_DWARF;
   public static int WAREHOUSE_SLOTS_NO_DWARF;
   public static int WAREHOUSE_SLOTS_CLAN;
   public static int ALT_FREIGHT_SLOTS;
   public static int MAX_AMOUNT_BY_MULTISELL;
   public static boolean ALLOW_MULTISELL_DEBUG;
   public static int ALT_FREIGHT_PRICE;
   public static int EXPAND_INVENTORY_LIMIT;
   public static int EXPAND_WAREHOUSE_LIMIT;
   public static int EXPAND_SELLSTORE_LIMIT;
   public static int EXPAND_BUYSTORE_LIMIT;
   public static int EXPAND_DWARFRECIPE_LIMIT;
   public static int EXPAND_COMMONRECIPE_LIMIT;
   public static boolean ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE;
   public static boolean ALT_GAME_KARMA_PLAYER_CAN_SHOP;
   public static boolean ALT_GAME_KARMA_PLAYER_CAN_TELEPORT;
   public static boolean ALT_GAME_KARMA_PLAYER_CAN_USE_GK;
   public static boolean ALT_GAME_KARMA_PLAYER_CAN_TRADE;
   public static boolean ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE;
   public static int MAX_PERSONAL_FAME_POINTS;
   public static int FORTRESS_ZONE_FAME_TASK_FREQUENCY;
   public static int FORTRESS_ZONE_FAME_AQUIRE_POINTS;
   public static int CASTLE_ZONE_FAME_TASK_FREQUENCY;
   public static int CASTLE_ZONE_FAME_AQUIRE_POINTS;
   public static boolean FAME_FOR_DEAD_PLAYERS;
   public static boolean IS_CRAFTING_ENABLED;
   public static boolean CRAFT_MASTERWORK;
   public static double CRAFT_DOUBLECRAFT_CHANCE;
   public static int DWARF_RECIPE_SLOTS;
   public static int COMMON_RECIPE_SLOTS;
   public static boolean ALT_GAME_CREATION;
   public static double ALT_GAME_CREATION_SPEED;
   public static double ALT_GAME_CREATION_XP_RATE;
   public static double ALT_GAME_CREATION_RARE_XPSP_RATE;
   public static double ALT_GAME_CREATION_SP_RATE;
   public static boolean ALT_BLACKSMITH_USE_RECIPES;
   public static int ALT_CLAN_LEADER_DATE_CHANGE;
   public static int ALT_CLAN_DEFAULT_LEVEL;
   public static String ALT_CLAN_LEADER_HOUR_CHANGE;
   public static boolean ALT_CLAN_LEADER_INSTANT_ACTIVATION;
   public static int ALT_CLAN_JOIN_DAYS;
   public static int ALT_CLAN_CREATE_DAYS;
   public static int ALT_CLAN_DISSOLVE_DAYS;
   public static int ALT_ALLY_JOIN_DAYS_WHEN_LEAVED;
   public static int ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED;
   public static int ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED;
   public static int ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED;
   public static int ALT_MAX_NUM_OF_CLANS_IN_ALLY;
   public static int ALT_CLAN_MEMBERS_FOR_WAR;
   public static boolean ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH;
   public static boolean REMOVE_CASTLE_CIRCLETS;
   public static int ALT_PARTY_RANGE;
   public static int ALT_PARTY_RANGE2;
   public static boolean ALT_LEAVE_PARTY_LEADER;
   public static long STARTING_ADENA;
   public static byte STARTING_LEVEL;
   public static int STARTING_SP;
   public static long MAX_ADENA;
   public static boolean AUTO_LOOT;
   public static boolean AUTO_LOOT_RAIDS;
   public static int LOOT_RAIDS_PRIVILEGE_INTERVAL;
   public static int LOOT_RAIDS_PRIVILEGE_CC_SIZE;
   public static int UNSTUCK_INTERVAL;
   public static int TELEPORT_WATCHDOG_TIMEOUT;
   public static int PLAYER_SPAWN_PROTECTION;
   public static List<Integer> SPAWN_PROTECTION_ALLOWED_ITEMS;
   public static int PLAYER_TELEPORT_PROTECTION;
   public static boolean RANDOM_RESPAWN_IN_TOWN_ENABLED;
   public static boolean OFFSET_ON_TELEPORT_ENABLED;
   public static int MAX_OFFSET_ON_TELEPORT;
   public static boolean RESTORE_PLAYER_INSTANCE;
   public static boolean ALLOW_SUMMON_TO_INSTANCE;
   public static int EJECT_DEAD_PLAYER_TIME;
   public static boolean PETITIONING_ALLOWED;
   public static boolean NEW_PETITIONING_SYSTEM;
   public static int MAX_PETITIONS_PER_PLAYER;
   public static int MAX_PETITIONS_PENDING;
   public static boolean ALT_GAME_FREE_TELEPORT;
   public static int DELETE_DAYS;
   public static float ALT_GAME_EXPONENT_XP;
   public static float ALT_GAME_EXPONENT_SP;
   public static String PARTY_XP_CUTOFF_METHOD;
   public static double PARTY_XP_CUTOFF_PERCENT;
   public static int PARTY_XP_CUTOFF_LEVEL;
   public static int[][] PARTY_XP_CUTOFF_GAPS;
   public static int[] PARTY_XP_CUTOFF_GAP_PERCENTS;
   public static boolean DISABLE_TUTORIAL;
   public static boolean EXPERTISE_PENALTY;
   public static boolean STORE_RECIPE_SHOPLIST;
   public static boolean STORE_UI_SETTINGS;
   public static String[] FORBIDDEN_NAMES;
   public static boolean SILENCE_MODE_EXCLUDE;
   public static boolean ALT_VALIDATE_TRIGGER_SKILLS;
   public static float ALT_DAGGER_DMG_VS_HEAVY;
   public static float ALT_DAGGER_DMG_VS_ROBE;
   public static float ALT_DAGGER_DMG_VS_LIGHT;
   public static float ALT_BOW_DMG_VS_HEAVY;
   public static float ALT_BOW_DMG_VS_ROBE;
   public static float ALT_BOW_DMG_VS_LIGHT;
   public static float ALT_MAGES_PHYSICAL_DAMAGE_MULTI;
   public static float ALT_MAGES_MAGICAL_DAMAGE_MULTI;
   public static float ALT_FIGHTERS_PHYSICAL_DAMAGE_MULTI;
   public static float ALT_FIGHTERS_MAGICAL_DAMAGE_MULTI;
   public static float ALT_PETS_PHYSICAL_DAMAGE_MULTI;
   public static float ALT_PETS_MAGICAL_DAMAGE_MULTI;
   public static float ALT_NPC_PHYSICAL_DAMAGE_MULTI;
   public static float ALT_NPC_MAGICAL_DAMAGE_MULTI;
   public static float PATK_SPEED_MULTI;
   public static float MATK_SPEED_MULTI;
   public static boolean RESTORE_DISPEL_SKILLS;
   public static int RESTORE_DISPEL_SKILLS_TIME;
   public static boolean ALT_GAME_VIEWPLAYER;
   public static boolean DISPLAY_MESSAGE;
   public static int BASE_STR_LIMIT;
   public static int BASE_INT_LIMIT;
   public static int BASE_DEX_LIMIT;
   public static int BASE_WIT_LIMIT;
   public static int BASE_CON_LIMIT;
   public static int BASE_MEN_LIMIT;
   public static int BASE_RESET_STR;
   public static int BASE_RESET_INT;
   public static int BASE_RESET_DEX;
   public static int BASE_RESET_WIT;
   public static int BASE_RESET_CON;
   public static int BASE_RESET_MEN;
   public static boolean TRADE_ONLY_IN_PEACE_ZONE;
   public static boolean ATTACK_STANCE_MAGIC;
   public static long CH_TELE_FEE_RATIO;
   public static int CH_TELE1_FEE;
   public static int CH_TELE2_FEE;
   public static long CH_ITEM_FEE_RATIO;
   public static int CH_ITEM1_FEE;
   public static int CH_ITEM2_FEE;
   public static int CH_ITEM3_FEE;
   public static long CH_MPREG_FEE_RATIO;
   public static int CH_MPREG1_FEE;
   public static int CH_MPREG2_FEE;
   public static int CH_MPREG3_FEE;
   public static int CH_MPREG4_FEE;
   public static int CH_MPREG5_FEE;
   public static long CH_HPREG_FEE_RATIO;
   public static int CH_HPREG1_FEE;
   public static int CH_HPREG2_FEE;
   public static int CH_HPREG3_FEE;
   public static int CH_HPREG4_FEE;
   public static int CH_HPREG5_FEE;
   public static int CH_HPREG6_FEE;
   public static int CH_HPREG7_FEE;
   public static int CH_HPREG8_FEE;
   public static int CH_HPREG9_FEE;
   public static int CH_HPREG10_FEE;
   public static int CH_HPREG11_FEE;
   public static int CH_HPREG12_FEE;
   public static int CH_HPREG13_FEE;
   public static long CH_EXPREG_FEE_RATIO;
   public static int CH_EXPREG1_FEE;
   public static int CH_EXPREG2_FEE;
   public static int CH_EXPREG3_FEE;
   public static int CH_EXPREG4_FEE;
   public static int CH_EXPREG5_FEE;
   public static int CH_EXPREG6_FEE;
   public static int CH_EXPREG7_FEE;
   public static long CH_SUPPORT_FEE_RATIO;
   public static int CH_SUPPORT1_FEE;
   public static int CH_SUPPORT2_FEE;
   public static int CH_SUPPORT3_FEE;
   public static int CH_SUPPORT4_FEE;
   public static int CH_SUPPORT5_FEE;
   public static int CH_SUPPORT6_FEE;
   public static int CH_SUPPORT7_FEE;
   public static int CH_SUPPORT8_FEE;
   public static long CH_CURTAIN_FEE_RATIO;
   public static int CH_CURTAIN1_FEE;
   public static int CH_CURTAIN2_FEE;
   public static long CH_FRONT_FEE_RATIO;
   public static int CH_FRONT1_FEE;
   public static int CH_FRONT2_FEE;
   public static boolean CH_BUFF_FREE;
   public static long CS_TELE_FEE_RATIO;
   public static int CS_TELE1_FEE;
   public static int CS_TELE2_FEE;
   public static long CS_MPREG_FEE_RATIO;
   public static int CS_MPREG1_FEE;
   public static int CS_MPREG2_FEE;
   public static int CS_MPREG3_FEE;
   public static int CS_MPREG4_FEE;
   public static long CS_HPREG_FEE_RATIO;
   public static int CS_HPREG1_FEE;
   public static int CS_HPREG2_FEE;
   public static int CS_HPREG3_FEE;
   public static int CS_HPREG4_FEE;
   public static int CS_HPREG5_FEE;
   public static long CS_EXPREG_FEE_RATIO;
   public static int CS_EXPREG1_FEE;
   public static int CS_EXPREG2_FEE;
   public static int CS_EXPREG3_FEE;
   public static int CS_EXPREG4_FEE;
   public static long CS_SUPPORT_FEE_RATIO;
   public static int CS_SUPPORT1_FEE;
   public static int CS_SUPPORT2_FEE;
   public static int CS_SUPPORT3_FEE;
   public static int CS_SUPPORT4_FEE;
   public static List<Integer> SIEGE_HOUR_LIST;
   public static long FS_TELE_FEE_RATIO;
   public static int FS_TELE1_FEE;
   public static int FS_TELE2_FEE;
   public static long FS_MPREG_FEE_RATIO;
   public static int FS_MPREG1_FEE;
   public static int FS_MPREG2_FEE;
   public static long FS_HPREG_FEE_RATIO;
   public static int FS_HPREG1_FEE;
   public static int FS_HPREG2_FEE;
   public static long FS_EXPREG_FEE_RATIO;
   public static int FS_EXPREG1_FEE;
   public static int FS_EXPREG2_FEE;
   public static long FS_SUPPORT_FEE_RATIO;
   public static int FS_SUPPORT1_FEE;
   public static int FS_SUPPORT2_FEE;
   public static int FS_BLOOD_OATH_COUNT;
   public static int FS_UPDATE_FRQ;
   public static int FS_MAX_SUPPLY_LEVEL;
   public static int FS_FEE_FOR_CASTLE;
   public static int FS_MAX_OWN_TIME;
   public static int TAKE_FORT_POINTS;
   public static int LOOSE_FORT_POINTS;
   public static int TAKE_CASTLE_POINTS;
   public static int LOOSE_CASTLE_POINTS;
   public static int CASTLE_DEFENDED_POINTS;
   public static int FESTIVAL_WIN_POINTS;
   public static int HERO_POINTS;
   public static int ROYAL_GUARD_COST;
   public static int KNIGHT_UNIT_COST;
   public static int KNIGHT_REINFORCE_COST;
   public static int BALLISTA_POINTS;
   public static int BLOODALLIANCE_POINTS;
   public static int BLOODOATH_POINTS;
   public static int KNIGHTSEPAULETTE_POINTS;
   public static int REPUTATION_SCORE_PER_KILL;
   public static int JOIN_ACADEMY_MIN_REP_SCORE;
   public static int JOIN_ACADEMY_MAX_REP_SCORE;
   public static int RAID_RANKING_1ST;
   public static int RAID_RANKING_2ND;
   public static int RAID_RANKING_3RD;
   public static int RAID_RANKING_4TH;
   public static int RAID_RANKING_5TH;
   public static int RAID_RANKING_6TH;
   public static int RAID_RANKING_7TH;
   public static int RAID_RANKING_8TH;
   public static int RAID_RANKING_9TH;
   public static int RAID_RANKING_10TH;
   public static int RAID_RANKING_UP_TO_50TH;
   public static int RAID_RANKING_UP_TO_100TH;
   public static int CLAN_LEVEL_6_COST;
   public static int CLAN_LEVEL_7_COST;
   public static int CLAN_LEVEL_8_COST;
   public static int CLAN_LEVEL_9_COST;
   public static int CLAN_LEVEL_10_COST;
   public static int CLAN_LEVEL_11_COST;
   public static int CLAN_LEVEL_6_REQUIREMENT;
   public static int CLAN_LEVEL_7_REQUIREMENT;
   public static int CLAN_LEVEL_8_REQUIREMENT;
   public static int CLAN_LEVEL_9_REQUIREMENT;
   public static int CLAN_LEVEL_10_REQUIREMENT;
   public static int CLAN_LEVEL_11_REQUIREMENT;
   public static int RANK_CLASS_FOR_CC;
   public static boolean ALLOW_WYVERN_ALWAYS;
   public static boolean ALLOW_WYVERN_DURING_SIEGE;
   public static boolean EVERYBODY_HAS_ADMIN_RIGHTS;
   public static boolean DISPLAY_SERVER_VERSION;
   public static boolean GM_HERO_AURA;
   public static boolean GM_STARTUP_INVULNERABLE;
   public static boolean GM_STARTUP_INVISIBLE;
   public static boolean GM_STARTUP_SILENCE;
   public static boolean GM_STARTUP_AUTO_LIST;
   public static boolean GM_STARTUP_DIET_MODE;
   public static boolean GM_ITEM_RESTRICTION;
   public static boolean GM_SKILL_RESTRICTION;
   public static boolean GM_TRADE_RESTRICTED_ITEMS;
   public static boolean GM_RESTART_FIGHTING;
   public static boolean GM_ANNOUNCER_NAME;
   public static boolean GM_CRITANNOUNCER_NAME;
   public static boolean GM_GIVE_SPECIAL_SKILLS;
   public static boolean GM_GIVE_SPECIAL_AURA_SKILLS;
   public static boolean GAMEGUARD_ENFORCE;
   public static boolean LOG_CHAT;
   public static boolean LOG_AUTO_ANNOUNCEMENTS;
   public static boolean LOG_ITEMS;
   public static boolean SERVICE_LOGS;
   public static boolean LOG_ITEMS_SMALL_LOG;
   public static boolean LOG_ITEM_ENCHANTS;
   public static boolean LOG_SKILL_ENCHANTS;
   public static boolean GMAUDIT;
   public static boolean LOG_GAME_DAMAGE;
   public static int LOG_GAME_DAMAGE_THRESHOLD;
   public static boolean SKILL_CHECK_ENABLE;
   public static boolean SKILL_CHECK_REMOVE;
   public static boolean SKILL_CHECK_GM;
   public static boolean DEBUG;
   public static boolean DEBUG_SPAWN;
   public static boolean PACKET_HANDLER_DEBUG;
   public static boolean DEVELOPER;
   public static boolean ALT_DEV_NO_HANDLERS;
   public static boolean ALT_DEV_NO_SCRIPTS;
   public static boolean ALT_DEV_NO_SPAWNS;
   public static boolean ALT_CHEST_NO_SPAWNS;
   public static int SCHEDULED_THREAD_POOL_SIZE;
   public static int EXECUTOR_THREAD_POOL_SIZE;
   public static boolean ALLOW_DISCARDITEM;
   public static List<Integer> LIST_DISCARDITEM_ITEMS;
   public static int AUTODESTROY_ITEM_AFTER;
   public static int HERB_AUTO_DESTROY_TIME;
   public static List<Integer> LIST_PROTECTED_ITEMS;
   public static boolean DATABASE_CLEAN_UP;
   public static int CHAR_STORE_INTERVAL;
   public static boolean LAZY_ITEMS_UPDATE;
   public static boolean UPDATE_ITEMS_ON_CHAR_STORE;
   public static boolean DESTROY_DROPPED_PLAYER_ITEM;
   public static boolean DESTROY_EQUIPABLE_PLAYER_ITEM;
   public static boolean SAVE_DROPPED_ITEM;
   public static boolean EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD;
   public static int SAVE_DROPPED_ITEM_INTERVAL;
   public static boolean CLEAR_DROPPED_ITEM_TABLE;
   public static boolean AUTODELETE_INVALID_QUEST_DATA;
   public static boolean PRECISE_DROP_CALCULATION;
   public static boolean MULTIPLE_ITEM_DROP;
   public static boolean FORCE_INVENTORY_UPDATE;
   public static boolean LAZY_CACHE;
   public static boolean CACHE_CHAR_NAMES;
   public static int MIN_NPC_ANIMATION;
   public static int MAX_NPC_ANIMATION;
   public static int MIN_MONSTER_ANIMATION;
   public static int MAX_MONSTER_ANIMATION;
   public static boolean ENABLE_FALLING_DAMAGE;
   public static int PEACE_ZONE_MODE;
   public static boolean ALLOW_WAREHOUSE;
   public static boolean WAREHOUSE_CACHE;
   public static int WAREHOUSE_CACHE_TIME;
   public static boolean ALLOW_REFUND;
   public static boolean ALLOW_MAIL;
   public static int MAIL_EXPIRATION;
   public static int MAIL_COND_EXPIRATION;
   public static boolean ALLOW_ATTACHMENTS;
   public static boolean ALLOW_WEAR;
   public static int WEAR_DELAY;
   public static int WEAR_PRICE;
   public static boolean ALLOW_LOTTERY;
   public static boolean ALLOW_RACE;
   public static boolean ALLOW_WATER;
   public static boolean ALLOW_RENTPET;
   public static boolean ALLOWFISHING;
   public static boolean ALLOW_BOAT;
   public static int BOAT_BROADCAST_RADIUS;
   public static boolean ALLOW_CURSED_WEAPONS;
   public static boolean ALLOW_MANOR;
   public static boolean ALLOW_PET_WALKERS;
   public static boolean SERVER_NEWS;
   public static String ALT_OLY_START_TIME;
   public static String OLYMPIAD_PERIOD;
   public static boolean OLY_PRINT_CLASS_OPPONENT;
   public static boolean AUTO_GET_HERO;
   public static boolean ALLOW_OLY_HIT_SUMMON;
   public static boolean ALLOW_OLY_FAST_INVITE;
   public static boolean ALLOW_RESTART_AT_OLY;
   public static long ALT_OLY_CPERIOD;
   public static long ALT_OLY_BATTLE;
   public static int ALT_OLY_TELE_TO_TOWN;
   public static long ALT_OLY_WPERIOD;
   public static long ALT_OLY_VPERIOD;
   public static int ALT_OLY_START_POINTS;
   public static int ALT_OLY_WEEKLY_POINTS;
   public static int ALT_OLY_CLASSED;
   public static int ALT_OLY_NONCLASSED;
   public static int ALT_OLY_TEAMS;
   public static int ALT_OLY_REG_DISPLAY;
   public static int[][] ALT_OLY_CLASSED_REWARD;
   public static int[][] ALT_OLY_NONCLASSED_REWARD;
   public static int[][] ALT_OLY_TEAM_REWARD;
   public static int ALT_OLY_COMP_RITEM;
   public static int ALT_OLY_MIN_MATCHES;
   public static int ALT_OLY_GP_PER_POINT;
   public static int ALT_OLY_HERO_POINTS;
   public static int ALT_OLY_RANK1_POINTS;
   public static int ALT_OLY_RANK2_POINTS;
   public static int ALT_OLY_RANK3_POINTS;
   public static int ALT_OLY_RANK4_POINTS;
   public static int ALT_OLY_RANK5_POINTS;
   public static int ALT_OLY_MAX_POINTS;
   public static int ALT_OLY_DIVIDER_CLASSED;
   public static int ALT_OLY_DIVIDER_NON_CLASSED;
   public static int ALT_OLY_MAX_WEEKLY_MATCHES;
   public static int ALT_OLY_MAX_WEEKLY_MATCHES_NON_CLASSED;
   public static int ALT_OLY_MAX_WEEKLY_MATCHES_CLASSED;
   public static int ALT_OLY_MAX_WEEKLY_MATCHES_TEAM;
   public static boolean ALT_OLY_LOG_FIGHTS;
   public static boolean ALT_OLY_SHOW_MONTHLY_WINNERS;
   public static boolean ALT_OLY_ANNOUNCE_GAMES;
   public static List<Integer> LIST_OLY_RESTRICTED_ITEMS;
   public static int ALT_OLY_ENCHANT_LIMIT;
   public static int ALT_OLY_WAIT_TIME;
   public static boolean BLOCK_VISUAL_OLY;
   public static int ALT_MANOR_REFRESH_TIME;
   public static int ALT_MANOR_REFRESH_MIN;
   public static int ALT_MANOR_APPROVE_TIME;
   public static int ALT_MANOR_APPROVE_MIN;
   public static int ALT_MANOR_MAINTENANCE_PERIOD;
   public static boolean ALT_MANOR_SAVE_ALL_ACTIONS;
   public static int ALT_MANOR_SAVE_PERIOD_RATE;
   public static long ALT_LOTTERY_PRIZE;
   public static long ALT_LOTTERY_TICKET_PRICE;
   public static float ALT_LOTTERY_5_NUMBER_RATE;
   public static float ALT_LOTTERY_4_NUMBER_RATE;
   public static float ALT_LOTTERY_3_NUMBER_RATE;
   public static long ALT_LOTTERY_2_AND_1_NUMBER_PRIZE;
   public static boolean ALT_ITEM_AUCTION_ENABLED;
   public static boolean ALLOW_ITEM_AUCTION_ANNOUNCE;
   public static int ALT_ITEM_AUCTION_EXPIRED_AFTER;
   public static long ALT_ITEM_AUCTION_TIME_EXTENDS_ON_BID;
   public static int FS_TIME_ATTACK;
   public static int FS_TIME_COOLDOWN;
   public static int FS_TIME_ENTRY;
   public static int FS_TIME_WARMUP;
   public static int FS_PARTY_MEMBER_COUNT;
   public static int RIFT_MIN_PARTY_SIZE;
   public static int RIFT_SPAWN_DELAY;
   public static int RIFT_MAX_JUMPS;
   public static int RIFT_AUTO_JUMPS_TIME_MIN;
   public static int RIFT_AUTO_JUMPS_TIME_MAX;
   public static float RIFT_BOSS_ROOM_TIME_MUTIPLY;
   public static int RIFT_ENTER_COST_RECRUIT;
   public static int RIFT_ENTER_COST_SOLDIER;
   public static int RIFT_ENTER_COST_OFFICER;
   public static int RIFT_ENTER_COST_CAPTAIN;
   public static int RIFT_ENTER_COST_COMMANDER;
   public static int RIFT_ENTER_COST_HERO;
   public static boolean CUSTOM_SPAWNLIST;
   public static boolean SAVE_GMSPAWN_ON_CUSTOM;
   public static boolean CUSTOM_NPC;
   public static boolean CUSTOM_SKILLS;
   public static boolean CUSTOM_ITEMS;
   public static boolean CUSTOM_MULTISELLS;
   public static boolean CUSTOM_BUYLIST;
   public static int ALT_BIRTHDAY_GIFT;
   public static String ALT_BIRTHDAY_MAIL_SUBJECT;
   public static String ALT_BIRTHDAY_MAIL_TEXT;
   public static boolean ENABLE_BLOCK_CHECKER_EVENT;
   public static int MIN_BLOCK_CHECKER_TEAM_MEMBERS;
   public static boolean HBCE_FAIR_PLAY;
   public static boolean CLEAR_CREST_CACHE;
   public static boolean SKILL_CHANCE_SHOW;
   public static int NORMAL_ENCHANT_COST_MULTIPLIER;
   public static int SAFE_ENCHANT_COST_MULTIPLIER;
   public static final List<FloodProtectorConfig> FLOOD_PROTECTORS = new ArrayList<>();
   public static boolean ALLOW_WEDDING;
   public static int WEDDING_PRICE;
   public static boolean WEDDING_PUNISH_INFIDELITY;
   public static boolean WEDDING_TELEPORT;
   public static int WEDDING_TELEPORT_PRICE;
   public static int WEDDING_TELEPORT_DURATION;
   public static boolean WEDDING_FORMALWEAR;
   public static int WEDDING_DIVORCE_COSTS;
   public static int[] WEDDING_REWARD = new int[2];
   public static boolean HELLBOUND_STATUS;
   public static boolean BANKING_SYSTEM_ENABLED;
   public static int BANKING_SYSTEM_GOLDBARS;
   public static int BANKING_SYSTEM_ADENA;
   public static boolean ENABLE_WAREHOUSESORTING_CLAN;
   public static boolean ENABLE_WAREHOUSESORTING_PRIVATE;
   public static boolean OFFLINE_TRADE_ENABLE;
   public static int OFFLINE_TRADE_MIN_LVL;
   public static boolean OFFLINE_CRAFT_ENABLE;
   public static boolean OFFLINE_MODE_IN_PEACE_ZONE;
   public static boolean OFFLINE_MODE_NO_DAMAGE;
   public static boolean RESTORE_OFFLINERS;
   public static int OFFLINE_MAX_DAYS;
   public static boolean OFFLINE_DISCONNECT_FINISHED;
   public static boolean OFFLINE_SET_NAME_COLOR;
   public static boolean OFFLINE_SET_VISUAL_EFFECT;
   public static int OFFLINE_NAME_COLOR;
   public static boolean OFFLINE_FAME;
   public static boolean ENABLE_MANA_POTIONS_SUPPORT;
   public static boolean DISPLAY_SERVER_TIME;
   public static boolean WELCOME_MESSAGE_ENABLED;
   public static String WELCOME_MESSAGE_TEXT;
   public static int WELCOME_MESSAGE_TIME;
   public static boolean ANNOUNCE_PK_PVP;
   public static boolean ANNOUNCE_PK_PVP_NORMAL_MESSAGE;
   public static String ANNOUNCE_PK_MSG;
   public static String ANNOUNCE_PVP_MSG;
   public static boolean CHAT_ADMIN;
   public static boolean L2WALKER_PROTECTION;
   public static boolean DEBUG_VOICE_COMMAND;
   public static boolean DOUBLE_SESSIONS_ENABLE;
   public static boolean DOUBLE_SESSIONS_HWIDS;
   public static boolean DOUBLE_SESSIONS_DISCONNECTED;
   public static int DOUBLE_SESSIONS_INTERVAL;
   public static int DOUBLE_SESSIONS_CHECK_MAX_PLAYERS;
   public static boolean DOUBLE_SESSIONS_CONSIDER_OFFLINE_TRADERS;
   public static int DOUBLE_SESSIONS_CHECK_MAX_OLYMPIAD_PARTICIPANTS;
   public static int DOUBLE_SESSIONS_CHECK_MAX_EVENT_PARTICIPANTS;
   public static boolean ALLOW_CHANGE_PASSWORD;
   public static boolean PROTECTION_IP_ENABLED;
   public static int SOULSHOT_CHANCE;
   public static int SPIRITSHOT_CHANCE;
   public static int PLAYER_MOVEMENT_BLOCK_TIME;
   public static boolean ANNOUNCE_MAMMON_SPAWN;
   public static boolean ALT_MOB_AGRO_IN_PEACEZONE;
   public static boolean ALT_ATTACKABLE_NPCS;
   public static boolean ALT_GAME_VIEWNPC;
   public static int MAX_DRIFT_RANGE;
   public static boolean DEEPBLUE_DROP_RULES;
   public static int DEEPBLUE_DROP_MAXDIFF;
   public static int DEEPBLUE_DROP_RAID_MAXDIFF;
   public static boolean SHOW_NPC_SERVER_NAME;
   public static boolean SHOW_NPC_SERVER_TITLE;
   public static boolean SHOW_NPC_LVL;
   public static boolean SHOW_CREST_WITHOUT_QUEST;
   public static boolean ENABLE_RANDOM_ENCHANT_EFFECT;
   public static int MIN_NPC_LVL_DMG_PENALTY;
   public static Map<Integer, Float> NPC_DMG_PENALTY;
   public static Map<Integer, Float> NPC_CRIT_DMG_PENALTY;
   public static Map<Integer, Float> NPC_SKILL_DMG_PENALTY;
   public static int MIN_NPC_LVL_MAGIC_PENALTY;
   public static Map<Integer, Float> NPC_SKILL_CHANCE_PENALTY;
   public static int NPC_DEAD_TIME_TASK;
   public static int DECAY_TIME_TASK;
   public static int NPC_DECAY_TIME;
   public static int RAID_BOSS_DECAY_TIME;
   public static int SPOILED_DECAY_TIME;
   public static int MAX_SWEEPER_TIME;
   public static boolean GUARD_ATTACK_AGGRO_MOB;
   public static boolean ALLOW_WYVERN_UPGRADER;
   public static List<Integer> LIST_PET_RENT_NPC;
   public static double RAID_HP_REGEN_MULTIPLIER;
   public static double RAID_MP_REGEN_MULTIPLIER;
   public static double RAID_PDEFENCE_MULTIPLIER;
   public static double RAID_MDEFENCE_MULTIPLIER;
   public static double RAID_PATTACK_MULTIPLIER;
   public static double RAID_MATTACK_MULTIPLIER;
   public static double RAID_MINION_RESPAWN_TIMER;
   public static Map<Integer, Integer> MINIONS_RESPAWN_TIME;
   public static float RAID_MIN_RESPAWN_MULTIPLIER;
   public static float RAID_MAX_RESPAWN_MULTIPLIER;
   public static boolean RAID_DISABLE_CURSE;
   public static int INVENTORY_MAXIMUM_PET;
   public static double PET_HP_REGEN_MULTIPLIER;
   public static double PET_MP_REGEN_MULTIPLIER;
   public static boolean LAKFI_ENABLED;
   public static int TIME_CHANGE_SPAWN;
   public static long MIN_ADENA_TO_EAT;
   public static int TIME_IF_NOT_FEED;
   public static int INTERVAL_EATING;
   public static int[] NPC_BLOCK_SHIFT_LIST;
   public static boolean EPAULETTE_ONLY_FOR_REG;
   public static boolean DRAGON_VORTEX_UNLIMITED_SPAWN;
   public static int VORTEX_DELAY_TIME;
   public static boolean ALLOW_RAIDBOSS_CHANCE_DEBUFF;
   public static double RAIDBOSS_CHANCE_DEBUFF;
   public static boolean ALLOW_GRANDBOSS_CHANCE_DEBUFF;
   public static double GRANDBOSS_CHANCE_DEBUFF;
   public static int[] RAIDBOSS_DEBUFF_SPECIAL;
   public static int[] GRANDBOSS_DEBUFF_SPECIAL;
   public static double RAIDBOSS_CHANCE_DEBUFF_SPECIAL;
   public static double GRANDBOSS_CHANCE_DEBUFF_SPECIAL;
   public static boolean ALWAYS_TELEPORT_HOME;
   public static int MAX_PURSUE_RANGE;
   public static int MAX_PURSUE_RANGE_RAID;
   public static boolean CALC_NPCS_STATS;
   public static int CRUMA_MAX_LEVEL;
   public static double SKILLS_MOB_CHANCE;
   public static boolean ALLOW_NPC_LVL_MOD;
   public static boolean ALLOW_SUMMON_LVL_MOD;
   public static double PATK_HATE_MOD;
   public static double MATK_HATE_MOD;
   public static double PET_HATE_MOD;
   public static int[] RAIDBOSS_ANNOUNCE_LIST;
   public static int[] GRANDBOSS_ANNOUNCE_LIST;
   public static int[] RAIDBOSS_DEAD_ANNOUNCE_LIST;
   public static int[] GRANDBOSS_DEAD_ANNOUNCE_LIST;
   public static boolean ALLOW_DAMAGE_LIMIT;
   public static int NPC_DROP_PROTECTION;
   public static int RAID_DROP_PROTECTION;
   public static int KARMA_MIN_KARMA;
   public static int KARMA_MAX_KARMA;
   public static int KARMA_XP_DIVIDER;
   public static int KARMA_LOST_BASE;
   public static boolean KARMA_DROP_GM;
   public static boolean KARMA_AWARD_PK_KILL;
   public static int KARMA_PK_LIMIT;
   public static String KARMA_NONDROPPABLE_PET_ITEMS;
   public static String KARMA_NONDROPPABLE_ITEMS;
   public static int[] KARMA_LIST_NONDROPPABLE_PET_ITEMS;
   public static int[] KARMA_LIST_NONDROPPABLE_ITEMS;
   public static int DISABLE_ATTACK_IF_LVL_DIFFERENCE_OVER;
   public static boolean PVP_COLOR_SYSTEM = false;
   public static int PVP_AMMOUNT1 = 0;
   public static int PVP_AMMOUNT2 = 0;
   public static int PVP_AMMOUNT3 = 0;
   public static int PVP_AMMOUNT4 = 0;
   public static int PVP_AMMOUNT5 = 0;
   public static int COLOR_FOR_AMMOUNT1 = 0;
   public static int COLOR_FOR_AMMOUNT2 = 0;
   public static int COLOR_FOR_AMMOUNT3 = 0;
   public static int COLOR_FOR_AMMOUNT4 = 0;
   public static int COLOR_FOR_AMMOUNT5 = 0;
   public static int TITLE_COLOR_FOR_AMMOUNT1 = 0;
   public static int TITLE_COLOR_FOR_AMMOUNT2 = 0;
   public static int TITLE_COLOR_FOR_AMMOUNT3 = 0;
   public static int TITLE_COLOR_FOR_AMMOUNT4 = 0;
   public static int TITLE_COLOR_FOR_AMMOUNT5 = 0;
   public static boolean USE_PREMIUMSERVICE;
   public static boolean PREMIUMSERVICE_DOUBLE;
   public static boolean AUTO_GIVE_PREMIUM;
   public static int GIVE_PREMIUM_ID;
   public static boolean PREMIUM_PARTY_RATE;
   public static int MAX_DROP_ITEMS_FROM_ONE_GROUP;
   public static int MAX_SPOIL_ITEMS_FROM_ONE_GROUP;
   public static int MAX_DROP_ITEMS_FROM_ONE_GROUP_RAIDS;
   public static double GROUP_CHANCE_MODIFIER;
   public static double[] RATE_XP_BY_LVL;
   public static double[] RATE_SP_BY_LVL;
   public static double RATE_PARTY_XP;
   public static double RATE_PARTY_SP;
   public static double RATE_DROP_ADENA;
   public static double RATE_DROP_ITEMS;
   public static double RATE_DROP_SPOIL;
   public static double RATE_DROP_RAIDBOSS;
   public static double RATE_DROP_EPICBOSS;
   public static double RATE_DROP_SIEGE_GUARD;
   public static boolean NO_RATE_EQUIPMENT;
   public static boolean ALLOW_MODIFIER_FOR_DROP;
   public static boolean ALLOW_MODIFIER_FOR_RAIDS;
   public static boolean ALLOW_MODIFIER_FOR_SPOIL;
   public static boolean NO_RATE_KEY_MATERIAL;
   public static boolean NO_RATE_RECIPES;
   public static int[] NO_RATE_ITEMS;
   public static double RATE_DROP_FISHING;
   public static double RATE_CHANCE_GROUP_DROP_ITEMS;
   public static double RATE_CHANCE_DROP_ITEMS;
   public static double RATE_CHANCE_ATTRIBUTE;
   public static double RATE_CHANCE_DROP_HERBS;
   public static double RATE_CHANCE_SPOIL;
   public static double RATE_CHANCE_SPOIL_WEAPON_ARMOR_ACCESSORY;
   public static double RATE_CHANCE_DROP_WEAPON_ARMOR_ACCESSORY;
   public static double RATE_CHANCE_DROP_EPOLET;
   public static double RATE_CONSUMABLE_COST;
   public static double RATE_EXTRACTABLE;
   public static double RATE_HB_TRUST_INCREASE;
   public static double RATE_HB_TRUST_DECREASE;
   public static boolean RATE_QUEST_REWARD_USE_MULTIPLIERS;
   public static double ADENA_FIXED_CHANCE;
   public static double RATE_DROP_MANOR;
   public static float RATE_QUEST_DROP;
   public static float RATE_QUEST_REWARD;
   public static float RATE_QUEST_REWARD_XP;
   public static float RATE_QUEST_REWARD_SP;
   public static float RATE_QUEST_REWARD_ADENA;
   public static float RATE_QUEST_REWARD_POTION;
   public static float RATE_QUEST_REWARD_SCROLL;
   public static float RATE_QUEST_REWARD_RECIPE;
   public static float RATE_QUEST_REWARD_MATERIAL;
   public static float RATE_KARMA_EXP_LOST;
   public static float RATE_SIEGE_GUARDS_PRICE;
   public static int PLAYER_DROP_LIMIT;
   public static int PLAYER_RATE_DROP;
   public static int PLAYER_RATE_DROP_ITEM;
   public static int PLAYER_RATE_DROP_EQUIP;
   public static int PLAYER_RATE_DROP_EQUIP_WEAPON;
   public static float PET_XP_RATE;
   public static int PET_FOOD_RATE;
   public static float SINEATER_XP_RATE;
   public static int KARMA_DROP_LIMIT;
   public static int KARMA_RATE_DROP;
   public static int KARMA_RATE_DROP_ITEM;
   public static int KARMA_RATE_DROP_EQUIP;
   public static int KARMA_RATE_DROP_EQUIP_WEAPON;
   public static List<Integer> DISABLE_ITEM_DROP_LIST;
   public static int RATE_TALISMAN_MULTIPLIER;
   public static int RATE_TALISMAN_ITEM_MULTIPLIER;
   public static List<Integer> NO_DROP_ITEMS_FOR_SWEEP;
   public static List<Integer> ALLOW_ONLY_THESE_DROP_ITEMS_ID;
   public static double RATE_NOBLE_STONES_COUNT_MIN;
   public static double RATE_LIFE_STONES_COUNT_MIN;
   public static double RATE_ENCHANT_SCROLLS_COUNT_MIN;
   public static double RATE_FORGOTTEN_SCROLLS_COUNT_MIN;
   public static double RATE_KEY_MATHETIRALS_COUNT_MIN;
   public static double RATE_RECEPIES_COUNT_MIN;
   public static double RATE_BELTS_COUNT_MIN;
   public static double RATE_BRACELETS_COUNT_MIN;
   public static double RATE_CLOAKS_COUNT_MIN;
   public static double RATE_CODEX_BOOKS_COUNT_MIN;
   public static double RATE_ATTRIBUTE_STONES_COUNT_MIN;
   public static double RATE_ATTRIBUTE_CRYSTALS_COUNT_MIN;
   public static double RATE_ATTRIBUTE_JEWELS_COUNT_MIN;
   public static double RATE_ATTRIBUTE_ENERGY_COUNT_MIN;
   public static double RATE_WEAPONS_COUNT_MIN;
   public static double RATE_ARMOR_COUNT_MIN;
   public static double RATE_ACCESSORY_COUNT_MIN;
   public static double RATE_SEAL_STONES_COUNT_MIN;
   public static double RATE_NOBLE_STONES_COUNT_MAX;
   public static double RATE_LIFE_STONES_COUNT_MAX;
   public static double RATE_ENCHANT_SCROLLS_COUNT_MAX;
   public static double RATE_FORGOTTEN_SCROLLS_COUNT_MAX;
   public static double RATE_KEY_MATHETIRALS_COUNT_MAX;
   public static double RATE_RECEPIES_COUNT_MAX;
   public static double RATE_BELTS_COUNT_MAX;
   public static double RATE_BRACELETS_COUNT_MAX;
   public static double RATE_CLOAKS_COUNT_MAX;
   public static double RATE_CODEX_BOOKS_COUNT_MAX;
   public static double RATE_ATTRIBUTE_STONES_COUNT_MAX;
   public static double RATE_ATTRIBUTE_CRYSTALS_COUNT_MAX;
   public static double RATE_ATTRIBUTE_JEWELS_COUNT_MAX;
   public static double RATE_ATTRIBUTE_ENERGY_COUNT_MAX;
   public static double RATE_WEAPONS_COUNT_MAX;
   public static double RATE_ARMOR_COUNT_MAX;
   public static double RATE_ACCESSORY_COUNT_MAX;
   public static double RATE_SEAL_STONES_COUNT_MAX;
   public static Map<Integer, Double> MAX_AMOUNT_CORRECTOR;
   public static boolean ALT_GAME_CASTLE_DAWN;
   public static boolean ALT_GAME_CASTLE_DUSK;
   public static boolean ALT_GAME_REQUIRE_CLAN_CASTLE;
   public static int ALT_FESTIVAL_MIN_PLAYER;
   public static int ALT_MAXIMUM_PLAYER_CONTRIB;
   public static long ALT_FESTIVAL_MANAGER_START;
   public static long ALT_FESTIVAL_LENGTH;
   public static long ALT_FESTIVAL_CYCLE_LENGTH;
   public static long ALT_FESTIVAL_FIRST_SPAWN;
   public static long ALT_FESTIVAL_FIRST_SWARM;
   public static long ALT_FESTIVAL_SECOND_SPAWN;
   public static long ALT_FESTIVAL_SECOND_SWARM;
   public static long ALT_FESTIVAL_CHEST_SPAWN;
   public static double ALT_SIEGE_DAWN_GATES_PDEF_MULT;
   public static double ALT_SIEGE_DUSK_GATES_PDEF_MULT;
   public static double ALT_SIEGE_DAWN_GATES_MDEF_MULT;
   public static double ALT_SIEGE_DUSK_GATES_MDEF_MULT;
   public static boolean ALT_STRICT_SEVENSIGNS;
   public static boolean ALT_SEVENSIGNS_LAZY_UPDATE;
   public static int SSQ_DAWN_TICKET_QUANTITY;
   public static int SSQ_DAWN_TICKET_PRICE;
   public static int SSQ_DAWN_TICKET_BUNDLE;
   public static int SSQ_MANORS_AGREEMENT_ID;
   public static int SSQ_JOIN_DAWN_ADENA_FEE;
   public static boolean isActivate = true;
   public static boolean ALLOW_MULILOGIN;
   public static String USER_NAME;
   public static String USER_KEY;
   public static String PROTECTION;
   public static String DATABASE_DRIVER;
   public static String DATABASE_URL;
   public static String DATABASE_LOGIN;
   public static String DATABASE_PASSWORD;
   public static int DATABASE_MAX_CONNECTIONS;
   public static int DATABASE_MAX_IDLE_TIMEOUT;
   public static int DATABASE_IDLE_TEST_PERIOD;
   public static int MAXIMUM_ONLINE_USERS;
   public static String CNAME_TEMPLATE;
   public static String PET_NAME_TEMPLATE;
   public static String CLAN_NAME_TEMPLATE;
   public static int MAX_CHARACTERS_NUMBER_PER_ACCOUNT;
   public static File DATAPACK_ROOT;
   public static int REQUEST_ID;
   public static int PORT_GAME;
   public static boolean RESERVE_HOST_ON_LOGIN = false;
   public static List<Integer> PROTOCOL_LIST;
   public static boolean SERVER_LIST_BRACKET;
   public static boolean SERVER_LIST_IS_PVP;
   public static int SERVER_LIST_TYPE;
   public static int SERVER_LIST_AGE;
   public static boolean SERVER_GMONLY;
   public static boolean ENABLE_VITALITY;
   public static boolean RECOVER_VITALITY_ON_RECONNECT;
   public static float RATE_VITALITY_LEVEL_1;
   public static float RATE_VITALITY_LEVEL_2;
   public static float RATE_VITALITY_LEVEL_3;
   public static float RATE_VITALITY_LEVEL_4;
   public static float RATE_RECOVERY_VITALITY_PEACE_ZONE;
   public static float RATE_VITALITY_LOST;
   public static float RATE_VITALITY_GAIN;
   public static float RATE_RECOVERY_ON_RECONNECT;
   public static int STARTING_VITALITY_POINTS;
   public static double VITALITY_RAID_BONUS;
   public static double VITALITY_NEVIT_UP_POINT;
   public static double VITALITY_NEVIT_POINT;
   public static boolean ALLOW_NEVIT_SYSTEM;
   public static int NEVIT_ADVENT_TIME;
   public static int NEVIT_MAX_POINTS;
   public static int NEVIT_BONUS_EFFECT_TIME;
   public static boolean ALLOW_RECO_BONUS_SYSTEM;
   public static int MAX_ITEM_IN_PACKET;
   public static boolean CHECK_KNOWN;
   public static String EXTERNAL_HOSTNAME;
   public static int PVP_NORMAL_TIME;
   public static int PVP_PVP_TIME;
   public static Config.IdFactoryType IDFACTORY_TYPE;
   public static boolean BAD_ID_CHECKING;
   public static double ENCHANT_CHANCE_ELEMENT_STONE;
   public static double ENCHANT_CHANCE_ELEMENT_CRYSTAL;
   public static double ENCHANT_CHANCE_ELEMENT_JEWEL;
   public static double ENCHANT_CHANCE_ELEMENT_ENERGY;
   public static boolean ENCHANT_ELEMENT_ALL_ITEMS;
   public static int[] ENCHANT_BLACKLIST;
   public static boolean SYSTEM_BLESSED_ENCHANT;
   public static int BLESSED_ENCHANT_SAVE;
   public static int AUGMENTATION_NG_SKILL_CHANCE;
   public static int AUGMENTATION_NG_GLOW_CHANCE;
   public static int AUGMENTATION_MID_SKILL_CHANCE;
   public static int AUGMENTATION_MID_GLOW_CHANCE;
   public static int AUGMENTATION_HIGH_SKILL_CHANCE;
   public static int AUGMENTATION_HIGH_GLOW_CHANCE;
   public static int AUGMENTATION_TOP_SKILL_CHANCE;
   public static int AUGMENTATION_TOP_GLOW_CHANCE;
   public static int AUGMENTATION_BASESTAT_CHANCE;
   public static int AUGMENTATION_ACC_SKILL_CHANCE;
   public static boolean RETAIL_LIKE_AUGMENTATION;
   public static int[] RETAIL_LIKE_AUGMENTATION_NG_CHANCE;
   public static int[] RETAIL_LIKE_AUGMENTATION_MID_CHANCE;
   public static int[] RETAIL_LIKE_AUGMENTATION_HIGH_CHANCE;
   public static int[] RETAIL_LIKE_AUGMENTATION_TOP_CHANCE;
   public static boolean RETAIL_LIKE_AUGMENTATION_ACCESSORY;
   public static int[] AUGMENTATION_BLACKLIST;
   public static boolean ALT_ALLOW_AUGMENT_PVP_ITEMS;
   public static boolean ALLOW_DROP_AUGMENT_ITEMS;
   public static boolean ALLOW_TRADE_AUGMENT_ITEMS;
   public static boolean ALLOW_SELL_AUGMENT_ITEMS;
   public static double HP_REGEN_MULTIPLIER;
   public static double MP_REGEN_MULTIPLIER;
   public static double CP_REGEN_MULTIPLIER;
   public static boolean AUTO_LOOT_BY_ID_SYSTEM;
   public static int[] AUTO_LOOT_BY_ID;
   public static boolean ALLOW_DAMAGE_INFO;
   public static int DAMAGE_INFO_UPDATE;
   public static int DAMAGE_INFO_LIMIT_TIME;
   public static int EPIDOS_POINTS_NEED;
   public static int ANTHARAS_WAIT_TIME;
   public static String ANTHARAS_RESPAWN_PATTERN;
   public static int VALAKAS_WAIT_TIME;
   public static String VALAKAS_RESPAWN_PATTERN;
   public static boolean VALAKAS_ATT_RESPAWN;
   public static String VALAKAS_ATT_RESPAWN_TIME;
   public static boolean VALAKAS_DAYS_RESPAWN;
   public static String VALAKAS_DAYS_RESPAWN_TIME;
   public static String BAIUM_RESPAWN_PATTERN;
   public static String CORE_RESPAWN_PATTERN;
   public static String ORFEN_RESPAWN_PATTERN;
   public static String QUEEN_ANT_RESPAWN_PATTERN;
   public static boolean BELETH_NO_CC;
   public static int BELETH_MIN_PLAYERS;
   public static String BELETH_RESPAWN_PATTERN;
   public static int BELETH_SPAWN_DELAY;
   public static int BELETH_ZONE_CLEAN_DELAY;
   public static int BELETH_CLONES_RESPAWN;
   public static String SAILREN_RESPAWN_PATTERN;
   public static int CHANCE_SPAWN;
   public static int RESPAWN_TIME;
   public static int SOD_TIAT_KILL_COUNT;
   public static long SOD_STAGE_2_LENGTH;
   public static int SOI_EKIMUS_KILL_COUNT;
   public static int MIN_EKIMUS_PLAYERS;
   public static int MAX_EKIMUS_PLAYERS;
   public static ArrayList<String> FILTER_LIST;
   public static ArrayList<String> BROADCAST_FILTER_LIST;
   public static boolean SECOND_AUTH_ENABLED;
   public static boolean SECOND_AUTH_STRONG_PASS;
   public static int SECOND_AUTH_MAX_ATTEMPTS;
   public static long SECOND_AUTH_BAN_TIME;
   public static String SECOND_AUTH_REC_LINK;
   public static boolean SECURITY_SKILL_CHECK;
   public static boolean SECURITY_SKILL_CHECK_CLEAR;
   public static boolean ENABLE_SAFE_ADMIN_PROTECTION;
   public static List<String> SAFE_ADMIN_NAMES;
   public static boolean SAFE_ADMIN_SHOW_ADMIN_ENTER;
   public static boolean BOTREPORT_ENABLE;
   public static String[] BOTREPORT_RESETPOINT_HOUR;
   public static long BOTREPORT_REPORT_DELAY;
   public static boolean BOTREPORT_ALLOW_REPORTS_FROM_SAME_CLAN_MEMBERS;
   public static int DEFAULT_PUNISH;
   public static int PUNISH_VALID_ATTEMPTS;
   public static int DEFAULT_PUNISH_PARAM;
   public static boolean ONLY_GM_ITEMS_FREE;
   public static boolean JAIL_IS_PVP;
   public static boolean JAIL_DISABLE_CHAT;
   public static String EMAIL_SERVERINFO_NAME;
   public static String EMAIL_SERVERINFO_ADDRESS;
   public static boolean EMAIL_SYS_ENABLED;
   public static String EMAIL_SYS_HOST;
   public static int EMAIL_SYS_PORT;
   public static boolean EMAIL_SYS_SMTP_AUTH;
   public static String EMAIL_SYS_FACTORY;
   public static boolean EMAIL_SYS_FACTORY_CALLBACK;
   public static String EMAIL_SYS_USERNAME;
   public static String EMAIL_SYS_PASSWORD;
   public static String EMAIL_SYS_ADDRESS;
   public static String EMAIL_SYS_SELECTQUERY;
   public static String EMAIL_SYS_DBFIELD;
   public static int CHS_CLAN_MINLEVEL;
   public static int CHS_MAX_ATTACKERS;
   public static int CHS_MAX_FLAGS_PER_CLAN;
   public static boolean CHS_ENABLE_FAME;
   public static int CHS_FAME_AMOUNT;
   public static int CHS_FAME_FREQUENCY;
   public static int CLAN_HALL_HWID_LIMIT;
   public static boolean MULTILANG_ENABLE;
   public static List<String> MULTILANG_ALLOWED = new ArrayList<>();
   public static String MULTILANG_DEFAULT;
   public static boolean MULTILANG_VOICED_ALLOW;
   public static boolean ALLOW_EXP_GAIN_COMMAND;
   public static boolean ALLOW_AUTOLOOT_COMMAND;
   public static boolean VOICE_ONLINE_ENABLE;
   public static double FAKE_ONLINE;
   public static boolean ALLOW_TELETO_LEADER;
   public static int TELETO_LEADER_ID;
   public static int TELETO_LEADER_COUNT;
   public static boolean ALLOW_REPAIR_COMMAND;
   public static boolean ALLOW_VISUAL_ARMOR_COMMAND;
   public static boolean ALLOW_SEVENBOSSES_COMMAND;
   public static boolean ALLOW_ANCIENT_EXCHANGER_COMMAND;
   public static boolean ALLOW_SELLBUFFS_COMMAND;
   public static boolean SELLBUFF_USED_MP;
   public static Map<String, Integer> SELLBUFF_CURRECY_LIST;
   public static int SELLBUFF_MIN_PRICE;
   public static int SELLBUFF_MAX_PRICE;
   public static int SELLBUFF_MAX_BUFFS;
   public static boolean ALLOW_SELLBUFFS_PETS;
   public static boolean ALLOW_STATS_COMMAND;
   public static boolean ALLOW_BLOCKBUFFS_COMMAND;
   public static boolean ALLOW_HIDE_TRADERS_COMMAND;
   public static boolean ALLOW_HIDE_BUFFS_ANIMATION_COMMAND;
   public static boolean ALLOW_BLOCK_TRADERS_COMMAND;
   public static boolean ALLOW_BLOCK_PARTY_COMMAND;
   public static boolean ALLOW_BLOCK_FRIEND_COMMAND;
   public static boolean ALLOW_MENU_COMMAND;
   public static boolean ALLOW_SECURITY_COMMAND;
   public static boolean ALLOW_IP_LOCK;
   public static boolean ALLOW_HWID_LOCK;
   public static int PUNISH_LOCK_DELAY;
   public static boolean ALLOW_FIND_PARTY;
   public static boolean PARTY_LEADER_ONLY_CAN_INVITE;
   public static int FIND_PARTY_REFRESH_TIME;
   public static int FIND_PARTY_FLOOD_TIME;
   public static int FIND_PARTY_MIN_LEVEL;
   public static boolean ALLOW_ENCHANT_SERVICE;
   public static boolean ENCHANT_SERVICE_ONLY_FOR_PREMIUM;
   public static boolean ENCHANT_ALLOW_BELTS;
   public static boolean ENCHANT_ALLOW_SCROLLS;
   public static boolean ENCHANT_ALLOW_ATTRIBUTE;
   public static int ENCHANT_MAX_WEAPON;
   public static int ENCHANT_MAX_ARMOR;
   public static int ENCHANT_MAX_JEWELRY;
   public static int ENCHANT_MAX_ITEM_LIMIT;
   public static int ENCHANT_CONSUME_ITEM;
   public static int ENCHANT_CONSUME_ITEM_COUNT;
   public static int enchantServiceDefaultLimit;
   public static int enchantServiceDefaultEnchant;
   public static int enchantServiceDefaultAttribute;
   public static int ENCHANT_SCROLL_CHANCE_CORRECT;
   public static boolean ALLOW_RELOG_COMMAND;
   public static boolean ALLOW_PARTY_RANK_COMMAND;
   public static boolean ALLOW_PARTY_RANK_ONLY_FOR_CC;
   public static boolean PARTY_RANK_AUTO_OPEN;
   public static boolean ALLOW_RECOVERY_ITEMS;
   public static int RECOVERY_ITEMS_HOURS;
   public static boolean ALLOW_PROMOCODES_COMMAND;
   public static int PROMOCODES_USE_DELAY;
   public static boolean ALLOW_AUTO_FISH_SHOTS;
   public static boolean ALLOW_CUSTOM_INTERFACE;
   public static boolean CUSTOM_INTERFACE_VERSION_OLD;
   public static String SERVER_NAME;
   public static boolean ONLINE_PLAYERS_AT_STARTUP;
   public static int ONLINE_PLAYERS_ANNOUNCE_INTERVAL;
   public static boolean ALLOW_NEW_CHARACTER_TITLE;
   public static String NEW_CHARACTER_TITLE;
   public static boolean NEW_CHAR_IS_NOBLE;
   public static boolean NEW_CHAR_IS_HERO;
   public static boolean UNSTUCK_SKILL;
   public static boolean ALLOW_NEW_CHAR_CUSTOM_POSITION;
   public static int NEW_CHAR_POSITION_X;
   public static int NEW_CHAR_POSITION_Y;
   public static int NEW_CHAR_POSITION_Z;
   public static boolean ENABLE_NOBLESS_COLOR;
   public static int NOBLESS_COLOR_NAME;
   public static boolean ENABLE_NOBLESS_TITLE_COLOR;
   public static int NOBLESS_COLOR_TITLE_NAME;
   public static boolean INFINITE_SOUL_SHOT;
   public static boolean INFINITE_BEAST_SOUL_SHOT;
   public static boolean INFINITE_BEAST_SPIRIT_SHOT;
   public static boolean INFINITE_SPIRIT_SHOT;
   public static boolean INFINITE_BLESSED_SPIRIT_SHOT;
   public static boolean INFINITE_ARROWS;
   public static boolean ENTER_HELLBOUND_WITHOUT_QUEST;
   public static int AUTO_RESTART_TIME;
   public static String AUTO_RESTART_PATTERN;
   public static boolean SPEED_UP_RUN;
   public static int DISCONNECT_TIMEOUT;
   public static boolean DISCONNECT_SYSTEM_ENABLED;
   public static String DISCONNECT_TITLECOLOR;
   public static String DISCONNECT_TITLE;
   public static boolean CUSTOM_ENCHANT_ITEMS_ENABLED;
   public static Map<Integer, Float> ENCHANT_ITEMS_ID;
   public static boolean ALLOW_UNLIM_ENTER_CATACOMBS;
   public static boolean AUTO_POINTS_SYSTEM;
   public static List<Integer> AUTO_HP_VALID_ITEMS;
   public static List<Integer> AUTO_MP_VALID_ITEMS;
   public static List<Integer> AUTO_CP_VALID_ITEMS;
   public static List<Integer> AUTO_SOUL_VALID_ITEMS;
   public static int DEFAULT_HP_PERCENT;
   public static int DEFAULT_MP_PERCENT;
   public static int DEFAULT_CP_PERCENT;
   public static int DEFAULT_SOUL_AMOUNT;
   public static boolean DISABLE_WITHOUT_POTIONS;
   public static double SELL_PRICE_MODIFIER;
   public static boolean ALT_KAMALOKA_SOLO_PREMIUM_ONLY;
   public static boolean ALT_KAMALOKA_ESSENCE_PREMIUM_ONLY;
   public static boolean ITEM_BROKER_ITEM_SEARCH;
   public static int ITEM_BROKER_ITEMS_PER_PAGE;
   public static int ITEM_BROKER_PAGES_PER_LIST;
   public static long ITEM_BROKER_TIME_UPDATE;
   public static boolean ALLOW_BLOCK_TRANSFORMS_AT_SIEGE;
   public static List<Integer> LIST_BLOCK_TRANSFORMS_AT_SIEGE;
   public static boolean PC_BANG_ENABLED;
   public static boolean PC_BANG_ONLY_FOR_PREMIUM;
   public static int PC_POINT_ID;
   public static int PC_BANG_MIN_LEVEL;
   public static int PC_BANG_POINTS_MIN;
   public static int PC_BANG_POINTS_PREMIUM_MIN;
   public static int PC_BANG_POINTS_MAX;
   public static int PC_BANG_POINTS_PREMIUM_MAX;
   public static int MAX_PC_BANG_POINTS;
   public static boolean ENABLE_DOUBLE_PC_BANG_POINTS;
   public static int DOUBLE_PC_BANG_POINTS_CHANCE;
   public static int PC_BANG_INTERVAL;
   public static boolean ALLOW_COMMUNITY;
   public static boolean BLOCK_COMMUNITY_IN_PVP_ZONE;
   public static List<String> DISABLE_COMMUNITY_BYPASSES;
   public static String BBS_HOME_PAGE;
   public static String BBS_FAVORITE_PAGE;
   public static String BBS_LINK_PAGE;
   public static String BBS_REGION_PAGE;
   public static String BBS_CLAN_PAGE;
   public static String BBS_MEMO_PAGE;
   public static String BBS_MAIL_PAGE;
   public static String BBS_FRIENDS_PAGE;
   public static String BBS_ADDFAV_PAGE;
   public static boolean ALLOW_SENDING_IMAGES;
   public static boolean ALLOW_COMMUNITY_PEACE_ZONE;
   public static List<Integer> AVALIABLE_COMMUNITY_MULTISELLS;
   public static boolean ALLOW_COMMUNITY_BUFF_IN_SIEGE;
   public static boolean ALLOW_COMMUNITY_TELEPORT_IN_SIEGE;
   public static boolean BLOCK_TP_AT_SIEGES_FOR_ALL;
   public static boolean ALLOW_COMMUNITY_COORDS_TP;
   public static boolean ALLOW_COMMUNITY_TP_NO_RESTART_ZONES;
   public static boolean ALLOW_COMMUNITY_TP_SIEGE_ZONES;
   public static int COMMUNITY_TELEPORT_TABS;
   public static int COMMUNITY_FREE_TP_LVL;
   public static int COMMUNITY_FREE_BUFF_LVL;
   public static int INTERVAL_STATS_UPDATE;
   public static boolean ALLOW_BUFF_PEACE_ZONE;
   public static boolean ALLOW_BUFF_WITHOUT_PEACE_FOR_PREMIUM;
   public static boolean FREE_ALL_BUFFS;
   public static boolean ALLOW_SCHEMES_FOR_PREMIUMS;
   public static int BUFF_ID_ITEM;
   public static boolean ALLOW_HEAL_ONLY_PEACE;
   public static int BUFF_AMOUNT;
   public static int CANCEL_BUFF_AMOUNT;
   public static int HPMPCP_BUFF_AMOUNT;
   public static int SET_BUFF_AMOUNT;
   public static int BUFF_MAX_SCHEMES;
   public static boolean SERVICES_LEVELUP_ENABLE;
   public static boolean SERVICES_DELEVEL_ENABLE;
   public static int[] SERVICES_LEVELUP_ITEM = new int[2];
   public static int[] SERVICES_DELEVEL_ITEM = new int[2];
   public static int[] SERVICES_GIVENOOBLESS_ITEM = new int[2];
   public static int[] SERVICES_CHANGEGENDER_ITEM = new int[2];
   public static int[] SERVICES_GIVEHERO_ITEM = new int[2];
   public static int SERVICES_GIVEHERO_TIME;
   public static int[] SERVICES_RECOVERYPK_ITEM = new int[2];
   public static int[] SERVICES_RECOVERYKARMA_ITEM = new int[2];
   public static int[] SERVICES_RECOVERYVITALITY_ITEM = new int[2];
   public static int[] SERVICES_GIVESP_ITEM = new int[2];
   public static int[] SERVICES_NAMECHANGE_ITEM = new int[2];
   public static String SERVICES_NAMECHANGE_TEMPLATE;
   public static int[] SERVICES_CLANNAMECHANGE_ITEM = new int[2];
   public static int[] SERVICES_UNBAN_ITEM = new int[2];
   public static int[] SERVICES_CLANLVL_ITEM = new int[2];
   public static int[] SERVICES_CLANSKILLS_ITEM = new int[2];
   public static int[] SERVICES_GIVEREC_ITEM = new int[2];
   public static int[] SERVICES_GIVEREP_ITEM = new int[2];
   public static int SERVICES_REP_COUNT;
   public static int[] SERVICES_GIVEFAME_ITEM = new int[2];
   public static int[] SERVICES_CLAN_CREATE_PENALTY_ITEM = new int[2];
   public static int[] SERVICES_CLAN_JOIN_PENALTY_ITEM = new int[2];
   public static int SERVICES_FAME_COUNT;
   public static boolean SERVICES_AUGMENTATION_FORMATE;
   public static int[] SERVICES_AUGMENTATION_ITEM = new int[2];
   public static List<Integer> SERVICES_AUGMENTATION_AVAILABLE_LIST = new ArrayList<>();
   public static List<Integer> SERVICES_AUGMENTATION_DISABLED_LIST = new ArrayList<>();
   public static int BBS_FORGE_ENCHANT_ITEM;
   public static int BBS_FORGE_ENCHANT_START;
   public static int BBS_FORGE_FOUNDATION_ITEM;
   public static int[] BBS_FORGE_FOUNDATION_PRICE_ARMOR;
   public static int[] BBS_FORGE_FOUNDATION_PRICE_WEAPON;
   public static int[] BBS_FORGE_FOUNDATION_PRICE_JEWEL;
   public static int[] BBS_FORGE_ENCHANT_MAX;
   public static int[] BBS_FORGE_WEAPON_ENCHANT_LVL;
   public static int[] BBS_FORGE_ARMOR_ENCHANT_LVL;
   public static int[] BBS_FORGE_JEWELS_ENCHANT_LVL;
   public static int[] BBS_FORGE_ENCHANT_PRICE_WEAPON;
   public static int[] BBS_FORGE_ENCHANT_PRICE_ARMOR;
   public static int[] BBS_FORGE_ENCHANT_PRICE_JEWELS;
   public static int BBS_FORGE_WEAPON_ATTRIBUTE_MAX;
   public static int BBS_FORGE_ARMOR_ATTRIBUTE_MAX;
   public static int[] BBS_FORGE_ATRIBUTE_LVL_WEAPON;
   public static int[] BBS_FORGE_ATRIBUTE_LVL_ARMOR;
   public static int[] BBS_FORGE_ATRIBUTE_PRICE_ARMOR;
   public static int[] BBS_FORGE_ATRIBUTE_PRICE_WEAPON;
   public static int[] SERVICES_SOUL_CLOAK_TRANSFER_ITEM = new int[2];
   public static int SERVICES_OLF_STORE_ITEM;
   public static int SERVICES_OLF_STORE_0_PRICE;
   public static int SERVICES_OLF_STORE_6_PRICE;
   public static int SERVICES_OLF_STORE_7_PRICE;
   public static int SERVICES_OLF_STORE_8_PRICE;
   public static int SERVICES_OLF_STORE_9_PRICE;
   public static int SERVICES_OLF_STORE_10_PRICE;
   public static int[] SERVICES_OLF_TRANSFER_ITEM = new int[2];
   public static boolean ENABLE_MULTI_AUCTION_SYSTEM;
   public static long AUCTION_FEE;
   public static boolean ALLOW_AUCTION_OUTSIDE_TOWN;
   public static boolean ALLOW_ADDING_AUCTION_DELAY;
   public static int SECONDS_BETWEEN_ADDING_AUCTIONS;
   public static boolean AUCTION_PRIVATE_STORE_AUTO_ADDED;
   public static int[] BBS_BOSSES_TO_NOT_SHOW;
   public static boolean ALLOW_BOSS_RESPAWN_TIME;
   public static int[] SERVICES_PREMIUM_VALID_ID;
   public static boolean ALLOW_CERT_DONATE_MODE;
   public static int CERT_MIN_LEVEL;
   public static String CERT_BLOCK_SKILL_LIST;
   public static int[] EMERGET_SKILLS_LEARN = new int[2];
   public static int[] MASTER_SKILLS_LEARN = new int[2];
   public static int[] TRANSFORM_SKILLS_LEARN = new int[2];
   public static int[] CLEAN_SKILLS_LEARN = new int[2];
   public static boolean ALLOW_TELEPORT_TO_RAID;
   public static int[] TELEPORT_TO_RAID_PRICE = new int[2];
   public static List<Integer> BLOCKED_RAID_LIST = new ArrayList<>();
   public static Map<Integer, String> CHANGE_COLOR_TITLE_LIST;
   public static Map<Integer, String> CHANGE_COLOR_NAME_LIST;
   public static int[] SERVICES_EXPAND_INVENTORY = new int[2];
   public static int[] SERVICES_EXPAND_WAREHOUSE = new int[2];
   public static int[] SERVICES_EXPAND_SELLSTORE = new int[2];
   public static int[] SERVICES_EXPAND_BUYSTORE = new int[2];
   public static int[] SERVICES_EXPAND_DWARFRECIPE = new int[2];
   public static int[] SERVICES_EXPAND_COMMONRECIPE = new int[2];
   public static int EXPAND_INVENTORY_STEP;
   public static int EXPAND_WAREHOUSE_STEP;
   public static int EXPAND_SELLSTORE_STEP;
   public static int EXPAND_BUYSTORE_STEP;
   public static int EXPAND_DWARFRECIPE_STEP;
   public static int EXPAND_COMMONRECIPE_STEP;
   public static int SERVICES_EXPAND_INVENTORY_LIMIT;
   public static int SERVICES_EXPAND_WAREHOUSE_LIMIT;
   public static int SERVICES_EXPAND_SELLSTORE_LIMIT;
   public static int SERVICES_EXPAND_BUYSTORE_LIMIT;
   public static int SERVICES_EXPAND_DWARFRECIPE_LIMIT;
   public static int SERVICES_EXPAND_COMMONRECIPE_LIMIT;
   public static String SERVICES_ACADEMY_REWARD;
   public static long ACADEMY_MIN_ADENA_AMOUNT;
   public static long ACADEMY_MAX_ADENA_AMOUNT;
   public static long MAX_TIME_IN_ACADEMY;
   public static int ACADEMY_INVITE_DELAY;
   public static boolean HITMAN_TAKE_KARMA;
   public static boolean HITMAN_ANNOUNCE;
   public static int HITMAN_MAX_PER_PAGE;
   public static List<Integer> HITMAN_CURRENCY;
   public static boolean HITMAN_SAME_TEAM;
   public static int HITMAN_TARGETS_LIMIT;
   public static int HITMAN_SAVE_TARGET;
   public static boolean ENABLED_LEPRECHAUN;
   public static int LEPRECHAUN_ID;
   public static int LEPRECHAUN_FIRST_SPAWN_DELAY;
   public static int LEPRECHAUN_RESPAWN_INTERVAL;
   public static int LEPRECHAUN_SPAWN_TIME;
   public static int LEPRECHAUN_ANNOUNCE_INTERVAL;
   public static boolean SHOW_NICK;
   public static boolean SHOW_REGION;
   public static int[] LEPRECHAUN_REWARD_ID;
   public static int[] LEPRECHAUN_REWARD_COUNT;
   public static int[] LEPRECHAUN_REWARD_CHANCE;
   public static String UC_START_TIME;
   public static int UC_TIME_PERIOD;
   public static boolean UC_ANNOUNCE_BATTLES;
   public static int UC_PARTY_LIMIT;
   public static int UC_RESS_TIME;
   public static int GAME_POINT_ITEM_ID;
   public static int CLEFT_MIN_TEAM_PLAYERS;
   public static boolean CLEFT_BALANCER;
   public static int CLEFT_WAR_TIME;
   public static int CLEFT_COLLECT_TIME;
   public static int CLEFT_REWARD_ID;
   public static int CLEFT_REWARD_COUNT_WINNER;
   public static int CLEFT_REWARD_COUNT_LOOSER;
   public static int CLEFT_MIN_PLAYR_EVENT_TIME;
   public static boolean CLEFT_WITHOUT_SEEDS;
   public static int CLEFT_MIN_LEVEL;
   public static int CLEFT_TIME_RELOAD_REG;
   public static int CLEFT_MAX_PLAYERS;
   public static int CLEFT_RESPAWN_DELAY;
   public static int CLEFT_LEAVE_DELAY;
   public static int LARGE_COMPRESSOR_POINT;
   public static int SMALL_COMPRESSOR_POINT;
   public static int TEAM_CAT_POINT;
   public static int TEAM_PLAYER_POINT;
   public static boolean ENABLE_OLY_FEED;
   public static int OLY_ANTI_FEED_WEAPON_RIGHT;
   public static int OLY_ANTI_FEED_WEAPON_LEFT;
   public static int OLY_ANTI_FEED_GLOVES;
   public static int OLY_ANTI_FEED_CHEST;
   public static int OLY_ANTI_FEED_LEGS;
   public static int OLY_ANTI_FEED_FEET;
   public static int OLY_ANTI_FEED_CLOAK;
   public static int OLY_ANTI_FEED_RIGH_HAND_ARMOR;
   public static int OLY_ANTI_FEED_HAIR_MISC_1;
   public static int OLY_ANTI_FEED_HAIR_MISC_2;
   public static int OLY_ANTI_FEED_RACE;
   public static int OLY_ANTI_FEED_GENDER;
   public static int OLY_ANTI_FEED_CLASS_RADIUS;
   public static int OLY_ANTI_FEED_CLASS_HEIGHT;
   public static int OLY_ANTI_FEED_PLAYER_HAVE_RECS;
   public static boolean GEODATA;
   public static boolean SYNC_BY_GEO;
   public static boolean ACCEPT_GEOEDITOR_CONN;
   public static int CLIENT_SHIFTZ;
   public static boolean PATH_CLEAN;
   public static int MAX_Z_DIFF;
   public static int MAX_WRONG_ATTEMPTS;
   public static boolean ALLOW_FALL_FROM_WALLS;
   public static int MIN_LAYER_HEIGHT;
   public static int REGION_EDGE_MAX_Z_DIFF;
   public static int PATHFIND_MAX_Z_DIFF;
   public static int PATHFIND_BOOST;
   public static boolean PATHFIND_DIAGONAL;
   public static int PATHFIND_MAP_MUL;
   public static String PATHFIND_BUFFERS;
   public static String GEOFILES_PATTERN;
   public static boolean COMPACT_GEO;
   public static long PATHFIND_MAX_TIME;
   public static String VERTICAL_SPLIT_REGIONS;
   public static boolean ENABLE_ANTI_BOT_SYSTEM;
   public static int ASK_ANSWER_DELAY;
   public static int MINIMUM_TIME_QUESTION_ASK;
   public static int MAXIMUM_TIME_QUESTION_ASK;
   public static int MINIMUM_BOT_POINTS_TO_STOP_ASKING;
   public static int MAXIMUM_BOT_POINTS_TO_STOP_ASKING;
   public static int MAX_BOT_POINTS;
   public static int MINIMAL_BOT_RATING_TO_BAN;
   public static boolean ANNOUNCE_AUTO_BOT_BAN;
   public static boolean ON_WRONG_QUESTION_KICK;
   public static boolean ALLOW_CUSTOM_RATES;
   public static float MONDAY_RATE_EXP;
   public static float MONDAY_RATE_SP;
   public static float TUESDAY_RATE_EXP;
   public static float TUESDAY_RATE_SP;
   public static float WEDNESDAY_RATE_EXP;
   public static float WEDNESDAY_RATE_SP;
   public static float THURSDAY_RATE_EXP;
   public static float THURSDAY_RATE_SP;
   public static float FRIDAY_RATE_EXP;
   public static float FRIDAY_RATE_SP;
   public static float SATURDAY_RATE_EXP;
   public static float SATURDAY_RATE_SP;
   public static float SUNDAY_RATE_EXP;
   public static float SUNDAY_RATE_SP;
   public static boolean ALLOW_FIGHT_EVENTS;
   public static boolean ALLOW_RESPAWN_PROTECT_PLAYER;
   public static boolean ALLOW_REG_CONFIRM_DLG;
   public static int[] DISALLOW_FIGHT_EVENTS;
   public static boolean FIGHT_EVENTS_EQUALIZE_ROOMS;
   public static int FIGHT_EVENTS_MIN_LVL;
   public static int FIGHT_EVENTS_REWARD_MULTIPLIER;
   public static int TIME_FIRST_TELEPORT;
   public static int TIME_PLAYER_TELEPORTING;
   public static int TIME_PREPARATION_BEFORE_FIRST_ROUND;
   public static int TIME_PREPARATION_BETWEEN_NEXT_ROUNDS;
   public static int TIME_AFTER_ROUND_END_TO_RETURN_SPAWN;
   public static int TIME_TELEPORT_BACK_TOWN;
   public static int TIME_MAX_SECONDS_OUTSIDE_ZONE;
   public static int TIME_TO_BE_AFK;
   public static int ITEMS_FOR_MINUTE_OF_AFK;
   public static boolean ALLOW_FAKE_PLAYERS;
   public static boolean ALLOW_ENCHANT_WEAPONS;
   public static boolean ALLOW_ENCHANT_ARMORS;
   public static boolean ALLOW_ENCHANT_JEWERLYS;
   public static int[] RND_ENCHANT_WEAPONS = new int[2];
   public static int[] RND_ENCHANT_ARMORS = new int[2];
   public static int[] RND_ENCHANT_JEWERLYS = new int[2];
   public static int[][] FAKE_FIGHTER_BUFFS;
   public static int[][] FAKE_MAGE_BUFFS;
   public static boolean ALLOW_SPAWN_FAKE_PLAYERS;
   public static int ENCHANTERS_MAX_LVL;
   public static int FAKE_PLAYERS_AMOUNT;
   public static String DEFAULT_GLOBAL_CHAT;
   public static String DEFAULT_TRADE_CHAT;
   public static boolean USE_SAY_FILTER;
   public static boolean USE_BROADCAST_SAY_FILTER;
   public static String CHAT_FILTER_CHARS;
   public static int[] BAN_CHAT_CHANNELS;
   public static boolean ALLOW_CUSTOM_CHAT;
   public static int CHECK_CHAT_VALID;
   public static int CHAT_MSG_SIMPLE;
   public static int CHAT_MSG_PREMIUM;
   public static int CHAT_MSG_ANNOUNCE;
   public static int MIN_LVL_GLOBAL_CHAT;
   public static boolean ALLOW_STREAM_SYSTEM;
   public static boolean ALLOW_STREAM_LOGS;
   public static String TWITCH_CLIENT_ID;
   public static int TWITCH_CHECK_DELAY;
   public static String TWITCH_ACTIVE_STREAMS_URL;
   public static boolean ALLOW_STREAM_GAME_NAME;
   public static String TWITCH_CORRECT_STREAM_GAME;
   public static String TWITCH_CORRECT_STREAM_TITLE;
   public static boolean ALLOW_STREAM_PANEL;
   public static boolean ALLOW_STREAM_SAVE_DB;
   public static boolean ALLOW_STREAM_NEED_APPROVAL;
   public static boolean ALLOW_STREAM_SAME_DB;
   public static String STREAM_DB_URL;
   public static String STREAM_DB_LOGIN;
   public static String STREAM_DB_PASS;
   public static boolean ALLOW_REWARD_STREAMERS;
   public static int TIME_TO_REWARD_STREAMERS;
   public static int MAX_SEC_TO_REWARD_STREAMERS;
   public static int TOTAL_REWARD_STREAMERS;
   public static String STREAMING_REWARDS;
   public static boolean ALLOW_INCREASE_REWARD;
   public static boolean ALLOW_STREAM_AFK_SYSTEM;
   public static int STREAM_AFK_SECONDS;
   public static int STREAM_AFK_DELAY;
   public static boolean ALLOW_FACEBOOK_SYSTEM;
   public static int FACEBOOK_MIN_LVL;
   public static String FACEBOOK_NAME_SYNTAX;
   public static boolean ALLOW_REG_EXACT_COMMENTS;
   public static int FACEBOOK_TIME_LIMIT;
   public static int FACEBOOK_TIME_LIMIT_DELAY;
   public static int FACEBOOK_DELAY_BETWEEN_TASK;
   public static int FACEBOOK_COMMENT_REUSE;
   public static Map<Integer, Long> FACEBOOK_REWARD_LIKE_TASK;
   public static Map<Integer, Long> FACEBOOK_REWARD_LIKE_NO_TASK;
   public static Map<Integer, Long> FACEBOOK_REWARD_COMMENT_TASK;
   public static Map<Integer, Long> FACEBOOK_REWARD_COMMENT_NO_TASK;
   public static boolean ALLOW_AUTO_ANNONCE_NOT_CONNECT;
   public static int FACEBOOK_AUTO_ANNONCE_NOT_DEDAY;
   public static boolean ALLOW_AUTO_ANNONCE_CONNECTED;
   public static int FACEBOOK_AUTO_ANNONCE_DEDAY;
   public static boolean ALLOW_NEGATIVE_POINTS;
   public static int FACEBOOK_NEGATIVE_POINTS_DELAY;
   public static int FACEBOOK_EXTRACTION_DELAY;
   public static int FACEBOOK_VALID_POST_COUNT;
   public static String FACEBOOK_PAGE_NAME;
   public static String FACEBOOK_TOKEN;
   public static boolean WEEKLY_TRADER_ENABLE;
   public static int WEEKLY_TRADER_DAY_OF_WEEK;
   public static int WEEKLY_TRADER_HOUR_OF_DAY;
   public static int WEEKLY_TRADER_MINUTE_OF_DAY;
   public static int WEEKLY_TRADER_DURATION;
   public static int WEEKLY_TRADER_MULTISELL_ID;
   public static boolean ALLOW_DAILY_REWARD;
   public static boolean ALLOW_DAILY_TASKS;
   public static boolean ALLOW_VISUAL_SYSTEM;
   public static boolean ALLOW_VIP_SYSTEM;
   public static boolean ALLOW_REVENGE_SYSTEM;

   public static void load() {
      _log.info("Loading configuration files...");
      InputStream is = null;

      try {
         loadPersonalSettings(_personalConfigs);
         IPSettings.getInstance();
         loadServerSettings(is);
         loadSecuritySettings(is);
         loadFeatureSettings(is);
         loadCreaureSettings(is);
         loadMmoSettings(is);
         loadIdFactorySettings(is);
         loadGeneralSettings(is);
         loadFloodProtectorSettings(is);
         loadNpcsSettings(is);
         loadRatesSettings(is);
         loadPvpSettings(is);
         loadOlympiadSettings(is);
         loadEpicsSettings(is);
         loadGraciaSettings(is);
         loadFilterSettings();
         loadBroadCastFilterSettings();
         loadClanhallSiegeSettings(is);
         loadLanguageSettings(is);
         loadVoiceSettings(is);
         loadCustomSettings(is);
         loadPcBangSettings(is);
         loadPremiumSettings(is);
         loadCommunitySettings(is);
         loadWeddingSettings(is);
         loadOfflineTradeSettings(is);
         loadDualSessionSettings(is);
         loadEnchantSettings(is);
         loadHitmanSettings(is);
         loadUndergroundColliseumSettings(is);
         loadItemMallSettings(is);
         loadLeprechaunSettings(is);
         loadAerialCleftSettings(is);
         loadOlyAntiFeedSettings(is);
         loadGeodataSettings(is);
         loadAntiBotSettings(is);
         loadCustomRatesSettings(is);
         loadFakeSettings(is);
         loadFightEventsSettings(is);
         loadChatSettings(is);
         loadStreamersSettings(is);
         loadFacebookSettings(is);
         loadWeeklyTraderSettings(is);
         loadModsSettings();
         BotFunctions.getInstance();
         FarmSettings.getInstance();
         AcademyRewards.getInstance().load();
      } finally {
         try {
            if (is != null) {
               is.close();
            }
         } catch (Exception var7) {
         }
      }
   }

   private static void loadPersonalSettings(HashMap<String, String> map) {
      map.clear();
      Pattern LINE_PATTERN = Pattern.compile("^(((?!=).)+)=(.*?)$");
      Scanner scanner = null;

      try {
         File file = new File("./config/main/personal.ini");
         String content = Files.readFile(file);
         scanner = new Scanner(content);

         while(scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (!line.startsWith("#")) {
               Matcher m = LINE_PATTERN.matcher(line);
               if (m.find()) {
                  String name = m.group(1).trim();
                  String value = m.group(3).trim();
                  map.put(name, value);
               }
            }
         }
      } catch (IOException var16) {
         _log.warning("Config: " + var16.getMessage());
         throw new Error("Failed to Load ./config/main/personal.ini File.");
      } finally {
         try {
            scanner.close();
         } catch (Exception var15) {
         }
      }
   }

   private static void loadServerSettings(InputStream is) {
      try {
         GameSettings serverSettings = new GameSettings();
         InputStream var11 = new FileInputStream(new File("./config/network/server.ini"));
         serverSettings.load(var11);
         USER_NAME = serverSettings.getProperty("UserName");
         USER_KEY = serverSettings.getProperty("UserKey");
         PROTECTION = serverSettings.getProperty("Protection");
         ALLOW_MULILOGIN = Boolean.parseBoolean(serverSettings.getProperty("AllowMultilogin", "False"));
         DATABASE_DRIVER = serverSettings.getProperty("Driver", "com.mysql.jdbc.Driver");
         DATABASE_URL = serverSettings.getProperty("URL", "jdbc:mysql://localhost/l2jgs");
         DATABASE_LOGIN = serverSettings.getProperty("Login", "root");
         DATABASE_PASSWORD = serverSettings.getProperty("Password", "");
         DATABASE_MAX_CONNECTIONS = Integer.parseInt(serverSettings.getProperty("MaximumDbConnections", "10"));
         DATABASE_MAX_IDLE_TIMEOUT = Integer.parseInt(serverSettings.getProperty("MaxIdleConnectionTimeout", "600"));
         DATABASE_IDLE_TEST_PERIOD = Integer.parseInt(serverSettings.getProperty("IdleConnectionTestPeriod", "60"));
         SERVER_LIST_BRACKET = Boolean.parseBoolean(serverSettings.getProperty("ServerListBrackets", "false"));
         SERVER_LIST_IS_PVP = Boolean.parseBoolean(serverSettings.getProperty("ServerListIsPvp", "false"));
         SERVER_LIST_TYPE = getServerTypeId(serverSettings.getProperty("ServerListType", "Normal").split(","));
         SERVER_LIST_AGE = Integer.parseInt(serverSettings.getProperty("ServerListAge", "0"));
         SERVER_GMONLY = Boolean.parseBoolean(serverSettings.getProperty("ServerGMOnly", "false"));

         try {
            DATAPACK_ROOT = new File(serverSettings.getProperty("DatapackRoot", ".").replaceAll("\\\\", "/")).getCanonicalFile();
         } catch (IOException var9) {
            _log.log(Level.WARNING, "Error setting datapack root!", (Throwable)var9);
            DATAPACK_ROOT = new File(".");
         }

         CNAME_TEMPLATE = serverSettings.getProperty("CnameTemplate", ".*");
         PET_NAME_TEMPLATE = serverSettings.getProperty("PetNameTemplate", ".*");
         CLAN_NAME_TEMPLATE = serverSettings.getProperty("ClanNameTemplate", ".*");
         MAX_CHARACTERS_NUMBER_PER_ACCOUNT = Integer.parseInt(serverSettings.getProperty("CharMaxNumber", "7"));
         MAXIMUM_ONLINE_USERS = Integer.parseInt(serverSettings.getProperty("MaximumOnlineUsers", "100"));
         String[] protocols = serverSettings.getProperty("AllowedProtocolRevisions", "267;268;271;273").split(";");
         PROTOCOL_LIST = new ArrayList<>(protocols.length);

         for(String protocol : protocols) {
            try {
               PROTOCOL_LIST.add(Integer.parseInt(protocol.trim()));
            } catch (NumberFormatException var8) {
               _log.info("Wrong config protocol version: " + protocol + ". Skipped.");
            }
         }
      } catch (Exception var10) {
         _log.warning("Config: " + var10.getMessage());
         throw new Error("Failed to Load ./config/network/server.ini File.");
      }
   }

   private static void loadSecuritySettings(InputStream is) {
      try {
         GameSettings securitySettings = new GameSettings();
         InputStream var8 = new FileInputStream(new File("./config/main/security.ini"));
         securitySettings.load(var8);
         SECOND_AUTH_ENABLED = Boolean.parseBoolean(securitySettings.getProperty("SecondAuthEnabled", "False"));
         SECOND_AUTH_STRONG_PASS = Boolean.parseBoolean(securitySettings.getProperty("SecondAuthStrongPassword", "true"));
         SECOND_AUTH_MAX_ATTEMPTS = Integer.parseInt(securitySettings.getProperty("SecondAuthMaxAttempts", "5"));
         SECOND_AUTH_BAN_TIME = (long)Integer.parseInt(securitySettings.getProperty("SecondAuthBanTime", "480"));
         SECOND_AUTH_REC_LINK = securitySettings.getProperty("SecondAuthRecoveryLink", "5");
         L2WALKER_PROTECTION = Boolean.parseBoolean(securitySettings.getProperty("L2WalkerProtection", "False"));
         SECURITY_SKILL_CHECK = Boolean.parseBoolean(securitySettings.getProperty("SkillsCheck", "False"));
         SECURITY_SKILL_CHECK_CLEAR = Boolean.parseBoolean(securitySettings.getProperty("SkillsCheckClear", "False"));
         ENABLE_SAFE_ADMIN_PROTECTION = Boolean.parseBoolean(securitySettings.getProperty("EnableSafeAdminProtection", "False"));
         String[] props = securitySettings.getProperty("SafeAdminName", "").split(",");
         SAFE_ADMIN_NAMES = new ArrayList<>(props.length);
         if (props.length != 0) {
            for(String name : props) {
               SAFE_ADMIN_NAMES.add(name);
            }
         }

         SAFE_ADMIN_SHOW_ADMIN_ENTER = Boolean.parseBoolean(securitySettings.getProperty("SafeAdminShowAdminEnter", "False"));
         BOTREPORT_ENABLE = Boolean.parseBoolean(securitySettings.getProperty("EnableBotReport", "False"));
         BOTREPORT_RESETPOINT_HOUR = securitySettings.getProperty("BotReportPointsResetHour", "00:00").split(":");
         BOTREPORT_REPORT_DELAY = (long)(Integer.parseInt(securitySettings.getProperty("BotReportDelay", "30")) * 60000);
         BOTREPORT_ALLOW_REPORTS_FROM_SAME_CLAN_MEMBERS = Boolean.parseBoolean(securitySettings.getProperty("AllowReportsFromSameClanMembers", "False"));
         PUNISH_VALID_ATTEMPTS = Integer.parseInt(securitySettings.getProperty("PunishValidAttempts", "5"));
         DEFAULT_PUNISH = Integer.parseInt(securitySettings.getProperty("DefaultPunish", "2"));
         DEFAULT_PUNISH_PARAM = Integer.parseInt(securitySettings.getProperty("DefaultPunishParam", "0"));
         ONLY_GM_ITEMS_FREE = Boolean.parseBoolean(securitySettings.getProperty("OnlyGMItemsFree", "True"));
         JAIL_IS_PVP = Boolean.parseBoolean(securitySettings.getProperty("JailIsPvp", "False"));
         JAIL_DISABLE_CHAT = Boolean.parseBoolean(securitySettings.getProperty("JailDisableChat", "True"));
      } catch (Exception var7) {
         _log.warning("Config: " + var7.getMessage());
         throw new Error("Failed to Load ./config/main/security.ini File.");
      }
   }

   private static void loadFeatureSettings(InputStream is) {
      try {
         GameSettings Feature = new GameSettings();
         InputStream var7 = new FileInputStream(new File("./config/main/feature.ini"));
         Feature.load(var7);
         CH_TELE_FEE_RATIO = Long.parseLong(Feature.getProperty("ClanHallTeleportFunctionFeeRatio", "604800000"));
         CH_TELE1_FEE = Integer.parseInt(Feature.getProperty("ClanHallTeleportFunctionFeeLvl1", "7000"));
         CH_TELE2_FEE = Integer.parseInt(Feature.getProperty("ClanHallTeleportFunctionFeeLvl2", "14000"));
         CH_SUPPORT_FEE_RATIO = Long.parseLong(Feature.getProperty("ClanHallSupportFunctionFeeRatio", "86400000"));
         CH_SUPPORT1_FEE = Integer.parseInt(Feature.getProperty("ClanHallSupportFeeLvl1", "2500"));
         CH_SUPPORT2_FEE = Integer.parseInt(Feature.getProperty("ClanHallSupportFeeLvl2", "5000"));
         CH_SUPPORT3_FEE = Integer.parseInt(Feature.getProperty("ClanHallSupportFeeLvl3", "7000"));
         CH_SUPPORT4_FEE = Integer.parseInt(Feature.getProperty("ClanHallSupportFeeLvl4", "11000"));
         CH_SUPPORT5_FEE = Integer.parseInt(Feature.getProperty("ClanHallSupportFeeLvl5", "21000"));
         CH_SUPPORT6_FEE = Integer.parseInt(Feature.getProperty("ClanHallSupportFeeLvl6", "36000"));
         CH_SUPPORT7_FEE = Integer.parseInt(Feature.getProperty("ClanHallSupportFeeLvl7", "37000"));
         CH_SUPPORT8_FEE = Integer.parseInt(Feature.getProperty("ClanHallSupportFeeLvl8", "52000"));
         CH_MPREG_FEE_RATIO = Long.parseLong(Feature.getProperty("ClanHallMpRegenerationFunctionFeeRatio", "86400000"));
         CH_MPREG1_FEE = Integer.parseInt(Feature.getProperty("ClanHallMpRegenerationFeeLvl1", "2000"));
         CH_MPREG2_FEE = Integer.parseInt(Feature.getProperty("ClanHallMpRegenerationFeeLvl2", "3750"));
         CH_MPREG3_FEE = Integer.parseInt(Feature.getProperty("ClanHallMpRegenerationFeeLvl3", "6500"));
         CH_MPREG4_FEE = Integer.parseInt(Feature.getProperty("ClanHallMpRegenerationFeeLvl4", "13750"));
         CH_MPREG5_FEE = Integer.parseInt(Feature.getProperty("ClanHallMpRegenerationFeeLvl5", "20000"));
         CH_HPREG_FEE_RATIO = Long.parseLong(Feature.getProperty("ClanHallHpRegenerationFunctionFeeRatio", "86400000"));
         CH_HPREG1_FEE = Integer.parseInt(Feature.getProperty("ClanHallHpRegenerationFeeLvl1", "700"));
         CH_HPREG2_FEE = Integer.parseInt(Feature.getProperty("ClanHallHpRegenerationFeeLvl2", "800"));
         CH_HPREG3_FEE = Integer.parseInt(Feature.getProperty("ClanHallHpRegenerationFeeLvl3", "1000"));
         CH_HPREG4_FEE = Integer.parseInt(Feature.getProperty("ClanHallHpRegenerationFeeLvl4", "1166"));
         CH_HPREG5_FEE = Integer.parseInt(Feature.getProperty("ClanHallHpRegenerationFeeLvl5", "1500"));
         CH_HPREG6_FEE = Integer.parseInt(Feature.getProperty("ClanHallHpRegenerationFeeLvl6", "1750"));
         CH_HPREG7_FEE = Integer.parseInt(Feature.getProperty("ClanHallHpRegenerationFeeLvl7", "2000"));
         CH_HPREG8_FEE = Integer.parseInt(Feature.getProperty("ClanHallHpRegenerationFeeLvl8", "2250"));
         CH_HPREG9_FEE = Integer.parseInt(Feature.getProperty("ClanHallHpRegenerationFeeLvl9", "2500"));
         CH_HPREG10_FEE = Integer.parseInt(Feature.getProperty("ClanHallHpRegenerationFeeLvl10", "3250"));
         CH_HPREG11_FEE = Integer.parseInt(Feature.getProperty("ClanHallHpRegenerationFeeLvl11", "3270"));
         CH_HPREG12_FEE = Integer.parseInt(Feature.getProperty("ClanHallHpRegenerationFeeLvl12", "4250"));
         CH_HPREG13_FEE = Integer.parseInt(Feature.getProperty("ClanHallHpRegenerationFeeLvl13", "5166"));
         CH_EXPREG_FEE_RATIO = Long.parseLong(Feature.getProperty("ClanHallExpRegenerationFunctionFeeRatio", "86400000"));
         CH_EXPREG1_FEE = Integer.parseInt(Feature.getProperty("ClanHallExpRegenerationFeeLvl1", "3000"));
         CH_EXPREG2_FEE = Integer.parseInt(Feature.getProperty("ClanHallExpRegenerationFeeLvl2", "6000"));
         CH_EXPREG3_FEE = Integer.parseInt(Feature.getProperty("ClanHallExpRegenerationFeeLvl3", "9000"));
         CH_EXPREG4_FEE = Integer.parseInt(Feature.getProperty("ClanHallExpRegenerationFeeLvl4", "15000"));
         CH_EXPREG5_FEE = Integer.parseInt(Feature.getProperty("ClanHallExpRegenerationFeeLvl5", "21000"));
         CH_EXPREG6_FEE = Integer.parseInt(Feature.getProperty("ClanHallExpRegenerationFeeLvl6", "23330"));
         CH_EXPREG7_FEE = Integer.parseInt(Feature.getProperty("ClanHallExpRegenerationFeeLvl7", "30000"));
         CH_ITEM_FEE_RATIO = Long.parseLong(Feature.getProperty("ClanHallItemCreationFunctionFeeRatio", "86400000"));
         CH_ITEM1_FEE = Integer.parseInt(Feature.getProperty("ClanHallItemCreationFunctionFeeLvl1", "30000"));
         CH_ITEM2_FEE = Integer.parseInt(Feature.getProperty("ClanHallItemCreationFunctionFeeLvl2", "70000"));
         CH_ITEM3_FEE = Integer.parseInt(Feature.getProperty("ClanHallItemCreationFunctionFeeLvl3", "140000"));
         CH_CURTAIN_FEE_RATIO = Long.parseLong(Feature.getProperty("ClanHallCurtainFunctionFeeRatio", "604800000"));
         CH_CURTAIN1_FEE = Integer.parseInt(Feature.getProperty("ClanHallCurtainFunctionFeeLvl1", "2000"));
         CH_CURTAIN2_FEE = Integer.parseInt(Feature.getProperty("ClanHallCurtainFunctionFeeLvl2", "2500"));
         CH_FRONT_FEE_RATIO = Long.parseLong(Feature.getProperty("ClanHallFrontPlatformFunctionFeeRatio", "259200000"));
         CH_FRONT1_FEE = Integer.parseInt(Feature.getProperty("ClanHallFrontPlatformFunctionFeeLvl1", "1300"));
         CH_FRONT2_FEE = Integer.parseInt(Feature.getProperty("ClanHallFrontPlatformFunctionFeeLvl2", "4000"));
         CH_BUFF_FREE = Boolean.parseBoolean(Feature.getProperty("AltClanHallMpBuffFree", "False"));
         SIEGE_HOUR_LIST = new ArrayList<>();

         for(String hour : Feature.getProperty("SiegeHourList", "").split(",")) {
            if (Util.isDigit(hour)) {
               SIEGE_HOUR_LIST.add(Integer.parseInt(hour));
            }
         }

         CS_TELE_FEE_RATIO = Long.parseLong(Feature.getProperty("CastleTeleportFunctionFeeRatio", "604800000"));
         CS_TELE1_FEE = Integer.parseInt(Feature.getProperty("CastleTeleportFunctionFeeLvl1", "7000"));
         CS_TELE2_FEE = Integer.parseInt(Feature.getProperty("CastleTeleportFunctionFeeLvl2", "14000"));
         CS_SUPPORT_FEE_RATIO = Long.parseLong(Feature.getProperty("CastleSupportFunctionFeeRatio", "86400000"));
         CS_SUPPORT1_FEE = Integer.parseInt(Feature.getProperty("CastleSupportFeeLvl1", "7000"));
         CS_SUPPORT2_FEE = Integer.parseInt(Feature.getProperty("CastleSupportFeeLvl2", "21000"));
         CS_SUPPORT3_FEE = Integer.parseInt(Feature.getProperty("CastleSupportFeeLvl3", "37000"));
         CS_SUPPORT4_FEE = Integer.parseInt(Feature.getProperty("CastleSupportFeeLvl4", "52000"));
         CS_MPREG_FEE_RATIO = Long.parseLong(Feature.getProperty("CastleMpRegenerationFunctionFeeRatio", "86400000"));
         CS_MPREG1_FEE = Integer.parseInt(Feature.getProperty("CastleMpRegenerationFeeLvl1", "2000"));
         CS_MPREG2_FEE = Integer.parseInt(Feature.getProperty("CastleMpRegenerationFeeLvl2", "6500"));
         CS_MPREG3_FEE = Integer.parseInt(Feature.getProperty("CastleMpRegenerationFeeLvl3", "13750"));
         CS_MPREG4_FEE = Integer.parseInt(Feature.getProperty("CastleMpRegenerationFeeLvl4", "20000"));
         CS_HPREG_FEE_RATIO = Long.parseLong(Feature.getProperty("CastleHpRegenerationFunctionFeeRatio", "86400000"));
         CS_HPREG1_FEE = Integer.parseInt(Feature.getProperty("CastleHpRegenerationFeeLvl1", "1000"));
         CS_HPREG2_FEE = Integer.parseInt(Feature.getProperty("CastleHpRegenerationFeeLvl2", "1500"));
         CS_HPREG3_FEE = Integer.parseInt(Feature.getProperty("CastleHpRegenerationFeeLvl3", "2250"));
         CS_HPREG4_FEE = Integer.parseInt(Feature.getProperty("CastleHpRegenerationFeeLvl4", "3270"));
         CS_HPREG5_FEE = Integer.parseInt(Feature.getProperty("CastleHpRegenerationFeeLvl5", "5166"));
         CS_EXPREG_FEE_RATIO = Long.parseLong(Feature.getProperty("CastleExpRegenerationFunctionFeeRatio", "86400000"));
         CS_EXPREG1_FEE = Integer.parseInt(Feature.getProperty("CastleExpRegenerationFeeLvl1", "9000"));
         CS_EXPREG2_FEE = Integer.parseInt(Feature.getProperty("CastleExpRegenerationFeeLvl2", "15000"));
         CS_EXPREG3_FEE = Integer.parseInt(Feature.getProperty("CastleExpRegenerationFeeLvl3", "21000"));
         CS_EXPREG4_FEE = Integer.parseInt(Feature.getProperty("CastleExpRegenerationFeeLvl4", "30000"));
         FS_TELE_FEE_RATIO = Long.parseLong(Feature.getProperty("FortressTeleportFunctionFeeRatio", "604800000"));
         FS_TELE1_FEE = Integer.parseInt(Feature.getProperty("FortressTeleportFunctionFeeLvl1", "1000"));
         FS_TELE2_FEE = Integer.parseInt(Feature.getProperty("FortressTeleportFunctionFeeLvl2", "10000"));
         FS_SUPPORT_FEE_RATIO = Long.parseLong(Feature.getProperty("FortressSupportFunctionFeeRatio", "86400000"));
         FS_SUPPORT1_FEE = Integer.parseInt(Feature.getProperty("FortressSupportFeeLvl1", "7000"));
         FS_SUPPORT2_FEE = Integer.parseInt(Feature.getProperty("FortressSupportFeeLvl2", "17000"));
         FS_MPREG_FEE_RATIO = Long.parseLong(Feature.getProperty("FortressMpRegenerationFunctionFeeRatio", "86400000"));
         FS_MPREG1_FEE = Integer.parseInt(Feature.getProperty("FortressMpRegenerationFeeLvl1", "6500"));
         FS_MPREG2_FEE = Integer.parseInt(Feature.getProperty("FortressMpRegenerationFeeLvl2", "9300"));
         FS_HPREG_FEE_RATIO = Long.parseLong(Feature.getProperty("FortressHpRegenerationFunctionFeeRatio", "86400000"));
         FS_HPREG1_FEE = Integer.parseInt(Feature.getProperty("FortressHpRegenerationFeeLvl1", "2000"));
         FS_HPREG2_FEE = Integer.parseInt(Feature.getProperty("FortressHpRegenerationFeeLvl2", "3500"));
         FS_EXPREG_FEE_RATIO = Long.parseLong(Feature.getProperty("FortressExpRegenerationFunctionFeeRatio", "86400000"));
         FS_EXPREG1_FEE = Integer.parseInt(Feature.getProperty("FortressExpRegenerationFeeLvl1", "9000"));
         FS_EXPREG2_FEE = Integer.parseInt(Feature.getProperty("FortressExpRegenerationFeeLvl2", "10000"));
         FS_UPDATE_FRQ = Integer.parseInt(Feature.getProperty("FortressPeriodicUpdateFrequency", "360"));
         FS_BLOOD_OATH_COUNT = Integer.parseInt(Feature.getProperty("FortressBloodOathCount", "1"));
         FS_MAX_SUPPLY_LEVEL = Integer.parseInt(Feature.getProperty("FortressMaxSupplyLevel", "6"));
         FS_FEE_FOR_CASTLE = Integer.parseInt(Feature.getProperty("FortressFeeForCastle", "25000"));
         FS_MAX_OWN_TIME = Integer.parseInt(Feature.getProperty("FortressMaximumOwnTime", "168"));
         ALT_GAME_CASTLE_DAWN = Boolean.parseBoolean(Feature.getProperty("AltCastleForDawn", "True"));
         ALT_GAME_CASTLE_DUSK = Boolean.parseBoolean(Feature.getProperty("AltCastleForDusk", "True"));
         ALT_GAME_REQUIRE_CLAN_CASTLE = Boolean.parseBoolean(Feature.getProperty("AltRequireClanCastle", "False"));
         ALT_FESTIVAL_MIN_PLAYER = Integer.parseInt(Feature.getProperty("AltFestivalMinPlayer", "5"));
         ALT_MAXIMUM_PLAYER_CONTRIB = Integer.parseInt(Feature.getProperty("AltMaxPlayerContrib", "1000000"));
         ALT_FESTIVAL_MANAGER_START = Long.parseLong(Feature.getProperty("AltFestivalManagerStart", "120000"));
         ALT_FESTIVAL_LENGTH = Long.parseLong(Feature.getProperty("AltFestivalLength", "1080000"));
         ALT_FESTIVAL_CYCLE_LENGTH = Long.parseLong(Feature.getProperty("AltFestivalCycleLength", "2280000"));
         ALT_FESTIVAL_FIRST_SPAWN = Long.parseLong(Feature.getProperty("AltFestivalFirstSpawn", "120000"));
         ALT_FESTIVAL_FIRST_SWARM = Long.parseLong(Feature.getProperty("AltFestivalFirstSwarm", "300000"));
         ALT_FESTIVAL_SECOND_SPAWN = Long.parseLong(Feature.getProperty("AltFestivalSecondSpawn", "540000"));
         ALT_FESTIVAL_SECOND_SWARM = Long.parseLong(Feature.getProperty("AltFestivalSecondSwarm", "720000"));
         ALT_FESTIVAL_CHEST_SPAWN = Long.parseLong(Feature.getProperty("AltFestivalChestSpawn", "900000"));
         ALT_SIEGE_DAWN_GATES_PDEF_MULT = Double.parseDouble(Feature.getProperty("AltDawnGatesPdefMult", "1.1"));
         ALT_SIEGE_DUSK_GATES_PDEF_MULT = Double.parseDouble(Feature.getProperty("AltDuskGatesPdefMult", "0.8"));
         ALT_SIEGE_DAWN_GATES_MDEF_MULT = Double.parseDouble(Feature.getProperty("AltDawnGatesMdefMult", "1.1"));
         ALT_SIEGE_DUSK_GATES_MDEF_MULT = Double.parseDouble(Feature.getProperty("AltDuskGatesMdefMult", "0.8"));
         ALT_STRICT_SEVENSIGNS = Boolean.parseBoolean(Feature.getProperty("StrictSevenSigns", "True"));
         ALT_SEVENSIGNS_LAZY_UPDATE = Boolean.parseBoolean(Feature.getProperty("AltSevenSignsLazyUpdate", "True"));
         SSQ_DAWN_TICKET_QUANTITY = Integer.parseInt(Feature.getProperty("SevenSignsDawnTicketQuantity", "300"));
         SSQ_DAWN_TICKET_PRICE = Integer.parseInt(Feature.getProperty("SevenSignsDawnTicketPrice", "1000"));
         SSQ_DAWN_TICKET_BUNDLE = Integer.parseInt(Feature.getProperty("SevenSignsDawnTicketBundle", "10"));
         SSQ_MANORS_AGREEMENT_ID = Integer.parseInt(Feature.getProperty("SevenSignsManorsAgreementId", "6388"));
         SSQ_JOIN_DAWN_ADENA_FEE = Integer.parseInt(Feature.getProperty("SevenSignsJoinDawnFee", "50000"));
         TAKE_FORT_POINTS = Integer.parseInt(Feature.getProperty("TakeFortPoints", "200"));
         LOOSE_FORT_POINTS = Integer.parseInt(Feature.getProperty("LooseFortPoints", "0"));
         TAKE_CASTLE_POINTS = Integer.parseInt(Feature.getProperty("TakeCastlePoints", "1500"));
         LOOSE_CASTLE_POINTS = Integer.parseInt(Feature.getProperty("LooseCastlePoints", "3000"));
         CASTLE_DEFENDED_POINTS = Integer.parseInt(Feature.getProperty("CastleDefendedPoints", "750"));
         FESTIVAL_WIN_POINTS = Integer.parseInt(Feature.getProperty("FestivalOfDarknessWin", "200"));
         HERO_POINTS = Integer.parseInt(Feature.getProperty("HeroPoints", "1000"));
         ROYAL_GUARD_COST = Integer.parseInt(Feature.getProperty("CreateRoyalGuardCost", "5000"));
         KNIGHT_UNIT_COST = Integer.parseInt(Feature.getProperty("CreateKnightUnitCost", "10000"));
         KNIGHT_REINFORCE_COST = Integer.parseInt(Feature.getProperty("ReinforceKnightUnitCost", "5000"));
         BALLISTA_POINTS = Integer.parseInt(Feature.getProperty("KillBallistaPoints", "30"));
         BLOODALLIANCE_POINTS = Integer.parseInt(Feature.getProperty("BloodAlliancePoints", "500"));
         BLOODOATH_POINTS = Integer.parseInt(Feature.getProperty("BloodOathPoints", "200"));
         KNIGHTSEPAULETTE_POINTS = Integer.parseInt(Feature.getProperty("KnightsEpaulettePoints", "20"));
         REPUTATION_SCORE_PER_KILL = Integer.parseInt(Feature.getProperty("ReputationScorePerKill", "1"));
         JOIN_ACADEMY_MIN_REP_SCORE = Integer.parseInt(Feature.getProperty("CompleteAcademyMinPoints", "190"));
         JOIN_ACADEMY_MAX_REP_SCORE = Integer.parseInt(Feature.getProperty("CompleteAcademyMaxPoints", "650"));
         RAID_RANKING_1ST = Integer.parseInt(Feature.getProperty("1stRaidRankingPoints", "1250"));
         RAID_RANKING_2ND = Integer.parseInt(Feature.getProperty("2ndRaidRankingPoints", "900"));
         RAID_RANKING_3RD = Integer.parseInt(Feature.getProperty("3rdRaidRankingPoints", "700"));
         RAID_RANKING_4TH = Integer.parseInt(Feature.getProperty("4thRaidRankingPoints", "600"));
         RAID_RANKING_5TH = Integer.parseInt(Feature.getProperty("5thRaidRankingPoints", "450"));
         RAID_RANKING_6TH = Integer.parseInt(Feature.getProperty("6thRaidRankingPoints", "350"));
         RAID_RANKING_7TH = Integer.parseInt(Feature.getProperty("7thRaidRankingPoints", "300"));
         RAID_RANKING_8TH = Integer.parseInt(Feature.getProperty("8thRaidRankingPoints", "200"));
         RAID_RANKING_9TH = Integer.parseInt(Feature.getProperty("9thRaidRankingPoints", "150"));
         RAID_RANKING_10TH = Integer.parseInt(Feature.getProperty("10thRaidRankingPoints", "100"));
         RAID_RANKING_UP_TO_50TH = Integer.parseInt(Feature.getProperty("UpTo50thRaidRankingPoints", "25"));
         RAID_RANKING_UP_TO_100TH = Integer.parseInt(Feature.getProperty("UpTo100thRaidRankingPoints", "12"));
         CLAN_LEVEL_6_COST = Integer.parseInt(Feature.getProperty("ClanLevel6Cost", "5000"));
         CLAN_LEVEL_7_COST = Integer.parseInt(Feature.getProperty("ClanLevel7Cost", "10000"));
         CLAN_LEVEL_8_COST = Integer.parseInt(Feature.getProperty("ClanLevel8Cost", "20000"));
         CLAN_LEVEL_9_COST = Integer.parseInt(Feature.getProperty("ClanLevel9Cost", "40000"));
         CLAN_LEVEL_10_COST = Integer.parseInt(Feature.getProperty("ClanLevel10Cost", "40000"));
         CLAN_LEVEL_11_COST = Integer.parseInt(Feature.getProperty("ClanLevel11Cost", "75000"));
         CLAN_LEVEL_6_REQUIREMENT = Integer.parseInt(Feature.getProperty("ClanLevel6Requirement", "30"));
         CLAN_LEVEL_7_REQUIREMENT = Integer.parseInt(Feature.getProperty("ClanLevel7Requirement", "50"));
         CLAN_LEVEL_8_REQUIREMENT = Integer.parseInt(Feature.getProperty("ClanLevel8Requirement", "80"));
         CLAN_LEVEL_9_REQUIREMENT = Integer.parseInt(Feature.getProperty("ClanLevel9Requirement", "120"));
         CLAN_LEVEL_10_REQUIREMENT = Integer.parseInt(Feature.getProperty("ClanLevel10Requirement", "140"));
         CLAN_LEVEL_11_REQUIREMENT = Integer.parseInt(Feature.getProperty("ClanLevel11Requirement", "170"));
         RANK_CLASS_FOR_CC = Integer.parseInt(Feature.getProperty("CommandChannelRankClass", "5"));
         ALLOW_WYVERN_ALWAYS = Boolean.parseBoolean(Feature.getProperty("AllowRideWyvernAlways", "False"));
         ALLOW_WYVERN_DURING_SIEGE = Boolean.parseBoolean(Feature.getProperty("AllowRideWyvernDuringSiege", "True"));
      } catch (Exception var6) {
         _log.warning("Config: " + var6.getMessage());
         throw new Error("Failed to Load ./config/main/feature.ini File.");
      }
   }

   private static void loadCreaureSettings(InputStream is) {
      try {
         GameSettings Character = new GameSettings();
         InputStream var15 = new FileInputStream(new File("./config/main/character.ini"));
         Character.load(var15);
         ALLOW_ZONES_LIMITS = Boolean.parseBoolean(Character.getProperty("AllowZonesLimits", "False"));
         ALT_REUSE_CORRECTION = Long.parseLong(Character.getProperty("SkillReuseCorrection", "0"));
         USER_INFO_INTERVAL = Long.parseLong(Character.getProperty("BroadcastUserInfoInterval", "100"));
         BROADCAST_CHAR_INFO_INTERVAL = Long.parseLong(Character.getProperty("BroadcastCharInfoInterval", "100"));
         BROADCAST_STATUS_UPDATE_INTERVAL = Long.parseLong(Character.getProperty("BroadcastStatusUpdateInterval", "100"));
         USER_STATS_UPDATE_INTERVAL = Long.parseLong(Character.getProperty("BroadcastStatsUpdateInterval", "100"));
         USER_ABNORMAL_EFFECTS_INTERVAL = Long.parseLong(Character.getProperty("BroadcastEffectsInterval", "100"));
         MOVE_PACKET_DELAY = Long.parseLong(Character.getProperty("MovePacketInterval", "100"));
         ATTACK_PACKET_DELAY = Long.parseLong(Character.getProperty("AttackPacketInterval", "100"));
         REQUEST_MAGIC_PACKET_DELAY = Long.parseLong(Character.getProperty("RequestMagicPacketInterval", "100"));
         ALLOW_UI_OPEN = Boolean.parseBoolean(Character.getProperty("AllowBuySellUIClose", "True"));
         BALANCER_ALLOW = Boolean.parseBoolean(Character.getProperty("BalancerAllow", "False"));
         CHECK_ATTACK_STATUS_TO_MOVE = Boolean.parseBoolean(Character.getProperty("CheckAttackToMove", "True"));
         MIN_HIT_TIME = Integer.parseInt(Character.getProperty("MinHitTime", "500"));
         ALT_GAME_DELEVEL = Boolean.parseBoolean(Character.getProperty("Delevel", "true"));
         DECREASE_SKILL_LEVEL = Boolean.parseBoolean(Character.getProperty("DecreaseSkillOnDelevel", "true"));
         DECREASE_ENCHANT_SKILLS = Boolean.parseBoolean(Character.getProperty("DecreaseEnchantSkills", "true"));
         ALT_WEIGHT_LIMIT = Double.parseDouble(Character.getProperty("AltWeightLimit", "1"));
         RUN_SPD_BOOST = Integer.parseInt(Character.getProperty("RunSpeedBoost", "0"));
         DEATH_PENALTY_CHANCE = Integer.parseInt(Character.getProperty("DeathPenaltyChance", "20"));
         RESPAWN_RESTORE_CP = Double.parseDouble(Character.getProperty("RespawnRestoreCP", "0")) / 100.0;
         RESPAWN_RESTORE_HP = Double.parseDouble(Character.getProperty("RespawnRestoreHP", "65")) / 100.0;
         RESPAWN_RESTORE_MP = Double.parseDouble(Character.getProperty("RespawnRestoreMP", "0")) / 100.0;
         HP_REGEN_MULTIPLIER = Double.parseDouble(Character.getProperty("HpRegenMultiplier", "100")) / 100.0;
         MP_REGEN_MULTIPLIER = Double.parseDouble(Character.getProperty("MpRegenMultiplier", "100")) / 100.0;
         CP_REGEN_MULTIPLIER = Double.parseDouble(Character.getProperty("CpRegenMultiplier", "100")) / 100.0;
         ALT_GAME_TIREDNESS = Boolean.parseBoolean(Character.getProperty("AltGameTiredness", "false"));
         ENABLE_MODIFY_SKILL_DURATION = Boolean.parseBoolean(Character.getProperty("EnableModifySkillDuration", "false"));
         if (ENABLE_MODIFY_SKILL_DURATION) {
            String[] propertySplit = Character.getProperty("SkillDurationList", "").split(";");
            SKILL_DURATION_LIST_SIMPLE = new HashMap<>(propertySplit.length);

            for(String skill : propertySplit) {
               String[] skillSplit = skill.split(",");
               if (skillSplit.length != 2) {
                  _log.warning("[SkillDurationList]: invalid config property -> SkillDurationList " + skill + "");
               } else {
                  try {
                     SKILL_DURATION_LIST_SIMPLE.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
                  } catch (NumberFormatException var13) {
                     if (!skill.isEmpty()) {
                        _log.warning("[SkillDurationList]: invalid config property -> SkillList " + skillSplit[0] + " " + skillSplit[1] + "");
                     }
                  }
               }
            }

            String[] propertyPremium = Character.getProperty("SkillDurationListPremium", "").split(";");
            SKILL_DURATION_LIST_PREMIUM = new HashMap<>(propertyPremium.length);

            for(String skill : propertyPremium) {
               String[] skillSplit = skill.split(",");
               if (skillSplit.length != 2) {
                  _log.warning("[SkillDurationListPremium]: invalid config property -> SkillDurationListPremium " + skill + "");
               } else {
                  try {
                     SKILL_DURATION_LIST_PREMIUM.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
                  } catch (NumberFormatException var12) {
                     if (!skill.isEmpty()) {
                        _log.warning("[SkillDurationListPremium]: invalid config property -> SkillList " + skillSplit[0] + " " + skillSplit[1] + "");
                     }
                  }
               }
            }
         }

         ENABLE_MODIFY_SKILL_REUSE = Boolean.parseBoolean(Character.getProperty("EnableModifySkillReuse", "false"));
         if (ENABLE_MODIFY_SKILL_REUSE) {
            String[] propertySplit = Character.getProperty("SkillReuseList", "").split(";");
            SKILL_REUSE_LIST = new HashMap<>(propertySplit.length);

            for(String skill : propertySplit) {
               String[] skillSplit = skill.split(",");
               if (skillSplit.length != 2) {
                  _log.warning("[SkillReuseList]: invalid config property -> SkillReuseList " + skill + "");
               } else {
                  try {
                     SKILL_REUSE_LIST.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
                  } catch (NumberFormatException var11) {
                     if (!skill.isEmpty()) {
                        _log.warning("[SkillReuseList]: invalid config property -> SkillList " + skillSplit[0] + " " + skillSplit[1] + "");
                     }
                  }
               }
            }
         }

         ALLOW_SKILL_END_CAST = Boolean.parseBoolean(Character.getProperty("AllowSkillEndCast", "False"));
         AUTO_LEARN_SKILLS = Boolean.parseBoolean(Character.getProperty("AutoLearnSkills", "False"));
         AUTO_LEARN_SKILLS_MAX_LEVEL = Integer.parseInt(Character.getProperty("AutoLearnSkillsMaxLevel", "85"));
         AUTO_LEARN_FS_SKILLS = Boolean.parseBoolean(Character.getProperty("AutoLearnForgottenScrollSkills", "False"));
         AUTO_LOOT_HERBS = Boolean.parseBoolean(Character.getProperty("AutoLootHerbs", "false"));
         BUFFS_MAX_AMOUNT = Byte.parseByte(Character.getProperty("MaxBuffAmount", "20"));
         BUFFS_MAX_AMOUNT_PREMIUM = Byte.parseByte(Character.getProperty("MaxBuffAmountForPremium", "24"));
         TRIGGERED_BUFFS_MAX_AMOUNT = Byte.parseByte(Character.getProperty("MaxTriggeredBuffAmount", "12"));
         DANCES_MAX_AMOUNT = Byte.parseByte(Character.getProperty("MaxDanceAmount", "12"));
         DANCE_CANCEL_BUFF = Boolean.parseBoolean(Character.getProperty("DanceCancelBuff", "false"));
         DANCE_CONSUME_ADDITIONAL_MP = Boolean.parseBoolean(Character.getProperty("DanceConsumeAdditionalMP", "true"));
         ALT_STORE_DANCES = Boolean.parseBoolean(Character.getProperty("AltStoreDances", "false"));
         AUTO_LEARN_DIVINE_INSPIRATION = Boolean.parseBoolean(Character.getProperty("AutoLearnDivineInspiration", "false"));
         ALT_GAME_CANCEL_CAST = Boolean.parseBoolean(Character.getProperty("AltGameCancelByHit", "true"));
         EFFECT_CANCELING = Boolean.parseBoolean(Character.getProperty("CancelLesserEffect", "True"));
         ALT_GAME_MAGICFAILURES = Boolean.parseBoolean(Character.getProperty("MagicFailures", "true"));
         PLAYER_FAKEDEATH_UP_PROTECTION = Integer.parseInt(Character.getProperty("PlayerFakeDeathUpProtection", "0"));
         STORE_SKILL_COOLTIME = Boolean.parseBoolean(Character.getProperty("StoreSkillCooltime", "true"));
         DEBUFF_REOVERLAY = Boolean.parseBoolean(Character.getProperty("DebuffReOverlay", "true"));
         ALLOW_DEBUFF_INFO = Boolean.parseBoolean(Character.getProperty("AllowDebuffInfo", "False"));
         SUBCLASS_STORE_SKILL_COOLTIME = Boolean.parseBoolean(Character.getProperty("SubclassStoreSkillCooltime", "false"));
         SUBCLASS_STORE_SKILL = Boolean.parseBoolean(Character.getProperty("SubclassSaveSkill", "False"));
         SUMMON_STORE_SKILL_COOLTIME = Boolean.parseBoolean(Character.getProperty("SummonStoreSkillCooltime", "True"));
         ALT_GAME_SHIELD_BLOCKS = Boolean.parseBoolean(Character.getProperty("AltShieldBlocks", "false"));
         ALT_PERFECT_SHLD_BLOCK = Integer.parseInt(Character.getProperty("AltPerfectShieldBlockRate", "10"));
         ALT_SHLD_BLOCK_MODIFIER = Double.parseDouble(Character.getProperty("AltShieldBlockModifier", "1.0"));
         ALLOW_ENTIRE_TREE = Boolean.parseBoolean(Character.getProperty("AllowEntireTree", "False"));
         ALTERNATE_CLASS_MASTER = Boolean.parseBoolean(Character.getProperty("AlternateClassMaster", "False"));
         LIFE_CRYSTAL_NEEDED = Boolean.parseBoolean(Character.getProperty("LifeCrystalNeeded", "true"));
         ES_SP_BOOK_NEEDED = Boolean.parseBoolean(Character.getProperty("EnchantSkillSpBookNeeded", "true"));
         DIVINE_SP_BOOK_NEEDED = Boolean.parseBoolean(Character.getProperty("DivineInspirationSpBookNeeded", "true"));
         ALT_GAME_SKILL_LEARN = Boolean.parseBoolean(Character.getProperty("AltGameSkillLearn", "false"));
         ALT_GAME_SUBCLASS_WITHOUT_QUESTS = Boolean.parseBoolean(Character.getProperty("AltSubClassWithoutQuests", "False"));
         ALT_GAME_SUBCLASS_EVERYWHERE = Boolean.parseBoolean(Character.getProperty("AltSubclassEverywhere", "False"));
         ALT_GAME_SUBCLASS_ALL_CLASSES = Boolean.parseBoolean(Character.getProperty("AltSubClassAllClasses", "False"));
         RESTORE_SERVITOR_ON_RECONNECT = Boolean.parseBoolean(Character.getProperty("RestoreServitorOnReconnect", "True"));
         RESTORE_PET_ON_RECONNECT = Boolean.parseBoolean(Character.getProperty("RestorePetOnReconnect", "True"));
         ALLOW_TRANSFORM_WITHOUT_QUEST = Boolean.parseBoolean(Character.getProperty("AltTransformationWithoutQuest", "False"));
         FEE_DELETE_TRANSFER_SKILLS = Integer.parseInt(Character.getProperty("FeeDeleteTransferSkills", "10000000"));
         FEE_DELETE_SUBCLASS_SKILLS = Integer.parseInt(Character.getProperty("FeeDeleteSubClassSkills", "10000000"));
         ENABLE_VITALITY = Boolean.parseBoolean(Character.getProperty("EnableVitality", "True"));
         RECOVER_VITALITY_ON_RECONNECT = Boolean.parseBoolean(Character.getProperty("RecoverVitalityOnReconnect", "True"));
         STARTING_VITALITY_POINTS = Integer.parseInt(Character.getProperty("StartingVitalityPoints", "20000"));
         VITALITY_RAID_BONUS = Double.parseDouble(Character.getProperty("VitalityRaidBonus", "2000"));
         VITALITY_NEVIT_UP_POINT = Double.parseDouble(Character.getProperty("VitalityNevitUpPoint", "10"));
         VITALITY_NEVIT_POINT = Double.parseDouble(Character.getProperty("VitalityNevitPoint", "10"));
         MAX_BONUS_EXP = Double.parseDouble(Character.getProperty("MaxExpBonus", "3.5"));
         MAX_BONUS_SP = Double.parseDouble(Character.getProperty("MaxSpBonus", "3.5"));
         MAX_RUN_SPEED = Integer.parseInt(Character.getProperty("MaxRunSpeed", "300"));
         MAX_PCRIT_RATE = Integer.parseInt(Character.getProperty("MaxPCritRate", "500"));
         MAX_MCRIT_RATE = Integer.parseInt(Character.getProperty("MaxMCritRate", "200"));
         MAX_PATK_SPEED = Integer.parseInt(Character.getProperty("MaxPAtkSpeed", "1500"));
         MAX_MATK_SPEED = Integer.parseInt(Character.getProperty("MaxMAtkSpeed", "1999"));
         MAX_EVASION = Integer.parseInt(Character.getProperty("MaxEvasion", "250"));
         MIN_ABNORMAL_STATE_SUCCESS_RATE = Integer.parseInt(Character.getProperty("MinAbnormalStateSuccessRate", "10"));
         MAX_ABNORMAL_STATE_SUCCESS_RATE = Integer.parseInt(Character.getProperty("MaxAbnormalStateSuccessRate", "90"));
         SKILLS_CHANCE_MOD = Double.parseDouble(Character.getProperty("SkillsChanceMod", "11."));
         SKILLS_CHANCE_POW = Double.parseDouble(Character.getProperty("SkillsChancePow", "0.5"));
         STUN_CHANCE_MOD = Double.parseDouble(Character.getProperty("StunChanceMod", "10.0"));
         STUN_CHANCE_CRIT_MOD = Double.parseDouble(Character.getProperty("StunChanceCritMod", "75.0"));
         SKILL_BREAK_MOD = Double.parseDouble(Character.getProperty("SkillBreakChanceMod", "10.0"));
         SKILL_BREAK_CRIT_MOD = Double.parseDouble(Character.getProperty("SkillBreakChanceCritMod", "75.0"));
         MAX_SUBCLASS = Byte.parseByte(Character.getProperty("MaxSubclass", "3"));
         BASE_SUBCLASS_LEVEL = Byte.parseByte(Character.getProperty("BaseSubclassLevel", "40"));
         MAX_SUBCLASS_LEVEL = Byte.parseByte(Character.getProperty("MaxSubclassLevel", "80"));
         MAX_PVTSTORESELL_SLOTS_DWARF = Integer.parseInt(Character.getProperty("SellStoreSlotsDwarf", "4"));
         MAX_PVTSTORESELL_SLOTS_OTHER = Integer.parseInt(Character.getProperty("SellStoreSlotsOther", "3"));
         MAX_PVTSTOREBUY_SLOTS_DWARF = Integer.parseInt(Character.getProperty("BuyStoreSlotsDwarf", "5"));
         MAX_PVTSTOREBUY_SLOTS_OTHER = Integer.parseInt(Character.getProperty("BuyStoreSlotsOther", "4"));
         INVENTORY_MAXIMUM_NO_DWARF = Integer.parseInt(Character.getProperty("InventorySlotsForNoDwarf", "80"));
         INVENTORY_MAXIMUM_DWARF = Integer.parseInt(Character.getProperty("InventorySlotsForDwarf", "100"));
         INVENTORY_MAXIMUM_GM = Integer.parseInt(Character.getProperty("InventorySlotsForGM", "250"));
         INVENTORY_MAXIMUM_QUEST_ITEMS = Integer.parseInt(Character.getProperty("InventorySlotsForQuestItems", "100"));
         MAX_ITEM_IN_PACKET = Math.max(INVENTORY_MAXIMUM_NO_DWARF, Math.max(INVENTORY_MAXIMUM_DWARF, INVENTORY_MAXIMUM_GM));
         WAREHOUSE_SLOTS_DWARF = Integer.parseInt(Character.getProperty("WarehouseSlotsForDwarf", "120"));
         WAREHOUSE_SLOTS_NO_DWARF = Integer.parseInt(Character.getProperty("WarehouseSlotsForNoDwarf", "100"));
         WAREHOUSE_SLOTS_CLAN = Integer.parseInt(Character.getProperty("WarehouseSlotsForClan", "150"));
         ALT_FREIGHT_SLOTS = Integer.parseInt(Character.getProperty("FreightSlots", "200"));
         MAX_AMOUNT_BY_MULTISELL = Integer.parseInt(Character.getProperty("MaximumItemsPerMultisell", "5000"));
         ALT_FREIGHT_PRICE = Integer.parseInt(Character.getProperty("FreightPrice", "1000"));
         EXPAND_INVENTORY_LIMIT = Integer.parseInt(Character.getProperty("ExpandInventoryLimit", "300"));
         EXPAND_WAREHOUSE_LIMIT = Integer.parseInt(Character.getProperty("ExpandWareHouseLimit", "200"));
         EXPAND_SELLSTORE_LIMIT = Integer.parseInt(Character.getProperty("ExpandSellStoreLimit", "10"));
         EXPAND_BUYSTORE_LIMIT = Integer.parseInt(Character.getProperty("ExpandBuyStoreLimit", "10"));
         EXPAND_DWARFRECIPE_LIMIT = Integer.parseInt(Character.getProperty("ExpandDwarfRecipeLimit", "100"));
         EXPAND_COMMONRECIPE_LIMIT = Integer.parseInt(Character.getProperty("ExpandCommonRecipeLimit", "100"));
         AUGMENTATION_NG_SKILL_CHANCE = Integer.parseInt(Character.getProperty("AugmentationNGSkillChance", "15"));
         AUGMENTATION_NG_GLOW_CHANCE = Integer.parseInt(Character.getProperty("AugmentationNGGlowChance", "0"));
         AUGMENTATION_MID_SKILL_CHANCE = Integer.parseInt(Character.getProperty("AugmentationMidSkillChance", "30"));
         AUGMENTATION_MID_GLOW_CHANCE = Integer.parseInt(Character.getProperty("AugmentationMidGlowChance", "40"));
         AUGMENTATION_HIGH_SKILL_CHANCE = Integer.parseInt(Character.getProperty("AugmentationHighSkillChance", "45"));
         AUGMENTATION_HIGH_GLOW_CHANCE = Integer.parseInt(Character.getProperty("AugmentationHighGlowChance", "70"));
         AUGMENTATION_TOP_SKILL_CHANCE = Integer.parseInt(Character.getProperty("AugmentationTopSkillChance", "60"));
         AUGMENTATION_TOP_GLOW_CHANCE = Integer.parseInt(Character.getProperty("AugmentationTopGlowChance", "100"));
         AUGMENTATION_BASESTAT_CHANCE = Integer.parseInt(Character.getProperty("AugmentationBaseStatChance", "1"));
         AUGMENTATION_ACC_SKILL_CHANCE = Integer.parseInt(Character.getProperty("AugmentationAccSkillChance", "0"));
         RETAIL_LIKE_AUGMENTATION = Boolean.parseBoolean(Character.getProperty("RetailLikeAugmentation", "True"));
         String[] array = Character.getProperty("RetailLikeAugmentationNoGradeChance", "55,35,7,3").split(",");
         RETAIL_LIKE_AUGMENTATION_NG_CHANCE = new int[array.length];

         for(int i = 0; i < 4; ++i) {
            RETAIL_LIKE_AUGMENTATION_NG_CHANCE[i] = Integer.parseInt(array[i]);
         }

         array = Character.getProperty("RetailLikeAugmentationMidGradeChance", "55,35,7,3").split(",");
         RETAIL_LIKE_AUGMENTATION_MID_CHANCE = new int[array.length];

         for(int i = 0; i < 4; ++i) {
            RETAIL_LIKE_AUGMENTATION_MID_CHANCE[i] = Integer.parseInt(array[i]);
         }

         array = Character.getProperty("RetailLikeAugmentationHighGradeChance", "55,35,7,3").split(",");
         RETAIL_LIKE_AUGMENTATION_HIGH_CHANCE = new int[array.length];

         for(int i = 0; i < 4; ++i) {
            RETAIL_LIKE_AUGMENTATION_HIGH_CHANCE[i] = Integer.parseInt(array[i]);
         }

         array = Character.getProperty("RetailLikeAugmentationTopGradeChance", "55,35,7,3").split(",");
         RETAIL_LIKE_AUGMENTATION_TOP_CHANCE = new int[array.length];

         for(int i = 0; i < 4; ++i) {
            RETAIL_LIKE_AUGMENTATION_TOP_CHANCE[i] = Integer.parseInt(array[i]);
         }

         RETAIL_LIKE_AUGMENTATION_ACCESSORY = Boolean.parseBoolean(Character.getProperty("RetailLikeAugmentationAccessory", "True"));
         array = Character.getProperty(
               "AugmentationBlackList",
               "6656,6657,6658,6659,6660,6661,6662,8191,10170,10314,13740,13741,13742,13743,13744,13745,13746,13747,13748,14592,14593,14594,14595,14596,14597,14598,14599,14600,14664,14665,14666,14667,14668,14669,14670,14671,14672,14801,14802,14803,14804,14805,14806,14807,14808,14809,15282,15283,15284,15285,15286,15287,15288,15289,15290,15291,15292,15293,15294,15295,15296,15297,15298,15299,16025,16026,21712,22173,22174,22175"
            )
            .split(",");
         AUGMENTATION_BLACKLIST = new int[array.length];

         for(int i = 0; i < array.length; ++i) {
            AUGMENTATION_BLACKLIST[i] = Integer.parseInt(array[i]);
         }

         Arrays.sort(AUGMENTATION_BLACKLIST);
         ALT_ALLOW_AUGMENT_PVP_ITEMS = Boolean.parseBoolean(Character.getProperty("AltAllowAugmentPvPItems", "false"));
         ALLOW_DROP_AUGMENT_ITEMS = Boolean.parseBoolean(Character.getProperty("AllowDropAugmentItems", "false"));
         ALLOW_TRADE_AUGMENT_ITEMS = Boolean.parseBoolean(Character.getProperty("AllowTradeAugmentItems", "false"));
         ALLOW_SELL_AUGMENT_ITEMS = Boolean.parseBoolean(Character.getProperty("AllowSellAugmentItems", "false"));
         ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE = Boolean.parseBoolean(Character.getProperty("AltKarmaPlayerCanBeKilledInPeaceZone", "false"));
         ALT_GAME_KARMA_PLAYER_CAN_SHOP = Boolean.parseBoolean(Character.getProperty("AltKarmaPlayerCanShop", "true"));
         ALT_GAME_KARMA_PLAYER_CAN_TELEPORT = Boolean.parseBoolean(Character.getProperty("AltKarmaPlayerCanTeleport", "true"));
         ALT_GAME_KARMA_PLAYER_CAN_USE_GK = Boolean.parseBoolean(Character.getProperty("AltKarmaPlayerCanUseGK", "false"));
         ALT_GAME_KARMA_PLAYER_CAN_TRADE = Boolean.parseBoolean(Character.getProperty("AltKarmaPlayerCanTrade", "true"));
         ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE = Boolean.parseBoolean(Character.getProperty("AltKarmaPlayerCanUseWareHouse", "true"));
         MAX_PERSONAL_FAME_POINTS = Integer.parseInt(Character.getProperty("MaxPersonalFamePoints", "50000"));
         FORTRESS_ZONE_FAME_TASK_FREQUENCY = Integer.parseInt(Character.getProperty("FortressZoneFameTaskFrequency", "300"));
         FORTRESS_ZONE_FAME_AQUIRE_POINTS = Integer.parseInt(Character.getProperty("FortressZoneFameAquirePoints", "31"));
         CASTLE_ZONE_FAME_TASK_FREQUENCY = Integer.parseInt(Character.getProperty("CastleZoneFameTaskFrequency", "300"));
         CASTLE_ZONE_FAME_AQUIRE_POINTS = Integer.parseInt(Character.getProperty("CastleZoneFameAquirePoints", "125"));
         FAME_FOR_DEAD_PLAYERS = Boolean.parseBoolean(Character.getProperty("FameForDeadPlayers", "true"));
         IS_CRAFTING_ENABLED = Boolean.parseBoolean(Character.getProperty("CraftingEnabled", "true"));
         CRAFT_MASTERWORK = Boolean.parseBoolean(Character.getProperty("CraftMasterwork", "True"));
         CRAFT_DOUBLECRAFT_CHANCE = Double.parseDouble(Character.getProperty("CraftDoubleCraftChance", "3."));
         DWARF_RECIPE_SLOTS = Integer.parseInt(Character.getProperty("DwarfRecipeSlots", "50"));
         COMMON_RECIPE_SLOTS = Integer.parseInt(Character.getProperty("CommonRecipeSlots", "50"));
         ALT_GAME_CREATION = Boolean.parseBoolean(Character.getProperty("AltGameCreation", "false"));
         ALT_GAME_CREATION_SPEED = Double.parseDouble(Character.getProperty("AltGameCreationSpeed", "1"));
         ALT_GAME_CREATION_XP_RATE = Double.parseDouble(Character.getProperty("AltGameCreationXpRate", "1"));
         ALT_GAME_CREATION_SP_RATE = Double.parseDouble(Character.getProperty("AltGameCreationSpRate", "1"));
         ALT_GAME_CREATION_RARE_XPSP_RATE = Double.parseDouble(Character.getProperty("AltGameCreationRareXpSpRate", "2"));
         ALT_BLACKSMITH_USE_RECIPES = Boolean.parseBoolean(Character.getProperty("AltBlacksmithUseRecipes", "true"));
         ALT_CLAN_LEADER_DATE_CHANGE = Integer.parseInt(Character.getProperty("AltClanLeaderDateChange", "3"));
         if (ALT_CLAN_LEADER_DATE_CHANGE < 1 || ALT_CLAN_LEADER_DATE_CHANGE > 7) {
            _log.log(Level.WARNING, "Wrong value specified for AltClanLeaderDateChange: " + ALT_CLAN_LEADER_DATE_CHANGE);
            ALT_CLAN_LEADER_DATE_CHANGE = 3;
         }

         ALT_CLAN_DEFAULT_LEVEL = Integer.parseInt(Character.getProperty("ClanDefaultLevel", "1"));
         ALT_CLAN_LEADER_HOUR_CHANGE = Character.getProperty("AltClanLeaderHourChange", "00:00:00");
         ALT_CLAN_LEADER_INSTANT_ACTIVATION = Boolean.parseBoolean(Character.getProperty("AltClanLeaderInstantActivation", "false"));
         ALT_CLAN_JOIN_DAYS = Integer.parseInt(Character.getProperty("DaysBeforeJoinAClan", "24"));
         ALT_CLAN_CREATE_DAYS = Integer.parseInt(Character.getProperty("DaysBeforeCreateAClan", "240"));
         ALT_CLAN_DISSOLVE_DAYS = Integer.parseInt(Character.getProperty("DaysToPassToDissolveAClan", "168"));
         ALT_ALLY_JOIN_DAYS_WHEN_LEAVED = Integer.parseInt(Character.getProperty("DaysBeforeJoinAllyWhenLeaved", "24"));
         ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED = Integer.parseInt(Character.getProperty("DaysBeforeJoinAllyWhenDismissed", "24"));
         ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED = Integer.parseInt(Character.getProperty("DaysBeforeAcceptNewClanWhenDismissed", "24"));
         ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED = Integer.parseInt(Character.getProperty("DaysBeforeCreateNewAllyWhenDissolved", "24"));
         ALT_MAX_NUM_OF_CLANS_IN_ALLY = Integer.parseInt(Character.getProperty("AltMaxNumOfClansInAlly", "3"));
         ALT_CLAN_MEMBERS_FOR_WAR = Integer.parseInt(Character.getProperty("AltClanMembersForWar", "15"));
         ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH = Boolean.parseBoolean(Character.getProperty("AltMembersCanWithdrawFromClanWH", "false"));
         REMOVE_CASTLE_CIRCLETS = Boolean.parseBoolean(Character.getProperty("RemoveCastleCirclets", "true"));
         ALT_PARTY_RANGE = Integer.parseInt(Character.getProperty("AltPartyRange", "1600"));
         ALT_PARTY_RANGE2 = Integer.parseInt(Character.getProperty("AltPartyRange2", "1400"));
         ALT_LEAVE_PARTY_LEADER = Boolean.parseBoolean(Character.getProperty("AltLeavePartyLeader", "False"));
         STARTING_ADENA = Long.parseLong(Character.getProperty("StartingAdena", "0"));
         STARTING_LEVEL = Byte.parseByte(Character.getProperty("StartingLevel", "1"));
         STARTING_SP = Integer.parseInt(Character.getProperty("StartingSP", "0"));
         MAX_ADENA = Long.parseLong(Character.getProperty("MaxAdena", "99900000000"));
         if (MAX_ADENA < 0L) {
            MAX_ADENA = Long.MAX_VALUE;
         }

         AUTO_LOOT = Boolean.parseBoolean(Character.getProperty("AutoLoot", "false"));
         AUTO_LOOT_RAIDS = Boolean.parseBoolean(Character.getProperty("AutoLootRaids", "false"));
         LOOT_RAIDS_PRIVILEGE_INTERVAL = Integer.parseInt(Character.getProperty("RaidLootRightsInterval", "900")) * 1000;
         LOOT_RAIDS_PRIVILEGE_CC_SIZE = Integer.parseInt(Character.getProperty("RaidLootRightsCCSize", "45"));
         UNSTUCK_INTERVAL = Integer.parseInt(Character.getProperty("UnstuckInterval", "300"));
         TELEPORT_WATCHDOG_TIMEOUT = Integer.parseInt(Character.getProperty("TeleportWatchdogTimeout", "0"));
         PLAYER_SPAWN_PROTECTION = Integer.parseInt(Character.getProperty("PlayerSpawnProtection", "0"));
         String[] items = Character.getProperty("PlayerSpawnProtectionAllowedItems", "0").split(",");
         SPAWN_PROTECTION_ALLOWED_ITEMS = new ArrayList<>(items.length);

         for(String item : items) {
            Integer itm = 0;

            try {
               itm = Integer.parseInt(item);
            } catch (NumberFormatException var10) {
               _log.warning("Player Spawn Protection: Wrong ItemId passed: " + item);
               _log.warning(var10.getMessage());
            }

            if (itm != 0) {
               SPAWN_PROTECTION_ALLOWED_ITEMS.add(itm);
            }
         }

         PLAYER_TELEPORT_PROTECTION = Integer.parseInt(Character.getProperty("PlayerTeleportProtection", "0"));
         RANDOM_RESPAWN_IN_TOWN_ENABLED = Boolean.parseBoolean(Character.getProperty("RandomRespawnInTownEnabled", "True"));
         OFFSET_ON_TELEPORT_ENABLED = Boolean.parseBoolean(Character.getProperty("OffsetOnTeleportEnabled", "True"));
         MAX_OFFSET_ON_TELEPORT = Integer.parseInt(Character.getProperty("MaxOffsetOnTeleport", "50"));
         RESTORE_PLAYER_INSTANCE = Boolean.parseBoolean(Character.getProperty("RestorePlayerInstance", "False"));
         ALLOW_SUMMON_TO_INSTANCE = Boolean.parseBoolean(Character.getProperty("AllowSummonToInstance", "True"));
         EJECT_DEAD_PLAYER_TIME = 1000 * Integer.parseInt(Character.getProperty("EjectDeadPlayerTime", "60"));
         PETITIONING_ALLOWED = Boolean.parseBoolean(Character.getProperty("PetitioningAllowed", "True"));
         NEW_PETITIONING_SYSTEM = Boolean.parseBoolean(Character.getProperty("NewPetitionSystem", "False"));
         MAX_PETITIONS_PER_PLAYER = Integer.parseInt(Character.getProperty("MaxPetitionsPerPlayer", "5"));
         MAX_PETITIONS_PENDING = Integer.parseInt(Character.getProperty("MaxPetitionsPending", "25"));
         ALT_GAME_FREE_TELEPORT = Boolean.parseBoolean(Character.getProperty("AltFreeTeleporting", "False"));
         DELETE_DAYS = Integer.parseInt(Character.getProperty("DeleteCharAfterDays", "7"));
         ALT_GAME_EXPONENT_XP = Float.parseFloat(Character.getProperty("AltGameExponentXp", "0."));
         ALT_GAME_EXPONENT_SP = Float.parseFloat(Character.getProperty("AltGameExponentSp", "0."));
         PARTY_XP_CUTOFF_METHOD = Character.getProperty("PartyXpCutoffMethod", "highfive");
         PARTY_XP_CUTOFF_PERCENT = Double.parseDouble(Character.getProperty("PartyXpCutoffPercent", "3."));
         PARTY_XP_CUTOFF_LEVEL = Integer.parseInt(Character.getProperty("PartyXpCutoffLevel", "20"));
         String[] gaps = Character.getProperty("PartyXpCutoffGaps", "0,9;10,14;15,99").split(";");
         PARTY_XP_CUTOFF_GAPS = new int[gaps.length][2];

         for(int i = 0; i < gaps.length; ++i) {
            PARTY_XP_CUTOFF_GAPS[i] = new int[]{Integer.parseInt(gaps[i].split(",")[0]), Integer.parseInt(gaps[i].split(",")[1])};
         }

         String[] percents = Character.getProperty("PartyXpCutoffGapPercent", "100;30;0").split(";");
         PARTY_XP_CUTOFF_GAP_PERCENTS = new int[percents.length];

         for(int i = 0; i < percents.length; ++i) {
            PARTY_XP_CUTOFF_GAP_PERCENTS[i] = Integer.parseInt(percents[i]);
         }

         DISABLE_TUTORIAL = Boolean.parseBoolean(Character.getProperty("DisableTutorial", "False"));
         EXPERTISE_PENALTY = Boolean.parseBoolean(Character.getProperty("ExpertisePenalty", "True"));
         STORE_RECIPE_SHOPLIST = Boolean.parseBoolean(Character.getProperty("StoreRecipeShopList", "False"));
         STORE_UI_SETTINGS = Boolean.parseBoolean(Character.getProperty("StoreCharUiSettings", "False"));
         FORBIDDEN_NAMES = Character.getProperty("ForbiddenNames", "").split(",");
         SILENCE_MODE_EXCLUDE = Boolean.parseBoolean(Character.getProperty("SilenceModeExclude", "False"));
         ALT_VALIDATE_TRIGGER_SKILLS = Boolean.parseBoolean(Character.getProperty("AltValidateTriggerSkills", "False"));
         SKILL_CHANCE_SHOW = Boolean.parseBoolean(Character.getProperty("SkillChanceShow", "false"));
         ALT_DAGGER_DMG_VS_HEAVY = Float.parseFloat(Character.getProperty("DaggerVSHeavy", "2.50"));
         ALT_DAGGER_DMG_VS_ROBE = Float.parseFloat(Character.getProperty("DaggerVSRobe", "1.80"));
         ALT_DAGGER_DMG_VS_LIGHT = Float.parseFloat(Character.getProperty("DaggerVSLight", "2.00"));
         ALT_BOW_DMG_VS_HEAVY = Float.parseFloat(Character.getProperty("ArcherVSHeavy", "1.00"));
         ALT_BOW_DMG_VS_ROBE = Float.parseFloat(Character.getProperty("ArcherVSRobe", "1.00"));
         ALT_BOW_DMG_VS_LIGHT = Float.parseFloat(Character.getProperty("ArcherVSLight", "1.00"));
         ALT_MAGES_PHYSICAL_DAMAGE_MULTI = Float.parseFloat(Character.getProperty("AltPDamageMages", "1.00"));
         ALT_MAGES_MAGICAL_DAMAGE_MULTI = Float.parseFloat(Character.getProperty("AltMDamageMages", "1.00"));
         ALT_FIGHTERS_PHYSICAL_DAMAGE_MULTI = Float.parseFloat(Character.getProperty("AltPDamageFighters", "1.00"));
         ALT_FIGHTERS_MAGICAL_DAMAGE_MULTI = Float.parseFloat(Character.getProperty("AltMDamageFighters", "1.00"));
         ALT_PETS_PHYSICAL_DAMAGE_MULTI = Float.parseFloat(Character.getProperty("AltPDamagePets", "1.00"));
         ALT_PETS_MAGICAL_DAMAGE_MULTI = Float.parseFloat(Character.getProperty("AltMDamagePets", "1.00"));
         ALT_NPC_PHYSICAL_DAMAGE_MULTI = Float.parseFloat(Character.getProperty("AltPDamageNpc", "1.00"));
         ALT_NPC_MAGICAL_DAMAGE_MULTI = Float.parseFloat(Character.getProperty("AltMDamageNpc", "1.00"));
         PATK_SPEED_MULTI = Float.parseFloat(Character.getProperty("AltAttackSpeed", "1.00"));
         MATK_SPEED_MULTI = Float.parseFloat(Character.getProperty("AltCastingSpeed", "1.00"));
         RESTORE_DISPEL_SKILLS = Boolean.parseBoolean(Character.getProperty("RestoreDispelSkills", "False"));
         RESTORE_DISPEL_SKILLS_TIME = Integer.parseInt(Character.getProperty("RestoreDispelSkillsTime", "10"));
         ALT_GAME_VIEWPLAYER = Boolean.parseBoolean(Character.getProperty("AltGameViewPlayer", "False"));
         AUTO_LOOT_BY_ID_SYSTEM = Boolean.parseBoolean(Character.getProperty("AutoLootByIdSystem", "False"));
         array = Character.getProperty("AutoLootById", "0").split(",");
         AUTO_LOOT_BY_ID = new int[array.length];

         for(int i = 0; i < array.length; ++i) {
            AUTO_LOOT_BY_ID[i] = Integer.parseInt(array[i]);
         }

         Arrays.sort(AUTO_LOOT_BY_ID);
         DISPLAY_MESSAGE = Boolean.parseBoolean(Character.getProperty("DisplayEffectMessageThroughDeath", "True"));
         BASE_STR_LIMIT = Integer.parseInt(Character.getProperty("BaseStrLimit", "100"));
         BASE_INT_LIMIT = Integer.parseInt(Character.getProperty("BaseIntLimit", "100"));
         BASE_DEX_LIMIT = Integer.parseInt(Character.getProperty("BaseDexLimit", "100"));
         BASE_WIT_LIMIT = Integer.parseInt(Character.getProperty("BaseWitLimit", "100"));
         BASE_CON_LIMIT = Integer.parseInt(Character.getProperty("BaseConLimit", "100"));
         BASE_MEN_LIMIT = Integer.parseInt(Character.getProperty("BaseMenLimit", "100"));
         BASE_RESET_STR = Integer.parseInt(Character.getProperty("BaseResetStr", "1"));
         BASE_RESET_INT = Integer.parseInt(Character.getProperty("BaseResetInt", "1"));
         BASE_RESET_DEX = Integer.parseInt(Character.getProperty("BaseResetDex", "1"));
         BASE_RESET_WIT = Integer.parseInt(Character.getProperty("BaseResetWin", "1"));
         BASE_RESET_CON = Integer.parseInt(Character.getProperty("BaseResetCon", "1"));
         BASE_RESET_MEN = Integer.parseInt(Character.getProperty("BaseResetMen", "1"));
         TRADE_ONLY_IN_PEACE_ZONE = Boolean.parseBoolean(Character.getProperty("TradeOnlyInPeaceZones", "False"));
         ATTACK_STANCE_MAGIC = Boolean.parseBoolean(Character.getProperty("DisableAttackStanceFromMagic", "False"));
         ALLOW_NEVIT_SYSTEM = Boolean.parseBoolean(Character.getProperty("AllowNevitSystem", "True"));
         NEVIT_ADVENT_TIME = Integer.parseInt(Character.getProperty("NevitAdventTime", "240"));
         NEVIT_MAX_POINTS = Integer.parseInt(Character.getProperty("NevitMaxPoints", "7200"));
         NEVIT_BONUS_EFFECT_TIME = Integer.parseInt(Character.getProperty("NevitBonusEffectTime", "180"));
         ALLOW_RECO_BONUS_SYSTEM = Boolean.parseBoolean(Character.getProperty("AllowRecBonusSystem", "True"));
      } catch (Exception var14) {
         _log.warning("Config: " + var14.getMessage());
         throw new Error("Failed to Load ./config/main/character.ini file.");
      }
   }

   private static void loadMmoSettings(InputStream is) {
      try {
         GameSettings mmoSettings = new GameSettings();
         InputStream var3 = new FileInputStream(new File("./config/main/mmo.ini"));
         mmoSettings.load(var3);
         SELECTOR_CONFIG.SLEEP_TIME = Long.parseLong(mmoSettings.getProperty("SelectorSleepTime", "10"));
         SELECTOR_CONFIG.INTEREST_DELAY = Long.parseLong(mmoSettings.getProperty("InterestDelay", "30"));
         SELECTOR_CONFIG.MAX_SEND_PER_PASS = Integer.parseInt(mmoSettings.getProperty("MaxSendPerPass", "32"));
         SELECTOR_CONFIG.READ_BUFFER_SIZE = Integer.parseInt(mmoSettings.getProperty("ReadBufferSize", "65536"));
         SELECTOR_CONFIG.WRITE_BUFFER_SIZE = Integer.parseInt(mmoSettings.getProperty("WriteBufferSize", "131072"));
         SELECTOR_CONFIG.HELPER_BUFFER_COUNT = Integer.parseInt(mmoSettings.getProperty("BufferPoolSize", "64"));
      } catch (Exception var2) {
         _log.warning("Config: " + var2.getMessage());
         throw new Error("Failed to Load ./config/main/mmo.ini File.");
      }
   }

   private static void loadIdFactorySettings(InputStream is) {
      try {
         GameSettings idSettings = new GameSettings();
         InputStream var3 = new FileInputStream(new File("./config/main/idfactory.ini"));
         idSettings.load(var3);
         IDFACTORY_TYPE = Config.IdFactoryType.valueOf(idSettings.getProperty("IDFactory", "Compaction"));
         BAD_ID_CHECKING = Boolean.parseBoolean(idSettings.getProperty("BadIdChecking", "True"));
      } catch (Exception var2) {
         _log.warning("Config: " + var2.getMessage());
         throw new Error("Failed to Load ./config/main/idfactory.ini file.");
      }
   }

   private static void loadGeneralSettings(InputStream is) {
      try {
         GameSettings General = new GameSettings();
         InputStream var9 = new FileInputStream(new File("./config/main/general.ini"));
         General.load(var9);
         EVERYBODY_HAS_ADMIN_RIGHTS = Boolean.parseBoolean(General.getProperty("EverybodyHasAdminRights", "false"));
         DISPLAY_SERVER_VERSION = Boolean.parseBoolean(General.getProperty("DisplayServerRevision", "True"));
         GM_HERO_AURA = Boolean.parseBoolean(General.getProperty("GMHeroAura", "False"));
         GM_STARTUP_INVULNERABLE = Boolean.parseBoolean(General.getProperty("GMStartupInvulnerable", "False"));
         GM_STARTUP_INVISIBLE = Boolean.parseBoolean(General.getProperty("GMStartupInvisible", "False"));
         GM_STARTUP_SILENCE = Boolean.parseBoolean(General.getProperty("GMStartupSilence", "False"));
         GM_STARTUP_AUTO_LIST = Boolean.parseBoolean(General.getProperty("GMStartupAutoList", "False"));
         GM_STARTUP_DIET_MODE = Boolean.parseBoolean(General.getProperty("GMStartupDietMode", "False"));
         GM_ITEM_RESTRICTION = Boolean.parseBoolean(General.getProperty("GMItemRestriction", "True"));
         GM_SKILL_RESTRICTION = Boolean.parseBoolean(General.getProperty("GMSkillRestriction", "True"));
         GM_TRADE_RESTRICTED_ITEMS = Boolean.parseBoolean(General.getProperty("GMTradeRestrictedItems", "False"));
         GM_RESTART_FIGHTING = Boolean.parseBoolean(General.getProperty("GMRestartFighting", "True"));
         GM_ANNOUNCER_NAME = Boolean.parseBoolean(General.getProperty("GMShowAnnouncerName", "False"));
         GM_CRITANNOUNCER_NAME = Boolean.parseBoolean(General.getProperty("GMShowCritAnnouncerName", "False"));
         GM_GIVE_SPECIAL_SKILLS = Boolean.parseBoolean(General.getProperty("GMGiveSpecialSkills", "False"));
         GM_GIVE_SPECIAL_AURA_SKILLS = Boolean.parseBoolean(General.getProperty("GMGiveSpecialAuraSkills", "False"));
         GAMEGUARD_ENFORCE = Boolean.parseBoolean(General.getProperty("GameGuardEnforce", "False"));
         LOG_CHAT = Boolean.parseBoolean(General.getProperty("LogChat", "false"));
         LOG_AUTO_ANNOUNCEMENTS = Boolean.parseBoolean(General.getProperty("LogAutoAnnouncements", "False"));
         SERVICE_LOGS = Boolean.parseBoolean(General.getProperty("LogServices", "false"));
         LOG_ITEMS = Boolean.parseBoolean(General.getProperty("LogItems", "false"));
         LOG_ITEMS_SMALL_LOG = Boolean.parseBoolean(General.getProperty("LogItemsSmallLog", "false"));
         LOG_ITEM_ENCHANTS = Boolean.parseBoolean(General.getProperty("LogItemEnchants", "false"));
         LOG_SKILL_ENCHANTS = Boolean.parseBoolean(General.getProperty("LogSkillEnchants", "false"));
         GMAUDIT = Boolean.parseBoolean(General.getProperty("GMAudit", "False"));
         LOG_GAME_DAMAGE = Boolean.parseBoolean(General.getProperty("LogGameDamage", "False"));
         LOG_GAME_DAMAGE_THRESHOLD = Integer.parseInt(General.getProperty("LogGameDamageThreshold", "5000"));
         SKILL_CHECK_ENABLE = Boolean.parseBoolean(General.getProperty("SkillCheckEnable", "False"));
         SKILL_CHECK_REMOVE = Boolean.parseBoolean(General.getProperty("SkillCheckRemove", "False"));
         SKILL_CHECK_GM = Boolean.parseBoolean(General.getProperty("SkillCheckGM", "True"));
         DEBUG = Boolean.parseBoolean(General.getProperty("Debug", "false"));
         DEBUG_SPAWN = Boolean.parseBoolean(General.getProperty("DebugSpawn", "false"));
         PACKET_HANDLER_DEBUG = Boolean.parseBoolean(General.getProperty("PacketHandlerDebug", "false"));
         ALLOW_MULTISELL_DEBUG = Boolean.parseBoolean(General.getProperty("MutisellDebugPrice", "False"));
         DEVELOPER = Boolean.parseBoolean(General.getProperty("Developer", "false"));
         ALT_DEV_NO_HANDLERS = Boolean.parseBoolean(General.getProperty("AltDevNoHandlers", "False"));
         ALT_DEV_NO_SCRIPTS = Boolean.parseBoolean(General.getProperty("AltDevNoScripts", "False"));
         ALT_DEV_NO_SPAWNS = Boolean.parseBoolean(General.getProperty("AltDevNoSpawns", "False"));
         ALT_CHEST_NO_SPAWNS = Boolean.parseBoolean(General.getProperty("AltTreasureChestNoSpawns", "False"));
         SCHEDULED_THREAD_POOL_SIZE = Integer.parseInt(General.getProperty("ScheduledThreadPoolSize", "16"));
         EXECUTOR_THREAD_POOL_SIZE = Integer.parseInt(General.getProperty("ExecutorThreadPoolSize", "8"));
         ALLOW_DISCARDITEM = Boolean.parseBoolean(General.getProperty("AllowDiscardItem", "True"));
         String[] list = General.getProperty("ListOfDiscardItems", "0").split(",");
         LIST_DISCARDITEM_ITEMS = new ArrayList<>(list.length);

         for(String id : list) {
            LIST_DISCARDITEM_ITEMS.add(Integer.parseInt(id));
         }

         AUTODESTROY_ITEM_AFTER = Integer.parseInt(General.getProperty("AutoDestroyDroppedItemAfter", "600"));
         HERB_AUTO_DESTROY_TIME = Integer.parseInt(General.getProperty("AutoDestroyHerbTime", "60")) * 1000;
         String[] split = General.getProperty("ListOfProtectedItems", "0").split(",");
         LIST_PROTECTED_ITEMS = new ArrayList<>(split.length);

         for(String id : split) {
            LIST_PROTECTED_ITEMS.add(Integer.parseInt(id));
         }

         DATABASE_CLEAN_UP = Boolean.parseBoolean(General.getProperty("DatabaseCleanUp", "true"));
         CHAR_STORE_INTERVAL = Integer.parseInt(General.getProperty("CharacterDataStoreInterval", "15"));
         LAZY_ITEMS_UPDATE = Boolean.parseBoolean(General.getProperty("LazyItemsUpdate", "false"));
         UPDATE_ITEMS_ON_CHAR_STORE = Boolean.parseBoolean(General.getProperty("UpdateItemsOnCharStore", "false"));
         DESTROY_DROPPED_PLAYER_ITEM = Boolean.parseBoolean(General.getProperty("DestroyPlayerDroppedItem", "false"));
         DESTROY_EQUIPABLE_PLAYER_ITEM = Boolean.parseBoolean(General.getProperty("DestroyEquipableItem", "false"));
         SAVE_DROPPED_ITEM = Boolean.parseBoolean(General.getProperty("SaveDroppedItem", "false"));
         EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD = Boolean.parseBoolean(General.getProperty("EmptyDroppedItemTableAfterLoad", "false"));
         SAVE_DROPPED_ITEM_INTERVAL = Integer.parseInt(General.getProperty("SaveDroppedItemInterval", "60")) * 60000;
         CLEAR_DROPPED_ITEM_TABLE = Boolean.parseBoolean(General.getProperty("ClearDroppedItemTable", "false"));
         AUTODELETE_INVALID_QUEST_DATA = Boolean.parseBoolean(General.getProperty("AutoDeleteInvalidQuestData", "False"));
         PRECISE_DROP_CALCULATION = Boolean.parseBoolean(General.getProperty("PreciseDropCalculation", "True"));
         MULTIPLE_ITEM_DROP = Boolean.parseBoolean(General.getProperty("MultipleItemDrop", "True"));
         FORCE_INVENTORY_UPDATE = Boolean.parseBoolean(General.getProperty("ForceInventoryUpdate", "False"));
         LAZY_CACHE = Boolean.parseBoolean(General.getProperty("LazyCache", "True"));
         CACHE_CHAR_NAMES = Boolean.parseBoolean(General.getProperty("CacheCharNames", "True"));
         MIN_NPC_ANIMATION = Integer.parseInt(General.getProperty("MinNPCAnimation", "10"));
         MAX_NPC_ANIMATION = Integer.parseInt(General.getProperty("MaxNPCAnimation", "20"));
         MIN_MONSTER_ANIMATION = Integer.parseInt(General.getProperty("MinMonsterAnimation", "5"));
         MAX_MONSTER_ANIMATION = Integer.parseInt(General.getProperty("MaxMonsterAnimation", "20"));
         ENABLE_FALLING_DAMAGE = Boolean.parseBoolean(General.getProperty("EnableFallingDamage", "True"));
         PEACE_ZONE_MODE = Integer.parseInt(General.getProperty("PeaceZoneMode", "0"));
         ALLOW_WAREHOUSE = Boolean.parseBoolean(General.getProperty("AllowWarehouse", "True"));
         WAREHOUSE_CACHE = Boolean.parseBoolean(General.getProperty("WarehouseCache", "False"));
         WAREHOUSE_CACHE_TIME = Integer.parseInt(General.getProperty("WarehouseCacheTime", "15"));
         ALLOW_REFUND = Boolean.parseBoolean(General.getProperty("AllowRefund", "True"));
         ALLOW_MAIL = Boolean.parseBoolean(General.getProperty("AllowMail", "True"));
         MAIL_EXPIRATION = Integer.parseInt(General.getProperty("MailExpiration", "360"));
         MAIL_COND_EXPIRATION = Integer.parseInt(General.getProperty("MailCondExpiration", "12"));
         ALLOW_ATTACHMENTS = Boolean.parseBoolean(General.getProperty("AllowAttachments", "True"));
         ALLOW_WEAR = Boolean.parseBoolean(General.getProperty("AllowWear", "True"));
         WEAR_DELAY = Integer.parseInt(General.getProperty("WearDelay", "5"));
         WEAR_PRICE = Integer.parseInt(General.getProperty("WearPrice", "10"));
         ALLOW_LOTTERY = Boolean.parseBoolean(General.getProperty("AllowLottery", "True"));
         ALLOW_RACE = Boolean.parseBoolean(General.getProperty("AllowRace", "True"));
         ALLOW_WATER = Boolean.parseBoolean(General.getProperty("AllowWater", "True"));
         ALLOW_RENTPET = Boolean.parseBoolean(General.getProperty("AllowRentPet", "False"));
         ALLOWFISHING = Boolean.parseBoolean(General.getProperty("AllowFishing", "True"));
         ALLOW_MANOR = Boolean.parseBoolean(General.getProperty("AllowManor", "True"));
         ALLOW_BOAT = Boolean.parseBoolean(General.getProperty("AllowBoat", "True"));
         BOAT_BROADCAST_RADIUS = Integer.parseInt(General.getProperty("BoatBroadcastRadius", "20000"));
         ALLOW_CURSED_WEAPONS = Boolean.parseBoolean(General.getProperty("AllowCursedWeapons", "True"));
         ALLOW_PET_WALKERS = Boolean.parseBoolean(General.getProperty("AllowPetWalkers", "True"));
         SERVER_NEWS = Boolean.parseBoolean(General.getProperty("ShowServerNews", "False"));
         ALT_MANOR_REFRESH_TIME = Integer.parseInt(General.getProperty("AltManorRefreshTime", "20"));
         ALT_MANOR_REFRESH_MIN = Integer.parseInt(General.getProperty("AltManorRefreshMin", "00"));
         ALT_MANOR_APPROVE_TIME = Integer.parseInt(General.getProperty("AltManorApproveTime", "6"));
         ALT_MANOR_APPROVE_MIN = Integer.parseInt(General.getProperty("AltManorApproveMin", "00"));
         ALT_MANOR_MAINTENANCE_PERIOD = Integer.parseInt(General.getProperty("AltManorMaintenancePeriod", "360000"));
         ALT_MANOR_SAVE_ALL_ACTIONS = Boolean.parseBoolean(General.getProperty("AltManorSaveAllActions", "false"));
         ALT_MANOR_SAVE_PERIOD_RATE = Integer.parseInt(General.getProperty("AltManorSavePeriodRate", "2"));
         ALT_LOTTERY_PRIZE = Long.parseLong(General.getProperty("AltLotteryPrize", "50000"));
         ALT_LOTTERY_TICKET_PRICE = Long.parseLong(General.getProperty("AltLotteryTicketPrice", "2000"));
         ALT_LOTTERY_5_NUMBER_RATE = Float.parseFloat(General.getProperty("AltLottery5NumberRate", "0.6"));
         ALT_LOTTERY_4_NUMBER_RATE = Float.parseFloat(General.getProperty("AltLottery4NumberRate", "0.2"));
         ALT_LOTTERY_3_NUMBER_RATE = Float.parseFloat(General.getProperty("AltLottery3NumberRate", "0.2"));
         ALT_LOTTERY_2_AND_1_NUMBER_PRIZE = Long.parseLong(General.getProperty("AltLottery2and1NumberPrize", "200"));
         ALT_ITEM_AUCTION_ENABLED = Boolean.parseBoolean(General.getProperty("AltItemAuctionEnabled", "True"));
         ALLOW_ITEM_AUCTION_ANNOUNCE = Boolean.parseBoolean(General.getProperty("AllowItemAuctionAnnounce", "False"));
         ALT_ITEM_AUCTION_EXPIRED_AFTER = Integer.parseInt(General.getProperty("AltItemAuctionExpiredAfter", "14"));
         ALT_ITEM_AUCTION_TIME_EXTENDS_ON_BID = 1000L * (long)Integer.parseInt(General.getProperty("AltItemAuctionTimeExtendsOnBid", "0"));
         FS_TIME_ATTACK = Integer.parseInt(General.getProperty("TimeOfAttack", "50"));
         FS_TIME_COOLDOWN = Integer.parseInt(General.getProperty("TimeOfCoolDown", "5"));
         FS_TIME_ENTRY = Integer.parseInt(General.getProperty("TimeOfEntry", "3"));
         FS_TIME_WARMUP = Integer.parseInt(General.getProperty("TimeOfWarmUp", "2"));
         FS_PARTY_MEMBER_COUNT = Integer.parseInt(General.getProperty("NumberOfNecessaryPartyMembers", "4"));
         if (FS_TIME_ATTACK <= 0) {
            FS_TIME_ATTACK = 50;
         }

         if (FS_TIME_COOLDOWN <= 0) {
            FS_TIME_COOLDOWN = 5;
         }

         if (FS_TIME_ENTRY <= 0) {
            FS_TIME_ENTRY = 3;
         }

         if (FS_TIME_ENTRY <= 0) {
            FS_TIME_ENTRY = 3;
         }

         if (FS_TIME_ENTRY <= 0) {
            FS_TIME_ENTRY = 3;
         }

         RIFT_MIN_PARTY_SIZE = Integer.parseInt(General.getProperty("RiftMinPartySize", "5"));
         RIFT_MAX_JUMPS = Integer.parseInt(General.getProperty("MaxRiftJumps", "4"));
         RIFT_SPAWN_DELAY = Integer.parseInt(General.getProperty("RiftSpawnDelay", "10000"));
         RIFT_AUTO_JUMPS_TIME_MIN = Integer.parseInt(General.getProperty("AutoJumpsDelayMin", "480"));
         RIFT_AUTO_JUMPS_TIME_MAX = Integer.parseInt(General.getProperty("AutoJumpsDelayMax", "600"));
         RIFT_BOSS_ROOM_TIME_MUTIPLY = Float.parseFloat(General.getProperty("BossRoomTimeMultiply", "1.5"));
         RIFT_ENTER_COST_RECRUIT = Integer.parseInt(General.getProperty("RecruitCost", "18"));
         RIFT_ENTER_COST_SOLDIER = Integer.parseInt(General.getProperty("SoldierCost", "21"));
         RIFT_ENTER_COST_OFFICER = Integer.parseInt(General.getProperty("OfficerCost", "24"));
         RIFT_ENTER_COST_CAPTAIN = Integer.parseInt(General.getProperty("CaptainCost", "27"));
         RIFT_ENTER_COST_COMMANDER = Integer.parseInt(General.getProperty("CommanderCost", "30"));
         RIFT_ENTER_COST_HERO = Integer.parseInt(General.getProperty("HeroCost", "33"));
         CUSTOM_SPAWNLIST = Boolean.parseBoolean(General.getProperty("CustomSpawnlist", "false"));
         SAVE_GMSPAWN_ON_CUSTOM = Boolean.parseBoolean(General.getProperty("SaveGmSpawnOnCustom", "false"));
         CUSTOM_NPC = Boolean.parseBoolean(General.getProperty("CustomNpcs", "false"));
         CUSTOM_SKILLS = Boolean.parseBoolean(General.getProperty("CustomSkills", "false"));
         CUSTOM_ITEMS = Boolean.parseBoolean(General.getProperty("CustomItems", "false"));
         CUSTOM_MULTISELLS = Boolean.parseBoolean(General.getProperty("CustomMultisells", "false"));
         CUSTOM_BUYLIST = Boolean.parseBoolean(General.getProperty("CustomBuyList", "false"));
         ALT_BIRTHDAY_GIFT = Integer.parseInt(General.getProperty("AltBirthdayGift", "22187"));
         ALT_BIRTHDAY_MAIL_SUBJECT = General.getProperty("AltBirthdayMailSubject", "Happy Birthday!");
         ALT_BIRTHDAY_MAIL_TEXT = General.getProperty(
            "AltBirthdayMailText",
            "Hello Adventurer!! Seeing as you're one year older now, I thought I would send you some birthday cheer :) Please find your birthday pack attached. May these gifts bring you joy and happiness on this very special day."
               + EOL
               + EOL
               + "Sincerely, Alegria"
         );
         ENABLE_BLOCK_CHECKER_EVENT = Boolean.parseBoolean(General.getProperty("EnableBlockCheckerEvent", "false"));
         MIN_BLOCK_CHECKER_TEAM_MEMBERS = Integer.parseInt(General.getProperty("BlockCheckerMinTeamMembers", "2"));
         if (MIN_BLOCK_CHECKER_TEAM_MEMBERS < 1) {
            MIN_BLOCK_CHECKER_TEAM_MEMBERS = 1;
         } else if (MIN_BLOCK_CHECKER_TEAM_MEMBERS > 6) {
            MIN_BLOCK_CHECKER_TEAM_MEMBERS = 6;
         }

         HBCE_FAIR_PLAY = Boolean.parseBoolean(General.getProperty("HBCEFairPlay", "false"));
         CLEAR_CREST_CACHE = Boolean.parseBoolean(General.getProperty("ClearClanCache", "false"));
         NORMAL_ENCHANT_COST_MULTIPLIER = Integer.parseInt(General.getProperty("NormalEnchantCostMultipiler", "1"));
         SAFE_ENCHANT_COST_MULTIPLIER = Integer.parseInt(General.getProperty("SafeEnchantCostMultipiler", "5"));
      } catch (Exception var8) {
         _log.warning("Config: " + var8.getMessage());
         throw new Error("Failed to Load ./config/main/general.ini File.");
      }
   }

   private static void loadFloodProtectorSettings(InputStream is) {
      try {
         GameSettings security = new GameSettings();
         InputStream var3 = new FileInputStream(new File("./config/main/floodprotector.ini"));
         security.load(var3);
         loadFloodProtectorConfigs(security);
      } catch (Exception var2) {
         _log.warning("Config: " + var2.getMessage());
         throw new Error("Failed to Load ./config/main/floodprotector.ini");
      }
   }

   private static void loadNpcsSettings(InputStream is) {
      try {
         GameSettings NPC = new GameSettings();
         InputStream var14 = new FileInputStream(new File("./config/main/npc.ini"));
         NPC.load(var14);
         PLAYER_MOVEMENT_BLOCK_TIME = Integer.parseInt(NPC.getProperty("NpcTalkBlockingTime", "0")) * 1000;
         ANNOUNCE_MAMMON_SPAWN = Boolean.parseBoolean(NPC.getProperty("AnnounceMammonSpawn", "False"));
         ALT_MOB_AGRO_IN_PEACEZONE = Boolean.parseBoolean(NPC.getProperty("AltMobAgroInPeaceZone", "True"));
         ALT_ATTACKABLE_NPCS = Boolean.parseBoolean(NPC.getProperty("AltAttackableNpcs", "True"));
         ALT_GAME_VIEWNPC = Boolean.parseBoolean(NPC.getProperty("AltGameViewNpc", "False"));
         MAX_DRIFT_RANGE = Integer.parseInt(NPC.getProperty("MaxDriftRange", "300"));
         DEEPBLUE_DROP_RULES = Boolean.parseBoolean(NPC.getProperty("UseDeepBlueDropRules", "True"));
         DEEPBLUE_DROP_MAXDIFF = Integer.parseInt(NPC.getProperty("DeepBlueDropMaxDiff", "8"));
         DEEPBLUE_DROP_RAID_MAXDIFF = Integer.parseInt(NPC.getProperty("DeepBlueDropRaidMaxDiff", "2"));
         SHOW_NPC_SERVER_NAME = Boolean.parseBoolean(NPC.getProperty("ShowNpcServerName", "False"));
         SHOW_NPC_SERVER_TITLE = Boolean.parseBoolean(NPC.getProperty("ShowNpcServerTitle", "False"));
         SHOW_NPC_LVL = Boolean.parseBoolean(NPC.getProperty("ShowNpcLevel", "False"));
         SHOW_CREST_WITHOUT_QUEST = Boolean.parseBoolean(NPC.getProperty("ShowCrestWithoutQuest", "False"));
         ENABLE_RANDOM_ENCHANT_EFFECT = Boolean.parseBoolean(NPC.getProperty("EnableRandomEnchantEffect", "False"));
         MIN_NPC_LVL_DMG_PENALTY = Integer.parseInt(NPC.getProperty("MinNPCLevelForDmgPenalty", "78"));
         NPC_DMG_PENALTY = parseConfigLine(NPC.getProperty("DmgPenaltyForLvLDifferences", "0.7, 0.6, 0.6, 0.55"));
         NPC_CRIT_DMG_PENALTY = parseConfigLine(NPC.getProperty("CritDmgPenaltyForLvLDifferences", "0.75, 0.65, 0.6, 0.58"));
         NPC_SKILL_DMG_PENALTY = parseConfigLine(NPC.getProperty("SkillDmgPenaltyForLvLDifferences", "0.8, 0.7, 0.65, 0.62"));
         MIN_NPC_LVL_MAGIC_PENALTY = Integer.parseInt(NPC.getProperty("MinNPCLevelForMagicPenalty", "78"));
         NPC_SKILL_CHANCE_PENALTY = parseConfigLine(NPC.getProperty("SkillChancePenaltyForLvLDifferences", "2.5, 3.0, 3.25, 3.5"));
         DECAY_TIME_TASK = Integer.parseInt(NPC.getProperty("DecayTimeTask", "5"));
         NPC_DEAD_TIME_TASK = Integer.parseInt(NPC.getProperty("NpcDeadTimeTask", "3"));
         NPC_DECAY_TIME = Integer.parseInt(NPC.getProperty("NpcDecayTime", "7"));
         RAID_BOSS_DECAY_TIME = Integer.parseInt(NPC.getProperty("RaidBossDecayTime", "30"));
         SPOILED_DECAY_TIME = Integer.parseInt(NPC.getProperty("SpoiledDecayTime", "10"));
         MAX_SWEEPER_TIME = Integer.parseInt(NPC.getProperty("SweeperTimeTimeBeforeDecay", "2"));
         GUARD_ATTACK_AGGRO_MOB = Boolean.parseBoolean(NPC.getProperty("GuardAttackAggroMob", "False"));
         ALLOW_WYVERN_UPGRADER = Boolean.parseBoolean(NPC.getProperty("AllowWyvernUpgrader", "False"));
         String[] split = NPC.getProperty("ListPetRentNpc", "30827").split(",");
         LIST_PET_RENT_NPC = new ArrayList<>(split.length);

         for(String id : split) {
            LIST_PET_RENT_NPC.add(Integer.valueOf(id));
         }

         RAID_HP_REGEN_MULTIPLIER = Double.parseDouble(NPC.getProperty("RaidHpRegenMultiplier", "100")) / 100.0;
         RAID_MP_REGEN_MULTIPLIER = Double.parseDouble(NPC.getProperty("RaidMpRegenMultiplier", "100")) / 100.0;
         RAID_PDEFENCE_MULTIPLIER = Double.parseDouble(NPC.getProperty("RaidPDefenceMultiplier", "100")) / 100.0;
         RAID_MDEFENCE_MULTIPLIER = Double.parseDouble(NPC.getProperty("RaidMDefenceMultiplier", "100")) / 100.0;
         RAID_PATTACK_MULTIPLIER = Double.parseDouble(NPC.getProperty("RaidPAttackMultiplier", "100")) / 100.0;
         RAID_MATTACK_MULTIPLIER = Double.parseDouble(NPC.getProperty("RaidMAttackMultiplier", "100")) / 100.0;
         RAID_MIN_RESPAWN_MULTIPLIER = Float.parseFloat(NPC.getProperty("RaidMinRespawnMultiplier", "1.0"));
         RAID_MAX_RESPAWN_MULTIPLIER = Float.parseFloat(NPC.getProperty("RaidMaxRespawnMultiplier", "1.0"));
         RAID_MINION_RESPAWN_TIMER = (double)Integer.parseInt(NPC.getProperty("RaidMinionRespawnTime", "300000"));
         String[] propertySplit = NPC.getProperty("CustomMinionsRespawnTime", "").split(";");
         MINIONS_RESPAWN_TIME = new HashMap<>(propertySplit.length);

         for(String prop : propertySplit) {
            String[] propSplit = prop.split(",");
            if (propSplit.length != 2) {
               _log.warning("[CustomMinionsRespawnTime]: invalid config property -> CustomMinionsRespawnTime " + prop + "");
            }

            try {
               MINIONS_RESPAWN_TIME.put(Integer.valueOf(propSplit[0]), Integer.valueOf(propSplit[1]));
            } catch (NumberFormatException var12) {
               if (!prop.isEmpty()) {
                  _log.warning("[CustomMinionsRespawnTime]: invalid config property -> CustomMinionsRespawnTime " + propSplit[0] + " " + propSplit[1] + "");
               }
            }
         }

         RAID_DISABLE_CURSE = Boolean.parseBoolean(NPC.getProperty("DisableRaidCurse", "False"));
         INVENTORY_MAXIMUM_PET = Integer.parseInt(NPC.getProperty("MaximumSlotsForPet", "12"));
         PET_HP_REGEN_MULTIPLIER = Double.parseDouble(NPC.getProperty("PetHpRegenMultiplier", "100")) / 100.0;
         PET_MP_REGEN_MULTIPLIER = Double.parseDouble(NPC.getProperty("PetMpRegenMultiplier", "100")) / 100.0;
         LAKFI_ENABLED = Boolean.parseBoolean(NPC.getProperty("LakfiSpawnEnabled", "True"));
         TIME_CHANGE_SPAWN = Integer.parseInt(NPC.getProperty("IntervalChangeSpawn", "20"));
         MIN_ADENA_TO_EAT = Long.parseLong(NPC.getProperty("MinAdenaLakfiEat", "10000"));
         TIME_IF_NOT_FEED = Integer.parseInt(NPC.getProperty("TimeIfNotFeedDissapear", "10"));
         INTERVAL_EATING = Integer.parseInt(NPC.getProperty("IntervalBetweenEating", "15"));
         DRAGON_VORTEX_UNLIMITED_SPAWN = Boolean.parseBoolean(NPC.getProperty("DragonVortexUnlimitedSpawn", "False"));
         VORTEX_DELAY_TIME = Integer.parseInt(NPC.getProperty("DragonVortexDelaySpawnTime", "0"));
         ALLOW_RAIDBOSS_CHANCE_DEBUFF = Boolean.parseBoolean(NPC.getProperty("AllowRaidBossDebuff", "True"));
         RAIDBOSS_CHANCE_DEBUFF = Double.parseDouble(NPC.getProperty("RaidBossChanceDebuff", "0.9"));
         ALLOW_GRANDBOSS_CHANCE_DEBUFF = Boolean.parseBoolean(NPC.getProperty("AllowGrandBossDebuff", "True"));
         GRANDBOSS_CHANCE_DEBUFF = Double.parseDouble(NPC.getProperty("GrandBossChanceDebuff", "0.3"));
         RAIDBOSS_CHANCE_DEBUFF_SPECIAL = Double.parseDouble(NPC.getProperty("RaidBossChanceDebuffSpecial", "0.4"));
         GRANDBOSS_CHANCE_DEBUFF_SPECIAL = Double.parseDouble(NPC.getProperty("GrandBossChanceDebuffSpecial", "0.1"));
         String[] raidList = NPC.getProperty("SpecialRaidBossList", "29020,29068,29028").split(",");
         RAIDBOSS_DEBUFF_SPECIAL = new int[raidList.length];

         for(int i = 0; i < raidList.length; ++i) {
            RAIDBOSS_DEBUFF_SPECIAL[i] = Integer.parseInt(raidList[i]);
         }

         Arrays.sort(RAIDBOSS_DEBUFF_SPECIAL);
         String[] grandList = NPC.getProperty("SpecialGrandBossList", "29020,29068,29028").split(",");
         GRANDBOSS_DEBUFF_SPECIAL = new int[grandList.length];

         for(int i = 0; i < grandList.length; ++i) {
            GRANDBOSS_DEBUFF_SPECIAL[i] = Integer.parseInt(grandList[i]);
         }

         Arrays.sort(GRANDBOSS_DEBUFF_SPECIAL);
         SOULSHOT_CHANCE = Integer.parseInt(NPC.getProperty("SoulShotsChance", "30"));
         SPIRITSHOT_CHANCE = Integer.parseInt(NPC.getProperty("SpiritShotsChance", "30"));
         ALWAYS_TELEPORT_HOME = Boolean.parseBoolean(NPC.getProperty("AlwaysTeleportHome", "False"));
         MAX_PURSUE_RANGE = Integer.parseInt(NPC.getProperty("MaxPursueRange", "4000"));
         MAX_PURSUE_RANGE_RAID = Integer.parseInt(NPC.getProperty("MaxPursueRangeRaid", "5000"));
         CALC_NPCS_STATS = Boolean.parseBoolean(NPC.getProperty("CalcNpcStats", "False"));
         CRUMA_MAX_LEVEL = Integer.parseInt(NPC.getProperty("CrumaMaxLvlEnter", "55"));
         String[] npcList = NPC.getProperty("NpcBlockShiftList", "0").split(",");
         NPC_BLOCK_SHIFT_LIST = new int[npcList.length];

         for(int i = 0; i < npcList.length; ++i) {
            NPC_BLOCK_SHIFT_LIST[i] = Integer.parseInt(npcList[i]);
         }

         Arrays.sort(NPC_BLOCK_SHIFT_LIST);
         EPAULETTE_ONLY_FOR_REG = Boolean.parseBoolean(NPC.getProperty("DropEpauletteForRegisterPlayers", "False"));
         SKILLS_MOB_CHANCE = Double.parseDouble(NPC.getProperty("SkillsMobChance", "0.5"));
         ALLOW_NPC_LVL_MOD = Boolean.parseBoolean(NPC.getProperty("AllowNpcLvlMod", "False"));
         ALLOW_SUMMON_LVL_MOD = Boolean.parseBoolean(NPC.getProperty("AllowSummonLvlMod", "False"));
         PATK_HATE_MOD = Double.parseDouble(NPC.getProperty("PAtkHateModifier", "1.0"));
         MATK_HATE_MOD = Double.parseDouble(NPC.getProperty("MAtkHateModifier", "1.0"));
         PET_HATE_MOD = Double.parseDouble(NPC.getProperty("SummonAtkHateModifier", "1.0"));
         String[] raidAnnounce = NPC.getProperty("RaidAnnounceList", "0").split(",");
         RAIDBOSS_ANNOUNCE_LIST = new int[raidAnnounce.length];

         for(int i = 0; i < raidAnnounce.length; ++i) {
            RAIDBOSS_ANNOUNCE_LIST[i] = Integer.parseInt(raidAnnounce[i]);
         }

         Arrays.sort(RAIDBOSS_ANNOUNCE_LIST);
         String[] epicAnnounce = NPC.getProperty("EpicAnnounceList", "0").split(",");
         GRANDBOSS_ANNOUNCE_LIST = new int[epicAnnounce.length];

         for(int i = 0; i < epicAnnounce.length; ++i) {
            GRANDBOSS_ANNOUNCE_LIST[i] = Integer.parseInt(epicAnnounce[i]);
         }

         Arrays.sort(GRANDBOSS_ANNOUNCE_LIST);
         String[] raidDeadAnnounce = NPC.getProperty("RaidDeathAnnounceList", "0").split(",");
         RAIDBOSS_DEAD_ANNOUNCE_LIST = new int[raidDeadAnnounce.length];

         for(int i = 0; i < raidDeadAnnounce.length; ++i) {
            RAIDBOSS_DEAD_ANNOUNCE_LIST[i] = Integer.parseInt(raidDeadAnnounce[i]);
         }

         Arrays.sort(RAIDBOSS_DEAD_ANNOUNCE_LIST);
         String[] epicDeadAnnounce = NPC.getProperty("EpicDeathAnnounceList", "0").split(",");
         GRANDBOSS_DEAD_ANNOUNCE_LIST = new int[epicDeadAnnounce.length];

         for(int i = 0; i < epicDeadAnnounce.length; ++i) {
            GRANDBOSS_DEAD_ANNOUNCE_LIST[i] = Integer.parseInt(epicDeadAnnounce[i]);
         }

         Arrays.sort(GRANDBOSS_DEAD_ANNOUNCE_LIST);
         ALLOW_DAMAGE_LIMIT = Boolean.parseBoolean(NPC.getProperty("AllowDamageLimit", "False"));
         NPC_DROP_PROTECTION = Integer.parseInt(NPC.getProperty("NpcDropProtection", "15"));
         RAID_DROP_PROTECTION = Integer.parseInt(NPC.getProperty("RaidDropProtection", "300"));
      } catch (Exception var13) {
         _log.warning("Config: " + var13.getMessage());
         throw new Error("Failed to Load ./config/main/npc.ini File.");
      }
   }

   private static void loadRatesSettings(InputStream is) {
      try {
         GameSettings ratesSettings = new GameSettings();
         InputStream var21 = new FileInputStream(new File("./config/main/rates.ini"));
         ratesSettings.load(var21);
         MAX_DROP_ITEMS_FROM_ONE_GROUP = Integer.parseInt(ratesSettings.getProperty("MaxDropItemsFromOneGroup", "1"));
         MAX_SPOIL_ITEMS_FROM_ONE_GROUP = Integer.parseInt(ratesSettings.getProperty("MaxSpoilItemsFromOneGroup", "1"));
         MAX_DROP_ITEMS_FROM_ONE_GROUP_RAIDS = Integer.parseInt(ratesSettings.getProperty("MaxRaidDropItemsFromOneGroup", "1"));
         GROUP_CHANCE_MODIFIER = Double.parseDouble(ratesSettings.getProperty("GroupChanceModifier", "0.1"));
         double RATE_XP = Double.parseDouble(ratesSettings.getProperty("RateXp", "1."));
         RATE_XP_BY_LVL = new double[ExperienceParser.getInstance().getMaxLevel()];
         double prevRateXp = RATE_XP;

         for(int i = 1; i < RATE_XP_BY_LVL.length; ++i) {
            double rate = Double.parseDouble(ratesSettings.getProperty("RateXpByLevel" + i, "" + prevRateXp + ""));
            RATE_XP_BY_LVL[i] = rate;
            if (rate != prevRateXp) {
               prevRateXp = rate;
            }
         }

         double RATE_SP = Double.parseDouble(ratesSettings.getProperty("RateSp", "1."));
         RATE_SP_BY_LVL = new double[ExperienceParser.getInstance().getMaxLevel()];
         double prevRateSp = RATE_SP;

         for(int i = 1; i < RATE_SP_BY_LVL.length; ++i) {
            double rate = Double.parseDouble(ratesSettings.getProperty("RateSpByLevel" + i, "" + prevRateSp + ""));
            RATE_SP_BY_LVL[i] = rate;
            if (rate != prevRateSp) {
               prevRateSp = rate;
            }
         }

         RATE_PARTY_XP = Double.parseDouble(ratesSettings.getProperty("RatePartyXp", "1."));
         RATE_PARTY_SP = Double.parseDouble(ratesSettings.getProperty("RatePartySp", "1."));
         RATE_DROP_ADENA = Double.parseDouble(ratesSettings.getProperty("RateDropAdena", "1."));
         RATE_DROP_ITEMS = Double.parseDouble(ratesSettings.getProperty("RateDropItems", "1."));
         RATE_CHANCE_ATTRIBUTE = Double.parseDouble(ratesSettings.getProperty("RateChanceAttribute", "1."));
         RATE_DROP_SPOIL = Double.parseDouble(ratesSettings.getProperty("RateDropSpoil", "1."));
         RATE_DROP_RAIDBOSS = Double.parseDouble(ratesSettings.getProperty("RateRaidBoss", "1."));
         RATE_DROP_EPICBOSS = Double.parseDouble(ratesSettings.getProperty("RateEpicBoss", "1."));
         RATE_CHANCE_GROUP_DROP_ITEMS = Double.parseDouble(ratesSettings.getProperty("RateChanceGroupDropItems", "1."));
         RATE_CHANCE_DROP_ITEMS = Double.parseDouble(ratesSettings.getProperty("RateChanceDropItems", "1."));
         RATE_CHANCE_DROP_HERBS = Double.parseDouble(ratesSettings.getProperty("RateChanceDropHerbs", "1."));
         RATE_CHANCE_SPOIL = Double.parseDouble(ratesSettings.getProperty("RateChanceSpoil", "1."));
         RATE_CHANCE_SPOIL_WEAPON_ARMOR_ACCESSORY = Double.parseDouble(ratesSettings.getProperty("RateChanceSpoilWAA", "1."));
         RATE_CHANCE_DROP_WEAPON_ARMOR_ACCESSORY = Double.parseDouble(ratesSettings.getProperty("RateChanceDropWAA", "1."));
         RATE_CHANCE_DROP_EPOLET = Double.parseDouble(ratesSettings.getProperty("RateChanceDropEpolets", "1."));
         RATE_DROP_SIEGE_GUARD = Double.parseDouble(ratesSettings.getProperty("RateSiegeGuard", "1."));
         RATE_DROP_FISHING = Double.parseDouble(ratesSettings.getProperty("RateFishing", "1."));
         RATE_NOBLE_STONES_COUNT_MIN = Double.parseDouble(ratesSettings.getProperty("ModifierNobleStonesMinCount", "1."));
         RATE_LIFE_STONES_COUNT_MIN = Double.parseDouble(ratesSettings.getProperty("ModifierLifeStonesMinCount", "1."));
         RATE_ENCHANT_SCROLLS_COUNT_MIN = Double.parseDouble(ratesSettings.getProperty("ModifierEnchantScrollsMinCount", "1."));
         RATE_FORGOTTEN_SCROLLS_COUNT_MIN = Double.parseDouble(ratesSettings.getProperty("ModifierForgottenScrollsMinCount", "1."));
         RATE_KEY_MATHETIRALS_COUNT_MIN = Double.parseDouble(ratesSettings.getProperty("ModifierMaterialsMinCount", "1."));
         RATE_RECEPIES_COUNT_MIN = Double.parseDouble(ratesSettings.getProperty("ModifierRepicesMinCount", "1."));
         RATE_BELTS_COUNT_MIN = Double.parseDouble(ratesSettings.getProperty("ModifierBeltsMinCount", "1."));
         RATE_BRACELETS_COUNT_MIN = Double.parseDouble(ratesSettings.getProperty("ModifierBraceletsMinCount", "1."));
         RATE_CLOAKS_COUNT_MIN = Double.parseDouble(ratesSettings.getProperty("ModifierCloaksMinCount", "1."));
         RATE_CODEX_BOOKS_COUNT_MIN = Double.parseDouble(ratesSettings.getProperty("ModifierCodexBooksMinCount", "1."));
         RATE_ATTRIBUTE_STONES_COUNT_MIN = Double.parseDouble(ratesSettings.getProperty("ModifierAttStonesMinCount", "1."));
         RATE_ATTRIBUTE_CRYSTALS_COUNT_MIN = Double.parseDouble(ratesSettings.getProperty("ModifierAttCrystalsMinCount", "1."));
         RATE_ATTRIBUTE_JEWELS_COUNT_MIN = Double.parseDouble(ratesSettings.getProperty("ModifierAttJewelsMinCount", "1."));
         RATE_ATTRIBUTE_ENERGY_COUNT_MIN = Double.parseDouble(ratesSettings.getProperty("ModifierAttEnergyMinCount", "1."));
         RATE_WEAPONS_COUNT_MIN = Double.parseDouble(ratesSettings.getProperty("ModifierWeaponsMinCount", "1."));
         RATE_ARMOR_COUNT_MIN = Double.parseDouble(ratesSettings.getProperty("ModifierArmorsMinCount", "1."));
         RATE_ACCESSORY_COUNT_MIN = Double.parseDouble(ratesSettings.getProperty("ModifierAccessoryesMinCount", "1."));
         RATE_SEAL_STONES_COUNT_MIN = Double.parseDouble(ratesSettings.getProperty("ModifierSealStonesMinCount", "1."));
         RATE_NOBLE_STONES_COUNT_MAX = Double.parseDouble(ratesSettings.getProperty("ModifierNobleStonesMaxCount", "1."));
         RATE_LIFE_STONES_COUNT_MAX = Double.parseDouble(ratesSettings.getProperty("ModifierLifeStonesMaxCount", "1."));
         RATE_ENCHANT_SCROLLS_COUNT_MAX = Double.parseDouble(ratesSettings.getProperty("ModifierEnchantScrollsMaxCount", "1."));
         RATE_FORGOTTEN_SCROLLS_COUNT_MAX = Double.parseDouble(ratesSettings.getProperty("ModifierForgottenScrollsMaxCount", "1."));
         RATE_KEY_MATHETIRALS_COUNT_MAX = Double.parseDouble(ratesSettings.getProperty("ModifierMaterialsMaxCount", "1."));
         RATE_RECEPIES_COUNT_MAX = Double.parseDouble(ratesSettings.getProperty("ModifierRepicesMaxCount", "1."));
         RATE_BELTS_COUNT_MAX = Double.parseDouble(ratesSettings.getProperty("ModifierBeltsMaxCount", "1."));
         RATE_BRACELETS_COUNT_MAX = Double.parseDouble(ratesSettings.getProperty("ModifierBraceletsMaxCount", "1."));
         RATE_CLOAKS_COUNT_MAX = Double.parseDouble(ratesSettings.getProperty("ModifierCloaksMaxCount", "1."));
         RATE_CODEX_BOOKS_COUNT_MAX = Double.parseDouble(ratesSettings.getProperty("ModifierCodexBooksMaxCount", "1."));
         RATE_ATTRIBUTE_STONES_COUNT_MAX = Double.parseDouble(ratesSettings.getProperty("ModifierAttStonesMaxCount", "1."));
         RATE_ATTRIBUTE_CRYSTALS_COUNT_MAX = Double.parseDouble(ratesSettings.getProperty("ModifierAttCrystalsMaxCount", "1."));
         RATE_ATTRIBUTE_JEWELS_COUNT_MAX = Double.parseDouble(ratesSettings.getProperty("ModifierAttJewelsMaxCount", "1."));
         RATE_ATTRIBUTE_ENERGY_COUNT_MAX = Double.parseDouble(ratesSettings.getProperty("ModifierAttEnergyMaxCount", "1."));
         RATE_WEAPONS_COUNT_MAX = Double.parseDouble(ratesSettings.getProperty("ModifierWeaponsMaxCount", "1."));
         RATE_ARMOR_COUNT_MAX = Double.parseDouble(ratesSettings.getProperty("ModifierArmorsMaxCount", "1."));
         RATE_ACCESSORY_COUNT_MAX = Double.parseDouble(ratesSettings.getProperty("ModifierAccessoryesMaxCount", "1."));
         RATE_SEAL_STONES_COUNT_MAX = Double.parseDouble(ratesSettings.getProperty("ModifierSealStonesMaxCount", "1."));
         ALLOW_MODIFIER_FOR_DROP = Boolean.parseBoolean(ratesSettings.getProperty("AllowModifierForDrop", "True"));
         ALLOW_MODIFIER_FOR_RAIDS = Boolean.parseBoolean(ratesSettings.getProperty("AllowModifierForRaids", "True"));
         ALLOW_MODIFIER_FOR_SPOIL = Boolean.parseBoolean(ratesSettings.getProperty("AllowModifierForSpoil", "True"));
         NO_RATE_EQUIPMENT = Boolean.parseBoolean(ratesSettings.getProperty("NoRateEquipment", "True"));
         NO_RATE_KEY_MATERIAL = Boolean.parseBoolean(ratesSettings.getProperty("NoRateKeyMaterial", "True"));
         NO_RATE_RECIPES = Boolean.parseBoolean(ratesSettings.getProperty("NoRateRecipes", "True"));
         String[] notRate = ratesSettings.getProperty("NoRateItemIds", "6660,6662,6661,6659,6656,6658,8191,6657,10170,10314,16025,16026").split(",");
         NO_RATE_ITEMS = new int[notRate.length];

         for(int i = 0; i < notRate.length; ++i) {
            NO_RATE_ITEMS[i] = Integer.parseInt(notRate[i]);
         }

         Arrays.sort(NO_RATE_ITEMS);
         String[] mod = ratesSettings.getProperty("MaxAmountCorrectListMod", "2,0.5;5,0.4;10,0.3;50,0.25;100").split(";");
         MAX_AMOUNT_CORRECTOR = new HashMap<>(mod.length);

         for(String entry : mod) {
            String[] entrySplit = entry.split(",");
            if (entrySplit.length != 2) {
               _log.warning("[Config.load()]: invalid config property -> MaxAmountCorrectListMod " + entry + "");
            } else {
               try {
                  MAX_AMOUNT_CORRECTOR.put(Integer.parseInt(entrySplit[0]), Double.parseDouble(entrySplit[1]));
               } catch (Exception var19) {
                  _log.warning("[Config.load()]: MaxAmountCorrectListMod invalid params!");
               }
            }
         }

         RATE_CONSUMABLE_COST = Double.parseDouble(ratesSettings.getProperty("RateConsumableCost", "1."));
         RATE_EXTRACTABLE = Double.parseDouble(ratesSettings.getProperty("RateExtractable", "1."));
         RATE_DROP_MANOR = Double.parseDouble(ratesSettings.getProperty("RateDropManor", "1."));
         RATE_QUEST_DROP = Float.parseFloat(ratesSettings.getProperty("RateQuestDrop", "1."));
         RATE_QUEST_REWARD = Float.parseFloat(ratesSettings.getProperty("RateQuestReward", "1."));
         RATE_QUEST_REWARD_XP = Float.parseFloat(ratesSettings.getProperty("RateQuestRewardXP", "1."));
         RATE_QUEST_REWARD_SP = Float.parseFloat(ratesSettings.getProperty("RateQuestRewardSP", "1."));
         RATE_QUEST_REWARD_ADENA = Float.parseFloat(ratesSettings.getProperty("RateQuestRewardAdena", "1."));
         RATE_QUEST_REWARD_USE_MULTIPLIERS = Boolean.parseBoolean(ratesSettings.getProperty("UseQuestRewardMultipliers", "False"));
         RATE_QUEST_REWARD_POTION = Float.parseFloat(ratesSettings.getProperty("RateQuestRewardPotion", "1."));
         RATE_QUEST_REWARD_SCROLL = Float.parseFloat(ratesSettings.getProperty("RateQuestRewardScroll", "1."));
         RATE_QUEST_REWARD_RECIPE = Float.parseFloat(ratesSettings.getProperty("RateQuestRewardRecipe", "1."));
         RATE_QUEST_REWARD_MATERIAL = Float.parseFloat(ratesSettings.getProperty("RateQuestRewardMaterial", "1."));
         ADENA_FIXED_CHANCE = Double.parseDouble(ratesSettings.getProperty("AdenaFixedChance", "0."));
         RATE_HB_TRUST_INCREASE = Double.parseDouble(ratesSettings.getProperty("RateHellboundTrustIncrease", "1."));
         RATE_HB_TRUST_DECREASE = Double.parseDouble(ratesSettings.getProperty("RateHellboundTrustDecrease", "1."));
         RATE_VITALITY_LEVEL_1 = Float.parseFloat(ratesSettings.getProperty("RateVitalityLevel1", "1.5"));
         RATE_VITALITY_LEVEL_2 = Float.parseFloat(ratesSettings.getProperty("RateVitalityLevel2", "2."));
         RATE_VITALITY_LEVEL_3 = Float.parseFloat(ratesSettings.getProperty("RateVitalityLevel3", "2.5"));
         RATE_VITALITY_LEVEL_4 = Float.parseFloat(ratesSettings.getProperty("RateVitalityLevel4", "3."));
         RATE_RECOVERY_VITALITY_PEACE_ZONE = Float.parseFloat(ratesSettings.getProperty("RateRecoveryPeaceZone", "1."));
         RATE_VITALITY_LOST = Float.parseFloat(ratesSettings.getProperty("RateVitalityLost", "1."));
         RATE_VITALITY_GAIN = Float.parseFloat(ratesSettings.getProperty("RateVitalityGain", "1."));
         RATE_RECOVERY_ON_RECONNECT = Float.parseFloat(ratesSettings.getProperty("RateRecoveryOnReconnect", "4."));
         RATE_KARMA_EXP_LOST = Float.parseFloat(ratesSettings.getProperty("RateKarmaExpLost", "1."));
         RATE_SIEGE_GUARDS_PRICE = Float.parseFloat(ratesSettings.getProperty("RateSiegeGuardsPrice", "1."));
         PLAYER_DROP_LIMIT = Integer.parseInt(ratesSettings.getProperty("PlayerDropLimit", "3"));
         PLAYER_RATE_DROP = Integer.parseInt(ratesSettings.getProperty("PlayerRateDrop", "5"));
         PLAYER_RATE_DROP_ITEM = Integer.parseInt(ratesSettings.getProperty("PlayerRateDropItem", "70"));
         PLAYER_RATE_DROP_EQUIP = Integer.parseInt(ratesSettings.getProperty("PlayerRateDropEquip", "25"));
         PLAYER_RATE_DROP_EQUIP_WEAPON = Integer.parseInt(ratesSettings.getProperty("PlayerRateDropEquipWeapon", "5"));
         PET_XP_RATE = Float.parseFloat(ratesSettings.getProperty("PetXpRate", "1."));
         PET_FOOD_RATE = Integer.parseInt(ratesSettings.getProperty("PetFoodRate", "1"));
         SINEATER_XP_RATE = Float.parseFloat(ratesSettings.getProperty("SinEaterXpRate", "1."));
         KARMA_DROP_LIMIT = Integer.parseInt(ratesSettings.getProperty("KarmaDropLimit", "10"));
         KARMA_RATE_DROP = Integer.parseInt(ratesSettings.getProperty("KarmaRateDrop", "70"));
         KARMA_RATE_DROP_ITEM = Integer.parseInt(ratesSettings.getProperty("KarmaRateDropItem", "50"));
         KARMA_RATE_DROP_EQUIP = Integer.parseInt(ratesSettings.getProperty("KarmaRateDropEquip", "40"));
         KARMA_RATE_DROP_EQUIP_WEAPON = Integer.parseInt(ratesSettings.getProperty("KarmaRateDropEquipWeapon", "10"));
         String[] items = ratesSettings.getProperty("DisableItemDropList", "0").split(",");
         DISABLE_ITEM_DROP_LIST = new ArrayList<>(items.length);

         for(String id : items) {
            DISABLE_ITEM_DROP_LIST.add(Integer.parseInt(id));
         }

         RATE_TALISMAN_MULTIPLIER = Integer.parseInt(ratesSettings.getProperty("TalismanAmountMultiplier", "1"));
         RATE_TALISMAN_ITEM_MULTIPLIER = Integer.parseInt(ratesSettings.getProperty("TalismanItemAmountMultiplier", "1"));
         String[] noSweep = ratesSettings.getProperty("DisableItemSweepList", "0").split(",");
         NO_DROP_ITEMS_FOR_SWEEP = new ArrayList<>(noSweep.length);

         for(String id : noSweep) {
            NO_DROP_ITEMS_FOR_SWEEP.add(Integer.parseInt(id));
         }

         String[] dropOnly = ratesSettings.getProperty("DropOnlyTheseItemList", "0").split(",");
         ALLOW_ONLY_THESE_DROP_ITEMS_ID = new ArrayList<>(dropOnly.length);

         for(String id : dropOnly) {
            ALLOW_ONLY_THESE_DROP_ITEMS_ID.add(Integer.parseInt(id));
         }
      } catch (Exception var20) {
         _log.warning("Config: " + var20.getMessage());
         throw new Error("Failed to Load ./config/main/rates.ini File.");
      }
   }

   private static void loadPvpSettings(InputStream is) {
      try {
         GameSettings pvpSettings = new GameSettings();
         InputStream var5 = new FileInputStream(new File("./config/main/pvp.ini"));
         pvpSettings.load(var5);
         KARMA_MIN_KARMA = Integer.parseInt(pvpSettings.getProperty("MinKarma", "240"));
         KARMA_MAX_KARMA = Integer.parseInt(pvpSettings.getProperty("MaxKarma", "10000"));
         KARMA_XP_DIVIDER = Integer.parseInt(pvpSettings.getProperty("XPDivider", "260"));
         KARMA_LOST_BASE = Integer.parseInt(pvpSettings.getProperty("BaseKarmaLost", "0"));
         KARMA_DROP_GM = Boolean.parseBoolean(pvpSettings.getProperty("CanGMDropEquipment", "false"));
         KARMA_AWARD_PK_KILL = Boolean.parseBoolean(pvpSettings.getProperty("AwardPKKillPVPPoint", "true"));
         KARMA_PK_LIMIT = Integer.parseInt(pvpSettings.getProperty("MinimumPKRequiredToDrop", "5"));
         KARMA_NONDROPPABLE_PET_ITEMS = pvpSettings.getProperty("ListOfPetItems", "2375,3500,3501,3502,4422,4423,4424,4425,6648,6649,6650,9882");
         KARMA_NONDROPPABLE_ITEMS = pvpSettings.getProperty(
            "ListOfNonDroppableItems",
            "57,1147,425,1146,461,10,2368,7,6,2370,2369,6842,6611,6612,6613,6614,6615,6616,6617,6618,6619,6620,6621,7694,8181,5575,7694,9388,9389,9390"
         );
         String[] array = KARMA_NONDROPPABLE_PET_ITEMS.split(",");
         KARMA_LIST_NONDROPPABLE_PET_ITEMS = new int[array.length];

         for(int i = 0; i < array.length; ++i) {
            KARMA_LIST_NONDROPPABLE_PET_ITEMS[i] = Integer.parseInt(array[i]);
         }

         array = KARMA_NONDROPPABLE_ITEMS.split(",");
         KARMA_LIST_NONDROPPABLE_ITEMS = new int[array.length];

         for(int i = 0; i < array.length; ++i) {
            KARMA_LIST_NONDROPPABLE_ITEMS[i] = Integer.parseInt(array[i]);
         }

         Arrays.sort(KARMA_LIST_NONDROPPABLE_PET_ITEMS);
         Arrays.sort(KARMA_LIST_NONDROPPABLE_ITEMS);
         PVP_NORMAL_TIME = Integer.parseInt(pvpSettings.getProperty("PvPVsNormalTime", "120000"));
         PVP_PVP_TIME = Integer.parseInt(pvpSettings.getProperty("PvPVsPvPTime", "60000"));
         DISABLE_ATTACK_IF_LVL_DIFFERENCE_OVER = Integer.parseInt(pvpSettings.getProperty("DisableAttackIfLvlDifferenceOver", "0"));
         PVP_COLOR_SYSTEM = Boolean.parseBoolean(pvpSettings.getProperty("PvPColorSystem", "False"));
         PVP_AMMOUNT1 = Integer.parseInt(pvpSettings.getProperty("PvpAmmount1", "50"));
         PVP_AMMOUNT2 = Integer.parseInt(pvpSettings.getProperty("PvpAmmount2", "100"));
         PVP_AMMOUNT3 = Integer.parseInt(pvpSettings.getProperty("PvpAmmount3", "150"));
         PVP_AMMOUNT4 = Integer.parseInt(pvpSettings.getProperty("PvpAmmount4", "250"));
         PVP_AMMOUNT5 = Integer.parseInt(pvpSettings.getProperty("PvpAmmount5", "500"));
         COLOR_FOR_AMMOUNT1 = Integer.decode("0x" + pvpSettings.getProperty("ColorForAmmount1", "00FF00"));
         COLOR_FOR_AMMOUNT2 = Integer.decode("0x" + pvpSettings.getProperty("ColorForAmmount2", "00FF00"));
         COLOR_FOR_AMMOUNT3 = Integer.decode("0x" + pvpSettings.getProperty("ColorForAmmount3", "00FF00"));
         COLOR_FOR_AMMOUNT4 = Integer.decode("0x" + pvpSettings.getProperty("ColorForAmmount4", "00FF00"));
         COLOR_FOR_AMMOUNT5 = Integer.decode("0x" + pvpSettings.getProperty("ColorForAmmount4", "00FF00"));
         TITLE_COLOR_FOR_AMMOUNT1 = Integer.decode("0x" + pvpSettings.getProperty("TitleForAmmount1", "00FF00"));
         TITLE_COLOR_FOR_AMMOUNT2 = Integer.decode("0x" + pvpSettings.getProperty("TitleForAmmount2", "00FF00"));
         TITLE_COLOR_FOR_AMMOUNT3 = Integer.decode("0x" + pvpSettings.getProperty("TitleForAmmount3", "00FF00"));
         TITLE_COLOR_FOR_AMMOUNT4 = Integer.decode("0x" + pvpSettings.getProperty("TitleForAmmount4", "00FF00"));
         TITLE_COLOR_FOR_AMMOUNT5 = Integer.decode("0x" + pvpSettings.getProperty("TitleForAmmount5", "00FF00"));
      } catch (Exception var4) {
         _log.warning("Config: " + var4.getMessage());
         throw new Error("Failed to Load ./config/main/pvp.ini File.");
      }
   }

   private static void loadOlympiadSettings(InputStream is) {
      try {
         GameSettings olympiad = new GameSettings();
         InputStream var8 = new FileInputStream(new File("./config/main/olympiad.ini"));
         olympiad.load(var8);
         OLYMPIAD_PERIOD = olympiad.getProperty("OlympiadPeriod", "0 0 1 * *");
         AUTO_GET_HERO = Boolean.parseBoolean(olympiad.getProperty("AllowAutoGetHero", "False"));
         ALLOW_OLY_HIT_SUMMON = Boolean.parseBoolean(olympiad.getProperty("AllowToHitSummon", "False"));
         OLY_PRINT_CLASS_OPPONENT = Boolean.parseBoolean(olympiad.getProperty("AllowPrintOpponentInfo", "False"));
         ALLOW_OLY_FAST_INVITE = Boolean.parseBoolean(olympiad.getProperty("AllowConfirmDlgInvite", "False"));
         ALLOW_RESTART_AT_OLY = Boolean.parseBoolean(olympiad.getProperty("AllowRestartAtOly", "True"));
         ALT_OLY_START_TIME = olympiad.getProperty("AltOlyStartTime", "0 18 * * *");
         ALT_OLY_CPERIOD = Long.parseLong(olympiad.getProperty("AltOlyCompetitionPeriod", "6"));
         ALT_OLY_BATTLE = Long.parseLong(olympiad.getProperty("AltOlyBattle", "5"));
         ALT_OLY_TELE_TO_TOWN = Integer.parseInt(olympiad.getProperty("AltOlyTeleToTown", "40"));
         ALT_OLY_WPERIOD = Long.parseLong(olympiad.getProperty("AltOlyWeeklyPeriod", "168"));
         ALT_OLY_VPERIOD = Long.parseLong(olympiad.getProperty("AltOlyValidationPeriod", "12"));
         ALT_OLY_START_POINTS = Integer.parseInt(olympiad.getProperty("AltOlyStartPoints", "10"));
         ALT_OLY_WEEKLY_POINTS = Integer.parseInt(olympiad.getProperty("AltOlyWeeklyPoints", "10"));
         ALT_OLY_CLASSED = Integer.parseInt(olympiad.getProperty("AltOlyClassedParticipants", "11"));
         ALT_OLY_NONCLASSED = Integer.parseInt(olympiad.getProperty("AltOlyNonClassedParticipants", "11"));
         ALT_OLY_TEAMS = Integer.parseInt(olympiad.getProperty("AltOlyTeamsParticipants", "6"));
         ALT_OLY_REG_DISPLAY = Integer.parseInt(olympiad.getProperty("AltOlyRegistrationDisplayNumber", "100"));
         ALT_OLY_CLASSED_REWARD = parseItemsList(olympiad.getProperty("AltOlyClassedReward", "13722,50"));
         ALT_OLY_NONCLASSED_REWARD = parseItemsList(olympiad.getProperty("AltOlyNonClassedReward", "13722,40"));
         ALT_OLY_TEAM_REWARD = parseItemsList(olympiad.getProperty("AltOlyTeamReward", "13722,85"));
         ALT_OLY_COMP_RITEM = Integer.parseInt(olympiad.getProperty("AltOlyCompRewItem", "13722"));
         ALT_OLY_MIN_MATCHES = Integer.parseInt(olympiad.getProperty("AltOlyMinMatchesForPoints", "15"));
         ALT_OLY_GP_PER_POINT = Integer.parseInt(olympiad.getProperty("AltOlyGPPerPoint", "1000"));
         ALT_OLY_HERO_POINTS = Integer.parseInt(olympiad.getProperty("AltOlyHeroPoints", "200"));
         ALT_OLY_RANK1_POINTS = Integer.parseInt(olympiad.getProperty("AltOlyRank1Points", "100"));
         ALT_OLY_RANK2_POINTS = Integer.parseInt(olympiad.getProperty("AltOlyRank2Points", "75"));
         ALT_OLY_RANK3_POINTS = Integer.parseInt(olympiad.getProperty("AltOlyRank3Points", "55"));
         ALT_OLY_RANK4_POINTS = Integer.parseInt(olympiad.getProperty("AltOlyRank4Points", "40"));
         ALT_OLY_RANK5_POINTS = Integer.parseInt(olympiad.getProperty("AltOlyRank5Points", "30"));
         ALT_OLY_MAX_POINTS = Integer.parseInt(olympiad.getProperty("AltOlyMaxPoints", "10"));
         ALT_OLY_DIVIDER_CLASSED = Integer.parseInt(olympiad.getProperty("AltOlyDividerClassed", "5"));
         ALT_OLY_DIVIDER_NON_CLASSED = Integer.parseInt(olympiad.getProperty("AltOlyDividerNonClassed", "5"));
         ALT_OLY_MAX_WEEKLY_MATCHES = Integer.parseInt(olympiad.getProperty("AltOlyMaxWeeklyMatches", "70"));
         ALT_OLY_MAX_WEEKLY_MATCHES_NON_CLASSED = Integer.parseInt(olympiad.getProperty("AltOlyMaxWeeklyMatchesNonClassed", "60"));
         ALT_OLY_MAX_WEEKLY_MATCHES_CLASSED = Integer.parseInt(olympiad.getProperty("AltOlyMaxWeeklyMatchesClassed", "30"));
         ALT_OLY_MAX_WEEKLY_MATCHES_TEAM = Integer.parseInt(olympiad.getProperty("AltOlyMaxWeeklyMatchesTeam", "10"));
         ALT_OLY_LOG_FIGHTS = Boolean.parseBoolean(olympiad.getProperty("AltOlyLogFights", "false"));
         ALT_OLY_SHOW_MONTHLY_WINNERS = Boolean.parseBoolean(olympiad.getProperty("AltOlyShowMonthlyWinners", "true"));
         ALT_OLY_ANNOUNCE_GAMES = Boolean.parseBoolean(olympiad.getProperty("AltOlyAnnounceGames", "true"));
         String[] split = olympiad.getProperty(
               "AltOlyRestrictedItems",
               "6611,6612,6613,6614,6615,6616,6617,6618,6619,6620,6621,9388,9389,9390,17049,17050,17051,17052,17053,17054,17055,17056,17057,17058,17059,17060,17061,20759,20775,20776,20777,20778,14774"
            )
            .split(",");
         LIST_OLY_RESTRICTED_ITEMS = new ArrayList<>(split.length);

         for(String id : split) {
            LIST_OLY_RESTRICTED_ITEMS.add(Integer.parseInt(id));
         }

         ALT_OLY_ENCHANT_LIMIT = Integer.parseInt(olympiad.getProperty("AltOlyEnchantLimit", "-1"));
         ALT_OLY_WAIT_TIME = Integer.parseInt(olympiad.getProperty("AltOlyWaitTime", "120"));
         BLOCK_VISUAL_OLY = Boolean.parseBoolean(olympiad.getProperty("AllowOlyBlockVisuals", "True"));
      } catch (Exception var7) {
         _log.warning("Config: " + var7.getMessage());
         throw new Error("Failed to Load ./config/main/olympiad.ini File.");
      }
   }

   private static void loadEpicsSettings(InputStream is) {
      try {
         GameSettings grandbossSettings = new GameSettings();
         InputStream var3 = new FileInputStream(new File("./config/main/grandboss.ini"));
         grandbossSettings.load(var3);
         ALLOW_DAMAGE_INFO = Boolean.parseBoolean(grandbossSettings.getProperty("AllowDamageInfo", "false"));
         DAMAGE_INFO_UPDATE = Integer.parseInt(grandbossSettings.getProperty("DamageInfoUpdateTime", "5"));
         DAMAGE_INFO_LIMIT_TIME = Integer.parseInt(grandbossSettings.getProperty("DamageInfoLimitTime", "5"));
         EPIDOS_POINTS_NEED = Integer.parseInt(grandbossSettings.getProperty("EpidosIndexLimit", "100"));
         CHANCE_SPAWN = Integer.parseInt(grandbossSettings.getProperty("ChanceSpawn", "50"));
         RESPAWN_TIME = Integer.parseInt(grandbossSettings.getProperty("RespawnTime", "4"));
         ANTHARAS_WAIT_TIME = Integer.parseInt(grandbossSettings.getProperty("AntharasWaitTime", "30"));
         ANTHARAS_RESPAWN_PATTERN = grandbossSettings.getProperty("AntharasRespawnPattern", "");
         VALAKAS_WAIT_TIME = Integer.parseInt(grandbossSettings.getProperty("ValakasWaitTime", "30"));
         VALAKAS_RESPAWN_PATTERN = grandbossSettings.getProperty("ValakasRespawnPattern", "");
         BAIUM_RESPAWN_PATTERN = grandbossSettings.getProperty("BaiumRespawnPattern", "");
         CORE_RESPAWN_PATTERN = grandbossSettings.getProperty("CoreRespawnPattern", "");
         ORFEN_RESPAWN_PATTERN = grandbossSettings.getProperty("OrfenRespawnPattern", "");
         QUEEN_ANT_RESPAWN_PATTERN = grandbossSettings.getProperty("QueenAntRespawnPattern", "");
         SAILREN_RESPAWN_PATTERN = grandbossSettings.getProperty("SailrenRespawnPattern", "");
         BELETH_RESPAWN_PATTERN = grandbossSettings.getProperty("BelethRespawnPattern", "");
         BELETH_MIN_PLAYERS = Integer.parseInt(grandbossSettings.getProperty("BelethMinPlayers", "36"));
         BELETH_SPAWN_DELAY = Integer.parseInt(grandbossSettings.getProperty("BelethSpawnDelay", "5"));
         BELETH_ZONE_CLEAN_DELAY = Integer.parseInt(grandbossSettings.getProperty("BelethZoneCleanUpDelay", "5"));
         BELETH_CLONES_RESPAWN = Integer.parseInt(grandbossSettings.getProperty("RespawnTimeClones", "60"));
         BELETH_NO_CC = Boolean.parseBoolean(grandbossSettings.getProperty("BelethNoCommandChannel", "False"));
      } catch (Exception var2) {
         _log.warning("Config: " + var2.getMessage());
         throw new Error("Failed to Load ./config/main/grandboss.ini File.");
      }
   }

   private static void loadGraciaSettings(InputStream is) {
      try {
         GameSettings graciaseedsSettings = new GameSettings();
         InputStream var3 = new FileInputStream(new File("./config/main/graciaSeeds.ini"));
         graciaseedsSettings.load(var3);
         SOD_TIAT_KILL_COUNT = Integer.parseInt(graciaseedsSettings.getProperty("TiatKillCountForNextState", "10"));
         SOD_STAGE_2_LENGTH = Long.parseLong(graciaseedsSettings.getProperty("Stage2Length", "720")) * 60000L;
         SOI_EKIMUS_KILL_COUNT = Integer.parseInt(graciaseedsSettings.getProperty("EkimusKillCount", "5"));
         MIN_EKIMUS_PLAYERS = Integer.parseInt(graciaseedsSettings.getProperty("MinEkimusPlayers", "18"));
         MAX_EKIMUS_PLAYERS = Integer.parseInt(graciaseedsSettings.getProperty("MaxEkimusPlayers", "27"));
      } catch (Exception var2) {
         _log.warning("Config: " + var2.getMessage());
         throw new Error("Failed to Load ./config/main/graciaSeeds.ini File.");
      }
   }

   private static void loadFilterSettings() {
      try {
         FILTER_LIST = new ArrayList<>();
         LineNumberReader lnr = new LineNumberReader(new BufferedReader(new FileReader(new File("./config/main/chatfilter.txt"))));
         String line = null;

         while((line = lnr.readLine()) != null) {
            if (!line.trim().isEmpty() && line.charAt(0) != '#') {
               FILTER_LIST.add(line.trim());
            }
         }

         _log.info("Loaded " + FILTER_LIST.size() + " Filter Words.");
      } catch (Exception var2) {
         _log.warning("Config: " + var2.getMessage());
         throw new Error("Failed to Load ./config/main/chatfilter.txt File.");
      }
   }

   private static void loadBroadCastFilterSettings() {
      try {
         BROADCAST_FILTER_LIST = new ArrayList<>();
         LineNumberReader lnr = new LineNumberReader(new BufferedReader(new FileReader(new File("./config/main/broadcastfilter.txt"))));
         String line = null;

         while((line = lnr.readLine()) != null) {
            if (!line.trim().isEmpty() && line.charAt(0) != '#') {
               BROADCAST_FILTER_LIST.add(line.trim());
            }
         }

         _log.info("Loaded " + BROADCAST_FILTER_LIST.size() + " BroadCast Filter Words.");
      } catch (Exception var2) {
         _log.warning("Config: " + var2.getMessage());
         throw new Error("Failed to Load ./config/main/broadcastfilter.txt File.");
      }
   }

   private static void loadClanhallSiegeSettings(InputStream is) {
      try {
         GameSettings chSiege = new GameSettings();
         InputStream var3 = new FileInputStream(new File("./config/main/clanhallSiege.ini"));
         chSiege.load(var3);
         CHS_MAX_ATTACKERS = Integer.parseInt(chSiege.getProperty("MaxAttackers", "500"));
         CHS_CLAN_MINLEVEL = Integer.parseInt(chSiege.getProperty("MinClanLevel", "4"));
         CHS_MAX_FLAGS_PER_CLAN = Integer.parseInt(chSiege.getProperty("MaxFlagsPerClan", "1"));
         CHS_ENABLE_FAME = Boolean.parseBoolean(chSiege.getProperty("EnableFame", "false"));
         CHS_FAME_AMOUNT = Integer.parseInt(chSiege.getProperty("FameAmount", "0"));
         CHS_FAME_FREQUENCY = Integer.parseInt(chSiege.getProperty("FameFrequency", "0"));
         CLAN_HALL_HWID_LIMIT = Integer.parseInt(chSiege.getProperty("ClanHallSiegeLimitPlayers", "0"));
      } catch (Exception var2) {
         _log.warning("Config: " + var2.getMessage());
         throw new Error("Failed to Load ./config/main/clanhallSiege.ini File.");
      }
   }

   private static void loadLanguageSettings(InputStream is) {
      try {
         GameSettings LanguageSettings = new GameSettings();
         InputStream var8 = new FileInputStream(new File("./config/main/language.ini"));
         LanguageSettings.load(var8);
         MULTILANG_ENABLE = Boolean.parseBoolean(LanguageSettings.getProperty("MultiLangEnable", "false"));
         String[] allowed = LanguageSettings.getProperty("MultiLangAllowed", "en").split(";");
         MULTILANG_ALLOWED = new ArrayList<>(allowed.length);

         for(String lang : allowed) {
            MULTILANG_ALLOWED.add(lang);
         }

         MULTILANG_DEFAULT = LanguageSettings.getProperty("MultiLangDefault", "en");
         if (!MULTILANG_ALLOWED.contains(MULTILANG_DEFAULT)) {
            _log.warning("Default language: " + MULTILANG_DEFAULT + " is not in allowed list!");
         }

         MULTILANG_VOICED_ALLOW = Boolean.parseBoolean(LanguageSettings.getProperty("MultiLangVoiceCommand", "True"));
      } catch (Exception var7) {
         _log.warning("Config: " + var7.getMessage());
         throw new Error("Failed to Load ./config/main/language.ini File.");
      }
   }

   private static void loadVoiceSettings(InputStream is) {
      try {
         GameSettings VoiceSettings = new GameSettings();
         InputStream var11 = new FileInputStream(new File("./config/main/voicecommands.ini"));
         VoiceSettings.load(var11);
         ALLOW_EXP_GAIN_COMMAND = Boolean.parseBoolean(VoiceSettings.getProperty("AllowExpGainCommand", "False"));
         ALLOW_AUTOLOOT_COMMAND = Boolean.parseBoolean(VoiceSettings.getProperty("AutoLootVoiceCommand", "False"));
         BANKING_SYSTEM_ENABLED = Boolean.parseBoolean(VoiceSettings.getProperty("BankingEnabled", "false"));
         BANKING_SYSTEM_GOLDBARS = Integer.parseInt(VoiceSettings.getProperty("BankingGoldbarCount", "1"));
         BANKING_SYSTEM_ADENA = Integer.parseInt(VoiceSettings.getProperty("BankingAdenaCount", "500000000"));
         CHAT_ADMIN = Boolean.parseBoolean(VoiceSettings.getProperty("ChatAdmin", "false"));
         HELLBOUND_STATUS = Boolean.parseBoolean(VoiceSettings.getProperty("HellboundStatus", "False"));
         DEBUG_VOICE_COMMAND = Boolean.parseBoolean(VoiceSettings.getProperty("DebugVoiceCommand", "False"));
         ALLOW_CHANGE_PASSWORD = Boolean.parseBoolean(VoiceSettings.getProperty("AllowChangePassword", "False"));
         VOICE_ONLINE_ENABLE = Boolean.parseBoolean(VoiceSettings.getProperty("OnlineEnable", "False"));
         FAKE_ONLINE = Double.parseDouble(VoiceSettings.getProperty("FakeOnline", "1.0"));
         ALLOW_TELETO_LEADER = Boolean.parseBoolean(VoiceSettings.getProperty("AllowTeletoLeader", "False"));
         TELETO_LEADER_ID = Integer.parseInt(VoiceSettings.getProperty("TeletoLeaderId", "57"));
         TELETO_LEADER_COUNT = Integer.parseInt(VoiceSettings.getProperty("TeletoLeaderCount", "1000"));
         ALLOW_REPAIR_COMMAND = Boolean.parseBoolean(VoiceSettings.getProperty("AllowRepairCommand", "False"));
         ALLOW_VISUAL_ARMOR_COMMAND = Boolean.parseBoolean(VoiceSettings.getProperty("AllowVisualArmorCommand", "False"));
         ALLOW_SEVENBOSSES_COMMAND = Boolean.parseBoolean(VoiceSettings.getProperty("AllowSevenBossesCommand", "False"));
         ALLOW_ANCIENT_EXCHANGER_COMMAND = Boolean.parseBoolean(VoiceSettings.getProperty("AllowAncientExchangerCommand", "False"));
         ALLOW_SELLBUFFS_COMMAND = Boolean.parseBoolean(VoiceSettings.getProperty("AllowSellBuffsCommand", "False"));
         SELLBUFF_USED_MP = Boolean.parseBoolean(VoiceSettings.getProperty("SellBuffsUseMp", "False"));
         String[] currecySplit = VoiceSettings.getProperty("SellBuffCurrecyList", "adena,57").split(";");
         SELLBUFF_CURRECY_LIST = new HashMap<>(currecySplit.length);

         for(String entry : currecySplit) {
            String[] entrySplit = entry.split(",");
            if (entrySplit.length != 2) {
               _log.warning("[Config.load()]: invalid config property -> SellBuffCurrecyList " + entry + "");
            } else {
               try {
                  SELLBUFF_CURRECY_LIST.put(entrySplit[0], Integer.parseInt(entrySplit[1]));
               } catch (Exception var9) {
                  _log.warning("[Config.load()]: invalid params!");
               }
            }
         }

         SELLBUFF_MIN_PRICE = Integer.parseInt(VoiceSettings.getProperty("SellBuffsMinPrice", "1"));
         SELLBUFF_MAX_PRICE = Integer.parseInt(VoiceSettings.getProperty("SellBuffsMaxPrice", "100000000"));
         SELLBUFF_MAX_BUFFS = Integer.parseInt(VoiceSettings.getProperty("SellBuffsMaxBuffs", "15"));
         ALLOW_SELLBUFFS_PETS = Boolean.parseBoolean(VoiceSettings.getProperty("AllowSellBuffSummons", "False"));
         ALLOW_STATS_COMMAND = Boolean.parseBoolean(VoiceSettings.getProperty("AllowStatsCommand", "False"));
         ALLOW_BLOCKBUFFS_COMMAND = Boolean.parseBoolean(VoiceSettings.getProperty("AllowBlockBuffsCommand", "False"));
         ALLOW_HIDE_TRADERS_COMMAND = Boolean.parseBoolean(VoiceSettings.getProperty("AllowHideTradersCommand", "False"));
         ALLOW_HIDE_BUFFS_ANIMATION_COMMAND = Boolean.parseBoolean(VoiceSettings.getProperty("AllowHideBuffsAnimCommand", "False"));
         ALLOW_BLOCK_TRADERS_COMMAND = Boolean.parseBoolean(VoiceSettings.getProperty("AllowBlockTradersCommand", "False"));
         ALLOW_BLOCK_PARTY_COMMAND = Boolean.parseBoolean(VoiceSettings.getProperty("AllowBlockPartyCommand", "False"));
         ALLOW_BLOCK_FRIEND_COMMAND = Boolean.parseBoolean(VoiceSettings.getProperty("AllowBlockFriendCommand", "False"));
         ALLOW_MENU_COMMAND = Boolean.parseBoolean(VoiceSettings.getProperty("AllowMenuCommand", "False"));
         ALLOW_SECURITY_COMMAND = Boolean.parseBoolean(VoiceSettings.getProperty("AllowSecurityCommand", "False"));
         ALLOW_IP_LOCK = Boolean.parseBoolean(VoiceSettings.getProperty("AllowIpLock", "False"));
         ALLOW_HWID_LOCK = Boolean.parseBoolean(VoiceSettings.getProperty("AllowHwidLock", "False"));
         PUNISH_LOCK_DELAY = Integer.parseInt(VoiceSettings.getProperty("KickPlayerDelay", "5"));
         ALLOW_FIND_PARTY = Boolean.parseBoolean(VoiceSettings.getProperty("AllowFindPartyCommand", "False"));
         PARTY_LEADER_ONLY_CAN_INVITE = Boolean.parseBoolean(VoiceSettings.getProperty("AllowLeaderOnlyCanInvite", "False"));
         FIND_PARTY_REFRESH_TIME = Integer.parseInt(VoiceSettings.getProperty("FindPartyRefreshTime", "600"));
         FIND_PARTY_FLOOD_TIME = Integer.parseInt(VoiceSettings.getProperty("FindPartyFloodTime", "60"));
         FIND_PARTY_MIN_LEVEL = Integer.parseInt(VoiceSettings.getProperty("FindPartyMinLevel", "40"));
         ALLOW_ENCHANT_SERVICE = Boolean.parseBoolean(VoiceSettings.getProperty("AllowEnchantService", "False"));
         ENCHANT_SERVICE_ONLY_FOR_PREMIUM = Boolean.parseBoolean(VoiceSettings.getProperty("EnchantServiceOnlyForPremium", "False"));
         ENCHANT_ALLOW_BELTS = Boolean.parseBoolean(VoiceSettings.getProperty("EnchantServiceAllowBelts", "False"));
         ENCHANT_ALLOW_SCROLLS = Boolean.parseBoolean(VoiceSettings.getProperty("EnchantServiceScrollsEnable", "False"));
         ENCHANT_ALLOW_ATTRIBUTE = Boolean.parseBoolean(VoiceSettings.getProperty("EnchantServiceAttributeEnable", "False"));
         ENCHANT_MAX_WEAPON = Integer.parseInt(VoiceSettings.getProperty("EnchantServiceMaxWeapon", "20"));
         ENCHANT_MAX_ARMOR = Integer.parseInt(VoiceSettings.getProperty("EnchantServiceMaxArmor", "20"));
         ENCHANT_MAX_JEWELRY = Integer.parseInt(VoiceSettings.getProperty("EnchantServiceMaxJewelry", "20"));
         ENCHANT_MAX_ITEM_LIMIT = Integer.parseInt(VoiceSettings.getProperty("EnchantServiceMaxItemLimit", "40"));
         ENCHANT_CONSUME_ITEM = Integer.parseInt(VoiceSettings.getProperty("EnchantServiceConsumeItem", "57"));
         ENCHANT_CONSUME_ITEM_COUNT = Integer.parseInt(VoiceSettings.getProperty("EnchantServiceConsumeItemCount", "1000"));
         enchantServiceDefaultLimit = Integer.parseInt(VoiceSettings.getProperty("EnchantServiceDefaultLimit", "40"));
         enchantServiceDefaultEnchant = Integer.parseInt(VoiceSettings.getProperty("EnchantServiceDefaultEnchant", "20"));
         enchantServiceDefaultAttribute = Integer.parseInt(VoiceSettings.getProperty("EnchantServiceDefaultAttribute", "120"));
         ENCHANT_SCROLL_CHANCE_CORRECT = Integer.parseInt(VoiceSettings.getProperty("EnchantServiceScrollChanceCorrect", "0"));
         ALLOW_RELOG_COMMAND = Boolean.parseBoolean(VoiceSettings.getProperty("AllowRelogCommand", "False"));
         ALLOW_PARTY_RANK_COMMAND = Boolean.parseBoolean(VoiceSettings.getProperty("AllowPartyRankCommand", "False"));
         ALLOW_PARTY_RANK_ONLY_FOR_CC = Boolean.parseBoolean(VoiceSettings.getProperty("PartyRankOnlyForCC", "False"));
         PARTY_RANK_AUTO_OPEN = Boolean.parseBoolean(VoiceSettings.getProperty("PartyRankAutoOpenWindow", "False"));
         ALLOW_RECOVERY_ITEMS = Boolean.parseBoolean(VoiceSettings.getProperty("AllowRecoveryItemCommand", "False"));
         RECOVERY_ITEMS_HOURS = Integer.parseInt(VoiceSettings.getProperty("RecoveryItemHours", "24"));
         ALLOW_PROMOCODES_COMMAND = Boolean.parseBoolean(VoiceSettings.getProperty("AllowPromoCodesCommand", "False"));
         PROMOCODES_USE_DELAY = Integer.parseInt(VoiceSettings.getProperty("PromoCodesDelay", "60"));
      } catch (Exception var10) {
         _log.warning("Config: " + var10.getMessage());
         throw new Error("Failed to Load ./config/main/voicecommands.ini File.");
      }
   }

   private static void loadCustomSettings(InputStream is) {
      try {
         GameSettings customSettings = new GameSettings();
         InputStream var14 = new FileInputStream(new File("./config/main/custom.ini"));
         customSettings.load(var14);
         ALLOW_CUSTOM_INTERFACE = Boolean.parseBoolean(customSettings.getProperty("AllowCustomInterface", "False"));
         CUSTOM_INTERFACE_VERSION_OLD = Boolean.parseBoolean(customSettings.getProperty("IsInterfaceOldVersion", "False"));
         SERVER_NAME = customSettings.getProperty("ServerName", "L2J Eternity-World Server");
         ENABLE_MANA_POTIONS_SUPPORT = Boolean.parseBoolean(customSettings.getProperty("EnableManaPotionSupport", "false"));
         DISPLAY_SERVER_TIME = Boolean.parseBoolean(customSettings.getProperty("DisplayServerTime", "false"));
         ENABLE_WAREHOUSESORTING_CLAN = Boolean.parseBoolean(customSettings.getProperty("EnableWarehouseSortingClan", "False"));
         ENABLE_WAREHOUSESORTING_PRIVATE = Boolean.parseBoolean(customSettings.getProperty("EnableWarehouseSortingPrivate", "False"));
         WELCOME_MESSAGE_ENABLED = Boolean.parseBoolean(customSettings.getProperty("ScreenWelcomeMessageEnable", "false"));
         WELCOME_MESSAGE_TEXT = customSettings.getProperty("ScreenWelcomeMessageText", "Welcome to L2J server!");
         WELCOME_MESSAGE_TIME = Integer.parseInt(customSettings.getProperty("ScreenWelcomeMessageTime", "10")) * 1000;
         ANNOUNCE_PK_PVP = Boolean.parseBoolean(customSettings.getProperty("AnnouncePkPvP", "False"));
         ANNOUNCE_PK_PVP_NORMAL_MESSAGE = Boolean.parseBoolean(customSettings.getProperty("AnnouncePkPvPNormalMessage", "True"));
         ANNOUNCE_PK_MSG = customSettings.getProperty("AnnouncePkMsg", "$killer has slaughtered $target");
         ANNOUNCE_PVP_MSG = customSettings.getProperty("AnnouncePvpMsg", "$killer has defeated $target");
         ONLINE_PLAYERS_AT_STARTUP = Boolean.parseBoolean(customSettings.getProperty("ShowOnlinePlayersAtStartup", "True"));
         ONLINE_PLAYERS_ANNOUNCE_INTERVAL = Integer.parseInt(customSettings.getProperty("OnlinePlayersAnnounceInterval", "900000"));
         ALLOW_NEW_CHARACTER_TITLE = Boolean.parseBoolean(customSettings.getProperty("AllowNewCharacterTitle", "False"));
         NEW_CHARACTER_TITLE = customSettings.getProperty("NewCharacterTitle", "Newbie");
         NEW_CHAR_IS_NOBLE = Boolean.parseBoolean(customSettings.getProperty("NewCharIsNoble", "False"));
         NEW_CHAR_IS_HERO = Boolean.parseBoolean(customSettings.getProperty("NewCharIsHero", "False"));
         UNSTUCK_SKILL = Boolean.parseBoolean(customSettings.getProperty("UnstuckSkill", "False"));
         ALLOW_NEW_CHAR_CUSTOM_POSITION = Boolean.parseBoolean(customSettings.getProperty("AltSpawnNewChar", "False"));
         NEW_CHAR_POSITION_X = Integer.parseInt(customSettings.getProperty("AltSpawnX", "0"));
         NEW_CHAR_POSITION_Y = Integer.parseInt(customSettings.getProperty("AltSpawnY", "0"));
         NEW_CHAR_POSITION_Z = Integer.parseInt(customSettings.getProperty("AltSpawnZ", "0"));
         ENABLE_NOBLESS_COLOR = Boolean.parseBoolean(customSettings.getProperty("EnableNoblessColor", "False"));
         NOBLESS_COLOR_NAME = Integer.decode("0x" + customSettings.getProperty("NoblessColorName", "000000"));
         ENABLE_NOBLESS_TITLE_COLOR = Boolean.parseBoolean(customSettings.getProperty("EnableNoblessTitleColor", "False"));
         NOBLESS_COLOR_TITLE_NAME = Integer.decode("0x" + customSettings.getProperty("NoblessColorTitleName", "000000"));
         INFINITE_SOUL_SHOT = Boolean.parseBoolean(customSettings.getProperty("InfiniteSoulShot", "False"));
         INFINITE_BEAST_SOUL_SHOT = Boolean.parseBoolean(customSettings.getProperty("InfiniteBeastSoulShot", "False"));
         INFINITE_BEAST_SPIRIT_SHOT = Boolean.parseBoolean(customSettings.getProperty("InfiniteBeastSpiritShot", "False"));
         INFINITE_SPIRIT_SHOT = Boolean.parseBoolean(customSettings.getProperty("InfiniteSpiritShot", "False"));
         INFINITE_BLESSED_SPIRIT_SHOT = Boolean.parseBoolean(customSettings.getProperty("InfiniteBlessedSpiritShot", "False"));
         INFINITE_ARROWS = Boolean.parseBoolean(customSettings.getProperty("InfiniteArrows", "False"));
         ENTER_HELLBOUND_WITHOUT_QUEST = Boolean.parseBoolean(customSettings.getProperty("EnterHellBoundWithoutQuest", "False"));
         AUTO_RESTART_TIME = Integer.parseInt(customSettings.getProperty("AutoRestartSeconds", "360"));
         AUTO_RESTART_PATTERN = customSettings.getProperty("AutoRestartPattern", "* * * * *");
         SPEED_UP_RUN = Boolean.parseBoolean(customSettings.getProperty("SpeedUpRunInTown", "False"));
         DISCONNECT_SYSTEM_ENABLED = Boolean.parseBoolean(customSettings.getProperty("DisconnectSystemEnable", "False"));
         DISCONNECT_TIMEOUT = Integer.parseInt(customSettings.getProperty("DisconnectTimeout", "15"));
         DISCONNECT_TITLECOLOR = customSettings.getProperty("DisconnectColorTitle", "FF0000");
         DISCONNECT_TITLE = customSettings.getProperty("DisconnectTitle", "[NO CARRIER]");
         CUSTOM_ENCHANT_ITEMS_ENABLED = Boolean.parseBoolean(customSettings.getProperty("CustomEnchantSystemEnable", "False"));
         String[] propertySplit = customSettings.getProperty("CustomEnchantItemsById", "").split(";");
         ENCHANT_ITEMS_ID = new HashMap<>(propertySplit.length);
         if (!propertySplit[0].isEmpty()) {
            for(String item : propertySplit) {
               String[] itemSplit = item.split(",");
               if (itemSplit.length != 2) {
                  _log.warning("Config.load(): invalid config property -> CustomEnchantItemsById " + item + "");
               } else {
                  try {
                     ENCHANT_ITEMS_ID.put(Integer.valueOf(itemSplit[0]), Float.valueOf(itemSplit[1]));
                  } catch (NumberFormatException var12) {
                     if (!item.isEmpty()) {
                        _log.warning("Config.load(): invalid config property -> CustomEnchantItemsById " + item + "");
                     }
                  }
               }
            }
         }

         ALLOW_UNLIM_ENTER_CATACOMBS = Boolean.parseBoolean(customSettings.getProperty("AllowUnlimEnterCatacombs", "False"));
         AUTO_POINTS_SYSTEM = Boolean.parseBoolean(customSettings.getProperty("AllowAutoPotions", "False"));
         String[] hp = customSettings.getProperty("ListOfValidHpPotions", "0").split(",");
         AUTO_HP_VALID_ITEMS = new ArrayList<>(hp.length);

         for(String id : hp) {
            AUTO_HP_VALID_ITEMS.add(Integer.parseInt(id));
         }

         String[] mp = customSettings.getProperty("ListOfValidMpPotions", "0").split(",");
         AUTO_MP_VALID_ITEMS = new ArrayList<>(mp.length);

         for(String id : mp) {
            AUTO_MP_VALID_ITEMS.add(Integer.parseInt(id));
         }

         String[] cp = customSettings.getProperty("ListOfValidCpPotions", "0").split(",");
         AUTO_CP_VALID_ITEMS = new ArrayList<>(cp.length);

         for(String id : cp) {
            AUTO_CP_VALID_ITEMS.add(Integer.parseInt(id));
         }

         String[] soul = customSettings.getProperty("ListOfValidSoulPotions", "0").split(",");
         AUTO_SOUL_VALID_ITEMS = new ArrayList<>(soul.length);

         for(String id : soul) {
            AUTO_SOUL_VALID_ITEMS.add(Integer.parseInt(id));
         }

         DEFAULT_HP_PERCENT = Integer.parseInt(customSettings.getProperty("DefaultHpPercent", "70"));
         DEFAULT_MP_PERCENT = Integer.parseInt(customSettings.getProperty("DefaultMpPercent", "60"));
         DEFAULT_CP_PERCENT = Integer.parseInt(customSettings.getProperty("DefaultCpPercent", "90"));
         DEFAULT_SOUL_AMOUNT = Integer.parseInt(customSettings.getProperty("DefaultSoulAmount", "10"));
         DISABLE_WITHOUT_POTIONS = Boolean.parseBoolean(customSettings.getProperty("DisableWithOutPotions", "True"));
         SELL_PRICE_MODIFIER = Double.parseDouble(customSettings.getProperty("SellPriceModifier", "1.0"));
         ALT_KAMALOKA_SOLO_PREMIUM_ONLY = Boolean.parseBoolean(customSettings.getProperty("SoloKamalokaPremiumOnly", "True"));
         ALT_KAMALOKA_ESSENCE_PREMIUM_ONLY = Boolean.parseBoolean(customSettings.getProperty("KamalokaEssencePremiumOnly", "True"));
         ITEM_BROKER_ITEM_SEARCH = Boolean.parseBoolean(customSettings.getProperty("AllowItemBrokerItemSearch", "False"));
         ITEM_BROKER_ITEMS_PER_PAGE = Integer.parseInt(customSettings.getProperty("ItemBrokerItemsPerPage", "10"));
         ITEM_BROKER_TIME_UPDATE = Long.parseLong(customSettings.getProperty("ItemBrokerUpdateTime", "30"));
         ALLOW_BLOCK_TRANSFORMS_AT_SIEGE = Boolean.parseBoolean(customSettings.getProperty("AllowBlockTransformAtSiege", "False"));
         String[] transform = customSettings.getProperty("BlockTransformationList", "0,0").split(",");
         LIST_BLOCK_TRANSFORMS_AT_SIEGE = new ArrayList<>(transform.length);

         for(String id : transform) {
            LIST_BLOCK_TRANSFORMS_AT_SIEGE.add(Integer.parseInt(id));
         }

         ALLOW_AUTO_FISH_SHOTS = Boolean.parseBoolean(customSettings.getProperty("AllowAutoFishShots", "False"));
      } catch (Exception var13) {
         _log.warning("Config: " + var13.getMessage());
         throw new Error("Failed to Load ./config/main/custom.ini File.");
      }
   }

   private static void loadPcBangSettings(InputStream is) {
      try {
         GameSettings pccaffeSettings = new GameSettings();
         InputStream var3 = new FileInputStream(new File("./config/mods/pcPoints.ini"));
         pccaffeSettings.load(var3);
         PC_BANG_ENABLED = Boolean.parseBoolean(pccaffeSettings.getProperty("PcBangPointEnable", "false"));
         PC_BANG_ONLY_FOR_PREMIUM = Boolean.parseBoolean(pccaffeSettings.getProperty("PcBangPointOnlyForPremium", "false"));
         PC_POINT_ID = Integer.parseInt(pccaffeSettings.getProperty("PcBangPointId", "-100"));
         MAX_PC_BANG_POINTS = Integer.parseInt(pccaffeSettings.getProperty("MaxPcBangPoints", "200000"));
         if (MAX_PC_BANG_POINTS < 0) {
            MAX_PC_BANG_POINTS = 0;
         }

         ENABLE_DOUBLE_PC_BANG_POINTS = Boolean.parseBoolean(pccaffeSettings.getProperty("DoublingAcquisitionPoints", "false"));
         DOUBLE_PC_BANG_POINTS_CHANCE = Integer.parseInt(pccaffeSettings.getProperty("DoublingAcquisitionPointsChance", "1"));
         if (DOUBLE_PC_BANG_POINTS_CHANCE < 0 || DOUBLE_PC_BANG_POINTS_CHANCE > 100) {
            DOUBLE_PC_BANG_POINTS_CHANCE = 1;
         }

         PC_BANG_MIN_LEVEL = Integer.parseInt(pccaffeSettings.getProperty("PcBangPointMinLevel", "20"));
         PC_BANG_POINTS_MIN = Integer.parseInt(pccaffeSettings.getProperty("PcBangPointMinCount", "20"));
         PC_BANG_POINTS_PREMIUM_MIN = Integer.parseInt(pccaffeSettings.getProperty("PcBangPointPremiumMinCount", "20"));
         PC_BANG_POINTS_MAX = Integer.parseInt(pccaffeSettings.getProperty("PcBangPointMaxCount", "30"));
         PC_BANG_POINTS_PREMIUM_MAX = Integer.parseInt(pccaffeSettings.getProperty("PcBangPointPremiumMaxCount", "30"));
         PC_BANG_INTERVAL = Integer.parseInt(pccaffeSettings.getProperty("PcBangPointIntervalTime", "900"));
      } catch (Exception var2) {
         _log.warning("Config: " + var2.getMessage());
         throw new Error("Failed to Load ./config/mods/pcPoints.ini File.");
      }
   }

   private static void loadPremiumSettings(InputStream is) {
      try {
         GameSettings premium = new GameSettings();
         InputStream var3 = new FileInputStream(new File("./config/main/premiumAccount.ini"));
         premium.load(var3);
         USE_PREMIUMSERVICE = Boolean.parseBoolean(premium.getProperty("UsePremiumServices", "False"));
         PREMIUMSERVICE_DOUBLE = Boolean.parseBoolean(premium.getProperty("AllowBuyPremiumOver", "False"));
         AUTO_GIVE_PREMIUM = Boolean.parseBoolean(premium.getProperty("AutoGivePremium", "False"));
         GIVE_PREMIUM_ID = Integer.parseInt(premium.getProperty("GivePremiumId", "1"));
         PREMIUM_PARTY_RATE = Boolean.parseBoolean(premium.getProperty("AllowPremiumPartyRate", "False"));
      } catch (Exception var2) {
         _log.warning("Config: " + var2.getMessage());
         throw new Error("Failed to Load ./config/main/premiumAccount.ini File.");
      }
   }

   private static void loadCommunitySettings(InputStream is) {
      try {
         GameSettings CommunityBoardSettings = new GameSettings();
         InputStream var111 = new FileInputStream(new File("./config/main/communityBoard.ini"));
         CommunityBoardSettings.load(var111);
         ALLOW_COMMUNITY = Boolean.parseBoolean(CommunityBoardSettings.getProperty("AllowCommunity", "True"));
         BLOCK_COMMUNITY_IN_PVP_ZONE = Boolean.parseBoolean(CommunityBoardSettings.getProperty("BlockCommunityAtPvpZones", "False"));
         String[] props = CommunityBoardSettings.getProperty("DisableBypassList", "").split(",");
         DISABLE_COMMUNITY_BYPASSES = new ArrayList<>(props.length);
         if (props.length != 0) {
            for(String name : props) {
               DISABLE_COMMUNITY_BYPASSES.add(name);
            }
         }

         ALLOW_SENDING_IMAGES = Boolean.parseBoolean(CommunityBoardSettings.getProperty("AllowSendingImages", "True"));
         BBS_HOME_PAGE = CommunityBoardSettings.getProperty("bbshome", "_bbshome");
         BBS_FAVORITE_PAGE = CommunityBoardSettings.getProperty("bbsgetfav", "_bbsgetfav");
         BBS_LINK_PAGE = CommunityBoardSettings.getProperty("bbslink", "_bbslink");
         BBS_REGION_PAGE = CommunityBoardSettings.getProperty("bbsloc", "_bbsloc");
         BBS_CLAN_PAGE = CommunityBoardSettings.getProperty("bbsclan", "_bbsclan");
         BBS_MEMO_PAGE = CommunityBoardSettings.getProperty("bbsmemo", "_bbsmemo");
         BBS_MAIL_PAGE = CommunityBoardSettings.getProperty("bbsmail", "_maillist_0_1_0_");
         BBS_FRIENDS_PAGE = CommunityBoardSettings.getProperty("bbsfriends", "_friendlist_0_");
         BBS_ADDFAV_PAGE = CommunityBoardSettings.getProperty("bbsAddFav", "");
         ALLOW_COMMUNITY_PEACE_ZONE = Boolean.parseBoolean(CommunityBoardSettings.getProperty("AllowCommunityPeaceZone", "False"));
         String[] multisells = CommunityBoardSettings.getProperty("AvaliableMultiSellList", "0").split(",");
         AVALIABLE_COMMUNITY_MULTISELLS = new ArrayList<>(multisells.length);

         for(String id : multisells) {
            AVALIABLE_COMMUNITY_MULTISELLS.add(Integer.parseInt(id));
         }

         INTERVAL_STATS_UPDATE = Integer.parseInt(CommunityBoardSettings.getProperty("IntervalStatsUpdate", "60"));
         ALLOW_COMMUNITY_COORDS_TP = Boolean.parseBoolean(CommunityBoardSettings.getProperty("AllowTeleportByCoords", "False"));
         ALLOW_COMMUNITY_TP_NO_RESTART_ZONES = Boolean.parseBoolean(CommunityBoardSettings.getProperty("AllowTeleportByNoRestartZones", "False"));
         ALLOW_COMMUNITY_TP_SIEGE_ZONES = Boolean.parseBoolean(CommunityBoardSettings.getProperty("AllowTeleportBySiegeZones", "False"));
         COMMUNITY_TELEPORT_TABS = Integer.parseInt(CommunityBoardSettings.getProperty("CommunityTeleportTabsLimit", "5"));
         ALLOW_COMMUNITY_BUFF_IN_SIEGE = Boolean.parseBoolean(CommunityBoardSettings.getProperty("AllowCommunityBuffInSiege", "False"));
         ALLOW_COMMUNITY_TELEPORT_IN_SIEGE = Boolean.parseBoolean(CommunityBoardSettings.getProperty("AllowCommunityTeleportInSiege", "False"));
         BLOCK_TP_AT_SIEGES_FOR_ALL = Boolean.parseBoolean(CommunityBoardSettings.getProperty("BlockCommunityPersonalTpInSieges", "False"));
         ALLOW_BUFF_PEACE_ZONE = Boolean.parseBoolean(CommunityBoardSettings.getProperty("AllowBufferInPeaceZone", "False"));
         ALLOW_BUFF_WITHOUT_PEACE_FOR_PREMIUM = Boolean.parseBoolean(CommunityBoardSettings.getProperty("CanUsePremiumInPeaceZone", "False"));
         FREE_ALL_BUFFS = Boolean.parseBoolean(CommunityBoardSettings.getProperty("AllowFreeAllBuffs", "False"));
         ALLOW_SCHEMES_FOR_PREMIUMS = Boolean.parseBoolean(CommunityBoardSettings.getProperty("AllowSchemesForPremium", "False"));
         ALLOW_HEAL_ONLY_PEACE = Boolean.parseBoolean(CommunityBoardSettings.getProperty("AllowHealOnlyPeaceZone", "False"));
         BUFF_ID_ITEM = Integer.parseInt(CommunityBoardSettings.getProperty("BuffPriceId", "57"));
         BUFF_AMOUNT = Integer.parseInt(CommunityBoardSettings.getProperty("BuffPriceAmount", "10000"));
         CANCEL_BUFF_AMOUNT = Integer.parseInt(CommunityBoardSettings.getProperty("CancelBuffPriceAmount", "10000"));
         HPMPCP_BUFF_AMOUNT = Integer.parseInt(CommunityBoardSettings.getProperty("RecoveryPriceAmount", "10000"));
         SET_BUFF_AMOUNT = Integer.parseInt(CommunityBoardSettings.getProperty("SetBuffsPriceAmount", "10000"));
         BUFF_MAX_SCHEMES = Integer.parseInt(CommunityBoardSettings.getProperty("BuffMaxSchemesPerChar", "4"));
         SERVICES_LEVELUP_ENABLE = Boolean.parseBoolean(CommunityBoardSettings.getProperty("AllowLevelUpService", "false"));
         SERVICES_DELEVEL_ENABLE = Boolean.parseBoolean(CommunityBoardSettings.getProperty("AllowDeLevelService", "false"));
         String[] propertylevelUp = CommunityBoardSettings.getProperty("LevelUpPrice", "4037,10").split(",");

         try {
            SERVICES_LEVELUP_ITEM[0] = Integer.parseInt(propertylevelUp[0]);
            SERVICES_LEVELUP_ITEM[1] = Integer.parseInt(propertylevelUp[1]);
         } catch (NumberFormatException var109) {
            if (propertylevelUp.length > 0) {
               _log.warning("[Config.load()]: invalid config property -> LevelUpPrice");
            }
         }

         String[] propertyDelevel = CommunityBoardSettings.getProperty("DelevelPrice", "4037,10").split(",");

         try {
            SERVICES_DELEVEL_ITEM[0] = Integer.parseInt(propertyDelevel[0]);
            SERVICES_DELEVEL_ITEM[1] = Integer.parseInt(propertyDelevel[1]);
         } catch (NumberFormatException var108) {
            if (propertyDelevel.length > 0) {
               _log.warning("[Config.load()]: invalid config property -> DelevelPrice");
            }
         }

         String[] propertyNoobless = CommunityBoardSettings.getProperty("GiveNooblessPrice", "4037,30").split(",");

         try {
            SERVICES_GIVENOOBLESS_ITEM[0] = Integer.parseInt(propertyNoobless[0]);
            SERVICES_GIVENOOBLESS_ITEM[1] = Integer.parseInt(propertyNoobless[1]);
         } catch (NumberFormatException var107) {
            if (propertyNoobless.length > 0) {
               _log.warning("[Config.load()]: invalid config property -> GiveNooblessPrice");
            }
         }

         String[] propertyGender = CommunityBoardSettings.getProperty("ChangeGenderPrice", "4037,30").split(",");

         try {
            SERVICES_CHANGEGENDER_ITEM[0] = Integer.parseInt(propertyGender[0]);
            SERVICES_CHANGEGENDER_ITEM[1] = Integer.parseInt(propertyGender[1]);
         } catch (NumberFormatException var106) {
            if (propertyGender.length > 0) {
               _log.warning("[Config.load()]: invalid config property -> ChangeGenderPrice");
            }
         }

         String[] propertyGiveHero = CommunityBoardSettings.getProperty("GiveHeroPrice", "4037,100").split(",");

         try {
            SERVICES_GIVEHERO_ITEM[0] = Integer.parseInt(propertyGiveHero[0]);
            SERVICES_GIVEHERO_ITEM[1] = Integer.parseInt(propertyGiveHero[1]);
         } catch (NumberFormatException var105) {
            if (propertyGiveHero.length > 0) {
               _log.warning("[Config.load()]: invalid config property -> GiveHeroPrice");
            }
         }

         SERVICES_GIVEHERO_TIME = Integer.parseInt(CommunityBoardSettings.getProperty("GiveHeroTime", "60"));
         String[] propertyRecPK = CommunityBoardSettings.getProperty("RecoveryPkPrice", "4037,10").split(",");

         try {
            SERVICES_RECOVERYPK_ITEM[0] = Integer.parseInt(propertyRecPK[0]);
            SERVICES_RECOVERYPK_ITEM[1] = Integer.parseInt(propertyRecPK[1]);
         } catch (NumberFormatException var104) {
            if (propertyRecPK.length > 0) {
               _log.warning("[Config.load()]: invalid config property -> RecoveryPkPrice");
            }
         }

         String[] propertyRecKarma = CommunityBoardSettings.getProperty("RecoveryKarmaPrice", "4037,10").split(",");

         try {
            SERVICES_RECOVERYKARMA_ITEM[0] = Integer.parseInt(propertyRecKarma[0]);
            SERVICES_RECOVERYKARMA_ITEM[1] = Integer.parseInt(propertyRecKarma[1]);
         } catch (NumberFormatException var103) {
            if (propertyRecKarma.length > 0) {
               _log.warning("[Config.load()]: invalid config property -> RecoveryKarmaPrice");
            }
         }

         String[] propertyVitality = CommunityBoardSettings.getProperty("RecoveryVitalityPrice", "4037,10").split(",");

         try {
            SERVICES_RECOVERYVITALITY_ITEM[0] = Integer.parseInt(propertyVitality[0]);
            SERVICES_RECOVERYVITALITY_ITEM[1] = Integer.parseInt(propertyVitality[1]);
         } catch (NumberFormatException var102) {
            if (propertyVitality.length > 0) {
               _log.warning("[Config.load()]: invalid config property -> RecoveryVitalityPrice");
            }
         }

         String[] propertyGiveSP = CommunityBoardSettings.getProperty("GiveSpPrice", "4037,10").split(",");

         try {
            SERVICES_GIVESP_ITEM[0] = Integer.parseInt(propertyGiveSP[0]);
            SERVICES_GIVESP_ITEM[1] = Integer.parseInt(propertyGiveSP[1]);
         } catch (NumberFormatException var101) {
            if (propertyGiveSP.length > 0) {
               _log.warning("[Config.load()]: invalid config property -> GiveSpPrice");
            }
         }

         String[] propertyNameChange = CommunityBoardSettings.getProperty("NameChangePrice", "4037,100").split(",");

         try {
            SERVICES_NAMECHANGE_ITEM[0] = Integer.parseInt(propertyNameChange[0]);
            SERVICES_NAMECHANGE_ITEM[1] = Integer.parseInt(propertyNameChange[1]);
         } catch (NumberFormatException var100) {
            if (propertyNameChange.length > 0) {
               _log.warning("[Config.load()]: invalid config property -> NameChangePrice");
            }
         }

         SERVICES_NAMECHANGE_TEMPLATE = CommunityBoardSettings.getProperty("ChangeNameTemplate", ".*");
         String[] propertyClanNameChange = CommunityBoardSettings.getProperty("ClanNameChangePrice", "4037,100").split(",");

         try {
            SERVICES_CLANNAMECHANGE_ITEM[0] = Integer.parseInt(propertyClanNameChange[0]);
            SERVICES_CLANNAMECHANGE_ITEM[1] = Integer.parseInt(propertyClanNameChange[1]);
         } catch (NumberFormatException var99) {
            if (propertyClanNameChange.length > 0) {
               _log.warning("[Config.load()]: invalid config property -> ClanNameChangePrice");
            }
         }

         String[] propertySplit = CommunityBoardSettings.getProperty("UnbanPrice", "4037,100").split(",");

         try {
            SERVICES_UNBAN_ITEM[0] = Integer.parseInt(propertySplit[0]);
            SERVICES_UNBAN_ITEM[1] = Integer.parseInt(propertySplit[1]);
         } catch (NumberFormatException var98) {
            if (propertySplit.length > 0) {
               _log.warning("[Config.load()]: invalid config property -> UnbanPrice");
            }
         }

         String[] propertyclan = CommunityBoardSettings.getProperty("ClanLvlUpPrice", "4037,10").split(",");

         try {
            SERVICES_CLANLVL_ITEM[0] = Integer.parseInt(propertyclan[0]);
            SERVICES_CLANLVL_ITEM[1] = Integer.parseInt(propertyclan[1]);
         } catch (NumberFormatException var97) {
            if (propertyclan.length > 0) {
               _log.warning("[Config.load()]: invalid config property -> ClanLvlUpPrice");
            }
         }

         String[] propertyclanskills = CommunityBoardSettings.getProperty("ClanSkillsPrice", "4037,10").split(",");

         try {
            SERVICES_CLANSKILLS_ITEM[0] = Integer.parseInt(propertyclanskills[0]);
            SERVICES_CLANSKILLS_ITEM[1] = Integer.parseInt(propertyclanskills[1]);
         } catch (NumberFormatException var96) {
            if (propertyclanskills.length > 0) {
               _log.warning("[Config.load()]: invalid config property -> ClanSkillsPrice");
            }
         }

         String[] propertyGiveRec = CommunityBoardSettings.getProperty("GiveRecPrice", "4037,50").split(",");

         try {
            SERVICES_GIVEREC_ITEM[0] = Integer.parseInt(propertyGiveRec[0]);
            SERVICES_GIVEREC_ITEM[1] = Integer.parseInt(propertyGiveRec[1]);
         } catch (NumberFormatException var95) {
            if (propertyGiveRec.length > 0) {
               _log.warning("[Config.load()]: invalid config property -> GiveRecPrice");
            }
         }

         SERVICES_REP_COUNT = Integer.parseInt(CommunityBoardSettings.getProperty("ReputationCount", "40000"));
         String[] propertyGiveRep = CommunityBoardSettings.getProperty("GiveRepPrice", "4037,30").split(",");

         try {
            SERVICES_GIVEREP_ITEM[0] = Integer.parseInt(propertyGiveRep[0]);
            SERVICES_GIVEREP_ITEM[1] = Integer.parseInt(propertyGiveRep[1]);
         } catch (NumberFormatException var94) {
            if (propertyGiveRep.length > 0) {
               _log.warning("[Config.load()]: invalid config property -> GiveRepPrice");
            }
         }

         SERVICES_FAME_COUNT = Integer.parseInt(CommunityBoardSettings.getProperty("FameCount", "15000"));
         String[] propertyGiveFame = CommunityBoardSettings.getProperty("GiveFamePrice", "4037,30").split(",");

         try {
            SERVICES_GIVEFAME_ITEM[0] = Integer.parseInt(propertyGiveFame[0]);
            SERVICES_GIVEFAME_ITEM[1] = Integer.parseInt(propertyGiveFame[1]);
         } catch (NumberFormatException var93) {
            if (propertyGiveFame.length > 0) {
               _log.warning("[Config.load()]: invalid config property -> GiveFamePrice");
            }
         }

         String[] propertyAugment = CommunityBoardSettings.getProperty("AugmentationPrice", "4037,10").split(",");

         try {
            SERVICES_AUGMENTATION_ITEM[0] = Integer.parseInt(propertyAugment[0]);
            SERVICES_AUGMENTATION_ITEM[1] = Integer.parseInt(propertyAugment[1]);
         } catch (NumberFormatException var92) {
            if (propertyAugment.length > 0) {
               _log.warning("[Config.load()]: invalid config property -> AugmentationPrice");
            }
         }

         SERVICES_AUGMENTATION_FORMATE = Boolean.parseBoolean(CommunityBoardSettings.getProperty("AugmentationAvailableFormat", "false"));
         String[] augs2 = CommunityBoardSettings.getProperty("AugmentationAvailableList", "0").trim().split(",");

         for(String aug : augs2) {
            if (!aug.isEmpty()) {
               SERVICES_AUGMENTATION_AVAILABLE_LIST.add(Integer.parseInt(aug.trim()));
            }
         }

         String[] augs = CommunityBoardSettings.getProperty("AugmentationDisabledList", "0").trim().split(",");

         for(String aug : augs) {
            if (!aug.isEmpty()) {
               SERVICES_AUGMENTATION_DISABLED_LIST.add(Integer.parseInt(aug.trim()));
            }
         }

         BBS_FORGE_ENCHANT_ITEM = Integer.parseInt(CommunityBoardSettings.getProperty("ItemID", "4037"));
         BBS_FORGE_FOUNDATION_ITEM = Integer.parseInt(CommunityBoardSettings.getProperty("FoundationItem", "4037"));
         String[] forgeArmor = CommunityBoardSettings.getProperty("FoundationPriceArmor", "1,1,1,1,1,2,5,10").trim().split(",");
         BBS_FORGE_FOUNDATION_PRICE_ARMOR = new int[forgeArmor.length];

         try {
            int i = 0;

            for(String priceId : forgeArmor) {
               BBS_FORGE_FOUNDATION_PRICE_ARMOR[i++] = Integer.parseInt(priceId);
            }
         } catch (NumberFormatException var91) {
            _log.log(Level.WARNING, var91.getMessage(), (Throwable)var91);
         }

         String[] forgeWeapon = CommunityBoardSettings.getProperty("FoundationPriceWeapon", "1,1,1,1,1,2,5,10").trim().split(",");
         BBS_FORGE_FOUNDATION_PRICE_WEAPON = new int[forgeWeapon.length];

         try {
            int i = 0;

            for(String priceId : forgeWeapon) {
               BBS_FORGE_FOUNDATION_PRICE_WEAPON[i++] = Integer.parseInt(priceId);
            }
         } catch (NumberFormatException var90) {
            _log.log(Level.WARNING, var90.getMessage(), (Throwable)var90);
         }

         String[] forgeJewel = CommunityBoardSettings.getProperty("FoundationPriceJewel", "1,1,1,1,1,2,5,10").trim().split(",");
         BBS_FORGE_FOUNDATION_PRICE_JEWEL = new int[forgeJewel.length];

         try {
            int i = 0;

            for(String priceId : forgeJewel) {
               BBS_FORGE_FOUNDATION_PRICE_JEWEL[i++] = Integer.parseInt(priceId);
            }
         } catch (NumberFormatException var89) {
            _log.log(Level.WARNING, var89.getMessage(), (Throwable)var89);
         }

         String[] enchantMax = CommunityBoardSettings.getProperty("MaxEnchant", "12,12,12").trim().split(",");
         BBS_FORGE_ENCHANT_MAX = new int[enchantMax.length];

         try {
            int i = 0;

            for(String enchMax : enchantMax) {
               BBS_FORGE_ENCHANT_MAX[i++] = Integer.parseInt(enchMax);
            }
         } catch (NumberFormatException var88) {
            _log.log(Level.WARNING, var88.getMessage(), (Throwable)var88);
         }

         String[] enchantLvl = CommunityBoardSettings.getProperty("WeaponEnchantLvls", "6,7,8").trim().split(",");
         BBS_FORGE_WEAPON_ENCHANT_LVL = new int[enchantLvl.length];

         try {
            int i = 0;

            for(String enchLvl : enchantLvl) {
               BBS_FORGE_WEAPON_ENCHANT_LVL[i++] = Integer.parseInt(enchLvl);
            }
         } catch (NumberFormatException var87) {
            _log.log(Level.WARNING, var87.getMessage(), (Throwable)var87);
         }

         String[] enchantWLvl = CommunityBoardSettings.getProperty("ArmorEnchantLvls", "6,7,8").trim().split(",");
         BBS_FORGE_ARMOR_ENCHANT_LVL = new int[enchantWLvl.length];

         try {
            int i = 0;

            for(String enchLvl : enchantWLvl) {
               BBS_FORGE_ARMOR_ENCHANT_LVL[i++] = Integer.parseInt(enchLvl);
            }
         } catch (NumberFormatException var86) {
            _log.log(Level.WARNING, var86.getMessage(), (Throwable)var86);
         }

         String[] enchantJLvl = CommunityBoardSettings.getProperty("JewelryEnchantLvls", "6,7,8").trim().split(",");
         BBS_FORGE_JEWELS_ENCHANT_LVL = new int[enchantJLvl.length];

         try {
            int i = 0;

            for(String enchLvl : enchantJLvl) {
               BBS_FORGE_JEWELS_ENCHANT_LVL[i++] = Integer.parseInt(enchLvl);
            }
         } catch (NumberFormatException var85) {
            _log.log(Level.WARNING, var85.getMessage(), (Throwable)var85);
         }

         String[] Wprice = CommunityBoardSettings.getProperty("EnchantWeaponPrice", "10,20,30").trim().split(",");
         BBS_FORGE_ENCHANT_PRICE_WEAPON = new int[Wprice.length];

         try {
            int i = 0;

            for(String wPrice : Wprice) {
               BBS_FORGE_ENCHANT_PRICE_WEAPON[i++] = Integer.parseInt(wPrice);
            }
         } catch (NumberFormatException var84) {
            _log.log(Level.WARNING, var84.getMessage(), (Throwable)var84);
         }

         String[] Aprice = CommunityBoardSettings.getProperty("EnchantArmorPrice", "5,10,12").trim().split(",");
         BBS_FORGE_ENCHANT_PRICE_ARMOR = new int[Aprice.length];

         try {
            int i = 0;

            for(String aPrice : Aprice) {
               BBS_FORGE_ENCHANT_PRICE_ARMOR[i++] = Integer.parseInt(aPrice);
            }
         } catch (NumberFormatException var83) {
            _log.log(Level.WARNING, var83.getMessage(), (Throwable)var83);
         }

         String[] Jprice = CommunityBoardSettings.getProperty("EnchantJewelryPrice", "4,6,8").trim().split(",");
         BBS_FORGE_ENCHANT_PRICE_JEWELS = new int[Jprice.length];

         try {
            int i = 0;

            for(String jPrice : Jprice) {
               BBS_FORGE_ENCHANT_PRICE_JEWELS[i++] = Integer.parseInt(jPrice);
            }
         } catch (NumberFormatException var82) {
            _log.log(Level.WARNING, var82.getMessage(), (Throwable)var82);
         }

         String[] attWLvl = CommunityBoardSettings.getProperty("AtributeWeaponValue", "300").trim().split(",");
         BBS_FORGE_ATRIBUTE_LVL_WEAPON = new int[attWLvl.length];

         try {
            int i = 0;

            for(String wLvl : attWLvl) {
               BBS_FORGE_ATRIBUTE_LVL_WEAPON[i++] = Integer.parseInt(wLvl);
            }
         } catch (NumberFormatException var81) {
            _log.log(Level.WARNING, var81.getMessage(), (Throwable)var81);
         }

         String[] attWPrice = CommunityBoardSettings.getProperty("PriceForAtributeWeapon", "30").trim().split(",");
         BBS_FORGE_ATRIBUTE_PRICE_WEAPON = new int[attWPrice.length];

         try {
            int i = 0;

            for(String wPrice : attWPrice) {
               BBS_FORGE_ATRIBUTE_PRICE_WEAPON[i++] = Integer.parseInt(wPrice);
            }
         } catch (NumberFormatException var80) {
            _log.log(Level.WARNING, var80.getMessage(), (Throwable)var80);
         }

         String[] attALvl = CommunityBoardSettings.getProperty("AtributeArmorValue", "120").trim().split(",");
         BBS_FORGE_ATRIBUTE_LVL_ARMOR = new int[attALvl.length];

         try {
            int i = 0;

            for(String aLvl : attALvl) {
               BBS_FORGE_ATRIBUTE_LVL_ARMOR[i++] = Integer.parseInt(aLvl);
            }
         } catch (NumberFormatException var79) {
            _log.log(Level.WARNING, var79.getMessage(), (Throwable)var79);
         }

         String[] attAPrice = CommunityBoardSettings.getProperty("PriceForAtributeArmor", "6").trim().split(",");
         BBS_FORGE_ATRIBUTE_PRICE_ARMOR = new int[attAPrice.length];

         try {
            int i = 0;

            for(String aPrice : attAPrice) {
               BBS_FORGE_ATRIBUTE_PRICE_ARMOR[i++] = Integer.parseInt(aPrice);
            }
         } catch (NumberFormatException var78) {
            _log.log(Level.WARNING, var78.getMessage(), (Throwable)var78);
         }

         BBS_FORGE_WEAPON_ATTRIBUTE_MAX = Integer.parseInt(CommunityBoardSettings.getProperty("MaxWeaponAttribute", "25"));
         BBS_FORGE_ARMOR_ATTRIBUTE_MAX = Integer.parseInt(CommunityBoardSettings.getProperty("MaxArmorAttribute", "25"));
         String[] propertyCloak = CommunityBoardSettings.getProperty("SoulCloakTransferItem", "4037,50").split(",");

         try {
            SERVICES_SOUL_CLOAK_TRANSFER_ITEM[0] = Integer.parseInt(propertyCloak[0]);
            SERVICES_SOUL_CLOAK_TRANSFER_ITEM[1] = Integer.parseInt(propertyCloak[1]);
         } catch (NumberFormatException var77) {
            if (propertyCloak.length > 0) {
               _log.warning("[Config.load()]: invalid config property -> SoulCloakTransferItem");
            }
         }

         SERVICES_OLF_STORE_ITEM = Integer.parseInt(CommunityBoardSettings.getProperty("OlfStoreItemId", "4037"));
         SERVICES_OLF_STORE_0_PRICE = Integer.parseInt(CommunityBoardSettings.getProperty("OlfStoreEnchant0", "100"));
         SERVICES_OLF_STORE_6_PRICE = Integer.parseInt(CommunityBoardSettings.getProperty("OlfStoreEnchant6", "200"));
         SERVICES_OLF_STORE_7_PRICE = Integer.parseInt(CommunityBoardSettings.getProperty("OlfStoreEnchant7", "275"));
         SERVICES_OLF_STORE_8_PRICE = Integer.parseInt(CommunityBoardSettings.getProperty("OlfStoreEnchant8", "350"));
         SERVICES_OLF_STORE_9_PRICE = Integer.parseInt(CommunityBoardSettings.getProperty("OlfStoreEnchant9", "425"));
         SERVICES_OLF_STORE_10_PRICE = Integer.parseInt(CommunityBoardSettings.getProperty("OlfStoreEnchant10", "500"));
         String[] propertyOlf = CommunityBoardSettings.getProperty("OlfTransferItem", "4037,50").split(",");

         try {
            SERVICES_OLF_TRANSFER_ITEM[0] = Integer.parseInt(propertyOlf[0]);
            SERVICES_OLF_TRANSFER_ITEM[1] = Integer.parseInt(propertyOlf[1]);
         } catch (NumberFormatException var76) {
            if (propertyOlf.length > 0) {
               _log.warning("[Config.load()]: invalid config property -> OlfTransferItem");
            }
         }

         ENABLE_MULTI_AUCTION_SYSTEM = Boolean.parseBoolean(CommunityBoardSettings.getProperty("EnableMultiAuctionSystem", "False"));
         AUCTION_FEE = (long)Integer.parseInt(CommunityBoardSettings.getProperty("AuctionPrice", "10000"));
         ALLOW_AUCTION_OUTSIDE_TOWN = Boolean.parseBoolean(CommunityBoardSettings.getProperty("AuctionOutsideTown", "false"));
         ALLOW_ADDING_AUCTION_DELAY = Boolean.parseBoolean(CommunityBoardSettings.getProperty("AllowAuctionDelay", "false"));
         SECONDS_BETWEEN_ADDING_AUCTIONS = Integer.parseInt(CommunityBoardSettings.getProperty("AuctionAddDelay", "30"));
         AUCTION_PRIVATE_STORE_AUTO_ADDED = Boolean.parseBoolean(CommunityBoardSettings.getProperty("AuctionPrivateStoreAutoAdded", "true"));
         String[] bosses = CommunityBoardSettings.getProperty("RaidBossesNotShow", "25423,25010").trim().split(",");
         BBS_BOSSES_TO_NOT_SHOW = new int[bosses.length];

         try {
            int i = 0;

            for(String boss : bosses) {
               BBS_BOSSES_TO_NOT_SHOW[i++] = Integer.parseInt(boss);
            }
         } catch (NumberFormatException var75) {
            _log.log(Level.WARNING, var75.getMessage(), (Throwable)var75);
         }

         ALLOW_BOSS_RESPAWN_TIME = Boolean.parseBoolean(CommunityBoardSettings.getProperty("DisplayRespawnTime", "False"));
         String[] pTemplates = CommunityBoardSettings.getProperty("PremiumValidTemplates", "1,2,3").trim().split(",");
         SERVICES_PREMIUM_VALID_ID = new int[pTemplates.length];

         try {
            int i = 0;

            for(String tpl : pTemplates) {
               SERVICES_PREMIUM_VALID_ID[i++] = Integer.parseInt(tpl);
            }
         } catch (NumberFormatException var74) {
            _log.log(Level.WARNING, var74.getMessage(), (Throwable)var74);
         }

         ALLOW_CERT_DONATE_MODE = Boolean.parseBoolean(CommunityBoardSettings.getProperty("AllowCertificationDonate", "False"));
         CERT_MIN_LEVEL = Integer.parseInt(CommunityBoardSettings.getProperty("CertificationUseLevel", "80"));
         CERT_BLOCK_SKILL_LIST = CommunityBoardSettings.getProperty("CertificationBlockSkills", "");
         String[] propertyEmergent = CommunityBoardSettings.getProperty("EmergentSkillsPrice", "4037,10").split(",");

         try {
            EMERGET_SKILLS_LEARN[0] = Integer.parseInt(propertyEmergent[0]);
            EMERGET_SKILLS_LEARN[1] = Integer.parseInt(propertyEmergent[1]);
         } catch (NumberFormatException var73) {
            if (propertyEmergent.length > 0) {
               _log.warning("[Config.load()]: invalid config property -> EmergentSkillsPrice");
            }
         }

         String[] propertyMaster = CommunityBoardSettings.getProperty("MasterSkillsPrice", "4037,10").split(",");

         try {
            MASTER_SKILLS_LEARN[0] = Integer.parseInt(propertyMaster[0]);
            MASTER_SKILLS_LEARN[1] = Integer.parseInt(propertyMaster[1]);
         } catch (NumberFormatException var72) {
            if (propertyMaster.length > 0) {
               _log.warning("[Config.load()]: invalid config property -> MasterSkillsPrice");
            }
         }

         String[] propertyTransform = CommunityBoardSettings.getProperty("TransformSkillsPrice", "4037,10").split(",");

         try {
            TRANSFORM_SKILLS_LEARN[0] = Integer.parseInt(propertyTransform[0]);
            TRANSFORM_SKILLS_LEARN[1] = Integer.parseInt(propertyTransform[1]);
         } catch (NumberFormatException var71) {
            if (propertyTransform.length > 0) {
               _log.warning("[Config.load()]: invalid config property -> TransformSkillsPrice");
            }
         }

         String[] propertyClean = CommunityBoardSettings.getProperty("CleanSkillsPrice", "4037,10").split(",");

         try {
            CLEAN_SKILLS_LEARN[0] = Integer.parseInt(propertyClean[0]);
            CLEAN_SKILLS_LEARN[1] = Integer.parseInt(propertyClean[1]);
         } catch (NumberFormatException var70) {
            if (propertyClean.length > 0) {
               _log.warning("[Config.load()]: invalid config property -> CleanSkillsPrice");
            }
         }

         ALLOW_TELEPORT_TO_RAID = Boolean.parseBoolean(CommunityBoardSettings.getProperty("AllowTeleportToRaid", "False"));
         String[] propertyTp = CommunityBoardSettings.getProperty("TeleportToRaidPrice", "57,10000").split(",");

         try {
            TELEPORT_TO_RAID_PRICE[0] = Integer.parseInt(propertyTp[0]);
            TELEPORT_TO_RAID_PRICE[1] = Integer.parseInt(propertyTp[1]);
         } catch (NumberFormatException var69) {
            if (propertyTp.length > 0) {
               _log.warning("[Config.load()]: invalid config property -> TeleportToRaidPrice");
            }
         }

         String[] blockRaids = CommunityBoardSettings.getProperty("BlockedRaidList", "0").trim().split(",");

         for(String blockRaid : blockRaids) {
            if (!blockRaid.isEmpty()) {
               BLOCKED_RAID_LIST.add(Integer.parseInt(blockRaid.trim()));
            }
         }

         String[] prCreatePenalty = CommunityBoardSettings.getProperty("RemoveClanCreatePenaltyPrice", "4037,30").split(",");

         try {
            SERVICES_CLAN_CREATE_PENALTY_ITEM[0] = Integer.parseInt(prCreatePenalty[0]);
            SERVICES_CLAN_CREATE_PENALTY_ITEM[1] = Integer.parseInt(prCreatePenalty[1]);
         } catch (NumberFormatException var68) {
            if (prCreatePenalty.length > 0) {
               _log.warning("[Config.load()]: invalid config property -> RemoveClanCreatePenaltyPrice");
            }
         }

         String[] prJoinPenalty = CommunityBoardSettings.getProperty("RemoveClanJoinPenaltyPrice", "4037,30").split(",");

         try {
            SERVICES_CLAN_JOIN_PENALTY_ITEM[0] = Integer.parseInt(prJoinPenalty[0]);
            SERVICES_CLAN_JOIN_PENALTY_ITEM[1] = Integer.parseInt(prJoinPenalty[1]);
         } catch (NumberFormatException var67) {
            if (prJoinPenalty.length > 0) {
               _log.warning("[Config.load()]: invalid config property -> RemoveClanJoinPenaltyPrice");
            }
         }

         COMMUNITY_FREE_TP_LVL = Integer.parseInt(CommunityBoardSettings.getProperty("FreeTeleportsMaxLvl", "1"));
         COMMUNITY_FREE_BUFF_LVL = Integer.parseInt(CommunityBoardSettings.getProperty("FreeBuffsMaxLvl", "1"));
         String[] colorNameSplit = CommunityBoardSettings.getProperty("ColorNamePriceList", "").split(";");
         CHANGE_COLOR_NAME_LIST = new HashMap<>(colorNameSplit.length);

         for(String price : colorNameSplit) {
            String[] priceSplit = price.split(",");
            if (priceSplit.length != 2) {
               _log.warning("[Config.load()]: invalid config property -> ColorNamePriceList " + price + "");
            } else {
               try {
                  CHANGE_COLOR_NAME_LIST.put(Integer.parseInt(priceSplit[0]), priceSplit[1]);
               } catch (NumberFormatException var66) {
                  if (!price.isEmpty()) {
                     _log.warning("[Config.load()]: invalid config property -> Price " + priceSplit[0] + " " + priceSplit[1] + "");
                  }
               }
            }
         }

         String[] colorTitleSplit = CommunityBoardSettings.getProperty("ColorTitlePriceList", "").split(";");
         CHANGE_COLOR_TITLE_LIST = new HashMap<>(colorTitleSplit.length);

         for(String price : colorTitleSplit) {
            String[] priceSplit = price.split(",");
            if (priceSplit.length != 2) {
               _log.warning("[ColorTitlePriceList]: invalid config property -> ColorTitlePriceList " + price + "");
            } else {
               try {
                  CHANGE_COLOR_TITLE_LIST.put(Integer.parseInt(priceSplit[0]), priceSplit[1]);
               } catch (NumberFormatException var65) {
                  if (!price.isEmpty()) {
                     _log.warning("[ColorTitlePriceList]: invalid config property -> Price " + priceSplit[0] + " " + priceSplit[1] + "");
                  }
               }
            }
         }

         String[] exInv = CommunityBoardSettings.getProperty("ExpandInventoryPrice", "4037,30").split(",");

         try {
            SERVICES_EXPAND_INVENTORY[0] = Integer.parseInt(exInv[0]);
            SERVICES_EXPAND_INVENTORY[1] = Integer.parseInt(exInv[1]);
         } catch (NumberFormatException var64) {
            if (exInv.length > 0) {
               _log.warning("[Config.load()]: invalid config property -> ExpandInventoryPrice");
            }
         }

         String[] exWareH = CommunityBoardSettings.getProperty("ExpandWareHousePrice", "4037,30").split(",");

         try {
            SERVICES_EXPAND_WAREHOUSE[0] = Integer.parseInt(exWareH[0]);
            SERVICES_EXPAND_WAREHOUSE[1] = Integer.parseInt(exWareH[1]);
         } catch (NumberFormatException var63) {
            if (exWareH.length > 0) {
               _log.warning("[Config.load()]: invalid config property -> ExpandWareHousePrice");
            }
         }

         String[] exSellStore = CommunityBoardSettings.getProperty("ExpandSellStorePrice", "4037,30").split(",");

         try {
            SERVICES_EXPAND_SELLSTORE[0] = Integer.parseInt(exSellStore[0]);
            SERVICES_EXPAND_SELLSTORE[1] = Integer.parseInt(exSellStore[1]);
         } catch (NumberFormatException var62) {
            if (exSellStore.length > 0) {
               _log.warning("[Config.load()]: invalid config property -> ExpandSellStorePrice");
            }
         }

         String[] exBuyStore = CommunityBoardSettings.getProperty("ExpandBuyStorePrice", "4037,30").split(",");

         try {
            SERVICES_EXPAND_BUYSTORE[0] = Integer.parseInt(exBuyStore[0]);
            SERVICES_EXPAND_BUYSTORE[1] = Integer.parseInt(exBuyStore[1]);
         } catch (NumberFormatException var61) {
            if (exBuyStore.length > 0) {
               _log.warning("[Config.load()]: invalid config property -> ExpandBuyStorePrice");
            }
         }

         String[] exDwarfRec = CommunityBoardSettings.getProperty("ExpandDwarfRecipePrice", "4037,30").split(",");

         try {
            SERVICES_EXPAND_DWARFRECIPE[0] = Integer.parseInt(exDwarfRec[0]);
            SERVICES_EXPAND_DWARFRECIPE[1] = Integer.parseInt(exDwarfRec[1]);
         } catch (NumberFormatException var60) {
            if (exDwarfRec.length > 0) {
               _log.warning("[Config.load()]: invalid config property -> ExpandDwarfRecipePrice");
            }
         }

         String[] exComRec = CommunityBoardSettings.getProperty("ExpandCommonRecipePrice", "4037,30").split(",");

         try {
            SERVICES_EXPAND_COMMONRECIPE[0] = Integer.parseInt(exComRec[0]);
            SERVICES_EXPAND_COMMONRECIPE[1] = Integer.parseInt(exComRec[1]);
         } catch (NumberFormatException var59) {
            if (exComRec.length > 0) {
               _log.warning("[Config.load()]: invalid config property -> ExpandCommonRecipePrice");
            }
         }

         EXPAND_INVENTORY_STEP = Integer.parseInt(CommunityBoardSettings.getProperty("ExpandInventoryStep", "1"));
         EXPAND_WAREHOUSE_STEP = Integer.parseInt(CommunityBoardSettings.getProperty("ExpandWareHouseStep", "1"));
         EXPAND_SELLSTORE_STEP = Integer.parseInt(CommunityBoardSettings.getProperty("ExpandSellStoreStep", "1"));
         EXPAND_BUYSTORE_STEP = Integer.parseInt(CommunityBoardSettings.getProperty("ExpandBuyStoreStep", "1"));
         EXPAND_DWARFRECIPE_STEP = Integer.parseInt(CommunityBoardSettings.getProperty("ExpandDwarfRecipeStep", "1"));
         EXPAND_COMMONRECIPE_STEP = Integer.parseInt(CommunityBoardSettings.getProperty("ExpandCommonRecipeStep", "1"));
         SERVICES_EXPAND_INVENTORY_LIMIT = Integer.parseInt(CommunityBoardSettings.getProperty("CBExpandInventoryLimit", "50"));
         SERVICES_EXPAND_WAREHOUSE_LIMIT = Integer.parseInt(CommunityBoardSettings.getProperty("CBExpandWareHouseLimit", "50"));
         SERVICES_EXPAND_SELLSTORE_LIMIT = Integer.parseInt(CommunityBoardSettings.getProperty("CBExpandSellStoreLimit", "50"));
         SERVICES_EXPAND_BUYSTORE_LIMIT = Integer.parseInt(CommunityBoardSettings.getProperty("CBExpandBuyStoreLimit", "50"));
         SERVICES_EXPAND_DWARFRECIPE_LIMIT = Integer.parseInt(CommunityBoardSettings.getProperty("CBExpandDwarfRecipeLimit", "50"));
         SERVICES_EXPAND_COMMONRECIPE_LIMIT = Integer.parseInt(CommunityBoardSettings.getProperty("CBExpandCommonRecipeLimit", "50"));
         SERVICES_ACADEMY_REWARD = CommunityBoardSettings.getProperty("AcademyRewardsItemList", "1");
         ACADEMY_MIN_ADENA_AMOUNT = Long.parseLong(CommunityBoardSettings.getProperty("AcademyMinPrice", "1"));
         ACADEMY_MAX_ADENA_AMOUNT = Long.parseLong(CommunityBoardSettings.getProperty("AcademyMaxPrice", "1000000000"));
         MAX_TIME_IN_ACADEMY = Long.parseLong(CommunityBoardSettings.getProperty("AcademyKickDelay", "4320"));
      } catch (Exception var110) {
         _log.warning("Config: " + var110.getMessage());
         throw new Error("Failed to Load ./config/main/communityBoard.ini File.");
      }
   }

   private static void loadWeddingSettings(InputStream is) {
      try {
         GameSettings WeddingSettings = new GameSettings();
         InputStream var6 = new FileInputStream(new File("./config/mods/wedding.ini"));
         WeddingSettings.load(var6);
         ALLOW_WEDDING = Boolean.parseBoolean(WeddingSettings.getProperty("AllowWedding", "False"));
         WEDDING_PRICE = Integer.parseInt(WeddingSettings.getProperty("WeddingPrice", "250000000"));
         WEDDING_PUNISH_INFIDELITY = Boolean.parseBoolean(WeddingSettings.getProperty("WeddingPunishInfidelity", "True"));
         WEDDING_TELEPORT = Boolean.parseBoolean(WeddingSettings.getProperty("WeddingTeleport", "True"));
         WEDDING_TELEPORT_PRICE = Integer.parseInt(WeddingSettings.getProperty("WeddingTeleportPrice", "50000"));
         WEDDING_TELEPORT_DURATION = Integer.parseInt(WeddingSettings.getProperty("WeddingTeleportDuration", "60"));
         WEDDING_FORMALWEAR = Boolean.parseBoolean(WeddingSettings.getProperty("WeddingFormalWear", "True"));
         WEDDING_DIVORCE_COSTS = Integer.parseInt(WeddingSettings.getProperty("WeddingDivorceCosts", "20"));
         String[] weddingReward = WeddingSettings.getProperty("WeddingReward", "4037,10").split(",");

         try {
            WEDDING_REWARD[0] = Integer.parseInt(weddingReward[0]);
            WEDDING_REWARD[1] = Integer.parseInt(weddingReward[1]);
         } catch (NumberFormatException var4) {
            if (weddingReward.length > 0) {
               _log.warning("[Config.load()]: invalid config property -> WeddingReward");
            }
         }
      } catch (Exception var5) {
         _log.warning("Config: " + var5.getMessage());
         throw new Error("Failed to Load ./config/mods/wedding.ini File.");
      }
   }

   private static void loadOfflineTradeSettings(InputStream is) {
      try {
         GameSettings offtradeSettings = new GameSettings();
         InputStream var3 = new FileInputStream(new File("./config/mods/offline_trade.ini"));
         offtradeSettings.load(var3);
         OFFLINE_TRADE_ENABLE = Boolean.parseBoolean(offtradeSettings.getProperty("OfflineTradeEnable", "False"));
         OFFLINE_TRADE_MIN_LVL = Integer.parseInt(offtradeSettings.getProperty("OfflineTradeMinLevel", "1"));
         OFFLINE_CRAFT_ENABLE = Boolean.parseBoolean(offtradeSettings.getProperty("OfflineCraftEnable", "False"));
         OFFLINE_MODE_IN_PEACE_ZONE = Boolean.parseBoolean(offtradeSettings.getProperty("OfflineModeInPaceZone", "False"));
         OFFLINE_MODE_NO_DAMAGE = Boolean.parseBoolean(offtradeSettings.getProperty("OfflineModeNoDamage", "False"));
         OFFLINE_SET_NAME_COLOR = Boolean.parseBoolean(offtradeSettings.getProperty("OfflineSetNameColor", "False"));
         OFFLINE_SET_VISUAL_EFFECT = Boolean.parseBoolean(offtradeSettings.getProperty("OfflineSetVisualEffect", "False"));
         OFFLINE_NAME_COLOR = Integer.decode("0x" + offtradeSettings.getProperty("OfflineNameColor", "808080"));
         OFFLINE_FAME = Boolean.parseBoolean(offtradeSettings.getProperty("OfflineFame", "True"));
         RESTORE_OFFLINERS = Boolean.parseBoolean(offtradeSettings.getProperty("RestoreOffliners", "False"));
         OFFLINE_MAX_DAYS = Integer.parseInt(offtradeSettings.getProperty("OfflineMaxDays", "10"));
         OFFLINE_DISCONNECT_FINISHED = Boolean.parseBoolean(offtradeSettings.getProperty("OfflineDisconnectFinished", "True"));
      } catch (Exception var2) {
         _log.warning("Config: " + var2.getMessage());
         throw new Error("Failed to Load ./config/mods/offline_trade.ini File.");
      }
   }

   private static void loadDualSessionSettings(InputStream is) {
      try {
         GameSettings DualSessionSettings = new GameSettings();
         InputStream var3 = new FileInputStream(new File("./config/mods/doubleSessions.ini"));
         DualSessionSettings.load(var3);
         DOUBLE_SESSIONS_ENABLE = Boolean.parseBoolean(DualSessionSettings.getProperty("AllowCheckSessions", "false"));
         DOUBLE_SESSIONS_HWIDS = Boolean.parseBoolean(DualSessionSettings.getProperty("AllowCheckSessionHwids", "false"));
         DOUBLE_SESSIONS_DISCONNECTED = Boolean.parseBoolean(DualSessionSettings.getProperty("AllowCheckDisconnectedSessions", "true"));
         DOUBLE_SESSIONS_INTERVAL = 1000 * Integer.parseInt(DualSessionSettings.getProperty("CheckSessionsInterval", "120"));
         DOUBLE_SESSIONS_CHECK_MAX_PLAYERS = Integer.parseInt(DualSessionSettings.getProperty("SessionCheckMaxPlayers", "0"));
         DOUBLE_SESSIONS_CONSIDER_OFFLINE_TRADERS = Boolean.parseBoolean(DualSessionSettings.getProperty("ConsiderSessionOfflineTraders", "True"));
         DOUBLE_SESSIONS_CHECK_MAX_OLYMPIAD_PARTICIPANTS = Integer.parseInt(DualSessionSettings.getProperty("CheckMaxOlympiadParticipants", "0"));
         DOUBLE_SESSIONS_CHECK_MAX_EVENT_PARTICIPANTS = Integer.parseInt(DualSessionSettings.getProperty("CheckMaxFightEventParticipants", "0"));
         PROTECTION_IP_ENABLED = Boolean.parseBoolean(DualSessionSettings.getProperty("ProtectionIpEnabled", "False"));
      } catch (Exception var2) {
         _log.warning("Config: " + var2.getMessage());
         throw new Error("Failed to Load ./config/mods/doubleSessions.ini File.");
      }
   }

   private static void loadEnchantSettings(InputStream is) {
      try {
         GameSettings enchantSettings = new GameSettings();
         InputStream var5 = new FileInputStream(new File("./config/main/enchant.ini"));
         enchantSettings.load(var5);
         ENCHANT_CHANCE_ELEMENT_STONE = Double.parseDouble(enchantSettings.getProperty("EnchantChanceElementStone", "50"));
         ENCHANT_CHANCE_ELEMENT_CRYSTAL = Double.parseDouble(enchantSettings.getProperty("EnchantChanceElementCrystal", "30"));
         ENCHANT_CHANCE_ELEMENT_JEWEL = Double.parseDouble(enchantSettings.getProperty("EnchantChanceElementJewel", "20"));
         ENCHANT_CHANCE_ELEMENT_ENERGY = Double.parseDouble(enchantSettings.getProperty("EnchantChanceElementEnergy", "10"));
         ENCHANT_ELEMENT_ALL_ITEMS = Boolean.parseBoolean(enchantSettings.getProperty("AllowEnchantElementAllItems", "False"));
         String[] notenchantable = enchantSettings.getProperty(
               "EnchantBlackList", "7816,7817,7818,7819,7820,7821,7822,7823,7824,7825,7826,7827,7828,7829,7830,7831,13293,13294,13296"
            )
            .split(",");
         ENCHANT_BLACKLIST = new int[notenchantable.length];

         for(int i = 0; i < notenchantable.length; ++i) {
            ENCHANT_BLACKLIST[i] = Integer.parseInt(notenchantable[i]);
         }

         Arrays.sort(ENCHANT_BLACKLIST);
         SYSTEM_BLESSED_ENCHANT = Boolean.parseBoolean(enchantSettings.getProperty("SystemBlessedEnchant", "False"));
         BLESSED_ENCHANT_SAVE = Integer.parseInt(enchantSettings.getProperty("BlessedEnchantSave", "0"));
      } catch (Exception var4) {
         _log.warning("Config: " + var4.getMessage());
         throw new Error("Failed to Load ./config/main/enchant.ini File.");
      }
   }

   private static void loadHitmanSettings(InputStream is) {
      try {
         GameSettings HitmanSettings = new GameSettings();
         InputStream var10 = new FileInputStream(new File("./config/events/hitman_event.ini"));
         HitmanSettings.load(var10);
         HITMAN_TAKE_KARMA = Boolean.parseBoolean(HitmanSettings.getProperty("HitmansTakekarma", "True"));
         HITMAN_ANNOUNCE = Boolean.parseBoolean(HitmanSettings.getProperty("HitmanAnnounce", "False"));
         HITMAN_MAX_PER_PAGE = Integer.parseInt(HitmanSettings.getProperty("HitmanMaxPerPage", "20"));
         String[] split = HitmanSettings.getProperty("HitmanCurrency", "57,4037,9143").split(",");
         HITMAN_CURRENCY = new ArrayList<>();

         for(String id : split) {
            try {
               Integer itemId = Integer.parseInt(id);
               HITMAN_CURRENCY.add(itemId);
            } catch (Exception var8) {
               _log.info("Wrong config item id: " + id + ". Skipped.");
            }
         }

         HITMAN_SAME_TEAM = Boolean.parseBoolean(HitmanSettings.getProperty("HitmanSameTeam", "False"));
         HITMAN_SAVE_TARGET = Integer.parseInt(HitmanSettings.getProperty("HitmanSaveTarget", "15"));
         HITMAN_TARGETS_LIMIT = Integer.parseInt(HitmanSettings.getProperty("HitmanTargetsLimit", "5"));
      } catch (Exception var9) {
         _log.warning("Config: " + var9.getMessage());
         throw new Error("Failed to Load ./config/events/hitman_event.ini File.");
      }
   }

   private static void loadUndergroundColliseumSettings(InputStream is) {
      try {
         GameSettings undergroundcoliseum = new GameSettings();
         InputStream var3 = new FileInputStream(new File("./config/events/undergroundColiseum.ini"));
         undergroundcoliseum.load(var3);
         UC_START_TIME = undergroundcoliseum.getProperty("BattlesStartTime", "0 17 * * *");
         UC_TIME_PERIOD = Integer.parseInt(undergroundcoliseum.getProperty("BattlesPeriod", "5"));
         UC_ANNOUNCE_BATTLES = Boolean.parseBoolean(undergroundcoliseum.getProperty("AllowAnnouncePeriods", "False"));
         UC_PARTY_LIMIT = Integer.parseInt(undergroundcoliseum.getProperty("PartyLimit", "7"));
         UC_RESS_TIME = Integer.parseInt(undergroundcoliseum.getProperty("RessTime", "10"));
      } catch (Exception var2) {
         _log.warning("Config: " + var2.getMessage());
         throw new Error("Failed to Load ./config/events/undergroundColiseum.ini File.");
      }
   }

   private static void loadItemMallSettings(InputStream is) {
      try {
         GameSettings itemmallSettings = new GameSettings();
         InputStream var3 = new FileInputStream(new File("./config/main/itemMall.ini"));
         itemmallSettings.load(var3);
         GAME_POINT_ITEM_ID = Integer.parseInt(itemmallSettings.getProperty("GamePointItemId", "-1"));
      } catch (Exception var2) {
         _log.warning("Config: " + var2.getMessage());
         throw new Error("Failed to Load ./config/main/itemMall.ini File.");
      }
   }

   private static void loadLeprechaunSettings(InputStream is) {
      try {
         GameSettings leprechaunEventSettings = new GameSettings();
         InputStream var14 = new FileInputStream(new File("./config/events/leprechaun_event.ini"));
         leprechaunEventSettings.load(var14);
         ENABLED_LEPRECHAUN = Boolean.parseBoolean(leprechaunEventSettings.getProperty("EnabledLeprechaun", "False"));
         LEPRECHAUN_ID = Integer.parseInt(leprechaunEventSettings.getProperty("LeprechaunId", "7805"));
         LEPRECHAUN_FIRST_SPAWN_DELAY = Integer.parseInt(leprechaunEventSettings.getProperty("LeprechaunFirstSpawnDelay", "5"));
         LEPRECHAUN_RESPAWN_INTERVAL = Integer.parseInt(leprechaunEventSettings.getProperty("LeprechaunRespawnInterval", "60"));
         LEPRECHAUN_SPAWN_TIME = Integer.parseInt(leprechaunEventSettings.getProperty("LeprechaunSpawnTime", "30"));
         LEPRECHAUN_ANNOUNCE_INTERVAL = Integer.parseInt(leprechaunEventSettings.getProperty("LeprechaunAnnounceInterval", "5"));
         SHOW_NICK = Boolean.parseBoolean(leprechaunEventSettings.getProperty("ShowNick", "True"));
         SHOW_REGION = Boolean.parseBoolean(leprechaunEventSettings.getProperty("ShowRegion", "True"));
         String[] rewardId = leprechaunEventSettings.getProperty("LeprechaunRewardId", "57,4037").trim().split(",");
         LEPRECHAUN_REWARD_ID = new int[rewardId.length];

         try {
            int i = 0;

            for(String id : rewardId) {
               LEPRECHAUN_REWARD_ID[i++] = Integer.parseInt(id);
            }
         } catch (NumberFormatException var12) {
            _log.log(Level.WARNING, var12.getMessage(), (Throwable)var12);
         }

         String[] rewardCount = leprechaunEventSettings.getProperty("LeprechaunRewardCount", "1000000,10").trim().split(",");
         LEPRECHAUN_REWARD_COUNT = new int[rewardCount.length];

         try {
            int i = 0;

            for(String count : rewardCount) {
               LEPRECHAUN_REWARD_COUNT[i++] = Integer.parseInt(count);
            }
         } catch (NumberFormatException var11) {
            _log.log(Level.WARNING, var11.getMessage(), (Throwable)var11);
         }

         String[] rewardChance = leprechaunEventSettings.getProperty("LeprechaunRewardChance", "100,60").trim().split(",");
         LEPRECHAUN_REWARD_CHANCE = new int[rewardChance.length];

         try {
            int i = 0;

            for(String chance : rewardChance) {
               LEPRECHAUN_REWARD_CHANCE[i++] = Integer.parseInt(chance);
            }
         } catch (NumberFormatException var10) {
            _log.log(Level.WARNING, var10.getMessage(), (Throwable)var10);
         }
      } catch (Exception var13) {
         _log.warning("Config: " + var13.getMessage());
         throw new Error("Failed to Load ./config/events/leprechaun_event.ini File.");
      }
   }

   private static void loadAerialCleftSettings(InputStream is) {
      try {
         GameSettings cleftSettings = new GameSettings();
         InputStream var3 = new FileInputStream(new File("./config/events/aerialCleft.ini"));
         cleftSettings.load(var3);
         CLEFT_MIN_TEAM_PLAYERS = Integer.parseInt(cleftSettings.getProperty("CleftMinTeamPlayers", "1"));
         CLEFT_BALANCER = Boolean.parseBoolean(cleftSettings.getProperty("CleftTeamsBalancer", "True"));
         CLEFT_WAR_TIME = Integer.parseInt(cleftSettings.getProperty("CleftEventTime", "25"));
         CLEFT_COLLECT_TIME = Integer.parseInt(cleftSettings.getProperty("CleftCollectTime", "5"));
         CLEFT_REWARD_ID = Integer.parseInt(cleftSettings.getProperty("CleftRewardId", "13749"));
         CLEFT_REWARD_COUNT_WINNER = Integer.parseInt(cleftSettings.getProperty("CleftRewardCountWinner", "50"));
         CLEFT_REWARD_COUNT_LOOSER = Integer.parseInt(cleftSettings.getProperty("CleftRewardCountLooser", "20"));
         CLEFT_MIN_PLAYR_EVENT_TIME = Integer.parseInt(cleftSettings.getProperty("CleftMinPlayerInEventTime", "15"));
         CLEFT_WITHOUT_SEEDS = Boolean.parseBoolean(cleftSettings.getProperty("CleftWithountSeeds", "False"));
         CLEFT_MIN_LEVEL = Integer.parseInt(cleftSettings.getProperty("CleftMinLevel", "75"));
         CLEFT_TIME_RELOAD_REG = Integer.parseInt(cleftSettings.getProperty("CleftTimeReloadTime", "60"));
         CLEFT_MAX_PLAYERS = Integer.parseInt(cleftSettings.getProperty("CleftMaximumPlayers", "18"));
         CLEFT_RESPAWN_DELAY = Integer.parseInt(cleftSettings.getProperty("CleftRespawnDelay", "10"));
         CLEFT_LEAVE_DELAY = Integer.parseInt(cleftSettings.getProperty("CleftLeaveDelay", "2"));
         LARGE_COMPRESSOR_POINT = Integer.parseInt(cleftSettings.getProperty("CleftLargeCompressorPoint", "100"));
         SMALL_COMPRESSOR_POINT = Integer.parseInt(cleftSettings.getProperty("CleftSmallCompressorPoint", "40"));
         TEAM_CAT_POINT = Integer.parseInt(cleftSettings.getProperty("CleftTeamCatPoint", "10"));
         TEAM_PLAYER_POINT = Integer.parseInt(cleftSettings.getProperty("CleftPlayerPoint", "1"));
      } catch (Exception var2) {
         _log.warning("Config: " + var2.getMessage());
         throw new Error("Failed to Load ./config/events/aerialCleft.ini File.");
      }
   }

   private static void loadOlyAntiFeedSettings(InputStream is) {
      try {
         GameSettings antifeedoly = new GameSettings();
         InputStream var3 = new FileInputStream(new File("./config/mods/olympiadAntiFeed.ini"));
         antifeedoly.load(var3);
         ENABLE_OLY_FEED = Boolean.parseBoolean(antifeedoly.getProperty("OlympiadAntiFeedEnable", "False"));
         OLY_ANTI_FEED_WEAPON_RIGHT = Integer.parseInt(antifeedoly.getProperty("OlympiadAntiFeedRightWeapon", "0"));
         OLY_ANTI_FEED_WEAPON_LEFT = Integer.parseInt(antifeedoly.getProperty("OlympiadAntiFeedLeftWeapon", "0"));
         OLY_ANTI_FEED_GLOVES = Integer.parseInt(antifeedoly.getProperty("OlympiadAntiFeedGloves", "0"));
         OLY_ANTI_FEED_CHEST = Integer.parseInt(antifeedoly.getProperty("OlympiadAntiFeedChest", "0"));
         OLY_ANTI_FEED_LEGS = Integer.parseInt(antifeedoly.getProperty("OlympiadAntiFeedLegs", "0"));
         OLY_ANTI_FEED_FEET = Integer.parseInt(antifeedoly.getProperty("OlympiadAntiFeedFeet", "0"));
         OLY_ANTI_FEED_CLOAK = Integer.parseInt(antifeedoly.getProperty("OlympiadAntiFeedCloak", "0"));
         OLY_ANTI_FEED_RIGH_HAND_ARMOR = Integer.parseInt(antifeedoly.getProperty("OlympiadAntiFeedRightArmor", "0"));
         OLY_ANTI_FEED_HAIR_MISC_1 = Integer.parseInt(antifeedoly.getProperty("OlympiadAntiFeedHair1", "0"));
         OLY_ANTI_FEED_HAIR_MISC_2 = Integer.parseInt(antifeedoly.getProperty("OlympiadAntiFeedHair2", "0"));
         OLY_ANTI_FEED_RACE = Integer.parseInt(antifeedoly.getProperty("OlympiadAntiFeedRace", "0"));
         OLY_ANTI_FEED_GENDER = Integer.parseInt(antifeedoly.getProperty("OlympiadAntiFeedGender", "0"));
         OLY_ANTI_FEED_CLASS_RADIUS = Integer.parseInt(antifeedoly.getProperty("OlympiadAntiFeedClassRadius", "0"));
         OLY_ANTI_FEED_CLASS_HEIGHT = Integer.parseInt(antifeedoly.getProperty("OlympiadAntiFeedClassHeight", "0"));
         OLY_ANTI_FEED_PLAYER_HAVE_RECS = Integer.parseInt(antifeedoly.getProperty("OlympiadAntiFeedHaveRecs", "0"));
      } catch (Exception var2) {
         _log.warning("Config: " + var2.getMessage());
         throw new Error("Failed to Load ./config/mods/olympiadAntiFeed.ini File.");
      }
   }

   private static void loadGeodataSettings(InputStream is) {
      try {
         GameSettings geoSettings = new GameSettings();
         InputStream var3 = new FileInputStream(new File("./config/main/geodata.ini"));
         geoSettings.load(var3);
         GEODATA = Boolean.parseBoolean(geoSettings.getProperty("AllowGeoData", "False"));
         SYNC_BY_GEO = Boolean.parseBoolean(geoSettings.getProperty("AllowSyncByGeo", "False"));
         ACCEPT_GEOEDITOR_CONN = Boolean.parseBoolean(geoSettings.getProperty("AcceptGeoeditorConnect", "False"));
         CLIENT_SHIFTZ = Integer.parseInt(geoSettings.getProperty("GeoClientShiftZ", "16"));
         PATH_CLEAN = Boolean.parseBoolean(geoSettings.getProperty("PathFindClean", "True"));
         ALLOW_FALL_FROM_WALLS = Boolean.parseBoolean(geoSettings.getProperty("AllowFallFromWalls", "False"));
         PATHFIND_BOOST = Integer.parseInt(geoSettings.getProperty("PathFindBoost", "2"));
         PATHFIND_DIAGONAL = Boolean.parseBoolean(geoSettings.getProperty("PathFindDiagonal", "False"));
         PATHFIND_MAP_MUL = Integer.parseInt(geoSettings.getProperty("PathFindMapMul", "2"));
         MAX_Z_DIFF = Integer.parseInt(geoSettings.getProperty("MaxZDiff", "64"));
         MAX_WRONG_ATTEMPTS = Integer.parseInt(geoSettings.getProperty("MaxWrongAttempts", "3"));
         MIN_LAYER_HEIGHT = Integer.parseInt(geoSettings.getProperty("MinLayerHeight", "64"));
         REGION_EDGE_MAX_Z_DIFF = Integer.parseInt(geoSettings.getProperty("RegionEdgeMaxZDiff", "128"));
         PATHFIND_MAX_Z_DIFF = Integer.parseInt(geoSettings.getProperty("PathFindMaxZDiff", "32"));
         GEOFILES_PATTERN = geoSettings.getProperty("GeoFilesPattern", "(\\d{2}_\\d{2})\\.l2j");
         COMPACT_GEO = Boolean.parseBoolean(geoSettings.getProperty("CompactGeoData", "False"));
         PATHFIND_MAX_TIME = (long)Integer.parseInt(geoSettings.getProperty("PathFindMaxTime", "10000000"));
         PATHFIND_BUFFERS = geoSettings.getProperty("PathFindBuffers", "8x96;8x128;8x160;8x192;4x224;4x256;4x288;2x320;2x384;2x352;1x512");
         VERTICAL_SPLIT_REGIONS = geoSettings.getProperty("VerticalRegions", "20_21;22_16;22_25;23_18;25_14;25_15;25_19;24_23");
      } catch (Exception var2) {
         _log.warning("Config: " + var2.getMessage());
         throw new Error("Failed to Load ./config/main/geodata.ini File.");
      }
   }

   private static void loadAntiBotSettings(InputStream is) {
      try {
         GameSettings antiBotSettings = new GameSettings();
         InputStream var3 = new FileInputStream(new File("./config/mods/antiBot.ini"));
         antiBotSettings.load(var3);
         ENABLE_ANTI_BOT_SYSTEM = Boolean.parseBoolean(antiBotSettings.getProperty("EnableAntiBotSystem", "False"));
         ASK_ANSWER_DELAY = Integer.parseInt(antiBotSettings.getProperty("ASK_ANSWER_DELAY", "3"));
         MINIMUM_TIME_QUESTION_ASK = Integer.parseInt(antiBotSettings.getProperty("MinimumTimeQuestionAsk", "60"));
         MAXIMUM_TIME_QUESTION_ASK = Integer.parseInt(antiBotSettings.getProperty("MaximumTimeQuestionAsk", "120"));
         MINIMUM_BOT_POINTS_TO_STOP_ASKING = Integer.parseInt(antiBotSettings.getProperty("MinimumBotPointsToStopAsking", "10"));
         MAXIMUM_BOT_POINTS_TO_STOP_ASKING = Integer.parseInt(antiBotSettings.getProperty("MaximumBotPointsToStopAsking", "15"));
         MAX_BOT_POINTS = Integer.parseInt(antiBotSettings.getProperty("MaxBotPoints", "15"));
         MINIMAL_BOT_RATING_TO_BAN = Integer.parseInt(antiBotSettings.getProperty("MinimalBotPointsToBan", "-5"));
         ANNOUNCE_AUTO_BOT_BAN = Boolean.parseBoolean(antiBotSettings.getProperty("AnounceAutoBan", "False"));
         ON_WRONG_QUESTION_KICK = Boolean.parseBoolean(antiBotSettings.getProperty("IfWrongKick", "False"));
      } catch (Exception var2) {
         _log.warning("Config: " + var2.getMessage());
         throw new Error("Failed to Load ./config/mods/antiBot.ini File.");
      }
   }

   private static void loadCustomRatesSettings(InputStream is) {
      try {
         GameSettings customRatesSettings = new GameSettings();
         InputStream var3 = new FileInputStream(new File("./config/mods/customRates.ini"));
         customRatesSettings.load(var3);
         ALLOW_CUSTOM_RATES = Boolean.parseBoolean(customRatesSettings.getProperty("AllowCustomRates", "False"));
         MONDAY_RATE_EXP = Float.parseFloat(customRatesSettings.getProperty("CustomMondayExp", "2"));
         MONDAY_RATE_SP = Float.parseFloat(customRatesSettings.getProperty("CustomMondaySp", "2"));
         TUESDAY_RATE_EXP = Float.parseFloat(customRatesSettings.getProperty("CustomTuesdayExp", "2"));
         TUESDAY_RATE_SP = Float.parseFloat(customRatesSettings.getProperty("CustomTuesdaySp", "2"));
         WEDNESDAY_RATE_EXP = Float.parseFloat(customRatesSettings.getProperty("CustomWednesdayExp", "2"));
         WEDNESDAY_RATE_SP = Float.parseFloat(customRatesSettings.getProperty("CustomWednesdaySp", "2"));
         THURSDAY_RATE_EXP = Float.parseFloat(customRatesSettings.getProperty("CustomThursdayExp", "2"));
         THURSDAY_RATE_SP = Float.parseFloat(customRatesSettings.getProperty("CustomThursdaySp", "2"));
         FRIDAY_RATE_EXP = Float.parseFloat(customRatesSettings.getProperty("CustomFridayExp", "2"));
         FRIDAY_RATE_SP = Float.parseFloat(customRatesSettings.getProperty("CustomFridaySp", "2"));
         SATURDAY_RATE_EXP = Float.parseFloat(customRatesSettings.getProperty("CustomSaturdayExp", "2"));
         SATURDAY_RATE_SP = Float.parseFloat(customRatesSettings.getProperty("CustomSaturdaySp", "2"));
         SUNDAY_RATE_EXP = Float.parseFloat(customRatesSettings.getProperty("CustomSundayExp", "2"));
         SUNDAY_RATE_SP = Float.parseFloat(customRatesSettings.getProperty("CustomSundaySp", "2"));
      } catch (Exception var2) {
         _log.warning("Config: " + var2.getMessage());
         throw new Error("Failed to Load ./config/mods/customRates.ini File.");
      }
   }

   private static void loadFakeSettings(InputStream is) {
      try {
         GameSettings fakeSettings = new GameSettings();
         InputStream var10 = new FileInputStream(new File("./config/mods/fakes/fakePlayers.ini"));
         fakeSettings.load(var10);
         ALLOW_FAKE_PLAYERS = Boolean.parseBoolean(fakeSettings.getProperty("AllowFakePlayers", "False"));
         ALLOW_ENCHANT_WEAPONS = Boolean.parseBoolean(fakeSettings.getProperty("AllowEnchantWeapons", "False"));
         ALLOW_ENCHANT_ARMORS = Boolean.parseBoolean(fakeSettings.getProperty("AllowEnchantArmors", "False"));
         ALLOW_ENCHANT_JEWERLYS = Boolean.parseBoolean(fakeSettings.getProperty("AllowEnchantJewerlys", "False"));
         String[] propertySplit1 = fakeSettings.getProperty("RandomEnchatWeapon", "1,15").split(",");

         try {
            RND_ENCHANT_WEAPONS[0] = Integer.parseInt(propertySplit1[0]);
            RND_ENCHANT_WEAPONS[1] = Integer.parseInt(propertySplit1[1]);
         } catch (NumberFormatException var8) {
            if (propertySplit1.length > 0) {
               _log.warning("[Config.load()]: invalid config property -> RandomEnchatWeapon");
            }
         }

         String[] propertySplit2 = fakeSettings.getProperty("RandomEnchatArmor", "1,15").split(",");

         try {
            RND_ENCHANT_ARMORS[0] = Integer.parseInt(propertySplit2[0]);
            RND_ENCHANT_ARMORS[1] = Integer.parseInt(propertySplit2[1]);
         } catch (NumberFormatException var7) {
            if (propertySplit2.length > 0) {
               _log.warning("[Config.load()]: invalid config property -> RandomEnchatArmor");
            }
         }

         String[] propertySplit = fakeSettings.getProperty("RandomEnchatJewerly", "1,15").split(",");

         try {
            RND_ENCHANT_JEWERLYS[0] = Integer.parseInt(propertySplit[0]);
            RND_ENCHANT_JEWERLYS[1] = Integer.parseInt(propertySplit[1]);
         } catch (NumberFormatException var6) {
            if (propertySplit.length > 0) {
               _log.warning("[Config.load()]: invalid config property -> RandomEnchatJewerly");
            }
         }

         FAKE_FIGHTER_BUFFS = parseItemsList(fakeSettings.getProperty("FakeFighterBuffs", "1204,2"));
         FAKE_MAGE_BUFFS = parseItemsList(fakeSettings.getProperty("FakeMageBuffs", "1204,2"));
         ALLOW_SPAWN_FAKE_PLAYERS = Boolean.parseBoolean(fakeSettings.getProperty("AllowAutoSpawnFakes", "False"));
         ENCHANTERS_MAX_LVL = Integer.parseInt(fakeSettings.getProperty("FakeMaxEnchantItems", "20"));
         FAKE_PLAYERS_AMOUNT = Integer.parseInt(fakeSettings.getProperty("FakePlayersAmount", "50"));
      } catch (Exception var9) {
         _log.warning("Config: " + var9.getMessage());
         throw new Error("Failed to Load ./config/mods/fakes/fakePlayers.ini File.");
      }
   }

   private static void loadFightEventsSettings(InputStream is) {
      try {
         GameSettings fightEventSettings = new GameSettings();
         InputStream var5 = new FileInputStream(new File("./config/events/fightEvents.ini"));
         fightEventSettings.load(var5);
         ALLOW_FIGHT_EVENTS = Boolean.parseBoolean(fightEventSettings.getProperty("AllowFightEvents", "False"));
         ALLOW_RESPAWN_PROTECT_PLAYER = Boolean.parseBoolean(fightEventSettings.getProperty("AllowRespawnProtect", "False"));
         ALLOW_REG_CONFIRM_DLG = Boolean.parseBoolean(fightEventSettings.getProperty("AllowRegisterAtConfirmDlg", "True"));
         String[] eventList = fightEventSettings.getProperty("NotAllowedFightEvents", "0").split(",");
         DISALLOW_FIGHT_EVENTS = new int[eventList.length];

         for(int i = 0; i < eventList.length; ++i) {
            DISALLOW_FIGHT_EVENTS[i] = Integer.parseInt(eventList[i]);
         }

         Arrays.sort(DISALLOW_FIGHT_EVENTS);
         FIGHT_EVENTS_EQUALIZE_ROOMS = Boolean.parseBoolean(fightEventSettings.getProperty("FightEventsEqualizeRooms", "False"));
         FIGHT_EVENTS_MIN_LVL = Integer.parseInt(fightEventSettings.getProperty("FightEventsMinLevel", "76"));
         FIGHT_EVENTS_REWARD_MULTIPLIER = Integer.parseInt(fightEventSettings.getProperty("RewardMultiplier", "2"));
         TIME_FIRST_TELEPORT = Integer.parseInt(fightEventSettings.getProperty("TimeFirstTeleport", "10"));
         TIME_PLAYER_TELEPORTING = Integer.parseInt(fightEventSettings.getProperty("TimeTeleportPlayers", "15"));
         TIME_PREPARATION_BEFORE_FIRST_ROUND = Integer.parseInt(fightEventSettings.getProperty("TimeBeforeFirstRound", "30"));
         TIME_PREPARATION_BETWEEN_NEXT_ROUNDS = Integer.parseInt(fightEventSettings.getProperty("TimeBeforeNextRound", "30"));
         TIME_AFTER_ROUND_END_TO_RETURN_SPAWN = Integer.parseInt(fightEventSettings.getProperty("TimeAfterRoundEnd", "15"));
         TIME_TELEPORT_BACK_TOWN = Integer.parseInt(fightEventSettings.getProperty("TeleportTimeBackTown", "30"));
         TIME_MAX_SECONDS_OUTSIDE_ZONE = Integer.parseInt(fightEventSettings.getProperty("TimeOutsideZone", "10"));
         TIME_TO_BE_AFK = Integer.parseInt(fightEventSettings.getProperty("TimeToBeAfk", "120"));
         ITEMS_FOR_MINUTE_OF_AFK = Integer.parseInt(fightEventSettings.getProperty("ItemsForMinOfAfk", "-1"));
      } catch (Exception var4) {
         _log.warning("Config: " + var4.getMessage());
         throw new Error("Failed to Load ./config/events/fightEvents.ini File.");
      }
   }

   private static void loadChatSettings(InputStream is) {
      try {
         GameSettings chatSettings = new GameSettings();
         InputStream var10 = new FileInputStream(new File("./config/main/chat.ini"));
         chatSettings.load(var10);
         DEFAULT_GLOBAL_CHAT = chatSettings.getProperty("GlobalChat", "ON");
         DEFAULT_TRADE_CHAT = chatSettings.getProperty("TradeChat", "ON");
         USE_SAY_FILTER = Boolean.parseBoolean(chatSettings.getProperty("UseChatFilter", "False"));
         USE_BROADCAST_SAY_FILTER = Boolean.parseBoolean(chatSettings.getProperty("UseBroadCastChatFilter", "False"));
         CHAT_FILTER_CHARS = chatSettings.getProperty("ChatFilterChars", "^_^");
         String[] chatSplit = chatSettings.getProperty("BanChatChannels", "0;1;8;17").trim().split(";");
         BAN_CHAT_CHANNELS = new int[chatSplit.length];

         try {
            int i = 0;

            for(String chatId : chatSplit) {
               BAN_CHAT_CHANNELS[i++] = Integer.parseInt(chatId);
            }
         } catch (NumberFormatException var8) {
            _log.log(Level.WARNING, var8.getMessage(), (Throwable)var8);
         }

         ALLOW_CUSTOM_CHAT = Boolean.parseBoolean(chatSettings.getProperty("AllowCustomChat", "False"));
         CHECK_CHAT_VALID = Integer.parseInt(chatSettings.getProperty("CheckValidCustomChat", "0"));
         CHAT_MSG_SIMPLE = Integer.parseInt(chatSettings.getProperty("ChatMessageSimpleAcc", "20"));
         CHAT_MSG_PREMIUM = Integer.parseInt(chatSettings.getProperty("ChatMessagePremiumAcc", "40"));
         CHAT_MSG_ANNOUNCE = Integer.parseInt(chatSettings.getProperty("ChatAnnounceMessage", "5"));
         MIN_LVL_GLOBAL_CHAT = Integer.parseInt(chatSettings.getProperty("GlobalChatMinLevel", "1"));
      } catch (Exception var9) {
         _log.warning("Config: " + var9.getMessage());
         throw new Error("Failed to Load ./config/main/chat.ini File.");
      }
   }

   private static void loadStreamersSettings(InputStream is) {
      try {
         GameSettings streamSettings = new GameSettings();
         InputStream var3 = new FileInputStream(new File("./config/mods/streamers.ini"));
         streamSettings.load(var3);
         ALLOW_STREAM_SYSTEM = Boolean.parseBoolean(streamSettings.getProperty("AllowStreamingSystem", "False"));
         ALLOW_STREAM_LOGS = Boolean.parseBoolean(streamSettings.getProperty("AllowStreamLogs", "False"));
         TWITCH_CLIENT_ID = streamSettings.getProperty("TwitchClientId", "");
         TWITCH_CHECK_DELAY = Integer.parseInt(streamSettings.getProperty("StreamCheckTwitchDelay", "60000"));
         TWITCH_ACTIVE_STREAMS_URL = streamSettings.getProperty("TwitchActiveStreamsURL", "");
         ALLOW_STREAM_GAME_NAME = Boolean.parseBoolean(streamSettings.getProperty("TwitchActiveStreamsURLWithGameName", "False"));
         TWITCH_CORRECT_STREAM_GAME = streamSettings.getProperty("TwitchCorrectStreamGame", "");
         TWITCH_CORRECT_STREAM_TITLE = streamSettings.getProperty("TwitchCorrectStreamTitlesContainArray", "");
         ALLOW_STREAM_PANEL = Boolean.parseBoolean(streamSettings.getProperty("AllowStreamPanel", "False"));
         ALLOW_STREAM_SAVE_DB = Boolean.parseBoolean(streamSettings.getProperty("StreamConnectionSavedInDB", "True"));
         ALLOW_STREAM_NEED_APPROVAL = Boolean.parseBoolean(streamSettings.getProperty("StreamConnectionNeedsApproval", "False"));
         ALLOW_STREAM_SAME_DB = Boolean.parseBoolean(streamSettings.getProperty("StreamTableInGameServerDB", "True"));
         STREAM_DB_URL = streamSettings.getProperty("StreamTableDBURL", "jdbc:mysql://localhost/l2e");
         STREAM_DB_LOGIN = streamSettings.getProperty("StreamTableDBLogin", "root");
         STREAM_DB_PASS = streamSettings.getProperty("StreamTableDBPassword", "12345");
         ALLOW_REWARD_STREAMERS = Boolean.parseBoolean(streamSettings.getProperty("AllowRewardingStreamers", "True"));
         TIME_TO_REWARD_STREAMERS = Integer.parseInt(streamSettings.getProperty("StreamingSecondsToReward", "7200"));
         MAX_SEC_TO_REWARD_STREAMERS = Integer.parseInt(streamSettings.getProperty("StreamingMaxRewardedSecondsPerDay", "28800"));
         TOTAL_REWARD_STREAMERS = Integer.parseInt(streamSettings.getProperty("StreamingTotalRewardedTimeClearHour", "7"));
         STREAMING_REWARDS = streamSettings.getProperty("StreamingRewards", "3,6673,6;0,6673,2");
         ALLOW_INCREASE_REWARD = Boolean.parseBoolean(streamSettings.getProperty("StreamIncreaseRewardWhileOffline", "False"));
         ALLOW_STREAM_AFK_SYSTEM = Boolean.parseBoolean(streamSettings.getProperty("AllowStreamingAFKSystem", "True"));
         STREAM_AFK_SECONDS = Integer.parseInt(streamSettings.getProperty("StreamingAFKSystemSecondsToAFK", "60"));
         STREAM_AFK_DELAY = Integer.parseInt(streamSettings.getProperty("StreamingAFKSystemDelayBetweenMsgs", "10"));
      } catch (Exception var2) {
         _log.warning("Config: " + var2.getMessage());
         throw new Error("Failed to Load ./config/mods/streamers.ini File.");
      }
   }

   private static void loadFacebookSettings(InputStream is) {
      try {
         GameSettings facebookSettings = new GameSettings();
         InputStream var17 = new FileInputStream(new File("./config/mods/facebookRewards.ini"));
         facebookSettings.load(var17);
         ALLOW_FACEBOOK_SYSTEM = Boolean.parseBoolean(facebookSettings.getProperty("AllowFacebookRewardSystem", "False"));
         FACEBOOK_MIN_LVL = Integer.parseInt(facebookSettings.getProperty("FacebookConnectMinLevel", "76"));
         FACEBOOK_NAME_SYNTAX = facebookSettings.getProperty("FacebookRegistrationNicknameSyntax", "");
         ALLOW_REG_EXACT_COMMENTS = Boolean.parseBoolean(facebookSettings.getProperty("FacebookRegistrationOnlyExactComment", "False"));
         FACEBOOK_TIME_LIMIT = Integer.parseInt(facebookSettings.getProperty("FacebookTimeLimit", "900"));
         FACEBOOK_TIME_LIMIT_DELAY = Integer.parseInt(facebookSettings.getProperty("FacebookTimeLimitThreadDelay", "5000"));
         FACEBOOK_DELAY_BETWEEN_TASK = Integer.parseInt(facebookSettings.getProperty("FacebookDelayBetweenTasks", "10800"));
         FACEBOOK_COMMENT_REUSE = Integer.parseInt(facebookSettings.getProperty("FacebookCommentContentReuse", "1800"));
         if (ALLOW_FACEBOOK_SYSTEM) {
            String[] propertySplit = facebookSettings.getProperty("FacebookRewardLikeTask", "").split(";");
            FACEBOOK_REWARD_LIKE_TASK = new HashMap<>(propertySplit.length);

            for(String reward : propertySplit) {
               String[] rewardSplit = reward.split(",");
               if (rewardSplit.length != 2) {
                  _log.warning("[facebookRewards]: invalid config property -> FacebookRewardLikeTask " + reward + "");
               } else {
                  try {
                     FACEBOOK_REWARD_LIKE_TASK.put(Integer.parseInt(rewardSplit[0]), Long.parseLong(rewardSplit[1]));
                  } catch (NumberFormatException var15) {
                     if (!reward.isEmpty()) {
                        _log.warning("[facebookRewards]: invalid config property -> FacebookRewardLikeTask " + rewardSplit[0] + " " + rewardSplit[1] + "");
                     }
                  }
               }
            }

            String[] propertySplit3 = facebookSettings.getProperty("FacebookRewardLikeNoTask", "").split(";");
            FACEBOOK_REWARD_LIKE_NO_TASK = new HashMap<>(propertySplit3.length);

            for(String reward : propertySplit3) {
               String[] rewardSplit = reward.split(",");
               if (rewardSplit.length != 2) {
                  _log.warning("[facebookRewards]: invalid config property -> FacebookRewardLikeNoTask " + reward + "");
               } else {
                  try {
                     FACEBOOK_REWARD_LIKE_NO_TASK.put(Integer.parseInt(rewardSplit[0]), Long.parseLong(rewardSplit[1]));
                  } catch (NumberFormatException var14) {
                     if (!reward.isEmpty()) {
                        _log.warning("[facebookRewards]: invalid config property -> FacebookRewardLikeNoTask " + rewardSplit[0] + " " + rewardSplit[1] + "");
                     }
                  }
               }
            }

            String[] propertySplit1 = facebookSettings.getProperty("FacebookRewardCommentNoTask", "").split(";");
            FACEBOOK_REWARD_COMMENT_NO_TASK = new HashMap<>(propertySplit1.length);

            for(String reward : propertySplit1) {
               String[] rewardSplit = reward.split(",");
               if (rewardSplit.length != 2) {
                  _log.warning("[facebookRewards]: invalid config property -> FacebookRewardCommentNoTask " + reward + "");
               } else {
                  try {
                     FACEBOOK_REWARD_COMMENT_NO_TASK.put(Integer.parseInt(rewardSplit[0]), Long.parseLong(rewardSplit[1]));
                  } catch (NumberFormatException var13) {
                     if (!reward.isEmpty()) {
                        _log.warning("[facebookRewards]: invalid config property -> FacebookRewardCommentNoTask " + rewardSplit[0] + " " + rewardSplit[1] + "");
                     }
                  }
               }
            }

            String[] propertySplit2 = facebookSettings.getProperty("FacebookRewardCommentTask", "").split(";");
            FACEBOOK_REWARD_COMMENT_TASK = new HashMap<>(propertySplit2.length);

            for(String reward : propertySplit2) {
               String[] rewardSplit = reward.split(",");
               if (rewardSplit.length != 2) {
                  _log.warning("[facebookRewards]: invalid config property -> FacebookRewardCommentTask " + reward + "");
               } else {
                  try {
                     FACEBOOK_REWARD_COMMENT_TASK.put(Integer.parseInt(rewardSplit[0]), Long.parseLong(rewardSplit[1]));
                  } catch (NumberFormatException var12) {
                     if (!reward.isEmpty()) {
                        _log.warning("[facebookRewards]: invalid config property -> FacebookRewardCommentTask " + rewardSplit[0] + " " + rewardSplit[1] + "");
                     }
                  }
               }
            }
         }

         ALLOW_AUTO_ANNONCE_NOT_CONNECT = Boolean.parseBoolean(facebookSettings.getProperty("FacebookAllowAutoAnnouncementNotConnected", "True"));
         FACEBOOK_AUTO_ANNONCE_NOT_DEDAY = Integer.parseInt(facebookSettings.getProperty("FacebookAutoAnnouncementNotConnectedDelay", "420"));
         ALLOW_AUTO_ANNONCE_CONNECTED = Boolean.parseBoolean(facebookSettings.getProperty("FacebookAllowAutoAnnouncementConnected", "True"));
         FACEBOOK_AUTO_ANNONCE_DEDAY = Integer.parseInt(facebookSettings.getProperty("FacebookAutoAnnouncementConnectedDelay", "240"));
         ALLOW_NEGATIVE_POINTS = Boolean.parseBoolean(facebookSettings.getProperty("FacebookAllowAutoAnnounceNegativePoints", "True"));
         FACEBOOK_NEGATIVE_POINTS_DELAY = Integer.parseInt(facebookSettings.getProperty("FacebookAutoAnnouncementNegativePointsDelay", "60"));
         FACEBOOK_EXTRACTION_DELAY = Integer.parseInt(facebookSettings.getProperty("FacebookExtractionDelay", "3000"));
         FACEBOOK_VALID_POST_COUNT = Integer.parseInt(facebookSettings.getProperty("FacebookValidPostsCount", "3"));
         FACEBOOK_PAGE_NAME = facebookSettings.getProperty("FacebookPageName", "");
         FACEBOOK_TOKEN = facebookSettings.getProperty("FacebookToken", "");
      } catch (Exception var16) {
         _log.warning("Config: " + var16.getMessage());
         throw new Error("Failed to Load ./config/mods/facebookRewards.ini File.");
      }
   }

   private static void loadWeeklyTraderSettings(InputStream is) {
      try {
         GameSettings weeklySettings = new GameSettings();
         InputStream var3 = new FileInputStream(new File("./config/mods/weeklyTrader.ini"));
         weeklySettings.load(var3);
         WEEKLY_TRADER_ENABLE = Boolean.parseBoolean(weeklySettings.getProperty("EnableWeeklyTrader", "False"));
         WEEKLY_TRADER_DAY_OF_WEEK = Integer.parseInt(weeklySettings.getProperty("DayOfWeek", "0"));
         WEEKLY_TRADER_HOUR_OF_DAY = Integer.parseInt(weeklySettings.getProperty("HourOfDay", "0"));
         WEEKLY_TRADER_MINUTE_OF_DAY = Integer.parseInt(weeklySettings.getProperty("MinuteOfDay", "0"));
         WEEKLY_TRADER_DURATION = Integer.parseInt(weeklySettings.getProperty("Duration", "120"));
         WEEKLY_TRADER_MULTISELL_ID = Integer.parseInt(weeklySettings.getProperty("MultisellId", "9999999"));
      } catch (Exception var2) {
         _log.warning("Config: " + var2.getMessage());
         throw new Error("Failed to Load ./config/mods/weeklyTrader.ini File.");
      }
   }

   private static void loadModsSettings() {
      ALLOW_DAILY_REWARD = isAllowDailyReward();
      ALLOW_DAILY_TASKS = isAllowDailyTask();
      ALLOW_VISUAL_SYSTEM = isAllowVisual();
      ALLOW_VIP_SYSTEM = isAllowVipSystem();
      ALLOW_REVENGE_SYSTEM = isAllowRevengeSystem();
   }

   private static void loadFloodProtectorConfigs(GameSettings properties) {
      FLOOD_PROTECTORS.clear();
      String[] floodProtectorTypes = properties.getProperty("FLOOD_PROTECTORS_TYPES", "").split(";");

      for(String type : floodProtectorTypes) {
         if (!StringUtils.isEmpty(type)) {
            FloodProtectorConfig floodProtector = FloodProtectorConfig.load(type, properties);
            if (floodProtector != null) {
               FLOOD_PROTECTORS.add(floodProtector);
            }
         }
      }
   }

   public static int getServerTypeId(String[] serverTypes) {
      int tType = 0;

      for(String cType : serverTypes) {
         cType = cType.trim();
         if (cType.equalsIgnoreCase("Normal")) {
            tType |= 1;
         } else if (cType.equalsIgnoreCase("Relax")) {
            tType |= 2;
         } else if (cType.equalsIgnoreCase("Test")) {
            tType |= 4;
         } else if (cType.equalsIgnoreCase("NoLabel")) {
            tType |= 8;
         } else if (cType.equalsIgnoreCase("Restricted")) {
            tType |= 16;
         } else if (cType.equalsIgnoreCase("Event")) {
            tType |= 32;
         } else if (cType.equalsIgnoreCase("Free")) {
            tType |= 64;
         }
      }

      return tType;
   }

   private static Map<Integer, Float> parseConfigLine(String line) {
      String[] propertySplit = line.split(",");
      Map<Integer, Float> ret = new HashMap<>(propertySplit.length);
      int i = 0;

      for(String value : propertySplit) {
         ret.put(i++, Float.parseFloat(value));
      }

      return ret;
   }

   private static int[][] parseItemsList(String line) {
      String[] propertySplit = line.split(";");
      if (propertySplit.length == 0) {
         return (int[][])null;
      } else {
         int i = 0;
         int[][] result = new int[propertySplit.length][];

         for(String value : propertySplit) {
            String[] valueSplit = value.split(",");
            if (valueSplit.length != 2) {
               _log.warning("[Config.load()]: invalid parseItemsList entry -> " + valueSplit[0] + ", should be itemId,itemNumber");
               return (int[][])null;
            }

            result[i] = new int[2];

            try {
               result[i][0] = Integer.parseInt(valueSplit[0]);
            } catch (NumberFormatException var11) {
               _log.warning("[Config.load()]: invalid parseItemsList itemId -> " + valueSplit[0] + "");
               return (int[][])null;
            }

            try {
               result[i][1] = Integer.parseInt(valueSplit[1]);
            } catch (NumberFormatException var10) {
               _log.warning("[Config.load()]: invalid parseItemsList item number -> " + valueSplit[1] + "");
               return (int[][])null;
            }

            ++i;
         }

         return result;
      }
   }

   private static boolean isAllowDailyTask() {
      return new File(DATAPACK_ROOT + "/data/scripts/services/DailyTask.java").exists();
   }

   private static boolean isAllowDailyReward() {
      return new File(DATAPACK_ROOT + "/data/scripts/services/DailyReward.java").exists();
   }

   private static boolean isAllowVisual() {
      return new File(DATAPACK_ROOT + "/data/scripts/services/VisualMe.java").exists();
   }

   private static boolean isAllowVipSystem() {
      return new File(DATAPACK_ROOT + "/data/scripts/services/CommunityVip.java").exists();
   }

   private static boolean isAllowRevengeSystem() {
      return new File(DATAPACK_ROOT + "/data/scripts/services/RevengeCmd.java").exists();
   }

   public static HashMap<String, String> getPersonalConfigs() {
      return _personalConfigs;
   }

   public static enum IdFactoryType {
      Compaction,
      BitSet,
      Stack;
   }
}
