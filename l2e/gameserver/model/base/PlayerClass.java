package l2e.gameserver.model.base;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;
import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Player;

public enum PlayerClass {
   HumanFighter(Race.Human, ClassType.Fighter, ClassLevel.First),
   Warrior(Race.Human, ClassType.Fighter, ClassLevel.Second),
   Gladiator(Race.Human, ClassType.Fighter, ClassLevel.Third),
   Warlord(Race.Human, ClassType.Fighter, ClassLevel.Third),
   HumanKnight(Race.Human, ClassType.Fighter, ClassLevel.Second),
   Paladin(Race.Human, ClassType.Fighter, ClassLevel.Third),
   DarkAvenger(Race.Human, ClassType.Fighter, ClassLevel.Third),
   Rogue(Race.Human, ClassType.Fighter, ClassLevel.Second),
   TreasureHunter(Race.Human, ClassType.Fighter, ClassLevel.Third),
   Hawkeye(Race.Human, ClassType.Fighter, ClassLevel.Third),
   HumanMystic(Race.Human, ClassType.Mystic, ClassLevel.First),
   HumanWizard(Race.Human, ClassType.Mystic, ClassLevel.Second),
   Sorceror(Race.Human, ClassType.Mystic, ClassLevel.Third),
   Necromancer(Race.Human, ClassType.Mystic, ClassLevel.Third),
   Warlock(Race.Human, ClassType.Mystic, ClassLevel.Third),
   Cleric(Race.Human, ClassType.Priest, ClassLevel.Second),
   Bishop(Race.Human, ClassType.Priest, ClassLevel.Third),
   Prophet(Race.Human, ClassType.Priest, ClassLevel.Third),
   ElvenFighter(Race.Elf, ClassType.Fighter, ClassLevel.First),
   ElvenKnight(Race.Elf, ClassType.Fighter, ClassLevel.Second),
   TempleKnight(Race.Elf, ClassType.Fighter, ClassLevel.Third),
   Swordsinger(Race.Elf, ClassType.Fighter, ClassLevel.Third),
   ElvenScout(Race.Elf, ClassType.Fighter, ClassLevel.Second),
   Plainswalker(Race.Elf, ClassType.Fighter, ClassLevel.Third),
   SilverRanger(Race.Elf, ClassType.Fighter, ClassLevel.Third),
   ElvenMystic(Race.Elf, ClassType.Mystic, ClassLevel.First),
   ElvenWizard(Race.Elf, ClassType.Mystic, ClassLevel.Second),
   Spellsinger(Race.Elf, ClassType.Mystic, ClassLevel.Third),
   ElementalSummoner(Race.Elf, ClassType.Mystic, ClassLevel.Third),
   ElvenOracle(Race.Elf, ClassType.Priest, ClassLevel.Second),
   ElvenElder(Race.Elf, ClassType.Priest, ClassLevel.Third),
   DarkElvenFighter(Race.DarkElf, ClassType.Fighter, ClassLevel.First),
   PalusKnight(Race.DarkElf, ClassType.Fighter, ClassLevel.Second),
   ShillienKnight(Race.DarkElf, ClassType.Fighter, ClassLevel.Third),
   Bladedancer(Race.DarkElf, ClassType.Fighter, ClassLevel.Third),
   Assassin(Race.DarkElf, ClassType.Fighter, ClassLevel.Second),
   AbyssWalker(Race.DarkElf, ClassType.Fighter, ClassLevel.Third),
   PhantomRanger(Race.DarkElf, ClassType.Fighter, ClassLevel.Third),
   DarkElvenMystic(Race.DarkElf, ClassType.Mystic, ClassLevel.First),
   DarkElvenWizard(Race.DarkElf, ClassType.Mystic, ClassLevel.Second),
   Spellhowler(Race.DarkElf, ClassType.Mystic, ClassLevel.Third),
   PhantomSummoner(Race.DarkElf, ClassType.Mystic, ClassLevel.Third),
   ShillienOracle(Race.DarkElf, ClassType.Priest, ClassLevel.Second),
   ShillienElder(Race.DarkElf, ClassType.Priest, ClassLevel.Third),
   OrcFighter(Race.Orc, ClassType.Fighter, ClassLevel.First),
   OrcRaider(Race.Orc, ClassType.Fighter, ClassLevel.Second),
   Destroyer(Race.Orc, ClassType.Fighter, ClassLevel.Third),
   OrcMonk(Race.Orc, ClassType.Fighter, ClassLevel.Second),
   Tyrant(Race.Orc, ClassType.Fighter, ClassLevel.Third),
   OrcMystic(Race.Orc, ClassType.Mystic, ClassLevel.First),
   OrcShaman(Race.Orc, ClassType.Mystic, ClassLevel.Second),
   Overlord(Race.Orc, ClassType.Mystic, ClassLevel.Third),
   Warcryer(Race.Orc, ClassType.Mystic, ClassLevel.Third),
   DwarvenFighter(Race.Dwarf, ClassType.Fighter, ClassLevel.First),
   DwarvenScavenger(Race.Dwarf, ClassType.Fighter, ClassLevel.Second),
   BountyHunter(Race.Dwarf, ClassType.Fighter, ClassLevel.Third),
   DwarvenArtisan(Race.Dwarf, ClassType.Fighter, ClassLevel.Second),
   Warsmith(Race.Dwarf, ClassType.Fighter, ClassLevel.Third),
   dummyEntry1(null, null, null),
   dummyEntry2(null, null, null),
   dummyEntry3(null, null, null),
   dummyEntry4(null, null, null),
   dummyEntry5(null, null, null),
   dummyEntry6(null, null, null),
   dummyEntry7(null, null, null),
   dummyEntry8(null, null, null),
   dummyEntry9(null, null, null),
   dummyEntry10(null, null, null),
   dummyEntry11(null, null, null),
   dummyEntry12(null, null, null),
   dummyEntry13(null, null, null),
   dummyEntry14(null, null, null),
   dummyEntry15(null, null, null),
   dummyEntry16(null, null, null),
   dummyEntry17(null, null, null),
   dummyEntry18(null, null, null),
   dummyEntry19(null, null, null),
   dummyEntry20(null, null, null),
   dummyEntry21(null, null, null),
   dummyEntry22(null, null, null),
   dummyEntry23(null, null, null),
   dummyEntry24(null, null, null),
   dummyEntry25(null, null, null),
   dummyEntry26(null, null, null),
   dummyEntry27(null, null, null),
   dummyEntry28(null, null, null),
   dummyEntry29(null, null, null),
   dummyEntry30(null, null, null),
   duelist(Race.Human, ClassType.Fighter, ClassLevel.Fourth),
   dreadnought(Race.Human, ClassType.Fighter, ClassLevel.Fourth),
   phoenixKnight(Race.Human, ClassType.Fighter, ClassLevel.Fourth),
   hellKnight(Race.Human, ClassType.Fighter, ClassLevel.Fourth),
   sagittarius(Race.Human, ClassType.Fighter, ClassLevel.Fourth),
   adventurer(Race.Human, ClassType.Fighter, ClassLevel.Fourth),
   archmage(Race.Human, ClassType.Mystic, ClassLevel.Fourth),
   soultaker(Race.Human, ClassType.Mystic, ClassLevel.Fourth),
   arcanaLord(Race.Human, ClassType.Mystic, ClassLevel.Fourth),
   cardinal(Race.Human, ClassType.Priest, ClassLevel.Fourth),
   hierophant(Race.Human, ClassType.Priest, ClassLevel.Fourth),
   evaTemplar(Race.Elf, ClassType.Fighter, ClassLevel.Fourth),
   swordMuse(Race.Elf, ClassType.Fighter, ClassLevel.Fourth),
   windRider(Race.Elf, ClassType.Fighter, ClassLevel.Fourth),
   moonlightSentinel(Race.Elf, ClassType.Fighter, ClassLevel.Fourth),
   mysticMuse(Race.Elf, ClassType.Mystic, ClassLevel.Fourth),
   elementalMaster(Race.Elf, ClassType.Mystic, ClassLevel.Fourth),
   evaSaint(Race.Elf, ClassType.Priest, ClassLevel.Fourth),
   shillienTemplar(Race.DarkElf, ClassType.Fighter, ClassLevel.Fourth),
   spectralDancer(Race.DarkElf, ClassType.Fighter, ClassLevel.Fourth),
   ghostHunter(Race.DarkElf, ClassType.Fighter, ClassLevel.Fourth),
   ghostSentinel(Race.DarkElf, ClassType.Fighter, ClassLevel.Fourth),
   stormScreamer(Race.DarkElf, ClassType.Mystic, ClassLevel.Fourth),
   spectralMaster(Race.DarkElf, ClassType.Mystic, ClassLevel.Fourth),
   shillienSaint(Race.DarkElf, ClassType.Priest, ClassLevel.Fourth),
   titan(Race.Orc, ClassType.Fighter, ClassLevel.Fourth),
   grandKhavatari(Race.Orc, ClassType.Fighter, ClassLevel.Fourth),
   dominator(Race.Orc, ClassType.Mystic, ClassLevel.Fourth),
   doomcryer(Race.Orc, ClassType.Mystic, ClassLevel.Fourth),
   fortuneSeeker(Race.Dwarf, ClassType.Fighter, ClassLevel.Fourth),
   maestro(Race.Dwarf, ClassType.Fighter, ClassLevel.Fourth),
   dummyEntry31(null, null, null),
   dummyEntry32(null, null, null),
   dummyEntry33(null, null, null),
   dummyEntry34(null, null, null),
   maleSoldier(Race.Kamael, ClassType.Fighter, ClassLevel.First),
   femaleSoldier(Race.Kamael, ClassType.Fighter, ClassLevel.First),
   trooper(Race.Kamael, ClassType.Fighter, ClassLevel.Second),
   warder(Race.Kamael, ClassType.Fighter, ClassLevel.Second),
   berserker(Race.Kamael, ClassType.Fighter, ClassLevel.Third),
   maleSoulbreaker(Race.Kamael, ClassType.Fighter, ClassLevel.Third),
   femaleSoulbreaker(Race.Kamael, ClassType.Fighter, ClassLevel.Third),
   arbalester(Race.Kamael, ClassType.Fighter, ClassLevel.Third),
   doombringer(Race.Kamael, ClassType.Fighter, ClassLevel.Fourth),
   maleSoulhound(Race.Kamael, ClassType.Fighter, ClassLevel.Fourth),
   femaleSoulhound(Race.Kamael, ClassType.Fighter, ClassLevel.Fourth),
   trickster(Race.Kamael, ClassType.Fighter, ClassLevel.Fourth),
   inspector(Race.Kamael, ClassType.Fighter, ClassLevel.Third),
   judicator(Race.Kamael, ClassType.Fighter, ClassLevel.Fourth);

