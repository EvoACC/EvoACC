//
//  CommandLineInterface.cpp
//
//  This is the command line interface for the data-insertion-finder tool. For the meantime
//  it also contains quite a bit of processing, so a bit more than just an interface.
//

#include <iostream>
#include <clang/VariableScopeAnalysis/Scope.h>
#include <clang/VariableScopeAnalysis/ScopeAnalysis.h>
#include <clang/VariableScopeAnalysis/VariableScopeAnalysis.h>
#include <fstream>
#include <iostream>
#include <map>
#include <set>

struct ValidInsertionPoint{
    unsigned int startLine;
    unsigned int endLine;
    unsigned int structuresCovered;
    unsigned int dataCaptured;
    unsigned int updateStatementsRequired; //TODO: always zero for the meantime.
};

std::map<SourceCodeLocation, std::set<DataFlowInfo> > getPresentDirectives(Scope* s, fstream& csvFile){
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
        getline(csvFile, endRange,'\n');
        
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
        
        DataFlowInfo dfi = {variable,flowStatus,startRange,endRange};
        
        toReturn[scl].insert(dfi);
    }
    return toReturn;
}


int main(int argc, char* argv[]){
    // argv[1] == C Source file
    // argv[2] == CSV file containing data directives. In format: <start_line>,<end_line>,<COPY/COPYIN/etc.>,<variable_name>
    // argv[3] == Language (C or CPP)
    // argv[4...X] == includes
    
    if(argc < 4){
        std::cerr << "Invalid number of arguments. Correct:" << std::endl
        << "data-insertion-finder <source_file> <present_directives_csv> <language> [includes...]" << std::endl;
        exit(EXIT_FAILURE);
    }
    
    fstream inputFile(argv[1]);
    fstream csvFile(argv[2]);
    std::string input;
    std::string tempString;
    while(std::getline(inputFile, tempString)){
        input += tempString + '\n';
    }
    
    LANGUAGE lang = C;
    if(strcmp(argv[3], "C")==0){
        lang = C;
    } else if(strcmp(argv[3], "CPP")==0){
        lang = CPP;
    } else {
        cerr << "Invalid lagnuage specified. Valid inputs:" << endl
        << "C, CPP" << endl;
        return EXIT_FAILURE;
    }
    
    set<string> includes;
    for(int i=4; i<argc; i++){
        includes.insert(argv[i]);
    }
    
    vector<ValidInsertionPoint> toReturn;
    Scope* scope = VariableScopeAnalysis::run(input, lang, includes);
    
    if(!scope){
        std::cerr << "Input C file could not be parsed by clang" << std::endl;
        exit(EXIT_FAILURE);
    }
    
    const map<SourceCodeLocation, set<DataFlowInfo> > presentDirectives = getPresentDirectives(scope, csvFile);
    
    std::set<SourceCodeLocation> presentDirectivesLocation;
    for(auto const& p : presentDirectives){
        presentDirectivesLocation.insert(p.first);
    }
    
    delete scope;
    scope = VariableScopeAnalysis::run(input, lang, includes, presentDirectivesLocation);
    
    std::set<unsigned int> startingPoints;
    std::set<unsigned int> endingPoints;
    for(SourceCodeLocation scl : presentDirectivesLocation){
        startingPoints.insert(scl.startLineNo);
        endingPoints.insert(scl.endLineNo+1);
    }
    
    for(std::set<unsigned int>::iterator start = startingPoints.begin(); start != startingPoints.end(); ++start){
        for(std::set<unsigned int>::iterator end = endingPoints.begin(); end != endingPoints.end(); ++end){
            if((*end) > (*start)){
                set<DataFlowInfo> flowData;
                set<UpdateStatement> updateData;
                if(getDataDirectiveDataFlow(scope, (*start), (*end), presentDirectives, flowData, updateData) == SCOPE_ANALYSIS_SUCCESS && !flowData.empty()){
                    std::cout << (*start) << ',' << (*end) << endl;
                }
            }
        }
    }
    
    if(scope->getRoot()){
        scope = scope->getRoot();
    }
    
    delete scope;
    
}
