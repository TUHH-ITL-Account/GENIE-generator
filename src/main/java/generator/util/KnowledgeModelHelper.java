package generator.util;

import fdl.fdl._symboltable.CharacteristicsSymbol;
import fdl.fdl._symboltable.ExamplesSymbol;
import fdl.fdl._symboltable.ICommonFdlSymbol;
import generator.caching.FdlNode;
import generator.caching.TopicNode;
import generator.types.FdlDslSymbolHelper.FdlTypes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import java.util.function.Predicate;

public class KnowledgeModelHelper {

  /**
   * Returns a fdl of specified type which is startDistance topic nodes away from the given origin,
   * or less if no suitable FdlNodes were found, or more if no suitable FdlNodes within
   * 0:startDistance
   *
   * @param origin         The starting node
   * @param type           The to-be-searched Fdl type
   * @param startDistance  The distance between the start node/its TopicNode and the potential other
   *                       TopicNode
   * @param randomInstance a Random object, passing null will disable randomizations
   * @return A FdlNode or null if none were found
   */
  public static FdlNode findOtherFdlTo(FdlNode origin, FdlTypes type, int startDistance,
      Random randomInstance) {
    if (startDistance < 0) {
      startDistance = 0;
    }

    // optimisation potential by writing separate function to get first TopicNode containing a FdlType'd fdl
    Map<Integer, List<TopicNode>> distanceMap = getDistantTopicsDownFrom(origin.getTopicNode(),
        startDistance);
    for (int i = startDistance; i >= 0; i--) {
      if (!distanceMap.containsKey(i)) {
        continue;
      }
      if (randomInstance != null) {
        Collections.shuffle(distanceMap.get(i), randomInstance);
      }
      for (TopicNode tNode : distanceMap.get(i)) {
        for (FdlNode fNode : tNode.getFdls()) {
          if (fNode.getFdlType().equals(type) && !fNode.equals(origin)) {
            return fNode;
          }
        }
      }
    }
    // if no fitting fdl was found, search progressively
    // optimization potential : use separate method to skip fdl checks in the first startDist nodes
    return getFirstOtherFdlUpTo(origin, type, -1, randomInstance);
  }

  public static FdlNode findOtherFdlTo(FdlNode origin, FdlTypes type, int startDistance,
      Predicate<ICommonFdlSymbol> predicate, Random randomInstance) {
    if (startDistance < 0) {
      startDistance = 0;
    }

    // optimisation potential by writing separate function to get first TopicNode containing a FdlType'd fdl
    Map<Integer, List<TopicNode>> distanceMap = getDistantTopicsDownFrom(origin.getTopicNode(),
        startDistance);
    for (int i = startDistance; i >= 0; i--) {
      if (!distanceMap.containsKey(i)) {
        continue;
      }
      if (randomInstance != null) {
        Collections.shuffle(distanceMap.get(i), randomInstance);
      }
      for (TopicNode tNode : distanceMap.get(i)) {
        for (FdlNode fNode : tNode.getFdls()) {
          if (fNode.getFdlType().equals(type) && !fNode.equals(origin) && predicate.test(
              fNode.getSymbol())) {
            return fNode;
          }
        }
      }
    }
    // if no fitting fdl was found, search progressively
    // optimization potential : use separate method to skip fdl checks in the first startDist nodes
    return getFirstOtherFdlUpTo(origin, type, -1, predicate, randomInstance);
  }

