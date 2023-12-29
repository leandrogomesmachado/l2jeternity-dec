package l2e.gameserver.handler.skillhandlers.impl;

import l2e.gameserver.handler.skillhandlers.ISkillHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.Weapon;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.SkillType;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExAutoSoulShot;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.ShortCutInit;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class ConvertItem implements ISkillHandler {
   private static final SkillType[] SKILL_IDS = new SkillType[]{SkillType.CONVERT_ITEM};

   @Override
   public void useSkill(Creature activeChar, Skill skill, GameObject[] targets) {
      if (!activeChar.isAlikeDead() && activeChar.isPlayer()) {
         Player player = activeChar.getActingPlayer();
         if (!player.isEnchanting()) {
            ItemInstance item = activeChar.getActiveWeaponInstance();
            if (item != null) {
               int itemId = ((Weapon)item.getItem()).getChangeWeaponId();
               if (itemId != 0) {
                  player.getInventory().unEquipItem(item);
                  InventoryUpdate u = new InventoryUpdate();
                  u.addRemovedItem(item);
                  player.sendPacket(u);
                  item.setItemId(itemId);
                  player.sendPacket(new ShortCutInit(player));

                  for(int shotId : player.getAutoSoulShot()) {
                     player.sendPacket(new ExAutoSoulShot(shotId, 1));
                  }

                  InventoryUpdate ui = new InventoryUpdate();
                  ui.addNewItem(item);
                  player.sendPacket(ui);
                  SystemMessage msg;
                  if (item.getEnchantLevel() > 0) {
                     msg = SystemMessage.getSystemMessage(SystemMessageId.S1_S2_EQUIPPED);
                     msg.addNumber(item.getEnchantLevel());
                     msg.addItemName(item);
                  } else {
                     msg = SystemMessage.getSystemMessage(SystemMessageId.S1_EQUIPPED);
                     msg.addItemName(item);
                  }

                  player.sendPacket(msg);
                  player.getInventory().equipItem(item);
                  player.broadcastUserInfo(true);
                  player.sendItemList(false);
               }
            }
         }
      }
   }

   @Override
   public SkillType[] getSkillIds() {
      return SKILL_IDS;
   }
}
