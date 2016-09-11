# JsonMapper

Before running set the parameters at the top of main class.
Here a configuration example is provided
	
	private static final String FILE_INPUT_DIR = "/Users/chucknorris/simulations/istanze-250";
	private static final boolean PRETTIFY_JSON = true;
	private static final boolean ADD_ML_FEATURES = true;
	private static final String FILE_ML_DIR = "/Users/chucknorris/simulations/ml/R";
	

The whole documentations about every parameters can be found in the Main class.
Remember that the map of MlFeatures must have the same parameters contained in Profile and also h, x.


Precondition: Each InstanceData has 1 class. (Still not tested with multiple classes)
Every instanceData.json files are named as ID_* where ID is a number. (Till now Not possible having ID= 1_2_3_... )