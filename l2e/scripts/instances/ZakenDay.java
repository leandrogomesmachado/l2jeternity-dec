package l2e.scripts.instances;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.GameTimeController;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.reflection.ReflectionTemplate;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.model.entity.Reflection;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.network.NpcStringId;

public class ZakenDay extends AbstractReflection {
   private static final int[][] ROOM_SPAWN = new int[][]{
      {54240, 220133, -3498, 1, 3, 4, 6},
      {54240, 218073, -3498, 2, 5, 4, 7},
      {55265, 219095, -3498, 4, 9, 6, 7},
      {56289, 220133, -3498, 8, 11, 6, 9},
      {56289, 218073, -3498, 10, 12, 7, 9},
      {54240, 220133, -3226, 13, 15, 16, 18},
      {54240, 218073, -3226, 14, 17, 16, 19},
      {55265, 219095, -3226, 21, 16, 19, 18},
      {56289, 220133, -3226, 20, 23, 21, 18},
      {56289, 218073, -3226, 22, 24, 19, 21},
      {54240, 220133, -2954, 25, 27, 28, 30},
      {54240, 218073, -2954, 26, 29, 28, 31},
      {55265, 219095, -2954, 33, 28, 31, 30},
      {56289, 220133, -2954, 32, 35, 30, 33},
      {56289, 218073, -2954, 34, 36, 31, 33}
   };
   private static final int[][] CANDLE_SPAWN = new int[][]{
      {53313, 220133, -3498},
      {53313, 218079, -3498},
      {54240, 221045, -3498},
      {54325, 219095, -3498},
      {54240, 217155, -3498},
      {55257, 220028, -3498},
      {55257, 218172, -3498},
      {56280, 221045, -3498},
      {56195, 219095, -3498},
      {56280, 217155, -3498},
      {57215, 220133, -3498},
      {57215, 218079, -3498},
      {53313, 220133, -3226},
      {53313, 218079, -3226},
      {54240, 221045, -3226},
      {54325, 219095, -3226},
      {54240, 217155, -3226},
      {55257, 220028, -3226},
      {55257, 218172, -3226},
      {56280, 221045, -3226},
      {56195, 219095, -3226},
      {56280, 217155, -3226},
      {57215, 220133, -3226},
      {57215, 218079, -3226},
      {53313, 220133, -2954},
      {53313, 218079, -2954},
      {54240, 221045, -2954},
      {54325, 219095, -2954},
      {54240, 217155, -2954},
      {55257, 220028, -2954},
      {55257, 218172, -2954},
      {56280, 221045, -2954},
      {56195, 219095, -2954},
      {56280, 217155, -2954},
      {57215, 220133, -2954},
      {57215, 218079, -2954}
   };

   public ZakenDay(String name, String descr) {
      super(name, descr);
      this.addStartNpc(32713);
      this.addTalkId(32713);
      this.addFirstTalkId(32705);
      this.addKillId(new int[]{29176, 29181});
   }

   private final synchronized void enterInstance(Player player, Npc npc, boolean is83) {
      if (this.enterInstance(player, npc, new ZakenDay.ZakenDayWorld(), is83 ? 135 : 133)) {
         ReflectionWorld world = ReflectionManager.getInstance().getPlayerWorld(player);
         ((ZakenDay.ZakenDayWorld)world).startTime = System.currentTimeMillis();
         ((ZakenDay.ZakenDayWorld)world)._is83 = world.getTemplateId() == 135;
         this.spawnCandles(world);
      }
   }

   @Override
   protected void onTeleportEnter(Player player, ReflectionTemplate template, ReflectionWorld world, boolean firstEntrance) {
      if (firstEntrance) {
         world.addAllowed(player.getObjectId());
         ((ZakenDay.ZakenDayWorld)world).playersInside.add(player);
         player.getAI().setIntention(CtrlIntention.IDLE);
         player.setReflectionId(world.getReflectionId());
         Location teleLoc = template.getTeleportCoord();
         player.teleToLocation(teleLoc, true);
         if (player.hasSummon()) {
            player.getSummon().getAI().setIntention(CtrlIntention.IDLE);
            player.getSummon().setReflectionId(world.getReflectionId());
            player.getSummon().teleToLocation(teleLoc, true);
         }
      } else {
         player.getAI().setIntention(CtrlIntention.IDLE);
         player.setReflectionId(world.getReflectionId());
         Location teleLoc = template.getTeleportCoord();
         player.teleToLocation(teleLoc, true);
         if (player.hasSummon()) {
            player.getSummon().getAI().setIntention(CtrlIntention.IDLE);
            player.getSummon().setReflectionId(world.getReflectionId());
            player.getSummon().teleToLocation(teleLoc, true);
         }
      }
   }

