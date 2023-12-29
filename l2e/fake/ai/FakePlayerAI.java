package l2e.fake.ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import l2e.commons.threading.RunnableImpl;
import l2e.commons.util.Rnd;
import l2e.fake.FakePlayer;
import l2e.fake.FakePlayerManager;
import l2e.fake.FakePoolManager;
import l2e.fake.model.FakeSupport;
import l2e.fake.model.SupportSpell;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.instancemanager.MapRegionManager;
import l2e.gameserver.instancemanager.TownManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.TeleportWhereType;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.DoorInstance;
import l2e.gameserver.model.actor.instance.TreasureChestInstance;
import l2e.gameserver.model.actor.templates.player.FakeLocTemplate;
import l2e.gameserver.model.actor.templates.player.FakePassiveLocTemplate;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.skills.targets.TargetType;
import l2e.gameserver.model.zone.type.TownZone;
import l2e.gameserver.network.serverpackets.FinishRotatings;
import l2e.gameserver.network.serverpackets.MoveToLocation;
import l2e.gameserver.network.serverpackets.MoveToPawn;
import l2e.gameserver.network.serverpackets.StopMove;
import l2e.gameserver.network.serverpackets.TeleportToLocation;

public abstract class FakePlayerAI extends RunnableImpl {
   protected final FakePlayer _fakePlayer;
   protected volatile boolean _clientMoving;
   protected volatile boolean _clientAutoAttacking;
   private long _moveToPawnTimeout;
   protected int _clientMovingToPawnOffset;
   protected boolean _isBusyThinking = false;
   protected int _iterationsOnDeath = 0;
   private final int _toVillageIterationsOnDeath = 10;
   private ScheduledFuture<?> _actionTask;
   protected ScheduledFuture<?> _removeTask;
   private long _buffTime = 0L;
   protected long _idleTime = 0L;
   protected long _shotsTime = 0L;
   protected long _spiritOreTime = 0L;
   private long _arrowTime = 0L;
   protected long _sitTime = 0L;
   private boolean _isPeaceLocation = false;
   private boolean _isWantToFarm = false;
   private boolean _isTargetLock = false;

   public FakePlayerAI(FakePlayer character, boolean isPassive) {
      this._fakePlayer = character;
      this.setup();
      if (!isPassive) {
         this.checkFakeLocation();
         this.applyDefaultBuffs();
         if (this._fakePlayer.getClassId() == ClassId.shillienTemplar) {
            this.selfCubicBuffs();
         }

         this.startActionTask();
      } else {
         FakePassiveLocTemplate loc = this._fakePlayer.getFakeTerritory();
         if (loc != null) {
            this._removeTask = FakePoolManager.getInstance().schedule(new FakePlayerAI.RemoveTask(), Rnd.get(loc.getMinDelay(), loc.getMaxDelay()) * 1000L);
         }
      }
   }

   private void checkFakeLocation() {
      Location loc = this._fakePlayer.getFakeLocation().getLocation();
      TownZone zone = TownManager.getTown(loc.getX(), loc.getY(), loc.getZ());
      this._isPeaceLocation = zone != null;
   }

   @Override
   public void runImpl() throws Exception {
      if (this._fakePlayer != null && !this._fakePlayer.getFakeAi().isBusyThinking()) {
         this.applyDefaultBuffs();
         this.thinkAndAct();
      }
   }

   public void setup() {
      this._fakePlayer.setIsRunning(true);
   }

