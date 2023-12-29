package l2e.scripts.custom;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.SpawnParser;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.spawn.Spawner;
import l2e.scripts.ai.AbstractNpcAI;

public class ZealotOfShilen extends AbstractNpcAI {
   private static final int ZEALOT = 18782;
   private static final int GUARD1 = 32628;
   private static final int GUARD2 = 32629;
   private final List<Npc> _zealot = new ArrayList<>();
   private final List<Npc> _guard1 = new ArrayList<>();
   private final List<Npc> _guard2 = new ArrayList<>();

   private ZealotOfShilen(String name, String descr) {
      super(name, descr);
      this.addSpawnId(new int[]{18782});
      this.addFirstTalkId(new int[]{32628, 32629});
      this.findNpcs();
   }

   private void findNpcs() {
      for(Spawner spawn : SpawnParser.getInstance().getSpawnData()) {
         if (spawn != null) {
            if (spawn.getId() == 18782) {
               this._zealot.add(spawn.getLastSpawn());

               for(Npc zealot : this._zealot) {
                  zealot.setIsNoRndWalk(true);
               }
            } else if (spawn.getId() == 32628) {
               this._guard1.add(spawn.getLastSpawn());

               for(Npc guard : this._guard1) {
                  guard.setIsInvul(true);
                  ((Attackable)guard).setCanReturnToSpawnPoint(false);
                  this.startQuestTimer("WATCHING", 10000L, guard, null, true);
               }
            } else if (spawn.getId() == 32629) {
               this._guard2.add(spawn.getLastSpawn());

               for(Npc guards : this._guard2) {
                  guards.setIsInvul(true);
                  ((Attackable)guards).setCanReturnToSpawnPoint(false);
                  this.startQuestTimer("WATCHING", 10000L, guards, null, true);
               }
            }
         }
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      if (event.equalsIgnoreCase("WATCHING") && !npc.isAttackingNow()) {
         for(Npc character : World.getInstance().getAroundNpc(npc)) {
            if (character.isMonster() && !character.isDead() && !((Attackable)character).isDecayed()) {
               npc.setRunning();
               ((Attackable)npc).addDamageHate(character, 0, 999);
               npc.getAI().setIntention(CtrlIntention.ATTACK, character, null);
            }
         }
      }

      return null;
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      return npc.isAttackingNow() ? "32628-01.htm" : npc.getId() + ".htm";
   }

   @Override
   public String onSpawn(Npc npc) {
      npc.setIsNoRndWalk(true);
      return super.onSpawn(npc);
   }

   public static void main(String[] args) {
      new ZealotOfShilen(ZealotOfShilen.class.getSimpleName(), "custom");
   }
}
