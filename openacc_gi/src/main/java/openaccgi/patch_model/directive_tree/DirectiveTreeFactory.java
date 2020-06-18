package openaccgi.patch_model.directive_tree;

import openaccgi.patch_model.ADirective;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DirectiveTreeFactory {
	private DirectiveTreeFactory(){}

	public static DirectiveTreeRoot getTree(ADirective[] dirs){
		List<ADirective> dirList = new ArrayList<ADirective>();
		for(ADirective dir : dirs){
			dirList.add(dir);
		}

		return getTree(dirList);
	}

	public static DirectiveTreeRoot getTree(Collection<ADirective> dirs){
		DirectiveTreeRoot root = new DirectiveTreeRoot();
		for(ADirective dir : dirs){
			root.addDirective(dir);
		}

		return root;
	}
}
