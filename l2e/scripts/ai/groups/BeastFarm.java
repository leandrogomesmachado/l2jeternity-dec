package l2e.scripts.ai.groups;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import l2e.commons.util.Util;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.TamedBeastInstance;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.network.serverpackets.NpcInfo;
import l2e.scripts.ai.AbstractNpcAI;
import l2e.scripts.quests._020_BringUpWithLove;
import l2e.scripts.quests._655_AGrandPlanForTamingWildBeasts;

public class BeastFarm extends AbstractNpcAI {
   private static final int GOLDEN_SPICE = 15474;
   private static final int CRYSTAL_SPICE = 15475;
   private static final int SKILL_GOLDEN_SPICE = 9049;
   private static final int SKILL_CRYSTAL_SPICE = 9050;
   private static final int SKILL_BLESSED_GOLDEN_SPICE = 9051;
   private static final int SKILL_BLESSED_CRYSTAL_SPICE = 9052;
   private static final int SKILL_SGRADE_GOLDEN_SPICE = 9053;
   private static final int SKILL_SGRADE_CRYSTAL_SPICE = 9054;
   private static final int[] TAMED_BEASTS = new int[]{18869, 18870, 18871, 18872};
   private static final int TAME_CHANCE = 20;
   protected static final int[] SPECIAL_SPICE_CHANCES = new int[]{33, 75};
   private static final int[] FEEDABLE_BEASTS = new int[]{
      18873,
      18874,
      18875,
      18876,
      18877,
      18878,
      18879,
      18880,
      18881,
      18882,
      18883,
      18884,
      18885,
      18886,
      18887,
      18888,
      18889,
      18890,
      18891,
      18892,
      18893,
      18894,
      18895,
      18896,
      18897,
      18898,
      18899,
      18900
   };
   private static Map<Integer, Integer> _FeedInfo = new ConcurrentHashMap<>();
   private static Map<Integer, BeastFarm.GrowthCapableMob> _GrowthCapableMobs = new ConcurrentHashMap<>();
   private static List<BeastFarm.TamedBeast> TAMED_BEAST_DATA = new ArrayList<>();

