#include "clang/VariableScopeAnalysis/VariableScopeAnalysis.h"
#include "gtest/gtest.h"
#include <iostream>
#include <fstream>
#include <string>
#include <iostream>

using namespace llvm;
using namespace clang;
using namespace std;

TEST(VariableScopeAnalysis, run_basicProgram)
{
    string input = "int global_1 = 0;\n";                 //1
    input += "extern int global_2;\n";                    //2
    input += "int add(int a, int b){\n";                  //3
    input += "return a + b;\n";                           //4
    input += "}\n";                                       //5
    input += "int plusOne(int a){\n";                     //6
    input += "return a + 1;\n";                           //7
    input += "}\n";                                       //8
    input += "int function_1(int input){\n";              //9
    input += "int* pointer;\n";                           //10
    input += "int toReturn = 0;\n";                       //11
    input += "if(true){\n";                               //12
    input += "int a = 0;\n";                              //13
    input += "toReturn++;\n";                             //14
    input += "}\n";                                       //15
    input += "for(int i=0; i < 10; i++){\n";              //16
    input += "int bla[20]; //Not initialised\n";          //17
    input += "for(int j=0; j < 20; j++){\n";              //18
    input += "toReturn = add(plusOne(bla[j]), input);\n"; //19
    input += "bla[j] = 6;\n";                             //20
    input += "}\n";                                       //21
    input += "*(pointer+1) = bla[i];\n";                  //22
    input += "}\n";                                       //23
    input += "return toReturn * global_1;\n";             //24
    input += "}";                                         //25
    
    set<string> temp;
    Scope* scope = VariableScopeAnalysis::run(input, CPP,temp);
    VariableDeclared* vd;
    VariableAssigned* va;
    VariableRead* vr;
    VariableUsedInFunction* vf;
    
    ASSERT_TRUE(scope);
    EXPECT_EQ(6, scope->getElements().size());;
    
    //int global_1 = 0;
    ASSERT_TRUE(vd = dyn_cast<VariableDeclared>(scope->getElements().at(0)));
    EXPECT_TRUE(vd->isExtern());
    EXPECT_FALSE(vd->isPointer());
    EXPECT_FALSE(vd->isReference());
    EXPECT_FALSE(vd->isFunctionParameter());
    EXPECT_FALSE(vd->isStatic());
    EXPECT_FALSE(vd->isArray());
    EXPECT_EQ("global_1", vd->getVariable());
    EXPECT_EQ(1, vd->getSourceCodeLocation().startLineNo);
    EXPECT_EQ(1, vd->getSourceCodeLocation().startColNo);
    ASSERT_TRUE(va = dyn_cast<VariableAssigned>(scope->getElements().at(1)));
    EXPECT_EQ("global_1", va->getVariable());
    
    //extern int global_2;
    ASSERT_TRUE(vd = dyn_cast<VariableDeclared>(scope->getElements().at(2)));
    EXPECT_TRUE(vd->isExtern());
    EXPECT_FALSE(vd->isPointer());
    EXPECT_FALSE(vd->isReference());
    EXPECT_FALSE(vd->isFunctionParameter());
    EXPECT_FALSE(vd->isStatic());
    EXPECT_FALSE(vd->isArray());
    EXPECT_EQ("global_2", vd->getVariable());
    EXPECT_EQ(2, vd->getSourceCodeLocation().startLineNo);
    EXPECT_EQ(1, vd->getSourceCodeLocation().startColNo);
    
    //int add(int a, int b) { ... }
    ASSERT_TRUE(scope = dyn_cast<Scope>(scope->getElements().at(3)));
    EXPECT_EQ(3, scope->getElements().size());
    
    //int a
    ASSERT_TRUE(vd = dyn_cast<VariableDeclared>(scope->getElements().at(0)));
    EXPECT_EQ("a", vd->getVariable());
    EXPECT_FALSE(vd->isExtern());
    EXPECT_FALSE(vd->isPointer());
    EXPECT_FALSE(vd->isReference());
    EXPECT_TRUE(vd->isFunctionParameter());
    EXPECT_FALSE(vd->isStatic());
    EXPECT_FALSE(vd->isArray());
    
    //int b;
    ASSERT_TRUE(vd = dyn_cast<VariableDeclared>(scope->getElements().at(1)));
    EXPECT_EQ("b", vd->getVariable());
    EXPECT_FALSE(vd->isExtern());
    EXPECT_FALSE(vd->isPointer());
    EXPECT_FALSE(vd->isReference());
    EXPECT_TRUE(vd->isFunctionParameter());
    EXPECT_FALSE(vd->isStatic());
    EXPECT_FALSE(vd->isArray());
    
    //{ ... }
    ASSERT_TRUE(scope = dyn_cast<Scope>(scope->getElements().at(2)));
    EXPECT_EQ(2, scope->getElements().size());
    
    //return a + b;
    ASSERT_TRUE(vr = dyn_cast<VariableRead>(scope->getElements().at(0)));
    EXPECT_EQ("a", vr->getVariable());
    EXPECT_FALSE(vd->isPointer());
    EXPECT_EQ(3, vd->getSourceCodeLocation().startLineNo);
    EXPECT_EQ(16, vd->getSourceCodeLocation().startColNo);
    ASSERT_TRUE(vr = dyn_cast<VariableRead>(scope->getElements().at(1)));
    EXPECT_EQ("b", vr->getVariable());
    EXPECT_FALSE(vd->isPointer());
    
    scope = scope->getParent()->getParent();
    
    //int plusOne(int a){ ... }
    ASSERT_TRUE(scope = dyn_cast<Scope>(scope->getElements().at(4)));
    EXPECT_EQ(2, scope->getElements().size());
    
    //int a
    ASSERT_TRUE(vd = dyn_cast<VariableDeclared>(scope->getElements().at(0)));
    EXPECT_EQ("a", vd->getVariable());
    EXPECT_FALSE(vd->isExtern());
    EXPECT_FALSE(vd->isPointer());
    EXPECT_FALSE(vd->isReference());
    EXPECT_TRUE(vd->isFunctionParameter());
    EXPECT_FALSE(vd->isStatic());
    EXPECT_FALSE(vd->isArray());
    
    //{ ... }
    ASSERT_TRUE(scope = dyn_cast<Scope>(scope->getElements().at(1)));
    EXPECT_EQ(1, scope->getElements().size());
    
    //return a + 1;
    ASSERT_TRUE(vr = dyn_cast<VariableRead>(scope->getElements().at(0)));
    EXPECT_EQ("a", vr->getVariable());
    
    scope = scope->getParent()->getParent();
    
    //int function_1(int input){ ... }
    ASSERT_TRUE(scope = dyn_cast<Scope>(scope->getElements().at(5)));
    EXPECT_EQ(2, scope->getElements().size());
    
    //int input
    ASSERT_TRUE(vd = dyn_cast<VariableDeclared>(scope->getElements().at(0)));
    EXPECT_EQ("input", vd->getVariable());
    EXPECT_FALSE(vd->isExtern());
    EXPECT_FALSE(vd->isPointer());
    EXPECT_FALSE(vd->isReference());
    EXPECT_TRUE(vd->isFunctionParameter());
    EXPECT_FALSE(vd->isStatic());
    EXPECT_FALSE(vd->isArray());
    
    //{ ... }
    ASSERT_TRUE(scope = dyn_cast<Scope>(scope->getElements().at(1)));
    EXPECT_EQ(7, scope->getElements().size());
    
    //int* pointer;
    ASSERT_TRUE(vd = dyn_cast<VariableDeclared>(scope->getElements().at(0)));
    EXPECT_EQ("pointer", vd->getVariable());
    EXPECT_FALSE(vd->isExtern());
    EXPECT_TRUE(vd->isPointer());
    EXPECT_FALSE(vd->isReference());
    EXPECT_FALSE(vd->isFunctionParameter());
    EXPECT_FALSE(vd->isStatic());
    EXPECT_FALSE(vd->isArray());
    
    //int toReturn = 0;
    ASSERT_TRUE(vd = dyn_cast<VariableDeclared>(scope->getElements().at(1)));
    EXPECT_EQ("toReturn", vd->getVariable());
    EXPECT_FALSE(vd->isExtern());
    EXPECT_FALSE(vd->isPointer());
    ASSERT_TRUE(va = dyn_cast<VariableAssigned>(scope->getElements().at(2)));
    EXPECT_EQ("toReturn", va->getVariable());
    
    //if(true){ ... }
    ASSERT_TRUE(scope = dyn_cast<Scope>(scope->getElements().at(3)));
    EXPECT_EQ(1, scope->getElements().size());
    
    //{ ... }
    ASSERT_TRUE(scope = dyn_cast<Scope>(scope->getElements().at(0)));
    EXPECT_EQ(4, scope->getElements().size());
    
    //int a = 0;
    ASSERT_TRUE(vd = dyn_cast<VariableDeclared>(scope->getElements().at(0)));
    EXPECT_EQ("a", vd->getVariable());
    EXPECT_FALSE(vd->isExtern());
    EXPECT_FALSE(vd->isPointer());
    EXPECT_FALSE(vd->isReference());
    EXPECT_FALSE(vd->isFunctionParameter());
    ASSERT_TRUE(va = dyn_cast<VariableAssigned>(scope->getElements().at(1)));
    ASSERT_EQ("a", va->getVariable());
    EXPECT_FALSE(vd->isStatic());
    EXPECT_FALSE(vd->isArray());
    
    //toReturn++;
    ASSERT_TRUE(vr = dyn_cast<VariableRead>(scope->getElements().at(2)));
    ASSERT_EQ("toReturn", vr->getVariable());
    ASSERT_TRUE(va = dyn_cast<VariableAssigned>(scope->getElements().at(3)));
    ASSERT_EQ("toReturn", va->getVariable());
    
    scope = scope->getParent()->getParent();
    
    //for(int i=0; i < 10; i++){ ... }
    ASSERT_TRUE(scope = dyn_cast<Scope>(scope->getElements().at(4)));
    EXPECT_EQ(6, scope->getElements().size());
    
    //int i=0;
    ASSERT_TRUE(vd = dyn_cast<VariableDeclared>(scope->getElements().at(0)));
    EXPECT_EQ("i", vd->getVariable());
    EXPECT_FALSE(vd->isExtern());
    EXPECT_FALSE(vd->isPointer());
    EXPECT_FALSE(vd->isReference());
    EXPECT_FALSE(vd->isFunctionParameter());
    EXPECT_FALSE(vd->isStatic());
    EXPECT_FALSE(vd->isArray());
    ASSERT_TRUE(va = dyn_cast<VariableAssigned>(scope->getElements().at(1)));
    EXPECT_EQ("i", vd->getVariable());
    
    
    //i < 10
    ASSERT_TRUE(vr = dyn_cast<VariableRead>(scope->getElements().at(2)));
    EXPECT_EQ("i", vr->getVariable());
    
    //i++
    ASSERT_TRUE(vr = dyn_cast<VariableRead>(scope->getElements().at(3)));
    EXPECT_EQ("i", va->getVariable());
    ASSERT_TRUE(va = dyn_cast<VariableAssigned>(scope->getElements().at(4)));
    EXPECT_EQ("i", va->getVariable());
    
    //{ ... }
    ASSERT_TRUE(scope = dyn_cast<Scope>(scope->getElements().at(5)));
    ASSERT_EQ(5, scope->getElements().size());
    
    //int bla[20];
    ASSERT_TRUE(vd = dyn_cast<VariableDeclared>(scope->getElements().at(0)));
    EXPECT_EQ("bla", vd->getVariable());
    EXPECT_TRUE(vd->isArray());
    
    //for(int j=0; j < 20; j++){ ... }
    ASSERT_TRUE(scope = dyn_cast<Scope>(scope->getElements().at(1)));
    EXPECT_EQ(6, scope->getElements().size());
    
    //int j=0;
    ASSERT_TRUE(vd = dyn_cast<VariableDeclared>(scope->getElements().at(0)));
    EXPECT_EQ("j", vd->getVariable());
    ASSERT_TRUE(va = dyn_cast<VariableAssigned>(scope->getElements().at(1)));
    EXPECT_EQ("j", va->getVariable());
    
    //j < 20
    ASSERT_TRUE(vr = dyn_cast<VariableRead>(scope->getElements().at(2)));
    EXPECT_EQ("j", vr->getVariable());
    
    //j++
    ASSERT_TRUE(vr = dyn_cast<VariableRead>(scope->getElements().at(3)));
    EXPECT_EQ("j", vr->getVariable());
    ASSERT_TRUE(va = dyn_cast<VariableAssigned>(scope->getElements().at(4)));
    EXPECT_EQ("j", va->getVariable());
    
    //{ ... }
    ASSERT_TRUE(scope = dyn_cast<Scope>(scope->getElements().at(5)));
    EXPECT_EQ(6, scope->getElements().size());
    
    //toReturn = add(plusOne(bla[j]), input);
    ASSERT_TRUE(va = dyn_cast<VariableAssigned>(scope->getElements().at(0)));
    EXPECT_EQ("toReturn", va->getVariable());
    ASSERT_TRUE(vf = dyn_cast<VariableUsedInFunction>(scope->getElements().at(1)));
    EXPECT_EQ("bla", vf->getVariable());
    ASSERT_TRUE(vf = dyn_cast<VariableUsedInFunction>(scope->getElements().at(2)));
    ASSERT_EQ("j", vf->getVariable());
    ASSERT_TRUE(vf = dyn_cast<VariableUsedInFunction>(scope->getElements().at(3)));
    EXPECT_EQ("input", vf->getVariable());
    
    //bla[j] = 6;
    ASSERT_TRUE(va = dyn_cast<VariableAssigned>(scope->getElements().at(4)));
    EXPECT_EQ("bla", va->getVariable());
    ASSERT_TRUE(vr = dyn_cast<VariableRead>(scope->getElements().at(5)));
    EXPECT_EQ("j", vr->getVariable());
    
    scope = scope->getParent()->getParent();
    
    //*(pointer+1) = bla[i]
    ASSERT_TRUE(vr = dyn_cast<VariableRead>(scope->getElements().at(2)));
    EXPECT_EQ("pointer", vr->getVariable());
    ASSERT_TRUE(vr = dyn_cast<VariableRead>(scope->getElements().at(3)));
    EXPECT_EQ("bla", vr->getVariable());
    ASSERT_TRUE(vr = dyn_cast<VariableRead>(scope->getElements().at(4)));
    EXPECT_EQ("i", vr->getVariable());
    
    scope = scope->getParent()->getParent();
    
    //return toReturn * global_1;
    ASSERT_TRUE(vr = dyn_cast<VariableRead>(scope->getElements().at(5)));
    EXPECT_EQ("toReturn", vr->getVariable());
    ASSERT_TRUE(vr = dyn_cast<VariableRead>(scope->getElements().at(6)));
    EXPECT_EQ("global_1", vr->getVariable());
    
    EXPECT_EQ(scope->getRoot(), scope->getParent()->getParent());
    
    delete scope->getRoot();
}


