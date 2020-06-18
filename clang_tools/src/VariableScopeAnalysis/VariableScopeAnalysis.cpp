// TODO: pointers are troublesome. I, for the meantime, assume they
// read from and written to as in some cases it's difficult to tell
// though static analysis. E.g. in the case '*(ptr + b) = 10'
// TODO: I use 'isUsedInFunction', i do not delve into the function
// to see what's going on. In future I could change this

#include <clang/VariableScopeAnalysis/VariableScopeAnalysis.h>
#include <clang/Frontend/CompilerInstance.h>
#include <clang/Tooling/Tooling.h>
#include <iostream>
#include <sstream>
#include <vector>
#include <set>

using namespace std;

Scope* VariableScopeAnalysis::scope;

bool VariableScopeAnalysis::VariableScopeAnalysisVisitor::isInMain(SourceLocation loc){
    return Context->getSourceManager().isInMainFile((Context->getSourceManager().getSpellingLoc(loc)));
}

//VariableScopeAnalysis

Scope* VariableScopeAnalysis::run(string& code, LANGUAGE lang, set<string>& includes){
    unsigned int noLines=0;
    string temp;
    istringstream f(code);
    while(getline(f, temp)){
        noLines++;
    }
    
    SourceCodeLocation loc = {0,0, noLines+1, 0}; // I.e. the whole file
    
    scope = new Scope(loc);
    
    vector<string> arguments;
    arguments.push_back("-w"); //Supress warnings
    switch(lang){
        case C:
            arguments.push_back("-xc");
            break;
        case CPP:
            arguments.push_back("-xc++");
            break;
        default:
            cerr << "Invalid LANGUAGE (VariableScopeAnalysis::run)" << endl;
            exit(EXIT_FAILURE);
            break;
    }
    
    for(set<string>::iterator it = includes.begin(); it != includes.end(); ++it){
        arguments.push_back("-I" + (*it));
    }
    
    if(!clang::tooling::runToolOnCodeWithArgs(new VariableScopeAnalysisAction, code.c_str(), arguments)){
        delete scope->getRoot();
        return NULL;
    }
        
    return scope->getRoot();
}

Scope* VariableScopeAnalysis::run(string& code, LANGUAGE lang, set<string>& includes, set<SourceCodeLocation>& scopesToAdd){
    Scope* toReturn = run(code, lang, includes);
    for(set<SourceCodeLocation>::iterator it = scopesToAdd.begin(); it != scopesToAdd.end(); ++it){
        SourceCodeLocation scl = (*it);
        if(scl.startLineNo != scl.endLineNo){
            toReturn->addScope(scl);
        }
    }
    return toReturn;
}

//VariableScopeAnalysisVisitor

bool VariableScopeAnalysis::VariableScopeAnalysisVisitor::VisitStmt(Stmt* s){
    if(isInMain(s->getLocStart())){
        if(CompoundStmt* c = dyn_cast<CompoundStmt>(s)){
            FullSourceLoc leftBrac = Context->getFullLoc(c->getLocStart());
            FullSourceLoc rightBrac = Context->getFullLoc(c->getLocEnd());
            createScope(leftBrac, rightBrac);
        } else if(ForStmt* f = dyn_cast<ForStmt>(s)){
            FullSourceLoc leftBrac = Context->getFullLoc(f->getLParenLoc());
            FullSourceLoc rightBrac = Context->getFullLoc(f->getLocEnd());
            createScope(leftBrac, rightBrac);
        } else if(IfStmt* i = dyn_cast<IfStmt>(s)){
            FullSourceLoc leftBrac = Context->getFullLoc(i->getLocStart());
            FullSourceLoc rightBrac = Context->getFullLoc(i->getLocEnd());
            createScope(leftBrac, rightBrac);
        } else if(WhileStmt* w = dyn_cast<WhileStmt>(s)){
            FullSourceLoc leftBrac = Context->getFullLoc(w->getLocStart());
            FullSourceLoc rightBrac = Context->getFullLoc(w->getLocEnd());
            createScope(leftBrac, rightBrac);
        } else if(IfStmt* f = dyn_cast<IfStmt>(s)){
            FullSourceLoc leftBrac = Context->getFullLoc(f->getLocStart());
            FullSourceLoc rightBrac = Context->getFullLoc(f->getLocEnd());
            createScope(leftBrac, rightBrac);
        }
    }
    
    return true;
}


