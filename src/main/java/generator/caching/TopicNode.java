package generator.caching;

import java.util.ArrayList;
import java.util.List;

public class TopicNode {

  private final String nodeName;
  private String id;
  private TopicNode parent;
  private List<TopicNode> children;
  private List<TopicNode> distractors;
  private List<FdlNode> fdls;
  private String description;
  private String modelname;
  private boolean generationSource;

  public TopicNode(String nodeName) {
    this.nodeName = nodeName;
  }

  public TopicNode(String nodeName, String id, List<TopicNode> children,
      List<TopicNode> distractors, List<FdlNode> fdls, String description, String modelname) {
    this.nodeName = nodeName;
    this.id = id;
    this.children = children;
    this.distractors = distractors;
    this.fdls = fdls;
    this.description = description;
    this.parent = null;
    this.modelname = modelname;
  }

  public TopicNode(String nodeName, String id, String modelname) {
    this(nodeName, id, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), "", modelname);
  }

  public String getNodeName() {
    return nodeName;
  }

  public String getId() {
    return id;
  }

  public TopicNode getParent() {
    return parent;
  }

  public void setParent(TopicNode parent) {
    this.parent = parent;
  }

  public List<TopicNode> getChildren() {
    return children;
  }

  public List<TopicNode> getDistractors() {
    return distractors;
  }

  public List<FdlNode> getFdls() {
    return fdls;
  }

  public String getDescription() {
    return description;
  }

  public boolean isGenerationSource() {
    return generationSource;
  }

  public void setGenerationSource(boolean generationSource) {
    this.generationSource = generationSource;
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof TopicNode) {
      return this.nodeName.equals(((TopicNode) other).getNodeName()) && this.id.equals(
          ((TopicNode) other).getId());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return (this.nodeName + this.id).hashCode();
  }

  public String getModelname() {
    return modelname;
  }
}
