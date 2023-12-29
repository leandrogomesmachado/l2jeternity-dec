package l2e.gameserver.handler.skillhandlers;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import l2e.gameserver.handler.skillhandlers.impl.BallistaBomb;
import l2e.gameserver.handler.skillhandlers.impl.Blow;
import l2e.gameserver.handler.skillhandlers.impl.Continuous;
import l2e.gameserver.handler.skillhandlers.impl.ConvertItem;
import l2e.gameserver.handler.skillhandlers.impl.CpDamPercent;
import l2e.gameserver.handler.skillhandlers.impl.DeluxeKey;
import l2e.gameserver.handler.skillhandlers.impl.Detection;
import l2e.gameserver.handler.skillhandlers.impl.Disablers;
import l2e.gameserver.handler.skillhandlers.impl.Dummy;
import l2e.gameserver.handler.skillhandlers.impl.EnergyReplenish;
import l2e.gameserver.handler.skillhandlers.impl.EnergySpend;
import l2e.gameserver.handler.skillhandlers.impl.ExtractStone;
import l2e.gameserver.handler.skillhandlers.impl.Fishing;
import l2e.gameserver.handler.skillhandlers.impl.FishingSkill;
import l2e.gameserver.handler.skillhandlers.impl.GetPlayer;
import l2e.gameserver.handler.skillhandlers.impl.Manadam;
import l2e.gameserver.handler.skillhandlers.impl.Mdam;
import l2e.gameserver.handler.skillhandlers.impl.NegateEffects;
import l2e.gameserver.handler.skillhandlers.impl.NornilsPower;
import l2e.gameserver.handler.skillhandlers.impl.Pdam;
import l2e.gameserver.handler.skillhandlers.impl.Resurrect;
import l2e.gameserver.handler.skillhandlers.impl.Sow;
import l2e.gameserver.handler.skillhandlers.impl.TakeFort;
import l2e.gameserver.handler.skillhandlers.impl.Trap;
import l2e.gameserver.handler.skillhandlers.impl.Unlock;
import l2e.gameserver.model.skills.SkillType;

public class SkillHandler {
   private static Logger _log = Logger.getLogger(SkillHandler.class.getName());
   private final Map<Integer, ISkillHandler> _handlers = new HashMap<>();

   public static SkillHandler getInstance() {
      return SkillHandler.SingletonHolder._instance;
   }

   protected SkillHandler() {
      this.registerHandler(new BallistaBomb());
      this.registerHandler(new Blow());
      this.registerHandler(new Continuous());
      this.registerHandler(new ConvertItem());
      this.registerHandler(new CpDamPercent());
      this.registerHandler(new DeluxeKey());
      this.registerHandler(new Detection());
      this.registerHandler(new Disablers());
      this.registerHandler(new Dummy());
      this.registerHandler(new EnergyReplenish());
      this.registerHandler(new EnergySpend());
      this.registerHandler(new ExtractStone());
      this.registerHandler(new Fishing());
      this.registerHandler(new FishingSkill());
      this.registerHandler(new GetPlayer());
      this.registerHandler(new Manadam());
      this.registerHandler(new Mdam());
      this.registerHandler(new NegateEffects());
      this.registerHandler(new NornilsPower());
      this.registerHandler(new Pdam());
      this.registerHandler(new Resurrect());
      this.registerHandler(new Sow());
      this.registerHandler(new TakeFort());
      this.registerHandler(new Trap());
      this.registerHandler(new Unlock());
      _log.info("Loaded " + this._handlers.size() + " SkillHandlers");
   }

   public void registerHandler(ISkillHandler handler) {
      SkillType[] types = handler.getSkillIds();

      for(SkillType t : types) {
         if (this._handlers.containsKey(t.ordinal())) {
            _log.fine(
               "VoicedCommand: dublicate bypass registered! First handler: "
                  + this._handlers.get(t.ordinal()).getClass().getSimpleName()
                  + " second: "
                  + handler.getClass().getSimpleName()
            );
            this._handlers.remove(t.ordinal());
         }

         this._handlers.put(t.ordinal(), handler);
      }
   }

   public synchronized void removeHandler(ISkillHandler handler) {
      SkillType[] types = handler.getSkillIds();

      for(SkillType t : types) {
         this._handlers.remove(t.ordinal());
      }
   }

   public ISkillHandler getHandler(SkillType skillType) {
      return this._handlers.get(skillType.ordinal());
   }

   public int size() {
      return this._handlers.size();
   }

   private static class SingletonHolder {
      protected static final SkillHandler _instance = new SkillHandler();
   }
}
