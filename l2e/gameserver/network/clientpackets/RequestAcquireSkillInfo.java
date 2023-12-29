package l2e.gameserver.network.clientpackets;

import l2e.gameserver.data.parser.SkillTreesParser;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.SkillLearn;
import l2e.gameserver.model.SquadTrainer;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.NpcInstance;
import l2e.gameserver.model.base.AcquireSkillType;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.network.serverpackets.AcquireSkillInfo;

public final class RequestAcquireSkillInfo extends GameClientPacket {
   private int _id;
   private int _level;
   private AcquireSkillType _skillType;

   @Override
   protected void readImpl() {
      this._id = this.readD();
      this._level = this.readD();
      this._skillType = AcquireSkillType.getAcquireSkillType(this.readD());
   }

   @Override
   protected void runImpl() {
      if (this._id > 0 && this._level > 0) {
         Player activeChar = this.getClient().getActiveChar();
         if (activeChar != null) {
            Npc trainer = activeChar.getLastFolkNPC();
            if (trainer instanceof NpcInstance) {
               if (trainer.canInteract(activeChar) || activeChar.isGM()) {
                  Skill skill = SkillsParser.getInstance().getInfo(this._id, this._level);
                  if (skill == null) {
                     _log.warning(
                        RequestAcquireSkillInfo.class.getSimpleName()
                           + ": Skill Id: "
                           + this._id
                           + " level: "
                           + this._level
                           + " is undefined. "
                           + RequestAcquireSkillInfo.class.getName()
                           + " failed."
                     );
                  } else {
                     int prevSkillLevel = activeChar.getSkillLevel(this._id);
                     if (prevSkillLevel > 0 && this._skillType != AcquireSkillType.TRANSFER && this._skillType != AcquireSkillType.SUBPLEDGE) {
                        if (prevSkillLevel == this._level) {
                           _log.warning(
                              RequestAcquireSkillInfo.class.getSimpleName()
                                 + ": Player "
                                 + activeChar.getName()
                                 + " is trequesting info for a skill that already knows, Id: "
                                 + this._id
                                 + " level: "
                                 + this._level
                                 + "!"
                           );
                        } else if (prevSkillLevel != this._level - 1) {
                           _log.warning(
                              RequestAcquireSkillInfo.class.getSimpleName()
                                 + ": Player "
                                 + activeChar.getName()
                                 + " is requesting info for skill Id: "
                                 + this._id
                                 + " level "
                                 + this._level
                                 + " without knowing it's previous level!"
                           );
                        }
                     }

                     SkillLearn s = SkillTreesParser.getInstance().getSkillLearn(this._skillType, this._id, this._level, activeChar);
                     if (s != null) {
                        switch(this._skillType) {
                           case TRANSFORM:
                           case FISHING:
                           case SUBCLASS:
                           case COLLECT:
                           case TRANSFER:
                              this.sendPacket(new AcquireSkillInfo(this._skillType, s));
                              break;
                           case CLASS:
                              if (trainer.getTemplate().canTeach(activeChar.getLearningClass())) {
                                 int customSp = s.getCalculatedLevelUpSp(activeChar.getClassId(), activeChar.getLearningClass());
                                 this.sendPacket(new AcquireSkillInfo(this._skillType, s, customSp));
                              }
                              break;
                           case PLEDGE:
                              if (!activeChar.isClanLeader()) {
                                 return;
                              }

                              this.sendPacket(new AcquireSkillInfo(this._skillType, s));
                              break;
                           case SUBPLEDGE:
                              if (!activeChar.isClanLeader() || !(trainer instanceof SquadTrainer)) {
                                 return;
                              }

                              this.sendPacket(new AcquireSkillInfo(this._skillType, s));
                        }
                     }
                  }
               }
            }
         }
      } else {
         _log.warning(RequestAcquireSkillInfo.class.getSimpleName() + ": Invalid Id: " + this._id + " or level: " + this._level + "!");
      }
   }
}
