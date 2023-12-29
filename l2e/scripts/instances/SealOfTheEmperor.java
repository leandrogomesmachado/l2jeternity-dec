package l2e.scripts.instances;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.reflection.ReflectionTemplate;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.model.entity.Reflection;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.scripts.quests._196_SevenSignSealOfTheEmperor;

public class SealOfTheEmperor extends AbstractReflection {
   private static final NpcStringId[] ANAKIM_TEXT = new NpcStringId[]{
      NpcStringId.FOR_THE_ETERNITY_OF_EINHASAD,
      NpcStringId.DEAR_SHILLIENS_OFFSPRINGS_YOU_ARE_NOT_CAPABLE_OF_CONFRONTING_US,
      NpcStringId.ILL_SHOW_YOU_THE_REAL_POWER_OF_EINHASAD,
      NpcStringId.DEAR_MILITARY_FORCE_OF_LIGHT_GO_DESTROY_THE_OFFSPRINGS_OF_SHILLIEN
   };
   private static final NpcStringId[] LILITH_TEXT = new NpcStringId[]{
      NpcStringId.YOU_SUCH_A_FOOL_THE_VICTORY_OVER_THIS_WAR_BELONGS_TO_SHILIEN,
      NpcStringId.HOW_DARE_YOU_TRY_TO_CONTEND_AGAINST_ME_IN_STRENGTH_RIDICULOUS,
      NpcStringId.ANAKIM_IN_THE_NAME_OF_GREAT_SHILIEN_I_WILL_CUT_YOUR_THROAT,
      NpcStringId.YOU_CANNOT_BE_THE_MATCH_OF_LILITH_ILL_TEACH_YOU_A_LESSON
   };

   public SealOfTheEmperor() {
      super(SealOfTheEmperor.class.getSimpleName(), "instances");
      this.addStartNpc(new int[]{32585, 32657});
      this.addTalkId(new int[]{32585, 32657});
      this.addSkillSeeId(new int[]{27384});
      this.addSpawnId(new int[]{32718, 32715, 27384});
      this.addAggroRangeEnterId(new int[]{27371, 27372, 27373, 27377, 27378, 27379});
      this.addAttackId(new int[]{27384, 32715, 32716, 32717, 32718, 32719, 32720, 32721});
      this.addKillId(new int[]{27371, 27372, 27373, 27374, 27375, 27377, 27378, 27379, 27384});
   }

   @Override
   public String onAggroRangeEnter(Npc npc, Player player, boolean isSummon) {
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(player.getReflectionId());
      if (tmpworld != null && tmpworld instanceof SealOfTheEmperor.SIGNSWorld) {
         ((Attackable)npc).abortAttack();
         npc.setTarget(player);
         npc.setIsRunning(true);
         npc.getAI().setIntention(CtrlIntention.ATTACK, player);
      }

      return super.onAggroRangeEnter(npc, player, isSummon);
   }

   protected void runStartRoom(SealOfTheEmperor.SIGNSWorld world) {
      world.setStatus(0);
      addSpawn(32586, -89456, 216184, -7504, 40960, false, 0L, false, world.getReflectionId());
      addSpawn(32587, -89400, 216125, -7504, 40960, false, 0L, false, world.getReflectionId());
      addSpawn(32657, -84385, 216117, -7497, 0, false, 0L, false, world.getReflectionId());
      addSpawn(32598, -84945, 220643, -7495, 0, false, 0L, false, world.getReflectionId());
      addSpawn(32598, -89563, 220647, -7491, 0, false, 0L, false, world.getReflectionId());
   }

   protected void runFirstRoom(SealOfTheEmperor.SIGNSWorld world) {
      world._npcList.add(addSpawn(27371, -89049, 217979, -7495, 0, false, 0L, false, world.getReflectionId()));
      addSpawn(27372, -89049, 217979, -7495, 0, false, 0L, false, world.getReflectionId());
      addSpawn(27373, -89049, 217979, -7495, 0, false, 0L, false, world.getReflectionId());
      addSpawn(27374, -89049, 217979, -7495, 0, false, 0L, false, world.getReflectionId());
      world.setStatus(1);
   }

