package l2e.scripts.ai;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import l2e.commons.util.Util;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.TamedBeastInstance;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.scripts.quests._020_BringUpWithLove;

public class FeedableBeasts extends AbstractNpcAI {
   private static final int GOLDEN_SPICE = 6643;
   private static final int CRYSTAL_SPICE = 6644;
   private static final int SKILL_GOLDEN_SPICE = 2188;
   private static final int SKILL_CRYSTAL_SPICE = 2189;
   private static final int[] TAMED_BEASTS = new int[]{16013, 16014, 16015, 16016, 16017, 16018};
   private static final int FOODSKILLDIFF = 4455;
   private static final int[] FEEDABLE_BEASTS = new int[]{
      21451,
      21452,
      21453,
      21454,
      21455,
      21456,
      21457,
      21458,
      21459,
      21460,
      21461,
      21462,
      21463,
      21464,
      21465,
      21466,
      21467,
      21468,
      21469,
      21470,
      21471,
      21472,
      21473,
      21474,
      21475,
      21476,
      21477,
      21478,
      21479,
      21480,
      21481,
      21482,
      21483,
      21484,
      21485,
      21486,
      21487,
      21488,
      21489,
      21490,
      21491,
      21492,
      21493,
      21494,
      21495,
      21496,
      21497,
      21498,
      21499,
      21500,
      21501,
      21502,
      21503,
      21504,
      21505,
      21506,
      21507,
      21824,
      21825,
      21826,
      21827,
      21828,
      21829,
      16013,
      16014,
      16015,
      16016,
      16017,
      16018
   };
   private static final Map<Integer, Integer> MAD_COW_POLYMORPH = new ConcurrentHashMap<>();
   private static final NpcStringId[][] TEXT = new NpcStringId[][]{
      {
            NpcStringId.WHAT_DID_YOU_JUST_DO_TO_ME,
            NpcStringId.ARE_YOU_TRYING_TO_TAME_ME_DONT_DO_THAT,
            NpcStringId.DONT_GIVE_SUCH_A_THING_YOU_CAN_ENDANGER_YOURSELF,
            NpcStringId.YUCK_WHAT_IS_THIS_IT_TASTES_TERRIBLE,
            NpcStringId.IM_HUNGRY_GIVE_ME_A_LITTLE_MORE_PLEASE,
            NpcStringId.WHAT_IS_THIS_IS_THIS_EDIBLE,
            NpcStringId.DONT_WORRY_ABOUT_ME,
            NpcStringId.THANK_YOU_THAT_WAS_DELICIOUS,
            NpcStringId.I_THINK_I_AM_STARTING_TO_LIKE_YOU,
            NpcStringId.EEEEEK_EEEEEK
      },
      {
            NpcStringId.DONT_KEEP_TRYING_TO_TAME_ME_I_DONT_WANT_TO_BE_TAMED,
            NpcStringId.IT_IS_JUST_FOOD_TO_ME_ALTHOUGH_IT_MAY_ALSO_BE_YOUR_HAND,
            NpcStringId.IF_I_KEEP_EATING_LIKE_THIS_WONT_I_BECOME_FAT_CHOMP_CHOMP,
            NpcStringId.WHY_DO_YOU_KEEP_FEEDING_ME,
            NpcStringId.DONT_TRUST_ME_IM_AFRAID_I_MAY_BETRAY_YOU_LATER
      },
      {
            NpcStringId.GRRRRR,
            NpcStringId.YOU_BROUGHT_THIS_UPON_YOURSELF,
            NpcStringId.I_FEEL_STRANGE_I_KEEP_HAVING_THESE_EVIL_THOUGHTS,
            NpcStringId.ALAS_SO_THIS_IS_HOW_IT_ALL_ENDS,
            NpcStringId.I_DONT_FEEL_SO_GOOD_OH_MY_MIND_IS_VERY_TROUBLED
      }
   };
   private static final NpcStringId[] TAMED_TEXT = new NpcStringId[]{
      NpcStringId.S1_SO_WHAT_DO_YOU_THINK_IT_IS_LIKE_TO_BE_TAMED,
      NpcStringId.S1_WHENEVER_I_SEE_SPICE_I_THINK_I_WILL_MISS_YOUR_HAND_THAT_USED_TO_FEED_IT_TO_ME,
      NpcStringId.S1_DONT_GO_TO_THE_VILLAGE_I_DONT_HAVE_THE_STRENGTH_TO_FOLLOW_YOU,
      NpcStringId.THANK_YOU_FOR_TRUSTING_ME_S1_I_HOPE_I_WILL_BE_HELPFUL_TO_YOU,
      NpcStringId.S1_WILL_I_BE_ABLE_TO_HELP_YOU,
      NpcStringId.I_GUESS_ITS_JUST_MY_ANIMAL_MAGNETISM,
      NpcStringId.TOO_MUCH_SPICY_FOOD_MAKES_ME_SWEAT_LIKE_A_BEAST,
      NpcStringId.ANIMALS_NEED_LOVE_TOO
   };
   private static Map<Integer, Integer> _FeedInfo = new ConcurrentHashMap<>();
   private static Map<Integer, FeedableBeasts.GrowthCapableMob> _GrowthCapableMobs = new ConcurrentHashMap<>();

