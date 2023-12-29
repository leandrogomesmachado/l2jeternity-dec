package l2e.gameserver.model.actor;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import l2e.commons.util.Broadcast;
import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.SevenSigns;
import l2e.gameserver.SevenSignsFestival;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.ai.npc.Corpse;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.data.parser.CategoryParser;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.handler.bypasshandlers.BypassHandler;
import l2e.gameserver.handler.bypasshandlers.IBypassHandler;
import l2e.gameserver.handler.communityhandlers.CommunityBoardHandler;
import l2e.gameserver.handler.communityhandlers.ICommunityBoardHandler;
import l2e.gameserver.instancemanager.CHSiegeManager;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.instancemanager.TownManager;
import l2e.gameserver.instancemanager.WalkingManager;
import l2e.gameserver.model.CategoryType;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.MinionList;
import l2e.gameserver.model.ShotType;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.instance.ClanHallManagerInstance;
import l2e.gameserver.model.actor.instance.DoormenInstance;
import l2e.gameserver.model.actor.instance.FishermanInstance;
import l2e.gameserver.model.actor.instance.GrandBossInstance;
import l2e.gameserver.model.actor.instance.MerchantInstance;
import l2e.gameserver.model.actor.instance.RaidBossInstance;
import l2e.gameserver.model.actor.instance.TeleporterInstance;
import l2e.gameserver.model.actor.instance.TrainerInstance;
import l2e.gameserver.model.actor.instance.WarehouseInstance;
import l2e.gameserver.model.actor.stat.NpcStat;
import l2e.gameserver.model.actor.status.NpcStatus;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.actor.templates.items.Weapon;
import l2e.gameserver.model.actor.templates.npc.Faction;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.model.entity.clanhall.SiegableHall;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.model.stats.NpcStats;
import l2e.gameserver.model.zone.type.TownZone;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExChangeNpcState;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.gameserver.network.serverpackets.MoveToLocation;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.NpcInfo;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.gameserver.network.serverpackets.ServerObjectInfo;
import l2e.gameserver.network.serverpackets.SocialAction;
import l2e.gameserver.taskmanager.DecayTaskManager;

public class Npc extends Creature {
   public static final int INTERACTION_DISTANCE = 150;
   private Spawner _spawn;
   private boolean _isBusy = false;
   private String _busyMessage = "";
   private volatile boolean _isDecayed = false;
   private int _castleIndex = -2;
   private int _fortIndex = -2;
   private boolean _isRandomAnimationEnabled = true;
   private boolean _isHasNoChatWindow = false;
   private boolean _eventMob = false;
   private boolean _isInTown = false;
   private boolean _isAutoAttackable = false;
   private boolean _isRunner = false;
   private boolean _isSpecialCamera = false;
   private boolean _isEkimusFood = false;
   private String _showBoard = "";
   private long _lastSocialBroadcast = 0L;
   private final int _minimalSocialInterval = 6000;
   protected Npc.RandomAnimationTask _rAniTask = null;
   private int _currentLHandId;
   private int _currentRHandId;
   private int _currentEnchant;
   private double _currentCollisionHeight;
   private double _currentCollisionRadius;
   private int _displayEffect = 0;
   private Creature _summoner = null;
   private int _shotsMask = 0;
   private TerritoryWarManager.Territory _nearestTerritory;

   public Faction getFaction() {
      return this.getTemplate().getFaction();
   }

   public boolean isInFaction(Attackable npc) {
      return this.getFaction().equals(npc.getFaction()) && !this.getFaction().isIgnoreNpcId(npc.getId());
   }

   public void onRandomAnimation(int animationId) {
      long now = System.currentTimeMillis();
      if (now - this._lastSocialBroadcast > 6000L) {
         this._lastSocialBroadcast = now;
         this.broadcastPacket(new SocialAction(this.getObjectId(), animationId));
      }
   }

   public void startRandomAnimationTimer() {
      if (this.hasRandomAnimation() && this.getId() != 32705) {
         int minWait = this.isMob() ? Config.MIN_MONSTER_ANIMATION : Config.MIN_NPC_ANIMATION;
         int maxWait = this.isMob() ? Config.MAX_MONSTER_ANIMATION : Config.MAX_NPC_ANIMATION;
         int interval = Rnd.get(minWait, maxWait) * 1000;
         this._rAniTask = new Npc.RandomAnimationTask(this);
         ThreadPoolManager.getInstance().schedule(this._rAniTask, (long)interval);
      }
   }

   public Npc(int objectId, NpcTemplate template) {
      super(objectId, template);
      if (template == null) {
         _log.severe("No template for Npc. Please check your datapack is setup correctly.");
      } else {
         this.setInstanceType(GameObject.InstanceType.Npc);
         this.initCharStatusUpdateValues();
         this._currentLHandId = this.getTemplate().getLeftHand();
         this._currentRHandId = this.getTemplate().getRightHand();
         this._currentEnchant = Config.ENABLE_RANDOM_ENCHANT_EFFECT ? Rnd.get(4, 21) : this.getTemplate().getEnchantEffect();
         this._currentCollisionHeight = this.getTemplate().getfCollisionHeight();
         this._currentCollisionRadius = this.getTemplate().getfCollisionRadius();
         this.setName(template.getName());
         this.setNameRu(template.getNameRu());
         if (template.isImmobilized()) {
            this.setIsImmobilized(true);
         }

         if (template.getRandomWalk()) {
            this.setIsNoRndWalk(true);
         }

         if (template.getRandomAnimation()) {
            this.setRandomAnimationEnabled(false);
         }

         if (template.isHasNoChatWindow()) {
            this._isHasNoChatWindow = true;
         }

         this._showBoard = template.getParameter("showBoard", "");
      }
   }

