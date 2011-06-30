/**
 * Copyright (C) 2011 SINTEF <franck.fleurey@sintef.no>
 *
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sintef.thingml.resource.thingml.ui;

/**
 * A CodeCompletionHelper can be used to derive completion proposals for partial
 * documents. It runs the parser generated by EMFText in a special mode (i.e., the
 * rememberExpectedElements mode). Based on the elements that are expected by the
 * parser for different regions in the document, valid proposals are computed.
 */
public class ThingmlCodeCompletionHelper {
	
	private org.sintef.thingml.resource.thingml.mopp.ThingmlAttributeValueProvider attributeValueProvider = new org.sintef.thingml.resource.thingml.mopp.ThingmlAttributeValueProvider();
	
	/**
	 * Computes a set of proposals for the given document assuming the cursor is at
	 * 'cursorOffset'. The proposals are derived using the meta information, i.e., the
	 * generated language plug-in.
	 * 
	 * @param originalResource
	 * @param content the documents content
	 * @param cursorOffset
	 * 
	 * @return
	 */
	public org.sintef.thingml.resource.thingml.ui.ThingmlCompletionProposal[] computeCompletionProposals(org.sintef.thingml.resource.thingml.IThingmlTextResource originalResource, String content, int cursorOffset) {
		org.eclipse.emf.ecore.resource.ResourceSet resourceSet = new org.eclipse.emf.ecore.resource.impl.ResourceSetImpl();
		// the shadow resource needs the same URI because reference resolvers may use the
		// URI to resolve external references
		org.sintef.thingml.resource.thingml.IThingmlTextResource resource = (org.sintef.thingml.resource.thingml.IThingmlTextResource) resourceSet.createResource(originalResource.getURI());
		java.io.ByteArrayInputStream inputStream = new java.io.ByteArrayInputStream(content.getBytes());
		org.sintef.thingml.resource.thingml.IThingmlMetaInformation metaInformation = resource.getMetaInformation();
		org.sintef.thingml.resource.thingml.IThingmlTextParser parser = metaInformation.createParser(inputStream, null);
		org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedTerminal[] expectedElements = parseToExpectedElements(parser, resource, cursorOffset);
		if (expectedElements == null) {
			return new org.sintef.thingml.resource.thingml.ui.ThingmlCompletionProposal[0];
		}
		if (expectedElements.length == 0) {
			return new org.sintef.thingml.resource.thingml.ui.ThingmlCompletionProposal[0];
		}
		java.util.List<org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedTerminal> expectedAfterCursor = java.util.Arrays.asList(getElementsExpectedAt(expectedElements, cursorOffset));
		java.util.List<org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedTerminal> expectedBeforeCursor = java.util.Arrays.asList(getElementsExpectedAt(expectedElements, cursorOffset - 1));
		setPrefixes(expectedAfterCursor, content, cursorOffset);
		setPrefixes(expectedBeforeCursor, content, cursorOffset);
		// First, we derive all possible proposals from the set of elements that are
		// expected at the cursor position.
		java.util.Collection<org.sintef.thingml.resource.thingml.ui.ThingmlCompletionProposal> allProposals = new java.util.LinkedHashSet<org.sintef.thingml.resource.thingml.ui.ThingmlCompletionProposal>();
		java.util.Collection<org.sintef.thingml.resource.thingml.ui.ThingmlCompletionProposal> rightProposals = deriveProposals(expectedAfterCursor, content, resource, cursorOffset);
		java.util.Collection<org.sintef.thingml.resource.thingml.ui.ThingmlCompletionProposal> leftProposals = deriveProposals(expectedBeforeCursor, content, resource, cursorOffset - 1);
		// Second, the set of left proposals (i.e., the ones before the cursor) is checked
		// for emptiness. If the set is empty, the right proposals (i.e., the ones after
		// the cursor) are also considered. If the set is not empty, the right proposal
		// are discarded, because it does not make sense to propose them until the element
		// before the cursor was completed.
		allProposals.addAll(leftProposals);
		if (leftProposals.isEmpty()) {
			allProposals.addAll(rightProposals);
		}
		// Third, the proposals are sorted according to their relevance. Proposals that
		// matched the prefix are preferred over ones that did not. Finally, proposals are
		// sorted alphabetically.
		final java.util.List<org.sintef.thingml.resource.thingml.ui.ThingmlCompletionProposal> sortedProposals = new java.util.ArrayList<org.sintef.thingml.resource.thingml.ui.ThingmlCompletionProposal>(allProposals);
		java.util.Collections.sort(sortedProposals);
		return sortedProposals.toArray(new org.sintef.thingml.resource.thingml.ui.ThingmlCompletionProposal[sortedProposals.size()]);
	}
	
