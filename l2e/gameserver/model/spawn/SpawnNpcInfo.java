package l2e.gameserver.model.spawn;

import l2e.commons.collections.MultiValueSet;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;

public class SpawnNpcInfo {
   private final NpcTemplate _template;
   private final int _max;
   private final MultiValueSet<String> _parameters;
   private final int _npcId;

   public SpawnNpcInfo(int npcId, int max, MultiValueSet<String> set) {
      this._template = NpcsParser.getInstance().getTemplate(npcId);
      this._max = max;
      this._parameters = set;
      this._npcId = npcId;
   }

   public NpcTemplate getTemplate() {
      return this._template;
   }

   public int getId() {
      return this._npcId;
   }

   public int getMax() {
      return this._max;
   }

   public MultiValueSet<String> getParameters() {
      return this._parameters;
   }
}
