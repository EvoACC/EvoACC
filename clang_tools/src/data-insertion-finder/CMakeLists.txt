set(LLVM_LINK_COMPONENTS support)

add_clang_executable(data-insertion-finder
	CommandLineInterface.cpp
  	)

target_link_libraries(data-insertion-finder
	VariableScopeAnalysis
	)
