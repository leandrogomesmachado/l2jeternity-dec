package l2e.gameserver.model;

import java.util.List;
import l2e.gameserver.model.actor.templates.NpcWalkerTemplate;

public class WalkRoute {
   private final String _name;
   private final List<NpcWalkerTemplate> _nodeList;
   private final boolean _repeatWalk;
   private boolean _stopAfterCycle;
   private final byte _repeatType;

   public WalkRoute(String name, List<NpcWalkerTemplate> route, boolean repeat, boolean once, byte repeatType) {
      this._name = name;
      this._nodeList = route;
      this._repeatType = repeatType;
      this._repeatWalk = this._repeatType >= 0 && this._repeatType <= 2 ? repeat : false;
   }

   public String getName() {
      return this._name;
   }

   public List<NpcWalkerTemplate> getNodeList() {
      return this._nodeList;
   }

   public NpcWalkerTemplate getLastNode() {
      return this._nodeList.get(this._nodeList.size() - 1);
   }

   public boolean repeatWalk() {
      return this._repeatWalk;
   }

   public boolean doOnce() {
      return this._stopAfterCycle;
   }

   public byte getRepeatType() {
      return this._repeatType;
   }

   public int getNodesCount() {
      return this._nodeList.size();
   }
}
