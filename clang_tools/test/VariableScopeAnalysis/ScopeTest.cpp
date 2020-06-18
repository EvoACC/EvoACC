
#include <clang/VariableScopeAnalysis/Scope.h>
#include "clang/Tooling/Tooling.h"
#include "gtest/gtest.h"

using namespace clang;
using namespace std;

TEST(Scope, basicUsage){
    
    SourceCodeLocation loc = {0,0, 100, 0};
    
    Scope* scope = new Scope(loc);
    string var1 = "variable";
    SourceCodeLocation var1Loc = {1, 10, 1, 15};
    scope->addElement(new VariableAssigned(var1, var1Loc));
    scope->addElement(new VariableRead(var1, var1Loc));
    string var2 = "anotherVariable";
    SourceCodeLocation var2Loc = {2,3, 2, 15};
    scope->addElement(new VariableDeclared(var2, var2Loc, false,true, true, false, false, false));
    scope->addElement(new VariableAssigned(var2, var2Loc));
    scope->addElement(new VariableRead(var2, var2Loc));
    scope->addElement(new VariableUsedInFunction(var2, var2Loc));
    
    SourceCodeLocation subLoc = {10,0, 13, 15};
    
    Scope* subScope1 = new Scope(subLoc);
    string var3= "variable2";
    SourceCodeLocation var3Loc = {10,15, 11,12};
    subScope1->addElement(new VariableDeclared(var3, var3Loc, true,false, false, false, false, true));
    scope->addElement(subScope1);
    
    SourceCodeLocation subsubloc = {11,6,12,4};
    Scope* subsubScope1 = new Scope(subsubloc);
    subScope1->addElement(subsubScope1);
    
    SourceCodeLocation sub2loc = {14, 4, 20, 0};
    
    Scope* subScope2 = new Scope(sub2loc);
    string var4= "asdfasdf";
    SourceCodeLocation var4Loc = {16,10 , 16,11};
    subScope2->addElement(new VariableRead(var4, var4Loc));
    scope->addElement(subScope2);
    
    EXPECT_EQ(0, scope->getSourceCodeLocation().startLineNo);
    EXPECT_EQ(0, scope->getSourceCodeLocation().startColNo);
    
    EXPECT_EQ(100, scope->getSourceCodeLocation().endLineNo);
    EXPECT_EQ(0, scope->getSourceCodeLocation().endColNo);
    EXPECT_EQ(8, scope->getElements().size());
    VariableAssigned* va;
    ASSERT_TRUE(va = dyn_cast<VariableAssigned>(scope->getElements().at(0)));
    EXPECT_EQ(var1, va->getVariable());
    EXPECT_EQ(1, va->getSourceCodeLocation().startLineNo);
    EXPECT_EQ(10, va->getSourceCodeLocation().startColNo);
    VariableRead* vr;
    ASSERT_TRUE(vr = dyn_cast<VariableRead>(scope->getElements().at(1)));
    EXPECT_EQ(var1, vr->getVariable());
    EXPECT_EQ(1, vr->getSourceCodeLocation().startLineNo);
    EXPECT_EQ(10, vr->getSourceCodeLocation().startColNo);
    VariableDeclared* vd;
    ASSERT_TRUE(vd = dyn_cast<VariableDeclared>(scope->getElements().at(2)));
    EXPECT_EQ(var2, vd->getVariable());
    EXPECT_FALSE(vd->isExtern());
    EXPECT_TRUE(vd->isPointer());
    EXPECT_TRUE(vd->isReference());
    EXPECT_FALSE(vd->isFunctionParameter());
    EXPECT_EQ(2, vd->getSourceCodeLocation().startLineNo);
    EXPECT_EQ(3, vd->getSourceCodeLocation().startColNo);
    ASSERT_TRUE(va = dyn_cast<VariableAssigned>(scope->getElements().at(3)));
    EXPECT_EQ(var2, va->getVariable());
    EXPECT_EQ(2, va->getSourceCodeLocation().startLineNo);
    EXPECT_EQ(3, va->getSourceCodeLocation().startColNo);
    ASSERT_TRUE(vr = dyn_cast<VariableRead>(scope->getElements().at(4)));
    EXPECT_EQ(var2, vr->getVariable());
    EXPECT_EQ(2, vr->getSourceCodeLocation().startLineNo);
    EXPECT_EQ(3, vr->getSourceCodeLocation().startColNo);
    VariableUsedInFunction* vu;
    ASSERT_TRUE(vu = dyn_cast<VariableUsedInFunction>(scope->getElements().at(5)));
    EXPECT_EQ(var2, vu->getVariable());
    EXPECT_EQ(2, vu->getSourceCodeLocation().startLineNo);
    EXPECT_EQ(3, vu->getSourceCodeLocation().startColNo);
    
    Scope* s;
    ASSERT_TRUE(s = dyn_cast<Scope>(scope->getElements().at(6)));
    EXPECT_EQ(s, subScope1);
    
    ASSERT_TRUE(s = dyn_cast<Scope>(scope->getElements().at(7)));
    EXPECT_EQ(s, subScope2);
    
    EXPECT_EQ(10, subScope1->getSourceCodeLocation().startLineNo);
    EXPECT_EQ(0, subScope1->getSourceCodeLocation().startColNo);
    EXPECT_EQ(13, subScope1->getSourceCodeLocation().endLineNo);
    EXPECT_EQ(15, subScope1->getSourceCodeLocation().endColNo);
    EXPECT_EQ(2, subScope1->getElements().size());
    ASSERT_TRUE(vd = dyn_cast<VariableDeclared>(subScope1->getElements().at(0)));
    EXPECT_EQ(var3, vd->getVariable());
    EXPECT_TRUE(vd->isExtern());
    EXPECT_FALSE(vd->isPointer());
    EXPECT_FALSE(vd->isReference());
    EXPECT_TRUE(vd->isFunctionParameter());
    EXPECT_EQ(10, vd->getSourceCodeLocation().startLineNo);
    EXPECT_EQ(15, vd->getSourceCodeLocation().startColNo);
    ASSERT_TRUE(s = dyn_cast<Scope>(subScope1->getElements().at(1)));
    EXPECT_EQ(s, subsubScope1);
    
    EXPECT_EQ(11, subsubScope1->getSourceCodeLocation().startLineNo);
    EXPECT_EQ(6, subsubScope1->getSourceCodeLocation().startColNo);
    EXPECT_EQ(12, subsubScope1->getSourceCodeLocation().endLineNo);
    EXPECT_EQ(4, subsubScope1->getSourceCodeLocation().endColNo);
    
    EXPECT_EQ(1, subScope2->getElements().size());
    EXPECT_EQ(14, subScope2->getSourceCodeLocation().startLineNo);
    EXPECT_EQ(4, subScope2->getSourceCodeLocation().startColNo);
    EXPECT_EQ(20, subScope2->getSourceCodeLocation().endLineNo);
    EXPECT_EQ(0, subScope2->getSourceCodeLocation().endColNo);
    ASSERT_TRUE(vr = dyn_cast<VariableRead>(subScope2->getElements().at(0)));
    EXPECT_EQ(var4, vr->getVariable());
    EXPECT_EQ(16, vr->getSourceCodeLocation().startLineNo);
    EXPECT_EQ(10, vr->getSourceCodeLocation().startColNo);
    
    EXPECT_EQ(scope, subScope1->getParent());
    EXPECT_EQ(scope, subScope2->getParent());
    EXPECT_EQ(subScope1, subsubScope1->getParent());
    EXPECT_EQ(scope, subsubScope1->getRoot());
    
    delete scope->getRoot();
}

