#include <clang/VariableScopeAnalysis/ScopeAnalysis.h>
#include <clang/VariableScopeAnalysis/VariableScopeAnalysis.h>
#include "gtest/gtest.h"
#include <fstream>
#include <string>
#include <iostream>

TEST(ScopeAnalysis, getDataFlow_basic1){
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
    // start scope here
    input += "for(int j=0; j < 20; j++){\n";              //18
    input += "toReturn += input;\n";                      //19
    input += "bla[j] = 6;\n";                             //20
    input += "}\n";                                       //21
    // end scope here
    input += "*(pointer+1) = bla[i];\n";                  //22
    input += "}\n";                                       //23
    input += "return toReturn * global_1;\n";             //24
    input += "}";                                         //25
    
    set<string> temp;
    Scope* s = VariableScopeAnalysis::run(input, CPP,temp);
    
    if(!s){
        ASSERT_TRUE(false);
    }
    
    set<DataFlowInfo> data;
    
    unsigned int endScope;
    getDataFlow(s, 18, endScope, data);
    ASSERT_EQ(SCOPE_ANALYSIS_SUCCESS, 0);
    
    EXPECT_EQ(21, endScope);
    
    EXPECT_EQ(3, data.size());
    DataFlowInfo one = {"bla", COPY, "", ""};
    EXPECT_TRUE(data.count(one));
    DataFlowInfo two = {"input", COPY_IN, "", ""};
    EXPECT_TRUE(data.count(two));
    DataFlowInfo three = {"toReturn", COPY, "", ""};
    EXPECT_TRUE(data.count(three));
}

TEST(ScopeAnalysis, getDataFlow_basic2){
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
        // start scope here
    input += "for(int i=0; i < 10; i++){\n";              //16
    input += "int bla[20]; //Not initialised\n";          //17
    input += "for(int j=0; j < 20; j++){\n";              //18
    input += "toReturn += input;\n";                      //19
    input += "bla[j] = 6;\n";                             //20
    input += "}\n";                                       //21
    input += "*(pointer+1) = bla[i];\n";                  //22
    input += "}\n";                                       //23
    // end scope here
    input += "return toReturn * global_1;\n";             //24
    input += "}";                                         //25
    
    set<string> temp;
    Scope* s = VariableScopeAnalysis::run(input, CPP, temp);
    
    if(!s){
        ASSERT_TRUE(false);
    }
    
    set<DataFlowInfo> data;
    
    unsigned int endScope;
    ASSERT_EQ(SCOPE_ANALYSIS_SUCCESS, getDataFlow(s, 16, endScope, data));
    
    EXPECT_EQ(23, endScope);
    
    EXPECT_EQ(3, data.size());
    DataFlowInfo one = {"input", COPY_IN, "", ""};
    EXPECT_TRUE(data.count(one));
    DataFlowInfo two = {"pointer", COPY_IN, "", ""};
    EXPECT_TRUE(data.count(two));
    DataFlowInfo three = {"toReturn", COPY, "", ""};
    EXPECT_TRUE(data.count(three));
}

TEST(ScopeAnalysis, getDataFlow_basic3){
    string input = "int global_1 = 0;\n";                       //1
    input += "extern int global_2;\n";                          //2
    input += "int add(int a, int b){\n";                        //3
    input += "return a + b;\n";                                 //4
    input += "}\n";                                             //5
    input += "int plusOne(int a){\n";                           //6
    input += "return a + 1;\n";                                 //7
    input += "}\n";                                             //8
    input += "int function_1(int input){\n";                    //9
    input += "int* pointer;\n";                                 //10
    input += "int toReturn = 0;\n";                             //11
    input += "if(true){\n";                                     //12
    input += "int a = 0;\n";                                    //13
    input += "toReturn++;\n";                                   //14
    input += "}\n";                                             //15
    input += "for(int i=0; i < 10; i++){\n";                    //16
    input += "int bla[20]; //Not initialised\n";                //17
    input += "for(int j=0; j < 20; j++){\n";                    //18
    input += "toReturn = add(plusOne(bla[j]), input);\n";       //19
    input += "bla[j] = 6;\n";                                   //20
    input += "}\n";                                             //21
    input += "}\n";                                             //22
    
    input += "for(int i=0; i < 10; i++){\n";                    //23
    input += "int bla[20]; //Not initialised\n";                //24
    input += "for(int j=0; j < 20; j++){\n";                    //25
    input += "toReturn = add(plusOne(bla[j]), input);\n";       //26
    input += "bla[j] = 6;\n";                                   //27
    input += "}\n" ;                                            //28
    input += "*(pointer+1) = bla[i];\n";                        //29
    input += "}\n";                                             //30
    
    input += "return toReturn * global_1;\n";                   //31
    input += "}";                                               //32
    
    set<string> temp;
    Scope* s = VariableScopeAnalysis::run(input, CPP, temp);
    
    if(!s){
        ASSERT_TRUE(false);
    }
    
    set<DataFlowInfo> data;
    
    unsigned int endScope;
    ASSERT_EQ(SCOPE_ANALYSIS_SUCCESS, getDataFlow(s, 23, endScope, data));
    
    EXPECT_EQ(30, endScope);
    
    EXPECT_EQ(3, data.size());
    DataFlowInfo one = {"toReturn", COPY_OUT, "", ""};
    EXPECT_TRUE(data.count(one));
    DataFlowInfo two = {"pointer", COPY_IN, "", ""};
    EXPECT_TRUE(data.count(two));
    DataFlowInfo three = {"input", COPY_IN, "", ""};
    EXPECT_TRUE(data.count(three));
}

TEST(ScopeAnalysis, nullScope){
    Scope* s = NULL;
    
    set<DataFlowInfo> data;
    unsigned int endScope;
    ASSERT_EQ(SCOPE_ANALYSIS_SCOPE_INVALID, getDataFlow(s, 16, endScope, data));
}

TEST(ScopeAnalysis, invalidScopeSelected){
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
    input += "toReturn += input;\n";                      //19
    input += "bla[j] = 6;\n";                             //20
    input += "}\n";                                       //21
    input += "*(pointer+1) = bla[i];\n";                  //22
    input += "}\n";                                       //23
    input += "return toReturn * global_1;\n";             //24
    input += "}";                                         //25
    
    set<string> temp;
    Scope* s = VariableScopeAnalysis::run(input, CPP, temp);
    
    if(!s){
        ASSERT_TRUE(false);
    }
    
    set<DataFlowInfo> data;
    
    unsigned int endScope;
    ASSERT_EQ(SCOPE_ANALYSIS_INVALID_LINE_NO, getDataFlow(s, 13, endScope, data));
}

TEST(ScopeAnalysis, newScopeInserted){

    string input = "";
    input += "int global_1 = 0;\n";             //1
    input += "extern int global_2;\n";          //2
    input += "int function_1(int input){\n";    //3
    input += "int toReturn = 0;\n";             //4
    // Data directive start here
    input += "if(0){\n";                        //5
    input += "int a = 0;\n";                    //6
    input += "toReturn++;\n";                   //7
    input += "}\n";                             //8
    input += "\n";                              //9
    input += "for(int i=0; i < 10; i++){\n";    //10
    input += "int bla;\n";                      //11
    input += "for(int j=0; j < 20; j++){\n";    //12
    input += "toReturn += input;\n";            //13
    input += "}\n";                             //14
    input += "}\n";                             //15
    input += "toReturn++;\n";                   //16
    input += "toReturn++;\n";                   //17
    input += "for(int j=0; j < 100; j++){\n";   //18
    input += "toReturn++;\n";                   //19
    input += "}\n";                             //20
    // Data directive end here
    input += "\n";                              //21
    input += "return toReturn * global_1;\n";   //22
    input += "}";                               //23
    
    set<string> temp;
    Scope* s = VariableScopeAnalysis::run(input, CPP, temp);
    
    if(!s){
        ASSERT_TRUE(false);
    }
    
    SourceCodeLocation scl = {5,0,21,5};
    s->addScope(scl);
    
    set<DataFlowInfo> data;
    
    unsigned int endScope;
    ASSERT_EQ(SCOPE_ANALYSIS_SUCCESS, getDataFlow(s, scl.startLineNo, endScope, data));
    
    EXPECT_EQ(scl.endLineNo, endScope);
    
    EXPECT_EQ(2, data.size());
    DataFlowInfo one = {"input", COPY_IN, "", ""};
    EXPECT_TRUE(data.count(one));
    DataFlowInfo two = {"toReturn", COPY, "", ""};
    EXPECT_TRUE(data.count(two));
}

