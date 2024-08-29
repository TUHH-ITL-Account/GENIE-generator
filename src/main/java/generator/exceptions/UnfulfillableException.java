package generator.exceptions;

import generator.caching.TopicNode;

public class UnfulfillableException extends Exception {

  public UnfulfillableException(String errorMessage) {
    super(errorMessage);
  }

  public UnfulfillableException(TopicNode topicNode) {
    super("Unable to find template-compatible FDLs in topic '" + topicNode.getNodeName()
        + "', containing "
        + topicNode.getFdls().size() + " FDLs.");
  }
}
