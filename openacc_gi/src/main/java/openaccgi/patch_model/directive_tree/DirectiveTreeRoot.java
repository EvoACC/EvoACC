package openaccgi.patch_model.directive_tree;

import openaccgi.patch_model.ADirective;
import openaccgi.patch_model.IToolUtils;

import java.io.File;
import java.util.*;

public class DirectiveTreeRoot extends DirectiveTreeNode {
	private final List<DirectiveTreeFileNode> children = new ArrayList<DirectiveTreeFileNode>();

	/*package*/ DirectiveTreeRoot(){
		super(Optional.empty());
	}

	public void removeUselessElements(IToolUtils toolUtils){
		boolean somethingChanged = false;
		do {
			somethingChanged = false;
			for (DirectiveTreeNode leaf : this.getLeafNodes()) {
				if(removeUselessElementsToRoot(leaf, toolUtils)){
					somethingChanged = true;
				}
			}
		}while(somethingChanged);
	}

	private static boolean removeUselessElementsToRoot(DirectiveTreeNode directiveTreeNode, IToolUtils toolUtils){
		//return true if something has been changed
		boolean toReturn = false;
		if(directiveTreeNode instanceof DirectiveTreeDirectiveNode){
			DirectiveTreeDirectiveNode dirNodeLeaf = (DirectiveTreeDirectiveNode) directiveTreeNode;
			if(dirNodeLeaf.removeUselessElements(toolUtils)){
				toReturn = true;
			}
		}
		if(directiveTreeNode.getParent().isPresent()){
			if(removeUselessElementsToRoot(directiveTreeNode.getParent().get(), toolUtils)){
				toReturn = true;
			}
		}
		return toReturn;
	}

	/*package*/ List<DirectiveTreeFileNode> getChildren(){
		return Collections.unmodifiableList(this.children);
	}


	@Override
	protected void addDirective(ADirective directive){
		File f = directive.getFile();

		int insertLoc = this.children.isEmpty() ? 0 : -1;
		for (int i = 0; i < this.children.size(); i++) {
			int compareInt = directive.getFile().compareTo(this.children.get(i).getFile());
			if (compareInt == 0) {
				this.children.get(i).addDirective(directive);
				break;
			} else if (compareInt < 0) {
				insertLoc = i;
				break;
			} else if (this.children.size() - 1 == i) {
				insertLoc = this.children.size();
				break;
			}
		}

		if (insertLoc != -1) {
			DirectiveTreeFileNode directiveTreeFileNode = new DirectiveTreeFileNode(this, f);
			directiveTreeFileNode.addDirective(directive);
			this.children.add(insertLoc, directiveTreeFileNode);
		}
	}

	@Override
	protected Set<ADirective> getAllDirectives(){
		Set<ADirective> toReturn = new HashSet<ADirective>();;
		for(DirectiveTreeNode directiveTreeNode : this.children){
			toReturn.addAll(directiveTreeNode.getAllDirectives());
		}

		return toReturn;
	}

	@Override
	protected List<DirectiveTreeDirectiveNode> getLeafNodes(){
		List<DirectiveTreeDirectiveNode> toReturn = new ArrayList<DirectiveTreeDirectiveNode>();
		for(DirectiveTreeFileNode directiveTreeFileNode : this.children){
			toReturn.addAll(directiveTreeFileNode.getLeafNodes());
		}
		return toReturn;
	}

}