   protected void runSecondRoom(SealOfTheEmperor.SIGNSWorld world) {
      world._npcList.clear();
      world._npcList.add(addSpawn(27371, -88599, 220071, -7495, 0, false, 0L, false, world.getReflectionId()));
      addSpawn(27371, -88599, 220071, -7495, 0, false, 0L, false, world.getReflectionId());
      addSpawn(27372, -88599, 220071, -7495, 0, false, 0L, false, world.getReflectionId());
      addSpawn(27373, -88599, 220071, -7495, 0, false, 0L, false, world.getReflectionId());
      addSpawn(27373, -88599, 220071, -7495, 0, false, 0L, false, world.getReflectionId());
      addSpawn(27374, -88599, 220071, -7495, 0, false, 0L, false, world.getReflectionId());
      world.setStatus(2);
   }

   protected void runThirdRoom(SealOfTheEmperor.SIGNSWorld world) {
      world._npcList.clear();
      world._npcList.add(addSpawn(27371, -86846, 220639, -7495, 0, false, 0L, false, world.getReflectionId()));
      addSpawn(27371, -86846, 220639, -7495, 0, false, 0L, false, world.getReflectionId());
      addSpawn(27372, -86846, 220639, -7495, 0, false, 0L, false, world.getReflectionId());
      addSpawn(27372, -86846, 220639, -7495, 0, false, 0L, false, world.getReflectionId());
      addSpawn(27373, -86846, 220639, -7495, 0, false, 0L, false, world.getReflectionId());
      addSpawn(27373, -86846, 220639, -7495, 0, false, 0L, false, world.getReflectionId());
      addSpawn(27374, -86846, 220639, -7495, 0, false, 0L, false, world.getReflectionId());
      addSpawn(27374, -86846, 220639, -7495, 0, false, 0L, false, world.getReflectionId());
      world.setStatus(3);
   }

   protected void runForthRoom(SealOfTheEmperor.SIGNSWorld world) {
      world._npcList.clear();
      world._npcList.add(addSpawn(27371, -85463, 219227, -7495, 0, false, 0L, false, world.getReflectionId()));
      addSpawn(27372, -85463, 219227, -7495, 0, false, 0L, false, world.getReflectionId());
      addSpawn(27373, -85463, 219227, -7495, 0, false, 0L, false, world.getReflectionId());
      addSpawn(27374, -85463, 219227, -7495, 0, false, 0L, false, world.getReflectionId());
      addSpawn(27375, -85463, 219227, -7495, 0, false, 0L, false, world.getReflectionId());
      addSpawn(27377, -85463, 219227, -7495, 0, false, 0L, false, world.getReflectionId());
      addSpawn(27378, -85463, 219227, -7495, 0, false, 0L, false, world.getReflectionId());
      addSpawn(27379, -85463, 219227, -7495, 0, false, 0L, false, world.getReflectionId());
      world.setStatus(4);
   }

   protected void runFifthRoom(SealOfTheEmperor.SIGNSWorld world) {
      world._npcList.clear();
      world._npcList.add(addSpawn(27371, -87441, 217623, -7495, 0, false, 0L, false, world.getReflectionId()));
      addSpawn(27372, -87441, 217623, -7495, 0, false, 0L, false, world.getReflectionId());
      addSpawn(27373, -87441, 217623, -7495, 0, false, 0L, false, world.getReflectionId());
      addSpawn(27374, -87441, 217623, -7495, 0, false, 0L, false, world.getReflectionId());
      addSpawn(27375, -87441, 217623, -7495, 0, false, 0L, false, world.getReflectionId());
      addSpawn(27375, -87441, 217623, -7495, 0, false, 0L, false, world.getReflectionId());
      addSpawn(27377, -87441, 217623, -7495, 0, false, 0L, false, world.getReflectionId());
      addSpawn(27377, -87441, 217623, -7495, 0, false, 0L, false, world.getReflectionId());
      addSpawn(27378, -87441, 217623, -7495, 0, false, 0L, false, world.getReflectionId());
      addSpawn(27378, -87441, 217623, -7495, 0, false, 0L, false, world.getReflectionId());
      addSpawn(27379, -87441, 217623, -7495, 0, false, 0L, false, world.getReflectionId());
      addSpawn(27379, -87441, 217623, -7495, 0, false, 0L, false, world.getReflectionId());
      world.setStatus(5);
   }