   private Race _race;
   private ClassLevel _level;
   private ClassType _type;
   private static final Set<PlayerClass> mainSubclassSet;
   private static final Set<PlayerClass> neverSubclassed = EnumSet.of(Overlord, Warsmith);
   protected static final Set<PlayerClass> neverKamaelSubclassed = EnumSet.of(berserker, maleSoulbreaker, femaleSoulbreaker, arbalester, inspector);
   private static final Set<PlayerClass> subclasseSet1 = EnumSet.of(DarkAvenger, Paladin, TempleKnight, ShillienKnight);
   private static final Set<PlayerClass> subclasseSet2 = EnumSet.of(TreasureHunter, AbyssWalker, Plainswalker);
   private static final Set<PlayerClass> subclasseSet3 = EnumSet.of(Hawkeye, SilverRanger, PhantomRanger);
   private static final Set<PlayerClass> subclasseSet4 = EnumSet.of(Warlock, ElementalSummoner, PhantomSummoner);
   private static final Set<PlayerClass> subclasseSet5 = EnumSet.of(Sorceror, Spellsinger, Spellhowler);
   private static final EnumMap<PlayerClass, Set<PlayerClass>> subclassSetMap = new EnumMap<>(PlayerClass.class);

   private PlayerClass(Race pRace, ClassType pType, ClassLevel pLevel) {
      this._race = pRace;
      this._level = pLevel;
      this._type = pType;
   }

