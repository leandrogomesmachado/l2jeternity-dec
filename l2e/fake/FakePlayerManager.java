package l2e.fake;

import java.util.ArrayList;
import java.util.List;
import l2e.commons.util.Rnd;
import l2e.fake.model.FakeSupport;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.FakeArmorParser;
import l2e.gameserver.data.parser.FakeClassesParser;
import l2e.gameserver.data.parser.FakeLocationParser;
import l2e.gameserver.data.parser.FakePassiveLocationParser;
import l2e.gameserver.data.parser.FakeSkillsParser;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.player.FakeLocTemplate;
import l2e.gameserver.model.actor.templates.player.FakePassiveLocTemplate;
import l2e.gameserver.network.GameClient;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class FakePlayerManager {
   private static final FakePlayerManager _instance = new FakePlayerManager();
   private final List<FakePlayer> _fakePlayer = new ArrayList<>();

   public static final FakePlayerManager getInstance() {
      return _instance;
   }

   public FakePlayerManager() {
      if (Config.ALLOW_FAKE_PLAYERS) {
         FakePoolManager.getInstance();
         FakeClassesParser.getInstance();
         FakePlayerNameManager.getInstance();
         FakeLocationParser.getInstance();
         FakePassiveLocationParser.getInstance();
         FakeArmorParser.getInstance();
         FakeSkillsParser.getInstance();
         if (Config.ALLOW_SPAWN_FAKE_PLAYERS) {
            FakePoolManager.getInstance().schedule(new Runnable() {
               @Override
               public void run() {
                  FakePlayerManager.this.autoSpawnPassivePlayers();
                  FakePlayerManager.this.autoSpawnPlayer(Config.FAKE_PLAYERS_AMOUNT);
               }
            }, 30000L);
         }
      }
   }

   public void autoSpawnPlayer(int amount) {
      int checkAmount = amount > FakeLocationParser.getInstance().getTotalAmount() ? FakeLocationParser.getInstance().getTotalAmount() : amount;

      for(int i = 0; i < checkAmount; ++i) {
         FakePlayer fakePlayer = this.spawnPlayer();
         if (fakePlayer != null) {
            fakePlayer.assignDefaultAI(false);
            this.addFakePlayers(fakePlayer);
         }

         try {
            Thread.sleep(2000L);
         } catch (InterruptedException var6) {
         }
      }
   }

   public void autoSpawnPassivePlayers() {
      int checkAmount = FakePassiveLocationParser.getInstance().getTotalAmount();

      for(int i = 0; i < checkAmount; ++i) {
         FakePlayer fakePlayer = this.spawnPassivePlayer();
         if (fakePlayer != null) {
            fakePlayer.assignDefaultAI(true);
         }

         try {
            Thread.sleep(10000L);
         } catch (InterruptedException var5) {
         }
      }
   }

   public FakePlayer spawnPlayer() {
      FakeLocTemplate rndLoc = FakeLocationParser.getInstance().getRandomSpawnLoc();
      if (rndLoc != null) {
         FakePlayer activeChar = FakeSupport.createRandomFakePlayer(rndLoc);
         GameClient client = new GameClient(null);
         client.setDetached(true);
         client.setActiveChar(activeChar);
         activeChar.setOnlineStatus(true, false);
         client.setLogin(activeChar.getAccountNamePlayer());
         handlePlayerClanOnSpawn(activeChar);
         if (Config.PLAYER_SPAWN_PROTECTION > 0) {
            activeChar.setProtection(true);
         }

         activeChar.setFakeLocation(rndLoc);
         rndLoc.setCurrentAmount(rndLoc.getCurrentAmount() + 1);
         World.getInstance().addToAllPlayers(activeChar);
         activeChar.setX(rndLoc.getLocation().getX());
         activeChar.setY(rndLoc.getLocation().getY());
         activeChar.setZ(rndLoc.getLocation().getZ());
         client.setState(GameClient.GameClientState.IN_GAME);
         activeChar.setClient(client);
         activeChar.spawnMe();
         activeChar.onPlayerEnter();
         activeChar.heal();
         activeChar.setOnlineStatus(true, true);
         activeChar.broadcastCharInfo();
         return activeChar;
      } else {
         return null;
      }
   }

   public void respawnPassivePlayer(long delay) {
      FakePoolManager.getInstance().schedule(() -> {
         FakePlayer fakePlayer = this.spawnPassivePlayer();
         if (fakePlayer != null) {
            fakePlayer.assignDefaultAI(true);
         }
      }, delay * 1000L);
   }

   public FakePlayer spawnPassivePlayer() {
      FakePassiveLocTemplate rndLoc = FakePassiveLocationParser.getInstance().getRandomSpawnLoc();
      if (rndLoc != null) {
         FakePlayer activeChar = FakeSupport.createRandomPassiveFakePlayer(rndLoc);
         GameClient client = new GameClient(null);
         client.setDetached(true);
         client.setActiveChar(activeChar);
         activeChar.setOnlineStatus(true, false);
         client.setLogin(activeChar.getAccountNamePlayer());
         handlePlayerClanOnSpawn(activeChar);
         if (Config.PLAYER_SPAWN_PROTECTION > 0) {
            activeChar.setProtection(true);
         }

         activeChar.setFakeTerritoryLocation(rndLoc);
         rndLoc.setCurrentAmount(rndLoc.getCurrentAmount() + 1);
         World.getInstance().addToAllPlayers(activeChar);
         Location loc = rndLoc.getTerritory().getRandomLoc(0, false);
         activeChar.setX(loc.getX());
         activeChar.setY(loc.getY());
         activeChar.setZ(loc.getZ());
         activeChar.setHeading(Rnd.get(65535));
         client.setState(GameClient.GameClientState.IN_GAME);
         activeChar.setClient(client);
         activeChar.spawnMe();
         activeChar.onPlayerEnter();
         activeChar.heal();
         activeChar.setOnlineStatus(true, true);
         activeChar.broadcastCharInfo();
         return activeChar;
      } else {
         return null;
      }
   }

   public FakePlayer spawnRndPlayer(FakeLocTemplate loc) {
      if (loc != null) {
         FakePlayer activeChar = FakeSupport.createRandomFakePlayer(loc);
         GameClient client = new GameClient(null);
         client.setDetached(true);
         client.setActiveChar(activeChar);
         activeChar.setOnlineStatus(true, false);
         client.setLogin(activeChar.getAccountNamePlayer());
         handlePlayerClanOnSpawn(activeChar);
         if (Config.PLAYER_SPAWN_PROTECTION > 0) {
            activeChar.setProtection(true);
         }

         activeChar.setFakeLocation(loc);
         loc.setCurrentAmount(loc.getCurrentAmount() + 1);
         World.getInstance().addToAllPlayers(activeChar);
         activeChar.setX(loc.getLocation().getX());
         activeChar.setY(loc.getLocation().getY());
         activeChar.setZ(loc.getLocation().getZ());
         client.setState(GameClient.GameClientState.IN_GAME);
         activeChar.setClient(client);
         activeChar.spawnMe();
         activeChar.onPlayerEnter();
         activeChar.heal();
         activeChar.setOnlineStatus(true, true);
         activeChar.broadcastCharInfo();
         return activeChar;
      } else {
         return null;
      }
   }

   public void despawnFakePlayer(int objectId) {
      Player player = World.getInstance().getPlayer(objectId);
      if (player instanceof FakePlayer) {
         FakePlayer fakePlayer = (FakePlayer)player;
         if (this.removeFakePlayers(fakePlayer)) {
            fakePlayer.despawnPlayer();
         }
      }
   }

   private static void handlePlayerClanOnSpawn(FakePlayer activeChar) {
      Clan clan = activeChar.getClan();
      if (clan != null) {
         clan.getClanMember(activeChar.getObjectId()).setPlayerInstance(activeChar);
         SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_LOGGED_IN).addPcName(activeChar);
         PledgeShowMemberListUpdate update = new PledgeShowMemberListUpdate(activeChar);

         for(Player member : clan.getOnlineMembers()) {
            if (member != activeChar) {
               member.sendPacket(msg);
               member.sendPacket(update);
            }
         }
      }
   }

   public void addFakePlayers(FakePlayer player) {
      this._fakePlayer.add(player);
   }

   public boolean removeFakePlayers(FakePlayer player) {
      return this._fakePlayer.contains(player) ? this._fakePlayer.remove(player) : false;
   }

   public List<FakePlayer> getFakePlayers() {
      return this._fakePlayer;
   }
}
