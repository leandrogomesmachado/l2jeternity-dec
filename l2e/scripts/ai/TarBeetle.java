package l2e.scripts.ai;

import l2e.commons.threading.RunnableImpl;
import l2e.commons.util.Rnd;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.DefaultAI;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.network.serverpackets.MagicSkillUse;

public class TarBeetle extends DefaultAI {
   public static final Location[] POSITIONS = new Location[]{
      new Location(179256, -117160, -3608),
      new Location(179752, -115000, -3608),
      new Location(177944, -119528, -4112),
      new Location(177144, -120808, -4112),
      new Location(181224, -120088, -3672),
      new Location(181960, -117864, -3328),
      new Location(186200, -118120, -3272),
      new Location(188840, -118696, -3288),
      new Location(185448, -120536, -3088),
      new Location(183672, -119048, -3088),
      new Location(188072, -120824, -3088),
      new Location(189592, -120392, -3048),
      new Location(189448, -117464, -3288),
      new Location(188456, -115816, -3288),
      new Location(186424, -114440, -3280),
      new Location(185112, -113272, -3280),
      new Location(187768, -112952, -3288),
      new Location(189176, -111672, -3288),
      new Location(189960, -108712, -3288),
      new Location(187816, -110536, -3288),
      new Location(185368, -109880, -3288),
      new Location(181848, -109368, -3664),
      new Location(181816, -112392, -3664),
      new Location(180136, -112632, -3664),
      new Location(183608, -111432, -3648),
      new Location(178632, -108568, -3664),
      new Location(176264, -109448, -3664),
      new Location(176072, -112952, -3488),
      new Location(175720, -112136, -5520),
      new Location(178504, -112712, -5816),
      new Location(180248, -116136, -6104),
      new Location(182552, -114824, -6104),
      new Location(184248, -116600, -6104),
      new Location(181336, -110536, -5832),
      new Location(182088, -106664, -6000),
      new Location(178808, -107736, -5832),
      new Location(178776, -110120, -5824)
   };
   private boolean CAN_DEBUF = false;
   private static final long TELEPORT_PERIOD = 180000L;
   private long LAST_TELEPORT = System.currentTimeMillis();

   public TarBeetle(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtSpawn() {
      Attackable npc = this.getActiveChar();
      npc.setIsNoRndWalk(true);
      npc.setIsImmobilized(true);
      npc.setIsInvul(true);
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      if (attacker != null && !attacker.isPlayable()) {
         super.onEvtAttacked(attacker, damage);
      }
   }

   @Override
   protected boolean thinkActive() {
      Attackable actor = this.getActiveChar();
      if (Rnd.chance(1)) {
         this.CAN_DEBUF = true;
      }

      if (this.CAN_DEBUF) {
         for(Player player : World.getInstance().getAroundPlayers(actor, 500, 200)) {
            this.addEffect(actor, player);
         }

         this.CAN_DEBUF = false;
      }

      if (actor != null && System.currentTimeMillis() - this.LAST_TELEPORT >= 180000L) {
         for(int i = 0; i < POSITIONS.length; ++i) {
            Location loc = POSITIONS[Rnd.get(POSITIONS.length)];
            if (!actor.getLocation().equals(loc)) {
               int x = loc.getX() + Rnd.get(1, 8);
               int y = loc.getY() + Rnd.get(1, 8);
               int z = GeoEngine.getHeight(x, y, loc.getZ(), actor.getGeoIndex());
               actor.broadcastPacket(new MagicSkillUse(actor, actor, 4671, 1, 500, 0));
               ThreadPoolManager.getInstance().schedule(new TarBeetle.Teleport(new Location(x, y, z)), 500L);
               this.LAST_TELEPORT = System.currentTimeMillis();
               break;
            }
         }

         return super.thinkActive();
      } else {
         return false;
      }
   }

   protected void addEffect(Attackable actor, Player player) {
      Effect effect = player.getFirstEffect(6142);
      if (effect != null) {
         int level = effect.getSkill().getLevel();
         if (level < 3) {
            effect.exit();
            Skill skill = SkillsParser.getInstance().getInfo(6142, level + 1);
            skill.getEffects(actor, player, false);
            actor.broadcastPacket(new MagicSkillUse(actor, player, skill.getId(), level, skill.getHitTime(), 0));
         }
      } else {
         Skill skill = SkillsParser.getInstance().getInfo(6142, 1);
         if (skill != null) {
            skill.getEffects(actor, player, false);
            actor.broadcastPacket(new MagicSkillUse(actor, player, skill.getId(), 1, skill.getHitTime(), 0));
         }
      }
   }

   protected class Teleport extends RunnableImpl {
      Location _loc;

      public Teleport(Location destination) {
         this._loc = destination;
      }

      @Override
      public void runImpl() throws Exception {
         Attackable actor = TarBeetle.this.getActiveChar();
         if (actor != null) {
            actor.teleToLocation(this._loc, true);
         }
      }
   }
}