	public org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedTerminal[] parseToExpectedElements(org.sintef.thingml.resource.thingml.IThingmlTextParser parser, org.sintef.thingml.resource.thingml.IThingmlTextResource resource, int cursorOffset) {
		final java.util.List<org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedTerminal> expectedElements = parser.parseToExpectedElements(null, resource, cursorOffset);
		if (expectedElements == null) {
			return new org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedTerminal[0];
		}
		removeDuplicateEntries(expectedElements);
		removeInvalidEntriesAtEnd(expectedElements);
		return expectedElements.toArray(new org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedTerminal[expectedElements.size()]);
	}
	
	private void removeDuplicateEntries(java.util.List<org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedTerminal> expectedElements) {
		for (int i = 0; i < expectedElements.size() - 1; i++) {
			org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedTerminal elementAtIndex = expectedElements.get(i);
			for (int j = i + 1; j < expectedElements.size();) {
				org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedTerminal elementAtNext = expectedElements.get(j);
				if (elementAtIndex.equals(elementAtNext) && elementAtIndex.getStartExcludingHiddenTokens() == elementAtNext.getStartExcludingHiddenTokens()) {
					expectedElements.remove(j);
				} else {
					j++;
				}
			}
		}
	}
	
	private void removeInvalidEntriesAtEnd(java.util.List<org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedTerminal> expectedElements) {
		for (int i = 0; i < expectedElements.size() - 1;) {
			org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedTerminal elementAtIndex = expectedElements.get(i);
			org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedTerminal elementAtNext = expectedElements.get(i + 1);
			if (elementAtIndex.getStartExcludingHiddenTokens() == elementAtNext.getStartExcludingHiddenTokens() && shouldRemove(elementAtIndex.getFollowSetID(), elementAtNext.getFollowSetID())) {
				expectedElements.remove(i + 1);
			} else {
				i++;
			}
		}
	}
	
	public boolean shouldRemove(int followSetID1, int followSetID2) {
		return followSetID1 != followSetID2;
	}
	
	private String findPrefix(java.util.List<org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedTerminal> expectedElements, org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedTerminal expectedAtCursor, String content, int cursorOffset) {
		if (cursorOffset < 0) {
			return "";
		}
		int end = 0;
		for (org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedTerminal expectedElement : expectedElements) {
			if (expectedElement == expectedAtCursor) {
				final int start = expectedElement.getStartExcludingHiddenTokens();
				if (start >= 0  && start < Integer.MAX_VALUE) {
					end = start;
				}
				break;
			}
		}
		end = Math.min(end, cursorOffset);
		final String prefix = content.substring(end, Math.min(content.length(), cursorOffset));
		return prefix;
	}
	
	private java.util.Collection<org.sintef.thingml.resource.thingml.ui.ThingmlCompletionProposal> deriveProposals(java.util.List<org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedTerminal> expectedElements, String content, org.sintef.thingml.resource.thingml.IThingmlTextResource resource, int cursorOffset) {
		java.util.Collection<org.sintef.thingml.resource.thingml.ui.ThingmlCompletionProposal> resultSet = new java.util.LinkedHashSet<org.sintef.thingml.resource.thingml.ui.ThingmlCompletionProposal>();
		for (org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedTerminal expectedElement : expectedElements) {
			resultSet.addAll(deriveProposals(expectedElement, content, resource, cursorOffset));
		}
		return resultSet;
	}
	
