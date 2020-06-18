//
//  CommandLineInterface.cpp
//
//  This is the command line interface for the data-directive-analyser tool. For the meantime
//  it also contains quite a bit of processing, so a bit more than just an interface.
//

#include <clang/VariableScopeAnalysis/ScopeAnalysis.h>
#include <clang/VariableScopeAnalysis/Scope.h>
#include <clang/VariableScopeAnalysis/VariableScopeAnalysis.h>
#include <llvm/Support/Casting.h>
#include <string>
#include <vector>
#include <map>
#include <set>
#include <iostream>
#include <fstream>

using namespace std;

/*struct PresentDirective{
    SourceCodeLocation loc;
    map<string, Flow> variables;
};*/

/*set<string> getVariablesOfInterest(vector<PresentDirective> presentDirectives, set<DataFlowInfo> dataFlowInfo, SourceCodeLocation loc){
    map<string,int> variableCount;
    
    
    for(unsigned long i=0; i<presentDirectives.size(); i++){
        PresentDirective pd = presentDirectives.at(i);
        if(loc.contains(pd.loc)){
            for(auto const& p : pd.variables){
                for(set<DataFlowInfo>::iterator it=dataFlowInfo.begin(); it!=dataFlowInfo.end(); ++it){
                    DataFlowInfo dfi = (*it);
                    if(dfi.variable == p.first){
                        if(!variableCount.count(p.first)){
                            variableCount[p.first]=0;
                        }
                        variableCount[p.first]++;
                        break;
                    }
                }
            }
        }
    }
    
    set<string> toReturn;
    for(auto const& v : variableCount){
        if(v.second >= 2){
            toReturn.insert(v.first);
        }
    }
    
    return toReturn;
}

PresentDirective* getPresentDirective(vector<PresentDirective>& presentDirectives, SourceCodeLocation loc){
    
    for(unsigned long i = 0; i<presentDirectives.size(); i++){
        PresentDirective* presentDirective = &presentDirectives.at(i);
        if(presentDirective->loc == loc){
            return presentDirective;
        }
    }
    
    return NULL;
}*/

/*bool withinPresentDirective(const vector<PresentDirective>& presentDirectives, SourceCodeLocation loc){
    for(unsigned long i=0; i < presentDirectives.size(); i++){
        if(presentDirectives.at(i).loc.contains(loc)){
            return true;
        }
    }
    return false;
}*/

/*void printUpdate(Scope* s, vector<PresentDirective>& presentDirectives, set<string>& varsOfInterest, set<string>& variablesToCopyBack){
    vector<ProgramElement*> programElements = s->getElements();
    for(unsigned long i=0; i < programElements.size(); i++){
        ProgramElement* pe = programElements.at(i);
        PresentDirective* pd = getPresentDirective(presentDirectives, pe->getSourceCodeLocation());
        if(!pd){
            if(!withinPresentDirective(presentDirectives, pe->getSourceCodeLocation())){
                if(Scope* subS = llvm::dyn_cast<Scope>(pe)){
                    printUpdate(subS, presentDirectives, varsOfInterest, variablesToCopyBack);
                } else if(VariableEvent* ve = llvm::dyn_cast<VariableEvent>(pe)){
                    if(varsOfInterest.count(ve->getVariable()) && !variablesToCopyBack.count(ve->getVariable())){
                        //TODO: This could be optimised, i.e. what about cases where the data is copied to the GPU and only read on the host before being written to by a compute region (in such a case and UPDATE_HOST is not necessisary). For the meantime this is a decent placeholder (I see no reason why it won't work).
                        cout << pe->getSourceCodeLocation().startLineNo << ',' << pe->getSourceCodeLocation().endLineNo << ',' << "UPDATE_HOST" << ',' << ve->getVariable() << endl;
                        variablesToCopyBack.insert(ve->getVariable());
                    }
                } else {
                    cerr << "ERROR: Unrecognised ProgramElement type (not VariableEvent or Scope)" << endl;
                    exit(EXIT_FAILURE);
                }
            }
        } else {
            for(auto const& p : pd->variables){
                if(variablesToCopyBack.count(p.first)){
                    cout << pe->getSourceCodeLocation().startLineNo << ',' << pe->getSourceCodeLocation().endLineNo << ',' << "UPDATE_DEVICE" << ',' << p.first << endl;
                    variablesToCopyBack.erase(p.first);
                }
            }
        }
    }
}*/