bool VariableScopeAnalysis::VariableScopeAnalysisVisitor::VisitFunctionDecl(FunctionDecl* f){
    if(isInMain(f->getLocStart())){
        FullSourceLoc leftBrac = Context->getFullLoc(f->getLocStart());
        FullSourceLoc rightBrac = Context->getFullLoc(f->getLocEnd());
        createScope(leftBrac, rightBrac);
    }
    return true;
}

void VariableScopeAnalysis::VariableScopeAnalysisVisitor::createScope(FullSourceLoc leftBrac, FullSourceLoc rightBrac){
    navigateToCorrectScope(leftBrac);
    SourceCodeLocation loc = {leftBrac.getSpellingLineNumber(), leftBrac.getSpellingColumnNumber(),rightBrac.getSpellingLineNumber(), rightBrac.getSpellingColumnNumber()};
    Scope* temp = new Scope(loc);
    scope->addElement(temp);
    scope = temp;
}

// Checks to see if any parents is a Call Expr
bool VariableScopeAnalysis::VariableScopeAnalysisVisitor::isInCallExpr(const Expr* d){
    const auto& parents = Context->getParents(*d);
    if(parents.empty() || !parents[0].get<Expr>()){
        return false;
    }

    if(isa<CallExpr>(parents[0].get<Expr>())){
        return true;
    }
    
    return isInCallExpr(parents[0].get<Expr>());
}

//Skips any parentheses to see if the expression is the LValue in a CompoundAssignOperator
CompoundAssignOperator* VariableScopeAnalysis::VariableScopeAnalysisVisitor::isInCompoundAssignOperator(const Expr* d){
    const auto& parents = Context->getParents(*d);
    
    if(parents.empty() || !parents[0].get<Expr>()){
        return NULL;
    }
    
    if(isa<CompoundAssignOperator>(parents[0].get<Expr>())){
        return ((CompoundAssignOperator*)parents[0].get<Expr>());
    }
    
    if(isa<ParenExpr>(parents[0].get<Expr>())){
        return isInCompoundAssignOperator(parents[0].get<Expr>());
    }
    
    return NULL;
}

//Skips any parentheses to see if the expression is in an ImplcitCastExpr of an certain kind


ImplicitCastExpr* VariableScopeAnalysis::VariableScopeAnalysisVisitor::isInImplicitCastExpr(const Expr* d){
    const auto& parents = Context->getParents(*d);
    
    if(parents.empty() || !parents[0].get<Expr>()){
        return NULL;
    }
    
    if(isa<ImplicitCastExpr>(parents[0].get<Expr>())){
        return ((ImplicitCastExpr*)parents[0].get<Expr>());
    }
    
    if(isa<ParenExpr>(parents[0].get<Expr>())){
        return isInImplicitCastExpr(parents[0].get<Expr>());
    }
    
    return NULL;
}

//Skips any parenthesis to see if the expression is in an ArraySubscriptExpr
ArraySubscriptExpr* VariableScopeAnalysis::VariableScopeAnalysisVisitor::isInArraySubscriptExpr(const Expr* d){
    const auto& parents = Context->getParents(*d);
    
    if(parents.empty() || !parents[0].get<Expr>()){
        return NULL;
    }
    
    if(isa<ArraySubscriptExpr>(parents[0].get<Expr>())){
        return ((ArraySubscriptExpr*)parents[0].get<Expr>());
    }
    
    if(isa<ParenExpr>(parents[0].get<Expr>())){
        return isInArraySubscriptExpr(parents[0].get<Expr>());
    }
    
    return NULL;
}

//Skips any parenthesis to see if the expression is in an UnaryOperator
UnaryOperator* VariableScopeAnalysis::VariableScopeAnalysisVisitor::isInUnaryOperator(const Expr* d){
    const auto& parents = Context->getParents(*d);
    
    if(parents.empty() || !parents[0].get<Expr>()){
        return NULL;
    }
    
    if(isa<UnaryOperator>(parents[0].get<Expr>())){
        return ((UnaryOperator*)parents[0].get<Expr>());
    }
    
    if(isa<ParenExpr>(parents[0].get<Expr>())){
        return isInUnaryOperator(parents[0].get<Expr>());
    }
    
    return NULL;
}

bool previouslyBeenDeclared(string variable, Scope* s){
    for(unsigned long i=0; i<s->getElements().size(); i++){
        if(Scope* tempS = dyn_cast<Scope>(s->getElements().at(i))){
            if(previouslyBeenDeclared(variable, tempS)){
                return true;
            }
        }else if(VariableDeclared* vd = dyn_cast<VariableDeclared>(s->getElements().at(i))){
            if(vd->getVariable() == variable){
                return true;
            }
        }
    }
    return false;
}