   public NpcStat getStat() {
      return (NpcStat)super.getStat();
   }

   @Override
   public void initCharStat() {
      this.setStat(new NpcStat(this));
   }

   public NpcStatus getStatus() {
      return (NpcStatus)super.getStatus();
   }

   @Override
   public void initCharStatus() {
      this.setStatus(new NpcStatus(this));
   }

   public final NpcTemplate getTemplate() {
      return (NpcTemplate)super.getTemplate();
   }

   @Override
   public int getId() {
      return this.getTemplate().getId();
   }

   @Override
   public final int getLevel() {
      return this.getTemplate().getLevel();
   }

   public boolean isAggressive() {
      return false;
   }

   public int getAggroRange() {
      return this.getTemplate().getAggroRange();
   }

   public int getHideAggroRange() {
      return this.getTemplate().getHideAggroRange();
   }

   @Override
   public boolean isUndead() {
      return this.getTemplate().isUndead();
   }

   @Override
   public void updateAbnormalEffect() {
      this.broadcastInfo();
   }

   public boolean isEventMob() {
      return this._eventMob;
   }

   public void setEventMob(boolean val) {
      this._eventMob = val;
   }

   @Override
   public boolean isAutoAttackable(Creature attacker) {
      return this._isAutoAttackable;
   }

   public void setAutoAttackable(boolean flag) {
      this._isAutoAttackable = flag;
   }

   public int getLeftHandItem() {
      return this._currentLHandId;
   }

   public int getRightHandItem() {
      return this._currentRHandId;
   }

   public int getEnchantEffect() {
      return this._currentEnchant;
   }

   public final boolean isBusy() {
      return this._isBusy;
   }

   public void setBusy(boolean isBusy) {
      this._isBusy = isBusy;
   }

   public final String getBusyMessage() {
      return this._busyMessage;
   }

   public void setBusyMessage(String message) {
      this._busyMessage = message;
   }

   public boolean isWarehouse() {
      return false;
   }

   public void setIsHasNoChatWindow(boolean isHasNoChatWindow) {
      this._isHasNoChatWindow = isHasNoChatWindow;
   }

   public boolean isHasNoChatWindow() {
      return this._isHasNoChatWindow;
   }

   public boolean canTarget(Player player) {
      if (!this.isTargetable()) {
         player.sendActionFailed();
         return false;
      } else if (player.isOutOfControl()) {
         player.sendActionFailed();
         return false;
      } else if (player.isLockedTarget() && player.getLockedTarget() != this) {
         player.sendPacket(SystemMessageId.FAILED_CHANGE_TARGET);
         player.sendActionFailed();
         return false;
      } else {
         return true;
      }
   }

   public boolean canInteract(Player player) {
      if (player.isCastingNow() || player.isCastingSimultaneouslyNow()) {
         return false;
      } else if (player.isDead() || player.isFakeDeathNow()) {
         return false;
      } else if (player.isSitting()) {
         return false;
      } else if (player.getPrivateStoreType() != 0) {
         return false;
      } else if (!this.isInsideRadius(player, 150, true, false)) {
         return false;
      } else if (player.getReflectionId() != this.getReflectionId()) {
         return false;
      } else {
         return !this.isBusy();
      }
   }

   public final Castle getCastle() {
      if (this._castleIndex < 0) {
         TownZone town = TownManager.getTown(this.getX(), this.getY(), this.getZ());
         if (town != null) {
            this._castleIndex = CastleManager.getInstance().getCastleIndex(town.getTaxById());
         }

         if (this._castleIndex < 0) {
            this._castleIndex = CastleManager.getInstance().findNearestCastleIndex(this);
         } else {
            this._isInTown = true;
         }
      }

      return this._castleIndex < 0 ? null : CastleManager.getInstance().getCastles().get(this._castleIndex);
   }

   public boolean isMyLord(Player player) {
      if (player.isClanLeader()) {
         int castleId = this.getCastle() != null ? this.getCastle().getId() : -1;
         int fortId = this.getFort() != null ? this.getFort().getId() : -1;
         return player.getClan().getCastleId() == castleId || player.getClan().getFortId() == fortId;
      } else {
         return false;
      }
   }

   public final SiegableHall getConquerableHall() {
      return CHSiegeManager.getInstance().getNearbyClanHall(this.getX(), this.getY(), 10000);
   }

   public final Castle getCastle(long maxDistance) {
      int index = CastleManager.getInstance().findNearestCastleIndex(this, maxDistance);
      return index < 0 ? null : CastleManager.getInstance().getCastles().get(index);
   }

   public final Fort getFort() {
      if (this._fortIndex < 0) {
         Fort fort = FortManager.getInstance().getFort(this.getX(), this.getY(), this.getZ());
         if (fort != null) {
            this._fortIndex = FortManager.getInstance().getFortIndex(fort.getId());
         }

         if (this._fortIndex < 0) {
            this._fortIndex = FortManager.getInstance().findNearestFortIndex(this);
         }
      }

      return this._fortIndex < 0 ? null : FortManager.getInstance().getForts().get(this._fortIndex);
   }

   public final Fort getFort(long maxDistance) {
      int index = FortManager.getInstance().findNearestFortIndex(this, maxDistance);
      return index < 0 ? null : FortManager.getInstance().getForts().get(index);
   }

   public final boolean getIsInTown() {
      if (this._castleIndex < 0) {
         this.getCastle();
      }

      return this._isInTown;
   }

