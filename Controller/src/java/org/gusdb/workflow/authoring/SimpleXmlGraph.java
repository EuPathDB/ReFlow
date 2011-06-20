package org.gusdb.workflow.authoring;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.workflow.Name;
import org.gusdb.workflow.NamedValue;
import org.gusdb.workflow.WorkflowXmlContainer;

public class SimpleXmlGraph extends WorkflowXmlContainer<SimpleXmlNode> {

  private File _xmlFileName;
  private List<String> _paramDeclarations = new ArrayList<String>();
  private Map<String, String> _constants = new HashMap<String, String>();
  private Map<String, String> _globalConstants = new HashMap<String, String>();
  private Map<String, SimpleXmlNode> _stepsByName = new LinkedHashMap<String, SimpleXmlNode>();

  //TODO: xmlFileName may actually be a URL, handle if so
  @Override
  public void setXmlFileName(String xmlFileName) {
    _xmlFileName = new File(xmlFileName);
  }
  
  public String getXmlFileName() {
    return _xmlFileName.getAbsolutePath();
  }

  @Override
  public void addParamDeclaration(Name name) {
    _paramDeclarations.add(name.getName());
  }

  @Override
  public void addConstant(NamedValue namedValue) {
    _constants.put(namedValue.getName(), namedValue.getValue());
  }

  @Override
  public void addGlobalConstant(NamedValue namedValue) {
    _globalConstants.put(namedValue.getName(), namedValue.getValue());
  }

  @Override
  public void addStep(SimpleXmlNode step) throws IOException {
    _stepsByName.put(step.getBaseName(), step);
  }
  
  @Override
  public void addGlobalStep(SimpleXmlNode step) throws IOException {
    // don't differentiate between global and local steps
    // TODO is this correct?
    addStep(step);
  }
  
  public Collection<SimpleXmlNode> getNodes() {
    return _stepsByName.values();
  }
}