bool VariableScopeAnalysis::VariableScopeAnalysisVisitor::TraverseDeclRefExpr(DeclRefExpr* d){
    if(!d->getType()->isFunctionType()){ //If it's a function declation, then skip. We don't care about those
        FullSourceLoc startLoc = Context->getFullLoc(d->getLocStart());
        FullSourceLoc endLoc = Context->getFullLoc(d->getLocEnd());
        navigateToCorrectScope(startLoc);
        string varName = d->getNameInfo().getAsString();
        
        /*
         In headers, I'm only concerned with recording global variables.
         In 'TraverseVarDecl' I already check to ensure this is the case.
         If a variable is a non-global variable in the header it is not
         recorded as a declaration. For 'TraverseDeclRefExpr, I have
         no means to directly check if the Variable in question is
         global or otherwise. I therefore have a check to see if the
         Variable has already been recorded as declared. If not, it
         is ignored It's a nasty little hack but it works.
         */
        if(previouslyBeenDeclared(varName, scope->getRoot())){
            SourceCodeLocation sourceCodeLocation = {startLoc.getSpellingLineNumber(), startLoc.getSpellingColumnNumber(), endLoc.getSpellingLineNumber(), endLoc.getSpellingColumnNumber()};
            if(isInCallExpr(d)){ // If it's in a callExpr (i.e. a function call)
                //Our default for now. We do not make any assuptions about how the variable is used within the function. It is a black box to us. We may improve this later by seeing how the function uses the variable.
                scope->addElement(new VariableUsedInFunction(varName, sourceCodeLocation));
            } else if(d->isLValue()){ // Else if the value is L Value...
                //const auto& parents = Context->getParents(*d);
                
                ImplicitCastExpr* ice = NULL;
                ArraySubscriptExpr* ase = NULL;
                Expr* tempD = d;
                while((ice = isInImplicitCastExpr(tempD))){
                    if((ase = isInArraySubscriptExpr(ice))){
                        clang::StmtIterator it = ase->child_begin();
                        if((*it) != ice){
                            scope->addElement(new VariableRead(varName, sourceCodeLocation));
                            return true;
                        }
                        tempD = ase;
                    } else if(isInCompoundAssignOperator(tempD) || isInUnaryOperator(tempD)){
                        scope->addElement(new VariableRead(varName, sourceCodeLocation));
                        scope->addElement(new VariableAssigned(varName, sourceCodeLocation));
                        return true;
                    } else {
                        scope->addElement(new VariableRead(varName, sourceCodeLocation));
                        return true;
                    }
                }
                
                if(ice && ase){
                    scope->addElement(new VariableAssigned(varName, sourceCodeLocation));
                } else if(ice && ice->getCastKind() == CastKind::CK_LValueToRValue){
                    scope->addElement(new VariableRead(varName, sourceCodeLocation));
                }else if(isInCompoundAssignOperator(d) || isInUnaryOperator(d) || (ase && isInCompoundAssignOperator(ase))){
                    scope->addElement(new VariableRead(varName, sourceCodeLocation));
                    scope->addElement(new VariableAssigned(varName, sourceCodeLocation));
                } else {
                    scope->addElement(new VariableAssigned(varName, sourceCodeLocation));
                }
                
            } else if(d->isRValue()){
                scope->addElement(new VariableAssigned(varName, sourceCodeLocation));
            }
        }
    }
    return true;
}

bool VariableScopeAnalysis::VariableScopeAnalysisVisitor::TraverseParmVarDecl(ParmVarDecl* v){
    FullSourceLoc startLoc = Context->getFullLoc(v->getLocStart());
    FullSourceLoc endLoc = Context->getFullLoc(v->getLocEnd());
    navigateToCorrectScope(startLoc);
    string tempS = v->getNameAsString();
    SourceCodeLocation sourceCodeLocation = {startLoc.getSpellingLineNumber(), startLoc.getSpellingColumnNumber(), endLoc.getSpellingLineNumber(), endLoc.getSpellingColumnNumber()};
    
    
    scope->addElement(new VariableDeclared(tempS, sourceCodeLocation, v->isExternallyVisible(), v->getType()->isPointerType(), v->getType()->isReferenceType(), v->getStorageClass() == clang::StorageClass::SC_Static, v->getType()->isArrayType(), true));
    return true;
}

