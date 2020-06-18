package openaccgi.patch_model.directive_tree;

import openaccgi.patch_model.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;


public class DirectiveTreeTest {
	private static File f1;
	private static File f2;
	private static File f3;

	@BeforeClass
	public static void before(){
		try{
			f1 = File.createTempFile("AtempFile","");
			f2 = File.createTempFile("BtempFile","");
			f3 = File.createTempFile("CtempFile","");
		}catch(IOException e){
			e.printStackTrace();
			System.exit(-1);
		}
	}

	@AfterClass
	public static void after(){
		f1.delete();
		f2.delete();
		f3.delete();
	}

	private static Set<ADirective> getFile1Directives(MockToolUtils toolUtils){
		/*
		f1
		`-- OpenACCDataDirective (1--70) Copy(x)
		    `-- OpenACCParallelLoop (2--10) Copy(x,y)
		    `-- OpenACCParallelLoop (20--50) Copy(x)
		    `-- OpenACCUpdateDirective (15) Update_host(X)
		    `-- OpenACCUpdateDirective (18) Update_device(X)

		 Note: Nothing should be changed when we "Remove Useless Elements" (all elements are useful)
		 */

		Set<ADirective> toReturn = new HashSet<ADirective>();

		Map<String, OpenACCUpdateDirective.UPDATE_TYPE> updateTypes1 =
			new HashMap<String, OpenACCUpdateDirective.UPDATE_TYPE>();
		updateTypes1.put("x",OpenACCUpdateDirective.UPDATE_TYPE.HOST);
		OpenACCUpdateDirective updateDirective1 =  new OpenACCUpdateDirective(15, f1, updateTypes1);

		Map<String, OpenACCUpdateDirective.UPDATE_TYPE> updateTypes2 =
			new HashMap<String, OpenACCUpdateDirective.UPDATE_TYPE>();
		updateTypes2.put("x", OpenACCUpdateDirective.UPDATE_TYPE.DEVICE);
		OpenACCUpdateDirective updateDirective2 = new OpenACCUpdateDirective(18, f1, updateTypes2);

		Set<OpenACCUpdateDirective> updateDirectives = new HashSet<OpenACCUpdateDirective>();
		updateDirectives.add(updateDirective1);
		updateDirectives.add(updateDirective2);

		Map<String, VARIABLE_STATUS> vars = new HashMap<String, VARIABLE_STATUS>();
		vars.put("x", VARIABLE_STATUS.COPY);
		OpenACCDataDirective dataDirective = new OpenACCDataDirective(1,70,f1,vars,
			new HashMap<String, Map.Entry<String, String>>(), updateDirectives);

		Map<String, VARIABLE_STATUS> loop1Vars = new HashMap<String, VARIABLE_STATUS>();
		loop1Vars.put("x", VARIABLE_STATUS.COPY);
		loop1Vars.put("y", VARIABLE_STATUS.COPY);
		OpenACCParallelLoopDirective loop1 = new OpenACCParallelLoopDirective(2,f1,loop1Vars,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty());

		Map<String, VARIABLE_STATUS> loop2Vars = new HashMap<String, VARIABLE_STATUS>();
		loop2Vars.put("x", VARIABLE_STATUS.COPY);
		OpenACCParallelLoopDirective loop2 = new OpenACCParallelLoopDirective(20, f1, loop2Vars,
			new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty());

		toReturn.add(dataDirective);
		toReturn.add(loop1);
		toReturn.add(loop2);

		SortedSet<DataDirectiveInsertionPoint> insertionPoints = new TreeSet<DataDirectiveInsertionPoint>();
		insertionPoints.add(new DataDirectiveInsertionPoint(f1,1,70));
		toolUtils.addDataDirectiveInsertionPoint(f1,insertionPoints);

		Map<String, VARIABLE_STATUS> varsAlt = new HashMap<String, VARIABLE_STATUS>();
		vars.put("x", VARIABLE_STATUS.COPY);
		OpenACCDataDirective dataDirectiveAlt = new OpenACCDataDirective(1,70,f1,varsAlt,
			new HashMap<String, Map.Entry<String, String>>(), new HashSet<OpenACCUpdateDirective>());

		toolUtils.addDataDirective(f1,1,70, Optional.of(dataDirectiveAlt));

		toolUtils.addDataInfo(f1,2,loop1Vars);
		toolUtils.addDataInfo(f1,20,loop2Vars);
		toolUtils.addDataInfo(f1,1,dataDirective.getVariables());


		return toReturn;
	}

