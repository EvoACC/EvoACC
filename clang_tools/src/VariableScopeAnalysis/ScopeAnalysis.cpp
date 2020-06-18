#include <clang/VariableScopeAnalysis/ScopeAnalysis.h>
#include <llvm/Support/Casting.h>
#include <stdio.h>
#include <iostream>

using namespace std;

struct VariableData{
    bool isReadBefore;
    bool isWrittenBefore;
    bool isReadWithin;
    bool isWrittenWithin;
    bool isReadAfter;
    bool isWrittenAfter;
    bool isArray;
    bool isPointer;
};

enum TRAVERSE_STATUS{
    BEFORE,
    WITHIN,
    AFTER
};

inline TRAVERSE_STATUS getStatus(SourceCodeLocation scopeLoc, SourceCodeLocation target){
    
    if(target < scopeLoc){
        return BEFORE;
    } else if(target > scopeLoc){
        return AFTER;
    }
    
    return WITHIN;
}

void addVariable(map<string,VariableData>& data, const string& var, bool isArray, bool isPointer){
    if(!data.count(var)){
        data[var] = {false, false, false, false, false, false, isArray, isPointer};
    }
}

void isRead(map<string, VariableData>& data, const string& var, TRAVERSE_STATUS status){
    if(data.count(var)){
        switch(status){
            case BEFORE:
                data[var].isReadBefore = true;
                break;
            case WITHIN:
                data[var].isReadWithin = true;
                break;
            case AFTER:
                data[var].isReadAfter = true;
                break;
        }
    }
}

void isRead(map<string,VariableData>& data, const string& var, SourceCodeLocation scopeLoc, SourceCodeLocation target){
    isRead(data, var, getStatus(scopeLoc, target));
}

void isWritten(map<string, VariableData>& data, const string& var, TRAVERSE_STATUS status){
    if(data.count(var)){
        switch(status){
            case BEFORE:
                data[var].isWrittenBefore = true;
                break;
            case WITHIN:
                data[var].isWrittenWithin = true;
                break;
            case AFTER:
                data[var].isWrittenAfter = true;
                break;
        }
    }
}

void isWritten(map<string,VariableData>& data, const string& var, SourceCodeLocation scopeLoc, SourceCodeLocation target){
    isWritten(data, var, getStatus(scopeLoc, target));
}

void processScope(Scope* s, SourceCodeLocation loc, map<string, VariableData>& data){
    for(unsigned int i = 0; i < s->getElements().size(); i++){
        Scope* subS;
        if((subS = llvm::dyn_cast<Scope>(s->getElements().at(i)))){
            processScope(subS, loc, data);
        } else if(VariableDeclared* vr = llvm::dyn_cast<VariableDeclared>(s->getElements().at(i))){
            if(s->getSourceCodeLocation().contains(loc) ){//}|| getStatus(loc, vr->getSourceCodeLocation()) == BEFORE){
                addVariable(data, vr->getVariable(), vr->isArray(), vr->isPointer());
                
                if(vr->isExtern() ||vr->isPointer() || vr->isReference() || vr->isStatic()){
                    isRead(data, vr->getVariable(), BEFORE);
                    isRead(data, vr->getVariable(), AFTER);
                    isWritten(data, vr->getVariable(), BEFORE);
                    isWritten(data, vr->getVariable(), AFTER);
                }
                
                if(vr->isFunctionParameter()){
                    isRead(data, vr->getVariable(), BEFORE);
                    isWritten(data, vr->getVariable(), BEFORE);
                }
            }
        } else if(VariableRead* vr = llvm::dyn_cast<VariableRead>(s->getElements().at(i))){
            isRead(data, vr->getVariable(), loc, vr->getSourceCodeLocation());
        } else if(VariableAssigned* va = llvm::dyn_cast<VariableAssigned>(s->getElements().at(i))){
            isWritten(data, va->getVariable(), loc, va->getSourceCodeLocation());
        } else if(VariableUsedInFunction* vf = llvm::dyn_cast<VariableUsedInFunction>(s->getElements().at(i))){
            //TODO: This is a very simple over approximation. More data could be extracted to make this better better
            isRead(data, vf->getVariable(), loc, vf->getSourceCodeLocation());
            isWritten(data, vf->getVariable(), loc, vf->getSourceCodeLocation());
        }
    }
}

