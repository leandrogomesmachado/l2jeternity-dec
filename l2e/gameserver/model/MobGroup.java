package l2e.gameserver.model;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import l2e.commons.util.Rnd;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.ai.npc.Minions;
import l2e.gameserver.data.parser.SpawnParser;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.ControllableMobInstance;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.spawn.SpawnGroup;

public final class MobGroup {
   private final NpcTemplate _npcTemplate;
   private final int _groupId;
   private final int _maxMobCount;
   private List<ControllableMobInstance> _mobs;

   public MobGroup(int groupId, NpcTemplate npcTemplate, int maxMobCount) {
      this._groupId = groupId;
      this._npcTemplate = npcTemplate;
      this._maxMobCount = maxMobCount;
   }

   public int getActiveMobCount() {
      return this.getMobs().size();
   }

   public int getGroupId() {
      return this._groupId;
   }

   public int getMaxMobCount() {
      return this._maxMobCount;
   }

   public List<ControllableMobInstance> getMobs() {
      if (this._mobs == null) {
         this._mobs = new CopyOnWriteArrayList<>();
      }

      return this._mobs;
   }

   public String getStatus() {
      try {
         Minions mobGroupAI = (Minions)this.getMobs().get(0).getAI();
         switch(mobGroupAI.getAlternateAI()) {
            case 2:
               return "Idle";
            case 3:
               return "Force Attacking";
            case 4:
               return "Following";
            case 5:
               return "Casting";
            case 6:
               return "Attacking Group";
            default:
               return "Idle";
         }
      } catch (Exception var2) {
         return "Unspawned";
      }
   }

   public NpcTemplate getTemplate() {
      return this._npcTemplate;
   }

   public boolean isGroupMember(ControllableMobInstance mobInst) {
      for(ControllableMobInstance groupMember : this.getMobs()) {
         if (groupMember != null && groupMember.getObjectId() == mobInst.getObjectId()) {
            return true;
         }
      }

      return false;
   }

   public void spawnGroup(int x, int y, int z) {
      if (this.getActiveMobCount() <= 0) {
         try {
            for(int i = 0; i < this.getMaxMobCount(); ++i) {
               SpawnGroup spawn = new SpawnGroup(this.getTemplate());
               int signX = Rnd.nextInt(2) == 0 ? -1 : 1;
               int signY = Rnd.nextInt(2) == 0 ? -1 : 1;
               int randX = Rnd.nextInt(300);
               int randY = Rnd.nextInt(300);
               spawn.setX(x + signX * randX);
               spawn.setY(y + signY * randY);
               spawn.setZ(z);
               spawn.stopRespawn();
               SpawnParser.getInstance().addNewSpawn(spawn);
               this.getMobs().add((ControllableMobInstance)spawn.doGroupSpawn());
            }
         } catch (ClassNotFoundException var10) {
         } catch (NoSuchMethodException var11) {
         }
      }
   }

   public void spawnGroup(Player activeChar) {
      this.spawnGroup(activeChar.getX(), activeChar.getY(), activeChar.getZ());
   }

   public void teleportGroup(Player player) {
      this.removeDead();

      for(ControllableMobInstance mobInst : this.getMobs()) {
         if (mobInst != null && !mobInst.isDead()) {
            int x = player.getX() + Rnd.nextInt(50);
            int y = player.getY() + Rnd.nextInt(50);
            mobInst.teleToLocation(x, y, player.getZ(), true);
            Minions ai = (Minions)mobInst.getAI();
            ai.follow(player);
         }
      }
   }

   public ControllableMobInstance getRandomMob() {
      this.removeDead();
      if (this.getActiveMobCount() == 0) {
         return null;
      } else {
         int choice = Rnd.nextInt(this.getActiveMobCount());
         return this.getMobs().get(choice);
      }
   }