	private static Set<ADirective> getFile2Directives(MockToolUtils toolUtils){
		/*
		f2
		`-- OpenACCDataDirective (10--20) Copy(x,y) [To be deactivated]
		    `-- OpenACCDataDirective(12--14) Copy(x,y) [To be deactivated]
		    `-- OpenACCUpdateDirective(15) Update_host(x,y) [To be deactivated]
		    `-- OpenACCUpdateDirective(16) Update_device(x,y) [To be deactivated]
		    `-- OpenACCDataDirective(17--19) Copy(x) [To be deactivated]
		`-- OpenACCDataDirective(25--50) Copy(x)
		    `-- OpenACCParallelLoop (26--28) Copy(x)
		    `-- OpenACCParallelLoop (29--31) Copy(x)
		    `-- OpenACCDataDirective (32--45) Copy(x,y) [To be deactivated]
		        `-- OpenACCParallelLoop (33--36) Copy(x)
		        `-- OpenACCParallelLoop (38--43) Copy(y)
		 */
		Set<ADirective> toReturn = new HashSet<ADirective>();

		Map<String, OpenACCUpdateDirective.UPDATE_TYPE> updateDirectiveType1 =
				new HashMap<String, OpenACCUpdateDirective.UPDATE_TYPE>();
		updateDirectiveType1.put("x", OpenACCUpdateDirective.UPDATE_TYPE.HOST);
		updateDirectiveType1.put("y",OpenACCUpdateDirective.UPDATE_TYPE.HOST);
		OpenACCUpdateDirective updateDirective1 = new OpenACCUpdateDirective(15,f2,updateDirectiveType1);

		Map<String, OpenACCUpdateDirective.UPDATE_TYPE> updateDirectiveType2 =
				new HashMap<String, OpenACCUpdateDirective.UPDATE_TYPE>();
		updateDirectiveType2.put("x", OpenACCUpdateDirective.UPDATE_TYPE.DEVICE);
		updateDirectiveType2.put("y", OpenACCUpdateDirective.UPDATE_TYPE.DEVICE);
		OpenACCUpdateDirective updateDirective2 = new OpenACCUpdateDirective(16,f2,updateDirectiveType2);

		Set<OpenACCUpdateDirective> updateDirectives = new HashSet<OpenACCUpdateDirective>();
		updateDirectives.add(updateDirective1);
		updateDirectives.add(updateDirective2);

		Map<String, VARIABLE_STATUS> vars1 = new HashMap<String, VARIABLE_STATUS>();
		vars1.put("x", VARIABLE_STATUS.COPY);
		vars1.put("y", VARIABLE_STATUS.COPY);
		OpenACCDataDirective dataDirective1 = new OpenACCDataDirective(10,20, f2,vars1,
				new HashMap<String, Map.Entry<String, String>>(), updateDirectives);

		Map<String, VARIABLE_STATUS> vars2 = new HashMap<String, VARIABLE_STATUS>();
		vars2.put("x", VARIABLE_STATUS.COPY);
		vars2.put("y", VARIABLE_STATUS.COPY);
		OpenACCDataDirective dataDirective2 = new OpenACCDataDirective(12,14,f2,vars2,
				new HashMap<String, Map.Entry<String, String>>(), new HashSet<OpenACCUpdateDirective>());

		Map<String, VARIABLE_STATUS> vars3 = new HashMap<String, VARIABLE_STATUS>();
		vars3.put("x", VARIABLE_STATUS.COPY);
		OpenACCDataDirective dataDirective3 = new OpenACCDataDirective(17,19,f2,vars3,
				new HashMap<String, Map.Entry<String, String>>(), new HashSet<OpenACCUpdateDirective>());

		Map<String, VARIABLE_STATUS> vars4 = new HashMap<String, VARIABLE_STATUS>();
		vars4.put("x", VARIABLE_STATUS.COPY);
		OpenACCDataDirective dataDirective4 = new OpenACCDataDirective(25,50,f2,vars4,
				new HashMap<String, Map.Entry<String, String>>(), new HashSet<OpenACCUpdateDirective>());

		Map<String, VARIABLE_STATUS> vars5 = new HashMap<String, VARIABLE_STATUS>();
		vars5.put("x", VARIABLE_STATUS.COPY);
		vars5.put("y", VARIABLE_STATUS.COPY);
		OpenACCDataDirective dataDirective5 = new OpenACCDataDirective(32,45,f2,vars5,
				new HashMap<String, Map.Entry<String, String>>(), new HashSet<OpenACCUpdateDirective>());

		Map<String, VARIABLE_STATUS> parVars1 = new HashMap<String,VARIABLE_STATUS>();
		parVars1.put("x", VARIABLE_STATUS.COPY);
		OpenACCParallelLoopDirective loop1 = new OpenACCParallelLoopDirective(26,f2,parVars1,
				new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty());

		Map<String, VARIABLE_STATUS> parVars2 = new HashMap<String,VARIABLE_STATUS>();
		parVars1.put("x", VARIABLE_STATUS.COPY);
		OpenACCParallelLoopDirective loop2 = new OpenACCParallelLoopDirective(29,f2,parVars2,
				new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty());

		Map<String, VARIABLE_STATUS> parVars3 = new HashMap<String,VARIABLE_STATUS>();
		parVars1.put("x", VARIABLE_STATUS.COPY);
		OpenACCParallelLoopDirective loop3 = new OpenACCParallelLoopDirective(33,f2,parVars3,
				new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty());

		Map<String, VARIABLE_STATUS> parVars4 = new HashMap<String,VARIABLE_STATUS>();
		parVars1.put("y", VARIABLE_STATUS.COPY);
		OpenACCParallelLoopDirective loop4 = new OpenACCParallelLoopDirective(38,f2,parVars4,
				new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty());

		toReturn.add(dataDirective1);
		toReturn.add(dataDirective2);
		toReturn.add(dataDirective3);
		toReturn.add(dataDirective4);
		toReturn.add(dataDirective5);
		toReturn.add(loop1);
		toReturn.add(loop2);
		toReturn.add(loop3);
		toReturn.add(loop4);

		SortedSet<DataDirectiveInsertionPoint> insertionPoints = new TreeSet<DataDirectiveInsertionPoint>();
		insertionPoints.add(new DataDirectiveInsertionPoint(f2,25,50));
		toolUtils.addDataDirectiveInsertionPoint(f2,insertionPoints);

		toolUtils.addDataDirective(f2,25,50, Optional.of(dataDirective4));

		toolUtils.addDataInfo(f2,25, vars4);
		toolUtils.addDataInfo(f2,26, parVars1);
		toolUtils.addDataInfo(f2,29, parVars2);
		toolUtils.addDataInfo(f2,33, parVars3);
		toolUtils.addDataInfo(f2,38, parVars4);

		return toReturn;
	}