	private java.util.Collection<org.sintef.thingml.resource.thingml.ui.ThingmlCompletionProposal> deriveProposals(org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedTerminal expectedTerminal, String content, org.sintef.thingml.resource.thingml.IThingmlTextResource resource, int cursorOffset) {
		org.sintef.thingml.resource.thingml.IThingmlMetaInformation metaInformation = resource.getMetaInformation();
		org.sintef.thingml.resource.thingml.IThingmlLocationMap locationMap = resource.getLocationMap();
		org.sintef.thingml.resource.thingml.IThingmlExpectedElement expectedElement = (org.sintef.thingml.resource.thingml.IThingmlExpectedElement) expectedTerminal.getTerminal();
		if (expectedElement instanceof org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedCsString) {
			org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedCsString csString = (org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedCsString) expectedElement;
			return handleKeyword(csString, expectedTerminal.getPrefix());
		} else if (expectedElement instanceof org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedBooleanTerminal) {
			org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedBooleanTerminal expectedBooleanTerminal = (org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedBooleanTerminal) expectedElement;
			return handleBooleanTerminal(expectedBooleanTerminal, expectedTerminal.getPrefix());
		} else if (expectedElement instanceof org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedEnumerationTerminal) {
			org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedEnumerationTerminal expectedEnumerationTerminal = (org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedEnumerationTerminal) expectedElement;
			return handleEnumerationTerminal(expectedEnumerationTerminal, expectedTerminal.getPrefix());
		} else if (expectedElement instanceof org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedStructuralFeature) {
			org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedStructuralFeature expectedFeature = (org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedStructuralFeature) expectedElement;
			org.eclipse.emf.ecore.EStructuralFeature feature = expectedFeature.getFeature();
			org.eclipse.emf.ecore.EClassifier featureType = feature.getEType();
			java.util.List<org.eclipse.emf.ecore.EObject> elementsAtCursor = locationMap.getElementsAt(cursorOffset);
			org.eclipse.emf.ecore.EObject container = null;
			// we need to skip the proxy elements at the cursor, because they are not the
			// container for the reference we try to complete
			for (int i = 0; i < elementsAtCursor.size(); i++) {
				container = elementsAtCursor.get(i);
				if (!container.eIsProxy()) {
					break;
				}
			}
			// if no container can be found, the cursor is probably at the end of the
			// document. we need to create artificial containers.
			if (container == null) {
				boolean attachedArtificialContainer = false;
				org.eclipse.emf.ecore.EClass containerClass = expectedTerminal.getTerminal().getRuleMetaclass();
				org.eclipse.emf.ecore.EStructuralFeature[] containmentTrace = expectedTerminal.getContainmentTrace();
				java.util.List<org.eclipse.emf.ecore.EObject> contentList = null;
				for (org.eclipse.emf.ecore.EStructuralFeature eStructuralFeature : containmentTrace) {
					if (attachedArtificialContainer) {
						break;
					}
					org.eclipse.emf.ecore.EClass neededClass = eStructuralFeature.getEContainingClass();
					// fill the content list during the first iteration of the loop
					if (contentList == null) {
						contentList = new java.util.ArrayList<org.eclipse.emf.ecore.EObject>();
						java.util.Iterator<org.eclipse.emf.ecore.EObject> allContents = resource.getAllContents();
						while (allContents.hasNext()) {
							org.eclipse.emf.ecore.EObject next = allContents.next();
							contentList.add(next);
						}
					}
					// find object to attach artificial container to
					for (int i = contentList.size() - 1; i >= 0; i--) {
						org.eclipse.emf.ecore.EObject object = contentList.get(i);
						if (neededClass.isInstance(object)) {
							org.eclipse.emf.ecore.EObject newContainer = containerClass.getEPackage().getEFactoryInstance().create(containerClass);
							if (eStructuralFeature.getEType().isInstance(newContainer)) {
								org.sintef.thingml.resource.thingml.util.ThingmlEObjectUtil.setFeature(object, eStructuralFeature, newContainer, false);
								container = newContainer;
								attachedArtificialContainer = true;
							}
						}
					}
				}
			}
			
			if (feature instanceof org.eclipse.emf.ecore.EReference) {
				org.eclipse.emf.ecore.EReference reference = (org.eclipse.emf.ecore.EReference) feature;
				if (featureType instanceof org.eclipse.emf.ecore.EClass) {
					if (reference.isContainment()) {
						// the FOLLOW set should contain only non-containment references
						assert false;
					} else {
						return handleNCReference(metaInformation, container, reference, expectedTerminal.getPrefix(), expectedFeature.getTokenName());
					}
				}
			} else if (feature instanceof org.eclipse.emf.ecore.EAttribute) {
				org.eclipse.emf.ecore.EAttribute attribute = (org.eclipse.emf.ecore.EAttribute) feature;
				if (featureType instanceof org.eclipse.emf.ecore.EEnum) {
					org.eclipse.emf.ecore.EEnum enumType = (org.eclipse.emf.ecore.EEnum) featureType;
					return handleEnumAttribute(metaInformation, expectedFeature, enumType, expectedTerminal.getPrefix(), container);
				} else {
					// handle EAttributes (derive default value depending on the type of the
					// attribute, figure out token resolver, and call deResolve())
					return handleAttribute(metaInformation, expectedFeature, container, attribute, expectedTerminal.getPrefix());
				}
			} else {
				// there should be no other subclass of EStructuralFeature
				assert false;
			}
		} else {
			// there should be no other class implementing IExpectedElement
			assert false;
		}
		return java.util.Collections.emptyList();
	}
	
