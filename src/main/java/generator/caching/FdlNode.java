package generator.caching;

import fdl.fdl._symboltable.ICommonFdlSymbol;
import generator.types.FdlDslSymbolHelper;
import generator.types.FdlDslSymbolHelper.FdlTypes;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FdlNode {

  private final String fdlName;
  private String id;
  private String fileName;
  private List<TopicNode> prerequisites;
  private String description;
  private Date unlockDate;
  private boolean isActivated;
  private ICommonFdlSymbol symbol;
  private FdlTypes fdlType;
  private TopicNode topicNode;

  public FdlNode(String fdlName) {
    this.fdlName = fdlName;
  }

  public FdlNode(String fdlName, String id, String fileName,
      List<TopicNode> prerequisites, String description, Date unlockDate, boolean isActivated) {
    this.fdlName = fdlName;
    this.id = id;
    this.fileName = fileName;
    this.prerequisites = prerequisites;
    this.description = description;
    this.unlockDate = unlockDate;
    this.isActivated = isActivated;
  }

  public FdlNode(String fdlName, String id, String fileName) {
    this(fdlName, id, fileName, new ArrayList<TopicNode>(), "", null, true);
  }

  public String getFdlName() {
    return fdlName;
  }

  public String getId() {
    return id;
  }

  public String getFileName() {
    return fileName;
  }

  public List<TopicNode> getPrerequisites() {
    return prerequisites;
  }

  public String getDescription() {
    return description;
  }

  public Date getUnlockDate() {
    return unlockDate;
  }

  public boolean isActivated() {
    return isActivated;
  }

  public ICommonFdlSymbol getSymbol() {
    return symbol;
  }

  public void setSymbolAndType(ICommonFdlSymbol symbol) {
    this.symbol = symbol;
    fdlType = FdlDslSymbolHelper.getFdlType(symbol);
  }

  public FdlTypes getFdlType() {
    return fdlType;
  }

  public TopicNode getTopicNode() {
    return topicNode;
  }

  public void setTopicNode(TopicNode topicNode) {
    this.topicNode = topicNode;
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof FdlNode) {
      return this.fdlName.equals(((FdlNode) other).getFdlName()) && this.id.equals(
          ((FdlNode) other).getId());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return (this.fdlName + this.id).hashCode();
  }

}
