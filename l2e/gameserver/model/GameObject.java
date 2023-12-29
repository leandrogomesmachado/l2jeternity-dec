package l2e.gameserver.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import l2e.commons.util.Util;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.handler.actionhandlers.ActionHandler;
import l2e.gameserver.handler.actionhandlers.IActionHandler;
import l2e.gameserver.handler.actionshifthandlers.ActionShiftHandler;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.instancemanager.MapRegionManager;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.poly.ObjectPoly;
import l2e.gameserver.model.entity.Reflection;
import l2e.gameserver.model.entity.events.AbstractFightEvent;
import l2e.gameserver.model.entity.events.model.FightEventOwner;
import l2e.gameserver.model.interfaces.IIdentifiable;
import l2e.gameserver.model.interfaces.ILocational;
import l2e.gameserver.model.interfaces.IPositionable;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.DeleteObject;
import l2e.gameserver.network.serverpackets.ExSendUIEvent;
import l2e.gameserver.network.serverpackets.GameServerPacket;

public abstract class GameObject extends FightEventOwner implements IIdentifiable, IPositionable {
   public static final Logger _log = Logger.getLogger(GameObject.class.getName());
   private boolean _isVisible;
   private boolean _isInvisible;
   private String _name;
   private String _nameRu;
   private int _objectId;
   private WorldRegion _worldRegion;
   private ObjectPoly _poly;
   private int _x;
   private int _y;
   private int _z;
   private int _heading;
   private int _reflectionId;
   private int _geoIndex = 0;
   private GameObject.InstanceType _instanceType = null;
   private volatile Map<String, Object> _scripts;

   public GameObject(int objectId) {
      this.setInstanceType(GameObject.InstanceType.GameObject);
      this._objectId = objectId;
   }

   protected final void setInstanceType(GameObject.InstanceType instanceType) {
      this._instanceType = instanceType;
   }

   public final GameObject.InstanceType getInstanceType() {
      return this._instanceType;
   }

   public final boolean isInstanceType(GameObject.InstanceType instanceType) {
      return this._instanceType.isType(instanceType);
   }

   public final boolean isInstanceTypes(GameObject.InstanceType... instanceType) {
      return this._instanceType.isTypes(instanceType);
   }

   public void onAction(Player player) {
      this.onAction(player, true);
   }

   public void onAction(Player player, boolean interact) {
      IActionHandler handler = ActionHandler.getInstance().getHandler(this.getInstanceType());
      if (handler != null) {
         handler.action(player, this, interact);
      }

      player.sendActionFailed();
   }

   public void onActionShift(Player player) {
      IActionHandler handler = ActionShiftHandler.getInstance().getHandler(this.getInstanceType());
      if (handler != null) {
         handler.action(player, this, true);
      }

      player.sendActionFailed();
   }

   public void onForcedAttack(Player player) {
      player.sendActionFailed();
   }

   public void onSpawn() {
   }

   @Override
   public void setXYZ(int x, int y, int z) {
      this.setX(x);
      this.setY(y);
      this.setZ(z);
      if (this.isVisible()) {
         try {
            WorldRegion region = World.getInstance().getRegion(this.getLocation());
            if (region != this._worldRegion) {
               this.setWorldRegion(region);
            }
         } catch (Exception var6) {
            if (this.isCreature()) {
               this.decayMe();
            } else if (this.isPlayer()) {
               _log.warning("Error with " + this.getName() + " coords: " + this.getX() + " " + this.getY() + " " + this.getZ());
               Location loc = MapRegionManager.getInstance().getTeleToLocation((Creature)this, TeleportWhereType.TOWN);
               if (loc != null) {
                  ((Creature)this).teleToLocation(loc, true);
               } else {
                  ((Creature)this).teleToLocation(new Location(-83646, 243397, -3700), false);
               }
            }
         }
      }
   }

