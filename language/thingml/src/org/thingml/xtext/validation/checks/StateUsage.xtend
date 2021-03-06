package org.thingml.xtext.validation.checks

import org.eclipse.xtext.validation.Check
import org.thingml.xtext.thingML.CompositeState
import org.thingml.xtext.thingML.FinalState
import org.thingml.xtext.thingML.StateContainer
import org.thingml.xtext.thingML.ThingMLPackage
import org.thingml.xtext.validation.ThingMLValidatorCheck
import org.thingml.xtext.thingML.Session
import org.thingml.xtext.thingML.Region

class StateUsage extends ThingMLValidatorCheck {

	@Check(FAST)
	def chectStateUniqueness(org.thingml.xtext.thingML.State s) {
		if (s.eContainer instanceof StateContainer) {
			val c = s.eContainer as StateContainer
			val states = c.substate.filter(
				s2 |
					s2.name == s.name
			)
			if (states.size > 1) {
				val msg = "State " + s.getName() + " is duplicated.";
				error(msg, c, ThingMLPackage.eINSTANCE.stateContainer_Substate, c.substate.indexOf(s),
					"duplicate-state")
			}
		}
	}

	@Check(FAST)
	def chectSessionUniqueness(Session s) {
		val c = s.eContainer as CompositeState
		val sessions = c.session.filter(
			s2 |
				s2.name == s.name
		)
		if (sessions.size > 1) {
			val msg = "Session " + s.getName() + " is duplicated.";
			error(msg, c, ThingMLPackage.eINSTANCE.compositeState_Session, c.substate.indexOf(s), "duplicate-session")
		}
	}

	@Check(FAST)
	def chectRegionUniqueness(Region r) {
		val c = r.eContainer as CompositeState
		val regions = c.region.filter(
			r2 |
				r2.name == r.name
		)
		if (regions.size > 1) {
			val msg = "Region " + r.getName() + " is duplicated.";
			error(msg, c, ThingMLPackage.eINSTANCE.compositeState_Region, c.region.indexOf(r), "duplicate-region")
		}
	}

	@Check(NORMAL)
	def checkUnreachableState(StateContainer sc) {
		sc.substate.forEach [ s, i |
			if(sc.initial === s) return // initial state might otherwise be unreachable
			val isReachable = sc.substate.exists [ st |
				st.outgoing.exists[t|t.target == s]
			]
			if (!isReachable) {
				warning("State " + s.name + " is unreachable", sc, ThingMLPackage.eINSTANCE.stateContainer_Substate, i,
					"state-unreachable")
			}
		]
	}

	@Check(NORMAL)
	def checkSinkState(StateContainer sc) {
		sc.substate.forEach [ s, i |
			if (!(s instanceof FinalState || s instanceof CompositeState) && s.outgoing.empty) {
				warning("State " + s.name + " is a sink state. Consider making it final", sc,
					ThingMLPackage.eINSTANCE.stateContainer_Substate, i, "state-sink", s.name)
			}
		]
	}
}