TEST(ScopeAnalysis, getDataDirectiveDataFlow_basic1){
    string input2 = "int global_1 = 0;\n";                 //1
    input2 += "extern int global_2;\n";                    //2
    input2 += "int add(int a, int b){\n";                  //3
    input2 += "return a + b;\n";                           //4
    input2 += "}\n";                                       //5
    input2 += "int plusOne(int a){\n";                     //6
    input2 += "return a + 1;\n";                           //7
    input2 += "}\n";                                       //8
    input2 += "int function_1(int input){\n";              //9
    input2 += "int* pointer;\n";                           //10
    input2 += "int toReturn = 0;\n";                       //11
    input2 += "if(1){\n";                                  //12
    input2 += "int a = 0;\n";                              //13
    input2 += "toReturn++;\n";                             //14
    input2 += "}\n";                                       //15
    input2 += "for(int i=0; i < 10; i++){\n";              //16
    input2 += "int bla[20]; //Not initialised\n";          //17
    input2 += "for(int j=0; j < 20; j++){\n";              //18
    input2 += "toReturn = add(plusOne(bla[j]), input);\n"; //19
    input2 += "bla[j] = 6;\n";                             //20
    input2 += "}\n";                                       //21
    input2 += "}\n";                                       //22
    input2 += "for(int i=0; i < 10; i++){\n";              //23
    input2 += "int bla[20]; //Not initialised\n";          //24
    input2 += "for(int j=0; j < 20; j++){\n";              //25
    input2 += "toReturn = add(plusOne(bla[j]), input);\n"; //26
    input2 += "bla[j] = 6;\n";                             //27
    input2 += "}\n";                                       //28
    input2 += "*(pointer+1) = bla[i];\n";                  //29
    input2 += "}\n";                                       //30
    input2 += "for(int i=0; i<20; i++){\n";                //31
    input2 += "toReturn++;\n";                             //32
    input2 += "}\n";                                       //33
    input2 += "return toReturn * global_1;\n";             //34
    input2 += "}";                                         //35
    
    map<SourceCodeLocation, set<DataFlowInfo> > presentDirectives;
    
    SourceCodeLocation loc1 = {16,0,22,0};
    set<DataFlowInfo> dfi1;
    DataFlowInfo var1 = {"pointer", COPY, "", ""};
    dfi1.insert(var1);
    DataFlowInfo var2 = {"toReturn", COPY, "10", "1000"};
    dfi1.insert(var2);
    DataFlowInfo var3 = {"input", COPY_IN, "0", "1"};
    dfi1.insert(var3);
    presentDirectives[loc1] = dfi1;
    
    SourceCodeLocation loc2 = {23,0,30,0};
    presentDirectives[loc2] = dfi1;
    
    SourceCodeLocation loc3 = {31,0,33,0};
    set<DataFlowInfo> dfi2;
    DataFlowInfo var4 = {"toReturn", COPY, "5", "100"};
    dfi2.insert(var4);
    presentDirectives[loc3] = dfi2;
    
    set<SourceCodeLocation> presDirLocs;
    presDirLocs.insert(loc1);
    presDirLocs.insert(loc2);
    presDirLocs.insert(loc3);
    
    set<string> includes;
    Scope* s = VariableScopeAnalysis::run(input2, C, includes, presDirLocs);
    
    set<DataFlowInfo> flowData;
    set<UpdateStatement> updateData;
    int errCode = getDataDirectiveDataFlow(s, 16, 34, presentDirectives, flowData, updateData);
    ASSERT_EQ(SCOPE_ANALYSIS_SUCCESS, errCode);
    
    EXPECT_EQ(3, flowData.size());
    DataFlowInfo expected1 = {"input", COPY_IN, "0", "1"};
    DataFlowInfo expected2 = {"pointer", COPY_IN, "", ""};
    DataFlowInfo expected3 = {"toReturn", COPY, "5", "1000"};
    
    EXPECT_TRUE(flowData.count(expected1));
    EXPECT_TRUE(flowData.count(expected2));
    EXPECT_TRUE(flowData.count(expected3));
    
    EXPECT_EQ(0, updateData.size());
}

TEST(ScopeAnalysis, getDataFlow_array){
    string input2 = "int global_1 = 0;\n";                 //1
    input2 += "extern int global_2;\n";                    //2
    input2 += "extern int iArray[];\n";                    //3
    input2 += "int add(int a, int b){\n";                  //4
    input2 += "return a + b;\n";                           //5
    input2 += "}\n";                                       //6
    input2 += "int plusOne(int a){\n";                     //7
    input2 += "return a + 1;\n";                           //8
    input2 += "}\n";                                       //9
    input2 += "int function_1(int input){\n";              //10
    input2 += "int* pointer;\n";                           //11
    input2 += "int toReturn = 0;\n";                       //12
    input2 += "if(1){\n";                                  //13
    input2 += "int a = 0;\n";                              //14
    input2 += "toReturn++;\n";                             //15
    input2 += "}\n";                                       //16
    
    input2 += "for(int i=0; i < 10; i++){\n";              //17
    input2 += "i+=iArray[i];\n";                           //18
    input2 += "int bla[20]; //Not initialised\n";          //19
    input2 += "for(int j=0; j < 20; j++){\n";              //20
    input2 += "toReturn = add(plusOne(bla[j]), input);\n"; //21
    input2 += "bla[j] = 6;\n";                             //22
    input2 += "}\n";                                       //23
    input2 += "}\n";                                       //24
    
    input2 += "for(int i=0; i < 10; i++){\n";              //25
    input2 += "int bla[20]; //Not initialised\n";          //26
    input2 += "for(int j=0; j < 20; j++){\n";              //27
    input2 += "toReturn = add(plusOne(bla[j]), input);\n"; //28
    input2 += "bla[j] = 6;\n";                             //29
    input2 += "}\n";                                       //30
    input2 += "*(pointer+1) = bla[i];\n";                  //31
    input2 += "}\n";                                       //32
    input2 += "for(int i=0; i<20; i++){\n";                //33
    input2 += "toReturn++;\n";                             //34
    input2 += "}\n";                                       //35
    input2 += "return toReturn * global_1;\n";             //36
    input2 += "}";                                         //37
    
    set<string> temp;
    Scope* s = VariableScopeAnalysis::run(input2, CPP, temp);
    
    ASSERT_TRUE(s);
    
    set<DataFlowInfo> data;
    unsigned int endScope;
    ASSERT_EQ(SCOPE_ANALYSIS_SUCCESS, getDataFlow(s, 17, endScope, data));
    
    
    EXPECT_EQ(24, endScope);
    EXPECT_EQ(3, data.size());
    DataFlowInfo one = {"iArray", COPY_IN, "", ""};
    DataFlowInfo two = {"toReturn", COPY_OUT, "", ""};
    DataFlowInfo three = {"input", COPY, "", ""};
    
    EXPECT_TRUE(data.count(one));
    EXPECT_TRUE(data.count(two));
    EXPECT_TRUE(data.count(three));
}

TEST(ScopeAnalysis, getDataFlow_arrayInHeader){
    char *tmpname = strdup("/tmp/tmpfileXXXXXX");
    mkstemp(tmpname);
    ofstream f(tmpname);
    
    string header = "int iArray[1020];\n";
    f << header;
    f.close();
    
    string input2 = "#include \"" + string(tmpname) + "\"\n";        //1
    input2 += "int global_1 = 0;\n";                       //2
    input2 += "extern int global_2;\n";                    //3
    input2 += "int add(int a, int b){\n";                  //4
    input2 += "return a + b;\n";                           //5
    input2 += "}\n";                                       //6
    input2 += "int plusOne(int a){\n";                     //7
    input2 += "return a + 1;\n";                           //8
    input2 += "}\n";                                       //9
    input2 += "int function_1(int input){\n";              //10
    input2 += "int* pointer;\n";                           //11
    input2 += "int toReturn = 0;\n";                       //12
    input2 += "if(1){\n";                                  //13
    input2 += "int a = 0;\n";                              //14
    input2 += "toReturn++;\n";                             //15
    input2 += "}\n";                                       //16
    
    input2 += "for(int i=0; i < 10; i++){\n";              //17
    input2 += "i+=iArray[i];\n";                           //18
    input2 += "int bla[20]; //Not initialised\n";          //19
    input2 += "for(int j=0; j < 20; j++){\n";              //20
    input2 += "toReturn = add(plusOne(bla[j]), input);\n"; //21
    input2 += "bla[j] = 6;\n";                             //22
    input2 += "}\n";                                       //23
    input2 += "}\n";                                       //24
    
    input2 += "for(int i=0; i < 10; i++){\n";              //25
    input2 += "int bla[20]; //Not initialised\n";          //26
    input2 += "for(int j=0; j < 20; j++){\n";              //27
    input2 += "toReturn = add(plusOne(bla[j]), input);\n"; //28
    input2 += "bla[j] = 6;\n";                             //29
    input2 += "}\n";                                       //30
    input2 += "*(pointer+1) = bla[i];\n";                  //31
    input2 += "}\n";                                       //32
    input2 += "for(int i=0; i<20; i++){\n";                //33
    input2 += "toReturn++;\n";                             //34
    input2 += "}\n";                                       //35
    input2 += "return toReturn * global_1;\n";             //36
    input2 += "}";                                         //37
    
    set<string> temp;
    Scope* s = VariableScopeAnalysis::run(input2, CPP, temp);
    
    ASSERT_TRUE(s);
    
    set<DataFlowInfo> data;
    unsigned int endScope;
    ASSERT_EQ(SCOPE_ANALYSIS_SUCCESS, getDataFlow(s, 17, endScope, data));
    
    
    EXPECT_EQ(24, endScope);
    EXPECT_EQ(3, data.size());
    DataFlowInfo one = {"iArray", COPY_IN, "", ""};
    DataFlowInfo two = {"toReturn", COPY_OUT, "", ""};
    DataFlowInfo three = {"input", COPY, "", ""};
    
    EXPECT_TRUE(data.count(one));
    EXPECT_TRUE(data.count(two));
    EXPECT_TRUE(data.count(three));
}

