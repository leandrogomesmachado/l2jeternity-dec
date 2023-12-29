package l2e.gameserver.model.actor.instance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.character.SummonAI;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.holder.CharSummonHolder;
import l2e.gameserver.data.holder.SummonEffectsHolder;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.data.parser.PetsParser;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.handler.itemhandlers.IItemHandler;
import l2e.gameserver.handler.itemhandlers.ItemHandler;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.instancemanager.CursedWeaponsManager;
import l2e.gameserver.instancemanager.ItemsOnGroundManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.PetData;
import l2e.gameserver.model.TimeStamp;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.actor.stat.PetStat;
import l2e.gameserver.model.actor.templates.PetLevelTemplate;
import l2e.gameserver.model.actor.templates.items.Weapon;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.itemcontainer.Inventory;
import l2e.gameserver.model.items.itemcontainer.PetInventory;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.stats.BaseStats;
import l2e.gameserver.model.stats.Stats;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.PetInventoryUpdate;
import l2e.gameserver.network.serverpackets.PetItemList;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.StopMove;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.taskmanager.DecayTaskManager;

public class PetInstance extends Summon {
   protected static final Logger _logPet = Logger.getLogger(PetInstance.class.getName());
   private static final String ADD_SKILL_SAVE = "INSERT INTO character_pet_skills_save (petObjItemId,skill_id,skill_level,effect_count,effect_cur_time,effect_total_time,buff_index) VALUES (?,?,?,?,?,?,?)";
   private static final String RESTORE_SKILL_SAVE = "SELECT petObjItemId,skill_id,skill_level,effect_count,effect_cur_time,effect_total_time,buff_index FROM character_pet_skills_save WHERE petObjItemId=? ORDER BY buff_index ASC";
   private static final String DELETE_SKILL_SAVE = "DELETE FROM character_pet_skills_save WHERE petObjItemId=?";
   private final Map<Integer, TimeStamp> _reuseTimeStampsSkills = new ConcurrentHashMap<>();
   private final Map<Integer, TimeStamp> _reuseTimeStampsItems = new ConcurrentHashMap<>();
   private int _curFed;
   private final PetInventory _inventory;
   private final int _controlObjectId;
   private boolean _respawned;
   private final boolean _mountable;
   private Future<?> _feedTask;
   private PetData _data;
   private PetLevelTemplate _leveldata;
   private long _expBeforeDeath = 0L;
   private int _curWeightPenalty = 0;
   private static final int PET_DECAY_DELAY = 86400;

   public final PetLevelTemplate getPetLevelData() {
      if (this._leveldata == null) {
         this._leveldata = PetsParser.getInstance().getPetLevelData(this.getTemplate().getId(), this.getStat().getLevel());
      }

      return this._leveldata;
   }

   public final PetData getPetData() {
      if (this._data == null) {
         this._data = PetsParser.getInstance().getPetData(this.getTemplate().getId());
      }

      return this._data;
   }

   public final void setPetData(PetLevelTemplate value) {
      this._leveldata = value;
   }

   public static synchronized PetInstance spawnPet(NpcTemplate template, Player owner, ItemInstance control) {
      if (World.getInstance().getPet(owner.getObjectId()) != null) {
         return null;
      } else {
         PetData data = PetsParser.getInstance().getPetData(template.getId());
         PetInstance pet = restore(control, template, owner);
         if (pet != null) {
            pet.setTitle(owner.getName());
            if (data.isSynchLevel() && pet.getLevel() != owner.getLevel()) {
               pet.getStat().setLevel((byte)owner.getLevel());
               pet.getStat().setExp(pet.getStat().getExpForLevel(owner.getLevel()));
            }

            World.getInstance().addPet(owner.getObjectId(), pet);
         }

         return pet;
      }
   }

   public PetInstance(int objectId, NpcTemplate template, Player owner, ItemInstance control) {
      this(
         objectId,
         template,
         owner,
         control,
         (byte)(
            template.getIdTemplate() != 12564
                  && template.getIdTemplate() != 16043
                  && template.getIdTemplate() != 16044
                  && template.getIdTemplate() != 16045
                  && template.getIdTemplate() != 16046
                  && template.getIdTemplate() != 16050
                  && template.getIdTemplate() != 16051
                  && template.getIdTemplate() != 16052
                  && template.getIdTemplate() != 16053
               ? template.getLevel()
               : owner.getLevel()
         )
      );
   }

