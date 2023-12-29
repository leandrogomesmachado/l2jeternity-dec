package l2e.gameserver.data.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.instancemanager.MapRegionManager;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.actor.ColosseumFence;
import l2e.gameserver.model.actor.Player;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class ColosseumFenceParser extends DocumentParser {
   private final Map<Integer, List<ColosseumFence>> _fence = new HashMap<>();

   protected ColosseumFenceParser() {
      this.load();
   }

   @Override
   public void load() {
      this._fence.clear();
      this.parseDatapackFile("data/stats/admin/colosseum_fences.xml");
      this._log.info(this.getClass().getSimpleName() + ": Loaded " + this._fence.size() + " colosseum fences.");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node n = this.getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equalsIgnoreCase(n.getNodeName())) {
            for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
               if ("colosseum_fence".equalsIgnoreCase(d.getNodeName())) {
                  NamedNodeMap fence = d.getAttributes();
                  int x = Integer.parseInt(fence.getNamedItem("x").getNodeValue());
                  int y = Integer.parseInt(fence.getNamedItem("y").getNodeValue());
                  int z = Integer.parseInt(fence.getNamedItem("z").getNodeValue());
                  int minZ = Integer.parseInt(fence.getNamedItem("minZ").getNodeValue());
                  int maxZ = Integer.parseInt(fence.getNamedItem("maxZ").getNodeValue());
                  int width = Integer.parseInt(fence.getNamedItem("width").getNodeValue());
                  int height = Integer.parseInt(fence.getNamedItem("height").getNodeValue());
                  int type = Integer.parseInt(fence.getNamedItem("type").getNodeValue());
                  ColosseumFence.FenceState fenceState = ColosseumFence.FenceState.HIDDEN;
                  if (type == 1) {
                     fenceState = ColosseumFence.FenceState.OPEN;
                  } else if (type == 2) {
                     fenceState = ColosseumFence.FenceState.CLOSED;
                  }

                  ColosseumFence fenceInstance = new ColosseumFence(0, x, y, z, minZ, maxZ, width, height, fenceState);
                  Integer region = MapRegionManager.getInstance().getMapRegionLocId(fenceInstance);
                  if (!this._fence.containsKey(region)) {
                     this._fence.put(region, new ArrayList<>());
                  }

                  this._fence.get(region).add(fenceInstance);
                  fenceInstance.spawnMe();
               }
            }
         }
      }
   }

   public ColosseumFence addDynamic(Player player, int x, int y, int z, int minZ, int maxZ, int width, int height, int state) {
      ColosseumFence.FenceState fenceState = ColosseumFence.FenceState.HIDDEN;
      if (state == 1) {
         fenceState = ColosseumFence.FenceState.OPEN;
      } else if (state == 2) {
         fenceState = ColosseumFence.FenceState.CLOSED;
      }

      ColosseumFence fence = new ColosseumFence(player.getReflectionId(), x, y, z, minZ, maxZ, width, height, fenceState);
      Integer region = MapRegionManager.getInstance().getMapRegionLocId(fence);
      if (!this._fence.containsKey(region)) {
         this._fence.put(region, new ArrayList<>());
      }

      this._fence.get(region).add(fence);
      fence.spawnMe();
      return fence;
   }

   public boolean checkIfFencesBetween(int x, int y, int z, int tx, int ty, int tz, int instanceId) {
      Collection<ColosseumFence> allFences;
      if (instanceId > 0 && ReflectionManager.getInstance().getReflection(instanceId) != null) {
         allFences = ReflectionManager.getInstance().getReflection(instanceId).getFences();
      } else {
         int mapRegion = MapRegionManager.getInstance().getMapRegionLocId(x, y);
         allFences = new ArrayList<>();
         if (this._fence.containsKey(mapRegion)) {
            allFences.addAll(this._fence.get(mapRegion));
         }
      }

      for(ColosseumFence fence : allFences) {
         if (fence.getFenceState() == ColosseumFence.FenceState.CLOSED && fence.isInsideFence(x, y, z) != fence.isInsideFence(tx, ty, tz)) {
            return true;
         }
      }

      return false;
   }

   public final void removeFence(ColosseumFence fence, int region) {
      fence.decayMe();
      this._fence.get(region).remove(fence);
   }

   public Map<Integer, List<ColosseumFence>> getFences() {
      return this._fence;
   }

   public static ColosseumFenceParser getInstance() {
      return ColosseumFenceParser.SingletonHolder.INSTANCE;
   }

   private static final class SingletonHolder {
      protected static final ColosseumFenceParser INSTANCE = new ColosseumFenceParser();
   }
}
