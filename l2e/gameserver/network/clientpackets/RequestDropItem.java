package l2e.gameserver.network.clientpackets;

import l2e.commons.util.GMAudit;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.AdminParser;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.type.EtcItemType;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.InventoryUpdate;

public final class RequestDropItem extends GameClientPacket {
   private int _objectId;
   private long _count;
   private int _x;
   private int _y;
   private int _z;

   @Override
   protected void readImpl() {
      this._objectId = this.readD();
      this._count = this.readQ();
      this._x = this.readD();
      this._y = this.readD();
      this._z = this.readD();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null && !activeChar.isDead()) {
         if (activeChar.isActionsDisabled()) {
            activeChar.sendActionFailed();
         } else {
            ItemInstance item = activeChar.getInventory().getItemByObjectId(this._objectId);
            if (item == null
               || this._count == 0L
               || !activeChar.validateItemManipulation(this._objectId, "drop")
               || !Config.ALLOW_DISCARDITEM
                  && !activeChar.canOverrideCond(PcCondOverride.DROP_ALL_ITEMS)
                  && !Config.LIST_DISCARDITEM_ITEMS.contains(item.getId())
               || !item.isDropable() && (!activeChar.canOverrideCond(PcCondOverride.DROP_ALL_ITEMS) || !Config.GM_TRADE_RESTRICTED_ITEMS)
               || item.getItemType() == EtcItemType.PET_COLLAR && activeChar.havePetInvItems()
               || activeChar.isInsideZone(ZoneId.NO_ITEM_DROP)) {
               activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
            } else if (!item.isQuestItem() || activeChar.canOverrideCond(PcCondOverride.DROP_ALL_ITEMS) && Config.GM_TRADE_RESTRICTED_ITEMS) {
               if (this._count > item.getCount()) {
                  activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
               } else if (Config.PLAYER_SPAWN_PROTECTION > 0 && activeChar.isInvul() && !activeChar.isGM()) {
                  activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
               } else if (this._count < 0L) {
                  Util.handleIllegalPlayerAction(
                     activeChar,
                     ""
                        + activeChar.getName()
                        + " of account "
                        + activeChar.getAccountName()
                        + " tried to drop item with oid "
                        + this._objectId
                        + " but has count < 0!"
                  );
               } else if (!item.isStackable() && this._count > 1L) {
                  Util.handleIllegalPlayerAction(
                     activeChar,
                     ""
                        + activeChar.getName()
                        + " of account "
                        + activeChar.getAccountName()
                        + " tried to drop non-stackable item with oid "
                        + this._objectId
                        + " but has count > 1!"
                  );
               } else if (activeChar.isJailed()) {
                  activeChar.sendMessage("You cannot drop items in Jail.");
               } else if (!activeChar.getAccessLevel().allowTransaction()) {
                  activeChar.sendMessage("Transactions are disabled for your Access Level.");
                  activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
               } else if (activeChar.isProcessingTransaction() || activeChar.getPrivateStoreType() != 0) {
                  activeChar.sendPacket(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
               } else if (activeChar.isFishing()) {
                  activeChar.sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_2);
               } else if (!activeChar.isFlying()) {
                  if (activeChar.isCastingNow()
                     && activeChar.getCurrentSkill() != null
                     && activeChar.getCurrentSkill().getSkill().getItemConsumeId() == item.getId()) {
                     activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
                  } else if (activeChar.isCastingSimultaneouslyNow()
                     && activeChar.getCastingSkill() != null
                     && activeChar.getCastingSkill().getItemConsumeId() == item.getId()) {
                     activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
                  } else if (3 == item.getItem().getType2() && !activeChar.canOverrideCond(PcCondOverride.DROP_ALL_ITEMS)) {
                     if (Config.DEBUG) {
                        _log.finest(activeChar.getObjectId() + ":player tried to drop quest item");
                     }

                     activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_EXCHANGE_ITEM);
                  } else if (activeChar.isInsideRadius(this._x, this._y, 150, false) && Math.abs(this._z - activeChar.getZ()) <= 50) {
                     if (!activeChar.getInventory().canManipulateWithItemId(item.getId())) {
                        activeChar.sendMessage("You cannot use this item.");
                     } else {
                        if (Config.DEBUG) {
                           _log.fine("requested drop item " + this._objectId + "(" + item.getCount() + ") at " + this._x + "/" + this._y + "/" + this._z);
                        }

                        if (item.isEquipped()) {
                           ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInSlotAndRecord(item.getLocationSlot());
                           InventoryUpdate iu = new InventoryUpdate();

                           for(ItemInstance itm : unequiped) {
                              itm.unChargeAllShots();
                              iu.addModifiedItem(itm);
                           }

                           activeChar.sendPacket(iu);
                           activeChar.broadcastCharInfo();
                           activeChar.sendItemList(true);
                        }

                        ItemInstance dropedItem = activeChar.dropItem("Drop", this._objectId, this._count, this._x, this._y, this._z, null, false, false);
                        if (Config.DEBUG) {
                           _log.fine("dropping " + this._objectId + " item(" + this._count + ") at: " + this._x + " " + this._y + " " + this._z);
                        }

                        if (activeChar.isGM()) {
                           String target = activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target";
                           GMAudit.auditGMAction(
                              activeChar.getName() + " [" + activeChar.getObjectId() + "]",
                              "Drop",
                              target,
                              "(id: "
                                 + dropedItem.getId()
                                 + " name: "
                                 + dropedItem.getItem().getNameEn()
                                 + " objId: "
                                 + dropedItem.getObjectId()
                                 + " x: "
                                 + activeChar.getX()
                                 + " y: "
                                 + activeChar.getY()
                                 + " z: "
                                 + activeChar.getZ()
                                 + ")"
                           );
                        }

                        if (dropedItem != null && dropedItem.getId() == 57 && dropedItem.getCount() >= 1000000L) {
                           String msg = "Character ("
                              + activeChar.getName()
                              + ") has dropped ("
                              + dropedItem.getCount()
                              + ")adena at ("
                              + this._x
                              + ","
                              + this._y
                              + ","
                              + this._z
                              + ")";
                           _log.warning(msg);
                           AdminParser.getInstance().broadcastMessageToGMs(msg);
                        }
                     }
                  } else {
                     if (Config.DEBUG) {
                        _log.finest(activeChar.getObjectId() + ": trying to drop too far away");
                     }

                     activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_DISTANCE_TOO_FAR);
                  }
               }
            }
         }
      }
   }

   @Override
   protected boolean triggersOnActionRequest() {
      return false;
   }
}
