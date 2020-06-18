//
//  Scope.h
//
//
//  The Scope class is a doubly-linked list containing information regarding a Scope and its contents
//  The contents can either be another Scope or a 'VariableEvent', a read, write, declaration, or usage
//  in a function
//

#ifndef Scope_h
#define Scope_h

#include <string>
#include <vector>

#define DECLARED    "declared"
#define READ        "read"
#define ASSIGNED    "assigned"
#define FUNC        "function"
#define SCOPE       "scope"

using namespace std;

struct SourceCodeLocation{
    unsigned int startLineNo;
    unsigned int startColNo;
    unsigned int endLineNo;
    unsigned int endColNo;
    
    bool operator == (const SourceCodeLocation& rhs) const{
        return (this->startLineNo == rhs.startLineNo
                && this->endLineNo == rhs.endLineNo
                && this->startColNo == rhs.startColNo
                && this->endColNo == rhs.endColNo);
    }
    
    bool operator != (const SourceCodeLocation& rhs) const{
        const SourceCodeLocation thisSourceCodeLocation = (*this);
        return !(thisSourceCodeLocation == rhs);
    }
    
    bool operator < (const SourceCodeLocation& rhs) const {
        return this->endLineNo < rhs.startLineNo
                || (this->endLineNo == rhs.startLineNo && this->endColNo < rhs.startColNo);
    }
    
    bool operator > (const SourceCodeLocation& rhs) const {
        return this->startLineNo > rhs.endLineNo
        || (this->startLineNo == rhs.endLineNo && this->startColNo > rhs.endColNo);
    }
    
    //Is a SourceCodeLocation contained within this?
    bool contains(const SourceCodeLocation& test) const {
        return (this->startLineNo < test.startLineNo
            || (this->startLineNo == test.startLineNo && this->startColNo <= test.startColNo))
        && ((this->endLineNo > test.endLineNo)
            || (this->endLineNo == test.endLineNo && this->endColNo >= test.endColNo))
        && (*this) != test;
    }
    
    //Would it be feasible to insert this SourceCodeLocation to this scope?
    bool feasibleInsert(const SourceCodeLocation& test) const{
        const SourceCodeLocation thisSourceCodeLocation = (*this);
        return test!=thisSourceCodeLocation && (thisSourceCodeLocation < test || thisSourceCodeLocation > test || thisSourceCodeLocation.contains(test) || test.contains(thisSourceCodeLocation));
    }
};

class ProgramElement {
public:
    ProgramElement(string var, SourceCodeLocation loc): varID(var), codeLoc(loc){}
    const string varID;
    const SourceCodeLocation getSourceCodeLocation();
private:
    const SourceCodeLocation codeLoc;
};

class VariableEvent : public ProgramElement{
public:
    VariableEvent(string variable, SourceCodeLocation loc, string varID): ProgramElement(varID, loc), var(variable){}
    string getVariable();
    static inline bool classof(const VariableEvent* v ){ return (v->varID == DECLARED || v->varID == READ || v->varID == ASSIGNED || v->varID == FUNC); }
    static inline bool classof(const ProgramElement* p){ return (p->varID == DECLARED || p->varID == READ || p->varID == ASSIGNED || p->varID == FUNC);}
protected:
    string var;
};

class VariableDeclared : public VariableEvent {
public:
    VariableDeclared(string v, SourceCodeLocation loc,  bool anExtern, bool aPointer, bool aReference, bool aStatic, bool anArray, bool aFunctionParameter): VariableEvent(v, loc, DECLARED), isAnExternal(anExtern), isAReference(aReference), isAFunctionParameter(aFunctionParameter), isAnArray(anArray), isAPointer(aPointer), isAStatic(aStatic) {}
    bool isExtern();
    bool isPointer();
    bool isReference();
    bool isFunctionParameter();
    bool isStatic();
    bool isArray();
    static inline bool classof(VariableDeclared const*){return true;}
    static inline bool classof(const VariableEvent* v ){ return (v->varID == DECLARED); }
    static inline bool classof(const ProgramElement* p){ return (p->varID == DECLARED);}
private:
    bool isAnExternal;
    bool isAStatic;
    bool isAPointer;
    bool isAReference;
    bool isAFunctionParameter;
    bool isAnArray;
};

class VariableRead : public VariableEvent {
public:
    VariableRead(string v, SourceCodeLocation loc): VariableEvent(v, loc, READ) {}
    static inline bool classof(const VariableRead* r){ return true; }
    static inline bool classof(const VariableEvent* v ){ return (v->varID == READ); }
    static inline bool classof(const ProgramElement* p){ return (p->varID == READ);}
};

class VariableAssigned : public VariableEvent {
public:
    VariableAssigned(string v, SourceCodeLocation loc): VariableEvent(v,loc, ASSIGNED) {}
    static inline bool classof(const VariableAssigned* v){ return true;}
    static inline bool classof(const VariableEvent* v){ return (v->varID == ASSIGNED);}
    static inline bool classof(const ProgramElement* p){ return (p->varID == ASSIGNED);}
};

class VariableUsedInFunction : public VariableEvent {
public:
    VariableUsedInFunction(string v, SourceCodeLocation loc): VariableEvent(v, loc, FUNC) {}
    static inline bool classof(const VariableUsedInFunction* v){ return true;}
    static inline bool classof(const VariableEvent* v){ return (v->varID == FUNC);}
    static inline bool classof(const ProgramElement* p){ return (p->varID == FUNC);}
};

class Scope : public ProgramElement{
public:
    Scope(SourceCodeLocation loc) : ProgramElement(SCOPE, loc){}
    ~Scope(){
        /* There is an assumption the children will be
         * dynamically allocated, any users of this ---
         *  ensure this is the case!
         */
        for(auto it=elements.begin(); it != elements.end(); ++it){
            delete (*it);
        }
    }
    
    Scope* getParent();
    const vector<ProgramElement*> getElements();
    void addElement(ProgramElement* element);
    void addElements(vector<ProgramElement*> elements);
    Scope* getRoot();
    bool validInsertion(SourceCodeLocation scopeInsert);
    bool addScope(SourceCodeLocation newScopeLoc);
    bool removeScope(SourceCodeLocation newScopeLoc);
    ProgramElement* getElementAt(SourceCodeLocation scl);
    ProgramElement* getElementAtLine(unsigned int lineNo);
    static inline bool classof(const Scope* s){ return true;}
    static inline bool classof(const ProgramElement* p){ return (p->varID == SCOPE);}
    std::string toString(); //This is quite good for testing and debugging
private:
    Scope* parent = NULL;
    vector<ProgramElement*> elements;
    void clearElements();
    std::string toString(unsigned int indentation);
};

#endif /* Scope_h */
