package l2e.gameserver.model.zone.type;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.DoorInstance;
import l2e.gameserver.model.actor.instance.OlympiadManagerInstance;
import l2e.gameserver.model.olympiad.OlympiadGameTask;
import l2e.gameserver.model.zone.AbstractZoneSettings;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.ZoneRespawn;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExOlympiadMatchEnd;
import l2e.gameserver.network.serverpackets.ExOlympiadUserInfo;
import l2e.gameserver.network.serverpackets.GameServerPacket;

public class OlympiadStadiumZone extends ZoneRespawn {
   private List<Location> _spectatorLocations;

   public OlympiadStadiumZone(int id) {
      super(id);
      AbstractZoneSettings settings = ZoneManager.getSettings(this.getName());
      if (settings == null) {
         settings = new OlympiadStadiumZone.Settings();
      }

      this.setSettings(settings);
   }

   public OlympiadStadiumZone.Settings getSettings() {
      return (OlympiadStadiumZone.Settings)super.getSettings();
   }

   public final void registerTask(OlympiadGameTask task) {
      this.getSettings().setTask(task);
   }

   public final void openDoors() {
      for(DoorInstance door : ReflectionManager.getInstance().getReflection(this.getReflectionId()).getDoors()) {
         if (door != null && door.isClosed()) {
            door.openMe();
         }
      }
   }

   public final void closeDoors() {
      for(DoorInstance door : ReflectionManager.getInstance().getReflection(this.getReflectionId()).getDoors()) {
         if (door != null && door.isOpened()) {
            door.closeMe();
         }
      }
   }

   public final void spawnBuffers() {
      for(Npc buffer : ReflectionManager.getInstance().getReflection(this.getReflectionId()).getNpcs()) {
         if (buffer instanceof OlympiadManagerInstance && !buffer.isVisible()) {
            buffer.spawnMe();
         }
      }
   }

   public final void deleteBuffers() {
      for(Npc buffer : ReflectionManager.getInstance().getReflection(this.getReflectionId()).getNpcs()) {
         if (buffer instanceof OlympiadManagerInstance && buffer.isVisible()) {
            buffer.decayMe();
         }
      }
   }

   public final void broadcastStatusUpdate(Player player) {
      ExOlympiadUserInfo packet = new ExOlympiadUserInfo(player);

      for(Player target : this.getPlayersInside()) {
         if (target != null && (target.inObserverMode() || target.getOlympiadSide() != player.getOlympiadSide())) {
            target.sendPacket(packet);
         }
      }
   }

   public final void broadcastPacketToObservers(GameServerPacket packet) {
      for(Creature character : this.getCharactersInside()) {
         if (character != null && character.isPlayer() && character.getActingPlayer().inObserverMode()) {
            character.sendPacket(packet);
         }
      }
   }

   @Override
   protected final void onEnter(Creature character) {
      if (this.getSettings().getOlympiadTask() != null && this.getSettings().getOlympiadTask().isBattleStarted() && character.isPlayer()) {
         character.sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE);
         this.getSettings().getOlympiadTask().getGame().sendOlympiadInfo(character);
      }

      if (character.isPlayable()) {
         Player player = character.getActingPlayer();
         if (player != null && player.hasPet()) {
            player.getSummon().unSummon(player);
         }
      }
   }

   @Override
   protected final void onExit(Creature character) {
      character.abortAttack();
      character.abortCast();
      if (character.isPlayer()) {
         character.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
         character.sendPacket(ExOlympiadMatchEnd.STATIC_PACKET);
      }
   }

   public final void updateZoneStatusForCharactersInside(boolean forceStoped) {
      if (this.getSettings().getOlympiadTask() != null) {
         boolean battleStarted = this.getSettings().getOlympiadTask().isBattleStarted() && !forceStoped;
         if (battleStarted) {
            this.addZoneId(ZoneId.PVP);
         } else {
            this.getZoneId().clear();
         }

         for(Creature character : this.getCharactersInside()) {
            if (character != null) {
               if (battleStarted) {
                  if (character.isPlayer()) {
                     character.sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE);
                  }
               } else {
                  character.abortAttack();
                  character.abortCast();
                  if (character.isPlayer()) {
                     if (character.hasSummon()) {
                        character.getSummon().cancelAction();
                     }

                     character.sendPacket(ExOlympiadMatchEnd.STATIC_PACKET);
                  }
               }
            }
         }

         if (!battleStarted) {
            this.getZoneId().clear();
         }
      }
   }

   @Override
   public void onDieInside(Creature character) {
   }

   @Override
   public void onReviveInside(Creature character) {
   }

   @Override
   public void parseLoc(int x, int y, int z, String type) {
      if (type != null && type.equals("spectatorSpawn")) {
         if (this._spectatorLocations == null) {
            this._spectatorLocations = new ArrayList<>();
         }

         this._spectatorLocations.add(new Location(x, y, z));
      } else {
         super.parseLoc(x, y, z, type);
      }
   }

   public List<Location> getSpectatorSpawns() {
      return this._spectatorLocations;
   }

   public final class Settings extends AbstractZoneSettings {
      private OlympiadGameTask _task = null;

      public OlympiadGameTask getOlympiadTask() {
         return this._task;
      }

      protected void setTask(OlympiadGameTask task) {
         this._task = task;
      }

      @Override
      public void clear() {
         this._task = null;
      }
   }
}