TEST(ScopeAnalysis, getDataFlow_arrayFunctionParameter){
    string input = "void ff(int x[66][99][89]){\n";
    input += "for(int i=0; i<66; i++){\n";
    input += "x[i][i][i] = 0;\n";
    input += "}\n";
    input += "}";
    
    set<string> temp;
    Scope* s = VariableScopeAnalysis::run(input, CPP, temp);
    
    ASSERT_TRUE(s);
    
    set<DataFlowInfo> data;
    unsigned int endScope;
    ASSERT_EQ(SCOPE_ANALYSIS_SUCCESS, getDataFlow(s, 2, endScope, data));
    
    EXPECT_EQ(4, endScope);
    EXPECT_EQ(1, data.size());
    DataFlowInfo x_copyout = {"x", COPY_OUT, "", ""};
    DataFlowInfo x_copyin = {"x", COPY_IN, "", ""};
    DataFlowInfo x_copy = {"x", COPY, "", ""};
    DataFlowInfo x_present = {"x", PRESENT, "", ""};
    EXPECT_FALSE(data.count(x_copyout));
    EXPECT_FALSE(data.count(x_copyin));
    EXPECT_TRUE(data.count(x_copy));
    EXPECT_FALSE(data.count(x_present));
}

TEST(ScopeAnalysis, getDataFlow_arrayIsStatic){
    string input = "static double x[700];\n";
    input += "void ff(){\n";
    input += "for(int i=0; i<66; i++){\n";
    input += "x[i] = 0;\n";
    input += "}\n";
    input += "}";
    
    set<string> temp;
    Scope* s = VariableScopeAnalysis::run(input, CPP, temp);
    
    ASSERT_TRUE(s);
    
    set<DataFlowInfo> data;
    unsigned int endScope;
    ASSERT_EQ(SCOPE_ANALYSIS_SUCCESS, getDataFlow(s, 3, endScope, data));
    
    EXPECT_EQ(5, endScope);
    EXPECT_EQ(1, data.size());
    DataFlowInfo x_copyout = {"x", COPY_OUT, "", ""};
    DataFlowInfo x_copyin = {"x", COPY_IN, "", ""};
    DataFlowInfo x_copy = {"x", COPY, "", ""};
    DataFlowInfo x_create = {"x", CREATE, "", ""};
    
    EXPECT_FALSE(data.count(x_copyout));
    EXPECT_FALSE(data.count(x_copyin));
    EXPECT_TRUE(data.count(x_copy));
    EXPECT_FALSE(data.count(x_create));
}

TEST(ScopeAnalysis, getDataFlow_externArray1){
    string input = "extern double x[700];\n";
    input += "void ff(){\n";
    input += "for(int i=0; i<66; i++){\n";
    input += "int s = x[i];\n";
    input += "}\n";
    input += "}";
    
    set<string> temp;
    Scope* s = VariableScopeAnalysis::run(input, CPP, temp);
    
    ASSERT_TRUE(s);
    
    set<DataFlowInfo> data;
    unsigned int endScope;
    ASSERT_EQ(SCOPE_ANALYSIS_SUCCESS, getDataFlow(s, 3, endScope, data));
    
    EXPECT_EQ(5, endScope);
    EXPECT_EQ(1, data.size());
    DataFlowInfo x = {"x", COPY_IN, "", ""};
    EXPECT_TRUE(data.count(x));
}

TEST(ScopeAnalysis, getDataFlow_externArray2){
    string input = "extern double x[700];\n";
    input += "void ff(){\n";
    input += "for(int i=0; i<66; i++){\n";
    input += "x[i]=2;\n";
    input += "}\n";
    input += "}";
    
    set<string> temp;
    Scope* s = VariableScopeAnalysis::run(input, CPP, temp);
    
    ASSERT_TRUE(s);
    
    set<DataFlowInfo> data;
    unsigned int endScope;
    ASSERT_EQ(SCOPE_ANALYSIS_SUCCESS, getDataFlow(s, 3, endScope, data));
    
    EXPECT_EQ(5, endScope);
    EXPECT_EQ(1, data.size());
    DataFlowInfo x = {"x", COPY, "", ""};
    EXPECT_TRUE(data.count(x));
}

TEST(ScopeAnalysis, getDataFlow_externArray3DArrayRead){
    string input = "extern double x[700][800][900];\n";
    input += "void ff(){\n";
    input += "for(int i=0; i<66; i++){\n";
    input += "int s = x[67][70][i];\n";
    input += "}\n";
    input += "}";
    
    set<string> temp;
    Scope* s = VariableScopeAnalysis::run(input, CPP, temp);
    
    ASSERT_TRUE(s);
    
    set<DataFlowInfo> data;
    unsigned int endScope;
    ASSERT_EQ(SCOPE_ANALYSIS_SUCCESS, getDataFlow(s, 3, endScope, data));
    
    EXPECT_EQ(5, endScope);
    EXPECT_EQ(1, data.size());
    DataFlowInfo x = {"x", COPY_IN, "", ""};
    EXPECT_TRUE(data.count(x));
}

