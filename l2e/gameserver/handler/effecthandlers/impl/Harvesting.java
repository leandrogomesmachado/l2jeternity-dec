package l2e.gameserver.handler.effecthandlers.impl;

import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.holders.ItemHolder;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class Harvesting extends Effect {
   public Harvesting(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Deprecated
   @Override
   public boolean onStart() {
      if (this.getEffector() != null
         && this.getEffected() != null
         && this.getEffector().isPlayer()
         && this.getEffected().isNpc()
         && this.getEffected().isDead()) {
         Player player = this.getEffector().getActingPlayer();
         GameObject[] targets = this.getSkill().getTargetList(player, false, this.getEffected());
         if (targets != null && targets.length != 0) {
            InventoryUpdate iu = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();

            for(GameObject target : targets) {
               if (target != null && target.isMonster()) {
                  MonsterInstance monster = (MonsterInstance)target;
                  if (player.getObjectId() != monster.getSeederId()) {
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_HARVEST);
                     player.sendPacket(sm);
                  } else if (monster.isSeeded()) {
                     if (this.calcSuccess(player, monster)) {
                        ItemHolder[] items = monster.takeHarvest();
                        if (items != null && items.length > 0) {
                           for(ItemHolder reward : items) {
                              if (reward != null) {
                                 ItemInstance item = player.getInventory().addItem("Harvesting", reward.getId(), reward.getCount(), player, monster);
                                 if (iu != null) {
                                    iu.addItem(item);
                                 }

                                 SystemMessage smsg = null;
                                 if (reward.getCount() > 1L) {
                                    smsg = SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2);
                                    smsg.addItemName(reward.getId());
                                    smsg.addLong(reward.getCount());
                                 } else {
                                    smsg = SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1);
                                    smsg.addItemName(reward.getId());
                                 }

                                 player.sendPacket(smsg);
                                 player.getCounters().addAchivementInfo("takeHarvests", 0, reward.getCount(), false, false, false);
                                 if (player.isInParty()) {
                                    if (reward.getCount() > 1L) {
                                       smsg = SystemMessage.getSystemMessage(SystemMessageId.C1_HARVESTED_S3_S2S);
                                       smsg.addString(player.getName());
                                       smsg.addLong(reward.getCount());
                                       smsg.addItemName(reward.getId());
                                    } else {
                                       smsg = SystemMessage.getSystemMessage(SystemMessageId.C1_HARVESTED_S2S);
                                       smsg.addString(player.getName());
                                       smsg.addItemName(reward.getId());
                                    }

                                    player.getParty().broadcastToPartyMembers(player, smsg);
                                 }

                                 if (iu != null) {
                                    player.sendPacket(iu);
                                 } else {
                                    player.sendItemList(false);
                                 }
                              }
                           }

                           return true;
                        }
                     } else {
                        player.sendPacket(SystemMessageId.THE_HARVEST_HAS_FAILED);
                     }
                  } else {
                     player.sendPacket(SystemMessageId.THE_HARVEST_FAILED_BECAUSE_THE_SEED_WAS_NOT_SOWN);
                  }
               }
            }

            return false;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean calcSuccess(Player activeChar, MonsterInstance target) {
      int basicSuccess = 100;
      int levelPlayer = activeChar.getLevel();
      int levelTarget = target.getLevel();
      int diff = levelPlayer - levelTarget;
      if (diff < 0) {
         diff = -diff;
      }

      if (diff > 5) {
         basicSuccess -= (diff - 5) * 5;
      }

      if (basicSuccess < 1) {
         basicSuccess = 1;
      }

      return Rnd.nextInt(99) < basicSuccess;
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.HARVESTING;
   }
}
