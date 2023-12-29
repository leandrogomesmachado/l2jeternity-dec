package l2e.commons.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.data.parser.PetsParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.PetData;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.PetInstance;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.MagicSkillLaunched;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class Evolve {
   public static final Logger _log = Logger.getLogger(Evolve.class.getName());

   public static final boolean doEvolve(Player player, Npc npc, int itemIdtake, int itemIdgive, int petminlvl) {
      if (itemIdtake == 0 || itemIdgive == 0 || petminlvl == 0) {
         return false;
      } else if (!player.hasPet()) {
         return false;
      } else {
         PetInstance currentPet = (PetInstance)player.getSummon();
         if (currentPet.isAlikeDead()) {
            Util.handleIllegalPlayerAction(player, "" + player.getName() + " tried to use death pet exploit!");
            return false;
         } else {
            ItemInstance item = null;
            long petexp = currentPet.getStat().getExp();
            String oldname = currentPet.getName();
            int oldX = currentPet.getX();
            int oldY = currentPet.getY();
            int oldZ = currentPet.getZ();
            PetData oldData = PetsParser.getInstance().getPetDataByItemId(itemIdtake);
            if (oldData == null) {
               return false;
            } else {
               int oldnpcID = oldData.getNpcId();
               if (currentPet.getStat().getLevel() >= petminlvl && currentPet.getId() == oldnpcID) {
                  PetData petData = PetsParser.getInstance().getPetDataByItemId(itemIdgive);
                  if (petData == null) {
                     return false;
                  } else {
                     int npcID = petData.getNpcId();
                     if (npcID == 0) {
                        return false;
                     } else {
                        NpcTemplate npcTemplate = NpcsParser.getInstance().getTemplate(npcID);
                        currentPet.unSummon(player);
                        currentPet.destroyControlItem(player, true);
                        item = player.getInventory().addItem("Evolve", itemIdgive, 1L, player, npc);
                        PetInstance petSummon = PetInstance.spawnPet(npcTemplate, player, item);
                        if (petSummon == null) {
                           return false;
                        } else {
                           long _minimumexp = petSummon.getStat().getExpForLevel(petminlvl);
                           if (petexp < _minimumexp) {
                              petexp = _minimumexp;
                           }

                           petSummon.getStat().addExp(petexp);
                           petSummon.setCurrentHp(petSummon.getMaxHp());
                           petSummon.setCurrentMp(petSummon.getMaxMp());
                           petSummon.setCurrentFed(petSummon.getMaxFed());
                           petSummon.setTitle(player.getName());
                           petSummon.setName(oldname);
                           petSummon.setRunning();
                           petSummon.store();
                           player.setPet(petSummon);
                           player.sendPacket(new MagicSkillUse(npc, 2046, 1, 1000, 600000));
                           player.sendPacket(SystemMessageId.SUMMON_A_PET);
                           petSummon.spawnMe(oldX, oldY, oldZ);
                           petSummon.startFeed();
                           item.setEnchantLevel(petSummon.getLevel());
                           ThreadPoolManager.getInstance().schedule(new Evolve.EvolveFinalizer(player, petSummon), 900L);
                           if (petSummon.getCurrentFed() <= 0) {
                              ThreadPoolManager.getInstance().schedule(new Evolve.EvolveFeedWait(player, petSummon), 60000L);
                           } else {
                              petSummon.startFeed();
                           }

                           return true;
                        }
                     }
                  }
               } else {
                  return false;
               }
            }
         }
      }
   }

   public static final boolean doRestore(Player player, Npc npc, int itemIdtake, int itemIdgive, int petminlvl) {
      if (itemIdtake != 0 && itemIdgive != 0 && petminlvl != 0) {
         ItemInstance item = player.getInventory().getItemByItemId(itemIdtake);
         if (item == null) {
            return false;
         } else {
            int oldpetlvl = item.getEnchantLevel();
            if (oldpetlvl < petminlvl) {
               oldpetlvl = petminlvl;
            }

            PetData oldData = PetsParser.getInstance().getPetDataByItemId(itemIdtake);
            if (oldData == null) {
               return false;
            } else {
               PetData petData = PetsParser.getInstance().getPetDataByItemId(itemIdgive);
               if (petData == null) {
                  return false;
               } else {
                  int npcId = petData.getNpcId();
                  if (npcId == 0) {
                     return false;
                  } else {
                     NpcTemplate npcTemplate = NpcsParser.getInstance().getTemplate(npcId);
                     ItemInstance removedItem = player.getInventory().destroyItem("PetRestore", item, player, npc);
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
                     sm.addItemName(removedItem);
                     player.sendPacket(sm);
                     ItemInstance addedItem = player.getInventory().addItem("PetRestore", itemIdgive, 1L, player, npc);
                     PetInstance petSummon = PetInstance.spawnPet(npcTemplate, player, addedItem);
                     if (petSummon == null) {
                        return false;
                     } else {
                        long _maxexp = petSummon.getStat().getExpForLevel(oldpetlvl);
                        petSummon.getStat().addExp(_maxexp);
                        petSummon.setCurrentHp(petSummon.getMaxHp());
                        petSummon.setCurrentMp(petSummon.getMaxMp());
                        petSummon.setCurrentFed(petSummon.getMaxFed());
                        petSummon.setTitle(player.getName());
                        petSummon.setRunning();
                        petSummon.store();
                        player.setPet(petSummon);
                        player.sendPacket(new MagicSkillUse(npc, 2046, 1, 1000, 600000));
                        player.sendPacket(SystemMessageId.SUMMON_A_PET);
                        petSummon.spawnMe(player.getX(), player.getY(), player.getZ());
                        petSummon.startFeed();
                        addedItem.setEnchantLevel(petSummon.getLevel());
                        InventoryUpdate iu = new InventoryUpdate();
                        iu.addRemovedItem(removedItem);
                        player.sendPacket(iu);
                        StatusUpdate su = new StatusUpdate(player);
                        su.addAttribute(14, player.getCurrentLoad());
                        player.sendPacket(su);
                        player.broadcastCharInfo();
                        World world = World.getInstance();
                        world.removeObject(removedItem);
                        ThreadPoolManager.getInstance().schedule(new Evolve.EvolveFinalizer(player, petSummon), 900L);
                        if (petSummon.getCurrentFed() <= 0) {
                           ThreadPoolManager.getInstance().schedule(new Evolve.EvolveFeedWait(player, petSummon), 60000L);
                        } else {
                           petSummon.startFeed();
                        }

                        try (
                           Connection con = DatabaseFactory.getInstance().getConnection();
                           PreparedStatement ps = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?");
                        ) {
                           ps.setInt(1, removedItem.getObjectId());
                           ps.execute();
                        } catch (Exception var52) {
                        }

                        return true;
                     }
                  }
               }
            }
         }
      } else {
         return false;
      }
   }

   static final class EvolveFeedWait implements Runnable {
      private final Player _activeChar;
      private final PetInstance _petSummon;

      EvolveFeedWait(Player activeChar, PetInstance petSummon) {
         this._activeChar = activeChar;
         this._petSummon = petSummon;
      }

      @Override
      public void run() {
         try {
            if (this._petSummon.getCurrentFed() <= 0) {
               this._petSummon.unSummon(this._activeChar);
            } else {
               this._petSummon.startFeed();
            }
         } catch (Exception var2) {
            Evolve._log.log(Level.WARNING, "", (Throwable)var2);
         }
      }
   }

   static final class EvolveFinalizer implements Runnable {
      private final Player _activeChar;
      private final PetInstance _petSummon;

      EvolveFinalizer(Player activeChar, PetInstance petSummon) {
         this._activeChar = activeChar;
         this._petSummon = petSummon;
      }

      @Override
      public void run() {
         try {
            this._activeChar.sendPacket(new MagicSkillLaunched(this._activeChar, 2046, 1));
            this._petSummon.setFollowStatus(true);
            this._petSummon.setShowSummonAnimation(false);
         } catch (Throwable var2) {
            Evolve._log.log(Level.WARNING, "", var2);
         }
      }
   }
}