TEST(ScopeAnalysis, getDataFlow_BT){
    string input = "# 1 \"experiment/setup/NPB3.3-SER-C/BT/x_solve.c\"\n"; //1
    input += "# 1 \"<built-in>\" 1\n"; //2
    input += "# 1 \"<built-in>\" 3\n"; //3
    input += "# 161 \"<built-in>\" 3\n"; //4
    input += "# 1 \"<command line>\" 1\n"; //5
    input += "# 1 \"<built-in>\" 2\n"; //6
    input += "# 1 \"experiment/setup/NPB3.3-SER-C/BT/x_solve.c\" 2\n"; //7
    input += "# 35 \"experiment/setup/NPB3.3-SER-C/BT/x_solve.c\"\n"; //8
    input += "# 1 \"experiment/setup/NPB3.3-SER-C/BT/header.h\" 1\n"; //9
    input += "# 52 \"experiment/setup/NPB3.3-SER-C/BT/header.h\"\n"; //10
    input += "# 1 \"experiment/setup/NPB3.3-SER-C/BT/npbparams.h\" 1\n"; //11
    input += "# 53 \"experiment/setup/NPB3.3-SER-C/BT/header.h\" 2\n"; //12
    input += "# 1 \"./experiment/setup/NPB3.3-SER-C/common/type.h\" 1\n"; //13
    input += "typedef enum { False, True } logical;\n"; //14
    input += "typedef struct {\n"; //15
    input += "double real;\n"; //16
    input += "double imag;\n"; //17
    input += "} dcomplex;\n"; //18
    input += "# 54 \"experiment/setup/NPB3.3-SER-C/BT/header.h\" 2\n"; //19
    input += "extern double elapsed_time;\n"; //20
    input += "extern int grid_points[3];\n"; //21
    input += "extern logical timeron;\n"; //22
    input += "extern double tx1, tx2, tx3, ty1, ty2, ty3, tz1, tz2, tz3,\n"; //23
    input += "dx1, dx2, dx3, dx4, dx5, dy1, dy2, dy3, dy4,\n"; //24
    input += "dy5, dz1, dz2, dz3, dz4, dz5, dssp, dt,\n"; //25
    input += "ce[5][13], dxmax, dymax, dzmax, xxcon1, xxcon2,\n"; //26
    input += "xxcon3, xxcon4, xxcon5, dx1tx1, dx2tx1, dx3tx1,\n"; //27
    input += "dx4tx1, dx5tx1, yycon1, yycon2, yycon3, yycon4,\n"; //28
    input += "yycon5, dy1ty1, dy2ty1, dy3ty1, dy4ty1, dy5ty1,\n"; //29
    input += "zzcon1, zzcon2, zzcon3, zzcon4, zzcon5, dz1tz1,\n"; //30
    input += "dz2tz1, dz3tz1, dz4tz1, dz5tz1, dnxm1, dnym1,\n"; //31
    input += "dnzm1, c1c2, c1c5, c3c4, c1345, conz1, c1, c2,\n"; //32
    input += "c3, c4, c5, c4dssp, c5dssp, dtdssp, dttx1,\n"; //33
    input += "dttx2, dtty1, dtty2, dttz1, dttz2, c2dttx1,\n"; //34
    input += "c2dtty1, c2dttz1, comz1, comz4, comz5, comz6,\n"; //35
    input += "c3c4tx3, c3c4ty3, c3c4tz3, c2iv, con43, con16;\n"; //36
    input += "# 91 \"experiment/setup/NPB3.3-SER-C/BT/header.h\"\n"; //37
    input += "extern double us [40][40/2*2 +1][40/2*2 +1];\n"; //38
    input += "extern double vs [40][40/2*2 +1][40/2*2 +1];\n"; //39
    input += "extern double ws [40][40/2*2 +1][40/2*2 +1];\n"; //40
    input += "extern double qs [40][40/2*2 +1][40/2*2 +1];\n"; //41
    input += "extern double rho_i [40][40/2*2 +1][40/2*2 +1];\n"; //42
    input += "extern double square [40][40/2*2 +1][40/2*2 +1];\n"; //43
    input += "extern double forcing[40][40/2*2 +1][40/2*2 +1][5];\n"; //44
    input += "extern double u [40][40/2*2 +1][40/2*2 +1][5];\n"; //45
    input += "extern double rhs [40][40/2*2 +1][40/2*2 +1][5];\n"; //46
    input += "extern double cuf[40 +1];\n"; //47
    input += "extern double q [40 +1];\n"; //48
    input += "extern double ue [40 +1][5];\n"; //49
    input += "extern double buf[40 +1][5];\n"; //50
    input += "# 125 \"experiment/setup/NPB3.3-SER-C/BT/header.h\"\n"; //51
    input += "void initialize();\n"; //52
    input += "void lhsinit(double lhs[][3][5][5], int size);\n"; //53
    input += "void exact_solution(double xi, double eta, double zeta, double dtemp[5]);\n"; //54
    input += "void exact_rhs();\n"; //55
    input += "void set_constants();\n"; //56
    input += "void adi();\n"; //57
    input += "void compute_rhs();\n"; //58
    input += "void x_solve();\n"; //59
    input += "void y_solve();\n"; //60
    input += "void matvec_sub(double ablock[5][5], double avec[5], double bvec[5]);\n"; //61
    input += "void matmul_sub(double ablock[5][5], double bblock[5][5], double cblock[5][5]);\n"; //62
    input += "void binvcrhs(double lhs[5][5], double c[5][5], double r[5]);\n"; //63
    input += "void binvrhs(double lhs[5][5], double r[5]);\n"; //64
    input += "void z_solve();\n"; //65
    input += "void add();\n"; //66
    input += "void error_norm(double rms[5]);\n"; //67
    input += "void rhs_norm(double rms[5]);\n"; //68
    input += "void verify(int no_time_steps,  logical *verified);\n"; //69
    input += "# 36 \"experiment/setup/NPB3.3-SER-C/BT/x_solve.c\" 2\n"; //70
    input += "# 1 \"experiment/setup/NPB3.3-SER-C/BT/work_lhs.h\" 1\n"; //71
    input += "# 43 \"experiment/setup/NPB3.3-SER-C/BT/work_lhs.h\"\n"; //72
    input += "extern double fjac[40 +1][5][5];\n"; //73
    input += "extern double njac[40 +1][5][5];\n"; //74
    input += "extern double lhs [40 +1][3][5][5];\n"; //75
    input += "extern double tmp1, tmp2, tmp3;\n"; //76
    input += "# 39 \"experiment/setup/NPB3.3-SER-C/BT/x_solve.c\" 2\n"; //77
    input += "# 1 \"./experiment/setup/NPB3.3-SER-C/common/timers.h\" 1\n"; //78
    input += "void timer_clear( int n );\n"; //79
    input += "void timer_start( int n );\n"; //80
    input += "void timer_stop( int n );\n"; //81
    input += "double timer_read( int n );\n"; //82
    input += "# 42 \"experiment/setup/NPB3.3-SER-C/BT/x_solve.c\" 2\n"; //83
    input += "# 1 \"/opt/pgi/linux86-64/17.4/include/openacc.h\" 1\n"; //84
    input += "# 27 \"/opt/pgi/linux86-64/17.4/include/openacc.h\"\n"; //85
    input += "typedef enum{\n"; //86
    input += "acc_device_none = 0,\n"; //87
    input += "acc_device_default = 1,\n"; //88
    input += "acc_device_host = 2,\n"; //89
    input += "acc_device_not_host = 3,\n"; //90
    input += "acc_device_nvidia = 4,\n"; //91
    input += "acc_device_radeon = 5,\n"; //92
    input += "acc_device_xeonphi = 6,\n"; //93
    input += "acc_device_pgi_opencl = 7,\n"; //94
    input += "acc_device_nvidia_opencl = 8,\n"; //95
    input += "acc_device_opencl = 9\n"; //96
    input += "}acc_device_t;\n"; //97
    input += "void acc_set_default_async(int async);\n"; //98
    input += "int acc_get_default_async(void);\n"; //99
    input += "extern int acc_get_num_devices( acc_device_t devtype );\n"; //100
    input += "extern acc_device_t acc_get_device(void);\n"; //101
    input += "extern void acc_set_device_num( int devnum, acc_device_t devtype );\n"; //102
    input += "extern int acc_get_device_num( acc_device_t devtype );\n"; //103
    input += "extern void acc_init( acc_device_t devtype );\n"; //104
    input += "extern void acc_shutdown( acc_device_t devtype );\n"; //105
    input += "extern void acc_set_deviceid( int devid );\n"; //106
    input += "extern int acc_get_deviceid( int devnum, acc_device_t devtype );\n"; //107
    input += "extern int acc_async_test( long async );\n"; //108
    input += "extern int acc_async_test_all(void);\n"; //109
    input += "extern void acc_async_wait( long async );\n"; //110
    input += "extern void acc_async_wait_all(void);\n"; //111
    input += "extern void acc_wait( long async );\n"; //112
    input += "extern void acc_wait_async( long arg, long async );\n"; //113
    input += "extern void acc_wait_all(void);\n"; //114
    input += "extern void acc_wait_all_async( long async );\n"; //115
    input += "extern int acc_on_device( acc_device_t devtype );\n"; //116
    input += "extern void acc_free(void*);\n"; //117
    input += "extern void* acc_memcpy( void* targetptr, void* srcptr, unsigned long bytes );\n"; //118
    input += "extern void* acc_memcpy_async( void* targetptr, void* srcptr, unsigned long bytes, long async );\n"; //119
    input += "extern void* acc_copyin( void* hostptr, unsigned long bytes );\n"; //120
    input += "extern void* acc_copyin_async( void* hostptr, unsigned long bytes, long async );\n"; //121
    input += "extern void* acc_pcopyin( void* hostptr, unsigned long bytes );\n"; //122
    input += "extern void* acc_pcopyin_async( void* hostptr, unsigned long bytes, long async );\n"; //123
    input += "extern void* acc_present_or_copyin( void* hostptr, unsigned long bytes );\n"; //124
    input += "extern void* acc_present_or_copyin_async( void* hostptr, unsigned long bytes, long async );\n"; //125
    input += "extern void* acc_create( void* hostptr, unsigned long bytes );\n"; //126
    input += "extern void* acc_create_async( void* hostptr, unsigned long bytes, long async );\n"; //127
    input += "extern void* acc_pcreate( void* hostptr, unsigned long bytes );\n"; //128
    input += "extern void* acc_pcreate_async( void* hostptr, unsigned long bytes, long async );\n"; //129
    input += "extern void* acc_present_or_create( void* hostptr, unsigned long bytes );\n"; //130
    input += "extern void* acc_present_or_create_async( void* hostptr, unsigned long bytes, long async );\n"; //131
    input += "extern void acc_copyout( void* hostptr, unsigned long bytes );\n"; //132
    input += "extern void acc_copyout_async( void* hostptr, unsigned long bytes, long async );\n"; //133
    input += "extern void acc_delete( void* hostptr, unsigned long bytes );\n"; //134
    input += "extern void acc_delete_async( void* hostptr, unsigned long bytes, long async );\n"; //135
    input += "extern void acc_update_device( void* hostptr, unsigned long bytes );\n"; //136
    input += "extern void acc_update_device_async( void* hostptr, unsigned long bytes, long async );\n"; //137
    input += "extern void acc_update_self( void* hostptr, unsigned long bytes );\n"; //138
    input += "extern void acc_update_self_async( void* hostptr, unsigned long bytes, long async );\n"; //139
    input += "extern void acc_update_host( void* hostptr, unsigned long bytes );\n"; //140
    input += "extern void acc_update_host_async( void* hostptr, unsigned long bytes, long async );\n"; //141
    input += "extern void acc_memcpy_to_device( void* devptr, void* hostptr, unsigned long bytes );\n"; //142
    input += "extern void acc_memcpy_to_device_async( void* devptr, void* hostptr, unsigned long bytes, long async );\n"; //143
    input += "extern void acc_memcpy_from_device( void* hostptr, void* devptr, unsigned long bytes );\n"; //144
    input += "extern void acc_memcpy_from_device_async( void* hostptr, void* devptr, unsigned long bytes, long async );\n"; //145
    input += "extern void* acc_memcpy_device( void* targetdevptr, void* srcdevptr, unsigned long bytes );\n"; //146
    input += "extern void* acc_memcpy_device_async( void* targetdevptr, void* srcdevptr, unsigned long bytes, long async );\n"; //147
    input += "extern void acc_attach( void** hostptrptr );\n"; //148
    input += "extern void acc_attach_async( void** hostptrptr, long async );\n"; //149
    input += "extern void acc_detach( void** hostptrptr );\n"; //150
    input += "extern void acc_detach_async( void** hostptrptr, long async );\n"; //151
    input += "extern void acc_set_device_type( acc_device_t devtype );\n"; //152
    input += "extern acc_device_t acc_get_device_type(void);\n"; //153
    input += "extern void* acc_malloc(unsigned long);\n"; //154
    input += "extern void* acc_deviceptr( void* hostptr );\n"; //155
    input += "extern void* acc_hostptr( void* devptr );\n"; //156
    input += "extern void acc_map_data( void* hostptr, void* devptr, unsigned long bytes );\n"; //157
    input += "extern void acc_unmap_data( void* hostptr );\n"; //158
    input += "extern int acc_is_present( void* hostptr, unsigned long bytes );\n"; //159
    input += "extern int acc_present_count( void* hostptr );\n"; //160
    input += "extern void acc_updatein( void* hostptr, unsigned long bytes );\n"; //161
    input += "extern void acc_updatein_async( void* hostptr, unsigned long bytes, long async );\n"; //162
    input += "extern void acc_updateout( void* hostptr, unsigned long bytes );\n"; //163
    input += "extern void acc_updateout_async( void* hostptr, unsigned long bytes, long async );\n"; //164
    input += "extern void* acc_get_current_cuda_context(void);\n"; //165
    input += "extern int acc_get_current_cuda_device(void);\n"; //166
    input += "extern void* acc_get_cuda_stream(long);\n"; //167
    input += "extern void acc_set_cuda_stream(long,void*);\n"; //168
    input += "extern void* acc_cuda_get_context(int);\n"; //169
    input += "extern int acc_cuda_get_device(int);\n"; //170
    input += "extern void* acc_get_current_opencl_context(void);\n"; //171
    input += "extern void* acc_get_current_opencl_device(void);\n"; //172
    input += "extern void* acc_get_opencl_queue(long);\n"; //173
    input += "extern int atomicaddi(void *address, int val);\n"; //174
    input += "extern unsigned int atomicaddu(void *address, unsigned int val);\n"; //175
    input += "extern unsigned long long atomicaddul(void *address, unsigned long long val);\n"; //176
    input += "extern float atomicaddf(void *address, float val);\n"; //177
    input += "extern double atomicaddd(void *address, double val);\n"; //178
    input += "extern int atomicsubi(void *address, int val);\n"; //179
    input += "extern unsigned int atomicsubu(void *address, unsigned int val);\n"; //180
    input += "extern unsigned long long atomicsubul(void *address, unsigned long long val);\n"; //181
    input += "extern float atomicsubf(void *address, float val);\n"; //182
    input += "extern double atomicsubd(void *address, double val);\n"; //183
    input += "extern int atomicmaxi(void *address, int val);\n"; //184
    input += "extern unsigned int atomicmaxu(void *address, unsigned int val);\n"; //185
    input += "extern unsigned long long atomicmaxul(void *address, unsigned long long val);\n"; //186
    input += "extern float atomicmaxf(void *address, float val);\n"; //187
    input += "extern double atomicmaxd(void *address, double val);\n"; //188
    input += "extern int atomicmini(void *address, int val);\n"; //189
    input += "extern unsigned int atomicminu(void *address, unsigned int val);\n"; //190
    input += "extern unsigned long long atomicminul(void *address, unsigned long long val);\n"; //191
    input += "extern float atomicminf(void *address, float val);\n"; //192
    input += "extern double atomicmind(void *address, double val);\n"; //193
    input += "extern int atomicandi(void *address, int val);\n"; //194
    input += "extern unsigned int atomicandu(void *address, unsigned int val);\n"; //195
    input += "extern unsigned long long atomicandul(void *address, unsigned long long val);\n"; //196
    input += "extern int atomicori(void *address, int val);\n"; //197
    input += "extern unsigned int atomicoru(void *address, unsigned int val);\n"; //198
    input += "extern unsigned long long atomicorul(void *address, unsigned long long val);\n"; //199
    input += "extern int atomicxori(void *address, int val);\n"; //200
    input += "extern unsigned int atomicxoru(void *address, unsigned int val);\n"; //201
    input += "extern unsigned long long atomicxorul(void *address, unsigned long long val);\n"; //202
    input += "extern int atomicexchi(void *address, int val);\n"; //203
    input += "extern unsigned int atomicexchu(void *address, unsigned int val);\n"; //204
    input += "extern unsigned long long atomicexchul(void *address, unsigned long long val);\n"; //205
    input += "extern float atomicexchf(void *address, float val);\n"; //206
    input += "extern double atomicexchd(void *address, double val);\n"; //207
    input += "extern unsigned int atomicincu(void *address, unsigned int val);\n"; //208
    input += "extern unsigned int atomicdecu(void *address, unsigned int val);\n"; //209
    input += "extern int atomiccasi(void *address, int val, int val2);\n"; //210
    input += "extern unsigned int atomiccasu(void *address, unsigned int val, unsigned int val2);\n"; //211
    input += "extern unsigned long long atomiccasul(void *address, unsigned long long val, unsigned long long val2);\n"; //212
    input += "extern float atomiccasf(void *address, float val, float val2);\n"; //213
    input += "extern double atomiccasd(void *address, double val, double val2);\n"; //214
    input += "extern int __pgi_gangidx(void);\n"; //215
    input += "extern int __pgi_workeridx(void);\n"; //216
    input += "extern int __pgi_vectoridx(void);\n"; //217
    input += "extern int __pgi_blockidx(int);\n"; //218
    input += "extern int __pgi_threadidx(int);\n"; //219
    input += "# 45 \"experiment/setup/NPB3.3-SER-C/BT/x_solve.c\" 2\n"; //220
    input += "# 58 \"experiment/setup/NPB3.3-SER-C/BT/x_solve.c\"\n"; //221
    input += "void x_solve()\n"; //222
    input += "{\n"; //223
    input += "int i, j, k, m, n, isize;\n"; //224
    input += "if (timeron) { timer_start(6);}\n"; //225
    input += "# 77 \"experiment/setup/NPB3.3-SER-C/BT/x_solve.c\"\n"; //226
    input += "isize = grid_points[0]-1;\n"; //227
    input += "for (k = 1; k <= grid_points[2]-2; k++) {\n"; //228
    input += "for (j = 1; j <= grid_points[1]-2; j++) {\n"; //229
    
    
    // [Parallelisation here]
    input += "for (i = 0; i <= isize; i++) {\n"; //230
    input += "tmp1 = rho_i[k][j][i];\n"; //231
    input += "tmp2 = tmp1 * tmp1;\n"; //232
    input += "tmp3 = tmp1 * tmp2;\n"; //233
    input += "fjac[i][0][0] = 0.0;\n"; //234
    input += "fjac[i][1][0] = 1.0;\n"; //235
    input += "fjac[i][2][0] = 0.0;\n"; //236
    input += "fjac[i][3][0] = 0.0;\n"; //237
    input += "fjac[i][4][0] = 0.0;\n"; //238
    input += "fjac[i][0][1] = -(u[k][j][i][1] * tmp2 * u[k][j][i][1])\n"; //239
    input += "+ c2 * qs[k][j][i];\n"; //240
    input += "fjac[i][1][1] = ( 2.0 - c2 ) * ( u[k][j][i][1] / u[k][j][i][0] );\n"; //241
    input += "fjac[i][2][1] = -c2 * ( u[k][j][i][2] * tmp1 );\n"; //242
    input += "fjac[i][3][1] = -c2 * ( u[k][j][i][3] * tmp1 );\n"; //243
    input += "fjac[i][4][1] = c2;\n"; //244
    input += "fjac[i][0][2] = -( u[k][j][i][1]*u[k][j][i][2] ) * tmp2;\n"; //245
    input += "fjac[i][1][2] = u[k][j][i][2] * tmp1;\n"; //246
    input += "fjac[i][2][2] = u[k][j][i][1] * tmp1;\n"; //247
    input += "fjac[i][3][2] = 0.0;\n"; //248
    input += "fjac[i][4][2] = 0.0;\n"; //249
    input += "fjac[i][0][3] = -( u[k][j][i][1]*u[k][j][i][3] ) * tmp2;\n"; //250
    input += "fjac[i][1][3] = u[k][j][i][3] * tmp1;\n"; //251
    input += "fjac[i][2][3] = 0.0;\n"; //252
    input += "fjac[i][3][3] = u[k][j][i][1] * tmp1;\n"; //253
    input += "fjac[i][4][3] = 0.0;\n"; //254
    input += "fjac[i][0][4] = ( c2 * 2.0 * square[k][j][i] - c1 * u[k][j][i][4] )\n"; //255
    input += "* ( u[k][j][i][1] * tmp2 );\n"; //256
    input += "fjac[i][1][4] = c1 * u[k][j][i][4] * tmp1\n"; //257
    input += "- c2 * ( u[k][j][i][1]*u[k][j][i][1] * tmp2 + qs[k][j][i] );\n"; //258
    input += "fjac[i][2][4] = -c2 * ( u[k][j][i][2]*u[k][j][i][1] ) * tmp2;\n"; //259
    input += "fjac[i][3][4] = -c2 * ( u[k][j][i][3]*u[k][j][i][1] ) * tmp2;\n"; //260
    input += "fjac[i][4][4] = c1 * ( u[k][j][i][1] * tmp1 );\n"; //261
    input += "njac[i][0][0] = 0.0;\n"; //262
    input += "njac[i][1][0] = 0.0;\n"; //263
    input += "njac[i][2][0] = 0.0;\n"; //264
    input += "njac[i][3][0] = 0.0;\n"; //265
    input += "njac[i][4][0] = 0.0;\n"; //266
    input += "njac[i][0][1] = -con43 * c3c4 * tmp2 * u[k][j][i][1];\n"; //267
    input += "njac[i][1][1] = con43 * c3c4 * tmp1;\n"; //268
    input += "njac[i][2][1] = 0.0;\n"; //269
    input += "njac[i][3][1] = 0.0;\n"; //270
    input += "njac[i][4][1] = 0.0;\n"; //271
    input += "njac[i][0][2] = -c3c4 * tmp2 * u[k][j][i][2];\n"; //272
    input += "njac[i][1][2] = 0.0;\n"; //273
    input += "njac[i][2][2] = c3c4 * tmp1;\n"; //274
    input += "njac[i][3][2] = 0.0;\n"; //275
    input += "njac[i][4][2] = 0.0;\n"; //276
    input += "njac[i][0][3] = -c3c4 * tmp2 * u[k][j][i][3];\n"; //277
    input += "njac[i][1][3] = 0.0;\n"; //278
    input += "njac[i][2][3] = 0.0;\n"; //279
    input += "njac[i][3][3] = c3c4 * tmp1;\n"; //280
    input += "njac[i][4][3] = 0.0;\n"; //281
    input += "njac[i][0][4] = -( con43 * c3c4- c1345 ) * tmp3 * (u[k][j][i][1]*u[k][j][i][1])\n"; //282
    input += "- ( c3c4 - c1345 ) * tmp3 * (u[k][j][i][2]*u[k][j][i][2])\n"; //283
    input += "- ( c3c4 - c1345 ) * tmp3 * (u[k][j][i][3]*u[k][j][i][3])\n"; //284
    input += "- c1345 * tmp2 * u[k][j][i][4];\n"; //285
    input += "njac[i][1][4] = ( con43 * c3c4- c1345 ) * tmp2 * u[k][j][i][1];\n"; //286
    input += "njac[i][2][4] = ( c3c4 - c1345 ) * tmp2 * u[k][j][i][2];\n"; //287
    input += "njac[i][3][4] = ( c3c4 - c1345 ) * tmp2 * u[k][j][i][3];\n"; //288
    input += "njac[i][4][4] = ( c1345 ) * tmp1;\n"; //289
    input += "}\n"; //290
    
    
    
    input += "lhsinit(lhs, isize);\n"; //291
    input += "for (i = 1; i <= isize-1; i++) {\n"; //292
    input += "tmp1 = dt * tx1;\n"; //293
    input += "tmp2 = dt * tx2;\n"; //294
    input += "lhs[i][0][0][0] = -tmp2 * fjac[i-1][0][0]\n"; //295
    input += "- tmp1 * njac[i-1][0][0]\n"; //296
    input += "- tmp1 * dx1;\n"; //297
    input += "lhs[i][0][1][0] = -tmp2 * fjac[i-1][1][0]\n"; //298
    input += "- tmp1 * njac[i-1][1][0];\n"; //299
    input += "lhs[i][0][2][0] = -tmp2 * fjac[i-1][2][0]\n"; //300
    input += "- tmp1 * njac[i-1][2][0];\n"; //301
    input += "lhs[i][0][3][0] = -tmp2 * fjac[i-1][3][0]\n"; //302
    input += "- tmp1 * njac[i-1][3][0];\n"; //303
    input += "lhs[i][0][4][0] = -tmp2 * fjac[i-1][4][0]\n"; //304
    input += "- tmp1 * njac[i-1][4][0];\n"; //305
    input += "lhs[i][0][0][1] = -tmp2 * fjac[i-1][0][1]\n"; //306
    input += "- tmp1 * njac[i-1][0][1];\n"; //307
    input += "lhs[i][0][1][1] = -tmp2 * fjac[i-1][1][1]\n"; //308
    input += "- tmp1 * njac[i-1][1][1]\n"; //309
    input += "- tmp1 * dx2;\n"; //310
    input += "lhs[i][0][2][1] = -tmp2 * fjac[i-1][2][1]\n"; //311
    input += "- tmp1 * njac[i-1][2][1];\n"; //312
    input += "lhs[i][0][3][1] = -tmp2 * fjac[i-1][3][1]\n"; //313
    input += "- tmp1 * njac[i-1][3][1];\n"; //314
    input += "lhs[i][0][4][1] = -tmp2 * fjac[i-1][4][1]\n"; //315
    input += "- tmp1 * njac[i-1][4][1];\n"; //316
    input += "lhs[i][0][0][2] = -tmp2 * fjac[i-1][0][2]\n"; //317
    input += "- tmp1 * njac[i-1][0][2];\n"; //318
    input += "lhs[i][0][1][2] = -tmp2 * fjac[i-1][1][2]\n"; //319
    input += "- tmp1 * njac[i-1][1][2];\n"; //320
    input += "lhs[i][0][2][2] = -tmp2 * fjac[i-1][2][2]\n"; //321
    input += "- tmp1 * njac[i-1][2][2]\n"; //322
    input += "- tmp1 * dx3;\n"; //323
    input += "lhs[i][0][3][2] = -tmp2 * fjac[i-1][3][2]\n"; //324
    input += "- tmp1 * njac[i-1][3][2];\n"; //325
    input += "lhs[i][0][4][2] = -tmp2 * fjac[i-1][4][2]\n"; //326
    input += "- tmp1 * njac[i-1][4][2];\n"; //327
    input += "lhs[i][0][0][3] = -tmp2 * fjac[i-1][0][3]\n"; //328
    input += "- tmp1 * njac[i-1][0][3];\n"; //329
    input += "lhs[i][0][1][3] = -tmp2 * fjac[i-1][1][3]\n"; //330
    input += "- tmp1 * njac[i-1][1][3];\n"; //331
    input += "lhs[i][0][2][3] = -tmp2 * fjac[i-1][2][3]\n"; //332
    input += "- tmp1 * njac[i-1][2][3];\n"; //333
    input += "lhs[i][0][3][3] = -tmp2 * fjac[i-1][3][3]\n"; //334
    input += "- tmp1 * njac[i-1][3][3]\n"; //335
    input += "- tmp1 * dx4;\n"; //336
    input += "lhs[i][0][4][3] = -tmp2 * fjac[i-1][4][3]\n"; //337
    input += "- tmp1 * njac[i-1][4][3];\n"; //338
    input += "lhs[i][0][0][4] = -tmp2 * fjac[i-1][0][4]\n"; //339
    input += "- tmp1 * njac[i-1][0][4];\n"; //340
    input += "lhs[i][0][1][4] = -tmp2 * fjac[i-1][1][4]\n"; //341
    input += "- tmp1 * njac[i-1][1][4];\n"; //342
    input += "lhs[i][0][2][4] = -tmp2 * fjac[i-1][2][4]\n"; //343
    input += "- tmp1 * njac[i-1][2][4];\n"; //344
    input += "lhs[i][0][3][4] = -tmp2 * fjac[i-1][3][4]\n"; //345
    input += "- tmp1 * njac[i-1][3][4];\n"; //346
    input += "lhs[i][0][4][4] = -tmp2 * fjac[i-1][4][4]\n"; //347
    input += "- tmp1 * njac[i-1][4][4]\n"; //348
    input += "- tmp1 * dx5;\n"; //349
    input += "lhs[i][1][0][0] = 1.0\n"; //350
    input += "+ tmp1 * 2.0 * njac[i][0][0]\n"; //351
    input += "+ tmp1 * 2.0 * dx1;\n"; //352
    input += "lhs[i][1][1][0] = tmp1 * 2.0 * njac[i][1][0];\n"; //353
    input += "lhs[i][1][2][0] = tmp1 * 2.0 * njac[i][2][0];\n"; //354
    input += "lhs[i][1][3][0] = tmp1 * 2.0 * njac[i][3][0];\n"; //355
    input += "lhs[i][1][4][0] = tmp1 * 2.0 * njac[i][4][0];\n"; //356
    input += "lhs[i][1][0][1] = tmp1 * 2.0 * njac[i][0][1];\n"; //357
    input += "lhs[i][1][1][1] = 1.0\n"; //358
    input += "+ tmp1 * 2.0 * njac[i][1][1]\n"; //359
    input += "+ tmp1 * 2.0 * dx2;\n"; //360
    input += "lhs[i][1][2][1] = tmp1 * 2.0 * njac[i][2][1];\n"; //361
    input += "lhs[i][1][3][1] = tmp1 * 2.0 * njac[i][3][1];\n"; //362
    input += "lhs[i][1][4][1] = tmp1 * 2.0 * njac[i][4][1];\n"; //363
    input += "lhs[i][1][0][2] = tmp1 * 2.0 * njac[i][0][2];\n"; //364
    input += "lhs[i][1][1][2] = tmp1 * 2.0 * njac[i][1][2];\n"; //365
    input += "lhs[i][1][2][2] = 1.0\n"; //366
    input += "+ tmp1 * 2.0 * njac[i][2][2]\n"; //367
    input += "+ tmp1 * 2.0 * dx3;\n"; //368
    input += "lhs[i][1][3][2] = tmp1 * 2.0 * njac[i][3][2];\n"; //369
    input += "lhs[i][1][4][2] = tmp1 * 2.0 * njac[i][4][2];\n"; //370
    input += "lhs[i][1][0][3] = tmp1 * 2.0 * njac[i][0][3];\n"; //371
    input += "lhs[i][1][1][3] = tmp1 * 2.0 * njac[i][1][3];\n"; //372
    input += "lhs[i][1][2][3] = tmp1 * 2.0 * njac[i][2][3];\n"; //373
    input += "lhs[i][1][3][3] = 1.0\n"; //374
    input += "+ tmp1 * 2.0 * njac[i][3][3]\n"; //375
    input += "+ tmp1 * 2.0 * dx4;\n"; //376
    input += "lhs[i][1][4][3] = tmp1 * 2.0 * njac[i][4][3];\n"; //377
    input += "lhs[i][1][0][4] = tmp1 * 2.0 * njac[i][0][4];\n"; //378
    input += "lhs[i][1][1][4] = tmp1 * 2.0 * njac[i][1][4];\n"; //379
    input += "lhs[i][1][2][4] = tmp1 * 2.0 * njac[i][2][4];\n"; //380
    input += "lhs[i][1][3][4] = tmp1 * 2.0 * njac[i][3][4];\n"; //381
    input += "lhs[i][1][4][4] = 1.0\n"; //382
    input += "+ tmp1 * 2.0 * njac[i][4][4]\n"; //383
    input += "+ tmp1 * 2.0 * dx5;\n"; //384
    input += "lhs[i][2][0][0] = tmp2 * fjac[i+1][0][0]\n"; //385
    input += "- tmp1 * njac[i+1][0][0]\n"; //386
    input += "- tmp1 * dx1;\n"; //387
    input += "lhs[i][2][1][0] = tmp2 * fjac[i+1][1][0]\n"; //388
    input += "- tmp1 * njac[i+1][1][0];\n"; //389
    input += "lhs[i][2][2][0] = tmp2 * fjac[i+1][2][0]\n"; //390
    input += "- tmp1 * njac[i+1][2][0];\n"; //391
    input += "lhs[i][2][3][0] = tmp2 * fjac[i+1][3][0]\n"; //392
    input += "- tmp1 * njac[i+1][3][0];\n"; //393
    input += "lhs[i][2][4][0] = tmp2 * fjac[i+1][4][0]\n"; //394
    input += "- tmp1 * njac[i+1][4][0];\n"; //395
    input += "lhs[i][2][0][1] = tmp2 * fjac[i+1][0][1]\n"; //396
    input += "- tmp1 * njac[i+1][0][1];\n"; //397
    input += "lhs[i][2][1][1] = tmp2 * fjac[i+1][1][1]\n"; //398
    input += "- tmp1 * njac[i+1][1][1]\n"; //399
    input += "- tmp1 * dx2;\n"; //400
    input += "lhs[i][2][2][1] = tmp2 * fjac[i+1][2][1]\n"; //401
    input += "- tmp1 * njac[i+1][2][1];\n"; //402
    input += "lhs[i][2][3][1] = tmp2 * fjac[i+1][3][1]\n"; //403
    input += "- tmp1 * njac[i+1][3][1];\n"; //404
    input += "lhs[i][2][4][1] = tmp2 * fjac[i+1][4][1]\n"; //405
    input += "- tmp1 * njac[i+1][4][1];\n"; //406
    input += "lhs[i][2][0][2] = tmp2 * fjac[i+1][0][2]\n"; //407
    input += "- tmp1 * njac[i+1][0][2];\n"; //408
    input += "lhs[i][2][1][2] = tmp2 * fjac[i+1][1][2]\n"; //409
    input += "- tmp1 * njac[i+1][1][2];\n"; //410
    input += "lhs[i][2][2][2] = tmp2 * fjac[i+1][2][2]\n"; //411
    input += "- tmp1 * njac[i+1][2][2]\n"; //412
    input += "- tmp1 * dx3;\n"; //413
    input += "lhs[i][2][3][2] = tmp2 * fjac[i+1][3][2]\n"; //414
    input += "- tmp1 * njac[i+1][3][2];\n"; //415
    input += "lhs[i][2][4][2] = tmp2 * fjac[i+1][4][2]\n"; //416
    input += "- tmp1 * njac[i+1][4][2];\n"; //417
    input += "lhs[i][2][0][3] = tmp2 * fjac[i+1][0][3]\n"; //418
    input += "- tmp1 * njac[i+1][0][3];\n"; //419
    input += "lhs[i][2][1][3] = tmp2 * fjac[i+1][1][3]\n"; //420
    input += "- tmp1 * njac[i+1][1][3];\n"; //421
    input += "lhs[i][2][2][3] = tmp2 * fjac[i+1][2][3]\n"; //422
    input += "- tmp1 * njac[i+1][2][3];\n"; //423
    input += "lhs[i][2][3][3] = tmp2 * fjac[i+1][3][3]\n"; //424
    input += "- tmp1 * njac[i+1][3][3]\n"; //425
    input += "- tmp1 * dx4;\n"; //426
    input += "lhs[i][2][4][3] = tmp2 * fjac[i+1][4][3]\n"; //427
    input += "- tmp1 * njac[i+1][4][3];\n"; //428
    input += "lhs[i][2][0][4] = tmp2 * fjac[i+1][0][4]\n"; //429
    input += "- tmp1 * njac[i+1][0][4];\n"; //430
    input += "lhs[i][2][1][4] = tmp2 * fjac[i+1][1][4]\n"; //431
    input += "- tmp1 * njac[i+1][1][4];\n"; //432
    input += "lhs[i][2][2][4] = tmp2 * fjac[i+1][2][4]\n"; //433
    input += "- tmp1 * njac[i+1][2][4];\n"; //434
    input += "lhs[i][2][3][4] = tmp2 * fjac[i+1][3][4]\n"; //435
    input += "- tmp1 * njac[i+1][3][4];\n"; //436
    input += "lhs[i][2][4][4] = tmp2 * fjac[i+1][4][4]\n"; //437
    input += "- tmp1 * njac[i+1][4][4]\n"; //438
    input += "- tmp1 * dx5;\n"; //439
    input += "}\n"; //440
    input += "# 486 \"experiment/setup/NPB3.3-SER-C/BT/x_solve.c\"\n"; //441
    input += "binvcrhs( lhs[0][1], lhs[0][2], rhs[k][j][0] );\n"; //442
    input += "for (i = 1; i <= isize-1; i++) {\n"; //443
    input += "matvec_sub(lhs[i][0], rhs[k][j][i-1], rhs[k][j][i]);\n"; //444
    input += "matmul_sub(lhs[i][0], lhs[i-1][2], lhs[i][1]);\n"; //445
    input += "binvcrhs( lhs[i][1], lhs[i][2], rhs[k][j][i] );\n"; //446
    input += "}\n"; //447
    input += "matvec_sub(lhs[isize][0], rhs[k][j][isize-1], rhs[k][j][isize]);\n"; //448
    input += "matmul_sub(lhs[isize][0], lhs[isize-1][2], lhs[isize][1]);\n"; //449
    input += "binvrhs( lhs[isize][1], rhs[k][j][isize] );\n"; //450
    input += "# 541 \"experiment/setup/NPB3.3-SER-C/BT/x_solve.c\"\n"; //451
    input += "for (i = isize-1; i >=0; i--) {\n"; //452
    input += "for (m = 0; m < 5; m++) {\n"; //453
    input += "for (n = 0; n < 5; n++) {\n"; //454
    input += "rhs[k][j][i][m] = rhs[k][j][i][m]\n"; //455
    input += "- lhs[i][2][n][m]*rhs[k][j][i+1][n];\n"; //456
    input += "}\n"; //457
    input += "}\n"; //458
    input += "}\n"; //459
    input += "}\n"; //460
    input += "}\n"; //461
    input += "if (timeron) { timer_stop(6);}\n"; //462
    input += "}\n"; //463

    set<string> temp;
    Scope* s = VariableScopeAnalysis::run(input, CPP, temp);
    
    ASSERT_TRUE(s);
    
    set<DataFlowInfo> data;
    unsigned int endScope;
    ASSERT_EQ(SCOPE_ANALYSIS_SUCCESS, getDataFlow(s, 230, endScope, data));
    EXPECT_EQ(290, endScope);
    
    DataFlowInfo i = {"i", COPY_OUT, "", ""};
    DataFlowInfo k = {"k", COPY_IN, "", ""};
    DataFlowInfo j = {"j", COPY_IN, "", ""};
    DataFlowInfo tmp1 = {"tmp1", COPY, "", ""};
    DataFlowInfo tmp2 = {"tmp2", COPY, "", ""};
    DataFlowInfo tmp3 = {"tmp3", COPY, "", ""};
    DataFlowInfo fjac = {"fjac", COPY, "", ""};
    DataFlowInfo njac = {"njac", COPY, "", ""};
    DataFlowInfo u = {"u", COPY_IN, "", ""};
    DataFlowInfo c1 = {"c1", COPY_IN, "", ""};
    DataFlowInfo c2 = {"c2", COPY_IN, "", ""};
    DataFlowInfo con43 = {"con43", COPY_IN, "", ""};
    DataFlowInfo c3c4 = {"c3c4", COPY_IN, "", ""};
    DataFlowInfo c1345 = {"c1345", COPY_IN, "", ""};
    DataFlowInfo isize = {"isize", COPY_IN, "", ""};
    DataFlowInfo square = {"square", COPY_IN, "", ""};
    DataFlowInfo rho_i = {"rho_i", COPY_IN, "", ""};
    
    EXPECT_TRUE(data.count(i));
    EXPECT_TRUE(data.count(k));
    EXPECT_TRUE(data.count(j));
    EXPECT_TRUE(data.count(tmp1));
    EXPECT_TRUE(data.count(tmp2));
    EXPECT_TRUE(data.count(tmp3));
    EXPECT_TRUE(data.count(fjac));
    EXPECT_TRUE(data.count(njac));
    EXPECT_TRUE(data.count(u)); //Fail
    EXPECT_TRUE(data.count(c1));
    EXPECT_TRUE(data.count(c2));
    EXPECT_TRUE(data.count(con43));
    EXPECT_TRUE(data.count(c3c4));
    EXPECT_TRUE(data.count(c1345));
    EXPECT_TRUE(data.count(isize));
    EXPECT_TRUE(data.count(square)); //Fail
    EXPECT_TRUE(data.count(rho_i)); //Fail
}