	private static Set<ADirective> getFile3Directives(MockToolUtils toolUtils) {
		/*
		f3
		`--OpenACCDataDirective (1--100) Copy(x) [To be deactivated]
		   `-- OpenACCDataDirective (2--49) Copy(x) [To be deactivated]
		       `-- OpenACCParallelLoop (15--20) Copy(x)
		       `-- OpenACCParallelLoop (21--35) Copy(x) [Deactivated from the start]
		       `-- OpenACCDataDirective(36--38) Copy(x) [To be deactivated]
		   `-- OpenACCDataDirective (51--99) Copy(x) [To be deactivated]
		       `-- OpenACCDataDirective(60--80) Copy(x) [To be deactivated]
		           `--OpenACCParallelLoop(70--75) Copy(x)
		*/
		Set<ADirective> toReturn = new HashSet<ADirective>();

		Map<String, VARIABLE_STATUS> vars = new HashMap<String, VARIABLE_STATUS>();
		vars.put("x", VARIABLE_STATUS.COPY);

		OpenACCDataDirective dataDirective1 = new OpenACCDataDirective(1,100,f3,vars,
				new HashMap<String, Map.Entry<String, String>>(), new HashSet<OpenACCUpdateDirective>());

		OpenACCDataDirective dataDirective2 = new OpenACCDataDirective(2,49,f3,vars,
				new HashMap<String, Map.Entry<String, String>>(), new HashSet<OpenACCUpdateDirective>());

		OpenACCDataDirective dataDirective3 = new OpenACCDataDirective(36,38,f3,vars,
				new HashMap<String, Map.Entry<String, String>>(), new HashSet<OpenACCUpdateDirective>());

		OpenACCDataDirective dataDirective4 = new OpenACCDataDirective(51,99,f3,vars,
				new HashMap<String, Map.Entry<String, String>>(), new HashSet<OpenACCUpdateDirective>());

		OpenACCDataDirective dataDirective5 = new OpenACCDataDirective(60,80,f3,vars,
				new HashMap<String, Map.Entry<String, String>>(), new HashSet<OpenACCUpdateDirective>());

		OpenACCParallelLoopDirective loop1 = new OpenACCParallelLoopDirective(15,f3,vars,
				new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty());

		OpenACCParallelLoopDirective loop2 = new OpenACCParallelLoopDirective(21,f3,vars,
				new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty());
		loop2.setActive(false);

		OpenACCParallelLoopDirective loop3 = new OpenACCParallelLoopDirective(70,f3,vars,
				new HashMap<String, Map.Entry<String, String>>(), Optional.empty(), Optional.empty(), Optional.empty());

		toReturn.add(dataDirective1);
		toReturn.add(dataDirective2);
		toReturn.add(dataDirective3);
		toReturn.add(dataDirective4);
		toReturn.add(dataDirective5);
		toReturn.add(loop1);
		toReturn.add(loop2);
		toReturn.add(loop3);

		SortedSet<DataDirectiveInsertionPoint> insertionPoints = new TreeSet<DataDirectiveInsertionPoint>();
		toolUtils.addDataDirectiveInsertionPoint(f3,insertionPoints);

		toolUtils.addDataInfo(f3,15, vars);
		toolUtils.addDataInfo(f3,21, vars);
		toolUtils.addDataInfo(f3,70, vars);

		return toReturn;
	}

