#include <clang/VariableScopeAnalysis/ScopeAnalysis.h>
#include <clang/VariableScopeAnalysis/Scope.h>
#include <clang/VariableScopeAnalysis/VariableScopeAnalysis.h>
#include <vector>
#include <fstream>
#include <iostream>

using namespace std;

int main(int argc, char* argv[]){
    
    //TODO: Would be good to have a more professional CLI, however, for now this will do
    // argv[1] == The source-file
    // argv[2] == The line number
    // argv[3] == Language (C or CPP)
    // argv[4...X] == includes
    
    if(argc < 4){
        cerr << "Invalid number or argument. Correct:" << endl
        << "loop-analyser <source_file> <line_number> <language> [includes...]" << endl;
        return EXIT_FAILURE;
    }
    
    ifstream infile(argv[1]);
    unsigned int lineNo = atoi(argv[2]);
    unsigned int endScopeLine;
    set<DataFlowInfo> output;
    
    string input;
    string temp;
    while(getline(infile, temp)){
        input += temp + "\n";
    }
    
    LANGUAGE lang = C;
    if(strcmp(argv[3],"C")==0){
        lang = C;
    } else if(strcmp(argv[3], "CPP")==0){
        lang = CPP;
    } else {
        cerr << "Invalid lagnuage specified. Valid inputs" << endl
        << "C, CPP" << endl;
        return EXIT_FAILURE;
    }
    
    set<string> includes;
    for(int i=4; i<argc; i++){
        includes.insert(argv[i]);
    }
    
    Scope* scope = VariableScopeAnalysis::run(input, lang, includes);
    if(!scope){
        cerr << "The source code given could not be parsed by clang. See errors output" << endl;
        return EXIT_FAILURE;
    }
    
    int errorCode;
    if((errorCode = getDataFlow(scope, lineNo, endScopeLine, output))){
        switch(errorCode){
            case SCOPE_ANALYSIS_INVALID_LINE_NO:
                cerr << "Invalid line number. Line number must be a scope" << endl;
                break;
            case SCOPE_ANALYSIS_SCOPE_INVALID:
                cerr << "Invalid scope" << endl;
                break;
            default:
                cerr << "Error code " << errorCode << " not recognised" << endl;
                break;
        }
        delete scope->getRoot();
        return EXIT_FAILURE;
    }

	for(set<DataFlowInfo>::iterator it = output.begin(); it != output.end(); ++it){
		DataFlowInfo current = (*it);
        
        string flowValue;
        switch(current.flow){
            case COPY_IN:
                flowValue = "COPY_IN";
                break;
            case COPY_OUT:
                flowValue = "COPY_OUT";
                break;
            case COPY:
                flowValue = "COPY";
                break;
            case PRESENT:
                flowValue = "PRESENT";
                break;
            case CREATE:
                flowValue = "CREATE";
                break;
            default:
                cerr << "Flow value not recognised for " + current.variable <<endl;
                return EXIT_FAILURE;
                break;
        }
        
        cout << lineNo << ',' << endScopeLine  << ',' << current.variable.c_str() << ',' << flowValue << endl;
	}

	return EXIT_SUCCESS;
}
