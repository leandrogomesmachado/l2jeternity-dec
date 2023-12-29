package l2e.gameserver.model.actor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.listener.Listener;
import l2e.commons.util.PositionUtils;
import l2e.commons.util.Rnd;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.GameTimeController;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.DefaultAI;
import l2e.gameserver.ai.character.CharacterAI;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.DoorParser;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.geodata.GeoMove;
import l2e.gameserver.handler.skillhandlers.ISkillHandler;
import l2e.gameserver.handler.skillhandlers.SkillHandler;
import l2e.gameserver.instancemanager.DimensionalRiftManager;
import l2e.gameserver.instancemanager.MapRegionManager;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.instancemanager.SiegeManager;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.instancemanager.TownManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.AccessLevel;
import l2e.gameserver.model.CategoryType;
import l2e.gameserver.model.ChanceSkillList;
import l2e.gameserver.model.CharEffectList;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.FusionSkill;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.ShotType;
import l2e.gameserver.model.TeleportWhereType;
import l2e.gameserver.model.TimeStamp;
import l2e.gameserver.model.World;
import l2e.gameserver.model.WorldRegion;
import l2e.gameserver.model.actor.events.CharEvents;
import l2e.gameserver.model.actor.instance.DoorInstance;
import l2e.gameserver.model.actor.instance.EventMapGuardInstance;
import l2e.gameserver.model.actor.instance.PetInstance;
import l2e.gameserver.model.actor.instance.RiftInvaderInstance;
import l2e.gameserver.model.actor.instance.player.AutoFarmOptions;
import l2e.gameserver.model.actor.instance.player.PremiumBonus;
import l2e.gameserver.model.actor.listener.CharListenerList;
import l2e.gameserver.model.actor.stat.CharStat;
import l2e.gameserver.model.actor.status.CharStatus;
import l2e.gameserver.model.actor.tasks.character.HitTask;
import l2e.gameserver.model.actor.tasks.character.MagicGeoCheckTask;
import l2e.gameserver.model.actor.tasks.character.MagicUseTask;
import l2e.gameserver.model.actor.tasks.character.NotifyAITask;
import l2e.gameserver.model.actor.tasks.character.QueuedMagicUseTask;
import l2e.gameserver.model.actor.templates.character.CharTemplate;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.actor.templates.items.Weapon;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.actor.templates.npc.champion.ChampionTemplate;
import l2e.gameserver.model.actor.transform.Transform;
import l2e.gameserver.model.actor.transform.TransformTemplate;
import l2e.gameserver.model.entity.Reflection;
import l2e.gameserver.model.entity.events.AbstractFightEvent;
import l2e.gameserver.model.holders.InvulSkillHolder;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.holders.SkillUseHolder;
import l2e.gameserver.model.interfaces.IChanceSkillTrigger;
import l2e.gameserver.model.interfaces.ILocational;
import l2e.gameserver.model.interfaces.ISkillsHolder;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.itemcontainer.Inventory;
import l2e.gameserver.model.items.type.WeaponType;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.SkillType;
import l2e.gameserver.model.skills.effects.AbnormalEffect;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectFlag;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.skills.funcs.Func;
import l2e.gameserver.model.skills.l2skills.SkillSummon;
import l2e.gameserver.model.skills.options.OptionsSkillHolder;
import l2e.gameserver.model.skills.options.OptionsSkillType;
import l2e.gameserver.model.skills.targets.TargetType;
import l2e.gameserver.model.stats.Calculator;
import l2e.gameserver.model.stats.Formulas;
import l2e.gameserver.model.stats.Stats;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.ZoneType;
import l2e.gameserver.model.zone.type.FunPvpZone;
import l2e.gameserver.model.zone.type.NoGeoZone;
import l2e.gameserver.model.zone.type.PeaceZone;
import l2e.gameserver.model.zone.type.SiegeZone;
import l2e.gameserver.model.zone.type.WaterZone;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ActionFail;
import l2e.gameserver.network.serverpackets.Attack;
import l2e.gameserver.network.serverpackets.ChangeMoveType;
import l2e.gameserver.network.serverpackets.ChangeWaitType;
import l2e.gameserver.network.serverpackets.CharInfo;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.ExRotation;
import l2e.gameserver.network.serverpackets.FlyToLocation;
import l2e.gameserver.network.serverpackets.GameServerPacket;
import l2e.gameserver.network.serverpackets.MagicSkillCanceled;
import l2e.gameserver.network.serverpackets.MagicSkillLaunched;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.gameserver.network.serverpackets.MoveToLocation;
import l2e.gameserver.network.serverpackets.Revive;
import l2e.gameserver.network.serverpackets.SetupGauge;
import l2e.gameserver.network.serverpackets.SocialAction;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.StopMove;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.network.serverpackets.TeleportToLocation;
import l2e.gameserver.network.serverpackets.ValidateLocation;
import l2e.gameserver.taskmanager.AttackStanceTaskManager;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;

public abstract class Creature extends GameObject implements ISkillsHolder {
   public static final Logger _log = Logger.getLogger(Creature.class.getName());
   protected volatile CharListenerList _listeners;
   private volatile Set<Creature> _attackByList;
   private volatile boolean _isCastingNow = false;
   private volatile boolean _isCastingSimultaneouslyNow = false;
   private boolean _isDead = false;
   private boolean _isImmobilized = false;
   private boolean _isOverloaded = false;
   private boolean _isStunned = false;
   private boolean _isFakeDeath = false;
   private boolean _isParalyzed = false;
   private boolean _isPendingRevive = false;
   private boolean _isRunning = false;
   private boolean _isNoRndWalk = false;
   protected boolean _showSummonAnimation = false;
   private boolean _isTeleporting = false;
   private boolean _isInvul = false;
   private boolean _isMortal = true;
   private boolean _isFlying = false;
   private boolean _isDanceStun = false;
   private boolean _isRooted = false;
   private boolean _isHealBlocked = false;
   private ChampionTemplate _championTemplate = null;
   private Location _flyLoc;
   private boolean _isFlyingNow = false;
   private boolean _isFlyingPos = false;
   private boolean _blocked;
   private CharStat _stat;
   private CharStatus _status;
   private CharEvents _events;
   private CharTemplate _template;
   private String _title;
   public static final double MAX_HP_BAR_PX = 352.0;
   private double _hpUpdateIncCheck = 0.0;
   private double _hpUpdateDecCheck = 0.0;
   private double _hpUpdateInterval = 0.0;
   private Calculator[] _calculators;
   protected final ReentrantLock _lock = new ReentrantLock();
   protected Collection<DoorInstance> _doors;
   private final Map<Integer, Skill> _skills = new ConcurrentHashMap<>();
   private final List<Skill> _blockSkills = new ArrayList<>();
   private volatile ChanceSkillList _chanceSkills;
   protected FusionSkill _fusionSkill;
   protected byte _zoneValidateCounter = 4;
   private List<ZoneType> _zoneList = null;
   private final ReentrantLock _zoneLock = new ReentrantLock();
   private Creature _debugger = null;
   private final ReentrantLock _teleportLock;
   private int _team;
   protected long _exceptions = 0L;
   private volatile Map<Integer, OptionsSkillHolder> _triggerSkills;
   private volatile Map<Integer, InvulSkillHolder> _invulAgainst;
   private final Set<AbnormalEffect> _abnormalEffects = new CopyOnWriteArraySet<>();
   private int _abnormalEffectsMask;
   private int _abnormalEffectsMask2;
   private int _abnormalEffectsMask3;
   private int _watchDistance = 0;
   protected CharEffectList _effects = new CharEffectList(this);
   protected IntObjectMap<TimeStamp> _skillReuses = new CHashIntObjectMap<>();
   private boolean _allSkillsDisabled;
   protected Creature.MoveData _move;
   private int _wrongCoords = 0;
   private GameObject _target;
   private Creature _castingTarget;
   private volatile long _attackEndTime;
   private long _disableBowAttackEndTime;
   private Skill _castingSkill;
   private long _castInterruptTime;
   private long _animationEndTime;
   private static final Calculator[] NPC_STD_CALCULATOR = Formulas.getStdNPCCalculators();
   private volatile CharacterAI _ai = null;
   private Future<?> _skillCast;
   private Future<?> _skillCast2;
   public Future<?> _skillGeoCheckTask;
   private boolean _AIdisabled = false;

   public boolean isDebug() {
      return this._debugger != null;
   }

   public void setDebug(Creature d) {
      this._debugger = d;
   }

   public void sendDebugPacket(GameServerPacket pkt) {
      if (this._debugger != null) {
         this._debugger.sendPacket(pkt);
      }
   }

   public void sendDebugMessage(String msg) {
      if (this._debugger != null) {
         this._debugger.sendMessage(msg);
      }
   }

   public Inventory getInventory() {
      return null;
   }

   public boolean destroyItemByItemId(String process, int itemId, long count, GameObject reference, boolean sendMessage) {
      return true;
   }

   public boolean destroyItem(String process, int objectId, long count, GameObject reference, boolean sendMessage) {
      return true;
   }

   @Override
   public boolean isInsideZone(ZoneId zone) {
      this._zoneLock.lock();

      try {
         Reflection ref = null;
         if (this.getReflectionId() > 0) {
            ref = ReflectionManager.getInstance().getReflection(this.getReflectionId());
         }

         switch(zone) {
            case PVP:
               if (ref != null && ref.isPvPInstance()) {
                  return true;
               }
               break;
            case PEACE:
               if (ref != null && ref.isPvPInstance()) {
                  return false;
               }
         }

         if (this._zoneList == null) {
            return false;
         } else {
            for(int i = 0; i < this._zoneList.size(); ++i) {
               ZoneType zType = this._zoneList.get(i);
               if (zType.getZoneId().contains(zone)) {
                  return true;
               }
            }

            return false;
         }
      } finally {
         this._zoneLock.unlock();
      }
   }

   public boolean isInsideZone(ZoneId zone, ZoneType type) {
      this._zoneLock.lock();

      try {
         if (this._zoneList == null) {
            return false;
         } else {
            for(int i = 0; i < this._zoneList.size(); ++i) {
               ZoneType zType = this._zoneList.get(i);
               if (zType != type && zType.getZoneId().contains(zone)) {
                  return true;
               }
            }

            return false;
         }
      } finally {
         this._zoneLock.unlock();
      }
   }

   public boolean isTransformed() {
      return false;
   }

   public Transform getTransformation() {
      return null;
   }

   public void untransform() {
   }

   public boolean isGM() {
      return false;
   }

   public AccessLevel getAccessLevel() {
      return null;
   }

   public Creature(int objectId, CharTemplate template) {
      super(objectId);
      if (template == null) {
         throw new NullPointerException("Template is null!");
      } else {
         this.setInstanceType(GameObject.InstanceType.Creature);
         this.initCharStat();
         this.initCharStatus();
         this.initCharEvents();
         this._template = template;
         if (this.isDoor()) {
            this._calculators = Formulas.getStdDoorCalculators();
         } else if (this.isNpc()) {
            this._calculators = NPC_STD_CALCULATOR;
            if (template.getSkills() != null) {
               this._skills.putAll(template.getSkills());
            }

            for(Skill skill : this._skills.values()) {
               this.addStatFuncs(skill.getStatFuncs(null, this));
            }
         } else {
            this._calculators = new Calculator[Stats.NUM_STATS];
            if (this.isSummon()) {
               this._skills.putAll(((NpcTemplate)template).getSkills());

               for(Skill skill : this._skills.values()) {
                  this.addStatFuncs(skill.getStatFuncs(null, this));
               }
            }

            Formulas.addFuncsToNewCharacter(this);
         }

         this.setIsInvul(true);
         this._teleportLock = new ReentrantLock();
      }
   }

   protected void initCharStatusUpdateValues() {
      this._hpUpdateIncCheck = this.getMaxHp();
      this._hpUpdateInterval = this._hpUpdateIncCheck / 352.0;
      this._hpUpdateDecCheck = this._hpUpdateIncCheck - this._hpUpdateInterval;
   }

   public void onDecay() {
      this.decayMe();
   }

   @Override
   public void onSpawn() {
      super.onSpawn();
      this.revalidateZone(true);
   }

   @Override
   public final void setWorldRegion(WorldRegion value) {
      super.setWorldRegion(value);
      this.revalidateZone(true);
   }

   public void onTeleported() {
      if (this._teleportLock.tryLock()) {
         try {
            if (!this.isTeleporting()) {
               return;
            }

            this.spawnMe(this.getX(), this.getY(), this.getZ());
            this.setIsTeleporting(false);
            this.getEvents().onTeleported();
         } finally {
            this._teleportLock.unlock();
         }

         if (this._isPendingRevive) {
            this.doRevive();
         }
      }
   }

   public void addAttackerToAttackByList(Creature player) {
   }

   public void broadcastPacket(GameServerPacket mov) {
      if (!(mov instanceof CharInfo)) {
         this.sendPacket(mov);
      }

      mov.setInvisible(this.isInvisible());

      for(Player player : World.getInstance().getAroundPlayers(this)) {
         if (player != null) {
            player.sendPacket(mov);
         }
      }
   }

   public void broadcastPacket(GameServerPacket... packets) {
      this.sendPacket(packets);

      for(GameServerPacket packet : packets) {
         this.broadcastPacket(packet);
      }
   }

   public void broadcastPacket(GameServerPacket mov, int radiusInKnownlist) {
      if (!(mov instanceof CharInfo)) {
         this.sendPacket(mov);
      }

      mov.setInvisible(this.isInvisible());

      for(Player player : World.getInstance().getAroundPlayers(this)) {
         if (player != null && this.isInsideRadius(player, radiusInKnownlist, false, false)) {
            player.sendPacket(mov);
         }
      }
   }

   protected boolean needHpUpdate() {
      double currentHp = this.getCurrentHp();
      double maxHp = this.getMaxHp();
      if (!(currentHp <= 1.0) && !(maxHp < 352.0)) {
         if (!(currentHp < this._hpUpdateDecCheck)
            && !(Math.abs(currentHp - this._hpUpdateDecCheck) <= 1.0E-6)
            && !(currentHp > this._hpUpdateIncCheck)
            && !(Math.abs(currentHp - this._hpUpdateIncCheck) <= 1.0E-6)) {
            return false;
         } else {
            if (Math.abs(currentHp - maxHp) <= 1.0E-6) {
               this._hpUpdateIncCheck = currentHp + 1.0;
               this._hpUpdateDecCheck = currentHp - this._hpUpdateInterval;
            } else {
               double doubleMulti = currentHp / this._hpUpdateInterval;
               int intMulti = (int)doubleMulti;
               this._hpUpdateDecCheck = this._hpUpdateInterval * (double)(doubleMulti < (double)intMulti ? intMulti-- : intMulti);
               this._hpUpdateIncCheck = this._hpUpdateDecCheck + this._hpUpdateInterval;
            }

            return true;
         }
      } else {
         return true;
      }
   }

   public void broadcastStatusUpdate() {
      if (!this.getStatus().getStatusListener().isEmpty() && this.needHpUpdate()) {
         StatusUpdate su = new StatusUpdate(this);
         su.addAttribute(10, this.getMaxHp());
         su.addAttribute(9, (int)this.getCurrentHp());

         for(Creature temp : this.getStatus().getStatusListener()) {
            if (temp != null) {
               temp.sendPacket(su);
            }
         }
      }
   }

   public void sendMessage(String text) {
   }

   public void teleToLocation(int x, int y, int z, int heading, int randomOffset, boolean revalidateZone, boolean sendTelePacket) {
      this.stopMove(null);
      this.abortAttack();
      this.abortCast();
      this.setIsTeleporting(true);
      this.setTarget(null);
      this.getAI().setIntention(CtrlIntention.ACTIVE);
      if (Config.OFFSET_ON_TELEPORT_ENABLED && randomOffset > 0) {
         Location loc = Location.findAroundPosition(x, y, z, randomOffset / 2, randomOffset, this.getGeoIndex());
         if (loc != null) {
            x = loc.getX();
            y = loc.getY();
            z = loc.getZ();
         }
      }

      if ((!this.isPlayer() || !this.getActingPlayer().isInVehicle() || this.isFlying() || ZoneManager.getInstance().getZone(x, y, z, WaterZone.class) != null)
         && ZoneManager.getInstance().getZone(x, y, z, NoGeoZone.class) == null) {
         z = GeoEngine.getHeight(x, y, z, this.getGeoIndex());
      }

      if (sendTelePacket) {
         this.broadcastPacket(new TeleportToLocation(this, x, y, z, heading));
      }

      this.setWorldRegion(null);
      this.setXYZ(x, y, z);
      if (heading != 0) {
         this.setHeading(heading);
      }

      if (!this.isPlayer() || this.getActingPlayer().getClient() != null && this.getActingPlayer().getClient().isDetached()) {
         this.onTeleported();
      }

      if (revalidateZone) {
         this.revalidateZone(true);
      }
   }

   public void teleToLocation(int x, int y, int z, boolean revalidateZone, boolean sendTelePacket) {
      this.teleToLocation(x, y, z, this.getHeading(), 0, revalidateZone, sendTelePacket);
   }

   public void teleToLocation(int x, int y, int z, boolean revalidateZone) {
      this.teleToLocation(x, y, z, this.getHeading(), 0, revalidateZone, true);
   }

   public void teleToLocation(int x, int y, int z, int randomOffset, boolean revalidateZone) {
      this.teleToLocation(x, y, z, this.getHeading(), randomOffset, revalidateZone, true);
   }

   public void teleToLocation(ILocational loc, int randomOffset, boolean revalidateZone) {
      int x = loc.getX();
      int y = loc.getY();
      int z = loc.getZ();
      if (this.isPlayer() && DimensionalRiftManager.getInstance().checkIfInRiftZone(this.getX(), this.getY(), this.getZ(), false)) {
         Player player = this.getActingPlayer();
         player.sendMessage("You have been sent to the waiting room.");
         if (player.isInParty() && player.getParty().isInDimensionalRift()) {
            player.getParty().getDimensionalRift().usedTeleport(player);
         }

         int[] newCoords = DimensionalRiftManager.getInstance().getRoom((byte)0, (byte)0).getTeleportCoorinates();
         x = newCoords[0];
         y = newCoords[1];
         z = newCoords[2];
      }

      this.teleToLocation(x, y, z, this.getHeading(), randomOffset, revalidateZone, true);
   }

   public void teleToLocation(ILocational loc, boolean revalidateZone) {
      this.teleToLocation(new Location(loc.getX(), loc.getY(), loc.getZ(), loc.getHeading()), 0, revalidateZone);
   }

   public void teleToLocation(TeleportWhereType teleportWhere, boolean revalidateZone) {
      this.teleToLocation(MapRegionManager.getInstance().getTeleToLocation(this, teleportWhere), revalidateZone);
   }

   public void teleToLocation(ILocational loc, boolean allowRandomOffset, boolean revalidateZone) {
      this.teleToLocation(loc, allowRandomOffset ? Config.MAX_OFFSET_ON_TELEPORT : 0, revalidateZone);
   }

   public void teleToLocation(int x, int y, int z, boolean allowRandomOffset, boolean revalidateZone, boolean sendTelePacket) {
      if (allowRandomOffset) {
         this.teleToLocation(x, y, z, Config.MAX_OFFSET_ON_TELEPORT, revalidateZone, sendTelePacket);
      } else {
         this.teleToLocation(x, y, z, 0, revalidateZone, sendTelePacket);
      }
   }

   public void teleToLocation(int x, int y, int z, int heading, boolean allowRandomOffset, boolean revalidateZone) {
      if (allowRandomOffset) {
         this.teleToLocation(x, y, z, heading, Config.MAX_OFFSET_ON_TELEPORT, revalidateZone, true);
      } else {
         this.teleToLocation(x, y, z, heading, 0, revalidateZone, true);
      }
   }

   private boolean canUseRangeWeapon() {
      if (this.isTransformed()) {
         return true;
      } else {
         Weapon weaponItem = this.getActiveWeaponItem();
         if (weaponItem != null && weaponItem.getItemType().isRanged()) {
            if (this.isPlayer()) {
               if (!this.checkAndEquipArrows()) {
                  this.getAI().setIntention(CtrlIntention.IDLE);
                  this.sendActionFailed();
                  this.sendPacket(weaponItem.getItemType().isBow() ? SystemMessageId.NOT_ENOUGH_ARROWS : SystemMessageId.NOT_ENOUGH_BOLTS);
                  return false;
               }

               long timeToNextBowCrossBowAttack = this._disableBowAttackEndTime - System.currentTimeMillis();
               if (timeToNextBowCrossBowAttack > 0L) {
                  ThreadPoolManager.getInstance().schedule(new NotifyAITask(this, CtrlEvent.EVT_READY_TO_ACT), timeToNextBowCrossBowAttack);
                  this.sendActionFailed();
                  return false;
               }

               int mpConsume = weaponItem.getMpConsume();
               if (weaponItem.getReducedMpConsume() > 0 && Rnd.get(100) < weaponItem.getReducedMpConsumeChance()) {
                  mpConsume = weaponItem.getReducedMpConsume();
               }

               mpConsume = (int)this.calcStat(Stats.BOW_MP_CONSUME_RATE, (double)mpConsume, null, null);
               if (this.getCurrentMp() < (double)mpConsume) {
                  ThreadPoolManager.getInstance().schedule(new NotifyAITask(this, CtrlEvent.EVT_READY_TO_ACT), 100L);
                  this.sendPacket(SystemMessageId.NOT_ENOUGH_MP);
                  this.sendActionFailed();
                  return false;
               }

               if (mpConsume > 0) {
                  this.getStatus().reduceMp((double)mpConsume);
               }
            } else if (this.isNpc() && this._disableBowAttackEndTime > System.currentTimeMillis()) {
               return false;
            }

            return true;
         } else {
            return false;
         }
      }
   }

