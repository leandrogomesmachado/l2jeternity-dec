package l2e.gameserver.handler.itemhandlers.impl;

import l2e.gameserver.data.parser.RecipeParser;
import l2e.gameserver.handler.itemhandlers.IItemHandler;
import l2e.gameserver.model.RecipeList;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class Recipes implements IItemHandler {
   @Override
   public boolean useItem(Playable playable, ItemInstance item, boolean forceUse) {
      if (!playable.isPlayer()) {
         playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
         return false;
      } else {
         Player activeChar = playable.getActingPlayer();
         if (activeChar.isInCraftMode()) {
            activeChar.sendPacket(SystemMessageId.CANT_ALTER_RECIPEBOOK_WHILE_CRAFTING);
            return false;
         } else {
            RecipeList rp = RecipeParser.getInstance().getRecipeByItemId(item.getId());
            if (rp == null) {
               return false;
            } else if (activeChar.hasRecipeList(rp.getId())) {
               activeChar.sendPacket(SystemMessageId.RECIPE_ALREADY_REGISTERED);
               return false;
            } else {
               boolean canCraft = false;
               boolean recipeLevel = false;
               boolean recipeLimit = false;
               if (rp.isDwarvenRecipe()) {
                  canCraft = activeChar.hasDwarvenCraft();
                  recipeLevel = rp.getLevel() > activeChar.getDwarvenCraft();
                  recipeLimit = activeChar.getDwarvenRecipeBook().length >= activeChar.getDwarfRecipeLimit();
               } else {
                  canCraft = activeChar.hasCommonCraft();
                  recipeLevel = rp.getLevel() > activeChar.getCommonCraft();
                  recipeLimit = activeChar.getCommonRecipeBook().length >= activeChar.getCommonRecipeLimit();
               }

               if (!canCraft) {
                  activeChar.sendPacket(SystemMessageId.CANT_REGISTER_NO_ABILITY_TO_CRAFT);
                  return false;
               } else if (recipeLevel) {
                  activeChar.sendPacket(SystemMessageId.CREATE_LVL_TOO_LOW_TO_REGISTER);
                  return false;
               } else if (recipeLimit) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.UP_TO_S1_RECIPES_CAN_REGISTER);
                  sm.addNumber(rp.isDwarvenRecipe() ? activeChar.getDwarfRecipeLimit() : activeChar.getCommonRecipeLimit());
                  activeChar.sendPacket(sm);
                  return false;
               } else {
                  if (rp.isDwarvenRecipe()) {
                     activeChar.registerDwarvenRecipeList(rp, true);
                  } else {
                     activeChar.registerCommonRecipeList(rp, true);
                  }

                  activeChar.destroyItem("Consume", item.getObjectId(), 1L, null, false);
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_ADDED);
                  sm.addItemName(item);
                  activeChar.sendPacket(sm);
                  return true;
               }
            }
         }
      }
   }
}
