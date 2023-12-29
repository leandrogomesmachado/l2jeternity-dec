package l2e.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.ShortCutTemplate;
import l2e.gameserver.model.base.ShortcutType;
import l2e.gameserver.model.interfaces.IRestorable;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.type.EtcItemType;
import l2e.gameserver.network.serverpackets.ExAutoSoulShot;
import l2e.gameserver.network.serverpackets.ShortCutInit;
import l2e.gameserver.network.serverpackets.ShortCutRegister;

public class ShortCuts implements IRestorable {
   private static Logger _log = Logger.getLogger(ShortCuts.class.getName());
   private static final int MAX_SHORTCUTS_PER_BAR = 12;
   private final Player _owner;
   private final Map<Integer, ShortCutTemplate> _shortCuts = new TreeMap<>();

   public ShortCuts(Player owner) {
      this._owner = owner;
   }

   public ShortCutTemplate[] getAllShortCuts() {
      return this._shortCuts.values().toArray(new ShortCutTemplate[this._shortCuts.values().size()]);
   }

   public ShortCutTemplate getShortCut(int slot, int page) {
      ShortCutTemplate sc = this._shortCuts.get(slot + page * 12);
      if (sc != null && sc.getType() == ShortcutType.ITEM && this._owner.getInventory().getItemByObjectId(sc.getId()) == null) {
         this.deleteShortCut(sc.getSlot(), sc.getPage());
         sc = null;
      }

      return sc;
   }

   public synchronized void registerShortCut(ShortCutTemplate shortcut) {
      if (shortcut.getType() == ShortcutType.ITEM) {
         ItemInstance item = this._owner.getInventory().getItemByObjectId(shortcut.getId());
         if (item == null) {
            return;
         }

         shortcut.setSharedReuseGroup(item.getSharedReuseGroup());
         if (item.getSharedReuseGroup() > 0) {
            TimeStamp timeStamp = this._owner.getSharedItemReuse(item.getObjectId());
            if (timeStamp != null) {
               shortcut.setCurrenReuse((int)(timeStamp.getRemaining() / 1000L));
               shortcut.setReuse((int)(timeStamp.getReuseBasic() / 1000L));
            }
         }

         if (item.getAugmentation() != null) {
            shortcut.setAugmentationId(item.getAugmentation().getAugmentationId());
         } else {
            shortcut.setAugmentationId(0);
         }
      }

      ShortCutTemplate oldShortCut = this._shortCuts.put(shortcut.getSlot() + shortcut.getPage() * 12, shortcut);
      this.registerShortCutInDb(shortcut, oldShortCut);
   }

   public synchronized void registerShortCut(ShortCutTemplate shortcut, boolean storeToDb) {
      if (shortcut.getType() == ShortcutType.ITEM) {
         ItemInstance item = this._owner.getInventory().getItemByObjectId(shortcut.getId());
         if (item == null) {
            return;
         }

         if (item.isEtcItem()) {
            shortcut.setSharedReuseGroup(item.getEtcItem().getSharedReuseGroup());
            if (item.getEtcItem().getSharedReuseGroup() > 0) {
               TimeStamp timeStamp = this._owner.getSharedItemReuse(item.getObjectId());
               if (timeStamp != null) {
                  shortcut.setCurrenReuse((int)(timeStamp.getRemaining() / 1000L));
                  shortcut.setReuse((int)(timeStamp.getReuseBasic() / 1000L));
               }
            }

            if (item.getAugmentation() != null) {
               shortcut.setAugmentationId(item.getAugmentation().getAugmentationId());
            } else {
               shortcut.setAugmentationId(0);
            }
         }
      }

      ShortCutTemplate oldShortCut = this._shortCuts.put(shortcut.getSlot() + 12 * shortcut.getPage(), shortcut);
      if (storeToDb) {
         this.registerShortCutInDb(shortcut, oldShortCut);
      }
   }