   protected void applyDefaultBuffs() {
      if (this._buffTime <= System.currentTimeMillis()) {
         if (this._fakePlayer.getLevel() > 8) {
            for(int[] buff : this.getBuffs()) {
               try {
                  Map<Integer, Effect> activeEffects = Arrays.stream(this._fakePlayer.getAllEffects())
                     .filter(x -> x.getEffectType() == EffectType.BUFF)
                     .collect(Collectors.toMap(x -> x.getSkill().getId(), x -> x));
                  if (!activeEffects.containsKey(buff[0])) {
                     SkillsParser.getInstance().getInfo(buff[0], buff[1]).getEffects(this._fakePlayer, this._fakePlayer, false);
                  } else if (activeEffects.get(buff[0]).getAbnormalTime() - activeEffects.get(buff[0]).getTime() <= 20) {
                     SkillsParser.getInstance().getInfo(buff[0], buff[1]).getEffects(this._fakePlayer, this._fakePlayer, false);
                  }
               } catch (Exception var6) {
                  var6.printStackTrace();
               }
            }
         }

         this._buffTime = System.currentTimeMillis() + 1200000L;
      }
   }

   protected void handleDeath() {
      if (this._fakePlayer.isDead()) {
         if (this._iterationsOnDeath >= 10) {
            this.toVillageOnDeath();
         }

         ++this._iterationsOnDeath;
      } else {
         this._iterationsOnDeath = 0;
      }
   }

   public void setBusyThinking(boolean thinking) {
      this._isBusyThinking = thinking;
   }

   public boolean isBusyThinking() {
      return this._isBusyThinking;
   }

   protected void teleportToLocation(int x, int y, int z, int randomOffset) {
      this._fakePlayer.stopMove(null);
      this._fakePlayer.abortAttack();
      this._fakePlayer.abortCast();
      this._fakePlayer.setIsTeleporting(true);
      this._fakePlayer.setTarget(null);
      this._fakePlayer.getAI().setIntention(CtrlIntention.ACTIVE);
      if (randomOffset > 0) {
         x += Rnd.get(-randomOffset, randomOffset);
         y += Rnd.get(-randomOffset, randomOffset);
      }

      z += 5;
      this._fakePlayer.broadcastPacket(new TeleportToLocation(this._fakePlayer, x, y, z, 0));
      this._fakePlayer.decayMe();
      this._fakePlayer.setXYZ(x, y, z);
      this._fakePlayer.onTeleported();
      this._fakePlayer.revalidateZone(true);
      this._buffTime = 0L;
      this.applyDefaultBuffs();
      this.applyDefaultItems(this._fakePlayer);
      this._fakePlayer.heal();
      if (this._fakePlayer.getClassId() == ClassId.shillienTemplar) {
         this.selfCubicBuffs();
      }
   }

   public void rndShortWalk() {
      int posX = this._fakePlayer.getX();
      int posY = this._fakePlayer.getY();
      int posZ = this._fakePlayer.getZ();
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

      this._fakePlayer.setRunning();
      this._fakePlayer.getAI().setIntention(CtrlIntention.MOVING, new Location(posX, posY, posZ));
   }

   protected GameObject tryTargetRandomCreatureByTypeInRadius(int radius, Function<Creature, Boolean> condition) {
      GameObject target = this._fakePlayer.getTarget();
      if (target != null && ((Creature)target).isDead()) {
         this._fakePlayer.setTarget(null);
         target = null;
      }

      if (target == null) {
         List<Creature> result = new ArrayList<>();

         for(Creature obj : World.getInstance().getAroundCharacters(this._fakePlayer, radius, 300)) {
            if (obj.isPlayer()) {
               Player pl = (Player)obj;
               boolean canAttack = this._fakePlayer.getLevel() >= pl.getLevel() || pl.getLevel() - this._fakePlayer.getLevel() <= 5;
               if (pl.getPvpFlag() > 0 && this._fakePlayer.getDistance(pl) < 1000.0 && canAttack && condition.apply(pl)) {
                  result.add(pl);
                  break;
               }
            }

            if (obj instanceof Attackable) {
               Attackable npc = (Attackable)obj;
               if (npc.isMonster()
                  && !npc.isDead()
                  && npc.isVisible()
                  && !(npc instanceof TreasureChestInstance)
                  && !npc.isRaid()
                  && !npc.isRaidMinion()
                  && condition.apply(npc)
                  && npc.hasAI()
                  && (npc.getAI().getTargetList().isEmpty() || npc.getAI().getTargetList().contains(this._fakePlayer))) {
                  result.add(npc);
               }
            }
         }

         if (!result.isEmpty()) {
            GameObject closestTarget = result.stream()
               .min((o1, o2) -> Integer.compare((int)Math.sqrt(this._fakePlayer.getDistanceSq(o1)), (int)Math.sqrt(this._fakePlayer.getDistanceSq(o2))))
               .get();
            result.clear();
            return closestTarget;
         }
      } else if (target.isPlayer() && (target.getActingPlayer().isDead() || target.getActingPlayer().getPvpFlag() == 0)) {
         this._fakePlayer.setTarget(null);
         target = null;
      }

      return target;
   }