//TODO: This whole thing could be better tested. Not yet tested fail cases
TEST(Scope, addAScopeTest){
    SourceCodeLocation loc = {0,0, 100, 0};
    
    Scope* scope = new Scope(loc);
    string var = "aVariable";
    SourceCodeLocation varLoc = {2,3, 2, 15};
    scope->addElement(new VariableDeclared(var, varLoc, false,true, true, false, false, false));
    
    SourceCodeLocation subLoc = {5,0,50,0};
    Scope* subScope = new Scope(subLoc);
    
    SourceCodeLocation varLoc2 = {7,3,7,11};
    subScope->addElement(new VariableAssigned(var, varLoc2));
    
    scope->addElement(subScope);
    
    SourceCodeLocation varLoc3 = {60,3,61,12};
    scope->addElement(new VariableRead(var,varLoc3));
    
    SourceCodeLocation newScopeLoc = {6,3,40,0};
    ASSERT_TRUE(scope->addScope(newScopeLoc));
    
    EXPECT_EQ(3, scope->getElements().size());
    
    VariableDeclared* vd;
    ASSERT_TRUE(vd = llvm::dyn_cast<VariableDeclared>(scope->getElements().at(0)));
    EXPECT_EQ(var, vd->getVariable());
    EXPECT_EQ(varLoc, vd->getSourceCodeLocation());
    
    EXPECT_EQ(subScope, scope->getElements().at(1));
    
    EXPECT_EQ(1, subScope->getElements().size());
    Scope* newScope;
    ASSERT_TRUE(newScope = dyn_cast<Scope>(subScope->getElements().at(0)));
    EXPECT_EQ(newScopeLoc, newScope->getSourceCodeLocation());
    
    EXPECT_EQ(1, newScope->getElements().size());
    VariableAssigned* va;
    ASSERT_TRUE(va = dyn_cast<VariableAssigned>(newScope->getElements().at(0)));
    EXPECT_EQ(var, va->getVariable());
    EXPECT_EQ(varLoc2, va->getSourceCodeLocation());
    
    VariableRead* vr;
    ASSERT_TRUE(vr = dyn_cast<VariableRead>(scope->getElements().at(2)));
    EXPECT_EQ(var, vr->getVariable());
    EXPECT_EQ(varLoc3, vr->getSourceCodeLocation());
    
    delete scope->getRoot();
}