TEST(VariableScopeAnalysis, run_specialAssignments)
{
    string input = "void func(){\n";
    input += "int a = 5;\n";
    input += "int b = 10;\n";
    input += "a+=1;\n";
    input += "b-=1;\n";
    input += "a*=b;\n";
    input += "b/=a;\n";
    input += "b++;\n";
    input += "a--;\n";
    input += "int c[5];\n";
    input += "c[a]+=b;\n";
    input += "((c[a]))+=b;\n";
    input += "}";
    
    set<string> temp;
    Scope* scope = VariableScopeAnalysis::run(input, CPP,temp);
    VariableDeclared* vd;
    VariableAssigned* va;
    VariableRead* vr;
    
    ASSERT_TRUE(scope);
    ASSERT_EQ(1, scope->getElements().size());
    ASSERT_TRUE(scope = dyn_cast<Scope>(scope->getElements().at(0)));
    
    ASSERT_EQ(1, scope->getElements().size());
    ASSERT_TRUE(scope = dyn_cast<Scope>(scope->getElements().at(0)));
    
    EXPECT_EQ(27, scope->getElements().size());
    
    //int a = 5;
    ASSERT_TRUE(vd = dyn_cast<VariableDeclared>(scope->getElements().at(0)));
    EXPECT_EQ("a", vd->getVariable());
    EXPECT_FALSE(vd->isExtern());
    EXPECT_FALSE(vd->isPointer());
    EXPECT_FALSE(vd->isReference());
    EXPECT_FALSE(vd->isFunctionParameter());
    EXPECT_FALSE(vd->isStatic());
    EXPECT_FALSE(vd->isArray());
    ASSERT_TRUE(va = dyn_cast<VariableAssigned>(scope->getElements().at(1)));
    EXPECT_EQ("a", va->getVariable());
    
    //int b = 10;
    ASSERT_TRUE(vd = dyn_cast<VariableDeclared>(scope->getElements().at(2)));
    EXPECT_EQ("b", vd->getVariable());
    EXPECT_FALSE(vd->isExtern());
    EXPECT_FALSE(vd->isPointer());
    EXPECT_FALSE(vd->isReference());
    EXPECT_FALSE(vd->isFunctionParameter());
    EXPECT_FALSE(vd->isStatic());
    EXPECT_FALSE(vd->isArray());
    ASSERT_TRUE(va = dyn_cast<VariableAssigned>(scope->getElements().at(3)));
    EXPECT_EQ("b", va->getVariable());
    
    //a+=1;
    ASSERT_TRUE(vr = dyn_cast<VariableRead>(scope->getElements().at(4)));
    EXPECT_EQ("a", vr->getVariable());
    ASSERT_TRUE(va = dyn_cast<VariableAssigned>(scope->getElements().at(5)));
    EXPECT_EQ("a", va->getVariable());
    
    //b-=1;
    ASSERT_TRUE(vr = dyn_cast<VariableRead>(scope->getElements().at(6)));
    EXPECT_EQ("b", vr->getVariable());
    ASSERT_TRUE(va = dyn_cast<VariableAssigned>(scope->getElements().at(7)));
    EXPECT_EQ("b", va->getVariable());
    
    //a*=b;
    ASSERT_TRUE(vr = dyn_cast<VariableRead>(scope->getElements().at(8)));
    EXPECT_EQ("a", vr->getVariable());
    ASSERT_TRUE(va = dyn_cast<VariableAssigned>(scope->getElements().at(9)));
    EXPECT_EQ("a", va->getVariable());
    ASSERT_TRUE(vr = dyn_cast<VariableRead>(scope->getElements().at(10)));
    EXPECT_EQ("b", vr->getVariable());
    
    //b/=a;
    ASSERT_TRUE(vr = dyn_cast<VariableRead>(scope->getElements().at(11)));
    EXPECT_EQ("b", vr->getVariable());
    ASSERT_TRUE(va = dyn_cast<VariableAssigned>(scope->getElements().at(12)));
    EXPECT_EQ("b", va->getVariable());
    ASSERT_TRUE(vr = dyn_cast<VariableRead>(scope->getElements().at(13)));
    EXPECT_EQ("a", vr->getVariable());
    
    //b++;
    ASSERT_TRUE(vr=dyn_cast<VariableRead>(scope->getElements().at(14)));
    EXPECT_EQ("b", vr->getVariable());
    ASSERT_TRUE(va = dyn_cast<VariableAssigned>(scope->getElements().at(15)));
    EXPECT_EQ("b", va->getVariable());
    
    //a--;
    ASSERT_TRUE(vr=dyn_cast<VariableRead>(scope->getElements().at(16)));
    EXPECT_EQ("a", vr->getVariable());
    ASSERT_TRUE(va = dyn_cast<VariableAssigned>(scope->getElements().at(17)));
    EXPECT_EQ("a", va->getVariable());
    
    //int c[5];
    ASSERT_TRUE(vd = dyn_cast<VariableDeclared>(scope->getElements().at(18)));
    EXPECT_EQ("c", vd->getVariable());
    EXPECT_FALSE(vd->isExtern());
    EXPECT_FALSE(vd->isPointer());
    EXPECT_FALSE(vd->isStatic());
    EXPECT_TRUE(vd->isArray());
    
    //c[a]+=b;
    ASSERT_TRUE(vr = dyn_cast<VariableRead>(scope->getElements().at(19)));
    EXPECT_EQ("c", vr->getVariable());
    ASSERT_TRUE(va = dyn_cast<VariableAssigned>(scope->getElements().at(20)));
    EXPECT_EQ("c", va->getVariable());
    ASSERT_TRUE(vr = dyn_cast<VariableRead>(scope->getElements().at(21)));
    EXPECT_EQ("a", vr->getVariable());
    ASSERT_TRUE(vr=dyn_cast<VariableRead>(scope->getElements().at(22)));
    EXPECT_EQ("b", vr->getVariable());
    
    //((c[a]))+=b;
    ASSERT_TRUE(vr = dyn_cast<VariableRead>(scope->getElements().at(23)));
    EXPECT_EQ("c", vr->getVariable());
    ASSERT_TRUE(va = dyn_cast<VariableAssigned>(scope->getElements().at(24)));
    EXPECT_EQ("c", va->getVariable());
    ASSERT_TRUE(vr = dyn_cast<VariableRead>(scope->getElements().at(25)));
    EXPECT_EQ("a", vr->getVariable());
    ASSERT_TRUE(vr=dyn_cast<VariableRead>(scope->getElements().at(26)));
    EXPECT_EQ("b", vr->getVariable());
    
    
    delete scope->getRoot();
}