	private java.util.Collection<org.sintef.thingml.resource.thingml.ui.ThingmlCompletionProposal> handleEnumAttribute(org.sintef.thingml.resource.thingml.IThingmlMetaInformation metaInformation, org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedStructuralFeature expectedFeature, org.eclipse.emf.ecore.EEnum enumType, String prefix, org.eclipse.emf.ecore.EObject container) {
		java.util.Collection<org.eclipse.emf.ecore.EEnumLiteral> enumLiterals = enumType.getELiterals();
		java.util.Collection<org.sintef.thingml.resource.thingml.ui.ThingmlCompletionProposal> result = new java.util.LinkedHashSet<org.sintef.thingml.resource.thingml.ui.ThingmlCompletionProposal>();
		for (org.eclipse.emf.ecore.EEnumLiteral literal : enumLiterals) {
			String unResolvedLiteral = literal.getLiteral();
			// use token resolver to get de-resolved value of the literal
			org.sintef.thingml.resource.thingml.IThingmlTokenResolverFactory tokenResolverFactory = metaInformation.getTokenResolverFactory();
			org.sintef.thingml.resource.thingml.IThingmlTokenResolver tokenResolver = tokenResolverFactory.createTokenResolver(expectedFeature.getTokenName());
			String resolvedLiteral = tokenResolver.deResolve(unResolvedLiteral, expectedFeature.getFeature(), container);
			boolean matchesPrefix = matches(resolvedLiteral, prefix);
			result.add(new org.sintef.thingml.resource.thingml.ui.ThingmlCompletionProposal(resolvedLiteral, prefix, matchesPrefix, expectedFeature.getFeature(), container));
		}
		return result;
	}
	
	private java.util.Collection<org.sintef.thingml.resource.thingml.ui.ThingmlCompletionProposal> handleNCReference(org.sintef.thingml.resource.thingml.IThingmlMetaInformation metaInformation, org.eclipse.emf.ecore.EObject container, org.eclipse.emf.ecore.EReference reference, String prefix, String tokenName) {
		// proposals for non-containment references are derived by calling the reference
		// resolver switch in fuzzy mode.
		org.sintef.thingml.resource.thingml.IThingmlReferenceResolverSwitch resolverSwitch = metaInformation.getReferenceResolverSwitch();
		org.sintef.thingml.resource.thingml.IThingmlTokenResolverFactory tokenResolverFactory = metaInformation.getTokenResolverFactory();
		org.sintef.thingml.resource.thingml.IThingmlReferenceResolveResult<org.eclipse.emf.ecore.EObject> result = new org.sintef.thingml.resource.thingml.mopp.ThingmlReferenceResolveResult<org.eclipse.emf.ecore.EObject>(true);
		resolverSwitch.resolveFuzzy(prefix, container, reference, 0, result);
		java.util.Collection<org.sintef.thingml.resource.thingml.IThingmlReferenceMapping<org.eclipse.emf.ecore.EObject>> mappings = result.getMappings();
		if (mappings != null) {
			java.util.Collection<org.sintef.thingml.resource.thingml.ui.ThingmlCompletionProposal> resultSet = new java.util.LinkedHashSet<org.sintef.thingml.resource.thingml.ui.ThingmlCompletionProposal>();
			for (org.sintef.thingml.resource.thingml.IThingmlReferenceMapping<org.eclipse.emf.ecore.EObject> mapping : mappings) {
				org.eclipse.swt.graphics.Image image = null;
				if (mapping instanceof org.sintef.thingml.resource.thingml.mopp.ThingmlElementMapping<?>) {
					org.sintef.thingml.resource.thingml.mopp.ThingmlElementMapping<?> elementMapping = (org.sintef.thingml.resource.thingml.mopp.ThingmlElementMapping<?>) mapping;
					Object target = elementMapping.getTargetElement();
					// de-resolve reference to obtain correct identifier
					org.sintef.thingml.resource.thingml.IThingmlTokenResolver tokenResolver = tokenResolverFactory.createTokenResolver(tokenName);
					final String identifier = tokenResolver.deResolve(elementMapping.getIdentifier(), reference, container);
					if (target instanceof org.eclipse.emf.ecore.EObject) {
						image = getImage((org.eclipse.emf.ecore.EObject) target);
					}
					boolean matchesPrefix = matches(identifier, prefix);
					resultSet.add(new org.sintef.thingml.resource.thingml.ui.ThingmlCompletionProposal(identifier, prefix, matchesPrefix, reference, container, image));
				}
			}
			return resultSet;
		}
		return java.util.Collections.emptyList();
	}
	
