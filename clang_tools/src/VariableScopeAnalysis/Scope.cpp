#include <clang/VariableScopeAnalysis/Scope.h>
#include <llvm/Support/Casting.h>
#include <iostream>


const SourceCodeLocation ProgramElement::getSourceCodeLocation(){
    return codeLoc;
}

string VariableEvent::getVariable() {
    return var;
}

bool VariableDeclared::isExtern(){
    return isAnExternal;
        
}
    
bool VariableDeclared::isPointer(){
    return isAPointer;
}
    
bool VariableDeclared::isReference(){
    return isAReference;
}
    
bool VariableDeclared::isFunctionParameter(){
    return isAFunctionParameter;
}

bool VariableDeclared::isStatic(){
    return isAStatic;
}

bool VariableDeclared::isArray(){
    return isAnArray;
}

Scope* Scope::getParent() {
    return parent;
}

const vector<ProgramElement*> Scope::getElements() {
    return elements;
}

void Scope::addElement(ProgramElement* element) {
    if(Scope* s = llvm::dyn_cast<Scope>(element)){
        s->parent = this;
    }
    elements.push_back(element);
}

Scope* Scope::getRoot() {
    if(this->getParent()){
        return this->getParent()->getRoot();
    }
    return this;
}

//TODO: This has not been tested
bool Scope::validInsertion(SourceCodeLocation scopeInsert){
    if(!this->getSourceCodeLocation().feasibleInsert(scopeInsert)){
        return false;
    }
    for(unsigned long i=0; i < this->getElements().size(); i++){
        if(Scope* s = llvm::dyn_cast<Scope>((this->getElements().at(i)))){
            if(!s->validInsertion(scopeInsert)){
                return false;
            }
        }
    }
    return true;
}

// Returns to the scope in which the new scope is to be inserted, or if scope already exists, the scope
// Returns null if no avlid insertion scope (i.e. insertion invalid)
//TODO: Need to test this fully
Scope* navigateToCorrectScope(Scope* scope, SourceCodeLocation newScopeLoc){
    
    if(scope->getSourceCodeLocation() == newScopeLoc){
        return scope;
    }
    
    if(!scope->getSourceCodeLocation().feasibleInsert(newScopeLoc)){
        return NULL;
    }
    
    if(scope->getSourceCodeLocation().contains(newScopeLoc)){
        for(unsigned long i=0; i < scope->getElements().size(); i++){
            ProgramElement* pe = scope->getElements().at(i);
            if(Scope* s = llvm::dyn_cast<Scope>(pe)){
               /* if((s->getSourceCodeLocation().startLineNo < newScopeLoc.startLineNo
                    || (s->getSourceCodeLocation().startLineNo == newScopeLoc.startLineNo && s->getSourceCodeLocation().startColNo <= newScopeLoc.startColNo))
                   && (s->getSourceCodeLocation().endLineNo > newScopeLoc.startLineNo
                    || (s->getSourceCodeLocation().endLineNo == newScopeLoc.startLineNo && s->getSourceCodeLocation().endColNo >= newScopeLoc.startColNo))){*/
                if(s->getSourceCodeLocation() == newScopeLoc || s->getSourceCodeLocation().contains(newScopeLoc)){
                    return navigateToCorrectScope(s, newScopeLoc);
                }
            }
        }
    }
    return scope;
}
    
//    return NULL;
//}

void Scope::addElements(vector<ProgramElement*> elements){
    for(unsigned long i=0; i<elements.size(); i++){
        this->addElement(elements.at(i));
    }
}

void Scope::clearElements(){
    this->elements.clear();
}

//TODO: This is not well tested: please improve
bool Scope::addScope(SourceCodeLocation newScopeLoc){
    //Get correct scope
    Scope* scope = navigateToCorrectScope(this->getRoot(), newScopeLoc);
    
    //Scope =       0,0,24,0
    //newScopeLoc = 1,0,22,0
    //This is not a feasible insert but scope->getSourceCodeLocation().feasibleInsert(newScopeLoc) returns true
    
    if(!scope || (scope->getSourceCodeLocation() != newScopeLoc && !scope->validInsertion(newScopeLoc))){
        return false;
    }
    
    vector<ProgramElement*> replacement;
    vector<ProgramElement*> original = scope->getElements();
    bool scopeAdded = false;
    //Iterate through all the current program elements
    Scope* newScope = new Scope(newScopeLoc);
    
    for(unsigned long i = 0; i<original.size(); i++){
        ProgramElement* pe = original.at(i);
        if(newScope->getSourceCodeLocation().contains(pe->getSourceCodeLocation()) || newScope->getSourceCodeLocation() == pe->getSourceCodeLocation()){
            newScope->addElement(pe);
            if(!scopeAdded){
                replacement.push_back(newScope);
                scopeAdded = true;
            }
        } else {
            replacement.push_back(pe);
        }
    }
    
    scope->clearElements();
    scope->addElements(replacement);
    
    return true;
}