  public static List<FdlNode> findOtherFdlsTo(FdlNode origin, FdlTypes type, int startDistance,
      int endDistance, List<FdlNode> collectedNodes, Predicate<ICommonFdlSymbol> predicate) {
    if (collectedNodes == null) {
      collectedNodes = new ArrayList<>();
    }
    if (startDistance < 0) {
      startDistance = 0;
    }
    if (startDistance > endDistance) {
      int temp = startDistance;
      startDistance = endDistance;
      endDistance = temp;
    }

    Map<Integer, List<TopicNode>> distanceMap = getDistantTopicsDownFrom(origin.getTopicNode(),
        endDistance);
    for (int i = startDistance; i <= endDistance; i++) {
      if (!distanceMap.containsKey(i)) {
        continue;
      }
      for (TopicNode tNode : distanceMap.get(i)) {
        for (FdlNode fNode : tNode.getFdls()) {
          if (predicate != null) {
            if (fNode.getFdlType().equals(type) && !fNode.equals(origin) && predicate.test(
                fNode.getSymbol())) {
              collectedNodes.add(fNode);
            }
          } else {
            if (fNode.getFdlType().equals(type) && !fNode.equals(origin)) {
              collectedNodes.add(fNode);
            }
          }
        }
      }
    }
    return collectedNodes;
  }

  /**
   * Searches the knowledge model starting from the origin node for same symbol FDLs and creates a
   * map based on number of matching categories.
   *
   * @param origin        the original node
   * @param type          FDL types to look through
   * @param startDistance start distance, min 0
   * @param endDistance   max distance to search
   * @param predicate     optional predicate used on FdlNodes
   * @return map of number of matches per FdlNode list
   */
  public static Map<Integer, List<FdlNode>> findOtherCategorizedFdlsTo(FdlNode origin,
      FdlTypes type, int startDistance,
      int endDistance, Predicate<ICommonFdlSymbol> predicate) {
    Map<Integer, List<FdlNode>> ret = new HashMap<>();
    List<String> categories = getCategoriesFromSymbol(origin.getSymbol());
    if (categories == null) {
      return ret;
    }

    if (startDistance < 0) {
      startDistance = 0;
    }

    Map<Integer, List<TopicNode>> distanceMap = getDistantTopicsDownFrom(origin.getTopicNode(),
        endDistance);
    for (int i = startDistance; i < endDistance; i++) {
      if (!distanceMap.containsKey(i)) {
        continue;
      }
      for (TopicNode tNode : distanceMap.get(i)) {
        for (FdlNode fNode : tNode.getFdls()) {
          if (fNode.getFdlType().equals(type) && !fNode.equals(origin) && (predicate == null
              || predicate.test(fNode.getSymbol()))) {
            int matches = 0;
            List<String> otherCat = getCategoriesFromSymbol(fNode.getSymbol());
            for (String cat : categories) {
              if (otherCat.contains(cat)) {
                matches++;
              }
            }
            List<FdlNode> entry = ret.get(matches);
            if (entry == null) {
              entry = new ArrayList<>();
              entry.add(fNode);
              ret.put(matches, entry);
            } else {
              entry.add(fNode);
            }
          }
        }
      }
    }
    return ret;
  }

  public static List<String> getCategoriesFromSymbol(ICommonFdlSymbol sym) {
    if (sym instanceof CharacteristicsSymbol) {
      return ((CharacteristicsSymbol) sym).getCategories();
    } else if (sym instanceof ExamplesSymbol) {
      return ((ExamplesSymbol) sym).getCategories();
    } else {
      return null;
    }
  }

  /**
   * Returns a map listing lists of nodes to their distance from a given starting node.
   *
   * @param origin      The start node / distance 0 node.
   * @param maxDistance Maximum node-distance to be collected.
   * @return a Map<Int, List<TopicNode>>
   */
  public static Map<Integer, List<TopicNode>> getDistantTopicsDownFrom(TopicNode origin,
      int maxDistance) {
    Map<Integer, List<TopicNode>> map = new HashMap<>();
    map.put(0, new ArrayList<>());
    map.get(0).add(origin);

    if (maxDistance > 0) {
      List<TopicNode> visited = new ArrayList<>();
      Stack<TopicNode> nextNodes = new Stack<>();
      visited.add(origin);
      if (origin.getParent() != null) {
        nextNodes.push(origin.getParent());
      }
      for (TopicNode child : origin.getChildren()) {
        nextNodes.push(child);
      }

      int depth = 0;
      while (depth < maxDistance && !nextNodes.empty()) {
        map.put(depth + 1, new ArrayList<>());
        Stack<TopicNode> nextNext = new Stack<>();
        while (!nextNodes.empty()) {
          TopicNode next = nextNodes.pop();
          if (!visited.contains(next)) {
            visited.add(next);
            map.get(depth + 1).add(next);
            if (next.getParent() != null) {
              nextNext.push(next.getParent());
            }
            for (TopicNode child : next.getChildren()) {
              nextNext.push(child);
            }
          }
        }
        nextNodes = nextNext;
        depth++;
      }
    }
    return map;
  }