   private FeedableBeasts() {
      super(FeedableBeasts.class.getSimpleName(), "ai");
      this.registerMobs(FEEDABLE_BEASTS, new Quest.QuestEventType[]{Quest.QuestEventType.ON_KILL, Quest.QuestEventType.ON_SKILL_SEE});
      int[][] Kookabura_0_Gold = new int[][]{{21452, 21453, 21454, 21455}};
      int[][] Kookabura_0_Crystal = new int[][]{{21456, 21457, 21458, 21459}};
      int[][] Kookabura_1_Gold_1 = new int[][]{{21460, 21462}};
      int[][] Kookabura_1_Gold_2 = new int[][]{{21461, 21463}};
      int[][] Kookabura_1_Crystal_1 = new int[][]{{21464, 21466}};
      int[][] Kookabura_1_Crystal_2 = new int[][]{{21465, 21467}};
      int[][] Kookabura_2_1 = new int[][]{{21468, 21824}, {16017, 16018}};
      int[][] Kookabura_2_2 = new int[][]{{21469, 21825}, {16017, 16018}};
      int[][] Buffalo_0_Gold = new int[][]{{21471, 21472, 21473, 21474}};
      int[][] Buffalo_0_Crystal = new int[][]{{21475, 21476, 21477, 21478}};
      int[][] Buffalo_1_Gold_1 = new int[][]{{21479, 21481}};
      int[][] Buffalo_1_Gold_2 = new int[][]{{21481, 21482}};
      int[][] Buffalo_1_Crystal_1 = new int[][]{{21483, 21485}};
      int[][] Buffalo_1_Crystal_2 = new int[][]{{21484, 21486}};
      int[][] Buffalo_2_1 = new int[][]{{21487, 21826}, {16013, 16014}};
      int[][] Buffalo_2_2 = new int[][]{{21488, 21827}, {16013, 16014}};
      int[][] Cougar_0_Gold = new int[][]{{21490, 21491, 21492, 21493}};
      int[][] Cougar_0_Crystal = new int[][]{{21494, 21495, 21496, 21497}};
      int[][] Cougar_1_Gold_1 = new int[][]{{21498, 21500}};
      int[][] Cougar_1_Gold_2 = new int[][]{{21499, 21501}};
      int[][] Cougar_1_Crystal_1 = new int[][]{{21502, 21504}};
      int[][] Cougar_1_Crystal_2 = new int[][]{{21503, 21505}};
      int[][] Cougar_2_1 = new int[][]{{21506, 21828}, {16015, 16016}};
      int[][] Cougar_2_2 = new int[][]{{21507, 21829}, {16015, 16016}};
      FeedableBeasts.GrowthCapableMob temp = new FeedableBeasts.GrowthCapableMob(0, 100);
      temp.addMobs(6643, Kookabura_0_Gold);
      temp.addMobs(6644, Kookabura_0_Crystal);
      _GrowthCapableMobs.put(21451, temp);
      temp = new FeedableBeasts.GrowthCapableMob(1, 40);
      temp.addMobs(6643, Kookabura_1_Gold_1);
      _GrowthCapableMobs.put(21452, temp);
      _GrowthCapableMobs.put(21454, temp);
      temp = new FeedableBeasts.GrowthCapableMob(1, 40);
      temp.addMobs(6643, Kookabura_1_Gold_2);
      _GrowthCapableMobs.put(21453, temp);
      _GrowthCapableMobs.put(21455, temp);
      temp = new FeedableBeasts.GrowthCapableMob(1, 40);
      temp.addMobs(6644, Kookabura_1_Crystal_1);
      _GrowthCapableMobs.put(21456, temp);
      _GrowthCapableMobs.put(21458, temp);
      temp = new FeedableBeasts.GrowthCapableMob(1, 40);
      temp.addMobs(6644, Kookabura_1_Crystal_2);
      _GrowthCapableMobs.put(21457, temp);
      _GrowthCapableMobs.put(21459, temp);
      temp = new FeedableBeasts.GrowthCapableMob(2, 25);
      temp.addMobs(6643, Kookabura_2_1);
      _GrowthCapableMobs.put(21460, temp);
      _GrowthCapableMobs.put(21462, temp);
      temp = new FeedableBeasts.GrowthCapableMob(2, 25);
      temp.addMobs(6643, Kookabura_2_2);
      _GrowthCapableMobs.put(21461, temp);
      _GrowthCapableMobs.put(21463, temp);
      temp = new FeedableBeasts.GrowthCapableMob(2, 25);
      temp.addMobs(6644, Kookabura_2_1);
      _GrowthCapableMobs.put(21464, temp);
      _GrowthCapableMobs.put(21466, temp);
      temp = new FeedableBeasts.GrowthCapableMob(2, 25);
      temp.addMobs(6644, Kookabura_2_2);
      _GrowthCapableMobs.put(21465, temp);
      _GrowthCapableMobs.put(21467, temp);
      temp = new FeedableBeasts.GrowthCapableMob(0, 100);
      temp.addMobs(6643, Buffalo_0_Gold);
      temp.addMobs(6644, Buffalo_0_Crystal);
      _GrowthCapableMobs.put(21470, temp);
      temp = new FeedableBeasts.GrowthCapableMob(1, 40);
      temp.addMobs(6643, Buffalo_1_Gold_1);
      _GrowthCapableMobs.put(21471, temp);
      _GrowthCapableMobs.put(21473, temp);
      temp = new FeedableBeasts.GrowthCapableMob(1, 40);
      temp.addMobs(6643, Buffalo_1_Gold_2);
      _GrowthCapableMobs.put(21472, temp);
      _GrowthCapableMobs.put(21474, temp);
      temp = new FeedableBeasts.GrowthCapableMob(1, 40);
      temp.addMobs(6644, Buffalo_1_Crystal_1);
      _GrowthCapableMobs.put(21475, temp);
      _GrowthCapableMobs.put(21477, temp);
      temp = new FeedableBeasts.GrowthCapableMob(1, 40);
      temp.addMobs(6644, Buffalo_1_Crystal_2);
      _GrowthCapableMobs.put(21476, temp);
      _GrowthCapableMobs.put(21478, temp);
      temp = new FeedableBeasts.GrowthCapableMob(2, 25);
      temp.addMobs(6643, Buffalo_2_1);
      _GrowthCapableMobs.put(21479, temp);
      _GrowthCapableMobs.put(21481, temp);
      temp = new FeedableBeasts.GrowthCapableMob(2, 25);
      temp.addMobs(6643, Buffalo_2_2);
      _GrowthCapableMobs.put(21480, temp);
      _GrowthCapableMobs.put(21482, temp);
      temp = new FeedableBeasts.GrowthCapableMob(2, 25);
      temp.addMobs(6644, Buffalo_2_1);
      _GrowthCapableMobs.put(21483, temp);
      _GrowthCapableMobs.put(21485, temp);
      temp = new FeedableBeasts.GrowthCapableMob(2, 25);
      temp.addMobs(6644, Buffalo_2_2);
      _GrowthCapableMobs.put(21484, temp);
      _GrowthCapableMobs.put(21486, temp);
      temp = new FeedableBeasts.GrowthCapableMob(0, 100);
      temp.addMobs(6643, Cougar_0_Gold);
      temp.addMobs(6644, Cougar_0_Crystal);
      _GrowthCapableMobs.put(21489, temp);
      temp = new FeedableBeasts.GrowthCapableMob(1, 40);
      temp.addMobs(6643, Cougar_1_Gold_1);
      _GrowthCapableMobs.put(21490, temp);
      _GrowthCapableMobs.put(21492, temp);
      temp = new FeedableBeasts.GrowthCapableMob(1, 40);
      temp.addMobs(6643, Cougar_1_Gold_2);
      _GrowthCapableMobs.put(21491, temp);
      _GrowthCapableMobs.put(21493, temp);
      temp = new FeedableBeasts.GrowthCapableMob(1, 40);
      temp.addMobs(6644, Cougar_1_Crystal_1);
      _GrowthCapableMobs.put(21494, temp);
      _GrowthCapableMobs.put(21496, temp);
      temp = new FeedableBeasts.GrowthCapableMob(1, 40);
      temp.addMobs(6644, Cougar_1_Crystal_2);
      _GrowthCapableMobs.put(21495, temp);
      _GrowthCapableMobs.put(21497, temp);
      temp = new FeedableBeasts.GrowthCapableMob(2, 25);
      temp.addMobs(6643, Cougar_2_1);
      _GrowthCapableMobs.put(21498, temp);
      _GrowthCapableMobs.put(21500, temp);
      temp = new FeedableBeasts.GrowthCapableMob(2, 25);
      temp.addMobs(6643, Cougar_2_2);
      _GrowthCapableMobs.put(21499, temp);
      _GrowthCapableMobs.put(21501, temp);
      temp = new FeedableBeasts.GrowthCapableMob(2, 25);
      temp.addMobs(6644, Cougar_2_1);
      _GrowthCapableMobs.put(21502, temp);
      _GrowthCapableMobs.put(21504, temp);
      temp = new FeedableBeasts.GrowthCapableMob(2, 25);
      temp.addMobs(6644, Cougar_2_2);
      _GrowthCapableMobs.put(21503, temp);
      _GrowthCapableMobs.put(21505, temp);
   }