//This function will remove a scope and insert any children into the parent scope at the removed scope's former location
//If the scope to remove is the root then the scope is deleted
bool Scope::removeScope(SourceCodeLocation newScopeLoc){
    Scope* scope = navigateToCorrectScope(this->getRoot(), newScopeLoc);
    
    if(!scope|| scope->getSourceCodeLocation() != newScopeLoc){
        return false;
    }
    
    if(!scope->getParent()){
        if(scope){
            delete scope;
        }
        return true;
    }
    
    Scope* parentScope = scope->getParent();
    
    vector<ProgramElement*> parentScopeNewChildren;
    for(vector<ProgramElement*>::iterator it = parentScope->elements.begin(); it!= parentScope->elements.end(); ++it){
        if((*it) != scope){
            parentScopeNewChildren.push_back((*it));
        } else {
            for(vector<ProgramElement*>::iterator it2 = scope->elements.begin(); it2 != scope->elements.end(); ++it2){
                parentScopeNewChildren.push_back((*it2));
            }
        }
    }
    
    scope->clearElements();
    if(scope){
        delete scope;
    }
    
    parentScope->clearElements();
    parentScope->addElements(parentScopeNewChildren);
    
    return true;
    
    
}


//TODO: These of these are tested
ProgramElement* Scope::getElementAt(SourceCodeLocation scl){
    for(unsigned long i=0; i<this->getElements().size(); i++){
        if(this->getElements().at(i)->getSourceCodeLocation() == scl){
            return this->getElements().at(i);
        } else if(Scope* s = llvm::dyn_cast<Scope>(this->getElements().at(i))){
            ProgramElement* temp = s->getElementAt(scl);
            if(temp){
                return temp;
            }
        }
    }
    return NULL;
}

ProgramElement* Scope::getElementAtLine(unsigned int lineNo){
    for(unsigned long i=0; i<this->getElements().size(); i++){
        if(this->getElements().at(i)->getSourceCodeLocation().startLineNo == lineNo){
            return this->getElements().at(i);
        } else if(Scope* s = llvm::dyn_cast<Scope>(this->getElements().at(i))){
            ProgramElement* temp = s->getElementAtLine(lineNo);
            if(temp){
                return temp;
            }
        }
    }
    return NULL;
}

std::string Scope::toString(){
    return toString(0);
}

std::string Scope::toString(unsigned int indentation){
    std::string toReturn;
    toReturn += this->varID + " " + (!this->getParent() ? "[Root]" : "[Not Root]");
    toReturn += " (" + std::to_string(this->getSourceCodeLocation().startLineNo) + ":" + std::to_string(this->getSourceCodeLocation().startColNo) + "," + std::to_string(this->getSourceCodeLocation().endLineNo) + ":" + std::to_string(this->getSourceCodeLocation().endColNo) + ")" + "\n";
    
    for(unsigned long i = 0; i<this->getElements().size();i++){
        for(unsigned int j = 0; j < (indentation + 1); j++){
            toReturn += "---";
        }
        if(Scope* temp = llvm::dyn_cast<Scope>(this->getElements().at(i))){
            toReturn += temp->toString(indentation + 1);
        } else if(VariableEvent* temp = llvm::dyn_cast<VariableEvent>(this->getElements().at(i))){
            toReturn += temp->varID + " [" + temp->getVariable() + "]";
            toReturn += " (" + std::to_string(temp->getSourceCodeLocation().startLineNo) + ":" + std::to_string(temp->getSourceCodeLocation().startColNo) + "," + std::to_string(temp->getSourceCodeLocation().endLineNo) + ":" + std::to_string(temp->getSourceCodeLocation().endColNo) + ")" + "\n";
        } else {
            std::cerr << "Cannot output unknown Element" << std::endl;
            exit(EXIT_FAILURE);
        }
    }
    return toReturn;
}