TEST(VariableScopeAnalysis, run_extremeParenthesis)
{
    string input = "void funct(){\n";
    input += "int a = 5;\n";
    input += "int b = 66;\n";
    input += "int c[5] = {1,2,3,4,5};\n";
    input += "int d = -100;\n";
    input += "(((((((c[(a)]) = (((d) + b))))))));\n";
    input += "}";
    
    set<string> temp;
    Scope* scope = VariableScopeAnalysis::run(input, CPP,temp);
    VariableDeclared* vd;
    VariableAssigned* va;
    VariableRead* vr;
    
    ASSERT_TRUE(scope);
    ASSERT_EQ(1, scope->getElements().size());
    ASSERT_TRUE(scope = dyn_cast<Scope>(scope->getElements().at(0)));
    
    ASSERT_EQ(1, scope->getElements().size());
    ASSERT_TRUE(scope = dyn_cast<Scope>(scope->getElements().at(0)));
    
    EXPECT_EQ(12, scope->getElements().size());
    
    //int a = 5;
    ASSERT_TRUE(vd = dyn_cast<VariableDeclared>(scope->getElements().at(0)));
    EXPECT_EQ("a", vd->getVariable());
    EXPECT_FALSE(vd->isExtern());
    EXPECT_FALSE(vd->isPointer());
    EXPECT_FALSE(vd->isReference());
    EXPECT_FALSE(vd->isFunctionParameter());
    ASSERT_TRUE(va = dyn_cast<VariableAssigned>(scope->getElements().at(1)));
    EXPECT_EQ("a", va->getVariable());
    
    //int b = 66;
    ASSERT_TRUE(vd = dyn_cast<VariableDeclared>(scope->getElements().at(2)));
    EXPECT_EQ("b", vd->getVariable());
    EXPECT_FALSE(vd->isExtern());
    EXPECT_FALSE(vd->isPointer());
    EXPECT_FALSE(vd->isReference());
    EXPECT_FALSE(vd->isFunctionParameter());
    ASSERT_TRUE(va = dyn_cast<VariableAssigned>(scope->getElements().at(3)));
    EXPECT_EQ("b", va->getVariable());
    
    //int c[5] = {1,2,3,4,5};
    ASSERT_TRUE(vd = dyn_cast<VariableDeclared>(scope->getElements().at(4)));
    EXPECT_EQ("c", vd->getVariable());
    EXPECT_FALSE(vd->isExtern());
    EXPECT_FALSE(vd->isPointer());
    EXPECT_FALSE(vd->isReference());
    EXPECT_FALSE(vd->isFunctionParameter());
    ASSERT_TRUE(va = dyn_cast<VariableAssigned>(scope->getElements().at(5)));
    EXPECT_EQ("c", va->getVariable());
    
    //int d = -100;
    ASSERT_TRUE(vd = dyn_cast<VariableDeclared>(scope->getElements().at(6)));
    EXPECT_EQ("d", vd->getVariable());
    EXPECT_FALSE(vd->isExtern());
    EXPECT_FALSE(vd->isPointer());
    EXPECT_FALSE(vd->isReference());
    EXPECT_FALSE(vd->isFunctionParameter());
    ASSERT_TRUE(va = dyn_cast<VariableAssigned>(scope->getElements().at(7)));
    EXPECT_EQ("d", va->getVariable());
    
    //(((((((c[(a)]) = (((d) + b))))))));;
    ASSERT_TRUE(va = dyn_cast<VariableAssigned>(scope->getElements().at(8)));
    EXPECT_EQ("c", va->getVariable());
    ASSERT_TRUE(vr = dyn_cast<VariableRead>(scope->getElements().at(9)));
    EXPECT_EQ("a", vr->getVariable());
    ASSERT_TRUE(vr=dyn_cast<VariableRead>(scope->getElements().at(10)));
    EXPECT_EQ("d", vr->getVariable());
    ASSERT_TRUE(vr=dyn_cast<VariableRead>(scope->getElements().at(11)));
    EXPECT_EQ("b", vr->getVariable());
    
    
    delete scope->getRoot();
}