   public void onBypassFeedback(Player player, String command) {
      if (player.isActionsDisabled()) {
         player.sendActionFailed();
      } else {
         if (this.isBusy() && this.getBusyMessage().length() > 0) {
            player.sendActionFailed();
            NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
            html.setFile(player, player.getLang(), "data/html/npcbusy.htm");
            html.replace("%busymessage%", this.getBusyMessage());
            html.replace("%npcname%", this.getName());
            html.replace("%playername%", player.getName());
            player.sendPacket(html);
         } else {
            IBypassHandler handler = BypassHandler.getInstance().getHandler(command);
            if (handler != null) {
               handler.useBypass(command, player, this);
            } else {
               _log.info(this.getClass().getSimpleName() + ": Unknown NPC bypass: \"" + command + "\" NpcId: " + this.getId());
            }
         }
      }
   }

   @Override
   public ItemInstance getActiveWeaponInstance() {
      return null;
   }

   @Override
   public Weapon getActiveWeaponItem() {
      int weaponId = this.getTemplate().getRightHand();
      if (weaponId < 1) {
         return null;
      } else {
         Item item = ItemsParser.getInstance().getTemplate(this.getTemplate().getRightHand());
         return !(item instanceof Weapon) ? null : (Weapon)item;
      }
   }

   @Override
   public ItemInstance getSecondaryWeaponInstance() {
      return null;
   }

   public Weapon getSecondaryWeaponItem() {
      int weaponId = this.getTemplate().getLeftHand();
      if (weaponId < 1) {
         return null;
      } else {
         Item item = ItemsParser.getInstance().getTemplate(this.getTemplate().getLeftHand());
         return !(item instanceof Weapon) ? null : (Weapon)item;
      }
   }

   public void insertObjectIdAndShowChatWindow(Player player, String content) {
      content = content.replaceAll("%objectId%", String.valueOf(this.getObjectId()));
      NpcHtmlMessage npcReply = new NpcHtmlMessage(this.getObjectId());
      npcReply.setHtml(player, content);
      player.sendPacket(npcReply);
   }

   public String getHtmlPath(int npcId, int val) {
      String pom = "";
      if (val == 0) {
         pom = "" + npcId;
      } else {
         pom = npcId + "-" + val;
      }

      String temp = "data/html/default/" + pom + ".htm";
      if (!Config.LAZY_CACHE) {
         if (HtmCache.getInstance().contains(temp)) {
            return temp;
         }
      } else if (HtmCache.getInstance().isLoadable(temp)) {
         return temp;
      }

      return "data/html/npcdefault.htm";
   }

   public void showChatWindow(Player player) {
      this.showChatWindow(player, 0);
   }

   private boolean showPkDenyChatWindow(Player player, String type) {
      String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/" + type + "/" + this.getId() + "-pk.htm");
      if (html != null) {
         NpcHtmlMessage pkDenyMsg = new NpcHtmlMessage(this.getObjectId());
         pkDenyMsg.setHtml(player, html);
         player.sendPacket(pkDenyMsg);
         player.sendActionFailed();
         return true;
      } else {
         return false;
      }
   }

