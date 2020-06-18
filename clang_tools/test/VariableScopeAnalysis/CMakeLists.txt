set(LLVM_LINK_COMPONENTS
	  Support
	    )

   add_clang_unittest(VariableScopeAnalysisTests
	   VariableScopeAnalysisTest.cpp
	   ScopeTest.cpp
	   ScopeAnalysisTest.cpp
	   )

    target_link_libraries(VariableScopeAnalysisTests
	    VariableScopeAnalysis
	    )
