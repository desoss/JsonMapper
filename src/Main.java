import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import it.polimi.diceH2020.SPACE4Cloud.shared.inputData.InstanceData;
import it.polimi.diceH2020.SPACE4Cloud.shared.inputDataMultiProvider.InstanceDataMultiProvider;
import it.polimi.diceH2020.SPACE4Cloud.shared.inputDataMultiProvider.JobMLProfilesMap;

public class Main {

	/* EVERY INPUT JSON FILE MUST HAVE THE EXTENSION .json IN ITS NAME */
	private static final String FILE_INPUT_DIR = "/Users/jacoporigoli/Desktop/PROVA/istanze-250";

	private static final String FILE_TEXT_EXT = ".json";
	
	private static final boolean COMBINE_JSONS = true; 
	
	/*
	 * Precondition: input.json in the old format are of public case
	 * 
	 * If SINGLE_FOLDER_INPUT_PRIVATEandPUBLIC_FOLDERS_OUPUT = true, private and public json in the new
	 * format are created in the same parent directory of FILE_INPUT_DIR
	 */
	private static final boolean SINGLE_FOLDER_INPUT_PRIVATEandPUBLIC_FOLDERS_OUPUT = true;
	/*
	 * Clearly if SINGLEINPUT_MULTIOUTPUT is set to true FILE_OUTPUT_DIR is
	 * unnecessary
	 */
	private static final String FILE_OUTPUT_DIR = "#@OUTPUTFOLDERPATH@#";

	/*
	 * Inpus JSONs are of public case, force a private case transformation by
	 * deleting unused parameters an initialing useful ones
	 */
	private static final boolean CONVERT_TO_PRIVATE = false;
	private static final boolean PRETTIFY_JSON = true;

	/*
	 * If ADD_ML_FEATURES = true, MLProfile are added from .json files contained
	 * in FILE_ML_DIR. Every external .json file (containing the MLFeatures)
	 * should named with ApplicationID_. For example if Application id is ID123,
	 * so the file should be named ID123_.json
	 */
	private static final boolean ADD_ML_FEATURES = true;

	private static final String FILE_ML_DIR = "/Users/jacoporigoli/Desktop/dati_ml_renamed/R";

	static ObjectMapper mapper = new ObjectMapper().registerModule(new Jdk8Module());

	public static void main(String[] args) {
		Main findExt = new Main();
		checkInputs();

		if (PRETTIFY_JSON) {
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
		}

		String[] list = findExt.listFile(FILE_INPUT_DIR, FILE_TEXT_EXT);
		
		System.out.println("These JSONs are going to be converted:");
		for (String file : list) {
			String temp = new StringBuffer(FILE_INPUT_DIR).append(File.separator).append(file).toString();
			System.out.println("file : " + temp);
		}

		try {
			if (SINGLE_FOLDER_INPUT_PRIVATEandPUBLIC_FOLDERS_OUPUT) {
				findExt.multiConversion(ADD_ML_FEATURES, FILE_ML_DIR);
			} else {
				findExt.convertJSONs(ADD_ML_FEATURES, FILE_ML_DIR, FILE_OUTPUT_DIR, CONVERT_TO_PRIVATE);
			}
		} catch (IOException e) {
			System.out.println("Error with JSONs serialization!");
		}
		System.out.println("FINISHED");
	}

	private static void checkInputs() {
		if (new File(FILE_INPUT_DIR).isDirectory() == false) {
			System.out.println("Directory does not exists : " + FILE_INPUT_DIR);
			System.exit(0);
		}
		if (!SINGLE_FOLDER_INPUT_PRIVATEandPUBLIC_FOLDERS_OUPUT) {
			if (new File(FILE_OUTPUT_DIR).isDirectory() == false) {
				System.out.println("Directory does not exists : " + FILE_OUTPUT_DIR);
				System.exit(0);
			}
		}
		if (ADD_ML_FEATURES) {
			if (new File(FILE_ML_DIR).isDirectory() == false) {
				System.out.println("Directory does not exists : " + FILE_ML_DIR);
				System.exit(0);
			}
		}
	}

	public String[] listFile(String folder, String ext) {

		GenericExtFilter filter = new GenericExtFilter(ext);
		File dirInput = new File(folder);
		
		String[] list = dirInput.list(filter);

		if (list.length == 0) {
			System.out.println("no files end with : " + ext);
			return new String[0];
		}
		
		return list;
	}

