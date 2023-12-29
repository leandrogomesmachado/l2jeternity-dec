package l2e.scripts.custom;

import l2e.commons.util.Util;
import l2e.gameserver.data.parser.SpawnParser;
import l2e.gameserver.listener.events.SkillUseEvent;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.network.NpcStringId;
import l2e.scripts.ai.AbstractNpcAI;

public class Sumiel extends AbstractNpcAI {
   private static final int SUMIEL = 32758;
   private static final int BURNER = 18913;
   private static final int TREASURE_BOX = 18911;
   private static final int UNLIT_TORCHLIGHT = 15540;
   private static final int TORCHLIGHT = 15485;
   private static final int SKILL_TORCH_LIGHT = 9059;
   private static final SkillHolder TRIGGER_MIRAGE = new SkillHolder(5144, 1);
   private static final Location TELEPORT1 = new Location(113187, -85388, -3424, 0);
   private static final Location TELEPORT2 = new Location(118833, -80589, -2688, 0);
   private static final int TIMER_INTERVAL = 3;
   private static final int MAX_ATTEMPTS = 3;
   private final Sumiel.MinigameRoom[] _rooms = new Sumiel.MinigameRoom[2];

   public Sumiel(String name, String descr) {
      super(name, descr);
      this.addStartNpc(32758);
      this.addFirstTalkId(32758);
      this.addTalkId(32758);
      this.addSpawnId(new int[]{18911});
      int i = 0;

      for(Spawner spawn : SpawnParser.getInstance().getSpawnData()) {
         if (spawn.getId() == 32758) {
            this._rooms[i++] = this.initRoom(spawn.getLastSpawn());
         }
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      Sumiel.MinigameRoom room = this.getRoomByManager(npc);
      switch(event) {
         case "restart":
            boolean miniGameStarted = room.getStarted();
            if (!miniGameStarted && !hasQuestItems(player, 15540)) {
               return "32758-05.htm";
            }

            if (npc.getTarget() != null && npc.getTarget() != player) {
               return "32758-04.htm";
            }

            takeItems(player, 15540, 1L);
            giveItems(player, 15485, 1L);
            this.broadcastNpcSay(npc, 22, NpcStringId.THE_FURNACE_WILL_GO_OUT_WATCH_AND_SEE);
            room.getManager().setTarget(player);
            room.setParticipant(player);
            room.setStarted(true);

            for(int i = 0; i < 9; ++i) {
               room.getOrder()[i] = getRandom(8);
            }

            this.cancelQuestTimer("hurry_up", npc, null);
            this.cancelQuestTimer("hurry_up2", npc, null);
            this.cancelQuestTimer("expire", npc, null);
            this.startQuestTimer("hurry_up", 120000L, npc, null);
            this.startQuestTimer("expire", 190000L, npc, null);
            this.startQuestTimer("start", 1000L, npc, null);
            return null;
         case "off":
            if (npc.getId() == 18913) {
               npc.setDisplayEffect(2);
               npc.setIsRunning(false);
            } else {
               for(Npc burner : room.getBurners()) {
                  burner.setDisplayEffect(2);
                  burner.setIsRunning(false);
               }
            }
            break;
         case "teleport1":
            player.teleToLocation(TELEPORT1, 0, true);
            break;
         case "teleport2":
            player.teleToLocation(TELEPORT2, 0, true);
            break;
         case "start":
            room.burnThemAll();
            this.startQuestTimer("off", 2000L, npc, null);
            this.startQuestTimer("timer", 4000L, npc, null);
            break;
         case "timer":
            if (room.getCurrentPot() < 9) {
               Npc b = room.getBurners()[room.getOrder()[room.getCurrentPot()]];
               b.setDisplayEffect(1);
               b.setIsRunning(false);
               this.startQuestTimer("off", 2000L, b, null);
               this.startQuestTimer("timer", 3000L, npc, null);
               room.setCurrentPot(room.getCurrentPot() + 1);
            } else {
               this.broadcastNpcSay(room.getManager(), 22, NpcStringId.NOW_LIGHT_THE_FURNACES_FIRE);
               room.burnThemAll();
               this.startQuestTimer("off", 2000L, npc, null);
               this.addSkillUseNotify(room.getParticipant());
               room.setCurrentPot(0);
            }
            break;
         case "hurry_up":
            this.broadcastNpcSay(npc, 22, NpcStringId.THERES_ABOUT_1_MINUTE_LEFT);
            this.startQuestTimer("hurry_up2", 60000L, npc, null);
            break;
         case "hurry_up2":
            this.broadcastNpcSay(npc, 22, NpcStringId.THERES_JUST_10_SECONDS_LEFT);
            this.startQuestTimer("expire", 10000L, npc, null);
            break;
         case "expire":
            this.broadcastNpcSay(npc, 22, NpcStringId.TIME_IS_UP_AND_YOU_HAVE_FAILED_ANY_MORE_WILL_BE_DIFFICULT);
         case "end":
            this.cancelQuestTimer("expire", npc, null);
            this.cancelQuestTimer("hurry_up", npc, null);
            this.cancelQuestTimer("hurry_up2", npc, null);
            room.getManager().setTarget(null);
            room.setParticipant(null);
            room.setStarted(false);
            room.setAttemptNumber(1);
            room.setCurrentPot(0);
            break;
         case "afterthat":
            npc.deleteMe();
      }

      return event;
   }

   @Override
   public String onFirstTalk(Npc npc, Player talker) {
      String htmltext = null;
      Sumiel.MinigameRoom room = this.getRoomByManager(npc);
      boolean miniGameStarted = room.getStarted();
      if (npc.getTarget() == null) {
         htmltext = miniGameStarted ? "32758-08.htm" : "32758.htm";
      } else if (npc.getTarget() == talker) {
         if (miniGameStarted) {
            htmltext = "32758-07.htm";
         } else {
            int attemptNumber = room.getAttemptNumber();
            if (attemptNumber == 2) {
               htmltext = "32758-02.htm";
            } else if (attemptNumber == 3) {
               htmltext = "32758-03.htm";
            }
         }
      } else {
         htmltext = "32758-04.htm";
      }

      return htmltext;
   }

   @Override
   public String onSpawn(Npc npc) {
      npc.disableCoreAI(true);
      this.startQuestTimer("afterthat", 180000L, npc, null);
      return super.onSpawn(npc);
   }

   @Override
   public boolean onSkillUse(SkillUseEvent event) {
      Sumiel.MinigameRoom room = this.getRoomByParticipant((Player)event.getCaster());
      boolean miniGameStarted = room.getStarted();
      if (miniGameStarted && event.getSkill().getId() == 9059) {
         for(GameObject obj : event.getTargets()) {
            if (obj != null && obj.isNpc()) {
               Npc npc = (Npc)obj;
               if (npc.getId() == 18913) {
                  npc.doCast(TRIGGER_MIRAGE.getSkill());
                  int pos = room.getBurnerPos(npc);
                  if (pos == room.getOrder()[room.getCurrentPot()]) {
                     if (room.getCurrentPot() < 8) {
                        npc.setDisplayEffect(1);
                        npc.setIsRunning(false);
                        this.startQuestTimer("off", 2000L, npc, null);
                        room.setCurrentPot(room.getCurrentPot() + 1);
                     } else {
                        addSpawn(18911, room.getParticipant().getLocation(), true, 0L);
                        this.broadcastNpcSay(room.getManager(), 22, NpcStringId.OH_YOUVE_SUCCEEDED);
                        room.setCurrentPot(0);
                        room.burnThemAll();
                        this.startQuestTimer("off", 2000L, room.getManager(), null);
                        this.startQuestTimer("end", 4000L, room.getManager(), null);
                     }
                  } else if (room.getAttemptNumber() == 3) {
                     this.broadcastNpcSay(room.getManager(), 22, NpcStringId.AH_IVE_FAILED_GOING_FURTHER_WILL_BE_DIFFICULT);
                     room.burnThemAll();
                     this.startQuestTimer("off", 2000L, room.getManager(), null);
                     this.removeSkillUseNotify(room.getParticipant());
                     this.startQuestTimer("end", 4000L, room.getManager(), null);
                  } else if (room.getAttemptNumber() < 3) {
                     this.broadcastNpcSay(room.getManager(), 22, NpcStringId.AH_IS_THIS_FAILURE_BUT_IT_LOOKS_LIKE_I_CAN_KEEP_GOING);
                     room.burnThemAll();
                     this.startQuestTimer("off", 2000L, room.getManager(), null);
                     room.setAttemptNumber(room.getAttemptNumber() + 1);
                  }
                  break;
               }
            }
         }
      }

      return true;
   }

   private Sumiel.MinigameRoom initRoom(Npc manager) {
      Npc[] burners = new Npc[9];
      int potNumber = 0;

      for(Spawner spawn : SpawnParser.getInstance().getSpawnData()) {
         if (spawn.getId() == 18913) {
            Npc lastSpawn = spawn.getLastSpawn();
            if (potNumber <= 8 && Util.checkIfInRange(1000, manager, lastSpawn, false)) {
               lastSpawn.setAutoAttackable(true);
               burners[potNumber++] = lastSpawn;
            }
         }
      }

      return new Sumiel.MinigameRoom(burners, manager);
   }

   private Sumiel.MinigameRoom getRoomByManager(Npc manager) {
      return this._rooms[0].getManager() == manager ? this._rooms[0] : this._rooms[1];
   }

   private Sumiel.MinigameRoom getRoomByParticipant(Player participant) {
      return this._rooms[0].getParticipant() == participant ? this._rooms[0] : this._rooms[1];
   }

   public static void main(String[] args) {
      new Sumiel(Sumiel.class.getSimpleName(), "custom");
   }

   private class MinigameRoom {
      private final Npc[] _burners;
      private final Npc _manager;
      private Player _participant;
      private boolean _started;
      private int _attemptNumber;
      private int _currentPot;
      private final int[] _order;

      public MinigameRoom(Npc[] burners, Npc manager) {
         this._burners = burners;
         this._manager = manager;
         this._participant = null;
         this._started = false;
         this._attemptNumber = 1;
         this._currentPot = 0;
         this._order = new int[9];
      }

      public int getBurnerPos(Npc npc) {
         for(int i = 0; i < 9; ++i) {
            if (npc.equals(this._burners[i])) {
               return i;
            }
         }

         return 0;
      }

      public void burnThemAll() {
         for(Npc burner : this._burners) {
            burner.setDisplayEffect(1);
            burner.setIsRunning(false);
         }
      }

      public Npc[] getBurners() {
         return this._burners;
      }

      public Npc getManager() {
         return this._manager;
      }

      public Player getParticipant() {
         return this._participant;
      }

      public void setParticipant(Player participant) {
         this._participant = participant;
      }

      public boolean getStarted() {
         return this._started;
      }

      public void setStarted(boolean started) {
         this._started = started;
      }

      public int getCurrentPot() {
         return this._currentPot;
      }

      public void setCurrentPot(int pot) {
         this._currentPot = pot;
      }

      public int getAttemptNumber() {
         return this._attemptNumber;
      }

      public void setAttemptNumber(int attempt) {
         this._attemptNumber = attempt;
      }

      public int[] getOrder() {
         return this._order;
      }
   }
}