TEST(ScopeAnalysis, getDataFlow_BT_mini){
    string input = "extern double rho_i [40][40/2*2 +1][40/2*2 +1];\n"; //1
    input += "extern double square [40][40/2*2 +1][40/2*2 +1];\n"; //2
    input += "extern double u [40][40/2*2 +1][40/2*2 +1][5];\n"; //3
    input += "extern double fjac[40 +1][5][5];\n"; //4
    input += "extern double qs [40][40/2*2 +1][40/2*2 +1];\n"; //5
    input += "extern double tmp1, tmp2, tmp3,c1,c2;\n"; //6
    input += "void x_solve()\n"; //7
    input += "{\n"; //8
    input += "int i, j, k, m, n, isize;\n"; //9
    input += "isize = 1111;\n"; //10
    
    input += "for (k = 1; k <= 78-2; k++) {\n"; //11
    input += "for (j = 1; j <= 89-2; j++) {\n"; //12
    
    // [Parallelisation here]
    input += "for (i = 0; i <= isize; i++) {\n"; //13
    input += "tmp1 = rho_i[k][j][i];\n"; //14
    input += "fjac[i][0][1] = -(u[k][j][i][1] * tmp2 * u[k][j][i][1])\n"; //15
    input += "+ c2 * qs[k][j][i];\n"; //16
    input += "fjac[i][1][1] = ( 2.0 - c2 ) * ( u[k][j][i][1] / u[k][j][i][0] );\n"; //17
    input += "fjac[i][2][1] = -c2 * ( u[k][j][i][2] * tmp1 );\n"; //18
    input += "fjac[i][3][1] = -c2 * ( u[k][j][i][3] * tmp1 );\n"; //19
    input += "fjac[i][4][1] = c2;\n"; //20
    input += "fjac[i][0][4] = ( c2 * 2.0 * square[k][j][i] - c1 * u[k][j][i][4] );\n"; //21
    input += "}\n"; //22
    input += "}\n"; //23
    input += "}\n"; //24
    input += "}\n"; //25
    
    set<string> temp;
    Scope* s = VariableScopeAnalysis::run(input, CPP, temp);
    
    ASSERT_TRUE(s);
    
    set<DataFlowInfo> data;
    unsigned int endScope;
    ASSERT_EQ(SCOPE_ANALYSIS_SUCCESS, getDataFlow(s, 13, endScope, data));
    EXPECT_EQ(22, endScope);
    
    DataFlowInfo u = {"u", COPY_IN, "", ""};
    DataFlowInfo square = {"square", COPY_IN, "", ""};
    DataFlowInfo rho_i = {"rho_i", COPY_IN, "", ""};
    
    EXPECT_TRUE(data.count(u));
    EXPECT_TRUE(data.count(square));
    EXPECT_TRUE(data.count(rho_i));
}