TEST(Scope, addAScopeTest_SameInsertionPoint_Outer){
    SourceCodeLocation loc = {0,0, 100, 0};
    
    Scope* scope = new Scope(loc);
    string var = "aVariable";
    SourceCodeLocation varLoc = {2,3, 2, 15};
    scope->addElement(new VariableDeclared(var, varLoc, false,true, true, false, false, false));
    
    SourceCodeLocation subLoc = {5,0,50,0};
    Scope* subScope = new Scope(subLoc);
    
    SourceCodeLocation varLoc2 = {7,3,7,11};
    subScope->addElement(new VariableAssigned(var, varLoc2));
    
    scope->addElement(subScope);
    
    SourceCodeLocation varLoc3 = {60,3,61,12};
    scope->addElement(new VariableRead(var,varLoc3));
    
    SourceCodeLocation newScopeLoc = {5,0,55,0}; //Note: same starting location as 'subLoc', but different endLoc
    ASSERT_TRUE(scope->addScope(newScopeLoc));
    
    EXPECT_EQ(3, scope->getElements().size());
    
    VariableDeclared* vd;
    ASSERT_TRUE(vd = llvm::dyn_cast<VariableDeclared>(scope->getElements().at(0)));
    EXPECT_EQ(var, vd->getVariable());
    EXPECT_EQ(varLoc, vd->getSourceCodeLocation());
    
    Scope* newScope;
    ASSERT_TRUE(newScope = dyn_cast<Scope>(scope->getElements().at(1)));
    EXPECT_EQ(newScopeLoc, newScope->getSourceCodeLocation());
    EXPECT_EQ(1, newScope->getElements().size());
    EXPECT_EQ(subScope, newScope->getElements().at(0));
    
    delete scope->getRoot();
}

