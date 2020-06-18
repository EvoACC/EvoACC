//
//  ScopeAnalysis.h
//
//
//  This contains all the functions related to OpenACC specific functionality.
//
//  TODO: This does not have any unit tests yet

#ifndef ScopeAnalysis_h
#define ScopeAnalysis_h

#include <clang/VariableScopeAnalysis/Scope.h>
#include <string>
#include <set>
#include <map>
#include <vector>

using namespace std;

enum Flow {COPY_IN, COPY_OUT, COPY, PRESENT, CREATE};

enum Update {HOST, DEVICE};

struct UpdateStatement {
    unsigned int lineNo;
    string variable;
    Update update;
};

inline bool operator<(const UpdateStatement& lhs, const UpdateStatement& rhs)
{
    if(lhs.lineNo != rhs.lineNo){
        return lhs.lineNo < rhs.lineNo;
    } else {
        return lhs.variable < rhs.variable;
    }
}

struct DataFlowInfo {
	string variable;
	Flow flow;
    string startRange;
    string endRange;
};

inline bool operator<(const DataFlowInfo& lhs, const DataFlowInfo& rhs)
{
    if(lhs.variable == rhs.variable){
        if(lhs.flow == rhs.flow){
            if(lhs.startRange == rhs.startRange){
                return lhs.endRange < rhs.endRange;
            }
            return lhs.startRange < rhs.startRange;
        }
        return lhs.flow < rhs.flow;
    }
    return lhs.variable < rhs.variable;
}

inline bool operator==(const DataFlowInfo& lhs, const DataFlowInfo& rhs){
    return lhs.variable == rhs.variable && lhs.flow == rhs.flow && lhs.startRange == rhs.startRange && lhs.endRange == rhs.endRange;
}

inline bool operator!=(const DataFlowInfo& lhs, const DataFlowInfo& rhs){
    return !(lhs==rhs);
}

// Return/Error codes
#define SCOPE_ANALYSIS_SUCCESS 0
#define SCOPE_ANALYSIS_INVALID_LINE_NO 1
#define SCOPE_ANALYSIS_SCOPE_INVALID 2

int getDataFlow(Scope* s, unsigned int lineNo, unsigned int& endScope, set<DataFlowInfo>& data);

int getDataDirectiveDataFlow(Scope* s, unsigned int lineNo, unsigned int endScope, const map<SourceCodeLocation, set<DataFlowInfo> > presentDirectives, set<DataFlowInfo>& flowData, set<UpdateStatement>& updateData);

//DataFlowInfo getDataFlowInfo(string var, unisgned int lineNo, unsigned int endScope, set<DataFlowInfo> presentDirecives);

string toString(Flow f);

string toString(Update u);

#endif /*ScopeAnalysis_h*/
