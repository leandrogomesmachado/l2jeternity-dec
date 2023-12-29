package l2e.gameserver.data.parser;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.collections.MultiValueSet;
import l2e.gameserver.Config;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.entity.events.AbstractFightEvent;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.TreeIntObjectMap;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class FightEventParser extends DocumentParser {
   private static final Logger _log = Logger.getLogger(FightEventParser.class.getName());
   private final IntObjectMap<AbstractFightEvent> _events = new TreeIntObjectMap<>();
   private final List<Integer> _activeEvents = new CopyOnWriteArrayList<>();
   private final List<Integer> _disabledEvents = new CopyOnWriteArrayList<>();

   protected FightEventParser() {
      this._events.clear();
      this._activeEvents.clear();
      this._disabledEvents.clear();
      this.load();
   }

   public void reload() {
      this._events.clear();
      this._activeEvents.clear();
      this._disabledEvents.clear();
      this.load();
   }

   @Override
   public final void load() {
      this.parseDirectory("data/stats/events", false);
      _log.info(this.getClass().getSimpleName() + ": Loaded " + this._events.size() + " events.");
      _log.info(this.getClass().getSimpleName() + ": Loaded " + this._activeEvents.size() + " active and " + this._disabledEvents.size() + " disable events.");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node c = this.getCurrentDocument().getFirstChild(); c != null; c = c.getNextSibling()) {
         if ("list".equalsIgnoreCase(c.getNodeName())) {
            for(Node events = c.getFirstChild(); events != null; events = events.getNextSibling()) {
               if ("event".equalsIgnoreCase(events.getNodeName())) {
                  NamedNodeMap attrs = events.getAttributes();
                  int id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
                  String nameEn = attrs.getNamedItem("nameEn").getNodeValue();
                  String nameRu = attrs.getNamedItem("nameRu").getNodeValue();
                  String impl = attrs.getNamedItem("impl").getNodeValue();
                  Class<AbstractFightEvent> eventClass = null;

                  try {
                     eventClass = Class.forName("l2e.gameserver.model.entity.events.model.impl." + impl + "Event");
                  } catch (ClassNotFoundException var15) {
                     _log.log(Level.WARNING, "Not found impl class: " + impl + "; File: " + this.getCurrentFile().getName(), (Throwable)var15);
                     continue;
                  }

                  Constructor<AbstractFightEvent> constructor = null;

                  try {
                     constructor = eventClass.getConstructor(MultiValueSet.class);
                  } catch (NoSuchMethodException | SecurityException | IllegalArgumentException var13) {
                     _log.warning("Unable to create eventClass!");
                     var13.printStackTrace();
                  }

                  MultiValueSet<String> set = new MultiValueSet<>();
                  set.set("id", id);
                  set.set("nameEn", nameEn);
                  set.set("nameRu", nameRu);
                  set.set("eventClass", "l2e.gameserver.model.entity.events.model.impl." + impl + "Event");

                  for(Node par = events.getFirstChild(); par != null; par = par.getNextSibling()) {
                     if ("parameter".equalsIgnoreCase(par.getNodeName())) {
                        attrs = par.getAttributes();
                        set.set(attrs.getNamedItem("name").getNodeValue(), attrs.getNamedItem("value").getNodeValue());
                     }
                  }

                  AbstractFightEvent event = null;

                  try {
                     event = constructor.newInstance(set);
                  } catch (InvocationTargetException | InstantiationException | IllegalArgumentException | IllegalAccessException var14) {
                     _log.warning("Unable to create event!");
                     var14.printStackTrace();
                  }

                  if (Arrays.binarySearch(Config.DISALLOW_FIGHT_EVENTS, id) >= 0) {
                     this._disabledEvents.add(event.getId());
                  } else {
                     this._activeEvents.add(event.getId());
                  }

                  this._events.put(event.getId(), event);
               }
            }
         }
      }
   }

   public AbstractFightEvent getEvent(int id) {
      return this._events.get(id);
   }

   public IntObjectMap<AbstractFightEvent> getEvents() {
      return this._events;
   }

   public List<Integer> getAcviteEvents() {
      return this._activeEvents;
   }

   public List<Integer> getDisabledEvents() {
      return this._disabledEvents;
   }

   public static FightEventParser getInstance() {
      return FightEventParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final FightEventParser _instance = new FightEventParser();
   }
}
