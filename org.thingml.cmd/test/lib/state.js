/*
 * Copyright (C) 2014 SINTEF <franck.fleurey@sintef.no>
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
function initStateJS(g){function r(a,b){var c,e,s;c=0;for(e=a.length;c<e;c+=1)if(b(a[c])){if(s)throw"single found more than one result";s=a[c]}return s}function m(a,b){if(a.steelbreeze_statejs_active)return a.steelbreeze_statejs_active[b]}function h(a,b){if(a.steelbreeze_statejs_current)return a.steelbreeze_statejs_current[b]}function x(a,b){var c,e;c=0;for(e=a.length;c<e;c+=1)a[c](b)}function n(a,b,c){this.guard=c||function(a){return!0};this.effect=[];if(b&&null!==b){for(var e=a.owner.ancestors(),
s=b.owner.ancestors(),d=0;e.length>d&&s.length>d&&e[d]===s[d];)d+=1;this.sourceAncestorsToExit=e.slice(d);this.targetAncestorsToEnter=s.slice(d);this.sourceAncestorsToExit.reverse();this.source=a;this.target=b}a[c&&1<c.length?"transitions":"completions"].push(this)}function w(a,b){if(1===b.length)return b[0];throw"initial pseudo states must have one and only one outbound transition";}function d(a,b){this.name=a;this.owner=b}function u(a,b,c){d.call(this,a,c);this.kind=b;this.completions=[];this.kind.isInitial&&
(this.owner.initial=this)}function f(a,b){d.call(this,a,b);this.completions=[];this.transitions=[];this.exit=[];this.entry=[]}function p(a,b){f.call(this,a,b)}function q(a,b){f.call(this,a,b);this.regions=[]}function t(a,b){f.call(this,a,b);this.isFinalState=!0}function k(a,b){d.call(this,a,b);this.initial=null;this.owner&&this.owner.regions.push(this)}function l(a){d.call(this,a);this.regions=[]}n.prototype.onEffect=function(a,b){var c=this.effect,e,d;e=0;for(d=c.length;e<d;e+=1)c[e](a,b)};n.prototype.traverse=
function(a,b){var c,e;if(this.source)for(this.source.beginExit(a),this.source.endExit(a),c=0,e=this.sourceAncestorsToExit.length;c<e;c+=1)this.sourceAncestorsToExit[c].endExit(a);this.onEffect(a,b);if(this.target){c=0;for(e=this.targetAncestorsToEnter.length;c<e;c+=1)this.targetAncestorsToEnter[c].beginEntry(a);this.target.beginEntry(a);this.target.endEntry(a,!1)}};n.Else=function(a,b){n.call(this,a,b,function(a){return!1});this.isElse=!0};n.Else.prototype=n.prototype;n.Else.prototype.constructor=
n.Else;var v={Choice:{isInitial:!1,isHistory:!1,completions:function(a,b){var c,e,d=[];c=0;for(e=b.length;c<e;c+=1)b[c].guard(a)&&d.push(b[c]);return 0<d.length?d[(d.length-1)*Math.random()]:r(b,function(a){return a.isElse})}},DeepHistory:{isInitial:!0,isHistory:!0,completions:w},Initial:{isInitial:!0,isHistory:!1,completions:w},Junction:{isInitial:!1,isHistory:!1,completions:function(a,b){var c=r(b,function(b){return b.guard(a)});if(c||(c=r(b,function(a){return a.isElse})))return c;throw"junction PseudoState has no valid competion transitions";
}},ShallowHistory:{isInitial:!0,isHistory:!0,completions:w},Terminate:{isInitial:!1,isHistory:!1,completions:null}};d.prototype.qualifiedName=function(){return this.owner?this.owner+"."+this.name:this.name};d.prototype.ancestors=function(){return(this.owner?this.owner.ancestors():[]).concat(this)};d.prototype.beginExit=function(a){};d.prototype.endExit=function(a){/*console.log("Leave: "+this.toString());*/a.steelbreeze_statejs_active||(a.steelbreeze_statejs_active=[]);a.steelbreeze_statejs_active[this]=
!1};d.prototype.beginEntry=function(a){m(a,this)&&(this.beginExit(a),this.endExit(a));/*console.log("Enter: "+this.toString());*/a.steelbreeze_statejs_active||(a.steelbreeze_statejs_active=[]);a.steelbreeze_statejs_active[this]=!0};d.prototype.endEntry=function(a,b){};d.prototype.getCurrent=function(a){return{name:this.name}};d.prototype.toString=function(){return this.qualifiedName()};u.prototype=new d;u.prototype.constructor=u;u.prototype.endEntry=function(a,b){this.kind===v.Terminate?a.IsTerminated=
!0:this.kind.completions(a,this.completions).traverse(a,b)};f.prototype=new d;f.prototype.constructor=f;f.prototype.isComplete=function(a){return!0};f.prototype.onExit=function(a){x(this.exit,a)};f.prototype.endExit=function(a){this.onExit(a);d.prototype.endExit.call(this,a)};f.prototype.onEntry=function(a){x(this.entry,a)};f.prototype.beginEntry=function(a){d.prototype.beginEntry.call(this,a);if(this.owner){var b=this.owner;a.steelbreeze_statejs_current||(a.steelbreeze_statejs_current=[]);a.steelbreeze_statejs_current[b]=
this}this.onEntry(a)};f.prototype.endEntry=function(a,b){this.evaluateCompletions(a,b)};f.prototype.evaluateCompletions=function(a,b){if(this.isComplete(a)){var c=r(this.completions,function(b){return b.guard(a)});c&&c.traverse(a,b)}};f.prototype.process=function(a,b){var c=r(this.transitions,function(c){return c.guard(a,b)});if(!c)return!1;c.traverse(a,b);return!0};p.prototype=new f;p.prototype.constructor=p;p.prototype.isComplete=function(a){var b=h(a,this);return a.isTerminated||null===b||b.isFinalState||
!1===m(a,b)};p.prototype.beginExit=function(a){var b=h(a,this);b&&(b.beginExit(a),b.endExit(a))};p.prototype.endEntry=function(a,b){var c=(b||this.initial.kind.isHistory?h(a,this):this.initial)||this.initial;c.beginEntry(a);c.endEntry(a,b||this.initial.kind===v.DeepHistory);f.prototype.endEntry.call(this,a,b)};p.prototype.process=function(a,b){var c=h(a,this).process(a,b)||f.prototype.process.call(this,a,b);!0===c&&this.evaluateCompletions(a,!1);return c};p.prototype.getCurrent=function(a){var b=
d.prototype.getCurrent.call(this,a),c=h(a,this);c&&(b.current=c.getCurrent(a));return b};q.prototype=new f;q.prototype.constructor=q;q.prototype.isComplete=function(a){var b,c;if(!a.isTerminated)for(b=0,c=this.regions.length;b<c;b+=1)if(!this.regions[b].isComplete(a))return!1;return!0};q.prototype.beginExit=function(a){var b,c;b=0;for(c=this.regions.length;b<c;b+=1)m(a,this.regions[b])&&(this.regions[b].beginExit(a),this.regions[b].endExit(a))};q.prototype.endEntry=function(a,b){var c,e;c=0;for(e=
this.regions.length;c<e;c+=1)this.regions[c].beginEntry(a),this.regions[c].endEntry(a);f.prototype.endEntry.call(this,a,b)};q.prototype.process=function(a,b){var c,e,d=!1;if(!a.isTerminated){c=0;for(e=this.regions.length;c<e;c+=1)d=this.regions[c].process(a,b)||d;!1===d&&(d=f.prototype.process.call(this,a,b))}!0===d&&this.evaluateCompletions(a,!1);return d};q.prototype.getCurrent=function(a){var b=d.prototype.getCurrent.call(this,a),c,e;b.regions=[];c=0;for(e=this.regions.length;c<e;c+=1)m(a,this.regions[c])&&
(b.regions[c]=this.regions[c].getCurrent(a));return b};t.prototype=new f;t.prototype.constructor=t;delete t.prototype.comlpetions;delete t.prototype.transitions;t.prototype.process=function(a,b){return!1};k.prototype=new d;k.prototype.constructor=k;k.prototype.isComplete=function(a){var b=h(a,this);return a.isTerminated||null===b||b.isFinalState||!1===m(a,b)};k.prototype.initialise=function(a){this.beginEntry(a);this.endEntry(a,!1)};k.prototype.beginExit=function(a){var b=h(a,this);b&&(b.beginExit(a),
b.endExit(a))};k.prototype.endEntry=function(a,b){var c=(b||this.initial.kind.isHistory?h(a,this):this.initial)||this.initial;c.beginEntry(a);c.endEntry(a,b||this.initial.kind===v.DeepHistory)};k.prototype.process=function(a,b){return a.isTerminated?!1:m(a,this)&&h(a,this).process(a,b)};k.prototype.getCurrent=function(a){var b=d.prototype.getCurrent.call(this,a),c=h(a,this);c&&(b.current=c.getCurrent(a));return b};l.prototype=new d;l.prototype.constructor=l;l.prototype.isComplete=function(a){var b,
c;if(!a.isTerminated)for(b=0,c=this.regions.length;b<c;b+=1)if(!this.regions[b].isComplete(a))return!1;return!0};l.prototype.initialise=function(a){this.beginEntry(a);this.endEntry(a,!1)};l.prototype.beginExit=function(a){var b,c;b=0;for(c=this.regions.length;b<c;b+=1)m(a,this.regions[b])&&(this.regions[b].beginExit(a),this.regions[b].endExit(a))};l.prototype.endEntry=function(a,b){var c,e;c=0;for(e=this.regions.length;c<e;c+=1)this.regions[c].beginEntry(a),this.regions[c].endEntry(a,b);d.prototype.endEntry.call(this,
a,b)};l.prototype.process=function(a,b){var c,d,f=!1;if(!a.isTerminated)for(c=0,d=this.regions.length;c<d;c+=1)f=this.regions[c].process(a,b)||f;return f};l.prototype.getCurrent=function(a){var b=d.prototype.getCurrent.call(this,a),c,e;b.regions=[];c=0;for(e=this.regions.length;c<e;c+=1)m(a,this.regions[c])&&(b.regions[c]=this.regions[c].getCurrent(a));return b};g.PseudoStateKind=v;g.PseudoState=u;g.SimpleState=f;g.CompositeState=p;g.OrthogonalState=q;g.FinalState=t;g.Region=k;g.StateMachine=l;g.Transition=
n}this.exports?initStateJS(this.exports):this.define?this.define(function(g,r,m){initStateJS(r)}):initStateJS(this);