	@Test
	public void directiveTree_constructTest(){
		Set<ADirective> allDirectives = new HashSet<ADirective>();
		MockToolUtils mockToolUtils = new MockToolUtils();
		allDirectives.addAll(this.getFile1Directives(mockToolUtils));
		allDirectives.addAll(this.getFile2Directives(mockToolUtils));
		allDirectives.addAll(this.getFile3Directives(mockToolUtils));

		DirectiveTreeRoot root = DirectiveTreeFactory.getTree(allDirectives);

		assertEquals(3, root.getChildren().size());
		DirectiveTreeFileNode directiveFile1Node = root.getChildren().get(0);
		DirectiveTreeFileNode directiveFile2Node = root.getChildren().get(1);
		DirectiveTreeFileNode directiveFile3Node = root.getChildren().get(2);

		assertEquals(f1, directiveFile1Node.getFile());
		assertEquals(f2, directiveFile2Node.getFile());
		assertEquals(f3, directiveFile3Node.getFile());

		/*
		f1
		`-- OpenACCDataDirective (1--70) Copy(x)
		    `-- OpenACCParallelLoop (2--10) Copy(x,y)
		    `-- OpenACCParallelLoop (20--50) Copy(x)
		    `-- OpenACCUpdateDirective (15) Update_host(X)
		    `-- OpenACCUpdateDirective (18) Update_device(X)
		 */

		assertEquals(1, directiveFile1Node.getChildren().size());
		assertTrue(directiveFile1Node.getParent().isPresent());
		assertEquals(root, directiveFile1Node.getParent().get());
		DirectiveTreeDirectiveNode dirNode = directiveFile1Node.getChildren().get(0);
		assertTrue(dirNode.getParent().isPresent());
		assertEquals(directiveFile1Node,dirNode.getParent().get());
		assertTrue(dirNode.getDirective().getClass().equals(OpenACCDataDirective.class));
		OpenACCDataDirective dataDirective = (OpenACCDataDirective) dirNode.getDirective();
		assertTrue(dataDirective.isActive());
		assertEquals(2, dataDirective.getUpdateDirectives().size());
		for(OpenACCUpdateDirective updateDirective: dataDirective.getUpdateDirectives()){
			assertTrue(updateDirective.isActive());
		}

		assertEquals(2, dirNode.getChildren().size());
		DirectiveTreeDirectiveNode dirNodeInner = dirNode.getChildren().get(0);
		assertTrue(dirNodeInner.getParent().isPresent());
		assertEquals(dirNode, dirNodeInner.getParent().get());
		assertTrue(dirNodeInner.getDirective().getClass().equals(OpenACCParallelLoopDirective.class));
		OpenACCParallelLoopDirective loop = (OpenACCParallelLoopDirective) dirNodeInner.getDirective();
		assertEquals(2, loop.getStartLineNumber());
		assertTrue(loop.isActive());
		dirNodeInner = dirNode.getChildren().get(1);
		assertTrue(dirNodeInner.getParent().isPresent());
		assertEquals(dirNode, dirNodeInner.getParent().get());
		assertTrue(dirNodeInner.getDirective().getClass().equals(OpenACCParallelLoopDirective.class));
		loop = (OpenACCParallelLoopDirective) dirNodeInner.getDirective();
		assertEquals(20, loop.getStartLineNumber());
		assertTrue(loop.isActive());

		/*
		f2
		`-- OpenACCDataDirective (10--20) Copy(x,y) [To be deactivated]
		    `-- OpenACCDataDirective(12--14) Copy(x,y) [To be deactivated]
		    `-- OpenACCUpdateDirective(15) Update_host(x,y) [To be deactivated]
		    `-- OpenACCUpdateDirective(16) Update_device(x,y) [To be deactivated]
		    `-- OpenACCDataDirective(17--19) Copy(x) [To be deactivated]
		`-- OpenACCDataDirective(25--50) Copy(x)
		    `-- OpenACCParallelLoop (26--28) Copy(x)
		    `-- OpenACCParallelLoop (29--31) Copy(x)
		    `-- OpenACCDataDirective (32--45) Copy(x,y) [To be deactivated]
		        `-- OpenACCParallelLoop (33--36) Copy(x)
		        `-- OpenACCParallelLoop (38--43) Copy(y)
		 */

		assertEquals(2, directiveFile2Node.getChildren().size());
		assertTrue(directiveFile2Node.getParent().isPresent());
		assertEquals(root, directiveFile2Node.getParent().get());
		dirNode = directiveFile2Node.getChildren().get(0);
		assertEquals(directiveFile2Node, dirNode.getParent().get());
		assertTrue(dirNode.getDirective().getClass().equals(OpenACCDataDirective.class));
		dataDirective = (OpenACCDataDirective)dirNode.getDirective();
		assertTrue(dataDirective.isActive());
		assertEquals(2, dataDirective.getUpdateDirectives().size());
		for(OpenACCUpdateDirective updateDirective : dataDirective.getUpdateDirectives()){
			assertTrue(updateDirective.isActive());
		}

		assertEquals(2, dirNode.getChildren().size());
		dirNodeInner = dirNode.getChildren().get(0);
		assertTrue(dirNodeInner.getParent().isPresent());
		assertEquals(dirNode, dirNodeInner.getParent().get());
		assertTrue(dirNodeInner.getDirective().getClass().equals(OpenACCDataDirective.class));
		dataDirective = (OpenACCDataDirective) dirNodeInner.getDirective();
		assertTrue(dataDirective.isActive());
		assertEquals(12, dataDirective.getStartLineNumber());
		assertEquals(14, dataDirective.getEndLineNumber());

		dirNodeInner = dirNode.getChildren().get(1);
		assertTrue(dirNodeInner.getParent().isPresent());
		assertEquals(dirNode, dirNodeInner.getParent().get());
		assertTrue(dirNodeInner.getDirective().getClass().equals(OpenACCDataDirective.class));
		dataDirective = (OpenACCDataDirective) dirNodeInner.getDirective();
		assertTrue(dataDirective.isActive());
		assertEquals(17, dataDirective.getStartLineNumber());
		assertEquals(19, dataDirective.getEndLineNumber());

		dirNode = directiveFile2Node.getChildren().get(1);
		assertTrue(dirNode.getParent().isPresent());
		assertEquals(directiveFile2Node, dirNode.getParent().get());
		assertTrue(dirNode.getDirective().getClass().equals(OpenACCDataDirective.class));
		dataDirective = (OpenACCDataDirective)dirNode.getDirective();
		assertTrue(dataDirective.isActive());
		assertEquals(3,dirNode.getChildren().size());

		dirNodeInner = dirNode.getChildren().get(0);
		assertTrue(dirNodeInner.getParent().isPresent());
		assertEquals(dirNode, dirNodeInner.getParent().get());
		assertTrue(dirNodeInner.getDirective().getClass().equals(OpenACCParallelLoopDirective.class));
		loop = (OpenACCParallelLoopDirective) dirNodeInner.getDirective();
		assertTrue(loop.isActive());
		assertEquals(26,loop.getStartLineNumber());

		dirNodeInner = dirNode.getChildren().get(1);
		assertTrue(dirNodeInner.getParent().isPresent());
		assertEquals(dirNode, dirNodeInner.getParent().get());
		assertTrue(dirNodeInner.getDirective().getClass().equals(OpenACCParallelLoopDirective.class));
		loop = (OpenACCParallelLoopDirective) dirNodeInner.getDirective();
		assertTrue(loop.isActive());
		assertEquals(29,loop.getStartLineNumber());

		dirNodeInner = dirNode.getChildren().get(2);
		assertTrue(dirNodeInner.getParent().isPresent());
		assertEquals(dirNode, dirNodeInner.getParent().get());
		assertTrue(dirNodeInner.getDirective().getClass().equals(OpenACCDataDirective.class));
		dataDirective = (OpenACCDataDirective)dirNodeInner.getDirective();
		assertTrue(dataDirective.isActive());
		assertEquals(32, dataDirective.getStartLineNumber());
		assertEquals(45, dataDirective.getEndLineNumber());
		assertEquals(2, dirNodeInner.getChildren().size());

		dirNode = dirNodeInner.getChildren().get(0);
		assertTrue(dirNode.getParent().isPresent());
		assertEquals(dirNodeInner, dirNode.getParent().get());
		assertTrue(dirNode.getDirective().getClass().equals(OpenACCParallelLoopDirective.class));
		loop=(OpenACCParallelLoopDirective)dirNode.getDirective();
		assertTrue(loop.isActive());
		assertEquals(33, loop.getStartLineNumber());

		dirNode = dirNodeInner.getChildren().get(1);
		assertTrue(dirNode.getParent().isPresent());
		assertEquals(dirNodeInner, dirNode.getParent().get());
		assertTrue(dirNode.getDirective().getClass().equals(OpenACCParallelLoopDirective.class));
		loop=(OpenACCParallelLoopDirective)dirNode.getDirective();
		assertTrue(loop.isActive());
		assertEquals(38, loop.getStartLineNumber());

		/*
		f3
		`--OpenACCDataDirective (1--100) Copy(x) [To be deactivated]
		   `-- OpenACCDataDirective (2--49) Copy(x) [To be deactivated]
		       `-- OpenACCParallelLoop (15--20) Copy(x)
		       `-- OpenACCParallelLoop (21--35) Copy(x) [Deactivated from the start]
		       `-- OpenACCDataDirective(36--38) Copy(x) [To be deactivated]
		   `-- OpenACCDataDirective (51--99) Copy(x) [To be deactivated]
		       `-- OpenACCDataDirective(60--80) Copy(x) [To be deactivated]
		           `--OpenACCParallelLoop(70--75) Copy(x)
		*/

		assertEquals(1,directiveFile3Node.getChildren().size());
		assertTrue(directiveFile3Node.getParent().isPresent());
		assertEquals(root, directiveFile3Node.getParent().get());
		dirNode = directiveFile3Node.getChildren().get(0);
		assertTrue(dirNode.getParent().isPresent());
		assertEquals(directiveFile3Node, dirNode.getParent().get());
		assertTrue(dirNode.getDirective().getClass().equals(OpenACCDataDirective.class));
		dataDirective = (OpenACCDataDirective) dirNode.getDirective();
		assertTrue(dataDirective.isActive());
		assertEquals(1, dataDirective.getStartLineNumber());
		assertEquals(100, dataDirective.getEndLineNumber());

		assertEquals(2, dirNode.getChildren().size());

		dirNodeInner = dirNode.getChildren().get(0);
		assertTrue(dirNodeInner.getParent().isPresent());
		assertEquals(dirNode, dirNodeInner.getParent().get());
		assertTrue(dirNodeInner.getDirective().getClass().equals(OpenACCDataDirective.class));
		dataDirective = (OpenACCDataDirective) dirNodeInner.getDirective();
		assertTrue(dataDirective.isActive());
		assertEquals(2, dataDirective.getStartLineNumber());
		assertEquals(49, dataDirective.getEndLineNumber());

		assertEquals(3, dirNodeInner.getChildren().size());
		dirNode = dirNodeInner.getChildren().get(0);
		assertTrue(dirNode.getParent().isPresent());
		assertEquals(dirNodeInner, dirNode.getParent().get());
		assertTrue(dirNode.getDirective().getClass().equals(OpenACCParallelLoopDirective.class));
		loop = (OpenACCParallelLoopDirective)dirNode.getDirective();
		assertTrue(loop.isActive());
		assertEquals(15, loop.getStartLineNumber());

		dirNode = dirNodeInner.getChildren().get(1);
		assertTrue(dirNode.getParent().isPresent());
		assertEquals(dirNodeInner, dirNode.getParent().get());
		assertTrue(dirNode.getDirective().getClass().equals(OpenACCParallelLoopDirective.class));
		loop = (OpenACCParallelLoopDirective)dirNode.getDirective();
		assertFalse(loop.isActive()); //Is deactivated from the start
		assertEquals(21,loop.getStartLineNumber());

		dirNode = dirNodeInner.getChildren().get(2);
		assertTrue(dirNode.getParent().isPresent());
		assertEquals(dirNodeInner, dirNode.getParent().get());
		assertTrue(dirNode.getChildren().isEmpty());
		assertTrue(dirNode.getDirective().getClass().equals(OpenACCDataDirective.class));
		dataDirective = (OpenACCDataDirective)dirNode.getDirective();
		assertTrue(dataDirective.isActive());
		assertEquals(36, dataDirective.getStartLineNumber());
		assertEquals(38, dataDirective.getEndLineNumber());

		dirNodeInner = directiveFile3Node.getChildren().get(0).getChildren().get(1);
		assertTrue(dirNodeInner.getDirective().getClass().equals(OpenACCDataDirective.class));
		dataDirective = (OpenACCDataDirective)dirNodeInner.getDirective();
		assertTrue(dataDirective.isActive());
		assertEquals(51, dataDirective.getStartLineNumber());
		assertEquals(99, dataDirective.getEndLineNumber());
		assertEquals(1, dirNodeInner.getChildren().size());
		dirNode = dirNodeInner.getChildren().get(0);
		assertTrue(dirNode.getParent().isPresent());
		assertEquals(dirNodeInner, dirNode.getParent().get());

		assertTrue(dirNode.getDirective().getClass().equals(OpenACCDataDirective.class));
		dataDirective = (OpenACCDataDirective)dirNode.getDirective();
		assertTrue(dataDirective.isActive());
		assertEquals(60, dataDirective.getStartLineNumber());
		assertEquals(80, dataDirective.getEndLineNumber());
		assertEquals(1, dirNode.getChildren().size());
		dirNodeInner = dirNode.getChildren().get(0);
		assertTrue(dirNodeInner.getParent().isPresent());
		assertEquals(dirNode, dirNodeInner.getParent().get());

		assertTrue(dirNodeInner.getDirective().getClass().equals(OpenACCParallelLoopDirective.class));
		loop = (OpenACCParallelLoopDirective) dirNodeInner.getDirective();
		assertTrue(loop.isActive());
		assertEquals(70, loop.getStartLineNumber());
		assertTrue(dirNodeInner.getChildren().isEmpty());
	}

