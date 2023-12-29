package l2e.commons.util;

import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.CharInfo;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.GameServerPacket;
import l2e.gameserver.network.serverpackets.RelationChanged;

public final class Broadcast {
   public static void toPlayersTargettingMyself(Creature character, GameServerPacket mov) {
      for(Player player : World.getInstance().getAroundPlayers(character)) {
         if (player.getTarget() == character) {
            player.sendPacket(mov);
         }
      }
   }

   public static void toKnownPlayers(Creature character, GameServerPacket mov) {
      for(Player player : World.getInstance().getAroundPlayers(character)) {
         player.sendPacket(mov);
         if (mov instanceof CharInfo && character.isPlayer()) {
            player.sendPacket(RelationChanged.update(player, character.getActingPlayer(), player));
         }
      }
   }

   public static void toKnownPlayersInRadius(Creature character, int radius, GameServerPacket... mov) {
      if (radius < 0) {
         radius = 1500;
      }

      for(Player player : World.getInstance().getAroundPlayers(character)) {
         if (character.isInsideRadius(player, radius, false, false)) {
            player.sendPacket(mov);
         }
      }
   }

   public static void toKnownPlayersInRadius(Creature character, GameServerPacket mov, int radius) {
      if (radius < 0) {
         radius = 1500;
      }

      for(Player player : World.getInstance().getAroundPlayers(character)) {
         if (character.isInsideRadius(player, radius, false, false)) {
            player.sendPacket(mov);
         }
      }
   }

   public static void toSelfAndKnownPlayers(Creature character, GameServerPacket mov) {
      if (character.isPlayer()) {
         character.sendPacket(mov);
      }

      toKnownPlayers(character, mov);
   }

   public static void toSelfAndKnownPlayersInRadius(Creature character, GameServerPacket mov, int radius) {
      if (radius < 0) {
         radius = 600;
      }

      if (character.isPlayer()) {
         character.sendPacket(mov);
      }

      for(Player player : World.getInstance().getAroundPlayers(character)) {
         if (player != null && Util.checkIfInRange(radius, character, player, false)) {
            player.sendPacket(mov);
         }
      }
   }

   public static void toAllOnlinePlayers(GameServerPacket packet) {
      for(Player player : World.getInstance().getAllPlayers()) {
         if (player != null && player.isOnline()) {
            player.sendPacket(packet);
         }
      }
   }

   public static void toAllOnlinePlayers(String text) {
      toAllOnlinePlayers(text, false);
   }

   public static void toAllOnlinePlayers(String text, boolean isCritical) {
      toAllOnlinePlayers(new CreatureSay(0, isCritical ? 18 : 10, "", text));
   }

   public static void announceToOnlinePlayers(String text, boolean isCritical) {
      CreatureSay cs;
      if (isCritical) {
         cs = new CreatureSay(0, 18, "", text);
      } else {
         cs = new CreatureSay(0, 10, "", text);
      }

      toAllOnlinePlayers(cs);
   }

   public static void toPlayersInInstance(GameServerPacket packet, int instanceId) {
      for(Player player : World.getInstance().getAllPlayers()) {
         if (player != null && player.isOnline() && player.getReflectionId() == instanceId) {
            player.sendPacket(packet);
         }
      }
   }
}