TEST(Scope, addAScopeTest_SameInsertionPoint_Inner){
    SourceCodeLocation loc = {0,0, 100, 0};
    
    Scope* scope = new Scope(loc);
    string var = "aVariable";
    SourceCodeLocation varLoc = {2,3, 2, 15};
    scope->addElement(new VariableDeclared(var, varLoc, false,true, true, false, false, false));
    
    SourceCodeLocation subLoc = {5,0,50,0};
    Scope* subScope = new Scope(subLoc);
    
    SourceCodeLocation varLoc2 = {7,3,7,11};
    subScope->addElement(new VariableAssigned(var, varLoc2));
    
    scope->addElement(subScope);
    
    SourceCodeLocation varLoc3 = {60,3,61,12};
    scope->addElement(new VariableRead(var,varLoc3));
    
    SourceCodeLocation newScopeLoc = {5,0,49,300}; //Note: same starting location as 'subLoc', but different endLoc
    ASSERT_TRUE(scope->addScope(newScopeLoc));
    
    EXPECT_EQ(3, scope->getElements().size());
    
    VariableDeclared* vd;
    ASSERT_TRUE(vd = llvm::dyn_cast<VariableDeclared>(scope->getElements().at(0)));
    EXPECT_EQ(var, vd->getVariable());
    EXPECT_EQ(varLoc, vd->getSourceCodeLocation());
    
    EXPECT_EQ(subScope, scope->getElements().at(1));
    EXPECT_EQ(1, subScope->getElements().size());
    Scope* newScope;
    ASSERT_TRUE(newScope = dyn_cast<Scope>(subScope->getElements().at(0)));
    EXPECT_EQ(newScopeLoc, newScope->getSourceCodeLocation());
    
    delete scope->getRoot();
}

TEST(Scope, addAScopeTest_SameEndpoint){
    SourceCodeLocation loc = {0,0, 100, 0};
    
    Scope* scope = new Scope(loc);
    string var = "aVariable";
    SourceCodeLocation varLoc = {2,3, 2, 15};
    scope->addElement(new VariableDeclared(var, varLoc, false,true, true, false, false, false));
    
    SourceCodeLocation subLoc = {5,0,50,0};
    Scope* subScope = new Scope(subLoc);
    
    SourceCodeLocation varLoc2 = {7,3,7,11};
    subScope->addElement(new VariableAssigned(var, varLoc2));
    
    scope->addElement(subScope);
    
    SourceCodeLocation varLoc3 = {60,3,61,12};
    scope->addElement(new VariableRead(var,varLoc3));
    
    SourceCodeLocation newScopeLoc = {3,0,50,0}; //Note: same ending location as 'subLoc', but different startLoc
    ASSERT_TRUE(scope->addScope(newScopeLoc));
    
    EXPECT_EQ(3, scope->getElements().size());
    
    VariableDeclared* vd;
    ASSERT_TRUE(vd = llvm::dyn_cast<VariableDeclared>(scope->getElements().at(0)));
    EXPECT_EQ(var, vd->getVariable());
    EXPECT_EQ(varLoc, vd->getSourceCodeLocation());
    
    Scope* newScope;
    ASSERT_TRUE(newScope = dyn_cast<Scope>(scope->getElements().at(1)));
    EXPECT_EQ(newScopeLoc, newScope->getSourceCodeLocation());
    EXPECT_EQ(1, newScope->getElements().size());
    EXPECT_EQ(subScope, newScope->getElements().at(0));
    
    delete scope->getRoot();
}

