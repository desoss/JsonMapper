import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import it.polimi.diceH2020.SPACE4Cloud.shared.inputData.old.InstanceData_old;
import it.polimi.diceH2020.SPACE4Cloud.shared.inputData.old.Profile_old;
import it.polimi.diceH2020.SPACE4Cloud.shared.inputDataMultiProvider.InstanceDataMultiProvider;
import it.polimi.diceH2020.SPACE4Cloud.shared.inputDataMultiProvider.JobMLProfilesMap;
import it.polimi.diceH2020.SPACE4Cloud.shared.inputDataMultiProvider.TypeVMJobClassKey;

import org.apache.commons.io.FilenameUtils;

public class Main {

	//TODO datasize
	
	
	/* EVERY INPUT JSON FILE MUST HAVE THE EXTENSION .json IN ITS NAME */
	private static final String FILE_INPUT_DIR = "/Users/jacoporigoli/Desktop/FUCKYES/istanze-250";

	private static final String FILE_EXT = ".json";

	/*
	 * Inpus JSONs are of public case, force a private case transformation by
	 * deleting unused parameters an initialing useful ones
	 */
	private static final boolean PRETTIFY_JSON = true;

	/*
	 * If ADD_ML_FEATURES = true, MLProfile are added from .json files contained
	 * in FILE_ML_DIR. Every external .json file (containing the MLFeatures)
	 * should named with ApplicationID_. For example if Application id is ID123,
	 * so the file should be named ID123_.json
	 */
	private static final boolean ADD_ML_FEATURES = true;
	public static final String PRIVATE_PROVIDER_NAME = "inHouse";
	public static final double DATASIZE = 250.0; //[GB]

	private static final String FILE_ML_DIR = "/Users/jacoporigoli/Desktop/dati_ml_renamed/R";
	
	

	static ObjectMapper mapper = new ObjectMapper().registerModule(new Jdk8Module());