   public void spawnNext(Npc npc, int growthLevel, Player player, int food) {
      int npcId = npc.getId();
      int nextNpcId = 0;
      if (growthLevel == 2) {
         if (getRandom(2) == 0) {
            if (player.getClassId().isMage()) {
               nextNpcId = _GrowthCapableMobs.get(npcId).getMob(food, 1, 1);
            } else {
               nextNpcId = _GrowthCapableMobs.get(npcId).getMob(food, 1, 0);
            }
         } else if (getRandom(5) == 0) {
            nextNpcId = _GrowthCapableMobs.get(npcId).getMob(food, 0, 1);
         } else {
            nextNpcId = _GrowthCapableMobs.get(npcId).getMob(food, 0, 0);
         }
      } else {
         nextNpcId = _GrowthCapableMobs.get(npcId).getRandomMob(food);
      }

      if (_FeedInfo.containsKey(npc.getObjectId()) && _FeedInfo.get(npc.getObjectId()) == player.getObjectId()) {
         _FeedInfo.remove(npc.getObjectId());
      }

      npc.deleteMe();
      if (Util.contains(TAMED_BEASTS, nextNpcId)) {
         if (player.getTrainedBeasts() != null && !player.getTrainedBeasts().isEmpty()) {
            for(TamedBeastInstance oldTrained : player.getTrainedBeasts()) {
               oldTrained.deleteMe();
            }
         }

         NpcTemplate template = NpcsParser.getInstance().getTemplate(nextNpcId);
         TamedBeastInstance nextNpc = new TamedBeastInstance(
            IdFactory.getInstance().getNextId(), template, player, food - 4455, npc.getX(), npc.getY(), npc.getZ()
         );
         nextNpc.setRunning();
         _020_BringUpWithLove.checkJewelOfInnocence(player);
         if (getRandom(20) == 0) {
            NpcStringId message = NpcStringId.getNpcStringId(getRandom(2024, 2029));
            NpcSay packet = new NpcSay(nextNpc, 0, message);
            if (message.getParamCount() > 0) {
               packet.addStringParameter(player.getName());
            }

            npc.broadcastPacket(packet, 2000);
         }
      } else {
         Attackable nextNpc = (Attackable)addSpawn(nextNpcId, npc);
         if (MAD_COW_POLYMORPH.containsKey(nextNpcId)) {
            this.startQuestTimer("polymorph Mad Cow", 10000L, nextNpc, player);
         }

         _FeedInfo.put(nextNpc.getObjectId(), player.getObjectId());
         nextNpc.setRunning();
         nextNpc.addDamageHate(player, 0, 99999);
         nextNpc.getAI().setIntention(CtrlIntention.ATTACK, player);
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      if (event.equalsIgnoreCase("polymorph Mad Cow") && npc != null && player != null && MAD_COW_POLYMORPH.containsKey(npc.getId())) {
         if (_FeedInfo.get(npc.getObjectId()) == player.getObjectId()) {
            _FeedInfo.remove(npc.getObjectId());
         }

         npc.deleteMe();
         Attackable nextNpc = (Attackable)addSpawn(MAD_COW_POLYMORPH.get(npc.getId()), npc);
         _FeedInfo.put(nextNpc.getObjectId(), player.getObjectId());
         nextNpc.setRunning();
         nextNpc.addDamageHate(player, 0, 99999);
         nextNpc.getAI().setIntention(CtrlIntention.ATTACK, player);
      }

      return super.onAdvEvent(event, npc, player);
   }

   @Override
   public String onSkillSee(Npc npc, Player caster, Skill skill, GameObject[] targets, boolean isSummon) {
      if (!Util.contains(targets, npc)) {
         return super.onSkillSee(npc, caster, skill, targets, isSummon);
      } else {
         int npcId = npc.getId();
         int skillId = skill.getId();
         if (Util.contains(FEEDABLE_BEASTS, npcId) && (skillId == 2188 || skillId == 2189)) {
            int objectId = npc.getObjectId();
            int growthLevel = 3;
            if (_GrowthCapableMobs.containsKey(npcId)) {
               growthLevel = _GrowthCapableMobs.get(npcId).getGrowthLevel();
            }

            if (growthLevel == 0 && _FeedInfo.containsKey(objectId)) {
               return super.onSkillSee(npc, caster, skill, targets, isSummon);
            } else {
               _FeedInfo.put(objectId, caster.getObjectId());
               int food = 0;
               if (skillId == 2188) {
                  food = 6643;
               } else if (skillId == 2189) {
                  food = 6644;
               }

               npc.broadcastSocialAction(2);
               if (_GrowthCapableMobs.containsKey(npcId)) {
                  if (_GrowthCapableMobs.get(npcId).getMob(food, 0, 0) == null) {
                     return super.onSkillSee(npc, caster, skill, targets, isSummon);
                  }

                  if (getRandom(20) == 0) {
                     NpcStringId message = TEXT[growthLevel][getRandom(TEXT[growthLevel].length)];
                     NpcSay packet = new NpcSay(npc, 0, message);
                     if (message.getParamCount() > 0) {
                        packet.addStringParameter(caster.getName());
                     }

                     npc.broadcastPacket(packet, 2000);
                  }

                  if (growthLevel > 0 && _FeedInfo.get(objectId) != caster.getObjectId()) {
                     return super.onSkillSee(npc, caster, skill, targets, isSummon);
                  }

                  if (getRandom(100) < _GrowthCapableMobs.get(npcId).getChance()) {
                     this.spawnNext(npc, growthLevel, caster, food);
                  }
               } else if (Util.contains(TAMED_BEASTS, npcId) && npc instanceof TamedBeastInstance) {
                  TamedBeastInstance beast = (TamedBeastInstance)npc;
                  if (skillId == beast.getFoodType()) {
                     beast.onReceiveFood();
                     NpcStringId message = TAMED_TEXT[getRandom(TAMED_TEXT.length)];
                     NpcSay packet = new NpcSay(npc, 0, message);
                     if (message.getParamCount() > 0) {
                        packet.addStringParameter(caster.getName());
                     }

                     beast.broadcastPacket(packet, 2000);
                  }
               }

               return super.onSkillSee(npc, caster, skill, targets, isSummon);
            }
         } else {
            return super.onSkillSee(npc, caster, skill, targets, isSummon);
         }
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      if (_FeedInfo.containsKey(npc.getObjectId())) {
         _FeedInfo.remove(npc.getObjectId());
      }

      return super.onKill(npc, killer, isSummon);
   }

   public static void main(String[] args) {
      new FeedableBeasts();
   }

   static {
      MAD_COW_POLYMORPH.put(21824, 21468);
      MAD_COW_POLYMORPH.put(21825, 21469);
      MAD_COW_POLYMORPH.put(21826, 21487);
      MAD_COW_POLYMORPH.put(21827, 21488);
      MAD_COW_POLYMORPH.put(21828, 21506);
      MAD_COW_POLYMORPH.put(21829, 21507);
   }

   private static class GrowthCapableMob {
      private final int _growthLevel;
      private final int _chance;
      private final Map<Integer, int[][]> _spiceToMob = new ConcurrentHashMap<>();

      public GrowthCapableMob(int growthLevel, int chance) {
         this._growthLevel = growthLevel;
         this._chance = chance;
      }

      public void addMobs(int spice, int[][] Mobs) {
         this._spiceToMob.put(spice, Mobs);
      }

      public Integer getMob(int spice, int mobType, int classType) {
         return this._spiceToMob.containsKey(spice) ? this._spiceToMob.get(spice)[mobType][classType] : null;
      }

      public Integer getRandomMob(int spice) {
         int[][] temp = (int[][])this._spiceToMob.get(spice);
         int rand = Quest.getRandom(temp[0].length);
         return temp[0][rand];
      }

      public Integer getChance() {
         return this._chance;
      }

      public Integer getGrowthLevel() {
         return this._growthLevel;
      }
   }
}
