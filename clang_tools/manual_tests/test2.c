int global_1 = 0;
extern int global_2;
int function_1(int input){
	int toReturn = 0;
	if(0){
		int a = 0;
		toReturn++;
	}

	for(int i=0; i<10; i++){
		int bla;
		for(int j=0; j<20; j++){
			toReturn += input;
		}
	}
	toReturn++;
	toReturn++;
	for(int j=0; j<100;j++){
		toReturn++;
	}

	return toReturn * global_1;
}