	public static void main(String[] args) {
		Main findExt = new Main();
		checkInputs();

		if (PRETTIFY_JSON) {
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
		}

		String[] list = findExt.listFile(FILE_INPUT_DIR, FILE_EXT);

		System.out.println("These JSONs are going to be converted:");
		for (String file : list) {
			String temp = new StringBuffer(FILE_INPUT_DIR).append(File.separator).append(file).toString();
			System.out.println("file : " + temp);
		}

		try {
			findExt.multiConversion(ADD_ML_FEATURES, FILE_ML_DIR);
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

	/**
	 * Create resultFolder for each combination and copy there all the required
	 * .txt
	 */
	private void prepareFolders(List<Combination> combinations, String outputDir)
			throws JsonParseException, JsonMappingException {
		// combinations contains the name of .json and relative .txt files
		for (Combination combination : combinations) {
			String resultDir = outputDir + File.separator  + combination.getInputList().size() + File.separator + combination.getId();
			combination.setResultPath(resultDir);
			Utils.createFolders(resultDir);
			for (String name : combination.getInputList()) {
				String txtName = FilenameUtils.removeExtension(name);
				InstanceData_old insDat;
				try {
					insDat = getJsonFromInputDirectory(name);
				
				for (Map.Entry<TypeVMJobClassKey, Profile_old> entry : insDat.getMapProfiles().entrySet()) {
					String oldMapTxt = txtName+"MapJ"+entry.getKey().getJob()+entry.getKey().getTypeVM()+".txt";
					String oldRsTxt = txtName+"RSJ"+entry.getKey().getJob()+entry.getKey().getTypeVM()+".txt";
					
					String newMapTxt = combination.getId()+"MapJ"+entry.getKey().getJob()+entry.getKey().getTypeVM()+".txt";
					String newRsTxt =  combination.getId()+"RSJ"+entry.getKey().getJob()+entry.getKey().getTypeVM()+".txt";
					try{
						Files.copy(Paths.get(FILE_INPUT_DIR + File.separator + oldMapTxt), Paths.get(resultDir + File.separator + newMapTxt));
					}catch(Exception exception){
						System.out.println("Not Possible to copy file from "+FILE_INPUT_DIR + File.separator + oldMapTxt);
						System.out.println("To "+resultDir + File.separator + newMapTxt);
						System.out.println(exception);
					}
					try{
						Files.copy(Paths.get(FILE_INPUT_DIR + File.separator + oldRsTxt), Paths.get(resultDir + File.separator + newRsTxt));
					}catch(Exception exception){
						System.out.println("Not Possible to copy file from "+FILE_INPUT_DIR + File.separator + oldRsTxt);
						System.out.println("To "+resultDir + File.separator + newRsTxt);
						System.out.println(exception);
					}
				}
				} catch (IOException e) {
					System.out.println(name+"is an invalid InstanceData ");
				}
			}
		}
	}

//	private String getNewTxtName(String oldTxtName, String id){
//		String[] newTxtParts = oldTxtName.split("_");
//		String newTxtName = new String();
//		newTxtName = id.split("_")[0] + "_";
//		for(int i=1;i<newTxtParts.length;i++){
//			newTxtName = 
//		}
//		return newTxtName;
//	}
	
	private List<Combination> getAllPossibleCombinations(ArrayList<String> inputs, List<Combination> outputs) {

		if (inputs.size() != 0) {
			String curr = inputs.remove(0);
			if(!outputs.isEmpty()){
				int size = outputs.size();
				for (int i=0; i<size;i++) {
					List<String> combinedJSONs = new ArrayList<>(outputs.get(i).getInputList());
					combinedJSONs.add(curr);
					outputs.add(new Combination(getJsonFileUID(curr) + outputs.get(i).getId(), new ArrayList<>(combinedJSONs)));
				}
			}
			
			outputs.add(new Combination(FilenameUtils.removeExtension(curr), new ArrayList<>(Arrays.asList(curr))));
			getAllPossibleCombinations(inputs, outputs);
		}
		return outputs;
	}

	private String getJsonFileUID(String jsonFileName) {
		// input SINGLE CLASS OK TODO with multiclass input, so this method need
		// to know how many class has the jsonFile
		String parts[] = jsonFileName.split("_");
		return parts[0];
	}

	private Map<String, InstanceData_old> getInstanceData(String[] paths, boolean addML)
			throws JsonParseException, JsonMappingException, IOException {
		Map<String, InstanceData_old> istanceDataMap = new HashMap<>();
		for (String jsonPath : paths) {
			InstanceData_old id = getJsonFromInputDirectory(jsonPath);
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
		return istanceDataMap;
	}

	/**
	 * Every instancedata.json in "list" is combined to create a single
	 * instancedatamultiprovider.json
	 * 
	 * @param list
	 *            String array with the paths to input instancedata.json
	 * @param addML
	 * @param mlDirecotoryPath
	 * @param outputDir
	 * @param convertToPrivate
	 */
	private void convertJSONs(String id,String[] list, boolean addML, String mlDirecotoryPath, String outputDir, boolean privateCase)
			throws JsonParseException, JsonMappingException, IOException {

		Map<String, InstanceData_old> istanceDataMap = getInstanceData(list, addML);
		InstanceDataMultiProvider idmp = new InstanceDataMultiProvider();
		idmp.setId(id);
		JsonMapper.convertJSONs(istanceDataMap, idmp, privateCase);

		//Utils.deleteFiles(getJSONsAbsolutePath(outputDir)); //no more needed
		
		mapper.writeValue(new File(outputDir + "/" + id +".json"), idmp);// TODO

	}

	public String[] getJSONsAbsolutePath(String folder) {
		String[] outputList = listFile(folder, ".json");
		String[] outputPathList = new String[outputList.length];
		for (int i = 0; i < outputList.length; i++) {
			outputPathList[i] = folder + "/" + outputList[i];
		}
		return outputPathList;
	}

	private InstanceData_old getJsonFromInputDirectory(String filename) throws JsonParseException, JsonMappingException, IOException {
		File file = new File(FILE_INPUT_DIR + "/" + filename);
		InstanceData_old instanceData = mapper.readValue(file, InstanceData_old.class);
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

		String[] list = listFile(FILE_INPUT_DIR, FILE_EXT);
		if (list.length == 0) {
			System.out.println("No JSONs to be converted");
			return;
		}
		List<Combination> combinations_private = getAllPossibleCombinations(new ArrayList<String>(Arrays.asList(list)),
				new ArrayList<Combination>());
		List<Combination> combinations_public = new ArrayList<Combination>(combinations_private);
		
		prepareFolders(combinations_private, outputDirPrivate);

		for (Combination combination : combinations_private) {
			String[] inputArray = new String[combination.jsonList.size()];
			combination.jsonList.toArray(inputArray);// TODO quick and very
														// dirty :P
			convertJSONs(combination.getId(),inputArray, addMlFeatures, FILE_ML_DIR, combination.getResultPath(), true);
		}
		
		for(Combination combination : combinations_public){
			combination.setResultPath(combination.getResultPath().replace("_new_private", "_new_public"));
		}
		prepareFolders(combinations_public, outputDirPublic);

		for (Combination combination : combinations_public) {
			String[] inputArray = new String[combination.jsonList.size()];
			combination.jsonList.toArray(inputArray);// TODO quick and very
														// dirty :P
			convertJSONs(combination.getId(),inputArray, addMlFeatures, FILE_ML_DIR, combination.getResultPath(), false);
		}
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