   public final void setXYZInvisible(int x, int y, int z) {
      this.setXYZ(Util.limit(x, -294912, 229375), Util.limit(y, -262144, 294911), z);
      this.setIsVisible(false);
   }

   @Override
   public final int getX() {
      return this._x;
   }

   @Override
   public final int getY() {
      return this._y;
   }

   @Override
   public final int getZ() {
      return this._z;
   }

   @Override
   public void setXYZ(ILocational loc) {
      this.setXYZ(loc.getX(), loc.getY(), loc.getZ());
   }

   public Location getLocation() {
      return new Location(this.getX(), this.getY(), this.getZ(), this.getHeading());
   }

   @Override
   public void setLocation(Location loc) {
      this.setXYZ(loc);
   }

   @Override
   public void setHeading(int newHeading) {
      this._heading = newHeading;
   }

   @Override
   public int getHeading() {
      return this._heading;
   }

   public int getReflectionId() {
      return this._reflectionId;
   }

   @Override
   public void setX(int newX) {
      this._x = newX;
   }

   @Override
   public void setY(int newY) {
      this._y = newY;
   }

   @Override
   public void setZ(int newZ) {
      this._z = newZ;
   }

   public void setReflectionId(int reflectionId) {
      if (reflectionId >= 0 && this.getReflectionId() != reflectionId) {
         Reflection oldI = ReflectionManager.getInstance().getReflection(this.getReflectionId());
         Reflection newI = ReflectionManager.getInstance().getReflection(reflectionId);
         if (newI != null) {
            if (reflectionId == 0) {
               if (this.isPlayer()) {
                  Effect effect = this.getActingPlayer().getFirstEffect(8239);
                  if (effect != null) {
                     effect.exit();
                  }
               }

               this.setGeoIndex(0);
            } else {
               this.setGeoIndex(newI.getGeoIndex());
            }

            if (this.isPlayer()) {
               Player player = this.getActingPlayer();
               if (this.getReflectionId() > 0 && oldI != null) {
                  oldI.removePlayer(this.getObjectId());
                  if (oldI.isShowTimer()) {
                     int startTime = (int)((System.currentTimeMillis() - oldI.getInstanceStartTime()) / 1000L);
                     int endTime = (int)((oldI.getInstanceEndTime() - oldI.getInstanceStartTime()) / 1000L);
                     if (oldI.isTimerIncrease()) {
                        this.sendPacket(new ExSendUIEvent(this.getActingPlayer(), true, true, startTime, endTime, oldI.getTimerText()));
                     } else {
                        this.sendPacket(new ExSendUIEvent(this.getActingPlayer(), true, false, endTime - startTime, 0, oldI.getTimerText()));
                     }
                  }
               }

               if (reflectionId > 0) {
                  newI.addPlayer(this.getObjectId());
                  if (newI.isShowTimer()) {
                     int startTime = (int)((System.currentTimeMillis() - newI.getInstanceStartTime()) / 1000L);
                     int endTime = (int)((newI.getInstanceEndTime() - newI.getInstanceStartTime()) / 1000L);
                     if (newI.isTimerIncrease()) {
                        this.sendPacket(new ExSendUIEvent(this.getActingPlayer(), false, true, startTime, endTime, newI.getTimerText()));
                     } else {
                        this.sendPacket(new ExSendUIEvent(this.getActingPlayer(), false, false, endTime - startTime, 0, newI.getTimerText()));
                     }
                  }
               }

               if (player.hasSummon()) {
                  player.getSummon().setReflectionId(reflectionId);
               }
            } else if (this.isNpc()) {
               Npc npc = (Npc)this;
               if (this.getReflectionId() > 0 && oldI != null) {
                  oldI.removeNpc(npc);
               }

               if (reflectionId > 0) {
                  newI.addNpc(npc);
               }
            }

            this._reflectionId = reflectionId;
            if (this.isVisible() && !this.isPlayer()) {
               this.decayMe();
               this.spawnMe();
            }
         }
      }
   }