	private java.util.Collection<org.sintef.thingml.resource.thingml.ui.ThingmlCompletionProposal> handleAttribute(org.sintef.thingml.resource.thingml.IThingmlMetaInformation metaInformation, org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedStructuralFeature expectedFeature, org.eclipse.emf.ecore.EObject container, org.eclipse.emf.ecore.EAttribute attribute, String prefix) {
		java.util.Collection<org.sintef.thingml.resource.thingml.ui.ThingmlCompletionProposal> resultSet = new java.util.LinkedHashSet<org.sintef.thingml.resource.thingml.ui.ThingmlCompletionProposal>();
		Object[] defaultValues = attributeValueProvider.getDefaultValues(attribute);
		if (defaultValues != null) {
			for (Object defaultValue : defaultValues) {
				if (defaultValue != null) {
					org.sintef.thingml.resource.thingml.IThingmlTokenResolverFactory tokenResolverFactory = metaInformation.getTokenResolverFactory();
					String tokenName = expectedFeature.getTokenName();
					if (tokenName != null) {
						org.sintef.thingml.resource.thingml.IThingmlTokenResolver tokenResolver = tokenResolverFactory.createTokenResolver(tokenName);
						if (tokenResolver != null) {
							String defaultValueAsString = tokenResolver.deResolve(defaultValue, attribute, container);
							boolean matchesPrefix = matches(defaultValueAsString, prefix);
							resultSet.add(new org.sintef.thingml.resource.thingml.ui.ThingmlCompletionProposal(defaultValueAsString, prefix, matchesPrefix, expectedFeature.getFeature(), container));
						}
					}
				}
			}
		}
		return resultSet;
	}
	
	private java.util.Collection<org.sintef.thingml.resource.thingml.ui.ThingmlCompletionProposal> handleKeyword(org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedCsString csString, String prefix) {
		String proposal = csString.getValue();
		boolean matchesPrefix = matches(proposal, prefix);
		return java.util.Collections.singleton(new org.sintef.thingml.resource.thingml.ui.ThingmlCompletionProposal(proposal, prefix, matchesPrefix, null, null));
	}
	
	private java.util.Collection<org.sintef.thingml.resource.thingml.ui.ThingmlCompletionProposal> handleBooleanTerminal(org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedBooleanTerminal expectedBooleanTerminal, String prefix) {
		java.util.Collection<org.sintef.thingml.resource.thingml.ui.ThingmlCompletionProposal> result = new java.util.LinkedHashSet<org.sintef.thingml.resource.thingml.ui.ThingmlCompletionProposal>(2);
		org.sintef.thingml.resource.thingml.grammar.ThingmlBooleanTerminal booleanTerminal = expectedBooleanTerminal.getBooleanTerminal();
		result.addAll(handleLiteral(booleanTerminal.getAttribute(), prefix, booleanTerminal.getTrueLiteral()));
		result.addAll(handleLiteral(booleanTerminal.getAttribute(), prefix, booleanTerminal.getFalseLiteral()));
		return result;
	}
	