TEST(ScopeAnalysis, getDataFlow_EmbarrisinglyParallel){
    //An Example from the Embarrisingly Parallel benchmark suite
    
    string input = "#define MAX(X,Y)  (((X) > (Y)) ? (X) : (Y))\n";             //1
    input += "#define fabs(x)   (x<0)?-x:x\n";                                  //2
    input += "#define MK        16\n";                                          //3
    input += "#define NK        (1 << MK)\n";                                   //4
    input += "#define NQ        10\n";                                          //5
    input += "static double x[2*NK];\n";                                        //6
    input += "static double q[NQ];\n";                                          //7
    input += "int main(){\n";                                                   //8
    input += "double t1,t2,t3,t4,x1,x2,gc;\n";                                  //9
    input += "double sx=6.0; double sy=77;\n";                                  //10
    input += "int l,i;\n";                                                      //11
    input += "for (i = 0; i < 2 * NK; i++) {\n";                                //12
    input += "q[i] = 0.0;\n";                                                   //13
    input += "}\n";                                                             //14
    input += "t1 = 6.0;\n";                                                     //15
    input += "t2 = 7.0 * t1;\n";                                                //16
    input += "t3 = 8.0;\n";                                                     //17
    input += "gc=0.0;\n";                                                       //18
    
    input += "for (i = 0; i < NK; i++) { //LOOP Parallelisation here\n";        //19
    input += "x1 = 2.0 * x[2*i] - 1.0;\n";                                      //20
    input += "x2 = 2.0 * x[2*i+1] - 1.0;\n";                                    //21
    input += "t1 = x1 * x1 + x2 * x2;\n";                                       //22
    input += "if (t1 <= 1.0) {\n";                                              //23
    input += "t2   = (-2.0 * (t1) / t1);\n";                                    //24
    input += "t3   = (x1 * t2);\n";                                             //25
    input += "t4   = (x2 * t2);\n";                                             //26
    input += "l    = MAX(fabs(t3), fabs(t4));\n";                               //27
    input += "q[l] = q[l] + 1.0;\n";                                            //28
    input += "sx   = sx + t3;\n";                                               //29
    input += "sy   = sy + t4;\n";                                               //30
    input += "}\n";                                                             //31
    input += "}\n";                                                             //32
    
    input += "for (i = 0; i < NQ; i++) {\n";                                    //33
    input += "gc = gc + q[i];\n";                                               //34
    input += "}\n";                                                             //35
    input += "int y = sy;\n";                                                   //36
    input += "int x8 = sx;\n";                                                   //37
    input += "}";                                                               //38
    
    set<string> temp;
    Scope* s = VariableScopeAnalysis::run(input, CPP, temp);
    
    ASSERT_TRUE(s);
    
    set<DataFlowInfo> data;
    unsigned int endScope;
    ASSERT_EQ(SCOPE_ANALYSIS_SUCCESS, getDataFlow(s, 19, endScope, data));
    EXPECT_EQ(32, endScope);
    
    DataFlowInfo x1 = {"x1", CREATE, "", ""};
    DataFlowInfo x2 = {"x2", CREATE, "", ""};
    DataFlowInfo x = {"x", COPY_IN, "", ""};
    DataFlowInfo t1 = {"t1", COPY_IN, "", ""};
    DataFlowInfo t2 = {"t2", COPY_IN, "", ""};
    DataFlowInfo t3 = {"t3", COPY_IN, "", ""};
    DataFlowInfo t4 = {"t4", CREATE, "", ""};
    DataFlowInfo q = {"q", COPY, "", ""};
    DataFlowInfo sx = {"sx", COPY, "", ""};
    DataFlowInfo sy = {"sy", COPY, "", ""};
    DataFlowInfo l = {"l", CREATE, "", ""};
    DataFlowInfo i = {"i", COPY, "", ""};
    
    EXPECT_EQ(12, data.size());
    EXPECT_TRUE(data.count(x1));
    EXPECT_TRUE(data.count(x2));
    EXPECT_TRUE(data.count(x));
    EXPECT_TRUE(data.count(t1));
    EXPECT_TRUE(data.count(t2));
    EXPECT_TRUE(data.count(t3));
    EXPECT_TRUE(data.count(t4));
    EXPECT_TRUE(data.count(q));
    EXPECT_TRUE(data.count(sx));
    EXPECT_TRUE(data.count(sy));
    EXPECT_TRUE(data.count(l));
}

TEST(ScopeAnalysis, DataFlowInfo_equals){
    DataFlowInfo x1 = {"x", CREATE, "", ""};
    DataFlowInfo x2 = {"x", COPY, "", ""};
    DataFlowInfo x3 = {"x", COPY, "", ""};
    DataFlowInfo y1 = {"y", CREATE, "", ""};
    DataFlowInfo y2 = {"y", COPY, "", ""};
    
    EXPECT_EQ(x2,x3);
    EXPECT_NE(x1, x2);
    EXPECT_NE(x1, x3);
    EXPECT_NE(x1, y1);
    EXPECT_NE(x1, y2);
}



