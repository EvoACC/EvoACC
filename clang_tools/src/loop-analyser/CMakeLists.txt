set(LLVM_LINK_COMPONENTS support)

add_clang_executable(loop-analyser
	CommandLineInterface.cpp
  	)

target_link_libraries(loop-analyser
	VariableScopeAnalysis
	)
