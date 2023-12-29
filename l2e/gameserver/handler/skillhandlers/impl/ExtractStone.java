package l2e.gameserver.handler.skillhandlers.impl;

import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.handler.skillhandlers.ISkillHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.SkillType;
import l2e.gameserver.network.SystemMessageId;

public class ExtractStone implements ISkillHandler {
   private static final int EXTRACT_SCROLL_SKILL = 2630;
   private static final int EXTRACTED_COARSE_RED_STAR_STONE = 13858;
   private static final int EXTRACTED_COARSE_BLUE_STAR_STONE = 13859;
   private static final int EXTRACTED_COARSE_GREEN_STAR_STONE = 13860;
   private static final int EXTRACTED_RED_STAR_STONE = 14009;
   private static final int EXTRACTED_BLUE_STAR_STONE = 14010;
   private static final int EXTRACTED_GREEN_STAR_STONE = 14011;
   private static final int RED_STAR_STONE1 = 18684;
   private static final int RED_STAR_STONE2 = 18685;
   private static final int RED_STAR_STONE3 = 18686;
   private static final int BLUE_STAR_STONE1 = 18687;
   private static final int BLUE_STAR_STONE2 = 18688;
   private static final int BLUE_STAR_STONE3 = 18689;
   private static final int GREEN_STAR_STONE1 = 18690;
   private static final int GREEN_STAR_STONE2 = 18691;
   private static final int GREEN_STAR_STONE3 = 18692;
   private static final int FIRE_ENERGY_COMPRESSION_STONE = 14015;
   private static final int WATER_ENERGY_COMPRESSION_STONE = 14016;
   private static final int WIND_ENERGY_COMPRESSION_STONE = 14017;
   private static final int EARTH_ENERGY_COMPRESSION_STONE = 14018;
   private static final int DARKNESS_ENERGY_COMPRESSION_STONE = 14019;
   private static final int SACRED_ENERGY_COMPRESSION_STONE = 14020;
   private static final int SEED_FIRE = 18679;
   private static final int SEED_WATER = 18678;
   private static final int SEED_WIND = 18680;
   private static final int SEED_EARTH = 18681;
   private static final int SEED_DARKNESS = 18683;
   private static final int SEED_DIVINITY = 18682;
   private static final SkillType[] SKILL_IDS = new SkillType[]{SkillType.EXTRACT_STONE};

   private int getItemId(int npcId, int skillId) {
      switch(npcId) {
         case 18678:
            return 14016;
         case 18679:
            return 14015;
         case 18680:
            return 14017;
         case 18681:
            return 14018;
         case 18682:
            return 14020;
         case 18683:
            return 14019;
         case 18684:
         case 18685:
         case 18686:
            if (skillId == 2630) {
               return 13858;
            }

            return 14009;
         case 18687:
         case 18688:
         case 18689:
            if (skillId == 2630) {
               return 13859;
            }

            return 14010;
         case 18690:
         case 18691:
         case 18692:
            if (skillId == 2630) {
               return 13860;
            }

            return 14011;
         default:
            return 0;
      }
   }

   @Override
   public void useSkill(Creature activeChar, Skill skill, GameObject[] targets) {
      Player player = activeChar.getActingPlayer();
      if (player != null) {
         for(Creature target : (Creature[])targets) {
            if (target != null && this.getItemId(target.getId(), skill.getId()) != 0) {
               double rate = (double)Config.RATE_QUEST_DROP
                  * (player.isInParty() && Config.PREMIUM_PARTY_RATE ? player.getParty().getQuestRewardRate() : player.getPremiumBonus().getQuestRewardRate());
               long count = skill.getId() == 2630 ? 1L : (long)Math.min(10, Rnd.get((int)((double)skill.getLevel() * rate + 1.0)));
               int itemId = this.getItemId(target.getId(), skill.getId());
               if (count > 0L) {
                  player.addItem("StarStone", itemId, count, null, true);
                  player.sendPacket(SystemMessageId.THE_COLLECTION_HAS_SUCCEEDED);
               } else {
                  player.sendPacket(SystemMessageId.THE_COLLECTION_HAS_FAILED);
               }

               target.doDie(player);
            }
         }
      }
   }

   @Override
   public SkillType[] getSkillIds() {
      return SKILL_IDS;
   }
}