  /**
   * Returns the first found, different FdlNode of the given type, starting from a start-FdlNode, by
   * walking the parent-children relation of TopicNodes.
   *
   * @param fOrigin        The start node
   * @param type           The desired type of the FdlNode
   * @param maxDistance    The max node distance to check. Negative numbers result in no limit.
   * @param randomInstance A Random object to shuffle visiting orders. Disabled on being null.
   * @return a FdlNode or null if none was found.
   */
  public static FdlNode getFirstOtherFdlUpTo(FdlNode fOrigin, FdlTypes type, int maxDistance,
      Random randomInstance) {
    boolean fullSearch = maxDistance < 0;

    List<TopicNode> visited = new ArrayList<>();
    List<TopicNode> nextNodes = new ArrayList<>();
    nextNodes.add(fOrigin.getTopicNode());
    for (int depth = -1; (depth < maxDistance || fullSearch) && !nextNodes.isEmpty(); depth++) {
      List<TopicNode> nextNext = new ArrayList<>();
      for (TopicNode now : nextNodes) {
        visited.add(now);
        List<FdlNode> fdls = new ArrayList<>(now.getFdls());
        if (randomInstance != null) {
          Collections.shuffle(fdls, randomInstance);
        }
        for (FdlNode fdl : fdls) {
          if (fdl.getFdlType().equals(type) && !fdl.equals(fOrigin)) {
            return fdl;
          }
        }
        TopicNode parent = now.getParent();
        if (parent != null && !visited.contains(parent)) {
          nextNext.add(parent);
        }
        for (TopicNode child : now.getChildren()) {
          if (!visited.contains(child)) {
            nextNext.add(child);
          }
        }
      }
      if (randomInstance != null) {
        Collections.shuffle(nextNext, randomInstance);
      }
      nextNodes = nextNext;
    }
    return null;
  }

  public static FdlNode getFirstOtherFdlUpTo(FdlNode fOrigin, FdlTypes type, int maxDistance,
      Predicate<ICommonFdlSymbol> predicate, Random randomInstance) {
    boolean fullSearch = maxDistance < 0;

    List<TopicNode> visited = new ArrayList<>();
    List<TopicNode> nextNodes = new ArrayList<>();
    nextNodes.add(fOrigin.getTopicNode());
    for (int depth = -1; (depth < maxDistance || fullSearch) && !nextNodes.isEmpty(); depth++) {
      List<TopicNode> nextNext = new ArrayList<>();
      for (TopicNode now : nextNodes) {
        visited.add(now);
        List<FdlNode> fdls = new ArrayList<>(now.getFdls());
        if (randomInstance != null) {
          Collections.shuffle(fdls, randomInstance);
        }
        for (FdlNode fdl : fdls) {
          if (fdl.getFdlType().equals(type) && !fdl.equals(fOrigin) && predicate.test(
              fdl.getSymbol())) {
            return fdl;
          }
        }
        TopicNode parent = now.getParent();
        if (parent != null && !visited.contains(parent)) {
          nextNext.add(parent);
        }
        for (TopicNode child : now.getChildren()) {
          if (!visited.contains(child)) {
            nextNext.add(child);
          }
        }
      }
      if (randomInstance != null) {
        Collections.shuffle(nextNext, randomInstance);
      }
      nextNodes = nextNext;
    }
    return null;
  }

  public static String cutExtension(String str) {
    if (str != null && str.contains(".")) {
      return str.substring(0, str.lastIndexOf('.'));
    }
    return str;
  }

}
