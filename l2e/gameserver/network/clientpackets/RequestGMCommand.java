package l2e.gameserver.network.clientpackets;

import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.serverpackets.ExGMViewQuestItemList;
import l2e.gameserver.network.serverpackets.GMHennaInfo;
import l2e.gameserver.network.serverpackets.GMViewCharacterInfo;
import l2e.gameserver.network.serverpackets.GMViewItemList;
import l2e.gameserver.network.serverpackets.GMViewPledgeInfo;
import l2e.gameserver.network.serverpackets.GMViewSkillInfo;
import l2e.gameserver.network.serverpackets.GMViewWarehouseWithdrawList;
import l2e.gameserver.network.serverpackets.GmViewQuestInfo;

public final class RequestGMCommand extends GameClientPacket {
   private String _targetName;
   private int _command;

   @Override
   protected void readImpl() {
      this._targetName = this.readS();
      this._command = this.readD();
   }

   @Override
   protected void runImpl() {
      if (this.getClient().getActiveChar().isGM() && this.getClient().getActiveChar().getAccessLevel().allowAltG()) {
         Player player = World.getInstance().getPlayer(this._targetName);
         Clan clan = ClanHolder.getInstance().getClanByName(this._targetName);
         if (player != null || clan != null && this._command == 6) {
            switch(this._command) {
               case 1:
                  this.sendPacket(new GMViewCharacterInfo(player));
                  this.sendPacket(new GMHennaInfo(player));
                  break;
               case 2:
                  if (player != null && player.getClan() != null) {
                     this.sendPacket(new GMViewPledgeInfo(player.getClan(), player));
                  }
                  break;
               case 3:
                  this.sendPacket(new GMViewSkillInfo(player));
                  break;
               case 4:
                  this.sendPacket(new GmViewQuestInfo(player));
                  break;
               case 5:
                  ItemInstance[] items = player.getInventory().getItems();
                  int questSize = 0;

                  for(ItemInstance item : items) {
                     if (item.isQuestItem()) {
                        ++questSize;
                     }
                  }

                  this.sendPacket(new GMViewItemList(player, items, items.length - questSize));
                  this.sendPacket(new ExGMViewQuestItemList(player, items, questSize));
                  this.sendPacket(new GMHennaInfo(player));
                  break;
               case 6:
                  if (player != null) {
                     this.sendPacket(new GMViewWarehouseWithdrawList(player));
                  } else {
                     this.sendPacket(new GMViewWarehouseWithdrawList(clan));
                  }
            }
         }
      }
   }
}