TEST(VariableScopeAnalysis, run_functionParameterCheck)
{
    string input = "void funct(int one, int* two, int& three){\n}";
    set<string> temp;
    Scope* scope = VariableScopeAnalysis::run(input, CPP,temp);
    VariableDeclared* vd;
    
    ASSERT_TRUE(scope);
    ASSERT_EQ(1, scope->getElements().size());
    ASSERT_TRUE(scope = dyn_cast<Scope>(scope->getElements().at(0)));
    
    ASSERT_EQ(4, scope->getElements().size()); //Below and the function scope
    
    //int one
    ASSERT_TRUE(vd = dyn_cast<VariableDeclared>(scope->getElements().at(0)));
    EXPECT_EQ("one", vd->getVariable());
    EXPECT_FALSE(vd->isPointer());
    EXPECT_FALSE(vd->isExtern());
    EXPECT_FALSE(vd->isReference());
    EXPECT_TRUE(vd->isFunctionParameter());
    EXPECT_FALSE(vd->isStatic());
    EXPECT_FALSE(vd->isArray());
    
    //int two
    ASSERT_TRUE(vd = dyn_cast<VariableDeclared>(scope->getElements().at(1)));
    EXPECT_EQ("two", vd->getVariable());
    EXPECT_TRUE(vd->isPointer());
    EXPECT_FALSE(vd->isExtern());
    EXPECT_FALSE(vd->isReference());
    EXPECT_TRUE(vd->isFunctionParameter());
    EXPECT_FALSE(vd->isStatic());
    EXPECT_FALSE(vd->isArray());
    
    //int three
    ASSERT_TRUE(vd = dyn_cast<VariableDeclared>(scope->getElements().at(2)));
    EXPECT_EQ("three", vd->getVariable());
    EXPECT_FALSE(vd->isPointer());
    EXPECT_FALSE(vd->isExtern());
    EXPECT_TRUE(vd->isReference());
    EXPECT_TRUE(vd->isFunctionParameter());
    EXPECT_FALSE(vd->isStatic());
    EXPECT_FALSE(vd->isArray());
}

