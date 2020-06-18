set(LLVM_LINK_COMPONENTS support)

add_clang_library(VariableScopeAnalysis
	VariableScopeAnalysis.cpp
	Scope.cpp
	ScopeAnalysis.cpp	
	LINK_LIBS

	clangBasic
	clangTooling
	clangAST
	clangFrontend

	)

