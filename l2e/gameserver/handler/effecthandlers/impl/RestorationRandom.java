package l2e.gameserver.handler.effecthandlers.impl;

import java.util.ArrayList;
import java.util.List;
import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.ExtractableProductItemTemplate;
import l2e.gameserver.model.actor.templates.ExtractableSkillTemplate;
import l2e.gameserver.model.holders.ItemHolder;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.SystemMessageId;

public class RestorationRandom extends Effect {
   public RestorationRandom(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public boolean onStart() {
      if (this.getEffector() != null && this.getEffected() != null && this.getEffector().isPlayer() && this.getEffected().isPlayer()) {
         ExtractableSkillTemplate exSkill = this.getSkill().getExtractableSkill();
         if (exSkill == null) {
            return false;
         } else if (exSkill.getProductItems().isEmpty()) {
            _log.warning("Extractable Skill with no data, probably wrong/empty table in Skill Id: " + this.getSkill().getId());
            return false;
         } else {
            double rndNum = 100.0 * Rnd.nextDouble();
            double chance = 0.0;
            double chanceFrom = 0.0;
            List<ItemHolder> creationList = new ArrayList<>();

            for(ExtractableProductItemTemplate expi : exSkill.getProductItems()) {
               chance = expi.getChance();
               if (rndNum >= chanceFrom && rndNum <= chance + chanceFrom) {
                  creationList.addAll(expi.getItems());
                  break;
               }

               chanceFrom += chance;
            }

            Player player = this.getEffected().getActingPlayer();
            if (creationList.isEmpty()) {
               player.sendPacket(SystemMessageId.NOTHING_INSIDE_THAT);
               return false;
            } else {
               for(ItemHolder item : creationList) {
                  if (item.getId() > 0 && item.getCount() > 0L) {
                     player.addItem("Extract", item.getId(), (long)((double)item.getCount() * Config.RATE_EXTRACTABLE), this.getEffector(), true);
                  }
               }

               return true;
            }
         }
      } else {
         return false;
      }
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.RESTORATION_RANDOM;
   }
}