//TODO: Complete duplication of method in data-insertion-finder. Should probably create a utils
map<SourceCodeLocation, set<DataFlowInfo> > getPresentDirectives(Scope* s, ifstream& csvFile){
    std::map<SourceCodeLocation, std::set<DataFlowInfo> > toReturn;
    
    string startLine;
    string endLine;
    string status;
    string variable;
    string startRange;
    string endRange;
    while(getline(csvFile, startLine, ',')){
        getline(csvFile, endLine, ',');
        getline(csvFile, status, ',');
        getline(csvFile, variable, ',');
        getline(csvFile, startRange, ',');
        getline(csvFile, endRange, '\n');
        
        unsigned int startLineNo = atoi(startLine.c_str());
        unsigned int endLineNo = atoi(endLine.c_str());
        
        ProgramElement* pe = s->getRoot()->getElementAtLine(startLineNo);
        Scope* tempScope = NULL;
        SourceCodeLocation scl = {NULL,NULL,NULL,NULL};
        
        if(!pe || !(tempScope = llvm::dyn_cast<Scope>(pe)) || (tempScope->getSourceCodeLocation().endLineNo != endLineNo && startLineNo != endLineNo)){
            scl = {startLineNo, 0, endLineNo, 0};
        } else {
            scl = tempScope->getSourceCodeLocation();
        }
        
        /*if(pe && (tempScope = llvm::dyn_cast<Scope>(pe))){
         scl = tempScope->getSourceCodeLocation();
         } else {
         scl = {startLineNo, 0, endLineNo, 0};
         }*/
        
        if(!toReturn.count(scl)){
            std::set<DataFlowInfo> toInsert;
            toReturn[scl] = toInsert;
        }
        
        Flow flowStatus;
        if(status == "COPY"){
            flowStatus = COPY;
        } else if(status == "COPY_IN"){
            flowStatus = COPY_IN;
        } else if(status == "COPY_OUT"){
            flowStatus = COPY_OUT;
        } else if(status == "PRESENT"){
            flowStatus = PRESENT;
        } else if(status == "CREATE"){
            flowStatus = CREATE;
        } else {
            cerr << "Cannot process flow status '" << status << "'" << endl;
            exit(EXIT_FAILURE);
        }
        
        DataFlowInfo dfi = {variable,flowStatus, startRange, endRange};
        
        toReturn[scl].insert(dfi);
    }
    return toReturn;
}

