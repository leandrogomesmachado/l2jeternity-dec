package l2e.scripts.ai.groups;

import l2e.commons.util.Util;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.zone.type.EffectZone;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.scripts.ai.AbstractNpcAI;

public class DenOfEvil extends AbstractNpcAI {
   protected static final int[] EYE_IDS = new int[]{18812, 18813, 18814};
   private static final int SKILL_ID = 6150;
   private static final Location[] EYE_SPAWNS = new Location[]{
      new Location(71544, -129400, -3360, 16472),
      new Location(70954, -128854, -3360, 16),
      new Location(72145, -128847, -3368, 32832),
      new Location(76147, -128372, -3144, 16152),
      new Location(71573, -128309, -3360, 49152),
      new Location(75211, -127441, -3152, 0),
      new Location(77005, -127406, -3144, 32784),
      new Location(75965, -126486, -3144, 49120),
      new Location(70972, -126429, -3016, 19208),
      new Location(69916, -125838, -3024, 2840),
      new Location(71658, -125459, -3016, 35136),
      new Location(70605, -124646, -3040, 52104),
      new Location(67283, -123237, -2912, 12376),
      new Location(68383, -122754, -2912, 27904),
      new Location(74137, -122733, -3024, 13272),
      new Location(66736, -122007, -2896, 60576),
      new Location(73289, -121769, -3024, 1024),
      new Location(67894, -121491, -2912, 43872),
      new Location(75530, -121477, -3008, 34424),
      new Location(74117, -120459, -3024, 52344),
      new Location(69608, -119855, -2534, 17251),
      new Location(71014, -119027, -2520, 31904),
      new Location(68944, -118964, -2527, 59874),
      new Location(62261, -118263, -3072, 12888),
      new Location(70300, -117942, -2528, 46208),
      new Location(74312, -117583, -2272, 15280),
      new Location(63276, -117409, -3064, 24760),
      new Location(68104, -117192, -2168, 15888),
      new Location(73758, -116945, -2216, 0),
      new Location(74944, -116858, -2220, 30892),
      new Location(61715, -116623, -3064, 59888),
      new Location(69140, -116464, -2168, 28952),
      new Location(67311, -116374, -2152, 1280),
      new Location(62459, -116370, -3064, 48624),
      new Location(74475, -116260, -2216, 47456),
      new Location(68333, -115015, -2168, 45136),
      new Location(68280, -108129, -1160, 17992),
      new Location(62983, -107259, -2384, 12552),
      new Location(67062, -107125, -1144, 64008),
      new Location(68893, -106954, -1160, 36704),
      new Location(63848, -106771, -2384, 32784),
      new Location(62372, -106514, -2384, 0),
      new Location(67838, -106143, -1160, 51232),
      new Location(62905, -106109, -2384, 51288)
   };

   private DenOfEvil(String name, String descr) {
      super(name, descr);
      this.registerMobs(EYE_IDS, new Quest.QuestEventType[]{Quest.QuestEventType.ON_KILL, Quest.QuestEventType.ON_SPAWN});
      this.spawnEyes();
   }

   private int getSkillIdByNpcId(int npcId) {
      int diff = npcId - EYE_IDS[0];
      diff *= 2;
      return 6150 + diff;
   }

   @Override
   public String onSpawn(Npc npc) {
      if (Util.contains(EYE_IDS, npc.getId())) {
         npc.disableCoreAI(true);
         npc.setIsImmobilized(true);
         npc.setScriptValue(0);
         EffectZone zone = ZoneManager.getInstance().getZone(npc, EffectZone.class);
         if (zone == null) {
            this._log
               .warning("NPC " + npc + " spawned outside of EffectZone, check your zone coords! X:" + npc.getX() + " Y:" + npc.getY() + " Z:" + npc.getZ());
            return null;
         }

         int skillId = this.getSkillIdByNpcId(npc.getId());
         int skillLevel = zone.getSkillLevel(skillId);
         zone.addSkill(skillId, skillLevel + 1);
         if (skillLevel == 3) {
            ThreadPoolManager.getInstance().schedule(new DenOfEvil.KashaDestruction(zone), 120000L);
            zone.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.KASHA_EYE_PITCHES_TOSSES_EXPLODE));
         } else if (skillLevel == 2) {
            zone.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.I_CAN_FEEL_ENERGY_KASHA_EYE_GETTING_STRONGER_RAPIDLY));
         }
      }

      return null;
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      if (Util.contains(EYE_IDS, npc.getId()) && npc.isScriptValue(0)) {
         ThreadPoolManager.getInstance().schedule(new DenOfEvil.RespawnNewEye(npc.getLocation()), 15000L);
         EffectZone zone = ZoneManager.getInstance().getZone(npc, EffectZone.class);
         if (zone == null) {
            this._log
               .warning("NPC " + npc + " killed outside of EffectZone, check your zone coords! X:" + npc.getX() + " Y:" + npc.getY() + " Z:" + npc.getZ());
            return null;
         }

         int skillId = this.getSkillIdByNpcId(npc.getId());
         int skillLevel = zone.getSkillLevel(skillId);
         zone.addSkill(skillId, skillLevel - 1);
      }

      return null;
   }

   private void spawnEyes() {
      for(Location loc : EYE_SPAWNS) {
         addSpawn(EYE_IDS[getRandom(EYE_IDS.length)], loc, false, 0L);
      }
   }

   public static void main(String[] args) {
      new DenOfEvil(DenOfEvil.class.getSimpleName(), "ai");
   }

   private class KashaDestruction implements Runnable {
      EffectZone _zone;

      public KashaDestruction(EffectZone zone) {
         this._zone = zone;
      }

      @Override
      public void run() {
         for(int i = 6150; i <= 6154; i += 2) {
            if (this._zone.getSkillLevel(i) > 3) {
               this.destroyZone();
               break;
            }
         }
      }

      private void destroyZone() {
         for(Creature character : this._zone.getCharactersInside()) {
            if (character != null) {
               if (character.isPlayable()) {
                  Skill skill = SkillsParser.getInstance().getInfo(6149, 1);
                  skill.getEffects(character, character, false);
               } else if (character.isNpc()) {
                  Npc npc = (Npc)character;
                  if (Util.contains(DenOfEvil.EYE_IDS, npc.getId()) && !npc.isDead()) {
                     npc.setScriptValue(1);
                     npc.doDie(null);
                     ThreadPoolManager.getInstance().schedule(DenOfEvil.this.new RespawnNewEye(npc.getLocation()), 15000L);
                  }
               }
            }
         }

         for(int i = 6150; i <= 6154; i += 2) {
            this._zone.removeSkill(i);
         }
      }
   }

   private class RespawnNewEye implements Runnable {
      private final Location _loc;

      public RespawnNewEye(Location loc) {
         this._loc = loc;
      }

      @Override
      public void run() {
         Quest.addSpawn(DenOfEvil.EYE_IDS[Quest.getRandom(DenOfEvil.EYE_IDS.length)], this._loc, false, 0L);
      }
   }
}