   public void doAttack(Creature target) {
      if (target != null && !this.isAttackingDisabled() && this.getEvents().onAttack(target)) {
         if (this.isSummon() && this.isCancelAction()) {
            ((Summon)this).setCancelAction(false);
            this.getAI().setIntention(CtrlIntention.ACTIVE);
         } else {
            if (this.isInFightEvent()) {
               for(AbstractFightEvent e : this.getFightEvents()) {
                  if (e != null && !e.canAttack(target, this)) {
                     if (this.isPlayer()) {
                        this.sendActionFailed();
                        this.getAI().setIntention(CtrlIntention.ACTIVE);
                     }

                     return;
                  }
               }
            }

            if (!this.isAlikeDead()) {
               if (this.isNpc() && target.isAlikeDead() || !World.getInstance().getAroundCharacters(this).contains(target)) {
                  this.getAI().setIntention(CtrlIntention.ACTIVE);
                  this.sendActionFailed();
                  return;
               }

               if (this.isPlayer()) {
                  if (target.isDead() || !target.isVisibleFor(this.getActingPlayer())) {
                     this.getAI().setIntention(CtrlIntention.ACTIVE);
                     this.sendActionFailed();
                     return;
                  }

                  Player actor = this.getActingPlayer();
                  if (actor.isTransformed() && !actor.getTransformation().canAttack()) {
                     this.sendActionFailed();
                     return;
                  }
               } else if (this.isSummon() && target.isDead()) {
                  ((Summon)this).setFollowStatus(true);
                  this.getAI().setIntention(CtrlIntention.ACTIVE);
                  return;
               }
            }

            if (this.getActiveWeaponItem() != null) {
               Weapon wpn = this.getActiveWeaponItem();
               if (wpn != null && !wpn.isAttackWeapon() && !this.isGM()) {
                  if (wpn.getItemType() == WeaponType.FISHINGROD) {
                     this.sendPacket(SystemMessageId.CANNOT_ATTACK_WITH_FISHING_POLE);
                  } else {
                     this.sendPacket(SystemMessageId.THAT_WEAPON_CANT_ATTACK);
                  }

                  this.sendActionFailed();
                  return;
               }
            }

            if (this.getActingPlayer() != null) {
               if (this.getActingPlayer().inObserverMode()) {
                  this.sendPacket(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE);
                  this.sendActionFailed();
                  return;
               }

               if (target.getActingPlayer() != null
                  && this.getActingPlayer().getSiegeState() > 0
                  && this.isInsideZone(ZoneId.SIEGE)
                  && target.getActingPlayer().getSiegeState() == this.getActingPlayer().getSiegeState()
                  && target.getActingPlayer() != this
                  && target.getActingPlayer().getSiegeSide() == this.getActingPlayer().getSiegeSide()) {
                  if (!SiegeManager.getInstance().canAttackSameSiegeSide()) {
                     if (TerritoryWarManager.getInstance().isTWInProgress()) {
                        this.sendPacket(SystemMessageId.YOU_CANNOT_ATTACK_A_MEMBER_OF_THE_SAME_TERRITORY);
                     } else {
                        this.sendPacket(SystemMessageId.FORCED_ATTACK_IS_IMPOSSIBLE_AGAINST_SIEGE_SIDE_TEMPORARY_ALLIED_MEMBERS);
                     }

                     this.sendActionFailed();
                     return;
                  }

                  Clan clan1 = target.getActingPlayer().getClan();
                  Clan clan2 = this.getActingPlayer().getClan();
                  if (clan1 != null
                     && clan2 != null
                     && (clan1.getAllyId() != 0 && clan2.getAllyId() != 0 && clan1.getAllyId() == clan2.getAllyId() || clan1.getId() == clan2.getId())) {
                     if (TerritoryWarManager.getInstance().isTWInProgress()) {
                        this.sendPacket(SystemMessageId.YOU_CANNOT_ATTACK_A_MEMBER_OF_THE_SAME_TERRITORY);
                     } else {
                        this.sendPacket(SystemMessageId.FORCED_ATTACK_IS_IMPOSSIBLE_AGAINST_SIEGE_SIDE_TEMPORARY_ALLIED_MEMBERS);
                     }

                     this.sendActionFailed();
                     return;
                  }
               } else if (target.isInsidePeaceZone(this.getActingPlayer())) {
                  this.getAI().setIntention(CtrlIntention.ACTIVE);
                  this.sendActionFailed();
                  return;
               }
            } else if (this.isInsidePeaceZone(this, target)) {
               this.getAI().setIntention(CtrlIntention.ACTIVE);
               this.sendActionFailed();
               return;
            }

            this.stopEffectsOnAction();
            if (!GeoEngine.canSeeTarget(this, target, false)) {
               this.sendPacket(SystemMessageId.CANT_SEE_TARGET);
               this.getAI().setIntention(CtrlIntention.ACTIVE);
               this.sendActionFailed();
            } else {
               if (this.isAttackable()) {
                  this.stopMove(this.getLocation());
               }

               if (Config.ALT_GAME_TIREDNESS) {
                  this.setCurrentCp(this.getCurrentCp() - 10.0);
               }

               Weapon weaponItem = this.getActiveWeaponItem();
               int timeAtk = this.calculateTimeBetweenAttacks();
               int timeToHit = timeAtk / 2;
               Attack attack = new Attack(this, target, this.isChargedShot(ShotType.SOULSHOTS), weaponItem != null ? weaponItem.getItemGradeSPlus() : 0);
               this.setHeading(Util.calculateHeadingFrom(this, target));
               int reuse = this.calculateReuseTime(weaponItem);
               if (this.isNpc() || this.isSummon()) {
                  this._attackEndTime = System.nanoTime() + TimeUnit.NANOSECONDS.convert((long)timeAtk, TimeUnit.MILLISECONDS);
               }

               boolean hitted = false;
               switch(this.getAttackType()) {
                  case BOW:
                     if (!this.canUseRangeWeapon()) {
                        return;
                     }

                     this._attackEndTime = System.nanoTime() + TimeUnit.NANOSECONDS.convert((long)timeAtk, TimeUnit.MILLISECONDS);
                     hitted = this.doAttackHitByBow(attack, target, timeAtk, reuse);
                     break;
                  case CROSSBOW:
                     if (!this.canUseRangeWeapon()) {
                        return;
                     }

                     this._attackEndTime = System.nanoTime() + TimeUnit.NANOSECONDS.convert((long)timeAtk, TimeUnit.MILLISECONDS);
                     hitted = this.doAttackHitByCrossBow(attack, target, timeAtk, reuse);
                     break;
                  case POLE:
                     this._attackEndTime = System.nanoTime() + TimeUnit.NANOSECONDS.convert((long)timeAtk, TimeUnit.MILLISECONDS);
                     hitted = this.doAttackHitByPole(attack, target, timeToHit);
                     break;
                  case FIST:
                     if (!this.isPlayer()) {
                        this._attackEndTime = System.nanoTime() + TimeUnit.NANOSECONDS.convert((long)timeAtk, TimeUnit.MILLISECONDS);
                        hitted = this.doAttackHitSimple(attack, target, timeToHit);
                        break;
                     }
                  case DUAL:
                  case DUALFIST:
                  case DUALDAGGER:
                     this._attackEndTime = System.nanoTime() + TimeUnit.NANOSECONDS.convert((long)timeAtk, TimeUnit.MILLISECONDS);
                     hitted = this.doAttackHitByDual(attack, target, timeToHit);
                     break;
                  default:
                     this._attackEndTime = System.nanoTime() + TimeUnit.NANOSECONDS.convert((long)timeAtk, TimeUnit.MILLISECONDS);
                     hitted = this.doAttackHitSimple(attack, target, timeToHit);
               }

               Player player = this.getActingPlayer();
               if (player != null) {
                  AttackStanceTaskManager.getInstance().addAttackStanceTask(player);
                  if (player.getSummon() != target) {
                     player.updatePvPStatus(target);
                  }
               }

               if (!hitted) {
                  this.abortAttack();
               } else {
                  this.setChargedShot(ShotType.SOULSHOTS, false);
                  if (player != null) {
                     if (player.isCursedWeaponEquipped()) {
                        if (!target.isInvul()) {
                           target.setCurrentCp(0.0);
                        }
                     } else if (player.isHero() && target.isPlayer() && target.getActingPlayer().isCursedWeaponEquipped()) {
                        target.setCurrentCp(0.0);
                     }
                  }
               }

               if (attack.hasHits()) {
                  this.broadcastPacket(attack);
               }

               ThreadPoolManager.getInstance().schedule(new NotifyAITask(this, CtrlEvent.EVT_READY_TO_ACT), (long)timeAtk);
            }
         }
      }
   }

   private boolean doAttackHitByBow(Attack attack, Creature target, int sAtk, int reuse) {
      int damage1 = 0;
      byte shld1 = 0;
      boolean crit1 = false;
      boolean miss1 = Formulas.calcHitMiss(this, target);
      if (!Config.INFINITE_ARROWS) {
         this.reduceArrowCount(false);
      }

      this._move = null;
      if (!miss1) {
         shld1 = Formulas.calcShldUse(this, target);
         crit1 = Rnd.chance(Formulas.calcCrit(this, target, null, false));
         damage1 = (int)Formulas.calcPhysDam(this, target, null, shld1, crit1, attack.hasSoulshot());
         damage1 = (int)((double)damage1 * (Math.sqrt(this.getDistanceSq(target)) / 4000.0 + 0.8));
      }

      if (this.isPlayer()) {
         this.sendPacket(new SetupGauge(this, 1, sAtk + reuse));
      }

      ThreadPoolManager.getInstance().schedule(new HitTask(this, target, damage1, crit1, miss1, attack.hasSoulshot(), shld1), (long)sAtk);
      this._disableBowAttackEndTime = System.currentTimeMillis() + (long)(sAtk + reuse);
      attack.addHit(target, damage1, miss1, crit1, shld1);
      return !miss1;
   }

   private boolean doAttackHitByCrossBow(Attack attack, Creature target, int sAtk, int reuse) {
      int damage1 = 0;
      byte shld1 = 0;
      boolean crit1 = false;
      boolean miss1 = Formulas.calcHitMiss(this, target);
      if (!Config.INFINITE_ARROWS) {
         this.reduceArrowCount(true);
      }

      this._move = null;
      if (!miss1) {
         shld1 = Formulas.calcShldUse(this, target);
         crit1 = Rnd.chance(Formulas.calcCrit(this, target, null, false));
         damage1 = (int)Formulas.calcPhysDam(this, target, null, shld1, crit1, attack.hasSoulshot());
      }

      if (this.isPlayer()) {
         this.sendPacket(SystemMessageId.CROSSBOW_PREPARING_TO_FIRE);
         SetupGauge sg = new SetupGauge(this, 1, sAtk + reuse);
         this.sendPacket(sg);
      }

      ThreadPoolManager.getInstance().schedule(new HitTask(this, target, damage1, crit1, miss1, attack.hasSoulshot(), shld1), (long)sAtk);
      this._disableBowAttackEndTime = System.currentTimeMillis() + (long)(sAtk + reuse);
      attack.addHit(target, damage1, miss1, crit1, shld1);
      return !miss1;
   }

   private boolean doAttackHitByDual(Attack attack, Creature target, int sAtk) {
      int damage1 = 0;
      int damage2 = 0;
      byte shld1 = 0;
      byte shld2 = 0;
      boolean crit1 = false;
      boolean crit2 = false;
      boolean miss1 = Formulas.calcHitMiss(this, target);
      boolean miss2 = Formulas.calcHitMiss(this, target);
      if (!miss1) {
         shld1 = Formulas.calcShldUse(this, target);
         crit1 = Rnd.chance(Formulas.calcCrit(this, target, null, false));
         damage1 = (int)Formulas.calcPhysDam(this, target, null, shld1, crit1, attack.hasSoulshot());
         damage1 /= 2;
      }

      if (!miss2) {
         shld2 = Formulas.calcShldUse(this, target);
         crit2 = Rnd.chance(Formulas.calcCrit(this, target, null, false));
         damage2 = (int)Formulas.calcPhysDam(this, target, null, shld2, crit2, attack.hasSoulshot());
         damage2 /= 2;
      }

      ThreadPoolManager.getInstance().schedule(new HitTask(this, target, damage1, crit1, miss1, attack.hasSoulshot(), shld1), (long)(sAtk / 2));
      ThreadPoolManager.getInstance().schedule(new HitTask(this, target, damage2, crit2, miss2, attack.hasSoulshot(), shld2), (long)sAtk);
      attack.addHit(target, damage1, miss1, crit1, shld1);
      attack.addHit(target, damage2, miss2, crit2, shld2);
      return !miss1 || !miss2;
   }

   private boolean doAttackHitByPole(Attack attack, Creature target, int sAtk) {
      boolean hitted = this.doAttackHitSimple(attack, target, 100.0, sAtk, true);
      if (this.isAffected(EffectFlag.SINGLE_TARGET)) {
         return hitted;
      } else {
         int attackRandomCountMax = (int)this.getStat().calcStat(Stats.ATTACK_COUNT_MAX, 1.0, null, null);
         int attackcount = 0;
         double attackpercent = 85.0;
         Weapon weaponItem = this.getActiveWeaponItem();
         if (weaponItem == null) {
            return hitted;
         } else {
            int[] _fanRange = weaponItem.getDamageRange();
            if (_fanRange != null) {
               for(Creature obj : World.getInstance().getAroundCharacters(this, _fanRange[2] + this.getPhysicalAttackRange() / 2, 200)) {
                  if (attackcount >= attackRandomCountMax) {
                     break;
                  }

                  if (obj != null
                     && obj != target
                     && (!obj.isPet() || !this.isPlayer() || ((PetInstance)obj).getOwner() != this.getActingPlayer())
                     && Math.abs(obj.getZ() - this.getZ()) <= 650
                     && Util.isOnAngle(this, obj, _fanRange[1], _fanRange[3])
                     && (!this.isAttackable() || !obj.isPlayer() || this.getTarget() == null || !this.getTarget().isAttackable())
                     && !obj.isAlikeDead()
                     && (obj == this.getAI().getAttackTarget() || obj.isAutoAttackable(this))) {
                     hitted |= this.doAttackHitSimple(attack, obj, attackpercent, sAtk, true);
                     attackpercent /= 1.15;
                     ++attackcount;
                  }
               }
            }

            return hitted;
         }
      }
   }

   private boolean doAttackHitSimple(Attack attack, Creature target, int sAtk) {
      return this.doAttackHitSimple(attack, target, 100.0, sAtk, false);
   }

   private boolean doAttackHitSimple(Attack attack, Creature target, double attackpercent, int sAtk, boolean isPoleAtk) {
      int damage1 = 0;
      byte shld1 = 0;
      boolean crit1 = false;
      boolean miss1 = Formulas.calcHitMiss(this, target);
      if (!miss1) {
         shld1 = Formulas.calcShldUse(this, target);
         crit1 = Rnd.chance(Formulas.calcCrit(this, target, null, false));
         damage1 = (int)Formulas.calcPhysDam(this, target, null, shld1, crit1, attack.hasSoulshot());
         if (attackpercent != 100.0) {
            damage1 = (int)((double)damage1 * attackpercent / 100.0);
         }

         Player player = this.getActingPlayer();
         if (player != null && isPoleAtk && player.getTarget() != null && player.getTarget() != target) {
            player.updatePvPStatus(target);
         }
      }

      ThreadPoolManager.getInstance().schedule(new HitTask(this, target, damage1, crit1, miss1, attack.hasSoulshot(), shld1), (long)sAtk);
      attack.addHit(target, damage1, miss1, crit1, shld1);
      return !miss1;
   }

   public void doCast(Skill skill) {
      this.beginCast(skill, false);
   }

   public void doSimultaneousCast(Skill skill) {
      this.beginCast(skill, true);
   }

   public void doCast(Skill skill, Creature target, GameObject[] targets) {
      if (!this.checkDoCastConditions(skill, true)) {
         this.setIsCastingNow(false);
      } else if (skill.isSimultaneousCast()) {
         this.doSimultaneousCast(skill, target, targets);
      } else {
         this.stopEffectsOnAction();
         this.beginCast(skill, false, target, targets);
      }
   }

   public void doSimultaneousCast(Skill skill, Creature target, GameObject[] targets) {
      if (!this.checkDoCastConditions(skill, true)) {
         this.setIsCastingSimultaneouslyNow(false);
      } else {
         this.stopEffectsOnAction();
         this.beginCast(skill, true, target, targets);
      }
   }