   protected void runBossRoom(SealOfTheEmperor.SIGNSWorld world) {
      world._lilith = (Attackable)addSpawn(32715, -83175, 217021, -7504, 49151, false, 0L, false, world.getReflectionId());
      world._lilith_guard0 = (Attackable)addSpawn(32716, -83222, 217055, -7504, 49151, false, 0L, false, world.getReflectionId());
      world._lilith_guard1 = (Attackable)addSpawn(32717, -83127, 217056, -7504, 49151, false, 0L, false, world.getReflectionId());
      world._anakim = (Attackable)addSpawn(32718, -83179, 216479, -7504, 16384, false, 0L, false, world.getReflectionId());
      world._anakim_guard0 = (Attackable)addSpawn(32719, -83227, 216443, -7504, 16384, false, 0L, false, world.getReflectionId());
      world._anakim_guard1 = (Attackable)addSpawn(32720, -83179, 216432, -7504, 16384, false, 0L, false, world.getReflectionId());
      world._anakim_guard2 = (Attackable)addSpawn(32721, -83134, 216443, -7504, 16384, false, 0L, false, world.getReflectionId());
      world._lilith_guard0.setIsImmobilized(true);
      world._lilith_guard1.setIsImmobilized(true);
      world._anakim_guard0.setIsImmobilized(true);
      world._anakim_guard1.setIsImmobilized(true);
      world._anakim_guard2.setIsImmobilized(true);
      addSpawn(27384, -83177, 217353, -7520, 32768, false, 0L, false, world.getReflectionId());
      addSpawn(27384, -83177, 216137, -7520, 32768, false, 0L, false, world.getReflectionId());
      addSpawn(27384, -82588, 216754, -7520, 32768, false, 0L, false, world.getReflectionId());
      addSpawn(27384, -83804, 216754, -7520, 32768, false, 0L, false, world.getReflectionId());
      addSpawn(32592, -83176, 216753, -7497, 0, false, 0L, false, world.getReflectionId());
      world.setStatus(6);
   }

   protected void runSDRoom(SealOfTheEmperor.SIGNSWorld world) {
      Npc npc1 = addSpawn(27384, -83177, 217353, -7520, 32768, false, 0L, false, world.getReflectionId());
      npc1.setIsNoRndWalk(true);
      npc1.setRHandId(15281);
      Npc npc2 = addSpawn(27384, -83177, 216137, -7520, 32768, false, 0L, false, world.getReflectionId());
      npc2.setIsNoRndWalk(true);
      npc2.setRHandId(15281);
      Npc npc3 = addSpawn(27384, -82588, 216754, -7520, 32768, false, 0L, false, world.getReflectionId());
      npc3.setIsNoRndWalk(true);
      npc3.setRHandId(15281);
      Npc npc4 = addSpawn(27384, -83804, 216754, -7520, 32768, false, 0L, false, world.getReflectionId());
      npc4.setIsNoRndWalk(true);
      npc4.setRHandId(15281);
   }

   protected boolean checkKillProgress(SealOfTheEmperor.SIGNSWorld world, Npc npc) {
      return world._npcList.contains(npc);
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

   public final synchronized void enterInstance(Player player, Npc npc) {
      if (this.enterInstance(player, npc, new SealOfTheEmperor.SIGNSWorld(), 112)) {
         ReflectionWorld world = ReflectionManager.getInstance().getPlayerWorld(player);
         this.runStartRoom((SealOfTheEmperor.SIGNSWorld)world);
         this.runFirstRoom((SealOfTheEmperor.SIGNSWorld)world);
      }
   }

   protected void exitInstance(Player player) {
      player.setReflectionId(0);
      player.teleToLocation(171782, -17612, -4901, true);
      ReflectionWorld world = ReflectionManager.getInstance().getPlayerWorld(player);
      if (world != null) {
         Reflection inst = world.getReflection();
         inst.setDuration(300000);
         inst.setEmptyDestroyTime(0L);
      }
   }

   @Override
   public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon, Skill skill) {
      int npcId = npc.getId();
      if (npcId == 32715 || npcId == 32716 || npcId == 32717) {
         npc.setCurrentHp(npc.getCurrentHp() + (double)damage);
         ((Attackable)npc).stopHating(attacker);
      }

      ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
      if (tmpworld instanceof SealOfTheEmperor.SIGNSWorld) {
         SealOfTheEmperor.SIGNSWorld world = (SealOfTheEmperor.SIGNSWorld)tmpworld;
         if (world.isStatus(6) && npc.getId() == 27384) {
            npc.doCast(SkillsParser.getInstance().getInfo(5980, 3));
         }
      }

      return super.onAttack(npc, attacker, damage, isSummon, skill);
   }

