/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package org.thingml.xtext.validation;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator;
import org.eclipse.xtext.validation.Issue;
import org.thingml.xtext.constraints.ThingMLHelpers;
import org.thingml.xtext.thingML.Configuration;
import org.thingml.xtext.thingML.ThingMLModel;

public class Checker {
	private List<Issue> issues;
	
	public Checker() {
		issues = new LinkedList<Issue>();
		// TODO: Add a constructor that accepts compiler-specific validators as well
	}
    
    private static synchronized List<Issue> validate(List<Issue> out, ThingMLModel model) {
    	out.clear();
    	ResourceSet rs = model.eResource().getResourceSet();
    	for (Resource r : rs.getResources()) {
    		if (r instanceof XtextResource) {
    			IResourceValidator validator = ((XtextResource)r).getResourceServiceProvider().getResourceValidator();
    			List<Issue> issues = validator.validate(r, CheckMode.ALL, CancelIndicator.NullImpl);
    			out.addAll(issues);
    		}
    	}
    	return out;
    }
    
    public boolean validateModel(ThingMLModel model) {
    	validate(this.issues, model);
    	return !this.hasErrors();
    }
    
    public boolean validateConfiguration(Configuration cfg) {
    	validate(this.issues, ThingMLHelpers.findContainingModel(cfg));
    	return !this.hasErrors();
    }
    
    /* -- Helpers -- */
    private List<Issue> getIssueBySeverity(Severity severity) {
    	List<Issue> errors = new LinkedList<Issue>();
    	for (Issue issue : this.issues)
    		if (issue.getSeverity() == severity)
    			errors.add(issue);
    	return errors;
    }
    
    public List<Issue> getErrors() {
    	return getIssueBySeverity(Severity.ERROR);
    }
    
    public List<Issue> getWarnings() {
    	return getIssueBySeverity(Severity.WARNING);
    }
    
    public List<Issue> getInfos() {
    	return getIssueBySeverity(Severity.INFO);    	
    }
    
    public boolean hasErrors() {
    	for (Issue issue : this.issues)
    		if (issue.getSeverity() == Severity.ERROR)
    			return true;
    	return false;
    }

    
    /* -- Printing -- */
}