TEST(Scope, deleteScopeTest){
    SourceCodeLocation loc = {0,0, 100, 0};
    
    Scope* scope = new Scope(loc);
    string var = "aVariable";
    SourceCodeLocation varLoc = {2,3, 2, 15};
    scope->addElement(new VariableDeclared(var, varLoc, false,true, true, false, false, false));
    
    SourceCodeLocation subLoc = {5,0,50,0};
    Scope* subScope = new Scope(subLoc);
    
    SourceCodeLocation varLoc2 = {7,3,7,11};
    subScope->addElement(new VariableAssigned(var, varLoc2));
    
    scope->addElement(subScope);
    
    SourceCodeLocation varLoc3 = {60,3,61,12};
    scope->addElement(new VariableRead(var,varLoc3));
    
    SourceCodeLocation newScopeLoc = {6,3,40,0};
    ASSERT_TRUE(scope->addScope(newScopeLoc));
    
    ASSERT_TRUE(scope->removeScope(newScopeLoc));
    
    ASSERT_EQ(3, scope->getElements().size());
    VariableDeclared* vd;
    ASSERT_TRUE(vd = dyn_cast<VariableDeclared>(scope->getElements().at(0)));
    EXPECT_EQ(var, vd->getVariable());
    EXPECT_EQ(varLoc, vd->getSourceCodeLocation());
    
    ASSERT_EQ(subScope, scope->getElements().at(1));
    ASSERT_EQ(1, subScope->getElements().size());
    VariableAssigned* va;
    ASSERT_TRUE(va = dyn_cast<VariableAssigned>(subScope->getElements().at(0)));
    EXPECT_EQ(varLoc2, va->getSourceCodeLocation());
    EXPECT_EQ(var, va->getVariable());
    
    VariableRead* vr;
    ASSERT_TRUE(vr = dyn_cast<VariableRead>(scope->getElements().at(2)));
    EXPECT_EQ(varLoc3, vr->getSourceCodeLocation());
    EXPECT_EQ(var, vr->getVariable());
}

TEST(Scope, SourceCodeLocation_LessThan){
    SourceCodeLocation one = {1,0,1,10};
    SourceCodeLocation two = {2,0,2,10};
    
    EXPECT_TRUE(one < two);
    EXPECT_FALSE(two < one);
    
    SourceCodeLocation three = {3,0,3,11};
    SourceCodeLocation four = {3,12,3,50};
    
    EXPECT_TRUE(three < four);
    EXPECT_FALSE(four < three);
}

TEST(Scope, SourceCodeLocation_GreaterThan){
    SourceCodeLocation one = {1,0,1,10};
    SourceCodeLocation two = {2,0,2,10};
    
    EXPECT_FALSE(one > two);
    EXPECT_TRUE(two > one);
    
    SourceCodeLocation three = {3,0,3,11};
    SourceCodeLocation four = {3,12,3,50};
    
    EXPECT_FALSE(three > four);
    EXPECT_TRUE(four > three);
}

TEST(Scope, SourceCodeLocation_Equal){
    SourceCodeLocation one = {1,2,3,4};
    SourceCodeLocation two = {1,2,3,4};
    SourceCodeLocation three = {2,3,4,5};
    
    EXPECT_TRUE(one == two);
    EXPECT_TRUE(two == one);
    EXPECT_FALSE(one == three);
    EXPECT_FALSE(three == one);
    EXPECT_FALSE(two == three);
    EXPECT_FALSE(three == two);
    
    EXPECT_FALSE(one != two);
    EXPECT_FALSE(two != one);
    EXPECT_TRUE(one != three);
    EXPECT_TRUE(three != one);
    EXPECT_TRUE(two != three);
    EXPECT_TRUE(three != two);
}

TEST(Scope, SourceCodeLocation_contains){
    SourceCodeLocation one = {1,0, 10, 9};
    SourceCodeLocation two = {5,5, 10, 5};
    
    EXPECT_TRUE(one.contains(two));
    EXPECT_FALSE(two.contains(one));
}

TEST(Scope, SourceCodeLocation_feasibleInsert){
    SourceCodeLocation one = {2,0, 10, 9};
    SourceCodeLocation two = {5,5, 10, 5};
    SourceCodeLocation three = {10,10,10,50};
    SourceCodeLocation four = {1,0,1,10};
    
    EXPECT_TRUE(one.feasibleInsert(two));
    EXPECT_TRUE(two.feasibleInsert(one));
    EXPECT_TRUE(two.feasibleInsert(three));
    EXPECT_TRUE(three.feasibleInsert(two));
    EXPECT_TRUE(three.feasibleInsert(four));
    EXPECT_TRUE(four.feasibleInsert(three));
}