   public PetInstance(int objectId, NpcTemplate template, Player owner, ItemInstance control, byte level) {
      super(objectId, template, owner);
      this.setInstanceType(GameObject.InstanceType.PetInstance);
      this._controlObjectId = control.getObjectId();
      this.getStat().setLevel((byte)Math.max(level, PetsParser.getInstance().getPetMinLevel(template.getId())));
      if (template.getIdTemplate() == 16043
         || template.getIdTemplate() == 16044
         || template.getIdTemplate() == 16045
         || template.getIdTemplate() == 16046
         || template.getIdTemplate() == 16050
         || template.getIdTemplate() == 16051
         || template.getIdTemplate() == 16052
         || template.getIdTemplate() == 16053) {
         this.getStat().setLevel((byte)this.getOwner().getLevel());
      }

      this._inventory = new PetInventory(this);
      this._inventory.restore();
      int npcId = template.getId();
      this._mountable = PetsParser.isMountable(npcId);
      this.getPetData();
      this.getPetLevelData();
   }

   public PetStat getStat() {
      return (PetStat)super.getStat();
   }

   @Override
   public void initCharStat() {
      this.setStat(new PetStat(this));
   }

   public boolean isRespawned() {
      return this._respawned;
   }

   @Override
   public int getSummonType() {
      return 2;
   }

   @Override
   public int getControlObjectId() {
      return this._controlObjectId;
   }

   public ItemInstance getControlItem() {
      return this.getOwner().getInventory().getItemByObjectId(this._controlObjectId);
   }

   public int getCurrentFed() {
      return this._curFed;
   }

   public void setCurrentFed(int num) {
      this._curFed = num > this.getMaxFed() ? this.getMaxFed() : num;
   }

   @Override
   public ItemInstance getActiveWeaponInstance() {
      for(ItemInstance item : this.getInventory().getItems()) {
         if (item.getItemLocation() == ItemInstance.ItemLocation.PET_EQUIP && item.getItem().getBodyPart() == 128) {
            return item;
         }
      }

      return null;
   }

   @Override
   public Weapon getActiveWeaponItem() {
      ItemInstance weapon = this.getActiveWeaponInstance();
      return weapon == null ? null : (Weapon)weapon.getItem();
   }

   @Override
   public ItemInstance getSecondaryWeaponInstance() {
      return null;
   }

   @Override
   public Weapon getSecondaryWeaponItem() {
      return null;
   }

   @Override
   public PetInventory getInventory() {
      return this._inventory;
   }

