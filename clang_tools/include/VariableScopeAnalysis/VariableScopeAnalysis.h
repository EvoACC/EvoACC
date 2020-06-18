//
//  VariableScopeAnalysis
//
//  VariableScopeAnalysis creates a Scope objective for a particular C file by traversing the clang AST
//
//


#ifndef VariableScopeAnalysis_h
#define VariableScopeAnalysis_h

#include <clang/AST/ASTConsumer.h>
#include <clang/AST/ASTContext.h>
#include <clang/AST/RecursiveASTVisitor.h>
#include <clang/Frontend/FrontendAction.h>
#include <clang/VariableScopeAnalysis/Scope.h>
#include <set>

using namespace std;
using namespace clang;


/*
 * What 'run' returns should be self explanatory.
 * Each Scope contains 'ProgramElements' which are
 * variable read, assignments, declarations, uses
 * in function, or other scopes.
 *
 * There is, however, some unintuative use of this
 *  when it comes to function declarations, and for,
 * if, and while statements. Consider the following
 * code:
 *
 * for(int i=0; i<5; i++){
 *    int j=0
 * }
 *
 * This would result in The following
 *
 * Scope //The entire scope of the file
 *  `Scope //The 'for' statement
 *      `VariableDeclared //int i
 *      `VariableAssigned //i=0
 *      `VariableRead //i<5
 *      `VariableAssigned //i++
 *      `Scope //The Body of the for
 *          `VariableDeclared //int j
 *          `VariableAssigned //j=0
 *
 * Note how the for is a scope but also contains
 * its body as a scope.
 *
 * Also not that 'int i=0;' is treated as a
 * variable delcaration followed by assignment.
 */

enum LANGUAGE {C,CPP};

class VariableScopeAnalysis{
public:
    //Contract: This will return a dynamical allocated scope. Please remember delete it at the root, 'delete scope->getRoot();' is recommended
    static Scope* run(string& code, LANGUAGE lang, set<string>& includes);
    static Scope* run(string& code, LANGUAGE lang, set<string>& includes, set<SourceCodeLocation>& scopesToAdd);
private:

    /*I hate using statics in this way but I need these to be
     used (and altered in the case of scope) by the ASTVisitor.
     It is therefore a requirement that the scope is cleared upon
     each use. I have added 'isRunning' as I'm paranoid about
     anything modifying the varMap while 'run' is executed
     (won't happen, just paranoia)*/
    static Scope* scope;
    
    class VariableScopeAnalysisVisitor : public RecursiveASTVisitor<VariableScopeAnalysisVisitor> {
    public:
        explicit VariableScopeAnalysisVisitor(ASTContext *Context) : Context(Context) {}
        bool VisitStmt(Stmt* c);
        bool VisitVarDecl(VarDecl *v);
        bool TraverseDeclRefExpr(DeclRefExpr* d);
        bool VisitFunctionDecl(FunctionDecl* f);
        bool TraverseParmVarDecl(ParmVarDecl* p);
    private:
        void createScope(FullSourceLoc leftBrac, FullSourceLoc rightBrac);
        void navigateToCorrectScope(FullSourceLoc loc);
        bool isInMain(SourceLocation s);
        bool isInCallExpr(const Expr* d);
        CompoundAssignOperator* isInCompoundAssignOperator(const Expr* d);
        ImplicitCastExpr* isInImplicitCastExpr(const Expr* d);
        ArraySubscriptExpr* isInArraySubscriptExpr(const Expr* d);
        UnaryOperator* isInUnaryOperator(const Expr* d);
        ASTContext *Context;
    };
    
    class VariableScopeAnalysisConsumer : public clang::ASTConsumer {
        friend class VariableScopeAnalysis;
    public:
        explicit VariableScopeAnalysisConsumer(ASTContext *Context) : Visitor(Context) {}
        virtual void HandleTranslationUnit(clang::ASTContext &Context);
    private:
        VariableScopeAnalysisVisitor Visitor;
    };
    
    class VariableScopeAnalysisAction : public clang::ASTFrontendAction {
    public:
        virtual std::unique_ptr<clang::ASTConsumer> CreateASTConsumer(clang::CompilerInstance &Compiler, llvm::StringRef InFile);
    };
};

#endif /* VariableScopeAnalysis_h */