   @Override
   public String onSkillSee(Npc npc, Player caster, Skill skill, GameObject[] targets, boolean isSummon) {
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
      if (tmpworld != null && tmpworld instanceof SealOfTheEmperor.SIGNSWorld) {
         SealOfTheEmperor.SIGNSWorld world = (SealOfTheEmperor.SIGNSWorld)tmpworld;
         if (skill.getId() == 8357 && world.isStatus(6) && npc.getId() == 27384) {
            npc.doCast(SkillsParser.getInstance().getInfo(5980, 3));
         }
      }

      return super.onSkillSee(npc, caster, skill, targets, isSummon);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(player.getReflectionId());
      if (tmpworld != null && tmpworld instanceof SealOfTheEmperor.SIGNSWorld) {
         SealOfTheEmperor.SIGNSWorld world = (SealOfTheEmperor.SIGNSWorld)tmpworld;
         if (event.equalsIgnoreCase("DOORS")) {
            world.getReflection().openDoor(17240111);

            for(int objId : world.getAllowed()) {
               Player pl = World.getInstance().getPlayer(objId);
               if (pl != null) {
                  pl.showQuestMovie(12);
               }

               ThreadPoolManager.getInstance().schedule(new SealOfTheEmperor.SpawnLilithRoom(world), 22000L);
               this.startQuestTimer("lilith_text", 26000L, npc, player);
               this.startQuestTimer("anakim_text", 26000L, npc, player);
               this.startQuestTimer("go_fight", 25000L, npc, player);
            }

            return null;
         }

         if (event.equalsIgnoreCase("anakim_text")) {
            this.cancelQuestTimer("anakim_text", npc, player);
            if (world._anakim != null) {
               NpcSay ns = new NpcSay(world._anakim.getObjectId(), 0, world._anakim.getId(), ANAKIM_TEXT[getRandom(ANAKIM_TEXT.length)]);
               player.sendPacket(ns);
               this.startQuestTimer("anakim_text", 20000L, npc, player);
            }

            return null;
         }

         if (event.equalsIgnoreCase("lilith_text")) {
            this.cancelQuestTimer("lilith_text", npc, player);
            if (world._lilith != null) {
               NpcSay ns = new NpcSay(world._lilith.getObjectId(), 0, world._lilith.getId(), LILITH_TEXT[getRandom(LILITH_TEXT.length)]);
               player.sendPacket(ns);
               this.startQuestTimer("lilith_text", 22000L, npc, player);
            }

            return null;
         }

         if (event.equalsIgnoreCase("go_fight")) {
            world._lilith_guard0.setIsImmobilized(false);
            world._lilith_guard1.setIsImmobilized(false);
            world._anakim_guard0.setIsImmobilized(false);
            world._anakim_guard1.setIsImmobilized(false);
            world._anakim_guard2.setIsImmobilized(false);
            return null;
         }

         if (event.equalsIgnoreCase("Delete")) {
            world._lilith.deleteMe();
            world._lilith = null;
            world._anakim.deleteMe();
            world._anakim = null;
            world._lilith_guard0.deleteMe();
            world._lilith_guard0 = null;
            world._lilith_guard1.deleteMe();
            world._lilith_guard1 = null;
            world._anakim_guard0.deleteMe();
            world._anakim_guard0 = null;
            world._anakim_guard1.deleteMe();
            world._anakim_guard1 = null;
            world._anakim_guard2.deleteMe();
            world._anakim_guard2 = null;
            return null;
         }

         if (event.equalsIgnoreCase("Tele")) {
            player.teleToLocation(-89528, 216056, -7516, true);
            return null;
         }
      }

      return null;
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(_196_SevenSignSealOfTheEmperor.class.getSimpleName());
      if (st == null) {
         return htmltext;
      } else {
         switch(npc.getId()) {
            case 32585:
               if (st.isCond(3) || st.isCond(4)) {
                  this.enterInstance(player, npc);
                  return null;
               }
            default:
               return htmltext;
         }
      }
   }