int main(int argc, char* argv[]){
    
    //TODO: A more professional CLI would be beneficial
    //argv[1] == C File
    //argv[2] == Data directive scope start line number
    //argv[3] == Data directive scope end line number
    //argv[4] == CSV file containing data directives. In format: <start_line>,<end_line>,<COPY/COPYIN/etc.>,<variable_name>, <start_range>, <end_range>
    //argv[5] == Language (C or CPP)
    //argv[6...X] == includes
    
    if(argc < 6){
        cerr << "Invalid number or argument. Correct:" << endl
        << "data-directive-analyser <source_file> <data_directive_start_line> <data_directive_end_line> <present_directives_csv> <language> [includes...]" << endl;
        return EXIT_FAILURE;
    }
    
    ifstream sourceFile(argv[1]);
    unsigned int startLineNo = atoi(argv[2]);
    unsigned int endLineNo = atoi(argv[3]);
    ifstream csvFile(argv[4]);
    
    string input;
    string tempString;
    while(getline(sourceFile, tempString)){
        input += tempString + "\n";
    }
    
    LANGUAGE lang = C;
    if(strcmp(argv[5],"C")==0){
        lang = C;
    } else if(strcmp(argv[5], "CPP")==0){
        lang = CPP;
    } else {
        cerr << "Invalid lagnuage specified. Valid inputs" << endl
        << "C, CPP" << endl;
        return EXIT_FAILURE;
    }
    
    set<string> includes;
    for(int i=6; i<argc; i++){
        includes.insert(argv[i]);
    }
    
    Scope* s = VariableScopeAnalysis::run(input, lang, includes);
    
    map<SourceCodeLocation, set<DataFlowInfo> > presentDirectives = getPresentDirectives(s, csvFile);
    
    delete s;
    
    set<SourceCodeLocation> presentDirectivesSCLs;
    for(auto const& p : presentDirectives){
        presentDirectivesSCLs.insert(p.first);
    }
    
    s = VariableScopeAnalysis::run(input, lang, includes, presentDirectivesSCLs);
    
    SourceCodeLocation newScopeLoc ={startLineNo, 0, endLineNo, 0};
    
    if(!s->addScope(newScopeLoc)){
        cerr << "Invalid scope insertion location" << endl;
        return EXIT_FAILURE;
    }
    
    int errCode;
    set<DataFlowInfo> flowData;
    set<UpdateStatement> updateData;
    if((errCode = getDataDirectiveDataFlow(s, startLineNo, endLineNo, presentDirectives, flowData, updateData))){
        switch(errCode){
            case SCOPE_ANALYSIS_INVALID_LINE_NO:
                cerr << "Invalid scope location specified" << endl;
                break;
            case SCOPE_ANALYSIS_SCOPE_INVALID:
                cerr << "Scope input incorrect" << endl;
                break;
            default:
                cerr << "Unknown error code triggered" << endl;
                break;
        }
        return EXIT_FAILURE;
    }
    
  /*  if(endLineNo != tempEnd){
        cerr << "Inconsisteny with end of scope" << endl;
        return EXIT_FAILURE;
    }*/
    

    delete s->getRoot();

    
    for(set<DataFlowInfo>::iterator it = flowData.begin(); it!=flowData.end(); ++it){
        DataFlowInfo dfi = (*it);
        cout << startLineNo << ',' << endLineNo << ',' << toString(dfi.flow) << ',' << dfi.variable << ',' << dfi.startRange << ',' << dfi.endRange << endl;
    }
    
    //cout << pe->getSourceCodeLocation().startLineNo << ',' << pe->getSourceCodeLocation().endLineNo << ',' << "UPDATE_HOST" << ',' << ve->getVariable() << endl;
    for(set<UpdateStatement>::iterator it = updateData.begin(); it!=updateData.end(); ++it){
        UpdateStatement us = (*it);
        cout << us.lineNo << ',' << us.lineNo << ',' << toString(us.update) << ',' << us.variable << endl;
    }
    
    
  /*  set<string> varOfInterest =  getVariablesOfInterest(presentDirectives, output, newScopeLoc);
    
    //TODO: right now we just treat a data directive as if it were a FOR loop, i.e. analyse the data flow of the variable in and out of the region. We then remove any directives not associated with any inner compute regions (as determined by 'presentDirectives'). This is somewhat inefficient and the program would be improved by using the inner compute regions, as well as other information, to determine what data regions should be.
    //Print out the data directive info
    for(set<DataFlowInfo>::iterator it = output.begin(); it!=output.end(); ++it){
        DataFlowInfo dfi = (*it);
        if(varOfInterest.count(dfi.variable)){
            cout << newScopeLoc.startLineNo << ',' << newScopeLoc.endLineNo << ',' << toString(dfi.flow) << ',' << dfi.variable << endl;
        }
    }
    
    
    ProgramElement* temp = s->getRoot()->getElementAt(newScopeLoc);
    if(temp && llvm::isa<Scope>(temp)){
        s = dyn_cast<Scope>(temp);
        set<string> variablesToCopyBack;
        printUpdate(s, presentDirectives, varOfInterest, variablesToCopyBack);
    } else {
        cout << "Could not find inserted scope" << endl;
        return EXIT_FAILURE;
    }*/
    
    return EXIT_SUCCESS;
}