   public final Set<PlayerClass> getAvailableSubclasses(Player player) {
      Set<PlayerClass> subclasses = null;
      if (this._level == ClassLevel.Third) {
         if (Config.ALT_GAME_SUBCLASS_ALL_CLASSES) {
            subclasses = EnumSet.copyOf(mainSubclassSet);
            subclasses.addAll(neverSubclassed);
            subclasses.remove(this);
         } else if (player.getRace() != Race.Kamael) {
            subclasses = EnumSet.copyOf(mainSubclassSet);
            subclasses.remove(this);
            switch(player.getRace()) {
               case Elf:
                  subclasses.removeAll(getSet(Race.DarkElf, ClassLevel.Third));
                  break;
               case DarkElf:
                  subclasses.removeAll(getSet(Race.Elf, ClassLevel.Third));
            }

            subclasses.removeAll(getSet(Race.Kamael, ClassLevel.Third));
            Set<PlayerClass> unavailableClasses = subclassSetMap.get(this);
            if (unavailableClasses != null) {
               subclasses.removeAll(unavailableClasses);
            }
         } else {
            subclasses = getSet(Race.Kamael, ClassLevel.Third);
            subclasses.remove(this);
            if (Config.MAX_SUBCLASS <= 3) {
               if (!player.getAppearance().getSex()) {
                  subclasses.removeAll(EnumSet.of(femaleSoulbreaker));
               } else {
                  subclasses.removeAll(EnumSet.of(maleSoulbreaker));
               }
            }

            if (!player.getSubClasses().containsKey(2) || player.getSubClasses().get(2).getLevel() < 75) {
               subclasses.removeAll(EnumSet.of(inspector));
            }
         }
      }

      return subclasses;
   }