TEST(VariableScopeAnalysis, run_badCode)
{
    string input = "asdfdl";
    cout << "--- Expected clang errors ---" << endl;
    set<string> temp;
    Scope* scope = VariableScopeAnalysis::run(input, CPP,temp);
    cout << "---  End of expected clang errors  ---" <<endl;
    ASSERT_EQ(NULL, scope);
}

TEST(VariableScopeAnalysis, getDataFlow_arrayBeingRead){
    string input = "double x11[700];\n";
    input += "void ff(){\n";
    input += "for(int i=0; i<66; i++){\n";
    input += "int s = x11[i];\n";
    input += "static int j;\n";
    input += "}\n";
    input += "}";
    
    set<string> includes;
    Scope* scope = VariableScopeAnalysis::run(input, CPP,includes);
    
    VariableDeclared* vd;
    VariableAssigned* va;
    VariableRead* vr;
    
    EXPECT_EQ(2, scope->getElements().size());
    
    //static double x[700];
    ASSERT_TRUE(vd = dyn_cast<VariableDeclared>(scope->getElements().at(0)));
    EXPECT_EQ("x11", vd->getVariable());
    EXPECT_TRUE(vd->isExtern());
    EXPECT_FALSE(vd->isReference());
    EXPECT_FALSE(vd->isPointer());
    EXPECT_FALSE(vd->isFunctionParameter());
    EXPECT_FALSE(vd->isStatic());
    EXPECT_TRUE(vd->isArray());
    
    //void ff(){ .. }
    ASSERT_TRUE(scope = dyn_cast<Scope>(scope->getElements().at(1)));
    EXPECT_EQ(1, scope->getElements().size());
    
    //for(int i = 0; i<66; i++){ ... }
    ASSERT_TRUE(scope = dyn_cast<Scope>(scope->getElements().at(0)));
    EXPECT_EQ(1, scope->getElements().size());
    
    ASSERT_TRUE(scope = dyn_cast<Scope>(scope->getElements().at(0)));
    EXPECT_EQ(6, scope->getElements().size());
    
    //int i = 0;
    ASSERT_TRUE(vd = dyn_cast<VariableDeclared>(scope->getElements().at(0)));
    EXPECT_EQ("i", vd->getVariable());
    ASSERT_TRUE(va = dyn_cast<VariableAssigned>(scope->getElements().at(1)));
    EXPECT_EQ("i", vd->getVariable());
    EXPECT_FALSE(vd->isStatic());
    EXPECT_FALSE(vd->isArray());
    
    //i<66;
    ASSERT_TRUE(vr = dyn_cast<VariableRead>(scope->getElements().at(2)));
    EXPECT_EQ("i", vr->getVariable());
    
    //i++
    ASSERT_TRUE(vr = dyn_cast<VariableRead>(scope->getElements().at(3)));
    EXPECT_EQ("i", vr->getVariable());
    ASSERT_TRUE(va = dyn_cast<VariableAssigned>(scope->getElements().at(4)));
    EXPECT_EQ("i", vr->getVariable());
    
    // { ... }
    ASSERT_TRUE(scope = dyn_cast<Scope>(scope->getElements().at(5)));
    EXPECT_EQ(5, scope->getElements().size());
    
    //int s = x[i];
    ASSERT_TRUE(vd = dyn_cast<VariableDeclared>(scope->getElements().at(0)));
    EXPECT_EQ("s", vd->getVariable());
    EXPECT_FALSE(vd->isStatic());
    ASSERT_TRUE(va = dyn_cast<VariableAssigned>(scope->getElements().at(1)));
    EXPECT_EQ("s", va->getVariable());
    ASSERT_TRUE(vr = dyn_cast<VariableRead>(scope->getElements().at(2)));
    EXPECT_EQ("x11", vr->getVariable());
    ASSERT_TRUE(vr = dyn_cast<VariableRead>(scope->getElements().at(3)));
    EXPECT_EQ("i", vr->getVariable());
    
    //static int j;
    ASSERT_TRUE(vd = dyn_cast<VariableDeclared>(scope->getElements().at(4)));
    EXPECT_EQ("j", vd->getVariable());
    EXPECT_FALSE(vd->isExtern());
    EXPECT_FALSE(vd->isReference());
    EXPECT_FALSE(vd->isPointer());
    EXPECT_FALSE(vd->isFunctionParameter());
    EXPECT_TRUE(vd->isStatic());

}