   public void decayMe() {
      this._isVisible = false;
      World.getInstance().removeObject(this);
      this.setWorldRegion(null);
   }

   public void refreshID() {
      World.getInstance().removeObject(this);
      IdFactory.getInstance().releaseId(this.getObjectId());
      this._objectId = IdFactory.getInstance().getNextId();
   }

   public final void spawnMe() {
      this._isVisible = true;
      this.setWorldRegion(World.getInstance().getRegion(this.getLocation()));
      World.getInstance().addObject(this);
      this.onSpawn();
   }

   public final void spawnMe(int x, int y, int z) {
      this.setXYZ(Util.limit(x, -294912, 229375), Util.limit(y, -262144, 294911), z);
      this.spawnMe();
   }

   public boolean isAttackable() {
      return false;
   }

   public abstract boolean isAutoAttackable(Creature var1);

   public final boolean isVisible() {
      return this._worldRegion != null && this._isVisible;
   }

   public final void setIsVisible(boolean value) {
      this._isVisible = value;
      if (!this._isVisible) {
         this.setWorldRegion(null);
      }
   }

   public final String getName() {
      return this._name;
   }

   public final String getNameRu() {
      return this._nameRu != null ? this._nameRu : this._name;
   }

   public void setName(String value) {
      this._name = value;
   }

   public void setNameRu(String value) {
      this._nameRu = value;
   }

   public final int getObjectId() {
      return this._objectId;
   }

   public final ObjectPoly getPoly() {
      return this._poly == null ? (this._poly = new ObjectPoly(this)) : this._poly;
   }

   public WorldRegion getWorldRegion() {
      return this._worldRegion;
   }

   public void setWorldRegion(WorldRegion newRegion) {
      try {
         List<WorldRegion> oldAreas = Collections.emptyList();
         if (this._worldRegion != null) {
            this._worldRegion.removeVisibleObject(this);
            oldAreas = this._worldRegion.getNeighbors();
         }

         List<WorldRegion> newAreas = Collections.emptyList();
         if (newRegion != null) {
            newRegion.addVisibleObject(this);
            newAreas = newRegion.getNeighbors();
         }

         int oid = this.getObjectId();
         int rid = this.getReflectionId();

         for(WorldRegion region : oldAreas) {
            if (!newAreas.contains(region)) {
               for(GameObject obj : region.getVisibleObjects().values()) {
                  if (obj == null || obj.getObjectId() != oid) {
                     obj.removeInfoObject(this);
                     this.removeInfoObject(obj);
                  }
               }

               if (this.isPlayer() && region.isEmptyNeighborhood()) {
                  region.switchActive(false);
               }
            }
         }

         for(WorldRegion region : newAreas) {
            if (!oldAreas.contains(region)) {
               for(GameObject obj : region.getVisibleObjects().values()) {
                  if (obj != null && obj.getObjectId() != oid && obj.getReflectionId() == rid) {
                     if (!obj.isPlayer()
                        || !this.isPlayer()
                        || !obj.getActingPlayer().getAppearance().isGhost()
                           && (!obj.getActingPlayer().isInStoreNow() || !this.getActingPlayer().getNotShowTraders())) {
                        obj.addInfoObject(this);
                        this.addInfoObject(obj);
                     } else {
                        obj.addInfoObject(this);
                     }
                  }
               }

               if (this.isPlayer()) {
                  region.switchActive(true);
               }
            }
         }

         this._worldRegion = newRegion;
      } catch (Exception var10) {
      }
   }

   public Player getActingPlayer() {
      return null;
   }

   public Npc getActingNpc() {
      return null;
   }

   public static final Player getActingPlayer(GameObject obj) {
      return obj == null ? null : obj.getActingPlayer();
   }

   public abstract void sendInfo(Player var1);

   @Override
   public String toString() {
      return this.getClass().getSimpleName() + ":" + this.getName() + "[" + this.getObjectId() + "]";
   }

   public void doDie(Creature killer) {
   }

