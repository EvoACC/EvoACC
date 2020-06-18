package openaccgi.patch_model.directive_tree;

import openaccgi.patch_model.*;
import openaccgi.patch_model.*;

import java.io.IOException;
import java.util.*;

/*package*/ class DirectiveTreeDirectiveNode extends DirectiveTreeNode {
	private final ADirective directive;
	private final List<DirectiveTreeDirectiveNode> children = new ArrayList<DirectiveTreeDirectiveNode>();

	/*package*/ DirectiveTreeDirectiveNode(ADirective dir, DirectiveTreeNode parent){
		super(Optional.of(parent));
		this.directive = dir;
	}

	protected ADirective getDirective(){
		return this.directive;
	}

	/*package*/ void setChildren(List<DirectiveTreeDirectiveNode> childList){
		this.children.clear();
		this.children.addAll(childList);
	}

	/*package*/ List<DirectiveTreeDirectiveNode> getChildren(){
		return Collections.unmodifiableList(this.children);
	}


	protected boolean removeUselessElements(IToolUtils toolUtils){
		//Returns 'true' if something has been changed
		boolean toReturn = false;

		if(this.getDirective().isActive() && this.getDirective().getClass() == OpenACCDataDirective.class){
			OpenACCDataDirective dir = (OpenACCDataDirective) this.getDirective();

			Set<ADirective> allDirectives = this.getAllDirectivesInTree();
			try {
				DataDirectiveInsertionPoint d =
						new DataDirectiveInsertionPoint(dir.getFile(),dir.getStartLineNumber(),dir.getEndLineNumber());
				if(toolUtils.getDataDirectiveInsertionPoints(
						dir.getFile(),allDirectives.toArray(new ADirective[allDirectives.size()])).contains(d)){

					Optional<OpenACCDataDirective> altDirective = toolUtils.getDataDirective(dir.getFile(),
							dir.getStartLineNumber(), dir.getEndLineNumber(),
							allDirectives.toArray(new ADirective[allDirectives.size()]));

					if(altDirective.isPresent()) {

						assert (altDirective.isPresent());
						assert (dir.getStartLineNumber() == altDirective.get().getStartLineNumber());
						assert (dir.getEndLineNumber() == altDirective.get().getEndLineNumber());

						if (!altDirective.get().getVariables().equals(dir.getVariables())
								|| !altDirective.get().getVariableRanges().equals(dir.getVariableRanges())
								|| !altDirective.get().getUpdateDirectives().equals(dir.getUpdateDirectives())) {

							//Handle the variables
							dir.wipeVariables();

							assert (dir.getVariables().isEmpty());
							assert (dir.getVariableRanges().isEmpty());

							for (String var : altDirective.get().getVariables().keySet()) {
								dir.setVariable(var, altDirective.get().getVariables().get(var));
								if (altDirective.get().getVariableRanges().containsKey(var)) {
									dir.setVariableRange(var, altDirective.get().getVariableRanges().get(var));
								}
							}

							//Handle the update directives
							dir.wipeUpdateDirectives();

							assert(dir.getUpdateDirectives().isEmpty());

							for(OpenACCUpdateDirective upDir : altDirective.get().getUpdateDirectives()) {
								dir.addUpdateDirective(upDir);
							}

							toReturn = true;
						}
					} else {
						dir.setActive(false);
						toReturn = true;
					}

				} else {
					dir.setActive(false);
					toReturn = true;
				}

			}catch(IOException e){
				assert(false); //TODO: Handle this
			}
		}
		return toReturn;
	}


	protected Map.Entry<Integer, Integer> getRange(){
		return getDirectiveRange(this.getDirective());
	}

	@Override
	protected void addDirective(ADirective directive) {
		Map.Entry<Integer, Integer> range = this.getRange();
		Map.Entry<Integer, Integer> directiveRange = getDirectiveRange(directive);

		boolean added=false;
		if(this.children.isEmpty()){
			this.children.add(new DirectiveTreeDirectiveNode(directive, this));
			added=true;
		}else if(this.children.get(0).getRange().getKey() <= range.getKey()){ //If before all children...
			this.children.add(0,new DirectiveTreeDirectiveNode(directive, this));
			added=true;
			//If after all children..
		} else if(this.children.get(this.children.size()-1).getRange().getValue() <= directiveRange.getKey()) {
			this.children.add(new DirectiveTreeDirectiveNode(directive, this));
			added = true;
		} else { // If between children...
			for(int i=0; i<this.children.size();i++){
				DirectiveTreeDirectiveNode directiveTreeDirectiveNode = this.children.get(i);
				Map.Entry<Integer, Integer> directiveNodeRange =
					getDirectiveRange(directiveTreeDirectiveNode.getDirective());
				if(directiveNodeRange.getKey() < directiveRange.getKey()
						&& directiveRange.getValue() < directiveNodeRange.getValue()){
					directiveTreeDirectiveNode.addDirective(directive);
					added=true;
					break;
				} else if((i+1) < this.children.size()){
					DirectiveTreeDirectiveNode nextDirectiveTreeDirectiveNode = this.children.get(i+1);
					if(directiveTreeDirectiveNode.getRange().getValue() <= directiveRange.getKey()
							&& directiveRange.getValue() <= nextDirectiveTreeDirectiveNode.getRange().getKey()){
						this.children.add((i+1),
								new DirectiveTreeDirectiveNode(directive, this));
						added=true;
						break;
					}
				}
			}
		}

		if(!added){ //If across children...
			List<DirectiveTreeDirectiveNode> covered = new ArrayList<DirectiveTreeDirectiveNode>();
			for(DirectiveTreeDirectiveNode dirNode : this.children){
				if(directiveRange.getKey() <= dirNode.getRange().getKey()
						&& directiveRange.getValue() >= dirNode.getRange().getValue()){
					covered.add(dirNode);
				}
			}

			if(!covered.isEmpty()){
				DirectiveTreeDirectiveNode directiveTreeDirectiveNode
					= new DirectiveTreeDirectiveNode(directive, this);
				int indexToInsert = this.children.indexOf(covered.get(0));
				for(DirectiveTreeDirectiveNode node : covered){
					this.children.remove(node);
					node.setParent(Optional.of(directiveTreeDirectiveNode));
				}
				directiveTreeDirectiveNode.setChildren(covered);

				this.children.add(indexToInsert, directiveTreeDirectiveNode);
				added = true;
			}
		}

		assert(added);
	}

	@Override
	protected Set<ADirective> getAllDirectives(){
		Set<ADirective> toReturn = new HashSet<ADirective>();
		toReturn.add(this.getDirective());
		for(DirectiveTreeNode directiveTreeNode : this.children){
			toReturn.addAll(directiveTreeNode.getAllDirectives());
		}

		return toReturn;
	}

	@Override
	protected List<DirectiveTreeDirectiveNode> getLeafNodes(){
		List<DirectiveTreeDirectiveNode> toReturn = new ArrayList<DirectiveTreeDirectiveNode>();
		if(this.children.isEmpty()){
			toReturn.add(this);
		} else {
			for(DirectiveTreeDirectiveNode dirNode : this.children){
				toReturn.addAll(dirNode.getLeafNodes());
			}
		}
		return toReturn;
	}
}