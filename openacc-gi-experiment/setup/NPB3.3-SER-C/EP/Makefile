SHELL=/bin/sh
BENCHMARK=ep
BENCHMARKU=EP
DEFINES=-DCRPL_COMP=0
PXM=acc

include ../config/make.def

include ../sys/make.common

OBJS = ep.o \
	${COMMON}/print_results.o \
			 ${RAND}.o \
       ${COMMON}/c_timers.o \
			 ${COMMON}/wtime.o 

${PROGRAM}: config ${OBJS}
	${CLINK} ${CLINKFLAGS} -o ${PROGRAM} ${OBJS} ${C_LIB}

.c.o:
	${CCOMPILE} ${DEFINES} $<

ep.o: ep.c npbparams.h

clean:
	- rm -f *.i *.w2c.h *.w2c.c *.t *.spin *.B *.x *.w2c.ptx *.w2c.cu *.o *~ ../common/*.o
	- rm -f npbparams.h core
	- rm -f ep.*.x