   public void sendPacket(GameServerPacket mov) {
   }

   public void sendPacket(GameServerPacket... mov) {
   }

   public void sendPacket(List<? extends GameServerPacket> mov) {
   }

   public void sendPacket(SystemMessageId id) {
   }

   public void sendPacket(GameServerPacket packet, SystemMessageId id) {
   }

   public Creature getActingCharacter() {
      return null;
   }

   public boolean isPlayer() {
      return false;
   }

   public boolean isPlayable() {
      return false;
   }

   public boolean isSummon() {
      return false;
   }

   public boolean isPet() {
      return false;
   }

   public boolean isServitor() {
      return false;
   }

   public boolean isCreature() {
      return false;
   }

   public boolean isDoor() {
      return false;
   }

   public boolean isNpc() {
      return false;
   }

   public boolean isMonster() {
      return false;
   }

   public boolean isMinion() {
      return false;
   }

   public boolean isTrap() {
      return false;
   }

   public boolean isItem() {
      return false;
   }

   public boolean isVehicle() {
      return false;
   }

   public boolean isWalker() {
      return false;
   }

   public boolean isRunner() {
      return false;
   }

   public boolean isSpecialCamera() {
      return false;
   }

   public boolean isEkimusFood() {
      return false;
   }

   public boolean isTargetable() {
      return true;
   }

   public boolean isInsideZone(ZoneId zone) {
      return false;
   }

   public boolean isChargedShot(ShotType type) {
      return false;
   }

   public void setChargedShot(ShotType type, boolean charged) {
   }

   public void rechargeShots(boolean physical, boolean magical) {
   }

   public final <T> T addScript(T script) {
      if (this._scripts == null) {
         synchronized(this) {
            if (this._scripts == null) {
               this._scripts = new ConcurrentHashMap<>();
            }
         }
      }

      this._scripts.put(script.getClass().getName(), script);
      return script;
   }

   public final <T> T removeScript(Class<T> script) {
      return (T)(this._scripts == null ? null : this._scripts.remove(script.getName()));
   }

   public final <T> T getScript(Class<T> script) {
      return (T)(this._scripts == null ? null : this._scripts.get(script.getName()));
   }

   public void removeStatusListener(Creature object) {
   }

   public boolean isInvisible() {
      return this._isInvisible;
   }

   public void setInvisible(boolean invis) {
      this._isInvisible = invis;
      if (invis) {
         DeleteObject deletePacket = new DeleteObject(this);

         for(Player player : World.getInstance().getAroundPlayers(this)) {
            if (!this.isVisibleFor(player)) {
               if (player.getTarget() == this) {
                  player.setTarget(null);
                  player.abortAttack();
                  player.abortCast();
                  player.getAI().setIntention(CtrlIntention.IDLE);
               }

               if (player.hasSummon()) {
                  player.getSummon().setTarget(null);
                  player.getSummon().abortAttack();
                  player.getSummon().abortCast();
               }

               player.sendPacket(deletePacket);
            }
         }

         for(Npc npc : World.getInstance().getAroundNpc(this)) {
            if (npc != null && npc instanceof Attackable && !npc.isDead()) {
               npc.removeInfoObject(this);
            }
         }
      }

      this.broadcastInfo();
   }

   public boolean isVisibleFor(Player player) {
      return !this.isInvisible() || player.canOverrideCond(PcCondOverride.SEE_ALL_PLAYERS);
   }

   public void broadcastInfo() {
      for(Player player : World.getInstance().getAroundPlayers(this)) {
         if (this.isVisibleFor(player)) {
            this.sendInfo(player);
         }
      }
   }

   public final long getXYDeltaSq(int x, int y) {
      long dx = (long)(x - this.getX());
      long dy = (long)(y - this.getY());
      return dx * dx + dy * dy;
   }

   public final long getXYZDeltaSq(int x, int y, int z) {
      return this.getXYDeltaSq(x, y) + this.getZDeltaSq(z);
   }

