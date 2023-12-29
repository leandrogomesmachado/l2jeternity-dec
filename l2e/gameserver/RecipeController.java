package l2e.gameserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import l2e.commons.threading.RunnableImpl;
import l2e.commons.util.Rnd;
import l2e.commons.util.Util;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.data.parser.RecipeParser;
import l2e.gameserver.model.RecipeList;
import l2e.gameserver.model.TempItem;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.ManufactureItemTemplate;
import l2e.gameserver.model.actor.templates.RecipeStatTemplate;
import l2e.gameserver.model.actor.templates.RecipeTemplate;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.itemcontainer.Inventory;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.stats.Stats;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.gameserver.network.serverpackets.RecipeBookItemList;
import l2e.gameserver.network.serverpackets.RecipeItemMakeInfo;
import l2e.gameserver.network.serverpackets.RecipeShopItemInfo;
import l2e.gameserver.network.serverpackets.SetupGauge;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class RecipeController {
   protected static final Map<Integer, RecipeController.RecipeItemMaker> _activeMakers = new ConcurrentHashMap<>();

   protected RecipeController() {
   }

   public void requestBookOpen(Player player, boolean isDwarvenCraft) {
      if (!_activeMakers.containsKey(player.getObjectId())) {
         RecipeBookItemList response = new RecipeBookItemList(isDwarvenCraft, (int)player.getMaxMp());
         response.addRecipes(isDwarvenCraft ? player.getDwarvenRecipeBook() : player.getCommonRecipeBook());
         player.sendPacket(response);
      } else {
         player.sendPacket(SystemMessageId.CANT_ALTER_RECIPEBOOK_WHILE_CRAFTING);
      }
   }

   public void requestMakeItemAbort(Player player) {
      _activeMakers.remove(player.getObjectId());
   }

   public void requestManufactureItem(Player manufacturer, int recipeListId, Player player) {
      RecipeList recipeList = RecipeParser.getInstance().getValidRecipeList(player, recipeListId);
      if (recipeList != null) {
         List<RecipeList> dwarfRecipes = Arrays.asList(manufacturer.getDwarvenRecipeBook());
         List<RecipeList> commonRecipes = Arrays.asList(manufacturer.getCommonRecipeBook());
         if (!dwarfRecipes.contains(recipeList) && !commonRecipes.contains(recipeList)) {
            Util.handleIllegalPlayerAction(player, "" + player.getName() + " of account " + player.getAccountName() + " sent a false recipe id.");
         } else if (Config.ALT_GAME_CREATION && _activeMakers.containsKey(manufacturer.getObjectId())) {
            player.sendPacket(SystemMessageId.CLOSE_STORE_WINDOW_AND_TRY_AGAIN);
         } else {
            RecipeController.RecipeItemMaker maker = new RecipeController.RecipeItemMaker(manufacturer, recipeList, player);
            if (maker._isValid) {
               if (Config.ALT_GAME_CREATION) {
                  _activeMakers.put(manufacturer.getObjectId(), maker);
                  ThreadPoolManager.getInstance().schedule(maker, 100L);
               } else {
                  maker.run();
               }
            }
         }
      }
   }

   public void requestMakeItem(Player player, int recipeListId) {
      if (!player.isInCombat() && !player.isInDuel()) {
         RecipeList recipeList = RecipeParser.getInstance().getValidRecipeList(player, recipeListId);
         if (recipeList != null) {
            List<RecipeList> dwarfRecipes = Arrays.asList(player.getDwarvenRecipeBook());
            List<RecipeList> commonRecipes = Arrays.asList(player.getCommonRecipeBook());
            if (!dwarfRecipes.contains(recipeList) && !commonRecipes.contains(recipeList)) {
               Util.handleIllegalPlayerAction(player, "" + player.getName() + " of account " + player.getAccountName() + " sent a false recipe id.");
            } else if (Config.ALT_GAME_CREATION && _activeMakers.containsKey(player.getObjectId())) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1);
               sm.addItemName(recipeList.getItemId());
               sm.addString("You are busy creating.");
               player.sendPacket(sm);
            } else {
               RecipeController.RecipeItemMaker maker = new RecipeController.RecipeItemMaker(player, recipeList, player);
               if (maker._isValid) {
                  if (Config.ALT_GAME_CREATION) {
                     _activeMakers.put(player.getObjectId(), maker);
                     ThreadPoolManager.getInstance().schedule(maker, 100L);
                  } else {
                     maker.run();
                  }
               }
            }
         }
      } else {
         player.sendPacket(SystemMessageId.CANT_OPERATE_PRIVATE_STORE_DURING_COMBAT);
      }
   }

   public static RecipeController getInstance() {
      return RecipeController.SingletonHolder._instance;
   }

   private static class RecipeItemMaker extends RunnableImpl {
      private static final Logger _log = Logger.getLogger(RecipeController.RecipeItemMaker.class.getName());
      protected boolean _isValid;
      protected List<TempItem> _items = null;
      protected final RecipeList _recipeList;
      protected final Player _player;
      protected final Player _target;
      protected final Skill _skill;
      protected final int _skillId;
      protected final int _skillLevel;
      protected int _creationPasses = 1;
      protected int _itemGrab;
      protected int _exp = -1;
      protected int _sp = -1;
      protected long _price;
      protected int _totalItems;
      protected int _delay;

      public RecipeItemMaker(Player pPlayer, RecipeList pRecipeList, Player pTarget) {
         this._player = pPlayer;
         this._target = pTarget;
         this._recipeList = pRecipeList;
         this._isValid = false;
         this._skillId = this._recipeList.isDwarvenRecipe() ? 172 : 1320;
         this._skillLevel = this._player.getSkillLevel(this._skillId);
         this._skill = this._player.getKnownSkill(this._skillId);
         this._player.isInCraftMode(true);
         if (this._player.isAlikeDead()) {
            this._player.sendActionFailed();
            this.abort();
         } else if (this._target.isAlikeDead()) {
            this._target.sendActionFailed();
            this.abort();
         } else if (this._target.isProcessingTransaction()) {
            this._target.sendActionFailed();
            this.abort();
         } else if (this._player.isProcessingTransaction()) {
            this._player.sendActionFailed();
            this.abort();
         } else if (this._recipeList.getRecipes().length == 0) {
            this._player.sendActionFailed();
            this.abort();
         } else if (this._recipeList.getLevel() > this._skillLevel) {
            this._player.sendActionFailed();
            this.abort();
         } else {
            if (this._player != this._target) {
               ManufactureItemTemplate item = this._player.getManufactureItems().get(this._recipeList.getId());
               if (item != null) {
                  this._price = item.getCost();
                  if (this._target.getAdena() < this._price) {
                     this._target.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
                     this.abort();
                     return;
                  }
               }
            }

            if ((this._items = this.listItems(false)) == null) {
               this.abort();
            } else {
               for(TempItem i : this._items) {
                  this._totalItems += i.getQuantity();
               }

               if (!this.calculateStatUse(false, false)) {
                  this.abort();
               } else {
                  if (Config.ALT_GAME_CREATION) {
                     this.calculateAltStatChange();
                  }

                  this.updateMakeInfo(true);
                  this.updateCurMp();
                  this.updateCurLoad();
                  this._player.isInCraftMode(false);
                  this._isValid = true;
               }
            }
         }
      }

      @Override
      public void runImpl() throws Exception {
         if (!Config.IS_CRAFTING_ENABLED) {
            this._target.sendMessage("Item creation is currently disabled.");
            this.abort();
         } else if (this._player == null || this._target == null) {
            _log.warning("player or target == null (disconnected?), aborting" + this._target + this._player);
            this.abort();
         } else if (this._player.isOnline() && this._target.isOnline()) {
            if (Config.ALT_GAME_CREATION && !RecipeController._activeMakers.containsKey(this._player.getObjectId())) {
               if (this._target != this._player) {
                  this._target.sendMessage("Manufacture aborted");
                  this._player.sendMessage("Manufacture aborted");
               } else {
                  this._player.sendMessage("Item creation aborted");
               }

               this.abort();
            } else {
               if (Config.ALT_GAME_CREATION && !this._items.isEmpty()) {
                  if (!this.calculateStatUse(true, true)) {
                     return;
                  }

                  this.updateCurMp();
                  this.grabSomeItems();
                  if (!this._items.isEmpty()) {
                     this._delay = (int)(Config.ALT_GAME_CREATION_SPEED * this._player.getMReuseRate(this._skill) * 10.0 / Config.RATE_CONSUMABLE_COST) * 100;
                     MagicSkillUse msk = new MagicSkillUse(this._player, this._skillId, this._skillLevel, this._delay, 0);
                     this._player.broadcastPacket(msk);
                     this._player.sendPacket(new SetupGauge(this._player, 0, this._delay));
                     ThreadPoolManager.getInstance().schedule(this, (long)(100 + this._delay));
                  } else {
                     this._player.sendPacket(new SetupGauge(this._player, 0, this._delay));

                     try {
                        Thread.sleep((long)this._delay);
                     } catch (InterruptedException var5) {
                     } finally {
                        this.finishCrafting();
                     }
                  }
               } else {
                  this.finishCrafting();
               }
            }
         } else {
            _log.warning("player or target is not online, aborting " + this._target + this._player);
            this.abort();
         }
      }

      private void finishCrafting() {
         if (!Config.ALT_GAME_CREATION) {
            this.calculateStatUse(false, true);
         }

         if (this._target != this._player && this._price > 0L) {
            ItemInstance adenatransfer = this._target
               .transferItem(
                  "PayManufacture", this._target.getInventory().getAdenaInstance().getObjectId(), this._price, this._player.getInventory(), this._player
               );
            if (adenatransfer == null) {
               this._target.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
               this.abort();
               return;
            }
         }

         if ((this._items = this.listItems(true)) != null) {
            int tryCount = 1;
            if (Rnd.chance(Config.CRAFT_DOUBLECRAFT_CHANCE)) {
               ++tryCount;
            }

            int chance = this._recipeList.getSuccessRate() + this._player.getPremiumBonus().getCraftChance();

            for(int i = 0; i < tryCount; ++i) {
               if (chance >= 100) {
                  this.rewardPlayer();
                  this.updateMakeInfo(true);
               } else if (Rnd.chance(chance)) {
                  this.rewardPlayer();
                  this.updateMakeInfo(true);
                  this._target.getCounters().addAchivementInfo("recipesSucceeded", 0, -1L, false, false, false);
               } else {
                  if (this._target != this._player) {
                     SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.CREATION_OF_S2_FOR_C1_AT_S3_ADENA_FAILED);
                     msg.addString(this._target.getName());
                     msg.addItemName(this._recipeList.getItemId());
                     msg.addItemNumber(this._price);
                     this._player.sendPacket(msg);
                     msg = SystemMessage.getSystemMessage(SystemMessageId.C1_FAILED_TO_CREATE_S2_FOR_S3_ADENA);
                     msg.addString(this._player.getName());
                     msg.addItemName(this._recipeList.getItemId());
                     msg.addItemNumber(this._price);
                     this._target.sendPacket(msg);
                  } else {
                     this._target.sendPacket(SystemMessageId.ITEM_MIXING_FAILED);
                     this._target.getCounters().addAchivementInfo("recipesFailed", 0, -1L, false, false, false);
                  }

                  this.updateMakeInfo(false);
               }
            }
         }

         this.updateCurMp();
         this.updateCurLoad();
         RecipeController._activeMakers.remove(this._player.getObjectId());
         this._player.isInCraftMode(false);
         this._target.sendItemList(false);
      }

      private void updateMakeInfo(boolean success) {
         if (this._target == this._player) {
            this._target.sendPacket(new RecipeItemMakeInfo(this._recipeList.getId(), this._target, success));
         } else {
            this._target.sendPacket(new RecipeShopItemInfo(this._player, this._recipeList.getId()));
         }
      }

      private void updateCurLoad() {
         StatusUpdate su = new StatusUpdate(this._target);
         su.addAttribute(14, this._target.getCurrentLoad());
         this._target.sendPacket(su);
      }

      private void updateCurMp() {
         StatusUpdate su = new StatusUpdate(this._target);
         su.addAttribute(11, (int)this._target.getCurrentMp());
         this._target.sendPacket(su);
      }

      private void grabSomeItems() {
         int grabItems = this._itemGrab;

         while(grabItems > 0 && !this._items.isEmpty()) {
            TempItem item = this._items.get(0);
            int count = item.getQuantity();
            if (count >= grabItems) {
               count = grabItems;
            }

            item.setQuantity(item.getQuantity() - count);
            if (item.getQuantity() <= 0) {
               this._items.remove(0);
            } else {
               this._items.set(0, item);
            }

            grabItems -= count;
            if (this._target == this._player) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_S2_EQUIPPED);
               sm.addItemNumber((long)count);
               sm.addItemName(item.getId());
               this._player.sendPacket(sm);
            } else {
               this._target.sendMessage("Manufacturer " + this._player.getName() + " used " + count + " " + this._target.getItemName(item.getItem()));
            }
         }
      }

      private void calculateAltStatChange() {
         this._itemGrab = this._skillLevel;

         for(RecipeStatTemplate altStatChange : this._recipeList.getAltStatChange()) {
            if (altStatChange.getType() == RecipeStatTemplate.StatType.XP) {
               this._exp = altStatChange.getValue();
            } else if (altStatChange.getType() == RecipeStatTemplate.StatType.SP) {
               this._sp = altStatChange.getValue();
            } else if (altStatChange.getType() == RecipeStatTemplate.StatType.GIM) {
               this._itemGrab *= altStatChange.getValue();
            }
         }

         this._creationPasses = this._totalItems / this._itemGrab + (this._totalItems % this._itemGrab != 0 ? 1 : 0);
         if (this._creationPasses < 1) {
            this._creationPasses = 1;
         }
      }

      private boolean calculateStatUse(boolean isWait, boolean isReduce) {
         boolean ret = true;

         for(RecipeStatTemplate statUse : this._recipeList.getStatUse()) {
            double modifiedValue = (double)(statUse.getValue() / this._creationPasses);
            if (statUse.getType() == RecipeStatTemplate.StatType.HP) {
               if (this._player.getCurrentHp() <= modifiedValue) {
                  if (Config.ALT_GAME_CREATION && isWait) {
                     this._player.sendPacket(new SetupGauge(this._player, 0, this._delay));
                     ThreadPoolManager.getInstance().schedule(this, (long)(100 + this._delay));
                  } else {
                     this._target.sendPacket(SystemMessageId.NOT_ENOUGH_HP);
                     this.abort();
                  }

                  ret = false;
               } else if (isReduce) {
                  this._player.reduceCurrentHp(modifiedValue, this._player, this._skill);
               }
            } else if (statUse.getType() == RecipeStatTemplate.StatType.MP) {
               if (this._player.getCurrentMp() < modifiedValue) {
                  if (Config.ALT_GAME_CREATION && isWait) {
                     this._player.sendPacket(new SetupGauge(this._player, 0, this._delay));
                     ThreadPoolManager.getInstance().schedule(this, (long)(100 + this._delay));
                  } else {
                     this._target.sendPacket(SystemMessageId.NOT_ENOUGH_MP);
                     this.abort();
                  }

                  ret = false;
               } else if (isReduce) {
                  this._player.reduceCurrentMp(modifiedValue);
               }
            } else {
               this._target.sendMessage("Recipe error!!!, please tell this to your GM.");
               ret = false;
               this.abort();
            }
         }

         return ret;
      }

      private List<TempItem> listItems(boolean remove) {
         RecipeTemplate[] recipes = this._recipeList.getRecipes();
         Inventory inv = this._target.getInventory();
         List<TempItem> materials = new ArrayList<>();

         for(RecipeTemplate recipe : recipes) {
            if (recipe.getQuantity() > 0) {
               ItemInstance item = inv.getItemByItemId(recipe.getId());
               long itemQuantityAmount = item == null ? 0L : item.getCount();
               if (itemQuantityAmount < (long)recipe.getQuantity()) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.MISSING_S2_S1_TO_CREATE);
                  sm.addItemName(recipe.getId());
                  sm.addItemNumber((long)recipe.getQuantity() - itemQuantityAmount);
                  this._target.sendPacket(sm);
                  this.abort();
                  return null;
               }

               TempItem temp = new TempItem(item, recipe.getQuantity());
               materials.add(temp);
            }
         }

         if (remove) {
            for(TempItem tmp : materials) {
               inv.destroyItemByItemId("Manufacture", tmp.getId(), (long)tmp.getQuantity(), this._target, this._player);
               if (tmp.getQuantity() > 1) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
                  sm.addItemName(tmp.getId());
                  sm.addItemNumber((long)tmp.getQuantity());
                  this._target.sendPacket(sm);
               } else {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
                  sm.addItemName(tmp.getId());
                  this._target.sendPacket(sm);
               }
            }
         }

         return materials;
      }

      private void abort() {
         this.updateMakeInfo(false);
         this._player.isInCraftMode(false);
         RecipeController._activeMakers.remove(this._player.getObjectId());
      }

      private void rewardPlayer() {
         int rareProdId = this._recipeList.getRareItemId();
         int itemId = this._recipeList.getItemId();
         int itemCount = this._recipeList.getCount();
         Item template = ItemsParser.getInstance().getTemplate(itemId);
         if (rareProdId > 0 && (rareProdId == itemId || Config.CRAFT_MASTERWORK)) {
            int chance = this._recipeList.getRarity() + this._player.getPremiumBonus().getMasterWorkChance();
            if (chance >= 100) {
               itemId = rareProdId;
               itemCount = this._recipeList.getRareCount();
            } else if (Rnd.chance(chance)) {
               itemId = rareProdId;
               itemCount = this._recipeList.getRareCount();
               this._target.getCounters().addAchivementInfo("rareCreated", 0, -1L, false, false, false);
            }
         }

         this._target.getInventory().addItem("Manufacture", itemId, (long)itemCount, this._target, this._player);
         SystemMessage sm = null;
         if (this._target != this._player) {
            if (itemCount == 1) {
               sm = SystemMessage.getSystemMessage(SystemMessageId.S2_CREATED_FOR_C1_FOR_S3_ADENA);
               sm.addString(this._target.getName());
               sm.addItemName(itemId);
               sm.addItemNumber(this._price);
               this._player.sendPacket(sm);
               sm = SystemMessage.getSystemMessage(SystemMessageId.C1_CREATED_S2_FOR_S3_ADENA);
               sm.addString(this._player.getName());
               sm.addItemName(itemId);
               sm.addItemNumber(this._price);
               this._target.sendPacket(sm);
            } else {
               sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S3_S_CREATED_FOR_C1_FOR_S4_ADENA);
               sm.addString(this._target.getName());
               sm.addNumber(itemCount);
               sm.addItemName(itemId);
               sm.addItemNumber(this._price);
               this._player.sendPacket(sm);
               sm = SystemMessage.getSystemMessage(SystemMessageId.C1_CREATED_S2_S3_S_FOR_S4_ADENA);
               sm.addString(this._player.getName());
               sm.addNumber(itemCount);
               sm.addItemName(itemId);
               sm.addItemNumber(this._price);
               this._target.sendPacket(sm);
            }
         }

         if (itemCount > 1) {
            sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
            sm.addItemName(itemId);
            sm.addItemNumber((long)itemCount);
            this._target.sendPacket(sm);
         } else {
            sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
            sm.addItemName(itemId);
            this._target.sendPacket(sm);
         }

         if (Config.ALT_GAME_CREATION) {
            int recipeLevel = this._recipeList.getLevel();
            if (this._exp < 0) {
               this._exp = template.getReferencePrice() * itemCount;
               this._exp /= recipeLevel;
            }

            if (this._sp < 0) {
               this._sp = this._exp / 10;
            }

            if (itemId == rareProdId) {
               this._exp = (int)((double)this._exp * Config.ALT_GAME_CREATION_RARE_XPSP_RATE);
               this._sp = (int)((double)this._sp * Config.ALT_GAME_CREATION_RARE_XPSP_RATE);
            }

            if (this._exp < 0) {
               this._exp = 0;
            }

            if (this._sp < 0) {
               this._sp = 0;
            }

            for(int i = this._skillLevel; i > recipeLevel; --i) {
               this._exp /= 4;
               this._sp /= 4;
            }

            this._player
               .addExpAndSp(
                  (long)(
                     (int)this._player
                        .calcStat(Stats.EXPSP_RATE, (double)this._exp * Config.ALT_GAME_CREATION_XP_RATE * Config.ALT_GAME_CREATION_SPEED, null, null)
                  ),
                  (int)this._player
                     .calcStat(Stats.EXPSP_RATE, (double)this._sp * Config.ALT_GAME_CREATION_SP_RATE * Config.ALT_GAME_CREATION_SPEED, null, null)
               );
         }

         this.updateMakeInfo(true);
      }
   }

   private static class SingletonHolder {
      protected static final RecipeController _instance = new RecipeController();
   }
}