   @Override
   public boolean destroyItem(String process, int objectId, long count, GameObject reference, boolean sendMessage) {
      ItemInstance item = this._inventory.destroyItem(process, objectId, count, this.getOwner(), reference);
      if (item == null) {
         if (sendMessage) {
            this.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
         }

         return false;
      } else {
         PetInventoryUpdate petIU = new PetInventoryUpdate();
         petIU.addItem(item);
         this.sendPacket(petIU);
         if (sendMessage) {
            if (count > 1L) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
               sm.addItemName(item.getId());
               sm.addItemNumber(count);
               this.sendPacket(sm);
            } else {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
               sm.addItemName(item.getId());
               this.sendPacket(sm);
            }
         }

         return true;
      }
   }

   @Override
   public boolean destroyItemByItemId(String process, int itemId, long count, GameObject reference, boolean sendMessage) {
      ItemInstance item = this._inventory.destroyItemByItemId(process, itemId, count, this.getOwner(), reference);
      if (item == null) {
         if (sendMessage) {
            this.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
         }

         return false;
      } else {
         PetInventoryUpdate petIU = new PetInventoryUpdate();
         petIU.addItem(item);
         this.sendPacket(petIU);
         if (sendMessage) {
            if (count > 1L) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
               sm.addItemName(item.getId());
               sm.addItemNumber(count);
               this.sendPacket(sm);
            } else {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
               sm.addItemName(item.getId());
               this.sendPacket(sm);
            }
         }

         return true;
      }
   }

   @Override
   public void doPickupItem(GameObject object) {
      boolean follow = this.isInFollowStatus();
      if (!this.isDead()) {
         this.getAI().setIntention(CtrlIntention.IDLE);
         StopMove sm = new StopMove(this.getObjectId(), this.getX(), this.getY(), this.getZ(), this.getHeading());
         if (Config.DEBUG) {
            _logPet.fine("Pet pickup pos: " + object.getX() + " " + object.getY() + " " + object.getZ());
         }

         this.broadcastPacket(sm);
         if (!(object instanceof ItemInstance)) {
            _logPet.warning(this + " trying to pickup wrong target." + object);
            this.sendActionFailed();
         } else {
            ItemInstance target = (ItemInstance)object;
            if (CursedWeaponsManager.getInstance().isCursed(target.getId())) {
               SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1);
               smsg.addItemName(target.getId());
               this.sendPacket(smsg);
            } else {
               synchronized(target) {
                  if (!target.isVisible()) {
                     this.sendActionFailed();
                     return;
                  }

                  if (!target.getDropProtection().tryPickUp(this)) {
                     this.sendActionFailed();
                     SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1);
                     smsg.addItemName(target);
                     this.sendPacket(smsg);
                     return;
                  }

                  if (!this._inventory.validateCapacity(target)) {
                     this.sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_ANY_MORE_ITEMS);
                     return;
                  }

                  if (!this._inventory.validateWeight(target, target.getCount())) {
                     this.sendPacket(SystemMessageId.UNABLE_TO_PLACE_ITEM_YOUR_PET_IS_TOO_ENCUMBERED);
                     return;
                  }

                  if (target.getOwnerId() != 0
                     && target.getOwnerId() != this.getOwner().getObjectId()
                     && !this.getOwner().isInLooterParty(target.getOwnerId())) {
                     this.sendActionFailed();
                     if (target.getId() == 57) {
                        SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1_ADENA);
                        smsg.addItemNumber(target.getCount());
                        this.sendPacket(smsg);
                     } else if (target.getCount() > 1L) {
                        SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S2_S1_S);
                        smsg.addItemName(target.getId());
                        smsg.addItemNumber(target.getCount());
                        this.sendPacket(smsg);
                     } else {
                        SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1);
                        smsg.addItemName(target.getId());
                        this.sendPacket(smsg);
                     }

                     return;
                  }

                  if (target.getItemLootShedule() != null
                     && (target.getOwnerId() == this.getOwner().getObjectId() || this.getOwner().isInLooterParty(target.getOwnerId()))) {
                     target.resetOwnerTimer();
                  }

                  target.pickupMe(this);
                  if (Config.SAVE_DROPPED_ITEM) {
                     ItemsOnGroundManager.getInstance().removeObject(target);
                  }
               }

               if (target.getItem().isHerb()) {
                  IItemHandler handler = ItemHandler.getInstance().getHandler(target.getEtcItem());
                  if (handler == null) {
                     _log.warning("No item handler registered for item ID: " + target.getId() + ".");
                  } else {
                     handler.useItem(this, target, false);
                  }

                  ItemsParser.getInstance().destroyItem("Consume", target, this.getOwner(), null);
                  this.broadcastStatusUpdate();
               } else {
                  if (target.getId() == 57) {
                     SystemMessage sm2 = SystemMessage.getSystemMessage(SystemMessageId.PET_PICKED_S1_ADENA);
                     sm2.addItemNumber(target.getCount());
                     this.sendPacket(sm2);
                  } else if (target.getEnchantLevel() > 0) {
                     SystemMessage sm2 = SystemMessage.getSystemMessage(SystemMessageId.PET_PICKED_S1_S2);
                     sm2.addNumber(target.getEnchantLevel());
                     sm2.addString(target.getName());
                     this.sendPacket(sm2);
                  } else if (target.getCount() > 1L) {
                     SystemMessage sm2 = SystemMessage.getSystemMessage(SystemMessageId.PET_PICKED_S2_S1_S);
                     sm2.addItemNumber(target.getCount());
                     sm2.addString(target.getName());
                     this.sendPacket(sm2);
                  } else {
                     SystemMessage sm2 = SystemMessage.getSystemMessage(SystemMessageId.PET_PICKED_S1);
                     sm2.addString(target.getName());
                     this.sendPacket(sm2);
                  }

                  if (this.getOwner().isInParty() && this.getOwner().getParty().getLootDistribution() != 0) {
                     this.getOwner().getParty().distributeItem(this.getOwner(), target);
                  } else {
                     this.getInventory().addItem("Pickup", target, this.getOwner(), this);
                     this.sendPacket(new PetItemList(this.getInventory().getItems()));
                  }
               }

               this.getAI().setIntention(CtrlIntention.IDLE);
               if (follow) {
                  ((SummonAI)this.getAI()).setStartFollowController(true);
                  this.followOwner();
               }
            }
         }
      }
   }

   @Override
   public void deleteMe(Player owner) {
      this.getInventory().transferItemsToOwner();
      super.deleteMe(owner);
      this.destroyControlItem(owner, false);
      CharSummonHolder.getInstance().getPets().remove(this.getOwner().getObjectId());
   }

   @Override
   protected void onDeath(Creature killer) {
      this.stopFeed();
      this.sendPacket(SystemMessageId.MAKE_SURE_YOU_RESSURECT_YOUR_PET_WITHIN_24_HOURS);
      DecayTaskManager.getInstance().add(this, 86400L);
      Player owner = this.getOwner();
      if (owner != null && !owner.isInDuel() && (!this.isInsideZone(ZoneId.PVP) || this.isInsideZone(ZoneId.SIEGE))) {
         this.deathPenalty();
      }

      super.onDeath(killer);
   }

   @Override
   public void doRevive() {
      this.getOwner().removeReviving();
      super.doRevive();
      DecayTaskManager.getInstance().cancel(this);
      this.startFeed();
      if (!this.isHungry()) {
         this.setRunning();
      }

      this.getAI().setIntention(CtrlIntention.ACTIVE, null);
   }

   @Override
   public void doRevive(double revivePower) {
      this.restoreExp(revivePower);
      this.doRevive();
   }

   public ItemInstance transferItem(String process, int objectId, long count, Inventory target, Player actor, GameObject reference) {
      ItemInstance oldItem = this.getInventory().getItemByObjectId(objectId);
      ItemInstance playerOldItem = target.getItemByItemId(oldItem.getId());
      ItemInstance newItem = this.getInventory().transferItem(process, objectId, count, target, actor, reference);
      if (newItem == null) {
         return null;
      } else {
         PetInventoryUpdate petIU = new PetInventoryUpdate();
         if (oldItem.getCount() > 0L && oldItem != newItem) {
            petIU.addModifiedItem(oldItem);
         } else {
            petIU.addRemovedItem(oldItem);
         }

         this.sendPacket(petIU);
         if (!newItem.isStackable()) {
            if (this.getOwner() != null) {
               InventoryUpdate iu = new InventoryUpdate();
               iu.addNewItem(newItem);
               this.getOwner().sendPacket(iu);
            }
         } else if (playerOldItem != null && newItem.isStackable() && this.getOwner() != null) {
            InventoryUpdate iu = new InventoryUpdate();
            iu.addModifiedItem(newItem);
            this.getOwner().sendPacket(iu);
         }

         return newItem;
      }
   }

   public void destroyControlItem(Player owner, boolean evolve) {
      World.getInstance().removePet(owner.getObjectId());

      try {
         ItemInstance removedItem;
         if (evolve) {
            removedItem = owner.getInventory().destroyItem("Evolve", this.getControlObjectId(), 1L, this.getOwner(), this);
         } else {
            removedItem = owner.getInventory().destroyItem("PetDestroy", this.getControlObjectId(), 1L, this.getOwner(), this);
            if (removedItem != null) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
               sm.addItemName(removedItem);
               owner.sendPacket(sm);
            }
         }

         if (removedItem == null) {
            _log.warning("Couldn't destroy pet control item for " + owner + " pet: " + this + " evolve: " + evolve);
         } else {
            InventoryUpdate iu = new InventoryUpdate();
            iu.addRemovedItem(removedItem);
            owner.sendPacket(iu);
            StatusUpdate su = new StatusUpdate(owner);
            su.addAttribute(14, owner.getCurrentLoad());
            owner.sendPacket(su);
            owner.broadcastUserInfo(true);
            World.getInstance().removeObject(removedItem);
         }
      } catch (Exception var34) {
         _logPet.log(Level.WARNING, "Error while destroying control item: " + var34.getMessage(), (Throwable)var34);
      }

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id = ?");
      ) {
         statement.setInt(1, this.getControlObjectId());
         statement.execute();
      } catch (Exception var38) {
         _logPet.log(Level.SEVERE, "Failed to delete Pet [ObjectId: " + this.getObjectId() + "]", (Throwable)var38);
      }
   }

   public void dropAllItems() {
      try {
         for(ItemInstance item : this.getInventory().getItems()) {
            this.dropItemHere(item);
         }
      } catch (Exception var5) {
         _logPet.log(Level.WARNING, "Pet Drop Error: " + var5.getMessage(), (Throwable)var5);
      }
   }

   public void dropItemHere(ItemInstance dropit, boolean protect) {
      dropit = this.getInventory().dropItem("Drop", dropit.getObjectId(), dropit.getCount(), this.getOwner(), this);
      if (dropit != null) {
         if (protect) {
            dropit.getDropProtection().protect(this.getOwner(), false);
         }

         _logPet.finer("Item id to drop: " + dropit.getId() + " amount: " + dropit.getCount());
         dropit.dropMe(this, this.getX(), this.getY(), this.getZ() + 100);
      }
   }

   public void dropItemHere(ItemInstance dropit) {
      this.dropItemHere(dropit, false);
   }

   @Override
   public boolean isMountable() {
      return this._mountable;
   }

   private static PetInstance restore(ItemInstance control, NpcTemplate template, Player owner) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT item_obj_id, name, level, curHp, curMp, exp, sp, fed FROM pets WHERE item_obj_id=?");
      ) {
         statement.setInt(1, control.getObjectId());

         PetInstance pet;
         try (ResultSet rset = statement.executeQuery()) {
            int id = IdFactory.getInstance().getNextId();
            if (!rset.next()) {
               if (template.isType("BabyPet")) {
                  pet = new BabyPetInstance(id, template, owner, control);
               } else {
                  pet = new PetInstance(id, template, owner, control);
               }

               return pet;
            }

            if (template.isType("BabyPet")) {
               pet = new BabyPetInstance(id, template, owner, control, rset.getByte("level"));
            } else {
               pet = new PetInstance(id, template, owner, control, rset.getByte("level"));
            }

            pet._respawned = true;
            pet.setName(rset.getString("name"));
            pet.setNameRu(rset.getString("name"));
            long exp = rset.getLong("exp");
            PetLevelTemplate info = PetsParser.getInstance().getPetLevelData(pet.getId(), pet.getLevel());
            if (info != null && exp < info.getPetMaxExp()) {
               exp = info.getPetMaxExp();
            }

            pet.getStat().setExp(exp);
            pet.getStat().setSp(rset.getInt("sp"));
            pet.getStatus().setCurrentHp((double)rset.getInt("curHp"));
            pet.getStatus().setCurrentMp((double)rset.getInt("curMp"));
            pet.getStatus().setCurrentCp(pet.getMaxCp());
            if (rset.getDouble("curHp") < 1.0) {
               pet.setIsDead(true);
               pet.stopHpMpRegeneration();
            }

            pet.setCurrentFed(rset.getInt("fed"));
         }

         return pet;
      } catch (Exception var71) {
         _logPet.log(Level.WARNING, "Could not restore pet data for owner: " + owner + " - " + var71.getMessage(), (Throwable)var71);
         return null;
      }
   }

   @Override
   public void setRestoreSummon(boolean val) {
      this._restoreSummon = val;
   }

   @Override
   public final void stopSkillEffects(int skillId) {
      super.stopSkillEffects(skillId);
      SummonEffectsHolder.getInstance().removePetEffects(this.getControlObjectId(), skillId);
   }

   @Override
   public void store() {
      if (this.getControlObjectId() != 0) {
         if (!Config.RESTORE_PET_ON_RECONNECT) {
            this._restoreSummon = false;
         }

         String req;
         if (!this.isRespawned()) {
            req = "INSERT INTO pets (name,level,curHp,curMp,exp,sp,fed,ownerId,restore,item_obj_id) VALUES (?,?,?,?,?,?,?,?,?,?)";
         } else {
            req = "UPDATE pets SET name=?,level=?,curHp=?,curMp=?,exp=?,sp=?,fed=?,ownerId=?,restore=? WHERE item_obj_id = ?";
         }

         try (Connection con = DatabaseFactory.getInstance().getConnection()) {
            PreparedStatement statement = con.prepareStatement(req);
            statement.setString(1, this.getName());
            statement.setInt(2, this.getStat().getLevel());
            statement.setDouble(3, this.getStatus().getCurrentHp());
            statement.setDouble(4, this.getStatus().getCurrentMp());
            statement.setLong(5, this.getStat().getExp());
            statement.setInt(6, this.getStat().getSp());
            statement.setInt(7, this.getCurrentFed());
            statement.setInt(8, this.getOwner().getObjectId());
            statement.setString(9, String.valueOf(this._restoreSummon));
            statement.setInt(10, this.getControlObjectId());
            statement.executeUpdate();
            statement.close();
            this._respawned = true;
            if (this._restoreSummon) {
               CharSummonHolder.getInstance().getPets().put(this.getOwner().getObjectId(), this.getControlObjectId());
            } else {
               CharSummonHolder.getInstance().getPets().remove(this.getOwner().getObjectId());
            }
         } catch (Exception var15) {
            _logPet.log(Level.SEVERE, "Failed to store Pet [ObjectId: " + this.getObjectId() + "] data", (Throwable)var15);
         }

         ItemInstance itemInst = this.getControlItem();
         if (itemInst != null && itemInst.getEnchantLevel() != this.getStat().getLevel()) {
            itemInst.setEnchantLevel(this.getStat().getLevel());
            itemInst.updateDatabase();
         }
      }
   }

   @Override
   public void storeEffect(boolean storeEffects) {
      if (Config.SUMMON_STORE_SKILL_COOLTIME) {
         SummonEffectsHolder.getInstance().clearPetEffects(this.getControlObjectId());

         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement ps1 = con.prepareStatement("DELETE FROM character_pet_skills_save WHERE petObjItemId=?");
            PreparedStatement ps2 = con.prepareStatement(
               "INSERT INTO character_pet_skills_save (petObjItemId,skill_id,skill_level,effect_count,effect_cur_time,effect_total_time,buff_index) VALUES (?,?,?,?,?,?,?)"
            );
         ) {
            ps1.setInt(1, this.getControlObjectId());
            ps1.execute();
            int buff_index = 0;
            List<Integer> storedSkills = new LinkedList<>();
            if (storeEffects) {
               for(Effect effect : this.getAllEffects()) {
                  if (effect != null) {
                     switch(effect.getEffectType()) {
                        case HEAL_OVER_TIME:
                        case CPHEAL_OVER_TIME:
                        case HIDE:
                           break;
                        default:
                           if (!effect.getAbnormalType().equalsIgnoreCase("LIFE_FORCE_OTHERS")) {
                              Skill skill = effect.getSkill();
                              if ((!skill.isDance() || Config.ALT_STORE_DANCES) && !storedSkills.contains(skill.getReuseHashCode())) {
                                 storedSkills.add(skill.getReuseHashCode());
                                 if (effect.isInUse() && !skill.isToggle()) {
                                    ps2.setInt(1, this.getControlObjectId());
                                    ps2.setInt(2, skill.getId());
                                    ps2.setInt(3, skill.getLevel());
                                    ps2.setInt(4, effect.getTickCount());
                                    ps2.setInt(5, effect.getTime());
                                    ps2.setInt(6, effect.getAbnormalTime());
                                    ps2.setInt(7, ++buff_index);
                                    ps2.execute();
                                    SummonEffectsHolder.getInstance()
                                       .addPetEffect(this.getControlObjectId(), skill, effect.getTickCount(), effect.getTime(), effect.getAbnormalTime());
                                 }
                              }
                           }
                     }
                  }
               }
            }
         } catch (Exception var66) {
            _log.log(Level.WARNING, "Could not store pet effect data: ", (Throwable)var66);
         }
      }
   }

   @Override
   public void restoreEffects() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement ps1 = con.prepareStatement(
            "SELECT petObjItemId,skill_id,skill_level,effect_count,effect_cur_time,effect_total_time,buff_index FROM character_pet_skills_save WHERE petObjItemId=? ORDER BY buff_index ASC"
         );
         PreparedStatement ps2 = con.prepareStatement("DELETE FROM character_pet_skills_save WHERE petObjItemId=?");
      ) {
         if (!SummonEffectsHolder.getInstance().containsPetId(this.getControlObjectId())) {
            ps1.setInt(1, this.getControlObjectId());

            try (ResultSet rset = ps1.executeQuery()) {
               while(rset.next()) {
                  int effectCount = rset.getInt("effect_count");
                  int effectCurTime = rset.getInt("effect_cur_time");
                  int effectTotalTime = rset.getInt("effect_total_time");
                  Skill skill = SkillsParser.getInstance().getInfo(rset.getInt("skill_id"), rset.getInt("skill_level"));
                  if (skill != null && skill.hasEffects()) {
                     SummonEffectsHolder.getInstance().addPetEffect(this.getControlObjectId(), skill, effectCount, effectCurTime, effectTotalTime);
                  }
               }
            }
         }

         ps2.setInt(1, this.getControlObjectId());
         ps2.executeUpdate();
      } catch (Exception var118) {
         _log.log(Level.WARNING, "Could not restore " + this + " active effect data: " + var118.getMessage(), (Throwable)var118);
      } finally {
         SummonEffectsHolder.getInstance().applyPetEffects(this, this.getControlObjectId());
      }
   }

   public synchronized void stopFeed() {
      if (this._feedTask != null) {
         this._feedTask.cancel(false);
         this._feedTask = null;
      }
   }

   public synchronized void startFeed() {
      this.stopFeed();
      if (!this.isDead() && this.getOwner().getSummon() == this) {
         this._feedTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new PetInstance.FeedTask(), 10000L, 10000L);
      }
   }

   @Override
   public synchronized void unSummon(Player owner) {
      this.stopFeed();
      this.stopHpMpRegeneration();
      super.unSummon(owner);
      if (!this.isDead()) {
         if (this.getInventory() != null) {
            this.getInventory().deleteMe();
         }

         World.getInstance().removePet(owner.getObjectId());
      }
   }

   public void restoreExp(double restorePercent) {
      if (this._expBeforeDeath > 0L) {
         this.getStat().addExp(Math.round((double)(this._expBeforeDeath - this.getStat().getExp()) * restorePercent / 100.0));
         this._expBeforeDeath = 0L;
      }
   }

   private void deathPenalty() {
      int lvl = this.getStat().getLevel();
      double percentLost = -0.07 * (double)lvl + 6.5;
      long lostExp = Math.round((double)(this.getStat().getExpForLevel(lvl + 1) - this.getStat().getExpForLevel(lvl)) * percentLost / 100.0);
      this._expBeforeDeath = this.getStat().getExp();
      this.getStat().addExp(-lostExp);
   }

   @Override
   public void addExpAndSp(long addToExp, int addToSp) {
      if (this.getId() == 12564) {
         this.getStat().addExpAndSp((long)Math.round((float)addToExp * Config.SINEATER_XP_RATE), addToSp);
      } else {
         this.getStat().addExpAndSp((long)Math.round((float)addToExp * Config.PET_XP_RATE), addToSp);
      }
   }

   @Override
   public long getExpForThisLevel() {
      return this.getStat().getExpForLevel(this.getLevel());
   }

   @Override
   public long getExpForNextLevel() {
      return this.getStat().getExpForLevel(this.getLevel() + 1);
   }

   @Override
   public final int getLevel() {
      return this.getStat().getLevel();
   }

   public int getMaxFed() {
      return this.getStat().getMaxFeed();
   }

   @Override
   public double getCriticalHit(Creature target, Skill skill) {
      return this.getStat().getCriticalHit(target, skill);
   }

   @Override
   public double getMAtk(Creature target, Skill skill) {
      return this.getStat().getMAtk(target, skill);
   }

   @Override
   public double getMDef(Creature target, Skill skill) {
      return this.getStat().getMDef(target, skill);
   }

   @Override
   public final int getSkillLevel(int skillId) {
      if (this.getKnownSkill(skillId) == null) {
         return -1;
      } else {
         int lvl = this.getLevel();
         return lvl > 70 ? 7 + (lvl - 70) / 5 : lvl / 10;
      }
   }

   public void updateRefOwner(Player owner) {
      int oldOwnerId = this.getOwner().getObjectId();
      this.setOwner(owner);
      World.getInstance().removePet(oldOwnerId);
      World.getInstance().addPet(oldOwnerId, this);
   }

   public int getInventoryLimit() {
      return Config.INVENTORY_MAXIMUM_PET;
   }

   public void refreshOverloaded() {
      int maxLoad = this.getMaxLoad();
      if (maxLoad > 0) {
         long weightproc = (long)((this.getCurrentLoad() - this.getBonusWeightPenalty()) * 1000 / maxLoad);
         int newWeightPenalty;
         if (weightproc < 500L || this.getOwner().getDietMode()) {
            newWeightPenalty = 0;
         } else if (weightproc < 666L) {
            newWeightPenalty = 1;
         } else if (weightproc < 800L) {
            newWeightPenalty = 2;
         } else if (weightproc < 1000L) {
            newWeightPenalty = 3;
         } else {
            newWeightPenalty = 4;
         }

         if (this._curWeightPenalty != newWeightPenalty) {
            this._curWeightPenalty = newWeightPenalty;
            if (newWeightPenalty > 0) {
               this.addSkill(SkillsParser.getInstance().getInfo(4270, newWeightPenalty));
               this.setIsOverloaded(this.getCurrentLoad() >= maxLoad);
            } else {
               this.removeSkill(this.getKnownSkill(4270), true);
               this.setIsOverloaded(false);
            }
         }
      }
   }

   @Override
   public void updateAndBroadcastStatus(int val) {
      this.refreshOverloaded();
      super.updateAndBroadcastStatus(val);
   }

   @Override
   public final boolean isHungry() {
      return (float)this.getCurrentFed() < (float)this.getPetData().getHungryLimit() / 100.0F * (float)this.getPetLevelData().getPetMaxFeed();
   }

   @Override
   public final int getWeapon() {
      ItemInstance weapon = this.getInventory().getPaperdollItem(5);
      return weapon != null ? weapon.getId() : 0;
   }

   @Override
   public final int getArmor() {
      ItemInstance weapon = this.getInventory().getPaperdollItem(6);
      return weapon != null ? weapon.getId() : 0;
   }

   public final int getJewel() {
      ItemInstance weapon = this.getInventory().getPaperdollItem(4);
      return weapon != null ? weapon.getId() : 0;
   }

   @Override
   public int getSoulShotsPerHit() {
      return this.getPetLevelData().getPetSoulShot();
   }

   @Override
   public int getSpiritShotsPerHit() {
      return this.getPetLevelData().getPetSpiritShot();
   }

   @Override
   public void setName(String name) {
      ItemInstance controlItem = this.getControlItem();
      if (controlItem != null) {
         if (controlItem.getCustomType2() == (name == null ? 1 : 0)) {
            controlItem.setCustomType2(name != null ? 1 : 0);
            controlItem.updateDatabase();
            InventoryUpdate iu = new InventoryUpdate();
            iu.addModifiedItem(controlItem);
            this.sendPacket(iu);
         }
      } else {
         _log.log(Level.WARNING, "Pet control item null, for pet: " + this.toString());
      }

      super.setName(name);
   }

   public boolean canEatFoodId(int itemId) {
      return this._data.getFood().contains(itemId);
   }

   public Map<Integer, TimeStamp> getSkillReuseTimeStamps() {
      return this._reuseTimeStampsSkills;
   }

   @Override
   public void addTimeStamp(Skill skill, long reuse) {
      this._reuseTimeStampsSkills.put(skill.getReuseHashCode(), new TimeStamp(skill, reuse));
   }

   @Override
   public long getSkillRemainingReuseTime(int skillReuseHashId) {
      return !this._reuseTimeStampsSkills.isEmpty() && this._reuseTimeStampsSkills.containsKey(skillReuseHashId)
         ? this._reuseTimeStampsSkills.get(skillReuseHashId).getRemaining()
         : -1L;
   }

   @Override
   public void addTimeStampItem(ItemInstance item, long reuse, boolean byCron) {
      this._reuseTimeStampsItems.put(item.getObjectId(), new TimeStamp(item, reuse, byCron));
   }

   @Override
   public long getItemRemainingReuseTime(int itemObjId) {
      return !this._reuseTimeStampsItems.isEmpty() && this._reuseTimeStampsItems.containsKey(itemObjId)
         ? this._reuseTimeStampsItems.get(itemObjId).getRemaining()
         : -1L;
   }

   @Override
   public boolean isPet() {
      return true;
   }

   @Override
   public int getMaxLoad() {
      return (int)this.calcStat(Stats.WEIGHT_LIMIT, Math.floor(BaseStats.CON.calcBonus(this) * 34500.0 * Config.ALT_WEIGHT_LIMIT), this, null);
   }

   @Override
   public int getBonusWeightPenalty() {
      return (int)this.calcStat(Stats.WEIGHT_PENALTY, 1.0, this, null);
   }

   @Override
   public int getCurrentLoad() {
      return this.getInventory().getTotalWeight();
   }

   @Override
   public double getLevelMod() {
      return (89.0 + (double)this.getLevel()) / 100.0;
   }

   public boolean isUncontrollable() {
      return this.getCurrentFed() <= 0;
   }

   class FeedTask implements Runnable {
      @Override
      public void run() {
         try {
            if (PetInstance.this.getOwner() == null
               || !PetInstance.this.getOwner().hasSummon()
               || PetInstance.this.getOwner().getSummon().getObjectId() != PetInstance.this.getObjectId()) {
               PetInstance.this.stopFeed();
               return;
            }

            if (PetInstance.this.getCurrentFed() > this.getFeedConsume()) {
               PetInstance.this.setCurrentFed(PetInstance.this.getCurrentFed() - this.getFeedConsume());
            } else {
               PetInstance.this.setCurrentFed(0);
            }

            PetInstance.this.broadcastStatusUpdate();
            List<Integer> foodIds = PetInstance.this.getPetData().getFood();
            if (foodIds.isEmpty()) {
               if (PetInstance.this.isUncontrollable()) {
                  if (PetInstance.this.getTemplate().getId() == 16050 && PetInstance.this.getOwner() != null) {
                     PetInstance.this.getOwner().setPkKills(Math.max(0, PetInstance.this.getOwner().getPkKills() - Rnd.get(1, 6)));
                  }

                  PetInstance.this.sendPacket(SystemMessageId.THE_HELPER_PET_LEAVING);
                  PetInstance.this.deleteMe(PetInstance.this.getOwner());
               } else if (PetInstance.this.isHungry()) {
                  PetInstance.this.sendPacket(SystemMessageId.THERE_NOT_MUCH_TIME_REMAINING_UNTIL_HELPER_LEAVES);
               }

               return;
            }

            ItemInstance food = null;

            for(int id : foodIds) {
               food = PetInstance.this.getInventory().getItemByItemId(id);
               if (food != null) {
                  break;
               }
            }

            if (food != null && PetInstance.this.isHungry()) {
               IItemHandler handler = ItemHandler.getInstance().getHandler(food.getEtcItem());
               if (handler != null) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PET_TOOK_S1_BECAUSE_HE_WAS_HUNGRY);
                  sm.addItemName(food.getId());
                  PetInstance.this.sendPacket(sm);
                  handler.useItem(PetInstance.this, food, false);
               }
            }

            if (PetInstance.this.isUncontrollable()) {
               PetInstance.this.sendPacket(SystemMessageId.YOUR_PET_IS_STARVING_AND_WILL_NOT_OBEY_UNTIL_IT_GETS_ITS_FOOD_FEED_YOUR_PET);
            }
         } catch (Exception var5) {
            PetInstance._logPet.log(Level.SEVERE, "Pet [ObjectId: " + PetInstance.this.getObjectId() + "] a feed task error has occurred", (Throwable)var5);
         }
      }

      private int getFeedConsume() {
         return PetInstance.this.isAttackingNow()
            ? PetInstance.this.getPetLevelData().getPetFeedBattle()
            : PetInstance.this.getPetLevelData().getPetFeedNormal();
      }
   }
}
