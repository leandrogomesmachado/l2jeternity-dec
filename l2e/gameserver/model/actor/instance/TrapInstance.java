package l2e.gameserver.model.actor.instance;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.parser.DamageLimitParser;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.tasks.npc.trap.TrapTask;
import l2e.gameserver.model.actor.tasks.npc.trap.TrapTriggerTask;
import l2e.gameserver.model.actor.templates.items.Weapon;
import l2e.gameserver.model.actor.templates.npc.DamageLimit;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.olympiad.OlympiadGameManager;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.GameServerPacket;
import l2e.gameserver.network.serverpackets.NpcInfo;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.taskmanager.DecayTaskManager;

public final class TrapInstance extends Npc {
   private static final int TICK = 1000;
   private boolean _hasLifeTime;
   private boolean _isInArena = false;
   private boolean _isTriggered;
   private final int _lifeTime;
   private Player _owner;
   private final List<Integer> _playersWhoDetectedMe = new ArrayList<>();
   private Skill _skill;
   private int _remainingTime;

   public TrapInstance(int objectId, NpcTemplate template, int instanceId, int lifeTime) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.TrapInstance);
      this.setReflectionId(instanceId);
      this.setName(template.getName());
      this.setIsInvul(false);
      this._owner = null;
      this._isTriggered = false;

      for(Skill skill : template.getSkills().values()) {
         if (skill.getId() == 4072
            || skill.getId() == 4186
            || skill.getId() == 5267
            || skill.getId() == 5268
            || skill.getId() == 5269
            || skill.getId() == 5270
            || skill.getId() == 5271
            || skill.getId() == 5340
            || skill.getId() == 5422
            || skill.getId() == 5423
            || skill.getId() == 5424
            || skill.getId() == 5679) {
            this._skill = skill;
            break;
         }
      }

      this._hasLifeTime = lifeTime >= 0;
      this._lifeTime = lifeTime != 0 ? lifeTime : 30000;
      this._remainingTime = this._lifeTime;
      if (this._skill != null) {
         ThreadPoolManager.getInstance().schedule(new TrapTask(this), 1000L);
      }
   }

   public TrapInstance(int objectId, NpcTemplate template, Player owner, int lifeTime) {
      this(objectId, template, owner.getReflectionId(), lifeTime);
      this._owner = owner;
   }

   @Override
   public void broadcastPacket(GameServerPacket mov) {
      for(Player player : World.getInstance().getAroundPlayers(this)) {
         if (player != null && (this._isTriggered || this.canBeSeen(player))) {
            player.sendPacket(mov);
         }
      }
   }

   @Override
   public void broadcastPacket(GameServerPacket mov, int radiusInKnownlist) {
      for(Player player : World.getInstance().getAroundPlayers(this)) {
         if (player != null && this.isInsideRadius(player, radiusInKnownlist, false, false) && (this._isTriggered || this.canBeSeen(player))) {
            player.sendPacket(mov);
         }
      }
   }

   public boolean canBeSeen(Creature cha) {
      if (cha != null && this._playersWhoDetectedMe.contains(cha.getObjectId())) {
         return true;
      } else if (this._owner == null || cha == null) {
         return false;
      } else if (cha == this._owner) {
         return true;
      } else {
         if (cha.isPlayer()) {
            if (((Player)cha).inObserverMode()) {
               return false;
            }

            if (this._owner.isInOlympiadMode() && ((Player)cha).isInOlympiadMode() && ((Player)cha).getOlympiadSide() != this._owner.getOlympiadSide()) {
               return false;
            }
         }

         if (this._isInArena) {
            return true;
         } else {
            return this._owner.isInParty() && cha.isInParty() && this._owner.getParty().getLeaderObjectId() == cha.getParty().getLeaderObjectId();
         }
      }
   }

   public boolean checkTarget(Creature target) {
      if (!Skill.checkForAreaOffensiveSkills(this, target, this._skill, this._isInArena)) {
         return false;
      } else if (target.isPlayer() && ((Player)target).inObserverMode()) {
         return false;
      } else {
         if (this._owner != null && this._owner.isInOlympiadMode()) {
            Player player = target.getActingPlayer();
            if (player != null && player.isInOlympiadMode() && player.getOlympiadSide() == this._owner.getOlympiadSide()) {
               return false;
            }
         }

         if (this._isInArena) {
            return true;
         } else {
            if (this._owner != null) {
               if (target instanceof Attackable) {
                  return true;
               }

               Player player = target.getActingPlayer();
               if (player == null || player.getPvpFlag() == 0 && player.getKarma() == 0) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   @Override
   public void deleteMe() {
      if (this._owner != null) {
         this._owner.setTrap(null);
         this._owner = null;
      }

      super.deleteMe();
   }

   @Override
   public Player getActingPlayer() {
      return this._owner;
   }

   @Override
   public Weapon getActiveWeaponItem() {
      return null;
   }

   public int getKarma() {
      return this._owner != null ? this._owner.getKarma() : 0;
   }

   public Player getOwner() {
      return this._owner;
   }

   public byte getPvpFlag() {
      return this._owner != null ? this._owner.getPvpFlag() : 0;
   }

   @Override
   public ItemInstance getSecondaryWeaponInstance() {
      return null;
   }

   @Override
   public Weapon getSecondaryWeaponItem() {
      return null;
   }

   public Skill getSkill() {
      return this._skill;
   }

   @Override
   public boolean isAutoAttackable(Creature attacker) {
      return !this.canBeSeen(attacker);
   }

   @Override
   public boolean isTrap() {
      return true;
   }

   public boolean isTriggered() {
      return this._isTriggered;
   }

   @Override
   public void onSpawn() {
      super.onSpawn();
      this._isInArena = this.isInsideZone(ZoneId.PVP) && !this.isInsideZone(ZoneId.SIEGE);
      this._playersWhoDetectedMe.clear();
   }

   @Override
   public void sendDamageMessage(Creature target, int damage, Skill skill, boolean mcrit, boolean pcrit, boolean miss) {
      if (!miss && this._owner != null) {
         if (this._owner.isInOlympiadMode()
            && target.isPlayer()
            && ((Player)target).isInOlympiadMode()
            && ((Player)target).getOlympiadGameId() == this._owner.getOlympiadGameId()) {
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

         if (target.isInvul() && !(target instanceof NpcInstance)) {
            this._owner.sendPacket(SystemMessageId.ATTACK_WAS_BLOCKED);
         } else {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_DONE_S3_DAMAGE_TO_C2);
            sm.addCharName(this);
            sm.addCharName(target);
            sm.addNumber(damage);
            this._owner.sendPacket(sm);
         }
      }
   }

   @Override
   public void sendInfo(Player activeChar) {
      if (this._isTriggered || this.canBeSeen(activeChar)) {
         activeChar.sendPacket(new NpcInfo.TrapInfo(this, activeChar));
      }
   }

   public void setDetected(Creature detector) {
      if (this._isInArena) {
         if (detector.isPlayable()) {
            this.sendInfo(detector.getActingPlayer());
         }
      } else if (this._owner == null || this._owner.getPvpFlag() != 0 || this._owner.getKarma() != 0) {
         this._playersWhoDetectedMe.add(detector.getObjectId());
         if (this.getTemplate().getEventQuests(Quest.QuestEventType.ON_TRAP_ACTION) != null) {
            for(Quest quest : this.getTemplate().getEventQuests(Quest.QuestEventType.ON_TRAP_ACTION)) {
               quest.notifyTrapAction(this, detector, Quest.TrapAction.TRAP_DETECTED);
            }
         }

         if (detector.isPlayable()) {
            this.sendInfo(detector.getActingPlayer());
         }
      }
   }

   public void stopDecay() {
      DecayTaskManager.getInstance().cancel(this);
   }

   public void triggerTrap(Creature target) {
      this._isTriggered = true;
      this.broadcastPacket(new NpcInfo.TrapInfo(this, null));
      this.setTarget(target);
      if (this.getTemplate().getEventQuests(Quest.QuestEventType.ON_TRAP_ACTION) != null) {
         for(Quest quest : this.getTemplate().getEventQuests(Quest.QuestEventType.ON_TRAP_ACTION)) {
            quest.notifyTrapAction(this, target, Quest.TrapAction.TRAP_TRIGGERED);
         }
      }

      ThreadPoolManager.getInstance().schedule(new TrapTriggerTask(this), 300L);
   }

   public void unSummon() {
      if (this._owner != null) {
         this._owner.setTrap(null);
         this._owner = null;
      }

      if (this.isVisible() && !this.isDead()) {
         this.deleteMe();
      }
   }

   @Override
   public void updateAbnormalEffect() {
   }

   public boolean hasLifeTime() {
      return this._hasLifeTime;
   }

   public void setHasLifeTime(boolean val) {
      this._hasLifeTime = val;
   }

   public int getRemainingTime() {
      return this._remainingTime;
   }

   public void setRemainingTime(int time) {
      this._remainingTime = time;
   }

   public void setSkill(Skill skill) {
      this._skill = skill;
   }

   public int getLifeTime() {
      return this._lifeTime;
   }
}