TEST(VariableScopeAnalysis, run_scopeWithArrayWrittenTo){
    string input = "void ff(int x[66][99][89]){\n";
    input += "for(int i=0; i<66; i++){\n";
    input += "x[i][i][i] = 0;\n";
    input += "}\n";
    input += "}";
    
    VariableDeclared* vd;
    VariableAssigned* va;
    VariableRead* vr;
    
    set<string> temp;
    Scope* s = VariableScopeAnalysis::run(input, CPP, temp);
    ASSERT_TRUE(s);
    
    ASSERT_EQ(1, s->getElements().size());
    
    //void ff(int x[66][99][89]){ ... }
    ASSERT_TRUE(s = dyn_cast<Scope>(s->getElements().at(0)));
    ASSERT_EQ(2, s->getElements().size());
    ASSERT_TRUE(vd = dyn_cast<VariableDeclared>(s->getElements().at(0)));
    EXPECT_EQ("x", vd->getVariable());
    
    //It's an array but, in C and, by extension, clang, it's really stored as a pointer
    EXPECT_FALSE(vd->isArray());
    EXPECT_TRUE(vd->isPointer());
    
    
    ASSERT_TRUE(s = dyn_cast<Scope>(s->getElements().at(1)));
    
    ASSERT_EQ(1, s->getElements().size());
    ASSERT_TRUE(s = dyn_cast<Scope>(s->getElements().at(0)));
    
    //for(int i=0; i<66; i++){ ... }
    ASSERT_EQ(6, s->getElements().size());
    
    //int i = 0;
    ASSERT_TRUE(vd = dyn_cast<VariableDeclared>(s->getElements().at(0)));
    EXPECT_EQ("i", vd->getVariable());
    ASSERT_TRUE(va = dyn_cast<VariableAssigned>(s->getElements().at(1)));
    EXPECT_EQ("i", vd->getVariable());
    
    //i<66;
    ASSERT_TRUE(vr = dyn_cast<VariableRead>(s->getElements().at(2)));
    EXPECT_EQ("i", vr->getVariable());
    
    //i++
    ASSERT_TRUE(vr = dyn_cast<VariableRead>(s->getElements().at(3)));
    EXPECT_EQ("i", vr->getVariable());
    ASSERT_TRUE(va = dyn_cast<VariableAssigned>(s->getElements().at(4)));
    EXPECT_EQ("i", vr->getVariable());
    
    // { ... }
    ASSERT_TRUE(s = dyn_cast<Scope>(s->getElements().at(5)));
    ASSERT_EQ(4, s->getElements().size());
    
    //x[i][i][i] = 0;
    ASSERT_TRUE(va = dyn_cast<VariableAssigned>(s->getElements().at(0)));
    EXPECT_EQ("x", va->getVariable());
    ASSERT_TRUE(vr = dyn_cast<VariableRead>(s->getElements().at(1)));
    EXPECT_EQ("i", vr->getVariable());
    ASSERT_TRUE(vr = dyn_cast<VariableRead>(s->getElements().at(2)));
    EXPECT_EQ("i", vr->getVariable());
    ASSERT_TRUE(vr = dyn_cast<VariableRead>(s->getElements().at(3)));
    EXPECT_EQ("i", vr->getVariable());
}

