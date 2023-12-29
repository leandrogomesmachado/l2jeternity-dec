package l2e.gameserver.model.actor.instance;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import l2e.commons.geometry.Point3D;
import l2e.commons.util.Rnd;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.SkillType;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.network.serverpackets.NpcInfo;
import l2e.gameserver.network.serverpackets.SocialAction;
import l2e.gameserver.network.serverpackets.StopMove;

public final class TamedBeastInstance extends FeedableBeastInstance {
   private int _foodSkillId;
   private static final int MAX_DISTANCE_FROM_HOME = 30000;
   private static final int MAX_DISTANCE_FROM_OWNER = 2000;
   private static final int MAX_DURATION = 1200000;
   private static final int DURATION_CHECK_INTERVAL = 60000;
   private static final int DURATION_INCREASE_INTERVAL = 20000;
   private static final int BUFF_INTERVAL = 5000;
   private int _remainingTime = 1200000;
   private int _homeX;
   private int _homeY;
   private int _homeZ;
   protected Player _owner;
   private Future<?> _buffTask = null;
   private Future<?> _durationCheckTask = null;
   protected boolean _isFreyaBeast;
   private List<Skill> _beastSkills = null;

   public TamedBeastInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.TamedBeastInstance);
      this.setHome(this);
   }

   public TamedBeastInstance(int objectId, NpcTemplate template, Player owner, int foodSkillId, int x, int y, int z) {
      super(objectId, template);
      this._isFreyaBeast = false;
      this.setInstanceType(GameObject.InstanceType.TamedBeastInstance);
      this.setCurrentHp(this.getMaxHp());
      this.setCurrentMp(this.getMaxMp());
      this.setOwner(owner);
      this.setFoodType(foodSkillId);
      this.setHome(x, y, z);
      this.spawnMe(x, y, z);
   }

   public TamedBeastInstance(int objectId, NpcTemplate template, Player owner, int food, int x, int y, int z, boolean isFreyaBeast) {
      super(objectId, template);
      this._isFreyaBeast = isFreyaBeast;
      this.setInstanceType(GameObject.InstanceType.TamedBeastInstance);
      this.setCurrentHp(this.getMaxHp());
      this.setCurrentMp(this.getMaxMp());
      this.setFoodType(food);
      this.setHome(x, y, z);
      this.spawnMe(x, y, z);
      this.setOwner(owner);
      if (isFreyaBeast) {
         this.getAI().setIntention(CtrlIntention.FOLLOW, this._owner);
      }
   }

   public void onReceiveFood() {
      this._remainingTime += 20000;
      if (this._remainingTime > 1200000) {
         this._remainingTime = 1200000;
      }
   }

   public Point3D getHome() {
      return new Point3D(this._homeX, this._homeY, this._homeZ);
   }

   public void setHome(int x, int y, int z) {
      this._homeX = x;
      this._homeY = y;
      this._homeZ = z;
   }

   public void setHome(Creature c) {
      this.setHome(c.getX(), c.getY(), c.getZ());
   }

   public int getRemainingTime() {
      return this._remainingTime;
   }

   public void setRemainingTime(int duration) {
      this._remainingTime = duration;
   }

   public int getFoodType() {
      return this._foodSkillId;
   }

   public void setFoodType(int foodItemId) {
      if (foodItemId > 0) {
         this._foodSkillId = foodItemId;
         if (this._durationCheckTask != null) {
            this._durationCheckTask.cancel(true);
         }

         this._durationCheckTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new TamedBeastInstance.CheckDuration(this), 60000L, 60000L);
      }
   }

   @Override
   protected void onDeath(Creature killer) {
      this.getAI().stopFollow();
      if (this._buffTask != null) {
         this._buffTask.cancel(true);
      }

      if (this._durationCheckTask != null) {
         this._durationCheckTask.cancel(true);
      }

      if (this._owner != null && this._owner.getTrainedBeasts() != null) {
         this._owner.getTrainedBeasts().remove(this);
      }

      this._buffTask = null;
      this._durationCheckTask = null;
      this._owner = null;
      this._foodSkillId = 0;
      this._remainingTime = 0;
      super.onDeath(killer);
   }

   @Override
   public boolean isAutoAttackable(Creature attacker) {
      return !this._isFreyaBeast;
   }

   public boolean isFreyaBeast() {
      return this._isFreyaBeast;
   }

   public void addBeastSkill(Skill skill) {
      if (this._beastSkills == null) {
         this._beastSkills = new CopyOnWriteArrayList<>();
      }

      this._beastSkills.add(skill);
   }

   public void castBeastSkills() {
      if (this._owner != null && this._beastSkills != null) {
         int delay = 100;

         for(Skill skill : this._beastSkills) {
            ThreadPoolManager.getInstance().schedule(new TamedBeastInstance.buffCast(skill), (long)delay);
            delay += 100 + skill.getHitTime();
         }

         ThreadPoolManager.getInstance().schedule(new TamedBeastInstance.buffCast(null), (long)delay);
      }
   }

   public Player getOwner() {
      return this._owner;
   }

   public void setOwner(Player owner) {
      if (owner != null) {
         this._owner = owner;
         this.setTitle(owner.getName());
         this.setShowSummonAnimation(true);
         this.broadcastPacket(new NpcInfo.Info(this, owner));
         owner.addTrainedBeast(this);
         this.getAI().startFollow(this._owner, 100);
         if (!this._isFreyaBeast) {
            int totalBuffsAvailable = 0;

            for(Skill skill : this.getTemplate().getSkills().values()) {
               if (skill.getSkillType() == SkillType.BUFF) {
                  ++totalBuffsAvailable;
               }
            }

            if (this._buffTask != null) {
               this._buffTask.cancel(true);
            }

            this._buffTask = ThreadPoolManager.getInstance()
               .scheduleAtFixedRate(new TamedBeastInstance.CheckOwnerBuffs(this, totalBuffsAvailable), 5000L, 5000L);
         }
      } else {
         this.deleteMe();
      }
   }

   public boolean isTooFarFromHome() {
      return !this.isInsideRadius(this._homeX, this._homeY, this._homeZ, 30000, true, true);
   }

   @Override
   public void deleteMe() {
      if (this._buffTask != null) {
         this._buffTask.cancel(true);
      }

      if (this._durationCheckTask != null) {
         this._durationCheckTask.cancel(true);
      }

      this.stopHpMpRegeneration();
      if (this._owner != null && this._owner.getTrainedBeasts() != null) {
         this._owner.getTrainedBeasts().remove(this);
      }

      this.setTarget(null);
      this._buffTask = null;
      this._durationCheckTask = null;
      this._owner = null;
      this._foodSkillId = 0;
      this._remainingTime = 0;
      super.deleteMe();
   }

   public void onOwnerGotAttacked(Creature attacker) {
      if (this._owner == null || !this._owner.isOnline()) {
         this.deleteMe();
      } else if (!this._owner.isInsideRadius(this, 2000, true, true)) {
         this.getAI().startFollow(this._owner);
      } else if (!this._owner.isDead() && !this._isFreyaBeast) {
         if (!this.isCastingNow()) {
            double HPRatio = this._owner.getCurrentHp() / this._owner.getMaxHp();
            if (HPRatio >= 0.8) {
               for(Skill skill : this.getTemplate().getSkills().values()) {
                  if (skill.getSkillType() == SkillType.DEBUFF && Rnd.get(3) < 1 && attacker != null && attacker.getFirstEffect(skill) != null) {
                     this.sitCastAndFollow(skill, attacker);
                  }
               }
            } else if (HPRatio < 0.5) {
               int chance = 1;
               if (HPRatio < 0.25) {
                  chance = 2;
               }

               for(Skill skill : this.getTemplate().getSkills().values()) {
                  if (Rnd.get(5) < chance
                     && skill.hasEffectType(
                        EffectType.CPHEAL, EffectType.HEAL, EffectType.HEAL_PERCENT, EffectType.MANAHEAL_BY_LEVEL, EffectType.MANAHEAL_PERCENT
                     )) {
                     this.sitCastAndFollow(skill, this._owner);
                  }
               }
            }
         }
      }
   }

   protected void sitCastAndFollow(Skill skill, Creature target) {
      this.stopMove(null);
      this.broadcastPacket(new StopMove(this));
      this.getAI().setIntention(CtrlIntention.IDLE);
      this.setTarget(target);
      this.doCast(skill);
      this.getAI().setIntention(CtrlIntention.FOLLOW, this._owner);
   }

   @Override
   public void onAction(Player player, boolean interact) {
      if (player != null && this.canTarget(player)) {
         if (this != player.getTarget()) {
            player.setTarget(this);
         } else if (interact) {
            if (this.isAutoAttackable(player) && Math.abs(player.getZ() - this.getZ()) < 100) {
               player.getAI().setIntention(CtrlIntention.ATTACK, this);
            } else {
               player.sendActionFailed();
            }
         }
      }
   }

   private static class CheckDuration implements Runnable {
      private final TamedBeastInstance _tamedBeast;

      CheckDuration(TamedBeastInstance tamedBeast) {
         this._tamedBeast = tamedBeast;
      }

      @Override
      public void run() {
         int foodTypeSkillId = this._tamedBeast.getFoodType();
         Player owner = this._tamedBeast.getOwner();
         ItemInstance item = null;
         if (this._tamedBeast._isFreyaBeast) {
            item = owner.getInventory().getItemByItemId(foodTypeSkillId);
            if (item != null && item.getCount() >= 1L) {
               owner.destroyItem("BeastMob", item, 1L, this._tamedBeast, true);
               this._tamedBeast.broadcastPacket(new SocialAction(this._tamedBeast.getObjectId(), 3));
            } else {
               this._tamedBeast.deleteMe();
            }
         } else {
            this._tamedBeast.setRemainingTime(this._tamedBeast.getRemainingTime() - 60000);
            if (foodTypeSkillId == 2188) {
               item = owner.getInventory().getItemByItemId(6643);
            } else if (foodTypeSkillId == 2189) {
               item = owner.getInventory().getItemByItemId(6644);
            }

            if (item != null && item.getCount() >= 1L) {
               GameObject oldTarget = owner.getTarget();
               owner.setTarget(this._tamedBeast);
               GameObject[] targets = new GameObject[]{this._tamedBeast};
               owner.callSkill(SkillsParser.getInstance().getInfo(foodTypeSkillId, 1), targets);
               owner.setTarget(oldTarget);
            } else if (this._tamedBeast.getRemainingTime() < 900000) {
               this._tamedBeast.setRemainingTime(-1);
            }

            if (this._tamedBeast.getRemainingTime() <= 0) {
               this._tamedBeast.deleteMe();
            }
         }
      }
   }

   private class CheckOwnerBuffs implements Runnable {
      private final TamedBeastInstance _tamedBeast;
      private final int _numBuffs;

      CheckOwnerBuffs(TamedBeastInstance tamedBeast, int numBuffs) {
         this._tamedBeast = tamedBeast;
         this._numBuffs = numBuffs;
      }

      @Override
      public void run() {
         Player owner = this._tamedBeast.getOwner();
         if (owner != null && owner.isOnline()) {
            if (!TamedBeastInstance.this.isInsideRadius(owner, 2000, true, true)) {
               TamedBeastInstance.this.getAI().startFollow(owner);
            } else if (!owner.isDead()) {
               if (!TamedBeastInstance.this.isCastingNow()) {
                  int totalBuffsOnOwner = 0;
                  int i = 0;
                  int rand = Rnd.get(this._numBuffs);
                  Skill buffToGive = null;

                  for(Skill skill : this._tamedBeast.getTemplate().getSkills().values()) {
                     if (skill.getSkillType() == SkillType.BUFF) {
                        if (i++ == rand) {
                           buffToGive = skill;
                        }

                        if (owner.getFirstEffect(skill) != null) {
                           ++totalBuffsOnOwner;
                        }
                     }
                  }

                  if (this._numBuffs * 2 / 3 > totalBuffsOnOwner) {
                     this._tamedBeast.sitCastAndFollow(buffToGive, owner);
                  }

                  TamedBeastInstance.this.getAI().setIntention(CtrlIntention.FOLLOW, this._tamedBeast.getOwner());
               }
            }
         } else {
            TamedBeastInstance.this.deleteMe();
         }
      }
   }

   private class buffCast implements Runnable {
      private final Skill _skill;

      public buffCast(Skill skill) {
         this._skill = skill;
      }

      @Override
      public void run() {
         if (this._skill == null) {
            TamedBeastInstance.this.getAI().setIntention(CtrlIntention.FOLLOW, TamedBeastInstance.this._owner);
         } else {
            TamedBeastInstance.this.sitCastAndFollow(this._skill, TamedBeastInstance.this._owner);
         }
      }
   }
}
