package l2e.gameserver.model.entity.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import l2e.commons.util.Rnd;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.entity.events.model.template.WorldEventDrop;

public class EventsDropManager {
   private final Map<Integer, EventsDropManager.DropRule> _dropRules = new ConcurrentHashMap<>();

   public EventsDropManager() {
      this._dropRules.clear();
   }

   public void addRule(int eventId, List<WorldEventDrop> dropList, boolean lvlControl) {
      EventsDropManager.DropRule rule = new EventsDropManager.DropRule();
      rule._levDifferenceControl = lvlControl;

      for(WorldEventDrop drop : dropList) {
         rule._items.add(drop);
      }

      this._dropRules.put(eventId, rule);
   }

   public void removeRule(int eventId) {
      EventsDropManager.DropRule rule = this._dropRules.get(eventId);
      if (rule != null) {
         this._dropRules.remove(eventId);
      }
   }

   public int[] calculateRewardItem(NpcTemplate npcTemplate, Creature lastAttacker) {
      int[] res = new int[]{0, 0};
      int lvlDif = lastAttacker.getLevel() - npcTemplate.getLevel();
      List<WorldEventDrop> rewards = new ArrayList<>();
      if (!this._dropRules.isEmpty()) {
         for(EventsDropManager.DropRule tmp : this._dropRules.values()) {
            if (tmp != null && (!tmp._levDifferenceControl || lvlDif <= 7 && lvlDif >= -7) && tmp._items != null && !tmp._items.isEmpty()) {
               for(WorldEventDrop drop : tmp._items) {
                  if (npcTemplate.getLevel() >= drop.getMinLevel() && npcTemplate.getLevel() <= drop.getMaxLevel() && Rnd.chance(drop.getChance())) {
                     rewards.add(drop);
                  }
               }
            }
         }
      }

      if (rewards.size() > 0) {
         int rndRew = Rnd.get(rewards.size());
         res[0] = rewards.get(rndRew).getId();
         res[1] = (int)(
            rewards.get(rndRew).getMaxCount() > 0L
               ? Rnd.get(rewards.get(rndRew).getMinCount(), rewards.get(rndRew).getMaxCount())
               : rewards.get(rndRew).getMinCount()
         );
      }

      return res;
   }

   public Map<Integer, EventsDropManager.DropRule> getEventRules() {
      return this._dropRules;
   }

   public static final EventsDropManager getInstance() {
      return EventsDropManager.SingletonHolder._instance;
   }

   private static class DropRule {
      public boolean _levDifferenceControl;
      public List<WorldEventDrop> _items = new ArrayList<>();

      private DropRule() {
      }
   }

   private static class SingletonHolder {
      protected static final EventsDropManager _instance = new EventsDropManager();
   }
}