bool VariableScopeAnalysis::VariableScopeAnalysisVisitor::VisitVarDecl(VarDecl *v){ // Get the variable declarations
    FullSourceLoc startLoc = Context->getFullLoc(v->getLocStart());
    FullSourceLoc endLoc = Context->getFullLoc(v->getLocEnd());
    navigateToCorrectScope(startLoc);
    string tempS = v->getNameAsString();
    SourceCodeLocation sourceCodeLocation = {startLoc.getSpellingLineNumber(), startLoc.getSpellingColumnNumber(), endLoc.getSpellingLineNumber(), endLoc.getSpellingColumnNumber()};
    
    //If not in main (i.e. it's in a header of some sort, I'm only interested if it's externally visible (i.e. a global variable)
    if(isInMain(v->getLocation()) || (!isInMain(v->getLocation()) && v->isExternallyVisible())){
        scope->addElement(new VariableDeclared(tempS, sourceCodeLocation, v->isExternallyVisible(), v->getType()->isPointerType(), v->getType()->isReferenceType(), v->getStorageClass() == clang::StorageClass::SC_Static , v->getType()->isArrayType(), false));
        if(v->hasInit()){
            scope->addElement(new VariableAssigned(tempS, sourceCodeLocation));
        }
    }
    return true;
}

void VariableScopeAnalysis::VariableScopeAnalysisVisitor::navigateToCorrectScope(FullSourceLoc loc){
    //If the FullSourceLoc is in a header (i.e. not in the mail file), we simply go to the parent scope
    if(!isInMain(loc)){
        scope = scope->getRoot();
    } else {
        // If the current line is after the current scope, then recursevely go to the parent
        if(scope->getSourceCodeLocation().endLineNo < loc.getSpellingLineNumber()
           || (scope->getSourceCodeLocation().endLineNo == loc.getSpellingLineNumber()
               && scope->getSourceCodeLocation().endColNo < loc.getSpellingColumnNumber())){
            if(!scope->getParent()){
                cerr << "ERROR: Trying to navigate to parent scope that does not exist" << endl;
                exit(EXIT_FAILURE);
            }
            scope = scope->getParent();
            navigateToCorrectScope(loc);
        // If within the current scope
        } else if((scope->getSourceCodeLocation().startLineNo < loc.getSpellingLineNumber()
                   || (scope->getSourceCodeLocation().startLineNo  == loc.getSpellingLineNumber()
                       && scope->getSourceCodeLocation().startColNo  < loc.getSpellingColumnNumber()))
                  && (scope->getSourceCodeLocation().endLineNo > loc.getSpellingLineNumber()
                      || (scope->getSourceCodeLocation().endLineNo == loc.getSpellingLineNumber()
                          && scope->getSourceCodeLocation().endColNo > loc.getSpellingColumnNumber()))){
            for(unsigned long i=0; i < scope->getElements().size(); i++){
                ProgramElement* temp = scope->getElements().at(i);
                if(Scope* s = dyn_cast<Scope>(temp)){
                    //Ensure not within the child scopes
                    if((s->getSourceCodeLocation().startLineNo < loc.getSpellingLineNumber()
                        || (s->getSourceCodeLocation().startLineNo == loc.getSpellingLineNumber()
                            && s->getSourceCodeLocation().startColNo < loc.getSpellingColumnNumber()))
                       && (s->getSourceCodeLocation().endLineNo > loc.getSpellingLineNumber()
                           || (s->getSourceCodeLocation().endLineNo == loc.getSpellingLineNumber()
                               && s->getSourceCodeLocation().endColNo > loc.getSpellingColumnNumber()))){
                        scope = s;
                        navigateToCorrectScope(loc);
                        break;
                    }
                }
            }
            //if before the current scope
        } else if(scope->getSourceCodeLocation().startLineNo > loc.getSpellingLineNumber()
                  || (scope->getSourceCodeLocation().startLineNo == loc.getSpellingLineNumber()
                      && scope->getSourceCodeLocation().startColNo > loc.getSpellingColumnNumber())){
            scope = scope->getParent();
            navigateToCorrectScope(loc);
        }
    }
}

//VariableScopeAnalysisConsumer

void VariableScopeAnalysis::VariableScopeAnalysisConsumer::HandleTranslationUnit(clang::ASTContext &Context) {
    Visitor.TraverseDecl(Context.getTranslationUnitDecl());
}

//VariableScopeAnalysisAction

std::unique_ptr<clang::ASTConsumer> VariableScopeAnalysis::VariableScopeAnalysisAction::CreateASTConsumer(clang::CompilerInstance &Compiler, llvm::StringRef InFile) {
    return std::unique_ptr<clang::ASTConsumer>(new VariableScopeAnalysisConsumer(&Compiler.getASTContext()));
}

