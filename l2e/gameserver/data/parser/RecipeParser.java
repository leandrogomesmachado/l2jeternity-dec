package l2e.gameserver.data.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.RecipeList;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.RecipeStatTemplate;
import l2e.gameserver.model.actor.templates.RecipeTemplate;
import l2e.gameserver.model.stats.StatsSet;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class RecipeParser extends DocumentParser {
   private static final Map<Integer, RecipeList> _recipes = new HashMap<>();

   protected RecipeParser() {
      this.load();
   }

   @Override
   public void load() {
      _recipes.clear();
      this.parseDatapackFile("data/stats/items/recipes.xml");
      this._log.info(this.getClass().getSimpleName() + ": Loaded " + _recipes.size() + " recipes.");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      List<RecipeTemplate> recipePartList = new ArrayList<>();
      List<RecipeStatTemplate> recipeStatUseList = new ArrayList<>();
      List<RecipeStatTemplate> recipeAltStatChangeList = new ArrayList<>();

      for(Node n = this.getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equalsIgnoreCase(n.getNodeName())) {
            label116:
            for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
               if ("item".equalsIgnoreCase(d.getNodeName())) {
                  recipePartList.clear();
                  recipeStatUseList.clear();
                  recipeAltStatChangeList.clear();
                  NamedNodeMap attrs = d.getAttributes();
                  int id = -1;
                  boolean haveRare = false;
                  StatsSet set = new StatsSet();
                  Node att = attrs.getNamedItem("id");
                  if (att == null) {
                     this._log.severe(this.getClass().getSimpleName() + ": Missing id for recipe item, skipping");
                  } else {
                     id = Integer.parseInt(att.getNodeValue());
                     set.set("id", id);
                     att = attrs.getNamedItem("recipeId");
                     if (att == null) {
                        this._log.severe(this.getClass().getSimpleName() + ": Missing recipeId for recipe item id: " + id + ", skipping");
                     } else {
                        set.set("recipeId", Integer.parseInt(att.getNodeValue()));
                        att = attrs.getNamedItem("name");
                        if (att == null) {
                           this._log.severe(this.getClass().getSimpleName() + ": Missing name for recipe item id: " + id + ", skipping");
                        } else {
                           set.set("recipeName", att.getNodeValue());
                           att = attrs.getNamedItem("craftLevel");
                           if (att == null) {
                              this._log.severe(this.getClass().getSimpleName() + ": Missing level for recipe item id: " + id + ", skipping");
                           } else {
                              set.set("craftLevel", Integer.parseInt(att.getNodeValue()));
                              att = attrs.getNamedItem("type");
                              if (att == null) {
                                 this._log.severe(this.getClass().getSimpleName() + ": Missing type for recipe item id: " + id + ", skipping");
                              } else {
                                 set.set("isDwarvenRecipe", att.getNodeValue().equalsIgnoreCase("dwarven"));
                                 att = attrs.getNamedItem("successRate");
                                 if (att == null) {
                                    this._log.severe(this.getClass().getSimpleName() + ": Missing successRate for recipe item id: " + id + ", skipping");
                                 } else {
                                    set.set("successRate", Integer.parseInt(att.getNodeValue()));

                                    for(Node c = d.getFirstChild(); c != null; c = c.getNextSibling()) {
                                       if ("statUse".equalsIgnoreCase(c.getNodeName())) {
                                          String statName = c.getAttributes().getNamedItem("name").getNodeValue();
                                          int value = Integer.parseInt(c.getAttributes().getNamedItem("value").getNodeValue());

                                          try {
                                             recipeStatUseList.add(new RecipeStatTemplate(statName, value));
                                          } catch (Exception var16) {
                                             this._log
                                                .severe(
                                                   this.getClass().getSimpleName() + ": Error in StatUse parameter for recipe item id: " + id + ", skipping"
                                                );
                                             continue label116;
                                          }
                                       } else if ("altStatChange".equalsIgnoreCase(c.getNodeName())) {
                                          String statName = c.getAttributes().getNamedItem("name").getNodeValue();
                                          int value = Integer.parseInt(c.getAttributes().getNamedItem("value").getNodeValue());

                                          try {
                                             recipeAltStatChangeList.add(new RecipeStatTemplate(statName, value));
                                          } catch (Exception var15) {
                                             this._log
                                                .severe(
                                                   this.getClass().getSimpleName()
                                                      + ": Error in AltStatChange parameter for recipe item id: "
                                                      + id
                                                      + ", skipping"
                                                );
                                             continue label116;
                                          }
                                       } else if ("ingredient".equalsIgnoreCase(c.getNodeName())) {
                                          int ingId = Integer.parseInt(c.getAttributes().getNamedItem("id").getNodeValue());
                                          int ingCount = Integer.parseInt(c.getAttributes().getNamedItem("count").getNodeValue());
                                          recipePartList.add(new RecipeTemplate(ingId, ingCount));
                                       } else if ("production".equalsIgnoreCase(c.getNodeName())) {
                                          set.set("itemId", Integer.parseInt(c.getAttributes().getNamedItem("id").getNodeValue()));
                                          set.set("count", Integer.parseInt(c.getAttributes().getNamedItem("count").getNodeValue()));
                                       } else if ("productionRare".equalsIgnoreCase(c.getNodeName())) {
                                          set.set("rareItemId", Integer.parseInt(c.getAttributes().getNamedItem("id").getNodeValue()));
                                          set.set("rareCount", Integer.parseInt(c.getAttributes().getNamedItem("count").getNodeValue()));
                                          set.set("rarity", Integer.parseInt(c.getAttributes().getNamedItem("rarity").getNodeValue()));
                                          haveRare = true;
                                       }
                                    }

                                    RecipeList recipeList = new RecipeList(set, haveRare);

                                    for(RecipeTemplate recipePart : recipePartList) {
                                       recipeList.addRecipe(recipePart);
                                    }

                                    for(RecipeStatTemplate recipeStatUse : recipeStatUseList) {
                                       recipeList.addStatUse(recipeStatUse);
                                    }

                                    for(RecipeStatTemplate recipeAltStatChange : recipeAltStatChangeList) {
                                       recipeList.addAltStatChange(recipeAltStatChange);
                                    }

                                    _recipes.put(id, recipeList);
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public RecipeList getRecipeList(int listId) {
      return _recipes.get(listId);
   }

   public RecipeList getRecipeByItemId(int itemId) {
      for(RecipeList find : _recipes.values()) {
         if (find.getRecipeId() == itemId) {
            return find;
         }
      }

      return null;
   }

   public int[] getAllItemIds() {
      int[] idList = new int[_recipes.size()];
      int i = 0;

      for(RecipeList rec : _recipes.values()) {
         idList[i++] = rec.getRecipeId();
      }

      return idList;
   }

   public RecipeList getValidRecipeList(Player player, int id) {
      RecipeList recipeList = _recipes.get(id);
      if (recipeList != null && recipeList.getRecipes().length != 0) {
         return recipeList;
      } else {
         player.sendMessage(this.getClass().getSimpleName() + ": No recipe for: " + id);
         player.isInCraftMode(false);
         return null;
      }
   }

   public static RecipeParser getInstance() {
      return RecipeParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final RecipeParser _instance = new RecipeParser();
   }
}
