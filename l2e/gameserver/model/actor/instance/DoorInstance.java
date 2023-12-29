package l2e.gameserver.model.actor.instance;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import l2e.commons.geometry.Shape;
import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.character.CharacterAI;
import l2e.gameserver.ai.character.DoorAI;
import l2e.gameserver.data.parser.DoorParser;
import l2e.gameserver.geodata.GeoCollision;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.ClanHallManager;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.status.DoorStatus;
import l2e.gameserver.model.actor.templates.door.DoorTemplate;
import l2e.gameserver.model.actor.templates.items.Weapon;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.entity.ClanHall;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.model.entity.Reflection;
import l2e.gameserver.model.entity.clanhall.SiegableHall;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.stats.StatsSet;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.EventTrigger;
import l2e.gameserver.network.serverpackets.StaticObject;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class DoorInstance extends Creature implements GeoCollision {
   private static final byte OPEN_BY_CLICK = 1;
   private static final byte OPEN_BY_TIME = 2;
   private static final byte OPEN_BY_ITEM = 4;
   private static final byte OPEN_BY_SKILL = 8;
   private static final byte OPEN_BY_CYCLE = 16;
   private int _castleIndex = -2;
   private int _fortIndex = -2;
   private ClanHall _clanHall;
   private boolean _open = false;
   private boolean _geoOpen = true;
   private byte[][] _geoAround;
   private boolean _isTargetable;
   private final boolean _checkCollision;
   private int _openType = 0;
   private int _meshindex = 1;
   private int _level = 0;
   protected int _closeTime = -1;
   protected int _openTime = -1;
   protected int _randomTime = -1;
   private Future<?> _autoCloseTask;
   private boolean _isAttackableDoor = false;
   private final Lock _openLock = new ReentrantLock();

   public DoorInstance(int objectId, DoorTemplate template, StatsSet data) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.DoorInstance);
      this.setIsInvul(false);
      this._isTargetable = data.getBool("targetable", true);
      if (this.getGroupName() != null) {
         DoorParser.addDoorGroup(this.getGroupName(), this.getDoorId());
      }

      if (data.getString("default_status", "close").equals("open")) {
         this._open = true;
         this._geoOpen = true;
      }

      this._closeTime = data.getInteger("close_time", -1);
      this._level = data.getInteger("level", 0);
      this._openType = data.getInteger("open_method", 0);
      this._checkCollision = data.getBool("check_collision", true);
      if (this.isOpenableByTime()) {
         this._openTime = data.getInteger("open_time");
         this._randomTime = data.getInteger("random_time", -1);
         this.startTimerOpen();
      }

      int clanhallId = data.getInteger("clanhall_id", 0);
      if (clanhallId > 0) {
         ClanHall hall = ClanHallManager.getAllClanHalls().get(clanhallId);
         if (hall != null) {
            this.setClanHall(hall);
            hall.getDoors().add(this);
         }
      }
   }

   @Override
   public void moveToLocation(int x, int y, int z, int offset) {
   }

   @Override
   public void stopMove(Location loc) {
   }

   @Override
   public synchronized void doAttack(Creature target) {
   }

   @Override
   public void doCast(Skill skill) {
   }

   @Override
   protected CharacterAI initAI() {
      return new DoorAI(this);
   }

   private void startTimerOpen() {
      int delay = this._open ? this._openTime : this._closeTime;
      if (this._randomTime > 0) {
         delay += Rnd.get(this._randomTime);
      }

      ThreadPoolManager.getInstance().schedule(new DoorInstance.TimerOpen(), (long)(delay * 1000));
   }

   public DoorTemplate getTemplate() {
      return (DoorTemplate)super.getTemplate();
   }

   public final DoorStatus getStatus() {
      return (DoorStatus)super.getStatus();
   }

   @Override
   public void initCharStatus() {
      this.setStatus(new DoorStatus(this));
   }

   public final boolean isOpenableBySkill() {
      return (this._openType & 8) != 0;
   }

   public final boolean isOpenableByItem() {
      return (this._openType & 4) != 0;
   }

   public final boolean isOpenableByClick() {
      return (this._openType & 1) != 0;
   }

   public final boolean isOpenableByTime() {
      return (this._openType & 2) != 0;
   }

   public final boolean isOpenableByCycle() {
      return (this._openType & 16) != 0;
   }

   @Override
   public final int getLevel() {
      return this._level;
   }

   @Override
   public int getId() {
      return this.getTemplate().getId();
   }

   public int getDoorId() {
      return this.getTemplate().doorId;
   }

   public boolean getOpen() {
      return this._open;
   }

   public boolean isOpened() {
      return this._open;
   }

   public boolean isClosed() {
      return !this._open;
   }

   public void setOpen(boolean open) {
      this._open = open;
      if (this.getChildId() > 0) {
         DoorInstance sibling = this.getSiblingDoor(this.getChildId());
         if (sibling != null) {
            sibling.notifyChildEvent(open);
         } else {
            _log.log(Level.WARNING, this.getClass().getSimpleName() + ": cannot find child id: " + this.getChildId());
         }
      }
   }

   public boolean getIsShowHp() {
      return this.getTemplate().showHp;
   }

   public int getDamage() {
      int dmg = 6 - (int)Math.ceil(this.getCurrentHp() / this.getMaxHp() * 6.0);
      if (dmg > 6) {
         return 6;
      } else {
         return dmg < 0 ? 0 : dmg;
      }
   }

   public final Castle getCastle() {
      if (this._castleIndex < 0) {
         this._castleIndex = CastleManager.getInstance().getCastleIndex(this);
      }

      return this._castleIndex < 0 ? null : CastleManager.getInstance().getCastles().get(this._castleIndex);
   }

   public final Fort getFort() {
      if (this._fortIndex < 0) {
         this._fortIndex = FortManager.getInstance().getFortIndex(this);
      }

      return this._fortIndex < 0 ? null : FortManager.getInstance().getForts().get(this._fortIndex);
   }

   public void setClanHall(ClanHall clanhall) {
      this._clanHall = clanhall;
   }

   public ClanHall getClanHall() {
      return this._clanHall;
   }

   public boolean isEnemy() {
      if (this.getCastle() != null && this.getCastle().getId() > 0 && this.getCastle().getZone().isActive() && this.getIsShowHp()) {
         return true;
      } else if (this.getFort() != null && this.getFort().getId() > 0 && this.getFort().getZone().isActive() && this.getIsShowHp()) {
         return true;
      } else {
         return this.getClanHall() != null
            && this.getClanHall().isSiegableHall()
            && ((SiegableHall)this.getClanHall()).getSiegeZone().isActive()
            && this.getIsShowHp();
      }
   }

   @Override
   public boolean isAutoAttackable(Creature attacker) {
      if (!(attacker instanceof Playable)) {
         return false;
      } else if (this.getIsAttackableDoor()) {
         return true;
      } else if (!this.getIsShowHp()) {
         return false;
      } else {
         Player actingPlayer = attacker.getActingPlayer();
         if (this.getClanHall() != null) {
            if (!this.getClanHall().isSiegableHall()) {
               return false;
            } else {
               return ((SiegableHall)this.getClanHall()).isInSiege()
                  && ((SiegableHall)this.getClanHall()).getSiege().doorIsAutoAttackable()
                  && ((SiegableHall)this.getClanHall()).getSiege().checkIsAttacker(actingPlayer.getClan());
            }
         } else {
            boolean isCastle = this.getCastle() != null && this.getCastle().getId() > 0 && this.getCastle().getZone().isActive();
            boolean isFort = this.getFort() != null && this.getFort().getId() > 0 && this.getFort().getZone().isActive();
            int activeSiegeId = this.getFort() != null ? this.getFort().getId() : (this.getCastle() != null ? this.getCastle().getId() : 0);
            if (TerritoryWarManager.getInstance().isTWInProgress()) {
               return !TerritoryWarManager.getInstance().isAllyField(actingPlayer, activeSiegeId);
            } else {
               if (isFort) {
                  Clan clan = actingPlayer.getClan();
                  if (clan != null && clan == this.getFort().getOwnerClan()) {
                     return false;
                  }
               } else if (isCastle) {
                  Clan clan = actingPlayer.getClan();
                  if (clan != null && clan.getId() == this.getCastle().getOwnerId()) {
                     return false;
                  }
               }

               return isCastle || isFort;
            }
         }
      }
   }

   @Override
   public void updateAbnormalEffect() {
   }

   @Override
   public ItemInstance getActiveWeaponInstance() {
      return null;
   }

   @Override
   public Weapon getActiveWeaponItem() {
      return null;
   }

   @Override
   public ItemInstance getSecondaryWeaponInstance() {
      return null;
   }

   public Weapon getSecondaryWeaponItem() {
      return null;
   }

   @Override
   public void broadcastStatusUpdate() {
      EventTrigger oe = this.getEmitter() > 0 ? new EventTrigger(this.getEmitter(), this.isClosed()) : null;

      for(Player player : World.getInstance().getAroundPlayers(this)) {
         if (player != null) {
            player.sendPacket(new StaticObject(this, player));
            if (oe != null) {
               player.sendPacket(oe);
            }
         }
      }
   }

   public final void openMe() {
      if (this.getGroupName() != null) {
         this.manageGroupOpen(true, this.getGroupName());
      } else {
         this._openLock.lock();

         try {
            this.setOpen(true);
            this.setGeoOpen(true);
         } finally {
            this._openLock.unlock();
         }

         this.broadcastStatusUpdate();
         this.startAutoCloseTask();
      }
   }

   public final void closeMe() {
      Future<?> oldTask = this._autoCloseTask;
      if (oldTask != null) {
         this._autoCloseTask = null;
         oldTask.cancel(false);
      }

      if (this.getGroupName() != null) {
         this.manageGroupOpen(false, this.getGroupName());
      } else {
         this._openLock.lock();

         try {
            this.setOpen(false);
            this.setGeoOpen(false);
         } finally {
            this._openLock.unlock();
         }

         this.broadcastStatusUpdate();
      }
   }

   private void manageGroupOpen(boolean open, String groupName) {
      Set<Integer> set = DoorParser.getDoorsByGroup(groupName);
      DoorInstance first = null;

      for(Integer id : set) {
         DoorInstance door = this.getSiblingDoor(id);
         if (first == null) {
            first = door;
         }

         if (door.getOpen() != open) {
            door.setOpen(open);
            door.setGeoOpen(open);
            door.broadcastStatusUpdate();
         }
      }

      if (first != null && open) {
         first.startAutoCloseTask();
      }
   }

   private void notifyChildEvent(boolean open) {
      byte openThis = open ? this.getTemplate().masterDoorOpen : this.getTemplate().masterDoorClose;
      if (openThis != 0) {
         if (openThis == 1) {
            this.openMe();
         } else if (openThis == -1) {
            this.closeMe();
         }
      }
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName() + "[" + this.getTemplate().doorId + "](" + this.getObjectId() + ")";
   }

   public String getDoorName() {
      return this.getTemplate().name;
   }

   public int getX(int i) {
      return this.getTemplate().nodeX[i];
   }

   public int getY(int i) {
      return this.getTemplate().nodeY[i];
   }

   public int getZMin() {
      return this.getTemplate().nodeZ;
   }

   public int getZMax() {
      return this.getTemplate().nodeZ + this.getTemplate().height;
   }

   public List<DefenderInstance> getKnownDefenders() {
      List<DefenderInstance> result = new ArrayList<>();

      for(Npc obj : World.getInstance().getAroundNpc(this)) {
         if (obj instanceof DefenderInstance) {
            result.add((DefenderInstance)obj);
         }
      }

      return result;
   }

   public void setMeshIndex(int mesh) {
      this._meshindex = mesh;
   }

   public int getMeshIndex() {
      return this._meshindex;
   }

   public int getEmitter() {
      return this.getTemplate().emmiter;
   }

   public boolean isWall() {
      return this.getTemplate().isWall;
   }

   public String getGroupName() {
      return this.getTemplate().groupName;
   }

   public int getChildId() {
      return this.getTemplate().childDoorId;
   }

   @Override
   public void reduceCurrentHp(double damage, Creature attacker, boolean awake, boolean isDOT, Skill skill) {
      if (!this.isWall() || attacker instanceof SiegeSummonInstance) {
         super.reduceCurrentHp(damage, attacker, awake, isDOT, skill);
      }
   }

   @Override
   public void reduceCurrentHpByDOT(double i, Creature attacker, Skill skill) {
   }

   @Override
   protected void onDeath(Creature killer) {
      this._openLock.lock();

      try {
         this.setGeoOpen(true);
      } finally {
         this._openLock.unlock();
      }

      boolean isFort = this.getFort() != null && this.getFort().getId() > 0 && this.getFort().getSiege().getIsInProgress();
      boolean isCastle = this.getCastle() != null && this.getCastle().getId() > 0 && this.getCastle().getSiege().getIsInProgress();
      boolean isHall = this.getClanHall() != null && this.getClanHall().isSiegableHall() && ((SiegableHall)this.getClanHall()).isInSiege();
      if (isFort || isCastle || isHall) {
         this.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.CASTLE_GATE_BROKEN_DOWN));
      }

      super.onDeath(killer);
   }

   @Override
   public void onSpawn() {
      super.onSpawn();
      this._openLock.lock();

      try {
         if (!this.isOpen()) {
            this.setGeoOpen(false);
         }
      } finally {
         this._openLock.unlock();
      }
   }

   @Override
   public void sendInfo(Player activeChar) {
      if (this.isVisibleFor(activeChar)) {
         if (this.getEmitter() > 0) {
            activeChar.sendPacket(new EventTrigger(this, this.getOpen()));
         }

         activeChar.sendPacket(new StaticObject(this, activeChar));
      }
   }

   public void setTargetable(boolean b) {
      this._isTargetable = b;
      this.broadcastStatusUpdate();
   }

   @Override
   public boolean isTargetable() {
      return this._isTargetable;
   }

   public boolean checkCollision() {
      return this._checkCollision;
   }

   private DoorInstance getSiblingDoor(int doorId) {
      if (this.getReflectionId() == 0) {
         return DoorParser.getInstance().getDoor(doorId);
      } else {
         Reflection inst = ReflectionManager.getInstance().getReflection(this.getReflectionId());
         return inst != null ? inst.getDoor(doorId) : null;
      }
   }

   private void startAutoCloseTask() {
      if (this._closeTime >= 0 && !this.isOpenableByTime()) {
         Future<?> oldTask = this._autoCloseTask;
         if (oldTask != null) {
            this._autoCloseTask = null;
            oldTask.cancel(false);
         }

         this._autoCloseTask = ThreadPoolManager.getInstance().schedule(new DoorInstance.AutoClose(), (long)(this._closeTime * 1000));
      }
   }

   @Override
   public boolean isDoor() {
      return true;
   }

   @Override
   public boolean isConcrete() {
      return true;
   }

   @Override
   public Shape getShape() {
      return this.getTemplate().getPolygon();
   }

   @Override
   public byte[][] getGeoAround() {
      return this._geoAround;
   }

   @Override
   public void setGeoAround(byte[][] geo) {
      this._geoAround = geo;
   }

   protected boolean setGeoOpen(boolean open) {
      if (this._geoOpen == open) {
         return false;
      } else {
         this._geoOpen = open;
         if (Config.GEODATA) {
            if (open) {
               if (this.getId() != 20250777 && this.getId() != 20250778) {
                  GeoEngine.removeGeoCollision(this, this.getGeoIndex());
               }
            } else if (this.getId() != 20250777 && this.getId() != 20250778) {
               GeoEngine.applyGeoCollision(this, this.getGeoIndex());
            }
         }

         return this._geoOpen;
      }
   }

   public boolean getIsAttackableDoor() {
      return this._isAttackableDoor;
   }

   @Override
   public boolean canBeAttacked() {
      return this._isAttackableDoor;
   }

   public void setIsAttackableDoor(boolean val) {
      this._isAttackableDoor = val;
   }

   public boolean isOpen() {
      return this._open;
   }

   @Override
   public boolean isHealBlocked() {
      return true;
   }

   @Override
   public boolean isActionsDisabled() {
      return true;
   }

   class AutoClose implements Runnable {
      @Override
      public void run() {
         if (DoorInstance.this.getOpen()) {
            DoorInstance.this.closeMe();
         }
      }
   }

   class TimerOpen implements Runnable {
      @Override
      public void run() {
         boolean open = DoorInstance.this.getOpen();
         if (open) {
            DoorInstance.this.closeMe();
         } else {
            DoorInstance.this.openMe();
         }

         int delay = open ? DoorInstance.this._closeTime : DoorInstance.this._openTime;
         if (DoorInstance.this._randomTime > 0) {
            delay += Rnd.get(DoorInstance.this._randomTime);
         }

         ThreadPoolManager.getInstance().schedule(this, (long)(delay * 1000));
      }
   }
}
