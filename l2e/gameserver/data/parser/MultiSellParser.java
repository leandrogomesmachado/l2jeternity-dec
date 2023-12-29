package l2e.gameserver.data.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import l2e.commons.math.SafeMath;
import l2e.commons.util.Util;
import l2e.commons.util.file.filter.NumericNameFilter;
import l2e.gameserver.Config;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.items.multisell.Entry;
import l2e.gameserver.model.items.multisell.Ingredient;
import l2e.gameserver.model.items.multisell.ListContainer;
import l2e.gameserver.model.items.multisell.PreparedListContainer;
import l2e.gameserver.model.stats.StatsSet;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExPCCafePointInfo;
import l2e.gameserver.network.serverpackets.MultiSellList;
import l2e.gameserver.network.serverpackets.SystemMessage;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class MultiSellParser extends DocumentParser {
   public static final int PAGE_SIZE = 40;
   public static final int PC_BANG_POINTS = Config.PC_POINT_ID;
   public static final int CLAN_REPUTATION = -200;
   public static final int FAME = -300;
   private final Map<Integer, ListContainer> _entries = new HashMap<>();

   protected MultiSellParser() {
      this.setCurrentFileFilter(new NumericNameFilter());
      this.load();
   }

   @Override
   public final void load() {
      this._entries.clear();
      this.parseDirectory("data/stats/npcs/multisell", false);
      if (Config.CUSTOM_MULTISELLS) {
         this.parseDirectory("data/stats/npcs/multisell/custom", false);
      }

      this.verify();
      this._log.log(Level.INFO, this.getClass().getSimpleName() + ": Loaded " + this._entries.size() + " lists.");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected final void parseDocument() {
      try {
         int id = Integer.parseInt(this.getCurrentFile().getName().replaceAll(".xml", ""));
         int entryId = 1;
         ListContainer list = new ListContainer(id);

         for(Node n = this.getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling()) {
            if ("list".equalsIgnoreCase(n.getNodeName())) {
               Node att = n.getAttributes().getNamedItem("applyTaxes");
               list.setApplyTaxes(att != null && Boolean.parseBoolean(att.getNodeValue()));
               att = n.getAttributes().getNamedItem("useRate");
               if (att != null) {
                  try {
                     list.setUseRate(Double.valueOf(att.getNodeValue()));
                     if (list.getUseRate() <= 1.0E-6) {
                        throw new NumberFormatException("The value cannot be 0");
                     }
                  } catch (NumberFormatException var9) {
                     try {
                        list.setUseRate(Config.class.getField(att.getNodeValue()).getDouble(Config.class));
                     } catch (Exception var8) {
                        this._log.warning(var8.getMessage() + this.getCurrentDocument().getLocalName());
                        list.setUseRate(1.0);
                     }
                  } catch (DOMException var10) {
                     this._log.warning(var10.getMessage() + this.getCurrentDocument().getLocalName());
                  }
               }

               att = n.getAttributes().getNamedItem("maintainEnchantment");
               list.setMaintainEnchantment(att != null && Boolean.parseBoolean(att.getNodeValue()));

               for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
                  if ("item".equalsIgnoreCase(d.getNodeName())) {
                     Entry e = this.parseEntry(id, d, entryId++, list);
                     list.getEntries().add(e);
                  } else if ("npcs".equalsIgnoreCase(d.getNodeName())) {
                     for(Node b = d.getFirstChild(); b != null; b = b.getNextSibling()) {
                        if ("npc".equalsIgnoreCase(b.getNodeName()) && Util.isDigit(b.getTextContent())) {
                           list.allowNpc(Integer.parseInt(b.getTextContent()));
                        }
                     }
                  }
               }
            }
         }

         this._entries.put(id, list);
      } catch (Exception var11) {
         this._log.log(Level.SEVERE, this.getClass().getSimpleName() + ": Error in file " + this.getCurrentFile(), (Throwable)var11);
      }
   }

   private final Entry parseEntry(int id, Node n, int entryId, ListContainer list) {
      Node first = n.getFirstChild();
      Entry entry = new Entry(entryId);

      for(Node var15 = first; var15 != null; var15 = var15.getNextSibling()) {
         if ("ingredient".equalsIgnoreCase(var15.getNodeName())) {
            NamedNodeMap attrs = var15.getAttributes();
            StatsSet set = new StatsSet();

            for(int i = 0; i < attrs.getLength(); ++i) {
               Node att = attrs.item(i);
               set.set(att.getNodeName(), att.getNodeValue());
            }

            entry.addIngredient(new Ingredient(set));
         } else if ("production".equalsIgnoreCase(var15.getNodeName())) {
            NamedNodeMap attrs = var15.getAttributes();
            StatsSet set = new StatsSet();

            for(int i = 0; i < attrs.getLength(); ++i) {
               Node att = attrs.item(i);
               set.set(att.getNodeName(), att.getNodeValue());
            }

            entry.addProduct(new Ingredient(set));
         }
      }

      if (entry.getIngredients().size() == 1 && entry.getIngredients().get(0).getId() == 57) {
         long count = 0L;

         for(Ingredient product : entry.getProducts()) {
            if (product.getId() >= 0) {
               Item item = ItemsParser.getInstance().getTemplate(product.getId());
               if (item == null) {
                  this._log
                     .warning(this.getClass().getSimpleName() + ": MultiSell [" + id + "] Production [" + entry.getProducts().get(0).getId() + "] not found!");
                  return null;
               }

               count = SafeMath.addAndCheck(count, SafeMath.mulAndCheck(entry.getProducts().get(0).getCount(), (long)item.getReferencePrice()));
            }
         }

         if (count > entry.getIngredients().get(0).getCount() && Config.ALLOW_MULTISELL_DEBUG) {
            this._log
               .warning(
                  this.getClass().getSimpleName()
                     + ": MultiSell ["
                     + id
                     + "] Production ["
                     + entry.getEntryId()
                     + "] ["
                     + entry.getProducts().get(0).getId()
                     + "] price is lower than referenced | "
                     + count
                     + " > "
                     + entry.getIngredients().get(0).getCount()
               );
         }
      }

      return entry;
   }

   public final void separateAndSend(int listId, Player player, Npc npc, boolean inventoryOnly, double productMultiplier, double ingredientMultiplier) {
      if (!player.isProcessingTransaction()) {
         ListContainer template = this._entries.get(listId);
         if (template == null) {
            this._log
               .warning(
                  this.getClass().getSimpleName()
                     + ": can't find list id: "
                     + listId
                     + " requested by player: "
                     + player.getName()
                     + ", npcId:"
                     + (npc != null ? npc.getId() : 0)
               );
         } else if ((npc == null || template.isNpcAllowed(npc.getId())) && (npc != null || !template.isNpcOnly())) {
            PreparedListContainer list = new PreparedListContainer(template, inventoryOnly, player, npc);
            if (productMultiplier != 1.0 || ingredientMultiplier != 1.0) {
               for(Entry entry : list.getEntries()) {
                  for(Ingredient product : entry.getProducts()) {
                     product.setCount((long)Math.max((double)product.getCount() * productMultiplier, 1.0));
                  }

                  for(Ingredient ingredient : entry.getIngredients()) {
                     ingredient.setCount((long)Math.max((double)ingredient.getCount() * ingredientMultiplier, 1.0));
                  }
               }
            }

            int index = 0;

            do {
               player.sendPacket(new MultiSellList(list, index));
               index += 40;
            } while(index < list.getEntries().size());

            if (player.isGM()) {
               player.sendMessage("MutliSell: " + listId + ".xml");
            }

            player.setMultiSell(list);
         } else {
            this._log
               .warning(
                  this.getClass().getSimpleName()
                     + ": player "
                     + player
                     + " attempted to open multisell "
                     + listId
                     + " from npc "
                     + npc
                     + " which is not allowed!"
               );
         }
      }
   }

   public final void separateAndSend(int listId, Player player, Npc npc, boolean inventoryOnly) {
      this.separateAndSend(listId, player, npc, inventoryOnly, 1.0, 1.0);
   }

   public static final boolean checkSpecialIngredient(int id, long amount, Player player) {
      if (id == PC_BANG_POINTS) {
         if ((long)player.getPcBangPoints() < amount) {
            player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
            return false;
         }
      } else if (id == -200) {
         if (player.getClan() == null) {
            player.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER);
            return false;
         }

         if (!player.isClanLeader()) {
            player.sendPacket(SystemMessageId.ONLY_THE_CLAN_LEADER_IS_ENABLED);
            return false;
         }

         if ((long)player.getClan().getReputationScore() < amount) {
            player.sendPacket(SystemMessageId.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW);
            return false;
         }
      } else if (id == -300 && (long)player.getFame() < amount) {
         player.sendPacket(SystemMessageId.NOT_ENOUGH_FAME_POINTS);
         return false;
      }

      return true;
   }

   public static final boolean getSpecialIngredient(int id, long amount, Player player) {
      if (id == PC_BANG_POINTS) {
         int cost = player.getPcBangPoints() - (int)amount;
         player.setPcBangPoints(cost);
         SystemMessage smsgpc = SystemMessage.getSystemMessage(SystemMessageId.USING_S1_PCPOINT);
         smsgpc.addNumber((int)amount);
         player.sendPacket(smsgpc);
         player.sendPacket(new ExPCCafePointInfo(player.getPcBangPoints(), (int)amount, false, false, 1));
         return true;
      } else if (id == -200 && player.getClan() != null) {
         player.getClan().takeReputationScore((int)amount, true);
         SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
         smsg.addNumber((int)amount);
         player.sendPacket(smsg);
         return true;
      } else if (id == -300) {
         player.setFame(player.getFame() - (int)amount);
         player.sendUserInfo();
         return true;
      } else {
         return false;
      }
   }

   public static final void addSpecialProduct(int id, long amount, Player player) {
      switch(id) {
         case -300:
            player.setFame((int)((long)player.getFame() + amount));
            player.sendUserInfo();
            break;
         case -200:
            if (player.getClan() != null) {
               player.getClan().addReputationScore((int)amount, true);
            }
      }
   }

   private final void verify() {
      for(ListContainer list : this._entries.values()) {
         for(Entry ent : list.getEntries()) {
            for(Ingredient ing : ent.getIngredients()) {
               if (!this.verifyIngredient(ing)) {
                  this._log.warning(this.getClass().getSimpleName() + ": can't find ingredient with itemId: " + ing.getId() + " in list: " + list.getListId());
               }
            }

            for(Ingredient ing : ent.getProducts()) {
               if (!this.verifyIngredient(ing)) {
                  this._log.warning(this.getClass().getSimpleName() + ": can't find product with itemId: " + ing.getId() + " in list: " + list.getListId());
               }
            }
         }
      }
   }

   private final boolean verifyIngredient(Ingredient ing) {
      switch(ing.getId()) {
         case -300:
         case -200:
            return true;
         default:
            if (ing.getId() == PC_BANG_POINTS) {
               return true;
            } else {
               return ing.getTemplate() != null;
            }
      }
   }

   public Map<Integer, ListContainer> getEntries() {
      return this._entries;
   }

   public static MultiSellParser getInstance() {
      return MultiSellParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final MultiSellParser _instance = new MultiSellParser();
   }
}
