package l2e.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.util.Broadcast;
import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.data.parser.TransformParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.instancemanager.CursedWeaponsManager;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.EarthQuake;
import l2e.gameserver.network.serverpackets.ExRedSky;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.SocialAction;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class CursedWeapon {
   private static final Logger _log = Logger.getLogger(CursedWeapon.class.getName());
   private final String _name;
   private final int _itemId;
   private final int _skillId;
   private final int _skillMaxLevel;
   private int _dropRate;
   private int _duration;
   private int _durationLost;
   private int _disapearChance;
   private int _stageKills;
   private boolean _isDropped = false;
   private boolean _isActivated = false;
   private ScheduledFuture<?> _removeTask;
   private int _nbKills = 0;
   private long _endTime = 0L;
   private int _playerId = 0;
   protected Player _player = null;
   private ItemInstance _item = null;
   private int _playerKarma = 0;
   private int _playerPkKills = 0;
   protected int transformationId = 0;

   public CursedWeapon(int itemId, int skillId, String name) {
      this._name = name;
      this._itemId = itemId;
      this._skillId = skillId;
      this._skillMaxLevel = SkillsParser.getInstance().getMaxLevel(this._skillId);
   }

   public void endOfLife() {
      if (this._isActivated) {
         if (this._player != null && this._player.isOnline()) {
            _log.info(this._name + " being removed online.");
            this._player.abortAttack();
            this._player.setKarma(this._playerKarma);
            this._player.setPkKills(this._playerPkKills);
            this._player.setCursedWeaponEquippedId(0);
            this.removeSkill();
            this._player.getInventory().unEquipItemInBodySlot(16384);
            this._player.store();
            ItemInstance removedItem = this._player.getInventory().destroyItemByItemId("", this._itemId, 1L, this._player, null);
            if (!Config.FORCE_INVENTORY_UPDATE) {
               InventoryUpdate iu = new InventoryUpdate();
               if (removedItem.getCount() == 0L) {
                  iu.addRemovedItem(removedItem);
               } else {
                  iu.addModifiedItem(removedItem);
               }

               this._player.sendPacket(iu);
            } else {
               this._player.sendItemList(true);
            }

            this._player.broadcastUserInfo(true);
         } else {
            _log.info(this._name + " being removed offline.");

            try (Connection con = DatabaseFactory.getInstance().getConnection()) {
               PreparedStatement statement = con.prepareStatement("DELETE FROM items WHERE owner_id=? AND item_id=?");
               statement.setInt(1, this._playerId);
               statement.setInt(2, this._itemId);
               if (statement.executeUpdate() != 1) {
                  _log.warning("Error while deleting itemId " + this._itemId + " from userId " + this._playerId);
               }

               statement.close();
               statement = con.prepareStatement("UPDATE characters SET karma=?, pkkills=? WHERE charId=?");
               statement.setInt(1, this._playerKarma);
               statement.setInt(2, this._playerPkKills);
               statement.setInt(3, this._playerId);
               if (statement.executeUpdate() != 1) {
                  _log.warning("Error while updating karma & pkkills for userId " + this._playerId);
               }

               statement.close();
            } catch (Exception var14) {
               _log.log(Level.WARNING, "Could not delete : " + var14.getMessage(), (Throwable)var14);
            }
         }
      } else if (this._player != null && this._player.getInventory().getItemByItemId(this._itemId) != null) {
         ItemInstance removedItem = this._player.getInventory().destroyItemByItemId("", this._itemId, 1L, this._player, null);
         if (!Config.FORCE_INVENTORY_UPDATE) {
            InventoryUpdate iu = new InventoryUpdate();
            if (removedItem.getCount() == 0L) {
               iu.addRemovedItem(removedItem);
            } else {
               iu.addModifiedItem(removedItem);
            }

            this._player.sendPacket(iu);
         } else {
            this._player.sendItemList(true);
         }

         this._player.broadcastUserInfo(true);
      } else if (this._item != null) {
         this._item.decayMe();
         _log.info(this._name + " item has been removed from World.");
      }

      CursedWeaponsManager.removeFromDb(this._itemId);
      SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_DISAPPEARED);
      sm.addItemName(this._itemId);
      CursedWeaponsManager.announce(sm);
      this.cancelTask();
      this._isActivated = false;
      this._isDropped = false;
      this._endTime = 0L;
      this._player = null;
      this._playerId = 0;
      this._playerKarma = 0;
      this._playerPkKills = 0;
      this._item = null;
      this._nbKills = 0;
   }

   private void cancelTask() {
      if (this._removeTask != null) {
         this._removeTask.cancel(true);
         this._removeTask = null;
      }
   }

   private void dropIt(Attackable attackable, Player player) {
      this.dropIt(attackable, player, null, true);
   }

   private void dropIt(Attackable attackable, Player player, Creature killer, boolean fromMonster) {
      this._isActivated = false;
      if (fromMonster) {
         this._item = attackable.dropItem(player, this._itemId, 1L);
         this._item.setDropTime(0L);
         ExRedSky packet = new ExRedSky(10);
         EarthQuake eq = new EarthQuake(player.getX(), player.getY(), player.getZ(), 14, 3);
         Broadcast.toAllOnlinePlayers(packet);
         Broadcast.toAllOnlinePlayers(eq);
      } else {
         this._item = this._player.getInventory().getItemByItemId(this._itemId);
         this._player.dropItem("DieDrop", this._item, killer, true);
         this._player.setKarma(this._playerKarma);
         this._player.setPkKills(this._playerPkKills);
         this._player.setCursedWeaponEquippedId(0);
         this.removeSkill();
         this._player.abortAttack();
      }

      this._isDropped = true;
      SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_WAS_DROPPED_IN_THE_S1_REGION);
      if (player != null) {
         sm.addZoneName(player.getX(), player.getY(), player.getZ());
      } else if (this._player != null) {
         sm.addZoneName(this._player.getX(), this._player.getY(), this._player.getZ());
      } else {
         sm.addZoneName(killer.getX(), killer.getY(), killer.getZ());
      }

      sm.addItemName(this._itemId);
      CursedWeaponsManager.announce(sm);
   }

   public void cursedOnLogin() {
      this.doTransform();
      this.giveSkill();
      SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.S2_OWNER_HAS_LOGGED_INTO_THE_S1_REGION);
      msg.addZoneName(this._player.getX(), this._player.getY(), this._player.getZ());
      msg.addItemName(this._player.getCursedWeaponEquippedId());
      CursedWeaponsManager.announce(msg);
      CursedWeapon cw = CursedWeaponsManager.getInstance().getCursedWeapon(this._player.getCursedWeaponEquippedId());
      SystemMessage msg2 = SystemMessage.getSystemMessage(SystemMessageId.S2_MINUTE_OF_USAGE_TIME_ARE_LEFT_FOR_S1);
      int timeLeft = (int)(cw.getTimeLeft() / 60000L);
      msg2.addItemName(this._player.getCursedWeaponEquippedId());
      msg2.addNumber(timeLeft);
      this._player.sendPacket(msg2);
   }

   public void giveSkill() {
      int level = 1 + this._nbKills / this._stageKills;
      if (level > this._skillMaxLevel) {
         level = this._skillMaxLevel;
      }

      Skill skill = SkillsParser.getInstance().getInfo(this._skillId, level);
      this._player.addSkill(skill, false);
      this._player.addSkill(SkillsParser.FrequentSkill.VOID_BURST.getSkill(), false);
      this._player.addTransformSkill(SkillsParser.FrequentSkill.VOID_BURST.getId());
      this._player.addSkill(SkillsParser.FrequentSkill.VOID_FLOW.getSkill(), false);
      this._player.addTransformSkill(SkillsParser.FrequentSkill.VOID_FLOW.getId());
      this._player.sendSkillList(false);
   }

   public void doTransform() {
      if (this._itemId == 8689) {
         this.transformationId = 302;
      } else if (this._itemId == 8190) {
         this.transformationId = 301;
      }

      if (!this._player.isTransformed() && !this._player.isInStance()) {
         TransformParser.getInstance().transformPlayer(this.transformationId, this._player);
      } else {
         this._player.stopTransformation(true);
         ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
               TransformParser.getInstance().transformPlayer(CursedWeapon.this.transformationId, CursedWeapon.this._player);
            }
         }, 500L);
      }
   }

   public void removeSkill() {
      this._player.removeSkill(this._skillId);
      this._player.removeSkill(SkillsParser.FrequentSkill.VOID_BURST.getSkill().getId());
      this._player.removeSkill(SkillsParser.FrequentSkill.VOID_FLOW.getSkill().getId());
      this._player.untransform();
      this._player.sendSkillList(false);
   }

   public void reActivate() {
      this._isActivated = true;
      if (this._endTime - System.currentTimeMillis() <= 0L) {
         this.endOfLife();
      } else {
         this._removeTask = ThreadPoolManager.getInstance()
            .scheduleAtFixedRate(new CursedWeapon.RemoveTask(), (long)this._durationLost * 12000L, (long)this._durationLost * 12000L);
      }
   }

   public boolean checkDrop(Attackable attackable, Player player) {
      if (Rnd.get(100000) < this._dropRate) {
         this.dropIt(attackable, player);
         this._endTime = System.currentTimeMillis() + (long)this._duration * 60000L;
         this._removeTask = ThreadPoolManager.getInstance()
            .scheduleAtFixedRate(new CursedWeapon.RemoveTask(), (long)this._durationLost * 12000L, (long)this._durationLost * 12000L);
         return true;
      } else {
         return false;
      }
   }

   public void activate(Player player, ItemInstance item) {
      if (player.isMounted() && !player.dismount()) {
         player.sendPacket(SystemMessageId.FAILED_TO_PICKUP_S1);
         player.dropItem("InvDrop", item, null, true);
      } else {
         this._isActivated = true;
         this._player = player;
         this._playerId = this._player.getObjectId();
         this._playerKarma = this._player.getKarma();
         this._playerPkKills = this._player.getPkKills();
         this.saveData();
         this._player.setCursedWeaponEquippedId(this._itemId);
         this._player.setKarma(9999999);
         this._player.setPkKills(0);
         if (this._player.isInParty()) {
            this._player.getParty().removePartyMember(this._player, Party.messageType.Expelled);
         }

         this.doTransform();
         this.giveSkill();
         this._item = item;
         this._player.getInventory().equipItem(this._item);
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_EQUIPPED);
         sm.addItemName(this._item);
         this._player.sendPacket(sm);
         this._player.setCurrentHpMp(this._player.getMaxHp(), this._player.getMaxMp());
         this._player.setCurrentCp(this._player.getMaxCp());
         if (!Config.FORCE_INVENTORY_UPDATE) {
            InventoryUpdate iu = new InventoryUpdate();
            iu.addItem(this._item);
            this._player.sendPacket(iu);
         } else {
            this._player.sendItemList(false);
         }

         this._player.broadcastUserInfo(true);
         SocialAction atk = new SocialAction(this._player.getObjectId(), 17);
         this._player.broadcastPacket(atk);
         sm = SystemMessage.getSystemMessage(SystemMessageId.THE_OWNER_OF_S2_HAS_APPEARED_IN_THE_S1_REGION);
         sm.addZoneName(this._player.getX(), this._player.getY(), this._player.getZ());
         sm.addItemName(this._item);
         CursedWeaponsManager.announce(sm);
      }
   }

   public void saveData() {
      if (Config.DEBUG) {
         _log.info("CursedWeapon: Saving data to disk.");
      }

      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("DELETE FROM cursed_weapons WHERE itemId = ?");
         statement.setInt(1, this._itemId);
         statement.executeUpdate();
         statement.close();
         if (this._isActivated) {
            statement = con.prepareStatement(
               "INSERT INTO cursed_weapons (itemId, charId, playerKarma, playerPkKills, nbKills, endTime) VALUES (?, ?, ?, ?, ?, ?)"
            );
            statement.setInt(1, this._itemId);
            statement.setInt(2, this._playerId);
            statement.setInt(3, this._playerKarma);
            statement.setInt(4, this._playerPkKills);
            statement.setInt(5, this._nbKills);
            statement.setLong(6, this._endTime);
            statement.executeUpdate();
            statement.close();
         }
      } catch (SQLException var14) {
         _log.log(Level.SEVERE, "CursedWeapon: Failed to save data.", (Throwable)var14);
      }
   }

   public void dropIt(Creature killer) {
      if (Rnd.get(100) <= this._disapearChance) {
         this.endOfLife();
      } else {
         this.dropIt(null, null, killer, false);
         this._player.setKarma(this._playerKarma);
         this._player.setPkKills(this._playerPkKills);
         this._player.setCursedWeaponEquippedId(0);
         this.removeSkill();
         this._player.abortAttack();
         this._player.broadcastUserInfo(true);
      }
   }

   public void increaseKills() {
      ++this._nbKills;
      if (this._player != null && this._player.isOnline()) {
         this._player.setPkKills(this._nbKills);
         this._player.sendUserInfo();
         if (this._nbKills % this._stageKills == 0 && this._nbKills <= this._stageKills * (this._skillMaxLevel - 1)) {
            this.giveSkill();
         }
      }

      this._endTime -= (long)this._durationLost * 60000L;
      this.saveData();
   }

   public void setDisapearChance(int disapearChance) {
      this._disapearChance = disapearChance;
   }

   public void setDropRate(int dropRate) {
      this._dropRate = dropRate;
   }

   public void setDuration(int duration) {
      this._duration = duration;
   }

   public void setDurationLost(int durationLost) {
      this._durationLost = durationLost;
   }

   public void setStageKills(int stageKills) {
      this._stageKills = stageKills;
   }

   public void setNbKills(int nbKills) {
      this._nbKills = nbKills;
   }

   public void setPlayerId(int playerId) {
      this._playerId = playerId;
   }

   public void setPlayerKarma(int playerKarma) {
      this._playerKarma = playerKarma;
   }

   public void setPlayerPkKills(int playerPkKills) {
      this._playerPkKills = playerPkKills;
   }

   public void setActivated(boolean isActivated) {
      this._isActivated = isActivated;
   }

   public void setDropped(boolean isDropped) {
      this._isDropped = isDropped;
   }

   public void setEndTime(long endTime) {
      this._endTime = endTime;
   }

   public void setPlayer(Player player) {
      this._player = player;
   }

   public void setItem(ItemInstance item) {
      this._item = item;
   }

   public boolean isActivated() {
      return this._isActivated;
   }

   public boolean isDropped() {
      return this._isDropped;
   }

   public long getEndTime() {
      return this._endTime;
   }

   public String getName() {
      return this._name;
   }

   public int getItemId() {
      return this._itemId;
   }

   public int getSkillId() {
      return this._skillId;
   }

   public int getPlayerId() {
      return this._playerId;
   }

   public Player getPlayer() {
      return this._player;
   }

   public int getPlayerKarma() {
      return this._playerKarma;
   }

   public int getPlayerPkKills() {
      return this._playerPkKills;
   }

   public int getNbKills() {
      return this._nbKills;
   }

   public int getStageKills() {
      return this._stageKills;
   }

   public boolean isActive() {
      return this._isActivated || this._isDropped;
   }

   public int getLevel() {
      return this._nbKills > this._stageKills * this._skillMaxLevel ? this._skillMaxLevel : this._nbKills / this._stageKills;
   }

   public long getTimeLeft() {
      return this._endTime - System.currentTimeMillis();
   }

   public void goTo(Player player) {
      if (player != null) {
         if (this._isActivated && this._player != null) {
            player.teleToLocation(this._player.getX(), this._player.getY(), this._player.getZ() + 20, true);
         } else if (this._isDropped && this._item != null) {
            player.teleToLocation(this._item.getX(), this._item.getY(), this._item.getZ() + 20, true);
         } else {
            player.sendMessage(this._name + " isn't in the World.");
         }
      }
   }

   public Location getWorldPosition() {
      if (this._isActivated && this._player != null) {
         return this._player.getLocation();
      } else {
         return this._isDropped && this._item != null ? this._item.getLocation() : null;
      }
   }

   public long getDuration() {
      return (long)this._duration;
   }

   private class RemoveTask implements Runnable {
      protected RemoveTask() {
      }

      @Override
      public void run() {
         if (System.currentTimeMillis() >= CursedWeapon.this.getEndTime()) {
            CursedWeapon.this.endOfLife();
         }
      }
   }
}
