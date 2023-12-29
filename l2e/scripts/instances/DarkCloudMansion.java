package l2e.scripts.instances;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.reflection.ReflectionTemplate;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.model.entity.Reflection;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.gameserver.network.serverpackets.NpcSay;

public class DarkCloudMansion extends AbstractReflection {
   private static int[] BM = new int[]{22272, 22273, 22274};
   private static int[] BS = new int[]{18371, 18372, 18373, 18374, 18375, 18376, 18377};
   private static NpcStringId[] _spawnChat = new NpcStringId[]{
      NpcStringId.IM_THE_REAL_ONE,
      NpcStringId.PICK_ME,
      NpcStringId.TRUST_ME,
      NpcStringId.NOT_THAT_DUDE_IM_THE_REAL_ONE,
      NpcStringId.DONT_BE_FOOLED_DONT_BE_FOOLED_IM_THE_REAL_ONE
   };
   private static NpcStringId[] _decayChat = new NpcStringId[]{NpcStringId.IM_THE_REAL_ONE_PHEW, NpcStringId.CANT_YOU_EVEN_FIND_OUT, NpcStringId.FIND_ME};
   private static NpcStringId[] _successChat = new NpcStringId[]{
      NpcStringId.HUH_HOW_DID_YOU_KNOW_IT_WAS_ME, NpcStringId.EXCELLENT_CHOICE_TEEHEE, NpcStringId.YOUVE_DONE_WELL, NpcStringId.OH_VERY_SENSIBLE
   };
   private static NpcStringId[] _faildChat = new NpcStringId[]{NpcStringId.YOUVE_BEEN_FOOLED, NpcStringId.SORRY_BUT_IM_THE_FAKE_ONE};
   private static int[][] MonolithOrder = new int[][]{
      {1, 2, 3, 4, 5, 6},
      {6, 5, 4, 3, 2, 1},
      {4, 5, 6, 3, 2, 1},
      {2, 6, 3, 5, 1, 4},
      {4, 1, 5, 6, 2, 3},
      {3, 5, 1, 6, 2, 4},
      {6, 1, 3, 4, 5, 2},
      {5, 6, 1, 2, 4, 3},
      {5, 2, 6, 3, 4, 1},
      {1, 5, 2, 6, 3, 4},
      {1, 2, 3, 6, 5, 4},
      {6, 4, 3, 1, 5, 2},
      {3, 5, 2, 4, 1, 6},
      {3, 2, 4, 5, 1, 6},
      {5, 4, 3, 1, 6, 2}
   };
   private static int[][] GolemSpawn = new int[][]{
      {18369, 148060, 181389},
      {18370, 147910, 181173},
      {18369, 147810, 181334},
      {18370, 147713, 181179},
      {18369, 147569, 181410},
      {18370, 147810, 181517},
      {18369, 147805, 181281}
   };
   private static int[][] ColumnRows = new int[][]{{1, 1, 0, 1, 0}, {0, 1, 1, 0, 1}, {1, 0, 1, 1, 0}, {0, 1, 0, 1, 1}, {1, 0, 1, 0, 1}};
   private static int[][] Beleths = new int[][]{
      {1, 0, 1, 0, 1, 0, 0},
      {0, 0, 1, 0, 1, 1, 0},
      {0, 0, 0, 1, 0, 1, 1},
      {1, 0, 1, 1, 0, 0, 0},
      {1, 1, 0, 0, 0, 1, 0},
      {0, 1, 0, 1, 0, 1, 0},
      {0, 0, 0, 1, 1, 1, 0},
      {1, 0, 1, 0, 0, 1, 0},
      {0, 1, 1, 0, 0, 0, 1}
   };

   public DarkCloudMansion(String name, String descr) {
      super(name, descr);
      this.addFirstTalkId(new int[]{32291, 32324});
      this.addStartNpc(32282);
      this.addTalkId(new int[]{32282, 32291});
      this.addAttackId(new int[]{18369, 18370, 18371, 18372, 18373, 18374, 18375, 18376, 18377, 22402});
      this.addKillId(new int[]{18371, 18372, 18373, 18374, 18375, 18376, 18377, 22318, 22319, 22272, 22273, 22274, 18369, 18370, 22402, 22264});
   }