TEST(VariableScopeAnalysis, run_staticCheck){
    string input ="static int a[100];\n";
    input += "int b[100];\n";
    //input += "void func(){\n";
    //input += "    int c[100];\n";
    //input += "    static int d[100];\n";
    //input += "}\n";
    
    set<string> includes;
    Scope* scope = VariableScopeAnalysis::run(input, CPP,includes);
    
    VariableDeclared* vd;
    VariableAssigned* va;
    VariableRead* vr;
    
    ASSERT_EQ(2, scope->getElements().size());
    ASSERT_TRUE(vd = dyn_cast<VariableDeclared>(scope->getElements().at(0)));
    
    EXPECT_EQ("a", vd->getVariable());
    EXPECT_FALSE(vd->isExtern());
    EXPECT_FALSE(vd->isReference());
    EXPECT_FALSE(vd->isPointer());
    EXPECT_FALSE(vd->isFunctionParameter());
    EXPECT_TRUE(vd->isStatic());
    EXPECT_TRUE(vd->isArray());
    
    ASSERT_TRUE(vd = dyn_cast<VariableDeclared>(scope->getElements().at(1)));
    EXPECT_EQ("b", vd->getVariable());
    EXPECT_TRUE(vd->isExtern());
    EXPECT_FALSE(vd->isReference());
    EXPECT_FALSE(vd->isPointer());
    EXPECT_FALSE(vd->isFunctionParameter());
    EXPECT_FALSE(vd->isStatic());
    EXPECT_TRUE(vd->isArray());
}