   public final long getZDeltaSq(int z) {
      long dz = (long)(z - this.getZ());
      return dz * dz;
   }

   public final double getDistance(GameObject obj) {
      return obj == null ? 0.0 : Math.sqrt((double)this.getXYDeltaSq(obj.getX(), obj.getY()));
   }

   public final double getDistance3D(GameObject obj) {
      return obj == null ? 0.0 : Math.sqrt((double)this.getXYZDeltaSq(obj.getX(), obj.getY(), obj.getZ()));
   }

   public final double getRealDistance3D(GameObject obj) {
      return this.getRealDistance3D(obj, false);
   }

   public final double getRealDistance3D(GameObject obj, boolean ignoreZ) {
      double distance = ignoreZ ? this.getDistance(obj) : this.getDistance3D(obj);
      distance -= this.getColRadius();
      return distance > 0.0 ? distance : 0.0;
   }

   public final boolean isInRange(Location loc, long range) {
      return this.isInRangeSq(loc, range * range);
   }

   public final boolean isInRangeSq(Location loc, long range) {
      return this.getXYDeltaSq(loc) <= range;
   }

   public final long getXYDeltaSq(Location loc) {
      return this.getXYDeltaSq(loc.getX(), loc.getY());
   }

   public final boolean isInRangeZ(GameObject obj, long range) {
      if (obj == null) {
         return false;
      } else if (obj.getReflectionId() != this.getReflectionId()) {
         return false;
      } else {
         long dx = (long)Math.abs(obj.getX() - this.getX());
         if (dx > range) {
            return false;
         } else {
            long dy = (long)Math.abs(obj.getY() - this.getY());
            if (dy > range) {
               return false;
            } else {
               long dz = (long)Math.abs(obj.getZ() - this.getZ());
               return dz <= range && dx * dx + dy * dy + dz * dz <= range * range;
            }
         }
      }
   }

   public final boolean isInRangeZ(Creature actor, GameObject obj, long range) {
      if (obj == null) {
         return false;
      } else {
         long dx = (long)Math.abs(obj.getX() - actor.getX());
         if (dx > range) {
            return false;
         } else {
            long dy = (long)Math.abs(obj.getY() - actor.getY());
            if (dy > range) {
               return false;
            } else {
               long dz = (long)Math.abs(obj.getZ() - actor.getZ());
               return dz <= range && dx * dx + dy * dy + dz * dz <= range * range;
            }
         }
      }
   }

   public int getGeoIndex() {
      return this._geoIndex;
   }

   public void setGeoIndex(int geoIndex) {
      this._geoIndex = geoIndex;
   }

   public final double calculateDistance(ILocational loc, boolean includeZAxis, boolean squared) {
      return this.calculateDistance(loc.getX(), loc.getY(), loc.getZ(), includeZAxis, squared);
   }

   public double calculateDistance3D(int x, int y, int z) {
      return Math.sqrt(Math.pow((double)(x - this._x), 2.0) + Math.pow((double)(y - this._y), 2.0) + Math.pow((double)(z - this._z), 2.0));
   }

   public final double calculateDistance(int x, int y, int z, boolean includeZAxis, boolean squared) {
      double distance = Math.pow((double)(x - this.getX()), 2.0)
         + Math.pow((double)(y - this.getY()), 2.0)
         + (includeZAxis ? Math.pow((double)(z - this.getZ()), 2.0) : 0.0);
      return squared ? distance : Math.sqrt(distance);
   }

   public final double calculateDirectionTo(GameObject target) {
      int heading = Util.calculateHeadingFrom(this, target) - this.getHeading() & 65535;
      return Util.convertHeadingToDegree(heading);
   }

   public final double calculateDistance(GameObject obj, boolean includeZAxis, boolean squared) {
      return this.calculateDistance(obj.getX(), obj.getY(), obj.getZ(), includeZAxis, squared);
   }