   public void unspawnGroup() {
      this.removeDead();
      if (this.getActiveMobCount() != 0) {
         for(ControllableMobInstance mobInst : this.getMobs()) {
            if (mobInst != null) {
               if (!mobInst.isDead()) {
                  mobInst.deleteMe();
               }

               SpawnParser.getInstance().deleteSpawn(mobInst.getSpawn());
            }
         }

         this.getMobs().clear();
      }
   }

   public void killGroup(Player activeChar) {
      this.removeDead();

      for(ControllableMobInstance mobInst : this.getMobs()) {
         if (mobInst != null) {
            if (!mobInst.isDead()) {
               mobInst.reduceCurrentHp(mobInst.getMaxHp() + 1.0, activeChar, null);
            }

            SpawnParser.getInstance().deleteSpawn(mobInst.getSpawn());
         }
      }

      this.getMobs().clear();
   }

   public void setAttackRandom() {
      this.removeDead();

      for(ControllableMobInstance mobInst : this.getMobs()) {
         if (mobInst != null) {
            Minions ai = (Minions)mobInst.getAI();
            ai.setAlternateAI(2);
            ai.setIntention(CtrlIntention.ACTIVE);
         }
      }
   }

   public void setAttackTarget(Creature target) {
      this.removeDead();

      for(ControllableMobInstance mobInst : this.getMobs()) {
         if (mobInst != null) {
            Minions ai = (Minions)mobInst.getAI();
            ai.forceAttack(target);
         }
      }
   }

   public void setIdleMode() {
      this.removeDead();

      for(ControllableMobInstance mobInst : this.getMobs()) {
         if (mobInst != null) {
            Minions ai = (Minions)mobInst.getAI();
            ai.stop();
         }
      }
   }

   public void returnGroup(Creature activeChar) {
      this.setIdleMode();

      for(ControllableMobInstance mobInst : this.getMobs()) {
         if (mobInst != null) {
            int signX = Rnd.nextInt(2) == 0 ? -1 : 1;
            int signY = Rnd.nextInt(2) == 0 ? -1 : 1;
            int randX = Rnd.nextInt(300);
            int randY = Rnd.nextInt(300);
            Minions ai = (Minions)mobInst.getAI();
            ai.move(activeChar.getX() + signX * randX, activeChar.getY() + signY * randY, activeChar.getZ());
         }
      }
   }

   public void setFollowMode(Creature character) {
      this.removeDead();

      for(ControllableMobInstance mobInst : this.getMobs()) {
         if (mobInst != null) {
            Minions ai = (Minions)mobInst.getAI();
            ai.follow(character);
         }
      }
   }

   public void setCastMode() {
      this.removeDead();

      for(ControllableMobInstance mobInst : this.getMobs()) {
         if (mobInst != null) {
            Minions ai = (Minions)mobInst.getAI();
            ai.setAlternateAI(5);
         }
      }
   }

   public void setNoMoveMode(boolean enabled) {
      this.removeDead();

      for(ControllableMobInstance mobInst : this.getMobs()) {
         if (mobInst != null) {
            Minions ai = (Minions)mobInst.getAI();
            ai.setNotMoving(enabled);
         }
      }
   }

   protected void removeDead() {
      List<ControllableMobInstance> deadMobs = new LinkedList<>();

      for(ControllableMobInstance mobInst : this.getMobs()) {
         if (mobInst != null && mobInst.isDead()) {
            deadMobs.add(mobInst);
         }
      }

      this.getMobs().removeAll(deadMobs);
   }

   public void setInvul(boolean invulState) {
      this.removeDead();

      for(ControllableMobInstance mobInst : this.getMobs()) {
         if (mobInst != null) {
            mobInst.setInvul(invulState);
         }
      }
   }

   public void setAttackGroup(MobGroup otherGrp) {
      this.removeDead();

      for(ControllableMobInstance mobInst : this.getMobs()) {
         if (mobInst != null) {
            Minions ai = (Minions)mobInst.getAI();
            ai.forceAttackGroup(otherGrp);
            ai.setIntention(CtrlIntention.ACTIVE);
         }
      }
   }
}
