int global_1 = 0;       			//1
extern int global_2;          			//2
int function_1(int input){    			//3
	int toReturn = 0;             		//4
	if(0){	                 		//5
		int a = 0;                    	//6
		toReturn++;                   	//7
	}                        		//8
    						//9
	for(int i=0; i < 10; i++){    		//10
		int bla; //Not initialised    	//11
		for(int j=0; j < 20; j++){    	//12
			toReturn += input;      //13
		}                             	//14
	}                             		//15
    						//16
	return toReturn * global_1;   		//17
}                               		//18 
