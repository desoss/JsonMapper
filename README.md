# JsonMapper

Before running set the parameters at the top of main class.
Here an example is provided
	
	private static final String FILE_INPUT_DIR = "/Users/chucknorris/simulations/istances-250";
	private static final boolean COMBINE_JSONS = true; 
	private static final boolean SINGLEFOLDERINPUT_TO_PRIVATEandPUBLICFOLDERSOUPUT = true;
	private static final String FILE_TEXT_EXT = ".json";
	private static final String FILE_OUTPUT_DIR = "#@OUTPUTFOLDERPATH@#";
	private static final boolean CONVERT_TO_PRIVATE = false;
	private static final boolean PRETTIFY_JSON = true;
	private static final boolean ADD_ML_FEATURES = true;
	private static final String FILE_ML_DIR = "/Users/chucknorris/ml_renamed/R";
	

The whole documentations about every parameters can be found in the Main class.
Remember that the map of MlFeatures must have the same parameters contained in Profile and also h, x.

Clearly if SINGLEINPUT_MULTIOUTPUT is set to true FILE_OUTPUT_DIR is unnecessary.