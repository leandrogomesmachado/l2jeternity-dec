package l2e.gameserver.model.base;

import l2e.gameserver.model.interfaces.IIdentifiable;
import l2e.gameserver.model.strings.server.ServerStorage;

public enum ClassId implements IIdentifiable {
   fighter(0, false, Race.Human, null),
   warrior(1, false, Race.Human, fighter),
   gladiator(2, false, Race.Human, warrior),
   warlord(3, false, Race.Human, warrior),
   knight(4, false, Race.Human, fighter),
   paladin(5, false, Race.Human, knight),
   darkAvenger(6, false, Race.Human, knight),
   rogue(7, false, Race.Human, fighter),
   treasureHunter(8, false, Race.Human, rogue),
   hawkeye(9, false, Race.Human, rogue),
   mage(10, true, Race.Human, null),
   wizard(11, true, Race.Human, mage),
   sorceror(12, true, Race.Human, wizard),
   necromancer(13, true, Race.Human, wizard),
   warlock(14, true, true, Race.Human, wizard),
   cleric(15, true, Race.Human, mage),
   bishop(16, true, Race.Human, cleric),
   prophet(17, true, Race.Human, cleric),
   elvenFighter(18, false, Race.Elf, null),
   elvenKnight(19, false, Race.Elf, elvenFighter),
   templeKnight(20, false, Race.Elf, elvenKnight),
   swordSinger(21, false, Race.Elf, elvenKnight),
   elvenScout(22, false, Race.Elf, elvenFighter),
   plainsWalker(23, false, Race.Elf, elvenScout),
   silverRanger(24, false, Race.Elf, elvenScout),
   elvenMage(25, true, Race.Elf, null),
   elvenWizard(26, true, Race.Elf, elvenMage),
   spellsinger(27, true, Race.Elf, elvenWizard),
   elementalSummoner(28, true, true, Race.Elf, elvenWizard),
   oracle(29, true, Race.Elf, elvenMage),
   elder(30, true, Race.Elf, oracle),
   darkFighter(31, false, Race.DarkElf, null),
   palusKnight(32, false, Race.DarkElf, darkFighter),
   shillienKnight(33, false, Race.DarkElf, palusKnight),
   bladedancer(34, false, Race.DarkElf, palusKnight),
   assassin(35, false, Race.DarkElf, darkFighter),
   abyssWalker(36, false, Race.DarkElf, assassin),
   phantomRanger(37, false, Race.DarkElf, assassin),
   darkMage(38, true, Race.DarkElf, null),
   darkWizard(39, true, Race.DarkElf, darkMage),
   spellhowler(40, true, Race.DarkElf, darkWizard),
   phantomSummoner(41, true, true, Race.DarkElf, darkWizard),
   shillienOracle(42, true, Race.DarkElf, darkMage),
   shillenElder(43, true, Race.DarkElf, shillienOracle),
   orcFighter(44, false, Race.Orc, null),
   orcRaider(45, false, Race.Orc, orcFighter),
   destroyer(46, false, Race.Orc, orcRaider),
   orcMonk(47, false, Race.Orc, orcFighter),
   tyrant(48, false, Race.Orc, orcMonk),
   orcMage(49, false, Race.Orc, null),
   orcShaman(50, true, Race.Orc, orcMage),
   overlord(51, true, Race.Orc, orcShaman),
   warcryer(52, true, Race.Orc, orcShaman),
   dwarvenFighter(53, false, Race.Dwarf, null),
   scavenger(54, false, Race.Dwarf, dwarvenFighter),
   bountyHunter(55, false, Race.Dwarf, scavenger),
   artisan(56, false, Race.Dwarf, dwarvenFighter),
   warsmith(57, false, Race.Dwarf, artisan),
   dummyEntry1(58, false, null, null),
   dummyEntry2(59, false, null, null),
   dummyEntry3(60, false, null, null),
   dummyEntry4(61, false, null, null),
   dummyEntry5(62, false, null, null),
   dummyEntry6(63, false, null, null),
   dummyEntry7(64, false, null, null),
   dummyEntry8(65, false, null, null),
   dummyEntry9(66, false, null, null),
   dummyEntry10(67, false, null, null),
   dummyEntry11(68, false, null, null),
   dummyEntry12(69, false, null, null),
   dummyEntry13(70, false, null, null),
   dummyEntry14(71, false, null, null),
   dummyEntry15(72, false, null, null),
   dummyEntry16(73, false, null, null),
   dummyEntry17(74, false, null, null),
   dummyEntry18(75, false, null, null),
   dummyEntry19(76, false, null, null),
   dummyEntry20(77, false, null, null),
   dummyEntry21(78, false, null, null),
   dummyEntry22(79, false, null, null),
   dummyEntry23(80, false, null, null),
   dummyEntry24(81, false, null, null),
   dummyEntry25(82, false, null, null),
   dummyEntry26(83, false, null, null),
   dummyEntry27(84, false, null, null),
   dummyEntry28(85, false, null, null),
   dummyEntry29(86, false, null, null),
   dummyEntry30(87, false, null, null),
   duelist(88, false, Race.Human, gladiator),
   dreadnought(89, false, Race.Human, warlord),
   phoenixKnight(90, false, Race.Human, paladin),
   hellKnight(91, false, Race.Human, darkAvenger),
   sagittarius(92, false, Race.Human, hawkeye),
   adventurer(93, false, Race.Human, treasureHunter),
   archmage(94, true, Race.Human, sorceror),
   soultaker(95, true, Race.Human, necromancer),
   arcanaLord(96, true, true, Race.Human, warlock),
   cardinal(97, true, Race.Human, bishop),
   hierophant(98, true, Race.Human, prophet),
   evaTemplar(99, false, Race.Elf, templeKnight),
   swordMuse(100, false, Race.Elf, swordSinger),
   windRider(101, false, Race.Elf, plainsWalker),
   moonlightSentinel(102, false, Race.Elf, silverRanger),
   mysticMuse(103, true, Race.Elf, spellsinger),
   elementalMaster(104, true, true, Race.Elf, elementalSummoner),
   evaSaint(105, true, Race.Elf, elder),
   shillienTemplar(106, false, Race.DarkElf, shillienKnight),
   spectralDancer(107, false, Race.DarkElf, bladedancer),
   ghostHunter(108, false, Race.DarkElf, abyssWalker),
   ghostSentinel(109, false, Race.DarkElf, phantomRanger),
   stormScreamer(110, true, Race.DarkElf, spellhowler),
   spectralMaster(111, true, true, Race.DarkElf, phantomSummoner),
   shillienSaint(112, true, Race.DarkElf, shillenElder),
   titan(113, false, Race.Orc, destroyer),
   grandKhavatari(114, false, Race.Orc, tyrant),
   dominator(115, true, Race.Orc, overlord),
   doomcryer(116, true, Race.Orc, warcryer),
   fortuneSeeker(117, false, Race.Dwarf, bountyHunter),
   maestro(118, false, Race.Dwarf, warsmith),
   dummyEntry31(119, false, null, null),
   dummyEntry32(120, false, null, null),
   dummyEntry33(121, false, null, null),
   dummyEntry34(122, false, null, null),
   maleSoldier(123, false, Race.Kamael, null),
   femaleSoldier(124, false, Race.Kamael, null),
   trooper(125, false, Race.Kamael, maleSoldier),
   warder(126, false, Race.Kamael, femaleSoldier),
   berserker(127, false, Race.Kamael, trooper),
   maleSoulbreaker(128, false, Race.Kamael, trooper),
   femaleSoulbreaker(129, false, Race.Kamael, warder),
   arbalester(130, false, Race.Kamael, warder),
   doombringer(131, false, Race.Kamael, berserker),
   maleSoulhound(132, false, Race.Kamael, maleSoulbreaker),
   femaleSoulhound(133, false, Race.Kamael, femaleSoulbreaker),
   trickster(134, false, Race.Kamael, arbalester),
   inspector(135, false, Race.Kamael, warder),
   judicator(136, false, Race.Kamael, inspector);

