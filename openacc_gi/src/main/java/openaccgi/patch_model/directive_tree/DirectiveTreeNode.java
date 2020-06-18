package openaccgi.patch_model.directive_tree;

import openaccgi.patch_model.ADirective;
import openaccgi.patch_model.OpenACCDataDirective;

import java.util.*;

/*package*/ abstract class DirectiveTreeNode {
	private Optional<DirectiveTreeNode> parent;

	/*package*/ DirectiveTreeNode(Optional<DirectiveTreeNode> par) {
		this.parent = par;
	}

	/*package*/ DirectiveTreeNode getRoot() {
		if (!this.getParent().isPresent()) {
			return this;
		}
		return this.getParent().get().getRoot();
	}


	/*package*/ Set<ADirective> getAllDirectivesInTree() {
		return this.getRoot().getAllDirectives();
	}


	abstract protected void addDirective(ADirective directive);

	/*package*/ Optional<DirectiveTreeNode> getParent() {
		return this.parent;
	}

	protected void setParent(Optional<DirectiveTreeNode> node){
		this.parent = node;
	}

	protected static Map.Entry<Integer, Integer> getDirectiveRange(ADirective directive) {
		if(directive instanceof OpenACCDataDirective) {
			int startLineNumber = ((OpenACCDataDirective) directive).getStartLineNumber();
			int endLineNumber = ((OpenACCDataDirective) directive).getEndLineNumber();
			return new HashMap.SimpleEntry<Integer, Integer>(startLineNumber, endLineNumber);
		}
		assert(false);
		return null;
	}

	abstract protected Set<ADirective> getAllDirectives();
	abstract protected List<DirectiveTreeDirectiveNode> getLeafNodes();
}