   public static final EnumSet<PlayerClass> getSet(Race race, ClassLevel level) {
      EnumSet<PlayerClass> allOf = EnumSet.noneOf(PlayerClass.class);

      for(PlayerClass playerClass : EnumSet.allOf(PlayerClass.class)) {
         if ((race == null || playerClass.isOfRace(race)) && (level == null || playerClass.isOfLevel(level))) {
            allOf.add(playerClass);
         }
      }

      return allOf;
   }

   public final boolean isOfRace(Race pRace) {
      return this._race == pRace;
   }

   public final boolean isOfType(ClassType pType) {
      return this._type == pType;
   }

   public final boolean isOfLevel(ClassLevel pLevel) {
      return this._level == pLevel;
   }

   public final ClassLevel getLevel() {
      return this._level;
   }

   static {
      Set<PlayerClass> subclasses = getSet(null, ClassLevel.Third);
      subclasses.removeAll(neverSubclassed);
      mainSubclassSet = subclasses;
      subclassSetMap.put(DarkAvenger, subclasseSet1);
      subclassSetMap.put(Paladin, subclasseSet1);
      subclassSetMap.put(TempleKnight, subclasseSet1);
      subclassSetMap.put(ShillienKnight, subclasseSet1);
      subclassSetMap.put(TreasureHunter, subclasseSet2);
      subclassSetMap.put(AbyssWalker, subclasseSet2);
      subclassSetMap.put(Plainswalker, subclasseSet2);
      subclassSetMap.put(Hawkeye, subclasseSet3);
      subclassSetMap.put(SilverRanger, subclasseSet3);
      subclassSetMap.put(PhantomRanger, subclasseSet3);
      subclassSetMap.put(Warlock, subclasseSet4);
      subclassSetMap.put(ElementalSummoner, subclasseSet4);
      subclassSetMap.put(PhantomSummoner, subclasseSet4);
      subclassSetMap.put(Sorceror, subclasseSet5);
      subclassSetMap.put(Spellsinger, subclasseSet5);
      subclassSetMap.put(Spellhowler, subclasseSet5);
   }
}
