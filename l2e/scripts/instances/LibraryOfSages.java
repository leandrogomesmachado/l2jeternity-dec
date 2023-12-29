package l2e.scripts.instances;

import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.reflection.ReflectionTemplate;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class LibraryOfSages extends AbstractReflection {
   private static final NpcStringId[] spam = new NpcStringId[]{
      NpcStringId.I_MUST_ASK_LIBRARIAN_SOPHIA_ABOUT_THE_BOOK,
      NpcStringId.THIS_LIBRARY_ITS_HUGE_BUT_THERE_ARENT_MANY_USEFUL_BOOKS_RIGHT,
      NpcStringId.AN_UNDERGROUND_LIBRARY_I_HATE_DAMP_AND_SMELLY_PLACES,
      NpcStringId.THE_BOOK_THAT_WE_SEEK_IS_CERTAINLY_HERE_SEARCH_INCH_BY_INCH
   };

   public LibraryOfSages(String name, String descr) {
      super(name, descr);
      this.addStartNpc(new int[]{32861, 32596});
      this.addTalkId(new int[]{32861, 32863, 32596, 32785});
   }

   private final synchronized void enterInstance(Player player, Npc npc) {
      if (this.enterInstance(player, npc, new LibraryOfSages.LibraryOfSagesWorld(), 156)) {
         ReflectionWorld world = ReflectionManager.getInstance().getPlayerWorld(player);
         ((LibraryOfSages.LibraryOfSagesWorld)world).support = addSpawn(
            32785, player.getX(), player.getY(), player.getZ(), 0, false, 0L, false, player.getReflectionId()
         );
         this.startQuestTimer("check_follow", 3000L, ((LibraryOfSages.LibraryOfSagesWorld)world).support, player);
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

   private void teleportPlayer(Npc npc, Player player, Location loc, int instanceId) {
      player.stopAllEffectsExceptThoseThatLastThroughDeath();
      player.getAI().setIntention(CtrlIntention.IDLE);
      player.setReflectionId(instanceId);
      player.teleToLocation(loc.getX(), loc.getY(), loc.getZ(), false);
      if (instanceId > 0) {
         ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
         if (tmpworld instanceof LibraryOfSages.LibraryOfSagesWorld) {
            LibraryOfSages.LibraryOfSagesWorld world = (LibraryOfSages.LibraryOfSagesWorld)tmpworld;
            this.cancelQuestTimer("check_follow", world.support, player);
            world.support.deleteMe();
            world.support = addSpawn(32785, player.getX(), player.getY(), player.getZ(), 0, false, 0L, false, player.getReflectionId());
            this.startQuestTimer("check_follow", 3000L, world.support, player);
         }
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      if (event.equalsIgnoreCase("check_follow")) {
         this.cancelQuestTimer("check_follow", npc, player);
         npc.getAI().stopFollow();
         npc.setIsRunning(true);
         npc.getAI().startFollow(player);
         npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), spam[getRandom(0, spam.length - 1)]), 2000);
         this.startQuestTimer("check_follow", 20000L, npc, player);
         return "";
      } else {
         if (npc.getId() == 32596) {
            if (event.equalsIgnoreCase("tele1")) {
               this.enterInstance(player, npc);
               return null;
            }
         } else if (npc.getId() == 32861) {
            if (event.equalsIgnoreCase("tele2")) {
               this.teleportPlayer(player, new Location(37355, -50065, -1127), player.getReflectionId());
               return null;
            }

            if (event.equalsIgnoreCase("tele3")) {
               ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
               if (tmpworld instanceof LibraryOfSages.LibraryOfSagesWorld) {
                  LibraryOfSages.LibraryOfSagesWorld world = (LibraryOfSages.LibraryOfSagesWorld)tmpworld;
                  this.cancelQuestTimer("check_follow", world.support, player);
                  world.support.deleteMe();
                  this.teleportPlayer(npc, player, new Location(37063, -49813, -1128), 0);
               }

               return null;
            }
         } else if (npc.getId() == 32863 && event.equalsIgnoreCase("tele4")) {
            this.teleportPlayer(npc, player, new Location(37063, -49813, -1128), player.getReflectionId());
            return null;
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new LibraryOfSages(LibraryOfSages.class.getSimpleName(), "instances");
   }

   private class LibraryOfSagesWorld extends ReflectionWorld {
      Npc support = null;

      private LibraryOfSagesWorld() {
      }
   }
}
