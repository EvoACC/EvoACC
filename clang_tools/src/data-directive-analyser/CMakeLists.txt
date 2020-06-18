set(LLVM_LINK_COMPONENTS support)

add_clang_executable(data-directive-analyser
	CommandLineInterface.cpp
  	)

target_link_libraries(data-directive-analyser
	VariableScopeAnalysis
	)