   private final int _id;
   private final boolean _isMage;
   private final boolean _isSummoner;
   private final Race _race;
   private final ClassId _parent;

   private ClassId(int pId, boolean pIsMage, Race pRace, ClassId pParent) {
      this._id = pId;
      this._isMage = pIsMage;
      this._isSummoner = false;
      this._race = pRace;
      this._parent = pParent;
   }

   private ClassId(int pId, boolean pIsMage, boolean pIsSummoner, Race pRace, ClassId pParent) {
      this._id = pId;
      this._isMage = pIsMage;
      this._isSummoner = pIsSummoner;
      this._race = pRace;
      this._parent = pParent;
   }

   @Override
   public final int getId() {
      return this._id;
   }

   public final String getName(String lang) {
      return ServerStorage.getInstance().getString(lang, "ClassName." + this._id);
   }

   public final boolean isMage() {
      return this._isMage;
   }

   public final boolean isSummoner() {
      return this._isSummoner;
   }

   public final Race getRace() {
      return this._race;
   }

   public final boolean childOf(ClassId cid) {
      if (this._parent == null) {
         return false;
      } else {
         return this._parent == cid ? true : this._parent.childOf(cid);
      }
   }

   public final boolean equalsOrChildOf(ClassId cid) {
      return this == cid || this.childOf(cid);
   }

   public final int level() {
      return this._parent == null ? 0 : 1 + this._parent.level();
   }

   public final ClassId getParent() {
      return this._parent;
   }

   public static ClassId getClassId(int cId) {
      try {
         return values()[cId];
      } catch (Exception var2) {
         return null;
      }
   }

   public static final ClassId getClassById(int classId) {
      try {
         for(ClassId id : values()) {
            if (id.getRace() != null && id.ordinal() == classId) {
               return id;
            }
         }
      } catch (Exception var5) {
         return null;
      }

      return values()[0];
   }
}