   public final boolean isInRange(GameObject obj, long range) {
      if (obj == null) {
         return false;
      } else if (obj.getReflectionId() != this.getReflectionId()) {
         return false;
      } else {
         long dx = (long)Math.abs(obj.getX() - this.getX());
         if (dx > range) {
            return false;
         } else {
            long dy = (long)Math.abs(obj.getY() - this.getY());
            if (dy > range) {
               return false;
            } else {
               long dz = (long)Math.abs(obj.getZ() - this.getZ());
               return dz <= 1500L && dx * dx + dy * dy <= range * range;
            }
         }
      }
   }

   public final boolean isInRangeZ(Location loc, long range) {
      return this.isInRangeZSq(loc, range * range);
   }

   public final boolean isInRangeZSq(Location loc, long range) {
      return this.getXYZDeltaSq(loc) <= range;
   }

   public final long getXYZDeltaSq(Location loc) {
      return this.getXYDeltaSq(loc.getX(), loc.getY()) + this.getZDeltaSq(loc.getZ());
   }

   public final double getDistance(Location loc) {
      return this.getDistance(loc.getX(), loc.getY(), loc.getZ());
   }

   public double getDistance(int x, int y, int z) {
      return Math.sqrt((double)this.getXYZDeltaSq(x, y, z));
   }

   public double getDistance(int x, int y) {
      return Math.sqrt((double)this.getXYDeltaSq(x, y));
   }

   public void addInfoObject(GameObject object) {
   }

   public void removeInfoObject(GameObject object) {
   }

   @Override
   public void addEvent(AbstractFightEvent event) {
      event.onAddEvent(this);
      super.addEvent(event);
   }

   @Override
   public void removeEvent(AbstractFightEvent event) {
      event.onRemoveEvent(this);
      super.removeEvent(event);
   }

   public int getWatchDistance() {
      return 0;
   }

   public boolean canBeAttacked() {
      return false;
   }

   public boolean isFlying() {
      return false;
   }

   public boolean isInWater(GameObject object) {
      return false;
   }

   public double getColRadius() {
      return 0.0;
   }

   public double getColHeight() {
      return 0.0;
   }

   public Location getSpawnedLoc() {
      return null;
   }

