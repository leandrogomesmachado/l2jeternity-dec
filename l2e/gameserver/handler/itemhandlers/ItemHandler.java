package l2e.gameserver.handler.itemhandlers;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import l2e.gameserver.handler.itemhandlers.impl.BeastSoulShot;
import l2e.gameserver.handler.itemhandlers.impl.BeastSpice;
import l2e.gameserver.handler.itemhandlers.impl.BeastSpiritShot;
import l2e.gameserver.handler.itemhandlers.impl.BlessedSpiritShot;
import l2e.gameserver.handler.itemhandlers.impl.Book;
import l2e.gameserver.handler.itemhandlers.impl.Bypass;
import l2e.gameserver.handler.itemhandlers.impl.Calculator;
import l2e.gameserver.handler.itemhandlers.impl.ChristmasTree;
import l2e.gameserver.handler.itemhandlers.impl.Community;
import l2e.gameserver.handler.itemhandlers.impl.Disguise;
import l2e.gameserver.handler.itemhandlers.impl.Elixir;
import l2e.gameserver.handler.itemhandlers.impl.EnchantAttribute;
import l2e.gameserver.handler.itemhandlers.impl.EnchantScrolls;
import l2e.gameserver.handler.itemhandlers.impl.EventItem;
import l2e.gameserver.handler.itemhandlers.impl.ExtractableItems;
import l2e.gameserver.handler.itemhandlers.impl.FishShots;
import l2e.gameserver.handler.itemhandlers.impl.Harvester;
import l2e.gameserver.handler.itemhandlers.impl.ItemSkills;
import l2e.gameserver.handler.itemhandlers.impl.ItemSkillsTemplate;
import l2e.gameserver.handler.itemhandlers.impl.ManaPotion;
import l2e.gameserver.handler.itemhandlers.impl.Maps;
import l2e.gameserver.handler.itemhandlers.impl.MercTicket;
import l2e.gameserver.handler.itemhandlers.impl.NevitHourglass;
import l2e.gameserver.handler.itemhandlers.impl.NicknameColor;
import l2e.gameserver.handler.itemhandlers.impl.PetFood;
import l2e.gameserver.handler.itemhandlers.impl.Premium;
import l2e.gameserver.handler.itemhandlers.impl.QuestItems;
import l2e.gameserver.handler.itemhandlers.impl.Recipes;
import l2e.gameserver.handler.itemhandlers.impl.RollingDice;
import l2e.gameserver.handler.itemhandlers.impl.ScrollOfResurrection;
import l2e.gameserver.handler.itemhandlers.impl.Seed;
import l2e.gameserver.handler.itemhandlers.impl.SevenSignsRecord;
import l2e.gameserver.handler.itemhandlers.impl.SoulShots;
import l2e.gameserver.handler.itemhandlers.impl.SpecialXMas;
import l2e.gameserver.handler.itemhandlers.impl.SpiritShot;
import l2e.gameserver.handler.itemhandlers.impl.SummonItems;
import l2e.gameserver.handler.itemhandlers.impl.TeleportBookmark;
import l2e.gameserver.handler.itemhandlers.impl.TempHero;
import l2e.gameserver.handler.itemhandlers.impl.VisualItems;
import l2e.gameserver.model.actor.templates.items.EtcItem;

public class ItemHandler {
   private static Logger _log = Logger.getLogger(ItemHandler.class.getName());
   private final Map<String, IItemHandler> _handlers = new HashMap<>();

   public static ItemHandler getInstance() {
      return ItemHandler.SingletonHolder._instance;
   }

   public int size() {
      return this._handlers.size();
   }

   protected ItemHandler() {
      this.registerHandler(new BeastSoulShot());
      this.registerHandler(new BeastSpice());
      this.registerHandler(new BeastSpiritShot());
      this.registerHandler(new BlessedSpiritShot());
      this.registerHandler(new Bypass());
      this.registerHandler(new Book());
      this.registerHandler(new Calculator());
      this.registerHandler(new ChristmasTree());
      this.registerHandler(new Community());
      this.registerHandler(new Disguise());
      this.registerHandler(new Elixir());
      this.registerHandler(new EnchantAttribute());
      this.registerHandler(new EnchantScrolls());
      this.registerHandler(new EventItem());
      this.registerHandler(new ExtractableItems());
      this.registerHandler(new FishShots());
      this.registerHandler(new Harvester());
      this.registerHandler(new ItemSkills());
      this.registerHandler(new ItemSkillsTemplate());
      this.registerHandler(new ManaPotion());
      this.registerHandler(new Maps());
      this.registerHandler(new MercTicket());
      this.registerHandler(new NevitHourglass());
      this.registerHandler(new NicknameColor());
      this.registerHandler(new PetFood());
      this.registerHandler(new Premium());
      this.registerHandler(new QuestItems());
      this.registerHandler(new Recipes());
      this.registerHandler(new RollingDice());
      this.registerHandler(new ScrollOfResurrection());
      this.registerHandler(new Seed());
      this.registerHandler(new SevenSignsRecord());
      this.registerHandler(new SoulShots());
      this.registerHandler(new SpecialXMas());
      this.registerHandler(new SpiritShot());
      this.registerHandler(new SummonItems());
      this.registerHandler(new TempHero());
      this.registerHandler(new TeleportBookmark());
      this.registerHandler(new VisualItems());
      _log.info("Loaded " + this._handlers.size() + " ItemHandlers.");
   }

   public void registerHandler(IItemHandler handler) {
      this._handlers.put(handler.getClass().getSimpleName(), handler);
   }

   public synchronized void removeHandler(IItemHandler handler) {
      this._handlers.remove(handler.getClass().getSimpleName());
   }

   public IItemHandler getHandler(EtcItem item) {
      return item != null && item.getHandlerName() != null ? this._handlers.get(item.getHandlerName()) : null;
   }

   private static class SingletonHolder {
      protected static final ItemHandler _instance = new ItemHandler();
   }
}