   public boolean castSpell(Skill skill) {
      if (!this._fakePlayer.isCastingNow()) {
         if (skill.getTargetType() == TargetType.GROUND) {
            if (this.maybeMoveToPosition(this._fakePlayer.getCurrentSkillWorldPosition(), this._fakePlayer.getMagicalAttackRange(skill))) {
               this._fakePlayer.setIsCastingNow(false);
               return false;
            }
         } else {
            if (this.checkTargetLost(this._fakePlayer.getTarget())) {
               if (skill.isOffensive() && this._fakePlayer.getTarget() != null) {
                  this._fakePlayer.setTarget(null);
               }

               this._fakePlayer.setIsCastingNow(false);
               return false;
            }

            if (this._fakePlayer.getTarget() != null && this.maybeMoveToPawn(this._fakePlayer.getTarget(), this._fakePlayer.getMagicalAttackRange(skill))) {
               this._fakePlayer.setIsCastingNow(false);
               return false;
            }

            if (this._fakePlayer.isSkillDisabled(skill)) {
               return false;
            }
         }

         if (skill.getHitTime() > 50 && !skill.isSimultaneousCast()) {
            this.clientStopMoving(null);
         }

         this._fakePlayer.doCast(skill);
         return true;
      } else {
         return false;
      }
   }

   protected void castSelfSpell(Skill skill) {
      if (!this._fakePlayer.isCastingNow() && !this._fakePlayer.isSkillDisabled(skill)) {
         if (skill.getHitTime() > 50 && !skill.isSimultaneousCast()) {
            this.clientStopMoving(null);
         }

         this._fakePlayer.doCast(skill);
      }
   }

   protected void toVillageOnDeath() {
      Location location = MapRegionManager.getInstance().getTeleToLocation(this._fakePlayer, TeleportWhereType.TOWN);
      if (this._fakePlayer.isDead()) {
         this._fakePlayer.doRevive();
      }

      FakeLocTemplate template = this._fakePlayer.getFakeLocation();
      if (this._fakePlayer.getLevel() >= template.getMaxLvl()) {
         FakeSupport.setLevel(this._fakePlayer, template.getMinLvl());
      }

      this._fakePlayer.getFakeAi().teleportToLocation(location.getX(), location.getY(), location.getZ(), 20);
   }

   protected void clientStopMoving(Location loc) {
      if (this._fakePlayer.isMoving()) {
         this._fakePlayer.stopMove(loc);
      }

      this._clientMovingToPawnOffset = 0;
      if (this._clientMoving || loc != null) {
         this._clientMoving = false;
         this._fakePlayer.broadcastPacket(new StopMove(this._fakePlayer));
         if (loc != null) {
            this._fakePlayer.broadcastPacket(new FinishRotatings(this._fakePlayer.getObjectId(), loc.getHeading(), 0));
         }
      }
   }