   @Override
   public final String onSpawn(Npc npc) {
      if (npc.getId() == 32718 || npc.getId() == 32715 || npc.getId() == 27384) {
         npc.setIsNoRndWalk(true);
         npc.setIsImmobilized(true);
      }

      return super.onSpawn(npc);
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState(_196_SevenSignSealOfTheEmperor.class.getSimpleName());
      if (st == null) {
         return null;
      } else {
         ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
         if (tmpworld != null && tmpworld instanceof SealOfTheEmperor.SIGNSWorld) {
            SealOfTheEmperor.SIGNSWorld world = (SealOfTheEmperor.SIGNSWorld)tmpworld;
            if (world.isStatus(1)) {
               if (this.checkKillProgress(world, npc)) {
                  this.runSecondRoom(world);
                  world.getReflection().openDoor(17240102);
               }
            } else if (world.isStatus(2)) {
               if (this.checkKillProgress(world, npc)) {
                  this.runThirdRoom(world);
                  world.getReflection().openDoor(17240104);
               }
            } else if (world.isStatus(3)) {
               if (this.checkKillProgress(world, npc)) {
                  this.runForthRoom(world);
                  world.getReflection().openDoor(17240106);
               }
            } else if (world.isStatus(4)) {
               if (this.checkKillProgress(world, npc)) {
                  this.runFifthRoom(world);
                  world.getReflection().openDoor(17240108);
               }
            } else if (world.isStatus(5)) {
               if (this.checkKillProgress(world, npc)) {
                  world.getReflection().openDoor(17240110);
               }
            } else if (world.isStatus(6) && npc.getId() == 27384) {
               if (st.getQuestItemsCount(13846) < 3L) {
                  npc.setRHandId(15281);
                  st.playSound("ItemSound.quest_itemget");
                  st.giveItems(13846, 1L);
               } else {
                  npc.setRHandId(15281);
                  giveItems(player, 13846, 1L);
                  st.playSound("ItemSound.quest_middle");
                  this.runSDRoom(world);
                  player.showQuestMovie(13);
                  this.startQuestTimer("Tele", 26000L, null, player);
                  this.startQuestTimer("Delete", 26000L, null, player);
               }
            }
         }

         return super.onKill(npc, player, isSummon);
      }
   }

   public static void main(String[] args) {
      new SealOfTheEmperor();
   }

   private class SIGNSWorld extends ReflectionWorld {
      public List<Npc> _npcList = new ArrayList<>();
      public Attackable _lilith = null;
      public Attackable _lilith_guard0 = null;
      public Attackable _lilith_guard1 = null;
      public Attackable _anakim = null;
      public Attackable _anakim_guard0 = null;
      public Attackable _anakim_guard1 = null;
      public Attackable _anakim_guard2 = null;

      public SIGNSWorld() {
      }
   }

   private class SpawnLilithRoom implements Runnable {
      private final SealOfTheEmperor.SIGNSWorld _world;

      public SpawnLilithRoom(SealOfTheEmperor.SIGNSWorld world) {
         this._world = world;
      }

      @Override
      public void run() {
         if (ReflectionManager.getInstance().getWorld(this._world.getReflectionId()) == this._world) {
            Reflection inst = ReflectionManager.getInstance().getReflection(this._world.getReflectionId());
            if (inst != null) {
               SealOfTheEmperor.this.runBossRoom(this._world);
            }
         }
      }
   }
}
