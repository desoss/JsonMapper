# JsonMapper

Before running set the parameters at the top of main class.
Here an example is provided
	
	private static final String FILE_INPUT_DIR = "/Users/jacoporigoli/simulations/istanze-250_old";
	private static final String FILE_TEXT_EXT = ".json";
	private static final String FILE_OUTPUT_DIR = "/Users/jacoporigoli/simulations/istanze-250_new_private";
	private static final boolean CONVERT_TO_PRIVATE = true;
	private static final boolean PRETTIFY_JSON = true;
	private static final boolean ADD_ML_FEATURES = true;
	private static final String FILE_ML_DIR = "/Users/jacoporigoli/ml_renamed/R";
	
Remember that the map of MlFeatures must have the same parameters contained in Profile and also h, x.