   public void showChatWindow(Player player, int val) {
      if (this.isHasNoChatWindow()) {
         player.sendActionFailed();
      } else if (!this._showBoard.isEmpty()) {
         ICommunityBoardHandler handler = CommunityBoardHandler.getInstance().getHandler(this._showBoard);
         if (handler != null) {
            handler.onBypassCommand(this._showBoard, player);
         }
      } else if (!player.isCursedWeaponEquipped() || player.getTarget() instanceof ClanHallManagerInstance && player.getTarget() instanceof DoormenInstance) {
         if (player.getKarma() > 0) {
            if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && this instanceof MerchantInstance) {
               if (this.showPkDenyChatWindow(player, "merchant")) {
                  return;
               }
            } else if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_GK && this instanceof TeleporterInstance) {
               if (this.showPkDenyChatWindow(player, "teleporter")) {
                  return;
               }
            } else if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE && this instanceof WarehouseInstance) {
               if (this.showPkDenyChatWindow(player, "warehouse")) {
                  return;
               }
            } else if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && this instanceof FishermanInstance && this.showPkDenyChatWindow(player, "fisherman")) {
               return;
            }
         }

         if (!this.getTemplate().isType("Auctioneer") || val != 0) {
            int npcId = this.getTemplate().getId();
            String filename = "data/html/seven_signs/";
            int sealAvariceOwner = SevenSigns.getInstance().getSealOwner(1);
            int sealGnosisOwner = SevenSigns.getInstance().getSealOwner(2);
            int playerCabal = SevenSigns.getInstance().getPlayerCabal(player.getObjectId());
            int compWinner = SevenSigns.getInstance().getCabalHighestScore();
            switch(npcId) {
               case 30298:
                  if (player.isAcademyMember()) {
                     filename = this.getHtmlPath(npcId, 1);
                  } else {
                     filename = this.getHtmlPath(npcId, val);
                  }
                  break;
               case 31092:
                  filename = filename + "blkmrkt_1.htm";
                  break;
               case 31113:
                  if (Config.ALT_STRICT_SEVENSIGNS) {
                     switch(compWinner) {
                        case 1:
                           if (playerCabal != compWinner || playerCabal != sealAvariceOwner) {
                              player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DUSK);
                              player.sendActionFailed();
                              return;
                           }
                           break;
                        case 2:
                           if (playerCabal != compWinner || playerCabal != sealAvariceOwner) {
                              player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DAWN);
                              player.sendActionFailed();
                              return;
                           }
                           break;
                        default:
                           player.sendPacket(SystemMessageId.SSQ_COMPETITION_UNDERWAY);
                           return;
                     }
                  }

                  filename = filename + "mammmerch_1.htm";
                  break;
               case 31126:
                  if (Config.ALT_STRICT_SEVENSIGNS) {
                     switch(compWinner) {
                        case 1:
                           if (playerCabal != compWinner || playerCabal != sealGnosisOwner) {
                              player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DUSK);
                              player.sendActionFailed();
                              return;
                           }
                           break;
                        case 2:
                           if (playerCabal != compWinner || playerCabal != sealGnosisOwner) {
                              player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DAWN);
                              player.sendActionFailed();
                              return;
                           }
                           break;
                        default:
                           player.sendPacket(SystemMessageId.SSQ_COMPETITION_UNDERWAY);
                           return;
                     }
                  }

                  filename = filename + "mammblack_1.htm";
                  break;
               case 31127:
               case 31128:
               case 31129:
               case 31130:
               case 31131:
                  filename = filename + "festival/dawn_guide.htm";
                  break;
               case 31132:
               case 31133:
               case 31134:
               case 31135:
               case 31136:
               case 31142:
               case 31143:
               case 31144:
               case 31145:
               case 31146:
                  filename = filename + "festival/festival_witch.htm";
                  break;
               case 31137:
               case 31138:
               case 31139:
               case 31140:
               case 31141:
                  filename = filename + "festival/dusk_guide.htm";
                  break;
               case 31688:
                  if (player.isNoble()) {
                     filename = "data/html/olympiad/noble_main.htm";
                  } else {
                     filename = this.getHtmlPath(npcId, val);
                  }
                  break;
               case 31690:
               case 31769:
               case 31770:
               case 31771:
               case 31772:
                  if (!player.isHero() && !player.isNoble()) {
                     filename = this.getHtmlPath(npcId, val);
                  } else {
                     filename = "data/html/olympiad/hero_main.htm";
                  }
                  break;
               case 36402:
                  if (player.olyBuff > 0) {
                     filename = player.olyBuff == 5 ? "data/html/olympiad/olympiad_buffs.htm" : "data/html/olympiad/olympiad_5buffs.htm";
                  } else {
                     filename = "data/html/olympiad/olympiad_nobuffs.htm";
                  }
                  break;
               default:
                  if (npcId >= 31865 && npcId <= 31918) {
                     if (val == 0) {
                        filename = filename + "rift/GuardianOfBorder.htm";
                     } else {
                        filename = filename + "rift/GuardianOfBorder-" + val + ".htm";
                     }
                  } else {
                     if (npcId >= 31093 && npcId <= 31094 || npcId >= 31172 && npcId <= 31201 || npcId >= 31239 && npcId <= 31254) {
                        return;
                     }

                     filename = this.getHtmlPath(npcId, val);
                  }
            }

            NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
            html.setFile(player, player.getLang(), filename);
            if (this instanceof MerchantInstance && Config.LIST_PET_RENT_NPC.contains(npcId)) {
               html.replace("_Quest", "_RentPet\">Rent Pet</a><br><a action=\"bypass -h npc_%objectId%_Quest");
            }

            html.replace("%objectId%", String.valueOf(this.getObjectId()));
            html.replace("%festivalMins%", SevenSignsFestival.getInstance().getTimeToNextFestivalStr());
            player.sendPacket(html);
            player.sendActionFailed();
         }
      } else {
         player.setTarget(player);
      }
   }

   public void showChatWindow(Player player, String filename) {
      NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
      html.setFile(player, player.getLang(), filename);
      html.replace("%objectId%", String.valueOf(this.getObjectId()));
      player.sendPacket(html);
      player.sendActionFailed();
   }

   public int getExpReward(Creature attacker) {
      if (Config.ALLOW_CUSTOM_RATES && attacker.isPlayer()) {
         int day = Calendar.getInstance().get(7);
         if (this.isMonday(day)) {
            return (int)(
               (double)this.getTemplate().getRewardExp()
                  * Config.RATE_XP_BY_LVL[attacker.getLevel()]
                  * (
                     !attacker.getPremiumBonus().isPersonal()
                        ? (attacker.isInParty() && Config.PREMIUM_PARTY_RATE ? attacker.getParty().getRateXp() : attacker.getPremiumBonus().getRateXp())
                        : 1.0
                  )
                  * (double)Config.MONDAY_RATE_EXP
            );
         }

         if (this.isTuesday(day)) {
            return (int)(
               (double)this.getTemplate().getRewardExp()
                  * Config.RATE_XP_BY_LVL[attacker.getLevel()]
                  * (
                     !attacker.getPremiumBonus().isPersonal()
                        ? (attacker.isInParty() && Config.PREMIUM_PARTY_RATE ? attacker.getParty().getRateXp() : attacker.getPremiumBonus().getRateXp())
                        : 1.0
                  )
                  * (double)Config.TUESDAY_RATE_EXP
            );
         }

         if (this.isWednesday(day)) {
            return (int)(
               (double)this.getTemplate().getRewardExp()
                  * Config.RATE_XP_BY_LVL[attacker.getLevel()]
                  * (
                     !attacker.getPremiumBonus().isPersonal()
                        ? (attacker.isInParty() && Config.PREMIUM_PARTY_RATE ? attacker.getParty().getRateXp() : attacker.getPremiumBonus().getRateXp())
                        : 1.0
                  )
                  * (double)Config.WEDNESDAY_RATE_EXP
            );
         }

         if (this.isThursday(day)) {
            return (int)(
               (double)this.getTemplate().getRewardExp()
                  * Config.RATE_XP_BY_LVL[attacker.getLevel()]
                  * (
                     !attacker.getPremiumBonus().isPersonal()
                        ? (attacker.isInParty() && Config.PREMIUM_PARTY_RATE ? attacker.getParty().getRateXp() : attacker.getPremiumBonus().getRateXp())
                        : 1.0
                  )
                  * (double)Config.THURSDAY_RATE_EXP
            );
         }

         if (this.isFriday(day)) {
            return (int)(
               (double)this.getTemplate().getRewardExp()
                  * Config.RATE_XP_BY_LVL[attacker.getLevel()]
                  * (
                     !attacker.getPremiumBonus().isPersonal()
                        ? (attacker.isInParty() && Config.PREMIUM_PARTY_RATE ? attacker.getParty().getRateXp() : attacker.getPremiumBonus().getRateXp())
                        : 1.0
                  )
                  * (double)Config.FRIDAY_RATE_EXP
            );
         }

         if (this.isSaturday(day)) {
            return (int)(
               (double)this.getTemplate().getRewardExp()
                  * Config.RATE_XP_BY_LVL[attacker.getLevel()]
                  * (
                     !attacker.getPremiumBonus().isPersonal()
                        ? (attacker.isInParty() && Config.PREMIUM_PARTY_RATE ? attacker.getParty().getRateXp() : attacker.getPremiumBonus().getRateXp())
                        : 1.0
                  )
                  * (double)Config.SATURDAY_RATE_EXP
            );
         }

         if (this.isSunday(day)) {
            return (int)(
               (double)this.getTemplate().getRewardExp()
                  * Config.RATE_XP_BY_LVL[attacker.getLevel()]
                  * (
                     !attacker.getPremiumBonus().isPersonal()
                        ? (attacker.isInParty() && Config.PREMIUM_PARTY_RATE ? attacker.getParty().getRateXp() : attacker.getPremiumBonus().getRateXp())
                        : 1.0
                  )
                  * (double)Config.SUNDAY_RATE_EXP
            );
         }
      }

      return (int)(
         (double)this.getTemplate().getRewardExp()
            * Config.RATE_XP_BY_LVL[attacker.getLevel()]
            * (
               attacker.isPlayer() && !attacker.getPremiumBonus().isPersonal()
                  ? (attacker.isInParty() && Config.PREMIUM_PARTY_RATE ? attacker.getParty().getRateXp() : attacker.getPremiumBonus().getRateXp())
                  : 1.0
            )
      );
   }

   public int getSpReward(Creature attacker) {
      if (Config.ALLOW_CUSTOM_RATES && attacker.isPlayer()) {
         int day = Calendar.getInstance().get(7);
         if (this.isMonday(day)) {
            return (int)(
               (double)this.getTemplate().getRewardExp()
                  * Config.RATE_SP_BY_LVL[attacker.getLevel()]
                  * (
                     !attacker.getPremiumBonus().isPersonal()
                        ? (attacker.isInParty() && Config.PREMIUM_PARTY_RATE ? attacker.getParty().getRateSp() : attacker.getPremiumBonus().getRateSp())
                        : 1.0
                  )
                  * (double)Config.MONDAY_RATE_SP
            );
         }

         if (this.isTuesday(day)) {
            return (int)(
               (double)this.getTemplate().getRewardExp()
                  * Config.RATE_SP_BY_LVL[attacker.getLevel()]
                  * (
                     !attacker.getPremiumBonus().isPersonal()
                        ? (attacker.isInParty() && Config.PREMIUM_PARTY_RATE ? attacker.getParty().getRateSp() : attacker.getPremiumBonus().getRateSp())
                        : 1.0
                  )
                  * (double)Config.TUESDAY_RATE_SP
            );
         }

         if (this.isWednesday(day)) {
            return (int)(
               (double)this.getTemplate().getRewardExp()
                  * Config.RATE_SP_BY_LVL[attacker.getLevel()]
                  * (
                     !attacker.getPremiumBonus().isPersonal()
                        ? (attacker.isInParty() && Config.PREMIUM_PARTY_RATE ? attacker.getParty().getRateSp() : attacker.getPremiumBonus().getRateSp())
                        : 1.0
                  )
                  * (double)Config.WEDNESDAY_RATE_SP
            );
         }

         if (this.isThursday(day)) {
            return (int)(
               (double)this.getTemplate().getRewardExp()
                  * Config.RATE_SP_BY_LVL[attacker.getLevel()]
                  * (
                     !attacker.getPremiumBonus().isPersonal()
                        ? (attacker.isInParty() && Config.PREMIUM_PARTY_RATE ? attacker.getParty().getRateSp() : attacker.getPremiumBonus().getRateSp())
                        : 1.0
                  )
                  * (double)Config.THURSDAY_RATE_SP
            );
         }

         if (this.isFriday(day)) {
            return (int)(
               (double)this.getTemplate().getRewardExp()
                  * Config.RATE_SP_BY_LVL[attacker.getLevel()]
                  * (
                     !attacker.getPremiumBonus().isPersonal()
                        ? (attacker.isInParty() && Config.PREMIUM_PARTY_RATE ? attacker.getParty().getRateSp() : attacker.getPremiumBonus().getRateSp())
                        : 1.0
                  )
                  * (double)Config.FRIDAY_RATE_SP
            );
         }

         if (this.isSaturday(day)) {
            return (int)(
               (double)this.getTemplate().getRewardExp()
                  * Config.RATE_SP_BY_LVL[attacker.getLevel()]
                  * (
                     !attacker.getPremiumBonus().isPersonal()
                        ? (attacker.isInParty() && Config.PREMIUM_PARTY_RATE ? attacker.getParty().getRateSp() : attacker.getPremiumBonus().getRateSp())
                        : 1.0
                  )
                  * (double)Config.SATURDAY_RATE_SP
            );
         }

         if (this.isSunday(day)) {
            return (int)(
               (double)this.getTemplate().getRewardExp()
                  * Config.RATE_SP_BY_LVL[attacker.getLevel()]
                  * (
                     !attacker.getPremiumBonus().isPersonal()
                        ? (attacker.isInParty() && Config.PREMIUM_PARTY_RATE ? attacker.getParty().getRateSp() : attacker.getPremiumBonus().getRateSp())
                        : 1.0
                  )
                  * (double)Config.SUNDAY_RATE_SP
            );
         }
      }

      return (int)(
         (double)this.getTemplate().getRewardSp()
            * Config.RATE_SP_BY_LVL[attacker.getLevel()]
            * (
               attacker.isPlayer() && !attacker.getPremiumBonus().isPersonal()
                  ? (attacker.isInParty() && Config.PREMIUM_PARTY_RATE ? attacker.getParty().getRateSp() : attacker.getPremiumBonus().getRateSp())
                  : 1.0
            )
      );
   }

   @Override
   protected void onDeath(Creature killer) {
      if (killer != null && killer.isPlayer() && killer.isInFightEvent()) {
         killer.getFightEvent().onKilled(killer, this);
      }

      this._currentLHandId = this.getTemplate().getLeftHand();
      this._currentRHandId = this.getTemplate().getRightHand();
      this._currentCollisionHeight = this.getTemplate().getfCollisionHeight();
      this._currentCollisionRadius = this.getTemplate().getfCollisionRadius();
      DecayTaskManager.getInstance().add(this);
      super.onDeath(killer);
   }

   public void setSpawn(Spawner spawn) {
      this._spawn = spawn;
   }

   @Override
   public void onSpawn() {
      super.onSpawn();
      if (this.getTemplate().getEventQuests(Quest.QuestEventType.ON_SPAWN) != null) {
         for(Quest quest : this.getTemplate().getEventQuests(Quest.QuestEventType.ON_SPAWN)) {
            quest.notifySpawn(this);
         }
      }

      if (!this.isTeleporting()) {
         WalkingManager.getInstance().onSpawn(this);
      }
   }

   @Override
   public void onDecay() {
      if (!this.isDecayed()) {
         this.setDecayed(true);
         super.onDecay();
         if (this._spawn != null) {
            this._spawn.decreaseCount(this);
         } else {
            this.deleteMe();
         }

         WalkingManager.getInstance().onDeath(this);
      }
   }

   @Override
   public void deleteMe() {
      this.onDecay();

      try {
         if (this._fusionSkill != null) {
            this.abortCast();
         }

         for(Creature character : World.getInstance().getAroundCharacters(this)) {
            if (character.getFusionSkill() != null && character.getFusionSkill().getTarget() == this) {
               character.abortCast();
            }
         }
      } catch (Exception var3) {
         _log.log(Level.SEVERE, "deleteMe()", (Throwable)var3);
      }

      super.deleteMe();
   }

   public Spawner getSpawn() {
      return this._spawn;
   }

   @Override
   public Location getSpawnedLoc() {
      return this._spawn != null ? this._spawn.getLocation() : null;
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName() + ":" + this.getName() + "(" + this.getId() + ")[" + this.getObjectId() + "]";
   }

   public boolean isDecayed() {
      return this._isDecayed;
   }

   public void setDecayed(boolean decayed) {
      this._isDecayed = decayed;
   }

   public void endDecayTask() {
      if (!this.isDecayed()) {
         DecayTaskManager.getInstance().cancel(this);
         this.onDecay();
      }
   }

   public boolean isMob() {
      return false;
   }

   public void setLHandId(int newWeaponId) {
      this._currentLHandId = newWeaponId;
      this.updateAbnormalEffect();
   }

   public void setRHandId(int newWeaponId) {
      this._currentRHandId = newWeaponId;
      this.updateAbnormalEffect();
   }

   public void setLRHandId(int newLWeaponId, int newRWeaponId) {
      this._currentRHandId = newRWeaponId;
      this._currentLHandId = newLWeaponId;
      this.updateAbnormalEffect();
   }

   public void setEnchant(int newEnchantValue) {
      this._currentEnchant = newEnchantValue;
      this.updateAbnormalEffect();
   }

   public boolean isShowName() {
      return !this.getTemplate().isShowName();
   }

   @Override
   public boolean isTargetable() {
      return !this.getTemplate().isTargetable();
   }

   public void setCollisionHeight(double height) {
      this._currentCollisionHeight = height;
   }

   public void setCollisionRadius(double radius) {
      this._currentCollisionRadius = radius;
   }

   public double getCollisionHeight() {
      return this._currentCollisionHeight;
   }

   public double getCollisionRadius() {
      return this._currentCollisionRadius;
   }

   @Override
   public void sendInfo(Player activeChar) {
      if (this.isVisibleFor(activeChar)) {
         if (Config.CHECK_KNOWN && activeChar.isGM()) {
            activeChar.sendMessage("Added NPC: " + this.getName());
         }

         if (this.getRunSpeed() == 0.0) {
            activeChar.sendPacket(new ServerObjectInfo(this, activeChar));
         } else {
            activeChar.sendPacket(new NpcInfo.Info(this, activeChar));
         }

         if (this.isMoving()) {
            activeChar.sendPacket(new MoveToLocation(this));
         }
      }
   }

   public void showNoTeachHtml(Player player) {
      int npcId = this.getId();
      String html = "";
      if (this instanceof WarehouseInstance) {
         html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/warehouse/" + npcId + "-noteach.htm");
      } else if (this instanceof TrainerInstance) {
         html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/trainer/" + npcId + "-noteach.htm");
         if (html == null) {
            html = HtmCache.getInstance().getHtm(player, "data/scripts/custom/HealerTrainer/" + player.getLang() + "/" + npcId + "-noteach.htm");
         }
      }

      NpcHtmlMessage noTeachMsg = new NpcHtmlMessage(this.getObjectId());
      if (html == null) {
         _log.warning("Npc " + npcId + " missing noTeach html!");
         noTeachMsg.setHtml(player, "<html><body>I cannot teach you any skills.<br>You must find your current class teachers.</body></html>");
      } else {
         noTeachMsg.setHtml(player, html);
         noTeachMsg.replace("%objectId%", String.valueOf(this.getObjectId()));
      }

      player.sendPacket(noTeachMsg);
   }

   public Npc scheduleDespawn(long delay) {
      ThreadPoolManager.getInstance().schedule(new Npc.DespawnTask(), delay);
      return this;
   }

   @Override
   protected final void notifyQuestEventSkillFinished(Skill skill, GameObject target) {
      try {
         if (this.getTemplate().getEventQuests(Quest.QuestEventType.ON_SPELL_FINISHED) != null) {
            Player player = null;
            if (target != null) {
               player = target.getActingPlayer();
            }

            for(Quest quest : this.getTemplate().getEventQuests(Quest.QuestEventType.ON_SPELL_FINISHED)) {
               quest.notifySpellFinished(this, player, skill);
            }
         }
      } catch (Exception var6) {
         _log.log(Level.SEVERE, "", (Throwable)var6);
      }
   }

   @Override
   public boolean isMovementDisabled() {
      return super.isMovementDisabled() || this.getTemplate().isMovementDisabled() || this.getAI() instanceof Corpse;
   }

   public String getAiType() {
      return this.getTemplate().getAI();
   }

   public void setDisplayEffect(int val) {
      if (val != this._displayEffect) {
         this._displayEffect = val;
         this.broadcastPacket(new ExChangeNpcState(this.getObjectId(), val));
      }
   }

   public int getDisplayEffect() {
      return this._displayEffect;
   }

   public int getColorEffect() {
      return 0;
   }

   public void broadcastNpcSay(String text) {
      this.broadcastNpcSay(0, text);
   }

   public void broadcastNpcSay(int messageType, String text) {
      this.broadcastPacket(new NpcSay(this.getObjectId(), messageType, this.getId(), text));
   }

   public Creature getSummoner() {
      return this._summoner;
   }

   public void setSummoner(Creature summoner) {
      this._summoner = summoner;
   }

   @Override
   public boolean isNpc() {
      return true;
   }

   @Override
   public void setTeam(int id) {
      super.setTeam(id);
      this.broadcastInfo();
   }

   public boolean hasRandomAnimation() {
      return Config.MAX_NPC_ANIMATION > 0 && !(this.getAI() instanceof Corpse);
   }

   @Override
   public boolean isWalker() {
      return WalkingManager.getInstance().isRegistered(this);
   }

   @Override
   public boolean isRunner() {
      return this._isRunner;
   }

   public void setIsRunner(boolean status) {
      this._isRunner = status;
   }

   @Override
   public boolean isSpecialCamera() {
      return this._isSpecialCamera;
   }

   public void setIsSpecialCamera(boolean status) {
      this._isSpecialCamera = status;
   }

   @Override
   public boolean isEkimusFood() {
      return this._isEkimusFood;
   }

   public void setIsEkimusFood(boolean status) {
      this._isEkimusFood = status;
   }

   @Override
   public boolean isChargedShot(ShotType type) {
      return (this._shotsMask & type.getMask()) == type.getMask();
   }

   @Override
   public void setChargedShot(ShotType type, boolean charged) {
      if (charged) {
         this._shotsMask |= type.getMask();
      } else {
         this._shotsMask &= ~type.getMask();
      }
   }

   @Override
   public void rechargeShots(boolean physical, boolean magic) {
      if (this.getTemplate().getShots() != NpcTemplate.ShotsType.NONE) {
         if (physical
            && (
               this.getTemplate().getShots() == NpcTemplate.ShotsType.SOUL
                  || this.getTemplate().getShots() == NpcTemplate.ShotsType.SOUL_SPIRIT
                  || this.getTemplate().getShots() == NpcTemplate.ShotsType.SOUL_BSPIRIT
            )) {
            if (Rnd.get(100) > Config.SOULSHOT_CHANCE) {
               return;
            }

            Broadcast.toSelfAndKnownPlayersInRadius(this, new MagicSkillUse(this, this, 2154, 1, 0, 0), 600);
            this.setChargedShot(ShotType.SOULSHOTS, true);
         }

         if (magic
            && (
               this.getTemplate().getShots() == NpcTemplate.ShotsType.SPIRIT
                  || this.getTemplate().getShots() == NpcTemplate.ShotsType.SOUL_SPIRIT
                  || this.getTemplate().getShots() == NpcTemplate.ShotsType.SOUL_BSPIRIT
            )) {
            if (Rnd.get(100) > Config.SPIRITSHOT_CHANCE) {
               return;
            }

            Broadcast.toSelfAndKnownPlayersInRadius(this, new MagicSkillUse(this, this, 2061, 1, 0, 0), 600);
            this.setChargedShot(ShotType.SPIRITSHOTS, true);
         }
      }
   }

   public int getScriptValue() {
      return this.getVariables().getInteger("script_val");
   }

   public void setScriptValue(int val) {
      this.getVariables().set("script_val", val);
   }

   public boolean isScriptValue(int val) {
      return this.getVariables().getInteger("script_val") == val;
   }

   public boolean hasVariables() {
      return this.getScript(NpcStats.class) != null;
   }

   public NpcStats getVariables() {
      NpcStats vars = this.getScript(NpcStats.class);
      return vars != null ? vars : this.addScript(new NpcStats());
   }

   public void broadcastEvent(String eventName, int radius, GameObject reference) {
      for(Npc obj : World.getInstance().getAroundNpc(this, radius, 200)) {
         if (obj.getTemplate().getEventQuests(Quest.QuestEventType.ON_EVENT_RECEIVED) != null) {
            for(Quest quest : obj.getTemplate().getEventQuests(Quest.QuestEventType.ON_EVENT_RECEIVED)) {
               quest.notifyEventReceived(eventName, this, obj, reference);
            }
         }
      }
   }

   @Override
   public boolean isInCategory(CategoryType type) {
      return CategoryParser.getInstance().isInCategory(type, this.getId());
   }

   @Override
   public Npc getActingNpc() {
      return this;
   }

   public final boolean isSevenSignsMonster() {
      return this.getFaction().getName().equalsIgnoreCase("c_dungeon_clan");
   }

   public boolean staysInSpawnLoc() {
      return this.getSpawn() != null && this.getSpawn().getX(this) == this.getX() && this.getSpawn().getY(this) == this.getY();
   }

   private boolean isMonday(int day) {
      return day == 2;
   }

   private boolean isTuesday(int day) {
      return day == 3;
   }

   private boolean isWednesday(int day) {
      return day == 4;
   }

   private boolean isThursday(int day) {
      return day == 5;
   }

   private boolean isFriday(int day) {
      return day == 6;
   }

   private boolean isSaturday(int day) {
      return day == 7;
   }

   private boolean isSunday(int day) {
      return day == 1;
   }

   @Override
   public boolean isVisibleFor(Player player) {
      if (this.getTemplate().getEventQuests(Quest.QuestEventType.ON_CAN_SEE_ME) != null) {
         Iterator var2 = this.getTemplate().getEventQuests(Quest.QuestEventType.ON_CAN_SEE_ME).iterator();
         if (var2.hasNext()) {
            Quest quest = (Quest)var2.next();
            return quest.notifyOnCanSeeMe(this, player);
         }
      }

      return super.isVisibleFor(player);
   }

   public void broadcastSay(int chatType, NpcStringId npcStringId, String... parameters) {
      NpcSay npcSay = new NpcSay(this, chatType, npcStringId);
      if (parameters != null) {
         for(String parameter : parameters) {
            if (parameter != null) {
               npcSay.addStringParameter(parameter);
            }
         }
      }

      switch(chatType) {
         case 22:
            Broadcast.toKnownPlayersInRadius(this, npcSay, 1250);
            break;
         default:
            Broadcast.toKnownPlayers(this, npcSay);
      }
   }

   public int calculateLevelDiffForDrop(int charLevel) {
      return calculateLevelDiffForDrop(this.getLevel(), charLevel, this instanceof RaidBossInstance || this instanceof GrandBossInstance);
   }

   public static int calculateLevelDiffForDrop(int mobLevel, int charLevel, boolean boss) {
      if (!Config.DEEPBLUE_DROP_RULES) {
         return 0;
      } else {
         int deepblue_maxdiff = boss ? Config.DEEPBLUE_DROP_RAID_MAXDIFF : Config.DEEPBLUE_DROP_MAXDIFF;
         return Math.max(charLevel - mobLevel - deepblue_maxdiff, 0);
      }
   }

   public void setRandomAnimationEnabled(boolean val) {
      this._isRandomAnimationEnabled = val;
   }

   public boolean isRandomAnimationEnabled() {
      return this._isRandomAnimationEnabled;
   }

   public TerritoryWarManager.Territory getTerritory() {
      if (this.getReflectionId() != 0) {
         return null;
      } else {
         if (this._nearestTerritory == null) {
            if (this.getTemplate().getCastleId() == 0) {
               return null;
            }

            this._nearestTerritory = TerritoryWarManager.getInstance().getTerritory(this.getTemplate().getCastleId());
         }

         return this._nearestTerritory;
      }
   }

   @Override
   public boolean canBeAttacked() {
      return Config.ALT_ATTACKABLE_NPCS;
   }

   public MinionList getMinionList() {
      return null;
   }

   public boolean hasMinions() {
      return false;
   }

   @Override
   public double getColRadius() {
      return this.getCollisionRadius();
   }

   @Override
   public double getColHeight() {
      return this.getCollisionHeight();
   }

   @Override
   public void addInfoObject(GameObject object) {
      if (object.isCreature()) {
         List<Quest> quests = this.getTemplate().getEventQuests(Quest.QuestEventType.ON_SEE_CREATURE);
         if (quests != null) {
            for(Quest quest : quests) {
               quest.notifySeeCreature(this, (Creature)object, object.isSummon());
            }
         }
      }
   }

   public class DespawnTask implements Runnable {
      @Override
      public void run() {
         if (!Npc.this.isDecayed()) {
            Npc.this.deleteMe();
         }
      }
   }

   protected static class RandomAnimationTask implements Runnable {
      private final Npc _npc;

      protected RandomAnimationTask(Npc npc) {
         this._npc = npc;
      }

      @Override
      public void run() {
         try {
            if (this._npc != null) {
               if (this._npc.isMob()) {
                  if (this._npc.getAI() != null && this._npc.getAI().getIntention() != CtrlIntention.ACTIVE) {
                     return;
                  }
               } else if (!this._npc.isInActiveRegion()) {
                  return;
               }

               if (!this._npc.isDead() && !this._npc.isStunned() && !this._npc.isSleeping() && !this._npc.isParalyzed()) {
                  this._npc.onRandomAnimation(Rnd.get(2, 3));
               }

               this._npc.startRandomAnimationTimer();
            }
         } catch (Exception var2) {
            if (Config.DEBUG) {
               Creature._log.log(Level.SEVERE, "There has been an error trying to perform a random animation for NPC {}!", (Throwable)var2);
            }
         }
      }
   }
}