TEST(VariableScopeAnalysis, run_scopeWithHeaders)
{
    
    // First header
    char *tmpname1 = strdup("/tmp/tmpfileXXXXXX");
    mkstemp(tmpname1);
    ofstream f1(tmpname1);
    
    string header1 = "int iArray[567];\n";
    header1 += "int bla=2;\n";
    header1 += "int* bla2 = &bla;\n";
    f1 << header1;
    f1.close();
    
    // Second header
    char *tmpname2 = strdup("/tmp/tmpfileXXXXXX");
    mkstemp(tmpname2);
    ofstream f2(tmpname2);
    
    string header2 = "#include \"" + string(tmpname1) + "\"\n";
    header2 += "void funct();\n";
    header2 += "void funct2(){\n";
    header2 += "int i=0;\n";
    header2 += "i++;\n";
    header2 += "}\n";
    f2 << header2;
    f2.close();
    
    // Main file
    string input = "#include \"" + string(tmpname2) + "\"\n";
    input += "void funct(){\n";
    input += "int a = 5;\n";
    input += "int b = 66;\n";
    input += "int c[5] = {1,2,3,4,5};\n";
    input += "int d = -100;\n";
    input += "(((((((c[(a)]) = (((d) + b))))))));\n";
    input += "iArray[0] = 2;\n";
    input += "}";
    
    
    set<string> includes;
    Scope* scope = VariableScopeAnalysis::run(input, CPP,includes);
    
    VariableDeclared* vd;
    VariableAssigned* va;
    VariableRead* vr;
    
    ASSERT_TRUE(scope);
    
    //Headers
    EXPECT_EQ(8, scope->getElements().size());
    
    //int iArray[567];
    ASSERT_TRUE(vd = dyn_cast<VariableDeclared>(scope->getElements().at(0)));
    EXPECT_EQ("iArray", vd->getVariable());
    EXPECT_TRUE(vd->isExtern());
    EXPECT_FALSE(vd->isReference());
    EXPECT_FALSE(vd->isPointer());
    EXPECT_FALSE(vd->isFunctionParameter());
    
    //int bla=2;
    ASSERT_TRUE(vd = dyn_cast<VariableDeclared>(scope->getElements().at(1)));
    EXPECT_EQ("bla", vd->getVariable());
    EXPECT_TRUE(vd->isExtern());
    EXPECT_FALSE(vd->isReference());
    EXPECT_FALSE(vd->isPointer());
    EXPECT_FALSE(vd->isFunctionParameter());
    EXPECT_FALSE(vd->isStatic());
    EXPECT_FALSE(vd->isArray());
    ASSERT_TRUE(va = dyn_cast<VariableAssigned>(scope->getElements().at(2)));
    EXPECT_EQ("bla", va->getVariable());
    
    //int* bla2 = &bla;
    ASSERT_TRUE(vd = dyn_cast<VariableDeclared>(scope->getElements().at(3)));
    EXPECT_EQ("bla2", vd->getVariable());
    EXPECT_TRUE(vd->isExtern());
    EXPECT_FALSE(vd->isReference());
    EXPECT_TRUE(vd->isPointer());
    EXPECT_FALSE(vd->isFunctionParameter());
    EXPECT_FALSE(vd->isStatic());
    EXPECT_FALSE(vd->isArray());
    ASSERT_TRUE(va = dyn_cast<VariableAssigned>(scope->getElements().at(4)));
    EXPECT_EQ("bla2", va->getVariable());
    ASSERT_TRUE(vr = dyn_cast<VariableRead>(scope->getElements().at(5)));
    EXPECT_EQ("bla", vr->getVariable());
    ASSERT_TRUE(va = dyn_cast<VariableAssigned>(scope->getElements().at(6)));
    EXPECT_EQ("bla", va->getVariable());
    
    // Main file
    ASSERT_TRUE(scope = dyn_cast<Scope>(scope->getElements().at(7)));
    
    ASSERT_EQ(1, scope->getElements().size());
    ASSERT_TRUE(scope = dyn_cast<Scope>(scope->getElements().at(0)));
    
    EXPECT_EQ(13, scope->getElements().size());
    
    //int a = 5;
    ASSERT_TRUE(vd = dyn_cast<VariableDeclared>(scope->getElements().at(0)));
    EXPECT_EQ("a", vd->getVariable());
    EXPECT_FALSE(vd->isExtern());
    EXPECT_FALSE(vd->isPointer());
    EXPECT_FALSE(vd->isReference());
    EXPECT_FALSE(vd->isFunctionParameter());
    EXPECT_FALSE(vd->isStatic());
    EXPECT_FALSE(vd->isArray());
    ASSERT_TRUE(va = dyn_cast<VariableAssigned>(scope->getElements().at(1)));
    EXPECT_EQ("a", va->getVariable());
    
    //int b = 66;
    ASSERT_TRUE(vd = dyn_cast<VariableDeclared>(scope->getElements().at(2)));
    EXPECT_EQ("b", vd->getVariable());
    EXPECT_FALSE(vd->isExtern());
    EXPECT_FALSE(vd->isPointer());
    EXPECT_FALSE(vd->isReference());
    EXPECT_FALSE(vd->isFunctionParameter());
    EXPECT_FALSE(vd->isStatic());
    EXPECT_FALSE(vd->isArray());
    ASSERT_TRUE(va = dyn_cast<VariableAssigned>(scope->getElements().at(3)));
    EXPECT_EQ("b", va->getVariable());
    
    //int c[5] = {1,2,3,4,5};
    ASSERT_TRUE(vd = dyn_cast<VariableDeclared>(scope->getElements().at(4)));
    EXPECT_EQ("c", vd->getVariable());
    EXPECT_FALSE(vd->isExtern());
    EXPECT_FALSE(vd->isPointer());
    EXPECT_FALSE(vd->isReference());
    EXPECT_FALSE(vd->isFunctionParameter());
    EXPECT_FALSE(vd->isStatic());
    EXPECT_TRUE(vd->isArray());
    ASSERT_TRUE(va = dyn_cast<VariableAssigned>(scope->getElements().at(5)));
    EXPECT_EQ("c", va->getVariable());
    
    //int d = -100;
    ASSERT_TRUE(vd = dyn_cast<VariableDeclared>(scope->getElements().at(6)));
    EXPECT_EQ("d", vd->getVariable());
    EXPECT_FALSE(vd->isExtern());
    EXPECT_FALSE(vd->isPointer());
    EXPECT_FALSE(vd->isReference());
    EXPECT_FALSE(vd->isFunctionParameter());
    ASSERT_TRUE(va = dyn_cast<VariableAssigned>(scope->getElements().at(7)));
    EXPECT_EQ("d", va->getVariable());
    
    //(((((((c[(a)]) = (((d) + b))))))));;
    ASSERT_TRUE(va = dyn_cast<VariableAssigned>(scope->getElements().at(8)));
    EXPECT_EQ("c", va->getVariable());
    ASSERT_TRUE(vr = dyn_cast<VariableRead>(scope->getElements().at(9)));
    EXPECT_EQ("a", vr->getVariable());
    ASSERT_TRUE(vr=dyn_cast<VariableRead>(scope->getElements().at(10)));
    EXPECT_EQ("d", vr->getVariable());
    ASSERT_TRUE(vr=dyn_cast<VariableRead>(scope->getElements().at(11)));
    EXPECT_EQ("b", vr->getVariable());
    
    //iArray[0] = 2;
    ASSERT_TRUE(va = dyn_cast<VariableAssigned>(scope->getElements().at(12)));
    EXPECT_EQ("iArray", va->getVariable());
    
    
    delete scope->getRoot();
}
