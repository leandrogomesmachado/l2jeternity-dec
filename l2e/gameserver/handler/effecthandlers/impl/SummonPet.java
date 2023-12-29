package l2e.gameserver.handler.effecthandlers.impl;

import java.util.logging.Level;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.data.parser.PetsParser;
import l2e.gameserver.model.PetData;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.PetInstance;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.holders.PetItemHolder;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.PetItemList;

public class SummonPet extends Effect {
   public SummonPet(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.SUMMON_PET;
   }

   @Override
   public boolean onStart() {
      if (this.getEffector() != null
         && this.getEffected() != null
         && this.getEffector().isPlayer()
         && this.getEffected().isPlayer()
         && !this.getEffected().isAlikeDead()) {
         Player player = this.getEffector().getActingPlayer();
         if (player.isInOlympiadMode()) {
            player.sendPacket(SystemMessageId.THIS_SKILL_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
            return false;
         } else if (player.isInFightEvent()) {
            player.sendMessage("You can not use that item in Fight Event!");
            return false;
         } else if (!player.hasSummon() && !player.isMounted()) {
            PetItemHolder holder = player.removeScript(PetItemHolder.class);
            if (holder == null) {
               _log.log(Level.WARNING, "Summoning pet without attaching PetItemHandler!", new Throwable());
               return false;
            } else {
               ItemInstance item = holder.getItem();
               if (player.getInventory().getItemByObjectId(item.getObjectId()) != item) {
                  _log.log(Level.WARNING, "Player: " + player + " is trying to summon pet from item that he doesn't owns.");
                  return false;
               } else {
                  PetData petData = PetsParser.getInstance().getPetDataByItemId(item.getId());
                  if (petData != null && petData.getNpcId() != -1) {
                     NpcTemplate npcTemplate = NpcsParser.getInstance().getTemplate(petData.getNpcId());
                     PetInstance pet = PetInstance.spawnPet(npcTemplate, player, item);
                     pet.setShowSummonAnimation(true);
                     if (!pet.isRespawned()) {
                        pet.setCurrentHp(pet.getMaxHp());
                        pet.setCurrentMp(pet.getMaxMp());
                        pet.getStat().setExp(pet.getExpForThisLevel());
                        pet.setCurrentFed(pet.getMaxFed());
                     }

                     pet.setRunning();
                     if (!pet.isRespawned()) {
                        pet.store();
                     }

                     player.setPet(pet);
                     pet.spawnMe(player.getX() + 50, player.getY() + 100, player.getZ());
                     pet.startFeed();
                     item.setEnchantLevel(pet.getLevel());
                     pet.startFeed();
                     pet.setFollowStatus(true);
                     pet.getOwner().sendPacket(new PetItemList(pet.getInventory().getItems()));
                     pet.broadcastStatusUpdate();
                     return true;
                  } else {
                     return false;
                  }
               }
            }
         } else {
            player.sendPacket(SystemMessageId.YOU_ALREADY_HAVE_A_PET);
            return false;
         }
      } else {
         return false;
      }
   }
}