   private void registerShortCutInDb(ShortCutTemplate shortcut, ShortCutTemplate oldShortCut) {
      if (oldShortCut != null) {
         this.deleteShortCutFromDb(oldShortCut);
      }

      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement(
            "REPLACE INTO character_shortcuts (charId,slot,page,type,shortcut_id,level,class_index) values(?,?,?,?,?,?,?)"
         );
         statement.setInt(1, this._owner.getObjectId());
         statement.setInt(2, shortcut.getSlot());
         statement.setInt(3, shortcut.getPage());
         statement.setInt(4, shortcut.getType().ordinal());
         statement.setInt(5, shortcut.getId());
         statement.setInt(6, shortcut.getLevel());
         statement.setInt(7, this._owner.getClassIndex());
         statement.execute();
         statement.close();
      } catch (Exception var16) {
         _log.log(Level.WARNING, "Could not store character shortcut: " + var16.getMessage(), (Throwable)var16);
      }
   }

   public synchronized void deleteShortCut(int slot, int page, boolean fromDb) {
      ShortCutTemplate old = this._shortCuts.remove(slot + page * 12);
      if (old != null && this._owner != null) {
         if (fromDb) {
            this.deleteShortCutFromDb(old);
         }

         if (old.getType() == ShortcutType.ITEM) {
            ItemInstance item = this._owner.getInventory().getItemByObjectId(old.getId());
            if (item != null && item.getItemType() == EtcItemType.SHOT && this._owner.removeAutoSoulShot(item.getId())) {
               this._owner.sendPacket(new ExAutoSoulShot(item.getId(), 0));
            }
         }

         this._owner.sendPacket(new ShortCutInit(this._owner));

         for(int shotId : this._owner.getAutoSoulShot()) {
            this._owner.sendPacket(new ExAutoSoulShot(shotId, 1));
         }
      }
   }

   public synchronized void deleteShortCut(int slot, int page) {
      ShortCutTemplate old = this._shortCuts.remove(slot + page * 12);
      if (old != null && this._owner != null) {
         this.deleteShortCutFromDb(old);
         if (old.getType() == ShortcutType.ITEM) {
            ItemInstance item = this._owner.getInventory().getItemByObjectId(old.getId());
            if (item != null && item.getItemType() == EtcItemType.SHOT && this._owner.removeAutoSoulShot(item.getId())) {
               this._owner.sendPacket(new ExAutoSoulShot(item.getId(), 0));
            }
         }

         this._owner.sendPacket(new ShortCutInit(this._owner));

         for(int shotId : this._owner.getAutoSoulShot()) {
            this._owner.sendPacket(new ExAutoSoulShot(shotId, 1));
         }
      }
   }

   public synchronized void deleteShortCutByObjectId(int objectId) {
      for(ShortCutTemplate shortcut : this._shortCuts.values()) {
         if (shortcut.getType() == ShortcutType.ITEM && shortcut.getId() == objectId) {
            this.deleteShortCut(shortcut.getSlot(), shortcut.getPage());
            break;
         }
      }
   }

   private void deleteShortCutFromDb(ShortCutTemplate shortcut) {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE charId=? AND slot=? AND page=? AND class_index=?");
         statement.setInt(1, this._owner.getObjectId());
         statement.setInt(2, shortcut.getSlot());
         statement.setInt(3, shortcut.getPage());
         statement.setInt(4, this._owner.getClassIndex());
         statement.execute();
         statement.close();
      } catch (Exception var15) {
         _log.log(Level.WARNING, "Could not delete character shortcut: " + var15.getMessage(), (Throwable)var15);
      }
   }

   @Override
   public boolean restoreMe() {
      this._shortCuts.clear();

      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement(
            "SELECT charId, slot, page, type, shortcut_id, level FROM character_shortcuts WHERE charId=? AND class_index=?"
         );
         statement.setInt(1, this._owner.getObjectId());
         statement.setInt(2, this._owner.getClassIndex());
         ResultSet rset = statement.executeQuery();

         while(rset.next()) {
            int slot = rset.getInt("slot");
            int page = rset.getInt("page");
            int type = rset.getInt("type");
            int id = rset.getInt("shortcut_id");
            int level = rset.getInt("level");
            ShortCutTemplate sc = new ShortCutTemplate(slot, page, ShortcutType.values()[type], id, level, 1);
            this._shortCuts.put(slot + page * 12, sc);
         }

         rset.close();
         statement.close();
      } catch (Exception var21) {
         _log.log(Level.WARNING, "Could not restore character shortcuts: " + var21.getMessage(), (Throwable)var21);
         return false;
      }

      for(ShortCutTemplate sc : this.getAllShortCuts()) {
         if (sc.getType() == ShortcutType.ITEM) {
            ItemInstance item = this._owner.getInventory().getItemByObjectId(sc.getId());
            if (item == null) {
               this.deleteShortCut(sc.getSlot(), sc.getPage());
            } else if (item.isEtcItem()) {
               sc.setSharedReuseGroup(item.getEtcItem().getSharedReuseGroup());
               if (item.getEtcItem().getSharedReuseGroup() > 0) {
                  TimeStamp timeStamp = this._owner.getSharedItemReuse(item.getObjectId());
                  if (timeStamp != null) {
                     sc.setCurrenReuse((int)(timeStamp.getRemaining() / 1000L));
                     sc.setReuse((int)(timeStamp.getReuseBasic() / 1000L));
                  }
               }

               if (item.getAugmentation() != null) {
                  sc.setAugmentationId(item.getAugmentation().getAugmentationId());
               } else {
                  sc.setAugmentationId(0);
               }
            }
         }
      }

      return true;
   }

   public synchronized void updateShortCuts(int skillId, int skillLevel) {
      for(ShortCutTemplate sc : this._shortCuts.values()) {
         if (sc.getId() == skillId && sc.getType() == ShortcutType.SKILL) {
            ShortCutTemplate newsc = new ShortCutTemplate(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), skillLevel, 1);
            this._owner.sendPacket(new ShortCutRegister(newsc));
            this._owner.registerShortCut(newsc);
         }
      }
   }

   public synchronized void updateShortCuts(int objId, ShortcutType type) {
      for(ShortCutTemplate sc : this._shortCuts.values()) {
         if (sc.getId() == objId && sc.getType() == type) {
            ShortCutTemplate newsc = new ShortCutTemplate(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), sc.getLevel(), sc.getCharacterType());
            this._owner.sendPacket(new ShortCutRegister(newsc));
            this._owner.registerShortCut(newsc);
         }
      }
   }

   public synchronized void tempRemoveAll() {
      this._shortCuts.clear();
   }
}