	/*-----------------------JSON-------------------------*/
	private void convertJSONs(boolean addML, String mlDirecotoryPath, String outputDir, boolean convertToPrivate)
			throws JsonParseException, JsonMappingException, IOException {
		String[] list = listFile(FILE_INPUT_DIR, FILE_TEXT_EXT);
		if (list.length == 0) {
			System.out.println("No JSONs to be converted");
			return;
		}

		Map<String, InstanceData> istanceDataMap = new HashMap<>();
		List<InstanceDataMultiProvider> istanceDataMultiProviderList = new ArrayList<>();
		for (String jsonPath : list) {
			InstanceData id = getJsonFromPath(jsonPath);
			if (addML) {
				String mlFileName = jsonPath.split("_")[0] + ".json";
				File f = new File(FILE_ML_DIR + "/" + mlFileName);
				if (f.exists() && !f.isDirectory()) {
					id.setMapJobMLProfiles(getMLProfileFromPath(mlFileName));
				} else {
					System.out
							.println("Not possible to add mlProfile. File not found:" + FILE_ML_DIR + "/" + mlFileName);
				}
			} else {
				System.out.println("MLProfiles not going to be added from external directory");
			}
			istanceDataMap.put(jsonPath, id);
		}
		if(!COMBINE_JSONS){//MAP 1 to 1. Each old .json is converted in 1 new .json
			for (Map.Entry<String, InstanceData> input : istanceDataMap.entrySet()) {
				InstanceDataMultiProvider idmp = new InstanceDataMultiProvider();
				JsonMapper.ConvertJson(input.getValue(),idmp, convertToPrivate);
				mapper.writeValue(new File(outputDir + "/" + input.getKey()), idmp);
				istanceDataMultiProviderList.add(idmp); //??
			}
		}else{//all .json in the directory are combined to create one new .json
			
			InstanceDataMultiProvider idmp = new InstanceDataMultiProvider();
			JsonMapper.CombineJsons(istanceDataMap, idmp,convertToPrivate);
		
			Utils.deleteFiles(getJSONsAbsolutePath(outputDir));
			mapper.writeValue(new File(outputDir + "/" + "combined.json"), idmp);//TODO
			//TODO delete old .json in result directory
		}

	}
	
	private String[] getJSONsAbsolutePath(String folder){
		String[] outputList = listFile(folder,".json");
		String[] outputPathList = new String[outputList.length];
		for (int i=0; i<outputList.length;i++) {
			outputPathList[i] = folder+"/"+outputList[i];
		}
		return outputPathList;
	}

	private InstanceData getJsonFromPath(String path) throws JsonParseException, JsonMappingException, IOException {
		File file = new File(FILE_INPUT_DIR + "/" + path);
		InstanceData instanceData = mapper.readValue(file, InstanceData.class);
		return instanceData;
	}

	private JobMLProfilesMap getMLProfileFromPath(String path)
			throws JsonParseException, JsonMappingException, IOException {
		File file = new File(FILE_ML_DIR + "/" + path);
		JobMLProfilesMap mlProfiles = mapper.readValue(file, JobMLProfilesMap.class);
		return mlProfiles;
	}

	private void multiConversion(boolean addMlFeatures, String fileMLDir)
			throws JsonParseException, JsonMappingException, IOException {
		String outputDirPrivate = FILE_INPUT_DIR + "_new_private";
		String outputDirPublic = FILE_INPUT_DIR + "_new_public";
		Utils.copyFolder(FILE_INPUT_DIR, outputDirPrivate);
		System.out.println("Result folder name: " + outputDirPrivate);
		Utils.copyFolder(FILE_INPUT_DIR, outputDirPublic);
		System.out.println("Result folder name: " + outputDirPublic);
		convertJSONs(addMlFeatures, fileMLDir, outputDirPrivate, true);
		convertJSONs(addMlFeatures, fileMLDir, outputDirPublic, false);
	}

	/*------------------------------------------------*/

	public class GenericExtFilter implements FilenameFilter {

		private String ext;

		public GenericExtFilter(String ext) {
			this.ext = ext;
		}

		public boolean accept(File dir, String name) {
			return (name.endsWith(ext));
		}
	}

}
