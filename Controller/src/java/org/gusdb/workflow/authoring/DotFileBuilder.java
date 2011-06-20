package org.gusdb.workflow.authoring;

import java.io.File;

import org.gusdb.workflow.Name;
import org.gusdb.workflow.Utilities;
import org.gusdb.workflow.WorkflowXmlParser;

public class DotFileBuilder {

  private final static String NL = System.getProperty("line.separator");
  
  private final static int MAX_CHARS_PER_LINE = 20;
  
  public static void main(String[] args) {
    try {
      File inputFile = parseArgs(args);
      
      WorkflowXmlParser<SimpleXmlNode, SimpleXmlGraph> parser =
        new WorkflowXmlParser<SimpleXmlNode, SimpleXmlGraph>();

      SimpleXmlGraph graph = parser.parseWorkflow(SimpleXmlNode.class, SimpleXmlGraph.class,
          inputFile.getAbsolutePath(), "", false);
      
      // append dot file header
      StringBuilder output = new StringBuilder()
        .append("digraph name {").append(NL)
        .append("  graph [ fontsize=8, rankdir=\"TB\" ]").append(NL)
        .append("  node [ fontsize=8, height=0, width=0, margin=0.03,0.02 ]").append(NL)
        .append("  edge [ fontsize=8, arrowhead=open ]").append(NL);
      
      // append vertex info
      for (SimpleXmlNode vertex : graph.getNodes()) {
        String nodeLabel = formatNodeLabel(vertex.getBaseName());
        output
          .append("  ")
          .append(nodeLabel)
          .append(" [ shape=rectangle");
        if (vertex.getSubgraphXmlFileName() != null) {
          if (vertex.getSubgraphXmlFileName().startsWith("$$")) {
            // add url, but make sure it points only to the file (html)
            output.append(", color=green");
          }
          else {
            // add url, but make sure it points only to the file (html)
            File htmlFile = new File(vertex.getSubgraphXmlFileName().replace(".xml", ".html"));
            output.append(", URL=\"" + htmlFile.getName() + "\", color=blue, penwidth=2");
          }
        }
        else {
          output.append(", color=black");
        }
        output.append(" ]").append(NL);
      }
      
      // append dependency info
      for (SimpleXmlNode vertex : graph.getNodes()) {
        String nodeLabel = formatNodeLabel(vertex.getBaseName());
        for (Name dependency : vertex.getDependsNames()) {
          output
            .append("  ").append(formatNodeLabel(dependency.getName())).append(" -> ")
            .append(nodeLabel).append(NL);
        }
      }
      
      // append dot file footer
      output.append("}").append(NL);
      
      // dump to stdout
      System.out.println(output.toString());
    }
    catch (Exception e) {
      e.printStackTrace();
      System.exit(1); // important for caller to know we failed
    }
  }

  /**
   * Base name passed in is a camel-case string representing a name; it may
   * also contain underscores and dashes, which must be trimmed and/or
   * formatted nicely.  The first letter should also be capitalized if it
   * isn't already.
   * 
   * @param baseName
   * @return name formatted for display and DOT-file legality
   */
  private static String formatNodeLabel(String baseName) {
	// split camel-case into words
	baseName = Utilities.splitCamelCase(baseName);
	// capitalize first letter
    baseName = baseName.substring(0, 1).toUpperCase() + baseName.substring(1, baseName.length());
    // replace dashes with underscores (DOT format requires this)
    baseName = baseName.replace('-', '_');
    // get rid of single-underscore words
    baseName = baseName.replace("_ ", "");
    // convert to multi-line format and return
    return Utilities.multiLineFormat("\"" + baseName + "\"", MAX_CHARS_PER_LINE);
  }

  private static File parseArgs(String[] args) {
    if (args.length != 1) {
      System.err.println("USAGE: java " + DotFileBuilder.class.getName() + " <workflowXmlFile>");
      System.exit(1);
    }
    return Utilities.getReadableFileOrDie(args[0]);
  }
  
}