	@Test
	public void directiveTree_removeUselessElementsTest(){
		Set<ADirective> allDirectives = new HashSet<ADirective>();
		MockToolUtils mockToolUtils = new MockToolUtils();
		allDirectives.addAll(this.getFile1Directives(mockToolUtils));
		allDirectives.addAll(this.getFile2Directives(mockToolUtils));
		allDirectives.addAll(this.getFile3Directives(mockToolUtils));

		Map<File, Map<Integer, Boolean>> map = new HashMap<File, Map<Integer, Boolean>>();
		map.put(f1, new HashMap<Integer, Boolean>());
		map.put(f2, new HashMap<Integer, Boolean>());
		map.put(f3, new HashMap<Integer, Boolean>());

		map.get(f1).put(1, true);
		map.get(f1).put(2, true);
		map.get(f1).put(20, true);
		map.get(f1).put(15, true);
		map.get(f1).put(18, true);

		map.get(f2).put(10, false);
		map.get(f2).put(12, false);
		map.get(f2).put(15, false);
		map.get(f2).put(16, false);
		map.get(f2).put(17, false);
		map.get(f2).put(25, true);
		map.get(f2).put(26, true);
		map.get(f2).put(29, true);
		map.get(f2).put(32, false);
		map.get(f2).put(33, true);
		map.get(f2).put(38, true);

		map.get(f3).put(1, false);
		map.get(f3).put(2, false);
		map.get(f3).put(15, true);
		map.get(f3).put(21, false);
		map.get(f3).put(36, false);
		map.get(f3).put(51, false);
		map.get(f3).put(60, false);
		map.get(f3).put(70, true);

		DirectiveTreeRoot root = DirectiveTreeFactory.getTree(allDirectives);
		root.removeUselessElements(mockToolUtils);

		for(ADirective dir : allDirectives){
			assertTrue(map.get(dir.getFile())
					.get(((OpenACCDataDirective)dir).getStartLineNumber())==dir.isActive());
		}

	}
}