	private java.util.Collection<org.sintef.thingml.resource.thingml.ui.ThingmlCompletionProposal> handleEnumerationTerminal(org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedEnumerationTerminal expectedEnumerationTerminal, String prefix) {
		java.util.Collection<org.sintef.thingml.resource.thingml.ui.ThingmlCompletionProposal> result = new java.util.LinkedHashSet<org.sintef.thingml.resource.thingml.ui.ThingmlCompletionProposal>(2);
		org.sintef.thingml.resource.thingml.grammar.ThingmlEnumerationTerminal enumerationTerminal = expectedEnumerationTerminal.getEnumerationTerminal();
		java.util.Map<String, String> literalMapping = enumerationTerminal.getLiteralMapping();
		for (String literalName : literalMapping.keySet()) {
			result.addAll(handleLiteral(enumerationTerminal.getAttribute(), prefix, literalMapping.get(literalName)));
		}
		return result;
	}
	
	private java.util.Collection<org.sintef.thingml.resource.thingml.ui.ThingmlCompletionProposal> handleLiteral(org.eclipse.emf.ecore.EAttribute attribute, String prefix, String literal) {
		if ("".equals(literal)) {
			return java.util.Collections.emptySet();
		}
		boolean matchesPrefix = matches(literal, prefix);
		return java.util.Collections.singleton(new org.sintef.thingml.resource.thingml.ui.ThingmlCompletionProposal(literal, prefix, matchesPrefix, null, null));
	}
	
	/**
	 * Calculates the prefix for each given expected element. The prefix depends on
	 * the current document content, the cursor position, and the position where the
	 * element is expected.
	 */
	private void setPrefixes(java.util.List<org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedTerminal> expectedElements, String content, int cursorOffset) {
		if (cursorOffset < 0) {
			return;
		}
		for (org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedTerminal expectedElement : expectedElements) {
			String prefix = findPrefix(expectedElements, expectedElement, content, cursorOffset);
			expectedElement.setPrefix(prefix);
		}
	}
	
	public org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedTerminal[] getElementsExpectedAt(org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedTerminal[] allExpectedElements, int cursorOffset) {
		java.util.List<org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedTerminal> expectedAtCursor = new java.util.ArrayList<org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedTerminal>();
		for (int i = 0; i < allExpectedElements.length; i++) {
			org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedTerminal expectedElement = allExpectedElements[i];
			int startIncludingHidden = expectedElement.getStartIncludingHiddenTokens();
			int end = getEnd(allExpectedElements, i);
			if (cursorOffset >= startIncludingHidden && cursorOffset <= end) {
				expectedAtCursor.add(expectedElement);
			}
		}
		return expectedAtCursor.toArray(new org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedTerminal[expectedAtCursor.size()]);
	}
	
	private int getEnd(org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedTerminal[] allExpectedElements, int indexInList) {
		org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedTerminal elementAtIndex = allExpectedElements[indexInList];
		int startIncludingHidden = elementAtIndex.getStartIncludingHiddenTokens();
		int startExcludingHidden = elementAtIndex.getStartExcludingHiddenTokens();
		for (int i = indexInList + 1; i < allExpectedElements.length; i++) {
			org.sintef.thingml.resource.thingml.mopp.ThingmlExpectedTerminal elementAtI = allExpectedElements[i];
			int startIncludingHiddenForI = elementAtI.getStartIncludingHiddenTokens();
			int startExcludingHiddenForI = elementAtI.getStartExcludingHiddenTokens();
			if (startIncludingHidden != startIncludingHiddenForI || startExcludingHidden != startExcludingHiddenForI) {
				return startIncludingHiddenForI - 1;
			}
		}
		return Integer.MAX_VALUE;
	}
	
	private boolean matches(String proposal, String prefix) {
		return (proposal.toLowerCase().startsWith(prefix.toLowerCase()) || org.sintef.thingml.resource.thingml.util.ThingmlStringUtil.matchCamelCase(prefix, proposal) != null) && !proposal.equals(prefix);
	}
	
	public org.eclipse.swt.graphics.Image getImage(org.eclipse.emf.ecore.EObject element) {
		if (!org.eclipse.core.runtime.Platform.isRunning()) {
			return null;
		}
		org.eclipse.emf.edit.provider.ComposedAdapterFactory adapterFactory = new org.eclipse.emf.edit.provider.ComposedAdapterFactory(org.eclipse.emf.edit.provider.ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
		adapterFactory.addAdapterFactory(new org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory());
		adapterFactory.addAdapterFactory(new org.eclipse.emf.ecore.provider.EcoreItemProviderAdapterFactory());
		adapterFactory.addAdapterFactory(new org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory());
		org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider labelProvider = new org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider(adapterFactory);
		return labelProvider.getImage(element);
	}
}