TEST(Scope, SourceCodeLoation_infeasibleInsert){
    SourceCodeLocation two = {5,5, 10, 5};
    SourceCodeLocation three = {10,0,10,50};
    
    EXPECT_FALSE(two.feasibleInsert(three));
    EXPECT_FALSE(three.feasibleInsert(two));
}

TEST(Scope, toString_basic){
    SourceCodeLocation loc = {0,0, 100, 0};
    
    Scope* scope = new Scope(loc);
    string var1 = "variable";
    SourceCodeLocation var1Loc = {1, 10, 1, 15};
    scope->addElement(new VariableAssigned(var1, var1Loc));
    scope->addElement(new VariableRead(var1, var1Loc));
    string var2 = "anotherVariable";
    SourceCodeLocation var2Loc = {2,3, 2, 15};
    scope->addElement(new VariableDeclared(var2, var2Loc, false,true, true, false, false, false));
    scope->addElement(new VariableAssigned(var2, var2Loc));
    scope->addElement(new VariableRead(var2, var2Loc));
    scope->addElement(new VariableUsedInFunction(var2, var2Loc));
    
    SourceCodeLocation subLoc = {10,0, 13, 15};
    
    Scope* subScope1 = new Scope(subLoc);
    string var3= "variable2";
    SourceCodeLocation var3Loc = {10,15, 11,12};
    subScope1->addElement(new VariableDeclared(var3, var3Loc, true,false, false, false, false, true));
    scope->addElement(subScope1);
    
    SourceCodeLocation subsubloc = {11,6,12,4};
    Scope* subsubScope1 = new Scope(subsubloc);
    subScope1->addElement(subsubScope1);
    
    SourceCodeLocation sub2loc = {14, 4, 20, 0};
    
    Scope* subScope2 = new Scope(sub2loc);
    string var4= "asdfasdf";
    SourceCodeLocation var4Loc = {16,10 , 16,11};
    subScope2->addElement(new VariableRead(var4, var4Loc));
    scope->addElement(subScope2);
    
    SourceCodeLocation newScopeLoc = {1,0,21,1};
    ASSERT_TRUE(scope->addScope(newScopeLoc));
    
    std::string expected = "scope [Root] (0:0,100:0)\n";
    expected += "---scope [Not Root] (1:0,21:1)\n";
    expected += "------assigned [variable] (1:10,1:15)\n";
    expected += "------read [variable] (1:10,1:15)\n";
    expected += "------declared [anotherVariable] (2:3,2:15)\n";
    expected += "------assigned [anotherVariable] (2:3,2:15)\n";
    expected += "------read [anotherVariable] (2:3,2:15)\n";
    expected += "------function [anotherVariable] (2:3,2:15)\n";
    expected += "------scope [Not Root] (10:0,13:15)\n";
    expected += "---------declared [variable2] (10:15,11:12)\n";
    expected += "---------scope [Not Root] (11:6,12:4)\n";
    expected += "------scope [Not Root] (14:4,20:0)\n";
    expected += "---------read [asdfasdf] (16:10,16:11)\n";
    
    EXPECT_EQ(expected, scope->toString());
}

TEST(Scope, declared_static_true){
    SourceCodeLocation varLoc = {1, 10, 1, 15};
    VariableDeclared vd("test", varLoc, false, false, false, true, false, false);
    EXPECT_TRUE(vd.isStatic());
}

TEST(Scope, declared_static_false){
    SourceCodeLocation varLoc = {1, 10, 1, 15};
    VariableDeclared vd("test", varLoc, false, false, false, false, false, false);
    EXPECT_FALSE(vd.isStatic());
}

TEST(Scope, declared_array_true){
    SourceCodeLocation varLoc = {1, 10, 1, 15};
    VariableDeclared vd("test", varLoc, false, false, false, true, true, false);
    EXPECT_TRUE(vd.isArray());
}

TEST(Scope, declared_array_false){
    SourceCodeLocation varLoc = {1, 10, 1, 15};
    VariableDeclared vd("test", varLoc, false, false, false, true, false, false);
    EXPECT_FALSE(vd.isArray());
}

