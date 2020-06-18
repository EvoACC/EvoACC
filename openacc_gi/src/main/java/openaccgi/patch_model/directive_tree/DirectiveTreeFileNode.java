package openaccgi.patch_model.directive_tree;

import openaccgi.patch_model.ADirective;

import java.io.File;
import java.util.*;

public class DirectiveTreeFileNode extends DirectiveTreeNode {
	private final File file;
	private final List<DirectiveTreeDirectiveNode> children = new ArrayList<DirectiveTreeDirectiveNode>();

	protected DirectiveTreeFileNode(DirectiveTreeRoot root, File f){
		super(Optional.of(root));
		this.file=f;
	}

	/*package*/ File getFile(){
		return this.file;
	}

	//TODO: Lots of duplication over the next three methods and those contained in the DirectiveTreeDirectiveNode class.
	private Map.Entry<Integer, Integer> getRange(){
		if(this.children.isEmpty()) {
			return new HashMap.SimpleEntry<Integer, Integer>(0,0);
		}

		return new HashMap.SimpleEntry<Integer, Integer>(this.children.get(0).getRange().getKey(),
				this.children.get(this.children.size()-1).getRange().getValue());
	}

	/*package*/ List<DirectiveTreeDirectiveNode> getChildren(){
		return Collections.unmodifiableList(children);
	}

	@Override
	protected void addDirective(ADirective directive){
		Map.Entry<Integer, Integer> range = this.getRange();
		Map.Entry<Integer, Integer> directiveRange = getDirectiveRange(directive);

		boolean added=false;
		if(this.children.isEmpty()){ //If no children...
			this.children.add(new DirectiveTreeDirectiveNode(directive, this));
			added=true;
		}else if(directiveRange.getValue() <= range.getKey()){ //If before all children...
			this.children.add(0,new DirectiveTreeDirectiveNode(directive, this));
			added=true;
		} else if(range.getValue() <= directiveRange.getKey()) {//If after all children..
			this.children.add(new DirectiveTreeDirectiveNode(directive, this));
			added = true;
		} else { // If between children...
			for(int i=0; i<this.children.size();i++){
				DirectiveTreeDirectiveNode directiveTreeDirectiveNode = this.children.get(i);
				Map.Entry<Integer, Integer> directiveNodeRange
					= getDirectiveRange(directiveTreeDirectiveNode.getDirective());
				if(directiveNodeRange.getKey() < directiveRange.getKey()
						&&  directiveNodeRange.getValue() > directiveRange.getValue()){
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

		if(!added ){ //If across children...
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
		for(DirectiveTreeDirectiveNode dirNode : this.children){
			toReturn.addAll(dirNode.getAllDirectives());
		}
		return toReturn;
	}

	@Override
	protected List<DirectiveTreeDirectiveNode> getLeafNodes(){
		List<DirectiveTreeDirectiveNode> toReturn = new ArrayList<DirectiveTreeDirectiveNode>();
		for(DirectiveTreeDirectiveNode dirNode : this.children){
			toReturn.addAll(dirNode.getLeafNodes());
		}
		return toReturn;
	}
}
