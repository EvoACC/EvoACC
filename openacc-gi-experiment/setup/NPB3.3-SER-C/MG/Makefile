SHELL=/bin/sh
BENCHMARK=mg
BENCHMARKU=MG
DEFINES=-DCRPL_COMP=0
PXM=acc

include ../config/make.def

include ../sys/make.common

OBJS = mg.o \
       ${COMMON}/print_results.o  \
       ${RAND}.o \
       ${COMMON}/c_timers.o \
       ${COMMON}/wtime.o


${PROGRAM}: config ${OBJS}
	${CLINK} ${CLINKFLAGS} -o ${PROGRAM} ${OBJS} ${C_LIB}

.c.o:
	${CCOMPILE} ${DEFINES} $<

mg.o:		mg.c globals.h npbparams.h

clean:
	- rm -f *.o *~ 
	- rm -f npbparams.h core
	- rm -f mg.*.x