   private void beginCast(Skill skill, boolean simultaneously) {
      boolean abort = false;
      Creature target = this.getTarget() != null ? (Creature)this.getTarget() : (this.isPlayable() ? this.getAI().getCastTarget() : null);
      if (target != null && this.isInFightEvent()) {
         for(AbstractFightEvent e : this.getFightEvents()) {
            if (e != null && !e.canUseMagic(target, this, skill)) {
               abort = true;
            }
         }
      }

      if (this.checkDoCastConditions(skill, true) && !abort) {
         if (skill.isSimultaneousCast() && !simultaneously) {
            simultaneously = true;
         }

         this.stopEffectsOnAction();
         GameObject[] targets = skill.getTargetList(this);
         boolean doit = false;
         switch(skill.getTargetType()) {
            case AREA_SUMMON:
               target = this.getSummon();
               break;
            case AURA:
            case AURA_CORPSE_MOB:
            case FRONT_AURA:
            case BEHIND_AURA:
            case GROUND:
               target = this;
               break;
            case PET:
            case SELF:
            case SERVITOR:
            case SUMMON:
            case OWNER_PET:
            case PARTY:
            case PARTY_NOTME:
            case CLAN:
            case PARTY_CLAN:
            case CORPSE_CLAN:
            case COMMAND_CHANNEL:
            case AURA_UNDEAD_ENEMY:
               doit = true;
            default:
               if (targets.length == 0) {
                  if (simultaneously) {
                     this.setIsCastingSimultaneouslyNow(false);
                  } else {
                     this.setIsCastingNow(false);
                  }

                  if (this.isPlayer()) {
                     this.sendActionFailed();
                     this.getAI().setIntention(CtrlIntention.ACTIVE);
                  } else if (this.isSummon()) {
                     this.getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING);
                  }

                  return;
               }

               switch(skill.getSkillType()) {
                  case BUFF:
                     doit = true;
                     break;
                  case DUMMY:
                     if (skill.hasEffectType(EffectType.CPHEAL, EffectType.HEAL)) {
                        doit = true;
                     }
               }

               target = doit ? (Creature)targets[0] : target;
         }

         this.beginCast(skill, simultaneously, target, targets);
      } else {
         if (simultaneously) {
            this.setIsCastingSimultaneouslyNow(false);
         } else {
            this.setIsCastingNow(false);
         }
      }
   }

   private void beginCast(Skill skill, boolean simultaneously, Creature target, GameObject[] targets) {
      if (target != null && this.getEvents().onMagic(skill, simultaneously, target, targets)) {
         if (this.isPlayable() && skill.getReferenceItemId() > 0 && ItemsParser.getInstance().getTemplate(skill.getReferenceItemId()).getBodyPart() == 4194304
            )
          {
            for(ItemInstance item : this.getInventory().getItemsByItemId(skill.getReferenceItemId())) {
               if (item.isEquipped()) {
                  if (item.getMana() < item.useSkillDisTime()) {
                     this.sendPacket(SystemMessageId.THERE_ARE_NOT_ENOUGH_NECESSARY_ITEMS_TO_USE_THE_SKILL);
                     this.abortCast();
                     return;
                  }

                  item.decreaseMana(false, item.useSkillDisTime());
                  break;
               }
            }
         }

         switch(skill.getFlyType()) {
            case DUMMY:
            case CHARGE:
               Location flyLoc = this.getFlyLocation(target, skill);
               if (flyLoc == null) {
                  this.sendPacket(SystemMessageId.CANT_SEE_TARGET);
                  this.setIsCastingNow(false);
                  return;
               }

               this._flyLoc = flyLoc;
               this._isFlyingNow = true;
         }

         if (skill.getSkillType() == SkillType.ENERGY_SPEND) {
            ItemInstance item = this.getInventory().getPaperdollItem(15);
            if (item != null && item.getAgathionEnergy() < skill.getEnergyConsume()) {
               this.sendPacket(SystemMessageId.THE_SKILL_HAS_BEEN_CANCELED_BECAUSE_YOU_HAVE_INSUFFICIENT_ENERGY);
               this.setIsCastingNow(false);
               return;
            }
         }

         if (skill.getSkillType() == SkillType.ENERGY_REPLENISH) {
            ItemInstance item = this.getInventory().getPaperdollItem(15);
            if (item == null) {
               this.sendPacket(SystemMessageId.YOUR_ENERGY_CANNOT_BE_REPLENISHED_BECAUSE_CONDITIONS_ARE_NOT_MET);
               this.setIsCastingNow(false);
               return;
            }

            if (item.getItem().getAgathionMaxEnergy() - item.getAgathionEnergy() < skill.getEnergyConsume()) {
               this.sendPacket(SystemMessageId.NOTHING_HAPPENED);
               this.setIsCastingNow(false);
               return;
            }
         }

         if (skill.getSkillType() != SkillType.RESURRECT || !this.isResurrectionBlocked() && !target.isResurrectionBlocked()) {
            if (skill.getHitTime() > 100 && !skill.isSimultaneousCast()) {
               this.getAI().clientStopMoving(null);
            }

            int magicId = skill.getId();
            int hitTime = skill.getHitTime();
            int coolTime = skill.getCoolTime();
            int skillInterruptTime = !skill.isMagic() && !skill.isStatic() ? 0 : Formulas.calcAtkSpd(this, skill, (double)skill.getSkillInterruptTime());
            boolean effectWhileCasting = skill.getSkillType() == SkillType.FUSION || skill.getSkillType() == SkillType.SIGNET_CASTTIME;
            if (!effectWhileCasting) {
               hitTime = Formulas.calcAtkSpd(this, skill, (double)hitTime);
               if (coolTime > 0) {
                  coolTime = Formulas.calcAtkSpd(this, skill, (double)coolTime);
               }
            }

            this._animationEndTime = System.currentTimeMillis() + (long)hitTime;
            if (skill.isMagic() && !effectWhileCasting && (this.isChargedShot(ShotType.SPIRITSHOTS) || this.isChargedShot(ShotType.BLESSED_SPIRITSHOTS))) {
               hitTime = (int)(0.7 * (double)hitTime);
               coolTime = (int)(0.7 * (double)coolTime);
               skillInterruptTime = (int)(0.7 * (double)skillInterruptTime);
            }

            if (!effectWhileCasting && !skill.isStatic()) {
               if (skill.getHitTime() >= 500 && hitTime < 500) {
                  hitTime = Config.MIN_HIT_TIME;
               }
            } else {
               hitTime = skill.getHitTime();
               coolTime = skill.getCoolTime();
            }

            if (!skill.isStatic()
               && !effectWhileCasting
               && !skill.isSimultaneousCast()
               && !skill.isHealingPotionSkill()
               && skill.getHitTime() < 500
               && skill.getHitTime() > 0
               && hitTime < 500) {
               hitTime = Config.MIN_HIT_TIME;
            }

            hitTime = skill.getFlyType() != null && skill.getFlyType() == FlyToLocation.FlyType.DUMMY ? skill.getHitTime() : hitTime;
            if (this.isCastingSimultaneouslyNow() && simultaneously) {
               ThreadPoolManager.getInstance().schedule(() -> this.beginCast(skill, simultaneously, target, targets), 100L);
            } else {
               if (simultaneously) {
                  this.setIsCastingSimultaneouslyNow(true);
               } else {
                  this.setIsCastingNow(true);
               }

               int reuseDelay;
               if (skill.isStaticReuse() || skill.isStatic()) {
                  reuseDelay = skill.getReuseDelay();
               } else if (skill.isMagic()) {
                  reuseDelay = (int)((double)skill.getReuseDelay() * this.calcStat(Stats.MAGIC_REUSE_RATE, 1.0, null, null));
               } else {
                  reuseDelay = (int)((double)skill.getReuseDelay() * this.calcStat(Stats.P_REUSE, 1.0, null, null));
               }

               boolean skillMastery = Formulas.calcSkillMastery(this, skill);
               if ((!skill.isHandler() || reuseDelay >= 30000) && !skillMastery) {
                  this.addTimeStamp(skill, (long)reuseDelay);
               }

               int initmpcons = this.getStat().getMpInitialConsume(skill);
               if (initmpcons > 0) {
                  this.getStatus().reduceMp((double)initmpcons);
                  StatusUpdate su = new StatusUpdate(this);
                  su.addAttribute(11, (int)this.getCurrentMp());
                  this.sendPacket(su);
               }

               if (skillMastery) {
                  reuseDelay = 0;
                  if (this.getActingPlayer() != null) {
                     this.getActingPlayer().sendPacket(SystemMessageId.SKILL_READY_TO_USE_AGAIN);
                  }
               }

               if (!skill.isHandler() || reuseDelay > 10) {
                  this.disableSkill(skill, (long)reuseDelay);
               }

               if (target != this) {
                  this.setHeading(Util.calculateHeadingFrom(this, target));
                  this.broadcastPacket(new ExRotation(this.getObjectId(), this.getHeading()));
               }

               if (this.isPlayer() && skill.getChargeConsume() > 0) {
                  this.getActingPlayer().decreaseCharges(skill.getChargeConsume());
               }

               if (effectWhileCasting) {
                  if (skill.getItemConsumeId() > 0 && !this.destroyItemByItemId("Consume", skill.getItemConsumeId(), (long)skill.getItemConsume(), null, true)
                     )
                   {
                     this.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
                     if (simultaneously) {
                        this.setIsCastingSimultaneouslyNow(false);
                     } else {
                        this.setIsCastingNow(false);
                     }

                     if (this.isPlayer()) {
                        this.getAI().setIntention(CtrlIntention.ACTIVE);
                     }

                     return;
                  }

                  if (skill.getMaxSoulConsumeCount() > 0 && this.isPlayer() && !this.getActingPlayer().decreaseSouls(skill.getMaxSoulConsumeCount(), skill)) {
                     if (simultaneously) {
                        this.setIsCastingSimultaneouslyNow(false);
                     } else {
                        this.setIsCastingNow(false);
                     }

                     return;
                  }

                  switch(skill.getSkillType()) {
                     case FUSION:
                        this.startFusionSkill(target, skill);
                        break;
                     default:
                        this.callSkill(skill, targets);
                  }
               }

               if (!skill.isToggle()) {
                  this.broadcastPacket(new MagicSkillUse(this, target, skill.getDisplayId(), skill.getDisplayLevel(), hitTime, reuseDelay));
                  if (this._flyLoc != null && this._isFlyingNow) {
                     this._isFlyingNow = false;
                     this._move = null;
                     this.broadcastPacket(new FlyToLocation(this, this._flyLoc, skill.getFlyType()));
                  }
               }

               if (this.isPlayer()) {
                  SystemMessage sm = null;
                  switch(magicId) {
                     case 1312:
                        break;
                     case 2046:
                        sm = SystemMessage.getSystemMessage(SystemMessageId.SUMMON_A_PET);
                        break;
                     default:
                        sm = SystemMessage.getSystemMessage(SystemMessageId.USE_S1);
                        sm.addSkillName(skill);
                  }

                  this.sendPacket(sm);
               }

               if (this.isPlayable()
                  && !effectWhileCasting
                  && skill.getItemConsumeId() > 0
                  && !this.destroyItemByItemId("Consume", skill.getItemConsumeId(), (long)skill.getItemConsume(), null, true)) {
                  this.getActingPlayer().sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
                  this.abortCast();
               } else {
                  for(int negateSkillId : skill.getNegateCasterId()) {
                     if (negateSkillId != 0) {
                        this.stopSkillEffects(negateSkillId);
                     }
                  }

                  this._castingSkill = skill;
                  this._castInterruptTime = System.currentTimeMillis() + (long)skillInterruptTime;
                  this.setCastingTarget(target);
                  MagicUseTask mut = new MagicUseTask(this, targets, skill, hitTime, coolTime, simultaneously);
                  if (this.isPlayer()
                     && (effectWhileCasting && skill.getSkillType() == SkillType.SIGNET_CASTTIME || !effectWhileCasting)
                     && !skill.isToggle()
                     && hitTime >= 100) {
                     this.sendPacket(new SetupGauge(this, 0, hitTime));
                  }

                  if (hitTime > 410 && !skill.isToggle()) {
                     if (skill.getHitCounts() > 0) {
                        hitTime = hitTime * skill.getHitTimings()[0] / 100;
                     }

                     if (effectWhileCasting) {
                        mut.setPhase(2);
                     }

                     if (simultaneously) {
                        Future<?> future = this._skillCast2;
                        if (future != null) {
                           future.cancel(true);
                           this._skillCast2 = null;
                        }

                        this._skillCast2 = ThreadPoolManager.getInstance().schedule(mut, (long)(hitTime - 400));
                     } else {
                        Future<?> future = this._skillCast;
                        if (future != null) {
                           future.cancel(true);
                           this._skillCast = null;
                        }

                        this._skillCast = ThreadPoolManager.getInstance().schedule(mut, (long)(hitTime - 400));
                     }

                     this._skillGeoCheckTask = null;
                     if (!skill.isDisableGeoCheck() && (skill.getCastRange() > 0 || skill.getEffectRange() > 0) && mut.getHitTime() > 550) {
                        this._skillGeoCheckTask = ThreadPoolManager.getInstance()
                           .schedule(new MagicGeoCheckTask(this), (long)((double)mut.getHitTime() * 0.5));
                     }
                  } else {
                     mut.setHitTime(0);
                     this.onMagicLaunchedTimer(mut);
                  }
               }
            }
         } else {
            this.sendPacket(SystemMessageId.REJECT_RESURRECTION);
            target.sendPacket(SystemMessageId.REJECT_RESURRECTION);
            if (simultaneously) {
               this.setIsCastingSimultaneouslyNow(false);
            } else {
               this.setIsCastingNow(false);
            }

            if (this.isPlayer()) {
               this.sendActionFailed();
               this.getAI().setIntention(CtrlIntention.ACTIVE);
            }
         }
      } else {
         if (simultaneously) {
            this.setIsCastingSimultaneouslyNow(false);
         } else {
            this.setIsCastingNow(false);
         }

         if (this.isPlayer()) {
            this.sendActionFailed();
            this.getAI().setIntention(CtrlIntention.ACTIVE);
         }
      }
   }

   public boolean checkDoCastConditions(Skill skill, boolean msg) {
      if (skill != null
         && !this.isSkillDisabled(skill)
         && !this.isSkillBlocked(skill)
         && (
            skill.getFlyRadius() <= 0 && skill.getFlyType() == FlyToLocation.FlyType.NONE
               || !this.isMovementDisabled()
               || skill.getId() == 628
               || skill.getId() == 821
         )) {
         if (this.isPlayer() && this.isInsideZone(ZoneId.FUN_PVP)) {
            FunPvpZone zone = ZoneManager.getInstance().getZone(this, FunPvpZone.class);
            if (zone != null && !zone.checkSkill(skill)) {
               if (msg) {
                  this.sendMessage("You cannot use " + skill.getNameEn() + " inside this zone.");
               }

               return false;
            }
         }

         if (this.getTarget() != null && skill.getFlyType() == FlyToLocation.FlyType.CHARGE && this.getDistanceSq(this.getTarget()) < 200.0) {
            if (msg) {
               this.sendPacket(SystemMessageId.NOT_ENOUGH_SPACE_FOR_SKILL);
            }

            return false;
         } else if (this.getCurrentMp() < (double)(this.getStat().getMpConsume(skill) + this.getStat().getMpInitialConsume(skill))) {
            if (msg) {
               this.sendPacket(SystemMessageId.NOT_ENOUGH_MP);
            }

            this.sendActionFailed();
            return false;
         } else if (this.getCurrentHp() <= (double)skill.getHpConsume()) {
            if (msg) {
               this.sendPacket(SystemMessageId.NOT_ENOUGH_HP);
            }

            this.sendActionFailed();
            return false;
         } else {
            if (!skill.isStatic()) {
               if (skill.isMagic()) {
                  if (this.isMuted()) {
                     this.sendActionFailed();
                     return false;
                  }
               } else if (this.isPhysicalMuted()) {
                  this.sendActionFailed();
                  return false;
               }
            }

            switch(skill.getSkillType()) {
               case SIGNET:
               case SIGNET_CASTTIME:
                  boolean canCast = true;
                  if (skill.getTargetType() == TargetType.GROUND && this.isPlayer()) {
                     Location wp = this.getActingPlayer().getCurrentSkillWorldPosition();
                     if (!this.checkEffectRangeInsidePeaceZone(skill, wp.getX(), wp.getY(), wp.getZ())) {
                        canCast = false;
                     }
                  } else if (!this.checkEffectRangeInsidePeaceZone(skill, this.getX(), this.getY(), this.getZ())) {
                     canCast = false;
                  }

                  if (!canCast && !this.isInFightEvent()) {
                     if (msg) {
                        SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
                        sm.addSkillName(skill);
                        this.sendPacket(sm);
                     }

                     return false;
                  }
               default:
                  if (this.getActiveWeaponItem() != null && !this.isGM()) {
                     Weapon wep = this.getActiveWeaponItem();
                     if (wep != null && wep.useWeaponSkillsOnly() && wep.hasSkills()) {
                        boolean found = false;

                        for(SkillHolder sh : wep.getSkills()) {
                           if (sh.getId() == skill.getId()) {
                              found = true;
                           }
                        }

                        if (!found) {
                           if (this.getActingPlayer() != null && !this.getActingPlayer().isCombatFlagEquipped() && msg) {
                              this.sendPacket(SystemMessageId.WEAPON_CAN_USE_ONLY_WEAPON_SKILL);
                           }

                           return false;
                        }
                     }
                  }

                  if (skill.getItemConsumeId() > 0 && this.getInventory() != null) {
                     ItemInstance requiredItems = this.getInventory().getItemByItemId(skill.getItemConsumeId());
                     if (requiredItems == null || requiredItems.getCount() < (long)skill.getItemConsume()) {
                        if (skill.getSkillType() == SkillType.SUMMON) {
                           if (msg) {
                              SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SUMMONING_SERVITOR_COSTS_S2_S1);
                              sm.addItemName(skill.getItemConsumeId());
                              sm.addNumber(skill.getItemConsume());
                              this.sendPacket(sm);
                           }
                        } else if (msg) {
                           this.sendPacket(SystemMessageId.THERE_ARE_NOT_ENOUGH_NECESSARY_ITEMS_TO_USE_THE_SKILL);
                        }

                        return false;
                     }
                  }

                  return true;
            }
         }
      } else {
         this.sendActionFailed();
         return false;
      }
   }

   public void addTimeStampItem(ItemInstance item, long reuse, boolean byCron) {
   }

   public long getItemRemainingReuseTime(int itemObjId) {
      return -1L;
   }

   public void addTimeStamp(Skill skill, long reuse) {
   }

   public void addTimeStamp(Skill skill, long reuse, long systime) {
   }

   public long getSkillRemainingReuseTime(int skillReuseHashId) {
      return -1L;
   }

   public void startFusionSkill(Creature target, Skill skill) {
      if (skill.getSkillType() == SkillType.FUSION) {
         if (this._fusionSkill == null) {
            this._fusionSkill = new FusionSkill(this, target, skill);
         }
      }
   }

   @Override
   public void doDie(Creature killer) {
      synchronized(this) {
         if (this.isDead()) {
            return;
         }

         this.getStatus().stopHpMpRegeneration();
         this.setCurrentHp(0.0);
         this.setIsDead(true);
      }

      this.onDeath(killer);
   }

   protected void onDeath(Creature killer) {
      this.setTarget(null);
      this.stopMove(null);
      this.getStatus().stopHpMpRegeneration();
      boolean fightEventKeepBuffs = this.isPlayer() && this.isInFightEvent() && !this.getFightEvent().loseBuffsOnDeath(this.getActingPlayer());
      if (this.isPlayable() && ((Playable)this).isPhoenixBlessed()) {
         if (((Playable)this).isCharmOfLuckAffected()) {
            this.stopEffects(EffectType.CHARM_OF_LUCK);
         }

         if (((Playable)this).isNoblesseBlessed()) {
            this.stopEffects(EffectType.NOBLESSE_BLESSING);
         }
      } else if (this.isPlayable() && ((Playable)this).isNoblesseBlessed()) {
         this.stopEffects(EffectType.NOBLESSE_BLESSING);
         if (((Playable)this).isCharmOfLuckAffected()) {
            this.stopEffects(EffectType.CHARM_OF_LUCK);
         }
      } else if (!fightEventKeepBuffs) {
         this.stopAllEffectsExceptThoseThatLastThroughDeath();
      }

      if (this.isPlayer() && this.getActingPlayer().getAgathionId() != 0) {
         this.getActingPlayer().setAgathionId(0);
      }

      this.calculateRewards(killer);
      this.broadcastStatusUpdate();
      if (this.hasAI()) {
         this.getAI().stopAllTaskAndTimers();
         ThreadPoolManager.getInstance().schedule(new NotifyAITask(this, CtrlEvent.EVT_DEAD, killer), 100L);
      }

      this.onDeathInZones(this);
      this.getAttackByList().clear();
      if (this.isSummon() && ((Summon)this).isPhoenixBlessed() && ((Summon)this).getOwner() != null) {
         ((Summon)this).getOwner().reviveRequest(((Summon)this).getOwner(), null, true);
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
      } catch (Exception var5) {
         _log.log(Level.SEVERE, "deleteMe()", (Throwable)var5);
      }
   }

   public void deleteMe() {
      this.setDebug(null);
      if (this.hasAI()) {
         this.getAI().stopAllTaskAndTimers();
         this.getAI().stopAITask();
      }
   }

   public void detachAI() {
      if (!this.isWalker()) {
         this.setAI(null);
      }
   }

   protected void calculateRewards(Creature killer) {
   }

   public void doRevive() {
      if (this.isDead()) {
         if (!this.isTeleporting()) {
            this.setIsPendingRevive(false);
            this.setIsDead(false);
            boolean restorefull = false;
            if (this.isPlayable() && ((Playable)this).isPhoenixBlessed() || this.isPlayer() && this.getActingPlayer().isInFightEvent()) {
               restorefull = true;
               this.stopEffects(EffectType.PHOENIX_BLESSING);
            }

            if (restorefull) {
               this._status.setCurrentCp(this.getCurrentCp());
               this._status.setCurrentHp(this.getMaxHp());
               this._status.setCurrentMp(this.getMaxMp());
            } else {
               if (Config.RESPAWN_RESTORE_CP > 0.0 && this.getCurrentCp() < this.getMaxCp() * Config.RESPAWN_RESTORE_CP) {
                  this._status.setCurrentCp(this.getMaxCp() * Config.RESPAWN_RESTORE_CP);
               }

               if (Config.RESPAWN_RESTORE_HP > 0.0 && this.getCurrentHp() < this.getMaxHp() * Config.RESPAWN_RESTORE_HP) {
                  this._status.setCurrentHp(this.getMaxHp() * Config.RESPAWN_RESTORE_HP);
               }

               if (Config.RESPAWN_RESTORE_MP > 0.0 && this.getCurrentMp() < this.getMaxMp() * Config.RESPAWN_RESTORE_MP) {
                  this._status.setCurrentMp(this.getMaxMp() * Config.RESPAWN_RESTORE_MP);
               }
            }

            this.broadcastPacket(new Revive(this));
            this.onReviveInZones(this);
         } else {
            this.setIsPendingRevive(true);
         }
      }
   }

   public void doRevive(double revivePower) {
      this.doRevive();
   }

   public CharacterAI getAI() {
      if (this._ai == null) {
         synchronized(this) {
            if (this._ai == null) {
               return this._ai = this.initAI();
            }
         }
      }

      return this._ai;
   }

   protected CharacterAI initAI() {
      return new CharacterAI(this);
   }

   public void setAI(CharacterAI newAI) {
      CharacterAI oldAI = this._ai;
      if (oldAI != null && oldAI != newAI && oldAI instanceof DefaultAI) {
         oldAI.stopAITask();
      }

      this._ai = newAI;
   }

   public boolean hasAI() {
      return this._ai != null;
   }

   public boolean isRaid() {
      return false;
   }

   public boolean isEpicRaid() {
      return false;
   }

   public boolean isSiegeGuard() {
      return false;
   }

   public boolean isRaidMinion() {
      return false;
   }

   public final Set<Creature> getAttackByList() {
      if (this._attackByList == null) {
         synchronized(this) {
            if (this._attackByList == null) {
               this._attackByList = ConcurrentHashMap.newKeySet();
            }
         }
      }

      return this._attackByList;
   }

   public boolean isNoRndWalk() {
      return this._isNoRndWalk;
   }

   public final void setIsNoRndWalk(boolean value) {
      this._isNoRndWalk = value;
   }

   public final boolean isAfraid() {
      return this.isAffected(EffectFlag.FEAR);
   }

   public final boolean isAllSkillsDisabled() {
      return this._allSkillsDisabled || this.isStunned() || this.isSleeping() || this.isParalyzed();
   }

   public boolean isAttackingDisabled() {
      return this.isStunned()
         || this.isSleeping()
         || this.isAttackingNow()
         || this.isAlikeDead()
         || this.isParalyzed()
         || this.isPhysicalAttackMuted()
         || this.isCoreAIDisabled();
   }

   public final Calculator[] getCalculators() {
      return this._calculators;
   }

   public final boolean isConfused() {
      return this.isAffected(EffectFlag.CONFUSED);
   }

   public boolean isAlikeDead() {
      return this._isDead;
   }

   public final boolean isDead() {
      return this._isDead;
   }

   public final void setIsDead(boolean value) {
      this._isDead = value;
   }

   public boolean isImmobilized() {
      return this._isImmobilized;
   }

   public void setIsImmobilized(boolean value) {
      this._isImmobilized = value;
   }

   public final boolean isMuted() {
      return this.isAffected(EffectFlag.MUTED);
   }

   public final boolean isPhysicalMuted() {
      return this.isAffected(EffectFlag.PSYCHICAL_MUTED);
   }

   public final boolean isPhysicalAttackMuted() {
      return this.isAffected(EffectFlag.PSYCHICAL_ATTACK_MUTED);
   }

   public boolean isMovementDisabled() {
      return this.isBlocked()
         || this.isStunned()
         || this.isRooted()
         || this.isSleeping()
         || this.isOverloaded()
         || this.isParalyzed()
         || this.isImmobilized()
         || this.isAlikeDead()
         || this.isTeleporting();
   }

   public boolean isActionsDisabled() {
      if (Config.CHECK_ATTACK_STATUS_TO_MOVE && this.isAttackingNow()) {
         return true;
      } else {
         return this.isBlocked() || this.isAlikeDead() || this.isStunned() || this.isSleeping() || this.isParalyzed() || this.isCastingNow();
      }
   }

   public final boolean isOutOfControl() {
      return this.isBlocked() || this.isConfused() || this.isAfraid();
   }

   public final boolean isOverloaded() {
      return this._isOverloaded;
   }

   public final void setIsOverloaded(boolean value) {
      this._isOverloaded = value;
   }

   public final boolean isParalyzed() {
      return this._isParalyzed || this.isAffected(EffectFlag.PARALYZED);
   }

   public final void setIsParalyzed(boolean value) {
      this._isParalyzed = value;
   }

   public final boolean isPendingRevive() {
      return this.isDead() && this._isPendingRevive;
   }

   public final void setIsPendingRevive(boolean value) {
      this._isPendingRevive = value;
   }

   public final boolean isDisarmed() {
      return this.isAffected(EffectFlag.DISARMED);
   }

   public Summon getSummon() {
      return null;
   }

   public final boolean hasSummon() {
      return this.getSummon() != null;
   }

   public final boolean hasPet() {
      return this.hasSummon() && this.getSummon().isPet();
   }

   public final boolean hasServitor() {
      return this.hasSummon() && this.getSummon().isServitor();
   }

   public void startRooted(boolean rooted) {
      this._isRooted = rooted;
   }

   public final boolean isRooted() {
      return this.isAffected(EffectFlag.ROOTED) || this._isRooted;
   }

   public boolean isRunning() {
      return this._isRunning;
   }

   public final void setIsRunning(boolean value) {
      this._isRunning = value;
      if (this.getRunSpeed() != 0.0) {
         this.broadcastPacket(new ChangeMoveType(this));
      }

      if (this.isPlayer()) {
         this.getActingPlayer().broadcastUserInfo(true);
      } else if (this.isSummon()) {
         this.broadcastStatusUpdate();
      } else if (this.isNpc()) {
         this.broadcastInfo();
      }
   }

   public final void setRunning() {
      if (!this.isRunning()) {
         this.setIsRunning(true);
      }
   }

   public final boolean isSleeping() {
      return this.isAffected(EffectFlag.SLEEP);
   }

   public final boolean isStunned() {
      return this._isStunned;
   }

   public final boolean isBetrayed() {
      return this.isAffected(EffectFlag.BETRAYED);
   }

   public final boolean isTeleporting() {
      return this._isTeleporting;
   }

   public void setIsTeleporting(boolean value) {
      this._isTeleporting = value;
   }

   public void setIsInvul(boolean b) {
      this._isInvul = b;
   }

   public boolean isInvul() {
      return this._isInvul || this._isTeleporting;
   }

   public void setIsMortal(boolean b) {
      this._isMortal = b;
   }

   public boolean isMortal() {
      return this._isMortal;
   }

   public boolean isUndead() {
      return false;
   }

   public boolean isResurrectionBlocked() {
      return this.isAffected(EffectFlag.BLOCK_RESURRECTION);
   }

   @Override
   public boolean isInWater(GameObject object) {
      return object.isInsideZone(ZoneId.WATER) && !object.isFlying();
   }

   public boolean isInVehicle() {
      return this.isPlayer() && this.getActingPlayer().isInVehicle();
   }

   @Override
   public final boolean isFlying() {
      return this._isFlying;
   }

   public final void setIsFlying(boolean mode) {
      this._isFlying = mode;
   }

   public CharStat getStat() {
      return this._stat;
   }

   public void initCharStat() {
      this._stat = new CharStat(this);
   }

   public final void setStat(CharStat value) {
      this._stat = value;
   }

   public CharStatus getStatus() {
      return this._status;
   }

   public void initCharStatus() {
      this._status = new CharStatus(this);
   }

   public final void setStatus(CharStatus value) {
      this._status = value;
   }

   public void initCharEvents() {
      this._events = new CharEvents(this);
   }

   public void setCharEvents(CharEvents events) {
      this._events = events;
   }

   public CharEvents getEvents() {
      return this._events;
   }

   public CharTemplate getTemplate() {
      return this._template;
   }

   protected final void setTemplate(CharTemplate template) {
      this._template = template;
   }

   public final String getTitle() {
      return this._title;
   }

   public final void setTitle(String value) {
      if (value == null) {
         this._title = "";
      } else {
         this._title = value.length() > 21 ? value.substring(0, 20) : value;
      }
   }

   public final void setWalking() {
      if (this.isRunning()) {
         this.setIsRunning(false);
      }
   }

   public final CharEffectList getEffectList() {
      return this._effects;
   }

   public void addEffect(Effect newEffect) {
      this._effects.queueEffect(newEffect, false, true);
   }

   public final void removeEffect(Effect effect, boolean printMessage) {
      this._effects.queueEffect(effect, true, printMessage);
   }

   public final void startAbnormalEffect(AbnormalEffect ae) {
      if (ae != AbnormalEffect.NONE) {
         this._abnormalEffects.add(ae);
         if (ae.isSpecial()) {
            this._abnormalEffectsMask2 |= ae.getMask();
         } else if (ae.isEvent()) {
            this._abnormalEffectsMask3 |= ae.getMask();
         } else {
            this._abnormalEffectsMask |= ae.getMask();
         }

         this.updateAbnormalEffect();
      }
   }

   public Set<AbnormalEffect> getAbnormalEffects() {
      return this._abnormalEffects;
   }

   public AbnormalEffect[] getAbnormalEffectsArray() {
      return this._abnormalEffects.toArray(new AbnormalEffect[this._abnormalEffects.size()]);
   }

   public int getAbnormalEffectMask() {
      int ae = this._abnormalEffectsMask;
      if (!this.isFlying() && this.isStunned()) {
         if (this.isIsDanceStun()) {
            ae |= AbnormalEffect.DANCE_STUNNED.getMask();
         } else {
            ae |= AbnormalEffect.STUN.getMask();
         }
      }

      if (!this.isFlying() && this.isRooted()) {
         ae |= AbnormalEffect.ROOT.getMask();
      }

      if (this.isSleeping()) {
         ae |= AbnormalEffect.SLEEP.getMask();
      }

      if (this.isConfused()) {
         ae |= AbnormalEffect.FEAR.getMask();
      }

      if (this.isMuted()) {
         ae |= AbnormalEffect.MUTED.getMask();
      }

      if (this.isPhysicalMuted()) {
         ae |= AbnormalEffect.MUTED.getMask();
      }

      if (this.isAfraid()) {
         ae |= AbnormalEffect.SKULL_FEAR.getMask();
      }

      return ae;
   }

   public int getAbnormalEffectMask2() {
      int se = this._abnormalEffectsMask2;
      if (this.isFlying() && this.isStunned()) {
         se |= AbnormalEffect.S_AIR_STUN.getMask();
      }

      if (this.isFlying() && this.isRooted()) {
         se |= AbnormalEffect.S_AIR_ROOT.getMask();
      }

      return se;
   }

   public int getAbnormalEffectMask3() {
      return this._abnormalEffectsMask3;
   }

   public void stopAbnormalEffect(AbnormalEffect ae) {
      this._abnormalEffects.remove(ae);
      if (ae.isSpecial()) {
         this._abnormalEffectsMask2 &= ~ae.getMask();
      }

      if (ae.isEvent()) {
         this._abnormalEffectsMask3 &= ~ae.getMask();
      } else {
         this._abnormalEffectsMask &= ~ae.getMask();
      }

      this.updateAbnormalEffect();
   }

   public final void startFakeDeath() {
      if (this.isPlayer()) {
         this.abortAttack();
         this.abortCast();
         this.stopMove(null);
         this.getAI().notifyEvent(CtrlEvent.EVT_FAKE_DEATH);
         this.broadcastPacket(new ChangeWaitType(this, 2));
         this._isFakeDeath = true;
      }
   }

   public final void setIsStuned(boolean isStuned) {
      this._isStunned = isStuned;
      if (!isStuned && !this.isPlayer()) {
         this.getAI().notifyEvent(CtrlEvent.EVT_THINK);
      }

      this.updateAbnormalEffect();
   }

   public final void startConfused() {
      this.getAI().notifyEvent(CtrlEvent.EVT_CONFUSED);
      this.updateAbnormalEffect();
   }

   public final void stopConfused() {
      if (!this.isPlayer()) {
         this.getAI().notifyEvent(CtrlEvent.EVT_THINK);
      }

      this.updateAbnormalEffect();
   }

   public final void startParalyze() {
      this.abortAttack();
      this.abortCast();
      this.stopMove(null);
      this.getAI().notifyEvent(CtrlEvent.EVT_PARALYZED);
   }

   public void stopAllEffects() {
      this._effects.stopAllEffects();
   }

   public void stopAllEffectsExceptThoseThatLastThroughDeath() {
      this._effects.stopAllEffectsExceptThoseThatLastThroughDeath();
   }

   public void stopSkillEffects(int skillId) {
      this._effects.stopSkillEffects(skillId);
   }

   public final void stopEffects(EffectType type) {
      this._effects.stopEffects(type);
   }

   public final void stopEffectsOnAction() {
      this._effects.stopEffectsOnAction();
   }

   public final void stopEffectsOnDamage(boolean awake) {
      this._effects.stopEffectsOnDamage(awake);
   }

   public final void stopFakeDeath(boolean removeEffects) {
      if (removeEffects) {
         this.stopEffects(EffectType.FAKE_DEATH);
      }

      if (this.isPlayer()) {
         this.getActingPlayer().setIsFakeDeath(false);
         this.getActingPlayer().setRecentFakeDeath(true);
      }

      this.broadcastPacket(new ChangeWaitType(this, 3));
      this.broadcastPacket(new Revive(this));
      this._isFakeDeath = false;
   }

   public boolean isFakeDeathNow() {
      return this._isFakeDeath;
   }

   public final void stopTransformation(boolean removeEffects) {
      if (removeEffects) {
         this.stopEffects(EffectType.TRANSFORMATION);
      }

      if (this.isPlayer() && this.getActingPlayer().getTransformation() != null) {
         this.getActingPlayer().untransform();
      }

      if (!this.isPlayer()) {
         this.getAI().notifyEvent(CtrlEvent.EVT_THINK);
      }

      this.updateAbnormalEffect();
   }

   public final void startFear() {
      this.abortCast();
      this.getAI().setIntention(CtrlIntention.ACTIVE);
      this.stopMove(null);
      this.updateAbnormalEffect();
   }

   public final void stopFear(boolean removeEffects) {
      if (removeEffects) {
         this.stopEffects(EffectType.FEAR);
      }

      this.updateAbnormalEffect();
   }

   public abstract void updateAbnormalEffect();

   public void updateEffectIcons() {
      this.updateEffectIcons(false);
   }

   public void updateEffectIcons(boolean partyOnly) {
   }

   public final Effect[] getAllEffects() {
      return this._effects.getAllEffects();
   }

   public final Effect getFirstEffect(int skillId) {
      return this._effects.getFirstEffect(skillId);
   }

   public final Effect getFirstEffect(Skill skill) {
      return this._effects.getFirstEffect(skill);
   }

   public final Effect getFirstEffect(EffectType tp) {
      return this._effects.getFirstEffect(tp);
   }

   public final Effect getFirstPassiveEffect(EffectType type) {
      return this._effects.getFirstPassiveEffect(type);
   }

   public final void addStatFunc(Func f) {
      if (f != null) {
         synchronized(this) {
            if (this._calculators == NPC_STD_CALCULATOR) {
               this._calculators = new Calculator[Stats.NUM_STATS];

               for(int i = 0; i < Stats.NUM_STATS; ++i) {
                  if (NPC_STD_CALCULATOR[i] != null) {
                     this._calculators[i] = new Calculator(NPC_STD_CALCULATOR[i]);
                  }
               }
            }

            int stat = f.stat.ordinal();
            if (this._calculators[stat] == null) {
               this._calculators[stat] = new Calculator();
            }

            this._calculators[stat].addFunc(f);
         }
      }
   }

   public final void addStatFuncs(Func[] funcs) {
      if (!this.isPlayer() && World.getInstance().getAroundPlayers(this).isEmpty()) {
         for(Func f : funcs) {
            this.addStatFunc(f);
         }
      } else {
         List<Stats> modifiedStats = new ArrayList<>();

         for(Func f : funcs) {
            modifiedStats.add(f.stat);
            this.addStatFunc(f);
         }

         this.broadcastModifiedStats(modifiedStats);
      }
   }

   public final void removeStatFunc(Func f) {
      if (f != null) {
         int stat = f.stat.ordinal();
         synchronized(this) {
            if (this._calculators[stat] != null) {
               this._calculators[stat].removeFunc(f);
               if (this._calculators[stat].size() == 0) {
                  this._calculators[stat] = null;
               }

               if (this.isNpc()) {
                  int i = 0;

                  while(i < Stats.NUM_STATS && Calculator.equalsCals(this._calculators[i], NPC_STD_CALCULATOR[i])) {
                     ++i;
                  }

                  if (i >= Stats.NUM_STATS) {
                     this._calculators = NPC_STD_CALCULATOR;
                  }
               }
            }
         }
      }
   }

   public final void removeStatFuncs(Func[] funcs) {
      if (!this.isPlayer() && World.getInstance().getAroundPlayers(this).isEmpty()) {
         for(Func f : funcs) {
            this.removeStatFunc(f);
         }
      } else {
         List<Stats> modifiedStats = new ArrayList<>();

         for(Func f : funcs) {
            modifiedStats.add(f.stat);
            this.removeStatFunc(f);
         }

         this.broadcastModifiedStats(modifiedStats);
      }
   }

   public final void removeStatsOwner(Object owner) {
      List<Stats> modifiedStats = null;
      int i = 0;
      synchronized(this._calculators) {
         for(Calculator calc : this._calculators) {
            if (calc != null) {
               if (modifiedStats != null) {
                  modifiedStats.addAll(calc.removeOwner(owner));
               } else {
                  modifiedStats = calc.removeOwner(owner);
               }

               if (calc.size() == 0) {
                  this._calculators[i] = null;
               }
            }

            ++i;
         }

         if (this.isNpc()) {
            i = 0;

            while(i < Stats.NUM_STATS && Calculator.equalsCals(this._calculators[i], NPC_STD_CALCULATOR[i])) {
               ++i;
            }

            if (i >= Stats.NUM_STATS) {
               this._calculators = NPC_STD_CALCULATOR;
            }
         }

         if (owner instanceof Effect) {
            if (!((Effect)owner)._preventExitUpdate) {
               this.broadcastModifiedStats(modifiedStats);
            }
         } else {
            this.broadcastModifiedStats(modifiedStats);
         }
      }
   }

   protected void broadcastModifiedStats(List<Stats> stats) {
      if (stats != null && !stats.isEmpty()) {
         if (this.isSummon()) {
            Summon summon = (Summon)this;
            if (summon.getOwner() != null) {
               summon.updateAndBroadcastStatus(1);
            }
         } else {
            boolean broadcastFull = false;
            StatusUpdate su = new StatusUpdate(this);

            for(Stats stat : stats) {
               if (stat == Stats.POWER_ATTACK_SPEED) {
                  su.addAttribute(18, (int)this.getPAtkSpd());
               } else if (stat == Stats.MAGIC_ATTACK_SPEED) {
                  su.addAttribute(24, this.getMAtkSpd());
               } else if (stat == Stats.MOVE_SPEED) {
                  broadcastFull = true;
               }
            }

            if (this.isPlayer()) {
               if (broadcastFull) {
                  this.getActingPlayer().updateAndBroadcastStatus(2);
               } else {
                  this.getActingPlayer().updateAndBroadcastStatus(1);
                  if (su.hasAttributes()) {
                     this.broadcastPacket(su);
                  }
               }

               if (this.getSummon() != null && this.isAffected(EffectFlag.SERVITOR_SHARE)) {
                  this.getSummon().broadcastStatusUpdate();
               }
            } else if (this.isNpc()) {
               if (broadcastFull) {
                  this.broadcastInfo();
               } else if (su.hasAttributes()) {
                  this.broadcastPacket(su);
               }
            } else if (su.hasAttributes()) {
               this.broadcastPacket(su);
            }
         }
      }
   }

   public final int getXdestination() {
      Creature.MoveData m = this._move;
      return m != null ? m._xDestination : this.getX();
   }

   public final int getYdestination() {
      Creature.MoveData m = this._move;
      return m != null ? m._yDestination : this.getY();
   }

   public final int getZdestination() {
      Creature.MoveData m = this._move;
      return m != null ? m._zDestination : this.getZ();
   }

   public boolean isInCombat() {
      return this.hasAI() && (this.getAI().getAttackTarget() != null || this.getAI().isAutoAttacking());
   }

   public final boolean isMoving() {
      return this._move != null;
   }

   public final boolean isOnGeodataPath() {
      Creature.MoveData m = this._move;
      if (m == null) {
         return false;
      } else if (m.onGeodataPathIndex == -1) {
         return false;
      } else {
         return m.onGeodataPathIndex != m.geoPath.size() - 1;
      }
   }

   public final boolean isCastingNow() {
      return this._isCastingNow;
   }

   public void setIsCastingNow(boolean value) {
      this._isCastingNow = value;
   }

   public final boolean isCastingSimultaneouslyNow() {
      return this._isCastingSimultaneouslyNow;
   }

   public void setIsCastingSimultaneouslyNow(boolean value) {
      this._isCastingSimultaneouslyNow = value;
   }

   public final boolean canAbortCast() {
      return this._castInterruptTime > System.currentTimeMillis();
   }

   public boolean isAttackingNow() {
      return this._attackEndTime > System.nanoTime();
   }

   public final void abortAttack() {
      if (this.isAttackingNow()) {
         this.sendActionFailed();
      }
   }

   public final void abortCast() {
      if (this.isCastingNow() || this.isCastingSimultaneouslyNow()) {
         Future<?> future = this._skillCast;
         if (future != null) {
            future.cancel(true);
            this._skillCast = null;
         }

         future = this._skillCast2;
         if (future != null) {
            future.cancel(true);
            this._skillCast2 = null;
         }

         future = this._skillGeoCheckTask;
         if (future != null) {
            future.cancel(false);
            this._skillGeoCheckTask = null;
         }

         this.finishFly();
         if (this.getFusionSkill() != null) {
            this.getFusionSkill().onCastAbort();
         }

         Effect mog = this.getFirstEffect(EffectType.SIGNET_GROUND);
         if (mog != null) {
            mog.exit();
         }

         if (this._allSkillsDisabled) {
            this.enableAllSkills();
         }

         this.setIsCastingNow(false);
         this.setIsCastingSimultaneouslyNow(false);
         this._animationEndTime = 0L;
         this._castInterruptTime = 0L;
         this._castingSkill = null;
         if (this.isPlayer()) {
            this.getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING);
         }

         this.broadcastPacket(new MagicSkillCanceled(this.getObjectId()));
         this.sendActionFailed();
      }
   }

   public boolean updatePosition() {
      Creature.MoveData m = this._move;
      if (m == null) {
         return true;
      } else if (!this.isVisible()) {
         this._move = null;
         return true;
      } else {
         if (m._moveTimestamp == 0) {
            m._moveTimestamp = m._moveStartTime;
            m._xAccurate = (double)this.getX();
            m._yAccurate = (double)this.getY();
         }

         int gameTicks = GameTimeController.getInstance().getGameTicks();
         if (m._moveTimestamp == gameTicks) {
            return false;
         } else {
            int xPrev = this.getX();
            int yPrev = this.getY();
            int zPrev = this.getZ();
            double dx;
            double dy;
            if (Config.PATHFIND_BOOST == 1) {
               dx = (double)(m._xDestination - xPrev);
               dy = (double)(m._yDestination - yPrev);
            } else {
               dx = (double)m._xDestination - m._xAccurate;
               dy = (double)m._yDestination - m._yAccurate;
            }

            WaterZone waterZone = ZoneManager.getInstance().getZone(this, WaterZone.class);
            boolean isInWater = this.isInWater(this) && waterZone != null;
            boolean isFloating = this.isFlying() || isInWater || this.isVehicle();
            boolean checkZ = !isFloating && !this.isInVehicle() && !this.isInsideZone(ZoneId.NO_GEO);
            boolean syncByGeo = Config.SYNC_BY_GEO || this.isFakePlayer() || this.getFarmSystem() != null && this.getFarmSystem().isAutofarming();
            double dz;
            if (!Config.SYNC_BY_GEO
               && !isFloating
               && !this.isInsideZone(ZoneId.NO_GEO)
               && !m.disregardingGeodata
               && GameTimeController.getInstance().getGameTicks() % 10 == 0
               && GeoEngine.hasGeo(xPrev, yPrev, this.getGeoIndex())) {
               int geoHeight = GeoEngine.getHeight(xPrev, yPrev, zPrev, this.getGeoIndex());
               dz = (double)(m._zDestination - geoHeight);
               if (this.isPlayer() && Math.abs(this.getActingPlayer().getClientZ() - geoHeight) > 100) {
                  dz = (double)(m._zDestination - zPrev);
               } else if (this.isInCombat() && Math.abs(dz) > 100.0 && dx * dx + dy * dy < 40000.0) {
                  dz = (double)(m._zDestination - zPrev);
               } else {
                  zPrev = geoHeight;
               }
            } else {
               dz = (double)(m._zDestination - zPrev);
            }

            double delta = dx * dx + dy * dy;
            if (delta < 10000.0 && dz * dz > 2500.0 && !isFloating) {
               delta = Math.sqrt(delta);
            } else {
               delta = Math.sqrt(delta + dz * dz);
            }

            double distFraction = Double.MAX_VALUE;
            if (delta > 1.0) {
               double distPassed = this.getMoveSpeed() * (double)(gameTicks - m._moveTimestamp) / 10.0;
               distFraction = distPassed / delta;
            }

            if (distFraction > 1.0) {
               if (!syncByGeo) {
                  super.setXYZ(m._xDestination, m._yDestination, m._zDestination);
               } else {
                  int z = checkZ && GeoEngine.hasGeo(m._xDestination, m._yDestination, this.getGeoIndex())
                     ? GeoEngine.getHeight(m._xDestination, m._yDestination, m._zDestination, this.getGeoIndex())
                     : m._zDestination;
                  if (checkZ && Math.abs(m._zDestination - z) > Config.MAX_Z_DIFF) {
                     z = m._zDestination;
                     ++this._wrongCoords;
                  }

                  super.setXYZ(m._xDestination, m._yDestination, z);
               }

               this.revalidateZone(true);
            } else {
               double x = dx * distFraction;
               double y = dy * distFraction;
               m._xAccurate += x;
               m._yAccurate += y;
               int z;
               if (syncByGeo) {
                  z = checkZ
                     ? GeoEngine.getHeight((int)m._xAccurate, (int)m._yAccurate, zPrev + (int)(dz * distFraction + 0.5), this.getGeoIndex())
                     : zPrev + (int)(dz * distFraction + 0.5);
               } else {
                  z = zPrev + (int)(dz * distFraction + 0.5);
               }

               if (isInWater && z > waterZone.getWaterZ()) {
                  z = waterZone.getWaterZ() - 16;
               }

               if (checkZ && syncByGeo && Math.abs(this.getZ() - z) > Config.MAX_Z_DIFF && this._wrongCoords <= Config.MAX_WRONG_ATTEMPTS) {
                  z = this.getZ();
                  ++this._wrongCoords;
               }

               super.setXYZ((int)m._xAccurate, (int)m._yAccurate, z);
               if (this.isPlayer()
                  && DoorParser.getInstance()
                     .checkIfDoorsBetween(
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        (int)(m._xAccurate + x * 2.0),
                        (int)(m._yAccurate + y * 2.0),
                        this.getZ(),
                        this.getReflectionId()
                     )) {
                  this.stopMove(null);
                  return true;
               }

               this.revalidateZone(false);
            }

            m._moveTimestamp = gameTicks;
            if (distFraction > 1.0) {
               ThreadPoolManager.getInstance().execute(() -> this.getAI().notifyEvent(CtrlEvent.EVT_ARRIVED));
               return true;
            } else {
               return false;
            }
         }
      }
   }

   public void revalidateZone(boolean force) {
      if (!this.isTeleporting()) {
         if (!force && this._zoneValidateCounter <= 4) {
            ++this._zoneValidateCounter;
         } else {
            this._zoneValidateCounter = 0;
            List<ZoneType> currentZone = ZoneManager.getInstance().getZones(this.getX(), this.getY(), this.getZ());
            List<ZoneType> newZones = null;
            List<ZoneType> oldZones = null;
            this._zoneLock.lock();

            try {
               if (this._zoneList == null) {
                  newZones = currentZone;
               } else {
                  if (currentZone != null) {
                     for(ZoneType zone : currentZone) {
                        if (!this._zoneList.contains(zone)) {
                           if (newZones == null) {
                              newZones = new ArrayList<>();
                           }

                           newZones.add(zone);
                        }
                     }
                  }

                  if (this._zoneList.size() > 0) {
                     for(ZoneType zone : this._zoneList) {
                        if (currentZone == null || !currentZone.contains(zone)) {
                           if (oldZones == null) {
                              oldZones = new ArrayList<>();
                           }

                           oldZones.add(zone);
                        }
                     }
                  }
               }

               if (currentZone != null && currentZone.size() > 0) {
                  this._zoneList = currentZone;
               } else {
                  this._zoneList = null;
               }
            } finally {
               this._zoneLock.unlock();
            }

            if (oldZones != null) {
               for(ZoneType zone : oldZones) {
                  if (zone != null) {
                     zone.removeCharacter(this);
                  }
               }
            }

            if (newZones != null) {
               for(ZoneType zone : newZones) {
                  if (zone != null) {
                     zone.revalidateInZone(this);
                  }
               }
            }
         }
      }
   }

   public void clearZones() {
      this._zoneLock.lock();

      try {
         if (this._zoneList != null) {
            for(ZoneType zone : this._zoneList) {
               if (zone != null) {
                  zone.removeCharacter(this);
               }
            }
         }

         this._zoneList = null;
      } finally {
         this._zoneLock.unlock();
      }
   }

   public void stopMove(Location loc) {
      this._move = null;
      if (loc != null) {
         this.setXYZ(loc.getX(), loc.getY(), loc.getZ());
         this.setHeading(loc.getHeading());
         this.revalidateZone(true);
      }

      this.broadcastPacket(new StopMove(this));
   }

   public void setTarget(GameObject object) {
      if (object != null && !object.isVisible()) {
         object = null;
      }

      this._target = object;
   }

   public final int getTargetId() {
      GameObject target = this.getTarget();
      return target == null ? -1 : target.getObjectId();
   }

   public final GameObject getTarget() {
      return this._target;
   }

   public void moveToLocation(int x, int y, int z, int offset) {
      if (!Config.CHECK_ATTACK_STATUS_TO_MOVE || !this.isAttackingNow() && !this.isCastingNow()) {
         double speed = this.getMoveSpeed();
         if (!(speed <= 0.0) && !this.isMovementDisabled()) {
            int curX = super.getX();
            int curY = super.getY();
            int curZ = super.getZ();
            double dx = (double)(x - curX);
            double dy = (double)(y - curY);
            double dz = (double)(z - curZ);
            double distance = Math.hypot(dx, dy);
            boolean verticalMovementOnly = this.isFlying() && distance == 0.0 && dz != 0.0;
            if (verticalMovementOnly) {
               distance = Math.abs(dz);
            }

            double cos;
            double sin;
            if (offset <= 0 && !(distance < 1.0)) {
               sin = dy / distance;
               cos = dx / distance;
            } else {
               offset = (int)((double)offset - Math.abs(dz));
               if (offset < 5) {
                  offset = 5;
               }

               if (distance < 1.0 || distance - (double)offset <= 0.0) {
                  this.getAI().notifyEvent(CtrlEvent.EVT_ARRIVED);
                  return;
               }

               sin = dy / distance;
               cos = dx / distance;
               distance -= (double)(offset - 5);
               x = curX + (int)(distance * cos);
               y = curY + (int)(distance * sin);
            }

            Creature.MoveData m = new Creature.MoveData();
            m.onGeodataPathIndex = -1;
            m.disregardingGeodata = false;
            boolean checkPathFind = ZoneManager.getInstance().getZone(this, SiegeZone.class) != null ? true : !this.isInWater(this);
            if (!this.isFlying() && !this.isVehicle() && !this.isInsideZone(ZoneId.NO_GEO) && checkPathFind) {
               if (this.isInVehicle()) {
                  m.disregardingGeodata = true;
               }

               double originalDistance = distance;
               int originalX = x;
               int originalY = y;
               int originalZ = z;
               int gtx = x - -294912 >> 4;
               int gty = y - -262144 >> 4;
               if (Config.PATHFIND_BOOST > 0) {
                  if (this.isOnGeodataPath()) {
                     try {
                        if (gtx == this._move.geoPathGtx && gty == this._move.geoPathGty) {
                           return;
                        }

                        this._move.onGeodataPathIndex = -1;
                     } catch (NullPointerException var33) {
                     }
                  }

                  if (curX < -294912 || curX > 229375 || curY < -262144 || curY > 294911) {
                     if (Config.DEBUG) {
                        _log.warning("Character " + this.getName() + " outside world area, in coordinates x:" + curX + " y:" + curY);
                     }

                     this.getAI().setIntention(CtrlIntention.IDLE);
                     if (this.isPlayer()) {
                        this.getActingPlayer().logout();
                     } else {
                        if (this.isSummon()) {
                           return;
                        }

                        this.onDecay();
                     }

                     return;
                  }

                  Location destiny = GeoEngine.moveCheck(curX, curY, curZ, x, y, this.getGeoIndex());
                  x = destiny.getX();
                  y = destiny.getY();
                  z = destiny.getZ();
                  dx = (double)(x - curX);
                  dy = (double)(y - curY);
                  dz = (double)(z - curZ);
                  distance = verticalMovementOnly ? Math.abs(dz * dz) : Math.sqrt(dx * dx + dy * dy);
               }

               if (Config.PATHFIND_BOOST > 0
                  && originalDistance - distance > 20.0
                  && !this.isInVehicle()
                  && !this.isVehicle()
                  && !this.isInsideZone(ZoneId.NO_GEO)) {
                  m.geoPath = GeoMove.findPath(curX, curY, curZ, originalX, originalY, originalZ, this, true, this.getGeoIndex());
                  if (m.geoPath != null && m.geoPath.size() >= 2) {
                     m.onGeodataPathIndex = 0;
                     m.geoPathGtx = gtx;
                     m.geoPathGty = gty;
                     m.geoPathAccurateTx = originalX;
                     m.geoPathAccurateTy = originalY;
                     x = m.geoPath.get(m.onGeodataPathIndex).getX();
                     y = m.geoPath.get(m.onGeodataPathIndex).getY();
                     z = m.geoPath.get(m.onGeodataPathIndex).getZ();
                     dx = (double)(x - curX);
                     dy = (double)(y - curY);
                     dz = (double)(z - curZ);
                     distance = verticalMovementOnly ? Math.abs(dz * dz) : Math.sqrt(dx * dx + dy * dy);
                     sin = dy / distance;
                     cos = dx / distance;
                  } else {
                     Location dest = this.applyOffset(new Location(originalX, originalY, originalZ), offset);
                     m.geoPath = GeoEngine.MoveList(curX, curY, curZ, dest.getX(), dest.getY(), this.getGeoIndex(), false);
                     if (m.geoPath == null || m.geoPath.isEmpty()) {
                        if (this.isPlayer() || this.isNpc() || this.isSummon()) {
                           if (this.isMonster() || this.isRaid() && Math.abs(z - curZ) > 200) {
                              ((Attackable)this).clearAggroList();
                              this.stopMove(null);
                           }

                           this.getAI().notifyEvent(CtrlEvent.EVT_ARRIVED);
                           return;
                        }

                        m.disregardingGeodata = true;
                     }
                  }
               }

               if (distance < 1.0 && (Config.PATHFIND_BOOST > 0 || this.isPlayable() || this instanceof RiftInvaderInstance || this.isAfraid())) {
                  this.getAI().notifyEvent(CtrlEvent.EVT_ARRIVED);
                  return;
               }
            }

            if ((this.isFlying() || this.isInWater(this)) && !verticalMovementOnly) {
               distance = Math.hypot(distance, dz);
            }

            int ticksToMove = 1 + (int)(10.0 * distance / speed);
            m._xDestination = x;
            m._yDestination = y;
            m._zDestination = z;
            m._heading = 0;
            if (!verticalMovementOnly) {
               this.setHeading(Util.calculateHeadingFrom(cos, sin));
            }

            m._moveStartTime = GameTimeController.getInstance().getGameTicks();
            this._move = m;
            GameTimeController.getInstance().registerMovingObject(this);
            if (ticksToMove * 100 > 3000) {
               ThreadPoolManager.getInstance().schedule(new NotifyAITask(this, CtrlEvent.EVT_ARRIVED_REVALIDATE), 2000L);
            }
         }
      }
   }

   public Location applyOffset(Location point, int offset) {
      if (offset <= 0) {
         return point;
      } else {
         long dx = (long)(point.getX() - this.getX());
         long dy = (long)(point.getY() - this.getY());
         long dz = (long)(point.getZ() - this.getZ());
         double distance = Math.sqrt((double)(dx * dx + dy * dy + dz * dz));
         if (distance <= (double)offset) {
            point.set(this.getX(), this.getY(), this.getZ());
            return point;
         } else {
            if (distance >= 1.0) {
               double cut = (double)offset / distance;
               point.setX(point.getX() - (int)((double)dx * cut + 0.5));
               point.setY(point.getY() - (int)((double)dy * cut + 0.5));
               point.setZ(point.getZ() - (int)((double)dz * cut + 0.5));
               if (!this.isFlying() && !this.isInWater(this) && !this.isInVehicle() && !this.isVehicle()) {
                  point.correctGeoZ(this.getGeoIndex());
               }
            }

            return point;
         }
      }
   }

   public boolean moveToNextRoutePoint() {
      if (!this.isOnGeodataPath()) {
         this._move = null;
         return false;
      } else {
         double speed = this.getMoveSpeed();
         if (!(speed <= 0.0) && !this.isMovementDisabled()) {
            Creature.MoveData md = this._move;
            if (md != null && md.geoPath != null) {
               try {
                  Creature.MoveData m = new Creature.MoveData();
                  m.onGeodataPathIndex = md.onGeodataPathIndex + 1;
                  m.geoPath = md.geoPath;
                  m.geoPathGtx = md.geoPathGtx;
                  m.geoPathGty = md.geoPathGty;
                  m.geoPathAccurateTx = md.geoPathAccurateTx;
                  m.geoPathAccurateTy = md.geoPathAccurateTy;
                  if (md.onGeodataPathIndex == md.geoPath.size() - 2) {
                     m._xDestination = md.geoPathAccurateTx;
                     m._yDestination = md.geoPathAccurateTy;
                     m._zDestination = md.geoPath.get(m.onGeodataPathIndex).getZ();
                  } else {
                     m._xDestination = md.geoPath.get(m.onGeodataPathIndex).getX();
                     m._yDestination = md.geoPath.get(m.onGeodataPathIndex).getY();
                     m._zDestination = md.geoPath.get(m.onGeodataPathIndex).getZ();
                  }

                  double dx = (double)(m._xDestination - super.getX());
                  double dy = (double)(m._yDestination - super.getY());
                  double distance = Math.sqrt(dx * dx + dy * dy);
                  if (distance != 0.0) {
                     this.setHeading(Util.calculateHeadingFrom(this.getX(), this.getY(), m._xDestination, m._yDestination));
                  }

                  int ticksToMove = 1 + (int)(10.0 * distance / speed);
                  m._heading = 0;
                  m._moveStartTime = GameTimeController.getInstance().getGameTicks();
                  this._move = m;
                  GameTimeController.getInstance().registerMovingObject(this);
                  if (ticksToMove * 100 > 3000) {
                     ThreadPoolManager.getInstance().schedule(new NotifyAITask(this, CtrlEvent.EVT_ARRIVED_REVALIDATE), 2000L);
                  }

                  this.broadcastPacket(new MoveToLocation(this));
                  if (this.isNpc()) {
                     this.broadcastPacket(new ValidateLocation(this));
                  }
               } catch (Exception var12) {
                  if (Config.DEBUG) {
                     _log.log(Level.WARNING, this.getClass().getSimpleName() + ": moveToNextRoutePoint() failed.", (Throwable)var12);
                  }
               }

               return true;
            } else {
               return false;
            }
         } else {
            this._move = null;
            return false;
         }
      }
   }

   public boolean validateMovementHeading(int heading) {
      Creature.MoveData m = this._move;
      if (m == null) {
         return true;
      } else {
         boolean result = true;
         if (m._heading != heading) {
            result = m._heading == 0;
            m._heading = heading;
         }

         return result;
      }
   }

   @Override
   public double getDistance(int x, int y) {
      double dx = (double)(x - this.getX());
      double dy = (double)(y - this.getY());
      return Math.sqrt(dx * dx + dy * dy);
   }

   @Override
   public double getDistance(int x, int y, int z) {
      double dx = (double)(x - this.getX());
      double dy = (double)(y - this.getY());
      double dz = (double)(z - this.getZ());
      return Math.sqrt(dx * dx + dy * dy + dz * dz);
   }

   public final double getDistanceSq(GameObject object) {
      return this.getDistanceSq(object.getX(), object.getY(), object.getZ());
   }

   public final double getDistanceSq(int x, int y, int z) {
      double dx = (double)(x - this.getX());
      double dy = (double)(y - this.getY());
      double dz = (double)(z - this.getZ());
      return dx * dx + dy * dy + dz * dz;
   }

   public final double getPlanDistanceSq(GameObject object) {
      return this.getPlanDistanceSq(object.getX(), object.getY());
   }

   public final double getPlanDistanceSq(int x, int y) {
      double dx = (double)(x - this.getX());
      double dy = (double)(y - this.getY());
      return dx * dx + dy * dy;
   }

   public final boolean isInsideRadius(int x, int y, int radius, boolean strictCheck) {
      return this.isInsideRadius(x, y, 0, radius, false, strictCheck);
   }

   public final boolean isInsideRadius(GameObject obj, int radius, boolean checkZ, boolean strictCheck) {
      return this.isInsideRadius(obj.getX(), obj.getY(), obj.getZ(), radius, checkZ, strictCheck);
   }

   public final boolean isInsideRadius(int x, int y, int z, int radius, boolean checkZ, boolean strictCheck) {
      double distance = this.calculateDistance(x, y, z, checkZ, true);
      return strictCheck ? distance < (double)(radius * radius) : distance <= (double)(radius * radius);
   }

   protected boolean checkAndEquipArrows() {
      return true;
   }

   protected boolean checkAndEquipBolts() {
      return true;
   }

   public void addExpAndSp(long addToExp, int addToSp) {
   }

   public abstract ItemInstance getActiveWeaponInstance();

   public abstract Weapon getActiveWeaponItem();

   public abstract ItemInstance getSecondaryWeaponInstance();

   public abstract Item getSecondaryWeaponItem();

   public boolean canFinishAttack() {
      return !this.isStunned() && !this.isSleeping() && !this.isAlikeDead() && !this.isParalyzed() && !this.isPhysicalAttackMuted();
   }

   public void onHitTimer(Creature target, int damage, boolean crit, boolean miss, boolean soulshot, byte shld) {
      if (!this.isCastingNow() && this.canFinishAttack()) {
         if (target != null
            && !this.isAlikeDead()
            && (!this.isNpc() || !((Npc)this).isEventMob())
            && (target == null || !this.isPlayer() || target.isVisibleFor(this.getActingPlayer()))) {
            if ((!this.isNpc() || !target.isAlikeDead())
               && !target.isDead()
               && (World.getInstance().getAroundCharacters(this).contains(target) || this.isDoor())) {
               if (miss) {
                  if (target.hasAI()) {
                     target.getAI().notifyEvent(CtrlEvent.EVT_EVADED, this);
                  }

                  if (target.getChanceSkills() != null) {
                     target.getChanceSkills().onEvadedHit(this);
                  }
               }

               if (!miss) {
                  Formulas.calcStunBreak(target, crit);
               }

               this.sendDamageMessage(target, damage, null, false, crit, miss);
               if (target.isRaid()
                  && target.giveRaidCurse()
                  && !Config.RAID_DISABLE_CURSE
                  && this.getLevel() > target.getLevel() + 8
                  && target.getId() != 29054) {
                  Skill skill = SkillsParser.FrequentSkill.RAID_CURSE2.getSkill();
                  if (skill != null) {
                     this.abortAttack();
                     this.abortCast();
                     this.getAI().setIntention(CtrlIntention.IDLE);
                     skill.getEffects(target, this, false);
                  }

                  damage = 0;
               }

               if (target.isPlayer() && !miss && !target.isInvul()) {
                  target.getActingPlayer().getAI().clientStartAutoAttack();
               }

               if (!miss && damage > 0) {
                  Weapon weapon = this.getActiveWeaponItem();
                  boolean isBow = weapon != null && (weapon.getItemType() == WeaponType.BOW || weapon.getItemType() == WeaponType.CROSSBOW);
                  double reflectedDamage = 0.0;
                  if (!isBow
                     && !target.isInvul()
                     && (!target.isRaid() || this.getActingPlayer() == null || this.getActingPlayer().getLevel() <= target.getLevel() + 8)) {
                     double reflectPercent = target.getStat().calcStat(Stats.REFLECT_DAMAGE_PERCENT, 0.0, null, null);
                     if (reflectPercent > 0.0) {
                        reflectedDamage = (double)((int)(reflectPercent / 100.0 * (double)damage));
                        if (reflectedDamage > target.getMaxHp()) {
                           reflectedDamage = target.getMaxHp();
                        }
                     }
                  }

                  if (target.hasAI()) {
                     target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this, Integer.valueOf(damage));
                  }

                  target.reduceCurrentHp((double)damage, this, null);
                  target.notifyDamageReceived((double)damage, this, null, crit, false);
                  if (reflectedDamage > 0.0 && !this.isInvul() && !target.isDead()) {
                     this.reduceCurrentHp(reflectedDamage, target, true, false, null);
                     this.notifyDamageReceived(reflectedDamage, target, null, crit, false);
                  }

                  if (!isBow && !this.isHealBlocked()) {
                     double absorbPercent = this.getStat().calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0.0, null, null);
                     if (absorbPercent > 0.0) {
                        int maxCanAbsorb = (int)((double)this.getMaxRecoverableHp() - this.getCurrentHp());
                        int absorbDamage = (int)(absorbPercent / 100.0 * (double)damage);
                        if (absorbDamage > maxCanAbsorb) {
                           absorbDamage = maxCanAbsorb;
                        }

                        if (absorbDamage > 0) {
                           this.setCurrentHp(this.getCurrentHp() + (double)absorbDamage);
                        }
                     }

                     absorbPercent = this.getStat().calcStat(Stats.ABSORB_MANA_DAMAGE_PERCENT, 0.0, null, null);
                     if (absorbPercent > 0.0) {
                        int maxCanAbsorb = (int)((double)this.getMaxRecoverableMp() - this.getCurrentMp());
                        int absorbDamage = (int)(absorbPercent / 100.0 * (double)damage);
                        if (absorbDamage > maxCanAbsorb) {
                           absorbDamage = maxCanAbsorb;
                        }

                        if (absorbDamage > 0) {
                           this.setCurrentMp(this.getCurrentMp() + (double)absorbDamage);
                        }
                     }
                  }

                  this.getAI().clientStartAutoAttack();
                  if (this.isSummon()) {
                     Player owner = ((Summon)this).getOwner();
                     if (owner != null) {
                        owner.getAI().clientStartAutoAttack();
                     }
                  }

                  if (target.isPlayer() && target.getActingPlayer().isFakeDeathNow()) {
                     target.stopFakeDeath(true);
                  }

                  if (!target.isRaid() && Formulas.calcAtkBreak(target, crit)) {
                     target.breakAttack();
                     target.breakCast();
                  }

                  if (this._chanceSkills != null) {
                     this._chanceSkills.onHit(target, damage, false, crit);
                     if (reflectedDamage > 0.0) {
                        this._chanceSkills.onHit(target, (int)reflectedDamage, true, false);
                     }
                  }

                  if (this._triggerSkills != null) {
                     for(OptionsSkillHolder holder : this._triggerSkills.values()) {
                        if ((!crit && holder.getSkillType() == OptionsSkillType.ATTACK || holder.getSkillType() == OptionsSkillType.CRITICAL && crit)
                           && (double)Rnd.get(100) < holder.getChance()) {
                           this.makeTriggerCast(holder.getSkill(), target);
                        }
                     }
                  }

                  if (target.getChanceSkills() != null) {
                     target.getChanceSkills().onHit(this, damage, true, crit);
                  }
               }

               Weapon activeWeapon = this.getActiveWeaponItem();
               if (activeWeapon != null) {
                  activeWeapon.getSkillEffects(this, target, crit);
               }

               if (this instanceof EventMapGuardInstance && target instanceof Player) {
                  target.doDie(this);
               }

               this.rechargeShots(true, false);
            } else {
               this.rechargeShots(true, false);
               this.getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
               this.sendActionFailed();
            }
         } else {
            this.getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
         }
      }
   }

   public void breakAttack() {
      if (this.isAttackingNow()) {
         this.abortAttack();
         if (this.isPlayer()) {
            this.sendPacket(SystemMessageId.ATTACK_FAILED);
         }
      }
   }

   public void breakCast() {
      if (this.isCastingNow() && this.canAbortCast() && this._castingSkill != null && (this._castingSkill.isMagic() || this._castingSkill.isStatic())) {
         this.abortCast();
         if (this.isPlayer()) {
            this.sendPacket(SystemMessageId.CASTING_INTERRUPTED);
         }
      }
   }

   protected void reduceArrowCount(boolean bolts) {
   }

   @Override
   public void onForcedAttack(Player player) {
      for(AbstractFightEvent e : player.getFightEvents()) {
         if (e != null && !e.canAttack(this, player)) {
            player.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
            player.sendActionFailed();
            return;
         }
      }

      if (this.isInsidePeaceZone(player) && !player.isInFightEvent()) {
         player.sendPacket(SystemMessageId.TARGET_IN_PEACEZONE);
         player.sendActionFailed();
      } else {
         if (player.isInOlympiadMode() && player.getTarget() != null) {
            if (!player.getTarget().isPlayable()) {
               player.sendActionFailed();
               return;
            }

            if (!Config.ALLOW_OLY_HIT_SUMMON
               || Config.ALLOW_OLY_HIT_SUMMON
                  && (!player.hasSummon() || player.hasSummon() && player.getTarget() != null && player.getTarget() != player.getSummon())) {
               Player target = null;
               GameObject object = player.getTarget();
               if (object != null && object.isPlayable()) {
                  target = object.getActingPlayer();
               }

               if (target == null || target.isInOlympiadMode() && (!player.isOlympiadStart() || player.getOlympiadGameId() != target.getOlympiadGameId())) {
                  player.sendActionFailed();
                  return;
               }
            }
         }

         if (player.getTarget() != null && !player.getTarget().canBeAttacked() && !player.getAccessLevel().allowPeaceAttack()) {
            player.sendActionFailed();
         } else if (player.isConfused()) {
            player.sendActionFailed();
         } else if (player.isBlocked()) {
            player.sendActionFailed();
         } else if (!GeoEngine.canSeeTarget(player, this, false)) {
            player.sendPacket(SystemMessageId.CANT_SEE_TARGET);
            player.sendActionFailed();
         } else if (player.getBlockCheckerArena() != -1) {
            player.sendActionFailed();
         } else {
            player.getAI().setIntention(CtrlIntention.ATTACK, this);
         }
      }
   }

   public boolean isInsidePeaceZone(Player attacker) {
      return this.isInFightEvent() && attacker.isInFightEvent() ? false : this.isInsidePeaceZone(attacker, this);
   }

   public boolean isInsidePeaceZone(Player attacker, GameObject target) {
      if (target.isPlayer() && target.getActingPlayer().isInFightEvent() && attacker.isInFightEvent()) {
         return false;
      } else {
         return !attacker.getAccessLevel().allowPeaceAttack() && this.isInsidePeaceZone((GameObject)attacker, target);
      }
   }

   public boolean isInsidePeaceZone(GameObject attacker, GameObject target) {
      if (target == null) {
         return false;
      } else if (!target.isPlayable() || !attacker.isPlayable()) {
         return false;
      } else if (attacker.isPlayer() && attacker.getActingPlayer().isInFightEvent() && target.isPlayer() && target.getActingPlayer().isInFightEvent()) {
         return false;
      } else if (ReflectionManager.getInstance().getReflection(this.getReflectionId()).isPvPInstance()) {
         return false;
      } else if (TerritoryWarManager.PLAYER_WITH_WARD_CAN_BE_KILLED_IN_PEACEZONE
         && TerritoryWarManager.getInstance().isTWInProgress()
         && target.isPlayer()
         && target.getActingPlayer().isCombatFlagEquipped()) {
         return false;
      } else {
         if (Config.ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE) {
            if (target.getActingPlayer() != null && target.getActingPlayer().getKarma() > 0) {
               return false;
            }

            if (attacker.getActingPlayer() != null
               && attacker.getActingPlayer().getKarma() > 0
               && target.getActingPlayer() != null
               && target.getActingPlayer().getPvpFlag() > 0) {
               return false;
            }

            if (attacker instanceof Creature && target instanceof Creature) {
               return target.isInsideZone(ZoneId.PEACE) || attacker.isInsideZone(ZoneId.PEACE);
            }

            if (attacker instanceof Creature) {
               return TownManager.getTown(target.getX(), target.getY(), target.getZ()) != null || attacker.isInsideZone(ZoneId.PEACE);
            }
         }

         if (attacker instanceof Creature && target instanceof Creature) {
            return target.isInsideZone(ZoneId.PEACE) || attacker.isInsideZone(ZoneId.PEACE);
         } else if (attacker instanceof Creature) {
            return TownManager.getTown(target.getX(), target.getY(), target.getZ()) != null || attacker.isInsideZone(ZoneId.PEACE);
         } else {
            return TownManager.getTown(target.getX(), target.getY(), target.getZ()) != null
               || TownManager.getTown(attacker.getX(), attacker.getY(), attacker.getZ()) != null;
         }
      }
   }

   public boolean isInActiveRegion() {
      WorldRegion region = this.getWorldRegion();
      return region != null && region.isActive();
   }

   public boolean isInParty() {
      return false;
   }

   public Party getParty() {
      return null;
   }

   public final WeaponType getAttackType() {
      if (this.isTransformed()) {
         TransformTemplate template = this.getTransformation().getTemplate(this.getActingPlayer());
         if (template != null) {
            return template.getBaseAttackType();
         }
      }

      Weapon weapon = this.getActiveWeaponItem();
      return weapon != null ? weapon.getItemType() : this.getTemplate().getBaseAttackType();
   }

   public int calculateTimeBetweenAttacks() {
      return (int)(500000.0 / this.getPAtkSpd());
   }

   public int calculateReuseTime(Weapon weapon) {
      if (this.isTransformed()) {
         switch(this.getAttackType()) {
            case BOW:
               return (int)(499500.0 * this.getStat().getWeaponReuseModifier(null) / this.getStat().getPAtkSpd());
            case CROSSBOW:
               return (int)(399600.0 * this.getStat().getWeaponReuseModifier(null) / this.getStat().getPAtkSpd());
         }
      }

      return weapon != null && weapon.getReuseDelay() != 0 ? (int)((double)(weapon.getReuseDelay() * 333) / this.getPAtkSpd()) : 0;
   }

   public boolean isUsingDualWeapon() {
      return false;
   }

   @Override
   public Skill addSkill(Skill newSkill) {
      Skill oldSkill = null;
      if (newSkill != null) {
         oldSkill = this._skills.put(newSkill.getId(), newSkill);
         if (oldSkill != null) {
            TimeStamp sts = this._skillReuses.get(oldSkill.hashCode());
            if (sts != null && sts.hasNotPassed()) {
               this._skillReuses.put(newSkill.hashCode(), sts);
               this.addTimeStamp(newSkill, sts.getReuse(), sts.getStamp());
            }
         }

         if (oldSkill != null) {
            if (oldSkill.triggerAnotherSkill()) {
               this.removeSkill(oldSkill.getTriggeredId(), true);
            }

            this.removeStatsOwner(oldSkill);
         }

         this.addStatFuncs(newSkill.getStatFuncs(null, this));
         if (oldSkill != null && this._chanceSkills != null) {
            this.removeChanceSkill(oldSkill.getId());
         }

         if (newSkill.isChance()) {
            this.addChanceTrigger(newSkill);
         }

         newSkill.getEffectsPassive(this);
      }

      return oldSkill;
   }

   public Skill removeSkill(Skill skill, boolean cancelEffect) {
      return skill != null ? this.removeSkill(skill.getId(), cancelEffect) : null;
   }

   public Skill removeSkill(int skillId) {
      return this.removeSkill(skillId, true);
   }

   public Skill removeSkill(int skillId, boolean cancelEffect) {
      Skill oldSkill = this._skills.remove(skillId);
      if (oldSkill != null) {
         if (oldSkill.triggerAnotherSkill() && oldSkill.getTriggeredId() > 0) {
            this.removeSkill(oldSkill.getTriggeredId(), true);
         }

         if (this._castingSkill != null && (this.isCastingNow() || this.isCastingSimultaneouslyNow()) && oldSkill.getId() == this._castingSkill.getId()) {
            this.abortCast();
         }

         this._effects.removePassiveEffects(skillId);
         if (cancelEffect || oldSkill.isToggle()) {
            Effect e = this.getFirstEffect(oldSkill);
            if (e == null || e.getEffectType() != EffectType.TRANSFORMATION) {
               this.removeStatsOwner(oldSkill);
               this.stopSkillEffects(oldSkill.getId());
            }
         }

         if (this.isPlayer() && oldSkill instanceof SkillSummon && oldSkill.getId() == 710 && this.hasSummon() && this.getSummon().getId() == 14870) {
            this.getActingPlayer().getSummon().unSummon(this.getActingPlayer());
         }

         if (oldSkill.isChance() && this._chanceSkills != null) {
            this.removeChanceSkill(oldSkill.getId());
         }
      }

      return oldSkill;
   }

   public void removeChanceSkill(int id) {
      if (this._chanceSkills != null) {
         synchronized(this._chanceSkills) {
            for(IChanceSkillTrigger trigger : this._chanceSkills.keySet()) {
               if (trigger instanceof Skill && ((Skill)trigger).getId() == id) {
                  this._chanceSkills.remove(trigger);
               }
            }
         }
      }
   }

   public void addChanceTrigger(IChanceSkillTrigger trigger) {
      if (this._chanceSkills == null) {
         synchronized(this) {
            if (this._chanceSkills == null) {
               this._chanceSkills = new ChanceSkillList(this);
            }
         }
      }

      this._chanceSkills.put(trigger, trigger.getTriggeredChanceCondition());
   }

   public void removeChanceEffect(IChanceSkillTrigger effect) {
      if (this._chanceSkills != null) {
         this._chanceSkills.remove(effect);
      }
   }

   public void onStartChanceEffect(byte element) {
      if (this._chanceSkills != null) {
         this._chanceSkills.onStart(element);
      }
   }

   public void onActionTimeChanceEffect(byte element) {
      if (this._chanceSkills != null) {
         this._chanceSkills.onActionTime(element);
      }
   }

   public void onExitChanceEffect(byte element) {
      if (this._chanceSkills != null) {
         this._chanceSkills.onExit(element);
      }
   }

   public final Collection<Skill> getAllSkills() {
      return this._skills.values();
   }

   @Override
   public Map<Integer, Skill> getSkills() {
      return this._skills;
   }

   public ChanceSkillList getChanceSkills() {
      return this._chanceSkills;
   }

   @Override
   public int getSkillLevel(int skillId) {
      Skill skill = this.getKnownSkill(skillId);
      return skill == null ? -1 : skill.getLevel();
   }

   @Override
   public final Skill getKnownSkill(int skillId) {
      return this._skills.get(skillId);
   }

   public int getBuffCount() {
      return this._effects.getBuffCount();
   }

   public int getDanceCount() {
      return this._effects.getDanceCount();
   }

   public void onMagicLaunchedTimer(MagicUseTask mut) {
      Skill skill = mut.getSkill();
      if (skill != null && mut.getTargets() != null) {
         GameObject[] targets = this.isPlayer()
            ? (
               skill.isAura()
                  ? skill.getTargetList(this)
                  : (skill.isArea() ? skill.getTargetList(this, false, this.getAI().getCastTarget()) : mut.getTargets())
            )
            : mut.getTargets();
         mut.setTargets(targets);
         if (targets.length == 0 && !skill.isAura()) {
            this.abortCast();
         } else {
            int escapeRange = 0;
            if (skill.getEffectRange() > escapeRange) {
               escapeRange = skill.getEffectRange();
            } else if (skill.getCastRange() < 0 && skill.getAffectRange() > 80) {
               escapeRange = skill.getAffectRange();
            }

            if (targets.length > 0 && escapeRange > 0) {
               int _skiprange = 0;
               int skipLOS = 0;
               int _skippeace = 0;
               List<Creature> targetList = new ArrayList<>();

               for(GameObject target : targets) {
                  if (target instanceof Creature) {
                     if (!this.isInsideRadius(target.getX(), target.getY(), target.getZ(), (int)((double)escapeRange + this.getColRadius()), true, false)) {
                        ++_skiprange;
                     } else if (!Config.ALLOW_SKILL_END_CAST
                        && !skill.isDisableGeoCheck()
                        && skill.getTargetType() != TargetType.PARTY
                        && !skill.hasEffectType(EffectType.HEAL)
                        && mut.getHitTime() > 550
                        && !GeoEngine.canSeeTarget(this, target, false)) {
                        ++skipLOS;
                     } else {
                        if (skill.isOffensive() && !skill.isNeutral()) {
                           if (this.isPlayer()) {
                              if (((Creature)target).isInsidePeaceZone(this.getActingPlayer())) {
                                 ++_skippeace;
                                 continue;
                              }
                           } else if (((Creature)target).isInsidePeaceZone(this, target)) {
                              ++_skippeace;
                              continue;
                           }
                        }

                        targetList.add((Creature)target);
                     }
                  }
               }

               if (targetList.isEmpty() && !skill.isAura()) {
                  if (this.isPlayer()) {
                     if (_skiprange > 0) {
                        this.sendPacket(SystemMessageId.DIST_TOO_FAR_CASTING_STOPPED);
                     } else if (skipLOS > 0) {
                        this.sendPacket(SystemMessageId.CANT_SEE_TARGET);
                     } else if (_skippeace > 0) {
                        this.sendPacket(SystemMessageId.A_MALICIOUS_SKILL_CANNOT_BE_USED_IN_PEACE_ZONE);
                     }
                  }

                  this.abortCast();
                  return;
               }

               mut.setTargets(targetList.toArray(new Creature[targetList.size()]));
               targets = mut.getTargets();
            }

            if ((!mut.isSimultaneous() || this.isCastingSimultaneouslyNow())
               && (mut.isSimultaneous() || this.isCastingNow())
               && (!this.isAlikeDead() || skill.isStatic())) {
               if (!skill.isStatic()) {
                  this.broadcastPacket(new MagicSkillLaunched(this, skill.getDisplayId(), skill.getDisplayLevel(), targets));
               }

               mut.setPhase(2);
               if (mut.getHitTime() == 0) {
                  this.onMagicHitTimer(mut);
               } else {
                  this._skillCast = ThreadPoolManager.getInstance().schedule(mut, 400L);
               }
            } else {
               this.getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
            }
         }
      } else {
         this.abortCast();
      }
   }

   public void onMagicHitTimer(MagicUseTask mut) {
      try {
         Skill skill = mut.getSkill();
         GameObject[] targets = mut.getTargets();
         if (skill == null || targets == null) {
            this.abortCast();
            return;
         }

         if (this.getFusionSkill() != null) {
            if (mut.isSimultaneous()) {
               this._skillCast2 = null;
               this.setIsCastingSimultaneouslyNow(false);
            } else {
               this._skillCast = null;
               this.setIsCastingNow(false);
            }

            this._castInterruptTime = 0L;
            this._castingSkill = null;
            this.getFusionSkill().onCastAbort();
            this.notifyQuestEventSkillFinished(skill, targets[0]);
            return;
         }

         Effect mog = this.getFirstEffect(EffectType.SIGNET_GROUND);
         if (mog != null) {
            if (mut.isSimultaneous()) {
               this._skillCast2 = null;
               this.setIsCastingSimultaneouslyNow(false);
            } else {
               this._skillCast = null;
               this.setIsCastingNow(false);
            }

            this._castInterruptTime = 0L;
            this._castingSkill = null;
            mog.exit();
            this.notifyQuestEventSkillFinished(skill, targets[0]);
            return;
         }

         for(GameObject tgt : targets) {
            if (tgt.isPlayable()) {
               Creature target = (Creature)tgt;
               if (skill.getSkillType() == SkillType.BUFF) {
                  SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
                  smsg.addSkillName(skill);
                  target.sendPacket(smsg);
               }

               if (this.isPlayer() && target.isSummon()) {
                  ((Summon)target).updateAndBroadcastStatus(1);
               }
            }
         }

         this.rechargeShots(skill.useSoulShot(), skill.useSpiritShot());
         StatusUpdate su = new StatusUpdate(this);
         boolean isSendStatus = false;
         double mpConsume = (double)this.getStat().getMpConsume(skill);
         if (mpConsume > 0.0) {
            if (mpConsume > this.getCurrentMp()) {
               this.sendPacket(SystemMessageId.NOT_ENOUGH_MP);
               this.abortCast();
               return;
            }

            this.getStatus().reduceMp(mpConsume);
            su.addAttribute(11, (int)this.getCurrentMp());
            isSendStatus = true;
         }

         if (skill.getHpConsume() > 0) {
            double consumeHp = (double)skill.getHpConsume();
            if (consumeHp >= this.getCurrentHp()) {
               this.sendPacket(SystemMessageId.NOT_ENOUGH_HP);
               this.abortCast();
               return;
            }

            this.getStatus().reduceHp(consumeHp, this, true);
            su.addAttribute(9, (int)this.getCurrentHp());
            isSendStatus = true;
         }

         if (isSendStatus) {
            this.sendPacket(su);
         }

         switch(skill.getFlyType()) {
            case THROW_UP:
            case THROW_HORIZONTAL:
               for(GameObject target : targets) {
                  Location flyLoc = this.getFlyLocation(null, skill);
                  target.setXYZ(flyLoc.getX(), flyLoc.getY(), flyLoc.getZ());
                  this.broadcastPacket(new FlyToLocation((Creature)target, flyLoc, skill.getFlyType()));
               }
         }

         this.callSkill(mut.getSkill(), mut.getTargets());
         if (mut.getHitTime() > 0) {
            mut.setCount(mut.getCount() + 1);
            if (mut.getCount() < skill.getHitCounts()) {
               int skillTime = mut.getHitTime() * skill.getHitTimings()[mut.getCount()] / 100;
               if (mut.isSimultaneous()) {
                  this._skillCast2 = ThreadPoolManager.getInstance().schedule(mut, (long)skillTime);
               } else {
                  this._skillCast = ThreadPoolManager.getInstance().schedule(mut, (long)skillTime);
               }

               return;
            }
         }

         mut.setPhase(3);
         if (mut.getHitTime() == 0 || mut.getCoolTime() == 0) {
            this.onMagicFinalizer(mut);
         } else if (mut.isSimultaneous()) {
            this._skillCast2 = ThreadPoolManager.getInstance().schedule(mut, (long)mut.getCoolTime());
         } else {
            this._skillCast = ThreadPoolManager.getInstance().schedule(mut, (long)mut.getCoolTime());
         }
      } catch (Exception var14) {
         if (Config.DEBUG) {
            _log.log(Level.WARNING, this.getClass().getSimpleName() + ": onMagicHitTimer() failed.", (Throwable)var14);
         }
      }
   }

   public void onMagicFinalizer(MagicUseTask mut) {
      if (mut.isSimultaneous()) {
         this._skillCast2 = null;
         this._castInterruptTime = 0L;
         this._castingSkill = null;
         this.setIsCastingSimultaneouslyNow(false);
      } else {
         this.finishFly();
         this._animationEndTime = 0L;
         this._skillCast = null;
         this._skillGeoCheckTask = null;
         this._castInterruptTime = 0L;
         this._castingSkill = null;
         this.setIsCastingNow(false);
         this.setIsCastingSimultaneouslyNow(false);
         Skill skill = mut.getSkill();
         GameObject target = mut.getTargets().length > 0 ? mut.getTargets()[0] : null;
         if (this.isPlayer() && skill.getMaxSoulConsumeCount() > 0 && !this.getActingPlayer().decreaseSouls(skill.getMaxSoulConsumeCount(), skill)) {
            this.abortCast();
         } else {
            if (mut.getCount() > 0) {
               this.rechargeShots(mut.getSkill().useSoulShot(), mut.getSkill().useSpiritShot());
            }

            boolean isCtrlPressed = this.isPlayer()
               && this.getActingPlayer().getCurrentSkill() != null
               && this.getActingPlayer().getCurrentSkill().isCtrlPressed();
            boolean hasQueuedSkill = this.isPlayer() && this.getActingPlayer().getQueuedSkill() != null;
            if (!isCtrlPressed
               && !hasQueuedSkill
               && skill.nextActionIsAttack()
               && this.getTarget() instanceof Creature
               && this.getTarget() != this
               && target != null
               && this.getTarget() == target
               && target.canBeAttacked()
               && (
                  this.getAI().getNextIntention() == null
                     || this.getAI().getNextIntention() != null && this.getAI().getNextIntention().getCtrlIntention() != CtrlIntention.MOVING
               )) {
               this.getAI().setIntention(CtrlIntention.ATTACK, target);
            }

            if (skill.isOffensive() && !skill.isNeutral()) {
               switch(skill.getSkillType()) {
                  case UNLOCK:
                  case UNLOCK_SPECIAL:
                  case DELUXE_KEY_UNLOCK:
                     break;
                  default:
                     this.getAI().clientStartAutoAttack();
               }
            }

            this.getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING);
            this.notifyQuestEventSkillFinished(skill, target);
            if (this.isPlayer()) {
               Player currPlayer = this.getActingPlayer();
               SkillUseHolder queuedSkill = currPlayer.getQueuedSkill();
               currPlayer.setCurrentSkill(null, false, false);
               if (queuedSkill != null) {
                  currPlayer.setQueuedSkill(null, false, false);
                  ThreadPoolManager.getInstance()
                     .execute(new QueuedMagicUseTask(currPlayer, queuedSkill.getSkill(), queuedSkill.isCtrlPressed(), queuedSkill.isShiftPressed()));
               }
            }
         }
      }
   }

   protected void notifyQuestEventSkillFinished(Skill skill, GameObject target) {
   }

   public Collection<TimeStamp> getSkillReuses() {
      return this._skillReuses.valueCollection();
   }

   public TimeStamp getSkillReuse(Skill skill) {
      return this._skillReuses.get(skill.hashCode());
   }

   public void enableSkill(Skill skill) {
      if (skill != null) {
         this._skillReuses.remove(skill.hashCode());
      }
   }

   public void disableSkill(Skill skill, long delay) {
      if (skill != null) {
         this._skillReuses.put(skill.hashCode(), new TimeStamp(skill, delay));
      }
   }

   public final void resetDisabledSkills() {
      this._skillReuses.clear();
   }

   public void addBlockSkill(Skill sk) {
      if (sk != null) {
         this._blockSkills.add(sk);
      }
   }

   public void removeBlockSkill(Skill sk) {
      if (sk != null) {
         if (this.isSkillBlocked(sk)) {
            this._blockSkills.remove(sk);
         }
      }
   }

   public void cleanBlockSkills() {
      this._blockSkills.clear();
   }

   public List<Skill> getBlockSkills() {
      return this._blockSkills;
   }

   public boolean isSkillBlocked(Skill sk) {
      return sk == null ? true : this._blockSkills.contains(sk);
   }

   public boolean isSkillDisabled(Skill skill) {
      TimeStamp sts = this._skillReuses.get(skill.hashCode());
      if (sts == null) {
         return false;
      } else if (sts.hasNotPassed()) {
         return true;
      } else {
         this._skillReuses.remove(skill.hashCode());
         return false;
      }
   }

   public void disableAllSkills() {
      this._allSkillsDisabled = true;
   }

   public void enableAllSkills() {
      this._allSkillsDisabled = false;
   }

   public void callSkill(Skill skill, GameObject[] targets) {
      try {
         Weapon activeWeapon = this.getActiveWeaponItem();
         if (skill.isToggle() && this.getFirstEffect(skill.getId()) != null) {
            return;
         }

         for(GameObject trg : targets) {
            if (trg instanceof Creature) {
               Creature target = (Creature)trg;
               Creature targetsAttackTarget = null;
               Creature targetsCastTarget = null;
               if (target.hasAI()) {
                  targetsAttackTarget = target.getAI().getAttackTarget();
                  targetsCastTarget = target.getAI().getCastTarget();
               }

               if (!Config.RAID_DISABLE_CURSE
                  && (
                     target.isRaid() && target.giveRaidCurse() && this.getLevel() > target.getLevel() + 8
                        || !skill.isOffensive()
                           && targetsAttackTarget != null
                           && targetsAttackTarget.isRaid()
                           && targetsAttackTarget.giveRaidCurse()
                           && targetsAttackTarget.getAttackByList().contains(target)
                           && this.getLevel() > targetsAttackTarget.getLevel() + 8
                        || !skill.isOffensive()
                           && targetsCastTarget != null
                           && targetsCastTarget.isRaid()
                           && targetsCastTarget.giveRaidCurse()
                           && targetsCastTarget.getAttackByList().contains(target)
                           && this.getLevel() > targetsCastTarget.getLevel() + 8
                  )) {
                  if (skill.isMagic()) {
                     Skill tempSkill = SkillsParser.FrequentSkill.RAID_CURSE.getSkill();
                     if (tempSkill != null) {
                        this.abortAttack();
                        this.abortCast();
                        this.getAI().setIntention(CtrlIntention.IDLE);
                        tempSkill.getEffects(target, this, false);
                     } else if (_log.isLoggable(Level.WARNING)) {
                        _log.log(Level.WARNING, "Skill 4215 at level 1 is missing in DP.");
                     }
                  } else if (target.getId() != 29054) {
                     Skill tempSkill = SkillsParser.FrequentSkill.RAID_CURSE2.getSkill();
                     if (tempSkill != null) {
                        this.abortAttack();
                        this.abortCast();
                        this.getAI().setIntention(CtrlIntention.IDLE);
                        tempSkill.getEffects(target, this, false);
                     } else if (_log.isLoggable(Level.WARNING)) {
                        _log.log(Level.WARNING, "Skill 4515 at level 1 is missing in DP.");
                     }
                  }

                  return;
               }

               if (skill.isOverhit() && target.isAttackable()) {
                  ((Attackable)target).overhitEnabled(true);
               }

               if (!skill.isStatic()) {
                  if (activeWeapon != null && !target.isDead() && activeWeapon.getSkillEffects(this, target, skill).length > 0 && this.isPlayer()) {
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_ACTIVATED);
                     sm.addSkillName(skill);
                     this.sendPacket(sm);
                  }

                  if (this._chanceSkills != null) {
                     this._chanceSkills.onSkillHit(target, 0.0, skill, false);
                  }

                  if (target.getChanceSkills() != null) {
                     target.getChanceSkills().onSkillHit(this, 0.0, skill, true);
                  }

                  if (this._triggerSkills != null) {
                     for(OptionsSkillHolder holder : this._triggerSkills.values()) {
                        if ((
                              skill.isMagic() && holder.getSkillType() == OptionsSkillType.MAGIC
                                 || skill.isPhysical() && holder.getSkillType() == OptionsSkillType.ATTACK
                           )
                           && (double)Rnd.get(100) < holder.getChance()) {
                           this.makeTriggerCast(holder.getSkill(), target);
                        }
                     }
                  }
               }
            }
         }

         ISkillHandler handler = SkillHandler.getInstance().getHandler(skill.getSkillType());
         if (handler != null) {
            handler.useSkill(this, skill, targets);
         } else {
            skill.useSkill(this, targets);
         }

         Player player = this.getActingPlayer();
         if (player != null) {
            for(GameObject target : targets) {
               if (target instanceof Creature && !skill.isNeutral()) {
                  if (skill.isOffensive()) {
                     if (target.isPlayer() || target.isSummon() || target.isTrap()) {
                        if (skill.getSkillType() != SkillType.SIGNET && skill.getSkillType() != SkillType.SIGNET_CASTTIME) {
                           if (target.isPlayer()) {
                              if (!((Creature)target).isInvul() || ((Creature)target).isInvul() && !Config.ATTACK_STANCE_MAGIC) {
                                 target.getActingPlayer().getAI().clientStartAutoAttack();
                              }
                           } else if (target.isSummon() && ((Creature)target).hasAI()) {
                              Player owner = ((Summon)target).getOwner();
                              if (owner != null && (!owner.isInvul() || owner.isInvul() && !Config.ATTACK_STANCE_MAGIC)) {
                                 owner.getAI().clientStartAutoAttack();
                              }
                           }

                           if (player.getSummon() != target && !this.isTrap()) {
                              player.updatePvPStatus((Creature)target);
                           }
                        }
                     } else if (target.isAttackable()) {
                        switch(skill.getId()) {
                           case 51:
                           case 511:
                              break;
                           default:
                              ((Creature)target).addAttackerToAttackByList(this);
                        }
                     }

                     if (((Creature)target).hasAI()) {
                        switch(skill.getSkillType()) {
                           case AGGREDUCE:
                           case AGGREDUCE_CHAR:
                           case AGGREMOVE:
                              break;
                           default:
                              ((Creature)target).getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this, Integer.valueOf(0));
                        }
                     }
                  } else if (target.isPlayer()) {
                     if (!target.equals(this)
                        && !target.equals(player)
                        && (target.getActingPlayer().getPvpFlag() > 0 || target.getActingPlayer().getKarma() > 0)) {
                        player.updatePvPStatus();
                     }
                  } else if (target.isAttackable()) {
                     switch(skill.getSkillType()) {
                        case UNLOCK:
                        case UNLOCK_SPECIAL:
                        case DELUXE_KEY_UNLOCK:
                        case SUMMON:
                           break;
                        case AGGREDUCE:
                        case AGGREDUCE_CHAR:
                        case AGGREMOVE:
                        default:
                           player.updatePvPStatus();
                     }
                  }
               }
            }

            for(Npc npcMob : World.getInstance().getAroundNpc(player)) {
               if (npcMob.isInsideRadius(player, 1000, true, true)) {
                  npcMob.getAI().notifyEvent(CtrlEvent.EVT_SEE_SPELL, skill, this);
                  if (npcMob.getTemplate().getEventQuests(Quest.QuestEventType.ON_SKILL_SEE) != null) {
                     for(Quest quest : npcMob.getTemplate().getEventQuests(Quest.QuestEventType.ON_SKILL_SEE)) {
                        quest.notifySkillSee(npcMob, player, skill, targets, this.isSummon());
                     }
                  }

                  if (npcMob.isAttackable()) {
                     Attackable attackable = (Attackable)npcMob;
                     int skillEffectPoint = skill.getAggroPoints();
                     if (player.hasSummon() && targets.length == 1 && Util.contains(targets, player.getSummon())) {
                        skillEffectPoint = 0;
                     }

                     if (skillEffectPoint > 0 && attackable.hasAI() && attackable.getAI().getIntention() == CtrlIntention.ATTACK) {
                        GameObject npcTarget = attackable.getTarget();

                        for(GameObject skillTarget : targets) {
                           if (npcTarget == skillTarget || npcMob == skillTarget) {
                              Creature originalCaster = (Creature)(this.isSummon() ? player.getSummon() : player);
                              attackable.addDamageHate(originalCaster, 0, (int)((double)skillEffectPoint * Config.MATK_HATE_MOD));
                           }
                        }
                     }
                  }
               }
            }
         }

         if (skill.isOffensive()) {
            switch(skill.getSkillType()) {
               case AGGREDUCE:
               case AGGREDUCE_CHAR:
               case AGGREMOVE:
                  return;
               default:
                  for(GameObject target : targets) {
                     if (target instanceof Creature && ((Creature)target).hasAI()) {
                        ((Creature)target).getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this, Integer.valueOf(0));
                     }
                  }
            }
         }
      } catch (Exception var16) {
         _log.log(Level.WARNING, this.getClass().getSimpleName() + ": callSkill() failed.", (Throwable)var16);
      }
   }

   public boolean isBehind(GameObject target) {
      double maxAngleDiff = 60.0;
      if (target == null) {
         return false;
      } else {
         if (target instanceof Creature) {
            Creature target1 = (Creature)target;
            double angleChar = Util.calculateAngleFrom(this, target1);
            double angleTarget = Util.convertHeadingToDegree(target1.getHeading());
            double angleDiff = angleChar - angleTarget;
            if (angleDiff <= -300.0) {
               angleDiff += 360.0;
            }

            if (angleDiff >= 300.0) {
               angleDiff -= 360.0;
            }

            if (Math.abs(angleDiff) <= 60.0) {
               return true;
            }
         }

         return false;
      }
   }

   public boolean isBehindTarget() {
      return this.isBehind(this.getTarget());
   }

   public boolean isInFrontOf(Creature target) {
      double maxAngleDiff = 60.0;
      if (target == null) {
         return false;
      } else {
         double angleTarget = Util.calculateAngleFrom(target, this);
         double angleChar = Util.convertHeadingToDegree(target.getHeading());
         double angleDiff = angleChar - angleTarget;
         if (angleDiff <= -300.0) {
            angleDiff += 360.0;
         }

         if (angleDiff >= 300.0) {
            angleDiff -= 360.0;
         }

         return Math.abs(angleDiff) <= 60.0;
      }
   }

   public boolean isInFrontOfTarget(Creature target, int range) {
      double maxAngleDiff = (double)range;
      if (target == null) {
         return false;
      } else {
         double angleTarget = Util.calculateAngleFrom(target, this);
         double angleChar = Util.convertHeadingToDegree(target.getHeading());
         double angleDiff = angleChar - angleTarget;
         if (angleDiff <= -360.0 + maxAngleDiff) {
            angleDiff += 360.0;
         }

         if (angleDiff >= 360.0 - maxAngleDiff) {
            angleDiff -= 360.0;
         }

         return Math.abs(angleDiff) <= maxAngleDiff;
      }
   }

   public boolean isFacing(GameObject target, int maxAngle) {
      if (target == null) {
         return false;
      } else {
         double maxAngleDiff = (double)maxAngle / 2.0;
         double angleTarget = Util.calculateAngleFrom(this, target);
         double angleChar = Util.convertHeadingToDegree(this.getHeading());
         double angleDiff = angleChar - angleTarget;
         if (angleDiff <= -360.0 + maxAngleDiff) {
            angleDiff += 360.0;
         }

         if (angleDiff >= 360.0 - maxAngleDiff) {
            angleDiff -= 360.0;
         }

         return Math.abs(angleDiff) <= maxAngleDiff;
      }
   }

   public boolean isInFrontOfTarget() {
      GameObject target = this.getTarget();
      return target instanceof Creature ? this.isInFrontOf((Creature)target) : false;
   }

   public double getLevelMod() {
      return Config.ALLOW_NPC_LVL_MOD ? (double)(this.getLevel() + 89) / 100.0 : 1.0;
   }

   public final void setSkillCast(Future<?> newSkillCast) {
      this._skillCast = newSkillCast;
   }

   public final void forceIsCasting(long interruptTime) {
      this.setIsCastingNow(true);
      this._castInterruptTime = interruptTime;
   }

   public void updatePvPFlag(int value) {
   }

   public final double getRandomDamageMultiplier() {
      Weapon activeWeapon = this.getActiveWeaponItem();
      int random;
      if (activeWeapon != null) {
         random = activeWeapon.getRandomDamage();
      } else {
         random = 5 + (int)Math.sqrt((double)this.getLevel());
      }

      return 1.0 + (double)Rnd.get(0 - random, random) / 100.0;
   }

   public long getAttackEndTime() {
      return this._attackEndTime;
   }

   public long getBowAttackEndTime() {
      return this._disableBowAttackEndTime;
   }

   public abstract int getLevel();

   public final double calcStat(Stats stat, double init, Creature target, Skill skill) {
      return this.getStat().calcStat(stat, init, target, skill);
   }

   public int getAccuracy() {
      return this.getStat().getAccuracy();
   }

   public float getAttackSpeedMultiplier() {
      return this.getStat().getAttackSpeedMultiplier();
   }

   public int getCON() {
      return this.getStat().getCON();
   }

   public int getDEX() {
      return this.getStat().getDEX();
   }

   public final double getCriticalDmg(Creature target, double init, Skill skill) {
      return this.getStat().getCriticalDmg(target, init, skill);
   }

   public double getCriticalHit(Creature target, Skill skill) {
      return this.getStat().getCriticalHit(target, skill);
   }

   public int getEvasionRate(Creature target) {
      return this.getStat().getEvasionRate(target);
   }

   public int getINT() {
      return this.getStat().getINT();
   }

   public final int getMagicalAttackRange(Skill skill) {
      return this.getStat().getMagicalAttackRange(skill);
   }

   public final double getMaxCp() {
      return this.getStat().getMaxCp();
   }

   public final int getMaxRecoverableCp() {
      return this.getStat().getMaxRecoverableCp();
   }

   public double getMAtk(Creature target, Skill skill) {
      return this.getStat().getMAtk(target, skill);
   }

   public double getMAtkSpd() {
      return this.getStat().getMAtkSpd();
   }

   public double getMaxMp() {
      return this.getStat().getMaxMp();
   }

   public int getMaxRecoverableMp() {
      return this.getStat().getMaxRecoverableMp();
   }

   public double getMaxHp() {
      return this.getStat().getMaxHp();
   }

   public int getMaxRecoverableHp() {
      return this.getStat().getMaxRecoverableHp();
   }

   public final double getMCriticalHit(Creature target, Skill skill) {
      return this.getStat().getMCriticalHit(target, skill);
   }

   public double getMDef(Creature target, Skill skill) {
      return this.getStat().getMDef(target, skill);
   }

   public int getMEN() {
      return this.getStat().getMEN();
   }

   public double getMReuseRate(Skill skill) {
      return this.getStat().getMReuseRate(skill);
   }

   public double getMovementSpeedMultiplier() {
      return this.getStat().getMovementSpeedMultiplier();
   }

   public double getPAtk(Creature target) {
      return this.getStat().getPAtk(target);
   }

   public double getPAtkAnimals(Creature target) {
      return this.getStat().getPAtkAnimals(target);
   }

   public double getPAtkDragons(Creature target) {
      return this.getStat().getPAtkDragons(target);
   }

   public double getPAtkInsects(Creature target) {
      return this.getStat().getPAtkInsects(target);
   }

   public double getPAtkMonsters(Creature target) {
      return this.getStat().getPAtkMonsters(target);
   }

   public double getPAtkPlants(Creature target) {
      return this.getStat().getPAtkPlants(target);
   }

   public double getPAtkGiants(Creature target) {
      return this.getStat().getPAtkGiants(target);
   }

   public double getPAtkMagicCreatures(Creature target) {
      return this.getStat().getPAtkMagicCreatures(target);
   }

   public double getPDefAnimals(Creature target) {
      return this.getStat().getPDefAnimals(target);
   }

   public double getPDefDragons(Creature target) {
      return this.getStat().getPDefDragons(target);
   }

   public double getPDefInsects(Creature target) {
      return this.getStat().getPDefInsects(target);
   }

   public double getPDefMonsters(Creature target) {
      return this.getStat().getPDefMonsters(target);
   }

   public double getPDefPlants(Creature target) {
      return this.getStat().getPDefPlants(target);
   }

   public double getPDefGiants(Creature target) {
      return this.getStat().getPDefGiants(target);
   }

   public double getPDefMagicCreatures(Creature target) {
      return this.getStat().getPDefMagicCreatures(target);
   }

   public double getPAtkSpd() {
      return this.getStat().getPAtkSpd();
   }

   public double getPDef(Creature target) {
      return this.getStat().getPDef(target);
   }

   public final int getPhysicalAttackRange() {
      return this.getStat().getPhysicalAttackRange();
   }

   public double getRunSpeed() {
      return this.getStat().getRunSpeed();
   }

   public double getSwimRunSpeed() {
      return this.getStat().getSwimRunSpeed();
   }

   public final int getShldDef() {
      return this.getStat().getShldDef();
   }

   public int getSTR() {
      return this.getStat().getSTR();
   }

   public double getWalkSpeed() {
      return this.getStat().getWalkSpeed();
   }

   public final double getSwimWalkSpeed() {
      return this.getStat().getSwimWalkSpeed();
   }

   public double getMoveSpeed() {
      return this.isPlayer() && this.getActingPlayer().isFalling() ? this.getStat().getMoveSpeed() * 2.0 : this.getStat().getMoveSpeed();
   }

   public int getWIT() {
      return this.getStat().getWIT();
   }

   public double getRExp() {
      return this.getStat().getRExp();
   }

   public double getRSp() {
      return this.getStat().getRSp();
   }

   public double getPvpPhysSkillDmg() {
      return this.getStat().getPvpPhysSkillDmg();
   }

   public double getPvpPhysSkillDef() {
      return this.getStat().getPvpPhysSkillDef();
   }

   public double getPvpPhysDef() {
      return this.getStat().getPvpPhysDef();
   }

   public double getPvpPhysDmg() {
      return this.getStat().getPvpPhysDmg();
   }

   public double getPvpMagicDmg() {
      return this.getStat().getPvpMagicDmg();
   }

   public double getPvpMagicDef() {
      return this.getStat().getPvpMagicDef();
   }

   public void addStatusListener(Creature object) {
      this.getStatus().addStatusListener(object);
   }

   public void reduceCurrentHp(double i, Creature attacker, Skill skill) {
      if (skill != null && !skill.isStatic() && this.getChanceSkills() != null) {
         this.getChanceSkills().onSkillHit(attacker, i, skill, true);
      }

      this.reduceCurrentHp(i, attacker, true, false, skill);
   }

   public void reduceCurrentHpByDOT(double i, Creature attacker, Skill skill) {
      this.reduceCurrentHp(i, attacker, !skill.isToggle(), true, skill);
   }

   public void reduceCurrentHp(double i, Creature attacker, boolean awake, boolean isDOT, Skill skill) {
      this.getStatus().reduceHp(i, attacker, awake, isDOT, false);
   }

   public void reduceCurrentMp(double i) {
      this.getStatus().reduceMp(i);
   }

   @Override
   public void removeStatusListener(Creature object) {
      this.getStatus().removeStatusListener(object);
   }

   public void stopHpMpRegeneration() {
      this.getStatus().stopHpMpRegeneration();
   }

   public final double getCurrentCp() {
      return this.getStatus().getCurrentCp();
   }

   public final void setCurrentCp(Double newCp) {
      this.setCurrentCp(newCp.doubleValue());
   }

   public final void setCurrentCp(double newCp) {
      this.getStatus().setCurrentCp(newCp);
   }

   public final double getCurrentHp() {
      return this.getStatus().getCurrentHp();
   }

   public final void setCurrentHp(double newHp) {
      this.getStatus().setCurrentHp(newHp);
   }

   public final void setCurrentHpMp(double newHp, double newMp) {
      this.getStatus().setCurrentHpMp(newHp, newMp);
   }

   public final double getCurrentMp() {
      return this.getStatus().getCurrentMp();
   }

   public final void setCurrentMp(Double newMp) {
      this.setCurrentMp(newMp.doubleValue());
   }

   public final void setCurrentMp(double newMp) {
      this.getStatus().setCurrentMp(newMp);
   }

   public int getMaxLoad() {
      return 0;
   }

   public int getBonusWeightPenalty() {
      return 0;
   }

   public int getCurrentLoad() {
      return 0;
   }

   public int getMaxBuffCount() {
      Effect effect = this.getFirstPassiveEffect(EffectType.ENLARGE_ABNORMAL_SLOT);
      return this.hasPremiumBonus()
         ? Config.BUFFS_MAX_AMOUNT_PREMIUM + (effect == null ? 0 : (int)effect.calc())
         : Config.BUFFS_MAX_AMOUNT + (effect == null ? 0 : (int)effect.calc());
   }

   public void sendDamageMessage(Creature target, int damage, Skill skill, boolean mcrit, boolean pcrit, boolean miss) {
   }

   public FusionSkill getFusionSkill() {
      return this._fusionSkill;
   }

   public void setFusionSkill(FusionSkill fb) {
      this._fusionSkill = fb;
   }

   public byte getAttackElement() {
      return this.getStat().getAttackElement();
   }

   public int getAttackElementValue(byte attackAttribute) {
      return this.getStat().getAttackElementValue(attackAttribute);
   }

   public int getDefenseElementValue(byte defenseAttribute) {
      return this.getStat().getDefenseElementValue(defenseAttribute);
   }

   public final void startPhysicalAttackMuted() {
      this.abortAttack();
   }

   public void disableCoreAI(boolean val) {
      this._AIdisabled = val;
   }

   public boolean isCoreAIDisabled() {
      return this._AIdisabled;
   }

   public boolean giveRaidCurse() {
      return true;
   }

   public boolean isAffected(EffectFlag flag) {
      return this._effects.isAffected(flag);
   }

   public void broadcastSocialAction(int id) {
      this.broadcastPacket(new SocialAction(this.getObjectId(), id));
   }

   public int getTeam() {
      return this._team;
   }

   public void setTeam(int id) {
      if (id >= 0 && id <= 2) {
         this._team = id;
      }
   }

   public void addOverrideCond(PcCondOverride... excs) {
      for(PcCondOverride exc : excs) {
         this._exceptions |= (long)exc.getMask();
      }
   }

   public void removeOverridedCond(PcCondOverride... excs) {
      for(PcCondOverride exc : excs) {
         this._exceptions &= (long)(~exc.getMask());
      }
   }

   public boolean canOverrideCond(PcCondOverride excs) {
      return (this._exceptions & (long)excs.getMask()) == (long)excs.getMask();
   }

   public void setOverrideCond(long masks) {
      this._exceptions = masks;
   }

   public Map<Integer, OptionsSkillHolder> getTriggerSkills() {
      if (this._triggerSkills == null) {
         synchronized(this) {
            if (this._triggerSkills == null) {
               this._triggerSkills = new ConcurrentHashMap<>();
            }
         }
      }

      return this._triggerSkills;
   }

   public void addTriggerSkill(OptionsSkillHolder holder) {
      this.getTriggerSkills().put(holder.getId(), holder);
   }

   public void removeTriggerSkill(OptionsSkillHolder holder) {
      this.getTriggerSkills().remove(holder.getId());
   }

   public void makeTriggerCast(Skill skill, Creature target) {
      try {
         if (skill.checkCondition(this, target, false, true)) {
            if (skill.triggersChanceSkill()) {
               skill = SkillsParser.getInstance().getInfo(skill.getTriggeredChanceId(), skill.getTriggeredChanceLevel());
               if (skill == null || skill.getSkillType() == SkillType.NOTDONE) {
                  return;
               }

               if (!skill.checkCondition(this, target, false, true)) {
                  return;
               }
            }

            if (this.isSkillDisabled(skill) || this.isSkillBlocked(skill)) {
               return;
            }

            if (!skill.isHandler() || skill.getReuseDelay() > 0) {
               this.disableSkill(skill, (long)skill.getReuseDelay());
            }

            GameObject[] targets = skill.getTargetList(this, false, target);
            if (targets.length == 0) {
               return;
            }

            Creature firstTarget = (Creature)targets[0];
            if (Config.ALT_VALIDATE_TRIGGER_SKILLS && this.isPlayable() && firstTarget != null && firstTarget.isPlayable()) {
               Player player = this.getActingPlayer();
               if (!player.checkPvpSkill(firstTarget, skill, this.isSummon())) {
                  return;
               }
            }

            this.broadcastPacket(new MagicSkillLaunched(this, skill.getDisplayId(), skill.getLevel(), targets));
            this.broadcastPacket(new MagicSkillUse(this, firstTarget, skill.getDisplayId(), skill.getLevel(), 0, 0));
            ISkillHandler handler = SkillHandler.getInstance().getHandler(skill.getSkillType());
            if (handler != null) {
               handler.useSkill(this, skill, targets);
            } else {
               skill.useSkill(this, targets);
            }
         }
      } catch (Exception var6) {
         _log.log(Level.WARNING, "", (Throwable)var6);
      }
   }

   public boolean canRevive() {
      if (this.isInsideZone(ZoneId.FUN_PVP)) {
         FunPvpZone zone = ZoneManager.getInstance().getZone(this, FunPvpZone.class);
         if (zone != null) {
            return !zone.canRevive();
         }
      }

      return true;
   }

   public void setCanRevive(boolean val) {
   }

   public boolean isSweepActive() {
      return false;
   }

   public boolean isOnEvent() {
      return false;
   }

   public int getClanId() {
      return 0;
   }

   public int getAllyId() {
      return 0;
   }

   public void notifyDamageReceived(double damage, Creature attacker, Skill skill, boolean critical, boolean damageOverTime) {
      this.getEvents().onDamageReceived(damage, attacker, skill, critical, damageOverTime);
      attacker.getEvents().onDamageDealt(damage, this, skill, critical, damageOverTime);
   }

   public PremiumBonus getPremiumBonus() {
      return null;
   }

   public boolean hasPremiumBonus() {
      return false;
   }

   public void block() {
      this._blocked = true;
   }

   public void unblock() {
      this._blocked = false;
   }

   public boolean isBlocked() {
      return this._blocked;
   }

   public boolean isInCategory(CategoryType type) {
      return false;
   }

   @Override
   public boolean isCreature() {
      return true;
   }

   public boolean isShowSummonAnimation() {
      return this._showSummonAnimation;
   }

   public void setShowSummonAnimation(boolean showSummonAnimation) {
      this._showSummonAnimation = showSummonAnimation;
   }

   public boolean isInZonePeace() {
      return this.isInsideZone(ZoneId.PEACE) || this.isInTownZone();
   }

   public boolean isInTownZone() {
      return this.isInsideZone(ZoneId.TOWN) && TownManager.getTownZone(this.getX(), this.getY(), this.getZ()) != null;
   }

   public void sayString(String text, int type) {
      this.broadcastPacket(new CreatureSay(this.getObjectId(), type, this.getName(), text));
   }

   public void rndWalk() {
      int posX = this.getX();
      int posY = this.getY();
      int posZ = this.getZ();
      switch(Rnd.get(1, 6)) {
         case 1:
            posX += 140;
            posY += 280;
            break;
         case 2:
            posX += 250;
            posY += 150;
            break;
         case 3:
            posX += 169;
            posY -= 200;
            break;
         case 4:
            posX += 110;
            posY -= 200;
            break;
         case 5:
            posX -= 250;
            posY -= 120;
            break;
         case 6:
            posX -= 200;
            posY += 160;
      }

      if (GeoEngine.canMoveToCoord(this.getX(), this.getY(), this.getZ(), posX, posY, posZ, this.getGeoIndex())) {
         this.setRunning();
         this.getAI().setIntention(CtrlIntention.MOVING, new Location(posX, posY, posZ));
      }
   }

   public int calcHeading(int x, int y) {
      return (int)(Math.atan2((double)(this.getY() - y), (double)(this.getX() - x)) * 10430.378350470453) + 32768;
   }

   public void teleToClosestTown() {
   }

   public int getMinDistance(GameObject obj) {
      int distance = (int)this.getColRadius();
      if (obj != null) {
         distance = (int)((double)distance + obj.getColRadius());
      }

      return distance;
   }

   public Location getFlyLocation(GameObject target, Skill skill) {
      if (target != null && target != this) {
         double radian = PositionUtils.convertHeadingToRadian(target.getHeading());
         Location loc;
         if (skill.isFlyToBack()) {
            loc = new Location(target.getX() + (int)(Math.sin(radian) * 40.0), target.getY() - (int)(Math.cos(radian) * 40.0), target.getZ());
         } else {
            loc = new Location(target.getX() - (int)(Math.sin(radian) * 40.0), target.getY() + (int)(Math.cos(radian) * 40.0), target.getZ());
         }

         if (this.isFlying()) {
            if (this.isPlayer() && this.isTransformed() && (loc.getZ() <= 0 || loc.getZ() >= 6000)) {
               return null;
            }

            if (GeoEngine.moveCheckInAir(this.getX(), this.getY(), this.getZ(), loc.getX(), loc.getY(), loc.getZ(), this.getColRadius(), this.getGeoIndex())
               == null) {
               return null;
            }
         } else {
            loc.correctGeoZ(this.getGeoIndex());
            if (!GeoEngine.canMoveToCoord(this.getX(), this.getY(), this.getZ(), loc.getX(), loc.getY(), loc.getZ(), this.getGeoIndex())) {
               loc = target.getLocation();
               if (!GeoEngine.canMoveToCoord(this.getX(), this.getY(), this.getZ(), loc.getX(), loc.getY(), loc.getZ(), this.getGeoIndex())) {
                  return null;
               }
            }
         }

         return loc;
      } else {
         double radian = PositionUtils.convertHeadingToRadian(this.getHeading());
         int x1 = -((int)(Math.sin(radian) * (double)skill.getFlyRadius()));
         int y1 = (int)(Math.cos(radian) * (double)skill.getFlyRadius());
         return this.isFlying()
            ? GeoEngine.moveCheckInAir(
               this.getX(), this.getY(), this.getZ(), this.getX() + x1, this.getY() + y1, this.getZ(), this.getColRadius(), this.getGeoIndex()
            )
            : GeoEngine.moveCheck(this.getX(), this.getY(), this.getZ(), this.getX() + x1, this.getY() + y1, this.getGeoIndex());
      }
   }

   protected void finishFly() {
      Location flyLoc = this._flyLoc;
      this._flyLoc = null;
      if (flyLoc != null) {
         this._isFlyingPos = true;
         this.setXYZ(flyLoc.getX(), flyLoc.getY(), flyLoc.getZ());
         this.broadcastPacket(new ValidateLocation(this));
         this.revalidateZone(true);
      }
   }

   public void correctCharPosition(int x, int y, int z) {
      if (this._isFlyingPos) {
         this.setXYZ(x, y, this.getZ());
         this._isFlyingPos = false;
      } else {
         this.setXYZ(x, y, z);
      }
   }

   public final boolean isIsDanceStun() {
      return this._isDanceStun;
   }

   public final void setIsDanceStun(boolean mode) {
      this._isDanceStun = mode;
   }

   public void addInvulAgainst(int skillId, int skillLvl) {
      InvulSkillHolder invulHolder = this.getInvulAgainstSkills().get(skillId);
      if (invulHolder != null) {
         invulHolder.increaseInstances();
      } else {
         this.getInvulAgainstSkills().put(skillId, new InvulSkillHolder(skillId, skillLvl));
      }
   }

   private Map<Integer, InvulSkillHolder> getInvulAgainstSkills() {
      if (this._invulAgainst == null) {
         synchronized(this) {
            if (this._invulAgainst == null) {
               return this._invulAgainst = new ConcurrentHashMap<>();
            }
         }
      }

      return this._invulAgainst;
   }

   public void removeInvulAgainst(int skillId, int skillLvl) {
      InvulSkillHolder invulHolder = this.getInvulAgainstSkills().get(skillId);
      if (invulHolder != null && invulHolder.decreaseInstances() < 1) {
         this._invulAgainst.remove(skillId);
      }
   }

   public boolean isInvulAgainst(int skillId, int skillLvl) {
      InvulSkillHolder invulHolder = this.getInvulAgainstSkills().get(skillId);
      if (invulHolder == null) {
         return false;
      } else {
         return invulHolder.getLvl() < 1 || invulHolder.getLvl() == skillLvl;
      }
   }

   public final double getCurrentHpRatio() {
      return this.getCurrentHp() / this.getMaxHp();
   }

   public final double getCurrentHpPercents() {
      return this.getCurrentHpRatio() * 100.0;
   }

   public final boolean isCurrentHpFull() {
      return (int)this.getCurrentHp() >= (int)this.getMaxHp();
   }

   public final double getCurrentMpRatio() {
      return this.getCurrentMp() / this.getMaxMp();
   }

   public final double getCurrentMpPercents() {
      return this.getCurrentMpRatio() * 100.0;
   }

   public final double getCurrentCpRatio() {
      return this.getCurrentCp() / this.getMaxCp();
   }

   public final double getCurrentCpPercents() {
      return this.getCurrentCpRatio() * 100.0;
   }

   public void setChampionTemplate(ChampionTemplate championTemplate) {
      this._championTemplate = championTemplate;
   }

   public ChampionTemplate getChampionTemplate() {
      return this._championTemplate;
   }

   public AbstractFightEvent getFightEvent() {
      return this.getEvent(AbstractFightEvent.class);
   }

   public boolean isRegisteredInFightEvent() {
      return this.getEvent(AbstractFightEvent.class) != null;
   }

   public boolean isInFightEvent() {
      try {
         if (this.getEvent(AbstractFightEvent.class) == null) {
            return false;
         } else {
            return this.getEvent(AbstractFightEvent.class).getFightEventPlayer(this) != null;
         }
      } catch (NullPointerException var2) {
         return false;
      }
   }

   public void startHealBlocked(boolean blocked) {
      this._isHealBlocked = blocked;
   }

   public boolean isHealBlocked() {
      return this.isAlikeDead() || this._isHealBlocked;
   }

   public void setWatchDistance(int distance) {
      this._watchDistance = distance;
   }

   @Override
   public int getWatchDistance() {
      return this._watchDistance;
   }

   public void sendActionFailed() {
      this.sendPacket(ActionFail.STATIC_PACKET);
   }

   public CharListenerList getListeners() {
      if (this._listeners == null) {
         synchronized(this) {
            if (this._listeners == null) {
               this._listeners = new CharListenerList(this);
            }
         }
      }

      return this._listeners;
   }

   public <T extends Listener<Creature>> boolean addListener(T listener) {
      return this.getListeners().add(listener);
   }

   public <T extends Listener<Creature>> boolean removeListener(T listener) {
      return this.getListeners().remove(listener);
   }

   public void onEvtTimer(int timerId, Object arg1) {
   }

   public boolean isCancelAction() {
      return false;
   }

   public void broadcastCharInfo() {
   }

   public void broadcastCharInfoImpl() {
   }

   public Location getMinionPosition() {
      return null;
   }

   public boolean isLethalImmune() {
      return false;
   }

   public boolean isGlobalAI() {
      return false;
   }

   @Override
   public double getColRadius() {
      return (double)this.getTemplate().getCollisionRadius();
   }

   @Override
   public double getColHeight() {
      return (double)this.getTemplate().getCollisionHeight();
   }

   public Creature getCastingTarget() {
      return this._castingTarget;
   }

   public void setCastingTarget(Creature target) {
      this._castingTarget = target;
   }

   public Skill getCastingSkill() {
      return this._castingSkill;
   }

   public long getAnimationEndTime() {
      return this._animationEndTime;
   }

   @Override
   public void removeInfoObject(GameObject object) {
      if (object == this.getTarget()) {
         this.setTarget(null);
      }
   }

   public boolean isFakePlayer() {
      return false;
   }

   public AutoFarmOptions getFarmSystem() {
      return null;
   }

   public boolean checkEffectRangeInsidePeaceZone(Skill skill, int x, int y, int z) {
      List<ZoneType> zones = ZoneManager.getInstance().getZones(this.getX(), this.getY(), this.getZ());
      if (zones != null && !zones.isEmpty()) {
         int range = skill.getEffectRange();
         int up = y + range;
         int down = y - range;
         int left = x + range;
         int right = x - range;

         for(ZoneType e : zones) {
            if (e instanceof PeaceZone) {
               if (e.isInsideZone(x, up, z)) {
                  return false;
               }

               if (e.isInsideZone(x, down, z)) {
                  return false;
               }

               if (e.isInsideZone(left, y, z)) {
                  return false;
               }

               if (e.isInsideZone(right, y, z)) {
                  return false;
               }

               if (e.isInsideZone(x, y, z)) {
                  return false;
               }
            }
         }
      }

      return true;
   }

   public void onDeathInZones(Creature character) {
      List<ZoneType> zones = ZoneManager.getInstance().getZones(this.getX(), this.getY(), this.getZ());
      if (zones != null && !zones.isEmpty()) {
         for(ZoneType zone : zones) {
            if (zone != null) {
               zone.onDieInside(this);
            }
         }
      }
   }

   public void onReviveInZones(Creature character) {
      List<ZoneType> zones = ZoneManager.getInstance().getZones(this.getX(), this.getY(), this.getZ());
      if (zones != null && !zones.isEmpty()) {
         for(ZoneType zone : zones) {
            if (zone != null) {
               zone.onReviveInside(this);
            }
         }
      }
   }

   public boolean containsZone(int zoneId) {
      List<ZoneType> zones = ZoneManager.getInstance().getZones(this.getX(), this.getY(), this.getZ());
      if (zones != null && !zones.isEmpty()) {
         for(ZoneType zone : zones) {
            if (zone != null && zone.getId() == zoneId) {
               return true;
            }
         }
      }

      return false;
   }

   public static class MoveData {
      public int _moveStartTime;
      public int _moveTimestamp;
      public int _xDestination;
      public int _yDestination;
      public int _zDestination;
      public double _xAccurate;
      public double _yAccurate;
      public double _zAccurate;
      public int _heading;
      public boolean disregardingGeodata;
      public int onGeodataPathIndex;
      public List<Location> geoPath;
      public int geoPathAccurateTx;
      public int geoPathAccurateTy;
      public int geoPathGtx;
      public int geoPathGty;
   }
}
