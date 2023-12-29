package l2e.gameserver.handler.targethandlers;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import l2e.gameserver.handler.targethandlers.impl.Area;
import l2e.gameserver.handler.targethandlers.impl.AreaCorpseMob;
import l2e.gameserver.handler.targethandlers.impl.AreaFriendly;
import l2e.gameserver.handler.targethandlers.impl.AreaSummon;
import l2e.gameserver.handler.targethandlers.impl.Aura;
import l2e.gameserver.handler.targethandlers.impl.AuraCorpseMob;
import l2e.gameserver.handler.targethandlers.impl.AuraUndeadEnemy;
import l2e.gameserver.handler.targethandlers.impl.BehindArea;
import l2e.gameserver.handler.targethandlers.impl.BehindAura;
import l2e.gameserver.handler.targethandlers.impl.ClanAll;
import l2e.gameserver.handler.targethandlers.impl.ClanMember;
import l2e.gameserver.handler.targethandlers.impl.CommandChannel;
import l2e.gameserver.handler.targethandlers.impl.CorpseClan;
import l2e.gameserver.handler.targethandlers.impl.CorpseMob;
import l2e.gameserver.handler.targethandlers.impl.CorpsePet;
import l2e.gameserver.handler.targethandlers.impl.CorpsePlayer;
import l2e.gameserver.handler.targethandlers.impl.EnemySummon;
import l2e.gameserver.handler.targethandlers.impl.FlagPole;
import l2e.gameserver.handler.targethandlers.impl.FrontArea;
import l2e.gameserver.handler.targethandlers.impl.FrontAura;
import l2e.gameserver.handler.targethandlers.impl.Ground;
import l2e.gameserver.handler.targethandlers.impl.Holy;
import l2e.gameserver.handler.targethandlers.impl.One;
import l2e.gameserver.handler.targethandlers.impl.OwnerPet;
import l2e.gameserver.handler.targethandlers.impl.Party;
import l2e.gameserver.handler.targethandlers.impl.PartyClan;
import l2e.gameserver.handler.targethandlers.impl.PartyMember;
import l2e.gameserver.handler.targethandlers.impl.PartyNotMe;
import l2e.gameserver.handler.targethandlers.impl.PartyOther;
import l2e.gameserver.handler.targethandlers.impl.Pet;
import l2e.gameserver.handler.targethandlers.impl.Self;
import l2e.gameserver.handler.targethandlers.impl.Servitor;
import l2e.gameserver.handler.targethandlers.impl.Summon;
import l2e.gameserver.handler.targethandlers.impl.Unlockable;
import l2e.gameserver.model.skills.targets.TargetType;

public class TargetHandler {
   private static final Logger _log = Logger.getLogger(TargetHandler.class.getName());
   private final Map<Enum<TargetType>, ITargetTypeHandler> _handlers = new HashMap<>();

   public static TargetHandler getInstance() {
      return TargetHandler.SingletonHolder._instance;
   }

   protected TargetHandler() {
      this.registerHandler(new Area());
      this.registerHandler(new AreaCorpseMob());
      this.registerHandler(new AreaFriendly());
      this.registerHandler(new AreaSummon());
      this.registerHandler(new Aura());
      this.registerHandler(new AuraCorpseMob());
      this.registerHandler(new AuraUndeadEnemy());
      this.registerHandler(new BehindArea());
      this.registerHandler(new BehindAura());
      this.registerHandler(new ClanAll());
      this.registerHandler(new ClanMember());
      this.registerHandler(new CommandChannel());
      this.registerHandler(new CorpseClan());
      this.registerHandler(new CorpseMob());
      this.registerHandler(new CorpsePet());
      this.registerHandler(new CorpsePlayer());
      this.registerHandler(new EnemySummon());
      this.registerHandler(new FlagPole());
      this.registerHandler(new FrontArea());
      this.registerHandler(new FrontAura());
      this.registerHandler(new Ground());
      this.registerHandler(new Holy());
      this.registerHandler(new One());
      this.registerHandler(new OwnerPet());
      this.registerHandler(new Party());
      this.registerHandler(new PartyClan());
      this.registerHandler(new PartyMember());
      this.registerHandler(new PartyNotMe());
      this.registerHandler(new PartyOther());
      this.registerHandler(new Pet());
      this.registerHandler(new Self());
      this.registerHandler(new Servitor());
      this.registerHandler(new Summon());
      this.registerHandler(new Unlockable());
      _log.info("Loaded " + this._handlers.size() + " TargetHandlers");
   }

   public void registerHandler(ITargetTypeHandler handler) {
      this._handlers.put(handler.getTargetType(), handler);
   }

   public synchronized void removeHandler(ITargetTypeHandler handler) {
      this._handlers.remove(handler.getTargetType());
   }

   public ITargetTypeHandler getHandler(Enum<TargetType> targetType) {
      return this._handlers.get(targetType);
   }

   public int size() {
      return this._handlers.size();
   }

   private static class SingletonHolder {
      protected static final TargetHandler _instance = new TargetHandler();
   }
}