   public static enum InstanceType {
      GameObject(null),
      ItemInstance(GameObject),
      Creature(GameObject),
      Npc(Creature),
      Playable(Creature),
      Summon(Playable),
      Decoy(Creature),
      Player(Playable),
      NpcInstance(Npc),
      MerchantInstance(NpcInstance),
      WarehouseInstance(NpcInstance),
      StaticObjectInstance(Creature),
      DoorInstance(Creature),
      TerrainObjectInstance(Npc),
      EffectPointInstance(Npc),
      ServitorInstance(Summon),
      SiegeSummonInstance(ServitorInstance),
      MerchantSummonInstance(ServitorInstance),
      PetInstance(Summon),
      BabyPetInstance(PetInstance),
      DecoyInstance(Decoy),
      TrapInstance(Npc),
      Attackable(Npc),
      GuardInstance(Attackable),
      QuestGuardInstance(GuardInstance),
      MonsterInstance(Attackable),
      ChestInstance(MonsterInstance),
      ControllableMobInstance(MonsterInstance),
      FeedableBeastInstance(MonsterInstance),
      TamedBeastInstance(FeedableBeastInstance),
      FriendlyMobInstance(Attackable),
      RiftInvaderInstance(MonsterInstance),
      RaidBossInstance(MonsterInstance),
      GrandBossInstance(RaidBossInstance),
      FlyNpcInstance(NpcInstance),
      FlyMonsterInstance(MonsterInstance),
      FlyRaidBossInstance(RaidBossInstance),
      FlyTerrainObjectInstance(MonsterInstance),
      SepulcherNpcInstance(NpcInstance),
      SepulcherMonsterInstance(MonsterInstance),
      FestivalGiudeInstance(Npc),
      FestivalMonsterInstance(MonsterInstance),
      Vehicle(Creature),
      BoatInstance(Vehicle),
      AirShipInstance(Vehicle),
      ControllableAirShipInstance(AirShipInstance),
      DefenderInstance(Attackable),
      ArtefactInstance(NpcInstance),
      ControlTowerInstance(Npc),
      FlameTowerInstance(Npc),
      SiegeFlagInstance(Npc),
      SiegeNpcInstance(Npc),
      FortBallistaInstance(Npc),
      FortCommanderInstance(DefenderInstance),
      CastleChamberlainInstance(MerchantInstance),
      CastleMagicianInstance(NpcInstance),
      FortEnvoyInstance(Npc),
      FortLogisticsInstance(MerchantInstance),
      FortManagerInstance(MerchantInstance),
      FortSiegeNpcInstance(Npc),
      FortSupportCaptainInstance(MerchantInstance),
      SignsPriestInstance(Npc),
      DawnPriestInstance(SignsPriestInstance),
      DuskPriestInstance(SignsPriestInstance),
      DungeonGatekeeperInstance(Npc),
      AdventurerInstance(NpcInstance),
      AuctioneerInstance(Npc),
      ClanHallManagerInstance(MerchantInstance),
      FishermanInstance(MerchantInstance),
      ManorManagerInstance(MerchantInstance),
      ObservationInstance(Npc),
      OlympiadManagerInstance(Npc),
      PetManagerInstance(MerchantInstance),
      RaceManagerInstance(Npc),
      SymbolMakerInstance(Npc),
      TeleporterInstance(Npc),
      TrainerInstance(NpcInstance),
      VillageMasterInstance(NpcInstance),
      DoormenInstance(NpcInstance),
      CastleDoormenInstance(DoormenInstance),
      FortDoormenInstance(DoormenInstance),
      ClanHallDoormenInstance(DoormenInstance),
      ClassMasterInstance(NpcInstance),
      NpcBufferInstance(Npc),
      TvTEventNpcInstance(Npc),
      TvTRoundEventNpcInstance(Npc),
      EventMobInstance(Npc),
      UCManagerInstance(Npc),
      ChronoMonsterInstance(MonsterInstance),
      HotSpringSquashInstance(MonsterInstance),
      CommunityBufferInstance(Npc),
      CommunityBankInstance(Npc),
      CommunityTeleporterInstance(Npc),
      CommunityDonationInstance(Npc),
      CommunityShopInstance(Npc),
      CommunityDyesInstance(Npc),
      CommunitySpecialInstance(Npc),
      CommunitySubInstance(Npc),
      CommunityCraftInstance(Npc),
      MinionInstance(Attackable);

      private final GameObject.InstanceType _parent;
      private final long _typeL;
      private final long _typeH;
      private final long _maskL;
      private final long _maskH;

      private InstanceType(GameObject.InstanceType parent) {
         this._parent = parent;
         int high = this.ordinal() - 63;
         if (high < 0) {
            this._typeL = 1L << this.ordinal();
            this._typeH = 0L;
         } else {
            this._typeL = 0L;
            this._typeH = 1L << high;
         }

         if (this._typeL >= 0L && this._typeH >= 0L) {
            if (parent != null) {
               this._maskL = this._typeL | parent._maskL;
               this._maskH = this._typeH | parent._maskH;
            } else {
               this._maskL = this._typeL;
               this._maskH = this._typeH;
            }
         } else {
            throw new Error("Too many instance types, failed to load " + this.name());
         }
      }

      public final GameObject.InstanceType getParent() {
         return this._parent;
      }

      public final boolean isType(GameObject.InstanceType it) {
         return (this._maskL & it._typeL) > 0L || (this._maskH & it._typeH) > 0L;
      }

      public final boolean isTypes(GameObject.InstanceType... it) {
         for(GameObject.InstanceType i : it) {
            if (this.isType(i)) {
               return true;
            }
         }

         return false;
      }
   }
}
