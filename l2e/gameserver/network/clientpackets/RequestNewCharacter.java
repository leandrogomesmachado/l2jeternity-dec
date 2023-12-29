package l2e.gameserver.network.clientpackets;

import l2e.gameserver.data.parser.CharTemplateParser;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.network.serverpackets.NewCharacterSuccess;

public final class RequestNewCharacter extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      NewCharacterSuccess ct = new NewCharacterSuccess();
      ct.addChar(CharTemplateParser.getInstance().getTemplate(ClassId.fighter));
      ct.addChar(CharTemplateParser.getInstance().getTemplate(ClassId.mage));
      ct.addChar(CharTemplateParser.getInstance().getTemplate(ClassId.elvenFighter));
      ct.addChar(CharTemplateParser.getInstance().getTemplate(ClassId.elvenMage));
      ct.addChar(CharTemplateParser.getInstance().getTemplate(ClassId.darkFighter));
      ct.addChar(CharTemplateParser.getInstance().getTemplate(ClassId.darkMage));
      ct.addChar(CharTemplateParser.getInstance().getTemplate(ClassId.orcFighter));
      ct.addChar(CharTemplateParser.getInstance().getTemplate(ClassId.orcMage));
      ct.addChar(CharTemplateParser.getInstance().getTemplate(ClassId.dwarvenFighter));
      ct.addChar(CharTemplateParser.getInstance().getTemplate(ClassId.maleSoldier));
      ct.addChar(CharTemplateParser.getInstance().getTemplate(ClassId.femaleSoldier));
      this.sendPacket(ct);
   }
}