   private BeastFarm() {
      super(BeastFarm.class.getSimpleName(), "ai");
      this.registerMobs(FEEDABLE_BEASTS, new Quest.QuestEventType[]{Quest.QuestEventType.ON_KILL, Quest.QuestEventType.ON_SKILL_SEE});
      BeastFarm.GrowthCapableMob temp = new BeastFarm.GrowthCapableMob(100, 0, 18869);
      temp.addNpcIdForSkillId(9049, 18874);
      temp.addNpcIdForSkillId(9050, 18875);
      temp.addNpcIdForSkillId(9051, 18869);
      temp.addNpcIdForSkillId(9052, 18869);
      temp.addNpcIdForSkillId(9053, 18878);
      temp.addNpcIdForSkillId(9054, 18879);
      _GrowthCapableMobs.put(18873, temp);
      temp = new BeastFarm.GrowthCapableMob(40, 1, 18869);
      temp.addNpcIdForSkillId(9049, 18876);
      _GrowthCapableMobs.put(18874, temp);
      temp = new BeastFarm.GrowthCapableMob(40, 1, 18869);
      temp.addNpcIdForSkillId(9050, 18877);
      _GrowthCapableMobs.put(18875, temp);
      temp = new BeastFarm.GrowthCapableMob(25, 2, 18869);
      temp.addNpcIdForSkillId(9049, 18878);
      _GrowthCapableMobs.put(18876, temp);
      temp = new BeastFarm.GrowthCapableMob(25, 2, 18869);
      temp.addNpcIdForSkillId(9050, 18879);
      _GrowthCapableMobs.put(18877, temp);
      temp = new BeastFarm.GrowthCapableMob(100, 0, 18870);
      temp.addNpcIdForSkillId(9049, 18881);
      temp.addNpcIdForSkillId(9050, 18882);
      temp.addNpcIdForSkillId(9051, 18870);
      temp.addNpcIdForSkillId(9052, 18870);
      temp.addNpcIdForSkillId(9053, 18885);
      temp.addNpcIdForSkillId(9054, 18886);
      _GrowthCapableMobs.put(18880, temp);
      temp = new BeastFarm.GrowthCapableMob(40, 1, 18870);
      temp.addNpcIdForSkillId(9049, 18883);
      _GrowthCapableMobs.put(18881, temp);
      temp = new BeastFarm.GrowthCapableMob(40, 1, 18870);
      temp.addNpcIdForSkillId(9050, 18884);
      _GrowthCapableMobs.put(18882, temp);
      temp = new BeastFarm.GrowthCapableMob(25, 2, 18870);
      temp.addNpcIdForSkillId(9049, 18885);
      _GrowthCapableMobs.put(18883, temp);
      temp = new BeastFarm.GrowthCapableMob(25, 2, 18870);
      temp.addNpcIdForSkillId(9050, 18886);
      _GrowthCapableMobs.put(18884, temp);
      temp = new BeastFarm.GrowthCapableMob(100, 0, 18871);
      temp.addNpcIdForSkillId(9049, 18888);
      temp.addNpcIdForSkillId(9050, 18889);
      temp.addNpcIdForSkillId(9051, 18871);
      temp.addNpcIdForSkillId(9052, 18871);
      temp.addNpcIdForSkillId(9053, 18892);
      temp.addNpcIdForSkillId(9054, 18893);
      _GrowthCapableMobs.put(18887, temp);
      temp = new BeastFarm.GrowthCapableMob(40, 1, 18871);
      temp.addNpcIdForSkillId(9049, 18890);
      _GrowthCapableMobs.put(18888, temp);
      temp = new BeastFarm.GrowthCapableMob(40, 1, 18871);
      temp.addNpcIdForSkillId(9050, 18891);
      _GrowthCapableMobs.put(18889, temp);
      temp = new BeastFarm.GrowthCapableMob(25, 2, 18871);
      temp.addNpcIdForSkillId(9049, 18892);
      _GrowthCapableMobs.put(18890, temp);
      temp = new BeastFarm.GrowthCapableMob(25, 2, 18871);
      temp.addNpcIdForSkillId(9050, 18893);
      _GrowthCapableMobs.put(18891, temp);
      temp = new BeastFarm.GrowthCapableMob(100, 0, 18872);
      temp.addNpcIdForSkillId(9049, 18895);
      temp.addNpcIdForSkillId(9050, 18896);
      temp.addNpcIdForSkillId(9051, 18872);
      temp.addNpcIdForSkillId(9052, 18872);
      temp.addNpcIdForSkillId(9053, 18899);
      temp.addNpcIdForSkillId(9054, 18900);
      _GrowthCapableMobs.put(18894, temp);
      temp = new BeastFarm.GrowthCapableMob(40, 1, 18872);
      temp.addNpcIdForSkillId(9049, 18897);
      _GrowthCapableMobs.put(18895, temp);
      temp = new BeastFarm.GrowthCapableMob(40, 1, 18872);
      temp.addNpcIdForSkillId(9050, 18898);
      _GrowthCapableMobs.put(18896, temp);
      temp = new BeastFarm.GrowthCapableMob(25, 2, 18872);
      temp.addNpcIdForSkillId(9049, 18899);
      _GrowthCapableMobs.put(18897, temp);
      temp = new BeastFarm.GrowthCapableMob(25, 2, 18872);
      temp.addNpcIdForSkillId(9050, 18900);
      _GrowthCapableMobs.put(18898, temp);
      TAMED_BEAST_DATA.add(new BeastFarm.TamedBeast("%name% of Focus", new SkillHolder(6432, 1), new SkillHolder(6668, 1)));
      TAMED_BEAST_DATA.add(new BeastFarm.TamedBeast("%name% of Guiding", new SkillHolder(6433, 1), new SkillHolder(6670, 1)));
      TAMED_BEAST_DATA.add(new BeastFarm.TamedBeast("%name% of Swifth", new SkillHolder(6434, 1), new SkillHolder(6667, 1)));
      TAMED_BEAST_DATA.add(new BeastFarm.TamedBeast("Berserker %name%", new SkillHolder(6671, 1)));
      TAMED_BEAST_DATA.add(new BeastFarm.TamedBeast("%name% of Protect", new SkillHolder(6669, 1), new SkillHolder(6672, 1)));
      TAMED_BEAST_DATA.add(new BeastFarm.TamedBeast("%name% of Vigor", new SkillHolder(6431, 1), new SkillHolder(6666, 1)));
   }

   public void spawnNext(Npc npc, Player player, int nextNpcId, int food) {
      if (_FeedInfo.containsKey(npc.getObjectId()) && player != null && _FeedInfo.get(npc.getObjectId()) == player.getObjectId()) {
         _FeedInfo.remove(npc.getObjectId());
      }

      npc.deleteMe();
      if (Util.contains(TAMED_BEASTS, nextNpcId)) {
         if (player.getTrainedBeasts() != null && player.getTrainedBeasts().size() >= 7) {
            return;
         }

         NpcTemplate template = NpcsParser.getInstance().getTemplate(nextNpcId);
         TamedBeastInstance nextNpc = new TamedBeastInstance(
            IdFactory.getInstance().getNextId(), template, player, food, npc.getX(), npc.getY(), npc.getZ(), true
         );
         BeastFarm.TamedBeast beast = TAMED_BEAST_DATA.get(getRandom(TAMED_BEAST_DATA.size()));
         String name = beast.getName();
         switch(nextNpcId) {
            case 18869:
               name = name.replace("%name%", "Alpine Kookaburra");
               break;
            case 18870:
               name = name.replace("%name%", "Alpine Cougar");
               break;
            case 18871:
               name = name.replace("%name%", "Alpine Buffalo");
               break;
            case 18872:
               name = name.replace("%name%", "Alpine Grendel");
         }

         nextNpc.setName(name);
         nextNpc.broadcastPacket(new NpcInfo.Info(nextNpc, player));
         nextNpc.setRunning();
         SkillsParser st = SkillsParser.getInstance();

         for(SkillHolder sh : beast.getSkills()) {
            nextNpc.addBeastSkill(st.getInfo(sh.getId(), sh.getLvl()));
         }

         _020_BringUpWithLove.checkJewelOfInnocence(player);
         _655_AGrandPlanForTamingWildBeasts.checkCrystalofPurity(player);
      } else {
         Attackable nextNpc = (Attackable)addSpawn(nextNpcId, npc);
         _FeedInfo.put(nextNpc.getObjectId(), player.getObjectId());
         nextNpc.setRunning();
         nextNpc.addDamageHate(player, 0, 99999);
         nextNpc.getAI().setIntention(CtrlIntention.ATTACK, player);
         player.setTarget(nextNpc);
      }
   }

