package l2e.gameserver.handler.communityhandlers;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.handler.communityhandlers.impl.CommunityAcademy;
import l2e.gameserver.handler.communityhandlers.impl.CommunityAuction;
import l2e.gameserver.handler.communityhandlers.impl.CommunityBalancer;
import l2e.gameserver.handler.communityhandlers.impl.CommunityBalancerSkill;
import l2e.gameserver.handler.communityhandlers.impl.CommunityBuffer;
import l2e.gameserver.handler.communityhandlers.impl.CommunityCertification;
import l2e.gameserver.handler.communityhandlers.impl.CommunityClan;
import l2e.gameserver.handler.communityhandlers.impl.CommunityClassMaster;
import l2e.gameserver.handler.communityhandlers.impl.CommunityEvents;
import l2e.gameserver.handler.communityhandlers.impl.CommunityFacebook;
import l2e.gameserver.handler.communityhandlers.impl.CommunityForge;
import l2e.gameserver.handler.communityhandlers.impl.CommunityFriend;
import l2e.gameserver.handler.communityhandlers.impl.CommunityGeneral;
import l2e.gameserver.handler.communityhandlers.impl.CommunityLink;
import l2e.gameserver.handler.communityhandlers.impl.CommunityNpcCalc;
import l2e.gameserver.handler.communityhandlers.impl.CommunityRaidBoss;
import l2e.gameserver.handler.communityhandlers.impl.CommunityRanking;
import l2e.gameserver.handler.communityhandlers.impl.CommunityServices;
import l2e.gameserver.handler.communityhandlers.impl.CommunityTeleport;
import l2e.gameserver.handler.communityhandlers.impl.CommunityTopic;
import l2e.gameserver.model.entity.auction.AuctionsManager;

public class CommunityBoardHandler {
   private static Logger _log = Logger.getLogger(CommunityBoardHandler.class.getName());
   private final Map<String, ICommunityBoardHandler> _handlers = new HashMap<>();

   private CommunityBoardHandler() {
      this.registerHandler(new CommunityAcademy());
      this.registerHandler(new CommunityGeneral());
      this.registerHandler(new CommunityForge());
      this.registerHandler(new CommunityRaidBoss());
      this.registerHandler(new CommunityBuffer());
      this.registerHandler(new CommunityClan());
      this.registerHandler(new CommunityClassMaster());
      this.registerHandler(new CommunityEvents());
      this.registerHandler(new CommunityFriend());
      this.registerHandler(new CommunityLink());
      this.registerHandler(new CommunityServices());
      this.registerHandler(new CommunityRanking());
      this.registerHandler(new CommunityTeleport());
      this.registerHandler(new CommunityTopic());
      this.registerHandler(new CommunityAuction());
      AuctionsManager.getInstance();
      this.registerHandler(new CommunityBalancer());
      this.registerHandler(new CommunityBalancerSkill());
      this.registerHandler(new CommunityCertification());
      this.registerHandler(new CommunityNpcCalc());
      this.registerHandler(new CommunityFacebook());
      _log.info("Loaded " + this._handlers.size() + " CommunityBoardHandlers.");
   }

   public void registerHandler(ICommunityBoardHandler commHandler) {
      for(String bypass : commHandler.getBypassCommands()) {
         if (this._handlers.containsKey(bypass)) {
            _log.fine(
               "CommunityBoard: dublicate bypass registered! First handler: "
                  + this._handlers.get(bypass).getClass().getSimpleName()
                  + " second: "
                  + commHandler.getClass().getSimpleName()
            );
            this._handlers.remove(bypass);
         }

         this._handlers.put(bypass, commHandler);
      }
   }

   public ICommunityBoardHandler getHandler(String bypass) {
      if (Config.ALLOW_COMMUNITY && !this._handlers.isEmpty()) {
         if (Config.DISABLE_COMMUNITY_BYPASSES.contains(bypass)) {
            return null;
         } else {
            for(Entry<String, ICommunityBoardHandler> entry : this._handlers.entrySet()) {
               if (bypass.contains(entry.getKey())) {
                  return entry.getValue();
               }
            }

            return null;
         }
      } else {
         return null;
      }
   }

   public int size() {
      return this._handlers.size();
   }

   public static CommunityBoardHandler getInstance() {
      return CommunityBoardHandler.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final CommunityBoardHandler _instance = new CommunityBoardHandler();
   }
}
