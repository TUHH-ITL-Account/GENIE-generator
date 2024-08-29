package generator.types;

import fdl.fdl._symboltable.EquationSymbol;
import fdl.fdl._symboltable.VariableGroupsSymbol;
import generator.caching.FdlNode;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CalculationGraph {

  private final List<FdlNode> fdls;
  private final VariableGroupsSymbol varGroups;
  public List<EquationNode> nodes;

  public CalculationGraph(List<FdlNode> fdls) {
    this(fdls, null);
  }

  public CalculationGraph(List<FdlNode> fdls, VariableGroupsSymbol varGroups) {
    nodes = new ArrayList<>();
    this.fdls = fdls == null ? new ArrayList<>() : fdls;
    this.varGroups = varGroups;
    for (FdlNode node : this.fdls) {
      if (!(node.getSymbol() instanceof EquationSymbol)) {
        continue;
      }
      EquationNode toAdd = new EquationNode(node.getFdlName());
      toAdd.identifiers = new ArrayList<>(
          ((EquationSymbol) node.getSymbol()).getAliasMap().keySet()); //todo: real filter
      nodes.add(toAdd);
    }
    for (EquationNode eNode : nodes) {
      for (String id : eNode.identifiers) {
        if (varGroups == null) {
          //todo
        } else {
          List<List<SimpleEntry<String,String>>> a = varGroups.getVariableToGroupsMap()
              .get(new SimpleEntry<>(eNode.fdlName, id));
          if (a != null) {
            for (List<SimpleEntry<String,String>> group : a) {
              for (SimpleEntry<String,String> element : group) {
                if (!Objects.equals(element.getValue(), eNode.fdlName)) {
                  eNode.addConnection(id, element.getKey(), element.getValue());
                }
              }
            }
          }
        }
      }
    }
  }

  public List<FdlNode> getFdls() {
    return fdls;
  }

  public VariableGroupsSymbol getVarGroups() {
    return varGroups;
  }

  public static class EquationNode {

    public String fdlName;
    public List<String> identifiers;
    /**
     * [id_own, id_other, fld_other]
     */
    public List<String[]> connections;

    public EquationNode(String fdlName) {
      this.fdlName = fdlName;
    }

    public void addConnection(String ownId, String otherId, String otherFdl) {
      connections.add(new String[]{ownId, otherId, otherFdl});
    }
  }
}
