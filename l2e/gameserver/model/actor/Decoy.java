package l2e.gameserver.model.actor;

import l2e.gameserver.data.parser.CategoryParser;
import l2e.gameserver.model.CategoryType;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.templates.character.CharTemplate;
import l2e.gameserver.model.actor.templates.items.Weapon;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.CharInfo;
import l2e.gameserver.network.serverpackets.GameServerPacket;
import l2e.gameserver.taskmanager.DecayTaskManager;

public abstract class Decoy extends Creature {
   private final Player _owner;

   public Decoy(int objectId, CharTemplate template, Player owner) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.Decoy);
      this._owner = owner;
      this.setXYZInvisible(owner.getX(), owner.getY(), owner.getZ());
      this.setIsInvul(false);
   }

   @Override
   public void onSpawn() {
      super.onSpawn();
      this.sendPacket(new CharInfo(this));
   }

   @Override
   public void updateAbnormalEffect() {
      for(Player player : World.getInstance().getAroundPlayers(this)) {
         if (player != null) {
            player.sendPacket(new CharInfo(this));
         }
      }
   }

   public void stopDecay() {
      DecayTaskManager.getInstance().cancel(this);
   }

   @Override
   public void onDecay() {
      this.deleteMe(this._owner);
   }

   @Override
   public boolean isAutoAttackable(Creature attacker) {
      return this._owner.isAutoAttackable(attacker);
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
   public final int getId() {
      return this.getTemplate().getId();
   }

   @Override
   public int getLevel() {
      return this.getTemplate().getLevel();
   }

   public void deleteMe(Player owner) {
      this.decayMe();
      owner.setDecoy(null);
   }

   public synchronized void unSummon(Player owner) {
      if (this.isVisible() && !this.isDead()) {
         owner.setDecoy(null);
         this.decayMe();
      }
   }

   public final Player getOwner() {
      return this._owner;
   }

   @Override
   public Player getActingPlayer() {
      return this._owner;
   }

   public NpcTemplate getTemplate() {
      return (NpcTemplate)super.getTemplate();
   }

   @Override
   public void sendInfo(Player activeChar) {
      activeChar.sendPacket(new CharInfo(this));
   }

   @Override
   public void sendPacket(GameServerPacket mov) {
      if (this.getOwner() != null) {
         this.getOwner().sendPacket(mov);
      }
   }

   @Override
   public void sendPacket(SystemMessageId id) {
      if (this.getOwner() != null) {
         this.getOwner().sendPacket(id);
      }
   }

   @Override
   public boolean isInCategory(CategoryType type) {
      return CategoryParser.getInstance().isInCategory(type, this.getId());
   }
}
