package l2e.fake.ai;

import java.util.List;
import l2e.fake.FakePlayer;
import l2e.fake.model.HealingSpell;
import l2e.fake.model.OffensiveSpell;
import l2e.fake.model.SupportSpell;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.FakeSkillsParser;

public class TitanAI extends CombatAI {
   public TitanAI(FakePlayer character) {
      super(character);
   }

   @Override
   public void thinkAndAct() {
      super.thinkAndAct();
      this.setBusyThinking(true);
      this.handleShots();
      this.selfSupportBuffs();
      this.tryAction(false);
      this.setBusyThinking(false);
   }

   @Override
   protected int changeOfUsingSkill() {
      return FakeSkillsParser.getInstance().getSkillsChance(this._fakePlayer.getClassId());
   }

   @Override
   protected List<OffensiveSpell> getOffensiveSpells() {
      return FakeSkillsParser.getInstance().getOffensiveSkills(this._fakePlayer.getClassId());
   }

   @Override
   protected List<HealingSpell> getHealingSpells() {
      return FakeSkillsParser.getInstance().getHealSkills(this._fakePlayer.getClassId());
   }

   @Override
   protected List<SupportSpell> getSelfSupportSpells() {
      return FakeSkillsParser.getInstance().getSupportSkills(this._fakePlayer.getClassId());
   }

   @Override
   protected int[][] getBuffs() {
      return Config.FAKE_FIGHTER_BUFFS;
   }
}