   @Override
   public String onSkillSee(Npc npc, Player caster, Skill skill, GameObject[] targets, boolean isSummon) {
      if (!Util.contains(targets, npc)) {
         return super.onSkillSee(npc, caster, skill, targets, isSummon);
      } else {
         int npcId = npc.getId();
         int skillId = skill.getId();
         if (Util.contains(FEEDABLE_BEASTS, npcId)
            && (skillId == 9049 || skillId == 9050 || skillId == 9051 || skillId == 9052 || skillId == 9053 || skillId == 9054)) {
            int objectId = npc.getObjectId();
            int growthLevel = 3;
            if (_GrowthCapableMobs.containsKey(npcId)) {
               growthLevel = _GrowthCapableMobs.get(npcId).getGrowthLevel();
            }

            if (growthLevel == 0 && _FeedInfo.containsKey(objectId)) {
               return super.onSkillSee(npc, caster, skill, targets, isSummon);
            } else {
               _FeedInfo.put(objectId, caster.getObjectId());
               npc.broadcastSocialAction(2);
               int food = 0;
               if (skillId == 9049 || skillId == 9051) {
                  food = 15474;
               } else if (skillId == 9050 || skillId == 9052) {
                  food = 15475;
               }

               if (_GrowthCapableMobs.containsKey(npcId)) {
                  int newNpcId = _GrowthCapableMobs.get(npcId).getLeveledNpcId(skillId);
                  if (newNpcId == -1) {
                     if (growthLevel == 0) {
                        _FeedInfo.remove(objectId);
                        npc.setRunning();
                        ((Attackable)npc).addDamageHate(caster, 0, 1);
                        npc.getAI().setIntention(CtrlIntention.ATTACK, caster);
                     }

                     return super.onSkillSee(npc, caster, skill, targets, isSummon);
                  }

                  if (growthLevel > 0 && _FeedInfo.get(objectId) != caster.getObjectId()) {
                     return super.onSkillSee(npc, caster, skill, targets, isSummon);
                  }

                  this.spawnNext(npc, caster, newNpcId, food);
               } else {
                  caster.sendMessage("The beast spit out the feed instead of eating it.");
                  ((Attackable)npc).dropItem(caster, food, 1L);
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
      new BeastFarm();
   }

   private static class GrowthCapableMob {
      private final int _chance;
      private final int _growthLevel;
      private final int _tameNpcId;
      private final Map<Integer, Integer> _skillSuccessNpcIdList = new ConcurrentHashMap<>();

      public GrowthCapableMob(int chance, int growthLevel, int tameNpcId) {
         this._chance = chance;
         this._growthLevel = growthLevel;
         this._tameNpcId = tameNpcId;
      }

      public void addNpcIdForSkillId(int skillId, int npcId) {
         this._skillSuccessNpcIdList.put(skillId, npcId);
      }

      public int getGrowthLevel() {
         return this._growthLevel;
      }

      public int getLeveledNpcId(int skillId) {
         if (!this._skillSuccessNpcIdList.containsKey(skillId)) {
            return -1;
         } else if (skillId != 9051 && skillId != 9052 && skillId != 9053 && skillId != 9054) {
            if (this._growthLevel == 2 && Quest.getRandom(100) < 20) {
               return this._tameNpcId;
            } else {
               return Quest.getRandom(100) < this._chance ? this._skillSuccessNpcIdList.get(skillId) : -1;
            }
         } else if (Quest.getRandom(100) < BeastFarm.SPECIAL_SPICE_CHANCES[0]) {
            if (Quest.getRandom(100) < BeastFarm.SPECIAL_SPICE_CHANCES[1]) {
               return this._skillSuccessNpcIdList.get(skillId);
            } else {
               return skillId != 9051 && skillId != 9053 ? this._skillSuccessNpcIdList.get(9050) : this._skillSuccessNpcIdList.get(9049);
            }
         } else {
            return -1;
         }
      }
   }

   private static class TamedBeast {
      private final String name;
      private final SkillHolder[] sh;

      public TamedBeast(String beastName, SkillHolder... holders) {
         this.name = beastName;
         this.sh = holders;
      }

      public String getName() {
         return this.name;
      }

      public SkillHolder[] getSkills() {
         return this.sh;
      }
   }
}