   private final synchronized void enterInstance(Player player, Npc npc) {
      if (this.enterInstance(player, npc, new DarkCloudMansion.DMCWorld(), 9)) {
         ReflectionWorld world = ReflectionManager.getInstance().getPlayerWorld(player);
         this.runStartRoom((DarkCloudMansion.DMCWorld)world);
      }
   }

   @Override
   protected void onTeleportEnter(Player player, ReflectionTemplate template, ReflectionWorld world, boolean firstEntrance) {
      if (firstEntrance) {
         world.addAllowed(player.getObjectId());
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

   protected void runStartRoom(DarkCloudMansion.DMCWorld world) {
      world.setStatus(0);
      DarkCloudMansion.DMCRoom StartRoom = new DarkCloudMansion.DMCRoom();
      DarkCloudMansion.DMCNpc thisnpc = new DarkCloudMansion.DMCNpc();
      thisnpc.npc = addSpawn(22272, 146817, 180335, -6117, 0, false, 0L, false, world.getReflectionId());
      StartRoom.npcList.add(thisnpc);
      thisnpc.npc.setIsNoRndWalk(true);
      thisnpc = new DarkCloudMansion.DMCNpc();
      thisnpc.npc = addSpawn(22272, 146741, 180589, -6117, 0, false, 0L, false, world.getReflectionId());
      StartRoom.npcList.add(thisnpc);
      thisnpc.npc.setIsNoRndWalk(true);
      world.rooms.put("StartRoom", StartRoom);
   }

   protected void spawnHall(DarkCloudMansion.DMCWorld world) {
      DarkCloudMansion.DMCRoom Hall = new DarkCloudMansion.DMCRoom();
      world.rooms.remove("Hall");
      DarkCloudMansion.DMCNpc thisnpc = new DarkCloudMansion.DMCNpc();
      thisnpc.npc = addSpawn(22273, 147217, 180112, -6117, 0, false, 0L, false, world.getReflectionId());
      thisnpc.npc.setIsNoRndWalk(true);
      Hall.npcList.add(thisnpc);
      thisnpc = new DarkCloudMansion.DMCNpc();
      thisnpc.npc = addSpawn(22274, 147217, 180209, -6117, 0, false, 0L, false, world.getReflectionId());
      thisnpc.npc.setIsNoRndWalk(true);
      Hall.npcList.add(thisnpc);
      thisnpc = new DarkCloudMansion.DMCNpc();
      thisnpc.npc = addSpawn(22273, 148521, 180112, -6117, 0, false, 0L, false, world.getReflectionId());
      thisnpc.npc.setIsNoRndWalk(true);
      Hall.npcList.add(thisnpc);
      thisnpc = new DarkCloudMansion.DMCNpc();
      thisnpc.npc = addSpawn(22272, 148521, 180209, -6117, 0, false, 0L, false, world.getReflectionId());
      thisnpc.npc.setIsNoRndWalk(true);
      Hall.npcList.add(thisnpc);
      thisnpc = new DarkCloudMansion.DMCNpc();
      thisnpc.npc = addSpawn(22273, 148525, 180910, -6117, 0, false, 0L, false, world.getReflectionId());
      thisnpc.npc.setIsNoRndWalk(true);
      Hall.npcList.add(thisnpc);
      thisnpc = new DarkCloudMansion.DMCNpc();
      thisnpc.npc = addSpawn(22274, 148435, 180910, -6117, 0, false, 0L, false, world.getReflectionId());
      thisnpc.npc.setIsNoRndWalk(true);
      Hall.npcList.add(thisnpc);
      thisnpc = new DarkCloudMansion.DMCNpc();
      thisnpc.npc = addSpawn(22273, 147242, 180910, -6117, 0, false, 0L, false, world.getReflectionId());
      thisnpc.npc.setIsNoRndWalk(true);
      Hall.npcList.add(thisnpc);
      thisnpc = new DarkCloudMansion.DMCNpc();
      thisnpc.npc = addSpawn(22274, 147242, 180819, -6117, 0, false, 0L, false, world.getReflectionId());
      thisnpc.npc.setIsNoRndWalk(true);
      Hall.npcList.add(thisnpc);
      world.rooms.put("Hall", Hall);
   }

   protected void runHall(DarkCloudMansion.DMCWorld world) {
      this.spawnHall(world);
      world.setStatus(1);
      world.getReflection().openDoor(24230001);
   }

   protected void runFirstRoom(DarkCloudMansion.DMCWorld world) {
      DarkCloudMansion.DMCRoom FirstRoom = new DarkCloudMansion.DMCRoom();
      DarkCloudMansion.DMCNpc thisnpc = new DarkCloudMansion.DMCNpc();
      thisnpc.npc = addSpawn(22264, 147842, 179837, -6117, 0, false, 0L, false, world.getReflectionId());
      thisnpc.npc.setIsNoRndWalk(true);
      FirstRoom.npcList.add(thisnpc);
      thisnpc = new DarkCloudMansion.DMCNpc();
      thisnpc.npc = addSpawn(22264, 147711, 179708, -6117, 0, false, 0L, false, world.getReflectionId());
      thisnpc.npc.setIsNoRndWalk(true);
      FirstRoom.npcList.add(thisnpc);
      thisnpc = new DarkCloudMansion.DMCNpc();
      thisnpc.npc = addSpawn(22264, 147842, 179552, -6117, 0, false, 0L, false, world.getReflectionId());
      thisnpc.npc.setIsNoRndWalk(true);
      FirstRoom.npcList.add(thisnpc);
      thisnpc = new DarkCloudMansion.DMCNpc();
      thisnpc.npc = addSpawn(22264, 147964, 179708, -6117, 0, false, 0L, false, world.getReflectionId());
      thisnpc.npc.setIsNoRndWalk(true);
      FirstRoom.npcList.add(thisnpc);
      world.rooms.put("FirstRoom", FirstRoom);
      world.setStatus(2);
      world.getReflection().openDoor(24230002);
   }

   protected void runHall2(DarkCloudMansion.DMCWorld world) {
      addSpawn(32288, 147818, 179643, -6117, 0, false, 0L, false, world.getReflectionId());
      this.spawnHall(world);
      world.setStatus(3);
   }

   protected void runSecondRoom(DarkCloudMansion.DMCWorld world) {
      DarkCloudMansion.DMCRoom SecondRoom = new DarkCloudMansion.DMCRoom();
      SecondRoom.Order = new int[7];
      SecondRoom.Order[0] = 1;

      for(int i = 1; i < 7; ++i) {
         SecondRoom.Order[i] = 0;
      }

      int i = getRandom(MonolithOrder.length);
      DarkCloudMansion.DMCNpc thisnpc = new DarkCloudMansion.DMCNpc();
      thisnpc.npc = addSpawn(32324, 147800, 181150, -6117, 0, false, 0L, false, world.getReflectionId());
      thisnpc.order = MonolithOrder[i][0];
      SecondRoom.npcList.add(thisnpc);
      thisnpc = new DarkCloudMansion.DMCNpc();
      thisnpc.npc = addSpawn(32324, 147900, 181215, -6117, 0, false, 0L, false, world.getReflectionId());
      thisnpc.order = MonolithOrder[i][1];
      SecondRoom.npcList.add(thisnpc);
      thisnpc = new DarkCloudMansion.DMCNpc();
      thisnpc.npc = addSpawn(32324, 147900, 181345, -6117, 0, false, 0L, false, world.getReflectionId());
      thisnpc.order = MonolithOrder[i][2];
      SecondRoom.npcList.add(thisnpc);
      thisnpc = new DarkCloudMansion.DMCNpc();
      thisnpc.npc = addSpawn(32324, 147800, 181410, -6117, 0, false, 0L, false, world.getReflectionId());
      thisnpc.order = MonolithOrder[i][3];
      SecondRoom.npcList.add(thisnpc);
      thisnpc = new DarkCloudMansion.DMCNpc();
      thisnpc.npc = addSpawn(32324, 147700, 181345, -6117, 0, false, 0L, false, world.getReflectionId());
      thisnpc.order = MonolithOrder[i][4];
      SecondRoom.npcList.add(thisnpc);
      thisnpc = new DarkCloudMansion.DMCNpc();
      thisnpc.npc = addSpawn(32324, 147700, 181215, -6117, 0, false, 0L, false, world.getReflectionId());
      thisnpc.order = MonolithOrder[i][5];
      SecondRoom.npcList.add(thisnpc);
      world.rooms.put("SecondRoom", SecondRoom);
      world.setStatus(4);
      world.getReflection().openDoor(24230005);
   }

   protected void runHall3(DarkCloudMansion.DMCWorld world) {
      addSpawn(32289, 147808, 181281, -6117, 16383, false, 0L, false, world.getReflectionId());
      this.spawnHall(world);
      world.setStatus(5);
   }

   protected void runThirdRoom(DarkCloudMansion.DMCWorld world) {
      DarkCloudMansion.DMCRoom ThirdRoom = new DarkCloudMansion.DMCRoom();
      DarkCloudMansion.DMCNpc thisnpc = new DarkCloudMansion.DMCNpc();
      thisnpc.isDead = false;
      thisnpc.npc = addSpawn(22273, 148765, 180450, -6117, 0, false, 0L, false, world.getReflectionId());
      thisnpc.npc.setIsNoRndWalk(true);
      ThirdRoom.npcList.add(thisnpc);
      thisnpc.npc = addSpawn(22274, 148865, 180190, -6117, 0, false, 0L, false, world.getReflectionId());
      thisnpc.npc.setIsNoRndWalk(true);
      ThirdRoom.npcList.add(thisnpc);
      thisnpc.npc = addSpawn(22273, 148995, 180190, -6117, 0, false, 0L, false, world.getReflectionId());
      thisnpc.npc.setIsNoRndWalk(true);
      ThirdRoom.npcList.add(thisnpc);
      thisnpc.npc = addSpawn(22272, 149090, 180450, -6117, 0, false, 0L, false, world.getReflectionId());
      thisnpc.npc.setIsNoRndWalk(true);
      ThirdRoom.npcList.add(thisnpc);
      thisnpc.npc = addSpawn(22273, 148995, 180705, -6117, 0, false, 0L, false, world.getReflectionId());
      thisnpc.npc.setIsNoRndWalk(true);
      ThirdRoom.npcList.add(thisnpc);
      thisnpc.npc = addSpawn(22274, 148865, 180705, -6117, 0, false, 0L, false, world.getReflectionId());
      thisnpc.npc.setIsNoRndWalk(true);
      ThirdRoom.npcList.add(thisnpc);
      world.rooms.put("ThirdRoom", ThirdRoom);
      world.setStatus(6);
      world.getReflection().openDoor(24230003);
   }

   protected void runThirdRoom2(DarkCloudMansion.DMCWorld world) {
      addSpawn(32290, 148910, 178397, -6117, 16383, false, 0L, false, world.getReflectionId());
      DarkCloudMansion.DMCRoom ThirdRoom = new DarkCloudMansion.DMCRoom();
      DarkCloudMansion.DMCNpc thisnpc = new DarkCloudMansion.DMCNpc();
      thisnpc.isDead = false;
      thisnpc.npc = addSpawn(22273, 148765, 180450, -6117, 0, false, 0L, false, world.getReflectionId());
      thisnpc.npc.setIsNoRndWalk(true);
      ThirdRoom.npcList.add(thisnpc);
      thisnpc.npc = addSpawn(22274, 148865, 180190, -6117, 0, false, 0L, false, world.getReflectionId());
      thisnpc.npc.setIsNoRndWalk(true);
      ThirdRoom.npcList.add(thisnpc);
      thisnpc.npc = addSpawn(22273, 148995, 180190, -6117, 0, false, 0L, false, world.getReflectionId());
      thisnpc.npc.setIsNoRndWalk(true);
      ThirdRoom.npcList.add(thisnpc);
      thisnpc.npc = addSpawn(22272, 149090, 180450, -6117, 0, false, 0L, false, world.getReflectionId());
      thisnpc.npc.setIsNoRndWalk(true);
      ThirdRoom.npcList.add(thisnpc);
      thisnpc.npc = addSpawn(22273, 148995, 180705, -6117, 0, false, 0L, false, world.getReflectionId());
      thisnpc.npc.setIsNoRndWalk(true);
      ThirdRoom.npcList.add(thisnpc);
      thisnpc.npc = addSpawn(22274, 148865, 180705, -6117, 0, false, 0L, false, world.getReflectionId());
      thisnpc.npc.setIsNoRndWalk(true);
      ThirdRoom.npcList.add(thisnpc);
      world.rooms.put("ThirdRoom2", ThirdRoom);
      world.setStatus(8);
   }

   protected void runForthRoom(DarkCloudMansion.DMCWorld world) {
      DarkCloudMansion.DMCRoom ForthRoom = new DarkCloudMansion.DMCRoom();
      ForthRoom.counter = 0;
      int[] temp = new int[7];
      int[][] templist = new int[7][5];
      int xx = 0;

      for(int i = 0; i < 7; ++i) {
         temp[i] = getRandom(ColumnRows.length);
      }

      for(int i = 0; i < 7; ++i) {
         templist[i] = ColumnRows[temp[i]];
      }

      for(int x = 148660; x < 149285; x += 125) {
         int yy = 0;

         for(int y = 179280; y > 178405; y -= 125) {
            DarkCloudMansion.DMCNpc thisnpc = new DarkCloudMansion.DMCNpc();
            thisnpc.npc = addSpawn(22402, x, y, -6115, 16215, false, 0L, false, world.getReflectionId());
            thisnpc.status = templist[yy][xx];
            thisnpc.order = yy;
            ForthRoom.npcList.add(thisnpc);
            ++yy;
         }

         ++xx;
      }

      for(DarkCloudMansion.DMCNpc npc : ForthRoom.npcList) {
         if (npc.status == 0) {
            npc.npc.setIsInvul(true);
         }
      }

      world.rooms.put("ForthRoom", ForthRoom);
      world.setStatus(7);
      world.getReflection().openDoor(24230004);
   }

   protected void runFifthRoom(DarkCloudMansion.DMCWorld world) {
      this.spawnFifthRoom(world);
      world.setStatus(9);
      world.getReflection().openDoor(24230006);
   }

   private void spawnFifthRoom(DarkCloudMansion.DMCWorld world) {
      int idx = 0;
      int[] temp = new int[6];
      DarkCloudMansion.DMCRoom FifthRoom = new DarkCloudMansion.DMCRoom();
      temp = Beleths[getRandom(Beleths.length)];
      FifthRoom.reset = 0;
      FifthRoom.founded = 0;

      for(int x = 148720; x < 149175; x += 65) {
         DarkCloudMansion.DMCNpc thisnpc = new DarkCloudMansion.DMCNpc();
         thisnpc.npc = addSpawn(BS[idx], x, 182145, -6117, 48810, false, 0L, false, world.getReflectionId());
         thisnpc.npc.setIsNoRndWalk(true);
         thisnpc.order = idx;
         thisnpc.status = temp[idx];
         thisnpc.count = 0;
         FifthRoom.npcList.add(thisnpc);
         if (temp[idx] == 1 && getRandom(100) < 95) {
            thisnpc.npc.broadcastPacket(new NpcSay(thisnpc.npc.getObjectId(), 0, thisnpc.npc.getId(), _spawnChat[getRandom(_spawnChat.length)]), 2000);
         } else if (temp[idx] != 1 && getRandom(100) < 67) {
            thisnpc.npc.broadcastPacket(new NpcSay(thisnpc.npc.getObjectId(), 0, thisnpc.npc.getId(), _spawnChat[getRandom(_spawnChat.length)]), 2000);
         }

         ++idx;
      }

      world.rooms.put("FifthRoom", FifthRoom);
   }

   protected boolean checkKillProgress(Npc npc, DarkCloudMansion.DMCRoom room) {
      boolean cont = true;

      for(DarkCloudMansion.DMCNpc npcobj : room.npcList) {
         if (npcobj.npc == npc) {
            npcobj.isDead = true;
         }

         if (!npcobj.isDead) {
            cont = false;
         }
      }

      return cont;
   }

   protected void spawnRndGolem(DarkCloudMansion.DMCWorld world, DarkCloudMansion.DMCNpc npc) {
      if (npc.golem == null) {
         int i = getRandom(GolemSpawn.length);
         int mobId = GolemSpawn[i][0];
         int x = GolemSpawn[i][1];
         int y = GolemSpawn[i][2];
         npc.golem = addSpawn(mobId, x, y, -6117, 0, false, 0L, false, world.getReflectionId());
         npc.golem.setIsNoRndWalk(true);
      }
   }

   protected void checkStone(Npc npc, int[] order, DarkCloudMansion.DMCNpc npcObj, DarkCloudMansion.DMCWorld world) {
      for(int i = 1; i < 7; ++i) {
         if (order[i] == 0 && order[i - 1] != 0 && npcObj.order == i && npcObj.status == 0) {
            order[i] = 1;
            npcObj.status = 1;
            npcObj.isDead = true;
            npc.broadcastPacket(new MagicSkillUse(npc, npc, 5441, 1, 1, 0));
            return;
         }
      }

      this.spawnRndGolem(world, npcObj);
   }

   protected void endInstance(DarkCloudMansion.DMCWorld world) {
      world.setStatus(10);
      addSpawn(32291, 148911, 181940, -6117, 16383, false, 0L, false, world.getReflectionId());
      world.rooms.clear();
   }

   protected void checkBelethSample(DarkCloudMansion.DMCWorld world, Npc npc, Player player) {
      DarkCloudMansion.DMCRoom FifthRoom = world.rooms.get("FifthRoom");

      for(DarkCloudMansion.DMCNpc mob : FifthRoom.npcList) {
         if (mob.npc == npc) {
            if (mob.count != 0) {
               return;
            }

            mob.count = 1;
            if (mob.status == 1) {
               mob.npc.broadcastPacket(new NpcSay(mob.npc.getObjectId(), 22, mob.npc.getId(), _successChat[getRandom(_successChat.length)]), 2000);
               ++FifthRoom.founded;
               this.startQuestTimer("decayMe", 1500L, npc, player);
            } else {
               FifthRoom.reset = 1;
               mob.npc.broadcastPacket(new NpcSay(mob.npc.getObjectId(), 22, mob.npc.getId(), _faildChat[getRandom(_faildChat.length)]), 2000);
               this.startQuestTimer("decayChatBelethSamples", 4000L, npc, player);
               this.startQuestTimer("decayBelethSamples", 4500L, npc, player);
            }
         }
      }
   }

   protected void killedBelethSample(DarkCloudMansion.DMCWorld world, Npc npc) {
      int decayedSamples = 0;
      DarkCloudMansion.DMCRoom FifthRoom = world.rooms.get("FifthRoom");

      for(DarkCloudMansion.DMCNpc mob : FifthRoom.npcList) {
         if (mob.npc == npc) {
            ++decayedSamples;
            mob.count = 2;
         } else if (mob.count == 2) {
            ++decayedSamples;
         }
      }

      if (FifthRoom.reset == 1) {
         for(DarkCloudMansion.DMCNpc mob : FifthRoom.npcList) {
            if (mob.count == 0 || mob.status == 1 && mob.count != 2) {
               ++decayedSamples;
               mob.npc.decayMe();
               mob.count = 2;
            }
         }

         if (decayedSamples == 7) {
            this.startQuestTimer("respawnFifth", 6000L, npc, null);
         }
      } else if (FifthRoom.reset == 0 && FifthRoom.founded == 3) {
         for(DarkCloudMansion.DMCNpc mob : FifthRoom.npcList) {
            mob.npc.decayMe();
         }

         this.endInstance(world);
      }
   }

   protected boolean allStonesDone(DarkCloudMansion.DMCWorld world) {
      DarkCloudMansion.DMCRoom SecondRoom = world.rooms.get("SecondRoom");

      for(DarkCloudMansion.DMCNpc mob : SecondRoom.npcList) {
         if (!mob.isDead) {
            return false;
         }
      }

      return true;
   }

   protected void removeMonoliths(DarkCloudMansion.DMCWorld world) {
      DarkCloudMansion.DMCRoom SecondRoom = world.rooms.get("SecondRoom");

      for(DarkCloudMansion.DMCNpc mob : SecondRoom.npcList) {
         mob.npc.decayMe();
      }
   }

   protected void chkShadowColumn(DarkCloudMansion.DMCWorld world, Npc npc) {
      DarkCloudMansion.DMCRoom ForthRoom = world.rooms.get("ForthRoom");

      for(DarkCloudMansion.DMCNpc mob : ForthRoom.npcList) {
         if (mob.npc == npc) {
            for(int i = 0; i < 7; ++i) {
               if (mob.order == i && ForthRoom.counter == i) {
                  world.getReflection().openDoor(24230007 + i);
                  ++ForthRoom.counter;
                  if (ForthRoom.counter == 7) {
                     this.runThirdRoom2(world);
                  }
               }
            }
         }
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      if (npc == null) {
         return "";
      } else {
         ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
         if (!(tmpworld instanceof DarkCloudMansion.DMCWorld)) {
            return "";
         } else {
            DarkCloudMansion.DMCWorld world = (DarkCloudMansion.DMCWorld)tmpworld;
            if (world.rooms.containsKey("FifthRoom")) {
               DarkCloudMansion.DMCRoom FifthRoom = world.rooms.get("FifthRoom");
               if (event.equalsIgnoreCase("decayMe")) {
                  for(DarkCloudMansion.DMCNpc mob : FifthRoom.npcList) {
                     if (mob.npc == npc || FifthRoom.reset == 0 && FifthRoom.founded == 3) {
                        mob.npc.decayMe();
                        mob.count = 2;
                     }
                  }

                  if (FifthRoom.reset == 0 && FifthRoom.founded == 3) {
                     this.endInstance(world);
                  }
               } else if (event.equalsIgnoreCase("decayBelethSamples")) {
                  for(DarkCloudMansion.DMCNpc mob : FifthRoom.npcList) {
                     if (mob.count == 0) {
                        mob.npc.decayMe();
                        mob.count = 2;
                     }
                  }
               } else if (event.equalsIgnoreCase("decayChatBelethSamples")) {
                  for(DarkCloudMansion.DMCNpc mob : FifthRoom.npcList) {
                     if (mob.status == 1) {
                        mob.npc.broadcastPacket(new NpcSay(mob.npc.getObjectId(), 22, mob.npc.getId(), _decayChat[getRandom(_decayChat.length)]), 2000);
                     }
                  }
               } else if (event.equalsIgnoreCase("respawnFifth")) {
                  this.spawnFifthRoom(world);
               }
            }

            return "";
         }
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
      if (tmpworld instanceof DarkCloudMansion.DMCWorld) {
         DarkCloudMansion.DMCWorld world = (DarkCloudMansion.DMCWorld)tmpworld;
         if (world.isStatus(0) && this.checkKillProgress(npc, world.rooms.get("StartRoom"))) {
            this.runHall(world);
         }

         if (world.isStatus(1) && this.checkKillProgress(npc, world.rooms.get("Hall"))) {
            this.runFirstRoom(world);
         }

         if (world.isStatus(2) && this.checkKillProgress(npc, world.rooms.get("FirstRoom"))) {
            this.runHall2(world);
         }

         if (world.isStatus(3) && this.checkKillProgress(npc, world.rooms.get("Hall"))) {
            this.runSecondRoom(world);
         }

         if (world.isStatus(4)) {
            DarkCloudMansion.DMCRoom SecondRoom = world.rooms.get("SecondRoom");

            for(DarkCloudMansion.DMCNpc mob : SecondRoom.npcList) {
               if (mob.golem == npc) {
                  mob.golem = null;
               }
            }
         }

         if (world.isStatus(5) && this.checkKillProgress(npc, world.rooms.get("Hall"))) {
            this.runThirdRoom(world);
         }

         if (world.isStatus(6) && this.checkKillProgress(npc, world.rooms.get("ThirdRoom"))) {
            this.runForthRoom(world);
         }

         if (world.isStatus(7)) {
            this.chkShadowColumn(world, npc);
         }

         if (world.isStatus(8) && this.checkKillProgress(npc, world.rooms.get("ThirdRoom2"))) {
            this.runFifthRoom(world);
         }

         if (world.isStatus(9)) {
            this.killedBelethSample(world, npc);
         }
      }

      return "";
   }

   @Override
   public String onAttack(Npc npc, Player player, int damage, boolean isSummon, Skill skill) {
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
      if (tmpworld instanceof DarkCloudMansion.DMCWorld) {
         DarkCloudMansion.DMCWorld world = (DarkCloudMansion.DMCWorld)tmpworld;
         if (world.isStatus(7)) {
            DarkCloudMansion.DMCRoom ForthRoom = world.rooms.get("ForthRoom");

            for(DarkCloudMansion.DMCNpc mob : ForthRoom.npcList) {
               if (mob.npc == npc && mob.npc.isInvul() && getRandom(100) < 12) {
                  addSpawn(BM[getRandom(BM.length)], player.getX(), player.getY(), player.getZ(), 0, false, 0L, false, world.getReflectionId());
               }
            }
         }

         if (world.isStatus(9)) {
            this.checkBelethSample(world, npc, player);
         }
      }

      return "";
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
      if (tmpworld instanceof DarkCloudMansion.DMCWorld) {
         DarkCloudMansion.DMCWorld world = (DarkCloudMansion.DMCWorld)tmpworld;
         if (world.isStatus(4)) {
            DarkCloudMansion.DMCRoom SecondRoom = world.rooms.get("SecondRoom");

            for(DarkCloudMansion.DMCNpc mob : SecondRoom.npcList) {
               if (mob.npc == npc) {
                  this.checkStone(npc, SecondRoom.Order, mob, world);
               }
            }

            if (this.allStonesDone(world)) {
               this.removeMonoliths(world);
               this.runHall3(world);
            }
         }

         if (npc.getId() == 32291 && world.isStatus(10)) {
            npc.showChatWindow(player);
            QuestState st = player.getQuestState(this.getName());
            if (st == null) {
               st = this.newQuestState(player);
            }

            if (!st.hasQuestItems(9690)) {
               st.giveItems(9690, 1L);
            }
         }
      }

      return "";
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      int npcId = npc.getId();
      if (npcId == 32282) {
         this.enterInstance(player, npc);
      } else {
         ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
         if (!(tmpworld instanceof DarkCloudMansion.DMCWorld)) {
            return "";
         }

         DarkCloudMansion.DMCWorld world = (DarkCloudMansion.DMCWorld)tmpworld;
         if (npcId == 32291) {
            if (world.isAllowed(player.getObjectId())) {
               world.removeAllowed(player.getObjectId());
            }

            this.teleportPlayer(player, new Location(139968, 150367, -3111), 0);
            int instanceId = npc.getReflectionId();
            Reflection instance = ReflectionManager.getInstance().getReflection(instanceId);
            if (instance.getPlayers().isEmpty()) {
               ReflectionManager.getInstance().destroyReflection(instanceId);
            }

            return "";
         }
      }

      return "";
   }

   public static void main(String[] args) {
      new DarkCloudMansion(DarkCloudMansion.class.getSimpleName(), "instances");
   }

   protected static class DMCNpc {
      public Npc npc;
      public boolean isDead = false;
      public Npc golem = null;
      public int status = 0;
      public int order = 0;
      public int count = 0;
   }

   protected static class DMCRoom {
      public List<DarkCloudMansion.DMCNpc> npcList = new ArrayList<>();
      public int counter = 0;
      public int reset = 0;
      public int founded = 0;
      public int[] Order;
   }

   protected class DMCWorld extends ReflectionWorld {
      public Map<String, DarkCloudMansion.DMCRoom> rooms = new ConcurrentHashMap<>();
   }
}