SourceCodeLocation* getLocation(Scope* s, unsigned int lineNo){
    if(s->getSourceCodeLocation().startLineNo == lineNo){
        SourceCodeLocation scl;
        scl.startLineNo = s->getSourceCodeLocation().startLineNo;
        scl.startColNo = s->getSourceCodeLocation().startColNo;
        scl.endLineNo = s->getSourceCodeLocation().endLineNo;
        scl.endColNo = s->getSourceCodeLocation().endColNo;
        SourceCodeLocation* sclToReturn = &scl;
        return sclToReturn;
    }
    
    if(s->getSourceCodeLocation().startLineNo < lineNo && s->getSourceCodeLocation().endLineNo > lineNo){
        for(unsigned long i = 0; i < s->getElements().size(); i++){
            ProgramElement* pe = s->getElements().at(i);
            if(Scope* anotherS = llvm::dyn_cast<Scope>(pe)){
                SourceCodeLocation* scl = getLocation(anotherS, lineNo);
                if(scl){
                    return scl;
                }
            }
        }
    }
    
    return NULL;
}

set<string> getVariablesOfInterest(map<SourceCodeLocation, set<DataFlowInfo> > presentDirectives, set<DataFlowInfo>& dataFlowInfo, SourceCodeLocation loc){
    map<string,int> variableCount;
    
    for(auto const& p : presentDirectives){
        if(loc.contains(p.first)){
            for(set<DataFlowInfo>::iterator it = p.second.begin(); it!= p.second.end();++it){
                DataFlowInfo presentDfi = (*it);
                for(set<DataFlowInfo>::iterator it2 = dataFlowInfo.begin(); it2 != dataFlowInfo.end(); ++it2){
                    DataFlowInfo dfi = (*it2);
                    if(dfi.variable == presentDfi.variable){
                        if(!variableCount.count(presentDfi.variable)){
                            variableCount[presentDfi.variable]=0;
                        }
                        variableCount[presentDfi.variable]++;
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

void getUpdateStatements(Scope* s, const map<SourceCodeLocation, set<DataFlowInfo> > presentDirectives, const set<string>& varsOfInterest, set<UpdateStatement>& updateData, set<string>& changedInHost, set<string>& changedInDevice, map<string, unsigned int>& lastSeenInHost){
    vector<ProgramElement*> programElements = s->getElements();
    for(unsigned long i=0; i < programElements.size(); i++){
        ProgramElement* pe = programElements.at(i);
        
        bool inParRegion = false;
        for(auto const& p: presentDirectives){
            if(p.first.contains(pe->getSourceCodeLocation())){
                inParRegion = true;
                break;
            }
        }
        
        Scope* tempS = NULL;
        if((tempS = llvm::dyn_cast<Scope>(pe))){
            getUpdateStatements(tempS, presentDirectives, varsOfInterest, updateData, changedInHost, changedInDevice, lastSeenInHost);
        } else if(VariableEvent* ve = llvm::dyn_cast<VariableEvent>(pe)){
            string curVar = ve->getVariable();
            SourceCodeLocation scl = ve->getSourceCodeLocation();
            if(varsOfInterest.count(curVar)){
                VariableDeclared* vd = NULL;
                VariableRead* vr = NULL;
                VariableAssigned* va = NULL;
                VariableUsedInFunction* vf = NULL;
                if((vd = llvm::dyn_cast<VariableDeclared>(ve))){
                    //Do nothing®
                } else if((vr = llvm::dyn_cast<VariableRead>(ve))){
                    if(!inParRegion && changedInDevice.count(curVar)){
                        UpdateStatement updateStatement ={scl.startLineNo, curVar, HOST};
                        updateData.insert(updateStatement);
                        changedInDevice.erase(curVar);
                    } else if(inParRegion && changedInHost.count(curVar)){
                        UpdateStatement updateStatement = {lastSeenInHost[curVar]+1, curVar, DEVICE};
                        updateData.insert(updateStatement);
                        changedInHost.erase(curVar);
                    }
                } else if((va = llvm::dyn_cast<VariableAssigned>(ve))){
                    if(!inParRegion){
                        changedInHost.insert(curVar);
                        lastSeenInHost[curVar]=scl.startLineNo;
                        changedInDevice.erase(curVar);
                    } else {
                        changedInDevice.insert(curVar);
                        changedInHost.erase(curVar);
                    }
                } else if((vf = llvm::dyn_cast<VariableUsedInFunction>(ve))){
                    if(inParRegion && changedInHost.count(curVar)){
                        UpdateStatement updateStatement = {lastSeenInHost[curVar]+1, curVar, DEVICE};
                        updateData.insert(updateStatement);
                        changedInHost.erase(curVar);
                    } else if(!inParRegion && changedInDevice.count(curVar)){
                        UpdateStatement updateStatement = {scl.startLineNo, curVar, HOST};
                    }
                    
                    if(inParRegion){
                        changedInDevice.insert(curVar);
                    } else {
                        changedInHost.insert(curVar);
                        lastSeenInHost[curVar] = scl.startLineNo;
                    }
                } else {
                    cerr << "Error: Program element '" << pe->varID << "' is not supported." << endl;
                    exit(EXIT_FAILURE);
                }
            }
        } else {
            cerr << "Error: Program element '" << pe->varID << "' is not supported." << endl;
            exit(EXIT_FAILURE);
        }
    }
}

void getUpdateStatements(Scope* s, const map<SourceCodeLocation, set<DataFlowInfo> > presentDirectives, const set<string>& varsOfInterest, set<UpdateStatement>& updateData){
   set<string> changedInHost;
   set<string> changedInDevice;
   map<string, unsigned int> lastSeenInHost;
   getUpdateStatements(s, presentDirectives, varsOfInterest, updateData, changedInHost, changedInDevice, lastSeenInHost);
}

int getDataFlow(Scope* scope, unsigned int lineNo, unsigned int& endScope, set<DataFlowInfo>& data){
    
    if(!scope){
        return SCOPE_ANALYSIS_SCOPE_INVALID;
    }
    
    map<string, VariableData> varData;
    SourceCodeLocation* p = getLocation(scope, lineNo);
    
    if(p){
        endScope = p->endLineNo;
        processScope(scope, (*p), varData);
    } else {
        return SCOPE_ANALYSIS_INVALID_LINE_NO;
    }
    
    
    map<string, VariableData>::iterator it;
    for(it = varData.begin(); it != varData.end(); ++it){
        string variable = it->first;
        VariableData vd = it->second;
        Flow toSet;
        
        // This was just off the top of my head, significant testing is needed to ensure this covers call cases
        if(vd.isReadWithin || vd.isWrittenWithin){
            if((!vd.isWrittenBefore || !vd.isReadWithin) && (!vd.isWrittenWithin || !vd.isReadAfter)){
                toSet = CREATE;
            } else if(!vd.isReadAfter || !vd.isWrittenWithin){
                toSet = COPY_IN;
            } else if((!vd.isWrittenBefore || (!vd.isReadWithin && vd.isWrittenWithin)) && vd.isReadAfter){
                /*
                 Arrays and pointers are troublesome.
                 A copyout may destroy data ranges
                 as we do not use range analysis.
                 */
                if(vd.isArray || vd.isPointer){
                    toSet = COPY;
                } else {
                    toSet = COPY_OUT;
                }
            } else {
                toSet = COPY;
            }
            
            DataFlowInfo toAdd = {variable, toSet, "", ""};
            data.insert(toAdd);
        }
    }
    return SCOPE_ANALYSIS_SUCCESS;
}


int getDataDirectiveDataFlow(Scope* s, unsigned int lineNo, unsigned int endScope, const map<SourceCodeLocation, set<DataFlowInfo> > presentDirectives, set<DataFlowInfo>& flowData, set<UpdateStatement>& updateData){
    
    SourceCodeLocation newScopeLoc ={lineNo, 0, endScope, 0};
    if(!s->addScope(newScopeLoc)){
        return SCOPE_ANALYSIS_SCOPE_INVALID;
    }
    
    set<DataFlowInfo> output;
    int errCode;
    if((errCode = getDataFlow(s, lineNo, endScope, output))){
        return errCode;
    }
    
    map<SourceCodeLocation, set<DataFlowInfo> > presentDirectivesOfInterest; //Only gets the present directives within the current scope
    for(auto const& p: presentDirectives){
        if(newScopeLoc.contains(p.first)){
            presentDirectivesOfInterest[p.first] = p.second;
        }
    }
    
   // for(auto& dfi : output){
    set<DataFlowInfo> newOutput;
    for(set<DataFlowInfo>::iterator it = output.begin(); it != output.end(); ++it){
        string startRange = "";
        string endRange = "";
        bool valid = true;
        for(auto const& pdoi: presentDirectives){
            if(valid == false){
                break;
            }
            for(auto const& dfiRef : pdoi.second){
                if(dfiRef.variable == (*it).variable){
                    if(dfiRef.startRange == ""){
                        startRange = "";
                        endRange = "";
                        valid = false;
                        break;
                    } else if(startRange == "" && dfiRef.startRange != ""){
                        startRange = dfiRef.startRange;
                        endRange = dfiRef.endRange;
                    } else {
                        //TODO: There should be something for when a range isn't an integer, but an expression. Not needed right now but may be needed in the future.
                        if(stoi(startRange) > stoi(dfiRef.startRange)){
                            startRange = dfiRef.startRange;
                        }
                        
                        if(stoi(endRange) < stoi(dfiRef.endRange)){
                            endRange = dfiRef.endRange;
                        }
                    }
                }
            }
        }
        DataFlowInfo newDfi = {it->variable, it->flow, startRange, endRange};
        newOutput.insert(newDfi);
     //   it->endRange = "";
     //   temp.endRange = endRange;
      //  dfi.endRange = endRange;
       // dfi.startRange = startRange;
    }
    
    output = newOutput;
    
    map<SourceCodeLocation, set<DataFlowInfo> > reducedPresentDirectives; //Removes present directives contained within other present directives
    for(auto const& p : presentDirectivesOfInterest){
        bool toAdd = true;
        for(auto const& p2 : presentDirectivesOfInterest){
            if(p.first != p2.first && p2.first.contains(p.first)){
                toAdd = false;
                break;
            }
        }
        if(toAdd){
            reducedPresentDirectives[p.first] = p.second;
        }
    }
    
    set<string> varsOfInterest = getVariablesOfInterest(reducedPresentDirectives, output, newScopeLoc);
    
    //TODO: right now we just treat a data directive as if it were a FOR loop, i.e. analyse the data flow of the variable in and out of the region. We then remove any directives not associated with any inner compute regions (as determined by 'presentDirectives'). This is somewhat inefficient and the program would be improved by using the inner compute regions, as well as other information, to determine what data regions should be.
    //Print out the data directive info
    for(set<DataFlowInfo>::iterator it = output.begin(); it!=output.end(); ++it){
        DataFlowInfo dfi = (*it);
        if(varsOfInterest.count(dfi.variable)){
            flowData.insert(dfi);
        }
    }
    
    Scope* dataDir;
    if((dataDir = llvm::dyn_cast<Scope>(s->getRoot()->getElementAt(newScopeLoc)))){
        getUpdateStatements(dataDir, reducedPresentDirectives, varsOfInterest, updateData);
    } else {
        cerr << "Cannot find added scope" << endl;
        exit(EXIT_FAILURE);
    }
    
    if(!s->removeScope(newScopeLoc)){
        cerr << "ERROR: Could not remove previously added scope" << endl;
        exit(EXIT_FAILURE);
    }
    
    return SCOPE_ANALYSIS_SUCCESS;
}

string toString(Flow f){
    switch(f){
        case COPY:
            return "COPY";
        case COPY_IN:
            return "COPY_IN";
        case COPY_OUT:
            return "COPY_OUT";
        case PRESENT:
            return "PRESENT";
        case CREATE:
            return "CREATE";
        default:
            cerr << "ERROR: Attempting to translate unrecognised flow to string [ScopeAnalysis::toString(Flow f)]" << endl;
            exit(EXIT_FAILURE);
    }
}

string toString(Update u){
    switch(u){
        case HOST:
            return "UPDATE_HOST";
        case DEVICE:
            return "UPDATE_DEVICE";
        default:
            cerr << "ERROR: Attempting to print translate unrecognised flow to string [ScopeAnalysis::toString(Flow f)]" << endl;
    }
}
