package l2e.gameserver.handler.itemhandlers.impl;

import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.DressArmorParser;
import l2e.gameserver.data.parser.DressCloakParser;
import l2e.gameserver.data.parser.DressHatParser;
import l2e.gameserver.data.parser.DressShieldParser;
import l2e.gameserver.data.parser.DressWeaponParser;
import l2e.gameserver.handler.itemhandlers.IItemHandler;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.DressArmorTemplate;
import l2e.gameserver.model.actor.templates.DressCloakTemplate;
import l2e.gameserver.model.actor.templates.DressHatTemplate;
import l2e.gameserver.model.actor.templates.DressShieldTemplate;
import l2e.gameserver.model.actor.templates.DressWeaponTemplate;
import l2e.gameserver.model.actor.templates.items.EtcItem;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.MagicSkillUse;

public class VisualItems implements IItemHandler {
   private static Logger _log = Logger.getLogger(VisualItems.class.getName());

   @Override
   public boolean useItem(Playable playable, ItemInstance item, boolean forceUse) {
      if (!Config.ALLOW_VISUAL_SYSTEM) {
         return false;
      } else if (!playable.isPlayer()) {
         playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
         return false;
      } else {
         Player player = playable.getActingPlayer();
         EtcItem etcitem = (EtcItem)item.getItem();
         if (etcitem.getSkinType() != null && !etcitem.getSkinType().isEmpty() && etcitem.getSkinId() != 0) {
            String activated = etcitem.getSkinType();
            switch(activated) {
               case "Weapon":
                  DressWeaponTemplate weapon = DressWeaponParser.getInstance().getWeapon(etcitem.getSkinId());
                  if (weapon != null && player.getWeaponSkins().contains(weapon.getId())) {
                     _log.info("You already have this skin!");
                     return false;
                  }
                  break;
               case "Armor":
                  DressArmorTemplate armor = DressArmorParser.getInstance().getArmor(etcitem.getSkinId());
                  if (armor != null && player.getArmorSkins().contains(armor.getId())) {
                     _log.info("You already have this skin!");
                     return false;
                  }
                  break;
               case "Shield":
                  DressShieldTemplate shield = DressShieldParser.getInstance().getShield(etcitem.getSkinId());
                  if (shield != null && player.getShieldSkins().contains(shield.getId())) {
                     _log.info("You already have this skin!");
                     return false;
                  }
                  break;
               case "Cloak":
                  DressCloakTemplate cloak = DressCloakParser.getInstance().getCloak(etcitem.getSkinId());
                  if (cloak != null && player.getCloakSkins().contains(cloak.getId())) {
                     _log.info("You already have this skin!");
                     return false;
                  }
                  break;
               case "Hair":
                  DressHatTemplate hat = DressHatParser.getInstance().getHat(etcitem.getSkinId());
                  if (hat != null && player.getHairSkins().contains(hat.getId())) {
                     _log.info("You already have this skin!");
                     return false;
                  }
            }

            if (!player.destroyItem("Visual", item.getObjectId(), 1L, player, true)) {
               return false;
            } else {
               boolean activated = false;
               if (etcitem.getSkinId() > 0) {
                  if (etcitem.getSkinId() > 0) {
                     player.addVisual(etcitem.getSkinType(), etcitem.getSkinId());
                     activated = true;
                  }

                  player.broadcastPacket(new MagicSkillUse(player, player, 22217, 1, 0, 0));
               }

               player.broadcastUserInfo(true);
               if (!activated) {
                  player.sendPacket(SystemMessageId.NOTHING_INSIDE_THAT);
               }

               return true;
            }
         } else {
            _log.info("Not correct visual params for " + etcitem);
            return false;
         }
      }
   }
}