   protected boolean checkTargetLost(GameObject target) {
      if (target instanceof Player) {
         Player victim = (Player)target;
         if (victim.isFakeDeath()) {
            victim.stopFakeDeath(true);
            return false;
         }
      }

      if (target == null) {
         this._fakePlayer.getAI().setIntention(CtrlIntention.ACTIVE);
         return true;
      } else {
         return false;
      }
   }

   protected boolean maybeMoveToPosition(Location worldPosition, int offset) {
      if (worldPosition == null) {
         return false;
      } else if (offset < 0) {
         return false;
      } else if (!this._fakePlayer.isInsideRadius(worldPosition.getX(), worldPosition.getY(), (int)((double)offset + this._fakePlayer.getColRadius()), false)) {
         if (this._fakePlayer.isMovementDisabled()) {
            return true;
         } else {
            int x = this._fakePlayer.getX();
            int y = this._fakePlayer.getY();
            double dx = (double)(worldPosition.getX() - x);
            double dy = (double)(worldPosition.getY() - y);
            double dist = Math.sqrt(dx * dx + dy * dy);
            double sin = dy / dist;
            double cos = dx / dist;
            dist -= (double)(offset - 5);
            x += (int)(dist * cos);
            y += (int)(dist * sin);
            this.moveTo(x, y, worldPosition.getZ());
            return true;
         }
      } else {
         return false;
      }
   }

   protected void moveToPawn(GameObject pawn, int offset) {
      if (!this._fakePlayer.isMovementDisabled()) {
         if (offset < 10) {
            offset = 10;
         }

         boolean sendPacket = true;
         if (this._clientMoving && this._fakePlayer.getTarget() == pawn) {
            if (this._clientMovingToPawnOffset == offset) {
               if (System.currentTimeMillis() < this._moveToPawnTimeout) {
                  return;
               }

               sendPacket = false;
            } else if (this._fakePlayer.isOnGeodataPath() && System.currentTimeMillis() < this._moveToPawnTimeout + 1000L) {
               return;
            }
         }

         this._clientMoving = true;
         this._clientMovingToPawnOffset = offset;
         this._fakePlayer.setTarget(pawn);
         this._moveToPawnTimeout = System.currentTimeMillis() + 1000L;
         if (pawn == null) {
            return;
         }

         this._fakePlayer.moveToLocation(pawn.getX(), pawn.getY(), pawn.getZ(), offset);
         if (!this._fakePlayer.isMoving()) {
            return;
         }

         if (pawn instanceof Creature) {
            if (this._fakePlayer.isOnGeodataPath()) {
               this._fakePlayer.broadcastPacket(new MoveToLocation(this._fakePlayer));
               this._clientMovingToPawnOffset = 0;
            } else if (sendPacket) {
               this._fakePlayer.broadcastPacket(new MoveToPawn(this._fakePlayer, (Creature)pawn, offset));
            }
         } else {
            this._fakePlayer.broadcastPacket(new MoveToLocation(this._fakePlayer));
         }
      }
   }

   public void moveTo(int x, int y, int z) {
      if (!this._fakePlayer.isMovementDisabled()) {
         this._clientMoving = true;
         this._clientMovingToPawnOffset = 0;
         this._fakePlayer.moveToLocation(x, y, z, 0);
         this._fakePlayer.broadcastPacket(new MoveToLocation(this._fakePlayer));
      }
   }

   protected boolean maybeMoveToPawn(GameObject target, int offset) {
      if (target != null && offset >= 0) {
         offset = (int)((double)offset + this._fakePlayer.getColRadius());
         if (target instanceof Creature) {
            offset += ((Creature)target).getTemplate().getCollisionRadius();
         }

         if (this._fakePlayer.isInsideRadius(target, offset, false, false)) {
            if (!GeoEngine.canSeeTarget(this._fakePlayer, this._fakePlayer.getTarget(), false)) {
               this._fakePlayer.setIsCastingNow(false);
               this.moveToPawn(target, 50);
               return true;
            } else {
               return false;
            }
         } else if (this._fakePlayer.isMovementDisabled()) {
            if (this._fakePlayer.getAI().getIntention() == CtrlIntention.ATTACK) {
               this._fakePlayer.getAI().setIntention(CtrlIntention.IDLE);
            }

            return true;
         } else {
            if (target instanceof Creature && !(target instanceof DoorInstance)) {
               if (((Creature)target).isMoving()) {
                  offset -= 30;
               }

               if (offset < 5) {
                  offset = 5;
               }
            }

            this.moveToPawn(target, offset);
            return true;
         }
      } else {
         return false;
      }
   }