   @Override
   protected boolean checkConditions(Player player, Npc npc, ReflectionTemplate template) {
      boolean checkTime = template.getParams().getBool("checkValidTime");
      if (!checkTime || this.getTimeHour() >= 4 && this.getTimeHour() <= 24) {
         return super.checkConditions(player, npc, template);
      } else {
         player.sendMessage(new ServerMessage("Zaken.INVALID_TIME", player.getLang()).toString());
         return false;
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      if (event.equalsIgnoreCase("60")) {
         this.enterInstance(player, npc, false);
      } else if (event.equalsIgnoreCase("83")) {
         this.enterInstance(player, npc, true);
      } else {
         ReflectionWorld tmpWorld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
         if (tmpWorld != null && tmpWorld instanceof ZakenDay.ZakenDayWorld) {
            ZakenDay.ZakenDayWorld world = (ZakenDay.ZakenDayWorld)tmpWorld;
            if (ReflectionManager.getInstance().getWorld(world.getReflectionId()) != world) {
               return null;
            }

            Reflection inst = ReflectionManager.getInstance().getReflection(world.getReflectionId());
            if (inst != null) {
               long burnDelay = inst.getParams().getLong("burnCandleDelay") * 1000L;
               long zakenDelay = inst.getParams().getLong("zakenSpawnDelay") * 1000L;
               switch(event) {
                  case "burn_good_candle":
                     if (npc.getRightHandItem() == 0) {
                        npc.setRHandId(15280);
                        this.startQuestTimer("burn_blue_candle", burnDelay, npc, player);
                        if (world._blueFounded == 4) {
                           this.startQuestTimer("spawn_zaken", zakenDelay, npc, player);
                        }
                     }
                     break;
                  case "burn_blue_candle":
                     if (npc.getRightHandItem() == 15280) {
                        npc.setRHandId(15302);
                     }
                     break;
                  case "burn_bad_candle":
                     if (npc.getRightHandItem() == 0) {
                        npc.setRHandId(15280);
                        this.startQuestTimer("burn_red_candle", burnDelay, npc, player);
                     }
                     break;
                  case "burn_red_candle":
                     if (npc.getRightHandItem() == 15280) {
                        int room = this.getRoomByCandle(world, npc);
                        npc.setRHandId(15281);
                        this.manageScreenMsg(world, NpcStringId.THE_CANDLES_CAN_LEAD_YOU_TO_ZAKEN_DESTROY_HIM);
                        this.spawnInRoom(world._is83 ? 29182 : 29023, room, player, world);
                        this.spawnInRoom(world._is83 ? 29183 : 29024, room, player, world);
                        this.spawnInRoom(world._is83 ? 29185 : 29027, room, player, world);
                        this.spawnInRoom(world._is83 ? 29184 : 29026, room, player, world);
                     }
                     break;
                  case "spawn_zaken":
                     if (world._is83) {
                        this.manageScreenMsg(world, NpcStringId.WHO_DARES_AWKAWEN_THE_MIGHTY_ZAKEN);
                     }

                     this.spawnInRoom(world._is83 ? 29181 : 29176, world.zakenRoom, player, world);
                     this.spawnInRoom(world._is83 ? 29182 : 29023, world.zakenRoom, player, world);
                     this.spawnInRoom(world._is83 ? 29185 : 29027, world.zakenRoom, player, world);
                     this.spawnInRoom(world._is83 ? 29184 : 29026, world.zakenRoom, player, world);
                     this.spawnInRoom(world._is83 ? 29183 : 29024, world.zakenRoom, player, world);
               }
            }
         }
      }

      return super.onAdvEvent(event, npc, player);
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      ReflectionWorld tmpWorld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
      if (tmpWorld != null && tmpWorld instanceof ZakenDay.ZakenDayWorld) {
         ZakenDay.ZakenDayWorld world = (ZakenDay.ZakenDayWorld)tmpWorld;
         if (ReflectionManager.getInstance().getWorld(world.getReflectionId()) != world) {
            return null;
         }

         long finishDiff = System.currentTimeMillis() - world.startTime;
         int npcId = npc.getId();
         if (npcId == 29176) {
            this.finishInstance(world, true);
         } else if (npcId == 29181) {
            this.finishInstance(world, true);
         }

         if (finishDiff <= 900000L) {
            if (npc.getId() == 29181 && killer.isInParty() && killer.getParty().getCommandChannel() != null) {
               for(Player player : killer.getParty().getCommandChannel().getMembers()) {
                  this.timebonus(world, npc, player);
               }
            } else if (npc.getId() == 29181 && killer.isInParty()) {
               for(Player player : killer.getParty().getMembers()) {
                  this.timebonus(world, npc, player);
               }
            } else if (npc.getId() == 29181 && !killer.isInParty()) {
               this.timebonus(world, npc, killer);
            }
         }
      }

      return null;
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
      if (tmpworld != null && tmpworld instanceof ZakenDay.ZakenDayWorld) {
         ZakenDay.ZakenDayWorld world = (ZakenDay.ZakenDayWorld)tmpworld;
         if (ReflectionManager.getInstance().getWorld(world.getReflectionId()) != world) {
            return null;
         }

         boolean isBlue = npc.getVariables().getInteger("isBlue", 0) == 1;
         if (npc.isScriptValue(0)) {
            npc.setScriptValue(1);
            if (isBlue) {
               world._blueFounded++;
               this.startQuestTimer("burn_good_candle", 500L, npc, player);
            } else {
               this.startQuestTimer("burn_bad_candle", 500L, npc, player);
            }
         }
      }

      return null;
   }

   private void timebonus(ZakenDay.ZakenDayWorld world, Npc npc, Player player) {
      long finishDiff = System.currentTimeMillis() - world.startTime;
      if (player.isInsideRadius(npc, 2000, false, false)) {
         int rand = getRandom(100);
         if (finishDiff <= 300000L) {
            if (rand < 50) {
               player.addItem("Zaken", 15763, 1L, npc, true);
            }
         } else if (finishDiff <= 600000L) {
            if (rand < 30) {
               player.addItem("Zaken", 15764, 1L, npc, true);
            }
         } else if (finishDiff <= 900000L && rand < 25) {
            player.addItem("Zaken", 15763, 1L, npc, true);
         }
      }
   }

   private void spawnCandles(ReflectionWorld world) {
      if (world != null && world instanceof ZakenDay.ZakenDayWorld) {
         if (ReflectionManager.getInstance().getWorld(world.getReflectionId()) != world) {
            return;
         }

         ZakenDay.ZakenDayWorld tmpworld = (ZakenDay.ZakenDayWorld)world;
         List<Npc> candles = new ArrayList<>();
         tmpworld.zakenRoom = getRandom(1, 15);

         for(int i = 0; i < 36; ++i) {
            Npc candle = addSpawn(32705, CANDLE_SPAWN[i][0], CANDLE_SPAWN[i][1], CANDLE_SPAWN[i][2], 0, false, 0L, false);
            candle.getVariables().set("candleId", i + 1);
            candle.setReflectionId(world.getReflectionId());
            candles.add(candle);
         }

         for(int i = 3; i < 7; ++i) {
            candles.get(ROOM_SPAWN[tmpworld.zakenRoom - 1][i] - 1).getVariables().set("isBlue", 1);
         }
      }
   }

   private int getRoomByCandle(ZakenDay.ZakenDayWorld world, Npc npc) {
      if (ReflectionManager.getInstance().getWorld(world.getReflectionId()) != world) {
         return 0;
      } else {
         int candleId = npc.getVariables().getInteger("candleId", 0);

         for(int i = 0; i < 15; ++i) {
            if (ROOM_SPAWN[i][3] == candleId || ROOM_SPAWN[i][4] == candleId) {
               return i + 1;
            }
         }

         if (candleId == 6 || candleId == 7) {
            return 3;
         } else if (candleId == 18 || candleId == 19) {
            return 8;
         } else {
            return candleId != 30 && candleId != 31 ? 0 : 13;
         }
      }
   }

   private void spawnInRoom(int npcId, int roomId, Player player, ZakenDay.ZakenDayWorld world) {
      if (ReflectionManager.getInstance().getWorld(world.getReflectionId()) == world) {
         if (player != null && npcId != 29176 && npcId != 29181) {
            Npc mob = addSpawn(
               npcId,
               ROOM_SPAWN[roomId - 1][0] + getRandom(350),
               ROOM_SPAWN[roomId - 1][1] + getRandom(350),
               ROOM_SPAWN[roomId - 1][2],
               0,
               false,
               0L,
               false,
               world.getReflectionId()
            );
            mob.setRunning();
            mob.setTarget(player);
            ((Attackable)mob).addDamageHate(player, 0, 999);
            mob.getAI().setIntention(CtrlIntention.ATTACK, player);
         } else {
            addSpawn(npcId, ROOM_SPAWN[roomId - 1][0], ROOM_SPAWN[roomId - 1][1], ROOM_SPAWN[roomId - 1][2], 0, false, 0L, false, world.getReflectionId());
         }
      }
   }

   private void manageScreenMsg(ZakenDay.ZakenDayWorld world, NpcStringId stringId) {
      for(Player players : world.playersInside) {
         if (players != null && players.getReflectionId() == world.getReflectionId()) {
            showOnScreenMsg(players, stringId, 2, 6000, new String[0]);
         }
      }
   }

   private int getTimeHour() {
      return GameTimeController.getInstance().getGameTime() / 60 % 24;
   }

   public static void main(String[] args) {
      new ZakenDay(ZakenDay.class.getSimpleName(), "instances");
   }

   private class ZakenDayWorld extends ReflectionWorld {
      private final List<Player> playersInside = new ArrayList<>();
      private long startTime = 0L;
      private boolean _is83;
      private int zakenRoom;
      private int _blueFounded;

      private ZakenDayWorld() {
      }
   }
}