   protected void applyDefaultItems(FakePlayer fakePlayer) {
      if (fakePlayer.getInventory().getItemByItemId(1539) != null) {
         if (fakePlayer.getInventory().getItemByItemId(1539).getCount() <= 10L) {
            fakePlayer.getInventory().addItem("", 1539, 100L, this._fakePlayer, null);
         }
      } else {
         fakePlayer.getInventory().addItem("", 1539, 100L, this._fakePlayer, null);
      }

      if (fakePlayer.getInventory().getItemByItemId(728) != null) {
         if (fakePlayer.getInventory().getItemByItemId(1539).getCount() <= 5L) {
            fakePlayer.getInventory().addItem("", 728, 50L, this._fakePlayer, null);
         }
      } else {
         fakePlayer.getInventory().addItem("", 728, 50L, this._fakePlayer, null);
      }
   }

   protected void selfCubicBuffs() {
      List<SupportSpell> _cubics = new ArrayList<>();
      _cubics.add(new SupportSpell(33, 1));
      _cubics.add(new SupportSpell(22, 1));
      _cubics.add(new SupportSpell(278, 1));
      SupportSpell rndSkill = _cubics.get(Rnd.get(_cubics.size()));
      Skill skill = SkillsParser.getInstance().getInfo(rndSkill.getSkillId(), this._fakePlayer.getSkillLevel(rndSkill.getSkillId()));
      this.castSelfSpell(skill);
   }

   public abstract void thinkAndAct();

   protected abstract int[][] getBuffs();

   private synchronized void startActionTask() {
      if (this._actionTask == null) {
         this._actionTask = FakePoolManager.getInstance().scheduleAtFixedDelay(this, 500L, 500L);
      }
   }

   protected synchronized void stopActionTask() {
      if (this._actionTask != null) {
         this._actionTask.cancel(true);
         this._actionTask = null;
      }
   }

   protected synchronized void stopRemoveTask() {
      if (this._removeTask != null) {
         this._removeTask.cancel(true);
         this._removeTask = null;
      }
   }

   public long getArrowTime() {
      return this._arrowTime;
   }

   public void setArrowTime(long time) {
      this._arrowTime = time;
   }

   public boolean isTownZone() {
      return this._isPeaceLocation;
   }

   public boolean isWantToFarm() {
      return this._isWantToFarm;
   }

   public void setWantToFarm(boolean isWant) {
      this._isWantToFarm = isWant;
   }

   public boolean isTargetLock() {
      return this._isTargetLock;
   }

   public void setTargetLock(boolean isWant) {
      this._isTargetLock = isWant;
   }

   private class RemoveTask extends RunnableImpl {
      private RemoveTask() {
      }

      @Override
      public void runImpl() {
         if (FakePlayerAI.this._fakePlayer != null) {
            FakePassiveLocTemplate loc = FakePlayerAI.this._fakePlayer.getFakeTerritory();
            if (loc != null) {
               FakePlayerAI.this._fakePlayer.despawnPlayer();
               FakePlayerAI.this.stopRemoveTask();
               loc.setCurrentAmount(loc.getCurrentAmount() - 1);
               FakePlayerManager.getInstance().respawnPassivePlayer(Rnd.get(loc.getMinRespawn(), loc.getMaxRespawn()));
            }
         }
      }
   }
}
